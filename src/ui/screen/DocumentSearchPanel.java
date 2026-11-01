package ui.screen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.function.BiConsumer;

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

/**
 * Màn hình tra cứu chứng từ thanh toán.
 * Gắn vào vùng CENTER của {@code MainFrame} qua {@code ThuNganShell}.
 *
 * <p>Chỉ chứa GUI; callback tra cứu và điều hướng do shell cung cấp.</p>
 */
public final class DocumentSearchPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Color FIELD_BORDER = UiTheme.BORDER;

    private final JTextField txtTuKhoa;
    private final JComboBox<String> cboPhuongThuc;

    /**
     * Tạo màn hình với các nút giao diện chưa nối nghiệp vụ.
     * Dùng khi cần xem trước độc lập.
     */
    public DocumentSearchPanel() {
        this((tuKhoa, phuongThuc) -> { }, () -> { });
    }

    /**
     * Tạo màn hình và nhận callback cho tìm kiếm và quay về trang chủ.
     *
     * @param khiTraCuu   nhận từ khóa và phương thức thanh toán khi bấm "Tra cứu"
     * @param veTrangChu  hành động khi bấm "Quay lại"
     */
    public DocumentSearchPanel(BiConsumer<String, String> khiTraCuu, Runnable veTrangChu) {
        BiConsumer<String, String> searchAction = khiTraCuu != null ? khiTraCuu : (t, p) -> { };
        Runnable homeAction = veTrangChu != null ? veTrangChu : () -> { };

        setLayout(new BorderLayout());
        setBackground(UiTheme.WINDOW);

        JPanel pnlContent = new JPanel(new BorderLayout(0, 8));
        pnlContent.setOpaque(false);
        pnlContent.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));

        pnlContent.add(createHeading(), BorderLayout.NORTH);

        JPanel pnlForm = createFormCard();
        txtTuKhoa     = createTextField();
        cboPhuongThuc = createPaymentMethodBox();
        pnlForm.add(createFieldGroup("So chung tu, bien so hoac ten khach", txtTuKhoa));
        pnlForm.add(Box.createVerticalStrut(8));
        pnlForm.add(createFieldGroup("Phuong thuc thanh toan", cboPhuongThuc));
        pnlContent.add(pnlForm, BorderLayout.CENTER);

        add(pnlContent,       BorderLayout.CENTER);
        add(createActions(searchAction, homeAction), BorderLayout.SOUTH);
    }

    // ── Tiêu đề ──────────────────────────────────────────────────────────────

    private JPanel createHeading() {
        JPanel pnlHeading = new JPanel();
        pnlHeading.setOpaque(false);
        pnlHeading.setLayout(new BoxLayout(pnlHeading, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("Tra cuu chung tu thanh toan");
        lblTitle.setFont(UiTheme.BRAND_FONT.deriveFont(Font.BOLD, 22f));
        lblTitle.setForeground(UiTheme.TEXT);

        JLabel lblDesc = new JLabel(
                "Nhap tieu chi de tim chung tu da phat hanh; ket qua duoc mo o man rieng.");
        lblDesc.setFont(UiTheme.CAPTION_FONT.deriveFont(12f));
        lblDesc.setForeground(UiTheme.TEXT);

        pnlHeading.add(lblTitle);
        pnlHeading.add(lblDesc);
        return pnlHeading;
    }

    // ── Card form ────────────────────────────────────────────────────────────

    private JPanel createFormCard() {
        JPanel pnlCard = new JPanel();
        pnlCard.setBackground(UiTheme.SURFACE);
        pnlCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER),
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

    /**
     * Tạo nhóm nhãn + trường nhập.
     *
     * @param labelText nhãn hiển thị phía trên trường
     * @param field     trường nhập (JTextField hoặc JComboBox)
     * @return panel nhóm đã căn trái
     */
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

    private JTextField createTextField() {
        JTextField txt = new JTextField("HD-2026-0108");
        txt.setFont(UiTheme.SHELL_FONT);
        txt.setForeground(UiTheme.TEXT);
        txt.setBackground(UiTheme.SURFACE);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        txt.setPreferredSize(new Dimension(320, 26));
        return txt;
    }

    private JComboBox<String> createPaymentMethodBox() {
        JComboBox<String> cmb = new JComboBox<>(
                new String[]{"Tat ca", "Tien mat", "Chuyen khoan"});
        cmb.setFont(UiTheme.SHELL_FONT);
        cmb.setForeground(UiTheme.TEXT);
        cmb.setBackground(UiTheme.SURFACE);
        cmb.setBorder(BorderFactory.createLineBorder(FIELD_BORDER));
        cmb.setPreferredSize(new Dimension(320, 26));
        return cmb;
    }

    // ── Nhóm nút ─────────────────────────────────────────────────────────────

    private JPanel createActions(BiConsumer<String, String> searchAction, Runnable homeAction) {
        JPanel pnlActions = new JPanel(new BorderLayout());
        pnlActions.setOpaque(false);
        pnlActions.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));

        // Trái: quay lại
        JButton btnQuayLai = createButton("Quay lại", false, 110);
        btnQuayLai.addActionListener(e -> homeAction.run());
        pnlActions.add(btnQuayLai, BorderLayout.WEST);

        // Phải: tra cứu
        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlRight.setOpaque(false);
        JButton btnTraCuu = createButton("Tra cuu", true, 90);
        btnTraCuu.addActionListener(e -> searchAction.accept(
                txtTuKhoa.getText().trim(),
                (String) cboPhuongThuc.getSelectedItem()));
        pnlRight.add(btnTraCuu);
        pnlActions.add(pnlRight, BorderLayout.EAST);

        return pnlActions;
    }

    /**
     * Tạo nút đã định dạng.
     *
     * @param text    nhãn nút
     * @param primary true = nền xanh PRIMARY, false = nền trắng
     * @param width   chiều rộng ưu tiên tính bằng pixel
     * @return nút đã định dạng, chưa gắn sự kiện nghiệp vụ
     */
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
     * Trả ô nhập từ khóa để màn hình cha đọc giá trị khi cần.
     *
     * @return ô nhập số chứng từ, biển số hoặc tên khách
     */
    public JTextField getTxtTuKhoa() {
        return txtTuKhoa;
    }

    /**
     * Trả combo phương thức thanh toán để màn hình cha đọc lựa chọn khi cần.
     *
     * @return combo phương thức thanh toán
     */
    public JComboBox<String> getCboPhuongThuc() {
        return cboPhuongThuc;
    }
}
