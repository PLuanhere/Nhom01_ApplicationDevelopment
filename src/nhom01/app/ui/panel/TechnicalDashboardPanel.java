package nhom01.app.ui.panel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import nhom01.app.model.TechnicalDashboardData;
import nhom01.app.model.TechnicalDashboardData.CompletionDetail;
import nhom01.app.model.TechnicalDashboardData.DailyCompletion;
import nhom01.app.model.TechnicalDashboardData.WorkGroupStat;
import nhom01.app.service.TechnicalDashboardService;

public class TechnicalDashboardPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final String DASHBOARD_PAGE = "DASHBOARD";
    private static final String DETAIL_PAGE = "DETAIL";

    private final TechnicalDashboardService dashboardService;
    private final TechnicalDashboardData dashboardData;
    private final CardLayout contentLayout;
    private final JPanel contentPanel;
    private final JPanel detailPanel;

    public TechnicalDashboardPanel() {
        dashboardService = new TechnicalDashboardService();
        dashboardData = dashboardService.getDashboardData();

        contentLayout = new CardLayout();
        contentPanel = new JPanel(contentLayout);
        detailPanel = new JPanel(new BorderLayout());

        setLayout(new BorderLayout());
        setBackground(new Color(240, 240, 240));

        add(createTopArea(), BorderLayout.NORTH);
        add(createMainArea(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);
    }

    // Tạo phần đầu màn hình gồm thanh tiêu đề và menu
    private JPanel createTopArea() {
        JPanel topArea = new JPanel();
        topArea.setLayout(new BoxLayout(topArea, BoxLayout.Y_AXIS));
        topArea.add(createTitleBar());
        topArea.add(createMenuBarPanel());
        return topArea;
    }

    private JPanel createTitleBar() {
        JPanel titleBar = new JPanel();
        titleBar.setLayout(new BoxLayout(titleBar, BoxLayout.X_AXIS));
        titleBar.setBackground(Color.WHITE);
        titleBar.setPreferredSize(new Dimension(0, 32));
        titleBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        titleBar.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel brandMark = new JPanel();
        brandMark.setBackground(new Color(249, 115, 22));
        brandMark.setPreferredSize(new Dimension(16, 16));
        brandMark.setMinimumSize(new Dimension(16, 16));
        brandMark.setMaximumSize(new Dimension(16, 16));

        JLabel title = new JLabel("Garage Ô tô Đức Dũng — Kỹ thuật");
        title.setFont(new Font("Noto Sans", Font.PLAIN, 13));
        title.setForeground(new Color(32, 43, 56));

        JLabel windowButtons = new JLabel("—   □   ×");
        windowButtons.setFont(new Font("Noto Sans", Font.PLAIN, 13));
        windowButtons.setForeground(new Color(90, 104, 120));

        titleBar.add(brandMark);
        titleBar.add(Box.createHorizontalStrut(8));
        titleBar.add(title);
        titleBar.add(Box.createHorizontalGlue());
        titleBar.add(windowButtons);

        return titleBar;
    }

    private JPanel createMenuBarPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(240, 240, 240));
        wrapper.setPreferredSize(new Dimension(0, 28));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(240, 240, 240));
        menuBar.setBorder(new EmptyBorder(0, 4, 0, 4));

        menuBar.add(createMenu("Hệ thống"));
        menuBar.add(createMenu("Danh mục"));
        menuBar.add(createMenu("Nghiệp vụ"));
        menuBar.add(createMenu("Báo cáo"));
        menuBar.add(createMenu("Trợ giúp"));

        wrapper.add(menuBar, BorderLayout.CENTER);
        return wrapper;
    }

    private JMenu createMenu(String text) {
        JMenu menu = new JMenu(text);
        menu.setFont(new Font("Noto Sans", Font.PLAIN, 13));
        menu.setForeground(new Color(32, 43, 56));
        menu.setPreferredSize(new Dimension(92, 24));
        return menu;
    }

    // Tạo khu vực chính gồm sidebar và vùng nội dung thay đổi bằng CardLayout
    private JPanel createMainArea() {
        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(new Color(240, 240, 240));

        contentPanel.setBackground(new Color(240, 240, 240));
        contentPanel.add(createDashboardPage(), DASHBOARD_PAGE);
        contentPanel.add(detailPanel, DETAIL_PAGE);

        mainArea.add(createSidebar(), BorderLayout.WEST);
        mainArea.add(contentPanel, BorderLayout.CENTER);

        return mainArea;
    }

    // Tạo sidebar dành cho nhân viên kỹ thuật
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(233, 238, 244));
        sidebar.setPreferredSize(new Dimension(180, 0));
        sidebar.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel brand = new JLabel("ĐỨC DŨNG");
        brand.setFont(new Font("Noto Sans", Font.BOLD, 14));
        brand.setForeground(new Color(32, 43, 56));
        brand.setAlignmentX(LEFT_ALIGNMENT);

        JLabel role = new JLabel("Kỹ thuật");
        role.setFont(new Font("Noto Sans", Font.PLAIN, 11));
        role.setForeground(new Color(90, 104, 120));
        role.setAlignmentX(LEFT_ALIGNMENT);

        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(role);
        sidebar.add(Box.createVerticalStrut(8));

        sidebar.add(createNavigationButton("Trang chủ", true, this::showDashboardPage));
        sidebar.add(Box.createVerticalStrut(4));

        sidebar.add(createNavigationButton("Công việc được giao", false, null));
        sidebar.add(Box.createVerticalStrut(4));

        sidebar.add(createNavigationButton("Báo cáo kiểm tra", false, null));
        sidebar.add(Box.createVerticalStrut(4));

        sidebar.add(createNavigationButton("Tiến độ sửa chữa", false, null));
        sidebar.add(Box.createVerticalStrut(4));

        sidebar.add(createNavigationButton("Phụ tùng sử dụng", false, null));
        sidebar.add(Box.createVerticalStrut(4));

        sidebar.add(createSidebarButton("Đổi vai trò", null));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(createSidebarButton("Đăng xuất", null));

        return sidebar;
    }

    private JButton createNavigationButton(String text, boolean selected, Runnable action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Noto Sans", selected ? Font.BOLD : Font.PLAIN, 13));
        button.setForeground(new Color(32, 43, 56));
        button.setBackground(selected ? new Color(220, 234, 254) : new Color(233, 238, 244));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(LEFT_ALIGNMENT);
        button.setPreferredSize(new Dimension(156, 32));
        button.setMaximumSize(new Dimension(156, 32));

        if (action != null) {
            button.addActionListener(e -> action.run());
        }

        return button;
    }

    private JButton createSidebarButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Noto Sans", Font.PLAIN, 13));
        button.setForeground(new Color(32, 43, 56));
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(LEFT_ALIGNMENT);
        button.setPreferredSize(new Dimension(156, 32));
        button.setMaximumSize(new Dimension(156, 32));

        if (action != null) {
            button.addActionListener(e -> action.run());
        }

        return button;
    }

    // Tạo trang Dashboard chính theo thiết kế Figma
    private JScrollPane createDashboardPage() {
        JPanel dashboard = new JPanel();
        dashboard.setLayout(new BoxLayout(dashboard, BoxLayout.Y_AXIS));
        dashboard.setBackground(new Color(240, 240, 240));
        dashboard.setBorder(new EmptyBorder(16, 16, 16, 16));

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        JLabel title = new JLabel("Trang chủ • Kỹ thuật");
        title.setFont(new Font("Noto Sans", Font.BOLD, 22));
        title.setForeground(new Color(32, 43, 56));
        title.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Dashboard công việc cá nhân • " + dashboardData.getTechnicianName()
                + " • " + dashboardData.getDashboardDate().format(dateFormatter));
        subtitle.setFont(new Font("Noto Sans", Font.PLAIN, 12));
        subtitle.setForeground(new Color(32, 43, 56));
        subtitle.setAlignmentX(LEFT_ALIGNMENT);

        dashboard.add(title);
        dashboard.add(Box.createVerticalStrut(4));
        dashboard.add(subtitle);
        dashboard.add(Box.createVerticalStrut(12));
        dashboard.add(createMetricRow());
        dashboard.add(Box.createVerticalStrut(8));
        dashboard.add(createChartCard());
        dashboard.add(Box.createVerticalStrut(8));
        dashboard.add(createNoticeCard());
        dashboard.add(Box.createVerticalStrut(8));
        dashboard.add(createShortcutCard());

        JScrollPane scrollPane = new JScrollPane(dashboard);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(240, 240, 240));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        return scrollPane;
    }

    private JPanel createMetricRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 8, 0));
        row.setBackground(new Color(240, 240, 240));
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 124));

        row.add(createMetricCard("Đang sửa", dashboardData.getRepairingCount(), "Lệnh của tôi"));
        row.add(createMetricCard("Chờ phụ tùng", dashboardData.getWaitingPartCount(), "Cần bổ sung vật tư"));
        row.add(createMetricCard("Chờ khách duyệt", dashboardData.getWaitingApprovalCount(),
                "Phát sinh chưa thực hiện"));
        row.add(createMetricCard("Hoàn tất", dashboardData.getCompletedCount(), "Công việc trong ngày"));

        return row;
    }

    private JPanel createMetricCard(String label, int value, String context) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(184, 194, 207)),
                new EmptyBorder(16, 16, 16, 16)));

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Noto Sans", Font.PLAIN, 11));
        lblLabel.setForeground(new Color(32, 43, 56));
        lblLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblValue = new JLabel(String.valueOf(value));
        lblValue.setFont(new Font("Noto Sans", Font.BOLD, 26));
        lblValue.setForeground(new Color(32, 43, 56));
        lblValue.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblContext = new JLabel(context);
        lblContext.setFont(new Font("Noto Sans", Font.PLAIN, 11));
        lblContext.setForeground(new Color(32, 43, 56));
        lblContext.setAlignmentX(LEFT_ALIGNMENT);

        card.add(lblLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(lblValue);
        card.add(Box.createVerticalStrut(8));
        card.add(lblContext);

        return card;
    }

    // Tạo biểu đồ và cho phép bấm từng ngày để xem chi tiết
    private JPanel createChartCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(184, 194, 207)),
                new EmptyBorder(16, 16, 16, 16)));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 244));

        JLabel title = new JLabel("Công việc đã hoàn tất theo ngày");
        title.setFont(new Font("Noto Sans", Font.BOLD, 14));
        title.setForeground(new Color(32, 43, 56));
        title.setAlignmentX(LEFT_ALIGNMENT);

        JLabel hint = new JLabel("Bấm vào cột hoặc ngày để xem chi tiết.");
        hint.setFont(new Font("Noto Sans", Font.PLAIN, 11));
        hint.setForeground(new Color(36, 95, 168));
        hint.setAlignmentX(LEFT_ALIGNMENT);

        DailyCompletionChart chart = new DailyCompletionChart(dashboardData.getDailyCompletions());
        chart.setAlignmentX(LEFT_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(hint);
        card.add(Box.createVerticalStrut(8));
        card.add(chart);

        return card;
    }

    private JPanel createNoticeCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(184, 194, 207)),
                new EmptyBorder(12, 16, 12, 16)));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JLabel title = new JLabel("Tóm tắt cần chú ý");
        title.setFont(new Font("Noto Sans", Font.BOLD, 13));
        title.setForeground(new Color(32, 43, 56));

        JLabel notice = new JLabel(dashboardData.getNotice());
        notice.setFont(new Font("Noto Sans", Font.PLAIN, 11));
        notice.setForeground(new Color(32, 43, 56));

        card.add(title, BorderLayout.NORTH);
        card.add(notice, BorderLayout.CENTER);

        return card;
    }

    private JPanel createShortcutCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(184, 194, 207)),
                new EmptyBorder(12, 16, 12, 16)));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));

        JLabel title = new JLabel("Lối vào công việc");
        title.setFont(new Font("Noto Sans", Font.BOLD, 13));
        title.setForeground(new Color(32, 43, 56));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        buttons.setBackground(Color.WHITE);
        buttons.add(createShortcutButton("Công việc được giao"));
        buttons.add(createShortcutButton("Xe chờ kiểm tra"));
        buttons.add(createShortcutButton("Báo cáo kiểm tra"));
        buttons.add(createShortcutButton("Tiến độ sửa chữa"));
        buttons.add(createShortcutButton("Đổi vai trò"));

        card.add(title, BorderLayout.NORTH);
        card.add(buttons, BorderLayout.CENTER);

        return card;
    }

    private JButton createShortcutButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Noto Sans", Font.PLAIN, 12));
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(32, 43, 56));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(152, 32));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    // Chuyển từ Dashboard sang trang chi tiết của ngày được chọn
    private void showCompletionDetail(LocalDate date) {
        CompletionDetail detail = dashboardService.findCompletionDetail(dashboardData, date);

        if (detail == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy dữ liệu chi tiết của ngày đã chọn.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        detailPanel.removeAll();
        detailPanel.add(createDetailPage(detail), BorderLayout.CENTER);
        detailPanel.revalidate();
        detailPanel.repaint();

        contentLayout.show(contentPanel, DETAIL_PAGE);
    }

    // Tạo trang chi tiết chiếm toàn bộ vùng nội dung bên phải sidebar
    private JPanel createDetailPage(CompletionDetail detail) {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(new Color(240, 240, 240));
        page.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel detailFrame = new JPanel(new BorderLayout());
        detailFrame.setBackground(new Color(240, 240, 240));
        detailFrame.setBorder(BorderFactory.createLineBorder(new Color(184, 194, 207)));

        detailFrame.add(createDetailHeader(), BorderLayout.NORTH);
        detailFrame.add(createDetailBody(detail), BorderLayout.CENTER);
        detailFrame.add(createDetailActionPanel(), BorderLayout.SOUTH);

        page.add(detailFrame, BorderLayout.CENTER);

        return page;
    }

    private JPanel createDetailHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 52));
        header.setBorder(new EmptyBorder(0, 16, 0, 16));

        JLabel title = new JLabel("Chi tiết công việc hoàn tất");
        title.setFont(new Font("Noto Sans", Font.BOLD, 18));
        title.setForeground(new Color(32, 43, 56));

        header.add(title, BorderLayout.CENTER);

        return header;
    }

    private JPanel createDetailBody(CompletionDetail detail) {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(new Color(240, 240, 240));
        body.setBorder(new EmptyBorder(12, 16, 12, 16));

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        JLabel subtitle = new JLabel("Ngày " + detail.getDate().format(dateFormatter) + " • "
                + dashboardData.getTechnicianName() + " • Tính theo ngày hoàn tất");
        subtitle.setFont(new Font("Noto Sans", Font.PLAIN, 12));
        subtitle.setForeground(new Color(32, 43, 56));
        subtitle.setAlignmentX(LEFT_ALIGNMENT);

        int total = dashboardService.calculateTotalCompleted(detail);

        body.add(subtitle);
        body.add(Box.createVerticalStrut(8));
        body.add(createDetailTotalCard(total));
        body.add(Box.createVerticalStrut(8));
        body.add(createDetailTableCard(detail, total));
        body.add(Box.createVerticalStrut(8));

        JLabel note = new JLabel("Đã hoàn tất " + total
                + " công việc trong ngày. Một xe có thể có nhiều công việc.");
        note.setFont(new Font("Noto Sans", Font.PLAIN, 13));
        note.setForeground(new Color(32, 43, 56));
        note.setAlignmentX(LEFT_ALIGNMENT);

        body.add(note);

        return body;
    }

    private JPanel createDetailTotalCard(int total) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(184, 194, 207)),
                new EmptyBorder(12, 16, 12, 16)));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));

        JLabel label = new JLabel("Tổng công việc hoàn tất");
        label.setFont(new Font("Noto Sans", Font.BOLD, 14));
        label.setForeground(new Color(32, 43, 56));
        label.setAlignmentX(LEFT_ALIGNMENT);

        JLabel value = new JLabel(total + " công việc");
        value.setFont(new Font("Noto Sans", Font.BOLD, 26));
        value.setForeground(new Color(32, 43, 56));
        value.setAlignmentX(LEFT_ALIGNMENT);

        card.add(label);
        card.add(Box.createVerticalStrut(4));
        card.add(value);

        return card;
    }

    // Tạo khu vực bảng chi tiết không sử dụng thanh cuộn
    private JPanel createDetailTableCard(CompletionDetail detail, int total) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(184, 194, 207)),
                new EmptyBorder(12, 16, 12, 16)));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 192));

        JLabel title = new JLabel("Chi tiết theo nhóm công việc");
        title.setFont(new Font("Noto Sans", Font.BOLD, 14));
        title.setForeground(new Color(32, 43, 56));
        title.setAlignmentX(LEFT_ALIGNMENT);

        JTable table = createDetailTable(detail, total);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createLineBorder(new Color(184, 194, 207)));
        tablePanel.setAlignmentX(LEFT_ALIGNMENT);
        tablePanel.setPreferredSize(new Dimension(0, 128));
        tablePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 128));

        tablePanel.add(table.getTableHeader(), BorderLayout.NORTH);
        tablePanel.add(table, BorderLayout.CENTER);

        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(tablePanel);

        return card;
    }

    private JTable createDetailTable(CompletionDetail detail, int total) {
        DefaultTableModel tableModel = new DefaultTableModel(
                new Object[] { "Nhóm công việc", "Đã hoàn tất", "Tỷ trọng" }, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        Locale vietnameseLocale = Locale.forLanguageTag("vi-VN");

        for (WorkGroupStat stat : detail.getWorkGroupStats()) {
            double percentage = dashboardService.calculatePercentage(stat.getCompletedCount(), total);

            tableModel.addRow(new Object[] {
                    stat.getWorkGroup(),
                    stat.getCompletedCount(),
                    String.format(vietnameseLocale, "%.1f%%", percentage)
            });
        }

        JTable table = new JTable(tableModel);
        table.setFont(new Font("Noto Sans", Font.PLAIN, 13));
        table.setForeground(new Color(32, 43, 56));
        table.setBackground(Color.WHITE);
        table.setRowHeight(32);
        table.setShowGrid(true);
        table.setGridColor(new Color(184, 194, 207));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setSelectionBackground(new Color(220, 234, 254));
        table.setSelectionForeground(new Color(32, 43, 56));

        table.getTableHeader().setFont(new Font("Noto Sans", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(238, 241, 245));
        table.getTableHeader().setForeground(new Color(32, 43, 56));
        table.getTableHeader().setPreferredSize(new Dimension(0, 32));
        table.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setFont(new Font("Noto Sans", Font.BOLD, 13));
        headerRenderer.setForeground(new Color(32, 43, 56));
        headerRenderer.setBackground(new Color(238, 241, 245));
        headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        headerRenderer.setBorder(BorderFactory.createMatteBorder(
                0, 0, 1, 1, new Color(184, 194, 207)));

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
        cellRenderer.setFont(new Font("Noto Sans", Font.PLAIN, 13));
        cellRenderer.setForeground(new Color(32, 43, 56));
        cellRenderer.setBackground(Color.WHITE);
        cellRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        cellRenderer.setBorder(new EmptyBorder(0, 8, 0, 8));

        for (int column = 0; column < table.getColumnCount(); column++) {
            table.getColumnModel().getColumn(column).setHeaderRenderer(headerRenderer);
            table.getColumnModel().getColumn(column).setCellRenderer(cellRenderer);
        }

        return table;
    }

    private JPanel createDetailActionPanel() {
        JPanel actions = new JPanel(new BorderLayout());
        actions.setBackground(new Color(240, 240, 240));
        actions.setPreferredSize(new Dimension(0, 64));
        actions.setBorder(new EmptyBorder(16, 16, 16, 16));

        JButton closeButton = new JButton("Đóng chi tiết");
        closeButton.setFont(new Font("Noto Sans", Font.PLAIN, 13));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(new Color(36, 95, 168));
        closeButton.setFocusPainted(false);
        closeButton.setPreferredSize(new Dimension(152, 32));
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> showDashboardPage());

        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonWrapper.setOpaque(false);
        buttonWrapper.add(closeButton);

        actions.add(buttonWrapper, BorderLayout.EAST);

        return actions;
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(Color.WHITE);
        statusBar.setPreferredSize(new Dimension(0, 24));
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(184, 194, 207)),
                new EmptyBorder(4, 8, 4, 8)));

        JLabel status = new JLabel("Sẵn sàng");
        status.setFont(new Font("Noto Sans", Font.PLAIN, 11));
        status.setForeground(new Color(90, 104, 120));

        JLabel account = new JLabel("Tài khoản: demo.nhom01   |   Vai trò: Kỹ thuật");
        account.setFont(new Font("Noto Sans", Font.PLAIN, 11));
        account.setForeground(new Color(90, 104, 120));

        statusBar.add(status, BorderLayout.WEST);
        statusBar.add(account, BorderLayout.EAST);

        return statusBar;
    }

    private void showDashboardPage() {
        contentLayout.show(contentPanel, DASHBOARD_PAGE);
    }

    private class DailyCompletionChart extends JPanel {
        private static final long serialVersionUID = 1L;

        private final List<DailyCompletion> items;
        private final DateTimeFormatter dateFormatter;

        public DailyCompletionChart(List<DailyCompletion> items) {
            this.items = items;
            dateFormatter = DateTimeFormatter.ofPattern("dd/MM");

            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(900, 156));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 156));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int selectedIndex = getSelectedIndex(e.getX());

                    if (selectedIndex >= 0 && selectedIndex < items.size()) {
                        showCompletionDetail(items.get(selectedIndex).getDate());
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (items.isEmpty()) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setFont(new Font("Noto Sans", Font.PLAIN, 11));

            int width = getWidth();
            int height = getHeight();
            int leftPadding = 12;
            int rightPadding = 12;
            int topPadding = 20;
            int bottomPadding = 28;
            int usableWidth = width - leftPadding - rightPadding;
            int columnWidth = usableWidth / items.size();
            int maxCount = getMaxCount();
            int maxBarHeight = height - topPadding - bottomPadding - 24;

            for (int i = 0; i < items.size(); i++) {
                DailyCompletion item = items.get(i);

                int centerX = leftPadding + i * columnWidth + columnWidth / 2;
                int barWidth = Math.min(80, Math.max(40, columnWidth - 32));
                int barHeight = maxCount == 0 ? 0
                        : (int) (item.getCount() / (double) maxCount * maxBarHeight);
                int barX = centerX - barWidth / 2;
                int barY = height - bottomPadding - barHeight;

                String countText = String.valueOf(item.getCount());
                int countWidth = g2.getFontMetrics().stringWidth(countText);

                g2.setColor(new Color(32, 43, 56));
                g2.drawString(countText, centerX - countWidth / 2, barY - 8);

                g2.setColor(new Color(36, 95, 168));
                g2.fillRect(barX, barY, barWidth, barHeight);

                String dateText = item.getDate().format(dateFormatter);
                int dateWidth = g2.getFontMetrics().stringWidth(dateText);

                g2.setColor(new Color(32, 43, 56));
                g2.drawString(dateText, centerX - dateWidth / 2, height - 8);
            }

            g2.dispose();
        }

        private int getSelectedIndex(int mouseX) {
            if (items.isEmpty()) {
                return -1;
            }

            int leftPadding = 12;
            int rightPadding = 12;
            int usableWidth = getWidth() - leftPadding - rightPadding;

            if (mouseX < leftPadding || mouseX > getWidth() - rightPadding) {
                return -1;
            }

            int columnWidth = usableWidth / items.size();
            int index = (mouseX - leftPadding) / Math.max(1, columnWidth);

            return Math.min(index, items.size() - 1);
        }

        private int getMaxCount() {
            int max = 0;

            for (DailyCompletion item : items) {
                if (item.getCount() > max) {
                    max = item.getCount();
                }
            }

            return max;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setUndecorated(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new TechnicalDashboardPanel());
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setVisible(true);
        });
    }
}