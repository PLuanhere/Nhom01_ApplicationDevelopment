package ui.screen.kythuat;

import entity.PhieuTiepNhan.BaoCaoKiemTra;
import entity.PhieuTiepNhan.TrangThaiKiemTra;
import entity.PhieuSuaChua;
import entity.PhieuSuaChua.TrangThaiSuaChua;
import entity.PhieuTiepNhan;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import ui.component.PhuTungUi;
import ui.component.KyThuatNavigationPanel;

import static ui.component.GiaoDienKyThuat.*;

// Hiển thị tổng quan công việc và các đường tắt của kỹ thuật viên.
public final class TrangChuKyThuatPanel extends TrangKyThuatPanel {

    private static final long serialVersionUID = 1L;

    public TrangChuKyThuatPanel(KhongGianKyThuatPanel khongGian) {
        super(khongGian);
    }

    // Đọc dữ liệu trong phiên và hiển thị tổng quan công việc kỹ thuật.
    public void hienTongQuan() {
        JPanel pnlTrang = taoTrang(
                "Công việc kỹ thuật",
                "Ưu tiên lệnh được giao, kiểm tra xe và cập nhật phụ tùng đã dùng.",
                "Tác vụ thường dùng",
                "PHAN_CONG");

        int dangSua = 0;
        int choPhuTung = 0;
        int hoanTat = 0;

        List<PhieuSuaChua> cacLenh = khongGian.dao.docLenh("", "Tất cả");

        for (PhieuSuaChua lenh : cacLenh) {
            if ("Chờ phụ tùng".equals(khongGian.dao.docTrangThai(lenh))) {
                choPhuTung++;
            } else if (lenh.getTrangThai() != TrangThaiSuaChua.HOAN_THANH) {
                dangSua++;
            }

            if (lenh.getThoiDiemHoanTat() != null
                    && lenh.getThoiDiemHoanTat().toLocalDate().equals(LocalDate.now())) {
                hoanTat++;
            }
        }

        List<PhieuTiepNhan> cacPhieuCho = new ArrayList<PhieuTiepNhan>();

        for (PhieuTiepNhan phieuCho : khongGian.dao.docPhieu()) {
            BaoCaoKiemTra daCo = phieuCho.isDaXacNhan()
                    ? khongGian.dao.docBaoCao(phieuCho) : null;

            if (phieuCho.isDaXacNhan()
                    && (daCo == null || daCo.getTrangThai() == TrangThaiKiemTra.NHAP)) {
                cacPhieuCho.add(phieuCho);
            }
        }

        themThanhPhan(pnlTrang, taoHang(
                taoTheSo("Đang sửa", dangSua, "Lệnh thuộc phạm vi phân công"),
                taoTheSo("Xe chờ kiểm tra", cacPhieuCho.size(), "Ưu tiên theo thời điểm tiếp nhận"),
                taoTheSo("Chờ phụ tùng", choPhuTung, "Không xuất vượt tồn kho"),
                taoTheSo("Hoàn tất hôm nay", hoanTat, "Đã ghi nhận kết quả")));

        JPanel pnlTrai = taoCot();
        JPanel pnlLenh = taoCot();

        for (PhieuSuaChua lenh : cacLenh) {
            JButton btnLenh = taoNut(
                    "<html><b>" + lenh.getMaPhieu() + " · " + lenh.getBienSo()
                            + "</b><br>" + khongGian.dao.docTrangThai(lenh) + " · Hạn "
                            + lenh.getHanHoanThanh().format(
                                    DateTimeFormatter.ofPattern("dd/MM HH:mm"))
                            + "</html>",
                    "LENH:" + lenh.getMaPhieu(),
                    false,
                    new ActionListener() {
                        // Chọn lệnh và gửi yêu cầu mở tiến độ về MainFrame.
                        @Override
                        public void actionPerformed(ActionEvent suKien) {
                            khongGian.maLenh = lenh.getMaPhieu();
                            khongGian.viTriHangMuc = 0;
                            khongGian.chuyen("TIEN_DO");
                        }
                    });

            btnLenh.setHorizontalAlignment(SwingConstants.LEFT);
            btnLenh.setPreferredSize(new Dimension(0, 64));
            btnLenh.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
            pnlLenh.add(btnLenh);
        }

        themThanhPhan(pnlTrai, taoThe(
                "Lệnh được phân công",
                "Xem nhanh tình trạng và hạn xử lý",
                pnlLenh));

        JPanel pnlCho = taoCot();

        for (PhieuTiepNhan phieuCho : cacPhieuCho) {
            JButton btnPhieu = taoNut(
                    "<html>" + phieuCho.getMaPhieu() + " · " + phieuCho.getBienSo()
                            + "<br>" + phieuCho.getYeuCau() + "</html>",
                    "MO_PHIEU",
                    false,
                    new ActionListener() {
                        // Mở báo cáo kiểm tra của phiếu tiếp nhận được chọn.
                        @Override
                        public void actionPerformed(ActionEvent suKien) {
                            khongGian.pnlKiemTraXe.moPhieu(phieuCho);
                        }
                    });

            btnPhieu.setHorizontalAlignment(SwingConstants.LEFT);
            btnPhieu.setPreferredSize(new Dimension(0, 64));
            btnPhieu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
            pnlCho.add(btnPhieu);
        }

        themThanhPhan(pnlTrai, taoThe("Xe chờ kiểm tra", "", pnlCho));

        JPanel pnlPhai = taoCot();
        JPanel pnlTacVu = new JPanel(new GridLayout(2, 2, 10, 10));
        pnlTacVu.setOpaque(false);

        pnlTacVu.add(taoNut("Lập báo cáo kiểm tra", "TAO_BAO_CAO", true, khongGian));
        pnlTacVu.add(taoNut("Cập nhật tiến độ", "TIEN_DO", false, khongGian));
        pnlTacVu.add(taoNut("Phụ tùng đã dùng", "PHU_TUNG", false, khongGian));
        pnlTacVu.add(taoNut("Báo phát sinh", "PHAT_SINH", false, khongGian));

        JPanel pnlTacVuCot = taoCot();
        themThanhPhan(pnlTacVuCot, pnlTacVu);
        pnlTacVuCot.add(taoThongBao(
                "Hạng mục phát sinh phải có báo giá được khách chấp thuận trước khi sửa.",
                false));

        themThanhPhan(pnlPhai, taoThe(
                "Công việc tiếp theo",
                "Cập nhật từ một lệnh đang thực hiện",
                pnlTacVuCot));

        JPanel pnlDuongTat = new JPanel(new GridLayout(2, 2, 10, 10));
        pnlDuongTat.setOpaque(false);

        String[] duongTat = {"PHAN_CONG", "KIEM_TRA", "TIEN_DO", "PHU_TUNG"};

        for (String muc : duongTat) {
            pnlDuongTat.add(taoNut(
                    KyThuatNavigationPanel.layNhan(muc), muc, false, khongGian));
        }

        themThanhPhan(pnlPhai, taoThe("Đường tắt", "", pnlDuongTat));
        themThanhPhan(pnlTrang, taoHaiCot(pnlTrai, pnlPhai));

        hienTrang(pnlTrang);
    }

    // Tạo thẻ thống kê gồm nhãn, số lượng và chú thích.
    private JPanel taoTheSo(String nhan, int so, String ghiChu) {
        JPanel pnlThe = taoMatPhang();

        JLabel lblNhan = taoNhan(nhan, false);
        lblNhan.setFont(CHU_NHO);
        lblNhan.setForeground(CHU_PHU);

        JLabel lblSo = taoNhan(String.format("%02d", so), true);
        lblSo.setFont(CHU_DAM.deriveFont(24.0f));

        javax.swing.JTextArea txaChuThich = PhuTungUi.taoMoTa(ghiChu);
        txaChuThich.setFont(CHU_NHO);
        txaChuThich.setForeground(CHU_PHU);

        pnlThe.add(lblNhan, BorderLayout.NORTH);
        pnlThe.add(lblSo, BorderLayout.CENTER);
        pnlThe.add(txaChuThich, BorderLayout.SOUTH);

        return pnlThe;
    }
}