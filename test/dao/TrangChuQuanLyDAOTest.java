package dao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import entity.HoaDonDoanhThu;
import entity.HoaDonDoanhThu.HinhThuc;
import entity.MucTongQuanQuanLy.Nhom;
import entity.TaiKhoan;
import entity.TongQuanQuanLy;
import entity.VaiTro;

/** Kiểm tra quyền, số liệu và đối chiếu doanh thu bảy ngày; chạy độc lập bằng JDK 17. */
public final class TrangChuQuanLyDAOTest {

    /** Tài khoản Quản lý bất kỳ, không phụ thuộc tên đăng nhập trong dữ liệu có sẵn. */
    private static final TaiKhoan QUAN_LY = new TaiKhoan("quanly-khac", VaiTro.QUAN_LY);

    /** Lớp chỉ chứa điểm chạy kiểm thử. */
    private TrangChuQuanLyDAOTest() {
    }

    /** @param args không sử dụng */
    public static void main(String[] args) {
        TrangChuQuanLyDAO dao = new TrangChuQuanLyDAO();
        kiemTraQuyen(dao);
        kiemTraSoLieu(dao);
        kiemTraBienNgay();
        System.out.println("PASS: quyền Quản lý; bốn chỉ số; đối chiếu 7 ngày/9 hóa đơn; "
                + "dữ liệu rỗng, biên ngày và chống cộng trùng.");
    }

    /** @param dao nguồn tổng quan chỉ cấp quyền cho Quản lý */
    private static void kiemTraQuyen(TrangChuQuanLyDAO dao) {
        for (VaiTro vaiTro : VaiTro.values()) {
            if (vaiTro != VaiTro.QUAN_LY) {
                TaiKhoan nguoiDung = new TaiKhoan("tai-khoan", vaiTro);
                canLoi(SecurityException.class, () -> dao.docTongQuan(nguoiDung));
                canLoi(SecurityException.class,
                        () -> dao.docHoaDonTheoNgay(nguoiDung, LocalDate.of(2026, 10, 27)));
            }
        }
        canLoi(NullPointerException.class, () -> dao.docTongQuan(null));
        canLoi(NullPointerException.class, () -> dao.docHoaDonTheoNgay(QUAN_LY, null));
        canLoi(IllegalArgumentException.class,
                () -> dao.docHoaDonTheoNgay(QUAN_LY, LocalDate.of(2026, 10, 20)));
        canLoi(IllegalArgumentException.class,
                () -> dao.docHoaDonTheoNgay(QUAN_LY, LocalDate.of(2026, 10, 28)));
    }

    /** @param dao nguồn chung của tổng tiền, các cột và danh sách trong dialog */
    private static void kiemTraSoLieu(TrangChuQuanLyDAO dao) {
        TongQuanQuanLy tongQuan = dao.docTongQuan(QUAN_LY);
        kiemTra(tongQuan.getCacMuc(Nhom.LENH_DANG_XU_LY).size() == 8, "Sai số lệnh.");
        kiemTra(tongQuan.getCacMuc(Nhom.BAO_GIA_CHO_KHACH).size() == 4, "Sai số báo giá.");
        kiemTra(tongQuan.getCacMuc(Nhom.XE_CHO_BAN_GIAO).size() == 2, "Sai số xe chờ giao.");
        kiemTra(tongQuan.getCacMuc(Nhom.PHU_TUNG_CANH_BAO).size() == 2, "Sai số phụ tùng.");
        kiemTra(tongQuan.getLenhDenHanTrongNgay().size() == 3, "Sai số lệnh đến hạn.");
        kiemTra(tongQuan.getLenhDenHanTrongNgay().stream().allMatch(muc ->
                muc.getHanXuLy().toLocalDate().equals(tongQuan.getDenNgay())), "Lẫn hạn ngày khác.");

        // Cộng độc lập từ hóa đơn để đối chiếu từng cột và toàn kỳ.
        String[] tienTungNgay = {"450000", "660000", "550000", "920000", "810000", "1100000", "1450000"};
        List<HoaDonDoanhThu> toanKy = new ArrayList<>();
        BigDecimal tongCacCot = BigDecimal.ZERO;
        for (int viTri = 0; viTri < tienTungNgay.length; viTri++) {
            LocalDate ngay = tongQuan.getTuNgay().plusDays(viTri);
            List<HoaDonDoanhThu> chiTiet = dao.docHoaDonTheoNgay(QUAN_LY, ngay);
            kiemTra(chiTiet.stream().allMatch(muc -> muc.getNgayThanhToan().toLocalDate().equals(ngay)),
                    "Chi tiết lẫn hóa đơn của ngày khác.");
            BigDecimal tongChiTiet = chiTiet.stream().map(HoaDonDoanhThu::getSoTien)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            bangTien(tongChiTiet, tienTungNgay[viTri]);
            kiemTra(tongChiTiet.compareTo(tongQuan.tinhDoanhThuNgay(ngay)) == 0, "Cột lệch chi tiết.");
            tongCacCot = tongCacCot.add(tongChiTiet);
            toanKy.addAll(chiTiet);
            canLoi(UnsupportedOperationException.class, chiTiet::clear);
        }
        kiemTra(toanKy.size() == 9, "Sai số hóa đơn.");
        bangTien(tongCacCot, "5940000");
        bangTien(tongQuan.tinhDoanhThuTrongKy(), "5940000");
        bangTien(toanKy.stream().filter(muc -> muc.getHinhThuc() == HinhThuc.TIEN_MAT)
                .map(HoaDonDoanhThu::getSoTien).reduce(BigDecimal.ZERO, BigDecimal::add), "3370000");
        bangTien(toanKy.stream().filter(muc -> muc.getHinhThuc() == HinhThuc.CHUYEN_KHOAN)
                .map(HoaDonDoanhThu::getSoTien).reduce(BigDecimal.ZERO, BigDecimal::add), "2570000");
        canLoi(UnsupportedOperationException.class, () -> tongQuan.getCacMuc(Nhom.LENH_DANG_XU_LY).clear());
    }

    /** Kiểm tra ngày rỗng, ranh giới nửa đêm, chứng từ ngoài kỳ và mã trùng. */
    private static void kiemTraBienNgay() {
        LocalDateTime chot = LocalDateTime.of(2026, 10, 27, 23, 59, 59);
        TongQuanQuanLy rong = new TongQuanQuanLy(chot, List.of(), List.of());
        bangTien(rong.tinhDoanhThuTrongKy(), "0");
        kiemTra(rong.getLenhDenHanTrongNgay().isEmpty(), "Dữ liệu rỗng phải có danh sách rỗng.");

        List<HoaDonDoanhThu> nguon = new ArrayList<>(List.of(
                hoaDon("A", "2026-10-21T00:00", "10.25"),
                hoaDon("B", "2026-10-21T23:59:59", "20.50"),
                hoaDon("C", "2026-10-22T00:00", "40.75"),
                hoaDon("D", "2026-10-27T23:59:59", "80"),
                hoaDon("NGOAI-KY", "2026-10-20T23:59:59", "1000")));
        TongQuanQuanLy tongQuan = new TongQuanQuanLy(chot, List.of(), nguon);
        nguon.clear();
        bangTien(tongQuan.tinhDoanhThuNgay(LocalDate.of(2026, 10, 21)), "30.75");
        bangTien(tongQuan.tinhDoanhThuNgay(LocalDate.of(2026, 10, 23)), "0");
        bangTien(tongQuan.tinhDoanhThuTrongKy(), "151.50");
        HoaDonDoanhThu trung = hoaDon("TRUNG", "2026-10-27T10:00", "10");
        canLoi(IllegalArgumentException.class,
                () -> new TongQuanQuanLy(chot, List.of(), List.of(trung, trung)));
        canLoi(IllegalArgumentException.class, () -> new TongQuanQuanLy(chot, List.of(),
                List.of(hoaDon("SAU-CHOT", "2026-10-28T00:00", "10"))));
        canLoi(IllegalArgumentException.class, () -> hoaDon("AM", "2026-10-27T10:00", "-1"));
    }

    /**
     * @param ma mã riêng của chứng từ
     * @param ngay thời điểm thanh toán ISO
     * @param tien số tiền chính xác
     * @return chứng từ nhỏ để kiểm tra biên ngày và tiền lẻ
     */
    private static HoaDonDoanhThu hoaDon(String ma, String ngay, String tien) {
        return new HoaDonDoanhThu(ma, "SC-" + ma, "Khách", "51A-123.45",
                LocalDateTime.parse(ngay), HinhThuc.TIEN_MAT, new BigDecimal(tien));
    }

    /**
     * @param tien thực tế
     * @param mongDoi giá trị cần bằng, không phụ thuộc scale
     */
    private static void bangTien(BigDecimal tien, String mongDoi) {
        kiemTra(tien.compareTo(new BigDecimal(mongDoi)) == 0, "Sai tiền: " + tien + " / " + mongDoi);
    }

    /**
     * @param dung điều kiện cần đúng
     * @param loi thông tin khi thất bại
     */
    private static void kiemTra(boolean dung, String loi) {
        if (!dung) {
            throw new AssertionError(loi);
        }
    }

    /**
     * @param loai lỗi mong đợi
     * @param thaoTac thao tác phải bị từ chối
     */
    private static void canLoi(Class<? extends Throwable> loai, Runnable thaoTac) {
        try {
            thaoTac.run();
        } catch (Throwable loi) {
            if (loai.isInstance(loi)) {
                return;
            }
            throw new AssertionError("Sai loại lỗi.", loi);
        }
        throw new AssertionError("Thiếu lỗi " + loai.getSimpleName());
    }
}
