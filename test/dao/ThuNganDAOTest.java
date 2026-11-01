package dao;

import java.math.BigDecimal;
import java.util.List;

import dao.ThuNganDAO.HoaDonThuNgan;
import dao.ThuNganDAO.PhieuChoThu;

/**
 * Kiểm thử {@link ThuNganDAO} với dữ liệu in-memory.
 * Chạy bằng {@code main()} — cùng phong cách {@code TaiKhoanDAOTest}.
 * Không cần database hay Swing.
 */
public final class ThuNganDAOTest {

    private ThuNganDAOTest() { }

    /**
     * Chạy toàn bộ ca kiểm thử; in PASS hoặc dừng với AssertionError.
     *
     * @param args không sử dụng
     */
    public static void main(String[] args) {

        // ── layPhieuChoThu ────────────────────────────────────────────────────
        {
            ThuNganDAO dao = new ThuNganDAO();
            List<PhieuChoThu> cho = dao.layPhieuChoThu();
            check(!cho.isEmpty(), "Phải có phiếu chờ thu.");
            for (PhieuChoThu p : cho) {
                check(p.getMaPhieu()   != null && !p.getMaPhieu().isBlank(),   "Mã phiếu không được rỗng.");
                check(p.getBienSo()    != null && !p.getBienSo().isBlank(),    "Biển số không được rỗng.");
                check(p.getTenKhach()  != null && !p.getTenKhach().isBlank(),  "Tên khách không được rỗng.");
                check(p.getTongTien()  != null &&
                        p.getTongTien().compareTo(BigDecimal.ZERO) > 0,        "Tổng tiền phải dương.");
                check(p.getNhanHangDau()  != null, "getNhanHangDau() không được null.");
                check(p.getNhanHangHai() != null,  "getNhanHangHai() không được null.");
            }
            check(dao.demPhieuChoThu() == cho.size(), "demPhieuChoThu() phải khớp kích thước.");
            System.out.println("PASS: layPhieuChoThu");
        }

        // ── timPhieuChoThuTheoMa ──────────────────────────────────────────────
        {
            ThuNganDAO dao = new ThuNganDAO();
            List<PhieuChoThu> cho = dao.layPhieuChoThu();
            if (!cho.isEmpty()) {
                String maDau = cho.get(0).getMaPhieu();
                check(dao.timPhieuChoThuTheoMa(maDau).isPresent(),
                        "Phải tìm thấy phiếu đầu tiên.");
                check(dao.timPhieuChoThuTheoMa("  " + maDau + "  ").isPresent(),
                        "Phải trim mã khi tìm.");
            }
            check(dao.timPhieuChoThuTheoMa("KHONG_TON_TAI").isEmpty(),
                    "Mã không tồn tại phải trả rỗng.");
            expectError(NullPointerException.class,
                    () -> dao.timPhieuChoThuTheoMa(null));
            System.out.println("PASS: timPhieuChoThuTheoMa");
        }

        // ── xacNhanThanhToanTienMat — thành công ─────────────────────────────
        {
            ThuNganDAO dao = new ThuNganDAO();
            List<PhieuChoThu> cho = dao.layPhieuChoThu();
            check(!cho.isEmpty(), "Cần có phiếu để kiểm thử xác nhận.");
            PhieuChoThu phieu = cho.get(0);
            int soHoaDonTruoc = dao.demHoaDonDaThu();

            String maHoaDon = dao.xacNhanThanhToanTienMat(
                    phieu.getMaPhieu(), phieu.getTongTien(), "thungan");

            check(maHoaDon != null && !maHoaDon.isBlank(),
                    "Phải trả mã hóa đơn sau xác nhận.");
            check(dao.demHoaDonDaThu() == soHoaDonTruoc + 1,
                    "Số hóa đơn phải tăng 1 sau xác nhận.");

            // Phiếu đã thu không được còn trong danh sách chờ
            check(dao.timPhieuChoThuTheoMa(phieu.getMaPhieu()).isEmpty(),
                    "Phiếu đã thanh toán không được còn trong danh sách chờ.");

            // Tìm lại hóa đơn vừa tạo
            HoaDonThuNgan hd = dao.layHoaDonDaThu().stream()
                    .filter(h -> h.getMaHoaDon().equals(maHoaDon))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Không tìm thấy hóa đơn vừa tạo."));
            check("Tiền mặt".equals(hd.getHinhThuc()), "Hình thức phải là Tiền mặt.");
            check(phieu.getMaPhieu().equals(hd.getMaPhieu()), "Mã phiếu trên hóa đơn phải khớp.");
            System.out.println("PASS: xacNhanThanhToanTienMat — thành công");
        }

        // ── xacNhanThanhToanTienMat — tiền chưa đủ ───────────────────────────
        {
            ThuNganDAO dao = new ThuNganDAO();
            List<PhieuChoThu> cho = dao.layPhieuChoThu();
            if (!cho.isEmpty()) {
                PhieuChoThu phieu = cho.get(0);
                BigDecimal thieu = phieu.getTongTien().subtract(BigDecimal.ONE);
                expectError(IllegalStateException.class,
                        () -> dao.xacNhanThanhToanTienMat(
                                phieu.getMaPhieu(), thieu, "thungan"));
            }
            System.out.println("PASS: xacNhanThanhToanTienMat — tiền chưa đủ");
        }

        // ── xacNhanThanhToanTienMat — phiếu không tồn tại ────────────────────
        {
            ThuNganDAO dao = new ThuNganDAO();
            expectError(IllegalStateException.class,
                    () -> dao.xacNhanThanhToanTienMat(
                            "SC-KHONG_TON_TAI", new BigDecimal("999999"), "thungan"));
            System.out.println("PASS: xacNhanThanhToanTienMat — phiếu không tồn tại");
        }

        // ── xacNhanThanhToanTienMat — hợp đồng đầu vào ───────────────────────
        {
            ThuNganDAO dao = new ThuNganDAO();
            expectError(NullPointerException.class,
                    () -> dao.xacNhanThanhToanTienMat(null, BigDecimal.ONE, "thungan"));
            expectError(NullPointerException.class,
                    () -> dao.xacNhanThanhToanTienMat("SC-X", null, "thungan"));
            expectError(NullPointerException.class,
                    () -> dao.xacNhanThanhToanTienMat("SC-X", BigDecimal.ONE, null));
            expectError(IllegalArgumentException.class,
                    () -> dao.xacNhanThanhToanTienMat("SC-X", BigDecimal.ONE, "  "));
            System.out.println("PASS: xacNhanThanhToanTienMat — hợp đồng đầu vào");
        }

        // ── thống kê ─────────────────────────────────────────────────────────
        {
            ThuNganDAO dao = new ThuNganDAO();
            check(dao.tinhTongDaThu().compareTo(BigDecimal.ZERO) >= 0,
                    "Tổng đã thu không được âm.");
            check(dao.tinhTongTheoHinhThuc("Tiền mặt").compareTo(BigDecimal.ZERO) >= 0,
                    "Tổng tiền mặt không được âm.");
            check(dao.tinhTongTheoHinhThuc("Chuyển khoản").compareTo(BigDecimal.ZERO) >= 0,
                    "Tổng chuyển khoản không được âm.");
            System.out.println("PASS: thống kê");
        }

        System.out.println("\nTất cả ca kiểm thử ThuNganDAO PASS.");
    }

    // ── Tiện ích ─────────────────────────────────────────────────────────────

    private static void check(boolean dieuKien, String thongBao) {
        if (!dieuKien) {
            throw new AssertionError(thongBao);
        }
    }

    private static void expectError(Class<? extends RuntimeException> loaiLoi, Runnable thaoTac) {
        try {
            thaoTac.run();
        } catch (RuntimeException e) {
            check(loaiLoi.isInstance(e),
                    "Sai loại lỗi: " + e.getClass().getSimpleName()
                    + " — mong đợi: " + loaiLoi.getSimpleName());
            return;
        }
        throw new AssertionError("Thiếu lỗi: " + loaiLoi.getSimpleName());
    }
}
