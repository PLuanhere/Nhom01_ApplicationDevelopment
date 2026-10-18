package nhom01.app.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicComboBoxUI;

import nhom01.app.model.InspectionReport;
import nhom01.app.service.InspectionService;

public class InspectionReportDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    public enum Mode {
        CREATE, EDIT, VIEW, RELATED
    }

    private final Mode mode;
    private final InspectionService service;
    private final InspectionReport originalReport;

    private JTextField reportNumberField;
    private JComboBox<String> receptionCombo;
    private JTextField technicianField;
    private JTextField dateField;
    private JComboBox<String> departmentCombo;
    private JTextArea resultArea;
    private JTextArea damageArea;
    private JTextArea proposalArea;

    private JLabel messageLabel;
    private JLabel receptionErrorLabel;
    private JLabel dateErrorLabel;
    private JLabel departmentErrorLabel;
    private JLabel resultErrorLabel;
    private JLabel proposalErrorLabel;
    private JButton saveButton;

    private InspectionReport savedReport;
    private boolean deleteRequested;
    private boolean dirty;
    private boolean loading;

    public InspectionReportDialog(Window owner, Mode mode, InspectionService service, InspectionReport report) {
        super(owner, ModalityType.APPLICATION_MODAL);

        this.mode = mode;
        this.service = service;
        originalReport = report == null ? createDefaultReport() : report.copy();
        loading = true;

        setUndecorated(true);
        setResizable(false);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setContentPane(createContent());
        setSize(760, mode == Mode.CREATE || mode == Mode.EDIT ? 500 : mode == Mode.VIEW ? 376 : 324);
        setLocationRelativeTo(owner);

        loading = false;
        dirty = false;
    }

    // Tạo dữ liệu mẫu ban đầu cho màn thêm báo cáo
    private InspectionReport createDefaultReport() {
        return new InspectionReport(
                service.previewNextReportNumber(),
                "TN-2026-0113 · Đã xác nhận",
                "51K-001.23",
                "Chung Ngô Minh Hoàng",
                LocalDate.of(2026, 10, 5),
                "Điều hòa",
                "Làm lạnh yếu",
                "Lọc gió bẩn",
                "Vệ sinh và kiểm tra lại",
                "Nháp");
    }

    // Tạo toàn bộ nội dung dialog
    private JPanel createContent() {
        JPanel root = new JPanel(new BorderLayout());

        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createLineBorder(new Color(184, 194, 207)));
        root.add(createHeader(), BorderLayout.NORTH);

        if (mode == Mode.CREATE || mode == Mode.EDIT) {
            root.add(createEditableForm(), BorderLayout.CENTER);
        } else {
            root.add(createReadOnlyForm(), BorderLayout.CENTER);
        }

        root.add(createActions(), BorderLayout.SOUTH);

        return root;
    }

    // Tạo phần tiêu đề dialog
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(10, 12, 6, 8));
        header.setPreferredSize(new Dimension(760, 46));

        String title = switch (mode) {
            case CREATE -> "Thêm báo cáo kiểm tra";
            case EDIT -> "Sửa báo cáo kiểm tra";
            case VIEW -> "Chi tiết báo cáo kiểm tra";
            case RELATED -> "Chi tiết " + originalReport.getReportNumber();
        };

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Noto Sans", Font.BOLD, 18));
        titleLabel.setForeground(new Color(32, 43, 56));

        JButton close = new JButton("×");

        close.setUI(new BasicButtonUI());
        close.setFont(new Font("Noto Sans", Font.PLAIN, 16));
        close.setForeground(new Color(90, 104, 120));
        close.setBackground(Color.WHITE);
        close.setBorder(null);
        close.setFocusPainted(false);
        close.setPreferredSize(new Dimension(30, 30));
        close.addActionListener(e -> closeDialog());

        header.add(titleLabel, BorderLayout.CENTER);
        header.add(close, BorderLayout.EAST);

        return header;
    }

    // Tạo form nhập liệu cho trạng thái thêm và sửa
    private JPanel createEditableForm() {
        JPanel wrapper = new JPanel();

        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(new EmptyBorder(0, 16, 0, 16));

        messageLabel = new JLabel(
                mode == Mode.EDIT
                        ? "Chỉnh sửa thông tin · Có thay đổi chưa lưu"
                        : "Nhập hồ sơ mới · Các trường * là bắt buộc");

        messageLabel.setFont(new Font("Noto Sans", Font.PLAIN, 12));
        messageLabel.setForeground(new Color(90, 104, 120));
        messageLabel.setAlignmentX(LEFT_ALIGNMENT);

        wrapper.add(messageLabel);
        wrapper.add(Box.createVerticalStrut(8));

        JPanel grid = new JPanel(new GridLayout(4, 2, 12, 8));

        grid.setBackground(Color.WHITE);
        grid.setAlignmentX(LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 332));

        reportNumberField = createTextField();
        reportNumberField.setEditable(false);

        receptionCombo = createReceptionCombo();

        technicianField = createTextField();
        technicianField.setEditable(false);

        dateField = createTextField();

        departmentCombo = new JComboBox<>(
                new String[] { "Điều hòa", "Phanh trước", "Khoang máy / dầu", "Điện" });

        styleComboBox(departmentCombo);

        resultArea = createTextArea();
        damageArea = createTextArea();
        proposalArea = createTextArea();

        receptionErrorLabel = createErrorLabel();
        dateErrorLabel = createErrorLabel();
        departmentErrorLabel = createErrorLabel();
        resultErrorLabel = createErrorLabel();
        proposalErrorLabel = createErrorLabel();

        grid.add(createFieldBlock("Số báo cáo", reportNumberField, null));
        grid.add(createFieldBlock("Phiếu tiếp nhận *", receptionCombo, receptionErrorLabel));
        grid.add(createFieldBlock("Kỹ thuật viên", technicianField, null));
        grid.add(createFieldBlock("Ngày kiểm tra *", dateField, dateErrorLabel));
        grid.add(createFieldBlock("Bộ phận *", departmentCombo, departmentErrorLabel));
        grid.add(createFieldBlock("Kết quả kiểm tra *", createAreaScroll(resultArea), resultErrorLabel));
        grid.add(createFieldBlock("Hư hỏng phát hiện", createAreaScroll(damageArea), null));
        grid.add(createFieldBlock("Đề xuất sửa chữa *", createAreaScroll(proposalArea), proposalErrorLabel));

        wrapper.add(grid);
        wrapper.add(Box.createVerticalStrut(8));

        JLabel note = new JLabel(
                "<html>Kỹ thuật chỉ sửa nháp thuộc phân công. "
                        + "Chỉ báo cáo hoàn tất hợp lệ mới làm căn cứ báo giá; "
                        + "báo cáo đã liên kết được giữ lại.</html>");

        note.setFont(new Font("Noto Sans", Font.PLAIN, 12));
        note.setForeground(new Color(90, 104, 120));
        note.setAlignmentX(LEFT_ALIGNMENT);

        wrapper.add(note);

        loadEditableData();
        installChangeListeners();

        return wrapper;
    }

    // Tạo form chỉ đọc cho trạng thái xem và dữ liệu liên quan
    private JPanel createReadOnlyForm() {
        JPanel wrapper = new JPanel();

        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(new EmptyBorder(0, 16, 0, 16));

        JLabel subtitle = new JLabel("Thông tin hồ sơ · Chỉ đọc");

        subtitle.setFont(new Font("Noto Sans", Font.PLAIN, 12));
        subtitle.setForeground(new Color(90, 104, 120));
        subtitle.setAlignmentX(LEFT_ALIGNMENT);

        wrapper.add(subtitle);
        wrapper.add(Box.createVerticalStrut(10));

        JPanel grid = new JPanel(new GridLayout(mode == Mode.RELATED ? 3 : 4, 2, 12, 8));

        grid.setBackground(Color.WHITE);
        grid.setAlignmentX(LEFT_ALIGNMENT);

        if (mode == Mode.RELATED) {
            grid.add(createReadOnlyBlock("Số báo cáo", originalReport.getReportNumber()));
            grid.add(createReadOnlyBlock("Phiếu tiếp nhận", originalReport.getReceptionNumber()));
            grid.add(createReadOnlyBlock("Biển số", originalReport.getLicensePlate()));
            grid.add(createReadOnlyBlock("Kỹ thuật viên", originalReport.getTechnicianName()));
            grid.add(createReadOnlyBlock("Trạng thái", originalReport.getStatus()));
            grid.add(createTransparentPanel());
        } else {
            grid.add(createReadOnlyBlock("Số báo cáo", originalReport.getReportNumber()));
            grid.add(createReadOnlyBlock(
                    "Phiếu tiếp nhận *",
                    originalReport.getReceptionNumber() + " · Đã xác nhận"));

            grid.add(createReadOnlyBlock("Kỹ thuật viên", originalReport.getTechnicianName()));
            grid.add(createReadOnlyBlock("Ngày kiểm tra *", formatDate(originalReport.getInspectionDate())));
            grid.add(createReadOnlyBlock("Bộ phận *", originalReport.getDepartment()));
            grid.add(createReadOnlyBlock("Kết quả kiểm tra *", originalReport.getInspectionResult()));
            grid.add(createReadOnlyBlock("Hư hỏng phát hiện", originalReport.getDetectedDamage()));
            grid.add(createReadOnlyBlock("Đề xuất sửa chữa *", originalReport.getRepairProposal()));
        }

        wrapper.add(grid);
        wrapper.add(Box.createVerticalStrut(10));

        JLabel note = new JLabel(
                "<html>Kỹ thuật chỉ sửa nháp thuộc phân công. "
                        + "Chỉ báo cáo hoàn tất hợp lệ mới làm căn cứ báo giá; "
                        + "báo cáo đã liên kết được giữ lại.</html>");

        note.setFont(new Font("Noto Sans", Font.PLAIN, 12));
        note.setForeground(new Color(90, 104, 120));
        note.setAlignmentX(LEFT_ALIGNMENT);

        wrapper.add(note);

        return wrapper;
    }

    // Tạo nhóm nút phía cuối dialog
    private JPanel createActions() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));

        actions.setBackground(new Color(240, 240, 240));
        actions.setPreferredSize(new Dimension(760, 54));

        if (mode == Mode.VIEW) {
            JButton close = createButton("Đóng", false, false, 82);

            close.addActionListener(e -> dispose());
            actions.add(close);

            return actions;
        }

        if (mode == Mode.RELATED) {
            JButton close = createButton("Đóng", false, false, 82);
            JButton delete = createButton("Xóa", false, true, 82);

            close.addActionListener(e -> dispose());
            delete.addActionListener(e -> requestDelete());

            actions.add(close);
            actions.add(delete);

            return actions;
        }

        JButton cancel = createButton("Hủy", false, false, 82);

        saveButton = createButton("Lưu", true, false, 82);

        cancel.addActionListener(e -> closeDialog());
        saveButton.addActionListener(e -> saveReport());

        actions.add(cancel);
        actions.add(saveButton);

        return actions;
    }

    // Tạo một block gồm label, control và dòng lỗi
    private JPanel createFieldBlock(String title, Component component, JLabel errorLabel) {
        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);

        titleLabel.setFont(new Font("Noto Sans", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(32, 43, 56));
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(4));

        if (component instanceof javax.swing.JComponent swingComponent) {
            swingComponent.setAlignmentX(LEFT_ALIGNMENT);
        }

        panel.add(component);

        if (errorLabel != null) {
            panel.add(Box.createVerticalStrut(2));
            panel.add(errorLabel);
        }

        return panel;
    }

    // Tạo block dữ liệu chỉ đọc
    private JPanel createReadOnlyBlock(String title, String value) {
        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);

        titleLabel.setFont(new Font("Noto Sans", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(32, 43, 56));

        JLabel valueLabel = new JLabel("<html>" + safe(value) + "</html>");

        valueLabel.setFont(new Font("Noto Sans", Font.PLAIN, 13));
        valueLabel.setForeground(new Color(32, 43, 56));

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(valueLabel);

        return panel;
    }

    // Tạo JTextField theo style của form
    private JTextField createTextField() {
        JTextField field = new JTextField();

        field.setFont(new Font("Noto Sans", Font.PLAIN, 13));
        field.setForeground(new Color(32, 43, 56));
        field.setBackground(Color.WHITE);
        field.setBorder(normalBorder());
        field.setPreferredSize(new Dimension(340, 36));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        return field;
    }

    // Tạo combobox phiếu tiếp nhận
    private JComboBox<String> createReceptionCombo() {
        JComboBox<String> combo = new JComboBox<>(
                new String[] {
                        "TN-2026-0113 · Đã xác nhận",
                        "TN-2026-0111 · Đã xác nhận",
                        "TN-2026-0108 · Đã xác nhận"
                });

        combo.setEditable(true);
        styleComboBox(combo);

        return combo;
    }

    // Áp dụng style cho combobox
    private void styleComboBox(JComboBox<String> combo) {
        combo.setUI(new BasicComboBoxUI());
        combo.setFont(new Font("Noto Sans", Font.PLAIN, 13));
        combo.setForeground(new Color(32, 43, 56));
        combo.setBackground(Color.WHITE);
        combo.setBorder(normalBorder());
        combo.setPreferredSize(new Dimension(340, 36));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
    }

    // Tạo JTextArea cho các trường mô tả
    private JTextArea createTextArea() {
        JTextArea area = new JTextArea();

        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Noto Sans", Font.PLAIN, 13));
        area.setForeground(new Color(32, 43, 56));
        area.setBackground(Color.WHITE);
        area.setBorder(new EmptyBorder(6, 8, 6, 8));

        return area;
    }

    // Bọc JTextArea trong JScrollPane
    private JScrollPane createAreaScroll(JTextArea area) {
        JScrollPane scroll = new JScrollPane(area);

        scroll.setBorder(normalBorder());
        scroll.setPreferredSize(new Dimension(340, 74));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        return scroll;
    }

    // Tạo dòng thông báo lỗi dưới field
    private JLabel createErrorLabel() {
        JLabel label = new JLabel(" ");

        label.setFont(new Font("Noto Sans", Font.PLAIN, 11));
        label.setForeground(new Color(179, 38, 30));

        return label;
    }

    // Tạo JButton theo style của dialog
    private JButton createButton(String text, boolean primary, boolean danger, int width) {
        JButton button = new JButton(text);

        button.setUI(new BasicButtonUI());
        button.setFont(new Font("Noto Sans", Font.PLAIN, 13));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(width, 30));
        button.setBackground(primary ? new Color(36, 95, 168) : Color.WHITE);

        button.setForeground(
                primary ? Color.WHITE
                        : danger ? new Color(179, 38, 30) : new Color(32, 43, 56));

        button.setBorder(BorderFactory.createLineBorder(
                primary ? new Color(36, 95, 168)
                        : danger ? new Color(179, 38, 30) : new Color(184, 194, 207)));

        return button;
    }

    // Nạp dữ liệu hiện tại vào form
    private void loadEditableData() {
        reportNumberField.setText(originalReport.getReportNumber());

        receptionCombo.setSelectedItem(
                mode == Mode.CREATE
                        ? originalReport.getReceptionNumber()
                        : originalReport.getReceptionNumber() + " · Đã xác nhận");

        technicianField.setText(originalReport.getTechnicianName());
        dateField.setText(formatDate(originalReport.getInspectionDate()));
        departmentCombo.setSelectedItem(originalReport.getDepartment());
        resultArea.setText(originalReport.getInspectionResult());
        damageArea.setText(originalReport.getDetectedDamage());
        proposalArea.setText(originalReport.getRepairProposal());
    }

    // Theo dõi dữ liệu thay đổi để xử lý bỏ thay đổi chưa lưu
    private void installChangeListeners() {
        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                handleChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                handleChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                handleChange();
            }
        };

        dateField.getDocument().addDocumentListener(listener);
        resultArea.getDocument().addDocumentListener(listener);
        damageArea.getDocument().addDocumentListener(listener);
        proposalArea.getDocument().addDocumentListener(listener);

        Component editor = receptionCombo.getEditor().getEditorComponent();

        if (editor instanceof JTextField field) {
            field.getDocument().addDocumentListener(listener);
        }

        receptionCombo.addActionListener(e -> handleChange());
        departmentCombo.addActionListener(e -> handleChange());
    }

    // Ghi nhận form đã thay đổi và xóa trạng thái lỗi cũ
    private void handleChange() {
        if (loading) return;

        dirty = true;

        clearValidation();
        setSaveEnabled(true);
    }

    // Kiểm tra dữ liệu rồi trả báo cáo về panel cha
    private void saveReport() {
        clearValidation();

        InspectionReport report = new InspectionReport(
                mode == Mode.CREATE ? service.previewNextReportNumber() : originalReport.getReportNumber(),
                getReceptionText(),
                originalReport.getLicensePlate(),
                technicianField.getText().trim(),
                parseDate(dateField.getText().trim()),
                String.valueOf(departmentCombo.getSelectedItem()),
                resultArea.getText().trim(),
                damageArea.getText().trim(),
                proposalArea.getText().trim(),
                originalReport.getStatus());

        String error = service.validateReport(report);

        if (error != null) {
            showValidation(error);
            return;
        }

        savedReport = report;
        dirty = false;

        dispose();
    }

    // Hiển thị lỗi tại đúng field không hợp lệ
    private void showValidation(String field) {
        messageLabel.setText("Chưa lưu: cần nhập đủ trường bắt buộc và dữ liệu hợp lệ.");
        messageLabel.setForeground(new Color(179, 38, 30));

        setSize(760, 520);

        switch (field) {
            case "RECEPTION" -> {
                receptionCombo.setBorder(errorBorder());
                receptionErrorLabel.setText("Thông tin bắt buộc / không hợp lệ");
            }
            case "DATE" -> {
                dateField.setBorder(errorBorder());
                dateErrorLabel.setText("Ngày kiểm tra không hợp lệ");
            }
            case "DEPARTMENT" -> {
                departmentCombo.setBorder(errorBorder());
                departmentErrorLabel.setText("Thông tin bắt buộc / không hợp lệ");
            }
            case "RESULT" -> {
                setAreaBorder(resultArea, errorBorder());
                resultErrorLabel.setText("Thông tin bắt buộc / không hợp lệ");
            }
            case "PROPOSAL" -> {
                setAreaBorder(proposalArea, errorBorder());
                proposalErrorLabel.setText("Thông tin bắt buộc / không hợp lệ");
            }
            default -> {
            }
        }

        setSaveEnabled(false);
    }

    // Xóa trạng thái validation cũ
    private void clearValidation() {
        if (messageLabel == null) return;

        messageLabel.setText(
                mode == Mode.EDIT
                        ? "Chỉnh sửa thông tin · Có thay đổi chưa lưu"
                        : "Nhập hồ sơ mới · Các trường * là bắt buộc");

        messageLabel.setForeground(new Color(90, 104, 120));

        receptionCombo.setBorder(normalBorder());
        dateField.setBorder(normalBorder());
        departmentCombo.setBorder(normalBorder());

        setAreaBorder(resultArea, normalBorder());
        setAreaBorder(proposalArea, normalBorder());

        receptionErrorLabel.setText(" ");
        dateErrorLabel.setText(" ");
        departmentErrorLabel.setText(" ");
        resultErrorLabel.setText(" ");
        proposalErrorLabel.setText(" ");
    }

    // Đổi viền của JScrollPane chứa JTextArea
    private void setAreaBorder(JTextArea area, javax.swing.border.Border border) {
        Component viewport = area.getParent();
        Component scroll = viewport == null ? null : viewport.getParent();

        if (scroll instanceof JScrollPane pane) {
            pane.setBorder(border);
        }
    }

    // Bật hoặc khóa nút Lưu
    private void setSaveEnabled(boolean enabled) {
        if (saveButton == null) return;

        saveButton.setEnabled(enabled);
        saveButton.setBackground(enabled ? new Color(36, 95, 168) : new Color(220, 225, 231));
        saveButton.setForeground(enabled ? Color.WHITE : new Color(90, 104, 120));

        saveButton.setBorder(BorderFactory.createLineBorder(
                enabled ? new Color(36, 95, 168) : new Color(184, 194, 207)));
    }

    // Hỏi xác nhận trước khi đóng form đang có thay đổi chưa lưu
    private void closeDialog() {
        if ((mode == Mode.CREATE || mode == Mode.EDIT) && dirty) {
            InspectionMessageDialog dialog = new InspectionMessageDialog(
                    this,
                    InspectionMessageDialog.Type.DISCARD_CHANGES,
                    originalReport.getReportNumber(),
                    originalReport.getReceptionNumber());

            dialog.setVisible(true);

            if (!dialog.isConfirmed()) {
                return;
            }
        }

        dispose();
    }

    // Ghi nhận yêu cầu xóa từ màn dữ liệu liên quan
    private void requestDelete() {
        deleteRequested = true;
        dispose();
    }

    // Lấy giá trị đang nhập trong combobox phiếu tiếp nhận
    private String getReceptionText() {
        Object value = receptionCombo.getEditor().getItem();

        return value == null ? "" : value.toString().trim();
    }

    // Chuyển chuỗi ngày dd/MM/yyyy thành LocalDate
    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return null;
        }
    }

    // Định dạng LocalDate thành dd/MM/yyyy
    private String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    // Tạo viền nhập liệu bình thường
    private javax.swing.border.Border normalBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(184, 194, 207)),
                new EmptyBorder(4, 8, 4, 8));
    }

    // Tạo viền đỏ cho field đang lỗi
    private javax.swing.border.Border errorBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(179, 38, 30)),
                new EmptyBorder(4, 8, 4, 8));
    }

    // Tạo panel rỗng để cân layout
    private JPanel createTransparentPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);

        return panel;
    }

    // Thay giá trị rỗng bằng dấu gạch ngang
    private String safe(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    public InspectionReport getSavedReport() {
        return savedReport == null ? null : savedReport.copy();
    }

    public boolean isDeleteRequested() {
        return deleteRequested;
    }
}