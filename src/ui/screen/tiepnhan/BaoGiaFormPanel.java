package ui.screen.tiepnhan;

import ui.component.ScrollablePanel;
import ui.component.UiTheme;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/** Chứa biểu mẫu lập báo giá và ghi nhận phản hồi khách hàng. */
public final class BaoGiaFormPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final CardLayout boCuc = new CardLayout();

    public BaoGiaFormPanel(JComboBox<String> cboBaoCao, JLabel lblCanCu, JLabel lblPhienBan, JTextArea txtDeXuat,
            JTextField txtHen, JTable tblChiTiet, JLabel lblTong, JLabel lblLoi,
            JLabel lblBaoGiaGui, JTable tblQuyetDinh, JLabel lblTongQuyetDinh, JRadioButton radDongY,
            JRadioButton radDieuChinh, JRadioButton radTuChoi, ButtonGroup nhomQuyetDinh, JTextField txtPhanHoiLuc,
            JTextField txtKenh, JTextArea txtGhiChu, JButton btnGhiNhan, JLabel lblPhanHoi, ActionListener quayLai,
            ActionListener themDong, ActionListener suaDong, ActionListener xoaDong, ActionListener luuNhap,
            ActionListener guiKhach, ActionListener moPhanHoi, ActionListener ghiNhan) {
        super();
        setOpaque(false);
        setLayout(boCuc);
        add(taoForm(cboBaoCao, lblCanCu, lblPhienBan, txtDeXuat, txtHen, tblChiTiet, lblTong, lblLoi,
                quayLai, themDong, suaDong, xoaDong, luuNhap, guiKhach), "FORM");
        add(taoQuyetDinh(lblBaoGiaGui, tblQuyetDinh, lblTongQuyetDinh, radDongY, radDieuChinh, radTuChoi,
                nhomQuyetDinh, txtPhanHoiLuc, txtKenh, txtGhiChu, btnGhiNhan, lblPhanHoi, moPhanHoi, ghiNhan), "QUYET_DINH");
    }

    public void hienThiForm() { boCuc.show(this, "FORM"); }
    public void hienThiQuyetDinh() { boCuc.show(this, "QUYET_DINH"); }

    private JPanel taoForm(JComboBox<String> cboBaoCao, JLabel lblCanCu, JLabel lblPhienBan, JTextArea txtDeXuat,
            JTextField txtHen, JTable tblChiTiet, JLabel lblTong, JLabel lblLoi,
            ActionListener quayLai, ActionListener themDong, ActionListener suaDong, ActionListener xoaDong,
            ActionListener luuNhap, ActionListener guiKhach) {
        JPanel pnl = nen(new BorderLayout(0, 16));
        pnl.add(tieuDe("Lập / điều chỉnh báo giá", "1. Kết quả kiểm tra → 2. Hạng mục & chi phí → 3. Khách phản hồi",
                nut("Danh sách / quay lại", false, quayLai)), BorderLayout.NORTH);
        JPanel khung = khung(new BorderLayout(0, 16));
        JPanel dau = nen(new GridLayout(0, 1, 0, 8));
        dau.add(nhan("Báo cáo kiểm tra hoàn tất", true));
        dau.add(cboBaoCao);
        dau.add(lblCanCu);
        dau.add(lblPhienBan);
        txtDeXuat.setEditable(false);
        txtDeXuat.setLineWrap(true);
        txtDeXuat.setWrapStyleWord(true);
        txtDeXuat.setFont(UiTheme.APP_FONT);
        dau.add(new JScrollPane(txtDeXuat));
        dau.add(truong("Ngày hẹn dự kiến * · dd/MM/yyyy HH:mm", txtHen));
        epCot(dau);
        khung.add(dau, BorderLayout.NORTH);
        JPanel chiTiet = nen(new BorderLayout(0, 10));
        JScrollPane bang = cuonBang(tblChiTiet);
        bang.setPreferredSize(new Dimension(0, 250));
        chiTiet.add(bang);
        JPanel tacVu = nen(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tacVu.add(nut("Thêm dòng", false, themDong));
        tacVu.add(nut("Sửa dòng", false, suaDong));
        tacVu.add(nut("Xóa dòng", false, xoaDong));
        chiTiet.add(tacVu, BorderLayout.SOUTH);
        khung.add(chiTiet);
        JPanel chan = nen(new GridLayout(0, 1, 0, 10));
        chan.add(lblTong);
        lblLoi.setForeground(UiTheme.ERROR);
        chan.add(lblLoi);
        chan.add(nhan("Bản đã gửi chỉ đọc. Điều chỉnh tạo phiên bản mới và cần khách duyệt lại.", false));
        JPanel nutLuu = nen(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nutLuu.add(nut("Lưu nháp", false, luuNhap));
        nutLuu.add(nut("Ghi nhận gửi khách", true, guiKhach));
        chan.add(nutLuu);
        khung.add(chan, BorderLayout.SOUTH);
        pnl.add(cuonNoiDung(khung));
        return pnl;
    }

    private JPanel taoQuyetDinh(JLabel lblBaoGiaGui, JTable tblQuyetDinh, JLabel lblTongQuyetDinh,
            JRadioButton radDongY, JRadioButton radDieuChinh, JRadioButton radTuChoi, ButtonGroup nhomQuyetDinh,
            JTextField txtPhanHoiLuc, JTextField txtKenh, JTextArea txtGhiChu, JButton btnGhiNhan, JLabel lblPhanHoi,
            ActionListener quayLai, ActionListener ghiNhan) {
        JPanel pnl = nen(new BorderLayout(0, 16));
        pnl.add(tieuDe("Ghi nhận quyết định khách hàng", "Đối chiếu đúng báo giá và phiên bản đã gửi khách.",
                nut("Quay lại", false, quayLai)), BorderLayout.NORTH);
        JPanel hang = nen(new GridLayout(1, 2, 16, 0));
        JPanel trai = khung(new BorderLayout(0, 12));
        trai.add(lblBaoGiaGui, BorderLayout.NORTH);
        trai.add(cuonBang(tblQuyetDinh));
        trai.add(lblTongQuyetDinh, BorderLayout.SOUTH);
        hang.add(trai);
        JPanel phai = khung(new BorderLayout(0, 14));
        JPanel truong = nen(new GridLayout(0, 1, 0, 10));
        truong.add(nhan("Kết quả phản hồi khách · không mặc định đồng ý", true));
        for (JRadioButton rad : new JRadioButton[] { radDongY, radDieuChinh, radTuChoi }) {
            rad.setFont(UiTheme.APP_FONT); rad.setOpaque(false); nhomQuyetDinh.add(rad); truong.add(rad);
        }
        truong.add(truong("Thời điểm phản hồi * · dd/MM/yyyy HH:mm", txtPhanHoiLuc));
        truong.add(truong("Kênh phản hồi *", txtKenh));
        txtGhiChu.setLineWrap(true); txtGhiChu.setWrapStyleWord(true);
        truong.add(truong("Ghi chú phản hồi", txtGhiChu));
        epCot(truong);
        phai.add(truong, BorderLayout.NORTH);
        JPanel chan = nen(new GridLayout(0, 1, 0, 10));
        chan.add(lblPhanHoi);
        chan.add(nhan("Từ chối/điều chỉnh: không tạo lệnh từ bản hiện tại.", false));
        btnGhiNhan.setEnabled(false);
        btnGhiNhan.addActionListener(ghiNhan);
        chan.add(btnGhiNhan);
        phai.add(chan, BorderLayout.SOUTH);
        hang.add(phai);
        hang.setPreferredSize(new Dimension(600, 680));
        JScrollPane cuon = cuonNoiDung(hang);
        cuon.getViewport().addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                boolean hep = cuon.getViewport().getWidth() < 850;
                GridLayout layout = (GridLayout) hang.getLayout();
                layout.setRows(hep ? 2 : 1); layout.setColumns(hep ? 1 : 2); layout.setVgap(16);
                hang.setPreferredSize(new Dimension(600, hep ? 1300 : 680)); hang.revalidate();
            }
        });
        pnl.add(cuon);
        return pnl;
    }

    private static JPanel truong(String ten, JComponent component) { JPanel pnl = nen(new BorderLayout(0, 6)); pnl.add(nhan(ten, true), BorderLayout.NORTH); pnl.add(component); return pnl; }
    private static void epCot(JPanel pnl) { Component[] ds = pnl.getComponents(); pnl.removeAll(); pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS)); for (Component component : ds) { JComponent c = (JComponent) component; int cao = c instanceof JComboBox ? 36 : c.getPreferredSize().height; if (c instanceof JScrollPane) cao = 65; c.setAlignmentX(Component.LEFT_ALIGNMENT); c.setMaximumSize(new Dimension(Integer.MAX_VALUE, cao)); c.setPreferredSize(new Dimension(0, cao)); pnl.add(c); pnl.add(Box.createVerticalStrut(8)); } }
    private static JPanel nen(LayoutManager layout) { JPanel pnl = new JPanel(layout); pnl.setOpaque(false); return pnl; }
    private static JPanel khung(LayoutManager layout) { JPanel pnl = UiTheme.roundedPanel(layout, Color.WHITE, 8); pnl.setBorder(UiTheme.roundedBorder(UiTheme.APP_BORDER, 8, 16, 16)); return pnl; }
    private static JLabel nhan(String chu, boolean dam) { JLabel lbl = new JLabel(chu); lbl.setFont(dam ? UiTheme.APP_BOLD_FONT : UiTheme.APP_FONT); lbl.setForeground(dam ? UiTheme.APP_TEXT : UiTheme.APP_MUTED); return lbl; }
    private static JButton nut(String ten, boolean chinh, ActionListener action) { JButton btn = UiTheme.createAppButton(ten, chinh); btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 18, 36)); btn.addActionListener(action); return btn; }
    private static JPanel tieuDe(String ten, String moTa, JButton thaoTac) { JPanel pnl = nen(new BorderLayout(8, 10)); pnl.add(nhan("Tiếp nhận / Nghiệp vụ / " + ten, false), BorderLayout.NORTH); JPanel chu = nen(new BorderLayout(0, 6)); JLabel lbl = nhan(ten, true); lbl.setFont(UiTheme.APP_BOLD_FONT.deriveFont(24f)); chu.add(lbl, BorderLayout.NORTH); chu.add(nhan(moTa, false)); pnl.add(chu); JPanel nut = nen(new FlowLayout(FlowLayout.RIGHT, 0, 0)); nut.add(thaoTac); pnl.add(nut, BorderLayout.EAST); return pnl; }
    private static JScrollPane cuonBang(JTable tbl) { JScrollPane cuon = new JScrollPane(tbl); cuon.getViewport().setBackground(Color.WHITE); return cuon; }
    private static JScrollPane cuonNoiDung(JPanel pnl) { JPanel neo = new ScrollablePanel(); neo.add(pnl, BorderLayout.NORTH); JScrollPane cuon = new JScrollPane(neo); cuon.setBorder(null); cuon.getViewport().setBackground(UiTheme.APP_BACKGROUND); cuon.getVerticalScrollBar().setUnitIncrement(20); return cuon; }
}
