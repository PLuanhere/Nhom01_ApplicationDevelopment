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
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import javax.swing.table.DefaultTableModel;

/** Chứa biểu mẫu nhập và bước đối chiếu của phiếu tiếp nhận. */
public final class PhieuTiepNhanFormPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final CardLayout boCuc = new CardLayout();

    public PhieuTiepNhanFormPanel(JTextField txtTraCuu, JTable tblHoSo, JLabel lblHoSo, JTextField txtKhach,
            JTextField txtXe, JTextField txtKm, JTextField txtNgay, JTextArea txtTinhTrang, JTextArea txtTaiSan,
            JTextArea txtYeuCau, JLabel lblLoi, JTextArea txtDoiChieu, JCheckBox chkXacNhan, JButton btnHoanTat,
            JLabel lblXacNhan, ActionListener quayLai, ActionListener traHoSo, ActionListener chonHoSo,
            ActionListener quanLyKhach, ActionListener quanLyXe, ActionListener doiChieu, ActionListener luuPhieu) {
        super();
        setOpaque(false);
        setLayout(boCuc);
        add(taoForm(txtTraCuu, tblHoSo, lblHoSo, txtKhach, txtXe, txtKm, txtNgay, txtTinhTrang, txtTaiSan,
                txtYeuCau, lblLoi, quayLai, traHoSo, chonHoSo, quanLyKhach, quanLyXe, doiChieu), "FORM");
        add(taoDoiChieu(txtDoiChieu, chkXacNhan, btnHoanTat, lblXacNhan, quayLai, luuPhieu), "DOI_CHIEU");
    }

    public void hienThiForm() {
        boCuc.show(this, "FORM");
    }

    public void hienThiDoiChieu() {
        boCuc.show(this, "DOI_CHIEU");
    }

    private JPanel taoForm(JTextField txtTraCuu, JTable tblHoSo, JLabel lblHoSo, JTextField txtKhach, JTextField txtXe,
            JTextField txtKm, JTextField txtNgay, JTextArea txtTinhTrang, JTextArea txtTaiSan, JTextArea txtYeuCau,
            JLabel lblLoi, ActionListener quayLai, ActionListener traHoSo, ActionListener chonHoSo,
            ActionListener quanLyKhach, ActionListener quanLyXe, ActionListener doiChieu) {
        JPanel pnl = nen(new BorderLayout(0, 16));
        pnl.add(tieuDe("Lập phiếu tiếp nhận xe", "1. Tra cứu hồ sơ → 2. Thông tin tiếp nhận → 3. Đối chiếu",
                nut("Danh sách / quay lại", false, quayLai)), BorderLayout.NORTH);
        JPanel noiDung = nen(new BorderLayout(0, 16));
        JPanel tra = khung(new BorderLayout(0, 10));
        JPanel tim = nen(new BorderLayout(8, 0));
        txtTraCuu.setBorder(BorderFactory.createTitledBorder("Số điện thoại / biển số"));
        txtTraCuu.addActionListener(traHoSo);
        tim.add(txtTraCuu);
        tim.add(nut("Tra cứu", false, traHoSo), BorderLayout.EAST);
        tra.add(tim, BorderLayout.NORTH);
        JScrollPane ketQua = cuonBang(tblHoSo);
        ketQua.setPreferredSize(new Dimension(0, 150));
        tra.add(ketQua);
        JPanel duoi = nen(new BorderLayout(0, 10));
        duoi.add(lblHoSo, BorderLayout.NORTH);
        JPanel tacVu = nen(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tacVu.add(nut("Chọn xe", true, chonHoSo));
        tacVu.add(nut("Quản lý khách hàng", false, quanLyKhach));
        tacVu.add(nut("Quản lý phương tiện", false, quanLyXe));
        duoi.add(tacVu);
        tra.add(duoi, BorderLayout.SOUTH);
        noiDung.add(tra, BorderLayout.NORTH);
        JPanel nhap = khung(new BorderLayout(0, 14));
        JPanel thongTin = nen(new GridLayout(2, 2, 14, 14));
        thongTin.add(truong("Khách hàng đã chọn · chỉ đọc", txtKhach));
        thongTin.add(truong("Phương tiện đã chọn · chỉ đọc", txtXe));
        txtKhach.setEditable(false);
        txtXe.setEditable(false);
        thongTin.add(truong("Số kilomet * · số nguyên không âm", txtKm));
        thongTin.add(truong("Thời gian nhận * · dd/MM/yyyy HH:mm", txtNgay));
        nhap.add(thongTin, BorderLayout.NORTH);
        JPanel moTa = nen(new GridLayout(3, 1, 0, 14));
        moTa.add(truong("Tình trạng ban đầu *", txtTinhTrang));
        moTa.add(truong("Tài sản khách để lại · tùy chọn", txtTaiSan));
        moTa.add(truong("Yêu cầu sửa chữa / bảo dưỡng *", txtYeuCau));
        nhap.add(moTa);
        JPanel chan = nen(new BorderLayout(0, 10));
        lblLoi.setForeground(UiTheme.ERROR);
        chan.add(lblLoi, BorderLayout.NORTH);
        chan.add(nut("Xem lại & ghi nhận", true, doiChieu));
        nhap.add(chan, BorderLayout.SOUTH);
        noiDung.add(nhap);
        pnl.add(cuonNoiDung(noiDung));
        return pnl;
    }

    private JPanel taoDoiChieu(JTextArea txtDoiChieu, JCheckBox chkXacNhan, JButton btnHoanTat, JLabel lblXacNhan,
            ActionListener quayLai, ActionListener luuPhieu) {
        JPanel pnl = nen(new BorderLayout(0, 16));
        pnl.add(tieuDe("Đối chiếu phiếu tiếp nhận", "Đọc lại hiện trạng và yêu cầu cùng khách trước khi xác nhận.",
                nut("Quay lại chỉnh sửa", false, e -> {
                    chkXacNhan.setSelected(false);
                    hienThiForm();
                })), BorderLayout.NORTH);
        JPanel hang = nen(new GridLayout(1, 2, 16, 0));
        JPanel thongTin = khung(new BorderLayout(0, 12));
        thongTin.add(nhan("Thông tin tiếp nhận · chưa lưu", true), BorderLayout.NORTH);
        txtDoiChieu.setEditable(false);
        txtDoiChieu.setRows(16);
        thongTin.add(new JScrollPane(txtDoiChieu));
        hang.add(thongTin);
        JPanel xacNhan = khung(new BorderLayout(0, 16));
        JPanel chu = nen(new GridLayout(0, 1, 0, 8));
        chu.setPreferredSize(new Dimension(0, 320));
        chu.add(nhan("Khách hàng xác nhận nội dung", true));
        chu.add(nhan("<html>Đối chiếu tên khách, xe, kilomet, hiện trạng,<br>tài sản và yêu cầu.<br>Không mặc định khách đã đồng ý.</html>", false));
        chkXacNhan.setOpaque(false);
        chkXacNhan.setFont(UiTheme.APP_FONT);
        chkXacNhan.setText("<html>Khách đã xem và đồng ý thông tin tiếp nhận</html>");
        chkXacNhan.addItemListener(e -> {
            btnHoanTat.setEnabled(chkXacNhan.isSelected());
            lblXacNhan.setText(chkXacNhan.isSelected() ? "Đã chọn xác nhận · chưa lưu phiếu."
                    : "Chưa xác nhận · chưa lưu phiếu.");
        });
        chu.add(chkXacNhan);
        chu.add(lblXacNhan);
        chu.add(nhan("<html>Phiếu được lưu trên SQL Server.<br>Có thể phân công kiểm tra cho kỹ thuật viên.</html>", false));
        xacNhan.add(chu, BorderLayout.NORTH);
        btnHoanTat.setEnabled(false);
        btnHoanTat.setPreferredSize(new Dimension(0, 40));
        btnHoanTat.addActionListener(luuPhieu);
        xacNhan.add(btnHoanTat, BorderLayout.SOUTH);
        hang.add(xacNhan);
        hang.setPreferredSize(new Dimension(600, 440));
        JScrollPane cuon = cuonNoiDung(hang);
        cuon.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                boolean hep = cuon.getViewport().getWidth() < 850;
                GridLayout layout = (GridLayout) hang.getLayout();
                layout.setRows(hep ? 2 : 1);
                layout.setColumns(hep ? 1 : 2);
                layout.setVgap(16);
                hang.setPreferredSize(new Dimension(600, hep ? 900 : 440));
                hang.revalidate();
            }
        });
        pnl.add(cuon);
        return pnl;
    }

    private static JPanel truong(String ten, JTextComponent txt) {
        JPanel pnl = nen(new BorderLayout(0, 6));
        JLabel lbl = nhan(ten, true);
        lbl.setLabelFor(txt);
        pnl.add(lbl, BorderLayout.NORTH);
        txt.setFont(UiTheme.APP_FONT);
        if (txt instanceof JTextArea) {
            JScrollPane cuon = new JScrollPane(txt);
            cuon.setPreferredSize(new Dimension(0, 90));
            pnl.add(cuon);
        } else {
            txt.setPreferredSize(new Dimension(0, 38));
            pnl.add(txt);
        }
        return pnl;
    }

    private static JPanel nen(LayoutManager layout) { JPanel pnl = new JPanel(layout); pnl.setOpaque(false); return pnl; }
    private static JPanel khung(LayoutManager layout) { JPanel pnl = UiTheme.roundedPanel(layout, Color.WHITE, 8); pnl.setBorder(UiTheme.roundedBorder(UiTheme.APP_BORDER, 8, 16, 16)); return pnl; }
    private static JLabel nhan(String chu, boolean dam) { JLabel lbl = new JLabel(chu); lbl.setFont(dam ? UiTheme.APP_BOLD_FONT : UiTheme.APP_FONT); lbl.setForeground(dam ? UiTheme.APP_TEXT : UiTheme.APP_MUTED); return lbl; }
    private static JButton nut(String ten, boolean chinh, ActionListener thaoTac) { JButton btn = UiTheme.createAppButton(ten, chinh); btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 18, 36)); btn.addActionListener(thaoTac); return btn; }
    private static JPanel tieuDe(String ten, String moTa, JButton thaoTac) { JPanel pnl = nen(new BorderLayout(8, 10)); pnl.add(nhan("Tiếp nhận / Nghiệp vụ / " + ten, false), BorderLayout.NORTH); JPanel chu = nen(new BorderLayout(0, 6)); JLabel lbl = nhan(ten, true); lbl.setFont(UiTheme.APP_BOLD_FONT.deriveFont(24f)); chu.add(lbl, BorderLayout.NORTH); chu.add(nhan(moTa, false)); pnl.add(chu); JPanel nut = nen(new FlowLayout(FlowLayout.RIGHT, 0, 0)); nut.add(thaoTac); pnl.add(nut, BorderLayout.EAST); return pnl; }
    private static JScrollPane cuonBang(JTable tbl) { JScrollPane cuon = new JScrollPane(tbl); cuon.getViewport().setBackground(Color.WHITE); return cuon; }
    private static JScrollPane cuonNoiDung(JPanel pnl) { JPanel neo = new ScrollablePanel(); neo.add(pnl, BorderLayout.NORTH); JScrollPane cuon = new JScrollPane(neo); cuon.setBorder(null); cuon.getViewport().setBackground(UiTheme.APP_BACKGROUND); cuon.getVerticalScrollBar().setUnitIncrement(20); return cuon; }
}
