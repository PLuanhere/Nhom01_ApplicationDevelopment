package ui;

import dao.BanGiaoXeDAO;
import dao.BaoGiaDAO;
import dao.DuLieuGarageDAO;
import dao.HoaDonDAO;
import dao.DoiChieuChiPhiDAO;
import dao.KhachHangDAO;
import dao.XeDAO;
import dao.TaiKhoanDAO;
import dao.PhuTungDAO;
import dao.KyThuatDAO;
import dao.PhieuSuaChuaDAO;
import dao.PhieuTiepNhanDAO;
import dao.HoaDonDAO.PhieuChoThu;
import entity.HoaDon.HinhThucThanhToan;
import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;
import session.SessionManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import ui.component.AppHeaderPanel;
import ui.component.KyThuatNavigationPanel;
import ui.component.QuanLyNavigationPanel;
import ui.component.SidebarPanel;
import ui.component.ThuNganNavigationPanel;
import ui.component.TiepNhanNavigationPanel;
import ui.component.UiTheme;
import ui.component.WindowMouseHandler;
import ui.screen.kythuat.BaoCaoKiemTraPanel;
import ui.screen.kythuat.KhongGianKyThuatPanel;
import ui.screen.kythuat.PhieuSuaChuaPanel;
import ui.screen.kythuat.PhuTungSuDungPanel;
import ui.screen.kythuat.TrangChuKyThuatPanel;
import ui.screen.kythuat.TienDoSuaChuaPanel;
import ui.screen.kythuat.BaoHuHongPhatSinhPanel;
import ui.screen.kythuat.KiemTraHoanTatPanel;
import ui.screen.quanly.BaoCaoPanel;
import ui.screen.quanly.TrangChuQuanLyPanel;
import ui.screen.quanly.DichVuPanel;
import ui.screen.quanly.NhanVienPanel;
import ui.screen.quanly.PhuTungPanel;
import ui.screen.quanly.TaiKhoanPanel;
import ui.screen.thungan.CashPaymentConfirmPanel;
import ui.screen.thungan.BankTransferPaymentPanel;
import ui.screen.thungan.DoiChieuChiPhiPanel;
import ui.screen.thungan.PaidInvoicesPanel;
import ui.screen.thungan.PaidInvoiceDetailPanel;
import ui.screen.thungan.PaymentCenterPanel;
import ui.screen.thungan.PendingCollectionPanel;
import ui.screen.tiepnhan.BanGiaoXePanel;
import ui.screen.tiepnhan.BaoGiaPanel;
import ui.screen.tiepnhan.KhachHangPanel;
import ui.screen.tiepnhan.LenhSuaChuaTiepNhanPanel;
import ui.screen.tiepnhan.PhieuTiepNhanPanel;
import ui.screen.tiepnhan.TrangChuTiepNhanPanel;
import ui.screen.tiepnhan.XePanel;

/** Khung chung chứa header, sidebar và màn hình làm việc của các vai trò. */
public final class MainFrame extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;

    // Các vùng nội dung và biểu mẫu con của màn hình.
    private final SidebarPanel pnlSidebar = new SidebarPanel();
    private final JPanel pnlNoiDung = new JPanel(new BorderLayout());

    // Các nhãn hiển thị tiêu đề, số kết quả và phản hồi.
    private final JLabel lblTrangThai = new JLabel("Sẵn sàng");
    private final ActionListener khiDangXuat;

    private TaiKhoan taiKhoanDangNhap;
    private final DuLieuGarageDAO duLieuChung;

    private NhanVienPanel pnlNhanVien;
    private TrangChuQuanLyPanel pnlTrangChuQuanLy;
    private BaoCaoPanel pnlBaoCaoQuanLy;
    private QuanLyNavigationPanel pnlDieuHuongQuanLy;
    private TaiKhoanPanel pnlTaiKhoan;
    private DichVuPanel pnlDichVu;
    private PhuTungPanel pnlPhuTungQuanLy;
    private PhuTungDAO khoQuanLy;
    private String mucQuanLyDangMo = "Tổng quan điều hành";
    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();
    private Timer timerKiemTraPhien;

    private HoaDonDAO thuNganDAO;
    private ThuNganNavigationPanel pnlDieuHuongThuNgan;
    private PaymentCenterPanel pnlTrungTamThanhToan;
    private PendingCollectionPanel pnlPhieuChoThu;
    private PaidInvoicesPanel pnlHoaDonDaThu;
    private String mucThuNganDangMo = "Trung tâm thanh toán";

    private KhachHangDAO khachHangDAO;
    private XeDAO xeDAO;
    private PhieuTiepNhanDAO phieuTiepNhanDAO;
    private KhachHangPanel pnlKhachHang;
    private XePanel pnlXe;
    private PhieuTiepNhanPanel pnlTiepNhan;
    private BaoGiaPanel pnlBaoGia;
    private BaoGiaDAO baoGiaDAO;
    private PhieuSuaChuaDAO phieuSuaChuaDAO;
    private LenhSuaChuaTiepNhanPanel pnlLenhTiepNhan;
    private BanGiaoXePanel pnlBanGiao;
    private BanGiaoXeDAO banGiaoDAO;
    private TrangChuTiepNhanPanel pnlTrangChuTiepNhan;
    private TiepNhanNavigationPanel pnlDieuHuongTiepNhan;
    private String mucTiepNhanDangMo = "Tổng quan tiếp nhận";

    // MainFrame khởi tạo và giữ các panel của vai trò kỹ thuật.
    private TrangChuKyThuatPanel pnlTrangChuKyThuat;
    private KhongGianKyThuatPanel pnlKhongGianKyThuat;
    private BaoCaoKiemTraPanel pnlKiemTra;
    private PhieuSuaChuaPanel pnlSuaChua;
    private PhuTungSuDungPanel pnlPhuTung;
    private TienDoSuaChuaPanel pnlTienDoKyThuat;
    private BaoHuHongPhatSinhPanel pnlPhatSinhKyThuat;
    private KiemTraHoanTatPanel pnlHoanTatKyThuat;
    private KyThuatNavigationPanel pnlDieuHuongKyThuat;
    private String mucKyThuatDangMo = "TONG_QUAN";

    /** Khởi tạo khung ứng dụng, nguồn dữ liệu và màn hình theo vai trò đăng nhập. */
    public MainFrame(TaiKhoan taiKhoan, ActionListener khiDangXuat) {
        this(taiKhoan, khiDangXuat, null);
    }

    /** Khởi tạo khung ứng dụng, nguồn dữ liệu và màn hình theo vai trò đăng nhập. */
    public MainFrame(TaiKhoan taiKhoan, ActionListener khiDangXuat, DuLieuGarageDAO duLieu) {
        super("Garage Ô tô Đức Dũng");

        this.duLieuChung = duLieu == null ? new DuLieuGarageDAO() : duLieu;

        if (taiKhoan == null) {
            throw new NullPointerException("Cần tài khoản đã đăng nhập.");
        }

        taiKhoanDangNhap = taiKhoan;
        taiKhoanDangNhap.kiemTraPhien();
        if (!SessionManager.isDangDangNhap()) {
            SessionManager.batDauPhien(taiKhoan);
        }

        if (khiDangXuat == null) {
            throw new NullPointerException("Cần thao tác quay lại trang đăng nhập.");
        }

        this.khiDangXuat = khiDangXuat;

        timerKiemTraPhien = new Timer(3000, new ActionListener() {
            /** Kiểm tra trạng thái tài khoản định kỳ từ database. */
            @Override
            public void actionPerformed(ActionEvent event) {
                kiemTraPhienDangNhap();
            }
        });

        timerKiemTraPhien.start();

        setTitle(getTitle() + " — " + taiKhoan.getVaiTro().getTenHienThi());
        setUndecorated(false);
        setIconImage(UiTheme.createWindowIcon());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 600));
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        getContentPane().setBackground(UiTheme.APP_BACKGROUND);
        getRootPane().setBorder(BorderFactory.createLineBorder(UiTheme.APP_BORDER));

        WindowMouseHandler mouseHandler = new WindowMouseHandler(this);
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);

        pnlNoiDung.setBackground(UiTheme.APP_BACKGROUND);
        pnlNoiDung.setBorder(BorderFactory.createEmptyBorder(23, 26, 16, 26));

        add(pnlSidebar, BorderLayout.WEST);
        pnlSidebar.setRoleText(taiKhoan.getVaiTro().getTenHienThi());
        pnlSidebar.setAccount(taiKhoan);

        JPanel pnlLamViec = new JPanel(new BorderLayout());
        pnlLamViec.add(createHeader(), BorderLayout.NORTH);
        pnlLamViec.add(pnlNoiDung, BorderLayout.CENTER);
        pnlLamViec.add(createStatusBar(), BorderLayout.SOUTH);
        add(pnlLamViec, BorderLayout.CENTER);

        if (taiKhoan.getVaiTro() == VaiTro.QUAN_LY) {
            pnlDieuHuongQuanLy = new QuanLyNavigationPanel(new ActionListener() {
                /** Thực hiện thao tác tương ứng với nút, bộ lọc hoặc phím vừa chọn. */
                @Override
                public void actionPerformed(ActionEvent event) {
                    dieuHuongQuanLy(event.getActionCommand());
                }
            });

            setSidebarContent(pnlDieuHuongQuanLy);
            dieuHuongQuanLy("Tổng quan điều hành");
        } else if (taiKhoan.getVaiTro() == VaiTro.NHAN_VIEN_KY_THUAT) {
            khoiTaoTrangChuKyThuat();
        } else if (taiKhoan.getVaiTro() == VaiTro.NHAN_VIEN_TIEP_NHAN) {
            khoiTaoTrangChuTiepNhan();
        } else if (taiKhoan.getVaiTro() == VaiTro.THU_NGAN) {
            khoiTaoThuNgan();
        }
    }

    // Chuyển sự kiện kỹ thuật về luồng điều hướng chung của MainFrame.
    @Override
    public void actionPerformed(ActionEvent event) {
        dieuHuongKyThuat(event.getActionCommand());
    }

    /** Lấy danh tính đang sử dụng khung ứng dụng. */
    public TaiKhoan getTaiKhoanDangNhap() {
        if (taiKhoanDangNhap == null) {
            throw new IllegalStateException("Phiên đăng nhập đã kết thúc.");
        }

        taiKhoanDangNhap.kiemTraPhien();
        return taiKhoanDangNhap;
    }

    /** Thay màn hình đang hiển thị trong vùng nội dung chung. */
    public void setScreen(JPanel pnlManHinh) {
        if (pnlManHinh == null) {
            throw new NullPointerException("Màn hình không được null.");
        }

        pnlNoiDung.removeAll();
        pnlNoiDung.add(pnlManHinh, BorderLayout.CENTER);
        pnlNoiDung.revalidate();
        pnlNoiDung.repaint();
    }

    /** Gắn các mục điều hướng phù hợp vào sidebar. */
    public void setSidebarContent(JPanel pnlDieuHuong) {
        pnlSidebar.setNavigation(pnlDieuHuong);
    }

    /** Hiển thị nội dung mới trên thanh trạng thái. */
    public void setStatus(String thongBao) {
        if (thongBao == null) {
            throw new NullPointerException("Thông báo không được null.");
        }

        lblTrangThai.setText(thongBao);
    }

    /** Tạo header chung với menu, tìm kiếm và thông tin tài khoản. */
    private JPanel createHeader() {
        return new AppHeaderPanel(taiKhoanDangNhap, new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                MainFrame.this.dangXuat();
            }
        }, new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                dispatchEvent(new WindowEvent(MainFrame.this, WindowEvent.WINDOW_CLOSING));
            }
        }, taiKhoanDangNhap.getVaiTro() == VaiTro.QUAN_LY ? new ActionListener() {
            /** Mở báo cáo từ menu, vẫn kiểm tra form chưa lưu khi chuyển mục. */
            @Override
            public void actionPerformed(ActionEvent event) {
                dieuHuongQuanLy("Báo cáo và thống kê");
            }
        } : null);
    }

    /** Tạo thanh trạng thái ở cuối khung ứng dụng. */
    private JPanel createStatusBar() {
        JPanel pnlTrangThai = new JPanel(new BorderLayout());
        pnlTrangThai.setBackground(UiTheme.APP_BACKGROUND);
        pnlTrangThai.setBorder(BorderFactory.createEmptyBorder(10, 26, 16, 26));

        JLabel lblGarage = new JLabel("Garage Đức Dũng · Nội bộ nhân viên");
        lblGarage.setFont(UiTheme.APP_FONT.deriveFont(11.0f));
        lblGarage.setForeground(UiTheme.APP_MUTED);
        pnlTrangThai.add(lblGarage, BorderLayout.WEST);

        lblTrangThai.setFont(UiTheme.APP_FONT.deriveFont(11.0f));
        lblTrangThai.setForeground(UiTheme.APP_MUTED);
        lblTrangThai.setHorizontalAlignment(SwingConstants.RIGHT);
        pnlTrangThai.add(lblTrangThai, BorderLayout.CENTER);

        return pnlTrangThai;
    }

    /** Kiểm tra thay đổi chưa lưu, đóng phiên làm việc và mở lại đăng nhập. */
    private void dangXuat() {
        if (taiKhoanDangNhap != null && choPhepRoiNhanVien() && choPhepRoiTaiKhoan()
                && choPhepRoiDichVu() && choPhepRoiKho() && choPhepRoiTiepNhan()
                && choPhepRoiKyThuat()) {
            SessionManager.ketThucPhien();
            taiKhoanDAO.dangXuat(taiKhoanDangNhap);
            taiKhoanDangNhap = null;
            dispose();

            khiDangXuat.actionPerformed(
                    new ActionEvent(khiDangXuat, ActionEvent.ACTION_PERFORMED, "thaoTac"));
        }
    }

    /** Mở màn Quản lý được chọn sau khi kiểm tra dữ liệu chưa lưu. */
    private void dieuHuongQuanLy(String tenMuc) {
        boolean daCo = "Tổng quan điều hành".equals(tenMuc) || "Nhân viên".equals(tenMuc)
                || "Tài khoản & phân quyền".equals(tenMuc) || "Dịch vụ và bảng giá".equals(tenMuc)
                || "Phụ tùng".equals(tenMuc) || "Báo cáo và thống kê".equals(tenMuc);

        if (!daCo) {
            pnlDieuHuongQuanLy.chonMuc(mucQuanLyDangMo);
            setStatus(tenMuc + ": chức năng chưa triển khai.");
            return;
        }

        if (!tenMuc.equals(mucQuanLyDangMo)
                && (!choPhepRoiNhanVien() || !choPhepRoiTaiKhoan()
                        || !choPhepRoiDichVu() || !choPhepRoiKho())) {
            pnlDieuHuongQuanLy.chonMuc(mucQuanLyDangMo);
            return;
        }

        if ("Tổng quan điều hành".equals(tenMuc)) {
            if (pnlTrangChuQuanLy == null) {
                pnlTrangChuQuanLy = new TrangChuQuanLyPanel(
                        getTaiKhoanDangNhap(), duLieuChung.getBaoCaoDAO(), new ActionListener() {
                            /** Mở chức năng Quản lý từ đường tắt trên Trang chủ. */
                            @Override
                            public void actionPerformed(ActionEvent event) {
                                dieuHuongQuanLy(event.getActionCommand());
                            }
                        });
            } else {
                pnlTrangChuQuanLy.taiDuLieu();
            }

            setScreen(pnlTrangChuQuanLy);
            setStatus("Tổng quan điều hành");
        } else if ("Nhân viên".equals(tenMuc)) {
            moDanhSachNhanVien();
        } else if ("Tài khoản & phân quyền".equals(tenMuc)) {
            if (pnlTaiKhoan == null) {
                pnlTaiKhoan = new TaiKhoanPanel(getTaiKhoanDangNhap());
            }

            setScreen(pnlTaiKhoan);
            setStatus("Tài khoản & phân quyền");
        } else if ("Dịch vụ và bảng giá".equals(tenMuc)) {
            if (pnlDichVu == null) {
                pnlDichVu = new DichVuPanel(getTaiKhoanDangNhap(), duLieuChung.getDichVuDAO());
            }

            setScreen(pnlDichVu);
            setStatus("Danh sách dịch vụ và bảng giá");
        } else if ("Phụ tùng".equals(tenMuc)) {
            if (pnlPhuTungQuanLy == null) {
                pnlPhuTungQuanLy = new PhuTungPanel(getTaiKhoanDangNhap(), layKhoQuanLy());
            }

            pnlPhuTungQuanLy.capNhatDanhSach();
            setScreen(pnlPhuTungQuanLy);
            setStatus("Danh mục phụ tùng và tồn kho");
        } else if ("Báo cáo và thống kê".equals(tenMuc)) {
            if (pnlBaoCaoQuanLy == null) {
                pnlBaoCaoQuanLy = new BaoCaoPanel(getTaiKhoanDangNhap(), duLieuChung.getBaoCaoDAO());
            } else {
                pnlBaoCaoQuanLy.taiDuLieu();
            }

            setScreen(pnlBaoCaoQuanLy);
            setStatus("Báo cáo và thống kê");
        }

        pnlDieuHuongQuanLy.chonMuc(tenMuc);
        mucQuanLyDangMo = tenMuc;
    }

    /** Tạo hoặc mở phân hệ Nhân viên trong vùng nội dung chung. */
    private void moDanhSachNhanVien() {
        if (pnlNhanVien == null) {
            pnlNhanVien = new NhanVienPanel(getTaiKhoanDangNhap());
        }

        setScreen(pnlNhanVien);
        setStatus("Danh sách nhân viên");
    }

    /** Kiểm tra form Nhân viên trước khi rời phân hệ hoặc đóng ứng dụng. */
    private boolean choPhepRoiNhanVien() {
        return pnlNhanVien == null || !pnlNoiDung.isAncestorOf(pnlNhanVien)
                || pnlNhanVien.choPhepRoiTrang();
    }

    /** Kiểm tra form Tài khoản trước khi rời phân hệ hoặc đóng ứng dụng. */
    private boolean choPhepRoiTaiKhoan() {
        return pnlTaiKhoan == null || !pnlNoiDung.isAncestorOf(pnlTaiKhoan)
                || pnlTaiKhoan.choPhepRoiTrang();
    }

    /** Kiểm tra form Dịch vụ trước khi rời phân hệ hoặc đóng ứng dụng. */
    private boolean choPhepRoiDichVu() {
        return pnlDichVu == null || !pnlNoiDung.isAncestorOf(pnlDichVu)
                || pnlDichVu.choPhepRoiTrang();
    }

    /** Lấy DAO kho dùng chung với các vai trò, hoặc khởi tạo nguồn cho cửa sổ độc lập. */
    private PhuTungDAO layKhoQuanLy() {
        if (duLieuChung != null) {
            return duLieuChung.getKhoDAO();
        }

        if (khoQuanLy == null) {
            khoQuanLy = new PhuTungDAO();
        }

        return khoQuanLy;
    }

    /** Kiểm tra form Phụ tùng hoặc nhập kho trước khi rời phân hệ. */
    private boolean choPhepRoiKho() {
        return pnlPhuTungQuanLy == null || !pnlNoiDung.isAncestorOf(pnlPhuTungQuanLy)
                || pnlPhuTungQuanLy.choPhepRoiTrang();
    }

    /** Đóng khung và yêu cầu đăng nhập lại khi phiên bị thu hồi. */
    private void kiemTraPhienDangNhap() {
        try {
            if (taiKhoanDangNhap != null && !taiKhoanDAO.phienConHopLe(taiKhoanDangNhap)) {
                taiKhoanDangNhap = null;
                dispose();

                khiDangXuat.actionPerformed(
                        new ActionEvent(khiDangXuat, ActionEvent.ACTION_PERFORMED, "thaoTac"));
            }
        } catch (IllegalStateException ex) {
            lblTrangThai.setText("Mất kết nối database; đang thử kết nối lại...");
        }
    }

    /** Đóng cửa sổ và dọn dữ liệu hoặc theo dõi không còn sử dụng. */
    @Override
    public void dispose() {
        if (timerKiemTraPhien != null) {
            timerKiemTraPhien.stop();
        }

        super.dispose();
    }

    /** Kiểm tra trạng thái và dữ liệu chưa lưu trước khi đóng cửa sổ. */
    @Override
    protected void processWindowEvent(WindowEvent event) {
        if (event.getID() == WindowEvent.WINDOW_CLOSING
                && (!choPhepRoiNhanVien() || !choPhepRoiTaiKhoan()
                        || !choPhepRoiDichVu() || !choPhepRoiKho()
                        || !choPhepRoiTiepNhan() || !choPhepRoiKyThuat())) {
            return;
        }

        super.processWindowEvent(event);
    }

    /** Chuẩn bị nguồn dữ liệu và màn hình ban đầu cho vai trò Tiếp nhận. */
    private void khoiTaoTrangChuTiepNhan() {
        khachHangDAO = duLieuChung.getKhachHangDAO();
        xeDAO = duLieuChung.getXeDAO();
        phieuTiepNhanDAO = duLieuChung.getTiepNhanDAO();
        baoGiaDAO = duLieuChung.getBaoGiaDAO();
        phieuSuaChuaDAO = duLieuChung.getSuaChuaDAO();
        banGiaoDAO = duLieuChung.getBanGiaoDAO();

        pnlDieuHuongTiepNhan = new TiepNhanNavigationPanel(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                String giaTri1 = (String) event.getSource();
                MainFrame.this.dieuHuongTiepNhan(giaTri1);
            }
        });

        pnlTrangChuTiepNhan = new TrangChuTiepNhanPanel(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                String giaTri1 = (String) event.getSource();
                MainFrame.this.dieuHuongTiepNhan(giaTri1);
            }
        }, duLieuChung == null ? null : duLieuChung.getTrangChuDAO(),
                MainFrame.this.getTaiKhoanDangNhap(), new ActionListener() {
                    /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        String giaTri1 = (String) event.getSource();
                        MainFrame.this.moBanGiaoTuTongQuan(giaTri1);
                    }
                });

        setSidebarContent(pnlDieuHuongTiepNhan);
        dieuHuongTiepNhan("Tổng quan tiếp nhận");
    }

    /** Mở màn của Tiếp nhận theo mục sidebar được chọn. */
    private void dieuHuongTiepNhan(String muc) {
        if (getTaiKhoanDangNhap().getVaiTro() != VaiTro.NHAN_VIEN_TIEP_NHAN) {
            throw new IllegalStateException("Chỉ dành cho nhân viên tiếp nhận.");
        }

        boolean daTrienKhai = "Tổng quan tiếp nhận".equals(muc) || "Khách hàng".equals(muc)
                || "Phương tiện".equals(muc) || "Phiếu tiếp nhận".equals(muc)
                || "Báo giá".equals(muc) || "Lệnh sửa chữa".equals(muc) || "Bàn giao xe".equals(muc);

        if (daTrienKhai && !muc.equals(mucTiepNhanDangMo)) {
            if (!choPhepRoiTiepNhan()) {
                pnlDieuHuongTiepNhan.chonMuc(mucTiepNhanDangMo);
                return;
            }

            if ("Khách hàng".equals(mucTiepNhanDangMo) && pnlKhachHang != null) {
                pnlKhachHang.moDanhSach();
            }
            if ("Phương tiện".equals(mucTiepNhanDangMo) && pnlXe != null) {
                pnlXe.moDanhSach();
            }
            if ("Phiếu tiếp nhận".equals(mucTiepNhanDangMo) && pnlTiepNhan != null) {
                pnlTiepNhan.moDanhSach();
            }
            if ("Bàn giao xe".equals(mucTiepNhanDangMo) && pnlBanGiao != null) {
                pnlBanGiao.moDanhSach();
            }
            if ("Báo giá".equals(mucTiepNhanDangMo) && pnlBaoGia != null) {
                pnlBaoGia.moDanhSach();
            }
            if ("Lệnh sửa chữa".equals(mucTiepNhanDangMo) && pnlLenhTiepNhan != null) {
                pnlLenhTiepNhan.moDanhSach();
            }
        }

        if ("Khách hàng".equals(muc)) {
            if (pnlKhachHang == null) {
                pnlKhachHang = new KhachHangPanel(
                        khachHangDAO, MainFrame.this.getTaiKhoanDangNhap(), xeDAO);
            }

            if (!"Khách hàng".equals(mucTiepNhanDangMo)) {
                pnlKhachHang.moDanhSach();
            }

            setScreen(pnlKhachHang);
            mucTiepNhanDangMo = muc;
            pnlDieuHuongTiepNhan.chonMuc(muc);
            setStatus("Khách hàng · Dữ liệu trong bộ nhớ, chưa lưu lâu dài");
        } else if ("Phiếu tiếp nhận".equals(muc)) {
            if (pnlTiepNhan == null) {
                pnlTiepNhan = new PhieuTiepNhanPanel(
                        khachHangDAO, xeDAO, phieuTiepNhanDAO, MainFrame.this.getTaiKhoanDangNhap(),
                        duLieuChung == null ? null : duLieuChung.getTrangChuDAO());
            } else if (!"Phiếu tiếp nhận".equals(mucTiepNhanDangMo)) {
                pnlTiepNhan.moDanhSach();
            }

            setScreen(pnlTiepNhan);
            mucTiepNhanDangMo = muc;
            pnlDieuHuongTiepNhan.chonMuc(muc);
            setStatus("Tiếp nhận · Phiếu lưu trong bộ nhớ của phiên");
        } else if ("Báo giá".equals(muc)) {
            if (pnlBaoGia == null) {
                pnlBaoGia = new BaoGiaPanel(
                        baoGiaDAO, MainFrame.this.getTaiKhoanDangNhap(), duLieuChung.getPhuTungDAO());
            }

            setScreen(pnlBaoGia);
            mucTiepNhanDangMo = muc;
            pnlDieuHuongTiepNhan.chonMuc(muc);
            setStatus("Báo giá · Lưu trong bộ nhớ của phiên");
        } else if ("Phương tiện".equals(muc)) {
            if (pnlXe == null) {
                pnlXe = new XePanel(
                        xeDAO, khachHangDAO, MainFrame.this.getTaiKhoanDangNhap(), phieuTiepNhanDAO,
                        duLieuChung == null ? null : duLieuChung.getTrangChuDAO());
            }

            if (!"Phương tiện".equals(mucTiepNhanDangMo)) {
                pnlXe.moDanhSach();
            }

            setScreen(pnlXe);
            mucTiepNhanDangMo = muc;
            pnlDieuHuongTiepNhan.chonMuc(muc);
            setStatus("Phương tiện · Lịch sử từ nguồn dữ liệu trong bộ nhớ");
        } else if ("Tổng quan tiếp nhận".equals(muc)) {
            setScreen(pnlTrangChuTiepNhan);
            mucTiepNhanDangMo = muc;
            pnlDieuHuongTiepNhan.chonMuc(muc);
            pnlTrangChuTiepNhan.capNhatDuLieu();
            setStatus("Tổng quan tiếp nhận · Dữ liệu trong bộ nhớ của lần chạy ứng dụng");
        } else if ("Bàn giao xe".equals(muc)) {
            if (pnlBanGiao == null) {
                pnlBanGiao = new BanGiaoXePanel(banGiaoDAO, MainFrame.this.getTaiKhoanDangNhap());
            } else if (!"Bàn giao xe".equals(mucTiepNhanDangMo)) {
                pnlBanGiao.moDanhSach();
            }

            setScreen(pnlBanGiao);
            mucTiepNhanDangMo = muc;
            pnlDieuHuongTiepNhan.chonMuc(muc);
            setStatus("Bàn giao xe · Đối chiếu căn cứ thanh toán/phê duyệt từ nguồn phụ trách");
        } else if ("Lệnh sửa chữa".equals(muc)) {
            if (pnlLenhTiepNhan == null) {
                pnlLenhTiepNhan = new LenhSuaChuaTiepNhanPanel(
                        phieuSuaChuaDAO, MainFrame.this.getTaiKhoanDangNhap());
            }

            setScreen(pnlLenhTiepNhan);
            setStatus("Lệnh sửa chữa · Lưu trong bộ nhớ của phiên");
            mucTiepNhanDangMo = muc;
            pnlDieuHuongTiepNhan.chonMuc(muc);
        } else {
            pnlDieuHuongTiepNhan.chonMuc(mucTiepNhanDangMo);
            setStatus(muc + ": chức năng chưa triển khai.");
        }
    }

    /** Mở màn bàn giao cho hồ sơ được chọn từ tổng quan Tiếp nhận. */
    private void moBanGiaoTuTongQuan(String maLenh) {
        dieuHuongTiepNhan("Bàn giao xe");

        if (!"Bàn giao xe".equals(mucTiepNhanDangMo)) {
            return;
        }

        if (maLenh == null) {
            pnlBanGiao.moDanhSachChoGiao();
        } else {
            pnlBanGiao.moTheoLenh(maLenh);
        }
    }

    /** Kiểm tra dữ liệu chưa lưu trong các màn Tiếp nhận trước khi điều hướng. */
    private boolean choPhepRoiTiepNhan() {
        if ("Bàn giao xe".equals(mucTiepNhanDangMo) && pnlBanGiao != null) {
            return pnlBanGiao.choPhepRoiManHinh();
        }
        if ("Lệnh sửa chữa".equals(mucTiepNhanDangMo) && pnlLenhTiepNhan != null) {
            return pnlLenhTiepNhan.choPhepRoiManHinh();
        }
        if ("Báo giá".equals(mucTiepNhanDangMo) && pnlBaoGia != null) {
            return pnlBaoGia.choPhepRoiManHinh();
        }
        if ("Khách hàng".equals(mucTiepNhanDangMo) && pnlKhachHang != null) {
            return pnlKhachHang.choPhepRoiManHinh();
        }
        if ("Phương tiện".equals(mucTiepNhanDangMo) && pnlXe != null) {
            return pnlXe.choPhepRoiManHinh();
        }
        if ("Phiếu tiếp nhận".equals(mucTiepNhanDangMo) && pnlTiepNhan != null) {
            return pnlTiepNhan.choPhepRoiManHinh();
        }

        return true;
    }

    // Khởi tạo một bộ panel kỹ thuật và gắn menu vào SidebarPanel của khung chung.
    private void khoiTaoTrangChuKyThuat() {
        kiemTraVaiTroKyThuat();

        KyThuatDAO dao = new KyThuatDAO(
                getTaiKhoanDangNhap(), duLieuChung.getSuaChuaDAO(),
                duLieuChung.getKiemTraDAO(), duLieuChung.getPhuTungDAO());

        pnlKhongGianKyThuat = new KhongGianKyThuatPanel(
                this, getTaiKhoanDangNhap(), new ActionListener() {
                    // Đăng xuất qua MainFrame để kiểm tra dữ liệu chưa lưu và đóng phiên.
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        dangXuat();
                    }
                }, dao);

        pnlTrangChuKyThuat = new TrangChuKyThuatPanel(pnlKhongGianKyThuat);
        pnlSuaChua = new PhieuSuaChuaPanel(pnlKhongGianKyThuat);
        pnlKiemTra = new BaoCaoKiemTraPanel(pnlKhongGianKyThuat);
        pnlTienDoKyThuat = new TienDoSuaChuaPanel(pnlKhongGianKyThuat);
        pnlPhuTung = new PhuTungSuDungPanel(pnlKhongGianKyThuat);
        pnlPhatSinhKyThuat = new BaoHuHongPhatSinhPanel(pnlKhongGianKyThuat);
        pnlHoanTatKyThuat = new KiemTraHoanTatPanel(pnlKhongGianKyThuat);

        pnlKhongGianKyThuat.ganCacTrang(
                pnlTrangChuKyThuat, pnlSuaChua, pnlKiemTra, pnlTienDoKyThuat,
                pnlPhuTung, pnlPhatSinhKyThuat, pnlHoanTatKyThuat);

        pnlDieuHuongKyThuat = new KyThuatNavigationPanel(this);
        setSidebarContent(pnlDieuHuongKyThuat);
        dieuHuongKyThuat("TONG_QUAN");
    }

    // Kiểm tra role trước mọi đường mở trang kỹ thuật, kể cả nút đường tắt.
    private void kiemTraVaiTroKyThuat() {
        if (getTaiKhoanDangNhap().getVaiTro() != VaiTro.NHAN_VIEN_KY_THUAT) {
            throw new SecurityException("Chỉ dành cho kỹ thuật viên.");
        }
    }

    // Kiểm tra dữ liệu chưa lưu và mở đúng panel kỹ thuật đã được khởi tạo.
    public void dieuHuongKyThuat(String muc) {
        kiemTraVaiTroKyThuat();

        if (!KyThuatNavigationPanel.coMuc(muc)) {
            setStatus("Không có mục kỹ thuật: " + muc);
            return;
        }

        if (!choPhepRoiKyThuat()) {
            pnlDieuHuongKyThuat.chonMuc(mucKyThuatDangMo);
            return;
        }

        if (!"TONG_QUAN".equals(muc) && !"PHAN_CONG".equals(muc) && !"KIEM_TRA".equals(muc)
                && pnlKhongGianKyThuat.getMaLenh() == null) {
            muc = "PHAN_CONG";
        }

        pnlKhongGianKyThuat.batDauTrang(muc);

        switch (muc) {
        case "TONG_QUAN":
            pnlTrangChuKyThuat.hienTongQuan();
            break;
        case "PHAN_CONG":
            pnlSuaChua.hienDanhSachLenh("");
            break;
        case "KIEM_TRA":
            pnlKiemTra.hienDanhSachBaoCao();
            break;
        case "TIEN_DO":
            pnlTienDoKyThuat.hienTienDo();
            break;
        case "PHU_TUNG":
            pnlPhuTung.hienPhuTung();
            break;
        case "PHAT_SINH":
            pnlPhatSinhKyThuat.hienPhatSinh();
            break;
        case "HOAN_TAT":
            pnlHoanTatKyThuat.hienHoanTat();
            break;
        default:
            throw new IllegalArgumentException("Mục kỹ thuật không hợp lệ.");
        }
    }

    // Đồng bộ sidebar và thanh trạng thái khi mở trang hoặc biểu mẫu kỹ thuật.
    public void capNhatMucKyThuat(String muc) {
        kiemTraVaiTroKyThuat();
        pnlDieuHuongKyThuat.chonMuc(muc);
        mucKyThuatDangMo = muc;
        setStatus(KyThuatNavigationPanel.layNhan(muc));
    }

    // Chỉ cho hiển thị các panel thuộc bộ kỹ thuật của phiên hiện tại.
    public void hienTrangKyThuat(JPanel trang) {
        kiemTraVaiTroKyThuat();

        if (trang != pnlTrangChuKyThuat && trang != pnlSuaChua && trang != pnlKiemTra
                && trang != pnlTienDoKyThuat && trang != pnlPhuTung
                && trang != pnlPhatSinhKyThuat && trang != pnlHoanTatKyThuat) {
            throw new IllegalArgumentException("Panel không thuộc phiên kỹ thuật hiện tại.");
        }

        setScreen(trang);
    }

    // Kiểm tra nội dung chưa lưu trước khi rời trang kỹ thuật hoặc đóng phiên.
    private boolean choPhepRoiKyThuat() {
        return pnlKhongGianKyThuat == null || pnlKhongGianKyThuat.choPhepRoiTrang();
    }

    /** Chuẩn bị nguồn dữ liệu và các màn hình cho vai trò Thu ngân. */
    private void khoiTaoThuNgan() {
        thuNganDAO = duLieuChung.getHoaDonDAO();

        pnlDieuHuongThuNgan = new ThuNganNavigationPanel(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                String giaTri1 = (String) event.getSource();
                MainFrame.this.dieuHuongThuNgan(giaTri1);
            }
        });

        setSidebarContent(pnlDieuHuongThuNgan);
        dieuHuongThuNgan("Trung tâm thanh toán");
    }

    /** Mở màn Thu ngân theo mục sidebar được chọn; tái dùng panel như các vai trò khác. */
    private void dieuHuongThuNgan(String muc) {
        getTaiKhoanDangNhap().kiemTraQuyenThuNgan();
        pnlDieuHuongThuNgan.chonMuc(muc);

        if ("Trung tâm thanh toán".equals(muc)) {
            if (pnlTrungTamThanhToan == null) {
                pnlTrungTamThanhToan = taoTrungTamThanhToan();
            }

            setScreen(pnlTrungTamThanhToan);
            mucThuNganDangMo = muc;
            setStatus("Trung tâm thanh toán");
        } else if ("Phiếu chờ thu".equals(muc)) {
            if (pnlPhieuChoThu == null) {
                pnlPhieuChoThu = new PendingCollectionPanel(thuNganDAO, getTaiKhoanDangNhap(), this);
            } else if (!"Phiếu chờ thu".equals(mucThuNganDangMo)
                    && !"Đối chiếu chi phí".equals(mucThuNganDangMo)) {
                pnlPhieuChoThu = new PendingCollectionPanel(thuNganDAO, getTaiKhoanDangNhap(), this);
            }

            setScreen(pnlPhieuChoThu);
            mucThuNganDangMo = muc;
            setStatus("Phiếu chờ thu");
        } else if ("Đối chiếu chi phí".equals(muc)) {
            if (pnlPhieuChoThu == null) {
                pnlPhieuChoThu = new PendingCollectionPanel(thuNganDAO, getTaiKhoanDangNhap(), this);
            }

            setScreen(pnlPhieuChoThu);
            mucThuNganDangMo = "Phiếu chờ thu";
            setStatus("Đối chiếu chi phí · Chọn phiếu để kiểm tra và lập hóa đơn");
            pnlDieuHuongThuNgan.chonMuc("Phiếu chờ thu");
        } else if ("Hoá đơn đã thanh toán".equals(muc)) {
            if (pnlHoaDonDaThu == null) {
                pnlHoaDonDaThu = new PaidInvoicesPanel(
                        thuNganDAO, getTaiKhoanDangNhap(), new ActionListener() {
                            /** Xem/in đúng hóa đơn người dùng chọn trong bảng. */
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                moChiTietHoaDon(e.getActionCommand());
                            }
                        });
            } else if (!"Hoá đơn đã thanh toán".equals(mucThuNganDangMo)) {
                pnlHoaDonDaThu = new PaidInvoicesPanel(
                        thuNganDAO, getTaiKhoanDangNhap(), new ActionListener() {
                            /** Xem/in đúng hóa đơn người dùng chọn trong bảng. */
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                moChiTietHoaDon(e.getActionCommand());
                            }
                        });
            }

            setScreen(pnlHoaDonDaThu);
            mucThuNganDangMo = muc;
            setStatus("Hóa đơn đã thanh toán");
        } else {
            setStatus(muc + ": chức năng chưa triển khai.");
        }
    }

    /** Trung tâm thanh toán dựng một lần; các callback trỏ về MainFrame nên không cần tạo lại. */
    private PaymentCenterPanel taoTrungTamThanhToan() {
        PaymentCenterPanel.Navigation nav = new PaymentCenterPanel.Navigation();

        nav.costReconciliation = new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                if (thuNganDAO.timPhieuTheoMa(
                        getTaiKhoanDangNhap(), event.getActionCommand()) != null) {
                    moDoiChieuChiPhi(event.getActionCommand());
                } else {
                    dieuHuongThuNgan("Đối chiếu chi phí");
                }
            }
        };

        nav.pendingInvoices = new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                dieuHuongThuNgan("Phiếu chờ thu");
            }
        };

        nav.paidDocuments = new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                dieuHuongThuNgan("Hoá đơn đã thanh toán");
            }
        };

        nav.documentSearch = new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                dieuHuongThuNgan("Hoá đơn đã thanh toán");
            }
        };

        nav.cashPayment = new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                MainFrame.this.moThanhToanTienMat();
            }
        };

        nav.bankTransfer = new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                dieuHuongThuNgan("Phiếu chờ thu");
                setStatus("Chọn phiếu để đối chiếu và thanh toán chuyển khoản.");
            }
        };

        nav.viewInvoice = new ActionListener() {
            /** Mở đúng hóa đơn được bấm trên Trang chủ Thu ngân. */
            @Override
            public void actionPerformed(ActionEvent e) {
                moChiTietHoaDon(e.getActionCommand());
            }
        };

        return new PaymentCenterPanel(thuNganDAO, getTaiKhoanDangNhap(), nav);
    }

    /** Đối chiếu đúng mã được chọn; không tự thay bằng phiếu đầu danh sách. */
    public void moDoiChieuChiPhi(String maPhieu) {
        if (maPhieu == null || thuNganDAO.timPhieuTheoMa(getTaiKhoanDangNhap(), maPhieu) == null) {
            setStatus("Phiếu không còn chờ thu. Hãy làm mới danh sách.");
            return;
        }

        pnlDieuHuongThuNgan.chonMuc("Phiếu chờ thu");

        setScreen(new DoiChieuChiPhiPanel(
                new DoiChieuChiPhiDAO(thuNganDAO, getTaiKhoanDangNhap()), maPhieu,
                new ActionListener() {
                    /** Mở đúng bước xác nhận theo hình thức đã chọn. */
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        if (event.getSource() == HinhThucThanhToan.TIEN_MAT) {
                            moThanhToanTienMat(maPhieu);
                        } else {
                            moThanhToanChuyenKhoan(maPhieu);
                        }
                    }
                }, new ActionListener() {
                    /** Trở lại danh sách phiếu chờ thu. */
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        dieuHuongThuNgan("Phiếu chờ thu");
                    }
                }));

        setStatus("Đối chiếu chi phí · " + maPhieu);
    }

    /** Mở chuyển khoản cho phiếu đã đối chiếu và chưa được thu. */
    private void moThanhToanChuyenKhoan(String maPhieu) {
        PhieuChoThu phieu = thuNganDAO.timPhieuTheoMa(getTaiKhoanDangNhap(), maPhieu);

        if (phieu == null) {
            setStatus("Phiếu không còn chờ thu.");
            return;
        }

        setScreen(new BankTransferPaymentPanel(
                thuNganDAO, phieu, getTaiKhoanDangNhap(),
                () -> moDoiChieuChiPhi(maPhieu),
                maHoaDon -> moChiTietHoaDon(maHoaDon)));

        setStatus("Thanh toán chuyển khoản / QR · " + maPhieu);
    }

    /** Mở màn xác nhận thu tiền mặt cho phiếu được chọn. */
    private void moThanhToanTienMat() {
        List<PhieuChoThu> danhSach = thuNganDAO.layPhieuChoThu(getTaiKhoanDangNhap());

        if (danhSach.isEmpty()) {
            setStatus("Không có phiếu chờ thu tiền mặt.");
            return;
        }

        dieuHuongThuNgan("Phiếu chờ thu");
    }

    /** Đọc lại phiếu được chọn trước khi mở xác nhận thanh toán. */
    public void moThanhToanTienMat(String maPhieu) {
        PhieuChoThu phieu = thuNganDAO.timPhieuTheoMa(getTaiKhoanDangNhap(), maPhieu);

        if (phieu == null) {
            setStatus("Phiếu không còn chờ thu. Hãy làm mới danh sách.");
            return;
        }

        setScreen(new CashPaymentConfirmPanel(
                thuNganDAO, phieu, taiKhoanDangNhap,
                () -> dieuHuongThuNgan("Trung tâm thanh toán"),
                maHoaDon -> moChiTietHoaDon(maHoaDon)));

        setStatus("Thanh toán tiền mặt · " + phieu.getMaPhieu());
    }

    /** Mở chi tiết hóa đơn hợp lệ trong nguồn chung, không tự lấy chứng từ khác. */
    private void moChiTietHoaDon(String ma) {
        if (thuNganDAO.timHoaDonTheoMa(getTaiKhoanDangNhap(), ma) == null) {
            setStatus("Không tìm thấy hóa đơn " + ma);
            return;
        }

        pnlDieuHuongThuNgan.chonMuc("Hoá đơn đã thanh toán");

        setScreen(new PaidInvoiceDetailPanel(
                thuNganDAO, getTaiKhoanDangNhap(), ma,
                suKien -> dieuHuongThuNgan("Hoá đơn đã thanh toán")));

        setStatus("Chi tiết hóa đơn · " + ma);
    }
}