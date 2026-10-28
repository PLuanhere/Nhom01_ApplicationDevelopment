package ui.component;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.plaf.FontUIResource;

/** Màu, font và khoảng cách dùng chung theo thiết kế Swing; không chứa nghiệp vụ. */
public final class UiTheme {

    /** Nền cửa sổ và vùng nội dung chưa gắn màn hình. */
    public static final Color WINDOW = new Color(0xF0F0F0);
    /** Nền sidebar theo thiết kế Figma. */
    public static final Color NAVIGATION = new Color(0xE9EEF4);
    /** Nền xanh nhạt đánh dấu mục sidebar đang chọn. */
    public static final Color NAVIGATION_SELECTED = new Color(0xDCEAFE);
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
    /** Màu nút chính theo Figma. */
    public static final Color PRIMARY = new Color(0x245FA8);
    /** Màu thông báo lỗi, luôn đi cùng nội dung hướng dẫn. */
    public static final Color ERROR = new Color(0xB42318);
    /** Font mặc định 14 pt; nạp tài nguyên đóng gói cùng mã nguồn. */
    public static final Font BODY_FONT = loadFont("NotoSans-Regular.ttf", 14);
    /** Font 13 pt cho menu và tiêu đề cửa sổ, khớp Swing / Body trong Figma. */
    public static final Font SHELL_FONT = BODY_FONT.deriveFont(13.0f);
    /** Font nhãn thương hiệu trên sidebar. */
    public static final Font BRAND_FONT = loadFont("NotoSans-Bold.ttf", 14);
    /** Font tiêu đề màn hình đăng nhập. */
    public static final Font HEADING_FONT = BRAND_FONT.deriveFont(20.0f);
    /** Font nhỏ cho thanh trạng thái. */
    public static final Font CAPTION_FONT = BODY_FONT.deriveFont(11.0f);
    /** Khoảng cách nhỏ giữa các thành phần, tính bằng pixel. */
    public static final int SPACE_MEDIUM = 8;
    /** Lề sidebar, tính bằng pixel. */
    public static final int SIDEBAR_PADDING = 12;
    /** Lề vùng nội dung, tính bằng pixel. */
    public static final int CONTENT_PADDING = 16;
    /** Chiều rộng sidebar theo Figma, tính bằng pixel. */
    public static final int SIDEBAR_WIDTH = 216;
    /** Chiều cao thanh tiêu đề màu trắng, tính bằng pixel logic. */
    public static final int TITLE_HEIGHT = 32;
    /** Chiều cao menu theo nhịp 20 px nội dung và 3 px lề mỗi cạnh. */
    public static final int MENU_HEIGHT = 26;
    /** Mỗi nhóm menu gồm nhãn 92 px và khoảng trống 16 px theo Figma. */
    public static final int MENU_GROUP_WIDTH = 92 + 16;
    /** Chiều cao thanh trạng thái: 16 px nội dung và 3 px lề mỗi cạnh. */
    public static final int STATUS_HEIGHT = 22;

    // Màu Page 3 dành cho khung chung; giữ theme cũ của các màn thành viên đang dùng.
    public static final Color APP_BACKGROUND = new Color(0xF4F6F9);
    public static final Color APP_TEXT = new Color(0x202D3D);
    public static final Color APP_MUTED = new Color(0x647489);
    public static final Color APP_BORDER = new Color(0xDCE3EC);
    public static final Color APP_PRIMARY = new Color(0x2264A6);
    public static final Color SIDEBAR_BACKGROUND = new Color(0x172634);
    public static final Color SIDEBAR_ACTIVE = new Color(0x31506A);
    public static final Color SIDEBAR_TEXT = new Color(0xCBD9E6);
    public static final Color SIDEBAR_MUTED = new Color(0x95A8BA);
    /** Font Windows theo Design System Page 3, không thay font của các màn nghiệp vụ đã tích hợp. */
    public static final Font APP_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font APP_BOLD_FONT = APP_FONT.deriveFont(Font.BOLD);

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
     * Áp dụng nút phẳng dùng chung cho đăng nhập, đăng xuất và thao tác phụ.
     * @param btn nút cần định dạng
     * @param primary true cho nút chính nền xanh, false cho nút phụ nền trắng
     */
    public static void styleButton(JButton btn, boolean primary) {
        btn.setFont(SHELL_FONT);
        btn.setBackground(primary ? PRIMARY : SURFACE);
        btn.setForeground(primary ? SURFACE : TEXT);
        btn.setBorder(BorderFactory.createLineBorder(primary ? PRIMARY : BORDER));
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setFocusPainted(true);
    }

    /**
     * Định dạng nút trong layout và đăng nhập theo Page 3.
     * @param text nhãn của nút
     * @param primary true cho nút chính nền xanh
     * @return nút bo góc, vẫn dùng sự kiện và phím tắt chuẩn Swing
     */
    public static JButton createAppButton(String text, boolean primary) {
        JButton btn = new JButton(text) {
            private static final long serialVersionUID = 1L;

            /** {@inheritDoc} */
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color color = isEnabled() ? getBackground() : APP_BACKGROUND;
                g.setColor(getModel().isPressed() ? color.darker() : color);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        btn.setFont(APP_BOLD_FONT);
        btn.setBackground(primary ? APP_PRIMARY : SURFACE);
        btn.setForeground(primary ? SURFACE : APP_TEXT);
        btn.setBorder(roundedBorder(primary ? APP_PRIMARY : APP_BORDER, 6, 8, 12));
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFocusPainted(true);
        return btn;
    }

    /**
     * Tạo bề mặt bo góc dùng chung cho thẻ đăng nhập và vùng tìm kiếm.
     * @param layout layout manager bố trí các thành phần con
     * @param color màu nền
     * @param radius bán kính góc tính bằng pixel
     * @return panel đã vẽ nền, không chứa logic nghiệp vụ
     */
    public static JPanel roundedPanel(LayoutManager layout, Color color, int radius) {
        JPanel pnl = new JPanel(layout) {
            private static final long serialVersionUID = 1L;

            /** {@inheritDoc} */
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(getBackground());
                g.fillRoundRect(0, 0, getWidth(), getHeight(), radius * 2, radius * 2);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        pnl.setOpaque(false);
        pnl.setBackground(color);
        return pnl;
    }

    /**
     * Tạo viền bo góc và khoảng đệm, hỗ trợ hiển thị focus trên ô nhập.
     * @param color màu viền bình thường
     * @param radius bán kính góc
     * @param vertical khoảng đệm trên/dưới
     * @param horizontal khoảng đệm trái/phải
     * @return viền dùng cho nút, thẻ và ô nhập
     */
    public static Border roundedBorder(Color color, int radius, int vertical, int horizontal) {
        return new AbstractBorder() {
            private static final long serialVersionUID = 1L;

            /** {@inheritDoc} */
            @Override
            public Insets getBorderInsets(Component component) {
                return new Insets(vertical, horizontal, vertical, horizontal);
            }

            /** {@inheritDoc} */
            @Override
            public void paintBorder(Component component, Graphics graphics, int x, int y, int width, int height) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(component.hasFocus() ? APP_PRIMARY : color);
                g.drawRoundRect(x, y, width - 1, height - 1, radius * 2, radius * 2);
                g.dispose();
            }
        };
    }

    /**
     * Đọc ảnh Figma đã đóng gói trong classpath; không truy cập mạng khi chạy ứng dụng.
     * @param name tên ảnh trong thư mục image cạnh UiTheme
     * @return ảnh gốc giữ nguyên kích thước
     * @throws IllegalStateException nếu thiếu ảnh hoặc ảnh không hợp lệ
     */
    public static BufferedImage readImage(String name) {
        try (InputStream stream = UiTheme.class.getResourceAsStream("image/" + name)) {
            if (stream == null) {
                throw new IllegalStateException("Thiếu ảnh giao diện: " + name);
            }
            BufferedImage image = ImageIO.read(stream);
            if (image == null) {
                throw new IllegalStateException("Ảnh giao diện không hợp lệ: " + name);
            }
            return image;
        } catch (IOException error) {
            throw new IllegalStateException("Không đọc được ảnh giao diện: " + name, error);
        }
    }

    /**
     * Tạo icon từ ảnh Figma ở kích thước gốc.
     * @param name tên ảnh trong classpath
     * @return icon dùng cho nhãn hoặc nút
     */
    public static ImageIcon icon(String name) {
        ImageIcon icon = new ImageIcon(readImage(name + ".png"));
        icon.setDescription(name);
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
