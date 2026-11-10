---
name: garage-database
description: Cẩm nang quản trị CSDL QuanLiGarage (SQL Server 2016+, Schema, Stored Procedures, Views, Triggers, Dữ liệu mẫu)
---

# Skill: Quản trị CSDL QuanLiGarage

Kỹ năng này hướng dẫn các thao tác với Cơ sở dữ liệu Microsoft SQL Server `QuanLiGarage` của hệ thống Garage Đức Dũng.

---

## 1. Hướng dẫn Chạy & Khởi tạo CSDL

1. **Yêu cầu môi trường**:
   * Microsoft SQL Server 2016 SP1 trở lên (Compatibility Level từ 130).
   * Đã bật chế độ xác thực kết hợp (SQL Server and Windows Authentication mode).
   * Dịch vụ SQL Server đang chạy trên cổng mặc định `1433`.
2. **Khởi tạo dữ liệu**:
   * Mở Microsoft SQL Server Management Studio (SSMS).
   * Mở file [`database/QuanLiGarage.sql`](file:///d:/esclipse/Nhom01_ApplicationDevelopment/database/QuanLiGarage.sql).
   * Chạy toàn bộ file (Nhấn `F5` hoặc `Execute`).
   * Script đã tích hợp kiểm tra có điều kiện, tự động tạo 47 bảng, view, sequence, procedure và nạp sẵn dữ liệu demo ban đầu.

---

## 2. Danh mục Stored Procedure Trọng tâm

| Stored Procedure | Mục đích sử dụng | Bảng bị tác động |
| :--- | :--- | :--- |
| `dbo.usp_NhapKho` | Nhập kho phụ tùng với JSON chi tiết hàng | `PhieuNhapKho`, `ChiTietNhapKho`, `PhuTung`, `BienDongKho` |
| `dbo.usp_LuuBaoCao` | Lưu snapshot báo cáo dạng chỉ số và bảng | `BaoCao`, `ChiSoBaoCao`, `DongBaoCao` |
| `dbo.usp_XacNhanDoiChieu` | Khóa snapshot chi phí thực tế trước khi thu tiền | `DoiChieuChiPhi`, `ChiTietDoiChieu` |
| `dbo.usp_GhiNhanThanhToan` | Ghi nhận lần thu tiền (tiền mặt / chuyển khoản) | `GiaoDichThanhToan` |
| `dbo.usp_XacMinhThanhToan` | Thu ngân xác minh tiền đã thực tế vào quỹ/tài khoản | `GiaoDichThanhToan` |
| `dbo.usp_LapHoaDonDaThu` | Lập hóa đơn bất biến sau khi thu đủ | `HoaDon`, `DongHoaDon`, `PhieuSuaChua` |
| `dbo.usp_BanGiaoXe` | Bàn giao xe thực tế cho khách hàng | `BanGiaoXe` |
| `dbo.usp_PheDuyetBanGiaoNgoaiLe` | Quản lý duyệt bàn giao khi còn công nợ | `PheDuyetBanGiaoNgoaiLe` |
| `dbo.usp_HoanPhuTung` | Hoàn trả phụ tùng thừa về kho có chứng từ | `HoanPhuTung`, `ChiTietHoanPhuTung`, `BienDongKho` |
| `dbo.usp_SuaBaoGiaNhap` | Cập nhật dòng báo giá nháp có kiểm soát `rowversion` | `ChiTietBaoGia`, `BaoGia` |

---

## 3. Danh mục View Nghiệp vụ Tài chính & Bàn giao

* `dbo.vw_ChiPhiThucTe`: Tổng hợp chi phí thực tế gồm công thợ hoàn tất, phụ tùng dùng ròng sau hoàn trả và các hạng mục phát sinh được khách duyệt.
* `dbo.vw_TongPhaiThu`: Tính tổng tiền phải thu sau khi áp thuế và giảm giá.
* `dbo.vw_DoanhThuDaThu`: Nguồn tính doanh thu chuẩn, chỉ tính các hóa đơn và giao dịch đã thu tiền thật, có trừ đi các khoản hoàn tiền/giảm trừ sau hóa đơn.
* `dbo.vw_NghiaVuThanhToan`: Theo dõi chi tiết số tiền phải thu, số tiền đã thu và số tiền còn nợ của từng lệnh sửa chữa.
* `dbo.vw_CanCuBanGiaoMoRong`: Cung cấp căn cứ hợp lệ để kiểm tra điều kiện bàn giao xe.

---

## 4. Tài khoản Demo trong CSDL

Tất cả các tài khoản nhân viên demo đều được thiết lập mật khẩu mặc định: **`Garage@2026!`** (được băm bằng PBKDF2 600.000 vòng có Salt riêng trong `TaiKhoanThongTinBaoMat`).

| Mã NV | Họ và tên | Chức vụ | Vai trò hệ thống |
| :--- | :--- | :--- | :--- |
| `NV001` | Trần Minh Hùng | Quản lý | `QUAN_LY` |
| `NV003` | Ngô Minh Hoàng | Tiếp nhận xe | `NHAN_VIEN_TIEP_NHAN` |
| `NV005` | Lê Minh Tâm | Kỹ thuật sửa chữa | `NHAN_VIEN_KY_THUAT` |
| `NV007` | Nguyễn Minh Vũ | Thu ngân | `THU_NGAN` |
| `NV008` | Lê Thị Thu Hà | Thu ngân | `THU_NGAN` |
| `NV009` | Phạm Văn Tùng | Tiếp nhận xe | `NHAN_VIEN_TIEP_NHAN` |
| `NV010` | Trần Văn Khoa | Kỹ thuật sửa chữa | `NHAN_VIEN_KY_THUAT` |
