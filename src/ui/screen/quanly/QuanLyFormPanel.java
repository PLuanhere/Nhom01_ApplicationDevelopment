package ui.screen.quanly;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/** Giữ bố cục và căn lề chung của các biểu mẫu Quản lý. */
public class QuanLyFormPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    /** Khởi tạo bố cục biểu mẫu với vùng nội dung và thanh thao tác phía dưới. */
    protected QuanLyFormPanel() {
        super(new BorderLayout(0, 14));
        setOpaque(false);
    }

    /** Căn lề và bố trí nội dung theo chiều rộng cửa sổ. */
    @Override
    public void doLayout() {
        int le = Math.max(0, (getWidth() - 1100) / 2);
        if (getInsets().left != le) {
            setBorder(BorderFactory.createEmptyBorder(0, le, 0, le));
        }
        super.doLayout();
    }
}
