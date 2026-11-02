package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.Objects;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingConstants;

import dao.KhachHangDAO;
import dao.BaoGiaDAO;
import dao.BanGiaoXeDAO;
import dao.DuLieuGarageDAO;
import dao.KyThuatDAO;
import dao.BaoCaoKiemTraDAO;
import dao.PhieuTiepNhanDAO;
import dao.PhieuSuaChuaDAO;
import dao.PhuTungSuDungDAO;
import dao.PhuTungDAO;
import dao.TrangChuKyThuatDAO;
import dao.XeDAO;
import dao.ThuNganDAO;
import dao.TaiKhoanDAO;
import entity.TaiKhoan;
import entity.VaiTro;
import ui.component.AppHeaderPanel;
import ui.component.KyThuatUi;
import ui.component.QuanLyNavigationPanel;
import ui.component.SidebarPanel;
import ui.component.ThuNganNavigationPanel;
import ui.component.TiepNhanNavigationPanel;
import ui.component.UiTheme;
import ui.component.WindowMouseHandler;
import ui.component.WindowTitlePanel;
import ui.screen.CashPaymentConfirmPanel;
import ui.screen.PaymentCenterPanel;
import ui.screen.PendingCollectionPanel;
import ui.screen.kythuat.BaoCaoKiemTraPanel;
import ui.screen.kythuat.KhongGianKyThuatPanel;
import ui.screen.kythuat.PhieuSuaChuaPanel;
import ui.screen.kythuat.PhuTungSuDungPanel;
import ui.screen.kythuat.TrangChuKyThuatPanel;
import ui.screen.quanly.DichVuPanel;
import ui.screen.quanly.NhanVienPanel;
import ui.screen.quanly.PhuTungPanel;
import ui.screen.quanly.TaiKhoanPanel;
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
    private final Runnable khiDangXuat;

    private TaiKhoan taiKhoanDangNhap;
    private final DuLieuGarageDAO duLieuChung;

    private NhanVienPanel pnlNhanVien;
    private QuanLyNavigationPanel pnlDieuHuongQuanLy;
    private TaiKhoanPanel pnlTaiKhoan;
    private DichVuPanel pnlDichVu;
    private PhuTungPanel pnlPhuTungQuanLy;
    private PhuTungDAO khoQuanLy;
    private String mucQuanLyDangMo = "Tổng quan điều hành";
    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();
    private final ActionListener theoDoiTaiKhoan = new ActionListener() {
        /** Thực hiện thao tác tương ứng với nút, bộ lọc hoặc phím vừa chọn. */
        @Override public void actionPerformed(ActionEvent event) { kiemTraPhienDangNhap(); }
    };

    private ThuNganDAO thuNganDAO;
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
    private BaoCaoKiemTraPanel pnlKiemTra;
    private PhieuSuaChuaPanel pnlSuaChua;
    private PhuTungSuDungPanel pnlPhuTung;

    /** Khởi tạo khung ứng dụng, nguồn dữ liệu và màn hình theo vai trò đăng nhập. */
    public MainFrame(TaiKhoan taiKhoan, Runnable khiDangXuat) {
        this(taiKhoan, khiDangXuat, null);
    }

    /** Khởi tạo khung ứng dụng, nguồn dữ liệu và màn hình theo vai trò đăng nhập. */
    public MainFrame(TaiKhoan taiKhoan, Runnable khiDangXuat, DuLieuGarageDAO duLieuChung) {
        super("Garage Ô tô Đức Dũng");

        this.duLieuChung = duLieuChung;
        taiKhoanDangNhap = Objects.requireNonNull(
                taiKhoan, "Cần tài khoản đã đăng nhập.");
        taiKhoanDangNhap.kiemTraPhien();
        this.khiDangXuat = Objects.requireNonNull(
                khiDangXuat, "Cần thao tác quay lại trang đăng nhập.");
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
        getRootPane().setBorder(
                BorderFactory.createLineBorder(UiTheme.APP_BORDER));

        if (taiKhoan.getVaiTro() == VaiTro.NHAN_VIEN_KY_THUAT) {
            add(new WindowTitlePanel(this), BorderLayout.NORTH);
            KyThuatDAO nguonKyThuat = duLieuChung == null ? new KyThuatDAO(taiKhoan)
                    : new KyThuatDAO(this::getTaiKhoanDangNhap, duLieuChung.getSuaChuaDAO(), duLieuChung.getKiemTraDAO(), duLieuChung.getPhuTungDAO());
            add(new KhongGianKyThuatPanel(this, taiKhoan, this::dangXuat, nguonKyThuat), BorderLayout.CENTER);
            return;
        }

        add(new WindowTitlePanel(this), BorderLayout.NORTH);

        WindowMouseHandler mouseHandler = new WindowMouseHandler(this);
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);

        pnlNoiDung.setBackground(UiTheme.APP_BACKGROUND);
        pnlNoiDung.setBorder(
                BorderFactory.createEmptyBorder(23, 26, 16, 26));

        add(pnlSidebar, BorderLayout.WEST);
        pnlSidebar.setRoleText(taiKhoan.getVaiTro().getTenHienThi());
        pnlSidebar.setAccount(taiKhoan);

        JPanel pnlLamViec = new JPanel(new BorderLayout());
        pnlLamViec.add(createHeader(), BorderLayout.NORTH);
        pnlLamViec.add(pnlNoiDung, BorderLayout.CENTER);
        pnlLamViec.add(createStatusBar(), BorderLayout.SOUTH);
        add(pnlLamViec, BorderLayout.CENTER);

        if (taiKhoan.getVaiTro() == VaiTro.QUAN_LY) {
            pnlDieuHuongQuanLy =
                    new QuanLyNavigationPanel(new ActionListener() {
                        /** Thực hiện thao tác tương ứng với nút, bộ lọc hoặc phím vừa chọn. */
                        @Override public void actionPerformed(ActionEvent event) {
                            dieuHuongQuanLy(event.getActionCommand());
                        }
                    });
            setSidebarContent(pnlDieuHuongQuanLy);
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
            pnlKiemTra.actionPerformed(new ActionEvent(
                    pnlKiemTra, ActionEvent.ACTION_PERFORMED, "DANH_SACH"));
            setStatus("Báo cáo kiểm tra · Dữ liệu mẫu trong bộ nhớ");
        } else if ("Lệnh sửa chữa".equals(tenManHinh)
                || "Công việc được giao".equals(tenManHinh)
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
        Objects.requireNonNull(pnlManHinh, "Màn hình không được null.");

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
        lblTrangThai.setText(Objects.requireNonNull(
                thongBao, "Thông báo không được null."));
    }

    /** Tạo header chung với menu, tìm kiếm và thông tin tài khoản. */
    private JPanel createHeader() {
        return new AppHeaderPanel(
                taiKhoanDangNhap,
                this::dangXuat,
                () -> dispatchEvent(
                        new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));
    }

    /** Tạo thanh trạng thái ở cuối khung ứng dụng. */
    private JPanel createStatusBar() {
        JPanel pnlTrangThai = new JPanel(new BorderLayout());
        pnlTrangThai.setBackground(UiTheme.APP_BACKGROUND);
        pnlTrangThai.setBorder(
                BorderFactory.createEmptyBorder(10, 26, 16, 26));

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
        if (taiKhoanDangNhap != null
                && choPhepRoiNhanVien()
                && choPhepRoiTaiKhoan()
                && choPhepRoiDichVu()
                && choPhepRoiKho()
                && choPhepRoiTiepNhan()) {
            taiKhoanDangNhap = null;
            dispose();
            khiDangXuat.run();
        }
    }

    /** Mở màn Quản lý được chọn sau khi kiểm tra dữ liệu chưa lưu. */
    private void dieuHuongQuanLy(String tenMuc) {
        boolean daCo = "Tổng quan điều hành".equals(tenMuc) || "Nhân viên".equals(tenMuc)
                || "Tài khoản & phân quyền".equals(tenMuc) || "Dịch vụ và bảng giá".equals(tenMuc)
                || "Phụ tùng".equals(tenMuc);
        if (!daCo) {
            pnlDieuHuongQuanLy.chonMuc(mucQuanLyDangMo);
            setStatus(tenMuc + ": chức năng chưa triển khai.");
            return;
        }
        if (!tenMuc.equals(mucQuanLyDangMo) && (!choPhepRoiNhanVien() || !choPhepRoiTaiKhoan() || !choPhepRoiDichVu() || !choPhepRoiKho())) {
            pnlDieuHuongQuanLy.chonMuc(mucQuanLyDangMo);
            return;
        }
        if ("Tổng quan điều hành".equals(tenMuc)) {
            JPanel pnlTrong = new JPanel();
            pnlTrong.setOpaque(false);
            setScreen(pnlTrong);
            setStatus("Tổng quan điều hành: chưa triển khai.");
        } else if ("Nhân viên".equals(tenMuc)) {
            moDanhSachNhanVien();
        } else if ("Tài khoản & phân quyền".equals(tenMuc)) {
            if (pnlTaiKhoan == null) { pnlTaiKhoan = new TaiKhoanPanel(getTaiKhoanDangNhap()); }
            setScreen(pnlTaiKhoan);
            setStatus("Tài khoản & phân quyền");
        } else if ("Dịch vụ và bảng giá".equals(tenMuc)) {
            if (pnlDichVu == null) { pnlDichVu = new DichVuPanel(getTaiKhoanDangNhap()); }
            setScreen(pnlDichVu);
            setStatus("Danh sách dịch vụ và bảng giá");
        } else if ("Phụ tùng".equals(tenMuc)) {
            if (pnlPhuTungQuanLy == null) { pnlPhuTungQuanLy = new PhuTungPanel(getTaiKhoanDangNhap(), layKhoQuanLy()); }
            pnlPhuTungQuanLy.capNhatDanhSach();
            setScreen(pnlPhuTungQuanLy);
            setStatus("Danh mục phụ tùng và tồn kho");
        }
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
        return pnlNhanVien == null
                || !pnlNoiDung.isAncestorOf(pnlNhanVien)
                || pnlNhanVien.choPhepRoiTrang();
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
        if (duLieuChung != null) { return duLieuChung.getKhoDAO(); }
        if (khoQuanLy == null) { khoQuanLy = new PhuTungDAO(); }
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
            khiDangXuat.run();
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
        if (event.getID() == WindowEvent.WINDOW_CLOSING
                && (!choPhepRoiNhanVien() || !choPhepRoiTaiKhoan() || !choPhepRoiDichVu() || !choPhepRoiKho() || !choPhepRoiTiepNhan())) {
            return;
        }

        super.processWindowEvent(event);
    }

    /** Chuẩn bị nguồn dữ liệu và màn hình ban đầu cho vai trò Tiếp nhận. */
    private void khoiTaoTrangChuTiepNhan() {
        if (duLieuChung != null) {
            khachHangDAO = duLieuChung.getKhachHangDAO(); xeDAO = duLieuChung.getXeDAO();
            phieuTiepNhanDAO = duLieuChung.getTiepNhanDAO(); baoGiaDAO = duLieuChung.getBaoGiaDAO();
            phieuSuaChuaDAO = duLieuChung.getSuaChuaDAO();
        } else {
            khachHangDAO = KhachHangDAO.taoDuLieuMau();
            xeDAO = XeDAO.taoDuLieuMau(khachHangDAO, getTaiKhoanDangNhap());
            phieuTiepNhanDAO = new PhieuTiepNhanDAO(khachHangDAO, xeDAO);
            BaoCaoKiemTraDAO nguonKiemTra = new BaoCaoKiemTraDAO();
            baoGiaDAO = new BaoGiaDAO(phieuTiepNhanDAO,
                    () -> nguonKiemTra.layBaoCaoHoanTatChoBaoGia(getTaiKhoanDangNhap()));

            phieuSuaChuaDAO = new PhieuSuaChuaDAO(baoGiaDAO, List::of);
        }

        banGiaoDAO = duLieuChung == null ? new BanGiaoXeDAO(phieuSuaChuaDAO, ma -> null) : duLieuChung.getBanGiaoDAO();
        pnlDieuHuongTiepNhan =
                new TiepNhanNavigationPanel(this::dieuHuongTiepNhan);
        pnlTrangChuTiepNhan =
                new TrangChuTiepNhanPanel(this::dieuHuongTiepNhan,
                        duLieuChung == null ? null : duLieuChung.getTrangChuDAO(), this::getTaiKhoanDangNhap, this::moBanGiaoTuTongQuan);

        setSidebarContent(pnlDieuHuongTiepNhan);
        dieuHuongTiepNhan("Tổng quan tiếp nhận");
    }

    /** Mở màn của Tiếp nhận theo mục sidebar được chọn. */
    private void dieuHuongTiepNhan(String muc) {
        if (getTaiKhoanDangNhap().getVaiTro()
                != VaiTro.NHAN_VIEN_TIEP_NHAN) {
            throw new IllegalStateException(
                    "Chỉ dành cho nhân viên tiếp nhận.");
        }

        boolean daTrienKhai =
                "Tổng quan tiếp nhận".equals(muc)
                || "Khách hàng".equals(muc)
                || "Phương tiện".equals(muc)
                || "Phiếu tiếp nhận".equals(muc)
                || "Báo giá".equals(muc)
                || "Lệnh sửa chữa".equals(muc) || "Bàn giao xe".equals(muc);

        if (daTrienKhai && !muc.equals(mucTiepNhanDangMo)) {
            if (!choPhepRoiTiepNhan()) {
                pnlDieuHuongTiepNhan.chonMuc(mucTiepNhanDangMo);
                return;
            }

            if ("Khách hàng".equals(mucTiepNhanDangMo)
                    && pnlKhachHang != null) {
                pnlKhachHang.moDanhSach();
            }
            if ("Phương tiện".equals(mucTiepNhanDangMo)
                    && pnlXe != null) {
                pnlXe.moDanhSach();
            }
            if ("Phiếu tiếp nhận".equals(mucTiepNhanDangMo)
                    && pnlTiepNhan != null) {
                pnlTiepNhan.moDanhSach();
            }
            if ("Bàn giao xe".equals(mucTiepNhanDangMo) && pnlBanGiao != null) pnlBanGiao.moDanhSach();
            if ("Báo giá".equals(mucTiepNhanDangMo) && pnlBaoGia != null) pnlBaoGia.moDanhSach();
            if ("Lệnh sửa chữa".equals(mucTiepNhanDangMo) && pnlLenhTiepNhan != null) pnlLenhTiepNhan.moDanhSach();
        }

        if ("Khách hàng".equals(muc)) {
            if (pnlKhachHang == null) {
                pnlKhachHang = new KhachHangPanel(
                        khachHangDAO,
                        this::getTaiKhoanDangNhap,
                        maKH -> xeDAO.demTheoKhach(
                                getTaiKhoanDangNhap(), maKH));
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
                        khachHangDAO, xeDAO, phieuTiepNhanDAO, this::getTaiKhoanDangNhap,
                        duLieuChung == null ? null : duLieuChung.getTrangChuDAO());
            }
            else if (!"Phiếu tiếp nhận".equals(mucTiepNhanDangMo)) pnlTiepNhan.moDanhSach();

            setScreen(pnlTiepNhan);
            mucTiepNhanDangMo = muc;
            pnlDieuHuongTiepNhan.chonMuc(muc);
            setStatus("Tiếp nhận · Phiếu lưu trong bộ nhớ của phiên");

        } else if ("Báo giá".equals(muc)) {
            if (pnlBaoGia == null) pnlBaoGia = new BaoGiaPanel(baoGiaDAO, this::getTaiKhoanDangNhap,
                    () -> duLieuChung == null ? List.of() : duLieuChung.getPhuTungDAO().docDanhMucDangHoatDong());
            setScreen(pnlBaoGia);
            mucTiepNhanDangMo = muc;
            pnlDieuHuongTiepNhan.chonMuc(muc);
            setStatus("Báo giá · Lưu trong bộ nhớ của phiên");

        } else if ("Phương tiện".equals(muc)) {
            if (pnlXe == null) {
                pnlXe = new XePanel(
                        xeDAO, khachHangDAO, this::getTaiKhoanDangNhap, phieuTiepNhanDAO,
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
            if (pnlBanGiao == null) pnlBanGiao = new BanGiaoXePanel(banGiaoDAO, this::getTaiKhoanDangNhap);
            else if (!"Bàn giao xe".equals(mucTiepNhanDangMo)) pnlBanGiao.moDanhSach();
            setScreen(pnlBanGiao); mucTiepNhanDangMo = muc; pnlDieuHuongTiepNhan.chonMuc(muc);
            setStatus("Bàn giao xe · Đối chiếu căn cứ thanh toán/phê duyệt từ nguồn phụ trách");

        } else if ("Lệnh sửa chữa".equals(muc)) {
            if (pnlLenhTiepNhan == null) pnlLenhTiepNhan = new LenhSuaChuaTiepNhanPanel(phieuSuaChuaDAO, this::getTaiKhoanDangNhap);
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
        if (!"Bàn giao xe".equals(mucTiepNhanDangMo)) return;
        if (maLenh == null) pnlBanGiao.moDanhSachChoGiao();
        else pnlBanGiao.moTheoLenh(maLenh);
    }

    /** Kiểm tra dữ liệu chưa lưu trong các màn Tiếp nhận trước khi điều hướng. */
    private boolean choPhepRoiTiepNhan() {
        if ("Bàn giao xe".equals(mucTiepNhanDangMo) && pnlBanGiao != null) return pnlBanGiao.choPhepRoiManHinh();
        if ("Lệnh sửa chữa".equals(mucTiepNhanDangMo) && pnlLenhTiepNhan != null) return pnlLenhTiepNhan.choPhepRoiManHinh();
        if ("Báo giá".equals(mucTiepNhanDangMo) && pnlBaoGia != null) return pnlBaoGia.choPhepRoiManHinh();
        if ("Khách hàng".equals(mucTiepNhanDangMo)
                && pnlKhachHang != null) {
            return pnlKhachHang.choPhepRoiManHinh();
        }
        if ("Phương tiện".equals(mucTiepNhanDangMo)
                && pnlXe != null) {
            return pnlXe.choPhepRoiManHinh();
        }
        if ("Phiếu tiếp nhận".equals(mucTiepNhanDangMo)
                && pnlTiepNhan != null) {
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
        pnlSuaChua.actionPerformed(new ActionEvent(
                pnlSuaChua, ActionEvent.ACTION_PERFORMED, "DANH_SACH"));
        setStatus("Sửa chữa · Dữ liệu mẫu trong bộ nhớ");
    }

    /** Mở phần ghi nhận phụ tùng thực dùng của Kỹ thuật. */
    private void moDanhSachPhuTung() {
        if (pnlPhuTung == null) {
            pnlPhuTung = new PhuTungSuDungPanel(
                    getTaiKhoanDangNhap(), duLieuChung == null ? new PhuTungSuDungDAO() : duLieuChung.getPhuTungDAO());
        }

        pnlPhuTung.moDanhSach();
        setScreen(pnlPhuTung);
        setStatus("Phụ tùng sử dụng · Dữ liệu mẫu trong bộ nhớ");
    }

    /** Chuẩn bị nguồn dữ liệu và màn hình ban đầu cho vai trò Kỹ thuật. */
    private void khoiTaoTrangChuKyThuat() {
        TrangChuKyThuatDAO trangChuDAO = new TrangChuKyThuatDAO();
        pnlTrangChuKyThuat = new TrangChuKyThuatPanel(
                trangChuDAO.docDuLieuMau(getTaiKhoanDangNhap()), this, this);

        JPanel pnlDanhSach = new JPanel();
        pnlDanhSach.setOpaque(false);
        pnlDanhSach.setLayout(
                new BoxLayout(pnlDanhSach, BoxLayout.Y_AXIS));

        String[] mucDieuHuong = {
                "Trang chủ", "Công việc được giao", "Báo cáo kiểm tra",
                "Tiến độ sửa chữa", "Phụ tùng sử dụng", "Đổi vai trò"
        };

        for (String muc : mucDieuHuong) {
            JButton btnMuc = KyThuatUi.taoNut(muc, 156);
            btnMuc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            btnMuc.setHorizontalAlignment(SwingConstants.LEFT);

            if (!"Đổi vai trò".equals(muc)) {
                btnMuc.setBorder(
                        BorderFactory.createEmptyBorder(0, 8, 0, 8));
                btnMuc.setBackground("Trang chủ".equals(muc)
                        ? new Color(0xDCEAFE) : UiTheme.NAVIGATION);
            }

            if ("Trang chủ".equals(muc)) {
                btnMuc.setFont(UiTheme.BRAND_FONT.deriveFont(13.0f));
            }

            btnMuc.addActionListener(this);
            pnlDanhSach.add(btnMuc);
            pnlDanhSach.add(javax.swing.Box.createVerticalStrut(4));
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
            setStatus(
                    "Tài khoản chỉ có một vai trò. Đăng xuất để đăng nhập bằng tài khoản khác.");
        } else {
            setStatus(
                    tenManHinh + " thuộc nhánh tiếp theo, chưa được tích hợp vào dashboard.");
        }
    }

    /** Chuẩn bị nguồn dữ liệu và các màn hình cho vai trò Thu ngân. */
    private void khoiTaoThuNgan() {
        thuNganDAO = new ThuNganDAO();
        pnlDieuHuongThuNgan = new ThuNganNavigationPanel(this::dieuHuongThuNgan);
        setSidebarContent(pnlDieuHuongThuNgan);
        dieuHuongThuNgan("Trung tâm thanh toán");
    }

    /** Mở màn Thu ngân theo mục sidebar được chọn. */
    private void dieuHuongThuNgan(String muc) {
        mucThuNganDangMo = muc;
        pnlDieuHuongThuNgan.chonMuc(muc);
        switch (muc) {
            case "Trung tâm thanh toán" -> {
                PaymentCenterPanel.Navigation nav = new PaymentCenterPanel.Navigation();
                nav.costReconciliation = () -> dieuHuongThuNgan("Đối chiếu chi phí");
                nav.pendingInvoices    = () -> dieuHuongThuNgan("Phiếu chờ thu");
                nav.paidDocuments      = () -> dieuHuongThuNgan("Hoá đơn đã thanh toán");
                nav.documentSearch     = () -> setStatus("Tra cứu hóa đơn · Chưa tích hợp");
                nav.cashPayment        = this::moThanhToanTienMat;
                nav.bankTransfer       = () -> setStatus("Chuyển khoản / QR · Chưa tích hợp");
                setScreen(new PaymentCenterPanel(thuNganDAO, nav));
                setStatus("Trung tâm thanh toán");
            }
            case "Phiếu chờ thu" -> {
                setScreen(new PendingCollectionPanel(
                        () -> dieuHuongThuNgan("Đối chiếu chi phí"),
                        () -> setStatus("Xem chi tiết phiếu · Chưa tích hợp")));
                setStatus("Phiếu chờ thu");
            }
            case "Đối chiếu chi phí" -> {
                setScreen(taoManHinhTrong());
                setStatus("Đối chiếu chi phí · Chưa tích hợp");
            }
            case "Hoá đơn đã thanh toán" -> {
                setScreen(taoManHinhTrong());
                setStatus("Hoá đơn đã thanh toán · Chưa tích hợp");
            }
            default -> setStatus(muc + ": chức năng chưa triển khai.");
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
        java.util.List<dao.ThuNganDAO.PhieuChoThu> danhSach =
                thuNganDAO.layPhieuChoThu();
        if (danhSach.isEmpty()) {
            setStatus("Không có phiếu chờ thu tiền mặt.");
            return;
        }
        dao.ThuNganDAO.PhieuChoThu phieu = danhSach.get(0);
        setScreen(new CashPaymentConfirmPanel(
                thuNganDAO,
                phieu,
                taiKhoanDangNhap,
                () -> dieuHuongThuNgan("Trung tâm thanh toán"),
                () -> dieuHuongThuNgan("Trung tâm thanh toán")));
        setStatus("Thanh toán tiền mặt · " + phieu.getMaPhieu());
    }
}
