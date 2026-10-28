package ui.component;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import entity.TongQuanQuanLy;

/** Biểu đồ bảy ngày bằng nút Swing: hover, tooltip, Tab/Enter/Space và chọn đúng ngày để xem chi tiết. */
public final class BieuDoDoanhThuPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    /** Nhãn ngày dưới cột; tooltip dùng đủ ngày/tháng/năm. */
    private static final DateTimeFormatter NGAY_NGAN = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter NGAY_DAY_DU = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Vẽ từ cùng snapshot với tổng tiền, không lưu số tiền riêng trong UI.
     * @param tongQuan dữ liệu tổng quan
     * @param khiChonNgay thao tác mở chi tiết của ngày được chọn
     */
    public BieuDoDoanhThuPanel(TongQuanQuanLy tongQuan, Consumer<LocalDate> khiChonNgay) {
        super(new GridLayout(1, 7, 8, 0));
        Objects.requireNonNull(tongQuan);
        Objects.requireNonNull(khiChonNgay);
        setOpaque(false);
        setPreferredSize(new Dimension(0, 160));
        BigDecimal lonNhat = BigDecimal.ZERO;
        for (int viTri = 0; viTri < 7; viTri++) {
            lonNhat = lonNhat.max(tongQuan.tinhDoanhThuNgay(tongQuan.getTuNgay().plusDays(viTri)));
        }
        for (int viTri = 0; viTri < 7; viTri++) {
            LocalDate ngay = tongQuan.getTuNgay().plusDays(viTri);
            BigDecimal soTien = tongQuan.tinhDoanhThuNgay(ngay);
            JButton btnCot = new CotDoanhThuButton(ngay, soTien, lonNhat, ngay.equals(tongQuan.getDenNgay()));
            String moTa = "Ngày " + ngay.format(NGAY_DAY_DU) + " · " + QuanLyUi.dinhDangTien(soTien)
                    + " · " + tongQuan.getHoaDonTheoNgay(ngay).size() + " hóa đơn";
            btnCot.setToolTipText("<html>" + moTa + "<br>Nhấn để xem chi tiết thanh toán.</html>");
            btnCot.getAccessibleContext().setAccessibleName(moTa);
            btnCot.addActionListener(event -> khiChonNgay.accept(ngay));
            add(btnCot);
        }
    }

    /** Một cột có vùng bấm và focus riêng; không gán sự kiện cho nền biểu đồ. */
    private static final class CotDoanhThuButton extends JButton {

        private static final long serialVersionUID = 1L;
        /** Dữ liệu bất biến để vẽ lại đúng tỷ lệ khi thay đổi kích thước. */
        private final LocalDate ngay;
        private final BigDecimal soTien;
        private final BigDecimal lonNhat;
        private final boolean ngayCuoi;

        /**
         * @param ngay ngày của cột
         * @param soTien doanh thu ngày
         * @param lonNhat doanh thu lớn nhất trong kỳ
         * @param ngayCuoi cột cuối được tô màu nhấn theo Figma
         */
        private CotDoanhThuButton(LocalDate ngay, BigDecimal soTien, BigDecimal lonNhat, boolean ngayCuoi) {
            this.ngay = ngay;
            this.soTien = soTien;
            this.lonNhat = lonNhat;
            this.ngayCuoi = ngayCuoi;
            setBorder(BorderFactory.createEmptyBorder());
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            getModel().addChangeListener(event -> repaint());
            getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("pressed ENTER"), "pressed");
            getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("released ENTER"), "released");
        }

        /** {@inheritDoc} */
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int rong = Math.max(1, Math.min(51, getWidth() - 8));
            int caoToiDa = Math.max(1, getHeight() - 36);
            int cao = lonNhat.signum() == 0 ? 0 : soTien.multiply(BigDecimal.valueOf(caoToiDa))
                    .divide(lonNhat, 0, RoundingMode.HALF_UP).intValue();
            int trai = (getWidth() - rong) / 2;
            int chan = getHeight() - 25;
            boolean nhanManh = getModel().isRollover() || hasFocus();
            // Ngày 0đ chỉ có vạch đáy, không tạo cột có chiều cao giả.
            g.setColor(nhanManh || ngayCuoi ? UiTheme.APP_PRIMARY : new Color(0x9CB9D6));
            if (cao == 0) {
                g.drawLine(trai, chan, trai + rong, chan);
            } else {
                g.fillRoundRect(trai, chan - cao, rong, cao, 5, 5);
            }
            g.setFont(UiTheme.APP_FONT.deriveFont(10.5f));
            g.setColor(nhanManh ? UiTheme.APP_PRIMARY : UiTheme.APP_MUTED);
            FontMetrics font = g.getFontMetrics();
            String nhan = ngay.format(NGAY_NGAN);
            g.drawString(nhan, (getWidth() - font.stringWidth(nhan)) / 2, getHeight() - 5);
            if (hasFocus()) {
                g.drawRoundRect(1, 1, Math.max(0, getWidth() - 3), Math.max(0, getHeight() - 3), 6, 6);
            }
            g.dispose();
        }
    }
}
