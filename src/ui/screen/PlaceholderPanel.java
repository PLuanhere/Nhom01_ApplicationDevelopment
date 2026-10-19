package ui.screen;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import ui.component.UiTheme;
/** Giữ vùng nội dung cho các phần UI tiếp theo, không giả lập chức năng nghiệp vụ. */
public final class PlaceholderPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    public PlaceholderPanel(ShellPage page) {
        super(new BorderLayout(0, UiTheme.CONTENT_PADDING));
        setName("page." + page.name());
        setBorder(UiTheme.padding(UiTheme.CONTENT_PADDING));

        JLabel lblTitle = new JLabel(page.getTitle());
        lblTitle.setName("pageTitle." + page.name());
        lblTitle.setFont(UiTheme.HEADING);
        add(lblTitle, BorderLayout.NORTH);

        JPanel pnlContent = new JPanel(new BorderLayout());
        pnlContent.setBackground(UiTheme.SURFACE);
        pnlContent.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));
        JLabel lblPlaceholder = new JLabel("Nội dung đang được chuẩn bị.", SwingConstants.CENTER);
        lblPlaceholder.setForeground(UiTheme.MUTED);
        pnlContent.add(lblPlaceholder, BorderLayout.CENTER);
        add(pnlContent, BorderLayout.CENTER);
    }
}
