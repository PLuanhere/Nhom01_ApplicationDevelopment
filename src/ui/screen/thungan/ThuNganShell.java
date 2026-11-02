package ui.screen.thungan;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import ui.MainFrame;
import ui.component.UiTheme;

/** Điều hướng giao diện Thu ngân khi xem trước độc lập. */
public final class ThuNganShell {

    /** Khung chính nhận sidebar và màn hình từ shell này. */
    private final MainFrame winChinh;
    /** Callback quay màn hình đăng nhập khi đổi vai trò hoặc đăng xuất. */
    private final ActionListener khiDangXuat;

    /** Tạo shell cho vai trò Thu ngân. */
    public ThuNganShell(MainFrame winChinh, ActionListener khiDangXuat) {
        if (winChinh == null) {
            throw new NullPointerException("Cần khung chính.");
        }
        this.winChinh = winChinh;
        if (khiDangXuat == null) {
            throw new NullPointerException("Cần callback đăng xuất.");
        }
        this.khiDangXuat = khiDangXuat;
    }

    /** Gắn panel điều hướng vào sidebar và hiển thị trang chủ; gọi trên EDT sau khi tạo MainFrame. */
    public void start() {
        ThuNganNavigationPanel pnlNav = new ThuNganNavigationPanel(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                ThuNganShellPage giaTri1 = (ThuNganShellPage) event.getSource();
                ThuNganShell.this.chuyenTrang(giaTri1);
            }
        }, new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                ThuNganShell.this.xuLyDoiVaiTro();
            }
        }, new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                ThuNganShell.this.xuLyDangXuat();
            }
        });

        winChinh.setSidebarContent(pnlNav);
        // Hiển thị trang chủ ngay sau khi đăng nhập.
        chuyenTrang(ThuNganShellPage.TRANG_CHU);
    }

    // ── Điều hướng màn hình ──────────────────────────────────────────────────

    /** Tạo và hiển thị màn hình tương ứng với trang được chọn trên sidebar. */
    private void chuyenTrang(ThuNganShellPage trang) {
        JPanel pnlManHinh;
        switch (trang) {
        case TRANG_CHU:
            pnlManHinh = new HomeCashDashboardThuNganN4(new ActionListener() {
                /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
                @Override
                public void actionPerformed(ActionEvent event) {
                    winChinh.setScreen(new DashDetailCashierPanel(new ActionListener() {
                        /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
                        @Override
                        public void actionPerformed(ActionEvent event) {
                            chuyenTrang(ThuNganShellPage.TRANG_CHU);
                        }
                    }));
                }
            }, new ActionListener() {
                /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
                @Override
                public void actionPerformed(ActionEvent event) {
                    ThuNganShell.this.moManHinhPhieuCho();
                }
            });
            break;
        case THANH_TOAN:
            pnlManHinh = placeholder("💳", "Thanh toán",
                    "Chức năng lập hóa đơn và ghi nhận thanh toán sẽ được tích hợp tại đây.");
            break;
        case TRA_CUU_CHUNG_TU:
            pnlManHinh = placeholder("🔍", "Tra cứu chứng từ",
                    "Chức năng tra cứu và in lại hóa đơn, chứng từ sẽ được tích hợp tại đây.");
            break;
        default:
            throw new IllegalArgumentException("Lựa chọn không hợp lệ.");
        }
        winChinh.setScreen(pnlManHinh);
        winChinh.setStatus(trang.getTenHienThi());
    }

    /** Mở màn hình danh sách phiếu chờ thanh toán. */
    private void moManHinhPhieuCho() {
        winChinh.setScreen(new PendingInvoicesPanel(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                winChinh.setScreen(new DocumentSearchPanel(new ActionListener() {
                    /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        String[] duLieu = (String[]) event.getSource();
                        String giaTri1 = duLieu[0];
                        String giaTri2 = duLieu[1];
                        ThuNganShell.this.xuLyTraCuu(giaTri1, giaTri2);
                    }
                }, new ActionListener() {
                    /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        ThuNganShell.this.moManHinhPhieuCho();
                    }
                }));
            }
        }, new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {

                /* đối chiếu — tích hợp sau */
            }
        }, new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                chuyenTrang(ThuNganShellPage.TRANG_CHU);
            }
        }));
    }

    /** Mở màn hình tra cứu chứng từ độc lập (từ sidebar hoặc nút Đổi tiêu chí). */
    private void moManHinhTraCuu() {
        winChinh.setScreen(new DocumentSearchPanel(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                String[] duLieu = (String[]) event.getSource();
                String giaTri1 = duLieu[0];
                String giaTri2 = duLieu[1];
                ThuNganShell.this.xuLyTraCuu(giaTri1, giaTri2);
            }
        }, new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                chuyenTrang(ThuNganShellPage.TRANG_CHU);
            }
        }));
    }

    /** Xử lý kết quả tra cứu: mở CashReceiptPanel khi có dữ liệu (tiền mặt/chuyển khoản), mở DocumentNotFoundPanel khi không tìm thấy (tất cả hoặc từ khóa không khớp). */
    private void xuLyTraCuu(String tuKhoa, String phuongThuc) {
        boolean coKetQua = "Tất cả".equals(phuongThuc) || "Tiền mặt".equals(phuongThuc);
        if (coKetQua) {
            winChinh.setScreen(new CashReceiptPanel(phuongThuc, new ActionListener() {
                /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
                @Override
                public void actionPerformed(ActionEvent event) {
                    ThuNganShell.this.moManHinhTraCuu();
                }
            }, new ActionListener() {
                /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
                @Override
                public void actionPerformed(ActionEvent event) {

                    /* xem chi tiết chứng từ — tích hợp sau */
                }
            }));
        } else {
            winChinh.setScreen(new DocumentNotFoundPanel(new ActionListener() {
                /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
                @Override
                public void actionPerformed(ActionEvent e) {
                    moManHinhTraCuu();
                }
            }));
        }
    }

    /** Tạo panel giữ chỗ cho màn hình chưa tích hợp. */
    private JPanel placeholder(String icon, String tieuDe, String ghiChu) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(UiTheme.WINDOW);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));

        JLabel lblIcon = new JLabel(icon, SwingConstants.CENTER);
        lblIcon.setFont(UiTheme.HEADING_FONT.deriveFont(48.0f));
        lblIcon.setAlignmentX(JComponent.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel(tieuDe, SwingConstants.CENTER);
        lblTitle.setFont(UiTheme.HEADING_FONT.deriveFont(Font.BOLD, 20.0f));
        lblTitle.setForeground(UiTheme.TEXT);
        lblTitle.setAlignmentX(JComponent.CENTER_ALIGNMENT);

        JLabel lblNote = new JLabel(ghiChu, SwingConstants.CENTER);
        lblNote.setFont(UiTheme.BODY_FONT);
        lblNote.setForeground(UiTheme.MUTED);
        lblNote.setAlignmentX(JComponent.CENTER_ALIGNMENT);

        center.add(lblIcon);
        center.add(Box.createVerticalStrut(16));
        center.add(lblTitle);
        center.add(Box.createVerticalStrut(8));
        center.add(lblNote);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(center, BorderLayout.NORTH);
        pnl.add(wrap, BorderLayout.CENTER);
        return pnl;
    }

    // ── Hành động hệ thống ───────────────────────────────────────────────────

    /** Hỏi xác nhận rồi đóng cửa sổ và mở lại màn hình đăng nhập để chọn vai trò khác. */
    private void xuLyDoiVaiTro() {
        int ketQua = JOptionPane.showConfirmDialog(winChinh,
                "Bạn muốn đổi sang vai trò khác?\nPhiên làm việc hiện tại sẽ kết thúc.", "Đổi vai trò",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ketQua == JOptionPane.YES_OPTION) {
            SwingUtilities.invokeLater(new Runnable() {
                /** Khởi tạo hoặc cập nhật giao diện trên luồng Swing. */
                @Override
                public void run() {
                    winChinh.dispose();
                    khiDangXuat.actionPerformed(new ActionEvent(khiDangXuat, ActionEvent.ACTION_PERFORMED, "thaoTac"));
                }
            });
        }
    }

    /** Hỏi xác nhận rồi kết thúc phiên và quay màn hình đăng nhập. */
    private void xuLyDangXuat() {
        int ketQua = JOptionPane.showConfirmDialog(winChinh, "Bạn có chắc muốn đăng xuất?", "Đăng xuất",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ketQua == JOptionPane.YES_OPTION) {
            SwingUtilities.invokeLater(new Runnable() {
                /** Khởi tạo hoặc cập nhật giao diện trên luồng Swing. */
                @Override
                public void run() {
                    winChinh.dispose();
                    khiDangXuat.actionPerformed(new ActionEvent(khiDangXuat, ActionEvent.ACTION_PERFORMED, "thaoTac"));
                }
            });
        }
    }
}
