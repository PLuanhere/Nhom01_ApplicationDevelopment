package entity;

public enum TrangThaiBanGiao {
    CHO_CAN_CU("Chờ xác nhận thanh toán"), CHO_GIAO("Chờ bàn giao"), DA_GIAO("Đã bàn giao");
    private final String tenHienThi;
    TrangThaiBanGiao(String tenHienThi) { this.tenHienThi = tenHienThi; }
    public String getTenHienThi() { return tenHienThi; }
}
