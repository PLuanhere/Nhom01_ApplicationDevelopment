package entity;

/** Trạng thái tiếp nhận hiện có; các bước kiểm tra/báo giá bổ sung khi nối workflow. */
public enum TrangThaiTiepNhan {
    NHAP("Nháp"), CHO_KIEM_TRA("Chờ kiểm tra");
    private final String tenHienThi;
    TrangThaiTiepNhan(String tenHienThi) { this.tenHienThi = tenHienThi; }
    public String getTenHienThi() { return tenHienThi; }
}
