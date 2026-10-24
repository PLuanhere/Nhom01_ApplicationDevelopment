package entity;

/**
 * Hình thức thanh toán được chấp nhận tại quầy thu ngân.
 * Nhãn hiển thị khớp với lựa chọn trong {@code DocumentSearchPanel}.
 */
public enum HinhThucThanhToan {

    /** Khách trả bằng tiền mặt tại quầy. */
    TIEN_MAT("Tiền mặt"),

    /** Khách chuyển khoản ngân hàng, thu ngân đối chiếu trước khi xác nhận. */
    CHUYEN_KHOAN("Chuyển khoản");

    private final String tenHienThi;

    HinhThucThanhToan(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    /**
     * Nhãn tiếng Việt hiển thị trên giao diện và bộ lọc tra cứu.
     *
     * @return tên hiển thị
     */
    public String getTenHienThi() {
        return tenHienThi;
    }

    /**
     * Tìm enum từ nhãn hiển thị; dùng khi ánh xạ lựa chọn JComboBox.
     *
     * @param tenHienThi nhãn cần tra cứu
     * @return enum tương ứng hoặc null nếu không khớp
     */
    public static HinhThucThanhToan tuTenHienThi(String tenHienThi) {
        for (HinhThucThanhToan h : values()) {
            if (h.tenHienThi.equals(tenHienThi)) {
                return h;
            }
        }
        return null;
    }
}
