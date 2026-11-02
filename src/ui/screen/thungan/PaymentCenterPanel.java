package ui.screen.thungan;

import dao.hoadon.HoaDonDAO;
import entity.hoadon.HoaDon;
import entity.hoadon.PhieuChoThu;
import entity.nhansu.TaiKhoan;
import entity.nhansu.VaiTro;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import ui.component.UiTheme;

/** Màn hình Trung tâm thanh toán — tổng quan ca thu và lối vào các chức năng chính. */
public final class PaymentCenterPanel extends JPanel {

    private final TaiKhoan taiKhoan;
    private static final long serialVersionUID = 1L;
    private static final Color GRID = new Color(0xDCE3EC);
    private static final Color MUTED = new Color(0x647489);
    private static final Color GREEN = new Color(0x16835B);
    private static final Color AMBER = new Color(0xB56A00);
    private static final NumberFormat FMT = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

    /** Tạo màn hình với no-op callback, dùng DAO mặc định. */
    public PaymentCenterPanel() {
        this(new HoaDonDAO(), new TaiKhoan("thungan", VaiTro.THU_NGAN), new Navigation());
    }

    /** Tạo màn hình với DAO và callback điều hướng do MainFrame cung cấp. */
    public PaymentCenterPanel(HoaDonDAO dao, TaiKhoan taiKhoan, Navigation nav) {
        this.taiKhoan = taiKhoan;
        HoaDonDAO d = dao != null ? dao : new HoaDonDAO();
        Navigation n = nav != null ? nav : new Navigation();

        setLayout(new BorderLayout(0, 12));
        setBackground(UiTheme.WINDOW);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 14, 16));

        add(createIntro(), BorderLayout.NORTH);
        add(createScrollBody(d, n), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
    }

    // ── Intro ─────────────────────────────────────────────────────────────────

    /** Tạo intro cho giao diện. */
    private JPanel createIntro() {
        JPanel pnl = new JPanel();
        pnl.setOpaque(false);
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.add(lbl("Thu ngân / Hệ thống / Trung tâm thanh toán", UiTheme.CAPTION_FONT, MUTED));
        pnl.add(Box.createVerticalStrut(6));
        pnl.add(lbl("Trung tâm thanh toán", UiTheme.BRAND_FONT.deriveFont(Font.BOLD, 20f), UiTheme.TEXT));
        pnl.add(lbl("Chỉ thu từ phiếu sửa chữa hoàn thành và chưa có hóa đơn hợp lệ.", UiTheme.CAPTION_FONT, MUTED));
        pnl.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
        return pnl;
    }

    // ── Body cuộn ─────────────────────────────────────────────────────────────

    /** Tạo scroll body cho giao diện. */
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

    /** Tạo summary cards cho giao diện. */
    private JPanel createSummaryCards(HoaDonDAO dao) {
        int soPhieu = dao.demPhieuChoThu(taiKhoan);
        int soHoaDon = dao.demHoaDonDaThu(taiKhoan);
        long tienMat = dao.tinhTongTheoHinhThuc(taiKhoan, "Tiền mặt");
        long chuyenKhoan = dao.tinhTongTheoHinhThuc(taiKhoan, "Chuyển khoản");

        JPanel cards = new JPanel(new GridLayout(1, 4, 10, 0));
        cards.setOpaque(false);
        cards.add(stat("Phiếu hoàn tất chờ thu", String.valueOf(soPhieu), "Chỉ lấy phiếu chưa lập hóa đơn", AMBER));
        cards.add(stat("Hóa đơn đã thu", String.valueOf(soHoaDon), "Chờ bàn giao cho tiếp nhận", GREEN));
        cards.add(stat("Đã thu tiền mặt", FMT.format(tienMat) + "đ", "Trong kỳ hiện tại", MUTED));
        cards.add(stat("Đã thu chuyển khoản", FMT.format(chuyenKhoan) + "đ", "Trong kỳ hiện tại", MUTED));
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));
        return cards;
    }

    // ── Hai cột nội dung ──────────────────────────────────────────────────────

    /** Tạo two columns cho giao diện. */
    private JPanel createTwoColumns(HoaDonDAO dao, Navigation nav) {
        JPanel cols = new JPanel(new GridLayout(1, 2, 12, 0));
        cols.setOpaque(false);

        // Cột trái
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(createCardPanel("Phiếu sửa chữa chờ thanh toán", "Đối chiếu chi phí trước khi phát hành",
                createPendingList(dao.layPhieuChoThu(taiKhoan), nav)));
        left.add(Box.createVerticalStrut(12));
        left.add(createCardPanel("Chứng từ vừa hoàn tất", "", createCompletedList(dao.layHoaDonDaThu(taiKhoan), nav)));

        // Cột phải — dùng BorderLayout để card không bị kéo giãn theo chiều dọc
        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        JPanel shortcutCard = createCardPanel("Đường tắt", "", createShortcuts(nav));
        shortcutCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, shortcutCard.getPreferredSize().height));
        right.add(shortcutCard, BorderLayout.NORTH);

        cols.add(left);
        cols.add(right);
        return cols;
    }

    // ── Danh sách phiếu chờ ───────────────────────────────────────────────────

    /** Tạo danh sách hàng từ dữ liệu DAO — không hard-code tên/số tiền. */
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
            list.add(receiptRow(p.getMaPhieu() + " · " + p.getBienSo(),
                    p.getTenKhach() + " · " + FMT.format(p.getTongTien()) + "đ", "Chờ lập hóa đơn", AMBER,
                    "Đối chiếu chi phí", nav.costReconciliation));
            if (i < danhSach.size() - 1) {
                list.add(rowDivider());
            }
        }
        return list;
    }

    // ── Danh sách hóa đơn đã thu ──────────────────────────────────────────────

    /** Tạo danh sách hóa đơn đã thu từ DAO. */
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
            list.add(receiptRow(hd.getMaHoaDon() + " · " + hd.getTenKhachHang(),
                    FMT.format(hd.getTongTien()) + "đ · " + hd.getHinhThucThanhToan().getTenHienThi(), "Đã thanh toán",
                    GREEN, "Xem chứng từ", nav.paidDocuments));
            if (i < danhSach.size() - 1) {
                list.add(rowDivider());
            }
        }
        return list;
    }

    // ── Đường tắt ─────────────────────────────────────────────────────────────

    /** Tạo shortcuts cho giao diện. */
    private JPanel createShortcuts(Navigation nav) {
        JPanel p = new JPanel(new GridLayout(2, 2, 8, 8));
        p.setOpaque(false);
        p.add(actionBtn("Phiếu chờ thu", false, nav.pendingInvoices));
        p.add(actionBtn("Đối chiếu chi phí", false, nav.costReconciliation));
        p.add(actionBtn("Hóa đơn đã thanh toán", false, nav.paidDocuments));
        p.add(actionBtn("Tra cứu hóa đơn", false, nav.documentSearch));
        return p;
    }

    // ── Footer ────────────────────────────────────────────────────────────────

    /** Tạo footer cho giao diện. */
    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, GRID));
        JLabel txt = lbl("Garage Đức Dũng · Nội bộ nhân viên", UiTheme.CAPTION_FONT, MUTED);
        txt.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        footer.add(txt, BorderLayout.WEST);
        return footer;
    }

    // ── Thành phần nhỏ ────────────────────────────────────────────────────────

    /** Thực hiện stat cho thành phần hiện tại. */
    private JPanel stat(String title, String value, String note, Color noteColor) {
        JPanel p = new JPanel();
        p.setBackground(UiTheme.SURFACE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(GRID),
                BorderFactory.createEmptyBorder(9, 10, 8, 10)));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(lbl(title, UiTheme.CAPTION_FONT, MUTED));
        JLabel amount = lbl(value, UiTheme.BRAND_FONT.deriveFont(Font.BOLD, 18f), UiTheme.TEXT);
        amount.setBorder(BorderFactory.createEmptyBorder(2, 0, 3, 0));
        p.add(amount);
        p.add(lbl(note, UiTheme.CAPTION_FONT, noteColor));
        return p;
    }

    /** Tạo card panel cho giao diện. */
    private JPanel createCardPanel(String title, String subtitle, JPanel content) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UiTheme.SURFACE);
        p.setBorder(BorderFactory.createLineBorder(GRID));

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, GRID),
                BorderFactory.createEmptyBorder(10, 11, 9, 11)));
        head.add(lbl(title, UiTheme.BRAND_FONT.deriveFont(Font.BOLD, 14f), UiTheme.TEXT));
        if (subtitle != null && !subtitle.isBlank()) {
            head.add(lbl(subtitle, UiTheme.CAPTION_FONT, MUTED));
        }
        p.add(head, BorderLayout.NORTH);

        content.setBorder(BorderFactory.createEmptyBorder(10, 11, 10, 11));
        p.add(content, BorderLayout.CENTER);
        p.setAlignmentX(LEFT_ALIGNMENT);
        return p;
    }

    /** Thực hiện receipt row cho thành phần hiện tại. */
    private JPanel receiptRow(String title, String detail, String status, Color statusColor, String btnText,
            ActionListener action) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(7, 0, 7, 0));

        // Thanh màu bên trái
        JPanel marker = new JPanel();
        marker.setBackground(statusColor);
        marker.setPreferredSize(new Dimension(3, 34));
        row.add(marker, BorderLayout.WEST);

        // Nội dung chính
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(lbl(title, UiTheme.BRAND_FONT.deriveFont(Font.BOLD, 12f), UiTheme.TEXT));
        text.add(lbl(detail, UiTheme.CAPTION_FONT, MUTED));
        row.add(text, BorderLayout.CENTER);

        // Badge + nút bên phải
        JPanel end = new JPanel();
        end.setOpaque(false);
        end.setLayout(new BoxLayout(end, BoxLayout.Y_AXIS));

        JLabel badge = lbl(status, UiTheme.CAPTION_FONT.deriveFont(Font.BOLD, 10f), statusColor);
        badge.setOpaque(true);
        badge.setBackground(statusColor == GREEN ? new Color(0xE8F7EF) : new Color(0xFFF3DB));
        badge.setBorder(BorderFactory.createEmptyBorder(3, 7, 3, 7));
        end.add(badge);

        JButton btn = actionBtn(btnText, false, action);
        btn.setFont(UiTheme.CAPTION_FONT);
        btn.setPreferredSize(new Dimension(120, 27));
        btn.setMaximumSize(new Dimension(140, 27));
        btn.setAlignmentX(RIGHT_ALIGNMENT);
        end.add(Box.createVerticalStrut(4));
        end.add(btn);
        row.add(end, BorderLayout.EAST);
        return row;
    }

    /** Thực hiện row divider cho thành phần hiện tại. */
    private JPanel rowDivider() {
        JPanel line = new JPanel();
        line.setBackground(new Color(0xE9EDF2));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        line.setPreferredSize(new Dimension(1, 1));
        return line;
    }

    /** Thực hiện action btn cho thành phần hiện tại. */
    private JButton actionBtn(String text, boolean primary, ActionListener action) {
        JButton btn = new JButton(text);
        UiTheme.styleButton(btn, primary);
        btn.setFont(UiTheme.CAPTION_FONT);
        btn.setPreferredSize(new Dimension(150, 34));
        btn.addActionListener(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent e) {
                if (action != null) {
                    action.actionPerformed(new ActionEvent(action, ActionEvent.ACTION_PERFORMED, "thaoTac"));
                }
            }
        });
        return btn;
    }

    /** Thực hiện lbl cho thành phần hiện tại. */
    private JLabel lbl(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    /** Callback đích do MainFrame/shell cung cấp khi nối các nút hành động. */
    public static final class Navigation {
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
