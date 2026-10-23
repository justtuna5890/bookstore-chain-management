package com.bookstorechain.service.impl;

import com.bookstorechain.dto.request.PromotionRequestDTO;
import com.bookstorechain.dto.response.PromotionDTO;
import com.bookstorechain.entity.Promotion;
import com.bookstorechain.entity.Store;
import com.bookstorechain.enums.PromotionType;
import com.bookstorechain.exception.BusinessException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.PromotionRepository;
import com.bookstorechain.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminPromotionServiceImplTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 23, 12, 0);

    @Mock private PromotionRepository promotionRepository;
    @Mock private StoreRepository storeRepository;

    private AdminPromotionServiceImpl service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        service = new AdminPromotionServiceImpl(promotionRepository, storeRepository, new PromotionMapper(), clock);
    }

    private PromotionRequestDTO request() {
        return PromotionRequestDTO.builder().code(" welcome10 ").description("  Giam 10%  ").type(PromotionType.PERCENT)
                .value(new BigDecimal("10")).minOrderAmount(new BigDecimal("100000")).maxDiscountAmount(new BigDecimal("50000"))
                .startsAt(NOW.minusDays(1)).endsAt(NOW.plusDays(30)).usageLimit(100).onePerCustomer(true).active(true).build();
    }

    private Promotion existing(int usedCount) {
        Promotion promotion = Promotion.builder().code("WELCOME10").type(PromotionType.PERCENT).value(BigDecimal.TEN)
                .startsAt(NOW.minusDays(1)).endsAt(NOW.plusDays(30)).usedCount(usedCount).build();
        promotion.setId(5L);
        when(promotionRepository.findById(5L)).thenReturn(Optional.of(promotion));
        return promotion;
    }

    @Test
    @DisplayName("Tạo mã: chuẩn hoá chữ hoa, cắt khoảng trắng và lưu đầy đủ điều kiện")
    void createNormalizesAndStores() {
        when(promotionRepository.existsByCode("WELCOME10")).thenReturn(false);
        when(promotionRepository.save(any(Promotion.class))).thenAnswer(inv -> inv.getArgument(0));

        PromotionDTO created = service.create(request());

        ArgumentCaptor<Promotion> saved = ArgumentCaptor.forClass(Promotion.class);
        verify(promotionRepository).save(saved.capture());
        assertThat(saved.getValue().getCode()).isEqualTo("WELCOME10");
        assertThat(saved.getValue().getDescription()).isEqualTo("Giam 10%");
        assertThat(saved.getValue().getMaxDiscountAmount()).isEqualByComparingTo("50000");
        assertThat(saved.getValue().getUsedCount()).isZero();
        assertThat(created.getStatus()).isEqualTo("RUNNING");
    }

    @Test
    @DisplayName("Mã trùng bị từ chối")
    void createRejectsDuplicateCode() {
        when(promotionRepository.existsByCode("WELCOME10")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request())).isInstanceOf(BusinessException.class).hasMessageContaining("đã tồn tại");

        verify(promotionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Giảm theo % không vượt 100, giảm cố định phải từ 1 đồng")
    void valueRulesPerType() {
        PromotionRequestDTO tooHigh = request();
        tooHigh.setValue(new BigDecimal("101"));
        assertThatThrownBy(() -> service.create(tooHigh)).hasMessageContaining("100%");

        PromotionRequestDTO tooLowFixed = request();
        tooLowFixed.setType(PromotionType.FIXED);
        tooLowFixed.setValue(new BigDecimal("0.5"));
        assertThatThrownBy(() -> service.create(tooLowFixed)).hasMessageContaining("1 đồng");
    }

    @Test
    @DisplayName("Thời điểm kết thúc phải sau thời điểm bắt đầu")
    void endMustBeAfterStart() {
        PromotionRequestDTO bad = request();
        bad.setEndsAt(bad.getStartsAt());

        assertThatThrownBy(() -> service.create(bad)).isInstanceOf(BusinessException.class).hasMessageContaining("sau thời điểm bắt đầu");
    }

    @Test
    @DisplayName("Mã giảm cố định bỏ qua mức giảm tối đa")
    void fixedPromotionDropsMaxDiscount() {
        when(promotionRepository.existsByCode("WELCOME10")).thenReturn(false);
        when(promotionRepository.save(any(Promotion.class))).thenAnswer(inv -> inv.getArgument(0));
        PromotionRequestDTO fixed = request();
        fixed.setType(PromotionType.FIXED);
        fixed.setValue(new BigDecimal("20000"));

        service.create(fixed);

        ArgumentCaptor<Promotion> saved = ArgumentCaptor.forClass(Promotion.class);
        verify(promotionRepository).save(saved.capture());
        assertThat(saved.getValue().getMaxDiscountAmount()).isNull();
    }

    @Test
    @DisplayName("Gắn chi nhánh khi chọn, báo lỗi nếu chi nhánh không tồn tại")
    void storeIsResolved() {
        when(promotionRepository.existsByCode("WELCOME10")).thenReturn(false);
        when(promotionRepository.save(any(Promotion.class))).thenAnswer(inv -> inv.getArgument(0));
        Store hanoi = TestData.store(1L, "OldBook Ha Noi");
        when(storeRepository.findById(1L)).thenReturn(Optional.of(hanoi));
        when(storeRepository.findById(99L)).thenReturn(Optional.empty());

        PromotionRequestDTO scoped = request();
        scoped.setStoreId(1L);
        assertThat(service.create(scoped).getStoreName()).isEqualTo("OldBook Ha Noi");

        PromotionRequestDTO missing = request();
        missing.setStoreId(99L);
        assertThatThrownBy(() -> service.create(missing)).hasMessageContaining("Chi nhánh");
    }

    @Test
    @DisplayName("Sửa mã không đổi chuỗi mã và không cho giới hạn lượt thấp hơn số lượt đã dùng")
    void updateKeepsCodeAndProtectsUsedCount() {
        Promotion promotion = existing(10);
        when(promotionRepository.save(any(Promotion.class))).thenAnswer(inv -> inv.getArgument(0));

        PromotionRequestDTO tooLow = request();
        tooLow.setCode("DOIMA");
        tooLow.setUsageLimit(9);
        assertThatThrownBy(() -> service.update(5L, tooLow)).hasMessageContaining("(10)");

        PromotionRequestDTO ok = request();
        ok.setCode("DOIMA");
        ok.setUsageLimit(10);
        service.update(5L, ok);

        assertThat(promotion.getCode()).isEqualTo("WELCOME10");
        assertThat(promotion.getUsageLimit()).isEqualTo(10);
        assertThat(promotion.getUsedCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("Tạm dừng và kích hoạt lại mã")
    void setActiveToggles() {
        Promotion promotion = existing(0);
        when(promotionRepository.save(any(Promotion.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.setActive(5L, false).getStatus()).isEqualTo("PAUSED");
        assertThat(promotion.isActive()).isFalse();
        assertThat(service.setActive(5L, true).getStatus()).isEqualTo("RUNNING");
    }

    @Test
    @DisplayName("Mã không tồn tại báo không tìm thấy")
    void unknownIdIsNotFound() {
        when(promotionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(99L)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.update(99L, request())).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.setActive(99L, true)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Danh sách gắn trạng thái đúng cho mã đang chạy, hết hạn và hết lượt")
    void listAssignsStatuses() {
        Promotion running = Promotion.builder().code("A").type(PromotionType.FIXED).value(BigDecimal.TEN)
                .startsAt(NOW.minusDays(1)).endsAt(NOW.plusDays(1)).build();
        Promotion expired = Promotion.builder().code("B").type(PromotionType.FIXED).value(BigDecimal.TEN)
                .startsAt(NOW.minusDays(5)).endsAt(NOW.minusDays(1)).build();
        Promotion exhausted = Promotion.builder().code("C").type(PromotionType.FIXED).value(BigDecimal.TEN)
                .startsAt(NOW.minusDays(1)).endsAt(NOW.plusDays(1)).usageLimit(3).usedCount(3).build();
        PageRequest pageable = PageRequest.of(0, 10);
        when(promotionRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(running, expired, exhausted)));

        Page<PromotionDTO> result = service.list(pageable);

        assertThat(result.getContent()).extracting(PromotionDTO::getStatus).containsExactly("RUNNING", "EXPIRED", "EXHAUSTED");
    }

    @Test
    @DisplayName("Dữ liệu form sửa lấy đủ trường từ mã hiện có")
    void toRequestCopiesAllFields() {
        Promotion promotion = existing(0);
        promotion.setMaxDiscountAmount(new BigDecimal("30000"));
        promotion.setUsageLimit(50);

        PromotionRequestDTO form = service.toRequest(5L);

        assertThat(form.getCode()).isEqualTo("WELCOME10");
        assertThat(form.getMaxDiscountAmount()).isEqualByComparingTo("30000");
        assertThat(form.getUsageLimit()).isEqualTo(50);
        assertThat(form.getStartsAtInput()).isEqualTo("2026-10-22T12:00");
    }
}
