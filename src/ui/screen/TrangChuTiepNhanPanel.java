package ui.screen;

import java.awt.*;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Supplier;
import java.time.LocalDate;
import dao.TrangChuTiepNhanDAO;
import entity.*;
import java.util.function.Consumer;
import javax.swing.*;
import ui.component.UiTheme;

/** Tổng quan tại quầy đọc chứng từ hiện tại, không tự suy ra đã thanh toán hay được bàn giao. */
public final class TrangChuTiepNhanPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final Consumer<String> dieuHuong;
    private final TrangChuTiepNhanDAO dao;
    private final Supplier<TaiKhoan> phien;
    private final Consumer<String> moBanGiao;

    /** @param dieuHuong callback dùng chung với sidebar, gọi trên EDT */
    public TrangChuTiepNhanPanel(Consumer<String> dieuHuong) { this(dieuHuong, null, null); }

    public TrangChuTiepNhanPanel(Consumer<String> dieuHuong, TrangChuTiepNhanDAO dao, Supplier<TaiKhoan> phien) {
        this(dieuHuong, dao, phien, ma -> dieuHuong.accept("Bàn giao xe"));
    }

    public TrangChuTiepNhanPanel(Consumer<String> dieuHuong, TrangChuTiepNhanDAO dao,
            Supplier<TaiKhoan> phien, Consumer<String> moBanGiao) {
        super(new BorderLayout());
        this.dieuHuong = Objects.requireNonNull(dieuHuong, "Cần thao tác điều hướng.");
        this.dao = dao; this.phien = phien;
        this.moBanGiao = Objects.requireNonNull(moBanGiao);
        setOpaque(false); capNhatDuLieu();
    }

    public void capNhatDuLieu() {
        removeAll();
        JPanel trang = new TrangCuon();
        trang.setOpaque(false);
        trang.setLayout(new BoxLayout(trang, BoxLayout.Y_AXIS));
        trang.add(nhan("Tiếp nhận / Hệ thống / Tổng quan tiếp nhận", 12, false));
        trang.add(Box.createVerticalStrut(16));
        JPanel tieuDe = new JPanel(new BorderLayout(12, 0)); tieuDe.setOpaque(false); tieuDe.setAlignmentX(LEFT_ALIGNMENT);
        tieuDe.add(nhan("Tổng quan tiếp nhận", 24, true));
        JButton lamMoi = UiTheme.createAppButton("Làm mới tổng quan", false);
        lamMoi.addActionListener(e -> capNhatDuLieu()); tieuDe.add(lamMoi, BorderLayout.EAST);
        tieuDe.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36)); trang.add(tieuDe);
        trang.add(nhan("Xử lý xe đến garage, báo giá và bàn giao đúng quy trình.", 13, false));
        trang.add(Box.createVerticalStrut(16));
        JPanel thongKe = new JPanel(new GridLayout(1, 4, 12, 0));
        thongKe.setOpaque(false);
        TaiKhoan tk = dao == null ? null : phien.get();
        List<PhieuSuaChua> choGiao = List.of(); String loiBanGiao = null;
        try { if (dao != null) choGiao = dao.layChoBanGiao(tk); }
        catch (IllegalArgumentException | IllegalStateException | SecurityException e) { loiBanGiao = e.getMessage(); }
        thongKe.add(the("Tiếp nhận hôm nay", dao == null ? "0" : Integer.toString(dao.demTiepNhan(tk, LocalDate.now())), "Theo ngày tiếp nhận"));
        thongKe.add(the("Chưa hoàn tất kiểm tra", dao == null ? "0" : Integer.toString(dao.demChuaKiemTraXong(tk)), "Phiếu đã xác nhận"));
        thongKe.add(the("Báo giá chờ khách", dao == null ? "0" : Integer.toString(dao.demChoKhach(tk)), "Phiên bản hiện hành"));
        thongKe.add(the("Chờ bàn giao", loiBanGiao == null ? Integer.toString(choGiao.size()) : "—", "Đủ tiền hoặc có phê duyệt"));
        thongKe.setPreferredSize(new Dimension(0, 116));
        thongKe.setMaximumSize(new Dimension(Integer.MAX_VALUE, 116));
        thongKe.setAlignmentX(LEFT_ALIGNMENT);
        trang.add(thongKe);
        if (loiBanGiao != null) {
            JLabel loi = nhan("Chưa đọc được dữ liệu bàn giao. Hãy thử làm mới tổng quan.", 12, false);
            loi.setForeground(UiTheme.ERROR); loi.setToolTipText(loiBanGiao); trang.add(loi);
        }
        trang.add(Box.createVerticalStrut(16));
        JPanel cotTrai = cot();
        List<String[]> ganDay = new ArrayList<>(), hoanThanh = new ArrayList<>();
        if (dao != null) {
            for (PhieuTiepNhan p : dao.layGanDay(tk)) ganDay.add(new String[] {
                    p.getMaPhieu() + " · " + p.getBienSo(), p.getTenKhach() + " · " + p.getYeuCau(), docTrangThai(tk, p.getMaPhieu())});
            for (PhieuSuaChua lenh : choGiao.subList(0, Math.min(2, choGiao.size()))) hoanThanh.add(new String[] {
                    lenh.getMaPhieu() + " · " + lenh.getBienSo(), "Đã có căn cứ · mở để đối chiếu trước khi giao", "Chờ bàn giao", lenh.getMaPhieu()});
        }
        cotTrai.add(danhSach("Phiếu tiếp nhận gần đây", ganDay.toArray(String[][]::new), "Chưa có dữ liệu."));
        cotTrai.add(Box.createVerticalStrut(16));
        cotTrai.add(danhSach("Bàn giao cần thực hiện", hoanThanh.toArray(String[][]::new),
                loiBanGiao == null ? "Chưa có xe đủ điều kiện chờ bàn giao." : "Chưa đọc được danh sách bàn giao."));
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
        trang.add(nhan("Dữ liệu trong bộ nhớ của lần chạy ứng dụng · chưa lưu SQL Server", 12, false));
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
        add(cuon); revalidate(); repaint();
    }

    private String docTrangThai(TaiKhoan tk, String ma) {
        try { return dao.trangThaiPhieu(tk, ma); }
        catch (IllegalArgumentException | IllegalStateException | SecurityException e) { return "Chưa đọc được trạng thái"; }
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
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, lbl.getPreferredSize().height));
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

    private JPanel danhSach(String ten, String[][] dong, String khiTrong) {
        JPanel pnl = khung(ten);
        JPanel ds = new JPanel(new GridLayout(0, 1, 0, 8));
        ds.setOpaque(false);
        if (dong.length == 0) ds.add(nhan(khiTrong, 12, false));
        for (String[] duLieu : dong) {
            JPanel hang = new JPanel(new BorderLayout(8, 4));
            hang.setOpaque(false);
            hang.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.APP_BORDER));
            JPanel thongTin = new JPanel(new BorderLayout(0, 6));
            thongTin.setOpaque(false);
            if (duLieu.length > 3) {
                JButton mo = new JButton(duLieu[0]); mo.setFont(UiTheme.APP_BOLD_FONT.deriveFont(12f));
                mo.setForeground(UiTheme.APP_PRIMARY); mo.setHorizontalAlignment(SwingConstants.LEFT);
                mo.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0)); mo.setContentAreaFilled(false);
                mo.setToolTipText("Mở bàn giao cho lệnh " + duLieu[3]); mo.addActionListener(e -> moBanGiao.accept(duLieu[3]));
                thongTin.add(mo, BorderLayout.NORTH);
            } else thongTin.add(nhan(duLieu[0], 12, true), BorderLayout.NORTH);
            thongTin.add(nhan(duLieu[1], 11, false), BorderLayout.CENTER);
            hang.add(thongTin, BorderLayout.CENTER);
            JLabel trangThai = nhan(duLieu[2], 11, true);
            trangThai.setForeground(UiTheme.APP_PRIMARY);
            hang.add(trangThai, BorderLayout.EAST);
            ds.add(hang);
        }
        JPanel neo = new JPanel(new BorderLayout()); neo.setOpaque(false); neo.add(ds, BorderLayout.NORTH);
        pnl.add(neo);
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
            btn.setToolTipText("Mở " + muc);
            if ("Xe chờ bàn giao".equals(nhanNut[i])) btn.addActionListener(e -> moBanGiao.accept(null));
            else btn.addActionListener(e -> dieuHuong.accept(muc));
            nut.add(btn);
        }
        pnl.add(nut);
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 158));
        return pnl;
    }
}
