package nhom01.app.ui.panel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import nhom01.app.model.InspectionReport;
import nhom01.app.service.InspectionService;
import nhom01.app.ui.dialog.InspectionMessageDialog;
import nhom01.app.ui.dialog.InspectionReportDialog;

public class InspectionReportPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final InspectionService service;
    private final Runnable openWorkflow;
    private final CardLayout layout;
    private final JPanel pages;

    private JLabel listTitleLabel;
    private JLabel listSubtitleLabel;
    private JLabel listMessageLabel;
    private JButton showAllButton;
    private JTable reportTable;
    private DefaultTableModel reportTableModel;
    private JTextField searchField;
    private javax.swing.JComboBox<String> statusCombo;

    public InspectionReportPanel(InspectionService service, Runnable openWorkflow) {
        this.service = service;
        this.openWorkflow = openWorkflow;
        layout = new CardLayout();
        pages = new JPanel(layout);

        setLayout(new BorderLayout());

        pages.add(createListPage(), "LIST");
        pages.add(createSearchPage(), "SEARCH");

        add(pages, BorderLayout.CENTER);

        showAllReports("Sẵn sàng");
    }

    // Tạo màn danh sách báo cáo kiểm tra
    private JPanel createListPage() {
        listTitleLabel = InspectionPanel.createPageTitle("Danh sách báo cáo kiểm tra");
        listSubtitleLabel = InspectionPanel.createSubtitle("");

        JPanel body = InspectionPanel.createScrollContent();

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

        toolbar.setOpaque(false);
        toolbar.setAlignmentX(LEFT_ALIGNMENT);
        toolbar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JButton search = InspectionPanel.createButton("Tìm kiếm", false, false, 142);
        JButton add = InspectionPanel.createButton("Thêm", true, false, 82);
        JButton view = InspectionPanel.createButton("Xem", false, false, 82);
        JButton edit = InspectionPanel.createButton("Sửa", false, false, 82);
        JButton delete = InspectionPanel.createButton("Xóa", false, true, 82);
        JButton related = InspectionPanel.createButton("Dữ liệu liên quan", false, false, 154);
        JButton workflow = InspectionPanel.createButton("Lập kết quả kiểm tra", false, false, 176);

        showAllButton = InspectionPanel.createButton("Toàn bộ danh sách", false, false, 160);
        showAllButton.setVisible(false);

        search.addActionListener(e -> layout.show(pages, "SEARCH"));
        add.addActionListener(e -> openCreateDialog());
        view.addActionListener(e -> openViewDialog(false));
        edit.addActionListener(e -> openEditDialog());
        delete.addActionListener(e -> deleteSelectedReport());
        related.addActionListener(e -> openViewDialog(true));
        workflow.addActionListener(e -> openWorkflow.run());
        showAllButton.addActionListener(e -> showAllReports("Đã quay về toàn bộ danh sách."));

        toolbar.add(search);
        toolbar.add(add);
        toolbar.add(view);
        toolbar.add(edit);
        toolbar.add(delete);
        toolbar.add(related);
        toolbar.add(workflow);
        toolbar.add(showAllButton);

        body.add(toolbar);
        body.add(Box.createVerticalStrut(10));

        reportTableModel = new DefaultTableModel(
                new Object[] { "Số báo cáo", "Phiếu tiếp nhận", "Biển số", "Kỹ thuật viên", "Trạng thái" },
                0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        reportTable = InspectionPanel.createTable(reportTableModel);

        reportTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        reportTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        reportTable.getColumnModel().getColumn(2).setPreferredWidth(170);
        reportTable.getColumnModel().getColumn(3).setPreferredWidth(300);
        reportTable.getColumnModel().getColumn(4).setPreferredWidth(170);

        reportTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = reportTable.rowAtPoint(e.getPoint());

                if (row < 0 || e.getClickCount() != 1) return;

                reportTable.setRowSelectionInterval(row, row);
                openViewDialog(false);
            }
        });

        JScrollPane tableScroll = InspectionPanel.createTableScroll(reportTable, 400);

        body.add(tableScroll);
        body.add(Box.createVerticalStrut(10));

        listMessageLabel = InspectionPanel.createSubtitle(
                "Chọn một hồ sơ trong bảng để xem, sửa hoặc xóa. Thêm luôn sẵn sàng.");

        body.add(listMessageLabel);
        body.add(Box.createVerticalStrut(8));

        body.add(InspectionPanel.createSubtitle(
                "Kỹ thuật chỉ sửa nháp thuộc phân công. "
                        + "Chỉ báo cáo hoàn tất hợp lệ mới làm căn cứ báo giá; "
                        + "báo cáo đã liên kết được giữ lại."));

        return InspectionPanel.createBusinessPage(
                listTitleLabel,
                listSubtitleLabel,
                body,
                null);
    }

    // Tạo màn nhập tiêu chí tìm kiếm
    private JPanel createSearchPage() {
        JPanel body = InspectionPanel.createScrollContent();

        JPanel card = InspectionPanel.createCard(248, Color.WHITE);

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.add(InspectionPanel.createSectionTitle("Tiêu chí tìm kiếm"));
        card.add(Box.createVerticalStrut(12));
        card.add(InspectionPanel.createSmallLabel("Số báo cáo, phiếu tiếp nhận hoặc biển số"));
        card.add(Box.createVerticalStrut(4));

        searchField = new JTextField();

        InspectionPanel.styleTextField(searchField);

        card.add(searchField);
        card.add(Box.createVerticalStrut(12));
        card.add(InspectionPanel.createSmallLabel("Trạng thái kiểm tra"));
        card.add(Box.createVerticalStrut(4));

        statusCombo = InspectionPanel.createComboBox(
                new String[] { "Tất cả", "Nháp", "Hoàn tất" });

        card.add(statusCombo);

        body.add(card);
        body.add(Box.createVerticalStrut(8));

        JPanel guide = InspectionPanel.createCard(78, new Color(240, 240, 240));

        guide.setLayout(new BoxLayout(guide, BoxLayout.Y_AXIS));
        guide.add(InspectionPanel.createSectionTitle("Hướng dẫn"));
        guide.add(Box.createVerticalStrut(4));
        guide.add(InspectionPanel.createSubtitle(
                "Có thể nhập một phần thông tin. Để xem toàn bộ dữ liệu, quay lại màn danh sách."));

        body.add(guide);

        JPanel footer = InspectionPanel.createFooter();

        JButton back = InspectionPanel.createButton("Về danh sách", false, false, 124);
        JButton searchButton = InspectionPanel.createButton("Tìm kiếm", true, false, 108);

        back.addActionListener(e -> showAllReports("Sẵn sàng"));
        searchButton.addActionListener(e -> performSearch());

        footer.add(back);
        footer.add(searchButton);

        return InspectionPanel.createBusinessPage(
                InspectionPanel.createPageTitle("Tìm kiếm báo cáo kiểm tra"),
                InspectionPanel.createSubtitle("Nhập tiêu chí tìm kiếm rồi chuyển sang màn kết quả."),
                body,
                footer);
    }

    // Tìm kiếm và đổi cùng view sang trạng thái có kết quả hoặc không có kết quả
    private void performSearch() {
        String keyword = searchField.getText().trim();

        List<InspectionReport> results = service.searchReports(
                keyword,
                String.valueOf(statusCombo.getSelectedItem()));

        loadReports(results);

        showAllButton.setVisible(true);

        if (results.isEmpty()) {
            listTitleLabel.setText("Không tìm thấy báo cáo kiểm tra");
            listMessageLabel.setText(
                    "<html>Không tìm thấy hồ sơ. Đổi từ khóa hoặc bấm Toàn bộ danh sách.</html>");
        } else {
            listTitleLabel.setText("Kết quả tìm kiếm báo cáo kiểm tra");
            listMessageLabel.setText("<html>Chọn một hồ sơ trong bảng để tiếp tục.</html>");
        }

        listSubtitleLabel.setText(
                "<html>Từ khóa đã dùng: "
                        + (keyword.isBlank() ? "Tất cả" : keyword)
                        + "</html>");

        layout.show(pages, "LIST");
    }

    // Hiển thị lại toàn bộ danh sách báo cáo
    private void showAllReports(String message) {
        loadReports(service.getReports());

        listTitleLabel.setText("Danh sách báo cáo kiểm tra");
        listSubtitleLabel.setText("");
        listMessageLabel.setText("<html>" + message + "</html>");
        showAllButton.setVisible(false);

        layout.show(pages, "LIST");
    }

    // Nạp dữ liệu báo cáo vào JTable
    private void loadReports(List<InspectionReport> reports) {
        reportTableModel.setRowCount(0);

        for (InspectionReport report : reports) {
            reportTableModel.addRow(new Object[] {
                    report.getReportNumber(),
                    report.getReceptionNumber(),
                    report.getLicensePlate(),
                    report.getTechnicianName(),
                    report.getStatus()
            });
        }

        if (reportTable.getRowCount() > 0) {
            reportTable.setRowSelectionInterval(0, 0);
        }
    }

    // Lấy báo cáo đang được chọn trên bảng
    private InspectionReport getSelectedReport() {
        int row = reportTable.getSelectedRow();

        if (row < 0) {
            listMessageLabel.setText(
                    "<html>Chưa chọn dòng. Chọn một hồ sơ trong bảng để tiếp tục.</html>");
            return null;
        }

        return service.findReport(
                String.valueOf(reportTableModel.getValueAt(row, 0)));
    }

    // Lấy Window cha để mở dialog đúng vị trí
    private Window getOwnerWindow() {
        return SwingUtilities.getWindowAncestor(this);
    }

    // Mở dialog thêm báo cáo mới
    private void openCreateDialog() {
        InspectionReportDialog dialog = new InspectionReportDialog(
                getOwnerWindow(),
                InspectionReportDialog.Mode.CREATE,
                service,
                null);

        dialog.setVisible(true);

        InspectionReport report = dialog.getSavedReport();

        if (report == null) return;

        InspectionReport saved = service.createReport(report);

        showAllReports("Đã thêm " + saved.getReportNumber() + ".");
    }

    // Mở dialog sửa báo cáo Nháp
    private void openEditDialog() {
        InspectionReport report = getSelectedReport();

        if (report == null) return;

        if (!"Nháp".equals(report.getStatus())) {
            listMessageLabel.setText(
                    "<html>Chỉ được sửa báo cáo Nháp thuộc phân công.</html>");
            return;
        }

        InspectionReportDialog dialog = new InspectionReportDialog(
                getOwnerWindow(),
                InspectionReportDialog.Mode.EDIT,
                service,
                report);

        dialog.setVisible(true);

        InspectionReport updated = dialog.getSavedReport();

        if (updated != null && service.updateReport(updated)) {
            showAllReports("Đã cập nhật " + updated.getReportNumber() + ".");
        }
    }

    // Mở dialog xem chi tiết hoặc dữ liệu liên quan
    private void openViewDialog(boolean related) {
        InspectionReport report = getSelectedReport();

        if (report == null) return;

        InspectionReportDialog.Mode mode = related
                ? InspectionReportDialog.Mode.RELATED
                : InspectionReportDialog.Mode.VIEW;

        InspectionReportDialog dialog = new InspectionReportDialog(
                getOwnerWindow(),
                mode,
                service,
                report);

        dialog.setVisible(true);

        if (related && dialog.isDeleteRequested()) {
            deleteReport(report);
        }
    }

    // Xóa báo cáo đang được chọn
    private void deleteSelectedReport() {
        InspectionReport report = getSelectedReport();

        if (report != null) {
            deleteReport(report);
        }
    }

    // Kiểm tra ràng buộc và hỏi xác nhận trước khi xóa
    private void deleteReport(InspectionReport report) {
        if (!service.canDelete(report)) {
            InspectionMessageDialog dialog = new InspectionMessageDialog(
                    getOwnerWindow(),
                    InspectionMessageDialog.Type.DELETE_BLOCKED,
                    report.getReportNumber(),
                    report.getReceptionNumber());

            dialog.setVisible(true);

            return;
        }

        InspectionMessageDialog dialog = new InspectionMessageDialog(
                getOwnerWindow(),
                InspectionMessageDialog.Type.DELETE_CONFIRM,
                report.getReportNumber(),
                report.getReceptionNumber());

        dialog.setVisible(true);

        if (dialog.isConfirmed() && service.deleteReport(report.getReportNumber())) {
            showAllReports("Đã xóa " + report.getReportNumber() + ".");
        }
    }
}