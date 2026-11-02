package ui.component;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.JPanel;

/** Biểu đồ bảy ngày, rê chuột xem tiền và bấm cột để mở chi tiết ngày đó. */
public final class BieuDoDoanhThuPanel extends JPanel implements MouseListener, MouseMotionListener {
    private static final long serialVersionUID = 1L;
    private final LocalDate ngayCuoi;
    private final long[] doanhThu;
    private final Rectangle[] cacCot = new Rectangle[7];
    private final ActionListener khiChon;
    private int dangTro = -1;

    /** Nhận bảy số liệu đã tổng hợp từ DAO, không tự sinh giá trị biểu đồ. */
    public BieuDoDoanhThuPanel(LocalDate ngayCuoi, long[] doanhThu, ActionListener khiChon) {
        this.ngayCuoi = ngayCuoi;
        this.doanhThu = doanhThu.clone();
        this.khiChon = khiChon;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(380, 190));
        setFont(UiTheme.APP_FONT.deriveFont(11f));
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    /** Vẽ trục và các cột; ngày không có doanh thu vẫn bấm được tại đường đáy. */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int rong = Math.max(1, (getWidth() - 20) / 7);
        int day = getHeight() - 28;
        long lonNhat = 0;
        for (long tien : doanhThu) {
            if (tien > lonNhat) {
                lonNhat = tien;
            }
        }
        g.setColor(UiTheme.APP_BORDER);
        g.drawLine(8, day, getWidth() - 8, day);
        for (int i = 0; i < 7; i++) {
            int cao = lonNhat == 0 ? 0 : (int) (doanhThu[i] / (double) lonNhat * (day - 26));
            int x = 10 + i * rong + 5;
            int w = Math.max(4, rong - 14);
            cacCot[i] = new Rectangle(x, day - Math.max(8, cao), w, Math.max(8, cao));
            g.setColor(i == dangTro || i == 6 ? UiTheme.APP_PRIMARY : new Color(0x9DBBD9));
            g.fillRect(x, day - Math.max(2, cao), w, Math.max(2, cao));
            g.setColor(UiTheme.APP_MUTED);
            String ngay = ngayCuoi.minusDays(6 - i).format(DateTimeFormatter.ofPattern("dd/MM"));
            g.drawString(ngay, x + (w - g.getFontMetrics().stringWidth(ngay)) / 2, day + 20);
        }
        if (lonNhat == 0) {
            g.setColor(UiTheme.APP_MUTED);
            g.drawString("Chưa có doanh thu trong 7 ngày", 12, 22);
        }
    }

    /** Tìm cột biểu đồ đang được trỏ vào. */
    private int timCot(MouseEvent e) {
        for (int i = 0; i < 7; i++) {
            if (cacCot[i] != null && cacCot[i].contains(e.getPoint())) {
                return i;
            }
        }
        return -1;
    }

    /** Hiển thị tooltip và màu cột khi rê chuột. */
    @Override
    public void mouseMoved(MouseEvent e) {
        dangTro = timCot(e);
        setCursor(Cursor.getPredefinedCursor(dangTro < 0 ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR));
        setToolTipText(dangTro < 0 ? null
                : ngayCuoi.minusDays(6 - dangTro) + ": " + UiChung.so(doanhThu[dangTro]) + " đ · Bấm xem hóa đơn");
        repaint();
    }

    /** Gửi đúng ngày được chọn cho màn Trang chủ. */
    @Override
    public void mouseClicked(MouseEvent e) {
        int i = timCot(e);
        if (i >= 0) {
            khiChon.actionPerformed(
                    new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ngayCuoi.minusDays(6 - i).toString()));
        }
    }

    /** Bỏ màu nhấn khi chuột rời biểu đồ. */
    @Override
    public void mouseExited(MouseEvent e) {
        dangTro = -1;
        repaint();
    }

    /** Không thực hiện thao tác khi kéo chuột. */
    @Override
    public void mouseDragged(MouseEvent e) {
    }

    /** Không thực hiện thao tác khi chuột vào vùng vẽ. */
    @Override
    public void mouseEntered(MouseEvent e) {
    }

    /** Không thực hiện thao tác khi nhấn giữ chuột. */
    @Override
    public void mousePressed(MouseEvent e) {
    }

    /** Không thực hiện thao tác khi thả chuột. */
    @Override
    public void mouseReleased(MouseEvent e) {
    }
}
