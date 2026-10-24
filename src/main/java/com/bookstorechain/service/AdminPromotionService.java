package com.bookstorechain.service;

import com.bookstorechain.dto.request.PromotionRequestDTO;
import com.bookstorechain.dto.response.PromotionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Quản lý mã giảm giá cho quản trị viên và quản lý. */
public interface AdminPromotionService {

    Page<PromotionDTO> list(Pageable pageable);

    /** Ném ResourceNotFoundException nếu không có mã. */
    PromotionDTO get(Long id);

    /** Tạo mã mới; mã được chuẩn hoá chữ hoa. Ném BusinessException nếu trùng mã hoặc vi phạm quy tắc. */
    PromotionDTO create(PromotionRequestDTO request);

    /** Sửa mã (không đổi được chuỗi mã); giới hạn lượt không được nhỏ hơn số lượt đã dùng. */
    PromotionDTO update(Long id, PromotionRequestDTO request);

    /** Bật hoặc tạm dừng mã. Mã không bị xoá để các đơn cũ vẫn tham chiếu được. */
    PromotionDTO setActive(Long id, boolean active);

    /** Dữ liệu form chỉnh sửa của một mã. */
    PromotionRequestDTO toRequest(Long id);
}
