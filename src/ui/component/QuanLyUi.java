package ui.component;

import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.LayoutManager;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

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

    /** Đầu trang Page 3; nút quay lại xuống hàng khi cửa sổ hẹp. */
    public static JPanel taoDauTrang(String breadcrumb, String tieuDe, String moTa, JButton... cacNut) {
        JPanel pnl = new JPanel(new BorderLayout(0, 12));
        pnl.setOpaque(false);
        pnl.add(taoNhan(breadcrumb, 11.5f, false, UiTheme.APP_MUTED), BorderLayout.NORTH);
        JPanel pnlChu = new JPanel(new BorderLayout(0, 5));
        pnlChu.setOpaque(false);
        pnlChu.add(taoNhan(tieuDe, 24, true, UiTheme.APP_TEXT), BorderLayout.NORTH);
        pnlChu.add(taoNhan(moTa, 12, false, UiTheme.APP_MUTED));
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.setOpaque(false);
        for (JButton nut : cacNut) { pnlNut.add(nut); }
        int rongCacNut = pnlNut.getPreferredSize().width;
        JPanel pnlHang = new JPanel(new BorderLayout(12, 10));
        pnlHang.setOpaque(false);
        pnlHang.add(pnlChu);
        pnl.add(pnlHang);
        if (cacNut.length == 0) { return pnl; }
        pnlHang.add(pnlNut, BorderLayout.EAST);
        pnlHang.addComponentListener(new ComponentAdapter() {
            private boolean xepDoc;

            @Override
            public void componentResized(ComponentEvent event) {
                boolean canXepDoc = pnlHang.getWidth() < pnlChu.getPreferredSize().width
                        + rongCacNut + 20;
                if (xepDoc != canXepDoc) {
                    xepDoc = canXepDoc;
                    pnlHang.remove(pnlNut);
                    pnlHang.add(pnlNut, xepDoc ? BorderLayout.SOUTH : BorderLayout.EAST);
                    pnlHang.revalidate();
                }
            }
        });
        return pnl;
    }

    /** Tạo nút cao 36 px theo Page 3. */
    public static JButton taoNut(String chu, boolean chinh) {
        JButton nut = UiTheme.createAppButton(chu, chinh);
        nut.setFont(UiTheme.APP_FONT.deriveFont(12.0f));
        nut.setPreferredSize(new Dimension(nut.getPreferredSize().width, 36));
        return nut;
    }

    /** Tạo ô nhập; ô chỉ đọc vẫn hiển thị rõ chữ. */
    public static JTextField taoONhap(boolean choSua) {
        JTextField txt = new JTextField();
        txt.setEditable(choSua);
        dinhDangO(txt);
        if (!choSua) { txt.setBackground(new Color(0xF5F7FB)); }
        return txt;
    }

    /** Font, chiều cao và viền của trường nhập liệu. */
    public static void dinhDangO(JComponent oNhap) {
        oNhap.setFont(UiTheme.APP_FONT.deriveFont(14.0f));
        oNhap.setPreferredSize(new Dimension(0, 42));
        oNhap.setBackground(Color.WHITE);
        if (!(oNhap instanceof JComboBox<?>)) {
            oNhap.setBorder(UiTheme.roundedBorder(UiTheme.APP_BORDER, 6, 0, 12));
        }
    }

    /** Tạo nhãn nằm trên trường nhập và tên hỗ trợ đọc màn hình. */
    public static JPanel taoTruong(String nhan, JComponent oNhap) {
        JPanel pnl = new JPanel(new BorderLayout(0, 6));
        pnl.setOpaque(false);
        JLabel lbl = taoNhan(nhan, 12, true, UiTheme.APP_MUTED);
        lbl.setLabelFor(oNhap);
        oNhap.getAccessibleContext().setAccessibleName(nhan);
        pnl.add(lbl, BorderLayout.NORTH);
        pnl.add(oNhap);
        return pnl;
    }

    /** Ghép hai trường bằng nhau; chiều rộng do cửa sổ quyết định. */
    public static JPanel taoHang(JComponent trai, JComponent phai) {
        JPanel pnl = new JPanel(new GridLayout(1, 2, 14, 0));
        pnl.setOpaque(false);
        pnl.add(trai);
        pnl.add(phai);
        return pnl;
    }

    /** Thanh nút cố định dưới form, giúp luôn thấy thao tác chính khi cuộn nội dung. */
    public static JPanel taoChanTrang(JLabel thongBao, JButton... cacNut) {
        JPanel pnl = taoThe(new BorderLayout(0, 8));
        pnl.setBorder(BorderFactory.createCompoundBorder(pnl.getBorder(),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        thongBao.setVisible(!thongBao.getText().isBlank());
        pnl.add(thongBao, BorderLayout.NORTH);
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.setOpaque(false);
        for (JButton nut : cacNut) { pnlNut.add(nut); }
        pnl.add(pnlNut);
        return pnl;
    }

    /** Dòng lưu ý có xuống hàng trong cửa sổ hẹp. */
    public static JLabel taoLuuY(String chu) {
        JLabel lbl = new JLabel("<html>" + chu + "</html>") {
            private static final long serialVersionUID = 1L;

            @Override public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                if (getWidth() > 0 && getClientProperty(BasicHTML.propertyKey) instanceof View html) {
                    html.setSize(Math.max(1, getWidth() - getInsets().left - getInsets().right), 0);
                    size.height = (int) Math.ceil(html.getPreferredSpan(View.Y_AXIS))
                            + getInsets().top + getInsets().bottom;
                }
                return size;
            }

            @Override public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }

            @Override public void setBounds(int x, int y, int rong, int cao) {
                boolean doiRong = rong != getWidth();
                super.setBounds(x, y, rong, cao);
                if (doiRong) {
                    getPreferredSize();
                    if (getParent() != null) { getParent().invalidate(); }
                    revalidate();
                }
            }
        };
        lbl.setFont(UiTheme.APP_FONT.deriveFont(12.0f));
        lbl.setForeground(new Color(0x806326));
        lbl.setOpaque(true);
        lbl.setBackground(new Color(0xFFF6D9));
        lbl.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        return lbl;
    }

    /** Nội dung form luôn theo chiều rộng cửa sổ, chỉ cuộn dọc khi thiếu chiều cao. */
    public static JScrollPane taoVungCuon(JPanel noiDung) {
        JPanel pnlBoc = new NoiDungCuon();
        pnlBoc.add(noiDung, BorderLayout.NORTH);
        JScrollPane scr = new JScrollPane(pnlBoc);
        scr.setBorder(BorderFactory.createEmptyBorder());
        scr.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scr.getViewport().setBackground(UiTheme.APP_BACKGROUND);
        scr.getVerticalScrollBar().setUnitIncrement(24);
        return scr;
    }

    /** JScrollPane cần biết form bám chiều rộng viewport thay vì chiều rộng của các nhãn. */
    private static final class NoiDungCuon extends JPanel implements Scrollable {
        private static final long serialVersionUID = 1L;
        private NoiDungCuon() { super(new BorderLayout()); setOpaque(false); }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int huong, int chieu) { return 24; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int huong, int chieu) { return r.height - 24; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

}
