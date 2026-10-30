package ui.component;

import java.awt.Color;
import java.awt.LayoutManager;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Nhãn và thẻ dùng chung cho các màn hình Quản lý. */
public final class QuanLyUi {

    /** Ngăn tạo đối tượng vì lớp chỉ cung cấp định dạng giao diện. */
    private QuanLyUi() {
    }

    /**
     * Tạo nhãn theo font Page 3.
     * @param chu nội dung hiển thị
     * @param coChu cỡ chữ
     * @param dam có in đậm hay không
     * @param mau màu chữ
     * @return nhãn, có tooltip để đọc đầy đủ khi cửa sổ hẹp
     */
    public static JLabel taoNhan(String chu, float coChu, boolean dam, Color mau) {
        JLabel lbl = new JLabel(chu);
        lbl.setFont((dam ? UiTheme.APP_BOLD_FONT : UiTheme.APP_FONT).deriveFont(coChu));
        lbl.setForeground(mau);
        lbl.setToolTipText(chu);
        return lbl;
    }

    /**
     * Tạo thẻ trắng bo góc với viền Page 3.
     * @param layout layout manager của thẻ
     * @return panel có viền, chưa có nội dung
     */
    public static JPanel taoThe(LayoutManager layout) {
        JPanel pnl = UiTheme.roundedPanel(layout, Color.WHITE, 8);
        pnl.setBorder(UiTheme.roundedBorder(UiTheme.APP_BORDER, 8, 0, 0));
        return pnl;
    }

}
