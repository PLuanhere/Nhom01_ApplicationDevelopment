package ui.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

// Thành phần trình bày của nhánh kiểm tra, dùng màu và font từ UiTheme của leader.
public final class KiemTraUi {
    public static final Color NGUY_HIEM = new Color(0xB3261E);

    public static final String QUY_TAC = "Kỹ thuật chỉ sửa nháp thuộc phân công. Chỉ báo cáo hoàn tất hợp lệ "
            + "mới làm căn cứ báo giá; báo cáo đã liên kết được giữ lại.";

    private KiemTraUi() {
    }

    /**
     * @param chu nội dung
     * @param dam có dùng font đậm
     * @return nhãn theo theme chung
     */
    // Tạo nhãn có chiều cao đủ cho font để không cắt chữ và dấu tiếng Việt.
    public static JLabel taoNhan(String chu, boolean dam) {
        JLabel lblChu = new JLabel(chu);
        lblChu.setFont(dam ? UiTheme.BRAND_FONT : UiTheme.SHELL_FONT);
        lblChu.setForeground(UiTheme.TEXT);
        int chieuCao = Math.max(20, lblChu.getFontMetrics(lblChu.getFont()).getHeight());
        lblChu.setPreferredSize(new Dimension(0, chieuCao));
        return lblChu;
    }

    /**
     * @param chu nội dung
     * @return vùng văn bản chỉ đọc
     */
    // Hiển thị văn bản dài có tự xuống dòng.
    public static JTextArea taoVanBan(String chu) {
        JTextArea txaChu = new JTextArea(chu);
        txaChu.setFont(UiTheme.SHELL_FONT);
        txaChu.setForeground(UiTheme.TEXT);
        txaChu.setOpaque(false);
        txaChu.setEditable(false);
        txaChu.setLineWrap(true);
        txaChu.setWrapStyleWord(true);
        return txaChu;
    }

    /**
     * @param chu nhãn nút
     * @param lenh mã thao tác
     * @param chinh nút chính
     * @param xuLy đối tượng xử lý sự kiện
     * @return nút đã định dạng
     */
    // Tạo nút theo theme và dùng viền đỏ, chữ đỏ cho thao tác xóa hoặc bỏ thay đổi.
    public static JButton taoNut(String chu, String lenh, boolean chinh, ActionListener xuLy) {
        JButton btnNut = new JButton(chu);
        UiTheme.styleButton(btnNut, chinh);
        if ("XOA".equals(lenh) || "BO_THAY_DOI".equals(lenh) || "Xóa".equals(chu)
                || "Bỏ thay đổi".equals(chu)) {
            btnNut.setForeground(NGUY_HIEM);
            btnNut.setBackground(UiTheme.SURFACE);
            btnNut.setBorder(BorderFactory.createLineBorder(NGUY_HIEM));
        }
        int chieuRong = Math.max(82, btnNut.getFontMetrics(btnNut.getFont()).stringWidth(chu) + 24);
        btnNut.setPreferredSize(new Dimension(chieuRong, 30));
        btnNut.setActionCommand(lenh);
        btnNut.addActionListener(xuLy);
        return btnNut;
    }

    /**
     * @param tieuDe tiêu đề thẻ
     * @return thẻ nền trắng có viền
     */
    // Tạo thẻ để nhóm thông tin hoặc các trường nhập liên quan.
    public static JPanel taoThe(String tieuDe) {
        JPanel pnlThe = new JPanel(new BorderLayout(8, 8));
        pnlThe.setBackground(UiTheme.SURFACE);
        pnlThe.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.BORDER),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        pnlThe.add(taoNhan(tieuDe, true), BorderLayout.NORTH);
        return pnlThe;
    }

    /**
     * @param chu nhãn trường
     * @param truong thành phần nhập hoặc hiển thị
     * @return panel chứa nhãn và trường
     */
    // Ghép nhãn ở trên và trường nhập ở dưới thành một ô biểu mẫu.
    public static JPanel taoTruong(String chu, JComponent truong) {
        JPanel pnlTruong = new JPanel(new BorderLayout(0, 4));
        pnlTruong.setOpaque(false);
        JLabel lblNhan = taoNhan(chu, false);
        lblNhan.setFont(UiTheme.CAPTION_FONT);
        lblNhan.setPreferredSize(new Dimension(0, 16));
        pnlTruong.add(lblNhan, BorderLayout.NORTH);
        truong.setFont(UiTheme.SHELL_FONT);
        if (truong instanceof JTextField || truong instanceof JComboBox) {
            truong.setPreferredSize(new Dimension(0, 30));
            truong.setBackground(UiTheme.SURFACE);
            truong.setForeground(UiTheme.TEXT);
            truong.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));
            if (truong instanceof JTextField && !((JTextField) truong).isEditable()) {
                truong.setBackground(UiTheme.WINDOW);
            }
            JPanel pnlO = new JPanel(new BorderLayout());
            pnlO.setOpaque(false);
            pnlO.add(truong, BorderLayout.NORTH);
            pnlTruong.add(pnlO, BorderLayout.CENTER);
        } else {
            pnlTruong.add(truong, BorderLayout.CENTER);
        }
        return pnlTruong;
    }

    /**
     * @param cot tên các cột
     * @return model bảng chỉ đọc
     */
    // Tạo DefaultTableModel không cho sửa dữ liệu trực tiếp trên bảng.
    public static DefaultTableModel taoModel(String[] cot) {
        return new DefaultTableModel(cot, 0) {
            private static final long serialVersionUID = 1L;

            /** {@inheritDoc} */
            // Giữ các ô dữ liệu ở chế độ chỉ đọc.
            @Override
            public boolean isCellEditable(int dong, int cot) {
                return false;
            }
        };
    }

    /**
     * @param modelBang model chứa dữ liệu
     * @return bảng đã định dạng
     */
    // Định dạng bảng theo thiết kế và chỉ cho chọn một dòng.
    public static JTable taoBang(DefaultTableModel modelBang) {
        JTable tblBang = new JTable(modelBang);
        tblBang.setFont(UiTheme.SHELL_FONT);
        tblBang.setForeground(UiTheme.TEXT);
        tblBang.setBackground(UiTheme.SURFACE);
        tblBang.setRowHeight(30);
        tblBang.setGridColor(UiTheme.BORDER);
        tblBang.setShowGrid(true);
        tblBang.setSelectionBackground(new Color(0xDCEAFE));
        tblBang.setSelectionForeground(UiTheme.TEXT);
        tblBang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblBang.getTableHeader().setReorderingAllowed(false);
        tblBang.getTableHeader().setPreferredSize(new Dimension(0, 30));
        DefaultTableCellRenderer rendererBang = new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            /** {@inheritDoc} */
            // Định dạng font, nền và khoảng đệm cho ô tiêu đề hoặc ô dữ liệu.
            @Override
            public Component getTableCellRendererComponent(JTable tblBangDuLieu, Object giaTri,
                    boolean chon, boolean coTieuDiem, int dong, int cot) {
                JLabel lblO = (JLabel) super.getTableCellRendererComponent(
                        tblBangDuLieu, giaTri, chon, coTieuDiem, dong, cot);
                lblO.setFont(dong < 0 ? UiTheme.BRAND_FONT.deriveFont(13.0f) : UiTheme.SHELL_FONT);
                lblO.setBackground(dong < 0 ? UiTheme.WINDOW
                        : chon ? tblBangDuLieu.getSelectionBackground() : UiTheme.SURFACE);
                lblO.setForeground(UiTheme.TEXT);
                lblO.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return lblO;
            }
        };
        tblBang.setDefaultRenderer(Object.class, rendererBang);
        tblBang.getTableHeader().setDefaultRenderer(rendererBang);
        return tblBang;
    }

    /**
     * @param thanhPhan nội dung
     * @return vùng cuộn có viền
     */
    // Bọc bảng hoặc nội dung dài trong vùng cuộn theo theme.
    public static JScrollPane taoVungCuon(Component thanhPhan) {
        JScrollPane scrCuon = new JScrollPane(thanhPhan);
        scrCuon.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));
        scrCuon.getViewport().setBackground(UiTheme.SURFACE);
        scrCuon.getVerticalScrollBar().setUnitIncrement(16);
        return scrCuon;
    }

    /**
     * @param winChu khung cha
     * @param tieuDe tiêu đề
     * @param chu nội dung thông báo
     * @param dongY nhãn nút xác nhận
     * @param huy nhãn nút hủy, null khi chỉ thông báo
     * @return người dùng có xác nhận hay không
     */
    // Hiển thị thông báo, xác nhận xóa hoặc bỏ thay đổi theo bố cục và màu nút của Figma.
    public static boolean xacNhan(JFrame winChu, String tieuDe, String chu, String dongY, String huy) {
        final JDialog winHoi = new JDialog(winChu, tieuDe, true);
        final boolean[] ketQua = {false};
        boolean boThayDoi = "Bỏ thay đổi".equals(dongY);
        boolean xoa = "Xóa".equals(dongY);
        ActionListener xuLy = new ActionListener() {
            /** {@inheritDoc} */
            // Chỉ xác nhận khi bấm nút đồng ý; Hủy hoặc dấu đóng giữ nguyên dữ liệu.
            @Override
            public void actionPerformed(ActionEvent suKien) {
                ketQua[0] = "DONG_Y".equals(suKien.getActionCommand());
                winHoi.dispose();
            }
        };
        winHoi.setUndecorated(true);
        winHoi.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        winHoi.getRootPane().setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));
        JPanel pnlChinh = new JPanel(new BorderLayout());
        pnlChinh.setBackground(UiTheme.WINDOW);
        JPanel pnlDau = new JPanel(new BorderLayout(8, 0));
        pnlDau.setBackground(UiTheme.SURFACE);
        pnlDau.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        JLabel lblTieuDe = taoNhan(tieuDe, true);
        lblTieuDe.setFont(UiTheme.BRAND_FONT.deriveFont(13.0f));
        pnlDau.add(lblTieuDe, BorderLayout.CENTER);
        JButton btnDong = taoNut("×", "HUY", false, xuLy);
        btnDong.setForeground(UiTheme.MUTED);
        btnDong.setBorder(BorderFactory.createEmptyBorder());
        btnDong.setPreferredSize(new Dimension(24, 20));
        pnlDau.add(btnDong, BorderLayout.EAST);
        pnlChinh.add(pnlDau, BorderLayout.NORTH);
        JPanel pnlThongBao = new JPanel(new BorderLayout(0, 12));
        pnlThongBao.setOpaque(false);
        pnlThongBao.setBorder(BorderFactory.createEmptyBorder(16, 16, 0, 16));
        String[] cacDoan = chu.split("\n\n", 2);
        JTextArea txaNhan = taoVanBan(cacDoan[0]);
        if (boThayDoi || xoa) {
            txaNhan.setFont(UiTheme.BRAND_FONT);
        }
        if (xoa) {
            txaNhan.setForeground(NGUY_HIEM);
        }
        pnlThongBao.add(txaNhan, BorderLayout.NORTH);
        String moTa = boThayDoi
                ? "Nếu bỏ thay đổi, danh sách giữ nguyên dữ liệu trước khi mở biểu mẫu."
                : cacDoan.length > 1 ? cacDoan[1] : "";
        if (!moTa.isEmpty()) {
            pnlThongBao.add(taoVanBan(moTa), BorderLayout.CENTER);
        }
        pnlChinh.add(pnlThongBao, BorderLayout.CENTER);
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        pnlNut.setOpaque(false);
        if (huy != null) {
            pnlNut.add(taoNut(huy, "HUY", false, xuLy));
        }
        pnlNut.add(taoNut(dongY, "DONG_Y", false, xuLy));
        pnlChinh.add(pnlNut, BorderLayout.SOUTH);
        winHoi.setContentPane(pnlChinh);
        winHoi.setSize(560, boThayDoi ? 172 : xoa ? 212 : 192);
        winHoi.setLocationRelativeTo(winChu);
        winHoi.setVisible(true);
        return ketQua[0];
    }
}