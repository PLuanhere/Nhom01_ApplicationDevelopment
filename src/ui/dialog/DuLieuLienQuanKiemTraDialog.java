package ui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import entity.BaoCaoKiemTra;
import ui.component.KiemTraUi;
import ui.component.UiTheme;

// Chi tiết trả yêu cầu xóa để màn hình kiểm tra quyền và trạng thái.
public final class DuLieuLienQuanKiemTraDialog extends JDialog implements ActionListener {
    private static final long serialVersionUID = 1L;
    private final BaoCaoKiemTra baoCao;
    private boolean yeuCauXoa;

    public DuLieuLienQuanKiemTraDialog(JFrame winChu, BaoCaoKiemTra baoCao) {
        super(winChu, "Chi tiết " + Objects.requireNonNull(baoCao).getMaBaoCao(), true);
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

    /** @return thanh tiêu đề trắng có dấu đóng */
    // Tạo tiêu đề nhỏ và nút đóng theo hộp thoại KT_RH trong Figma.
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

    /** @return thông tin chỉ đọc của báo cáo được chọn */
    // Hiển thị mã báo cáo, phiếu, biển số, kỹ thuật viên và trạng thái thành hai cột.
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
        pnlTruong.add(KiemTraUi.taoTruong("Phiếu tiếp nhận",
                KiemTraUi.taoNhan(baoCao.getPhieu().getMaPhieu(), false)));
        pnlTruong.add(KiemTraUi.taoTruong("Biển số", KiemTraUi.taoNhan(baoCao.getPhieu().getBienSo(), false)));
        String taiKhoanKyThuat = baoCao.getPhieu().getTaiKhoanKyThuat();
        String tenKyThuat = "kythuat".equals(taiKhoanKyThuat) ? "Chung Ngô Minh Hoàng" : taiKhoanKyThuat;
        pnlTruong.add(KiemTraUi.taoTruong("Kỹ thuật viên", KiemTraUi.taoNhan(tenKyThuat, false)));
        pnlTruong.add(KiemTraUi.taoTruong("Trạng thái",
                KiemTraUi.taoNhan(baoCao.getTrangThai().getTenHienThi(), false)));
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