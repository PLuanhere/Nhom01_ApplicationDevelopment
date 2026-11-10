package ui.screen.thungan;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import dao.DoiChieuChiPhiDAO;
import dao.HoaDonDAO;
import entity.HoaDon;
import entity.TaiKhoan;

/**
 * Màn hình chi tiết hóa đơn đã thanh toán và in lại.
 * Tuân thủ quy định: UI nhận DAO, đọc entity HoaDon.
 */
public class PaidInvoiceDetailPanel extends JPanel {
    
    private static final Color PAGE = new Color(244, 246, 249);
    private static final Color WHITE = Color.WHITE;
    private static final Color INK = new Color(28, 41, 59);
    private static final Color MUTED = new Color(99, 115, 135);
    private static final Color BORDER = new Color(217, 227, 237);
    private static final Color BLUE = new Color(36, 102, 168);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    private final HoaDonDAO thuNganDAO;
    private final DoiChieuChiPhiDAO doiChieuDAO;
    private final String maHoaDon;
    private TaiKhoan taiKhoan;
    private final Runnable onBack;
    
    public PaidInvoiceDetailPanel(HoaDonDAO thuNganDAO, DoiChieuChiPhiDAO doiChieuDAO, 
                                   String maHoaDon, Runnable onBack) {
        this(thuNganDAO, doiChieuDAO, maHoaDon, onBack, null);
    }

    private PaidInvoiceDetailPanel(HoaDonDAO thuNganDAO, DoiChieuChiPhiDAO doiChieuDAO,
            String maHoaDon, Runnable onBack, TaiKhoan taiKhoan) {
        this.thuNganDAO = thuNganDAO;
        this.doiChieuDAO = doiChieuDAO;
        this.maHoaDon = maHoaDon;
        this.onBack = onBack;
        this.taiKhoan = taiKhoan;
        
        setLayout(new BorderLayout());
        setBackground(PAGE);
        setBorder(BorderFactory.createEmptyBorder(20, 26, 18, 26));
        
        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setOpaque(false);
        add(content, BorderLayout.NORTH);
        
        content.add(buildHeading(), BorderLayout.NORTH);
        content.add(buildInvoiceCard(), BorderLayout.CENTER);
    }

    public PaidInvoiceDetailPanel(HoaDonDAO dao, TaiKhoan taiKhoan, String maHoaDon,
            java.awt.event.ActionListener onBack) {
        this(dao, new DoiChieuChiPhiDAO(dao, taiKhoan), maHoaDon, null, taiKhoan);
        if (onBack != null) {
            // Constructor chính đã dựng UI; callback quay lại được xử lý bởi shell.
            // Không tham chiếu this trong lời gọi this(...).
        }
    }
    
    private JPanel buildHeading() {
        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JPanel left = new JPanel(new BorderLayout(0, 8));
        left.setOpaque(false);
        left.add(label("Thu ngân  /  Nghiệp vụ  /  Chi tiết & in hóa đơn", 11, Font.PLAIN, MUTED), BorderLayout.NORTH);
        JPanel titles = new JPanel(new BorderLayout(0, 5));
        titles.setOpaque(false);
        titles.add(label("Chi tiết & in hóa đơn", 24, Font.BOLD, INK), BorderLayout.NORTH);
        titles.add(label("Tra cứu hóa đơn đã lưu và hỗ trợ xem/in lại, không cho sửa hoặc xóa hóa đơn đã thanh toán.", 12, Font.PLAIN, MUTED), BorderLayout.SOUTH);
        left.add(titles, BorderLayout.CENTER);
        heading.add(left, BorderLayout.CENTER);
        JButton back = secondaryButton("Trở về tổng quan");
        back.addActionListener(e -> { if (onBack != null) onBack.run(); });
        heading.add(back, BorderLayout.EAST);
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));
        return heading;
    }
    
    private JPanel buildInvoiceCard() {
        HoaDon hoaDon = thuNganDAO.layHoaDonDaThu(taiKhoan).stream()
                .filter(hd -> hd.getMaHoaDon().equals(maHoaDon))
                .findFirst().orElse(null);
        if (hoaDon == null) return taoManHinhLoi("Không tìm thấy hóa đơn " + maHoaDon);
        
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);
        top.add(label("Hóa đơn " + hoaDon.getMaHoaDon() + " · Đã thanh toán", 18, Font.BOLD, INK), BorderLayout.NORTH);
        top.add(label("Bản xem và in lại · chứng từ đã thu là chỉ đọc, không sửa hoặc xóa trực tiếp.", 12, Font.PLAIN, MUTED), BorderLayout.CENTER);
        
        JPanel factsOne = new JPanel(new GridLayout(1, 3, 12, 0));
        factsOne.setOpaque(false);
        factsOne.add(fact("Khách hàng / xe", hoaDon.getTenKhachHang() + " · " + hoaDon.getBienSo()));
        factsOne.add(fact("Phiếu sửa chữa", hoaDon.getMaPhieuSuaChua()));
        factsOne.add(fact("Ngày thanh toán", hoaDon.getThoiDiemThanhToan().format(DATE_FMT)));
        
        JPanel factsTwo = new JPanel(new GridLayout(1, 3, 12, 0));
        factsTwo.setOpaque(false);
        factsTwo.add(fact("Người thu", hoaDon.getNguoiThucHien() + " · Thu ngân"));
        factsTwo.add(fact("Phương thức", hoaDon.getHinhThucThanhToan().getTenHienThi()));
        factsTwo.add(fact("Trạng thái", "Đã thanh toán · chỉ đọc"));
        
        JPanel factGroup = new JPanel(new GridLayout(2, 1, 0, 12));
        factGroup.setOpaque(false);
        factGroup.add(factsOne);
        factGroup.add(factsTwo);
        top.add(factGroup, BorderLayout.SOUTH);
        card.add(top, BorderLayout.NORTH);
        card.add(buildChiTietTable(hoaDon), BorderLayout.CENTER);
        card.add(buildBottom(hoaDon), BorderLayout.SOUTH);
        return card;
    }
    
    private JPanel buildChiTietTable(HoaDon hoaDon) {
        JPanel middle = new JPanel(new BorderLayout(0, 8));
        middle.setOpaque(false);
        middle.add(label("Chi tiết dịch vụ và phụ tùng", 15, Font.BOLD, INK), BorderLayout.NORTH);
        List<DoiChieuChiPhiDAO.DongChiPhi> chiTiet = null;
        try {
            DoiChieuChiPhiDAO.KetQuaDoiChieu kq = doiChieuDAO.layKetQuaDoiChieu(hoaDon.getMaPhieuSuaChua());
            chiTiet = kq.getDanhSachChiPhi();
        } catch (IllegalArgumentException e) { }
        
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Hạng mục", "SL", "Đơn giá", "Thành tiền"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        
        if (chiTiet != null && !chiTiet.isEmpty()) {
            for (DoiChieuChiPhiDAO.DongChiPhi dong : chiTiet) {
                model.addRow(new Object[]{dong.getHangMuc(), dong.getSlDvt(), dong.getDonGia(), dong.getThanhTien()});
            }
        } else {
            model.addRow(new Object[]{"(Không có chi tiết)", "—", 0L, 0L});
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
        table.setPreferredScrollableViewportSize(new Dimension(table.getPreferredSize().width, bodyH));
        
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
    
    private JPanel buildBottom(HoaDon hoaDon) {
        JPanel bottom = new JPanel(new BorderLayout(0, 10));
        bottom.setOpaque(false);
        JPanel totals = new JPanel(new GridLayout(1, 2, 12, 0));
        totals.setOpaque(false);
        totals.add(readOnly("Tổng đã thanh toán · chỉ đọc", formatMoney(hoaDon.getTongTien())));
        totals.add(readOnly("Bảo hành / thuế (nếu áp dụng) · chỉ đọc", "Đối chiếu theo phiếu sửa chữa và chính sách garage"));
        bottom.add(totals, BorderLayout.NORTH);
        JLabel note = label("In lại đúng mã hóa đơn cũ; không tạo chứng từ mới. Tài khoản thuế/bảo hành đối chiếu theo chính sách garage.", 13, Font.PLAIN, MUTED);
        note.setOpaque(true);
        note.setBackground(WHITE);
        note.setBorder(BorderFactory.createEmptyBorder(11, 11, 11, 11));
        bottom.add(note, BorderLayout.CENTER);
        JPanel actions = new JPanel(new BorderLayout(12, 0));
        actions.setOpaque(false);
        actions.add(label("Bản xem trước → Hộp thoại in hệ điều hành", 12, Font.PLAIN, MUTED), BorderLayout.CENTER);
        JButton print = primaryButton("In hóa đơn");
        print.addActionListener(e -> inHoaDon(hoaDon));
        actions.add(print, BorderLayout.EAST);
        bottom.add(actions, BorderLayout.SOUTH);
        return bottom;
    }
    private void inHoaDon(HoaDon hoaDon) {
        if (hoaDon == null) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu hóa đơn để in.",
                    "Không thể in", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Tạo PrinterJob
        PrinterJob printerJob;
        try {
            printerJob = PrinterJob.getPrinterJob();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Không thể khởi tạo máy in: " + ex.getMessage(),
                    "Lỗi in hóa đơn", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Tạo Printable cho hóa đơn
        Printable printable = new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                if (pageIndex > 0) {
                    return NO_SUCH_PAGE;
                }
                
                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                
                int y = 50;
                int x = 50;
                
                // Header - Thông tin garage
                g2d.setFont(new Font("Dialog", Font.BOLD, 18));
                g2d.drawString("GARAGE Ô TÔ ĐỨC DŨNG", x, y);
                y += 25;
                
                g2d.setFont(new Font("Dialog", Font.PLAIN, 10));
                g2d.drawString("Địa chỉ: 123 Đường ABC, Quận XYZ, TP.HCM", x, y);
                y += 15;
                g2d.drawString("Điện thoại: 028 1234 5678", x, y);
                y += 30;
                
                // Tiêu đề hóa đơn
                g2d.setFont(new Font("Dialog", Font.BOLD, 16));
                g2d.drawString("HÓA ĐƠN THANH TOÁN", x + 150, y);
                y += 30;
                
                // Thông tin hóa đơn
                g2d.setFont(new Font("Dialog", Font.PLAIN, 11));
                g2d.drawString("Mã hóa đơn: " + hoaDon.getMaHoaDon(), x, y);
                y += 18;
                g2d.drawString("Ngày: " + hoaDon.getThoiDiemThanhToan().format(DATE_FMT), x, y);
                y += 18;
                g2d.drawString("Khách hàng: " + hoaDon.getTenKhachHang(), x, y);
                y += 18;
                g2d.drawString("Hình thức: " + hoaDon.getHinhThucThanhToan().getTenHienThi(), x, y);
                y += 25;
                
                // Đường kẻ
                g2d.drawLine(x, y, x + 450, y);
                y += 15;
                
                // Chi tiết lấy từ snapshot hóa đơn đã thanh toán, không đọc lại phiếu chờ thu.
                var chiTietHoaDon = thuNganDAO.layChiTietHoaDon(taiKhoan, hoaDon.getMaHoaDon());
                if (chiTietHoaDon != null && !chiTietHoaDon.isEmpty()) {
                    g2d.setFont(new Font("Dialog", Font.BOLD, 11));
                    g2d.drawString("Mô tả", x, y);
                    g2d.drawString("Đơn vị", x + 250, y);
                    g2d.drawString("Đơn giá", x + 350, y);
                    g2d.drawString("Thành tiền", x + 420, y);
                    y += 20;
                    
                    g2d.setFont(new Font("Dialog", Font.PLAIN, 10));
                    
                    // Danh sách chi phí
                    for (var cp : chiTietHoaDon) {
                        g2d.drawString(cp.getTenHangMuc(), x, y);
                        g2d.drawString(cp.getSoLuong() + " " + cp.getDonVi(), x + 250, y);
                        g2d.drawString(formatMoney(cp.getDonGia()), x + 350, y);
                        g2d.drawString(formatMoney(cp.getThanhTien()), x + 420, y);
                        y += 15;
                    }
                }
                
                y += 10;
                g2d.drawLine(x, y, x + 450, y);
                y += 20;
                
                // Tổng tiền
                g2d.setFont(new Font("Dialog", Font.BOLD, 13));
                g2d.drawString("TỔNG CỘNG:", x + 300, y);
                g2d.drawString(formatMoney(hoaDon.getTongTien()), x + 420, y);
                y += 30;
                
                // Footer
                g2d.setFont(new Font("Dialog", Font.ITALIC, 9));
                g2d.drawString("Cảm ơn quý khách đã sử dụng dịch vụ!", x + 150, y);
                
                return PAGE_EXISTS;
            }
        };
        
        printerJob.setPrintable(printable);
        
        // Hiển thị dialog chọn máy in
        try {
            if (!printerJob.printDialog()) {
                return;
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Không thể mở hộp thoại máy in: " + ex.getMessage(),
                    "Lỗi in hóa đơn", JOptionPane.ERROR_MESSAGE);
            return;
        }
        {
            try {
                printerJob.print();
                JOptionPane.showMessageDialog(this, 
                    "In hóa đơn thành công!", 
                    "Thông báo", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Lỗi khi in: " + ex.getMessage(), 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private JPanel taoManHinhLoi(String thongBao) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(40, 40, 40, 40)));
        panel.add(label(thongBao, 16, Font.BOLD, MUTED), BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel fact(String title, String value) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        panel.add(label(title, 12, Font.PLAIN, MUTED), BorderLayout.NORTH);
        panel.add(label(value, 14, Font.BOLD, INK), BorderLayout.SOUTH);
        return panel;
    }
    
    private JPanel readOnly(String title, String value) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        panel.add(label(title, 12, Font.BOLD, MUTED), BorderLayout.NORTH);
        JLabel field = label(value, 14, Font.PLAIN, MUTED);
        field.setOpaque(true);
        field.setBackground(new Color(245, 248, 252));
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }
    
    private static String formatMoney(long amount) {
        return NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN")).format(amount) + "đ";
    }
    
    private static JLabel label(String text, int size, int style, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Dialog", style, size));
        l.setForeground(color);
        return l;
    }
    
    private static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Dialog", Font.BOLD, 14));
        b.setForeground(WHITE);
        b.setBackground(BLUE);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(11, 14, 11, 14));
        return b;
    }
    
    private static JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Dialog", Font.BOLD, 12));
        b.setForeground(new Color(49, 68, 91));
        b.setBackground(WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        return b;
    }
    
    private static class MoneyRenderer extends DefaultTableCellRenderer {
        MoneyRenderer() { setHorizontalAlignment(SwingConstants.RIGHT); }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int col) {
            String amount = formatMoney(((Number) value).longValue());
            super.getTableCellRendererComponent(t, amount, selected, focus, row, col);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 14));
            return this;
        }
    }
}
