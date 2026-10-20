package ui.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Sidebar dùng chung hiển thị thương hiệu, vai trò và vùng điều hướng để nhóm tích hợp. */
public final class SidebarPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    /** Vùng dành cho các mục điều hướng do nhóm bổ sung sau. */
    private final JPanel pnlDieuHuong = new JPanel(new BorderLayout());
    /** Vai trò của tài khoản đã đăng nhập, không tự chọn hoặc sửa trên sidebar. */
    private final JLabel lblVaiTro = new JLabel();

    /** Tạo sidebar cố định chiều rộng, co giãn chiều cao theo khung chính. */
    public SidebarPanel() {
        super(new BorderLayout(0, UiTheme.SPACE_MEDIUM));
        setBackground(UiTheme.NAVIGATION);
        setPreferredSize(new Dimension(UiTheme.SIDEBAR_WIDTH, 0));
        setBorder(BorderFactory.createEmptyBorder(UiTheme.SIDEBAR_PADDING, UiTheme.SIDEBAR_PADDING,
                UiTheme.SIDEBAR_PADDING, UiTheme.SIDEBAR_PADDING));

        JLabel lblThuongHieu = new JLabel("ĐỨC DŨNG");
        lblThuongHieu.setFont(UiTheme.BRAND_FONT);
        lblThuongHieu.setForeground(UiTheme.TEXT);
        lblThuongHieu.setPreferredSize(new Dimension(0, 20));
        JPanel pnlThuongHieu = new JPanel(new BorderLayout(0, 4));
        pnlThuongHieu.setOpaque(false);
        pnlThuongHieu.add(lblThuongHieu, BorderLayout.NORTH);
        lblVaiTro.setFont(UiTheme.CAPTION_FONT);
        lblVaiTro.setForeground(UiTheme.MUTED);
        pnlThuongHieu.add(lblVaiTro, BorderLayout.CENTER);
        add(pnlThuongHieu, BorderLayout.NORTH);

        pnlDieuHuong.setOpaque(false);
        add(pnlDieuHuong, BorderLayout.CENTER);
    }

    /**
     * Hiển thị vai trò do khung chính cung cấp sau xác thực, gọi trên EDT.
     * @param tenVaiTro nhãn tiếng Việt của vai trò
     * @throws NullPointerException nếu nhãn là null
     */
    public void setRoleText(String tenVaiTro) {
        lblVaiTro.setText(Objects.requireNonNull(tenVaiTro, "Tên vai trò không được null."));
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
