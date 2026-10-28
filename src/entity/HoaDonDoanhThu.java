package entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/** Thông tin chỉ đọc của một hóa đơn đã thanh toán, dùng đối chiếu doanh thu; không lập/sửa hóa đơn. */
public final class HoaDonDoanhThu {

    /** Hai phương thức thanh toán đang được hỗ trợ trong báo cáo. */
    public enum HinhThuc {
        TIEN_MAT("Tiền mặt"), CHUYEN_KHOAN("Chuyển khoản");

        /** Nhãn tiếng Việt của phương thức thu tiền. */
        private final String tenHienThi;

        /** @param tenHienThi nhãn tiếng Việt trên báo cáo */
        HinhThuc(String tenHienThi) {
            this.tenHienThi = tenHienThi;
        }

        /** @return nhãn phương thức thanh toán */
        public String getTenHienThi() {
            return tenHienThi;
        }
    }

    /** Định danh chứng từ và hồ sơ đối chiếu. */
    private final String maHoaDon;
    private final String maPhieuSuaChua;
    private final String khachHang;
    private final String bienSo;
    /** Thời điểm đã thu tiền và số tiền thực thu, đơn vị VND. */
    private final LocalDateTime ngayThanhToan;
    private final HinhThuc hinhThuc;
    private final BigDecimal soTien;

    /**
     * Tạo dữ liệu đối chiếu bất biến của hóa đơn đã thanh toán.
     * @param maHoaDon mã hóa đơn
     * @param maPhieuSuaChua phiếu sửa chữa nguồn
     * @param khachHang tên khách hàng
     * @param bienSo biển số xe
     * @param ngayThanhToan thời điểm thanh toán, không dùng ngày lập để tính doanh thu
     * @param hinhThuc phương thức thu tiền
     * @param soTien số tiền không âm
     * @throws IllegalArgumentException nếu thông tin rỗng hoặc tiền âm
     * @throws NullPointerException nếu thiếu dữ liệu
     */
    public HoaDonDoanhThu(String maHoaDon, String maPhieuSuaChua, String khachHang, String bienSo,
            LocalDateTime ngayThanhToan, HinhThuc hinhThuc, BigDecimal soTien) {
        this.maHoaDon = kiemTraChu(maHoaDon);
        this.maPhieuSuaChua = kiemTraChu(maPhieuSuaChua);
        this.khachHang = kiemTraChu(khachHang);
        this.bienSo = kiemTraChu(bienSo);
        this.ngayThanhToan = Objects.requireNonNull(ngayThanhToan);
        this.hinhThuc = Objects.requireNonNull(hinhThuc);
        this.soTien = Objects.requireNonNull(soTien);
        if (soTien.signum() < 0) {
            throw new IllegalArgumentException("Số tiền hóa đơn không được âm.");
        }
    }

    /** @return mã hóa đơn đã thanh toán */
    public String getMaHoaDon() {
        return maHoaDon;
    }

    /** @return phiếu sửa chữa nguồn */
    public String getMaPhieuSuaChua() {
        return maPhieuSuaChua;
    }

    /** @return tên khách hàng */
    public String getKhachHang() {
        return khachHang;
    }

    /** @return biển số xe */
    public String getBienSo() {
        return bienSo;
    }

    /** @return thời điểm đã thanh toán */
    public LocalDateTime getNgayThanhToan() {
        return ngayThanhToan;
    }

    /** @return phương thức thu tiền */
    public HinhThuc getHinhThuc() {
        return hinhThuc;
    }

    /** @return tiền thực thu, đơn vị VND */
    public BigDecimal getSoTien() {
        return soTien;
    }

    /**
     * @param chu thông tin bắt buộc của hóa đơn
     * @return nội dung đã bỏ khoảng trắng, không rỗng
     */
    private String kiemTraChu(String chu) {
        String ketQua = Objects.requireNonNull(chu).trim();
        if (ketQua.isEmpty()) {
            throw new IllegalArgumentException("Thông tin hóa đơn không được rỗng.");
        }
        return ketQua;
    }
}
