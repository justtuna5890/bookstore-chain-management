package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.CartDTO;
import com.bookstorechain.dto.response.CartItemDTO;
import com.bookstorechain.dto.response.CartStoreGroupDTO;
import com.bookstorechain.entity.BookCopy;
import com.bookstorechain.entity.Cart;
import com.bookstorechain.entity.CartItem;
import com.bookstorechain.enums.CopyStatus;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.BookCopyRepository;
import com.bookstorechain.repository.CartItemRepository;
import com.bookstorechain.repository.CartRepository;
import com.bookstorechain.repository.UserRepository;
import com.bookstorechain.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BookCopyRepository bookCopyRepository;
    private final UserRepository userRepository;
    private final UserLocks userLocks;
    private final PlatformTransactionManager transactionManager;

    @Override
    @Transactional(readOnly = true)
    public CartDTO getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) {
            return CartDTO.builder().build();
        }

        Map<Long, CartStoreGroupDTO> groups = new LinkedHashMap<>();
        int itemCount = 0;
        int availableCount = 0;
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : cartItemRepository.findByCartIdOrderByCreatedAtAsc(cart.getId())) {
            CartItemDTO item = toItemDTO(cartItem.getCopy());
            CartStoreGroupDTO group = groups.computeIfAbsent(item.getStoreId(), id -> CartStoreGroupDTO.builder()
                    .storeId(item.getStoreId())
                    .storeName(item.getStoreName())
                    .items(new ArrayList<>())
                    .subtotal(BigDecimal.ZERO)
                    .build());
            group.getItems().add(item);
            itemCount++;
            if (item.isAvailable()) {
                availableCount++;
                group.setSubtotal(group.getSubtotal().add(item.getPrice()));
                subtotal = subtotal.add(item.getPrice());
            }
        }

        return CartDTO.builder()
                .groups(new ArrayList<>(groups.values()))
                .itemCount(itemCount)
                .availableCount(availableCount)
                .subtotal(subtotal)
                .build();
    }

    @Override
    public int addItem(Long userId, Long copyId) {
        // Khoá bên ngoài transaction: transaction của yêu cầu trước đã commit xong khi yêu cầu sau vào
        synchronized (userLocks.lockFor(userId)) {
            return new TransactionTemplate(transactionManager).execute(status -> doAddItem(userId, copyId));
        }
    }

    private int doAddItem(Long userId, Long copyId) {
        BookCopy copy = bookCopyRepository.findById(copyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản sách với ID: " + copyId));
        if (copy.getStatus() != CopyStatus.AVAILABLE) {
            throw new BusinessException("Cuốn sách này hiện không còn sẵn sàng để bán.");
        }

        Cart cart = getOrCreateCart(userId);
        if (cartItemRepository.existsByCartIdAndCopyId(cart.getId(), copyId)) {
            throw new BusinessException("Cuốn sách này đã có trong giỏ hàng của bạn.");
        }

        cartItemRepository.save(CartItem.builder().cart(cart).copy(copy).build());
        return (int) cartItemRepository.countByCartId(cart.getId());
    }

    @Override
    @Transactional
    public void removeItem(Long userId, Long copyId) {
        cartRepository.findByUserId(userId)
                .flatMap(cart -> cartItemRepository.findByCartIdAndCopyId(cart.getId(), copyId))
                .ifPresent(cartItemRepository::delete);
    }

    @Override
    @Transactional
    public void clear(Long userId) {
        cartRepository.findByUserId(userId)
                .ifPresent(cart -> cartItemRepository.deleteAll(cartItemRepository.findByCartIdOrderByCreatedAtAsc(cart.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public long countItems(Long userId) {
        return cartItemRepository.countByCartUserId(userId);
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() ->
                cartRepository.save(Cart.builder().user(userRepository.getReferenceById(userId)).build()));
    }

    private CartItemDTO toItemDTO(BookCopy copy) {
        return CartItemDTO.builder()
                .copyId(copy.getId())
                .copyCode(copy.getCopyCode())
                .bookId(copy.getBook().getId())
                .title(copy.getBook().getTitle())
                .author(copy.getBook().getAuthor())
                .coverImage(copy.getBook().getCoverImage())
                .bookCondition(copy.getBookCondition())
                .price(copy.getPrice())
                .storeId(copy.getStore().getId())
                .storeName(copy.getStore().getName())
                .available(copy.getStatus() == CopyStatus.AVAILABLE)
                .build();
    }
}
