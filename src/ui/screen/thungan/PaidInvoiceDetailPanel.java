package ui.screen.thungan;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import dao.HoaDonDAO;
import entity.BaoGia.ChiTietBaoGia;
import entity.HoaDon;
import entity.TaiKhoan;
import ui.component.BaoCaoUi;
import ui.component.UiChung;
import ui.component.UiTheme;

/** Xem và in lại đúng hóa đơn đã thu, không tạo thêm hay sửa chứng từ. */
public final class PaidInvoiceDetailPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final HoaDonDAO dao;
    private final TaiKhoan taiKhoan;
    private final String maHoaDon;
    private final DateTimeFormatter ngay = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");

    /** Đọc hóa đơn cùng chi tiết đã duyệt từ nguồn dùng chung của ứng dụng. */
    public PaidInvoiceDetailPanel(HoaDonDAO dao, TaiKhoan tk, String ma, ActionListener quayLai) {
        super(new BorderLayout());
        this.dao = dao;
        taiKhoan = tk;
        maHoaDon = ma;
        setOpaque(false);
        HoaDon hd = docHoaDon();
        List<ChiTietBaoGia> chiTiet = dao.layChiTietHoaDon(tk, ma);
        JPanel noiDung = BaoCaoUi.taoCot();
        JButton btnQuayLai = UiChung.taoNut("Trở về danh sách", false);
        if (quayLai != null) {
            btnQuayLai.addActionListener(quayLai);
        }
        BaoCaoUi.them(noiDung, UiChung.taoDauTrang("Thu ngân / Chi tiết và in hóa đơn", "Chi tiết & in hóa đơn",
                "Chứng từ đã thanh toán chỉ được xem hoặc in lại.", btnQuayLai));

        // Thông tin đầu chứng từ, giữ nguyên người và thời điểm đã thu.
        JPanel thongTin = BaoCaoUi.taoCot();
        thongTin.add(BaoCaoUi.moTa("Hóa đơn: " + hd.getMaHoaDon() + " · Lệnh: " + hd.getMaPhieuSuaChua()
                + "\nKhách hàng: " + hd.getTenKhachHang() + " · Biển số: " + hd.getBienSo()));
        thongTin.add(BaoCaoUi.moTa("Thanh toán: " + hd.getThoiDiemThanhToan().format(ngay) + " · Người thu: "
                + hd.getNguoiThucHien() + "\nHình thức: " + hd.getHinhThucThanhToan().getTenHienThi()
                + " · Mã giao dịch: " + hd.getMaGiaoDich()));
        BaoCaoUi.them(noiDung, BaoCaoUi.taoThe("Hóa đơn " + ma + " · Đã thanh toán", thongTin));

        // Bảng chi tiết chỉ đọc, tiền nguyên đồng và không kéo thay đổi cột.
        DefaultTableModel model = new DefaultTableModel(
                new String[] { "Hạng mục", "SL / ĐVT", "Đơn giá", "Thành tiền" }, 0);
        for (ChiTietBaoGia ct : chiTiet) {
            model.addRow(new Object[] { ct.getTenHangMuc(), UiChung.so(ct.getSoLuong()) + " " + ct.getDonVi(),
                    ct.getDonGia(), ct.getThanhTien() });
        }
        JTable bang = UiChung.taoBang(model, 320, 140, 160, 160);
        BaoCaoUi.them(noiDung, BaoCaoUi.taoThe("Chi tiết dịch vụ và phụ tùng", BaoCaoUi.cuonBang(bang)));
        JPanel tong = new JPanel(new BorderLayout(0, 12));
        tong.setOpaque(false);
        tong.add(UiChung.taoNhan("Tổng đã thanh toán: " + UiChung.so(hd.getTongTien()) + " đ", 22, true,
                UiTheme.APP_TEXT), BorderLayout.NORTH);
        tong.add(BaoCaoUi
                .moTa("In lại đúng mã hóa đơn cũ. Thuế và các hạng mục chỉ hiển thị khi đã có trên chứng từ gốc."));
        JButton btnIn = UiChung.taoNut("In hóa đơn", true);
        btnIn.setMnemonic('I');
        btnIn.addActionListener(new ActionListener() {
            /** Mở hộp chọn máy in và in đúng chứng từ đang xem. */
            @Override
            public void actionPerformed(ActionEvent event) {
                inHoaDon();
            }
        });
        tong.add(UiChung.taoHangNut(btnIn), BorderLayout.SOUTH);
        BaoCaoUi.them(noiDung, BaoCaoUi.taoThe("Thanh toán", tong));
        add(UiChung.taoVungCuon(noiDung));
    }

    /** Đọc lại chứng từ và kiểm tra phiên trước mỗi thao tác xem/in. */
    private HoaDon docHoaDon() {
        HoaDon hd = dao.timHoaDonTheoMa(taiKhoan, maHoaDon);
        if (hd == null) {
            throw new IllegalArgumentException("Không tìm thấy hóa đơn " + maHoaDon);
        }
        return hd;
    }

    /** Chia dòng dài để không mất tên hàng khi in. */
    private void themDong(List<String> dong, String chu) {
        String noiDung = chu.replace('\n', ' ').replace('\r', ' ');
        while (noiDung.length() > 78) {
            dong.add(noiDung.substring(0, 78));
            noiDung = noiDung.substring(78);
        }
        dong.add(noiDung);
    }

    /** Tạo bản in nhiều trang từ dữ liệu đã lưu, không phụ thuộc vùng cuộn đang thấy. */
    public Printable taoBanIn() {
        HoaDon hd = docHoaDon();
        List<String> dong = new ArrayList<>();
        themDong(dong, "GARAGE Ô TÔ ĐỨC DŨNG");
        themDong(dong, "HÓA ĐƠN " + hd.getMaHoaDon() + " - ĐÃ THANH TOÁN");
        themDong(dong, "Lệnh sửa chữa: " + hd.getMaPhieuSuaChua());
        themDong(dong, "Khách hàng: " + hd.getTenKhachHang());
        themDong(dong, "Biển số: " + hd.getBienSo());
        themDong(dong, "Thanh toán lúc: " + hd.getThoiDiemThanhToan().format(ngay));
        themDong(dong, "Hình thức: " + hd.getHinhThucThanhToan().getTenHienThi());
        themDong(dong, "Mã giao dịch: " + hd.getMaGiaoDich());
        themDong(dong, "Người thu: " + hd.getNguoiThucHien());
        themDong(dong, "");
        for (ChiTietBaoGia ct : dao.layChiTietHoaDon(taiKhoan, maHoaDon)) {
            themDong(dong, ct.getTenHangMuc());
            themDong(dong, "  " + UiChung.so(ct.getSoLuong()) + " " + ct.getDonVi() + " x " + UiChung.so(ct.getDonGia())
                    + " d = " + UiChung.so(ct.getThanhTien()) + " d");
        }
        themDong(dong, "");
        themDong(dong, "TỔNG ĐÃ THANH TOÁN: " + UiChung.so(hd.getTongTien()) + " d");
        themDong(dong, "In lại - không tạo hóa đơn mới.");
        return new Printable() {
            /** Vẽ phần nội dung thuộc trang được yêu cầu và chặn phiên đã hết hiệu lực. */
            @Override
            public int print(Graphics graphics, PageFormat page, int trang) throws PrinterException {
                taiKhoan.kiemTraPhien();
                int soDong = (int) ((page.getImageableHeight() - 50) / 14);
                if (soDong < 1) {
                    throw new PrinterException("Khổ giấy không đủ để in hóa đơn.");
                }
                if (trang < 0 || trang > (dong.size() - 1) / soDong) {
                    return NO_SUCH_PAGE;
                }
                Graphics2D ve = (Graphics2D) graphics.create();
                try {
                    ve.translate(page.getImageableX(), page.getImageableY());
                    ve.scale(Math.min(1, page.getImageableWidth() / 480), 1);
                    ve.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
                    ve.drawString("HÓA ĐƠN " + maHoaDon + " - Trang " + (trang + 1), 0, 12);
                    int dau = trang * soDong;
                    for (int i = dau; i < dong.size() && i < dau + soDong; i++) {
                        ve.drawString(dong.get(i), 0, 36 + (i - dau) * 14);
                    }
                    return PAGE_EXISTS;
                } finally {
                    ve.dispose();
                }
            }
        };
    }

    /** Chỉ gửi bản in sau khi người dùng xác nhận hộp thoại máy in. */
    private void inHoaDon() {
        try {
            Printable banIn = taoBanIn();
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setJobName("Hóa đơn " + maHoaDon);
            job.setPrintable(banIn);
            if (job.printDialog()) {
                docHoaDon();
                job.print();
            }
        } catch (PrinterException | RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Không in được hóa đơn: " + ex.getMessage(), "In hóa đơn",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
