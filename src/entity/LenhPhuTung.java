package entity;

import java.util.Arrays;

public final class LenhPhuTung {
    private final String maLenh;
    private final String bienSo;
    private final String kyThuatVien;
    private final boolean baoGiaV2;
    private String trangThai;
    private String trangThaiCongViec;
    private String ketQuaCongViec;
    private String[] phatSinh;
    private String[] ketQuaHoanTat;

    public LenhPhuTung(String maLenh, String bienSo, String kyThuatVien, boolean baoGiaV2) {
        this.maLenh = maLenh;
        this.bienSo = bienSo;
        this.kyThuatVien = kyThuatVien;
        this.baoGiaV2 = baoGiaV2;
        trangThai = "Đang sửa chữa";
        trangThaiCongViec = "Đang sửa chữa";
        ketQuaCongViec = "Đã thay dầu/lọc; đang kiểm tra phanh.";
        phatSinh = new String[] {"", "", "", "", ""};
        ketQuaHoanTat = new String[] {"", "", ""};
    }

    public LenhPhuTung(LenhPhuTung lenh) {
        maLenh = lenh.maLenh;
        bienSo = lenh.bienSo;
        kyThuatVien = lenh.kyThuatVien;
        baoGiaV2 = lenh.baoGiaV2;
        trangThai = lenh.trangThai;
        trangThaiCongViec = lenh.trangThaiCongViec;
        ketQuaCongViec = lenh.ketQuaCongViec;
        phatSinh = Arrays.copyOf(lenh.phatSinh, lenh.phatSinh.length);
        ketQuaHoanTat = Arrays.copyOf(lenh.ketQuaHoanTat, lenh.ketQuaHoanTat.length);
    }

    public String getMaLenh() {
        return maLenh;
    }

    public String getBienSo() {
        return bienSo;
    }

    public String getKyThuatVien() {
        return kyThuatVien;
    }

    public boolean isBaoGiaV2() {
        return baoGiaV2;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getTrangThaiCongViec() {
        return trangThaiCongViec;
    }

    public void setTrangThaiCongViec(String trangThaiCongViec) {
        this.trangThaiCongViec = trangThaiCongViec;
    }

    public String getKetQuaCongViec() {
        return ketQuaCongViec;
    }

    public void setKetQuaCongViec(String ketQuaCongViec) {
        this.ketQuaCongViec = ketQuaCongViec;
    }

    public String[] getPhatSinh() {
        return Arrays.copyOf(phatSinh, phatSinh.length);
    }

    public void setPhatSinh(String[] phatSinh) {
        this.phatSinh = Arrays.copyOf(phatSinh, phatSinh.length);
    }

    public String[] getKetQuaHoanTat() {
        return Arrays.copyOf(ketQuaHoanTat, ketQuaHoanTat.length);
    }

    public void setKetQuaHoanTat(String[] ketQuaHoanTat) {
        this.ketQuaHoanTat = Arrays.copyOf(ketQuaHoanTat, ketQuaHoanTat.length);
    }
}