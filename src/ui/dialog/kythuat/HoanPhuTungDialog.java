package ui.dialog.kythuat;

import dao.HoanPhuTungDAO;
import entity.TaiKhoan;
import java.awt.*;
import java.awt.event.*;
import java.util.UUID;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import ui.component.GiaoDienKyThuat;

// Hiển thị lần xuất gốc, chứng từ hoàn và biểu mẫu xác nhận phụ tùng thực tế đã trả kho.
public final class HoanPhuTungDialog extends JDialog implements ActionListener {
    private static final long serialVersionUID = 1L;
    private final HoanPhuTungDAO dao = new HoanPhuTungDAO();
    private final TaiKhoan taiKhoan;
    private final String maLenh;
    private final DefaultTableModel modelXuat = GiaoDienKyThuat.taoModel(
            "Lần xuất", "Hạng mục số", "Mã phụ tùng", "Tên phụ tùng", "Đơn vị", "Đã xuất", "Đã hoàn", "Còn thực dùng");
    private final DefaultTableModel modelHoan = GiaoDienKyThuat.taoModel(
            "Phiếu hoàn", "Thời điểm", "Người ghi", "Lần xuất gốc", "Mã phụ tùng", "Số lượng", "Lý do");
    private final JTable tblXuat = GiaoDienKyThuat.taoBang(modelXuat);
    private final JTable tblHoan = GiaoDienKyThuat.taoBang(modelHoan);
    private final JTextField txtSo = GiaoDienKyThuat.taoONhap("", false);
    private final JTextField txtLyDo = GiaoDienKyThuat.taoONhap("", false);
    private final JCheckBox chkDaTra = new JCheckBox("Đã bàn giao lại phụ tùng còn sử dụng được cho kho");
    private final JLabel lblThongBao = GiaoDienKyThuat.taoNhan("Chọn lần xuất cần hoàn trong bảng trên.", false);
    private final boolean duocHoan;
    private String maThaoTac;
    private String noiDungGui;

    public HoanPhuTungDialog(Window chu, TaiKhoan tk, String maLenh, boolean duocHoan) {
        super(chu, "Hoàn / điều chỉnh phụ tùng · " + maLenh, ModalityType.APPLICATION_MODAL);
        this.taiKhoan = tk;
        this.maLenh = maLenh;
        this.duocHoan = duocHoan;
        JPanel pnl = new JPanel(new BorderLayout(0, 12));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        pnl.add(GiaoDienKyThuat.taoNhan("Lệnh " + maLenh + " · Giữ nguyên lịch sử xuất, hoàn bằng chứng từ mới", true),
                BorderLayout.NORTH);
        JPanel cot = GiaoDienKyThuat.taoCot();
        GiaoDienKyThuat.themThanhPhan(cot, GiaoDienKyThuat.taoNhan("Các lần xuất phụ tùng", true));
        GiaoDienKyThuat.themThanhPhan(cot, bocBang(tblXuat));
        JPanel form = new JPanel(new GridLayout(0, 2, 12, 8));
        form.setOpaque(false);
        form.add(GiaoDienKyThuat.taoTruong("Số lượng thực tế trả kho *", txtSo));
        form.add(GiaoDienKyThuat.taoTruong("Lý do hoàn / ghi nhầm *", txtLyDo));
        GiaoDienKyThuat.themThanhPhan(cot, form);
        chkDaTra.setFont(GiaoDienKyThuat.CHU_THUONG);
        chkDaTra.setOpaque(false);
        GiaoDienKyThuat.themThanhPhan(cot, chkDaTra);
        JButton btnHoan = GiaoDienKyThuat.taoNut("Xác nhận hoàn kho", "HOAN", true, this);
        btnHoan.setEnabled(duocHoan);
        txtSo.setEditable(duocHoan);
        txtLyDo.setEditable(duocHoan);
        chkDaTra.setEnabled(duocHoan);
        GiaoDienKyThuat.themThanhPhan(cot, GiaoDienKyThuat.taoHangNut(btnHoan));
        GiaoDienKyThuat.themThanhPhan(cot, GiaoDienKyThuat.taoThongBao(duocHoan
                ? "Chỉ hoàn lượng đã trả kho, không hoàn phụ tùng đã tiêu hao hoặc bị hỏng. "
                    + "Nếu ghi thiếu, dùng biểu mẫu ghi nhận thêm. Nếu đổi loại, cần khách duyệt loại mới trước."
                : "Lệnh đã hoàn thành: chỉ xem lịch sử. Không điều chỉnh tồn từ màn hình này.", true));
        GiaoDienKyThuat.themThanhPhan(cot, GiaoDienKyThuat.taoNhan("Lịch sử chứng từ hoàn", true));
        GiaoDienKyThuat.themThanhPhan(cot, bocBang(tblHoan));
        pnl.add(new JScrollPane(cot), BorderLayout.CENTER);
        JPanel cuoi = new JPanel(new BorderLayout(12, 0));
        cuoi.setOpaque(false);
        cuoi.add(lblThongBao, BorderLayout.CENTER);
        cuoi.add(GiaoDienKyThuat.taoNut("Đóng", "DONG", false, this), BorderLayout.EAST);
        pnl.add(cuoi, BorderLayout.SOUTH);
        setContentPane(pnl);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        Dimension man = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(1250, man.width - 60), Math.min(850, man.height - 80));
        setLocationRelativeTo(chu);
        nap();
    }

    // Bọc bảng trong vùng cuộn vừa số dòng, tối đa năm dòng trước khi cuộn.
    private JScrollPane bocBang(JTable bang) {
        JScrollPane cuon = new JScrollPane(bang);
        bang.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        bang.getTableHeader().setReorderingAllowed(false);
        for (int i = 0; i < bang.getColumnCount(); i++) {
            bang.getColumnModel().getColumn(i).setPreferredWidth(i == 3 || i == 6 ? 220 : 135);
        }
        cuon.setPreferredSize(new Dimension(1120, 220));
        return cuon;
    }

    // Đọc lại lần xuất và lịch sử hoàn từ SQL sau mỗi thao tác.
    private void nap() {
        modelXuat.setRowCount(0);
        for (String[] dong : dao.docLanXuat(taiKhoan, maLenh)) {
            modelXuat.addRow(dong);
        }
        modelHoan.setRowCount(0);
        for (String[] dong : dao.docLichSu(taiKhoan, maLenh)) {
            modelHoan.addRow(dong);
        }
        capNhatChieuCao(tblXuat);
        capNhatChieuCao(tblHoan);
    }

    // Giữ bảng ngắn đủ hiển thị tất cả dòng khi số hồ sơ ít.
    private void capNhatChieuCao(JTable bang) {
        JScrollPane cuon = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, bang);
        int soDong = Math.max(1, Math.min(5, bang.getRowCount()));
        cuon.setPreferredSize(new Dimension(1120,
                bang.getTableHeader().getPreferredSize().height + soDong * bang.getRowHeight() + 24));
        cuon.revalidate();
    }

    // Xử lý xác nhận hoàn kho hoặc đóng cửa sổ và thông báo lỗi nhập liệu.
    @Override
    public void actionPerformed(ActionEvent e) {
        if ("DONG".equals(e.getActionCommand())) {
            dispose();
            return;
        }
        try {
            hoan();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Chưa hoàn được phụ tùng", JOptionPane.WARNING_MESSAGE);
        }
    }

    // Xác nhận lần xuất và số lượng trước khi tạo chứng từ hoàn kho.
    private void hoan() {
        if (!duocHoan) {
            throw new IllegalStateException("Lệnh đã hoàn thành.");
        }
        int dong = tblXuat.getSelectedRow();
        if (dong < 0) {
            throw new IllegalArgumentException("Chọn một lần xuất trong bảng trên.");
        }
        dong = tblXuat.convertRowIndexToModel(dong);
        String ma = modelXuat.getValueAt(dong, 0).toString();
        double so;
        try {
            so = Double.parseDouble(txtSo.getText().trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Nhập số lượng hoàn hợp lệ.");
        }
        if (!chkDaTra.isSelected()) {
            throw new IllegalArgumentException("Cần xác nhận phụ tùng thực tế đã được trả kho.");
        }
        if (txtLyDo.getText().isBlank()) {
            throw new IllegalArgumentException("Nhập lý do hoàn phụ tùng.");
        }
        String noiDung = ma + "|" + so + "|" + txtLyDo.getText().trim();
        if (!noiDung.equals(noiDungGui)) {
            maThaoTac = "HK-" + UUID.randomUUID().toString().replace("-", "");
            noiDungGui = noiDung;
        }
        int chon = JOptionPane.showConfirmDialog(this,
                "Hoàn " + so + " từ " + ma + "?\nTồn kho tăng, lượng thực dùng giảm tương ứng.\n"
                    + "Lý do: " + txtLyDo.getText().trim(),
                "Xác nhận hoàn kho", JOptionPane.YES_NO_OPTION);
        if (chon != JOptionPane.YES_OPTION) {
            return;
        }
        dao.hoan(taiKhoan, maLenh, ma, so, txtLyDo.getText(), maThaoTac);
        lblThongBao.setText("Đã ghi chứng từ " + maThaoTac);
        txtSo.setText("");
        txtLyDo.setText("");
        chkDaTra.setSelected(false);
        nap();
    }
}
