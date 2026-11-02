package ui.screen.thungan;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import ui.component.UiTheme;

/** Màn hình kết quả rỗng khi tra cứu chứng từ không tìm thấy bản ghi nào. */
public final class DocumentNotFoundPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    /** Tạo màn hình không tìm thấy chứng từ; nút "Đổi tiêu chí" không có hành động. */
    public DocumentNotFoundPanel() {
        this(null);
    }

    /** Tạo màn hình không tìm thấy chứng từ. */
    public DocumentNotFoundPanel(ActionListener khiDoiTieuChi) {
        setLayout(new BorderLayout(0, 8));
        setBackground(UiTheme.WINDOW);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(createHeading(), BorderLayout.NORTH);
        add(createCard(), BorderLayout.CENTER);
        add(createActions(khiDoiTieuChi), BorderLayout.SOUTH);
    }

    // ── Tiêu đề ──────────────────────────────────────────────────────────────

    /** Tạo heading cho giao diện. */
    private JPanel createHeading() {
        JPanel pnlHeading = new JPanel();
        pnlHeading.setOpaque(false);
        pnlHeading.setLayout(new BoxLayout(pnlHeading, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("Không tìm thấy chứng từ");
        lblTitle.setFont(UiTheme.BRAND_FONT.deriveFont(Font.BOLD, 22f));
        lblTitle.setForeground(UiTheme.TEXT);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Chứng từ đã phát hành chỉ được xem, in hoặc xuất.");
        lblSubtitle.setFont(UiTheme.CAPTION_FONT.deriveFont(12f));
        lblSubtitle.setForeground(UiTheme.TEXT);
        lblSubtitle.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        pnlHeading.add(lblTitle);
        pnlHeading.add(lblSubtitle);
        return pnlHeading;
    }

    // ── Card bảng kết quả ────────────────────────────────────────────────────

    /** Tạo card cho giao diện. */
    private JPanel createCard() {
        JPanel pnlCard = new JPanel(new BorderLayout(0, 8));
        pnlCard.setBackground(UiTheme.SURFACE);
        pnlCard.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.BORDER),
                BorderFactory.createEmptyBorder(16, 12, 12, 12)));

        JLabel lblCardTitle = new JLabel("Chứng từ đã thanh toán");
        lblCardTitle.setFont(UiTheme.BRAND_FONT.deriveFont(Font.BOLD, 14f));
        lblCardTitle.setForeground(UiTheme.TEXT);
        pnlCard.add(lblCardTitle, BorderLayout.NORTH);
        pnlCard.add(createResultArea(), BorderLayout.CENTER);
        return pnlCard;
    }

    /** Tạo result area cho giao diện. */
    private JPanel createResultArea() {
        JPanel pnlResult = new JPanel(new BorderLayout(0, 8));
        pnlResult.setOpaque(false);
        pnlResult.add(createEmptyTable(), BorderLayout.NORTH);

        JLabel lblEmpty = new JLabel("Không có chứng từ khớp tiêu chí. Chọn Đổi tiêu chí để tìm lại.",
                SwingConstants.CENTER);
        lblEmpty.setFont(UiTheme.BODY_FONT);
        lblEmpty.setForeground(UiTheme.TEXT);
        lblEmpty.setHorizontalAlignment(SwingConstants.CENTER);
        pnlResult.add(lblEmpty, BorderLayout.CENTER);
        return pnlResult;
    }

    /** Tạo bảng rỗng chỉ hiển thị header — không có dòng dữ liệu. */
    private JScrollPane createEmptyTable() {
        DefaultTableModel model = new DefaultTableModel(
                new String[] { "Chứng từ", "Phiếu sửa chữa", "Khách hàng", "Đã thu", "Phương thức" }, 0) {
            private static final long serialVersionUID = 1L;

            /** Cho biết cell editable. */
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tblKetQua = new JTable(model);

        tblKetQua.getTableHeader().setReorderingAllowed(false);

        tblKetQua.getTableHeader().setResizingAllowed(false);
        tblKetQua.setFont(UiTheme.BODY_FONT);
        tblKetQua.setForeground(UiTheme.TEXT);
        tblKetQua.setRowHeight(30);
        tblKetQua.setShowGrid(true);
        tblKetQua.setGridColor(UiTheme.BORDER);
        tblKetQua.setFillsViewportHeight(false);

        // Header căn trái, nền WINDOW
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        headerRenderer.setBackground(UiTheme.WINDOW);
        headerRenderer.setForeground(UiTheme.TEXT);
        headerRenderer.setBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, UiTheme.BORDER),
                        BorderFactory.createEmptyBorder(0, 8, 0, 8)));

        tblKetQua.getTableHeader().setFont(UiTheme.BRAND_FONT.deriveFont(13f));
        tblKetQua.getTableHeader().setBackground(UiTheme.WINDOW);
        tblKetQua.getTableHeader().setForeground(UiTheme.TEXT);
        tblKetQua.getTableHeader().setPreferredSize(new Dimension(0, 32));
        for (int i = 0; i < tblKetQua.getColumnModel().getColumnCount(); i++) {
            tblKetQua.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        JScrollPane scrKetQua = new JScrollPane(tblKetQua);
        scrKetQua.setBorder(BorderFactory.createEmptyBorder());
        scrKetQua.getViewport().setBackground(UiTheme.SURFACE);
        // Chỉ hiện header, không có dòng dữ liệu.
        scrKetQua.setPreferredSize(new Dimension(0, 32));
        return scrKetQua;
    }

    // ── Nhóm nút ─────────────────────────────────────────────────────────────

    /** Tạo actions cho giao diện. */
    private JPanel createActions(ActionListener khiDoiTieuChi) {
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlActions.setOpaque(false);

        JButton btnDoiTieuChi = new JButton("Đổi tiêu chí");
        UiTheme.styleButton(btnDoiTieuChi, false);
        btnDoiTieuChi.setFont(UiTheme.BODY_FONT);
        btnDoiTieuChi.setPreferredSize(new Dimension(112, 32));
        btnDoiTieuChi.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (khiDoiTieuChi != null) {
            btnDoiTieuChi.addActionListener(khiDoiTieuChi);
        }
        pnlActions.add(btnDoiTieuChi);
        return pnlActions;
    }
}
