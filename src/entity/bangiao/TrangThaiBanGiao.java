package entity.bangiao;

public enum TrangThaiBanGiao {
    CHO_CAN_CU("Chờ xác nhận thanh toán"), CHO_GIAO("Chờ bàn giao"), DA_GIAO("Đã bàn giao");

    private final String tenHienThi;

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    TrangThaiBanGiao(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    /** Trả về ten hien thi. */
    public String getTenHienThi() {
        return tenHienThi;
    }
}
