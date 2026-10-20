package ui.component;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/** Giữ khả năng kéo và đổi kích thước khi dùng thanh tiêu đề Swing thay cho viền Windows. */
public final class WindowMouseHandler extends MouseAdapter {

    /** Khoảng bắt chuột sát mép cửa sổ, tính bằng pixel logic. */
    private static final int RESIZE_MARGIN = 5;
    /** Cửa sổ được quản lý. */
    private final JFrame winChu;
    /** Vị trí chuột trên màn hình tại lúc bắt đầu kéo. */
    private Point diemBatDau;
    /** Khung cửa sổ trước khi kéo để tránh cộng dồn sai số. */
    private Rectangle khungBanDau;
    /** Hướng ngang: -1 kéo trái, 0 không đổi chiều rộng, 1 kéo phải. */
    private int canhNgang;
    /** Hướng dọc: -1 kéo trên, 0 không đổi chiều cao, 1 kéo dưới. */
    private int canhDoc;

    /**
     * Tạo bộ xử lý; cửa sổ chủ đăng ký MouseListener và MouseMotionListener.
     * @param winChu cửa sổ không dùng viền hệ điều hành
     */
    public WindowMouseHandler(JFrame winChu) {
        this.winChu = winChu;
    }

    /** {@inheritDoc} */
    @Override
    public void mousePressed(MouseEvent event) {
        if (!SwingUtilities.isLeftMouseButton(event) || winChu.getExtendedState() != Frame.NORMAL) {
            return;
        }
        canhNgang = findEdge(event.getX(), winChu.getWidth());
        canhDoc = findEdge(event.getY(), winChu.getHeight());
        if (canhNgang != 0 || canhDoc != 0 || event.getY() <= UiTheme.TITLE_HEIGHT) {
            diemBatDau = event.getLocationOnScreen();
            khungBanDau = winChu.getBounds();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void mouseDragged(MouseEvent event) {
        if (diemBatDau == null) {
            return;
        }
        // Tính độ dời từ lần nhấn đầu để vị trí không bị nhảy theo từng sự kiện.
        int doDoiX = event.getXOnScreen() - diemBatDau.x;
        int doDoiY = event.getYOnScreen() - diemBatDau.y;
        if (canhNgang == 0 && canhDoc == 0) {
            winChu.setLocation(khungBanDau.x + doDoiX, khungBanDau.y + doDoiY);
        } else {
            resizeWindow(doDoiX, doDoiY);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void mouseReleased(MouseEvent event) {
        diemBatDau = null;
        khungBanDau = null;
    }

    /** {@inheritDoc} */
    @Override
    public void mouseClicked(MouseEvent event) {
        if (winChu.isResizable() && SwingUtilities.isLeftMouseButton(event) && event.getClickCount() == 2
                && event.getY() <= UiTheme.TITLE_HEIGHT
                && findEdge(event.getX(), winChu.getWidth()) == 0
                && findEdge(event.getY(), winChu.getHeight()) == 0) {
            WindowTitlePanel.toggleMaximized(winChu);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void mouseMoved(MouseEvent event) {
        winChu.setCursor(Cursor.getPredefinedCursor(getResizeCursor(event)));
    }

    /** {@inheritDoc} */
    @Override
    public void mouseExited(MouseEvent event) {
        if (diemBatDau == null) {
            winChu.setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * Xác định cạnh trên một trục; khóa đổi kích thước khi đang phóng to.
     * @param toaDo vị trí chuột trên trục
     * @param chieuDai chiều rộng hoặc chiều cao cửa sổ
     * @return -1 ở đầu trục, 1 ở cuối trục, 0 ở giữa hoặc khi không thể đổi kích thước
     */
    private int findEdge(int toaDo, int chieuDai) {
        if (winChu.getExtendedState() != Frame.NORMAL || !winChu.isResizable()) {
            return 0;
        }
        if (toaDo < RESIZE_MARGIN) {
            return -1;
        }
        return toaDo >= chieuDai - RESIZE_MARGIN ? 1 : 0;
    }

    /**
     * Chọn con trỏ tương ứng với cạnh hoặc góc nằm dưới chuột.
     * @param event sự kiện có tọa độ tương đối với JFrame
     * @return mã con trỏ chuẩn của AWT
     */
    private int getResizeCursor(MouseEvent event) {
        // Kết hợp hai hướng để nhận bốn cạnh và bốn góc.
        int ngang = findEdge(event.getX(), winChu.getWidth());
        int doc = findEdge(event.getY(), winChu.getHeight());
        if (ngang == 0 && doc == 0) {
            return Cursor.DEFAULT_CURSOR;
        }
        if (doc == 0) {
            return ngang < 0 ? Cursor.W_RESIZE_CURSOR : Cursor.E_RESIZE_CURSOR;
        }
        if (ngang == 0) {
            return doc < 0 ? Cursor.N_RESIZE_CURSOR : Cursor.S_RESIZE_CURSOR;
        }
        if (doc < 0) {
            return ngang < 0 ? Cursor.NW_RESIZE_CURSOR : Cursor.NE_RESIZE_CURSOR;
        }
        return ngang < 0 ? Cursor.SW_RESIZE_CURSOR : Cursor.SE_RESIZE_CURSOR;
    }

    /**
     * Đổi kích thước theo cạnh đang kéo và giữ giới hạn tối thiểu của MainFrame.
     * @param doDoiX độ dời chuột theo chiều ngang
     * @param doDoiY độ dời chuột theo chiều dọc
     */
    private void resizeWindow(int doDoiX, int doDoiY) {
        // Tọa độ cạnh đối diện được giữ cố định khi kéo cạnh trên hoặc cạnh trái.
        Rectangle khungMoi = new Rectangle(khungBanDau);
        Dimension toiThieu = winChu.getMinimumSize();
        if (canhNgang < 0) {
            khungMoi.width = Math.max(toiThieu.width, khungBanDau.width - doDoiX);
            khungMoi.x = khungBanDau.x + khungBanDau.width - khungMoi.width;
        } else if (canhNgang > 0) {
            khungMoi.width = Math.max(toiThieu.width, khungBanDau.width + doDoiX);
        }
        if (canhDoc < 0) {
            khungMoi.height = Math.max(toiThieu.height, khungBanDau.height - doDoiY);
            khungMoi.y = khungBanDau.y + khungBanDau.height - khungMoi.height;
        } else if (canhDoc > 0) {
            khungMoi.height = Math.max(toiThieu.height, khungBanDau.height + doDoiY);
        }
        winChu.setBounds(khungMoi);
    }
}
