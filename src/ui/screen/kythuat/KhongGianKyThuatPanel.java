package ui.screen.kythuat;

import dao.DuLieuGarageDAO;
import dao.KyThuatDAO;
import entity.PhieuTiepNhan.BaoCaoKiemTra;
import entity.TaiKhoan;
import entity.PhuTung;
import entity.PhieuSuaChua;
import entity.PhieuSuaChua.TrangThaiSuaChua;
import entity.PhieuTiepNhan;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import ui.component.AppHeaderPanel;
import ui.component.BieuTuongKyThuat;
import ui.component.GiaoDienKyThuat;
import ui.component.PhuTungUi;
import ui.component.UiChung;
import ui.dialog.kythuat.DoiChieuKyThuatDialog;

// Ghép layout chung và điều hướng các màn hình trong phiên kỹ thuật.
public final class KhongGianKyThuatPanel extends JPanel implements ActionListener, DocumentListener {
    private static final long serialVersionUID = 1L;
    static final String[] CAC_TRANG = {"TONG_QUAN", "PHAN_CONG", "KIEM_TRA", "TIEN_DO",
            "PHU_TUNG", "PHAT_SINH", "HOAN_TAT"};
    static final String[] CAC_NHAN = {"Công việc kỹ thuật", "Công việc được phân công",
            "Báo cáo kiểm tra", "Tiến độ sửa chữa", "Phụ tùng sử dụng",
            "Báo hư hỏng phát sinh", "Kiểm tra hoàn tất sửa chữa"};
    final JFrame winChu;
    final TaiKhoan taiKhoan;
    final ActionListener dangXuat;
    final KyThuatDAO dao;
    final JPanel pnlNoiDung = new JPanel(new BorderLayout());
    final List<JButton> nutSidebar = new ArrayList<>();
    final List<JTextField> truongDangNhap = new ArrayList<JTextField>();
    String trang = "TONG_QUAN";
    String maLenh;
    int viTriHangMuc;
    BaoCaoKiemTra baoCao;
    PhieuTiepNhan phieu;
    DefaultTableModel modelNhap;
    JTable tblNhap;
    JTextField txtLoi;
    JTextField txtDeXuat;
    JTextField txtKetLuan;
    JTextField txtKetQua;
    JTextField txtPhatSinh;
    JTextField txtPhuTungDuKien;
    JTextField txtMoTa;
    JTextField txtLuong;
    JTextField txtTon;
    JTextField txtTonSau;
    JComboBox<TrangThaiSuaChua> cmbTienDo;
    JComboBox<String> cmbHangMuc;
    JComboBox<PhuTung> cmbPhuTung;
    JButton btnHoanTat;
    String maLuot;
    String anhChup = "";
    boolean dangNap;
    final TrangChuKyThuatPanel pnlTongQuanCongViec = new TrangChuKyThuatPanel(this);
    final PhieuSuaChuaPanel pnlCongViecPhanCong = new PhieuSuaChuaPanel(this);
    final BaoCaoKiemTraPanel pnlKiemTraXe = new BaoCaoKiemTraPanel(this);
    final TienDoSuaChuaPanel pnlTienDoSuaChua = new TienDoSuaChuaPanel(this);
    final BaoHuHongPhatSinhPanel pnlBaoHuHongPhatSinh = new BaoHuHongPhatSinhPanel(this);
    final PhuTungSuDungPanel pnlGhiNhanPhuTung = new PhuTungSuDungPanel(this);
    final KiemTraHoanTatPanel pnlKiemTraHoanTat = new KiemTraHoanTatPanel(this);

    public KhongGianKyThuatPanel(JFrame winChu, TaiKhoan taiKhoan, ActionListener dangXuat) {
        this(winChu, taiKhoan, dangXuat, new KyThuatDAO(taiKhoan));
    }

    public KhongGianKyThuatPanel(JFrame winChu, TaiKhoan taiKhoan, ActionListener dangXuat, KyThuatDAO daoChung) {
        super(new BorderLayout());
        this.winChu = winChu;
        this.taiKhoan = taiKhoan;
        this.dangXuat = dangXuat;
        if (taiKhoan == null || dangXuat == null || daoChung == null) {
            throw new IllegalArgumentException("Thiếu tài khoản, thao tác đăng xuất hoặc nguồn kỹ thuật.");
        }
        taiKhoan.kiemTraPhien();
        dao = daoChung;
        setBackground(GiaoDienKyThuat.NEN);
        add(taoSidebar(), BorderLayout.WEST);
        JPanel pnlKhongGian = new JPanel(new BorderLayout());
        pnlKhongGian.add(new AppHeaderPanel(taiKhoan, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                yeuCauDangXuat();
            }
        }, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                yeuCauThoat();
            }
        }), BorderLayout.NORTH);
        pnlNoiDung.setBackground(GiaoDienKyThuat.NEN);
        pnlNoiDung.setBorder(BorderFactory.createEmptyBorder(0, 32, 0, 32));
        pnlKhongGian.add(pnlNoiDung, BorderLayout.CENTER);
        add(pnlKhongGian, BorderLayout.CENTER);
        List<PhieuSuaChua> cacLenh = dao.docLenh("", "Tất cả");
        if (!cacLenh.isEmpty()) {
            maLenh = cacLenh.get(0).getMaPhieu();
        }
        chuyen("TONG_QUAN");
    }

    // Tạo sidebar gồm thương hiệu, các chức năng và tài khoản đăng nhập.
    private JPanel taoSidebar() {
        JPanel pnlSide = new JPanel(new BorderLayout(0, 18));
        pnlSide.setPreferredSize(new Dimension(216, 0));
        pnlSide.setBackground(GiaoDienKyThuat.SIDEBAR);
        pnlSide.setBorder(BorderFactory.createEmptyBorder(18, 10, 18, 10));
        JPanel pnlDau = GiaoDienKyThuat.taoCot();
        JPanel pnlThuongHieu = new JPanel(new BorderLayout(10, 0));
        pnlThuongHieu.setOpaque(false);
        pnlThuongHieu.setBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x314453)),
                        BorderFactory.createEmptyBorder(6, 10, 18, 0)));
        JLabel lblXe = new JLabel(BieuTuongKyThuat.lay(0));
        lblXe.setPreferredSize(new Dimension(32, 32));
        lblXe.setBorder(BorderFactory.createLineBorder(new Color(0x547080)));
        pnlThuongHieu.add(lblXe, BorderLayout.WEST);
        JPanel pnlTen = GiaoDienKyThuat.taoCot();
        JLabel lblTen = GiaoDienKyThuat.taoNhan("<html>GARAGE ĐỨC<br>DŨNG</html>", true);
        lblTen.setForeground(Color.WHITE);
        pnlTen.add(lblTen);
        JLabel lblGhiChu = GiaoDienKyThuat.taoNhan("Quản lý dịch vụ sửa chữa", false);
        lblGhiChu.setForeground(new Color(0x95A8BA));
        lblGhiChu.setFont(GiaoDienKyThuat.CHU_NHO);
        pnlTen.add(lblGhiChu);
        pnlThuongHieu.add(pnlTen, BorderLayout.CENTER);
        GiaoDienKyThuat.themThanhPhan(pnlDau, pnlThuongHieu);
        JLabel lblNhom = GiaoDienKyThuat.taoNhan("KỸ THUẬT VIÊN", false);
        lblNhom.setFont(GiaoDienKyThuat.CHU_NHO);
        lblNhom.setForeground(new Color(0x8BA2B4));
        lblNhom.setBorder(BorderFactory.createEmptyBorder(4, 12, 8, 0));
        pnlDau.add(lblNhom);
        pnlDau.add(Box.createVerticalStrut(14));
        for (int viTri = 0; viTri < CAC_TRANG.length; viTri++) {
            JButton btnMuc = GiaoDienKyThuat.taoNut("<html>" + CAC_NHAN[viTri] + "</html>", CAC_TRANG[viTri], false,
                    this);
            btnMuc.setIcon(BieuTuongKyThuat.lay(viTri + 1));
            btnMuc.setIconTextGap(10);
            btnMuc.setHorizontalAlignment(SwingConstants.LEFT);
            btnMuc.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 8));
            btnMuc.putClientProperty("khongVien", true);
            int cao = viTri == 1 || viTri == 6 ? 56 : 40;
            btnMuc.setMaximumSize(new Dimension(Integer.MAX_VALUE, cao));
            btnMuc.setPreferredSize(new Dimension(196, cao));
            btnMuc.setMinimumSize(new Dimension(0, cao));
            btnMuc.setAlignmentX(LEFT_ALIGNMENT);
            btnMuc.setName(CAC_TRANG[viTri]);
            nutSidebar.add(btnMuc);
            pnlDau.add(btnMuc);
        }
        pnlSide.add(pnlDau, BorderLayout.NORTH);
        JPanel pnlNguoi = new JPanel(new BorderLayout(8, 0));
        pnlNguoi.setOpaque(false);
        pnlNguoi.setBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0x314453)),
                        BorderFactory.createEmptyBorder(14, 8, 0, 0)));
        JLabel lblTaiKhoan = GiaoDienKyThuat
                .taoNhan("<html>" + taiKhoan.getTenDangNhap() + "<br><small>Kỹ thuật viên</small></html>", false);
        lblTaiKhoan.setForeground(Color.WHITE);
        pnlNguoi.add(taoAvatar(), BorderLayout.WEST);
        pnlNguoi.add(lblTaiKhoan, BorderLayout.CENTER);
        pnlSide.add(pnlNguoi, BorderLayout.SOUTH);
        return pnlSide;
    }

    // Tạo hình đại diện bằng chữ viết tắt của vai trò.
    private JLabel taoAvatar() {
        JLabel lblAvatar = new JLabel("KT") {
            private static final long serialVersionUID = 1L;

            // Vẽ nền tròn của hình đại diện.
            /** Vẽ nội dung của thành phần theo trạng thái hiện tại. */
            @Override
            protected void paintComponent(Graphics doHoa) {
                Graphics2D ve = (Graphics2D) doHoa.create();
                ve.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                ve.setColor(new Color(0xCBD9E6));
                ve.fillOval(0, 0, getWidth(), getHeight());
                ve.dispose();
                super.paintComponent(doHoa);
            }
        };
        lblAvatar.setFont(GiaoDienKyThuat.CHU_NHO);
        lblAvatar.setOpaque(false);
        lblAvatar.setHorizontalAlignment(SwingConstants.CENTER);
        lblAvatar.setPreferredSize(new Dimension(32, 32));
        return lblAvatar;
    }

    // Điều phối tác vụ và giữ biểu mẫu khi thao tác dữ liệu thất bại.
    @Override
    public void actionPerformed(ActionEvent suKien) {
        String lenh = suKien.getActionCommand();
        try {
            for (String ten : CAC_TRANG) {
                if (ten.equals(lenh)) {
                    chuyen(ten);

                    return;
                }
            }
            if ("DANG_XUAT".equals(lenh)) {
                yeuCauDangXuat();
            } else if ("THOAT".equals(lenh)) {
                yeuCauThoat();
            } else if ("TAO_BAO_CAO".equals(lenh)) {
                pnlKiemTraXe.chonPhieu();
            } else if ("HUONG_DAN".equals(lenh)) {
                baoLoi("Hướng dẫn sử dụng", "Chọn công việc được phân công để mở lệnh. "
                        + "Chỉ cập nhật hạng mục đã được duyệt; ghi nhận phụ tùng thực dùng trước khi hoàn tất.");
            } else if ("SUA_BAO_CAO".equals(lenh) && choPhepRoiTrang()) {
                batDauTrang("KIEM_TRA");
                pnlKiemTraXe.hienLapBaoCao();
            } else if ("XOA_BAO_CAO".equals(lenh)) {
                if (xacNhan("Xác nhận xóa", "Xóa báo cáo nháp " + baoCao.getMaBaoCao() + "?", new String[0][0],
                        "Chỉ xóa bản nháp chưa hoàn tất.", "Xóa", true)) {
                    dao.xoaBaoCao(baoCao);
                    batDauTrang("KIEM_TRA");
                    pnlKiemTraXe.hienDanhSachBaoCao();
                }
            } else if ("DOI_CHIEU".equals(lenh)) {
                pnlKiemTraXe.luuBanNhap();
            } else if ("HOAN_TAT_BAO_CAO".equals(lenh)) {
                pnlKiemTraXe.hoanTatBaoCao();
            } else if ("LUU_TIEN_DO".equals(lenh)) {
                dao.luuTienDo(maLenh, viTriHangMuc, (TrangThaiSuaChua) cmbTienDo.getSelectedItem(),
                        txtKetQua.getText());
                anhChup = docNoiDung();
                chuyen("TIEN_DO");
            } else if ("GUI_PHAT_SINH".equals(lenh)) {
                dao.guiPhatSinh(maLenh, txtPhatSinh.getText(), txtPhuTungDuKien.getText(), txtMoTa.getText());
                anhChup = docNoiDung();
                chuyen("PHAT_SINH");
            } else if ("DOI_HANG_MUC".equals(lenh) && !dangNap) {
                pnlGhiNhanPhuTung.napPhuTung();
            } else if ("DOI_PHU_TUNG".equals(lenh) && !dangNap) {
                pnlGhiNhanPhuTung.capNhatTon();
            } else if ("GHI_PHU_TUNG".equals(lenh)) {
                pnlGhiNhanPhuTung.ghiPhuTung();
            } else if ("HOAN_TAT_LENH".equals(lenh)) {
                pnlKiemTraHoanTat.hoanTatLenh();
            }
        } catch (IllegalArgumentException | IllegalStateException | SecurityException loi) {
            baoLoi("Không thể thực hiện", loi.getMessage());
        }
    }

    // Kiểm tra nội dung chưa lưu trước khi chuyển trang hoặc đăng xuất.
    boolean duocRoiTrang() {
        return choPhepRoiTrang();
    }

    // Cho MainFrame kiểm tra nội dung chưa lưu khi đóng hoặc đăng xuất.
    public boolean choPhepRoiTrang() {
        if (tblNhap != null && tblNhap.isEditing() && !tblNhap.getCellEditor().stopCellEditing()) {
            return false;
        }
        if (anhChup.equals(docNoiDung())) {
            return true;
        }
        return xacNhan("Bỏ thay đổi chưa lưu?", "Nội dung đang nhập chưa được lưu.", new String[0][0],
                "Nếu bỏ thay đổi, dữ liệu đã lưu vẫn được giữ nguyên.", "Bỏ thay đổi", true);
    }

    // Chuyển màn hình và cập nhật mục sidebar đang chọn.
    void chuyen(String tenTrang) {
        if (!duocRoiTrang()) {
            return;
        }
        batDauTrang(tenTrang);
        if ("TONG_QUAN".equals(tenTrang)) {
            pnlTongQuanCongViec.hienTongQuan();
        } else if ("PHAN_CONG".equals(tenTrang)) {
            pnlCongViecPhanCong.hienDanhSachLenh("");
        } else if ("KIEM_TRA".equals(tenTrang)) {
            pnlKiemTraXe.hienDanhSachBaoCao();
        } else if (maLenh == null) {
            pnlCongViecPhanCong.hienDanhSachLenh("");
        } else if ("TIEN_DO".equals(tenTrang)) {
            pnlTienDoSuaChua.hienTienDo();
        } else if ("PHAT_SINH".equals(tenTrang)) {
            pnlBaoHuHongPhatSinh.hienPhatSinh();
        } else if ("PHU_TUNG".equals(tenTrang)) {
            pnlGhiNhanPhuTung.hienPhuTung();
        } else if ("HOAN_TAT".equals(tenTrang)) {
            pnlKiemTraHoanTat.hienHoanTat();
        }
    }

    // Xóa trạng thái nhập cũ và tô màu mục điều hướng hiện tại.
    void batDauTrang(String tenTrang) {
        trang = tenTrang;
        truongDangNhap.clear();
        modelNhap = null;
        tblNhap = null;
        btnHoanTat = null;
        anhChup = "";
        for (JButton nut : nutSidebar) {
            boolean dangChon = tenTrang.equals(nut.getName());
            nut.setBackground(dangChon ? GiaoDienKyThuat.DANG_CHON : GiaoDienKyThuat.SIDEBAR);
            nut.setForeground(dangChon ? Color.WHITE : new Color(0xCBD9E6));
        }
    }

    // Gắn trang vào vùng nội dung và lưu trạng thái nhập ban đầu.
    void hienTrang(JPanel pnlManHinh, JPanel pnlCot) {
        JLabel lblCuoi = GiaoDienKyThuat.taoNhan("Garage Đức Dũng · Nội bộ nhân viên", false);
        lblCuoi.setFont(GiaoDienKyThuat.CHU_NHO);
        lblCuoi.setForeground(new Color(0x8390A2));
        lblCuoi.setBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xE9EDF2)),
                        BorderFactory.createEmptyBorder(18, 0, 0, 0)));
        lblCuoi.setMaximumSize(new Dimension(Integer.MAX_VALUE, lblCuoi.getPreferredSize().height));
        GiaoDienKyThuat.themThanhPhan(pnlCot, lblCuoi);
        JScrollPane scrTrang = UiChung.taoVungCuon(pnlCot);
        PhuTungUi.dinhDangVungCuon(scrTrang);
        scrTrang.setBorder(BorderFactory.createEmptyBorder());
        scrTrang.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pnlNoiDung.removeAll();
        pnlManHinh.removeAll();
        pnlManHinh.add(scrTrang, BorderLayout.CENTER);
        pnlNoiDung.add(pnlManHinh, BorderLayout.CENTER);
        pnlNoiDung.revalidate();
        pnlNoiDung.repaint();
        anhChup = docNoiDung();
    }

    // Tạo ô nhập thông thường hoặc chỉ đọc bằng component chung.
    JTextField taoONhap(String noiDung, boolean chiDoc) {
        return GiaoDienKyThuat.taoONhap(noiDung, chiDoc);
    }

    // Tạo ô nhập được theo dõi để phát hiện thay đổi chưa lưu.
    JTextField taoONhap(String noiDung) {
        JTextField txtNhap = taoONhap(noiDung, false);
        truongDangNhap.add(txtNhap);
        txtNhap.getDocument().addDocumentListener(this);
        return txtNhap;
    }

    // Đọc phụ tùng thực dùng và cho phép xem hạng mục chưa được thực hiện.
    String docThucDungAnToan(int viTri) {
        try {
            return dao.docThucDung(maLenh, viTri);
        } catch (IllegalStateException loi) {
            return "Chưa được phép thực hiện";
        }
    }

    // Chụp nội dung biểu mẫu để kiểm tra thay đổi chưa lưu.
    String docNoiDung() {
        StringBuilder noiDung = new StringBuilder();
        for (JTextField txtNhap : truongDangNhap) {
            noiDung.append(txtNhap.getText()).append('\n');
        }
        if ("TIEN_DO".equals(trang) && cmbTienDo != null) {
            noiDung.append(cmbTienDo.getSelectedItem());
        }
        if ("PHU_TUNG".equals(trang) && cmbHangMuc != null && cmbPhuTung != null) {
            noiDung.append(cmbHangMuc.getSelectedIndex()).append(cmbPhuTung.getSelectedItem());
        }
        if (modelNhap != null) {
            for (int dong = 0; dong < modelNhap.getRowCount(); dong++) {
                for (int cot = 0; cot < modelNhap.getColumnCount(); cot++) {
                    noiDung.append(modelNhap.getValueAt(dong, cot)).append('\n');
                }
            }
        }
        return noiDung.toString();
    }

    // Mở hộp thoại đối chiếu và trả về lựa chọn xác nhận.
    boolean xacNhan(String tieuDe, String moTa, String[][] thongTin,
            String canhBao, String nhanNut, boolean nguyHiem) {
        DoiChieuKyThuatDialog winDialog = new DoiChieuKyThuatDialog(winChu, tieuDe, moTa,
                thongTin, canhBao, nhanNut, nguyHiem);
        winDialog.setVisible(true);
        return winDialog.isDongY();
    }

    // Hiển thị thông báo bằng hộp thoại kỹ thuật đang có.
    void baoLoi(String tieuDe, String noiDung) {
        xacNhan(tieuDe, noiDung, new String[0][0], "", null, false);
    }

    // Tính lại dữ liệu phụ thuộc khi thêm ký tự.
    @Override
    public void insertUpdate(DocumentEvent suKien) {
        pnlGhiNhanPhuTung.capNhatTon();
        pnlKiemTraHoanTat.capNhatHoanTat();
    }

    // Tính lại dữ liệu phụ thuộc khi xóa ký tự.
    @Override
    public void removeUpdate(DocumentEvent suKien) {
        pnlGhiNhanPhuTung.capNhatTon();
        pnlKiemTraHoanTat.capNhatHoanTat();
    }

    // Tính lại dữ liệu phụ thuộc khi thuộc tính tài liệu thay đổi.
    @Override
    public void changedUpdate(DocumentEvent suKien) {
        pnlGhiNhanPhuTung.capNhatTon();
        pnlKiemTraHoanTat.capNhatHoanTat();
    }

    // Đăng xuất bằng luồng của khung chung sau khi kiểm tra biểu mẫu.
    private void yeuCauDangXuat() {
        if (duocRoiTrang()) {
            dangXuat.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "dangXuat"));
        }
    }

    // Gửi yêu cầu đóng cửa sổ sau khi kiểm tra nội dung chưa lưu.
    private void yeuCauThoat() {
        if (duocRoiTrang() && winChu != null) {
            winChu.dispatchEvent(new WindowEvent(winChu, WindowEvent.WINDOW_CLOSING));
        }
    }

    /** Giữ ô tìm co giãn, bộ lọc và nút làm mới trong cùng chiều rộng cửa sổ. */
    private JPanel taoThanhLoc(JTextField txtTim, JComboBox<?> cmbLoc, ActionListener loc) {
        JPanel pnl = new JPanel(new BorderLayout(8, 0));
        pnl.setOpaque(false);
        JPanel pnlPhai = new JPanel(new BorderLayout(8, 0));
        pnlPhai.setOpaque(false);
        pnlPhai.add(cmbLoc, BorderLayout.CENTER);
        pnlPhai.add(GiaoDienKyThuat.taoNut("Làm mới", "LOC", false, loc), BorderLayout.EAST);
        pnl.add(txtTim, BorderLayout.CENTER);
        pnl.add(pnlPhai, BorderLayout.EAST);
        return pnl;
    }
}
