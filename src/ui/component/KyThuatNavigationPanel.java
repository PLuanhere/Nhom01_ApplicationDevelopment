package ui.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

// Hiển thị menu kỹ thuật trong SidebarPanel chung của MainFrame.
public final class KyThuatNavigationPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final String[] MA_MUC = {
            "TONG_QUAN",
            "PHAN_CONG",
            "KIEM_TRA",
            "TIEN_DO",
            "PHU_TUNG",
            "PHAT_SINH",
            "HOAN_TAT"
    };

    private static final String[] NHAN_MUC = {
            "Công việc kỹ thuật",
            "Công việc được phân công",
            "Báo cáo kiểm tra",
            "Tiến độ sửa chữa",
            "Phụ tùng sử dụng",
            "Báo hư hỏng phát sinh",
            "Kiểm tra hoàn tất sửa chữa"
    };

    private final ButtonGroup nhomMuc = new ButtonGroup();

    public KyThuatNavigationPanel(ActionListener khiChonMuc) {
        super(new BorderLayout());

        if (khiChonMuc == null) {
            throw new IllegalArgumentException("Cần thao tác điều hướng kỹ thuật.");
        }

        setOpaque(false);

        JPanel danhSach = new JPanel(new GridLayout(0, 1, 0, 4));
        danhSach.setOpaque(false);

        for (int i = 0; i < MA_MUC.length; i++) {
            danhSach.add(taoMuc(i, khiChonMuc));
        }

        add(danhSach, BorderLayout.NORTH);
    }

    // Tạo nút menu theo font, màu và biểu tượng của giao diện chung.
    private JToggleButton taoMuc(int viTri, ActionListener khiChonMuc) {
        String nhan = NHAN_MUC[viTri];

        if ("PHAN_CONG".equals(MA_MUC[viTri])) {
            nhan = "Công việc được<br>phân công";
        } else if ("HOAN_TAT".equals(MA_MUC[viTri])) {
            nhan = "Kiểm tra hoàn tất<br>sửa chữa";
        }

        JToggleButton nut = new JToggleButton("<html>" + nhan + "</html>") {
            private static final long serialVersionUID = 1L;

            // Vẽ nền theo bộ màu chung, không dùng màu chọn mặc định của Swing.
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D ve = (Graphics2D) graphics.create();

                ve.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                ve.setColor(getBackground());
                ve.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                ve.dispose();

                super.paintComponent(graphics);
            }
        };

        nut.setActionCommand(MA_MUC[viTri]);
        nut.setName(MA_MUC[viTri]);
        nut.setIcon(BieuTuongKyThuat.lay(viTri + 1));
        nut.setIconTextGap(8);
        nut.setHorizontalAlignment(JToggleButton.LEFT);
        nut.setPreferredSize(new Dimension(0, 52));
        nut.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 8));
        nut.setFocusPainted(false);
        nut.setFont(UiTheme.APP_FONT);
        nut.setForeground(UiTheme.SIDEBAR_TEXT);
        nut.setBackground(UiTheme.SIDEBAR_BACKGROUND);
        nut.setContentAreaFilled(false);
        nut.setOpaque(false);
        nut.getAccessibleContext().setAccessibleName(NHAN_MUC[viTri]);

        nut.addItemListener(new ItemListener() {
            // Cập nhật màu và font khi mục được chọn hoặc bỏ chọn.
            @Override
            public void itemStateChanged(ItemEvent e) {
                nut.setBackground(
                        nut.isSelected() ? UiTheme.SIDEBAR_ACTIVE : UiTheme.SIDEBAR_BACKGROUND);

                nut.setForeground(
                        nut.isSelected() ? Color.WHITE : UiTheme.SIDEBAR_TEXT);

                nut.setFont(
                        UiTheme.APP_FONT.deriveFont(nut.isSelected() ? Font.BOLD : Font.PLAIN));
            }
        });

        nut.addActionListener(new ActionListener() {
            // Gửi mã trang về MainFrame, không tự khởi tạo hoặc mở màn hình.
            @Override
            public void actionPerformed(ActionEvent e) {
                khiChonMuc.actionPerformed(
                        new ActionEvent(nut, ActionEvent.ACTION_PERFORMED, nut.getActionCommand()));
            }
        });

        nhomMuc.add(nut);
        return nut;
    }

    // Đồng bộ nút đang chọn mà không phát thêm sự kiện điều hướng.
    public void chonMuc(String ma) {
        Enumeration<AbstractButton> cacNut = nhomMuc.getElements();

        while (cacNut.hasMoreElements()) {
            AbstractButton nut = cacNut.nextElement();

            if (nut.getActionCommand().equals(ma)) {
                nut.setSelected(true);
                return;
            }
        }

        throw new IllegalArgumentException("Không có mục kỹ thuật: " + ma);
    }

    // Kiểm tra mã mục trước khi MainFrame xử lý yêu cầu điều hướng.
    public static boolean coMuc(String ma) {
        for (String muc : MA_MUC) {
            if (muc.equals(ma)) {
                return true;
            }
        }

        return false;
    }

    // Lấy nhãn của mục để MainFrame hiển thị trên thanh trạng thái.
    public static String layNhan(String ma) {
        for (int i = 0; i < MA_MUC.length; i++) {
            if (MA_MUC[i].equals(ma)) {
                return NHAN_MUC[i];
            }
        }

        throw new IllegalArgumentException("Không có mục kỹ thuật: " + ma);
    }
}