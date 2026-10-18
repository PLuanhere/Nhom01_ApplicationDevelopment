package nhom01.app.ui.panel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import nhom01.app.model.InspectionQueueItem;
import nhom01.app.model.InspectionReport;
import nhom01.app.service.InspectionService;

public class InspectionWorkflowPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final InspectionService service;
    private final Runnable showReports;
    private final Runnable showReceptionShell;
    private final Runnable showTechnicalShell;
    private final CardLayout layout;
    private final JPanel pages;

    private JTable queueTable;
    private DefaultTableModel queueTableModel;

    private JLabel workInfoLabel;
    private JLabel requestValueLabel;
    private JLabel conditionValueLabel;

    private JTextArea brakeArea;
    private JTextArea oilArea;
    private JTextArea electricalArea;
    private JTextArea diagnosticArea;
    private JTextArea proposalArea;

    private JLabel reviewReceptionValue;
    private JLabel reviewVehicleValue;
    private JLabel reviewTechnicianValue;
    private DefaultTableModel reviewTableModel;
    private JCheckBox reviewCheckBox;

    private JLabel completedTitleInfo;
    private JLabel completedReceptionValue;
    private JLabel completedPlateValue;
    private JLabel completedTechnicianValue;
    private DefaultTableModel completedTableModel;

    private JLabel invalidReceptionValue;

    private JLabel requiredInfoLabel;
    private JLabel requiredRequestValue;
    private JLabel requiredConditionValue;

    private JLabel saveErrorReceptionValue;
    private JLabel saveErrorVehicleValue;
    private JLabel saveErrorTechnicianValue;
    private DefaultTableModel saveErrorTableModel;

    private InspectionQueueItem selectedQueueItem;

    public InspectionWorkflowPanel(
            InspectionService service,
            Runnable showReports,
            Runnable showReceptionShell,
            Runnable showTechnicalShell) {
        this.service = service;
        this.showReports = showReports;
        this.showReceptionShell = showReceptionShell;
        this.showTechnicalShell = showTechnicalShell;

        layout = new CardLayout();
        pages = new JPanel(layout);

        setLayout(new BorderLayout());

        pages.add(createQueuePage(), "QUEUE");
        pages.add(createWorkPage(), "WORK");
        pages.add(createReviewPage(), "REVIEW");
        pages.add(createCompletedPage(), "COMPLETED");
        pages.add(createInvalidReceptionPage(), "INVALID");
        pages.add(createRequiredErrorPage(), "REQUIRED");
        pages.add(createSaveErrorPage(), "SAVE_ERROR");

        add(pages, BorderLayout.CENTER);

        layout.show(pages, "QUEUE");
    }

    // Tạo màn danh sách xe chờ kiểm tra
    private JPanel createQueuePage() {
        JPanel body = InspectionPanel.createScrollContent();

        JPanel card = InspectionPanel.createCard(392, Color.WHITE);

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.add(InspectionPanel.createSectionTitle("Phiếu đã được phân công kiểm tra"));
        card.add(Box.createVerticalStrut(8));

        queueTableModel = new DefaultTableModel(
                new Object[] { "Phiếu tiếp nhận", "Biển số", "Yêu cầu", "Trạng thái" },
                0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        queueTable = InspectionPanel.createTable(queueTableModel);

        queueTable.getColumnModel().getColumn(0).setPreferredWidth(190);
        queueTable.getColumnModel().getColumn(1).setPreferredWidth(170);
        queueTable.getColumnModel().getColumn(2).setPreferredWidth(510);
        queueTable.getColumnModel().getColumn(3).setPreferredWidth(170);

        card.add(InspectionPanel.createTableScroll(queueTable, 332));

        body.add(card);

        JPanel footer = InspectionPanel.createFooter();

        JButton back = InspectionPanel.createButton("Quay lại", false, false, 108);
        JButton create = InspectionPanel.createButton("Lập báo cáo kiểm tra", true, false, 184);

        back.addActionListener(e -> showReports.run());
        create.addActionListener(e -> startInspection());

        footer.add(back);
        footer.add(create);

        return InspectionPanel.createBusinessPage(
                InspectionPanel.createPageTitle("Xe chờ kiểm tra"),
                InspectionPanel.createSubtitle(
                        "Chọn một phiếu tiếp nhận hợp lệ để lập báo cáo kiểm tra."),
                body,
                footer);
    }

    // Tạo màn nhập kết quả kiểm tra xe
    private JPanel createWorkPage() {
        JPanel body = InspectionPanel.createScrollContent();

        JPanel receptionCard = InspectionPanel.createCard(142, Color.WHITE);

        receptionCard.setLayout(new BoxLayout(receptionCard, BoxLayout.Y_AXIS));
        receptionCard.add(InspectionPanel.createSectionTitle(
                "Thông tin được chuyển từ tiếp nhận"));

        receptionCard.add(Box.createVerticalStrut(8));
        receptionCard.add(InspectionPanel.createSmallLabel("Yêu cầu khách"));

        requestValueLabel = InspectionPanel.createValueLabel("");

        receptionCard.add(requestValueLabel);
        receptionCard.add(Box.createVerticalStrut(8));
        receptionCard.add(InspectionPanel.createSmallLabel("Tình trạng ban đầu"));

        conditionValueLabel = InspectionPanel.createValueLabel("");

        receptionCard.add(conditionValueLabel);

        body.add(receptionCard);
        body.add(Box.createVerticalStrut(8));

        JPanel resultCard = InspectionPanel.createCard(456, Color.WHITE);

        resultCard.setLayout(new BoxLayout(resultCard, BoxLayout.Y_AXIS));
        resultCard.add(InspectionPanel.createSectionTitle("Kết quả kiểm tra"));
        resultCard.add(Box.createVerticalStrut(8));

        brakeArea = addTextAreaField(resultCard, "Hệ thống phanh *");
        oilArea = addTextAreaField(resultCard, "Khoang máy / dầu *");
        electricalArea = addTextAreaField(resultCard, "Điện / điều hòa");
        diagnosticArea = addTextAreaField(resultCard, "Mã lỗi chẩn đoán nếu có");
        proposalArea = addTextAreaField(resultCard, "Hạng mục đề xuất *");

        body.add(resultCard);

        JPanel footer = InspectionPanel.createFooter();

        JButton back = InspectionPanel.createButton("Quay lại", false, false, 108);
        JButton review = InspectionPanel.createButton("Xem lại báo cáo", true, false, 148);

        back.addActionListener(e -> showQueuePage());
        review.addActionListener(e -> reviewInspection());

        footer.add(back);
        footer.add(review);

        workInfoLabel = InspectionPanel.createSubtitle("");

        return InspectionPanel.createBusinessPage(
                InspectionPanel.createPageTitle("Lập báo cáo kiểm tra xe"),
                workInfoLabel,
                body,
                footer);
    }

    // Tạo màn đối chiếu trước khi hoàn tất kiểm tra
    private JPanel createReviewPage() {
        JPanel body = InspectionPanel.createScrollContent();

        body.add(createLinkedCard(false));
        body.add(Box.createVerticalStrut(8));
        body.add(createReviewTableCard(false));
        body.add(Box.createVerticalStrut(8));

        JPanel checkCard = InspectionPanel.createCard(70, Color.WHITE);

        checkCard.setLayout(new BorderLayout());
        checkCard.add(InspectionPanel.createSectionTitle("Đối chiếu"), BorderLayout.NORTH);

        reviewCheckBox = new JCheckBox(
                "Kết quả đầy đủ, đúng xe và đúng phiếu tiếp nhận.");

        reviewCheckBox.setSelected(true);
        reviewCheckBox.setOpaque(false);
        reviewCheckBox.setFont(new Font("Noto Sans", Font.PLAIN, 13));
        reviewCheckBox.setForeground(new Color(32, 43, 56));
        reviewCheckBox.setFocusPainted(false);
        reviewCheckBox.setIcon(new CheckIcon(false));
        reviewCheckBox.setSelectedIcon(new CheckIcon(true));

        JPanel checkWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));

        checkWrap.setOpaque(false);
        checkWrap.add(reviewCheckBox);

        checkCard.add(checkWrap, BorderLayout.CENTER);

        body.add(checkCard);
        body.add(Box.createVerticalStrut(8));
        body.add(createAfterConfirmCard());

        JPanel footer = InspectionPanel.createFooter();

        JButton edit = InspectionPanel.createButton("Chỉnh sửa", false, false, 92);
        JButton complete = InspectionPanel.createButton("Hoàn tất kiểm tra", true, false, 154);

        edit.addActionListener(e -> showPage("WORK"));
        complete.addActionListener(e -> completeInspection());

        footer.add(edit);
        footer.add(complete);

        return InspectionPanel.createBusinessPage(
                InspectionPanel.createPageTitle("Đối chiếu báo cáo kiểm tra"),
                InspectionPanel.createSubtitle(
                        "Xem lại kết quả trước khi xác nhận hoàn tất."),
                body,
                footer);
    }

    // Tạo màn báo cáo đã hoàn tất và bàn giao sang Tiếp nhận
    private JPanel createCompletedPage() {
        JPanel body = InspectionPanel.createScrollContent();

        JPanel infoCard = InspectionPanel.createCard(134, Color.WHITE);

        infoCard.setLayout(new BorderLayout());

        JLabel infoTitle = InspectionPanel.createSectionTitle("Thông tin báo cáo");

        infoTitle.setBorder(new EmptyBorder(0, 0, 8, 0));

        infoCard.add(infoTitle, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 12, 8));

        grid.setOpaque(false);

        completedReceptionValue = InspectionPanel.createValueLabel("");
        completedPlateValue = InspectionPanel.createValueLabel("");
        completedTechnicianValue = InspectionPanel.createValueLabel("");

        grid.add(createInfoBlock("Phiếu tiếp nhận", completedReceptionValue));
        grid.add(createInfoBlock("Biển số", completedPlateValue));
        grid.add(createInfoBlock("Kỹ thuật viên", completedTechnicianValue));
        grid.add(createInfoBlock(
                "Hoàn tất",
                InspectionPanel.createValueLabel("05/10/2026 · 10:05")));

        infoCard.add(grid, BorderLayout.CENTER);

        body.add(infoCard);
        body.add(Box.createVerticalStrut(8));

        JPanel resultCard = InspectionPanel.createCard(168, Color.WHITE);

        resultCard.setLayout(new BorderLayout(0, 8));
        resultCard.add(
                InspectionPanel.createSectionTitle("Kết luận kiểm tra"),
                BorderLayout.NORTH);

        completedTableModel = createResultTableModel();

        JTable completedTable = InspectionPanel.createTable(completedTableModel);

        resultCard.add(
                InspectionPanel.createTableScroll(completedTable, 122),
                BorderLayout.CENTER);

        body.add(resultCard);
        body.add(Box.createVerticalStrut(8));

        JPanel successCard = InspectionPanel.createCard(
                62,
                new Color(237, 247, 239));

        successCard.setLayout(new BoxLayout(successCard, BoxLayout.Y_AXIS));

        JLabel successTitle = InspectionPanel.createSectionTitle("Kiểm tra đã hoàn tất");

        successTitle.setForeground(new Color(23, 99, 57));

        successCard.add(successTitle);
        successCard.add(Box.createVerticalStrut(4));
        successCard.add(InspectionPanel.createValueLabel(
                "Nhân viên tiếp nhận lập báo giá từ kết quả này. "
                        + "Khách chưa xác nhận báo giá nên chưa bắt đầu sửa chữa."));

        body.add(successCard);

        JPanel footer = InspectionPanel.createFooter();

        footer.add(InspectionPanel.createButton("Xem tiếp nhận", false, false, 124));
        footer.add(InspectionPanel.createButton("Lập báo giá", true, false, 108));

        completedTitleInfo = InspectionPanel.createSubtitle("");

        return InspectionPanel.createBusinessPage(
                InspectionPanel.createPageTitle("Báo cáo kiểm tra hoàn tất"),
                completedTitleInfo,
                body,
                footer);
    }

    // Tạo trạng thái phiếu tiếp nhận chưa hợp lệ W_E04
    private JPanel createInvalidReceptionPage() {
        JPanel body = InspectionPanel.createScrollContent();

        JPanel card = InspectionPanel.createCard(180, Color.WHITE);

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.add(InspectionPanel.createSectionTitle("Phiếu đang chọn"));
        card.add(Box.createVerticalStrut(8));

        invalidReceptionValue = InspectionPanel.createValueLabel("");

        card.add(invalidReceptionValue);
        card.add(Box.createVerticalStrut(20));
        card.add(InspectionPanel.createSectionTitle("Cách xử lý"));
        card.add(Box.createVerticalStrut(8));
        card.add(InspectionPanel.createValueLabel(
                "Nhân viên tiếp nhận cần hoàn tất và xác nhận phiếu. "
                        + "Kỹ thuật viên chọn một phiếu hợp lệ đã được phân công để tiếp tục."));

        body.add(card);

        JPanel footer = InspectionPanel.createFooter();

        JButton choose = InspectionPanel.createButton("Chọn phiếu khác", true, false, 144);

        choose.addActionListener(e -> showQueuePage());

        footer.add(choose);

        return InspectionPanel.createBusinessPage(
                InspectionPanel.createPageTitle("Phiếu tiếp nhận chưa hợp lệ"),
                InspectionPanel.createSubtitle(
                        "Chưa được phép lập báo cáo kiểm tra cho phiếu này."),
                body,
                footer);
    }

    // Tạo trạng thái thiếu kết quả bắt buộc W_E05
    private JPanel createRequiredErrorPage() {
        JPanel body = InspectionPanel.createScrollContent();

        JPanel warning = InspectionPanel.createCard(
                70,
                new Color(253, 239, 238));

        warning.setLayout(new BoxLayout(warning, BoxLayout.Y_AXIS));

        JLabel warningTitle = InspectionPanel.createSectionTitle("Thiếu kết quả bắt buộc");

        warningTitle.setForeground(new Color(179, 38, 30));

        warning.add(warningTitle);
        warning.add(Box.createVerticalStrut(4));
        warning.add(InspectionPanel.createValueLabel(
                "Hãy bổ sung kết quả hệ thống phanh và hạng mục đề xuất trước khi xem lại báo cáo."));

        body.add(warning);
        body.add(Box.createVerticalStrut(8));

        JPanel receptionCard = InspectionPanel.createCard(142, Color.WHITE);

        receptionCard.setLayout(new BoxLayout(receptionCard, BoxLayout.Y_AXIS));
        receptionCard.add(InspectionPanel.createSectionTitle(
                "Thông tin được chuyển từ tiếp nhận"));

        receptionCard.add(Box.createVerticalStrut(8));
        receptionCard.add(InspectionPanel.createSmallLabel("Yêu cầu khách"));

        requiredRequestValue = InspectionPanel.createValueLabel("");

        receptionCard.add(requiredRequestValue);
        receptionCard.add(Box.createVerticalStrut(8));
        receptionCard.add(InspectionPanel.createSmallLabel("Tình trạng ban đầu"));

        requiredConditionValue = InspectionPanel.createValueLabel("");

        receptionCard.add(requiredConditionValue);

        body.add(receptionCard);
        body.add(Box.createVerticalStrut(8));

        JPanel missingCard = InspectionPanel.createCard(176, Color.WHITE);

        missingCard.setLayout(new GridLayout(2, 2, 12, 8));

        missingCard.add(createInfoBlock(
                "Hệ thống phanh *",
                createDangerValue("Chưa nhập kết quả")));

        missingCard.add(createInfoBlock(
                "Hạng mục đề xuất *",
                createDangerValue("Chưa nhập đề xuất")));

        missingCard.add(createInfoBlock(
                "Điện / điều hòa",
                InspectionPanel.createValueLabel("Hoạt động bình thường.")));

        missingCard.add(createTransparentPanel());

        body.add(missingCard);

        JPanel footer = InspectionPanel.createFooter();

        JButton supplement = InspectionPanel.createButton("Bổ sung kết quả", true, false, 148);

        supplement.addActionListener(e -> showPage("WORK"));

        footer.add(supplement);

        requiredInfoLabel = InspectionPanel.createSubtitle("");

        return InspectionPanel.createBusinessPage(
                InspectionPanel.createPageTitle("Thiếu kết quả bắt buộc"),
                requiredInfoLabel,
                body,
                footer);
    }

    // Tạo trạng thái không lưu được báo cáo W_E06
    private JPanel createSaveErrorPage() {
        JPanel body = InspectionPanel.createScrollContent();

        JPanel warning = InspectionPanel.createCard(
                70,
                new Color(253, 239, 238));

        warning.setLayout(new BoxLayout(warning, BoxLayout.Y_AXIS));

        JLabel warningTitle = InspectionPanel.createSectionTitle(
                "Không lưu được báo cáo kiểm tra");

        warningTitle.setForeground(new Color(179, 38, 30));

        warning.add(warningTitle);
        warning.add(Box.createVerticalStrut(4));
        warning.add(InspectionPanel.createValueLabel(
                "Báo cáo chưa được ghi nhận hoàn tất. "
                        + "Phiếu tiếp nhận và dữ liệu trước đó được giữ nguyên."));

        body.add(warning);
        body.add(Box.createVerticalStrut(8));
        body.add(createLinkedCard(true));
        body.add(Box.createVerticalStrut(8));
        body.add(createReviewTableCard(true));
        body.add(Box.createVerticalStrut(8));
        body.add(createAfterConfirmCard());

        JPanel footer = InspectionPanel.createFooter();

        JButton edit = InspectionPanel.createButton("Chỉnh sửa", false, false, 92);
        JButton retry = InspectionPanel.createButton("Thử lưu lại", true, false, 120);

        edit.addActionListener(e -> showPage("WORK"));
        retry.addActionListener(e -> completeInspection());

        footer.add(edit);
        footer.add(retry);

        return InspectionPanel.createBusinessPage(
                InspectionPanel.createPageTitle("Không lưu được báo cáo kiểm tra"),
                InspectionPanel.createSubtitle(
                        "Xem lại kết quả trước khi xác nhận hoàn tất."),
                body,
                footer);
    }

    // Tạo card thông tin phiếu liên kết
    private JPanel createLinkedCard(boolean saveError) {
        JPanel card = InspectionPanel.createCard(134, Color.WHITE);

        card.setLayout(new BorderLayout());

        JLabel title = InspectionPanel.createSectionTitle("Phiếu liên kết");

        title.setBorder(new EmptyBorder(0, 0, 8, 0));

        card.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 12, 8));

        grid.setOpaque(false);

        if (saveError) {
            saveErrorReceptionValue = InspectionPanel.createValueLabel("");
            saveErrorVehicleValue = InspectionPanel.createValueLabel("");
            saveErrorTechnicianValue = InspectionPanel.createValueLabel("");

            grid.add(createInfoBlock("Mã phiếu", saveErrorReceptionValue));
            grid.add(createInfoBlock("Phương tiện", saveErrorVehicleValue));
            grid.add(createInfoBlock("Kỹ thuật viên", saveErrorTechnicianValue));
        } else {
            reviewReceptionValue = InspectionPanel.createValueLabel("");
            reviewVehicleValue = InspectionPanel.createValueLabel("");
            reviewTechnicianValue = InspectionPanel.createValueLabel("");

            grid.add(createInfoBlock("Mã phiếu", reviewReceptionValue));
            grid.add(createInfoBlock("Phương tiện", reviewVehicleValue));
            grid.add(createInfoBlock("Kỹ thuật viên", reviewTechnicianValue));
        }

        grid.add(createTransparentPanel());

        card.add(grid, BorderLayout.CENTER);

        return card;
    }

    // Tạo card bảng kết quả dùng cho đối chiếu và lỗi lưu
    private JPanel createReviewTableCard(boolean saveError) {
        JPanel card = InspectionPanel.createCard(168, Color.WHITE);

        card.setLayout(new BorderLayout(0, 8));
        card.add(
                InspectionPanel.createSectionTitle("Kết quả và đề xuất"),
                BorderLayout.NORTH);

        DefaultTableModel model = createResultTableModel();

        if (saveError) {
            saveErrorTableModel = model;
        } else {
            reviewTableModel = model;
        }

        JTable table = InspectionPanel.createTable(model);

        card.add(
                InspectionPanel.createTableScroll(table, 122),
                BorderLayout.CENTER);

        return card;
    }

    // Tạo card mô tả hành động sau khi xác nhận
    private JPanel createAfterConfirmCard() {
        JPanel card = InspectionPanel.createCard(
                62,
                new Color(240, 240, 240));

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.add(InspectionPanel.createSectionTitle("Sau khi xác nhận"));
        card.add(Box.createVerticalStrut(4));
        card.add(InspectionPanel.createValueLabel(
                "Hệ thống sinh mã báo cáo và cung cấp kết quả chính thức cho nhân viên tiếp nhận."));

        return card;
    }

    // Tạo model bảng kết quả
    private DefaultTableModel createResultTableModel() {
        return new DefaultTableModel(
                new Object[] { "Bộ phận", "Kết quả", "Đề xuất" },
                0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    // Tạo một trường nhập kết quả trong form kiểm tra
    private JTextArea addTextAreaField(JPanel parent, String label) {
        parent.add(InspectionPanel.createSmallLabel(label));
        parent.add(Box.createVerticalStrut(4));

        JTextArea area = new JTextArea();

        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Noto Sans", Font.PLAIN, 13));
        area.setForeground(new Color(32, 43, 56));
        area.setBorder(new EmptyBorder(6, 8, 6, 8));

        JScrollPane scroll = new JScrollPane(area);

        scroll.setBorder(BorderFactory.createLineBorder(new Color(184, 194, 207)));
        scroll.setPreferredSize(new Dimension(100, 54));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        scroll.setAlignmentX(LEFT_ALIGNMENT);

        parent.add(scroll);
        parent.add(Box.createVerticalStrut(8));

        return area;
    }

    // Tạo block label và giá trị
    private JPanel createInfoBlock(String title, JLabel value) {
        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        panel.add(InspectionPanel.createSmallLabel(title));
        panel.add(Box.createVerticalStrut(4));
        panel.add(value);

        return panel;
    }

    // Tạo panel rỗng để cân layout
    private JPanel createTransparentPanel() {
        JPanel panel = new JPanel();

        panel.setOpaque(false);

        return panel;
    }

    // Tạo giá trị cảnh báo màu đỏ
    private JLabel createDangerValue(String text) {
        JLabel label = InspectionPanel.createValueLabel(text);

        label.setForeground(new Color(179, 38, 30));

        return label;
    }

    // Nạp và hiển thị lại danh sách xe chờ kiểm tra
    public void showQueuePage() {
        showTechnicalShell.run();
        queueTableModel.setRowCount(0);

        for (InspectionQueueItem item : service.getInspectionQueue()) {
            queueTableModel.addRow(new Object[] {
                    item.getReceptionNumber(),
                    item.getLicensePlate(),
                    item.getCustomerRequest(),
                    item.getStatus()
            });
        }

        if (queueTable.getRowCount() > 0) {
            queueTable.setRowSelectionInterval(0, 0);
        }

        layout.show(pages, "QUEUE");
    }

    // Bắt đầu kiểm tra và chuyển phiếu không hợp lệ sang W_E04
    private void startInspection() {
        int row = queueTable.getSelectedRow();

        if (row < 0) return;

        selectedQueueItem = service.getInspectionQueue().get(row);

        if (!selectedQueueItem.isValid()) {
            invalidReceptionValue.setText(
                    "<html>"
                            + selectedQueueItem.getReceptionNumber()
                            + " • Nháp • Chưa có xác nhận nội dung tiếp nhận của khách."
                            + "</html>");

            showPage("INVALID");

            return;
        }

        NumberFormat format = NumberFormat.getIntegerInstance(Locale.of("vi", "VN"));

        workInfoLabel.setText(
                "<html>"
                        + selectedQueueItem.getReceptionNumber()
                        + " · "
                        + selectedQueueItem.getLicensePlate()
                        + " · "
                        + format.format(selectedQueueItem.getMileage())
                        + " km.</html>");

        requestValueLabel.setText(
                "<html>" + selectedQueueItem.getCustomerRequest() + "</html>");

        conditionValueLabel.setText(
                "<html>" + selectedQueueItem.getInitialCondition() + "</html>");

        brakeArea.setText("Má phanh còn đạt giới hạn; bụi bám, cần vệ sinh.");
        oilArea.setText("Dầu đến kỳ thay, không phát hiện rò rỉ.");
        electricalArea.setText("Hoạt động bình thường.");
        diagnosticArea.setText("Không ghi nhận mã lỗi.");
        proposalArea.setText("Bảo dưỡng định kỳ, thay dầu/lọc dầu, vệ sinh phanh.");

        showPage("WORK");
    }

    // Kiểm tra trường bắt buộc trước khi chuyển sang màn đối chiếu
    private void reviewInspection() {
        if (selectedQueueItem == null) {
            showQueuePage();
            return;
        }

        if (brakeArea.getText().trim().isEmpty()
                || proposalArea.getText().trim().isEmpty()) {
            NumberFormat format = NumberFormat.getIntegerInstance(Locale.of("vi", "VN"));

            requiredInfoLabel.setText(
                    "<html>"
                            + selectedQueueItem.getReceptionNumber()
                            + " · "
                            + selectedQueueItem.getLicensePlate()
                            + " · "
                            + format.format(selectedQueueItem.getMileage())
                            + " km.</html>");

            requiredRequestValue.setText(
                    "<html>" + selectedQueueItem.getCustomerRequest() + "</html>");

            requiredConditionValue.setText(
                    "<html>" + selectedQueueItem.getInitialCondition() + "</html>");

            showPage("REQUIRED");

            return;
        }

        fillReviewData(
                reviewReceptionValue,
                reviewVehicleValue,
                reviewTechnicianValue,
                reviewTableModel);

        reviewCheckBox.setSelected(true);

        showPage("REVIEW");
    }

    // Hoàn tất kiểm tra hoặc chuyển sang W_E06 khi lưu thất bại
    private void completeInspection() {
        if (selectedQueueItem == null || !reviewCheckBox.isSelected()) {
            return;
        }

        InspectionReport completed = service.completeInspection(
                selectedQueueItem,
                brakeArea.getText().trim(),
                oilArea.getText().trim(),
                electricalArea.getText().trim(),
                diagnosticArea.getText().trim(),
                proposalArea.getText().trim());

        if (completed == null) {
            fillReviewData(
                    saveErrorReceptionValue,
                    saveErrorVehicleValue,
                    saveErrorTechnicianValue,
                    saveErrorTableModel);

            showPage("SAVE_ERROR");

            return;
        }

        completedTitleInfo.setText(
                "<html>"
                        + completed.getReportNumber()
                        + " · Kết quả chính thức đã sẵn sàng để lập báo giá."
                        + "</html>");

        completedReceptionValue.setText(completed.getReceptionNumber());
        completedPlateValue.setText(completed.getLicensePlate());
        completedTechnicianValue.setText(completed.getTechnicianName());

        completedTableModel.setRowCount(0);

        completedTableModel.addRow(new Object[] {
                "Phanh trước",
                completed.getBrakeResult(),
                "Vệ sinh phanh"
        });

        completedTableModel.addRow(new Object[] {
                "Dầu động cơ",
                completed.getOilResult(),
                "Bảo dưỡng, thay dầu/lọc"
        });

        completedTableModel.addRow(new Object[] {
                "Điện / điều hòa",
                completed.getElectricalResult(),
                "Không sửa thêm"
        });

        showReceptionShell.run();

        layout.show(pages, "COMPLETED");
    }

    // Nạp dữ liệu vào màn đối chiếu hoặc trạng thái lỗi lưu
    private void fillReviewData(
            JLabel reception,
            JLabel vehicle,
            JLabel technician,
            DefaultTableModel model) {
        reception.setText(selectedQueueItem.getReceptionNumber());

        vehicle.setText(
                selectedQueueItem.getLicensePlate()
                        + " · "
                        + selectedQueueItem.getVehicleName());

        technician.setText("Chung Ngô Minh Hoàng");

        model.setRowCount(0);

        model.addRow(new Object[] {
                "Phanh trước",
                brakeArea.getText().trim(),
                "Vệ sinh, kiểm tra lại"
        });

        model.addRow(new Object[] {
                "Dầu động cơ",
                oilArea.getText().trim(),
                "Thay dầu và lọc dầu"
        });

        model.addRow(new Object[] {
                "Điện / điều hòa",
                electricalArea.getText().trim(),
                "Không sửa thêm"
        });
    }

    // Chuyển giữa các trạng thái của workflow
    private void showPage(String page) {
        showTechnicalShell.run();
        layout.show(pages, page);
    }

    private static class CheckIcon implements Icon {
        private final boolean selected;

        CheckIcon(boolean selected) {
            this.selected = selected;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(selected ? new Color(36, 95, 168) : Color.WHITE);
            g.fillRect(x, y, 14, 14);

            g.setColor(selected ? Color.WHITE : new Color(184, 194, 207));

            if (selected) {
                g.drawString("✓", x + 2, y + 11);
            } else {
                g.drawRect(x, y, 13, 13);
            }
        }

        @Override
        public int getIconWidth() {
            return 14;
        }

        @Override
        public int getIconHeight() {
            return 14;
        }
    }
}