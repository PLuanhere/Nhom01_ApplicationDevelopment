package ui.screen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import ui.component.UiTheme;

/**
 * Màn hình kết quả tra cứu chứng từ thanh toán có dữ liệu.
 * Tiêu đề và dữ liệu bảng thay đổi theo phương thức thanh toán được chọn.
 * Gắn vào vùng CENTER của {@code MainFrame} qua {@code ThuNganShell}.
 *
 * <p>Chỉ chứa GUI và dữ liệu minh họa; không kết nối DAO.</p>
 */
public final class CashReceiptPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Color GRID = new Color(0xDCE1E7);

    private JTable tblChungTu;

    /**
     * Tạo màn hình với chứng từ mẫu, phương thức "Tất cả".
     */
    public CashReceiptPanel() {
        this("Tất cả", () -> { }, () -> { });
    }

    /**
     * Tạo màn hình với phương thức thanh toán cụ thể.
     *
     * @param phuongThuc   "Tất cả", "Tiền mặt" hoặc "Chuyển khoản"
     * @param doiTieuChi   hành động khi bấm "Đổi tiêu chí"
     * @param xemChungTu   hành động khi bấm "Xem chứng từ"
     */
    public CashReceiptPanel(String phuongThuc, Runnable doiTieuChi, Runnable xemChungTu) {
        Runnable onChangeCriteria = doiTieuChi != null ? doiTieuChi : () -> { };
        Runnable onViewReceipt   = xemChungTu  != null ? xemChungTu  : () -> { };

        setLayout(new BorderLayout());
        setBackground(UiTheme.WINDOW);

        JPanel pnlContent = new JPanel(new BorderLayout(0, 8));
        pnlContent.setOpaque(false);
        pnlContent.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        pnlContent.add(createHeading(phuongThuc), BorderLayout.NORTH);
        pnlContent.add(createReceiptCard(phuongThuc), BorderLayout.CENTER);

        add(pnlContent, BorderLayout.CENTER);
        add(createActions(onChangeCriteria, onViewReceipt), BorderLayout.SOUTH);
    }

    // ── Tiêu đề ──────────────────────────────────────────────────────────────

    /**
     * Tạo vùng tiêu đề; tiêu đề chính thay đổi theo phương thức.
     *
     * @param phuongThuc phương thức thanh toán đang lọc
     * @return panel tiêu đề
     */
    private JPanel createHeading(String phuongThuc) {
        JPanel pnlHeading = new JPanel();
        pnlHeading.setOpaque(false);
        pnlHeading.setLayout(new BoxLayout(pnlHeading, BoxLayout.Y_AXIS));

        // Tiêu đề thay đổi theo phương thức
        String tenManHinh = switch (phuongThuc) {
            case "Tiền mặt"     -> "Chứng từ thanh toán tiền mặt";
            case "Chuyển khoản" -> "Chứng từ thanh toán chuyển khoản";
            default             -> "Chứng từ thanh toán";
        };

        JLabel lblTitle = new JLabel(tenManHinh);
        lblTitle.setFont(UiTheme.BRAND_FONT.deriveFont(Font.BOLD, 22f));
        lblTitle.setForeground(UiTheme.TEXT);

        JLabel lblDesc = new JLabel("Chứng từ đã phát hành chỉ được xem, in hoặc xuất.");
        lblDesc.setFont(UiTheme.CAPTION_FONT.deriveFont(12f));
        lblDesc.setForeground(UiTheme.TEXT);

        pnlHeading.add(lblTitle);
        pnlHeading.add(lblDesc);
        return pnlHeading;
    }

    // ── Card bảng ────────────────────────────────────────────────────────────

    /**
     * Tạo card chứa bảng kết quả; tiêu đề card thay đổi theo phương thức.
     *
     * @param phuongThuc phương thức thanh toán đang lọc
     * @return panel card
     */
    private JPanel createReceiptCard(String phuongThuc) {
        JPanel pnlCard = new JPanel(new BorderLayout(0, 8));
        pnlCard.setBackground(UiTheme.SURFACE);
        pnlCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        // Tiêu đề card thay đổi theo phương thức
        String tenCard = switch (phuongThuc) {
            case "Tiền mặt"     -> "Chứng từ tiền mặt đã thanh toán";
            case "Chuyển khoản" -> "Chứng từ chuyển khoản đã thanh toán";
            default             -> "Chứng từ đã thanh toán";
        };

        JLabel lblCardTitle = new JLabel(tenCard);
        lblCardTitle.setFont(UiTheme.BRAND_FONT);
        lblCardTitle.setForeground(UiTheme.TEXT);

        pnlCard.add(lblCardTitle, BorderLayout.NORTH);
        pnlCard.add(createTable(phuongThuc), BorderLayout.CENTER);
        return pnlCard;
    }

    /**
     * Tạo bảng chứng từ lọc theo phương thức (dữ liệu minh họa).
     *
     * @param phuongThuc phương thức thanh toán để lọc dữ liệu mẫu
     * @return JScrollPane chứa bảng
     */
    private JScrollPane createTable(String phuongThuc) {
        // Dữ liệu mẫu — lọc theo phương thức
        Object[][] allRows = {
            {"HD-2026-0108", "SC-2026-0108", "Nguyễn Văn An", "1.270.000 ₫", "Tiền mặt"},
        };

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Chứng từ", "Phiếu sửa chữa", "Khách hàng", "Đã thu", "Phương thức"},
                0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        for (Object[] row : allRows) {
            // "Tất cả" lấy hết, ngược lại chỉ lấy đúng phương thức
            if ("Tất cả".equals(phuongThuc) || row[4].equals(phuongThuc)) {
                model.addRow(row);
            }
        }

        tblChungTu = new JTable(model);
        tblChungTu.setFont(UiTheme.CAPTION_FONT.deriveFont(11f));
        tblChungTu.setForeground(UiTheme.TEXT);
        tblChungTu.setBackground(UiTheme.SURFACE);
        tblChungTu.setRowHeight(26);
        tblChungTu.setShowGrid(true);
        tblChungTu.setGridColor(GRID);
        tblChungTu.setIntercellSpacing(new Dimension(1, 1));
        tblChungTu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblChungTu.setFillsViewportHeight(true);
        tblChungTu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JTableHeader tbhChungTu = tblChungTu.getTableHeader();
        tbhChungTu.setFont(UiTheme.BRAND_FONT.deriveFont(11f));
        tbhChungTu.setForeground(UiTheme.TEXT);
        tbhChungTu.setBackground(UiTheme.WINDOW);
        tbhChungTu.setPreferredSize(new Dimension(tbhChungTu.getPreferredSize().width, 26));

        JScrollPane scrChungTu = new JScrollPane(tblChungTu);
        scrChungTu.setBorder(BorderFactory.createLineBorder(GRID));
        scrChungTu.getViewport().setBackground(UiTheme.SURFACE);
        scrChungTu.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        return scrChungTu;
    }

    // ── Nhóm nút ─────────────────────────────────────────────────────────────

    private JPanel createActions(Runnable doiTieuChi, Runnable xemChungTu) {
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlActions.setOpaque(false);
        pnlActions.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));

        JButton btnDoi = createButton("Đổi tiêu chí", false, 112);
        btnDoi.addActionListener(e -> doiTieuChi.run());

        JButton btnXem = createButton("Xem chứng từ", true, 118);
        btnXem.addActionListener(e -> xemChungTu.run());

        pnlActions.add(btnDoi);
        pnlActions.add(btnXem);
        return pnlActions;
    }

    private JButton createButton(String text, boolean primary, int width) {
        JButton btn = new JButton(text);
        UiTheme.styleButton(btn, primary);
        btn.setFont(UiTheme.CAPTION_FONT);
        btn.setPreferredSize(new Dimension(width, 26));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Accessor ─────────────────────────────────────────────────────────────

    /**
     * Trả bảng chứng từ để màn hình cha đọc dòng đang chọn khi tích hợp nghiệp vụ.
     *
     * @return bảng chứng từ đã thanh toán
     */
    public JTable getTblChungTu() {
        return tblChungTu;
    }
}
