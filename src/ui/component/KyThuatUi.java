package ui.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

// Thành phần trình bày của dashboard, dùng font và màu của UiTheme chung.
public final class KyThuatUi {
    public static final int LINE_HEIGHT = 20;

    private KyThuatUi() {
    }

    /** @return viền thẻ với khoảng đệm theo thiết kế */
    // Ghép đường viền và khoảng đệm của thẻ nội dung.
    public static Border taoVienThe() {
        return BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.BORDER),
                BorderFactory.createEmptyBorder(16, 16, 16, 16));
    }

    /** @return thẻ nền trắng có bố cục co giãn */
    // Tạo thẻ nội dung chung cho dashboard và hộp thoại.
    public static JPanel taoThe() {
        JPanel pnlThe = new JPanel(new BorderLayout(8, 8));
        pnlThe.setBackground(UiTheme.SURFACE);
        pnlThe.setBorder(taoVienThe());
        return pnlThe;
    }

    /**
     * @param noiDung nội dung hiển thị
     * @param font font của nhãn
     * @return nhãn có kích thước đủ hiển thị chữ và dấu
     */
    // Tính chiều cao theo font thực tế để chữ và dấu tiếng Việt không bị cắt.
    public static JLabel taoNhan(String noiDung, Font font) {
        JLabel lblNoiDung = new JLabel(noiDung);
        lblNoiDung.setFont(font);
        lblNoiDung.setForeground(UiTheme.TEXT);
        Dimension kichThuoc = lblNoiDung.getPreferredSize();
        kichThuoc.height = Math.max(LINE_HEIGHT, lblNoiDung.getFontMetrics(font).getHeight() + 4);
        lblNoiDung.setPreferredSize(kichThuoc);
        return lblNoiDung;
    }

    /**
     * @param nhan nội dung nút
     * @param chieuRong chiều rộng thiết kế
     * @return nút đã định dạng
     */
    // Dùng định dạng nút của leader cho các thao tác trên dashboard.
    public static JButton taoNut(String nhan, int chieuRong) {
        JButton btnThaoTac = new JButton(nhan);
        UiTheme.styleButton(btnThaoTac, false);
        btnThaoTac.setPreferredSize(new Dimension(chieuRong, 32));
        return btnThaoTac;
    }
}