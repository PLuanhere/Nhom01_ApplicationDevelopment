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
import ui.screen.PhieuSuaChuaPanel;
import ui.component.SuaChuaUi;
import ui.component.SidebarPanel;
import ui.component.UiTheme;
import ui.component.WindowMouseHandler;
import ui.component.WindowTitlePanel;

/**
 * Khung ứng dụng dùng chung gồm menu đầu trang, sidebar, nội dung và thanh trạng thái.
 * Nhận danh tính đã xác thực; chưa chứa màn hình hoặc dữ liệu nghiệp vụ.
 */
public final class MainFrame extends JFrame {

    /** Phiên bản tuần tự hóa theo lớp cha Swing; không dùng để lưu phiên làm việc. */
    private static final long serialVersionUID = 1L;

    /** Sidebar có thương hiệu, vai trò, đăng xuất và vùng điều hướng trống. */
    private final SidebarPanel pnlSidebar = new SidebarPanel();
    /** Vùng chứa một màn hình JPanel do thành viên phụ trách chức năng cung cấp. */
    private final JPanel pnlNoiDung = new JPanel(new BorderLayout());
    /** Thông báo trạng thái chung để các màn hình cập nhật. */
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
        // Kích thước Figma 1280×800; giới hạn nhỏ nhất theo QUY_DINH là 960×600.
        setMinimumSize(new Dimension(960, 600));
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UiTheme.WINDOW);
        getRootPane().setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));
        add(createHeader(), BorderLayout.NORTH);
        // Đăng ký rõ ràng hai loại sự kiện kéo và đổi kích thước cửa sổ.
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
        if (taiKhoan.getVaiTro() == VaiTro.NHAN_VIEN_TIEP_NHAN
                || taiKhoan.getVaiTro() == VaiTro.NHAN_VIEN_KY_THUAT) {
            ganNhanhSuaChua();
        }
    }

    /**
     * Lấy danh tính của phiên hiện tại để các màn hình sử dụng khi tích hợp.
     * @return tài khoản đã xác thực; không chứa mật khẩu
     * @throws IllegalStateException nếu đã đăng xuất
     */
    public TaiKhoan getTaiKhoanDangNhap() {
        if (taiKhoanDangNhap == null) {
            throw new IllegalStateException("Phiên đăng nhập đã kết thúc.");
        }
        return taiKhoanDangNhap;
    }

    /**
     * Gắn hoặc thay màn hình chức năng trong vùng nội dung, gọi trên EDT.
     * @param pnlManHinh màn hình của thành viên; tự quản lý bố cục và vùng cuộn bên trong
     * @throws NullPointerException nếu màn hình là null
     */
    public void setScreen(JPanel pnlManHinh) {
        Objects.requireNonNull(pnlManHinh, "Màn hình không được null.");
        pnlNoiDung.removeAll();
        pnlNoiDung.add(pnlManHinh, BorderLayout.CENTER);
        pnlNoiDung.revalidate();
        pnlNoiDung.repaint();
    }

    /**
     * Gắn panel điều hướng khi nhóm bắt đầu tích hợp chức năng, gọi trên EDT.
     * @param pnlDieuHuong panel chứa các mục điều hướng
     * @throws NullPointerException nếu panel là null
     */
    public void setSidebarContent(JPanel pnlDieuHuong) {
        pnlSidebar.setNavigation(pnlDieuHuong);
    }

    /**
     * Cập nhật thông báo chung dưới cửa sổ, gọi trên EDT.
     * @param thongBao nội dung trạng thái cần hiển thị
     * @throws NullPointerException nếu thông báo là null
     */
    public void setStatus(String thongBao) {
        lblTrangThai.setText(Objects.requireNonNull(thongBao, "Thông báo không được null."));
    }

    /**
     * Tạo header theo nhóm menu trong Figma; chưa có mục con hoặc sự kiện.
     * @return thanh menu chỉ hiển thị các nhãn nhóm
     */
    private JMenuBar createMenuBar() {
        // Nhãn tĩnh giữ đúng màu chữ Figma, không dùng chữ disabled bị xám của Look and Feel.
        JMenuBar mnuChinh = new JMenuBar();
        String[] nhomMenu = {"Hệ thống", "Danh mục", "Nghiệp vụ", "Thống kê", "Trợ giúp"};
        mnuChinh.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        mnuChinh.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        mnuChinh.setPreferredSize(new Dimension(0, UiTheme.MENU_HEIGHT));

        for (String tenNhom : nhomMenu) {
            // Mỗi nhóm mới chỉ là nhãn của khung giao diện.
            JLabel lblNhom = new JLabel(tenNhom);
            lblNhom.setFont(UiTheme.SHELL_FONT);
            lblNhom.setForeground(UiTheme.TEXT);
            lblNhom.setPreferredSize(new Dimension(UiTheme.MENU_GROUP_WIDTH, 20));
            mnuChinh.add(lblNhom);
        }
        return mnuChinh;
    }

    /**
     * Ghép thanh tiêu đề trắng và các nhãn menu thành header chung.
     * @return header tự co giãn chiều ngang, giữ chiều cao theo Figma
     */
    private JPanel createHeader() {
        // Header là thành phần riêng với vùng sidebar/nội dung bên dưới.
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.add(new WindowTitlePanel(this), BorderLayout.NORTH);
        pnlHeader.add(createMenuBar(), BorderLayout.CENTER);
        return pnlHeader;
    }

    /**
     * Tạo thanh trạng thái và hiển thị tài khoản, vai trò của phiên hiện tại.
     * @return panel trạng thái ở cạnh dưới cửa sổ
     */
    private JPanel createStatusBar() {
        // Thanh trạng thái giữ chiều cao tự nhiên theo font và lề.
        JPanel pnlTrangThai = new JPanel(new BorderLayout());
        pnlTrangThai.setPreferredSize(new Dimension(0, UiTheme.STATUS_HEIGHT));
        pnlTrangThai.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        lblTrangThai.setFont(UiTheme.CAPTION_FONT);
        lblTrangThai.setForeground(UiTheme.TEXT);
        pnlTrangThai.add(lblTrangThai, BorderLayout.CENTER);
        // Thông tin phiên được lấy từ tài khoản đã xác thực, không gán vai trò mặc định.
        JLabel lblTaiKhoan = new JLabel("Tài khoản: " + taiKhoanDangNhap.getTenDangNhap()
                + "   |   Vai trò: " + taiKhoanDangNhap.getVaiTro().getTenHienThi());
        lblTaiKhoan.setFont(UiTheme.CAPTION_FONT);
        lblTaiKhoan.setForeground(UiTheme.MUTED);
        pnlTrangThai.add(lblTaiKhoan, BorderLayout.EAST);
        return pnlTrangThai;
    }

    /**
     * Tạo nút kết thúc phiên và quay lại trang đăng nhập để thử tài khoản khác.
     * @param khiDangXuat thao tác mở lại trang đăng nhập
     * @return nút đăng xuất đã gắn sự kiện
     */
    private JButton createLogoutButton(Runnable khiDangXuat) {
        // Nút xác thực duy nhất trên sidebar; vùng chức năng vẫn để trống.
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

    /** Gắn nhánh sửa chữa cho đúng vai trò đang đăng nhập. */
    // Gắn màn hình và các lối vào nhánh 3, giữ nguyên layout và đăng xuất của leader.
    private void ganNhanhSuaChua() {
        final PhieuSuaChuaPanel pnlSuaChua = new PhieuSuaChuaPanel(this, getTaiKhoanDangNhap());
        boolean kyThuat = getTaiKhoanDangNhap().getVaiTro() == VaiTro.NHAN_VIEN_KY_THUAT;
        String[] nhan = kyThuat
                ? new String[]{"Trang chủ", "Công việc được giao", "Báo cáo kiểm tra",
                    "Tiến độ sửa chữa", "Phụ tùng sử dụng"}
                : new String[]{"Trang chủ", "Khách hàng", "Phương tiện", "Phiếu tiếp nhận", "Báo giá", "Lệnh sửa chữa"};
        JPanel pnlDieuHuong = new JPanel();
        pnlDieuHuong.setOpaque(false);
        pnlDieuHuong.setLayout(new BoxLayout(pnlDieuHuong, BoxLayout.Y_AXIS));
        ActionListener xuLy = new ActionListener() {
            /** {@inheritDoc} */
            // Mở danh sách nhánh sửa chữa hoặc thông báo chức năng chưa được tích hợp.
            @Override
            public void actionPerformed(ActionEvent suKien) {
                String mucChon = suKien.getActionCommand();
                if ("Lệnh sửa chữa".equals(mucChon) || "Công việc được giao".equals(mucChon)
                        || "Tiến độ sửa chữa".equals(mucChon)) {
                    pnlSuaChua.actionPerformed(new ActionEvent(pnlSuaChua, ActionEvent.ACTION_PERFORMED, "DANH_SACH"));
                } else {
                    setStatus(mucChon + " thuộc nhánh khác, chưa tích hợp trong nhánh sửa chữa.");
                }
            }
        };
        for (String tenMuc : nhan) {
            JButton btnMuc = SuaChuaUi.taoNut(tenMuc, tenMuc, false, xuLy);
            btnMuc.setHorizontalAlignment(SwingConstants.LEFT);
            btnMuc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            btnMuc.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            btnMuc.setBackground((kyThuat ? "Công việc được giao" : "Lệnh sửa chữa").equals(tenMuc)
                    ? new java.awt.Color(0xDCEAFE) : UiTheme.NAVIGATION);
            pnlDieuHuong.add(btnMuc);
            pnlDieuHuong.add(javax.swing.Box.createVerticalStrut(4));
        }
        JPanel pnlVung = new JPanel(new BorderLayout());
        pnlVung.setOpaque(false);
        pnlVung.add(pnlDieuHuong, BorderLayout.NORTH);
        setSidebarContent(pnlVung);
        setScreen(pnlSuaChua);
        setStatus("Sửa chữa · Dữ liệu mẫu trong bộ nhớ");
    }
}