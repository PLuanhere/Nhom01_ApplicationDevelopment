package dao;

import dao.TrangChuKyThuatDAO.ThongKeNgayKyThuat;
import dao.TrangChuKyThuatDAO.TongQuanKyThuat;
import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Cung cấp dữ liệu mẫu chỉ đọc của dashboard theo Figma
public final class TrangChuKyThuatDAO {

    // Kiểm tra tài khoản và tạo dữ liệu mẫu đúng số liệu của prototype.
    /** Đọc du lieu mau từ dữ liệu hiện tại. */
    public TongQuanKyThuat docDuLieuMau(TaiKhoan taiKhoan) {
        if (taiKhoan == null) {
            throw new IllegalArgumentException("Cần tài khoản đã đăng nhập.");
        }
        taiKhoan.kiemTraPhien();
        if (taiKhoan.getVaiTro() != VaiTro.NHAN_VIEN_KY_THUAT || !"kythuat".equals(taiKhoan.getTenDangNhap())) {
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

    // Ghép số lượng của ba nhóm công việc thành thống kê mẫu của một ngày.
    /** Tạo thong ke ngay cho giao diện. */
    private ThongKeNgayKyThuat taoThongKeNgay(int ngay, int baoDuong, int coKhi, int dien) {
        List<Integer> soLuong = new ArrayList<Integer>();
        soLuong.add(baoDuong);
        soLuong.add(coKhi);
        soLuong.add(dien);
        return new ThongKeNgayKyThuat(LocalDate.of(2026, 10, ngay), soLuong);
    }

    public static final class ThongKeNgayKyThuat {
        private final LocalDate ngay;
        private final List<Integer> soLuongTheoNhom;
    
        /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
        public ThongKeNgayKyThuat(LocalDate ngay, List<Integer> soLuongTheoNhom) {
            if (ngay == null || soLuongTheoNhom == null || soLuongTheoNhom.size() != 3) {
                throw new IllegalArgumentException("Cần ngày và số lượng của ba nhóm công việc.");
            }
            for (Integer soLuong : soLuongTheoNhom) {
                if (soLuong == null || soLuong < 0) {
                    throw new IllegalArgumentException("Số lượng công việc không được âm hoặc null.");
                }
            }
            this.ngay = ngay;
            this.soLuongTheoNhom = new ArrayList<Integer>(soLuongTheoNhom);
        }
    
        /** Trả về ngay. */
        public LocalDate getNgay() {
            return ngay;
        }
    
        /** Trả về so luong theo nhom. */
        public List<Integer> getSoLuongTheoNhom() {
            return new ArrayList<Integer>(soLuongTheoNhom);
        }
    
        // Cộng số lượng của ba nhóm để tính tổng công việc hoàn tất trong ngày.
        /** Thực hiện tinh tong cong viec cho thành phần hiện tại. */
        public int tinhTongCongViec() {
            int tong = 0;
            for (Integer soLuong : soLuongTheoNhom) {
                tong += soLuong;
            }
            return tong;
        }
    }

    public static final class TongQuanKyThuat {
        private final String tenKyThuatVien;
        private final LocalDate ngayBaoCao;
        private final int dangSua;
        private final int choPhuTung;
        private final int choKhachDuyet;
        private final List<ThongKeNgayKyThuat> thongKeNgay;
    
        /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
        public TongQuanKyThuat(String tenKyThuatVien, LocalDate ngayBaoCao, int dangSua, int choPhuTung, int choKhachDuyet,
                List<ThongKeNgayKyThuat> thongKeNgay) {
            if (tenKyThuatVien == null || tenKyThuatVien.trim().isEmpty() || ngayBaoCao == null || thongKeNgay == null
                    || dangSua < 0 || choPhuTung < 0 || choKhachDuyet < 0) {
                throw new IllegalArgumentException("Thông tin tổng quan không hợp lệ.");
            }
            for (ThongKeNgayKyThuat thongKe : thongKeNgay) {
                if (thongKe == null) {
                    throw new IllegalArgumentException("Thống kê theo ngày không được null.");
                }
            }
            this.tenKyThuatVien = tenKyThuatVien.trim();
            this.ngayBaoCao = ngayBaoCao;
            this.dangSua = dangSua;
            this.choPhuTung = choPhuTung;
            this.choKhachDuyet = choKhachDuyet;
            this.thongKeNgay = new ArrayList<ThongKeNgayKyThuat>(thongKeNgay);
        }
    
        /** Trả về ten ky thuat vien. */
        public String getTenKyThuatVien() {
            return tenKyThuatVien;
        }
    
        /** Trả về ngay bao cao. */
        public LocalDate getNgayBaoCao() {
            return ngayBaoCao;
        }
    
        /** Trả về dang sua. */
        public int getDangSua() {
            return dangSua;
        }
    
        /** Trả về cho phu tung. */
        public int getChoPhuTung() {
            return choPhuTung;
        }
    
        /** Trả về cho khach duyet. */
        public int getChoKhachDuyet() {
            return choKhachDuyet;
        }
    
        /** Trả về thong ke ngay. */
        public List<ThongKeNgayKyThuat> getThongKeNgay() {
            return new ArrayList<ThongKeNgayKyThuat>(thongKeNgay);
        }
    
        // Cộng số công việc hoàn tất của các thống kê có ngày trùng với ngày báo cáo.
        /** Thực hiện tinh hoan tat trong ngay cho thành phần hiện tại. */
        public int tinhHoanTatTrongNgay() {
            int tong = 0;
            for (ThongKeNgayKyThuat thongKe : thongKeNgay) {
                if (thongKe.getNgay().equals(ngayBaoCao)) {
                    tong += thongKe.tinhTongCongViec();
                }
            }
            return tong;
        }
    }
}
