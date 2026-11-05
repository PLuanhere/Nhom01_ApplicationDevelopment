package ui.screen.quanly;

import entity.NhanVien.BoPhanNhanVien;
import entity.NhanVien;
import entity.NhanVien.TrangThaiNhanVien;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ui.component.QuanLyFormPanel;
import ui.component.UiChung;
import ui.component.UiTheme;

/** Biểu mẫu xem, thêm và sửa hồ sơ nhân viên. */
public final class NhanVienFormPanel extends QuanLyFormPanel implements DocumentListener, ActionListener {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final Color MAU_LOI = new Color(0xB42318);

    private final NhanVienPanel manHinh;
    private final NhanVien nhanVien;

    // Các ô nhập và lựa chọn trên biểu mẫu.
    private final JTextField txtMa = new JTextField();
    private final JTextField txtHoTen = new JTextField();
    private final JTextField txtNgaySinh = new JTextField();
    private final JTextField txtDienThoai = new JTextField();
    private final JTextField txtKinhNghiem = new JTextField("0");
    private final JComboBox<String> cmbGioiTinh = new JComboBox<>(new String[] { "Nam", "Nữ" });
    private final JTextField txtDiaChi = new JTextField();
    private final JComboBox<TrangThaiNhanVien> cmbTrangThai = new JComboBox<>(TrangThaiNhanVien.values());
    private final JComboBox<BoPhanNhanVien> cmbBoPhan = new JComboBox<>(BoPhanNhanVien.values());

    // Các nhãn báo lỗi và mô tả trạng thái biểu mẫu.
    private final JLabel lblLoiTen = taoNhanLoi();
    private final JLabel lblLoiNgay = taoNhanLoi();
    private final JLabel lblLoiSo = taoNhanLoi();
    private final JLabel lblLoiNam = taoNhanLoi();
    private final JLabel lblThongBao = taoNhanLoi();
    private final JLabel lblMoTa = UiChung.taoNhan("", 12, false, UiTheme.APP_MUTED);

    // Các nút thực hiện thao tác của màn hình.
    private final JButton btnLuu = UiChung.taoNut("Lưu hồ sơ nhân viên", true);
    private final JButton btnSua = UiChung.taoNut("Chỉnh sửa", true);
    private boolean dangSua;
    private String[] giaTriBanDau;

    /** Tạo biểu mẫu nhân viên, nạp hồ sơ và gắn sự kiện kiểm tra, lưu. */
    public NhanVienFormPanel(NhanVienPanel manHinh, NhanVien nhanVien) {
        super();
        this.manHinh = manHinh;
        this.nhanVien = nhanVien;
        napHoSo();
        add(taoPhanDau(), BorderLayout.NORTH);
        // Vùng thông tin có cuộn khi chiều cao cửa sổ không đủ.
        add(UiChung.taoVungCuon(taoNoiDung()));
        add(taoCuoiForm(), BorderLayout.SOUTH);
        batChinhSua(nhanVien == null);
        giaTriBanDau = layGiaTriNhap();
        ganSuKien();
    }

    /** Gắn sự kiện nhập liệu, chỉnh sửa, lưu và quay lại cho biểu mẫu. */
    private void ganSuKien() {
        for (JTextField txt : new JTextField[] { txtHoTen, txtNgaySinh, txtDienThoai, txtKinhNghiem }) {
            txt.getDocument().addDocumentListener(this);
        }
        giuChuRoKhiChiDoc(cmbGioiTinh);
        giuChuRoKhiChiDoc(cmbTrangThai);
        giuChuRoKhiChiDoc(cmbBoPhan);
        btnLuu.setActionCommand("luu");
        btnLuu.addActionListener(this);
        btnSua.setActionCommand("sua");
        btnSua.addActionListener(this);
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

    /** So sánh dữ liệu nhập với lúc mở form khi đang ở chế độ chỉnh sửa. */
    public boolean coThayDoi() {
        return dangSua && UiChung.daThayDoi(giaTriBanDau, layGiaTriNhap());
    }

    /** Kiểm tra dữ liệu chưa lưu và xác nhận trước khi rời màn hình. */
    public boolean choPhepRoiTrang() {
        if (!coThayDoi()) {
            return true;
        }
        Object[] luaChon = { "Bỏ thay đổi", "Tiếp tục chỉnh sửa" };
        return JOptionPane.showOptionDialog(this, "Hồ sơ có thay đổi chưa lưu.", "Rời hồ sơ nhân viên",
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, luaChon, luaChon[1]) == 0;
    }

    /** Kiểm tra thay đổi chưa lưu rồi quay về danh sách. */
    private void quayLai() {
        if (choPhepRoiTrang()) {
            manHinh.moDanhSach();
        }
    }

    /** Đưa hồ sơ nhân viên vào form hoặc chuẩn bị mã cho hồ sơ mới. */
    private void napHoSo() {
        txtMa.setText(nhanVien == null ? manHinh.layMaMoi() : nhanVien.getMaNhanVien());
        if (nhanVien != null) {
            txtHoTen.setText(nhanVien.getHoTen());
            txtNgaySinh.setText(nhanVien.getNgaySinh().format(DINH_DANG_NGAY));
            txtDienThoai.setText(nhanVien.getSoDienThoai());
            txtKinhNghiem.setText(String.valueOf(nhanVien.getSoNamKinhNghiem()));
            cmbGioiTinh.setSelectedItem(nhanVien.getGioiTinh());
            txtDiaChi.setText(nhanVien.getDiaChi());
            cmbTrangThai.setSelectedItem(nhanVien.getTrangThai());
            cmbBoPhan.setSelectedItem(nhanVien.getBoPhan());
        }
    }

    /** Tạo breadcrumb, tiêu đề và nút quay lại hoặc thao tác của trang. */
    private JPanel taoPhanDau() {
        JButton btnQuayLai = UiChung.taoNut("Danh sách / quay lại", false);
        btnQuayLai.setActionCommand("quayLai");
        btnQuayLai.addActionListener(this);
        return UiChung.taoDauTrang("Quản lý / Danh mục / Thông tin nhân viên", "Thông tin nhân viên",
                "Xem và cập nhật hồ sơ, bộ phận và tình trạng làm việc.", btnQuayLai);
    }

    /** Tạo thẻ chứa các trường thông tin và lưu ý của biểu mẫu. */
    private JPanel taoNoiDung() {
        JPanel pnl = UiChung.taoThe(new BorderLayout(0, 16));
        pnl.setBorder(
                BorderFactory.createCompoundBorder(pnl.getBorder(), BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        pnl.add(UiChung.taoNhan("Hồ sơ nhân viên", 18, true, UiTheme.APP_TEXT), BorderLayout.NORTH);
        pnl.add(taoCacTruong());
        pnl.add(UiChung.taoLuuY("Những trường có dấu * là bắt buộc. Nhân viên phải đủ 18 tuổi; "
                + "điện thoại gồm 10–11 chữ số; kinh nghiệm là số nguyên không âm."), BorderLayout.SOUTH);
        return pnl;
    }

    /** Tạo lưới nhập hồ sơ nhân viên gồm thông tin cá nhân, trạng thái và bộ phận. */
    private JPanel taoCacTruong() {
        JPanel pnl = new JPanel(new GridLayout(0, 2, 14, 10));
        pnl.setOpaque(false);
        pnl.add(taoTruong("Mã nhân viên · tự sinh · chỉ đọc", txtMa, null));
        pnl.add(taoTruong("Họ và tên *", txtHoTen, lblLoiTen));
        pnl.add(taoTruong("Ngày sinh * · từ 18 tuổi (dd/MM/yyyy)", txtNgaySinh, lblLoiNgay));
        pnl.add(taoTruong("Số điện thoại * · 10–11 chữ số", txtDienThoai, lblLoiSo));
        pnl.add(taoTruong("Số năm kinh nghiệm * · không âm", txtKinhNghiem, lblLoiNam));
        pnl.add(taoTruong("Giới tính", cmbGioiTinh, null));
        pnl.add(taoTruong("Địa chỉ", txtDiaChi, null));
        pnl.add(taoTruong("Trạng thái làm việc", cmbTrangThai, null));
        pnl.add(taoTruong("Bộ phận", cmbBoPhan, null));
        pnl.add(Box.createGlue());
        return pnl;
    }

    /** Ghép nhãn, ô nhập và vùng báo lỗi nếu cần. */
    private JPanel taoTruong(String ten, JComponent truong, JLabel loi) {
        UiChung.dinhDangO(truong);
        JPanel pnl = UiChung.taoTruong(ten, truong);
        JPanel pnlO = new JPanel(new BorderLayout());
        pnlO.setOpaque(false);
        pnlO.add(truong, BorderLayout.NORTH);
        pnl.add(pnlO);
        pnl.add(loi == null ? taoNhanLoi() : loi, BorderLayout.SOUTH);
        return pnl;
    }

    /** Tạo thanh thông báo và nút chỉnh sửa, lưu hồ sơ nhân viên. */
    private JPanel taoCuoiForm() {
        return UiChung.taoChanTrang(lblThongBao, btnSua, btnLuu);
    }

    /** Giữ chữ trong hộp chọn dễ đọc khi form ở chế độ chỉ xem. */
    private void giuChuRoKhiChiDoc(JComboBox<?> cmb) {
        UiChung.giuChuRo(cmb);
    }

    /** Bật hoặc khóa các trường và nút theo chế độ xem hoặc chỉnh sửa. */
    private void batChinhSua(boolean sua) {
        dangSua = sua;
        txtMa.setEditable(false);
        txtMa.setFocusable(false);
        txtMa.setBackground(new Color(0xF5F8FC));
        for (JTextField txt : new JTextField[] { txtHoTen, txtNgaySinh, txtDienThoai, txtKinhNghiem, txtDiaChi }) {
            txt.setEditable(sua);
            txt.setBackground(sua ? Color.WHITE : new Color(0xF5F8FC));
        }
        cmbGioiTinh.setEnabled(sua);
        cmbTrangThai.setEnabled(sua);
        cmbBoPhan.setEnabled(sua);
        btnLuu.setVisible(sua);
        btnSua.setVisible(!sua);
        lblMoTa.setText(sua ? (nhanVien == null ? "Thêm hồ sơ nhân viên." : "Chỉnh sửa hồ sơ nhân viên.")
                : "Xem chi tiết hồ sơ nhân viên.");
        revalidate();
        repaint();
    }

    /** Kiểm tra trường vừa nhập hoặc xóa và cập nhật lỗi trên biểu mẫu. */
    @Override
    public void insertUpdate(DocumentEvent event) {
        kiemTraTruong(event);
    }

    /** Kiểm tra trường vừa nhập hoặc xóa và cập nhật lỗi trên biểu mẫu. */
    @Override
    public void removeUpdate(DocumentEvent event) {
        kiemTraTruong(event);
    }

    /** Kiểm tra trường vừa nhập hoặc xóa và cập nhật lỗi trên biểu mẫu. */
    @Override
    public void changedUpdate(DocumentEvent event) {
        kiemTraTruong(event);
    }

    /** Kiểm tra trường nhân viên vừa thay đổi và cập nhật thông báo lỗi. */
    private void kiemTraTruong(DocumentEvent event) {
        if (!dangSua) {

            return;
        }
        if (event.getDocument() == txtHoTen.getDocument()) {
            kiemTraHoTen();
        } else if (event.getDocument() == txtNgaySinh.getDocument()) {
            kiemTraNgaySinh();
        } else if (event.getDocument() == txtDienThoai.getDocument()) {
            kiemTraDienThoai();
        } else {
            kiemTraKinhNghiem();
        }
        lblThongBao.setText(" ");
        lblThongBao.setVisible(false);
    }

    /** Kiểm tra họ tên và hiển thị lỗi ngay dưới ô nhập. */
    private boolean kiemTraHoTen() {
        return hienLoi(txtHoTen, lblLoiTen, NhanVien.hoTenHopLe(txtHoTen.getText()) ? "" : "Vui lòng nhập họ và tên.");
    }

    /** Kiểm tra ngày sinh, điều kiện đủ 18 tuổi và hiển thị lỗi. */
    private boolean kiemTraNgaySinh() {
        LocalDate ngay = docNgaySinh();
        String loi = "";
        if (ngay == null) {
            loi = "Nhập ngày hợp lệ theo dd/MM/yyyy.";
        } else if (!NhanVien.ngaySinhHopLe(ngay)) {
            loi = "Nhân viên phải đủ 18 tuổi.";
        }
        return hienLoi(txtNgaySinh, lblLoiNgay, loi);
    }

    /** Kiểm tra số điện thoại và hiển thị lỗi ngay dưới ô nhập. */
    private boolean kiemTraDienThoai() {
        return hienLoi(txtDienThoai, lblLoiSo,
                NhanVien.dienThoaiHopLe(txtDienThoai.getText()) ? "" : "Số điện thoại phải gồm 10–11 chữ số.");
    }

    /** Kiểm tra số năm kinh nghiệm nguyên không âm và hiển thị lỗi. */
    private boolean kiemTraKinhNghiem() {
        return hienLoi(txtKinhNghiem, lblLoiNam,
                NhanVien.kinhNghiemHopLe(docKinhNghiem()) ? "" : "Nhập số năm kinh nghiệm là số nguyên không âm.");
    }

    /** Hiển thị lỗi nhập hoặc lưu để người dùng sửa lại dữ liệu. */
    private boolean hienLoi(JTextField txt, JLabel lbl, String loi) {
        boolean hopLe = loi.isBlank();
        lbl.setText(hopLe ? " " : loi);
        lbl.setToolTipText(hopLe ? null : loi);
        txt.setBorder(UiTheme.roundedBorder(hopLe ? UiTheme.APP_BORDER : MAU_LOI, 5, 0, 10));
        return hopLe;
    }

    /** Đọc ngày sinh theo dd/MM/yyyy; trả null nếu không hợp lệ. */
    private LocalDate docNgaySinh() {
        try {
            return LocalDate.parse(txtNgaySinh.getText().trim(), DINH_DANG_NGAY);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    /** Đọc số năm kinh nghiệm; trả giá trị lỗi khi không phải số nguyên. */
    private int docKinhNghiem() {
        try {
            return Integer.parseInt(txtKinhNghiem.getText().trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    /** Kiểm tra toàn bộ hồ sơ và đưa focus đến trường lỗi đầu tiên. */
    private boolean kiemTraForm() {
        boolean[] hopLe = { kiemTraHoTen(), kiemTraNgaySinh(), kiemTraDienThoai(), kiemTraKinhNghiem() };
        JTextField[] truong = { txtHoTen, txtNgaySinh, txtDienThoai, txtKinhNghiem };
        for (int i = 0; i < hopLe.length; i++) {
            if (!hopLe[i]) {
                truong[i].scrollRectToVisible(new Rectangle(0, 0, truong[i].getWidth(), truong[i].getHeight()));
                truong[i].requestFocusInWindow();
                lblThongBao.setText("Vui lòng kiểm tra các trường đang báo lỗi.");
                lblThongBao.setVisible(true);
                revalidate();
                return false;
            }
        }
        return true;
    }

    /** Tạo hồ sơ từ dữ liệu hợp lệ rồi gửi màn Nhân viên lưu qua DAO. */
    private void luuHoSo() {
        if (!dangSua || !btnLuu.isEnabled() || !kiemTraForm()) {

            return;
        }
        btnLuu.setEnabled(false);
        try {
            NhanVien moi = new NhanVien(txtMa.getText(), txtHoTen.getText(), docNgaySinh(), txtDienThoai.getText(),
                    docKinhNghiem(), (String) cmbGioiTinh.getSelectedItem(), txtDiaChi.getText(),
                    (BoPhanNhanVien) cmbBoPhan.getSelectedItem(), (TrangThaiNhanVien) cmbTrangThai.getSelectedItem());
            manHinh.luuNhanVien(moi, nhanVien == null);
            giaTriBanDau = layGiaTriNhap();
            dangSua = false;
        } catch (IllegalArgumentException | SecurityException ex) {
            lblThongBao.setText(ex.getMessage());
            lblThongBao.setToolTipText(ex.getMessage());
            lblThongBao.setVisible(true);
            revalidate();
        } finally {
            btnLuu.setEnabled(dangSua);
        }
    }

    /** Lấy các giá trị đang nhập để phát hiện thay đổi chưa lưu. */
    private String[] layGiaTriNhap() {
        return new String[] { txtHoTen.getText(), txtNgaySinh.getText(), txtDienThoai.getText(),
                txtKinhNghiem.getText(), String.valueOf(cmbGioiTinh.getSelectedItem()), txtDiaChi.getText(),
                String.valueOf(cmbTrangThai.getSelectedItem()), String.valueOf(cmbBoPhan.getSelectedItem()) };
    }

    /** Tạo nhãn hiển thị lỗi kiểm tra đầu vào. */
    private static JLabel taoNhanLoi() {
        JLabel lbl = UiChung.taoNhan(" ", 11, false, MAU_LOI);
        lbl.setPreferredSize(new Dimension(0, 18));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    /** Thực hiện thao tác tương ứng với nút, bộ lọc hoặc phím vừa chọn. */
    @Override
    public void actionPerformed(ActionEvent event) {
        String lenh = event.getActionCommand();
        if ("luu".equals(lenh)) {
            luuHoSo();
        } else if ("sua".equals(lenh)) {
            batChinhSua(true);
            txtHoTen.requestFocusInWindow();
        } else if ("quayLai".equals(lenh)) {
            quayLai();
        }
    }
}
