package entity.hoadon;

import java.time.LocalDateTime;

/** Thông tin lệnh hoàn thành để Thu ngân đối chiếu và thu tiền. */
public final class PhieuChoThu {
    private final String maPhieu;
    private final String bienSo;
    private final String tenKhach;
    private final long tongTien;
    private final LocalDateTime thoiDiemHoanTat;

    /** Tạo thông tin phiếu chờ thu từ lệnh và báo giá đã duyệt. */
    public PhieuChoThu(String maPhieu, String bienSo, String tenKhach, long tongTien, LocalDateTime thoiDiemHoanTat) {
        if (maPhieu == null || maPhieu.trim().isEmpty() || bienSo == null || tenKhach == null || tongTien < 0
                || thoiDiemHoanTat == null) {
            throw new IllegalArgumentException("Thông tin phiếu chờ thu không hợp lệ.");
        }
        this.maPhieu = maPhieu.trim();
        this.bienSo = bienSo;
        this.tenKhach = tenKhach;
        this.tongTien = tongTien;
        this.thoiDiemHoanTat = thoiDiemHoanTat;
    }

    /** Trả về mã lệnh sửa chữa. */
    public String getMaPhieu() {
        return maPhieu;
    }

    /** Trả về biển số xe. */
    public String getBienSo() {
        return bienSo;
    }

    /** Trả về tên khách hàng. */
    public String getTenKhach() {
        return tenKhach;
    }

    /** Trả về số tiền phải thu, nguyên đồng. */
    public long getTongTien() {
        return tongTien;
    }

    /** Trả về thời điểm lệnh hoàn thành. */
    public LocalDateTime getThoiDiemHoanTat() {
        return thoiDiemHoanTat;
    }
}
