package ui.component;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

/** Màu, font và khoảng cách dùng chung theo thiết kế Swing; không chứa nghiệp vụ. */
public final class UiTheme {

    /** Nền cửa sổ và vùng nội dung chưa gắn màn hình. */
    public static final Color WINDOW = new Color(0xF0F0F0);
    /** Nền sidebar theo thiết kế Figma. */
    public static final Color NAVIGATION = new Color(0xE9EEF4);
    /** Màu chữ chính. */
    public static final Color TEXT = new Color(0x202B38);
    /** Màu chữ phụ. */
    public static final Color MUTED = new Color(0x5A6878);
    /** Nền thanh tiêu đề theo Figma. */
    public static final Color SURFACE = Color.WHITE;
    /** Viền mảnh quanh cửa sổ theo Figma. */
    public static final Color BORDER = new Color(0xB8C2CF);
    /** Điểm nhấn thương hiệu trên thanh tiêu đề. */
    public static final Color ORANGE = new Color(0xF97316);
    /** Font mặc định 14 pt; nạp tài nguyên đóng gói cùng mã nguồn. */
    public static final Font BODY_FONT = loadFont("NotoSans-Regular.ttf", 14);
    /** Font 13 pt cho menu và tiêu đề cửa sổ, khớp Swing / Body trong Figma. */
    public static final Font SHELL_FONT = BODY_FONT.deriveFont(13.0f);
    /** Font nhãn thương hiệu trên sidebar. */
    public static final Font BRAND_FONT = loadFont("NotoSans-Bold.ttf", 14);
    /** Font nhỏ cho thanh trạng thái. */
    public static final Font CAPTION_FONT = BODY_FONT.deriveFont(11.0f);
    /** Khoảng cách nhỏ giữa các thành phần, tính bằng pixel. */
    public static final int SPACE_MEDIUM = 8;
    /** Lề sidebar, tính bằng pixel. */
    public static final int SIDEBAR_PADDING = 12;
    /** Lề vùng nội dung, tính bằng pixel. */
    public static final int CONTENT_PADDING = 16;
    /** Chiều rộng sidebar theo Figma, tính bằng pixel. */
    public static final int SIDEBAR_WIDTH = 180;
    /** Chiều cao thanh tiêu đề màu trắng, tính bằng pixel logic. */
    public static final int TITLE_HEIGHT = 32;
    /** Chiều cao menu theo nhịp 20 px nội dung và 3 px lề mỗi cạnh. */
    public static final int MENU_HEIGHT = 26;
    /** Mỗi nhóm menu gồm nhãn 92 px và khoảng trống 16 px theo Figma. */
    public static final int MENU_GROUP_WIDTH = 92 + 16;
    /** Chiều cao thanh trạng thái: 16 px nội dung và 3 px lề mỗi cạnh. */
    public static final int STATUS_HEIGHT = 22;

    /** Ngăn tạo đối tượng vì theme chỉ chứa cấu hình dùng chung. */
    private UiTheme() {
    }

    /** Áp dụng mặc định Swing trên EDT trước khi tạo cửa sổ. */
    public static void apply() {
        // Giữ Look and Feel sẵn có, chỉ thống nhất font và màu của khung chung.
        for (Object key : UIManager.getDefaults().keySet().toArray()) {
            if (UIManager.get(key) instanceof FontUIResource) {
                UIManager.put(key, new FontUIResource(BODY_FONT));
            }
        }
        UIManager.put("Panel.background", WINDOW);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("MenuBar.background", WINDOW);
    }

    /**
     * Tạo ô vuông cam 14 px được thiết kế trực tiếp trong Figma, không dùng icon Java mặc định.
     * @return ảnh thương hiệu cho cửa sổ và thanh tiêu đề
     */
    public static BufferedImage createWindowIcon() {
        // Đây là hình học đơn giản của thiết kế, không phải ảnh minh họa bên ngoài.
        BufferedImage icon = new BufferedImage(14, 14, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = icon.createGraphics();
        graphics.setColor(ORANGE);
        graphics.fillRect(0, 0, 14, 14);
        graphics.dispose();
        return icon;
    }

    /**
     * Nạp font Noto Sans để các máy thành viên hiển thị cùng kiểu chữ.
     * @param tenTep tên tài nguyên trong thư mục font cạnh lớp theme
     * @param kichThuoc cỡ chữ tính bằng pt
     * @return font ở kích thước yêu cầu
     * @throws IllegalStateException nếu font không được đóng gói hoặc không đọc được
     */
    private static Font loadFont(String tenTep, float kichThuoc) {
        try (InputStream fontStream = UiTheme.class.getResourceAsStream("font/" + tenTep)) {
            if (fontStream == null) {
                throw new IllegalStateException("Thiếu font giao diện: ui/component/font/" + tenTep);
            }
            return Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(kichThuoc);
        } catch (IOException | FontFormatException error) {
            throw new IllegalStateException("Không đọc được font giao diện: " + tenTep, error);
        }
    }
}
