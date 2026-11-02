package ui.dialog.thungan;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import entity.hoadon.HinhThucThanhToan;

/**
 * Hộp thoại chọn hình thức thanh toán trước khi lập hóa đơn.
 *
 * <p>Hiển thị hai lựa chọn: Tiền mặt và Chuyển khoản / QR.
 * Trả {@link HinhThucThanhToan} được chọn qua {@link #getKetQua()},
 * hoặc {@code null} nếu người dùng đóng mà không xác nhận.</p>
 */
public final class ChonHinhThucThanhToanDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final Color INK = new Color(28, 41, 59);
    private static final Color MUTED = new Color(99, 115, 135);
    private static final Color BORDER = new Color(217, 227, 237);
    private static final Color BLUE = new Color(34, 100, 166);
    private static final Color PAGE = new Color(244, 246, 249);

    /** Kết quả lựa chọn; null nếu hủy. */
    private HinhThucThanhToan ketQua = null;

    /**
     * Tạo hộp thoại chọn hình thức thanh toán.
     *
     * @param owner cửa sổ cha để dialog hiển thị giữa
     */
    public ChonHinhThucThanhToanDialog(Window owner) {
        super(owner, "Chọn hình thức thanh toán", ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(24, 24, 20, 24)));

        // ── Tiêu đề ───────────────────────────────────────────────────
        JPanel pnlTop = new JPanel(new BorderLayout(0, 6));
        pnlTop.setOpaque(false);
        pnlTop.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JLabel lblTitle = new JLabel("Chọn hình thức thanh toán");
        lblTitle.setFont(new Font("Dialog", Font.BOLD, 17));
        lblTitle.setForeground(INK);
        pnlTop.add(lblTitle, BorderLayout.NORTH);

        JLabel lblSub = new JLabel("Chọn một hình thức trước khi lập hóa đơn.");
        lblSub.setFont(new Font("Dialog", Font.PLAIN, 12));
        lblSub.setForeground(MUTED);
        pnlTop.add(lblSub, BorderLayout.CENTER);
        root.add(pnlTop, BorderLayout.NORTH);

        // ── Lựa chọn ───────────────────────────────────────────────────
        JPanel pnlOptions = new JPanel(new GridLayout(2, 1, 0, 10));
        pnlOptions.setOpaque(false);
        pnlOptions.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        ButtonGroup grp = new ButtonGroup();
        JRadioButton radTienMat = taoLuaChon("Tiền mặt", "Khách trả tiền mặt tại quầy, thu ngân kiểm và xác nhận.", grp,
                true);
        JRadioButton radChuyenKhoan = taoLuaChon("Chuyển khoản / QR",
                "Khách chuyển khoản, thu ngân đối chiếu trước khi xác nhận.", grp, false);

        pnlOptions.add(wrapOption(radTienMat));
        pnlOptions.add(wrapOption(radChuyenKhoan));
        root.add(pnlOptions, BorderLayout.CENTER);

        // ── Nút ───────────────────────────────────────────────────
        JPanel pnlButtons = new JPanel(new BorderLayout(12, 0));
        pnlButtons.setOpaque(false);
        pnlButtons.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                BorderFactory.createEmptyBorder(16, 0, 0, 0)));

        JButton btnHuy = new JButton("Hủy");
        btnHuy.setFont(new Font("Dialog", Font.PLAIN, 13));
        btnHuy.setForeground(INK);
        btnHuy.setBackground(Color.WHITE);
        btnHuy.setOpaque(true);
        btnHuy.setContentAreaFilled(true);
        btnHuy.setBorderPainted(true);
        btnHuy.setFocusPainted(false);
        btnHuy.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(9, 18, 9, 18)));
        btnHuy.addActionListener(new ActionListener() {
            /** Thực hiện thao tác vừa chọn trên biểu mẫu. */
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        pnlButtons.add(btnHuy, BorderLayout.WEST);

        JButton btnXacNhan = new JButton("Xác nhận");
        btnXacNhan.setFont(new Font("Dialog", Font.BOLD, 13));
        btnXacNhan.setForeground(Color.WHITE);
        btnXacNhan.setBackground(BLUE);
        btnXacNhan.setOpaque(true);
        btnXacNhan.setContentAreaFilled(true);
        btnXacNhan.setBorderPainted(false);
        btnXacNhan.setFocusPainted(false);
        btnXacNhan.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        btnXacNhan.addActionListener(new ActionListener() {
            /** Thực hiện thao tác vừa chọn trên biểu mẫu. */
            @Override
            public void actionPerformed(ActionEvent e) {
                ketQua = radTienMat.isSelected() ? HinhThucThanhToan.TIEN_MAT : HinhThucThanhToan.CHUYEN_KHOAN;
                dispose();
            }
        });
        pnlButtons.add(btnXacNhan, BorderLayout.EAST);
        root.add(pnlButtons, BorderLayout.SOUTH);

        // ESC để hủy
        getRootPane().registerKeyboardAction(new ActionListener() {
            /** Đóng hộp chọn bằng phím Escape. */
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        }, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);

        setContentPane(root);
        pack();
        setMinimumSize(new Dimension(420, getPreferredSize().height));
        setLocationRelativeTo(owner);
    }

    /**
     * Trả hình thức được chọn sau khi dialog đóng.
     *
     * @return {@link HinhThucThanhToan} hoặc {@code null} nếu người dùng hủy
     */
    public HinhThucThanhToan getKetQua() {
        return ketQua;
    }

    // ── Tiện ích ───────────────────────────────────────────────────

    private JRadioButton taoLuaChon(String ten, String moTa, ButtonGroup grp, boolean macDinh) {
        JRadioButton rad = new JRadioButton(ten);
        rad.setFont(new Font("Dialog", Font.BOLD, 13));
        rad.setForeground(INK);
        rad.setOpaque(false);
        rad.setSelected(macDinh);
        rad.putClientProperty("moTa", moTa);
        grp.add(rad);
        return rad;
    }

    private JPanel wrapOption(JRadioButton rad) {
        JPanel pnl = new JPanel(new BorderLayout(0, 3));
        pnl.setBackground(PAGE);
        pnl.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        pnl.add(rad, BorderLayout.NORTH);
        JLabel lblMoTa = new JLabel((String) rad.getClientProperty("moTa"));
        lblMoTa.setFont(new Font("Dialog", Font.PLAIN, 11));
        lblMoTa.setForeground(MUTED);
        lblMoTa.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        pnl.add(lblMoTa, BorderLayout.CENTER);
        return pnl;
    }
}
