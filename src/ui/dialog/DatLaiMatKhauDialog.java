package ui.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;

import dao.NhanVienDAO;
import dao.TaiKhoanDAO;
import entity.TaiKhoan;
import ui.component.QuanLyUi;
import ui.component.UiTheme;

/** Xác nhận và đặt lại mật khẩu theo dialog Page 3; không đọc mật khẩu hiện có. */
public final class DatLaiMatKhauDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(DatLaiMatKhauDialog.class.getName());
    private final TaiKhoan nguoiDung;
    private final TaiKhoan dich;
    private final String lyDo;
    private final Runnable khiLuu;
    private final JPasswordField txtMatKhau = new JPasswordField();
    private final JPasswordField txtXacNhan = new JPasswordField();
    private final JLabel lblLoi = QuanLyUi.taoNhan(" ", 12, false, new Color(0xB42318));
    private final JButton btnLuu = QuanLyUi.taoNut("Đặt lại mật khẩu", true);
    private final JButton btnHuy = QuanLyUi.taoNut("Hủy", false);
    private boolean dangLuu;

    /**
     * @param chu cửa sổ sở hữu
     * @param nguoiDung Quản lý thực hiện
     * @param dich tài khoản được chọn
     * @param lyDo lý do đã nhập tại màn bảo mật
     * @param khiLuu tải lại tài khoản sau khi lưu thành công
     */
    public DatLaiMatKhauDialog(Window chu, TaiKhoan nguoiDung, TaiKhoan dich, String lyDo, Runnable khiLuu) {
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
        them(pnl, QuanLyUi.taoNhan("Đặt lại mật khẩu tài khoản", 19, true, UiTheme.APP_TEXT));
        them(pnl, QuanLyUi.taoNhan("Chỉ Quản lý được thao tác cho tài khoản đã chọn.",
                12, false, UiTheme.APP_MUTED));
        QuanLyUi.dinhDangO(txtMatKhau);
        QuanLyUi.dinhDangO(txtXacNhan);
        String ten = new NhanVienDAO().timTheoMa(nguoiDung, dich.getMaNhanVien()).getHoTen();
        them(pnl, QuanLyUi.taoTruong("Tài khoản đích", QuanLyUi.taoNhan(
                dich + " · " + ten, 13, true, UiTheme.APP_TEXT)));
        them(pnl, QuanLyUi.taoNhan("Lý do: " + lyDo, 12, false, UiTheme.APP_MUTED));
        them(pnl, QuanLyUi.taoHang(QuanLyUi.taoTruong("Mật khẩu mới *", txtMatKhau),
                QuanLyUi.taoTruong("Nhập lại mật khẩu *", txtXacNhan)));
        them(pnl, lblLoi);
        them(pnl, QuanLyUi.taoLuuY("Mật khẩu cần ít nhất 8 ký tự, có chữ hoa, số và ký tự đặc biệt. "
                + "Tài khoản đích sẽ phải đăng nhập lại ngay."));
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.setOpaque(false);
        btnLuu.setPreferredSize(new Dimension(190, 42));
        btnHuy.setPreferredSize(new Dimension(100, 42));
        pnlNut.add(btnHuy);
        pnlNut.add(btnLuu);
        them(pnl, pnlNut);
        add(pnl);
        btnHuy.addActionListener(event -> { if (!dangLuu) { dispose(); } });
        btnLuu.addActionListener(event -> luu());
        getRootPane().setDefaultButton(btnLuu);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "huy");
        getRootPane().getActionMap().put("huy", new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override public void actionPerformed(ActionEvent event) { if (!dangLuu) { dispose(); } }
        });
        pack();
        setSize(Math.min(700, chu.getWidth() - 40), getPreferredSize().height);
        setMinimumSize(new Dimension(600, getPreferredSize().height));
        setLocationRelativeTo(chu);
    }

    /** Nhận mật khẩu vừa nhập trên màn bảo mật, chỉ để xem lại trước khi xác nhận. */
    public void napMatKhau(char[] matKhau, char[] xacNhan) {
        txtMatKhau.setText(new String(matKhau));
        txtXacNhan.setText(new String(xacNhan));
    }

    private static void them(JPanel pnl, javax.swing.JComponent hang) {
        hang.setAlignmentX(LEFT_ALIGNMENT);
        hang.setMaximumSize(new Dimension(Integer.MAX_VALUE, hang.getPreferredSize().height));
        pnl.add(hang);
        pnl.add(Box.createVerticalStrut(12));
    }

    private void luu() {
        if (dangLuu) { return; }
        char[] matKhau = txtMatKhau.getPassword();
        char[] xacNhan = txtXacNhan.getPassword();
        try {
            TaiKhoan.kiemTraMatKhauMoi(matKhau);
            if (!Arrays.equals(matKhau, xacNhan)) {
                txtXacNhan.requestFocusInWindow();
                throw new IllegalArgumentException("Hai lần nhập mật khẩu không khớp.");
            }
        } catch (IllegalArgumentException ex) {
            Arrays.fill(matKhau, '\0');
            lblLoi.setForeground(new Color(0xB42318));
            lblLoi.setText(ex.getMessage());
            return;
        } finally { Arrays.fill(xacNhan, '\0'); }
        dangLuu = true;
        btnLuu.setEnabled(false);
        btnHuy.setEnabled(false);
        txtMatKhau.setEnabled(false);
        txtXacNhan.setEnabled(false);
        lblLoi.setText("Đang đặt lại mật khẩu...");
        lblLoi.setForeground(UiTheme.APP_MUTED);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                try { new TaiKhoanDAO().datLaiMatKhau(nguoiDung, dich, matKhau, lyDo); }
                finally { Arrays.fill(matKhau, '\0'); }
                return null;
            }
            @Override protected void done() {
                dangLuu = false;
                try {
                    get();
                    dispose();
                    if (nguoiDung.isPhienHopLe()) { khiLuu.run(); }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    lblLoi.setForeground(new Color(0xB42318));
                    lblLoi.setText("Tác vụ bị gián đoạn. Vui lòng kiểm tra lại danh sách.");
                } catch (ExecutionException ex) {
                    lblLoi.setForeground(new Color(0xB42318));
                    Throwable loi = ex.getCause();
                    if (loi instanceof IllegalArgumentException || loi instanceof SecurityException) {
                        lblLoi.setText(loi.getMessage());
                    } else {
                        LOGGER.log(Level.SEVERE, "Không thể đặt lại mật khẩu.", loi);
                        lblLoi.setText("Không thể đặt lại mật khẩu. Vui lòng thử lại.");
                    }
                }
                btnLuu.setEnabled(true);
                btnHuy.setEnabled(true);
                txtMatKhau.setEnabled(true);
                txtXacNhan.setEnabled(true);
            }
        }.execute();
    }

    /** Không đóng giữa lúc ghi; nút Hủy, Esc và dấu X đều xóa dữ liệu mật khẩu khi đóng. */
    @Override protected void processWindowEvent(WindowEvent event) {
        if (event.getID() == WindowEvent.WINDOW_CLOSING) {
            if (!dangLuu) { dispose(); }
            return;
        }
        super.processWindowEvent(event);
    }

    /** Xóa mật khẩu khỏi các ô nhập khi dialog đóng hoặc phiên bị thu hồi. */
    @Override public void dispose() {
        txtMatKhau.setText("");
        txtXacNhan.setText("");
        super.dispose();
    }
}
