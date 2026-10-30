package ui.screen;

import java.awt.*;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.*;
import ui.component.UiTheme;

/** Tổng quan tại quầy theo Page 3; dữ liệu minh họa chưa nối DAO nghiệp vụ. */
public final class TrangChuTiepNhanPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final Consumer<String> dieuHuong;

    /** @param dieuHuong callback dùng chung với sidebar, gọi trên EDT */
    public TrangChuTiepNhanPanel(Consumer<String> dieuHuong) {
        super(new BorderLayout());
        this.dieuHuong = Objects.requireNonNull(dieuHuong, "Cần thao tác điều hướng.");
        setOpaque(false);
        JPanel trang = new TrangCuon();
        trang.setOpaque(false);
        trang.setLayout(new BoxLayout(trang, BoxLayout.Y_AXIS));
        trang.add(nhan("Tiếp nhận / Hệ thống / Tổng quan tiếp nhận", 12, false));
        trang.add(Box.createVerticalStrut(16));
        trang.add(nhan("Tổng quan tiếp nhận", 24, true));
        trang.add(nhan("Xử lý xe đến garage, báo giá và bàn giao đúng quy trình.", 13, false));
        trang.add(Box.createVerticalStrut(16));
        JPanel thongKe = new JPanel(new GridLayout(1, 4, 12, 0));
        thongKe.setOpaque(false);
        thongKe.add(the("Tiếp nhận hôm nay", "12", "Dữ liệu minh họa"));
        thongKe.add(the("Chờ kỹ thuật kiểm tra", "05", "02 xe vừa đến"));
        thongKe.add(the("Báo giá chờ khách", "04", "01 báo giá cần gọi lại"));
        thongKe.add(the("Chờ bàn giao", "02", "Thu ngân đã thu đủ"));
        thongKe.setPreferredSize(new Dimension(0, 116));
        thongKe.setMaximumSize(new Dimension(Integer.MAX_VALUE, 116));
        thongKe.setAlignmentX(LEFT_ALIGNMENT);
        trang.add(thongKe);
        trang.add(Box.createVerticalStrut(16));
        JPanel cotTrai = cot();
        cotTrai.add(danhSach("Phiếu tiếp nhận gần đây", new String[][] {
            {"TN-2026-0112 · 59A-456.88", "Phạm Quốc Huy · Kiểm tra tiếng kêu phanh", "Chờ kiểm tra"},
            {"TN-2026-0109 · 51G-678.90", "Trần Thị Bình · Bảo dưỡng định kỳ", "Đang kiểm tra"},
            {"TN-2026-0108 · 51H-246.80", "Nguyễn Văn An · Dựa trên kết quả chẩn đoán", "Đã lập báo giá"}
        }));
        cotTrai.add(Box.createVerticalStrut(16));
        cotTrai.add(danhSach("Bàn giao cần thực hiện", new String[][] {
            {"GX-2026-0104 · 51F-234.56", "Thanh toán xong · hẹn khách 16:30", "Chờ bàn giao"},
            {"GX-2026-0108 · 51H-246.80", "Thanh toán xong · hẹn khách 17:00", "Chờ bàn giao"}
        }));
        JPanel cotPhai = cot();
        cotPhai.add(tacVu("Làm việc nhanh tại quầy", new String[] {"Tiếp nhận xe mới", "Tìm khách hàng",
                "Báo giá chờ duyệt", "Xe chờ bàn giao"}, new String[] {"Phiếu tiếp nhận", "Khách hàng", "Báo giá", "Bàn giao xe"}));
        cotPhai.add(Box.createVerticalStrut(16));
        cotPhai.add(tacVu("Đường tắt", new String[] {"Phiếu tiếp nhận", "Khách hàng", "Phương tiện", "Báo giá"},
                new String[] {"Phiếu tiếp nhận", "Khách hàng", "Phương tiện", "Báo giá"}));
        cotPhai.add(Box.createVerticalGlue());
        JPanel noiDung = new JPanel(new GridLayout(1, 2, 16, 0));
        noiDung.setOpaque(false);
        noiDung.add(cotTrai);
        noiDung.add(cotPhai);
        noiDung.setPreferredSize(new Dimension(0, 450));
        noiDung.setMaximumSize(new Dimension(Integer.MAX_VALUE, 450));
        noiDung.setAlignmentX(LEFT_ALIGNMENT);
        trang.add(noiDung);
        trang.add(Box.createVerticalStrut(16));
        trang.add(nhan("Dữ liệu minh họa · chưa kết nối nghiệp vụ / cơ sở dữ liệu", 12, false));
        trang.add(Box.createVerticalGlue());
        // Cuộn dọc giữ toàn bộ tác vụ truy cập được ở màn hình 960×600.
        JScrollPane cuon = new JScrollPane(trang, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        cuon.setBorder(null);
        cuon.setOpaque(false);
        cuon.getViewport().setOpaque(false);
        cuon.getVerticalScrollBar().setUnitIncrement(20);
        cuon.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                boolean hep = cuon.getViewport().getWidth() < 850;
                ((GridLayout) thongKe.getLayout()).setRows(hep ? 2 : 1);
                ((GridLayout) thongKe.getLayout()).setColumns(hep ? 2 : 4);
                ((GridLayout) thongKe.getLayout()).setVgap(12);
                thongKe.setPreferredSize(new Dimension(0, hep ? 244 : 116));
                thongKe.setMaximumSize(new Dimension(Integer.MAX_VALUE, hep ? 244 : 116));
                ((GridLayout) noiDung.getLayout()).setRows(hep ? 2 : 1);
                ((GridLayout) noiDung.getLayout()).setColumns(hep ? 1 : 2);
                ((GridLayout) noiDung.getLayout()).setVgap(16);
                noiDung.setPreferredSize(new Dimension(0, hep ? 800 : 450));
                noiDung.setMaximumSize(new Dimension(Integer.MAX_VALUE, hep ? 800 : 450));
                trang.revalidate();
            }
        });
        add(cuon);
    }

    /** Bám chiều rộng vùng xem để không cắt cột khi cửa sổ hẹp. */
    private static final class TrangCuon extends JPanel implements Scrollable {
        private static final long serialVersionUID = 1L;
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) { return 20; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) { return Math.max(20, r.height - 20); }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    private JPanel cot() {
        JPanel pnl = new JPanel();
        pnl.setOpaque(false);
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        return pnl;
    }

    private JLabel nhan(String chu, int co, boolean dam) {
        JLabel lbl = new JLabel(chu);
        lbl.setFont((dam ? UiTheme.APP_BOLD_FONT : UiTheme.APP_FONT).deriveFont((float) co));
        lbl.setForeground(dam ? UiTheme.APP_TEXT : UiTheme.APP_MUTED);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private JPanel khung(String tieuDe) {
        JPanel pnl = UiTheme.roundedPanel(new BorderLayout(0, 14), Color.WHITE, 8);
        pnl.setBorder(UiTheme.roundedBorder(UiTheme.APP_BORDER, 8, 16, 16));
        pnl.setAlignmentX(LEFT_ALIGNMENT);
        pnl.add(nhan(tieuDe, 14, true), BorderLayout.NORTH);
        return pnl;
    }

    private JPanel the(String ten, String so, String ghiChu) {
        JPanel pnl = khung(ten);
        pnl.add(nhan(so, 28, true), BorderLayout.CENTER);
        pnl.add(nhan(ghiChu, 11, false), BorderLayout.SOUTH);
        return pnl;
    }

    private JPanel danhSach(String ten, String[][] dong) {
        JPanel pnl = khung(ten);
        JPanel ds = new JPanel(new GridLayout(0, 1, 0, 8));
        ds.setOpaque(false);
        for (String[] duLieu : dong) {
            JPanel hang = new JPanel(new BorderLayout(8, 4));
            hang.setOpaque(false);
            hang.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.APP_BORDER));
            JPanel thongTin = new JPanel(new BorderLayout(0, 6));
            thongTin.setOpaque(false);
            thongTin.add(nhan(duLieu[0], 12, true), BorderLayout.NORTH);
            thongTin.add(nhan(duLieu[1], 11, false), BorderLayout.CENTER);
            hang.add(thongTin, BorderLayout.CENTER);
            JLabel trangThai = nhan(duLieu[2], 11, true);
            trangThai.setForeground(UiTheme.APP_PRIMARY);
            hang.add(trangThai, BorderLayout.EAST);
            ds.add(hang);
        }
        pnl.add(ds);
        return pnl;
    }

    private JPanel tacVu(String ten, String[] nhanNut, String[] dich) {
        JPanel pnl = khung(ten);
        JPanel nut = new JPanel(new GridLayout(2, 2, 8, 8));
        nut.setOpaque(false);
        for (int i = 0; i < nhanNut.length; i++) {
            String muc = dich[i];
            JButton btn = UiTheme.createAppButton(nhanNut[i], i == 0 && ten.equals("Làm việc nhanh tại quầy"));
            btn.setPreferredSize(new Dimension(160, 36));
            btn.setToolTipText(muc + ": chưa triển khai trong nhánh dashboard.");
            btn.addActionListener(e -> dieuHuong.accept(muc));
            nut.add(btn);
        }
        pnl.add(nut);
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 158));
        return pnl;
    }
}
