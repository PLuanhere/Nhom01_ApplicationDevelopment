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
import javax.swing.SwingConstants;

import dao.KhachHangDAO;
import dao.BaoGiaDAO;
import dao.BaoCaoKiemTraDAO;
import dao.PhieuTiepNhanDAO;
import dao.PhieuSuaChuaDAO;
import dao.PhuTungSuDungDAO;
import dao.TrangChuKyThuatDAO;
import dao.XeDAO;
import entity.TaiKhoan;
import entity.VaiTro;
import ui.component.AppHeaderPanel;
import ui.component.KyThuatUi;
import ui.component.QuanLyNavigationPanel;
import ui.component.SidebarPanel;
import ui.component.TiepNhanNavigationPanel;
import ui.component.UiTheme;
import ui.component.WindowMouseHandler;
import ui.component.WindowTitlePanel;
import ui.screen.BaoCaoKiemTraPanel;
import ui.screen.KhachHangPanel;
import ui.screen.BaoGiaPanel;
import ui.screen.LenhSuaChuaTiepNhanPanel;
import ui.screen.KhongGianKyThuatPanel;
import ui.screen.NhanVienPanel;
import ui.screen.PhieuTiepNhanPanel;
import ui.screen.PhieuSuaChuaPanel;
import ui.screen.PhuTungSuDungPanel;
import ui.screen.TrangChuKyThuatPanel;
import ui.screen.TrangChuTiepNhanPanel;
import ui.screen.XePanel;

/** Khung chung và điều hướng các phân hệ theo tài khoản đăng nhập. */
public final class MainFrame extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;

    private final SidebarPanel pnlSidebar = new SidebarPanel();
    private final JPanel pnlNoiDung = new JPanel(new BorderLayout());
    private final JLabel lblTrangThai = new JLabel("Sẵn sàng");
    private final Runnable khiDangXuat;

    private TaiKhoan taiKhoanDangNhap;

    // Phân hệ Quản lý.
    private NhanVienPanel pnlNhanVien;
    private QuanLyNavigationPanel pnlDieuHuongQuanLy;

    // Phân hệ Tiếp nhận, cùng dùng một nguồn hồ sơ trong phiên.
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
    private TrangChuTiepNhanPanel pnlTrangChuTiepNhan;
    private TiepNhanNavigationPanel pnlDieuHuongTiepNhan;
    private String mucTiepNhanDangMo = "Tổng quan tiếp nhận";

    // Các màn hình kỹ thuật và sửa chữa hiện có.
    private TrangChuKyThuatPanel pnlTrangChuKyThuat;
    private BaoCaoKiemTraPanel pnlKiemTra;
    private PhieuSuaChuaPanel pnlSuaChua;
    private PhuTungSuDungPanel pnlPhuTung;

    /**
     * @param taiKhoan tài khoản đã xác thực
     * @param khiDangXuat thao tác mở lại trang đăng nhập
     */
    public MainFrame(TaiKhoan taiKhoan, Runnable khiDangXuat) {
        super("Garage Ô tô Đức Dũng");

        taiKhoanDangNhap = Objects.requireNonNull(
                taiKhoan, "Cần tài khoản đã đăng nhập.");
        this.khiDangXuat = Objects.requireNonNull(
                khiDangXuat, "Cần thao tác quay lại trang đăng nhập.");

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

        // Giữ không gian Kỹ thuật mới từ main.
        if (taiKhoan.getVaiTro() == VaiTro.NHAN_VIEN_KY_THUAT) {
            add(new WindowTitlePanel(this), BorderLayout.NORTH);
            add(new KhongGianKyThuatPanel(
                    this, taiKhoan, this::dangXuat), BorderLayout.CENTER);
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
                    new QuanLyNavigationPanel(this::dieuHuongQuanLy);
            setSidebarContent(pnlDieuHuongQuanLy);
        } else if (taiKhoan.getVaiTro() == VaiTro.NHAN_VIEN_KY_THUAT) {
            khoiTaoTrangChuKyThuat();
        } else if (taiKhoan.getVaiTro() == VaiTro.NHAN_VIEN_TIEP_NHAN) {
            khoiTaoTrangChuTiepNhan();
        }
    }

    /** Tiếp nhận điều hướng của các màn hình hiện có. */
    @Override
    public void actionPerformed(ActionEvent event) {
        String tenManHinh = event.getActionCommand();

        if ("Trang chủ".equals(tenManHinh) && pnlTrangChuKyThuat != null) {
            setScreen(pnlTrangChuKyThuat);
            setStatus("Dashboard kỹ thuật • Dữ liệu mẫu theo Figma 05/10/2026");
        } else if ("Báo cáo kiểm tra".equals(tenManHinh)) {
            if (pnlKiemTra == null) {
                pnlKiemTra = new BaoCaoKiemTraPanel(
                        this, getTaiKhoanDangNhap());
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

    /** @return tài khoản của phiên hiện tại */
    public TaiKhoan getTaiKhoanDangNhap() {
        if (taiKhoanDangNhap == null) {
            throw new IllegalStateException("Phiên đăng nhập đã kết thúc.");
        }
        return taiKhoanDangNhap;
    }

    /** @param pnlManHinh màn hình cần hiển thị trong layout chung */
    public void setScreen(JPanel pnlManHinh) {
        Objects.requireNonNull(pnlManHinh, "Màn hình không được null.");

        pnlNoiDung.removeAll();
        pnlNoiDung.add(pnlManHinh, BorderLayout.CENTER);
        pnlNoiDung.revalidate();
        pnlNoiDung.repaint();
    }

    /** @param pnlDieuHuong nội dung điều hướng cần gắn vào sidebar */
    public void setSidebarContent(JPanel pnlDieuHuong) {
        pnlSidebar.setNavigation(pnlDieuHuong);
    }

    /** @param thongBao thông báo ở cuối khung */
    public void setStatus(String thongBao) {
        lblTrangThai.setText(Objects.requireNonNull(
                thongBao, "Thông báo không được null."));
    }

    /** @return header Page 3 dùng chung */
    private JPanel createHeader() {
        return new AppHeaderPanel(
                taiKhoanDangNhap,
                this::dangXuat,
                () -> dispatchEvent(
                        new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));
    }

    /** @return thanh trạng thái của layout chung */
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

    /** Kiểm tra dữ liệu chưa lưu trước khi kết thúc phiên. */
    private void dangXuat() {
        if (taiKhoanDangNhap != null
                && choPhepRoiNhanVien()
                && choPhepRoiTiepNhan()) {
            taiKhoanDangNhap = null;
            dispose();
            khiDangXuat.run();
        }
    }

    /** @param tenMuc mục Quản lý vừa chọn */
    private void dieuHuongQuanLy(String tenMuc) {
        if ("Tổng quan điều hành".equals(tenMuc)) {
            if (!choPhepRoiNhanVien()) {
                pnlDieuHuongQuanLy.chonMuc("Nhân viên");
                return;
            }

            JPanel pnlTrong = new JPanel();
            pnlTrong.setOpaque(false);
            setScreen(pnlTrong);
            setStatus("Tổng quan điều hành: chưa triển khai.");
        } else if ("Nhân viên".equals(tenMuc)) {
            moDanhSachNhanVien();
        } else {
            setStatus(tenMuc + ": chức năng chưa triển khai.");
        }
    }

    /** Mở trang Nhân viên trong layout chung. */
    private void moDanhSachNhanVien() {
        if (pnlNhanVien == null) {
            pnlNhanVien = new NhanVienPanel(getTaiKhoanDangNhap());
        }

        setScreen(pnlNhanVien);
        setStatus("Danh sách nhân viên");
    }

    /** @return true nếu được rời trang Nhân viên */
    private boolean choPhepRoiNhanVien() {
        return pnlNhanVien == null
                || !pnlNoiDung.isAncestorOf(pnlNhanVien)
                || pnlNhanVien.choPhepRoiTrang();
    }

    /** Kiểm tra dữ liệu chưa lưu trước khi đóng cửa sổ. */
    @Override
    protected void processWindowEvent(WindowEvent event) {
        if (event.getID() == WindowEvent.WINDOW_CLOSING
                && (!choPhepRoiNhanVien() || !choPhepRoiTiepNhan())) {
            return;
        }

        super.processWindowEvent(event);
    }

    /** Khởi tạo nguồn hồ sơ và điều hướng Tiếp nhận. */
    private void khoiTaoTrangChuTiepNhan() {
        khachHangDAO = KhachHangDAO.taoDuLieuMau();
        xeDAO = XeDAO.taoDuLieuMau(khachHangDAO, getTaiKhoanDangNhap());
        phieuTiepNhanDAO = new PhieuTiepNhanDAO(khachHangDAO, xeDAO);
        BaoCaoKiemTraDAO nguonKiemTra = new BaoCaoKiemTraDAO();
        baoGiaDAO = new BaoGiaDAO(phieuTiepNhanDAO,
                () -> nguonKiemTra.layBaoCaoHoanTatChoBaoGia(getTaiKhoanDangNhap()));
        // Chưa có liên kết nhân viên/tài khoản: không tự ghép theo tên hoặc cấp quyền giả.
        phieuSuaChuaDAO = new PhieuSuaChuaDAO(baoGiaDAO, List::of);

        pnlDieuHuongTiepNhan =
                new TiepNhanNavigationPanel(this::dieuHuongTiepNhan);
        pnlTrangChuTiepNhan =
                new TrangChuTiepNhanPanel(this::dieuHuongTiepNhan);

        setSidebarContent(pnlDieuHuongTiepNhan);
        dieuHuongTiepNhan("Tổng quan tiếp nhận");
    }

    /** @param muc mục Tiếp nhận vừa chọn */
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
                || "Lệnh sửa chữa".equals(muc);

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
                        khachHangDAO, xeDAO, phieuTiepNhanDAO, this::getTaiKhoanDangNhap);
            }

            setScreen(pnlTiepNhan);
            mucTiepNhanDangMo = muc;
            pnlDieuHuongTiepNhan.chonMuc(muc);
            setStatus("Tiếp nhận · Phiếu lưu trong bộ nhớ của phiên");

        } else if ("Báo giá".equals(muc)) {
            if (pnlBaoGia == null) pnlBaoGia = new BaoGiaPanel(baoGiaDAO, this::getTaiKhoanDangNhap);
            setScreen(pnlBaoGia);
            mucTiepNhanDangMo = muc;
            pnlDieuHuongTiepNhan.chonMuc(muc);
            setStatus("Báo giá · Lưu trong bộ nhớ của phiên");

        } else if ("Phương tiện".equals(muc)) {
            if (pnlXe == null) {
                pnlXe = new XePanel(
                        xeDAO, khachHangDAO, this::getTaiKhoanDangNhap, phieuTiepNhanDAO);
            }

            if (!"Phương tiện".equals(mucTiepNhanDangMo)) {
                pnlXe.moDanhSach();
            }

            setScreen(pnlXe);
            mucTiepNhanDangMo = muc;
            pnlDieuHuongTiepNhan.chonMuc(muc);
            setStatus("Phương tiện · Tiếp nhận trong phiên, sửa chữa chưa kết nối");

        } else if ("Tổng quan tiếp nhận".equals(muc)) {
            setScreen(pnlTrangChuTiepNhan);
            mucTiepNhanDangMo = muc;
            pnlDieuHuongTiepNhan.chonMuc(muc);
            setStatus("Tổng quan tiếp nhận · Dữ liệu minh họa, chưa nối nghiệp vụ");

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

    /** @return true nếu được rời màn hình Tiếp nhận hiện tại */
    private boolean choPhepRoiTiepNhan() {
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

    /** Mở màn sửa chữa hiện có và giữ dữ liệu trong phiên. */
    private void moDanhSachSuaChua() {
        if (getTaiKhoanDangNhap().getVaiTro() == VaiTro.NHAN_VIEN_TIEP_NHAN) {
            dieuHuongTiepNhan("Lệnh sửa chữa");
            return;
        }
        if (pnlSuaChua == null) {
            pnlSuaChua = new PhieuSuaChuaPanel(
                    this, getTaiKhoanDangNhap());
        }

        setScreen(pnlSuaChua);
        pnlSuaChua.actionPerformed(new ActionEvent(
                pnlSuaChua, ActionEvent.ACTION_PERFORMED, "DANH_SACH"));
        setStatus("Sửa chữa · Dữ liệu mẫu trong bộ nhớ");
    }

    /** Mở phân hệ phụ tùng hiện có. */
    private void moDanhSachPhuTung() {
        if (pnlPhuTung == null) {
            pnlPhuTung = new PhuTungSuDungPanel(
                    getTaiKhoanDangNhap(), new PhuTungSuDungDAO());
        }

        pnlPhuTung.moDanhSach();
        setScreen(pnlPhuTung);
        setStatus("Phụ tùng sử dụng · Dữ liệu mẫu trong bộ nhớ");
    }

    /** Giữ phần khởi tạo kỹ thuật cũ để tương thích các màn hiện có. */
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

    /** @param tenManHinh chức năng kỹ thuật vừa chọn */
    private void dieuHuongKyThuat(String tenManHinh) {
        if ("Đổi vai trò".equals(tenManHinh)) {
            setStatus(
                    "Tài khoản chỉ có một vai trò. Đăng xuất để đăng nhập bằng tài khoản khác.");
        } else {
            setStatus(
                    tenManHinh + " thuộc nhánh tiếp theo, chưa được tích hợp vào dashboard.");
        }
    }
}