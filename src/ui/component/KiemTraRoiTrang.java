package ui.component;

/**
 * Interface cho các màn hình hoặc biểu mẫu có trạng thái nhập liệu.
 * Cho phép kiểm tra thay đổi chưa lưu trước khi điều hướng hoặc thoát ứng dụng.
 */
public interface KiemTraRoiTrang {

    /**
     * Kiểm tra xem màn hình có cho phép rời đi không (đã lưu hoặc người dùng đồng ý bỏ thay đổi).
     *
     * @return true nếu cho phép rời trang, false nếu muốn giữ nguyên trạng thái để tiếp tục sửa.
     */
    boolean choPhepRoiTrang();
}
