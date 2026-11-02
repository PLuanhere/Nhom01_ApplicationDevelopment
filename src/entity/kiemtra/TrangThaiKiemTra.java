package entity.kiemtra;

// Trạng thái nghiệp vụ của báo cáo kiểm tra.
public enum TrangThaiKiemTra {
    NHAP("Nháp"), HOAN_TAT("Hoàn tất");

    private final String tenHienThi;

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    TrangThaiKiemTra(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    /** Trả về ten hien thi. */
    public String getTenHienThi() {
        return tenHienThi;
    }
}