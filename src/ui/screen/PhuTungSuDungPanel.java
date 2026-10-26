package ui.screen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import dao.LenhPhuTungDAO;
import dao.PhuTungSuDungDAO;
import entity.LenhPhuTung;
import entity.PhuTung;
import entity.PhuTungSuDung;
import entity.TaiKhoan;
import entity.TrangThaiSuDung;
import ui.component.PhuTungUi;
import ui.component.UiTheme;
import ui.dialog.PhuTungSuDungDialog;

public final class PhuTungSuDungPanel extends JPanel
        implements ActionListener, DocumentListener, ListSelectionListener {
    private static final long serialVersionUID = 1L;
    private final TaiKhoan taiKhoan;
    private final PhuTungSuDungDAO dao;
    private final LenhPhuTungDAO lenhDAO;
    private final DefaultTableModel modelDanhSach = PhuTungUi.taoModel(
            "Số ghi nhận", "Lệnh sửa chữa", "Phụ tùng", "Số lượng", "Trạng thái");
    private final JTable tblDanhSach = PhuTungUi.taoBang(modelDanhSach);
    private final List<PhuTungSuDung> cacDong = new ArrayList<PhuTungSuDung>();
    private final JTextField txtTimKiem = new JTextField();
    private final JComboBox<String> cmbTrangThai = new JComboBox<String>(
            new String[] {"Tất cả", "Nháp", "Đã ghi nhận"});
    private final JLabel lblTrangThai = PhuTungUi.taoNhan(" ", false);
    private final Map<String, JTextField> cacLuong = new LinkedHashMap<String, JTextField>();
    private final Map<String, JTextArea> cacTonDuKien = new LinkedHashMap<String, JTextArea>();
    private final JTextArea txaGhiChu = new JTextArea(3, 24);
    private final JComboBox<String> cmbTienDo = new JComboBox<String>(
            new String[] {"Đang sửa chữa", "Chờ phụ tùng", "Hoàn tất"});
    private final JTextField txtKetQuaCongViec = new JTextField();
    private final JTextField[] cacTruongPhatSinh = {
            new JTextField(), new JTextField(), new JTextField(), new JTextField()
    };
    private final JTextArea txaGhiChuPhatSinh = new JTextArea(3, 24);
    private final JCheckBox[] cacKiemTra = new JCheckBox[4];
    private final JTextField txtKetLuan = new JTextField();
    private final JTextField txtBaoHanh = new JTextField();
    private final JTextField txtKhuyenNghi = new JTextField();
    private PhuTungSuDung nhapDangXacNhan;
    private String maLenhDangMo;
    private String tuKhoaDangLoc = "";
    private TrangThaiSuDung trangThaiDangLoc;
    private String loiVuotTon;
    private boolean dangNap;

    /**
     * Tạo các màn phụ tùng cho phiên kỹ thuật.
     * @param taiKhoan tài khoản đã đăng nhập
     * @param dao DAO phụ tùng dùng chung trong phiên
     */
    public PhuTungSuDungPanel(TaiKhoan taiKhoan, PhuTungSuDungDAO dao) {
        super(new BorderLayout());
        this.taiKhoan = taiKhoan;
        this.dao = dao;
        lenhDAO = new LenhPhuTungDAO(dao, taiKhoan);
        setBackground(UiTheme.WINDOW);
        PhuTungUi.dinhDangCombo(cmbTrangThai);
        PhuTungUi.dinhDangCombo(cmbTienDo);
        tblDanhSach.getSelectionModel().addListSelectionListener(this);
        dinhDangVungNhap(txaGhiChu);
        dinhDangVungNhap(txaGhiChuPhatSinh);
        moDanhSach();
    }

    // Xóa bộ lọc và tải dữ liệu mới nhất khi chọn mục phụ tùng sử dụng.
    public void moDanhSach() {
        tuKhoaDangLoc = "";
        trangThaiDangLoc = null;
        hienDanhSach();
    }

    /**
     * Mở lệnh hiện tại từ sidebar.
     */
    // Dùng lệnh đang làm hoặc lệnh của dòng được chọn để mở chi tiết tiến độ.
    public void moLenhDangChon() {
        if (maLenhDangMo == null) {
            if (!cacDong.isEmpty()) {
                maLenhDangMo = docDongChon().getMaLenhSuaChua();
            } else {
                List<String> cacMaLenh = dao.docLenhDaDuyet(taiKhoan);
                if (cacMaLenh.isEmpty()) {
                    thongBao("Lệnh sửa chữa", "Chưa có lệnh được phân công.");
                    return;
                }
                maLenhDangMo = cacMaLenh.get(0);
            }
        }
        hienChiTietLenh();
    }

    // Điều phối thao tác giữa danh sách, biểu mẫu và các màn của cùng một lệnh.
    @Override
    public void actionPerformed(ActionEvent suKien) {
        try {
            switch (suKien.getActionCommand()) {
                case "TIM":
                    hienTimKiem();
                    break;
                case "LOC":
                    timKiem();
                    break;
                case "DANH_SACH":
                    moDanhSach();
                    break;
                case "THEM":
                    moBieuMau(null, false, false);
                    break;
                case "XEM":
                    moBieuMau(docDongChon(), true, false);
                    break;
                case "SUA":
                    PhuTungSuDung banSua = docDongChon();
                    kiemTraCoTheSua(banSua);
                    moBieuMau(banSua, false, false);
                    break;
                case "XOA":
                    xoaHoSo(docDongChon());
                    break;
                case "LIEN_QUAN":
                    moBieuMau(docDongChon(), true, true);
                    break;
                case "MO_XAC_NHAN":
                    batDauXacNhan();
                    break;
                case "DA_DUYET":
                    hienDaDuyet();
                    break;
                case "NHAP_LUONG":
                    hienNhapLuong();
                    break;
                case "XAC_NHAN":
                    xacNhanThucDung();
                    break;
                case "VE_LENH":
                    hienChiTietLenh();
                    break;
                case "CHO_PHU_TUNG":
                    lenhDAO.chuyenChoPhuTung(taiKhoan, maLenhDangMo, nhapDangXacNhan);
                    hienChiTietLenh();
                    break;
                case "GHI_NHAN_TU_LENH":
                    batDauTuLenh();
                    break;
                case "CAP_NHAT_CONG_VIEC":
                    hienCapNhatCongViec();
                    break;
                case "LUU_TIEN_DO":
                    lenhDAO.capNhatTienDo(taiKhoan, maLenhDangMo,
                            (String) cmbTienDo.getSelectedItem(), txtKetQuaCongViec.getText());
                    hienChiTietLenh();
                    break;
                case "BAO_PHAT_SINH":
                    hienBaoPhatSinh();
                    break;
                case "LUU_PHAT_SINH":
                    luuPhatSinh();
                    break;
                case "KIEM_TRA_HOAN_TAT":
                    hienKiemTraHoanTat(true);
                    break;
                case "HOAN_TAT":
                    hoanTatLenh();
                    break;
                default:
                    throw new IllegalArgumentException("Thao tác không hợp lệ.");
            }
        } catch (IllegalArgumentException | IllegalStateException | SecurityException loi) {
            thongBao("Không thể thực hiện", loi.getMessage());
        }
    }

    // Cập nhật mã hồ sơ đang chọn bên dưới danh sách.
    @Override
    public void valueChanged(ListSelectionEvent suKien) {
        if (!suKien.getValueIsAdjusting()) {
            capNhatTrangThai();
        }
    }

    // Tính lại tồn dự kiến khi nhập thêm ký tự.
    @Override
    public void insertUpdate(DocumentEvent suKien) {
        capNhatDuKien();
    }

    // Tính lại tồn dự kiến khi xóa ký tự.
    @Override
    public void removeUpdate(DocumentEvent suKien) {
        capNhatDuKien();
    }

    // Tính lại tồn dự kiến khi thuộc tính tài liệu thay đổi.
    @Override
    public void changedUpdate(DocumentEvent suKien) {
        capNhatDuKien();
    }
    // Hiển thị thanh công cụ căn trái và bảng cao tối đa 400 px theo thiết kế.
    private void hienDanhSach() {
        JPanel pnlTrang = taoTrang("Danh sách phụ tùng sử dụng", null);
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlNut.setOpaque(false);
        String[] nhan = {"Tìm kiếm", "Thêm", "Xem", "Sửa", "Xóa", "Dữ liệu liên quan", "Xác nhận thực dùng"};
        String[] lenh = {"TIM", "THEM", "XEM", "SUA", "XOA", "LIEN_QUAN", "MO_XAC_NHAN"};
        for (int viTri = 0; viTri < nhan.length; viTri++) {
            JButton btnNut = PhuTungUi.taoNut(nhan[viTri], viTri == 1, viTri == 4);
            if (viTri == 0) {
                btnNut.setPreferredSize(new Dimension(142, 32));
                btnNut.setMinimumSize(new Dimension(142, 32));
            }
            btnNut.setActionCommand(lenh[viTri]);
            btnNut.addActionListener(this);
            pnlNut.add(btnNut);
        }
        JPanel pnlThanhCongCu = new JPanel(new BorderLayout());
        pnlThanhCongCu.setOpaque(false);
        pnlThanhCongCu.setBorder(BorderFactory.createEmptyBorder(0, -8, 0, 0));
        pnlThanhCongCu.add(pnlNut, BorderLayout.CENTER);
        cacDong.clear();
        cacDong.addAll(dao.docDanhSach(taiKhoan, tuKhoaDangLoc, trangThaiDangLoc));
        modelDanhSach.setRowCount(0);
        for (PhuTungSuDung banGhi : cacDong) {
            modelDanhSach.addRow(new Object[] {
                    banGhi.getMaGhiNhan(), banGhi.getMaLenhSuaChua(), banGhi.getPhuTung(),
                    PhuTungUi.dinhDangLuong(banGhi.getSoLuong()) + " " + banGhi.getPhuTung().getDonViTinh(),
                    banGhi.getTrangThai().getTenHienThi()
            });
        }
        if (!cacDong.isEmpty()) {
            tblDanhSach.setRowSelectionInterval(0, 0);
        }
        JScrollPane scrBang = new JScrollPane(tblDanhSach);
        scrBang.setPreferredSize(new Dimension(0, 400));
        scrBang.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        PhuTungUi.dinhDangVungCuon(scrBang);
        JTextArea txaQuyTac = PhuTungUi.taoMoTa(PhuTungUi.QUY_TAC);
        txaQuyTac.setFont(UiTheme.CAPTION_FONT.deriveFont(11.0f));
        txaQuyTac.setForeground(UiTheme.MUTED);
        JPanel pnlChuThich = new JPanel(new BorderLayout(0, 12));
        pnlChuThich.setOpaque(false);
        pnlChuThich.add(txaQuyTac, BorderLayout.NORTH);
        pnlChuThich.add(lblTrangThai, BorderLayout.SOUTH);
        JPanel pnlBangVaChuThich = new JPanel(new BorderLayout(0, 10));
        pnlBangVaChuThich.setOpaque(false);
        pnlBangVaChuThich.add(scrBang, BorderLayout.CENTER);
        pnlBangVaChuThich.add(pnlChuThich, BorderLayout.SOUTH);
        JPanel pnlCanDinh = new JPanel(new BorderLayout());
        pnlCanDinh.setOpaque(false);
        pnlCanDinh.add(pnlBangVaChuThich, BorderLayout.NORTH);
        JPanel pnlGiua = new JPanel(new BorderLayout(0, 10)) {
            private static final long serialVersionUID = 1L;

            // Giảm chiều cao bảng khi cần để phần hướng dẫn và số hồ sơ không bị khuất.
            @Override
            public void doLayout() {
                int chieuCaoCon = getHeight() - pnlThanhCongCu.getPreferredSize().height
                        - pnlChuThich.getPreferredSize().height - 20;
                int chieuCaoBang = Math.max(90, Math.min(400, chieuCaoCon));
                scrBang.setPreferredSize(new Dimension(0, chieuCaoBang));
                super.doLayout();
            }
        };
        pnlGiua.setOpaque(false);
        pnlGiua.add(pnlThanhCongCu, BorderLayout.NORTH);
        pnlGiua.add(pnlCanDinh, BorderLayout.CENTER);
        pnlTrang.add(pnlGiua, BorderLayout.CENTER);
        capNhatTrangThai();
        lblTrangThai.setFont(UiTheme.SHELL_FONT.deriveFont(13.0f));
        doiTrang(pnlTrang);
    }

    // Ghi số hồ sơ và mã dòng đang được chọn theo model bảng.
    private void capNhatTrangThai() {
        int dong = tblDanhSach.getSelectedRow();
        int dongModel = dong < 0 ? -1 : tblDanhSach.convertRowIndexToModel(dong);
        String daChon = dongModel >= 0 && dongModel < cacDong.size()
                ? "Đã chọn " + cacDong.get(dongModel).getMaGhiNhan() + " · " : "";
        lblTrangThai.setText(daChon + cacDong.size() + " hồ sơ");
        lblTrangThai.setFont(UiTheme.CAPTION_FONT);
        lblTrangThai.setForeground(UiTheme.MUTED);
    }

    // Hiển thị tiêu chí tìm kiếm theo từ khóa và trạng thái.
    private void hienTimKiem() {
        JPanel pnlTrang = taoTrang("Tìm kiếm phụ tùng sử dụng", "Nhập tiêu chí để tìm hồ sơ phù hợp.");
        txtTimKiem.setText(tuKhoaDangLoc);
        cmbTrangThai.setSelectedIndex(trangThaiDangLoc == null ? 0
                : trangThaiDangLoc == TrangThaiSuDung.NHAP ? 1 : 2);
        JPanel pnlTruong = taoLuoi();
        themONhap(pnlTruong, "Số ghi nhận, lệnh sửa chữa hoặc mã phụ tùng", txtTimKiem);
        themONhap(pnlTruong, "Trạng thái ghi nhận", cmbTrangThai);
        JPanel pnlCot = taoCot();
        themThe(pnlCot, PhuTungUi.taoThe("Tiêu chí tìm kiếm", pnlTruong));
        themThe(pnlCot, PhuTungUi.taoThe("Hướng dẫn",
                PhuTungUi.taoMoTa("Có thể nhập một phần thông tin; để trống để lấy toàn bộ hồ sơ.")));
        pnlTrang.add(bocVungCuon(pnlCot), BorderLayout.CENTER);
        JPanel pnlNut = taoHangNut();
        themNut(pnlNut, "Về danh sách", "DANH_SACH", false, false);
        themNut(pnlNut, "Tìm kiếm", "LOC", true, false);
        pnlTrang.add(pnlNut, BorderLayout.SOUTH);
        doiTrang(pnlTrang);
    }

    // Áp dụng tiêu chí vào danh sách mà không thay đổi dữ liệu gốc.
    private void timKiem() {
        tuKhoaDangLoc = txtTimKiem.getText().trim();
        trangThaiDangLoc = cmbTrangThai.getSelectedIndex() == 0 ? null
                : cmbTrangThai.getSelectedIndex() == 1 ? TrangThaiSuDung.NHAP : TrangThaiSuDung.DA_GHI_NHAN;
        hienDanhSach();
    }

    // Lấy đúng entity của dòng đang chọn trên bảng.
    private PhuTungSuDung docDongChon() {
        int dong = tblDanhSach.getSelectedRow();
        if (dong < 0) {
            throw new IllegalArgumentException("Hãy chọn một hồ sơ trong danh sách.");
        }
        return cacDong.get(tblDanhSach.convertRowIndexToModel(dong));
    }

    // Chặn sửa, xóa hoặc xác nhận lại hồ sơ đã ghi nhận.
    private void kiemTraCoTheSua(PhuTungSuDung banGhi) {
        if (banGhi.getTrangThai() != TrangThaiSuDung.NHAP) {
            throw new IllegalStateException("Hồ sơ đã ghi nhận xuất kho; không được sửa, xóa hoặc xác nhận lại.");
        }
        if ("Hoàn thành".equals(lenhDAO.docLenh(taiKhoan, banGhi.getMaLenhSuaChua()).getTrangThai())) {
            throw new IllegalStateException("Lệnh đã hoàn thành.");
        }
    }

    // Dùng lại hộp thoại cũ cho thêm, sửa, xem và dữ liệu liên quan.
    private void moBieuMau(PhuTungSuDung banGhi, boolean chiDoc, boolean lienQuan) {
        PhuTungSuDungDialog winBieuMau = new PhuTungSuDungDialog(
                docCuaSo(), dao, taiKhoan, banGhi, chiDoc, lienQuan);
        winBieuMau.setVisible(true);
        if (winBieuMau.isYeuCauXoa()) {
            xoaHoSo(banGhi);
        } else if (winBieuMau.isDaLuu()) {
            hienDanhSach();
        }
    }

    // Xác nhận xóa nháp bằng nút chữ đỏ và viền đỏ của component chung.
    private void xoaHoSo(PhuTungSuDung banGhi) {
        kiemTraCoTheSua(banGhi);
        if (PhuTungUi.hienHopThoai(docCuaSo(), "Xác nhận xóa",
                "Xóa " + banGhi.getMaGhiNhan() + " — " + banGhi.getMaLenhSuaChua()
                        + "?\n\nHồ sơ chưa ghi nhận xuất kho. " + PhuTungUi.QUY_TAC,
                "Xóa", true, true)) {
            dao.xoaNhap(taiKhoan, banGhi);
            hienDanhSach();
        }
    }

    // Khởi tạo xác nhận thực dùng từ bản nháp đang chọn.
    private void batDauXacNhan() {
        nhapDangXacNhan = docDongChon();
        kiemTraCoTheSua(nhapDangXacNhan);
        maLenhDangMo = nhapDangXacNhan.getMaLenhSuaChua();
        napLuongThucDung();
        hienNhapLuong();
    }

    // Mở phạm vi được duyệt từ màn lệnh mà không tự trừ kho hoặc tự lưu nháp.
    private void batDauTuLenh() {
        LenhPhuTung lenh = lenhDAO.docLenh(taiKhoan, maLenhDangMo);
        if ("Hoàn thành".equals(lenh.getTrangThai())) {
            throw new IllegalStateException("Lệnh đã hoàn thành.");
        }
        nhapDangXacNhan = null;
        for (PhuTungSuDung nhap : dao.docDanhSach(taiKhoan, maLenhDangMo, TrangThaiSuDung.NHAP)) {
            if (maLenhDangMo.equals(nhap.getMaLenhSuaChua())) {
                nhapDangXacNhan = nhap;
                break;
            }
        }
        napLuongThucDung();
        hienDaDuyet();
    }

    // Nạp lượng còn được dùng và ưu tiên giá trị của bản nháp cùng lệnh.
    private void napLuongThucDung() {
        dangNap = true;
        cacTonDuKien.clear();
        cacLuong.clear();
        List<PhuTungSuDung> cacNhap = dao.docDanhSach(taiKhoan, maLenhDangMo, TrangThaiSuDung.NHAP);
        for (String maPhuTung : dao.docPhamViDuyet(taiKhoan, maLenhDangMo).keySet()) {
            BigDecimal soLuong = dao.docLuongConLai(taiKhoan, maLenhDangMo, maPhuTung);
            for (PhuTungSuDung nhap : cacNhap) {
                if (maLenhDangMo.equals(nhap.getMaLenhSuaChua())
                        && maPhuTung.equals(nhap.getPhuTung().getMaPhuTung())) {
                    soLuong = nhap.getSoLuong();
                    break;
                }
            }
            JTextField txtLuong = new JTextField(PhuTungUi.dinhDangLuong(soLuong));
            txtLuong.getDocument().addDocumentListener(this);
            cacLuong.put(maPhuTung, txtLuong);
        }
        txaGhiChu.setText(nhapDangXacNhan == null ? "" : nhapDangXacNhan.getGhiChu());
        dangNap = false;
    }

    // Hiển thị màn nhập thực dùng V1 hoặc V2 với tồn dự kiến cập nhật theo lượng nhập.
    private void hienNhapLuong() {
        LenhPhuTung lenh = lenhDAO.docLenh(taiKhoan, maLenhDangMo);
        JPanel pnlTrang = taoTrang(lenh.isBaoGiaV2()
                ? "Ghi nhận phụ tùng — báo giá V2" : "Ghi nhận phụ tùng đã dùng",
                maLenhDangMo + " · Kho chỉ trừ khi xác nhận lượng dùng thực tế.");
        JPanel pnlNhap = taoLuoi();
        for (Map.Entry<String, JTextField> dong : cacLuong.entrySet()) {
            PhuTung phuTung = dao.docPhuTung(dong.getKey());
            themONhap(pnlNhap, phuTung.toString() + " * (" + phuTung.getDonViTinh() + ")", dong.getValue());
        }
        themONhap(pnlNhap, "Người xác nhận", PhuTungUi.taoNhan(lenh.getKyThuatVien(), false));
        themONhap(pnlNhap, "Ghi chú", bocVungNhap(txaGhiChu));
        JPanel pnlCot = taoCot();
        themThe(pnlCot, PhuTungUi.taoThe("Lượng đã dùng thực tế", pnlNhap));
        JPanel pnlTon = taoLuoi();
        cacTonDuKien.clear();
        for (String maPhuTung : cacLuong.keySet()) {
            JTextArea txaTon = PhuTungUi.taoMoTa(" ");
            txaTon.setForeground(UiTheme.TEXT);
            cacTonDuKien.put(maPhuTung, txaTon);
            themONhap(pnlTon, maPhuTung, txaTon);
        }
        themThe(pnlCot, PhuTungUi.taoThe("Tồn kho sau khi ghi nhận", pnlTon));
        themThe(pnlCot, taoCanhBaoKhongXuatHaiLan());
        if (lenh.isBaoGiaV2()) {
            themThe(pnlCot, taoThongDiep("Kiểm tra trước khi ghi sổ",
                    "Chỉ ghi nhận phụ tùng thực dùng trong phạm vi báo giá V2 khách đã duyệt. "
                            + "Mỗi phiếu chỉ ghi nhận một lần; không trừ tồn khi mới báo giá.",
                    UiTheme.SURFACE));
        }
        pnlTrang.add(bocVungCuon(pnlCot), BorderLayout.CENTER);
        JPanel pnlNut = taoHangNut();
        themNut(pnlNut, "Quay lại lệnh", "VE_LENH", false, false);
        themNut(pnlNut, lenh.isBaoGiaV2() ? "Xác nhận thực dùng" : "Xác nhận phụ tùng đã dùng",
                "XAC_NHAN", true, false);
        themNut(pnlNut, "Xem phụ tùng đã duyệt", "DA_DUYET", false, false);
        pnlTrang.add(pnlNut, BorderLayout.SOUTH);
        doiTrang(pnlTrang);
        capNhatDuKien();
    }

    // Tính tồn sau ghi nhận để hiển thị, không cập nhật dữ liệu tồn kho.
    private void capNhatDuKien() {
        if (dangNap) {
            return;
        }
        for (Map.Entry<String, JTextArea> dong : cacTonDuKien.entrySet()) {
            try {
                BigDecimal luong = PhuTungUi.docSoLuong(cacLuong.get(dong.getKey()));
                if (luong.signum() < 0) {
                    throw new IllegalArgumentException("Số lượng không được âm.");
                }
                PhuTung phuTung = dao.docPhuTung(dong.getKey());
                BigDecimal ton = dao.docTon(dong.getKey());
                BigDecimal sau = ton.subtract(luong);
                String moTa = PhuTungUi.dinhDangLuong(ton) + " - " + PhuTungUi.dinhDangLuong(luong)
                        + " = " + PhuTungUi.dinhDangLuong(sau) + " " + phuTung.getDonViTinh();
                if (sau.signum() < 0) {
                    moTa += " · Vượt tồn";
                } else if (sau.compareTo(phuTung.getMucToiThieu()) < 0) {
                    moTa += " · Dưới mức tối thiểu " + PhuTungUi.dinhDangLuong(phuTung.getMucToiThieu());
                }
                dong.getValue().setText(moTa);
                dong.getValue().setForeground(sau.signum() < 0 ? PhuTungUi.MAU_NGUY_HIEM : UiTheme.TEXT);
            } catch (IllegalArgumentException loi) {
                dong.getValue().setText("Nhập số lượng hợp lệ để xem dự kiến.");
            }
        }
    }

    // Hiển thị danh sách được duyệt và trở lại đúng các ô nhập đang giữ trong phiên.
    private void hienDaDuyet() {
        JPanel pnlTrang = taoTrang("Phụ tùng đã được duyệt",
                maLenhDangMo + " · Đối chiếu phạm vi trước khi ghi nhận lượng thực dùng.");
        DefaultTableModel modelDuyet = PhuTungUi.taoModel(
                "Mã phụ tùng", "Tên phụ tùng", "Được duyệt", "Tồn khả dụng");
        for (Map.Entry<String, BigDecimal> dong : dao.docPhamViDuyet(taiKhoan, maLenhDangMo).entrySet()) {
            PhuTung phuTung = dao.docPhuTung(dong.getKey());
            modelDuyet.addRow(new Object[] {
                    dong.getKey(), phuTung.getTenPhuTung(),
                    PhuTungUi.dinhDangLuong(dong.getValue()) + " " + phuTung.getDonViTinh(),
                    PhuTungUi.dinhDangLuong(dao.docTon(dong.getKey())) + " " + phuTung.getDonViTinh()
            });
        }
        JPanel pnlCot = taoCot();
        themThe(pnlCot, PhuTungUi.taoThe("Danh sách phụ tùng được phép sử dụng", bocBang(modelDuyet)));
        pnlTrang.add(bocVungCuon(pnlCot), BorderLayout.CENTER);
        JPanel pnlNut = taoHangNut();
        themNut(pnlNut, "Nhập lượng thực dùng", "NHAP_LUONG", true, false);
        pnlTrang.add(pnlNut, BorderLayout.SOUTH);
        doiTrang(pnlTrang);
    }

    // Ghi nhận thực dùng rồi chuyển tới chi tiết lệnh V1 hoặc kiểm tra hoàn tất V2.
    private void xacNhanThucDung() {
        Map<String, BigDecimal> luongDung = new LinkedHashMap<String, BigDecimal>();
        loiVuotTon = null;
        for (Map.Entry<String, JTextField> dong : cacLuong.entrySet()) {
            BigDecimal soLuong = PhuTungUi.docSoLuong(dong.getValue());
            if (soLuong.signum() < 0) {
                throw new IllegalArgumentException("Số lượng không được âm.");
            }
            if (soLuong.signum() > 0) {
                dao.docPhuTung(dong.getKey()).kiemTraSoLuong(soLuong);
            }
            luongDung.put(dong.getKey(), soLuong);
            if (soLuong.compareTo(dao.docTon(dong.getKey())) > 0 && loiVuotTon == null) {
                PhuTung phuTung = dao.docPhuTung(dong.getKey());
                loiVuotTon = dong.getKey() + " còn " + PhuTungUi.dinhDangLuong(dao.docTon(dong.getKey()))
                        + " " + phuTung.getDonViTinh() + " nhưng nhập " + PhuTungUi.dinhDangLuong(soLuong)
                        + " " + phuTung.getDonViTinh()
                        + ". Không ghi nhận xuất; kiểm tra lại hoặc chuyển lệnh sang chờ phụ tùng.";
            }
        }
        if (loiVuotTon != null) {
            hienVuotTon();
            return;
        }
        lenhDAO.xacNhanThucDung(taiKhoan, maLenhDangMo, nhapDangXacNhan,
                luongDung, txaGhiChu.getText().trim());
        nhapDangXacNhan = null;
        cacTonDuKien.clear();
        if (lenhDAO.docLenh(taiKhoan, maLenhDangMo).isBaoGiaV2()) {
            hienKiemTraHoanTat(true);
        } else {
            hienChiTietLenh();
        }
    }

    // Giữ lượng đang nhập để sửa lại hoặc chuyển chính lệnh này sang chờ phụ tùng.
    private void hienVuotTon() {
        JPanel pnlTrang = taoTrang("Số lượng dùng vượt tồn kho",
                maLenhDangMo + " · Kho chỉ trừ khi xác nhận lượng dùng thực tế.");
        JPanel pnlCot = taoCot();
        themThe(pnlCot, taoThongDiep("Số lượng dùng vượt tồn kho", loiVuotTon, new Color(0xFDECEC)));
        JPanel pnlLuong = taoLuoi();
        for (Map.Entry<String, JTextField> dong : cacLuong.entrySet()) {
            PhuTung phuTung = dao.docPhuTung(dong.getKey());
            String noiDung = dong.getValue().getText() + " " + phuTung.getDonViTinh();
            boolean vuot = PhuTungUi.docSoLuong(dong.getValue()).compareTo(dao.docTon(dong.getKey())) > 0;
            if (vuot) {
                noiDung += " · Vượt tồn khả dụng " + PhuTungUi.dinhDangLuong(dao.docTon(dong.getKey()));
            }
            JTextArea txaLuong = PhuTungUi.taoMoTa(noiDung);
            txaLuong.setForeground(vuot ? PhuTungUi.MAU_NGUY_HIEM : UiTheme.TEXT);
            themONhap(pnlLuong, phuTung.toString() + " *", txaLuong);
        }
        themThe(pnlCot, PhuTungUi.taoThe("Lượng đã dùng thực tế", pnlLuong));
        themThe(pnlCot, taoCanhBaoKhongXuatHaiLan());
        pnlTrang.add(bocVungCuon(pnlCot), BorderLayout.CENTER);
        JPanel pnlNut = taoHangNut();
        themNut(pnlNut, "Sửa số lượng", "NHAP_LUONG", false, false);
        themNut(pnlNut, "Chờ phụ tùng", "CHO_PHU_TUNG", true, false);
        themNut(pnlNut, "Xem phụ tùng đã duyệt", "DA_DUYET", false, false);
        pnlTrang.add(pnlNut, BorderLayout.SOUTH);
        doiTrang(pnlTrang);
    }

    // Dựng màn chi tiết lệnh đúng các khối thông tin, tiến độ, phát sinh và kết quả đã lưu.
    private void hienChiTietLenh() {
        LenhPhuTung lenh = lenhDAO.docLenh(taiKhoan, maLenhDangMo);
        JPanel pnlTrang = taoTrang(lenh.isBaoGiaV2()
                ? "Sửa chữa — phát sinh đã được duyệt" : "Chi tiết tiến độ sửa chữa",
                lenh.getMaLenh() + " · " + lenh.getBienSo()
                        + " · Chỉ thực hiện hạng mục đã được khách duyệt.");
        JPanel pnlCot = taoCot();
        JPanel pnlThongTin = taoLuoi();
        themONhap(pnlThongTin, "Kỹ thuật viên chính", PhuTungUi.taoNhan(lenh.getKyThuatVien(), false));
        themONhap(pnlThongTin, "Báo giá", PhuTungUi.taoNhan(
                lenh.getMaLenh().replace("SC-", "BG-") + (lenh.isBaoGiaV2() ? " V2" : " V1")
                        + " · Đã đồng ý", false));
        themONhap(pnlThongTin, "Hạn dự kiến",
                PhuTungUi.taoNhan(lenh.isBaoGiaV2() ? "05/10/2026 · 19:00" : "05/10/2026 · 17:00", false));
        themONhap(pnlThongTin, "Trạng thái", PhuTungUi.taoNhan(lenh.getTrangThai(), false));
        themThe(pnlCot, PhuTungUi.taoThe("Thông tin lệnh", pnlThongTin));
        DefaultTableModel modelTienDo = PhuTungUi.taoModel(
                "Công việc", "Người thực hiện", "Kết quả", "Trạng thái");
        boolean daXong = "Hoàn tất".equals(lenh.getTrangThaiCongViec());
        modelTienDo.addRow(new Object[] {
                "Bảo dưỡng dầu / lọc", "Chung Minh Hoàng", "Đã thay dầu, kiểm tra rò rỉ", "Hoàn tất"
        });
        modelTienDo.addRow(new Object[] {
                "Vệ sinh phanh trước", "Nguyễn Minh Vũ", lenh.getKetQuaCongViec(),
                daXong ? "Hoàn tất" : "Chờ phụ tùng".equals(lenh.getTrangThaiCongViec())
                        ? "Chờ phụ tùng" : "Đang thực hiện"
        });
        if (lenh.isBaoGiaV2()) {
            modelTienDo.addRow(new Object[] {
                    "Thay rotuyn lái", "Chung Minh Hoàng",
                    daXong ? lenh.getKetQuaCongViec() : "Khách đồng ý; chờ thực hiện",
                    daXong ? "Hoàn tất" : "Được phép thực hiện"
            });
        }
        themThe(pnlCot, PhuTungUi.taoThe("Tiến độ hạng mục", bocBang(modelTienDo)));
        String[] phatSinh = lenh.getPhatSinh();
        String noiDung = phatSinh[0].isEmpty()
                ? lenh.isBaoGiaV2()
                        ? "Phát sinh trong báo giá V2 đã được khách chấp thuận. "
                                + "Chỉ trừ tồn khi ghi nhận phụ tùng thực dùng."
                        : "Nếu có đề xuất phát sinh chưa được khách đồng ý, chỉ tạm dừng phần việc liên quan."
                : phatSinh[0] + " · " + phatSinh[2] + "\nĐang chờ tiếp nhận xử lý và khách xác nhận.";
        themThe(pnlCot, taoThongDiep(lenh.isBaoGiaV2() && phatSinh[0].isEmpty()
                ? "Phát sinh đã được chấp thuận" : "Phát sinh đang chờ", noiDung,
                lenh.isBaoGiaV2() && phatSinh[0].isEmpty() ? new Color(0xEDF7EF) : UiTheme.WINDOW));
        themThe(pnlCot, PhuTungUi.taoThe("Kết quả đã lưu", PhuTungUi.taoMoTa(lenh.getKetQuaCongViec())));
        if ("Hoàn thành".equals(lenh.getTrangThai())) {
            themThe(pnlCot, taoThongDiep("Đã hoàn thành",
                    lenh.getKetQuaHoanTat()[0] + "\nLệnh sẵn sàng để thu ngân đối chiếu; chưa thanh toán.",
                    new Color(0xEDF7EF)));
        }
        pnlTrang.add(bocVungCuon(pnlCot), BorderLayout.CENTER);
        JPanel pnlNut = taoHangNut();
        if ("Hoàn thành".equals(lenh.getTrangThai())) {
            themNut(pnlNut, "Về danh sách phụ tùng", "DANH_SACH", false, false);
        } else {
            if (lenh.isBaoGiaV2()) {
                themNut(pnlNut, "Về danh sách phụ tùng", "DANH_SACH", false, false);
            }
            themNut(pnlNut, lenh.isBaoGiaV2() ? "Ghi nhận phụ tùng thực dùng" : "Ghi nhận phụ tùng",
                    "GHI_NHAN_TU_LENH", false, false);
            if (!lenh.isBaoGiaV2()) {
                themNut(pnlNut, "Báo phát sinh", "BAO_PHAT_SINH", false, false);
            }
            themNut(pnlNut, "Kiểm tra hoàn thành", "KIEM_TRA_HOAN_TAT", true, false);
            themNut(pnlNut, "Cập nhật công việc", "CAP_NHAT_CONG_VIEC", true, false);
        }
        pnlTrang.add(pnlNut, BorderLayout.SOUTH);
        doiTrang(pnlTrang);
    }

    // Mở màn cập nhật tiến độ với giá trị đã lưu của chính lệnh đang xem.
    private void hienCapNhatCongViec() {
        LenhPhuTung lenh = lenhDAO.docLenh(taiKhoan, maLenhDangMo);
        cmbTienDo.setSelectedItem(lenh.getTrangThaiCongViec());
        txtKetQuaCongViec.setText(lenh.getKetQuaCongViec());
        JPanel pnlTrang = taoTrang("Cập nhật tiến độ công việc",
                "Lệnh " + maLenhDangMo + " · " + lenh.getKyThuatVien());
        JPanel pnlTruong = new JPanel(new GridLayout(0, 1, 0, 10));
        pnlTruong.setOpaque(false);
        pnlTruong.add(PhuTungUi.taoTruong("Trạng thái *", cmbTienDo));
        pnlTruong.add(PhuTungUi.taoTruong("Kết quả thực hiện *", txtKetQuaCongViec));
        JPanel pnlCot = taoCot();
        themThe(pnlCot, PhuTungUi.taoThe("Kết quả công việc", pnlTruong));
        pnlTrang.add(bocVungCuon(pnlCot), BorderLayout.CENTER);
        JPanel pnlNut = taoHangNut();
        themNut(pnlNut, "Hủy", "VE_LENH", false, false);
        themNut(pnlNut, "Lưu tiến độ", "LUU_TIEN_DO", true, false);
        pnlTrang.add(pnlNut, BorderLayout.SOUTH);
        doiTrang(pnlTrang);
    }

    // Mở biểu mẫu báo phát sinh và không tự chuyển phát sinh thành đã được khách duyệt.
    private void hienBaoPhatSinh() {
        String[] phatSinh = lenhDAO.docLenh(taiKhoan, maLenhDangMo).getPhatSinh();
        for (int viTri = 0; viTri < cacTruongPhatSinh.length; viTri++) {
            cacTruongPhatSinh[viTri].setText(phatSinh[viTri]);
        }
        txaGhiChuPhatSinh.setText(phatSinh[4]);
        JPanel pnlTrang = taoTrang("Báo hư hỏng phát sinh",
                maLenhDangMo + " · Gửi tiếp nhận để lập báo giá và liên hệ khách.");
        JPanel pnlTruong = taoLuoi();
        String[] nhan = {"Bộ phận / hư hỏng *", "Nguyên nhân phát hiện *",
                "Dịch vụ / phụ tùng đề xuất *", "Thời gian dự kiến thêm"};
        for (int viTri = 0; viTri < nhan.length; viTri++) {
            themONhap(pnlTruong, nhan[viTri], cacTruongPhatSinh[viTri]);
        }
        themONhap(pnlTruong, "Ghi chú", bocVungNhap(txaGhiChuPhatSinh));
        JPanel pnlCot = taoCot();
        themThe(pnlCot, PhuTungUi.taoThe("Hư hỏng phát hiện", pnlTruong));
        themThe(pnlCot, taoThongDiep("Chưa được phép thực hiện",
                "Nhân viên tiếp nhận phải báo chi phí và ghi nhận khách đồng ý trước khi kỹ thuật làm thêm.",
                UiTheme.WINDOW));
        pnlTrang.add(bocVungCuon(pnlCot), BorderLayout.CENTER);
        JPanel pnlNut = taoHangNut();
        themNut(pnlNut, "Quay lại lệnh", "VE_LENH", false, false);
        themNut(pnlNut, "Gửi nhân viên tiếp nhận", "LUU_PHAT_SINH", true, false);
        pnlTrang.add(pnlNut, BorderLayout.SOUTH);
        doiTrang(pnlTrang);
    }

    // Lưu đề xuất phát sinh trong dữ liệu mẫu rồi trở lại chi tiết lệnh.
    private void luuPhatSinh() {
        String[] noiDung = new String[5];
        for (int viTri = 0; viTri < cacTruongPhatSinh.length; viTri++) {
            noiDung[viTri] = cacTruongPhatSinh[viTri].getText();
        }
        noiDung[4] = txaGhiChuPhatSinh.getText();
        lenhDAO.luuPhatSinh(taiKhoan, maLenhDangMo, noiDung);
        hienChiTietLenh();
    }

    // Hiển thị kiểm tra cuối và báo rõ tiến độ chưa đạt trước khi người dùng xác nhận.
    private void hienKiemTraHoanTat(boolean napMoi) {
        LenhPhuTung lenh = lenhDAO.docLenh(taiKhoan, maLenhDangMo);
        JPanel pnlTrang = taoTrang(lenh.isBaoGiaV2()
                ? "Kiểm tra hoàn tất — gồm phát sinh được duyệt" : "Kiểm tra và hoàn tất sửa chữa",
                maLenhDangMo + " · Đối chiếu công việc và vật tư trước khi chuyển thu ngân.");
        JPanel pnlKiemTra = new JPanel(new GridLayout(0, 1, 0, 0));
        pnlKiemTra.setOpaque(false);
        String[] nhan = {
                "Hạng mục đã duyệt được thực hiện và kiểm tra lại.",
                docMoTaDaGhiNhan(),
                "Phát sinh chưa duyệt không được thực hiện hoặc tính tiền.",
                "Phụ tùng cũ được giữ để trả theo yêu cầu của khách."
        };
        for (int viTri = 0; viTri < cacKiemTra.length; viTri++) {
            if (napMoi || cacKiemTra[viTri] == null) {
                cacKiemTra[viTri] = new JCheckBox(nhan[viTri], false);
            } else {
                cacKiemTra[viTri].setText(nhan[viTri]);
            }
            PhuTungUi.dinhDangKiemTra(cacKiemTra[viTri]);
            pnlKiemTra.add(cacKiemTra[viTri]);
        }
        if (napMoi) {
            String[] ketQua = lenh.getKetQuaHoanTat();
            txtKetLuan.setText(ketQua[0]);
            txtBaoHanh.setText(ketQua[1]);
            txtKhuyenNghi.setText(ketQua[2]);
        }
        JPanel pnlKetQua = taoLuoi();
        themONhap(pnlKetQua, "Kết luận *", txtKetLuan);
        themONhap(pnlKetQua, "Thông tin bảo hành", txtBaoHanh);
        themONhap(pnlKetQua, "Khuyến nghị", txtKhuyenNghi);
        JPanel pnlCot = taoCot();
        themThe(pnlCot, PhuTungUi.taoThe("Kiểm tra cuối", pnlKiemTra));
        themThe(pnlCot, PhuTungUi.taoThe("Kết quả hoàn tất", pnlKetQua));
        themThe(pnlCot, taoThongDiep("Chuyển sang Hoàn thành",
                "Sau xác nhận, phiếu được thu ngân chọn để đối chiếu và lập hóa đơn. "
                        + "Chưa chuyển thành Đã thanh toán.", new Color(0xEDF7EF)));
        if (lenh.isBaoGiaV2()) {
            themThe(pnlCot, taoThongDiep("Đối chiếu phần bổ sung",
                    "Đối chiếu công việc bổ sung, kết quả hệ thống lái và lượng thực dùng đã ghi sổ. "
                            + "Chỉ hoàn tất khi mọi hạng mục được duyệt có kết quả đạt yêu cầu.",
                    new Color(0xEDF7EF)));
        }
        boolean tienDoDaXong = "Hoàn tất".equals(lenh.getTrangThaiCongViec());
        boolean conPhatSinh = !lenh.getPhatSinh()[0].isEmpty();
        if (!tienDoDaXong || conPhatSinh) {
            String noiDung = !tienDoDaXong
                    ? "Tiến độ hiện tại: " + lenh.getTrangThaiCongViec()
                            + ". Chọn Quay lại chỉnh sửa, mở Cập nhật công việc và lưu trạng thái Hoàn tất "
                            + "sau khi các hạng mục đã thực hiện xong."
                    : "Lệnh còn phát sinh đang chờ xử lý. Chưa thể xác nhận hoàn thành.";
            themThe(pnlCot, taoThongDiep("Chưa đủ điều kiện hoàn thành",
                    noiDung, new Color(0xFFF4E5)));
        }
        pnlTrang.add(bocVungCuon(pnlCot), BorderLayout.CENTER);
        JPanel pnlNut = taoHangNut();
        themNut(pnlNut, lenh.isBaoGiaV2() ? "Quay lại lệnh" : "Quay lại chỉnh sửa",
                "VE_LENH", false, false);
        themNut(pnlNut, lenh.isBaoGiaV2() ? "Hoàn tất và chuyển thu ngân" : "Xác nhận hoàn thành",
                "HOAN_TAT", true, false);
        pnlTrang.add(pnlNut, BorderLayout.SOUTH);
        doiTrang(pnlTrang);
    }

    // Tổng hợp đúng các bản ghi thực dùng của lệnh để không hiển thị lượng xác nhận giả.
    private String docMoTaDaGhiNhan() {
        StringBuilder noiDung = new StringBuilder("Phụ tùng thực tế đã ghi nhận: ");
        boolean coDuLieu = false;
        for (PhuTungSuDung banGhi : dao.docDanhSach(taiKhoan, maLenhDangMo, TrangThaiSuDung.DA_GHI_NHAN)) {
            if (!maLenhDangMo.equals(banGhi.getMaLenhSuaChua())) {
                continue;
            }
            if (coDuLieu) {
                noiDung.append("; ");
            }
            noiDung.append(banGhi.getPhuTung().getMaPhuTung()).append(" ")
                    .append(PhuTungUi.dinhDangLuong(banGhi.getSoLuong())).append(" ")
                    .append(banGhi.getPhuTung().getDonViTinh());
            coDuLieu = true;
        }
        return noiDung.append(coDuLieu ? "." : "chưa có.").toString();
    }

    // Kiểm tra các xác nhận cuối rồi lưu hoàn thành mà không đổi vai trò đăng nhập.
    private void hoanTatLenh() {
        boolean[] daKiemTra = new boolean[cacKiemTra.length];
        for (int viTri = 0; viTri < cacKiemTra.length; viTri++) {
            daKiemTra[viTri] = cacKiemTra[viTri].isSelected();
        }
        lenhDAO.hoanTatLenh(taiKhoan, maLenhDangMo, daKiemTra,
                new String[] {txtKetLuan.getText(), txtBaoHanh.getText(), txtKhuyenNghi.getText()});
        hienChiTietLenh();
    }

    // Tạo cảnh báo thống nhất về thời điểm trừ tồn kho.
    private JPanel taoCanhBaoKhongXuatHaiLan() {
        return taoThongDiep("Không xuất hai lần",
                "Xác nhận này tạo chứng từ xuất gắn với lệnh. Không giảm kho khi mới lập báo giá; "
                        + "không xuất vượt tồn.", UiTheme.WINDOW);
    }

    // Tạo thẻ thông báo dùng lại màu và kiểu chữ từ component chung.
    private JPanel taoThongDiep(String tieuDe, String noiDung, Color mauNen) {
        JPanel pnlThe = PhuTungUi.taoThe(tieuDe, PhuTungUi.taoMoTa(noiDung));
        pnlThe.setBackground(mauNen);
        return pnlThe;
    }

    // Tạo trang nội dung để MainFrame tiếp tục quản lý toàn bộ khung ứng dụng.
    private JPanel taoTrang(String tieuDe, String moTa) {
        JPanel pnlTrang = new JPanel(new BorderLayout(0, 10));
        pnlTrang.setOpaque(false);
        JPanel pnlDau = new JPanel(new BorderLayout(0, 4));
        pnlDau.setOpaque(false);
        JLabel lblTieuDe = PhuTungUi.taoNhan(tieuDe, true);
        lblTieuDe.setFont(UiTheme.HEADING_FONT);
        pnlDau.add(lblTieuDe, BorderLayout.NORTH);
        if (moTa != null) {
            pnlDau.add(PhuTungUi.taoMoTa(moTa), BorderLayout.CENTER);
        }
        pnlTrang.add(pnlDau, BorderLayout.NORTH);
        return pnlTrang;
    }

    // Tạo lưới hai cột cho các trường thông tin.
    private JPanel taoLuoi() {
        JPanel pnlLuoi = new JPanel(new GridBagLayout());
        pnlLuoi.setOpaque(false);
        return pnlLuoi;
    }

    // Đặt trường vào hai cột bằng chiều cao tự nhiên để tránh cắt chữ.
    private void themONhap(JPanel pnlLuoi, String nhan, JComponent thanhPhan) {
        int viTri = pnlLuoi.getComponentCount();
        GridBagConstraints rangBuoc = new GridBagConstraints();
        rangBuoc.gridx = viTri % 2;
        rangBuoc.gridy = viTri / 2;
        rangBuoc.weightx = 1;
        rangBuoc.fill = GridBagConstraints.HORIZONTAL;
        rangBuoc.anchor = GridBagConstraints.NORTH;
        rangBuoc.insets = new Insets(0, viTri % 2 == 0 ? 0 : 6, 10, viTri % 2 == 0 ? 6 : 0);
        JPanel pnlTruong = PhuTungUi.taoTruong(nhan, thanhPhan);
        int chieuCao = pnlTruong.getPreferredSize().height;
        pnlTruong.setPreferredSize(new Dimension(0, chieuCao));
        pnlTruong.setMinimumSize(new Dimension(0, chieuCao));
        pnlLuoi.add(pnlTruong, rangBuoc);
    }

    // Tạo cột nội dung căn trên theo các thẻ trong Figma.
    private JPanel taoCot() {
        JPanel pnlCot = new JPanel();
        pnlCot.setLayout(new BoxLayout(pnlCot, BoxLayout.Y_AXIS));
        pnlCot.setBackground(UiTheme.SURFACE);
        pnlCot.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return pnlCot;
    }

    // Thêm thẻ nội dung và khoảng cách giữa các thẻ.
    private void themThe(JPanel pnlCot, JPanel pnlThe) {
        pnlThe.setAlignmentX(LEFT_ALIGNMENT);
        pnlThe.setMaximumSize(new Dimension(Integer.MAX_VALUE, pnlThe.getPreferredSize().height));
        pnlCot.add(pnlThe);
        pnlCot.add(Box.createVerticalStrut(8));
    }

    // Bọc nội dung màn hình bằng vùng cuộn; không áp dụng cho hộp thoại.
    private JScrollPane bocVungCuon(JPanel pnlCot) {
        JPanel pnlCanDinh = new JPanel(new BorderLayout());
        pnlCanDinh.setBackground(UiTheme.SURFACE);
        pnlCanDinh.add(pnlCot, BorderLayout.NORTH);
        JScrollPane scrNoiDung = new JScrollPane(pnlCanDinh);
        scrNoiDung.setBorder(BorderFactory.createLineBorder(new Color(0xDCE1E7)));
        scrNoiDung.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrNoiDung.getViewport().setBackground(UiTheme.SURFACE);
        scrNoiDung.getVerticalScrollBar().setUnitIncrement(16);
        return scrNoiDung;
    }

    // Tạo vùng bảng đủ chiều cao hiển thị các dòng mẫu.
    private JScrollPane bocBang(DefaultTableModel modelBang) {
        JTable tblBang = PhuTungUi.taoBang(modelBang);
        JScrollPane scrBang = new JScrollPane(tblBang);
        scrBang.setBorder(BorderFactory.createLineBorder(new Color(0xDCE1E7)));
        scrBang.setPreferredSize(new Dimension(0,
                tblBang.getTableHeader().getPreferredSize().height
                        + tblBang.getRowHeight() * Math.max(1, modelBang.getRowCount()) + 4));
        return scrBang;
    }

    // Định dạng vùng nhập ghi chú bằng font có sẵn của leader.
    private void dinhDangVungNhap(JTextArea txaNoiDung) {
        txaNoiDung.setLineWrap(true);
        txaNoiDung.setWrapStyleWord(true);
        txaNoiDung.setFont(UiTheme.SHELL_FONT);
        txaNoiDung.setForeground(UiTheme.TEXT);
        txaNoiDung.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
    }

    // Bọc riêng trường ghi chú với chiều cao đủ cho nội dung nhập.
    private JScrollPane bocVungNhap(JTextArea txaNoiDung) {
        JScrollPane scrNhap = new JScrollPane(txaNoiDung);
        scrNhap.setPreferredSize(new Dimension(0, 72));
        scrNhap.setMinimumSize(new Dimension(0, 72));
        scrNhap.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));
        return scrNhap;
    }

    // Tạo hàng nút thao tác ở cạnh dưới màn hình.
    private JPanel taoHangNut() {
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.setOpaque(false);
        return pnlNut;
    }

    // Tạo nút bằng component chung và gắn lệnh điều hướng tương ứng.
    private void themNut(JPanel pnlNut, String nhan, String lenh, boolean chinh, boolean nguyHiem) {
        JButton btnNut = PhuTungUi.taoNut(nhan, chinh, nguyHiem);
        btnNut.setActionCommand(lenh);
        btnNut.addActionListener(this);
        pnlNut.add(btnNut);
    }

    // Định dạng các điều khiển của màn trước khi hiển thị trong MainFrame.
    private void doiTrang(JPanel pnlTrang) {
        PhuTungUi.dinhDangCay(pnlTrang);
        removeAll();
        add(pnlTrang, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    // Lấy cửa sổ chính làm chủ cho mọi hộp thoại.
    private Window docCuaSo() {
        return SwingUtilities.getWindowAncestor(this);
    }

    // Hiển thị thông báo theo component chung và giữ nội dung màn đang nhập.
    private void thongBao(String tieuDe, String noiDung) {
        PhuTungUi.hienHopThoai(docCuaSo(), tieuDe, noiDung, "Đóng", false, false);
    }
}