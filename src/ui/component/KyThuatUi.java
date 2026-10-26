package ui.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

/** Thành phần trình bày dùng cho dashboard kỹ thuật và dialog thống kê ngày. */
public final class KyThuatUi {
    /** Chiều cao dòng cơ sở của thiết kế, tính bằng pixel. */
    public static final int LINE_HEIGHT = 20;

    /** Tiện ích chỉ cung cấp phương thức tạo component. */
    private KyThuatUi() {
    }

    /** @return viền mảnh và khoảng đệm 16 px theo thiết kế */
    public static Border taoVienThe() {
        return BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.BORDER),
                BorderFactory.createEmptyBorder(16, 16, 16, 16));
    }

    /** @return thẻ nền trắng dùng Layout Manager để co giãn theo nội dung */
    public static JPanel taoThe() {
        JPanel pnlThe = new JPanel(new BorderLayout(8, 8));
        pnlThe.setBackground(UiTheme.SURFACE);
        pnlThe.setBorder(taoVienThe());
        return pnlThe;
    }

    /**
     * Chọn chiều cao theo FontMetrics để đủ chỗ cho dấu tiếng Việt.
     * @param noiDung nội dung hiển thị
     * @param font font của nhãn
     * @return nhãn có kích thước đủ hiển thị chữ và dấu
     */
    public static JLabel taoNhan(String noiDung, Font font) {
        JLabel lblNoiDung = new JLabel(noiDung);
        lblNoiDung.setFont(font);
        lblNoiDung.setForeground(UiTheme.TEXT);
        Dimension kichThuoc = lblNoiDung.getPreferredSize();
        kichThuoc.height = Math.max(LINE_HEIGHT, lblNoiDung.getFontMetrics(font).getHeight());
        lblNoiDung.setPreferredSize(kichThuoc);
        return lblNoiDung;
    }

    /**
     * Tạo nút dùng màu và font của khung chung.
     * @param nhan nội dung nút
     * @param chieuRong chiều rộng thiết kế
     * @return nút đã định dạng
     */
    public static JButton taoNut(String nhan, int chieuRong) {
        JButton btnThaoTac = new JButton(nhan);
        UiTheme.styleButton(btnThaoTac, false);
        btnThaoTac.setPreferredSize(new Dimension(chieuRong, 32));
        return btnThaoTac;
    }
}
