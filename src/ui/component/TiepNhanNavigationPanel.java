package ui.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.*;

/** Điều hướng tiếp nhận; khung chính quyết định màn hình được phép mở. */
public final class TiepNhanNavigationPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final Map<String, JToggleButton> cacMuc = new LinkedHashMap<>();

    /** @param khiChon thao tác điều hướng trên EDT, dùng chung với đường tắt */
    public TiepNhanNavigationPanel(Consumer<String> khiChon) {
        super(new BorderLayout());
        Objects.requireNonNull(khiChon, "Cần thao tác điều hướng.");
        setOpaque(false);
        JPanel danhSach = new JPanel(new GridLayout(0, 1, 0, 2));
        danhSach.setOpaque(false);
        String[] ten = {"Tổng quan tiếp nhận", "Phiếu tiếp nhận", "Khách hàng", "Phương tiện",
                "Báo giá", "Lệnh sửa chữa", "Bàn giao xe"};
        String[] icon = {"reception-home", "clipboard", "users", "car", "reception-file",
                "reception-wrench", "reception-key"};
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
            nut.addItemListener(e -> dinhDang(nut));
            nut.addActionListener(e -> khiChon.accept(muc));
            nhom.add(nut);
            cacMuc.put(muc, nut);
            dinhDang(nut);
            danhSach.add(nut);
        }
        add(danhSach, BorderLayout.NORTH);
        chonMuc("Tổng quan tiếp nhận");
    }

    /** Đồng bộ mục chọn khi mở từ dashboard; không phát thêm sự kiện điều hướng. */
    public void chonMuc(String ten) {
        JToggleButton nut = cacMuc.get(ten);
        if (nut == null) throw new IllegalArgumentException("Mục không tồn tại: " + ten);
        nut.setSelected(true);
    }

    private void dinhDang(JToggleButton nut) {
        nut.setBackground(nut.isSelected() ? UiTheme.SIDEBAR_ACTIVE : UiTheme.SIDEBAR_BACKGROUND);
        nut.setForeground(nut.isSelected() ? Color.WHITE : UiTheme.SIDEBAR_TEXT);
        nut.setFont(nut.isSelected() ? UiTheme.APP_BOLD_FONT : UiTheme.APP_FONT);
    }
}
