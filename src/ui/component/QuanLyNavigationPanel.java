package ui.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/** Hiển thị sidebar Quản lý và gửi sự kiện chọn màn hình cho MainFrame. */
public final class QuanLyNavigationPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final ButtonGroup nhomMuc = new ButtonGroup();
    private final List<JToggleButton> cacMuc = new ArrayList<>();

    /** Tạo các mục sidebar và gắn sự kiện chọn màn hình. */
    public QuanLyNavigationPanel(ActionListener khiChonMuc) {
        super(new BorderLayout());
        setOpaque(false);
        JPanel pnlDanhSach = new JPanel(new GridLayout(0, 1, 0, 2));
        pnlDanhSach.setOpaque(false);
        String[] ten = {"Tổng quan điều hành", "Nhân viên", "Tài khoản & phân quyền",
                "Dịch vụ và bảng giá", "Phụ tùng", "Báo cáo và thống kê"};
        String[] icon = {"dashboard", "users", "shield", "clipboard", "package", "chart"};
        for (int i = 0; i < ten.length; i++) {
            JToggleButton btn = taoMuc(ten[i], khiChonMuc);
            btn.setIcon(UiTheme.icon(icon[i]));
            pnlDanhSach.add(btn);
        }
        add(pnlDanhSach, BorderLayout.NORTH);
    }

    /** Chọn lại mục sidebar mà không phát thêm sự kiện điều hướng. */
    public void chonMuc(String tenMuc) {
        for (JToggleButton btn : cacMuc) {
            if (btn.getText().equals(tenMuc)) { btn.setSelected(true); return; }
        }
    }

    /** Tạo nút sidebar và gắn sự kiện chọn mục. */
    private JToggleButton taoMuc(String ten, ActionListener khiChonMuc) {
        final JToggleButton btn = new JToggleButton(ten);
        btn.setHorizontalAlignment(JToggleButton.LEFT);
        btn.setPreferredSize(new Dimension(0, 38));
        btn.setIconTextGap(9);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 6));
        btn.setFont(UiTheme.APP_FONT);
        btn.setForeground(UiTheme.SIDEBAR_TEXT);
        btn.setBackground(UiTheme.SIDEBAR_BACKGROUND);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        boolean daCo = "Nhân viên".equals(ten) || "Tài khoản & phân quyền".equals(ten)
                || "Dịch vụ và bảng giá".equals(ten) || "Phụ tùng".equals(ten);
        btn.setToolTipText(daCo ? "Mở " + ten.toLowerCase() : ten + ": chức năng chưa triển khai.");
        btn.getAccessibleContext().setAccessibleDescription(btn.getToolTipText());
        btn.addItemListener(new ItemListener() {
            /** Cập nhật nền, màu chữ và font của mục sidebar được chọn. */
            @Override public void itemStateChanged(ItemEvent event) {
                btn.setBackground(btn.isSelected() ? UiTheme.SIDEBAR_ACTIVE : UiTheme.SIDEBAR_BACKGROUND);
                btn.setForeground(btn.isSelected() ? Color.WHITE : UiTheme.SIDEBAR_TEXT);
                btn.setFont(UiTheme.APP_FONT.deriveFont(btn.isSelected() ? Font.BOLD : Font.PLAIN));
            }
        });
        btn.setActionCommand(ten);
        btn.addActionListener(khiChonMuc);
        nhomMuc.add(btn);
        cacMuc.add(btn);
        btn.setSelected(cacMuc.size() == 1);
        return btn;
    }
}
