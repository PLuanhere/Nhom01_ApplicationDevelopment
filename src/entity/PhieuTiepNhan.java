package entity;

// Thông tin phiếu được giao kiểm tra, chỉ đọc trong phân hệ kiểm tra.
public final class PhieuTiepNhan {
    private final String maPhieu;
    private final String bienSo;
    private final String yeuCau;
    private final String tinhTrang;
    private final String taiKhoanKyThuat;
    private final boolean daXacNhan;

    public PhieuTiepNhan(String maPhieu, String bienSo, String yeuCau, String tinhTrang,
            String taiKhoanKyThuat, boolean daXacNhan) {
        if (maPhieu == null || bienSo == null || taiKhoanKyThuat == null
                || yeuCau == null || tinhTrang == null || maPhieu.trim().isEmpty()) {
            throw new IllegalArgumentException("Thông tin phiếu tiếp nhận không hợp lệ.");
        }
        this.maPhieu = maPhieu;
        this.bienSo = bienSo;
        this.yeuCau = yeuCau;
        this.tinhTrang = tinhTrang;
        this.taiKhoanKyThuat = taiKhoanKyThuat;
        this.daXacNhan = daXacNhan;
    }

    public String getMaPhieu() { return maPhieu; }
    public String getBienSo() { return bienSo; }
    public String getYeuCau() { return yeuCau; }
    public String getTinhTrang() { return tinhTrang; }
    public String getTaiKhoanKyThuat() { return taiKhoanKyThuat; }
    public boolean isDaXacNhan() { return daXacNhan; }

    /** {@inheritDoc} */
    // Hiển thị mã phiếu và trạng thái xác nhận trong danh sách chọn.
    @Override
    public String toString() {
        return maPhieu + (daXacNhan ? " · Đã xác nhận" : " · Nháp");
    }
}