package entity;

import java.math.BigDecimal;
import java.util.Objects;

// Hạng mục báo giá mẫu; tiền và số lượng dùng BigDecimal.
public final class ChiTietBaoGia {
    private final String tenHangMuc;
    private final String loai;
    private final String donVi;
    private final BigDecimal soLuong;
    private final BigDecimal donGia;

    public ChiTietBaoGia(String tenHangMuc, String loai, String donVi, String soLuong, String donGia) {
        this.tenHangMuc = Objects.requireNonNull(tenHangMuc);
        this.loai = Objects.requireNonNull(loai);
        this.donVi = Objects.requireNonNull(donVi);
        this.soLuong = new BigDecimal(soLuong);
        this.donGia = new BigDecimal(donGia);
        if (this.soLuong.signum() <= 0 || this.donGia.signum() < 0) {
            throw new IllegalArgumentException("Số lượng phải dương và đơn giá không được âm.");
        }
    }

    public String getTenHangMuc() { return tenHangMuc; }
    public String getLoai() { return loai; }
    public String getDonVi() { return donVi; }
    public BigDecimal getSoLuong() { return soLuong; }
    public BigDecimal getDonGia() { return donGia; }
    public BigDecimal getThanhTien() { return soLuong.multiply(donGia); }
}