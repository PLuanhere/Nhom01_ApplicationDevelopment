package ui.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.GridLayout;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/** Các mục Quản lý đã có trong Page 3; mới đổi trạng thái chọn, chưa tạo màn nghiệp vụ. */
public final class QuanLyNavigationPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private final ButtonGroup nhomMuc = new ButtonGroup();

    /**
     * Tạo danh sách mục, mặc định chọn Tổng quan điều hành; gọi trên EDT.
     * @param khiChonMuc thao tác thông báo mục vừa được chọn
     */
    public QuanLyNavigationPanel(Consumer<String> khiChonMuc) {
        super(new BorderLayout(0, UiTheme.SPACE_MEDIUM));
        Objects.requireNonNull(khiChonMuc, "Cần thao tác khi chọn mục.");
        setOpaque(false);

        // Các hàng cao bằng nhau, bám phía trên và không giãn theo cửa sổ.
        JPanel pnlDanhSach = new JPanel(new GridLayout(0, 1, 0, 2));
        pnlDanhSach.setOpaque(false);
        String[] tenCacMuc = {"Tổng quan điều hành", "Nhân viên", "Tài khoản & phân quyền",
                "Dịch vụ và bảng giá", "Phụ tùng", "Phiếu nhập kho", "Báo cáo và thống kê"};
        String[] tenIcon = {"dashboard", "users", "shield", "clipboard", "package", "truck", "chart"};
        for (int viTri = 0; viTri < tenCacMuc.length; viTri++) {
            JToggleButton tglMuc = taoMuc(tenCacMuc[viTri], khiChonMuc);
            tglMuc.setIcon(UiTheme.icon(tenIcon[viTri]));
            tglMuc.setIconTextGap(9);
            pnlDanhSach.add(tglMuc);
        }
        add(pnlDanhSach, BorderLayout.NORTH);

    }

    /* Tạo nút chọn mục, cập nhật nền/chữ và báo tên mục khi được kích hoạt. */
    private JToggleButton taoMuc(String tenMuc, Consumer<String> khiChonMuc) {
        JToggleButton btnMuc = new JToggleButton(tenMuc) {
            private static final long serialVersionUID = 1L;

            /** {@inheritDoc} */
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(getBackground());
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        btnMuc.setHorizontalAlignment(JToggleButton.LEFT);
        btnMuc.setPreferredSize(new Dimension(0, 38));
        btnMuc.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 6));
        btnMuc.setFont(UiTheme.APP_FONT);
        btnMuc.setForeground(UiTheme.SIDEBAR_TEXT);
        btnMuc.setBackground(UiTheme.SIDEBAR_BACKGROUND);
        btnMuc.setContentAreaFilled(false);
        btnMuc.setOpaque(false);
        btnMuc.setToolTipText(tenMuc + ": chức năng chưa triển khai.");
        btnMuc.getAccessibleContext().setAccessibleDescription(btnMuc.getToolTipText());
        btnMuc.addItemListener(event -> {
            btnMuc.setBackground(btnMuc.isSelected() ? UiTheme.SIDEBAR_ACTIVE : UiTheme.SIDEBAR_BACKGROUND);
            btnMuc.setForeground(btnMuc.isSelected() ? Color.WHITE : UiTheme.SIDEBAR_TEXT);
            btnMuc.setFont(UiTheme.APP_FONT.deriveFont(btnMuc.isSelected() ? Font.BOLD : Font.PLAIN));
        });
        btnMuc.addActionListener(event -> khiChonMuc.accept(tenMuc));
        nhomMuc.add(btnMuc);
        btnMuc.setSelected(nhomMuc.getButtonCount() == 1);
        return btnMuc;
    }
}
