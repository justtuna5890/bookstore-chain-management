package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.OrderSummaryDTO;
import com.bookstorechain.entity.CustomerOrder;
import com.bookstorechain.entity.Store;
import com.bookstorechain.entity.User;
import com.bookstorechain.enums.OrderStatus;
import com.bookstorechain.enums.Role;
import com.bookstorechain.enums.UserNotificationType;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.CustomerOrderRepository;
import com.bookstorechain.repository.UserRepository;
import com.bookstorechain.dto.response.OrderSummaryDTO;
import com.bookstorechain.service.OrderNotificationService;
import com.bookstorechain.service.ShipperOrderService;
import com.bookstorechain.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShipperOrderServiceImpl implements ShipperOrderService {

    static final int MAX_REASON_LENGTH = 255;

    private final CustomerOrderRepository customerOrderRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final OrderCompletionSupport orderCompletionSupport;
    private final UserNotificationService userNotificationService;
    private final OrderNotificationService orderNotificationService;

    @Override
    @Transactional(readOnly = true)
    public List<OrderSummaryDTO> findAvailable(Long shipperId) {
        Store store = storeOf(requireShipper(shipperId));
        return toSummaries(customerOrderRepository.findByStoreIdAndStatusOrderByIdAsc(store.getId(), OrderStatus.READY_FOR_DELIVERY));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderSummaryDTO> findMine(Long shipperId) {
        requireShipper(shipperId);
        return toSummaries(customerOrderRepository.findByShipperIdAndStatusOrderByIdAsc(shipperId, OrderStatus.SHIPPING));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderSummaryDTO> findHistory(Long shipperId) {
        requireShipper(shipperId);
        return toSummaries(customerOrderRepository.findTop20ByShipperIdAndStatusInOrderByIdDesc(shipperId,
                List.of(OrderStatus.COMPLETED, OrderStatus.DELIVERY_FAILED)));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderSummaryDTO getOrder(Long orderId, Long shipperId) {
        User shipper = requireShipper(shipperId);
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));
        boolean mine = isAssignedTo(order, shipperId);
        boolean waitingInMyStore = order.getStatus() == OrderStatus.READY_FOR_DELIVERY
                && order.getStore().getId().equals(storeOf(shipper).getId());
        if (!mine && !waitingInMyStore) {
            throw new BusinessException("Bạn không có quyền xem đơn hàng này.");
        }
        return orderMapper.toSummary(order);
    }

    @Override
    @Transactional
    public void claim(Long orderId, Long shipperId) {
        User shipper = requireShipper(shipperId);
        CustomerOrder order = lock(orderId);
        if (!order.getStore().getId().equals(storeOf(shipper).getId())) {
            throw new BusinessException("Đơn này thuộc chi nhánh khác, bạn không nhận được.");
        }
        // Đọc trạng thái sau khi đã khoá dòng nên người bấm sau thấy đơn đã sang SHIPPING
        if (order.getStatus() != OrderStatus.READY_FOR_DELIVERY) {
            throw new BusinessException("Đơn này đã có shipper khác nhận hoặc không còn chờ giao.");
        }
        order.setShipper(shipper);
        order.setStatus(OrderStatus.SHIPPING);
        notifyCustomer(order, "Đơn " + order.getOrderCode() + " đang được giao",
                "Shipper " + shipper.getFullName() + " đã nhận đơn của bạn, hãy giữ điện thoại để nhận hàng.");
        OrderSummaryDTO summary = orderMapper.toSummary(order);
        orderNotificationService.notifyDeliveryTaken(summary);
        orderNotificationService.notifyStaffDelivery(summary, "Shipper đã nhận đơn " + order.getOrderCode(),
                shipper.getFullName() + " đang giao đơn này.");
    }

    @Override
    @Transactional
    public void deliver(Long orderId, Long shipperId) {
        requireShipper(shipperId);
        CustomerOrder order = lockAssigned(orderId, shipperId);
        orderCompletionSupport.complete(order);
    }

    @Override
    @Transactional
    public void fail(Long orderId, Long shipperId, String reason) {
        requireShipper(shipperId);
        String cleaned = reason == null ? "" : reason.trim();
        if (cleaned.isEmpty()) {
            throw new BusinessException("Vui lòng ghi lý do giao không thành công.");
        }
        if (cleaned.length() > MAX_REASON_LENGTH) {
            throw new BusinessException("Lý do tối đa " + MAX_REASON_LENGTH + " ký tự.");
        }
        CustomerOrder order = lockAssigned(orderId, shipperId);
        order.setStatus(OrderStatus.DELIVERY_FAILED);
        order.setDeliveryFailureReason(cleaned);
        // Lý do là ghi chú nội bộ của shipper nên không gửi nguyên văn cho khách
        notifyCustomer(order, "Đơn " + order.getOrderCode() + " chưa giao được",
                "Shipper chưa giao được đơn của bạn. " + order.getStore().getName() + " sẽ liên hệ để sắp xếp giao lại.");
        orderNotificationService.notifyStaffDelivery(orderMapper.toSummary(order), "Giao thất bại đơn " + order.getOrderCode(),
                order.getShipper().getFullName() + " không giao được, cần cho giao lại hoặc huỷ đơn.");
    }

    /** Khoá đơn và bảo đảm đang ở SHIPPING do đúng shipper này giữ. */
    private CustomerOrder lockAssigned(Long orderId, Long shipperId) {
        CustomerOrder order = lock(orderId);
        if (!isAssignedTo(order, shipperId)) {
            throw new BusinessException("Đơn này không do bạn phụ trách.");
        }
        if (order.getStatus() != OrderStatus.SHIPPING) {
            throw new BusinessException("Chỉ xử lý được đơn đang giao.");
        }
        return order;
    }

    private CustomerOrder lock(Long orderId) {
        return customerOrderRepository.lockById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));
    }

    private boolean isAssignedTo(CustomerOrder order, Long shipperId) {
        return order.getShipper() != null && order.getShipper().getId().equals(shipperId);
    }

    private User requireShipper(Long shipperId) {
        User user = userRepository.findById(shipperId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản shipper."));
        if (user.getRole() != Role.SHIPPER || !user.isActive()) {
            throw new BusinessException("Tài khoản này không phải shipper đang hoạt động.");
        }
        return user;
    }

    private Store storeOf(User shipper) {
        if (shipper.getStore() == null) {
            throw new BusinessException("Tài khoản của bạn chưa được phân bổ chi nhánh công tác.");
        }
        return shipper.getStore();
    }

    private List<OrderSummaryDTO> toSummaries(List<CustomerOrder> orders) {
        return orders.stream().map(orderMapper::toSummary).collect(Collectors.toList());
    }

    private void notifyCustomer(CustomerOrder order, String title, String message) {
        userNotificationService.notify(order.getCustomer().getId(), UserNotificationType.ORDER, title, message,
                "/orders/" + order.getOrderCode());
    }
}
