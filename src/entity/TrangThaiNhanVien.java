package entity;

/** Các trạng thái làm việc và nhãn hiển thị của nhân viên. */
public enum TrangThaiNhanVien {
    DANG_LAM("Đang làm việc", "Đang làm"),
    DA_NGHI("Đã nghỉ việc", "Đã nghỉ");

    private final String tenHienThi;
    private final String tenNgan;

    /** Gắn tên đầy đủ và nhãn ngắn cho trạng thái làm việc. */
    TrangThaiNhanVien(String tenHienThi, String tenNgan) {
        this.tenHienThi = tenHienThi;
        this.tenNgan = tenNgan;
    }

    /** Trả về nhãn trạng thái ngắn dùng trong bảng. */
    public String getTenNgan() {
        return tenNgan;
    }

    /** Trả về tên hiển thị để dùng trong biểu mẫu và bảng. */
    @Override
    public String toString() {
        return tenHienThi;
    }
}
