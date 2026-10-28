package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.Insets;
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
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import dao.TaiKhoanDAO;
import entity.TaiKhoan;
import ui.component.LoginBackgroundPanel;
import ui.component.UiTheme;
import ui.component.WindowMouseHandler;
import ui.component.WindowTitlePanel;

/** Trang đăng nhập Page 3: đổi phần trình bày, giữ xác thực bốn tài khoản có sẵn qua DAO. */
public final class LoginFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    /** Log lỗi kỹ thuật, không ghi tên đăng nhập hoặc mật khẩu. */
    private static final System.Logger LOGGER = System.getLogger(LoginFrame.class.getName());
    /** Nguồn xác thực hiện tại, chưa kết nối database. */
    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();
    /** Hai ô nhập chỉ vẽ gợi ý khi chưa có dữ liệu; không dùng gợi ý làm giá trị nhập. */
    private final JTextField txtTenDangNhap = new JTextField() {
        private static final long serialVersionUID = 1L;

        /** {@inheritDoc} */
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            veGoiY(graphics, this, "Ví dụ: quanly");
        }
    };
    private final JPasswordField txtMatKhau = new JPasswordField() {
        private static final long serialVersionUID = 1L;

        /** {@inheritDoc} */
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            veGoiY(graphics, this, "Nhập mật khẩu");
        }
    };
    /** Các điều khiển giữ nguyên hành vi đăng nhập/hiện mật khẩu. */
    private final JCheckBox chkHienMatKhau = new JCheckBox("Hiện mật khẩu");
    private final JButton btnDangNhap = UiTheme.createAppButton("Đăng nhập", true);
    private final JLabel lblThongBao = new JLabel(
            "Nhập tài khoản nhân viên được quản lý cấp để tiếp tục công việc.");

    /** Tạo cửa sổ co giãn, thẻ nhập ở giữa ảnh và có vùng cuộn khi màn hình thấp. */
    public LoginFrame() {
        super("Garage Ô tô Đức Dũng — Đăng nhập");
        setUndecorated(true);
        setMinimumSize(new Dimension(960, 600));
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setIconImage(UiTheme.createWindowIcon());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getRootPane().setBorder(BorderFactory.createLineBorder(UiTheme.APP_BORDER));
        add(new WindowTitlePanel(this), BorderLayout.NORTH);

        LoginBackgroundPanel pnlNen = new LoginBackgroundPanel();
        GridBagConstraints viTri = new GridBagConstraints();
        viTri.insets = new Insets(24, 24, 24, 24);
        pnlNen.add(taoBieuMau(), viTri);
        JScrollPane scrDangNhap = new JScrollPane(pnlNen);
        scrDangNhap.setBorder(BorderFactory.createEmptyBorder());
        scrDangNhap.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrDangNhap, BorderLayout.CENTER);
        getRootPane().setDefaultButton(btnDangNhap);

        WindowMouseHandler mouseHandler = new WindowMouseHandler(this);
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        txtMatKhau.setEchoChar('•');
        chkHienMatKhau.addActionListener(event ->
                txtMatKhau.setEchoChar(chkHienMatKhau.isSelected() ? '\0' : '•'));
        btnDangNhap.addActionListener(event -> dangNhap());
        SwingUtilities.invokeLater(() -> txtTenDangNhap.requestFocusInWindow());
    }

    /** @return thẻ đăng nhập 488×573 px theo Page 3, không chứa nghiệp vụ xác thực */
    private JPanel taoBieuMau() {
        JPanel pnlForm = UiTheme.roundedPanel(null, new Color(255, 255, 255, 249), 12);
        pnlForm.setLayout(new BoxLayout(pnlForm, BoxLayout.Y_AXIS));
        pnlForm.setBorder(UiTheme.roundedBorder(UiTheme.APP_BORDER, 12, 34, 38));
        pnlForm.setPreferredSize(new Dimension(488, 573));
        themDong(pnlForm, taoNhanHieu(), 32);
        pnlForm.add(Box.createVerticalStrut(29));
        JLabel lblTieuDe = new JLabel("Đăng nhập hệ thống");
        lblTieuDe.setFont(UiTheme.APP_BOLD_FONT.deriveFont(26.5f));
        lblTieuDe.setForeground(UiTheme.APP_TEXT);
        themDong(pnlForm, lblTieuDe, 41);
        pnlForm.add(Box.createVerticalStrut(9));
        lblThongBao.setFont(UiTheme.APP_FONT);
        lblThongBao.setForeground(UiTheme.APP_MUTED);
        themDong(pnlForm, lblThongBao, 21);
        pnlForm.add(Box.createVerticalStrut(26));
        themDong(pnlForm, taoHangNhap("Tên đăng nhập *", txtTenDangNhap), 65);
        pnlForm.add(Box.createVerticalStrut(18));
        themDong(pnlForm, taoHangNhap("Mật khẩu *", txtMatKhau), 65);
        pnlForm.add(Box.createVerticalStrut(18));
        themDong(pnlForm, taoTuyChon(), 18);
        pnlForm.add(Box.createVerticalStrut(48));
        btnDangNhap.setFont(UiTheme.APP_BOLD_FONT.deriveFont(14.0f));
        themDong(pnlForm, btnDangNhap, 42);
        pnlForm.add(Box.createVerticalGlue());
        JLabel lblHuongDan = new JLabel("•  Tài khoản do Quản lý cấp. Quên mật khẩu? Liên hệ Quản lý.");
        lblHuongDan.setFont(UiTheme.APP_FONT.deriveFont(10.5f));
        lblHuongDan.setForeground(new Color(0x5D7084));
        JPanel pnlHuongDan = UiTheme.roundedPanel(new BorderLayout(), new Color(0xF8FAFC), 7);
        pnlHuongDan.setBorder(UiTheme.roundedBorder(new Color(0xE9EDF2), 7, 12, 12));
        pnlHuongDan.add(lblHuongDan);
        themDong(pnlForm, pnlHuongDan, 43);
        return pnlForm;
    }

    /** @return biểu tượng gốc và nhãn thương hiệu nằm trên tiêu đề */
    private JPanel taoNhanHieu() {
        JPanel pnlNhan = new JPanel(new BorderLayout(8, 0));
        pnlNhan.setOpaque(false);
        JLabel lblIcon = new JLabel(UiTheme.icon("car-login"));
        lblIcon.setPreferredSize(new Dimension(21, 32));
        lblIcon.setBorder(UiTheme.roundedBorder(new Color(0xB2C6D8), 5, 2, 2));
        pnlNhan.add(lblIcon, BorderLayout.WEST);
        JLabel lblTen = new JLabel("GARAGE ĐỨC DŨNG · STAFF DESKTOP");
        lblTen.setFont(UiTheme.APP_BOLD_FONT.deriveFont(12.0f));
        lblTen.setForeground(new Color(0x3A5976));
        pnlNhan.add(lblTen, BorderLayout.CENTER);
        return pnlNhan;
    }

    /** @return lựa chọn hiện mật khẩu và hướng dẫn liên hệ Quản lý */
    private JPanel taoTuyChon() {
        JPanel pnlTuyChon = new JPanel(new BorderLayout());
        pnlTuyChon.setOpaque(false);
        chkHienMatKhau.setOpaque(false);
        chkHienMatKhau.setFont(UiTheme.APP_FONT.deriveFont(12.0f));
        chkHienMatKhau.setForeground(UiTheme.APP_MUTED);
        chkHienMatKhau.setBorder(BorderFactory.createEmptyBorder());
        pnlTuyChon.add(chkHienMatKhau, BorderLayout.WEST);
        JButton btnQuenMatKhau = new JButton("Quên mật khẩu?");
        btnQuenMatKhau.setFont(UiTheme.APP_FONT.deriveFont(12.0f));
        btnQuenMatKhau.setForeground(UiTheme.APP_PRIMARY);
        btnQuenMatKhau.setContentAreaFilled(false);
        btnQuenMatKhau.setBorder(BorderFactory.createEmptyBorder());
        btnQuenMatKhau.addActionListener(event -> {
            lblThongBao.setForeground(UiTheme.APP_MUTED);
            lblThongBao.setText("Vui lòng liên hệ Quản lý để đặt lại mật khẩu.");
        });
        pnlTuyChon.add(btnQuenMatKhau, BorderLayout.EAST);
        return pnlTuyChon;
    }

    /**
     * Ghép nhãn và ô nhập; nhãn liên kết với control để hỗ trợ truy cập.
     * @param nhan tên trường
     * @param txtTruong ô nhập tên hoặc mật khẩu
     * @return hàng biểu mẫu cao 65 px
     */
    private JPanel taoHangNhap(String nhan, JTextField txtTruong) {
        JPanel pnlTruong = new JPanel(new BorderLayout(0, 6));
        pnlTruong.setOpaque(false);
        JLabel lblNhan = new JLabel(nhan);
        lblNhan.setFont(UiTheme.APP_BOLD_FONT.deriveFont(11.5f));
        lblNhan.setForeground(new Color(0x3F536B));
        lblNhan.setPreferredSize(new Dimension(0, 18));
        lblNhan.setLabelFor(txtTruong);
        txtTruong.setFont(UiTheme.APP_FONT);
        txtTruong.setBackground(Color.WHITE);
        txtTruong.setForeground(UiTheme.APP_TEXT);
        txtTruong.setBorder(UiTheme.roundedBorder(new Color(0xCCD7E4), 6, 0, 10));
        pnlTruong.add(lblNhan, BorderLayout.NORTH);
        pnlTruong.add(txtTruong, BorderLayout.CENTER);
        return pnlTruong;
    }

    /**
     * Thêm một hàng với chiều cao thiết kế; BoxLayout quyết định vị trí thực tế.
     * @param pnlCha panel biểu mẫu
     * @param thanhPhan thành phần cần thêm
     * @param chieuCao chiều cao hàng tính bằng pixel
     */
    private void themDong(JPanel pnlCha, JComponent thanhPhan, int chieuCao) {
        thanhPhan.setAlignmentX(LEFT_ALIGNMENT);
        thanhPhan.setPreferredSize(new Dimension(0, chieuCao));
        thanhPhan.setMinimumSize(new Dimension(0, chieuCao));
        thanhPhan.setMaximumSize(new Dimension(Integer.MAX_VALUE, chieuCao));
        pnlCha.add(thanhPhan);
    }

    /**
     * Vẽ gợi ý trong ô rỗng mà không ghi gợi ý vào dữ liệu tài khoản.
     * @param graphics ngữ cảnh vẽ của ô nhập
     * @param txtTruong ô cần vẽ gợi ý
     * @param goiY nội dung hướng dẫn
     */
    private void veGoiY(Graphics graphics, JTextField txtTruong, String goiY) {
        if (txtTruong.getDocument().getLength() == 0) {
            Graphics g = graphics.create();
            g.setFont(txtTruong.getFont());
            g.setColor(new Color(0x757575));
            FontMetrics font = g.getFontMetrics();
            g.drawString(goiY, txtTruong.getInsets().left,
                    (txtTruong.getHeight() - font.getHeight()) / 2 + font.getAscent());
            g.dispose();
        }
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
            lblThongBao.setForeground(UiTheme.APP_MUTED);
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
     * Mở khung chung với danh tính đã xác thực; đăng xuất tạo form trống mới.
     * @param taiKhoan tài khoản do DAO trả về sau đăng nhập thành công
     */
    private void moKhungChinh(TaiKhoan taiKhoan) {
        txtMatKhau.setText("");
        chkHienMatKhau.setSelected(false);
        txtMatKhau.setEchoChar('•');
        MainFrame winChinh = new MainFrame(taiKhoan, () -> new LoginFrame().setVisible(true));
        winChinh.setVisible(true);
        dispose();
    }
}
