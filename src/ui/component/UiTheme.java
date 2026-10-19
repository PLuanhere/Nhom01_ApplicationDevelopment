package ui.component;

import java.awt.Color;
import java.awt.Font;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

/** Màu và kích thước dùng chung cho khung Swing theo thiết kế Garage Đức Dũng. */
public final class UiTheme {
    public static final Color WINDOW = new Color(0xF0F0F0);
    public static final Color SURFACE = Color.WHITE;
    public static final Color NAVIGATION = new Color(0xE9EEF4);
    public static final Color PRIMARY = new Color(0x245FA8);
    public static final Color SELECTION = new Color(0xDCEAFE);
    public static final Color BORDER = new Color(0xB8C2CF);
    public static final Color TEXT = new Color(0x202B38);
    public static final Color MUTED = new Color(0x5A6878);

    public static final Font BODY = new Font(Font.DIALOG, Font.PLAIN, 14);
    public static final Font STRONG = BODY.deriveFont(Font.BOLD);
    public static final Font HEADING = BODY.deriveFont(Font.BOLD, 20f);
    public static final Font CAPTION = BODY.deriveFont(11f);

    public static final int SIDEBAR_WIDTH = 180;
    public static final int CONTROL_HEIGHT = 30;
    public static final int SPACE_SMALL = 4;
    public static final int SPACE_MEDIUM = 8;
    public static final int CONTENT_PADDING = 16;

    private static final Logger LOGGER = Logger.getLogger(UiTheme.class.getName());

    private UiTheme() {
    }

    /** Gọi một lần trên EDT trước khi tạo các component của ứng dụng. */
    public static void install() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException exception) {
            LOGGER.log(Level.WARNING, "Không thể cài giao diện Swing mặc định; giữ giao diện hiện tại.", exception);
        }

        UIDefaults defaults = UIManager.getDefaults();
        for (Object key : defaults.keySet().toArray()) {
            if (defaults.get(key) instanceof Font) {
                defaults.put(key, new FontUIResource(BODY));
            }
        }
        UIManager.put("Panel.background", new ColorUIResource(WINDOW));
        UIManager.put("Label.foreground", new ColorUIResource(TEXT));
        UIManager.put("Button.foreground", new ColorUIResource(TEXT));
        UIManager.put("Button.background", new ColorUIResource(SURFACE));
        UIManager.put("Button.disabledText", new ColorUIResource(MUTED));
        UIManager.put("Button.focus", new ColorUIResource(PRIMARY));
        UIManager.put("ToggleButton.select", new ColorUIResource(SELECTION));
        UIManager.put("ToggleButton.focus", new ColorUIResource(PRIMARY));
        UIManager.put("MenuBar.background", new ColorUIResource(WINDOW));
        UIManager.put("Menu.background", new ColorUIResource(WINDOW));
        UIManager.put("Menu.foreground", new ColorUIResource(TEXT));
        UIManager.put("MenuItem.background", new ColorUIResource(SURFACE));
        UIManager.put("MenuItem.foreground", new ColorUIResource(TEXT));
        UIManager.put("MenuItem.selectionBackground", new ColorUIResource(SELECTION));
        UIManager.put("MenuItem.selectionForeground", new ColorUIResource(TEXT));
        UIManager.put("ToolTip.font", new FontUIResource(CAPTION));
    }

    public static Border padding(int amount) {
        return BorderFactory.createEmptyBorder(amount, amount, amount, amount);
    }
}
