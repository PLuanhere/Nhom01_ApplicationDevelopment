package ui.screen.quanly;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import dao.PhuTungDAO;

import entity.PhuTung;
import entity.TaiKhoan;

import ui.component.QuanLyUi;
import ui.component.QuanLyFormPanel;
import ui.component.UiTheme;

/** Biểu mẫu kiểm kê, tính chênh lệch và xác nhận điều chỉnh tồn kho. */
public final class DoiSoatKhoPanel extends QuanLyFormPanel implements DocumentListener, ActionListener {
    private static final long serialVersionUID = 1L;
    private final PhuTungPanel manHinh;
    private final TaiKhoan taiKhoan;
    private final PhuTungDAO dao;
    private final PhuTung phuTung;
    private final double tonDaDoc;

    // Các ô nhập và lựa chọn trên biểu mẫu.
    private final JTextField txtKiemKe = QuanLyUi.taoONhap(true);
    private final JTextField txtChenhLech = QuanLyUi.taoONhap(false);
    private final JTextField txtTonSau = QuanLyUi.taoONhap(false);
    private final JTextField txtLyDo = QuanLyUi.taoONhap(true);
    private final JTextField txtDienGiai = QuanLyUi.taoONhap(true);

    // Các nhãn báo lỗi và mô tả trạng thái biểu mẫu.
    private final JLabel lblLoi = QuanLyUi.taoNhan("", 12, false, new Color(0xB42318));
    private final String[] giaTriBanDau;

    /** Đọc tồn hiện tại và tạo biểu mẫu kiểm kê, tính chênh lệch, xác nhận điều chỉnh. */
    public DoiSoatKhoPanel(PhuTungPanel manHinh, TaiKhoan taiKhoan, PhuTungDAO dao, String ma) {
        super();
        this.manHinh = manHinh;
        this.taiKhoan = taiKhoan;
        this.dao = dao;
        phuTung = dao.timTheoMa(taiKhoan, ma);
        tonDaDoc = dao.layTon(taiKhoan, ma);
        JButton btnVe = QuanLyUi.taoNut("Danh sách / quay lại", false);
        btnVe.setActionCommand("ve");
        btnVe.addActionListener(this);
        // Tiêu đề và điều hướng quay lại danh sách.
        add(QuanLyUi.taoDauTrang("Quản lý / Đối soát & điều chỉnh tồn kho", "Đối soát & điều chỉnh tồn kho",
            "Điều chỉnh tồn kho có lý do và nhật ký; kiểm tra số cũ / số mới trước khi xác nhận.", btnVe),
            BorderLayout.NORTH);
        JPanel pnl = QuanLyUi.taoThe(null);
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setBorder(BorderFactory.createCompoundBorder(pnl.getBorder(),
            BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        dong(pnl, QuanLyUi.taoNhan("Đối soát và điều chỉnh tồn kho", 18, true, UiTheme.APP_TEXT));
        dong(pnl, QuanLyUi.taoHang(doc("Mã phụ tùng * · chỉ đọc", phuTung.toString()),
            doc("Tồn hiện tại · chỉ đọc", QuanLyUi.so(tonDaDoc) + " " + phuTung.getDonViTinh())));
        dong(pnl, QuanLyUi.taoHang(QuanLyUi.taoTruong("Số lượng kiểm kê *", txtKiemKe),
            QuanLyUi.taoTruong("Chênh lệch · tự tính · chỉ đọc", txtChenhLech)));
        dong(pnl, QuanLyUi.taoHang(QuanLyUi.taoTruong("Số lượng sau điều chỉnh · chỉ đọc", txtTonSau),
            QuanLyUi.taoTruong("Lý do điều chỉnh *", txtLyDo)));
        dong(pnl, QuanLyUi.taoHang(QuanLyUi.taoTruong("Chứng từ / diễn giải", txtDienGiai),
            doc("Người thực hiện · chỉ đọc", taiKhoan.getTenDangNhap())));
        dong(pnl, QuanLyUi.taoLuuY("Không tạo biến động nếu không có chênh lệch. "
            + "Xác nhận số cũ / số mới; số tồn đã đổi thì phải đọc lại trước khi điều chỉnh."));
        // Vùng thông tin có cuộn khi chiều cao cửa sổ không đủ.
        add(QuanLyUi.taoVungCuon(pnl));
        JButton btnLuu = QuanLyUi.taoNut("Xác nhận điều chỉnh tồn", true);
        btnLuu.setActionCommand("luu");
        btnLuu.addActionListener(this);
        // Thanh phản hồi và các nút thao tác nằm dưới biểu mẫu.
        add(QuanLyUi.taoChanTrang(lblLoi, btnLuu), BorderLayout.SOUTH);
        txtKiemKe.setText(QuanLyUi.so(tonDaDoc));
        capNhatChenhLech();
        giaTriBanDau = giaTri();
        txtKiemKe.getDocument().addDocumentListener(this);
    }

    /** Tạo trường chỉ đọc có nhãn để hiển thị thông tin đối soát. */
    private static JPanel doc(String ten, String chu) {
        JTextField txt = QuanLyUi.taoONhap(false); txt.setText(chu);
        return QuanLyUi.taoTruong(ten, txt);
    }

    /** Thêm một hàng thành phần UI và khoảng cách vào bố cục dọc. */
    private static void dong(JPanel pnl, JComponent truong) {
        truong.setAlignmentX(LEFT_ALIGNMENT); pnl.add(truong); pnl.add(Box.createVerticalStrut(12));
    }

    /** Lấy các giá trị đang nhập để phát hiện thay đổi chưa lưu. */
    private String[] giaTri() {
        return new String[] {
            txtKiemKe.getText(),
            txtLyDo.getText(),
            txtDienGiai.getText()};
    }

    /** Kiểm tra dữ liệu chưa lưu và xác nhận trước khi rời màn hình. */
    public boolean choPhepRoiTrang() { return QuanLyUi.hoiBoThayDoi(this, QuanLyUi.daThayDoi(giaTriBanDau, giaTri())); }
    /** Kiểm tra thay đổi chưa lưu rồi quay về danh sách. */
    private void quayLai() { if (choPhepRoiTrang()) { manHinh.moDanhSach(); } }

    /** Tính lại chênh lệch và tồn sau từ số lượng kiểm kê đang nhập. */
    private void capNhatChenhLech() {
        try {
            double so = QuanLyUi.docSo(txtKiemKe.getText(), "Số lượng kiểm kê");
            double lech = Math.round((so - tonDaDoc) * 1000) / 1000.0;
            txtTonSau.setText(QuanLyUi.so(so) + " " + phuTung.getDonViTinh());
            txtChenhLech.setText((lech > 0 ? "+" : "") + QuanLyUi.so(lech) + " " + phuTung.getDonViTinh());
        } catch (IllegalArgumentException ex) { txtTonSau.setText("—"); txtChenhLech.setText("—"); }
    }

    /** Kiểm tra số kiểm kê, yêu cầu xác nhận rồi ghi điều chỉnh tồn qua DAO. */
    private void luu() {
        try {
            double so = QuanLyUi.docSo(txtKiemKe.getText(), "Số lượng kiểm kê");
            if (so > 0) { phuTung.kiemTraSoLuong(so); }
            if (txtLyDo.getText().isBlank()) { throw new IllegalArgumentException("Nhập lý do điều chỉnh."); }
            if (so == tonDaDoc) { throw new IllegalArgumentException("Không có chênh lệch để điều chỉnh."); }
            if (JOptionPane.showConfirmDialog(this,
                phuTung + "\nTồn cũ: " + QuanLyUi.so(tonDaDoc) + "\nTồn mới: " + QuanLyUi.so(so)
                + "\nLý do: " + txtLyDo.getText(), "Xác nhận điều chỉnh tồn",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.OK_OPTION) { return; }
            dao.dieuChinh(taiKhoan, phuTung, tonDaDoc, so, txtLyDo.getText(), txtDienGiai.getText());
            manHinh.moDanhSach();
            manHinh.thongBao("Đã điều chỉnh tồn " + phuTung.getMaPhuTung() + " và ghi nhật ký.");
        } catch (IllegalArgumentException | SecurityException ex) {
            lblLoi.setText(ex.getMessage()); lblLoi.setToolTipText(ex.getMessage()); lblLoi.setVisible(true); revalidate();
        }
    }

    /** Tính lại chênh lệch và tồn sau khi ô kiểm kê thay đổi. */
    @Override public void insertUpdate(DocumentEvent event) { capNhatChenhLech(); }
    /** Tính lại chênh lệch và tồn sau khi ô kiểm kê thay đổi. */
    @Override public void removeUpdate(DocumentEvent event) { capNhatChenhLech(); }
    /** Tính lại chênh lệch và tồn sau khi ô kiểm kê thay đổi. */
    @Override public void changedUpdate(DocumentEvent event) { capNhatChenhLech(); }

    /** Thực hiện thao tác tương ứng với nút, bộ lọc hoặc phím vừa chọn. */
    @Override public void actionPerformed(ActionEvent event) {
        String lenh = event.getActionCommand();
        if ("ve".equals(lenh)) {
            quayLai();
        } else if ("luu".equals(lenh)) {
            luu();
        }
    }
}
