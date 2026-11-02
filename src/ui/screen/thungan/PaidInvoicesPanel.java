package ui.screen.thungan;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import dao.hoadon.HoaDonDAO;
import entity.nhansu.TaiKhoan;

/** Màn hóa đơn đã thu dùng lại bộ tìm/lọc và xem chứng từ của nguồn chung. */
public final class PaidInvoicesPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final CashReceiptPanel noiDung;

    /** Tạo màn danh sách đã thanh toán của đúng phiên Thu ngân. */
    public PaidInvoicesPanel(HoaDonDAO dao, TaiKhoan tk) {
        this(dao, tk, null);
    }

    /** Gắn điều hướng tới chứng từ được chọn từ màn chính. */
    public PaidInvoicesPanel(HoaDonDAO dao, TaiKhoan tk, ActionListener khiXem) {
        super(new BorderLayout());
        noiDung = new CashReceiptPanel(dao, tk);
        noiDung.setKhiXem(khiXem);
        add(noiDung);
    }
}
