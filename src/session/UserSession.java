package session;

import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;
import java.time.LocalDateTime;

/**
 * Đại diện cho một phiên làm việc cụ thể của nhân viên sau khi đăng nhập.
 */
public final class UserSession {

    private final TaiKhoan taiKhoan;
    private final LocalDateTime thoiGianDangNhap;
    private volatile boolean hopLe = true;

    public UserSession(TaiKhoan taiKhoan, LocalDateTime thoiGianDangNhap) {
        if (taiKhoan == null) {
            throw new IllegalArgumentException("Tài khoản không được null.");
        }
        this.taiKhoan = taiKhoan;
        this.thoiGianDangNhap = thoiGianDangNhap != null ? thoiGianDangNhap : LocalDateTime.now();
    }

    public TaiKhoan getTaiKhoan() {
        return taiKhoan;
    }

    public String getMaNhanVien() {
        return taiKhoan.getMaNhanVien();
    }

    public VaiTro getVaiTro() {
        return taiKhoan.getVaiTro();
    }

    public LocalDateTime getThoiGianDangNhap() {
        return thoiGianDangNhap;
    }

    public boolean isHopLe() {
        return hopLe && taiKhoan.isPhienHopLe();
    }

    public void ketThuc() {
        hopLe = false;
        taiKhoan.ketThucPhien();
    }
}
