package ui.screen.quanly;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
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
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;

import dao.NhanVienDAO;
import dao.TaiKhoanDAO;
import entity.NhanVien;
import entity.TaiKhoan;
import entity.VaiTro;
import ui.component.QuanLyUi;
import ui.component.UiTheme;

/** Form cấp/xem/sửa tài khoản; tên đăng nhập và nhân viên cố định sau khi cấp. */
public final class TaiKhoanFormPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(TaiKhoanFormPanel.class.getName());
    private final TaiKhoanPanel manHinh;
    private final TaiKhoan nguoiDung;
    private final TaiKhoan banDau;
    private final TaiKhoanDAO dao = new TaiKhoanDAO();
    private final JTextField txtMa = QuanLyUi.taoONhap(false);
    private final JTextField txtTen = QuanLyUi.taoONhap(true);
    private final JComboBox<NhanVien> cmbNhanVien = new JComboBox<>();
    private final JComboBox<VaiTro> cmbVaiTro = new JComboBox<>(VaiTro.values());
    private final JComboBox<String> cmbTrangThai = new JComboBox<>(new String[] {"Đang hoạt động", "Đã khóa"});
    private final JPasswordField txtMatKhau = new JPasswordField();
    private final JPasswordField txtXacNhan = new JPasswordField();
    private final JTextField txtLyDo = QuanLyUi.taoONhap(true);
    private final JLabel lblLoi = QuanLyUi.taoNhan(" ", 12, false, new Color(0xB42318));
    private final JButton btnLuu = QuanLyUi.taoNut("Kiểm tra & cấp tài khoản", true);
    private final JButton btnSua = QuanLyUi.taoNut("Chỉnh sửa", true);
    private final JButton btnBaoMat = QuanLyUi.taoNut("Khóa, mở & đặt lại mật khẩu", false);
    private List<Object> giaTriBanDau;
    private boolean dangSua;
    private boolean dangLuu;
    private JPanel pnlLyDo;

    /** @param manHinh trang danh sách @param nguoiDung Quản lý @param banDau null khi cấp mới */
    public TaiKhoanFormPanel(TaiKhoanPanel manHinh, TaiKhoan nguoiDung, TaiKhoan banDau) {
        super(new BorderLayout(0, 14));
        this.manHinh = manHinh;
        this.nguoiDung = nguoiDung;
        this.banDau = banDau;
        setOpaque(false);
        napDuLieu();
        JButton btnQuayLai = QuanLyUi.taoNut("Danh sách / quay lại", false);
        btnQuayLai.addActionListener(event -> quayLai());
        add(QuanLyUi.taoDauTrang("Quản lý / Hệ thống / Cấp & quản lý tài khoản",
                "Cấp & quản lý tài khoản",
                "Tạo tài khoản nhân viên; gán một vai trò chính tại thời điểm quản lý.", btnQuayLai),
                BorderLayout.NORTH);
        add(QuanLyUi.taoVungCuon(taoForm()));
        btnLuu.setPreferredSize(new Dimension(260, 42));
        add(QuanLyUi.taoChanTrang(lblLoi, btnBaoMat, btnSua, btnLuu), BorderLayout.SOUTH);
        batChinhSua(banDau == null);
        giaTriBanDau = layGiaTri();
        btnLuu.addActionListener(event -> luu());
        btnSua.addActionListener(event -> batChinhSua(true));
        btnBaoMat.addActionListener(event -> {
            if (choPhepRoiTrang()) { manHinh.moBaoMat(banDau); }
        });
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ESCAPE"), "quayLai");
        getActionMap().put("quayLai", new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override public void actionPerformed(ActionEvent event) { quayLai(); }
        });
    }

    private void napDuLieu() {
        txtMa.setText(banDau == null ? dao.maTiepTheo(nguoiDung) : banDau.getMaTaiKhoan());
        List<NhanVien> nhanVien = banDau == null ? dao.nhanVienChuaCoTaiKhoan(nguoiDung)
                : List.of(new NhanVienDAO().timTheoMa(nguoiDung, banDau.getMaNhanVien()));
        for (NhanVien nv : nhanVien) { cmbNhanVien.addItem(nv); }
        cmbVaiTro.setSelectedIndex(-1);
        if (banDau != null) {
            txtTen.setText(banDau.getTenDangNhap());
            cmbVaiTro.setSelectedItem(banDau.getVaiTro());
            cmbTrangThai.setSelectedIndex(banDau.isHoatDong() ? 0 : 1);
            btnLuu.setText("Kiểm tra & lưu thay đổi");
        } else if (nhanVien.isEmpty()) {
            lblLoi.setText("Chưa có nhân viên để cấp tài khoản. Hãy thêm hồ sơ tại trang Nhân viên.");
            lblLoi.setForeground(UiTheme.APP_MUTED);
        }
    }

    private JPanel taoForm() {
        JPanel pnl = QuanLyUi.taoThe(null);
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setBorder(BorderFactory.createCompoundBorder(pnl.getBorder(),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        themHang(pnl, QuanLyUi.taoNhan(banDau == null ? "Cấp tài khoản nhân viên" : "Hồ sơ tài khoản nhân viên",
                18, true, UiTheme.APP_TEXT));
        themHang(pnl, QuanLyUi.taoNhan(banDau == null
                ? "Chọn nhân viên, tên đăng nhập và vai trò trước khi cấp tài khoản."
                : "Tên đăng nhập và nhân viên liên kết được giữ cố định sau khi cấp.",
                12, false, UiTheme.APP_MUTED));
        for (JComponent o : List.of(cmbNhanVien, cmbVaiTro, cmbTrangThai, txtMatKhau, txtXacNhan)) {
            QuanLyUi.dinhDangO(o);
        }
        for (JComboBox<?> cmb : List.of(cmbNhanVien, cmbVaiTro, cmbTrangThai)) { dinhDangLuaChon(cmb); }
        JTextField txtChinhSach = QuanLyUi.taoONhap(false);
        txtChinhSach.setText("Một tài khoản chỉ truy cập chức năng được cấp");
        txtChinhSach.setToolTipText(txtChinhSach.getText());
        themHang(pnl, QuanLyUi.taoHang(QuanLyUi.taoTruong("Mã tài khoản · chỉ đọc", txtMa),
                QuanLyUi.taoTruong("Nhân viên *", cmbNhanVien)));
        themHang(pnl, QuanLyUi.taoHang(QuanLyUi.taoTruong(
                banDau == null ? "Tên đăng nhập * · kiểm tra trùng" : "Tên đăng nhập · cố định", txtTen),
                QuanLyUi.taoTruong("Vai trò được cấp *", cmbVaiTro)));
        if (banDau == null) {
            themHang(pnl, QuanLyUi.taoHang(QuanLyUi.taoTruong("Mật khẩu mới *", txtMatKhau),
                    QuanLyUi.taoTruong("Nhập lại mật khẩu *", txtXacNhan)));
            themHang(pnl, QuanLyUi.taoHang(QuanLyUi.taoTruong("Trạng thái tài khoản", cmbTrangThai),
                    QuanLyUi.taoTruong("Chính sách quyền · chỉ đọc", txtChinhSach)));
        } else {
            JTextField txtNgayTao = QuanLyUi.taoONhap(false);
            txtNgayTao.setText(banDau.getNgayTao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            themHang(pnl, QuanLyUi.taoHang(QuanLyUi.taoTruong("Trạng thái tài khoản", cmbTrangThai),
                    QuanLyUi.taoTruong("Ngày cấp · chỉ đọc", txtNgayTao)));
        }
        pnlLyDo = QuanLyUi.taoTruong("Lý do thay đổi *", txtLyDo);
        pnlLyDo.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlLyDo.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        pnl.add(pnlLyDo);
        String luuY;
        if (banDau == null) {
            luuY = "Mật khẩu cần ít nhất 8 ký tự, có chữ hoa, số và ký tự đặc biệt. "
                    + "Tài khoản ở trạng thái Đã khóa sẽ chưa đăng nhập được.";
        } else if (banDau.getMaTaiKhoan().equals(nguoiDung.getMaTaiKhoan())) {
            luuY = "Đây là tài khoản đang đăng nhập. Không được tự khóa hoặc tự đổi vai trò. "
                    + "Bạn có thể đặt lại mật khẩu tại phần bảo mật tài khoản.";
        } else {
            luuY = "Đổi vai trò hoặc trạng thái cần có lý do và xác nhận; tài khoản sẽ phải đăng nhập lại. "
                    + "Để đặt lại mật khẩu, mở phần bảo mật tài khoản.";
        }
        themHang(pnl, QuanLyUi.taoLuuY(luuY));
        return pnl;
    }

    /** Giá trị chỉ đọc vẫn rõ chữ; ô chưa chọn có hướng dẫn thay vì để trống. */
    private void dinhDangLuaChon(JComboBox<?> cmb) {
        cmb.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean selected, boolean focus) {
                Object chu = value instanceof NhanVien nv ? nv.getMaNhanVien() + " · " + nv.getHoTen()
                        : value == null ? (cmb == cmbNhanVien ? "Không có nhân viên phù hợp" : "Chọn vai trò...") : value;
                return super.getListCellRendererComponent(list, chu, index, selected, focus);
            }
            @Override protected void paintComponent(Graphics g) {
                if (!cmb.isEnabled()) { setForeground(UiTheme.APP_TEXT); }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                super.paintComponent(g);
            }
        });
    }

    /** Các hàng bám trái và giữ chiều cao, để nội dung cuộn được khi cửa sổ thấp. */
    static void themHang(JPanel pnl, JComponent hang) {
        hang.setAlignmentX(Component.LEFT_ALIGNMENT);
        hang.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                hang instanceof JPanel ? Integer.MAX_VALUE : hang.getPreferredSize().height));
        pnl.add(hang);
        pnl.add(Box.createVerticalStrut(12));
    }

    private void batChinhSua(boolean choSua) {
        dangSua = choSua;
        boolean laBanThan = banDau != null && banDau.getMaTaiKhoan().equals(nguoiDung.getMaTaiKhoan());
        txtTen.setEditable(choSua && banDau == null);
        txtTen.setBackground(banDau == null ? Color.WHITE : new Color(0xF5F7FB));
        cmbNhanVien.setEnabled(choSua && banDau == null && cmbNhanVien.getItemCount() > 0);
        cmbVaiTro.setEnabled(choSua && !laBanThan);
        cmbTrangThai.setEnabled(choSua && !laBanThan);
        txtMatKhau.setEnabled(banDau == null);
        txtXacNhan.setEnabled(banDau == null);
        btnSua.setVisible(!choSua);
        btnSua.setEnabled(!laBanThan);
        btnSua.setToolTipText(laBanThan ? "Không được tự thay đổi vai trò hoặc khóa tài khoản đang đăng nhập." : null);
        btnBaoMat.setVisible(banDau != null && !choSua);
        btnLuu.setVisible(choSua);
        btnLuu.setEnabled(banDau != null || cmbNhanVien.getItemCount() > 0);
        pnlLyDo.setVisible(banDau != null && choSua && !laBanThan);
        revalidate();
    }

    private List<Object> layGiaTri() {
        return Arrays.asList(txtTen.getText(), cmbNhanVien.getSelectedItem(), cmbVaiTro.getSelectedItem(),
                cmbTrangThai.getSelectedItem(), txtLyDo.getText());
    }

    /** @return cho phép rời sau khi xác nhận bỏ thay đổi; không cho rời khi đang lưu */
    public boolean choPhepRoiTrang() {
        if (dangLuu) { return false; }
        boolean daSua = dangSua && (!layGiaTri().equals(giaTriBanDau)
                || txtMatKhau.getDocument().getLength() > 0 || txtXacNhan.getDocument().getLength() > 0);
        return !daSua || JOptionPane.showConfirmDialog(this, "Bỏ thay đổi tài khoản chưa lưu?",
                "Rời trang tài khoản", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private void quayLai() {
        if (choPhepRoiTrang()) { xoaMatKhau(); manHinh.moDanhSach(); }
    }

    private void luu() {
        if (dangLuu) { return; }
        lblLoi.setText(" ");
        lblLoi.setVisible(false);
        lblLoi.setForeground(new Color(0xB42318));
        try {
            if (!(cmbNhanVien.getSelectedItem() instanceof NhanVien nv)) {
                throw new IllegalArgumentException("Hãy chọn nhân viên chưa có tài khoản.");
            }
            if (txtTen.getText().isBlank()) {
                txtTen.requestFocusInWindow();
                throw new IllegalArgumentException("Nhập tên đăng nhập.");
            }
            VaiTro vaiTro = (VaiTro) cmbVaiTro.getSelectedItem();
            if (vaiTro == null) { cmbVaiTro.requestFocusInWindow(); throw new IllegalArgumentException("Hãy chọn vai trò."); }
            boolean hoatDong = cmbTrangThai.getSelectedIndex() == 0;
            TaiKhoan thongTin = new TaiKhoan(txtMa.getText(), nv.getMaNhanVien(), txtTen.getText(),
                    vaiTro, hoatDong, banDau == null ? LocalDateTime.now() : banDau.getNgayTao());
            if (banDau == null) {
                for (TaiKhoan daCo : dao.layDanhSach(nguoiDung)) {
                    if (daCo.getTenDangNhap().equals(thongTin.getTenDangNhap())) {
                        txtTen.requestFocusInWindow();
                        throw new IllegalArgumentException("Tên đăng nhập đã tồn tại.");
                    }
                }
                kiemTraMatKhau();
            }
            else if (banDau.getVaiTro() == vaiTro && banDau.isHoatDong() == hoatDong) {
                throw new IllegalArgumentException("Vai trò và trạng thái chưa thay đổi.");
            }
            if (banDau != null && banDau.getMaTaiKhoan().equals(nguoiDung.getMaTaiKhoan())) {
                throw new IllegalArgumentException("Không được tự thay đổi vai trò hoặc khóa tài khoản đang đăng nhập.");
            }
            String lyDo = banDau == null ? "Cấp tài khoản" : txtLyDo.getText().trim();
            if (lyDo.isBlank()) { txtLyDo.requestFocusInWindow(); throw new IllegalArgumentException("Nhập lý do thay đổi."); }
            String noiDung = thongTin + " · " + nv.getHoTen()
                    + "\nVai trò: " + (banDau == null ? "" : banDau.getVaiTro() + " → ") + vaiTro.getTenHienThi()
                    + "\nTrạng thái: " + (banDau == null ? "" : TaiKhoanDAO.tenTrangThai(banDau) + " → ")
                    + TaiKhoanDAO.tenTrangThai(thongTin)
                    + (banDau == null ? "" : "\nLý do: " + lyDo + "\nTài khoản này sẽ phải đăng nhập lại.");
            if (JOptionPane.showConfirmDialog(this, noiDung, "Xác nhận tài khoản",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.OK_OPTION) { return; }
            char[] matKhau = txtMatKhau.getPassword();
            luuTaiKhoan(thongTin, matKhau, lyDo);
        } catch (IllegalArgumentException | SecurityException ex) {
            hienLoi(ex.getMessage());
        }
    }

    /** Hiển thị lỗi cùng thanh nút để không bị khuất trong vùng đang cuộn. */
    private void hienLoi(String chu) {
        lblLoi.setForeground(new Color(0xB42318));
        lblLoi.setText(chu);
        lblLoi.setToolTipText(chu);
        lblLoi.setVisible(true);
        revalidate();
    }

    /** Lưu ở nền; lỗi giữ nguyên form, thành công mới quay lại danh sách. */
    private void luuTaiKhoan(TaiKhoan thongTin, char[] matKhau, String lyDo) {
        batDangLuu(true);
        hienLoi("Đang lưu...");
        lblLoi.setForeground(UiTheme.APP_MUTED);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                try {
                    if (banDau == null) { dao.them(nguoiDung, thongTin, matKhau); }
                    else { dao.capNhat(nguoiDung, banDau, thongTin.getVaiTro(), thongTin.isHoatDong(), lyDo); }
                    return null;
                } finally { Arrays.fill(matKhau, '\0'); }
            }
            @Override protected void done() {
                batDangLuu(false);
                try {
                    get();
                    xoaMatKhau();
                    if (nguoiDung.isPhienHopLe()) {
                        manHinh.hienKetQua(thongTin.getMaTaiKhoan(),
                                "Đã " + (banDau == null ? "cấp" : "cập nhật") + " tài khoản "
                                + thongTin.getMaTaiKhoan() + ".");
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    hienLoi("Tác vụ bị gián đoạn. Vui lòng kiểm tra lại danh sách.");
                } catch (ExecutionException ex) {
                    Throwable loi = ex.getCause();
                    if (loi instanceof IllegalArgumentException || loi instanceof SecurityException) {
                        hienLoi(loi.getMessage());
                    } else {
                        LOGGER.log(Level.SEVERE, "Không thể lưu tài khoản.", loi);
                        hienLoi("Không thể lưu tài khoản. Vui lòng thử lại.");
                    }
                }
            }
        }.execute();
    }

    private void kiemTraMatKhau() {
        char[] matKhau = txtMatKhau.getPassword();
        char[] xacNhan = txtXacNhan.getPassword();
        try {
            TaiKhoan.kiemTraMatKhauMoi(matKhau);
            if (!Arrays.equals(matKhau, xacNhan)) {
                txtXacNhan.requestFocusInWindow();
                throw new IllegalArgumentException("Hai lần nhập mật khẩu không khớp.");
            }
        } finally { Arrays.fill(matKhau, '\0'); Arrays.fill(xacNhan, '\0'); }
    }

    private void batDangLuu(boolean luu) {
        dangLuu = luu;
        for (JComponent o : List.of(txtTen, cmbNhanVien, cmbVaiTro, cmbTrangThai, txtLyDo,
                txtMatKhau, txtXacNhan, btnLuu)) {
            o.setEnabled(!luu);
        }
        if (!luu) { batChinhSua(dangSua); }
    }

    private void xoaMatKhau() { txtMatKhau.setText(""); txtXacNhan.setText(""); }
}
