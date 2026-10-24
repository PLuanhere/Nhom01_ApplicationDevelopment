package entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Phiếu sửa chữa xe — đơn vị công việc từ tiếp nhận đến hoàn thành.
 * Thu ngân chỉ thao tác với phiếu ở trạng thái {@link TrangThaiPhieu#HOAN_THANH}.
 *
 * <p>Kiểm tra nội tại: mã, biển số, tên khách không được rỗng;
 * tổng chi phí không âm; ngày hoàn thành không trước ngày tiếp nhận.</p>
 */
public final class PhieuSuaChua {

    private final String maPhieu;
    private final String bienSo;
    private final String tenKhachHang;
    private final LocalDate ngayTiepNhan;
    private final LocalDate ngayHoanThanh;
    /** Tổng chi phí dịch vụ và phụ tùng đã xác nhận; không âm. */
    private final BigDecimal tongChiPhi;
    private final TrangThaiPhieu trangThai;

    /**
     * Tạo phiếu sửa chữa.
     *
     * @param maPhieu        mã phiếu duy nhất, không rỗng
     * @param bienSo         biển số xe, không rỗng
     * @param tenKhachHang   tên khách hàng, không rỗng
     * @param ngayTiepNhan   ngày xe vào garage
     * @param ngayHoanThanh  ngày hoàn thành; null nếu chưa xong
     * @param tongChiPhi     tổng chi phí, không âm
     * @param trangThai      trạng thái hiện tại
     * @throws NullPointerException     nếu mã, biển số, tên, ngày tiếp nhận, chi phí hoặc trạng thái là null
     * @throws IllegalArgumentException nếu mã/biển số/tên rỗng, chi phí âm,
     *                                  hoặc ngày hoàn thành trước ngày tiếp nhận
     */
    public PhieuSuaChua(String maPhieu, String bienSo, String tenKhachHang,
            LocalDate ngayTiepNhan, LocalDate ngayHoanThanh,
            BigDecimal tongChiPhi, TrangThaiPhieu trangThai) {
        this.maPhieu       = requireNonBlank(maPhieu,      "Mã phiếu");
        this.bienSo        = requireNonBlank(bienSo,       "Biển số");
        this.tenKhachHang  = requireNonBlank(tenKhachHang, "Tên khách hàng");
        this.ngayTiepNhan  = Objects.requireNonNull(ngayTiepNhan,  "Ngày tiếp nhận không được null.");
        this.ngayHoanThanh = ngayHoanThanh; // null hợp lệ khi chưa xong
        this.tongChiPhi    = Objects.requireNonNull(tongChiPhi, "Tổng chi phí không được null.");
        this.trangThai     = Objects.requireNonNull(trangThai,  "Trạng thái không được null.");

        if (tongChiPhi.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tổng chi phí không được âm.");
        }
        if (ngayHoanThanh != null && ngayHoanThanh.isBefore(ngayTiepNhan)) {
            throw new IllegalArgumentException("Ngày hoàn thành không được trước ngày tiếp nhận.");
        }
    }

    public String getMaPhieu()       { return maPhieu; }
    public String getBienSo()        { return bienSo; }
    public String getTenKhachHang()  { return tenKhachHang; }
    public LocalDate getNgayTiepNhan()  { return ngayTiepNhan; }
    public LocalDate getNgayHoanThanh() { return ngayHoanThanh; }
    public BigDecimal getTongChiPhi()   { return tongChiPhi; }
    public TrangThaiPhieu getTrangThai() { return trangThai; }

    private static String requireNonBlank(String value, String tenTruong) {
        String trimmed = Objects.requireNonNull(value, tenTruong + " không được null.").trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(tenTruong + " không được rỗng.");
        }
        return trimmed;
    }
}
