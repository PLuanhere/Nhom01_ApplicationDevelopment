package ui.dialog;

import java.awt.*;
import javax.swing.*;
import ui.component.UiTheme;

/** Đối chiếu trước khi ghi; callback lỗi giữ dialog mở và không báo bàn giao thành công. */
public final class BanGiaoXeDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private final JLabel lblLoi = new JLabel(" ");
    private final JButton btnHoanTat = UiTheme.createAppButton("Hoàn tất khi khách xác nhận", true);

    public BanGiaoXeDialog(Window chu, String[][] thongTin, boolean khachDaXacNhan, Runnable khiXacNhan) {
        super(chu, "Xác nhận bàn giao thực tế", ModalityType.APPLICATION_MODAL);
        setUndecorated(true); getRootPane().setBorder(BorderFactory.createLineBorder(UiTheme.APP_BORDER));
        JPanel pnl = UiTheme.roundedPanel(new BorderLayout(0, 14), Color.WHITE, 9);
        pnl.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JPanel dau = new JPanel(new BorderLayout(0, 8)); dau.setOpaque(false);
        JLabel ten = new JLabel("Xác nhận bàn giao thực tế"); ten.setFont(UiTheme.APP_BOLD_FONT.deriveFont(19f)); dau.add(ten, BorderLayout.NORTH);
        JLabel moTa = new JLabel("Tiếp nhận giao xe sau khi đã đối chiếu căn cứ thanh toán/phê duyệt."); moTa.setFont(UiTheme.APP_FONT); dau.add(moTa); pnl.add(dau, BorderLayout.NORTH);
        JPanel facts = new JPanel(new GridLayout(0, 2, 14, 12)); facts.setBackground(Color.WHITE);
        for (String[] dong : thongTin) {
            JPanel fact = new JPanel(new BorderLayout(0, 5)); fact.setOpaque(false);
            JLabel nhan = new JLabel(dong[0]); nhan.setFont(UiTheme.APP_FONT); nhan.setForeground(UiTheme.APP_MUTED); fact.add(nhan, BorderLayout.NORTH);
            JTextArea giaTri = new JTextArea(dong[1], 2, 20); giaTri.setFont(UiTheme.APP_BOLD_FONT); giaTri.setEditable(false); giaTri.setLineWrap(true); giaTri.setWrapStyleWord(true);
            JScrollPane cuon = new JScrollPane(giaTri); cuon.setBorder(null); fact.add(cuon); facts.add(fact);
        }
        JScrollPane noiDung = new JScrollPane(facts); noiDung.setBorder(null); noiDung.getVerticalScrollBar().setUnitIncrement(20); pnl.add(noiDung);
        JPanel chan = new JPanel(new BorderLayout(0, 10)); chan.setOpaque(false);
        JPanel thongBao = new JPanel(new BorderLayout(0, 8)); thongBao.setOpaque(false);
        JTextArea quyTac = new JTextArea("Chỉ hoàn tất khi đã đối chiếu người nhận, giờ thực giao, tình trạng xe và xác nhận khách.", 2, 20);
        quyTac.setFont(UiTheme.APP_FONT); quyTac.setLineWrap(true); quyTac.setWrapStyleWord(true); quyTac.setEditable(false); quyTac.setBackground(new Color(0xFFF7E3)); quyTac.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10)); thongBao.add(quyTac);
        lblLoi.setForeground(UiTheme.ERROR); lblLoi.setFont(UiTheme.APP_FONT); thongBao.add(lblLoi, BorderLayout.SOUTH); chan.add(thongBao, BorderLayout.NORTH);
        JPanel nut = new JPanel(new GridLayout(1, 2, 14, 0)); nut.setOpaque(false);
        JButton huy = UiTheme.createAppButton("Hủy", false); huy.addActionListener(e -> dispose()); nut.add(huy);
        btnHoanTat.setEnabled(khachDaXacNhan);
        btnHoanTat.addActionListener(e -> {
            btnHoanTat.setEnabled(false);
            try { khiXacNhan.run(); dispose(); }
            catch (IllegalArgumentException | IllegalStateException | SecurityException loi) { lblLoi.setText(loi.getMessage()); lblLoi.setToolTipText(loi.getMessage()); }
            finally { btnHoanTat.setEnabled(khachDaXacNhan); }
        });
        nut.add(btnHoanTat); chan.add(nut); pnl.add(chan, BorderLayout.SOUTH); setContentPane(pnl);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); setSize(790, Math.min(520, GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height - 60)); setLocationRelativeTo(chu);
        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
}
