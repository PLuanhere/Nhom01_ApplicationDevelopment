package entity;

import java.time.LocalDateTime;
import java.util.Objects;

/** Hồ sơ tài khoản và danh tính đăng nhập; không đưa mật khẩu hoặc hash sang giao diện. */
public final class TaiKhoan {

    /** Hồ sơ không chứa thông tin mật khẩu; thay đổi hồ sơ được thực hiện tại DAO. */
    private final String maTaiKhoan;
    private final String maNhanVien;
    private final String tenDangNhap;
    private final VaiTro vaiTro;
    private final boolean hoatDong;
    private final LocalDateTime ngayTao;
    /** Cờ thu hồi được cập nhật từ tác vụ nền và đọc trên EDT/các DAO nghiệp vụ. */
    private volatile boolean phienHopLe = true;

    /** Constructor tương thích với các màn/kiểm thử cũ; không cấp quyền quản trị tại TaiKhoanDAO. */
    public TaiKhoan(String tenDangNhap, VaiTro vaiTro) {
        maTaiKhoan = "";
        maNhanVien = "";
        this.tenDangNhap = kiemTraTen(tenDangNhap);
        this.vaiTro = Objects.requireNonNull(vaiTro, "Vai trò không được null.");
        hoatDong = true;
        ngayTao = LocalDateTime.now();
    }

    /**
     * Tạo bản thông tin không chứa mật khẩu.
     * @param maTaiKhoan mã do DAO cấp
     * @param maNhanVien nhân viên sở hữu tài khoản
     * @param tenDangNhap tên đăng nhập duy nhất
     * @param vaiTro một vai trò được cấp
     * @param hoatDong false nếu đã khóa
     * @param ngayTao thời điểm cấp tài khoản
     */
    public TaiKhoan(String maTaiKhoan, String maNhanVien, String tenDangNhap,
            VaiTro vaiTro, boolean hoatDong, LocalDateTime ngayTao) {
        this.maTaiKhoan = Objects.requireNonNull(maTaiKhoan).trim();
        this.maNhanVien = Objects.requireNonNull(maNhanVien).trim();
        if (this.maTaiKhoan.isEmpty() || this.maNhanVien.isEmpty()) {
            throw new IllegalArgumentException("Mã tài khoản và mã nhân viên không được rỗng.");
        }
        this.tenDangNhap = kiemTraTen(tenDangNhap);
        this.vaiTro = Objects.requireNonNull(vaiTro, "Vai trò không được null.");
        this.hoatDong = hoatDong;
        this.ngayTao = Objects.requireNonNull(ngayTao);
    }

    /** Chuẩn hóa và kiểm tra tên, không tự thay đổi cách viết hoa/thường. */
    private static String kiemTraTen(String ten) {
        String tenHopLe = Objects.requireNonNull(ten, "Tên đăng nhập không được null.").trim();
        if (tenHopLe.isEmpty()) { throw new IllegalArgumentException("Tên đăng nhập không được rỗng."); }
        return tenHopLe;
    }

    /** @return tên đăng nhập cố định sau khi cấp */
    public String getTenDangNhap() {
        return tenDangNhap;
    }

    /** @return vai trò tại thời điểm cấp danh tính; kiểm tra hiệu lực trước thao tác nghiệp vụ */
    public VaiTro getVaiTro() {
        return vaiTro;
    }

    /** @return mã tài khoản */
    public String getMaTaiKhoan() { return maTaiKhoan; }

    /** @return mã hồ sơ nhân viên liên kết */
    public String getMaNhanVien() { return maNhanVien; }

    /** @return true nếu tài khoản được đăng nhập */
    public boolean isHoatDong() { return hoatDong; }

    /** @return thời điểm cấp, giữ nguyên khi cập nhật */
    public LocalDateTime getNgayTao() { return ngayTao; }

    /** @return false nếu quyền hoặc mật khẩu đã thay đổi */
    public boolean isPhienHopLe() { return phienHopLe; }

    /** DAO thu hồi danh tính cũ sau khi thay đổi quyền, trạng thái hoặc mật khẩu. */
    public void ketThucPhien() { phienHopLe = false; }

    /** Chặn thao tác từ các màn hình còn giữ danh tính đã bị thu hồi. */
    public void kiemTraPhien() {
        if (!phienHopLe || !hoatDong) {
            throw new SecurityException("Phiên đăng nhập đã hết hiệu lực. Vui lòng đăng nhập lại.");
        }
    }

    /** Kiểm tra mật khẩu cấp mới/đặt lại; bốn tài khoản cũ vẫn được dùng mật khẩu 123. */
    public static void kiemTraMatKhauMoi(char[] matKhau) {
        Objects.requireNonNull(matKhau);
        boolean chuHoa = false;
        boolean chuSo = false;
        boolean dacBiet = false;
        for (char kyTu : matKhau) {
            chuHoa |= Character.isUpperCase(kyTu);
            chuSo |= Character.isDigit(kyTu);
            dacBiet |= !Character.isLetterOrDigit(kyTu) && !Character.isWhitespace(kyTu);
        }
        if (matKhau.length < 8 || !chuHoa || !chuSo || !dacBiet) {
            throw new IllegalArgumentException("Ít nhất 8 ký tự, có chữ hoa, chữ số và ký tự đặc biệt.");
        }
    }

    /** @return thông tin an toàn để hiển thị, không chứa mật khẩu */
    @Override
    public String toString() { return maTaiKhoan + " · " + tenDangNhap; }
}
