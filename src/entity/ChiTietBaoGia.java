package entity;

import java.math.BigDecimal;
import java.util.Objects;

// Hạng mục báo giá mẫu; tiền và số lượng dùng BigDecimal.
public final class ChiTietBaoGia {
    private final String maDichVu;
    private final String tenHangMuc;
    private final String loai;
    private final String donVi;
    private final BigDecimal soLuong;
    private final BigDecimal donGia;

    public ChiTietBaoGia(String tenHangMuc, String loai, String donVi, String soLuong, String donGia) {
        this("", tenHangMuc, loai, donVi, soLuong, donGia);
    }

    /** Mã dịch vụ liên kết danh mục; tên, đơn vị và giá là bản chụp tại lúc lập báo giá. */
    public ChiTietBaoGia(String maDichVu, String tenHangMuc, String loai, String donVi,
            String soLuong, String donGia) {
        this.maDichVu = Objects.requireNonNull(maDichVu).trim();
        this.tenHangMuc = Objects.requireNonNull(tenHangMuc);
        this.loai = Objects.requireNonNull(loai);
        this.donVi = Objects.requireNonNull(donVi);
        this.soLuong = new BigDecimal(soLuong);
        this.donGia = new BigDecimal(donGia);
        if (this.soLuong.signum() <= 0 || this.donGia.signum() < 0) {
            throw new IllegalArgumentException("Số lượng phải dương và đơn giá không được âm.");
        }
    }

    public String getMaDichVu() { return maDichVu; }
    public String getTenHangMuc() { return tenHangMuc; }
    public String getLoai() { return loai; }
    public String getDonVi() { return donVi; }
    public BigDecimal getSoLuong() { return soLuong; }
    public BigDecimal getDonGia() { return donGia; }
    public BigDecimal getThanhTien() { return soLuong.multiply(donGia); }
}
