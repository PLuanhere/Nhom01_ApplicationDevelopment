package dao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import entity.ChiTietNhapKho;
import entity.PhieuNhapKho;
import entity.PhuTung;
import entity.TaiKhoan;
import entity.VaiTro;

/** Lưu phiếu nhập nhiều mặt hàng và cập nhật tồn sau khi kiểm tra đủ dữ liệu. */
public final class PhieuNhapKhoDAO {
    private final PhuTungDAO kho;
    private final List<PhieuNhapKho> danhSach = new ArrayList<>();
    private int soTiepTheo = 1;

    /** Gắn nguồn phiếu nhập với đúng DAO đang quản lý kho. */
    PhieuNhapKhoDAO(PhuTungDAO kho) { this.kho = kho; }

    /** Sinh mã phiếu nhập kho dự kiến để hiển thị, chưa lưu hoặc chiếm mã. */
    public String maTiepTheo(TaiKhoan tk) {
        kiemTraQuyen(tk);
        return "NK-" + LocalDateTime.now().getYear() + "-" + String.format("%04d", soTiepTheo);
    }

    /** Kiểm tra phiếu nhập, gộp lượng cùng mã và lưu phiếu cùng biến động tồn. */
    public PhieuNhapKho ghiNhan(TaiKhoan tk, String ma, String nguon, String chungTu,
        List<ChiTietNhapKho> chiTiet) {
        kiemTraQuyen(tk);
        if (!maTiepTheo(tk).equals(ma)) {
            throw new IllegalArgumentException("Mã lần nhập đã thay đổi. Kiểm tra mã mới rồi ghi nhận lại.");
        }
        PhieuNhapKho phieu = new PhieuNhapKho(ma, LocalDateTime.now(), nguon, chungTu,
            tk.getTenDangNhap(), chiTiet);
        String[] cacMa = new String[chiTiet.size()];
        double[] cacLuong = new double[chiTiet.size()];
        int soMatHang = 0;
        for (ChiTietNhapKho dong : phieu.getChiTiet()) {
            PhuTung pt = kho.timNoiBo(dong.getPhuTung().getMaPhuTung());
            if (!kho.cungThongTin(pt, dong.getPhuTung())) {
                throw new IllegalArgumentException("Danh mục đã thay đổi. Mở sửa dòng hàng rồi kiểm tra lại.");
            }
            if (!pt.isHoatDong()) { throw new IllegalArgumentException("Phụ tùng đã ngừng hoạt động: " + pt.getMaPhuTung()); }
            int viTri = -1;
            for (int i = 0; i < soMatHang; i++) {
                if (cacMa[i].equals(pt.getMaPhuTung())) { viTri = i; break; }
            }
            if (viTri == -1) {
                cacMa[soMatHang] = pt.getMaPhuTung();
                cacLuong[soMatHang] = dong.getSoLuong();
                soMatHang++;
            } else {
                double tongLuong = cacLuong[viTri] + dong.getSoLuong();
                if (!(tongLuong < Long.MAX_VALUE / 1000.0)) {
                    throw new IllegalArgumentException("Tổng lượng nhập vượt giới hạn cho phép.");
                }
                cacLuong[viTri] = Math.round(tongLuong * 1000) / 1000.0;
            }
        }
        String[] maPhuTung = new String[soMatHang];
        double[] luongNhap = new double[soMatHang];
        for (int i = 0; i < soMatHang; i++) { maPhuTung[i] = cacMa[i]; luongNhap[i] = cacLuong[i]; }
        kho.ghiBienDong(maPhuTung, luongNhap, true, ma, tk, phieu.getNguonNhap(), phieu.getNgayNhap());
        danhSach.add(phieu);
        soTiepTheo++;
        return phieu;
    }

    /** Đọc danh sách phiếu nhập kho sau khi kiểm tra quyền truy cập. */
    public List<PhieuNhapKho> layDanhSach(TaiKhoan tk) {
        kiemTraQuyen(tk);
        return new ArrayList<>(danhSach);
    }

    /** Tìm phiếu nhập kho theo mã; báo lỗi nếu không tồn tại. */
    public PhieuNhapKho timTheoMa(TaiKhoan tk, String ma) {
        kiemTraQuyen(tk);
        for (PhieuNhapKho phieu : danhSach) {
            if (phieu.getMaPhieu().equals(ma)) { return phieu; }
        }
        throw new IllegalArgumentException("Không tìm thấy lần nhập kho.");
    }

    /** Kiểm tra tài khoản, phiên đăng nhập và quyền Quản lý trước thao tác DAO. */
    private void kiemTraQuyen(TaiKhoan tk) {
        if (tk == null) { throw new SecurityException("Cần đăng nhập."); }
        tk.kiemTraPhien();
        if (tk.getVaiTro() != VaiTro.QUAN_LY) { throw new SecurityException("Chỉ Quản lý được ghi nhận nhập kho."); }
    }
}
