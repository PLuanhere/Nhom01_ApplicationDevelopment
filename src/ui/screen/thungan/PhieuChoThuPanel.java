package ui.screen.thungan;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import dao.DoiChieuChiPhiDAO;
import dao.HoaDonDAO;
import entity.TaiKhoan;
import ui.component.UiTheme;

/** Content panel for the cashier's "Phiếu chờ thu" screen. */
public class PhieuChoThuPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Color PAGE    = new Color(244, 246, 249);
    private static final Color WHITE   = Color.WHITE;
    private static final Color INK     = new Color(27, 62, 102);
    private static final Color MUTED   = new Color(100, 116, 137);
    private static final Color BORDER  = new Color(233, 237, 242);
    private static final Color BLUE    = new Color(34, 100, 166);
    private static final Color GOLD_BG = new Color(255, 243, 221);
    private static final Color GOLD    = new Color(155, 99, 7);

    private final HoaDonDAO dao;
    private TaiKhoan taiKhoan;
    private final DoiChieuChiPhiDAO doiChieuDAO;
    private final DefaultTableModel model;
    private final JTable table;
    private final JLabel resultCount;
    private final JLabel footerCount;
    private final JTextField searchField;
    private TableRowSorter<DefaultTableModel> sorter;
    /** Callback nhận mã phiếu khi bấm "Đối chiếu chi phí". */
    private final Consumer<String> onCreateInvoice;
    private final Runnable onViewDetails;

    public PhieuChoThuPanel() {
        this((HoaDonDAO) null, (Consumer<String>) null, (Runnable) null);
    }

    /**
     * @param dao DAO Thu ngân; null dùng DAO mặc định
     * @param onCreateInvoice callback nhận mã phiếu đang chọn; null = no-op
     * @param onViewDetails   callback xem chi tiết; null = no-op
     */
    public PhieuChoThuPanel(HoaDonDAO dao, TaiKhoan taiKhoan, ui.MainFrame mainFrame) {
        this(dao, ma -> mainFrame.moDoiChieuChiPhi(ma), () -> {});
        this.taiKhoan = taiKhoan;
    }

    private PhieuChoThuPanel(HoaDonDAO dao, Consumer<String> onCreateInvoice, Runnable onViewDetails) {
        this.dao = dao != null ? dao : new HoaDonDAO();
        this.taiKhoan = new TaiKhoan("NV007", TaiKhoan.VaiTro.THU_NGAN);
        this.doiChieuDAO = new DoiChieuChiPhiDAO();
        this.onCreateInvoice = onCreateInvoice;
        this.onViewDetails   = onViewDetails;

        setLayout(new BorderLayout());
        setBackground(PAGE);
        setBorder(BorderFactory.createEmptyBorder(20, 26, 18, 26));

        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setOpaque(false);
        add(content, BorderLayout.CENTER);

        content.add(buildHeading(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);
        content.add(center, BorderLayout.CENTER);

        searchField  = new PlaceholderTextField("Tìm mã phiếu, khách hàng, biển số...");
        searchField.setToolTipText("Tìm mã phiếu sửa chữa hoặc khách hàng");

        JButton refresh = secondaryButton("Làm mới");

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 14));
        toolbar.setBackground(WHITE);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(0, 14, 0, 14)));
        searchField.setPreferredSize(new Dimension(340, 36));

        toolbar.add(searchField);
        toolbar.add(refresh);
        center.add(toolbar, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new Object[]{"Phiếu sửa chữa", "Khách hàng", "Biển số",
                        "Dịch vụ", "Phụ tùng", "Tổng tiền"}, 0) {
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

        // Default renderer có padding cho các cột text
        DefaultTableCellRenderer paddedRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, selected, focus, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 13, 0, 8));
                return this;
            }
        };
        table.getColumnModel().getColumn(0).setCellRenderer(paddedRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(paddedRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(paddedRenderer);

        table.getColumnModel().getColumn(3).setCellRenderer(new MoneyRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new MoneyRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new MoneyRenderer());

        sorter = new TableRowSorter<>(model);
        sorter.setSortable(0, false);
        sorter.setSortable(1, false);
        sorter.setSortable(2, false);
        sorter.setSortable(3, false);
        sorter.setSortable(4, false);
        sorter.setSortable(5, false);
        table.setRowSorter(sorter);

        JScrollPane tableScroll = new JScrollPane(table) {
            @Override
            protected javax.swing.JViewport createViewport() {
                return new javax.swing.JViewport() {
                    @Override
                    protected void paintComponent(java.awt.Graphics g) {
                        super.paintComponent(g);
                        if (table.getRowCount() == 0) {
                            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setFont(new Font("Dialog", Font.PLAIN, 13));
                            g2.setColor(MUTED);
                            String msg = "Không tìm thấy phiếu phù hợp";
                            java.awt.FontMetrics fm = g2.getFontMetrics();
                            int x = (getWidth()  - fm.stringWidth(msg)) / 2;
                            int y = (getHeight() + fm.getAscent()) / 2 - 4;
                            g2.drawString(msg, x, y);
                            g2.dispose();
                        }
                    }
                };
            }
        };
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        tableScroll.getViewport().setBackground(WHITE);

        resultCount = new JLabel();
        resultCount.setFont(new Font("Dialog", Font.PLAIN, 11));
        resultCount.setForeground(MUTED);

        footerCount = new JLabel();
        footerCount.setFont(new Font("Dialog", Font.PLAIN, 11));
        footerCount.setForeground(MUTED);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createLineBorder(BORDER));

        JPanel cardHeading = new JPanel(new BorderLayout());
        cardHeading.setBackground(WHITE);
        cardHeading.setBorder(BorderFactory.createEmptyBorder(14, 17, 14, 17));

        JLabel cardTitle = new JLabel("Danh sách phiếu chờ thu");
        cardTitle.setFont(new Font("Dialog", Font.BOLD, 15));
        cardTitle.setForeground(INK);

        JLabel cardSubtitle = new JLabel("Chọn một dòng để xem, lọc hoặc mở tác vụ liên quan.");
        cardSubtitle.setFont(new Font("Dialog", Font.PLAIN, 11));
        cardSubtitle.setForeground(MUTED);

        JPanel titleStack = new JPanel(new BorderLayout(0, 4));
        titleStack.setOpaque(false);
        titleStack.add(cardTitle, BorderLayout.NORTH);
        titleStack.add(cardSubtitle, BorderLayout.SOUTH);

        resultCount.setHorizontalAlignment(SwingConstants.RIGHT);
        cardHeading.add(titleStack, BorderLayout.WEST);
        cardHeading.add(resultCount, BorderLayout.EAST);
        card.add(cardHeading, BorderLayout.NORTH);
        card.add(tableScroll, BorderLayout.CENTER);

        JPanel cardFooter = new JPanel(new BorderLayout());
        cardFooter.setBackground(WHITE);
        cardFooter.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                BorderFactory.createEmptyBorder(11, 14, 11, 14)));
        cardFooter.add(footerCount, BorderLayout.WEST);
        card.add(cardFooter, BorderLayout.SOUTH);

        center.add(card, BorderLayout.CENTER);

        refresh.addActionListener(e -> {
            searchField.setText("");
            taiDuLieu();
        });
        searchField.addActionListener(e -> refreshRows());
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { refreshRows(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { refreshRows(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { refreshRows(); }
        });

        taiDuLieu();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        // Refresh dữ liệu từ DAO khi panel được hiển thị
        taiDuLieu();
    }

    private JPanel buildHeading() {
        JPanel heading = new JPanel(new BorderLayout(0, 5));
        heading.setOpaque(false);

        heading.add(new JLabel("Thu ngân  /  Nghiệp vụ  /  Phiếu chờ thu") {{
            setFont(new Font("Dialog", Font.PLAIN, 11));
            setForeground(MUTED);
        }}, BorderLayout.NORTH);

        JPanel titleBlock = new JPanel(new BorderLayout(0, 5));
        titleBlock.setOpaque(false);
        titleBlock.add(new JLabel("Phiếu chờ thu") {{
            setFont(new Font("Dialog", Font.BOLD, 24));
            setForeground(UiTheme.TEXT);
        }}, BorderLayout.NORTH);
        titleBlock.add(new JLabel("Không CRUD tự do với hóa đơn đã phát hành.") {{
            setFont(new Font("Dialog", Font.PLAIN, 12));
            setForeground(MUTED);
        }}, BorderLayout.SOUTH);
        heading.add(titleBlock, BorderLayout.CENTER);

        JButton createInvoice = primaryButton("Đối chiếu chi phí");
        createInvoice.addActionListener(e -> {
            if (this.onCreateInvoice == null) return;
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Vui lòng chọn một phiếu trước khi đối chiếu.",
                        "Chưa chọn phiếu", javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = table.convertRowIndexToModel(viewRow);
            String maPhieu = model.getValueAt(modelRow, 0).toString();
            this.onCreateInvoice.accept(maPhieu);
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(createInvoice);
        heading.add(btnPanel, BorderLayout.EAST);

        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        return heading;
    }

    private JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Dialog", Font.BOLD, 12));
        button.setForeground(WHITE);
        button.setBackground(BLUE);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
        return button;
    }

    private JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Dialog", Font.BOLD, 12));
        button.setForeground(new Color(49, 68, 91));
        button.setBackground(WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 13, 8, 13)));
        return button;
    }

    /**
     * Tải dữ liệu vào model — khi kết nối DB thật, thay phần mock bằng truy vấn DAO.
     * Gọi lần đầu khi khởi tạo và khi bấm "Làm mới".
     */
    private void taiDuLieu() {
        model.setRowCount(0); // xóa dữ liệu cũ
        // Lấy dữ liệu từ DAO
        for (HoaDonDAO.PhieuChoThu phieu : dao.layPhieuChoThu(taiKhoan)) {
            long tongTien = phieu.getTongTien();
            
            // Query chi tiết dịch vụ + phụ tùng từ DoiChieuChiPhiDAO
            long dicVu = 0L;
            long phuTung = 0L;
            try {
                DoiChieuChiPhiDAO.KetQuaDoiChieu kq = doiChieuDAO.layKetQuaDoiChieu(phieu.getMaPhieu());
                for (DoiChieuChiPhiDAO.DongChiPhi d : kq.getDanhSachChiPhi()) {
                    String hangMuc = d.getHangMuc().toLowerCase();
                    long thanhTien = d.getThanhTien();
                    // Phân loại: dịch vụ vs phụ tùng (dựa trên tên hạng mục)
                    if (hangMuc.contains("dịch vụ") || hangMuc.contains("bảo") || hangMuc.contains("công") 
                        || hangMuc.contains("kiểm") || hangMuc.contains("nạp")) {
                        dicVu += thanhTien;
                    } else {
                        phuTung += thanhTien;
                    }
                }
            } catch (IllegalArgumentException e) {
                // Phiếu không có chi tiết, để 0
            }
            
            model.addRow(new Object[]{
                phieu.getMaPhieu(),      // Phiếu sửa chữa
                phieu.getTenKhach(),     // Khách hàng
                phieu.getBienSo(),       // Biển số
                dicVu,                   // Dịch vụ
                phuTung,                 // Phụ tùng
                tongTien,                // Tổng tiền
                "Chờ thu tiền"           // Trạng thái
            });
        }
        refreshRows();
    }

    private void refreshRows() {
        String query = searchField.getText().trim().toLowerCase(Locale.ROOT);

        if (query.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.orFilter(java.util.Arrays.asList(
                    RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(query), 0),
                    RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(query), 1),
                    RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(query), 2)
            )));
        }

        int visible = table.getRowCount();
        int total   = model.getRowCount();
        resultCount.setText(visible + " kết quả đang hiển thị");
        footerCount.setText("Hiển thị " + visible + " / " + total + " hồ sơ");
    }

    private class MoneyRenderer extends DefaultTableCellRenderer {
        MoneyRenderer() { setHorizontalAlignment(SwingConstants.RIGHT); }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean selected, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t,
                    money(((Number) value).longValue()), selected, focus, row, col);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 18));
            return this;
        }
    }

    private class StatusRenderer implements javax.swing.table.TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean selected, boolean focus, int row, int col) {
            JPanel cell = new JPanel(new java.awt.GridBagLayout());
            cell.setOpaque(true);
            cell.setBackground(selected ? t.getSelectionBackground() : WHITE);
            cell.setBorder(selected
                    ? BorderFactory.createMatteBorder(1, 0, 1, 0, t.getSelectionBackground().darker())
                    : BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

            JLabel badge = new JLabel(value == null ? "" : value.toString());
            badge.setFont(new Font("Dialog", Font.BOLD, 10));
            if (selected) {
                badge.setForeground(Color.WHITE);
                badge.setOpaque(false);
                badge.setBorder(BorderFactory.createEmptyBorder(3, 7, 3, 7));
            } else {
                badge.setForeground(GOLD);
                badge.setOpaque(true);
                badge.setBackground(GOLD_BG);
                badge.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(224, 180, 80), 1),
                        BorderFactory.createEmptyBorder(3, 7, 3, 7)));
            }

            cell.add(badge);
            return cell;
        }
    }

    private class LinkRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean selected, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, value, selected, focus, row, col);
            if (!selected) setForeground(BLUE);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createEmptyBorder(0, 13, 0, 0));
            return this;
        }
    }

    private static String money(long value) {
        return String.format(Locale.forLanguageTag("vi-VN"), "%,d", value).replace(',', '.');
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

