package ui.screen.quanly;

import dao.PhuTungDAO;
import entity.TaiKhoan;
import entity.PhuTung;

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
import javax.swing.table.DefaultTableModel;

import ui.component.UiChung;
import ui.component.UiTheme;

/** Hiển thị danh mục phụ tùng và điều hướng nhập kho, đối soát, nhật ký. */
public final class PhuTungPanel extends JPanel implements DocumentListener, ActionListener {
    private static final long serialVersionUID = 1L;
    private final TaiKhoan taiKhoan;
    private final PhuTungDAO dao;

    // Các vùng nội dung và biểu mẫu con của màn hình.
    private final JPanel pnlDanhSach = new JPanel(new BorderLayout(0, 14));

    // Ô tìm kiếm và các bộ lọc danh sách.
    private final JTextField txtTim = UiChung.taoTimKiem("Tìm mã PT, tên hoặc loại phụ tùng...");
    private final JComboBox<String> cmbTon = new JComboBox<>(
            new String[] { "Tất cả tình trạng tồn", "Đủ tồn", "Sắp hết", "Hết hàng", "Ngừng hoạt động" });
    private final JComboBox<String> cmbSapXep = new JComboBox<>(
            new String[] { "Mã tăng dần", "Tên A → Z", "Tên Z → A", "Tồn tăng dần", "Tồn giảm dần" });
    // Bảng dữ liệu và mô hình các dòng hiển thị.
    private final DefaultTableModel model = new DefaultTableModel(new String[] { "Mã PT", "Tên phụ tùng", "Đơn vị",
            "Tồn hiện tại", "Tồn tối thiểu", "Trạng thái", "Thao tác" }, 0);
    private final JTable tblPhuTung = UiChung.taoBang(model, 130, 360, 130, 180, 180, 190, 170);

    // Số bản ghi đang hiển thị và phản hồi sau thao tác danh sách.
    private final JLabel lblSoLuong = UiChung.taoNhan("", 11, false, UiTheme.APP_MUTED);
    private final JLabel lblCuoi = UiChung.taoNhan("", 11, false, UiTheme.APP_MUTED);
    private final JLabel lblKetQua = UiChung.taoNhan("", 12, false, new Color(0x18794E));
    private PhuTungFormPanel pnlHoSo;
    private DoiSoatKhoPanel pnlDoiSoat;
    private NhapKhoPanel pnlNhapKho;

    /** Tạo danh sách phụ tùng, bộ lọc và các nút nhập kho, đối soát, nhật ký. */
    public PhuTungPanel(TaiKhoan taiKhoan, PhuTungDAO dao) {
        super(new BorderLayout());
        this.taiKhoan = taiKhoan;
        this.dao = dao;
        dao.layDanhSach(taiKhoan);
        setOpaque(false);
        pnlDanhSach.setOpaque(false);
        JButton btnThem = UiChung.taoNut("+ Thêm phụ tùng", true);
        btnThem.setActionCommand("them");
        btnThem.addActionListener(this);
        JButton btnDoiSoat = UiChung.taoNut("Đối soát tồn kho", false);
        btnDoiSoat.setActionCommand("doiSoat");
        btnDoiSoat.addActionListener(this);
        JButton btnNhatKy = UiChung.taoNut("Nhật ký nhập – xuất – tồn", false);
        btnNhatKy.setActionCommand("nhatKy");
        btnNhatKy.addActionListener(this);
        JButton btnNhap = UiChung.taoNut("Nhập kho", false);
        btnNhap.setActionCommand("nhap");
        btnNhap.addActionListener(this);
        JPanel pnlTren = new JPanel(new BorderLayout(0, 14));
        pnlTren.setOpaque(false);
        pnlTren.add(UiChung.taoDauTrang("Quản lý / Danh mục / Phụ tùng", "Phụ tùng",
                "Tra cứu tồn kho, nhập hàng và kiểm tra ngưỡng cảnh báo."), BorderLayout.NORTH);
        JButton btnMoi = UiChung.taoNut("Làm mới", false);
        btnMoi.setActionCommand("moi");
        btnMoi.addActionListener(this);
        JPanel pnlCongCu = UiChung.taoBoLoc(txtTim, btnMoi, cmbTon);
        pnlTren.add(pnlCongCu);
        lblKetQua.setVisible(false);
        JPanel pnlTacVu = new JPanel(new BorderLayout(0, 8));
        pnlTacVu.setOpaque(false);
        pnlTacVu.add(UiChung.taoHangNut(btnThem, btnNhap, btnDoiSoat, btnNhatKy), BorderLayout.NORTH);
        pnlTacVu.add(lblKetQua);
        pnlTren.add(pnlTacVu, BorderLayout.SOUTH);
        pnlDanhSach.add(pnlTren, BorderLayout.NORTH);
        pnlDanhSach.add(UiChung.theBang("Danh sách phụ tùng", tblPhuTung, lblSoLuong, lblCuoi, cmbSapXep));
        add(pnlDanhSach);
        txtTim.getDocument().addDocumentListener(this);
        cmbTon.setActionCommand("ton");
        cmbTon.addActionListener(this);
        cmbSapXep.setActionCommand("sapXep");
        cmbSapXep.addActionListener(this);
        tblPhuTung.addMouseListener(new MouseAdapter() {
            /** Mở chi tiết của dòng được nhấp đúp hoặc chọn ở cột thao tác. */
            @Override
            public void mouseClicked(MouseEvent event) {
                if (tblPhuTung.rowAtPoint(event.getPoint()) >= 0
                        && (event.getClickCount() == 2 || tblPhuTung.columnAtPoint(event.getPoint()) == 6)) {
                    moDaChon();
                }
            }
        });
        tblPhuTung.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "chiTiet");
        tblPhuTung.getActionMap().put("chiTiet", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            /** Thực hiện thao tác tương ứng với nút, bộ lọc hoặc phím vừa chọn. */
            @Override
            public void actionPerformed(ActionEvent event) {
                moDaChon();
            }
        });
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

    /** Lấy mã phụ tùng trên dòng bảng đang được chọn. */
    private String maDaChon() {
        int dong = tblPhuTung.getSelectedRow();
        return dong < 0 ? null : String.valueOf(tblPhuTung.getValueAt(dong, 0));
    }

    /** Mở biểu mẫu phụ tùng trên dòng đang được chọn. */
    private void moDaChon() {
        String ma = maDaChon();
        if (ma != null) {
            moHoSo(ma);
        }
    }

    /** Mở biểu mẫu xem chi tiết hoặc thêm mới và giữ điều kiện tìm kiếm của danh sách. */
    private void moHoSo(String ma) {
        try {
            pnlHoSo = new PhuTungFormPanel(this, ma == null ? null : dao.timTheoMa(taiKhoan, ma));
            hien(pnlHoSo);
        } catch (IllegalArgumentException | SecurityException ex) {
            baoLoi(ex);
        }
    }

    /** Đọc thông tin và tồn hiện tại rồi mở form kiểm kê phụ tùng. */
    void moDoiSoat(String ma) {
        try {
            pnlDoiSoat = new DoiSoatKhoPanel(this, taiKhoan, dao, ma);
            pnlHoSo = null;
            hien(pnlDoiSoat);
        } catch (IllegalArgumentException | SecurityException ex) {
            baoLoi(ex);
        }
    }

    /** Mở lịch sử biến động toàn kho hoặc của phụ tùng được chỉ định. */
    void moNhatKy(String ma) {
        pnlHoSo = null;
        pnlDoiSoat = null;
        pnlNhapKho = null;
        hien(new NhatKyKhoPanel(taiKhoan, dao, ma, this));
    }

    /** Mở form nhập mới hoặc chi tiết chỉ đọc của lần nhập đã ghi nhận. */
    void moNhapKho(String ma) {
        try {
            pnlHoSo = null;
            pnlDoiSoat = null;
            pnlNhapKho = new NhapKhoPanel(this, taiKhoan, dao,
                    ma == null ? null : dao.getNhapKhoDAO().timTheoMa(taiKhoan, ma));
            hien(pnlNhapKho);
        } catch (IllegalArgumentException | SecurityException ex) {
            baoLoi(ex);
        }
    }

    /** Thêm hoặc sửa danh mục qua DAO rồi tải lại bảng và thông báo kết quả. */
    void luu(PhuTung cu, PhuTung moi) {
        if (cu == null) {
            dao.them(taiKhoan, moi);
        } else {
            dao.capNhat(taiKhoan, cu, moi);
        }
        moDanhSach();
        boolean thay = false;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (moi.getMaPhuTung().equals(model.getValueAt(i, 0))) {
                tblPhuTung.setRowSelectionInterval(i, i);
                thay = true;

                break;
            }
        }
        thongBao("Đã lưu phụ tùng " + moi.getMaPhuTung() + "."
                + (thay ? "" : " Phụ tùng nằm ngoài điều kiện lọc hiện tại."));
    }

    /** Đóng form hiện tại, đọc lại bảng và giữ các điều kiện lọc. */
    void moDanhSach() {
        pnlHoSo = null;
        pnlDoiSoat = null;
        pnlNhapKho = null;
        taiDanhSach();
        hien(pnlDanhSach);
    }

    /** Hiển thị phản hồi lưu hoặc cập nhật kho trên màn danh sách. */
    void thongBao(String chu) {
        lblKetQua.setText(chu);
        lblKetQua.setToolTipText(chu);
        lblKetQua.setVisible(true);
    }

    /** Thay panel đang hiển thị trong phân hệ và cập nhật bố cục. */
    private void hien(JPanel pnl) {
        removeAll();
        add(pnl);
        revalidate();
        repaint();
    }

    /** Kiểm tra dữ liệu chưa lưu và xác nhận trước khi rời màn hình. */
    public boolean choPhepRoiTrang() {
        if (pnlHoSo != null && !pnlHoSo.choPhepRoiTrang()) {
            return false;
        }
        if (pnlDoiSoat != null && !pnlDoiSoat.choPhepRoiTrang()) {
            return false;
        }
        if (pnlNhapKho != null && !pnlNhapKho.choPhepRoiTrang()) {
            return false;
        }
        moDanhSach();
        return true;
    }

    /** Đọc lại danh sách và tồn mới mà không đóng form đang mở. */
    public void capNhatDanhSach() {
        taiDanhSach();
    }

    /** Đọc dữ liệu theo điều kiện tìm, lọc, sắp xếp rồi cập nhật bảng và số kết quả. */
    private void taiDanhSach() {
        try {
            int chon = cmbTon.getSelectedIndex();
            String tinhTrang = chon == 0 || chon == 4 ? null : String.valueOf(cmbTon.getSelectedItem());
            int hoatDong = chon == 0 ? -1 : chon == 4 ? 0 : 1;
            List<PhuTung> cacPhuTung = dao.timKiem(taiKhoan, txtTim.getText(), tinhTrang, hoatDong,
                    cmbSapXep.getSelectedIndex());
            model.setRowCount(0);
            for (PhuTung pt : cacPhuTung) {
                model.addRow(new Object[] { pt.getMaPhuTung(), pt.getTenPhuTung(), pt.getDonViTinh(),
                        dao.layTon(taiKhoan, pt.getMaPhuTung()), pt.getMucToiThieu(),
                        pt.isHoatDong() ? dao.tinhTrangTon(taiKhoan, pt) : "Ngừng hoạt động", "Xem chi tiết" });
            }
            lblSoLuong.setText(cacPhuTung.size() + " kết quả đang hiển thị");
            lblCuoi.setText("Hiển thị " + cacPhuTung.size() + " / " + dao.layDanhSach(taiKhoan).size() + " hồ sơ"
                    + (cacPhuTung.isEmpty() ? " · Không có phụ tùng phù hợp." : ""));
            lblKetQua.setVisible(false);
        } catch (IllegalArgumentException | SecurityException ex) {
            baoLoi(ex);
        }
    }

    /** Mở thông báo lỗi khi đọc dữ liệu hoặc thực hiện thao tác. */
    private void baoLoi(RuntimeException ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Phụ tùng", JOptionPane.WARNING_MESSAGE);
    }

    /** Cập nhật kết quả tìm kiếm khi nội dung ô tìm thay đổi. */
    @Override
    public void insertUpdate(DocumentEvent event) {
        taiDanhSach();
    }

    /** Cập nhật kết quả tìm kiếm khi nội dung ô tìm thay đổi. */
    @Override
    public void removeUpdate(DocumentEvent event) {
        taiDanhSach();
    }

    /** Cập nhật kết quả tìm kiếm khi nội dung ô tìm thay đổi. */
    @Override
    public void changedUpdate(DocumentEvent event) {
        taiDanhSach();
    }

    /** Thực hiện thao tác tương ứng với nút, bộ lọc hoặc phím vừa chọn. */
    @Override
    public void actionPerformed(ActionEvent event) {
        String lenh = event.getActionCommand();
        if ("them".equals(lenh)) {
            moHoSo(null);
        } else if ("doiSoat".equals(lenh)) {
            String ma = maDaChon();
            if (ma == null) {
                JOptionPane.showMessageDialog(this, "Chọn phụ tùng cần đối soát trong bảng.");
            } else {
                moDoiSoat(ma);
            }
        } else if ("nhatKy".equals(lenh)) {
            moNhatKy(null);
        } else if ("nhap".equals(lenh)) {
            moNhapKho(null);
        } else if ("moi".equals(lenh)) {
            txtTim.setText("");
            cmbTon.setSelectedIndex(0);
            cmbSapXep.setSelectedIndex(0);
            taiDanhSach();
        } else if ("ton".equals(lenh)) {
            taiDanhSach();
        } else if ("sapXep".equals(lenh)) {
            taiDanhSach();
        }
    }
}
