# Thư viện dùng chung

Nhóm chưa chốt hoặc thêm JDBC driver. Không coi thư mục hiện tại là đã có đủ thư viện để chạy.

- Dùng một phiên bản Microsoft JDBC Driver for SQL Server tương thích JDK 17; ghi phiên bản chính xác, tên JAR, nguồn tải chính thức và cách cấu hình vào đây khi bổ sung.
- Đưa thư viện được phép phân phối vào `lib/`, tham chiếu bằng đường dẫn tương đối từ `.classpath`; không tham chiếu tới Downloads hoặc thư mục cá nhân.
- Project dùng module Java. Thêm driver vào Modulepath và kiểm tra tên module thực tế trước khi thêm `requires` vào `module-info.java`.
- Thư viện mật khẩu, giao diện, QR, in/xuất báo cáo hoặc kiểm thử phải có mục đích rõ ràng, phiên bản cố định và được review. Không thêm hai thư viện thực hiện cùng việc nếu không có lý do.
- Nếu một thư viện không được lưu vào Git, phải cung cấp hướng dẫn tải đúng phiên bản và kiểm tra đủ phụ thuộc để thành viên mới có thể cấu hình lại.
