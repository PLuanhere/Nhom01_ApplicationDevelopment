package ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicMenuBarUI;

import ui.component.SidebarPanel;
import ui.component.UiTheme;
import ui.screen.PlaceholderPanel;
import ui.screen.ShellPage;
/** Khung dùng chung; các trang hiện tại chỉ là vùng giữ chỗ của UI-LUAN-01. */
public final class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel pnlCards = new JPanel(cardLayout);
    private final SidebarPanel sidebar;

    public MainFrame(String accountName, String roleLabel) {
        super("Garage Ô tô Đức Dũng — " + Objects.requireNonNull(roleLabel));
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("MainFrame phải được tạo trên EDT.");
        }
        Objects.requireNonNull(accountName);
        setName("garage.main");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setJMenuBar(createMenuBar());

        sidebar = new SidebarPanel(roleLabel, this::showPage);
        JScrollPane sidebarScroll = new JScrollPane(sidebar,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sidebarScroll.setBorder(BorderFactory.createEmptyBorder());
        sidebarScroll.setPreferredSize(new Dimension(UiTheme.SIDEBAR_WIDTH, 0));
        sidebarScroll.getVerticalScrollBar().setUnitIncrement(UiTheme.CONTROL_HEIGHT);
        add(sidebarScroll, BorderLayout.WEST);

        for (ShellPage page : ShellPage.values()) {
            pnlCards.add(new PlaceholderPanel(page), page.name());
        }
        add(pnlCards, BorderLayout.CENTER);
        add(createStatusBar(accountName, roleLabel), BorderLayout.SOUTH);
        showPage(ShellPage.HOME);

        setPreferredSize(new Dimension(1280, 800));
        setMinimumSize(new Dimension(960, 600));
        pack();
        fitToScreen();
        setLocationRelativeTo(null);
    }

    public void showPage(ShellPage page) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Điều hướng Swing phải chạy trên EDT.");
        }
        Objects.requireNonNull(page);
        cardLayout.show(pnlCards, page.name());
        sidebar.selectPage(page);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setUI(new BasicMenuBarUI());
        menuBar.setBorder(BorderFactory.createEmptyBorder(2, 3, 2, 3));

        JMenu systemMenu = createMenu("Hệ thống", KeyEvent.VK_H);
        addPageItem(systemMenu, ShellPage.HOME);
        addUnavailableItem(systemMenu, "Đổi vai trò", "Chức năng đổi vai trò sẽ được bổ sung sau.");
        addUnavailableItem(systemMenu, "Đăng xuất", "Chức năng đăng nhập sẽ được bổ sung sau.");
        systemMenu.addSeparator();
        JMenuItem exitItem = new JMenuItem("Thoát");
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_DOWN_MASK));
        exitItem.addActionListener(event -> dispose());
        systemMenu.add(exitItem);
        menuBar.add(systemMenu);

        JMenu catalogMenu = createMenu("Danh mục", KeyEvent.VK_D);
        addPageItem(catalogMenu, ShellPage.EMPLOYEES);
        addPageItem(catalogMenu, ShellPage.ACCOUNTS);
        addPageItem(catalogMenu, ShellPage.SERVICES);
        addPageItem(catalogMenu, ShellPage.PARTS);
        menuBar.add(catalogMenu);

        JMenu operationsMenu = createMenu("Nghiệp vụ", KeyEvent.VK_N);
        addUnavailableItem(operationsMenu, "Phiếu nhập kho", "Chức năng nhập kho sẽ được tích hợp sau.");
        addPageItem(operationsMenu, ShellPage.AUDIT);
        menuBar.add(operationsMenu);

        JMenu reportsMenu = createMenu("Thống kê", KeyEvent.VK_T);
        addUnavailableItem(reportsMenu, "Báo cáo và thống kê", "Chức năng thống kê sẽ được tích hợp sau.");
        menuBar.add(reportsMenu);

        JMenu helpMenu = createMenu("Trợ giúp", KeyEvent.VK_G);
        addUnavailableItem(helpMenu, "Hướng dẫn sử dụng", "Hướng dẫn sẽ được bổ sung ở phần Trợ giúp.");
        addUnavailableItem(helpMenu, "Giới thiệu ứng dụng", "Thông tin giới thiệu sẽ được bổ sung sau.");
        menuBar.add(helpMenu);
        return menuBar;
    }

    private JMenu createMenu(String text, int mnemonic) {
        JMenu menu = new JMenu(text);
        menu.setMnemonic(mnemonic);
        menu.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 18));
        return menu;
    }

    private void addPageItem(JMenu menu, ShellPage page) {
        JMenuItem item = new JMenuItem(page.getNavigationLabel());
        item.setName("menu." + page.name());
        item.addActionListener(event -> showPage(page));
        menu.add(item);
    }

    private void addUnavailableItem(JMenu menu, String text, String explanation) {
        JMenuItem item = new JMenuItem(text);
        item.setEnabled(false);
        item.setToolTipText(explanation);
        menu.add(item);
    }

    private JPanel createStatusBar(String accountName, String roleLabel) {
        JPanel statusBar = new JPanel(new BorderLayout(UiTheme.CONTENT_PADDING, 0));
        statusBar.setName("statusBar");
        statusBar.setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 4));
        JLabel lblStatus = new JLabel("Sẵn sàng");
        lblStatus.setFont(UiTheme.CAPTION);
        lblStatus.setPreferredSize(new Dimension(320, 18));
        JLabel lblSession = new JLabel("Tài khoản: " + accountName + "   |   Vai trò: " + roleLabel);
        lblSession.setName("demoSession");
        lblSession.setFont(UiTheme.CAPTION);
        lblSession.setForeground(UiTheme.MUTED);
        statusBar.add(lblStatus, BorderLayout.WEST);
        statusBar.add(lblSession, BorderLayout.CENTER);
        return statusBar;
    }

    private void fitToScreen() {
        Rectangle screen = getGraphicsConfiguration().getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
        setSize(Math.min(getWidth(), screen.width - insets.left - insets.right),
                Math.min(getHeight(), screen.height - insets.top - insets.bottom));
    }
}
