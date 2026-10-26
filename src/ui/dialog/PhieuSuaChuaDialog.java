package ui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

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

import dao.PhieuSuaChuaDAO;
import entity.BaoGia;
import entity.PhieuSuaChua;
import entity.TaiKhoan;
import ui.component.SuaChuaUi;
import ui.component.UiTheme;

public final class PhieuSuaChuaDialog extends JDialog implements ActionListener {
    private static final long serialVersionUID = 1L;

    private final JFrame winChu;
    private final PhieuSuaChuaDAO phieuDAO;
    private final TaiKhoan taiKhoan;
    private final PhieuSuaChua phieuCu;
    private final boolean chiDoc;

    private final JComboBox<BaoGia> cmbBaoGia = new JComboBox<BaoGia>();
    private final JComboBox<String> cmbKyThuat = new JComboBox<String>(
            new String[]{"Chung Ngô Minh Hoàng", "Nguyễn Minh Vũ"});
    private final JTextField txtHan = new JTextField("05/10/2026 17:00");
    private final JTextArea txaNoiDung = new JTextArea("Kiểm tra điều hòa", 3, 20);
    private final JLabel lblThongBao = SuaChuaUi.taoNhan("Các trường * là bắt buộc", false);

    /** Nội dung ban đầu và kết quả sau khi lưu thành công. */
    private String dauVetBanDau;
    private PhieuSuaChua ketQua;

    public PhieuSuaChuaDialog(JFrame winChu, PhieuSuaChuaDAO phieuDAO, TaiKhoan taiKhoan,
            PhieuSuaChua phieuCu, boolean chiDoc) {
        super(winChu, chiDoc ? "Chi tiết lệnh sửa chữa"
                : phieuCu == null ? "Thêm lệnh sửa chữa" : "Sửa lệnh sửa chữa", true);
        this.winChu = winChu;
        this.phieuDAO = phieuDAO;
        this.taiKhoan = taiKhoan;
        this.phieuCu = phieuCu;
        this.chiDoc = chiDoc;
        if (!chiDoc) {
            for (BaoGia baoGia : phieuDAO.layBaoGiaDaDuyet(taiKhoan)) {
                cmbBaoGia.addItem(baoGia);
                if ((phieuCu == null && "BG-2026-0113".equals(baoGia.getMaBaoGia()))
                        || (phieuCu != null && phieuCu.getMaBaoGia().equals(baoGia.getMaBaoGia()))) {
                    cmbBaoGia.setSelectedItem(baoGia);
                }
            }
        }
        if (phieuCu != null) {
            cmbKyThuat.setSelectedIndex("kythuatvu".equals(phieuCu.getKyThuatChinh()) ? 1 : 0);
            txtHan.setText(phieuCu.getHanHoanThanh().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            txaNoiDung.setText(phieuCu.getGhiChu());
            cmbBaoGia.setEnabled(false);
        }
        setUndecorated(true);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        getRootPane().setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));
        setLayout(new BorderLayout());
        JPanel pnlDau = new JPanel(new BorderLayout());
        pnlDau.setBackground(UiTheme.SURFACE);
        pnlDau.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        pnlDau.add(SuaChuaUi.taoNhan(getTitle(), true), BorderLayout.CENTER);
        JButton btnDong = SuaChuaUi.taoNut("×", "HUY", false, this);
        btnDong.setPreferredSize(new Dimension(32, 20));
        pnlDau.add(btnDong, BorderLayout.EAST);
        add(pnlDau, BorderLayout.NORTH);
        add(taoBieuMau(), BorderLayout.CENTER);
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        pnlNut.add(SuaChuaUi.taoNut(chiDoc ? "Đóng" : "Hủy", "HUY", false, this));
        if (!chiDoc) {
            pnlNut.add(SuaChuaUi.taoNut("Lưu", "LUU", true, this));
        }
        add(pnlNut, BorderLayout.SOUTH);
        dauVetBanDau = layDauVet();
        setSize(760, chiDoc ? 340 : 412);
        setLocationRelativeTo(winChu);
    }

    public PhieuSuaChua getKetQua() { return ketQua; }

    /** {@inheritDoc} */
    // Lưu dữ liệu hợp lệ hoặc hỏi trước khi bỏ những thay đổi thực sự chưa lưu.
    @Override
    public void actionPerformed(ActionEvent suKien) {
        if ("HUY".equals(suKien.getActionCommand())) {
            if (chiDoc || dauVetBanDau.equals(layDauVet()) || SuaChuaUi.xacNhan(winChu,
                    "Bỏ thay đổi chưa lưu?", "Thông tin đang nhập chưa được lưu.", "Bỏ thay đổi", "Tiếp tục nhập")) {
                dispose();
            }
            return;
        }
        try {
            LocalDateTime han = LocalDateTime.parse(txtHan.getText().trim(), DateTimeFormatter
                    .ofPattern("dd/MM/uuuu HH:mm").withResolverStyle(ResolverStyle.STRICT));
            BaoGia baoGia = (BaoGia) cmbBaoGia.getSelectedItem();
            if (baoGia == null) {
                throw new IllegalArgumentException("Chưa chọn báo giá đã được khách đồng ý.");
            }
            ketQua = phieuDAO.luu(taiKhoan, phieuCu == null ? "" : phieuCu.getMaPhieu(), baoGia.getMaBaoGia(),
                    cmbKyThuat.getSelectedIndex() == 0 ? "kythuat" : "kythuatvu",
                    phieuCu == null ? "" : phieuCu.getKyThuatPhoiHop(), han, txaNoiDung.getText().trim());
            dispose();
        } catch (java.time.format.DateTimeParseException loi) {
            lblThongBao.setText("Hạn phải đúng dd/MM/yyyy HH:mm và là ngày giờ hợp lệ.");
            lblThongBao.setForeground(UiTheme.ERROR);
        } catch (IllegalArgumentException | IllegalStateException | SecurityException loi) {
            lblThongBao.setText(loi.getMessage());
            lblThongBao.setToolTipText(loi.getMessage());
            lblThongBao.setForeground(UiTheme.ERROR);
        }
    }

    /** @return biểu mẫu theo bố cục thiết kế */
    // Tạo ba hàng hai cột và thay trường nhập bằng văn bản khi xem chi tiết.
    private JPanel taoBieuMau() {
        JPanel pnlNoiDung = new JPanel(new BorderLayout(0, 12));
        pnlNoiDung.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        lblThongBao.setForeground(UiTheme.MUTED);
        if (chiDoc) {
            lblThongBao.setText("Thông tin hồ sơ · Chỉ đọc");
        }
        pnlNoiDung.add(lblThongBao, BorderLayout.NORTH);
        JPanel pnlTruong = new JPanel();
        pnlTruong.setLayout(new BoxLayout(pnlTruong, BoxLayout.Y_AXIS));
        pnlTruong.setOpaque(false);
        JTextField txtMa = new JTextField(phieuCu == null ? "Tự sinh khi lưu" : phieuCu.getMaPhieu());
        txtMa.setEditable(false);
        JTextField txtTrangThai = new JTextField(phieuCu == null ? "Chờ sửa chữa"
                : phieuCu.getTrangThai().getTenHienThi());
        txtTrangThai.setEditable(false);
        themHang(pnlTruong, SuaChuaUi.taoTruong("Số lệnh", chiDoc
                ? SuaChuaUi.taoNhan(txtMa.getText(), false) : txtMa),
                SuaChuaUi.taoTruong("Báo giá *", chiDoc
                    ? SuaChuaUi.taoNhan(phieuCu.getMaBaoGia(), false) : cmbBaoGia), chiDoc ? 40 : 50);
        themHang(pnlTruong, SuaChuaUi.taoTruong("Kỹ thuật viên *", chiDoc
                ? SuaChuaUi.taoNhan(SuaChuaUi.layTenKyThuat(phieuCu.getKyThuatChinh()), false) : cmbKyThuat),
                SuaChuaUi.taoTruong("Hạn hoàn thành *", chiDoc
                    ? SuaChuaUi.taoNhan(txtHan.getText(), false) : txtHan), chiDoc ? 40 : 50);
        txaNoiDung.setLineWrap(true);
        txaNoiDung.setWrapStyleWord(true);
        JComponent truongNoiDung = chiDoc ? SuaChuaUi.taoVanBan(txaNoiDung.getText())
                : SuaChuaUi.taoVungCuon(txaNoiDung);
        themHang(pnlTruong, SuaChuaUi.taoTruong("Nội dung phân công", truongNoiDung),
                SuaChuaUi.taoTruong("Trạng thái", chiDoc
                    ? SuaChuaUi.taoNhan(txtTrangThai.getText(), false) : txtTrangThai), chiDoc ? 40 : 92);
        pnlNoiDung.add(pnlTruong, BorderLayout.CENTER);
        JTextArea txaQuyTac = SuaChuaUi.taoVanBan(SuaChuaUi.QUY_TAC);
        txaQuyTac.setFont(UiTheme.CAPTION_FONT);
        txaQuyTac.setPreferredSize(new Dimension(0, 34));
        pnlNoiDung.add(txaQuyTac, BorderLayout.SOUTH);
        return pnlNoiDung;
    }

    /**
     * @param pnlCha nơi chứa
     * @param pnlTrai cột trái
     * @param pnlPhai cột phải
     * @param cao chiều cao pixel
     */
    // Ghép hai trường thành một hàng có chiều cao theo thiết kế.
    private void themHang(JPanel pnlCha, JPanel pnlTrai, JPanel pnlPhai, int cao) {
        if (pnlCha.getComponentCount() > 0) {
            pnlCha.add(Box.createVerticalStrut(12));
        }
        JPanel pnlHang = new JPanel(new GridLayout(1, 2, 12, 0));
        pnlHang.setOpaque(false);
        pnlHang.add(pnlTrai);
        pnlHang.add(pnlPhai);
        pnlHang.setMaximumSize(new Dimension(Integer.MAX_VALUE, cao));
        pnlHang.setPreferredSize(new Dimension(0, cao));
        pnlCha.add(pnlHang);
    }

    /** @return dấu vết các trường nhập */
    // Ghi lại giá trị đang nhập để phát hiện thay đổi khi đóng biểu mẫu.
    private String layDauVet() {
        return cmbBaoGia.getSelectedIndex() + "|" + cmbKyThuat.getSelectedIndex()
                + "|" + txtHan.getText() + "|" + txaNoiDung.getText();
    }
}