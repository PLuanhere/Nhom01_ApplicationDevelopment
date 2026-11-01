package dao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import entity.HinhThucThanhToan;
import entity.HoaDon;
import entity.PhieuSuaChua;
import entity.TrangThaiPhieu;

/**
 * Kiểm thử {@link HoaDonDAO} với dữ liệu in-memory, không cần database hay Swing.
 * Chạy bằng {@code main()} — cùng phong cách với {@code TaiKhoanDAOTest}.
 */
public final class HoaDonDAOTest {

    private HoaDonDAOTest() { }

    /**
     * Chạy toàn bộ ca kiểm thử; in PASS hoặc dừng với AssertionError.
     *
     * @param args không sử dụng
     */
    public static void main(String[] args) {

        // ── Phiếu chờ thanh toán ──────────────────────────────────────────────
        {
            HoaDonDAO dao = new HoaDonDAO();
            List<PhieuSuaChua> cho = dao.layPhieuChoThanhToan();
            check(!cho.isEmpty(), "Phải có phiếu chờ thanh toán.");
            for (PhieuSuaChua p : cho) {
                check(p.getTrangThai() == TrangThaiPhieu.HOAN_THANH,
                        "Chỉ phiếu HOAN_THANH mới chờ thanh toán.");
            }
            check(dao.demPhieuChoThanhToan() == cho.size(),
                    "demPhieuChoThanhToan() phải khớp kích thước danh sách.");
            System.out.println("PASS: layPhieuChoThanhToan");
        }

        // ── timPhieuTheoMa ────────────────────────────────────────────────────
        {
            HoaDonDAO dao = new HoaDonDAO();
            check(dao.timPhieuTheoMa("SC-2026-0108").isPresent(), "Phải tìm thấy SC-2026-0108.");
            check(dao.timPhieuTheoMa("KHONG_TON_TAI").isEmpty(), "Mã không tồn tại phải trả rỗng.");
            check(dao.timPhieuTheoMa("  SC-2026-0108  ").isPresent(), "Phải trim mã phiếu khi tìm.");
            expectError(NullPointerException.class, () -> dao.timPhieuTheoMa(null));
            System.out.println("PASS: timPhieuTheoMa");
        }

        // ── traKiemHoaDon ─────────────────────────────────────────────────────
        {
            HoaDonDAO dao = new HoaDonDAO();

            // Tất cả: trả toàn bộ
            List<HoaDon> tatCa = dao.traKiemHoaDon("", null);
            check(!tatCa.isEmpty(), "Phải có hóa đơn khi tra cứu tất cả.");

            // Lọc theo hình thức
            List<HoaDon> tienMat = dao.traKiemHoaDon(null, HinhThucThanhToan.TIEN_MAT);
            for (HoaDon hd : tienMat) {
                check(hd.getHinhThucThanhToan() == HinhThucThanhToan.TIEN_MAT,
                        "Kết quả phải là tiền mặt.");
            }

            List<HoaDon> ck = dao.traKiemHoaDon(null, HinhThucThanhToan.CHUYEN_KHOAN);
            for (HoaDon hd : ck) {
                check(hd.getHinhThucThanhToan() == HinhThucThanhToan.CHUYEN_KHOAN,
                        "Kết quả phải là chuyển khoản.");
            }

            // Từ khóa không khớp → rỗng
            List<HoaDon> khongCo = dao.traKiemHoaDon("XXXXXXX", null);
            check(khongCo.isEmpty(), "Từ khóa không khớp phải trả danh sách rỗng.");

            // Tìm theo mã hóa đơn
            List<HoaDon> theoMa = dao.traKiemHoaDon("HD-2026-0108", null);
            check(theoMa.size() == 1, "Phải tìm đúng 1 hóa đơn theo mã.");
            check("HD-2026-0108".equals(theoMa.get(0).getMaHoaDon()), "Sai mã hóa đơn.");

            // Tìm theo tên khách (không phân biệt hoa thường)
            List<HoaDon> theoTen = dao.traKiemHoaDon("nguyễn văn an", null);
            check(!theoTen.isEmpty(), "Tìm theo tên không phân biệt hoa thường phải có kết quả.");
            System.out.println("PASS: traKiemHoaDon");
        }

        // ── luuHoaDon ─────────────────────────────────────────────────────────
        {
            HoaDonDAO dao = new HoaDonDAO();
            int soPhieuTruoc = dao.demPhieuChoThanhToan();

            HoaDon hdMoi = new HoaDon("HD-2026-9999", "SC-2026-0108", "Nguyễn Văn An",
                    new BigDecimal("1270000"), HinhThucThanhToan.TIEN_MAT,
                    LocalDateTime.of(2026, 10, 5, 15, 0), "thungan");
            dao.luuHoaDon(hdMoi);

            // Hóa đơn phải được lưu
            check(dao.timHoaDonTheoMa("HD-2026-9999").isPresent(), "Hóa đơn mới phải tìm thấy.");

            // Phiếu phải chuyển sang DA_THANH_TOAN
            PhieuSuaChua phieuSau = dao.timPhieuTheoMa("SC-2026-0108").orElseThrow();
            check(phieuSau.getTrangThai() == TrangThaiPhieu.DA_THANH_TOAN,
                    "Phiếu phải chuyển sang DA_THANH_TOAN sau khi lưu hóa đơn.");

            // Số phiếu chờ phải giảm 1
            check(dao.demPhieuChoThanhToan() == soPhieuTruoc - 1,
                    "Số phiếu chờ phải giảm 1 sau khi lưu hóa đơn.");

            // Không được lập lại hóa đơn cho cùng phiếu
            HoaDon hdTrung = new HoaDon("HD-2026-8888", "SC-2026-0108", "Nguyễn Văn An",
                    new BigDecimal("1270000"), HinhThucThanhToan.TIEN_MAT,
                    LocalDateTime.of(2026, 10, 5, 16, 0), "thungan");
            expectError(IllegalStateException.class, () -> dao.luuHoaDon(hdTrung));

            // Không được dùng lại mã hóa đơn
            HoaDon hdMaTrung = new HoaDon("HD-2026-9999", "SC-2026-0111", "Trần Thị Bình",
                    new BigDecimal("600000"), HinhThucThanhToan.TIEN_MAT,
                    LocalDateTime.of(2026, 10, 5, 16, 0), "thungan");
            expectError(IllegalStateException.class, () -> dao.luuHoaDon(hdMaTrung));

            // Phiếu không tồn tại
            HoaDon hdPhieuLa = new HoaDon("HD-2026-7777", "SC-KHONG_CO", "Test",
                    new BigDecimal("100000"), HinhThucThanhToan.TIEN_MAT,
                    LocalDateTime.now(), "thungan");
            expectError(IllegalStateException.class, () -> dao.luuHoaDon(hdPhieuLa));

            expectError(NullPointerException.class, () -> dao.luuHoaDon(null));
            System.out.println("PASS: luuHoaDon");
        }

        // ── Thống kê ──────────────────────────────────────────────────────────
        {
            HoaDonDAO dao = new HoaDonDAO();
            LocalDate tu   = LocalDate.of(2026, 10, 1);
            LocalDate den  = LocalDate.of(2026, 10, 31);

            BigDecimal tong = dao.tinhTongDaThu(tu, den);
            check(tong.compareTo(BigDecimal.ZERO) >= 0, "Tổng đã thu không được âm.");

            BigDecimal tongTM = dao.tinhTongDaThuTheoHinhThuc(tu, den, HinhThucThanhToan.TIEN_MAT);
            BigDecimal tongCK = dao.tinhTongDaThuTheoHinhThuc(tu, den, HinhThucThanhToan.CHUYEN_KHOAN);
            // Tổng theo hình thức không vượt tổng chung (có thể có hóa đơn ngoài tháng 10)
            check(tongTM.compareTo(BigDecimal.ZERO) >= 0, "Tổng tiền mặt không được âm.");
            check(tongCK.compareTo(BigDecimal.ZERO) >= 0, "Tổng chuyển khoản không được âm.");

            // Khoảng ngày không hợp lệ
            expectError(IllegalArgumentException.class,
                    () -> dao.tinhTongDaThu(den, tu));
            System.out.println("PASS: tinhTongDaThu");
        }

        // ── Entity ────────────────────────────────────────────────────────────
        {
            // PhieuSuaChua kiểm tra nội tại
            expectError(IllegalArgumentException.class, () -> new PhieuSuaChua(
                    "", "51H-000.00", "Test",
                    LocalDate.now(), null, BigDecimal.ZERO, TrangThaiPhieu.TIEP_NHAN));
            expectError(IllegalArgumentException.class, () -> new PhieuSuaChua(
                    "SC-X", "51H-000.00", "Test",
                    LocalDate.now(), null, new BigDecimal("-1"), TrangThaiPhieu.TIEP_NHAN));
            expectError(IllegalArgumentException.class, () -> new PhieuSuaChua(
                    "SC-X", "51H-000.00", "Test",
                    LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 4),
                    BigDecimal.ZERO, TrangThaiPhieu.HOAN_THANH));

            // HoaDon kiểm tra nội tại
            expectError(IllegalArgumentException.class, () -> new HoaDon(
                    "HD-X", "SC-X", "Test",
                    new BigDecimal("-1"), HinhThucThanhToan.TIEN_MAT,
                    LocalDateTime.now(), "thungan"));
            expectError(NullPointerException.class, () -> new HoaDon(
                    "HD-X", "SC-X", "Test",
                    BigDecimal.ZERO, null, LocalDateTime.now(), "thungan"));

            // HinhThucThanhToan.tuTenHienThi
            check(HinhThucThanhToan.tuTenHienThi("Tiền mặt") == HinhThucThanhToan.TIEN_MAT,
                    "tuTenHienThi phải ánh xạ đúng.");
            check(HinhThucThanhToan.tuTenHienThi("Tất cả") == null,
                    "Tất cả không ánh xạ sang enum.");
            System.out.println("PASS: entity validation");
        }

        System.out.println("\nTất cả ca kiểm thử PASS.");
    }

    // ── Tiện ích ─────────────────────────────────────────────────────────────

    private static void check(boolean dieu_kien, String thongBao) {
        if (!dieu_kien) {
            throw new AssertionError(thongBao);
        }
    }

    private static void expectError(Class<? extends RuntimeException> loaiLoi, Runnable thaoTac) {
        try {
            thaoTac.run();
        } catch (RuntimeException e) {
            check(loaiLoi.isInstance(e), "Sai loại lỗi: " + e.getClass().getSimpleName()
                    + " — mong đợi: " + loaiLoi.getSimpleName());
            return;
        }
        throw new AssertionError("Thiếu lỗi: " + loaiLoi.getSimpleName());
    }
}
