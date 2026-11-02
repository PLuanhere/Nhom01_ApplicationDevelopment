package dao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import entity.HinhThucThanhToan;
import entity.HoaDon;

/**
 * Truy cập dữ liệu hóa đơn và phiếu sửa chữa cho vai trò Thu ngân.
 *
 * <p>Hiện dùng danh sách in-memory để test độc lập với database.
 * Khi kết nối SQL Server, thay phần khởi tạo dữ liệu bằng truy vấn JDBC;
 * hợp đồng phương thức giữ nguyên.</p>
 *
 * <p>Không phụ thuộc Swing, không gọi {@code JOptionPane}.</p>
 */
public final class HoaDonDAO {

    /** Thông tin phiếu sửa chữa cần cho nghiệp vụ Thu ngân. */
    public static final class PhieuThuNgan {
        private final String maPhieu;
        private final String bienSo;
        private final String tenKhachHang;
        private final LocalDate ngayTiepNhan;
        private final LocalDate ngayHoanThanh;
        private final BigDecimal tongChiPhi;
        private boolean daThanhToan;

        public PhieuThuNgan(String maPhieu, String bienSo, String tenKhachHang,
                LocalDate ngayTiepNhan, LocalDate ngayHoanThanh,
                BigDecimal tongChiPhi, boolean daThanhToan) {
            this.maPhieu       = Objects.requireNonNull(maPhieu);
            this.bienSo        = Objects.requireNonNull(bienSo);
            this.tenKhachHang  = Objects.requireNonNull(tenKhachHang);
            this.ngayTiepNhan  = Objects.requireNonNull(ngayTiepNhan);
            this.ngayHoanThanh = Objects.requireNonNull(ngayHoanThanh);
            this.tongChiPhi    = Objects.requireNonNull(tongChiPhi);
            this.daThanhToan   = daThanhToan;
        }

        public String getMaPhieu()           { return maPhieu; }
        public String getBienSo()            { return bienSo; }
        public String getTenKhachHang()      { return tenKhachHang; }
        public LocalDate getNgayTiepNhan()   { return ngayTiepNhan; }
        public LocalDate getNgayHoanThanh()  { return ngayHoanThanh; }
        public BigDecimal getTongChiPhi()    { return tongChiPhi; }
        public boolean isDaThanhToan()       { return daThanhToan; }
        void setDaThanhToan(boolean val)     { this.daThanhToan = val; }
    }

    private final List<PhieuThuNgan> dsPhieu;
    private final List<HoaDon> dsHoaDon;

    /** Tạo DAO với dữ liệu mẫu. */
    public HoaDonDAO() {
        dsPhieu = new ArrayList<>();
        dsPhieu.add(new PhieuThuNgan("SC-2026-0108", "51H-246.80", "Nguyễn Văn An",
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 5),
                new BigDecimal("1270000"), false));
        dsPhieu.add(new PhieuThuNgan("SC-2026-0111", "51A-678.90", "Trần Thị Bình",
                LocalDate.of(2026, 10, 2), LocalDate.of(2026, 10, 5),
                new BigDecimal("600000"), false));
        dsPhieu.add(new PhieuThuNgan("SC-2026-0112", "51K-001.23", "Lê Văn Cường",
                LocalDate.of(2026, 10, 3), LocalDate.of(2026, 10, 5),
                new BigDecimal("200000"), false));
        dsPhieu.add(new PhieuThuNgan("SC-2026-0099", "51B-555.55", "Phạm Thị D",
                LocalDate.of(2026, 9, 28), LocalDate.of(2026, 9, 30),
                new BigDecimal("800000"), true));

        dsHoaDon = new ArrayList<>();
        dsHoaDon.add(new HoaDon("HD-2026-0108", "SC-2026-0108", "Nguyễn Văn An",
                new BigDecimal("1270000"), HinhThucThanhToan.TIEN_MAT,
                LocalDateTime.of(2026, 10, 5, 14, 30), "thungan"));
        dsHoaDon.add(new HoaDon("HD-2026-0105", "SC-2026-0099", "Phạm Thị D",
                new BigDecimal("800000"), HinhThucThanhToan.CHUYEN_KHOAN,
                LocalDateTime.of(2026, 9, 30, 16, 0), "thungan"));
    }

    // ── Phiếu chờ thu tiền ───────────────────────────────────────────────────

    /**
     * Lấy danh sách phiếu hoàn thành chưa có hóa đơn.
     *
     * @return danh sách phiếu chờ thanh toán; rỗng nếu không có
     */
    public List<PhieuThuNgan> layPhieuChoThanhToan() {
        List<PhieuThuNgan> ketQua = new ArrayList<>();
        for (PhieuThuNgan p : dsPhieu) {
            if (!p.isDaThanhToan() && !daCoHoaDon(p.getMaPhieu())) {
                ketQua.add(p);
            }
        }
        return ketQua;
    }

    /**
     * Tìm phiếu theo mã.
     *
     * @param maPhieu mã phiếu cần tìm, không được null
     * @return phiếu tương ứng hoặc Optional rỗng
     */
    public Optional<PhieuThuNgan> timPhieuTheoMa(String maPhieu) {
        Objects.requireNonNull(maPhieu, "Mã phiếu không được null.");
        for (PhieuThuNgan p : dsPhieu) {
            if (p.getMaPhieu().equals(maPhieu.trim())) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }

    // ── Hóa đơn ──────────────────────────────────────────────────────────────

    /**
     * Lấy tất cả hóa đơn đã phát hành.
     *
     * @return danh sách hóa đơn; rỗng nếu chưa có
     */
    public List<HoaDon> layTatCaHoaDon() {
        return new ArrayList<>(dsHoaDon);
    }

    /**
     * Tra cứu hóa đơn theo từ khóa và hình thức thanh toán.
     *
     * @param tuKhoa   từ khóa; null hoặc rỗng trả toàn bộ
     * @param hinhThuc lọc hình thức; null bỏ qua
     * @return danh sách khớp
     */
    public List<HoaDon> traKiemHoaDon(String tuKhoa, HinhThucThanhToan hinhThuc) {
        String khoa = (tuKhoa == null) ? "" : tuKhoa.trim().toLowerCase();
        List<HoaDon> ketQua = new ArrayList<>();
        for (HoaDon hd : dsHoaDon) {
            boolean khopTuKhoa = khoa.isEmpty()
                    || hd.getMaHoaDon().toLowerCase().contains(khoa)
                    || hd.getMaPhieuSuaChua().toLowerCase().contains(khoa)
                    || hd.getTenKhachHang().toLowerCase().contains(khoa);
            boolean khopHinhThuc = (hinhThuc == null) || hd.getHinhThucThanhToan() == hinhThuc;
            if (khopTuKhoa && khopHinhThuc) {
                ketQua.add(hd);
            }
        }
        return ketQua;
    }

    /**
     * Tìm hóa đơn theo mã.
     *
     * @param maHoaDon mã cần tìm, không được null
     * @return hóa đơn tương ứng hoặc Optional rỗng
     */
    public Optional<HoaDon> timHoaDonTheoMa(String maHoaDon) {
        Objects.requireNonNull(maHoaDon, "Mã hóa đơn không được null.");
        for (HoaDon hd : dsHoaDon) {
            if (hd.getMaHoaDon().equals(maHoaDon.trim())) {
                return Optional.of(hd);
            }
        }
        return Optional.empty();
    }

    /**
     * Lưu hóa đơn mới sau khi thu ngân xác nhận đã nhận đủ tiền.
     *
     * @param hoaDon hóa đơn cần lưu, không được null
     * @throws IllegalStateException nếu phiếu không tồn tại, đã thanh toán hoặc mã hóa đơn trùng
     */
    public void luuHoaDon(HoaDon hoaDon) {
        Objects.requireNonNull(hoaDon, "Hóa đơn không được null.");

        PhieuThuNgan phieu = timPhieuTheoMa(hoaDon.getMaPhieuSuaChua())
                .orElseThrow(() -> new IllegalStateException(
                        "Phiếu không tồn tại: " + hoaDon.getMaPhieuSuaChua()));

        if (phieu.isDaThanhToan()) {
            throw new IllegalStateException(
                    "Phiếu đã thanh toán, không thể lập hóa đơn: " + phieu.getMaPhieu());
        }
        if (daCoHoaDon(phieu.getMaPhieu())) {
            throw new IllegalStateException(
                    "Phiếu đã có hóa đơn, không được lập lại: " + phieu.getMaPhieu());
        }
        if (timHoaDonTheoMa(hoaDon.getMaHoaDon()).isPresent()) {
            throw new IllegalStateException(
                    "Mã hóa đơn đã tồn tại: " + hoaDon.getMaHoaDon());
        }

        // Khi kết nối DB: INSERT vào bảng HoaDon + UPDATE TrangThaiPhieu trong cùng transaction.
        dsHoaDon.add(hoaDon);

        // Cập nhật trạng thái phiếu trong bộ nhớ.
        phieu.setDaThanhToan(true);
    }

    // ── Thống kê nhanh cho dashboard ─────────────────────────────────────────

    /**
     * Đếm số phiếu hoàn thành chưa thanh toán.
     *
     * @return số phiếu chờ thu tiền
     */
    public int demPhieuChoThanhToan() {
        return layPhieuChoThanhToan().size();
    }

    /**
     * Tính tổng tiền đã thu trong khoảng ngày (tính theo ngày của thoiDiemThanhToan).
     *
     * @param tuNgay ngày bắt đầu (bao gồm), không được null
     * @param denNgay ngày kết thúc (bao gồm), không được null
     * @return tổng tiền đã thu; {@code BigDecimal.ZERO} nếu không có
     * @throws NullPointerException     nếu tuNgay hoặc denNgay là null
     * @throws IllegalArgumentException nếu denNgay trước tuNgay
     */
    public BigDecimal tinhTongDaThu(LocalDate tuNgay, LocalDate denNgay) {
        Objects.requireNonNull(tuNgay,  "Ngày bắt đầu không được null.");
        Objects.requireNonNull(denNgay, "Ngày kết thúc không được null.");
        if (denNgay.isBefore(tuNgay)) {
            throw new IllegalArgumentException("Ngày kết thúc không được trước ngày bắt đầu.");
        }
        BigDecimal tong = BigDecimal.ZERO;
        for (HoaDon hd : dsHoaDon) {
            LocalDate ngayThanhToan = hd.getThoiDiemThanhToan().toLocalDate();
            if (!ngayThanhToan.isBefore(tuNgay) && !ngayThanhToan.isAfter(denNgay)) {
                tong = tong.add(hd.getTongTien());
            }
        }
        return tong;
    }

    /**
     * Tính tổng tiền đã thu theo hình thức trong khoảng ngày.
     *
     * @param tuNgay    ngày bắt đầu (bao gồm)
     * @param denNgay   ngày kết thúc (bao gồm)
     * @param hinhThuc  hình thức cần tính
     * @return tổng tiền; {@code BigDecimal.ZERO} nếu không có
     * @throws NullPointerException nếu bất kỳ tham số nào là null
     */
    public BigDecimal tinhTongDaThuTheoHinhThuc(
            LocalDate tuNgay, LocalDate denNgay, HinhThucThanhToan hinhThuc) {
        Objects.requireNonNull(hinhThuc, "Hình thức thanh toán không được null.");
        BigDecimal tong = BigDecimal.ZERO;
        for (HoaDon hd : traKiemHoaDon(null, hinhThuc)) {
            LocalDate ngay = hd.getThoiDiemThanhToan().toLocalDate();
            if (!ngay.isBefore(tuNgay) && !ngay.isAfter(denNgay)) {
                tong = tong.add(hd.getTongTien());
            }
        }
        return tong;
    }

    // ── Tiện ích nội bộ ──────────────────────────────────────────────────────

    /**
     * Kiểm tra phiếu đã có hóa đơn chưa.
     *
     * @param maPhieu mã phiếu cần kiểm tra
     * @return true nếu đã có ít nhất một hóa đơn liên kết
     */
    private boolean daCoHoaDon(String maPhieu) {
        for (HoaDon hd : dsHoaDon) {
            if (hd.getMaPhieuSuaChua().equals(maPhieu)) {
                return true;
            }
        }
        return false;
    }
}
