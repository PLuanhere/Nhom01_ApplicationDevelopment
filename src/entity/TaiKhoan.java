package entity;

import java.time.LocalDateTime;
import java.util.Objects;

/** Hồ sơ tài khoản và danh tính đăng nhập, không chứa mật khẩu hoặc hash. */
public final class TaiKhoan {

    /** Thông tin hồ sơ; thay đổi được thực hiện thông qua DAO. */
    private final String maTaiKhoan;
    private final String maNhanVien;
    private final String tenDangNhap;
    private final VaiTro vaiTro;
    private final boolean hoatDong;
    private final LocalDateTime ngayTao;

    /** Cờ thu hồi được đọc từ EDT và các DAO nghiệp vụ. */
    private volatile boolean phienHopLe = true;

    /** Tương thích các màn/kiểm thử cũ, chưa có liên kết nhân viên. */
    public TaiKhoan(String tenDangNhap, VaiTro vaiTro) {
        this(tenDangNhap, vaiTro, "");
    }

    /**
     * Giữ constructor của main để liên kết hồ sơ nhân viên.
     * Không tự cấp quyền quản trị tại TaiKhoanDAO.
     *
     * @param tenDangNhap tên đăng nhập
     * @param vaiTro vai trò đăng nhập
     * @param maNhanVien mã nhân viên, có thể trống với dữ liệu cũ
     */
    public TaiKhoan(String tenDangNhap, VaiTro vaiTro, String maNhanVien) {
        this.maTaiKhoan = "";
        this.maNhanVien = Objects.requireNonNull(maNhanVien).strip();
        this.tenDangNhap = kiemTraTen(tenDangNhap);
        this.vaiTro = Objects.requireNonNull(vaiTro, "Vai trò không được null.");
        this.hoatDong = true;
        this.ngayTao = LocalDateTime.now();
    }

    /**
     * Tạo hồ sơ tài khoản đầy đủ, không chứa mật khẩu.
     *
     * @param maTaiKhoan mã do DAO cấp
     * @param maNhanVien nhân viên sở hữu tài khoản
     * @param tenDangNhap tên đăng nhập duy nhất
     * @param vaiTro một vai trò được cấp
     * @param hoatDong false nếu tài khoản đã khóa
     * @param ngayTao thời điểm cấp tài khoản
     */
    public TaiKhoan(String maTaiKhoan, String maNhanVien, String tenDangNhap,
            VaiTro vaiTro, boolean hoatDong, LocalDateTime ngayTao) {
        this.maTaiKhoan = Objects.requireNonNull(maTaiKhoan).trim();
        this.maNhanVien = Objects.requireNonNull(maNhanVien).trim();

        if (this.maTaiKhoan.isEmpty() || this.maNhanVien.isEmpty()) {
            throw new IllegalArgumentException(
                    "Mã tài khoản và mã nhân viên không được rỗng.");
        }

        this.tenDangNhap = kiemTraTen(tenDangNhap);
        this.vaiTro = Objects.requireNonNull(vaiTro, "Vai trò không được null.");
        this.hoatDong = hoatDong;
        this.ngayTao = Objects.requireNonNull(ngayTao);
    }

    /** @return mã tài khoản */
    public String getMaTaiKhoan() {
        return maTaiKhoan;
    }

    /** @return mã hồ sơ nhân viên liên kết */
    public String getMaNhanVien() {
        return maNhanVien;
    }

    /** @return tên đăng nhập cố định sau khi cấp */
    public String getTenDangNhap() {
        return tenDangNhap;
    }

    /** @return vai trò được cấp; cần kiểm tra phiên trước thao tác nghiệp vụ */
    public VaiTro getVaiTro() {
        return vaiTro;
    }

    /** @return true nếu tài khoản đang hoạt động */
    public boolean isHoatDong() {
        return hoatDong;
    }

    /** @return thời điểm cấp tài khoản */
    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    /** @return false nếu danh tính đã bị thu hồi */
    public boolean isPhienHopLe() {
        return phienHopLe;
    }

    /** DAO thu hồi danh tính cũ khi thay đổi quyền, trạng thái hoặc mật khẩu. */
    public void ketThucPhien() {
        phienHopLe = false;
    }

    /** Chặn thao tác từ màn hình còn giữ danh tính đã bị thu hồi. */
    public void kiemTraPhien() {
        if (!phienHopLe || !hoatDong) {
            throw new SecurityException(
                    "Phiên đăng nhập đã hết hiệu lực. Vui lòng đăng nhập lại.");
        }
    }

    /** Kiểm tra mật khẩu mới; bốn tài khoản ban đầu vẫn dùng mật khẩu 123. */
    public static void kiemTraMatKhauMoi(char[] matKhau) {
        Objects.requireNonNull(matKhau);

        boolean chuHoa = false;
        boolean chuSo = false;
        boolean dacBiet = false;

        for (char kyTu : matKhau) {
            chuHoa |= Character.isUpperCase(kyTu);
            chuSo |= Character.isDigit(kyTu);
            dacBiet |= !Character.isLetterOrDigit(kyTu)
                    && !Character.isWhitespace(kyTu);
        }

        if (matKhau.length < 8 || !chuHoa || !chuSo || !dacBiet) {
            throw new IllegalArgumentException(
                    "Ít nhất 8 ký tự, có chữ hoa, chữ số và ký tự đặc biệt.");
        }
    }

    /** Chuẩn hóa tên, giữ nguyên cách viết hoa/thường. */
    private static String kiemTraTen(String ten) {
        String tenHopLe = Objects.requireNonNull(
                ten, "Tên đăng nhập không được null.").trim();

        if (tenHopLe.isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập không được rỗng.");
        }

        return tenHopLe;
    }

    /** @return thông tin an toàn để hiển thị, không chứa mật khẩu */
    @Override
    public String toString() {
        return maTaiKhoan + " · " + tenDangNhap;
    }
}