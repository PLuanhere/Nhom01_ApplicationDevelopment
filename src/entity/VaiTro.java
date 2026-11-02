package entity;

/** Các vai trò truy cập ứng dụng và tên hiển thị tương ứng. */
public enum VaiTro {

    QUAN_LY("Quản lý"),
    NHAN_VIEN_TIEP_NHAN("Nhân viên tiếp nhận"),
    NHAN_VIEN_KY_THUAT("Nhân viên kỹ thuật"),
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
    public String toString() { return tenHienThi; }
}
