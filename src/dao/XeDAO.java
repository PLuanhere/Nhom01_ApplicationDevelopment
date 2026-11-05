package dao;

import entity.KhachHang;
import entity.Xe;
import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Kho xe trong phiên, dùng đúng kho khách hàng được khung chính truyền vào. */
public final class XeDAO {
    private final KhachHangDAO khachHangDAO;
    private final List<Xe> danhSach = new ArrayList<>();
    private int soThuTu = 1;

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public XeDAO(KhachHangDAO khachHangDAO) {
        if (khachHangDAO == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.khachHangDAO = khachHangDAO;
    }

    /** Nạp fixture qua cùng kiểm tra nghiệp vụ; chỉ gọi khi khởi tạo phiên mẫu. */
    public static XeDAO taoDuLieuMau(KhachHangDAO khachHangDAO, TaiKhoan taiKhoan) {
        XeDAO dao = new XeDAO(khachHangDAO);
        dao.luu(taiKhoan, null, "51H-246.80", "KH0001", "Toyota", "Camry 2.0Q", 2022, "Đen", "", "");
        dao.luu(taiKhoan, null, "51G-678.90", "KH0002", "Kia", "Seltos", 2021, "Trắng", "", "");
        dao.luu(taiKhoan, null, "51F-234.56", "KH0003", "Mazda", "CX-5", 2020, "Đỏ", "", "");
        return dao;
    }

    /** Tìm một phần biển số, tên hoặc mã chủ xe; không dùng biểu thức regex từ người dùng. */
    public List<Xe> timKiem(TaiKhoan taiKhoan, String tuKhoa) {
        kiemTraQuyen(taiKhoan);
        if (tuKhoa == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        String tu = tuKhoa.trim().toLowerCase();
        String bien = Xe.khoaBienSo(tu);
        List<Xe> ketQua = new ArrayList<>();
        for (Xe xe : danhSach) {
            KhachHang chu = khachHangDAO.timTheoMa(taiKhoan, xe.getMaKH());
            if (tu.isEmpty() || (!bien.isEmpty() && Xe.chuanHoaBienSo(xe.getBienSo()).contains(bien))
                    || chu.getHoTen().toLowerCase().contains(tu) || chu.getMaKH().toLowerCase().contains(tu)) {
                ketQua.add(xe);
            }
        }
        return ketQua;
    }

    /** Thực hiện tim theo ma cho thành phần hiện tại. */
    public Xe timTheoMa(TaiKhoan taiKhoan, String ma) {
        kiemTraQuyen(taiKhoan);
        return danhSach.get(viTri(ma));
    }

    /** Đếm tại nguồn xe, tránh lưu số lượng dẫn xuất vào hồ sơ khách. */
    public int demTheoKhach(TaiKhoan taiKhoan, String maKH) {
        kiemTraQuyen(taiKhoan);
        khachHangDAO.timTheoMa(taiKhoan, maKH);
        int dem = 0;
        for (Xe xe : danhSach) {
            if (xe.getMaKH().equals(maKH)) {
                dem++;
            }
        }
        return dem;
    }

    /** Lưu sau khi kiểm tra hết điều kiện; sửa thông thường không chuyển chủ xe. */
    public Xe luu(TaiKhoan taiKhoan, String maXe, String bienSo, String maKH, String hang, String dong, int nam,
            String mau, String soKhung, String soMay) {
        kiemTraQuyen(taiKhoan);
        khachHangDAO.timTheoMa(taiKhoan, maKH);
        int viTri = maXe == null ? -1 : viTri(maXe);
        if (maXe != null && !danhSach.get(viTri).getMaKH().equals(maKH)) {
            throw new IllegalArgumentException("Không đổi chủ xe trong thao tác sửa hồ sơ thông thường.");
        }
        String ma = maXe == null ? String.format(Locale.ROOT, "XE-%04d", soThuTu) : maXe;
        Xe moi = new Xe(ma, bienSo, maKH, hang, dong, nam, mau, soKhung, soMay);
        for (Xe cu : danhSach) {
            if (!cu.getMaXe().equals(ma)
                    && Xe.chuanHoaBienSo(cu.getBienSo()).equals(Xe.chuanHoaBienSo(moi.getBienSo()))) {
                throw new IllegalArgumentException(
                        "Biển số đã tồn tại ở " + cu.getMaXe() + ". Hãy chọn hồ sơ hiện có.");
            }
        }
        if (maXe == null) {
            danhSach.add(moi);
            soThuTu++;
        } else {
            danhSach.set(viTri, moi);
        }
        return moi;
    }

    /** Thực hiện vi tri cho thành phần hiện tại. */
    private int viTri(String ma) {
        for (int i = 0; i < danhSach.size(); i++) {
            if (danhSach.get(i).getMaXe().equals(ma)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Hồ sơ xe không tồn tại.");
    }

    /** Kiểm tra quyen trước khi xử lý. */
    private void kiemTraQuyen(TaiKhoan taiKhoan) {
        if (taiKhoan == null) {
            throw new NullPointerException("Cần phiên đăng nhập.");
        }
        taiKhoan.kiemTraPhien();
        if (taiKhoan.getVaiTro() != VaiTro.NHAN_VIEN_TIEP_NHAN) {
            throw new SecurityException("Chỉ nhân viên tiếp nhận được quản lý hồ sơ xe.");
        }
    }
}
