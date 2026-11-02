package entity;

/** Các bộ phận làm việc dùng trong hồ sơ nhân viên. */
public enum BoPhanNhanVien {
    QUAN_LY("Quản lý"),
    TIEP_NHAN("Tiếp nhận"),
    KY_THUAT("Kỹ thuật"),
    THU_NGAN("Thu ngân");

    private final String tenHienThi;

    /** Gắn tên hiển thị cho một bộ phận. */
    BoPhanNhanVien(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    /** Trả về tên hiển thị để dùng trong biểu mẫu và bảng. */
    @Override
    public String toString() {
        return tenHienThi;
    }
}
