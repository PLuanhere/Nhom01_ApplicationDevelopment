package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import dao.TaiKhoanDAO;
import entity.TaiKhoan;
import entity.VaiTro;
import ui.component.UiTheme;
import ui.screen.ThuNganShell;
import ui.component.WindowMouseHandler;
import ui.component.WindowTitlePanel;

/** Form đăng nhập: kiểm tra nhập liệu, gọi DAO xác thực rồi mở khung chung. */
public final class LoginFrame extends JFrame {

    /** Phiên bản tuần tự hóa của cửa sổ Swing. */
    private static final long serialVersionUID = 1L;
    /** Log lỗi kỹ thuật; không ghi tên đăng nhập hoặc mật khẩu. */
    private static final System.Logger LOGGER = System.getLogger(LoginFrame.class.getName());
    /** DAO xác thực bằng bốn tài khoản được tạo sẵn trong code. */
    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();
    /** Các ô nhập tên, mật khẩu và lựa chọn hiện mật khẩu của form. */
    private final JTextField txtTenDangNhap = new JTextField();
    private final JPasswordField txtMatKhau = new JPasswordField();
    private final JCheckBox chkHienMatKhau = new JCheckBox("Hiện mật khẩu");
    /** Nút chính, được khóa trong lúc kiểm tra thông tin. */
    private final JButton btnDangNhap = new JButton("Đăng nhập");
    /** Dòng hướng dẫn và thông báo lỗi; không làm thay đổi chiều cao form. */
    private final JLabel lblThongBao = new JLabel("Đăng nhập bằng tài khoản nhân viên được cấp.");

    /** Tạo form 600×404 theo Figma và gắn sự kiện; gọi trên EDT. */
    public LoginFrame() {
        super("Garage Ô tô Đức Dũng — Đăng nhập");
        setUndecorated(true);
        setResizable(false);
        setSize(600, 404);
        setLocationRelativeTo(null);
        setIconImage(UiTheme.createWindowIcon());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getRootPane().setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));
        add(new WindowTitlePanel(this, true), BorderLayout.NORTH);
        add(taoBieuMau(), BorderLayout.CENTER);
        getRootPane().setDefaultButton(btnDangNhap);

        // Dùng lại xử lý kéo cửa sổ và gắn hai thao tác của form.
        WindowMouseHandler mouseHandler = new WindowMouseHandler(this);
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        txtMatKhau.setEchoChar('•');
        chkHienMatKhau.addActionListener(event -> txtMatKhau.setEchoChar(chkHienMatKhau.isSelected() ? '\0' : '•'));
        btnDangNhap.addActionListener(event -> dangNhap());
        SwingUtilities.invokeLater(() -> txtTenDangNhap.requestFocusInWindow());
    }

    /**
     * Ghép các hàng biểu mẫu bằng layout manager với lề 32 px và khoảng cách 12 px theo Figma.
     * @return panel biểu mẫu đăng nhập
     */
    private JPanel taoBieuMau() {
        // Các hàng hiển thị theo thứ tự từ trên xuống, không dùng tọa độ cố định.
        JPanel pnlForm = new JPanel();
        pnlForm.setLayout(new BoxLayout(pnlForm, BoxLayout.Y_AXIS));
        pnlForm.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));
        JLabel lblThuongHieu = new JLabel("GARAGE ĐỨC DŨNG");
        lblThuongHieu.setFont(UiTheme.HEADING_FONT);
        lblThongBao.setFont(UiTheme.SHELL_FONT);
        lblThongBao.setForeground(UiTheme.MUTED);
        themDong(pnlForm, lblThuongHieu, 28);
        themDong(pnlForm, lblThongBao, 20);
        themDong(pnlForm, taoHangNhap("Tên đăng nhập *", txtTenDangNhap), 50);
        themDong(pnlForm, taoHangNhap("Mật khẩu *", txtMatKhau), 50);

        chkHienMatKhau.setFont(UiTheme.SHELL_FONT);
        chkHienMatKhau.setOpaque(false);
        chkHienMatKhau.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        themDong(pnlForm, chkHienMatKhau, 24);
        UiTheme.styleButton(btnDangNhap, true);
        btnDangNhap.setPreferredSize(new Dimension(92, 30));
        JPanel pnlDangNhap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlDangNhap.add(btnDangNhap);
        themDong(pnlForm, pnlDangNhap, 30);

        // Giữ nút theo Figma, chưa triển khai khôi phục mật khẩu.
        JButton btnQuenMatKhau = new JButton("Quên mật khẩu?");
        UiTheme.styleButton(btnQuenMatKhau, false);
        btnQuenMatKhau.setPreferredSize(new Dimension(156, 32));
        btnQuenMatKhau.setEnabled(false);
        btnQuenMatKhau.setToolTipText("Chức năng khôi phục mật khẩu chưa triển khai.");
        JPanel pnlQuenMatKhau = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlQuenMatKhau.add(btnQuenMatKhau);
        themDong(pnlForm, pnlQuenMatKhau, 32);
        return pnlForm;
    }

    /**
     * Tạo hàng nhãn và ô nhập, có liên kết nhãn hỗ trợ truy cập.
     * @param nhan tên trường nhập theo Figma
     * @param txtTruong ô nhập cần định dạng
     * @return panel gồm nhãn 16 px, khoảng cách 4 px và ô nhập 30 px
     */
    private JPanel taoHangNhap(String nhan, JTextField txtTruong) {
        // Mỗi hàng gồm nhãn phía trên và ô nhập bên dưới.
        JPanel pnlTruong = new JPanel(new BorderLayout(0, 4));
        JLabel lblNhan = new JLabel(nhan);
        lblNhan.setFont(UiTheme.CAPTION_FONT);
        lblNhan.setPreferredSize(new Dimension(0, 16));
        lblNhan.setLabelFor(txtTruong);
        txtTruong.setFont(UiTheme.SHELL_FONT);
        txtTruong.setBackground(UiTheme.SURFACE);
        txtTruong.setForeground(UiTheme.TEXT);
        txtTruong.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.BORDER),
                BorderFactory.createEmptyBorder(0, 8, 0, 8)));
        pnlTruong.add(lblNhan, BorderLayout.NORTH);
        pnlTruong.add(txtTruong, BorderLayout.CENTER);
        return pnlTruong;
    }

    /**
     * Thêm hàng có chiều cao cố định; các hàng cách nhau 12 px theo Figma.
     * @param pnlCha panel dùng BoxLayout dọc
     * @param thanhPhan thành phần cần thêm
     * @param chieuCao chiều cao hàng tính bằng pixel
     */
    private void themDong(JPanel pnlCha, JComponent thanhPhan, int chieuCao) {
        if (pnlCha.getComponentCount() > 0) {
            pnlCha.add(Box.createVerticalStrut(12));
        }
        thanhPhan.setAlignmentX(LEFT_ALIGNMENT);
        thanhPhan.setPreferredSize(new Dimension(thanhPhan.getPreferredSize().width, chieuCao));
        thanhPhan.setMaximumSize(new Dimension(Integer.MAX_VALUE, chieuCao));
        pnlCha.add(thanhPhan);
    }

    /** Kiểm tra trường bắt buộc trên EDT rồi xác thực ở luồng nền để không treo form. */
    private void dangNhap() {
        if (!btnDangNhap.isEnabled()) {
            return;
        }
        // Đọc dữ liệu trên EDT; xóa mảng mật khẩu khi không còn dùng.
        String tenDangNhap = txtTenDangNhap.getText().trim();
        char[] matKhau = txtMatKhau.getPassword();
        if (tenDangNhap.isEmpty() || matKhau.length == 0) {
            Arrays.fill(matKhau, '\0');
            hienThiLoi(tenDangNhap.isEmpty() ? "Vui lòng nhập tên đăng nhập." : "Vui lòng nhập mật khẩu.");
            return;
        }
        setDangXuLy(true);
        xacThucTaiKhoan(tenDangNhap, matKhau);
    }

    /**
     * Chạy xác thực ở nền; chỉ cập nhật form và mở cửa sổ tại done() trên EDT.
     * @param tenDangNhap tên đã nhập, bỏ khoảng trắng hai đầu
     * @param matKhau bản sao mật khẩu, được xóa khi xác thực xong
     */
    private void xacThucTaiKhoan(String tenDangNhap, char[] matKhau) {
        new SwingWorker<Optional<TaiKhoan>, Void>() {
            /** {@inheritDoc} */
            @Override
            protected Optional<TaiKhoan> doInBackground() {
                try {
                    return taiKhoanDAO.dangNhap(tenDangNhap, matKhau);
                } finally {
                    Arrays.fill(matKhau, '\0');
                }
            }

            /** {@inheritDoc} */
            @Override
            protected void done() {
                setDangXuLy(false);
                if (!isDisplayable()) {
                    return;
                }
                try {
                    // get() lấy kết quả đã hoàn tất, không chặn giao diện.
                    Optional<TaiKhoan> ketQua = get();
                    if (ketQua.isPresent()) {
                        moKhungChinh(ketQua.get());
                    } else {
                        hienThiLoi("Tên đăng nhập hoặc mật khẩu không đúng.");
                    }
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    hienThiLoi("Đăng nhập bị gián đoạn. Vui lòng thử lại.");
                } catch (ExecutionException error) {
                    LOGGER.log(System.Logger.Level.ERROR, "Lỗi xác thực tài khoản.", error.getCause());
                    hienThiLoi("Không thể đăng nhập lúc này. Vui lòng thử lại.");
                }
            }
        }.execute();
    }

    /**
     * Khóa hoặc mở các điều khiển theo trạng thái tác vụ.
     * @param dangXuLy true khi đang xác thực
     */
    private void setDangXuLy(boolean dangXuLy) {
        txtTenDangNhap.setEnabled(!dangXuLy);
        txtMatKhau.setEnabled(!dangXuLy);
        chkHienMatKhau.setEnabled(!dangXuLy);
        btnDangNhap.setEnabled(!dangXuLy);
        btnDangNhap.setText(dangXuLy ? "Đang xử lý" : "Đăng nhập");
        if (dangXuLy) {
            lblThongBao.setForeground(UiTheme.MUTED);
            lblThongBao.setText("Đang kiểm tra thông tin đăng nhập…");
        }
    }

    /**
     * Báo lỗi bằng chữ và màu, giữ nguyên dữ liệu để người dùng sửa.
     * @param thongBao hướng dẫn nhập lại thông tin
     */
    private void hienThiLoi(String thongBao) {
        lblThongBao.setForeground(UiTheme.ERROR);
        lblThongBao.setText(thongBao);
        if (txtTenDangNhap.getText().trim().isEmpty()) {
            txtTenDangNhap.requestFocusInWindow();
        } else {
            txtMatKhau.requestFocusInWindow();
        }
    }

    /**
     * Mở khung chung với danh tính đã xác thực; khởi động shell theo vai trò.
     * Đổi vai trò hoặc đăng xuất đều tạo form trống mới để chọn lại tài khoản.
     *
     * @param taiKhoan tài khoản do DAO trả về sau đăng nhập thành công
     */
    private void moKhungChinh(TaiKhoan taiKhoan) {
        txtMatKhau.setText("");
        chkHienMatKhau.setSelected(false);
        txtMatKhau.setEchoChar('•');
        Runnable quayDangNhap = () -> new LoginFrame().setVisible(true);
        MainFrame winChinh = new MainFrame(taiKhoan, quayDangNhap);
        // Khởi động shell tương ứng với vai trò; bổ sung các vai trò khác tại đây.
        if (taiKhoan.getVaiTro() == VaiTro.THU_NGAN) {
            new ThuNganShell(winChinh, quayDangNhap).start();
        }
        winChinh.setVisible(true);
        dispose();
    }
}
