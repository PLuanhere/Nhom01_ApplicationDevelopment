package dao;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import entity.BoPhanNhanVien;
import entity.NhanVien;
import entity.TaiKhoan;
import entity.TrangThaiNhanVien;
import entity.VaiTro;

/** Kiểm tra nghiệp vụ hồ sơ, quyền, tìm/lọc và lưu dữ liệu trong cùng tiến trình. */
public final class NhanVienDAOTest {

    private static final TaiKhoan QUAN_LY = new TaiKhoan("quanly-khac", VaiTro.QUAN_LY);
    private static final NhanVienDAO DAO = new NhanVienDAO();

    /** Chỉ chứa điểm chạy kiểm thử. */
    private NhanVienDAOTest() { }

    /** @param args không sử dụng */
    public static void main(String[] args) {
        kiemTraDuLieuBanDau();
        kiemTraPhanQuyen();
        kiemTraRangBuoc();
        kiemTraThemSua();
        System.out.println("PASS: hồ sơ, quyền, tìm/lọc, tuổi/điện thoại/kinh nghiệm, thêm/sửa, "
                + "mã tự sinh, từ chối mã trùng và dữ liệu dùng chung trong tiến trình.");
    }

    /** Kiểm tra nguồn bất biến và tìm kiếm kết hợp, có dấu/không dấu và điện thoại có khoảng cách. */
    private static void kiemTraDuLieuBanDau() {
        List<NhanVien> danhSach = DAO.layDanhSach(QUAN_LY);
        kiemTra(danhSach.size() == 4, "Thiếu bốn hồ sơ ban đầu.");
        Set<String> ma = new HashSet<>();
        for (NhanVien nv : danhSach) {
            kiemTra(ma.add(nv.getMaNhanVien()), "Trùng mã.");
            kiemTra(nv.getNgaySinh() != null && !nv.getDiaChi().isBlank(), "Thiếu dữ liệu hồ sơ.");
        }
        canLoi(UnsupportedOperationException.class, danhSach::clear);
        kiemTra(DAO.timKiem(QUAN_LY, "TRAN MINH HUNG", null).size() == 1, "Tìm tên không dấu sai.");
        kiemTra(DAO.timKiem(QUAN_LY, "  nv003  ", null).size() == 1, "Tìm mã sai.");
        kiemTra(DAO.timKiem(QUAN_LY, "0909 345 211", null).size() == 1, "Tìm điện thoại sai.");
        kiemTra(DAO.timKiem(QUAN_LY, "...", null).isEmpty(), "Ký tự phân cách không được khớp tất cả.");
        kiemTra(DAO.timKiem(QUAN_LY, "", TrangThaiNhanVien.DA_NGHI).isEmpty(), "Lọc sai trạng thái.");
        canLoi(IllegalArgumentException.class, () -> DAO.timTheoMa(QUAN_LY, "khong-co"));
    }

    /** Mọi thao tác đọc/ghi đều phải kiểm tra vai trò, không dựa vào tên đăng nhập. */
    private static void kiemTraPhanQuyen() {
        NhanVien cu = DAO.timTheoMa(QUAN_LY, "NV001");
        for (VaiTro vaiTro : VaiTro.values()) {
            if (vaiTro == VaiTro.QUAN_LY) {
                continue;
            }
            TaiKhoan khongCoQuyen = new TaiKhoan("quanly", vaiTro);
            canLoi(SecurityException.class, () -> DAO.layDanhSach(khongCoQuyen));
            canLoi(SecurityException.class, () -> DAO.timKiem(khongCoQuyen, "", null));
            canLoi(SecurityException.class, () -> DAO.timTheoMa(khongCoQuyen, "NV001"));
            canLoi(SecurityException.class, () -> DAO.maTiepTheo(khongCoQuyen));
            canLoi(SecurityException.class, () -> DAO.them(khongCoQuyen, cu));
            canLoi(SecurityException.class, () -> DAO.capNhat(khongCoQuyen, cu));
        }
        canLoi(NullPointerException.class, () -> DAO.layDanhSach(null));
        kiemTra(DAO.layDanhSach(QUAN_LY).size() == 4, "Thao tác bị từ chối vẫn thay đổi nguồn.");
    }

    /** Kiểm tra đúng ranh giới 18 tuổi, ngày tương lai, chuỗi số và kinh nghiệm. */
    private static void kiemTraRangBuoc() {
        LocalDate du18 = LocalDate.now().minusYears(18);
        taoHoSo("NV008", "Tên hợp lệ", du18, "0901234567", 0);
        taoHoSo("NV008", "Tên hợp lệ", du18, "09012345678", 0);
        canLoi(IllegalArgumentException.class, () -> taoHoSo("NV008", " ", du18, "0901234567", 0));
        canLoi(IllegalArgumentException.class,
                () -> taoHoSo("NV008", "Tên", du18.plusDays(1), "0901234567", 0));
        canLoi(IllegalArgumentException.class,
                () -> taoHoSo("NV008", "Tên", LocalDate.now().plusDays(1), "0901234567", 0));
        canLoi(IllegalArgumentException.class, () -> taoHoSo("NV008", "Tên", null, "0901234567", 0));
        for (String so : List.of("090123456", "090123456789", "090a234567", "0901 234567", "")) {
            canLoi(IllegalArgumentException.class, () -> taoHoSo("NV008", "Tên", du18, so, 0));
        }
        canLoi(IllegalArgumentException.class, () -> taoHoSo("NV008", "Tên", du18, "0901234567", -1));
        String ma = DAO.maTiepTheo(QUAN_LY);
        taoHoSo(ma, "Chưa lưu", du18, "0901234567", 0);
        kiemTra(DAO.maTiepTheo(QUAN_LY).equals(ma), "Tạo hồ sơ chưa lưu đã chiếm mã.");
    }

    /** Thêm, đổi bộ phận/trạng thái, đọc lại; không thêm trùng mã hoặc sửa bản ghi không tồn tại. */
    private static void kiemTraThemSua() {
        String ma = DAO.maTiepTheo(QUAN_LY);
        NhanVien nhap = taoHoSo(ma, "Nguyễn Thị An", LocalDate.of(1995, 2, 3), "0901234567", 2);
        NhanVien daThem = DAO.them(QUAN_LY, nhap);
        kiemTra(daThem.getMaNhanVien().equals("NV008"), "Mã thêm đầu tiên sai.");
        NhanVien daSua = new NhanVien(ma, "Nguyễn Thị An sửa", nhap.getNgaySinh(), "0912345678", 3,
                "Nữ", "Địa chỉ mới", BoPhanNhanVien.THU_NGAN, TrangThaiNhanVien.DA_NGHI);
        DAO.capNhat(QUAN_LY, daSua);
        NhanVien docLai = new NhanVienDAO().timTheoMa(QUAN_LY, ma);
        kiemTra(docLai == daSua && DAO.layDanhSach(QUAN_LY).size() == 5, "Sửa không thay đúng hồ sơ.");
        kiemTra(docLai.getBoPhan() == BoPhanNhanVien.THU_NGAN, "Không lưu bộ phận.");
        kiemTra(DAO.timKiem(QUAN_LY, "thi an", TrangThaiNhanVien.DA_NGHI).size() == 1, "Lọc kết hợp sai.");
        kiemTra(DAO.timKiem(QUAN_LY, "thi an", TrangThaiNhanVien.DANG_LAM).isEmpty(), "Lọc sai trạng thái.");
        canLoi(IllegalArgumentException.class, () -> DAO.them(QUAN_LY, nhap));
        NhanVien khongCo = taoHoSo("NV999", "Không có", nhap.getNgaySinh(), "0901234567", 0);
        canLoi(IllegalArgumentException.class, () -> DAO.capNhat(QUAN_LY, khongCo));
        kiemTra(DAO.timTheoMa(QUAN_LY, ma) == daSua, "Lỗi cập nhật làm đổi dữ liệu.");
        kiemTra(DAO.layDanhSach(QUAN_LY).size() == 5, "Lỗi lưu làm đổi số hồ sơ.");
    }

    /**
     * @param ma mã
     * @param ten tên
     * @param ngay ngày sinh
     * @param so điện thoại
     * @param nam kinh nghiệm
     * @return hồ sơ hợp lệ hoặc phát sinh lỗi ràng buộc
     */
    private static NhanVien taoHoSo(String ma, String ten, LocalDate ngay, String so, int nam) {
        return new NhanVien(ma, ten, ngay, so, nam, "Nữ", "", BoPhanNhanVien.KY_THUAT, TrangThaiNhanVien.DANG_LAM);
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
