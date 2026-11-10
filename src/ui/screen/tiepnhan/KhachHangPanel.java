package ui.screen.tiepnhan;

import dao.KhachHangDAO;
import dao.XeDAO;
import entity.KhachHang;
import entity.TaiKhoan;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import ui.component.KiemTraRoiTrang;
import ui.component.UiTheme;

/** Danh sách và form hồ sơ dùng chung một kho trong phiên tiếp nhận. */
public final class KhachHangPanel extends JPanel implements KiemTraRoiTrang {
    private static final long serialVersionUID = 1L;
    private final KhachHangDAO dao;

    @Override
    public boolean choPhepRoiTrang() {
        return choPhepRoiManHinh();
    }
    private final XeDAO demSoXe;
    private final TaiKhoan phien;
    private final CardLayout boCuc = new CardLayout();
    private final JPanel pnlThe = new JPanel(boCuc);
    private final JTextField txtTimKiem = new JTextField(24);
    private final DefaultTableModel model = new DefaultTableModel(
            new String[] { "Mã KH", "Họ và tên", "Điện thoại", "Địa chỉ", "Số xe", "Thao tác" }, 0) {
        private static final long serialVersionUID = 1L;

        /** Cho biết cell editable. */
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tblKhachHang = new JTable(model);
    private final JLabel lblKetQua = nhan(" ", 12, false);
    private final JLabel lblThongBao = nhan("Dữ liệu được lưu trên SQL Server", 12, false);
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

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public KhachHangPanel(KhachHangDAO dao, TaiKhoan phien) {
        this(dao, phien, new XeDAO(dao));
    }

    /** Đếm từ kho xe chung; không lưu số xe trùng lặp vào hồ sơ khách hàng. */
    public KhachHangPanel(KhachHangDAO dao, TaiKhoan phien, XeDAO demSoXe) {
        super(new BorderLayout());
        if (demSoXe == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.demSoXe = demSoXe;
        if (dao == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.dao = dao;
        if (phien == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.phien = phien;
        setOpaque(false);
        pnlThe.setOpaque(false);
        pnlThe.add(taoDanhSach(), "DANH_SACH");
        pnlThe.add(taoForm(), "FORM");
        add(pnlThe);
        taiDanhSach();
    }

    /** Hỏi trước khi rời form; hủy thao tác phải giữ nguyên dữ liệu đang nhập. */
    public boolean choPhepRoiManHinh() {
        return !daThayDoi || JOptionPane.showConfirmDialog(this, "Hồ sơ chưa lưu. Bạn muốn bỏ thay đổi?",
                "Thay đổi chưa lưu", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** Chỉ gọi sau khi người dùng đồng ý rời màn hình hoặc vừa lưu thành công. */
    public void moDanhSach() {
        daThayDoi = false;
        boCuc.show(pnlThe, "DANH_SACH");
        taiDanhSach();
    }

    /** Tạo danh sach cho giao diện. */
    private JPanel taoDanhSach() {
        JPanel pnl = new JPanel(new BorderLayout(0, 16));
        pnl.setOpaque(false);
        JPanel dau = new JPanel(new BorderLayout(0, 16));
        dau.setOpaque(false);
        JButton them = nut("+ Thêm khách hàng", true, new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                moForm(null);
            }
        });
        dau.add(tieuDe("Khách hàng", "Tra cứu trước bằng điện thoại để tránh tạo trùng hồ sơ.", them),
                BorderLayout.NORTH);
        JPanel congCu = khung(new BorderLayout(8, 0));
        txtTimKiem.setFont(UiTheme.APP_FONT);
        txtTimKiem.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(UiTheme.APP_BORDER),
                "Tên / mã khách / SĐT"));
        txtTimKiem.setToolTipText("Tìm tên, mã khách hoặc số điện thoại; Enter để tìm.");
        txtTimKiem.getAccessibleContext().setAccessibleName("Tìm khách hàng");
        txtTimKiem.addActionListener(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent e) {
                taiDanhSach();
            }
        });
        congCu.add(txtTimKiem);
        JPanel nutLoc = new JPanel(new BorderLayout());
        nutLoc.setOpaque(false);
        JPanel thanhNutLoc = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        thanhNutLoc.setOpaque(false);
        JComboBox<String> trangThai = new JComboBox<>(new String[] { "Tất cả trạng thái" });
        trangThai.setEnabled(false);
        trangThai.setToolTipText("Hồ sơ chưa có trạng thái nghiệp vụ được quy định.");
        thanhNutLoc.add(caoBangOtim(trangThai));
        thanhNutLoc.add(caoBangOtim(nut("Tìm kiếm", false, new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                KhachHangPanel.this.taiDanhSach();
            }
        })));
        thanhNutLoc.add(caoBangOtim(nut("Làm mới", false, new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {

                txtTimKiem.setText("");
                taiDanhSach();

            }
        })));
        nutLoc.add(thanhNutLoc, BorderLayout.SOUTH);
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
        tblKhachHang.getTableHeader().setResizingAllowed(false);
        tblKhachHang.getColumnModel().getColumn(1).setPreferredWidth(180);
        tblKhachHang.getColumnModel().getColumn(5).setPreferredWidth(110);
        tblKhachHang.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            /** Trả về table cell renderer component. */
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus,
                    int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, selected, focus, row, column);
                if (!selected) {
                    c.setForeground(UiTheme.APP_PRIMARY);
                }
                return c;
            }
        });
        tblKhachHang.addMouseListener(new MouseAdapter() {
            /** Mở use clicked khi người dùng yêu cầu. */
            @Override
            public void mouseClicked(MouseEvent e) {
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

            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent e) {
                moDaChon();
            }
        });
        JScrollPane cuon = new JScrollPane(tblKhachHang);
        cuon.getViewport().setBackground(Color.WHITE);
        cuon.setBorder(BorderFactory.createLineBorder(UiTheme.APP_BORDER));
        bang.add(cuon);
        JPanel chanBang = new JPanel(new BorderLayout(8, 8));
        chanBang.setOpaque(false);
        chanBang.add(lblKetQua);
        chanBang.add(nut("Xem chi tiết", false, new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                KhachHangPanel.this.moDaChon();
            }
        }), BorderLayout.EAST);
        bang.add(chanBang, BorderLayout.SOUTH);
        pnl.add(bang);
        pnl.add(lblThongBao, BorderLayout.SOUTH);
        return pnl;
    }

    /** Tạo form cho giao diện. */
    private JPanel taoForm() {
        JPanel pnl = new JPanel(new BorderLayout(0, 16));
        pnl.setOpaque(false);
        pnl.add(tieuDe("Thông tin khách hàng", "Một khách hàng có thể sở hữu nhiều phương tiện.",
                nut("Danh sách / quay lại", false, new ActionListener() {
                    /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
                    @Override
                    public void actionPerformed(ActionEvent event) {

                        if (choPhepRoiManHinh()) {
                            moDanhSach();
                        }

                    }
                })), BorderLayout.NORTH);
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
        phanDuoi.add(nhan("Tra cứu SĐT trước; đối chiếu hồ sơ hiện có khi phát hiện trùng.", 12, false),
                BorderLayout.NORTH);
        lblLoi.setForeground(UiTheme.ERROR);
        phanDuoi.add(lblLoi);
        btnLuu.setPreferredSize(new Dimension(240, 38));
        btnLuu.addActionListener(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent e) {
                luuHoSo();
            }
        });
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

    /** Thực hiện them truong cho thành phần hiện tại. */
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
            /** Thực hiện insert update cho thành phần hiện tại. */
            @Override
            public void insertUpdate(DocumentEvent e) {
                thayDoi();
            }

            /** Thực hiện remove update cho thành phần hiện tại. */
            @Override
            public void removeUpdate(DocumentEvent e) {
                thayDoi();
            }

            /** Thực hiện changed update cho thành phần hiện tại. */
            @Override
            public void changedUpdate(DocumentEvent e) {
                thayDoi();
            }

            /** Thực hiện thay doi cho thành phần hiện tại. */
            private void thayDoi() {
                if (!dangNhapForm) {
                    daThayDoi = true;
                }
            }
        });
        o.add(txt);
        pnl.add(o);
    }

    /** Đọc lại dữ liệu và cập nhật phần hiển thị. */
    private void taiDanhSach() {
        try {
            List<KhachHang> ketQua = dao.timKiem(phien, txtTimKiem.getText());
            model.setRowCount(0);
            for (KhachHang kh : ketQua) {
                int soXe = demSoXe.demTheoKhach(phien, kh.getMaKH());
                model.addRow(new Object[] { kh.getMaKH(), kh.getHoTen(), kh.getSoDienThoai(), kh.getDiaChi(),
                        soXe < 0 ? "—" : String.valueOf(soXe), "Xem chi tiết" });
            }
            lblKetQua.setText(
                    ketQua.isEmpty() ? "Không tìm thấy khách hàng phù hợp." : ketQua.size() + " hồ sơ đang hiển thị");
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            lblThongBao.setText(e.getMessage());
        }
    }

    /** Mở da chon khi người dùng yêu cầu. */
    private void moDaChon() {
        int dong = tblKhachHang.getSelectedRow();
        if (dong < 0) {
            lblThongBao.setText("Chọn khách hàng để xem chi tiết.");
            return;
        }
        // JTable trả chỉ số view; phải đổi sang model để mở đúng khách sau khi sort.
        int modelRow = tblKhachHang.convertRowIndexToModel(dong);
        try {
            moForm(dao.timTheoMa(phien, (String) model.getValueAt(modelRow, 0)));
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            lblThongBao.setText(e.getMessage());
        }
    }

    /** Mở form khi người dùng yêu cầu. */
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

    /** Kiểm tra và lưu ho so. */
    private void luuHoSo() {
        if (!btnLuu.isEnabled()) {
            return;
        }
        btnLuu.setEnabled(false);
        try {
            // Kiểm tra trước khi hỏi đối chiếu, không ghi hồ sơ dở khi người dùng hủy.
            KhachHang duLieu = new KhachHang(maDangSua == null ? "NHAP" : maDangSua, txtHoTen.getText(),
                    txtDienThoai.getText(), txtDiaChi.getText(), txtEmail.getText(), txtGhiChu.getText());
            List<KhachHang> trung = dao.timTrungSoDienThoai(phien, duLieu.getSoDienThoai(), maDangSua);
            if (!trung.isEmpty()) {
                Object[] luaChon = { "Xem hồ sơ hiện có", "Vẫn lưu hồ sơ này", "Quay lại" };
                int chon = JOptionPane.showOptionDialog(this,
                        "SĐT đã có trong " + trung.size() + " hồ sơ. Đối chiếu trước khi lưu.", "Trùng số điện thoại",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, luaChon, luaChon[0]);
                if (chon == 0) {
                    if (choPhepRoiManHinh()) {
                        String[] hoSo = new String[trung.size()];
                        for (int i = 0; i < trung.size(); i++) {
                            hoSo[i] = trung.get(i).getMaKH() + " · " + trung.get(i).getHoTen();
                        }
                        Object daChon = JOptionPane.showInputDialog(this, "Chọn hồ sơ cần đối chiếu", "Hồ sơ cùng SĐT",
                                JOptionPane.QUESTION_MESSAGE, null, hoSo, hoSo[0]);
                        if (daChon != null) {
                            for (int i = 0; i < hoSo.length; i++) {
                                if (hoSo[i].equals(daChon)) {
                                    moForm(trung.get(i));
                                }
                            }
                        }
                    }

                    return;
                }
                if (chon != 1) {
                    return;
                }
            }
            KhachHang daLuu = dao.luu(phien, maDangSua, duLieu.getHoTen(), duLieu.getSoDienThoai(), duLieu.getDiaChi(),
                    duLieu.getEmail(), duLieu.getGhiChu());
            maDangSua = daLuu.getMaKH();
            moDanhSach();
            lblThongBao.setText("Đã lưu " + daLuu.getMaKH() + " trên SQL Server.");
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            lblLoi.setText(e.getMessage());
        } finally {
            btnLuu.setEnabled(true);
        }
    }

    /** Thực hiện tieu de cho thành phần hiện tại. */
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
        nut.setOpaque(false);
        nut.add(thaoTac);
        pnl.add(nut, BorderLayout.EAST);
        return pnl;
    }

    /** Đồng bộ chiều cao điều khiển lọc với ô tìm kiếm có nhãn viền. */
    private static <T extends Component> T caoBangOtim(T control) {
        Dimension kichThuoc = control.getPreferredSize();
        control.setPreferredSize(new Dimension(kichThuoc.width, 36));
        return control;
    }

    /** Thực hiện nut cho thành phần hiện tại. */
    private JButton nut(String ten, boolean chinh, ActionListener thaoTac) {
        JButton btn = UiTheme.createAppButton(ten, chinh);
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 36));
        btn.addActionListener(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent e) {
                thaoTac.actionPerformed(new ActionEvent(thaoTac, ActionEvent.ACTION_PERFORMED, "thaoTac"));
            }
        });
        return btn;
    }

    /** Thực hiện khung cho thành phần hiện tại. */
    private JPanel khung(LayoutManager layout) {
        JPanel pnl = UiTheme.roundedPanel(layout, Color.WHITE, 8);
        pnl.setBorder(UiTheme.roundedBorder(UiTheme.APP_BORDER, 8, 16, 16));
        return pnl;
    }

    /** Thực hiện nhan cho thành phần hiện tại. */
    private static JLabel nhan(String chu, int co, boolean dam) {
        JLabel lbl = new JLabel(chu);
        lbl.setFont((dam ? UiTheme.APP_BOLD_FONT : UiTheme.APP_FONT).deriveFont((float) co));
        lbl.setForeground(dam ? UiTheme.APP_TEXT : UiTheme.APP_MUTED);
        return lbl;
    }
}
