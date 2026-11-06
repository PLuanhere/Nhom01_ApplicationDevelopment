package ui.screen.thungan;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import java.util.function.Consumer;

import dao.HoaDonDAO;
import entity.TaiKhoan;
import entity.HoaDon;

/** Read-only list of issued and paid invoices — dung entity HoaDon thuc. */
public class PaidInvoicesPanel extends JPanel {
    private static final Color PAGE = new Color(244, 246, 249);
    private static final Color WHITE = Color.WHITE;
    private static final Color INK = new Color(32, 45, 61);
    private static final Color MUTED = new Color(100, 116, 137);
    private static final Color BORDER = new Color(233, 237, 242);
    private static final Color BLUE = new Color(34, 100, 166);

    private final HoaDonDAO dao;
    private final TaiKhoan taiKhoan;
    private final DefaultTableModel model;
    private final JTable table;
    private final JTextField searchField;
    private final JComboBox<String> statusFilter;
    private final JLabel resultLabel;
    private final JLabel footerLabel;
    private final Consumer<String> onViewInvoice;

    public PaidInvoicesPanel(HoaDonDAO dao, TaiKhoan taiKhoan, java.awt.event.ActionListener onViewInvoice) {
        this.dao = dao;
        this.taiKhoan = taiKhoan;
        this.onViewInvoice = ma -> {
            if (onViewInvoice != null) {
                onViewInvoice.actionPerformed(new java.awt.event.ActionEvent(this, java.awt.event.ActionEvent.ACTION_PERFORMED, ma));
            }
        };
        setLayout(new BorderLayout());
        setBackground(PAGE);
        setBorder(BorderFactory.createEmptyBorder(20, 26, 18, 26));
        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setOpaque(false);
        add(content, BorderLayout.NORTH);
        content.add(buildHeading(), BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);
        content.add(center, BorderLayout.CENTER);
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 14));
        toolbar.setBackground(WHITE);
        toolbar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(0, 14, 0, 14)));
        searchField = new PlaceholderTextField("Tìm mã hóa đơn, biển số hoặc khách hàng");
        searchField.setPreferredSize(new Dimension(380, 36));
        statusFilter = new JComboBox<>(new String[]{"Tat ca trang thai", "Da thanh toan"});
        statusFilter.setPreferredSize(new Dimension(150, 36));
        JButton refresh = secondaryButton("Lam moi");
        toolbar.add(searchField);
        toolbar.add(statusFilter);
        toolbar.add(refresh);
        center.add(toolbar, BorderLayout.NORTH);
        model = new DefaultTableModel(new Object[]{"Ma hoa don", "Phieu sua chua", "Khach hang", "Bien so", "Ngay thanh toan", "Phuong thuc", "Tong tien"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(52);
        table.setFont(new Font("Dialog", Font.PLAIN, 12));
        table.setForeground(new Color(50, 68, 90));
        table.setGridColor(BORDER);
        table.setShowVerticalLines(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setPreferredSize(new Dimension(0, 39));
        table.getTableHeader().setFont(new Font("Dialog", Font.BOLD, 11));
        table.getTableHeader().setForeground(new Color(86, 103, 126));
        table.getTableHeader().setBackground(new Color(248, 250, 252));
        table.getTableHeader().setReorderingAllowed(false);

        // Renderer với padding cho các cột text
        DefaultTableCellRenderer paddedRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, selected, focus, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 13, 0, 8));
                return this;
            }
        };

        // Apply padding renderer cho tất cả cột trừ cột tiền (column 6)
        for (int i = 0; i < 6; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(paddedRenderer);
        }
        table.getColumnModel().getColumn(6).setCellRenderer(new MoneyRenderer());
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(WHITE);
        resultLabel = label("", 11, Font.PLAIN, MUTED);
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createLineBorder(BORDER));
        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setBackground(WHITE);
        cardHeader.setBorder(BorderFactory.createEmptyBorder(14, 17, 14, 17));
        JPanel headingStack = new JPanel(new BorderLayout(0, 4));
        headingStack.setOpaque(false);
        headingStack.add(label("Danh sach hoa don da thanh toan", 15, Font.BOLD, INK), BorderLayout.NORTH);
        headingStack.add(label("Chon mot dong de xem, loc hoac mo tac vu lien quan.", 11, Font.PLAIN, MUTED), BorderLayout.SOUTH);
        cardHeader.add(headingStack, BorderLayout.WEST);
        cardHeader.add(resultLabel, BorderLayout.EAST);
        card.add(cardHeader, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        footerLabel = label("", 11, Font.PLAIN, MUTED);
        JPanel cardFooter = new JPanel(new BorderLayout());
        cardFooter.setBackground(WHITE);
        cardFooter.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER), BorderFactory.createEmptyBorder(11, 14, 11, 14)));
        cardFooter.add(footerLabel, BorderLayout.WEST);
        card.add(cardFooter, BorderLayout.SOUTH);
        center.add(card, BorderLayout.CENTER);
        refresh.addActionListener(e -> taiDuLieu());
        searchField.addActionListener(e -> taiDuLieu());
        statusFilter.addActionListener(e -> taiDuLieu());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0 && PaidInvoicesPanel.this.onViewInvoice != null) {
                    int row = table.getSelectedRow();
                    String maHoaDon = (String) table.getValueAt(row, 0);
                    PaidInvoicesPanel.this.onViewInvoice.accept(maHoaDon);
                }
            }
        });
        taiDuLieu();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        taiDuLieu();
    }

    private void taiDuLieu() {
        model.setRowCount(0);
        for (HoaDon hd : dao.layHoaDonDaThu(taiKhoan)) {
            String thoiDiem = hd.getThoiDiemThanhToan().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            String hinhThuc = hd.getHinhThucThanhToan().getTenHienThi();

            model.addRow(new Object[]{
                hd.getMaHoaDon(),
                hd.getMaPhieuSuaChua(),
                hd.getTenKhachHang(),
                hd.getBienSo(),
                thoiDiem,
                hinhThuc,
                hd.getTongTien()
            });
        }
        footerLabel.setText("Hien thi " + model.getRowCount() + " / " + model.getRowCount() + " ho so");
    }

    private JPanel buildHeading() {
        JPanel heading = new JPanel(new BorderLayout(0, 5));
        heading.setOpaque(false);

        heading.add(label("Thu ngân  /  Nghiệp vụ  /  Hoá đơn đã thanh toán", 11, Font.PLAIN, MUTED), BorderLayout.NORTH);

        JPanel titleBlock = new JPanel(new BorderLayout(0, 5));
        titleBlock.setOpaque(false);
        titleBlock.add(label("Hoá đơn đã thanh toán", 24, Font.BOLD, INK), BorderLayout.NORTH);
        titleBlock.add(label("Chứng từ đã phát hành chỉ được xem và in lại, không xóa/sửa trực tiếp.", 12, Font.PLAIN, MUTED), BorderLayout.SOUTH);
        heading.add(titleBlock, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setOpaque(false);
        JButton view = primaryButton("Xem / in hóa đơn");
        view.addActionListener(e -> {
            if (this.onViewInvoice != null) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    String maHoaDon = (String) table.getValueAt(row, 0);
                    this.onViewInvoice.accept(maHoaDon);
                } else {
                    javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn", "Thông báo", javax.swing.JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        btnPanel.add(view);
        heading.add(btnPanel, BorderLayout.EAST);

        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));
        return heading;
    }

    private static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Dialog", Font.BOLD, 12));
        b.setForeground(WHITE);
        b.setBackground(BLUE);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
        return b;
    }

    private static JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Dialog", Font.BOLD, 12));
        b.setForeground(new Color(49, 68, 91));
        b.setBackground(WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(8, 13, 8, 13)));
        return b;
    }

    private static JLabel label(String text, int size, int style, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Dialog", style, size));
        l.setForeground(color);
        return l;
    }

    private static class MoneyRenderer extends DefaultTableCellRenderer {
        MoneyRenderer() { setHorizontalAlignment(SwingConstants.RIGHT); }
        @Override public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int col) {
            String amount = String.format(Locale.forLanguageTag("vi-VN"), "%,dđ", ((Number) value).longValue()).replace(',', '.');
            super.getTableCellRendererComponent(t, amount, selected, focus, row, col);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 15));
            return this;
        }
    }

    private static class PlaceholderTextField extends JTextField {
        private final String placeholder;

        PlaceholderTextField(String placeholder) {
            this.placeholder = placeholder;
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner()) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(180, 190, 200));
                g2.setFont(getFont().deriveFont(java.awt.Font.ITALIC));
                java.awt.Insets ins = getInsets();
                g2.drawString(placeholder, ins.left + 2,
                        getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 2);
                g2.dispose();
            }
        }
    }
}
