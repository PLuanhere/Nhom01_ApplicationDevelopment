package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import dao.PhuTungSuDungDAO;
import dao.TrangChuKyThuatDAO;
import entity.TaiKhoan;
import entity.VaiTro;
import ui.screen.BaoCaoKiemTraPanel;
import ui.component.AppHeaderPanel;
import ui.component.QuanLyNavigationPanel;
import ui.component.KyThuatUi;
import ui.component.SuaChuaUi;
import ui.component.SidebarPanel;
import ui.component.UiTheme;
import ui.component.WindowMouseHandler;
import ui.component.WindowTitlePanel;
import ui.screen.TrangChuKyThuatPanel;
import ui.screen.PhieuSuaChuaPanel;
import ui.screen.PhuTungSuDungPanel;

/**
 * Khung ứng dụng dùng chung gồm menu đầu trang, sidebar, nội dung và thanh trạng thái.
 * Tích hợp dashboard kỹ thuật, kiểm tra, sửa chữa và phụ tùng sử dụng theo vai trò đăng nhập.
 */
public final class MainFrame extends JFrame implements ActionListener {
    /** Phiên bản tương thích Swing. */
    private static final long serialVersionUID = 1L;
    /** Sidebar dùng chung của leader. */
    private final SidebarPanel pnlSidebar = new SidebarPanel();
    /** Vùng chứa màn hình chức năng. */
    private final JPanel pnlNoiDung = new JPanel(new BorderLayout());
    /** Thông báo trạng thái chung. */
    private final JLabel lblTrangThai = new JLabel("Sẵn sàng");
    /** Thao tác mở lại đăng nhập, dùng chung cho menu và sidebar. */
    private final Runnable khiDangXuat;
    /** Tài khoản của phiên hiện tại; được xóa khi đăng xuất. */
    private TaiKhoan taiKhoanDangNhap;
    /** Dashboard được tạo sau khi đăng nhập bằng vai trò kỹ thuật. */
    private TrangChuKyThuatPanel pnlTrangChuKyThuat;
    /** Màn hình kiểm tra được tạo một lần khi mở chức năng. */
    private BaoCaoKiemTraPanel pnlKiemTra;
    /** Màn hình sửa chữa được giữ lại trong suốt phiên đăng nhập. */
    private PhieuSuaChuaPanel pnlSuaChua;
    /** Màn hình và DAO phụ tùng được giữ lại trong suốt phiên đăng nhập. */
    private PhuTungSuDungPanel pnlPhuTung;

    /**
     * @param taiKhoan tài khoản đã được xác thực
     * @param khiDangXuat thao tác mở lại trang đăng nhập của leader
     * @throws NullPointerException nếu tài khoản hoặc thao tác đăng xuất là null
     */
    public MainFrame(TaiKhoan taiKhoan, Runnable khiDangXuat) {
        super("Garage Ô tô Đức Dũng");
        taiKhoanDangNhap = Objects.requireNonNull(taiKhoan, "Cần tài khoản đã đăng nhập.");
        this.khiDangXuat = Objects.requireNonNull(khiDangXuat, "Cần thao tác quay lại trang đăng nhập.");
        setTitle(getTitle() + " — " + taiKhoan.getVaiTro().getTenHienThi());
        setUndecorated(true);
        setIconImage(UiTheme.createWindowIcon());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 600));
        setSize(1280, 800);
        setLocationRelativeTo(null);
        // Mở khung chính ở trạng thái phóng to ngay sau đăng nhập.
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UiTheme.APP_BACKGROUND);
        getRootPane().setBorder(BorderFactory.createLineBorder(UiTheme.APP_BORDER));
        add(new WindowTitlePanel(this), BorderLayout.NORTH);
        WindowMouseHandler mouseHandler = new WindowMouseHandler(this);
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        pnlNoiDung.setBackground(UiTheme.APP_BACKGROUND);
        pnlNoiDung.setBorder(BorderFactory.createEmptyBorder(23, 26, 16, 26));
        add(pnlSidebar, BorderLayout.WEST);
        pnlSidebar.setRoleText(taiKhoan.getVaiTro().getTenHienThi());
        pnlSidebar.setAccount(taiKhoan);
        // Header và footer thuộc vùng làm việc; sidebar chạy suốt chiều cao bên trái.
        JPanel pnlLamViec = new JPanel(new BorderLayout());
        pnlLamViec.add(createHeader(), BorderLayout.NORTH);
        pnlLamViec.add(pnlNoiDung, BorderLayout.CENTER);
        pnlLamViec.add(createStatusBar(), BorderLayout.SOUTH);
        add(pnlLamViec, BorderLayout.CENTER);
        if (taiKhoan.getVaiTro() == VaiTro.QUAN_LY) {
            setSidebarContent(new QuanLyNavigationPanel(tenMuc ->
                    setStatus(tenMuc + ": chức năng chưa triển khai.")));
            setStatus("Tổng quan điều hành: chức năng chưa triển khai.");
        } else if (taiKhoan.getVaiTro() == VaiTro.NHAN_VIEN_KY_THUAT) {
            khoiTaoTrangChuKyThuat();
        } else if (taiKhoan.getVaiTro() == VaiTro.NHAN_VIEN_TIEP_NHAN) {
            ganNhanhSuaChua();
        }
    }

    /** {@inheritDoc} */
    // Nhận sự kiện từ các nút điều hướng và hiển thị màn hình hoặc trạng thái tương ứng.
    @Override
    public void actionPerformed(ActionEvent event) {
        String tenManHinh = event.getActionCommand();
        if ("Trang chủ".equals(tenManHinh) && pnlTrangChuKyThuat != null) {
            setScreen(pnlTrangChuKyThuat);
            setStatus("Dashboard kỹ thuật • Dữ liệu mẫu theo Figma 05/10/2026");
        } else if ("Báo cáo kiểm tra".equals(tenManHinh)) {
            if (pnlKiemTra == null) {
                pnlKiemTra = new BaoCaoKiemTraPanel(this, getTaiKhoanDangNhap());
            }
            setScreen(pnlKiemTra);
            pnlKiemTra.actionPerformed(
                    new ActionEvent(pnlKiemTra, ActionEvent.ACTION_PERFORMED, "DANH_SACH"));
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

    /**
     * @return tài khoản của phiên hiện tại
     * @throws IllegalStateException nếu phiên đã kết thúc
     */
    public TaiKhoan getTaiKhoanDangNhap() {
        if (taiKhoanDangNhap == null) {
            throw new IllegalStateException("Phiên đăng nhập đã kết thúc.");
        }
        return taiKhoanDangNhap;
    }

    /**
     * @param pnlManHinh panel chức năng cần hiển thị
     * @throws NullPointerException nếu panel là null
     */
    // Thay panel chức năng trong vùng nội dung của khung chung.
    public void setScreen(JPanel pnlManHinh) {
        Objects.requireNonNull(pnlManHinh, "Màn hình không được null.");
        pnlNoiDung.removeAll();
        pnlNoiDung.add(pnlManHinh, BorderLayout.CENTER);
        pnlNoiDung.revalidate();
        pnlNoiDung.repaint();
    }

    /**
     * @param pnlDieuHuong panel chứa các mục điều hướng
     * @throws NullPointerException nếu panel là null
     */
    // Gắn các mục điều hướng vào sidebar dùng chung.
    public void setSidebarContent(JPanel pnlDieuHuong) {
        pnlSidebar.setNavigation(pnlDieuHuong);
    }

    /**
     * @param thongBao nội dung trạng thái cần hiển thị
     * @throws NullPointerException nếu thông báo là null
     */
    // Cập nhật nội dung thông báo trên thanh trạng thái.
    public void setStatus(String thongBao) {
        lblTrangThai.setText(Objects.requireNonNull(thongBao, "Thông báo không được null."));
    }

    /**
     * Tạo header Page 3, giữ nguyên thao tác đăng xuất và thoát.
     * @return header nằm bên phải sidebar
     */
    private JPanel createHeader() {
        return new AppHeaderPanel(taiKhoanDangNhap, this::dangXuat,
                () -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));
    }

    /** @return thanh trạng thái ở cạnh dưới cửa sổ */
    // Giữ API thông báo cho các panel; danh tính đã hiển thị ở header và sidebar.
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

    /** Kết thúc phiên một lần từ menu Hệ thống và mở lại form đăng nhập trống. */
    private void dangXuat() {
        if (taiKhoanDangNhap != null) {
            taiKhoanDangNhap = null;
            dispose();
            khiDangXuat.run();
        }
    }

    /** Gắn điều hướng sửa chữa cho nhân viên tiếp nhận. */
    private void ganNhanhSuaChua() {
        String[] nhan = {"Trang chủ", "Khách hàng", "Phương tiện", "Phiếu tiếp nhận", "Báo giá", "Lệnh sửa chữa"};
        JPanel pnlDieuHuong = new JPanel();
        pnlDieuHuong.setOpaque(false);
        pnlDieuHuong.setLayout(new BoxLayout(pnlDieuHuong, BoxLayout.Y_AXIS));
        for (String tenMuc : nhan) {
            JButton btnMuc = SuaChuaUi.taoNut(tenMuc, tenMuc, false, this);
            btnMuc.setHorizontalAlignment(SwingConstants.LEFT);
            btnMuc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            btnMuc.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            btnMuc.setBackground("Lệnh sửa chữa".equals(tenMuc)
                    ? new Color(0xDCEAFE) : UiTheme.NAVIGATION);
            pnlDieuHuong.add(btnMuc);
            pnlDieuHuong.add(javax.swing.Box.createVerticalStrut(4));
        }
        JPanel pnlVung = new JPanel(new BorderLayout());
        pnlVung.setOpaque(false);
        pnlVung.add(pnlDieuHuong, BorderLayout.NORTH);
        setSidebarContent(pnlVung);
        moDanhSachSuaChua();
    }

    /** Mở lại cùng màn hình để giữ dữ liệu mẫu và xử lý xác nhận thay đổi chưa lưu. */
    private void moDanhSachSuaChua() {
        if (pnlSuaChua == null) {
            pnlSuaChua = new PhieuSuaChuaPanel(this, getTaiKhoanDangNhap());
        }
        setScreen(pnlSuaChua);
        pnlSuaChua.actionPerformed(
                new ActionEvent(pnlSuaChua, ActionEvent.ACTION_PERFORMED, "DANH_SACH"));
        setStatus("Sửa chữa · Dữ liệu mẫu trong bộ nhớ");
    }

    /** Mở phân hệ phụ tùng với cùng dữ liệu trong phiên đăng nhập kỹ thuật. */
    private void moDanhSachPhuTung() {
        if (pnlPhuTung == null) {
            pnlPhuTung = new PhuTungSuDungPanel(
                    getTaiKhoanDangNhap(), new PhuTungSuDungDAO());
        }
        pnlPhuTung.moDanhSach();
        setScreen(pnlPhuTung);
        setStatus("Phụ tùng sử dụng · Dữ liệu mẫu trong bộ nhớ");
    }

    /** Khởi tạo dashboard và các mục điều hướng dành cho tài khoản kỹ thuật. */
    // Nạp dữ liệu mẫu và gắn dashboard cùng các mục điều hướng vào khung chung.
    private void khoiTaoTrangChuKyThuat() {
        TrangChuKyThuatDAO trangChuDAO = new TrangChuKyThuatDAO();
        pnlTrangChuKyThuat = new TrangChuKyThuatPanel(
                trangChuDAO.docDuLieuMau(getTaiKhoanDangNhap()), this, this);
        JPanel pnlDanhSach = new JPanel();
        pnlDanhSach.setOpaque(false);
        pnlDanhSach.setLayout(new BoxLayout(pnlDanhSach, BoxLayout.Y_AXIS));
        String[] mucDieuHuong = {"Trang chủ", "Công việc được giao", "Báo cáo kiểm tra",
                "Tiến độ sửa chữa", "Phụ tùng sử dụng", "Đổi vai trò"};
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
            pnlDanhSach.add(javax.swing.Box.createVerticalStrut(4));
        }
        JPanel pnlDieuHuong = new JPanel(new BorderLayout());
        pnlDieuHuong.setOpaque(false);
        pnlDieuHuong.add(pnlDanhSach, BorderLayout.NORTH);
        setSidebarContent(pnlDieuHuong);
        setScreen(pnlTrangChuKyThuat);
        setStatus("Dashboard kỹ thuật • Dữ liệu mẫu theo Figma 05/10/2026");
    }

    /** @param tenManHinh tên chức năng được chọn */
    // Báo trạng thái các chức năng chưa tích hợp và hướng dẫn đổi tài khoản.
    private void dieuHuongKyThuat(String tenManHinh) {
        if ("Đổi vai trò".equals(tenManHinh)) {
            setStatus("Tài khoản chỉ có một vai trò. Đăng xuất để đăng nhập bằng tài khoản khác.");
        } else {
            setStatus(tenManHinh + " thuộc nhánh tiếp theo, chưa được tích hợp vào dashboard.");
        }
    }
}
