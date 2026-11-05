package ui.screen.kythuat;

import javax.swing.JButton;
import javax.swing.JPanel;
import entity.PhieuSuaChua;
import entity.PhieuSuaChua.TrangThaiSuaChua;
import static ui.component.GiaoDienKyThuat.*;

// Hiển thị nội dung phát sinh và biểu mẫu gửi đề xuất.
final class BaoHuHongPhatSinhPanel extends TrangKyThuatPanel {
    private static final long serialVersionUID = 1L;

    BaoHuHongPhatSinhPanel(KhongGianKyThuatPanel khongGian) {
        super(khongGian);
    }

    // Hiển thị phát sinh hiện có hoặc biểu mẫu ghi nhận phát sinh mới.
    void hienPhatSinh() {
        PhieuSuaChua lenh = khongGian.dao.docLenh(khongGian.maLenh);
        JPanel pnlTrang = taoTrang("Báo hư hỏng phát sinh",
                "Tạm dừng hạng mục phát sinh cho đến khi khách đồng ý báo giá bổ sung.",
                "Danh sách / quay lại", "PHAN_CONG");
        boolean chiDoc = lenh.getPhatSinh() != null || lenh.getTrangThai() == TrangThaiSuaChua.HOAN_THANH;
        String[] noiDung = lenh.getPhatSinh() == null
                ? new String[] {"", "", "", "", ""} : lenh.getPhatSinh().getNoiDung();
        khongGian.txtPhatSinh = chiDoc ? khongGian.taoONhap(noiDung[0], true) : khongGian.taoONhap(noiDung[0]);
        khongGian.txtPhuTungDuKien = chiDoc
                ? khongGian.taoONhap(noiDung[2], true) : khongGian.taoONhap(noiDung[2]);
        khongGian.txtMoTa = chiDoc ? khongGian.taoONhap(noiDung[1], true) : khongGian.taoONhap(noiDung[1]);
        JPanel pnlCot = taoCot();
        themThanhPhan(pnlCot, taoHang(taoGiaTri("Mã lệnh", khongGian.maLenh),
                taoGiaTri("Xe", lenh.getBienSo()),
                taoGiaTri("Trạng thái phát sinh", lenh.getPhatSinh() == null ? "Chưa gửi đề xuất"
                        : lenh.getPhatSinh().getTrangThai().getTenHienThi())));
        themThanhPhan(pnlCot, taoHang(taoTruong("Hạng mục phát sinh *", khongGian.txtPhatSinh),
                taoTruong("Phụ tùng dự kiến", khongGian.txtPhuTungDuKien)));
        themThanhPhan(pnlCot, taoHang(taoTruong("Mô tả & bằng chứng *", khongGian.txtMoTa),
                taoTruong("Chi phí dự kiến · chỉ đọc",
                        khongGian.taoONhap("Theo báo giá bổ sung – chưa phê duyệt", true))));
        themThanhPhan(pnlCot, taoThongBao(
                "Không tự chuyển phát sinh thành hạng mục đã duyệt. Tiếp nhận gửi báo giá bổ sung; "
                        + "chỉ tiếp tục sau khi khách đồng ý.", true));
        JButton btnGui = taoNut("Gửi báo cáo phát sinh", "GUI_PHAT_SINH", true, khongGian);
        btnGui.setEnabled(!chiDoc);
        pnlCot.add(taoHangNut(btnGui));
        themThanhPhan(pnlTrang, taoThe("Báo hư hỏng phát sinh",
                "Tạm dừng riêng hạng mục phát sinh, báo Tiếp nhận lập báo giá bổ sung trước khi sửa.", pnlCot));
        hienTrang(pnlTrang);
    }
}