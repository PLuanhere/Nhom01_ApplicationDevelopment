package entity;

import java.util.Objects;

/** Danh tính và một vai trò của tài khoản đã xác thực; không chứa mật khẩu hoặc dữ liệu */
public final class TaiKhoan {

    private final String tenDangNhap;
    private final VaiTro vaiTro;

    /* Tạo thông tin tài khoản bất biến để truyền từ DAO sang UI */
    public TaiKhoan(String tenDangNhap, VaiTro vaiTro) {
        this.tenDangNhap = Objects.requireNonNull(tenDangNhap, "Tên đăng nhập không được null.").trim();
        if (this.tenDangNhap.isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập không được rỗng.");
        }
        this.vaiTro = Objects.requireNonNull(vaiTro, "Vai trò không được null.");
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public VaiTro getVaiTro() {
        return vaiTro;
    }
}
