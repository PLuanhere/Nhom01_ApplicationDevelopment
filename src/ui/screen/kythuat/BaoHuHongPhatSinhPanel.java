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
import static ui.component.GiaoDienKyThuat.*;

// Hiển thị nhiều đề xuất của một lệnh và biểu mẫu gửi phát sinh tiếp theo.
final class BaoHuHongPhatSinhPanel extends TrangKyThuatPanel {
    private static final long serialVersionUID = 1L;

    BaoHuHongPhatSinhPanel(KhongGianKyThuatPanel khongGian) {
        super(khongGian);
    }

    // Đọc lại đề xuất và phản hồi từ SQL; lệnh hoàn thành chỉ được xem.
    void hienPhatSinh() {
        PhieuSuaChua lenh = khongGian.dao.docLenh(khongGian.maLenh);
        JPanel pnlTrang = taoTrang("Báo hư hỏng phát sinh", "Chỉ thực hiện phần bổ sung sau khi khách đồng ý giá.",
                "Danh sách / quay lại", "PHAN_CONG");
        List<DeXuatPhatSinh> ds = khongGian.dao.docPhatSinh(khongGian.maLenh);
        DefaultTableModel model = taoModel("Lần", "Hư hỏng", "Trạng thái", "Tổng phụ lục", "Người báo");
        for (DeXuatPhatSinh d : ds) {
            model.addRow(new Object[] {d.getSoThuTu(),d.getHuHong(),d.getTenTrangThai(),d.getTongTien(),d.getNguoiBao()});
        }
        JPanel lichSu = taoCot();
        themThanhPhan(lichSu, bocBang(taoBang(model)));
        lichSu.add(taoNut("Xem chi tiết giá / phản hồi", "", false, new ActionListener() {
            // Kỹ thuật chỉ xem phụ lục và quyết định khách, không tự phê duyệt.
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    new ui.dialog.tiepnhan.PhatSinhSuaChuaDialog(khongGian.winChu, khongGian.taiKhoan,
                            khongGian.maLenh).setVisible(true);
                } catch (RuntimeException ex) {
                    khongGian.baoLoi("Phát sinh", ex.getMessage());
                }
            }
        }));
        themThanhPhan(pnlTrang, taoThe("Các phát sinh của " + khongGian.maLenh,
                "Có thể gửi nhiều đề xuất; mỗi đề xuất có giá và quyết định khách riêng.", lichSu));
        boolean chiDoc = lenh.getTrangThai() == TrangThaiSuaChua.HOAN_THANH;
        khongGian.txtPhatSinh = khongGian.taoONhap("", chiDoc);
        khongGian.txtPhuTungDuKien = khongGian.taoONhap("", chiDoc);
        khongGian.txtMoTa = khongGian.taoONhap("", chiDoc);
        JPanel pnlCot = taoCot();
        themThanhPhan(pnlCot, taoHang(taoGiaTri("Mã lệnh", khongGian.maLenh), taoGiaTri("Xe", lenh.getBienSo())));
        themThanhPhan(pnlCot, taoTruong("Hư hỏng phát sinh *", khongGian.txtPhatSinh));
        themThanhPhan(pnlCot, taoTruong("Mô tả / nguyên nhân / bằng chứng *", khongGian.txtMoTa));
        themThanhPhan(pnlCot, taoTruong("Đề xuất xử lý, phụ tùng dự kiến *", khongGian.txtPhuTungDuKien));
        themThanhPhan(pnlCot, taoThongBao(chiDoc ? "Lệnh đã hoàn thành; không gửi thêm phát sinh."
                : "Tiếp nhận lập phụ lục giá và ghi phản hồi khách. Kỹ thuật không tự đồng ý làm thêm.", true));
        JButton btnGui = taoNut("Gửi đề xuất phát sinh mới", "GUI_PHAT_SINH", true, khongGian);
        btnGui.setEnabled(!chiDoc);
        pnlCot.add(taoHangNut(btnGui));
        themThanhPhan(pnlTrang, taoThe("Đề xuất mới", "Chưa ghi nhận xuất kho hoặc thu tiền khi gửi đề xuất.", pnlCot));
        hienTrang(pnlTrang);
    }
}
