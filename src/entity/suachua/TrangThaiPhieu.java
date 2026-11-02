package entity.suachua;

/** Trạng thái vòng đời của phiếu sửa chữa. */
public enum TrangThaiPhieu {

    /** Xe vừa được tiếp nhận, chưa bắt đầu sửa. */
    TIEP_NHAN("Tiếp nhận"),

    /** Kỹ thuật viên đang thực hiện sửa chữa. */
    DANG_SUA("Đang sửa"),

    /** Sửa chữa hoàn tất, chờ thu ngân lập hóa đơn. */
    HOAN_THANH("Hoàn thành"),

    /** Khách đã thanh toán, hóa đơn đã được lưu. */
    DA_THANH_TOAN("Đã thanh toán");

    private final String tenHienThi;

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    TrangThaiPhieu(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    /** Nhãn tiếng Việt hiển thị trên giao diện. */
    public String getTenHienThi() {
        return tenHienThi;
    }
}
