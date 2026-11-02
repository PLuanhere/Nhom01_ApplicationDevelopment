package ui.screen.thungan;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ui.component.UiTheme;

/** Màn hình tra cứu chứng từ thanh toán. */
public final class DocumentSearchPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Color FIELD_BORDER = UiTheme.BORDER;

    private final JTextField txtTuKhoa;
    private final JComboBox<String> cboPhuongThuc;

    /** Tạo màn hình với các nút giao diện chưa nối nghiệp vụ. */
    public DocumentSearchPanel() {
        this(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
            }
        }, new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {

            }
        });
    }

    /** Tạo màn hình và nhận callback cho tìm kiếm và quay về trang chủ. */
    public DocumentSearchPanel(ActionListener khiTraCuu, ActionListener veTrangChu) {
        ActionListener searchAction = khiTraCuu != null ? khiTraCuu : new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
            }
        };
        ActionListener homeAction = veTrangChu != null ? veTrangChu : new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {

            }
        };

        setLayout(new BorderLayout());
        setBackground(UiTheme.WINDOW);

        JPanel pnlContent = new JPanel(new BorderLayout(0, 8));
        pnlContent.setOpaque(false);
        pnlContent.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));

        pnlContent.add(createHeading(), BorderLayout.NORTH);

        JPanel pnlForm = createFormCard();
        txtTuKhoa = createTextField();
        cboPhuongThuc = createPaymentMethodBox();
        pnlForm.add(createFieldGroup("So chung tu, bien so hoac ten khach", txtTuKhoa));
        pnlForm.add(Box.createVerticalStrut(8));
        pnlForm.add(createFieldGroup("Phuong thuc thanh toan", cboPhuongThuc));
        pnlContent.add(pnlForm, BorderLayout.CENTER);

        add(pnlContent, BorderLayout.CENTER);
        add(createActions(searchAction, homeAction), BorderLayout.SOUTH);
    }

    // ── Tiêu đề ──────────────────────────────────────────────────────────────

    /** Tạo heading cho giao diện. */
    private JPanel createHeading() {
        JPanel pnlHeading = new JPanel();
        pnlHeading.setOpaque(false);
        pnlHeading.setLayout(new BoxLayout(pnlHeading, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("Tra cuu chung tu thanh toan");
        lblTitle.setFont(UiTheme.BRAND_FONT.deriveFont(Font.BOLD, 22f));
        lblTitle.setForeground(UiTheme.TEXT);

        JLabel lblDesc = new JLabel("Nhap tieu chi de tim chung tu da phat hanh; ket qua duoc mo o man rieng.");
        lblDesc.setFont(UiTheme.CAPTION_FONT.deriveFont(12f));
        lblDesc.setForeground(UiTheme.TEXT);

        pnlHeading.add(lblTitle);
        pnlHeading.add(lblDesc);
        return pnlHeading;
    }

    // ── Card form ────────────────────────────────────────────────────────────

    /** Tạo form card cho giao diện. */
    private JPanel createFormCard() {
        JPanel pnlCard = new JPanel();
        pnlCard.setBackground(UiTheme.SURFACE);
        pnlCard.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        pnlCard.setLayout(new BoxLayout(pnlCard, BoxLayout.Y_AXIS));

        JLabel lblCardTitle = new JLabel("Tieu chi tra cuu");
        lblCardTitle.setFont(UiTheme.BRAND_FONT);
        lblCardTitle.setForeground(UiTheme.TEXT);
        lblCardTitle.setAlignmentX(LEFT_ALIGNMENT);
        pnlCard.add(lblCardTitle);
        pnlCard.add(Box.createVerticalStrut(8));
        return pnlCard;
    }

    /** Tạo nhóm nhãn + trường nhập. */
    private JPanel createFieldGroup(String labelText, JComponent field) {
        JPanel pnlGroup = new JPanel();
        pnlGroup.setOpaque(false);
        pnlGroup.setLayout(new BoxLayout(pnlGroup, BoxLayout.Y_AXIS));
        pnlGroup.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UiTheme.CAPTION_FONT);
        lbl.setForeground(UiTheme.TEXT);
        lbl.setAlignmentX(LEFT_ALIGNMENT);

        field.setAlignmentX(LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        pnlGroup.add(lbl);
        pnlGroup.add(Box.createVerticalStrut(4));
        pnlGroup.add(field);
        return pnlGroup;
    }

    // ── Trường nhập ──────────────────────────────────────────────────────────

    /** Tạo text field cho giao diện. */
    private JTextField createTextField() {
        JTextField txt = new JTextField("HD-2026-0108");
        txt.setFont(UiTheme.SHELL_FONT);
        txt.setForeground(UiTheme.TEXT);
        txt.setBackground(UiTheme.SURFACE);
        txt.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(FIELD_BORDER),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        txt.setPreferredSize(new Dimension(320, 26));
        return txt;
    }

    /** Tạo payment method box cho giao diện. */
    private JComboBox<String> createPaymentMethodBox() {
        JComboBox<String> cmb = new JComboBox<>(new String[] { "Tat ca", "Tien mat", "Chuyen khoan" });
        cmb.setFont(UiTheme.SHELL_FONT);
        cmb.setForeground(UiTheme.TEXT);
        cmb.setBackground(UiTheme.SURFACE);
        cmb.setBorder(BorderFactory.createLineBorder(FIELD_BORDER));
        cmb.setPreferredSize(new Dimension(320, 26));
        return cmb;
    }

    // ── Nhóm nút ─────────────────────────────────────────────────────────────

    /** Tạo actions cho giao diện. */
    private JPanel createActions(ActionListener searchAction, ActionListener homeAction) {
        JPanel pnlActions = new JPanel(new BorderLayout());
        pnlActions.setOpaque(false);
        pnlActions.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));

        // Trái: quay lại
        JButton btnQuayLai = createButton("Quay lại", false, 110);
        btnQuayLai.addActionListener(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent e) {
                homeAction.actionPerformed(new ActionEvent(homeAction, ActionEvent.ACTION_PERFORMED, "thaoTac"));
            }
        });
        pnlActions.add(btnQuayLai, BorderLayout.WEST);

        // Phải: tra cứu
        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlRight.setOpaque(false);
        JButton btnTraCuu = createButton("Tra cuu", true, 90);
        btnTraCuu.addActionListener(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent e) {
                searchAction.actionPerformed(new ActionEvent(
                        new String[] { txtTuKhoa.getText().trim(), (String) cboPhuongThuc.getSelectedItem() },
                        ActionEvent.ACTION_PERFORMED, "thaoTac"));
            }
        });
        pnlRight.add(btnTraCuu);
        pnlActions.add(pnlRight, BorderLayout.EAST);

        return pnlActions;
    }

    /** Tạo nút đã định dạng. */
    private JButton createButton(String text, boolean primary, int width) {
        JButton btn = new JButton(text);
        UiTheme.styleButton(btn, primary);
        btn.setFont(UiTheme.CAPTION_FONT);
        btn.setPreferredSize(new Dimension(width, 26));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Accessor ─────────────────────────────────────────────────────────────

    /** Trả ô nhập từ khóa để màn hình cha đọc giá trị khi cần. */
    public JTextField getTxtTuKhoa() {
        return txtTuKhoa;
    }

    /** Trả combo phương thức thanh toán để màn hình cha đọc lựa chọn khi cần. */
    public JComboBox<String> getCboPhuongThuc() {
        return cboPhuongThuc;
    }
}
