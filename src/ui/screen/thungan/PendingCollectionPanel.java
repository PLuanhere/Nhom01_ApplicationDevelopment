package ui.screen.thungan;

import dao.hoadon.HoaDonDAO;
import entity.hoadon.PhieuChoThu;
import entity.nhansu.TaiKhoan;
import entity.nhansu.VaiTro;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import ui.MainFrame;

/** Content panel for the cashier's "Phiếu chờ thu" screen. */
public class PendingCollectionPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Color PAGE = new Color(244, 246, 249);
    private static final Color WHITE = Color.WHITE;
    private static final Color INK = new Color(27, 62, 102);
    private static final Color MUTED = new Color(100, 116, 137);
    private static final Color BORDER = new Color(233, 237, 242);
    private static final Color BLUE = new Color(34, 100, 166);
    private static final Color GOLD_BG = new Color(255, 243, 221);
    private static final Color GOLD = new Color(155, 99, 7);

    private final DefaultTableModel model;
    private final JTable table;
    private final JLabel resultCount;
    private final JLabel footerCount;
    private final JTextField searchField;
    private final JComboBox<String> statusFilter;
    private final ActionListener onCreateInvoice;
    private final ActionListener onViewDetails;

    private final HoaDonDAO dao;
    private final TaiKhoan taiKhoan;
    private final MainFrame winChinh;

    /** Mở danh sách phiếu của nguồn dữ liệu chung. */
    public PendingCollectionPanel(HoaDonDAO dao, TaiKhoan taiKhoan, MainFrame winChinh) {
        this(dao, taiKhoan, winChinh, null, null);
    }

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public PendingCollectionPanel() {
        this(null, null);
    }

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public PendingCollectionPanel(ActionListener onCreateInvoice, ActionListener onViewDetails) {
        this(new HoaDonDAO(), new TaiKhoan("thungan", VaiTro.THU_NGAN), null, onCreateInvoice, onViewDetails);
    }

    /** Khởi tạo bộ lọc, danh sách và thao tác cho phiếu được chọn. */
    private PendingCollectionPanel(HoaDonDAO dao, TaiKhoan taiKhoan, MainFrame winChinh, ActionListener onCreateInvoice,
            ActionListener onViewDetails) {
        this.dao = dao;
        this.taiKhoan = taiKhoan;
        this.winChinh = winChinh;
        this.onCreateInvoice = onCreateInvoice;
        this.onViewDetails = onViewDetails;

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

        searchField = new JTextField();
        searchField.setToolTipText("Tìm mã phiếu sửa chữa hoặc khách hàng");
        statusFilter = new JComboBox<>(new String[] { "Tất cả trạng thái", "Chờ lập hóa đơn" });

        JButton refresh = secondaryButton("Làm mới");

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 14));
        toolbar.setBackground(WHITE);
        toolbar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(0, 14, 0, 14)));
        searchField.setPreferredSize(new Dimension(380, 36));
        statusFilter.setPreferredSize(new Dimension(150, 36));
        toolbar.add(searchField);
        toolbar.add(statusFilter);
        toolbar.add(refresh);
        center.add(toolbar, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[] { "Phiếu sửa chữa", "Khách hàng", "Biển số", "Dịch vụ", "Phụ tùng",
                "Tổng tiền", "Trạng thái", "Thao tác" }, 0) {
            /** Cho biết cell editable. */
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.addRow(new Object[] { "SC-2026-0124", "Nguyễn Văn An", "51H-246.80", 400000L, 870000L, 1270000L,
                "Chờ lập hóa đơn", "Xem chi tiết" });

        table = new JTable(model);

        table.getTableHeader().setReorderingAllowed(false);

        table.getTableHeader().setResizingAllowed(false);
        table.setRowHeight(52);
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
        table.addMouseListener(new MouseAdapter() {
            /** Mở use clicked khi người dùng yêu cầu. */
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2 || table.columnAtPoint(event.getPoint()) == 7) {
                    moThanhToan();
                }
            }
        });
        table.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "thanhToan");
        table.getActionMap().put("thanhToan", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                moThanhToan();
            }
        });
        table.setFont(new Font("Dialog", Font.PLAIN, 12));
        table.setForeground(new Color(50, 68, 90));
        table.setGridColor(BORDER);
        table.setShowVerticalLines(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getTableHeader().setPreferredSize(new Dimension(0, 39));
        table.getTableHeader().setFont(new Font("Dialog", Font.BOLD, 11));
        table.getTableHeader().setForeground(new Color(86, 103, 126));
        table.getTableHeader().setBackground(new Color(248, 250, 252));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);

        int[] widths = { 224, 224, 183, 142, 151, 163, 267, 232 };
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        table.getColumnModel().getColumn(3).setCellRenderer(new MoneyRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new MoneyRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new MoneyRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusRenderer());
        table.getColumnModel().getColumn(7).setCellRenderer(new LinkRenderer());

        JScrollPane tableScroll = new JScrollPane(table);
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
        cardFooter.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                BorderFactory.createEmptyBorder(11, 14, 11, 14)));
        cardFooter.add(footerCount, BorderLayout.WEST);
        card.add(cardFooter, BorderLayout.SOUTH);

        center.add(card, BorderLayout.CENTER);

        refresh.addActionListener(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshRows();
            }
        });
        searchField.addActionListener(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshRows();
            }
        });
        statusFilter.addActionListener(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshRows();
            }
        });

        refreshRows();

        JLabel shellFooter = new JLabel("Garage Đức Dũng · Nội bộ nhân viên");
        shellFooter.setFont(new Font("Dialog", Font.PLAIN, 11));
        shellFooter.setForeground(new Color(131, 144, 162));
        shellFooter.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        content.add(shellFooter, BorderLayout.SOUTH);
    }

    /** Tạo heading cho giao diện. */
    private JPanel buildHeading() {
        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);

        JPanel labels = new JPanel(new BorderLayout(0, 5));
        labels.setOpaque(false);

        JLabel breadcrumb = new JLabel("Thu ngân  /  Nghiệp vụ  /  Phiếu chờ thu");
        breadcrumb.setFont(new Font("Dialog", Font.PLAIN, 11));
        breadcrumb.setForeground(MUTED);

        JLabel title = new JLabel("Phiếu chờ thu");
        title.setFont(new Font("Dialog", Font.BOLD, 24));
        title.setForeground(INK);

        JLabel note = new JLabel("Không CRUD tự do với hóa đơn đã phát hành.");
        note.setFont(new Font("Dialog", Font.PLAIN, 12));
        note.setForeground(MUTED);

        JPanel titleStack = new JPanel(new BorderLayout(0, 5));
        titleStack.setOpaque(false);
        titleStack.add(title, BorderLayout.NORTH);
        titleStack.add(note, BorderLayout.SOUTH);

        labels.add(breadcrumb, BorderLayout.NORTH);
        labels.add(titleStack, BorderLayout.CENTER);
        heading.add(labels, BorderLayout.CENTER);

        JButton createInvoice = primaryButton("Lập hóa đơn từ phiếu");
        createInvoice.addActionListener(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent e) {
                moThanhToan();
            }
        });

        // Wrap để nút không bị kéo giãn chiều dọc theo BorderLayout.EAST
        JPanel btnWrap = new JPanel(new GridBagLayout());
        btnWrap.setOpaque(false);
        btnWrap.add(createInvoice);

        heading.add(btnWrap, BorderLayout.EAST);
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        return heading;
    }

    /** Thực hiện primary button cho thành phần hiện tại. */
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

    /** Thực hiện secondary button cho thành phần hiện tại. */
    private JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Dialog", Font.BOLD, 12));
        button.setForeground(new Color(49, 68, 91));
        button.setBackground(WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 13, 8, 13)));
        return button;
    }

    /** Đọc lại dữ liệu và cập nhật phần hiển thị. */
    private void refreshRows() {
        String khoa = searchField.getText().trim().toLowerCase();
        List<PhieuChoThu> danhSach = dao.layPhieuChoThu(taiKhoan);
        model.setRowCount(0);
        for (PhieuChoThu phieu : danhSach) {
            String noiDung = phieu.getMaPhieu() + " " + phieu.getTenKhach() + " " + phieu.getBienSo();
            if (noiDung.toLowerCase().contains(khoa)) {
                long dichVu = 0;
                long phuTung = 0;
                long[] tien = dao.docChiPhi(taiKhoan, phieu.getMaPhieu());
                dichVu = tien[0];
                phuTung = tien[1];
                model.addRow(new Object[] { phieu.getMaPhieu(), phieu.getTenKhach(), phieu.getBienSo(), dichVu, phuTung,
                        phieu.getTongTien(), "Chờ lập hóa đơn", "Xem chi tiết" });
            }
        }
        resultCount.setText(model.getRowCount() + " kết quả đang hiển thị");
        footerCount.setText("Hiển thị " + model.getRowCount() + " / " + danhSach.size() + " hồ sơ");
        if (model.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
        }
    }

    private class MoneyRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
        MoneyRenderer() {
            setHorizontalAlignment(SwingConstants.RIGHT);
        }

        /** Trả về table cell renderer component. */
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row,
                int col) {
            super.getTableCellRendererComponent(t, money(((Number) value).longValue()), selected, focus, row, col);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 18));
            return this;
        }
    }

    private class StatusRenderer implements TableCellRenderer {
        /** Trả về table cell renderer component. */
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row,
                int col) {
            // Panel nền cell — trong suốt, căn giữa theo chiều dọc
            JPanel cell = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            cell.setOpaque(true);
            cell.setBackground(selected ? t.getSelectionBackground() : WHITE);
            cell.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

            // Badge bọc text vừa khít theo độ dài label
            JLabel badge = new JLabel(value == null ? "" : value.toString());
            badge.setFont(new Font("Dialog", Font.BOLD, 11));
            badge.setForeground(GOLD);
            badge.setOpaque(true);
            badge.setBackground(GOLD_BG);
            badge.setBorder(
                    BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(224, 180, 80), 1),
                            BorderFactory.createEmptyBorder(4, 10, 4, 10)));

            // Căn giữa badge theo chiều dọc trong cell
            cell.setLayout(new GridBagLayout());
            cell.add(badge);
            return cell;
        }
    }

    private class LinkRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        /** Trả về table cell renderer component. */
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row,
                int col) {
            super.getTableCellRendererComponent(t, value, selected, focus, row, col);
            setForeground(BLUE);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createEmptyBorder(0, 13, 0, 0));
            return this;
        }
    }

    /** Mở ney khi người dùng yêu cầu. */
    private static String money(long value) {
        return String.format(Locale.forLanguageTag("vi-VN"), "%,d", value).replace(',', '.');
    }

    /** Mở xác nhận thu tiền cho đúng phiếu được chọn, không tự lấy phiếu đầu tiên. */
    private void moThanhToan() {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một phiếu.");
            return;
        }
        String ma = model.getValueAt(table.convertRowIndexToModel(dong), 0).toString();
        if (winChinh != null) {
            winChinh.moDoiChieuChiPhi(ma);
        } else if (onCreateInvoice != null) {
            onCreateInvoice.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ma));
        }
    }
}
