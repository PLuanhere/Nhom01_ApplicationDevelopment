package app;

import javax.swing.SwingUtilities;

import ui.MainFrame;
import ui.component.UiTheme;
/** Điểm chạy bản xem trước giao diện, chưa khởi tạo nghiệp vụ hoặc database. */
public final class App {
    private App() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UiTheme.install();
            MainFrame frame = new MainFrame("demo.nhom01", "Quản lý");
            frame.setVisible(true);
        });
    }
}
