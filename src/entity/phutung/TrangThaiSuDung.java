package entity.phutung;

// Trạng thái ghi nhận phụ tùng; chỉ DAO được chuyển nháp sang đã ghi nhận
public enum TrangThaiSuDung {
    NHAP("Nháp"), DA_GHI_NHAN("Đã ghi nhận");

    private final String tenHienThi;

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    TrangThaiSuDung(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    /** Trả về ten hien thi. */
    public String getTenHienThi() {
        return tenHienThi;
    }
}