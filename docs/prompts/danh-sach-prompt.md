# Danh sách prompt AI đã sử dụng (Thành viên B)

Công cụ: **Claude Code** (trợ lý lập trình chạy trong thư mục dự án: đọc và sửa code, chạy Maven, SQL, HTTP, trình duyệt). Các bảng dưới đây chép prompt thật của thành viên B từ lịch sử phiên làm việc (giữ nguyên chính tả), nhóm theo giai đoạn. Cột "Kết quả" ghi những gì AI làm ra theo prompt đó; cột "Kiểm chứng" ghi cách kiểm tra lại, không nhận kết quả khi chưa thử.

Prompt của B thường ngắn vì AI tự đọc repo, tài liệu `Khung_suon_du_an.docx` và kế hoạch đã duyệt trước đó. Chất lượng của mỗi giai đoạn nằm ở **kế hoạch được duyệt trước khi viết code** và ở **bước kiểm chứng**, hai phần này được mô tả ở mục 5.

## 1. Phân tích đề và dựng khung dự án (27/09/2026)

| # | Prompt | Kết quả | Kiểm chứng |
|---|---|---|---|
| 1 | "xem chi tiết 5 tiêu chí chấm điểm trong rubric" | Tóm tắt 5 tiêu chí và trọng số, rút ra việc cần ưu tiên (chất lượng ứng dụng 35%, dùng AI và cộng tác GitHub 20%) | Đối chiếu với file rubric gốc |
| 2 | "15. Xây dựng website quản lý chuỗi cửa hàng sách cũ bằng Spring boot + JSP/JSTL+ Bootstrap + JPA + SQLServer/MySQL/ postgreSQL + JWT+websocket+ Cloudairy đây là đề tài cuối kì của nhóm tôi. phân tích nó. chưa làm ngay" | Phân tích yêu cầu, chức năng, rủi ro; dặn "chưa làm ngay" để chốt hướng trước | Nhóm đọc và chọn SQL Server |
| 3 | "nhóm tôi có 2 người. hãy làm 1 khung sườn dàn bài cho dư án này của nhóm tôi. lưu ý. đây là dự án lớn nên tôi muốn làm nó thật chỉn chu" | Dàn bài: kiến trúc, mô hình dữ liệu, chia việc theo chiều dọc cho 2 người | Nhóm rà lại phân công A/B |
| 4 | "làm thành 1 file word. chỉ text trắng đen, bảng và cây thư mục" | File `Khung_suon_du_an.docx` | Mở file kiểm tra bố cục |
| 5 | "ừ, khởi tạo giúp tôi khung project Spring Boot đi" | Khung dự án Spring Boot: Security, JWT, WebSocket, Cloudinary, 4 entity lõi | Build và chạy được |
| 6 | "nên tạo tên repo là gì" | Gợi ý tên `bookstore-chain-management` | |
| 7 | "https://github.com/Ayanokouj-KiyotakA/bookstore-chain-management.git push project lên đây" | Đẩy khung dự án lên GitHub | Xem lại repo trên GitHub |
| 8 | "tôi muốn đẩy bài nhóm này lên github, làm sao để khi tôi tạo 1 repo mới trên github thì có thể thêm thành viên"; "check lại xem repo của tôi đã có đồng tác giả chưa"; "làm sao để chỉnh sửa họ có vai trò write luôn" | Hướng dẫn mời cộng tác viên và cấp quyền write | Làm trên giao diện GitHub, gửi ảnh chụp để AI chỉ tiếp |

## 2. Kiểm tra hiện trạng và lập kế hoạch phần B (22/10/2026)

| # | Prompt | Kết quả | Kiểm chứng |
|---|---|---|---|
| 9 | "kiểm tra lại toàn bộ repo đến thời điểm hiện tại" | Báo cáo hiện trạng repo sau khi thành viên A làm xong phần của A | Đối chiếu `git log` |
| 10 | "kiểm tra phần của an làm có giống như trong phân công không" | Bảng đối chiếu từng mục phân công của A với code đã có | AI chỉ ra file cụ thể cho mỗi mục |
| 11 | "tôi là thành viên b, những gì đã commit trên repo là của thành viên a đúng không" | Xác nhận tác giả các commit, phần B chưa có | `git log` theo tác giả |
| 12 | "lập kế hoạch chi tiết phần B đi" | Kế hoạch 8 giai đoạn (0 đến 7): chi tiết sách, giỏ hàng, thanh toán, đơn hàng, mã giảm giá, đánh giá và yêu thích, thông báo, chat, doanh thu; quy tắc: chủ yếu thêm file mới, sửa file của A tối thiểu | B duyệt ("đồng ý") trước khi AI viết code |
| 13 | "đồng ý, bắt đầu code giai đoạn 1. trước khi code thì nên chia làm bao nhiêu commit cho phần 1 này" | Chia giai đoạn 1 thành 6 commit nhỏ theo từng lớp | Mỗi commit build được |
| 14 | "đặt cho tôi với mật khẩu là 123456"; "tôi chạy xong rồi, chạy app đi" | Tạo login SQL riêng cho ứng dụng, chạy script khởi tạo, chạy ứng dụng | Trang chủ hiển thị đúng tiếng Việt |

## 3. Cài đặt theo giai đoạn (22 đến 24/10/2026)

| # | Prompt | Kết quả | Kiểm chứng |
|---|---|---|---|
| 15 | (giai đoạn 1, sau khi duyệt kế hoạch) | Chi tiết sách `/books/{id}`, giỏ hàng, thanh toán giữ chỗ nguyên tử, lịch sử đơn, huỷ đơn; 22 test | `OrderConcurrencyIT`: 30 vòng, hai khách một cuốn, không bán trùng, không deadlock |
| 16 | "giai đoạn 2" | Quản lý đơn của nhân viên (máy trạng thái), thông báo đơn mới realtime, chặn đăng ký kênh WebSocket trái phép | Tái hiện lỗi thật: khách đăng nhập nhận được kênh của nhân viên, rồi vá |
| 17 | "giai đoạn 3" | Mã giảm giá (phần trăm hoặc cố định, theo chi nhánh, một lần cho mỗi khách), trang quản trị | `PromotionConcurrencyIT` |
| 18 | "giai đoạn 4"; "làm tiếp phần yêu thích"; "làm tiếp trang tài khoản"; "làm tiếp kiểm duyệt đánh giá" | Đánh giá sách (chỉ người đã mua), yêu thích, `/account`, kiểm duyệt đánh giá | 10 yêu cầu song song của cùng một khách; bài quét JSP chống XSS |
| 19 | "làm tiếp giai đoạn 5" | Thông báo riêng cho từng khách (chuông, trang `/notifications`), tác vụ báo sách yêu thích có hàng lại | Client STOMP thật: khách chỉ nhận thông báo của mình |
| 20 | "làm chat theo chi nhánh" | Chat hỗ trợ khách với từng chi nhánh, giới hạn 20 tin mỗi phút | `ChatServiceIT` lộ hai lỗi mà test dùng mock không thấy (mục 5) |
| 21 | "làm giai đoạn 7 bắt đầu từ dashboard" | Doanh thu trên dashboard, cột `completed_at`, dữ liệu mẫu 27 đơn | Doanh thu khớp SQL thuần; hoàn tất một đơn 165.000 đ thì đúng chi nhánh tăng đúng 165.000 |
| 22 | "kiểm tra toàn bộ repo đến thời điểm hiện tại. các phân công của từng người đã được hoàn thành đến đâu rồi" | Báo cáo tiến độ hai người và danh sách việc còn thiếu | Đếm dòng code theo tác giả bằng `git blame` |
| 23 | "làm tiếp theo thứ tự này" | Doanh thu vào báo cáo Excel và PDF của A; viết mô tả và mở 8 Pull Request xếp chồng | Đọc lại file Excel, trích chữ từ PDF trong test; thử gộp với nhánh mới nhất của A |
| 24 | "làm tiếp bước 3 bắt đầu từ README" | README mới, danh sách prompt này, tài liệu nộp bài | Đối chiếu từng con số với code và script SQL |

## 4. Prompt về Git

Các lệnh "push commit 1 lên, không để đồng tác giả", "bỏ dòng co-author ở 5 commit còn lại rồi push commit 2", "push commit 3 4 5 lên", "push commit 6 lên", "push giai đoạn 5 trước", "push giai đoạn 6 trước", "push giai đoạn 7 dashboard trước" dùng để **đẩy từng giai đoạn lên GitHub theo ý B**; B quyết định thời điểm. Theo yêu cầu của B, commit của B không có dòng `Co-Authored-By`.

## 5. Cách làm việc với AI và những gì B kiểm soát

**Quy trình mỗi giai đoạn:** B yêu cầu, AI đọc code liên quan và đề xuất cách làm, viết code, viết test, chạy `mvn test`, chạy thật ứng dụng trên SQL Server (HTTP, trình duyệt, client WebSocket), dọn dữ liệu thử về trạng thái mẫu, chia commit nhỏ, rồi B yêu cầu push.

**Quyết định do B đưa ra:** chọn SQL Server; chia việc A/B; duyệt kế hoạch 8 giai đoạn; thứ tự làm tiếp; thời điểm push; không ghi đồng tác giả; mở Pull Request để A review thay vì ghi thẳng vào nhánh của A.

**Lỗi tìm ra nhờ chạy thật (test dùng mock không thấy), đều có test hoặc bài quét chặn lại:**

| Lỗi | Cách phát hiện | Cách sửa |
|---|---|---|
| Khách đăng nhập đăng ký được kênh WebSocket của nhân viên | Viết client STOMP và thử đăng ký | `WebSocketSubscriptionInterceptor` từ chối mặc định |
| Mất số tin chưa đọc khi hai bên nhắn cùng lúc | `ChatServiceIT` chạy song song | Tra id bằng truy vấn giá trị đơn rồi mới khoá dòng và nạp thực thể |
| `LazyInitializationException` trong chat | Chạy thật | Bỏ `clearAutomatically` giữa nghiệp vụ |
| Deadlock khi nhiều yêu cầu ghi cùng lúc trên SQL Server | `OrderConcurrencyIT` | Khoá theo từng người dùng, giữ chỗ bằng một câu UPDATE có điều kiện |
| XSS lưu trữ: JSP in tên, địa chỉ, ghi chú không thoát HTML | Rà soát code và thử dữ liệu có thẻ HTML | `fn:escapeXml`, `JspOutputEscapingTest` |
| XSS trong thanh thông báo quản trị của A (`innerHTML` dữ liệu do khách nhập) | Thử tin chat chứa `<img onerror>` | `escapeHtml` và bài quét chống gỡ nhầm |
| Trang trả 200 nhưng bị cắt giữa chừng (`${x.empty}` là từ khoá EL) | Số liệu hiển thị thiếu | Đổi tên thuộc tính, thêm bài quét từ khoá EL |
| Trang chủ lỗi 500 do thiếu khai báo taglib `fn` | Mở trang trên trình duyệt | `JspTaglibDeclarationTest` |
| Thống kê tồn kho của A tính cả sách đã bán | Đối chiếu số liệu mẫu với SQL (68 bản, còn 42 trong kho) | Loại `SOLD` khỏi các truy vấn tồn kho |
| Nhân viên chưa gán chi nhánh xem được báo cáo toàn chuỗi | Gỡ chi nhánh của một nhân viên rồi thử | Trả 403 |

**Kỷ luật kiểm thử:** sau khi viết test, AI cố ý đưa lỗi vào code (đảo điều kiện, bỏ một bước) để chắc test thật sự bắt được; test nào không bắt được thì bị viết lại.

**Giới hạn:** AI viết phần lớn mã của phần B theo yêu cầu của B; B chịu trách nhiệm hiểu, chạy, kiểm tra và giải thích mã khi vấn đáp. Số liệu và hành vi nêu trong Pull Request đều đã được chạy thật trước khi ghi.
