package entity.phutung;

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

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public LenhPhuTung(String maLenh, String bienSo, String kyThuatVien, boolean baoGiaV2) {
        this.maLenh = maLenh;
        this.bienSo = bienSo;
        this.kyThuatVien = kyThuatVien;
        this.baoGiaV2 = baoGiaV2;
        trangThai = "Đang sửa chữa";
        trangThaiCongViec = "Đang sửa chữa";
        ketQuaCongViec = "Đã thay dầu/lọc; đang kiểm tra phanh.";
        phatSinh = new String[] { "", "", "", "", "" };
        ketQuaHoanTat = new String[] { "", "", "" };
    }

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
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

    /** Trả về ma lenh. */
    public String getMaLenh() {
        return maLenh;
    }

    /** Trả về bien so. */
    public String getBienSo() {
        return bienSo;
    }

    /** Trả về ky thuat vien. */
    public String getKyThuatVien() {
        return kyThuatVien;
    }

    /** Cho biết bao gia v2. */
    public boolean isBaoGiaV2() {
        return baoGiaV2;
    }

    /** Trả về trang thai. */
    public String getTrangThai() {
        return trangThai;
    }

    /** Kiểm tra và cập nhật trang thai. */
    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    /** Trả về trang thai cong viec. */
    public String getTrangThaiCongViec() {
        return trangThaiCongViec;
    }

    /** Kiểm tra và cập nhật trang thai cong viec. */
    public void setTrangThaiCongViec(String trangThaiCongViec) {
        this.trangThaiCongViec = trangThaiCongViec;
    }

    /** Trả về ket qua cong viec. */
    public String getKetQuaCongViec() {
        return ketQuaCongViec;
    }

    /** Kiểm tra và cập nhật ket qua cong viec. */
    public void setKetQuaCongViec(String ketQuaCongViec) {
        this.ketQuaCongViec = ketQuaCongViec;
    }

    /** Trả về phat sinh. */
    public String[] getPhatSinh() {
        return Arrays.copyOf(phatSinh, phatSinh.length);
    }

    /** Kiểm tra và cập nhật phat sinh. */
    public void setPhatSinh(String[] phatSinh) {
        this.phatSinh = Arrays.copyOf(phatSinh, phatSinh.length);
    }

    /** Trả về ket qua hoan tat. */
    public String[] getKetQuaHoanTat() {
        return Arrays.copyOf(ketQuaHoanTat, ketQuaHoanTat.length);
    }

    /** Kiểm tra và cập nhật ket qua hoan tat. */
    public void setKetQuaHoanTat(String[] ketQuaHoanTat) {
        this.ketQuaHoanTat = Arrays.copyOf(ketQuaHoanTat, ketQuaHoanTat.length);
    }
}