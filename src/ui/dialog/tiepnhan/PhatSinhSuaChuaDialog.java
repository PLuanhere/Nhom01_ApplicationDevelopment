package ui.dialog.tiepnhan;

import dao.PhatSinhDAO;
import dao.PhuTungSuDungDAO;
import entity.BaoGia.ChiTietBaoGia;
import entity.DeXuatPhatSinh;
import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import ui.screen.kythuat.GiaoDienKyThuat;

// Tiếp nhận lập phụ lục và ghi quyết định khách; kỹ thuật chỉ xem giá và phản hồi.
public final class PhatSinhSuaChuaDialog extends JDialog implements ActionListener {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm:ss")
            .withResolverStyle(ResolverStyle.STRICT);
    private final PhatSinhDAO dao = new PhatSinhDAO();
    private final TaiKhoan phien;
    private final String maLenh;
    private final JComboBox<DeXuatPhatSinh> cmbDanhSach = new JComboBox<>();
    private final JTextArea txtNoiDung = new JTextArea(7, 50);
    private final JTextField txtPhut = GiaoDienKyThuat.taoONhap("0", false);
    private final JTextField txtKhach = GiaoDienKyThuat.taoONhap("", false);
    private final JTextField txtKenh = GiaoDienKyThuat.taoONhap("Trực tiếp", false);
    private final JTextField txtLuc = GiaoDienKyThuat.taoONhap("", false);
    private final JTextField txtGhiChu = GiaoDienKyThuat.taoONhap("", false);
    private final JLabel lblThongBao = GiaoDienKyThuat.taoNhan(" ", false);
    private final JLabel lblTong = GiaoDienKyThuat.taoNhan("Tổng phụ lục: 0 đ", true);
    private final DefaultTableModel model = GiaoDienKyThuat.taoModel("Hạng mục", "Loại", "Số lượng", "Đơn giá", "Thành tiền");
    private final JTable tblGia = GiaoDienKyThuat.taoBang(model);
    private final List<ChiTietBaoGia> banNhap = new ArrayList<>();
    private final JButton btnThem = nut("Thêm dòng", "THEM", false);
    private final JButton btnSua = nut("Sửa dòng", "SUA", false);
    private final JButton btnXoa = nut("Xóa dòng", "XOA", false);
    private final JButton btnLuu = nut("Lưu phụ lục giá", "LUU", true);
    private final JButton btnDongY = nut("Khách đồng ý", "DONG_Y", true);
    private final JButton btnTuChoi = nut("Khách từ chối", "TU_CHOI", false);
    private DeXuatPhatSinh dangXem;
    private boolean daSua;
    private boolean dangNapDanhSach;

    public PhatSinhSuaChuaDialog(Window chu, TaiKhoan phien, String maLenh) {
        super(chu, "Phát sinh sửa chữa · " + maLenh, ModalityType.APPLICATION_MODAL);
        this.phien = phien;
        this.maLenh = maLenh;
        JPanel pnl = new JPanel(new BorderLayout(0, 12));
        pnl.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        pnl.setBackground(Color.WHITE);
        JPanel dau = new JPanel(new BorderLayout(8, 0));
        dau.setOpaque(false);
        GiaoDienKyThuat.dinhDangCombo(cmbDanhSach);
        cmbDanhSach.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (dangNapDanhSach) return;
                DeXuatPhatSinh moi = (DeXuatPhatSinh) cmbDanhSach.getSelectedItem();
                if (moi == dangXem) return;
                if (daSua && JOptionPane.showConfirmDialog(PhatSinhSuaChuaDialog.this,
                        "Bỏ phụ lục đang sửa để chuyển sang phát sinh khác?", "Chuyển phát sinh",
                        JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    dangNapDanhSach = true;
                    try {
                        cmbDanhSach.setSelectedItem(dangXem);
                    } finally {
                        dangNapDanhSach = false;
                    }
                    return;
                }
                hienThiDeXuat(moi);
            }
        });
        dau.add(cmbDanhSach, BorderLayout.CENTER);
        dau.add(nut("Mở / tải lại phát sinh", "MO", false), BorderLayout.EAST);
        pnl.add(dau, BorderLayout.NORTH);
        JPanel cot = GiaoDienKyThuat.taoCot();
        txtNoiDung.setFont(GiaoDienKyThuat.CHU_THUONG);
        txtNoiDung.setEditable(false);
        txtNoiDung.setLineWrap(true);
        txtNoiDung.setWrapStyleWord(true);
        GiaoDienKyThuat.themThanhPhan(cot, new JScrollPane(txtNoiDung));
        GiaoDienKyThuat.themThanhPhan(cot, GiaoDienKyThuat.bocBang(tblGia));
        JPanel nutGia = new JPanel(new FlowLayout(FlowLayout.LEFT));
        nutGia.setOpaque(false);
        nutGia.add(btnThem);
        nutGia.add(btnSua);
        nutGia.add(btnXoa);
        nutGia.add(btnLuu);
        GiaoDienKyThuat.themThanhPhan(cot, nutGia);
        GiaoDienKyThuat.themThanhPhan(cot, lblTong);
        GiaoDienKyThuat.themThanhPhan(cot, truong("Số phút bổ sung theo phụ lục", txtPhut));
        JPanel phanHoi = new JPanel(new GridLayout(0, 2, 12, 10));
        phanHoi.setOpaque(false);
        phanHoi.add(truong("Tên người xác nhận phía khách *", txtKhach));
        phanHoi.add(truong("Kênh phản hồi *", txtKenh));
        phanHoi.add(truong("Thời điểm khách phản hồi · dd/MM/yyyy HH:mm:ss *", txtLuc));
        phanHoi.add(truong("Nội dung phản hồi / lý do *", txtGhiChu));
        GiaoDienKyThuat.themThanhPhan(cot, phanHoi);
        JPanel nutPhanHoi = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        nutPhanHoi.setOpaque(false);
        nutPhanHoi.add(btnTuChoi);
        nutPhanHoi.add(btnDongY);
        GiaoDienKyThuat.themThanhPhan(cot, nutPhanHoi);
        pnl.add(new JScrollPane(cot), BorderLayout.CENTER);
        JPanel cuoi = new JPanel(new BorderLayout());
        cuoi.setOpaque(false);
        cuoi.add(lblThongBao, BorderLayout.CENTER);
        cuoi.add(nut("Đóng", "DONG", false), BorderLayout.EAST);
        pnl.add(cuoi, BorderLayout.SOUTH);
        setContentPane(pnl);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            // Hỏi trước khi bỏ phụ lục đang chỉnh nhưng chưa lưu.
            @Override
            public void windowClosing(WindowEvent e) {
                dong();
            }
        });
        setSize(1080, 820);
        setLocationRelativeTo(chu);
        taiDanhSach(null);
    }

    // Tạo nút với lệnh xử lý tập trung, dùng cùng font và màu của kỹ thuật.
    private JButton nut(String ten, String lenh, boolean chinh) {
        return GiaoDienKyThuat.taoNut(ten, lenh, chinh, this);
    }

    // Tạo nhãn và ô nhập trong một nhóm thông tin.
    private JPanel truong(String ten, JComponent thanhPhan) {
        return GiaoDienKyThuat.taoTruong(ten, thanhPhan);
    }

    // Tải lại danh sách và mở đúng đề xuất vừa lưu hoặc vừa ghi phản hồi.
    private void taiDanhSach(Long ma) {
        dangNapDanhSach = true;
        try {
            cmbDanhSach.removeAllItems();
            DeXuatPhatSinh chon = null;
            for (DeXuatPhatSinh d : dao.docDanhSach(phien, maLenh)) {
                cmbDanhSach.addItem(d);
                if (ma != null && d.getMaPhatSinh() == ma) chon = d;
            }
            if (chon != null) cmbDanhSach.setSelectedItem(chon);
        } finally {
            dangNapDanhSach = false;
        }
        hienThiDeXuat((DeXuatPhatSinh) cmbDanhSach.getSelectedItem());
    }

    // Hiển thị nội dung, phụ lục giá và phản hồi của đề xuất đang chọn.
    private void hienThiDeXuat(DeXuatPhatSinh deXuat) {
        dangXem = deXuat;
        banNhap.clear();
        if (dangXem != null) banNhap.addAll(dangXem.getChiTiet());
        hienGia();
        boolean duocSua = dangXem != null && "CHO_KHACH".equals(dangXem.getTrangThai())
                && phien.getVaiTro() == VaiTro.NHAN_VIEN_TIEP_NHAN;
        for (JButton b : new JButton[] {btnThem,btnSua,btnXoa,btnLuu,btnTuChoi,btnDongY}) b.setEnabled(duocSua);
        for (JTextField f : new JTextField[] {txtPhut,txtKhach,txtKenh,txtLuc,txtGhiChu}) f.setEditable(duocSua);
        txtNoiDung.setText(dangXem == null ? "Lệnh chưa có phát sinh." : "Phát sinh #" + dangXem.getSoThuTu()
                + " · " + dangXem.getTenTrangThai() + "\nHư hỏng: " + dangXem.getHuHong()
                + "\nMô tả/nguyên nhân: " + dangXem.getNguyenNhan() + "\nĐề xuất: " + dangXem.getDeXuat()
                + "\nNgười báo: " + dangXem.getNguoiBao() + " · " + dangXem.getThoiDiemBao()
                + "\nNgười ghi nhận phản hồi: " + (dangXem.getNguoiGhiNhan() == null ? "—" : dangXem.getNguoiGhiNhan()));
        txtPhut.setText(dangXem == null || dangXem.getThoiGianDuKienPhut() == null ? "0"
                : dangXem.getThoiGianDuKienPhut().toString());
        txtKhach.setText(dangXem == null || dangXem.getTenKhach() == null ? "" : dangXem.getTenKhach());
        txtKenh.setText(dangXem == null || dangXem.getKenh() == null ? "Trực tiếp" : dangXem.getKenh());
        txtGhiChu.setText(dangXem == null || dangXem.getGhiChu() == null ? "" : dangXem.getGhiChu());
        txtLuc.setText(dangXem == null || dangXem.getThoiDiemPhanHoi() == null
                ? LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).format(NGAY) : dangXem.getThoiDiemPhanHoi().format(NGAY));
        daSua = false;
    }

    // Hiển thị snapshot từng dòng giá; không tự lấy giá mới khi đã có quyết định khách.
    private void hienGia() {
        model.setRowCount(0);
        long tong = 0;
        for (ChiTietBaoGia d : banNhap) {
            model.addRow(new Object[] {d.getTenHangMuc(),d.getLoai(),d.getSoLuong() + " " + d.getDonVi(),
                    d.getDonGia(),d.getThanhTien()});
            tong = Math.addExact(tong, d.getThanhTien());
        }
        lblTong.setText("Tổng phụ lục: " + String.format("%,d", tong) + " đ");
    }

    // Mở lại form chọn dịch vụ/phụ tùng hiện có để thêm hoặc sửa một dòng phụ lục.
    private void suaDong(boolean sua) {
        int dong = tblGia.getSelectedRow();
        if (sua && dong < 0) {
            throw new IllegalArgumentException("Chọn dòng cần sửa.");
        }
        final int viTri = sua ? tblGia.convertRowIndexToModel(dong) : -1;
        HangMucBaoGiaDialog hop = new HangMucBaoGiaDialog(this, sua ? banNhap.get(viTri) : null,
                new ActionListener() {
                    // Chỉ cập nhật bản nháp tại giao diện; chưa thay đổi dữ liệu SQL.
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ChiTietBaoGia moi = (ChiTietBaoGia) e.getSource();
                        if (viTri < 0) banNhap.add(moi);
                        else banNhap.set(viTri, moi);
                        daSua = true;
                        hienGia();
                    }
                }, new PhuTungSuDungDAO(false).docDanhMucDangHoatDong(), dao.docDichVu(phien));
        hop.setVisible(true);
    }

    // Kiểm tra phụ lục đang hiển thị trùng nội dung SQL trước khi ghi phản hồi khách.
    private void ghiPhanHoi(boolean dongY) {
        if (daSua || !txtPhut.getText().trim().equals(dangXem.getThoiGianDuKienPhut() == null ? "0"
                : dangXem.getThoiGianDuKienPhut().toString()))
            throw new IllegalStateException("Lưu phụ lục giá và số phút trước khi ghi phản hồi.");
        if (JOptionPane.showConfirmDialog(this, "Xác nhận khách " + (dongY ? "đồng ý" : "từ chối")
                + " phụ lục " + lblTong.getText() + "?", "Ghi nhận phản hồi", JOptionPane.YES_NO_OPTION)
                != JOptionPane.YES_OPTION) return;
        dao.phanHoi(phien, dangXem, dongY, txtKhach.getText(), txtKenh.getText(), txtGhiChu.getText(),
                LocalDateTime.parse(txtLuc.getText().trim(), NGAY));
        taiDanhSach(dangXem.getMaPhatSinh());
        lblThongBao.setText(dongY ? "Đã ghi đồng ý và thêm công việc cho kỹ thuật chính." : "Đã ghi từ chối; không thêm công việc.");
    }

    // Đóng dialog sau khi xác nhận bỏ bản nháp nếu cần.
    private void dong() {
        if (!daSua || JOptionPane.showConfirmDialog(this, "Bỏ phụ lục chưa lưu?", "Đóng",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) dispose();
    }

    // Xử lý các thao tác; DAO tiếp tục kiểm tra quyền và trạng thái khi lưu.
    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            String lenh = e.getActionCommand();
            if ("DONG".equals(lenh)) dong();
            else if ("MO".equals(lenh)) {
                if (daSua && JOptionPane.showConfirmDialog(this, "Bỏ phụ lục đang sửa để tải lại?", "Tải lại",
                        JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
                DeXuatPhatSinh chon = (DeXuatPhatSinh) cmbDanhSach.getSelectedItem();
                taiDanhSach(chon == null ? null : chon.getMaPhatSinh());
            } else if ("THEM".equals(lenh)) suaDong(false);
            else if ("SUA".equals(lenh)) suaDong(true);
            else if ("XOA".equals(lenh)) {
                int dong = tblGia.getSelectedRow();
                if (dong < 0) {
                    throw new IllegalArgumentException("Chọn dòng cần xóa.");
                }
                banNhap.remove(tblGia.convertRowIndexToModel(dong));
                daSua = true;
                hienGia();
            } else if ("LUU".equals(lenh)) {
                dao.luuGia(phien, dangXem, banNhap, Integer.parseInt(txtPhut.getText().trim()));
                taiDanhSach(dangXem.getMaPhatSinh());
                lblThongBao.setText("Đã lưu phụ lục. Báo khách đúng nội dung và tổng tiền này trước khi ghi phản hồi.");
            } else if ("DONG_Y".equals(lenh)) ghiPhanHoi(true);
            else if ("TU_CHOI".equals(lenh)) ghiPhanHoi(false);
        } catch (RuntimeException ex) {
            lblThongBao.setForeground(new Color(0xB42318));
            lblThongBao.setText(ex.getMessage());
        }
    }
}
