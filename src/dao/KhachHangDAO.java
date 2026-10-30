package dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import entity.KhachHang;
import entity.TaiKhoan;
import entity.VaiTro;

/** Kho hồ sơ trong bộ nhớ, dùng một instance trong mỗi phiên; chưa lưu lâu dài. */
public final class KhachHangDAO {
    private final List<KhachHang> danhSach = new ArrayList<>();
    private int soThuTu = 1;

    /** @return kho mẫu độc lập để duyệt UI, không dùng dữ liệu khách thật */
    public static KhachHangDAO taoDuLieuMau() {
        KhachHangDAO dao = new KhachHangDAO();
        dao.danhSach.add(new KhachHang("KH0001", "Nguyễn Văn An", "0901234567", "TP.HCM", "", ""));
        dao.danhSach.add(new KhachHang("KH0002", "Trần Thị Bình", "0908221459", "TP.HCM", "", ""));
        dao.danhSach.add(new KhachHang("KH0003", "Lê Văn Cường", "0937555889", "TP.HCM", "", ""));
        dao.soThuTu = 4;
        return dao;
    }

    /** Tìm theo tên, mã hoặc SĐT; trả bản sao để UI không sửa kho ngoài thao tác lưu. */
    public List<KhachHang> timKiem(TaiKhoan taiKhoan, String tuKhoa) {
        kiemTraQuyen(taiKhoan);
        String tu = Objects.requireNonNull(tuKhoa, "Cần từ khóa.").strip().toLowerCase(Locale.ROOT);
        List<KhachHang> ketQua = new ArrayList<>();
        for (KhachHang kh : danhSach) {
            if (kh.getMaKH().toLowerCase(Locale.ROOT).contains(tu)
                    || kh.getHoTen().toLowerCase(Locale.ROOT).contains(tu) || kh.getSoDienThoai().contains(tu)) {
                ketQua.add(new KhachHang(kh));
            }
        }
        return ketQua;
    }

    /** @throws IllegalArgumentException nếu hồ sơ không còn tồn tại */
    public KhachHang timTheoMa(TaiKhoan taiKhoan, String ma) {
        kiemTraQuyen(taiKhoan);
        return new KhachHang(danhSach.get(viTri(ma)));
    }

    /** Tra cứu trùng để người dùng đối chiếu; SĐT chưa được quy định là khóa duy nhất. */
    public List<KhachHang> timTrungSoDienThoai(TaiKhoan taiKhoan, String so, String maLoaiTru) {
        kiemTraQuyen(taiKhoan);
        List<KhachHang> ketQua = new ArrayList<>();
        for (KhachHang kh : danhSach) {
            if (kh.getSoDienThoai().equals(so.strip()) && !kh.getMaKH().equals(maLoaiTru)) {
                ketQua.add(new KhachHang(kh));
            }
        }
        return ketQua;
    }

    /** Validate trước khi ghi; null mã là thêm mới, mã có sẵn là cập nhật đúng hồ sơ. */
    public KhachHang luu(TaiKhoan taiKhoan, String ma, String ten, String so, String diaChi,
            String email, String ghiChu) {
        kiemTraQuyen(taiKhoan);
        int viTri = ma == null ? -1 : viTri(ma);
        String maLuu = ma == null ? String.format(Locale.ROOT, "KH%04d", soThuTu) : ma;
        KhachHang kh = new KhachHang(maLuu, ten, so, diaChi, email, ghiChu);
        if (ma == null) {
            danhSach.add(kh);
            soThuTu++;
        } else {
            danhSach.set(viTri, kh);
        }
        return new KhachHang(kh);
    }

    private int viTri(String ma) {
        for (int i = 0; i < danhSach.size(); i++) {
            if (danhSach.get(i).getMaKH().equals(ma)) return i;
        }
        throw new IllegalArgumentException("Hồ sơ khách hàng không tồn tại.");
    }

    private void kiemTraQuyen(TaiKhoan taiKhoan) {
        if (Objects.requireNonNull(taiKhoan, "Cần phiên đăng nhập.").getVaiTro() != VaiTro.NHAN_VIEN_TIEP_NHAN) {
            throw new SecurityException("Chỉ nhân viên tiếp nhận được quản lý hồ sơ khách hàng.");
        }
    }
}
