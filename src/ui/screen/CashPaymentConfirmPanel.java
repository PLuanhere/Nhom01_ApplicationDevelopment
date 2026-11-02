package ui.screen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.NumberFormatter;

import dao.ThuNganDAO;
import dao.ThuNganDAO.PhieuChoThu;
import entity.TaiKhoan;
import ui.component.UiTheme;

/**
 * Màn hình xác nhận thanh toán tiền mặt cho một phiếu sửa chữa.
 *
 * <p>Layout: {@code BorderLayout} toàn màn hình — NORTH header, CENTER card cuộn,
 * mỗi nhóm trong card xếp bằng {@code GridBagLayout} dồn lên NORTH,
 * đảm bảo mọi thứ giãn full chiều ngang.</p>
 */
public final class CashPaymentConfirmPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Color BORDER_CLR = new Color(0xD9E3ED);
    private static final Color MUTED      = new Color(0x637387);
    private static final Color NOTE_BG    = new Color(0xFFF7E3);
    private static final Color ERROR      = new Color(0xA33A2B);
    private static final Color SUCCESS    = new Color(0x16835B);

    private final ThuNganDAO  dao;
    private final PhieuChoThu phieu;
    private final TaiKhoan    taiKhoan;

    private final JFormattedTextField receivedField;
    private final JLabel changeValue;
    private final JLabel message;

    /** Tạo màn hình xem trước không cần DAO hay tài khoản thực. */
    public CashPaymentConfirmPanel() {
        this(null, null, null, () -> {}, maHoaDon -> {});
    }

    /**
     * Tạo màn hình thanh toán tiền mặt đầy đủ.
     *
     * @param dao       nguồn dữ liệu Thu ngân; null dùng DAO mặc định
     * @param phieu     phiếu cần thu tiền; null dùng dữ liệu mẫu
     * @param taiKhoan  tài khoản đang đăng nhập
     * @param onBack    callback quay về danh sách / màn hình trước
     * @param onConfirm callback sau khi xác nhận thu tiền thành công, nhận mã hóa đơn
     */
    public CashPaymentConfirmPanel(ThuNganDAO dao, PhieuChoThu phieu,
            TaiKhoan taiKhoan, Runnable onBack, Consumer<String> onConfirm) {
        this.dao      = dao   != null ? dao   : new ThuNganDAO();
        this.phieu    = phieu != null ? phieu : placeholderPhieu();
        this.taiKhoan = taiKhoan;
        Runnable safeBack    = onBack    != null ? onBack    : () -> {};
        Consumer<String> safeConfirm = onConfirm != null ? onConfirm : maHoaDon -> {};

        // ── Khởi tạo field trước khi dùng trong createCard ──
        NumberFormatter formatter = new NumberFormatter(new DecimalFormat("#,##0"));
        formatter.setValueClass(Long.class);
        formatter.setAllowsInvalid(true);
        formatter.setCommitsOnValidEdit(true);
        receivedField = new JFormattedTextField(formatter);
        receivedField.setValue(this.phieu.getTongTien().longValue());
        receivedField.setFont(UiTheme.SHELL_FONT);
        receivedField.setForeground(UiTheme.TEXT);
        receivedField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR),
                BorderFactory.createEmptyBorder(8, 9, 8, 9)));
        receivedField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        changeValue = lbl("0đ", UiTheme.SHELL_FONT, MUTED);
        message     = lbl("",   UiTheme.CAPTION_FONT, ERROR);

        // ── Màn hình chính dùng BorderLayout ──
        setLayout(new BorderLayout(0, 0));
        setBackground(UiTheme.WINDOW);

        add(createHeader(safeBack),             BorderLayout.NORTH);
        add(createScrollCenter(safeConfirm),    BorderLayout.CENTER);

        receivedField.addPropertyChangeListener("value", e -> updateChange());
    }

    // ── Header ───────────────────────────────────────────────────────────────

    private JPanel createHeader(Runnable onBack) {
        JPanel pnl = new JPanel(new BorderLayout(12, 0));
        pnl.setOpaque(false);
        pnl.setBorder(BorderFactory.createEmptyBorder(22, 26, 14, 26));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(lbl("Thu ngân / Nghiệp vụ / Thanh toán tiền mặt",
                UiTheme.CAPTION_FONT, MUTED));
        heading.add(Box.createVerticalStrut(4));
        heading.add(lbl("Thanh toán tiền mặt",
                UiTheme.BRAND_FONT.deriveFont(Font.BOLD, 22f), UiTheme.TEXT));
        heading.add(lbl("Số tiền khách đưa phải đủ; hệ thống tính tiền thừa.",
                UiTheme.CAPTION_FONT, MUTED));
        pnl.add(heading, BorderLayout.CENTER);

        JButton btnBack = btn("Danh sách / quay lại", false);
        btnBack.setPreferredSize(new Dimension(180, 34));
        btnBack.addActionListener(e -> onBack.run());
        JPanel pnlBack = new JPanel(new BorderLayout());
        pnlBack.setOpaque(false);
        pnlBack.add(btnBack, BorderLayout.SOUTH);
        pnl.add(pnlBack, BorderLayout.EAST);
        return pnl;
    }

    // ── Vùng cuộn trung tâm ──────────────────────────────────────────────────

    private JScrollPane createScrollCenter(Consumer<String> onConfirm) {
        // Card dùng BorderLayout: NORTH chứa tất cả nội dung dồn lên trên
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UiTheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR),
                BorderFactory.createEmptyBorder(20, 20, 16, 20)));

        // GridBagLayout xếp các khối nội dung thành cột dọc,
        // fill=HORIZONTAL + weightx=1 → mỗi khối giãn full chiều ngang.
        JPanel stack = new JPanel(new GridBagLayout());
        stack.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx   = 0;
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        // ── Tiêu đề card ──
        gbc.gridy  = 0;
        gbc.insets = new Insets(0, 0, 4, 0);
        stack.add(lbl("Thu tiền mặt · " + phieu.getMaPhieu(),
                UiTheme.BRAND_FONT.deriveFont(Font.BOLD, 18f), UiTheme.TEXT), gbc);

        gbc.gridy  = 1;
        gbc.insets = new Insets(0, 0, 16, 0);
        stack.add(lbl("Chỉ xác nhận khi phiếu sửa chữa hoàn tất và số tiền khách đưa không thấp hơn tổng phải thu.",
                UiTheme.CAPTION_FONT, MUTED), gbc);

        // ── Thông tin phiếu ──
        gbc.gridy  = 2;
        gbc.insets = new Insets(0, 0, 16, 0);
        stack.add(createFacts(), gbc);

        // ── Lưới nhập ──
        gbc.gridy  = 3;
        gbc.insets = new Insets(0, 0, 12, 0);
        stack.add(createInputGrid(), gbc);

        // ── Ghi chú ──
        gbc.gridy  = 4;
        gbc.insets = new Insets(0, 0, 12, 0);
        stack.add(createNote(), gbc);

        // ── Nút xác nhận ──
        gbc.gridy  = 5;
        gbc.insets = new Insets(0, 0, 8, 0);
        stack.add(createConfirmRow(onConfirm), gbc);

        // ── Dòng thông báo lỗi/thành công ──
        gbc.gridy  = 6;
        gbc.insets = new Insets(0, 0, 0, 0);
        stack.add(message, gbc);

        card.add(stack, BorderLayout.NORTH);

        // Wrapper đặt card vào NORTH → card chỉ ôm nội dung, không giãn dọc
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UiTheme.WINDOW);
        wrapper.add(card, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 26, 18, 26));
        scroll.getViewport().setBackground(UiTheme.WINDOW);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    // ── Thông tin phiếu ──────────────────────────────────────────────────────

    private JPanel createFacts() {
        JPanel facts = new JPanel(new GridLayout(1, 3, 12, 0));
        facts.setOpaque(false);
        facts.add(fact("Khách / biển số",
                phieu.getTenKhach() + " · " + phieu.getBienSo()));
        facts.add(fact("Tình trạng phiếu", "Hoàn tất sửa chữa"));
        facts.add(fact("Hóa đơn", "Hệ thống tự sinh khi xác nhận"));
        return facts;
    }

    // ── Lưới nhập liệu ───────────────────────────────────────────────────────

    private JPanel createInputGrid() {
        JPanel grid = new JPanel(new GridLayout(2, 2, 12, 10));
        grid.setOpaque(false);
        String tongHienThi = new DecimalFormat("#,##0").format(
                phieu.getTongTien().longValue()) + "đ";
        String nguoiThuHienThi = taiKhoan != null
                ? taiKhoan.getTenDangNhap() : "thungan";
        grid.add(field("Tổng phải thu · chỉ đọc",           readOnly(tongHienThi)));
        grid.add(field("Số tiền khách giao *",               receivedField));
        grid.add(field("Tiền thừa trả khách · chỉ đọc",     readOnly(changeValue)));
        grid.add(field("Người thu · chỉ đọc",                readOnly(nguoiThuHienThi)));
        return grid;
    }

    // ── Ghi chú ──────────────────────────────────────────────────────────────

    private JPanel createNote() {
        JPanel note = new JPanel(new BorderLayout());
        note.setBackground(NOTE_BG);
        note.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        note.add(lbl("Nếu số tiền đưa thấp hơn tổng phải thu: hiển thị 'Chưa đủ tiền', giữ nguyên thông tin và không lập hóa đơn. Nếu lỗi lưu, cho thử lại.",
                UiTheme.CAPTION_FONT, MUTED), BorderLayout.CENTER);
        return note;
    }

    // ── Hàng nút xác nhận ────────────────────────────────────────────────────

    private JPanel createConfirmRow(Consumer<String> onConfirm) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.add(lbl("Xác nhận sau khi nhận tiền; tạo hóa đơn và chuyển hồ sơ cho Tiếp nhận bàn giao.",
                UiTheme.CAPTION_FONT, MUTED), BorderLayout.CENTER);
        JButton btnConfirm = btn("Xác nhận đã thu tiền mặt", true);
        btnConfirm.setPreferredSize(new Dimension(240, 36));
        btnConfirm.addActionListener(e -> confirmPayment(onConfirm));
        row.add(btnConfirm, BorderLayout.EAST);
        return row;
    }

    // ── Logic nghiệp vụ ──────────────────────────────────────────────────────

    private void updateChange() {
        try {
            receivedField.commitEdit();
            long nhan = ((Number) receivedField.getValue()).longValue();
            long tong = phieu.getTongTien().longValue();
            changeValue.setText(fmt(Math.max(0L, nhan - tong)) + "đ");
            message.setForeground(ERROR);
            message.setText(nhan < tong
                    ? "Chưa đủ tiền. Vui lòng kiểm tra số tiền khách giao." : "");
        } catch (ParseException | RuntimeException ignored) {
            message.setForeground(ERROR);
            message.setText("Vui lòng nhập số tiền hợp lệ.");
        }
    }

    private void confirmPayment(Consumer<String> onConfirm) {
        try {
            receivedField.commitEdit();
            long nhan = ((Number) receivedField.getValue()).longValue();
            long tong = phieu.getTongTien().longValue();
            if (nhan < tong) {
                message.setForeground(ERROR);
                message.setText("Chưa đủ tiền. Vui lòng kiểm tra số tiền khách giao.");
                receivedField.requestFocusInWindow();
                return;
            }
            int choice = JOptionPane.showConfirmDialog(this,
                    "Xác nhận đã nhận " + fmt(nhan) + "đ và lập hóa đơn cho phiếu "
                    + phieu.getMaPhieu() + "?",
                    "Xác nhận thanh toán",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
            String nguoiThu = taiKhoan != null ? taiKhoan.getTenDangNhap() : "thungan";
            String maHoaDon = dao.xacNhanThanhToanTienMat(
                    phieu.getMaPhieu(), BigDecimal.valueOf(nhan), nguoiThu);
            message.setForeground(SUCCESS);
            message.setText("Đã lưu hóa đơn " + maHoaDon
                    + ". Chuyển hồ sơ cho Tiếp nhận bàn giao.");
            onConfirm.accept(maHoaDon);
        } catch (IllegalStateException ex) {
            message.setForeground(ERROR);
            message.setText(ex.getMessage());
        } catch (ParseException | RuntimeException ex) {
            message.setForeground(ERROR);
            message.setText("Lỗi xử lý. Vui lòng thử lại.");
        }
    }

    // ── Tiện ích ─────────────────────────────────────────────────────────────

    private JPanel fact(String title, String value) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(lbl(title, UiTheme.CAPTION_FONT, MUTED));
        p.add(lbl(value, UiTheme.BRAND_FONT.deriveFont(Font.BOLD, 13f), UiTheme.TEXT));
        return p;
    }

    private JPanel field(String title, java.awt.Component input) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.add(lbl(title, UiTheme.CAPTION_FONT.deriveFont(Font.BOLD), MUTED),
                BorderLayout.NORTH);
        p.add(input, BorderLayout.CENTER);
        return p;
    }

    private JPanel readOnly(String value) {
        return readOnly(lbl(value, UiTheme.SHELL_FONT, MUTED));
    }

    private JPanel readOnly(JLabel value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(0xF5F8FC));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR),
                BorderFactory.createEmptyBorder(8, 9, 8, 9)));
        p.add(value, BorderLayout.CENTER);
        return p;
    }

    private String fmt(long amount) {
        return new DecimalFormat("#,##0").format(amount);
    }

    private JLabel lbl(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    private JButton btn(String text, boolean primary) {
        JButton b = new JButton(text);
        UiTheme.styleButton(b, primary);
        b.setFont(UiTheme.CAPTION_FONT);
        return b;
    }

    private static PhieuChoThu placeholderPhieu() {
        return new PhieuChoThu("SC-2026-0108", "51H-246.80", "Nguyễn Văn An",
                new BigDecimal("1270000"), null);
    }
}
