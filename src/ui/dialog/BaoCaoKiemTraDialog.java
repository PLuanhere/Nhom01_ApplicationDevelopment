package ui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import dao.BaoCaoKiemTraDAO;
import entity.BaoCaoKiemTra;
import entity.PhieuTiepNhan;
import entity.TaiKhoan;
import entity.TrangThaiKiemTra;
import ui.component.KiemTraUi;
import ui.component.UiTheme;

// Biểu mẫu thêm, sửa và xem báo cáo; lưu qua DAO và giữ nội dung khi lưu thất bại.
public final class BaoCaoKiemTraDialog extends JDialog implements ActionListener {
    private static final long serialVersionUID = 1L;

    private final JFrame winChu;
    private final BaoCaoKiemTraDAO baoCaoDAO;
    private final TaiKhoan taiKhoan;
    private final BaoCaoKiemTra baoCaoCu;
    private final boolean chiDoc;

    private final JComboBox<PhieuTiepNhan> cmbPhieu = new JComboBox<PhieuTiepNhan>();
    private final JComboBox<String> cmbBoPhan = new JComboBox<String>(
            new String[]{"Điều hòa", "Phanh", "Khoang máy / dầu", "Điện", "Tổng hợp"});
    private final JTextField txtNgay = new JTextField("05/10/2026");
    private final JTextArea txaKetQua = new JTextArea(3, 20);
    private final JTextArea txaHuHong = new JTextArea(3, 20);
    private final JTextArea txaDeXuat = new JTextArea(3, 20);
    private final JLabel lblThongBao = KiemTraUi.taoNhan("", false);

    private String dauVetBanDau;
    private BaoCaoKiemTra ketQua;

    public BaoCaoKiemTraDialog(JFrame winChu, BaoCaoKiemTraDAO baoCaoDAO, TaiKhoan taiKhoan,
            BaoCaoKiemTra baoCaoCu, boolean chiDoc) {
        super(winChu, chiDoc ? "Chi tiết báo cáo kiểm tra"
                : baoCaoCu == null ? "Thêm báo cáo kiểm tra" : "Sửa báo cáo kiểm tra", true);
        this.winChu = winChu;
        this.baoCaoDAO = baoCaoDAO;
        this.taiKhoan = taiKhoan;
        this.baoCaoCu = baoCaoCu;
        this.chiDoc = chiDoc;
        List<PhieuTiepNhan> danhSachPhieu = baoCaoDAO.layPhieu(taiKhoan);
        for (PhieuTiepNhan phieuTiepNhan : danhSachPhieu) {
            if (phieuTiepNhan.isDaXacNhan()) {
                cmbPhieu.addItem(phieuTiepNhan);
            }
        }
        if (baoCaoCu != null) {
            cmbPhieu.setSelectedItem(baoCaoCu.getPhieu());
            cmbBoPhan.setSelectedItem(baoCaoCu.getBoPhan());
            txtNgay.setText(baoCaoCu.getNgayKiemTra().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            String[] noiDung = baoCaoCu.getNoiDung();
            txaKetQua.setText(noiDung[0]);
            txaHuHong.setText(noiDung[1]);
            txaDeXuat.setText(noiDung[2]);
            cmbPhieu.setEnabled(false);
        } else {
            for (PhieuTiepNhan phieuTiepNhan : danhSachPhieu) {
                if (phieuTiepNhan.isDaXacNhan() && !baoCaoDAO.timTheoPhieu(taiKhoan, phieuTiepNhan).isPresent()) {
                    cmbPhieu.setSelectedItem(phieuTiepNhan);
                    break;
                }
            }
            txaKetQua.setText("Làm lạnh yếu");
            txaHuHong.setText("Lọc gió bẩn");
            txaDeXuat.setText("Vệ sinh và kiểm tra lại");
        }
        setUndecorated(true);
        getRootPane().setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));
        setLayout(new BorderLayout());
        JPanel pnlDau = new JPanel(new BorderLayout());
        pnlDau.setBackground(UiTheme.SURFACE);
        pnlDau.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        pnlDau.add(KiemTraUi.taoNhan(getTitle(), true), BorderLayout.CENTER);
        JButton btnDong = KiemTraUi.taoNut("×", "HUY", false, this);
        btnDong.setPreferredSize(new Dimension(32, 20));
        pnlDau.add(btnDong, BorderLayout.EAST);
        add(pnlDau, BorderLayout.NORTH);
        add(taoBieuMau(), BorderLayout.CENTER);
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        pnlNut.add(KiemTraUi.taoNut(chiDoc ? "Đóng" : "Hủy", "HUY", false, this));
        if (!chiDoc) {
            pnlNut.add(KiemTraUi.taoNut("Lưu", "LUU", true, this));
        }
        add(pnlNut, BorderLayout.SOUTH);
        dauVetBanDau = layDauVet();
        setSize(760, chiDoc ? 376 : 500);
        setLocationRelativeTo(winChu);
    }

    public BaoCaoKiemTra getKetQua() { return ketQua; }

    /** {@inheritDoc} */
    // Lưu báo cáo qua DAO hoặc xác nhận trước khi bỏ những thay đổi chưa lưu.
    @Override
    public void actionPerformed(ActionEvent suKien) {
        if ("HUY".equals(suKien.getActionCommand())) {
            if (chiDoc || dauVetBanDau.equals(layDauVet()) || KiemTraUi.xacNhan(winChu,
                    "Bỏ thay đổi chưa lưu?", "Thông tin đang nhập chưa được lưu.\n\n"
                    + "Nếu bỏ thay đổi, danh sách giữ nguyên dữ liệu trước khi mở biểu mẫu.",
                    "Bỏ thay đổi", "Tiếp tục nhập")) {
                dispose();
            }
            return;
        }
        try {
            LocalDate ngayKiemTra = LocalDate.parse(txtNgay.getText().trim(), DateTimeFormatter
                    .ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT));
            String[] noiDung = baoCaoCu == null ? new String[]{"", "", "", "", "", "", ""}
                    : baoCaoCu.getNoiDung();
            noiDung[0] = txaKetQua.getText().trim();
            noiDung[1] = txaHuHong.getText().trim();
            noiDung[2] = txaDeXuat.getText().trim();
            BaoCaoKiemTra banNhap = new BaoCaoKiemTra(baoCaoCu == null ? "" : baoCaoCu.getMaBaoCao(),
                    (PhieuTiepNhan) cmbPhieu.getSelectedItem(), ngayKiemTra,
                    String.valueOf(cmbBoPhan.getSelectedItem()), noiDung, TrangThaiKiemTra.NHAP, null);
            ketQua = baoCaoDAO.luu(taiKhoan, banNhap, false);
            dispose();
        } catch (RuntimeException loi) {
            if (!(loi instanceof IllegalArgumentException) && !(loi instanceof IllegalStateException)
                    && !(loi instanceof java.time.format.DateTimeParseException)) {
                java.util.logging.Logger.getLogger(BaoCaoKiemTraDialog.class.getName())
                        .log(java.util.logging.Level.WARNING, "Không lưu được báo cáo kiểm tra", loi);
            }
            lblThongBao.setText("Chưa lưu: kiểm tra ngày dd/MM/yyyy, phiếu, kết quả và đề xuất.");
            lblThongBao.setForeground(UiTheme.ERROR);
            lblThongBao.setToolTipText(loi.getMessage());
        }
    }

    /** @return biểu mẫu hai cột theo thiết kế */
    // Tạo các hàng thông tin, trường nhập và thông báo lỗi ngay trên biểu mẫu.
    private JPanel taoBieuMau() {
        JPanel pnlNoiDung = new JPanel(new BorderLayout(0, 12));
        pnlNoiDung.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        lblThongBao.setText(chiDoc ? "Thông tin hồ sơ · Chỉ đọc" : "Các trường * là bắt buộc");
        lblThongBao.setForeground(UiTheme.MUTED);
        pnlNoiDung.add(lblThongBao, BorderLayout.NORTH);
        JPanel pnlHang = new JPanel();
        pnlHang.setLayout(new BoxLayout(pnlHang, BoxLayout.Y_AXIS));
        pnlHang.setOpaque(false);
        JTextField txtMa = new JTextField(baoCaoCu == null ? "Tự sinh khi lưu" : baoCaoCu.getMaBaoCao());
        txtMa.setEditable(false);
        JTextField txtKyThuat = new JTextField("Chung Ngô Minh Hoàng");
        txtKyThuat.setEditable(false);
        JPanel pnlHang0 = new JPanel(new GridLayout(1, 2, 12, 0));
        pnlHang0.setOpaque(false);
        pnlHang0.add(KiemTraUi.taoTruong("Số báo cáo", chiDoc ? KiemTraUi.taoNhan(txtMa.getText(), false) : txtMa));
        pnlHang0.add(KiemTraUi.taoTruong("Phiếu tiếp nhận *", chiDoc
                ? KiemTraUi.taoNhan(String.valueOf(cmbPhieu.getSelectedItem()), false) : cmbPhieu));
        pnlHang0.setPreferredSize(new Dimension(0, chiDoc ? 40 : 50));
        pnlHang.add(pnlHang0);
        pnlHang.add(Box.createVerticalStrut(12));
        JPanel pnlHang1 = new JPanel(new GridLayout(1, 2, 12, 0));
        pnlHang1.setOpaque(false);
        pnlHang1.add(KiemTraUi.taoTruong("Kỹ thuật viên", chiDoc
                ? KiemTraUi.taoNhan(txtKyThuat.getText(), false) : txtKyThuat));
        pnlHang1.add(KiemTraUi.taoTruong("Ngày kiểm tra *", chiDoc
                ? KiemTraUi.taoNhan(txtNgay.getText(), false) : txtNgay));
        pnlHang1.setPreferredSize(new Dimension(0, chiDoc ? 40 : 50));
        pnlHang.add(pnlHang1);
        pnlHang.add(Box.createVerticalStrut(12));
        JPanel pnlHang2 = new JPanel(new GridLayout(1, 2, 12, 0));
        pnlHang2.setOpaque(false);
        pnlHang2.add(KiemTraUi.taoTruong("Bộ phận *", chiDoc
                ? KiemTraUi.taoNhan(String.valueOf(cmbBoPhan.getSelectedItem()), false) : cmbBoPhan));
        pnlHang2.add(KiemTraUi.taoTruong("Kết quả kiểm tra *", taoO(txaKetQua)));
        pnlHang2.setPreferredSize(new Dimension(0, chiDoc ? 40 : 92));
        pnlHang.add(pnlHang2);
        pnlHang.add(Box.createVerticalStrut(12));
        JPanel pnlHang3 = new JPanel(new GridLayout(1, 2, 12, 0));
        pnlHang3.setOpaque(false);
        pnlHang3.add(KiemTraUi.taoTruong("Hư hỏng phát hiện", taoO(txaHuHong)));
        pnlHang3.add(KiemTraUi.taoTruong("Đề xuất sửa chữa *", taoO(txaDeXuat)));
        pnlHang3.setPreferredSize(new Dimension(0, chiDoc ? 40 : 92));
        pnlHang.add(pnlHang3);
        pnlNoiDung.add(pnlHang, BorderLayout.CENTER);
        JTextArea txaQuyTac = KiemTraUi.taoVanBan(KiemTraUi.QUY_TAC);
        txaQuyTac.setFont(UiTheme.CAPTION_FONT);
        txaQuyTac.setPreferredSize(new Dimension(0, 32));
        pnlNoiDung.add(txaQuyTac, BorderLayout.SOUTH);
        return pnlNoiDung;
    }

    /**
     * @param txaNoiDung vùng nhập
     * @return ô nhập hoặc văn bản chỉ đọc
     */
    // Chọn vùng cuộn cho nhập nhiều dòng hoặc hiển thị văn bản khi xem chi tiết.
    private JComponent taoO(JTextArea txaNoiDung) {
        txaNoiDung.setFont(UiTheme.SHELL_FONT);
        txaNoiDung.setLineWrap(true);
        txaNoiDung.setWrapStyleWord(true);
        return chiDoc ? KiemTraUi.taoVanBan(txaNoiDung.getText()) : KiemTraUi.taoVungCuon(txaNoiDung);
    }

    /** @return chuỗi phản ánh các nội dung người dùng đang nhập */
    // Lấy dấu vết biểu mẫu để chỉ hỏi bỏ thay đổi khi dữ liệu thực sự đã khác.
    private String layDauVet() {
        return cmbPhieu.getSelectedIndex() + "|" + cmbBoPhan.getSelectedIndex() + "|" + txtNgay.getText()
                + "|" + txaKetQua.getText() + "|" + txaHuHong.getText() + "|" + txaDeXuat.getText();
    }
}