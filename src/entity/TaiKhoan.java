package entity;

import java.time.LocalDateTime;

/** Lưu thông tin đăng nhập của một nhân viên. */
public final class TaiKhoan {

    private final String maNhanVien;
    private final VaiTro vaiTro;
    private final boolean hoatDong;
    private final LocalDateTime ngayTao;
    private final int phienBan;

    private boolean phienHopLe = true;

    /** Tạo thông tin tài khoản với vai trò và trạng thái truy cập. */
    public TaiKhoan(String maNhanVien, VaiTro vaiTro) {
        this(maNhanVien, vaiTro, true, LocalDateTime.now(), 1);
    }

    /** Tạo thông tin tài khoản với vai trò và trạng thái truy cập. */
    public TaiKhoan(String maNhanVien, VaiTro vaiTro, boolean hoatDong, LocalDateTime ngayTao) {
        this(maNhanVien, vaiTro, hoatDong, ngayTao, 1);
    }

    /** Tạo tài khoản đọc từ database và giữ phiên bản để kiểm tra phiên. */
    public TaiKhoan(String maNhanVien, VaiTro vaiTro, boolean hoatDong, LocalDateTime ngayTao, int phienBan) {
        this.maNhanVien = kiemTraMaNhanVien(maNhanVien);
        if (vaiTro == null) {
            throw new IllegalArgumentException("Thiếu vaiTro.");
        }
        this.vaiTro = vaiTro;
        this.hoatDong = hoatDong;
        if (ngayTao == null) {
            throw new IllegalArgumentException("Thiếu ngayTao.");
        }
        this.ngayTao = ngayTao;
        this.phienBan = phienBan;
    }

    /** Trả về mã nhân viên dùng làm mã tài khoản. */
    public String getMaTaiKhoan() {
        return maNhanVien;
    }

    /** Trả về mã nhân viên. */
    public String getMaNhanVien() {
        return maNhanVien;
    }

    /** Trả về mã nhân viên dùng để đăng nhập. */
    public String getTenDangNhap() {
        return maNhanVien;
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

    /** Trả về phiên bản dữ liệu tài khoản tại lúc đăng nhập. */
    public int getPhienBan() {
        return phienBan;
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
            if (Character.isUpperCase(kyTu)) {
                chuHoa = true;
            }
            if (Character.isDigit(kyTu)) {
                chuSo = true;
            }
            if (!Character.isLetterOrDigit(kyTu) && !Character.isWhitespace(kyTu)) {
                dacBiet = true;
            }
        }

        if (matKhau.length < 8 || !chuHoa || !chuSo || !dacBiet) {
            throw new IllegalArgumentException("Ít nhất 8 ký tự, có chữ hoa, chữ số và ký tự đặc biệt.");
        }
    }

    /** Kiểm tra mã nhân viên dùng làm tài khoản. */
    private static String kiemTraMaNhanVien(String maNhanVien) {
        if (maNhanVien == null || maNhanVien.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhân viên không được rỗng.");
        }
        return maNhanVien.trim();
    }

    /** Trả về mã nhân viên để hiển thị tài khoản. */
    @Override
    public String toString() {
        return maNhanVien;
    }

    public enum VaiTro {
    
        QUAN_LY("Quản lý"), NHAN_VIEN_TIEP_NHAN("Nhân viên tiếp nhận"), NHAN_VIEN_KY_THUAT("Nhân viên kỹ thuật"),
        THU_NGAN("Thu ngân");
    
        private final String tenHienThi;
    
        /** Gắn tên hiển thị cho một vai trò. */
        VaiTro(String tenHienThi) {
            this.tenHienThi = tenHienThi;
        }
    
        /** Trả về tên hiển thị của vai trò. */
        public String getTenHienThi() {
            return tenHienThi;
        }
    
        /** Trả về tên hiển thị để dùng trong biểu mẫu và bảng. */
        @Override
        public String toString() {
            return tenHienThi;
        }
    }
}
