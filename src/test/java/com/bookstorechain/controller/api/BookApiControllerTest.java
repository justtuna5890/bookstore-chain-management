package com.bookstorechain.controller.api;

import com.bookstorechain.dto.request.BookRequestDTO;
import com.bookstorechain.dto.response.BookResponseDTO;
import com.bookstorechain.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookApiController API Tests")
class BookApiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookApiController bookApiController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookApiController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("GET /api/books - Trả về danh sách đầu sách phân trang HTTP 200")
    void getAll_ShouldReturnPageOfBooks() throws Exception {
        BookResponseDTO bookDto = BookResponseDTO.builder()
                .id(1L)
                .title("Dế Mèn Phiêu Lưu Ký")
                .author("Tô Hoài")
                .coverPrice(new BigDecimal("45000"))
                .build();

        when(bookService.getBooks(eq("Dế Mèn"), any()))
                .thenReturn(new PageImpl<>(List.of(bookDto), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/books")
                        .param("keyword", "Dế Mèn")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Dế Mèn Phiêu Lưu Ký"))
                .andExpect(jsonPath("$.content[0].author").value("Tô Hoài"));
    }

    @Test
    @DisplayName("GET /api/books/{id} - Lấy chi tiết sách theo ID HTTP 200")
    void getById_ShouldReturnBookDetail() throws Exception {
        BookResponseDTO bookDto = BookResponseDTO.builder()
                .id(10L)
                .title("Số Đỏ")
                .author("Vũ Trọng Phụng")
                .build();

        when(bookService.getBookById(10L)).thenReturn(bookDto);

        mockMvc.perform(get("/api/books/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("Số Đỏ"))
                .andExpect(jsonPath("$.author").value("Vũ Trọng Phụng"));
    }

    @Test
    @DisplayName("POST /api/books - Tạo đầu sách thành công HTTP 201 Created")
    void create_WhenValid_ShouldReturnCreated() throws Exception {
        BookRequestDTO dto = new BookRequestDTO();
        dto.setTitle("Lão Hạc");
        dto.setAuthor("Nam Cao");
        dto.setCoverPrice(new BigDecimal("50000"));

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Tạo đầu sách thành công!"))
                .andExpect(jsonPath("$.title").value("Lão Hạc"));
    }
}
