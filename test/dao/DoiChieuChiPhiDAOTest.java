package dao;

import java.math.BigDecimal;
import java.util.List;

import dao.DoiChieuChiPhiDAO.DongChiPhi;
import dao.DoiChieuChiPhiDAO.KetQuaDoiChieu;

/**
 * Kiểm thử {@link DoiChieuChiPhiDAO} với dữ liệu in-memory.
 * Chạy bằng {@code main()} — cùng phong cách với các DAOTest khác.
 * Không cần database hay Swing.
 */
public final class DoiChieuChiPhiDAOTest {

    private DoiChieuChiPhiDAOTest() { }

    /**
     * Chạy toàn bộ ca kiểm thử; in PASS hoặc dừng với AssertionError.
     *
     * @param args không sử dụng
     */
    public static void main(String[] args) {

        // ── layDanhSachMaPhieu ───────────────────────────────────────────────────────
        {
            DoiChieuChiPhiDAO dao = new DoiChieuChiPhiDAO();
            List<String> ds = dao.layDanhSachMaPhieu();
            check(!ds.isEmpty(), "Phải có ít nhất một phiếu trong mock.");
            check(ds.contains("SC-2026-0108"), "Phải chứa SC-2026-0108.");
            check(ds.contains("SC-2026-0111"), "Phải chứa SC-2026-0111.");
            System.out.println("PASS: layDanhSachMaPhieu");
        }

        // ── duDieuKienDoiChieu ─────────────────────────────────────────────────────
        {
            DoiChieuChiPhiDAO dao = new DoiChieuChiPhiDAO();
            check(dao.duDieuKienDoiChieu("SC-2026-0108"),
                    "SC-2026-0108 phải đủ điều kiện đối chiếu.");
            check(!dao.duDieuKienDoiChieu("SC-KHONG_TON_TAI"),
                    "Phiếu không tồn tại phải trả false.");
            check(dao.duDieuKienDoiChieu("  SC-2026-0108  "),
                    "Phải trim mã phiếu khi kiểm tra.");
            expectError(NullPointerException.class,
                    () -> dao.duDieuKienDoiChieu(null));
            System.out.println("PASS: duDieuKienDoiChieu");
        }

        // ── layKetQuaDoiChieu — thành công ─────────────────────────────────────────
        {
            DoiChieuChiPhiDAO dao = new DoiChieuChiPhiDAO();
            KetQuaDoiChieu kq = dao.layKetQuaDoiChieu("SC-2026-0108");

            check("SC-2026-0108".equals(kq.getMaPhieu()),  "Mã phiếu phải khớp.");
            check("Nguyễn Văn An".equals(kq.getTenKhach()), "Tên khách phải khớp.");
            check("51H-246.80".equals(kq.getBienSo()),     "Biển số phải khớp.");
            check(!kq.getDanhSachChiPhi().isEmpty(),        "Phải có ít nhất một dòng chi phí.");
            check(kq.getTongThucTe().compareTo(BigDecimal.ZERO) > 0,
                    "Tổng phải dương.");

            // Kiểm tra tổng = 300000 + 100000 + 720000 + 150000 = 1270000
            check(kq.getTongThucTe().compareTo(new BigDecimal("1270000")) == 0,
                    "Tổng SC-2026-0108 phải là 1.270.000.");

            System.out.println("PASS: layKetQuaDoiChieu — thành công");
        }

        // ── layKetQuaDoiChieu — trim mã ─────────────────────────────────────────────
        {
            DoiChieuChiPhiDAO dao = new DoiChieuChiPhiDAO();
            KetQuaDoiChieu kq = dao.layKetQuaDoiChieu("  SC-2026-0108  ");
            check("SC-2026-0108".equals(kq.getMaPhieu()), "Phải trim mã phiếu khi lấy.");
            System.out.println("PASS: layKetQuaDoiChieu — trim mã");
        }

        // ── layKetQuaDoiChieu — phiếu không tồn tại ───────────────────────────────
        {
            DoiChieuChiPhiDAO dao = new DoiChieuChiPhiDAO();
            expectError(IllegalArgumentException.class,
                    () -> dao.layKetQuaDoiChieu("SC-KHONG_TON_TAI"));
            expectError(NullPointerException.class,
                    () -> dao.layKetQuaDoiChieu(null));
            System.out.println("PASS: layKetQuaDoiChieu — phiếu không tồn tại");
        }

        // ── timKetQua ───────────────────────────────────────────────────────────────
        {
            DoiChieuChiPhiDAO dao = new DoiChieuChiPhiDAO();
            check(dao.timKetQua("SC-2026-0108").isPresent(),
                    "timKetQua phải trả giá trị khi phiếu tồn tại.");
            check(dao.timKetQua("KHONG_TON_TAI").isEmpty(),
                    "timKetQua phải trả rỗng khi không tìm thấy.");
            expectError(NullPointerException.class, () -> dao.timKetQua(null));
            System.out.println("PASS: timKetQua");
        }

        // ── DongChiPhi — kiểm tra nội tại ──────────────────────────────────────────
        {
            // Đơn giá âm phải bị từ chối
            expectError(IllegalArgumentException.class, () ->
                new DongChiPhi("Test", "1 cái",
                        new BigDecimal("-1"), new BigDecimal("100000")));
            // Thành tiền âm phải bị từ chối
            expectError(IllegalArgumentException.class, () ->
                new DongChiPhi("Test", "1 cái",
                        new BigDecimal("100000"), new BigDecimal("-1")));
            // null phải bị từ chối
            expectError(NullPointerException.class, () ->
                new DongChiPhi(null, "1 cái",
                        BigDecimal.ZERO, BigDecimal.ZERO));
            System.out.println("PASS: DongChiPhi — kiểm tra nội tại");
        }

        // ── KetQuaDoiChieu — tự tính tổng ───────────────────────────────────────────
        {
            DoiChieuChiPhiDAO dao = new DoiChieuChiPhiDAO();
            KetQuaDoiChieu kq0111 = dao.layKetQuaDoiChieu("SC-2026-0111");
            // 300000 + 500000 = 800000
            check(kq0111.getTongThucTe().compareTo(new BigDecimal("800000")) == 0,
                    "Tổng SC-2026-0111 phải là 800.000.");

            // getNhanPhieu và getNhanKhach
            check(kq0111.getNhanPhieu().contains("SC-2026-0111"), "getNhanPhieu phải chứa mã phiếu.");
            check(kq0111.getNhanKhach().contains("Trần Thị Bình"), "getNhanKhach phải chứa tên khách.");
            check(kq0111.getNhanKhach().contains("51H-135.79"),      "getNhanKhach phải chứa biển số.");
            System.out.println("PASS: KetQuaDoiChieu — tự tính tổng");
        }

        System.out.println("\nTất cả ca kiểm thử DoiChieuChiPhiDAO PASS.");
    }

    // ── Tiện ích ───────────────────────────────────────────────────────────────

    private static void check(boolean dieuKien, String thongBao) {
        if (!dieuKien) throw new AssertionError(thongBao);
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
