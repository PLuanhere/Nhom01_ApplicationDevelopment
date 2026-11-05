package ui.screen.thungan;

import dao.HoaDonDAO;
import entity.HoaDon.HinhThucThanhToan;
import entity.HoaDon;
import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;

import ui.component.UiChung;
import ui.component.UiTheme;

/** Tìm, lọc và xem hóa đơn đã thanh toán từ nguồn dùng chung. */
public final class CashReceiptPanel extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;
    private final HoaDonDAO dao;
    private final TaiKhoan taiKhoan;
    private final JTextField txtTim = new JTextField(24);
    private final JComboBox<String> cmbHinhThuc = new JComboBox<>(
            new String[] { "Tất cả", "Tiền mặt", "Chuyển khoản" });
    private final DefaultTableModel model = new DefaultTableModel(new String[] { "Mã hóa đơn", "Mã lệnh", "Khách hàng",
            "Biển số", "Hình thức", "Số tiền", "Thanh toán lúc", "Người thu" }, 0) {
        private static final long serialVersionUID = 1L;

        /** Cho biết cell editable. */
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    private final JTable tblChungTu = UiChung.taoBang(model, 140, 140, 210, 130, 130, 140, 190, 130);
    private final JLabel lblKetQua = new JLabel();
    private ActionListener khiDoiTieuChi;
    private ActionListener khiXem;

    /** Mở màn xem thử độc lập với dữ liệu tạo sẵn. */
    public CashReceiptPanel() {
        this(new HoaDonDAO(), new TaiKhoan("thungan", VaiTro.THU_NGAN));
    }

    /** Giữ cách mở màn từ phần tra cứu chứng từ hiện có. */
    public CashReceiptPanel(String hinhThuc, ActionListener khiDoiTieuChi, ActionListener khiXem) {
        this();
        this.khiDoiTieuChi = khiDoiTieuChi;
        this.khiXem = khiXem;
        cmbHinhThuc.setSelectedItem(hinhThuc);
        taiDanhSach();
    }

    /** Tạo bộ lọc, bảng và thao tác xem chứng từ từ nguồn hóa đơn chung. */
    public CashReceiptPanel(HoaDonDAO dao, TaiKhoan taiKhoan) {
        super(new BorderLayout(0, 14));
        this.dao = dao;
        this.taiKhoan = taiKhoan;
        setBackground(UiTheme.APP_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        JPanel pnlDau = new JPanel(new BorderLayout(0, 12));
        pnlDau.setOpaque(false);
        JLabel lblTieuDe = new JLabel("Hóa đơn đã thanh toán");
        lblTieuDe.setFont(UiTheme.APP_BOLD_FONT.deriveFont(24f));
        pnlDau.add(lblTieuDe, BorderLayout.NORTH);
        JPanel pnlLoc = new JPanel(new BorderLayout(0, 8));
        pnlLoc.setOpaque(false);
        JPanel pnlTim = new JPanel(new BorderLayout(8, 0));
        pnlTim.setOpaque(false);
        txtTim.setToolTipText("Tìm mã hóa đơn, mã lệnh hoặc khách hàng");
        txtTim.setPreferredSize(new Dimension(260, 38));
        cmbHinhThuc.setPreferredSize(new Dimension(150, 38));
        pnlTim.add(txtTim, BorderLayout.CENTER);
        pnlTim.add(cmbHinhThuc, BorderLayout.EAST);
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlNut.setOpaque(false);
        pnlNut.add(taoNut("Tìm kiếm", "tim", 'T'));
        pnlNut.add(taoNut("Làm mới", "moi", 'M'));
        pnlNut.add(taoNut("Xem chứng từ", "xem", 'X'));
        pnlLoc.add(pnlTim, BorderLayout.NORTH);
        pnlLoc.add(pnlNut, BorderLayout.SOUTH);
        pnlDau.add(pnlLoc, BorderLayout.CENTER);
        add(pnlDau, BorderLayout.NORTH);
        add(new JScrollPane(tblChungTu), BorderLayout.CENTER);
        add(lblKetQua, BorderLayout.SOUTH);
        txtTim.addActionListener(this);
        cmbHinhThuc.addActionListener(this);
        tblChungTu.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "xem");
        tblChungTu.getActionMap().put("xem", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                xemChungTu();
            }
        });
        taiDanhSach();
    }

    /** Gắn thao tác mở chi tiết/in hóa đơn của khung chính. */
    public void setKhiXem(ActionListener thaoTac) {
        khiXem = thaoTac;
    }

    /** Tạo nút có phím Alt và lệnh xử lý rõ ràng. */
    private JButton taoNut(String chu, String lenh, char phim) {
        JButton btn = UiTheme.createAppButton(chu, lenh.equals("xem"));
        btn.setActionCommand(lenh);
        btn.setMnemonic(phim);
        btn.addActionListener(this);
        return btn;
    }

    /** Tìm và lọc hóa đơn, cập nhật số dòng và lựa chọn hiện tại. */
    private void taiDanhSach() {
        HinhThucThanhToan hinhThuc = null;
        if (cmbHinhThuc.getSelectedIndex() == 1) {
            hinhThuc = HinhThucThanhToan.TIEN_MAT;
        } else if (cmbHinhThuc.getSelectedIndex() == 2) {
            hinhThuc = HinhThucThanhToan.CHUYEN_KHOAN;
        }
        model.setRowCount(0);
        for (HoaDon hoaDon : dao.timKiem(taiKhoan, txtTim.getText(), hinhThuc)) {
            model.addRow(new Object[] { hoaDon.getMaHoaDon(), hoaDon.getMaPhieuSuaChua(), hoaDon.getTenKhachHang(),
                    hoaDon.getBienSo(), hoaDon.getHinhThucThanhToan().getTenHienThi(),
                    UiChung.so(hoaDon.getTongTien()) + " đ",
                    hoaDon.getThoiDiemThanhToan().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    hoaDon.getNguoiThucHien() });
        }
        lblKetQua.setText(model.getRowCount() + " hóa đơn");
        if (model.getRowCount() > 0) {
            tblChungTu.setRowSelectionInterval(0, 0);
        }
    }

    /** Mở chứng từ chỉ đọc của hóa đơn được chọn. */
    private void xemChungTu() {
        int dong = tblChungTu.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một hóa đơn.");
            return;
        }
        if (khiXem != null) {
            khiXem.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
                    model.getValueAt(tblChungTu.convertRowIndexToModel(dong), 0).toString()));
            return;
        }
        HoaDon hoaDon = dao.timHoaDonTheoMa(taiKhoan,
                model.getValueAt(tblChungTu.convertRowIndexToModel(dong), 0).toString());
        if (hoaDon == null) {
            taiDanhSach();
            return;
        }
        JTextArea txt = new JTextArea("Garage Ô tô Đức Dũng\nHÓA ĐƠN " + hoaDon.getMaHoaDon() + "\nLệnh sửa chữa: "
                + hoaDon.getMaPhieuSuaChua() + "\nBiển số: " + hoaDon.getBienSo() + "\nMã giao dịch: "
                + hoaDon.getMaGiaoDich() + "\nKhách hàng: " + hoaDon.getTenKhachHang() + "\nĐã thanh toán: "
                + UiChung.so(hoaDon.getTongTien()) + " đ\nHình thức: " + hoaDon.getHinhThucThanhToan().getTenHienThi()
                + "\nThời điểm: "
                + hoaDon.getThoiDiemThanhToan().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                + "\nNgười thu: " + hoaDon.getNguoiThucHien());
        txt.setEditable(false);
        txt.setFont(UiTheme.APP_FONT);
        JOptionPane.showMessageDialog(this, txt, "Chứng từ thanh toán", JOptionPane.PLAIN_MESSAGE);
    }

    /** Xử lý tìm kiếm, làm mới và mở hóa đơn đã chọn. */
    @Override
    public void actionPerformed(ActionEvent event) {
        try {
            if (event.getActionCommand().equals("xem")) {
                xemChungTu();
            } else {
                if (event.getActionCommand().equals("moi")) {
                    txtTim.setText("");
                    cmbHinhThuc.setSelectedIndex(0);
                    if (khiDoiTieuChi != null) {
                        khiDoiTieuChi.actionPerformed(
                                new ActionEvent(khiDoiTieuChi, ActionEvent.ACTION_PERFORMED, "thaoTac"));
                    }
                }
                taiDanhSach();
            }
        } catch (IllegalArgumentException | SecurityException error) {
            JOptionPane.showMessageDialog(this, error.getMessage());
        }
    }

    /** Trả bảng để màn hình tra cứu hiện có đọc dòng được chọn. */
    public JTable getTblChungTu() {
        return tblChungTu;
    }
}
