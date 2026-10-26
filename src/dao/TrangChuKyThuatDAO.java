package dao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import entity.TaiKhoan;
import entity.ThongKeNgayKyThuat;
import entity.TongQuanKyThuat;
import entity.VaiTro;

// Cung cấp dữ liệu mẫu chỉ đọc của dashboard theo Figma
public final class TrangChuKyThuatDAO {
    /**
     * @param taiKhoan tài khoản được xác thực qua đăng nhập chung
     * @return dữ liệu mẫu dashboard kỹ thuật
     * @throws IllegalArgumentException nếu thiếu tài khoản
     * @throws SecurityException nếu tài khoản không được đọc dữ liệu mẫu
     */
    // Kiểm tra tài khoản và tạo dữ liệu mẫu đúng số liệu của prototype.
    public TongQuanKyThuat docDuLieuMau(TaiKhoan taiKhoan) {
        if (taiKhoan == null) {
            throw new IllegalArgumentException("Cần tài khoản đã đăng nhập.");
        }
        if (taiKhoan.getVaiTro() != VaiTro.NHAN_VIEN_KY_THUAT
                || !"kythuat".equals(taiKhoan.getTenDangNhap())) {
            throw new SecurityException("Dữ liệu mẫu chỉ dành cho tài khoản kythuat.");
        }
        List<ThongKeNgayKyThuat> thongKe = new ArrayList<ThongKeNgayKyThuat>();
        thongKe.add(taoThongKeNgay(1, 1, 1, 0));
        thongKe.add(taoThongKeNgay(2, 1, 1, 1));
        thongKe.add(taoThongKeNgay(3, 0, 1, 0));
        thongKe.add(taoThongKeNgay(4, 2, 1, 0));
        thongKe.add(taoThongKeNgay(5, 2, 1, 1));
        return new TongQuanKyThuat("Chung Ngô Minh Hoàng", LocalDate.of(2026, 10, 5), 3, 2, 1, thongKe);
    }

    /**
     * @param ngay ngày trong tháng 10/2026
     * @param baoDuong số công việc bảo dưỡng
     * @param coKhi số công việc cơ khí
     * @param dien số công việc điện và chẩn đoán
     * @return thống kê của ngày đã chọn
     */
    // Ghép số lượng của ba nhóm công việc thành thống kê mẫu của một ngày.
    private ThongKeNgayKyThuat taoThongKeNgay(int ngay, int baoDuong, int coKhi, int dien) {
        List<Integer> soLuong = new ArrayList<Integer>();
        soLuong.add(baoDuong);
        soLuong.add(coKhi);
        soLuong.add(dien);
        return new ThongKeNgayKyThuat(LocalDate.of(2026, 10, ngay), soLuong);
    }
}