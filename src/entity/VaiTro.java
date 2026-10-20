package entity;

/* Bốn vai trò nhân viên */
public enum VaiTro {

    QUAN_LY("Quản lý"),
    NHAN_VIEN_TIEP_NHAN("Nhân viên tiếp nhận"),
    NHAN_VIEN_KY_THUAT("Nhân viên kỹ thuật"),
    THU_NGAN("Thu ngân");

    private final String tenHienThi;

    VaiTro(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }
}
