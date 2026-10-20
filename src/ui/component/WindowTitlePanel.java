package ui.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Thanh tiêu đề trắng theo Figma; chỉ chứa các thao tác cửa sổ của hệ điều hành. */
public final class WindowTitlePanel extends JPanel {

    /** Phiên bản tuần tự hóa của thành phần Swing. */
    private static final long serialVersionUID = 1L;

    /**
     * Tạo tiêu đề và nút thu nhỏ, phóng to, đóng cho cửa sổ chủ.
     * @param winChu cửa sổ nhận thao tác; phải được khởi tạo trên EDT
     */
    public WindowTitlePanel(JFrame winChu) {
        this(winChu, false);
    }

    /**
     * Tạo thanh tiêu đề đầy đủ hoặc bản gọn cho màn hình đăng nhập.
     * @param winChu cửa sổ nhận thao tác trên EDT
     * @param thuGon true để chỉ hiện tên cửa sổ và nút đóng, false để hiện đủ điều khiển
     */
    public WindowTitlePanel(JFrame winChu, boolean thuGon) {
        super(new BorderLayout(UiTheme.SPACE_MEDIUM, 0));
        setBackground(UiTheme.SURFACE);
        setPreferredSize(new Dimension(0, UiTheme.TITLE_HEIGHT));
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        // Icon và kiểu chữ giữ đúng tỷ lệ 14 px / 13 pt trong thanh tiêu đề Figma.
        JLabel lblTieuDe = new JLabel(winChu.getTitle(),
                thuGon ? null : new ImageIcon(winChu.getIconImage()), JLabel.LEFT);
        lblTieuDe.setFont(UiTheme.SHELL_FONT);
        lblTieuDe.setForeground(UiTheme.TEXT);
        lblTieuDe.setIconTextGap(UiTheme.SPACE_MEDIUM);
        add(lblTieuDe, BorderLayout.CENTER);

        // Đây là điều khiển cửa sổ, không phải các chức năng nghiệp vụ trong sidebar.
        JPanel pnlNutCuaSo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlNutCuaSo.setOpaque(false);
        if (thuGon) {
            // Giữ vị trí nút đóng theo hàng tiêu đề đăng nhập trong Figma.
            pnlNutCuaSo.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
            pnlNutCuaSo.setPreferredSize(new Dimension(58, 20));
        }
        if (!thuGon) {
            // Bản đăng nhập cố định kích thước nên không có nút thu nhỏ/phóng to.
            JButton btnThuNho = createWindowButton("—", "Thu nhỏ cửa sổ");
            JButton btnPhongTo = createWindowButton("□", "Phóng to / khôi phục cửa sổ");
            btnThuNho.addActionListener(event -> winChu.setExtendedState(winChu.getExtendedState() | Frame.ICONIFIED));
            btnPhongTo.addActionListener(event -> toggleMaximized(winChu));
            pnlNutCuaSo.add(btnThuNho);
            pnlNutCuaSo.add(btnPhongTo);
        }
        // Nút đóng dùng chung cho cả hai kiểu tiêu đề.
        JButton btnDong = createWindowButton("×", "Đóng cửa sổ");
        btnDong.addActionListener(event -> winChu.dispatchEvent(new WindowEvent(winChu, WindowEvent.WINDOW_CLOSING)));
        pnlNutCuaSo.add(btnDong);
        add(pnlNutCuaSo, BorderLayout.EAST);
    }

    /**
     * Đổi giữa phóng to và kích thước trước đó; dùng cho nút và nhấp đôi tiêu đề.
     * @param winChu cửa sổ cần đổi trạng thái
     */
    static void toggleMaximized(JFrame winChu) {
        if ((winChu.getExtendedState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
            winChu.setExtendedState(Frame.NORMAL);
        } else {
            winChu.setExtendedState(Frame.MAXIMIZED_BOTH);
        }
    }

    /**
     * Tạo nút nhỏ, phẳng, không có viền hoặc hiệu ứng nổi của Metal Look and Feel.
     * @param kyHieu ký hiệu điều khiển cửa sổ
     * @param moTa mô tả tooltip và nhãn hỗ trợ truy cập
     * @return nút chưa gắn hành động
     */
    private JButton createWindowButton(String kyHieu, String moTa) {
        // Mỗi nút nằm trong hàng cao 20 px, dành tổng cộng 90 px như Figma.
        JButton btnCuaSo = new JButton(kyHieu);
        btnCuaSo.setPreferredSize(new Dimension(30, 20));
        // Font hệ thống bổ sung các ký hiệu cửa sổ không có trong bộ Noto Sans văn bản.
        btnCuaSo.setFont(new Font(Font.DIALOG, Font.PLAIN, 13));
        btnCuaSo.setForeground(UiTheme.MUTED);
        btnCuaSo.setBorder(BorderFactory.createEmptyBorder());
        btnCuaSo.setContentAreaFilled(false);
        btnCuaSo.setFocusPainted(false);
        btnCuaSo.setToolTipText(moTa);
        btnCuaSo.getAccessibleContext().setAccessibleName(moTa);
        return btnCuaSo;
    }
}
