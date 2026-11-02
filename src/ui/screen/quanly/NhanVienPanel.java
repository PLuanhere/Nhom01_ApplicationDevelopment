package ui.screen.quanly;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
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
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import dao.NhanVienDAO;

import entity.NhanVien;
import entity.TaiKhoan;
import entity.TrangThaiNhanVien;

import ui.component.QuanLyUi;
import ui.component.UiTheme;

/** Hiển thị danh sách nhân viên và điều hướng đến biểu mẫu hồ sơ. */
public final class NhanVienPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;

    private static final Color DUONG_KE = new Color(0xE9EDF2);

    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final TaiKhoan taiKhoan;

    // Ô tìm kiếm và các bộ lọc danh sách.
    private final JTextField txtTim = QuanLyUi.taoTimKiem("Tìm mã NV, họ tên, điện thoại");
    private final JComboBox<String> cmbSapXep = new JComboBox<>(new String[] {
            "Mã tăng dần", "Tên A → Z", "Tên Z → A"});
    private final JComboBox<Object> cmbTrangThai = new JComboBox<>(new Object[] {
            "Tất cả trạng thái", TrangThaiNhanVien.DANG_LAM, TrangThaiNhanVien.DA_NGHI});

    // Các vùng nội dung và biểu mẫu con của màn hình.
    private NhanVienFormPanel pnlHoSo;

    // Bảng dữ liệu và mô hình các dòng hiển thị.
    private final DefaultTableModel model = new DefaultTableModel(
        new String[] {"Mã NV", "Họ và tên", "Số điện thoại", "Bộ phận", "Trạng thái", "Thao tác"}, 0);
    private final JTable tblNhanVien = new JTable(model);

    // Số bản ghi đang hiển thị và phản hồi sau thao tác danh sách.
    private final JLabel lblMoTa = QuanLyUi.taoNhan("", 11.5f, false, UiTheme.APP_MUTED);
    private final JLabel lblSoLuong = QuanLyUi.taoNhan("", 10.5f, false, UiTheme.APP_MUTED);
    private final JLabel lblHienThi = QuanLyUi.taoNhan("", 10.5f, false, UiTheme.APP_MUTED);

    /** Tạo danh sách, tìm kiếm, bộ lọc và thao tác mở hồ sơ nhân viên. */
    public NhanVienPanel(TaiKhoan taiKhoan) {
        super(new CardLayout());
        if (taiKhoan == null) { throw new IllegalArgumentException("Thiếu taiKhoan."); }
        this.taiKhoan = taiKhoan;
        setBackground(UiTheme.APP_BACKGROUND);
        taiDanhSach();
        JPanel pnlTren = new JPanel(new BorderLayout(0, 14));
        pnlTren.setOpaque(false);
        pnlTren.add(taoPhanDau(), BorderLayout.NORTH);
        pnlTren.add(taoThanhCongCu());
        JButton btnThem = QuanLyUi.taoNut("+ Thêm nhân viên", true);
        btnThem.setActionCommand("them");
        btnThem.addActionListener(this);
        JPanel pnlTacVu = new JPanel(new BorderLayout(0, 8));
        pnlTacVu.setOpaque(false);
        pnlTacVu.add(QuanLyUi.taoHangNut(btnThem), BorderLayout.NORTH);
        lblMoTa.setVisible(false);
        pnlTacVu.add(lblMoTa);
        pnlTren.add(pnlTacVu, BorderLayout.SOUTH);
        JPanel pnlDanhSachChung = new JPanel(new BorderLayout(0, 14));
        pnlDanhSachChung.setOpaque(false);
        pnlDanhSachChung.add(pnlTren, BorderLayout.NORTH);

        JPanel pnlDanhSach = taoDanhSach();
        pnlDanhSachChung.add(pnlDanhSach);
        add(pnlDanhSachChung, "danhSach");
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

    /** Đọc dữ liệu theo điều kiện tìm, lọc, sắp xếp rồi cập nhật bảng và số kết quả. */
    private void taiDanhSach() {
        String maChon = tblNhanVien.getSelectedRow() < 0 ? null
        : String.valueOf(tblNhanVien.getValueAt(tblNhanVien.getSelectedRow(), 0));
        TrangThaiNhanVien trangThai = cmbTrangThai.getSelectedItem() instanceof TrangThaiNhanVien
        ? (TrangThaiNhanVien) cmbTrangThai.getSelectedItem() : null;
        List<NhanVien> ketQua = nhanVienDAO.timKiem(taiKhoan, txtTim.getText(), trangThai, cmbSapXep.getSelectedIndex());
        hienDanhSach(ketQua, nhanVienDAO.layDanhSach(taiKhoan).size());
        chonDong(maChon);
    }

    /** Đưa danh sách đã đọc vào bảng và cập nhật số bản ghi hiển thị. */
    private void hienDanhSach(List<NhanVien> danhSach, int tongSo) {
        model.setRowCount(0);
        for (NhanVien nhanVien : danhSach) {
            model.addRow(new Object[] {nhanVien.getMaNhanVien(), nhanVien.getHoTen(),
                    dinhDangDienThoai(nhanVien.getSoDienThoai()), nhanVien.getBoPhan(),
                    nhanVien.getTrangThai().getTenNgan(), "Xem chi tiết"});
        }
        lblSoLuong.setText(danhSach.size() + " kết quả đang hiển thị");
        lblHienThi.setText("Hiển thị " + danhSach.size() + " / " + tongSo + " hồ sơ");
        if (danhSach.isEmpty()) { lblHienThi.setText(lblHienThi.getText() + " · Không có nhân viên phù hợp."); }
        lblMoTa.setVisible(false);
    }

    /** Tạo breadcrumb, tiêu đề và nút quay lại hoặc thao tác của trang. */
    private JPanel taoPhanDau() {
        return QuanLyUi.taoDauTrang("Quản lý / Danh mục / Nhân viên", "Nhân viên",
            "Quản lý hồ sơ và tình trạng làm việc của nhân viên.");
    }

    /** Tạo thanh tìm kiếm, bộ lọc và nút đặt lại điều kiện. */
    private JPanel taoThanhCongCu() {
        txtTim.getDocument().addDocumentListener(new DocumentListener() {
                /** Cập nhật kết quả tìm kiếm khi nội dung ô tìm thay đổi. */
                @Override public void insertUpdate(DocumentEvent e) { taiDanhSach(); }
                /** Cập nhật kết quả tìm kiếm khi nội dung ô tìm thay đổi. */
                @Override public void removeUpdate(DocumentEvent e) { taiDanhSach(); }
                /** Cập nhật kết quả tìm kiếm khi nội dung ô tìm thay đổi. */
                @Override public void changedUpdate(DocumentEvent e) { taiDanhSach(); }
            });
        cmbTrangThai.setActionCommand("trangThai");
        cmbTrangThai.addActionListener(this);
        cmbSapXep.setActionCommand("sapXep");
        cmbSapXep.addActionListener(this);
        JButton btnLamMoi = QuanLyUi.taoNut("Làm mới", false);
        btnLamMoi.setActionCommand("lamMoi");
        btnLamMoi.addActionListener(this);

        return QuanLyUi.taoBoLoc(txtTim, btnLamMoi, cmbTrangThai);
    }

    /** Tạo thẻ danh sách gồm tiêu đề, bảng, sắp xếp và số kết quả. */
    private JPanel taoDanhSach() {
        taoBang();
        return QuanLyUi.theBang("Danh sách nhân viên", tblNhanVien, lblSoLuong, lblHienThi, cmbSapXep);
    }

    /** Tạo bảng chỉ đọc, phủ chiều rộng và không cho kéo thay đổi cột. */
    private void taoBang() {
        QuanLyUi.dinhDangBang(tblNhanVien, 90, 230, 155, 140, 130, 125);
        ganThaoTacBang();
    }

    /** Gắn thao tác mở hồ sơ bằng chuột hoặc phím Enter trên bảng. */
    private void ganThaoTacBang() {
        tblNhanVien.addMouseListener(new MouseAdapter() {

                /** Mở chi tiết của dòng được nhấp đúp hoặc chọn ở cột thao tác. */
                @Override
                public void mouseClicked(MouseEvent event) {
                    int dong = tblNhanVien.rowAtPoint(event.getPoint());
                    int cot = tblNhanVien.columnAtPoint(event.getPoint());
                    if (dong >= 0 && (cot == 5 || event.getClickCount() == 2)) {
                        moDongDaChon();
                    }
                }
            });
        tblNhanVien.addMouseMotionListener(new MouseAdapter() {

                /** Đổi con trỏ khi di chuyển lên ô mở chi tiết. */
                @Override
                public void mouseMoved(MouseEvent event) {
                    boolean laNut = tblNhanVien.rowAtPoint(event.getPoint()) >= 0
                    && tblNhanVien.columnAtPoint(event.getPoint()) == 5;
                    tblNhanVien.setCursor(Cursor.getPredefinedCursor(laNut ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                }
            });
        tblNhanVien.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "xemHoSo");
        tblNhanVien.getActionMap().put("xemHoSo", new AbstractAction() {
                private static final long serialVersionUID = 1L;

                /** Thực hiện thao tác tương ứng với nút, bộ lọc hoặc phím vừa chọn. */
                @Override
                public void actionPerformed(ActionEvent event) { moDongDaChon(); }
            });
    }

    /** Đọc lại hồ sơ nhân viên theo dòng được chọn rồi mở form. */
    private void moDongDaChon() {
        int dong = tblNhanVien.getSelectedRow();
        if (dong >= 0) {
            String ma = String.valueOf(model.getValueAt(tblNhanVien.convertRowIndexToModel(dong), 0));
            moHoSo(nhanVienDAO.timTheoMa(taiKhoan, ma));
        }
    }

    /** Mở biểu mẫu xem chi tiết hoặc thêm mới và giữ điều kiện tìm kiếm của danh sách. */
    private void moHoSo(NhanVien nhanVien) {
        pnlHoSo = new NhanVienFormPanel(this, nhanVien);
        add(pnlHoSo, "hoSo");
        ((CardLayout) getLayout()).show(this, "hoSo");
    }

    /** Đóng form hiện tại, đọc lại bảng và giữ các điều kiện lọc. */
    void moDanhSach() {
        ((CardLayout) getLayout()).show(this, "danhSach");
        if (pnlHoSo != null) {
            remove(pnlHoSo);
            pnlHoSo = null;
        }
        taiDanhSach();
    }

    /** Lấy mã dự kiến từ DAO để hiển thị khi thêm mới. */
    String layMaMoi() {
        return nhanVienDAO.maTiepTheo(taiKhoan);
    }

    /** Thêm hoặc cập nhật hồ sơ qua DAO, tải lại bảng và thông báo kết quả. */
    void luuNhanVien(NhanVien nhanVien, boolean themMoi) {
        if (themMoi) {
            nhanVienDAO.them(taiKhoan, nhanVien);
        } else {
            nhanVienDAO.capNhat(taiKhoan, nhanVien);
        }
        moDanhSach();
        chonDong(nhanVien.getMaNhanVien());
        lblMoTa.setText("Đã lưu hồ sơ " + nhanVien.getMaNhanVien() + "."
            + (tblNhanVien.getSelectedRow() < 0 ? " Hồ sơ nằm ngoài điều kiện lọc hiện tại." : ""));
        lblMoTa.setForeground(new Color(0x18794E));
        lblMoTa.setVisible(true);
        revalidate();
    }

    /** Kiểm tra dữ liệu chưa lưu và xác nhận trước khi rời màn hình. */
    public boolean choPhepRoiTrang() {
        if (pnlHoSo != null && !pnlHoSo.choPhepRoiTrang()) {
            return false;
        }
        moDanhSach();
        return true;
    }

    /** Chọn và cuộn đến bản ghi theo mã nếu còn nằm trong danh sách lọc. */
    private void chonDong(String ma) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals(ma)) {
                int dong = tblNhanVien.convertRowIndexToView(i);
                tblNhanVien.setRowSelectionInterval(dong, dong);
                tblNhanVien.scrollRectToVisible(tblNhanVien.getCellRect(dong, 0, true));
                return;
            }
        }
    }

    /** Nhóm chữ số điện thoại để danh sách dễ đọc hơn. */
    private String dinhDangDienThoai(String so) {
        return so.length() == 10 ? so.substring(0, 4) + " " + so.substring(4, 7) + " " + so.substring(7) : so;
    }

    /** Thực hiện thao tác tương ứng với nút, bộ lọc hoặc phím vừa chọn. */
    @Override public void actionPerformed(ActionEvent event) {
        String lenh = event.getActionCommand();
        if ("them".equals(lenh)) {
            moHoSo(null);
        } else if ("trangThai".equals(lenh)) {
            taiDanhSach();
        } else if ("sapXep".equals(lenh)) {
            taiDanhSach();
        } else if ("lamMoi".equals(lenh)) {
            txtTim.setText(""); cmbTrangThai.setSelectedIndex(0);
            cmbSapXep.setSelectedIndex(0); taiDanhSach();
        }
    }
}
