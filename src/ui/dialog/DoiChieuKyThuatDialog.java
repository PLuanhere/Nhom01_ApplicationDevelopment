package ui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import ui.component.GiaoDienKyThuat;
import ui.component.PhuTungUi;

// Hiển thị thông tin đối chiếu và trả lựa chọn xác nhận cho màn hình gọi.
public final class DoiChieuKyThuatDialog extends JDialog implements ActionListener {
    private static final long serialVersionUID = 1L;
    private boolean dongY;

    public DoiChieuKyThuatDialog(Window chu, String tieuDe, String moTa, String[][] thongTin,
            String canhBao, String nutDongY, boolean nguyHiem) {
        super(chu, tieuDe, ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getRootPane().setBorder(BorderFactory.createLineBorder(GiaoDienKyThuat.VIEN));
        JPanel pnlCot = GiaoDienKyThuat.taoCot();
        pnlCot.setOpaque(true);
        pnlCot.setBackground(Color.WHITE);
        pnlCot.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel lblTieuDe = GiaoDienKyThuat.taoNhan(tieuDe, true);
        lblTieuDe.setFont(GiaoDienKyThuat.CHU_DAM.deriveFont(19.0f));
        GiaoDienKyThuat.themThanhPhan(pnlCot, lblTieuDe);
        GiaoDienKyThuat.themThanhPhan(pnlCot, GiaoDienKyThuat.taoThongBao(moTa, false));

        JPanel pnlThongTin = new JPanel(new GridLayout(0, 2, 14, 14));
        pnlThongTin.setOpaque(false);
        for (String[] dong : thongTin) {
            JTextArea txaGiaTri = PhuTungUi.taoMoTa(dong[1]);
            txaGiaTri.setFont(GiaoDienKyThuat.CHU_DAM.deriveFont(13.0f));
            pnlThongTin.add(GiaoDienKyThuat.taoTruong(dong[0], txaGiaTri));
        }
        if (thongTin.length > 0) {
            GiaoDienKyThuat.themThanhPhan(pnlCot, pnlThongTin);
        }
        if (!canhBao.isBlank()) {
            GiaoDienKyThuat.themThanhPhan(pnlCot, GiaoDienKyThuat.taoThongBao(canhBao, true));
        }

        JPanel pnlNut = new JPanel(new GridLayout(1, nutDongY == null ? 1 : 2, 14, 0));
        pnlNut.setOpaque(false);
        pnlNut.add(GiaoDienKyThuat.taoNut(nutDongY == null ? "Đóng" : "Hủy", "HUY", false, this));
        if (nutDongY != null) {
            JButton btnDongY = GiaoDienKyThuat.taoNut(nutDongY, "DONG_Y", !nguyHiem, this);
            if (nguyHiem) {
                btnDongY.setForeground(new Color(0xB3261E));
                btnDongY.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0xB3261E)),
                        BorderFactory.createEmptyBorder(7, 12, 7, 12)));
            }
            pnlNut.add(btnDongY);
        }
        GiaoDienKyThuat.themThanhPhan(pnlCot, pnlNut);
        add(pnlCot, BorderLayout.CENTER);
        int rong = Math.min(790, java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds().width - 60);
        pnlCot.setSize(rong - 2, 1);
        pack();
        setSize(rong, getPreferredSize().height);
        validate();
        setSize(rong, Math.max(getHeight(), getContentPane().getPreferredSize().height + 2));
        setLocationRelativeTo(chu);
        getRootPane().registerKeyboardAction(this, "HUY",
                javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    public boolean isDongY() {
        return dongY;
    }

    // Trả lựa chọn xác nhận rồi đóng hộp thoại.
    @Override
    public void actionPerformed(ActionEvent suKien) {
        dongY = "DONG_Y".equals(suKien.getActionCommand());
        dispose();
    }
}