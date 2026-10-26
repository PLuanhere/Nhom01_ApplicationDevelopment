package ui;

import java.awt.BorderLayout;
import java.awt.Color;
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

import dao.PhuTungSuDungDAO;
import entity.TaiKhoan;
import entity.VaiTro;
import ui.component.PhuTungUi;
import ui.component.SidebarPanel;
import ui.component.UiTheme;
import ui.component.WindowMouseHandler;
import ui.component.WindowTitlePanel;
import ui.screen.PhuTungSuDungPanel;

/**
 * Khung ứng dụng chung nhận tài khoản đã đăng nhập và hiển thị màn chức năng.
 */
public final class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private final SidebarPanel pnlSidebar = new SidebarPanel();
    private final JPanel pnlNoiDung = new JPanel(new BorderLayout());
    private final JLabel lblTrangThai = new JLabel("Sẵn sàng");
    private TaiKhoan taiKhoanDangNhap;

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
        setLayout(new BorderLayout());
        getContentPane().setBackground(UiTheme.WINDOW);
        getRootPane().setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));
        add(createHeader(), BorderLayout.NORTH);
        WindowMouseHandler mouseHandler = new WindowMouseHandler(this);
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        pnlNoiDung.setBackground(UiTheme.WINDOW);
        pnlNoiDung.setBorder(BorderFactory.createEmptyBorder(
                UiTheme.CONTENT_PADDING, UiTheme.CONTENT_PADDING,
                UiTheme.CONTENT_PADDING, UiTheme.CONTENT_PADDING));
        add(pnlSidebar, BorderLayout.WEST);
        pnlSidebar.setRoleText(taiKhoan.getVaiTro().getTenHienThi());
        pnlSidebar.add(createLogoutButton(khiDangXuat), BorderLayout.SOUTH);
        add(pnlNoiDung, BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);
        ganNhanhPhuTung();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    public TaiKhoan getTaiKhoanDangNhap() {
        if (taiKhoanDangNhap == null) {
            throw new IllegalStateException("Phiên đăng nhập đã kết thúc.");
        }
        return taiKhoanDangNhap;
    }

    /**
     * Gắn màn hình vào vùng nội dung chung.
     * @param pnlManHinh panel cần hiển thị
     */
    // Thay panel chức năng và giữ nguyên header, sidebar, trạng thái của khung chung.
    public void setScreen(JPanel pnlManHinh) {
        Objects.requireNonNull(pnlManHinh, "Màn hình không được null.");
        pnlNoiDung.removeAll();
        pnlNoiDung.add(pnlManHinh, BorderLayout.CENTER);
        pnlNoiDung.revalidate();
        pnlNoiDung.repaint();
    }

    /**
     * Gắn điều hướng của phân hệ.
     * @param pnlDieuHuong panel điều hướng
     */
    // Đưa các mục chức năng vào vùng điều hướng có sẵn của leader.
    public void setSidebarContent(JPanel pnlDieuHuong) {
        pnlSidebar.setNavigation(pnlDieuHuong);
    }

    /**
     * Cập nhật trạng thái của cửa sổ.
     * @param thongBao nội dung trạng thái
     */
    // Hiển thị thông báo tại thanh trạng thái chung.
    public void setStatus(String thongBao) {
        lblTrangThai.setText(Objects.requireNonNull(thongBao, "Thông báo không được null."));
    }

    // Tạo các nhóm menu theo khung giao diện có sẵn.
    private JMenuBar createMenuBar() {
        JMenuBar mnuChinh = new JMenuBar();
        String[] nhomMenu = {"Hệ thống", "Danh mục", "Nghiệp vụ", "Báo cáo", "Trợ giúp"};
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

    // Ghép thanh tiêu đề và menu bằng component chung của leader.
    private JPanel createHeader() {
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.add(new WindowTitlePanel(this), BorderLayout.NORTH);
        pnlHeader.add(createMenuBar(), BorderLayout.CENTER);
        return pnlHeader;
    }

    // Hiển thị trạng thái và danh tính lấy từ phiên đăng nhập.
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

    // Kết thúc phiên hiện tại và gọi lại màn đăng nhập của leader.
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

    // Gắn phân hệ và bố trí điều hướng, đổi vai trò, đăng xuất theo sidebar của thiết kế.
    private void ganNhanhPhuTung() {
        if (taiKhoanDangNhap.getVaiTro() != VaiTro.NHAN_VIEN_KY_THUAT) {
            return;
        }
        pnlSidebar.setRoleText("Kỹ thuật");
        PhuTungSuDungPanel pnlPhuTung = new PhuTungSuDungPanel(
                taiKhoanDangNhap, new PhuTungSuDungDAO());
        JPanel pnlDieuHuong = new JPanel();
        pnlDieuHuong.setLayout(new BoxLayout(pnlDieuHuong, BoxLayout.Y_AXIS));
        pnlDieuHuong.setOpaque(false);
        java.util.Map<String, JButton> cacNutDieuHuong = new java.util.LinkedHashMap<String, JButton>();
        String[] cacMuc = {"Trang chủ", "Công việc được giao", "Báo cáo kiểm tra",
                "Tiến độ sửa chữa", "Phụ tùng sử dụng"};
        for (String tenMuc : cacMuc) {
            JButton btnMuc = PhuTungUi.taoNut(tenMuc, false, false);
            btnMuc.setPreferredSize(new Dimension(156, 30));
            btnMuc.setMinimumSize(new Dimension(0, 30));
            btnMuc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            btnMuc.setAlignmentX(LEFT_ALIGNMENT);
            btnMuc.setHorizontalAlignment(JButton.LEFT);
            btnMuc.setBackground("Phụ tùng sử dụng".equals(tenMuc)
                    ? PhuTungUi.MAU_CHON : UiTheme.NAVIGATION);
            btnMuc.setFont("Phụ tùng sử dụng".equals(tenMuc)
                    ? UiTheme.BRAND_FONT.deriveFont(13.0f) : UiTheme.SHELL_FONT.deriveFont(13.0f));
            btnMuc.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            cacNutDieuHuong.put(tenMuc, btnMuc);
            btnMuc.addActionListener(new ActionListener() {
                // Mở chức năng của nhánh hiện tại và cập nhật màu mục điều hướng được chọn.
                @Override
                public void actionPerformed(ActionEvent suKien) {
                    if ("Phụ tùng sử dụng".equals(tenMuc) || "Tiến độ sửa chữa".equals(tenMuc)) {
                        for (java.util.Map.Entry<String, JButton> muc : cacNutDieuHuong.entrySet()) {
                            boolean daChon = muc.getKey().equals(tenMuc);
                            muc.getValue().setBackground(daChon ? PhuTungUi.MAU_CHON : UiTheme.NAVIGATION);
                            muc.getValue().setFont(daChon ? UiTheme.BRAND_FONT.deriveFont(13.0f)
                                    : UiTheme.SHELL_FONT.deriveFont(13.0f));
                        }
                        if ("Phụ tùng sử dụng".equals(tenMuc)) {
                            pnlPhuTung.moDanhSach();
                        } else {
                            pnlPhuTung.moLenhDangChon();
                        }
                        setScreen(pnlPhuTung);
                    } else {
                        PhuTungUi.hienHopThoai(MainFrame.this, tenMuc,
                                "Màn hình này chưa được tích hợp vào nhánh phụ tùng.", "Đóng", false, false);
                    }
                }
            });
            pnlDieuHuong.add(btnMuc);
            pnlDieuHuong.add(javax.swing.Box.createVerticalStrut(4));
        }
        JButton btnDoiVaiTro = PhuTungUi.taoNut("Đổi vai trò", false, false);
        btnDoiVaiTro.setAlignmentX(LEFT_ALIGNMENT);
        btnDoiVaiTro.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        btnDoiVaiTro.addActionListener(new ActionListener() {
            // Giữ quy trình đổi tài khoản thông qua đăng xuất và đăng nhập hiện có.
            @Override
            public void actionPerformed(ActionEvent suKien) {
                PhuTungUi.hienHopThoai(MainFrame.this, "Đổi vai trò",
                        "Hãy đăng xuất và đăng nhập bằng tài khoản khác.", "Đóng", false, false);
            }
        });
        pnlDieuHuong.add(btnDoiVaiTro);
        pnlDieuHuong.add(javax.swing.Box.createVerticalStrut(4));
        BorderLayout boCucSidebar = (BorderLayout) pnlSidebar.getLayout();
        java.awt.Component nutDangXuat = boCucSidebar.getLayoutComponent(BorderLayout.SOUTH);
        if (nutDangXuat instanceof JButton) {
            JButton btnDangXuat = (JButton) nutDangXuat;
            pnlSidebar.remove(btnDangXuat);
            btnDangXuat.setUI(new javax.swing.plaf.basic.BasicButtonUI());
            btnDangXuat.setFont(UiTheme.SHELL_FONT.deriveFont(13.0f));
            btnDangXuat.setBackground(UiTheme.SURFACE);
            btnDangXuat.setForeground(UiTheme.TEXT);
            btnDangXuat.setOpaque(true);
            btnDangXuat.setContentAreaFilled(true);
            btnDangXuat.setFocusPainted(false);
            btnDangXuat.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));
            btnDangXuat.setAlignmentX(LEFT_ALIGNMENT);
            btnDangXuat.setPreferredSize(new Dimension(156, 30));
            btnDangXuat.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            pnlDieuHuong.add(btnDangXuat);
        }
        setSidebarContent(pnlDieuHuong);
        setScreen(pnlPhuTung);
    }
}