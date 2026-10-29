package com.bookstorechain.service.impl;

import com.bookstorechain.dto.request.CustomerBuybackDTO;
import com.bookstorechain.entity.BuybackItem;
import com.bookstorechain.entity.BuybackRequest;
import com.bookstorechain.entity.Store;
import com.bookstorechain.entity.User;
import com.bookstorechain.enums.BookCondition;
import com.bookstorechain.repository.BookCopyRepository;
import com.bookstorechain.repository.BookRepository;
import com.bookstorechain.repository.BuybackRequestRepository;
import com.bookstorechain.repository.StoreRepository;
import com.bookstorechain.repository.UserRepository;
import com.bookstorechain.service.FileUploadService;
import com.bookstorechain.service.NotificationService;
import com.bookstorechain.service.UserNotificationService;
import com.cloudinary.Cloudinary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Ảnh tình trạng sách trong form thu mua của khách: lưu URL vào buyback_item.item_image, lỗi upload không tạo đơn. */
@ExtendWith(MockitoExtension.class)
class BuybackPhotoUploadTest {

    private static final String IMAGE_URL = "https://res.cloudinary.com/demo/image/upload/bookstore-chain/buybacks/a.jpg";

    @Mock private BuybackRequestRepository buybackRequestRepository;
    @Mock private BookCopyRepository bookCopyRepository;
    @Mock private BookRepository bookRepository;
    @Mock private UserNotificationService userNotificationService;
    @Mock private StoreRepository storeRepository;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private NotificationService notificationService;
    @Mock private FileUploadService fileUploadService;

    private BuybackServiceImpl service;
    private User customer;

    @BeforeEach
    void setUp() {
        service = new BuybackServiceImpl(buybackRequestRepository, bookCopyRepository, bookRepository, userNotificationService,
                storeRepository, userRepository, passwordEncoder, notificationService, fileUploadService);
        customer = TestData.customer(5L);
    }

    private void storeExists() {
        Store store = TestData.store(1L, "OldBook Ha Noi");
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
    }

    private CustomerBuybackDTO form(MockMultipartFile image) {
        return CustomerBuybackDTO.builder()
                .storeId(1L)
                .bookTitle("Chien Tranh Va Hoa Binh")
                .condition(BookCondition.GOOD)
                .conditionImage(image)
                .build();
    }

    private BuybackItem savedItem() {
        ArgumentCaptor<BuybackRequest> captor = ArgumentCaptor.forClass(BuybackRequest.class);
        verify(buybackRequestRepository).save(captor.capture());
        return captor.getValue().getItems().get(0);
    }

    @Test
    @DisplayName("Khách đính kèm ảnh: upload vào thư mục buybacks và lưu URL vào item_image")
    void withImageStoresUploadedUrl() {
        storeExists();
        MockMultipartFile image = new MockMultipartFile("conditionImage", "bia.jpg", "image/jpeg", new byte[]{1, 2, 3});
        when(fileUploadService.uploadFile(image, "buybacks")).thenReturn(IMAGE_URL);
        when(buybackRequestRepository.save(any(BuybackRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        service.createCustomerBuyback(form(image), customer);

        assertThat(savedItem().getItemImage()).isEqualTo(IMAGE_URL);
        verify(notificationService).notifyNewBuyback(any(BuybackRequest.class));
    }

    @Test
    @DisplayName("Không đính kèm ảnh: item_image để trống và đơn vẫn được tạo")
    void withoutImageLeavesItemImageEmpty() {
        storeExists();
        when(fileUploadService.uploadFile(null, "buybacks")).thenReturn(null);
        when(buybackRequestRepository.save(any(BuybackRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        service.createCustomerBuyback(form(null), customer);

        assertThat(savedItem().getItemImage()).isNull();
    }

    @Test
    @DisplayName("Upload lỗi: không lưu đơn và không báo nhân viên")
    void uploadFailureCreatesNoRequest() {
        storeExists();
        MockMultipartFile image = new MockMultipartFile("conditionImage", "bia.jpg", "image/jpeg", new byte[]{1});
        when(fileUploadService.uploadFile(eq(image), eq("buybacks"))).thenThrow(new RuntimeException("Upload ảnh thất bại"));

        assertThatThrownBy(() -> service.createCustomerBuyback(form(image), customer))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Upload ảnh thất bại");

        verify(buybackRequestRepository, never()).save(any());
        verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("Dịch vụ upload từ chối file không phải ảnh, trước khi gọi Cloudinary")
    void cloudinaryServiceRejectsNonImage() {
        Cloudinary cloudinary = mock(Cloudinary.class);
        CloudinaryServiceImpl uploader = new CloudinaryServiceImpl(cloudinary);
        MockMultipartFile pdf = new MockMultipartFile("conditionImage", "x.pdf", "application/pdf", new byte[]{1});

        assertThatThrownBy(() -> uploader.uploadFile(pdf, "buybacks"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("hình ảnh");
        verifyNoInteractions(cloudinary);
    }

    @Test
    @DisplayName("Dịch vụ upload từ chối ảnh lớn hơn 5MB, và bỏ qua khi khách không chọn file")
    void cloudinaryServiceRejectsOversizeAndSkipsEmpty() {
        Cloudinary cloudinary = mock(Cloudinary.class);
        CloudinaryServiceImpl uploader = new CloudinaryServiceImpl(cloudinary);
        MockMultipartFile big = new MockMultipartFile("conditionImage", "big.jpg", "image/jpeg", new byte[5 * 1024 * 1024 + 1]);
        MockMultipartFile empty = new MockMultipartFile("conditionImage", "", "application/octet-stream", new byte[0]);

        assertThatThrownBy(() -> uploader.uploadFile(big, "buybacks"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("5MB");
        assertThat(uploader.uploadFile(empty, "buybacks")).isNull();
        assertThat(uploader.uploadFile(null, "buybacks")).isNull();
        verifyNoInteractions(cloudinary);
    }
}
