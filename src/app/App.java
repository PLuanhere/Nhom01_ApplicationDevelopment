package app;

import javax.swing.SwingUtilities;

import ui.MainFrame;
import ui.component.UiTheme;

/** Điểm khởi chạy layout chung; chưa khởi tạo đăng nhập hoặc kết nối dữ liệu. */
public final class App {

    /** Ngăn tạo đối tượng vì lớp chỉ cung cấp điểm chạy ứng dụng. */
    private App() {
    }

    /**
     * Khởi tạo giao diện trên Event Dispatch Thread.
     * @param args tham số dòng lệnh, hiện chưa sử dụng
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UiTheme.apply();
            // Khung chính là cửa sổ duy nhất của bản layout chung.
            MainFrame winChinh = new MainFrame();
            winChinh.setVisible(true);
        });
    }
}
