package ui.screen.kythuat;

import dao.chung.DuLieuGarageDAO;
import dao.suachua.KyThuatDAO;
import entity.kiemtra.BaoCaoKiemTra;
import entity.kiemtra.TrangThaiKiemTra;
import entity.nhansu.TaiKhoan;
import entity.phutung.PhuTung;
import entity.suachua.HangMucSuaChua;
import entity.suachua.PhieuSuaChua;
import entity.suachua.TrangThaiPhatSinh;
import entity.suachua.TrangThaiSuaChua;
import entity.tiepnhan.PhieuTiepNhan;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import ui.MainFrame;
import ui.component.BieuTuongKyThuat;
import ui.component.GiaoDienKyThuat;
import ui.component.PhuTungUi;
import ui.component.UiChung;
import ui.dialog.kythuat.DoiChieuKyThuatDialog;

// Hiển thị chín trang kỹ thuật và đồng bộ điều hướng trong cùng phiên dữ liệu.
public final class KhongGianKyThuatPanel extends JPanel implements ActionListener, DocumentListener {
    private static final long serialVersionUID = 1L;
    private static final String[] CAC_TRANG = { "TONG_QUAN", "PHAN_CONG", "KIEM_TRA", "TIEN_DO", "PHU_TUNG",
            "PHAT_SINH", "HOAN_TAT" };
    private static final String[] CAC_NHAN = { "Công việc kỹ thuật", "Công việc được phân công", "Báo cáo kiểm tra",
            "Tiến độ sửa chữa", "Phụ tùng sử dụng", "Báo hư hỏng phát sinh", "Kiểm tra hoàn tất sửa chữa" };
    private final MainFrame winChu;
    private final TaiKhoan taiKhoan;
    private final ActionListener dangXuat;
    private final KyThuatDAO dao;
    private final JPanel pnlNoiDung = new JPanel(new BorderLayout());
    private final List<JButton> nutSidebar = new ArrayList<>();
    private final List<JButton> nutMenu = new ArrayList<>();
    private final List<JTextField> truongDangNhap = new ArrayList<JTextField>();
    private final JTextField txtTimNhanh = taoONhap("", false);
    private String trang = "TONG_QUAN";
    private String maLenh;
    private int viTriHangMuc;
    private BaoCaoKiemTra baoCao;
    private PhieuTiepNhan phieu;
    private DefaultTableModel modelNhap;
    private JTable tblNhap;
    private JTextField txtLoi;
    private JTextField txtDeXuat;
    private JTextField txtKetLuan;
    private JTextField txtKetQua;
    private JTextField txtPhatSinh;
    private JTextField txtPhuTungDuKien;
    private JTextField txtMoTa;
    private JTextField txtLuong;
    private JTextField txtTon;
    private JTextField txtTonSau;
    private JComboBox<TrangThaiSuaChua> cmbTienDo;
    private JComboBox<String> cmbHangMuc;
    private JComboBox<PhuTung> cmbPhuTung;
    private JButton btnHoanTat;
    private String maLuot;
    private String anhChup = "";
    private boolean dangNap;
    private JPanel pnlTimNhanhHeader;

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public KhongGianKyThuatPanel(MainFrame winChu, TaiKhoan taiKhoan, ActionListener dangXuat) {
        this(winChu, taiKhoan, dangXuat, new KyThuatDAO(taiKhoan));
    }

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public KhongGianKyThuatPanel(MainFrame winChu, TaiKhoan taiKhoan, ActionListener dangXuat, KyThuatDAO daoChung) {
        super(new BorderLayout());
        this.winChu = winChu;
        this.taiKhoan = taiKhoan;
        this.dangXuat = dangXuat;
        if (daoChung == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        dao = daoChung;
        setBackground(GiaoDienKyThuat.NEN);
        add(taoSidebar(), BorderLayout.WEST);
        JPanel pnlKhongGian = new JPanel(new BorderLayout());
        pnlKhongGian.add(taoHeader(), BorderLayout.NORTH);
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

    // Tạo sidebar có đủ bảy chức năng và thông tin tài khoản ở cuối.
    /** Tạo sidebar cho giao diện. */
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

    // Tạo hình đại diện bằng chữ viết tắt của vai trò đăng nhập.
    /** Tạo avatar cho giao diện. */
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

    // Tạo menu trên, tìm kiếm nhanh và thông tin tài khoản.
    /** Tạo header cho giao diện. */
    private JPanel taoHeader() {
        JPanel pnlHeader = new JPanel(new BorderLayout(12, 0)) {
            private static final long serialVersionUID = 1L;

            // Thu gọn tìm kiếm khi cửa sổ hẹp để tránh đè lên menu.
            /** Thực hiện do layout cho thành phần hiện tại. */
            @Override
            public void doLayout() {
                if (pnlTimNhanhHeader != null) {
                    pnlTimNhanhHeader.setVisible(getWidth() >= 1080);
                }
                super.doLayout();
            }
        };
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setPreferredSize(new Dimension(0, 66));
        pnlHeader.setBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE9EDF2)),
                        BorderFactory.createEmptyBorder(12, 20, 12, 20)));
        JPanel pnlMenu = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        pnlMenu.setOpaque(false);
        for (String ten : new String[] { "Hệ thống", "Danh mục", "Nghiệp vụ", "Báo cáo", "Trợ giúp" }) {
            JButton btnMenu = GiaoDienKyThuat.taoNut(ten, "MENU:" + ten, false, this);
            btnMenu.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            btnMenu.putClientProperty("khongVien", true);
            btnMenu.setName(ten);
            nutMenu.add(btnMenu);
            pnlMenu.add(btnMenu);
        }
        pnlHeader.add(pnlMenu, BorderLayout.WEST);
        JPanel pnlPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlPhai.setOpaque(false);
        txtTimNhanh.setToolTipText("Nhập mã phiếu hoặc biển số rồi nhấn Enter");
        txtTimNhanh.putClientProperty("goiY", "Tìm phiếu / biển số...");
        txtTimNhanh.setActionCommand("TIM_NHANH");
        txtTimNhanh.addActionListener(this);
        JPanel pnlTimNhanh = new JPanel(new BorderLayout(6, 0));
        pnlTimNhanhHeader = pnlTimNhanh;
        pnlTimNhanh.setBackground(GiaoDienKyThuat.NHAT);
        pnlTimNhanh.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(GiaoDienKyThuat.VIEN),
                BorderFactory.createEmptyBorder(0, 8, 0, 6)));
        pnlTimNhanh.add(new JLabel(BieuTuongKyThuat.lay(8)), BorderLayout.WEST);
        txtTimNhanh.setBorder(BorderFactory.createEmptyBorder());
        txtTimNhanh.setBackground(GiaoDienKyThuat.NHAT);
        txtTimNhanh.setPreferredSize(new Dimension(180, 34));
        pnlTimNhanh.add(txtTimNhanh, BorderLayout.CENTER);
        pnlPhai.add(pnlTimNhanh);
        pnlPhai.add(GiaoDienKyThuat.taoNut("Thông báo", "THONG_BAO", false, this));
        pnlPhai.add(taoAvatar());
        pnlPhai.add(GiaoDienKyThuat
                .taoNhan("<html>" + taiKhoan.getTenDangNhap() + "<br><small>Kỹ thuật viên</small></html>", false));
        pnlHeader.add(pnlPhai, BorderLayout.EAST);
        return pnlHeader;
    }

    // Điều phối tác vụ và giữ nguyên biểu mẫu khi thao tác dữ liệu thất bại.
    /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
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
            if (lenh.startsWith("MENU:")) {
                moMenu(lenh.substring(5), (Component) suKien.getSource());
            } else if ("DANG_XUAT".equals(lenh)) {
                if (choPhepRoiTrang()) {
                    dangXuat.actionPerformed(new ActionEvent(dangXuat, ActionEvent.ACTION_PERFORMED, "thaoTac"));
                }
            } else if ("TIM_NHANH".equals(lenh)) {
                if (choPhepRoiTrang()) {
                    hienDanhSachLenh(txtTimNhanh.getText());
                }
            } else if ("THONG_BAO".equals(lenh)) {
                baoLoi("Thông báo", "Có " + dao.docLenh("", "Chờ phụ tùng").size() + " lệnh đang chờ phụ tùng.");
            } else if ("TAO_BAO_CAO".equals(lenh)) {
                chonPhieu();
            } else if ("HUONG_DAN".equals(lenh)) {
                baoLoi("Hướng dẫn sử dụng", "Chọn công việc được phân công để mở lệnh. "
                        + "Chỉ cập nhật hạng mục đã được duyệt; ghi nhận phụ tùng thực dùng trước khi hoàn tất.");
            } else if ("SUA_BAO_CAO".equals(lenh) && choPhepRoiTrang()) {
                batDauTrang("KIEM_TRA");
                hienLapBaoCao();
            } else if ("XOA_BAO_CAO".equals(lenh)) {
                if (xacNhan("Xác nhận xóa", "Xóa báo cáo nháp " + baoCao.getMaBaoCao() + "?", new String[0][0],
                        "Chỉ xóa bản nháp chưa hoàn tất.", "Xóa", true)) {
                    dao.xoaBaoCao(baoCao);
                    batDauTrang("KIEM_TRA");
                    hienDanhSachBaoCao();
                }
            } else if ("DOI_CHIEU".equals(lenh)) {
                luuBanNhap();
            } else if ("HOAN_TAT_BAO_CAO".equals(lenh)) {
                hoanTatBaoCao();
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
                napPhuTung();
            } else if ("DOI_PHU_TUNG".equals(lenh) && !dangNap) {
                capNhatTon();
            } else if ("GHI_PHU_TUNG".equals(lenh)) {
                ghiPhuTung();
            } else if ("HOAN_TAT_LENH".equals(lenh)) {
                hoanTatLenh();
            }
        } catch (IllegalArgumentException | IllegalStateException | SecurityException loi) {
            baoLoi("Không thể thực hiện", loi.getMessage());
        }
    }

    // Mở menu chức năng và dùng lại tác vụ đăng xuất của khung chung.
    /** Mở menu khi người dùng yêu cầu. */
    private void moMenu(String ten, Component chu) {
        JPopupMenu mnuPopup = new JPopupMenu();
        String[] nhan = "Hệ thống".equals(ten) ? new String[] { "Công việc kỹ thuật", "Đăng xuất" }
                : "Nghiệp vụ".equals(ten) ? CAC_NHAN
                        : "Trợ giúp".equals(ten) ? new String[] { "Hướng dẫn sử dụng" } : new String[0];
        if (nhan.length == 0) {
            baoLoi(ten, "Chức năng này chưa được triển khai trong phân hệ kỹ thuật.");

            return;
        }
        for (int viTri = 0; viTri < nhan.length; viTri++) {
            JMenuItem mniMuc = new JMenuItem(nhan[viTri]);
            mniMuc.setFont(GiaoDienKyThuat.CHU_THUONG);
            mniMuc.setBackground(Color.WHITE);
            mniMuc.setActionCommand("Hệ thống".equals(ten) ? viTri == 0 ? "TONG_QUAN" : "DANG_XUAT"
                    : "Trợ giúp".equals(ten) ? "HUONG_DAN" : CAC_TRANG[viTri]);
            mniMuc.addActionListener(this);
            mnuPopup.add(mniMuc);
        }
        mnuPopup.show(chu, 0, chu.getHeight());
    }

    // Kiểm tra nội dung chưa lưu trước khi chuyển trang hoặc đăng xuất.
    /** Kiểm tra dữ liệu chưa lưu trước khi đổi trang, đăng xuất hoặc đóng cửa sổ. */
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

    // Chuyển trang và cập nhật trạng thái active của sidebar cùng menu trên.
    /** Thực hiện chuyen cho thành phần hiện tại. */
    private void chuyen(String tenTrang) {
        if (!choPhepRoiTrang()) {

            return;
        }
        batDauTrang(tenTrang);
        if ("TONG_QUAN".equals(tenTrang)) {
            hienTongQuan();
        } else if ("PHAN_CONG".equals(tenTrang)) {
            hienDanhSachLenh("");
        } else if ("KIEM_TRA".equals(tenTrang)) {
            hienDanhSachBaoCao();
        } else if (maLenh == null) {
            hienDanhSachLenh("");
        } else if ("TIEN_DO".equals(tenTrang)) {
            hienTienDo();
        } else if ("PHAT_SINH".equals(tenTrang)) {
            hienPhatSinh();
        } else if ("PHU_TUNG".equals(tenTrang)) {
            hienPhuTung();
        } else if ("HOAN_TAT".equals(tenTrang)) {
            hienHoanTat();
        }
    }

    // Xóa trạng thái biểu mẫu cũ và tô màu mục điều hướng đang chọn.
    /** Thực hiện bat dau trang cho thành phần hiện tại. */
    private void batDauTrang(String tenTrang) {
        trang = tenTrang;
        truongDangNhap.clear();
        modelNhap = null;
        tblNhap = null;
        btnHoanTat = null;
        anhChup = "";
        for (JButton muc : nutSidebar) {
            boolean active = muc.getName().equals(tenTrang);
            muc.setBackground(active ? GiaoDienKyThuat.DANG_CHON : GiaoDienKyThuat.SIDEBAR);
            muc.setForeground(active ? Color.WHITE : new Color(0xCBD9E6));
        }
        for (JButton muc : nutMenu) {
            boolean active = muc.getName().equals("TONG_QUAN".equals(tenTrang) ? "Hệ thống" : "Nghiệp vụ");
            muc.setBackground(active ? new Color(0xEEF4FA) : Color.WHITE);
            muc.setForeground(active ? GiaoDienKyThuat.XANH : GiaoDienKyThuat.CHU);
        }
    }

    // Tạo đầu trang gồm breadcrumb, tiêu đề, mô tả và hành động quay lại.
    /** Tạo trang cho giao diện. */
    private JPanel taoTrang(String tieuDe, String moTa, String nut, String lenh) {
        JPanel pnlCot = GiaoDienKyThuat.taoCot();
        pnlCot.setBorder(BorderFactory.createEmptyBorder(23, 26, 24, 26));
        JLabel lblBreadcrumb = GiaoDienKyThuat.taoNhan(
                "Kỹ thuật viên / " + ("TONG_QUAN".equals(trang) ? "Hệ thống / " : "Nghiệp vụ / ") + tieuDe, false);
        lblBreadcrumb.setFont(GiaoDienKyThuat.CHU_NHO);
        lblBreadcrumb.setForeground(GiaoDienKyThuat.CHU_PHU);
        lblBreadcrumb.setPreferredSize(new Dimension(0, 18));
        GiaoDienKyThuat.themThanhPhan(pnlCot, lblBreadcrumb);
        JPanel pnlDau = new JPanel(new BorderLayout(12, 0));
        pnlDau.setOpaque(false);
        JPanel pnlChu = GiaoDienKyThuat.taoCot();
        JLabel lblTen = GiaoDienKyThuat.taoNhan(tieuDe, true);
        lblTen.setFont(GiaoDienKyThuat.TIEU_DE);
        lblTen.setPreferredSize(new Dimension(lblTen.getPreferredSize().width, 30));
        pnlChu.add(lblTen);
        pnlChu.add(Box.createVerticalStrut(5));
        JLabel lblMoTa = GiaoDienKyThuat.taoNhan(moTa, false);
        lblMoTa.setForeground(GiaoDienKyThuat.CHU_PHU);
        lblMoTa.setPreferredSize(new Dimension(lblMoTa.getPreferredSize().width, 20));
        pnlChu.add(lblMoTa);
        pnlDau.add(pnlChu, BorderLayout.CENTER);
        if (nut != null) {
            JPanel pnlNutDau = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            pnlNutDau.setOpaque(false);
            pnlNutDau.add(GiaoDienKyThuat.taoNut(nut, lenh, "TAO_BAO_CAO".equals(lenh), this));
            pnlDau.add(pnlNutDau, BorderLayout.EAST);
        }
        GiaoDienKyThuat.themThanhPhan(pnlCot, pnlDau);
        return pnlCot;
    }

    // Hiển thị trang trong vùng nội dung và chụp trạng thái ban đầu của biểu mẫu.
    /** Thực hiện hien trang cho thành phần hiện tại. */
    private void hienTrang(JPanel pnlCot) {
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
        pnlNoiDung.add(scrTrang, BorderLayout.CENTER);
        pnlNoiDung.revalidate();
        pnlNoiDung.repaint();
        anhChup = docNoiDung();
    }

    // Tạo một hàng chia đều các cột bằng GridLayout.
    /** Tạo hang cho giao diện. */
    private JPanel taoHang(JComponent... cacCot) {
        JPanel pnlHang = new JPanel(new GridLayout(1, cacCot.length, 14, 0));
        pnlHang.setOpaque(false);
        for (JComponent cot : cacCot) {
            pnlHang.add(cot);
        }
        return pnlHang;
    }

    // Chia nội dung thành hai cột theo tỷ lệ 59/41 và căn đỉnh.
    /** Tạo hai cot cho giao diện. */
    private JPanel taoHaiCot(JComponent trai, JComponent phai) {
        JPanel pnlHaiCot = new JPanel(new GridBagLayout());
        pnlHaiCot.setOpaque(false);
        JComponent[] cacCot = { trai, phai };
        for (int viTri = 0; viTri < cacCot.length; viTri++) {
            GridBagConstraints rangBuoc = new GridBagConstraints();
            rangBuoc.gridx = viTri;
            rangBuoc.weightx = viTri == 0 ? 0.59 : 0.41;
            rangBuoc.fill = GridBagConstraints.HORIZONTAL;
            rangBuoc.anchor = GridBagConstraints.NORTH;
            rangBuoc.insets = new Insets(0, viTri == 0 ? 0 : 7, 0, viTri == 0 ? 7 : 0);
            cacCot[viTri].setPreferredSize(new Dimension(0, cacCot[viTri].getPreferredSize().height));
            pnlHaiCot.add(cacCot[viTri], rangBuoc);
        }
        return pnlHaiCot;
    }

    // Tạo nhóm nhãn và giá trị chỉ đọc từ chứng từ gốc.
    /** Tạo gia tri cho giao diện. */
    private JPanel taoGiaTri(String nhan, String giaTri) {
        return GiaoDienKyThuat.taoTruong(nhan, GiaoDienKyThuat.taoNhan(giaTri, true));
    }

    // Tạo ô nhập thông thường hoặc chỉ đọc bằng thành phần dùng chung.
    /** Tạo onhap cho giao diện. */
    private JTextField taoONhap(String noiDung, boolean chiDoc) {
        return GiaoDienKyThuat.taoONhap(noiDung, chiDoc);
    }

    // Tạo ô nhập được theo dõi để bảo vệ nội dung chưa lưu.
    /** Tạo onhap cho giao diện. */
    private JTextField taoONhap(String noiDung) {
        JTextField txtNhap = taoONhap(noiDung, false);
        truongDangNhap.add(txtNhap);
        txtNhap.getDocument().addDocumentListener(this);
        return txtNhap;
    }

    // Tạo thanh bước có vòng tròn số, đường nối và màu của bước đang thực hiện.
    /** Tạo thanh buoc cho giao diện. */
    private JPanel taoThanhBuoc(String... tenBuoc) {
        JPanel pnlBuoc = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        pnlBuoc.setOpaque(false);
        pnlBuoc.setBorder(BorderFactory.createEmptyBorder(2, 0, 8, 0));
        int buocHienTai = "TIEN_DO".equals(trang) || "KIEM_TRA".equals(trang) ? 1 : 0;
        Color mauChuPhu = new Color(0x7789A3);
        Color mauDuong = new Color(0xDCE5EF);
        for (int viTri = 0; viTri < tenBuoc.length; viTri++) {
            final boolean dangChon = viTri == buocHienTai;
            JPanel pnlMuc = new JPanel(new BorderLayout(8, 0));
            pnlMuc.setOpaque(false);
            JLabel lblSo = new JLabel(String.valueOf(viTri + 1), SwingConstants.CENTER) {
                private static final long serialVersionUID = 1L;

                // Vẽ vòng tròn số và tô xanh nhạt cho bước đang thực hiện.
                /** Vẽ nội dung của thành phần theo trạng thái hiện tại. */
                @Override
                protected void paintComponent(Graphics doHoa) {
                    Graphics2D ve = (Graphics2D) doHoa.create();
                    ve.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    ve.setColor(dangChon ? new Color(0xEAF4FF) : GiaoDienKyThuat.NEN);
                    ve.fillOval(1, 1, getWidth() - 3, getHeight() - 3);
                    ve.setColor(dangChon ? new Color(0xA8CCEF) : mauDuong);
                    ve.setStroke(new BasicStroke(1.5f));
                    ve.drawOval(1, 1, getWidth() - 3, getHeight() - 3);
                    ve.dispose();
                    super.paintComponent(doHoa);
                }
            };
            lblSo.setOpaque(false);
            lblSo.setFont((dangChon ? GiaoDienKyThuat.CHU_DAM : GiaoDienKyThuat.CHU_THUONG).deriveFont(14.0f));
            lblSo.setForeground(dangChon ? GiaoDienKyThuat.XANH : mauChuPhu);
            lblSo.setPreferredSize(new Dimension(28, 28));
            JLabel lblTen = GiaoDienKyThuat.taoNhan(tenBuoc[viTri], dangChon);
            lblTen.setFont((dangChon ? GiaoDienKyThuat.CHU_DAM : GiaoDienKyThuat.CHU_THUONG).deriveFont(14.0f));
            lblTen.setForeground(dangChon ? GiaoDienKyThuat.XANH : mauChuPhu);
            pnlMuc.add(lblSo, BorderLayout.WEST);
            pnlMuc.add(lblTen, BorderLayout.CENTER);
            pnlBuoc.add(pnlMuc);
            if (viTri + 1 < tenBuoc.length) {
                JPanel pnlDuong = new JPanel() {
                    private static final long serialVersionUID = 1L;

                    // Vẽ đường nối ngang căn giữa hai bước.
                    /** Vẽ nội dung của thành phần theo trạng thái hiện tại. */
                    @Override
                    protected void paintComponent(Graphics doHoa) {
                        super.paintComponent(doHoa);
                        doHoa.setColor(mauDuong);
                        doHoa.fillRect(0, getHeight() / 2, getWidth(), 1);
                    }
                };
                pnlDuong.setOpaque(false);
                pnlDuong.setPreferredSize(new Dimension(64, 28));
                pnlBuoc.add(pnlDuong);
            }
        }
        return pnlBuoc;
    }

    // Hiển thị tổng quan và đường tắt từ dữ liệu mẫu hiện tại trong phiên.
    /** Thực hiện hien tong quan cho thành phần hiện tại. */
    private void hienTongQuan() {
        JPanel pnlTrang = taoTrang("Công việc kỹ thuật",
                "Ưu tiên lệnh được giao, kiểm tra xe và cập nhật phụ tùng đã dùng.", "Tác vụ thường dùng", "PHAN_CONG");
        int dangSua = 0;
        int choPhuTung = 0;
        int hoanTat = 0;
        List<PhieuSuaChua> cacLenh = dao.docLenh("", "Tất cả");
        for (PhieuSuaChua lenh : cacLenh) {
            if ("Chờ phụ tùng".equals(dao.docTrangThai(lenh))) {
                choPhuTung++;
            } else if (lenh.getTrangThai() != TrangThaiSuaChua.HOAN_THANH) {
                dangSua++;
            }
            if (lenh.getThoiDiemHoanTat() != null && lenh.getThoiDiemHoanTat().toLocalDate().equals(LocalDate.now())) {
                hoanTat++;
            }
        }
        List<PhieuTiepNhan> cacPhieuCho = new ArrayList<PhieuTiepNhan>();
        for (PhieuTiepNhan phieuCho : dao.docPhieu()) {
            BaoCaoKiemTra daCo = phieuCho.isDaXacNhan() ? dao.docBaoCao(phieuCho) : null;
            if (phieuCho.isDaXacNhan() && (daCo == null || daCo.getTrangThai() == TrangThaiKiemTra.NHAP)) {
                cacPhieuCho.add(phieuCho);
            }
        }
        GiaoDienKyThuat.themThanhPhan(pnlTrang,
                taoHang(taoTheSo("Đang sửa", dangSua, "Lệnh thuộc phạm vi phân công"),
                        taoTheSo("Xe chờ kiểm tra", cacPhieuCho.size(), "Ưu tiên theo thời điểm tiếp nhận"),
                        taoTheSo("Chờ phụ tùng", choPhuTung, "Không xuất vượt tồn kho"),
                        taoTheSo("Hoàn tất hôm nay", hoanTat, "Đã ghi nhận kết quả")));
        JPanel pnlTrai = GiaoDienKyThuat.taoCot();
        JPanel pnlLenh = GiaoDienKyThuat.taoCot();
        for (PhieuSuaChua lenh : cacLenh) {
            JButton btnLenh = GiaoDienKyThuat.taoNut("<html><b>" + lenh.getMaPhieu() + " · " + lenh.getBienSo()
                    + "</b><br>" + dao.docTrangThai(lenh) + " · Hạn "
                    + lenh.getHanHoanThanh().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) + "</html>",
                    "LENH:" + lenh.getMaPhieu(), false, new ActionListener() {
                        // Mở đúng lệnh được chọn từ trang tổng quan.
                        /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
                        @Override
                        public void actionPerformed(ActionEvent suKien) {
                            maLenh = lenh.getMaPhieu();
                            viTriHangMuc = 0;
                            chuyen("TIEN_DO");
                        }
                    });
            btnLenh.setHorizontalAlignment(SwingConstants.LEFT);
            btnLenh.setPreferredSize(new Dimension(0, 64));
            btnLenh.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
            pnlLenh.add(btnLenh);
        }
        GiaoDienKyThuat.themThanhPhan(pnlTrai,
                GiaoDienKyThuat.taoThe("Lệnh được phân công", "Xem nhanh tình trạng và hạn xử lý", pnlLenh));
        JPanel pnlCho = GiaoDienKyThuat.taoCot();
        for (PhieuTiepNhan phieuCho : cacPhieuCho) {
            JButton btnPhieu = GiaoDienKyThuat.taoNut("<html>" + phieuCho.getMaPhieu() + " · " + phieuCho.getBienSo()
                    + "<br>" + phieuCho.getYeuCau() + "</html>", "MO_PHIEU", false, new ActionListener() {
                        // Mở báo cáo của đúng phiếu tiếp nhận được chọn.
                        /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
                        @Override
                        public void actionPerformed(ActionEvent suKien) {
                            moPhieu(phieuCho);
                        }
                    });
            btnPhieu.setHorizontalAlignment(SwingConstants.LEFT);
            btnPhieu.setPreferredSize(new Dimension(0, 64));
            btnPhieu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
            pnlCho.add(btnPhieu);
        }
        GiaoDienKyThuat.themThanhPhan(pnlTrai, GiaoDienKyThuat.taoThe("Xe chờ kiểm tra", "", pnlCho));
        JPanel pnlPhai = GiaoDienKyThuat.taoCot();
        JPanel pnlTacVu = new JPanel(new GridLayout(2, 2, 10, 10));
        pnlTacVu.setOpaque(false);
        pnlTacVu.add(GiaoDienKyThuat.taoNut("Lập báo cáo kiểm tra", "TAO_BAO_CAO", true, this));
        pnlTacVu.add(GiaoDienKyThuat.taoNut("Cập nhật tiến độ", "TIEN_DO", false, this));
        pnlTacVu.add(GiaoDienKyThuat.taoNut("Phụ tùng đã dùng", "PHU_TUNG", false, this));
        pnlTacVu.add(GiaoDienKyThuat.taoNut("Báo phát sinh", "PHAT_SINH", false, this));
        JPanel pnlTacVuCot = GiaoDienKyThuat.taoCot();
        GiaoDienKyThuat.themThanhPhan(pnlTacVuCot, pnlTacVu);
        pnlTacVuCot.add(GiaoDienKyThuat
                .taoThongBao("Hạng mục phát sinh phải có báo giá được khách chấp thuận trước khi sửa.", false));
        GiaoDienKyThuat.themThanhPhan(pnlPhai,
                GiaoDienKyThuat.taoThe("Công việc tiếp theo", "Cập nhật từ một lệnh đang thực hiện", pnlTacVuCot));
        JPanel pnlDuongTat = new JPanel(new GridLayout(2, 2, 10, 10));
        pnlDuongTat.setOpaque(false);
        for (int viTri : new int[] { 1, 2, 3, 4 }) {
            pnlDuongTat.add(GiaoDienKyThuat.taoNut(CAC_NHAN[viTri], CAC_TRANG[viTri], false, this));
        }
        GiaoDienKyThuat.themThanhPhan(pnlPhai, GiaoDienKyThuat.taoThe("Đường tắt", "", pnlDuongTat));
        GiaoDienKyThuat.themThanhPhan(pnlTrang, taoHaiCot(pnlTrai, pnlPhai));
        hienTrang(pnlTrang);
    }

    // Tạo thẻ thống kê gồm nhãn, số lượng và chú thích.
    /** Tạo the so cho giao diện. */
    private JPanel taoTheSo(String nhan, int so, String ghiChu) {
        JPanel pnlThe = GiaoDienKyThuat.taoMatPhang();
        JLabel lblNhan = GiaoDienKyThuat.taoNhan(nhan, false);
        lblNhan.setFont(GiaoDienKyThuat.CHU_NHO);
        lblNhan.setForeground(GiaoDienKyThuat.CHU_PHU);
        JLabel lblSo = GiaoDienKyThuat.taoNhan(String.format("%02d", so), true);
        lblSo.setFont(GiaoDienKyThuat.CHU_DAM.deriveFont(24.0f));
        JTextArea txaChuThich = PhuTungUi.taoMoTa(ghiChu);
        txaChuThich.setFont(GiaoDienKyThuat.CHU_NHO);
        txaChuThich.setForeground(GiaoDienKyThuat.CHU_PHU);
        pnlThe.add(lblNhan, BorderLayout.NORTH);
        pnlThe.add(lblSo, BorderLayout.CENTER);
        pnlThe.add(txaChuThich, BorderLayout.SOUTH);
        return pnlThe;
    }

    // Hiển thị công việc được phân công với tìm kiếm và lọc trạng thái.
    /** Thực hiện hien danh sach lenh cho thành phần hiện tại. */
    private void hienDanhSachLenh(String tuKhoa) {
        batDauTrang("PHAN_CONG");
        JPanel pnlTrang = taoTrang("Công việc được phân công", "Chỉ hiển thị xe/phiếu trong phạm vi phân công.", null,
                null);
        JTextField txtTim = taoONhap(tuKhoa, false);
        txtTim.putClientProperty("goiY", "Tìm mã lệnh hoặc biển số...");
        txtTim.setPreferredSize(new Dimension(380, 36));
        JComboBox<String> cmbLoc = new JComboBox<String>(
                new String[] { "Tất cả", "Chờ sửa chữa", "Đang sửa", "Chờ phụ tùng", "Hoàn thành" });
        GiaoDienKyThuat.dinhDangCombo(cmbLoc);
        cmbLoc.setPreferredSize(new Dimension(190, 36));
        DefaultTableModel model = GiaoDienKyThuat.taoModel("Mã lệnh", "Biển số", "Nội dung", "Hạn", "Trạng thái",
                "Thao tác");
        JTable tblBang = GiaoDienKyThuat.taoBang(model);
        List<PhieuSuaChua> cacDong = new ArrayList<PhieuSuaChua>();
        JLabel lblSo = GiaoDienKyThuat.taoNhan("", false);
        ActionListener loc = new ActionListener() {
            // Nạp công việc theo tiêu chí đang chọn và giữ dữ liệu gắn với mỗi dòng.
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent suKien) {
                cacDong.clear();
                cacDong.addAll(dao.docLenh(txtTim.getText().trim(), (String) cmbLoc.getSelectedItem()));
                model.setRowCount(0);
                for (PhieuSuaChua lenh : cacDong) {
                    model.addRow(new Object[] { lenh.getMaPhieu(), lenh.getBienSo(), lenh.getGhiChu(),
                            lenh.getHanHoanThanh().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")),
                            dao.docTrangThai(lenh), "Xem chi tiết" });
                }
                lblSo.setText("Hiển thị " + cacDong.size() + " / " + cacDong.size() + " hồ sơ");
            }
        };
        txtTim.addActionListener(loc);
        cmbLoc.addActionListener(loc);
        JPanel pnlLoc = taoThanhLoc(txtTim, cmbLoc, loc);
        JPanel pnlThanh = GiaoDienKyThuat.taoMatPhang();
        pnlThanh.add(pnlLoc);
        GiaoDienKyThuat.themThanhPhan(pnlTrang, pnlThanh);
        tblBang.addMouseListener(new MouseAdapter() {
            // Mở đúng lệnh khi bấm thao tác hoặc nhấp đôi vào dòng dữ liệu.
            /** Mở use clicked khi người dùng yêu cầu. */
            @Override
            public void mouseClicked(MouseEvent suKien) {
                int dong = tblBang.rowAtPoint(suKien.getPoint());
                if (dong >= 0 && (tblBang.columnAtPoint(suKien.getPoint()) == 5 || suKien.getClickCount() == 2)) {
                    maLenh = cacDong.get(tblBang.convertRowIndexToModel(dong)).getMaPhieu();
                    viTriHangMuc = 0;
                    chuyen("TIEN_DO");
                }
            }
        });
        JPanel pnlBang = new JPanel(new BorderLayout(0, 12));
        pnlBang.setOpaque(false);
        pnlBang.add(GiaoDienKyThuat.bocBang(tblBang));
        pnlBang.add(lblSo, BorderLayout.SOUTH);
        loc.actionPerformed(new ActionEvent(this, 0, "LOC"));
        GiaoDienKyThuat.themThanhPhan(pnlTrang, GiaoDienKyThuat.taoThe("Danh sách công việc được phân công",
                "Chọn một dòng để xem, lọc hoặc mở tác vụ liên quan.", pnlBang));
        hienTrang(pnlTrang);
    }

    // Hiển thị danh sách báo cáo với bộ lọc và hành động lập báo cáo.
    /** Thực hiện hien danh sach bao cao cho thành phần hiện tại. */
    private void hienDanhSachBaoCao() {
        JPanel pnlTrang = taoTrang("Báo cáo kiểm tra",
                "Kiểm tra xe có phiếu tiếp nhận hợp lệ và ghi kết quả chẩn đoán.", "+ Lập báo cáo kiểm tra",
                "TAO_BAO_CAO");
        JTextField txtTim = taoONhap("", false);
        txtTim.putClientProperty("goiY", "Tìm mã báo cáo, phiếu, biển số...");
        txtTim.setPreferredSize(new Dimension(380, 36));
        JComboBox<String> cmbLoc = new JComboBox<String>(new String[] { "Tất cả", "Nháp", "Hoàn tất" });
        GiaoDienKyThuat.dinhDangCombo(cmbLoc);
        cmbLoc.setPreferredSize(new Dimension(190, 36));
        DefaultTableModel model = GiaoDienKyThuat.taoModel("Mã báo cáo", "Phiếu tiếp nhận", "Biển số", "Người kiểm tra",
                "Trạng thái", "Thao tác");
        JTable tblBang = GiaoDienKyThuat.taoBang(model);
        List<BaoCaoKiemTra> cacDong = new ArrayList<BaoCaoKiemTra>();
        JLabel lblSo = GiaoDienKyThuat.taoNhan("", false);
        ActionListener loc = new ActionListener() {
            // Lọc báo cáo và hiển thị trạng thái nghiệp vụ tương ứng.
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent suKien) {
                cacDong.clear();
                cacDong.addAll(dao.docBaoCao(txtTim.getText().trim(), (String) cmbLoc.getSelectedItem()));
                model.setRowCount(0);
                for (BaoCaoKiemTra dong : cacDong) {
                    model.addRow(new Object[] { dong.getMaBaoCao(), dong.getPhieu().getMaPhieu(),
                            dong.getPhieu().getBienSo(), taiKhoan.getTenDangNhap(),
                            dong.getTrangThai() == TrangThaiKiemTra.HOAN_TAT ? "Đã hoàn tất" : "Chưa xác nhận",
                            "Xem chi tiết" });
                }
                lblSo.setText("Hiển thị " + cacDong.size() + " / " + cacDong.size() + " hồ sơ");
            }
        };
        txtTim.addActionListener(loc);
        cmbLoc.addActionListener(loc);
        JPanel pnlLoc = taoThanhLoc(txtTim, cmbLoc, loc);
        JPanel pnlThanh = GiaoDienKyThuat.taoMatPhang();
        pnlThanh.add(pnlLoc);
        GiaoDienKyThuat.themThanhPhan(pnlTrang, pnlThanh);
        tblBang.addMouseListener(new MouseAdapter() {
            // Mở trang đối chiếu của đúng báo cáo được chọn.
            /** Mở use clicked khi người dùng yêu cầu. */
            @Override
            public void mouseClicked(MouseEvent suKien) {
                int dong = tblBang.rowAtPoint(suKien.getPoint());
                if (dong >= 0 && (tblBang.columnAtPoint(suKien.getPoint()) == 5 || suKien.getClickCount() == 2)) {
                    baoCao = cacDong.get(tblBang.convertRowIndexToModel(dong));
                    phieu = baoCao.getPhieu();
                    batDauTrang("KIEM_TRA");
                    hienDoiChieu();
                }
            }
        });
        JPanel pnlBang = new JPanel(new BorderLayout(0, 10));
        pnlBang.setOpaque(false);
        pnlBang.add(GiaoDienKyThuat.bocBang(tblBang));
        pnlBang.add(lblSo, BorderLayout.SOUTH);
        loc.actionPerformed(new ActionEvent(this, 0, "LOC"));
        GiaoDienKyThuat.themThanhPhan(pnlTrang, GiaoDienKyThuat.taoThe("Danh sách báo cáo kiểm tra",
                "Chọn một dòng để xem, lọc hoặc mở tác vụ liên quan.", pnlBang));
        hienTrang(pnlTrang);
    }

    // Cho chọn phiếu hợp lệ đang chờ kiểm tra hoặc đang có báo cáo nháp.
    /** Thực hiện chon phieu cho thành phần hiện tại. */
    private void chonPhieu() {
        if (!choPhepRoiTrang()) {

            return;
        }
        List<PhieuTiepNhan> cacPhieu = new ArrayList<PhieuTiepNhan>();
        for (PhieuTiepNhan dong : dao.docPhieu()) {
            if (dong.isDaXacNhan()) {
                BaoCaoKiemTra cu = dao.docBaoCao(dong);
                if (cu == null || cu.getTrangThai() == TrangThaiKiemTra.NHAP) {
                    cacPhieu.add(dong);
                }
            }
        }
        if (cacPhieu.isEmpty()) {
            baoLoi("Không có phiếu phù hợp", "Không còn phiếu hợp lệ đang chờ kiểm tra trong phạm vi phân công.");

            return;
        }
        JPopupMenu mnuPhieu = new JPopupMenu();
        for (PhieuTiepNhan dong : cacPhieu) {
            JMenuItem mniPhieu = new JMenuItem(dong.getMaPhieu() + " · " + dong.getBienSo());
            mniPhieu.setFont(GiaoDienKyThuat.CHU_THUONG);
            mniPhieu.addActionListener(new ActionListener() {
                // Mở phiếu đã chọn và nạp lại báo cáo nháp nếu đã có.
                /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
                @Override
                public void actionPerformed(ActionEvent suKien) {
                    moPhieu(dong);
                }
            });
            mnuPhieu.add(mniPhieu);
        }
        mnuPhieu.show(pnlNoiDung, 50, 110);
    }

    // Mở biểu mẫu kiểm tra theo thông tin gốc của phiếu đã xác nhận.
    /** Mở phieu khi người dùng yêu cầu. */
    private void moPhieu(PhieuTiepNhan dong) {
        if (!choPhepRoiTrang()) {

            return;
        }
        phieu = dong;
        baoCao = dao.docBaoCao(dong);
        batDauTrang("KIEM_TRA");
        hienLapBaoCao();
    }

    // Hiển thị biểu mẫu nhập kết quả và đề xuất của ba khu vực kiểm tra.
    /** Thực hiện hien lap bao cao cho thành phần hiện tại. */
    private void hienLapBaoCao() {
        JPanel pnlTrang = taoTrang("Lập báo cáo kiểm tra xe",
                "Kết quả phải đối chiếu với tình trạng ban đầu và yêu cầu khách hàng.", "Danh sách / quay lại",
                "KIEM_TRA");
        GiaoDienKyThuat.themThanhPhan(pnlTrang, taoThanhBuoc("Nhận phiếu", "Ghi kết quả", "Đối chiếu & lưu"));
        JPanel pnlCot = GiaoDienKyThuat.taoCot();
        GiaoDienKyThuat.themThanhPhan(pnlCot,
                taoHang(taoGiaTri("Phiếu tiếp nhận / xe", phieu.getMaPhieu() + " · " + phieu.getBienSo()),
                        taoGiaTri("Người kiểm tra", taiKhoan.getTenDangNhap())));
        GiaoDienKyThuat.themThanhPhan(pnlCot, GiaoDienKyThuat
                .taoThongBao("Thông tin gốc từ tiếp nhận: " + phieu.getYeuCau() + " · " + phieu.getTinhTrang(), false));
        modelNhap = new DefaultTableModel(new String[] { "Khu vực", "Kết quả kiểm tra", "Kết luận / đề xuất" }, 0) {
            private static final long serialVersionUID = 1L;

            // Chỉ cho sửa kết quả và đề xuất của khu vực kiểm tra.
            /** Cho biết cell editable. */
            @Override
            public boolean isCellEditable(int dong, int cot) {
                return cot > 0;
            }
        };
        String[] noiDung = baoCao == null ? new String[] { "", "", "", "", "", "", "" } : baoCao.getNoiDung();
        String[] deXuat = baoCao == null ? new String[] { "", "", "" } : baoCao.getDeXuatTheoVung();
        String[] khuVuc = { "Phanh", "Khoang máy / dầu", "Điện – điều hòa" };
        for (int viTri = 0; viTri < 3; viTri++) {
            modelNhap.addRow(new Object[] { khuVuc[viTri], noiDung[viTri + 3], deXuat[viTri] });
        }
        tblNhap = GiaoDienKyThuat.taoBang(modelNhap);
        GiaoDienKyThuat.themThanhPhan(pnlCot, GiaoDienKyThuat.bocBang(tblNhap));
        txtLoi = taoONhap(noiDung[1]);
        txtDeXuat = taoONhap(noiDung[2]);
        GiaoDienKyThuat.themThanhPhan(pnlCot, taoHang(GiaoDienKyThuat.taoTruong("Lỗi phát hiện *", txtLoi),
                GiaoDienKyThuat.taoTruong("Hạng mục đề xuất *", txtDeXuat)));
        pnlCot.add(
                GiaoDienKyThuat.taoHangNut(GiaoDienKyThuat.taoNut("Đối chiếu & lưu báo cáo", "DOI_CHIEU", true, this)));
        GiaoDienKyThuat.themThanhPhan(pnlTrang,
                GiaoDienKyThuat.taoThe("Lập báo cáo kiểm tra " + (baoCao == null ? "mới" : baoCao.getMaBaoCao()),
                        "Tự lấy thông tin tiếp nhận, chỉ nhập kết quả chẩn đoán và hạng mục đề xuất.", pnlCot));
        hienTrang(pnlTrang);
    }

    // Lưu bản nháp từ biểu mẫu và chuyển sang trang đối chiếu.
    /** Kiểm tra và lưu ban nhap. */
    private void luuBanNhap() {
        if (tblNhap.isEditing() && !tblNhap.getCellEditor().stopCellEditing()) {

            return;
        }
        String[] noiDung = { txtLoi.getText(), txtLoi.getText(), txtDeXuat.getText(), "", "", "", "" };
        String[] deXuat = new String[3];
        for (int viTri = 0; viTri < 3; viTri++) {
            noiDung[viTri + 3] = String.valueOf(modelNhap.getValueAt(viTri, 1)).trim();
            deXuat[viTri] = String.valueOf(modelNhap.getValueAt(viTri, 2)).trim();
        }
        BaoCaoKiemTra nhap = new BaoCaoKiemTra(baoCao == null ? "" : baoCao.getMaBaoCao(), phieu,
                baoCao == null ? LocalDate.now() : baoCao.getNgayKiemTra(), "Tổng hợp", noiDung, TrangThaiKiemTra.NHAP,
                null, deXuat);
        baoCao = dao.luuBaoCao(nhap, false);
        batDauTrang("KIEM_TRA");
        hienDoiChieu();
    }

    // Hiển thị nội dung đối chiếu và phần xác nhận hoàn tất báo cáo.
    /** Thực hiện hien doi chieu cho thành phần hiện tại. */
    private void hienDoiChieu() {
        JPanel pnlTrang = taoTrang("Đối chiếu báo cáo kiểm tra",
                "Kiểm tra đầy đủ kết quả trước khi chuyển cho tiếp nhận lập báo giá.", "Danh sách / quay lại",
                "KIEM_TRA");
        JPanel pnlTrai = GiaoDienKyThuat.taoCot();
        GiaoDienKyThuat.themThanhPhan(pnlTrai, taoHang(taoGiaTri("Phiếu tiếp nhận", phieu.getMaPhieu()),
                taoGiaTri("Biển số", phieu.getBienSo()), taoGiaTri("Người kiểm tra", taiKhoan.getTenDangNhap())));
        DefaultTableModel model = GiaoDienKyThuat.taoModel("Khu vực", "Kết quả kiểm tra", "Đề xuất");
        String[] ten = { "Hệ thống phanh", "Khoang máy / dầu", "Điện / điều hòa" };
        for (int viTri = 0; viTri < 3; viTri++) {
            model.addRow(
                    new Object[] { ten[viTri], baoCao.getNoiDung()[viTri + 3], baoCao.getDeXuatTheoVung()[viTri] });
        }
        GiaoDienKyThuat.themThanhPhan(pnlTrai, GiaoDienKyThuat.bocBang(GiaoDienKyThuat.taoBang(model)));
        pnlTrai.add(GiaoDienKyThuat.taoThongBao(
                "Đề xuất là cơ sở để tiếp nhận lập báo giá; chưa cho phép sửa khi khách chưa duyệt.", true));
        JPanel pnlPhai = GiaoDienKyThuat.taoCot();
        GiaoDienKyThuat.themThanhPhan(pnlPhai,
                GiaoDienKyThuat.taoTruong("Tình trạng khi nhận · chỉ đọc", taoONhap(phieu.getTinhTrang(), true)));
        boolean daXong = baoCao.getTrangThai() == TrangThaiKiemTra.HOAN_TAT;
        txtKetLuan = daXong ? taoONhap(baoCao.getNoiDung()[0], true) : taoONhap(baoCao.getNoiDung()[0]);
        GiaoDienKyThuat.themThanhPhan(pnlPhai, GiaoDienKyThuat.taoTruong("Kết luận kiểm tra", txtKetLuan));
        GiaoDienKyThuat.themThanhPhan(pnlPhai, GiaoDienKyThuat.taoTruong("Trạng thái báo cáo · chỉ đọc",
                taoONhap(daXong ? "Đã hoàn tất" : "Chưa xác nhận", true)));
        GiaoDienKyThuat.themThanhPhan(pnlPhai, GiaoDienKyThuat.taoThongBao(
                "Chỉ hoàn tất khi nội dung kiểm tra đúng. Chuyển kết quả cho Tiếp nhận lập báo giá.", true));
        JButton btnXong = GiaoDienKyThuat.taoNut("Hoàn tất báo cáo", "HOAN_TAT_BAO_CAO", true, this);
        btnXong.setEnabled(!daXong);
        pnlPhai.add(btnXong);
        if (!daXong) {
            pnlPhai.add(Box.createVerticalStrut(10));
            pnlPhai.add(GiaoDienKyThuat.taoNut("Sửa bản nháp", "SUA_BAO_CAO", false, this));
            pnlPhai.add(Box.createVerticalStrut(10));
            JButton btnXoa = GiaoDienKyThuat.taoNut("Xóa bản nháp", "XOA_BAO_CAO", false, this);
            btnXoa.setForeground(new Color(0xB3261E));
            btnXoa.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0xB3261E)),
                    BorderFactory.createEmptyBorder(8, 14, 8, 14)));
            pnlPhai.add(btnXoa);
        }
        GiaoDienKyThuat.themThanhPhan(pnlTrang,
                taoHang(GiaoDienKyThuat.taoThe("Đối chiếu báo cáo " + baoCao.getMaBaoCao(),
                        "Thông tin chẩn đoán phục vụ lập báo giá; không thay bằng bảng giá.", pnlTrai),
                        GiaoDienKyThuat.taoThe("Kiểm tra trước khi gửi", "Đối chiếu kết quả với tình trạng ban đầu.",
                                pnlPhai)));
        hienTrang(pnlTrang);
    }

    // Xác nhận đối chiếu rồi hoàn tất báo cáo hiện tại.
    /** Thực hiện hoan tat bao cao cho thành phần hiện tại. */
    private void hoanTatBaoCao() {
        if (!xacNhan("Đối chiếu báo cáo kiểm tra", "Xác nhận kết quả kiểm tra trước khi gửi tiếp nhận.",
                new String[][] { { "Báo cáo", baoCao.getMaBaoCao() }, { "Phiếu tiếp nhận", phieu.getMaPhieu() },
                        { "Biển số", phieu.getBienSo() }, { "Kết luận", txtKetLuan.getText() } },
                "Chỉ hoàn tất khi kết quả kiểm tra đã được đối chiếu.", "Hoàn tất báo cáo", false)) {

            return;
        }
        String[] noiDung = baoCao.getNoiDung();
        noiDung[0] = txtKetLuan.getText().trim();
        baoCao = dao.luuBaoCao(new BaoCaoKiemTra(baoCao.getMaBaoCao(), phieu, baoCao.getNgayKiemTra(),
                baoCao.getBoPhan(), noiDung, TrangThaiKiemTra.NHAP, null, baoCao.getDeXuatTheoVung()), true);
        batDauTrang("KIEM_TRA");
        hienDoiChieu();
    }

    // Hiển thị tiến độ và biểu mẫu cập nhật của hạng mục đang chọn.
    /** Thực hiện hien tien do cho thành phần hiện tại. */
    private void hienTienDo() {
        PhieuSuaChua lenh = dao.docLenh(maLenh);
        JPanel pnlTrang = taoTrang("Tiến độ sửa chữa", "Chỉ thao tác trong phạm vi hạng mục khách đã xác nhận.",
                "Danh sách / quay lại", "PHAN_CONG");
        GiaoDienKyThuat.themThanhPhan(pnlTrang, taoThanhBuoc("Nhận lệnh", "Thực hiện", "Kiểm tra hoàn tất"));
        JPanel pnlCot = GiaoDienKyThuat.taoCot();
        GiaoDienKyThuat.themThanhPhan(pnlCot,
                taoHang(taoGiaTri("Biển số", lenh.getBienSo()), taoGiaTri("Kỹ thuật viên", taiKhoan.getTenDangNhap()),
                        taoGiaTri("Trạng thái lệnh", dao.docTrangThai(lenh))));
        DefaultTableModel model = GiaoDienKyThuat.taoModel("Hạng mục được duyệt", "Tiến độ", "Kết quả / ghi chú",
                "Phụ tùng thực dùng");
        for (int viTri = 0; viTri < lenh.getDanhSachHangMuc().size(); viTri++) {
            HangMucSuaChua hangMuc = lenh.getDanhSachHangMuc().get(viTri);
            model.addRow(new Object[] { hangMuc.getTenHangMuc(), hangMuc.getTrangThai(), hangMuc.getKetQua(),
                    docThucDungAnToan(viTri) });
        }
        JTable tblBang = GiaoDienKyThuat.taoBang(model);
        viTriHangMuc = Math.min(viTriHangMuc, lenh.getDanhSachHangMuc().size() - 1);
        HangMucSuaChua hangMuc = lenh.getDanhSachHangMuc().get(viTriHangMuc);
        txtKetQua = taoONhap(hangMuc.getKetQua());
        txtKetQua.setEditable(lenh.getTrangThai() != TrangThaiSuaChua.HOAN_THANH);
        cmbTienDo = new JComboBox<TrangThaiSuaChua>(TrangThaiSuaChua.values());
        GiaoDienKyThuat.dinhDangCombo(cmbTienDo);
        cmbTienDo.setSelectedItem(hangMuc.getTrangThai());
        cmbTienDo.setEnabled(lenh.getTrangThai() != TrangThaiSuaChua.HOAN_THANH);
        tblBang.setRowSelectionInterval(viTriHangMuc, viTriHangMuc);
        tblBang.addMouseListener(new MouseAdapter() {
            // Đổi hạng mục sau khi xử lý nội dung chưa lưu của dòng trước.
            /** Mở use clicked khi người dùng yêu cầu. */
            @Override
            public void mouseClicked(MouseEvent suKien) {
                int dong = tblBang.rowAtPoint(suKien.getPoint());
                if (dong >= 0 && dong != viTriHangMuc && choPhepRoiTrang()) {
                    viTriHangMuc = tblBang.convertRowIndexToModel(dong);
                    batDauTrang("TIEN_DO");
                    hienTienDo();
                }
            }
        });
        GiaoDienKyThuat.themThanhPhan(pnlCot, GiaoDienKyThuat.bocBang(tblBang));
        GiaoDienKyThuat.themThanhPhan(pnlCot, taoHang(GiaoDienKyThuat.taoTruong("Kết quả cập nhật *", txtKetQua),
                GiaoDienKyThuat.taoTruong("Trạng thái sau cập nhật", cmbTienDo)));
        GiaoDienKyThuat.themThanhPhan(pnlCot,
                GiaoDienKyThuat.taoThongBao("Các hạng mục phát sinh chưa được khách duyệt phải dừng riêng; "
                        + "không đánh dấu hoàn tất thay cho hạng mục đang chờ.", true));
        JButton btnLuu = GiaoDienKyThuat.taoNut("Lưu cập nhật tiến độ", "LUU_TIEN_DO", true, this);
        btnLuu.setEnabled(lenh.getTrangThai() != TrangThaiSuaChua.HOAN_THANH);
        pnlCot.add(GiaoDienKyThuat.taoHangNut(btnLuu));
        GiaoDienKyThuat.themThanhPhan(pnlTrang, GiaoDienKyThuat.taoThe("Cập nhật tiến độ " + maLenh,
                "Chọn một dòng để cập nhật; không đổi thay các hạng mục còn lại.", pnlCot));
        hienTrang(pnlTrang);
    }

    // Hiển thị phát sinh hiện có hoặc biểu mẫu đề xuất mới.
    /** Thực hiện hien phat sinh cho thành phần hiện tại. */
    private void hienPhatSinh() {
        PhieuSuaChua lenh = dao.docLenh(maLenh);
        JPanel pnlTrang = taoTrang("Báo hư hỏng phát sinh",
                "Tạm dừng hạng mục phát sinh cho đến khi khách đồng ý báo giá bổ sung.", "Danh sách / quay lại",
                "PHAN_CONG");
        boolean chiDoc = lenh.getPhatSinh() != null || lenh.getTrangThai() == TrangThaiSuaChua.HOAN_THANH;
        String[] noiDung = lenh.getPhatSinh() == null ? new String[] { "", "", "", "", "" }
                : lenh.getPhatSinh().getNoiDung();
        txtPhatSinh = chiDoc ? taoONhap(noiDung[0], true) : taoONhap(noiDung[0]);
        txtPhuTungDuKien = chiDoc ? taoONhap(noiDung[2], true) : taoONhap(noiDung[2]);
        txtMoTa = chiDoc ? taoONhap(noiDung[1], true) : taoONhap(noiDung[1]);
        JPanel pnlCot = GiaoDienKyThuat.taoCot();
        GiaoDienKyThuat.themThanhPhan(pnlCot,
                taoHang(taoGiaTri("Mã lệnh", maLenh), taoGiaTri("Xe", lenh.getBienSo()),
                        taoGiaTri("Trạng thái phát sinh", lenh.getPhatSinh() == null ? "Chưa gửi đề xuất"
                                : lenh.getPhatSinh().getTrangThai().getTenHienThi())));
        GiaoDienKyThuat.themThanhPhan(pnlCot, taoHang(GiaoDienKyThuat.taoTruong("Hạng mục phát sinh *", txtPhatSinh),
                GiaoDienKyThuat.taoTruong("Phụ tùng dự kiến", txtPhuTungDuKien)));
        GiaoDienKyThuat.themThanhPhan(pnlCot,
                taoHang(GiaoDienKyThuat.taoTruong("Mô tả & bằng chứng *", txtMoTa), GiaoDienKyThuat.taoTruong(
                        "Chi phí dự kiến · chỉ đọc", taoONhap("Theo báo giá bổ sung – chưa phê duyệt", true))));
        GiaoDienKyThuat.themThanhPhan(pnlCot,
                GiaoDienKyThuat.taoThongBao(
                        "Không tự chuyển phát sinh thành hạng mục đã duyệt. Tiếp nhận gửi báo giá bổ sung; "
                                + "chỉ tiếp tục sau khi khách đồng ý.",
                        true));
        JButton btnGui = GiaoDienKyThuat.taoNut("Gửi báo cáo phát sinh", "GUI_PHAT_SINH", true, this);
        btnGui.setEnabled(!chiDoc);
        pnlCot.add(GiaoDienKyThuat.taoHangNut(btnGui));
        GiaoDienKyThuat.themThanhPhan(pnlTrang, GiaoDienKyThuat.taoThe("Báo hư hỏng phát sinh",
                "Tạm dừng riêng hạng mục phát sinh, báo Tiếp nhận lập báo giá bổ sung trước khi sửa.", pnlCot));
        hienTrang(pnlTrang);
    }

    // Hiển thị biểu mẫu ghi nhận phụ tùng gắn với lệnh và hạng mục hiện tại.
    /** Thực hiện hien phu tung cho thành phần hiện tại. */
    private void hienPhuTung() {
        PhieuSuaChua lenh = dao.docLenh(maLenh);
        JPanel pnlTrang = taoTrang("Phụ tùng sử dụng", "Trừ kho theo số lượng thực tế đã dùng, không vượt tồn.",
                "Danh sách / quay lại", "PHAN_CONG");
        JPanel pnlCot = GiaoDienKyThuat.taoCot();
        GiaoDienKyThuat.themThanhPhan(pnlCot, taoHang(taoGiaTri("Lệnh sửa chữa", maLenh),
                taoGiaTri("Biển số", lenh.getBienSo()), taoGiaTri("Người ghi nhận", taiKhoan.getTenDangNhap())));
        cmbHangMuc = new JComboBox<String>();
        for (HangMucSuaChua hangMuc : lenh.getDanhSachHangMuc()) {
            cmbHangMuc.addItem(hangMuc.getTenHangMuc());
        }
        cmbPhuTung = new JComboBox<PhuTung>();
        GiaoDienKyThuat.dinhDangCombo(cmbHangMuc);
        GiaoDienKyThuat.dinhDangCombo(cmbPhuTung);
        txtLuong = taoONhap("1");
        txtTon = taoONhap("", true);
        txtTonSau = taoONhap("", true);
        cmbHangMuc.setActionCommand("DOI_HANG_MUC");
        cmbHangMuc.addActionListener(this);
        cmbPhuTung.setActionCommand("DOI_PHU_TUNG");
        cmbPhuTung.addActionListener(this);
        cmbHangMuc.setSelectedIndex(Math.min(viTriHangMuc, cmbHangMuc.getItemCount() - 1));
        napPhuTung();
        GiaoDienKyThuat.themThanhPhan(pnlCot, taoHang(GiaoDienKyThuat.taoTruong("Phụ tùng *", cmbPhuTung),
                GiaoDienKyThuat.taoTruong("Hạng mục được duyệt *", cmbHangMuc)));
        GiaoDienKyThuat.themThanhPhan(pnlCot,
                taoHang(GiaoDienKyThuat.taoTruong("Đơn vị / tồn khả dụng · chỉ đọc", txtTon),
                        GiaoDienKyThuat.taoTruong("Số lượng thực dùng *", txtLuong),
                        GiaoDienKyThuat.taoTruong("Kết quả sau ghi nhận · chỉ đọc", txtTonSau)));
        GiaoDienKyThuat.themThanhPhan(pnlCot,
                GiaoDienKyThuat
                        .taoThongBao("Nếu số lượng thực dùng lớn hơn tồn khả dụng hoặc hạng mục chưa được duyệt, "
                                + "hệ thống từ chối ghi nhận và không trừ kho.", true));
        JButton btnGhi = GiaoDienKyThuat.taoNut("Ghi nhận đã sử dụng", "GHI_PHU_TUNG", true, this);
        btnGhi.setEnabled(lenh.getTrangThai() != TrangThaiSuaChua.HOAN_THANH);
        pnlCot.add(GiaoDienKyThuat.taoHangNut(btnGhi));
        maLuot = DuLieuGarageDAO.taoMaThaoTac();
        GiaoDienKyThuat.themThanhPhan(pnlTrang, GiaoDienKyThuat.taoThe("Ghi nhận phụ tùng thực tế đã dùng",
                "Chỉ ghi một lần cho cùng lượt sử dụng; giữ chứng từ và lịch sử thực dùng.", pnlCot));
        hienTrang(pnlTrang);
    }

    // Nạp phụ tùng được phép sử dụng cho hạng mục đang chọn.
    /** Đọc lại dữ liệu và cập nhật phần hiển thị. */
    private void napPhuTung() {
        dangNap = true;
        cmbPhuTung.removeAllItems();
        try {
            for (PhuTung phuTung : dao.docPhuTung(maLenh, cmbHangMuc.getSelectedIndex())) {
                cmbPhuTung.addItem(phuTung);
            }
            if (cmbPhuTung.getItemCount() > 1) {
                cmbPhuTung.setSelectedIndex(1);
            }
        } catch (IllegalStateException loi) {
            txtTon.setText(loi.getMessage());
        } finally {
            dangNap = false;
        }
        capNhatTon();
    }

    // Tính tồn dự kiến để hiển thị mà chưa thay đổi tồn kho.
    /** Thực hiện cap nhat ton cho thành phần hiện tại. */
    private void capNhatTon() {
        if (!"PHU_TUNG".equals(trang) || dangNap || cmbPhuTung == null || txtTon == null || txtLuong == null) {

            return;
        }
        PhuTung phuTung = (PhuTung) cmbPhuTung.getSelectedItem();
        if (phuTung == null) {
            txtTon.setText("Không có phụ tùng hợp lệ cho hạng mục này");
            txtTonSau.setText("Không ghi nhận xuất kho");

            return;
        }
        double ton = dao.docTon(phuTung.getMaPhuTung());
        txtTon.setText(phuTung.getDonViTinh() + " · " + String.valueOf(ton));
        try {
            double luong = Double.parseDouble(txtLuong.getText().trim().replace(',', '.'));
            txtTonSau.setText("Tồn dự kiến: " + String.valueOf((ton - luong)));
        } catch (NumberFormatException loi) {
            txtTonSau.setText("Nhập số lượng hợp lệ");
        }
    }

    // Đối chiếu thông tin rồi ghi nhận phụ tùng sau khi người dùng xác nhận.
    /** Thực hiện ghi phu tung cho thành phần hiện tại. */
    private void ghiPhuTung() {
        PhuTung phuTung = (PhuTung) cmbPhuTung.getSelectedItem();
        if (phuTung == null) {
            throw new IllegalArgumentException("Chưa có phụ tùng hợp lệ để ghi nhận.");
        }
        double soLuong = PhuTungUi.docSoLuong(txtLuong);
        phuTung.kiemTraSoLuong(soLuong);
        if (!xacNhan("Ghi nhận phụ tùng đã sử dụng", "Gắn phụ tùng vào lệnh và hạng mục được khách đồng ý.",
                new String[][] { { "Lệnh sửa chữa", maLenh },
                        { "Hạng mục đã duyệt", (String) cmbHangMuc.getSelectedItem() },
                        { "Phụ tùng", phuTung.toString() }, { "Đơn vị / tồn khả dụng", txtTon.getText() },
                        { "Số lượng thực tế dùng", String.valueOf(soLuong) },
                        { "Tồn dự kiến sau ghi", txtTonSau.getText() } },
                "Từ chối nếu vượt tồn hoặc hạng mục chưa được duyệt. Ghi lịch sử xuất một lần cho số thực dùng.",
                "Ghi nhận đã dùng", false)) {

            return;
        }
        dao.ghiNhan(maLenh, cmbHangMuc.getSelectedIndex(), phuTung.getMaPhuTung(), soLuong, maLuot);
        anhChup = docNoiDung();
        chuyen("TIEN_DO");
    }

    // Hiển thị kiểm tra cuối theo từng hạng mục và điều kiện hoàn tất lệnh.
    /** Thực hiện hien hoan tat cho thành phần hiện tại. */
    private void hienHoanTat() {
        PhieuSuaChua lenh = dao.docLenh(maLenh);
        JPanel pnlTrang = taoTrang("Kiểm tra hoàn tất sửa chữa",
                "Rà soát hạng mục, kết quả và phụ tùng rồi báo hoàn thành.", "Danh sách / quay lại", "PHAN_CONG");
        GiaoDienKyThuat.themThanhPhan(pnlTrang, taoThanhBuoc("Hạng mục", "Kiểm tra cuối", "Báo hoàn tất"));
        JPanel pnlCot = GiaoDienKyThuat.taoCot();
        int daXong = 0;
        for (HangMucSuaChua hangMuc : lenh.getDanhSachHangMuc()) {
            if (hangMuc.getTrangThai() == TrangThaiSuaChua.HOAN_THANH) {
                daXong++;
            }
        }
        GiaoDienKyThuat.themThanhPhan(pnlCot, taoHang(taoGiaTri("Biển số / xe", lenh.getBienSo()),
                taoGiaTri("Kỹ thuật viên phụ trách", lenh.getKyThuatChinh()),
                taoGiaTri("Tiến độ", daXong + "/" + lenh.getDanhSachHangMuc().size() + " hạng mục hoàn thành")));
        modelNhap = new DefaultTableModel(
                new String[] { "Hạng mục", "Kết quả", "Phụ tùng thực tế", "Kiểm tra cuối · Đạt" }, 0) {
            private static final long serialVersionUID = 1L;

            // Khai báo cột kiểm tra kiểu Boolean để sử dụng checkbox.
            /** Trả về column class. */
            @Override
            public Class<?> getColumnClass(int cot) {
                return cot == 3 ? Boolean.class : Object.class;
            }

            // Chỉ cho xác nhận đạt với hạng mục đã hoàn thành.
            /** Cho biết cell editable. */
            @Override
            public boolean isCellEditable(int dong, int cot) {
                return cot == 3 && lenh.getTrangThai() != TrangThaiSuaChua.HOAN_THANH
                        && lenh.getDanhSachHangMuc().get(dong).getTrangThai() == TrangThaiSuaChua.HOAN_THANH;
            }
        };
        for (int viTri = 0; viTri < lenh.getDanhSachHangMuc().size(); viTri++) {
            HangMucSuaChua hangMuc = lenh.getDanhSachHangMuc().get(viTri);
            modelNhap.addRow(new Object[] { hangMuc.getTenHangMuc(), hangMuc.getKetQua(), docThucDungAnToan(viTri),
                    lenh.getTrangThai() == TrangThaiSuaChua.HOAN_THANH });
        }
        tblNhap = GiaoDienKyThuat.taoBang(modelNhap);
        JCheckBox chkDat = new JCheckBox();
        PhuTungUi.dinhDangKiemTra(chkDat);
        tblNhap.setDefaultEditor(Boolean.class, new DefaultCellEditor(chkDat));
        tblNhap.setDefaultRenderer(Boolean.class, new TableCellRenderer() {
            // Vẽ ô kiểm tra theo định dạng của các thành phần kỹ thuật.
            /** Trả về table cell renderer component. */
            @Override
            public Component getTableCellRendererComponent(JTable bang, Object giaTri, boolean chon, boolean tieuDiem,
                    int dong, int cot) {
                JCheckBox chkVe = new JCheckBox();
                PhuTungUi.dinhDangKiemTra(chkVe);
                chkVe.setSelected(Boolean.TRUE.equals(giaTri));
                chkVe.setHorizontalAlignment(SwingConstants.LEFT);
                return chkVe;
            }
        });
        GiaoDienKyThuat.themThanhPhan(pnlCot, GiaoDienKyThuat.bocBang(tblNhap));
        String lyDo = dao.docLyDoChuaHoanTat(maLenh);
        GiaoDienKyThuat.themThanhPhan(pnlCot,
                GiaoDienKyThuat.taoThongBao(
                        lyDo.isEmpty() ? "Đối chiếu từng hạng mục và đánh dấu Đạt trước khi báo hoàn tất." : lyDo,
                        !lyDo.isEmpty()));
        txtKetLuan = taoONhap(lenh.getKetQuaHoanTat()[0]);
        txtKetLuan.setEditable(lenh.getTrangThai() != TrangThaiSuaChua.HOAN_THANH);
        GiaoDienKyThuat.themThanhPhan(pnlCot, taoHang(GiaoDienKyThuat.taoTruong("Kết quả kiểm tra cuối *", txtKetLuan),
                GiaoDienKyThuat.taoTruong("Phát sinh chưa duyệt? · chỉ đọc",
                        taoONhap(lenh.getPhatSinh() != null
                                && lenh.getPhatSinh().getTrangThai() == TrangThaiPhatSinh.CHO_DUYET ? "Có → buộc xử lý"
                                        : "Không có",
                                true))));
        btnHoanTat = GiaoDienKyThuat.taoNut("Hoàn tất khi đủ điều kiện", "HOAN_TAT_LENH", true, this);
        btnHoanTat.setEnabled(false);
        modelNhap.addTableModelListener(new TableModelListener() {
            // Cập nhật điều kiện hoàn tất khi người dùng đánh dấu kiểm tra.
            /** Thực hiện table changed cho thành phần hiện tại. */
            @Override
            public void tableChanged(TableModelEvent suKien) {
                capNhatHoanTat();
            }
        });
        pnlCot.add(GiaoDienKyThuat.taoHangNut(btnHoanTat));
        GiaoDienKyThuat.themThanhPhan(pnlTrang,
                GiaoDienKyThuat.taoThe("Kiểm tra hoàn tất " + maLenh,
                        "Chỉ xác nhận hoàn thành khi toàn bộ hạng mục hợp lệ đã hoàn tất "
                                + "và không còn phát sinh chưa duyệt.",
                        pnlCot));
        hienTrang(pnlTrang);
    }

    // Đọc thực dùng và vẫn cho xem hạng mục phát sinh chưa được phép thực hiện.
    /** Đọc thuc dung an toan từ dữ liệu hiện tại. */
    private String docThucDungAnToan(int viTri) {
        try {
            return dao.docThucDung(maLenh, viTri);
        } catch (IllegalStateException loi) {
            return "Chưa được phép thực hiện";
        }
    }

    // Bật nút hoàn tất khi lệnh hợp lệ, có kết luận và mọi dòng đã đạt kiểm tra cuối.
    /** Thực hiện cap nhat hoan tat cho thành phần hiện tại. */
    private void capNhatHoanTat() {
        if (!"HOAN_TAT".equals(trang) || btnHoanTat == null || modelNhap == null) {

            return;
        }
        boolean duDieuKien = dao.docLyDoChuaHoanTat(maLenh).isEmpty() && !txtKetLuan.getText().isBlank();
        for (int dong = 0; dong < modelNhap.getRowCount(); dong++) {
            duDieuKien = duDieuKien && Boolean.TRUE.equals(modelNhap.getValueAt(dong, 3));
        }
        btnHoanTat.setEnabled(duDieuKien);
    }

    // Xác nhận lần cuối rồi lưu hoàn thành và quay lại danh sách công việc.
    /** Thực hiện hoan tat lenh cho thành phần hiện tại. */
    private void hoanTatLenh() {
        if (tblNhap.isEditing()) {
            tblNhap.getCellEditor().stopCellEditing();
        }
        boolean[] daDat = new boolean[modelNhap.getRowCount()];
        for (int dong = 0; dong < daDat.length; dong++) {
            daDat[dong] = Boolean.TRUE.equals(modelNhap.getValueAt(dong, 3));
        }
        if (xacNhan("Xác nhận hoàn tất sửa chữa", "Đối chiếu kết quả trước khi chuyển thu ngân.",
                new String[][] { { "Lệnh sửa chữa", maLenh }, { "Kết luận", txtKetLuan.getText() } },
                "Chỉ báo hoàn thành; chưa thanh toán hoặc bàn giao xe.", "Hoàn tất sửa chữa", false)) {
            dao.hoanTat(maLenh, daDat, txtKetLuan.getText());
            anhChup = docNoiDung();
            chuyen("PHAN_CONG");
        }
    }

    // Chụp trường nhập, lựa chọn và bảng để phát hiện thay đổi chưa lưu.
    /** Đọc noi dung từ dữ liệu hiện tại. */
    private String docNoiDung() {
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

    // Mở hộp thoại đối chiếu và trả lựa chọn của người dùng.
    /** Thực hiện xac nhan cho thành phần hiện tại. */
    private boolean xacNhan(String tieuDe, String moTa, String[][] thongTin, String canhBao, String nhanNut,
            boolean nguyHiem) {
        DoiChieuKyThuatDialog winDialog = new DoiChieuKyThuatDialog(winChu, tieuDe, moTa, thongTin, canhBao, nhanNut,
                nguyHiem);
        winDialog.setVisible(true);
        return winDialog.isDongY();
    }

    // Hiển thị thông báo bằng hộp thoại của phân hệ kỹ thuật.
    /** Thực hiện bao loi cho thành phần hiện tại. */
    private void baoLoi(String tieuDe, String noiDung) {
        xacNhan(tieuDe, noiDung, new String[0][0], "", null, false);
    }

    // Tính lại các giá trị phụ thuộc khi nhập thêm ký tự.
    /** Thực hiện insert update cho thành phần hiện tại. */
    @Override
    public void insertUpdate(DocumentEvent suKien) {
        capNhatTon();
        capNhatHoanTat();
    }

    // Tính lại các giá trị phụ thuộc khi xóa ký tự.
    /** Thực hiện remove update cho thành phần hiện tại. */
    @Override
    public void removeUpdate(DocumentEvent suKien) {
        capNhatTon();
        capNhatHoanTat();
    }

    // Tính lại các giá trị phụ thuộc khi thuộc tính tài liệu thay đổi.
    /** Thực hiện changed update cho thành phần hiện tại. */
    @Override
    public void changedUpdate(DocumentEvent suKien) {
        capNhatTon();
        capNhatHoanTat();
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
