package ui.component;

import dao.baocao.BaoCaoDAO;
import entity.baocao.BaoCao;
import entity.hoadon.HoaDon;
import entity.nhansu.TaiKhoan;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/** Bố cục thẻ, chỉ số và bảng dùng lại giữa Trang chủ và các báo cáo. */
public final class BaoCaoUi {
    /** Lớp chỉ cung cấp các phương thức tạo giao diện. */
    private BaoCaoUi() {
    }

    /** Tạo vùng xếp nội dung dọc, có thể đặt trong vùng cuộn chung. */
    public static JPanel taoCot() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setOpaque(false);
        return pnl;
    }

    /** Thêm một khối cùng khoảng cách, không kéo giãn chiều cao khối. */
    public static void them(JPanel pnl, JPanel khoi) {
        JPanel boc = new JPanel(new BorderLayout()) {
            private static final long serialVersionUID = 1L;

            /** Giữ chiều cao vừa nội dung sau mỗi lần đổi kích thước. */
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        boc.setOpaque(false);
        boc.setAlignmentX(Component.LEFT_ALIGNMENT);
        boc.add(khoi, BorderLayout.NORTH);
        pnl.add(boc);
        pnl.add(Box.createVerticalStrut(14));
    }

    /** Tạo thẻ trắng có tiêu đề và nội dung. */
    public static JPanel taoThe(String ten, Component noiDung) {
        JPanel pnl = UiChung.taoThe(new BorderLayout(0, 12));
        pnl.setBorder(
                BorderFactory.createCompoundBorder(pnl.getBorder(), BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        pnl.add(UiChung.taoNhan(ten, 15, true, UiTheme.APP_TEXT), BorderLayout.NORTH);
        pnl.add(noiDung);
        return pnl;
    }

    /** Tạo đoạn mô tả tự xuống dòng khi cửa sổ hẹp. */
    public static JTextArea moTa(String chu) {
        JTextArea txt = new JTextArea(chu);
        txt.setEditable(false);
        txt.setOpaque(false);
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        txt.setFont(UiTheme.APP_FONT.deriveFont(12f));
        txt.setForeground(UiTheme.APP_MUTED);
        txt.setRows(2);
        return txt;
    }

    /** Tạo lưới tự chuyển giữa hai và bốn cột theo cửa sổ. */
    public static JPanel taoLuoiChiSo() {
        return new LuoiChiSo();
    }

    /** Tạo các ô tổng hợp, tiền được định dạng nguyên đồng. */
    public static JPanel taoChiSo(BaoCao bc) {
        JPanel pnl = new LuoiChiSo();
        String[] ten = bc.getTenChiSo();
        long[] giaTri = bc.getGiaTri();
        for (int i = 0; i < ten.length; i++) {
            JPanel o = new JPanel(new BorderLayout(0, 8));
            o.setOpaque(false);
            o.add(UiChung.taoNhan(ten[i], 12, false, UiTheme.APP_MUTED), BorderLayout.NORTH);
            boolean laTien = BaoCao.DOANH_THU.equals(bc.getLoai()) && i != 1;
            o.add(UiChung.taoNhan(UiChung.so(giaTri[i]) + (laTien ? " đ" : ""), 23, true, UiTheme.APP_TEXT));
            pnl.add(o);
        }
        return pnl;
    }

    /** Chuyển các dòng báo cáo sang bảng chỉ đọc, khóa kéo thay đổi cột. */
    public static JTable taoBang(String[] tenCot, List<String[]> dong) {
        DefaultTableModel model = new DefaultTableModel(tenCot, 0);
        for (String[] hang : dong) {
            String[] hienThi = hang.clone();
            for (int i = 0; i < tenCot.length; i++) {
                if (tenCot[i].equals("Số tiền (đ)")) {
                    hienThi[i] = UiChung.so(Long.parseLong(hang[i])) + " đ";
                }
                if (tenCot[i].equals("Tồn hiện tại") || tenCot[i].equals("Tồn tối thiểu")) {
                    hienThi[i] = UiChung.so(Double.parseDouble(hang[i]));
                }
            }
            model.addRow(hienThi);
        }
        JTable tbl = UiChung.taoBang(model);
        tbl.setRowHeight(44);
        for (int i = 0; i < tenCot.length; i++) {
            String ten = tenCot[i];
            if (ten.equals("Số tiền (đ)") || ten.equals("Tồn hiện tại") || ten.equals("Tồn tối thiểu")
                    || ten.equals("Số hồ sơ") || ten.equals("Lệnh được giao") || ten.equals("Đã hoàn thành")
                    || ten.equals("Chưa hoàn thành") || ten.equals("Tỷ lệ")) {
                tbl.getColumnModel().getColumn(i).setCellRenderer(new TableCellRenderer() {
                    /** Dùng kiểu bảng chung và căn phải các cột số. */
                    @Override
                    public Component getTableCellRendererComponent(JTable bang, Object value, boolean chon,
                            boolean focus, int dong, int cot) {
                        JLabel nhan = (JLabel) bang.getDefaultRenderer(Object.class).getTableCellRendererComponent(bang,
                                value, chon, focus, dong, cot);
                        nhan.setHorizontalAlignment(SwingConstants.RIGHT);
                        return nhan;
                    }
                });
            }
            if (tenCot[i].contains("Tên") || tenCot[i].equals("Ý nghĩa")) {
                tbl.getColumnModel().getColumn(i).setPreferredWidth(260);
            }
        }
        return tbl;
    }

    /** Bọc bảng trong vùng cuộn có chiều cao vừa danh sách. */
    public static JScrollPane cuonBang(JTable tbl) {
        JScrollPane scr = new JScrollPane(tbl);
        scr.setBorder(BorderFactory.createLineBorder(UiTheme.APP_BORDER));
        scr.setPreferredSize(new Dimension(600, Math.min(330, Math.max(150, 42 + tbl.getRowCount() * 44))));
        return scr;
    }

    /** Mở bản chụp báo cáo đã lưu hoặc chi tiết thống kê trong cửa sổ riêng. */
    public static void hienBaoCao(Component chu, BaoCao bc, TaiKhoan tk, BaoCaoDAO dao) {
        dao.kiemTraQuyen(tk);
        JPanel pnl = taoCot();
        them(pnl, taoThe(bc.getLoai() + " · " + BaoCaoDAO.khoangNgay(bc), taoChiSo(bc)));
        them(pnl, taoThe("Chi tiết · " + bc.getCacDong().size() + " dòng",
                cuonBang(taoBang(bc.getTenCot(), bc.getCacDong()))));
        JButton btnXuat = UiChung.taoNut("Xuất CSV", true);
        btnXuat.addActionListener(new ActionListener() {
            /** Xuất đúng bản chụp đang xem và kiểm tra lại phiên. */
            @Override
            public void actionPerformed(ActionEvent e) {
                xuatCsv(pnl, tk, dao, bc);
            }
        });
        JPanel thongTin = new JPanel(new BorderLayout());
        thongTin.setOpaque(false);
        thongTin.add(moTa("Người lập: " + bc.getNguoiLap() + " · Chụp số liệu: "
                + bc.getNgayLap().format(DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm:ss"))));
        them(pnl, thongTin);
        hienChiTiet(chu, bc.getMaBaoCao().isEmpty() ? bc.getLoai() : bc.getMaBaoCao(), pnl, btnXuat);
    }

    /** Hiển thị dialog cuộn được, có nút đóng và không thay đổi chứng từ. */
    public static void hienChiTiet(Component chu, String ten, JPanel noiDung, JButton... nutThem) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(chu), ten, JDialog.ModalityType.APPLICATION_MODAL);
        JPanel pnl = new JPanel(new BorderLayout(0, 14));
        pnl.setBackground(UiTheme.APP_BACKGROUND);
        pnl.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        pnl.add(UiChung.taoVungCuon(noiDung));
        JButton btnDong = UiChung.taoNut("Đóng", false);
        btnDong.addActionListener(new ActionListener() {
            /** Đóng phần xem chi tiết. */
            @Override
            public void actionPerformed(ActionEvent event) {
                dlg.dispose();
            }
        });
        JPanel cacNut = UiChung.taoHangNut(nutThem);
        cacNut.add(btnDong);
        pnl.add(cacNut, BorderLayout.SOUTH);
        dlg.add(pnl);
        dlg.setSize(Math.min(1000, Math.max(640, chu.getWidth())), 540);
        dlg.setLocationRelativeTo(chu);
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dlg.setVisible(true);
    }

    /** Xem các thông tin hóa đơn đã thu mà không cho thay đổi chứng từ. */
    public static void hienHoaDon(Component chu, HoaDon hd) {
        JPanel pnl = taoCot();
        List<String[]> dong = new ArrayList<>();
        dong.add(new String[] { "Mã hóa đơn", hd.getMaHoaDon() });
        dong.add(new String[] { "Lệnh sửa chữa", hd.getMaPhieuSuaChua() });
        dong.add(new String[] { "Khách hàng", hd.getTenKhachHang() });
        dong.add(new String[] { "Đã thanh toán", UiChung.so(hd.getTongTien()) + " đ" });
        dong.add(new String[] { "Hình thức", hd.getHinhThucThanhToan().getTenHienThi() });
        dong.add(new String[] { "Thời điểm",
                hd.getThoiDiemThanhToan().format(DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm")) });
        dong.add(new String[] { "Người thu", hd.getNguoiThucHien() });
        them(pnl, taoThe("Hóa đơn đã thanh toán", cuonBang(taoBang(new String[] { "Thông tin", "Nội dung" }, dong))));
        hienChiTiet(chu, hd.getMaHoaDon(), pnl);
    }

    /** Chọn nơi xuất file CSV; hủy chọn hoặc từ chối ghi đè thì giữ nguyên dữ liệu. */
    public static void xuatCsv(Component chu, TaiKhoan tk, BaoCaoDAO dao, BaoCao bc) {
        try {
            dao.kiemTraQuyen(tk);
            if (bc == null) {
                throw new IllegalArgumentException("Hãy thống kê trước khi xuất.");
            }
            JFileChooser chon = new JFileChooser();
            chon.setDialogTitle("Xuất báo cáo CSV");
            chon.setSelectedFile(
                    new File((bc.getMaBaoCao().isEmpty() ? "BaoCao-" + bc.getTuNgay() : bc.getMaBaoCao()) + ".csv"));
            if (chon.showSaveDialog(chu) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File file = chon.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getPath() + ".csv");
            }
            if (file.exists() && JOptionPane.showConfirmDialog(chu, "File đã có. Bạn muốn ghi đè?", "Xuất CSV",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            dao.kiemTraQuyen(tk);
            ghiCsv(file, bc);
            JOptionPane.showMessageDialog(chu, "Đã xuất báo cáo:\n" + file.getAbsolutePath());
        } catch (IOException | RuntimeException ex) {
            JOptionPane.showMessageDialog(chu, "Không xuất được báo cáo: " + ex.getMessage(), "Xuất CSV",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Ghi UTF-8 có BOM và dấu chấm phẩy để Excel đọc được tiếng Việt. */
    public static void ghiCsv(File file, BaoCao bc) throws IOException {
        try (BufferedWriter ghi = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            ghi.write('\uFEFF');
            ghi.write("sep=;\r\n");
            ghiDong(ghi, new String[] { "Mã báo cáo", bc.getMaBaoCao() });
            ghiDong(ghi, new String[] { "Loại báo cáo", bc.getLoai() });
            ghiDong(ghi, new String[] { "Khoảng ngày", BaoCaoDAO.khoangNgay(bc) });
            ghiDong(ghi, new String[] { "Thời điểm chụp số liệu",
                    bc.getNgayLap().format(DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm:ss")) });
            ghiDong(ghi, new String[] { "Người lập", bc.getNguoiLap() });
            ghiDong(ghi, new String[] { "Căn cứ",
                    BaoCao.TON_KHO.equals(bc.getLoai()) ? "Tồn hiện tại, không phải tồn lịch sử"
                            : BaoCao.DOANH_THU.equals(bc.getLoai()) ? "Ngày thanh toán, chỉ hóa đơn đã thu"
                                    : "Lệnh theo ngày phát hành; tiếp nhận theo ngày nhận; trạng thái tại lúc lập" });
            String[] ten = bc.getTenChiSo();
            long[] so = bc.getGiaTri();
            for (int i = 0; i < ten.length; i++) {
                ghiDong(ghi, new String[] { ten[i], "" + so[i] });
            }
            ghi.newLine();
            ghiDong(ghi, bc.getTenCot());
            for (String[] dong : bc.getCacDong()) {
                String[] banXuat = dong.clone();
                if (BaoCao.TON_KHO.equals(bc.getLoai())) {
                    banXuat[3] = banXuat[3].replace('.', ',');
                    banXuat[4] = banXuat[4].replace('.', ',');
                }
                ghiDong(ghi, banXuat);
            }
        }
    }

    /** Bọc ô và thoát dấu ngoặc kép; nội dung văn bản không trở thành công thức Excel. */
    private static void ghiDong(BufferedWriter ghi, String[] dong) throws IOException {
        for (int i = 0; i < dong.length; i++) {
            if (i > 0) {
                ghi.write(';');
            }
            String chu = dong[i];
            String boKhoang = chu.trim();
            if (!boKhoang.isEmpty() && "=+-@".indexOf(boKhoang.charAt(0)) >= 0) {
                chu = "'" + chu;
            }
            ghi.write('"');
            ghi.write(chu.replace("\"", "\"\""));
            ghi.write('"');
        }
        ghi.write("\r\n");
    }

    /** Xếp chỉ số thành hai cột trên cửa sổ nhỏ, tối đa bốn cột khi đủ rộng. */
    private static final class LuoiChiSo extends JPanel {
        private static final long serialVersionUID = 1L;

        /** Tạo lưới chỉ số không có nền riêng. */
        LuoiChiSo() {
            super(new GridLayout(0, 4, 14, 14));
            setOpaque(false);
        }

        /** Đổi số cột theo chiều rộng thực tế. */
        @Override
        public void doLayout() {
            int cot = soCot();
            GridLayout luoi = (GridLayout) getLayout();
            if (luoi.getColumns() != cot) {
                luoi.setColumns(cot);
                revalidate();
            }
            super.doLayout();
        }

        /** Dùng chiều rộng vùng cuộn hiện tại, tránh giữ kích thước cửa sổ cũ. */
        private int soCot() {
            JViewport view = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, this);
            int rong = view == null ? getWidth() : view.getWidth();
            return rong > 0 && rong < 900 ? 2 : Math.min(4, Math.max(1, getComponentCount()));
        }

        /** Chừa đủ chiều cao cho hai hàng khi cửa sổ hẹp. */
        @Override
        public Dimension getPreferredSize() {
            int cao = 64;
            for (Component c : getComponents()) {
                cao = Math.max(cao, c.getPreferredSize().height);
            }
            int cot = soCot();
            int hang = Math.max(1, (getComponentCount() + cot - 1) / cot);
            return new Dimension(600, hang * cao + (hang - 1) * 14);
        }
    }
}
