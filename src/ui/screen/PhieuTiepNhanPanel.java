package ui.screen;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;
import dao.KhachHangDAO;
import dao.TrangChuTiepNhanDAO;
import entity.KyThuatVienPhanCong;
import dao.XeDAO;
import dao.PhieuTiepNhanDAO;
import entity.PhieuTiepNhan;
import entity.TrangThaiTiepNhan;
import java.util.UUID;
import entity.KhachHang;
import entity.TaiKhoan;
import entity.VaiTro;
import entity.Xe;
import ui.component.UiTheme;

/** Nhập, đối chiếu và lưu phiếu vào kho chung trong phiên tiếp nhận. */
public final class PhieuTiepNhanPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter
            .ofPattern("dd/MM/uuuu HH:mm", Locale.ROOT).withResolverStyle(ResolverStyle.STRICT);
    private final KhachHangDAO khachHangDAO;
    private final XeDAO xeDAO;
    private final PhieuTiepNhanDAO phieuDAO;
    private TrangChuTiepNhanDAO tienDoDAO;
    /** Nội dung đã đọc lại với khách, vô hiệu khi quay về chỉnh sửa. */
    private Xe xeDoiChieu;
    private KhachHang khachDoiChieu;
    private long kmDoiChieu;
    private LocalDateTime ngayDoiChieu;
    private String maThaoTac;
    private final Supplier<TaiKhoan> phien;
    private final CardLayout boCuc = new CardLayout();
    private final JPanel pnlThe = new JPanel(boCuc);
    private final JTextField txtLoc = new JTextField();
    private final JComboBox<String> cboTrangThai = new JComboBox<>(new String[] {
            "Tất cả trạng thái", "Chờ kiểm tra"});
    private final DefaultTableModel modelDanhSach = model("Mã phiếu", "Biển số", "Khách hàng", "Ngày nhận", "Yêu cầu", "Trạng thái");
    private final JTable tblDanhSach = bang(modelDanhSach);
    private final JLabel lblDanhSach = nhan(" ", false);
    private final JTextField txtTraCuu = new JTextField();
    private final DefaultTableModel modelHoSo = model("Mã xe", "Biển số", "Mã khách", "Khách hàng", "SĐT");
    private final JTable tblHoSo = bang(modelHoSo);
    private final JLabel lblHoSo = nhan("Tra cứu và chọn đúng phương tiện trước khi nhập phiếu.", false);
    private final JTextField txtKhach = new JTextField();
    private final JTextField txtXe = new JTextField();
    private final JTextField txtKm = new JTextField();
    private final JTextField txtNgay = new JTextField();
    private final JTextArea txtTinhTrang = vungChu();
    private final JTextArea txtTaiSan = vungChu();
    private final JTextArea txtYeuCau = vungChu();
    private final JLabel lblLoi = nhan(" ", false);
    private final JTextArea txtDoiChieu = vungChu();
    private final JCheckBox chkXacNhan = new JCheckBox("Khách đã xem và đồng ý thông tin tiếp nhận");
    private final JButton btnHoanTat = UiTheme.createAppButton("Hoàn tất tiếp nhận", true);
    private final JLabel lblXacNhan = nhan("Chưa xác nhận · chưa lưu phiếu.", false);
    private String maXeDaChon;
    private boolean dangNap;
    private boolean daThayDoi;

    /** Hai kho dùng chung với màn khách/xe; mọi thao tác UI gọi trên EDT. */
    public PhieuTiepNhanPanel(KhachHangDAO khachHangDAO, XeDAO xeDAO, Supplier<TaiKhoan> phien) {
        this(khachHangDAO, xeDAO, new PhieuTiepNhanDAO(khachHangDAO, xeDAO), phien);
    }

    public PhieuTiepNhanPanel(KhachHangDAO khachHangDAO, XeDAO xeDAO,
            PhieuTiepNhanDAO phieuDAO, Supplier<TaiKhoan> phien) {
        super(new BorderLayout());
        this.phieuDAO = Objects.requireNonNull(phieuDAO);
        this.khachHangDAO = Objects.requireNonNull(khachHangDAO);
        this.xeDAO = Objects.requireNonNull(xeDAO);
        this.phien = Objects.requireNonNull(phien);
        kiemTraPhien();
        setOpaque(false); pnlThe.setOpaque(false);
        pnlThe.add(taoDanhSach(), "DANH_SACH");
        pnlThe.add(taoForm(), "FORM");
        pnlThe.add(taoDoiChieu(), "DOI_CHIEU");
        add(pnlThe);
        locDanhSach();
    }

    public PhieuTiepNhanPanel(KhachHangDAO khachHangDAO, XeDAO xeDAO, PhieuTiepNhanDAO phieuDAO,
            Supplier<TaiKhoan> phien, TrangChuTiepNhanDAO tienDoDAO) {
        this(khachHangDAO, xeDAO, phieuDAO, phien);
        this.tienDoDAO = tienDoDAO;
        if (tienDoDAO != null) {
            for (String ten : new String[] {"Đang kiểm tra", "Đã kiểm tra", "Nháp", "Chờ khách xác nhận", "Đã đồng ý", "Yêu cầu điều chỉnh", "Từ chối", "Chờ sửa chữa", "Đang sửa", "Chờ phụ tùng", "Hoàn thành"}) cboTrangThai.addItem(ten);
        }
        locDanhSach();
    }

    private void giaoKiemTra() {
        TaiKhoan tk = kiemTraPhien(); int row = tblDanhSach.getSelectedRow();
        if (row < 0) { lblDanhSach.setText("Chọn phiếu để giao kiểm tra."); return; }
        String ma = (String) modelDanhSach.getValueAt(tblDanhSach.convertRowIndexToModel(row), 0);
        List<KyThuatVienPhanCong> nhanSu = phieuDAO.layKyThuatPhanCong(tk);
        if (nhanSu.isEmpty()) { lblDanhSach.setText("Chưa có kỹ thuật viên đang làm việc được liên kết tài khoản."); return; }
        KyThuatVienPhanCong chon = (KyThuatVienPhanCong) JOptionPane.showInputDialog(this,
                "Chọn người kiểm tra cho " + ma, "Phân công kiểm tra", JOptionPane.QUESTION_MESSAGE, null, nhanSu.toArray(), null);
        if (chon == null) return;
        try {
            phieuDAO.giaoKiemTra(tk, phieuDAO.timTheoMa(tk, ma), chon.getTaiKhoan().getTenDangNhap());
            lblDanhSach.setText("Đã giao kiểm tra: " + phieuDAO.docPhanCongKiemTra(tk, ma));
        } catch (IllegalArgumentException | IllegalStateException e) { lblDanhSach.setText(e.getMessage()); }
    }

    public boolean choPhepRoiManHinh() {
        return !daThayDoi || JOptionPane.showConfirmDialog(this, "Phiếu đang nhập chưa lưu. Bỏ nội dung đang nhập?",
                "Thay đổi chưa lưu", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** Chỉ gọi khi đã đồng ý bỏ bản nhập; chưa tạo bản ghi nghiệp vụ. */
    public void moDanhSach() {
        daThayDoi = false;
        chkXacNhan.setSelected(false);
        boCuc.show(pnlThe, "DANH_SACH");
        locDanhSach();
    }

    private JPanel taoDanhSach() {
        JPanel pnl = nen(new BorderLayout(0, 16));
        JPanel dau = nen(new BorderLayout(0, 16));
        dau.add(tieuDe("Phiếu tiếp nhận", "Tra cứu khách hàng và phương tiện trước khi lập phiếu.",
                nut("+ Tiếp nhận xe", true, this::moPhieuMoi)), BorderLayout.NORTH);
        JPanel loc = khung(new BorderLayout(8, 0));
        txtLoc.setBorder(BorderFactory.createTitledBorder("Mã phiếu / biển số / khách"));
        txtLoc.addActionListener(e -> locDanhSach());
        loc.add(txtLoc);
        JPanel tacVu = nen(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        cboTrangThai.addActionListener(e -> locDanhSach());
        tacVu.add(cboTrangThai);
        tacVu.add(nut("Tìm", false, this::locDanhSach));
        tacVu.add(nut("Làm mới", false, () -> { txtLoc.setText(""); cboTrangThai.setSelectedIndex(0); locDanhSach(); }));
        loc.add(tacVu, BorderLayout.EAST);
        dau.add(loc); pnl.add(dau, BorderLayout.NORTH);
        JPanel ds = khung(new BorderLayout(0, 12));
        ds.add(nhan("Danh sách phiếu tiếp nhận · Dữ liệu trong bộ nhớ", true), BorderLayout.NORTH);
        ds.add(cuonBang(tblDanhSach));
        JPanel nut = nen(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nut.add(nut("Xem chi tiết", false, this::xemChiTiet));
        nut.add(nut("Xem lịch sử xe", false, this::xemLichSu));
        nut.add(nut("Phân công kiểm tra", false, this::giaoKiemTra));
        ds.add(nut, BorderLayout.SOUTH);
        tblDanhSach.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "CHI_TIET");
        tblDanhSach.getActionMap().put("CHI_TIET", new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override public void actionPerformed(ActionEvent e) { xemChiTiet(); }
        });
        pnl.add(ds); pnl.add(lblDanhSach, BorderLayout.SOUTH);
        return pnl;
    }

    private JPanel taoForm() {
        JPanel pnl = nen(new BorderLayout(0, 16));
        pnl.add(tieuDe("Lập phiếu tiếp nhận xe", "1. Tra cứu hồ sơ → 2. Thông tin tiếp nhận → 3. Đối chiếu",
                nut("Danh sách / quay lại", false, () -> { if (choPhepRoiManHinh()) moDanhSach(); })), BorderLayout.NORTH);
        JPanel noiDung = nen(new BorderLayout(0, 16));
        JPanel tra = khung(new BorderLayout(0, 10));
        JPanel tim = nen(new BorderLayout(8, 0));
        txtTraCuu.setBorder(BorderFactory.createTitledBorder("Số điện thoại / biển số"));
        txtTraCuu.addActionListener(e -> traHoSo());
        tim.add(txtTraCuu); tim.add(nut("Tra cứu", false, this::traHoSo), BorderLayout.EAST);
        tra.add(tim, BorderLayout.NORTH);
        JScrollPane ketQua = cuonBang(tblHoSo);
        ketQua.setPreferredSize(new Dimension(0, 150)); tra.add(ketQua);
        JPanel duoi = nen(new BorderLayout(0, 10));
        duoi.add(lblHoSo, BorderLayout.NORTH);
        JPanel tacVu = nen(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tacVu.add(nut("Chọn xe", true, this::chonHoSo));
        tacVu.add(nut("Quản lý khách hàng", false, () -> quanLyHoSo(true)));
        tacVu.add(nut("Quản lý phương tiện", false, () -> quanLyHoSo(false)));
        duoi.add(tacVu); tra.add(duoi, BorderLayout.SOUTH);
        noiDung.add(tra, BorderLayout.NORTH);
        JPanel nhap = khung(new BorderLayout(0, 14));
        JPanel thongTin = nen(new GridLayout(2, 2, 14, 14));
        thongTin.add(truong("Khách hàng đã chọn · chỉ đọc", txtKhach));
        thongTin.add(truong("Phương tiện đã chọn · chỉ đọc", txtXe));
        txtKhach.setEditable(false); txtXe.setEditable(false);
        thongTin.add(truong("Số kilomet * · số nguyên không âm", txtKm));
        thongTin.add(truong("Thời gian nhận * · dd/MM/yyyy HH:mm", txtNgay));
        nhap.add(thongTin, BorderLayout.NORTH);
        JPanel moTa = nen(new GridLayout(3, 1, 0, 14));
        moTa.add(truong("Tình trạng ban đầu *", txtTinhTrang));
        moTa.add(truong("Tài sản khách để lại · tùy chọn", txtTaiSan));
        moTa.add(truong("Yêu cầu sửa chữa / bảo dưỡng *", txtYeuCau));
        nhap.add(moTa);
        JPanel chan = nen(new BorderLayout(0, 10));
        lblLoi.setForeground(UiTheme.ERROR); chan.add(lblLoi, BorderLayout.NORTH);
        chan.add(nut("Xem lại & ghi nhận", true, this::moDoiChieu));
        nhap.add(chan, BorderLayout.SOUTH); noiDung.add(nhap);
        pnl.add(cuonNoiDung(noiDung));
        theoDoiThayDoi(txtKm); theoDoiThayDoi(txtNgay); theoDoiThayDoi(txtTinhTrang);
        theoDoiThayDoi(txtTaiSan); theoDoiThayDoi(txtYeuCau);
        return pnl;
    }

    private JPanel taoDoiChieu() {
        JPanel pnl = nen(new BorderLayout(0, 16));
        pnl.add(tieuDe("Đối chiếu phiếu tiếp nhận", "Đọc lại hiện trạng và yêu cầu cùng khách trước khi xác nhận.",
                nut("Quay lại chỉnh sửa", false, () -> {
                    chkXacNhan.setSelected(false); xeDoiChieu = null; boCuc.show(pnlThe, "FORM");
                })), BorderLayout.NORTH);
        JPanel hang = nen(new GridLayout(1, 2, 16, 0));
        JPanel thongTin = khung(new BorderLayout(0, 12));
        thongTin.add(nhan("Thông tin tiếp nhận · chưa lưu", true), BorderLayout.NORTH);
        txtDoiChieu.setEditable(false); txtDoiChieu.setRows(16);
        thongTin.add(new JScrollPane(txtDoiChieu)); hang.add(thongTin);
        JPanel xacNhan = khung(new BorderLayout(0, 16));
        JPanel chu = nen(new GridLayout(0, 1, 0, 8));
        chu.setPreferredSize(new Dimension(0, 320));
        chu.add(nhan("Khách hàng xác nhận nội dung", true));
        JLabel huongDan = nhan("<html>Đối chiếu tên khách, xe, kilomet, hiện trạng,<br>tài sản và yêu cầu.<br>Không mặc định khách đã đồng ý.</html>", false);
        chu.add(huongDan);
        chkXacNhan.setOpaque(false); chkXacNhan.setFont(UiTheme.APP_FONT);
        chkXacNhan.setText("<html>Khách đã xem và đồng ý thông tin tiếp nhận</html>");
        chkXacNhan.addItemListener(e -> {
            btnHoanTat.setEnabled(chkXacNhan.isSelected());
            lblXacNhan.setText(chkXacNhan.isSelected() ? "Đã chọn xác nhận · chưa lưu phiếu." : "Chưa xác nhận · chưa lưu phiếu.");
        });
        chu.add(chkXacNhan); chu.add(lblXacNhan);
        JLabel gioiHan = nhan("<html>Phiếu lưu trong bộ nhớ của phiên.<br>Chưa lưu SQL hoặc phân công kỹ thuật.</html>", false);
        chu.add(gioiHan); xacNhan.add(chu, BorderLayout.NORTH);
        btnHoanTat.setEnabled(false);
        btnHoanTat.setPreferredSize(new Dimension(0, 40));
        btnHoanTat.addActionListener(e -> luuPhieu());
        xacNhan.add(btnHoanTat, BorderLayout.SOUTH); hang.add(xacNhan);
        hang.setPreferredSize(new Dimension(600, 440));
        JScrollPane cuon = cuonNoiDung(hang);
        cuon.getViewport().addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                boolean hep = cuon.getViewport().getWidth() < 850;
                GridLayout layout = (GridLayout) hang.getLayout();
                layout.setRows(hep ? 2 : 1); layout.setColumns(hep ? 1 : 2); layout.setVgap(16);
                hang.setPreferredSize(new Dimension(600, hep ? 900 : 440));
                hang.revalidate();
            }
        });
        pnl.add(cuon);
        return pnl;
    }

    private void moPhieuMoi() {
        kiemTraPhien(); dangNap = true;
        maThaoTac = UUID.randomUUID().toString(); xeDoiChieu = null;
        maXeDaChon = null; txtKhach.setText(""); txtXe.setText(""); txtKm.setText("");
        txtNgay.setText(LocalDateTime.now().format(DINH_DANG_NGAY));
        txtTinhTrang.setText(""); txtTaiSan.setText(""); txtYeuCau.setText("");
        txtTraCuu.setText(""); modelHoSo.setRowCount(0);
        lblLoi.setText(" "); lblHoSo.setText("Tra cứu SĐT hoặc biển số rồi chọn đúng xe.");
        chkXacNhan.setSelected(false); daThayDoi = false; dangNap = false;
        boCuc.show(pnlThe, "FORM");
    }

    private void traHoSo() {
        try {
            TaiKhoan tk = kiemTraPhien();
            String tu = txtTraCuu.getText().strip(); modelHoSo.setRowCount(0);
            List<Xe> cacXe = xeDAO.timKiem(tk, "");
            for (Xe xe : cacXe) {
                KhachHang kh = khachHangDAO.timTheoMa(tk, xe.getMaKH());
                String bien = tu.toUpperCase(Locale.ROOT).replaceAll("[\\s.\\-]", "");
                if (tu.isEmpty() || kh.getSoDienThoai().contains(tu)
                        || (!bien.isEmpty() && Xe.chuanHoaBienSo(xe.getBienSo()).contains(bien))) {
                    modelHoSo.addRow(new Object[] {xe.getMaXe(), xe.getBienSo(), kh.getMaKH(), kh.getHoTen(), kh.getSoDienThoai()});
                }
            }
            lblHoSo.setText(modelHoSo.getRowCount() == 0 ? "Không có xe phù hợp. Kiểm tra hoặc thêm hồ sơ khách/xe." : "Tìm thấy " + modelHoSo.getRowCount() + " xe. Chọn đúng xe, không tự chọn kết quả đầu.");
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) { lblLoi.setText(e.getMessage()); }
    }

    private void chonHoSo() {
        int dong = tblHoSo.getSelectedRow();
        if (dong < 0) { lblHoSo.setText("Chọn một xe trong kết quả tra cứu."); return; }
        try {
            String ma = (String) modelHoSo.getValueAt(tblHoSo.convertRowIndexToModel(dong), 0);
            Xe xe = xeDAO.timTheoMa(kiemTraPhien(), ma);
            KhachHang kh = khachHangDAO.timTheoMa(kiemTraPhien(), xe.getMaKH());
            maXeDaChon = xe.getMaXe();
            txtKhach.setText(kh.getMaKH() + " · " + kh.getHoTen() + " · " + kh.getSoDienThoai());
            txtXe.setText(xe.getBienSo() + " · " + xe.getHangXe() + " " + xe.getModel());
            daThayDoi = true; chkXacNhan.setSelected(false);
            lblHoSo.setText("Đã chọn " + xe.getBienSo() + ". Kiểm tra thông tin bên dưới.");
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) { lblLoi.setText(e.getMessage()); }
    }

    private void moDoiChieu() {
        try {
            TaiKhoan tk = kiemTraPhien();
            if (maXeDaChon == null) throw new IllegalArgumentException("Cần tra cứu và chọn xe trước.");
            Xe xe = xeDAO.timTheoMa(tk, maXeDaChon);
            KhachHang kh = khachHangDAO.timTheoMa(tk, xe.getMaKH());
            if (!txtKm.getText().strip().matches("[0-9]+")) throw new IllegalArgumentException("Kilomet phải là số nguyên không âm.");
            long km;
            try { km = Long.parseLong(txtKm.getText().strip()); }
            catch (NumberFormatException e) { throw new IllegalArgumentException("Số kilomet vượt phạm vi hỗ trợ."); }
            LocalDateTime ngay;
            try { ngay = LocalDateTime.parse(txtNgay.getText().strip(), DINH_DANG_NGAY); }
            catch (DateTimeParseException e) { throw new IllegalArgumentException("Thời gian nhận không hợp lệ; nhập dd/MM/yyyy HH:mm."); }
            if (txtTinhTrang.getText().isBlank() || txtYeuCau.getText().isBlank()) throw new IllegalArgumentException("Cần nhập tình trạng ban đầu và yêu cầu của khách.");
            xeDoiChieu = xe; khachDoiChieu = new KhachHang(kh); kmDoiChieu = km; ngayDoiChieu = ngay;
            txtDoiChieu.setText("Khách hàng: " + kh.getHoTen() + " · " + kh.getSoDienThoai()
                    + "\n\nPhương tiện: " + xe.getBienSo() + " · " + xe.getHangXe() + " " + xe.getModel()
                    + "\n\nKilomet khi nhận: " + km + " km\n\nThời gian nhận: " + ngay.format(DINH_DANG_NGAY)
                    + "\n\nTình trạng ban đầu:\n" + txtTinhTrang.getText().strip() + "\n\nTài sản để lại:\n"
                    + (txtTaiSan.getText().isBlank() ? "Chưa ghi nhận" : txtTaiSan.getText().strip())
                    + "\n\nYêu cầu sửa chữa / bảo dưỡng:\n" + txtYeuCau.getText().strip());
            txtDoiChieu.setCaretPosition(0); chkXacNhan.setSelected(false); btnHoanTat.setEnabled(false);
            lblXacNhan.setText("Chưa xác nhận · chưa lưu phiếu.");
            boCuc.show(pnlThe, "DOI_CHIEU");
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) { lblLoi.setText(e.getMessage()); }
    }

    /** Mở lại màn hồ sơ đã có, không sao chép logic tạo khách/xe vào form tiếp nhận. */
    private void quanLyHoSo(boolean laKhach) {
        kiemTraPhien();
        if (laKhach) {
            KhachHangPanel pnl = new KhachHangPanel(khachHangDAO, phien,
                    ma -> xeDAO.demTheoKhach(kiemTraPhien(), ma));
            moHoSoDialog("Hồ sơ khách hàng · lưu riêng với phiếu tiếp nhận", pnl, pnl::choPhepRoiManHinh);
        } else {
            XePanel pnl = new XePanel(xeDAO, khachHangDAO, phien, phieuDAO, tienDoDAO);
            moHoSoDialog("Hồ sơ phương tiện · lưu riêng với phiếu tiếp nhận", pnl, pnl::choPhepRoiManHinh);
        }
        traHoSo();
        // Hồ sơ có thể được sửa trong dialog: bỏ lựa chọn cũ để đối chiếu lại đúng thông tin.
        maXeDaChon = null; txtKhach.setText(""); txtXe.setText(""); chkXacNhan.setSelected(false);
    }

    private void moHoSoDialog(String ten, JPanel noiDung, BooleanSupplier choPhepDong) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), ten, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        Runnable dong = () -> { if (choPhepDong.getAsBoolean()) dialog.dispose(); };
        dialog.addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { dong.run(); } });
        JPanel khung = nen(new BorderLayout(0, 10));
        khung.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        khung.add(noiDung); khung.add(nut("Đóng và quay lại phiếu", false, dong), BorderLayout.SOUTH);
        dialog.setContentPane(khung);
        dialog.getRootPane().registerKeyboardAction(e -> dong.run(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.setSize(Math.min(1100, getToolkit().getScreenSize().width - 60), Math.min(760, getToolkit().getScreenSize().height - 60));
        dialog.setLocationRelativeTo(this); dialog.setVisible(true);
    }

    private void locDanhSach() {
        TaiKhoan tk = kiemTraPhien();
        modelDanhSach.setRowCount(0);
        TrangThaiTiepNhan trangThai = tienDoDAO == null && cboTrangThai.getSelectedIndex() > 0 ? TrangThaiTiepNhan.CHO_KIEM_TRA : null;
        for (PhieuTiepNhan phieu : phieuDAO.timKiem(tk, txtLoc.getText(), trangThai)) {
            String hienThi = tienDoDAO == null ? phieu.getTrangThai().getTenHienThi() : tienDoDAO.trangThaiPhieu(phien.get(), phieu.getMaPhieu());
            if (tienDoDAO != null && cboTrangThai.getSelectedIndex() > 0 && !hienThi.equals(cboTrangThai.getSelectedItem())) continue;
            modelDanhSach.addRow(new Object[] {phieu.getMaPhieu(), phieu.getBienSo(), phieu.getTenKhach(),
                    phieu.getThoiGianNhan().format(DINH_DANG_NGAY), phieu.getYeuCau(), hienThi});
        }
        lblDanhSach.setText(modelDanhSach.getRowCount() + " phiếu · Dữ liệu trong bộ nhớ, chưa lưu lâu dài.");
    }

    private void luuPhieu() {
        if (!btnHoanTat.isEnabled()) return;
        btnHoanTat.setEnabled(false);
        try {
            if (xeDoiChieu == null) throw new IllegalStateException("Hãy đối chiếu lại nội dung với khách.");
            PhieuTiepNhan daLuu = phieuDAO.luu(kiemTraPhien(), maThaoTac, xeDoiChieu, khachDoiChieu,
                    kmDoiChieu, ngayDoiChieu, txtTinhTrang.getText(), txtTaiSan.getText(), txtYeuCau.getText(), chkXacNhan.isSelected());
            txtLoc.setText(""); cboTrangThai.setSelectedIndex(0);
            moDanhSach();
            lblDanhSach.setText("Đã lưu " + daLuu.getMaPhieu() + " · Chờ kiểm tra · Dữ liệu trong bộ nhớ.");
            xeDoiChieu = null;
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            lblXacNhan.setText(e.getMessage());
            btnHoanTat.setEnabled(chkXacNhan.isSelected());
        }
    }

    private int dongDangChon() {
        int dong = tblDanhSach.getSelectedRow();
        if (dong < 0) lblDanhSach.setText("Chọn phiếu tiếp nhận trước.");
        return dong < 0 ? -1 : tblDanhSach.convertRowIndexToModel(dong);
    }

    private void xemChiTiet() {
        kiemTraPhien(); int dong = dongDangChon(); if (dong < 0) return;
        PhieuTiepNhan phieu = phieuDAO.timTheoMa(kiemTraPhien(), (String) modelDanhSach.getValueAt(dong, 0));
        JTextArea noiDung = vungChu();
        noiDung.setText("Phiếu: " + phieu.getMaPhieu() + "\nKhách: " + phieu.getTenKhach() + " · " + phieu.getSoDienThoai()
                + "\nXe: " + phieu.getBienSo() + " · " + phieu.getTenXe() + "\nKilomet: " + phieu.getSoKm()
                + "\nNgày nhận: " + phieu.getThoiGianNhan().format(DINH_DANG_NGAY)
                + "\nTình trạng: " + phieu.getTinhTrang() + "\nTài sản: " + phieu.getTaiSan()
                + "\nYêu cầu: " + phieu.getYeuCau() + "\nNgười nhận: " + phieu.getNguoiTiepNhan()
                + "\nKhách xác nhận lúc: " + phieu.getThoiGianXacNhan().format(DINH_DANG_NGAY)
                + "\nTrạng thái: " + phieu.getTrangThai().getTenHienThi());
        noiDung.setEditable(false); noiDung.setRows(16); noiDung.setColumns(55);
        JOptionPane.showMessageDialog(this, new JScrollPane(noiDung), "Chi tiết phiếu đã xác nhận", JOptionPane.INFORMATION_MESSAGE);
    }

    private void xemLichSu() {
        kiemTraPhien(); int dong = dongDangChon(); if (dong < 0) return;
        XePanel pnl = new XePanel(xeDAO, khachHangDAO, phien, phieuDAO, tienDoDAO);
        pnl.moLichSuTheoBienSo((String) modelDanhSach.getValueAt(dong, 1));
        moHoSoDialog("Lịch sử xe · tiếp nhận trong phiên, sửa chữa chưa kết nối", pnl, pnl::choPhepRoiManHinh);
    }

    private TaiKhoan kiemTraPhien() {
        TaiKhoan tk = Objects.requireNonNull(phien.get(), "Cần phiên đăng nhập.");
        if (tk.getVaiTro() != VaiTro.NHAN_VIEN_TIEP_NHAN) throw new SecurityException("Chỉ nhân viên tiếp nhận được mở chức năng này.");
        return tk;
    }

    private void theoDoiThayDoi(JTextComponent txt) {
        txt.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { thayDoi(); }
            @Override public void removeUpdate(DocumentEvent e) { thayDoi(); }
            @Override public void changedUpdate(DocumentEvent e) { thayDoi(); }
            private void thayDoi() { if (!dangNap) { daThayDoi = true; xeDoiChieu = null; chkXacNhan.setSelected(false); } }
        });
    }

    private static JPanel truong(String ten, JTextComponent txt) {
        JPanel pnl = nen(new BorderLayout(0, 6)); JLabel lbl = nhan(ten, true); lbl.setLabelFor(txt); pnl.add(lbl, BorderLayout.NORTH);
        txt.setFont(UiTheme.APP_FONT);
        if (txt instanceof JTextArea) { JScrollPane cuon = new JScrollPane(txt); cuon.setPreferredSize(new Dimension(0, 90)); pnl.add(cuon); }
        else { txt.setPreferredSize(new Dimension(0, 38)); pnl.add(txt); }
        return pnl;
    }
    private static JTextArea vungChu() {
        JTextArea txt = new JTextArea();
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        txt.setFont(UiTheme.APP_FONT);
        txt.setMargin(new Insets(8, 8, 8, 8));
        return txt;
    }
    private static JPanel nen(LayoutManager layout) { JPanel pnl = new JPanel(layout); pnl.setOpaque(false); return pnl; }
    private static JPanel khung(LayoutManager layout) { JPanel pnl = UiTheme.roundedPanel(layout, Color.WHITE, 8); pnl.setBorder(UiTheme.roundedBorder(UiTheme.APP_BORDER, 8, 16, 16)); return pnl; }
    private static JLabel nhan(String chu, boolean dam) { JLabel lbl = new JLabel(chu); lbl.setFont(dam ? UiTheme.APP_BOLD_FONT : UiTheme.APP_FONT); lbl.setForeground(dam ? UiTheme.APP_TEXT : UiTheme.APP_MUTED); return lbl; }
    private static JButton nut(String ten, boolean chinh, Runnable thaoTac) {
        JButton btn = UiTheme.createAppButton(ten, chinh);
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 18, 36));
        btn.addActionListener(e -> thaoTac.run());
        return btn;
    }
    private static JPanel tieuDe(String ten, String moTa, JButton thaoTac) {
        JPanel pnl = nen(new BorderLayout(8, 10)); pnl.add(nhan("Tiếp nhận / Nghiệp vụ / " + ten, false), BorderLayout.NORTH);
        JPanel chu = nen(new BorderLayout(0, 6)); JLabel lbl = nhan(ten, true); lbl.setFont(UiTheme.APP_BOLD_FONT.deriveFont(24f)); chu.add(lbl, BorderLayout.NORTH); chu.add(nhan(moTa, false)); pnl.add(chu);
        JPanel nut = nen(new FlowLayout(FlowLayout.RIGHT, 0, 0)); nut.add(thaoTac); pnl.add(nut, BorderLayout.EAST); return pnl;
    }
    private static DefaultTableModel model(String... cot) { return new DefaultTableModel(cot, 0) { private static final long serialVersionUID = 1L; @Override public boolean isCellEditable(int row, int col) { return false; } }; }
    private static JTable bang(DefaultTableModel model) { JTable tbl = new JTable(model); tbl.setFont(UiTheme.APP_FONT); tbl.setRowHeight(44); tbl.setAutoCreateRowSorter(true); tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); tbl.getTableHeader().setFont(UiTheme.APP_BOLD_FONT); tbl.getTableHeader().setReorderingAllowed(false); tbl.setShowVerticalLines(false); tbl.setGridColor(UiTheme.APP_BORDER); return tbl; }
    private static JScrollPane cuonBang(JTable tbl) { JScrollPane cuon = new JScrollPane(tbl); cuon.getViewport().setBackground(Color.WHITE); return cuon; }
    /** Bám chiều rộng viewport, chỉ cuộn dọc để form không bị cắt khi thu nhỏ. */
    private static final class NoiDungCuon extends JPanel implements Scrollable {
        private static final long serialVersionUID = 1L;
        NoiDungCuon() { super(new BorderLayout()); setOpaque(false); }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) { return 20; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) { return Math.max(20, r.height - 20); }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }
    private static JScrollPane cuonNoiDung(JPanel pnl) {
        JPanel neo = new NoiDungCuon(); neo.add(pnl, BorderLayout.NORTH);
        JScrollPane cuon = new JScrollPane(neo);
        cuon.setBorder(null); cuon.getViewport().setBackground(UiTheme.APP_BACKGROUND);
        cuon.getVerticalScrollBar().setUnitIncrement(20); return cuon;
    }
}
