package entity;

/**
 * Trạng thái vòng đời của phiếu sửa chữa.
 * Thứ tự chuyển trạng thái: TIEP_NHAN → DANG_SUA → HOAN_THANH → DA_THANH_TOAN.
 */
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

    TrangThaiPhieu(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    /**
     * Nhãn tiếng Việt hiển thị trên giao diện.
     *
     * @return tên hiển thị của trạng thái
     */
    public String getTenHienThi() {
        return tenHienThi;
    }
}
