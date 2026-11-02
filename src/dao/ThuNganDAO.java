package dao;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

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
    /** Danh sách hóa đơn đã phát hành trong phiên. */
    private final List<HoaDonThuNgan> danhSachHoaDon  = new ArrayList<>();

    /** Tạo DAO với dữ liệu mẫu khớp màn hình UI. */
    public ThuNganDAO() {
        khoiTaoDuLieu();
    }

    private void khoiTaoDuLieu() {
        // ── Phiếu chờ thu (dữ liệu mẫu) ──
        danhSachPhieuCho.add(new PhieuChoThu("SC-2026-0111", "51H-135.79", "Trần Thị Bình",
                new BigDecimal("800000"),
                LocalDateTime.of(2026, 10, 5, 11, 0)));
        danhSachPhieuCho.add(new PhieuChoThu("SC-2026-0124", "51H-246.80", "Nguyễn Văn An",
                new BigDecimal("1270000"),
                LocalDateTime.of(2026, 10, 5, 15, 30)));

        // ── Hóa đơn đã thu (dữ liệu mẫu) ──
        danhSachHoaDon.add(new HoaDonThuNgan("HD-2026-0108", "SC-2026-0108", "Nguyễn Văn An",
                new BigDecimal("1270000"), "Chuyển khoản", "05/10/2026 14:30", "thungan"));
        danhSachHoaDon.add(new HoaDonThuNgan("HD-2026-0104", "SC-2026-0104", "Lê Văn Cường",
                new BigDecimal("1000000"), "Tiền mặt", "04/10/2026 10:15", "thungan"));
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
     * xóa phiếu khỏi danh sách chờ.
     *
     * <p>Điều kiện:</p>
     * <ul>
     *   <li>Phiếu phải tồn tại trong danh sách chờ.</li>
     *   <li>Số tiền thực nhận không thấp hơn tổng phải thu.</li>
     *   <li>Người thu không được rỗng.</li>
     * </ul>
     * Khi kết nối DB: thay bằng INSERT HoaDon + UPDATE TrangThai trong cùng transaction.
     *
     * @param maPhieu    mã phiếu cần thu tiền
     * @param soTienNhan số tiền thực nhận (đồng)
     * @param nguoiThu   tên đăng nhập của thu ngân
     * @return mã hóa đơn vừa tạo
     * @throws NullPointerException     nếu bất kỳ tham số nào là null
     * @throws IllegalArgumentException nếu nguoiThu rỗng
     * @throws IllegalStateException    nếu phiếu không tồn tại hoặc tiền chưa đủ
     */
    public String xacNhanThanhToanTienMat(String maPhieu,
            BigDecimal soTienNhan, String nguoiThu) {
        Objects.requireNonNull(maPhieu,    "Mã phiếu không được null.");
        Objects.requireNonNull(soTienNhan, "Số tiền nhận không được null.");
        Objects.requireNonNull(nguoiThu,   "Người thu không được null.");
        if (nguoiThu.isBlank()) {
            throw new IllegalArgumentException("Người thu không được rỗng.");
        }

        PhieuChoThu phieu = timPhieuChoThuTheoMa(maPhieu)
                .orElseThrow(() -> new IllegalStateException(
                        "Phiếu không tồn tại hoặc đã thanh toán: " + maPhieu));

        if (soTienNhan.compareTo(phieu.getTongTien()) < 0) {
            throw new IllegalStateException(
                    "Số tiền nhận chưa đủ. Cần: " + phieu.getTongTien()
                    + " · Nhận: " + soTienNhan);
        }

        // Sinh mã hóa đơn — khi có DB dùng SEQUENCE hoặc UUID.
        String maHoaDon = "HD-" + LocalDateTime.now().getYear()
                + "-" + String.format("%04d", danhSachHoaDon.size() + 1001);

        danhSachHoaDon.add(new HoaDonThuNgan(
                maHoaDon, maPhieu, phieu.getTenKhach(),
                phieu.getTongTien(), "Tiền mặt",
                LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                nguoiThu));

        // Xóa phiếu khỏi danh sách chờ sau khi lưu hóa đơn thành công.
        danhSachPhieuCho.removeIf(p -> p.getMaPhieu().equals(maPhieu.trim()));
        return maHoaDon;
    }

    // ── Hóa đơn đã thu ───────────────────────────────────────────────────────

    /**
     * Lấy danh sách hóa đơn đã thanh toán.
     * Khi kết nối DB: thay bằng SELECT từ bảng HoaDon.
     *
     * @return bản sao danh sách; bao gồm hóa đơn vừa tạo trong phiên
     */
    public List<HoaDonThuNgan> layHoaDonDaThu() {
        return new ArrayList<>(danhSachHoaDon);
    }

    /**
     * Đếm số hóa đơn đã thu — dùng cho thẻ thống kê dashboard.
     *
     * @return số hóa đơn đã phát hành
     */
    public int demHoaDonDaThu() {
        return danhSachHoaDon.size();
    }

    /**
     * Tính tổng tiền đã thu (tất cả hình thức) — dùng cho thẻ thống kê.
     *
     * @return tổng tiền đã thu
     */
    public BigDecimal tinhTongDaThu() {
        return danhSachHoaDon.stream()
                .map(HoaDonThuNgan::getTongTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Tính tổng tiền đã thu theo hình thức — dùng cho thẻ thống kê.
     *
     * @param hinhThuc "Tiền mặt" hoặc "Chuyển khoản"
     * @return tổng tiền theo hình thức
     */
    public BigDecimal tinhTongTheoHinhThuc(String hinhThuc) {
        return danhSachHoaDon.stream()
                .filter(hd -> hd.getHinhThuc().equals(hinhThuc))
                .map(HoaDonThuNgan::getTongTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Tính tổng chi phí của một phiếu — dùng khi hiển thị chi tiết đối chiếu.
     *
     * @param maPhieu mã phiếu cần tính
     * @return tổng tiền; {@code BigDecimal.ZERO} nếu không tìm thấy
     */
    public BigDecimal tinhTongTheoPhieu(String maPhieu) {
        Objects.requireNonNull(maPhieu, "Mã phiếu không được null.");
        return timPhieuChoThuTheoMa(maPhieu)
                .map(PhieuChoThu::getTongTien)
                .orElse(BigDecimal.ZERO);
    }

    // ── DTO ──────────────────────────────────────────────────────────────────

    /**
     * Dữ liệu phiếu chờ thu tiền trả ra cho UI.
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

    /**
     * Dữ liệu hóa đơn đã thu trả ra cho UI.
     */
    public static final class HoaDonThuNgan {
        private final String maHoaDon;
        private final String maPhieu;
        private final String tenKhach;
        private final BigDecimal tongTien;
        private final String hinhThuc;
        private final String thoiDiem;
        private final String nguoiThu;

        /** Tạo hóa đơn đã thu với đầy đủ thông tin. */
        public HoaDonThuNgan(String maHoaDon, String maPhieu, String tenKhach,
                BigDecimal tongTien, String hinhThuc, String thoiDiem, String nguoiThu) {
            this.maHoaDon = Objects.requireNonNull(maHoaDon);
            this.maPhieu  = Objects.requireNonNull(maPhieu);
            this.tenKhach = Objects.requireNonNull(tenKhach);
            this.tongTien = Objects.requireNonNull(tongTien);
            this.hinhThuc = Objects.requireNonNull(hinhThuc);
            this.thoiDiem = Objects.requireNonNull(thoiDiem);
            this.nguoiThu = Objects.requireNonNull(nguoiThu);
        }

        public String getMaHoaDon()       { return maHoaDon; }
        public String getMaPhieu()        { return maPhieu; }
        public String getTenKhach()       { return tenKhach; }
        public BigDecimal getTongTien()   { return tongTien; }
        public String getHinhThuc()       { return hinhThuc; }
        public String getThoiDiem()       { return thoiDiem; }
        public String getNguoiThu()       { return nguoiThu; }

        /** Nhãn hàng đầu. */
        public String getNhanHangDau() {
            return maHoaDon + " · " + tenKhach;
        }

        /** Chi tiết phụ. */
        public String getNhanHangHai() {
            NumberFormat fmt = NumberFormat.getNumberInstance(
                    Locale.forLanguageTag("vi-VN"));
            return fmt.format(tongTien) + "đ · " + hinhThuc + " · gửi tiếp nhận bàn giao";
        }
    }
}
