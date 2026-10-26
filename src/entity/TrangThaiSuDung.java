package entity;

// Trạng thái ghi nhận phụ tùng; chỉ DAO được chuyển nháp sang đã ghi nhận
public enum TrangThaiSuDung {
    NHAP("Nháp"), DA_GHI_NHAN("Đã ghi nhận");

    private final String tenHienThi;

    TrangThaiSuDung(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }
}