package com.bookstorechain.service.impl;

import com.bookstorechain.dto.response.PromotionApplicationDTO;
import com.bookstorechain.dto.response.PromotionDTO;
import com.bookstorechain.entity.Promotion;
import com.bookstorechain.entity.Store;
import com.bookstorechain.enums.OrderStatus;
import com.bookstorechain.enums.PromotionType;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.repository.CustomerOrderRepository;
import com.bookstorechain.repository.PromotionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotionServiceImplTest {

    private static final long USER_ID = 7L;
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 23, 12, 0);

    @Mock private PromotionRepository promotionRepository;
    @Mock private CustomerOrderRepository customerOrderRepository;

    private PromotionServiceImpl service;
    private Store hanoi;
    private Store saigon;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        service = new PromotionServiceImpl(promotionRepository, customerOrderRepository, new PromotionMapper(), clock);
        hanoi = TestData.store(1L, "OldBook Ha Noi");
        saigon = TestData.store(2L, "OldBook Sai Gon");
    }

    private Promotion promo(PromotionType type, String value) {
        Promotion promotion = Promotion.builder().code("WELCOME10").type(type).value(new BigDecimal(value))
                .startsAt(NOW.minusDays(10)).endsAt(NOW.plusDays(10)).onePerCustomer(false).build();
        promotion.setId(5L);
        when(promotionRepository.findByCode("WELCOME10")).thenReturn(Optional.of(promotion));
        return promotion;
    }

    private Map<Long, BigDecimal> cart(Object... pairs) {
        Map<Long, BigDecimal> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((Long) pairs[i], new BigDecimal((String) pairs[i + 1]));
        }
        return map;
    }

    @Test
    @DisplayName("Giảm theo phần trăm bị chặn bởi mức giảm tối đa")
    void percentIsCappedByMaxDiscount() {
        Promotion promotion = promo(PromotionType.PERCENT, "10");
        promotion.setMaxDiscountAmount(new BigDecimal("20000"));

        PromotionApplicationDTO result = service.evaluate("WELCOME10", USER_ID, cart(1L, "300000"));

        assertThat(result.getTotalDiscount()).isEqualByComparingTo("20000");
        assertThat(result.getDiscountByStoreId()).containsOnlyKeys(1L);
    }

    @Test
    @DisplayName("Giảm theo phần trăm làm tròn xuống đồng")
    void percentRoundsDown() {
        promo(PromotionType.PERCENT, "10");

        assertThat(service.evaluate("WELCOME10", USER_ID, cart(1L, "123456")).getTotalDiscount()).isEqualByComparingTo("12345");
    }

    @Test
    @DisplayName("Giảm số tiền cố định không vượt quá tạm tính")
    void fixedIsCappedBySubtotal() {
        promo(PromotionType.FIXED, "50000");

        assertThat(service.evaluate("WELCOME10", USER_ID, cart(1L, "30000")).getTotalDiscount()).isEqualByComparingTo("30000");
        assertThat(service.evaluate("WELCOME10", USER_ID, cart(1L, "80000")).getTotalDiscount()).isEqualByComparingTo("50000");
    }

    @Test
    @DisplayName("Giỏ nhiều chi nhánh: phần giảm chia theo tỷ lệ và tổng các phần đúng bằng tổng giảm")
    void discountIsSplitAcrossOrdersExactly() {
        promo(PromotionType.PERCENT, "10");

        PromotionApplicationDTO result = service.evaluate("WELCOME10", USER_ID, cart(1L, "100000", 2L, "50001"));

        assertThat(result.getTotalDiscount()).isEqualByComparingTo("15000");
        assertThat(result.getDiscountByStoreId().values().stream().reduce(BigDecimal.ZERO, BigDecimal::add))
                .isEqualByComparingTo("15000");
        assertThat(result.getDiscountByStoreId().get(1L)).isEqualByComparingTo("10000");
        assertThat(result.getDiscountByStoreId().get(2L)).isEqualByComparingTo("5000");
    }

    @Test
    @DisplayName("Phần dư khi chia không bao giờ làm một đơn bị giảm quá tạm tính của nó")
    void allocationNeverExceedsAnOrderSubtotal() {
        Map<Long, BigDecimal> shares = PromotionServiceImpl.allocate(new BigDecimal("1002"),
                cart(1L, "1000", 2L, "3"));

        assertThat(shares.get(1L)).isLessThanOrEqualTo(new BigDecimal("1000"));
        assertThat(shares.get(2L)).isLessThanOrEqualTo(new BigDecimal("3"));
        assertThat(shares.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add)).isEqualByComparingTo("1002");
    }

    @Test
    @DisplayName("Mã của chi nhánh chỉ tính trên đơn thuộc chi nhánh đó")
    void storeSpecificPromotionOnlyDiscountsThatStore() {
        Promotion promotion = promo(PromotionType.PERCENT, "10");
        promotion.setStore(hanoi);

        PromotionApplicationDTO result = service.evaluate("WELCOME10", USER_ID, cart(1L, "100000", 2L, "900000"));

        assertThat(result.getDiscountByStoreId()).containsOnlyKeys(1L);
        assertThat(result.getTotalDiscount()).isEqualByComparingTo("10000");
    }

    @Test
    @DisplayName("Mã của chi nhánh bị từ chối khi giỏ không có sách của chi nhánh đó")
    void storeSpecificPromotionRejectsOtherStores() {
        promo(PromotionType.PERCENT, "10").setStore(hanoi);

        assertThatThrownBy(() -> service.evaluate("WELCOME10", USER_ID, cart(2L, "100000")))
                .isInstanceOf(BusinessException.class).hasMessageContaining("OldBook Ha Noi");
    }

    @Test
    @DisplayName("Đơn chưa đạt giá trị tối thiểu bị từ chối kèm số tiền cần và số tiền hiện có")
    void minimumOrderAmountIsEnforced() {
        promo(PromotionType.FIXED, "20000").setMinOrderAmount(new BigDecimal("150000"));

        assertThatThrownBy(() -> service.evaluate("WELCOME10", USER_ID, cart(1L, "100000")))
                .isInstanceOf(BusinessException.class).hasMessageContaining("₫150,000").hasMessageContaining("₫100,000");
    }

    @Test
    @DisplayName("Mã tạm dừng, chưa bắt đầu, hết hạn, hết lượt, không tồn tại và mã rỗng đều bị từ chối")
    void unusablePromotionsAreRejected() {
        Promotion paused = promo(PromotionType.FIXED, "10000");
        paused.setActive(false);
        assertThatThrownBy(() -> service.evaluate("WELCOME10", USER_ID, cart(1L, "100000"))).hasMessageContaining("tạm dừng");

        paused.setActive(true);
        paused.setStartsAt(NOW.plusDays(1));
        assertThatThrownBy(() -> service.evaluate("WELCOME10", USER_ID, cart(1L, "100000"))).hasMessageContaining("chưa đến thời gian");

        paused.setStartsAt(NOW.minusDays(10));
        paused.setEndsAt(NOW.minusMinutes(1));
        assertThatThrownBy(() -> service.evaluate("WELCOME10", USER_ID, cart(1L, "100000"))).hasMessageContaining("hết hạn");

        paused.setEndsAt(NOW.plusDays(1));
        paused.setUsageLimit(5);
        paused.setUsedCount(5);
        assertThatThrownBy(() -> service.evaluate("WELCOME10", USER_ID, cart(1L, "100000"))).hasMessageContaining("hết lượt");

        assertThatThrownBy(() -> service.evaluate("KHONGCO", USER_ID, cart(1L, "100000"))).hasMessageContaining("không tồn tại");
        assertThatThrownBy(() -> service.evaluate("  ", USER_ID, cart(1L, "100000"))).hasMessageContaining("nhập mã");
        assertThatThrownBy(() -> service.evaluate(null, USER_ID, cart(1L, "100000"))).hasMessageContaining("nhập mã");
        assertThatThrownBy(() -> service.evaluate("WELCOME10", USER_ID, Map.of())).hasMessageContaining("Giỏ hàng");
    }

    @Test
    @DisplayName("Mã giới hạn mỗi khách một lần bị từ chối nếu khách đã có đơn chưa huỷ dùng mã")
    void onePerCustomerIsEnforced() {
        Promotion promotion = promo(PromotionType.FIXED, "10000");
        promotion.setOnePerCustomer(true);
        when(customerOrderRepository.existsByCustomerIdAndPromotionIdAndStatusNot(USER_ID, 5L, OrderStatus.CANCELLED)).thenReturn(true);

        assertThatThrownBy(() -> service.evaluate("WELCOME10", USER_ID, cart(1L, "100000")))
                .isInstanceOf(BusinessException.class).hasMessageContaining("đã dùng mã");
    }

    @Test
    @DisplayName("Mã không giới hạn mỗi khách thì không tra cứu lịch sử đơn")
    void unlimitedPerCustomerSkipsHistoryLookup() {
        promo(PromotionType.FIXED, "10000");

        service.evaluate("WELCOME10", USER_ID, cart(1L, "100000"));

        verify(customerOrderRepository, never()).existsByCustomerIdAndPromotionIdAndStatusNot(anyLong(), anyLong(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Còn ít lượt hơn số đơn sẽ tạo thì từ chối")
    void notEnoughUsesLeftForAllOrders() {
        Promotion promotion = promo(PromotionType.FIXED, "10000");
        promotion.setUsageLimit(3);
        promotion.setUsedCount(2);

        assertThatThrownBy(() -> service.evaluate("WELCOME10", USER_ID, cart(1L, "100000", 2L, "100000")))
                .isInstanceOf(BusinessException.class).hasMessageContaining("2 đơn hàng");
    }

    @Test
    @DisplayName("Mã được chuẩn hoá chữ hoa và bỏ khoảng trắng trước khi tra cứu")
    void codeIsNormalized() {
        promo(PromotionType.FIXED, "10000");

        assertThat(service.evaluate("  welcome10 ", USER_ID, cart(1L, "100000")).getCode()).isEqualTo("WELCOME10");
    }

    @Test
    @DisplayName("Redeem trừ lượt theo số đơn nhận giảm giá")
    void redeemConsumesOneUsePerDiscountedOrder() {
        promo(PromotionType.FIXED, "10000");
        when(promotionRepository.consume(5L, 2)).thenReturn(1);

        PromotionApplicationDTO result = service.redeem("WELCOME10", USER_ID, cart(1L, "100000", 2L, "100000"));

        assertThat(result.getDiscountByStoreId()).hasSize(2);
        verify(promotionRepository).consume(5L, 2);
    }

    @Test
    @DisplayName("Hai khách tranh lượt cuối: UPDATE có điều kiện trả 0 thì redeem ném lỗi")
    void redeemFailsWhenTheLastUseWasTakenFirst() {
        promo(PromotionType.FIXED, "10000");
        when(promotionRepository.consume(5L, 1)).thenReturn(0);

        assertThatThrownBy(() -> service.redeem("WELCOME10", USER_ID, cart(1L, "100000")))
                .isInstanceOf(BusinessException.class).hasMessageContaining("vừa hết lượt");
    }

    @Test
    @DisplayName("Evaluate không bao giờ trừ lượt")
    void evaluateNeverConsumes() {
        promo(PromotionType.FIXED, "10000");

        service.evaluate("WELCOME10", USER_ID, cart(1L, "100000"));

        verify(promotionRepository, never()).consume(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Trả lượt khi đơn bị huỷ")
    void releaseGivesTheUseBack() {
        service.releaseUsage(5L);

        verify(promotionRepository).release(5L);
    }

    @Test
    @DisplayName("Mã khả dụng: lọc theo chi nhánh trong giỏ và kèm trạng thái đang chạy")
    void availablePromotionsAreFilteredByCartStores() {
        Promotion chainWide = Promotion.builder().code("CHUOI").type(PromotionType.FIXED).value(BigDecimal.TEN)
                .startsAt(NOW.minusDays(1)).endsAt(NOW.plusDays(1)).build();
        Promotion hanoiOnly = Promotion.builder().code("HANOI").type(PromotionType.FIXED).value(BigDecimal.TEN)
                .startsAt(NOW.minusDays(1)).endsAt(NOW.plusDays(1)).store(hanoi).build();
        Promotion saigonOnly = Promotion.builder().code("SAIGON").type(PromotionType.FIXED).value(BigDecimal.TEN)
                .startsAt(NOW.minusDays(1)).endsAt(NOW.plusDays(1)).store(saigon).build();
        when(promotionRepository.findRunning(NOW)).thenReturn(List.of(chainWide, hanoiOnly, saigonOnly));

        List<PromotionDTO> result = service.getAvailablePromotions(Set.of(1L));

        assertThat(result).extracting(PromotionDTO::getCode).containsExactly("CHUOI", "HANOI");
        assertThat(result).allMatch(PromotionDTO::isRunning);
    }
}
