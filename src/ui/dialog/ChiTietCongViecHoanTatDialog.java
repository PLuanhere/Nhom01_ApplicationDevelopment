package ui.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import javax.swing.AbstractAction;
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
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import entity.ThongKeNgayKyThuat;
import ui.component.KyThuatUi;
import ui.component.UiTheme;

/** Chi tiết thống kê của ngày đã chọn; chỉ đọc, không thay đổi dữ liệu sửa chữa. */
public final class ChiTietCongViecHoanTatDialog extends JDialog implements ActionListener {
    /** Phiên bản tương thích Swing và nút kết thúc xem chi tiết. */
    private static final long serialVersionUID = 1L;
    private final JButton btnDong = KyThuatUi.taoNut("Đóng chi tiết", 152);

    /**
     * Tạo dialog modal thuộc phiên đang mở; hỗ trợ Enter và Escape để đóng.
     * @param owner khung chung đang mở hộp thoại
     * @param tenKyThuatVien tên kỹ thuật viên
     * @param thongKe thống kê của ngày được chọn
     */
    public ChiTietCongViecHoanTatDialog(JFrame owner, String tenKyThuatVien, ThongKeNgayKyThuat thongKe) {
        super(owner, "Chi tiết công việc hoàn tất", true);
        setUndecorated(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(taoNoiDung(tenKyThuatVien, thongKe));
        getRootPane().setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));
        pack();
        Dimension kichThuocNoiDung = getSize();
        setSize(Math.max(900, kichThuocNoiDung.width), Math.max(540, kichThuocNoiDung.height));
        getRootPane().setDefaultButton(btnDong);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ESCAPE"), "dongChiTiet");
        getRootPane().getActionMap().put("dongChiTiet", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            /** {@inheritDoc} */
            @Override
            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        });
        setLocationRelativeTo(owner);
    }

    /** Đóng dialog bằng nút chính, không ghi dữ liệu hoặc đổi trạng thái công việc. */
    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == btnDong) {
            dispose();
        }
    }

    /**
     * Ghép tiêu đề, dữ liệu chỉ đọc và nút đóng theo thiết kế.
     * @param tenKyThuatVien tên kỹ thuật viên được thống kê
     * @param thongKe thống kê của ngày được chọn
     * @return panel nội dung hộp thoại
     */
    private JPanel taoNoiDung(String tenKyThuatVien, ThongKeNgayKyThuat thongKe) {
        Objects.requireNonNull(tenKyThuatVien, "Cần tên kỹ thuật viên.");
        Objects.requireNonNull(thongKe, "Cần thống kê theo ngày.");
        JPanel pnlNoiDung = new JPanel(new BorderLayout());
        pnlNoiDung.setBackground(UiTheme.WINDOW);
        JLabel lblTieuDe = KyThuatUi.taoNhan("Chi tiết công việc hoàn tất", UiTheme.HEADING_FONT);
        lblTieuDe.setOpaque(true);
        lblTieuDe.setBackground(UiTheme.SURFACE);
        lblTieuDe.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        lblTieuDe.setPreferredSize(new Dimension(0,
                Math.max(53, lblTieuDe.getFontMetrics(lblTieuDe.getFont()).getHeight() + 32)));
        pnlNoiDung.add(lblTieuDe, BorderLayout.NORTH);
        pnlNoiDung.add(taoDuLieu(tenKyThuatVien, thongKe), BorderLayout.CENTER);
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 16));
        pnlNut.setBackground(UiTheme.WINDOW);
        UiTheme.styleButton(btnDong, true);
        btnDong.addActionListener(this);
        pnlNut.add(btnDong);
        pnlNoiDung.add(pnlNut, BorderLayout.SOUTH);
        return pnlNoiDung;
    }

    /**
     * Hiển thị cùng một ngày và cùng một tổng số trên toàn bộ dialog.
     * @param tenKyThuatVien tên kỹ thuật viên
     * @param thongKe thống kê của ngày được chọn
     * @return panel chứa mô tả, tổng số, bảng và kết luận
     */
    private static JPanel taoDuLieu(String tenKyThuatVien, ThongKeNgayKyThuat thongKe) {
        JPanel pnlDuLieu = new JPanel(new BorderLayout(0, 8));
        pnlDuLieu.setBackground(UiTheme.WINDOW);
        pnlDuLieu.setBorder(BorderFactory.createEmptyBorder(24, 16, 16, 16));
        JPanel pnlDau = new JPanel(new BorderLayout(0, 8));
        pnlDau.setOpaque(false);
        String ngay = thongKe.getNgay().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        pnlDau.add(KyThuatUi.taoNhan("Ngày " + ngay + " • " + tenKyThuatVien
                + " • Tính theo ngày hoàn tất", UiTheme.SHELL_FONT.deriveFont(12.0f)), BorderLayout.NORTH);
        JPanel pnlTong = KyThuatUi.taoThe();
        pnlTong.add(KyThuatUi.taoNhan("Tổng công việc hoàn tất", UiTheme.BRAND_FONT), BorderLayout.NORTH);
        pnlTong.add(KyThuatUi.taoNhan(thongKe.tinhTongCongViec() + " công việc",
                UiTheme.BRAND_FONT.deriveFont(26.0f)), BorderLayout.CENTER);
        Dimension kichThuocTong = pnlTong.getPreferredSize();
        pnlTong.setPreferredSize(new Dimension(0, Math.max(82, kichThuocTong.height)));
        pnlDau.add(pnlTong, BorderLayout.CENTER);
        pnlDuLieu.add(pnlDau, BorderLayout.NORTH);
        JPanel pnlBang = KyThuatUi.taoThe();
        pnlBang.add(KyThuatUi.taoNhan("Chi tiết theo nhóm công việc", UiTheme.BRAND_FONT), BorderLayout.NORTH);
        pnlBang.add(taoBang(thongKe), BorderLayout.CENTER);
        JPanel pnlVungBang = new JPanel(new BorderLayout(0, 8));
        pnlVungBang.setOpaque(false);
        pnlVungBang.add(pnlBang, BorderLayout.NORTH);
        pnlDuLieu.add(pnlVungBang, BorderLayout.CENTER);
        JLabel lblKetLuan = KyThuatUi.taoNhan("Đã hoàn tất " + thongKe.tinhTongCongViec()
                + " công việc trong ngày. Một xe có thể có nhiều công việc.", UiTheme.SHELL_FONT);
        lblKetLuan.setVerticalAlignment(SwingConstants.TOP);
        pnlVungBang.add(lblKetLuan, BorderLayout.CENTER);
        return pnlDuLieu;
    }

    /**
     * Tính tỷ trọng từ số lượng thực của từng nhóm, kể cả ngày có tổng bằng không.
     * @param thongKe thống kê của ngày được chọn
     * @return vùng cuộn chứa bảng thống kê chỉ đọc
     */
    private static JScrollPane taoBang(ThongKeNgayKyThuat thongKe) {
        DefaultTableModel modelChiTiet = new DefaultTableModel(
                new Object[]{"Nhóm công việc", "Đã hoàn tất", "Tỷ trọng"}, 0) {
            private static final long serialVersionUID = 1L;

            /** {@inheritDoc} */
            @Override
            public boolean isCellEditable(int dong, int cot) {
                return false;
            }
        };
        String[] nhomCongViec = {"Bảo dưỡng định kỳ", "Sửa chữa cơ khí", "Điện và chẩn đoán"};
        int tong = thongKe.tinhTongCongViec();
        List<Integer> soLuongTheoNhom = thongKe.getSoLuongTheoNhom();
        for (int i = 0; i < nhomCongViec.length; i++) {
            int soLuong = soLuongTheoNhom.get(i);
            String tyTrong = tong == 0 ? "0,0%" : BigDecimal.valueOf(soLuong * 100L)
                    .divide(BigDecimal.valueOf(tong), 1, RoundingMode.HALF_UP).toPlainString().replace('.', ',') + "%";
            modelChiTiet.addRow(new Object[]{nhomCongViec[i], soLuong, tyTrong});
        }
        JTable tblChiTiet = new JTable(modelChiTiet);
        tblChiTiet.setFont(UiTheme.SHELL_FONT);
        tblChiTiet.setForeground(UiTheme.TEXT);
        tblChiTiet.setBackground(UiTheme.SURFACE);
        tblChiTiet.setRowHeight(32);
        tblChiTiet.setGridColor(UiTheme.BORDER);
        tblChiTiet.setShowGrid(true);
        tblChiTiet.setRowSelectionAllowed(false);
        // Bảng không có thao tác chọn/sửa; giữ điểm Tab ở nút đóng để Enter không bị bảng nhận mất.
        tblChiTiet.setFocusable(false);
        tblChiTiet.getTableHeader().setReorderingAllowed(false);
        tblChiTiet.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            /** {@inheritDoc} */
            @Override
            public Component getTableCellRendererComponent(JTable tblBang, Object giaTri,
                    boolean duocChon, boolean coFocus, int dong, int cot) {
                JLabel lblO = (JLabel) super.getTableCellRendererComponent(
                        tblBang, giaTri, duocChon, coFocus, dong, cot);
                lblO.setFont(UiTheme.BRAND_FONT);
                lblO.setBackground(UiTheme.WINDOW);
                lblO.setForeground(UiTheme.TEXT);
                lblO.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.BORDER),
                        BorderFactory.createEmptyBorder(0, 8, 0, 8)));
                return lblO;
            }
        });
        tblChiTiet.getTableHeader().setPreferredSize(new Dimension(0, 32));
        tblChiTiet.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            /** {@inheritDoc} */
            @Override
            public Component getTableCellRendererComponent(JTable tblBang, Object giaTri,
                    boolean duocChon, boolean coFocus, int dong, int cot) {
                JLabel lblO = (JLabel) super.getTableCellRendererComponent(
                        tblBang, giaTri, duocChon, coFocus, dong, cot);
                lblO.setFont(UiTheme.SHELL_FONT);
                lblO.setBackground(UiTheme.SURFACE);
                lblO.setForeground(UiTheme.TEXT);
                lblO.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                lblO.setHorizontalAlignment(cot == 1 ? SwingConstants.RIGHT : SwingConstants.LEFT);
                return lblO;
            }
        });
        JScrollPane scrChiTiet = new JScrollPane(tblChiTiet);
        scrChiTiet.setColumnHeaderView(tblChiTiet.getTableHeader());
        scrChiTiet.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));
        scrChiTiet.setPreferredSize(new Dimension(0, 130));
        return scrChiTiet;
    }
}
