package entity;

// Trạng thái nghiệp vụ của báo cáo kiểm tra.
public enum TrangThaiKiemTra {
    NHAP("Nháp"), HOAN_TAT("Hoàn tất");

    private final String tenHienThi;

    TrangThaiKiemTra(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }
}