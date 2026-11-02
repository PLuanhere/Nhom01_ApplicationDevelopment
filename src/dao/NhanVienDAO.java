package dao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import entity.BoPhanNhanVien;
import entity.NhanVien;
import entity.KyThuatVienPhanCong;
import entity.TaiKhoan;
import entity.TrangThaiNhanVien;
import entity.VaiTro;

/** Quản lý dữ liệu nhân viên, tìm kiếm, thêm và cập nhật hồ sơ. */
public final class NhanVienDAO {

    private static final List<NhanVien> DANH_SACH = taoDuLieuBanDau();

    /** Lấy hồ sơ nhân viên và tài khoản đủ điều kiện để phân công kỹ thuật. */
    List<KyThuatVienPhanCong> layLienKetKyThuat(TaiKhoanDAO taiKhoanDAO) {
        List<KyThuatVienPhanCong> ketQua = new ArrayList<>();
        for (TaiKhoan tk : taiKhoanDAO.layTaiKhoanNoiBo()) {
            for (NhanVien nv : DANH_SACH) {
                if (nv.getMaNhanVien().equals(tk.getMaNhanVien())) {
                    KyThuatVienPhanCong lienKet = new KyThuatVienPhanCong(nv, tk);
                    if (lienKet.duocPhanCong()) ketQua.add(lienKet);
                }
            }
        }
        return ketQua;
    }

    /** Đọc danh sách nhân viên sau khi kiểm tra quyền truy cập. */
    public List<NhanVien> layDanhSach(TaiKhoan taiKhoan) {
        kiemTraQuyen(taiKhoan);
        return new ArrayList<>(DANH_SACH);
    }

    /** Tìm và lọc nhân viên theo các điều kiện được truyền vào. */
    public List<NhanVien> timKiem(TaiKhoan taiKhoan, String tuKhoa, TrangThaiNhanVien trangThai) {
        String chu = boDau(tuKhoa == null ? "" : tuKhoa.trim());
        String so = "";
        for (int i = 0; i < chu.length(); i++) {
            char c = chu.charAt(i);
            if (c != ' ' && c != '\t' && c != '(' && c != ')' && c != '.' && c != '-') { so = so + c; }
        }
        List<NhanVien> ketQua = new ArrayList<>();
        for (NhanVien nv : layDanhSach(taiKhoan)) {
            boolean dungTrangThai = trangThai == null || nv.getTrangThai() == trangThai;
            boolean dungTuKhoa = boDau(nv.getMaNhanVien()).contains(chu) || boDau(nv.getHoTen()).contains(chu)
                || (!so.isEmpty() && nv.getSoDienThoai().contains(so));
            if (dungTrangThai && dungTuKhoa) {
                ketQua.add(nv);
            }
        }
        return ketQua;
    }

    /** Tìm nhân viên theo mã; báo lỗi nếu không tồn tại. */
    public NhanVien timTheoMa(TaiKhoan taiKhoan, String maNhanVien) {
        kiemTraQuyen(taiKhoan);
        for (NhanVien nv : DANH_SACH) {
            if (nv.getMaNhanVien().equals(maNhanVien)) {
                return nv;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy nhân viên.");
    }

    /** Sinh mã nhân viên dự kiến để hiển thị, chưa lưu hoặc chiếm mã. */
    public String maTiepTheo(TaiKhoan taiKhoan) {
        kiemTraQuyen(taiKhoan);
        int soLonNhat = 0;
        for (NhanVien nv : DANH_SACH) {
            int so = Integer.parseInt(nv.getMaNhanVien().substring(2));
            soLonNhat = Math.max(soLonNhat, so);
        }
        return String.format("NV%03d", soLonNhat + 1);
    }

    /** Kiểm tra quyền và dữ liệu trước khi thêm nhân viên vào nguồn. */
    public NhanVien them(TaiKhoan taiKhoan, NhanVien nhanVien) {
        kiemTraQuyen(taiKhoan);
        if (nhanVien == null) { throw new IllegalArgumentException("Thiếu nhanVien."); }
        if (!nhanVien.getMaNhanVien().equals(maTiepTheo(taiKhoan))) {
            throw new IllegalArgumentException("Mã nhân viên đã thay đổi. Hãy mở lại form thêm.");
        }
        DANH_SACH.add(nhanVien);
        return nhanVien;
    }

    /** Kiểm tra dữ liệu rồi cập nhật nhân viên trong nguồn. */
    public void capNhat(TaiKhoan taiKhoan, NhanVien nhanVien) {
        kiemTraQuyen(taiKhoan);
        if (nhanVien == null) { throw new IllegalArgumentException("Thiếu nhanVien."); }
        for (int i = 0; i < DANH_SACH.size(); i++) {
            if (DANH_SACH.get(i).getMaNhanVien().equals(nhanVien.getMaNhanVien())) {
                DANH_SACH.set(i, nhanVien);
                return;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy nhân viên cần cập nhật.");
    }

    /** Kiểm tra tài khoản, phiên đăng nhập và quyền Quản lý trước thao tác DAO. */
    private void kiemTraQuyen(TaiKhoan taiKhoan) {
        if (taiKhoan == null) { throw new IllegalArgumentException("Thiếu taiKhoan."); }
        taiKhoan.kiemTraPhien();
        if (taiKhoan.getVaiTro() != VaiTro.QUAN_LY) {
            throw new SecurityException("Chỉ Quản lý được quản lý hồ sơ nhân viên.");
        }
    }

    /** Đổi chữ tiếng Việt thành chữ thường không dấu để tìm và sắp xếp. */
    private static String boDau(String chu) {
        String thuong = chu.toLowerCase();
        String[] cacNhom = {"àáảãạăằắẳẵặâầấẩẫậ", "èéẻẽẹêềếểễệ", "ìíỉĩị",
            "òóỏõọôồốổỗộơờớởỡợ", "ùúủũụưừứửữự", "ỳýỷỹỵ", "đ"};
        String thayThe = "aeiouyd";
        String ketQua = "";
        for (int i = 0; i < thuong.length(); i++) {
            char c = thuong.charAt(i);
            if (c >= '\u0300' && c <= '\u036F') { continue; }
            for (int j = 0; j < cacNhom.length; j++) {
                if (cacNhom[j].indexOf(c) >= 0) { c = thayThe.charAt(j); break; }
            }
            ketQua = ketQua + c;
        }
        return ketQua;
    }

    /** Tạo danh sách nhân viên ban đầu cho lần chạy thử nghiệm. */
    private static List<NhanVien> taoDuLieuBanDau() {
        List<NhanVien> danhSach = new ArrayList<>();
        danhSach.add(
            new NhanVien("NV001", "Trần Minh Hùng", LocalDate.of(1985, 3, 15), "0909345211", 12,
            "Nam", "Gò Vấp, TP. Hồ Chí Minh", BoPhanNhanVien.QUAN_LY, TrangThaiNhanVien.DANG_LAM));
        danhSach.add(new NhanVien("NV003", "Ngô Minh Hoàng", LocalDate.of(1994, 7, 20), "0901234567", 5,
            "Nam", "Bình Thạnh, TP. Hồ Chí Minh", BoPhanNhanVien.TIEP_NHAN, TrangThaiNhanVien.DANG_LAM));
        danhSach.add(new NhanVien("NV005", "Lê Minh Tâm", LocalDate.of(1996, 11, 8), "0912888456", 4,
            "Nam", "Tân Bình, TP. Hồ Chí Minh", BoPhanNhanVien.KY_THUAT, TrangThaiNhanVien.DANG_LAM));
        danhSach.add(new NhanVien("NV007", "Nguyễn Minh Vũ", LocalDate.of(1998, 1, 10), "0908678111", 3,
            "Nam", "Phú Nhuận, TP. Hồ Chí Minh", BoPhanNhanVien.THU_NGAN, TrangThaiNhanVien.DANG_LAM));
        return danhSach;
    }

    /** Tìm và lọc nhân viên theo các điều kiện rồi sắp xếp danh sách kết quả. */
    public List<NhanVien> timKiem(TaiKhoan taiKhoan, String tuKhoa, TrangThaiNhanVien trangThai, int sapXep) {
        List<NhanVien> ketQua = timKiem(taiKhoan, tuKhoa, trangThai);
        for (int i = 0; i < ketQua.size() - 1; i++) {
            for (int j = i + 1; j < ketQua.size(); j++) {
                NhanVien a = ketQua.get(i);
                NhanVien b = ketQua.get(j);
                int soSanh;
                if (sapXep == 0) { soSanh = a.getMaNhanVien().compareToIgnoreCase(b.getMaNhanVien()); }
                else { soSanh = boDau(a.getHoTen()).compareTo(boDau(b.getHoTen())); }
                if (sapXep == 2) { soSanh = -soSanh; }
                if (soSanh > 0) { ketQua.set(i, b); ketQua.set(j, a); }
            }
        }
        return ketQua;
    }
}
