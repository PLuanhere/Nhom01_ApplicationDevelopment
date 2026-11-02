package dao;

import java.util.ArrayList;
import java.util.List;

import entity.DichVu;
import entity.TaiKhoan;
import entity.VaiTro;

/** Quản lý danh mục dịch vụ, đơn giá và điều kiện xóa. */
public final class DichVuDAO {

    private static final List<DichVu> DANH_SACH = taoDuLieuBanDau();

    /** Tạo danh sách dịch vụ ban đầu cho lần chạy thử nghiệm. */
    private static List<DichVu> taoDuLieuBanDau() {
        List<DichVu> danhSach = new ArrayList<>();
        danhSach.add(new DichVu("DV001", "Bảo dưỡng định kỳ", "Bảo dưỡng", "Công", 300000L, "",
            "Theo quy trình garage", "Theo loại xe và phạm vi công việc", true));
        danhSach.add(new DichVu("DV002", "Vệ sinh phanh trước", "Phanh", "Công", 100000L, "", "", "", true));
        danhSach.add(new DichVu("DV003", "Kiểm tra điều hòa", "Điện lạnh", "Công", 200000L, "", "", "", true));
        return danhSach;
    }

    private static int soTiepTheo = 4;

    /** Đọc danh sách dịch vụ sau khi kiểm tra quyền truy cập. */
    public List<DichVu> layDanhSach(TaiKhoan taiKhoan) {
        kiemTraQuyen(taiKhoan);
        return new ArrayList<>(DANH_SACH);
    }

    /** Tìm và lọc dịch vụ theo các điều kiện được truyền vào. */
    public List<DichVu> timKiem(TaiKhoan taiKhoan, String tuKhoa, int dangApDung) {
        String chu = boDau(tuKhoa == null ? "" : tuKhoa.trim());
        List<DichVu> ketQua = new ArrayList<>();
        for (DichVu dv : layDanhSach(taiKhoan)) {
            boolean dungChu = boDau(dv.getMaDichVu()).contains(chu) || boDau(dv.getTenDichVu()).contains(chu)
                || boDau(dv.getNhomDichVu()).contains(chu);
            if (dungChu && (dangApDung == -1 || dv.isDangApDung() == (dangApDung == 1))) { ketQua.add(dv); }
        }
        return ketQua;
    }

    /** Tìm dịch vụ theo mã; báo lỗi nếu không tồn tại. */
    public DichVu timTheoMa(TaiKhoan taiKhoan, String maDichVu) {
        kiemTraQuyen(taiKhoan);
        for (DichVu dv : DANH_SACH) {
            if (dv.getMaDichVu().equals(maDichVu)) { return dv; }
        }
        throw new IllegalArgumentException("Không tìm thấy dịch vụ.");
    }

    /** Sinh mã dịch vụ dự kiến để hiển thị, chưa lưu hoặc chiếm mã. */
    public String maTiepTheo(TaiKhoan taiKhoan) {
        kiemTraQuyen(taiKhoan);
        return String.format("DV%03d", soTiepTheo);
    }

    /** Kiểm tra quyền và dữ liệu trước khi thêm dịch vụ vào nguồn. */
    public void them(TaiKhoan taiKhoan, DichVu dichVu) {
        kiemTraQuyen(taiKhoan);
        if (dichVu == null) { throw new IllegalArgumentException("Thiếu dichVu."); }
        if (!dichVu.getMaDichVu().equals(maTiepTheo(taiKhoan))) {
            throw new IllegalArgumentException("Mã dịch vụ đã thay đổi. Hãy mở lại form thêm dịch vụ.");
        }
        DANH_SACH.add(dichVu);
        soTiepTheo++;
    }

    /** Kiểm tra dữ liệu rồi cập nhật dịch vụ trong nguồn. */
    public void capNhat(TaiKhoan taiKhoan, DichVu banDau, DichVu dichVu) {
        kiemTraQuyen(taiKhoan);
        if (banDau == null) { throw new IllegalArgumentException("Thiếu banDau."); }
        if (dichVu == null) { throw new IllegalArgumentException("Thiếu dichVu."); }
        if (!banDau.getMaDichVu().equals(dichVu.getMaDichVu())) {
            throw new IllegalArgumentException("Không được thay đổi mã dịch vụ.");
        }
        DichVu hienTai = timTheoMa(taiKhoan, banDau.getMaDichVu());
        if (!cungThongTin(hienTai, banDau)) {
            throw new IllegalArgumentException("Dịch vụ đã được cập nhật ở cửa sổ khác. Hãy mở lại chi tiết.");
        }
        DANH_SACH.set(DANH_SACH.indexOf(hienTai), dichVu);
    }

    /** Kiểm tra tham chiếu báo giá rồi xóa dịch vụ chưa được sử dụng. */
    public void xoa(TaiKhoan taiKhoan, DichVu dichVu) {
        kiemTraQuyen(taiKhoan);
        if (dichVu == null) { throw new IllegalArgumentException("Thiếu dichVu."); }
        DichVu hienTai = timTheoMa(taiKhoan, dichVu.getMaDichVu());
        if (!cungThongTin(hienTai, dichVu)) {
            throw new IllegalArgumentException("Dịch vụ đã thay đổi. Hãy mở lại chi tiết trước khi xóa.");
        }
        if (PhieuSuaChuaDAO.daSuDungDichVu(taiKhoan, dichVu.getMaDichVu())) {
            throw new IllegalArgumentException("Dịch vụ đã có trong báo giá, không được xóa. Hãy tạm dừng dịch vụ.");
        }
        DANH_SACH.remove(hienTai);
    }

    /** Kiểm tra tài khoản, phiên đăng nhập và quyền Quản lý trước thao tác DAO. */
    private void kiemTraQuyen(TaiKhoan taiKhoan) {
        if (taiKhoan == null) { throw new SecurityException("Cần đăng nhập."); }
        taiKhoan.kiemTraPhien();
        if (taiKhoan.getVaiTro() != VaiTro.QUAN_LY) {
            throw new SecurityException("Chỉ Quản lý được quản lý dịch vụ và bảng giá.");
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

    /** Tìm và lọc dịch vụ theo các điều kiện rồi sắp xếp danh sách kết quả. */
    public List<DichVu> timKiem(TaiKhoan taiKhoan, String tuKhoa, int dangApDung, int sapXep) {
        List<DichVu> ketQua = timKiem(taiKhoan, tuKhoa, dangApDung);
        for (int i = 0; i < ketQua.size() - 1; i++) {
            for (int j = i + 1; j < ketQua.size(); j++) {
                DichVu a = ketQua.get(i);
                DichVu b = ketQua.get(j);
                int soSanh;
                if (sapXep == 0) { soSanh = a.getMaDichVu().compareToIgnoreCase(b.getMaDichVu()); }
                else { soSanh = boDau(a.getTenDichVu()).compareTo(boDau(b.getTenDichVu())); }
                if (sapXep == 2) { soSanh = -soSanh; }
                if (sapXep == 3 || sapXep == 4) {
                    soSanh = 0;
                    if (a.getDonGia() > b.getDonGia()) { soSanh = 1; }
                    if (a.getDonGia() < b.getDonGia()) { soSanh = -1; }
                    if (sapXep == 4) { soSanh = -soSanh; }
                }
                if (soSanh > 0) { ketQua.set(i, b); ketQua.set(j, a); }
            }
        }
        return ketQua;
    }

    /** So sánh dữ liệu danh mục để tránh ghi đè bản ghi đã được thay đổi. */
    private boolean cungThongTin(DichVu a, DichVu b) {
        return a.getMaDichVu().equals(b.getMaDichVu()) && a.getTenDichVu().equals(b.getTenDichVu())
            && a.getNhomDichVu().equals(b.getNhomDichVu()) && a.getDonViTinh().equals(b.getDonViTinh())
            && a.getDonGia() == b.getDonGia() && a.getMoTa().equals(b.getMoTa())
            && a.getThoiGianDuKien().equals(b.getThoiGianDuKien())
            && a.getDieuKienApDung().equals(b.getDieuKienApDung()) && a.isDangApDung() == b.isDangApDung();
    }
}
