package ui.screen.quanly;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

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
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import dao.DichVuDAO;
import entity.DichVu;
import entity.TaiKhoan;
import ui.component.QuanLyUi;
import ui.component.UiTheme;

/** Danh sách và điều hướng form dịch vụ theo Page 3; nghiệp vụ lưu nằm trong DAO. */
public final class DichVuPanel extends JPanel implements DocumentListener {

    private static final long serialVersionUID = 1L;
    /** Nguồn dữ liệu và danh tính được DAO kiểm tra ở mỗi thao tác. */
    private final DichVuDAO dao = new DichVuDAO();
    private final TaiKhoan taiKhoan;
    /** Bảng, bộ lọc và số lượng trên màn danh sách. */
    private final DefaultTableModel model = new DefaultTableModel(
            new String[] {"Mã DV", "Tên dịch vụ", "Nhóm", "Đơn giá (đ)", "Trạng thái", "Thao tác"}, 0);
    private final JTable tblDichVu = new JTable(model);
    private final JLabel lblSoLuong = QuanLyUi.taoNhan("", 11, false, UiTheme.APP_MUTED);
    private final JLabel lblHienThi = QuanLyUi.taoNhan("", 11, false, UiTheme.APP_MUTED);
    private final JLabel lblMoTa = QuanLyUi.taoNhan("", 12, false, UiTheme.APP_MUTED);
    private final JPanel pnlDanhSach = new JPanel(new BorderLayout(0, 14));
    private final JTextField txtTim = new JTextField() {
        private static final long serialVersionUID = 1L;
        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (getText().isEmpty() && !hasFocus()) {
                Graphics g = graphics.create();
                Insets le = getInsets();
                g.setColor(UiTheme.APP_MUTED);
                g.setFont(getFont());
                g.clipRect(le.left, 0, Math.max(0, getWidth() - le.left - le.right), getHeight());
                g.drawString("Tìm mã DV, tên hoặc nhóm dịch vụ...", le.left,
                        (getHeight() - g.getFontMetrics().getHeight()) / 2 + g.getFontMetrics().getAscent());
                g.dispose();
            }
        }
    };
    private final JComboBox<String> cmbTrangThai = new JComboBox<>(
            new String[] {"Tất cả trạng thái", "Đang áp dụng", "Tạm dừng"});
    private final JLabel lblKetQua = QuanLyUi.taoNhan("", 12, false, new Color(0x18794E));
    /** Form hiện tại; null khi đang xem danh sách. */
    private DichVuFormPanel pnlHoSo;
    private JPanel pnlBang;

    /** @param taiKhoan Quản lý đã đăng nhập; DAO kiểm tra quyền trước khi mở danh sách */
    public DichVuPanel(TaiKhoan taiKhoan) {
        super(new BorderLayout(0, 14));
        this.taiKhoan = taiKhoan;
        List<DichVu> danhSach = dao.layDanhSach(taiKhoan);
        setOpaque(false);
        JButton btnThem = QuanLyUi.taoNut("+ Thêm dịch vụ", true);
        btnThem.addActionListener(event -> moHoSo(null));
        pnlDanhSach.setOpaque(false);
        JPanel pnlTren = new JPanel(new BorderLayout(0, 14));
        pnlTren.setOpaque(false);
        pnlTren.add(QuanLyUi.taoDauTrang("Quản lý / Danh mục / Dịch vụ và bảng giá",
                "Dịch vụ và bảng giá", "Quản lý nhóm dịch vụ, đơn giá và điều kiện áp dụng.", btnThem),
                BorderLayout.NORTH);
        pnlTren.add(taoThanhCongCu());
        lblKetQua.setVisible(false);
        pnlTren.add(lblKetQua, BorderLayout.SOUTH);
        pnlDanhSach.add(pnlTren, BorderLayout.NORTH);
        pnlBang = taoDanhSach();
        JPanel pnlGiua = new JPanel();
        pnlGiua.setOpaque(false);
        pnlGiua.setLayout(new BoxLayout(pnlGiua, BoxLayout.Y_AXIS));
        pnlGiua.add(pnlBang);
        pnlDanhSach.add(pnlGiua);
        add(pnlDanhSach);
        hienDanhSach(danhSach);
        txtTim.getDocument().addDocumentListener(this);
        cmbTrangThai.addActionListener(event -> locDanhSach());
        ganSuKienBang();
    }

    /** Giữ chiều rộng tối đa như các màn Quản lý khác. */
    @Override public void doLayout() {
        int le = Math.max(0, (getWidth() - 1588) / 2);
        if (getInsets().left != le) { setBorder(BorderFactory.createEmptyBorder(0, le, 0, le)); }
        super.doLayout();
    }

    /** Tìm kiếm và trạng thái kết hợp; Làm mới xóa các điều kiện lọc. */
    private JPanel taoThanhCongCu() {
        JPanel pnl = QuanLyUi.taoThe(null);
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.X_AXIS));
        pnl.setBorder(BorderFactory.createCompoundBorder(pnl.getBorder(),
                BorderFactory.createEmptyBorder(15, 14, 15, 14)));
        QuanLyUi.dinhDangO(txtTim);
        txtTim.setToolTipText("Tìm theo mã, tên hoặc nhóm dịch vụ; có thể nhập không dấu.");
        txtTim.getAccessibleContext().setAccessibleName("Tìm dịch vụ");
        txtTim.setFont(UiTheme.APP_FONT.deriveFont(12.0f));
        txtTim.setPreferredSize(new Dimension(380, 36));
        txtTim.setMinimumSize(new Dimension(120, 36));
        txtTim.setMaximumSize(new Dimension(380, 36));
        QuanLyUi.dinhDangO(cmbTrangThai);
        cmbTrangThai.getAccessibleContext().setAccessibleName("Lọc trạng thái dịch vụ");
        cmbTrangThai.setFont(UiTheme.APP_FONT.deriveFont(12.0f));
        cmbTrangThai.setPreferredSize(new Dimension(170, 36));
        cmbTrangThai.setMinimumSize(new Dimension(150, 36));
        cmbTrangThai.setMaximumSize(new Dimension(170, 36));
        JButton btnLamMoi = QuanLyUi.taoNut("Làm mới", false);
        btnLamMoi.addActionListener(event -> lamMoi());
        pnl.add(txtTim);
        pnl.add(Box.createHorizontalStrut(8));
        pnl.add(cmbTrangThai);
        pnl.add(Box.createHorizontalStrut(8));
        pnl.add(btnLamMoi);
        pnl.add(Box.createHorizontalGlue());
        return pnl;
    }

    /** Thẻ danh sách gồm tiêu đề, bảng và số lượng. */
    private JPanel taoDanhSach() {
        JPanel pnl = QuanLyUi.taoThe(new BorderLayout());
        JPanel pnlTen = new JPanel(new BorderLayout(0, 4));
        pnlTen.setOpaque(false);
        pnlTen.add(QuanLyUi.taoNhan("Danh sách dịch vụ và bảng giá", 15, true,
                UiTheme.APP_TEXT), BorderLayout.NORTH);
        pnlTen.add(lblMoTa);
        JPanel pnlDau = new JPanel(new BorderLayout(10, 0));
        pnlDau.setOpaque(false);
        pnlDau.setBorder(BorderFactory.createEmptyBorder(14, 17, 14, 17));
        pnlDau.add(pnlTen);
        pnlDau.add(lblSoLuong, BorderLayout.EAST);
        pnl.add(pnlDau, BorderLayout.NORTH);
        pnl.add(taoBang());
        lblHienThi.setBorder(BorderFactory.createEmptyBorder(13, 14, 13, 14));
        pnl.add(lblHienThi, BorderLayout.SOUTH);
        return pnl;
    }

    /** Bảng phủ chiều rộng, chỉ đọc và không kéo thay đổi cột. */
    private JScrollPane taoBang() {
        tblDichVu.setDefaultEditor(Object.class, null);
        tblDichVu.setRowHeight(52);
        tblDichVu.setShowGrid(false);
        tblDichVu.setIntercellSpacing(new Dimension(0, 0));
        tblDichVu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblDichVu.setSelectionBackground(new Color(0xEEF4FA));
        tblDichVu.setFillsViewportHeight(true);
        tblDichVu.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblDichVu.setDefaultRenderer(Object.class, new ChuRenderer());
        tblDichVu.getTableHeader().setDefaultRenderer(new ChuRenderer());
        tblDichVu.getTableHeader().setPreferredSize(new Dimension(0, 39));
        tblDichVu.getTableHeader().setResizingAllowed(false);
        tblDichVu.getTableHeader().setReorderingAllowed(false);
        int[] rong = {175, 376, 220, 260, 240, 316};
        int[] toiThieu = {60, 160, 85, 100, 85, 100};
        for (int i = 0; i < rong.length; i++) {
            tblDichVu.getColumnModel().getColumn(i).setPreferredWidth(rong[i]);
            tblDichVu.getColumnModel().getColumn(i).setMinWidth(toiThieu[i]);
            tblDichVu.getColumnModel().getColumn(i).setResizable(false);
        }
        tblDichVu.getColumnModel().getColumn(4).setCellRenderer(new TrangThaiRenderer());
        JScrollPane scr = new JScrollPane(tblDichVu);
        scr.setColumnHeaderView(tblDichVu.getTableHeader());
        scr.setBorder(BorderFactory.createEmptyBorder());
        scr.getViewport().setBackground(Color.WHITE);
        scr.getVerticalScrollBar().setUnitIncrement(52);
        return scr;
    }

    /** Mở bằng nhấp vào tác vụ, nhấp đúp dòng hoặc Enter. */
    private void ganSuKienBang() {
        tblDichVu.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent event) {
                int dong = tblDichVu.rowAtPoint(event.getPoint());
                if (dong >= 0 && (tblDichVu.columnAtPoint(event.getPoint()) == 5 || event.getClickCount() == 2)) {
                    moChiTietDaChon();
                }
            }
        });
        tblDichVu.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent event) {
                boolean tacVu = tblDichVu.rowAtPoint(event.getPoint()) >= 0
                        && tblDichVu.columnAtPoint(event.getPoint()) == 5;
                tblDichVu.setCursor(Cursor.getPredefinedCursor(tacVu ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });
        tblDichVu.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "chiTiet");
        tblDichVu.getActionMap().put("chiTiet", new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override public void actionPerformed(ActionEvent event) { moChiTietDaChon(); }
        });
    }

    /** Mở dịch vụ của dòng được chọn; không làm gì khi bảng chưa chọn dòng. */
    private void moChiTietDaChon() {
        int dong = tblDichVu.getSelectedRow();
        if (dong >= 0) { moHoSo(String.valueOf(tblDichVu.getValueAt(dong, 0))); }
    }

    /** @param ma null để thêm mới, mã hiện tại để xem chi tiết */
    private void moHoSo(String ma) {
        try {
            DichVu dv = ma == null ? null : dao.timTheoMa(taiKhoan, ma);
            pnlHoSo = new DichVuFormPanel(this, dv);
            removeAll();
            add(pnlHoSo);
            revalidate();
            repaint();
        } catch (IllegalArgumentException | SecurityException ex) { baoLoi(ex); }
    }

    /** @return mã xem trước trên form thêm */
    String layMaMoi() { return dao.maTiepTheo(taiKhoan); }

    /** DAO lưu xong mới rời form; lỗi được form hiển thị, giữ nguyên dữ liệu nhập. */
    void luu(DichVu banDau, DichVu moi) {
        if (banDau == null) { dao.them(taiKhoan, moi); }
        else { dao.capNhat(taiKhoan, banDau, moi); }
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

    /** DAO kiểm tra lại tham chiếu tại thời điểm xóa. */
    void xoa(DichVu dichVu) {
        dao.xoa(taiKhoan, dichVu);
        moDanhSach();
        hienKetQua("Đã xóa dịch vụ " + dichVu.getMaDichVu() + ".");
    }

    /** Quay về danh sách, giữ các điều kiện tìm/lọc. */
    void moDanhSach() {
        pnlHoSo = null;
        removeAll();
        add(pnlDanhSach);
        locDanhSach();
        revalidate();
        repaint();
    }

    /** @return cho phép điều hướng, đăng xuất hoặc đóng khi đã xử lý thay đổi chưa lưu */
    public boolean choPhepRoiTrang() {
        if (pnlHoSo == null) { return true; }
        if (!pnlHoSo.choPhepRoiTrang()) { return false; }
        moDanhSach();
        return true;
    }

    /** Hiện phản hồi sau khi thao tác thành công. */
    private void hienKetQua(String chu) {
        lblKetQua.setText(chu);
        lblKetQua.setToolTipText(chu);
        lblKetQua.setVisible(true);
    }

    /** Xóa điều kiện tìm/lọc và đọc lại danh mục. */
    private void lamMoi() {
        txtTim.setText("");
        cmbTrangThai.setSelectedIndex(0);
        locDanhSach();
    }

    /** Chỉ thay bảng sau khi DAO đọc thành công. */
    private void locDanhSach() {
        try {
            Boolean trangThai = cmbTrangThai.getSelectedIndex() == 0 ? null : cmbTrangThai.getSelectedIndex() == 1;
            hienDanhSach(dao.timKiem(taiKhoan, txtTim.getText(), trangThai));
            lblKetQua.setVisible(false);
        } catch (IllegalArgumentException | SecurityException ex) { baoLoi(ex); }
    }

    /** Báo lỗi tải dữ liệu hoặc mở chi tiết. */
    private void baoLoi(RuntimeException ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Dịch vụ và bảng giá", JOptionPane.WARNING_MESSAGE);
    }

    @Override public void insertUpdate(DocumentEvent event) { locDanhSach(); }
    @Override public void removeUpdate(DocumentEvent event) { locDanhSach(); }
    @Override public void changedUpdate(DocumentEvent event) { locDanhSach(); }

    /** @param danhSach dữ liệu đã được DAO kiểm tra quyền, có thể rỗng */
    private void hienDanhSach(List<DichVu> danhSach) {
        model.setRowCount(0);
        for (DichVu dv : danhSach) {
            model.addRow(new Object[] {dv.getMaDichVu(), dv.getTenDichVu(), dv.getNhomDichVu(),
                    dv.getDonGia(), dv.isDangApDung() ? "Áp dụng" : "Tạm dừng", "Xem chi tiết"});
        }
        lblSoLuong.setText(danhSach.size() + " kết quả đang hiển thị");
        lblHienThi.setText("Hiển thị " + danhSach.size() + " / " + dao.layDanhSach(taiKhoan).size() + " hồ sơ");
        lblMoTa.setText(danhSach.isEmpty() ? "Không có dịch vụ phù hợp. Hãy thử làm mới bộ lọc."
                : "Chọn Xem chi tiết hoặc nhấp đúp vào dòng dịch vụ.");
        int soDong = Math.max(1, Math.min(8, danhSach.size()));
        tblDichVu.setPreferredScrollableViewportSize(new Dimension(0, soDong * 52));
        pnlBang.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70 + 39 + soDong * 52 + 44));
        pnlBang.revalidate();
    }

    /** Chữ bảng và định dạng tiền Việt Nam; giá giữ nguyên độ chính xác. */
    private static final class ChuRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;
        @Override public Component getTableCellRendererComponent(JTable bang, Object giaTri, boolean chon,
                boolean focus, int dong, int cot) {
            super.getTableCellRendererComponent(bang, giaTri, chon, focus, dong, cot);
            if (giaTri instanceof BigDecimal gia) {
                NumberFormat tien = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
                tien.setMaximumFractionDigits(Math.max(0, gia.scale()));
                setText(tien.format(gia));
            }
            setHorizontalAlignment(cot == 3 ? SwingConstants.RIGHT : SwingConstants.LEFT);
            setFont((dong < 0 || cot == 0 ? UiTheme.APP_BOLD_FONT : UiTheme.APP_FONT).deriveFont(12.0f));
            setForeground(dong < 0 ? UiTheme.APP_MUTED : cot == 5 ? UiTheme.APP_PRIMARY : UiTheme.APP_TEXT);
            setBackground(dong < 0 ? new Color(0xF7F9FC) : chon ? bang.getSelectionBackground() : Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE9EDF2)),
                    BorderFactory.createEmptyBorder(0, 13, 0, 13)));
            setToolTipText(getText());
            return this;
        }
    }

    /** Nhãn trạng thái theo Figma, luôn kèm chữ để phân biệt áp dụng/tạm dừng. */
    private static final class TrangThaiRenderer implements TableCellRenderer {
        private final JPanel pnlHang = new JPanel(new FlowLayout(FlowLayout.LEFT, 13, 14));
        private final JPanel pnlTag = UiTheme.roundedPanel(new BorderLayout(), new Color(0xEAF3FF), 5);
        private final JLabel lblTrangThai = QuanLyUi.taoNhan("", 11, true, new Color(0x24619D));
        private TrangThaiRenderer() {
            pnlTag.setBorder(BorderFactory.createEmptyBorder(4, 7, 4, 7));
            pnlTag.add(lblTrangThai);
            pnlHang.add(pnlTag);
            pnlHang.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE9EDF2)));
        }
        @Override public Component getTableCellRendererComponent(JTable bang, Object value, boolean chon,
                boolean focus, int dong, int cot) {
            boolean tamDung = "Tạm dừng".equals(value);
            lblTrangThai.setText(String.valueOf(value));
            lblTrangThai.setForeground(new Color(tamDung ? 0xB54708 : 0x24619D));
            pnlTag.setBackground(new Color(tamDung ? 0xFFF2E5 : 0xEAF3FF));
            pnlHang.setBackground(chon ? bang.getSelectionBackground() : Color.WHITE);
            pnlHang.setToolTipText(lblTrangThai.getText());
            return pnlHang;
        }
    }
}
