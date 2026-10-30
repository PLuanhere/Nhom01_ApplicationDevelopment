package entity;

/** Bộ phận ghi trong hồ sơ; không cấp quyền cho tài khoản đăng nhập. */
public enum BoPhanNhanVien {
    QUAN_LY("Quản lý"),
    TIEP_NHAN("Tiếp nhận"),
    KY_THUAT("Kỹ thuật"),
    THU_NGAN("Thu ngân");

    private final String tenHienThi;

    /** @param tenHienThi tên bộ phận trên form và danh sách */
    BoPhanNhanVien(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    /** @return tên bộ phận trên giao diện */
    @Override
    public String toString() {
        return tenHienThi;
    }
}
