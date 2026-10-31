package ui.screen;

import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import entity.BaoGia;
import entity.BaoCaoKiemTra;
import entity.ChiTietBaoGia;
import entity.KhachHang;
import entity.PhieuTiepNhan;
import entity.TaiKhoan;
import entity.TrangThaiKiemTra;
import entity.VaiTro;
import entity.Xe;
import ui.component.UiTheme;
import ui.dialog.HangMucBaoGiaDialog;

/** UI báo giá dùng fixture có nhãn; không lưu hay chuyển trạng thái nghiệp vụ. */
public final class BaoGiaPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm", Locale.ROOT).withResolverStyle(ResolverStyle.STRICT);
    private final Supplier<TaiKhoan> phien;
    private final CardLayout boCuc = new CardLayout();
    private final JPanel pnlThe = nen(boCuc);
    private final List<BaoCaoKiemTra> baoCaoHoanTat = new ArrayList<>();
    private final List<MauBaoGia> duLieuMau = new ArrayList<>();
    /** Các dòng đang sửa tách khỏi bản mẫu, nhất là bản mẫu đã được duyệt. */
    private final List<ChiTietBaoGia> banNhap = new ArrayList<>();
    private final JTextField txtTim = new JTextField();
    private final JComboBox<String> cboTrangThai = new JComboBox<>(new String[] {"Tất cả trạng thái", "Chờ khách xác nhận", "Đã đồng ý", "Điều chỉnh", "Từ chối"});
    private final DefaultTableModel modelDanhSach = model("Mã báo giá", "Phiếu tiếp nhận", "Biển số", "Tổng dự kiến", "Phiên bản", "Trạng thái");
    private final JTable tblDanhSach = bang(modelDanhSach);
    private final JLabel lblDanhSach = nhan(" ", false);
    private final JComboBox<String> cboBaoCao = new JComboBox<>();
    private final JTextArea txtDeXuat = new JTextArea(3, 20);
    private final JLabel lblCanCu = nhan("Chọn kết quả kiểm tra hoàn tất.", false);
    private final JLabel lblPhienBan = nhan("Bản nháp · chưa lưu", true);
    private final JTextField txtHen = new JTextField();
    private final DefaultTableModel modelChiTiet = model("Hạng mục", "Loại", "SL", "ĐVT", "Đơn giá", "Thành tiền");
    private final JTable tblChiTiet = bang(modelChiTiet);
    private final JLabel lblTong = nhan("0 đ", true);
    private final JLabel lblLoi = nhan(" ", false);
    private final DefaultTableModel modelQuyetDinh = model("Hạng mục", "SL / ĐVT", "Thành tiền");
    private final JLabel lblBaoGiaGui = nhan(" ", true);
    private final JRadioButton radDongY = new JRadioButton("Đồng ý báo giá");
    private final JRadioButton radDieuChinh = new JRadioButton("Yêu cầu điều chỉnh");
    private final JRadioButton radTuChoi = new JRadioButton("Từ chối báo giá");
    private final ButtonGroup nhomQuyetDinh = new ButtonGroup();
    private final JTextField txtPhanHoiLuc = new JTextField();
    private final JTextField txtKenh = new JTextField();
    private final JTextArea txtGhiChu = new JTextArea(3, 20);
    private final JButton btnGhiNhan = UiTheme.createAppButton("Ghi nhận quyết định", true);
    private final JLabel lblPhanHoi = nhan("Chưa chọn phản hồi · chưa lưu nghiệp vụ.", false);
    private String maDangXem;
    private boolean dangNap, daThayDoi;

    /** Mẫu độc lập phục vụ duyệt UI; không đọc kho kỹ thuật bằng tài khoản giả. */
    public BaoGiaPanel(Supplier<TaiKhoan> phien) { this(baoCaoMau(), phien); }

    /** Nhận dữ liệu đọc để dễ nối nguồn kiểm tra; chỉ lấy báo cáo đã hoàn tất. */
    public BaoGiaPanel(List<BaoCaoKiemTra> baoCao, Supplier<TaiKhoan> phien) {
        super(new BorderLayout());
        this.phien = Objects.requireNonNull(phien); kiemTraPhien(); setOpaque(false);
        for (BaoCaoKiemTra bc : baoCao) if (bc.getTrangThai() == TrangThaiKiemTra.HOAN_TAT) baoCaoHoanTat.add(bc);
        List<ChiTietBaoGia> dongMau = List.of(new ChiTietBaoGia("Bảo dưỡng định kỳ", "Dịch vụ", "công", "1", "300000"),
                new ChiTietBaoGia("Vệ sinh phanh trước", "Dịch vụ", "công", "1", "100000"),
                new ChiTietBaoGia("Dầu động cơ 5W-30", "Phụ tùng", "lít", "4", "180000"),
                new ChiTietBaoGia("Lọc dầu", "Phụ tùng", "cái", "1", "150000"));
        if (!baoCaoHoanTat.isEmpty()) {
            BaoCaoKiemTra bc = baoCaoHoanTat.get(0);
            duLieuMau.add(new MauBaoGia(new BaoGia("BG-MAU-01", bc.getPhieu().getBienSo(), false, dongMau), bc, "V1", "Chờ khách xác nhận"));
            duLieuMau.add(new MauBaoGia(new BaoGia("BG-MAU-02", bc.getPhieu().getBienSo(), true, dongMau.subList(0, 2)), bc, "V1", "Đã đồng ý"));
            duLieuMau.add(new MauBaoGia(new BaoGia("BG-MAU-03", bc.getPhieu().getBienSo(), false, dongMau.subList(2, 4)), bc, "V2", "Điều chỉnh"));
        }
        pnlThe.add(taoDanhSach(), "DANH_SACH"); pnlThe.add(taoForm(), "FORM"); pnlThe.add(taoQuyetDinh(), "QUYET_DINH"); add(pnlThe);
        cboBaoCao.addActionListener(e -> { if (!dangNap) { capNhatCanCu(); daThayDoi = true; } });
        theoDoi(txtHen); theoDoi(txtPhanHoiLuc); theoDoi(txtKenh); theoDoi(txtGhiChu);
        locDanhSach();
    }

    public boolean choPhepRoiManHinh() {
        return !daThayDoi || JOptionPane.showConfirmDialog(this, "Bản nhập báo giá/phản hồi chưa lưu. Bỏ thay đổi?",
                "Dữ liệu chưa lưu", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public void moDanhSach() { daThayDoi = false; boCuc.show(pnlThe, "DANH_SACH"); locDanhSach(); }

    private JPanel taoDanhSach() {
        JPanel pnl = nen(new BorderLayout(0, 16)); JPanel dau = nen(new BorderLayout(0, 16));
        dau.add(tieuDe("Báo giá", "Báo giá dựa trên kết quả kiểm tra hoàn tất.", nut("+ Lập báo giá", true, () -> moForm(null))), BorderLayout.NORTH);
        JPanel loc = khung(new BorderLayout(8, 0)); txtTim.setBorder(BorderFactory.createTitledBorder("Mã báo giá / khách / biển số"));
        txtTim.addActionListener(e -> locDanhSach()); loc.add(txtTim);
        JPanel nut = nen(new FlowLayout(FlowLayout.RIGHT, 8, 0)); cboTrangThai.addActionListener(e -> locDanhSach()); nut.add(cboTrangThai);
        nut.add(nut("Tìm", false, this::locDanhSach)); nut.add(nut("Làm mới", false, () -> { txtTim.setText(""); cboTrangThai.setSelectedIndex(0); locDanhSach(); }));
        loc.add(nut, BorderLayout.EAST); dau.add(loc); pnl.add(dau, BorderLayout.NORTH);
        JPanel ds = khung(new BorderLayout(0, 12)); ds.add(nhan("Danh sách báo giá · DỮ LIỆU MINH HỌA", true), BorderLayout.NORTH); ds.add(cuonBang(tblDanhSach));
        JPanel tacVu = nen(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tacVu.add(nut("Xem chi tiết", false, () -> moDaChon(false)));
        tacVu.add(nut("Điều chỉnh / phát sinh", false, () -> moDaChon(true)));
        tacVu.add(nut("Phản hồi khách", false, this::phanHoiMau)); ds.add(tacVu, BorderLayout.SOUTH);
        pnl.add(ds); pnl.add(lblDanhSach, BorderLayout.SOUTH); return pnl;
    }

    private JPanel taoForm() {
        JPanel pnl = nen(new BorderLayout(0, 16));
        pnl.add(tieuDe("Lập / điều chỉnh báo giá", "1. Kết quả kiểm tra → 2. Hạng mục & chi phí → 3. Khách phản hồi",
                nut("Danh sách / quay lại", false, () -> { if (choPhepRoiManHinh()) moDanhSach(); })), BorderLayout.NORTH);
        JPanel khung = khung(new BorderLayout(0, 16)); JPanel dau = nen(new GridLayout(0, 1, 0, 8));
        dau.add(nhan("Báo cáo kiểm tra hoàn tất · dữ liệu minh họa", true));
        for (BaoCaoKiemTra bc : baoCaoHoanTat) cboBaoCao.addItem(bc.getMaBaoCao() + " · " + bc.getPhieu().getBienSo());
        dau.add(cboBaoCao); dau.add(lblCanCu); dau.add(lblPhienBan);
        txtDeXuat.setEditable(false); txtDeXuat.setLineWrap(true); txtDeXuat.setWrapStyleWord(true); txtDeXuat.setFont(UiTheme.APP_FONT);
        dau.add(new JScrollPane(txtDeXuat));
        dau.add(truong("Ngày hẹn dự kiến * · dd/MM/yyyy HH:mm", txtHen)); epCot(dau); khung.add(dau, BorderLayout.NORTH);
        JPanel chiTiet = nen(new BorderLayout(0, 10)); JScrollPane bang = cuonBang(tblChiTiet); bang.setPreferredSize(new Dimension(0, 250)); chiTiet.add(bang);
        JPanel tacVu = nen(new FlowLayout(FlowLayout.LEFT, 8, 0)); tacVu.add(nut("Thêm dòng", false, () -> suaDong(false)));
        tacVu.add(nut("Sửa dòng", false, () -> suaDong(true))); tacVu.add(nut("Xóa dòng", false, this::xoaDong)); chiTiet.add(tacVu, BorderLayout.SOUTH); khung.add(chiTiet);
        JPanel chan = nen(new GridLayout(0, 1, 0, 10)); chan.add(lblTong); lblLoi.setForeground(UiTheme.ERROR); chan.add(lblLoi);
        chan.add(nhan("Điều chỉnh cần duyệt lại; bản UI chưa lưu hoặc gửi báo giá thực tế.", false));
        chan.add(nut("Kiểm tra & gửi báo giá", true, this::xemTruoc)); khung.add(chan, BorderLayout.SOUTH);
        pnl.add(cuonNoiDung(khung)); return pnl;
    }

    private JPanel taoQuyetDinh() {
        JPanel pnl = nen(new BorderLayout(0, 16)); pnl.add(tieuDe("Ghi nhận quyết định khách hàng", "Đối chiếu đúng báo giá và phiên bản đã gửi khách.",
                nut("Quay lại", false, this::quayLaiForm)), BorderLayout.NORTH);
        JPanel hang = nen(new GridLayout(1, 2, 16, 0)); JPanel trai = khung(new BorderLayout(0, 12));
        trai.add(lblBaoGiaGui, BorderLayout.NORTH); trai.add(cuonBang(bang(modelQuyetDinh))); hang.add(trai);
        JPanel phai = khung(new BorderLayout(0, 14)); JPanel truong = nen(new GridLayout(0, 1, 0, 10));
        truong.add(nhan("Kết quả phản hồi khách · không mặc định đồng ý", true));
        for (JRadioButton rad : List.of(radDongY, radDieuChinh, radTuChoi)) {
            rad.setFont(UiTheme.APP_FONT); rad.setOpaque(false); nhomQuyetDinh.add(rad);
            rad.addActionListener(e -> { btnGhiNhan.setEnabled(true); daThayDoi = true; lblPhanHoi.setText("Phản hồi chưa lưu nghiệp vụ."); }); truong.add(rad);
        }
        truong.add(truong("Thời điểm phản hồi * · dd/MM/yyyy HH:mm", txtPhanHoiLuc));
        truong.add(truong("Kênh phản hồi *", txtKenh)); txtGhiChu.setLineWrap(true); txtGhiChu.setWrapStyleWord(true);
        truong.add(truong("Ghi chú phản hồi", txtGhiChu)); epCot(truong); phai.add(truong, BorderLayout.NORTH);
        JPanel chan = nen(new GridLayout(0, 1, 0, 10)); chan.add(lblPhanHoi); chan.add(nhan("Từ chối/điều chỉnh: không tạo lệnh từ bản hiện tại.", false));
        btnGhiNhan.setEnabled(false); btnGhiNhan.addActionListener(e -> kiemTraPhanHoi()); chan.add(btnGhiNhan); phai.add(chan, BorderLayout.SOUTH); hang.add(phai);
        hang.setPreferredSize(new Dimension(600, 680)); JScrollPane cuon = cuonNoiDung(hang);
        cuon.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                boolean hep = cuon.getViewport().getWidth() < 850; GridLayout layout = (GridLayout) hang.getLayout();
                layout.setRows(hep ? 2 : 1); layout.setColumns(hep ? 1 : 2); layout.setVgap(16);
                hang.setPreferredSize(new Dimension(600, hep ? 1300 : 680)); hang.revalidate();
            }
        }); pnl.add(cuon); return pnl;
    }

    private void moForm(MauBaoGia mau) {
        kiemTraPhien(); dangNap = true; banNhap.clear(); maDangXem = mau == null ? null : mau.baoGia.getMaBaoGia();
        if (mau != null) banNhap.addAll(mau.baoGia.getChiTiet());
        cboBaoCao.setSelectedIndex(baoCaoHoanTat.isEmpty() ? -1 : 0);
        if (mau != null) for (int i = 0; i < baoCaoHoanTat.size(); i++) if (baoCaoHoanTat.get(i).getMaBaoCao().equals(mau.baoCao.getMaBaoCao())) cboBaoCao.setSelectedIndex(i);
        txtHen.setText(LocalDateTime.now().plusDays(1).format(NGAY)); lblPhienBan.setText(mau == null ? "Chưa cấp mã · bản nháp UI" : mau.baoGia.getMaBaoGia() + " · " + mau.phienBan + " · " + mau.trangThai + " · bản gốc chỉ đọc");
        cboBaoCao.setEnabled(mau == null); txtHen.setEditable(mau == null);
        lblLoi.setText(" "); capNhatCanCu(); capNhatChiTiet(); daThayDoi = false; dangNap = false; boCuc.show(pnlThe, "FORM");
    }

    private void moDaChon(boolean dieuChinh) {
        MauBaoGia mau = mauDangChon(); if (mau == null) return; moForm(mau);
        if (dieuChinh) {
            lblPhienBan.setText(mau.baoGia.getMaBaoGia() + " · bản điều chỉnh/phát sinh chưa lưu, cần khách duyệt lại");
            maDangXem = null; cboBaoCao.setEnabled(true); txtHen.setEditable(true); daThayDoi = true;
        }
    }

    private MauBaoGia mauDangChon() {
        kiemTraPhien(); int row = tblDanhSach.getSelectedRow(); if (row < 0) { lblDanhSach.setText("Chọn báo giá mẫu trước."); return null; }
        String ma = (String) modelDanhSach.getValueAt(tblDanhSach.convertRowIndexToModel(row), 0);
        for (MauBaoGia mau : duLieuMau) if (mau.baoGia.getMaBaoGia().equals(ma)) return mau;
        throw new IllegalArgumentException("Không có báo giá mẫu.");
    }

    private void suaDong(boolean sua) {
        kiemTraPhien();
        if (maDangXem != null) { lblLoi.setText("Bản gốc chỉ đọc. Dùng Điều chỉnh / phát sinh từ danh sách để tạo bản nhập riêng."); return; }
        int row = tblChiTiet.getSelectedRow(); if (sua && row < 0) { lblLoi.setText("Chọn dòng để sửa."); return; }
        int index = sua ? tblChiTiet.convertRowIndexToModel(row) : -1;
        new HangMucBaoGiaDialog(SwingUtilities.getWindowAncestor(this), sua ? banNhap.get(index) : null, dong -> {
            kiemTraPhien(); if (index < 0) banNhap.add(dong); else banNhap.set(index, dong);
            daThayDoi = true; capNhatChiTiet(); lblLoi.setText(" ");
        }).setVisible(true);
    }

    private void xoaDong() {
        kiemTraPhien(); if (maDangXem != null) { lblLoi.setText("Bản gốc chỉ đọc; cần tạo bản điều chỉnh riêng."); return; }
        int row = tblChiTiet.getSelectedRow(); if (row < 0) { lblLoi.setText("Chọn dòng để xóa."); return; }
        int index = tblChiTiet.convertRowIndexToModel(row);
        if (JOptionPane.showConfirmDialog(this, "Xóa dòng khỏi bản nhập?", "Xóa hạng mục", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            banNhap.remove(index); daThayDoi = true; capNhatChiTiet();
        }
    }

    private void capNhatCanCu() {
        int index = cboBaoCao.getSelectedIndex();
        if (index < 0) { lblCanCu.setText("Không có báo cáo hoàn tất; chưa thể lập báo giá."); txtDeXuat.setText(""); return; }
        BaoCaoKiemTra bc = baoCaoHoanTat.get(index); PhieuTiepNhan phieu = bc.getPhieu();
        lblCanCu.setText(phieu.getMaPhieu() + " · " + phieu.getTenKhach() + " · " + phieu.getBienSo());
        txtDeXuat.setText("Kết quả: " + bc.getNoiDung()[0] + "\nĐề xuất: " + bc.getNoiDung()[2]);
        txtDeXuat.setCaretPosition(0);
    }

    private void capNhatChiTiet() {
        modelChiTiet.setRowCount(0); for (ChiTietBaoGia dong : banNhap) modelChiTiet.addRow(new Object[] {dong.getTenHangMuc(), dong.getLoai(), dong.getSoLuong(), dong.getDonVi(), dong.getDonGia(), dong.getThanhTien()});
        lblTong.setText("Tổng dự kiến: " + tien(tong(banNhap)) + " đ · tự tính từ chi tiết");
    }

    private void xemTruoc() {
        kiemTraPhien();
        if (cboBaoCao.getSelectedIndex() < 0) { lblLoi.setText("Cần báo cáo kiểm tra hoàn tất."); return; }
        if (banNhap.isEmpty()) { lblLoi.setText("Báo giá cần ít nhất một hạng mục."); return; }
        try { LocalDateTime.parse(txtHen.getText().strip(), NGAY); }
        catch (DateTimeParseException e) { lblLoi.setText("Ngày hẹn không hợp lệ; dùng dd/MM/yyyy HH:mm."); return; }
        moPhanHoi(banNhap, (maDangXem == null ? "Bản nhập chưa lưu" : maDangXem) + " · " + lblCanCu.getText());
    }

    private void phanHoiMau() {
        MauBaoGia mau = mauDangChon(); if (mau == null) return; moForm(mau);
        moPhanHoi(mau.baoGia.getChiTiet(), mau.baoGia.getMaBaoGia() + " · " + mau.phienBan + " · " + mau.baoCao.getPhieu().getTenKhach());
    }

    private void moPhanHoi(List<ChiTietBaoGia> dong, String ten) {
        dangNap = true; modelQuyetDinh.setRowCount(0);
        for (ChiTietBaoGia item : dong) modelQuyetDinh.addRow(new Object[] {item.getTenHangMuc(), item.getSoLuong().toPlainString() + " " + item.getDonVi(), item.getThanhTien()});
        lblBaoGiaGui.setText(ten); nhomQuyetDinh.clearSelection(); btnGhiNhan.setEnabled(false);
        txtPhanHoiLuc.setText(LocalDateTime.now().format(NGAY)); txtKenh.setText(""); txtGhiChu.setText("");
        lblPhanHoi.setText("Chưa chọn phản hồi · chưa lưu nghiệp vụ."); dangNap = false; boCuc.show(pnlThe, "QUYET_DINH");
    }

    /** Quay về sửa nội dung sẽ làm phản hồi hiện tại không còn khớp bản xem trước. */
    private void quayLaiForm() {
        boolean daNhapPhanHoi = nhomQuyetDinh.getSelection() != null || !txtKenh.getText().isBlank() || !txtGhiChu.getText().isBlank();
        if (daNhapPhanHoi && JOptionPane.showConfirmDialog(this, "Phản hồi chưa lưu. Bỏ phản hồi để quay lại báo giá?",
                "Phản hồi chưa lưu", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        nhomQuyetDinh.clearSelection(); btnGhiNhan.setEnabled(false); boCuc.show(pnlThe, "FORM");
    }

    private void kiemTraPhanHoi() {
        kiemTraPhien(); if (nhomQuyetDinh.getSelection() == null) { lblPhanHoi.setText("Cần chọn phản hồi khách."); return; }
        try { LocalDateTime.parse(txtPhanHoiLuc.getText().strip(), NGAY); }
        catch (DateTimeParseException e) { lblPhanHoi.setText("Thời điểm phản hồi không hợp lệ."); return; }
        if (txtKenh.getText().isBlank()) { lblPhanHoi.setText("Cần kênh phản hồi của khách."); return; }
        lblPhanHoi.setText("Đã kiểm tra nội dung phản hồi · bản UI chưa lưu/gửi nghiệp vụ.");
    }

    private void locDanhSach() {
        kiemTraPhien(); modelDanhSach.setRowCount(0); String tu = txtTim.getText().strip().toLowerCase(Locale.ROOT);
        for (MauBaoGia mau : duLieuMau) {
            String chu = mau.baoGia.getMaBaoGia() + " " + mau.baoCao.getPhieu().getTenKhach() + " " + mau.baoGia.getBienSo();
            if (chu.toLowerCase(Locale.ROOT).contains(tu) && (cboTrangThai.getSelectedIndex() == 0 || mau.trangThai.equals(cboTrangThai.getSelectedItem()))) {
                modelDanhSach.addRow(new Object[] {mau.baoGia.getMaBaoGia(), mau.baoCao.getPhieu().getMaPhieu(), mau.baoGia.getBienSo(), tong(mau.baoGia.getChiTiet()), mau.phienBan, mau.trangThai});
            }
        }
        lblDanhSach.setText(modelDanhSach.getRowCount() + " báo giá minh họa · chưa lưu/gửi, chưa nối nguồn kiểm tra thật.");
    }

    private void kiemTraPhien() { if (Objects.requireNonNull(phien.get()).getVaiTro() != VaiTro.NHAN_VIEN_TIEP_NHAN) throw new SecurityException("Chỉ nhân viên tiếp nhận được mở báo giá."); }
    private void theoDoi(javax.swing.text.JTextComponent txt) { txt.getDocument().addDocumentListener(new DocumentListener() {
        @Override public void insertUpdate(DocumentEvent e) { thayDoi(); } @Override public void removeUpdate(DocumentEvent e) { thayDoi(); }
        @Override public void changedUpdate(DocumentEvent e) { thayDoi(); } private void thayDoi() { if (!dangNap) daThayDoi = true; }
    }); }
    private static BigDecimal tong(List<ChiTietBaoGia> dong) { BigDecimal tong = BigDecimal.ZERO; for (ChiTietBaoGia ct : dong) tong = tong.add(ct.getThanhTien()); return tong; }
    private static String tien(BigDecimal giaTri) { NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")); nf.setMaximumFractionDigits(340); return nf.format(giaTri); }
    /** Giữ nhịp form gọn: nhãn/radio không bị GridLayout kéo cao theo textarea. */
    private static void epCot(JPanel pnl) {
        Component[] cacThanhPhan = pnl.getComponents();
        pnl.removeAll(); pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        for (Component component : cacThanhPhan) {
            JComponent c = (JComponent) component;
            int cao = c instanceof JComboBox ? 36 : c.getPreferredSize().height;
            if (c instanceof JScrollPane) cao = 65;
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            c.setMaximumSize(new Dimension(Integer.MAX_VALUE, cao));
            c.setPreferredSize(new Dimension(0, cao));
            pnl.add(c); pnl.add(Box.createVerticalStrut(8));
        }
    }

    private static JPanel nen(LayoutManager layout) { JPanel pnl = new JPanel(layout); pnl.setOpaque(false); return pnl; }
    private static JPanel khung(LayoutManager layout) { JPanel pnl = UiTheme.roundedPanel(layout, Color.WHITE, 8); pnl.setBorder(UiTheme.roundedBorder(UiTheme.APP_BORDER, 8, 16, 16)); return pnl; }
    private static JLabel nhan(String chu, boolean dam) { JLabel lbl = new JLabel(chu); lbl.setFont(dam ? UiTheme.APP_BOLD_FONT : UiTheme.APP_FONT); lbl.setForeground(dam ? UiTheme.APP_TEXT : UiTheme.APP_MUTED); return lbl; }
    private static JButton nut(String ten, boolean chinh, Runnable action) { JButton btn = UiTheme.createAppButton(ten, chinh); btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 18, 36)); btn.addActionListener(e -> action.run()); return btn; }
    private static JPanel truong(String ten, JComponent component) { JPanel pnl = nen(new BorderLayout(0, 6)); JLabel lbl = nhan(ten, true); lbl.setLabelFor(component); pnl.add(lbl, BorderLayout.NORTH); component.setFont(UiTheme.APP_FONT); component.setPreferredSize(new Dimension(0, 40)); pnl.add(component instanceof JTextArea ? new JScrollPane(component) : component); return pnl; }
    private static JPanel tieuDe(String ten, String moTa, JButton thaoTac) { JPanel pnl = nen(new BorderLayout(8, 10)); pnl.add(nhan("Tiếp nhận / Nghiệp vụ / " + ten, false), BorderLayout.NORTH); JPanel chu = nen(new BorderLayout(0, 6)); JLabel lbl = nhan(ten, true); lbl.setFont(UiTheme.APP_BOLD_FONT.deriveFont(24f)); chu.add(lbl, BorderLayout.NORTH); chu.add(nhan(moTa, false)); pnl.add(chu); JPanel nut = nen(new FlowLayout(FlowLayout.RIGHT, 0, 0)); nut.add(thaoTac); pnl.add(nut, BorderLayout.EAST); return pnl; }
    private static DefaultTableModel model(String... cot) { return new DefaultTableModel(cot, 0) {
        private static final long serialVersionUID = 1L;
        @Override public boolean isCellEditable(int row, int column) { return false; }
        @Override public Class<?> getColumnClass(int col) { return getRowCount() > 0 && getValueAt(0, col) instanceof BigDecimal ? BigDecimal.class : String.class; }
    }; }
    private static JTable bang(DefaultTableModel model) {
        JTable tbl = new JTable(model); tbl.setFont(UiTheme.APP_FONT); tbl.setRowHeight(44); tbl.setAutoCreateRowSorter(true); tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); tbl.getTableHeader().setFont(UiTheme.APP_BOLD_FONT); tbl.getTableHeader().setReorderingAllowed(false);
        tbl.setDefaultRenderer(BigDecimal.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override public void setValue(Object value) { setHorizontalAlignment(SwingConstants.RIGHT); setText(value instanceof BigDecimal n ? tien(n) : ""); }
        }); return tbl;
    }
    private static JScrollPane cuonBang(JTable tbl) { JScrollPane cuon = new JScrollPane(tbl); cuon.getViewport().setBackground(Color.WHITE); return cuon; }
    private static final class NoiDungCuon extends JPanel implements Scrollable {
        private static final long serialVersionUID = 1L; NoiDungCuon() { super(new BorderLayout()); setOpaque(false); }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 20; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return Math.max(20, r.height - 20); }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }
    private static JScrollPane cuonNoiDung(JPanel pnl) { JPanel neo = new NoiDungCuon(); neo.add(pnl, BorderLayout.NORTH); JScrollPane cuon = new JScrollPane(neo); cuon.setBorder(null); cuon.getViewport().setBackground(UiTheme.APP_BACKGROUND); cuon.getVerticalScrollBar().setUnitIncrement(20); return cuon; }
    /** Fixture chỉ dùng UI, không bổ sung entity báo giá trùng với entity hiện có. */
    private static final class MauBaoGia {
        final BaoGia baoGia; final BaoCaoKiemTra baoCao; final String phienBan, trangThai;
        MauBaoGia(BaoGia baoGia, BaoCaoKiemTra baoCao, String phienBan, String trangThai) { this.baoGia = baoGia; this.baoCao = baoCao; this.phienBan = phienBan; this.trangThai = trangThai; }
    }
    private static List<BaoCaoKiemTra> baoCaoMau() {
        Xe xe = new Xe("XE-MAU", "51H-246.80", "KH-MAU", "Toyota", "Camry", 2022, "Đen", "", "");
        KhachHang kh = new KhachHang("KH-MAU", "Nguyễn Văn An", "0901234567", "", "", "");
        PhieuTiepNhan phieu = new PhieuTiepNhan("TN-MAU-01", xe, kh, 45280, LocalDateTime.of(2026, 10, 27, 8, 0), "Phanh kêu", "", "Kiểm tra phanh", "tiepnhan-mau", LocalDateTime.of(2026, 10, 27, 8, 5));
        BaoCaoKiemTra bc = new BaoCaoKiemTra("KT-MAU-01", phieu, LocalDate.of(2026, 10, 27), "Tổng hợp", new String[] {"Phanh cần vệ sinh", "", "Vệ sinh phanh và bảo dưỡng", "Đạt", "Cần thay", "Đạt", ""}, TrangThaiKiemTra.HOAN_TAT, LocalDateTime.of(2026, 10, 27, 9, 0));
        return List.of(bc);
    }
}
