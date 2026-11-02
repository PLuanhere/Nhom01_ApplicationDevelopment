package entity.hoadon;

import java.time.LocalDateTime;

/** Hóa đơn thanh toán được lập sau khi phiếu sửa chữa hoàn thành. */
public final class HoaDon {

    private final String maHoaDon;
    private final String maPhieuSuaChua;
    private final String tenKhachHang;
    private final long tongTien;
    private final HinhThucThanhToan hinhThucThanhToan;
    private final LocalDateTime thoiDiemThanhToan;
    private final String nguoiThucHien;

    /** Tạo hóa đơn đã thanh toán. */
    public HoaDon(String maHoaDon, String maPhieuSuaChua, String tenKhachHang, long tongTien,
            HinhThucThanhToan hinhThucThanhToan, LocalDateTime thoiDiemThanhToan, String nguoiThucHien) {
        this.maHoaDon = requireNonBlank(maHoaDon, "Mã hóa đơn");
        this.maPhieuSuaChua = requireNonBlank(maPhieuSuaChua, "Mã phiếu sửa chữa");
        this.tenKhachHang = requireNonBlank(tenKhachHang, "Tên khách hàng");
        this.nguoiThucHien = requireNonBlank(nguoiThucHien, "Người thực hiện");
        this.tongTien = tongTien;
        if (hinhThucThanhToan == null) {
            throw new NullPointerException("Hình thức thanh toán không được null.");
        }
        this.hinhThucThanhToan = hinhThucThanhToan;
        if (thoiDiemThanhToan == null) {
            throw new NullPointerException("Thời điểm thanh toán không được null.");
        }
        this.thoiDiemThanhToan = thoiDiemThanhToan;

        if ((tongTien < 0L)) {
            throw new IllegalArgumentException("Tổng tiền không được âm.");
        }
    }

    /** Trả về ma hoa don. */
    public String getMaHoaDon() {
        return maHoaDon;
    }

    /** Trả về ma phieu sua chua. */
    public String getMaPhieuSuaChua() {
        return maPhieuSuaChua;
    }

    /** Trả về ten khach hang. */
    public String getTenKhachHang() {
        return tenKhachHang;
    }

    /** Trả về tong tien. */
    public long getTongTien() {
        return tongTien;
    }

    /** Trả về hinh thuc thanh toan. */
    public HinhThucThanhToan getHinhThucThanhToan() {
        return hinhThucThanhToan;
    }

    /** Trả về thoi diem thanh toan. */
    public LocalDateTime getThoiDiemThanhToan() {
        return thoiDiemThanhToan;
    }

    /** Trả về nguoi thuc hien. */
    public String getNguoiThucHien() {
        return nguoiThucHien;
    }

    /** Thực hiện require non blank cho thành phần hiện tại. */
    private static String requireNonBlank(String value, String tenTruong) {
        if (value == null) {
            throw new NullPointerException(tenTruong + " không được null.");
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(tenTruong + " không được rỗng.");
        }
        return trimmed;
    }
}
