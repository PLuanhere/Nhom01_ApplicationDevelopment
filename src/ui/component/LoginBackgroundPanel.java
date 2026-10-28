package ui.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.LinearGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.Scrollable;

/** Nền ảnh garage của đăng nhập; form bên trong dùng GridBagLayout, không dùng tọa độ cố định. */
public final class LoginBackgroundPanel extends JPanel implements Scrollable {

    private static final long serialVersionUID = 1L;
    /** Ảnh gốc từ Figma đóng gói cùng ứng dụng. */
    private final BufferedImage anhGarage = UiTheme.readImage("login-background.png");

    /** Tạo nền có thể cuộn khi cửa sổ thấp hơn thẻ đăng nhập. */
    public LoginBackgroundPanel() {
        super(new GridBagLayout());
        setPreferredSize(new Dimension(960, 621));
        setBackground(UiTheme.SIDEBAR_BACKGROUND);
    }

    /** {@inheritDoc} */
    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        // Cover giữ đúng tỷ lệ ảnh, cắt đều hai cạnh giống ảnh nền Page 3.
        double tyLe = Math.max((double) getWidth() / anhGarage.getWidth(),
                (double) getHeight() / anhGarage.getHeight());
        int rong = (int) Math.ceil(anhGarage.getWidth() * tyLe);
        int cao = (int) Math.ceil(anhGarage.getHeight() * tyLe);
        g.drawImage(anhGarage, (getWidth() - rong) / 2, (getHeight() - cao) / 2, rong, cao, null);
        g.setPaint(new LinearGradientPaint(0, 0, Math.max(1, getWidth()), 0,
                new float[] {0, 0.38f, 1}, new Color[] {
                    new Color(7, 20, 34, 163), new Color(7, 20, 34, 92), new Color(7, 20, 34, 128)}));
        g.fillRect(0, 0, getWidth(), getHeight());
        veChuTrangTri(g);
        g.dispose();
    }

    /**
     * Vẽ chữ trang trí trên ảnh, tách khỏi các điều khiển nhập liệu.
     * @param g ngữ cảnh vẽ đã bật làm mịn
     */
    private void veChuTrangTri(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(UiTheme.APP_BOLD_FONT.deriveFont(16.0f));
        g.drawString("GARAGE ĐỨC DŨNG", 42, 50);
        // Ở cửa sổ hẹp, ẩn chữ quảng bá để không đè lên thẻ đăng nhập.
        if (getWidth() >= 1200) {
            g.setFont(UiTheme.APP_BOLD_FONT.deriveFont(25.0f));
            g.drawString("Vận hành garage chính", 42, getHeight() - 212);
            g.drawString("xác, từ tiếp nhận đến bàn", 42, getHeight() - 179);
            g.drawString("giao.", 42, getHeight() - 146);
            g.setFont(UiTheme.APP_FONT.deriveFont(11.5f));
            g.setColor(new Color(0xE2E9F2));
            g.drawString("Hồ sơ xe, báo giá, tiến độ, phụ tùng và hóa đơn trên", 42, getHeight() - 115);
            g.drawString("một hệ thống dành riêng cho nhân viên.", 42, getHeight() - 96);
        }
        g.setFont(UiTheme.APP_FONT.deriveFont(10.5f));
        g.setColor(new Color(0xC5D0DF));
        g.drawString("Hệ thống nội bộ dành riêng cho nhân viên", 42, getHeight() - 28);
    }

    /** {@inheritDoc} */
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    /** {@inheritDoc} */
    @Override
    public int getScrollableUnitIncrement(Rectangle visible, int orientation, int direction) {
        return 24;
    }

    /** {@inheritDoc} */
    @Override
    public int getScrollableBlockIncrement(Rectangle visible, int orientation, int direction) {
        return Math.max(24, visible.height - 24);
    }

    /** {@inheritDoc} */
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean getScrollableTracksViewportHeight() {
        return getParent() != null && getParent().getHeight() >= getPreferredSize().height;
    }
}
