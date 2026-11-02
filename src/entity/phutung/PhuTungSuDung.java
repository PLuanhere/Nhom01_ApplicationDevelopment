package entity.phutung;

// Lưu phụ tùng thực dùng và hạng mục tương ứng bằng bản ghi bất biến.
public final class PhuTungSuDung {
    private final String maGhiNhan;
    private final String maLenhSuaChua;
    private final PhuTung phuTung;
    private final double soLuong;
    private final String nguoiThucHien;
    private final String ghiChu;
    private final TrangThaiSuDung trangThai;
    private final String hangMuc;

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public PhuTungSuDung(String maGhiNhan, String maLenhSuaChua, PhuTung phuTung, double soLuong, String nguoiThucHien,
            String ghiChu, TrangThaiSuDung trangThai) {
        this(maGhiNhan, maLenhSuaChua, phuTung, soLuong, nguoiThucHien, ghiChu, trangThai, "");
    }

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public PhuTungSuDung(String maGhiNhan, String maLenhSuaChua, PhuTung phuTung, double soLuong, String nguoiThucHien,
            String ghiChu, TrangThaiSuDung trangThai, String hangMuc) {
        if (maGhiNhan == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.maGhiNhan = maGhiNhan.trim();
        if (maLenhSuaChua == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.maLenhSuaChua = maLenhSuaChua.trim();
        if (phuTung == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.phuTung = phuTung;
        phuTung.kiemTraSoLuong(soLuong);
        this.soLuong = soLuong;
        if (nguoiThucHien == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.nguoiThucHien = nguoiThucHien;
        if (ghiChu == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.ghiChu = ghiChu;
        if (trangThai == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.trangThai = trangThai;
        if (hangMuc == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.hangMuc = hangMuc;
        if (this.maGhiNhan.isEmpty() || this.maLenhSuaChua.isEmpty()) {
            throw new IllegalArgumentException("Thiếu mã ghi nhận hoặc mã lệnh.");
        }
    }

    /** Trả về ma ghi nhan. */
    public String getMaGhiNhan() {
        return maGhiNhan;
    }

    /** Trả về ma lenh sua chua. */
    public String getMaLenhSuaChua() {
        return maLenhSuaChua;
    }

    /** Trả về phu tung. */
    public PhuTung getPhuTung() {
        return phuTung;
    }

    /** Trả về so luong. */
    public double getSoLuong() {
        return soLuong;
    }

    /** Trả về nguoi thuc hien. */
    public String getNguoiThucHien() {
        return nguoiThucHien;
    }

    /** Trả về ghi chu. */
    public String getGhiChu() {
        return ghiChu;
    }

    /** Trả về trang thai. */
    public TrangThaiSuDung getTrangThai() {
        return trangThai;
    }

    /** Trả về hang muc. */
    public String getHangMuc() {
        return hangMuc;
    }
}