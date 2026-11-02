package entity.tiepnhan;

/** Trạng thái tiếp nhận hiện có; các bước kiểm tra/báo giá bổ sung khi nối workflow. */
public enum TrangThaiTiepNhan {
    NHAP("Nháp"), CHO_KIEM_TRA("Chờ kiểm tra");

    private final String tenHienThi;

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    TrangThaiTiepNhan(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    /** Trả về ten hien thi. */
    public String getTenHienThi() {
        return tenHienThi;
    }
}
