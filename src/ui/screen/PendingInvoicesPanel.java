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
 * Màn hình phiếu sửa chữa chờ thanh toán.
 * Gắn vào vùng CENTER của {@code MainFrame} qua {@code ThuNganShell}.
 *
 * <p>Chỉ chứa GUI và dữ liệu minh họa; không kết nối DAO hoặc cơ sở dữ liệu.</p>
 */
public final class PendingInvoicesPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    /** Màu đường kẻ bảng. */
    private static final Color GRID = new Color(0xDCE1E7);

    private final Runnable khiTraCuu;
    private final Runnable khiDoiChieu;
    private final Runnable khiQuayLai;
    /** Bảng phiếu để màn hình cha đọc dòng đang chọn khi tích hợp nghiệp vụ. */
    private JTable tblPhieu;

    /**
     * Tạo giao diện với dữ liệu minh họa và hành động no-op.
     * Dùng khi cần xem trước độc lập.
     */
    public PendingInvoicesPanel() {
        this(() -> { }, () -> { }, () -> { });
    }

    /**
     * Tạo giao diện và nhận hành động cho các nút.
     *
     * @param khiTraCuu   hành động khi bấm "Tra cứu chứng từ"
     * @param khiDoiChieu hành động khi bấm "Đối chiếu phiếu SC-0108"
     * @param khiQuayLai  hành động khi bấm "← Trang chủ"
     */
    public PendingInvoicesPanel(Runnable khiTraCuu, Runnable khiDoiChieu, Runnable khiQuayLai) {
        this.khiTraCuu   = khiTraCuu   != null ? khiTraCuu   : () -> { };
        this.khiDoiChieu = khiDoiChieu != null ? khiDoiChieu : () -> { };
        this.khiQuayLai  = khiQuayLai  != null ? khiQuayLai  : () -> { };

        setLayout(new BorderLayout());
        setBackground(UiTheme.WINDOW);

        JPanel pnlContent = new JPanel(new BorderLayout(0, 8));
        pnlContent.setOpaque(false);
        pnlContent.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        pnlContent.add(createHeading(),  BorderLayout.NORTH);
        pnlContent.add(createListCard(), BorderLayout.CENTER);

        add(pnlContent,     BorderLayout.CENTER);
        add(createActions(), BorderLayout.SOUTH);
    }

    // ── Tiêu đề ──────────────────────────────────────────────────────────────

    private JPanel createHeading() {
        JPanel pnlHeading = new JPanel();
        pnlHeading.setOpaque(false);
        pnlHeading.setLayout(new BoxLayout(pnlHeading, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("Phiếu chờ thanh toán");
        lblTitle.setFont(UiTheme.BRAND_FONT.deriveFont(Font.BOLD, 22f));
        lblTitle.setForeground(UiTheme.TEXT);

        JLabel lblDesc = new JLabel(
                "Chọn một phiếu sửa chữa đã hoàn tất để đối chiếu và thu tiền.");
        lblDesc.setFont(UiTheme.CAPTION_FONT.deriveFont(12f));
        lblDesc.setForeground(UiTheme.TEXT);

        pnlHeading.add(lblTitle);
        pnlHeading.add(lblDesc);
        return pnlHeading;
    }

    // ── Card danh sách ───────────────────────────────────────────────────────

    private JPanel createListCard() {
        JPanel pnlCard = new JPanel(new BorderLayout(0, 8));
        pnlCard.setBackground(UiTheme.SURFACE);
        pnlCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JLabel lblCardTitle = new JLabel("Danh sách phiếu sửa chữa hoàn tất");
        lblCardTitle.setFont(UiTheme.BRAND_FONT);
        lblCardTitle.setForeground(UiTheme.TEXT);

        pnlCard.add(lblCardTitle, BorderLayout.NORTH);
        pnlCard.add(createTable(), BorderLayout.CENTER);
        return pnlCard;
    }

    private JScrollPane createTable() {
        DefaultTableModel model = new DefaultTableModel(
                new Object[][]{
                    {"SC-2026-0108", "Nguyễn Văn An · 51H-246.80", "1.270.000 ₫", "Chờ thu tiền"},
                    {"SC-2026-0111", "Trần Thị Bình · 51A-678.90", "600.000 ₫",   "Chờ thu tiền"},
                    {"SC-2026-0112", "Lê Văn Cường · 51K-001.23",  "200.000 ₫",   "Chờ thu tiền"},
                },
                new String[]{"Phiếu sửa chữa", "Khách / biển số", "Phải thu", "Trạng thái"}) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tblPhieu = new JTable(model);
        tblPhieu.setFont(UiTheme.CAPTION_FONT.deriveFont(11f));
        tblPhieu.setForeground(UiTheme.TEXT);
        tblPhieu.setBackground(UiTheme.SURFACE);
        tblPhieu.setRowHeight(26);
        tblPhieu.setShowGrid(true);
        tblPhieu.setGridColor(GRID);
        tblPhieu.setIntercellSpacing(new Dimension(1, 1));
        tblPhieu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblPhieu.setFillsViewportHeight(true);
        tblPhieu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JTableHeader tbhPhieu = tblPhieu.getTableHeader();
        tbhPhieu.setFont(UiTheme.BRAND_FONT.deriveFont(11f));
        tbhPhieu.setForeground(UiTheme.TEXT);
        tbhPhieu.setBackground(UiTheme.WINDOW);
        tbhPhieu.setPreferredSize(new Dimension(tbhPhieu.getPreferredSize().width, 26));

        JScrollPane scrPhieu = new JScrollPane(tblPhieu);
        scrPhieu.setBorder(BorderFactory.createLineBorder(GRID));
        scrPhieu.getViewport().setBackground(UiTheme.SURFACE);
        scrPhieu.setPreferredSize(new Dimension(700, 110));
        return scrPhieu;
    }

    // ── Nhóm nút ─────────────────────────────────────────────────────────────

    private JPanel createActions() {
        JPanel pnlActions = new JPanel(new BorderLayout());
        pnlActions.setOpaque(false);
        pnlActions.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));

        // Trái: nút quay lại
        JButton btnQuayLai = new JButton("Quay lại");
        UiTheme.styleButton(btnQuayLai, false);
        btnQuayLai.setFont(UiTheme.CAPTION_FONT);
        btnQuayLai.setPreferredSize(new Dimension(110, 26));
        btnQuayLai.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnQuayLai.addActionListener(e -> khiQuayLai.run());
        pnlActions.add(btnQuayLai, BorderLayout.WEST);

        // Phải: tra cứu + đối chiếu
        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlRight.setOpaque(false);

        JButton btnTraCuu = new JButton("Tra cứu chứng từ");
        UiTheme.styleButton(btnTraCuu, false);
        btnTraCuu.setFont(UiTheme.CAPTION_FONT);
        btnTraCuu.setPreferredSize(new Dimension(138, 26));
        btnTraCuu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTraCuu.addActionListener(e -> khiTraCuu.run());

        JButton btnDoiChieu = new JButton("Đối chiếu phiếu SC-0108");
        UiTheme.styleButton(btnDoiChieu, true);
        btnDoiChieu.setFont(UiTheme.CAPTION_FONT);
        btnDoiChieu.setPreferredSize(new Dimension(176, 26));
        btnDoiChieu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDoiChieu.addActionListener(e -> khiDoiChieu.run());

        pnlRight.add(btnTraCuu);
        pnlRight.add(btnDoiChieu);
        pnlActions.add(pnlRight, BorderLayout.EAST);
        return pnlActions;
    }

    // ── Accessor ─────────────────────────────────────────────────────────────

    /**
     * Trả bảng phiếu để màn hình cha đọc dòng đang chọn khi tích hợp nghiệp vụ.
     *
     * @return bảng phiếu sửa chữa chờ thanh toán
     */
    public JTable getTblPhieu() {
        return tblPhieu;
    }
}
