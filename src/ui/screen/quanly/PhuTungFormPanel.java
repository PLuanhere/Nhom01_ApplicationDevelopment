package ui.screen.quanly;

import entity.phutung.PhuTung;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ui.component.QuanLyFormPanel;
import ui.component.UiChung;
import ui.component.UiTheme;

/** Biểu mẫu xem, thêm và sửa thông tin danh mục phụ tùng. */
public final class PhuTungFormPanel extends QuanLyFormPanel implements ActionListener {
    private static final long serialVersionUID = 1L;
    private final PhuTungPanel manHinh;
    private final PhuTung phuTung;

    // Các ô nhập và lựa chọn trên biểu mẫu.
    private final JTextField txtMa = UiChung.taoONhap(true);
    private final JTextField txtTen = UiChung.taoONhap(true);
    private final JTextField txtLoai = UiChung.taoONhap(true);
    private final JComboBox<String> cmbDonVi = new JComboBox<>(new String[] { "cái", "lít", "bộ" });
    private final JTextField txtGiaNhap = UiChung.taoONhap(true);
    private final JTextField txtGiaBan = UiChung.taoONhap(true);
    private final JTextField txtToiThieu = UiChung.taoONhap(true);
    private final JComboBox<String> cmbTrangThai = new JComboBox<>(
            new String[] { "Đang hoạt động", "Ngừng hoạt động" });

    // Các nhãn báo lỗi và mô tả trạng thái biểu mẫu.
    private final JLabel lblLoi = UiChung.taoNhan("", 12, false, new Color(0xB42318));

    // Các nút thực hiện thao tác của màn hình.
    private final JButton btnLuu = UiChung.taoNut("Lưu phụ tùng", true);
    private final JButton btnSua = UiChung.taoNut("Chỉnh sửa", true);
    private final JButton btnHuy = UiChung.taoNut("Hủy", false);
    private final JButton btnNhatKy = UiChung.taoNut("Nhật ký phụ tùng", false);
    private boolean dangSua;
    private String[] giaTriBanDau;

    /** Tạo biểu mẫu phụ tùng, nạp dữ liệu và gắn sự kiện xem, sửa, lưu, hủy. */
    public PhuTungFormPanel(PhuTungPanel manHinh, PhuTung phuTung) {
        super();
        this.manHinh = manHinh;
        this.phuTung = phuTung;
        UiChung.dinhDangO(cmbDonVi);
        UiChung.dinhDangO(cmbTrangThai);
        UiChung.giuChuRo(cmbDonVi);
        UiChung.giuChuRo(cmbTrangThai);
        JButton btnVe = UiChung.taoNut("Danh sách / quay lại", false);
        btnVe.setActionCommand("ve");
        btnVe.addActionListener(this);
        // Tiêu đề và điều hướng quay lại danh sách.
        add(UiChung.taoDauTrang("Quản lý / Danh mục / Thông tin phụ tùng", "Thông tin phụ tùng",
                "Khai báo danh mục phụ tùng, đơn vị tính và ngưỡng tồn.", btnVe), BorderLayout.NORTH);
        JPanel pnlThe = UiChung.taoThe(null);
        pnlThe.setLayout(new BoxLayout(pnlThe, BoxLayout.Y_AXIS));
        pnlThe.setBorder(BorderFactory.createCompoundBorder(pnlThe.getBorder(),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        dong(pnlThe, UiChung.taoNhan("Thông tin phụ tùng", 18, true, UiTheme.APP_TEXT));
        dong(pnlThe, UiChung.taoNhan("Mã và đơn vị cố định sau khi tạo; giá không thay đổi chứng từ cũ.", 12, false,
                UiTheme.APP_MUTED));
        dong(pnlThe, UiChung.taoHang(UiChung.taoTruong("Mã phụ tùng *", txtMa),
                UiChung.taoTruong("Tên phụ tùng *", txtTen)));
        dong(pnlThe, UiChung.taoHang(UiChung.taoTruong("Loại phụ tùng", txtLoai),
                UiChung.taoTruong("Đơn vị tính *", cmbDonVi)));
        dong(pnlThe, UiChung.taoHang(UiChung.taoTruong("Giá nhập * · VND", txtGiaNhap),
                UiChung.taoTruong("Giá bán * · VND", txtGiaBan)));
        dong(pnlThe, UiChung.taoHang(UiChung.taoTruong("Tồn tối thiểu *", txtToiThieu),
                UiChung.taoTruong("Trạng thái", cmbTrangThai)));
        dong(pnlThe, UiChung.taoLuuY(
                "Thêm phụ tùng bắt đầu với tồn 0. " + "Nhập kho và đối soát được ghi nhận bằng chứng từ riêng."));
        // Vùng thông tin có cuộn khi chiều cao cửa sổ không đủ.
        add(UiChung.taoVungCuon(pnlThe));
        // Thanh phản hồi và các nút thao tác nằm dưới biểu mẫu.
        add(UiChung.taoChanTrang(lblLoi, btnNhatKy, btnHuy, btnSua, btnLuu), BorderLayout.SOUTH);
        napDuLieu();
        batSua(phuTung == null);
        btnNhatKy.setActionCommand("nhatKy");
        btnNhatKy.addActionListener(this);
        btnSua.setActionCommand("sua");
        btnSua.addActionListener(this);
        btnLuu.setActionCommand("luu");
        btnLuu.addActionListener(this);
        btnHuy.setActionCommand("huy");
        btnHuy.addActionListener(this);
    }

    /** Thêm một hàng thành phần UI và khoảng cách vào bố cục dọc. */
    private static void dong(JPanel pnl, JComponent truong) {
        truong.setAlignmentX(LEFT_ALIGNMENT);
        pnl.add(truong);
        pnl.add(Box.createVerticalStrut(12));
    }

    /** Đưa dữ liệu gốc vào các trường và đặt lại mốc kiểm tra thay đổi. */
    private void napDuLieu() {
        if (phuTung == null) {
            txtGiaNhap.setText("0");
            txtGiaBan.setText("0");
            txtToiThieu.setText("0");
        } else {
            txtMa.setText(phuTung.getMaPhuTung());
            txtTen.setText(phuTung.getTenPhuTung());
            txtLoai.setText(phuTung.getLoaiPhuTung());
            cmbDonVi.setSelectedItem(phuTung.getDonViTinh());
            txtGiaNhap.setText(UiChung.so(phuTung.getGiaNhap()));
            txtGiaBan.setText(UiChung.so(phuTung.getGiaBan()));
            txtToiThieu.setText(UiChung.so(phuTung.getMucToiThieu()));
            cmbTrangThai.setSelectedIndex(phuTung.isHoatDong() ? 0 : 1);
        }
        giaTriBanDau = giaTri();
        lblLoi.setVisible(false);
    }

    /** Bật hoặc khóa các trường và nút theo chế độ xem hoặc chỉnh sửa. */
    private void batSua(boolean sua) {
        dangSua = sua;
        for (JTextField txt : new JTextField[] { txtTen, txtLoai, txtGiaNhap, txtGiaBan, txtToiThieu }) {
            txt.setEditable(sua);
            txt.setBackground(sua ? Color.WHITE : new Color(0xF5F7FB));
        }
        txtMa.setEditable(sua && phuTung == null);
        txtMa.setBackground(txtMa.isEditable() ? Color.WHITE : new Color(0xF5F7FB));
        cmbDonVi.setEnabled(sua && phuTung == null);
        cmbTrangThai.setEnabled(sua);
        btnLuu.setVisible(sua);
        btnSua.setVisible(!sua);
        btnHuy.setVisible(sua);
        btnNhatKy.setVisible(!sua && phuTung != null);
        revalidate();
        repaint();
    }

    /** Lấy các giá trị đang nhập để phát hiện thay đổi chưa lưu. */
    private String[] giaTri() {
        return new String[] { txtMa.getText(), txtTen.getText(), txtLoai.getText(),
                String.valueOf(cmbDonVi.getSelectedItem()), txtGiaNhap.getText(), txtGiaBan.getText(),
                txtToiThieu.getText(), String.valueOf(cmbTrangThai.getSelectedIndex()) };
    }

    /** Kiểm tra dữ liệu chưa lưu và xác nhận trước khi rời màn hình. */
    public boolean choPhepRoiTrang() {
        return UiChung.hoiBoThayDoi(this, dangSua && UiChung.daThayDoi(giaTriBanDau, giaTri()));
    }

    /** Kiểm tra thay đổi chưa lưu rồi quay về danh sách. */
    private void quayLai() {
        if (choPhepRoiTrang()) {
            manHinh.moDanhSach();
        }
    }

    /** Kiểm tra các ô nhập, tạo phụ tùng và gửi màn danh sách lưu qua DAO. */
    private void luu() {
        try {
            PhuTung moi = new PhuTung(txtMa.getText(), txtTen.getText(), txtLoai.getText(),
                    String.valueOf(cmbDonVi.getSelectedItem()), UiChung.docTien(txtGiaNhap.getText(), "Giá nhập"),
                    UiChung.docTien(txtGiaBan.getText(), "Giá bán"),
                    UiChung.docSo(txtToiThieu.getText(), "Tồn tối thiểu"), cmbTrangThai.getSelectedIndex() == 0);
            manHinh.luu(phuTung, moi);
        } catch (IllegalArgumentException | SecurityException ex) {
            lblLoi.setText(ex.getMessage());
            lblLoi.setToolTipText(ex.getMessage());
            lblLoi.setVisible(true);
            revalidate();
        }
    }

    /** Thực hiện thao tác tương ứng với nút, bộ lọc hoặc phím vừa chọn. */
    @Override
    public void actionPerformed(ActionEvent event) {
        String lenh = event.getActionCommand();
        if ("ve".equals(lenh)) {
            quayLai();
        } else if ("nhatKy".equals(lenh)) {
            manHinh.moNhatKy(phuTung.getMaPhuTung());
        } else if ("sua".equals(lenh)) {
            batSua(true);
            txtTen.requestFocusInWindow();
        } else if ("luu".equals(lenh)) {
            luu();
        } else if ("huy".equals(lenh)) {
            if (choPhepRoiTrang()) {
                if (phuTung == null) {
                    manHinh.moDanhSach();
                } else {
                    napDuLieu();
                    batSua(false);
                }
            }
        }
    }
}
