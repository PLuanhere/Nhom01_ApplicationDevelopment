package ui.component;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicTableHeaderUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

// Cung cấp màu, font và thành phần trình bày của giao diện kỹ thuật.
public final class GiaoDienKyThuat {
    public static final Color NEN = new Color(0xF4F6F9);
    public static final Color SIDEBAR = new Color(0x172634);
    public static final Color DANG_CHON = new Color(0x31506A);
    public static final Color CHU = new Color(0x202D3D);
    public static final Color CHU_PHU = new Color(0x647489);
    public static final Color VIEN = new Color(0xDCE3EC);
    public static final Color XANH = new Color(0x2264A6);
    public static final Color NHAT = new Color(0xF6F9FC);
    public static final Color VANG_NHAT = new Color(0xFFF7E5);
    public static final Font CHU_THUONG = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font CHU_DAM = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font CHU_NHO = GiaoDienKyThuat.CHU_THUONG.deriveFont(11.0f);
    public static final Font TIEU_DE = GiaoDienKyThuat.CHU_DAM.deriveFont(24.0f);

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    private GiaoDienKyThuat() {
    }

    // Tạo nhãn theo font và màu chữ của giao diện kỹ thuật.
    /** Tạo nhan cho giao diện. */
    public static JLabel taoNhan(String noiDung, boolean dam) {
        JLabel lblNhan = new JLabel(noiDung);
        lblNhan.setFont(dam ? GiaoDienKyThuat.CHU_DAM : GiaoDienKyThuat.CHU_THUONG);
        lblNhan.setForeground(GiaoDienKyThuat.CHU);
        return lblNhan;
    }

    // Tạo nút có nền, viền và màu chữ theo thiết kế.
    /** Tạo nut cho giao diện. */
    public static JButton taoNut(String noiDung, String lenh, boolean chinh, ActionListener xuLy) {
        JButton btnNut = new JButton(noiDung) {
            private static final long serialVersionUID = 1L;

            // Vẽ nền bo góc và viền của nút theo trạng thái hiện tại.
            /** Vẽ nội dung của thành phần theo trạng thái hiện tại. */
            @Override
            protected void paintComponent(Graphics doHoa) {
                Graphics2D ve = (Graphics2D) doHoa.create();
                ve.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                ve.setColor(isEnabled() ? getBackground() : new Color(0xEBF0F5));
                ve.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                if (!chinh && !Boolean.TRUE.equals(getClientProperty("khongVien"))) {
                    ve.setColor(GiaoDienKyThuat.VIEN);
                    ve.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                }
                ve.dispose();
                super.paintComponent(doHoa);
            }
        };
        btnNut.setUI(new BasicButtonUI() {
            // Vẽ chữ một lần khi nút bị khóa để tránh hiệu ứng chữ nổi chồng nét.
            @Override
            protected void paintText(Graphics doHoa, JComponent thanhPhan, Rectangle vungChu, String chu) {
                if (thanhPhan.isEnabled()) {
                    super.paintText(doHoa, thanhPhan, vungChu, chu);
                    return;
                }
                doHoa.setColor(GiaoDienKyThuat.CHU_PHU);
                BasicGraphicsUtils.drawStringUnderlineCharAt(doHoa, chu, -1, vungChu.x,
                        vungChu.y + doHoa.getFontMetrics().getAscent());
            }
        });
        btnNut.setFont(chinh ? GiaoDienKyThuat.CHU_DAM.deriveFont(13.0f) : GiaoDienKyThuat.CHU_THUONG);
        btnNut.setForeground(chinh ? Color.WHITE : GiaoDienKyThuat.CHU);
        btnNut.setBackground(chinh ? GiaoDienKyThuat.XANH : Color.WHITE);
        btnNut.setContentAreaFilled(false);
        btnNut.setOpaque(false);
        btnNut.setFocusPainted(false);
        btnNut.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        btnNut.setActionCommand(lenh);
        if (xuLy != null) {
            btnNut.addActionListener(xuLy);
        }
        btnNut.setPreferredSize(new Dimension(btnNut.getPreferredSize().width, 36));
        return btnNut;
    }

    // Tạo thẻ trắng bo góc với chiều cao vừa nội dung.
    /** Tạo mat phang cho giao diện. */
    public static JPanel taoMatPhang() {
        JPanel pnlMat = new JPanel(new BorderLayout(0, 12)) {
            private static final long serialVersionUID = 1L;

            // Vẽ nền trắng và đường viền của thẻ.
            /** Vẽ nội dung của thành phần theo trạng thái hiện tại. */
            @Override
            protected void paintComponent(Graphics doHoa) {
                Graphics2D ve = (Graphics2D) doHoa.create();
                ve.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                ve.setColor(getBackground());
                ve.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                ve.setColor(GiaoDienKyThuat.VIEN);
                ve.setStroke(new BasicStroke(1));
                ve.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                ve.dispose();
            }

            // Giữ chiều cao của thẻ theo nội dung khi xếp trong BoxLayout.
            /** Trả về maximum size. */
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        pnlMat.setOpaque(false);
        pnlMat.setBackground(Color.WHITE);
        pnlMat.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        return pnlMat;
    }

    // Tạo thẻ gồm tiêu đề, mô tả và vùng nội dung.
    /** Tạo the cho giao diện. */
    public static JPanel taoThe(String tieuDe, String moTa, JComponent noiDung) {
        JPanel pnlThe = GiaoDienKyThuat.taoMatPhang();
        JPanel pnlDau = GiaoDienKyThuat.taoCot();
        pnlDau.add(GiaoDienKyThuat.taoNhan(tieuDe, true));
        if (moTa != null && !moTa.isBlank()) {
            pnlDau.add(Box.createVerticalStrut(5));
            JLabel lblMoTa = GiaoDienKyThuat.taoNhan(moTa, false);
            lblMoTa.setFont(GiaoDienKyThuat.CHU_NHO);
            lblMoTa.setForeground(GiaoDienKyThuat.CHU_PHU);
            pnlDau.add(lblMoTa);
        }
        pnlThe.add(pnlDau, BorderLayout.NORTH);
        pnlThe.add(noiDung, BorderLayout.CENTER);
        return pnlThe;
    }

    // Tạo cột dọc và căn trái thống nhất các thành phần bên trong.
    /** Tạo cot cho giao diện. */
    public static JPanel taoCot() {
        JPanel pnlCot = new JPanel() {
            private static final long serialVersionUID = 1L;

            // Căn trái thành phần trước khi thêm để tránh lệch cột trong BoxLayout.
            /** Thực hiện add impl cho thành phần hiện tại. */
            @Override
            protected void addImpl(Component thanhPhan, Object rangBuoc, int viTri) {
                if (thanhPhan instanceof JComponent) {
                    ((JComponent) thanhPhan).setAlignmentX(Component.LEFT_ALIGNMENT);
                }
                super.addImpl(thanhPhan, rangBuoc, viTri);
            }
        };
        pnlCot.setLayout(new BoxLayout(pnlCot, BoxLayout.Y_AXIS));
        pnlCot.setOpaque(false);
        pnlCot.setAlignmentX(Component.LEFT_ALIGNMENT);
        return pnlCot;
    }

    // Thêm thành phần vào cột với khoảng cách giữa các nhóm nội dung.
    /** Thực hiện them thanh phan cho thành phần hiện tại. */
    public static void themThanhPhan(JPanel pnlCot, JComponent thanhPhan) {
        thanhPhan.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlCot.add(thanhPhan);
        pnlCot.add(Box.createVerticalStrut(14));
    }

    // Tạo ô nhập hoặc ô chỉ đọc cao 42 px.
    /** Tạo onhap cho giao diện. */
    public static JTextField taoONhap(String noiDung, boolean chiDoc) {
        JTextField txtNhap = new JTextField(noiDung) {
            private static final long serialVersionUID = 1L;

            // Hiển thị gợi ý khi ô trống mà không đưa gợi ý vào dữ liệu.
            /** Vẽ nội dung của thành phần theo trạng thái hiện tại. */
            @Override
            protected void paintComponent(Graphics doHoa) {
                super.paintComponent(doHoa);
                Object goiY = getClientProperty("goiY");
                if (getText().isEmpty() && goiY != null) {
                    doHoa.setColor(GiaoDienKyThuat.CHU_PHU);
                    doHoa.setFont(getFont());
                    int y = (getHeight() - doHoa.getFontMetrics().getHeight()) / 2 + doHoa.getFontMetrics().getAscent();
                    doHoa.drawString(goiY.toString(), getInsets().left + 2, y);
                }
            }
        };
        txtNhap.setEditable(!chiDoc);
        txtNhap.setFont(GiaoDienKyThuat.CHU_THUONG);
        txtNhap.setForeground(chiDoc ? GiaoDienKyThuat.CHU_PHU : GiaoDienKyThuat.CHU);
        txtNhap.setBackground(chiDoc ? GiaoDienKyThuat.NHAT : Color.WHITE);
        txtNhap.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(GiaoDienKyThuat.VIEN),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        txtNhap.setPreferredSize(new Dimension(0, 42));
        txtNhap.setMinimumSize(new Dimension(0, 42));
        return txtNhap;
    }

    // Định dạng combo và danh sách lựa chọn theo màu, font của thiết kế.
    /** Thực hiện dinh dang combo cho thành phần hiện tại. */
    public static void dinhDangCombo(JComboBox<?> cmbChon) {
        cmbChon.setUI(new BasicComboBoxUI() {
            // Vẽ nền trắng cho giá trị hiện tại của combo.
            /** Vẽ nội dung của thành phần theo trạng thái hiện tại. */
            @Override
            public void paintCurrentValueBackground(Graphics doHoa, Rectangle vung, boolean tieuDiem) {
                doHoa.setColor(Color.WHITE);
                doHoa.fillRect(vung.x, vung.y, vung.width, vung.height);
            }

            // Tạo nút mũi tên cùng nền với ô chọn.
            /** Tạo arrow button cho giao diện. */
            @Override
            protected JButton createArrowButton() {
                JButton btnMo = GiaoDienKyThuat.taoNut("▾", "", false, null);
                btnMo.setFont(new Font(Font.DIALOG, Font.PLAIN, 11));
                btnMo.putClientProperty("khongVien", true);
                btnMo.setBorder(BorderFactory.createEmptyBorder());
                btnMo.setPreferredSize(new Dimension(24, 34));
                return btnMo;
            }
        });
        cmbChon.setFont(GiaoDienKyThuat.CHU_THUONG);
        cmbChon.setBorder(BorderFactory.createLineBorder(GiaoDienKyThuat.VIEN));
        cmbChon.setPreferredSize(new Dimension(0, 42));
        cmbChon.setMinimumSize(new Dimension(0, 42));
        cmbChon.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            // Vẽ từng lựa chọn với font và màu nền thống nhất.
            /** Trả về list cell renderer component. */
            @Override
            public Component getListCellRendererComponent(JList<?> danhSach, Object giaTri, int viTri, boolean duocChon,
                    boolean tieuDiem) {
                super.getListCellRendererComponent(danhSach, giaTri, viTri, duocChon, tieuDiem);
                setFont(GiaoDienKyThuat.CHU_THUONG);
                setForeground(GiaoDienKyThuat.CHU);
                setBackground(duocChon && viTri >= 0 ? new Color(0xEEF4FA) : Color.WHITE);
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return this;
            }
        });
    }

    // Đặt nhãn mô tả phía trên giá trị hoặc ô nhập.
    /** Tạo truong cho giao diện. */
    public static JPanel taoTruong(String ten, JComponent giaTri) {
        JPanel pnlTruong = new JPanel(new BorderLayout(0, 6));
        pnlTruong.setOpaque(false);
        JLabel lblTen = GiaoDienKyThuat.taoNhan(ten, false);
        lblTen.setFont(GiaoDienKyThuat.CHU_THUONG.deriveFont(12.0f));
        lblTen.setForeground(GiaoDienKyThuat.CHU_PHU);
        lblTen.setLabelFor(giaTri);
        pnlTruong.add(lblTen, BorderLayout.NORTH);
        pnlTruong.add(giaTri, BorderLayout.CENTER);
        return pnlTruong;
    }

    // Tạo thông báo tự xuống dòng với nền thông tin hoặc cảnh báo.
    /** Tạo thong bao cho giao diện. */
    public static JPanel taoThongBao(String noiDung, boolean canhBao) {
        JPanel pnlTin = new JPanel(new BorderLayout());
        pnlTin.setBackground(canhBao ? GiaoDienKyThuat.VANG_NHAT : GiaoDienKyThuat.NHAT);
        pnlTin.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JTextArea txaTin = PhuTungUi.taoMoTa(noiDung);
        txaTin.setFont(GiaoDienKyThuat.CHU_NHO);
        txaTin.setForeground(GiaoDienKyThuat.CHU_PHU);
        pnlTin.add(txaTin, BorderLayout.CENTER);
        return pnlTin;
    }

    // Tạo bảng với tiêu đề, chiều cao dòng và đường phân cách theo thiết kế.
    /** Tạo bang cho giao diện. */
    public static JTable taoBang(DefaultTableModel model) {
        JTable tblBang = new JTable(model);
        tblBang.getTableHeader().setReorderingAllowed(false);
        tblBang.getTableHeader().setResizingAllowed(false);
        tblBang.setFont(GiaoDienKyThuat.CHU_THUONG);
        tblBang.setForeground(GiaoDienKyThuat.CHU);
        tblBang.setBackground(Color.WHITE);
        tblBang.setSelectionBackground(new Color(0xEEF4FA));
        tblBang.setSelectionForeground(GiaoDienKyThuat.CHU);
        tblBang.setRowHeight(52);
        tblBang.setShowVerticalLines(false);
        tblBang.setShowHorizontalLines(true);
        tblBang.setGridColor(new Color(0xEDF1F5));
        tblBang.setIntercellSpacing(new Dimension(0, 1));
        tblBang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblBang.getTableHeader().setUI(new BasicTableHeaderUI());
        tblBang.getTableHeader().setReorderingAllowed(false);
        tblBang.getTableHeader().setResizingAllowed(false);
        tblBang.getTableHeader().setPreferredSize(new Dimension(0, 39));
        DefaultTableCellRenderer boVe = new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            // Vẽ nội dung ô, liên kết thao tác và nhãn trạng thái của bảng.
            /** Trả về table cell renderer component. */
            @Override
            public Component getTableCellRendererComponent(JTable bang, Object giaTri, boolean chon, boolean tieuDiem,
                    int dong, int cot) {
                super.getTableCellRendererComponent(bang, giaTri, chon, false, dong, cot);
                setFont(dong < 0 ? GiaoDienKyThuat.CHU_DAM.deriveFont(12.0f) : GiaoDienKyThuat.CHU_THUONG);
                setBackground(dong < 0 ? GiaoDienKyThuat.NHAT : chon ? new Color(0xEEF4FA) : Color.WHITE);
                setForeground(
                        "Xem chi tiết".equals(String.valueOf(giaTri)) ? GiaoDienKyThuat.XANH : GiaoDienKyThuat.CHU);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                setToolTipText(giaTri == null ? null : giaTri.toString());
                if (dong >= 0) {
                    String noiDung = String.valueOf(giaTri);
                    if (noiDung.equals("Đang sửa") || noiDung.equals("Chờ sửa chữa") || noiDung.equals("Chờ phụ tùng")
                            || noiDung.equals("Hoàn thành") || noiDung.equals("Đã hoàn tất")
                            || noiDung.equals("Chưa xác nhận")) {
                        return GiaoDienKyThuat.trangThai(noiDung, chon);
                    }
                    if (cot == 0) {
                        setFont(GiaoDienKyThuat.CHU_DAM.deriveFont(12.0f));
                    }
                }
                return this;
            }
        };
        JTextField txtSuaO = new JTextField();
        txtSuaO.setFont(GiaoDienKyThuat.CHU_THUONG);
        txtSuaO.setForeground(GiaoDienKyThuat.CHU);
        txtSuaO.setCaretColor(GiaoDienKyThuat.CHU);
        txtSuaO.setBackground(Color.WHITE);
        txtSuaO.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GiaoDienKyThuat.XANH),
                BorderFactory.createEmptyBorder(0, 11, 0, 11)));
        tblBang.setDefaultEditor(Object.class, new DefaultCellEditor(txtSuaO));
        tblBang.setDefaultRenderer(Object.class, boVe);
        tblBang.getTableHeader().setDefaultRenderer(boVe);
        return tblBang;
    }

    // Tạo nhãn màu thể hiện trạng thái công việc trong bảng.
    /** Thực hiện trang thai cho thành phần hiện tại. */
    private static JPanel trangThai(String noiDung, boolean duocChon) {
        Color nen = noiDung.contains("Hoàn") || noiDung.contains("hoàn") ? new Color(0xE8F7EF)
                : noiDung.equals("Chờ phụ tùng") ? new Color(0xFFF3DD) : new Color(0xEAF3FF);
        Color chu = noiDung.contains("Hoàn") || noiDung.contains("hoàn") ? new Color(0x09734A)
                : noiDung.equals("Chờ phụ tùng") ? new Color(0x9B6307) : GiaoDienKyThuat.XANH;
        JLabel lblTrangThai = new JLabel(noiDung) {
            private static final long serialVersionUID = 1L;

            // Vẽ nền bo góc của nhãn trạng thái.
            /** Vẽ nội dung của thành phần theo trạng thái hiện tại. */
            @Override
            protected void paintComponent(Graphics doHoa) {
                Graphics2D ve = (Graphics2D) doHoa.create();
                ve.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                ve.setColor(nen);
                ve.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                ve.dispose();
                super.paintComponent(doHoa);
            }
        };
        lblTrangThai.setFont(GiaoDienKyThuat.CHU_DAM.deriveFont(11.0f));
        lblTrangThai.setForeground(chu);
        lblTrangThai.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        JPanel pnlTrangThai = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 13));
        pnlTrangThai.setBackground(duocChon ? new Color(0xEEF4FA) : Color.WHITE);
        pnlTrangThai.add(lblTrangThai);
        return pnlTrangThai;
    }

    // Bọc bảng và cập nhật chiều cao khi danh sách được nạp hoặc lọc lại.
    public static JScrollPane bocBang(JTable tblBang) {
        JScrollPane scrBang = new JScrollPane(tblBang);
        scrBang.setColumnHeaderView(tblBang.getTableHeader());
        PhuTungUi.dinhDangVungCuon(scrBang);
        scrBang.setBorder(BorderFactory.createEmptyBorder());
        capNhatChieuCaoBang(tblBang, scrBang);
        tblBang.getModel().addTableModelListener(suKien -> capNhatChieuCaoBang(tblBang, scrBang));
        return scrBang;
    }

    // Hiển thị tối đa tám dòng trước khi cuộn, tránh cắt dòng khi bảng có ít dữ liệu.
    private static void capNhatChieuCaoBang(JTable tblBang, JScrollPane scrBang) {
        int soDong = Math.max(1, Math.min(8, tblBang.getRowCount()));
        int chieuCao = tblBang.getTableHeader().getPreferredSize().height
                + soDong * tblBang.getRowHeight() + 2;
        scrBang.setPreferredSize(new Dimension(0, chieuCao));
        scrBang.setMinimumSize(new Dimension(0, chieuCao));
        scrBang.setMaximumSize(new Dimension(Integer.MAX_VALUE, chieuCao));
        scrBang.revalidate();
        scrBang.repaint();
    }

    // Tạo model chỉ đọc cho các danh sách dữ liệu.
    /** Tạo model cho giao diện. */
    public static DefaultTableModel taoModel(String... cot) {
        return new DefaultTableModel(cot, 0) {
            private static final long serialVersionUID = 1L;

            // Chặn chỉnh sửa trực tiếp các ô của danh sách.
            /** Cho biết cell editable. */
            @Override
            public boolean isCellEditable(int dong, int cotChon) {
                return false;
            }
        };
    }

    // Tạo hàng chứa nút hành động chính căn phải ở cuối thẻ.
    /** Tạo hang nut cho giao diện. */
    public static JPanel taoHangNut(JButton btnNut) {
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlNut.setOpaque(false);
        btnNut.setPreferredSize(new Dimension(Math.max(430, btnNut.getPreferredSize().width), 42));
        pnlNut.add(btnNut);
        return pnlNut;
    }
}