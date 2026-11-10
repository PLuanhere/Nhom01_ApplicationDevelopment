package ui.screen.kythuat;

import entity.DeXuatPhatSinh;
import entity.PhieuSuaChua;
import entity.PhieuSuaChua.TrangThaiSuaChua;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

import ui.dialog.tiepnhan.PhatSinhSuaChuaDialog;

import static ui.component.GiaoDienKyThuat.*;

// Hiển thị lịch sử phát sinh của một lệnh và biểu mẫu gửi đề xuất mới.
public final class BaoHuHongPhatSinhPanel extends TrangKyThuatPanel {

    private static final long serialVersionUID = 1L;

    public BaoHuHongPhatSinhPanel(KhongGianKyThuatPanel khongGian) {
        super(khongGian);
    }

    // Đọc đề xuất và phản hồi từ SQL; lệnh hoàn thành chỉ được xem.
    public void hienPhatSinh() {
        PhieuSuaChua lenh = khongGian.dao.docLenh(khongGian.maLenh);

        JPanel pnlTrang = taoTrang(
                "Báo hư hỏng phát sinh",
                "Chỉ thực hiện phần bổ sung sau khi khách đồng ý giá.",
                "Danh sách / quay lại",
                "PHAN_CONG");

        List<DeXuatPhatSinh> ds = khongGian.dao.docPhatSinh(khongGian.maLenh);

        DefaultTableModel model = taoModel(
                "Lần", "Hư hỏng", "Trạng thái", "Tổng phụ lục", "Người báo");

        for (DeXuatPhatSinh dong : ds) {
            model.addRow(new Object[] {
                    dong.getSoThuTu(),
                    dong.getHuHong(),
                    dong.getTenTrangThai(),
                    dong.getTongTien(),
                    dong.getNguoiBao()
            });
        }

        JPanel pnlLichSu = taoCot();
        themThanhPhan(pnlLichSu, bocBang(taoBang(model)));

        pnlLichSu.add(taoNut(
                "Xem chi tiết giá / phản hồi", "", false, new ActionListener() {
                    // Kỹ thuật chỉ xem phụ lục và quyết định khách, không tự phê duyệt.
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            new PhatSinhSuaChuaDialog(
                                    khongGian.winChu,
                                    khongGian.taiKhoan,
                                    khongGian.maLenh)
                                    .setVisible(true);
                        } catch (RuntimeException ex) {
                            khongGian.baoLoi("Phát sinh", ex.getMessage());
                        }
                    }
                }));

        themThanhPhan(pnlTrang, taoThe(
                "Các phát sinh của " + khongGian.maLenh,
                "Có thể gửi nhiều đề xuất; mỗi đề xuất có giá và quyết định khách riêng.",
                pnlLichSu));

        boolean chiDoc = lenh.getTrangThai() == TrangThaiSuaChua.HOAN_THANH;

        khongGian.txtPhatSinh = khongGian.taoONhap("", chiDoc);
        khongGian.txtPhuTungDuKien = khongGian.taoONhap("", chiDoc);
        khongGian.txtMoTa = khongGian.taoONhap("", chiDoc);

        JPanel pnlCot = taoCot();

        themThanhPhan(pnlCot, taoHang(
                taoGiaTri("Mã lệnh", khongGian.maLenh),
                taoGiaTri("Xe", lenh.getBienSo())));

        themThanhPhan(pnlCot, taoTruong(
                "Hư hỏng phát sinh *", khongGian.txtPhatSinh));

        themThanhPhan(pnlCot, taoTruong(
                "Mô tả / nguyên nhân / bằng chứng *", khongGian.txtMoTa));

        themThanhPhan(pnlCot, taoTruong(
                "Đề xuất xử lý, phụ tùng dự kiến *", khongGian.txtPhuTungDuKien));

        themThanhPhan(pnlCot, taoThongBao(
                chiDoc
                        ? "Lệnh đã hoàn thành; không gửi thêm phát sinh."
                        : "Tiếp nhận lập phụ lục giá và ghi phản hồi khách. "
                                + "Kỹ thuật không tự đồng ý làm thêm.",
                true));

        JButton btnGui = taoNut(
                "Gửi đề xuất phát sinh mới", "GUI_PHAT_SINH", true, khongGian);

        btnGui.setEnabled(!chiDoc);
        pnlCot.add(taoHangNut(btnGui));

        themThanhPhan(pnlTrang, taoThe(
                "Đề xuất mới",
                "Chưa ghi nhận xuất kho hoặc thu tiền khi gửi đề xuất.",
                pnlCot));

        hienTrang(pnlTrang);
    }
}