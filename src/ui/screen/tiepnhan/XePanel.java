package ui.screen.tiepnhan;

import dao.TrangChuTiepNhanDAO;
import dao.KhachHangDAO;
import dao.XeDAO;
import dao.PhieuTiepNhanDAO;
import entity.KhachHang;
import entity.Xe;
import entity.TaiKhoan;
import dao.PhuTungSuDungDAO.PhuTungSuDung;
import entity.PhieuSuaChua.ChiTietSuaChua;
import entity.PhieuSuaChua;
import entity.PhieuTiepNhan;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
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
import javax.swing.table.DefaultTableModel;

import ui.component.UiTheme;

/** Danh sách, form và lối tra cứu lịch sử xe; chỉ thao tác kho trong phiên hiện tại. */
public final class XePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");
    private final XeDAO dao;
    private final PhieuTiepNhanDAO phieuDAO;
    private TrangChuTiepNhanDAO tienDoDAO;
    private final DefaultTableModel modelLichSuSuaChua = model("Ngày", "Lệnh sửa chữa", "Nội dung",
            "Phụ tùng thực dùng", "Kết quả");
    private final DefaultTableModel modelLichSuTiepNhan = model("Ngày nhận", "Phiếu tiếp nhận", "Yêu cầu khách",
            "Trạng thái");
    private final KhachHangDAO khachHangDAO;
    private final TaiKhoan phien;
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
    private List<KhachHang> cacChuXe = new ArrayList<>();
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
    public XePanel(XeDAO dao, KhachHangDAO khachHangDAO, TaiKhoan phien) {
        this(dao, khachHangDAO, phien, null);
    }

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public XePanel(XeDAO dao, KhachHangDAO khachHangDAO, TaiKhoan phien, PhieuTiepNhanDAO phieuDAO) {
        super(new BorderLayout());
        this.phieuDAO = phieuDAO;
        if (dao == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.dao = dao;
        if (khachHangDAO == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.khachHangDAO = khachHangDAO;
        if (phien == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.phien = phien;
        setOpaque(false);
        pnlThe.setOpaque(false);
        pnlThe.add(taoDanhSach(), "DANH_SACH");
        pnlThe.add(taoForm(), "FORM");
        pnlThe.add(taoLichSu(), "LICH_SU");
        add(pnlThe);
        taiDanhSach();
    }

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public XePanel(XeDAO dao, KhachHangDAO khachHangDAO, TaiKhoan phien, PhieuTiepNhanDAO phieuDAO,
            TrangChuTiepNhanDAO tienDoDAO) {
        this(dao, khachHangDAO, phien, phieuDAO);
        this.tienDoDAO = tienDoDAO;
    }

    /** Hủy chuyển màn hình phải giữ nguyên form và mục sidebar hiện tại. */
    public boolean choPhepRoiManHinh() {
        return !daThayDoi || JOptionPane.showConfirmDialog(this, "Hồ sơ xe chưa lưu. Bỏ thay đổi?", "Thay đổi chưa lưu",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** Nạp lại hồ sơ, chỉ gọi khi đã lưu hoặc đồng ý bỏ thay đổi. */
    public void moDanhSach() {
        daThayDoi = false;
        taiDanhSach();
        boCuc.show(pnlThe, "DANH_SACH");
    }

    /** Tạo danh sach cho giao diện. */
    private JPanel taoDanhSach() {
        JPanel pnl = nen(new BorderLayout(0, 16));
        JPanel dau = nen(new BorderLayout(0, 16));
        dau.add(tieuDe("Phương tiện", "Biển số phải duy nhất và gắn với hồ sơ khách hàng.",
                nut("+ Thêm phương tiện", true, new ActionListener() {
                    /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        moForm(null);
                    }
                })), BorderLayout.NORTH);
        JPanel congCu = khung(new BorderLayout(8, 0));
        txtTim.setBorder(BorderFactory.createTitledBorder("Biển số / tên chủ xe / mã khách"));
        txtTim.addActionListener(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent e) {
                taiDanhSach();
            }
        });
        congCu.add(txtTim);
        JPanel nutLoc = nen(new BorderLayout());
        JPanel thanhNutLoc = nen(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        thanhNutLoc.add(caoBangOtim(nut("Làm mới", false, new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {

                txtTim.setText("");
                taiDanhSach();

            }
        })));
        nutLoc.add(thanhNutLoc, BorderLayout.SOUTH);
        congCu.add(nutLoc, BorderLayout.EAST);
        dau.add(congCu);
        pnl.add(dau, BorderLayout.NORTH);
        JPanel noiDung = khung(new BorderLayout(0, 12));
        noiDung.add(nhan("Danh sách phương tiện", true), BorderLayout.NORTH);
        JScrollPane cuonBang = new JScrollPane(tblXe);
        cuonBang.getViewport().setBackground(Color.WHITE);
        noiDung.add(cuonBang);
        JPanel thaoTac = nen(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        thaoTac.add(nut("Tìm kiếm", false, new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                XePanel.this.taiDanhSach();
            }
        }));
        thaoTac.add(nut("Xem chi tiết", false, new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                moDaChon(false);
            }
        }));
        thaoTac.add(nut("Lịch sử sửa chữa / tiếp nhận", false, new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                moDaChon(true);
            }
        }));
        noiDung.add(thaoTac, BorderLayout.SOUTH);
        tblXe.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "CHI_TIET");
        tblXe.getActionMap().put("CHI_TIET", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent e) {
                moDaChon(false);
            }
        });
        tblXe.addMouseListener(new MouseAdapter() {
            /** Mở use clicked khi người dùng yêu cầu. */
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    moDaChon(false);
                }
            }
        });
        pnl.add(noiDung);
        pnl.add(lblKetQua, BorderLayout.SOUTH);
        return pnl;
    }

    /** Tạo form cho giao diện. */
    private JPanel taoForm() {
        JPanel pnl = nen(new BorderLayout(0, 16));
        pnl.add(tieuDe("Thông tin phương tiện", "Chọn đúng chủ xe trước khi thêm phương tiện.",
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
        cboChuXe.addActionListener(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!dangNap) {
                    daThayDoi = true;
                }
            }
        });
        hoSo.add(truong);
        JPanel chan = nen(new BorderLayout(0, 12));
        chan.add(nhan("Tra cứu biển số trước. Nếu xe đã có, chọn hồ sơ hiện có.", false), BorderLayout.NORTH);
        lblLoi.setForeground(UiTheme.ERROR);
        chan.add(lblLoi);
        btnLuu.setPreferredSize(new Dimension(240, 38));
        btnLuu.addActionListener(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent e) {
                luu();
            }
        });
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

    /** Tạo lich su cho giao diện. */
    private JPanel taoLichSu() {
        JPanel pnl = nen(new BorderLayout(0, 16));
        pnl.add(tieuDe("Lịch sử xe & sửa chữa", "Tra cứu theo biển số; dữ liệu liên kết theo phương tiện.",
                nut("Danh sách / quay lại", false, new ActionListener() {
                    /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        XePanel.this.moDanhSach();
                    }
                })), BorderLayout.NORTH);
        JPanel noiDung = nen(new GridLayout(3, 1, 0, 16));
        JPanel tra = khung(new BorderLayout(8, 12));
        tra.add(lblXeLichSu, BorderLayout.NORTH);
        txtTraLichSu.setBorder(BorderFactory.createTitledBorder("Biển số xe · nhập đầy đủ"));
        txtTraLichSu.addActionListener(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent e) {
                traLichSu();
            }
        });
        tra.add(txtTraLichSu);
        tra.add(nut("Tra cứu", false, new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                XePanel.this.traLichSu();
            }
        }), BorderLayout.EAST);
        tra.add(nhan("Chứng từ liên kết theo mã xe; chỉ hiển thị dữ liệu đã có trong nguồn của phiên.", false),
                BorderLayout.SOUTH);
        noiDung.add(tra);
        noiDung.add(khungLichSu("Lịch sử sửa chữa · UC008", modelLichSuSuaChua));
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

    /** Thực hiện khung lich su cho thành phần hiện tại. */
    private JPanel khungLichSu(String ten, DefaultTableModel duLieu) {
        JPanel pnl = khung(new BorderLayout(0, 10));
        pnl.add(nhan(ten, true), BorderLayout.NORTH);
        pnl.add(new JScrollPane(bang(duLieu)));
        return pnl;
    }

    /** Đọc lại dữ liệu và cập nhật phần hiển thị. */
    private void taiDanhSach() {
        try {
            List<Xe> ketQua = dao.timKiem(phien, txtTim.getText());
            model.setRowCount(0);
            for (Xe xe : ketQua) {
                model.addRow(new Object[] { xe.getMaXe(), xe.getBienSo(),
                        khachHangDAO.timTheoMa(phien, xe.getMaKH()).getHoTen(), xe.getHangXe() + " / " + xe.getModel(),
                        xe.getNamSX(), xe.getMauXe() });
            }
            lblKetQua.setText(ketQua.size() + " xe · Dữ liệu từ SQL Server.");
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            lblKetQua.setText(e.getMessage());
        }
    }

    /** Mở da chon khi người dùng yêu cầu. */
    private void moDaChon(boolean lichSu) {
        int dong = tblXe.getSelectedRow();
        if (dong < 0) {
            lblKetQua.setText("Chọn phương tiện trước khi mở chi tiết/lịch sử.");
            return;
        }
        try {
            int viTri = tblXe.convertRowIndexToModel(dong);
            Xe xe = dao.timTheoMa(phien, (String) model.getValueAt(viTri, 0));
            if (lichSu) {
                txtTraLichSu.setText(xe.getBienSo());
                traLichSu();
                boCuc.show(pnlThe, "LICH_SU");
            } else {
                moForm(xe);
            }
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            lblKetQua.setText(e.getMessage());
        }
    }

    /** Mở tra cứu từ phiếu tiếp nhận, không tự coi các dòng mẫu là lịch sử thật. */
    public void moLichSuTheoBienSo(String bienSo) {
        txtTraLichSu.setText(bienSo);
        traLichSu();
        boCuc.show(pnlThe, "LICH_SU");
    }

    /** Thực hiện tra lich su cho thành phần hiện tại. */
    private void traLichSu() {
        lblXeLichSu.setText("Không tìm thấy phương tiện.");
        modelLichSuTiepNhan.setRowCount(0);
        modelLichSuSuaChua.setRowCount(0);
        try {
            String khoa = Xe.chuanHoaBienSo(txtTraLichSu.getText());
            for (Xe xe : dao.timKiem(phien, txtTraLichSu.getText())) {
                if (Xe.chuanHoaBienSo(xe.getBienSo()).equals(khoa)) {
                    if (phieuDAO != null) {
                        for (PhieuTiepNhan phieu : phieuDAO.lichSuTheoXe(phien, xe.getMaXe())) {
                            modelLichSuTiepNhan
                                    .addRow(new Object[] { phieu.getThoiGianNhan().format(NGAY), phieu.getMaPhieu(),
                                            phieu.getYeuCau(), tienDoDAO == null ? phieu.getTrangThai().getTenHienThi()
                                                    : tienDoDAO.trangThaiPhieu(phien, phieu.getMaPhieu()) });
                        }
                    }
                    if (tienDoDAO != null) {
                        for (PhieuSuaChua lenh : tienDoDAO.layLenhTheoXe(phien, xe.getMaXe())) {
                            StringBuilder phuTung = new StringBuilder();
                            for (PhuTungSuDung dong : tienDoDAO.layThucDung(phien, lenh.getMaPhieu())) {
                                if (phuTung.length() > 0) {
                                    phuTung.append("; ");
                                }
                                phuTung.append(dong.getPhuTung().getTenPhuTung()).append(": ")
                                        .append(String.valueOf(dong.getSoLuong())).append(" ")
                                        .append(dong.getPhuTung().getDonViTinh());
                            }
                            StringBuilder congViec = new StringBuilder();
                            for (ChiTietSuaChua hm : lenh.getDanhSachHangMuc()) {
                                if (congViec.length() > 0) {
                                    congViec.append("; ");
                                }
                                congViec.append(hm.getTenHangMuc());
                            }
                            modelLichSuSuaChua.addRow(new Object[] {
                                    (lenh.getThoiDiemHoanTat() == null ? lenh.getThoiDiemPhatHanh()
                                            : lenh.getThoiDiemHoanTat()).format(NGAY),
                                    lenh.getMaPhieu(), congViec.toString(), phuTung.toString(),
                                    lenh.getThoiDiemHoanTat() == null ? lenh.getTrangThai().getTenHienThi()
                                            : lenh.getKetQuaHoanTat()[0] });
                        }
                    }
                    lblXeLichSu
                            .setText(xe.getBienSo() + " · " + khachHangDAO.timTheoMa(phien, xe.getMaKH()).getHoTen());
                    return;

                }
            }
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            lblXeLichSu.setText(e.getMessage());
        }
    }

    /** Mở form khi người dùng yêu cầu. */
    private void moForm(Xe xe) {
        dangNap = true;
        try {
            cacChuXe = khachHangDAO.timKiem(phien, "");
            cboChuXe.removeAllItems();
            for (KhachHang kh : cacChuXe) {
                cboChuXe.addItem(kh.getMaKH() + " · " + kh.getHoTen());
            }
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
            if (xe != null) {
                for (int i = 0; i < cacChuXe.size(); i++) {
                    if (cacChuXe.get(i).getMaKH().equals(xe.getMaKH())) {
                        cboChuXe.setSelectedIndex(i);
                    }
                }
            }
            cboChuXe.setEnabled(xe == null);
            lblLoi.setText(cacChuXe.isEmpty() ? "Cần tạo hồ sơ khách hàng trước khi thêm xe." : " ");
            daThayDoi = false;
            boCuc.show(pnlThe, "FORM");
        } finally {
            dangNap = false;
        }
    }

    /** Kiểm tra và lưu . */
    private void luu() {
        if (!btnLuu.isEnabled()) {
            return;
        }
        btnLuu.setEnabled(false);
        try {
            int chu = cboChuXe.getSelectedIndex();
            if (chu < 0) {
                throw new IllegalArgumentException("Chọn chủ xe trước khi lưu.");
            }
            int nam;
            try {
                nam = Integer.parseInt(txtNam.getText().trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Năm sản xuất phải là số nguyên hợp lệ.");
            }
            Xe xe = dao.luu(phien, maDangSua, txtBienSo.getText(), cacChuXe.get(chu).getMaKH(), txtHang.getText(),
                    txtDong.getText(), nam, txtMau.getText(), txtSoKhung.getText(), txtSoMay.getText());
            maDangSua = xe.getMaXe();
            moDanhSach();
            lblKetQua.setText("Đã lưu " + xe.getMaXe() + " trên SQL Server.");
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            lblLoi.setText(e.getMessage());
        } finally {
            btnLuu.setEnabled(true);
        }
    }

    /** Thực hiện them truong cho thành phần hiện tại. */
    private void themTruong(JPanel pnl, String ten, JComponent truong) {
        JPanel o = nen(new BorderLayout(0, 6));
        JLabel lbl = nhan(ten, true);
        lbl.setLabelFor(truong);
        o.add(lbl, BorderLayout.NORTH);
        truong.setFont(UiTheme.APP_FONT);
        truong.setPreferredSize(new Dimension(100, 40));
        if (truong instanceof JTextField) {
            ((JTextField) truong).setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UiTheme.APP_BORDER), BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            ((JTextField) truong).getDocument().addDocumentListener(new DocumentListener() {
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
                    if (!dangNap) {
                        daThayDoi = true;
                    }
                }
            });
        }
        o.add(truong);
        pnl.add(o);
    }

    /** Thực hiện tieu de cho thành phần hiện tại. */
    private JPanel tieuDe(String ten, String moTa, JButton thaoTac) {
        JPanel pnl = nen(new BorderLayout(8, 10));
        pnl.add(nhan("Tiếp nhận / " + ten, false), BorderLayout.NORTH);
        JPanel chu = nen(new BorderLayout(0, 5));
        JLabel lbl = nhan(ten, true);
        lbl.setFont(UiTheme.APP_BOLD_FONT.deriveFont(24f));
        chu.add(lbl, BorderLayout.NORTH);
        chu.add(nhan(moTa, false));
        pnl.add(chu);
        JPanel nut = nen(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        nut.add(thaoTac);
        pnl.add(nut, BorderLayout.EAST);
        return pnl;
    }

    /** Mở del khi người dùng yêu cầu. */
    private static DefaultTableModel model(String... cot) {
        return new DefaultTableModel(cot, 0) {
            private static final long serialVersionUID = 1L;

            /** Cho biết cell editable. */
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }

            /** Trả về column class. */
            @Override
            public Class<?> getColumnClass(int col) {
                return getRowCount() > 0 && getValueAt(0, col) instanceof Integer ? Integer.class : String.class;
            }
        };
    }

    /** Thực hiện bang cho thành phần hiện tại. */
    private static JTable bang(DefaultTableModel model) {
        JTable tbl = new JTable(model) {
            private static final long serialVersionUID = 1L;

            /** Trả về tool tip text. */
            @Override
            public String getToolTipText(MouseEvent e) {
                int row = rowAtPoint(e.getPoint()), col = columnAtPoint(e.getPoint());
                Object value = row < 0 || col < 0 ? null : getValueAt(row, col);
                return value == null ? null : value.toString();
            }
        };
        tbl.setToolTipText("");
        tbl.setRowHeight(44);
        tbl.setFont(UiTheme.APP_FONT);
        tbl.setAutoCreateRowSorter(true);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl.setShowVerticalLines(false);
        tbl.setGridColor(UiTheme.APP_BORDER);
        tbl.getTableHeader().setFont(UiTheme.APP_BOLD_FONT);
        tbl.getTableHeader().setReorderingAllowed(false);
        tbl.getTableHeader().setResizingAllowed(false);
        return tbl;
    }

    /** Thực hiện nen cho thành phần hiện tại. */
    private static JPanel nen(LayoutManager layout) {
        JPanel pnl = new JPanel(layout);
        pnl.setOpaque(false);
        return pnl;
    }

    /** Thực hiện khung cho thành phần hiện tại. */
    private static JPanel khung(LayoutManager layout) {
        JPanel pnl = UiTheme.roundedPanel(layout, Color.WHITE, 8);
        pnl.setBorder(UiTheme.roundedBorder(UiTheme.APP_BORDER, 8, 16, 16));
        return pnl;
    }

    /** Thực hiện nhan cho thành phần hiện tại. */
    private static JLabel nhan(String chu, boolean dam) {
        JLabel lbl = new JLabel(chu);
        lbl.setFont(dam ? UiTheme.APP_BOLD_FONT : UiTheme.APP_FONT);
        lbl.setForeground(dam ? UiTheme.APP_TEXT : UiTheme.APP_MUTED);
        return lbl;
    }

    /** Đồng bộ chiều cao nút lọc với ô tìm kiếm có nhãn viền. */
    private static <T extends JComponent> T caoBangOtim(T control) {
        Dimension kichThuoc = control.getPreferredSize();
        control.setPreferredSize(new Dimension(kichThuoc.width, 36));
        return control;
    }

    /** Thực hiện nut cho thành phần hiện tại. */
    private static JButton nut(String ten, boolean chinh, ActionListener thaoTac) {
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
}
