package ui.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import entity.TaiKhoan;

/** Header Page 3 ghép menu hiện có, chỗ dành cho tìm nhanh/thông báo và danh tính đăng nhập. */
public final class AppHeaderPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    /** Các vùng giữ chỗ có thể ẩn khi cửa sổ hẹp, ưu tiên menu và danh tính. */
    private final JPanel pnlTimKiem;
    private final JButton btnThongBao;

    /**
     * Tạo header 66 px nằm bên phải sidebar; không tự bổ sung nghiệp vụ tìm kiếm/thông báo.
     * @param taiKhoan danh tính của phiên hiện tại
     * @param khiDangXuat thao tác đăng xuất đã có
     * @param khiThoat thao tác đóng cửa sổ
     */
    public AppHeaderPanel(TaiKhoan taiKhoan, Runnable khiDangXuat, Runnable khiThoat) {
        super(new BorderLayout(12, 0));
        setBackground(UiTheme.SURFACE);
        setPreferredSize(new Dimension(0, 66));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.APP_BORDER),
                BorderFactory.createEmptyBorder(0, 21, 0, 21)));
        JPanel pnlMenu = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 12));
        pnlMenu.setOpaque(false);
        pnlMenu.add(new AppMenuBar(khiDangXuat, khiThoat));
        add(pnlMenu, BorderLayout.WEST);

        JPanel pnlPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 11, 15));
        pnlPhai.setOpaque(false);
        pnlTimKiem = taoVungTimKiem();
        pnlPhai.add(pnlTimKiem);
        btnThongBao = UiTheme.createAppButton("Thông báo", false);
        btnThongBao.setFont(UiTheme.APP_FONT.deriveFont(12.0f));
        btnThongBao.setBorder(UiTheme.roundedBorder(UiTheme.APP_BORDER, 6, 6, 8));
        btnThongBao.setPreferredSize(new Dimension(81, 35));
        btnThongBao.setEnabled(false);
        btnThongBao.setToolTipText("Thông báo chưa được triển khai.");
        pnlPhai.add(btnThongBao);
        UserInfoPanel pnlNguoiDung = new UserInfoPanel(taiKhoan, false);
        pnlNguoiDung.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, UiTheme.APP_BORDER),
                BorderFactory.createEmptyBorder(0, 12, 0, 0)));
        pnlPhai.add(pnlNguoiDung);
        add(pnlPhai, BorderLayout.EAST);
    }

    /** {@inheritDoc} */
    @Override
    public void doLayout() {
        // Hai vùng chưa có chức năng không được che menu/đăng xuất ở kích thước 960×600.
        pnlTimKiem.setVisible(getWidth() >= 1000);
        btnThongBao.setVisible(getWidth() >= 830);
        super.doLayout();
    }

    /** @return vùng giữ chỗ của tìm kiếm nhanh, không tạo tìm kiếm giả */
    private JPanel taoVungTimKiem() {
        JPanel pnlTim = UiTheme.roundedPanel(new BorderLayout(7, 0), new Color(0xF6F8FA), 6);
        pnlTim.setPreferredSize(new Dimension(216, 35));
        pnlTim.setBorder(UiTheme.roundedBorder(UiTheme.APP_BORDER, 6, 0, 10));
        pnlTim.add(new JLabel(UiTheme.icon("search")), BorderLayout.WEST);
        JLabel lblGoiY = new JLabel("Tìm phiếu / biển số...");
        lblGoiY.setFont(UiTheme.APP_FONT.deriveFont(12.0f));
        lblGoiY.setForeground(UiTheme.APP_MUTED);
        pnlTim.add(lblGoiY, BorderLayout.CENTER);
        pnlTim.setToolTipText("Tìm kiếm nhanh chưa được triển khai.");
        return pnlTim;
    }
}
