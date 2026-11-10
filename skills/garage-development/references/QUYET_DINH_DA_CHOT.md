# Các Quyết Định Kiến Trúc Đã Chốt — Dự Án Garage Đức Dũng

Tài liệu này ghi nhận toàn bộ các quyết định kiến trúc, công nghệ và quy chuẩn nghiệp vụ đã được thống nhất và chốt qua các giai đoạn phát triển dự án.

---

### Quyết định 1: Kiến trúc 3 tầng (3-tier)
* **Chốt**: Giữ cấu trúc `Entity` – `DAO` – `UI` đơn giản.
* **Lý do**: Phù hợp với môn học và kích thước dự án Java Swing, không tạo thêm tầng Controller hoặc Service trung gian để tránh cồng kềnh mã nguồn.

### Quyết định 2: Chuyển đổi dữ liệu sang SQL Server 100%
* **Chốt**: Xóa bỏ toàn bộ việc lưu trữ trên `static List` trong RAM ở các DAO Quản lý (`NhanVienDAO`, `DichVuDAO`, `PhieuNhapKhoDAO`, `BaoCaoDAO`). Toàn bộ đọc/ghi trực tiếp từ SQL Server.
* **Lý do**: Đảm bảo dữ liệu bền vững, ứng dụng tắt đi bật lại không bị mất thông tin nhân viên, danh mục dịch vụ, phiếu nhập và báo cáo.

### Quyết định 3: Quản lý kết nối an toàn đa luồng (`ThreadLocal`)
* **Chốt**: Sử dụng `ThreadLocal<Connection>` trong `ConnectDB.java`.
* **Lý do**: Cho phép mỗi luồng xử lý (Swing EDT, background tasks) sở hữu kết nối riêng, tránh lỗi deadlock hoặc can thiệp transaction giữa các màn hình khác nhau.

### Quyết định 4: Cơ chế mã hóa mật khẩu PBKDF2
* **Chốt**: Sử dụng thuật toán `PBKDF2-HMAC-SHA256` với 600.000 vòng lặp và Salt ngẫu nhiên được lưu trữ trong bảng `dbo.TaiKhoanThongTinBaoMat`.
* **Mật khẩu demo**: `Garage@2026!`.
* **Lý do**: Tuân thủ tiêu chuẩn bảo mật hiện đại của OWASP, thay thế hoàn toàn cơ chế mật khẩu văn bản thô (plaintext).

### Quyết định 5: Quản lý phiên bản báo giá (`rowversion`)
* **Chốt**: Mỗi báo giá được quản lý theo nhóm (`NhomBaoGia`) và phiên bản (`PhienBan`). Sử dụng trường `PhienBanKhoa` (`rowversion`) để kiểm soát xung đột khi sửa nháp đồng thời.
* **Lý do**: Khi khách hàng đã đồng ý hoặc báo giá đã gửi, báo giá trở thành bất biến. Nếu có thay đổi phải sinh phiên bản mới để lưu vết lịch sử.

### Quyết định 6: Phân chia thẩm quyền Bàn giao xe
* **Chốt**: 
  * Thu ngân xác nhận thanh toán và lập hóa đơn.
  * Nhân viên Tiếp nhận thực hiện bàn giao xe thực tế cho khách hàng.
  * Quản lý phê duyệt ngoại lệ khi khách hàng được phép nhận xe trước khi thanh toán hết.
* **Lý do**: Đảm bảo phân tách trách nhiệm giữa thủ quỹ/thu ngân và người giao nhận hiện trường.

### Quyết định 7: Nguồn tính chi phí thực tế cho Hóa đơn
* **Chốt**: Chi phí tính tiền không lấy từ tổng báo giá dự toán ban đầu mà lấy từ View `dbo.vw_ChiPhiThucTe`:
  $$\text{Tổng phải thu} = \text{Tiền công thợ} + \text{Phụ tùng dùng ròng (Xuất - Hoàn)} + \text{Phát sinh khách đồng ý} \pm \text{Thuế/Giảm giá}$$
* **Lý do**: Phản ánh chính xác những gì đã thực sự được sửa chữa trên xe của khách hàng.

### Quyết định 8: Quy trình Nhập kho trong 1 Transaction
* **Chốt**: Gọi Stored Procedure `usp_NhapKho` của SQL Server.
* **Lý do**: Đảm bảo tạo phiếu nhập, chi tiết nhập, tăng tồn kho phụ tùng (`SoLuongTon`) và ghi nhật ký biến động kho (`BienDongKho`) diễn ra nguyên tử (atomic), không bị mất đồng bộ kho.

### Quyết định 9: Snapshot Hóa đơn và Báo cáo
* **Chốt**: Khi lập hóa đơn hoặc lưu báo cáo, chụp lại snapshot đầy đủ các dòng chi tiết vào CSDL.
* **Lý do**: Thay đổi đơn giá trong danh mục sau này không làm sai lệch số tiền và nội dung của các hóa đơn hoặc báo cáo cũ trong quá khứ.

### Quyết định 10: Fallback cấu hình kết nối DB
* **Chốt**: `ConnectDB.java` đọc biến môi trường `GARAGE_DB_*` nhưng cung cấp sẵn fallback mặc định:
  * URL: `jdbc:sqlserver://localhost:1433;databaseName=QuanLiGarage;encrypt=true;trustServerCertificate=true;`
  * User: `sa`
  * Password: `123456` (hoặc `sapassword`)
* **Lý do**: Giúp dự án có thể chạy ngay trên máy của giảng viên hoặc thành viên nhóm mà không bắt buộc phải cấu hình biến môi trường phức tạp trước khi demo.
