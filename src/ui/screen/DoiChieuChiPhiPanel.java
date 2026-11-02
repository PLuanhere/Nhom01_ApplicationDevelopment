package ui.screen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import dao.DoiChieuChiPhiDAO;
import dao.DoiChieuChiPhiDAO.DongChiPhi;
import dao.DoiChieuChiPhiDAO.KetQuaDoiChieu;
import entity.HinhThucThanhToan;
import ui.component.UiTheme;

/**
 * Màn hình đối chiếu chi phí trước khi thu tiền.
 */
public final class DoiChieuChiPhiPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Color PAGE   = new Color(244, 246, 249);
    private static final Color WHITE  = Color.WHITE;
    private static final Color INK    = new Color(28, 41, 59);
    private static final Color MUTED  = new Color(99, 115, 135);
    private static final Color BORDER = new Color(217, 227, 237);
    private static final Color BLUE   = new Color(34, 100, 166);

    private final Consumer<HinhThucThanhToan> onChoosePayment;
    private final Runnable onBack;

    public DoiChieuChiPhiPanel() {
        this(new DoiChieuChiPhiDAO(), "SC-2026-0108", ht -> {}, () -> {});
    }

    public DoiChieuChiPhiPanel(DoiChieuChiPhiDAO dao, String maPhieu,
            Consumer<HinhThucThanhToan> onChoosePayment, Runnable onBack) {
        this.onChoosePayment = onChoosePayment != null ? onChoosePayment : ht -> {};
        this.onBack          = onBack          != null ? onBack          : () -> {};

        setLayout(new BorderLayout());
        setBackground(PAGE);

        KetQuaDoiChieu kq;
        try {
            kq = (dao != null ? dao : new DoiChieuChiPhiDAO())
                    .layKetQuaDoiChieu(maPhieu != null ? maPhieu : "SC-2026-0108");
        } catch (IllegalArgumentException e) {
            add(buildErrorPanel(e.getMessage()), BorderLayout.CENTER);
            return;
        }

        // Heading cố định phía trên, không scroll
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(20, 26, 0, 26));
        header.add(buildHeading(), BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // Card + bottom + action bar gom chung trong vùng scroll
        JPanel cardWrap = new JPanel(new BorderLayout(0, 0));
        cardWrap.setOpaque(false);
        cardWrap.setBorder(BorderFactory.createEmptyBorder(14, 26, 18, 26));
        cardWrap.add(buildReviewCard(kq), BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(cardWrap);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(PAGE);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    // ── Heading ──────────────────────────────────────────────────────────────

    private JPanel buildHeading() {
        JPanel heading = new JPanel(new BorderLayout(0, 5));
        heading.setOpaque(false);

        heading.add(label("Thu ngân  /  Nghiệp vụ  /  Đối chiếu chi phí", 11, Font.PLAIN, MUTED),
                BorderLayout.NORTH);

        JPanel titleBlock = new JPanel(new BorderLayout(0, 5));
        titleBlock.setOpaque(false);
        titleBlock.add(label("Đối chiếu chi phí", 24, Font.BOLD, INK), BorderLayout.NORTH);
        titleBlock.add(label("Đối chiếu chi phí dịch vụ, tiền công, phụ tùng thực tế và thuế nếu có.",
                12, Font.PLAIN, MUTED), BorderLayout.SOUTH);
        heading.add(titleBlock, BorderLayout.CENTER);

        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));
        return heading;
    }

    // ── Card chính ────────────────────────────────────────────────────────────

    private JPanel buildReviewCard(KetQuaDoiChieu kq) {
        JPanel card = new JPanel();
        card.setLayout(new javax.swing.BoxLayout(card, javax.swing.BoxLayout.Y_AXIS));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);
        top.setAlignmentX(LEFT_ALIGNMENT);
        top.add(label("Đối chiếu chi phí trước khi thu tiền", 18, Font.BOLD, INK), BorderLayout.NORTH);
        top.add(label("Chỉ đối chiếu phiếu đã hoàn tất sửa chữa; không tự lập hóa đơn khi chưa xác nhận.",
                12, Font.PLAIN, MUTED), BorderLayout.CENTER);
        JPanel facts = new JPanel(new GridLayout(1, 3, 12, 0));
        facts.setOpaque(false);
        facts.add(fact("Lệnh sửa chữa",        kq.getNhanPhieu()));
        facts.add(fact("Khách / xe",            kq.getNhanKhach()));
        facts.add(fact("Trạng thái thanh toán", kq.getTrangThaiThanhToan()));
        top.add(facts, BorderLayout.SOUTH);
        card.add(top);
        card.add(javax.swing.Box.createVerticalStrut(12));

        JPanel middle = buildMiddle(kq);
        middle.setAlignmentX(LEFT_ALIGNMENT);
        card.add(middle);
        card.add(javax.swing.Box.createVerticalStrut(12));

        JPanel bottom = buildBottom(kq);
        bottom.setAlignmentX(LEFT_ALIGNMENT);
        card.add(bottom);
        card.add(javax.swing.Box.createVerticalStrut(12));

        // Action bar gom chung trong card — không scale chiều dọc riêng
        JPanel actionBar = new JPanel(new java.awt.GridBagLayout());
        actionBar.setOpaque(false);
        actionBar.setAlignmentX(LEFT_ALIGNMENT);
        actionBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                BorderFactory.createEmptyBorder(12, 0, 0, 0)));

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.gridy = 0;
        gbc.anchor = java.awt.GridBagConstraints.CENTER;

        JLabel next = new JLabel("Bước tiếp theo: Tiền mặt hoặc Chuyển khoản / QR");
        next.setFont(new Font("Dialog", Font.PLAIN, 13));
        next.setForeground(MUTED);
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.insets = new java.awt.Insets(0, 0, 0, 12);
        actionBar.add(next, gbc);

        JButton choosePayment = new JButton("Chọn hình thức thanh toán");
        choosePayment.setFont(new Font("Dialog", Font.BOLD, 13));
        choosePayment.setForeground(WHITE);
        choosePayment.setBackground(BLUE);
        choosePayment.setOpaque(true);
        choosePayment.setContentAreaFilled(true);
        choosePayment.setBorderPainted(false);
        choosePayment.setFocusPainted(false);
        choosePayment.setBorder(BorderFactory.createEmptyBorder(11, 20, 11, 20));
        choosePayment.addActionListener(e -> {
            Window win = javax.swing.SwingUtilities.getWindowAncestor(this);
            ui.dialog.ChonHinhThucThanhToanDialog dlg =
                    new ui.dialog.ChonHinhThucThanhToanDialog(win);
            dlg.setVisible(true);
            entity.HinhThucThanhToan ht = dlg.getKetQua();
            if (ht != null) {
                onChoosePayment.accept(ht);
            }
        });
        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.fill = java.awt.GridBagConstraints.NONE;
        gbc.insets = new java.awt.Insets(0, 0, 0, 0);
        actionBar.add(choosePayment, gbc);

        card.add(actionBar);

        return card;
    }

    private JPanel buildMiddle(KetQuaDoiChieu kq) {
        JPanel middle = new JPanel(new BorderLayout(0, 9));
        middle.setOpaque(false);
        middle.add(label("Bảng chi phí hợp lệ", 15, Font.BOLD, INK), BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Hạng mục", "SL / ĐVT", "Đơn giá", "Thành tiền"}, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        List<DongChiPhi> ds = kq.getDanhSachChiPhi();
        for (DongChiPhi d : ds) {
            model.addRow(new Object[]{
                d.getHangMuc(), d.getSlDvt(),
                d.getDonGia().longValue(), d.getThanhTien().longValue()
            });
        }

        JTable table = new JTable(model);
        table.setRowHeight(39);
        table.setFont(new Font("Dialog", Font.PLAIN, 13));
        table.setForeground(INK);
        table.setGridColor(new Color(233, 237, 242));
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, 39));
        table.getTableHeader().setFont(new Font("Dialog", Font.BOLD, 12));
        table.getTableHeader().setForeground(MUTED);
        table.getTableHeader().setBackground(new Color(245, 248, 252));
        table.getTableHeader().setReorderingAllowed(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getColumnModel().getColumn(2).setCellRenderer(new MoneyRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(new MoneyRenderer());

        // Tính đúng preferred height = header + tất cả dòng, không scroll dọc
        int headerH = table.getTableHeader().getPreferredSize().height;
        int bodyH   = table.getRowHeight() * model.getRowCount();
        int totalH  = headerH + bodyH;
        table.setPreferredScrollableViewportSize(
                new Dimension(table.getPreferredSize().width, bodyH));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        // +2 cho 2px border top+bottom của lineBorder
        scroll.setPreferredSize(new Dimension(scroll.getPreferredSize().width, totalH + 2));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, totalH + 2));
        middle.add(scroll, BorderLayout.CENTER);
        return middle;
    }

    private JPanel buildBottom(KetQuaDoiChieu kq) {
        JPanel bottom = new JPanel();
        bottom.setLayout(new javax.swing.BoxLayout(bottom, javax.swing.BoxLayout.Y_AXIS));
        bottom.setOpaque(false);

        JPanel totals = new JPanel(new GridLayout(1, 2, 12, 0));
        totals.setOpaque(false);
        totals.setAlignmentX(LEFT_ALIGNMENT);
        totals.add(readOnlyField("Tổng chi phí thực tế · tự tính · chỉ đọc",
                formatMoney(kq.getTongThucTe().longValue()) + "đ"));
        totals.add(readOnlyField("Thuế / điều chỉnh · chỉ đọc",
                "Theo chính sách chưa chốt; không tự cộng"));
        bottom.add(totals);
        bottom.add(javax.swing.Box.createVerticalStrut(10));

        JLabel note = new JLabel("Đối chiếu phụ tùng đã dùng và dịch vụ thực hiện. Nếu sai lệch, chuyển lại bộ phận phụ trách để xác nhận.");
        note.setOpaque(true);
        note.setBackground(new Color(245, 248, 252));
        note.setForeground(MUTED);
        note.setFont(new Font("Dialog", Font.PLAIN, 12));
        note.setBorder(BorderFactory.createEmptyBorder(11, 11, 11, 11));
        note.setAlignmentX(LEFT_ALIGNMENT);
        // Giới hạn chiều cao bằng preferred size — không để BoxLayout kéo giãn
        note.setMaximumSize(new Dimension(Integer.MAX_VALUE, note.getPreferredSize().height + 22));
        bottom.add(note);

        return bottom;
    }

    // ── Lỗi ──────────────────────────────────────────────────────────────────

    private JPanel buildErrorPanel(String message) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        JLabel lbl = label("Không thể tải dữ liệu: " + message, 13, Font.PLAIN, UiTheme.ERROR);
        lbl.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        pnl.add(lbl, BorderLayout.NORTH);
        return pnl;
    }

    // ── Tiện ích ──────────────────────────────────────────────────────────────

    private JPanel fact(String title, String value) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        panel.add(label(title, 12, Font.PLAIN, MUTED), BorderLayout.NORTH);
        panel.add(label(value, 14, Font.BOLD,  INK),   BorderLayout.SOUTH);
        return panel;
    }

    private JPanel readOnlyField(String title, String value) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        panel.add(label(title, 12, Font.PLAIN, MUTED), BorderLayout.NORTH);
        JLabel field = label(value, 14, Font.BOLD, INK);
        field.setOpaque(true);
        field.setBackground(new Color(248, 250, 252));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(11, 10, 11, 10)));
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private static JLabel label(String text, int size, int style, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Dialog", style, size));
        lbl.setForeground(color);
        return lbl;
    }

    private static String formatMoney(long value) {
        return String.format(Locale.forLanguageTag("vi-VN"), "%,d", value).replace(',', '.');
    }

    private static class ScrollablePanel extends JPanel implements javax.swing.Scrollable {
        @Override public java.awt.Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(java.awt.Rectangle r, int o, int d) { return 16; }
        @Override public int getScrollableBlockIncrement(java.awt.Rectangle r, int o, int d) { return 60; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    private static class MoneyRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;
        MoneyRenderer() { setHorizontalAlignment(SwingConstants.RIGHT); }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focus, int row, int column) {
            String formatted = formatMoney(((Number) value).longValue()) + "đ";
            super.getTableCellRendererComponent(table, formatted, selected, focus, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 14));
            return this;
        }
    }
}

