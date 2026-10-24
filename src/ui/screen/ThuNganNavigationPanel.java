package ui.screen;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import ui.component.UiTheme;

/**
 * Panel điều hướng dành cho vai trò Thu ngân.
 *
 * <p>Hiển thị ba mục trang (Trang chủ, Thanh toán, Tra cứu chứng từ),
 * một đường kẻ phân cách rồi hai nút hành động (Đổi vai trò, Đăng xuất).
 * Gắn vào sidebar qua {@code MainFrame.setSidebarContent()}.</p>
 *
 * <p>Không chứa SQL hoặc nghiệp vụ thanh toán.</p>
 */
public final class ThuNganNavigationPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    /** Màu nền mục đang được chọn. */
    private static final Color ACTIVE_BG = new Color(0xD0DCE9);
    /** Màu chữ mục đang được chọn. */
    private static final Color ACTIVE_FG = new Color(0x1A4A8A);
    /** Bán kính bo góc mỗi mục, tính bằng pixel. */
    private static final int ITEM_RADIUS = 6;
    /** Chiều cao mỗi mục điều hướng, tính bằng pixel. */
    private static final int ITEM_HEIGHT = 36;

    /** Ánh xạ trang → nút để cập nhật trạng thái active. */
    private final Map<ThuNganShellPage, JButton> anhXaNut = new EnumMap<>(ThuNganShellPage.class);
    /** Trang hiện tại; mặc định là TRANG_CHU ngay sau khi tạo. */
    private ThuNganShellPage trangHienTai = ThuNganShellPage.TRANG_CHU;

    /**
     * Tạo panel điều hướng và gắn callback.
     *
     * @param khiChonTrang  callback nhận trang mới khi người dùng nhấn mục nav
     * @param khiDoiVaiTro  callback kích hoạt luồng đổi vai trò
     * @param khiDangXuat   callback kết thúc phiên và quay màn hình đăng nhập
     * @throws NullPointerException nếu bất kỳ callback nào là null
     */
    public ThuNganNavigationPanel(
            Consumer<ThuNganShellPage> khiChonTrang,
            Runnable khiDoiVaiTro,
            Runnable khiDangXuat) {
        super();
        Objects.requireNonNull(khiChonTrang, "Callback chọn trang không được null.");
        Objects.requireNonNull(khiDoiVaiTro, "Callback đổi vai trò không được null.");
        Objects.requireNonNull(khiDangXuat, "Callback đăng xuất không được null.");

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        // ── Nhóm trang ──────────────────────────────────────────────────────
        for (ThuNganShellPage trang : ThuNganShellPage.values()) {
            JButton btn = taoMucNav(trang.getTenHienThi());
            btn.addActionListener(e -> {
                setTrangHienTai(trang);
                khiChonTrang.accept(trang);
            });
            anhXaNut.put(trang, btn);
            add(btn);
            add(Box.createVerticalStrut(4));
        }

        // ── Đường kẻ phân cách ──────────────────────────────────────────────
        add(Box.createVerticalStrut(8));
        add(taoDuongKe());
        add(Box.createVerticalStrut(8));

        // ── Đổi vai trò ─────────────────────────────────────────────────────
        JButton btnDoiVaiTro = taoNutHanhDong("Đổi vai trò");
        btnDoiVaiTro.addActionListener(e -> khiDoiVaiTro.run());
        add(btnDoiVaiTro);
        add(Box.createVerticalStrut(4));

        // ── Đăng xuất ───────────────────────────────────────────────────────
        JButton btnDangXuat = taoNutHanhDong("Đăng xuất");
        btnDangXuat.addActionListener(e -> khiDangXuat.run());
        add(btnDangXuat);

        // Đánh dấu mục đầu tiên ngay sau khi tạo.
        capNhatTrangThaiNut();
    }

    /**
     * Đánh dấu trang được chọn theo chương trình mà không kích hoạt callback.
     * Gọi khi {@code ThuNganShell} cần đồng bộ trạng thái sau điều hướng nội bộ.
     *
     * @param trang trang cần đánh dấu active
     * @throws NullPointerException nếu trang là null
     */
    public void setTrangHienTai(ThuNganShellPage trang) {
        trangHienTai = Objects.requireNonNull(trang, "Trang không được null.");
        capNhatTrangThaiNut();
    }

    // ── Tạo thành phần ──────────────────────────────────────────────────────

    /**
     * Tạo nút điều hướng bo góc; nền và chữ thay đổi khi active.
     *
     * @param nhan nhãn hiển thị
     * @return nút chưa gắn sự kiện nghiệp vụ
     */
    private JButton taoMucNav(String nhan) {
        // Vẽ đè paintComponent để có nền bo góc thay nền vuông mặc định của Swing.
        JButton btn = new JButton(nhan) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), ITEM_RADIUS * 2, ITEM_RADIUS * 2);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(UiTheme.BODY_FONT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, ITEM_HEIGHT));
        btn.setPreferredSize(new Dimension(0, ITEM_HEIGHT));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        return btn;
    }

    /**
     * Tạo nút hành động (Đổi vai trò, Đăng xuất) với kiểu phụ, không đổi màu active.
     *
     * @param nhan nhãn nút
     * @return nút chưa gắn sự kiện
     */
    private JButton taoNutHanhDong(String nhan) {
        JButton btn = taoMucNav(nhan);
        btn.setFont(UiTheme.SHELL_FONT);
        btn.setForeground(UiTheme.MUTED);
        // Màu trong suốt để lộ nền sidebar.
        btn.setBackground(new Color(0, 0, 0, 0));
        return btn;
    }

    /**
     * Tạo đường kẻ ngang phân cách giữa nhóm trang và nhóm hành động.
     *
     * @return separator chiều ngang chiếm toàn chiều rộng
     */
    private JSeparator taoDuongKe() {
        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setForeground(UiTheme.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(LEFT_ALIGNMENT);
        return sep;
    }

    // ── Cập nhật trạng thái ─────────────────────────────────────────────────

    /** Làm mới màu nền, màu chữ và font của mọi nút theo {@code trangHienTai}. */
    private void capNhatTrangThaiNut() {
        anhXaNut.forEach((trang, btn) -> {
            boolean active = trang == trangHienTai;
            btn.setBackground(active ? ACTIVE_BG : new Color(0, 0, 0, 0));
            btn.setForeground(active ? ACTIVE_FG : UiTheme.TEXT);
            btn.setFont(active ? UiTheme.BODY_FONT.deriveFont(Font.BOLD) : UiTheme.BODY_FONT);
            btn.repaint();
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Panel hoàn toàn trong suốt; màu nền do SidebarPanel cung cấp.
        setOpaque(false);
        super.paintComponent(g);
    }
}
