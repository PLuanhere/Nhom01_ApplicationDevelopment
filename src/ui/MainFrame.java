package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import dao.TrangChuKyThuatDAO;
import entity.TaiKhoan;
import entity.VaiTro;
import ui.component.KyThuatUi;
import ui.component.SidebarPanel;
import ui.component.UiTheme;
import ui.component.WindowMouseHandler;
import ui.component.WindowTitlePanel;
import ui.screen.TrangChuKyThuatPanel;

/**
 * Khung ứng dụng dùng chung gồm thanh tiêu đề, menu, sidebar, nội dung và thanh trạng thái.
 * Nhận tài khoản đã xác thực và gắn màn hình chức năng theo vai trò.
 */
public final class MainFrame extends JFrame implements ActionListener {
    /** Phiên bản tương thích Swing. */
    private static final long serialVersionUID = 1L;
    /** Sidebar dùng chung của leader. */
    private final SidebarPanel pnlSidebar = new SidebarPanel();
    /** Vùng hiển thị màn hình chức năng. */
    private final JPanel pnlNoiDung = new JPanel(new BorderLayout());
    /** Nội dung thông báo trên thanh trạng thái. */
    private final JLabel lblTrangThai = new JLabel("Sẵn sàng");
    /** Tài khoản của phiên hiện tại; được xóa khi đăng xuất. */
    private TaiKhoan taiKhoanDangNhap;
    /** Dashboard được tạo sau khi đăng nhập bằng vai trò kỹ thuật. */
    private TrangChuKyThuatPanel pnlTrangChuKyThuat;

    /**
     * @param taiKhoan tài khoản đã được xác thực
     * @param khiDangXuat thao tác mở lại trang đăng nhập của leader
     * @throws NullPointerException nếu tài khoản hoặc thao tác đăng xuất là null
     */
    public MainFrame(TaiKhoan taiKhoan, Runnable khiDangXuat) {
        super("Garage Ô tô Đức Dũng");
        taiKhoanDangNhap = Objects.requireNonNull(taiKhoan, "Cần tài khoản đã đăng nhập.");
        Objects.requireNonNull(khiDangXuat, "Cần thao tác quay lại trang đăng nhập.");
        setTitle(getTitle() + " — " + taiKhoan.getVaiTro().getTenHienThi());
        setUndecorated(true);
        setIconImage(UiTheme.createWindowIcon());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 600));
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UiTheme.WINDOW);
        getRootPane().setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));
        add(createHeader(), BorderLayout.NORTH);
        WindowMouseHandler mouseHandler = new WindowMouseHandler(this);
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        pnlNoiDung.setBackground(UiTheme.WINDOW);
        pnlNoiDung.setBorder(BorderFactory.createEmptyBorder(UiTheme.CONTENT_PADDING, UiTheme.CONTENT_PADDING,
                UiTheme.CONTENT_PADDING, UiTheme.CONTENT_PADDING));
        add(pnlSidebar, BorderLayout.WEST);
        pnlSidebar.setRoleText(taiKhoan.getVaiTro().getTenHienThi());
        pnlSidebar.add(createLogoutButton(khiDangXuat), BorderLayout.SOUTH);
        add(pnlNoiDung, BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);
        if (taiKhoan.getVaiTro() == VaiTro.NHAN_VIEN_KY_THUAT) {
            khoiTaoTrangChuKyThuat();
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

    /** @return thanh nhãn nhóm menu của khung chung */
    // Tạo các nhãn nhóm menu theo khung hiện có của leader.
    private JMenuBar createMenuBar() {
        JMenuBar mnuChinh = new JMenuBar();
        String[] nhomMenu = {"Hệ thống", "Danh mục", "Nghiệp vụ", "Thống kê", "Trợ giúp"};
        mnuChinh.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        mnuChinh.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        mnuChinh.setPreferredSize(new Dimension(0, UiTheme.MENU_HEIGHT));
        for (String tenNhom : nhomMenu) {
            JLabel lblNhom = new JLabel(tenNhom);
            lblNhom.setFont(UiTheme.SHELL_FONT);
            lblNhom.setForeground(UiTheme.TEXT);
            lblNhom.setPreferredSize(new Dimension(UiTheme.MENU_GROUP_WIDTH, 20));
            mnuChinh.add(lblNhom);
        }
        return mnuChinh;
    }

    /** @return panel chứa thanh tiêu đề và thanh menu */
    // Ghép thanh tiêu đề cửa sổ và thanh menu của leader.
    private JPanel createHeader() {
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.add(new WindowTitlePanel(this), BorderLayout.NORTH);
        pnlHeader.add(createMenuBar(), BorderLayout.CENTER);
        return pnlHeader;
    }

    /** @return thanh trạng thái ở cạnh dưới cửa sổ */
    // Hiển thị thông báo, tài khoản và vai trò của phiên hiện tại.
    private JPanel createStatusBar() {
        JPanel pnlTrangThai = new JPanel(new BorderLayout());
        pnlTrangThai.setPreferredSize(new Dimension(0, UiTheme.STATUS_HEIGHT));
        pnlTrangThai.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        lblTrangThai.setFont(UiTheme.CAPTION_FONT);
        lblTrangThai.setForeground(UiTheme.TEXT);
        pnlTrangThai.add(lblTrangThai, BorderLayout.CENTER);
        JLabel lblTaiKhoan = new JLabel("Tài khoản: " + taiKhoanDangNhap.getTenDangNhap()
                + "   |   Vai trò: " + taiKhoanDangNhap.getVaiTro().getTenHienThi());
        lblTaiKhoan.setFont(UiTheme.CAPTION_FONT);
        lblTaiKhoan.setForeground(UiTheme.MUTED);
        pnlTrangThai.add(lblTaiKhoan, BorderLayout.EAST);
        return pnlTrangThai;
    }

    /**
     * @param khiDangXuat thao tác mở lại màn hình đăng nhập
     * @return nút đăng xuất đã đăng ký sự kiện
     */
    // Tạo nút kết thúc phiên và quay lại trang đăng nhập.
    private JButton createLogoutButton(Runnable khiDangXuat) {
        JButton btnDangXuat = new JButton("Đăng xuất");
        btnDangXuat.setPreferredSize(new Dimension(0, 30));
        UiTheme.styleButton(btnDangXuat, false);
        btnDangXuat.addActionListener(new ActionListener() {
            /** {@inheritDoc} */
            // Xóa phiên hiện tại và gọi thao tác mở lại trang đăng nhập của leader.
            @Override
            public void actionPerformed(ActionEvent event) {
                if (taiKhoanDangNhap != null) {
                    taiKhoanDangNhap = null;
                    dispose();
                    khiDangXuat.run();
                }
            }
        });
        return btnDangXuat;
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
                btnMuc.setBackground("Trang chủ".equals(muc) ? new java.awt.Color(0xDCEAFE) : UiTheme.NAVIGATION);
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