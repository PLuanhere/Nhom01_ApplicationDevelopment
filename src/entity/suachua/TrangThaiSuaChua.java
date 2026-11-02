package entity.suachua;

// Trạng thái thực hiện của lệnh và hạng mục sửa chữa.
public enum TrangThaiSuaChua {
    CHO_SUA_CHUA("Chờ sửa chữa"), DANG_SUA("Đang sửa"), CHO_PHU_TUNG("Chờ phụ tùng"), HOAN_THANH("Hoàn thành");

    private final String tenHienThi;

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    TrangThaiSuaChua(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    /** Trả về ten hien thi. */
    public String getTenHienThi() {
        return tenHienThi;
    }

    /** {@inheritDoc} */
    // Hiển thị nhãn trạng thái trong JComboBox.
    @Override
    public String toString() {
        return tenHienThi;
    }
}