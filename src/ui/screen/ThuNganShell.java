package ui.screen;

import java.util.Objects;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import ui.MainFrame;

/**
 * Controller điều hướng cho vai trò Thu ngân.
 *
 * <p>Trách nhiệm:</p>
 * <ul>
 *   <li>Tạo {@link ThuNganNavigationPanel} và gắn vào sidebar của {@link MainFrame}.</li>
 *   <li>Chuyển màn hình tương ứng khi người dùng bấm mục nav.</li>
 *   <li>Xử lý Đổi vai trò và Đăng xuất với hộp thoại xác nhận.</li>
 * </ul>
 *
 * <p>Không chứa SQL hoặc nghiệp vụ thanh toán.</p>
 *
 * <h3>Cách dùng từ LoginFrame:</h3>
 * <pre>{@code
 * Runnable quayDangNhap = () -> new LoginFrame().setVisible(true);
 * MainFrame winChinh = new MainFrame(taiKhoan, quayDangNhap);
 * new ThuNganShell(winChinh, quayDangNhap).start();
 * winChinh.setVisible(true);
 * }</pre>
 */
public final class ThuNganShell {

    /** Khung chính nhận sidebar và màn hình từ shell này. */
    private final MainFrame winChinh;
    /** Callback quay màn hình đăng nhập khi đổi vai trò hoặc đăng xuất. */
    private final Runnable khiDangXuat;

    /**
     * Tạo shell cho vai trò Thu ngân.
     *
     * @param winChinh    khung chính đã được tạo với tài khoản Thu ngân
     * @param khiDangXuat callback quay lại màn hình đăng nhập
     * @throws NullPointerException nếu bất kỳ tham số nào là null
     */
    public ThuNganShell(MainFrame winChinh, Runnable khiDangXuat) {
        this.winChinh = Objects.requireNonNull(winChinh, "Cần khung chính.");
        this.khiDangXuat = Objects.requireNonNull(khiDangXuat, "Cần callback đăng xuất.");
    }

    /**
     * Gắn panel điều hướng vào sidebar và hiển thị trang chủ;
     * gọi trên EDT sau khi tạo {@code MainFrame}.
     */
    public void start() {
        ThuNganNavigationPanel pnlNav = new ThuNganNavigationPanel(
                this::chuyenTrang,
                this::xuLyDoiVaiTro,
                this::xuLyDangXuat);

        winChinh.setSidebarContent(pnlNav);
        // Hiển thị trang chủ ngay sau khi đăng nhập.
        chuyenTrang(ThuNganShellPage.TRANG_CHU);
    }

    // ── Điều hướng màn hình ──────────────────────────────────────────────────

    /**
     * Tạo và hiển thị màn hình tương ứng với trang được chọn trên sidebar.
     * Placeholder (THANH_TOAN, TRA_CUU_CHUNG_TU) sẽ được thay bằng panel thực khi tích hợp.
     *
     * @param trang trang cần điều hướng đến
     */
    private void chuyenTrang(ThuNganShellPage trang) {
        javax.swing.JPanel pnlManHinh = switch (trang) {
            case TRANG_CHU -> new HomeCashDashboardThuNganN4(
                    () -> winChinh.setScreen(new DashDetailCashierPanel(
                            () -> chuyenTrang(ThuNganShellPage.TRANG_CHU))),
                    this::moManHinhPhieuCho);
            case THANH_TOAN -> placeholder("💳", "Thanh toán",
                    "Chức năng lập hóa đơn và ghi nhận thanh toán sẽ được tích hợp tại đây.");
            case TRA_CUU_CHUNG_TU -> placeholder("🔍", "Tra cứu chứng từ",
                    "Chức năng tra cứu và in lại hóa đơn, chứng từ sẽ được tích hợp tại đây.");
        };
        winChinh.setScreen(pnlManHinh);
        winChinh.setStatus(trang.getTenHienThi());
    }

    /**
     * Mở màn hình danh sách phiếu chờ thanh toán.
     * Dùng chung cho nút từ dashboard và nút "Quay lại" từ DocumentSearchPanel.
     */
    private void moManHinhPhieuCho() {
        winChinh.setScreen(new PendingInvoicesPanel(
                () -> winChinh.setScreen(new DocumentSearchPanel(
                        this::xuLyTraCuu,
                        this::moManHinhPhieuCho)),
                () -> { /* đối chiếu — tích hợp sau */ },
                () -> chuyenTrang(ThuNganShellPage.TRANG_CHU)));
    }

    /**
     * Mở màn hình tra cứu chứng từ độc lập (từ sidebar hoặc nút Đổi tiêu chí).
     */
    private void moManHinhTraCuu() {
        winChinh.setScreen(new DocumentSearchPanel(
                this::xuLyTraCuu,
                () -> chuyenTrang(ThuNganShellPage.TRANG_CHU)));
    }

    /**
     * Xử lý kết quả tra cứu: mở CashReceiptPanel khi có dữ liệu (tiền mặt/chuyển khoản),
     * mở DocumentNotFoundPanel khi không tìm thấy (tất cả hoặc từ khóa không khớp).
     *
     * @param tuKhoa     từ khóa người dùng nhập
     * @param phuongThuc phương thức thanh toán được chọn
     */
    private void xuLyTraCuu(String tuKhoa, String phuongThuc) {
        boolean coKetQua = "Tất cả".equals(phuongThuc) || "Tiền mặt".equals(phuongThuc);
        if (coKetQua) {
            winChinh.setScreen(new CashReceiptPanel(
                    phuongThuc,
                    this::moManHinhTraCuu,
                    () -> { /* xem chi tiết chứng từ — tích hợp sau */ }));
        } else {
            winChinh.setScreen(new DocumentNotFoundPanel(e -> moManHinhTraCuu()));
        }
    }

    /**
     * Tạo panel giữ chỗ cho màn hình chưa tích hợp.
     *
     * @param icon    emoji hoặc ký tự đại diện
     * @param tieuDe  tên màn hình
     * @param ghiChu  mô tả ngắn về nội dung sẽ được tích hợp
     * @return panel placeholder
     */
    private javax.swing.JPanel placeholder(String icon, String tieuDe, String ghiChu) {
        javax.swing.JPanel pnl = new javax.swing.JPanel(new java.awt.BorderLayout());
        pnl.setBackground(ui.component.UiTheme.WINDOW);

        javax.swing.JPanel center = new javax.swing.JPanel();
        center.setLayout(new javax.swing.BoxLayout(center, javax.swing.BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.setBorder(javax.swing.BorderFactory.createEmptyBorder(40, 0, 0, 0));

        javax.swing.JLabel lblIcon = new javax.swing.JLabel(icon, javax.swing.SwingConstants.CENTER);
        lblIcon.setFont(ui.component.UiTheme.HEADING_FONT.deriveFont(48.0f));
        lblIcon.setAlignmentX(CENTER_ALIGNMENT);

        javax.swing.JLabel lblTitle = new javax.swing.JLabel(tieuDe, javax.swing.SwingConstants.CENTER);
        lblTitle.setFont(ui.component.UiTheme.HEADING_FONT.deriveFont(java.awt.Font.BOLD, 20.0f));
        lblTitle.setForeground(ui.component.UiTheme.TEXT);
        lblTitle.setAlignmentX(CENTER_ALIGNMENT);

        javax.swing.JLabel lblNote = new javax.swing.JLabel(ghiChu, javax.swing.SwingConstants.CENTER);
        lblNote.setFont(ui.component.UiTheme.BODY_FONT);
        lblNote.setForeground(ui.component.UiTheme.MUTED);
        lblNote.setAlignmentX(CENTER_ALIGNMENT);

        center.add(lblIcon);
        center.add(javax.swing.Box.createVerticalStrut(16));
        center.add(lblTitle);
        center.add(javax.swing.Box.createVerticalStrut(8));
        center.add(lblNote);

        javax.swing.JPanel wrap = new javax.swing.JPanel(new java.awt.BorderLayout());
        wrap.setOpaque(false);
        wrap.add(center, java.awt.BorderLayout.NORTH);
        pnl.add(wrap, java.awt.BorderLayout.CENTER);
        return pnl;
    }

    // ── Hành động hệ thống ───────────────────────────────────────────────────

    /**
     * Hỏi xác nhận rồi đóng cửa sổ và mở lại màn hình đăng nhập để chọn vai trò khác.
     * Đổi vai trò yêu cầu đăng nhập lại vì mỗi vai trò có shell và sidebar riêng.
     */
    private void xuLyDoiVaiTro() {
        int ketQua = JOptionPane.showConfirmDialog(
                winChinh,
                "Bạn muốn đổi sang vai trò khác?\nPhiên làm việc hiện tại sẽ kết thúc.",
                "Đổi vai trò",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (ketQua == JOptionPane.YES_OPTION) {
            SwingUtilities.invokeLater(() -> {
                winChinh.dispose();
                khiDangXuat.run();
            });
        }
    }

    /**
     * Hỏi xác nhận rồi kết thúc phiên và quay màn hình đăng nhập.
     */
    private void xuLyDangXuat() {
        int ketQua = JOptionPane.showConfirmDialog(
                winChinh,
                "Bạn có chắc muốn đăng xuất?",
                "Đăng xuất",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (ketQua == JOptionPane.YES_OPTION) {
            SwingUtilities.invokeLater(() -> {
                winChinh.dispose();
                khiDangXuat.run();
            });
        }
    }
}
