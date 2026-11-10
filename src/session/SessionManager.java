package session;

import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;
import java.time.LocalDateTime;

/**
 * Quản lý phiên làm việc (Session) tập trung của người dùng đang đăng nhập trong ứng dụng Desktop.
 * Giúp các màn hình và chức năng truy xuất thông tin người dùng hiện tại an toàn, nhất quán.
 */
public final class SessionManager {

    private static volatile UserSession phienHienTai;

    private SessionManager() {
    }

    /**
     * Bắt đầu phiên làm việc mới khi người dùng đăng nhập thành công.
     *
     * @param taiKhoan Tài khoản nhân viên vừa xác thực
     */
    public static synchronized void batDauPhien(TaiKhoan taiKhoan) {
        if (taiKhoan == null) {
            throw new IllegalArgumentException("Tài khoản đăng nhập không được null.");
        }
        taiKhoan.kiemTraPhien();
        phienHienTai = new UserSession(taiKhoan, LocalDateTime.now());
    }

    /**
     * Kết thúc phiên làm việc hiện tại khi người dùng đăng xuất.
     */
    public static synchronized void ketThucPhien() {
        if (phienHienTai != null) {
            phienHienTai.ketThuc();
            phienHienTai = null;
        }
    }

    /**
     * Lấy đối tượng phiên làm việc hiện tại.
     *
     * @return UserSession nếu đang đăng nhập, ngược lại null
     */
    public static UserSession getPhienHienTai() {
        return phienHienTai;
    }

    /**
     * Lấy thông tin tài khoản đang đăng nhập.
     *
     * @return TaiKhoan nếu đang đăng nhập, ngược lại null
     */
    public static TaiKhoan getTaiKhoanHienTai() {
        UserSession phien = phienHienTai;
        return phien != null ? phien.getTaiKhoan() : null;
    }

    /**
     * Lấy mã nhân viên đang đăng nhập hiện tại.
     *
     * @return Mã nhân viên (ví dụ NV001) hoặc null nếu chưa đăng nhập
     */
    public static String getMaNhanVienDangNhap() {
        TaiKhoan tk = getTaiKhoanHienTai();
        return tk != null ? tk.getMaNhanVien() : null;
    }

    /**
     * Lấy vai trò của người dùng hiện tại.
     *
     * @return VaiTro hoặc null nếu chưa đăng nhập
     */
    public static VaiTro getVaiTroDangNhap() {
        TaiKhoan tk = getTaiKhoanHienTai();
        return tk != null ? tk.getVaiTro() : null;
    }

    /**
     * Kiểm tra xem hiện có phiên đăng nhập nào đang hoạt động không.
     */
    public static boolean isDangDangNhap() {
        UserSession phien = phienHienTai;
        return phien != null && phien.isHopLe();
    }

    /**
     * Kiểm tra phiên làm việc hiện tại, ném ngoại lệ nếu phiên đã hết hạn hoặc chưa đăng nhập.
     */
    public static void kiemTraPhien() {
        UserSession phien = phienHienTai;
        if (phien == null || !phien.isHopLe()) {
            throw new IllegalStateException("Phiên làm việc đã hết hạn hoặc chưa đăng nhập.");
        }
        phien.getTaiKhoan().kiemTraPhien();
    }

    /**
     * Kiểm tra người dùng hiện tại có thuộc một trong các vai trò được cấp phép hay không.
     */
    public static boolean coQuyen(VaiTro... cacVaiTroChoPhep) {
        VaiTro vaiTroHienTai = getVaiTroDangNhap();
        if (vaiTroHienTai == null || cacVaiTroChoPhep == null) {
            return false;
        }
        for (VaiTro vaiTro : cacVaiTroChoPhep) {
            if (vaiTroHienTai == vaiTro) {
                return true;
            }
        }
        return false;
    }
}
