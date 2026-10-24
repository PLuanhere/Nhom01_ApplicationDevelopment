package ui.screen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
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
 * Màn hình chi tiết doanh thu đã thu trong ngày được chọn.
 *
 * <p>Hiển thị tổng tiền, bảng phân tích theo phương thức thanh toán và tóm tắt.
 * Nút "Đóng chi tiết" gọi {@code khiDong} để màn hình cha quay lại dashboard.</p>
 *
 * <p>Chỉ chứa GUI và dữ liệu minh họa; không kết nối DAO hoặc cơ sở dữ liệu.</p>
 */
public final class DashDetailCashierPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    /** Màu đường kẻ bảng theo thiết kế. */
    private static final Color GRID = new Color(0xDCE1E7);

    /** Hành động do màn hình cha cung cấp khi bấm Đóng chi tiết. */
    private final Runnable khiDong;

    /**
     * Tạo màn hình chi tiết với hành động đóng không làm thay đổi layout ngoài.
     * Dùng khi cần xem trước độc lập.
     */
    public DashDetailCashierPanel() {
        this(() -> { });
    }

    /**
     * Tạo màn hình chi tiết.
     *
     * @param khiDong hành động do màn hình cha cung cấp khi bấm Đóng chi tiết;
     *                null sẽ được coi là no-op
     */
    public DashDetailCashierPanel(Runnable khiDong) {
        this.khiDong = khiDong != null ? khiDong : () -> { };
        setLayout(new BorderLayout(0, 8));
        setBackground(UiTheme.WINDOW);

        add(createTitleBar(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
        add(createActions(), BorderLayout.SOUTH);
    }

    // ── Thanh tiêu đề ────────────────────────────────────────────────────────

    /**
     * Tạo thanh tiêu đề nền trắng với tên màn hình.
     *
     * @return panel tiêu đề
     */
    private JPanel createTitleBar() {
        JPanel pnlTitle = new JPanel(new BorderLayout());
        pnlTitle.setBackground(UiTheme.SURFACE);
        pnlTitle.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel lblTitle = new JLabel("Chi tiết doanh thu đã thu");
        lblTitle.setFont(UiTheme.BRAND_FONT.deriveFont(Font.BOLD, 18f));
        lblTitle.setForeground(UiTheme.TEXT);
        pnlTitle.add(lblTitle, BorderLayout.CENTER);
        return pnlTitle;
    }

    // ── Nội dung chính ───────────────────────────────────────────────────────

    /**
     * Tạo vùng nội dung gồm bộ lọc, thẻ tổng tiền, bảng phân tích và dòng tóm tắt.
     *
     * @return panel nội dung
     */
    private JPanel createContent() {
        JPanel pnlContent = new JPanel();
        pnlContent.setOpaque(false);
        pnlContent.setBorder(BorderFactory.createEmptyBorder(16, 16, 0, 16));
        pnlContent.setLayout(new BoxLayout(pnlContent, BoxLayout.Y_AXIS));

        // Dòng bộ lọc
        JLabel lblFilter = new JLabel(
                "Ngày 05/10/2026 • Toàn garage • Chỉ tính phiếu đã xác nhận thanh toán • Đơn vị: đồng");
        lblFilter.setFont(UiTheme.CAPTION_FONT.deriveFont(12f));
        lblFilter.setForeground(UiTheme.TEXT);
        lblFilter.setAlignmentX(LEFT_ALIGNMENT);
        pnlContent.add(lblFilter);
        pnlContent.add(Box.createVerticalStrut(8));

        // Thẻ tổng tiền
        JPanel pnlTotal = createCard();
        pnlTotal.setLayout(new BoxLayout(pnlTotal, BoxLayout.Y_AXIS));
        pnlTotal.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));
        pnlTotal.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblTotalTitle = sectionLabel("Tổng tiền đã thu", 14);
        JLabel lblAmount = new JLabel("4.220.000 ₫");
        lblAmount.setFont(UiTheme.BRAND_FONT.deriveFont(Font.BOLD, 26f));
        lblAmount.setForeground(UiTheme.TEXT);
        pnlTotal.add(lblTotalTitle);
        pnlTotal.add(Box.createVerticalStrut(2));
        pnlTotal.add(lblAmount);
        pnlContent.add(pnlTotal);
        pnlContent.add(Box.createVerticalStrut(8));

        // Thẻ bảng phân tích
        JPanel pnlBreakdown = createCard();
        pnlBreakdown.setLayout(new BorderLayout(0, 8));
        pnlBreakdown.setAlignmentX(LEFT_ALIGNMENT);
        pnlBreakdown.setMaximumSize(new Dimension(Integer.MAX_VALUE, 158));
        pnlBreakdown.add(
                sectionLabel("Chi tiết theo phương thức thanh toán", 14),
                BorderLayout.NORTH);
        pnlBreakdown.add(createReadOnlyTable(), BorderLayout.CENTER);
        pnlContent.add(pnlBreakdown);
        pnlContent.add(Box.createVerticalStrut(8));

        // Dòng tóm tắt
        JLabel lblSummary = new JLabel("3 phiếu đã thanh toán • Tổng thực thu 4.220.000 ₫.");
        lblSummary.setFont(UiTheme.SHELL_FONT);
        lblSummary.setForeground(UiTheme.TEXT);
        lblSummary.setAlignmentX(LEFT_ALIGNMENT);
        pnlContent.add(lblSummary);

        return pnlContent;
    }

    // ── Bảng phân tích ───────────────────────────────────────────────────────

    /**
     * Tạo bảng phân tích thanh toán chỉ đọc theo phương thức (dữ liệu minh họa).
     *
     * @return JScrollPane chứa bảng; không có thanh cuộn dọc vì bảng chỉ 2 dòng
     */
    private JScrollPane createReadOnlyTable() {
        DefaultTableModel model = new DefaultTableModel(
                new Object[][]{
                    {"Tiền mặt",     "2", "3.370.000"},
                    {"Chuyển khoản", "1", "850.000"},
                },
                new String[]{"Phương thức", "Số phiếu", "Số tiền (đồng)"}) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable tblPhuongThuc = new JTable(model);
        tblPhuongThuc.setFont(UiTheme.SHELL_FONT);
        tblPhuongThuc.setForeground(UiTheme.TEXT);
        tblPhuongThuc.setBackground(UiTheme.SURFACE);
        tblPhuongThuc.setRowHeight(32);
        tblPhuongThuc.setShowGrid(true);
        tblPhuongThuc.setGridColor(GRID);
        tblPhuongThuc.setIntercellSpacing(new Dimension(1, 1));
        tblPhuongThuc.setFillsViewportHeight(true);
        tblPhuongThuc.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader tbhPhuongThuc = tblPhuongThuc.getTableHeader();
        tbhPhuongThuc.setFont(UiTheme.BRAND_FONT.deriveFont(13f));
        tbhPhuongThuc.setForeground(UiTheme.TEXT);
        tbhPhuongThuc.setBackground(UiTheme.WINDOW);
        tbhPhuongThuc.setPreferredSize(
                new Dimension(tbhPhuongThuc.getPreferredSize().width, 32));

        JScrollPane scrPhuongThuc = new JScrollPane(tblPhuongThuc);
        scrPhuongThuc.setBorder(BorderFactory.createLineBorder(GRID));
        scrPhuongThuc.getViewport().setBackground(UiTheme.SURFACE);
        // Bảng chỉ có hai dòng, không cần cuộn dọc.
        scrPhuongThuc.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        return scrPhuongThuc;
    }

    // ── Nhóm nút ─────────────────────────────────────────────────────────────

    /**
     * Tạo vùng chứa nút Đóng chi tiết căn phải.
     *
     * @return panel nhóm nút
     */
    private JPanel createActions() {
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlActions.setOpaque(false);
        pnlActions.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));

        JButton btnDong = new JButton("Đóng chi tiết");
        UiTheme.styleButton(btnDong, true);
        btnDong.setFont(UiTheme.SHELL_FONT);
        btnDong.setPreferredSize(new Dimension(152, 32));
        btnDong.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDong.addActionListener(e -> khiDong.run());
        pnlActions.add(btnDong);
        return pnlActions;
    }

    // ── Tiện ích ─────────────────────────────────────────────────────────────

    /**
     * Tạo thẻ nền trắng có viền và lề trong.
     *
     * @return panel thẻ chưa gắn layout
     */
    private JPanel createCard() {
        JPanel pnlCard = new JPanel();
        pnlCard.setBackground(UiTheme.SURFACE);
        pnlCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        return pnlCard;
    }

    /**
     * Tạo nhãn tiêu đề phần với font đậm.
     *
     * @param text nội dung nhãn
     * @param size cỡ chữ tính bằng pt
     * @return nhãn đã định dạng
     */
    private JLabel sectionLabel(String text, int size) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UiTheme.BRAND_FONT.deriveFont(Font.BOLD, (float) size));
        lbl.setForeground(UiTheme.TEXT);
        return lbl;
    }
}
