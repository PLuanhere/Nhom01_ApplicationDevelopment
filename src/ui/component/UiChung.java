package ui.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/** Tạo và định dạng các thành phần Swing dùng chung cho màn hình Quản lý. */
public final class UiChung {

    /** Ngăn tạo đối tượng vì lớp chỉ cung cấp các hàm giao diện dùng chung. */
    private UiChung() {
    }

    /** Tạo nhãn có font, màu chữ và tooltip thống nhất. */
    public static JLabel taoNhan(String chu, float coChu, boolean dam, Color mau) {
        JLabel lbl = new JLabel(chu);
        lbl.setFont((dam ? UiTheme.APP_BOLD_FONT : UiTheme.APP_FONT).deriveFont(coChu));
        lbl.setForeground(mau);
        lbl.setToolTipText(chu);
        return lbl;
    }

    /** Tạo thẻ nội dung nền trắng có đường viền dùng chung. */
    public static JPanel taoThe(LayoutManager layout) {
        JPanel pnl = UiTheme.roundedPanel(layout, Color.WHITE, 8);
        pnl.setBorder(UiTheme.roundedBorder(UiTheme.APP_BORDER, 8, 0, 0));
        return pnl;
    }

    /** Tạo breadcrumb, tiêu đề, mô tả và nút quay lại nếu có. */
    public static JPanel taoDauTrang(String breadcrumb, String tieuDe, String moTa, JButton... cacNut) {
        JPanel pnl = new JPanel(new BorderLayout(0, 8));
        pnl.setOpaque(false);
        pnl.add(taoNhan(breadcrumb, 12, false, UiTheme.APP_MUTED), BorderLayout.NORTH);
        JPanel pnlChu = new JPanel(new BorderLayout(0, 6));
        pnlChu.setOpaque(false);
        pnlChu.add(taoNhan(tieuDe, 24, true, UiTheme.APP_TEXT), BorderLayout.NORTH);
        pnlChu.add(taoNhan(moTa, 13, false, UiTheme.APP_MUTED));
        pnl.add(pnlChu);
        if (cacNut.length > 0) {
            pnl.add(taoHangNut(cacNut), BorderLayout.SOUTH);
        }
        return pnl;
    }

    /** Tạo nút chính hoặc phụ với kích thước và kiểu hiển thị thống nhất. */
    public static JButton taoNut(String chu, boolean chinh) {
        JButton nut = UiTheme.createAppButton(chu, chinh);
        nut.setFont(UiTheme.APP_FONT.deriveFont(13.0f));
        nut.setPreferredSize(new Dimension(nut.getPreferredSize().width, 38));
        return nut;
    }

    /** Tạo ô nhập hoặc ô chỉ đọc theo kiểu chung của Quản lý. */
    public static JTextField taoONhap(boolean choSua) {
        JTextField txt = new JTextField();
        txt.setEditable(choSua);
        dinhDangO(txt);
        if (!choSua) {
            txt.setBackground(new Color(0xF5F7FB));
        }
        return txt;
    }

    /** Thiết lập font, chiều cao, nền và viền của ô nhập hoặc hộp chọn. */
    public static void dinhDangO(JComponent oNhap) {
        oNhap.setFont(UiTheme.APP_FONT.deriveFont(14.0f));
        oNhap.setPreferredSize(new Dimension(180, 40));
        oNhap.setBackground(Color.WHITE);
        if (!(oNhap instanceof JComboBox<?>)) {
            oNhap.setBorder(UiTheme.roundedBorder(UiTheme.APP_BORDER, 6, 0, 12));
        }
    }

    /** Ghép nhãn, ô nhập và vùng báo lỗi nếu cần. */
    public static JPanel taoTruong(String nhan, JComponent oNhap) {
        JPanel pnl = new JPanel(new BorderLayout(0, 6));
        pnl.setOpaque(false);
        JLabel lbl = taoNhan(nhan, 13, true, UiTheme.APP_MUTED);
        lbl.setLabelFor(oNhap);
        oNhap.getAccessibleContext().setAccessibleName(nhan);
        pnl.add(lbl, BorderLayout.NORTH);
        pnl.add(oNhap);
        return pnl;
    }

    /** Xếp hai trường thông tin thành hai cột có cùng chiều rộng. */
    public static JPanel taoHang(JComponent trai, JComponent phai) {
        JPanel pnl = new JPanel(new GridLayout(1, 2, 14, 0));
        pnl.setOpaque(false);
        pnl.add(trai);
        pnl.add(phai);
        return pnl;
    }

    /** Tạo thanh thông báo và các nút thao tác cố định dưới biểu mẫu. */
    public static JPanel taoChanTrang(JLabel thongBao, JButton... cacNut) {
        JPanel pnl = taoThe(new BorderLayout(0, 8));
        pnl.setBorder(
                BorderFactory.createCompoundBorder(pnl.getBorder(), BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        thongBao.setVisible(!thongBao.getText().isBlank());
        pnl.add(thongBao, BorderLayout.NORTH);
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.setOpaque(false);
        for (JButton nut : cacNut) {
            pnlNut.add(nut);
        }
        pnl.add(pnlNut);
        return pnl;
    }

    /** Tạo vùng lưu ý chỉ đọc, tự xuống dòng theo chiều rộng. */
    public static JTextArea taoLuuY(String chu) {
        JTextArea txt = new JTextArea(chu);
        txt.setEditable(false);
        txt.setFocusable(false);
        txt.setRows(2);
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        txt.setFont(UiTheme.APP_FONT.deriveFont(13.0f));
        txt.setForeground(new Color(0x806326));
        txt.setBackground(new Color(0xFFF6D9));
        txt.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        return txt;
    }

    /** Tạo vùng cuộn dọc, giữ nội dung bám chiều rộng cửa sổ. */
    public static JScrollPane taoVungCuon(JPanel noiDung) {
        JPanel pnlBoc = new ScrollablePanel();
        pnlBoc.add(noiDung, BorderLayout.NORTH);
        JScrollPane scr = new JScrollPane(pnlBoc);
        scr.setBorder(BorderFactory.createEmptyBorder());
        scr.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scr.getViewport().setBackground(UiTheme.APP_BACKGROUND);
        scr.getVerticalScrollBar().setUnitIncrement(24);
        return scr;
    }

    /** Định dạng số để hiển thị với dấu phân cách theo cách viết Việt Nam. */
    public static String so(double giaTri) {
        DecimalFormatSymbols kyHieu = new DecimalFormatSymbols();
        kyHieu.setGroupingSeparator('.');
        kyHieu.setDecimalSeparator(',');
        return new DecimalFormat("#,##0.###", kyHieu).format(giaTri);
    }

    /** Định dạng số để hiển thị với dấu phân cách theo cách viết Việt Nam. */
    public static String so(long giaTri) {
        DecimalFormatSymbols kyHieu = new DecimalFormatSymbols();
        kyHieu.setGroupingSeparator('.');
        return new DecimalFormat("#,##0", kyHieu).format(giaTri);
    }

    /** Đọc số lượng không âm từ nội dung nhập theo định dạng Việt Nam. */
    public static double docSo(String chu, String tenTruong) {
        String so = chuanHoaSo(chu, tenTruong);
        try {
            double giaTri = Double.parseDouble(so);
            if (!(giaTri >= 0 && giaTri < Long.MAX_VALUE / 1000.0)) {
                throw new NumberFormatException();
            }
            return giaTri;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(tenTruong + ": số lượng vượt giới hạn cho phép.");
        }
    }

    /** Đọc tiền nguyên đồng trực tiếp thành long, từ chối phần lẻ và số vượt giới hạn. */
    public static long docTien(String chu, String tenTruong) {
        String so = chuanHoaSo(chu, tenTruong);
        int dauPhay = so.indexOf('.');
        if (dauPhay >= 0) {
            for (int i = dauPhay + 1; i < so.length(); i++) {
                if (so.charAt(i) != '0') {
                    throw new IllegalArgumentException(tenTruong + ": tiền phải là số nguyên đồng.");
                }
            }
            so = so.substring(0, dauPhay);
        }
        try {
            return Long.parseLong(so);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(tenTruong + ": số tiền vượt giới hạn cho phép.");
        }
    }

    /** Kiểm tra ký tự, nhóm nghìn và dấu phẩy trước khi chuyển nội dung thành số. */
    private static String chuanHoaSo(String chu, String tenTruong) {
        String so = chu == null ? "" : chu.trim();
        int soChuSo = 0;
        boolean daCoNhom = false;
        boolean phanLe = false;
        for (int i = 0; i < so.length(); i++) {
            char c = so.charAt(i);
            if (c >= '0' && c <= '9') {
                soChuSo++;
            } else if (c == '.' && !phanLe && soChuSo > 0
                    && ((!daCoNhom && soChuSo <= 3) || (daCoNhom && soChuSo == 3))) {
                daCoNhom = true;
                soChuSo = 0;
            } else if (c == ',' && !phanLe && soChuSo > 0 && (!daCoNhom || soChuSo == 3)) {
                phanLe = true;
                soChuSo = 0;
            } else {
                throw new IllegalArgumentException(tenTruong + ": nhập số không âm; phần lẻ dùng dấu phẩy.");
            }
        }
        if (soChuSo == 0 || (!phanLe && daCoNhom && soChuSo != 3)) {
            throw new IllegalArgumentException(tenTruong + ": số không hợp lệ.");
        }
        return so.replace(".", "").replace(',', '.');
    }

    /** Đặt bảng và header vào vùng cuộn có nền trắng. */
    public static JScrollPane cuonBang(JTable tbl, int soDong) {
        tbl.setPreferredScrollableViewportSize(new Dimension(0, Math.max(1, soDong) * 48));
        JScrollPane scr = new JScrollPane(tbl);
        scr.setColumnHeaderView(tbl.getTableHeader());
        scr.setBorder(BorderFactory.createEmptyBorder());
        scr.getViewport().setBackground(Color.WHITE);
        scr.getVerticalScrollBar().setUnitIncrement(48);
        return scr;
    }

    /** Giữ chữ trong hộp chọn dễ đọc khi điều khiển bị khóa. */
    public static void giuChuRo(JComboBox<?> cmb) {
        cmb.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            /** Vẽ nội dung giao diện và giữ chữ hoặc gợi ý nhập liệu dễ đọc. */
            @Override
            protected void paintComponent(Graphics graphics) {
                if (!cmb.isEnabled()) {
                    setForeground(UiTheme.APP_TEXT);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
                super.paintComponent(graphics);
            }
        });
    }

    /** Hỏi xác nhận bỏ dữ liệu chưa lưu trước khi rời form. */
    public static boolean hoiBoThayDoi(Component cha, boolean coThayDoi) {
        if (!coThayDoi) {
            return true;
        }
        Object[] nut = { "Bỏ thay đổi", "Tiếp tục chỉnh sửa" };
        return JOptionPane.showOptionDialog(cha, "Thông tin có thay đổi chưa lưu.", "Rời màn hình",
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, nut, nut[1]) == 0;
    }

    /** Tạo ô tìm kiếm rỗng với gợi ý và tên hỗ trợ truy cập. */
    public static JTextField taoTimKiem(String goiY) {
        JTextField txt = taoONhap(true);
        txt.setToolTipText(goiY);
        txt.getAccessibleContext().setAccessibleName(goiY);
        return txt;
    }

    /** Xếp ô tìm kiếm, các bộ lọc và nút làm mới trên cùng một hàng. */
    public static JPanel taoBoLoc(JTextField txtTim, JButton btnMoi, JComboBox<?>... cacLoc) {
        JPanel pnl = taoThe(new GridBagLayout());
        pnl.setBorder(
                BorderFactory.createCompoundBorder(pnl.getBorder(), BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        txtTim.setMinimumSize(new Dimension(90, 40));
        JPanel pnlTim = new JPanel(new BorderLayout(10, 0));
        pnlTim.setOpaque(false);
        JLabel lblTim = taoNhan("Tìm kiếm", 13, true, UiTheme.APP_MUTED);
        lblTim.setLabelFor(txtTim);
        pnlTim.add(lblTim, BorderLayout.WEST);
        pnlTim.add(txtTim);
        pnlTim.setMinimumSize(new Dimension(150, 40));
        pnl.add(pnlTim, c);
        c.weightx = 0;
        for (int i = 0; i < cacLoc.length; i++) {
            JComboBox<?> cmb = cacLoc[i];
            dinhDangO(cmb);
            cmb.setFont(UiTheme.APP_FONT.deriveFont(13.0f));
            cmb.setPreferredSize(new Dimension(165, 40));
            cmb.setMinimumSize(new Dimension(145, 40));
            c.gridx = i + 1;
            c.insets = new Insets(0, 10, 0, 0);
            pnl.add(cmb, c);
        }
        c.gridx = cacLoc.length + 1;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, 10, 0, 0);
        pnl.add(btnMoi, c);
        return pnl;
    }

    /** Xếp các nút thao tác thành một hàng căn trái. */
    public static JPanel taoHangNut(JButton... cacNut) {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnl.setOpaque(false);
        for (int i = 0; i < cacNut.length; i++) {
            if (i > 0) {
                pnl.add(Box.createHorizontalStrut(8));
            }
            pnl.add(cacNut[i]);
        }
        return pnl;
    }

    /** Tạo bảng chỉ đọc, phủ chiều rộng và không cho kéo thay đổi cột. */
    public static JTable taoBang(DefaultTableModel model, int... doRong) {
        JTable tbl = new JTable(model);
        tbl.getTableHeader().setReorderingAllowed(false);
        tbl.getTableHeader().setResizingAllowed(false);
        dinhDangBang(tbl, doRong);
        return tbl;
    }

    /** Định dạng dòng, header, cách chọn và độ rộng cột của bảng. */
    public static void dinhDangBang(JTable tbl, int... doRong) {
        tbl.setDefaultEditor(Object.class, null);
        tbl.setFont(UiTheme.APP_FONT.deriveFont(13.0f));
        tbl.setRowHeight(48);
        tbl.setShowGrid(false);
        tbl.setIntercellSpacing(new Dimension(0, 0));
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl.setSelectionBackground(new Color(0xEAF2FC));
        tbl.setSelectionForeground(UiTheme.APP_TEXT);
        tbl.setFillsViewportHeight(true);
        tbl.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tbl.getTableHeader().setPreferredSize(new Dimension(0, 40));
        tbl.getTableHeader().setResizingAllowed(false);
        tbl.getTableHeader().setReorderingAllowed(false);
        tbl.getTableHeader().setResizingAllowed(false);
        tbl.setDefaultRenderer(Object.class, new ChuBang());
        tbl.getTableHeader().setDefaultRenderer(new ChuBang());
        for (int i = 0; i < tbl.getColumnCount(); i++) {
            tbl.getColumnModel().getColumn(i).setResizable(false);
            tbl.getColumnModel().getColumn(i).setMinWidth(45);
            if (i < doRong.length) {
                tbl.getColumnModel().getColumn(i).setPreferredWidth(doRong[i]);
            }
        }
    }

    /** Ghép tiêu đề, số kết quả, lựa chọn sắp xếp và bảng thành thẻ danh sách. */
    public static JPanel theBang(String ten, JTable tbl, JLabel soLuong, JLabel cuoi) {
        return theBang(ten, tbl, soLuong, cuoi, null);
    }

    /** Ghép tiêu đề, số kết quả, lựa chọn sắp xếp và bảng thành thẻ danh sách. */
    public static JPanel theBang(String ten, JTable tbl, JLabel soLuong, JLabel cuoi, JComboBox<String> sapXep) {
        JPanel pnl = taoThe(new BorderLayout());
        JPanel pnlDau = new JPanel(new BorderLayout(10, 10));
        pnlDau.setOpaque(false);
        pnlDau.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        JPanel pnlTen = new JPanel(new BorderLayout(0, 6));
        pnlTen.setOpaque(false);
        pnlTen.add(taoNhan(ten, 16, true, UiTheme.APP_TEXT), BorderLayout.NORTH);
        pnlTen.add(soLuong);
        pnlDau.add(pnlTen);
        if (sapXep != null) {
            dinhDangO(sapXep);
            sapXep.setPreferredSize(new Dimension(190, 38));
            sapXep.setFont(UiTheme.APP_FONT.deriveFont(13.0f));
            sapXep.getAccessibleContext().setAccessibleName("Sắp xếp danh sách");
            pnlDau.add(sapXep, BorderLayout.EAST);
        }
        pnl.add(pnlDau, BorderLayout.NORTH);
        pnl.add(cuonBang(tbl, 8));
        cuoi.setFont(UiTheme.APP_FONT.deriveFont(12.0f));
        cuoi.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        pnl.add(cuoi, BorderLayout.SOUTH);
        return pnl;
    }

    private static final class ChuBang extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        /** Định dạng chữ, số, màu trạng thái và tooltip của ô bảng. */
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value, boolean chon, boolean focus, int dong,
                int cot) {
            super.getTableCellRendererComponent(tbl, value, chon, focus, dong, cot);
            if (value instanceof Long) {
                setText(so((Long) value));
            } else if (value instanceof Double) {
                setText(so((Double) value));
            }
            String chu = getText();
            setToolTipText(chu);
            setHorizontalAlignment(value instanceof Number ? SwingConstants.RIGHT : SwingConstants.LEFT);
            setFont((dong < 0 || cot == 0 ? UiTheme.APP_BOLD_FONT : UiTheme.APP_FONT).deriveFont(13.0f));
            setBackground(dong < 0 ? new Color(0xF4F7FB) : chon ? tbl.getSelectionBackground() : Color.WHITE);
            setForeground(dong < 0 ? UiTheme.APP_MUTED : UiTheme.APP_TEXT);
            if (dong >= 0) {
                if ("Xem chi tiết".equals(chu)) {
                    setForeground(UiTheme.APP_PRIMARY);
                } else if ("Cảnh báo".equals(chu) || "Sắp hết".equals(chu) || "Hết hàng".equals(chu)
                        || "Đã khóa".equals(chu)) {
                    setForeground(new Color(0xB42318));
                } else if ("Tạm dừng".equals(chu) || "Đã nghỉ".equals(chu) || "Ngừng hoạt động".equals(chu)) {
                    setForeground(new Color(0x986000));
                } else if ("Áp dụng".equals(chu) || "Đủ tồn".equals(chu) || "Đang hoạt động".equals(chu)
                        || "Đang làm".equals(chu) || "Đã nhập".equals(chu)) {
                    setForeground(new Color(0x087443));
                }
            }
            setBorder(
                    BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE9EDF2)),
                            BorderFactory.createEmptyBorder(0, 12, 0, 12)));
            return this;
        }
    }

    /** So sánh từng giá trị trong hai mảng để phát hiện dữ liệu chưa lưu. */
    public static boolean daThayDoi(String[] cu, String[] moi) {
        if (cu == null || cu.length != moi.length) {
            return true;
        }
        for (int i = 0; i < cu.length; i++) {
            if (!cu[i].equals(moi[i])) {
                return true;
            }
        }
        return false;
    }
}
