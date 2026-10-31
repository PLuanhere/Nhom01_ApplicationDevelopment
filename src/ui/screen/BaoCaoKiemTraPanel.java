package ui.screen;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import dao.BaoCaoKiemTraDAO;
import entity.BaoCaoKiemTra;
import entity.PhieuTiepNhan;
import entity.TaiKhoan;
import entity.TrangThaiKiemTra;
import ui.component.KiemTraUi;
import ui.component.UiTheme;
import ui.dialog.BaoCaoKiemTraDialog;
import ui.dialog.DuLieuLienQuanKiemTraDialog;

// Giao diện nhánh kiểm tra độc lập; nhận phiên đăng nhập và gọi DAO dữ liệu mẫu.
public final class BaoCaoKiemTraPanel extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;
    private final JFrame winChu;
    private final TaiKhoan taiKhoan;
    private final BaoCaoKiemTraDAO baoCaoDAO;
    private final JLabel lblTrangThai = KiemTraUi.taoNhan("", false);
    private final JTextField txtTuKhoa = new JTextField("KT-2026-0112");
    private final JComboBox<String> cmbTrangThai = new JComboBox<String>(
            new String[]{"Tất cả", "Nháp", "Hoàn tất"});
    private final JTextField[] txtKetQua = new JTextField[5];
    private final JCheckBox chkDoiChieu = new JCheckBox("Kết quả đầy đủ, đúng xe và đúng phiếu tiếp nhận.");

    // Dữ liệu tương ứng với các dòng đang hiển thị trên bảng.
    private List<BaoCaoKiemTra> dangHien = new ArrayList<BaoCaoKiemTra>();
    private List<PhieuTiepNhan> phieuCho = new ArrayList<PhieuTiepNhan>();
    private JTable tblBaoCao;
    private JTable tblPhieu;

    // Phiếu và báo cáo đang được xử lý trong luồng lập kết quả.
    private PhieuTiepNhan phieuChon;
    private BaoCaoKiemTra banNhap;
    private BaoCaoKiemTra baoCaoHoanTat;

    // Trạng thái nhập và dấu vết dùng để phát hiện thay đổi chưa lưu.
    private boolean dangNhapKetQua;
    private String dauVetKetQua;

    public BaoCaoKiemTraPanel(JFrame winChu, TaiKhoan taiKhoan) {
        this(winChu, taiKhoan, new BaoCaoKiemTraDAO());
    }

    public BaoCaoKiemTraPanel(JFrame winChu, TaiKhoan taiKhoan, BaoCaoKiemTraDAO nguonChung) {
        this.baoCaoDAO = java.util.Objects.requireNonNull(nguonChung);
        this.winChu = winChu;
        this.taiKhoan = taiKhoan;
        setBackground(UiTheme.WINDOW);
        setLayout(new BorderLayout(8, 8));
        hienDanhSach(false, "");
    }

    /** {@inheritDoc} */
    // Điều phối thao tác giao diện và yêu cầu xác nhận trước khi bỏ nội dung chưa lưu.
    @Override
    public void actionPerformed(ActionEvent suKien) {
        String lenh = suKien.getActionCommand();
        if (("CHO".equals(lenh) || "DANH_SACH".equals(lenh)) && dangNhapKetQua
                && !layDauVetKetQua().equals(dauVetKetQua) && !KiemTraUi.xacNhan(winChu,
                    "Bỏ thay đổi chưa lưu?", "Thông tin đang nhập chưa được lưu.", "Bỏ thay đổi", "Tiếp tục nhập")) {
            return;
        }
        try {
            if ("DANH_SACH".equals(lenh)) {
                hienDanhSach(false, "");
            } else if ("TIM".equals(lenh)) {
                hienTimKiem();
            } else if ("LOC".equals(lenh)) {
                hienDanhSach(true, "");
            } else if ("THEM".equals(lenh)) {
                moBieuMau(null, false);
            } else if ("XEM".equals(lenh)) {
                moBieuMau(layBaoCaoChon(), true);
            } else if ("SUA".equals(lenh)) {
                suaBaoCao();
            } else if ("XOA".equals(lenh)) {
                xoaBaoCao();
            } else if ("LIEN_QUAN".equals(lenh)) {
                hienLienQuan();
            } else if ("CHO".equals(lenh)) {
                hienPhieuCho();
            } else if ("LAP".equals(lenh)) {
                chonPhieu();
            } else if ("DOI_CHIEU".equals(lenh)) {
                xemLai();
            } else if ("CHINH_SUA".equals(lenh)) {
                hienNhapKetQua();
            } else if ("HOAN_TAT".equals(lenh)) {
                hoanTat();
            } else if ("TIEP_NHAN".equals(lenh)) {
                KiemTraUi.xacNhan(winChu, "Phiếu tiếp nhận", baoCaoHoanTat.getPhieu().getMaPhieu()
                        + " · " + baoCaoHoanTat.getPhieu().getBienSo(), "Đóng", null);
            } else if ("BAO_GIA".equals(lenh)) {
                KiemTraUi.xacNhan(winChu, "Lập báo giá", "Báo cáo đã sẵn sàng cho tiếp nhận. "
                        + "Màn hình lập báo giá thuộc nhánh khác, chưa tích hợp.", "Đóng", null);
            }
        } catch (IllegalArgumentException | IllegalStateException | SecurityException loi) {
            KiemTraUi.xacNhan(winChu, "Không thể thực hiện", loi.getMessage(), "Đóng", null);
        }
    }

    /**
     * @param loc có áp dụng bộ lọc
     * @param thongBao kết quả thao tác trước đó
     */
    // Tải danh sách từ DAO và dùng một bảng cho danh sách, kết quả tìm kiếm và trạng thái rỗng.
    private void hienDanhSach(boolean loc, String thongBao) {
        dangHien = baoCaoDAO.timKiem(taiKhoan, loc ? txtTuKhoa.getText() : "",
                loc ? String.valueOf(cmbTrangThai.getSelectedItem()) : "Tất cả");
        DefaultTableModel modelBaoCao = KiemTraUi.taoModel(new String[]{"Số báo cáo", "Phiếu tiếp nhận",
                "Biển số", "Kỹ thuật viên", "Trạng thái"});
        for (BaoCaoKiemTra baoCao : dangHien) {
            modelBaoCao.addRow(new Object[]{baoCao.getMaBaoCao(), baoCao.getPhieu().getMaPhieu(),
                baoCao.getPhieu().getBienSo(), "Chung Ngô Minh Hoàng", baoCao.getTrangThai().getTenHienThi()});
        }
        tblBaoCao = KiemTraUi.taoBang(modelBaoCao);
        if (!dangHien.isEmpty() && thongBao.isEmpty()) {
            tblBaoCao.setRowSelectionInterval(0, 0);
        }
        JPanel pnlNoiDung = taoXepDoc();
        JPanel pnlThaoTac = taoHangNut(new String[]{"Tìm kiếm", "Thêm", "Xem", "Sửa", "Xóa",
                "Dữ liệu liên quan", "Lập kết quả kiểm tra"}, new String[]{"TIM", "THEM", "XEM", "SUA",
                "XOA", "LIEN_QUAN", "CHO"}, "THEM");
        pnlThaoTac.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        themThanhPhan(pnlNoiDung, pnlThaoTac, 30);
        themThanhPhan(pnlNoiDung, KiemTraUi.taoVungCuon(tblBaoCao), 400);
        JTextArea txaQuyTac = KiemTraUi.taoVanBan(KiemTraUi.QUY_TAC);
        txaQuyTac.setFont(UiTheme.CAPTION_FONT);
        themThanhPhan(pnlNoiDung, txaQuyTac, 34);
        lblTrangThai.setForeground(UiTheme.MUTED);
        lblTrangThai.setText(!thongBao.isEmpty() ? thongBao : dangHien.isEmpty()
                ? "Không tìm thấy hồ sơ. Đổi từ khóa hoặc bấm Làm mới."
                : "Có " + dangHien.size() + " hồ sơ. Chọn một dòng để xem, sửa hoặc xóa.");
        themThanhPhan(pnlNoiDung, lblTrangThai, 20);
        String tieuDe = !loc ? "Danh sách báo cáo kiểm tra" : dangHien.isEmpty()
                ? "Không tìm thấy báo cáo kiểm tra" : "Kết quả tìm kiếm báo cáo kiểm tra";
        hienThiTrang(tieuDe, loc ? "Từ khóa đã dùng: " + txtTuKhoa.getText() : "", pnlNoiDung,
                loc ? taoHangNut(new String[]{"Đổi tiêu chí", "Làm mới"},
                    new String[]{"TIM", "DANH_SACH"}, "") : null);
    }

    // Hiển thị tiêu chí tìm kiếm theo bố cục hai thẻ của thiết kế.
    private void hienTimKiem() {
        JPanel pnlNoiDung = taoXepDoc();
        JPanel pnlTieuChi = KiemTraUi.taoThe("Tiêu chí tìm kiếm");
        JPanel pnlTruong = new JPanel(new GridLayout(2, 1, 0, 12));
        pnlTruong.setOpaque(false);
        pnlTruong.add(KiemTraUi.taoTruong("Số báo cáo, phiếu tiếp nhận hoặc biển số", txtTuKhoa));
        pnlTruong.add(KiemTraUi.taoTruong("Trạng thái kiểm tra", cmbTrangThai));
        pnlTieuChi.add(pnlTruong, BorderLayout.CENTER);
        themThanhPhan(pnlNoiDung, pnlTieuChi, 190);
        themThanhPhan(pnlNoiDung, taoThongTin("Hướng dẫn", "Có thể nhập một phần thông tin. "
                + "Để xem toàn bộ dữ liệu, quay lại màn danh sách."), 82);
        hienThiTrang("Tìm kiếm báo cáo kiểm tra", "Nhập tiêu chí tìm kiếm rồi chuyển sang màn kết quả.", pnlNoiDung,
                taoHangNut(new String[]{"Về danh sách", "Tìm kiếm"}, new String[]{"DANH_SACH", "LOC"}, "LOC"));
    }

    /** @return báo cáo tương ứng dòng đã chọn */
    // Chuyển chỉ số dòng hiển thị sang model trước khi lấy đối tượng báo cáo.
    private BaoCaoKiemTra layBaoCaoChon() {
        int dong = tblBaoCao.getSelectedRow();
        if (dong < 0) {
            throw new IllegalArgumentException("Chưa chọn dòng. Hãy chọn một báo cáo trong bảng.");
        }
        return dangHien.get(tblBaoCao.convertRowIndexToModel(dong));
    }

    /**
     * @param baoCao hồ sơ cần xem hoặc sửa, null khi thêm
     * @param chiDoc chỉ xem
     */
    // Mở biểu mẫu modal và chỉ tải lại danh sách sau khi DAO lưu thành công.
    private void moBieuMau(BaoCaoKiemTra baoCao, boolean chiDoc) {
        BaoCaoKiemTraDialog winBieuMau = new BaoCaoKiemTraDialog(winChu, baoCaoDAO, taiKhoan, baoCao, chiDoc);
        winBieuMau.setVisible(true);
        if (winBieuMau.getKetQua() != null) {
            hienDanhSach(false, "Đã lưu. Bảng đã được tải lại.");
        }
    }

    // Chặn sửa báo cáo hoàn tất trước khi mở biểu mẫu sửa.
    private void suaBaoCao() {
        BaoCaoKiemTra baoCao = layBaoCaoChon();
        if (baoCao.getTrangThai() != TrangThaiKiemTra.NHAP) {
            throw new IllegalStateException("Chỉ được sửa báo cáo nháp thuộc phân công.");
        }
        moBieuMau(baoCao, false);
    }

    // Xác nhận xóa nháp và giữ nguyên báo cáo đã hoàn tất hoặc có liên kết.
    private void xoaBaoCao() {
        BaoCaoKiemTra baoCao = layBaoCaoChon();
        if (baoCao.getTrangThai() != TrangThaiKiemTra.NHAP) {
            KiemTraUi.xacNhan(winChu, "Không thể xóa dữ liệu", "Không thể xóa " + baoCao.getMaBaoCao()
                    + " vì đã có dữ liệu liên quan.\n\n" + KiemTraUi.QUY_TAC, "Đóng", null);
        } else if (KiemTraUi.xacNhan(winChu, "Xác nhận xóa", "Xóa " + baoCao.getMaBaoCao() + " — "
                + baoCao.getPhieu().getMaPhieu() + "?\n\n"
                + "Hồ sơ trong tình huống này chưa có dữ liệu liên quan / chưa ghi sổ. "
                + "Chỉ xóa sau khi bạn xác nhận. " + KiemTraUi.QUY_TAC, "Xóa", "Hủy")) {
            baoCaoDAO.xoa(taiKhoan, baoCao);
            hienDanhSach(false, "Đã xóa " + baoCao.getMaBaoCao() + ". Chưa chọn dòng mới.");
        }
    }

    // Mở chi tiết chỉ đọc dạng dialog theo KT_RH, không chuyển sang màn hoàn tất kiểm tra.
    private void hienLienQuan() {
        BaoCaoKiemTra baoCao = layBaoCaoChon();
        DuLieuLienQuanKiemTraDialog winLienQuan = new DuLieuLienQuanKiemTraDialog(winChu, baoCao);
        winLienQuan.setVisible(true);
        if (winLienQuan.isYeuCauXoa()) {
            xoaBaoCao();
        }
    }

    // Chỉ đưa phiếu chưa có báo cáo hoàn tất vào danh sách chờ để tránh kiểm tra trùng.
    private void hienPhieuCho() {
        phieuCho = new ArrayList<PhieuTiepNhan>();
        DefaultTableModel modelPhieu = KiemTraUi.taoModel(new String[]{
            "Phiếu tiếp nhận", "Biển số", "Yêu cầu", "Trạng thái"});
        for (PhieuTiepNhan phieu : baoCaoDAO.layPhieu(taiKhoan)) {
            BaoCaoKiemTra baoCao = phieu.isDaXacNhan()
                    ? baoCaoDAO.timTheoPhieu(taiKhoan, phieu).orElse(null) : null;
            if (baoCao == null || baoCao.getTrangThai() == TrangThaiKiemTra.NHAP) {
                phieuCho.add(phieu);
                modelPhieu.addRow(new Object[]{phieu.getMaPhieu(), phieu.getBienSo(), phieu.getYeuCau(),
                    phieu.isDaXacNhan() ? "Chờ kiểm tra" : "Nháp · Chưa hợp lệ"});
            }
        }
        tblPhieu = KiemTraUi.taoBang(modelPhieu);
        JPanel pnlNoiDung = taoXepDoc();
        JPanel pnlThe = KiemTraUi.taoThe("Phiếu đã được phân công kiểm tra");
        pnlThe.add(KiemTraUi.taoVungCuon(tblPhieu), BorderLayout.CENTER);
        themThanhPhan(pnlNoiDung, pnlThe, 166);
        hienThiTrang("Xe chờ kiểm tra", "Chọn một phiếu tiếp nhận hợp lệ để lập báo cáo kiểm tra.", pnlNoiDung,
                taoHangNut(new String[]{"Về danh sách", "Lập báo cáo kiểm tra"},
                    new String[]{"DANH_SACH", "LAP"}, "LAP"));
    }

    // Kiểm tra phiếu được chọn rồi nạp dữ liệu của chính phiếu đó vào biểu mẫu.
    private void chonPhieu() {
        int dong = tblPhieu.getSelectedRow();
        if (dong < 0) {
            throw new IllegalArgumentException("Hãy chọn một phiếu trong bảng.");
        }
        phieuChon = phieuCho.get(tblPhieu.convertRowIndexToModel(dong));
        if (!phieuChon.isDaXacNhan()) {
            JPanel pnlNoiDung = taoXepDoc();
            themThanhPhan(pnlNoiDung, taoThongTin("Phiếu đang chọn", phieuChon.getMaPhieu()
                    + " · Nháp · Chưa xác nhận nội dung tiếp nhận của khách."), 82);
            themThanhPhan(pnlNoiDung, taoThongTin("Cách xử lý", "Nhân viên tiếp nhận cần hoàn tất và xác nhận phiếu. "
                    + "Kỹ thuật viên chọn một phiếu hợp lệ đã được phân công để tiếp tục."), 82);
            hienThiTrang("Phiếu tiếp nhận chưa hợp lệ", "Chưa được phép lập báo cáo kiểm tra cho phiếu này.",
                    pnlNoiDung, taoHangNut(new String[]{"Chọn phiếu khác"}, new String[]{"CHO"}, "CHO"));
            return;
        }
        banNhap = baoCaoDAO.timTheoPhieu(taiKhoan, phieuChon).orElse(null);
        String[] noiDung = banNhap == null ? new String[]{"", "", "",
            "Má phanh còn đạt giới hạn; bụi bám, cần vệ sinh.",
            "Dầu đến kỳ thay, không phát hiện rò rỉ.", "Hoạt động bình thường.", "Không ghi nhận mã lỗi."}
            : banNhap.getNoiDung();
        for (int i = 0; i < 4; i++) {
            txtKetQua[i] = new JTextField(noiDung[i + 3]);
        }
        txtKetQua[4] = new JTextField(banNhap == null
                ? "Bảo dưỡng định kỳ, thay dầu/lọc dầu, vệ sinh phanh." : noiDung[2]);
        dauVetKetQua = layDauVetKetQua();
        hienNhapKetQua();
    }

    // Tạo biểu mẫu kết quả theo các hàng hai cột và giữ nội dung khi quay lại.
    private void hienNhapKetQua() {
        JPanel pnlNoiDung = taoXepDoc();
        themThanhPhan(pnlNoiDung, taoThongTinTiepNhan(), 90);
        JPanel pnlThe = KiemTraUi.taoThe("Kết quả kiểm tra");
        JPanel pnlTruong = new JPanel(new GridLayout(3, 2, 12, 8));
        pnlTruong.setOpaque(false);
        String[] nhan = {"Hệ thống phanh *", "Khoang máy / dầu *", "Điện / điều hòa",
            "Mã lỗi chẩn đoán nếu có", "Hạng mục đề xuất *"};
        for (int i = 0; i < txtKetQua.length; i++) {
            pnlTruong.add(KiemTraUi.taoTruong(nhan[i], txtKetQua[i]));
        }
        pnlThe.add(pnlTruong, BorderLayout.CENTER);
        themThanhPhan(pnlNoiDung, pnlThe, 212);
        hienThiTrang("Lập báo cáo kiểm tra xe", phieuChon.getMaPhieu() + " · " + phieuChon.getBienSo(), pnlNoiDung,
                taoHangNut(new String[]{"Quay lại", "Xem lại báo cáo"},
                    new String[]{"CHO", "DOI_CHIEU"}, "DOI_CHIEU"));
        dangNhapKetQua = true;
    }

    // Kiểm tra trường bắt buộc và xây dựng bản báo cáo để đối chiếu trước khi ghi.
    private void xemLai() {
        String[] noiDung = new String[7];
        for (int i = 0; i < 4; i++) {
            noiDung[i + 3] = txtKetQua[i].getText().trim();
        }
        noiDung[0] = noiDung[3] + "; " + noiDung[4];
        noiDung[1] = "";
        noiDung[2] = txtKetQua[4].getText().trim();
        if (noiDung[3].isEmpty() || noiDung[4].isEmpty() || noiDung[2].isEmpty()) {
            hienThieuKetQua();
            return;
        }
        banNhap = new BaoCaoKiemTra(banNhap == null ? "" : banNhap.getMaBaoCao(), phieuChon,
                LocalDate.of(2026, 10, 5), "Tổng hợp", noiDung, TrangThaiKiemTra.NHAP, null);
        chkDoiChieu.setSelected(false);
        hienKetQua(false, "");
    }

    /**
     * @param daHoanTat hiển thị kết quả cuối
     * @param loi thông báo lỗi nếu có
     */
    // Hiển thị nội dung vừa nhập ở bước đối chiếu, hoàn tất hoặc thử lưu lại.
    private void hienKetQua(boolean daHoanTat, String loi) {
        BaoCaoKiemTra baoCao = daHoanTat ? baoCaoHoanTat : banNhap;
        String[] noiDung = baoCao.getNoiDung();
        JPanel pnlNoiDung = taoXepDoc();
        if (!loi.isEmpty()) {
            themThanhPhan(pnlNoiDung, taoThongTin("Không lưu được báo cáo kiểm tra", loi), 70);
        }
        JPanel pnlPhieu = KiemTraUi.taoThe(daHoanTat ? "Thông tin báo cáo" : "Phiếu liên kết");
        JPanel pnlTruong = new JPanel(new GridLayout(2, 2, 12, 8));
        pnlTruong.setOpaque(false);
        pnlTruong.add(KiemTraUi.taoTruong("Phiếu tiếp nhận",
                KiemTraUi.taoNhan(baoCao.getPhieu().getMaPhieu(), false)));
        pnlTruong.add(KiemTraUi.taoTruong("Biển số", KiemTraUi.taoNhan(baoCao.getPhieu().getBienSo(), false)));
        pnlTruong.add(KiemTraUi.taoTruong("Kỹ thuật viên", KiemTraUi.taoNhan("Chung Ngô Minh Hoàng", false)));
        if (daHoanTat) {
            pnlTruong.add(KiemTraUi.taoTruong("Hoàn tất", KiemTraUi.taoNhan(baoCao.getThoiDiemHoanTat()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), false)));
        }
        pnlPhieu.add(pnlTruong, BorderLayout.CENTER);
        themThanhPhan(pnlNoiDung, pnlPhieu, 130);
        DefaultTableModel modelKetQua = KiemTraUi.taoModel(new String[]{"Bộ phận", "Kết quả", "Đề xuất"});
        if ("Tổng hợp".equals(baoCao.getBoPhan())) {
            modelKetQua.addRow(new Object[]{"Phanh trước", noiDung[3], noiDung[2]});
            modelKetQua.addRow(new Object[]{"Dầu động cơ", noiDung[4], noiDung[2]});
            modelKetQua.addRow(new Object[]{"Điện / điều hòa", noiDung[5], noiDung[2]});
        } else {
            modelKetQua.addRow(new Object[]{baoCao.getBoPhan(), noiDung[0], noiDung[2]});
        }
        JPanel pnlBang = KiemTraUi.taoThe(daHoanTat ? "Kết luận kiểm tra" : "Kết quả và đề xuất");
        pnlBang.add(KiemTraUi.taoVungCuon(KiemTraUi.taoBang(modelKetQua)), BorderLayout.CENTER);
        themThanhPhan(pnlNoiDung, pnlBang, 166);
        if (!daHoanTat) {
            JPanel pnlDoiChieu = KiemTraUi.taoThe("Đối chiếu");
            chkDoiChieu.setOpaque(false);
            chkDoiChieu.setFont(UiTheme.SHELL_FONT);
            pnlDoiChieu.add(chkDoiChieu, BorderLayout.CENTER);
            themThanhPhan(pnlNoiDung, pnlDoiChieu, 68);
        }
        themThanhPhan(pnlNoiDung, taoThongTin(daHoanTat ? "Kiểm tra đã hoàn tất" : "Sau khi xác nhận", daHoanTat
                ? "Nhân viên tiếp nhận lập báo giá từ kết quả này. "
                    + "Khách chưa xác nhận báo giá nên chưa bắt đầu sửa chữa."
                : "Hệ thống sinh mã báo cáo và cung cấp kết quả chính thức cho nhân viên tiếp nhận."), 72);
        hienThiTrang(daHoanTat ? "Báo cáo kiểm tra hoàn tất" : "Đối chiếu báo cáo kiểm tra",
                daHoanTat ? baoCao.getMaBaoCao() + " · Kết quả chính thức đã sẵn sàng để lập báo giá."
                    : "Xem lại kết quả trước khi xác nhận hoàn tất.", pnlNoiDung,
                daHoanTat ? taoHangNut(new String[]{"Về danh sách", "Xem tiếp nhận", "Lập báo giá"},
                        new String[]{"DANH_SACH", "TIEP_NHAN", "BAO_GIA"}, "BAO_GIA")
                    : taoHangNut(new String[]{"Chỉnh sửa", loi.isEmpty() ? "Hoàn tất kiểm tra" : "Thử lưu lại"},
                        new String[]{"CHINH_SUA", "HOAN_TAT"}, "HOAN_TAT"));
    }

    // Chỉ xác nhận hoàn tất sau đối chiếu và giữ nguyên bản nhập nếu DAO từ chối lưu.
    private void hoanTat() {
        if (!chkDoiChieu.isSelected()) {
            throw new IllegalArgumentException("Hãy xác nhận kết quả đầy đủ, đúng xe và đúng phiếu tiếp nhận.");
        }
        try {
            baoCaoHoanTat = baoCaoDAO.luu(taiKhoan, banNhap, true);
            hienKetQua(true, "");
        } catch (IllegalArgumentException | IllegalStateException loi) {
            hienKetQua(false, "Báo cáo chưa được ghi nhận hoàn tất. " + loi.getMessage());
        }
    }

    /** @return thẻ thông tin tiếp nhận của phiếu đang được chọn */
    // Hiển thị yêu cầu khách và tình trạng ban đầu thành hai cột chỉ đọc.
    private JPanel taoThongTinTiepNhan() {
        JPanel pnlThe = KiemTraUi.taoThe("Thông tin được chuyển từ tiếp nhận");
        JPanel pnlTruong = new JPanel(new GridLayout(1, 2, 12, 0));
        pnlTruong.setOpaque(false);
        pnlTruong.add(KiemTraUi.taoTruong("Yêu cầu khách", KiemTraUi.taoVanBan(phieuChon.getYeuCau())));
        pnlTruong.add(KiemTraUi.taoTruong("Tình trạng ban đầu", KiemTraUi.taoVanBan(phieuChon.getTinhTrang())));
        pnlThe.add(pnlTruong, BorderLayout.CENTER);
        return pnlThe;
    }

    // Hiển thị lỗi thiếu kết quả và giữ các ô người dùng đang nhập để bổ sung.
    private void hienThieuKetQua() {
        JPanel pnlNoiDung = taoXepDoc();
        themThanhPhan(pnlNoiDung, taoThongTin("Thiếu kết quả bắt buộc",
                "Hãy bổ sung hệ thống phanh, khoang máy/dầu và hạng mục đề xuất trước khi xem lại báo cáo."), 70);
        themThanhPhan(pnlNoiDung, taoThongTinTiepNhan(), 90);
        JPanel pnlThe = KiemTraUi.taoThe("Kết quả kiểm tra cần bổ sung");
        JPanel pnlTruong = new JPanel(new GridLayout(2, 2, 12, 8));
        pnlTruong.setOpaque(false);
        pnlTruong.add(KiemTraUi.taoTruong("Hệ thống phanh *", txtKetQua[0]));
        pnlTruong.add(KiemTraUi.taoTruong("Hạng mục đề xuất *", txtKetQua[4]));
        pnlTruong.add(KiemTraUi.taoTruong("Khoang máy / dầu *", txtKetQua[1]));
        pnlTruong.add(KiemTraUi.taoTruong("Điện / điều hòa", txtKetQua[2]));
        pnlThe.add(pnlTruong, BorderLayout.CENTER);
        themThanhPhan(pnlNoiDung, pnlThe, 154);
        hienThiTrang("Thiếu kết quả bắt buộc", phieuChon.getMaPhieu() + " · " + phieuChon.getBienSo(),
                pnlNoiDung, taoHangNut(new String[]{"Bổ sung kết quả"}, new String[]{"CHINH_SUA"}, "CHINH_SUA"));
        dangNhapKetQua = true;
    }

    /** @return dấu vết các trường kết quả đang nhập */
    // So sánh nội dung kết quả để xác nhận trước khi bỏ thay đổi chưa lưu.
    private String layDauVetKetQua() {
        StringBuilder dauVet = new StringBuilder();
        for (JTextField txtTruong : txtKetQua) {
            if (txtTruong != null) {
                dauVet.append(txtTruong.getText()).append("|");
            }
        }
        return dauVet.toString();
    }

    /**
     * @param chu tiêu đề
     * @param noiDung nội dung
     * @return thẻ thông tin
     */
    // Tạo thẻ văn bản để trình bày thông tin hoặc trạng thái nghiệp vụ.
    private JPanel taoThongTin(String chu, String noiDung) {
        JPanel pnlThe = KiemTraUi.taoThe(chu);
        pnlThe.add(KiemTraUi.taoVanBan(noiDung), BorderLayout.CENTER);
        if ("Kiểm tra đã hoàn tất".equals(chu)) {
            pnlThe.setBackground(new java.awt.Color(0xEDF8F0));
        } else if (chu.startsWith("Thiếu") || chu.startsWith("Không lưu")) {
            pnlThe.setBackground(new java.awt.Color(0xFFF1F1));
        }
        return pnlThe;
    }

    /** @return panel xếp nội dung theo chiều dọc */
    // Tạo panel dùng BoxLayout để nhóm các phần nội dung và khoảng cách.
    private JPanel taoXepDoc() {
        JPanel pnlDoc = new JPanel();
        pnlDoc.setLayout(new BoxLayout(pnlDoc, BoxLayout.Y_AXIS));
        pnlDoc.setOpaque(false);
        return pnlDoc;
    }

    /**
     * @param pnlCha panel chứa
     * @param thanhPhan nội dung
     * @param cao chiều cao pixel
     */
    // Thêm thành phần có chiều cao thiết kế vào panel xếp dọc.
    private void themThanhPhan(JPanel pnlCha, javax.swing.JComponent thanhPhan, int cao) {
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
     * @param nhan tên nút
     * @param lenh mã sự kiện
     * @param chinh mã nút chính
     * @return hàng nút
     */
    // Tạo hàng nút và đăng ký cùng ActionListener của màn hình.
    private JPanel taoHangNut(String[] nhan, String[] lenh, String chinh) {
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.setOpaque(false);
        for (int i = 0; i < nhan.length; i++) {
            pnlNut.add(KiemTraUi.taoNut(nhan[i], lenh[i], lenh[i].equals(chinh), this));
        }
        return pnlNut;
    }

    /**
     * @param tieuDe tên màn hình
     * @param moTa mô tả
     * @param pnlNoiDung phần giữa
     * @param pnlNut hàng nút cuối
     */
    // Thay nội dung trong cùng panel, giữ nguyên MainFrame và tài khoản của leader.
    private void hienThiTrang(String tieuDe, String moTa, JPanel pnlNoiDung, JPanel pnlNut) {
        dangNhapKetQua = false;
        removeAll();
        JPanel pnlDau = new JPanel(new BorderLayout(0, 8));
        pnlDau.setOpaque(false);
        JLabel lblTieuDe = KiemTraUi.taoNhan(tieuDe, true);
        lblTieuDe.setFont(UiTheme.BRAND_FONT.deriveFont(20.0f));
        lblTieuDe.setPreferredSize(new Dimension(0,
                Math.max(28, lblTieuDe.getFontMetrics(lblTieuDe.getFont()).getHeight() + 4)));
        pnlDau.add(lblTieuDe, BorderLayout.NORTH);
        if (!moTa.isEmpty()) {
            pnlDau.add(KiemTraUi.taoNhan(moTa, false), BorderLayout.SOUTH);
        }
        add(pnlDau, BorderLayout.NORTH);
        JScrollPane scrNoiDung = new JScrollPane(pnlNoiDung);
        scrNoiDung.setBorder(BorderFactory.createEmptyBorder());
        boolean bieuMau = tieuDe.contains("Lập báo cáo") || tieuDe.contains("Đối chiếu")
                || tieuDe.contains("hoàn tất") || tieuDe.contains("Thiếu") || tieuDe.contains("Không lưu");
        pnlNoiDung.setOpaque(bieuMau);
        pnlNoiDung.setBackground(UiTheme.SURFACE);
        scrNoiDung.getViewport().setBackground(bieuMau ? UiTheme.SURFACE : UiTheme.WINDOW);
        scrNoiDung.getVerticalScrollBar().setUnitIncrement(16);
        add(scrNoiDung, BorderLayout.CENTER);
        if (pnlNut != null) {
            add(pnlNut, BorderLayout.SOUTH);
        }
        revalidate();
        repaint();
    }
}