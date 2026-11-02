package entity.hoadon;

/** Hình thức thanh toán được chấp nhận tại quầy thu ngân. */
public enum HinhThucThanhToan {

    /** Khách trả bằng tiền mặt tại quầy. */
    TIEN_MAT("Tiền mặt"),

    /** Khách chuyển khoản ngân hàng, thu ngân đối chiếu trước khi xác nhận. */
    CHUYEN_KHOAN("Chuyển khoản");

    private final String tenHienThi;

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    HinhThucThanhToan(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    /** Nhãn tiếng Việt hiển thị trên giao diện và bộ lọc tra cứu. */
    public String getTenHienThi() {
        return tenHienThi;
    }

    /** Tìm enum từ nhãn hiển thị; dùng khi ánh xạ lựa chọn JComboBox. */
    public static HinhThucThanhToan tuTenHienThi(String tenHienThi) {
        for (HinhThucThanhToan h : values()) {
            if (h.tenHienThi.equals(tenHienThi)) {
                return h;
            }
        }
        return null;
    }
}
