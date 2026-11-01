package entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Hóa đơn thanh toán được lập sau khi phiếu sửa chữa hoàn thành.
 * Một phiếu chỉ có tối đa một hóa đơn; ràng buộc này được giữ tại DAO.
 *
 * <p>Kiểm tra nội tại: mã hóa đơn, mã phiếu, tên khách không rỗng;
 * tổng tiền không âm.</p>
 */
public final class HoaDon {

    private final String maHoaDon;
    private final String maPhieuSuaChua;
    private final String tenKhachHang;
    private final BigDecimal tongTien;
    private final HinhThucThanhToan hinhThucThanhToan;
    private final LocalDateTime thoiDiemThanhToan;
    private final String nguoiThucHien;

    /**
     * Tạo hóa đơn đã thanh toán.
     *
     * @param maHoaDon           mã hóa đơn duy nhất, không rỗng
     * @param maPhieuSuaChua     mã phiếu liên kết, không rỗng
     * @param tenKhachHang       tên khách tại thời điểm lập, không rỗng
     * @param tongTien           tổng tiền thực thu, không âm
     * @param hinhThucThanhToan  hình thức thanh toán
     * @param thoiDiemThanhToan  thời điểm xác nhận thu tiền
     * @param nguoiThucHien      tên đăng nhập của thu ngân
     * @throws NullPointerException     nếu bất kỳ tham số nào là null
     * @throws IllegalArgumentException nếu mã/tên rỗng hoặc tổng tiền âm
     */
    public HoaDon(String maHoaDon, String maPhieuSuaChua, String tenKhachHang,
            BigDecimal tongTien, HinhThucThanhToan hinhThucThanhToan,
            LocalDateTime thoiDiemThanhToan, String nguoiThucHien) {
        this.maHoaDon          = requireNonBlank(maHoaDon,       "Mã hóa đơn");
        this.maPhieuSuaChua    = requireNonBlank(maPhieuSuaChua, "Mã phiếu sửa chữa");
        this.tenKhachHang      = requireNonBlank(tenKhachHang,   "Tên khách hàng");
        this.nguoiThucHien     = requireNonBlank(nguoiThucHien,  "Người thực hiện");
        this.tongTien          = Objects.requireNonNull(tongTien,           "Tổng tiền không được null.");
        this.hinhThucThanhToan = Objects.requireNonNull(hinhThucThanhToan,  "Hình thức thanh toán không được null.");
        this.thoiDiemThanhToan = Objects.requireNonNull(thoiDiemThanhToan,  "Thời điểm thanh toán không được null.");

        if (tongTien.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tổng tiền không được âm.");
        }
    }

    public String getMaHoaDon()            { return maHoaDon; }
    public String getMaPhieuSuaChua()      { return maPhieuSuaChua; }
    public String getTenKhachHang()        { return tenKhachHang; }
    public BigDecimal getTongTien()        { return tongTien; }
    public HinhThucThanhToan getHinhThucThanhToan() { return hinhThucThanhToan; }
    public LocalDateTime getThoiDiemThanhToan()     { return thoiDiemThanhToan; }
    public String getNguoiThucHien()       { return nguoiThucHien; }

    private static String requireNonBlank(String value, String tenTruong) {
        String trimmed = Objects.requireNonNull(value, tenTruong + " không được null.").trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(tenTruong + " không được rỗng.");
        }
        return trimmed;
    }
}
