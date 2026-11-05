package ui.screen.quanly;

import dao.DichVuDAO;
import entity.DichVu;
import entity.TaiKhoan;

import java.awt.BorderLayout;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import ui.component.UiChung;
import ui.component.UiTheme;

/** Hiển thị danh sách dịch vụ và điều hướng biểu mẫu bảng giá. */
public final class DichVuPanel extends JPanel implements DocumentListener, ActionListener {

    private static final long serialVersionUID = 1L;

    private final DichVuDAO dao;
    private final TaiKhoan taiKhoan;

    // Bảng dữ liệu và mô hình các dòng hiển thị.
    private final DefaultTableModel model = new DefaultTableModel(
            new String[] { "Mã DV", "Tên dịch vụ", "Nhóm", "Đơn giá (đ)", "Trạng thái", "Thao tác" }, 0);
    private final JTable tblDichVu = new JTable(model);

    // Số bản ghi đang hiển thị và phản hồi sau thao tác danh sách.
    private final JLabel lblSoLuong = UiChung.taoNhan("", 11, false, UiTheme.APP_MUTED);
    private final JLabel lblHienThi = UiChung.taoNhan("", 11, false, UiTheme.APP_MUTED);

    // Các vùng nội dung và biểu mẫu con của màn hình.
    private final JPanel pnlDanhSach = new JPanel(new BorderLayout(0, 14));

    // Ô tìm kiếm và các bộ lọc danh sách.
    private final JTextField txtTim = UiChung.taoTimKiem("Tìm mã DV, tên hoặc nhóm dịch vụ");
    private final JComboBox<String> cmbSapXep = new JComboBox<>(
            new String[] { "Mã tăng dần", "Tên A → Z", "Tên Z → A", "Giá tăng dần", "Giá giảm dần" });
    private final JComboBox<String> cmbTrangThai = new JComboBox<>(
            new String[] { "Tất cả trạng thái", "Đang áp dụng", "Tạm dừng" });
    private final JLabel lblKetQua = UiChung.taoNhan("", 12, false, new Color(0x18794E));

    private DichVuFormPanel pnlHoSo;

    /** Tạo danh sách dịch vụ, bộ lọc, sắp xếp và thao tác mở biểu mẫu. */
    public DichVuPanel(TaiKhoan taiKhoan) {
        this(taiKhoan, new DichVuDAO());
    }

    /** Mở danh sách với nguồn dịch vụ và báo giá chung của ứng dụng. */
    public DichVuPanel(TaiKhoan taiKhoan, DichVuDAO dao) {
        super(new BorderLayout(0, 14));
        this.dao = dao;
        this.taiKhoan = taiKhoan;
        List<DichVu> danhSach = dao.layDanhSach(taiKhoan);
        setOpaque(false);
        JButton btnThem = UiChung.taoNut("+ Thêm dịch vụ", true);
        btnThem.setActionCommand("them");
        btnThem.addActionListener(this);
        pnlDanhSach.setOpaque(false);
        JPanel pnlTren = new JPanel(new BorderLayout(0, 14));
        pnlTren.setOpaque(false);
        pnlTren.add(UiChung.taoDauTrang("Quản lý / Danh mục / Dịch vụ và bảng giá", "Dịch vụ và bảng giá",
                "Quản lý nhóm dịch vụ, đơn giá và điều kiện áp dụng."), BorderLayout.NORTH);
        pnlTren.add(taoThanhCongCu());
        lblKetQua.setVisible(false);
        JPanel pnlTacVu = new JPanel(new BorderLayout(0, 8));
        pnlTacVu.setOpaque(false);
        pnlTacVu.add(UiChung.taoHangNut(btnThem), BorderLayout.NORTH);
        pnlTacVu.add(lblKetQua);
        pnlTren.add(pnlTacVu, BorderLayout.SOUTH);
        pnlDanhSach.add(pnlTren, BorderLayout.NORTH);
        JPanel pnlBang = taoDanhSach();
        pnlDanhSach.add(pnlBang);
        add(pnlDanhSach);
        hienDanhSach(danhSach);
        ganSuKienBang();
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
        txtTim.getDocument().addDocumentListener(this);
        cmbTrangThai.setActionCommand("trangThai");
        cmbTrangThai.addActionListener(this);
        cmbSapXep.setActionCommand("sapXep");
        cmbSapXep.addActionListener(this);
        JButton btnLamMoi = UiChung.taoNut("Làm mới", false);
        btnLamMoi.setActionCommand("lamMoi");
        btnLamMoi.addActionListener(this);

        return UiChung.taoBoLoc(txtTim, btnLamMoi, cmbTrangThai);
    }

    /** Tạo thẻ danh sách gồm tiêu đề, bảng, sắp xếp và số kết quả. */
    private JPanel taoDanhSach() {
        taoBang();
        return UiChung.theBang("Danh sách dịch vụ và bảng giá", tblDichVu, lblSoLuong, lblHienThi, cmbSapXep);
    }

    /** Tạo bảng chỉ đọc, phủ chiều rộng và không cho kéo thay đổi cột. */
    private void taoBang() {
        UiChung.dinhDangBang(tblDichVu, 90, 270, 145, 140, 135, 130);
    }

    /** Gắn thao tác mở chi tiết bằng chuột hoặc phím Enter trên bảng. */
    private void ganSuKienBang() {
        tblDichVu.addMouseListener(new MouseAdapter() {
            /** Mở chi tiết của dòng được nhấp đúp hoặc chọn ở cột thao tác. */
            @Override
            public void mouseClicked(MouseEvent event) {
                int dong = tblDichVu.rowAtPoint(event.getPoint());
                if (dong >= 0 && (tblDichVu.columnAtPoint(event.getPoint()) == 5 || event.getClickCount() == 2)) {
                    moChiTietDaChon();
                }
            }
        });
        tblDichVu.addMouseMotionListener(new MouseAdapter() {
            /** Đổi con trỏ khi di chuyển lên ô mở chi tiết. */
            @Override
            public void mouseMoved(MouseEvent event) {
                boolean tacVu = tblDichVu.rowAtPoint(event.getPoint()) >= 0
                        && tblDichVu.columnAtPoint(event.getPoint()) == 5;
                tblDichVu.setCursor(Cursor.getPredefinedCursor(tacVu ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });
        tblDichVu.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "chiTiet");
        tblDichVu.getActionMap().put("chiTiet", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            /** Thực hiện thao tác tương ứng với nút, bộ lọc hoặc phím vừa chọn. */
            @Override
            public void actionPerformed(ActionEvent event) {
                moChiTietDaChon();
            }
        });
    }

    /** Mở biểu mẫu dịch vụ trên dòng đang được chọn. */
    private void moChiTietDaChon() {
        int dong = tblDichVu.getSelectedRow();
        if (dong >= 0) {
            moHoSo(String.valueOf(tblDichVu.getValueAt(dong, 0)));
        }
    }

    /** Mở biểu mẫu xem chi tiết hoặc thêm mới và giữ điều kiện tìm kiếm của danh sách. */
    private void moHoSo(String ma) {
        try {
            DichVu dv = ma == null ? null : dao.timTheoMa(taiKhoan, ma);
            pnlHoSo = new DichVuFormPanel(this, dv);
            removeAll();
            add(pnlHoSo);
            revalidate();
            repaint();
        } catch (IllegalArgumentException | SecurityException ex) {
            baoLoi(ex);
        }
    }

    /** Lấy mã dự kiến từ DAO để hiển thị khi thêm mới. */
    String layMaMoi() {
        return dao.maTiepTheo(taiKhoan);
    }

    /** Thêm hoặc sửa dịch vụ qua DAO rồi chọn lại dòng và thông báo kết quả. */
    void luu(DichVu banDau, DichVu moi) {
        if (banDau == null) {
            dao.them(taiKhoan, moi);
        } else {
            dao.capNhat(taiKhoan, banDau, moi);
        }
        moDanhSach();
        boolean thayDong = false;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (moi.getMaDichVu().equals(model.getValueAt(i, 0))) {
                tblDichVu.setRowSelectionInterval(i, i);
                tblDichVu.scrollRectToVisible(tblDichVu.getCellRect(i, 0, true));
                thayDong = true;

                break;
            }
        }
        hienKetQua("Đã lưu dịch vụ " + moi.getMaDichVu() + "."
                + (thayDong ? "" : " Dịch vụ nằm ngoài điều kiện lọc hiện tại."));
    }

    /** Xác nhận xóa dịch vụ và gọi DAO kiểm tra điều kiện trước khi thực hiện. */
    void xoa(DichVu dichVu) {
        dao.xoa(taiKhoan, dichVu);
        moDanhSach();
        hienKetQua("Đã xóa dịch vụ " + dichVu.getMaDichVu() + ".");
    }

    /** Đóng form hiện tại, đọc lại bảng và giữ các điều kiện lọc. */
    void moDanhSach() {
        pnlHoSo = null;
        removeAll();
        add(pnlDanhSach);
        locDanhSach();
        revalidate();
        repaint();
    }

    /** Kiểm tra dữ liệu chưa lưu và xác nhận trước khi rời màn hình. */
    public boolean choPhepRoiTrang() {
        if (pnlHoSo == null) {
            return true;
        }
        if (!pnlHoSo.choPhepRoiTrang()) {
            return false;
        }
        moDanhSach();
        return true;
    }

    /** Hiển thị kết quả lưu, giữ điều kiện lọc và chọn lại dòng liên quan. */
    private void hienKetQua(String chu) {
        lblKetQua.setText(chu);
        lblKetQua.setToolTipText(chu);
        lblKetQua.setVisible(true);
    }

    /** Xóa từ khóa, đặt lại bộ lọc và tải lại danh sách. */
    private void lamMoi() {
        txtTim.setText("");
        cmbTrangThai.setSelectedIndex(0);
        cmbSapXep.setSelectedIndex(0);
        locDanhSach();
    }

    /** Lọc dữ liệu qua DAO rồi cập nhật bảng và số kết quả. */
    private void locDanhSach() {
        try {
            int trangThai = cmbTrangThai.getSelectedIndex() == 0 ? -1 : cmbTrangThai.getSelectedIndex() == 1 ? 1 : 0;
            hienDanhSach(dao.timKiem(taiKhoan, txtTim.getText(), trangThai, cmbSapXep.getSelectedIndex()));
            lblKetQua.setVisible(false);
        } catch (IllegalArgumentException | SecurityException ex) {
            baoLoi(ex);
        }
    }

    /** Mở thông báo lỗi khi đọc dữ liệu hoặc thực hiện thao tác. */
    private void baoLoi(RuntimeException ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Dịch vụ và bảng giá", JOptionPane.WARNING_MESSAGE);
    }

    /** Cập nhật kết quả tìm kiếm khi nội dung ô tìm thay đổi. */
    @Override
    public void insertUpdate(DocumentEvent event) {
        locDanhSach();
    }

    /** Cập nhật kết quả tìm kiếm khi nội dung ô tìm thay đổi. */
    @Override
    public void removeUpdate(DocumentEvent event) {
        locDanhSach();
    }

    /** Cập nhật kết quả tìm kiếm khi nội dung ô tìm thay đổi. */
    @Override
    public void changedUpdate(DocumentEvent event) {
        locDanhSach();
    }

    /** Đưa danh sách đã đọc vào bảng và cập nhật số bản ghi hiển thị. */
    private void hienDanhSach(List<DichVu> danhSach) {
        model.setRowCount(0);
        for (DichVu dv : danhSach) {
            model.addRow(new Object[] { dv.getMaDichVu(), dv.getTenDichVu(), dv.getNhomDichVu(), dv.getDonGia(),
                    dv.isDangApDung() ? "Áp dụng" : "Tạm dừng", "Xem chi tiết" });
        }
        lblSoLuong.setText(danhSach.size() + " kết quả đang hiển thị");
        lblHienThi.setText("Hiển thị " + danhSach.size() + " / " + dao.layDanhSach(taiKhoan).size() + " hồ sơ");
        if (danhSach.isEmpty()) {
            lblHienThi.setText(lblHienThi.getText() + " · Không có kết quả phù hợp.");
        }
    }

    /** Thực hiện thao tác tương ứng với nút, bộ lọc hoặc phím vừa chọn. */
    @Override
    public void actionPerformed(ActionEvent event) {
        String lenh = event.getActionCommand();
        if ("them".equals(lenh)) {
            moHoSo(null);
        } else if ("trangThai".equals(lenh)) {
            locDanhSach();
        } else if ("sapXep".equals(lenh)) {
            locDanhSach();
        } else if ("lamMoi".equals(lenh)) {
            lamMoi();
        }
    }
}
