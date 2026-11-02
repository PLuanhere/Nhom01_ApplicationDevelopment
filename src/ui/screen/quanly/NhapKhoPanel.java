package ui.screen.quanly;

import dao.phutung.PhieuNhapKhoDAO;
import dao.phutung.PhuTungDAO;
import entity.nhansu.TaiKhoan;
import entity.phutung.ChiTietNhapKho;
import entity.phutung.PhieuNhapKho;
import entity.phutung.PhuTung;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import ui.component.QuanLyFormPanel;
import ui.component.UiChung;
import ui.component.UiTheme;

/** Biểu mẫu nhập nhiều mặt hàng hoặc xem lại lần nhập đã ghi nhận. */
public final class NhapKhoPanel extends QuanLyFormPanel implements ActionListener {
    private static final long serialVersionUID = 1L;
    private final PhuTungPanel manHinh;
    private final TaiKhoan taiKhoan;
    private final PhuTungDAO kho;
    private final PhieuNhapKhoDAO dao;
    private final boolean chiDoc;
    private final List<ChiTietNhapKho> cacDong = new ArrayList<>();

    // Các ô nhập và lựa chọn trên biểu mẫu.
    private final JTextField txtMa = UiChung.taoONhap(false);
    private final JTextField txtNguon = UiChung.taoONhap(true);
    private final JTextField txtChungTu = UiChung.taoONhap(true);

    // Các nhãn báo lỗi và mô tả trạng thái biểu mẫu.
    private final JLabel lblTong = UiChung.taoNhan("", 14, true, UiTheme.APP_TEXT);
    private final JLabel lblLoi = UiChung.taoNhan("", 12, false, new Color(0xB42318));
    // Bảng dữ liệu và mô hình các dòng hiển thị.
    private final DefaultTableModel model = new DefaultTableModel(
            new String[] { "Mã PT", "Tên phụ tùng", "Đơn vị", "Số lượng", "Đơn giá nhập (đ)", "Thành tiền (đ)" }, 0);
    private final JTable tblHang = UiChung.taoBang(model, 120, 300, 110, 130, 180, 180);
    private boolean daLuu;

    /** Tạo biểu mẫu nhập nhiều mặt hàng hoặc xem lại phiếu đã lưu. */
    public NhapKhoPanel(PhuTungPanel manHinh, TaiKhoan taiKhoan, PhuTungDAO kho, PhieuNhapKho phieu) {
        super();
        this.manHinh = manHinh;
        this.taiKhoan = taiKhoan;
        this.kho = kho;
        dao = kho.getNhapKhoDAO();
        chiDoc = phieu != null;
        JButton btnVe = UiChung.taoNut("Danh sách / quay lại", false);
        btnVe.setActionCommand("ve");
        btnVe.addActionListener(this);
        // Tiêu đề và điều hướng quay lại danh sách.
        add(UiChung.taoDauTrang("Quản lý / Phụ tùng / Nhập kho", chiDoc ? "Chi tiết lần nhập kho" : "Nhập kho",
                "Ghi nhận nhiều mặt hàng; chỉ cộng tồn sau khi kiểm tra và xác nhận.", btnVe), BorderLayout.NORTH);
        JPanel pnl = UiChung.taoThe(null);
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setBorder(
                BorderFactory.createCompoundBorder(pnl.getBorder(), BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        dong(pnl, UiChung.taoNhan(chiDoc ? "Thông tin lần nhập đã ghi nhận" : "Thông tin nhập kho", 18, true,
                UiTheme.APP_TEXT));
        JTextField txtNgay = UiChung.taoONhap(false);
        txtNgay.setText((phieu == null ? LocalDate.now() : phieu.getNgayNhap().toLocalDate())
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtMa.setText(phieu == null ? dao.maTiepTheo(taiKhoan) : phieu.getMaPhieu());
        dong(pnl, UiChung.taoHang(UiChung.taoTruong("Mã lần nhập · tự sinh · chỉ đọc", txtMa),
                UiChung.taoTruong("Ngày nhập · tự động · chỉ đọc", txtNgay)));
        dong(pnl, UiChung.taoHang(UiChung.taoTruong("Nguồn hàng / diễn giải *", txtNguon),
                UiChung.taoTruong("Mã hóa đơn / biên nhận · nếu có", txtChungTu)));
        JTextField txtNguoi = UiChung.taoONhap(false);
        txtNguoi.setText(phieu == null ? taiKhoan.getTenDangNhap() : phieu.getNguoiNhap());
        dong(pnl, UiChung.taoTruong("Người nhập · tài khoản thực hiện · chỉ đọc", txtNguoi));
        if (phieu != null) {
            txtNguon.setText(phieu.getNguonNhap());
            txtChungTu.setText(phieu.getChungTuNguon());
            txtNguon.setEditable(false);
            txtChungTu.setEditable(false);
            txtNguon.setBackground(new Color(0xF5F7FB));
            txtChungTu.setBackground(new Color(0xF5F7FB));
            cacDong.addAll(phieu.getChiTiet());
        }
        if (!chiDoc) {
            JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            pnlNut.setOpaque(false);
            JButton btnThem = UiChung.taoNut("+ Thêm mặt hàng", false);
            JButton btnSua = UiChung.taoNut("Sửa dòng", false);
            JButton btnBo = UiChung.taoNut("Bỏ dòng", false);
            btnThem.setActionCommand("them");
            btnThem.addActionListener(this);
            btnSua.setActionCommand("sua");
            btnSua.addActionListener(this);
            btnBo.setActionCommand("bo");
            btnBo.addActionListener(this);
            pnlNut.add(btnThem);
            pnlNut.add(btnSua);
            pnlNut.add(btnBo);
            dong(pnl, pnlNut);
        }
        dong(pnl, UiChung.cuonBang(tblHang, 3));
        dong(pnl, lblTong);
        dong(pnl, UiChung.taoLuuY("Một lần nhập có nhiều mặt hàng. Nguồn hàng / diễn giải là bắt buộc; "
                + "mã hóa đơn/biên nhận tùy chọn. Sau ghi nhận chỉ xem lại; giá trên lần nhập không sửa giá danh mục."));
        // Vùng thông tin có cuộn khi chiều cao cửa sổ không đủ.
        add(UiChung.taoVungCuon(pnl));
        if (!chiDoc) {
            JButton btnLuu = UiChung.taoNut("Kiểm tra & ghi nhận nhập kho", true);
            btnLuu.setActionCommand("luu");
            btnLuu.addActionListener(this);
            JButton btnHuy = UiChung.taoNut("Hủy", false);
            btnHuy.setActionCommand("huy");
            btnHuy.addActionListener(this);
            // Thanh phản hồi và các nút thao tác nằm dưới biểu mẫu.
            add(UiChung.taoChanTrang(lblLoi, btnHuy, btnLuu), BorderLayout.SOUTH);
        } else {
            lblLoi.setText("Đã nhập · thông tin và đơn giá được giữ tại thời điểm ghi nhận.");
            lblLoi.setForeground(UiTheme.APP_MUTED);
            add(UiChung.taoChanTrang(lblLoi), BorderLayout.SOUTH);
        }
        taiBang();
    }

    /** Thêm một hàng thành phần UI và khoảng cách vào bố cục dọc. */
    private static void dong(JPanel pnl, JComponent truong) {
        truong.setAlignmentX(LEFT_ALIGNMENT);
        pnl.add(truong);
        pnl.add(Box.createVerticalStrut(12));
    }

    /** Mở hộp thoại thêm hoặc sửa một mặt hàng nhập và kiểm tra dữ liệu dòng. */
    private void suaDong(int viTri) {
        try {
            List<PhuTung> danhMuc = new ArrayList<>();
            for (PhuTung pt : kho.layDanhSach(taiKhoan)) {
                if (pt.isHoatDong()) {
                    danhMuc.add(pt);
                }
            }
            if (danhMuc.isEmpty()) {
                loi("Chưa có phụ tùng hoạt động. Thêm hoặc mở lại phụ tùng trước khi nhập.");
                return;
            }
            ChiTietNhapKho cu = viTri < 0 ? null : cacDong.get(viTri);
            JComboBox<PhuTung> cmbPhuTung = new JComboBox<>();
            for (PhuTung pt : danhMuc) {
                cmbPhuTung.addItem(pt);
            }
            UiChung.dinhDangO(cmbPhuTung);
            cmbPhuTung.setRenderer(new DefaultListCellRenderer() {
                private static final long serialVersionUID = 1L;

                /** Hiển thị nội dung lựa chọn rõ ràng trong danh sách hoặc hộp chọn. */
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean chon,
                        boolean focus) {
                    super.getListCellRendererComponent(list, value, index, chon, focus);
                    if (value instanceof PhuTung) {
                        setText(value + " · " + ((PhuTung) value).getDonViTinh());
                    }
                    return this;
                }
            });
            cmbPhuTung.setPreferredSize(new Dimension(430, 42));
            if (cu != null) {
                cmbPhuTung.setSelectedIndex(-1);
                for (PhuTung pt : danhMuc) {
                    if (pt.getMaPhuTung().equals(cu.getPhuTung().getMaPhuTung())) {
                        cmbPhuTung.setSelectedItem(pt);
                        break;
                    }
                }
            }
            JTextField txtSoLuong = UiChung.taoONhap(true);
            JTextField txtGia = UiChung.taoONhap(true);
            txtSoLuong.setText(cu == null ? "" : UiChung.so(cu.getSoLuong()));
            PhuTung chon = (PhuTung) cmbPhuTung.getSelectedItem();
            txtGia.setText(cu == null ? UiChung.so(chon.getGiaNhap()) : UiChung.so(cu.getDonGia()));
            cmbPhuTung.addActionListener(new ActionListener() {
                /** Thực hiện thao tác tương ứng với nút, bộ lọc hoặc phím vừa chọn. */
                @Override
                public void actionPerformed(ActionEvent event) {
                    PhuTung pt = (PhuTung) cmbPhuTung.getSelectedItem();
                    if (pt != null) {
                        txtGia.setText(UiChung.so(pt.getGiaNhap()));
                    }
                }
            });
            JPanel pnl = new JPanel(new BorderLayout(0, 12));
            pnl.add(UiChung.taoTruong("Phụ tùng * · đơn vị hiển thị trong danh sách", cmbPhuTung), BorderLayout.NORTH);
            JPanel pnlSo = UiChung.taoHang(UiChung.taoTruong("Số lượng nhập *", txtSoLuong),
                    UiChung.taoTruong("Đơn giá nhập * · VND", txtGia));
            pnl.add(pnlSo);
            JLabel lblNhapLoi = UiChung.taoNhan("Lít được nhập lẻ; cái/bộ phải nguyên. Phần lẻ dùng dấu phẩy.", 11,
                    false, UiTheme.APP_MUTED);
            pnl.add(lblNhapLoi, BorderLayout.SOUTH);
            while (JOptionPane.showConfirmDialog(this, pnl, cu == null ? "Thêm mặt hàng" : "Sửa mặt hàng",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                try {
                    taiKhoan.kiemTraPhien();
                    PhuTung pt = (PhuTung) cmbPhuTung.getSelectedItem();
                    if (pt == null) {
                        throw new IllegalArgumentException("Chọn phụ tùng đang hoạt động.");
                    }
                    ChiTietNhapKho moi = new ChiTietNhapKho(pt, UiChung.docSo(txtSoLuong.getText(), "Số lượng"),
                            UiChung.docTien(txtGia.getText(), "Đơn giá nhập"));
                    if (viTri < 0) {
                        cacDong.add(moi);
                    } else {
                        cacDong.set(viTri, moi);
                    }
                    taiBang();

                    return;
                } catch (IllegalArgumentException ex) {
                    lblNhapLoi.setText(ex.getMessage());
                    lblNhapLoi.setToolTipText(ex.getMessage());
                    lblNhapLoi.setForeground(new Color(0xB42318));
                }
            }
        } catch (IllegalArgumentException | SecurityException ex) {
            loi(ex.getMessage());
        }
    }

    /** Cập nhật các dòng nhập kho và tính lại tổng tiền. */
    private void taiBang() {
        model.setRowCount(0);
        long tong = 0;
        for (ChiTietNhapKho dong : cacDong) {
            model.addRow(new Object[] { dong.getPhuTung().getMaPhuTung(), dong.getPhuTung().getTenPhuTung(),
                    dong.getPhuTung().getDonViTinh(), dong.getSoLuong(), dong.getDonGia(), dong.getThanhTien() });
            if (Long.MAX_VALUE - tong < dong.getThanhTien()) {
                loi("Tổng tiền vượt giới hạn cho phép.");
                return;
            }
            tong = tong + dong.getThanhTien();
        }
        lblTong.setText(cacDong.size() + " dòng hàng · Tổng tiền: " + UiChung.so(tong) + " đ");
        if (!chiDoc) {
            lblLoi.setText("");
            lblLoi.setVisible(false);
        }
        revalidate();
    }

    /** Kiểm tra dữ liệu chưa lưu và xác nhận trước khi rời màn hình. */
    public boolean choPhepRoiTrang() {
        return UiChung.hoiBoThayDoi(this, !chiDoc && !daLuu
                && (!cacDong.isEmpty() || !txtNguon.getText().isBlank() || !txtChungTu.getText().isBlank()));
    }

    /** Kiểm tra thay đổi chưa lưu rồi quay về danh sách. */
    private void quayLai() {
        if (choPhepRoiTrang()) {
            manHinh.moDanhSach();
        }
    }

    /** Hiển thị lỗi trên màn hình và cập nhật bố cục thông báo. */
    private void loi(String chu) {
        lblLoi.setText(chu);
        lblLoi.setToolTipText(chu);
        lblLoi.setVisible(true);
        revalidate();
    }

    /** Kiểm tra phiếu, xác nhận và ghi nhận toàn bộ mặt hàng nhập kho. */
    private void luu() {
        if (chiDoc || daLuu) {
            return;
        }
        lblLoi.setVisible(false);
        try {
            if (txtNguon.getText().isBlank()) {
                throw new IllegalArgumentException("Nhập nguồn hàng / diễn giải.");
            }
            if (cacDong.isEmpty()) {
                throw new IllegalArgumentException("Thêm ít nhất một mặt hàng trước khi ghi nhận.");
            }
            if (JOptionPane.showConfirmDialog(this,
                    "Ghi nhận " + txtMa.getText() + " với " + cacDong.size() + " dòng hàng?\n" + lblTong.getText()
                            + "\nTồn kho được cộng sau khi xác nhận.",
                    "Xác nhận nhập kho", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE) != JOptionPane.OK_OPTION) {
                return;
            }
            PhieuNhapKho phieu = dao.ghiNhan(taiKhoan, txtMa.getText(), txtNguon.getText(), txtChungTu.getText(),
                    cacDong);
            daLuu = true;
            manHinh.moDanhSach();
            manHinh.thongBao("Đã nhập kho " + phieu.getMaPhieu() + ", cộng tồn và ghi nhật ký.");
        } catch (IllegalArgumentException | SecurityException ex) {
            loi(ex.getMessage());
            if (ex.getMessage().startsWith("Mã lần nhập")) {
                txtMa.setText(dao.maTiepTheo(taiKhoan));
            }
        }
    }

    /** Thực hiện thao tác tương ứng với nút, bộ lọc hoặc phím vừa chọn. */
    @Override
    public void actionPerformed(ActionEvent event) {
        String lenh = event.getActionCommand();
        if ("ve".equals(lenh)) {
            quayLai();
        } else if ("them".equals(lenh)) {
            suaDong(-1);
        } else if ("sua".equals(lenh)) {
            if (tblHang.getSelectedRow() >= 0) {
                suaDong(tblHang.getSelectedRow());
            } else {
                loi("Chọn dòng cần sửa.");
            }
        } else if ("bo".equals(lenh)) {
            int i = tblHang.getSelectedRow();
            if (i >= 0) {
                cacDong.remove(i);
                taiBang();
            } else {
                loi("Chọn dòng cần bỏ.");
            }
        } else if ("luu".equals(lenh)) {
            luu();
        } else if ("huy".equals(lenh)) {
            quayLai();
        }
    }
}
