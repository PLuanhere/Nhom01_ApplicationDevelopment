package ui.dialog.tiepnhan;

import entity.baogia.ChiTietBaoGia;
import entity.dichvu.DichVu;
import entity.phutung.PhuTung;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import ui.component.UiChung;
import ui.component.UiTheme;

/** Chọn hạng mục từ danh mục; chỉ trả dòng nháp, chưa lưu báo giá hoặc thay đổi kho. */
public final class HangMucBaoGiaDialog extends JDialog implements ActionListener {
    private static final long serialVersionUID = 1L;
    private final List<PhuTung> phuTung;
    private final List<DichVu> dichVu;
    private final ActionListener khiLuu;

    // Loại và danh mục quyết định mã liên kết, tên và đơn vị; số lượng, giá là bản chụp báo giá.
    private final JComboBox<String> cboLoai = new JComboBox<>(new String[] { "Dịch vụ", "Phụ tùng" });
    private final JComboBox<Object> cboDanhMuc = new JComboBox<>();
    private final JTextField txtTen = new JTextField();
    private final JTextField txtDonVi = new JTextField();
    private final JTextField txtSoLuong = new JTextField("1");
    private final JTextField txtDonGia = new JTextField("0");
    private final JLabel lblLoi = new JLabel("Lít dùng dấu phẩy, tối đa 3 số lẻ; tiền nguyên đồng, ví dụ 150.000.");

    /** Tạo form chọn danh mục; sửa dòng giữ lại thông tin và giá đã nhập trước đó. */
    public HangMucBaoGiaDialog(Window chu, ChiTietBaoGia cu, ActionListener khiLuu, List<PhuTung> phuTung,
            List<DichVu> dichVu) {
        super(chu, cu == null ? "Thêm hạng mục báo giá" : "Sửa hạng mục báo giá", ModalityType.APPLICATION_MODAL);
        if (phuTung == null || dichVu == null || khiLuu == null) {
            throw new IllegalArgumentException("Thiếu danh mục hoặc màn hình nhận dòng báo giá.");
        }
        this.phuTung = phuTung;
        this.dichVu = dichVu;
        this.khiLuu = khiLuu;
        JPanel pnl = new JPanel(new BorderLayout(0, 14));
        pnl.setBackground(UiTheme.SURFACE);
        pnl.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        JPanel pnlTruong = new JPanel(new GridLayout(0, 2, 12, 12));
        pnlTruong.setOpaque(false);
        themTruong(pnlTruong, "Loại hạng mục", cboLoai);
        themTruong(pnlTruong, "Chọn trong danh mục *", cboDanhMuc);
        themTruong(pnlTruong, "Tên hạng mục", txtTen);
        themTruong(pnlTruong, "Đơn vị", txtDonVi);
        themTruong(pnlTruong, "Số lượng *", txtSoLuong);
        themTruong(pnlTruong, "Đơn giá *", txtDonGia);
        txtTen.setEditable(false);
        txtDonVi.setEditable(false);
        if (cu != null) {
            cboLoai.setSelectedItem(cu.getLoai());
        }
        napDanhMuc();
        if (cu != null) {
            for (int i = 1; i < cboDanhMuc.getItemCount(); i++) {
                Object muc = cboDanhMuc.getItemAt(i);
                if ((muc instanceof DichVu && ((DichVu) muc).getMaDichVu().equals(cu.getMaDichVu()))
                        || (muc instanceof PhuTung && ((PhuTung) muc).getMaPhuTung().equals(cu.getMaPhuTung()))) {
                    cboDanhMuc.setSelectedIndex(i);
                    break;
                }
            }
            txtTen.setText(cu.getTenHangMuc());
            txtDonVi.setText(cu.getDonVi());
            txtSoLuong.setText(UiChung.so(cu.getSoLuong()));
            txtDonGia.setText(UiChung.so(cu.getDonGia()));
        }
        cboLoai.setActionCommand("loai");
        cboLoai.addActionListener(this);
        cboDanhMuc.setActionCommand("danhMuc");
        cboDanhMuc.addActionListener(this);
        pnl.add(pnlTruong, BorderLayout.CENTER);

        // Phản hồi và nút xác nhận; Enter lưu dòng, Escape hủy mà không sửa dữ liệu gốc.
        JPanel pnlCuoi = new JPanel(new BorderLayout(0, 10));
        pnlCuoi.setOpaque(false);
        lblLoi.setFont(UiTheme.APP_FONT.deriveFont(12f));
        pnlCuoi.add(lblLoi, BorderLayout.NORTH);
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.setOpaque(false);
        JButton btnHuy = UiTheme.createAppButton("Hủy", false);
        btnHuy.setActionCommand("huy");
        btnHuy.setMnemonic('H');
        btnHuy.addActionListener(this);
        JButton btnLuu = UiTheme.createAppButton("Lưu dòng báo giá", true);
        btnLuu.setActionCommand("luu");
        btnLuu.setMnemonic('L');
        btnLuu.addActionListener(this);
        pnlNut.add(btnHuy);
        pnlNut.add(btnLuu);
        pnlCuoi.add(pnlNut, BorderLayout.SOUTH);
        pnl.add(pnlCuoi, BorderLayout.SOUTH);
        setContentPane(pnl);
        getRootPane().setDefaultButton(btnLuu);
        getRootPane().registerKeyboardAction(this, "huy", KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        setSize(760, 430);
        setMinimumSize(new Dimension(660, 380));
        setLocationRelativeTo(chu);
    }

    /** Đọc danh mục của loại đang chọn và yêu cầu người dùng chọn một mã cụ thể. */
    private void napDanhMuc() {
        cboDanhMuc.removeAllItems();
        if (cboLoai.getSelectedIndex() == 0) {
            cboDanhMuc.addItem("Chọn dịch vụ...");
            for (DichVu dv : dichVu) {
                cboDanhMuc.addItem(dv);
            }
        } else {
            cboDanhMuc.addItem("Chọn phụ tùng...");
            for (PhuTung pt : phuTung) {
                cboDanhMuc.addItem(pt);
            }
        }
    }

    /** Điền tên, đơn vị và giá hiện tại khi người dùng chọn một mục khác. */
    private void dienDanhMuc() {
        Object muc = cboDanhMuc.getSelectedItem();
        if (muc instanceof DichVu) {
            DichVu dv = (DichVu) muc;
            txtTen.setText(dv.getTenDichVu());
            txtDonVi.setText(dv.getDonViTinh());
            txtDonGia.setText(UiChung.so(dv.getDonGia()));
        } else if (muc instanceof PhuTung) {
            PhuTung pt = (PhuTung) muc;
            txtTen.setText(pt.getTenPhuTung());
            txtDonVi.setText(pt.getDonViTinh());
            txtDonGia.setText(UiChung.so(pt.getGiaBan()));
        } else {
            txtTen.setText("");
            txtDonVi.setText("");
            txtDonGia.setText("0");
        }
    }

    /** Kiểm tra dòng nhập và trả đầy đủ mã liên kết, số lượng, đơn giá cho màn báo giá. */
    private void luuDong() {
        Object muc = cboDanhMuc.getSelectedItem();
        String maDV = "", maPT = "";
        if (muc instanceof DichVu && cboLoai.getSelectedIndex() == 0) {
            maDV = ((DichVu) muc).getMaDichVu();
        } else if (muc instanceof PhuTung && cboLoai.getSelectedIndex() == 1) {
            maPT = ((PhuTung) muc).getMaPhuTung();
        } else {
            throw new IllegalArgumentException("Cần chọn dịch vụ hoặc phụ tùng trong danh mục.");
        }
        double soLuong = UiChung.docSo(txtSoLuong.getText(), "Số lượng");
        long donGia = UiChung.docTien(txtDonGia.getText(), "Đơn giá");
        if (muc instanceof PhuTung) {
            ((PhuTung) muc).kiemTraSoLuong(soLuong);
        }
        ChiTietBaoGia moi = new ChiTietBaoGia(maDV, txtTen.getText(), (String) cboLoai.getSelectedItem(),
                txtDonVi.getText(), soLuong, donGia, maPT);
        khiLuu.actionPerformed(new ActionEvent(moi, ActionEvent.ACTION_PERFORMED, "luuDong"));
        dispose();
    }

    /** Tạo nhãn và control cùng một nhóm trong biểu mẫu. */
    private void themTruong(JPanel pnl, String ten, JComponent control) {
        JPanel o = new JPanel(new BorderLayout(0, 6));
        o.setOpaque(false);
        JLabel lbl = new JLabel(ten);
        lbl.setFont(UiTheme.APP_BOLD_FONT);
        lbl.setLabelFor(control);
        control.setFont(UiTheme.APP_FONT);
        control.setPreferredSize(new Dimension(0, 38));
        o.add(lbl, BorderLayout.NORTH);
        o.add(control, BorderLayout.CENTER);
        pnl.add(o);
    }

    /** Xử lý đổi loại, chọn danh mục, lưu dòng hoặc hủy hộp thoại. */
    @Override
    public void actionPerformed(ActionEvent event) {
        try {
            if (event.getActionCommand().equals("huy")) {
                dispose();
            } else if (event.getActionCommand().equals("loai")) {
                napDanhMuc();
                dienDanhMuc();
            } else if (event.getActionCommand().equals("danhMuc")) {
                dienDanhMuc();
            } else if (event.getActionCommand().equals("luu")) {
                luuDong();
            }
        } catch (IllegalArgumentException | SecurityException error) {
            lblLoi.setForeground(UiTheme.ERROR);
            lblLoi.setText(error.getMessage());
        }
    }
}
