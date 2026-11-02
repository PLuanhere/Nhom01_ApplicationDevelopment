package app;

import javax.swing.SwingUtilities;

import ui.LoginFrame;
import ui.component.UiTheme;

/** Điểm chạy ứng dụng Garage Đức Dũng. */
public final class App {

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    private App() {
    }

    /** Khởi tạo giao diện Swing và mở trang đăng nhập. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            /** Khởi tạo hoặc cập nhật giao diện trên luồng Swing. */
            @Override
            public void run() {
                UiTheme.apply();
                LoginFrame winDangNhap = new LoginFrame();
                winDangNhap.setVisible(true);
            }
        });
    }
}
