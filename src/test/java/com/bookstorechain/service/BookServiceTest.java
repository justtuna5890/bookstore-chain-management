package com.bookstorechain.service;

import com.bookstorechain.dto.request.BookRequestDTO;
import com.bookstorechain.dto.response.BookResponseDTO;
import com.bookstorechain.entity.Book;
import com.bookstorechain.exception.DuplicateResourceException;
import com.bookstorechain.exception.ResourceNotFoundException;
import com.bookstorechain.repository.BookRepository;
import com.bookstorechain.service.impl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookService Unit Tests")
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private FileUploadService fileUploadService;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book sampleBook;

    @BeforeEach
    void setUp() {
        sampleBook = Book.builder()
                .title("Đắc Nhân Tâm")
                .author("Dale Carnegie")
                .publisher("NXB Trẻ")
                .publicationYear(2020)
                .category("Kỹ năng sống")
                .coverPrice(new BigDecimal("86000"))
                .coverImage("https://res.cloudinary.com/demo/image/upload/sample.jpg")
                .isActive(true)
                .build();
        sampleBook.setId(1L);
    }

    @Test
    @DisplayName("getBookById - Lấy chi tiết sách thành công khi tồn tại ID")
    void getBookById_WhenExists_ShouldReturnResponseDTO() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));

        BookResponseDTO result = bookService.getBookById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Đắc Nhân Tâm");
        assertThat(result.getAuthor()).isEqualTo("Dale Carnegie");
    }

    @Test
    @DisplayName("getBookById - Bắt lỗi khi không tìm thấy sách")
    void getBookById_WhenNotFound_ShouldThrowResourceNotFoundException() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy sách với ID: 999");
    }

    @Test
    @DisplayName("createBook - Bắt lỗi khi đầu sách cùng tác giả đã tồn tại")
    void createBook_WhenTitleAndAuthorExist_ShouldThrowDuplicateResourceException() {
        BookRequestDTO dto = new BookRequestDTO();
        dto.setTitle("Đắc Nhân Tâm");
        dto.setAuthor("Dale Carnegie");

        when(bookRepository.existsByTitleAndAuthor("Đắc Nhân Tâm", "Dale Carnegie")).thenReturn(true);

        assertThatThrownBy(() -> bookService.createBook(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Đầu sách này của tác giả đã tồn tại trong hệ thống");

        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("createBook - Tạo sách mới thành công và upload ảnh bìa nếu có file đính kèm")
    void createBook_WhenValidWithImageFile_ShouldUploadImageAndSave() {
        MockMultipartFile file = new MockMultipartFile("imageFile", "cover.jpg", "image/jpeg", "imageContent".getBytes());

        BookRequestDTO dto = new BookRequestDTO();
        dto.setTitle("Tuổi Trẻ Đáng Giá Bao Nhiêu");
        dto.setAuthor("Rosie Nguyễn");
        dto.setPublisher("NXB Hội Nhà Văn");
        dto.setPublicationYear(2018);
        dto.setCategory("Kỹ năng");
        dto.setCoverPrice(new BigDecimal("80000"));
        dto.setImageFile(file);

        when(bookRepository.existsByTitleAndAuthor("Tuổi Trẻ Đáng Giá Bao Nhiêu", "Rosie Nguyễn")).thenReturn(false);
        when(fileUploadService.uploadFile(eq(file), eq("books"))).thenReturn("https://cloudinary.com/uploaded.jpg");

        bookService.createBook(dto);

        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(captor.capture());
        Book savedBook = captor.getValue();

        assertThat(savedBook.getTitle()).isEqualTo("Tuổi Trẻ Đáng Giá Bao Nhiêu");
        assertThat(savedBook.getAuthor()).isEqualTo("Rosie Nguyễn");
        assertThat(savedBook.getCoverImage()).isEqualTo("https://cloudinary.com/uploaded.jpg");
        assertThat(savedBook.isActive()).isTrue();
    }

    @Test
    @DisplayName("toggleBookStatus - Đảo cờ trạng thái kinh doanh của đầu sách")
    void toggleBookStatus_ShouldToggleActive() {
        sampleBook.setActive(true);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));

        bookService.toggleBookStatus(1L);

        assertThat(sampleBook.isActive()).isFalse();
        verify(bookRepository, times(1)).save(sampleBook);
    }
}
