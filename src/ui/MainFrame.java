package ui;

import dao.bangiao.BanGiaoXeDAO;
import dao.baocao.TrangChuKyThuatDAO;
import dao.baogia.BaoGiaDAO;
import dao.chung.DuLieuGarageDAO;
import dao.hoadon.HoaDonDAO;
import dao.khachhang.KhachHangDAO;
import dao.khachhang.XeDAO;
import dao.kiemtra.BaoCaoKiemTraDAO;
import dao.nhansu.TaiKhoanDAO;
import dao.phutung.PhuTungDAO;
import dao.phutung.PhuTungSuDungDAO;
import dao.suachua.KyThuatDAO;
import dao.suachua.PhieuSuaChuaDAO;
import dao.tiepnhan.PhieuTiepNhanDAO;
import entity.hoadon.PhieuChoThu;
import entity.nhansu.TaiKhoan;
import entity.nhansu.VaiTro;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import ui.component.AppHeaderPanel;
import ui.component.KyThuatUi;
import ui.component.QuanLyNavigationPanel;
import ui.component.SidebarPanel;
import ui.component.ThuNganNavigationPanel;
import ui.component.TiepNhanNavigationPanel;
import ui.component.UiTheme;
import ui.component.WindowMouseHandler;
import ui.component.WindowTitlePanel;
import ui.screen.kythuat.BaoCaoKiemTraPanel;
import ui.screen.kythuat.KhongGianKyThuatPanel;
import ui.screen.kythuat.PhieuSuaChuaPanel;
import ui.screen.kythuat.PhuTungSuDungPanel;
import ui.screen.kythuat.TrangChuKyThuatPanel;
import ui.screen.quanly.BaoCaoPanel;
import ui.screen.quanly.TrangChuQuanLyPanel;
import ui.screen.quanly.DichVuPanel;
import ui.screen.quanly.NhanVienPanel;
import ui.screen.quanly.PhuTungPanel;
import ui.screen.quanly.TaiKhoanPanel;
import ui.screen.thungan.CashPaymentConfirmPanel;
import ui.screen.thungan.CashReceiptPanel;
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
    private final TaiKhoanDAO.TheoDoiTaiKhoan theoDoiTaiKhoan = new TaiKhoanDAO.TheoDoiTaiKhoan() {
        /** Kiểm tra và đóng phiên khi tài khoản bị thay đổi. */
        @Override
        public void taiKhoanThayDoi() {
            kiemTraPhienDangNhap();
        }
    };

    private HoaDonDAO thuNganDAO;
    private ThuNganNavigationPanel pnlDieuHuongThuNgan;
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

    private TrangChuKyThuatPanel pnlTrangChuKyThuat;
    private KhongGianKyThuatPanel pnlKhongGianKyThuat;
    private BaoCaoKiemTraPanel pnlKiemTra;
    private PhieuSuaChuaPanel pnlSuaChua;
    private PhuTungSuDungPanel pnlPhuTung;

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
        if (khiDangXuat == null) {
            throw new NullPointerException("Cần thao tác quay lại trang đăng nhập.");
        }
        this.khiDangXuat = khiDangXuat;
        taiKhoanDAO.themTheoDoi(theoDoiTaiKhoan);

        setTitle(getTitle() + " — " + taiKhoan.getVaiTro().getTenHienThi());
        setUndecorated(true);
        setIconImage(UiTheme.createWindowIcon());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 600));
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        getContentPane().setBackground(UiTheme.APP_BACKGROUND);
        getRootPane().setBorder(BorderFactory.createLineBorder(UiTheme.APP_BORDER));

        if (taiKhoan.getVaiTro() == VaiTro.NHAN_VIEN_KY_THUAT) {
            add(new WindowTitlePanel(this), BorderLayout.NORTH);
            KyThuatDAO nguonKyThuat = new KyThuatDAO(MainFrame.this.getTaiKhoanDangNhap(), duLieuChung.getSuaChuaDAO(),
                    duLieuChung.getKiemTraDAO(), duLieuChung.getPhuTungDAO());
            pnlKhongGianKyThuat = new KhongGianKyThuatPanel(this, taiKhoan, new ActionListener() {
                /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
                @Override
                public void actionPerformed(ActionEvent event) {
                    MainFrame.this.dangXuat();
                }
            }, nguonKyThuat);
            add(pnlKhongGianKyThuat, BorderLayout.CENTER);
            return;

        }

        add(new WindowTitlePanel(this), BorderLayout.NORTH);

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

    /** Thực hiện thao tác tương ứng với nút, bộ lọc hoặc phím vừa chọn. */
    @Override
    public void actionPerformed(ActionEvent event) {
        String tenManHinh = event.getActionCommand();

        if ("Trang chủ".equals(tenManHinh) && pnlTrangChuKyThuat != null) {
            setScreen(pnlTrangChuKyThuat);
            setStatus("Dashboard kỹ thuật • Dữ liệu mẫu theo Figma 05/10/2026");
        } else if ("Báo cáo kiểm tra".equals(tenManHinh)) {
            if (pnlKiemTra == null) {
                pnlKiemTra = new BaoCaoKiemTraPanel(this, getTaiKhoanDangNhap(),
                        duLieuChung == null ? new BaoCaoKiemTraDAO() : duLieuChung.getKiemTraDAO());
            }

            setScreen(pnlKiemTra);
            pnlKiemTra.actionPerformed(new ActionEvent(pnlKiemTra, ActionEvent.ACTION_PERFORMED, "DANH_SACH"));
            setStatus("Báo cáo kiểm tra · Dữ liệu mẫu trong bộ nhớ");
        } else if ("Lệnh sửa chữa".equals(tenManHinh) || "Công việc được giao".equals(tenManHinh)
                || "Tiến độ sửa chữa".equals(tenManHinh)) {
            moDanhSachSuaChua();
        } else if ("Phụ tùng sử dụng".equals(tenManHinh)) {
            moDanhSachPhuTung();
        } else {
            dieuHuongKyThuat(tenManHinh);
        }
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
        if (taiKhoanDangNhap != null && choPhepRoiNhanVien() && choPhepRoiTaiKhoan() && choPhepRoiDichVu()
                && choPhepRoiKho() && choPhepRoiTiepNhan()) {
            taiKhoanDAO.dangXuat(taiKhoanDangNhap);
            taiKhoanDangNhap = null;
            dispose();
            khiDangXuat.actionPerformed(new ActionEvent(khiDangXuat, ActionEvent.ACTION_PERFORMED, "thaoTac"));
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
                && (!choPhepRoiNhanVien() || !choPhepRoiTaiKhoan() || !choPhepRoiDichVu() || !choPhepRoiKho())) {
            pnlDieuHuongQuanLy.chonMuc(mucQuanLyDangMo);
            return;

        }
        if ("Tổng quan điều hành".equals(tenMuc)) {
            if (pnlTrangChuQuanLy == null) {
                pnlTrangChuQuanLy = new TrangChuQuanLyPanel(getTaiKhoanDangNhap(), duLieuChung.getBaoCaoDAO(),
                        new ActionListener() {
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
        return pnlNhanVien == null || !pnlNoiDung.isAncestorOf(pnlNhanVien) || pnlNhanVien.choPhepRoiTrang();
    }

    /** Kiểm tra form Tài khoản trước khi rời phân hệ hoặc đóng ứng dụng. */
    private boolean choPhepRoiTaiKhoan() {
        return pnlTaiKhoan == null || !pnlNoiDung.isAncestorOf(pnlTaiKhoan) || pnlTaiKhoan.choPhepRoiTrang();
    }

    /** Kiểm tra form Dịch vụ trước khi rời phân hệ hoặc đóng ứng dụng. */
    private boolean choPhepRoiDichVu() {
        return pnlDichVu == null || !pnlNoiDung.isAncestorOf(pnlDichVu) || pnlDichVu.choPhepRoiTrang();
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
        if (taiKhoanDangNhap != null && !taiKhoanDangNhap.isPhienHopLe()) {
            taiKhoanDangNhap = null;
            dispose();
            khiDangXuat.actionPerformed(new ActionEvent(khiDangXuat, ActionEvent.ACTION_PERFORMED, "thaoTac"));
        }
    }

    /** Đóng cửa sổ và dọn dữ liệu hoặc theo dõi không còn sử dụng. */
    @Override
    public void dispose() {
        taiKhoanDAO.boTheoDoi(theoDoiTaiKhoan);
        super.dispose();
    }

    /** Kiểm tra trạng thái và dữ liệu chưa lưu trước khi đóng cửa sổ. */
    @Override
    protected void processWindowEvent(WindowEvent event) {
        if (event.getID() == WindowEvent.WINDOW_CLOSING && (!choPhepRoiNhanVien() || !choPhepRoiTaiKhoan()
                || !choPhepRoiDichVu() || !choPhepRoiKho() || !choPhepRoiTiepNhan()
                || (pnlKhongGianKyThuat != null && !pnlKhongGianKyThuat.choPhepRoiTrang()))) {

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
        }, duLieuChung == null ? null : duLieuChung.getTrangChuDAO(), MainFrame.this.getTaiKhoanDangNhap(),
                new ActionListener() {
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

        boolean daTrienKhai = "Tổng quan tiếp nhận".equals(muc) || "Khách hàng".equals(muc) || "Phương tiện".equals(muc)
                || "Phiếu tiếp nhận".equals(muc) || "Báo giá".equals(muc) || "Lệnh sửa chữa".equals(muc)
                || "Bàn giao xe".equals(muc);

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
                pnlKhachHang = new KhachHangPanel(khachHangDAO, MainFrame.this.getTaiKhoanDangNhap(), xeDAO);
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
                pnlTiepNhan = new PhieuTiepNhanPanel(khachHangDAO, xeDAO, phieuTiepNhanDAO,
                        MainFrame.this.getTaiKhoanDangNhap(),
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
                pnlBaoGia = new BaoGiaPanel(baoGiaDAO, MainFrame.this.getTaiKhoanDangNhap(),
                        duLieuChung.getPhuTungDAO());
            }
            setScreen(pnlBaoGia);
            mucTiepNhanDangMo = muc;
            pnlDieuHuongTiepNhan.chonMuc(muc);
            setStatus("Báo giá · Lưu trong bộ nhớ của phiên");

        } else if ("Phương tiện".equals(muc)) {
            if (pnlXe == null) {
                pnlXe = new XePanel(xeDAO, khachHangDAO, MainFrame.this.getTaiKhoanDangNhap(), phieuTiepNhanDAO,
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
                pnlLenhTiepNhan = new LenhSuaChuaTiepNhanPanel(phieuSuaChuaDAO, MainFrame.this.getTaiKhoanDangNhap());
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

    /** Mở danh sách sửa chữa với tài khoản và nguồn dữ liệu hiện tại. */
    private void moDanhSachSuaChua() {
        if (getTaiKhoanDangNhap().getVaiTro() == VaiTro.NHAN_VIEN_TIEP_NHAN) {
            dieuHuongTiepNhan("Lệnh sửa chữa");
            return;

        }
        if (pnlSuaChua == null) {
            pnlSuaChua = new PhieuSuaChuaPanel(this, getTaiKhoanDangNhap(),
                    duLieuChung == null ? new PhieuSuaChuaDAO() : duLieuChung.getSuaChuaDAO());
        }

        setScreen(pnlSuaChua);
        pnlSuaChua.actionPerformed(new ActionEvent(pnlSuaChua, ActionEvent.ACTION_PERFORMED, "DANH_SACH"));
        setStatus("Sửa chữa · Dữ liệu mẫu trong bộ nhớ");
    }

    /** Mở phần ghi nhận phụ tùng thực dùng của Kỹ thuật. */
    private void moDanhSachPhuTung() {
        if (pnlPhuTung == null) {
            pnlPhuTung = new PhuTungSuDungPanel(getTaiKhoanDangNhap(),
                    duLieuChung == null ? new PhuTungSuDungDAO() : duLieuChung.getPhuTungDAO());
        }

        pnlPhuTung.moDanhSach();
        setScreen(pnlPhuTung);
        setStatus("Phụ tùng sử dụng · Dữ liệu mẫu trong bộ nhớ");
    }

    /** Chuẩn bị nguồn dữ liệu và màn hình ban đầu cho vai trò Kỹ thuật. */
    private void khoiTaoTrangChuKyThuat() {
        TrangChuKyThuatDAO trangChuDAO = new TrangChuKyThuatDAO();
        pnlTrangChuKyThuat = new TrangChuKyThuatPanel(trangChuDAO.docDuLieuMau(getTaiKhoanDangNhap()), this, this);

        JPanel pnlDanhSach = new JPanel();
        pnlDanhSach.setOpaque(false);
        pnlDanhSach.setLayout(new BoxLayout(pnlDanhSach, BoxLayout.Y_AXIS));

        String[] mucDieuHuong = { "Trang chủ", "Công việc được giao", "Báo cáo kiểm tra", "Tiến độ sửa chữa",
                "Phụ tùng sử dụng", "Đổi vai trò" };

        for (String muc : mucDieuHuong) {
            JButton btnMuc = KyThuatUi.taoNut(muc, 156);
            btnMuc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            btnMuc.setHorizontalAlignment(SwingConstants.LEFT);

            if (!"Đổi vai trò".equals(muc)) {
                btnMuc.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                btnMuc.setBackground("Trang chủ".equals(muc) ? new Color(0xDCEAFE) : UiTheme.NAVIGATION);
            }

            if ("Trang chủ".equals(muc)) {
                btnMuc.setFont(UiTheme.BRAND_FONT.deriveFont(13.0f));
            }

            btnMuc.addActionListener(this);
            pnlDanhSach.add(btnMuc);
            pnlDanhSach.add(Box.createVerticalStrut(4));
        }

        JPanel pnlDieuHuong = new JPanel(new BorderLayout());
        pnlDieuHuong.setOpaque(false);
        pnlDieuHuong.add(pnlDanhSach, BorderLayout.NORTH);

        setSidebarContent(pnlDieuHuong);
        setScreen(pnlTrangChuKyThuat);
        setStatus("Dashboard kỹ thuật • Dữ liệu mẫu theo Figma 05/10/2026");
    }

    /** Mở màn hình Kỹ thuật tương ứng với mục điều hướng. */
    private void dieuHuongKyThuat(String tenManHinh) {
        if ("Đổi vai trò".equals(tenManHinh)) {
            setStatus("Tài khoản chỉ có một vai trò. Đăng xuất để đăng nhập bằng tài khoản khác.");
        } else {
            setStatus(tenManHinh + " thuộc nhánh tiếp theo, chưa được tích hợp vào dashboard.");
        }
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

    /** Mở màn Thu ngân theo mục sidebar được chọn. */
    private void dieuHuongThuNgan(String muc) {
        mucThuNganDangMo = muc;
        pnlDieuHuongThuNgan.chonMuc(muc);
        switch (muc) {
        case "Trung tâm thanh toán": {
            PaymentCenterPanel.Navigation nav = new PaymentCenterPanel.Navigation();
            nav.costReconciliation = new ActionListener() {
                /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
                @Override
                public void actionPerformed(ActionEvent event) {
                    dieuHuongThuNgan("Đối chiếu chi phí");
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
                    setStatus("Chuyển khoản / QR · Chưa tích hợp");
                }
            };
            setScreen(new PaymentCenterPanel(thuNganDAO, getTaiKhoanDangNhap(), nav));
            setStatus("Trung tâm thanh toán");
        }
            break;
        case "Phiếu chờ thu": {
            setScreen(new PendingCollectionPanel(thuNganDAO, getTaiKhoanDangNhap(), this));
            setStatus("Phiếu chờ thu");
        }
            break;
        case "Đối chiếu chi phí": {
            setScreen(new PendingCollectionPanel(thuNganDAO, getTaiKhoanDangNhap(), this));
            setStatus("Đối chiếu chi phí · Chọn phiếu để kiểm tra và lập hóa đơn");
        }
            break;
        case "Hoá đơn đã thanh toán": {
            setScreen(new CashReceiptPanel(thuNganDAO, getTaiKhoanDangNhap()));
            setStatus("Hóa đơn đã thanh toán");
        }
            break;
        default:
            setStatus(muc + ": chức năng chưa triển khai.");
            break;
        }

    }

    /** Tạo vùng giữ chỗ cho chức năng chưa triển khai. */
    private JPanel taoManHinhTrong() {
        JPanel pnl = new JPanel();
        pnl.setOpaque(false);
        return pnl;
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
        setScreen(new CashPaymentConfirmPanel(thuNganDAO, phieu, taiKhoanDangNhap, new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                dieuHuongThuNgan("Trung tâm thanh toán");
            }
        }, new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                dieuHuongThuNgan("Trung tâm thanh toán");
            }
        }));
        setStatus("Thanh toán tiền mặt · " + phieu.getMaPhieu());
    }
}
