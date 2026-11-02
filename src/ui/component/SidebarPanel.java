package ui.component;

import entity.nhansu.TaiKhoan;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Sidebar dùng chung hiển thị thương hiệu, vai trò và vùng điều hướng để nhóm tích hợp. */
public final class SidebarPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    /** Vùng dành cho các mục điều hướng do nhóm bổ sung sau. */
    private final JPanel pnlDieuHuong = new JPanel(new BorderLayout());
    /** Vai trò của tài khoản đã đăng nhập, không tự chọn hoặc sửa trên sidebar. */
    private final JLabel lblVaiTro = new JLabel();
    /** Danh tính của phiên hiển thị cuối sidebar. */
    private final JPanel pnlTaiKhoan = new JPanel(new BorderLayout());

    /** Tạo sidebar cố định chiều rộng, co giãn chiều cao theo khung chính. */
    public SidebarPanel() {
        super(new BorderLayout(0, 14));
        setBackground(UiTheme.SIDEBAR_BACKGROUND);
        setPreferredSize(new Dimension(UiTheme.SIDEBAR_WIDTH, 0));
        setBorder(BorderFactory.createEmptyBorder(18, 10, 14, 10));
        add(taoThuongHieu(), BorderLayout.NORTH);
        pnlDieuHuong.setOpaque(false);
        add(pnlDieuHuong, BorderLayout.CENTER);
        pnlTaiKhoan.setOpaque(false);
        pnlTaiKhoan.setBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0x354654)),
                        BorderFactory.createEmptyBorder(12, 9, 0, 9)));
        add(pnlTaiKhoan, BorderLayout.SOUTH);
    }

    /** Hiển thị vai trò do khung chính cung cấp sau xác thực, gọi trên EDT. */
    public void setRoleText(String tenVaiTro) {
        if (tenVaiTro == null) {
            throw new NullPointerException("Tên vai trò không được null.");
        }
        lblVaiTro.setText(tenVaiTro.toUpperCase());
    }

    /** Hiện danh tính đã xác thực, không dùng tên nhân viên minh họa của Figma. */
    public void setAccount(TaiKhoan taiKhoan) {
        pnlTaiKhoan.removeAll();
        pnlTaiKhoan.add(new UserInfoPanel(taiKhoan, true));
        pnlTaiKhoan.revalidate();
        pnlTaiKhoan.repaint();
    }

    /** Thay vùng điều hướng bằng panel của nhóm, gọi trên EDT. */
    public void setNavigation(JPanel pnlMoi) {
        if (pnlMoi == null) {
            throw new NullPointerException("Panel điều hướng không được null.");
        }

        dinhDangDieuHuong(pnlMoi);
        pnlDieuHuong.removeAll();
        pnlDieuHuong.add(pnlMoi, BorderLayout.CENTER);
        pnlDieuHuong.revalidate();
        pnlDieuHuong.repaint();
    }

    /** Tạo thuong hieu cho giao diện. */
    private JPanel taoThuongHieu() {
        JPanel pnlDau = new JPanel(new BorderLayout(0, 14));
        pnlDau.setOpaque(false);
        JPanel pnlThuongHieu = new JPanel(new BorderLayout(10, 0));
        pnlThuongHieu.setOpaque(false);
        pnlThuongHieu.setBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x314453)),
                        BorderFactory.createEmptyBorder(6, 10, 18, 0)));
        JLabel lblIcon = new JLabel(UiTheme.icon("car"));
        lblIcon.setPreferredSize(new Dimension(32, 32));
        lblIcon.setBorder(UiTheme.roundedBorder(new Color(0x547080), 6, 2, 2));
        JPanel pnlIcon = new JPanel(new GridBagLayout());
        pnlIcon.setOpaque(false);
        pnlIcon.add(lblIcon);
        pnlThuongHieu.add(pnlIcon, BorderLayout.WEST);
        JPanel pnlChu = new JPanel();
        pnlChu.setLayout(new BoxLayout(pnlChu, BoxLayout.Y_AXIS));
        pnlChu.setOpaque(false);
        JLabel lblTen = new JLabel("<html>GARAGE ĐỨC<br>DŨNG</html>");
        lblTen.setFont(UiTheme.APP_BOLD_FONT);
        lblTen.setForeground(Color.WHITE);
        JLabel lblMoTa = new JLabel("Quản lý dịch vụ sửa chữa");
        lblMoTa.setFont(UiTheme.APP_FONT.deriveFont(10.5f));
        lblMoTa.setForeground(UiTheme.SIDEBAR_MUTED);
        pnlChu.add(lblTen);
        pnlChu.add(lblMoTa);
        pnlThuongHieu.add(pnlChu, BorderLayout.CENTER);
        pnlDau.add(pnlThuongHieu, BorderLayout.CENTER);
        lblVaiTro.setFont(UiTheme.APP_FONT.deriveFont(11.0f));
        lblVaiTro.setForeground(UiTheme.SIDEBAR_MUTED);
        lblVaiTro.setBorder(BorderFactory.createEmptyBorder(3, 12, 2, 0));
        pnlDau.add(lblVaiTro, BorderLayout.SOUTH);
        return pnlDau;
    }

    /** Đồng bộ nền/chữ khi gắn điều hướng, giữ nguyên listener của nhóm. */
    private void dinhDangDieuHuong(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JPanel) {
                ((JPanel) component).setOpaque(false);
            }
            if (component instanceof AbstractButton) {
                boolean dangChon = ((AbstractButton) component).isSelected()
                        || UiTheme.NAVIGATION_SELECTED.equals(((AbstractButton) component).getBackground());
                ((AbstractButton) component)
                        .setBackground(dangChon ? UiTheme.SIDEBAR_ACTIVE : UiTheme.SIDEBAR_BACKGROUND);
                ((AbstractButton) component).setForeground(dangChon ? Color.WHITE : UiTheme.SIDEBAR_TEXT);
                ((AbstractButton) component).setFont(dangChon ? UiTheme.APP_BOLD_FONT : UiTheme.APP_FONT);
            }
            if (component instanceof Container) {
                dinhDangDieuHuong(((Container) component));
            }
        }
    }
}
