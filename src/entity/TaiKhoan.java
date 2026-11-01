package entity;

import java.util.Objects;

/** Danh tính và một vai trò của tài khoản đã xác thực; không chứa mật khẩu hoặc dữ liệu */
public final class TaiKhoan {

    private final String tenDangNhap;
    private final VaiTro vaiTro;
    private final String maNhanVien;

    /* Tạo thông tin tài khoản bất biến để truyền từ DAO sang UI */
    public TaiKhoan(String tenDangNhap, VaiTro vaiTro) {
        this(tenDangNhap, vaiTro, "");
    }

    /** Liên kết bằng mã hồ sơ; bộ phận nhân viên không tự cấp vai trò đăng nhập. */
    public TaiKhoan(String tenDangNhap, VaiTro vaiTro, String maNhanVien) {
        this.tenDangNhap = Objects.requireNonNull(tenDangNhap, "Tên đăng nhập không được null.").trim();
        if (this.tenDangNhap.isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập không được rỗng.");
        }
        this.vaiTro = Objects.requireNonNull(vaiTro, "Vai trò không được null.");
        this.maNhanVien = Objects.requireNonNull(maNhanVien).strip();
    }

    public String getMaNhanVien() { return maNhanVien; }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public VaiTro getVaiTro() {
        return vaiTro;
    }
}
