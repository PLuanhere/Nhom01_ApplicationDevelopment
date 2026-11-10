package ui.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.Scrollable;

/**
 * Panel hỗ trợ cuộn dọc mượt mà, tự động bám theo chiều rộng của viewport.
 * Ngăn giao diện bị cắt khi thu nhỏ cửa sổ và dùng chung cho các màn hình dài.
 */
public class ScrollablePanel extends JPanel implements Scrollable {

    private static final long serialVersionUID = 1L;

    /** Tạo panel cuộn với layout BorderLayout mặc định và nền trong suốt. */
    public ScrollablePanel() {
        this(new BorderLayout());
    }

    /** Tạo panel cuộn với layout chỉ định và nền trong suốt. */
    public ScrollablePanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 20;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return Math.max(20, visibleRect.height - 20);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
