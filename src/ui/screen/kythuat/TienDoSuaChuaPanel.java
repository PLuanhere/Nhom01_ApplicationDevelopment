package ui.screen.kythuat;

import entity.PhieuSuaChua.ChiTietSuaChua;
import entity.PhieuSuaChua;
import entity.PhieuSuaChua.TrangThaiSuaChua;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import static ui.screen.kythuat.GiaoDienKyThuat.*;

// Hiển thị tiến độ và kết quả thực hiện của từng hạng mục.
public final class TienDoSuaChuaPanel extends TrangKyThuatPanel {

    private static final long serialVersionUID = 1L;

    public TienDoSuaChuaPanel(KhongGianKyThuatPanel khongGian) {
        super(khongGian);
    }

    // Hiển thị các hạng mục và biểu mẫu cập nhật tiến độ.
    public void hienTienDo() {
        PhieuSuaChua lenh = khongGian.dao.docLenh(khongGian.maLenh);

        JPanel pnlTrang = taoTrang(
                "Tiến độ sửa chữa",
                "Chỉ thao tác trong phạm vi hạng mục khách đã xác nhận.",
                "Danh sách / quay lại",
                "PHAN_CONG");

        themThanhPhan(pnlTrang, taoThanhBuoc(
                "Nhận lệnh", "Thực hiện", "Kiểm tra hoàn tất"));

        JPanel pnlCot = taoCot();

        themThanhPhan(pnlCot, taoHang(
                taoGiaTri("Biển số", lenh.getBienSo()),
                taoGiaTri("Kỹ thuật viên", khongGian.taiKhoan.getTenDangNhap()),
                taoGiaTri("Trạng thái lệnh", khongGian.dao.docTrangThai(lenh))));

        DefaultTableModel model = taoModel(
                "Hạng mục được duyệt",
                "Tiến độ",
                "Kết quả / ghi chú",
                "Phụ tùng thực dùng");

        for (int viTri = 0; viTri < lenh.getDanhSachHangMuc().size(); viTri++) {
            ChiTietSuaChua hangMuc = lenh.getDanhSachHangMuc().get(viTri);

            model.addRow(new Object[] {
                    hangMuc.getTenHangMuc(),
                    hangMuc.getTrangThai(),
                    hangMuc.getKetQua(),
                    khongGian.docThucDungAnToan(viTri)
            });
        }

        JTable tblBang = taoBang(model);

        khongGian.viTriHangMuc = Math.min(
                khongGian.viTriHangMuc, lenh.getDanhSachHangMuc().size() - 1);

        ChiTietSuaChua hangMuc = lenh.getDanhSachHangMuc().get(khongGian.viTriHangMuc);

        khongGian.txtKetQua = khongGian.taoONhap(hangMuc.getKetQua());
        khongGian.txtKetQua.setEditable(
                lenh.getTrangThai() != TrangThaiSuaChua.HOAN_THANH);

        khongGian.cmbTienDo = new JComboBox<TrangThaiSuaChua>(
                TrangThaiSuaChua.values());

        dinhDangCombo(khongGian.cmbTienDo);
        khongGian.cmbTienDo.setSelectedItem(hangMuc.getTrangThai());
        khongGian.cmbTienDo.setEnabled(
                lenh.getTrangThai() != TrangThaiSuaChua.HOAN_THANH);

        tblBang.setRowSelectionInterval(
                khongGian.viTriHangMuc, khongGian.viTriHangMuc);

        tblBang.addMouseListener(new MouseAdapter() {
            // Kiểm tra nội dung chưa lưu trước khi chuyển sang hạng mục khác.
            @Override
            public void mouseClicked(MouseEvent suKien) {
                int dong = tblBang.rowAtPoint(suKien.getPoint());

                if (dong >= 0 && dong != khongGian.viTriHangMuc
                        && khongGian.duocRoiTrang()) {
                    khongGian.viTriHangMuc = tblBang.convertRowIndexToModel(dong);
                    khongGian.batDauTrang("TIEN_DO");
                    hienTienDo();
                }
            }
        });

        themThanhPhan(pnlCot, bocBang(tblBang));

        themThanhPhan(pnlCot, taoHang(
                taoTruong("Kết quả cập nhật *", khongGian.txtKetQua),
                taoTruong("Trạng thái sau cập nhật", khongGian.cmbTienDo)));

        themThanhPhan(pnlCot, taoThongBao(
                "Các hạng mục phát sinh chưa được khách duyệt phải dừng riêng; "
                        + "không đánh dấu hoàn tất thay cho hạng mục đang chờ.",
                true));

        JButton btnLuu = taoNut(
                "Lưu cập nhật tiến độ", "LUU_TIEN_DO", true, khongGian);

        btnLuu.setEnabled(lenh.getTrangThai() != TrangThaiSuaChua.HOAN_THANH);
        pnlCot.add(taoHangNut(btnLuu));

        themThanhPhan(pnlTrang, taoThe(
                "Cập nhật tiến độ " + khongGian.maLenh,
                "Chọn một dòng để cập nhật; không đổi thay các hạng mục còn lại.",
                pnlCot));

        hienTrang(pnlTrang);
    }
}