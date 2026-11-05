package ui.screen.quanly;

import entity.DichVu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import ui.component.QuanLyFormPanel;
import ui.component.UiChung;
import ui.component.UiTheme;

/** Biểu mẫu xem, thêm, sửa và xóa dịch vụ theo điều kiện nghiệp vụ. */
public final class DichVuFormPanel extends QuanLyFormPanel implements ActionListener {

    private static final long serialVersionUID = 1L;

    private static final Color MAU_LOI = new Color(0xB42318);

    private final DichVuPanel manHinh;
    private final DichVu dichVu;

    // Các ô nhập và lựa chọn trên biểu mẫu.
    private final JTextField txtMa = UiChung.taoONhap(false);
    private final JTextField txtTen = UiChung.taoONhap(true);
    private final JTextField txtDonVi = UiChung.taoONhap(true);
    private final JTextField txtGia = UiChung.taoONhap(true);
    private final JTextField txtNhom = UiChung.taoONhap(true);
    private final JTextField txtThoiGian = UiChung.taoONhap(true);
    private final JTextField txtDieuKien = UiChung.taoONhap(true);
    private final JTextField txtMoTa = UiChung.taoONhap(true);
    private final JComboBox<String> cmbTrangThai = new JComboBox<>(new String[] { "Đang áp dụng", "Tạm dừng" });

    // Các nhãn báo lỗi và mô tả trạng thái biểu mẫu.
    private final JLabel lblLoiTen = taoNhanLoi();
    private final JLabel lblLoiDonVi = taoNhanLoi();
    private final JLabel lblLoiGia = taoNhanLoi();
    private final JLabel lblThongBao = taoNhanLoi();

    // Các nút thực hiện thao tác của màn hình.
    private final JButton btnSua = UiChung.taoNut("Chỉnh sửa", true);
    private final JButton btnLuu = UiChung.taoNut("Lưu thông tin dịch vụ", true);
    private final JButton btnHuy = UiChung.taoNut("Hủy chỉnh sửa", false);
    private final JButton btnXoa = UiChung.taoNut("Xóa dịch vụ", false);

    private boolean dangSua;
    private String[] giaTriBanDau;

    /** Tạo biểu mẫu dịch vụ, nạp dữ liệu và gắn sự kiện lưu, xóa, hủy. */
    public DichVuFormPanel(DichVuPanel manHinh, DichVu dichVu) {
        super();
        this.manHinh = manHinh;
        this.dichVu = dichVu;
        UiChung.dinhDangO(cmbTrangThai);
        UiChung.giuChuRo(cmbTrangThai);
        txtGia.setToolTipText("Nhập 300000 hoặc 300.000; giá là số nguyên đồng, ví dụ 300.000.");
        JButton btnQuayLai = UiChung.taoNut("Danh sách / quay lại", false);
        btnQuayLai.setActionCommand("quayLai");
        btnQuayLai.addActionListener(this);
        // Tiêu đề và điều hướng quay lại danh sách.
        add(UiChung.taoDauTrang("Quản lý / Danh mục / Thông tin dịch vụ / bảng giá", "Thông tin dịch vụ / bảng giá",
                "Điều chỉnh giá và điều kiện không làm thay đổi chứng từ cũ.", btnQuayLai), BorderLayout.NORTH);
        // Vùng thông tin có cuộn khi chiều cao cửa sổ không đủ.
        add(UiChung.taoVungCuon(taoNoiDung()));
        btnXoa.setForeground(MAU_LOI);
        // Thanh phản hồi và các nút thao tác nằm dưới biểu mẫu.
        add(UiChung.taoChanTrang(lblThongBao, btnXoa, btnHuy, btnSua, btnLuu), BorderLayout.SOUTH);
        napDuLieu();
        batChinhSua(dichVu == null);
        btnSua.setActionCommand("sua");
        btnSua.addActionListener(this);
        btnLuu.setActionCommand("luu");
        btnLuu.addActionListener(this);
        btnHuy.setActionCommand("huy");
        btnHuy.addActionListener(this);
        btnXoa.setActionCommand("xoa");
        btnXoa.addActionListener(this);
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ESCAPE"), "quayLai");
        getActionMap().put("quayLai", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            /** Thực hiện thao tác tương ứng với nút, bộ lọc hoặc phím vừa chọn. */
            @Override
            public void actionPerformed(ActionEvent event) {
                quayLai();
            }
        });
    }

    /** Tạo thẻ chứa các trường thông tin và lưu ý của biểu mẫu. */
    private JPanel taoNoiDung() {
        JPanel pnl = UiChung.taoThe(null);
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setBorder(
                BorderFactory.createCompoundBorder(pnl.getBorder(), BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        themDong(pnl, UiChung.taoNhan("Thông tin dịch vụ / bảng giá", 18, true, UiTheme.APP_TEXT));
        themDong(pnl, UiChung.taoNhan("Đơn vị tính và đơn giá là dữ liệu bắt buộc để lập báo giá.", 12, false,
                UiTheme.APP_MUTED));
        themDong(pnl, UiChung.taoHang(taoTruong("Mã dịch vụ · tự sinh · chỉ đọc", txtMa, null),
                taoTruong("Tên dịch vụ *", txtTen, lblLoiTen)));
        themDong(pnl, UiChung.taoHang(taoTruong("Đơn vị tính *", txtDonVi, lblLoiDonVi),
                taoTruong("Đơn giá áp dụng * · VND", txtGia, lblLoiGia)));
        themDong(pnl, UiChung.taoHang(taoTruong("Nhóm dịch vụ", txtNhom, null),
                taoTruong("Thời gian dự kiến", txtThoiGian, null)));
        themDong(pnl, UiChung.taoHang(taoTruong("Trạng thái dịch vụ", cmbTrangThai, null),
                taoTruong("Điều kiện áp dụng", txtDieuKien, null)));
        themDong(pnl, taoTruong("Mô tả · tùy chọn", txtMoTa, null));
        themDong(pnl, UiChung.taoLuuY("Đơn giá mới không thay đổi báo giá hoặc hóa đơn đã phát hành. "
                + "Dịch vụ đã có trong báo giá không được xóa; hãy chuyển sang Tạm dừng."));
        themDong(pnl, UiChung.taoNhan("Những trường có dấu * là bắt buộc · mã tự sinh là chỉ đọc", 11, false,
                UiTheme.APP_MUTED));
        return pnl;
    }

    /** Thêm một hàng thành phần UI và khoảng cách vào bố cục dọc. */
    private static void themDong(JPanel pnl, JComponent dong) {
        dong.setAlignmentX(LEFT_ALIGNMENT);
        pnl.add(dong);
        pnl.add(Box.createVerticalStrut(10));
    }

    /** Ghép nhãn, ô nhập và vùng báo lỗi nếu cần. */
    private static JPanel taoTruong(String ten, JComponent oNhap, JLabel loi) {
        JPanel pnl = UiChung.taoTruong(ten, oNhap);

        JPanel pnlO = new JPanel(new BorderLayout());
        pnlO.setOpaque(false);
        pnlO.add(oNhap, BorderLayout.NORTH);
        pnl.add(pnlO);
        if (loi != null) {
            pnl.add(loi, BorderLayout.SOUTH);
        }
        return pnl;
    }

    /** Tạo nhãn hiển thị lỗi kiểm tra đầu vào. */
    private static JLabel taoNhanLoi() {
        JLabel lbl = UiChung.taoNhan(" ", 11, false, MAU_LOI);
        lbl.setVisible(false);
        return lbl;
    }

    /** Đưa dữ liệu gốc vào các trường và đặt lại mốc kiểm tra thay đổi. */
    private void napDuLieu() {
        txtMa.setText(dichVu == null ? manHinh.layMaMoi() : dichVu.getMaDichVu());
        if (dichVu != null) {
            txtTen.setText(dichVu.getTenDichVu());
            txtDonVi.setText(dichVu.getDonViTinh());
            txtGia.setText(UiChung.so(dichVu.getDonGia()));
            txtNhom.setText(dichVu.getNhomDichVu());
            txtThoiGian.setText(dichVu.getThoiGianDuKien());
            txtDieuKien.setText(dichVu.getDieuKienApDung());
            txtMoTa.setText(dichVu.getMoTa());
            cmbTrangThai.setSelectedIndex(dichVu.isDangApDung() ? 0 : 1);
        }
        giaTriBanDau = layGiaTri();
        xoaLoi();
    }

    /** Bật hoặc khóa các trường và nút theo chế độ xem hoặc chỉnh sửa. */
    private void batChinhSua(boolean choSua) {
        dangSua = choSua;
        for (JTextField txt : new JTextField[] { txtTen, txtDonVi, txtGia, txtNhom, txtThoiGian, txtDieuKien,
                txtMoTa }) {
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

    /** Lấy các giá trị đang nhập để phát hiện thay đổi chưa lưu. */
    private String[] layGiaTri() {
        return new String[] { txtTen.getText(), txtDonVi.getText(), txtGia.getText(), txtNhom.getText(),
                txtThoiGian.getText(), txtDieuKien.getText(), txtMoTa.getText(),
                String.valueOf(cmbTrangThai.getSelectedIndex()) };
    }

    /** So sánh dữ liệu nhập với lúc mở form khi đang ở chế độ chỉnh sửa. */
    public boolean coThayDoi() {
        return dangSua && UiChung.daThayDoi(giaTriBanDau, layGiaTri());
    }

    /** Kiểm tra dữ liệu chưa lưu và xác nhận trước khi rời màn hình. */
    public boolean choPhepRoiTrang() {
        if (!coThayDoi()) {
            return true;
        }
        Object[] luaChon = { "Bỏ thay đổi", "Tiếp tục chỉnh sửa" };
        return JOptionPane.showOptionDialog(this, "Thông tin dịch vụ có thay đổi chưa lưu.", "Rời dịch vụ",
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, luaChon, luaChon[1]) == 0;
    }

    /** Kiểm tra thay đổi chưa lưu rồi quay về danh sách. */
    private void quayLai() {
        if (choPhepRoiTrang()) {
            manHinh.moDanhSach();
        }
    }

    /** Đọc đơn giá nguyên đồng từ nội dung nhập theo định dạng Việt Nam. */
    static long docDonGia(String chu) {
        return UiChung.docTien(chu, "Đơn giá");
    }

    /** Xóa thông báo kiểm tra cũ trước khi nhập lại hoặc lưu dữ liệu. */
    private void xoaLoi() {
        for (JLabel lbl : new JLabel[] { lblLoiTen, lblLoiDonVi, lblLoiGia }) {
            lbl.setText(" ");
            lbl.setVisible(false);
        }
        lblThongBao.setText(" ");
        lblThongBao.setVisible(false);
    }

    /** Kiểm tra dữ liệu dịch vụ, xác nhận tạm dừng nếu cần rồi gửi DAO lưu. */
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
            if (oLoi == null) {
                oLoi = txtDonVi;
            }
        }
        long gia = 0;
        try {
            gia = docDonGia(txtGia.getText());
        } catch (IllegalArgumentException ex) {
            lblLoiGia.setText("Giá không hợp lệ. Ví dụ: 300000 hoặc 300.000.");
            lblLoiGia.setVisible(true);
            if (oLoi == null) {
                oLoi = txtGia;
            }
        }
        if (oLoi != null) {
            revalidate();
            oLoi.requestFocusInWindow();
            return;
        }
        if (dichVu != null && dichVu.isDangApDung() && cmbTrangThai.getSelectedIndex() == 1
                && JOptionPane.showConfirmDialog(this,
                        "Tạm dừng dịch vụ " + txtMa.getText() + "? Báo giá đã lập vẫn được giữ nguyên.",
                        "Tạm dừng dịch vụ", JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE) != JOptionPane.OK_OPTION) {

            return;
        }
        try {
            DichVu moi = new DichVu(txtMa.getText(), txtTen.getText(), txtNhom.getText(), txtDonVi.getText(), gia,
                    txtMoTa.getText(), txtThoiGian.getText(), txtDieuKien.getText(),
                    cmbTrangThai.getSelectedIndex() == 0);
            manHinh.luu(dichVu, moi);
        } catch (IllegalArgumentException | SecurityException ex) {
            hienLoi(ex);
        }
    }

    /** Xác nhận xóa dịch vụ và gọi DAO kiểm tra điều kiện trước khi thực hiện. */
    private void xoa() {
        if (JOptionPane.showConfirmDialog(this,
                "Xóa dịch vụ " + dichVu.getMaDichVu() + "? " + "Chỉ dịch vụ chưa có trong báo giá mới được xóa.",
                "Xóa dịch vụ", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            manHinh.xoa(dichVu);
        } catch (IllegalArgumentException | SecurityException ex) {
            hienLoi(ex);
        }
    }

    /** Hiển thị lỗi nhập hoặc lưu để người dùng sửa lại dữ liệu. */
    private void hienLoi(RuntimeException ex) {
        lblThongBao.setText(ex.getMessage());
        lblThongBao.setToolTipText(ex.getMessage());
        lblThongBao.setVisible(true);
        revalidate();
    }

    /** Thực hiện thao tác tương ứng với nút, bộ lọc hoặc phím vừa chọn. */
    @Override
    public void actionPerformed(ActionEvent event) {
        String lenh = event.getActionCommand();
        if ("quayLai".equals(lenh)) {
            quayLai();
        } else if ("sua".equals(lenh)) {
            batChinhSua(true);
            txtTen.requestFocusInWindow();
        } else if ("luu".equals(lenh)) {
            luu();
        } else if ("huy".equals(lenh)) {
            if (choPhepRoiTrang()) {
                napDuLieu();
                batChinhSua(false);
            }
        } else if ("xoa".equals(lenh)) {
            xoa();
        }
    }
}
