package app;

import javax.swing.SwingUtilities;

import ui.LoginFrame;
import ui.component.UiTheme;

public final class App {

    private App() {
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UiTheme.apply();
            LoginFrame winDangNhap = new LoginFrame();
            winDangNhap.setVisible(true);
        });
    }
}
