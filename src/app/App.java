package app;

import javax.swing.SwingUtilities;

import ui.LoginFrame;
import ui.component.UiTheme;

/** Điểm chạy ứng dụng: áp dụng theme và mở trang đăng nhập trên EDT. */
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
            LoginFrame winDangNhap = new LoginFrame();
            winDangNhap.setVisible(true);
        });
    }
}
