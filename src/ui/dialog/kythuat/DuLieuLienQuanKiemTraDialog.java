package ui.dialog.kythuat;

import entity.PhieuTiepNhan.BaoCaoKiemTra;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import ui.screen.kythuat.KiemTraUi;
import ui.component.UiTheme;

// Chi tiết trả yêu cầu xóa để màn hình kiểm tra quyền và trạng thái.
public final class DuLieuLienQuanKiemTraDialog extends JDialog implements ActionListener {
    private static final long serialVersionUID = 1L;
    private final BaoCaoKiemTra baoCao;
    private boolean yeuCauXoa;

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public DuLieuLienQuanKiemTraDialog(JFrame winChu, BaoCaoKiemTra baoCao) {
        super(winChu, "Chi tiết " + (baoCao == null ? "" : baoCao.getMaBaoCao()), true);
        if (baoCao == null) {
            throw new IllegalArgumentException("Cần chọn báo cáo.");
        }
        this.baoCao = baoCao;
        setUndecorated(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        getRootPane().setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));
        setLayout(new BorderLayout());
        getContentPane().setBackground(UiTheme.WINDOW);
        add(taoThanhTieuDe(), BorderLayout.NORTH);
        add(taoNoiDung(), BorderLayout.CENTER);
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        pnlNut.setOpaque(false);
        pnlNut.add(KiemTraUi.taoNut("Đóng", "DONG", false, this));
        pnlNut.add(KiemTraUi.taoNut("Xóa", "XOA", false, this));
        add(pnlNut, BorderLayout.SOUTH);
        setSize(760, 324);
        setLocationRelativeTo(winChu);
    }

    /** Cho biết yeu cau xoa. */
    public boolean isYeuCauXoa() {
        return yeuCauXoa;
    }

    /** {@inheritDoc} */
    // Ghi nhận yêu cầu của người dùng và đóng chi tiết, không tự xóa báo cáo.
    @Override
    public void actionPerformed(ActionEvent suKien) {
        yeuCauXoa = "XOA".equals(suKien.getActionCommand());
        dispose();
    }

    // Tạo tiêu đề nhỏ và nút đóng theo hộp thoại KT_RH trong Figma.
    /** Tạo thanh tieu de cho giao diện. */
    private JPanel taoThanhTieuDe() {
        JPanel pnlDau = new JPanel(new BorderLayout(8, 0));
        pnlDau.setBackground(UiTheme.SURFACE);
        pnlDau.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        JLabel lblTieuDe = KiemTraUi.taoNhan(getTitle(), true);
        lblTieuDe.setFont(UiTheme.BRAND_FONT.deriveFont(13.0f));
        pnlDau.add(lblTieuDe, BorderLayout.CENTER);
        JButton btnDong = KiemTraUi.taoNut("×", "DONG", false, this);
        btnDong.setForeground(UiTheme.MUTED);
        btnDong.setBorder(BorderFactory.createEmptyBorder());
        btnDong.setPreferredSize(new Dimension(24, 20));
        pnlDau.add(btnDong, BorderLayout.EAST);
        return pnlDau;
    }

    // Hiển thị mã báo cáo, phiếu, biển số, kỹ thuật viên và trạng thái thành hai cột.
    /** Tạo noi dung cho giao diện. */
    private JPanel taoNoiDung() {
        JPanel pnlNoiDung = new JPanel(new BorderLayout(0, 12));
        pnlNoiDung.setOpaque(false);
        pnlNoiDung.setBorder(BorderFactory.createEmptyBorder(16, 16, 0, 16));
        JLabel lblMoTa = KiemTraUi.taoNhan("Thông tin hồ sơ · Chỉ đọc", false);
        lblMoTa.setForeground(UiTheme.MUTED);
        pnlNoiDung.add(lblMoTa, BorderLayout.NORTH);
        JPanel pnlTruong = new JPanel(new GridLayout(3, 2, 12, 8));
        pnlTruong.setOpaque(false);
        pnlTruong.add(KiemTraUi.taoTruong("Số báo cáo", KiemTraUi.taoNhan(baoCao.getMaBaoCao(), false)));
        pnlTruong.add(KiemTraUi.taoTruong("Phiếu tiếp nhận", KiemTraUi.taoNhan(baoCao.getPhieu().getMaPhieu(), false)));
        pnlTruong.add(KiemTraUi.taoTruong("Biển số", KiemTraUi.taoNhan(baoCao.getPhieu().getBienSo(), false)));
        String taiKhoanKyThuat = baoCao.getPhieu().getTaiKhoanKyThuat();
        String tenKyThuat = "NV005".equals(taiKhoanKyThuat) ? "Lê Minh Tâm" : taiKhoanKyThuat;
        pnlTruong.add(KiemTraUi.taoTruong("Kỹ thuật viên", KiemTraUi.taoNhan(tenKyThuat, false)));
        pnlTruong.add(
                KiemTraUi.taoTruong("Trạng thái", KiemTraUi.taoNhan(baoCao.getTrangThai().getTenHienThi(), false)));
        pnlTruong.add(new JLabel());
        pnlNoiDung.add(pnlTruong, BorderLayout.CENTER);
        JTextArea txaQuyTac = KiemTraUi.taoVanBan(KiemTraUi.QUY_TAC);
        txaQuyTac.setFont(UiTheme.CAPTION_FONT);
        txaQuyTac.setForeground(UiTheme.MUTED);
        txaQuyTac.setPreferredSize(new Dimension(0, 32));
        pnlNoiDung.add(txaQuyTac, BorderLayout.SOUTH);
        return pnlNoiDung;
    }
}
