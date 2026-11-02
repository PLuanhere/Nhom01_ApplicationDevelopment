package entity;

/** Lưu danh mục phụ tùng, đơn giá, đơn vị tính và số lượng tồn. */
public final class PhuTung {
    private final String maPhuTung;
    private final String tenPhuTung;
    private final String loaiPhuTung;
    private final String donViTinh;
    private final long giaNhap;
    private final long giaBan;
    private final double mucToiThieu;
    private final boolean hoatDong;
    private double soLuongTon;

    /** Tạo thông tin phụ tùng và kiểm tra đơn vị, giá, mức tồn tối thiểu. */
    public PhuTung(String ma, String ten, String donVi, double toiThieu) {
        this(ma, ten, "", donVi, 0, 0, toiThieu, true);
    }

    /** Tạo thông tin phụ tùng và kiểm tra đơn vị, giá, mức tồn tối thiểu. */
    public PhuTung(String ma, String ten, String loai, String donVi,
        long giaNhap, long giaBan, double toiThieu, boolean hoatDong) {
        if (ma == null || ma.trim().isEmpty() || ten == null || ten.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã và tên phụ tùng không được rỗng.");
        }
        if (!"cái".equals(donVi) && !"lít".equals(donVi) && !"bộ".equals(donVi)) {
            throw new IllegalArgumentException("Đơn vị tính phải là cái, lít hoặc bộ.");
        }
        if (giaNhap < 0
            || giaBan < 0
            || !(toiThieu >= 0 && toiThieu <= Double.MAX_VALUE)) {
            throw new IllegalArgumentException("Giá và tồn tối thiểu phải là số không âm.");
        }
        this.maPhuTung = ma.trim();
        this.tenPhuTung = ten.trim();
        this.loaiPhuTung = loai == null ? "" : loai.trim();
        this.donViTinh = donVi;
        this.giaNhap = giaNhap;
        this.giaBan = giaBan;
        this.mucToiThieu = toiThieu;
        this.hoatDong = hoatDong;
        if (toiThieu > 0) { kiemTraSoLuong(toiThieu); }
    }

    /** Sao chép thông tin và tồn kho để bên gọi không sửa trực tiếp bản ghi nguồn. */
    public PhuTung(PhuTung cu) {
        this(cu.maPhuTung, cu.tenPhuTung, cu.loaiPhuTung, cu.donViTinh,
            cu.giaNhap, cu.giaBan, cu.mucToiThieu, cu.hoatDong);
        soLuongTon = cu.soLuongTon;
    }

    /** Trả về mã phụ tùng. */
    public String getMaPhuTung() { return maPhuTung; }
    /** Trả về tên phụ tùng. */
    public String getTenPhuTung() { return tenPhuTung; }
    /** Trả về loại phụ tùng. */
    public String getLoaiPhuTung() { return loaiPhuTung; }
    /** Trả về đơn vị tính. */
    public String getDonViTinh() { return donViTinh; }
    /** Trả về giá nhập danh mục. */
    public long getGiaNhap() { return giaNhap; }
    /** Trả về giá bán danh mục. */
    public long getGiaBan() { return giaBan; }
    /** Trả về mức tồn tối thiểu. */
    public double getMucToiThieu() { return mucToiThieu; }
    /** Cho biết phụ tùng có còn được áp dụng trong danh mục hay không. */
    public boolean isHoatDong() { return hoatDong; }
    /** Trả về số lượng tồn kho. */
    public double getSoLuongTon() { return soLuongTon; }

    /** Kiểm tra và gán số tồn hợp lệ theo đơn vị của phụ tùng. */
    public void setSoLuongTon(double soLuong) {
        if (!(soLuong >= 0 && soLuong < Long.MAX_VALUE / 1000.0)) {
            throw new IllegalArgumentException("Tồn kho phải là số không âm.");
        }
        if (soLuong > 0) { kiemTraSoLuong(soLuong); }
        soLuongTon = soLuong;
    }

    /** Kiểm tra lượng dương; lít tối đa 3 số lẻ, cái hoặc bộ phải nguyên. */
    public void kiemTraSoLuong(double soLuong) {
        if (!(soLuong > 0 && soLuong < Long.MAX_VALUE / 1000.0)) {
            throw new IllegalArgumentException(tenPhuTung + ": số lượng phải dương và trong giới hạn cho phép.");
        }
        if (donViTinh.equals("lít")) {
            if (soLuong != Math.round(soLuong * 1000) / 1000.0) {
                throw new IllegalArgumentException("Số lít có tối đa 3 chữ số lẻ.");
            }
        } else if (soLuong % 1 != 0) {
            throw new IllegalArgumentException("Số lượng cái/bộ phải là số nguyên.");
        }
    }

    /** Trả về mã và tên phụ tùng để hiển thị trong danh sách chọn. */
    @Override public String toString() { return maPhuTung + " · " + tenPhuTung; }
}
