package dao.hoadon;

import java.util.ArrayList;
import java.util.List;
import entity.nhansu.TaiKhoan;
import entity.nhansu.VaiTro;
import dao.hoadon.DoiChieuChiPhiDAO.DongChiPhi;
import dao.hoadon.DoiChieuChiPhiDAO.KetQuaDoiChieu;

/** Kiểm tra đối chiếu và thanh toán cùng nguồn sau khi gộp main. */
public final class DoiChieuChiPhiDAOTest {
    private static int dem;
    public static void main(String[] args) {
        HoaDonDAO hoaDon = new HoaDonDAO();
        TaiKhoan tk = new TaiKhoan("thungan", VaiTro.THU_NGAN);
        DoiChieuChiPhiDAO dao = new DoiChieuChiPhiDAO(hoaDon, tk);
        dung(dao.layDanhSachMaPhieu().size() == 2, "Danh sách khớp phiếu chờ");
        dung(!dao.duDieuKienDoiChieu("SC-2026-0108"), "Hóa đơn đã thu không được đối chiếu lại");
        dung(dao.duDieuKienDoiChieu("  SC-2026-0111  "), "Tra cứu trim mã");
        dung(dao.timKetQua("KHONG_CO") == null, "Không có thì trả null");
        KetQuaDoiChieu kq = dao.layKetQuaDoiChieu("SC-2026-0111");
        dung(kq.getBienSo().equals("51H-135.79") && kq.getTenKhach().equals("Trần Thị Bình"), "Đúng khách và xe");
        dung(kq.getTongThucTe() == 800000, "Tổng đối chiếu khớp nguồn thu");
        kq.getDanhSachChiPhi().clear();
        dung(!kq.getDanhSachChiPhi().isEmpty(), "Trả bản sao danh sách");
        try { hoaDon.xacNhanThanhToanChuyenKhoan(tk, "SC-2026-0111", 800000, " "); throw new AssertionError("Mã rỗng"); }
        catch (IllegalArgumentException expected) { dem++; }
        try { hoaDon.xacNhanThanhToanChuyenKhoan(tk, "SC-2026-0111", 799999, "GD-1"); throw new AssertionError("Thiếu tiền"); }
        catch (IllegalArgumentException expected) { dem++; }
        try { hoaDon.xacNhanThanhToanChuyenKhoan(new TaiKhoan("quanly", VaiTro.QUAN_LY), "SC-2026-0111", 800000, "GD-1"); throw new AssertionError("Sai vai trò"); }
        catch (SecurityException expected) { dem++; }
        dung(hoaDon.demHoaDonDaThu(tk) == 2 && dao.duDieuKienDoiChieu("SC-2026-0111"), "Lỗi không ghi một phần");
        String ma = hoaDon.xacNhanThanhToanChuyenKhoan(tk, "SC-2026-0111", 900000, " GD-1 ");
        dung(hoaDon.timHoaDonTheoMa(tk, ma).getMaGiaoDich().equals("GD-1"), "Giữ mã tham chiếu");
        dung(hoaDon.timHoaDonTheoMa(tk, ma).getBienSo().equals("51H-135.79"), "Giữ biển số hóa đơn");
        dung(hoaDon.timHoaDonTheoMa(tk, ma).getTongTien() == 800000, "Không cộng tiền thừa vào hóa đơn");
        dung(hoaDon.timKiem(tk, "51H-135.79", null).size() == 1, "Tìm hóa đơn theo biển số");
        dung(!dao.duDieuKienDoiChieu("SC-2026-0111"), "Thanh toán xong rời nguồn đối chiếu");
        try { dao.layKetQuaDoiChieu("SC-2026-0111"); throw new AssertionError("Phiếu đã thu"); }
        catch (IllegalArgumentException expected) { dem++; }
        try { hoaDon.xacNhanThanhToanChuyenKhoan(tk, "SC-2026-0111", 800000, "GD-1"); throw new AssertionError("Thu lặp"); }
        catch (IllegalStateException expected) { dem++; }
        List<DongChiPhi> dong = new ArrayList<>();
        dong.add(new DongChiPhi("Dòng 1", "1 lần", Long.MAX_VALUE, Long.MAX_VALUE));
        dong.add(new DongChiPhi("Dòng 2", "1 lần", 1, 1));
        try { new KetQuaDoiChieu("SC", "KH", "XE", "Chưa thu", dong); throw new AssertionError("Tràn tổng"); }
        catch (IllegalArgumentException expected) { dem++; }
        tk.ketThucPhien();
        try { dao.layDanhSachMaPhieu(); throw new AssertionError("Phiên hết hạn"); }
        catch (SecurityException expected) { dem++; }
        System.out.println("PASS DoiChieuChiPhiDAOTest: " + dem);
    }
    private static void dung(boolean dk, String loi) {
        if (!dk) { throw new AssertionError(loi); }
        dem++;
    }
}
