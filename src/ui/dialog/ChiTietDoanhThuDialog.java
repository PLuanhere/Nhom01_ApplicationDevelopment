package ui.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import entity.HoaDonDoanhThu;
import entity.HoaDonDoanhThu.HinhThuc;
import ui.component.QuanLyUi;
import ui.component.UiTheme;

/** Dialog chỉ đọc của ngày được bấm trên biểu đồ; không mở chức năng sửa/thu tiền. */
public final class ChiTietDoanhThuDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    /** Ngày và danh sách được cấp từ DAO sau kiểm tra quyền. */
    private final LocalDate ngay;
    private final List<HoaDonDoanhThu> hoaDon;

    /**
     * Mở thông tin tổng và hóa đơn cùng ngày.
     * @param winChu khung sở hữu dialog
     * @param ngay ngày đã chọn
     * @param hoaDon các hóa đơn của chính ngày đó
     * @throws IllegalArgumentException nếu danh sách có hóa đơn của ngày khác
     */
    public ChiTietDoanhThuDialog(JFrame winChu, LocalDate ngay, List<HoaDonDoanhThu> hoaDon) {
        super(winChu, "Doanh thu ngày " + Objects.requireNonNull(ngay)
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), true);
        this.ngay = ngay;
        this.hoaDon = List.copyOf(hoaDon);
        if (this.hoaDon.stream().anyMatch(muc -> !muc.getNgayThanhToan().toLocalDate().equals(ngay))) {
            throw new IllegalArgumentException("Chi tiết có hóa đơn không thuộc ngày được chọn.");
        }
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(taoNoiDung());
        setMinimumSize(new Dimension(680, 360));
        setSize(900, 500);
        setLocationRelativeTo(winChu);
        getRootPane().registerKeyboardAction(event -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    /** @return bố cục có tổng tiền, bảng hóa đơn và nút đóng */
    private JPanel taoNoiDung() {
        JPanel pnl = new JPanel(new BorderLayout(0, 16));
        pnl.setBackground(UiTheme.APP_BACKGROUND);
        pnl.setBorder(BorderFactory.createEmptyBorder(20, 20, 16, 20));
        JPanel pnlDau = new JPanel(new BorderLayout(0, 12));
        pnlDau.setOpaque(false);
        String tieuDe = "Doanh thu ngày " + ngay.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        pnlDau.add(QuanLyUi.taoNhan(tieuDe, 22, true, UiTheme.APP_TEXT), BorderLayout.NORTH);
        JPanel pnlTong = new JPanel(new GridLayout(1, 3, 12, 0));
        pnlTong.setOpaque(false);
        pnlTong.add(taoTong("Tổng đã thu", tinhTong(null)));
        pnlTong.add(taoTong("Tiền mặt", tinhTong(HinhThuc.TIEN_MAT)));
        pnlTong.add(taoTong("Chuyển khoản", tinhTong(HinhThuc.CHUYEN_KHOAN)));
        pnlDau.add(pnlTong, BorderLayout.CENTER);
        pnlDau.add(QuanLyUi.taoNhan(hoaDon.isEmpty() ? "Không có hóa đơn đã thanh toán trong ngày."
                : hoaDon.size() + " hóa đơn · Tính theo thời điểm thanh toán", 12, false,
                UiTheme.APP_MUTED), BorderLayout.SOUTH);
        pnl.add(pnlDau, BorderLayout.NORTH);
        pnl.add(taoBang(), BorderLayout.CENTER);
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlNut.setOpaque(false);
        JButton btnDong = UiTheme.createAppButton("Đóng", false);
        btnDong.setPreferredSize(new Dimension(96, 36));
        btnDong.addActionListener(event -> dispose());
        pnlNut.add(btnDong);
        pnl.add(pnlNut, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnDong);
        return pnl;
    }

    /**
     * Cộng từ chính các dòng đang hiển thị.
     * @param hinhThuc null để lấy tất cả, hoặc phương thức cần đối chiếu
     * @return tiền chính xác
     */
    private BigDecimal tinhTong(HinhThuc hinhThuc) {
        return hoaDon.stream().filter(muc -> hinhThuc == null || muc.getHinhThuc() == hinhThuc)
                .map(HoaDonDoanhThu::getSoTien).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * @param tieuDe tên chỉ số
     * @param soTien tiền cần hiển thị
     * @return thẻ tiền chỉ đọc
     */
    private JPanel taoTong(String tieuDe, BigDecimal soTien) {
        JPanel pnl = QuanLyUi.taoThe(new BorderLayout(0, 6));
        pnl.setBorder(BorderFactory.createCompoundBorder(pnl.getBorder(),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        pnl.add(QuanLyUi.taoNhan(tieuDe, 12, false, UiTheme.APP_MUTED), BorderLayout.NORTH);
        pnl.add(QuanLyUi.taoNhan(QuanLyUi.dinhDangTien(soTien), 22, true, UiTheme.APP_TEXT));
        return pnl;
    }

    /** @return bảng chỉ đọc, sắp xếp tiền theo giá trị số và có thanh cuộn khi cần */
    private JScrollPane taoBang() {
        String[] cot = {"Mã hóa đơn", "Phiếu sửa chữa", "Giờ thu", "Khách / biển số", "Hình thức", "Số tiền"};
        DefaultTableModel model = new DefaultTableModel(cot, 0) {
            private static final long serialVersionUID = 1L;

            /** {@inheritDoc} */
            @Override
            public boolean isCellEditable(int dong, int cot) { return false; }

            /** {@inheritDoc} */
            @Override
            public Class<?> getColumnClass(int cot) { return cot == 5 ? BigDecimal.class : String.class; }
        };
        for (HoaDonDoanhThu muc : hoaDon) {
            model.addRow(new Object[] {muc.getMaHoaDon(), muc.getMaPhieuSuaChua(),
                    muc.getNgayThanhToan().format(DateTimeFormatter.ofPattern("HH:mm")),
                    muc.getKhachHang() + " · " + muc.getBienSo(), muc.getHinhThuc().getTenHienThi(), muc.getSoTien()});
        }
        JTable tbl = new JTable(model) {
            private static final long serialVersionUID = 1L;

            /** Hiện đủ nội dung ô khi cột hẹp, kể cả sau khi sắp xếp bảng. */
            @Override
            public String getToolTipText(MouseEvent event) {
                int dong = rowAtPoint(event.getPoint());
                int cot = columnAtPoint(event.getPoint());
                if (dong < 0 || cot < 0) {
                    return null;
                }
                Object giaTri = getValueAt(dong, cot);
                return giaTri instanceof BigDecimal ? QuanLyUi.dinhDangTien((BigDecimal) giaTri)
                        : String.valueOf(giaTri);
            }
        };
        tbl.setFont(UiTheme.APP_FONT.deriveFont(12.0f));
        tbl.setRowHeight(36);
        tbl.setAutoCreateRowSorter(true);
        tbl.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl.setFillsViewportHeight(true);
        tbl.setGridColor(UiTheme.APP_BORDER);
        tbl.getTableHeader().setFont(UiTheme.APP_BOLD_FONT.deriveFont(12.0f));
        tbl.getTableHeader().setReorderingAllowed(false);
        int[] rong = {135, 135, 65, 255, 120, 125};
        for (int viTri = 0; viTri < rong.length; viTri++) {
            tbl.getColumnModel().getColumn(viTri).setPreferredWidth(rong[viTri]);
        }
        tbl.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            /** {@inheritDoc} */
            @Override
            public Component getTableCellRendererComponent(JTable bang, Object giaTri, boolean chon,
                    boolean focus, int dong, int cot) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(bang,
                        QuanLyUi.dinhDangTien((BigDecimal) giaTri), chon, focus, dong, cot);
                lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 8));
                return lbl;
            }
        });
        return new JScrollPane(tbl);
    }
}
