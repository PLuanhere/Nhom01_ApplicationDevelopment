package ui.screen.thungan;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import dao.hoadon.HoaDonDAO;
import entity.hoadon.PhieuChoThu;
import entity.nhansu.TaiKhoan;
import entity.nhansu.VaiTro;
import ui.component.UiChung;

/** Bank transfer / QR payment content; the application shell is supplied by the parent. */
public class BankTransferPaymentPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final Color PAGE = new Color(244, 246, 249);
    private static final Color WHITE = Color.WHITE;
    private static final Color INK = new Color(28, 41, 59);
    private static final Color MUTED = new Color(99, 115, 135);
    private static final Color BORDER = new Color(217, 227, 237);
    private static final Color AMBER = new Color(36, 102, 168); // Xanh như BLUE

    private final HoaDonDAO dao;
    private final PhieuChoThu phieu;
    private final TaiKhoan taiKhoan;
    private final long amountDue;
    private JTextField receivedAmountField;
    private JTextField referenceField;
    private JLabel statusLabel;
    private JButton confirmButton;
    private final ActionListener onConfirm;

    public BankTransferPaymentPanel() {
        this(null, null, null, null, null);
    }

    public BankTransferPaymentPanel(HoaDonDAO dao, PhieuChoThu phieu, TaiKhoan taiKhoan, ActionListener onBack,
            ActionListener onConfirm) {
        this.dao = dao != null ? dao : new HoaDonDAO();
        this.taiKhoan = taiKhoan != null ? taiKhoan : new TaiKhoan("thungan", VaiTro.THU_NGAN);
        if (phieu == null) {
            java.util.List<PhieuChoThu> ds = this.dao.layPhieuChoThu(this.taiKhoan);
            if (ds.isEmpty()) {
                throw new IllegalArgumentException("Không có phiếu chờ thu.");
            }
            this.phieu = ds.get(0);
        } else {
            this.phieu = phieu;
        }
        this.amountDue = this.phieu.getTongTien();
        this.onConfirm = onConfirm;

        setLayout(new BorderLayout());
        setBackground(PAGE);
        setBorder(BorderFactory.createEmptyBorder(20, 26, 18, 26));

        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setOpaque(false);
        add(UiChung.taoVungCuon(content), BorderLayout.CENTER);

        content.add(buildHeading(onBack), BorderLayout.NORTH);
        content.add(buildPaymentCard(), BorderLayout.CENTER);
    }

    private JPanel buildHeading(ActionListener onBack) {
        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);

        JPanel titleBlock = new JPanel(new BorderLayout(0, 8));
        titleBlock.setOpaque(false);
        titleBlock.add(label("Thu ngân  /  Nghiệp vụ  /  Thanh toán chuyển khoản / QR", 11, Font.PLAIN, MUTED),
                BorderLayout.NORTH);

        JPanel titleLines = new JPanel(new BorderLayout(0, 5));
        titleLines.setOpaque(false);
        titleLines.add(label("Thanh toán chuyển khoản / QR", 24, Font.BOLD, INK), BorderLayout.NORTH);
        titleLines.add(label("Chỉ xác nhận sau khi giao dịch đã được đối chiếu.", 12, Font.PLAIN, MUTED),
                BorderLayout.SOUTH);

        titleBlock.add(titleLines, BorderLayout.CENTER);
        heading.add(titleBlock, BorderLayout.CENTER);

        JButton back = new JButton("Danh sách / quay lại");
        back.setFont(new Font("Dialog", Font.BOLD, 12));
        back.setForeground(new Color(49, 68, 91));
        back.setBackground(WHITE);
        back.setFocusPainted(false);
        back.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        back.addActionListener(new ActionListener() {
            /** Thực hiện thao tác vừa chọn trên biểu mẫu. */
            @Override
            public void actionPerformed(ActionEvent e) {
                if (onBack != null)
                    onBack.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "quayLai"));
            }
        });
        heading.add(back, BorderLayout.EAST);

        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));
        return heading;
    }

    private JPanel buildPaymentCard() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);
        top.add(label("Thanh toán chuyển khoản / QR · " + phieu.getMaPhieu(), 18, Font.BOLD, INK), BorderLayout.NORTH);
        top.add(label("Nhập mã giao dịch không đồng nghĩa đã nhận tiền. Phải đối chiếu số tiền thực nhận.", 12,
                Font.PLAIN, MUTED), BorderLayout.CENTER);

        JPanel facts = new JPanel(new GridLayout(1, 3, 12, 0));
        facts.setOpaque(false);
        facts.add(fact("Tổng phải thu", money(amountDue)));
        facts.add(fact("Khách / biển số", phieu.getTenKhach() + " · " + phieu.getBienSo()));
        facts.add(fact("Hóa đơn", "Chưa phát hành"));
        top.add(facts, BorderLayout.SOUTH);

        card.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);

        JPanel bankBox = new JPanel(new BorderLayout(0, 6));
        bankBox.setBackground(new Color(245, 248, 252));
        bankBox.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        bankBox.add(label("TÀI KHOẢN GARAGE / QR THANH TOÁN", 13, Font.BOLD, INK), BorderLayout.NORTH);

        JPanel bankInfo = new JPanel(new GridLayout(2, 1, 0, 4));
        bankInfo.setOpaque(false);
        bankInfo.add(label("Ngân hàng · Số tài khoản · Chủ tài khoản: CẦN CẤU HÌNH THEO THÔNG TIN GARAGE", 13,
                Font.PLAIN, MUTED));
        bankInfo.add(label("Vùng QR sẽ hiển thị sau khi nhóm xác nhận tài khoản nhận tiền.", 12, Font.PLAIN, MUTED));
        bankBox.add(bankInfo, BorderLayout.CENTER);

        center.add(bankBox, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(2, 2, 12, 12));
        fields.setOpaque(false);

        receivedAmountField = new JTextField();
        receivedAmountField.setToolTipText("Nhập số tiền đã thực nhận sau khi đối soát");

        referenceField = new JTextField();
        referenceField.setToolTipText("Nhập mã tham chiếu giao dịch sau khi đối chiếu");

        fields.add(inputField("Số tiền đã nhận qua ngân hàng *", receivedAmountField));
        fields.add(inputField("Mã tham chiếu giao dịch *", referenceField));

        String nguoiThu = taiKhoan != null ? (taiKhoan.getTenDangNhap() + " · Thu ngân") : "thungan · Thu ngân";
        fields.add(readOnlyField("Người đối soát · chỉ đọc", nguoiThu));

        statusLabel = label("Chờ xác minh đã nhận đủ tiền", 14, Font.PLAIN, MUTED);
        fields.add(readOnlyField("Trạng thái thanh toán · chỉ đọc", statusLabel));

        center.add(fields, BorderLayout.CENTER);
        card.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(0, 12));
        bottom.setOpaque(false);

        JLabel note = label("Chỉ xác nhận thanh toán khi giao dịch đã vào tài khoản garage và số tiền hợp lệ.", 13,
                Font.PLAIN, MUTED);
        note.setOpaque(true);
        note.setBackground(new Color(255, 247, 227));
        note.setBorder(BorderFactory.createEmptyBorder(11, 11, 11, 11));
        bottom.add(note, BorderLayout.NORTH);

        JPanel actions = new JPanel(new BorderLayout(12, 0));
        actions.setOpaque(false);
        actions.add(label("Chỉ sau đối soát: phát hành hóa đơn và chuyển Tiếp nhận bàn giao", 12, Font.PLAIN, MUTED),
                BorderLayout.CENTER);

        confirmButton = new JButton("Xác nhận đã nhận đủ tiền");
        confirmButton.setFont(new Font("Dialog", Font.BOLD, 14));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setBackground(AMBER);
        confirmButton.setOpaque(true);
        confirmButton.setContentAreaFilled(true);
        confirmButton.setBorderPainted(false);
        confirmButton.setFocusPainted(false);
        confirmButton.setBorder(BorderFactory.createEmptyBorder(11, 14, 11, 14));
        confirmButton.setEnabled(false);
        confirmButton.addActionListener(new ActionListener() {
            /** Thực hiện thao tác vừa chọn trên biểu mẫu. */
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    long soTienNhan = parseAmount(receivedAmountField.getText());
                    String maGiaoDich = referenceField.getText().trim();

                    String maHoaDon = dao.xacNhanThanhToanChuyenKhoan(taiKhoan, phieu.getMaPhieu(), soTienNhan,
                            maGiaoDich);

                    confirmButton.setEnabled(false);
                    if (onConfirm != null)
                        onConfirm.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, maHoaDon));
                } catch (RuntimeException ex) {
                    javax.swing.JOptionPane.showMessageDialog(BankTransferPaymentPanel.this, ex.getMessage(),
                            "Lỗi xác nhận thanh toán", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        actions.add(confirmButton, BorderLayout.EAST);
        bottom.add(actions, BorderLayout.SOUTH);

        card.add(bottom, BorderLayout.SOUTH);

        DocumentListener listener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                refreshValidation();
            }

            public void removeUpdate(DocumentEvent e) {
                refreshValidation();
            }

            public void changedUpdate(DocumentEvent e) {
                refreshValidation();
            }
        };

        receivedAmountField.getDocument().addDocumentListener(listener);
        referenceField.getDocument().addDocumentListener(listener);

        refreshValidation();
        return card;
    }

    private JPanel fact(String title, String value) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        panel.add(label(title, 12, Font.PLAIN, MUTED), BorderLayout.NORTH);
        panel.add(label(value, 14, Font.BOLD, INK), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel inputField(String title, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        panel.add(label(title, 12, Font.BOLD, MUTED), BorderLayout.NORTH);
        field.setFont(new Font("Dialog", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private JPanel readOnlyField(String title, String value) {
        return readOnlyField(title, label(value, 14, Font.PLAIN, MUTED));
    }

    private JPanel readOnlyField(String title, JLabel value) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        panel.add(label(title, 12, Font.BOLD, MUTED), BorderLayout.NORTH);
        value.setOpaque(true);
        value.setBackground(new Color(245, 248, 252));
        value.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        panel.add(value, BorderLayout.CENTER);
        return panel;
    }

    private void refreshValidation() {
        long received;
        try {
            received = parseAmount(receivedAmountField.getText());
        } catch (IllegalArgumentException ex) {
            confirmButton.setEnabled(false);
            statusLabel.setText(ex.getMessage());
            return;
        }
        boolean hasReference = !referenceField.getText().trim().isEmpty();
        boolean valid = !receivedAmountField.getText().trim().isEmpty() && received >= amountDue && hasReference;

        confirmButton.setEnabled(valid);

        if (valid) {
            statusLabel.setText("Đủ số tiền · chờ xác nhận đối soát");
        } else if (received <= 0) {
            statusLabel.setText("Chờ xác minh đã nhận đủ tiền");
        } else if (received < amountDue) {
            statusLabel.setText("Chưa nhận đủ tiền · còn thiếu " + money(amountDue - received));
        } else if (!hasReference) {
            statusLabel.setText("Đủ số tiền · cần mã tham chiếu");
        } else {
            statusLabel.setText("Đủ số tiền · chờ xác nhận đối soát");
        }
    }

    private static long parseAmount(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        return UiChung.docTien(value, "Số tiền thực nhận");
    }

    private static String money(long amount) {
        return NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN")).format(amount) + "đ";
    }

    private static JLabel label(String text, int size, int style, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Dialog", style, size));
        label.setForeground(color);
        return label;
    }
}
