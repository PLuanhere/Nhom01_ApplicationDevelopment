package ui.screen.quanly;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import dao.NhanVienDAO;
import dao.TaiKhoanDAO;
import entity.NhanVien;
import entity.TaiKhoan;
import entity.VaiTro;
import ui.component.QuanLyUi;
import ui.component.UiTheme;

/** Danh sách, cấp tài khoản và mở các tác vụ bảo mật trong layout Quản lý. */
public final class TaiKhoanPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private final TaiKhoanDAO dao = new TaiKhoanDAO();
    private final TaiKhoan nguoiDung;
    private final JPanel pnlDanhSach = new JPanel(new BorderLayout(0, 14));
    private final JTextField txtTim = new JTextField() {
        private static final long serialVersionUID = 1L;

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (getText().isEmpty() && !hasFocus()) {
                Graphics g = graphics.create();
                Insets le = getInsets();
                g.setFont(getFont());
                g.setColor(UiTheme.APP_MUTED);
                g.clipRect(le.left, 0, Math.max(0, getWidth() - le.left - le.right), getHeight());
                g.drawString("Tìm mã TK, tài khoản, nhân viên...", le.left,
                        (getHeight() - g.getFontMetrics().getHeight()) / 2 + g.getFontMetrics().getAscent());
                g.dispose();
            }
        }
    };
    private final JComboBox<String> cmbTrangThai = new JComboBox<>(
            new String[] {"Tất cả trạng thái", "Đang hoạt động", "Đã khóa"});
    private final JComboBox<Object> cmbVaiTro = new JComboBox<>(new Object[] {"Tất cả vai trò",
            VaiTro.QUAN_LY, VaiTro.NHAN_VIEN_TIEP_NHAN, VaiTro.NHAN_VIEN_KY_THUAT, VaiTro.THU_NGAN});
    private final DefaultTableModel model = new DefaultTableModel(
            new String[] {"Mã TK", "Tên đăng nhập", "Nhân viên", "Vai trò được cấp", "Trạng thái", "Thao tác"}, 0);
    private final JTable tblTaiKhoan = new JTable(model);
    private final JButton btnBaoMat = QuanLyUi.taoNut("Khóa, mở & đặt lại mật khẩu", false);
    private final JLabel lblKetQua = QuanLyUi.taoNhan("", 12, false, new Color(0x18794E));
    private final JLabel lblSoLuong = QuanLyUi.taoNhan("", 11, false, UiTheme.APP_MUTED);
    private final JLabel lblHienThi = QuanLyUi.taoNhan("", 11, false, UiTheme.APP_MUTED);
    private final JLabel lblMoTa = QuanLyUi.taoNhan(
            "Chọn một dòng để xem, lọc hoặc mở tác vụ liên quan.", 12, false, UiTheme.APP_MUTED);
    private TaiKhoanFormPanel pnlHoSo;
    private BaoMatTaiKhoanPanel pnlBaoMat;
    private JPanel pnlTheDanhSach;

    /** @param nguoiDung Quản lý đã đăng nhập; DAO kiểm tra quyền khi đọc dữ liệu */
    public TaiKhoanPanel(TaiKhoan nguoiDung) {
        super(new BorderLayout());
        this.nguoiDung = nguoiDung;
        setOpaque(false);
        JButton btnCap = QuanLyUi.taoNut("+ Cấp tài khoản", true);
        btnCap.addActionListener(event -> moHoSo(null));
        btnBaoMat.setEnabled(false);
        btnBaoMat.setToolTipText("Chọn một tài khoản trong bảng để mở tác vụ bảo mật.");
        btnBaoMat.addActionListener(event -> moBaoMatDaChon());
        JPanel pnlTren = new JPanel(new BorderLayout(0, 14));
        pnlTren.setOpaque(false);
        pnlTren.add(QuanLyUi.taoDauTrang("Quản lý / Hệ thống / Tài khoản & phân quyền",
                "Tài khoản & phân quyền",
                "Một nhân viên dùng tài khoản riêng và chỉ sử dụng chức năng đúng vai trò."),
                BorderLayout.NORTH);
        pnlTren.add(taoThanhCongCu());
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlNut.setOpaque(false);
        pnlNut.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
        pnlNut.add(btnCap);
        pnlNut.add(btnBaoMat);
        JPanel pnlHanhDong = new JPanel(new BorderLayout(0, 8));
        pnlHanhDong.setOpaque(false);
        pnlHanhDong.add(pnlNut, BorderLayout.NORTH);
        lblKetQua.setVisible(false);
        lblKetQua.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));
        pnlHanhDong.add(lblKetQua);
        pnlTren.add(pnlHanhDong, BorderLayout.SOUTH);
        pnlDanhSach.setOpaque(false);
        pnlDanhSach.add(pnlTren, BorderLayout.NORTH);
        JPanel pnlGiua = new JPanel();
        pnlGiua.setOpaque(false);
        pnlGiua.setLayout(new BoxLayout(pnlGiua, BoxLayout.Y_AXIS));
        pnlTheDanhSach = taoDanhSach();
        pnlGiua.add(pnlTheDanhSach);
        pnlDanhSach.add(pnlGiua);
        add(pnlDanhSach);
        taiDanhSach();
    }

    /** Giữ cùng chiều rộng tối đa với các màn hình Quản lý Page 3. */
    @Override
    public void doLayout() {
        int le = Math.max(0, (getWidth() - 1588) / 2);
        if (getInsets().left != le) { setBorder(BorderFactory.createEmptyBorder(0, le, 0, le)); }
        super.doLayout();
    }

    /** Tìm tức thì, kết hợp trạng thái/vai trò và làm mới cả ba điều kiện. */
    private JPanel taoThanhCongCu() {
        JPanel pnl = QuanLyUi.taoThe(null);
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.X_AXIS));
        pnl.setBorder(BorderFactory.createCompoundBorder(pnl.getBorder(),
                BorderFactory.createEmptyBorder(15, 14, 15, 14)));
        QuanLyUi.dinhDangO(txtTim);
        txtTim.setFont(UiTheme.APP_FONT.deriveFont(12.0f));
        txtTim.setMaximumSize(new Dimension(380, 36));
        txtTim.setMinimumSize(new Dimension(120, 36));
        txtTim.setPreferredSize(new Dimension(380, 36));
        txtTim.getAccessibleContext().setAccessibleName("Tìm tài khoản hoặc nhân viên");
        txtTim.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { taiDanhSach(); }
            @Override public void removeUpdate(DocumentEvent e) { taiDanhSach(); }
            @Override public void changedUpdate(DocumentEvent e) { taiDanhSach(); }
        });
        QuanLyUi.dinhDangO(cmbTrangThai);
        cmbTrangThai.setFont(UiTheme.APP_FONT.deriveFont(12.0f));
        cmbTrangThai.setPreferredSize(new Dimension(170, 36));
        cmbTrangThai.setMaximumSize(new Dimension(170, 36));
        cmbTrangThai.setMinimumSize(new Dimension(150, 36));
        cmbTrangThai.getAccessibleContext().setAccessibleName("Lọc trạng thái tài khoản");
        cmbTrangThai.addActionListener(event -> taiDanhSach());
        QuanLyUi.dinhDangO(cmbVaiTro);
        cmbVaiTro.setFont(UiTheme.APP_FONT.deriveFont(12.0f));
        cmbVaiTro.setPreferredSize(new Dimension(190, 36));
        cmbVaiTro.setMaximumSize(new Dimension(190, 36));
        cmbVaiTro.setMinimumSize(new Dimension(180, 36));
        cmbVaiTro.getAccessibleContext().setAccessibleName("Lọc vai trò tài khoản");
        cmbVaiTro.addActionListener(event -> taiDanhSach());
        JButton btnLamMoi = QuanLyUi.taoNut("Làm mới", false);
        btnLamMoi.addActionListener(event -> {
            txtTim.setText("");
            cmbTrangThai.setSelectedIndex(0);
            cmbVaiTro.setSelectedIndex(0);
            taiDanhSach();
        });
        pnl.add(txtTim);
        pnl.add(Box.createHorizontalStrut(8));
        pnl.add(cmbTrangThai);
        pnl.add(Box.createHorizontalStrut(8));
        pnl.add(cmbVaiTro);
        pnl.add(Box.createHorizontalStrut(8));
        pnl.add(btnLamMoi);
        pnl.add(Box.createHorizontalGlue());
        return pnl;
    }

    /** Thẻ danh sách gồm tiêu đề, bảng và số kết quả. */
    private JPanel taoDanhSach() {
        JPanel pnl = QuanLyUi.taoThe(new BorderLayout());
        JPanel pnlTen = new JPanel(new BorderLayout(0, 4));
        pnlTen.setOpaque(false);
        pnlTen.add(QuanLyUi.taoNhan("Danh sách tài khoản & phân quyền", 15, true,
                UiTheme.APP_TEXT), BorderLayout.NORTH);
        pnlTen.add(lblMoTa);
        JPanel pnlDau = new JPanel(new BorderLayout(10, 0));
        pnlDau.setOpaque(false);
        pnlDau.setBorder(BorderFactory.createEmptyBorder(14, 17, 14, 17));
        pnlDau.add(pnlTen);
        pnlDau.add(lblSoLuong, BorderLayout.EAST);
        pnl.add(pnlDau, BorderLayout.NORTH);
        pnl.add(taoBang());
        lblHienThi.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        pnl.add(lblHienThi, BorderLayout.SOUTH);
        return pnl;
    }

    /** Bảng sáu cột chỉ đọc, phủ chiều rộng và khóa kéo cột. */
    private JScrollPane taoBang() {
        tblTaiKhoan.setDefaultEditor(Object.class, null);
        tblTaiKhoan.setRowHeight(52);
        tblTaiKhoan.setShowGrid(false);
        tblTaiKhoan.setIntercellSpacing(new Dimension(0, 0));
        tblTaiKhoan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblTaiKhoan.setSelectionBackground(new Color(0xEEF4FA));
        tblTaiKhoan.setFillsViewportHeight(true);
        tblTaiKhoan.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblTaiKhoan.setPreferredScrollableViewportSize(new Dimension(0, 208));
        tblTaiKhoan.setDefaultRenderer(Object.class, new ChuRenderer());
        tblTaiKhoan.getColumnModel().getColumn(4).setCellRenderer(new TrangThaiRenderer());
        tblTaiKhoan.getTableHeader().setDefaultRenderer(new ChuRenderer());
        tblTaiKhoan.getTableHeader().setPreferredSize(new Dimension(0, 39));
        tblTaiKhoan.getTableHeader().setResizingAllowed(false);
        tblTaiKhoan.getTableHeader().setReorderingAllowed(false);
        int[] rong = {157, 275, 293, 257, 318, 286};
        int[] toiThieu = {55, 95, 115, 115, 120, 100};
        for (int i = 0; i < rong.length; i++) {
            tblTaiKhoan.getColumnModel().getColumn(i).setPreferredWidth(rong[i]);
            tblTaiKhoan.getColumnModel().getColumn(i).setMinWidth(toiThieu[i]);
            tblTaiKhoan.getColumnModel().getColumn(i).setResizable(false);
        }
        ganSuKienBang();
        JScrollPane scr = new JScrollPane(tblTaiKhoan);
        scr.setBorder(BorderFactory.createEmptyBorder());
        scr.getViewport().setBackground(Color.WHITE);
        return scr;
    }

    /** Mở đúng mã tài khoản bằng nút chữ, nhấp đôi hoặc Enter. */
    private void ganSuKienBang() {
        tblTaiKhoan.getSelectionModel().addListSelectionListener(event ->
                btnBaoMat.setEnabled(tblTaiKhoan.getSelectedRow() >= 0));
        tblTaiKhoan.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (tblTaiKhoan.rowAtPoint(event.getPoint()) >= 0
                        && (tblTaiKhoan.columnAtPoint(event.getPoint()) == 5 || event.getClickCount() == 2)) {
                    TaiKhoan tk = layDongChon();
                    if (tk != null) { moHoSo(tk); }
                }
            }
        });
        tblTaiKhoan.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "chiTiet");
        tblTaiKhoan.getActionMap().put("chiTiet", new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override public void actionPerformed(ActionEvent event) {
                TaiKhoan tk = layDongChon();
                if (tk != null) { moHoSo(tk); }
            }
        });
    }

    /** DAO lọc dữ liệu trước khi thay bảng và cập nhật số lượng. */
    private void taiDanhSach() {
        int dongChon = tblTaiKhoan.getSelectedRow();
        String maChon = dongChon < 0 ? null : String.valueOf(tblTaiKhoan.getValueAt(dongChon, 0));
        Boolean hoatDong = cmbTrangThai.getSelectedIndex() == 0 ? null : cmbTrangThai.getSelectedIndex() == 1;
        VaiTro vaiTro = cmbVaiTro.getSelectedItem() instanceof VaiTro v ? v : null;
        List<TaiKhoan> ketQua = dao.timKiem(nguoiDung, txtTim.getText(), hoatDong, vaiTro);
        model.setRowCount(0);
        for (TaiKhoan tk : ketQua) {
            NhanVien nv = new NhanVienDAO().timTheoMa(nguoiDung, tk.getMaNhanVien());
            model.addRow(new Object[] {tk.getMaTaiKhoan(), tk.getTenDangNhap(), nv.getHoTen(),
                    tk.getVaiTro().getTenHienThi(), TaiKhoanDAO.tenTrangThai(tk), "Xem chi tiết"});
        }
        lblSoLuong.setText(ketQua.size() + " kết quả đang hiển thị");
        lblHienThi.setText("Hiển thị " + ketQua.size() + " / " + dao.layDanhSach(nguoiDung).size() + " hồ sơ");
        lblMoTa.setText(ketQua.isEmpty() ? "Không tìm thấy tài khoản phù hợp."
                : "Chọn một dòng để xem, lọc hoặc mở tác vụ liên quan.");
        int soDong = Math.max(1, Math.min(8, ketQua.size()));
        tblTaiKhoan.setPreferredScrollableViewportSize(new Dimension(0, soDong * 52));
        pnlTheDanhSach.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70 + 39 + soDong * 52 + 44));
        pnlTheDanhSach.revalidate();
        chonDong(maChon);
        lblKetQua.setVisible(false);
    }

    /** @return true nếu tài khoản cần chọn nằm trong kết quả lọc */
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

    /** Hiện kết quả lưu và chọn đúng tài khoản, giữ nguyên điều kiện lọc của người dùng. */
    void hienKetQua(String ma, String thongBao) {
        moDanhSach();
        boolean timThay = chonDong(ma);
        lblKetQua.setText(thongBao + (timThay ? "" : " Tài khoản nằm ngoài bộ lọc hiện tại."));
        lblKetQua.setToolTipText(lblKetQua.getText());
        lblKetQua.setVisible(true);
        revalidate();
    }

    /** @return hồ sơ đọc lại theo mã, null nếu chưa chọn dòng */
    private TaiKhoan layDongChon() {
        int dong = tblTaiKhoan.getSelectedRow();
        return dong < 0 ? null : dao.timTheoMa(nguoiDung,
                String.valueOf(model.getValueAt(tblTaiKhoan.convertRowIndexToModel(dong), 0)));
    }

    /** Không tự chọn tài khoản đích thay người dùng. */
    private void moBaoMatDaChon() {
        TaiKhoan tk = layDongChon();
        if (tk == null) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một tài khoản trong danh sách.");
        } else {
            moBaoMat(tk);
        }
    }

    /** @param tk null để cấp mới, khác null để xem hồ sơ */
    void moHoSo(TaiKhoan tk) {
        pnlHoSo = new TaiKhoanFormPanel(this, nguoiDung, tk);
        hienTrang(pnlHoSo);
    }

    /** @param tk mã tài khoản đích cần đọc lại trước khi quản trị */
    void moBaoMat(TaiKhoan tk) {
        pnlHoSo = null;
        pnlBaoMat = new BaoMatTaiKhoanPanel(this, nguoiDung, dao.timTheoMa(nguoiDung, tk.getMaTaiKhoan()));
        hienTrang(pnlBaoMat);
    }

    /** Tải hồ sơ bảo mật mới nhất và phản hồi rõ sau khi lưu thành công. */
    void moBaoMat(TaiKhoan tk, String thongBao) {
        moBaoMat(tk);
        pnlBaoMat.hienThanhCong(thongBao);
    }

    /** Quay lại danh sách, giữ điều kiện lọc. Form tự xác nhận bỏ thay đổi trước khi gọi. */
    public void moDanhSach() {
        pnlHoSo = null;
        pnlBaoMat = null;
        taiDanhSach();
        hienTrang(pnlDanhSach);
    }

    /** Thay nội dung trong phân hệ, không tạo lại layout chung. */
    private void hienTrang(JPanel trang) {
        removeAll();
        add(trang);
        revalidate();
        repaint();
    }

    /** @return true nếu không đang lưu và người dùng đồng ý bỏ dữ liệu chưa lưu */
    public boolean choPhepRoiTrang() {
        if (pnlHoSo != null && !pnlHoSo.choPhepRoiTrang()) { return false; }
        if (pnlBaoMat != null && !pnlBaoMat.choPhepRoiTrang()) { return false; }
        moDanhSach();
        return true;
    }

    /** Chữ bảng theo Page 3; tooltip hiển thị đầy đủ khi cột hẹp. */
    private static final class ChuRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;
        @Override
        public Component getTableCellRendererComponent(JTable bang, Object giaTri, boolean chon,
                boolean focus, int dong, int cot) {
            super.getTableCellRendererComponent(bang, giaTri, chon, focus, dong, cot);
            setFont((dong < 0 || cot == 0 ? UiTheme.APP_BOLD_FONT : UiTheme.APP_FONT).deriveFont(12.0f));
            setForeground(dong < 0 ? UiTheme.APP_MUTED : cot == 5 ? UiTheme.APP_PRIMARY : UiTheme.APP_TEXT);
            setBackground(dong < 0 ? new Color(0xF7F9FC) : chon ? bang.getSelectionBackground() : Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE9EDF2)),
                    BorderFactory.createEmptyBorder(0, 13, 0, 13)));
            if (dong >= 0 && cot == 4) {
                setForeground("Đã khóa".equals(giaTri) ? new Color(0xB54708) : new Color(0x24619D));
            }
            setToolTipText(String.valueOf(giaTri));
            return this;
        }
    }

    /** Nhãn xanh/cam vẫn ghi rõ trạng thái, không dùng riêng màu để truyền đạt. */
    private static final class TrangThaiRenderer implements TableCellRenderer {
        private final JPanel hang = new JPanel(new FlowLayout(FlowLayout.LEFT, 13, 14));
        private final JPanel tag = UiTheme.roundedPanel(new BorderLayout(), new Color(0xEAF3FF), 5);
        private final JLabel chu = QuanLyUi.taoNhan("", 11, true, new Color(0x24619D));

        private TrangThaiRenderer() {
            tag.setBorder(BorderFactory.createEmptyBorder(4, 7, 4, 7));
            tag.add(chu);
            hang.add(tag);
            hang.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE9EDF2)));
        }

        @Override public Component getTableCellRendererComponent(JTable bang, Object giaTri, boolean chon,
                boolean focus, int dong, int cot) {
            boolean khoa = "Đã khóa".equals(giaTri);
            chu.setText(String.valueOf(giaTri));
            chu.setForeground(new Color(khoa ? 0xB54708 : 0x24619D));
            tag.setBackground(new Color(khoa ? 0xFFF2E5 : 0xEAF3FF));
            hang.setBackground(chon ? bang.getSelectionBackground() : Color.WHITE);
            hang.setToolTipText(chu.getText());
            return hang;
        }
    }
}
