package dao;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import entity.HinhThucThanhToan;
import entity.HoaDon;

/**
 * Dữ liệu dành riêng cho vai trò Thu ngân.
 *
 * <p>Quản lý danh sách phiếu chờ thu và hóa đơn đã phát hành bằng in-memory.
 * Khi kết nối SQL Server, thay phần khởi tạo dữ liệu và các method đọc/ghi
 * bằng truy vấn JDBC; hợp đồng phương thức giữ nguyên.</p>
 *
 * <p>Không phụ thuộc Swing, không gọi JOptionPane.</p>
 */
public final class ThuNganDAO {

    /** Danh sách phiếu chờ thu tiền (phiếu sửa chữa đã HOAN_THANH, chưa lập hóa đơn). */
    private final List<PhieuChoThu>  danhSachPhieuCho = new ArrayList<>();
    /** Danh sách hóa đơn đã phát hành trong phiên — dùng entity HoaDon thực. */
    private final List<HoaDon> danhSachHoaDon  = new ArrayList<>();

    /** Tạo DAO với dữ liệu mẫu khớp màn hình UI. */
    public ThuNganDAO() {
        khoiTaoDuLieu();
    }

    private void khoiTaoDuLieu() {
        // ── Phiếu chờ thu (dữ liệu mẫu) ──
        danhSachPhieuCho.add(new PhieuChoThu("SC-2026-0111", "51H-135.79", "Trần Thị Bình",
                new BigDecimal("800000"),
                LocalDateTime.of(2026, 10, 5, 11, 0)));
        PhieuChoThu phieu0108 = new PhieuChoThu("SC-2026-0108", "51H-246.80", "Nguyễn Văn An",
                new BigDecimal("1270000"),
                LocalDateTime.of(2026, 10, 5, 15, 30));
        danhSachPhieuCho.add(phieu0108);

        // ── Hóa đơn đã thu (dữ liệu mẫu) — dùng entity HoaDon thực ──
        // Hóa đơn: dùng HinhThucThanhToan enum + LocalDateTime
        danhSachHoaDon.add(new HoaDon("HD-2026-0108", "SC-2026-0108", "Nguyễn Văn An", "51H-246.80",
                new BigDecimal("1270000"), HinhThucThanhToan.CHUYEN_KHOAN,
                LocalDateTime.of(2026, 10, 5, 14, 30), "thungan"));
        danhSachHoaDon.add(new HoaDon("HD-2026-0104", "SC-2026-0104", "Lê Văn Cường", "51G-789.12",
                new BigDecimal("1000000"), HinhThucThanhToan.TIEN_MAT,
                LocalDateTime.of(2026, 10, 4, 10, 15), "thungan"));
        
        // Xóa phiếu khỏi danh sách chờ khi đã thanh toán (logic transaction)
        danhSachPhieuCho.remove(phieu0108);
    }

    // ── Phiếu chờ thu ────────────────────────────────────────────────────────

    /**
     * Lấy danh sách phiếu chờ thu tiền (chưa lập hóa đơn).
     *
     * @return bản sao danh sách; rỗng nếu không có
     */
    public List<PhieuChoThu> layPhieuChoThu() {
        return new ArrayList<>(danhSachPhieuCho);
    }

    /**
     * Đếm số phiếu đang chờ thu — dùng cho thẻ thống kê dashboard.
     *
     * @return số phiếu chờ thu tiền
     */
    public int demPhieuChoThu() {
        return danhSachPhieuCho.size();
    }

    /**
     * Tìm phiếu chờ thu theo mã.
     *
     * @param maPhieu mã phiếu cần tìm
     * @return phiếu tương ứng hoặc Optional rỗng
     * @throws NullPointerException nếu maPhieu là null
     */
    public Optional<PhieuChoThu> timPhieuChoThuTheoMa(String maPhieu) {
        Objects.requireNonNull(maPhieu, "Mã phiếu không được null.");
        String ma = maPhieu.trim();
        return danhSachPhieuCho.stream()
                .filter(p -> p.getMaPhieu().equals(ma))
                .findFirst();
    }


    // ── Xác nhận thanh toán ──────────────────────────────────────────────────

    /**
     * Xác nhận thanh toán tiền mặt: kiểm tra điều kiện, lưu hóa đơn,
     * xóa phiếu khỏi danh sách chờ trong một transaction logic.
     *
     * @param maPhieu     mã phiếu cần thanh toán
     * @param soTienNhan  số tiền khách đưa
     * @param nguoiDuyet  tên tài khoản thu ngân
     * @return mã hóa đơn được tạo
     * @throws NullPointerException     nếu bất kỳ tham số nào là null
     * @throws IllegalArgumentException nếu phiếu không tồn tại hoặc điều kiện không hợp lệ
     */
    public String xacNhanThanhToanTienMat(String maPhieu, BigDecimal soTienNhan, String nguoiDuyet) {
        Objects.requireNonNull(maPhieu,    "Mã phiếu không được null.");
        Objects.requireNonNull(soTienNhan, "Số tiền không được null.");
        Objects.requireNonNull(nguoiDuyet, "Người duyệt không được null.");

        // Tìm phiếu chờ thu
        Optional<PhieuChoThu> opt = timPhieuChoThuTheoMa(maPhieu);
        if (!opt.isPresent()) {
            throw new IllegalArgumentException("Phiếu không tồn tại hoặc đã thanh toán: " + maPhieu);
        }

        PhieuChoThu phieu = opt.get();
        if (soTienNhan.compareTo(phieu.getTongTien()) < 0) {
            throw new IllegalArgumentException("Số tiền khách đưa không đủ.");
        }

        // Tạo hóa đơn
        String maHoaDon = "HD-" + System.currentTimeMillis(); // Mock: sinh mã tạm thời
        HoaDon hoaDon = new HoaDon(maHoaDon, maPhieu, phieu.getTenKhach(), phieu.getBienSo(),
                phieu.getTongTien(), HinhThucThanhToan.TIEN_MAT,
                LocalDateTime.now(), nguoiDuyet);

        // Lưu hóa đơn và xóa phiếu khỏi danh sách chờ
        danhSachHoaDon.add(hoaDon);
        danhSachPhieuCho.remove(phieu);

        return maHoaDon;
    }

    /**
     * Xác nhận thanh toán chuyển khoản/QR: kiểm tra điều kiện, lưu hóa đơn,
     * xóa phiếu khỏi danh sách chờ trong một transaction logic.
     *
     * @param maPhieu        mã phiếu cần thanh toán
     * @param soTienNhan     số tiền thực nhận qua ngân hàng
     * @param maGiaoDich     mã tham chiếu giao dịch
     * @param nguoiDuyet     tên tài khoản thu ngân
     * @return mã hóa đơn được tạo
     * @throws NullPointerException     nếu bất kỳ tham số nào là null
     * @throws IllegalArgumentException nếu phiếu không tồn tại hoặc điều kiện không hợp lệ
     */
    public String xacNhanThanhToanChuyenKhoan(String maPhieu, BigDecimal soTienNhan,
            String maGiaoDich, String nguoiDuyet) {
        Objects.requireNonNull(maPhieu,     "Mã phiếu không được null.");
        Objects.requireNonNull(soTienNhan,  "Số tiền không được null.");
        Objects.requireNonNull(maGiaoDich,  "Mã giao dịch không được null.");
        Objects.requireNonNull(nguoiDuyet,  "Người duyệt không được null.");

        String maGD = maGiaoDich.trim();
        if (maGD.isEmpty()) {
            throw new IllegalArgumentException("Mã giao dịch không được rỗng.");
        }

        // Tìm phiếu chờ thu
        Optional<PhieuChoThu> opt = timPhieuChoThuTheoMa(maPhieu);
        if (!opt.isPresent()) {
            throw new IllegalArgumentException("Phiếu không tồn tại hoặc đã thanh toán: " + maPhieu);
        }

        PhieuChoThu phieu = opt.get();
        if (soTienNhan.compareTo(phieu.getTongTien()) < 0) {
            throw new IllegalArgumentException("Số tiền nhận không đủ.");
        }

        // Tạo hóa đơn
        String maHoaDon = "HD-" + System.currentTimeMillis(); // Mock: sinh mã tạm thời
        HoaDon hoaDon = new HoaDon(maHoaDon, maPhieu, phieu.getTenKhach(), phieu.getBienSo(),
                phieu.getTongTien(), HinhThucThanhToan.CHUYEN_KHOAN,
                LocalDateTime.now(), nguoiDuyet);

        // Lưu hóa đơn và xóa phiếu khỏi danh sách chờ
        danhSachHoaDon.add(hoaDon);
        danhSachPhieuCho.remove(phieu);

        return maHoaDon;
    }

    // ── Hóa đơn đã thanh toán ────────────────────────────────────────────────

    /**
     * Lấy danh sách hóa đơn đã thanh toán — dùng entity HoaDon thực.
     *
     * @return bản sao danh sách; rỗng nếu không có
     */
    public List<HoaDon> layHoaDonDaThu() {
        return new ArrayList<>(danhSachHoaDon);
    }

    /**
     * Tìm hóa đơn theo mã.
     *
     * @param maHoaDon mã hóa đơn cần tìm
     * @return hóa đơn tương ứng hoặc Optional rỗng
     * @throws NullPointerException nếu maHoaDon là null
     */
    public Optional<HoaDon> timHoaDonTheoMa(String maHoaDon) {
        Objects.requireNonNull(maHoaDon, "Mã hóa đơn không được null.");
        String ma = maHoaDon.trim();
        return danhSachHoaDon.stream()
                .filter(hd -> hd.getMaHoaDon().equals(ma))
                .findFirst();
    }

    /**
     * Đếm số hóa đơn đã thanh toán — dùng cho thẻ thống kê dashboard.
     *
     * @return số hóa đơn
     */
    public int demHoaDonDaThu() {
        return danhSachHoaDon.size();
    }

    /**
     * Tính tổng tiền đã thu theo hình thức thanh toán.
     *
     * @param tenHinhThuc tên hình thức (ví dụ: "Tiền mặt", "Chuyển khoản")
     * @return tổng tiền; {@code BigDecimal.ZERO} nếu không có
     */
    public BigDecimal tinhTongTheoHinhThuc(String tenHinhThuc) {
        Objects.requireNonNull(tenHinhThuc, "Tên hình thức không được null.");
        BigDecimal tong = BigDecimal.ZERO;
        for (HoaDon hd : danhSachHoaDon) {
            if (hd.getHinhThucThanhToan().getTenHienThi().equals(tenHinhThuc)) {
                tong = tong.add(hd.getTongTien());
            }
        }
        return tong;
    }

    // ── Phiếu chờ thu — DTOs nội bộ ───────────────────────────────────────────

    /**
     * Dữ liệu tạm cho phiếu chờ thu — không phải entity, chỉ nhóm dữ liệu từ phiếu sửa chữa.
     */
    public static final class PhieuChoThu {
        private final String maPhieu;
        private final String bienSo;
        private final String tenKhach;
        private final BigDecimal tongTien;
        private final LocalDateTime thoiDiemHoanTat;

        /** Tạo phiếu chờ thu với đầy đủ thông tin. */
        public PhieuChoThu(String maPhieu, String bienSo, String tenKhach,
                BigDecimal tongTien, LocalDateTime thoiDiemHoanTat) {
            this.maPhieu         = Objects.requireNonNull(maPhieu);
            this.bienSo          = Objects.requireNonNull(bienSo);
            this.tenKhach        = Objects.requireNonNull(tenKhach);
            this.tongTien        = Objects.requireNonNull(tongTien);
            this.thoiDiemHoanTat = thoiDiemHoanTat; // null hợp lệ
        }

        public String getMaPhieu()              { return maPhieu; }
        public String getBienSo()               { return bienSo; }
        public String getTenKhach()             { return tenKhach; }
        public BigDecimal getTongTien()         { return tongTien; }
        public LocalDateTime getThoiDiemHoanTat() { return thoiDiemHoanTat; }

        /** Nhãn hàng đầu hiển thị trên danh sách. */
        public String getNhanHangDau() {
            return maPhieu + " · " + tenKhach;
        }

        /** Chi tiết phụ hiển thị dưới nhãn chính. */
        public String getNhanHangHai() {
            NumberFormat fmt = NumberFormat.getNumberInstance(
                    Locale.forLanguageTag("vi-VN"));
            return "Tổng cần thu: " + fmt.format(tongTien) + "đ · đã hoàn tất sửa";
        }
    }
}
