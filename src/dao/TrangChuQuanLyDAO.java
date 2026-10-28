package dao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import entity.HoaDonDoanhThu;
import entity.HoaDonDoanhThu.HinhThuc;
import entity.MucTongQuanQuanLy;
import entity.MucTongQuanQuanLy.Nhom;
import entity.TaiKhoan;
import entity.TongQuanQuanLy;
import entity.VaiTro;

/** Nguồn dữ liệu tạo sẵn, chỉ đọc của tổng quan Quản lý; chưa kết nối hoặc cập nhật database. */
public final class TrangChuQuanLyDAO {

    /** Snapshot dùng chung giữa chỉ số, biểu đồ và dialog trong một phiên. */
    private final TongQuanQuanLy tongQuan;

    /** Tạo bộ dữ liệu nhất quán của kỳ 21–27/10/2026 theo Page 3. */
    public TrangChuQuanLyDAO() {
        tongQuan = new TongQuanQuanLy(LocalDateTime.of(2026, 10, 27, 15, 30),
                taoCacMuc(), taoHoaDon());
    }

    /**
     * Đọc toàn bộ tổng quan sau khi kiểm tra quyền.
     * @param taiKhoan tài khoản đã xác thực
     * @return snapshot bất biến
     * @throws SecurityException nếu không phải Quản lý
     */
    public TongQuanQuanLy docTongQuan(TaiKhoan taiKhoan) {
        kiemTraQuyen(taiKhoan);
        return tongQuan;
    }

    /**
     * Đọc chi tiết ngày từ cùng nguồn với biểu đồ; không cấp quyền chỉ bằng cách ẩn nút.
     * @param taiKhoan tài khoản của phiên hiện tại
     * @param ngay ngày cần mở trong kỳ
     * @return các hóa đơn đã thanh toán trong ngày
     * @throws IllegalArgumentException nếu ngày nằm ngoài kỳ
     * @throws SecurityException nếu không có quyền
     */
    public List<HoaDonDoanhThu> docHoaDonTheoNgay(TaiKhoan taiKhoan, LocalDate ngay) {
        kiemTraQuyen(taiKhoan);
        Objects.requireNonNull(ngay, "Cần ngày đối chiếu.");
        if (ngay.isBefore(tongQuan.getTuNgay()) || ngay.isAfter(tongQuan.getDenNgay())) {
            throw new IllegalArgumentException("Ngày đối chiếu nằm ngoài kỳ đang xem.");
        }
        return tongQuan.getHoaDonTheoNgay(ngay);
    }

    /** @param taiKhoan danh tính được cấp quyền, không kiểm tra theo tên đăng nhập cố định */
    private void kiemTraQuyen(TaiKhoan taiKhoan) {
        Objects.requireNonNull(taiKhoan, "Cần tài khoản đã đăng nhập.");
        if (taiKhoan.getVaiTro() != VaiTro.QUAN_LY) {
            throw new SecurityException("Chỉ Quản lý được xem tổng quan doanh thu.");
        }
    }

    /** @return các hồ sơ nguồn để tính số lượng, tránh ghi số cố định trong UI */
    private List<MucTongQuanQuanLy> taoCacMuc() {
        List<MucTongQuanQuanLy> cacMuc = new ArrayList<>();
        String[] maLenh = {"0118", "0121", "0122", "0123", "0124", "0125", "0126", "0127"};
        String[] xe = {"51F-234.56", "51G-678.90", "59A-456.88", "51D-220.18",
                "51L-338.26", "51B-112.44", "51C-992.10", "51E-681.52"};
        for (int viTri = 0; viTri < maLenh.length; viTri++) {
            LocalDateTime han = LocalDateTime.of(2026, 10, viTri < 3 ? 27 : 28, 17, viTri == 1 ? 30 : 0);
            cacMuc.add(new MucTongQuanQuanLy(Nhom.LENH_DANG_XU_LY, "SC-2026-" + maLenh[viTri],
                    xe[viTri] + (viTri == 1 ? " · thiếu má phanh trước" : " · đang thực hiện hạng mục đã duyệt"),
                    viTri == 1 ? "Chờ phụ tùng" : "Đang sửa", han, viTri == 1));
        }
        cacMuc.add(new MucTongQuanQuanLy(Nhom.BAO_GIA_CHO_KHACH, "BG-2026-0108",
                "Nguyễn Văn An · 51H-246.80 · đã gửi 14:10", "Chờ khách", null, true));
        cacMuc.add(new MucTongQuanQuanLy(Nhom.BAO_GIA_CHO_KHACH, "BG-2026-0109",
                "Phạm Quốc Huy · 51R-887.19 · cần liên hệ lại", "Chờ khách", null, true));
        cacMuc.add(new MucTongQuanQuanLy(Nhom.BAO_GIA_CHO_KHACH, "BG-2026-0110",
                "Trần Thị Bình · 51A-225.68", "Chờ khách", null, false));
        cacMuc.add(new MucTongQuanQuanLy(Nhom.BAO_GIA_CHO_KHACH, "BG-2026-0111",
                "Lê Văn Cường · 51M-393.82", "Chờ khách", null, false));
        cacMuc.add(new MucTongQuanQuanLy(Nhom.XE_CHO_BAN_GIAO, "GX-2026-0104",
                "Trần Thị Bình · 51K-123.45 · Thu ngân đã xác nhận thanh toán", "Chờ bàn giao", null, true));
        cacMuc.add(new MucTongQuanQuanLy(Nhom.XE_CHO_BAN_GIAO, "GX-2026-0105",
                "Lê Văn Cường · 51G-776.12 · Thu ngân đã xác nhận thanh toán", "Chờ bàn giao", null, true));
        cacMuc.add(new MucTongQuanQuanLy(Nhom.PHU_TUNG_CANH_BAO, "PT002",
                "Lọc dầu động cơ · tồn 2 cái / tối thiểu 3", "Sắp hết", null, true));
        cacMuc.add(new MucTongQuanQuanLy(Nhom.PHU_TUNG_CANH_BAO, "PT003",
                "Má phanh trước · tồn 1 bộ / tối thiểu 2", "Sắp hết", null, true));
        return cacMuc;
    }

    /** @return chín hóa đơn, tổng 5.940.000đ; tiền mặt 3.370.000đ, chuyển khoản 2.570.000đ */
    private List<HoaDonDoanhThu> taoHoaDon() {
        return List.of(
                hoaDon("0201", 21, 9, 15, "Nguyễn Văn An", "51H-246.80", HinhThuc.TIEN_MAT, "450000"),
                hoaDon("0202", 22, 10, 20, "Trần Thị Bình", "51G-678.90", HinhThuc.TIEN_MAT, "660000"),
                hoaDon("0203", 23, 15, 0, "Lê Văn Cường", "51F-234.56", HinhThuc.TIEN_MAT, "550000"),
                hoaDon("0204", 24, 14, 10, "Phạm Quốc Huy", "59A-456.88", HinhThuc.TIEN_MAT, "920000"),
                hoaDon("0205", 25, 9, 30, "Nguyễn Văn An", "51H-246.80", HinhThuc.TIEN_MAT, "790000"),
                hoaDon("0206", 25, 11, 0, "Phạm Quốc Huy", "59A-456.88", HinhThuc.CHUYEN_KHOAN, "20000"),
                hoaDon("0207", 26, 16, 0, "Lê Văn Cường", "51F-234.56", HinhThuc.CHUYEN_KHOAN, "1100000"),
                hoaDon("0208", 27, 10, 30, "Trần Thị Bình", "51K-123.45", HinhThuc.CHUYEN_KHOAN, "1270000"),
                hoaDon("0209", 27, 14, 30, "Lê Văn Cường", "51G-776.12", HinhThuc.CHUYEN_KHOAN, "180000"));
    }

    /**
     * Ghép một chứng từ nguồn của doanh thu, không phải thao tác phát hành hóa đơn.
     * @param soMa hậu tố mã chứng từ
     * @param ngay ngày thanh toán trong tháng 10/2026
     * @param gio giờ thanh toán
     * @param phut phút thanh toán
     * @param khachHang tên khách
     * @param bienSo xe liên quan
     * @param hinhThuc cách thu tiền
     * @param soTien tiền VND ở dạng chuỗi để không mất chính xác
     * @return dữ liệu hóa đơn đã thanh toán
     */
    private HoaDonDoanhThu hoaDon(String soMa, int ngay, int gio, int phut, String khachHang,
            String bienSo, HinhThuc hinhThuc, String soTien) {
        return new HoaDonDoanhThu("HD-2026-" + soMa, "SC-2026-" + soMa, khachHang, bienSo,
                LocalDateTime.of(2026, 10, ngay, gio, phut), hinhThuc, new BigDecimal(soTien));
    }
}
