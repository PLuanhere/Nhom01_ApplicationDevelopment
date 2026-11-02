package entity.baocao;

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