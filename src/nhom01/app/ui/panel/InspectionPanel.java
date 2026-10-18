package nhom01.app.ui.panel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import nhom01.app.service.InspectionService;

public class InspectionPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final InspectionService inspectionService;
    private final CardLayout contentLayout;
    private final JPanel contentPanel;
    private final CardLayout sidebarLayout;
    private final JPanel sidebarPanel;

    private JLabel windowTitleLabel;
    private JLabel statusLabel;
    private JLabel accountLabel;
    private InspectionWorkflowPanel workflowPanel;

    public InspectionPanel() {
        inspectionService = new InspectionService();
        contentLayout = new CardLayout();
        contentPanel = new JPanel(contentLayout);
        sidebarLayout = new CardLayout();
        sidebarPanel = new JPanel(sidebarLayout);

        setLayout(new BorderLayout());
        setBackground(new Color(240, 240, 240));
        setPreferredSize(new Dimension(1280, 800));

        JPanel topArea = createTopArea();
        JPanel statusBar = createStatusBar();
        JPanel centerArea = createCenterArea();

        add(topArea, BorderLayout.NORTH);
        add(centerArea, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }

    // Tạo vùng title bar và menu phía trên
    private JPanel createTopArea() {
        JPanel panel = new JPanel(new BorderLayout());

        panel.setBackground(new Color(240, 240, 240));
        panel.add(createTitleBar(), BorderLayout.NORTH);
        panel.add(createMenuBarPanel(), BorderLayout.CENTER);

        return panel;
    }

    // Tạo thanh tiêu đề theo thiết kế
    private JPanel createTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());

        bar.setBackground(Color.WHITE);
        bar.setPreferredSize(new Dimension(1280, 32));
        bar.setBorder(new EmptyBorder(0, 6, 0, 6));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 9));

        left.setOpaque(false);

        JPanel icon = new JPanel();

        icon.setBackground(new Color(249, 115, 22));
        icon.setPreferredSize(new Dimension(14, 14));

        windowTitleLabel = new JLabel("Garage Ô tô Đức Dũng — Kỹ thuật");

        windowTitleLabel.setFont(new Font("Noto Sans", Font.PLAIN, 13));
        windowTitleLabel.setForeground(new Color(32, 43, 56));

        left.add(icon);
        left.add(windowTitleLabel);

        JLabel controls = new JLabel("—   □   ×", SwingConstants.RIGHT);

        controls.setFont(new Font("Noto Sans", Font.PLAIN, 13));
        controls.setForeground(new Color(90, 104, 120));
        controls.setPreferredSize(new Dimension(96, 32));

        bar.add(left, BorderLayout.WEST);
        bar.add(controls, BorderLayout.EAST);

        return bar;
    }

    // Tạo thanh menu của ứng dụng
    private JPanel createMenuBarPanel() {
        JMenuBar menuBar = new JMenuBar();

        menuBar.setBackground(new Color(240, 240, 240));
        menuBar.setBorder(null);
        menuBar.setPreferredSize(new Dimension(1280, 26));

        menuBar.add(createMenu("Hệ thống"));
        menuBar.add(createMenu("Danh mục"));
        menuBar.add(createMenu("Nghiệp vụ"));
        menuBar.add(createMenu("Báo cáo"));
        menuBar.add(createMenu("Trợ giúp"));

        JPanel panel = new JPanel(new BorderLayout());

        panel.setBackground(new Color(240, 240, 240));
        panel.add(menuBar, BorderLayout.CENTER);

        return panel;
    }

    // Tạo một mục trên menu chính
    private JMenu createMenu(String text) {
        JMenu menu = new JMenu(text);

        menu.setFont(new Font("Noto Sans", Font.PLAIN, 13));
        menu.setForeground(new Color(32, 43, 56));
        menu.setPreferredSize(new Dimension(108, 26));
        menu.setBorder(new EmptyBorder(0, 3, 0, 3));

        return menu;
    }

    // Tạo sidebar và hai nhóm màn hình của feature inspection
    private JPanel createCenterArea() {
        JPanel center = new JPanel(new BorderLayout());

        center.setBackground(new Color(240, 240, 240));
        center.add(createSidebarContainer(), BorderLayout.WEST);

        InspectionReportPanel reportPanel = new InspectionReportPanel(
                inspectionService,
                this::showWorkflow);

        workflowPanel = new InspectionWorkflowPanel(
                inspectionService,
                this::showReports,
                this::showReceptionShell,
                this::showTechnicalShell);

        contentPanel.add(reportPanel, "REPORTS");
        contentPanel.add(workflowPanel, "WORKFLOW");

        center.add(contentPanel, BorderLayout.CENTER);

        contentLayout.show(contentPanel, "REPORTS");

        return center;
    }

    // Tạo vùng sidebar có hai trạng thái Kỹ thuật và Tiếp nhận
    private JPanel createSidebarContainer() {
        sidebarPanel.setPreferredSize(new Dimension(180, 716));

        sidebarPanel.add(createTechnicalSidebar(), "TECH");
        sidebarPanel.add(createReceptionSidebar(), "RECEPTION");

        sidebarLayout.show(sidebarPanel, "TECH");

        return sidebarPanel;
    }

    // Tạo sidebar dành cho kỹ thuật viên
    private JPanel createTechnicalSidebar() {
        JPanel sidebar = createSidebarBase("Kỹ thuật");

        addSidebarItem(sidebar, "Trang chủ", false, null);
        addSidebarItem(sidebar, "Công việc được giao", false, null);
        addSidebarItem(sidebar, "Báo cáo kiểm tra", true, e -> showReports());
        addSidebarItem(sidebar, "Tiến độ sửa chữa", false, null);
        addSidebarItem(sidebar, "Phụ tùng sử dụng", false, null);
        addSidebarItem(sidebar, "Đổi vai trò", false, null);
        addSidebarItem(sidebar, "Đăng xuất", false, null);

        return sidebar;
    }

    // Tạo sidebar Tiếp nhận cho trạng thái bàn giao kết quả
    private JPanel createReceptionSidebar() {
        JPanel sidebar = createSidebarBase("Tiếp nhận");

        addSidebarItem(sidebar, "Trang chủ", false, null);
        addSidebarItem(sidebar, "Khách hàng", false, null);
        addSidebarItem(sidebar, "Phương tiện", false, null);
        addSidebarItem(sidebar, "Phiếu tiếp nhận", false, null);
        addSidebarItem(sidebar, "Báo giá", true, null);
        addSidebarItem(sidebar, "Lệnh sửa chữa", false, null);
        addSidebarItem(sidebar, "Đổi vai trò", false, null);
        addSidebarItem(sidebar, "Đăng xuất", false, null);

        return sidebar;
    }

    // Tạo phần đầu chung của sidebar
    private JPanel createSidebarBase(String role) {
        JPanel sidebar = new JPanel();

        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(233, 238, 244));
        sidebar.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel brand = new JLabel("ĐỨC DŨNG");

        brand.setFont(new Font("Noto Sans", Font.BOLD, 14));
        brand.setForeground(new Color(32, 43, 56));
        brand.setAlignmentX(LEFT_ALIGNMENT);

        JLabel roleLabel = new JLabel(role);

        roleLabel.setFont(new Font("Noto Sans", Font.PLAIN, 11));
        roleLabel.setForeground(new Color(90, 104, 120));
        roleLabel.setAlignmentX(LEFT_ALIGNMENT);

        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(roleLabel);
        sidebar.add(Box.createVerticalStrut(8));

        return sidebar;
    }

    // Thêm một nút điều hướng vào sidebar
    private void addSidebarItem(
            JPanel sidebar,
            String text,
            boolean selected,
            java.awt.event.ActionListener action) {
        JButton button = createButton(text, false, false, 156);

        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(new Font("Noto Sans", selected ? Font.BOLD : Font.PLAIN, 13));
        button.setForeground(new Color(32, 43, 56));
        button.setBackground(selected ? new Color(220, 234, 254) : new Color(233, 238, 244));
        button.setBorder(new EmptyBorder(5, 8, 5, 8));
        button.setMaximumSize(new Dimension(156, 30));
        button.setAlignmentX(LEFT_ALIGNMENT);

        if (action != null) {
            button.addActionListener(action);
        }

        sidebar.add(button);
        sidebar.add(Box.createVerticalStrut(4));
    }

    // Tạo thanh trạng thái cuối cửa sổ
    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());

        bar.setBackground(new Color(240, 240, 240));
        bar.setBorder(new EmptyBorder(3, 3, 3, 3));
        bar.setPreferredSize(new Dimension(1280, 22));

        statusLabel = new JLabel("Sẵn sàng");

        statusLabel.setFont(new Font("Noto Sans", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(32, 43, 56));

        accountLabel = new JLabel(
                "Tài khoản: demo.nhom01   |   Vai trò: Kỹ thuật",
                SwingConstants.RIGHT);

        accountLabel.setFont(new Font("Noto Sans", Font.PLAIN, 11));
        accountLabel.setForeground(new Color(90, 104, 120));

        bar.add(statusLabel, BorderLayout.WEST);
        bar.add(accountLabel, BorderLayout.CENTER);

        return bar;
    }

    // Chuyển về nhóm màn quản lý báo cáo
    private void showReports() {
        showTechnicalShell();
        contentLayout.show(contentPanel, "REPORTS");
    }

    // Chuyển sang nhóm luồng kiểm tra xe
    private void showWorkflow() {
        showTechnicalShell();
        workflowPanel.showQueuePage();
        contentLayout.show(contentPanel, "WORKFLOW");
    }

    // Chuyển shell về vai trò Kỹ thuật
    private void showTechnicalShell() {
        windowTitleLabel.setText("Garage Ô tô Đức Dũng — Kỹ thuật");
        sidebarLayout.show(sidebarPanel, "TECH");
        accountLabel.setText("Tài khoản: demo.nhom01   |   Vai trò: Kỹ thuật");
        statusLabel.setText("Sẵn sàng");
    }

    // Chuyển shell sang vai trò Tiếp nhận
    private void showReceptionShell() {
        windowTitleLabel.setText("Garage Ô tô Đức Dũng — Tiếp nhận");
        sidebarLayout.show(sidebarPanel, "RECEPTION");
        accountLabel.setText("Tài khoản: demo.nhom01   |   Vai trò: Tiếp nhận");
        statusLabel.setText("Đã chuyển kết quả cho nhân viên tiếp nhận.");
    }

    // Tạo khung chuẩn cho một trang nghiệp vụ
    static JPanel createBusinessPage(JLabel title, JLabel subtitle, JPanel body, JPanel footer) {
        JPanel page = new JPanel(new BorderLayout());

        page.setBackground(new Color(240, 240, 240));

        JPanel header = new JPanel();

        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(new Color(240, 240, 240));
        header.setBorder(new EmptyBorder(16, 16, 0, 16));
        header.setPreferredSize(new Dimension(100, 80));

        title.setAlignmentX(LEFT_ALIGNMENT);
        subtitle.setAlignmentX(LEFT_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);

        JPanel wrapper = new JPanel(new BorderLayout());

        wrapper.setBackground(new Color(240, 240, 240));
        wrapper.setBorder(new EmptyBorder(0, 16, 0, 16));

        JScrollPane scroll = new JScrollPane(body);

        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 231)));
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        wrapper.add(scroll, BorderLayout.CENTER);

        page.add(header, BorderLayout.NORTH);
        page.add(wrapper, BorderLayout.CENTER);

        if (footer != null) {
            page.add(footer, BorderLayout.SOUTH);
        }

        return page;
    }

    // Tạo vùng nội dung màu trắng có padding
    static JPanel createScrollContent() {
        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        return panel;
    }

    // Tạo footer chứa nhóm nút bên phải
    static JPanel createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));

        footer.setBackground(new Color(240, 240, 240));
        footer.setBorder(new EmptyBorder(0, 16, 0, 16));
        footer.setPreferredSize(new Dimension(100, 64));

        return footer;
    }

    // Tạo card có viền và padding
    static JPanel createCard(int height, Color background) {
        JPanel panel = new JPanel();

        panel.setBackground(background);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 231)),
                new EmptyBorder(8, 8, 8, 8)));

        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setPreferredSize(new Dimension(100, height));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));

        return panel;
    }

    // Tạo tiêu đề chính của trang
    static JLabel createPageTitle(String text) {
        JLabel label = new JLabel(text);

        label.setFont(new Font("Noto Sans", Font.BOLD, 20));
        label.setForeground(new Color(32, 43, 56));

        return label;
    }

    // Tạo tiêu đề của section
    static JLabel createSectionTitle(String text) {
        JLabel label = new JLabel(text);

        label.setFont(new Font("Noto Sans", Font.BOLD, 14));
        label.setForeground(new Color(32, 43, 56));
        label.setAlignmentX(LEFT_ALIGNMENT);

        return label;
    }

    // Tạo subtitle hoặc ghi chú phụ
    static JLabel createSubtitle(String text) {
        JLabel label = new JLabel("<html>" + text + "</html>");

        label.setFont(new Font("Noto Sans", Font.PLAIN, 12));
        label.setForeground(new Color(90, 104, 120));

        return label;
    }

    // Tạo label nhỏ
    static JLabel createSmallLabel(String text) {
        JLabel label = new JLabel(text);

        label.setFont(new Font("Noto Sans", Font.PLAIN, 12));
        label.setForeground(new Color(32, 43, 56));

        return label;
    }

    // Tạo label hiển thị giá trị
    static JLabel createValueLabel(String text) {
        JLabel label = new JLabel("<html>" + text + "</html>");

        label.setFont(new Font("Noto Sans", Font.PLAIN, 13));
        label.setForeground(new Color(32, 43, 56));

        return label;
    }

    // Tạo JButton theo style chung
    static JButton createButton(String text, boolean primary, boolean danger, int width) {
        JButton button = new JButton(text);

        button.setUI(new BasicButtonUI());
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFont(new Font("Noto Sans", Font.PLAIN, 13));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(width, 30));
        button.setBackground(primary ? new Color(36, 95, 168) : Color.WHITE);

        button.setForeground(
                primary ? Color.WHITE
                        : danger ? new Color(179, 38, 30) : new Color(32, 43, 56));

        button.setBorder(BorderFactory.createLineBorder(
                primary ? new Color(36, 95, 168)
                        : danger ? new Color(179, 38, 30) : new Color(184, 194, 207)));

        return button;
    }

    // Tạo combobox theo style chung
    static JComboBox<String> createComboBox(String[] values) {
        JComboBox<String> combo = new JComboBox<>(values);

        combo.setUI(new BasicComboBoxUI());
        combo.setFont(new Font("Noto Sans", Font.PLAIN, 13));
        combo.setForeground(new Color(32, 43, 56));
        combo.setBackground(Color.WHITE);
        combo.setBorder(BorderFactory.createLineBorder(new Color(184, 194, 207)));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        return combo;
    }

    // Áp dụng style chung cho JTextField
    static void styleTextField(JTextField field) {
        field.setFont(new Font("Noto Sans", Font.PLAIN, 13));
        field.setForeground(new Color(32, 43, 56));
        field.setBackground(Color.WHITE);

        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(184, 194, 207)),
                new EmptyBorder(4, 8, 4, 8)));

        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
    }

    // Tạo JTable theo style chung
    static JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);

        table.setFont(new Font("Noto Sans", Font.PLAIN, 13));
        table.setForeground(new Color(32, 43, 56));
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(220, 234, 254));
        table.setSelectionForeground(new Color(32, 43, 56));
        table.setRowHeight(30);
        table.setGridColor(new Color(220, 225, 231));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);

        DefaultTableCellRenderer header = new DefaultTableCellRenderer();

        header.setOpaque(true);
        header.setBackground(new Color(240, 240, 240));
        header.setForeground(new Color(32, 43, 56));
        header.setFont(new Font("Noto Sans", Font.BOLD, 13));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 231)),
                new EmptyBorder(0, 8, 0, 8)));

        table.getTableHeader().setDefaultRenderer(header);
        table.getTableHeader().setPreferredSize(new Dimension(100, 30));
        table.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer cell = new DefaultTableCellRenderer();

        cell.setBorder(new EmptyBorder(0, 8, 0, 8));

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cell);
        }

        return table;
    }

    // Bọc JTable trong JScrollPane
    static JScrollPane createTableScroll(JTable table, int height) {
        JScrollPane scroll = new JScrollPane(table);

        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 231)));
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setPreferredSize(new Dimension(100, height));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        return scroll;
    }

    // Chạy riêng feature inspection trong lúc chưa có MainFrame chung
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();

            frame.setUndecorated(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new InspectionPanel());
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setVisible(true);
        });
    }
}