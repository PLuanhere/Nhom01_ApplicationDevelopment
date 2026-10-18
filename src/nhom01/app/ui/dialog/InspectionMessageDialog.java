package nhom01.app.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;

public class InspectionMessageDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    public enum Type {
        DELETE_CONFIRM, DELETE_BLOCKED, DISCARD_CHANGES
    }

    private boolean confirmed;

    public InspectionMessageDialog(Window owner, Type type, String reportNumber, String receptionNumber) {
        super(owner, ModalityType.APPLICATION_MODAL);

        setUndecorated(true);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(createContent(type, reportNumber, receptionNumber));
        setSize(560, type == Type.DELETE_CONFIRM ? 212 : type == Type.DELETE_BLOCKED ? 192 : 172);
        setLocationRelativeTo(owner);
    }

    // Tạo toàn bộ nội dung dialog
    private JPanel createContent(Type type, String reportNumber, String receptionNumber) {
        JPanel root = new JPanel(new BorderLayout());

        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createLineBorder(new Color(184, 194, 207)));
        root.add(createHeader(type), BorderLayout.NORTH);
        root.add(createBody(type, reportNumber, receptionNumber), BorderLayout.CENTER);
        root.add(createActions(type), BorderLayout.SOUTH);

        return root;
    }

    // Tạo tiêu đề và nút đóng dialog
    private JPanel createHeader(Type type) {
        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(10, 12, 6, 8));
        header.setPreferredSize(new Dimension(560, 44));

        String title = switch (type) {
            case DELETE_CONFIRM -> "Xác nhận xóa";
            case DELETE_BLOCKED -> "Không thể xóa dữ liệu";
            case DISCARD_CHANGES -> "Bỏ thay đổi chưa lưu?";
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
        close.addActionListener(e -> dispose());

        header.add(titleLabel, BorderLayout.CENTER);
        header.add(close, BorderLayout.EAST);

        return header;
    }

    // Tạo nội dung thông báo theo từng trường hợp
    private JPanel createBody(Type type, String reportNumber, String receptionNumber) {
        JPanel body = new JPanel();

        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(6, 12, 6, 12));

        if (type == Type.DELETE_CONFIRM) {
            body.add(createText("Xóa " + reportNumber + " — " + receptionNumber + "?", Font.BOLD, 14));
            body.add(Box.createVerticalStrut(8));
            body.add(createText(
                    "Hồ sơ này chưa có dữ liệu liên quan nên có thể xóa sau khi xác nhận. "
                            + "Báo cáo đã liên kết được giữ lại.",
                    Font.PLAIN, 13));
        } else if (type == Type.DELETE_BLOCKED) {
            body.add(createText(
                    "Không thể xóa " + reportNumber + " vì đã có dữ liệu liên quan.",
                    Font.PLAIN, 13));

            body.add(Box.createVerticalStrut(8));
            body.add(createText("Chỉ báo cáo Nháp chưa liên kết mới được xóa.", Font.PLAIN, 13));
        } else {
            body.add(createText("Thông tin đang nhập chưa được lưu.", Font.PLAIN, 13));
            body.add(Box.createVerticalStrut(8));
            body.add(createText(
                    "Nếu bỏ thay đổi, dữ liệu trước khi mở biểu mẫu vẫn được giữ nguyên.",
                    Font.PLAIN, 13));
        }

        return body;
    }

    // Tạo nhóm nút thao tác theo từng loại dialog
    private JPanel createActions(Type type) {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));

        actions.setBackground(new Color(240, 240, 240));
        actions.setPreferredSize(new Dimension(560, 54));

        if (type == Type.DELETE_BLOCKED) {
            JButton close = createButton("Đóng", false, false, 82);

            close.addActionListener(e -> dispose());
            actions.add(close);

            return actions;
        }

        if (type == Type.DISCARD_CHANGES) {
            JButton keep = createButton("Tiếp tục nhập", false, false, 124);
            JButton discard = createButton("Bỏ thay đổi", true, false, 108);

            keep.addActionListener(e -> dispose());
            discard.addActionListener(e -> confirmAndClose());

            actions.add(keep);
            actions.add(discard);

            return actions;
        }

        JButton cancel = createButton("Hủy", false, false, 82);
        JButton delete = createButton("Xóa", false, true, 82);

        cancel.addActionListener(e -> dispose());
        delete.addActionListener(e -> confirmAndClose());

        actions.add(cancel);
        actions.add(delete);

        return actions;
    }

    // Tạo nội dung text nhiều dòng
    private JLabel createText(String text, int style, int size) {
        JLabel label = new JLabel("<html><div style='width:510px'>" + text + "</div></html>");

        label.setFont(new Font("Noto Sans", style, size));
        label.setForeground(new Color(32, 43, 56));
        label.setAlignmentX(LEFT_ALIGNMENT);

        return label;
    }

    // Tạo nút theo kiểu thường, chính hoặc nguy hiểm
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

    // Đánh dấu xác nhận rồi đóng dialog
    private void confirmAndClose() {
        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}