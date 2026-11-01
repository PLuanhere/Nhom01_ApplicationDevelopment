package entity;

import java.math.BigDecimal;
import java.util.Objects;

/** Hạng mục báo giá giữ tên, đơn vị và giá tại thời điểm lập. */
public final class ChiTietBaoGia {

    private final String maDichVu;
    private final String tenHangMuc;
    private final String loai;
    private final String donVi;
    private final BigDecimal soLuong;
    private final BigDecimal donGia;
    private final String maPhuTung;

    /** Hạng mục chưa có mã liên kết dịch vụ hoặc phụ tùng. */
    public ChiTietBaoGia(String tenHangMuc, String loai, String donVi,
            String soLuong, String donGia) {
        this("", tenHangMuc, loai, donVi, soLuong, donGia, "");
    }

    /** Giữ constructor của main: mã phụ tùng nằm cuối. */
    public ChiTietBaoGia(String tenHangMuc, String loai, String donVi,
            String soLuong, String donGia, String maPhuTung) {
        this("", tenHangMuc, loai, donVi, soLuong, donGia, maPhuTung);
    }

    /**
     * Tạo hạng mục có mã liên kết.
     * Mã không sử dụng truyền chuỗi rỗng.
     */
    public ChiTietBaoGia(String maDichVu, String tenHangMuc, String loai,
            String donVi, String soLuong, String donGia, String maPhuTung) {
        this.maDichVu = Objects.requireNonNull(maDichVu).trim();
        this.maPhuTung = Objects.requireNonNull(maPhuTung).strip();
        this.tenHangMuc = Objects.requireNonNull(tenHangMuc);
        this.loai = Objects.requireNonNull(loai);
        this.donVi = Objects.requireNonNull(donVi);
        this.soLuong = new BigDecimal(soLuong);
        this.donGia = new BigDecimal(donGia);

        if (this.soLuong.signum() <= 0 || this.donGia.signum() < 0) {
            throw new IllegalArgumentException(
                    "Số lượng phải dương và đơn giá không được âm.");
        }
    }

    public String getMaDichVu() { return maDichVu; }
    public String getMaPhuTung() { return maPhuTung; }
    public String getTenHangMuc() { return tenHangMuc; }
    public String getLoai() { return loai; }
    public String getDonVi() { return donVi; }
    public BigDecimal getSoLuong() { return soLuong; }
    public BigDecimal getDonGia() { return donGia; }

    /** @return thành tiền theo số lượng và giá đã lưu trên báo giá */
    public BigDecimal getThanhTien() {
        return soLuong.multiply(donGia);
    }
}