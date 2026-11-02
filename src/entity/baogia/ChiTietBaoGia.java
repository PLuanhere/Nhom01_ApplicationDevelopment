package entity.baogia;

/** Hạng mục báo giá giữ tên, đơn vị và giá tại thời điểm lập. */
public final class ChiTietBaoGia {

    private final String maDichVu;
    private final String tenHangMuc;
    private final String loai;
    private final String donVi;
    private final double soLuong;
    private final long donGia;
    private final String maPhuTung;

    /** Hạng mục chưa có mã liên kết dịch vụ hoặc phụ tùng. */
    public ChiTietBaoGia(String tenHangMuc, String loai, String donVi, double soLuong, long donGia) {
        this("", tenHangMuc, loai, donVi, soLuong, donGia, "");
    }

    /** Giữ constructor của main: mã phụ tùng nằm cuối. */
    public ChiTietBaoGia(String tenHangMuc, String loai, String donVi, double soLuong, long donGia, String maPhuTung) {
        this("", tenHangMuc, loai, donVi, soLuong, donGia, maPhuTung);
    }

    /** Tạo hạng mục có mã liên kết. */
    public ChiTietBaoGia(String maDichVu, String tenHangMuc, String loai, String donVi, double soLuong, long donGia,
            String maPhuTung) {
        if (maDichVu == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.maDichVu = maDichVu.trim();
        if (maPhuTung == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.maPhuTung = maPhuTung.trim();
        if (tenHangMuc == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.tenHangMuc = tenHangMuc;
        if (loai == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.loai = loai;
        if (donVi == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.donVi = donVi;
        this.soLuong = soLuong;
        this.donGia = donGia;

        if (!(this.soLuong > 0 && this.soLuong < Long.MAX_VALUE / 1000.0)
                || this.soLuong != Math.round(this.soLuong * 1000) / 1000.0 || this.donGia < 0
                || ((donVi.equals("cái") || donVi.equals("bộ")) && this.soLuong % 1 != 0)) {
            throw new IllegalArgumentException("Số lượng phải dương và đơn giá không được âm.");
        }
        getThanhTien();
    }

    /** Trả về ma dich vu. */
    public String getMaDichVu() {
        return maDichVu;
    }

    /** Trả về ma phu tung. */
    public String getMaPhuTung() {
        return maPhuTung;
    }

    /** Trả về ten hang muc. */
    public String getTenHangMuc() {
        return tenHangMuc;
    }

    /** Trả về loai. */
    public String getLoai() {
        return loai;
    }

    /** Trả về don vi. */
    public String getDonVi() {
        return donVi;
    }

    /** Trả về so luong. */
    public double getSoLuong() {
        return soLuong;
    }

    /** Trả về don gia. */
    public long getDonGia() {
        return donGia;
    }

    /** Trả về thanh tien. */
    public long getThanhTien() {
        return tinhThanhTien(soLuong, donGia);
    }

    /** Tính tiền bằng số nguyên, làm tròn phần lẻ đến đồng và kiểm tra giới hạn. */
    public static long tinhThanhTien(double soLuong, long donGia) {
        if (!(soLuong > 0 && soLuong < Long.MAX_VALUE / 1000.0) || donGia < 0
                || soLuong != Math.round(soLuong * 1000) / 1000.0) {
            throw new IllegalArgumentException("Số lượng hoặc đơn giá không hợp lệ.");
        }
        long luongNghin = Math.round(soLuong * 1000);
        long luongNguyen = luongNghin / 1000;
        long luongLe = luongNghin % 1000;
        if (luongNguyen > 0 && donGia > Long.MAX_VALUE / luongNguyen) {
            throw new IllegalArgumentException("Thành tiền vượt giới hạn cho phép.");
        }
        long tienNguyen = luongNguyen * donGia;
        long tienLe = (donGia / 1000) * luongLe + ((donGia % 1000) * luongLe + 500) / 1000;
        if (Long.MAX_VALUE - tienNguyen < tienLe) {
            throw new IllegalArgumentException("Thành tiền vượt giới hạn cho phép.");
        }
        return tienNguyen + tienLe;
    }
}
