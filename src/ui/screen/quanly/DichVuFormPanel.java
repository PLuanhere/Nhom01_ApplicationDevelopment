package ui.screen.quanly;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
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
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import entity.DichVu;
import ui.component.QuanLyUi;
import ui.component.UiTheme;

/** Một form cho thêm, xem và sửa; DAO quyết định quyền, mã và dữ liệu được lưu. */
public final class DichVuFormPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    /** Màu thông báo nhập liệu và lỗi lưu. */
    private static final Color MAU_LOI = new Color(0xB42318);
    /** Màn danh sách xử lý DAO; dịch vụ gốc là null khi thêm mới. */
    private final DichVuPanel manHinh;
    private final DichVu dichVu;
    /** Các trường theo thứ tự form; mã luôn chỉ đọc. */
    private final JTextField txtMa = QuanLyUi.taoONhap(false);
    private final JTextField txtTen = QuanLyUi.taoONhap(true);
    private final JTextField txtDonVi = QuanLyUi.taoONhap(true);
    private final JTextField txtGia = QuanLyUi.taoONhap(true);
    private final JTextField txtNhom = QuanLyUi.taoONhap(true);
    private final JTextField txtThoiGian = QuanLyUi.taoONhap(true);
    private final JTextField txtDieuKien = QuanLyUi.taoONhap(true);
    private final JTextField txtMoTa = QuanLyUi.taoONhap(true);
    private final JComboBox<String> cmbTrangThai = new JComboBox<>(new String[] {"Đang áp dụng", "Tạm dừng"});
    /** Lỗi cạnh trường nhập và phản hồi lưu/xóa dưới form. */
    private final JLabel lblLoiTen = taoNhanLoi();
    private final JLabel lblLoiDonVi = taoNhanLoi();
    private final JLabel lblLoiGia = taoNhanLoi();
    private final JLabel lblThongBao = taoNhanLoi();
    /** Nút đổi theo chế độ xem hoặc chỉnh sửa. */
    private final JButton btnSua = QuanLyUi.taoNut("Chỉnh sửa", true);
    private final JButton btnLuu = QuanLyUi.taoNut("Lưu thông tin dịch vụ", true);
    private final JButton btnHuy = QuanLyUi.taoNut("Hủy chỉnh sửa", false);
    private final JButton btnXoa = QuanLyUi.taoNut("Xóa dịch vụ", false);
    /** So sánh với dữ liệu lúc mở để nhận biết thay đổi chưa lưu. */
    private boolean dangSua;
    private List<Object> giaTriBanDau;

    /** @param dichVu null để thêm, bản ghi hiện tại để xem chi tiết */
    public DichVuFormPanel(DichVuPanel manHinh, DichVu dichVu) {
        super(new BorderLayout(0, 14));
        this.manHinh = manHinh;
        this.dichVu = dichVu;
        setOpaque(false);
        QuanLyUi.dinhDangO(cmbTrangThai);
        cmbTrangThai.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override protected void paintComponent(Graphics graphics) {
                if (!cmbTrangThai.isEnabled()) { setForeground(UiTheme.APP_TEXT); }
                setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
                super.paintComponent(graphics);
            }
        });
        txtGia.setToolTipText("Nhập 300000 hoặc 300.000; phần lẻ dùng dấu phẩy, ví dụ 300.000,50.");
        JButton btnQuayLai = QuanLyUi.taoNut("Danh sách / quay lại", false);
        btnQuayLai.addActionListener(event -> quayLai());
        add(QuanLyUi.taoDauTrang("Quản lý / Danh mục / Thông tin dịch vụ / bảng giá",
                "Thông tin dịch vụ / bảng giá", "Điều chỉnh giá và điều kiện không làm thay đổi chứng từ cũ.",
                btnQuayLai), BorderLayout.NORTH);
        add(QuanLyUi.taoVungCuon(taoNoiDung()));
        btnLuu.setPreferredSize(new Dimension(300, 44));
        btnXoa.setForeground(MAU_LOI);
        add(QuanLyUi.taoChanTrang(lblThongBao, btnXoa, btnHuy, btnSua, btnLuu), BorderLayout.SOUTH);
        napDuLieu();
        batChinhSua(dichVu == null);
        btnSua.addActionListener(event -> { batChinhSua(true); txtTen.requestFocusInWindow(); });
        btnLuu.addActionListener(event -> luu());
        btnHuy.addActionListener(event -> {
            if (choPhepRoiTrang()) { napDuLieu(); batChinhSua(false); }
        });
        btnXoa.addActionListener(event -> xoa());
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ESCAPE"), "quayLai");
        getActionMap().put("quayLai", new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override public void actionPerformed(ActionEvent event) { quayLai(); }
        });
    }

    /** Hai cột theo Figma; mô tả tùy chọn bổ sung theo thực thể trong REPORT. */
    private JPanel taoNoiDung() {
        JPanel pnl = QuanLyUi.taoThe(null);
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setBorder(BorderFactory.createCompoundBorder(pnl.getBorder(),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        themDong(pnl, QuanLyUi.taoNhan("Thông tin dịch vụ / bảng giá", 18, true, UiTheme.APP_TEXT));
        themDong(pnl, QuanLyUi.taoNhan("Đơn vị tính và đơn giá là dữ liệu bắt buộc để lập báo giá.",
                12, false, UiTheme.APP_MUTED));
        themDong(pnl, QuanLyUi.taoHang(taoTruong("Mã dịch vụ · tự sinh · chỉ đọc", txtMa, null),
                taoTruong("Tên dịch vụ *", txtTen, lblLoiTen)));
        themDong(pnl, QuanLyUi.taoHang(taoTruong("Đơn vị tính *", txtDonVi, lblLoiDonVi),
                taoTruong("Đơn giá áp dụng * · VND", txtGia, lblLoiGia)));
        themDong(pnl, QuanLyUi.taoHang(taoTruong("Nhóm dịch vụ", txtNhom, null),
                taoTruong("Thời gian dự kiến", txtThoiGian, null)));
        themDong(pnl, QuanLyUi.taoHang(taoTruong("Trạng thái dịch vụ", cmbTrangThai, null),
                taoTruong("Điều kiện áp dụng", txtDieuKien, null)));
        themDong(pnl, taoTruong("Mô tả · tùy chọn", txtMoTa, null));
        themDong(pnl, QuanLyUi.taoLuuY("Đơn giá mới không thay đổi báo giá hoặc hóa đơn đã phát hành. "
                + "Dịch vụ đã có trong báo giá không được xóa; hãy chuyển sang Tạm dừng."));
        themDong(pnl, QuanLyUi.taoNhan("Những trường có dấu * là bắt buộc · mã tự sinh là chỉ đọc",
                11, false, UiTheme.APP_MUTED));
        return pnl;
    }

    /** Thêm dòng vào cột dọc, giữ cùng lề trái. */
    private static void themDong(JPanel pnl, JComponent dong) {
        dong.setAlignmentX(LEFT_ALIGNMENT);
        pnl.add(dong);
        pnl.add(Box.createVerticalStrut(10));
    }

    /** Tạo nhãn, ô nhập và lỗi tùy chọn; không kéo giãn chiều cao ô nhập. */
    private static JPanel taoTruong(String ten, JComponent oNhap, JLabel loi) {
        JPanel pnl = QuanLyUi.taoTruong(ten, oNhap);
        // Giữ ô nhập cao 42 px khi trường bên cạnh có thêm dòng báo lỗi.
        JPanel pnlO = new JPanel(new BorderLayout());
        pnlO.setOpaque(false);
        pnlO.add(oNhap, BorderLayout.NORTH);
        pnl.add(pnlO);
        if (loi != null) { pnl.add(loi, BorderLayout.SOUTH); }
        return pnl;
    }

    /** @return chỗ báo lỗi, ẩn khi dữ liệu chưa có lỗi */
    private static JLabel taoNhanLoi() {
        JLabel lbl = QuanLyUi.taoNhan(" ", 11, false, MAU_LOI);
        lbl.setVisible(false);
        return lbl;
    }

    /** Đọc bản ghi gốc vào form, đồng thời đặt lại mốc kiểm tra thay đổi. */
    private void napDuLieu() {
        txtMa.setText(dichVu == null ? manHinh.layMaMoi() : dichVu.getMaDichVu());
        if (dichVu != null) {
            txtTen.setText(dichVu.getTenDichVu());
            txtDonVi.setText(dichVu.getDonViTinh());
            NumberFormat tien = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
            tien.setMaximumFractionDigits(Math.max(0, dichVu.getDonGia().scale()));
            txtGia.setText(tien.format(dichVu.getDonGia()));
            txtNhom.setText(dichVu.getNhomDichVu());
            txtThoiGian.setText(dichVu.getThoiGianDuKien());
            txtDieuKien.setText(dichVu.getDieuKienApDung());
            txtMoTa.setText(dichVu.getMoTa());
            cmbTrangThai.setSelectedIndex(dichVu.isDangApDung() ? 0 : 1);
        }
        giaTriBanDau = layGiaTri();
        xoaLoi();
    }

    /** @param choSua bật các ô nhập; mã dịch vụ luôn giữ chỉ đọc */
    private void batChinhSua(boolean choSua) {
        dangSua = choSua;
        for (JTextField txt : List.of(txtTen, txtDonVi, txtGia, txtNhom, txtThoiGian, txtDieuKien, txtMoTa)) {
            txt.setEditable(choSua);
            txt.setBackground(choSua ? Color.WHITE : new Color(0xF5F7FB));
        }
        cmbTrangThai.setEnabled(choSua);
        btnLuu.setVisible(choSua);
        btnSua.setVisible(!choSua);
        btnHuy.setVisible(choSua && dichVu != null);
        btnXoa.setVisible(!choSua && dichVu != null);
        revalidate();
        repaint();
    }

    /** @return các giá trị đang nhập để kiểm tra dữ liệu chưa lưu */
    private List<Object> layGiaTri() {
        return List.of(txtTen.getText(), txtDonVi.getText(), txtGia.getText(), txtNhom.getText(),
                txtThoiGian.getText(), txtDieuKien.getText(), txtMoTa.getText(), cmbTrangThai.getSelectedIndex());
    }

    /** @return có thay đổi chưa lưu trong chế độ sửa */
    public boolean coThayDoi() { return dangSua && !layGiaTri().equals(giaTriBanDau); }

    /** @return chỉ rời form sau khi xử lý thay đổi chưa lưu */
    public boolean choPhepRoiTrang() {
        if (!coThayDoi()) { return true; }
        Object[] luaChon = {"Bỏ thay đổi", "Tiếp tục chỉnh sửa"};
        return JOptionPane.showOptionDialog(this, "Thông tin dịch vụ có thay đổi chưa lưu.", "Rời dịch vụ",
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, luaChon, luaChon[1]) == 0;
    }

    /** Quay về danh sách sau khi xử lý thay đổi chưa lưu. */
    private void quayLai() { if (choPhepRoiTrang()) { manHinh.moDanhSach(); } }

    /** Chỉ nhận số tiền kiểu Việt Nam; không tự bỏ chữ/ký hiệu hoặc làm tròn dữ liệu. */
    static BigDecimal docDonGia(String chu) {
        String gia = chu.trim();
        if (!gia.matches("(?:[0-9]+|[0-9]{1,3}(?:[.][0-9]{3})+)(?:,[0-9]+)?")) {
            throw new IllegalArgumentException("Nhập giá không âm: 300000 hoặc 300.000; phần lẻ dùng dấu phẩy.");
        }
        return new BigDecimal(gia.replace(".", "").replace(',', '.'));
    }

    /** Xóa phản hồi cũ trước khi kiểm tra hoặc khôi phục dữ liệu. */
    private void xoaLoi() {
        for (JLabel lbl : List.of(lblLoiTen, lblLoiDonVi, lblLoiGia)) {
            lbl.setText(" ");
            lbl.setVisible(false);
        }
        lblThongBao.setText(" ");
        lblThongBao.setVisible(false);
    }

    /** Kiểm tra đầu vào, xác nhận tạm dừng và gửi dữ liệu cho DAO qua màn danh sách. */
    private void luu() {
        xoaLoi();
        JComponent oLoi = null;
        if (txtTen.getText().isBlank()) {
            lblLoiTen.setText("Nhập tên dịch vụ.");
            lblLoiTen.setVisible(true);
            oLoi = txtTen;
        }
        if (txtDonVi.getText().isBlank()) {
            lblLoiDonVi.setText("Nhập đơn vị tính.");
            lblLoiDonVi.setVisible(true);
            if (oLoi == null) { oLoi = txtDonVi; }
        }
        BigDecimal gia = null;
        try { gia = docDonGia(txtGia.getText()); }
        catch (IllegalArgumentException ex) {
            lblLoiGia.setText("Giá không hợp lệ. Ví dụ: 300000 hoặc 300.000,50.");
            lblLoiGia.setVisible(true);
            if (oLoi == null) { oLoi = txtGia; }
        }
        if (oLoi != null) { revalidate(); oLoi.requestFocusInWindow(); return; }
        if (dichVu != null && dichVu.isDangApDung() && cmbTrangThai.getSelectedIndex() == 1
                && JOptionPane.showConfirmDialog(this, "Tạm dừng dịch vụ " + txtMa.getText()
                        + "? Báo giá đã lập vẫn được giữ nguyên.", "Tạm dừng dịch vụ",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            DichVu moi = new DichVu(txtMa.getText(), txtTen.getText(), txtNhom.getText(), txtDonVi.getText(),
                    gia, txtMoTa.getText(), txtThoiGian.getText(), txtDieuKien.getText(),
                    cmbTrangThai.getSelectedIndex() == 0);
            manHinh.luu(dichVu, moi);
        } catch (IllegalArgumentException | SecurityException ex) { hienLoi(ex); }
    }

    /** Chỉ yêu cầu xóa sau khi người dùng xác nhận; DAO kiểm tra tham chiếu. */
    private void xoa() {
        if (JOptionPane.showConfirmDialog(this, "Xóa dịch vụ " + dichVu.getMaDichVu() + "? "
                + "Chỉ dịch vụ chưa có trong báo giá mới được xóa.", "Xóa dịch vụ",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.OK_OPTION) { return; }
        try { manHinh.xoa(dichVu); }
        catch (IllegalArgumentException | SecurityException ex) { hienLoi(ex); }
    }

    /** Hiện lỗi tại form và giữ toàn bộ dữ liệu đang nhập. */
    private void hienLoi(RuntimeException ex) {
        lblThongBao.setText(ex.getMessage());
        lblThongBao.setToolTipText(ex.getMessage());
        lblThongBao.setVisible(true);
        revalidate();
    }
}
