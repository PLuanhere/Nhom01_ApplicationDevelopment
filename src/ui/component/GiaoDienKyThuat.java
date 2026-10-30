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
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.plaf.basic.BasicButtonUI;
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
    public static final Font CHU_NHO = CHU_THUONG.deriveFont(11.0f);
    public static final Font TIEU_DE = CHU_DAM.deriveFont(24.0f);

    private GiaoDienKyThuat() {
    }

    // Tạo nhãn theo font và màu chữ của giao diện kỹ thuật.
    public static JLabel taoNhan(String noiDung, boolean dam) {
        JLabel lblNhan = new JLabel(noiDung);
        lblNhan.setFont(dam ? CHU_DAM : CHU_THUONG);
        lblNhan.setForeground(CHU);
        return lblNhan;
    }

    // Tạo nút có nền, viền và màu chữ theo thiết kế.
    public static JButton taoNut(String noiDung, String lenh, boolean chinh, ActionListener xuLy) {
        JButton btnNut = new JButton(noiDung) {
            private static final long serialVersionUID = 1L;

            // Vẽ nền bo góc và viền của nút theo trạng thái hiện tại.
            @Override
            protected void paintComponent(Graphics doHoa) {
                Graphics2D ve = (Graphics2D) doHoa.create();
                ve.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                ve.setColor(isEnabled() ? getBackground() : new Color(0xEBF0F5));
                ve.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                if (!chinh && !Boolean.TRUE.equals(getClientProperty("khongVien"))) {
                    ve.setColor(VIEN);
                    ve.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                }
                ve.dispose();
                super.paintComponent(doHoa);
            }
        };
        btnNut.setUI(new BasicButtonUI());
        btnNut.setFont(chinh ? CHU_DAM.deriveFont(13.0f) : CHU_THUONG);
        btnNut.setForeground(chinh ? Color.WHITE : CHU);
        btnNut.setBackground(chinh ? XANH : Color.WHITE);
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
    public static JPanel taoMatPhang() {
        JPanel pnlMat = new JPanel(new BorderLayout(0, 12)) {
            private static final long serialVersionUID = 1L;

            // Vẽ nền trắng và đường viền của thẻ.
            @Override
            protected void paintComponent(Graphics doHoa) {
                Graphics2D ve = (Graphics2D) doHoa.create();
                ve.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                ve.setColor(getBackground());
                ve.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                ve.setColor(VIEN);
                ve.setStroke(new BasicStroke(1));
                ve.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                ve.dispose();
            }

            // Giữ chiều cao của thẻ theo nội dung khi xếp trong BoxLayout.
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
    public static JPanel taoThe(String tieuDe, String moTa, JComponent noiDung) {
        JPanel pnlThe = taoMatPhang();
        JPanel pnlDau = taoCot();
        pnlDau.add(taoNhan(tieuDe, true));
        if (moTa != null && !moTa.isBlank()) {
            pnlDau.add(Box.createVerticalStrut(5));
            JLabel lblMoTa = taoNhan(moTa, false);
            lblMoTa.setFont(CHU_NHO);
            lblMoTa.setForeground(CHU_PHU);
            pnlDau.add(lblMoTa);
        }
        pnlThe.add(pnlDau, BorderLayout.NORTH);
        pnlThe.add(noiDung, BorderLayout.CENTER);
        return pnlThe;
    }

    // Tạo cột dọc và căn trái thống nhất các thành phần bên trong.
    public static JPanel taoCot() {
        JPanel pnlCot = new JPanel() {
            private static final long serialVersionUID = 1L;

            // Căn trái thành phần trước khi thêm để tránh lệch cột trong BoxLayout.
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
    public static void themThanhPhan(JPanel pnlCot, JComponent thanhPhan) {
        thanhPhan.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlCot.add(thanhPhan);
        pnlCot.add(Box.createVerticalStrut(14));
    }

    // Tạo ô nhập hoặc ô chỉ đọc cao 42 px.
    public static JTextField taoONhap(String noiDung, boolean chiDoc) {
        JTextField txtNhap = new JTextField(noiDung) {
            private static final long serialVersionUID = 1L;

            // Hiển thị gợi ý khi ô trống mà không đưa gợi ý vào dữ liệu.
            @Override
            protected void paintComponent(Graphics doHoa) {
                super.paintComponent(doHoa);
                Object goiY = getClientProperty("goiY");
                if (getText().isEmpty() && goiY != null) {
                    doHoa.setColor(CHU_PHU);
                    doHoa.setFont(getFont());
                    int y = (getHeight() - doHoa.getFontMetrics().getHeight()) / 2
                            + doHoa.getFontMetrics().getAscent();
                    doHoa.drawString(goiY.toString(), getInsets().left + 2, y);
                }
            }
        };
        txtNhap.setEditable(!chiDoc);
        txtNhap.setFont(CHU_THUONG);
        txtNhap.setForeground(chiDoc ? CHU_PHU : CHU);
        txtNhap.setBackground(chiDoc ? NHAT : Color.WHITE);
        txtNhap.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(VIEN),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        txtNhap.setPreferredSize(new Dimension(0, 42));
        txtNhap.setMinimumSize(new Dimension(0, 42));
        return txtNhap;
    }

    // Định dạng combo và danh sách lựa chọn theo màu, font của thiết kế.
    public static void dinhDangCombo(JComboBox<?> cmbChon) {
        cmbChon.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            // Vẽ nền trắng cho giá trị hiện tại của combo.
            @Override
            public void paintCurrentValueBackground(Graphics doHoa, java.awt.Rectangle vung, boolean tieuDiem) {
                doHoa.setColor(Color.WHITE);
                doHoa.fillRect(vung.x, vung.y, vung.width, vung.height);
            }

            // Tạo nút mũi tên cùng nền với ô chọn.
            @Override
            protected JButton createArrowButton() {
                JButton btnMo = taoNut("▾", "", false, null);
                btnMo.setFont(new Font(Font.DIALOG, Font.PLAIN, 11));
                btnMo.putClientProperty("khongVien", true);
                btnMo.setBorder(BorderFactory.createEmptyBorder());
                btnMo.setPreferredSize(new Dimension(24, 34));
                return btnMo;
            }
        });
        cmbChon.setFont(CHU_THUONG);
        cmbChon.setBorder(BorderFactory.createLineBorder(VIEN));
        cmbChon.setPreferredSize(new Dimension(0, 42));
        cmbChon.setMinimumSize(new Dimension(0, 42));
        cmbChon.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            // Vẽ từng lựa chọn với font và màu nền thống nhất.
            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> danhSach, Object giaTri,
                    int viTri, boolean duocChon, boolean tieuDiem) {
                super.getListCellRendererComponent(danhSach, giaTri, viTri, duocChon, tieuDiem);
                setFont(CHU_THUONG);
                setForeground(CHU);
                setBackground(duocChon && viTri >= 0 ? new Color(0xEEF4FA) : Color.WHITE);
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return this;
            }
        });
    }

    // Đặt nhãn mô tả phía trên giá trị hoặc ô nhập.
    public static JPanel taoTruong(String ten, JComponent giaTri) {
        JPanel pnlTruong = new JPanel(new BorderLayout(0, 6));
        pnlTruong.setOpaque(false);
        JLabel lblTen = taoNhan(ten, false);
        lblTen.setFont(CHU_THUONG.deriveFont(12.0f));
        lblTen.setForeground(CHU_PHU);
        lblTen.setLabelFor(giaTri);
        pnlTruong.add(lblTen, BorderLayout.NORTH);
        pnlTruong.add(giaTri, BorderLayout.CENTER);
        return pnlTruong;
    }

    // Tạo thông báo tự xuống dòng với nền thông tin hoặc cảnh báo.
    public static JPanel taoThongBao(String noiDung, boolean canhBao) {
        JPanel pnlTin = new JPanel(new BorderLayout());
        pnlTin.setBackground(canhBao ? VANG_NHAT : NHAT);
        pnlTin.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JTextArea txaTin = PhuTungUi.taoMoTa(noiDung);
        txaTin.setFont(CHU_NHO);
        txaTin.setForeground(CHU_PHU);
        pnlTin.add(txaTin, BorderLayout.CENTER);
        return pnlTin;
    }

    // Tạo bảng với tiêu đề, chiều cao dòng và đường phân cách theo thiết kế.
    public static JTable taoBang(DefaultTableModel model) {
        JTable tblBang = new JTable(model);
        tblBang.setFont(CHU_THUONG);
        tblBang.setForeground(CHU);
        tblBang.setBackground(Color.WHITE);
        tblBang.setSelectionBackground(new Color(0xEEF4FA));
        tblBang.setSelectionForeground(CHU);
        tblBang.setRowHeight(52);
        tblBang.setShowVerticalLines(false);
        tblBang.setShowHorizontalLines(true);
        tblBang.setGridColor(new Color(0xEDF1F5));
        tblBang.setIntercellSpacing(new Dimension(0, 1));
        tblBang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblBang.getTableHeader().setUI(new BasicTableHeaderUI());
        tblBang.getTableHeader().setReorderingAllowed(false);
        tblBang.getTableHeader().setPreferredSize(new Dimension(0, 39));
        DefaultTableCellRenderer boVe = new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            // Vẽ nội dung ô, liên kết thao tác và nhãn trạng thái của bảng.
            @Override
            public Component getTableCellRendererComponent(JTable bang, Object giaTri,
                    boolean chon, boolean tieuDiem, int dong, int cot) {
                super.getTableCellRendererComponent(bang, giaTri, chon, false, dong, cot);
                setFont(dong < 0 ? CHU_DAM.deriveFont(12.0f) : CHU_THUONG);
                setBackground(dong < 0 ? NHAT : chon ? new Color(0xEEF4FA) : Color.WHITE);
                setForeground("Xem chi tiết".equals(String.valueOf(giaTri)) ? XANH : CHU);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                setToolTipText(giaTri == null ? null : giaTri.toString());
                if (dong >= 0) {
                    String noiDung = String.valueOf(giaTri);
                    if (noiDung.equals("Đang sửa") || noiDung.equals("Chờ sửa chữa")
                            || noiDung.equals("Chờ phụ tùng") || noiDung.equals("Hoàn thành")
                            || noiDung.equals("Đã hoàn tất") || noiDung.equals("Chưa xác nhận")) {
                        return trangThai(noiDung, chon);
                    }
                    if (cot == 0) {
                        setFont(CHU_DAM.deriveFont(12.0f));
                    }
                }
                return this;
            }
        };
        tblBang.setDefaultRenderer(Object.class, boVe);
        tblBang.getTableHeader().setDefaultRenderer(boVe);
        return tblBang;
    }

    // Tạo nhãn màu thể hiện trạng thái công việc trong bảng.
    private static JPanel trangThai(String noiDung, boolean duocChon) {
        Color nen = noiDung.contains("Hoàn") || noiDung.contains("hoàn") ? new Color(0xE8F7EF)
                : noiDung.equals("Chờ phụ tùng") ? new Color(0xFFF3DD) : new Color(0xEAF3FF);
        Color chu = noiDung.contains("Hoàn") || noiDung.contains("hoàn") ? new Color(0x09734A)
                : noiDung.equals("Chờ phụ tùng") ? new Color(0x9B6307) : XANH;
        JLabel lblTrangThai = new JLabel(noiDung) {
            private static final long serialVersionUID = 1L;

            // Vẽ nền bo góc của nhãn trạng thái.
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
        lblTrangThai.setFont(CHU_DAM.deriveFont(11.0f));
        lblTrangThai.setForeground(chu);
        lblTrangThai.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        JPanel pnlTrangThai = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 13));
        pnlTrangThai.setBackground(duocChon ? new Color(0xEEF4FA) : Color.WHITE);
        pnlTrangThai.add(lblTrangThai);
        return pnlTrangThai;
    }

    // Bọc bảng trong vùng cuộn với chiều cao phù hợp số dòng dữ liệu.
    public static JScrollPane bocBang(JTable tblBang) {
        JScrollPane scrBang = new JScrollPane(tblBang);
        scrBang.setColumnHeaderView(tblBang.getTableHeader());
        PhuTungUi.dinhDangVungCuon(scrBang);
        scrBang.setBorder(BorderFactory.createEmptyBorder());
        scrBang.setPreferredSize(new Dimension(0, 39 + Math.max(1, tblBang.getRowCount()) * 52 + 2));
        scrBang.setMinimumSize(new Dimension(0, 93));
        return scrBang;
    }

    // Tạo model chỉ đọc cho các danh sách dữ liệu.
    public static DefaultTableModel taoModel(String... cot) {
        return new DefaultTableModel(cot, 0) {
            private static final long serialVersionUID = 1L;

            // Chặn chỉnh sửa trực tiếp các ô của danh sách.
            @Override
            public boolean isCellEditable(int dong, int cotChon) {
                return false;
            }
        };
    }

    // Tạo hàng chứa nút hành động chính căn phải ở cuối thẻ.
    public static JPanel taoHangNut(JButton btnNut) {
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlNut.setOpaque(false);
        btnNut.setPreferredSize(new Dimension(Math.max(430, btnNut.getPreferredSize().width), 42));
        pnlNut.add(btnNut);
        return pnlNut;
    }
}