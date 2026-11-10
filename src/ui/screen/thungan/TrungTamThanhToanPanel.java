package ui.screen.thungan;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import dao.HoaDonDAO;
import dao.HoaDonDAO.PhieuChoThu;
import entity.HoaDon;
import entity.TaiKhoan;
import ui.component.UiTheme;

/**
 * Màn hình Trung tâm thanh toán — tổng quan ca thu và lối vào các chức năng chính.
 * Dữ liệu được lấy từ {@link HoaDonDAO}; không hard-code số liệu trong UI.
 *
 * <p>Gắn vào vùng làm việc của {@code MainFrame} qua {@code dieuHuongThuNgan()}.</p>
 */
public final class TrungTamThanhToanPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Color GRID  = new Color(0xDCE3EC);
    private static final Color MUTED = new Color(0x647489);
    private static final Color GREEN = new Color(0x16835B);
    private static final Color AMBER = new Color(0xB56A00);
    private static final NumberFormat FMT =
            NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
    private final TaiKhoan taiKhoan;

    /**
     * Tạo màn hình với no-op callback, dùng DAO mặc định.
     * Dùng khi xem trước độc lập.
     */
    public TrungTamThanhToanPanel() {
        this(new HoaDonDAO(), new TaiKhoan("NV007", TaiKhoan.VaiTro.THU_NGAN), new Navigation());
    }

    /**
     * Tạo màn hình với DAO và callback điều hướng do MainFrame cung cấp.
     *
     * @param dao nguồn dữ liệu Thu ngân
     * @param nav callback đích cho các nút hành động
     */
    public TrungTamThanhToanPanel(HoaDonDAO dao, TaiKhoan taiKhoan, Navigation nav) {
        this.taiKhoan = taiKhoan;
        HoaDonDAO d    = dao != null ? dao : new HoaDonDAO();
        Navigation n    = nav != null ? nav  : new Navigation();

        setLayout(new BorderLayout(0, 12));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 26, 18, 26));

        add(buildHeading(),                         BorderLayout.NORTH);
        add(createScrollBody(d, n),                 BorderLayout.CENTER);
        add(createFooter(),                         BorderLayout.SOUTH);
    }

    // ── Intro ─────────────────────────────────────────────────────────────────

    private JPanel buildHeading() {
        JPanel pnl = new JPanel(new BorderLayout(0, 5));
        pnl.setOpaque(false);
        pnl.add(new JLabel("Thu ngân  /  Nghiệp vụ  /  Trung tâm thanh toán") {{
            setFont(new Font("Dialog", Font.PLAIN, 11));
            setForeground(MUTED);
        }}, BorderLayout.NORTH);
        JPanel titles = new JPanel(new BorderLayout(0, 5));
        titles.setOpaque(false);
        titles.add(lbl("Trung tâm thanh toán",
                new Font("Dialog", Font.BOLD, 24), UiTheme.TEXT), BorderLayout.NORTH);
        titles.add(new JLabel("Chỉ thu từ phiếu sửa chữa hoàn thành và chưa có hóa đơn hợp lệ.") {{
            setFont(new Font("Dialog", Font.PLAIN, 12));
            setForeground(MUTED);
        }}, BorderLayout.SOUTH);
        pnl.add(titles, BorderLayout.CENTER);
        pnl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        return pnl;
    }

    // ── Body cuộn ─────────────────────────────────────────────────────────────

    private JScrollPane createScrollBody(HoaDonDAO dao, Navigation nav) {
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        body.add(createSummaryCards(dao));
        body.add(Box.createVerticalStrut(12));
        body.add(createTwoColumns(dao, nav));

        JScrollPane scr = new JScrollPane(body);
        scr.setBorder(BorderFactory.createEmptyBorder());
        scr.getViewport().setOpaque(false);
        scr.setOpaque(false);
        scr.getVerticalScrollBar().setUnitIncrement(16);
        return scr;
    }

    // ── Thẻ thống kê ──────────────────────────────────────────────────────────

    private JPanel createSummaryCards(HoaDonDAO dao) {
        int soPhieu      = dao.layPhieuChoThu(taiKhoan).size();
        List<HoaDon> hoaDon = dao.layHoaDonDaThu(taiKhoan);
        int soHoaDon     = hoaDon.size();
        long tienMat  = hoaDon.stream().filter(h -> h.getHinhThucThanhToan() == HoaDon.HinhThucThanhToan.TIEN_MAT)
                .mapToLong(HoaDon::getTongTien).sum();
        long chuyenKhoan = hoaDon.stream().filter(h -> h.getHinhThucThanhToan() == HoaDon.HinhThucThanhToan.CHUYEN_KHOAN)
                .mapToLong(HoaDon::getTongTien).sum();

        JPanel cards = new JPanel(new GridLayout(1, 4, 10, 0));
        cards.setOpaque(false);
        cards.add(stat("Phiếu hoàn tất chờ thu",
                String.valueOf(soPhieu),
                "Chỉ lấy phiếu chưa lập hóa đơn", AMBER));
        cards.add(stat("Hóa đơn đã thu",
                String.valueOf(soHoaDon),
                "Chờ bàn giao cho tiếp nhận", GREEN));
        cards.add(stat("Đã thu tiền mặt",
                FMT.format(tienMat) + "đ",
                "Trong kỳ hiện tại", MUTED));
        cards.add(stat("Đã thu chuyển khoản",
                FMT.format(chuyenKhoan) + "đ",
                "Trong kỳ hiện tại", MUTED));
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));
        return cards;
    }

    // ── Hai cột nội dung ──────────────────────────────────────────────────────

    private JPanel createTwoColumns(HoaDonDAO dao, Navigation nav) {
        JPanel cols = new JPanel(new GridLayout(1, 2, 12, 0));
        cols.setOpaque(false);

        // Cột trái - Nhét 2 card vào 1 panel để kề nhau
        JPanel leftWrapper = new JPanel(new BorderLayout());
        leftWrapper.setOpaque(false);
        
        JPanel leftCards = new JPanel();
        leftCards.setOpaque(false);
        leftCards.setLayout(new BoxLayout(leftCards, BoxLayout.Y_AXIS));
        leftCards.add(createCardPanel("Phiếu sửa chữa chờ thanh toán",
                "Đối chiếu chi phí trước khi phát hành",
                createPendingList(dao.layPhieuChoThu(taiKhoan), nav)));
        leftCards.add(Box.createVerticalStrut(12));
        leftCards.add(createCardPanel("Chứng từ vừa hoàn tất", "",
                createCompletedList(dao.layHoaDonDaThu(taiKhoan), nav)));
        
        leftWrapper.add(leftCards, BorderLayout.NORTH);

        // Cột phải — dùng BorderLayout để card không bị kéo giãn theo chiều dọc
        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        JPanel shortcutCard = createCardPanel("Đường tắt", "", createShortcuts(nav));
        shortcutCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, shortcutCard.getPreferredSize().height));
        right.add(shortcutCard, BorderLayout.NORTH);

        cols.add(leftWrapper);
        cols.add(right);
        return cols;
    }

    // ── Danh sách phiếu chờ ───────────────────────────────────────────────────

    /**
     * Tạo danh sách hàng từ dữ liệu DAO — không hard-code tên/số tiền.
     *
     * @param danhSach danh sách phiếu từ DAO
     * @param nav      callback điều hướng
     * @return panel danh sách
     */
    private JPanel createPendingList(List<PhieuChoThu> danhSach, Navigation nav) {
        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        if (danhSach.isEmpty()) {
            list.add(lbl("Không có phiếu chờ thu tiền.", UiTheme.CAPTION_FONT, MUTED));
            return list;
        }

        for (int i = 0; i < danhSach.size(); i++) {
            PhieuChoThu p = danhSach.get(i);
            list.add(receiptRow(
                    p.getMaPhieu(),
                    p.getTenKhach() + " · " + p.getBienSo(),
                    "Chờ thu tiền", AMBER,
                    "Lập hóa đơn", nav.costReconciliation));
            if (i < danhSach.size() - 1) {
                list.add(rowDivider());
            }
        }
        return list;
    }

    // ── Danh sách hóa đơn đã thu ──────────────────────────────────────────────

    /**
     * Tạo danh sách hóa đơn đã thu từ DAO — dùng entity HoaDon thực.
     *
     * @param danhSach danh sách hóa đơn từ DAO
     * @param nav      callback điều hướng
     * @return panel danh sách
     */
    private JPanel createCompletedList(List<HoaDon> danhSach, Navigation nav) {
        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        if (danhSach.isEmpty()) {
            list.add(lbl("Chưa có hóa đơn trong kỳ.", UiTheme.CAPTION_FONT, MUTED));
            return list;
        }

        for (int i = 0; i < danhSach.size(); i++) {
            HoaDon hd = danhSach.get(i);
            // Dòng đầu: mã hóa đơn + tên khách
            String line1 = hd.getMaHoaDon() + " · " + hd.getTenKhachHang();
            // Dòng thứ hai: tổng tiền + hình thức + ghi chú
            String line2 = FMT.format(hd.getTongTien()) + "đ · " + 
                           hd.getHinhThucThanhToan().getTenHienThi() + 
                           " · gửi tiếp nhận bàn giao";
            list.add(receiptRow(
                    line1,
                    line2,
                    "Đã thanh toán", GREEN,
                    "Xem chứng từ", nav.paidDocuments));
            if (i < danhSach.size() - 1) {
                list.add(rowDivider());
            }
        }
        return list;
    }

    // ── Đường tắt ─────────────────────────────────────────────────────────────

    private JPanel createShortcuts(Navigation nav) {
        JPanel p = new JPanel(new GridLayout(1, 3, 8, 8));
        p.setOpaque(false);
        p.add(actionBtn("Phiếu chờ thu",          false, nav.pendingInvoices));
        p.add(actionBtn("Hóa đơn đã thanh toán",   false, nav.paidDocuments));
        p.add(actionBtn("Tra cứu hóa đơn",         false, nav.documentSearch));
        return p;
    }

    // ── Footer ────────────────────────────────────────────────────────────────

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, GRID));
        return footer;
    }

    // ── Thành phần nhỏ ────────────────────────────────────────────────────────

    private JPanel stat(String title, String value, String note, Color noteColor) {
        JPanel p = new JPanel();
        p.setBackground(UiTheme.SURFACE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GRID),
                BorderFactory.createEmptyBorder(9, 10, 8, 10)));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(lbl(title, UiTheme.CAPTION_FONT, MUTED));
        JLabel amount = lbl(value, UiTheme.BRAND_FONT.deriveFont(Font.BOLD, 18f), UiTheme.TEXT);
        amount.setBorder(BorderFactory.createEmptyBorder(2, 0, 3, 0));
        p.add(amount);
        p.add(lbl(note, UiTheme.CAPTION_FONT, noteColor));
        return p;
    }

    private JPanel createCardPanel(String title, String subtitle, JPanel content) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UiTheme.SURFACE);
        p.setBorder(BorderFactory.createLineBorder(GRID));

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, GRID),
                BorderFactory.createEmptyBorder(10, 11, 9, 11)));
        head.add(lbl(title, UiTheme.BRAND_FONT.deriveFont(Font.BOLD, 14f), UiTheme.TEXT));
        if (subtitle != null && !subtitle.isBlank()) {
            head.add(lbl(subtitle, UiTheme.CAPTION_FONT, MUTED));
        }
        p.add(head, BorderLayout.NORTH);

        content.setBorder(BorderFactory.createEmptyBorder(10, 11, 10, 11));
        p.add(content, BorderLayout.CENTER);
        
        wrapper.add(p, BorderLayout.NORTH);
        wrapper.setAlignmentX(LEFT_ALIGNMENT);
        return wrapper;
    }

    private JPanel receiptRow(String title, String detail,
            String status, Color statusColor, String btnText, ActionListener action) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(true);
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createEmptyBorder(7, 0, 7, 0));
        
        // Thêm cursor pointer
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Thanh màu bên trái
        JPanel marker = new JPanel();
        marker.setBackground(statusColor);
        marker.setPreferredSize(new Dimension(3, 34));
        row.add(marker, BorderLayout.WEST);

        // Nội dung chính
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(lbl(title,  UiTheme.BRAND_FONT.deriveFont(Font.BOLD, 12f), UiTheme.TEXT));
        text.add(lbl(detail, UiTheme.CAPTION_FONT, MUTED));
        row.add(text, BorderLayout.CENTER);

        // Badge góc phải - KHÔNG CÓ NÚT
        JPanel end = new JPanel(new BorderLayout());
        end.setOpaque(false);

        JLabel badge = lbl(status,
                UiTheme.CAPTION_FONT.deriveFont(Font.BOLD, 10f), statusColor);
        badge.setOpaque(true);
        badge.setBackground(statusColor == GREEN
                ? new Color(0xE8F7EF) : new Color(0xFFF3DB));
        badge.setBorder(BorderFactory.createEmptyBorder(3, 7, 3, 7));
        badge.setHorizontalAlignment(JLabel.CENTER);
        end.add(badge, BorderLayout.NORTH);
        
        row.add(end, BorderLayout.EAST);
        
        // Thêm MouseListener cho toàn bộ card
        row.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (action != null) {
                    action.actionPerformed(new java.awt.event.ActionEvent(row,
                            java.awt.event.ActionEvent.ACTION_PERFORMED, title));
                }
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                row.setBackground(new Color(0xF5F8FC)); // Màu xanh nhạt khi hover
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                row.setBackground(Color.WHITE);
            }
        });
        
        return row;
    }

    private JPanel rowDivider() {
        JPanel line = new JPanel();
        line.setBackground(new Color(0xE9EDF2));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        line.setPreferredSize(new Dimension(1, 1));
        return line;
    }

    private JButton actionBtn(String text, boolean primary, ActionListener action) {
        JButton btn = new JButton(text);
        UiTheme.styleButton(btn, primary);
        btn.setFont(UiTheme.CAPTION_FONT);
        btn.setPreferredSize(new Dimension(150, 34));
        btn.addActionListener(e -> { if (action != null) action.actionPerformed(new java.awt.event.ActionEvent(btn, java.awt.event.ActionEvent.ACTION_PERFORMED, text)); });
        return btn;
    }

    private JLabel lbl(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    /**
     * Callback đích do MainFrame/shell cung cấp khi nối các nút hành động.
     * Các field là {@code null} khi không được gán — nút sẽ im lặng.
     */
    public static final class Navigation {
        public ActionListener viewInvoice;
        /** Mở màn hình đối chiếu chi phí. */
        public ActionListener costReconciliation;
        /** Mở màn hình thanh toán tiền mặt. */
        public ActionListener cashPayment;
        /** Mở màn hình chuyển khoản / QR. */
        public ActionListener bankTransfer;
        /** Mở màn hình tra cứu hóa đơn. */
        public ActionListener documentSearch;
        /** Mở màn hình phiếu chờ thu. */
        public ActionListener pendingInvoices;
        /** Mở màn hình hóa đơn đã thanh toán. */
        public ActionListener paidDocuments;
    }
}
