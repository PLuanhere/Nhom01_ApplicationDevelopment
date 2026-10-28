package ui.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.LayoutManager;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Thành phần trình bày dùng chung giữa tổng quan, biểu đồ và dialog doanh thu. */
public final class QuanLyUi {

    /** Ngăn tạo đối tượng vì lớp chỉ cung cấp định dạng giao diện. */
    private QuanLyUi() {
    }

    /**
     * Định dạng tiền theo tiếng Việt; không dùng định dạng để tính tổng.
     * @param soTien tiền VND chính xác
     * @return chuỗi có dấu phân cách và đơn vị
     */
    public static String dinhDangTien(BigDecimal soTien) {
        NumberFormat dinhDang = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        dinhDang.setMaximumFractionDigits(2);
        return dinhDang.format(soTien) + "đ";
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

    /**
     * Tạo đầu thẻ với tiêu đề và mô tả.
     * @param tieuDe tên thẻ
     * @param moTa mô tả ngắn
     * @return đầu thẻ có lề phù hợp
     */
    public static JPanel taoDauThe(String tieuDe, String moTa) {
        JPanel pnlDau = new JPanel(new BorderLayout(0, 3));
        pnlDau.setOpaque(false);
        pnlDau.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE9EDF2)),
                BorderFactory.createEmptyBorder(13, 17, 14, 17)));
        pnlDau.add(taoNhan(tieuDe, 15, true, UiTheme.APP_TEXT), BorderLayout.NORTH);
        pnlDau.add(taoNhan(moTa, 11.5f, false, UiTheme.APP_MUTED), BorderLayout.SOUTH);
        return pnlDau;
    }
}
