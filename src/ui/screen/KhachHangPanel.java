package ui.screen;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import dao.KhachHangDAO;
import entity.KhachHang;
import entity.TaiKhoan;
import ui.component.UiTheme;

/** Danh sách và form hồ sơ dùng chung một kho trong phiên tiếp nhận. */
public final class KhachHangPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final KhachHangDAO dao;
    private final ToIntFunction<String> demSoXe;
    private final Supplier<TaiKhoan> phien;
    private final CardLayout boCuc = new CardLayout();
    private final JPanel pnlThe = new JPanel(boCuc);
    private final JTextField txtTimKiem = new JTextField(24);
    private final DefaultTableModel model = new DefaultTableModel(
            new String[] {"Mã KH", "Họ và tên", "Điện thoại", "Địa chỉ", "Số xe", "Thao tác"}, 0) {
        private static final long serialVersionUID = 1L;
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tblKhachHang = new JTable(model);
    private final JLabel lblKetQua = nhan(" ", 12, false);
    private final JLabel lblThongBao = nhan("Dữ liệu trong bộ nhớ · chưa lưu lâu dài", 12, false);
    private final JTextField txtMa = new JTextField();
    private final JTextField txtHoTen = new JTextField();
    private final JTextField txtDienThoai = new JTextField();
    private final JTextField txtDiaChi = new JTextField();
    private final JTextField txtEmail = new JTextField();
    private final JTextField txtGhiChu = new JTextField();
    private final JLabel lblLoi = nhan(" ", 13, false);
    private final JButton btnLuu = UiTheme.createAppButton("Lưu hồ sơ khách hàng", true);
    private String maDangSua;
    private boolean dangNhapForm;
    private boolean daThayDoi;

    /** @param phien nguồn phiên hiện tại, từ chối thao tác sau đăng xuất */
    public KhachHangPanel(KhachHangDAO dao, Supplier<TaiKhoan> phien) {
        this(dao, phien, ma -> -1);
    }

    /** Đếm từ kho xe chung; không lưu số xe trùng lặp vào hồ sơ khách hàng. */
    public KhachHangPanel(KhachHangDAO dao, Supplier<TaiKhoan> phien, ToIntFunction<String> demSoXe) {
        super(new BorderLayout());
        this.demSoXe = Objects.requireNonNull(demSoXe);
        this.dao = Objects.requireNonNull(dao);
        this.phien = Objects.requireNonNull(phien);
        setOpaque(false);
        pnlThe.setOpaque(false);
        pnlThe.add(taoDanhSach(), "DANH_SACH");
        pnlThe.add(taoForm(), "FORM");
        add(pnlThe);
        taiDanhSach();
    }

    /** Hỏi trước khi rời form; hủy thao tác phải giữ nguyên dữ liệu đang nhập. */
    public boolean choPhepRoiManHinh() {
        return !daThayDoi || JOptionPane.showConfirmDialog(this,
                "Hồ sơ chưa lưu. Bạn muốn bỏ thay đổi?", "Thay đổi chưa lưu",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** Chỉ gọi sau khi người dùng đồng ý rời màn hình hoặc vừa lưu thành công. */
    public void moDanhSach() {
        daThayDoi = false;
        boCuc.show(pnlThe, "DANH_SACH");
        taiDanhSach();
    }

    private JPanel taoDanhSach() {
        JPanel pnl = new JPanel(new BorderLayout(0, 16));
        pnl.setOpaque(false);
        JPanel dau = new JPanel(new BorderLayout(0, 16));
        dau.setOpaque(false);
        JButton them = nut("+ Thêm khách hàng", true, () -> moForm(null));
        dau.add(tieuDe("Khách hàng", "Tra cứu trước bằng điện thoại để tránh tạo trùng hồ sơ.", them), BorderLayout.NORTH);
        JPanel congCu = khung(new BorderLayout(8, 0));
        txtTimKiem.setFont(UiTheme.APP_FONT);
        txtTimKiem.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UiTheme.APP_BORDER), "Tên / mã khách / SĐT"));
        txtTimKiem.setToolTipText("Tìm tên, mã khách hoặc số điện thoại; Enter để tìm.");
        txtTimKiem.getAccessibleContext().setAccessibleName("Tìm khách hàng");
        txtTimKiem.addActionListener(e -> taiDanhSach());
        congCu.add(txtTimKiem);
        JPanel nutLoc = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        nutLoc.setOpaque(false);
        JComboBox<String> trangThai = new JComboBox<>(new String[] {"Tất cả trạng thái"});
        trangThai.setEnabled(false);
        trangThai.setToolTipText("Hồ sơ chưa có trạng thái nghiệp vụ được quy định.");
        nutLoc.add(trangThai);
        nutLoc.add(nut("Tìm kiếm", false, this::taiDanhSach));
        nutLoc.add(nut("Làm mới", false, () -> { txtTimKiem.setText(""); taiDanhSach(); }));
        congCu.add(nutLoc, BorderLayout.EAST);
        dau.add(congCu, BorderLayout.CENTER);
        pnl.add(dau, BorderLayout.NORTH);
        JPanel bang = khung(new BorderLayout(0, 12));
        bang.add(nhan("Danh sách khách hàng", 16, true), BorderLayout.NORTH);
        tblKhachHang.setFont(UiTheme.APP_FONT);
        tblKhachHang.setRowHeight(48);
        tblKhachHang.setAutoCreateRowSorter(true);
        tblKhachHang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblKhachHang.setShowVerticalLines(false);
        tblKhachHang.setGridColor(UiTheme.APP_BORDER);
        tblKhachHang.getTableHeader().setFont(UiTheme.APP_BOLD_FONT);
        tblKhachHang.getTableHeader().setBackground(UiTheme.APP_BACKGROUND);
        tblKhachHang.getTableHeader().setReorderingAllowed(false);
        tblKhachHang.getColumnModel().getColumn(1).setPreferredWidth(180);
        tblKhachHang.getColumnModel().getColumn(5).setPreferredWidth(110);
        tblKhachHang.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean selected, boolean focus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, selected, focus, row, column);
                if (!selected) c.setForeground(UiTheme.APP_PRIMARY);
                return c;
            }
        });
        tblKhachHang.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = tblKhachHang.rowAtPoint(e.getPoint());
                if (row >= 0 && (e.getClickCount() == 2 || tblKhachHang.columnAtPoint(e.getPoint()) == 5)) {
                    tblKhachHang.setRowSelectionInterval(row, row);
                    moDaChon();
                }
            }
        });
        tblKhachHang.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "CHI_TIET");
        tblKhachHang.getActionMap().put("CHI_TIET", new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override public void actionPerformed(ActionEvent e) { moDaChon(); }
        });
        JScrollPane cuon = new JScrollPane(tblKhachHang);
        cuon.getViewport().setBackground(Color.WHITE);
        cuon.setBorder(BorderFactory.createLineBorder(UiTheme.APP_BORDER));
        bang.add(cuon);
        JPanel chanBang = new JPanel(new BorderLayout(8, 8));
        chanBang.setOpaque(false);
        chanBang.add(lblKetQua);
        chanBang.add(nut("Xem chi tiết", false, this::moDaChon), BorderLayout.EAST);
        bang.add(chanBang, BorderLayout.SOUTH);
        pnl.add(bang);
        pnl.add(lblThongBao, BorderLayout.SOUTH);
        return pnl;
    }

    private JPanel taoForm() {
        JPanel pnl = new JPanel(new BorderLayout(0, 16));
        pnl.setOpaque(false);
        pnl.add(tieuDe("Thông tin khách hàng", "Một khách hàng có thể sở hữu nhiều phương tiện.",
                nut("Danh sách / quay lại", false, () -> { if (choPhepRoiManHinh()) moDanhSach(); })), BorderLayout.NORTH);
        JPanel hoSo = khung(new BorderLayout(0, 16));
        hoSo.add(nhan("Hồ sơ khách hàng", 18, true), BorderLayout.NORTH);
        JPanel truong = new JPanel(new GridLayout(0, 2, 14, 18));
        truong.setOpaque(false);
        themTruong(truong, "Mã khách hàng · tự sinh · chỉ đọc", txtMa);
        themTruong(truong, "Họ và tên *", txtHoTen);
        themTruong(truong, "Số điện thoại * · 10 số, bắt đầu bằng 0", txtDienThoai);
        themTruong(truong, "Địa chỉ", txtDiaChi);
        themTruong(truong, "Email · tùy chọn", txtEmail);
        themTruong(truong, "Ghi chú", txtGhiChu);
        txtMa.setEditable(false);
        txtMa.setBackground(UiTheme.APP_BACKGROUND);
        JPanel noiDung = new JPanel(new BorderLayout(0, 18));
        noiDung.setOpaque(false);
        noiDung.add(truong, BorderLayout.NORTH);
        JPanel phanDuoi = new JPanel(new BorderLayout(0, 12));
        phanDuoi.setOpaque(false);
        phanDuoi.add(nhan("Tra cứu SĐT trước; đối chiếu hồ sơ hiện có khi phát hiện trùng.", 12, false), BorderLayout.NORTH);
        lblLoi.setForeground(UiTheme.ERROR);
        phanDuoi.add(lblLoi);
        btnLuu.setPreferredSize(new Dimension(240, 38));
        btnLuu.addActionListener(e -> luuHoSo());
        phanDuoi.add(btnLuu, BorderLayout.SOUTH);
        noiDung.add(phanDuoi, BorderLayout.CENTER);
        hoSo.add(noiDung);
        JPanel neoTren = new JPanel(new BorderLayout());
        neoTren.setOpaque(false);
        neoTren.add(hoSo, BorderLayout.NORTH);
        JScrollPane cuon = new JScrollPane(neoTren);
        cuon.setBorder(null);
        cuon.getViewport().setBackground(UiTheme.APP_BACKGROUND);
        cuon.getVerticalScrollBar().setUnitIncrement(20);
        pnl.add(cuon);
        return pnl;
    }

    private void themTruong(JPanel pnl, String ten, JTextField txt) {
        JPanel o = new JPanel(new BorderLayout(0, 6));
        o.setOpaque(false);
        JLabel lbl = nhan(ten, 12, true);
        lbl.setLabelFor(txt);
        o.add(lbl, BorderLayout.NORTH);
        txt.setFont(UiTheme.APP_FONT);
        txt.setPreferredSize(new Dimension(100, 40));
        txt.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.APP_BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        txt.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { thayDoi(); }
            @Override public void removeUpdate(DocumentEvent e) { thayDoi(); }
            @Override public void changedUpdate(DocumentEvent e) { thayDoi(); }
            private void thayDoi() { if (!dangNhapForm) daThayDoi = true; }
        });
        o.add(txt);
        pnl.add(o);
    }

    private void taiDanhSach() {
        try {
            List<KhachHang> ketQua = dao.timKiem(phien.get(), txtTimKiem.getText());
            model.setRowCount(0);
            for (KhachHang kh : ketQua) {
                int soXe = demSoXe.applyAsInt(kh.getMaKH());
                model.addRow(new Object[] {kh.getMaKH(), kh.getHoTen(), kh.getSoDienThoai(),
                        kh.getDiaChi(), soXe < 0 ? "—" : String.valueOf(soXe), "Xem chi tiết"});
            }
            lblKetQua.setText(ketQua.isEmpty() ? "Không tìm thấy khách hàng phù hợp." : ketQua.size() + " hồ sơ đang hiển thị");
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            lblThongBao.setText(e.getMessage());
        }
    }

    private void moDaChon() {
        int dong = tblKhachHang.getSelectedRow();
        if (dong < 0) { lblThongBao.setText("Chọn khách hàng để xem chi tiết."); return; }
        // JTable trả chỉ số view; phải đổi sang model để mở đúng khách sau khi sort.
        int modelRow = tblKhachHang.convertRowIndexToModel(dong);
        try { moForm(dao.timTheoMa(phien.get(), (String) model.getValueAt(modelRow, 0))); }
        catch (IllegalArgumentException | IllegalStateException | SecurityException e) { lblThongBao.setText(e.getMessage()); }
    }

    private void moForm(KhachHang kh) {
        dangNhapForm = true;
        maDangSua = kh == null ? null : kh.getMaKH();
        txtMa.setText(kh == null ? "Cấp khi lưu thành công" : kh.getMaKH());
        txtHoTen.setText(kh == null ? "" : kh.getHoTen());
        txtDienThoai.setText(kh == null ? "" : kh.getSoDienThoai());
        txtDiaChi.setText(kh == null ? "" : kh.getDiaChi());
        txtEmail.setText(kh == null ? "" : kh.getEmail());
        txtGhiChu.setText(kh == null ? "" : kh.getGhiChu());
        lblLoi.setText(" ");
        daThayDoi = false;
        dangNhapForm = false;
        boCuc.show(pnlThe, "FORM");
        txtHoTen.requestFocusInWindow();
    }

    private void luuHoSo() {
        if (!btnLuu.isEnabled()) return;
        btnLuu.setEnabled(false);
        try {
            // Kiểm tra trước khi hỏi đối chiếu, không ghi hồ sơ dở khi người dùng hủy.
            KhachHang duLieu = new KhachHang(maDangSua == null ? "NHAP" : maDangSua,
                    txtHoTen.getText(), txtDienThoai.getText(), txtDiaChi.getText(), txtEmail.getText(), txtGhiChu.getText());
            List<KhachHang> trung = dao.timTrungSoDienThoai(phien.get(), duLieu.getSoDienThoai(), maDangSua);
            if (!trung.isEmpty()) {
                Object[] luaChon = {"Xem hồ sơ hiện có", "Vẫn lưu hồ sơ này", "Quay lại"};
                int chon = JOptionPane.showOptionDialog(this, "SĐT đã có trong " + trung.size()
                        + " hồ sơ. Đối chiếu trước khi lưu.", "Trùng số điện thoại", JOptionPane.DEFAULT_OPTION,
                        JOptionPane.WARNING_MESSAGE, null, luaChon, luaChon[0]);
                if (chon == 0) {
                    if (choPhepRoiManHinh()) {
                        String[] hoSo = new String[trung.size()];
                        for (int i = 0; i < trung.size(); i++) hoSo[i] = trung.get(i).getMaKH() + " · " + trung.get(i).getHoTen();
                        Object daChon = JOptionPane.showInputDialog(this, "Chọn hồ sơ cần đối chiếu", "Hồ sơ cùng SĐT",
                                JOptionPane.QUESTION_MESSAGE, null, hoSo, hoSo[0]);
                        if (daChon != null) {
                            for (int i = 0; i < hoSo.length; i++) if (hoSo[i].equals(daChon)) moForm(trung.get(i));
                        }
                    }
                    return;
                }
                if (chon != 1) return;
            }
            KhachHang daLuu = dao.luu(phien.get(), maDangSua, duLieu.getHoTen(), duLieu.getSoDienThoai(),
                    duLieu.getDiaChi(), duLieu.getEmail(), duLieu.getGhiChu());
            maDangSua = daLuu.getMaKH();
            moDanhSach();
            lblThongBao.setText("Đã lưu " + daLuu.getMaKH() + " · Dữ liệu trong bộ nhớ, chưa lưu lâu dài.");
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            lblLoi.setText(e.getMessage());
        } finally { btnLuu.setEnabled(true); }
    }

    private JPanel tieuDe(String ten, String moTa, JButton thaoTac) {
        JPanel pnl = new JPanel(new BorderLayout(10, 12));
        pnl.setOpaque(false);
        pnl.add(nhan("Tiếp nhận / Danh mục / " + ten, 12, false), BorderLayout.NORTH);
        JPanel chu = new JPanel(new BorderLayout(0, 6));
        chu.setOpaque(false);
        chu.add(nhan(ten, 24, true), BorderLayout.NORTH);
        chu.add(nhan(moTa, 12, false));
        pnl.add(chu);
        JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        nut.setOpaque(false); nut.add(thaoTac); pnl.add(nut, BorderLayout.EAST);
        return pnl;
    }

    private JButton nut(String ten, boolean chinh, Runnable thaoTac) {
        JButton btn = UiTheme.createAppButton(ten, chinh);
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 36));
        btn.addActionListener(e -> thaoTac.run());
        return btn;
    }

    private JPanel khung(LayoutManager layout) {
        JPanel pnl = UiTheme.roundedPanel(layout, Color.WHITE, 8);
        pnl.setBorder(UiTheme.roundedBorder(UiTheme.APP_BORDER, 8, 16, 16));
        return pnl;
    }

    private static JLabel nhan(String chu, int co, boolean dam) {
        JLabel lbl = new JLabel(chu);
        lbl.setFont((dam ? UiTheme.APP_BOLD_FONT : UiTheme.APP_FONT).deriveFont((float) co));
        lbl.setForeground(dam ? UiTheme.APP_TEXT : UiTheme.APP_MUTED);
        return lbl;
    }
}
