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
import ui.component.AppMenuBar;
import ui.component.QuanLyNavigationPanel;
import ui.component.KyThuatUi;
import ui.component.SuaChuaUi;
import ui.component.SidebarPanel;
import ui.component.UiTheme;
import ui.component.WindowMouseHandler;
import ui.component.WindowTitlePanel;
import ui.screen.BaoCaoKiemTraPanel;
import ui.screen.TrangChuKyThuatPanel;
import ui.screen.PhieuSuaChuaPanel;
import ui.screen.PhuTungSuDungPanel;
import ui.screen.KhongGianKyThuatPanel;

// Quản lý khung ứng dụng chung và gắn phân hệ theo tài khoản đăng nhập.
public final class MainFrame extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;
    private final SidebarPanel pnlSidebar = new SidebarPanel();
    private final JPanel pnlNoiDung = new JPanel(new BorderLayout());
    private final JLabel lblTrangThai = new JLabel("Sẵn sàng");
    private final Runnable khiDangXuat;
    private TaiKhoan taiKhoanDangNhap;
    private TrangChuKyThuatPanel pnlTrangChuKyThuat;
    private BaoCaoKiemTraPanel pnlKiemTra;
    private PhieuSuaChuaPanel pnlSuaChua;
    private PhuTungSuDungPanel pnlPhuTung;

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
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UiTheme.WINDOW);
        getRootPane().setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));

        if (taiKhoan.getVaiTro() == VaiTro.NHAN_VIEN_KY_THUAT) {
            add(new WindowTitlePanel(this), BorderLayout.NORTH);
            add(new KhongGianKyThuatPanel(this, taiKhoan, new Runnable() {
                // Kết thúc phiên và mở lại trang đăng nhập bằng thao tác của khung chung.
                @Override
                public void run() {
                    dangXuat();
                }
            }), BorderLayout.CENTER);
            return;
        }

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
        pnlSidebar.add(createLogoutButton(), BorderLayout.SOUTH);
        add(pnlNoiDung, BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);
        if (taiKhoan.getVaiTro() == VaiTro.QUAN_LY) {
            setSidebarContent(new QuanLyNavigationPanel(tenMuc ->
                    setStatus(tenMuc + ": chức năng chưa triển khai.")));
            setStatus("Trang chủ: chức năng chưa triển khai.");
        } else if (taiKhoan.getVaiTro() == VaiTro.NHAN_VIEN_KY_THUAT) {
            khoiTaoTrangChuKyThuat();
        } else if (taiKhoan.getVaiTro() == VaiTro.NHAN_VIEN_TIEP_NHAN) {
            ganNhanhSuaChua();
        }
    }

    // Tiếp nhận điều hướng của các màn hình hiện có trong khung chung.
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

    public TaiKhoan getTaiKhoanDangNhap() {
        if (taiKhoanDangNhap == null) {
            throw new IllegalStateException("Phiên đăng nhập đã kết thúc.");
        }
        return taiKhoanDangNhap;
    }

    // Thay màn hình chức năng trong vùng nội dung của khung chung.
    public void setScreen(JPanel pnlManHinh) {
        Objects.requireNonNull(pnlManHinh, "Màn hình không được null.");
        pnlNoiDung.removeAll();
        pnlNoiDung.add(pnlManHinh, BorderLayout.CENTER);
        pnlNoiDung.revalidate();
        pnlNoiDung.repaint();
    }

    // Gắn nội dung điều hướng vào sidebar dùng chung.
    public void setSidebarContent(JPanel pnlDieuHuong) {
        pnlSidebar.setNavigation(pnlDieuHuong);
    }

    // Cập nhật thông báo trạng thái ở cuối khung.
    public void setStatus(String thongBao) {
        lblTrangThai.setText(Objects.requireNonNull(thongBao, "Thông báo không được null."));
    }

    // Ghép thanh tiêu đề và menu hiện có của leader thành đầu khung.
    private JPanel createHeader() {
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.add(new WindowTitlePanel(this), BorderLayout.NORTH);
        pnlHeader.add(new AppMenuBar(this::dangXuat,
                () -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING))), BorderLayout.CENTER);
        return pnlHeader;
    }

    // Hiển thị trạng thái cùng tài khoản và vai trò hiện tại.
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

    // Tạo nút đăng xuất theo định dạng chung hiện có.
    private JButton createLogoutButton() {
        JButton btnDangXuat = new JButton("Đăng xuất");
        btnDangXuat.setPreferredSize(new Dimension(0, 30));
        UiTheme.styleButton(btnDangXuat, false);
        btnDangXuat.addActionListener(event -> dangXuat());
        return btnDangXuat;
    }

    // Kết thúc phiên một lần và mở lại trang đăng nhập.
    private void dangXuat() {
        if (taiKhoanDangNhap != null) {
            taiKhoanDangNhap = null;
            dispose();
            khiDangXuat.run();
        }
    }

    // Giữ điều hướng sửa chữa hiện có dành cho nhân viên tiếp nhận.
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
            btnMuc.setBackground("Lệnh sửa chữa".equals(tenMuc) ? new Color(0xDCEAFE) : UiTheme.NAVIGATION);
            pnlDieuHuong.add(btnMuc);
            pnlDieuHuong.add(javax.swing.Box.createVerticalStrut(4));
        }
        JPanel pnlVung = new JPanel(new BorderLayout());
        pnlVung.setOpaque(false);
        pnlVung.add(pnlDieuHuong, BorderLayout.NORTH);
        setSidebarContent(pnlVung);
        moDanhSachSuaChua();
    }

    // Mở lại màn sửa chữa hiện có và giữ dữ liệu trong phiên.
    private void moDanhSachSuaChua() {
        if (pnlSuaChua == null) {
            pnlSuaChua = new PhieuSuaChuaPanel(this, getTaiKhoanDangNhap());
        }
        setScreen(pnlSuaChua);
        pnlSuaChua.actionPerformed(new ActionEvent(pnlSuaChua, ActionEvent.ACTION_PERFORMED, "DANH_SACH"));
        setStatus("Sửa chữa · Dữ liệu mẫu trong bộ nhớ");
    }

    // Giữ khả năng mở phân hệ phụ tùng cũ cho các lời gọi hiện có.
    private void moDanhSachPhuTung() {
        if (pnlPhuTung == null) {
            pnlPhuTung = new PhuTungSuDungPanel(getTaiKhoanDangNhap(), new PhuTungSuDungDAO());
        }
        pnlPhuTung.moDanhSach();
        setScreen(pnlPhuTung);
        setStatus("Phụ tùng sử dụng · Dữ liệu mẫu trong bộ nhớ");
    }

    // Giữ phần khởi tạo dashboard cũ để tương thích các màn hình hiện có.
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

    // Thông báo chức năng chưa tích hợp và hướng dẫn đổi tài khoản.
    private void dieuHuongKyThuat(String tenManHinh) {
        if ("Đổi vai trò".equals(tenManHinh)) {
            setStatus("Tài khoản chỉ có một vai trò. Đăng xuất để đăng nhập bằng tài khoản khác.");
        } else {
            setStatus(tenManHinh + " thuộc nhánh tiếp theo, chưa được tích hợp vào dashboard.");
        }
    }
}