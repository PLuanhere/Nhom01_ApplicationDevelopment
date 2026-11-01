package ui.screen.quanly;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.KeyStroke;

import dao.NhanVienDAO;
import dao.TaiKhoanDAO;
import entity.TaiKhoan;
import entity.VaiTro;
import ui.component.QuanLyUi;
import ui.component.UiTheme;
import ui.dialog.DatLaiMatKhauDialog;

/** Khóa/mở, đổi một vai trò và đặt lại mật khẩu cho đúng tài khoản đã chọn. */
public final class BaoMatTaiKhoanPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(BaoMatTaiKhoanPanel.class.getName());
    private final TaiKhoanPanel manHinh;
    private final TaiKhoan nguoiDung;
    private final TaiKhoan dich;
    private final TaiKhoanDAO dao = new TaiKhoanDAO();
    private final JComboBox<String> cmbHanhDong = new JComboBox<>();
    private final JComboBox<VaiTro> cmbVaiTro = new JComboBox<>(VaiTro.values());
    private final JTextField txtLyDo = QuanLyUi.taoONhap(true);
    private final JPasswordField txtMatKhau = new JPasswordField();
    private final JPasswordField txtXacNhan = new JPasswordField();
    private final JLabel lblLoi = QuanLyUi.taoNhan(" ", 12, false, new Color(0xB42318));
    private final JButton btnXacNhan = QuanLyUi.taoNut("Xem lại thao tác & xác nhận", true);
    private JPanel pnlVaiTro;
    private JPanel pnlMatKhau;
    private boolean dangLuu;

    /** @param manHinh màn hình quản lý @param nguoiDung Quản lý @param dich tài khoản đích */
    public BaoMatTaiKhoanPanel(TaiKhoanPanel manHinh, TaiKhoan nguoiDung, TaiKhoan dich) {
        super(new BorderLayout(0, 14));
        this.manHinh = manHinh;
        this.nguoiDung = nguoiDung;
        this.dich = dich;
        setOpaque(false);
        if (!dich.getMaTaiKhoan().equals(nguoiDung.getMaTaiKhoan())) {
            cmbHanhDong.addItem(dich.isHoatDong() ? "Khóa tài khoản" : "Mở tài khoản");
            cmbHanhDong.addItem("Đổi vai trò");
        }
        cmbHanhDong.addItem("Đặt lại mật khẩu");
        cmbHanhDong.setSelectedIndex(-1);
        cmbHanhDong.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean selected, boolean focus) {
                return super.getListCellRendererComponent(list,
                        value == null ? "Chọn thao tác..." : value, index, selected, focus);
            }
        });
        cmbVaiTro.setSelectedIndex(-1);
        JButton btnQuayLai = QuanLyUi.taoNut("Danh sách / quay lại", false);
        btnQuayLai.addActionListener(event -> {
            if (choPhepRoiTrang()) { xoaMatKhau(); manHinh.moDanhSach(); }
        });
        add(QuanLyUi.taoDauTrang("Quản lý / Khóa tài khoản & đặt lại mật khẩu",
                "Khóa tài khoản & đặt lại mật khẩu",
                "Chỉ quản lý được phép thay đổi trạng thái truy cập hoặc khởi tạo lại mật khẩu nhân viên.",
                btnQuayLai), BorderLayout.NORTH);
        for (JComponent o : List.of(cmbHanhDong, cmbVaiTro, txtMatKhau, txtXacNhan)) {
            QuanLyUi.dinhDangO(o);
        }
        add(QuanLyUi.taoVungCuon(taoTheBaoMat()));
        btnXacNhan.setPreferredSize(new Dimension(260, 42));
        add(QuanLyUi.taoChanTrang(lblLoi, btnXacNhan), BorderLayout.SOUTH);
        cmbHanhDong.addActionListener(event -> doiHanhDong());
        btnXacNhan.addActionListener(event -> xacNhan());
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ESCAPE"), "quayLai");
        getActionMap().put("quayLai", new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override public void actionPerformed(ActionEvent event) { btnQuayLai.doClick(); }
        });
        doiHanhDong();
    }

    private JPanel taoTheBaoMat() {
        JPanel pnl = taoThe();
        them(pnl, QuanLyUi.taoNhan("Bảo mật tài khoản & quyền truy cập", 18, true, UiTheme.APP_TEXT));
        them(pnl, QuanLyUi.taoNhan("Khóa, mở và đổi quyền là thao tác quản trị riêng; đặt lại mật khẩu là tác vụ riêng.",
                12, false, UiTheme.APP_MUTED));
        String ten = new NhanVienDAO().timTheoMa(nguoiDung, dich.getMaNhanVien()).getHoTen();
        them(pnl, QuanLyUi.taoHang(
                QuanLyUi.taoTruong("Tài khoản đang chọn", QuanLyUi.taoNhan(
                        dich + " · " + ten, 13, true, UiTheme.APP_TEXT)),
                QuanLyUi.taoTruong("Vai trò hiện tại", QuanLyUi.taoNhan(
                        dich.getVaiTro().getTenHienThi() + " · " + TaiKhoanDAO.tenTrangThai(dich),
                        13, true, UiTheme.APP_TEXT))));
        them(pnl, QuanLyUi.taoNhan("Thay đổi quyền / trạng thái", 15, true, UiTheme.APP_TEXT));
        them(pnl, QuanLyUi.taoHang(QuanLyUi.taoTruong("Hành động *", cmbHanhDong),
                QuanLyUi.taoTruong("Lý do thay đổi *", txtLyDo)));
        JPanel pnlTrong = new JPanel();
        pnlTrong.setOpaque(false);
        pnlVaiTro = QuanLyUi.taoHang(QuanLyUi.taoTruong("Vai trò mới *", cmbVaiTro), pnlTrong);
        pnlVaiTro.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlVaiTro.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        pnl.add(pnlVaiTro);
        pnlMatKhau = new JPanel();
        pnlMatKhau.setOpaque(false);
        pnlMatKhau.setLayout(new BoxLayout(pnlMatKhau, BoxLayout.Y_AXIS));
        them(pnlMatKhau, QuanLyUi.taoHang(QuanLyUi.taoTruong("Mật khẩu mới *", txtMatKhau),
                QuanLyUi.taoTruong("Nhập lại mật khẩu *", txtXacNhan)));
        them(pnlMatKhau, QuanLyUi.taoNhan("<html>Ít nhất 8 ký tự, có chữ hoa, chữ số và ký tự đặc biệt.</html>",
                12, false, UiTheme.APP_MUTED));
        pnlMatKhau.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnl.add(pnlMatKhau);
        them(pnl, QuanLyUi.taoLuuY("Kiểm tra tài khoản đích trước khi thực hiện. Khóa, đổi vai trò hoặc đặt lại "
                + "mật khẩu sẽ buộc tài khoản đó đăng nhập lại ngay. Không được tự khóa hoặc tự hạ quyền."));
        return pnl;
    }

    private static JPanel taoThe() {
        JPanel pnl = QuanLyUi.taoThe(null);
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setBorder(BorderFactory.createCompoundBorder(pnl.getBorder(),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        return pnl;
    }

    private static void them(JPanel pnl, JComponent hang) { TaiKhoanFormPanel.themHang(pnl, hang); }

    private void doiHanhDong() {
        String hanhDong = (String) cmbHanhDong.getSelectedItem();
        boolean datLai = "Đặt lại mật khẩu".equals(hanhDong);
        pnlVaiTro.setVisible("Đổi vai trò".equals(hanhDong));
        pnlMatKhau.setVisible(datLai);
        if (!datLai) { xoaMatKhau(); }
        btnXacNhan.setEnabled(hanhDong != null);
        btnXacNhan.setText(hanhDong == null ? "Chọn thao tác để tiếp tục"
                : datLai ? "Xem lại mật khẩu" : "Xác nhận " + hanhDong.toLowerCase(java.util.Locale.ROOT));
        lblLoi.setText(" ");
        lblLoi.setVisible(false);
        revalidate();
    }

    private String layLyDo() {
        String lyDo = txtLyDo.getText().trim();
        if (lyDo.isEmpty()) { txtLyDo.requestFocusInWindow(); throw new IllegalArgumentException("Nhập lý do thay đổi."); }
        return lyDo;
    }

    private void xacNhan() {
        if (dangLuu) { return; }
        String hanhDong = (String) cmbHanhDong.getSelectedItem();
        if ("Đặt lại mật khẩu".equals(hanhDong)) { moDatLai(); return; }
        try {
            if (cmbHanhDong.getSelectedIndex() < 0) { throw new IllegalArgumentException("Hãy chọn hành động."); }
            String lyDo = layLyDo();
            VaiTro vaiTro = "Đổi vai trò".equals(hanhDong) ? (VaiTro) cmbVaiTro.getSelectedItem() : dich.getVaiTro();
            if (vaiTro == null) { cmbVaiTro.requestFocusInWindow(); throw new IllegalArgumentException("Chọn vai trò mới."); }
            boolean hoatDong = "Khóa tài khoản".equals(hanhDong) ? false
                    : "Mở tài khoản".equals(hanhDong) ? true : dich.isHoatDong();
            if (vaiTro == dich.getVaiTro() && hoatDong == dich.isHoatDong()) {
                throw new IllegalArgumentException("Vai trò và trạng thái chưa thay đổi.");
            }
            if (dich.getMaTaiKhoan().equals(nguoiDung.getMaTaiKhoan())
                    && (!hoatDong || vaiTro != VaiTro.QUAN_LY)) {
                throw new IllegalArgumentException("Không được tự khóa hoặc tự hạ quyền.");
            }
            String noiDung = dich + "\nVai trò: " + dich.getVaiTro() + " → " + vaiTro
                    + "\nTrạng thái: " + TaiKhoanDAO.tenTrangThai(dich) + " → " + (hoatDong ? "Đang hoạt động" : "Đã khóa")
                    + "\nLý do: " + lyDo + "\nTài khoản đích sẽ phải đăng nhập lại ngay.";
            if (JOptionPane.showConfirmDialog(this, noiDung, "Xác nhận thay đổi tài khoản",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.OK_OPTION) { return; }
            luuThayDoi(vaiTro, hoatDong, lyDo);
        } catch (IllegalArgumentException | SecurityException ex) { hienThongBao(ex.getMessage(), false); }
    }

    /** Phản hồi luôn nằm trên thanh nút, không bị che khi cuộn. */
    private void hienThongBao(String chu, boolean thanhCong) {
        lblLoi.setText(chu);
        lblLoi.setToolTipText(chu);
        lblLoi.setForeground(new Color(thanhCong ? 0x18794E : 0xB42318));
        lblLoi.setVisible(true);
        revalidate();
    }

    /** Hiện kết quả sau khi tải lại hồ sơ đã thay đổi. */
    void hienThanhCong(String chu) { hienThongBao(chu, true); }

    /** Mỗi xác nhận chỉ ghi một lần; tải lại đúng hồ sơ sau khi DAO lưu thành công. */
    private void luuThayDoi(VaiTro vaiTro, boolean hoatDong, String lyDo) {
        batDangLuu(true);
        hienThongBao("Đang lưu...", false);
        lblLoi.setForeground(UiTheme.APP_MUTED);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                dao.capNhat(nguoiDung, dich, vaiTro, hoatDong, lyDo);
                return null;
            }
            @Override protected void done() {
                batDangLuu(false);
                try {
                    get();
                    if (nguoiDung.isPhienHopLe()) { manHinh.moBaoMat(dich, "Đã cập nhật tài khoản " + dich.getMaTaiKhoan() + "."); }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    hienThongBao("Tác vụ bị gián đoạn. Vui lòng kiểm tra lại danh sách.", false);
                } catch (ExecutionException ex) {
                    Throwable loi = ex.getCause();
                    if (loi instanceof IllegalArgumentException || loi instanceof SecurityException) {
                        hienThongBao(loi.getMessage(), false);
                    } else {
                        LOGGER.log(Level.SEVERE, "Không thể cập nhật quyền/trạng thái.", loi);
                        hienThongBao("Không thể cập nhật tài khoản. Vui lòng thử lại.", false);
                    }
                }
            }
        }.execute();
    }

    private void moDatLai() {
        if (dangLuu) { return; }
        char[] matKhau = txtMatKhau.getPassword();
        char[] xacNhan = txtXacNhan.getPassword();
        try {
            String lyDo = layLyDo();
            TaiKhoan.kiemTraMatKhauMoi(matKhau);
            if (!Arrays.equals(matKhau, xacNhan)) {
                txtXacNhan.requestFocusInWindow();
                throw new IllegalArgumentException("Hai lần nhập mật khẩu không khớp.");
            }
            DatLaiMatKhauDialog dlg = new DatLaiMatKhauDialog(SwingUtilities.getWindowAncestor(this),
                    nguoiDung, dich, lyDo, () -> {
                        xoaMatKhau();
                        manHinh.moBaoMat(dich, "Đã đặt lại mật khẩu cho " + dich.getMaTaiKhoan() + ".");
                    });
            dlg.napMatKhau(matKhau, xacNhan);
            dlg.setVisible(true);
        } catch (IllegalArgumentException | SecurityException ex) { hienThongBao(ex.getMessage(), false); }
        finally { Arrays.fill(matKhau, '\0'); Arrays.fill(xacNhan, '\0'); }
    }

    /** @return không đang lưu và người dùng đồng ý bỏ thao tác chưa xác nhận */
    public boolean choPhepRoiTrang() {
        if (dangLuu) { return false; }
        boolean daSua = cmbHanhDong.getSelectedIndex() >= 0 || !txtLyDo.getText().isEmpty()
                || txtMatKhau.getDocument().getLength() > 0 || txtXacNhan.getDocument().getLength() > 0;
        return !daSua || JOptionPane.showConfirmDialog(this, "Bỏ thao tác bảo mật chưa xác nhận?",
                "Rời trang bảo mật", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private void batDangLuu(boolean luu) {
        dangLuu = luu;
        for (JComponent o : List.of(cmbHanhDong, cmbVaiTro, txtLyDo, txtMatKhau, txtXacNhan, btnXacNhan)) {
            o.setEnabled(!luu);
        }
        if (!luu) { doiHanhDong(); }
    }

    private void xoaMatKhau() {
        txtMatKhau.setText("");
        txtXacNhan.setText("");
    }
}
