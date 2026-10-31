package ui.dialog;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;
import javax.swing.*;
import entity.ChiTietBaoGia;
import entity.PhuTung;
import java.util.List;
import ui.component.UiTheme;

/** Nhập một dòng vào bản nháp UI; hủy dialog không thay đổi dòng gốc. */
public final class HangMucBaoGiaDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private final JComboBox<PhuTung> cboPhuTung = new JComboBox<>();
    private final JTextField txtTen = new JTextField();
    private final JComboBox<String> cboLoai = new JComboBox<>(new String[] {"Dịch vụ", "Phụ tùng"});
    private final JTextField txtDonVi = new JTextField();
    private final JTextField txtSoLuong = new JTextField();
    private final JTextField txtDonGia = new JTextField();
    private final JLabel lblLoi = new JLabel("Dùng dấu chấm cho phần thập phân; không nhập dấu phân cách hàng nghìn.");

    public HangMucBaoGiaDialog(Window chu, ChiTietBaoGia cu, Consumer<ChiTietBaoGia> khiLuu) {
        this(chu, cu, khiLuu, List.of());
    }

    public HangMucBaoGiaDialog(Window chu, ChiTietBaoGia cu, Consumer<ChiTietBaoGia> khiLuu, List<PhuTung> danhMuc) {
        super(chu, cu == null ? "Thêm hạng mục" : "Sửa hạng mục", ModalityType.APPLICATION_MODAL);
        JPanel noiDung = new JPanel(new BorderLayout(0, 14));
        noiDung.setBackground(UiTheme.SURFACE);
        noiDung.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        JPanel truong = new JPanel(new GridLayout(0, 2, 12, 12));
        truong.setOpaque(false);
        themTruong(truong, "Hạng mục *", txtTen);
        themTruong(truong, "Loại", cboLoai);
        if (!danhMuc.isEmpty()) { cboPhuTung.addItem(null); for (PhuTung pt : danhMuc) cboPhuTung.addItem(pt); themTruong(truong, "Phụ tùng trong danh mục *", cboPhuTung); }
        themTruong(truong, "Đơn vị *", txtDonVi);
        themTruong(truong, "Số lượng *", txtSoLuong);
        themTruong(truong, "Đơn giá *", txtDonGia);
        if (cu != null) {
            txtTen.setText(cu.getTenHangMuc()); cboLoai.setSelectedItem(cu.getLoai());
            txtDonVi.setText(cu.getDonVi()); txtSoLuong.setText(cu.getSoLuong().toPlainString());
            txtDonGia.setText(cu.getDonGia().toPlainString());
        }
        if (!danhMuc.isEmpty()) {
            if (cu != null) for (PhuTung pt : danhMuc) if (pt.getMaPhuTung().equals(cu.getMaPhuTung())) cboPhuTung.setSelectedItem(pt);
            Runnable chonLoai = () -> {
                boolean laPhuTung = "Phụ tùng".equals(cboLoai.getSelectedItem());
                cboPhuTung.setEnabled(laPhuTung); txtTen.setEditable(!laPhuTung); txtDonVi.setEditable(!laPhuTung);
                if (laPhuTung) {
                    PhuTung pt = (PhuTung) cboPhuTung.getSelectedItem();
                    txtTen.setText(pt == null ? "" : pt.getTenPhuTung()); txtDonVi.setText(pt == null ? "" : pt.getDonViTinh());
                }
            };
            cboLoai.addActionListener(e -> chonLoai.run()); cboPhuTung.addActionListener(e -> chonLoai.run()); chonLoai.run();
        }
        noiDung.add(truong, BorderLayout.NORTH);
        lblLoi.setFont(UiTheme.APP_FONT); lblLoi.setForeground(UiTheme.APP_MUTED);
        noiDung.add(lblLoi);
        JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT)); nut.setOpaque(false);
        JButton huy = UiTheme.createAppButton("Hủy", false);
        huy.addActionListener(e -> dispose()); nut.add(huy);
        JButton luu = UiTheme.createAppButton("Áp dụng vào bản nháp", true);
        luu.addActionListener(e -> {
            try {
                String ten = txtTen.getText().strip(), donVi = txtDonVi.getText().strip();
                if (ten.isEmpty() || donVi.isEmpty()) throw new IllegalArgumentException("Cần tên hạng mục và đơn vị.");
                String so = txtSoLuong.getText().strip(), gia = txtDonGia.getText().strip();
                if (!so.matches("[0-9]+([.][0-9]+)?") || !gia.matches("[0-9]+([.][0-9]+)?")) {
                    throw new IllegalArgumentException("Số lượng/đơn giá phải là số; dùng dấu chấm cho phần thập phân.");
                }
                PhuTung pt = "Phụ tùng".equals(cboLoai.getSelectedItem()) ? (PhuTung) cboPhuTung.getSelectedItem() : null;
                if (!danhMuc.isEmpty() && "Phụ tùng".equals(cboLoai.getSelectedItem()) && pt == null) throw new IllegalArgumentException("Cần chọn phụ tùng trong danh mục.");
                if (pt != null) pt.kiemTraSoLuong(new java.math.BigDecimal(so));
                ChiTietBaoGia moi = new ChiTietBaoGia(ten, (String) cboLoai.getSelectedItem(), donVi, so, gia, pt == null ? "" : pt.getMaPhuTung());
                khiLuu.accept(moi); dispose();
            } catch (IllegalArgumentException error) { lblLoi.setForeground(UiTheme.ERROR); lblLoi.setText(error.getMessage()); }
        });
        nut.add(luu); noiDung.add(nut, BorderLayout.SOUTH);
        setContentPane(noiDung); setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getRootPane().setDefaultButton(luu);
        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        setSize(690, danhMuc.isEmpty() ? 340 : 420); setLocationRelativeTo(chu);
    }

    private void themTruong(JPanel pnl, String ten, JComponent truong) {
        JPanel o = new JPanel(new BorderLayout(0, 6)); o.setOpaque(false);
        JLabel lbl = new JLabel(ten); lbl.setLabelFor(truong); lbl.setFont(UiTheme.APP_BOLD_FONT);
        truong.setFont(UiTheme.APP_FONT); truong.setPreferredSize(new Dimension(180, 36));
        o.add(lbl, BorderLayout.NORTH); o.add(truong); pnl.add(o);
    }
}
