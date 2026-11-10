package ui.screen.quanly;

import dao.BaoCaoDAO;
import entity.BaoCao;
import entity.TaiKhoan;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ui.component.UiChung;
import ui.component.UiTheme;

/** Tra cứu các bản chụp báo cáo đã lưu bền vững trong database. */
public final class BaoCaoDaLuuPanel extends JPanel implements ActionListener, DocumentListener {
    private static final long serialVersionUID = 1L;
    private final TaiKhoan taiKhoan;
    private final BaoCaoDAO dao;
    private final JTextField txtTim = UiChung.taoONhap(true);
    private final JComboBox<String> cmbLoai = new JComboBox<>(new String[] { "Tất cả loại báo cáo", BaoCao.DOANH_THU,
            BaoCao.TIEP_NHAN, BaoCao.TON_KHO, BaoCao.HIEU_SUAT });
    private final JPanel pnlBang = new JPanel(new BorderLayout());
    private final JLabel lblThongBao = UiChung.taoNhan("", 12, false, UiTheme.APP_MUTED);
    private List<BaoCao> danhSach;
    private JTable tbl;

    /** Tạo bộ tìm/lọc và bảng bản chụp báo cáo. */
    public BaoCaoDaLuuPanel(TaiKhoan tk, BaoCaoDAO dao) {
        super(new BorderLayout(0, 14));
        this.taiKhoan = tk;
        this.dao = dao;
        setOpaque(false);
        JPanel dau = BaoCaoUi.taoCot();
        BaoCaoUi.them(dau, UiChung.taoDauTrang("Quản lý / Báo cáo / Báo cáo đã lưu", "Báo cáo đã lưu",
                "Xem lại bản chụp kết quả tại thời điểm lập báo cáo."));
        JButton btnMoi = nut("Làm mới", "lamMoi");
        BaoCaoUi.them(dau, UiChung.taoBoLoc(txtTim, btnMoi, cmbLoai));
        BaoCaoUi.them(dau, UiChung.taoHangNut(nut("Xem chi tiết", "chiTiet")));
        add(dau, BorderLayout.NORTH);
        pnlBang.setOpaque(false);
        add(pnlBang);
        add(lblThongBao, BorderLayout.SOUTH);
        txtTim.getDocument().addDocumentListener(this);
        cmbLoai.addActionListener(this);
        taiBang();
    }

    /** Gắn mã thao tác cho nút danh sách. */
    private JButton nut(String ten, String lenh) {
        JButton btn = UiChung.taoNut(ten, false);
        btn.setActionCommand(lenh);
        btn.addActionListener(this);
        return btn;
    }

    /** Tìm/lọc bằng DAO, giữ đúng bản báo cáo ứng với dòng đang hiển thị. */
    private void taiBang() {
        danhSach = dao.timDaLuu(taiKhoan, txtTim.getText(),
                cmbLoai.getSelectedIndex() == 0 ? null : (String) cmbLoai.getSelectedItem());
        List<String[]> dong = new ArrayList<>();
        DateTimeFormatter ngay = DateTimeFormatter.ofPattern("dd/MM/uuuu");
        for (BaoCao bc : danhSach) {
            dong.add(new String[] { bc.getMaBaoCao(), bc.getLoai(), bc.getTuNgay().format(ngay),
                    bc.getDenNgay().format(ngay), bc.getNguoiLap(), "Xem chi tiết" });
        }
        tbl = BaoCaoUi.taoBang(
                new String[] { "Mã báo cáo", "Loại báo cáo", "Từ ngày", "Đến ngày", "Người tạo", "Thao tác" }, dong);
        tbl.addMouseListener(new MouseAdapter() {
            /** Mở đúng dòng được nhấp đôi. */
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 || tbl.columnAtPoint(e.getPoint()) == 5) {
                    xemChiTiet();
                }
            }
        });
        pnlBang.removeAll();
        pnlBang.add(BaoCaoUi.cuonBang(tbl));
        lblThongBao.setText("Hiển thị " + danhSach.size()
                + " báo cáo · Bản chụp được giữ nguyên trong database.");
        pnlBang.revalidate();
        pnlBang.repaint();
    }

    /** Xem nguyên bản đã lưu, không tự tổng hợp lại số liệu mới. */
    private void xemChiTiet() {
        dao.kiemTraQuyen(taiKhoan);
        int i = tbl.getSelectedRow();
        if (i < 0) {
            lblThongBao.setText("Chọn một báo cáo trước khi xem chi tiết.");
            return;
        }
        BaoCaoUi.hienBaoCao(this, danhSach.get(i), taiKhoan, dao);
    }

    /** Xem chi tiết hoặc thay điều kiện tìm/lọc. */
    @Override
    public void actionPerformed(ActionEvent e) {
        if ("chiTiet".equals(e.getActionCommand())) {
            xemChiTiet();
        } else {
            if ("lamMoi".equals(e.getActionCommand())) {
                txtTim.setText("");
                cmbLoai.setSelectedIndex(0);
            }
            taiBang();
        }
    }

    /** Lọc khi thêm ký tự. */
    @Override
    public void insertUpdate(DocumentEvent e) {
        taiBang();
    }

    /** Lọc khi xóa ký tự. */
    @Override
    public void removeUpdate(DocumentEvent e) {
        taiBang();
    }

    /** Lọc khi tài liệu nhập đổi định dạng. */
    @Override
    public void changedUpdate(DocumentEvent e) {
        taiBang();
    }
}
