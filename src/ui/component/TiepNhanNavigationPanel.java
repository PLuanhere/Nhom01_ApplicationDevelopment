package ui.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

/** Điều hướng tiếp nhận; khung chính quyết định màn hình được phép mở. */
public final class TiepNhanNavigationPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final List<JToggleButton> cacMuc = new ArrayList<>();

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public TiepNhanNavigationPanel(ActionListener khiChon) {
        super(new BorderLayout());
        if (khiChon == null) {
            throw new NullPointerException("Cần thao tác điều hướng.");
        }

        setOpaque(false);
        JPanel danhSach = new JPanel(new GridLayout(0, 1, 0, 2));
        danhSach.setOpaque(false);
        String[] ten = { "Tổng quan tiếp nhận", "Phiếu tiếp nhận", "Khách hàng", "Phương tiện", "Báo giá",
                "Lệnh sửa chữa", "Bàn giao xe" };
        String[] icon = { "reception-home", "clipboard", "users", "reception-car", "reception-file", "reception-wrench",
                "reception-key" };
        ButtonGroup nhom = new ButtonGroup();
        for (int i = 0; i < ten.length; i++) {
            String muc = ten[i];
            JToggleButton nut = new JToggleButton(muc, UiTheme.icon(icon[i]));
            nut.setHorizontalAlignment(SwingConstants.LEFT);
            nut.setIconTextGap(9);
            nut.setPreferredSize(new Dimension(196, 38));
            nut.setContentAreaFilled(false);
            nut.setOpaque(true);
            nut.setFocusPainted(true);
            nut.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 4));
            nut.addItemListener(new ItemListener() {
                /** Thực hiện item state changed cho thành phần hiện tại. */
                @Override
                public void itemStateChanged(ItemEvent e) {
                    dinhDang(nut);
                }
            });
            nut.addActionListener(new ActionListener() {
                /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
                @Override
                public void actionPerformed(ActionEvent e) {
                    khiChon.actionPerformed(new ActionEvent(muc, ActionEvent.ACTION_PERFORMED, "thaoTac"));
                }
            });
            nhom.add(nut);
            nut.setName(muc);
            cacMuc.add(nut);
            dinhDang(nut);
            danhSach.add(nut);
        }
        add(danhSach, BorderLayout.NORTH);
        chonMuc("Tổng quan tiếp nhận");
    }

    /** Đồng bộ mục chọn khi mở từ dashboard; không phát thêm sự kiện điều hướng. */
    public void chonMuc(String ten) {
        JToggleButton nut = timMuc(ten);
        if (nut == null) {
            throw new IllegalArgumentException("Mục không tồn tại: " + ten);
        }
        nut.setSelected(true);
    }

    /** Thực hiện dinh dang cho thành phần hiện tại. */
    private void dinhDang(JToggleButton nut) {
        nut.setBackground(nut.isSelected() ? UiTheme.SIDEBAR_ACTIVE : UiTheme.SIDEBAR_BACKGROUND);
        nut.setForeground(nut.isSelected() ? Color.WHITE : UiTheme.SIDEBAR_TEXT);
        nut.setFont(nut.isSelected() ? UiTheme.APP_BOLD_FONT : UiTheme.APP_FONT);
    }

    /** Tìm nút sidebar bằng tên mục điều hướng. */
    private JToggleButton timMuc(String ten) {
        for (JToggleButton nut : cacMuc) {
            if (ten.equals(nut.getName())) {
                return nut;
            }
        }
        return null;
    }
}
