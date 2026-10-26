package ui.component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Color;
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

public final class SuaChuaUi {
	public static final Color NGUY_HIEM = new Color(0xB3261E);
    public static final String QUY_TAC = "Chỉ xóa lệnh chưa bắt đầu và chưa có phụ tùng, kết quả hoặc hóa đơn. "
            + "Lệnh đang làm/hoàn thành cần ghi nhận điều chỉnh, không xóa mất lịch sử.";

    private SuaChuaUi() {
    }

    /**
     * @param chu nội dung
     * @param dam có in đậm
     * @return nhãn theo theme chung
     */
    // Tạo nhãn dùng chung với font và màu chữ của leader.
    public static JLabel taoNhan(String chu, boolean dam) {
        JLabel lblChu = new JLabel(chu);
        lblChu.setFont(dam ? UiTheme.BRAND_FONT : UiTheme.SHELL_FONT);
        lblChu.setForeground(UiTheme.TEXT);
        lblChu.setPreferredSize(new Dimension(0, 20));
        return lblChu;
    }

    /**
     * @param chu nội dung
     * @return vùng văn bản chỉ đọc, có xuống dòng
     */
    // Hiển thị văn bản dài mà không làm mất nội dung khi thu nhỏ cửa sổ.
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
    // Tạo thẻ để nhóm các thông tin hoặc trường nhập liên quan.
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
     * @return panel chứa trường
     */
    // Ghép nhãn ở trên và trường nhập ở dưới thành một ô của biểu mẫu.
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
    // Tạo DefaultTableModel chỉ đọc cho các bảng trong nhánh sửa chữa.
    public static DefaultTableModel taoModel(String[] cot) {
        return new DefaultTableModel(cot, 0) {
            private static final long serialVersionUID = 1L;

            /** {@inheritDoc} */
            // Ngăn chỉnh sửa trực tiếp dữ liệu nghiệp vụ trên bảng.
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
    // Định dạng bảng theo thiết kế và giữ chế độ chọn một dòng.
    public static JTable taoBang(DefaultTableModel modelBang) {
        JTable tblBang = new JTable(modelBang);
        tblBang.setFont(UiTheme.SHELL_FONT);
        tblBang.setForeground(UiTheme.TEXT);
        tblBang.setBackground(UiTheme.SURFACE);
        tblBang.setRowHeight(30);
        tblBang.setGridColor(UiTheme.BORDER);
        tblBang.setShowGrid(true);
        tblBang.setSelectionBackground(new java.awt.Color(0xDCEAFE));
        tblBang.setSelectionForeground(UiTheme.TEXT);
        tblBang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblBang.getTableHeader().setReorderingAllowed(false);
        tblBang.getTableHeader().setPreferredSize(new Dimension(0, 30));
        DefaultTableCellRenderer rendererBang = new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            /** {@inheritDoc} */
            // Thêm lề ô và dùng kiểu tiêu đề hoặc dữ liệu phù hợp.
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
     * @return vùng cuộn có viền theo theme
     */
    // Bọc nội dung dài hoặc bảng trong JScrollPane để truy cập được khi cửa sổ nhỏ.
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
     * @param chu thông báo
     * @param dongY nhãn xác nhận
     * @param huy nhãn hủy, null nếu chỉ thông báo
     * @return có chọn nút xác nhận
     */
    // Hiển thị thông báo hoặc xác nhận bằng JDialog theo theme dùng chung.
    public static boolean xacNhan(JFrame winChu, String tieuDe, String chu, String dongY, String huy) {
        final JDialog winHoi = new JDialog(winChu, tieuDe, true);
        final boolean[] ketQua = {false};
        winHoi.setUndecorated(true);
        winHoi.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        winHoi.getRootPane().setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));
        winHoi.setLayout(new BorderLayout());
        JLabel lblTieuDe = taoNhan(tieuDe, true);
        lblTieuDe.setOpaque(true);
        lblTieuDe.setBackground(UiTheme.SURFACE);
        lblTieuDe.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        lblTieuDe.setPreferredSize(new Dimension(0, 32));
        winHoi.add(lblTieuDe, BorderLayout.NORTH);
        JTextArea txaChu = taoVanBan(chu);
        txaChu.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        winHoi.add(txaChu, BorderLayout.CENTER);
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        ActionListener xuLy = new ActionListener() {
            /** {@inheritDoc} */
            // Ghi nhận lựa chọn của người dùng và đóng hộp thoại xác nhận.
            @Override
            public void actionPerformed(ActionEvent suKien) {
                ketQua[0] = "DONG_Y".equals(suKien.getActionCommand());
                winHoi.dispose();
            }
        };
        if (huy != null) {
            pnlNut.add(taoNut(huy, "HUY", false, xuLy));
        }
        pnlNut.add(taoNut(dongY, "DONG_Y", huy == null, xuLy));
        winHoi.add(pnlNut, BorderLayout.SOUTH);
        winHoi.setSize(560, 212);
        winHoi.setLocationRelativeTo(winChu);
        winHoi.setVisible(true);
        return ketQua[0];
    }

    /**
     * @param taiKhoan tài khoản kỹ thuật
     * @return tên hiển thị của dữ liệu mẫu
     */
    // Đổi tài khoản kỹ thuật mẫu thành tên người để hiển thị trên giao diện.
    public static String layTenKyThuat(String taiKhoan) {
        if ("kythuat".equals(taiKhoan)) {
            return "Chung Ngô Minh Hoàng";
        }
        return "kythuatvu".equals(taiKhoan) ? "Nguyễn Minh Vũ" : "Chưa phân công";
    }
}