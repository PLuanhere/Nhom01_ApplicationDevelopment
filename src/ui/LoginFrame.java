package ui;

import dao.chung.DuLieuGarageDAO;
import dao.nhansu.TaiKhoanDAO;
import entity.nhansu.TaiKhoan;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

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

import ui.component.LoginBackgroundPanel;
import ui.component.UiTheme;
import ui.component.WindowMouseHandler;
import ui.component.WindowTitlePanel;

/** Hiển thị trang đăng nhập và mở khu vực làm việc theo vai trò. */
public final class LoginFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final DuLieuGarageDAO duLieuChung;
    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();

    // Các ô nhập và lựa chọn trên biểu mẫu.
    private final JTextField txtTenDangNhap = new JTextField() {
        private static final long serialVersionUID = 1L;

        /** Vẽ nội dung giao diện và giữ chữ hoặc gợi ý nhập liệu dễ đọc. */
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            veGoiY(graphics, this, "Ví dụ: quanly");
        }
    };
    private final JPasswordField txtMatKhau = new JPasswordField() {
        private static final long serialVersionUID = 1L;

        /** Vẽ nội dung giao diện và giữ chữ hoặc gợi ý nhập liệu dễ đọc. */
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            veGoiY(graphics, this, "Nhập mật khẩu");
        }
    };

    private final JCheckBox chkHienMatKhau = new JCheckBox("Hiện mật khẩu");
    // Các nút thực hiện thao tác của màn hình.
    private final JButton btnDangNhap = UiTheme.createAppButton("Đăng nhập", true);
    // Thông báo hướng dẫn hoặc lỗi đăng nhập.
    private final JLabel lblThongBao = new JLabel("Nhập tài khoản nhân viên được quản lý cấp để tiếp tục công việc.");

    /** Khởi tạo cửa sổ đăng nhập, bố cục và các sự kiện nhập tài khoản. */
    public LoginFrame() {
        this(new DuLieuGarageDAO());
    }

    /** Khởi tạo cửa sổ đăng nhập, bố cục và các sự kiện nhập tài khoản. */
    public LoginFrame(DuLieuGarageDAO duLieuChung) {
        super("Garage Ô tô Đức Dũng — Đăng nhập");
        if (duLieuChung == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.duLieuChung = duLieuChung;
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
        chkHienMatKhau.addActionListener(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                txtMatKhau.setEchoChar(chkHienMatKhau.isSelected() ? '\0' : '•');
            }
        });
        btnDangNhap.addActionListener(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                dangNhap();
            }
        });
        SwingUtilities.invokeLater(new Runnable() {
            /** Khởi tạo hoặc cập nhật giao diện trên luồng Swing. */
            @Override
            public void run() {
                txtTenDangNhap.requestFocusInWindow();
            }
        });
    }

    /** Tạo thẻ đăng nhập với ô tài khoản, mật khẩu và nút xác nhận. */
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

    /** Tạo khối logo và tên garage trên trang đăng nhập. */
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

    /** Tạo lựa chọn hiện mật khẩu và hướng dẫn quên mật khẩu. */
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
        btnQuenMatKhau.addActionListener(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                lblThongBao.setForeground(UiTheme.APP_MUTED);
                lblThongBao.setText("Vui lòng liên hệ Quản lý để đặt lại mật khẩu.");
            }
        });
        pnlTuyChon.add(btnQuenMatKhau, BorderLayout.EAST);
        return pnlTuyChon;
    }

    /** Tạo một hàng gồm nhãn và ô nhập của trang đăng nhập. */
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

    /** Thêm một hàng thành phần UI và khoảng cách vào bố cục dọc. */
    private void themDong(JPanel pnlCha, JComponent thanhPhan, int chieuCao) {
        thanhPhan.setAlignmentX(LEFT_ALIGNMENT);
        thanhPhan.setPreferredSize(new Dimension(0, chieuCao));
        thanhPhan.setMinimumSize(new Dimension(0, chieuCao));
        thanhPhan.setMaximumSize(new Dimension(Integer.MAX_VALUE, chieuCao));
        pnlCha.add(thanhPhan);
    }

    /** Vẽ nội dung gợi ý khi ô nhập chưa có dữ liệu. */
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

    /** Đọc ô tài khoản, kiểm tra thiếu dữ liệu rồi gửi thông tin sang bước xác thực. */
    private void dangNhap() {
        if (!btnDangNhap.isEnabled()) {

            return;
        }

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

    /** Gọi DAO kiểm tra tài khoản và mở khung làm việc khi đăng nhập hợp lệ. */
    private void xacThucTaiKhoan(String tenDangNhap, char[] matKhau) {
        try {
            TaiKhoan ketQua = taiKhoanDAO.dangNhap(tenDangNhap, matKhau);
            if (ketQua != null && ketQua.isPhienHopLe()) {
                moKhungChinh(ketQua);
            } else {
                hienThiLoi("Tên đăng nhập hoặc mật khẩu không đúng.");
            }
        } catch (IllegalArgumentException | SecurityException ex) {
            hienThiLoi(ex.getMessage());
        } finally {
            for (int i = 0; i < matKhau.length; i++) {
                matKhau[i] = '\0';
            }
            setDangXuLy(false);
        }
    }

    /** Đổi trạng thái ô nhập và nút khi kiểm tra thông tin đăng nhập. */
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

    /** Hiển thị lỗi đăng nhập và chuyển focus về ô cần kiểm tra. */
    private void hienThiLoi(String thongBao) {
        lblThongBao.setForeground(UiTheme.ERROR);
        lblThongBao.setText(thongBao);
        if (txtTenDangNhap.getText().trim().isEmpty()) {
            txtTenDangNhap.requestFocusInWindow();
        } else {
            txtMatKhau.requestFocusInWindow();
        }
    }

    /** Xóa dữ liệu mật khẩu trên form và mở khung ứng dụng theo vai trò đăng nhập. */
    private void moKhungChinh(TaiKhoan taiKhoan) {
        txtMatKhau.setText("");
        chkHienMatKhau.setSelected(false);
        txtMatKhau.setEchoChar('•');
        MainFrame winChinh = new MainFrame(taiKhoan, new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                new LoginFrame(duLieuChung).setVisible(true);
            }
        }, duLieuChung);
        winChinh.setVisible(true);
        dispose();
    }
}
