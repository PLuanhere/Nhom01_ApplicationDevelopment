package entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import entity.MucTongQuanQuanLy.Nhom;

/** Snapshot chỉ đọc: các chỉ số và doanh thu bảy ngày được tính từ cùng danh sách chi tiết. */
public final class TongQuanQuanLy {

    /** Thời điểm chốt dữ liệu; không mặc định dữ liệu tạo sẵn là dữ liệu hôm nay. */
    private final LocalDateTime thoiDiem;
    /** Danh sách bất biến, không cho UI sửa trạng thái hoặc thêm chứng từ. */
    private final List<MucTongQuanQuanLy> cacMuc;
    private final List<HoaDonDoanhThu> hoaDon;

    /**
     * Tạo snapshot tổng quan; hóa đơn trùng mã bị từ chối để tránh cộng tiền hai lần.
     * @param thoiDiem thời điểm chốt dữ liệu
     * @param cacMuc hồ sơ hiện tại
     * @param hoaDon hóa đơn đã thanh toán
     * @throws IllegalArgumentException nếu trùng hóa đơn hoặc có thanh toán sau thời điểm chốt
     */
    public TongQuanQuanLy(LocalDateTime thoiDiem, List<MucTongQuanQuanLy> cacMuc,
            List<HoaDonDoanhThu> hoaDon) {
        this.thoiDiem = Objects.requireNonNull(thoiDiem);
        this.cacMuc = List.copyOf(cacMuc);
        this.hoaDon = List.copyOf(hoaDon);
        Set<String> maDaCo = new HashSet<>();
        for (HoaDonDoanhThu muc : this.hoaDon) {
            if (!maDaCo.add(muc.getMaHoaDon()) || muc.getNgayThanhToan().isAfter(thoiDiem)) {
                throw new IllegalArgumentException("Hóa đơn trùng mã hoặc chưa thuộc thời điểm chốt dữ liệu.");
            }
        }
    }

    /** @return thời điểm chốt dữ liệu */
    public LocalDateTime getThoiDiem() {
        return thoiDiem;
    }

    /** @return ngày đầu của bảy ngày hiển thị */
    public LocalDate getTuNgay() {
        return thoiDiem.toLocalDate().minusDays(6);
    }

    /** @return ngày cuối của kỳ */
    public LocalDate getDenNgay() {
        return thoiDiem.toLocalDate();
    }

    /**
     * Lấy hồ sơ thuộc một nhóm chỉ số.
     * @param nhom nhóm cần đọc
     * @return danh sách chỉ đọc
     */
    public List<MucTongQuanQuanLy> getCacMuc(Nhom nhom) {
        Objects.requireNonNull(nhom);
        return cacMuc.stream().filter(muc -> muc.getNhom() == nhom).toList();
    }

    /** @return lệnh đến hạn trong ngày chốt, sắp theo hạn; không tự đặt ngưỡng "gần đến hạn" */
    public List<MucTongQuanQuanLy> getLenhDenHanTrongNgay() {
        return getCacMuc(Nhom.LENH_DANG_XU_LY).stream()
                .filter(muc -> muc.getHanXuLy() != null && muc.getHanXuLy().toLocalDate().equals(getDenNgay()))
                .sorted(Comparator.comparing(MucTongQuanQuanLy::getHanXuLy)).toList();
    }

    /**
     * Lấy đúng hóa đơn của một cột trên biểu đồ, tính theo ngày thanh toán.
     * @param ngay ngày cần đối chiếu
     * @return danh sách chỉ đọc, sắp theo thời điểm thanh toán
     */
    public List<HoaDonDoanhThu> getHoaDonTheoNgay(LocalDate ngay) {
        Objects.requireNonNull(ngay);
        return hoaDon.stream().filter(muc -> muc.getNgayThanhToan().toLocalDate().equals(ngay))
                .sorted(Comparator.comparing(HoaDonDoanhThu::getNgayThanhToan)).toList();
    }

    /**
     * Tính tiền từ chính danh sách dùng trong dialog.
     * @param ngay ngày trên biểu đồ
     * @return tổng tiền chính xác, ngày không có hóa đơn trả 0
     */
    public BigDecimal tinhDoanhThuNgay(LocalDate ngay) {
        return getHoaDonTheoNgay(ngay).stream().map(HoaDonDoanhThu::getSoTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** @return tổng của đúng bảy cột, không cộng hóa đơn ngoài kỳ */
    public BigDecimal tinhDoanhThuTrongKy() {
        BigDecimal tong = BigDecimal.ZERO;
        for (int viTri = 0; viTri < 7; viTri++) {
            tong = tong.add(tinhDoanhThuNgay(getTuNgay().plusDays(viTri)));
        }
        return tong;
    }
}
