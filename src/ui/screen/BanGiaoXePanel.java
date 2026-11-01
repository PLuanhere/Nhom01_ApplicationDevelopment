package ui.screen;

import java.awt.*;
import java.time.*;
import java.time.format.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import dao.BanGiaoXeDAO;
import entity.*;
import ui.component.UiTheme;
import ui.dialog.BanGiaoXeDialog;

/** Tiếp nhận chỉ đọc căn cứ tài chính, ghi sự kiện thực giao sau khi khách xác nhận. */
public final class BanGiaoXePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter NGAY_GIO = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm", Locale.ROOT).withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.ROOT).withResolverStyle(ResolverStyle.STRICT);
    private final BanGiaoXeDAO dao;
    private final Supplier<TaiKhoan> phien;
    private final CardLayout boCuc = new CardLayout();
    private final JPanel pnlThe = nen(boCuc);
    private final JTextField txtTim = new JTextField(), txtTuNgay = new JTextField(), txtDenNgay = new JTextField();
    private final JComboBox<String> cboTrangThai = new JComboBox<>(new String[] {"Tất cả trạng thái", "Chờ bàn giao", "Đã bàn giao", "Chờ xác nhận thanh toán"});
    private final DefaultTableModel modelDanhSach = model("Mã bàn giao / lệnh", "Biển số / khách", "Ngày giờ thực giao", "Người giao", "Trạng thái");
    private final JTable tblDanhSach = bang(modelDanhSach);
    private final List<PhieuSuaChua> dangHien = new ArrayList<>();
    private final JLabel lblDanhSach = nhan(" ", false), lblLoi = nhan(" ", false), lblTieuDeForm = nhan("Xác nhận bàn giao xe", true);
    private final JTextArea txtThongTin = vanBan(3, false), txtThanhToan = vanBan(5, false), txtTinhTrang = vanBan(2, true), txtTaiSan = vanBan(2, true), txtGhiChu = vanBan(2, true);
    private final DefaultTableModel modelKetQua = model("Hạng mục đã sửa", "Người phụ trách", "Kết quả sửa chữa");
    private final JTextField txtNguoiGiao = new JTextField(), txtNguoiNhan = new JTextField(), txtGioGiao = new JTextField();
    private final JComboBox<String> cboHinhThuc = new JComboBox<>(new String[] {"Chọn hình thức xác nhận", "Ký phiếu bàn giao", "Xác nhận trực tiếp"});
    private final JCheckBox chkKhachXacNhan = new JCheckBox("Khách/người nhận đã đối chiếu và xác nhận nhận xe");
    private final JButton btnXacNhan = nut("Xác nhận bàn giao thực tế", true, this::doiChieu);
    private final JButton btnDocLai = nut("Đọc lại căn cứ", false, this::docLaiCanCu);
    private PhieuSuaChua lenhDangMo;
    private BaoGia baoGiaDangMo;
    private CanCuBanGiao canCuDaDoc;
    private String maThaoTac;
    private boolean chiDoc, dangNap, daThayDoi, coTheBanGiao;
    private JScrollPane cuonForm;

    public BanGiaoXePanel(BanGiaoXeDAO dao, Supplier<TaiKhoan> phien) {
        super(new BorderLayout()); this.dao = Objects.requireNonNull(dao); this.phien = Objects.requireNonNull(phien);
        kiemTraPhien(); setOpaque(false);
        pnlThe.add(taoDanhSach(), "DANH_SACH"); pnlThe.add(taoForm(), "FORM"); add(pnlThe);
        for (javax.swing.text.JTextComponent txt : List.of(txtNguoiNhan, txtGioGiao, txtTinhTrang, txtTaiSan, txtGhiChu)) theoDoi(txt);
        chkKhachXacNhan.addActionListener(e -> { if (!dangNap) daThayDoi = true; capNhatNut(); });
        cboHinhThuc.addActionListener(e -> { if (!dangNap) daThayDoi = true; });
        moDanhSach();
    }

    public boolean choPhepRoiManHinh() {
        return !daThayDoi || JOptionPane.showConfirmDialog(this, "Thông tin bàn giao chưa lưu. Bỏ thay đổi?", "Dữ liệu chưa lưu",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }
    public void moDanhSach() { kiemTraPhien(); daThayDoi = false; locDanhSach(); boCuc.show(pnlThe, "DANH_SACH"); }

    private JPanel taoDanhSach() {
        JPanel pnl = nen(new BorderLayout(0, 16)), dau = nen(new BorderLayout(0, 16));
        dau.add(tieuDe(nhan("Bàn giao xe", true), "Tiếp nhận giao xe sau khi có xác nhận thu tiền hoặc phê duyệt ngoại lệ.", null), BorderLayout.NORTH);
        JPanel loc = khung(new BorderLayout(8, 0)); txtTim.setBorder(BorderFactory.createTitledBorder("Mã bàn giao / lệnh / biển số / khách"));
        txtTim.addActionListener(e -> locDanhSach()); loc.add(txtTim);
        JPanel nut = nen(new FlowLayout(FlowLayout.RIGHT, 8, 0)); cboTrangThai.addActionListener(e -> locDanhSach()); nut.add(cboTrangThai); nut.add(nut("Tìm", false, this::locDanhSach));
        nut.add(nut("Làm mới", false, () -> { txtTim.setText(""); txtTuNgay.setText(""); txtDenNgay.setText(""); cboTrangThai.setSelectedIndex(0); locDanhSach(); })); loc.add(nut, BorderLayout.EAST); dau.add(loc); pnl.add(dau, BorderLayout.NORTH);
        JPanel ds = khung(new BorderLayout(0, 12)); JPanel thongTin = nen(new BorderLayout(0, 10));
        thongTin.add(nhan("Danh sách và lịch sử bàn giao xe", true), BorderLayout.NORTH);
        thongTin.add(hang(truong("Từ ngày · dd/MM/yyyy", txtTuNgay), truong("Đến ngày · dd/MM/yyyy", txtDenNgay))); ds.add(thongTin, BorderLayout.NORTH);
        txtTuNgay.addActionListener(e -> locDanhSach()); txtDenNgay.addActionListener(e -> locDanhSach()); ds.add(cuonBang(tblDanhSach));
        JPanel thaoTac = nen(new FlowLayout(FlowLayout.RIGHT, 8, 0)); thaoTac.add(nut("Áp dụng ngày", false, this::locDanhSach)); thaoTac.add(nut("Xem chi tiết / bàn giao", true, this::moDaChon)); ds.add(thaoTac, BorderLayout.SOUTH); pnl.add(ds);
        // Giữ vùng bảng đủ đọc trên cửa sổ thấp; phần danh sách cuộn thay vì ép mất các dòng xe.
        ds.setPreferredSize(new Dimension(0, 350));
        JPanel trang = nen(new BorderLayout(0, 12));
        trang.add(cuonNoiDung(pnl)); trang.add(lblDanhSach, BorderLayout.SOUTH); return trang;
    }

    private JPanel taoForm() {
        JPanel pnl = nen(new BorderLayout(0, 16));
        pnl.add(tieuDe(lblTieuDeForm, "Đối chiếu kết quả sửa chữa, căn cứ tài chính và thông tin thực giao.",
                nut("Danh sách / quay lại", false, () -> { if (choPhepRoiManHinh()) moDanhSach(); })), BorderLayout.NORTH);
        JPanel form = khung(new BorderLayout(0, 14)); JPanel dau = cot();
        them(dau, new JScrollPane(txtThongTin), 85); them(dau, nhan("Kết quả sửa chữa · chỉ đọc", true), 22);
        JScrollPane bang = cuonBang(bang(modelKetQua)); them(dau, bang, 150);
        them(dau, truong("Thanh toán / phê duyệt · chỉ đọc", txtThanhToan), 135); form.add(dau, BorderLayout.NORTH);
        JPanel truong = cot(); txtNguoiGiao.setEditable(false);
        them(truong, hang(truong("Người giao xe * · tài khoản đang đăng nhập", txtNguoiGiao), truong("Người nhận xe *", txtNguoiNhan)), 68);
        them(truong, hang(truong("Thời điểm giao thực tế * · dd/MM/yyyy HH:mm", txtGioGiao), truong("Tình trạng khi bàn giao *", txtTinhTrang)), 85);
        them(truong, hang(truong("Tài sản/phụ kiện đã đối chiếu và trả", txtTaiSan), truong("Hình thức khách xác nhận *", cboHinhThuc)), 85);
        them(truong, truong("Ghi chú bàn giao", txtGhiChu), 85);
        chkKhachXacNhan.setOpaque(false); chkKhachXacNhan.setFont(UiTheme.APP_FONT); them(truong, chkKhachXacNhan, 30);
        JTextArea ghiNho = vanBan(2, false); ghiNho.setText("Giờ giao là giờ thực tế, không lấy giờ hẹn. Chỉ hoàn tất khi khách/người nhận đã xác nhận; ngoại lệ phải có căn cứ quản lý."); ghiNho.setBackground(new Color(0xFFF7E3)); them(truong, ghiNho, 50); form.add(truong);
        cuonForm = cuonNoiDung(form); pnl.add(cuonForm);
        JPanel chan = nen(new BorderLayout(0, 8)); lblLoi.setForeground(UiTheme.ERROR); chan.add(lblLoi, BorderLayout.NORTH);
        JPanel nut = nen(new FlowLayout(FlowLayout.RIGHT, 8, 0)); nut.add(btnDocLai); nut.add(btnXacNhan); chan.add(nut); pnl.add(chan, BorderLayout.SOUTH); return pnl;
    }

    private void locDanhSach() {
        try {
            kiemTraPhien(); LocalDate tu = docNgay(txtTuNgay), den = docNgay(txtDenNgay);
            TrangThaiBanGiao trangThai = null;
            for (TrangThaiBanGiao tt : TrangThaiBanGiao.values()) if (tt.getTenHienThi().equals(cboTrangThai.getSelectedItem())) trangThai = tt;
            List<PhieuSuaChua> moi = dao.timKiem(phien.get(), txtTim.getText(), trangThai, tu, den);
            modelDanhSach.setRowCount(0); dangHien.clear(); dangHien.addAll(moi);
            for (PhieuSuaChua lenh : moi) {
                BanGiaoXe giao = dao.timTheoLenh(phien.get(), lenh.getMaPhieu()).orElse(null);
                BaoGia bg = dao.docBaoGia(phien.get(), lenh.getMaPhieu());
                String khach = bg.getBaoCao() == null ? "Chưa có phiếu liên kết" : bg.getBaoCao().getPhieu().getTenKhach();
                modelDanhSach.addRow(new Object[] {giao == null ? lenh.getMaPhieu() : giao.getMaBanGiao(), lenh.getBienSo() + " · " + khach,
                        giao == null ? "—" : giao.getThoiDiemGiao().format(NGAY_GIO), giao == null ? "—" : giao.getNguoiGiao(), dao.trangThai(phien.get(), lenh.getMaPhieu()).getTenHienThi()});
            }
            lblDanhSach.setText(moi.size() + " xe · Chưa giao lọc theo ngày hoàn tất sửa chữa; đã giao theo ngày thực giao.");
        } catch (DateTimeParseException e) { lblDanhSach.setText("Ngày lọc không hợp lệ; dùng dd/MM/yyyy."); }
        catch (IllegalArgumentException | IllegalStateException | SecurityException e) { lblDanhSach.setText(e.getMessage()); }
    }

    private void moDaChon() {
        kiemTraPhien(); int row = tblDanhSach.getSelectedRow();
        if (row < 0) { lblDanhSach.setText("Chọn xe để xem chi tiết/bàn giao."); return; }
        String ma = dangHien.get(tblDanhSach.convertRowIndexToModel(row)).getMaPhieu();
        try { moForm(ma); } catch (IllegalArgumentException | IllegalStateException | SecurityException e) { lblDanhSach.setText(e.getMessage()); }
    }

    private void moForm(String ma) {
        kiemTraPhien(); lenhDangMo = dao.docLenh(phien.get(), ma); baoGiaDangMo = dao.docBaoGia(phien.get(), ma);
        BanGiaoXe giao = dao.timTheoLenh(phien.get(), ma).orElse(null); chiDoc = giao != null;
        if (chiDoc) { lenhDangMo = giao.getLenh(); baoGiaDangMo = giao.getBaoGia(); canCuDaDoc = giao.getCanCu(); }
        else canCuDaDoc = dao.docCanCu(phien.get(), ma);
        dangNap = true; maThaoTac = UUID.randomUUID().toString(); lblTieuDeForm.setText(chiDoc ? "Chi tiết bàn giao đã lưu" : "Xác nhận bàn giao xe");
        PhieuTiepNhan phieu = baoGiaDangMo.getBaoCao() == null ? null : baoGiaDangMo.getBaoCao().getPhieu();
        txtThongTin.setText("Mã bàn giao: " + (chiDoc ? giao.getMaBanGiao() : "Tự sinh khi hoàn tất") + " · Lệnh: " + ma
                + "\nBiển số / khách: " + lenhDangMo.getBienSo() + " · " + (phieu == null ? "Chưa có phiếu liên kết" : phieu.getTenKhach())
                + "\nTài sản khi tiếp nhận: " + (phieu == null || phieu.getTaiSan().isBlank() ? "Không ghi nhận" : phieu.getTaiSan()));
        txtThongTin.setCaretPosition(0); modelKetQua.setRowCount(0);
        for (HangMucSuaChua hm : lenhDangMo.getDanhSachHangMuc()) modelKetQua.addRow(new Object[] {hm.getTenHangMuc(), hm.getTaiKhoanThucHien(), hm.getKetQua()});
        txtNguoiGiao.setText(chiDoc ? giao.getNguoiGiao() : phien.get().getTenDangNhap()); txtNguoiNhan.setText(chiDoc ? giao.getNguoiNhan() : "");
        txtGioGiao.setText((chiDoc ? giao.getThoiDiemGiao() : LocalDateTime.now()).format(NGAY_GIO)); txtTinhTrang.setText(chiDoc ? giao.getTinhTrang() : "");
        txtTaiSan.setText(chiDoc ? giao.getTaiSanDaTra() : ""); txtGhiChu.setText(chiDoc ? giao.getGhiChu() : "");
        cboHinhThuc.setSelectedItem(chiDoc ? giao.getHinhThucXacNhan() : "Chọn hình thức xác nhận"); chkKhachXacNhan.setSelected(chiDoc);
        for (javax.swing.text.JTextComponent txt : List.of(txtNguoiNhan, txtGioGiao, txtTinhTrang, txtTaiSan, txtGhiChu)) txt.setEditable(!chiDoc);
        chkKhachXacNhan.setEnabled(!chiDoc); cboHinhThuc.setEnabled(!chiDoc); btnDocLai.setVisible(!chiDoc); btnXacNhan.setVisible(!chiDoc);
        String lyDo = chiDoc ? "" : dao.lyDoChuaDu(phien.get(), ma);
        coTheBanGiao = !chiDoc && lyDo.isEmpty();
        dangNap = false; daThayDoi = false; hienCanCu();
        lblLoi.setText(chiDoc ? "Đã lưu lúc " + giao.getThoiDiemGhiNhan().format(NGAY_GIO) + " · chỉ đọc" : lyDo);
        capNhatNut(); boCuc.show(pnlThe, "FORM"); SwingUtilities.invokeLater(() -> cuonForm.getViewport().setViewPosition(new Point(0, 0)));
    }

    private void hienCanCu() {
        if (canCuDaDoc == null) { txtThanhToan.setText("Chưa có xác nhận thu tiền/phê duyệt từ nguồn phụ trách.\nTiếp nhận không tự xác nhận thanh toán."); }
        else {
            NumberFormat so = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")); so.setMaximumFractionDigits(340);
            txtThanhToan.setText((canCuDaDoc.isNgoaiLe() ? "Phê duyệt ngoại lệ" : canCuDaDoc.duDieuKien() ? "Đã thanh toán đủ" : "Chưa thanh toán đủ")
                    + " · Chứng từ: " + canCuDaDoc.getMaChungTu() + "\nLệnh trên căn cứ: " + canCuDaDoc.getMaLenh() + "\nPhải thu: " + so.format(canCuDaDoc.getPhaiThu()) + " đ · Đã thu: " + so.format(canCuDaDoc.getDaThu())
                    + " đ\nNgười xác nhận: " + canCuDaDoc.getNguoiXacNhan().getTenDangNhap() + " · " + canCuDaDoc.getThoiDiem().format(NGAY_GIO)
                    + (canCuDaDoc.isNgoaiLe() ? "\nLý do phê duyệt: " + canCuDaDoc.getLyDo() : ""));
        }
        txtThanhToan.setCaretPosition(0);
        txtThanhToan.setBackground(canCuDaDoc != null && !canCuDaDoc.isNgoaiLe() && (chiDoc || coTheBanGiao) ? new Color(0xEBFAF0) : new Color(0xFFF7E3));
    }

    private void docLaiCanCu() {
        kiemTraPhien(); if (chiDoc) return;
        try {
            canCuDaDoc = dao.docCanCu(phien.get(), lenhDangMo.getMaPhieu());
            String lyDo = dao.lyDoChuaDu(phien.get(), lenhDangMo.getMaPhieu()); coTheBanGiao = lyDo.isEmpty();
            hienCanCu(); chkKhachXacNhan.setSelected(false); daThayDoi = true; lblLoi.setText(lyDo); capNhatNut();
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) { coTheBanGiao = false; lblLoi.setText(e.getMessage()); capNhatNut(); }
    }

    private void capNhatNut() {
        btnXacNhan.setEnabled(!chiDoc && coTheBanGiao && chkKhachXacNhan.isSelected());
    }

    private void doiChieu() {
        kiemTraPhien(); if (chiDoc) return;
        try {
            LocalDateTime lucGiao = LocalDateTime.parse(txtGioGiao.getText().strip(), NGAY_GIO);
            if (txtNguoiNhan.getText().isBlank() || txtTinhTrang.getText().isBlank() || cboHinhThuc.getSelectedIndex() <= 0 || !chkKhachXacNhan.isSelected()) throw new IllegalArgumentException("Cần người nhận, tình trạng xe, hình thức và xác nhận của khách.");
            if (canCuDaDoc == null || !coTheBanGiao) throw new IllegalStateException("Chưa có căn cứ hợp lệ để bàn giao.");
            String[][] noiDung = {
                {"Biển số / khách", lenhDangMo.getBienSo() + " · " + baoGiaDangMo.getBaoCao().getPhieu().getTenKhach()},
                {"Thanh toán / phê duyệt", (canCuDaDoc.isNgoaiLe() ? "Ngoại lệ quản lý" : "Đã thanh toán đủ") + " · " + canCuDaDoc.getMaChungTu()},
                {"Người giao", phien.get().getTenDangNhap()}, {"Người nhận", txtNguoiNhan.getText().strip()},
                {"Thời điểm giao thực tế", txtGioGiao.getText().strip()}, {"Tình trạng xe", txtTinhTrang.getText().strip()},
                {"Tài sản / phụ kiện đã trả", txtTaiSan.getText().strip()}, {"Khách xác nhận", (String) cboHinhThuc.getSelectedItem()},
                {"Ghi chú", txtGhiChu.getText().strip()}, {"Căn cứ xác nhận", canCuDaDoc.getNguoiXacNhan().getTenDangNhap() + " · " + canCuDaDoc.getThoiDiem().format(NGAY_GIO) + (canCuDaDoc.isNgoaiLe() ? " · " + canCuDaDoc.getLyDo() : "")}
            };
            String actor = phien.get().getTenDangNhap(), token = maThaoTac, maLenh = lenhDangMo.getMaPhieu();
            String nhan = txtNguoiNhan.getText(), tinhTrang = txtTinhTrang.getText(), taiSan = txtTaiSan.getText(), ghiChu = txtGhiChu.getText();
            String hinhThuc = (String) cboHinhThuc.getSelectedItem(); CanCuBanGiao canCu = canCuDaDoc;
            // Giữ đúng nội dung đã đối chiếu; nguồn tài chính vẫn được DAO đọc lại khi bấm hoàn tất.
            new BanGiaoXeDialog(SwingUtilities.getWindowAncestor(this), noiDung, true, () -> {
                TaiKhoan tk = phien.get();
                if (!actor.equals(tk.getTenDangNhap())) throw new SecurityException("Phiên đăng nhập đã đổi, cần mở lại form.");
                BanGiaoXe giao = dao.xacNhan(tk, token, maLenh, canCu, nhan, lucGiao, tinhTrang, taiSan, ghiChu, hinhThuc, true);
                daThayDoi = false; moDanhSach(); lblDanhSach.setText("Đã lưu " + giao.getMaBanGiao() + " · " + giao.getThoiDiemGiao().format(NGAY_GIO));
            }).setVisible(true);
        } catch (DateTimeParseException e) { lblLoi.setText("Giờ giao không hợp lệ; dùng dd/MM/yyyy HH:mm."); txtGioGiao.requestFocusInWindow(); }
        catch (IllegalArgumentException | IllegalStateException | SecurityException e) { lblLoi.setText(e.getMessage()); }
    }

    private static LocalDate docNgay(JTextField txt) { return txt.getText().isBlank() ? null : LocalDate.parse(txt.getText().strip(), NGAY); }
    private void kiemTraPhien() { if (phien.get() == null || phien.get().getVaiTro() != VaiTro.NHAN_VIEN_TIEP_NHAN) throw new SecurityException("Chỉ tiếp nhận được dùng chức năng bàn giao."); }
    private void theoDoi(javax.swing.text.JTextComponent txt) { txt.getDocument().addDocumentListener(new DocumentListener() { private void doi() { if (!dangNap && !chiDoc) daThayDoi = true; } @Override public void insertUpdate(DocumentEvent e) { doi(); } @Override public void removeUpdate(DocumentEvent e) { doi(); } @Override public void changedUpdate(DocumentEvent e) { doi(); } }); }
    private static JPanel nen(LayoutManager layout) { JPanel pnl = new JPanel(layout); pnl.setOpaque(false); return pnl; }
    private static JPanel khung(LayoutManager layout) { JPanel pnl = UiTheme.roundedPanel(layout, Color.WHITE, 8); pnl.setBorder(UiTheme.roundedBorder(UiTheme.APP_BORDER, 8, 16, 16)); return pnl; }
    private static JLabel nhan(String text, boolean dam) { JLabel lbl = new JLabel(text); lbl.setFont(dam ? UiTheme.APP_BOLD_FONT : UiTheme.APP_FONT); lbl.setForeground(dam ? UiTheme.APP_TEXT : UiTheme.APP_MUTED); return lbl; }
    private static JTextArea vanBan(int rows, boolean editable) { JTextArea txt = new JTextArea(rows, 20); txt.setFont(UiTheme.APP_FONT); txt.setLineWrap(true); txt.setWrapStyleWord(true); txt.setEditable(editable); return txt; }
    private static JButton nut(String text, boolean chinh, Runnable run) { JButton btn = UiTheme.createAppButton(text, chinh); btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 18, 36)); btn.addActionListener(e -> run.run()); return btn; }
    private static JPanel cot() { JPanel pnl = nen(null); pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS)); return pnl; }
    private static void them(JPanel cot, JComponent c, int cao) { c.setAlignmentX(Component.LEFT_ALIGNMENT); c.setMaximumSize(new Dimension(Integer.MAX_VALUE, cao)); c.setPreferredSize(new Dimension(0, cao)); cot.add(c); cot.add(Box.createVerticalStrut(10)); }
    private static JPanel hang(JComponent a, JComponent b) { JPanel pnl = nen(new GridLayout(1, 2, 14, 0)); pnl.add(a); pnl.add(b); return pnl; }
    private static JPanel truong(String text, JComponent c) { JPanel pnl = nen(new BorderLayout(0, 6)); JLabel lbl = nhan(text, true); lbl.setLabelFor(c); pnl.add(lbl, BorderLayout.NORTH); c.setFont(UiTheme.APP_FONT); pnl.add(c instanceof JTextArea ? new JScrollPane(c) : c); return pnl; }
    private static JPanel tieuDe(JLabel ten, String moTa, JButton btn) { JPanel pnl = nen(new BorderLayout(8, 10)); pnl.add(nhan("Tiếp nhận / Nghiệp vụ / Bàn giao xe", false), BorderLayout.NORTH); JPanel chu = nen(new BorderLayout(0, 6)); ten.setFont(UiTheme.APP_BOLD_FONT.deriveFont(24f)); chu.add(ten, BorderLayout.NORTH); chu.add(nhan(moTa, false)); pnl.add(chu); if (btn != null) { JPanel nut = nen(new FlowLayout(FlowLayout.RIGHT, 0, 0)); nut.add(btn); pnl.add(nut, BorderLayout.EAST); } return pnl; }
    private static DefaultTableModel model(String... cot) { return new DefaultTableModel(cot, 0) { private static final long serialVersionUID = 1L; @Override public boolean isCellEditable(int r, int c) { return false; } }; }
    private static JTable bang(DefaultTableModel model) { JTable tbl = new JTable(model); tbl.setFont(UiTheme.APP_FONT); tbl.setRowHeight(44); tbl.setAutoCreateRowSorter(true); tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); tbl.getTableHeader().setFont(UiTheme.APP_BOLD_FONT); tbl.getTableHeader().setReorderingAllowed(false); return tbl; }
    private static JScrollPane cuonBang(JTable tbl) { JScrollPane cuon = new JScrollPane(tbl); cuon.getViewport().setBackground(Color.WHITE); return cuon; }
    private static final class NoiDungCuon extends JPanel implements Scrollable { private static final long serialVersionUID = 1L; NoiDungCuon() { super(new BorderLayout()); setOpaque(false); } @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); } @Override public int getScrollableUnitIncrement(Rectangle r,int o,int d) { return 20; } @Override public int getScrollableBlockIncrement(Rectangle r,int o,int d) { return Math.max(20,r.height-20); } @Override public boolean getScrollableTracksViewportWidth() { return true; } @Override public boolean getScrollableTracksViewportHeight() { return false; } }
    private static JScrollPane cuonNoiDung(JPanel pnl) { JPanel neo = new NoiDungCuon(); neo.add(pnl, BorderLayout.NORTH); JScrollPane cuon = new JScrollPane(neo); cuon.getViewport().setBackground(UiTheme.APP_BACKGROUND); cuon.setBorder(null); cuon.getVerticalScrollBar().setUnitIncrement(20); return cuon; }
}

