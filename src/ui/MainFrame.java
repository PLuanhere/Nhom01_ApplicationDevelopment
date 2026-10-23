package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import entity.TaiKhoan;
import entity.VaiTro;
import ui.screen.BaoCaoKiemTraPanel;
import ui.component.KiemTraUi;
import ui.component.SidebarPanel;
import ui.component.UiTheme;
import ui.component.WindowMouseHandler;
import ui.component.WindowTitlePanel;

/**
 * Khung ứng dụng dùng chung gồm menu đầu trang, sidebar, nội dung và thanh trạng thái.
 * Nhận danh tính đã xác thực và gắn màn hình kiểm tra của nhánh 2.
 */
public final class MainFrame extends JFrame {

    /** Phiên bản tuần tự hóa theo lớp cha Swing; không dùng để lưu phiên làm việc. */
    private static final long serialVersionUID = 1L;

    /** Sidebar có thương hiệu, vai trò, đăng xuất và vùng điều hướng trống. */
    private final SidebarPanel pnlSidebar = new SidebarPanel();
    /** Vùng chứa màn hình chức năng. */
    private final JPanel pnlNoiDung = new JPanel(new BorderLayout());
    /** Thông báo trạng thái chung. */
    private final JLabel lblTrangThai = new JLabel("Sẵn sàng");
    /** Danh tính của phiên hiện tại; được xóa khi đăng xuất. */
    private TaiKhoan taiKhoanDangNhap;

    /**
     * Tạo layout cho tài khoản đã đăng nhập; gọi trên EDT.
     * @param taiKhoan danh tính do DAO xác thực trả về
     * @param khiDangXuat thao tác mở lại trang đăng nhập
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
        // Mở khung chính ở trạng thái phóng to ngay sau đăng nhập.
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UiTheme.WINDOW);
        getRootPane().setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));
        add(createHeader(), BorderLayout.NORTH);
        // Đăng ký sự kiện kéo và đổi kích thước cửa sổ theo thành phần chung của leader.
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
            ganNhanhKiemTra();
        }
    }

    /**
     * @return tài khoản đã xác thực, không chứa mật khẩu
     * @throws IllegalStateException nếu đã đăng xuất
     */
    public TaiKhoan getTaiKhoanDangNhap() {
        if (taiKhoanDangNhap == null) {
            throw new IllegalStateException("Phiên đăng nhập đã kết thúc.");
        }
        return taiKhoanDangNhap;
    }

    /**
     * @param pnlManHinh màn hình chức năng
     * @throws NullPointerException nếu màn hình là null
     */
    // Gắn màn hình chức năng vào vùng nội dung của khung chung.
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
    // Gắn panel điều hướng vào sidebar dùng chung.
    public void setSidebarContent(JPanel pnlDieuHuong) {
        pnlSidebar.setNavigation(pnlDieuHuong);
    }

    /**
     * @param thongBao nội dung trạng thái
     * @throws NullPointerException nếu thông báo là null
     */
    // Cập nhật thông báo dưới cửa sổ.
    public void setStatus(String thongBao) {
        lblTrangThai.setText(Objects.requireNonNull(thongBao, "Thông báo không được null."));
    }

    /** @return thanh nhãn nhóm menu của khung chung */
    // Tạo các nhóm menu theo layout của leader.
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

    /** @return panel chứa thanh tiêu đề và menu */
    // Ghép thanh tiêu đề và thanh menu của leader thành header chung.
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
     * @param khiDangXuat thao tác mở lại trang đăng nhập
     * @return nút đăng xuất đã gắn sự kiện
     */
    // Tạo nút kết thúc phiên và quay lại trang đăng nhập.
    private JButton createLogoutButton(Runnable khiDangXuat) {
        JButton btnDangXuat = new JButton("Đăng xuất");
        btnDangXuat.setPreferredSize(new Dimension(0, 30));
        UiTheme.styleButton(btnDangXuat, false);
        btnDangXuat.addActionListener(event -> {
            if (taiKhoanDangNhap != null) {
                taiKhoanDangNhap = null;
                dispose();
                khiDangXuat.run();
            }
        });
        return btnDangXuat;
    }

    /** Gắn duy nhất nhánh kiểm tra vào khung chung của leader. */
    // Tạo màn hình kiểm tra và nối điều hướng của nhánh 2.
    private void ganNhanhKiemTra() {
        final BaoCaoKiemTraPanel pnlKiemTra = new BaoCaoKiemTraPanel(this, getTaiKhoanDangNhap());
        JPanel pnlDieuHuong = new JPanel();
        pnlDieuHuong.setOpaque(false);
        pnlDieuHuong.setLayout(new BoxLayout(pnlDieuHuong, BoxLayout.Y_AXIS));
        String[] nhan = {"Trang chủ", "Công việc được giao", "Báo cáo kiểm tra",
            "Tiến độ sửa chữa", "Phụ tùng sử dụng"};
        ActionListener xuLy = new ActionListener() {
            /** {@inheritDoc} */
            // Mở nhánh kiểm tra hoặc thông báo chức năng thuộc nhánh chưa tích hợp.
            @Override
            public void actionPerformed(ActionEvent suKien) {
                if ("Báo cáo kiểm tra".equals(suKien.getActionCommand())) {
                    setScreen(pnlKiemTra);
                    pnlKiemTra.actionPerformed(
                            new ActionEvent(pnlKiemTra, ActionEvent.ACTION_PERFORMED, "DANH_SACH"));
                } else {
                    setStatus(suKien.getActionCommand() + " thuộc nhánh khác, chưa tích hợp trong nhánh kiểm tra.");
                }
            }
        };
        for (String tenMuc : nhan) {
            JButton btnMuc = KiemTraUi.taoNut(tenMuc, tenMuc, false, xuLy);
            btnMuc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            btnMuc.setHorizontalAlignment(SwingConstants.LEFT);
            btnMuc.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            btnMuc.setBackground("Báo cáo kiểm tra".equals(tenMuc)
                    ? new java.awt.Color(0xDCEAFE) : UiTheme.NAVIGATION);
            pnlDieuHuong.add(btnMuc);
            pnlDieuHuong.add(javax.swing.Box.createVerticalStrut(4));
        }
        JPanel pnlVung = new JPanel(new BorderLayout());
        pnlVung.setOpaque(false);
        pnlVung.add(pnlDieuHuong, BorderLayout.NORTH);
        setSidebarContent(pnlVung);
        setScreen(pnlKiemTra);
        setStatus("Báo cáo kiểm tra · Dữ liệu mẫu trong bộ nhớ");
    }
}