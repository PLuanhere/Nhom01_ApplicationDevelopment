package ui.screen.quanly;

import dao.PhuTungDAO;
import entity.TaiKhoan;
import dao.PhuTungDAO.BienDongKho;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import ui.component.UiChung;
import ui.component.UiTheme;

/** Hiển thị và lọc lịch sử nhập, xuất, điều chỉnh tồn kho. */
public final class NhatKyKhoPanel extends JPanel implements DocumentListener, ActionListener {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private final TaiKhoan taiKhoan;
    private final PhuTungDAO dao;
    private final PhuTungPanel manHinh;
    private final String maPhuTung;

    // Ô tìm kiếm và các bộ lọc danh sách.
    private final JTextField txtTim = UiChung.taoTimKiem("Tìm mã PT, chứng từ, loại xuất nhập...");
    private final JTextField txtTuNgay = UiChung.taoONhap(true);
    private final JTextField txtDenNgay = UiChung.taoONhap(true);
    private final JComboBox<String> cmbLoai = new JComboBox<>(
            new String[] { "Tất cả loại", "Nhập", "Xuất", "Điều chỉnh" });
    // Bảng dữ liệu và mô hình các dòng hiển thị.
    private final DefaultTableModel model = new DefaultTableModel(
            new String[] { "Ngày giờ", "Mã PT", "Mặt hàng", "Loại", "Số lượng", "Liên quan", "Thao tác" }, 0);
    private final JTable tblNhatKy = UiChung.taoBang(model, 190, 110, 300, 120, 130, 220, 170);

    // Số bản ghi đang hiển thị và phản hồi sau thao tác danh sách.
    private final JLabel lblSoLuong = UiChung.taoNhan("", 11, false, UiTheme.APP_MUTED);
    private final JLabel lblCuoi = UiChung.taoNhan("", 11, false, UiTheme.APP_MUTED);
    private final JLabel lblLoi = UiChung.taoNhan("", 12, false, new Color(0xB42318));
    private List<BienDongKho> ketQua = new ArrayList<>();

    /** Tạo bộ lọc từ khóa, loại, ngày và bảng lịch sử biến động kho. */
    public NhatKyKhoPanel(TaiKhoan taiKhoan, PhuTungDAO dao, String maPhuTung, PhuTungPanel manHinh) {
        super(new BorderLayout(0, 14));
        this.taiKhoan = taiKhoan;
        this.dao = dao;
        this.maPhuTung = maPhuTung;
        this.manHinh = manHinh;
        setOpaque(false);

        int[] rongToiThieu = { 157, 55, 90, 65, 65, 90, 100 };
        for (int i = 0; i < rongToiThieu.length; i++) {
            tblNhatKy.getColumnModel().getColumn(i).setMinWidth(rongToiThieu[i]);
        }
        JButton btnVe = UiChung.taoNut("Danh sách / quay lại", false);
        btnVe.setActionCommand("ve");
        btnVe.addActionListener(this);
        add(UiChung.taoDauTrang("Quản lý / Danh mục / Nhật ký nhập – xuất – tồn", "Nhật ký nhập – xuất – tồn",
                "Theo dõi biến động kho; xuất thực tế theo phụ tùng kỹ thuật đã dùng.", btnVe), BorderLayout.NORTH);
        JButton btnMoi = UiChung.taoNut("Làm mới", false);
        btnMoi.setActionCommand("moi");
        btnMoi.addActionListener(this);
        JPanel pnlCongCu = UiChung.taoBoLoc(txtTim, btnMoi, cmbLoai);
        JPanel pnlLoc = new JPanel();
        pnlLoc.setOpaque(false);
        pnlLoc.setLayout(new BoxLayout(pnlLoc, BoxLayout.Y_AXIS));
        pnlLoc.add(pnlCongCu);
        pnlLoc.add(Box.createVerticalStrut(10));
        pnlLoc.add(UiChung.taoHang(UiChung.taoTruong("Từ ngày · dd/MM/yyyy · trống để lấy tất cả", txtTuNgay),
                UiChung.taoTruong("Đến ngày · dd/MM/yyyy", txtDenNgay)));
        JButton btnLocNgay = UiChung.taoNut("Áp dụng ngày", false);
        btnLocNgay.setActionCommand("locNgay");
        btnLocNgay.addActionListener(this);
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        pnlNut.setOpaque(false);
        pnlNut.add(btnLocNgay);
        pnlLoc.add(pnlNut);
        pnlLoc.add(lblLoi);
        lblLoi.setVisible(false);
        JPanel pnlGiua = new JPanel();
        pnlGiua.setLayout(new BoxLayout(pnlGiua, BoxLayout.Y_AXIS));
        pnlGiua.setOpaque(false);
        pnlLoc.setAlignmentX(LEFT_ALIGNMENT);
        pnlGiua.add(pnlLoc);
        pnlGiua.add(Box.createVerticalStrut(14));
        JPanel pnlBang = UiChung.theBang("Lịch sử biến động kho", tblNhatKy, lblSoLuong, lblCuoi);
        pnlBang.setAlignmentX(LEFT_ALIGNMENT);
        pnlGiua.add(pnlBang);
        add(UiChung.taoVungCuon(pnlGiua));
        txtTim.getDocument().addDocumentListener(this);
        cmbLoai.setActionCommand("loai");
        cmbLoai.addActionListener(this);
        txtTuNgay.setActionCommand("tuNgay");
        txtTuNgay.addActionListener(this);
        txtDenNgay.setActionCommand("denNgay");
        txtDenNgay.addActionListener(this);
        tblNhatKy.addMouseListener(new MouseAdapter() {
            /** Mở chi tiết của dòng được nhấp đúp hoặc chọn ở cột thao tác. */
            @Override
            public void mouseClicked(MouseEvent event) {
                if (tblNhatKy.rowAtPoint(event.getPoint()) >= 0
                        && (event.getClickCount() == 2 || tblNhatKy.columnAtPoint(event.getPoint()) == 6)) {
                    chiTiet();
                }
            }
        });
        tblNhatKy.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "chiTiet");
        tblNhatKy.getActionMap().put("chiTiet", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            /** Thực hiện thao tác tương ứng với nút, bộ lọc hoặc phím vừa chọn. */
            @Override
            public void actionPerformed(ActionEvent event) {
                chiTiet();
            }
        });
        tai();
    }

    /** Đọc ngày lọc theo dd/MM/yyyy hoặc trả null khi ô trống. */
    private LocalDate docNgay(JTextField txt) {
        return txt.getText().isBlank() ? null : LocalDate.parse(txt.getText().trim(), NGAY);
    }

    /** Đọc lịch sử theo từ khóa, loại, khoảng ngày rồi cập nhật bảng và số kết quả. */
    private void tai() {
        try {
            String loai = cmbLoai.getSelectedIndex() == 0 ? null : String.valueOf(cmbLoai.getSelectedItem());
            List<BienDongKho> danhSach = dao.layNhatKy(taiKhoan, txtTim.getText(), loai, maPhuTung, docNgay(txtTuNgay),
                    docNgay(txtDenNgay));
            ketQua = danhSach;
            model.setRowCount(0);
            for (BienDongKho dong : danhSach) {
                model.addRow(new Object[] { dong.getThoiDiem().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                        dong.getPhuTung().getMaPhuTung(), dong.getPhuTung().getTenPhuTung(), dong.getLoai(),
                        (dong.getChenhLech() > 0 ? "+" : "") + UiChung.so(dong.getChenhLech()), dong.getChungTu(),
                        "Xem chi tiết" });
            }
            tblNhatKy.setPreferredScrollableViewportSize(
                    new Dimension(0, Math.max(1, Math.min(8, danhSach.size())) * tblNhatKy.getRowHeight()));
            tblNhatKy.revalidate();
            lblSoLuong.setText(danhSach.size() + " kết quả đang hiển thị");
            lblCuoi.setText("Hiển thị " + danhSach.size() + " biến động" + (maPhuTung == null ? "" : " · " + maPhuTung)
                    + (danhSach.isEmpty() ? " · Chưa có biến động phù hợp." : ""));
            lblLoi.setVisible(false);
        } catch (DateTimeParseException ex) {
            loi("Ngày không hợp lệ. Nhập đúng dd/MM/yyyy hoặc để trống.");
        } catch (IllegalArgumentException | SecurityException ex) {
            loi(ex.getMessage());
        }
    }

    /** Hiển thị lỗi trên màn hình và cập nhật bố cục thông báo. */
    private void loi(String chu) {
        lblLoi.setText(chu);
        lblLoi.setToolTipText(chu);
        lblLoi.setVisible(true);
        revalidate();
    }

    /** Mở phiếu nhập hoặc thông báo chi tiết của biến động kho được chọn. */
    private void chiTiet() {
        int i = tblNhatKy.getSelectedRow();
        if (i < 0) {
            return;
        }
        try {
            taiKhoan.kiemTraPhien();
            BienDongKho dong = ketQua.get(i);
            if ("Nhập".equals(dong.getLoai())) {
                manHinh.moNhapKho(dong.getChungTu());
                return;
            }
            JOptionPane.showMessageDialog(this, dong.getPhuTung() + "\nLoại: " + dong.getLoai() + "\nNgày giờ: "
                    + dong.getThoiDiem().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "\nTồn trước: "
                    + UiChung.so(dong.getTonTruoc()) + "\nChênh lệch: " + UiChung.so(dong.getChenhLech())
                    + "\nTồn sau: " + UiChung.so(dong.getTonSau()) + "\nChứng từ: " + dong.getChungTu()
                    + "\nNgười thực hiện: " + dong.getNguoiThucHien() + "\nLý do: " + dong.getLyDo(),
                    "Chi tiết biến động kho", JOptionPane.INFORMATION_MESSAGE);
        } catch (SecurityException ex) {
            loi(ex.getMessage());
        }
    }

    /** Cập nhật kết quả tìm kiếm khi nội dung ô tìm thay đổi. */
    @Override
    public void insertUpdate(DocumentEvent event) {
        tai();
    }

    /** Cập nhật kết quả tìm kiếm khi nội dung ô tìm thay đổi. */
    @Override
    public void removeUpdate(DocumentEvent event) {
        tai();
    }

    /** Cập nhật kết quả tìm kiếm khi nội dung ô tìm thay đổi. */
    @Override
    public void changedUpdate(DocumentEvent event) {
        tai();
    }

    /** Thực hiện thao tác tương ứng với nút, bộ lọc hoặc phím vừa chọn. */
    @Override
    public void actionPerformed(ActionEvent event) {
        String lenh = event.getActionCommand();
        if ("ve".equals(lenh)) {
            manHinh.moDanhSach();
        } else if ("moi".equals(lenh)) {
            txtTuNgay.setText("");
            txtDenNgay.setText("");
            txtTim.setText("");
            cmbLoai.setSelectedIndex(0);
            tai();
        } else if ("locNgay".equals(lenh)) {
            tai();
        } else if ("loai".equals(lenh)) {
            tai();
        } else if ("tuNgay".equals(lenh)) {
            tai();
        } else if ("denNgay".equals(lenh)) {
            tai();
        }
    }
}
