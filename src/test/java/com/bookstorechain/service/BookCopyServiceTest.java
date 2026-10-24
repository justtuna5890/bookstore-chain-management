package com.bookstorechain.service;

import com.bookstorechain.dto.request.BookCopyRequestDTO;
import com.bookstorechain.dto.response.BookCopyResponseDTO;
import com.bookstorechain.entity.Book;
import com.bookstorechain.entity.BookCopy;
import com.bookstorechain.entity.Store;
import com.bookstorechain.enums.BookCondition;
import com.bookstorechain.enums.CopyStatus;
import com.bookstorechain.exception.DuplicateResourceException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.BookCopyRepository;
import com.bookstorechain.repository.BookRepository;
import com.bookstorechain.repository.StoreRepository;
import com.bookstorechain.service.impl.BookCopyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookCopyService Unit Tests")
class BookCopyServiceTest {

    @Mock
    private BookCopyRepository bookCopyRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private BookCopyServiceImpl bookCopyService;

    private Book sampleBook;
    private Store sampleStore;
    private BookCopy sampleCopy;

    @BeforeEach
    void setUp() {
        sampleBook = Book.builder()
                .title("Nhà Giả Kim")
                .author("Paulo Coelho")
                .build();
        sampleBook.setId(10L);

        sampleStore = Store.builder()
                .name("Chi nhánh Hai Bà Trưng")
                .build();
        sampleStore.setId(1L);

        sampleCopy = BookCopy.builder()
                .copyCode("NGK-001")
                .book(sampleBook)
                .store(sampleStore)
                .bookCondition(BookCondition.LIKE_NEW)
                .price(new BigDecimal("75000"))
                .status(CopyStatus.AVAILABLE)
                .build();
        sampleCopy.setId(100L);
    }

    @Test
    @DisplayName("getBookCopyById - Lấy bản sách theo ID thành công")
    void getBookCopyById_WhenExists_ShouldReturnResponseDTO() {
        when(bookCopyRepository.findById(100L)).thenReturn(Optional.of(sampleCopy));

        BookCopyResponseDTO result = bookCopyService.getBookCopyById(100L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getCopyCode()).isEqualTo("NGK-001");
        assertThat(result.getBookTitle()).isEqualTo("Nhà Giả Kim");
        assertThat(result.getStoreName()).isEqualTo("Chi nhánh Hai Bà Trưng");
    }

    @Test
    @DisplayName("getBookCopyById - Ném ngoại lệ khi không tìm thấy ID")
    void getBookCopyById_WhenNotFound_ShouldThrowResourceNotFoundException() {
        when(bookCopyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookCopyService.getBookCopyById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy bản sách với ID: 999");
    }

    @Test
    @DisplayName("createBookCopy - Bắt lỗi khi mã bản sách (copyCode) đã tồn tại")
    void createBookCopy_WhenCopyCodeExists_ShouldThrowDuplicateResourceException() {
        BookCopyRequestDTO dto = new BookCopyRequestDTO();
        dto.setCopyCode("NGK-001");

        when(bookCopyRepository.existsByCopyCode("NGK-001")).thenReturn(true);

        assertThatThrownBy(() -> bookCopyService.createBookCopy(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Mã bản sách này đã tồn tại trong kho");

        verify(bookCopyRepository, never()).save(any());
    }

    @Test
    @DisplayName("createBookCopy - Tạo mới bản sách thành công với trạng thái AVAILABLE")
    void createBookCopy_WhenValid_ShouldSaveBookCopyWithAvailableStatus() {
        BookCopyRequestDTO dto = new BookCopyRequestDTO();
        dto.setCopyCode("NGK-002");
        dto.setBookId(10L);
        dto.setStoreId(1L);
        dto.setBookCondition(BookCondition.GOOD);
        dto.setPrice(new BigDecimal("60000"));
        dto.setNote("Sách nguyên vẹn");

        when(bookCopyRepository.existsByCopyCode("NGK-002")).thenReturn(false);
        when(bookRepository.findById(10L)).thenReturn(Optional.of(sampleBook));
        when(storeRepository.findById(1L)).thenReturn(Optional.of(sampleStore));

        bookCopyService.createBookCopy(dto);

        verify(bookCopyRepository, times(1)).save(any(BookCopy.class));
    }

    @Test
    @DisplayName("updateBookCopyStatus - Cập nhật trạng thái bản sách thành công")
    void updateBookCopyStatus_ShouldUpdateStatus() {
        when(bookCopyRepository.findById(100L)).thenReturn(Optional.of(sampleCopy));

        bookCopyService.updateBookCopyStatus(100L, CopyStatus.SOLD);

        assertThat(sampleCopy.getStatus()).isEqualTo(CopyStatus.SOLD);
        verify(bookCopyRepository, times(1)).save(sampleCopy);
    }
}
