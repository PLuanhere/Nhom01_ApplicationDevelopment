package ui.screen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import entity.BoPhanNhanVien;
import entity.NhanVien;
import entity.TrangThaiNhanVien;
import ui.component.QuanLyUi;
import ui.component.UiTheme;

/** Form hồ sơ Page 3: hiển thị, kiểm tra đầu vào và gửi hồ sơ cho màn Nhân viên lưu. */
public final class NhanVienFormPanel extends JPanel implements DocumentListener {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DINH_DANG_NGAY =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final Color MAU_LOI = new Color(0xB42318);

    /** Màn hình quản lý việc lưu và hồ sơ đang xem; null khi thêm mới. */
    private final NhanVienPanel manHinh;
    private final NhanVien nhanVien;
    /** Các trường theo thứ tự Tab của form Figma. */
    private final JTextField txtMa = new JTextField();
    private final JTextField txtHoTen = new JTextField();
    private final JTextField txtNgaySinh = new JTextField();
    private final JTextField txtDienThoai = new JTextField();
    private final JTextField txtKinhNghiem = new JTextField("0");
    private final JComboBox<String> cmbGioiTinh = new JComboBox<>(new String[] {"Nam", "Nữ"});
    private final JTextField txtDiaChi = new JTextField();
    private final JComboBox<TrangThaiNhanVien> cmbTrangThai = new JComboBox<>(TrangThaiNhanVien.values());
    private final JComboBox<BoPhanNhanVien> cmbBoPhan = new JComboBox<>(BoPhanNhanVien.values());
    /** Lỗi nằm ngay dưới trường nhập, giữ nguyên chiều cao khi lỗi xuất hiện. */
    private final JLabel lblLoiTen = taoNhanLoi();
    private final JLabel lblLoiNgay = taoNhanLoi();
    private final JLabel lblLoiSo = taoNhanLoi();
    private final JLabel lblLoiNam = taoNhanLoi();
    private final JLabel lblThongBao = taoNhanLoi();
    private final JLabel lblMoTa = QuanLyUi.taoNhan("", 12, false, UiTheme.APP_MUTED);
    private final JButton btnLuu = UiTheme.createAppButton("Lưu hồ sơ nhân viên", true);
    private final JButton btnSua = UiTheme.createAppButton("Chỉnh sửa", true);
    private boolean dangSua;
    private List<Object> giaTriBanDau;

    /**
     * @param manHinh màn hình danh sách và xử lý DAO
     * @param nhanVien hồ sơ cần xem, null để thêm mới
     */
    public NhanVienFormPanel(NhanVienPanel manHinh, NhanVien nhanVien) {
        super(new BorderLayout(0, 14));
        this.manHinh = manHinh;
        this.nhanVien = nhanVien;
        setOpaque(false);
        napHoSo();
        add(taoPhanDau(), BorderLayout.NORTH);
        JScrollPane scrForm = new JScrollPane(taoNoiDung());
        scrForm.setBorder(BorderFactory.createEmptyBorder());
        scrForm.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrForm.getViewport().setBackground(UiTheme.APP_BACKGROUND);
        scrForm.getVerticalScrollBar().setUnitIncrement(24);
        add(scrForm);
        batChinhSua(nhanVien == null);
        giaTriBanDau = layGiaTriNhap();
        ganSuKien();
    }

    /** Gắn các sự kiện nhập, lưu, chỉnh sửa và quay lại sau khi tạo xong giao diện. */
    private void ganSuKien() {
        for (JTextField txt : List.of(txtHoTen, txtNgaySinh, txtDienThoai, txtKinhNghiem)) {
            txt.getDocument().addDocumentListener(this);
        }
        giuChuRoKhiChiDoc(cmbGioiTinh);
        giuChuRoKhiChiDoc(cmbTrangThai);
        giuChuRoKhiChiDoc(cmbBoPhan);
        btnLuu.addActionListener(event -> luuHoSo());
        btnSua.addActionListener(event -> {
            batChinhSua(true);
            txtHoTen.requestFocusInWindow();
        });
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ESCAPE"), "quayLai");
        getActionMap().put("quayLai", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            /** {@inheritDoc} */
            @Override
            public void actionPerformed(ActionEvent event) {
                quayLai();
            }
        });
    }

    /** @return true nếu người dùng đã sửa dữ liệu trong chế độ nhập */
    public boolean coThayDoi() {
        return dangSua && !layGiaTriNhap().equals(giaTriBanDau);
    }

    /** @return cho phép rời form nếu không có thay đổi hoặc người dùng đồng ý bỏ thay đổi */
    public boolean choPhepRoiTrang() {
        if (!coThayDoi()) {
            return true;
        }
        Object[] luaChon = {"Bỏ thay đổi", "Tiếp tục chỉnh sửa"};
        return JOptionPane.showOptionDialog(this, "Hồ sơ có thay đổi chưa lưu.", "Rời hồ sơ nhân viên",
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, luaChon, luaChon[1]) == 0;
    }

    /** Hỏi bỏ thay đổi trước khi quay về danh sách. */
    private void quayLai() {
        if (choPhepRoiTrang()) {
            manHinh.moDanhSach();
        }
    }

    /** Đọc hồ sơ vào form trước khi đăng ký listener để không báo lỗi lúc khởi tạo. */
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

    /**
     * @return breadcrumb và tiêu đề
     */
    private JPanel taoPhanDau() {
        JPanel pnl = new JPanel(new BorderLayout(0, 12));
        pnl.setOpaque(false);
        pnl.add(QuanLyUi.taoNhan("Quản lý / Danh mục / Thông tin nhân viên", 11.5f,
                false, UiTheme.APP_MUTED), BorderLayout.NORTH);
        JPanel pnlTieuDe = new JPanel(new BorderLayout(0, 5));
        pnlTieuDe.setOpaque(false);
        pnlTieuDe.add(QuanLyUi.taoNhan("Thông tin nhân viên", 24, true, UiTheme.APP_TEXT), BorderLayout.NORTH);
        pnlTieuDe.add(lblMoTa);
        JPanel pnlHang = new JPanel(new BorderLayout(12, 0));
        pnlHang.setOpaque(false);
        pnlHang.add(pnlTieuDe);
        JButton btnQuayLai = UiTheme.createAppButton("Danh sách / quay lại", false);
        btnQuayLai.setPreferredSize(new Dimension(170, 36));
        btnQuayLai.addActionListener(event -> quayLai());
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlNut.setOpaque(false);
        pnlNut.add(btnQuayLai);
        pnlHang.add(pnlNut, BorderLayout.EAST);
        pnl.add(pnlHang);
        return pnl;
    }

    /** @return thẻ hồ sơ bám phía trên; cuộn dọc khi cửa sổ nhỏ */
    private JPanel taoNoiDung() {
        JPanel pnl = QuanLyUi.taoThe(new BorderLayout(0, 14));
        pnl.setBorder(BorderFactory.createCompoundBorder(pnl.getBorder(),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        JPanel pnlDau = new JPanel(new BorderLayout(0, 14));
        pnlDau.setOpaque(false);
        pnlDau.add(QuanLyUi.taoNhan("Hồ sơ nhân viên", 18, true, UiTheme.APP_TEXT), BorderLayout.NORTH);
        pnlDau.add(QuanLyUi.taoNhan("Những trường ràng buộc hiển thị ngay khi người dùng nhập.",
                12, false, UiTheme.APP_MUTED));
        pnl.add(pnlDau, BorderLayout.NORTH);
        pnl.add(taoCacTruong());
        pnl.add(taoCuoiForm(), BorderLayout.SOUTH);
        JPanel pnlBoc = new JPanel(new BorderLayout());
        pnlBoc.setOpaque(false);
        pnlBoc.add(pnl, BorderLayout.NORTH);
        // Chiều rộng theo viewport, chiều cao đủ để cuộn tới cuối form.
        pnlBoc.setPreferredSize(new Dimension(0, pnl.getPreferredSize().height));
        return pnlBoc;
    }

    /** @return lưới hai cột theo Figma, bổ sung Bộ phận đã được chấp thuận */
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

    /**
     * @param ten nhãn trường
     * @param truong ô nhập hoặc lựa chọn
     * @param loi nhãn lỗi, null nếu trường chỉ chọn/không có ràng buộc
     * @return nhãn, control và chỗ hiển thị lỗi
     */
    private JPanel taoTruong(String ten, JComponent truong, JLabel loi) {
        JPanel pnl = new JPanel(new BorderLayout(0, 6));
        pnl.setOpaque(false);
        JLabel lbl = QuanLyUi.taoNhan(ten, 12, true, UiTheme.APP_MUTED);
        lbl.setLabelFor(truong);
        pnl.add(lbl, BorderLayout.NORTH);
        truong.setFont(UiTheme.APP_FONT.deriveFont(14.0f));
        truong.setForeground(UiTheme.APP_TEXT);
        truong.setBackground(Color.WHITE);
        truong.setPreferredSize(new Dimension(0, 42));
        truong.setBorder(UiTheme.roundedBorder(UiTheme.APP_BORDER, 5, 0, 10));
        truong.getAccessibleContext().setAccessibleName(ten);
        pnl.add(truong);
        pnl.add(loi == null ? taoNhanLoi() : loi, BorderLayout.SOUTH);
        return pnl;
    }

    /** @return ghi chú kiểm tra, thông báo và các nút hành động */
    private JPanel taoCuoiForm() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setOpaque(false);
        JPanel pnlGhiChu = UiTheme.roundedPanel(new BorderLayout(), new Color(0xFFF7E3), 6);
        pnlGhiChu.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlGhiChu.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        pnlGhiChu.add(QuanLyUi.taoNhan("<html>Nhân viên phải đủ 18 tuổi; điện thoại gồm 10–11 chữ số; "
                + "kinh nghiệm là số nguyên không âm.</html>", 12, false, UiTheme.APP_MUTED));
        pnl.add(pnlGhiChu);
        pnl.add(lblThongBao);
        JLabel lblBatBuoc = QuanLyUi.taoNhan("Những trường có dấu * là bắt buộc · mã tự sinh là chỉ đọc",
                11.5f, false, UiTheme.APP_MUTED);
        lblBatBuoc.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnl.add(lblBatBuoc);
        pnl.add(Box.createVerticalStrut(10));
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlNut.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlNut.setOpaque(false);
        btnLuu.setPreferredSize(new Dimension(440, 44));
        btnSua.setPreferredSize(new Dimension(150, 44));
        pnlNut.add(btnSua);
        pnlNut.add(btnLuu);
        pnl.add(pnlNut);
        return pnl;
    }

    /** @param cmb lựa chọn chỉ đọc cần giữ tương phản chữ như các ô hồ sơ */
    private void giuChuRoKhiChiDoc(JComboBox<?> cmb) {
        cmb.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            /** Windows làm mờ control bị khóa; giá trị hồ sơ vẫn cần đọc rõ. */
            @Override
            protected void paintComponent(Graphics graphics) {
                if (!cmb.isEnabled()) {
                    setForeground(UiTheme.APP_TEXT);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
                super.paintComponent(graphics);
            }
        });
    }

    /** @param sua true để nhập; mã luôn chỉ đọc trong cả ba chế độ */
    private void batChinhSua(boolean sua) {
        dangSua = sua;
        txtMa.setEditable(false);
        txtMa.setFocusable(false);
        txtMa.setBackground(new Color(0xF5F8FC));
        for (JTextField txt : List.of(txtHoTen, txtNgaySinh, txtDienThoai, txtKinhNghiem, txtDiaChi)) {
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

    /** {@inheritDoc} */
    @Override
    public void insertUpdate(DocumentEvent event) { kiemTraTruong(event); }

    /** {@inheritDoc} */
    @Override
    public void removeUpdate(DocumentEvent event) { kiemTraTruong(event); }

    /** {@inheritDoc} */
    @Override
    public void changedUpdate(DocumentEvent event) { kiemTraTruong(event); }

    /** @param event thay đổi của ô nhập; chỉ kiểm tra trường vừa được sửa */
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
    }

    /** @return true nếu tên hợp lệ, lỗi hiện ngay dưới ô tên */
    private boolean kiemTraHoTen() {
        return hienLoi(txtHoTen, lblLoiTen, NhanVien.hoTenHopLe(txtHoTen.getText())
                ? "" : "Vui lòng nhập họ và tên.");
    }

    /** @return true nếu ngày nhập đúng định dạng và nhân viên đủ 18 tuổi */
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

    /** @return true nếu điện thoại có 10–11 chữ số */
    private boolean kiemTraDienThoai() {
        return hienLoi(txtDienThoai, lblLoiSo, NhanVien.dienThoaiHopLe(txtDienThoai.getText())
                ? "" : "Số điện thoại phải gồm 10–11 chữ số.");
    }

    /** @return true nếu kinh nghiệm là số nguyên không âm */
    private boolean kiemTraKinhNghiem() {
        return hienLoi(txtKinhNghiem, lblLoiNam, NhanVien.kinhNghiemHopLe(docKinhNghiem())
                ? "" : "Nhập số năm kinh nghiệm là số nguyên không âm.");
    }

    /**
     * @param txt ô nhập
     * @param lbl nhãn lỗi
     * @param loi thông báo lỗi, trống nếu hợp lệ
     * @return true nếu hợp lệ
     */
    private boolean hienLoi(JTextField txt, JLabel lbl, String loi) {
        boolean hopLe = loi.isBlank();
        lbl.setText(hopLe ? " " : loi);
        lbl.setToolTipText(hopLe ? null : loi);
        txt.setBorder(UiTheme.roundedBorder(hopLe ? UiTheme.APP_BORDER : MAU_LOI, 5, 0, 10));
        return hopLe;
    }

    /**
     * @return ngày theo dd/MM/yyyy, null nếu nhập sai để UI báo lỗi
     */
    private LocalDate docNgaySinh() {
        try {
            return LocalDate.parse(txtNgaySinh.getText().trim(), DINH_DANG_NGAY);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    /**
     * @return số năm nguyên; -1 nếu nhập sai, không được đưa giá trị này xuống DAO
     */
    private int docKinhNghiem() {
        try {
            return Integer.parseInt(txtKinhNghiem.getText().trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    /** @return true nếu cả bốn trường hợp lệ; đưa focus đến lỗi đầu tiên */
    private boolean kiemTraForm() {
        boolean[] hopLe = {
                kiemTraHoTen(), kiemTraNgaySinh(), kiemTraDienThoai(), kiemTraKinhNghiem()};
        JTextField[] truong = {txtHoTen, txtNgaySinh, txtDienThoai, txtKinhNghiem};
        for (int i = 0; i < hopLe.length; i++) {
            if (!hopLe[i]) {
                truong[i].scrollRectToVisible(new Rectangle(0, 0, truong[i].getWidth(), truong[i].getHeight()));
                truong[i].requestFocusInWindow();
                lblThongBao.setText("Vui lòng kiểm tra các trường đang báo lỗi.");
                return false;
            }
        }
        return true;
    }

    /** Lưu một lần qua DAO; lỗi giữ nguyên toàn bộ đầu vào, thành công mới trở về danh sách. */
    private void luuHoSo() {
        if (!dangSua || !btnLuu.isEnabled() || !kiemTraForm()) {
            return;
        }
        btnLuu.setEnabled(false);
        try {
            NhanVien moi = new NhanVien(txtMa.getText(), txtHoTen.getText(), docNgaySinh(),
                    txtDienThoai.getText(), docKinhNghiem(), (String) cmbGioiTinh.getSelectedItem(),
                    txtDiaChi.getText(), (BoPhanNhanVien) cmbBoPhan.getSelectedItem(),
                    (TrangThaiNhanVien) cmbTrangThai.getSelectedItem());
            manHinh.luuNhanVien(moi, nhanVien == null);
            giaTriBanDau = layGiaTriNhap();
            dangSua = false;
        } catch (IllegalArgumentException | SecurityException ex) {
            lblThongBao.setText(ex.getMessage());
            lblThongBao.setToolTipText(ex.getMessage());
        } finally {
            btnLuu.setEnabled(dangSua);
        }
    }

    /** @return các giá trị nhập để phát hiện thay đổi, không sửa dữ liệu nguồn */
    private List<Object> layGiaTriNhap() {
        return Arrays.asList(txtHoTen.getText(), txtNgaySinh.getText(), txtDienThoai.getText(),
                txtKinhNghiem.getText(), cmbGioiTinh.getSelectedItem(), txtDiaChi.getText(),
                cmbTrangThai.getSelectedItem(), cmbBoPhan.getSelectedItem());
    }

    /** @return nhãn lỗi có chiều cao cố định, dùng khoảng trắng khi không có lỗi */
    private static JLabel taoNhanLoi() {
        JLabel lbl = QuanLyUi.taoNhan(" ", 11, false, MAU_LOI);
        lbl.setPreferredSize(new Dimension(0, 18));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }
}
