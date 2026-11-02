package ui.screen.thungan;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import dao.hoadon.HoaDonDAO;
import entity.nhansu.TaiKhoan;

/** Màn hóa đơn đã thu dùng lại bộ tìm/lọc và xem chứng từ của nguồn chung. */
public final class PaidInvoicesPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final CashReceiptPanel noiDung;

    /** Tạo màn danh sách đã thanh toán của đúng phiên Thu ngân. */
    public PaidInvoicesPanel(HoaDonDAO dao, TaiKhoan tk) {
        super(new BorderLayout());
        noiDung = new CashReceiptPanel(dao, tk);
        add(noiDung);
    }
}
