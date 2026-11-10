package ui.screen.quanly;

import dao.TaiKhoanDAO;
import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
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
import javax.swing.SwingUtilities;

import ui.component.UiChung;
import ui.component.UiTheme;
import ui.dialog.quanly.DatLaiMatKhauDialog;

/** Biểu mẫu khóa, mở tài khoản, đổi vai trò và đặt lại mật khẩu. */
public final class BaoMatTaiKhoanPanel extends QuanLyFormPanel implements ActionListener {

    private static final long serialVersionUID = 1L;
    private final TaiKhoanPanel manHinh;
    private final TaiKhoan nguoiDung;
    private final TaiKhoan dich;
    private final TaiKhoanDAO dao = new TaiKhoanDAO();

    // Các ô nhập và lựa chọn trên biểu mẫu.
    private final JComboBox<String> cmbHanhDong = new JComboBox<>();
    private final JComboBox<VaiTro> cmbVaiTro = new JComboBox<>(VaiTro.values());
    private final JTextField txtLyDo = UiChung.taoONhap(true);
    private final JPasswordField txtMatKhau = new JPasswordField();
    private final JPasswordField txtXacNhan = new JPasswordField();

    // Các nhãn báo lỗi và mô tả trạng thái biểu mẫu.
    private final JLabel lblLoi = UiChung.taoNhan(" ", 12, false, new Color(0xB42318));

    // Các nút thực hiện thao tác của màn hình.
    private final JButton btnXacNhan = UiChung.taoNut("Xem lại thao tác & xác nhận", true);

    // Các vùng nội dung và biểu mẫu con của màn hình.
    private JPanel pnlVaiTro;
    private JPanel pnlMatKhau;
    private boolean dangLuu;

    /** Tạo biểu mẫu quản trị tài khoản đích và gắn sự kiện chọn tác vụ bảo mật. */
    public BaoMatTaiKhoanPanel(TaiKhoanPanel manHinh, TaiKhoan nguoiDung, TaiKhoan dich) {
        super();
        this.manHinh = manHinh;
        this.nguoiDung = nguoiDung;
        this.dich = dich;
        if (!dich.getMaTaiKhoan().equals(nguoiDung.getMaTaiKhoan())) {
            cmbHanhDong.addItem(dich.isHoatDong() ? "Khóa tài khoản" : "Mở tài khoản");
            cmbHanhDong.addItem("Đổi vai trò");
        }
        cmbHanhDong.addItem("Đặt lại mật khẩu");
        cmbHanhDong.setSelectedIndex(-1);
        cmbHanhDong.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            /** Hiển thị nội dung lựa chọn rõ ràng trong danh sách hoặc hộp chọn. */
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected,
                    boolean focus) {
                return super.getListCellRendererComponent(list, value == null ? "Chọn thao tác..." : value, index,
                        selected, focus);
            }
        });
        cmbVaiTro.setSelectedIndex(-1);
        JButton btnQuayLai = UiChung.taoNut("Danh sách / quay lại", false);
        btnQuayLai.setActionCommand("quayLai");
        btnQuayLai.addActionListener(this);
        // Tiêu đề và điều hướng quay lại danh sách.
        add(UiChung.taoDauTrang("Quản lý / Khóa tài khoản & đặt lại mật khẩu", "Khóa tài khoản & đặt lại mật khẩu",
                "Chỉ quản lý được phép thay đổi trạng thái truy cập hoặc khởi tạo lại mật khẩu nhân viên.", btnQuayLai),
                BorderLayout.NORTH);
        for (JComponent o : new JComponent[] { cmbHanhDong, cmbVaiTro, txtMatKhau, txtXacNhan }) {
            UiChung.dinhDangO(o);
        }
        // Vùng thông tin có cuộn khi chiều cao cửa sổ không đủ.
        add(UiChung.taoVungCuon(taoTheBaoMat()));
        btnXacNhan.setPreferredSize(new Dimension(260, 42));
        // Thanh phản hồi và các nút thao tác nằm dưới biểu mẫu.
        add(UiChung.taoChanTrang(lblLoi, btnXacNhan), BorderLayout.SOUTH);
        cmbHanhDong.setActionCommand("hanhDong");
        cmbHanhDong.addActionListener(this);
        btnXacNhan.setActionCommand("xacNhan");
        btnXacNhan.addActionListener(this);
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ESCAPE"), "quayLai");
        getActionMap().put("quayLai", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            /** Thực hiện thao tác tương ứng với nút, bộ lọc hoặc phím vừa chọn. */
            @Override
            public void actionPerformed(ActionEvent event) {
                btnQuayLai.doClick();
            }
        });
        doiHanhDong();
    }

    /** Tạo thẻ chọn tác vụ, lý do và các trường bảo mật liên quan. */
    private JPanel taoTheBaoMat() {
        JPanel pnl = taoThe();
        them(pnl, UiChung.taoNhan("Bảo mật tài khoản & quyền truy cập", 18, true, UiTheme.APP_TEXT));
        them(pnl, UiChung.taoNhan("Khóa, mở và đổi quyền là thao tác quản trị riêng; đặt lại mật khẩu là tác vụ riêng.",
                12, false, UiTheme.APP_MUTED));
        String ten = dao.timNhanVienTheoMa(nguoiDung, dich.getMaNhanVien()).getHoTen();
        them(pnl,
                UiChung.taoHang(
                        UiChung.taoTruong("Tài khoản đang chọn",
                                UiChung.taoNhan(dich + " · " + ten, 13, true, UiTheme.APP_TEXT)),
                        UiChung.taoTruong("Vai trò hiện tại",
                                UiChung.taoNhan(
                                        dich.getVaiTro().getTenHienThi() + " · " + TaiKhoanDAO.tenTrangThai(dich), 13,
                                        true, UiTheme.APP_TEXT))));
        them(pnl, UiChung.taoNhan("Thay đổi quyền / trạng thái", 15, true, UiTheme.APP_TEXT));
        them(pnl, UiChung.taoHang(UiChung.taoTruong("Hành động *", cmbHanhDong),
                UiChung.taoTruong("Lý do thay đổi *", txtLyDo)));
        JPanel pnlTrong = new JPanel();
        pnlTrong.setOpaque(false);
        pnlVaiTro = UiChung.taoHang(UiChung.taoTruong("Vai trò mới *", cmbVaiTro), pnlTrong);
        pnlVaiTro.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlVaiTro.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        pnl.add(pnlVaiTro);
        pnlMatKhau = new JPanel();
        pnlMatKhau.setOpaque(false);
        pnlMatKhau.setLayout(new BoxLayout(pnlMatKhau, BoxLayout.Y_AXIS));
        them(pnlMatKhau, UiChung.taoHang(UiChung.taoTruong("Mật khẩu mới *", txtMatKhau),
                UiChung.taoTruong("Nhập lại mật khẩu *", txtXacNhan)));
        them(pnlMatKhau, UiChung.taoNhan("<html>Ít nhất 8 ký tự, có chữ hoa, chữ số và ký tự đặc biệt.</html>", 12,
                false, UiTheme.APP_MUTED));
        pnlMatKhau.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnl.add(pnlMatKhau);
        them(pnl, UiChung.taoLuuY("Kiểm tra tài khoản đích trước khi thực hiện. Khóa, đổi vai trò hoặc đặt lại "
                + "mật khẩu sẽ buộc tài khoản đó đăng nhập lại ngay. Không được tự khóa hoặc tự hạ quyền."));
        return pnl;
    }

    /** Tạo thẻ nội dung nền trắng có đường viền dùng chung. */
    private static JPanel taoThe() {
        JPanel pnl = UiChung.taoThe(null);
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setBorder(
                BorderFactory.createCompoundBorder(pnl.getBorder(), BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        return pnl;
    }

    /** Thêm một thành phần UI và khoảng cách vào thẻ nội dung. */
    private static void them(JPanel pnl, JComponent hang) {
        TaiKhoanFormPanel.themHang(pnl, hang);
    }

    /** Hiện đúng trường vai trò hoặc mật khẩu theo tác vụ bảo mật được chọn. */
    private void doiHanhDong() {
        String hanhDong = (String) cmbHanhDong.getSelectedItem();
        boolean datLai = "Đặt lại mật khẩu".equals(hanhDong);
        pnlVaiTro.setVisible("Đổi vai trò".equals(hanhDong));
        pnlMatKhau.setVisible(datLai);
        if (!datLai) {
            xoaMatKhau();
        }
        btnXacNhan.setEnabled(hanhDong != null);
        btnXacNhan.setText(hanhDong == null ? "Chọn thao tác để tiếp tục"
                : datLai ? "Xem lại mật khẩu" : "Xác nhận " + hanhDong.toLowerCase());
        lblLoi.setText(" ");
        lblLoi.setVisible(false);
        revalidate();
    }

    /** Đọc và kiểm tra lý do thay đổi tài khoản trước khi xác nhận. */
    private String layLyDo() {
        String lyDo = txtLyDo.getText().trim();
        if (lyDo.isEmpty()) {
            txtLyDo.requestFocusInWindow();
            throw new IllegalArgumentException("Nhập lý do thay đổi.");
        }
        return lyDo;
    }

    /** Kiểm tra dữ liệu và yêu cầu xác nhận tác vụ bảo mật trước khi lưu. */
    private void xacNhan() {
        if (dangLuu) {
            return;
        }
        String hanhDong = (String) cmbHanhDong.getSelectedItem();
        if ("Đặt lại mật khẩu".equals(hanhDong)) {
            moDatLai();
            return;
        }
        try {
            if (cmbHanhDong.getSelectedIndex() < 0) {
                throw new IllegalArgumentException("Hãy chọn hành động.");
            }
            String lyDo = layLyDo();
            VaiTro vaiTro = "Đổi vai trò".equals(hanhDong) ? (VaiTro) cmbVaiTro.getSelectedItem() : dich.getVaiTro();
            if (vaiTro == null) {
                cmbVaiTro.requestFocusInWindow();
                throw new IllegalArgumentException("Chọn vai trò mới.");
            }
            boolean hoatDong = "Khóa tài khoản".equals(hanhDong) ? false
                    : "Mở tài khoản".equals(hanhDong) ? true : dich.isHoatDong();
            if (vaiTro == dich.getVaiTro() && hoatDong == dich.isHoatDong()) {
                throw new IllegalArgumentException("Vai trò và trạng thái chưa thay đổi.");
            }
            if (dich.getMaTaiKhoan().equals(nguoiDung.getMaTaiKhoan()) && (!hoatDong || vaiTro != VaiTro.QUAN_LY)) {
                throw new IllegalArgumentException("Không được tự khóa hoặc tự hạ quyền.");
            }
            String noiDung = dich + "\nVai trò: " + dich.getVaiTro() + " → " + vaiTro + "\nTrạng thái: "
                    + TaiKhoanDAO.tenTrangThai(dich) + " → " + (hoatDong ? "Đang hoạt động" : "Đã khóa") + "\nLý do: "
                    + lyDo + "\nTài khoản đích sẽ phải đăng nhập lại ngay.";
            if (JOptionPane.showConfirmDialog(this, noiDung, "Xác nhận thay đổi tài khoản",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.OK_OPTION) {
                return;
            }
            luuThayDoi(vaiTro, hoatDong, lyDo);
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            hienThongBao(ex.getMessage(), false);
        }
    }

    /** Hiển thị phản hồi thao tác và chọn màu theo kết quả. */
    private void hienThongBao(String chu, boolean thanhCong) {
        lblLoi.setText(chu);
        lblLoi.setToolTipText(chu);
        lblLoi.setForeground(new Color(thanhCong ? 0x18794E : 0xB42318));
        lblLoi.setVisible(true);
        revalidate();
    }

    /** Hiển thị thông báo lưu thành công trên form bảo mật. */
    void hienThanhCong(String chu) {
        hienThongBao(chu, true);
    }

    /** Gọi DAO đổi vai trò hoặc trạng thái và hiển thị kết quả quản trị. */
    private void luuThayDoi(VaiTro vaiTro, boolean hoatDong, String lyDo) {
        batDangLuu(true);
        try {
            dao.capNhat(nguoiDung, dich, vaiTro, hoatDong, lyDo);
            if (nguoiDung.isPhienHopLe()) {
                manHinh.moBaoMat(dich, "Đã cập nhật tài khoản " + dich.getMaTaiKhoan() + ".");
            }
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            hienThongBao(ex.getMessage(), false);
        } finally {
            batDangLuu(false);
        }
    }

    /** Kiểm tra mật khẩu, lý do và mở hộp thoại xác nhận đặt lại. */
    private void moDatLai() {
        if (dangLuu) {
            return;
        }
        char[] matKhau = txtMatKhau.getPassword();
        char[] xacNhan = txtXacNhan.getPassword();
        try {
            String lyDo = layLyDo();
            TaiKhoan.kiemTraMatKhauMoi(matKhau);
            if (!Arrays.equals(matKhau, xacNhan)) {
                txtXacNhan.requestFocusInWindow();
                throw new IllegalArgumentException("Hai lần nhập mật khẩu không khớp.");
            }
            DatLaiMatKhauDialog dlg = new DatLaiMatKhauDialog(SwingUtilities.getWindowAncestor(this), nguoiDung, dich,
                    lyDo, new ActionListener() {
                        /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
                        @Override
                        public void actionPerformed(ActionEvent event) {

                            xoaMatKhau();
                            manHinh.moBaoMat(dich, "Đã đặt lại mật khẩu cho " + dich.getMaTaiKhoan() + ".");

                        }
                    });
            dlg.napMatKhau(matKhau, xacNhan);
            dlg.setVisible(true);
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            hienThongBao(ex.getMessage(), false);
        } finally {
            Arrays.fill(matKhau, '\0');
            Arrays.fill(xacNhan, '\0');
        }
    }

    /** Kiểm tra dữ liệu chưa lưu và xác nhận trước khi rời màn hình. */
    public boolean choPhepRoiTrang() {
        if (dangLuu) {
            return false;
        }
        boolean daSua = cmbHanhDong.getSelectedIndex() >= 0 || !txtLyDo.getText().isEmpty()
                || txtMatKhau.getDocument().getLength() > 0 || txtXacNhan.getDocument().getLength() > 0;
        return !daSua || JOptionPane.showConfirmDialog(this, "Bỏ thao tác bảo mật chưa xác nhận?", "Rời trang bảo mật",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** Khóa hoặc mở điều khiển để tránh thao tác lặp trong lúc lưu. */
    private void batDangLuu(boolean luu) {
        dangLuu = luu;
        for (JComponent o : new JComponent[] { cmbHanhDong, cmbVaiTro, txtLyDo, txtMatKhau, txtXacNhan, btnXacNhan }) {
            o.setEnabled(!luu);
        }
        if (!luu) {
            doiHanhDong();
        }
    }

    /** Xóa nội dung hai ô mật khẩu khỏi biểu mẫu. */
    private void xoaMatKhau() {
        txtMatKhau.setText("");
        txtXacNhan.setText("");
    }

    /** Thực hiện thao tác tương ứng với nút, bộ lọc hoặc phím vừa chọn. */
    @Override
    public void actionPerformed(ActionEvent event) {
        String lenh = event.getActionCommand();
        if ("quayLai".equals(lenh)) {
            if (choPhepRoiTrang()) {
                xoaMatKhau();
                manHinh.moDanhSach();
            }
        } else if ("hanhDong".equals(lenh)) {
            doiHanhDong();
        } else if ("xacNhan".equals(lenh)) {
            xacNhan();
        }
    }
}
