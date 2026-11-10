# Quy định Dự án — Nhom01 Application Development
**Dự án**: Hệ thống Quản lý Dịch vụ Sửa chữa Garage Ô tô Đức Dũng  
**Khoa CNTT - Đại học Công nghiệp TP.HCM (IUH)**  
**Phiên bản**: 3.5 (Cập nhật tháng 11/2026 sau khi hoàn thành tích hợp CSDL SQL Server)

---

## 1. Quy chuẩn mã nguồn & Kiến trúc

1. **Mô hình kiến trúc**:
   * Mô hình 3 lớp nghiêm ngặt: `UI` – `DAO` – `Entity`.
   * Giao diện UI giao tiếp trực tiếp với DAO để đọc/ghi dữ liệu. Không xây dựng các tầng trung gian như `Service`, `Repository`, `Controller`.
2. **Quy chuẩn đặt tên (Naming Conventions)**:
   * **Tên Class / Interface**: PascalCase bằng tiếng Việt không dấu (ví dụ: `KhachHangDAO`, `NhanVienFormPanel`, `ChiTietSuaChua`).
   * **Tên phương thức / Thuộc tính**: camelCase bằng tiếng Việt không dấu (ví dụ: `layDanhSachNhanVien()`, `soDienThoai`, `maPhieu`).
   * **Hằng số**: UPPER_SNAKE_CASE (ví dụ: `SO_VONG_BAM_PBKDF2`, `USER_MAC_DINH`).
3. **Quy chuẩn kiểu dữ liệu**:
   * **Tiền tệ / Giá cả**: Bắt buộc dùng `BigDecimal` hoặc `long`, `double` trong giao diện; trong CSDL là `DECIMAL(18,2)`. Tuyệt đối không dùng `float` để tính toán tiền tệ.
   * **Thời gian**: Dùng `LocalDate` cho ngày tháng, `LocalDateTime` cho ngày giờ cụ thể.
4. **Quy chuẩn giao diện Swing**:
   * Mọi thao tác cập nhật UI phải chạy trên **Event Dispatch Thread (EDT)** thông qua `SwingUtilities.invokeLater()`.
   * Các tác vụ I/O nặng hoặc truy vấn SQL lớn nên dùng `SwingWorker` để không gây đơ giao diện.
   * Kế thừa giao diện đồng nhất: Sử dụng `MainFrame` chung và các component trong `ui.component` (`UiTheme`, `AppHeaderPanel`, `SidebarPanel`).

---

## 2. Phân quyền và 4 Vai trò người dùng

Hệ thống phục vụ 4 vai trò chính với các trách nhiệm và giới hạn độc lập:

1. **Quản lý (`QUAN_LY`)**:
   * Quản lý thông tin nhân viên, tài khoản, dịch vụ, phụ tùng và nhập kho.
   * Xem báo cáo doanh thu, thống kê hiệu suất, xuất dữ liệu báo cáo.
   * Phê duyệt các trường hợp ngoại lệ (bàn giao xe khi còn nợ, hủy hóa đơn/phiếu).
2. **Nhân viên Tiếp nhận (`NHAN_VIEN_TIEP_NHAN`)**:
   * Quản lý hồ sơ xe và khách hàng.
   * Lập phiếu tiếp nhận xe, phân công kỹ thuật viên kiểm tra ban đầu.
   * Lập báo giá, gửi khách hàng và ghi nhận phản hồi (đồng ý / từ chối).
   * Phát hành lệnh sửa chữa sau khi báo giá được duyệt.
   * Bàn giao xe cho khách hàng sau khi đối chiếu nghĩa vụ thanh toán hoặc có ngoại lệ.
3. **Nhân viên Kỹ thuật (`NHAN_VIEN_KY_THUAT`)**:
   * Tiếp nhận xe và lập báo cáo kiểm tra tình trạng ban đầu.
   * Thực hiện sửa chữa theo lệnh sửa chữa, cập nhật tiến độ công việc.
   * Ghi nhận phụ tùng thực dùng và hoàn phụ tùng thừa về kho có chứng từ.
   * Đề xuất các hạng mục công việc phát sinh trong quá trình sửa chữa.
4. **Thu ngân (`THU_NGAN`)**:
   * Đối chiếu chi phí thực tế (tiền công, phụ tùng dùng ròng, chi phí phát sinh).
   * Ghi nhận thanh toán (tiền mặt / chuyển khoản), xác minh giao dịch.
   * Lập và in hóa đơn bán lẻ theo thông tin chính thức của Garage.

---

## 3. Quy tắc an toàn dữ liệu & Giao dịch (Transaction)

1. **Toàn vẹn Transaction**:
   * Khi một thao tác nghiệp vụ tác động nhiều bảng (ví dụ: Nhập kho, Lập hóa đơn, Bàn giao xe), bắt buộc phải thực hiện trong một Transaction (`Connection.setAutoCommit(false)`, `commit()`, `rollback()`) hoặc gọi Stored Procedure có sẵn Transaction trong SQL Server.
2. **Loại bỏ dữ liệu tạm trên RAM**:
   * Tuyệt đối không dùng `static List` hoặc các biến tĩnh lưu dữ liệu nghiệp vụ lâu dài. Mọi thay đổi phải lập tức phản ánh vào database `QuanLiGarage`.
3. **Quản lý kết nối**:
   * Kết nối DB được cấp qua `ConnectDB.getInstance().getConnection()`.
   * Vì dùng cơ chế `ThreadLocal`, mỗi luồng xử lý độc lập an toàn không tranh chấp tài nguyên.

---

## 4. Quy chuẩn làm việc với Git

1. **Quy chuẩn thông điệp Commit (Conventional Commits)**:
   * `feat: <nội dung>` (Thêm tính năng mới)
   * `fix: <nội dung>` (Sửa lỗi nghiệp vụ / kỹ thuật)
   * `refactor: <nội dung>` (Tái cấu trúc code, dọn dẹp không đổi hành vi)
   * `ui: <nội dung>` (Chỉnh sửa giao diện Swing)
   * `docs: <nội dung>` (Cập nhật tài liệu, README, hướng dẫn)
2. **Quy chuẩn phân nhánh**:
   * `main`: Nhánh production ổn định nhất.
   * `feature/...`: Nhánh phát triển tính năng mới.
   * `fix/...`: Nhánh sửa lỗi.
