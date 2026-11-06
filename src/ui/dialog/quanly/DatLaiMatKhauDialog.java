package ui.dialog.quanly;

import dao.TaiKhoanDAO;
import entity.TaiKhoan;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.KeyStroke;

import ui.component.UiChung;
import ui.component.UiTheme;

/** Hộp thoại xác nhận tài khoản đích và đặt lại mật khẩu. */
public final class DatLaiMatKhauDialog extends JDialog implements ActionListener {

    private static final long serialVersionUID = 1L;
    private final TaiKhoan nguoiDung;
    private final TaiKhoan dich;
    private final String lyDo;
    private final ActionListener khiLuu;
    // Các ô nhập và lựa chọn trên biểu mẫu.
    private final JPasswordField txtMatKhau = new JPasswordField();
    private final JPasswordField txtXacNhan = new JPasswordField();
    // Thông báo lỗi khi kiểm tra hoặc đặt lại mật khẩu.
    private final JLabel lblLoi = UiChung.taoNhan(" ", 12, false, new Color(0xB42318));
    // Các nút thực hiện thao tác của màn hình.
    private final JButton btnLuu = UiChung.taoNut("Đặt lại mật khẩu", true);
    private final JButton btnHuy = UiChung.taoNut("Hủy", false);
    private boolean dangLuu;

    /** Tạo hộp thoại nhập mật khẩu mới và xác nhận tài khoản được thay đổi. */
    public DatLaiMatKhauDialog(Window chu, TaiKhoan nguoiDung, TaiKhoan dich, String lyDo, ActionListener khiLuu) {
        super(chu, "Đặt lại mật khẩu tài khoản", ModalityType.APPLICATION_MODAL);
        this.nguoiDung = nguoiDung;
        this.dich = dich;
        this.lyDo = lyDo;
        this.khiLuu = khiLuu;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        them(pnl, UiChung.taoNhan("Đặt lại mật khẩu tài khoản", 19, true, UiTheme.APP_TEXT));
        them(pnl, UiChung.taoNhan("Chỉ Quản lý được thao tác cho tài khoản đã chọn.", 12, false, UiTheme.APP_MUTED));
        UiChung.dinhDangO(txtMatKhau);
        UiChung.dinhDangO(txtXacNhan);
        String ten = new TaiKhoanDAO().timNhanVienTheoMa(nguoiDung, dich.getMaNhanVien()).getHoTen();
        them(pnl, UiChung.taoTruong("Tài khoản đích", UiChung.taoNhan(dich + " · " + ten, 13, true, UiTheme.APP_TEXT)));
        them(pnl, UiChung.taoNhan("Lý do: " + lyDo, 12, false, UiTheme.APP_MUTED));
        them(pnl, UiChung.taoHang(UiChung.taoTruong("Mật khẩu mới *", txtMatKhau),
                UiChung.taoTruong("Nhập lại mật khẩu *", txtXacNhan)));
        them(pnl, lblLoi);
        them(pnl, UiChung.taoLuuY("Mật khẩu cần ít nhất 8 ký tự, có chữ hoa, số và ký tự đặc biệt. "
                + "Tài khoản đích sẽ phải đăng nhập lại ngay."));
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.setOpaque(false);
        btnLuu.setPreferredSize(new Dimension(190, 42));
        btnHuy.setPreferredSize(new Dimension(100, 42));
        pnlNut.add(btnHuy);
        pnlNut.add(btnLuu);
        them(pnl, pnlNut);
        add(pnl);
        btnHuy.setActionCommand("huy");
        btnHuy.addActionListener(this);
        btnLuu.setActionCommand("luu");
        btnLuu.addActionListener(this);
        getRootPane().setDefaultButton(btnLuu);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "huy");
        getRootPane().getActionMap().put("huy", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            /** Thực hiện thao tác tương ứng với nút, bộ lọc hoặc phím vừa chọn. */
            @Override
            public void actionPerformed(ActionEvent event) {
                if (!dangLuu) {
                    dispose();
                }
            }
        });
        pack();
        setSize(Math.min(700, chu.getWidth() - 40), getPreferredSize().height);
        setMinimumSize(new Dimension(600, getPreferredSize().height));
        setLocationRelativeTo(chu);
    }

    /** Đưa mật khẩu vừa nhập vào hộp thoại để xác nhận lại trước khi lưu. */
    public void napMatKhau(char[] matKhau, char[] xacNhan) {
        txtMatKhau.setText(new String(matKhau));
        txtXacNhan.setText(new String(xacNhan));
    }

    /** Thêm một thành phần UI và khoảng cách vào thẻ nội dung. */
    private static void them(JPanel pnl, JComponent hang) {
        hang.setAlignmentX(LEFT_ALIGNMENT);
        hang.setMaximumSize(new Dimension(Integer.MAX_VALUE, hang.getPreferredSize().height));
        pnl.add(hang);
        pnl.add(Box.createVerticalStrut(12));
    }

    /** Kiểm tra hai ô mật khẩu rồi gọi DAO đặt lại và thông báo màn cha. */
    private void luu() {
        if (dangLuu) {
            return;
        }
        char[] matKhau = txtMatKhau.getPassword();
        char[] xacNhan = txtXacNhan.getPassword();
        try {
            TaiKhoan.kiemTraMatKhauMoi(matKhau);
            if (!new String(matKhau).equals(new String(xacNhan))) {
                txtXacNhan.requestFocusInWindow();
                throw new IllegalArgumentException("Hai lần nhập mật khẩu không khớp.");
            }
            dangLuu = true;
            new TaiKhoanDAO().datLaiMatKhau(nguoiDung, dich, matKhau, lyDo);
            dispose();
            if (nguoiDung.isPhienHopLe()) {
                khiLuu.actionPerformed(new ActionEvent(khiLuu, ActionEvent.ACTION_PERFORMED, "thaoTac"));
            }
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            lblLoi.setForeground(new Color(0xB42318));
            lblLoi.setText(ex.getMessage());
        } finally {
            dangLuu = false;
            for (int i = 0; i < matKhau.length; i++) {
                matKhau[i] = '\0';
            }
            for (int i = 0; i < xacNhan.length; i++) {
                xacNhan[i] = '\0';
            }
        }
    }

    /** Kiểm tra trạng thái và dữ liệu chưa lưu trước khi đóng cửa sổ. */
    @Override
    protected void processWindowEvent(WindowEvent event) {
        if (event.getID() == WindowEvent.WINDOW_CLOSING) {
            if (!dangLuu) {
                dispose();
            }

            return;
        }
        super.processWindowEvent(event);
    }

    /** Đóng cửa sổ và dọn dữ liệu hoặc theo dõi không còn sử dụng. */
    @Override
    public void dispose() {
        txtMatKhau.setText("");
        txtXacNhan.setText("");
        super.dispose();
    }

    /** Thực hiện thao tác tương ứng với nút, bộ lọc hoặc phím vừa chọn. */
    @Override
    public void actionPerformed(ActionEvent event) {
        String lenh = event.getActionCommand();
        if ("huy".equals(lenh)) {
            if (!dangLuu) {
                dispose();
            }
        } else if ("luu".equals(lenh)) {
            luu();
        }
    }
}
