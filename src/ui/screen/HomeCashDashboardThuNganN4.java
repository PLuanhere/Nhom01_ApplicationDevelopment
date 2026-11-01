package ui.screen;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Màn hình Trang chủ của vai trò Thu ngân.
 *
 * <p>Hiển thị dashboard tổng quan ca thu, biểu đồ doanh thu theo ngày và lối vào
 * các công việc chính. Bấm vào cột biểu đồ hoặc ngày để xem chi tiết giao dịch.</p>
 *
 * <p>Chỉ chứa GUI và dữ liệu minh họa; không kết nối DAO hoặc cơ sở dữ liệu.
 * Dữ liệu thật sẽ được nối qua DAO khi tích hợp.</p>
 */
public class HomeCashDashboardThuNganN4 extends JPanel {

    private static final long serialVersionUID = 1L;

    // ── Màu sắc ─────────────────────────────────────────────────────────────
    private static final Color WINDOW   = new Color(240, 240, 240);
    private static final Color SURFACE  = Color.WHITE;
    private static final Color BORDER   = new Color(184, 194, 207);
    private static final Color NAV      = new Color(233, 238, 244);
    private static final Color SELECTED = new Color(220, 234, 254);
    private static final Color PRIMARY  = new Color(36, 95, 168);
    private static final Color TEXT     = new Color(32, 43, 56);
    private static final Color MUTED    = new Color(90, 104, 120);

    // ── Font ────────────────────────────────────────────────────────────────
    private static final Font BODY    = new Font("Noto Sans", Font.PLAIN, 13);
    private static final Font CAPTION = new Font("Noto Sans", Font.PLAIN, 11);
    private static final Font SECTION = new Font("Noto Sans", Font.BOLD, 14);

    // ── Hằng số điều hướng nội bộ ───────────────────────────────────────────
    private static final String DASHBOARD = "dashboard";
    private static final String DETAIL    = "detail";

    private static final DateTimeFormatter DAY_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM");
    /** Không dùng trực tiếp trong UI hiện tại, giữ để mở rộng định dạng số. */
    @SuppressWarnings("unused")
    private static final NumberFormat MONEY =
            NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

    // ── Thành phần dùng chung giữa dashboard và màn hình chi tiết ────────────
    private final CardLayout rootLayout  = new CardLayout();
    private final JPanel     rootCards   = new JPanel(rootLayout);
    private final JLabel     detailTitle = new JLabel("Chi tiết doanh thu đã thu");
    private final JLabel     detailStatus = new JLabel(" ");
    private final JPanel detailStateView  = new JPanel(new BorderLayout(8, 8));
    private final JPanel detailResultView = new JPanel(new BorderLayout(8, 8));
    private final JTable detailTable      = new JTable();

    // ── Dữ liệu minh họa biểu đồ ────────────────────────────────────────────
    private final String[] chartDays   = {"01/10", "02/10", "03/10", "04/10", "05/10"};
    private final int[]    chartValues = {0, 0, 1720, 0, 4220};
    private final JPanel   chart       = createRevenueChart();

    // ── Trạng thái màn hình ──────────────────────────────────────────────────
    private LocalDate   selectedDate = LocalDate.of(2026, 10, 5);
    private DetailState detailState  = DetailState.RESULT;
    private String      detailMessage = "";

    /**
     * Trạng thái hiển thị của màn hình chi tiết: kết quả, rỗng hoặc lỗi.
     * Dùng chung cho cùng một view để không tạo nhiều panel riêng.
     */
    public enum DetailState {
        /** Có dữ liệu; hiển thị bảng giao dịch. */
        RESULT,
        /** Ngày không có giao dịch. */
        EMPTY,
        /** Lỗi tải dữ liệu; hiển thị nút thử lại. */
        ERROR
    }

    /** Callback yêu cầu shell mở DashDetailCashierPanel. */
    private final Runnable khiMoChiTiet;
    /** Callback yêu cầu shell mở PendingInvoicesPanel. */
    private final Runnable khiMoPhieuCho;

    /**
     * Tạo panel màn hình Trang chủ Thu ngân; gọi trên EDT.
     * Khi bấm cột biểu đồ sẽ dùng màn hình chi tiết nội bộ (CardLayout).
     */
    public HomeCashDashboardThuNganN4() {
        this(null, null);
    }

    /**
     * Tạo panel màn hình Trang chủ Thu ngân với callback mở màn hình chi tiết bên ngoài.
     *
     * @param khiMoChiTiet callback do shell cung cấp để mở {@link DashDetailCashierPanel};
     *                     null thì bấm biểu đồ dùng màn hình chi tiết nội bộ.
     */
    public HomeCashDashboardThuNganN4(Runnable khiMoChiTiet) {
        this(khiMoChiTiet, null);
    }

    /**
     * Tạo panel màn hình Trang chủ Thu ngân với đầy đủ callback điều hướng.
     *
     * @param khiMoChiTiet  callback mở {@link DashDetailCashierPanel}; null = nội bộ
     * @param khiMoPhieuCho callback mở {@link PendingInvoicesPanel}; null = no-op
     */
    public HomeCashDashboardThuNganN4(Runnable khiMoChiTiet, Runnable khiMoPhieuCho) {
        this.khiMoChiTiet  = khiMoChiTiet;
        this.khiMoPhieuCho = khiMoPhieuCho;
        setLayout(new BorderLayout());
        setBackground(WINDOW);
        setMinimumSize(new Dimension(900, 560));

        buildDetailView();

        rootCards.setBackground(WINDOW);
        rootCards.add(buildDashboard(), DASHBOARD);
        rootCards.add(buildDetailScreen(), DETAIL);

        add(rootCards, BorderLayout.CENTER);
        showDashboard();
    }

    // ── Build dashboard ──────────────────────────────────────────────────────

    private JPanel buildDashboard() {
        JPanel dashboard = new JPanel(new BorderLayout(0, 0));
        dashboard.setBackground(WINDOW);

        JPanel page = new JPanel(new BorderLayout(0, 0));
        page.setBackground(WINDOW);
        page.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        // NORTH: tiêu đề trang
        page.add(buildDashboardHeading(), BorderLayout.NORTH);

        // NORTH của CENTER: tất cả card xếp thẳng đứng bằng GridBagLayout.
        // fill=HORIZONTAL + weightx=1 → mỗi card giãn full chiều ngang.
        // weighty=0 cho mọi card → tất cả co vừa nội dung, dồn lên trên.
        // CENTER và SOUTH của page bỏ trống.
        JPanel pnlCards = buildCardStack();
        JPanel pnlNorth = new JPanel(new BorderLayout());
        pnlNorth.setBackground(WINDOW);
        pnlNorth.add(pnlCards, BorderLayout.NORTH);
        // CENTER và SOUTH của page để trống.
        page.add(pnlNorth, BorderLayout.CENTER);

        dashboard.add(page, BorderLayout.CENTER);
        return dashboard;
    }

    /**
     * Xếp metricRow, chartCard, noticeCard, workEntryCard thành một cột
     * bằng GridBagLayout: mỗi card giãn full chiều ngang, co vừa chiều cao,
     * dồn lên trên; khoảng cách 8 px giữa các card qua {@code insets.bottom}.
     *
     * @return panel chứa 4 card, đặt vào NORTH của page
     */
    private JPanel buildCardStack() {
        JPanel stack = new JPanel(new java.awt.GridBagLayout());
        stack.setBackground(WINDOW);

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.gridx   = 0;
        gbc.fill    = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;

        // metricRow — khoảng cách dưới 8 px
        gbc.gridy  = 0;
        gbc.insets = new java.awt.Insets(0, 0, 8, 0);
        stack.add(buildMetricRow(), gbc);

        // chartCard
        gbc.gridy  = 1;
        stack.add(buildChartCard(), gbc);

        // noticeCard
        gbc.gridy  = 2;
        stack.add(buildNoticeCard(), gbc);

        // workEntryCard — không cần khoảng cách dưới
        gbc.gridy  = 3;
        gbc.insets = new java.awt.Insets(0, 0, 0, 0);
        stack.add(buildWorkEntryCard(), gbc);

        return stack;
    }

    private JPanel buildDashboardHeading() {
        JPanel heading = new JPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.setBackground(WINDOW);

        JLabel title = new JLabel("Trang chủ • Thu ngân");
        title.setFont(new Font("Noto Sans", Font.BOLD, 22));
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel("Dashboard thu tiền trong ca • Ngày làm việc 05/10/2026");
        subtitle.setFont(CAPTION);
        subtitle.setForeground(TEXT);

        heading.add(title);
        heading.add(Box.createVerticalStrut(2));
        heading.add(subtitle);
        heading.add(Box.createVerticalStrut(8));
        return heading;
    }

    private JPanel buildMetricRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 8, 0));
        row.setBackground(WINDOW);
        row.add(metricCard("Chờ thu tiền",  "3",            "Phiếu sửa chữa hoàn tất"));
        row.add(metricCard("Đã thu",        "5.940.000 ₫",  "Kỳ 01–05/10/2026"));
        row.add(metricCard("Tiền mặt",      "3.370.000 ₫",  "Tiền thực nhận"));
        row.add(metricCard("Chuyển khoản",  "2.570.000 ₫",  "Giao dịch đã đối chiếu"));
        return row;
    }

    /**
     * Tạo thẻ chỉ số đơn với nhãn, giá trị và ghi chú ngữ cảnh.
     *
     * @param label   nhãn chỉ số
     * @param value   giá trị hiển thị
     * @param context ghi chú kỳ/phạm vi
     * @return panel thẻ chỉ số
     */
    private JPanel metricCard(String label, String value, String context) {
        JPanel card = borderedCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 13, 10, 13)));

        JLabel lblLabel   = new JLabel(label);
        lblLabel.setFont(CAPTION);
        lblLabel.setForeground(TEXT);

        JLabel lblValue   = new JLabel(value);
        lblValue.setFont(new Font("Noto Sans", Font.BOLD, value.length() > 12 ? 19 : 22));
        lblValue.setForeground(TEXT);

        JLabel lblContext = new JLabel(context);
        lblContext.setFont(CAPTION);
        lblContext.setForeground(TEXT);

        card.add(lblLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(lblValue);
        card.add(Box.createVerticalStrut(2));
        card.add(lblContext);
        return card;
    }

    private JPanel buildChartCard() {
        JPanel card = borderedCard();
        card.setLayout(new BorderLayout(0, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 13, 10, 13)));

        JLabel heading = new JLabel("Doanh thu đã thu theo ngày (nghìn đồng)");
        heading.setFont(SECTION);
        heading.setForeground(TEXT);

        JPanel north = new JPanel();
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.setOpaque(false);
        north.add(heading);

        JLabel hint = new JLabel("Bấm vào cột hoặc ngày để xem chi tiết.");
        hint.setFont(CAPTION);
        hint.setForeground(PRIMARY);
        north.add(Box.createVerticalStrut(3));
        north.add(hint);

        card.add(north, BorderLayout.NORTH);
        card.add(chart, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildNoticeCard() {
        JPanel card = borderedCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 13, 12, 13)));

        JLabel title = new JLabel("Tóm tắt cần chú ý");
        title.setFont(SECTION);
        title.setForeground(TEXT);

        JLabel message = new JLabel(
                "Có 3 phiếu hoàn tất cần đối chiếu chi phí. "
                + "Chỉ xác nhận thanh toán sau khi nhận đủ tiền và lưu thành công.");
        message.setFont(BODY);
        message.setForeground(TEXT);

        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(message);
        return card;
    }

    private JPanel buildWorkEntryCard() {
        JPanel card = borderedCard();
        card.setLayout(new BorderLayout(0, 7));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 13, 12, 13)));

        JLabel title = new JLabel("Lối vào công việc");
        title.setFont(SECTION);
        title.setForeground(TEXT);

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));

        JButton btnPhieuCho = button("Phiếu chờ thanh toán");
        if (khiMoPhieuCho != null) {
            btnPhieuCho.addActionListener(e -> khiMoPhieuCho.run());
        }
        actions.add(btnPhieuCho);
        actions.add(Box.createHorizontalStrut(8));
        actions.add(button("Tra cứu chứng từ"));
        actions.add(Box.createHorizontalStrut(8));
        actions.add(button("Đối chiếu thanh toán"));

        card.add(title,   BorderLayout.NORTH);
        card.add(actions, BorderLayout.CENTER);
        return card;
    }


    // ── Build màn hình chi tiết ──────────────────────────────────────────────

    /** Khởi tạo các sub-panel dùng chung cho màn hình chi tiết. */
    private void buildDetailView() {
        detailResultView.setBackground(SURFACE);
        detailResultView.setBorder(BorderFactory.createLineBorder(BORDER));

        detailTable.setModel(createDetailTableModel(selectedDate));
        detailTable.setRowHeight(28);
        detailTable.setFont(BODY);
        detailTable.getTableHeader().setFont(new Font("Noto Sans", Font.BOLD, 12));
        detailTable.setFillsViewportHeight(true);
        detailResultView.add(new JScrollPane(detailTable), BorderLayout.CENTER);

        detailStatus.setFont(CAPTION);
        detailStatus.setForeground(MUTED);
        detailResultView.add(detailStatus, BorderLayout.SOUTH);

        detailStateView.setOpaque(false);
        detailStateView.add(detailResultView, BorderLayout.CENTER);
    }

    private JPanel buildDetailScreen() {
        JPanel screen = new JPanel(new BorderLayout(0, 10));
        screen.setBackground(WINDOW);
        screen.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // ── Heading ──
        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);

        JPanel labels = new JPanel();
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
        labels.setOpaque(false);

        detailTitle.setFont(new Font("Noto Sans", Font.BOLD, 22));
        detailTitle.setForeground(TEXT);
        JLabel subtitle = new JLabel("Chi tiết các khoản đã thu trong ngày được chọn");
        subtitle.setFont(CAPTION);
        subtitle.setForeground(MUTED);

        labels.add(detailTitle);
        labels.add(Box.createVerticalStrut(3));
        labels.add(subtitle);

        JButton btnBack = button("← Quay lại dashboard");
        btnBack.addActionListener(e -> showDashboard());

        heading.add(labels,  BorderLayout.WEST);
        heading.add(btnBack, BorderLayout.EAST);

        // ── Bộ lọc ──
        JPanel filters = new JPanel();
        filters.setOpaque(false);
        filters.setLayout(new BoxLayout(filters, BoxLayout.X_AXIS));

        JLabel dateLabel = new JLabel("Ngày:");
        dateLabel.setFont(BODY);

        JComboBox<String> cmbDate = new JComboBox<>(new String[]{
            "01/10/2026", "02/10/2026", "03/10/2026", "04/10/2026", "05/10/2026"});
        cmbDate.setSelectedItem("05/10/2026");
        cmbDate.addActionListener(e -> {
            Object sel = cmbDate.getSelectedItem();
            if (sel != null) {
                selectedDate = LocalDate.parse(
                        sel.toString(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                detailTitle.setText("Chi tiết doanh thu đã thu • "
                        + selectedDate.format(DAY_FORMAT) + "/2026");
                refreshDetailState();
            }
        });

        JLabel lblTotal = new JLabel("Tổng đã thu: 4.220.000 ₫");
        lblTotal.setFont(new Font("Noto Sans", Font.BOLD, 14));
        lblTotal.setForeground(TEXT);

        filters.add(dateLabel);
        filters.add(Box.createHorizontalStrut(8));
        filters.add(cmbDate);
        filters.add(Box.createHorizontalStrut(18));
        filters.add(lblTotal);

        // ── Content ──
        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setOpaque(false);
        content.add(filters,        BorderLayout.NORTH);
        content.add(detailStateView, BorderLayout.CENTER);

        screen.add(heading, BorderLayout.NORTH);
        screen.add(content, BorderLayout.CENTER);
        return screen;
    }

    // ── Trạng thái chi tiết ──────────────────────────────────────────────────

    /**
     * Chuyển màn hình chi tiết sang trạng thái chỉ định và làm mới giao diện.
     * An toàn khi gọi từ ngoài EDT.
     *
     * @param state   trạng thái hiển thị mới
     * @param message thông báo bổ sung; {@code null} hoặc trống thì dùng mặc định
     * @throws IllegalArgumentException nếu {@code state} là null
     */
    public void setDetailState(DetailState state, String message) {
        if (state == null) {
            throw new IllegalArgumentException("Trạng thái chi tiết không được null.");
        }
        if (!javax.swing.SwingUtilities.isEventDispatchThread()) {
            javax.swing.SwingUtilities.invokeLater(() -> setDetailState(state, message));
            return;
        }
        detailState   = state;
        detailMessage = message == null ? "" : message.trim();
        refreshDetailState();
    }

    private void refreshDetailState() {
        detailStateView.removeAll();

        if (detailState == DetailState.RESULT) {
            detailTable.setModel(createDetailTableModel(selectedDate));
            detailStateView.add(detailResultView, BorderLayout.CENTER);

            String total = selectedDate.getDayOfMonth() == 3 ? "1.720.000 ₫" : "4.220.000 ₫";
            int count = detailTable.getRowCount();
            detailStatus.setText("Dữ liệu minh họa • Ngày " + selectedDate.format(DAY_FORMAT)
                    + "/2026 • " + count + " giao dịch • Tổng: " + total);
        } else {
            JPanel msgPanel = new JPanel(new BorderLayout(0, 8));
            msgPanel.setBackground(SURFACE);
            msgPanel.setBorder(BorderFactory.createEmptyBorder(24, 16, 24, 16));

            String heading = detailState == DetailState.EMPTY
                    ? "Không có giao dịch đã thu trong ngày này"
                    : "Chưa tải được chi tiết doanh thu";
            String fallback = detailState == DetailState.EMPTY
                    ? "Hãy chọn ngày khác hoặc quay lại dashboard."
                    : "Kiểm tra kết nối rồi thử tải lại.";
            String body = detailMessage.isBlank() ? fallback : detailMessage;

            JLabel lblMsg = new JLabel(
                    "<html><b>" + heading + "</b><br>" + escapeHtml(body) + "</html>");
            lblMsg.setFont(BODY);
            lblMsg.setForeground(detailState == DetailState.ERROR
                    ? new Color(166, 45, 45) : MUTED);
            lblMsg.setHorizontalAlignment(SwingConstants.CENTER);
            msgPanel.add(lblMsg, BorderLayout.CENTER);

            if (detailState == DetailState.ERROR) {
                JButton btnRetry = button("Thử lại");
                btnRetry.addActionListener(e -> setDetailState(DetailState.RESULT, ""));
                JPanel actions = new JPanel();
                actions.setOpaque(false);
                actions.add(btnRetry);
                msgPanel.add(actions, BorderLayout.SOUTH);
            }
            detailStateView.add(msgPanel, BorderLayout.CENTER);
        }

        detailStateView.revalidate();
        detailStateView.repaint();
    }

    // ── Tạo model bảng chi tiết ──────────────────────────────────────────────

    /**
     * Tạo model bảng giao dịch cho ngày được chọn (dữ liệu minh họa).
     *
     * @param date ngày cần hiển thị
     * @return model chỉ đọc với dữ liệu tương ứng
     */
    private DefaultTableModel createDetailTableModel(LocalDate date) {
        Object[][] rows;
        if (date.getDayOfMonth() == 3) {
            rows = new Object[][]{
                {"HD-2026-0103", "Khách hàng A", "Tiền mặt", "1.720.000 ₫", "Đã thu"}
            };
        } else {
            rows = new Object[][]{
                {"HD-2026-0105", "Khách hàng B", "Chuyển khoản", "2.000.000 ₫", "Đã thu"},
                {"HD-2026-0106", "Khách hàng C", "Tiền mặt",     "2.220.000 ₫", "Đã thu"}
            };
        }
        return new DefaultTableModel(
                rows,
                new Object[]{"Mã hóa đơn", "Khách hàng", "Phương thức", "Số tiền", "Trạng thái"}) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    // ── Điều hướng nội bộ ────────────────────────────────────────────────────

    private void openDetail(LocalDate date) {
        selectedDate = date;
        if (khiMoChiTiet != null) {
            khiMoChiTiet.run();
            return;
        }
        // Fallback nội bộ khi dùng độc lập.
        detailTitle.setText("Chi tiết doanh thu đã thu • "
                + date.format(DAY_FORMAT) + "/2026");
        int day = date.getDayOfMonth();
        if (day == 1 || day == 2 || day == 4) {
            setDetailState(DetailState.EMPTY, "Hôm đó chưa ghi nhận khoản thu nào.");
        } else {
            setDetailState(DetailState.RESULT, "");
        }
        rootLayout.show(rootCards, DETAIL);
    }

    private void showDashboard() {
        rootLayout.show(rootCards, DASHBOARD);
    }

    // ── Biểu đồ doanh thu ────────────────────────────────────────────────────

    /** Index cột đang được hover; -1 khi không hover cột nào. */
    private int hoveredBarIndex = -1;

    /** Dữ liệu chi tiết minh họa tương ứng từng ngày trong biểu đồ. */
    private static final String[][] CHART_DETAIL = {
        // {ngày, số GD, tổng tiền}
        {"01/10/2026", "0", "0 ₫"},
        {"02/10/2026", "0", "0 ₫"},
        {"03/10/2026", "1", "1.720.000 ₫"},
        {"04/10/2026", "0", "0 ₫"},
        {"05/10/2026", "2", "4.220.000 ₫"},
    };

    /**
     * Tạo biểu đồ cột vẽ trực tiếp bằng Graphics2D.
     * Hover vào cột → highlight + hiện popup chi tiết ngay trên biểu đồ.
     * Bấm vào cột → mở màn hình chi tiết.
     *
     * @return panel biểu đồ đã gắn MouseListener và MouseMotionListener
     */
    private JPanel createRevenueChart() {
        JPanel chartPanel = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int left   = 18;
                int right  = 18;
                int bottom = 25;
                int baseY  = getHeight() - bottom;
                int chartH = Math.max(40, baseY - 20);
                int colW   = (getWidth() - left - right) / chartDays.length;
                int max    = 4600;

                // ── Vẽ cột ──────────────────────────────────────────────────
                for (int i = 0; i < chartDays.length; i++) {
                    int centerX = left + i * colW + colW / 2;
                    int barW    = Math.min(64, Math.max(28, colW / 2));
                    int barH    = chartValues[i] == 0
                            ? 3
                            : Math.max(4, (int) (chartH * (chartValues[i] / (double) max)));
                    int barX    = centerX - barW / 2;
                    int barY    = baseY - barH;

                    // Highlight cột đang hover
                    boolean hovered = (i == hoveredBarIndex);
                    Color barColor  = hovered
                            ? PRIMARY.brighter()
                            : (chartValues[i] == 0 ? BORDER : PRIMARY);

                    // Giá trị trên cột
                    g.setColor(hovered ? PRIMARY : TEXT);
                    g.setFont(new Font("Noto Sans", Font.BOLD, 11));
                    String val = chartValues[i] == 0 ? "—" : Integer.toString(chartValues[i]);
                    g.drawString(val,
                            centerX - g.getFontMetrics().stringWidth(val) / 2,
                            Math.max(12, barY - 5));

                    // Thân cột
                    g.setColor(barColor);
                    g.fillRect(barX, barY, barW, barH);

                    // Viền nhấn khi hover
                    if (hovered) {
                        g.setColor(PRIMARY.darker());
                        g.setStroke(new BasicStroke(1.5f));
                        g.drawRect(barX, barY, barW, barH);
                    }

                    // Nhãn ngày — đậm khi hover
                    g.setColor(hovered ? PRIMARY : TEXT);
                    g.setFont(hovered
                            ? new Font("Noto Sans", Font.BOLD, 11)
                            : CAPTION);
                    g.drawString(chartDays[i],
                            centerX - g.getFontMetrics().stringWidth(chartDays[i]) / 2,
                            getHeight() - 5);
                }

                // ── Đường nền ───────────────────────────────────────────────
                g.setColor(new Color(BORDER.getRed(), BORDER.getGreen(), BORDER.getBlue(), 100));
                g.setStroke(new BasicStroke(1f));
                g.drawLine(left, baseY, getWidth() - right, baseY);

                // ── Popup chi tiết khi hover ─────────────────────────────────
                if (hoveredBarIndex >= 0 && hoveredBarIndex < CHART_DETAIL.length) {
                    paintHoverPopup(g, hoveredBarIndex, left, colW, baseY);
                }

                g.dispose();
            }

            /**
             * Vẽ card popup thông tin chi tiết ngay trên cột đang hover.
             *
             * @param g     Graphics2D đang dùng
             * @param idx   index cột đang hover
             * @param left  lề trái biểu đồ
             * @param colW  chiều rộng mỗi cột
             * @param baseY y của đường nền
             */
            private void paintHoverPopup(Graphics2D g, int idx,
                    int left, int colW, int baseY) {
                String[] d  = CHART_DETAIL[idx];
                String ngay = d[0];
                String soGD = d[1] + " giao dịch";
                String tong = "Tổng: " + d[2];

                // Tính kích thước popup — chỉ 3 dòng
                g.setFont(new Font("Noto Sans", Font.PLAIN, 11));
                java.awt.FontMetrics fm = g.getFontMetrics();
                int lineH  = fm.getHeight() + 2;
                int popupH = 3 * lineH + 16;
                int popupW = Math.max(
                        fm.stringWidth(ngay),
                        Math.max(fm.stringWidth(soGD), fm.stringWidth(tong))) + 20;

                // Vị trí: trên cột, tránh ra ngoài cạnh phải/trái
                int centerX = left + idx * colW + colW / 2;
                int px = centerX - popupW / 2;
                px = Math.max(4, Math.min(getWidth() - popupW - 4, px));
                int barH = chartValues[idx] == 0
                        ? 3
                        : Math.max(4, (int) ((baseY - 20) * (chartValues[idx] / 4600.0)));
                int py = baseY - barH - popupH - 8;
                if (py < 4) { py = 4; }

                // Nền bo góc
                g.setColor(new Color(255, 255, 255, 230));
                g.fillRoundRect(px, py, popupW, popupH, 8, 8);
                g.setColor(BORDER);
                g.setStroke(new BasicStroke(1f));
                g.drawRoundRect(px, py, popupW, popupH, 8, 8);

                // Dòng 1: ngày (đậm)
                int tx = px + 10;
                int ty = py + fm.getAscent() + 8;
                g.setFont(new Font("Noto Sans", Font.BOLD, 11));
                g.setColor(TEXT);
                g.drawString(ngay, tx, ty);
                ty += lineH;

                // Dòng 2: số giao dịch
                g.setFont(new Font("Noto Sans", Font.PLAIN, 11));
                g.setColor(MUTED);
                g.drawString(soGD, tx, ty);
                ty += lineH;

                // Dòng 3: tổng tiền (màu PRIMARY, đậm)
                g.setFont(new Font("Noto Sans", Font.BOLD, 11));
                g.setColor(PRIMARY);
                g.drawString(tong, tx, ty);
            }
        };

        chartPanel.setOpaque(false);
        chartPanel.setPreferredSize(new Dimension(700, 155));
        chartPanel.setMinimumSize(new Dimension(500, 135));
        chartPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // ── Hover: highlight + popup ─────────────────────────────────────────
        chartPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int left   = 18;
                int usable = Math.max(1, chartPanel.getWidth() - left - 18);
                int idx    = (e.getX() - left) * chartDays.length / usable;
                int newIdx = (idx >= 0 && idx < chartDays.length) ? idx : -1;
                if (newIdx != hoveredBarIndex) {
                    hoveredBarIndex = newIdx;
                    chartPanel.repaint();
                }
            }
        });

        chartPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                if (hoveredBarIndex != -1) {
                    hoveredBarIndex = -1;
                    chartPanel.repaint();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                int left   = 18;
                int usable = Math.max(1, chartPanel.getWidth() - left - 18);
                int index  = (e.getX() - left) * chartDays.length / usable;
                if (index >= 0 && index < chartDays.length) {
                    hoveredBarIndex = -1;
                    openDetail(LocalDate.of(2026, 10, index + 1));
                }
            }
        });

        return chartPanel;
    }

    // ── Tiện ích ─────────────────────────────────────────────────────────────

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
    }

    private JPanel borderedCard() {
        JPanel panel = new JPanel();
        panel.setBackground(SURFACE);
        panel.setForeground(TEXT);
        return panel;
    }

    /**
     * Tạo nút phẳng dùng chung trong dashboard.
     *
     * @param text nhãn nút
     * @return nút đã định dạng, chưa gắn sự kiện nghiệp vụ
     */
    private JButton button(String text) {
        JButton btn = new JButton(text);
        btn.setFont(BODY);
        btn.setForeground(TEXT);
        btn.setBackground(SURFACE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Phương thức không còn dùng sau khi tích hợp vào MainFrame ────────────
    // buildNavigation(), buildWindowChrome(), buildStatusBar() đã bỏ:
    // sidebar và chrome do MainFrame + SidebarPanel quản lý.
    // navItem() và navButton() cũng bỏ theo.
}
