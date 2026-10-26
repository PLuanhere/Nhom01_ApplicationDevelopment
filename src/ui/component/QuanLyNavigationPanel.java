package ui.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

public final class QuanLyNavigationPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private final ButtonGroup nhomMuc = new ButtonGroup();

    /**
     * Tạo danh sách mục, mặc định chọn Trang chủ; gọi trên EDT.
     */
    public QuanLyNavigationPanel(Consumer<String> khiChonMuc) {
        super(new BorderLayout(0, UiTheme.SPACE_MEDIUM));
        Objects.requireNonNull(khiChonMuc, "Cần thao tác khi chọn mục.");
        setOpaque(false);

        // Các hàng cao bằng nhau, bám phía trên và không giãn theo cửa sổ.
        JPanel pnlDanhSach = new JPanel(new GridLayout(0, 1, 0, 2));
        pnlDanhSach.setOpaque(false);
        String[] tenCacMuc = {"Trang chủ", "Nhân viên", "Tài khoản", "Dịch vụ / bảng giá",
                "Phụ tùng", "Phiếu nhập kho", "Thống kê", "Lịch sử thao tác"};
        for (String tenMuc : tenCacMuc) {
            pnlDanhSach.add(taoMuc(tenMuc, khiChonMuc));
        }
        add(pnlDanhSach, BorderLayout.NORTH);

    }

    /* Tạo nút chọn mục, cập nhật nền/chữ và báo tên mục khi được kích hoạt. */
    private JToggleButton taoMuc(String tenMuc, Consumer<String> khiChonMuc) {
        JToggleButton btnMuc = new JToggleButton(tenMuc);
        btnMuc.setHorizontalAlignment(JToggleButton.LEFT);
        btnMuc.setPreferredSize(new Dimension(0, 32));
        btnMuc.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 4));
        btnMuc.setFont(UiTheme.SHELL_FONT);
        btnMuc.setForeground(UiTheme.TEXT);
        btnMuc.setBackground(UiTheme.NAVIGATION);
        btnMuc.setContentAreaFilled(false);
        btnMuc.setOpaque(true);
        btnMuc.setToolTipText(tenMuc + ": chức năng chưa triển khai.");
        btnMuc.getAccessibleContext().setAccessibleDescription(btnMuc.getToolTipText());
        btnMuc.addItemListener(event -> {
            btnMuc.setBackground(btnMuc.isSelected() ? UiTheme.NAVIGATION_SELECTED : UiTheme.NAVIGATION);
            btnMuc.setFont(UiTheme.SHELL_FONT.deriveFont(btnMuc.isSelected() ? Font.BOLD : Font.PLAIN));
        });
        btnMuc.addActionListener(event -> khiChonMuc.accept(tenMuc));
        nhomMuc.add(btnMuc);
        btnMuc.setSelected(nhomMuc.getButtonCount() == 1);
        return btnMuc;
    }
}
