package ui.component;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicToggleButtonUI;

import ui.screen.ShellPage;
@SuppressWarnings("serial") // Các callback điều hướng chỉ dùng trong lần chạy, không lưu bằng serialization.
public final class SidebarPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final Map<ShellPage, JToggleButton> navigationButtons = new EnumMap<>(ShellPage.class);
    private final ButtonGroup navigationGroup = new ButtonGroup();
    private final Consumer<ShellPage> onNavigate;

    public SidebarPanel(String roleLabel, Consumer<ShellPage> onNavigate) {
        this.onNavigate = onNavigate;
        setName("sidebar");
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(UiTheme.NAVIGATION);
        setBorder(UiTheme.padding(12));
        setPreferredSize(new Dimension(UiTheme.SIDEBAR_WIDTH, 0));

        addLabel("ĐỨC DŨNG", false);
        addLabel(roleLabel, true);
        addNavigation(ShellPage.HOME);
        addNavigation(ShellPage.EMPLOYEES);
        addNavigation(ShellPage.ACCOUNTS);
        addNavigation(ShellPage.SERVICES);
        addNavigation(ShellPage.PARTS);
        addUnavailable("Phiếu nhập kho", "Chức năng nhập kho sẽ được tích hợp sau.", false);
        addUnavailable("Thống kê", "Chức năng thống kê sẽ được tích hợp sau.", false);
        addNavigation(ShellPage.AUDIT);
        addUnavailable("Đổi vai trò", "Chức năng đổi vai trò sẽ được bổ sung sau.", true);
        addUnavailable("Đăng xuất", "Chức năng đăng nhập sẽ được bổ sung sau.", true);
        add(Box.createVerticalGlue());
    }

    public void selectPage(ShellPage page) {
        navigationButtons.get(page).setSelected(true);
        navigationButtons.forEach((item, button) -> {
            button.setBackground(item == page ? UiTheme.SELECTION : UiTheme.NAVIGATION);
            button.setFont(item == page ? UiTheme.STRONG : UiTheme.BODY);
        });
    }

    private void addLabel(String text, boolean caption) {
        JLabel label = new JLabel(text);
        label.setFont(caption ? UiTheme.CAPTION : UiTheme.STRONG);
        label.setForeground(caption ? UiTheme.MUTED : UiTheme.TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(label);
        add(Box.createVerticalStrut(UiTheme.SPACE_SMALL));
    }

    private void addNavigation(ShellPage page) {
        JToggleButton button = new JToggleButton(page.getNavigationLabel());
        button.setName("nav." + page.name());
        button.setUI(new BasicToggleButtonUI());
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBackground(UiTheme.NAVIGATION);
        button.setForeground(UiTheme.TEXT);
        button.setToolTipText(page.getTitle());
        button.setBorder(navigationBorder(UiTheme.NAVIGATION));
        button.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                button.setBorder(navigationBorder(UiTheme.PRIMARY));
            }

            @Override
            public void focusLost(FocusEvent event) {
                button.setBorder(navigationBorder(UiTheme.NAVIGATION));
            }
        });
        button.addActionListener(event -> onNavigate.accept(page));
        navigationGroup.add(button);
        navigationButtons.put(page, button);
        addControl(button);
    }

    private void addUnavailable(String text, String explanation, boolean outlined) {
        JButton button = new JButton(text);
        button.setUI(new BasicButtonUI());
        button.setEnabled(false);
        button.setToolTipText(explanation);
        button.setHorizontalAlignment(outlined ? SwingConstants.CENTER : SwingConstants.LEFT);
        button.setBackground(outlined ? UiTheme.SURFACE : UiTheme.NAVIGATION);
        button.setBorder(navigationBorder(outlined ? UiTheme.BORDER : UiTheme.NAVIGATION));
        addControl(button);
    }

    private void addControl(AbstractButton button) {
        Dimension size = new Dimension(UiTheme.SIDEBAR_WIDTH - 24, UiTheme.CONTROL_HEIGHT);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
        add(button);
        add(Box.createVerticalStrut(UiTheme.SPACE_SMALL));
    }

    private Border navigationBorder(java.awt.Color color) {
        return BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(color),
                BorderFactory.createEmptyBorder(0, 7, 0, 7));
    }
}
