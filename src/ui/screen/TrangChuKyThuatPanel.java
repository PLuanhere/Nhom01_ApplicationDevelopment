package ui.screen;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import entity.ThongKeNgayKyThuat;
import entity.TongQuanKyThuat;
import ui.component.KyThuatUi;
import ui.component.UiTheme;
import ui.dialog.ChiTietCongViecHoanTatDialog;

// Hiển thị dashboard kỹ thuật bên trong MainFrame, không tạo lại khung ứng dụng chung.
public final class TrangChuKyThuatPanel extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final TongQuanKyThuat tongQuan;
    private final ActionListener boXuLyDieuHuong;
    private final JFrame winChu;
    private final List<ThongKeNgayKyThuat> thongKeNgay;
    private final JButton[] btnCotNgay;

    /**
     * @param tongQuan dữ liệu tổng quan của kỹ thuật viên
     * @param winChu khung chung chứa dashboard
     * @param boXuLyDieuHuong đối tượng xử lý chuyển chức năng
     */
    public TrangChuKyThuatPanel(TongQuanKyThuat tongQuan, JFrame winChu, ActionListener boXuLyDieuHuong) {
        super(new BorderLayout(8, 8));
        this.tongQuan = Objects.requireNonNull(tongQuan, "Cần dữ liệu dashboard.");
        this.winChu = Objects.requireNonNull(winChu, "Cần khung chung.");
        this.boXuLyDieuHuong = Objects.requireNonNull(boXuLyDieuHuong, "Cần đối tượng xử lý điều hướng.");
        thongKeNgay = tongQuan.getThongKeNgay();
        btnCotNgay = new JButton[thongKeNgay.size()];
        setBackground(UiTheme.WINDOW);
        add(taoPhanDau(), BorderLayout.NORTH);
        JScrollPane scrNoiDung = new JScrollPane(taoNoiDung());
        scrNoiDung.setBorder(BorderFactory.createEmptyBorder());
        scrNoiDung.getViewport().setBackground(UiTheme.WINDOW);
        scrNoiDung.getVerticalScrollBar().setUnitIncrement(16);
        add(scrNoiDung, BorderLayout.CENTER);
        JPanel pnlCuoi = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlCuoi.setOpaque(false);
        JButton btnDoiVaiTro = KyThuatUi.taoNut("Đổi vai trò", 132);
        btnDoiVaiTro.setToolTipText("Tài khoản hiện tại chỉ được cấp một vai trò kỹ thuật.");
        btnDoiVaiTro.addActionListener(boXuLyDieuHuong);
        pnlCuoi.add(btnDoiVaiTro);
        add(pnlCuoi, BorderLayout.SOUTH);
    }

    /** {@inheritDoc} */
    // Xác định cột được bấm từ nguồn sự kiện rồi mở thống kê của đúng ngày.
    @Override
    public void actionPerformed(ActionEvent event) {
        Object nguon = event.getSource();
        for (int i = 0; i < btnCotNgay.length; i++) {
            if (nguon == btnCotNgay[i]) {
                moChiTiet(thongKeNgay.get(i));
                return;
            }
        }
    }

    /** @return panel chứa tiêu đề và mô tả dashboard */
    // Tạo tiêu đề cùng thông tin kỹ thuật viên và ngày báo cáo.
    private JPanel taoPhanDau() {
        JPanel pnlPhanDau = new JPanel(new BorderLayout(0, 8));
        pnlPhanDau.setOpaque(false);
        pnlPhanDau.add(KyThuatUi.taoNhan("Trang chủ • Kỹ thuật",
                UiTheme.BRAND_FONT.deriveFont(22.0f)), BorderLayout.NORTH);
        pnlPhanDau.add(KyThuatUi.taoNhan("Dashboard công việc cá nhân • " + tongQuan.getTenKyThuatVien()
                + " • " + tongQuan.getNgayBaoCao().format(DATE_FORMAT),
                UiTheme.SHELL_FONT.deriveFont(12.0f)), BorderLayout.CENTER);
        return pnlPhanDau;
    }

    /** @return panel chứa các thẻ dashboard */
    // Xếp các chỉ số, biểu đồ, lưu ý và lối vào theo thứ tự thiết kế.
    private JPanel taoNoiDung() {
        JPanel pnlNoiDung = new JPanel();
        pnlNoiDung.setLayout(new BoxLayout(pnlNoiDung, BoxLayout.Y_AXIS));
        pnlNoiDung.setBackground(UiTheme.WINDOW);
        JPanel pnlChiSo = new JPanel(new GridLayout(1, 4, 8, 0));
        pnlChiSo.setOpaque(false);
        pnlChiSo.add(taoChiSo("Đang sửa", tongQuan.getDangSua(), "Lệnh của tôi"));
        pnlChiSo.add(taoChiSo("Chờ phụ tùng", tongQuan.getChoPhuTung(), "Cần bổ sung vật tư"));
        pnlChiSo.add(taoChiSo("Chờ khách duyệt", tongQuan.getChoKhachDuyet(), "Phát sinh chưa thực hiện"));
        pnlChiSo.add(taoChiSo("Hoàn tất", tongQuan.tinhHoanTatTrongNgay(), "Công việc trong ngày"));
        themThe(pnlNoiDung, pnlChiSo, 124);
        themThe(pnlNoiDung, taoBieuDo(), 246);
        themThe(pnlNoiDung, taoTomTat(), 82);
        themThe(pnlNoiDung, taoLoiVao(), 94);
        pnlNoiDung.add(Box.createVerticalGlue());
        return pnlNoiDung;
    }

    /**
     * @param pnlCha panel chứa các thẻ
     * @param pnlThe thẻ cần thêm
     * @param chieuCao chiều cao thiết kế, tính bằng pixel
     */
    // Giữ chiều cao thiết kế hoặc tăng theo nội dung khi font cần thêm không gian.
    private void themThe(JPanel pnlCha, JPanel pnlThe, int chieuCao) {
        chieuCao = Math.max(chieuCao, pnlThe.getPreferredSize().height);
        pnlThe.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlThe.setPreferredSize(new Dimension(0, chieuCao));
        pnlThe.setMinimumSize(new Dimension(0, chieuCao));
        pnlThe.setMaximumSize(new Dimension(Integer.MAX_VALUE, chieuCao));
        if (pnlCha.getComponentCount() > 0) {
            pnlCha.add(Box.createVerticalStrut(8));
        }
        pnlCha.add(pnlThe);
    }

    /**
     * @param nhan tên chỉ số
     * @param soLuong giá trị thống kê
     * @param nguCanh phạm vi thống kê
     * @return thẻ chỉ số
     */
    // Xếp nhãn, số và mô tả theo chiều cao riêng để số lớn không bị cắt.
    private JPanel taoChiSo(String nhan, int soLuong, String nguCanh) {
        JPanel pnlThe = KyThuatUi.taoThe();
        JPanel pnlChu = new JPanel();
        pnlChu.setLayout(new BoxLayout(pnlChu, BoxLayout.Y_AXIS));
        pnlChu.setOpaque(false);
        pnlChu.add(KyThuatUi.taoNhan(nhan, UiTheme.CAPTION_FONT));
        pnlChu.add(Box.createVerticalStrut(8));
        pnlChu.add(KyThuatUi.taoNhan(String.valueOf(soLuong), UiTheme.BRAND_FONT.deriveFont(26.0f)));
        pnlChu.add(Box.createVerticalStrut(8));
        pnlChu.add(KyThuatUi.taoNhan(nguCanh, UiTheme.CAPTION_FONT));
        pnlThe.add(pnlChu, BorderLayout.NORTH);
        return pnlThe;
    }

    /** @return panel biểu đồ số công việc hoàn tất theo ngày */
    // Tạo biểu đồ và đăng ký sự kiện mở chi tiết cho từng cột.
    private JPanel taoBieuDo() {
        JPanel pnlBieuDo = KyThuatUi.taoThe();
        JPanel pnlTieuDe = new JPanel(new GridLayout(2, 1, 0, 8));
        pnlTieuDe.setOpaque(false);
        pnlTieuDe.add(KyThuatUi.taoNhan("Công việc đã hoàn tất theo ngày", UiTheme.BRAND_FONT));
        JLabel lblHuongDan = KyThuatUi.taoNhan("Bấm vào cột hoặc ngày để xem chi tiết.", UiTheme.CAPTION_FONT);
        lblHuongDan.setForeground(UiTheme.PRIMARY);
        pnlTieuDe.add(lblHuongDan);
        pnlBieuDo.add(pnlTieuDe, BorderLayout.NORTH);
        JPanel pnlCot = new JPanel(new GridLayout(1, Math.max(1, thongKeNgay.size()), 8, 0));
        pnlCot.setOpaque(false);
        for (int i = 0; i < thongKeNgay.size(); i++) {
            btnCotNgay[i] = taoCot(thongKeNgay.get(i));
            btnCotNgay[i].addActionListener(this);
            pnlCot.add(btnCotNgay[i]);
        }
        if (thongKeNgay.isEmpty()) {
            pnlCot.add(KyThuatUi.taoNhan("Chưa có công việc hoàn tất trong kỳ.", UiTheme.SHELL_FONT));
        }
        pnlBieuDo.add(pnlCot, BorderLayout.CENTER);
        pnlBieuDo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        pnlBieuDo.addMouseListener(new MouseAdapter() {
            /** {@inheritDoc} */
            // Mở ngày cuối khi người dùng bấm vào nền biểu đồ.
            @Override
            public void mouseClicked(MouseEvent event) {
                if (!thongKeNgay.isEmpty()) {
                    moChiTiet(thongKeNgay.get(thongKeNgay.size() - 1));
                }
            }
        });
        return pnlBieuDo;
    }

    /**
     * @param thongKe thống kê của ngày cần biểu diễn
     * @return nút chứa giá trị, thanh cột và nhãn ngày
     */
    // Tạo cột biểu đồ bằng các panel và nhãn đặt bên trong một nút trong suốt.
    private JButton taoCot(ThongKeNgayKyThuat thongKe) {
        JButton btnCot = new JButton();
        btnCot.setLayout(new BorderLayout());
        btnCot.setContentAreaFilled(false);
        btnCot.setBorder(BorderFactory.createEmptyBorder());
        btnCot.setFocusPainted(true);
        String ngay = thongKe.getNgay().format(DateTimeFormatter.ofPattern("dd/MM"));
        btnCot.setToolTipText("Xem " + thongKe.tinhTongCongViec() + " công việc ngày " + ngay);
        btnCot.getAccessibleContext().setAccessibleName(btnCot.getToolTipText());
        JPanel pnlChieuCao = new JPanel();
        pnlChieuCao.setOpaque(false);
        pnlChieuCao.setLayout(new BoxLayout(pnlChieuCao, BoxLayout.Y_AXIS));
        pnlChieuCao.add(Box.createVerticalGlue());
        JLabel lblGiaTri = KyThuatUi.taoNhan(String.valueOf(thongKe.tinhTongCongViec()), UiTheme.BRAND_FONT);
        lblGiaTri.setMaximumSize(new Dimension(Integer.MAX_VALUE, lblGiaTri.getPreferredSize().height));
        lblGiaTri.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlChieuCao.add(lblGiaTri);
        pnlChieuCao.add(Box.createVerticalStrut(8));
        JPanel pnlThanh = new JPanel();
        pnlThanh.setBackground(UiTheme.PRIMARY);
        int chieuCao = thongKe.tinhTongCongViec() * 23;
        pnlThanh.setPreferredSize(new Dimension(80, chieuCao));
        pnlThanh.setMaximumSize(new Dimension(80, chieuCao));
        pnlThanh.setMinimumSize(new Dimension(80, chieuCao));
        pnlThanh.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlChieuCao.add(pnlThanh);
        pnlChieuCao.add(Box.createVerticalStrut(8));
        JLabel lblNgay = KyThuatUi.taoNhan(ngay, UiTheme.CAPTION_FONT);
        lblNgay.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblNgay.setMaximumSize(new Dimension(Integer.MAX_VALUE, lblNgay.getPreferredSize().height));
        pnlChieuCao.add(lblNgay);
        btnCot.add(pnlChieuCao, BorderLayout.CENTER);
        return btnCot;
    }

    /** @return thẻ lưu ý về các đề xuất chờ duyệt */
    // Hiển thị nội dung cần chú ý và cho phép văn bản xuống dòng khi cửa sổ hẹp.
    private JPanel taoTomTat() {
        JPanel pnlThe = KyThuatUi.taoThe();
        pnlThe.add(KyThuatUi.taoNhan("Tóm tắt cần chú ý", UiTheme.BRAND_FONT), BorderLayout.NORTH);
        JTextArea txaLuuY = new JTextArea(tongQuan.getChoKhachDuyet()
                + " đề xuất phát sinh đang chờ khách duyệt. Chỉ tạm dừng hạng mục liên quan"
                + " và tiếp tục các phần đã được duyệt.");
        txaLuuY.setFont(UiTheme.SHELL_FONT);
        txaLuuY.setForeground(UiTheme.TEXT);
        txaLuuY.setBackground(UiTheme.SURFACE);
        txaLuuY.setEditable(false);
        txaLuuY.setLineWrap(true);
        txaLuuY.setWrapStyleWord(true);
        pnlThe.add(txaLuuY, BorderLayout.CENTER);
        pnlThe.addComponentListener(new ComponentAdapter() {
            /** {@inheritDoc} */
            // Tăng chiều cao thẻ khi cửa sổ hẹp để phần lưu ý không bị che.
            @Override
            public void componentResized(ComponentEvent event) {
                int chieuCao = pnlThe.getWidth() < 950 ? 102 : 82;
                if (pnlThe.getPreferredSize().height != chieuCao) {
                    pnlThe.setPreferredSize(new Dimension(0, chieuCao));
                    pnlThe.setMinimumSize(new Dimension(0, chieuCao));
                    pnlThe.setMaximumSize(new Dimension(Integer.MAX_VALUE, chieuCao));
                    pnlThe.revalidate();
                }
            }
        });
        return pnlThe;
    }

    /** @return thẻ chứa các nút đi đến chức năng liên quan */
    // Tạo các nút lối vào và đăng ký đối tượng xử lý điều hướng của MainFrame.
    private JPanel taoLoiVao() {
        JPanel pnlThe = KyThuatUi.taoThe();
        pnlThe.add(KyThuatUi.taoNhan("Lối vào công việc", UiTheme.BRAND_FONT), BorderLayout.NORTH);
        JPanel pnlNut = new JPanel();
        pnlNut.setLayout(new BoxLayout(pnlNut, BoxLayout.X_AXIS));
        pnlNut.setOpaque(false);
        String[] nhanNut = {"Công việc được giao", "Xe chờ kiểm tra", "Báo cáo kiểm tra", "Tiến độ sửa chữa"};
        int[] chieuRong = {176, 150, 152, 152};
        for (int i = 0; i < nhanNut.length; i++) {
            String nhan = nhanNut[i];
            JButton btnLoiVao = KyThuatUi.taoNut(nhan, chieuRong[i]);
            btnLoiVao.addActionListener(boXuLyDieuHuong);
            if (i > 0) {
                pnlNut.add(Box.createHorizontalStrut(8));
            }
            btnLoiVao.setMinimumSize(new Dimension(chieuRong[i], 32));
            btnLoiVao.setMaximumSize(new Dimension(chieuRong[i], 32));
            pnlNut.add(btnLoiVao);
        }
        pnlThe.add(pnlNut, BorderLayout.CENTER);
        return pnlThe;
    }

    /** @param thongKe thống kê của ngày được chọn */
    // Mở hộp thoại modal hiển thị đúng ngày và số liệu người dùng đã chọn.
    private void moChiTiet(ThongKeNgayKyThuat thongKe) {
        ChiTietCongViecHoanTatDialog winChiTiet = new ChiTietCongViecHoanTatDialog(
                winChu, tongQuan.getTenKyThuatVien(), thongKe);
        winChiTiet.setVisible(true);
    }
}