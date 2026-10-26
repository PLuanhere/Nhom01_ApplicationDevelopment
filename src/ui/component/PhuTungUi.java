package ui.component;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicCheckBoxUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicTableHeaderUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.View;

public final class PhuTungUi {
    public static final Color MAU_NGUY_HIEM = new Color(0xB3261E);
    public static final Color MAU_LUOI = new Color(0xDCE1E7);
    public static final Color MAU_CHON = new Color(0xDCEAFE);
    public static final String QUY_TAC = "Sửa/xóa nháp chưa xuất kho. Xác nhận sử dụng giảm tồn đúng một lần; "
            + "không vượt tồn và không dùng hạng mục chưa được khách duyệt.";

    private PhuTungUi() {
    }

    /**
     * Tạo nút thao tác theo thiết kế.
     * @param noiDung nhãn nút
     * @param chinh true cho nút xanh
     * @param nguyHiem true cho chữ đỏ và viền đỏ
     * @return nút chưa gắn sự kiện
     */
    // Tạo nút phẳng với chiều cao 30 px và khoảng đệm theo nội dung.
    public static JButton taoNut(String noiDung, boolean chinh, boolean nguyHiem) {
        JButton btnNut = new JButton(noiDung);
        btnNut.setUI(new BasicButtonUI());
        btnNut.setFont(UiTheme.SHELL_FONT.deriveFont(13.0f));
        btnNut.setOpaque(true);
        btnNut.setContentAreaFilled(true);
        btnNut.setFocusPainted(false);
        btnNut.setRolloverEnabled(false);
        btnNut.setMargin(new Insets(0, 8, 0, 8));
        btnNut.setBackground(chinh && !nguyHiem ? UiTheme.PRIMARY : UiTheme.SURFACE);
        btnNut.setForeground(nguyHiem ? MAU_NGUY_HIEM : chinh ? UiTheme.SURFACE : UiTheme.TEXT);
        Color mauVien = nguyHiem ? MAU_NGUY_HIEM : chinh ? UiTheme.PRIMARY : UiTheme.BORDER;
        btnNut.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(mauVien),
                BorderFactory.createEmptyBorder(0, 7, 0, 7)));
        int chieuRong = Math.max(82, btnNut.getFontMetrics(btnNut.getFont()).stringWidth(noiDung) + 18);
        btnNut.setPreferredSize(new Dimension(chieuRong, 30));
        btnNut.setMinimumSize(new Dimension(chieuRong, 30));
        return btnNut;
    }

    /**
     * Tạo nhãn giá trị hoặc tiêu đề nhóm.
     * @param noiDung nội dung nhãn
     * @param dam true cho tiêu đề nhóm
     * @return nhãn đã định dạng
     */
    // Tạo nhãn 13 px hoặc tiêu đề nhóm 14 px bằng font có sẵn của leader.
    public static JLabel taoNhan(String noiDung, boolean dam) {
        JLabel lblNhan = new JLabel(noiDung);
        lblNhan.setFont(dam ? UiTheme.BRAND_FONT.deriveFont(14.0f)
                : UiTheme.SHELL_FONT.deriveFont(13.0f));
        lblNhan.setForeground(UiTheme.TEXT);
        return lblNhan;
    }

    /**
     * Định dạng ô chọn.
     * @param cmbDanhSach danh sách cần định dạng
     */
    // Thay giao diện ô chọn mặc định bằng nền trắng, viền mảnh và mũi tên nhỏ.
    public static void dinhDangCombo(JComboBox<?> cmbDanhSach) {
        cmbDanhSach.setUI(new BasicComboBoxUI() {
            // Tạo nút mũi tên phẳng, không dùng nút mặc định của hệ điều hành.
            @Override
            protected JButton createArrowButton() {
                JButton btnMo = taoNut("▾", false, false);
                btnMo.setFont(new Font(Font.DIALOG, Font.PLAIN, 11));
                btnMo.setBorder(BorderFactory.createEmptyBorder());
                btnMo.setPreferredSize(new Dimension(24, 28));
                btnMo.setMinimumSize(new Dimension(24, 28));
                return btnMo;
            }
        });
        cmbDanhSach.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            // Căn chữ và màu lựa chọn đồng nhất giữa ô đóng và danh sách xổ xuống.
            @Override
            public Component getListCellRendererComponent(JList<?> danhSach, Object giaTri,
                    int viTri, boolean duocChon, boolean coTieuDiem) {
                JLabel lblGiaTri = (JLabel) super.getListCellRendererComponent(
                        danhSach, giaTri, viTri, duocChon, coTieuDiem);
                lblGiaTri.setFont(UiTheme.SHELL_FONT.deriveFont(13.0f));
                lblGiaTri.setForeground(UiTheme.TEXT);
                lblGiaTri.setBackground(duocChon && viTri >= 0 ? MAU_CHON : UiTheme.SURFACE);
                lblGiaTri.setBorder(BorderFactory.createEmptyBorder(2, 7, 2, 7));
                return lblGiaTri;
            }
        });
        cmbDanhSach.setFont(UiTheme.SHELL_FONT.deriveFont(13.0f));
        cmbDanhSach.setBackground(UiTheme.SURFACE);
        cmbDanhSach.setForeground(UiTheme.TEXT);
        cmbDanhSach.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));
        cmbDanhSach.setPreferredSize(new Dimension(0, 30));
        cmbDanhSach.setMinimumSize(new Dimension(0, 30));
    }

    /**
     * Tạo trường có nhãn phía trên.
     * @param nhan nhãn trường
     * @param thanhPhan ô nhập hoặc giá trị
     * @return vùng trường
     */
    // Ghép nhãn 11 px và điều khiển nhập, cách nhau 4 px.
    public static JPanel taoTruong(String nhan, JComponent thanhPhan) {
        JPanel pnlTruong = new JPanel(new BorderLayout(0, 4));
        pnlTruong.setOpaque(false);
        JLabel lblNhan = taoNhan(nhan, false);
        lblNhan.setFont(UiTheme.CAPTION_FONT.deriveFont(11.0f));
        lblNhan.setForeground(UiTheme.TEXT);
        lblNhan.setLabelFor(thanhPhan);
        pnlTruong.add(lblNhan, BorderLayout.NORTH);
        if (thanhPhan instanceof JTextField) {
            JTextField txtTruong = (JTextField) thanhPhan;
            txtTruong.setFont(UiTheme.SHELL_FONT.deriveFont(13.0f));
            txtTruong.setForeground(UiTheme.TEXT);
            txtTruong.setCaretColor(UiTheme.TEXT);
            txtTruong.setBackground(txtTruong.isEditable() ? UiTheme.SURFACE : UiTheme.WINDOW);
            txtTruong.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UiTheme.BORDER),
                    BorderFactory.createEmptyBorder(3, 7, 3, 7)));
            txtTruong.setPreferredSize(new Dimension(0, 30));
            txtTruong.setMinimumSize(new Dimension(0, 30));
        }
        pnlTruong.add(thanhPhan, BorderLayout.CENTER);
        return pnlTruong;
    }

    /**
     * Tạo thẻ nội dung.
     * @param tieuDe tiêu đề nhóm
     * @param noiDung thành phần bên trong
     * @return thẻ nền trắng
     */
    // Tạo thẻ viền xám với lề 8 px đúng các nhóm trong màn nghiệp vụ.
    public static JPanel taoThe(String tieuDe, JComponent noiDung) {
        JPanel pnlThe = new JPanel(new BorderLayout(0, 8)) {
            private static final long serialVersionUID = 1L;

            // Giữ thẻ theo chiều cao nội dung khi đặt trong BoxLayout.
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        pnlThe.setBackground(UiTheme.SURFACE);
        pnlThe.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MAU_LUOI),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        pnlThe.add(taoNhan(tieuDe, true), BorderLayout.NORTH);
        pnlThe.add(noiDung, BorderLayout.CENTER);
        return pnlThe;
    }

    /**
     * Tạo văn bản chỉ đọc tự xuống hàng.
     * @param noiDung nội dung văn bản
     * @return vùng văn bản trong suốt
     */
    // Tính chiều cao theo chiều rộng thực tế thay vì ấn định một hoặc hai dòng.
    public static JTextArea taoMoTa(String noiDung) {
        JTextArea txaMoTa = new JTextArea(noiDung) {
            private static final long serialVersionUID = 1L;

            // Đo chiều cao văn bản đã xuống hàng để không cắt mất dòng cuối.
            @Override
            public Dimension getPreferredSize() {
                Insets le = getInsets();
                int chieuRong = getWidth();
                if (chieuRong <= le.left + le.right) {
                    chieuRong = 680;
                }
                View goc = getUI().getRootView(this);
                goc.setSize(Math.max(1, chieuRong - le.left - le.right), Float.MAX_VALUE);
                int chieuCao = (int) Math.ceil(goc.getPreferredSpan(View.Y_AXIS)) + le.top + le.bottom;
                return new Dimension(0, Math.max(getFontMetrics(getFont()).getHeight(), chieuCao));
            }

            // Cho phép văn bản thu chiều ngang mà vẫn giữ đủ chiều cao.
            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
        };
        txaMoTa.setFont(UiTheme.SHELL_FONT.deriveFont(13.0f));
        txaMoTa.setForeground(UiTheme.TEXT);
        txaMoTa.setEditable(false);
        txaMoTa.setFocusable(false);
        txaMoTa.setLineWrap(true);
        txaMoTa.setWrapStyleWord(true);
        txaMoTa.setOpaque(false);
        txaMoTa.setBorder(BorderFactory.createEmptyBorder());
        txaMoTa.setMargin(new Insets(0, 0, 0, 0));
        return txaMoTa;
    }

    /**
     * Tạo bảng dữ liệu.
     * @param model dữ liệu bảng
     * @return bảng theo thiết kế
     */
    // Định dạng toàn bộ ô và header để không nhận nền hoặc viền mặc định Windows.
    public static JTable taoBang(DefaultTableModel model) {
        JTable tblBang = new JTable(model);
        tblBang.setFont(UiTheme.SHELL_FONT.deriveFont(13.0f));
        tblBang.setForeground(UiTheme.TEXT);
        tblBang.setBackground(UiTheme.SURFACE);
        tblBang.setSelectionBackground(MAU_CHON);
        tblBang.setSelectionForeground(UiTheme.TEXT);
        tblBang.setGridColor(MAU_LUOI);
        tblBang.setShowGrid(true);
        tblBang.setIntercellSpacing(new Dimension(1, 1));
        tblBang.setRowHeight(30);
        tblBang.setFillsViewportHeight(true);
        tblBang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblBang.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblBang.getTableHeader().setUI(new BasicTableHeaderUI());
        tblBang.getTableHeader().setReorderingAllowed(false);
        tblBang.getTableHeader().setResizingAllowed(true);
        tblBang.getTableHeader().setPreferredSize(new Dimension(0, 30));
        tblBang.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            // Vẽ header nền xám, chữ đậm, căn trái và lề 8 px.
            @Override
            public Component getTableCellRendererComponent(JTable bang, Object giaTri,
                    boolean duocChon, boolean coTieuDiem, int dong, int cot) {
                super.getTableCellRendererComponent(bang, giaTri, false, false, dong, cot);
                setFont(UiTheme.BRAND_FONT.deriveFont(13.0f));
                setForeground(UiTheme.TEXT);
                setBackground(UiTheme.WINDOW);
                setHorizontalAlignment(SwingConstants.LEFT);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, MAU_LUOI),
                        BorderFactory.createEmptyBorder(0, 8, 0, 8)));
                return this;
            }
        });
        tblBang.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            // Vẽ ô dữ liệu với màu chọn xanh nhạt và không có viền tiêu điểm mặc định.
            @Override
            public Component getTableCellRendererComponent(JTable bang, Object giaTri,
                    boolean duocChon, boolean coTieuDiem, int dong, int cot) {
                super.getTableCellRendererComponent(bang, giaTri, duocChon, false, dong, cot);
                setFont(UiTheme.SHELL_FONT.deriveFont(13.0f));
                setForeground(UiTheme.TEXT);
                setBackground(duocChon ? MAU_CHON : UiTheme.SURFACE);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                setToolTipText(giaTri == null ? null : giaTri.toString());
                return this;
            }
        });
        return tblBang;
    }

    /**
     * Tạo model chỉ đọc.
     * @param cot tên các cột
     * @return model trống
     */
    // Chỉ cho sửa dữ liệu qua biểu mẫu nghiệp vụ.
    public static DefaultTableModel taoModel(String... cot) {
        return new DefaultTableModel(cot, 0) {
            private static final long serialVersionUID = 1L;

            // Khóa việc chỉnh sửa trực tiếp trong ô bảng.
            @Override
            public boolean isCellEditable(int dong, int cotDuocChon) {
                return false;
            }
        };
    }

    /**
     * Định dạng số lượng.
     * @param soLuong giá trị cần hiển thị
     * @return chuỗi không có số không thập phân dư
     */
    // Định dạng BigDecimal mà không chuyển qua double.
    public static String dinhDangLuong(BigDecimal soLuong) {
        return soLuong.stripTrailingZeros().toPlainString();
    }

    /**
     * Đọc số lượng từ ô nhập.
     * @param txtSoLuong ô cần đọc
     * @return giá trị số lượng
     * @throws IllegalArgumentException nếu không phải số
     */
    // Đọc số thập phân và hỗ trợ dấu phẩy theo cách nhập tiếng Việt.
    public static BigDecimal docSoLuong(JTextField txtSoLuong) {
        try {
            return new BigDecimal(txtSoLuong.getText().trim().replace(',', '.'));
        } catch (NumberFormatException loi) {
            throw new IllegalArgumentException("Số lượng phải là số hợp lệ.", loi);
        }
    }

    /**
     * Định dạng checkbox.
     * @param chkLuaChon checkbox cần định dạng
     */
    // Thay ô tích Windows bằng ô phẳng có nền xanh và dấu tích trắng.
    public static void dinhDangKiemTra(JCheckBox chkLuaChon) {
        chkLuaChon.setUI(new BasicCheckBoxUI());
        chkLuaChon.setFont(UiTheme.SHELL_FONT.deriveFont(13.0f));
        chkLuaChon.setForeground(UiTheme.TEXT);
        chkLuaChon.setOpaque(false);
        chkLuaChon.setFocusPainted(false);
        chkLuaChon.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        chkLuaChon.setIconTextGap(4);
        Icon bieuTuong = new BieuTuongKiemTra();
        chkLuaChon.setIcon(bieuTuong);
        chkLuaChon.setSelectedIcon(bieuTuong);
        chkLuaChon.setRolloverIcon(bieuTuong);
        chkLuaChon.setRolloverSelectedIcon(bieuTuong);
        chkLuaChon.setPreferredSize(new Dimension(0, 24));
        chkLuaChon.setMinimumSize(new Dimension(0, 24));
    }

    /**
     * Định dạng vùng cuộn.
     * @param scrNoiDung vùng cuộn cần định dạng
     */
    // Áp dụng thanh cuộn rộng 14 px, nền xám và thanh kéo phẳng.
    public static void dinhDangVungCuon(JScrollPane scrNoiDung) {
        scrNoiDung.setBorder(BorderFactory.createLineBorder(
                scrNoiDung.getViewport().getView() instanceof JTextArea ? UiTheme.BORDER : MAU_LUOI));
        scrNoiDung.setViewportBorder(BorderFactory.createEmptyBorder());
        scrNoiDung.getViewport().setBackground(UiTheme.SURFACE);
        scrNoiDung.getVerticalScrollBar().setUnitIncrement(16);
        JScrollBar thanhCuon = scrNoiDung.getVerticalScrollBar();
        thanhCuon.setPreferredSize(new Dimension(14, 0));
        thanhCuon.setUI(new BasicScrollBarUI() {
            // Gán màu cho thanh cuộn mà không lấy màu từ Look and Feel.
            @Override
            protected void configureScrollBarColors() {
                trackColor = UiTheme.WINDOW;
                thumbColor = UiTheme.BORDER;
            }

            // Tạo nút cuộn lên nhỏ ở đầu thanh cuộn.
            @Override
            protected JButton createDecreaseButton(int huong) {
                return taoNutCuon(true);
            }

            // Tạo nút cuộn xuống nhỏ ở cuối thanh cuộn.
            @Override
            protected JButton createIncreaseButton(int huong) {
                return taoNutCuon(false);
            }

            // Vẽ rãnh cuộn nền xám phẳng.
            @Override
            protected void paintTrack(Graphics doHoa, JComponent thanhPhan, java.awt.Rectangle vung) {
                doHoa.setColor(UiTheme.WINDOW);
                doHoa.fillRect(vung.x, vung.y, vung.width, vung.height);
            }

            // Vẽ thanh kéo nằm giữa rãnh với khoảng trống hai bên.
            @Override
            protected void paintThumb(Graphics doHoa, JComponent thanhPhan, java.awt.Rectangle vung) {
                if (vung.isEmpty() || !thanhPhan.isEnabled()) {
                    return;
                }
                doHoa.setColor(UiTheme.BORDER);
                doHoa.fillRoundRect(vung.x + 3, vung.y, Math.max(1, vung.width - 6), vung.height, 2, 2);
            }
        });
    }

    // Tạo nút mũi tên cuộn bằng hình tam giác nhỏ, không dùng nút nổi mặc định.
    private static JButton taoNutCuon(boolean huongLen) {
        JButton btnCuon = new JButton() {
            private static final long serialVersionUID = 1L;

            // Vẽ mũi tên theo chiều cuộn trên nền phẳng.
            @Override
            protected void paintComponent(Graphics doHoa) {
                doHoa.setColor(UiTheme.WINDOW);
                doHoa.fillRect(0, 0, getWidth(), getHeight());
                int tamX = getWidth() / 2;
                int tamY = getHeight() / 2;
                int[] cacX = {tamX - 3, tamX + 3, tamX};
                int[] cacY = huongLen ? new int[] {tamY + 2, tamY + 2, tamY - 2}
                        : new int[] {tamY - 2, tamY - 2, tamY + 2};
                doHoa.setColor(UiTheme.MUTED);
                doHoa.fillPolygon(cacX, cacY, 3);
            }
        };
        btnCuon.setUI(new BasicButtonUI());
        btnCuon.setBorder(BorderFactory.createEmptyBorder());
        btnCuon.setFocusPainted(false);
        btnCuon.setPreferredSize(new Dimension(14, 14));
        btnCuon.setMinimumSize(new Dimension(14, 14));
        btnCuon.setMaximumSize(new Dimension(14, 14));
        return btnCuon;
    }

    /**
     * Định dạng các điều khiển trong một màn.
     * @param thanhPhan gốc của cây giao diện
     */
    // Áp dụng checkbox và thanh cuộn đã tùy chỉnh cho toàn bộ màn đang mở.
    public static void dinhDangCay(Component thanhPhan) {
        if (thanhPhan instanceof JCheckBox) {
            dinhDangKiemTra((JCheckBox) thanhPhan);
        }
        if (thanhPhan instanceof JScrollPane) {
            dinhDangVungCuon((JScrollPane) thanhPhan);
        }
        if (thanhPhan instanceof Container) {
            for (Component con : ((Container) thanhPhan).getComponents()) {
                dinhDangCay(con);
            }
        }
    }

    /**
     * Tạo tiêu đề hộp thoại.
     * @param winHopThoai hộp thoại chủ
     * @return thanh tiêu đề
     */
    // Tạo thanh tiêu đề trắng và nút đóng không có khung nổi.
    public static JPanel taoTieuDe(JDialog winHopThoai) {
        winHopThoai.getContentPane().setBackground(UiTheme.WINDOW);
        JPanel pnlTieuDe = new JPanel(new BorderLayout());
        pnlTieuDe.setBackground(UiTheme.SURFACE);
        pnlTieuDe.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        pnlTieuDe.setPreferredSize(new Dimension(0, 32));
        JLabel lblTieuDe = taoNhan(winHopThoai.getTitle(), true);
        lblTieuDe.setFont(UiTheme.BRAND_FONT.deriveFont(13.0f));
        pnlTieuDe.add(lblTieuDe, BorderLayout.CENTER);
        JButton btnDong = taoNut("×", false, false);
        btnDong.setFont(new Font(Font.DIALOG, Font.PLAIN, 13));
        btnDong.setForeground(UiTheme.MUTED);
        btnDong.setBorder(BorderFactory.createEmptyBorder());
        btnDong.setPreferredSize(new Dimension(24, 24));
        btnDong.setMinimumSize(new Dimension(24, 24));
        btnDong.addActionListener(new ActionListener() {
            // Gửi sự kiện đóng để biểu mẫu kiểm tra dữ liệu chưa lưu.
            @Override
            public void actionPerformed(ActionEvent suKien) {
                winHopThoai.dispatchEvent(new WindowEvent(winHopThoai, WindowEvent.WINDOW_CLOSING));
            }
        });
        pnlTieuDe.add(btnDong, BorderLayout.EAST);
        return pnlTieuDe;
    }

    /**
     * Mở thông báo hoặc xác nhận.
     * @param winChu cửa sổ chủ
     * @param tieuDe tiêu đề
     * @param thongBao nội dung
     * @param nhanDongY nhãn nút xác nhận
     * @param nguyHiem true cho nút nguy hiểm
     * @param canHuy true để có nút hủy
     * @return true khi người dùng xác nhận
     */
    // Mở hộp thoại vừa đủ nội dung, không dùng JOptionPane hoặc vùng cuộn ngoài.
    public static boolean hienHopThoai(Window winChu, String tieuDe, String thongBao,
            String nhanDongY, boolean nguyHiem, boolean canHuy) {
        JDialog winHopThoai = new JDialog(winChu, tieuDe, JDialog.ModalityType.APPLICATION_MODAL);
        boolean[] daDongY = {false};
        winHopThoai.setUndecorated(true);
        winHopThoai.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        winHopThoai.getRootPane().setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));
        winHopThoai.add(taoTieuDe(winHopThoai), BorderLayout.NORTH);
        JPanel pnlNoiDung = new JPanel();
        pnlNoiDung.setLayout(new BoxLayout(pnlNoiDung, BoxLayout.Y_AXIS));
        pnlNoiDung.setBackground(UiTheme.WINDOW);
        pnlNoiDung.setBorder(BorderFactory.createEmptyBorder(16, 16, 0, 16));
        String[] cacDoan = thongBao.split("\\n\\s*\\n", 2);
        int chieuCaoNoiDung = 16;
        for (int viTri = 0; viTri < cacDoan.length; viTri++) {
            JTextArea txaDoan = taoMoTa(cacDoan[viTri]);
            txaDoan.setSize(526, 1);
            txaDoan.setAlignmentX(Component.LEFT_ALIGNMENT);
            if (nguyHiem && cacDoan.length > 1 && viTri == 0) {
                txaDoan.setFont(UiTheme.BRAND_FONT.deriveFont(14.0f));
                txaDoan.setForeground(MAU_NGUY_HIEM);
            }
            int chieuCao = txaDoan.getPreferredSize().height;
            txaDoan.setMaximumSize(new Dimension(Integer.MAX_VALUE, chieuCao));
            pnlNoiDung.add(txaDoan);
            chieuCaoNoiDung += chieuCao;
            if (viTri < cacDoan.length - 1) {
                pnlNoiDung.add(Box.createVerticalStrut(12));
                chieuCaoNoiDung += 12;
            }
        }
        winHopThoai.add(pnlNoiDung, BorderLayout.CENTER);
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.setBackground(UiTheme.WINDOW);
        pnlNut.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        if (canHuy) {
            JButton btnHuy = taoNut("Bỏ thay đổi".equals(nhanDongY) ? "Tiếp tục nhập" : "Hủy", false, false);
            btnHuy.addActionListener(new ActionListener() {
                // Đóng thông báo và giữ nguyên nội dung của biểu mẫu chủ.
                @Override
                public void actionPerformed(ActionEvent suKien) {
                    winHopThoai.dispose();
                }
            });
            pnlNut.add(btnHuy);
        }
        JButton btnDongY = taoNut(nhanDongY, !nguyHiem, nguyHiem);
        btnDongY.addActionListener(new ActionListener() {
            // Ghi nhận lựa chọn đồng ý rồi đóng hộp thoại.
            @Override
            public void actionPerformed(ActionEvent suKien) {
                daDongY[0] = true;
                winHopThoai.dispose();
            }
        });
        pnlNut.add(btnDongY);
        winHopThoai.add(pnlNut, BorderLayout.SOUTH);
        int chieuCao = Math.max(172, 32 + chieuCaoNoiDung + pnlNut.getPreferredSize().height + 2);
        winHopThoai.setSize(560, chieuCao);
        winHopThoai.setLocationRelativeTo(winChu);
        winHopThoai.setVisible(true);
        return daDongY[0];
    }

    /**
     * Biểu tượng ô kiểm tra phẳng theo màu của thiết kế.
     */
    private static final class BieuTuongKiemTra implements Icon {
        // Trả chiều rộng ô kiểm tra theo thiết kế.
        @Override
        public int getIconWidth() {
            return 12;
        }

        // Trả chiều cao ô kiểm tra theo thiết kế.
        @Override
        public int getIconHeight() {
            return 12;
        }

        // Vẽ trạng thái chọn bằng nền xanh và dấu tích trắng.
        @Override
        public void paintIcon(Component thanhPhan, Graphics doHoa, int x, int y) {
            Graphics2D ve = (Graphics2D) doHoa.create();
            ve.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean duocChon = thanhPhan instanceof AbstractButton
                    && ((AbstractButton) thanhPhan).isSelected();
            ve.setColor(duocChon ? UiTheme.PRIMARY : UiTheme.SURFACE);
            ve.fillRect(x, y, 12, 12);
            ve.setColor(duocChon ? UiTheme.PRIMARY : UiTheme.BORDER);
            ve.drawRect(x, y, 11, 11);
            if (duocChon) {
                ve.setColor(UiTheme.SURFACE);
                ve.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                ve.drawLine(x + 3, y + 6, x + 5, y + 8);
                ve.drawLine(x + 5, y + 8, x + 9, y + 3);
            }
            ve.dispose();
        }
    }
}