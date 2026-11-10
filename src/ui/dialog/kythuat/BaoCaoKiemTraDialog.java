package ui.dialog.kythuat;

import dao.BaoCaoKiemTraDAO;
import entity.PhieuTiepNhan.BaoCaoKiemTra;
import entity.PhieuTiepNhan.TrangThaiKiemTra;
import entity.TaiKhoan;
import entity.ChiTietKiemTra;
import java.util.ArrayList;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import ui.screen.kythuat.GiaoDienKyThuat;
import entity.PhieuTiepNhan;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import ui.screen.kythuat.KiemTraUi;
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
            new String[] { "Điều hòa", "Phanh", "Khoang máy / dầu", "Điện", "Tổng hợp" });
    private final JTextField txtNgay = new JTextField("05/10/2026");
    private final JTextArea txaKetQua = new JTextArea(3, 20);
    private final JTextArea txaHuHong = new JTextArea(3, 20);
    private final JTextArea txaDeXuat = new JTextArea(3, 20);
    private final JLabel lblThongBao = KiemTraUi.taoNhan("", false);

    private final DefaultTableModel modelChiTiet = new DefaultTableModel(
            new String[] {"Khu vực", "Hạng mục", "Kết quả", "Mức độ", "Đề xuất", "Người ghi", "Thời điểm ghi"}, 0) {
        private static final long serialVersionUID = 1L;

        // Chặn chỉnh sửa khi dialog chỉ dùng để xem báo cáo.
        @Override
        public boolean isCellEditable(int dong, int cot) {
            return !chiDoc && cot < 5;
        }
    };
    private final JTable tblChiTiet = GiaoDienKyThuat.taoBang(modelChiTiet);
    private String dauVetBanDau;
    private BaoCaoKiemTra ketQua;

    // Khởi tạo dữ liệu và thành phần cần dùng của lớp.
    public BaoCaoKiemTraDialog(JFrame winChu, BaoCaoKiemTraDAO baoCaoDAO, TaiKhoan taiKhoan, BaoCaoKiemTra baoCaoCu,
            boolean chiDoc) {
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
                if (phieuTiepNhan.isDaXacNhan() && !(baoCaoDAO.timTheoPhieu(taiKhoan, phieuTiepNhan) != null)) {
                    cmbPhieu.setSelectedItem(phieuTiepNhan);
                    break;

                }
            }
            txaKetQua.setText("Làm lạnh yếu");
            txaHuHong.setText("Lọc gió bẩn");
            txaDeXuat.setText("Vệ sinh và kiểm tra lại");
        }
        cmbBoPhan.setEditable(!chiDoc);
        txtNgay.setText(baoCaoCu == null ? LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : txtNgay.getText());
        JComboBox<String> cmbMucDo = new JComboBox<>(new String[] {
                "Chưa đánh giá", "Đang kiểm tra", "Bình thường", "Cần theo dõi", "Cần xử lý"});
        cmbMucDo.setEditable(true);
        GiaoDienKyThuat.dinhDangCombo(cmbMucDo);
        tblChiTiet.getColumnModel().getColumn(3).setCellEditor(new javax.swing.DefaultCellEditor(cmbMucDo));
        if (baoCaoCu != null) {
            for (ChiTietKiemTra dong : baoCaoCu.getChiTiet()) {
                modelChiTiet.addRow(new Object[] {dong.getTenKhuVuc(), dong.getTenHangMuc(), dong.getKetQua(),
                        dong.getMucDo(), dong.getDeXuat(), dong.getNguoiGhi(), dong.getThoiDiemGhi()});
            }
        }
        if (baoCaoCu == null) {
            txaKetQua.setText("");
            txaHuHong.setText("");
            txaDeXuat.setText("");
            modelChiTiet.addRow(new Object[] {"", "", "", "Chưa đánh giá", "", "", ""});
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
        setSize(1120, 740);
        setLocationRelativeTo(winChu);
    }

    // Trả về ket qua.
    public BaoCaoKiemTra getKetQua() {
        return ketQua;
    }

        // Lưu báo cáo qua DAO hoặc xác nhận trước khi bỏ những thay đổi chưa lưu.
    @Override
    public void actionPerformed(ActionEvent suKien) {
        if (!chiDoc && "THEM_DONG".equals(suKien.getActionCommand())) {
            if (tblChiTiet.isEditing() && !tblChiTiet.getCellEditor().stopCellEditing()) {
                return;
            }
            modelChiTiet.addRow(new Object[] {"", "", "", "Chưa đánh giá", "", "", ""});
            return;
        }
        if (!chiDoc && "XOA_DONG".equals(suKien.getActionCommand())) {
            if (tblChiTiet.isEditing() && !tblChiTiet.getCellEditor().stopCellEditing()) {
                return;
            }
            int dong = tblChiTiet.getSelectedRow();
            if (dong >= 0) {
                modelChiTiet.removeRow(tblChiTiet.convertRowIndexToModel(dong));
            }
            return;
        }
        if ("HUY".equals(suKien.getActionCommand())) {
            if (chiDoc || dauVetBanDau.equals(layDauVet())
                    || KiemTraUi.xacNhan(winChu, "Bỏ thay đổi chưa lưu?",
                            "Thông tin đang nhập chưa được lưu.\n\n"
                                    + "Nếu bỏ thay đổi, danh sách giữ nguyên dữ liệu trước khi mở biểu mẫu.",
                            "Bỏ thay đổi", "Tiếp tục nhập")) {
                dispose();
            }

            return;
        }
        try {
            LocalDate ngayKiemTra = LocalDate.parse(txtNgay.getText().trim(),
                    DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT));
            String[] noiDung = baoCaoCu == null ? new String[] { "", "", "", "", "", "", "" } : baoCaoCu.getNoiDung();
            noiDung[0] = txaKetQua.getText().trim();
            noiDung[1] = txaHuHong.getText().trim();
            noiDung[2] = txaDeXuat.getText().trim();
            if (tblChiTiet.isEditing() && !tblChiTiet.getCellEditor().stopCellEditing()) {
                return;
            }
            List<ChiTietKiemTra> chiTiet = new ArrayList<>();
            for (int i = 0; i < modelChiTiet.getRowCount(); i++) {
                String ten = docO(i, 0), hangMuc = docO(i, 1), kq = docO(i, 2);
                String mucDo = docO(i, 3), dx = docO(i, 4);
                if (!ten.isBlank() || !hangMuc.isBlank() || !kq.isBlank() || !dx.isBlank()) {
                    chiTiet.add(new ChiTietKiemTra(ten, hangMuc, kq, mucDo, dx));
                }
            }
            BaoCaoKiemTra banNhap = new BaoCaoKiemTra(baoCaoCu == null ? "" : baoCaoCu.getMaBaoCao(),
                    (PhieuTiepNhan) cmbPhieu.getSelectedItem(), ngayKiemTra,
                    String.valueOf(cmbBoPhan.getSelectedItem()), noiDung, TrangThaiKiemTra.NHAP, null, chiTiet);
            ketQua = baoCaoDAO.luu(taiKhoan, banNhap, false);
            dispose();
        } catch (RuntimeException loi) {
            if (!(loi instanceof IllegalArgumentException) && !(loi instanceof IllegalStateException)
                    && !(loi instanceof DateTimeParseException)) {
                Logger.getLogger(BaoCaoKiemTraDialog.class.getName()).log(Level.WARNING,
                        "Không lưu được báo cáo kiểm tra", loi);
            }
            lblThongBao.setText("Chưa lưu: kiểm tra ngày dd/MM/yyyy, phiếu, kết quả và đề xuất.");
            lblThongBao.setForeground(UiTheme.ERROR);
            lblThongBao.setToolTipText(loi.getMessage());
        }
    }

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
        JTextField txtKyThuat = new JTextField(taiKhoan.getMaNhanVien());
        txtKyThuat.setEditable(false);
        JPanel pnlHang0 = new JPanel(new GridLayout(1, 2, 12, 0));
        pnlHang0.setOpaque(false);
        pnlHang0.add(KiemTraUi.taoTruong("Số báo cáo", chiDoc ? KiemTraUi.taoNhan(txtMa.getText(), false) : txtMa));
        pnlHang0.add(KiemTraUi.taoTruong("Phiếu tiếp nhận *",
                chiDoc ? KiemTraUi.taoNhan(String.valueOf(cmbPhieu.getSelectedItem()), false) : cmbPhieu));
        pnlHang0.setPreferredSize(new Dimension(0, chiDoc ? 40 : 50));
        pnlHang.add(pnlHang0);
        pnlHang.add(Box.createVerticalStrut(12));
        JPanel pnlHang1 = new JPanel(new GridLayout(1, 2, 12, 0));
        pnlHang1.setOpaque(false);
        pnlHang1.add(KiemTraUi.taoTruong("Kỹ thuật viên",
                chiDoc ? KiemTraUi.taoNhan(txtKyThuat.getText(), false) : txtKyThuat));
        pnlHang1.add(
                KiemTraUi.taoTruong("Ngày kiểm tra *", chiDoc ? KiemTraUi.taoNhan(txtNgay.getText(), false) : txtNgay));
        pnlHang1.setPreferredSize(new Dimension(0, chiDoc ? 40 : 50));
        pnlHang.add(pnlHang1);
        pnlHang.add(Box.createVerticalStrut(12));
        JPanel pnlHang2 = new JPanel(new GridLayout(1, 2, 12, 0));
        pnlHang2.setOpaque(false);
        pnlHang2.add(KiemTraUi.taoTruong("Bộ phận *",
                chiDoc ? KiemTraUi.taoNhan(String.valueOf(cmbBoPhan.getSelectedItem()), false) : cmbBoPhan));
        pnlHang2.add(KiemTraUi.taoTruong("Kết quả kiểm tra *", taoO(txaKetQua)));
        pnlHang2.setPreferredSize(new Dimension(0, chiDoc ? 40 : 92));
        pnlHang.add(pnlHang2);
        pnlHang.add(Box.createVerticalStrut(12));
        JPanel pnlHang3 = new JPanel(new GridLayout(1, 2, 12, 0));
        pnlHang3.setOpaque(false);
        pnlHang3.add(KiemTraUi.taoTruong("Hư hỏng phát hiện", taoO(txaHuHong)));
        pnlHang3.add(KiemTraUi.taoTruong("Đề xuất chung (nếu có)", taoO(txaDeXuat)));
        pnlHang3.setPreferredSize(new Dimension(0, chiDoc ? 40 : 92));
        pnlHang.add(pnlHang3);
        JPanel pnlChiTiet = new JPanel(new BorderLayout(0, 8));
        pnlChiTiet.add(pnlHang, BorderLayout.NORTH);
        pnlChiTiet.add(GiaoDienKyThuat.bocBang(tblChiTiet), BorderLayout.CENTER);
        if (!chiDoc) {
            JPanel pnlNutDong = new JPanel(new FlowLayout(FlowLayout.LEFT));
            pnlNutDong.add(GiaoDienKyThuat.taoNut("Thêm hạng mục", "THEM_DONG", false, this));
            pnlNutDong.add(GiaoDienKyThuat.taoNut("Xóa dòng đã chọn", "XOA_DONG", false, this));
            pnlChiTiet.add(pnlNutDong, BorderLayout.SOUTH);
        }
        pnlNoiDung.add(new javax.swing.JScrollPane(pnlChiTiet), BorderLayout.CENTER);
        JTextArea txaQuyTac = KiemTraUi.taoVanBan(KiemTraUi.QUY_TAC);
        txaQuyTac.setFont(UiTheme.CAPTION_FONT);
        txaQuyTac.setPreferredSize(new Dimension(0, 32));
        pnlNoiDung.add(txaQuyTac, BorderLayout.SOUTH);
        return pnlNoiDung;
    }

    // Chọn vùng cuộn cho nhập nhiều dòng hoặc hiển thị văn bản khi xem chi tiết.
    private JComponent taoO(JTextArea txaNoiDung) {
        txaNoiDung.setFont(UiTheme.SHELL_FONT);
        txaNoiDung.setLineWrap(true);
        txaNoiDung.setWrapStyleWord(true);
        return chiDoc ? KiemTraUi.taoVanBan(txaNoiDung.getText()) : KiemTraUi.taoVungCuon(txaNoiDung);
    }

    // Đọc nội dung ô và giữ chuỗi rỗng cho giá trị chưa nhập.
    private String docO(int dong, int cot) {
        Object giaTri = modelChiTiet.getValueAt(dong, cot);
        return giaTri == null ? "" : giaTri.toString().trim();
    }

    // Lấy dấu vết biểu mẫu để chỉ hỏi bỏ thay đổi khi dữ liệu thực sự đã khác.
    private String layDauVet() {
        if (tblChiTiet.isEditing()) {
            tblChiTiet.getCellEditor().stopCellEditing();
        }
        return modelChiTiet.getDataVector().toString() + "|" + cmbPhieu.getSelectedIndex() + "|"
                + cmbBoPhan.getSelectedIndex() + "|" + txtNgay.getText() + "|"
                + txaKetQua.getText() + "|" + txaHuHong.getText() + "|" + txaDeXuat.getText();
    }
}
