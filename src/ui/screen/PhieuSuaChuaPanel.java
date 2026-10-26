package ui.screen;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import dao.PhieuSuaChuaDAO;
import entity.ChiTietBaoGia;
import entity.HangMucSuaChua;
import entity.PhatSinhSuaChua;
import entity.PhieuSuaChua;
import entity.TaiKhoan;
import entity.TrangThaiPhatSinh;
import entity.TrangThaiSuaChua;
import entity.VaiTro;
import ui.component.SuaChuaUi;
import ui.component.UiTheme;
import ui.dialog.PhieuSuaChuaDialog;

public final class PhieuSuaChuaPanel extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;

    private final JFrame winChu;
    private final TaiKhoan taiKhoan;
    private final PhieuSuaChuaDAO phieuDAO = new PhieuSuaChuaDAO();

    private final JTextField txtTuKhoa = new JTextField("SC-2026-0112");
    private final JComboBox<String> cmbBoLoc = new JComboBox<String>(
            new String[]{"Tất cả", "Chờ sửa chữa", "Đang sửa", "Chờ phụ tùng", "Hoàn thành"});
    private final JComboBox<String> cmbChinh = new JComboBox<String>(
            new String[]{"Chung Ngô Minh Hoàng", "Nguyễn Minh Vũ"});
    private final JComboBox<String> cmbPhoiHop = new JComboBox<String>(
            new String[]{"Không", "Chung Ngô Minh Hoàng", "Nguyễn Minh Vũ"});
    private final JTextField txtHan = new JTextField();
    private final JTextArea txaGhiChu = new JTextArea(3, 20);
    private final JComboBox<TrangThaiSuaChua> cmbTienDo = new JComboBox<TrangThaiSuaChua>(
            new TrangThaiSuaChua[]{TrangThaiSuaChua.DANG_SUA, TrangThaiSuaChua.CHO_PHU_TUNG,
                TrangThaiSuaChua.HOAN_THANH});
    private final JTextArea txaKetQua = new JTextArea(3, 20);
    private final JTextField[] txtPhatSinh = new JTextField[4];
    private final JTextArea txaPhatSinh = new JTextArea(3, 20);
    private final JCheckBox[] chkKiemTra = new JCheckBox[4];
    private final JTextField[] txtHoanTat = new JTextField[3];
    private JTable tblPhieu;
    private JTable tblHangMuc;

    /** Các dòng hiển thị, mã lệnh và công việc đang được chọn. */
    private List<PhieuSuaChua> danhSachDangHien = new ArrayList<PhieuSuaChua>();
    private PhieuSuaChua phieuChon;
    private int viTriHangMuc;

    /** Dấu vết biểu mẫu phục vụ xác nhận hủy thay đổi. */
    private String cheDoNhap = "";
    private String dauVetBanDau = "";

    public PhieuSuaChuaPanel(JFrame winChu, TaiKhoan taiKhoan) {
        this.winChu = winChu;
        this.taiKhoan = taiKhoan;
        for (JTextArea txaTruong : new JTextArea[]{txaGhiChu, txaKetQua, txaPhatSinh}) {
            txaTruong.setLineWrap(true);
            txaTruong.setWrapStyleWord(true);
        }
        setLayout(new BorderLayout(8, 8));
        setBackground(UiTheme.WINDOW);
        hienDanhSach(false, "");
    }

    /** {@inheritDoc} */
    // Điều phối sự kiện và giữ nguyên biểu mẫu khi DAO từ chối thao tác lưu.
    @Override
    public void actionPerformed(ActionEvent suKien) {
        String lenh = suKien.getActionCommand();
        try {
            if ("DANH_SACH".equals(lenh) || "HUY".equals(lenh)) {
                if (!duocRoiBieuMau()) {
                    return;
                }
                if ("HUY".equals(lenh)) {
                    hienTienDo("");
                } else {
                    hienDanhSach(false, "");
                }
            } else if ("TIM".equals(lenh)) {
                hienTimKiem();
            } else if ("LOC".equals(lenh)) {
                hienDanhSach(true, "");
            } else if ("THEM".equals(lenh)) {
                moBieuMau(null, false);
            } else if ("XEM".equals(lenh)) {
                moBieuMau(layPhieuChon(), true);
            } else if ("SUA".equals(lenh)) {
                PhieuSuaChua phieu = layPhieuChon();
                if (phieu.getTrangThai() != TrangThaiSuaChua.CHO_SUA_CHUA) {
                    throw new IllegalStateException("Chỉ sửa phân công trực tiếp khi lệnh chưa bắt đầu.");
                }
                moBieuMau(phieu, false);
            } else if ("XOA".equals(lenh)) {
                xoaPhieu();
            } else if ("MO".equals(lenh)) {
                phieuChon = layPhieuChon();
                if (taiKhoan.getVaiTro() == VaiTro.NHAN_VIEN_TIEP_NHAN
                        && phieuChon.getTrangThai() == TrangThaiSuaChua.CHO_SUA_CHUA) {
                    hienPhanCong();
                } else {
                    hienTienDo("");
                }
            } else if ("BAO_GIA".equals(lenh)) {
                SuaChuaUi.xacNhan(winChu, "Báo giá đã duyệt", phieuChon.getMaBaoGia()
                        + " · " + phieuChon.getBienSo() + "\nMàn hình báo giá đầy đủ thuộc nhánh khác.", "Đóng", null);
            } else if ("PHAN_CONG".equals(lenh)) {
                luuPhanCong();
            } else if ("CAP_NHAT".equals(lenh)) {
                hienCapNhat();
            } else if ("LUU_TIEN_DO".equals(lenh)) {
                phieuDAO.capNhatTienDo(taiKhoan, phieuChon.getMaPhieu(), viTriHangMuc,
                        (TrangThaiSuaChua) cmbTienDo.getSelectedItem(), txaKetQua.getText());
                hienTienDo("Đã lưu tiến độ.");
            } else if ("PHAT_SINH".equals(lenh)) {
                hienPhatSinh();
            } else if ("GUI_PHAT_SINH".equals(lenh)) {
                phieuDAO.guiPhatSinh(taiKhoan, phieuChon.getMaPhieu(), layNoiDungPhatSinh());
                hienTienDo("Đề xuất đã được lưu ở trạng thái chờ khách duyệt.");
            } else if ("KIEM_TRA".equals(lenh)) {
                if (phieuChon.getPhatSinh() != null
                        && phieuChon.getPhatSinh().getTrangThai() == TrangThaiPhatSinh.CHO_DUYET) {
                    hienTienDo("Phát sinh chưa được duyệt. Không thực hiện phần này; tiếp tục phần đã duyệt.");
                } else {
                    hienHoanTat();
                }
            } else if ("HOAN_TAT".equals(lenh)) {
                xacNhanHoanTat();
            } else if ("PHU_TUNG".equals(lenh)) {
                SuaChuaUi.xacNhan(winChu, "Phụ tùng thực dùng", "Chức năng thuộc nhánh phụ tùng sử dụng, "
                        + "chưa tích hợp. Nhánh này chưa trừ kho hoặc chốt chi phí.", "Đóng", null);
            }
        } catch (java.time.format.DateTimeParseException loi) {
            SuaChuaUi.xacNhan(winChu, "Ngày giờ chưa hợp lệ", "Nhập hạn theo dd/MM/yyyy HH:mm.", "Đóng", null);
        } catch (IllegalArgumentException | IllegalStateException | SecurityException loi) {
            SuaChuaUi.xacNhan(winChu, "Không thể thực hiện", loi.getMessage(), "Đóng", null);
        }
    }

    /**
     * @param loc áp dụng tiêu chí
     * @param thongBao kết quả thao tác
     */
    // Dùng một bảng cho danh sách lệnh, công việc cá nhân và kết quả tìm kiếm.
    private void hienDanhSach(boolean loc, String thongBao) {
        boolean kyThuat = taiKhoan.getVaiTro() == VaiTro.NHAN_VIEN_KY_THUAT;
        danhSachDangHien = phieuDAO.timKiem(taiKhoan, loc ? txtTuKhoa.getText() : "",
                loc ? String.valueOf(cmbBoLoc.getSelectedItem()) : "Tất cả");
        DefaultTableModel modelPhieu = SuaChuaUi.taoModel(kyThuat
                ? new String[]{"Mã lệnh", "Biển số", "Nội dung", "Hạn", "Trạng thái"}
                : new String[]{"Số lệnh", "Báo giá", "Biển số", "Người thực hiện", "Trạng thái"});
        for (PhieuSuaChua phieu : danhSachDangHien) {
            modelPhieu.addRow(kyThuat ? new Object[]{phieu.getMaPhieu(), phieu.getBienSo(), phieu.getGhiChu(),
                phieu.getHanHoanThanh().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")), phieu.getTrangThai()}
                : new Object[]{phieu.getMaPhieu(), phieu.getMaBaoGia(), phieu.getBienSo(),
                    SuaChuaUi.layTenKyThuat(phieu.getKyThuatChinh()), phieu.getTrangThai()});
        }
        tblPhieu = SuaChuaUi.taoBang(modelPhieu);
        if (!danhSachDangHien.isEmpty() && thongBao.isEmpty()) {
            tblPhieu.setRowSelectionInterval(0, 0);
        }
        JPanel pnlNoiDung = taoXepDoc();
        if (!kyThuat) {
            JPanel pnlNut = taoHangNut(new String[]{"Tìm kiếm", "Thêm", "Xem", "Sửa", "Xóa",
                "Dữ liệu liên quan", "Phân công / tiến độ"},
                new String[]{"TIM", "THEM", "XEM", "SUA", "XOA", "XEM", "MO"}, "THEM");
            pnlNut.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
            themThanhPhan(pnlNoiDung, pnlNut, 30);
        }
        JPanel pnlBang = SuaChuaUi.taoThe(kyThuat ? "Lệnh được phân công" : "Lệnh sửa chữa");
        pnlBang.add(SuaChuaUi.taoVungCuon(tblPhieu), BorderLayout.CENTER);
        themThanhPhan(pnlNoiDung, pnlBang, kyThuat ? 166 : 400);
        JTextArea txaQuyTac = SuaChuaUi.taoVanBan(SuaChuaUi.QUY_TAC);
        txaQuyTac.setFont(UiTheme.CAPTION_FONT);
        themThanhPhan(pnlNoiDung, txaQuyTac, 34);
        themThanhPhan(pnlNoiDung, SuaChuaUi.taoVanBan(thongBao.isEmpty()
                ? "Có " + danhSachDangHien.size() + " hồ sơ. Chọn một dòng để tiếp tục." : thongBao), 34);
        String tieuDe = kyThuat ? "Công việc được phân công" : "Danh sách lệnh sửa chữa";
        if (loc) {
            tieuDe = danhSachDangHien.isEmpty() ? "Không tìm thấy lệnh sửa chữa" : "Kết quả tìm kiếm lệnh sửa chữa";
        }
        hienTrang(tieuDe, kyThuat ? "Danh sách lệnh thuộc phân công của tài khoản đang đăng nhập." : "",
                pnlNoiDung, kyThuat ? taoHangNut(new String[]{"Tìm kiếm", "Mở lệnh"},
                    new String[]{"TIM", "MO"}, "MO") : loc ? taoHangNut(new String[]{"Đổi tiêu chí", "Làm mới"},
                        new String[]{"TIM", "DANH_SACH"}, "") : null, false);
    }

    // Hiển thị tiêu chí tìm kiếm và hướng dẫn thành hai thẻ riêng.
    private void hienTimKiem() {
        JPanel pnlNoiDung = taoXepDoc();
        JPanel pnlThe = SuaChuaUi.taoThe("Tiêu chí tìm kiếm");
        JPanel pnlTruong = taoLuoi(2, 1);
        pnlTruong.add(SuaChuaUi.taoTruong("Số lệnh, báo giá hoặc biển số", txtTuKhoa));
        pnlTruong.add(SuaChuaUi.taoTruong("Trạng thái sửa chữa", cmbBoLoc));
        pnlThe.add(pnlTruong, BorderLayout.CENTER);
        themThanhPhan(pnlNoiDung, pnlThe, 190);
        themThanhPhan(pnlNoiDung, taoThongTin("Hướng dẫn", "Có thể nhập một phần thông tin. "
                + "Để xem toàn bộ dữ liệu, quay lại màn danh sách."), 82);
        hienTrang("Tìm kiếm lệnh sửa chữa", "Nhập tiêu chí tìm kiếm rồi chuyển sang màn kết quả.", pnlNoiDung,
                taoHangNut(new String[]{"Về danh sách", "Tìm kiếm"}, new String[]{"DANH_SACH", "LOC"}, "LOC"), false);
    }

    /** @return lệnh tương ứng dòng đã chọn */
    // Chuyển chỉ số dòng của bảng sang vị trí dữ liệu và kiểm tra lệnh còn tồn tại.
    private PhieuSuaChua layPhieuChon() {
        int dong = tblPhieu.getSelectedRow();
        if (dong < 0) {
            throw new IllegalArgumentException("Hãy chọn một lệnh trong bảng.");
        }
        String maPhieu = danhSachDangHien.get(tblPhieu.convertRowIndexToModel(dong)).getMaPhieu();
        PhieuSuaChua phieu = phieuDAO.timTheoMa(taiKhoan, maPhieu).orElse(null);
        if (phieu == null) {
            throw new IllegalArgumentException("Lệnh không tồn tại.");
        }
        return phieu;
    }

    /**
     * @param phieu lệnh cần mở hoặc null khi thêm
     * @param chiDoc chế độ xem
     */
    // Mở dialog của lệnh và chỉ làm mới bảng khi lưu thành công.
    private void moBieuMau(PhieuSuaChua phieu, boolean chiDoc) {
        PhieuSuaChuaDialog winBieuMau = new PhieuSuaChuaDialog(winChu, phieuDAO, taiKhoan, phieu, chiDoc);
        winBieuMau.setVisible(true);
        if (winBieuMau.getKetQua() != null) {
            hienDanhSach(false, "Đã lưu " + winBieuMau.getKetQua().getMaPhieu() + ". Chưa chọn dòng mới.");
        }
    }

    // Hỏi xác nhận rồi yêu cầu DAO kiểm tra và xóa lệnh chưa bắt đầu.
    private void xoaPhieu() {
        PhieuSuaChua phieu = layPhieuChon();
        if (SuaChuaUi.xacNhan(winChu, "Xác nhận xóa", "Xóa " + phieu.getMaPhieu() + " — "
                + phieu.getBienSo() + "?\n\n" + SuaChuaUi.QUY_TAC, "Xóa", "Hủy")) {
            phieuDAO.xoa(taiKhoan, phieu.getMaPhieu());
            hienDanhSach(false, "Đã xóa " + phieu.getMaPhieu() + ". Chưa chọn dòng mới.");
        }
    }

    // Nạp phân công hiện tại và đối chiếu danh sách công việc thuộc báo giá đã duyệt.
    private void hienPhanCong() {
        cmbChinh.setSelectedIndex("kythuatvu".equals(phieuChon.getKyThuatChinh()) ? 1 : 0);
        cmbPhoiHop.setSelectedIndex(phieuChon.getKyThuatPhoiHop().isEmpty() ? 0
                : "kythuat".equals(phieuChon.getKyThuatPhoiHop()) ? 1 : 2);
        txtHan.setText(phieuChon.getHanHoanThanh().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        txaGhiChu.setText(phieuChon.getGhiChu());
        JPanel pnlNoiDung = taoXepDoc();
        JPanel pnlHangMuc = SuaChuaUi.taoThe("Hạng mục đã được khách xác nhận");
        DefaultTableModel modelHangMuc = SuaChuaUi.taoModel(new String[]{"Hạng mục", "Loại", "Số lượng",
            "Đơn giá (₫)", "Thành tiền (₫)"});
        java.text.NumberFormat dinhDangTien = java.text.NumberFormat.getNumberInstance(
                java.util.Locale.forLanguageTag("vi-VN"));
        for (ChiTietBaoGia chiTiet : phieuDAO.layBaoGia(taiKhoan, phieuChon.getMaPhieu()).getChiTiet()) {
            modelHangMuc.addRow(new Object[]{chiTiet.getTenHangMuc(), chiTiet.getLoai(),
                chiTiet.getSoLuong().toPlainString() + " " + chiTiet.getDonVi(),
                dinhDangTien.format(chiTiet.getDonGia()), dinhDangTien.format(chiTiet.getThanhTien())});
        }
        pnlHangMuc.add(SuaChuaUi.taoVungCuon(SuaChuaUi.taoBang(modelHangMuc)), BorderLayout.CENTER);
        themThanhPhan(pnlNoiDung, pnlHangMuc, 220);
        JPanel pnlPhanCong = SuaChuaUi.taoThe("Phân công");
        JPanel pnlTruong = taoLuoi(2, 2);
        pnlTruong.add(SuaChuaUi.taoTruong("Kỹ thuật viên chính *", cmbChinh));
        pnlTruong.add(SuaChuaUi.taoTruong("Kỹ thuật viên phối hợp", cmbPhoiHop));
        pnlTruong.add(SuaChuaUi.taoTruong("Thời hạn dự kiến *", txtHan));
        pnlTruong.add(SuaChuaUi.taoTruong("Ghi chú thực hiện", SuaChuaUi.taoVungCuon(txaGhiChu)));
        pnlPhanCong.add(pnlTruong, BorderLayout.CENTER);
        themThanhPhan(pnlNoiDung, pnlPhanCong, 194);
        themThanhPhan(pnlNoiDung, taoThongTin("Hạng mục được phép thực hiện", "Lệnh chỉ chứa công việc đã "
                + "được khách xác nhận. Phát sinh phải có xác nhận riêng."), 62);
        hienTrang("Tạo lệnh và phân công kỹ thuật viên", phieuChon.getMaBaoGia() + " · Khách đã đồng ý.",
                pnlNoiDung, taoHangNut(new String[]{"Về danh sách", "Xem báo giá", "Xác nhận phân công"},
                    new String[]{"DANH_SACH", "BAO_GIA", "PHAN_CONG"}, "PHAN_CONG"), true);
        batDauNhap("PHAN_CONG");
    }

    // Lưu phân công và giữ tài khoản tiếp nhận ở màn danh sách sau khi thành công.
    private void luuPhanCong() {
        LocalDateTime han = LocalDateTime.parse(txtHan.getText().trim(), DateTimeFormatter
                .ofPattern("dd/MM/uuuu HH:mm").withResolverStyle(ResolverStyle.STRICT));
        String phoiHop = cmbPhoiHop.getSelectedIndex() == 0 ? ""
                : cmbPhoiHop.getSelectedIndex() == 1 ? "kythuat" : "kythuatvu";
        phieuDAO.luu(taiKhoan, phieuChon.getMaPhieu(), phieuChon.getMaBaoGia(),
                cmbChinh.getSelectedIndex() == 0 ? "kythuat" : "kythuatvu", phoiHop, han, txaGhiChu.getText());
        hienDanhSach(false, "Đã xác nhận phân công. Kỹ thuật viên xem lệnh bằng tài khoản của mình.");
    }

    /** @param thongBao kết quả thao tác trước đó */
    // Hiển thị tiến độ và trạng thái phản hồi khách từ đúng lệnh đang chọn.
    private void hienTienDo(String thongBao) {
        phieuChon = phieuDAO.timTheoMa(taiKhoan, phieuChon.getMaPhieu()).orElseThrow();
        JPanel pnlNoiDung = taoXepDoc();
        if (!thongBao.isEmpty()) {
            themThanhPhan(pnlNoiDung, taoThongTin("Thông báo", thongBao), 62);
        }
        JPanel pnlThongTin = SuaChuaUi.taoThe("Thông tin lệnh");
        JPanel pnlTruong = taoLuoi(2, 2);
        pnlTruong.add(SuaChuaUi.taoTruong("Kỹ thuật viên chính",
                SuaChuaUi.taoNhan(SuaChuaUi.layTenKyThuat(phieuChon.getKyThuatChinh()), false)));
        pnlTruong.add(SuaChuaUi.taoTruong("Báo giá", SuaChuaUi.taoNhan(phieuChon.getMaBaoGia()
                + (phieuChon.getPhatSinh() != null
                    && phieuChon.getPhatSinh().getTrangThai() == TrangThaiPhatSinh.DA_DUYET ? " V2" : " V1")
                + " · Đã đồng ý", false)));
        pnlTruong.add(SuaChuaUi.taoTruong("Hạn dự kiến", SuaChuaUi.taoNhan(phieuChon.getHanHoanThanh()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), false)));
        pnlTruong.add(SuaChuaUi.taoTruong("Trạng thái",
                SuaChuaUi.taoNhan(phieuChon.getTrangThai().getTenHienThi(), false)));
        pnlThongTin.add(pnlTruong, BorderLayout.CENTER);
        themThanhPhan(pnlNoiDung, pnlThongTin, 142);
        DefaultTableModel modelTienDo = SuaChuaUi.taoModel(new String[]{"Công việc", "Người thực hiện",
            "Kết quả", "Trạng thái"});
        StringBuilder ketQua = new StringBuilder();
        for (HangMucSuaChua hangMuc : phieuChon.getDanhSachHangMuc()) {
            modelTienDo.addRow(new Object[]{hangMuc.getTenHangMuc(),
                SuaChuaUi.layTenKyThuat(hangMuc.getTaiKhoanThucHien()), hangMuc.getKetQua(), hangMuc.getTrangThai()});
            if (!hangMuc.getKetQua().isBlank()) {
                ketQua.append(hangMuc.getKetQua()).append("; ");
            }
        }
        tblHangMuc = SuaChuaUi.taoBang(modelTienDo);
        tblHangMuc.setRowSelectionInterval(0, 0);
        JPanel pnlBang = SuaChuaUi.taoThe("Tiến độ hạng mục");
        pnlBang.add(SuaChuaUi.taoVungCuon(tblHangMuc), BorderLayout.CENTER);
        themThanhPhan(pnlNoiDung, pnlBang, 138);
        PhatSinhSuaChua phatSinh = phieuChon.getPhatSinh();
        String tieuDe = "Chi tiết tiến độ sửa chữa";
        String noiDung = "Nếu có đề xuất chưa được khách đồng ý, chỉ tạm dừng phần việc liên quan.";
        if (phatSinh != null) {
            noiDung = phatSinh.getNoiDung()[0] + " · " + phatSinh.getTrangThai().getTenHienThi();
            if (phatSinh.getTrangThai() != TrangThaiPhatSinh.CHO_DUYET) {
                noiDung += "\n" + phatSinh.getNguoiXacNhan() + " · " + phatSinh.getThoiDiemXacNhan()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                tieuDe = phatSinh.getTrangThai() == TrangThaiPhatSinh.DA_DUYET
                        ? "Sửa chữa — phát sinh đã được duyệt" : "Sửa chữa — khách từ chối phát sinh";
                noiDung += phatSinh.getTrangThai() == TrangThaiPhatSinh.DA_DUYET
                        ? "\nChỉ thực hiện phần đã duyệt; ghi nhận thực dùng tại nhánh phụ tùng."
                        : "\nKhông thực hiện, xuất phụ tùng hoặc tính tiền phần khách từ chối.";
            }
        }
        if (thongBao.startsWith("Phát sinh chưa được duyệt")) {
            tieuDe = "Phát sinh chưa được duyệt";
        }
        themThanhPhan(pnlNoiDung, taoThongTin("Phát sinh", noiDung), phatSinh == null ? 62 : 100);
        themThanhPhan(pnlNoiDung, taoThongTin("Kết quả đã lưu", ketQua.toString()), 82);
        boolean duocSua = taiKhoan.getVaiTro() == VaiTro.NHAN_VIEN_KY_THUAT
                && phieuChon.getTrangThai() != TrangThaiSuaChua.HOAN_THANH;
        hienTrang(tieuDe, phieuChon.getMaPhieu() + " · " + phieuChon.getBienSo()
                + " · Chỉ thực hiện hạng mục đã được khách duyệt.", pnlNoiDung, duocSua
                    ? taoHangNut(new String[]{"Về danh sách", "Ghi nhận phụ tùng", "Báo phát sinh",
                        "Kiểm tra hoàn thành", "Cập nhật công việc"},
                        new String[]{"DANH_SACH", "PHU_TUNG", "PHAT_SINH", "KIEM_TRA", "CAP_NHAT"}, "CAP_NHAT")
                    : taoHangNut(new String[]{"Về danh sách"}, new String[]{"DANH_SACH"}, ""), true);
    }

    // Nạp đúng dòng công việc được chọn vào biểu mẫu cập nhật tiến độ.
    private void hienCapNhat() {
        int dong = tblHangMuc.getSelectedRow();
        if (dong < 0) {
            throw new IllegalArgumentException("Hãy chọn công việc cần cập nhật.");
        }
        viTriHangMuc = tblHangMuc.convertRowIndexToModel(dong);
        HangMucSuaChua hangMuc = phieuChon.getDanhSachHangMuc().get(viTriHangMuc);
        cmbTienDo.setSelectedItem(hangMuc.getTrangThai() == TrangThaiSuaChua.CHO_SUA_CHUA
                ? TrangThaiSuaChua.DANG_SUA : hangMuc.getTrangThai());
        txaKetQua.setText(hangMuc.getKetQua());
        JPanel pnlNoiDung = taoXepDoc();
        JPanel pnlThe = SuaChuaUi.taoThe("Kết quả công việc");
        JPanel pnlTruong = taoLuoi(2, 1);
        pnlTruong.add(SuaChuaUi.taoTruong("Trạng thái *", cmbTienDo));
        pnlTruong.add(SuaChuaUi.taoTruong("Kết quả thực hiện *", SuaChuaUi.taoVungCuon(txaKetQua)));
        pnlThe.add(pnlTruong, BorderLayout.CENTER);
        themThanhPhan(pnlNoiDung, pnlThe, 228);
        hienTrang("Cập nhật tiến độ công việc", phieuChon.getMaPhieu() + " · " + hangMuc.getTenHangMuc(),
                pnlNoiDung, taoHangNut(new String[]{"Hủy", "Lưu tiến độ"},
                    new String[]{"HUY", "LUU_TIEN_DO"}, "LUU_TIEN_DO"), true);
        batDauNhap("TIEN_DO");
    }

    // Tạo biểu mẫu đề xuất phát sinh và giải thích điều kiện khách phải đồng ý trước.
    private void hienPhatSinh() {
        if (phieuChon.getPhatSinh() != null) {
            throw new IllegalStateException("Lệnh đã có đề xuất. Xem trạng thái phản hồi tại chi tiết lệnh.");
        }
        JPanel pnlNoiDung = taoXepDoc();
        JPanel pnlThe = SuaChuaUi.taoThe("Hư hỏng phát hiện");
        JPanel pnlTruong = taoLuoi(3, 2);
        String[] nhan = {"Bộ phận / hư hỏng *", "Nguyên nhân phát hiện *", "Dịch vụ / phụ tùng đề xuất *",
            "Thời gian dự kiến thêm"};
        String[] macDinh = {"Rotuyn lái bị rơ", "Phát hiện khi kiểm tra bổ sung gầm xe.",
            "Thay 1 rotuyn lái, tiền công thay.", "2 giờ"};
        for (int viTri = 0; viTri < txtPhatSinh.length; viTri++) {
            txtPhatSinh[viTri] = new JTextField(macDinh[viTri]);
            pnlTruong.add(SuaChuaUi.taoTruong(nhan[viTri], txtPhatSinh[viTri]));
        }
        txaPhatSinh.setText("Tạm dừng phần rotuyn; tiếp tục các hạng mục đã duyệt khác.");
        pnlTruong.add(SuaChuaUi.taoTruong("Ghi chú", SuaChuaUi.taoVungCuon(txaPhatSinh)));
        pnlThe.add(pnlTruong, BorderLayout.CENTER);
        themThanhPhan(pnlNoiDung, pnlThe, 254);
        themThanhPhan(pnlNoiDung, taoThongTin("Chưa được phép thực hiện", "Nhân viên tiếp nhận phải báo chi phí "
                + "và ghi nhận khách đồng ý trước khi kỹ thuật làm thêm."), 62);
        hienTrang("Báo hư hỏng phát sinh", phieuChon.getMaPhieu() + " · Gửi tiếp nhận để liên hệ khách.",
                pnlNoiDung, taoHangNut(new String[]{"Quay lại lệnh", "Gửi nhân viên tiếp nhận"},
                    new String[]{"HUY", "GUI_PHAT_SINH"}, "GUI_PHAT_SINH"), true);
        batDauNhap("PHAT_SINH");
    }

    // Tạo bước kiểm tra cuối với các xác nhận chưa chọn sẵn và kết luận có thể chỉnh sửa.
    private void hienHoanTat() {
        JPanel pnlNoiDung = taoXepDoc();
        JPanel pnlThe = SuaChuaUi.taoThe("Kiểm tra cuối");
        JPanel pnlDieuKien = taoLuoi(4, 1);
        String[] nhan = {"Hạng mục đã duyệt được thực hiện và kiểm tra lại.",
            "Phụ tùng thực tế đã được đối chiếu với hồ sơ thực dùng.",
            "Phát sinh chưa duyệt không được thực hiện hoặc tính tiền.",
            "Phụ tùng cũ được giữ để trả theo yêu cầu của khách."};
        for (int viTri = 0; viTri < chkKiemTra.length; viTri++) {
            chkKiemTra[viTri] = new JCheckBox(nhan[viTri]);
            chkKiemTra[viTri].setFont(UiTheme.SHELL_FONT);
            chkKiemTra[viTri].setOpaque(false);
            pnlDieuKien.add(chkKiemTra[viTri]);
        }
        pnlThe.add(pnlDieuKien, BorderLayout.CENTER);
        themThanhPhan(pnlNoiDung, pnlThe, 166);
        JPanel pnlKetQua = SuaChuaUi.taoThe("Kết quả hoàn tất");
        JPanel pnlTruong = taoLuoi(2, 2);
        String[] nhanKetQua = {"Kết luận *", "Thông tin bảo hành", "Khuyến nghị"};
        String[] macDinh = {"Xe vận hành ổn định; tiếng kêu phanh đã xử lý.",
            "Theo hạng mục và phiếu/hóa đơn của garage.", "Kiểm tra định kỳ và bảo dưỡng theo hướng dẫn."};
        for (int viTri = 0; viTri < txtHoanTat.length; viTri++) {
            txtHoanTat[viTri] = new JTextField(macDinh[viTri]);
            pnlTruong.add(SuaChuaUi.taoTruong(nhanKetQua[viTri], txtHoanTat[viTri]));
        }
        pnlKetQua.add(pnlTruong, BorderLayout.CENTER);
        themThanhPhan(pnlNoiDung, pnlKetQua, 154);
        themThanhPhan(pnlNoiDung, taoThongTin("Chuyển sang Hoàn thành", "Sau xác nhận, phiếu sẵn sàng để thu ngân "
                + "đối chiếu. Chưa chuyển thành Đã thanh toán; nhánh hóa đơn chưa tích hợp."), 62);
        boolean daDuyet = phieuChon.getPhatSinh() != null
                && phieuChon.getPhatSinh().getTrangThai() == TrangThaiPhatSinh.DA_DUYET;
        hienTrang(daDuyet ? "Kiểm tra hoàn tất — gồm phát sinh được duyệt" : "Kiểm tra và hoàn tất sửa chữa",
                phieuChon.getMaPhieu() + " · Đối chiếu công việc và vật tư trước khi chuyển thu ngân.", pnlNoiDung,
                taoHangNut(new String[]{"Quay lại chỉnh sửa", "Xác nhận hoàn thành"},
                    new String[]{"HUY", "HOAN_TAT"}, "HOAN_TAT"), true);
        batDauNhap("HOAN_TAT");
    }

    // Lấy giá trị thực của các xác nhận và kết luận để DAO kiểm tra trước khi hoàn tất.
    private void xacNhanHoanTat() {
        boolean[] daDoiChieu = new boolean[chkKiemTra.length];
        String[] ketQua = new String[txtHoanTat.length];
        for (int viTri = 0; viTri < daDoiChieu.length; viTri++) {
            daDoiChieu[viTri] = chkKiemTra[viTri].isSelected();
        }
        for (int viTri = 0; viTri < ketQua.length; viTri++) {
            ketQua[viTri] = txtHoanTat[viTri].getText().trim();
        }
        phieuDAO.hoanTat(taiKhoan, phieuChon.getMaPhieu(), ketQua, daDoiChieu);
        hienTienDo("Lệnh đã hoàn thành. Kết luận: " + ketQua[0]);
    }

    /** @return năm trường đề xuất */
    // Đọc nội dung phát sinh theo đúng thứ tự các trường trên biểu mẫu.
    private String[] layNoiDungPhatSinh() {
        return new String[]{txtPhatSinh[0].getText().trim(), txtPhatSinh[1].getText().trim(),
            txtPhatSinh[2].getText().trim(), txtPhatSinh[3].getText().trim(), txaPhatSinh.getText().trim()};
    }

    /** @param cheDo tên biểu mẫu */
    // Ghi dấu vết ngay sau khi nạp biểu mẫu để phát hiện thay đổi chưa lưu.
    private void batDauNhap(String cheDo) {
        cheDoNhap = cheDo;
        dauVetBanDau = layDauVet();
    }

    /** @return có được rời biểu mẫu */
    // Chỉ hỏi bỏ thay đổi khi nội dung hiện tại khác nội dung lúc mở biểu mẫu.
    private boolean duocRoiBieuMau() {
        return cheDoNhap.isEmpty() || dauVetBanDau.equals(layDauVet()) || SuaChuaUi.xacNhan(winChu,
                "Bỏ thay đổi chưa lưu?", "Thông tin đang nhập chưa được lưu.", "Bỏ thay đổi", "Tiếp tục nhập");
    }

    /** @return chuỗi giá trị hiện tại */
    // Tạo dấu vết từ các trường thuộc đúng biểu mẫu đang được mở.
    private String layDauVet() {
        if ("PHAN_CONG".equals(cheDoNhap)) {
            return cmbChinh.getSelectedIndex() + "|" + cmbPhoiHop.getSelectedIndex()
                    + "|" + txtHan.getText() + "|" + txaGhiChu.getText();
        }
        if ("TIEN_DO".equals(cheDoNhap)) {
            return cmbTienDo.getSelectedIndex() + "|" + txaKetQua.getText();
        }
        if ("PHAT_SINH".equals(cheDoNhap)) {
            return java.util.Arrays.toString(layNoiDungPhatSinh());
        }
        StringBuilder dauVet = new StringBuilder();
        for (JTextField txtTruong : txtHoanTat) {
            if (txtTruong != null) {
                dauVet.append(txtTruong.getText()).append("|");
            }
        }
        for (JCheckBox chkDieuKien : chkKiemTra) {
            if (chkDieuKien != null) {
                dauVet.append(chkDieuKien.isSelected()).append("|");
            }
        }
        return dauVet.toString();
    }

    /**
     * @param soDong số hàng
     * @param soCot số cột
     * @return lưới không nền
     */
    // Tạo lưới cho các nhóm trường nhập theo khoảng cách của thiết kế.
    private JPanel taoLuoi(int soDong, int soCot) {
        JPanel pnlLuoi = new JPanel(new GridLayout(soDong, soCot, 12, 8));
        pnlLuoi.setOpaque(false);
        return pnlLuoi;
    }

    /**
     * @param tieuDe tiêu đề
     * @param noiDung văn bản
     * @return thẻ thông tin
     */
    // Tạo thẻ văn bản có tự xuống dòng cho cảnh báo và kết quả nghiệp vụ.
    private JPanel taoThongTin(String tieuDe, String noiDung) {
        JPanel pnlThe = SuaChuaUi.taoThe(tieuDe);
        pnlThe.add(SuaChuaUi.taoVanBan(noiDung), BorderLayout.CENTER);
        if ("Chuyển sang Hoàn thành".equals(tieuDe) || "Hạng mục được phép thực hiện".equals(tieuDe)) {
            pnlThe.setBackground(new java.awt.Color(0xEDF8F0));
        } else if ("Phát sinh".equals(tieuDe) || "Chưa được phép thực hiện".equals(tieuDe)) {
            pnlThe.setBackground(UiTheme.WINDOW);
        } else if (noiDung.startsWith("Phát sinh chưa được duyệt")) {
            pnlThe.setBackground(new java.awt.Color(0xFFF1F1));
        }
        return pnlThe;
    }

    /** @return panel xếp dọc */
    // Tạo vùng chứa các thẻ theo chiều dọc bằng BoxLayout.
    private JPanel taoXepDoc() {
        JPanel pnlDoc = new JPanel();
        pnlDoc.setLayout(new BoxLayout(pnlDoc, BoxLayout.Y_AXIS));
        pnlDoc.setOpaque(false);
        return pnlDoc;
    }

    /**
     * @param pnlCha nơi chứa
     * @param thanhPhan thành phần con
     * @param cao chiều cao pixel
     */
    // Thêm thành phần có chiều cao thiết kế và khoảng cách tám pixel giữa các thẻ.
    private void themThanhPhan(JPanel pnlCha, JComponent thanhPhan, int cao) {
        if (pnlCha.getComponentCount() > 0) {
            pnlCha.add(Box.createVerticalStrut(8));
        }
        thanhPhan.setAlignmentX(Component.LEFT_ALIGNMENT);
        thanhPhan.setPreferredSize(new Dimension(0, cao));
        thanhPhan.setMinimumSize(new Dimension(0, cao));
        thanhPhan.setMaximumSize(new Dimension(Integer.MAX_VALUE, cao));
        pnlCha.add(thanhPhan);
    }

    /**
     * @param nhan tên các nút
     * @param lenh mã các thao tác
     * @param chinh mã nút chính
     * @return hàng nút
     */
    // Tạo các nút dùng chung ActionListener của màn hình.
    private JPanel taoHangNut(String[] nhan, String[] lenh, String chinh) {
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.setOpaque(false);
        for (int viTri = 0; viTri < nhan.length; viTri++) {
            pnlNut.add(SuaChuaUi.taoNut(nhan[viTri], lenh[viTri], lenh[viTri].equals(chinh), this));
        }
        return pnlNut;
    }

    /**
     * @param tieuDe tên trang
     * @param moTa mô tả
     * @param pnlNoiDung nội dung
     * @param pnlNut hành động cuối trang
     * @param nenTrang dùng nền trắng
     */
    // Đổi nội dung trong cùng JPanel và giữ layout, đăng nhập của MainFrame.
    private void hienTrang(String tieuDe, String moTa, JPanel pnlNoiDung, JPanel pnlNut, boolean nenTrang) {
        cheDoNhap = "";
        removeAll();
        JPanel pnlDau = new JPanel(new BorderLayout(0, 8));
        pnlDau.setOpaque(false);
        JLabel lblTieuDe = SuaChuaUi.taoNhan(tieuDe, true);
        lblTieuDe.setFont(UiTheme.HEADING_FONT);
        pnlDau.add(lblTieuDe, BorderLayout.NORTH);
        if (!moTa.isEmpty()) {
            JLabel lblMoTa = SuaChuaUi.taoNhan(moTa, false);
            lblMoTa.setForeground(UiTheme.MUTED);
            lblMoTa.setFont(UiTheme.CAPTION_FONT);
            pnlDau.add(lblMoTa, BorderLayout.SOUTH);
        }
        add(pnlDau, BorderLayout.NORTH);
        JScrollPane scrNoiDung = new JScrollPane(pnlNoiDung);
        scrNoiDung.setBorder(BorderFactory.createEmptyBorder());
        scrNoiDung.getViewport().setBackground(nenTrang ? UiTheme.SURFACE : UiTheme.WINDOW);
        scrNoiDung.getVerticalScrollBar().setUnitIncrement(16);
        add(scrNoiDung, BorderLayout.CENTER);
        if (pnlNut != null) {
            add(pnlNut, BorderLayout.SOUTH);
        }
        revalidate();
        repaint();
    }
}