# Hướng dẫn Agent — Nhom01 Application Development

Áp dụng cho repository **Garage Ô tô Đức Dũng**. Đọc kỹ các tài liệu dưới đây trước khi phân tích hoặc chỉnh sửa dự án:

1. [Skill Phát triển ứng dụng Garage](skills/garage-development/SKILL.md)
2. [Quy định dự án](skills/QUY_DINH_DU_AN.md)
3. [Các quyết định kiến trúc đã chốt](skills/garage-development/references/QUYET_DINH_DA_CHOT.md)
4. [Cẩm nang CSDL QuanLiGarage](skills/garage-database/SKILL.md)
5. Mã nguồn `src`, [README](README.md) và tài liệu phân tích `ISSUES.md`

---

## 1. Nguyên tắc kiến trúc cốt lõi
* **Công nghệ**: Java 17, Java Swing (EDT, `SwingUtilities.invokeLater`), JDBC thuần kết nối Microsoft SQL Server 2016+ (Database `QuanLiGarage`).
* **Mô hình 3 tầng (3-tier)**:
  $$\mathbf{UI \ (Swing)} \quad \longleftrightarrow \quad \mathbf{DAO \ (JDBC)} \quad \longleftrightarrow \quad \mathbf{Entity}$$
  **TUYỆT ĐỐI KHÔNG TỰ Ý THÊM TẦNG SERVICE HOẶC CONTROLLER**. Mọi tương tác cơ sở dữ liệu phải đi trực tiếp qua DAO.
* **Lưu trữ dữ liệu**: Tất cả dữ liệu của toàn bộ 4 phân hệ (Tiếp nhận, Kỹ thuật, Thu ngân, Quản lý) **đều đã được lưu bền vững vào SQL Server**. Không sử dụng `static List` trong RAM để lưu trữ dữ liệu nghiệp vụ.
* **Kết nối Database**: Dùng `ConnectDB.getInstance().getConnection()` hỗ trợ `ThreadLocal<Connection>`, tách biệt kết nối cho từng luồng. Không đóng connection singleton toàn cục.

---

## 2. Bảo mật & Xác thực
* **Mật khẩu**: Lưu trữ bằng băm **PBKDF2-HMAC-SHA256 với 600.000 vòng lặp và Salt ngẫu nhiên** trong bảng `dbo.TaiKhoanThongTinBaoMat`. Không lưu hay so sánh plaintext.
* **Mật khẩu demo cho tất cả tài khoản**: `Garage@2026!`.
* **Tài khoản kết nối SQL Server**: Ưu tiên biến môi trường `GARAGE_DB_*`, có fallback mặc định an toàn cho môi trường dev: `sa / 123456` (hoặc `sapassword`).

---

## 3. Quy chuẩn làm việc với Git
* Khi người dùng yêu cầu phân tích hoặc xem xét, **chỉ phân tích**, không tự ý sửa source.
* **Không tự ý commit, merge, reset hoặc xóa thay đổi** nếu chưa có chỉ đạo tường minh từ người dùng.
* Quy ước đặt tên nhánh:
  * Nhánh tính năng: `feature/<ten-tinh-nang>`
  * Nhánh sửa giao diện: `ui/<ten-giao-dien>`
  * Nhánh sửa lỗi: `fix/<ten-loi>`
* Sau khi merge hoặc hoàn thành nhánh, kiểm tra biên dịch Java 17 với `javac` để đảm bảo 0 lỗi trước khi bàn giao.
