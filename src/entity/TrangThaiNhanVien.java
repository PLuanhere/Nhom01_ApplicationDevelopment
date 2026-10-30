package entity;

/** Tình trạng làm việc của hồ sơ, độc lập với trạng thái tài khoản. */
public enum TrangThaiNhanVien {
    DANG_LAM("Đang làm việc", "Đang làm"),
    DA_NGHI("Đã nghỉ việc", "Đã nghỉ");

    private final String tenHienThi;
    private final String tenNgan;

    /**
     * @param tenHienThi tên trên form
     * @param tenNgan nhãn gọn trong bảng
     */
    TrangThaiNhanVien(String tenHienThi, String tenNgan) {
        this.tenHienThi = tenHienThi;
        this.tenNgan = tenNgan;
    }

    /** @return nhãn trạng thái trong bảng */
    public String getTenNgan() {
        return tenNgan;
    }

    /** @return tên trạng thái trên form và bộ lọc */
    @Override
    public String toString() {
        return tenHienThi;
    }
}
