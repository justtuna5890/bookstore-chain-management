package com.bookstorechain.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface FileUploadService {
    
    // Tải lên 1 ảnh (dùng cho Book cover, Avatar)
    String uploadFile(MultipartFile file, String folder);

    // Tải lên nhiều ảnh (dùng cho BookCopy, BuybackItem)
    List<String> uploadMultipleFiles(List<MultipartFile> files, String folder);
}