package entity.nhansu;

import java.time.LocalDateTime;

/** Lưu thông tin tài khoản, vai trò và trạng thái phiên đăng nhập. */
public final class TaiKhoan {

    private final String maTaiKhoan;
    private final String maNhanVien;
    private final String tenDangNhap;
    private final VaiTro vaiTro;
    private final boolean hoatDong;
    private final LocalDateTime ngayTao;

    private boolean phienHopLe = true;

    /** Tạo thông tin tài khoản với vai trò và trạng thái truy cập. */
    public TaiKhoan(String tenDangNhap, VaiTro vaiTro) {
        this(tenDangNhap, vaiTro, "");
    }

    /** Tạo thông tin tài khoản với vai trò và trạng thái truy cập. */
    public TaiKhoan(String tenDangNhap, VaiTro vaiTro, String maNhanVien) {
        this.maTaiKhoan = "";
        if (maNhanVien == null) {
            throw new IllegalArgumentException("Thiếu maNhanVien.");
        }
        this.maNhanVien = maNhanVien.trim();
        this.tenDangNhap = kiemTraTen(tenDangNhap);
        if (vaiTro == null) {
            throw new IllegalArgumentException("Thiếu vaiTro.");
        }
        this.vaiTro = vaiTro;
        this.hoatDong = true;
        this.ngayTao = LocalDateTime.now();
    }

    /** Tạo thông tin tài khoản với vai trò và trạng thái truy cập. */
    public TaiKhoan(String maTaiKhoan, String maNhanVien, String tenDangNhap, VaiTro vaiTro, boolean hoatDong,
            LocalDateTime ngayTao) {
        if (maTaiKhoan == null) {
            throw new IllegalArgumentException("Thiếu maTaiKhoan.");
        }
        this.maTaiKhoan = maTaiKhoan.trim();
        if (maNhanVien == null) {
            throw new IllegalArgumentException("Thiếu maNhanVien.");
        }
        this.maNhanVien = maNhanVien.trim();

        if (this.maTaiKhoan.isEmpty() || this.maNhanVien.isEmpty()) {
            throw new IllegalArgumentException("Mã tài khoản và mã nhân viên không được rỗng.");
        }

        this.tenDangNhap = kiemTraTen(tenDangNhap);
        if (vaiTro == null) {
            throw new IllegalArgumentException("Thiếu vaiTro.");
        }
        this.vaiTro = vaiTro;
        this.hoatDong = hoatDong;
        if (ngayTao == null) {
            throw new IllegalArgumentException("Thiếu ngayTao.");
        }
        this.ngayTao = ngayTao;
    }

    /** Trả về mã tài khoản. */
    public String getMaTaiKhoan() {
        return maTaiKhoan;
    }

    /** Trả về mã nhân viên. */
    public String getMaNhanVien() {
        return maNhanVien;
    }

    /** Trả về tên đăng nhập. */
    public String getTenDangNhap() {
        return tenDangNhap;
    }

    /** Trả về vai trò được cấp. */
    public VaiTro getVaiTro() {
        return vaiTro;
    }

    /** Cho biết tài khoản đang được mở hay đã khóa. */
    public boolean isHoatDong() {
        return hoatDong;
    }

    /** Trả về thời điểm tạo tài khoản. */
    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    /** Cho biết phiên đăng nhập đã bị thu hồi hay chưa. */
    public boolean isPhienHopLe() {
        return phienHopLe;
    }

    /** Đánh dấu phiên cũ hết hiệu lực để chặn các thao tác tiếp theo. */
    public void ketThucPhien() {
        phienHopLe = false;
    }

    /** Từ chối thao tác nếu phiên đã hết hiệu lực hoặc tài khoản bị khóa. */
    public void kiemTraPhien() {
        if (!phienHopLe || !hoatDong) {
            throw new SecurityException("Phiên đăng nhập đã hết hiệu lực. Vui lòng đăng nhập lại.");
        }
    }

    /** Kiểm tra mật khẩu mới có đủ độ dài, chữ hoa, số và ký tự đặc biệt. */
    public static void kiemTraMatKhauMoi(char[] matKhau) {
        if (matKhau == null) {
            throw new IllegalArgumentException("Thiếu matKhau.");
        }

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

    /** Kiểm tra tên đăng nhập không rỗng và bỏ khoảng trắng hai đầu. */
    private static String kiemTraTen(String ten) {
        if (ten == null || ten.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập không được rỗng.");
        }
        return ten.trim();
    }

    /** Trả về mã và tên đăng nhập để hiển thị tài khoản. */
    @Override
    public String toString() {
        return maTaiKhoan + " · " + tenDangNhap;
    }
}
