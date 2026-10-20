package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import ui.component.SidebarPanel;
import ui.component.UiTheme;
import ui.component.WindowMouseHandler;
import ui.component.WindowTitlePanel;

/**
 * Khung ứng dụng dùng chung gồm menu đầu trang, sidebar, nội dung và thanh trạng thái.
 * Chỉ quản lý bố cục; chưa có đăng nhập, dữ liệu hay chức năng nghiệp vụ.
 */
public final class MainFrame extends JFrame {

    /** Phiên bản tuần tự hóa theo lớp cha Swing; không dùng để lưu phiên làm việc. */
    private static final long serialVersionUID = 1L;

    /** Sidebar chỉ có thương hiệu và vùng điều hướng trống. */
    private final SidebarPanel pnlSidebar = new SidebarPanel();
    /** Vùng chứa một màn hình JPanel do thành viên phụ trách chức năng cung cấp. */
    private final JPanel pnlNoiDung = new JPanel(new BorderLayout());
    /** Thông báo trạng thái chung, chưa hiển thị tài khoản hoặc vai trò giả. */
    private final JLabel lblTrangThai = new JLabel("Sẵn sàng");

    /** Tạo layout trống; cần khởi tạo và hiển thị trên EDT. */
    public MainFrame() {
        super("Garage Ô tô Đức Dũng");
        setUndecorated(true);
        setIconImage(UiTheme.createWindowIcon());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Kích thước Figma 1280×800; giới hạn nhỏ nhất theo QUY_DINH là 960×600.
        setMinimumSize(new Dimension(960, 600));
        setSize(1280, 800);
        setLocationRelativeTo(null);
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
        add(pnlNoiDung, BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);
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
     * Tạo thanh trạng thái đơn giản, không gắn dữ liệu người dùng.
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
        return pnlTrangThai;
    }
}
