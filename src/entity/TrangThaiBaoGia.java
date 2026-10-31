package entity;

/** Trạng thái của từng phiên bản báo giá, không phải trạng thái của lệnh sửa chữa. */
public enum TrangThaiBaoGia {
    NHAP("Nháp"), CHO_XAC_NHAN("Chờ khách xác nhận"), DA_DONG_Y("Đã đồng ý"),
    YEU_CAU_DIEU_CHINH("Yêu cầu điều chỉnh"), TU_CHOI("Từ chối");
    private final String tenHienThi;
    TrangThaiBaoGia(String tenHienThi) { this.tenHienThi = tenHienThi; }
    public String getTenHienThi() { return tenHienThi; }
}
