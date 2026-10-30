package ui.screen;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import dao.NhanVienDAO;
import entity.NhanVien;
import entity.TaiKhoan;
import entity.TrangThaiNhanVien;
import ui.component.QuanLyUi;
import ui.component.UiTheme;

/** Phân hệ Nhân viên: danh sách tìm/lọc và form hồ sơ dùng chung trong layout Quản lý. */
public final class NhanVienPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    /** Màu đường phân cách theo Page 3. */
    private static final Color DUONG_KE = new Color(0xE9EDF2);
    /** Nguồn dữ liệu và danh tính dùng cho mỗi lần đọc danh sách. */
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final TaiKhoan taiKhoan;
    /** Giữ điều kiện tìm/lọc khi chuyển giữa danh sách và hồ sơ. */
    private final JTextField txtTim = new JTextField() {
        private static final long serialVersionUID = 1L;

        /** Vẽ gợi ý khi trống, không đưa gợi ý vào dữ liệu tìm kiếm. */
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (getText().isEmpty() && !hasFocus()) {
                Graphics g = graphics.create();
                Insets le = getInsets();
                g.setColor(UiTheme.APP_MUTED);
                g.setFont(getFont());
                g.clipRect(le.left, 0, getWidth() - le.left - le.right, getHeight());
                int y = (getHeight() - g.getFontMetrics().getHeight()) / 2 + g.getFontMetrics().getAscent();
                g.drawString("Tìm mã NV, họ tên, điện thoại...", le.left, y);
                g.dispose();
            }
        }
    };
    private final JComboBox<Object> cmbTrangThai = new JComboBox<>(new Object[] {
            "Tất cả trạng thái", TrangThaiNhanVien.DANG_LAM, TrangThaiNhanVien.DA_NGHI});
    private NhanVienFormPanel pnlHoSo;
    /** Bảng chỉ đọc; dữ liệu được đổ lại sau khi DAO cấp quyền. */
    private final DefaultTableModel model = new DefaultTableModel(
            new String[] {"Mã NV", "Họ và tên", "Số điện thoại", "Bộ phận", "Trạng thái", "Thao tác"}, 0);
    private final JTable tblNhanVien = new JTable(model);
    /** Các nhãn được cập nhật cùng dữ liệu bảng, kể cả khi danh sách rỗng. */
    private final JLabel lblMoTa = QuanLyUi.taoNhan("", 11.5f, false, UiTheme.APP_MUTED);
    private final JLabel lblSoLuong = QuanLyUi.taoNhan("", 10.5f, false, UiTheme.APP_MUTED);
    private final JLabel lblHienThi = QuanLyUi.taoNhan("", 10.5f, false, UiTheme.APP_MUTED);

    /**
     * Tạo màn hình và tải danh sách lần đầu; DAO kiểm tra quyền Quản lý.
     * @param taiKhoan tài khoản đã đăng nhập
     * @throws SecurityException nếu tài khoản không có quyền đọc nhân viên
     */
    public NhanVienPanel(TaiKhoan taiKhoan) {
        super(new CardLayout());
        this.taiKhoan = Objects.requireNonNull(taiKhoan);
        setBackground(UiTheme.APP_BACKGROUND);
        taiDanhSach();
        JPanel pnlTren = new JPanel(new BorderLayout(0, 14));
        pnlTren.setOpaque(false);
        pnlTren.add(taoPhanDau(), BorderLayout.NORTH);
        pnlTren.add(taoThanhCongCu());
        JPanel pnlDanhSachChung = new JPanel(new BorderLayout(0, 14));
        pnlDanhSachChung.setOpaque(false);
        pnlDanhSachChung.add(pnlTren, BorderLayout.NORTH);

        // Thẻ bám phía trên, thu nhỏ khi cửa sổ thấp; chỉ bảng có thanh cuộn.
        JPanel pnlGiua = new JPanel();
        pnlGiua.setLayout(new BoxLayout(pnlGiua, BoxLayout.Y_AXIS));
        pnlGiua.setOpaque(false);
        JPanel pnlDanhSach = taoDanhSach();
        pnlDanhSach.setMaximumSize(new Dimension(Integer.MAX_VALUE, pnlDanhSach.getPreferredSize().height));
        pnlGiua.add(pnlDanhSach);
        pnlDanhSachChung.add(pnlGiua);
        add(pnlDanhSachChung, "danhSach");
    }

    /** Giữ chiều rộng tối đa cùng trang tổng quan trong khung chung. */
    @Override
    public void doLayout() {
        int le = Math.max(0, (getWidth() - 1588) / 2);
        if (getInsets().left != le) {
            setBorder(BorderFactory.createEmptyBorder(0, le, 0, le));
        }
        super.doLayout();
    }

    /** Đọc thành công trước khi thay bảng; không xóa dữ liệu đang hiển thị nếu DAO từ chối. */
    private void taiDanhSach() {
        String maChon = tblNhanVien.getSelectedRow() < 0 ? null
                : String.valueOf(tblNhanVien.getValueAt(tblNhanVien.getSelectedRow(), 0));
        TrangThaiNhanVien trangThai = cmbTrangThai.getSelectedItem() instanceof TrangThaiNhanVien
                ? (TrangThaiNhanVien) cmbTrangThai.getSelectedItem() : null;
        List<NhanVien> ketQua = nhanVienDAO.timKiem(taiKhoan, txtTim.getText(), trangThai);
        hienDanhSach(ketQua, nhanVienDAO.layDanhSach(taiKhoan).size());
        chonDong(maChon);
    }

    /**
     * @param danhSach các hồ sơ đã lọc
     * @param tongSo tổng số hồ sơ trong nguồn dữ liệu
     */
    private void hienDanhSach(List<NhanVien> danhSach, int tongSo) {
        model.setRowCount(0);
        for (NhanVien nhanVien : danhSach) {
            model.addRow(new Object[] {nhanVien.getMaNhanVien(), nhanVien.getHoTen(),
                    dinhDangDienThoai(nhanVien.getSoDienThoai()), nhanVien.getBoPhan(),
                    nhanVien.getTrangThai(), "Xem chi tiết"});
        }
        lblSoLuong.setText(danhSach.size() + " kết quả đang hiển thị");
        lblHienThi.setText("Hiển thị " + danhSach.size() + " / " + tongSo + " hồ sơ");
        lblMoTa.setText(danhSach.isEmpty() ? "Không tìm thấy nhân viên phù hợp."
                : "Chọn một dòng để xem, lọc hoặc mở tác vụ liên quan.");
    }

    /** @return breadcrumb, tiêu đề và nút mở form thêm nhân viên */
    private JPanel taoPhanDau() {
        JPanel pnl = new JPanel(new BorderLayout(0, 12));
        pnl.setOpaque(false);
        pnl.add(QuanLyUi.taoNhan("Quản lý / Danh mục / Nhân viên", 11.5f, false,
                UiTheme.APP_MUTED), BorderLayout.NORTH);
        JPanel pnlTieuDe = new JPanel(new BorderLayout(0, 5));
        pnlTieuDe.setOpaque(false);
        pnlTieuDe.add(QuanLyUi.taoNhan("Nhân viên", 24, true, UiTheme.APP_TEXT), BorderLayout.NORTH);
        pnlTieuDe.add(QuanLyUi.taoNhan("Quản lý hồ sơ và tình trạng làm việc của nhân viên.",
                12, false, UiTheme.APP_MUTED), BorderLayout.SOUTH);
        JPanel pnlHang = new JPanel(new BorderLayout(12, 0));
        pnlHang.setOpaque(false);
        pnlHang.add(pnlTieuDe);
        JButton btnThem = UiTheme.createAppButton("+ Thêm nhân viên", true);
        btnThem.setPreferredSize(new Dimension(155, 36));
        btnThem.addActionListener(event -> moHoSo(null));
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlNut.setOpaque(false);
        pnlNut.add(btnThem);
        pnlHang.add(pnlNut, BorderLayout.EAST);
        pnl.add(pnlHang);
        return pnl;
    }

    /** @return tìm kiếm tức thì, lọc trạng thái và đặt lại điều kiện */
    private JPanel taoThanhCongCu() {
        JPanel pnl = QuanLyUi.taoThe(null);
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.X_AXIS));
        pnl.setBorder(BorderFactory.createCompoundBorder(pnl.getBorder(),
                BorderFactory.createEmptyBorder(15, 14, 15, 14)));
        txtTim.setFont(UiTheme.APP_FONT.deriveFont(12.0f));
        txtTim.setPreferredSize(new Dimension(380, 36));
        txtTim.setMinimumSize(new Dimension(180, 36));
        txtTim.setMaximumSize(new Dimension(380, 36));
        txtTim.setBorder(UiTheme.roundedBorder(UiTheme.APP_BORDER, 6, 0, 12));
        txtTim.setToolTipText("Tìm mã NV, họ tên, điện thoại...");
        txtTim.getAccessibleContext().setAccessibleName("Tìm mã NV, họ tên, điện thoại");
        txtTim.getDocument().addDocumentListener(new DocumentListener() {
            /** {@inheritDoc} */
            @Override
            public void insertUpdate(DocumentEvent event) { taiDanhSach(); }

            /** {@inheritDoc} */
            @Override
            public void removeUpdate(DocumentEvent event) { taiDanhSach(); }

            /** {@inheritDoc} */
            @Override
            public void changedUpdate(DocumentEvent event) { taiDanhSach(); }
        });
        cmbTrangThai.setFont(UiTheme.APP_FONT.deriveFont(12.0f));
        cmbTrangThai.setPreferredSize(new Dimension(170, 36));
        cmbTrangThai.setMinimumSize(new Dimension(170, 36));
        cmbTrangThai.setMaximumSize(new Dimension(170, 36));
        cmbTrangThai.getAccessibleContext().setAccessibleName("Lọc trạng thái làm việc");
        cmbTrangThai.addActionListener(event -> taiDanhSach());
        JButton btnLamMoi = UiTheme.createAppButton("Làm mới", false);
        btnLamMoi.setFont(UiTheme.APP_FONT.deriveFont(12.0f));
        btnLamMoi.setPreferredSize(new Dimension(81, 36));
        btnLamMoi.setMinimumSize(new Dimension(81, 36));
        btnLamMoi.setMaximumSize(new Dimension(81, 36));
        btnLamMoi.setToolTipText("Xóa điều kiện tìm/lọc và đọc lại danh sách.");
        btnLamMoi.addActionListener(event -> {
            txtTim.setText("");
            cmbTrangThai.setSelectedIndex(0);
            taiDanhSach();
        });
        pnl.add(txtTim);
        pnl.add(Box.createHorizontalStrut(8));
        pnl.add(cmbTrangThai);
        pnl.add(Box.createHorizontalStrut(8));
        pnl.add(btnLamMoi);
        pnl.add(Box.createHorizontalGlue());
        return pnl;
    }

    /** @return thẻ gồm đầu danh sách, bảng và số hồ sơ */
    private JPanel taoDanhSach() {
        JPanel pnl = QuanLyUi.taoThe(new BorderLayout());
        pnl.setBorder(BorderFactory.createCompoundBorder(pnl.getBorder(),
                BorderFactory.createEmptyBorder(1, 1, 1, 1)));
        JPanel pnlTen = new JPanel(new BorderLayout(0, 3));
        pnlTen.setOpaque(false);
        pnlTen.add(QuanLyUi.taoNhan("Danh sách nhân viên", 15, true, UiTheme.APP_TEXT), BorderLayout.NORTH);
        pnlTen.add(lblMoTa);
        JPanel pnlDau = new JPanel(new BorderLayout(12, 0));
        pnlDau.setOpaque(false);
        pnlDau.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, DUONG_KE),
                BorderFactory.createEmptyBorder(13, 17, 14, 17)));
        pnlDau.add(pnlTen);
        pnlDau.add(lblSoLuong, BorderLayout.EAST);
        pnl.add(pnlDau, BorderLayout.NORTH);
        pnl.add(taoBang());
        lblHienThi.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, DUONG_KE),
                BorderFactory.createEmptyBorder(13, 14, 13, 14)));
        pnl.add(lblHienThi, BorderLayout.SOUTH);
        return pnl;
    }

    /** @return bảng sáu cột chỉ đọc trong một vùng cuộn */
    private JScrollPane taoBang() {
        tblNhanVien.setDefaultEditor(Object.class, null);
        tblNhanVien.setFont(UiTheme.APP_FONT.deriveFont(12.0f));
        tblNhanVien.setRowHeight(52);
        tblNhanVien.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblNhanVien.setSelectionBackground(new Color(0xEEF4FA));
        tblNhanVien.setShowGrid(false);
        tblNhanVien.setIntercellSpacing(new Dimension(0, 0));
        tblNhanVien.setFillsViewportHeight(true);
        tblNhanVien.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblNhanVien.setPreferredScrollableViewportSize(new Dimension(0, 208));
        tblNhanVien.setDefaultRenderer(Object.class, new ChuRenderer());
        tblNhanVien.getTableHeader().setReorderingAllowed(false);
        tblNhanVien.getTableHeader().setResizingAllowed(false);
        tblNhanVien.getTableHeader().setPreferredSize(new Dimension(0, 39));
        tblNhanVien.getTableHeader().setDefaultRenderer(new ChuRenderer());
        int[] rong = {177, 328, 281, 223, 258, 320};
        int[] toiThieu = {70, 125, 110, 90, 100, 110};
        for (int viTri = 0; viTri < rong.length; viTri++) {
            tblNhanVien.getColumnModel().getColumn(viTri).setPreferredWidth(rong[viTri]);
            tblNhanVien.getColumnModel().getColumn(viTri).setMinWidth(toiThieu[viTri]);
            tblNhanVien.getColumnModel().getColumn(viTri).setResizable(false);
        }
        tblNhanVien.getColumnModel().getColumn(4).setCellRenderer(new TrangThaiRenderer());
        ganThaoTacBang();
        JScrollPane scrBang = new JScrollPane(tblNhanVien);
        scrBang.setColumnHeaderView(tblNhanVien.getTableHeader());
        scrBang.setBorder(BorderFactory.createEmptyBorder());
        scrBang.getViewport().setBackground(Color.WHITE);
        scrBang.getVerticalScrollBar().setUnitIncrement(52);
        return scrBang;
    }

    /** Mở bằng nút chữ, nhấp đôi dòng hoặc Enter; không cho sửa trực tiếp trên bảng. */
    private void ganThaoTacBang() {
        tblNhanVien.addMouseListener(new MouseAdapter() {
            /** {@inheritDoc} */
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
            /** {@inheritDoc} */
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

            /** {@inheritDoc} */
            @Override
            public void actionPerformed(ActionEvent event) { moDongDaChon(); }
        });
    }

    /** Đọc lại hồ sơ bằng mã; không dùng chỉ số dòng làm định danh nhân viên. */
    private void moDongDaChon() {
        int dong = tblNhanVien.getSelectedRow();
        if (dong >= 0) {
            String ma = String.valueOf(model.getValueAt(tblNhanVien.convertRowIndexToModel(dong), 0));
            moHoSo(nhanVienDAO.timTheoMa(taiKhoan, ma));
        }
    }

    /** @param nhanVien hồ sơ cần xem, null để mở form thêm */
    private void moHoSo(NhanVien nhanVien) {
        pnlHoSo = new NhanVienFormPanel(this, nhanVien);
        add(pnlHoSo, "hoSo");
        ((CardLayout) getLayout()).show(this, "hoSo");
    }

    /** Quay lại danh sách, giữ điều kiện tìm/lọc và bỏ form đã đóng. */
    void moDanhSach() {
        ((CardLayout) getLayout()).show(this, "danhSach");
        if (pnlHoSo != null) {
            remove(pnlHoSo);
            pnlHoSo = null;
        }
        taiDanhSach();
    }

    /** @return mã tự sinh để hiển thị trong form thêm, chưa lưu hồ sơ */
    String layMaMoi() {
        return nhanVienDAO.maTiepTheo(taiKhoan);
    }

    /**
     * Lưu qua DAO rồi cập nhật lại bảng; chỉ chạy sau khi form đã kiểm tra dữ liệu.
     * @param nhanVien hồ sơ cần lưu
     * @param themMoi true khi thêm, false khi sửa
     */
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
    }

    /** @return true nếu được rời phân hệ; MainFrame dùng trước điều hướng, đăng xuất và thoát */
    public boolean choPhepRoiTrang() {
        if (pnlHoSo != null && !pnlHoSo.choPhepRoiTrang()) {
            return false;
        }
        moDanhSach();
        return true;
    }

    /** @param ma mã cần chọn lại, null nếu không có lựa chọn */
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

    /**
     * @param so số lưu trong hồ sơ
     * @return số được nhóm như danh sách Figma
     */
    private String dinhDangDienThoai(String so) {
        return so.length() == 10 ? so.substring(0, 4) + " " + so.substring(4, 7) + " " + so.substring(7) : so;
    }

    /** Định dạng chữ cho tiêu đề cột và ô dữ liệu, kèm tooltip khi ô hẹp. */
    private static final class ChuRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        /** {@inheritDoc} */
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object giaTri, boolean chon,
                boolean focus, int dong, int cot) {
            super.getTableCellRendererComponent(tbl, giaTri, chon, focus, dong, cot);
            boolean tieuDe = dong < 0;
            if (tieuDe) {
                setFont(UiTheme.APP_BOLD_FONT.deriveFont(11.5f));
                setForeground(UiTheme.APP_MUTED);
                setBackground(new Color(0xF7F9FC));
            } else {
                setFont((cot == 0 ? UiTheme.APP_BOLD_FONT : UiTheme.APP_FONT).deriveFont(12.0f));
                setForeground(cot == 5 ? UiTheme.APP_PRIMARY : UiTheme.APP_TEXT);
                setBackground(chon ? tbl.getSelectionBackground() : Color.WHITE);
            }
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, tieuDe ? 0 : 1, 0, DUONG_KE),
                    BorderFactory.createEmptyBorder(0, 13, 0, 13)));
            setEnabled(true);
            setToolTipText(!tieuDe && cot == 5 ? "Mở hồ sơ; có thể chọn dòng rồi nhấn Enter."
                    : String.valueOf(giaTri));
            return this;
        }
    }

    /** Giữ nhãn trạng thái bo góc theo Figma, không chứa xử lý nghiệp vụ. */
    private static final class TrangThaiRenderer extends JPanel implements TableCellRenderer {
        private static final long serialVersionUID = 1L;
        /** Nhãn được dùng lại khi Swing vẽ các dòng. */
        private final JLabel lbl = QuanLyUi.taoNhan("", 11, true, new Color(0x24619D));
        private final JPanel pnlNhan = UiTheme.roundedPanel(new BorderLayout(), new Color(0xEAF3FF), 5);

        /** Bố trí nhãn cùng lề với nội dung bảng. */
        private TrangThaiRenderer() {
            super(new FlowLayout(FlowLayout.LEFT, 13, 14));
            lbl.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
            pnlNhan.add(lbl);
            add(pnlNhan);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, DUONG_KE));
        }

        /** {@inheritDoc} */
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object giaTri, boolean chon,
                boolean focus, int dong, int cot) {
            TrangThaiNhanVien trangThai = (TrangThaiNhanVien) giaTri;
            lbl.setText(trangThai.getTenNgan());
            boolean dangLam = trangThai == TrangThaiNhanVien.DANG_LAM;
            pnlNhan.setBackground(new Color(dangLam ? 0xEAF3FF : 0xFFF4DF));
            lbl.setForeground(new Color(dangLam ? 0x24619D : 0x8A5A12));
            setToolTipText(lbl.getText());
            setBackground(chon ? tbl.getSelectionBackground() : Color.WHITE);
            return this;
        }
    }
}
