package ui.screen;

/**
 * Các trang trong giao diện Thu ngân; dùng để điều hướng qua {@link ThuNganShell}.
 * Đặt trong {@code ui.screen} theo quy định mục 3.3.
 */
public enum ThuNganShellPage {

    /** Trang tổng quan sau khi đăng nhập. */
    TRANG_CHU("Trang chủ"),

    /** Màn hình lập và xác nhận hóa đơn. */
    THANH_TOAN("Thanh toán"),

    /** Màn hình tra cứu và in lại chứng từ. */
    TRA_CUU_CHUNG_TU("Tra cứu chứng từ");

    private final String tenHienThi;

    ThuNganShellPage(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    /**
     * Nhãn tiếng Việt hiển thị trên sidebar và tiêu đề màn hình.
     *
     * @return tên hiển thị của trang
     */
    public String getTenHienThi() {
        return tenHienThi;
    }
}
