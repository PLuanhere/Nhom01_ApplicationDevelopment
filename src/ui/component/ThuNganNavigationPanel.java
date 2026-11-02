package ui.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/** Điều hướng vai trò Thu ngân theo thiết kế Page 3. */
public final class ThuNganNavigationPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private final ButtonGroup nhomMuc = new ButtonGroup();

    /** Tạo danh sách mục, mặc định chọn Trang chủ; gọi trên EDT. */
    public ThuNganNavigationPanel(ActionListener khiChonMuc) {
        super(new BorderLayout(0, UiTheme.SPACE_MEDIUM));
        if (khiChonMuc == null) {
            throw new NullPointerException("Cần thao tác khi chọn mục.");
        }

        setOpaque(false);

        JPanel pnlDanhSach = new JPanel(new GridLayout(0, 1, 0, 2));
        pnlDanhSach.setOpaque(false);

        String[] tenCacMuc = { "Trung tâm thanh toán", "Phiếu chờ thu", "Đối chiếu chi phí", "Hoá đơn đã thanh toán" };
        String[] tenIcon = { "chart", "clipboard", "search", "package" };

        for (int i = 0; i < tenCacMuc.length; i++) {
            JToggleButton tglMuc = taoMuc(tenCacMuc[i], khiChonMuc);
            tglMuc.setIcon(UiTheme.icon(tenIcon[i]));
            tglMuc.setIconTextGap(9);
            pnlDanhSach.add(tglMuc);
        }

        add(pnlDanhSach, BorderLayout.NORTH);
    }

    /** Đồng bộ mục chọn khi mở từ bên ngoài; không phát thêm sự kiện điều hướng. */
    public void chonMuc(String tenMuc) {
        Enumeration<AbstractButton> cacNut = nhomMuc.getElements();
        while (cacNut.hasMoreElements()) {
            AbstractButton nut = cacNut.nextElement();
            if (nut.getText().equals(tenMuc)) {
                nut.setSelected(true);
                return;
            }
        }
        throw new IllegalArgumentException("Mục không tồn tại: " + tenMuc);
    }

    // ── Tạo nút ──────────────────────────────────────────────────────────────

    /** Tạo nút chọn mục bo góc, cập nhật nền/chữ và báo tên mục khi kích hoạt. */
    private JToggleButton taoMuc(String tenMuc, ActionListener khiChonMuc) {
        JToggleButton btnMuc = new JToggleButton(tenMuc) {
            private static final long serialVersionUID = 1L;

            /** Vẽ nền bo góc thay nền vuông mặc định của Swing. */
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
        btnMuc.getAccessibleContext().setAccessibleDescription("Mở màn hình " + tenMuc);

        btnMuc.addItemListener(new ItemListener() {
            /** Thực hiện item state changed cho thành phần hiện tại. */
            @Override
            public void itemStateChanged(ItemEvent e) {
                btnMuc.setBackground(btnMuc.isSelected() ? UiTheme.SIDEBAR_ACTIVE : UiTheme.SIDEBAR_BACKGROUND);
                btnMuc.setForeground(btnMuc.isSelected() ? Color.WHITE : UiTheme.SIDEBAR_TEXT);
                btnMuc.setFont(UiTheme.APP_FONT.deriveFont(btnMuc.isSelected() ? Font.BOLD : Font.PLAIN));
            }
        });
        btnMuc.addActionListener(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent e) {
                khiChonMuc.actionPerformed(new ActionEvent(tenMuc, ActionEvent.ACTION_PERFORMED, "thaoTac"));
            }
        });

        nhomMuc.add(btnMuc);
        // Mục đầu tiên mặc định được chọn.
        if (nhomMuc.getButtonCount() == 1) {
            btnMuc.setSelected(true);
            btnMuc.setBackground(UiTheme.SIDEBAR_ACTIVE);
            btnMuc.setForeground(Color.WHITE);
            btnMuc.setFont(UiTheme.APP_FONT.deriveFont(Font.BOLD));
        }
        return btnMuc;
    }
}
