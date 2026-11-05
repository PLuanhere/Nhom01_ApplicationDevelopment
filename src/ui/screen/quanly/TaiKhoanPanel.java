package ui.screen.quanly;

import dao.NhanVienDAO;
import dao.TaiKhoanDAO;
import entity.NhanVien;
import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import ui.component.UiChung;
import ui.component.UiTheme;

/** Hiển thị danh sách tài khoản và điều hướng các thao tác quản trị. */
public final class TaiKhoanPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;
    private final TaiKhoanDAO dao = new TaiKhoanDAO();
    private final TaiKhoan nguoiDung;

    // Các vùng nội dung và biểu mẫu con của màn hình.
    private final JPanel pnlDanhSach = new JPanel(new BorderLayout(0, 14));

    // Ô tìm kiếm và các bộ lọc danh sách.
    private final JTextField txtTim = UiChung.taoTimKiem("Tìm mã TK, tài khoản, nhân viên");
    private final JComboBox<String> cmbSapXep = new JComboBox<>(
            new String[] { "Mã tăng dần", "Tên A → Z", "Tên Z → A" });
    private final JComboBox<String> cmbTrangThai = new JComboBox<>(
            new String[] { "Tất cả trạng thái", "Đang hoạt động", "Đã khóa" });
    private final JComboBox<Object> cmbVaiTro = new JComboBox<>(new Object[] { "Tất cả vai trò", VaiTro.QUAN_LY,
            VaiTro.NHAN_VIEN_TIEP_NHAN, VaiTro.NHAN_VIEN_KY_THUAT, VaiTro.THU_NGAN });
    // Bảng dữ liệu và mô hình các dòng hiển thị.
    private final DefaultTableModel model = new DefaultTableModel(
            new String[] { "Mã TK", "Tên đăng nhập", "Nhân viên", "Vai trò được cấp", "Trạng thái", "Thao tác" }, 0);
    private final JTable tblTaiKhoan = new JTable(model);

    // Các nút thực hiện thao tác của màn hình.
    private final JButton btnBaoMat = UiChung.taoNut("Khóa, mở & đặt lại mật khẩu", false);

    // Số bản ghi đang hiển thị và phản hồi sau thao tác danh sách.
    private final JLabel lblKetQua = UiChung.taoNhan("", 12, false, new Color(0x18794E));
    private final JLabel lblSoLuong = UiChung.taoNhan("", 11, false, UiTheme.APP_MUTED);
    private final JLabel lblHienThi = UiChung.taoNhan("", 11, false, UiTheme.APP_MUTED);
    private TaiKhoanFormPanel pnlHoSo;
    private BaoMatTaiKhoanPanel pnlBaoMat;
    private JPanel pnlTheDanhSach;

    /** Tạo danh sách tài khoản, bộ lọc và các nút cấp tài khoản, bảo mật. */
    public TaiKhoanPanel(TaiKhoan nguoiDung) {
        super(new BorderLayout());
        this.nguoiDung = nguoiDung;
        setOpaque(false);
        JButton btnCap = UiChung.taoNut("+ Cấp tài khoản", true);
        btnCap.setActionCommand("cap");
        btnCap.addActionListener(this);
        btnBaoMat.setEnabled(false);
        btnBaoMat.setToolTipText("Chọn một tài khoản trong bảng để mở tác vụ bảo mật.");
        btnBaoMat.setActionCommand("baoMat");
        btnBaoMat.addActionListener(this);
        JPanel pnlTren = new JPanel(new BorderLayout(0, 14));
        pnlTren.setOpaque(false);
        pnlTren.add(
                UiChung.taoDauTrang("Quản lý / Hệ thống / Tài khoản & phân quyền", "Tài khoản & phân quyền",
                        "Một nhân viên dùng tài khoản riêng và chỉ sử dụng chức năng đúng vai trò."),
                BorderLayout.NORTH);
        pnlTren.add(taoThanhCongCu());
        JPanel pnlNut = UiChung.taoHangNut(btnCap, btnBaoMat);
        JPanel pnlHanhDong = new JPanel(new BorderLayout(0, 8));
        pnlHanhDong.setOpaque(false);
        pnlHanhDong.add(pnlNut, BorderLayout.NORTH);
        lblKetQua.setVisible(false);
        lblKetQua.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));
        pnlHanhDong.add(lblKetQua);
        pnlTren.add(pnlHanhDong, BorderLayout.SOUTH);
        pnlDanhSach.setOpaque(false);
        pnlDanhSach.add(pnlTren, BorderLayout.NORTH);
        pnlTheDanhSach = taoDanhSach();
        pnlDanhSach.add(pnlTheDanhSach);
        add(pnlDanhSach);
        taiDanhSach();
    }

    /** Căn lề và bố trí nội dung theo chiều rộng cửa sổ. */
    @Override
    public void doLayout() {
        int le = Math.max(0, (getWidth() - 1588) / 2);
        if (getInsets().left != le) {
            setBorder(BorderFactory.createEmptyBorder(0, le, 0, le));
        }
        super.doLayout();
    }

    /** Tạo thanh tìm kiếm, bộ lọc và nút đặt lại điều kiện. */
    private JPanel taoThanhCongCu() {
        txtTim.getDocument().addDocumentListener(new DocumentListener() {
            /** Cập nhật kết quả tìm kiếm khi nội dung ô tìm thay đổi. */
            @Override
            public void insertUpdate(DocumentEvent e) {
                taiDanhSach();
            }

            /** Cập nhật kết quả tìm kiếm khi nội dung ô tìm thay đổi. */
            @Override
            public void removeUpdate(DocumentEvent e) {
                taiDanhSach();
            }

            /** Cập nhật kết quả tìm kiếm khi nội dung ô tìm thay đổi. */
            @Override
            public void changedUpdate(DocumentEvent e) {
                taiDanhSach();
            }
        });
        cmbTrangThai.setActionCommand("trangThai");
        cmbTrangThai.addActionListener(this);
        cmbSapXep.setActionCommand("sapXep");
        cmbSapXep.addActionListener(this);
        JButton btnLamMoi = UiChung.taoNut("Làm mới", false);
        btnLamMoi.setActionCommand("lamMoi");
        btnLamMoi.addActionListener(this);
        cmbVaiTro.setActionCommand("vaiTro");
        cmbVaiTro.addActionListener(this);
        return UiChung.taoBoLoc(txtTim, btnLamMoi, cmbTrangThai, cmbVaiTro);
    }

    /** Tạo thẻ danh sách gồm tiêu đề, bảng, sắp xếp và số kết quả. */
    private JPanel taoDanhSach() {
        taoBang();
        return UiChung.theBang("Danh sách tài khoản", tblTaiKhoan, lblSoLuong, lblHienThi, cmbSapXep);
    }

    /** Tạo bảng chỉ đọc, phủ chiều rộng và không cho kéo thay đổi cột. */
    private void taoBang() {
        UiChung.dinhDangBang(tblTaiKhoan, 90, 155, 240, 150, 145, 130);
        ganSuKienBang();
    }

    /** Gắn thao tác mở chi tiết bằng chuột hoặc phím Enter trên bảng. */
    private void ganSuKienBang() {
        tblTaiKhoan.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            /** Bật nút bảo mật khi bảng có tài khoản được chọn. */
            @Override
            public void valueChanged(ListSelectionEvent event) {
                btnBaoMat.setEnabled(tblTaiKhoan.getSelectedRow() >= 0);
            }
        });
        tblTaiKhoan.addMouseListener(new MouseAdapter() {
            /** Mở chi tiết của dòng được nhấp đúp hoặc chọn ở cột thao tác. */
            @Override
            public void mouseClicked(MouseEvent event) {
                if (tblTaiKhoan.rowAtPoint(event.getPoint()) >= 0
                        && (tblTaiKhoan.columnAtPoint(event.getPoint()) == 5 || event.getClickCount() == 2)) {
                    TaiKhoan tk = layDongChon();
                    if (tk != null) {
                        moHoSo(tk);
                    }
                }
            }
        });
        tblTaiKhoan.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "chiTiet");
        tblTaiKhoan.getActionMap().put("chiTiet", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            /** Thực hiện thao tác tương ứng với nút, bộ lọc hoặc phím vừa chọn. */
            @Override
            public void actionPerformed(ActionEvent event) {
                TaiKhoan tk = layDongChon();
                if (tk != null) {
                    moHoSo(tk);
                }
            }
        });
    }

    /** Đọc dữ liệu theo điều kiện tìm, lọc, sắp xếp rồi cập nhật bảng và số kết quả. */
    private void taiDanhSach() {
        int dongChon = tblTaiKhoan.getSelectedRow();
        String maChon = dongChon < 0 ? null : String.valueOf(tblTaiKhoan.getValueAt(dongChon, 0));
        int hoatDong = cmbTrangThai.getSelectedIndex() == 0 ? -1 : cmbTrangThai.getSelectedIndex() == 1 ? 1 : 0;
        VaiTro vaiTro = cmbVaiTro.getSelectedItem() instanceof VaiTro ? (VaiTro) cmbVaiTro.getSelectedItem() : null;
        List<TaiKhoan> ketQua = dao.timKiem(nguoiDung, txtTim.getText(), hoatDong, vaiTro,
                cmbSapXep.getSelectedIndex());
        model.setRowCount(0);
        for (TaiKhoan tk : ketQua) {
            NhanVien nv = new NhanVienDAO().timTheoMa(nguoiDung, tk.getMaNhanVien());
            model.addRow(new Object[] { tk.getMaTaiKhoan(), tk.getTenDangNhap(), nv.getHoTen(),
                    tk.getVaiTro().getTenHienThi(), TaiKhoanDAO.tenTrangThai(tk), "Xem chi tiết" });
        }
        lblSoLuong.setText(ketQua.size() + " kết quả đang hiển thị");
        lblHienThi.setText("Hiển thị " + ketQua.size() + " / " + dao.layDanhSach(nguoiDung).size() + " hồ sơ");
        if (ketQua.isEmpty()) {
            lblHienThi.setText(lblHienThi.getText() + " · Không có kết quả phù hợp.");
        }
        chonDong(maChon);
        lblKetQua.setVisible(false);
    }

    /** Chọn và cuộn đến bản ghi theo mã nếu còn nằm trong danh sách lọc. */
    private boolean chonDong(String ma) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals(ma)) {
                int dong = tblTaiKhoan.convertRowIndexToView(i);
                tblTaiKhoan.setRowSelectionInterval(dong, dong);
                tblTaiKhoan.scrollRectToVisible(tblTaiKhoan.getCellRect(dong, 0, true));
                return true;
            }
        }
        return false;
    }

    /** Hiển thị kết quả lưu, giữ điều kiện lọc và chọn lại dòng liên quan. */
    void hienKetQua(String ma, String thongBao) {
        moDanhSach();
        boolean timThay = chonDong(ma);
        lblKetQua.setText(thongBao + (timThay ? "" : " Tài khoản nằm ngoài bộ lọc hiện tại."));
        lblKetQua.setToolTipText(lblKetQua.getText());
        lblKetQua.setVisible(true);
        revalidate();
    }

    /** Đọc lại tài khoản theo mã trên dòng đang chọn. */
    private TaiKhoan layDongChon() {
        int dong = tblTaiKhoan.getSelectedRow();
        return dong < 0 ? null
                : dao.timTheoMa(nguoiDung,
                        String.valueOf(model.getValueAt(tblTaiKhoan.convertRowIndexToModel(dong), 0)));
    }

    /** Mở thao tác bảo mật cho tài khoản đang chọn, yêu cầu chọn dòng nếu chưa có. */
    private void moBaoMatDaChon() {
        TaiKhoan tk = layDongChon();
        if (tk == null) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một tài khoản trong danh sách.");
        } else {
            moBaoMat(tk);
        }
    }

    /** Mở biểu mẫu xem chi tiết hoặc thêm mới và giữ điều kiện tìm kiếm của danh sách. */
    void moHoSo(TaiKhoan tk) {
        pnlHoSo = new TaiKhoanFormPanel(this, nguoiDung, tk);
        hienTrang(pnlHoSo);
    }

    /** Đọc lại tài khoản và mở form bảo mật, kèm phản hồi nếu được truyền vào. */
    void moBaoMat(TaiKhoan tk) {
        pnlHoSo = null;
        pnlBaoMat = new BaoMatTaiKhoanPanel(this, nguoiDung, dao.timTheoMa(nguoiDung, tk.getMaTaiKhoan()));
        hienTrang(pnlBaoMat);
    }

    /** Đọc lại tài khoản và mở form bảo mật, kèm phản hồi nếu được truyền vào. */
    void moBaoMat(TaiKhoan tk, String thongBao) {
        moBaoMat(tk);
        pnlBaoMat.hienThanhCong(thongBao);
    }

    /** Đóng form hiện tại, đọc lại bảng và giữ các điều kiện lọc. */
    public void moDanhSach() {
        pnlHoSo = null;
        pnlBaoMat = null;
        taiDanhSach();
        hienTrang(pnlDanhSach);
    }

    /** Thay màn hình tài khoản đang hiển thị và cập nhật bố cục. */
    private void hienTrang(JPanel trang) {
        removeAll();
        add(trang);
        revalidate();
        repaint();
    }

    /** Kiểm tra dữ liệu chưa lưu và xác nhận trước khi rời màn hình. */
    public boolean choPhepRoiTrang() {
        if (pnlHoSo != null && !pnlHoSo.choPhepRoiTrang()) {
            return false;
        }
        if (pnlBaoMat != null && !pnlBaoMat.choPhepRoiTrang()) {
            return false;
        }
        moDanhSach();
        return true;
    }

    /** Thực hiện thao tác tương ứng với nút, bộ lọc hoặc phím vừa chọn. */
    @Override
    public void actionPerformed(ActionEvent event) {
        String lenh = event.getActionCommand();
        if ("cap".equals(lenh)) {
            moHoSo(null);
        } else if ("baoMat".equals(lenh)) {
            moBaoMatDaChon();
        } else if ("trangThai".equals(lenh)) {
            taiDanhSach();
        } else if ("sapXep".equals(lenh)) {
            taiDanhSach();
        } else if ("lamMoi".equals(lenh)) {
            txtTim.setText("");
            cmbTrangThai.setSelectedIndex(0);
            cmbVaiTro.setSelectedIndex(0);
            cmbSapXep.setSelectedIndex(0);
            taiDanhSach();
        } else if ("vaiTro".equals(lenh)) {
            taiDanhSach();
        }
    }
}
