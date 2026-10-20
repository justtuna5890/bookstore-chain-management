package com.bookstorechain.config;

import com.bookstorechain.entity.*;
import com.bookstorechain.enums.*;
import com.bookstorechain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final BuybackRequestRepository buybackRequestRepository;
    private final StockTransferRepository stockTransferRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("========== BẮT ĐẦU KHỞI TẠO DỮ LIỆU MẪU BOOKSTORE CHAIN ==========");

        // 1. Khởi tạo 3 Chi nhánh
        Store storeHanoi = initStore("OldBook Hà Nội — Tràng Tiền", "Số 12 Tràng Tiền, Quận Hoàn Kiếm, Hà Nội", "02439123456");
        Store storeHcm = initStore("OldBook Sài Gòn — Nguyễn Thị Minh Khai", "Số 88 Nguyễn Thị Minh Khai, Quận 1, TP.HCM", "02838123456");
        Store storeDanang = initStore("OldBook Đà Nẵng — Bạch Đằng", "Số 45 Bạch Đằng, Quận Hải Châu, Đà Nẵng", "02363123456");

        // 2. Khởi tạo đủ 4 Roles với tài khoản test
        User admin = initUser("admin", "admin123", "Quản Trị Viên Toàn Hệ Thống", "admin@bookstorechain.com", "0901234567", Role.ADMIN, null);
        User managerHn = initUser("manager", "manager123", "Trần Quản Lý (Hà Nội)", "manager@bookstorechain.com", "0912345678", Role.MANAGER, storeHanoi);
        User staffHn = initUser("staff", "staff123", "Lê Nhân Viên (Hà Nội)", "staff@bookstorechain.com", "0923456789", Role.STAFF, storeHanoi);
        User staffHcm = initUser("staff_hcm", "staff123", "Nguyễn Nhân Viên (Sài Gòn)", "staff_hcm@bookstorechain.com", "0934567890", Role.STAFF, storeHcm);
        User customer1 = initUser("customer", "customer123", "Nguyễn Văn Đọc", "customer@bookstorechain.com", "0945678901", Role.CUSTOMER, null);
        User customer2 = initUser("khachhang", "customer123", "Phạm Thị Yêu Sách", "khachhang@bookstorechain.com", "0956789012", Role.CUSTOMER, null);

        // 3. Khởi tạo danh mục 12 đầu sách kinh điển
        List<Book> books = initBooks();

        // 4. Khởi tạo các bản sao sách trong kho (BookCopy)
        initBookCopies(books, storeHanoi, storeHcm, storeDanang);

        // 5. Khởi tạo yêu cầu thu mua mẫu
        initBuybackRequests(customer1, customer2, storeHanoi, storeHcm);

        // 6. Khởi tạo phiếu điều chuyển kho mẫu
        initStockTransfers(staffHn, storeHanoi, storeHcm);

        log.info("========== HOÀN TẤT KHỞI TẠO DỮ LIỆU MẪU BOOKSTORE CHAIN ==========");
    }

    private Store initStore(String name, String address, String phone) {
        return storeRepository.findByName(name)
                .orElseGet(() -> storeRepository.save(Store.builder()
                        .name(name)
                        .address(address)
                        .phone(phone)
                        .isActive(true)
                        .build()));
    }

    private User initUser(String username, String rawPassword, String fullName, String email, String phone, Role role, Store store) {
        return userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.save(User.builder()
                        .username(username)
                        .password(passwordEncoder.encode(rawPassword))
                        .fullName(fullName)
                        .email(email)
                        .phone(phone)
                        .role(role)
                        .store(store)
                        .isActive(true)
                        .build()));
    }

    private List<Book> initBooks() {
        List<Book> result = new ArrayList<>();

        result.add(getOrCreateBook("Hoàng Tử Bé", "Antoine de Saint-Exupéry", "NXB Kim Đồng", 2021, "Văn học cổ điển",
                "Một tác phẩm triết lý nhẹ nhàng, sâu lắng về tình yêu, sự cô đơn và cái nhìn trong trẻo của trẻ thơ về thế giới người lớn.",
                BigDecimal.valueOf(85000), "https://images.unsplash.com/photo-1544947950-fa07a98d237f?q=80&w=600&auto=format&fit=crop"));

        result.add(getOrCreateBook("Nhà Giả Kim", "Paulo Coelho", "NXB Hội Nhà Văn", 2020, "Tiểu thuyết triết lý",
                "Hành trình đi tìm kho báu của chàng chăn cừu Santiago dạy chúng ta về việc lắng nghe tiếng gọi trái tim và theo đuổi Ước mơ cuộc đời.",
                BigDecimal.valueOf(79000), "https://images.unsplash.com/photo-1512820790803-83ca734da794?q=80&w=600&auto=format&fit=crop"));

        result.add(getOrCreateBook("Tội Ác Và Trừng Phạt", "Fyodor Dostoevsky", "NXB Văn Học", 2019, "Văn học kinh điển Nga",
                "Kiệt tác phân tích tâm lý sâu sắc về sự giằng xé nội tâm, tội lỗi và sự cứu rỗi của chàng sinh viên nghèo Raskolnikov.",
                BigDecimal.valueOf(175000), "https://images.unsplash.com/photo-1532012164546-f432f2e3edd4?q=80&w=600&auto=format&fit=crop"));

        result.add(getOrCreateBook("Chiến Tranh Và Hòa Bình", "Leo Tolstoy", "NXB Văn Học", 2018, "Sử thi kinh điển",
                "Bộ sử thi hoành tráng khắc họa toàn cảnh xã hội Nga trong cơn bão táp của cuộc chiến tranh chống Napoléon năm 1812.",
                BigDecimal.valueOf(350000), "https://images.unsplash.com/photo-1495446815901-a7297e633e8d?q=80&w=600&auto=format&fit=crop"));

        result.add(getOrCreateBook("Đại Gia Gatsby", "F. Scott Fitzgerald", "NXB Hội Nhà Văn", 2022, "Văn học Mỹ",
                "Bản trường ca buồn về Giấc mơ Mỹ, sự phù hoa, ảo vọng và một mối tình si cuồng trong thời đại nhạc Jazz thập niên 1920.",
                BigDecimal.valueOf(98000), "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?q=80&w=600&auto=format&fit=crop"));

        result.add(getOrCreateBook("Trăm Năm Cô Đơn", "Gabriel García Márquez", "NXB Văn Học", 2020, "Hiện thực huyền ảo",
                "Biên niên sử về bảy thế hệ dòng họ Buendía tại ngôi làng huyền thoại Macondo, tượng trưng cho lịch sử của cả châu Mỹ Latinh.",
                BigDecimal.valueOf(160000), "https://images.unsplash.com/photo-1476275466078-4007374efbbe?q=80&w=600&auto=format&fit=crop"));

        result.add(getOrCreateBook("Sapiens: Lược Sử Loài Người", "Yuval Noah Harari", "NXB Tri Thức", 2021, "Lịch sử - Triết học",
                "Khảo sát toàn cảnh hành trình tiến hóa của Homo sapiens từ một sinh vật tầm thường trở thành bá chủ hành tinh.",
                BigDecimal.valueOf(220000), "https://images.unsplash.com/photo-1506880018603-83d5b814b5a6?q=80&w=600&auto=format&fit=crop"));

        result.add(getOrCreateBook("Tư Duy Nhanh Và Chậm", "Daniel Kahneman", "NXB Thế Giới", 2020, "Tâm lý học kinh tế",
                "Công trình đoạt giải Nobel khám phá hai hệ thống chi phối tư duy con người: phản xạ nhanh cảm tính và tính toán chậm logic.",
                BigDecimal.valueOf(250000), "https://images.unsplash.com/photo-1543002588-bfa74002ed7e?q=80&w=600&auto=format&fit=crop"));

        result.add(getOrCreateBook("Đi Tìm Lẽ Sống", "Viktor E. Frankl", "NXB Tổng Hợp TP.HCM", 2021, "Tâm lý trị liệu",
                "Trải nghiệm sinh tồn phi thường trong trại tập trung Auschwitz và phương pháp trị liệu ý nghĩa giúp con người vượt qua nghịch cảnh.",
                BigDecimal.valueOf(88000), "https://images.unsplash.com/photo-1457369804613-52c61a468e7d?q=80&w=600&auto=format&fit=crop"));

        result.add(getOrCreateBook("Dế Mèn Phiêu Lưu Ký", "Tô Hoài", "NXB Kim Đồng", 2022, "Văn học Việt Nam",
                "Tác phẩm văn học thiếu nhi bất hủ thấm đượm tình bạn, khát vọng tự do và tinh thần trượng nghĩa vì hòa bình.",
                BigDecimal.valueOf(65000), "https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?q=80&w=600&auto=format&fit=crop"));

        result.add(getOrCreateBook("Số Đỏ", "Vũ Trọng Phụng", "NXB Văn Học", 2020, "Hiện thực phê phán",
                "Kiệt tác trào phúng sắc sảo vạch trần xã hội Âu hóa nhố nhăng, đảo lộn luân thường đạo lý thời tiền chiến.",
                BigDecimal.valueOf(75000), "https://images.unsplash.com/photo-1516979187457-637abb4f9353?q=80&w=600&auto=format&fit=crop"));

        result.add(getOrCreateBook("Bố Già", "Mario Puzo", "NXB Văn Học", 2021, "Tiểu thuyết hình sự",
                "Bức tranh kinh điển về thế giới ngầm mafia Mỹ và gia tộc Corleone, chứa đựng những triết lý sâu sắc về tình thân và quyền lực.",
                BigDecimal.valueOf(145000), "https://images.unsplash.com/photo-1463320726281-696a485928c7?q=80&w=600&auto=format&fit=crop"));

        return result;
    }

    private Book getOrCreateBook(String title, String author, String publisher, Integer year, String category, String desc, BigDecimal coverPrice, String image) {
        return bookRepository.findAll().stream()
                .filter(b -> b.getTitle().equalsIgnoreCase(title))
                .findFirst()
                .orElseGet(() -> bookRepository.save(Book.builder()
                        .title(title)
                        .author(author)
                        .publisher(publisher)
                        .publicationYear(year)
                        .category(category)
                        .description(desc)
                        .coverPrice(coverPrice)
                        .coverImage(image)
                        .isActive(true)
                        .build()));
    }

    private void initBookCopies(List<Book> books, Store hn, Store hcm, Store dn) {
        if (bookCopyRepository.count() > 0) return;

        // Hà Nội copies
        createCopy("OB-HN-001", books.get(0), hn, BookCondition.LIKE_NEW, BigDecimal.valueOf(60000), "Sách sưu tầm, bìa cứng, nguyên seal giấy bóng kính.");
        createCopy("OB-HN-002", books.get(1), hn, BookCondition.GOOD, BigDecimal.valueOf(50000), "Ruột sách trắng đẹp không tì vết, có chữ ký lưu niệm nhỏ.");
        createCopy("OB-HN-003", books.get(2), hn, BookCondition.FAIR, BigDecimal.valueOf(110000), "Gáy sách hơi ngả màu thời gian, chất lượng đọc hoàn hảo.");
        createCopy("OB-HN-004", books.get(6), hn, BookCondition.LIKE_NEW, BigDecimal.valueOf(165000), "Bản in đặc biệt bìa cứng mạ nhũ vàng tuyệt đẹp.");
        createCopy("OB-HN-005", books.get(7), hn, BookCondition.GOOD, BigDecimal.valueOf(180000), "Đọc tốt, trang giấy dày dặn nguyên vẹn.");
        createCopy("OB-HN-006", books.get(9), hn, BookCondition.LIKE_NEW, BigDecimal.valueOf(48000), "Tranh minh họa màu nước gốc của Tạ Lựu.");

        // Sài Gòn copies
        createCopy("OB-SG-001", books.get(0), hcm, BookCondition.GOOD, BigDecimal.valueOf(55000), "Bản kỷ niệm 75 năm xuất bản.");
        createCopy("OB-SG-002", books.get(4), hcm, BookCondition.LIKE_NEW, BigDecimal.valueOf(75000), "Bìa jacket nguyên vẹn, dịch giả chuẩn xác.");
        createCopy("OB-SG-003", books.get(5), hcm, BookCondition.FAIR, BigDecimal.valueOf(105000), "Chữ rõ nét, giấy xốp chống lóa.");
        createCopy("OB-SG-004", books.get(8), hcm, BookCondition.LIKE_NEW, BigDecimal.valueOf(65000), "Sách được bảo quản trong tủ kính chuyên dụng.");
        createCopy("OB-SG-005", books.get(10), hcm, BookCondition.GOOD, BigDecimal.valueOf(52000), "Bản in ấn phẩm sưu tầm của NXB Văn Học.");
        createCopy("OB-SG-006", books.get(11), hcm, BookCondition.LIKE_NEW, BigDecimal.valueOf(115000), "Bìa da sang trọng, giấy thơm mùi gỗ sồi.");

        // Đà Nẵng copies
        createCopy("OB-DN-001", books.get(1), dn, BookCondition.GOOD, BigDecimal.valueOf(45000), "Trang giấy đều đẹp, thích hợp đọc nghiền ngẫm.");
        createCopy("OB-DN-002", books.get(3), dn, BookCondition.LIKE_NEW, BigDecimal.valueOf(250000), "Bộ 2 tập nguyên hộp bọc vải cao cấp.");
        createCopy("OB-DN-003", books.get(6), dn, BookCondition.GOOD, BigDecimal.valueOf(155000), "Bản dịch của dịch giả Giáp Văn Dương.");
        createCopy("OB-DN-004", books.get(9), dn, BookCondition.FAIR, BigDecimal.valueOf(40000), "Ấn bản thiếu nhi bìa mềm tiện dụng.");
    }

    private void createCopy(String code, Book book, Store store, BookCondition condition, BigDecimal price, String note) {
        if (!bookCopyRepository.existsByCopyCode(code)) {
            bookCopyRepository.save(BookCopy.builder()
                    .copyCode(code)
                    .book(book)
                    .store(store)
                    .bookCondition(condition)
                    .price(price)
                    .status(CopyStatus.AVAILABLE)
                    .note(note)
                    .build());
        }
    }

    private void initBuybackRequests(User customer1, User customer2, Store hanoi, Store hcm) {
        if (buybackRequestRepository.count() > 0) return;

        // Đơn 1: Đang chờ duyệt (PENDING) tại Hà Nội
        BuybackRequest req1 = BuybackRequest.builder()
                .customer(customer1)
                .store(hanoi)
                .status(BuybackStatus.PENDING)
                .staffNote("Khách hàng mang sách đến thẩm định vào sáng thứ Hai.")
                .build();

        BuybackItem item1 = BuybackItem.builder()
                .buybackRequest(req1)
                .bookTitle("Suối Nguồn (The Fountainhead)")
                .bookAuthor("Ayn Rand")
                .proposedCondition(BookCondition.GOOD)
                .offeredPrice(BigDecimal.valueOf(120000))
                .itemImage("https://images.unsplash.com/photo-1512820790803-83ca734da794?q=80&w=600&auto=format&fit=crop")
                .build();

        BuybackItem item2 = BuybackItem.builder()
                .buybackRequest(req1)
                .bookTitle("Hạt Giống Tâm Hồn - Tập 1")
                .bookAuthor("Nhiều tác giả")
                .proposedCondition(BookCondition.FAIR)
                .offeredPrice(BigDecimal.valueOf(25000))
                .itemImage("https://images.unsplash.com/photo-1544947950-fa07a98d237f?q=80&w=600&auto=format&fit=crop")
                .build();

        req1.getItems().add(item1);
        req1.getItems().add(item2);
        buybackRequestRepository.save(req1);

        // Đơn 2: Đã hoàn tất (COMPLETED) tại Sài Gòn
        BuybackRequest req2 = BuybackRequest.builder()
                .customer(customer2)
                .store(hcm)
                .status(BuybackStatus.COMPLETED)
                .staffNote("Đã kiểm tra kỹ tình trạng, thanh toán chuyển khoản thành công cho khách hàng.")
                .build();

        BuybackItem item3 = BuybackItem.builder()
                .buybackRequest(req2)
                .bookTitle("Đắc Nhân Tâm (Bản in xưa)")
                .bookAuthor("Dale Carnegie")
                .proposedCondition(BookCondition.LIKE_NEW)
                .offeredPrice(BigDecimal.valueOf(65000))
                .itemImage("https://images.unsplash.com/photo-1532012164546-f432f2e3edd4?q=80&w=600&auto=format&fit=crop")
                .build();

        req2.getItems().add(item3);
        buybackRequestRepository.save(req2);
    }

    private void initStockTransfers(User creator, Store sender, Store receiver) {
        if (stockTransferRepository.count() > 0) return;

        // Phiếu 1: Đang vận chuyển IN_TRANSIT từ Hà Nội sang Sài Gòn
        StockTransfer transfer1 = StockTransfer.builder()
                .senderStore(sender)
                .receiverStore(receiver)
                .creator(creator)
                .status(TransferStatus.IN_TRANSIT)
                .note("Điều chuyển bổ sung sách văn học cổ điển cho chi nhánh Sài Gòn cuối tuần.")
                .build();

        bookCopyRepository.findByCopyCode("OB-HN-003").ifPresent(copy -> {
            StockTransferItem item = StockTransferItem.builder()
                    .stockTransfer(transfer1)
                    .bookCopy(copy)
                    .build();
            transfer1.getItems().add(item);
        });

        stockTransferRepository.save(transfer1);
    }
}