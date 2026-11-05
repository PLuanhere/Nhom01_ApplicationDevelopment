package entity;

import java.time.LocalDateTime;

/** Lưu nhân viên được phân công thực hiện một phiếu sửa chữa. */
public final class PhanCong {
    private String maPC;
    private String maPhieuSC;
    private String maNV;
    private LocalDateTime ngayPhanCong;
    private String ghiChu;

    public PhanCong(String maPC, String maPhieuSC, String maNV, LocalDateTime ngayPhanCong, String ghiChu) {
        setMaPC(maPC);
        setMaPhieuSC(maPhieuSC);
        setMaNV(maNV);
        setNgayPhanCong(ngayPhanCong);
        setGhiChu(ghiChu);
    }

    public String getMaPC() { return maPC; }
    public String getMaPhieuSC() { return maPhieuSC; }
    public String getMaNV() { return maNV; }
    public LocalDateTime getNgayPhanCong() { return ngayPhanCong; }
    public String getGhiChu() { return ghiChu; }

    public void setMaPC(String maPC) { this.maPC = batBuoc(maPC, "Mã phân công"); }
    public void setMaPhieuSC(String maPhieuSC) { this.maPhieuSC = batBuoc(maPhieuSC, "Mã phiếu sửa chữa"); }
    public void setMaNV(String maNV) { this.maNV = batBuoc(maNV, "Mã nhân viên"); }

    public void setNgayPhanCong(LocalDateTime ngayPhanCong) {
        if (ngayPhanCong == null) {
            throw new IllegalArgumentException("Ngày phân công không được để trống.");
        }
        this.ngayPhanCong = ngayPhanCong;
    }

    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu == null ? "" : ghiChu.trim(); }

    private String batBuoc(String giaTri, String tenTruong) {
        if (giaTri == null || giaTri.trim().isEmpty()) {
            throw new IllegalArgumentException(tenTruong + " không được để trống.");
        }
        return giaTri.trim();
    }
}
