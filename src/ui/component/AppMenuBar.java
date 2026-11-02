package ui.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/** Menu đầu trang; Hệ thống dùng được, các nhóm nghiệp vụ mới chỉ hiển thị nhãn. */
public final class AppMenuBar extends JMenuBar {

    /** Phiên bản tuần tự hóa theo lớp cha Swing. */
    private static final long serialVersionUID = 1L;

    /** Tạo menu từ các thao tác cửa sổ do khung chính cung cấp; gọi trên EDT. */
    public AppMenuBar(ActionListener khiDangXuat, ActionListener khiThoat) {
        this(khiDangXuat, khiThoat, null);
    }

    /** Gắn menu Báo cáo khi vai trò có quyền và màn hình đã được triển khai. */
    public AppMenuBar(ActionListener khiDangXuat, ActionListener khiThoat, ActionListener khiBaoCao) {
        if (khiDangXuat == null) {
            throw new NullPointerException("Cần thao tác đăng xuất.");
        }

        if (khiThoat == null) {
            throw new NullPointerException("Cần thao tác thoát ứng dụng.");
        }

        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setBorder(BorderFactory.createEmptyBorder());
        setBackground(UiTheme.SURFACE);
        add(taoMenuHeThong(khiDangXuat, khiThoat));

        // Giữ vị trí các nhóm còn lại, chưa tạo mục con khi chưa có chức năng.
        String[] nhomMenu = { "Danh mục", "Nghiệp vụ", "Báo cáo", "Trợ giúp" };
        for (String tenNhom : nhomMenu) {
            if (tenNhom.equals("Báo cáo") && khiBaoCao != null) {
                JMenu mnuBaoCao = new JMenu("Báo cáo");
                mnuBaoCao.setFont(UiTheme.APP_FONT);
                mnuBaoCao.setForeground(UiTheme.APP_TEXT);
                mnuBaoCao.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
                mnuBaoCao.setMnemonic(KeyEvent.VK_B);
                mnuBaoCao.add(taoMuc("Báo cáo và thống kê", KeyEvent.VK_T, khiBaoCao));
                add(mnuBaoCao);
                continue;
            }
            JLabel lblNhom = new JLabel(tenNhom);
            lblNhom.setFont(UiTheme.APP_FONT);
            lblNhom.setForeground(UiTheme.APP_TEXT);
            lblNhom.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
            lblNhom.setToolTipText("Nhóm menu chưa triển khai.");
            add(lblNhom);
        }
    }

    /** Tạo menu Hệ thống với hai thao tác đã có; Alt+H mở bằng bàn phím. */
    private JMenu taoMenuHeThong(ActionListener khiDangXuat, ActionListener khiThoat) {
        // Kích thước nhóm giữ nguyên để các nhãn bên cạnh không bị dịch chuyển.
        JMenu mnuHeThong = new JMenu("Hệ thống");
        mnuHeThong.setMnemonic(KeyEvent.VK_H);
        mnuHeThong.setFont(UiTheme.APP_BOLD_FONT);
        mnuHeThong.setForeground(UiTheme.APP_PRIMARY);
        mnuHeThong.setBackground(new Color(0xEEF4FA));
        mnuHeThong.setOpaque(true);
        mnuHeThong.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        mnuHeThong.getPopupMenu().setBackground(UiTheme.SURFACE);
        mnuHeThong.getPopupMenu().setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));
        mnuHeThong.add(taoMuc("Đăng xuất", KeyEvent.VK_D, khiDangXuat));
        mnuHeThong.addSeparator();
        mnuHeThong.add(taoMuc("Thoát", KeyEvent.VK_T, khiThoat));
        return mnuHeThong;
    }

    /** Tạo mục menu có nhãn, phím chọn và hành động được cung cấp. */
    private JMenuItem taoMuc(String tenMuc, int phimChon, ActionListener thaoTac) {
        // Hai mục dùng chung kiểu chữ và kích thước, chỉ khác hành động.
        JMenuItem mniMuc = new JMenuItem(tenMuc, phimChon);
        mniMuc.setFont(UiTheme.APP_FONT);
        mniMuc.setForeground(UiTheme.APP_TEXT);
        mniMuc.setBackground(UiTheme.SURFACE);
        mniMuc.setPreferredSize(new Dimension(180, 30));
        mniMuc.addActionListener(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                thaoTac.actionPerformed(new ActionEvent(thaoTac, ActionEvent.ACTION_PERFORMED, "thaoTac"));
            }
        });
        return mniMuc;
    }
}
