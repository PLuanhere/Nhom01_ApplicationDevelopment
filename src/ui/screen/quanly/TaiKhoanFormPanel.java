package ui.screen.quanly;

import dao.TaiKhoanDAO;
import entity.NhanVien;
import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

import ui.component.UiChung;
import ui.component.UiTheme;

/** Biểu mẫu cấp tài khoản và cập nhật vai trò hoặc trạng thái truy cập. */
public final class TaiKhoanFormPanel extends QuanLyFormPanel implements ActionListener {

    private static final long serialVersionUID = 1L;
    private final TaiKhoanPanel manHinh;
    private final TaiKhoan nguoiDung;
    private final TaiKhoan banDau;
    private final TaiKhoanDAO dao = new TaiKhoanDAO();

    // Các ô nhập và lựa chọn trên biểu mẫu.
    private final JTextField txtMa = UiChung.taoONhap(false);
    private final JComboBox<NhanVien> cmbNhanVien = new JComboBox<>();
    private final JComboBox<VaiTro> cmbVaiTro = new JComboBox<>(VaiTro.values());
    private final JComboBox<String> cmbTrangThai = new JComboBox<>(new String[] { "Đang hoạt động", "Đã khóa" });
    private final JPasswordField txtMatKhau = new JPasswordField();
    private final JPasswordField txtXacNhan = new JPasswordField();
    private final JTextField txtLyDo = UiChung.taoONhap(true);

    // Các nhãn báo lỗi và mô tả trạng thái biểu mẫu.
    private final JLabel lblLoi = UiChung.taoNhan(" ", 12, false, new Color(0xB42318));

    // Các nút thực hiện thao tác của màn hình.
    private final JButton btnLuu = UiChung.taoNut("Kiểm tra & cấp tài khoản", true);
    private final JButton btnSua = UiChung.taoNut("Chỉnh sửa", true);
    private final JButton btnBaoMat = UiChung.taoNut("Khóa, mở & đặt lại mật khẩu", false);
    private String[] giaTriBanDau;
    private boolean dangSua;
    private boolean dangLuu;

    // Các vùng nội dung và biểu mẫu con của màn hình.
    private JPanel pnlLyDo;

    /** Tạo biểu mẫu cấp hoặc sửa tài khoản, nạp dữ liệu và gắn sự kiện lưu. */
    public TaiKhoanFormPanel(TaiKhoanPanel manHinh, TaiKhoan nguoiDung, TaiKhoan banDau) {
        super();
        this.manHinh = manHinh;
        this.nguoiDung = nguoiDung;
        this.banDau = banDau;
        napDuLieu();
        JButton btnQuayLai = UiChung.taoNut("Danh sách / quay lại", false);
        btnQuayLai.setActionCommand("quayLai");
        btnQuayLai.addActionListener(this);
        // Tiêu đề và điều hướng quay lại danh sách.
        add(UiChung.taoDauTrang("Quản lý / Hệ thống / Cấp & quản lý tài khoản", "Cấp & quản lý tài khoản",
                "Tạo tài khoản nhân viên; gán một vai trò chính tại thời điểm quản lý.", btnQuayLai),
                BorderLayout.NORTH);
        // Vùng thông tin có cuộn khi chiều cao cửa sổ không đủ.
        add(UiChung.taoVungCuon(taoForm()));
        btnLuu.setPreferredSize(new Dimension(260, 42));
        // Thanh phản hồi và các nút thao tác nằm dưới biểu mẫu.
        add(UiChung.taoChanTrang(lblLoi, btnBaoMat, btnSua, btnLuu), BorderLayout.SOUTH);
        batChinhSua(banDau == null);
        giaTriBanDau = layGiaTri();
        btnLuu.setActionCommand("luu");
        btnLuu.addActionListener(this);
        btnSua.setActionCommand("sua");
        btnSua.addActionListener(this);
        btnBaoMat.setActionCommand("baoMat");
        btnBaoMat.addActionListener(this);
        cmbNhanVien.setActionCommand("nhanVien");
        cmbNhanVien.addActionListener(this);
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ESCAPE"), "quayLai");
        getActionMap().put("quayLai", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            /** Thực hiện thao tác tương ứng với nút, bộ lọc hoặc phím vừa chọn. */
            @Override
            public void actionPerformed(ActionEvent event) {
                quayLai();
            }
        });
    }

    /** Đưa dữ liệu gốc vào các trường và đặt lại mốc kiểm tra thay đổi. */
    private void napDuLieu() {
        List<NhanVien> nhanVien = new ArrayList<>();
        if (banDau == null) {
            nhanVien = dao.nhanVienChuaCoTaiKhoan(nguoiDung);
        } else {
            nhanVien.add(dao.timNhanVienTheoMa(nguoiDung, banDau.getMaNhanVien()));
        }
        for (NhanVien nv : nhanVien) {
            cmbNhanVien.addItem(nv);
        }
        cmbVaiTro.setSelectedIndex(-1);
        if (banDau != null) {
            txtMa.setText(banDau.getMaNhanVien());
            cmbVaiTro.setSelectedItem(banDau.getVaiTro());
            cmbTrangThai.setSelectedIndex(banDau.isHoatDong() ? 0 : 1);
            btnLuu.setText("Kiểm tra & lưu thay đổi");
        } else if (nhanVien.isEmpty()) {
            lblLoi.setText("Chưa có nhân viên để cấp tài khoản. Hãy thêm hồ sơ tại trang Nhân viên.");
            lblLoi.setForeground(UiTheme.APP_MUTED);
        } else {
            txtMa.setText(nhanVien.get(0).getMaNhanVien());
        }
    }

    /** Tạo các trường tài khoản theo chế độ cấp mới hoặc xem, chỉnh sửa. */
    private JPanel taoForm() {
        JPanel pnl = UiChung.taoThe(null);
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setBorder(
                BorderFactory.createCompoundBorder(pnl.getBorder(), BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        themHang(pnl, UiChung.taoNhan(banDau == null ? "Cấp tài khoản nhân viên" : "Hồ sơ tài khoản nhân viên", 18,
                true, UiTheme.APP_TEXT));
        themHang(pnl,
                UiChung.taoNhan(
                        banDau == null ? "Chọn nhân viên và vai trò; mã nhân viên sẽ dùng để đăng nhập."
                                : "Mã nhân viên dùng để đăng nhập và được giữ cố định sau khi cấp.",
                        12, false, UiTheme.APP_MUTED));
        for (JComponent o : new JComponent[] { cmbNhanVien, cmbVaiTro, cmbTrangThai, txtMatKhau, txtXacNhan }) {
            UiChung.dinhDangO(o);
        }
        for (JComboBox<?> cmb : new JComboBox<?>[] { cmbNhanVien, cmbVaiTro, cmbTrangThai }) {
            dinhDangLuaChon(cmb);
        }
        themHang(pnl, UiChung.taoHang(UiChung.taoTruong("Mã nhân viên / đăng nhập · chỉ đọc", txtMa),
                UiChung.taoTruong("Nhân viên *", cmbNhanVien)));
        themHang(pnl, UiChung.taoHang(UiChung.taoTruong("Vai trò được cấp *", cmbVaiTro),
                UiChung.taoTruong("Trạng thái tài khoản", cmbTrangThai)));
        if (banDau == null) {
            themHang(pnl, UiChung.taoHang(UiChung.taoTruong("Mật khẩu mới *", txtMatKhau),
                    UiChung.taoTruong("Nhập lại mật khẩu *", txtXacNhan)));
        } else {
            JTextField txtNgayTao = UiChung.taoONhap(false);
            txtNgayTao.setText(banDau.getNgayTao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            themHang(pnl, UiChung.taoHang(UiChung.taoTruong("Trạng thái tài khoản", cmbTrangThai),
                    UiChung.taoTruong("Ngày cấp · chỉ đọc", txtNgayTao)));
        }
        pnlLyDo = UiChung.taoTruong("Lý do thay đổi *", txtLyDo);
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
        themHang(pnl, UiChung.taoLuuY(luuY));
        return pnl;
    }

    /** Định dạng nhãn nhân viên, vai trò và lựa chọn chưa có dữ liệu. */
    private void dinhDangLuaChon(JComboBox<?> cmb) {
        cmb.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            /** Hiển thị nội dung lựa chọn rõ ràng trong danh sách hoặc hộp chọn. */
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected,
                    boolean focus) {
                Object chu = value instanceof NhanVien
                        ? ((NhanVien) value).getMaNhanVien() + " · " + ((NhanVien) value).getHoTen()
                        : value == null ? (cmb == cmbNhanVien ? "Không có nhân viên phù hợp" : "Chọn vai trò...")
                                : value;
                return super.getListCellRendererComponent(list, chu, index, selected, focus);
            }

            /** Vẽ nội dung giao diện và giữ chữ hoặc gợi ý nhập liệu dễ đọc. */
            @Override
            protected void paintComponent(Graphics g) {
                if (!cmb.isEnabled()) {
                    setForeground(UiTheme.APP_TEXT);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                super.paintComponent(g);
            }
        });
    }

    /** Thêm một hàng thành phần UI và khoảng cách vào bố cục dọc. */
    static void themHang(JPanel pnl, JComponent hang) {
        hang.setAlignmentX(Component.LEFT_ALIGNMENT);
        hang.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                hang instanceof JPanel ? Integer.MAX_VALUE : hang.getPreferredSize().height));
        pnl.add(hang);
        pnl.add(Box.createVerticalStrut(12));
    }

    /** Bật hoặc khóa các trường và nút theo chế độ xem hoặc chỉnh sửa. */
    private void batChinhSua(boolean choSua) {
        dangSua = choSua;
        boolean laBanThan = banDau != null && banDau.getMaTaiKhoan().equals(nguoiDung.getMaTaiKhoan());
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

    /** Lấy các giá trị đang nhập để phát hiện thay đổi chưa lưu. */
    private String[] layGiaTri() {
        return new String[] { String.valueOf(cmbNhanVien.getSelectedItem()),
                String.valueOf(cmbVaiTro.getSelectedItem()), String.valueOf(cmbTrangThai.getSelectedItem()),
                txtLyDo.getText() };
    }

    /** Kiểm tra dữ liệu chưa lưu và xác nhận trước khi rời màn hình. */
    public boolean choPhepRoiTrang() {
        if (dangLuu) {
            return false;
        }
        boolean daSua = dangSua && (UiChung.daThayDoi(giaTriBanDau, layGiaTri())
                || txtMatKhau.getDocument().getLength() > 0 || txtXacNhan.getDocument().getLength() > 0);
        return !daSua || JOptionPane.showConfirmDialog(this, "Bỏ thay đổi tài khoản chưa lưu?", "Rời trang tài khoản",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** Kiểm tra thay đổi chưa lưu rồi quay về danh sách. */
    private void quayLai() {
        if (choPhepRoiTrang()) {
            xoaMatKhau();
            manHinh.moDanhSach();
        }
    }

    /** Kiểm tra nhân viên, mật khẩu, quyền và lý do trước khi xác nhận lưu tài khoản. */
    private void luu() {
        if (dangLuu) {
            return;
        }
        lblLoi.setText(" ");
        lblLoi.setVisible(false);
        lblLoi.setForeground(new Color(0xB42318));
        try {
            if (!(cmbNhanVien.getSelectedItem() instanceof NhanVien)) {
                throw new IllegalArgumentException("Hãy chọn nhân viên chưa có tài khoản.");
            }
            NhanVien nv = (NhanVien) cmbNhanVien.getSelectedItem();
            VaiTro vaiTro = (VaiTro) cmbVaiTro.getSelectedItem();
            if (vaiTro == null) {
                cmbVaiTro.requestFocusInWindow();
                throw new IllegalArgumentException("Hãy chọn vai trò.");
            }
            boolean hoatDong = cmbTrangThai.getSelectedIndex() == 0;
            TaiKhoan thongTin = new TaiKhoan(nv.getMaNhanVien(), vaiTro, hoatDong,
                    banDau == null ? LocalDateTime.now() : banDau.getNgayTao());
            if (banDau == null) {
                kiemTraMatKhau();
            } else if (banDau.getVaiTro() == vaiTro && banDau.isHoatDong() == hoatDong) {
                throw new IllegalArgumentException("Vai trò và trạng thái chưa thay đổi.");
            }
            if (banDau != null && banDau.getMaTaiKhoan().equals(nguoiDung.getMaTaiKhoan())) {
                throw new IllegalArgumentException(
                        "Không được tự thay đổi vai trò hoặc khóa tài khoản đang đăng nhập.");
            }
            String lyDo = banDau == null ? "Cấp tài khoản" : txtLyDo.getText().trim();
            if (lyDo.isBlank()) {
                txtLyDo.requestFocusInWindow();
                throw new IllegalArgumentException("Nhập lý do thay đổi.");
            }
            String noiDung = thongTin + " · " + nv.getHoTen() + "\nVai trò: "
                    + (banDau == null ? "" : banDau.getVaiTro() + " → ") + vaiTro.getTenHienThi() + "\nTrạng thái: "
                    + (banDau == null ? "" : TaiKhoanDAO.tenTrangThai(banDau) + " → ")
                    + TaiKhoanDAO.tenTrangThai(thongTin)
                    + (banDau == null ? "" : "\nLý do: " + lyDo + "\nTài khoản này sẽ phải đăng nhập lại.");
            if (JOptionPane.showConfirmDialog(this, noiDung, "Xác nhận tài khoản", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE) != JOptionPane.OK_OPTION) {
                return;
            }
            char[] matKhau = txtMatKhau.getPassword();
            luuTaiKhoan(thongTin, matKhau, lyDo);
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            hienLoi(ex.getMessage());
        }
    }

    /** Hiển thị lỗi nhập hoặc lưu để người dùng sửa lại dữ liệu. */
    private void hienLoi(String chu) {
        lblLoi.setForeground(new Color(0xB42318));
        lblLoi.setText(chu);
        lblLoi.setToolTipText(chu);
        lblLoi.setVisible(true);
        revalidate();
    }

    /** Gọi DAO cấp hoặc cập nhật tài khoản; lỗi giữ form, thành công về danh sách. */
    private void luuTaiKhoan(TaiKhoan thongTin, char[] matKhau, String lyDo) {
        batDangLuu(true);
        try {
            if (banDau == null) {
                dao.them(nguoiDung, thongTin, matKhau);
            } else {
                dao.capNhat(nguoiDung, banDau, thongTin.getVaiTro(), thongTin.isHoatDong(), lyDo);
            }
            xoaMatKhau();
            if (nguoiDung.isPhienHopLe()) {
                manHinh.hienKetQua(thongTin.getMaTaiKhoan(), "Đã lưu tài khoản " + thongTin.getMaTaiKhoan() + ".");
            }
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            hienLoi(ex.getMessage());
        } finally {
            for (int i = 0; i < matKhau.length; i++) {
                matKhau[i] = '\0';
            }
            batDangLuu(false);
        }
    }

    /** Kiểm tra chính sách mật khẩu và hai lần nhập phải khớp nhau. */
    private void kiemTraMatKhau() {
        char[] matKhau = txtMatKhau.getPassword();
        char[] xacNhan = txtXacNhan.getPassword();
        try {
            TaiKhoan.kiemTraMatKhauMoi(matKhau);
            if (!Arrays.equals(matKhau, xacNhan)) {
                txtXacNhan.requestFocusInWindow();
                throw new IllegalArgumentException("Hai lần nhập mật khẩu không khớp.");
            }
        } finally {
            Arrays.fill(matKhau, '\0');
            Arrays.fill(xacNhan, '\0');
        }
    }

    /** Khóa hoặc mở điều khiển để tránh thao tác lặp trong lúc lưu. */
    private void batDangLuu(boolean luu) {
        dangLuu = luu;
        for (JComponent o : new JComponent[] { cmbNhanVien, cmbVaiTro, cmbTrangThai, txtLyDo, txtMatKhau,
                txtXacNhan, btnLuu }) {
            o.setEnabled(!luu);
        }
        if (!luu) {
            batChinhSua(dangSua);
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
            quayLai();
        } else if ("luu".equals(lenh)) {
            luu();
        } else if ("sua".equals(lenh)) {
            batChinhSua(true);
        } else if ("baoMat".equals(lenh)) {
            if (choPhepRoiTrang()) {
                manHinh.moBaoMat(banDau);
            }
        } else if ("nhanVien".equals(lenh) && cmbNhanVien.getSelectedItem() instanceof NhanVien) {
            txtMa.setText(((NhanVien) cmbNhanVien.getSelectedItem()).getMaNhanVien());
        }
    }
}
