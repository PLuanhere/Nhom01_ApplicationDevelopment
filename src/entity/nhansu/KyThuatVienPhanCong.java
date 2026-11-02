package entity.nhansu;

/** Liên kết hồ sơ và tài khoản do nguồn nhân sự cung cấp, không suy ra từ họ tên. */
public final class KyThuatVienPhanCong {
    private final NhanVien nhanVien;
    private final TaiKhoan taiKhoan;

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public KyThuatVienPhanCong(NhanVien nhanVien, TaiKhoan taiKhoan) {
        if (nhanVien == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.nhanVien = nhanVien;
        if (taiKhoan == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.taiKhoan = taiKhoan;
    }

    /** Trả về nhan vien. */
    public NhanVien getNhanVien() {
        return nhanVien;
    }

    /** Trả về tai khoan. */
    public TaiKhoan getTaiKhoan() {
        return taiKhoan;
    }

    /** Thực hiện duoc phan cong cho thành phần hiện tại. */
    public boolean duocPhanCong() {
        return nhanVien.getBoPhan() == BoPhanNhanVien.KY_THUAT && nhanVien.getTrangThai() == TrangThaiNhanVien.DANG_LAM
                && taiKhoan.getVaiTro() == VaiTro.NHAN_VIEN_KY_THUAT;
    }

    /** Thực hiện to string cho thành phần hiện tại. */
    @Override
    public String toString() {
        return nhanVien.getHoTen() + " · " + taiKhoan.getTenDangNhap();
    }
}
