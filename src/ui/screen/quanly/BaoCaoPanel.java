package ui.screen.quanly;

import connectDB.ConnectDB;
import dao.BaoCaoDAO;
import entity.BaoCao;
import entity.TaiKhoan;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ui.component.UiChung;
import ui.component.UiTheme;

/** Một màn báo cáo dùng chung bộ lọc, tách rõ từng loại kết quả. */
public final class BaoCaoPanel extends JPanel implements ActionListener, DocumentListener {
    private static final long serialVersionUID = 1L;
    private final TaiKhoan taiKhoan;
    private final BaoCaoDAO dao;
    private final JComboBox<String> cmbLoai = new JComboBox<>(
            new String[] { BaoCao.DOANH_THU, BaoCao.TIEP_NHAN, BaoCao.TON_KHO, BaoCao.HIEU_SUAT });
    private final JTextField txtTuNgay = UiChung.taoONhap(true);
    private final JTextField txtDenNgay = UiChung.taoONhap(true);
    private final JTextField txtTim = UiChung.taoONhap(true);
    private final JComboBox<String> cmbTon = new JComboBox<>(new String[] { "Tất cả kết quả", "Cảnh báo", "Đủ tồn" });
    private final JPanel pnlKetQua = new JPanel(new BorderLayout());
    private final JLabel lblThongBao = UiChung.taoNhan("", 12, false, UiTheme.APP_MUTED);
    private final JButton btnLuu = UiChung.taoNut("Lưu báo cáo", false);
    private BaoCao ketQua;
    private boolean dangLuu;
    private JScrollPane scrTrang;
    private String tuNgayTruocTon;
    private String denNgayTruocTon;
    private JTable tblKetQua;
    private List<String[]> dongHienThi = new ArrayList<>();
    private final JButton btnXuat = UiChung.taoNut("Xuất CSV", true);
    private final JPanel pnlDanhSach = new JPanel(new BorderLayout(0, 8));
    private final DateTimeFormatter ngay = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    /** Dựng form chọn loại/khoảng ngày và đọc số liệu tháng hiện tại. */
    public BaoCaoPanel(TaiKhoan taiKhoan, BaoCaoDAO dao) {
        super(new BorderLayout());
        this.taiKhoan = taiKhoan;
        this.dao = dao;
        dao.kiemTraQuyen(taiKhoan);
        setOpaque(false);
        txtTuNgay.setText(LocalDate.now().withDayOfMonth(1).format(ngay));
        txtDenNgay.setText(LocalDate.now().format(ngay));
        JPanel pnl = BaoCaoUi.taoCot();
        BaoCaoUi.them(pnl, UiChung.taoDauTrang("Quản lý / Báo cáo / Báo cáo và thống kê", "Báo cáo và thống kê",
                "Thống kê doanh thu, tồn kho và tình hình sửa chữa."));
        JPanel dieuHuong = new JPanel(new GridLayout(1, 3, 8, 8));
        dieuHuong.setOpaque(false);
        dieuHuong.add(nut("Báo cáo đã lưu", "daLuu", false));
        dieuHuong.add(nut("Báo cáo tồn kho", "tonKho", false));
        dieuHuong.add(nut("Tiếp nhận và sửa chữa", "suaChua", false));
        BaoCaoUi.them(pnl, dieuHuong);

        // Bộ lọc chung; tồn kho không dùng khoảng ngày lịch sử.
        JPanel loc = BaoCaoUi.taoCot();
        UiChung.dinhDangO(cmbLoai);
        JPanel cacO = new JPanel(new GridLayout(1, 3, 12, 0));
        cacO.setOpaque(false);
        cacO.add(UiChung.taoTruong("Loại báo cáo", cmbLoai));
        cacO.add(UiChung.taoTruong("Từ ngày (dd/MM/yyyy)", txtTuNgay));
        cacO.add(UiChung.taoTruong("Đến ngày (dd/MM/yyyy)", txtDenNgay));
        BaoCaoUi.them(loc, cacO);
        BaoCaoUi.them(loc, UiChung.taoHangNut(nut("Thống kê", "thongKe", true), nut("Làm mới", "lamMoi", false)));
        loc.add(lblThongBao);
        BaoCaoUi.them(pnl, BaoCaoUi.taoThe("Điều kiện thống kê", loc));
        pnlKetQua.setOpaque(false);
        pnlKetQua.setAlignmentX(LEFT_ALIGNMENT);
        pnl.add(pnlKetQua);

        // Các tác vụ lưu bản chụp và phản hồi nằm sau kết quả.
        btnLuu.setActionCommand("luu");
        btnLuu.addActionListener(this);
        btnLuu.setToolTipText("Lưu toàn bộ kết quả thống kê đang xem");
        JPanel chan = BaoCaoUi.taoCot();
        btnXuat.setActionCommand("xuat");
        btnXuat.addActionListener(this);
        BaoCaoUi.them(chan, UiChung.taoHangNut(btnLuu, btnXuat));
        BaoCaoUi.them(pnl, chan);
        scrTrang = UiChung.taoVungCuon(pnl);
        add(scrTrang);
        cmbLoai.setActionCommand("loai");
        cmbLoai.addActionListener(this);
        cmbTon.setActionCommand("locBang");
        cmbTon.addActionListener(this);
        txtTuNgay.setActionCommand("thongKe");
        txtTuNgay.addActionListener(this);
        txtDenNgay.setActionCommand("thongKe");
        txtDenNgay.addActionListener(this);
        txtTuNgay.getDocument().addDocumentListener(this);
        txtDenNgay.getDocument().addDocumentListener(this);
        txtTim.getDocument().addDocumentListener(this);
        thongKe();
    }

    /** Tạo nút và gắn thao tác theo mã, không dùng lambda. */
    private JButton nut(String ten, String ma, boolean chinh) {
        JButton btn = UiChung.taoNut(ten, chinh);
        btn.setActionCommand(ma);
        btn.addActionListener(this);
        return btn;
    }

    /** Đọc ngày thật, từ chối ngày không tồn tại hoặc sai định dạng. */
    private LocalDate docNgay(JTextField txt) {
        try {
            return LocalDate.parse(txt.getText().trim(), ngay);
        } catch (RuntimeException ex) {
            txt.requestFocusInWindow();
            throw new IllegalArgumentException("Ngày không hợp lệ. Nhập dd/MM/yyyy, ví dụ 02/11/2026.");
        }
    }

    /** Lập lại kết quả từ DAO, chỉ cho lưu sau khi điều kiện hợp lệ. */
    private void thongKe() {
        try {
            boolean tonKho = BaoCao.TON_KHO.equals(cmbLoai.getSelectedItem());
            if (tonKho) {
                if (tuNgayTruocTon == null) {
                    tuNgayTruocTon = txtTuNgay.getText();
                    denNgayTruocTon = txtDenNgay.getText();
                }
                txtTuNgay.setText(LocalDate.now().format(ngay));
                txtDenNgay.setText(LocalDate.now().format(ngay));
            } else if (tuNgayTruocTon != null) {
                txtTuNgay.setText(tuNgayTruocTon);
                txtDenNgay.setText(denNgayTruocTon);
                tuNgayTruocTon = null;
                denNgayTruocTon = null;
            }
            txtTuNgay.setEnabled(!tonKho);
            txtDenNgay.setEnabled(!tonKho);
            LocalDate tu = tonKho ? LocalDate.now() : docNgay(txtTuNgay);
            LocalDate den = tonKho ? LocalDate.now() : docNgay(txtDenNgay);
            ketQua = dao.lap(taiKhoan, (String) cmbLoai.getSelectedItem(), tu, den);
            btnLuu.setEnabled(!dangLuu);
            btnXuat.setEnabled(true);
            hienKetQua();
            scrTrang.getVerticalScrollBar().setValue(0);
            thongBao("Đã thống kê · " + BaoCaoDAO.khoangNgay(ketQua), false);
        } catch (RuntimeException ex) {
            ketQua = null;
            btnLuu.setEnabled(false);
            btnXuat.setEnabled(false);
            pnlKetQua.removeAll();
            pnlKetQua.revalidate();
            pnlKetQua.repaint();
            thongBao(ex.getMessage(), true);
        }
    }

    /** Mô tả đúng ngày gốc và cách đếm của từng loại thống kê. */
    private String moTaKetQua() {
        if (BaoCao.TON_KHO.equals(ketQua.getLoai())) {
            return "Tồn hiện tại tại " + ketQua.getNgayLap().format(DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm"))
                    + ". Gồm cả mã ngừng hoạt động; cảnh báo khi tồn ≤ ngưỡng. Không phải báo cáo tồn lịch sử.";
        }
        if (BaoCao.TIEP_NHAN.equals(ketQua.getLoai())) {
            return "Lượt/xe theo ngày tiếp nhận; lệnh theo ngày phát hành; báo giá chờ theo ngày gửi."
                    + " Trạng thái là hiện tại lúc lập, các nhóm đếm có thể giao nhau.";
        }
        if (BaoCao.HIEU_SUAT.equals(ketQua.getLoai())) {
            return "Lệnh phát hành trong kỳ, trạng thái hiện tại. Mỗi lệnh tính cho cả người chính và hỗ trợ."
                    + " Tỷ lệ = lệnh hoàn thành / được giao; không có lệnh thì không tính tỷ lệ.";
        }
        return "Chỉ hóa đơn đã thanh toán trong kỳ, gồm cả hai ngày đầu/cuối. Tổng không gồm tiền thừa trả khách.";
    }

    /** Vẽ chỉ số và danh sách; tìm trong bảng không thay đổi bản chụp đã thống kê. */
    private void hienKetQua() {
        if (ketQua == null) {
            return;
        }
        JPanel pnl = BaoCaoUi.taoCot();
        BaoCaoUi.them(pnl,
                BaoCaoUi.taoThe(ketQua.getLoai() + " · " + BaoCaoDAO.khoangNgay(ketQua), BaoCaoUi.taoChiSo(ketQua)));
        JPanel bang = BaoCaoUi.taoCot();
        JPanel loc = new JPanel(new BorderLayout(10, 0));
        loc.setOpaque(false);
        txtTim.setToolTipText("Tìm trong các dòng kết quả đang xem");
        loc.add(UiChung.taoTruong("Tìm trong kết quả", txtTim));
        if (BaoCao.TON_KHO.equals(ketQua.getLoai())) {
            UiChung.dinhDangO(cmbTon);
            cmbTon.setPreferredSize(new Dimension(180, 40));
            loc.add(cmbTon, BorderLayout.EAST);
        }
        BaoCaoUi.them(bang, loc);
        pnlDanhSach.setOpaque(false);
        BaoCaoUi.them(bang, pnlDanhSach);
        if (BaoCao.DOANH_THU.equals(ketQua.getLoai())) {
            BaoCaoUi.them(bang, UiChung.taoHangNut(nut("Xem hóa đơn", "hoaDon", false)));
        }
        locBang();
        BaoCaoUi.them(bang, BaoCaoUi.taoThe("Cách đọc số liệu", BaoCaoUi.moTa(moTaKetQua())));
        BaoCaoUi.them(pnl, BaoCaoUi.taoThe("Danh sách kết quả", bang));
        pnlKetQua.removeAll();
        pnlKetQua.add(pnl);
        pnlKetQua.revalidate();
        pnlKetQua.repaint();
    }

    /** Chỉ thay bảng khi nhập tìm kiếm, giữ con trỏ trong ô đang gõ. */
    private void locBang() {
        if (ketQua == null) {
            return;
        }
        List<String[]> tatCa = ketQua.getCacDong();
        List<String[]> hienThi = new ArrayList<>();
        String khoa = txtTim.getText().trim().toLowerCase();
        for (String[] dong : tatCa) {
            boolean dungChu = false;
            for (String chu : dong) {
                if (chu.toLowerCase().contains(khoa)) {
                    dungChu = true;
                    break;
                }
            }
            boolean dungTon = !BaoCao.TON_KHO.equals(ketQua.getLoai()) || cmbTon.getSelectedIndex() == 0
                    || dong[5].equals(cmbTon.getSelectedItem());
            if (dungChu && dungTon) {
                hienThi.add(dong);
            }
        }
        pnlDanhSach.removeAll();
        dongHienThi = hienThi;
        tblKetQua = BaoCaoUi.taoBang(ketQua.getTenCot(), hienThi);
        pnlDanhSach.add(BaoCaoUi.cuonBang(tblKetQua));
        pnlDanhSach.add(UiChung.taoNhan("Hiển thị " + hienThi.size() + " / " + tatCa.size() + " dòng", 12, false,
                UiTheme.APP_MUTED), BorderLayout.SOUTH);
        pnlDanhSach.revalidate();
        pnlDanhSach.repaint();
    }

    /** Đọc lại dữ liệu khi trở về màn báo cáo và giữ điều kiện đã chọn. */
    public void taiDuLieu() {
        thongKe();
    }

    /** Thông báo thành công hoặc lỗi, giữ nguyên đầu vào khi sai. */
    private void thongBao(String chu, boolean loi) {
        lblThongBao.setForeground(loi ? UiTheme.ERROR : UiTheme.APP_MUTED);
        lblThongBao.setText(chu);
        lblThongBao.setToolTipText(chu);
    }

    /** Lưu bản chụp được chọn ở luồng nền và trả kết quả về Swing. */
    private void luuBaoCao() {
        if (dangLuu || ketQua == null) {
            return;
        }
        BaoCao banChup = ketQua;
        dangLuu = true;
        btnLuu.setEnabled(false);
        btnLuu.setText("Đang lưu…");
        thongBao("Đang lưu báo cáo " + banChup.getLoai() + "…", false);

        new SwingWorker<BaoCao, Void>() {
            @Override
            protected BaoCao doInBackground() {
                try {
                    return dao.luu(taiKhoan, banChup);
                } finally {
                    ConnectDB.getInstance().dongKetNoiHienTai();
                }
            }

            @Override
            protected void done() {
                try {
                    BaoCao baoCaoDaLuu = get();
                    thongBao("Đã lưu " + baoCaoDaLuu.getMaBaoCao(), false);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    thongBao("Không nhận được kết quả lưu báo cáo.", true);
                } catch (ExecutionException ex) {
                    Throwable loi = ex.getCause();
                    String noiDung = loi == null ? null : loi.getMessage();
                    thongBao(noiDung == null ? "Không thể lưu báo cáo." : noiDung, true);
                } finally {
                    dangLuu = false;
                    btnLuu.setText("Lưu báo cáo");
                    btnLuu.setEnabled(ketQua != null);
                }
            }
        }.execute();
    }

    /** Điều khiển bộ lọc, lưu bản chụp và mở danh sách đã lưu. */
    @Override
    public void actionPerformed(ActionEvent event) {
        String ma = event.getActionCommand();
        if (ma.equals("daLuu")) {
            BaoCaoDaLuuPanel panel = new BaoCaoDaLuuPanel(taiKhoan, dao);
            BaoCaoUi.hienChiTiet(this, "Báo cáo đã lưu", panel);
        } else if (ma.equals("tonKho")) {
            cmbLoai.setSelectedItem(BaoCao.TON_KHO);
        } else if (ma.equals("suaChua")) {
            cmbLoai.setSelectedItem(BaoCao.TIEP_NHAN);
        } else if (ma.equals("hieuSuat")) {
            cmbLoai.setSelectedItem(BaoCao.HIEU_SUAT);
        } else if (ma.equals("xuat")) {
            BaoCaoUi.xuatCsv(this, taiKhoan, dao, ketQua);
        } else if (ma.equals("hoaDon")) {
            try {
                if (ketQua == null) {
                    throw new IllegalArgumentException("Hãy thống kê lại trước khi xem hóa đơn.");
                }
                int i = tblKetQua.getSelectedRow();
                if (i < 0) {
                    throw new IllegalArgumentException("Chọn một hóa đơn trong bảng.");
                }
                BaoCaoUi.hienHoaDon(this, dao.timHoaDon(taiKhoan, dongHienThi.get(i)[0]));
            } catch (RuntimeException ex) {
                thongBao(ex.getMessage(), true);
            }
        } else if (ma.equals("luu")) {
            luuBaoCao();
        } else if (ma.equals("locBang")) {
            locBang();
        } else {
            if (ma.equals("lamMoi")) {
                txtTim.setText("");
                cmbTon.setSelectedIndex(0);
            }
            thongKe();
        }
    }

    /** Điều kiện ngày thay đổi phải thống kê lại; tìm trong bảng lọc ngay. */
    private void khiNhap(DocumentEvent event) {
        if (event.getDocument() == txtTim.getDocument()) {
            locBang();
        } else {
            ketQua = null;
            btnLuu.setEnabled(false);
            btnXuat.setEnabled(false);
            thongBao("Điều kiện đã thay đổi. Bấm Thống kê để xem và lưu kết quả mới.", false);
        }
    }

    /** Nhận ký tự vừa thêm. */
    @Override
    public void insertUpdate(DocumentEvent e) {
        khiNhap(e);
    }

    /** Nhận ký tự vừa xóa. */
    @Override
    public void removeUpdate(DocumentEvent e) {
        khiNhap(e);
    }

    /** Nhận thay đổi định dạng tài liệu nhập. */
    @Override
    public void changedUpdate(DocumentEvent e) {
        khiNhap(e);
    }
}
