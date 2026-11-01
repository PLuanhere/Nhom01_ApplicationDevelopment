package entity;

import java.util.Objects;

/** Liên kết hồ sơ và tài khoản do nguồn nhân sự cung cấp, không suy ra từ họ tên. */
public final class KyThuatVienPhanCong {
    private final NhanVien nhanVien;
    private final TaiKhoan taiKhoan;

    public KyThuatVienPhanCong(NhanVien nhanVien, TaiKhoan taiKhoan) {
        this.nhanVien = Objects.requireNonNull(nhanVien);
        this.taiKhoan = Objects.requireNonNull(taiKhoan);
    }

    public NhanVien getNhanVien() { return nhanVien; }
    public TaiKhoan getTaiKhoan() { return taiKhoan; }
    public boolean duocPhanCong() {
        return nhanVien.getBoPhan() == BoPhanNhanVien.KY_THUAT
                && nhanVien.getTrangThai() == TrangThaiNhanVien.DANG_LAM
                && taiKhoan.getVaiTro() == VaiTro.NHAN_VIEN_KY_THUAT;
    }
    @Override public String toString() { return nhanVien.getHoTen() + " · " + taiKhoan.getTenDangNhap(); }
}
