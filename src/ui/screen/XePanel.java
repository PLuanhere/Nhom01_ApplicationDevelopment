package ui.screen;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import dao.KhachHangDAO;
import dao.XeDAO;
import dao.PhieuTiepNhanDAO;
import entity.PhieuTiepNhan;
import entity.KhachHang;
import entity.TaiKhoan;
import entity.Xe;
import ui.component.UiTheme;

/** Danh sách, form và lối tra cứu lịch sử xe; chỉ thao tác kho trong phiên hiện tại. */
public final class XePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final XeDAO dao;
    private final PhieuTiepNhanDAO phieuDAO;
    private final DefaultTableModel modelLichSuTiepNhan = model("Ngày nhận", "Phiếu tiếp nhận", "Yêu cầu khách", "Trạng thái");
    private final KhachHangDAO khachHangDAO;
    private final Supplier<TaiKhoan> phien;
    private final CardLayout boCuc = new CardLayout();
    private final JPanel pnlThe = new JPanel(boCuc);
    private final JTextField txtTim = new JTextField();
    private final DefaultTableModel model = model("Mã xe", "Biển số", "Khách hàng", "Hãng / dòng", "Năm", "Màu");
    private final JTable tblXe = bang(model);
    private final JLabel lblKetQua = nhan(" ", false);
    private final JLabel lblLoi = nhan(" ", false);
    private final JTextField txtMa = new JTextField();
    private final JTextField txtBienSo = new JTextField();
    private final JComboBox<String> cboChuXe = new JComboBox<>();
    private List<KhachHang> cacChuXe = List.of();
    private final JTextField txtHang = new JTextField();
    private final JTextField txtDong = new JTextField();
    private final JTextField txtNam = new JTextField();
    private final JTextField txtMau = new JTextField();
    private final JTextField txtSoKhung = new JTextField();
    private final JTextField txtSoMay = new JTextField();
    private final JButton btnLuu = UiTheme.createAppButton("Lưu phương tiện", true);
    private final JTextField txtTraLichSu = new JTextField();
    private final JLabel lblXeLichSu = nhan("Chưa chọn phương tiện.", true);
    private String maDangSua;
    private boolean dangNap;
    private boolean daThayDoi;

    /** Gọi trên EDT; hai DAO phải cùng nguồn khách hàng của phiên đăng nhập. */
    public XePanel(XeDAO dao, KhachHangDAO khachHangDAO, Supplier<TaiKhoan> phien) {
        this(dao, khachHangDAO, phien, null);
    }

    public XePanel(XeDAO dao, KhachHangDAO khachHangDAO, Supplier<TaiKhoan> phien, PhieuTiepNhanDAO phieuDAO) {
        super(new BorderLayout());
        this.phieuDAO = phieuDAO;
        this.dao = Objects.requireNonNull(dao);
        this.khachHangDAO = Objects.requireNonNull(khachHangDAO);
        this.phien = Objects.requireNonNull(phien);
        setOpaque(false);
        pnlThe.setOpaque(false);
        pnlThe.add(taoDanhSach(), "DANH_SACH");
        pnlThe.add(taoForm(), "FORM");
        pnlThe.add(taoLichSu(), "LICH_SU");
        add(pnlThe);
        taiDanhSach();
    }

    /** Hủy chuyển màn hình phải giữ nguyên form và mục sidebar hiện tại. */
    public boolean choPhepRoiManHinh() {
        return !daThayDoi || JOptionPane.showConfirmDialog(this, "Hồ sơ xe chưa lưu. Bỏ thay đổi?",
                "Thay đổi chưa lưu", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** Nạp lại hồ sơ, chỉ gọi khi đã lưu hoặc đồng ý bỏ thay đổi. */
    public void moDanhSach() {
        daThayDoi = false;
        taiDanhSach();
        boCuc.show(pnlThe, "DANH_SACH");
    }

    private JPanel taoDanhSach() {
        JPanel pnl = nen(new BorderLayout(0, 16));
        JPanel dau = nen(new BorderLayout(0, 16));
        dau.add(tieuDe("Phương tiện", "Biển số phải duy nhất và gắn với hồ sơ khách hàng.",
                nut("+ Thêm phương tiện", true, () -> moForm(null))), BorderLayout.NORTH);
        JPanel congCu = khung(new BorderLayout(8, 0));
        txtTim.setBorder(BorderFactory.createTitledBorder("Biển số / tên chủ xe / mã khách"));
        txtTim.addActionListener(e -> taiDanhSach());
        congCu.add(txtTim);
        congCu.add(nut("Làm mới", false, () -> { txtTim.setText(""); taiDanhSach(); }), BorderLayout.EAST);
        dau.add(congCu);
        pnl.add(dau, BorderLayout.NORTH);
        JPanel noiDung = khung(new BorderLayout(0, 12));
        noiDung.add(nhan("Danh sách phương tiện", true), BorderLayout.NORTH);
        JScrollPane cuonBang = new JScrollPane(tblXe);
        cuonBang.getViewport().setBackground(Color.WHITE);
        noiDung.add(cuonBang);
        JPanel thaoTac = nen(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        thaoTac.add(nut("Tìm kiếm", false, this::taiDanhSach));
        thaoTac.add(nut("Xem chi tiết", false, () -> moDaChon(false)));
        thaoTac.add(nut("Lịch sử sửa chữa / tiếp nhận", false, () -> moDaChon(true)));
        noiDung.add(thaoTac, BorderLayout.SOUTH);
        tblXe.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "CHI_TIET");
        tblXe.getActionMap().put("CHI_TIET", new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override public void actionPerformed(ActionEvent e) { moDaChon(false); }
        });
        tblXe.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) moDaChon(false); }
        });
        pnl.add(noiDung);
        pnl.add(lblKetQua, BorderLayout.SOUTH);
        return pnl;
    }

    private JPanel taoForm() {
        JPanel pnl = nen(new BorderLayout(0, 16));
        pnl.add(tieuDe("Thông tin phương tiện", "Chọn đúng chủ xe trước khi thêm phương tiện.",
                nut("Danh sách / quay lại", false, () -> { if (choPhepRoiManHinh()) moDanhSach(); })), BorderLayout.NORTH);
        JPanel hoSo = khung(new BorderLayout(0, 16));
        hoSo.add(nhan("Thông tin phương tiện", true), BorderLayout.NORTH);
        JPanel truong = nen(new GridLayout(0, 2, 14, 16));
        themTruong(truong, "Mã xe · tự sinh · chỉ đọc", txtMa);
        themTruong(truong, "Biển số *", txtBienSo);
        themTruong(truong, "Chủ xe * · không đổi khi sửa", cboChuXe);
        themTruong(truong, "Hãng xe *", txtHang);
        themTruong(truong, "Dòng xe *", txtDong);
        themTruong(truong, "Năm sản xuất *", txtNam);
        themTruong(truong, "Màu sơn", txtMau);
        themTruong(truong, "Số khung · tùy chọn", txtSoKhung);
        themTruong(truong, "Số máy · tùy chọn", txtSoMay);
        txtMa.setEditable(false);
        txtMa.setBackground(UiTheme.APP_BACKGROUND);
        cboChuXe.addActionListener(e -> { if (!dangNap) daThayDoi = true; });
        hoSo.add(truong);
        JPanel chan = nen(new BorderLayout(0, 12));
        chan.add(nhan("Tra cứu biển số trước. Nếu xe đã có, chọn hồ sơ hiện có.", false), BorderLayout.NORTH);
        lblLoi.setForeground(UiTheme.ERROR);
        chan.add(lblLoi);
        btnLuu.setPreferredSize(new Dimension(240, 38));
        btnLuu.addActionListener(e -> luu());
        chan.add(btnLuu, BorderLayout.SOUTH);
        hoSo.add(chan, BorderLayout.SOUTH);
        JPanel neo = nen(new BorderLayout());
        neo.add(hoSo, BorderLayout.NORTH);
        JScrollPane cuon = new JScrollPane(neo);
        cuon.setBorder(null);
        cuon.getVerticalScrollBar().setUnitIncrement(20);
        pnl.add(cuon);
        return pnl;
    }

    private JPanel taoLichSu() {
        JPanel pnl = nen(new BorderLayout(0, 16));
        pnl.add(tieuDe("Lịch sử xe & sửa chữa", "Tra cứu theo biển số; dữ liệu liên kết theo phương tiện.",
                nut("Danh sách / quay lại", false, this::moDanhSach)), BorderLayout.NORTH);
        JPanel noiDung = nen(new GridLayout(3, 1, 0, 16));
        JPanel tra = khung(new BorderLayout(8, 12));
        tra.add(lblXeLichSu, BorderLayout.NORTH);
        txtTraLichSu.setBorder(BorderFactory.createTitledBorder("Biển số xe · nhập đầy đủ"));
        txtTraLichSu.addActionListener(e -> traLichSu());
        tra.add(txtTraLichSu);
        tra.add(nut("Tra cứu", false, this::traLichSu), BorderLayout.EAST);
        tra.add(nhan("Sửa chữa chưa nối nguồn; lịch sử tiếp nhận lấy từ kho trong phiên khi có.", false), BorderLayout.SOUTH);
        noiDung.add(tra);
        noiDung.add(khungLichSu("Lịch sử sửa chữa · UC008", "Ngày", "Lệnh sửa chữa", "Nội dung", "Phụ tùng", "Kết quả"));
        JPanel tiepNhan = khung(new BorderLayout(0, 10));
        tiepNhan.add(nhan("Lịch sử tiếp nhận · UC012", true), BorderLayout.NORTH);
        tiepNhan.add(new JScrollPane(bang(modelLichSuTiepNhan)));
        noiDung.add(tiepNhan);
        // Các khung lịch sử cần chiều cao tối thiểu; cuộn thay vì ép mất ô tra cứu.
        noiDung.setPreferredSize(new Dimension(600, 620));
        JPanel neo = nen(new BorderLayout());
        neo.add(noiDung, BorderLayout.NORTH);
        JScrollPane cuon = new JScrollPane(neo);
        cuon.setBorder(null);
        cuon.getVerticalScrollBar().setUnitIncrement(20);
        pnl.add(cuon);
        return pnl;
    }

    private JPanel khungLichSu(String ten, String... cot) {
        JPanel pnl = khung(new BorderLayout(0, 10));
        pnl.add(nhan(ten, true), BorderLayout.NORTH);
        pnl.add(new JScrollPane(bang(model(cot))));
        return pnl;
    }

    private void taiDanhSach() {
        try {
            List<Xe> ketQua = dao.timKiem(phien.get(), txtTim.getText());
            model.setRowCount(0);
            for (Xe xe : ketQua) model.addRow(new Object[] {xe.getMaXe(), xe.getBienSo(),
                    khachHangDAO.timTheoMa(phien.get(), xe.getMaKH()).getHoTen(), xe.getHangXe() + " / " + xe.getModel(), xe.getNamSX(), xe.getMauXe()});
            lblKetQua.setText(ketQua.size() + " xe · Dữ liệu trong bộ nhớ, chưa lưu lâu dài.");
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) { lblKetQua.setText(e.getMessage()); }
    }

    private void moDaChon(boolean lichSu) {
        int dong = tblXe.getSelectedRow();
        if (dong < 0) { lblKetQua.setText("Chọn phương tiện trước khi mở chi tiết/lịch sử."); return; }
        try {
            int viTri = tblXe.convertRowIndexToModel(dong);
            Xe xe = dao.timTheoMa(phien.get(), (String) model.getValueAt(viTri, 0));
            if (lichSu) {
                txtTraLichSu.setText(xe.getBienSo());
                traLichSu();
                boCuc.show(pnlThe, "LICH_SU");
            } else moForm(xe);
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) { lblKetQua.setText(e.getMessage()); }
    }

    /** Mở tra cứu từ phiếu tiếp nhận, không tự coi các dòng mẫu là lịch sử thật. */
    public void moLichSuTheoBienSo(String bienSo) {
        txtTraLichSu.setText(bienSo);
        traLichSu();
        boCuc.show(pnlThe, "LICH_SU");
    }

    private void traLichSu() {
        lblXeLichSu.setText("Không tìm thấy phương tiện.");
        modelLichSuTiepNhan.setRowCount(0);
        try {
            String khoa = Xe.chuanHoaBienSo(txtTraLichSu.getText());
            for (Xe xe : dao.timKiem(phien.get(), txtTraLichSu.getText())) {
                if (Xe.chuanHoaBienSo(xe.getBienSo()).equals(khoa)) {
                    if (phieuDAO != null) for (PhieuTiepNhan phieu : phieuDAO.lichSuTheoXe(phien.get(), xe.getMaXe())) {
                        modelLichSuTiepNhan.addRow(new Object[] {phieu.getThoiGianNhan().toString(), phieu.getMaPhieu(),
                                phieu.getYeuCau(), phieu.getTrangThai().getTenHienThi()});
                    }
                    lblXeLichSu.setText(xe.getBienSo() + " · " + khachHangDAO.timTheoMa(phien.get(), xe.getMaKH()).getHoTen());
                    return;
                }
            }
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) { lblXeLichSu.setText(e.getMessage()); }
    }

    private void moForm(Xe xe) {
        dangNap = true;
        try {
            cacChuXe = khachHangDAO.timKiem(phien.get(), "");
            cboChuXe.removeAllItems();
            for (KhachHang kh : cacChuXe) cboChuXe.addItem(kh.getMaKH() + " · " + kh.getHoTen());
            cboChuXe.setSelectedIndex(-1);
            maDangSua = xe == null ? null : xe.getMaXe();
            txtMa.setText(xe == null ? "Cấp khi lưu thành công" : xe.getMaXe());
            txtBienSo.setText(xe == null ? "" : xe.getBienSo());
            txtHang.setText(xe == null ? "" : xe.getHangXe());
            txtDong.setText(xe == null ? "" : xe.getModel());
            txtNam.setText(xe == null ? "" : String.valueOf(xe.getNamSX()));
            txtMau.setText(xe == null ? "" : xe.getMauXe());
            txtSoKhung.setText(xe == null ? "" : xe.getSoKhung());
            txtSoMay.setText(xe == null ? "" : xe.getSoMay());
            if (xe != null) for (int i = 0; i < cacChuXe.size(); i++) {
                if (cacChuXe.get(i).getMaKH().equals(xe.getMaKH())) cboChuXe.setSelectedIndex(i);
            }
            cboChuXe.setEnabled(xe == null);
            lblLoi.setText(cacChuXe.isEmpty() ? "Cần tạo hồ sơ khách hàng trước khi thêm xe." : " ");
            daThayDoi = false;
            boCuc.show(pnlThe, "FORM");
        } finally { dangNap = false; }
    }

    private void luu() {
        if (!btnLuu.isEnabled()) return;
        btnLuu.setEnabled(false);
        try {
            int chu = cboChuXe.getSelectedIndex();
            if (chu < 0) throw new IllegalArgumentException("Chọn chủ xe trước khi lưu.");
            int nam;
            try { nam = Integer.parseInt(txtNam.getText().strip()); }
            catch (NumberFormatException e) { throw new IllegalArgumentException("Năm sản xuất phải là số nguyên hợp lệ."); }
            Xe xe = dao.luu(phien.get(), maDangSua, txtBienSo.getText(), cacChuXe.get(chu).getMaKH(),
                    txtHang.getText(), txtDong.getText(), nam, txtMau.getText(), txtSoKhung.getText(), txtSoMay.getText());
            maDangSua = xe.getMaXe();
            moDanhSach();
            lblKetQua.setText("Đã lưu " + xe.getMaXe() + " · Dữ liệu trong bộ nhớ, chưa lưu lâu dài.");
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) { lblLoi.setText(e.getMessage()); }
        finally { btnLuu.setEnabled(true); }
    }

    private void themTruong(JPanel pnl, String ten, JComponent truong) {
        JPanel o = nen(new BorderLayout(0, 6));
        JLabel lbl = nhan(ten, true);
        lbl.setLabelFor(truong);
        o.add(lbl, BorderLayout.NORTH);
        truong.setFont(UiTheme.APP_FONT);
        truong.setPreferredSize(new Dimension(100, 40));
        if (truong instanceof JTextField txt) {
            txt.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.APP_BORDER), BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            txt.getDocument().addDocumentListener(new DocumentListener() {
                @Override public void insertUpdate(DocumentEvent e) { thayDoi(); }
                @Override public void removeUpdate(DocumentEvent e) { thayDoi(); }
                @Override public void changedUpdate(DocumentEvent e) { thayDoi(); }
                private void thayDoi() { if (!dangNap) daThayDoi = true; }
            });
        }
        o.add(truong);
        pnl.add(o);
    }

    private JPanel tieuDe(String ten, String moTa, JButton thaoTac) {
        JPanel pnl = nen(new BorderLayout(8, 10));
        pnl.add(nhan("Tiếp nhận / " + ten, false), BorderLayout.NORTH);
        JPanel chu = nen(new BorderLayout(0, 5));
        JLabel lbl = nhan(ten, true);
        lbl.setFont(UiTheme.APP_BOLD_FONT.deriveFont(24f));
        chu.add(lbl, BorderLayout.NORTH); chu.add(nhan(moTa, false));
        pnl.add(chu);
        JPanel nut = nen(new FlowLayout(FlowLayout.RIGHT, 0, 0)); nut.add(thaoTac);
        pnl.add(nut, BorderLayout.EAST);
        return pnl;
    }

    private static DefaultTableModel model(String... cot) {
        return new DefaultTableModel(cot, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int col) { return getRowCount() > 0 && getValueAt(0, col) instanceof Integer ? Integer.class : String.class; }
        };
    }
    private static JTable bang(DefaultTableModel model) {
        JTable tbl = new JTable(model);
        tbl.setRowHeight(44); tbl.setFont(UiTheme.APP_FONT); tbl.setAutoCreateRowSorter(true);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); tbl.setShowVerticalLines(false);
        tbl.setGridColor(UiTheme.APP_BORDER); tbl.getTableHeader().setFont(UiTheme.APP_BOLD_FONT);
        tbl.getTableHeader().setReorderingAllowed(false);
        return tbl;
    }
    private static JPanel nen(LayoutManager layout) { JPanel pnl = new JPanel(layout); pnl.setOpaque(false); return pnl; }
    private static JPanel khung(LayoutManager layout) {
        JPanel pnl = UiTheme.roundedPanel(layout, Color.WHITE, 8);
        pnl.setBorder(UiTheme.roundedBorder(UiTheme.APP_BORDER, 8, 16, 16)); return pnl;
    }
    private static JLabel nhan(String chu, boolean dam) {
        JLabel lbl = new JLabel(chu); lbl.setFont(dam ? UiTheme.APP_BOLD_FONT : UiTheme.APP_FONT);
        lbl.setForeground(dam ? UiTheme.APP_TEXT : UiTheme.APP_MUTED); return lbl;
    }
    private static JButton nut(String ten, boolean chinh, Runnable thaoTac) {
        JButton btn = UiTheme.createAppButton(ten, chinh);
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 36));
        btn.addActionListener(e -> thaoTac.run()); return btn;
    }
}
