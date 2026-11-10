package ui.screen.kythuat;

import dao.DuLieuGarageDAO;
import entity.PhuTung;
import entity.PhieuSuaChua.ChiTietSuaChua;
import entity.PhieuSuaChua;
import entity.PhieuSuaChua.TrangThaiSuaChua;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ui.dialog.kythuat.HoanPhuTungDialog;

import static ui.screen.kythuat.GiaoDienKyThuat.*;

// Hiển thị biểu mẫu ghi nhận phụ tùng thực tế đã dùng và mở lịch sử hoàn.
public final class PhuTungSuDungPanel extends TrangKyThuatPanel {

    private static final long serialVersionUID = 1L;

    public PhuTungSuDungPanel(KhongGianKyThuatPanel khongGian) {
        super(khongGian);
    }

    // Hiển thị phụ tùng và hạng mục được phép ghi nhận của lệnh đang chọn.
    public void hienPhuTung() {
        PhieuSuaChua lenh = khongGian.dao.docLenh(khongGian.maLenh);

        JPanel pnlTrang = taoTrang(
                "Phụ tùng sử dụng",
                "Trừ kho theo số lượng thực tế đã dùng, không vượt tồn.",
                "Danh sách / quay lại",
                "PHAN_CONG");

        JPanel pnlCot = taoCot();

        themThanhPhan(pnlCot, taoHang(
                taoGiaTri("Lệnh sửa chữa", khongGian.maLenh),
                taoGiaTri("Biển số", lenh.getBienSo()),
                taoGiaTri("Người ghi nhận", khongGian.taiKhoan.getTenDangNhap())));

        khongGian.cmbHangMuc = new JComboBox<String>();

        for (ChiTietSuaChua hangMuc : lenh.getDanhSachHangMuc()) {
            khongGian.cmbHangMuc.addItem(hangMuc.getTenHangMuc());
        }

        khongGian.cmbPhuTung = new JComboBox<PhuTung>();

        dinhDangCombo(khongGian.cmbHangMuc);
        dinhDangCombo(khongGian.cmbPhuTung);

        khongGian.txtLuong = khongGian.taoONhap("1");
        khongGian.txtTon = khongGian.taoONhap("", true);
        khongGian.txtTonSau = khongGian.taoONhap("", true);

        khongGian.cmbHangMuc.setActionCommand("DOI_HANG_MUC");
        khongGian.cmbHangMuc.addActionListener(khongGian);

        khongGian.cmbPhuTung.setActionCommand("DOI_PHU_TUNG");
        khongGian.cmbPhuTung.addActionListener(khongGian);

        khongGian.cmbHangMuc.setSelectedIndex(
                Math.min(khongGian.viTriHangMuc, khongGian.cmbHangMuc.getItemCount() - 1));

        napPhuTung();

        themThanhPhan(pnlCot, taoHang(
                taoTruong("Phụ tùng *", khongGian.cmbPhuTung),
                taoTruong("Hạng mục được duyệt *", khongGian.cmbHangMuc)));

        themThanhPhan(pnlCot, taoHang(
                taoTruong("Đơn vị / tồn khả dụng · chỉ đọc", khongGian.txtTon),
                taoTruong("Số lượng thực dùng *", khongGian.txtLuong),
                taoTruong("Kết quả sau ghi nhận · chỉ đọc", khongGian.txtTonSau)));

        themThanhPhan(pnlCot, taoThongBao(
                "Nếu số lượng thực dùng lớn hơn tồn khả dụng hoặc hạng mục chưa được duyệt, "
                        + "hệ thống từ chối ghi nhận và không trừ kho.",
                true));

        JButton btnGhi = taoNut(
                "Ghi nhận đã sử dụng", "GHI_PHU_TUNG", true, khongGian);

        btnGhi.setEnabled(lenh.getTrangThai() != TrangThaiSuaChua.HOAN_THANH);

        JButton btnHoan = taoNut(
                "Lịch sử / hoàn phụ tùng", "HOAN", false, new ActionListener() {
                    // Mở lịch sử hoàn rồi tải lại tồn khi cửa sổ đóng.
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        new HoanPhuTungDialog(
                                SwingUtilities.getWindowAncestor(PhuTungSuDungPanel.this),
                                khongGian.taiKhoan,
                                khongGian.maLenh,
                                lenh.getTrangThai() != TrangThaiSuaChua.HOAN_THANH)
                                .setVisible(true);

                        khongGian.chuyen("PHU_TUNG");
                    }
                });

        pnlCot.add(taoHangNut(btnHoan));
        pnlCot.add(taoHangNut(btnGhi));

        khongGian.maLuot = DuLieuGarageDAO.taoMaThaoTac();

        themThanhPhan(pnlTrang, taoThe(
                "Ghi nhận phụ tùng thực tế đã dùng",
                "Chỉ ghi một lần cho cùng lượt sử dụng; giữ chứng từ và lịch sử thực dùng.",
                pnlCot));

        hienTrang(pnlTrang);
    }

    // Nạp danh sách phụ tùng phù hợp với hạng mục được chọn.
    void napPhuTung() {
        khongGian.dangNap = true;
        khongGian.cmbPhuTung.removeAllItems();

        try {
            for (PhuTung phuTung : khongGian.dao.docPhuTung(
                    khongGian.maLenh, khongGian.cmbHangMuc.getSelectedIndex())) {
                khongGian.cmbPhuTung.addItem(phuTung);
            }

            if (khongGian.cmbPhuTung.getItemCount() > 1) {
                khongGian.cmbPhuTung.setSelectedIndex(1);
            }
        } catch (IllegalStateException loi) {
            khongGian.txtTon.setText(loi.getMessage());
        } finally {
            khongGian.dangNap = false;
        }

        capNhatTon();
    }

    // Tính tồn dự kiến để hiển thị mà chưa cập nhật tồn trong SQL.
    void capNhatTon() {
        if (!"PHU_TUNG".equals(khongGian.trang)
                || khongGian.dangNap
                || khongGian.cmbPhuTung == null
                || khongGian.txtTon == null
                || khongGian.txtLuong == null) {
            return;
        }

        PhuTung phuTung = (PhuTung) khongGian.cmbPhuTung.getSelectedItem();

        if (phuTung == null) {
            khongGian.txtTon.setText("Không có phụ tùng hợp lệ cho hạng mục này");
            khongGian.txtTonSau.setText("Không ghi nhận xuất kho");
            return;
        }

        double ton = khongGian.dao.docTon(phuTung.getMaPhuTung());

        khongGian.txtTon.setText(
                phuTung.getDonViTinh() + " · " + PhuTungUi.dinhDangLuong(ton));

        try {
            double luong = PhuTungUi.docSoLuong(khongGian.txtLuong);
            phuTung.kiemTraSoLuong(luong);

            if (luong > ton) {
                khongGian.txtTonSau.setText("Số lượng vượt tồn khả dụng");
            } else {
                double tonSau = Math.round((ton - luong) * 1000) / 1000.0;

                khongGian.txtTonSau.setText(
                        "Tồn dự kiến: " + PhuTungUi.dinhDangLuong(tonSau));
            }
        } catch (IllegalArgumentException loi) {
            khongGian.txtTonSau.setText("Nhập số lượng hợp lệ");
        }
    }

    // Đối chiếu và xác nhận trước khi lưu phụ tùng thực tế đã sử dụng.
    void ghiPhuTung() {
        PhuTung phuTung = (PhuTung) khongGian.cmbPhuTung.getSelectedItem();

        if (phuTung == null) {
            throw new IllegalArgumentException("Chưa có phụ tùng hợp lệ để ghi nhận.");
        }

        double soLuong = PhuTungUi.docSoLuong(khongGian.txtLuong);
        phuTung.kiemTraSoLuong(soLuong);

        if (!khongGian.xacNhan(
                "Ghi nhận phụ tùng đã sử dụng",
                "Gắn phụ tùng vào lệnh và hạng mục được khách đồng ý.",
                new String[][] {
                        {"Lệnh sửa chữa", khongGian.maLenh},
                        {"Hạng mục đã duyệt", (String) khongGian.cmbHangMuc.getSelectedItem()},
                        {"Phụ tùng", phuTung.toString()},
                        {"Đơn vị / tồn khả dụng", khongGian.txtTon.getText()},
                        {"Số lượng thực tế dùng", PhuTungUi.dinhDangLuong(soLuong)},
                        {"Tồn dự kiến sau ghi", khongGian.txtTonSau.getText()}
                },
                "Từ chối nếu vượt tồn hoặc hạng mục chưa được duyệt. "
                        + "Ghi lịch sử xuất một lần cho số thực dùng.",
                "Ghi nhận đã dùng",
                false)) {
            return;
        }

        khongGian.dao.ghiNhan(
                khongGian.maLenh,
                khongGian.cmbHangMuc.getSelectedIndex(),
                phuTung.getMaPhuTung(),
                soLuong,
                khongGian.maLuot);

        khongGian.anhChup = khongGian.docNoiDung();
        khongGian.chuyen("TIEN_DO");
    }
}