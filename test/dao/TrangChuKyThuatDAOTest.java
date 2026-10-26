package dao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import entity.TaiKhoan;
import entity.ThongKeNgayKyThuat;
import entity.TongQuanKyThuat;
import entity.VaiTro;

// Kiểm tra dữ liệu mẫu, quyền truy cập và việc bảo vệ dữ liệu trong entity.
public final class TrangChuKyThuatDAOTest {
    private TrangChuKyThuatDAOTest() {
    }

    /** @param args không sử dụng */
    // Kiểm tra tổng từng ngày, tài khoản ngoài quyền và bản sao danh sách thống kê.
    public static void main(String[] args) {
        TrangChuKyThuatDAO trangChuDAO = new TrangChuKyThuatDAO();
        TongQuanKyThuat tongQuan = trangChuDAO.docDuLieuMau(
                new TaiKhoan("kythuat", VaiTro.NHAN_VIEN_KY_THUAT));
        List<ThongKeNgayKyThuat> thongKeNgay = tongQuan.getThongKeNgay();
        int[] tongTheoNgay = {2, 3, 1, 3, 4};
        for (int i = 0; i < tongTheoNgay.length; i++) {
            if (thongKeNgay.get(i).tinhTongCongViec() != tongTheoNgay[i]) {
                throw new AssertionError("Sai tổng công việc ngày " + (i + 1));
            }
        }
        if (tongQuan.tinhHoanTatTrongNgay() != 4) {
            throw new AssertionError("Sai tổng số công việc hoàn tất trong ngày báo cáo.");
        }
        for (VaiTro vaiTro : VaiTro.values()) {
            if (vaiTro != VaiTro.NHAN_VIEN_KY_THUAT) {
                kiemTraTuChoi(trangChuDAO, new TaiKhoan("kythuat", vaiTro));
            }
        }
        kiemTraTuChoi(trangChuDAO, new TaiKhoan("nguoiKhac", VaiTro.NHAN_VIEN_KY_THUAT));
        List<Integer> soLuongRong = new ArrayList<Integer>();
        soLuongRong.add(0);
        soLuongRong.add(0);
        soLuongRong.add(0);
        ThongKeNgayKyThuat ngayRong = new ThongKeNgayKyThuat(LocalDate.of(2026, 10, 6), soLuongRong);
        if (ngayRong.tinhTongCongViec() != 0) {
            throw new AssertionError("Ngày rỗng phải có tổng bằng không.");
        }
        thongKeNgay.clear();
        if (tongQuan.getThongKeNgay().size() != 5) {
            throw new AssertionError("Getter đã làm lộ danh sách nội bộ.");
        }
        List<Integer> banSao = tongQuan.getThongKeNgay().get(0).getSoLuongTheoNhom();
        banSao.set(0, 100);
        if (tongQuan.getThongKeNgay().get(0).tinhTongCongViec() != 2) {
            throw new AssertionError("Getter đã làm lộ số lượng nội bộ.");
        }
        System.out.println("PASS: dữ liệu 5 ngày, KPI, ngày rỗng, quyền truy cập và bản sao collection.");
    }

    /**
     * @param trangChuDAO đối tượng truy cập dữ liệu mẫu
     * @param taiKhoan tài khoản không được đọc dữ liệu mẫu
     */
    // Xác nhận DAO từ chối tài khoản ngoài phạm vi dữ liệu mẫu.
    private static void kiemTraTuChoi(TrangChuKyThuatDAO trangChuDAO, TaiKhoan taiKhoan) {
        try {
            trangChuDAO.docDuLieuMau(taiKhoan);
        } catch (SecurityException expected) {
            return;
        }
        throw new AssertionError("DAO đã chấp nhận tài khoản ngoài quyền.");
    }
}