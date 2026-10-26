package ui.component;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/** Menu đầu trang; Hệ thống dùng được, các nhóm nghiệp vụ mới chỉ hiển thị nhãn. */
public final class AppMenuBar extends JMenuBar {

    /** Phiên bản tuần tự hóa theo lớp cha Swing. */
    private static final long serialVersionUID = 1L;

    /**
     * Tạo menu từ các thao tác cửa sổ do khung chính cung cấp; gọi trên EDT.
     * @param khiDangXuat thao tác kết thúc phiên và mở lại đăng nhập
     * @param khiThoat thao tác đóng ứng dụng như nút đóng cửa sổ
     * @throws NullPointerException nếu một trong hai thao tác là null
     */
    public AppMenuBar(Runnable khiDangXuat, Runnable khiThoat) {
        Objects.requireNonNull(khiDangXuat, "Cần thao tác đăng xuất.");
        Objects.requireNonNull(khiThoat, "Cần thao tác thoát ứng dụng.");
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        setBackground(UiTheme.WINDOW);
        setPreferredSize(new Dimension(0, UiTheme.MENU_HEIGHT));
        add(taoMenuHeThong(khiDangXuat, khiThoat));

        // Giữ vị trí các nhóm còn lại, chưa tạo mục con khi chưa có chức năng.
        String[] nhomMenu = {"Danh mục", "Nghiệp vụ", "Thống kê", "Trợ giúp"};
        for (String tenNhom : nhomMenu) {
            JLabel lblNhom = new JLabel(tenNhom);
            lblNhom.setFont(UiTheme.SHELL_FONT);
            lblNhom.setForeground(UiTheme.TEXT);
            lblNhom.setPreferredSize(new Dimension(UiTheme.MENU_GROUP_WIDTH, 20));
            lblNhom.setToolTipText("Nhóm menu chưa triển khai.");
            add(lblNhom);
        }
    }

    /**
     * Tạo menu Hệ thống với hai thao tác đã có; Alt+H mở bằng bàn phím.
     * @param khiDangXuat thao tác đăng xuất dùng chung với sidebar
     * @param khiThoat thao tác đóng cửa sổ
     * @return menu đã gắn hành động
     */
    private JMenu taoMenuHeThong(Runnable khiDangXuat, Runnable khiThoat) {
        // Kích thước nhóm giữ nguyên để các nhãn bên cạnh không bị dịch chuyển.
        JMenu mnuHeThong = new JMenu("Hệ thống");
        mnuHeThong.setMnemonic(KeyEvent.VK_H);
        mnuHeThong.setFont(UiTheme.SHELL_FONT);
        mnuHeThong.setForeground(UiTheme.TEXT);
        mnuHeThong.setBorder(BorderFactory.createEmptyBorder());
        mnuHeThong.setPreferredSize(new Dimension(UiTheme.MENU_GROUP_WIDTH, 20));
        mnuHeThong.getPopupMenu().setBackground(UiTheme.SURFACE);
        mnuHeThong.getPopupMenu().setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));
        mnuHeThong.add(taoMuc("Đăng xuất", KeyEvent.VK_D, khiDangXuat));
        mnuHeThong.addSeparator();
        mnuHeThong.add(taoMuc("Thoát", KeyEvent.VK_T, khiThoat));
        return mnuHeThong;
    }

    /**
     * Tạo mục menu có nhãn, phím chọn và hành động được cung cấp.
     * @param tenMuc nhãn tiếng Việt
     * @param phimChon ký tự chọn mục khi menu đang mở
     * @param thaoTac hành động khi kích hoạt
     * @return mục menu đã định dạng
     */
    private JMenuItem taoMuc(String tenMuc, int phimChon, Runnable thaoTac) {
        // Hai mục dùng chung kiểu chữ và kích thước, chỉ khác hành động.
        JMenuItem mniMuc = new JMenuItem(tenMuc, phimChon);
        mniMuc.setFont(UiTheme.SHELL_FONT);
        mniMuc.setForeground(UiTheme.TEXT);
        mniMuc.setBackground(UiTheme.SURFACE);
        mniMuc.setPreferredSize(new Dimension(180, 30));
        mniMuc.addActionListener(event -> thaoTac.run());
        return mniMuc;
    }
}
