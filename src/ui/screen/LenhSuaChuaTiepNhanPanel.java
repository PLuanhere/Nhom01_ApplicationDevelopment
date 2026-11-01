package ui.screen;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import dao.PhieuSuaChuaDAO;
import entity.*;
import ui.component.UiTheme;

/** Màn tiếp nhận dùng kho lệnh hiện có; không chứa thao tác cập nhật tiến độ của kỹ thuật. */
public final class LenhSuaChuaTiepNhanPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm", Locale.ROOT)
            .withResolverStyle(ResolverStyle.STRICT);
    private final PhieuSuaChuaDAO dao;
    private final Supplier<TaiKhoan> phien;
    private final CardLayout boCuc = new CardLayout();
    private final JPanel pnlThe = nen(boCuc);
    private JScrollPane cuonForm;
    private final JTextField txtTim = new JTextField();
    private final JComboBox<String> cboTrangThai = new JComboBox<>(new String[] {
            "Tất cả", "Chờ sửa chữa", "Đang sửa", "Chờ phụ tùng", "Hoàn thành"});
    private final DefaultTableModel modelDanhSach = chiDoc("Mã lệnh", "Biển số", "Kỹ thuật viên", "Hạn dự kiến", "Trạng thái");
    private final JTable tblDanhSach = bang(modelDanhSach);
    private final JLabel lblDanhSach = nhan(" ", false);
    private final JComboBox<BaoGia> cboBaoGia = new JComboBox<>();
    private final JTextArea txtCanCu = vanBan(3, false);
    private final JComboBox<Object> cboChinh = new JComboBox<>();
    private final JComboBox<Object> cboHoTro = new JComboBox<>();
    private final JComboBox<KyThuatVienPhanCong> cboPhuTrach = new JComboBox<>();
    private final JTextField txtMa = new JTextField();
    private final JTextField txtHan = new JTextField();
    private final JTextArea txtGhiChu = vanBan(2, true);
    private final JLabel lblLoi = nhan(" ", false);
    private final JButton btnPhatHanh = nut("Phát hành lệnh & phân công", true, this::phatHanh);
    private final DefaultTableModel modelHangMuc = new DefaultTableModel(
            new String[] {"Hạng mục được duyệt", "Số lượng", "Kỹ thuật viên phụ trách", "Tình trạng"}, 0) {
        private static final long serialVersionUID = 1L;
        @Override public boolean isCellEditable(int row, int column) { return !chiDoc && column == 2; }
    };
    private final JTable tblHangMuc = bang(modelHangMuc);
    private BaoGia baoGiaChon;
    private boolean chiDoc, dangNap, daThayDoi;
    private String maLuot;

    public LenhSuaChuaTiepNhanPanel(PhieuSuaChuaDAO dao, Supplier<TaiKhoan> phien) {
        super(new BorderLayout());
        this.dao = Objects.requireNonNull(dao); this.phien = Objects.requireNonNull(phien);
        kiemTraPhien(); setOpaque(false);
        pnlThe.add(taoDanhSach(), "DANH_SACH"); pnlThe.add(taoForm(), "FORM"); add(pnlThe);
        cboBaoGia.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                setText(value instanceof BaoGia bg ? bg.getMaBaoGia() + " · V" + bg.getPhienBan() + " · Đã đồng ý" : "Chưa có báo giá đủ điều kiện");
                return this;
            }
        });
        for (JComboBox<?> cbo : List.of(cboChinh, cboHoTro, cboPhuTrach)) {
            cbo.setRenderer(new DefaultListCellRenderer() {
                private static final long serialVersionUID = 1L;
                @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
                    super.getListCellRendererComponent(list, value, index, selected, focus);
                    setText(value == null ? "Chưa phân công" : value.toString()); return this;
                }
            });
        }
        cboBaoGia.addActionListener(e -> { if (!dangNap) doiBaoGia(); });
        cboChinh.addActionListener(e -> { if (!dangNap) doiNhanSu(); });
        cboHoTro.addActionListener(e -> { if (!dangNap) doiNhanSu(); });
        tblHangMuc.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(cboPhuTrach));
        DefaultTableCellRenderer soLuong = new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(table, value, selected, focus, row, col);
                setHorizontalAlignment(SwingConstants.RIGHT);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        };
        tblHangMuc.getColumnModel().getColumn(1).setCellRenderer(soLuong);
        int[] rongCot = {360, 150, 330, 200};
        for (int i = 0; i < rongCot.length; i++) tblHangMuc.getColumnModel().getColumn(i).setPreferredWidth(rongCot[i]);
        modelHangMuc.addTableModelListener(e -> { if (!dangNap) daThayDoi = true; });
        theoDoi(txtHan); theoDoi(txtGhiChu); moDanhSach();
    }

    public boolean choPhepRoiManHinh() {
        ketThucSuaO();
        return !daThayDoi || JOptionPane.showConfirmDialog(this, "Phân công chưa phát hành. Bỏ thay đổi?",
                "Dữ liệu chưa lưu", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public void moDanhSach() {
        kiemTraPhien(); daThayDoi = false; locDanhSach(); boCuc.show(pnlThe, "DANH_SACH");
    }

    private JPanel taoDanhSach() {
        JPanel pnl = nen(new BorderLayout(0, 16)), dau = nen(new BorderLayout(0, 16));
        dau.add(tieuDe("Lệnh sửa chữa", "Tạo lệnh sau khi khách đã đồng ý các hạng mục.",
                nut("+ Tạo lệnh từ báo giá", true, () -> moForm(null))), BorderLayout.NORTH);
        JPanel loc = khung(new BorderLayout(8, 0)); txtTim.setBorder(BorderFactory.createTitledBorder("Tìm mã lệnh, báo giá, biển số"));
        txtTim.addActionListener(e -> locDanhSach()); loc.add(txtTim);
        JPanel nut = nen(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        cboTrangThai.addActionListener(e -> locDanhSach()); nut.add(cboTrangThai);
        nut.add(nut("Tìm", false, this::locDanhSach));
        nut.add(nut("Làm mới", false, () -> { txtTim.setText(""); cboTrangThai.setSelectedIndex(0); locDanhSach(); }));
        loc.add(nut, BorderLayout.EAST); dau.add(loc); pnl.add(dau, BorderLayout.NORTH);
        JPanel ds = khung(new BorderLayout(0, 12)); ds.add(nhan("Danh sách lệnh sửa chữa", true), BorderLayout.NORTH);
        ds.add(cuonBang(tblDanhSach));
        JPanel tacVu = nen(new FlowLayout(FlowLayout.RIGHT, 0, 0)); tacVu.add(nut("Xem chi tiết", false, this::xemChiTiet));
        ds.add(tacVu, BorderLayout.SOUTH); pnl.add(ds); pnl.add(lblDanhSach, BorderLayout.SOUTH); return pnl;
    }

    private JPanel taoForm() {
        JPanel pnl = nen(new BorderLayout(0, 16));
        pnl.add(tieuDe("Tạo lệnh & phân công kỹ thuật", "1. Báo giá hợp lệ → 2. Phân công → 3. Phát hành lệnh",
                nut("Danh sách / quay lại", false, () -> { if (choPhepRoiManHinh()) moDanhSach(); })), BorderLayout.NORTH);
        JPanel form = khung(new BorderLayout(0, 14)); JPanel dau = cot();
        them(dau, nhan("Tạo lệnh sửa chữa từ báo giá đã được duyệt", true), 24);
        them(dau, nhan("Phân công theo hạng mục khách xác nhận; không tự thêm dòng ngoài báo giá.", false), 24);
        them(dau, truong("Báo giá được duyệt *", cboBaoGia), 68);
        them(dau, new JScrollPane(txtCanCu), 85); form.add(dau, BorderLayout.NORTH);
        JPanel giua = nen(new BorderLayout(0, 10)); giua.add(nhan("Danh sách hạng mục được phép sửa chữa", true), BorderLayout.NORTH);
        JScrollPane bang = cuonBang(tblHangMuc); bang.setPreferredSize(new Dimension(0, 220)); giua.add(bang); form.add(giua);
        JPanel cuoi = cot();
        them(cuoi, hang(truong("Kỹ thuật viên chính *", cboChinh), truong("Người hỗ trợ (nếu có)", cboHoTro)), 68);
        txtMa.setEditable(false);
        them(cuoi, hang(truong("Mã lệnh · tự sinh", txtMa), truong("Hạn dự kiến * · dd/MM/yyyy HH:mm", txtHan)), 68);
        them(cuoi, truong("Ghi chú phân công", txtGhiChu), 95);
        JTextArea quyTac = vanBan(2, false); quyTac.setText("Phát sinh cần báo giá bổ sung và khách đồng ý trước khi thực hiện. Tiếp tục phần đã được duyệt.");
        quyTac.setBackground(new Color(0xFFF7E5)); quyTac.setForeground(new Color(0x91631A));
        quyTac.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12)); them(cuoi, quyTac, 58);
        form.add(cuoi, BorderLayout.SOUTH); cuonForm = cuonNoiDung(form); pnl.add(cuonForm);
        JPanel chan = nen(new BorderLayout(0, 8)); chan.add(lblLoi, BorderLayout.NORTH);
        JPanel tacVu = nen(new FlowLayout(FlowLayout.RIGHT, 0, 0)); tacVu.add(btnPhatHanh); chan.add(tacVu);
        pnl.add(chan, BorderLayout.SOUTH); return pnl;
    }

    private void moForm(PhieuSuaChua phieu) {
        kiemTraPhien(); dangNap = true; chiDoc = phieu != null; maLuot = UUID.randomUUID().toString();
        modelHangMuc.setRowCount(0); cboBaoGia.removeAllItems();
        if (chiDoc) cboBaoGia.addItem(dao.layBaoGia(phien.get(), phieu.getMaPhieu()));
        else for (BaoGia bg : dao.layBaoGiaCoThePhatHanh(phien.get())) cboBaoGia.addItem(bg);
        cboBaoGia.setEnabled(!chiDoc); baoGiaChon = (BaoGia) cboBaoGia.getSelectedItem();
        cboChinh.removeAllItems(); cboHoTro.removeAllItems(); cboChinh.addItem(null); cboHoTro.addItem(null);
        for (KyThuatVienPhanCong kt : dao.layKyThuatPhanCong(phien.get())) { cboChinh.addItem(kt); cboHoTro.addItem(kt); }
        cboChinh.setEnabled(!chiDoc); cboHoTro.setEnabled(!chiDoc); txtHan.setEditable(!chiDoc); txtGhiChu.setEditable(!chiDoc);
        txtMa.setText(chiDoc ? phieu.getMaPhieu() : "Tự sinh khi phát hành");
        txtHan.setText((chiDoc ? phieu.getHanHoanThanh() : baoGiaChon == null ? LocalDateTime.now().plusDays(1) : baoGiaChon.getNgayHen()).format(NGAY));
        txtGhiChu.setText(chiDoc ? phieu.getGhiChu() : "");
        capNhatCanCu();
        if (baoGiaChon != null) {
            List<ChiTietBaoGia> chiTiet = baoGiaChon.getChiTiet();
            for (int i = 0; i < chiTiet.size(); i++) {
                ChiTietBaoGia ct = chiTiet.get(i); Object nguoi = null; String trangThai = "Chưa bắt đầu";
                if (chiDoc) {
                    HangMucSuaChua hm = phieu.getDanhSachHangMuc().get(i);
                    nguoi = tenKyThuat(hm.getTaiKhoanThucHien()); trangThai = hm.getTrangThai().getTenHienThi();
                }
                modelHangMuc.addRow(new Object[] {ct.getTenHangMuc(), ct.getSoLuong().toPlainString() + " " + ct.getDonVi(), nguoi, trangThai});
            }
        }
        if (chiDoc) {
            chonNguoiLichSu(cboChinh, phieu.getKyThuatChinh()); chonNguoiLichSu(cboHoTro, phieu.getKyThuatPhoiHop());
            txtCanCu.append("\nPhát hành: " + phieu.getNguoiPhatHanh() + " · " + phieu.getThoiDiemPhatHanh().format(NGAY));
        }
        btnPhatHanh.setVisible(!chiDoc);
        lblLoi.setForeground(UiTheme.APP_MUTED);
        lblLoi.setText(chiDoc ? "Lệnh đã phát hành · chỉ đọc" : baoGiaChon == null ? "Chưa có báo giá đủ điều kiện phát hành." : "Chọn người chính/hỗ trợ rồi phân công từng dòng.");
        dangNap = false; daThayDoi = false; capNhatEditor(); boCuc.show(pnlThe, "FORM");
        SwingUtilities.invokeLater(() -> cuonForm.getViewport().setViewPosition(new Point(0, 0)));
    }

    private void capNhatCanCu() {
        if (baoGiaChon == null) { txtCanCu.setText("Chưa có báo giá đã đồng ý và chưa lập lệnh."); return; }
        PhieuTiepNhan phieu = baoGiaChon.getBaoCao().getPhieu();
        txtCanCu.setText("Phiếu tiếp nhận: " + phieu.getMaPhieu() + "\nBáo giá: " + baoGiaChon.getMaBaoGia()
                + " · V" + baoGiaChon.getPhienBan() + " · Đã đồng ý\nKhách / xe: " + phieu.getTenKhach() + " · " + baoGiaChon.getBienSo());
        txtCanCu.setCaretPosition(0);
    }

    private void doiBaoGia() {
        BaoGia moi = (BaoGia) cboBaoGia.getSelectedItem(); if (moi == baoGiaChon) return;
        ketThucSuaO();
        if (daThayDoi && JOptionPane.showConfirmDialog(this, "Đổi báo giá sẽ bỏ phân công đang nhập. Tiếp tục?",
                "Đổi báo giá", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            dangNap = true; cboBaoGia.setSelectedItem(baoGiaChon); dangNap = false; return;
        }
        dangNap = true; baoGiaChon = moi; modelHangMuc.setRowCount(0); capNhatCanCu();
        if (moi != null) {
            for (ChiTietBaoGia ct : moi.getChiTiet()) modelHangMuc.addRow(new Object[] {
                    ct.getTenHangMuc(), ct.getSoLuong().toPlainString() + " " + ct.getDonVi(), null, "Chưa bắt đầu"});
            txtHan.setText(moi.getNgayHen().format(NGAY));
        }
        dangNap = false; daThayDoi = true;
    }

    private void doiNhanSu() {
        ketThucSuaO(); capNhatEditor(); daThayDoi = true;
        for (int i = 0; i < modelHangMuc.getRowCount(); i++) {
            Object value = modelHangMuc.getValueAt(i, 2);
            if (value != cboChinh.getSelectedItem() && value != cboHoTro.getSelectedItem()) modelHangMuc.setValueAt(null, i, 2);
        }
    }

    private void capNhatEditor() {
        if (chiDoc) return;
        cboPhuTrach.removeAllItems(); cboPhuTrach.addItem(null);
        if (cboChinh.getSelectedItem() != null) cboPhuTrach.addItem((KyThuatVienPhanCong) cboChinh.getSelectedItem());
        if (cboHoTro.getSelectedItem() != null && cboHoTro.getSelectedItem() != cboChinh.getSelectedItem()) cboPhuTrach.addItem((KyThuatVienPhanCong) cboHoTro.getSelectedItem());
    }

    private void phatHanh() {
        kiemTraPhien(); if (chiDoc) return;
        ketThucSuaO(); btnPhatHanh.setEnabled(false);
        try {
            LocalDateTime han = LocalDateTime.parse(txtHan.getText().strip(), NGAY);
            List<String> phuTrach = new ArrayList<>();
            for (int i = 0; i < modelHangMuc.getRowCount(); i++) phuTrach.add(taiKhoan(modelHangMuc.getValueAt(i, 2)));
            PhieuSuaChua moi = dao.phatHanh(phien.get(), maLuot, baoGiaChon, taiKhoan(cboChinh.getSelectedItem()),
                    taiKhoan(cboHoTro.getSelectedItem()), phuTrach, han, txtGhiChu.getText());
            moDanhSach(); lblDanhSach.setText("Đã phát hành " + moi.getMaPhieu() + " · " + moi.getMaBaoGia());
        } catch (DateTimeParseException e) { lblLoi.setForeground(UiTheme.ERROR); lblLoi.setText("Hạn dự kiến không hợp lệ; dùng dd/MM/yyyy HH:mm."); txtHan.requestFocusInWindow(); }
        catch (IllegalArgumentException | IllegalStateException e) { lblLoi.setForeground(UiTheme.ERROR); lblLoi.setText(e.getMessage()); lblLoi.setToolTipText(e.getMessage()); }
        finally { btnPhatHanh.setEnabled(true); }
    }

    private void xemChiTiet() {
        kiemTraPhien(); int row = tblDanhSach.getSelectedRow();
        if (row < 0) { lblDanhSach.setText("Chọn lệnh để xem chi tiết."); return; }
        String ma = (String) modelDanhSach.getValueAt(tblDanhSach.convertRowIndexToModel(row), 0);
        PhieuSuaChua phieu = dao.timTheoMa(phien.get(), ma).orElseThrow(() -> new IllegalArgumentException("Lệnh không còn tồn tại."));
        moForm(phieu);
    }

    private void locDanhSach() {
        kiemTraPhien(); modelDanhSach.setRowCount(0);
        for (PhieuSuaChua p : dao.timKiem(phien.get(), txtTim.getText(), (String) cboTrangThai.getSelectedItem())) {
            modelDanhSach.addRow(new Object[] {p.getMaPhieu(), p.getBienSo(), tenKyThuat(p.getKyThuatChinh()), p.getHanHoanThanh().format(NGAY), p.getTrangThai().getTenHienThi()});
        }
        lblDanhSach.setText(modelDanhSach.getRowCount() + " lệnh · lưu trong bộ nhớ của phiên.");
    }

    private String tenKyThuat(String taiKhoan) {
        for (KyThuatVienPhanCong kt : dao.layKyThuatPhanCong(phien.get())) if (kt.getTaiKhoan().getTenDangNhap().equals(taiKhoan)) return kt.toString();
        return taiKhoan;
    }
    private void chonNguoiLichSu(JComboBox<Object> cbo, String ten) {
        for (int i = 0; i < cbo.getItemCount(); i++) if (cbo.getItemAt(i) != null && taiKhoan(cbo.getItemAt(i)).equals(ten)) { cbo.setSelectedIndex(i); return; }
        if (!ten.isEmpty()) { cbo.addItem(ten + " · không còn trong nguồn hoạt động"); cbo.setSelectedIndex(cbo.getItemCount() - 1); }
    }
    private static String taiKhoan(Object nguoi) { return nguoi instanceof KyThuatVienPhanCong kt ? kt.getTaiKhoan().getTenDangNhap() : ""; }
    private void ketThucSuaO() { if (tblHangMuc.isEditing()) tblHangMuc.getCellEditor().stopCellEditing(); }
    private void kiemTraPhien() { if (phien.get() == null || phien.get().getVaiTro() != VaiTro.NHAN_VIEN_TIEP_NHAN) throw new SecurityException("Chức năng dành cho tiếp nhận."); }
    private void theoDoi(javax.swing.text.JTextComponent txt) {
        txt.getDocument().addDocumentListener(new DocumentListener() {
            private void doi() { if (!dangNap && !chiDoc) daThayDoi = true; }
            @Override public void insertUpdate(DocumentEvent e) { doi(); }
            @Override public void removeUpdate(DocumentEvent e) { doi(); }
            @Override public void changedUpdate(DocumentEvent e) { doi(); }
        });
    }

    private static JPanel nen(LayoutManager layout) { JPanel pnl = new JPanel(layout); pnl.setOpaque(false); return pnl; }
    private static JPanel khung(LayoutManager layout) { JPanel pnl = UiTheme.roundedPanel(layout, Color.WHITE, 8); pnl.setBorder(UiTheme.roundedBorder(UiTheme.APP_BORDER, 8, 16, 16)); return pnl; }
    private static JLabel nhan(String text, boolean dam) { JLabel lbl = new JLabel(text); lbl.setFont(dam ? UiTheme.APP_BOLD_FONT : UiTheme.APP_FONT); lbl.setForeground(dam ? UiTheme.APP_TEXT : UiTheme.APP_MUTED); return lbl; }
    private static JTextArea vanBan(int rows, boolean editable) { JTextArea txt = new JTextArea(rows, 20); txt.setEditable(editable); txt.setLineWrap(true); txt.setWrapStyleWord(true); txt.setFont(UiTheme.APP_FONT); return txt; }
    private static JButton nut(String text, boolean chinh, Runnable action) { JButton btn = UiTheme.createAppButton(text, chinh); btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 18, 36)); btn.addActionListener(e -> action.run()); return btn; }
    private static JPanel cot() { JPanel pnl = nen(null); pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS)); return pnl; }
    private static void them(JPanel cot, JComponent component, int cao) { component.setAlignmentX(Component.LEFT_ALIGNMENT); component.setMaximumSize(new Dimension(Integer.MAX_VALUE, cao)); component.setPreferredSize(new Dimension(0, cao)); cot.add(component); cot.add(Box.createVerticalStrut(10)); }
    private static JPanel hang(JComponent trai, JComponent phai) { JPanel pnl = nen(new GridLayout(1, 2, 14, 0)); pnl.add(trai); pnl.add(phai); return pnl; }
    private static JPanel truong(String text, JComponent c) { JPanel pnl = nen(new BorderLayout(0, 6)); JLabel lbl = nhan(text, true); lbl.setLabelFor(c); pnl.add(lbl, BorderLayout.NORTH); c.setFont(UiTheme.APP_FONT); pnl.add(c instanceof JTextArea ? new JScrollPane(c) : c); return pnl; }
    private static JPanel tieuDe(String text, String note, JButton btn) { JPanel pnl = nen(new BorderLayout(8, 10)); pnl.add(nhan("Tiếp nhận / Nghiệp vụ / " + text, false), BorderLayout.NORTH); JPanel title = nen(new BorderLayout(0, 6)); JLabel lbl = nhan(text, true); lbl.setFont(UiTheme.APP_BOLD_FONT.deriveFont(24f)); title.add(lbl, BorderLayout.NORTH); title.add(nhan(note, false)); pnl.add(title); JPanel action = nen(new FlowLayout(FlowLayout.RIGHT, 0, 0)); action.add(btn); pnl.add(action, BorderLayout.EAST); return pnl; }
    private static DefaultTableModel chiDoc(String... columns) { return new DefaultTableModel(columns, 0) { private static final long serialVersionUID = 1L; @Override public boolean isCellEditable(int row, int column) { return false; } }; }
    private static JTable bang(DefaultTableModel model) { JTable tbl = new JTable(model); tbl.setFont(UiTheme.APP_FONT); tbl.setRowHeight(44); tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); tbl.setAutoCreateRowSorter(true); tbl.getTableHeader().setFont(UiTheme.APP_BOLD_FONT); tbl.getTableHeader().setReorderingAllowed(false); return tbl; }
    private static JScrollPane cuonBang(JTable tbl) { JScrollPane cuon = new JScrollPane(tbl); cuon.getViewport().setBackground(Color.WHITE); return cuon; }
    private static final class NoiDungCuon extends JPanel implements Scrollable {
        private static final long serialVersionUID = 1L;
        NoiDungCuon() { super(new BorderLayout()); setOpaque(false); }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 20; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return Math.max(20, r.height - 20); }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }
    private static JScrollPane cuonNoiDung(JPanel pnl) { JPanel neo = new NoiDungCuon(); neo.add(pnl, BorderLayout.NORTH); JScrollPane cuon = new JScrollPane(neo); cuon.setBorder(null); cuon.getVerticalScrollBar().setUnitIncrement(20); return cuon; }
}
