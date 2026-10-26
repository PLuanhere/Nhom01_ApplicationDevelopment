package entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Lưu thông tin tổng quan dashboard của một kỹ thuật viên
public final class TongQuanKyThuat {
    private final String tenKyThuatVien;
    private final LocalDate ngayBaoCao;
    private final int dangSua;
    private final int choPhuTung;
    private final int choKhachDuyet;
    private final List<ThongKeNgayKyThuat> thongKeNgay;

    /**
     * @param tenKyThuatVien tên kỹ thuật viên
     * @param ngayBaoCao ngày thống kê
     * @param dangSua số lệnh đang sửa
     * @param choPhuTung số công việc chờ phụ tùng
     * @param choKhachDuyet số đề xuất chờ khách duyệt
     * @param thongKeNgay danh sách thống kê theo ngày
     * @throws IllegalArgumentException nếu dữ liệu thiếu hoặc chỉ số âm
     */
    public TongQuanKyThuat(String tenKyThuatVien, LocalDate ngayBaoCao, int dangSua,
            int choPhuTung, int choKhachDuyet, List<ThongKeNgayKyThuat> thongKeNgay) {
        if (tenKyThuatVien == null || tenKyThuatVien.trim().isEmpty() || ngayBaoCao == null
                || thongKeNgay == null || dangSua < 0 || choPhuTung < 0 || choKhachDuyet < 0) {
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

    public String getTenKyThuatVien() {
        return tenKyThuatVien;
    }

    public LocalDate getNgayBaoCao() {
        return ngayBaoCao;
    }

    public int getDangSua() {
        return dangSua;
    }

    public int getChoPhuTung() {
        return choPhuTung;
    }

    public int getChoKhachDuyet() {
        return choKhachDuyet;
    }

    public List<ThongKeNgayKyThuat> getThongKeNgay() {
        return new ArrayList<ThongKeNgayKyThuat>(thongKeNgay);
    }

    /** @return tổng công việc hoàn tất đúng ngày báo cáo */
    // Cộng số công việc hoàn tất của các thống kê có ngày trùng với ngày báo cáo.
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