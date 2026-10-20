package ui.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Sidebar dùng chung, chỉ có thương hiệu; vùng điều hướng ban đầu để trống. */
public final class SidebarPanel extends JPanel {

    /** Phiên bản tuần tự hóa theo lớp cha Swing; ứng dụng không lưu giao diện ra file. */
    private static final long serialVersionUID = 1L;

    /** Vùng dành cho các mục điều hướng do nhóm bổ sung sau. */
    private final JPanel pnlDieuHuong = new JPanel(new BorderLayout());

    /** Tạo sidebar cố định chiều rộng, co giãn chiều cao theo khung chính. */
    public SidebarPanel() {
        super(new BorderLayout(0, UiTheme.SPACE_MEDIUM));
        setBackground(UiTheme.NAVIGATION);
        setPreferredSize(new Dimension(UiTheme.SIDEBAR_WIDTH, 0));
        setBorder(BorderFactory.createEmptyBorder(UiTheme.SIDEBAR_PADDING, UiTheme.SIDEBAR_PADDING,
                UiTheme.SIDEBAR_PADDING, UiTheme.SIDEBAR_PADDING));

        // Thương hiệu là nội dung tĩnh; chưa có vai trò hoặc nút chức năng.
        JLabel lblThuongHieu = new JLabel("ĐỨC DŨNG");
        lblThuongHieu.setFont(UiTheme.BRAND_FONT);
        lblThuongHieu.setForeground(UiTheme.TEXT);
        lblThuongHieu.setPreferredSize(new Dimension(0, 20));
        add(lblThuongHieu, BorderLayout.NORTH);

        pnlDieuHuong.setOpaque(false);
        add(pnlDieuHuong, BorderLayout.CENTER);
    }

    /**
     * Thay vùng điều hướng bằng panel của nhóm, gọi trên EDT.
     * @param pnlMoi panel điều hướng cần gắn; nên dùng nền trong suốt
     * @throws NullPointerException nếu panel là null
     */
    public void setNavigation(JPanel pnlMoi) {
        Objects.requireNonNull(pnlMoi, "Panel điều hướng không được null.");
        pnlDieuHuong.removeAll();
        pnlDieuHuong.add(pnlMoi, BorderLayout.CENTER);
        pnlDieuHuong.revalidate();
        pnlDieuHuong.repaint();
    }
}
