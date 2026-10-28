package ui.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Objects;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import entity.TaiKhoan;

/** Danh tính dùng ở header và cuối sidebar; không thay đổi quyền của phiên đăng nhập. */
public final class UserInfoPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    /**
     * Ghép avatar vai trò, tên đăng nhập và vai trò của phiên hiện tại.
     * @param taiKhoan tài khoản đã xác thực, không dùng tên nhân viên minh họa trong Figma
     * @param dark true khi đặt trên sidebar tối
     */
    public UserInfoPanel(TaiKhoan taiKhoan, boolean dark) {
        super(new BorderLayout(8, 0));
        Objects.requireNonNull(taiKhoan, "Cần tài khoản đã đăng nhập.");
        setOpaque(false);
        String kyHieu = switch (taiKhoan.getVaiTro()) {
            case QUAN_LY -> "QL";
            case NHAN_VIEN_TIEP_NHAN -> "TN";
            case NHAN_VIEN_KY_THUAT -> "KT";
            case THU_NGAN -> "TG";
        };
        // Avatar chỉ trình bày vai trò; quyền vẫn lấy từ tài khoản.
        JPanel pnlAvatar = UiTheme.roundedPanel(new BorderLayout(), new Color(0xB8CAD9), 16);
        pnlAvatar.setPreferredSize(new Dimension(32, 32));
        JLabel lblAvatar = new JLabel(kyHieu, SwingConstants.CENTER);
        lblAvatar.setFont(UiTheme.APP_BOLD_FONT.deriveFont(11.0f));
        lblAvatar.setForeground(new Color(0x183347));
        pnlAvatar.add(lblAvatar, BorderLayout.CENTER);
        add(pnlAvatar, BorderLayout.WEST);

        // Hai dòng tự tính kích thước theo font để giữ đủ dấu tiếng Việt.
        JPanel pnlDanhTinh = new JPanel();
        pnlDanhTinh.setOpaque(false);
        pnlDanhTinh.setLayout(new BoxLayout(pnlDanhTinh, BoxLayout.Y_AXIS));
        JLabel lblTen = new JLabel(taiKhoan.getTenDangNhap());
        lblTen.setFont(UiTheme.APP_BOLD_FONT.deriveFont(12.0f));
        lblTen.setForeground(dark ? Color.WHITE : UiTheme.APP_TEXT);
        JLabel lblVaiTro = new JLabel(taiKhoan.getVaiTro().getTenHienThi());
        lblVaiTro.setFont(UiTheme.APP_FONT.deriveFont(11.0f));
        lblVaiTro.setForeground(dark ? UiTheme.SIDEBAR_MUTED : UiTheme.APP_MUTED);
        pnlDanhTinh.add(lblTen);
        pnlDanhTinh.add(lblVaiTro);
        add(pnlDanhTinh, BorderLayout.CENTER);
    }
}
