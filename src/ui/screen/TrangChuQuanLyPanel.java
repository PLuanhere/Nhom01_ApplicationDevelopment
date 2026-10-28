package ui.screen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import entity.MucTongQuanQuanLy;
import entity.MucTongQuanQuanLy.Nhom;
import entity.TongQuanQuanLy;
import ui.component.BieuDoDoanhThuPanel;
import ui.component.QuanLyUi;
import ui.component.UiTheme;

/** Tổng quan Quản lý theo Page 3, gắn trong MainFrame; không tạo lại header/sidebar hoặc sửa nghiệp vụ. */
public final class TrangChuQuanLyPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    /** Màu trạng thái chỉ phục vụ trình bày, không dùng màu để quyết định nghiệp vụ. */
    private static final Color CAN_CHU_Y = new Color(0xA16607);
    private static final Color SAN_SANG = new Color(0x08784B);
    /** Ngày giờ phải thể hiện rõ vì dữ liệu tạo sẵn không phải thời gian hiện tại. */
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final TongQuanQuanLy tongQuan;
    /** Các vùng co giãn, chuyển sang một cột khi cửa sổ hẹp. */
    private final JPanel pnlChiSo;
    private final JPanel pnlCacCot = new JPanel(new GridBagLayout());
    private final JPanel pnlTrai;
    private final JPanel pnlPhai;
    private final JPanel pnlDuongTat;
    private boolean boCucHep;

    /**
     * Ghép trang từ snapshot được DAO cấp và callback xem chi tiết.
     * @param tongQuan nguồn của mọi chỉ số/biểu đồ
     * @param khiChonNgay thao tác đọc và mở chi tiết đúng ngày
     */
    public TrangChuQuanLyPanel(TongQuanQuanLy tongQuan, Consumer<LocalDate> khiChonNgay) {
        super(new BorderLayout(0, 16));
        this.tongQuan = Objects.requireNonNull(tongQuan);
        Objects.requireNonNull(khiChonNgay);
        setBackground(UiTheme.APP_BACKGROUND);
        pnlChiSo = taoChiSo();
        pnlDuongTat = taoDuongTat();
        pnlTrai = taoCotTrai();
        pnlPhai = taoCotPhai(khiChonNgay);
        add(taoPhanDau(), BorderLayout.NORTH);

        // Chiều rộng bám viewport; nội dung dài cuộn dọc, không đẩy nút ra ngoài cửa sổ.
        JPanel pnlNoiDung = new JPanel(new BorderLayout(0, 16)) {
            private static final long serialVersionUID = 1L;

            /** {@inheritDoc} */
            @Override
            public Dimension getPreferredSize() {
                Dimension kichThuoc = super.getPreferredSize();
                return new Dimension(0, kichThuoc.height);
            }
        };
        pnlNoiDung.setOpaque(false);
        pnlNoiDung.add(pnlChiSo, BorderLayout.NORTH);
        pnlCacCot.setOpaque(false);
        pnlNoiDung.add(pnlCacCot, BorderLayout.CENTER);
        JScrollPane scr = new JScrollPane(pnlNoiDung);
        scr.setBorder(BorderFactory.createEmptyBorder());
        scr.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scr.getViewport().setBackground(UiTheme.APP_BACKGROUND);
        scr.getVerticalScrollBar().setUnitIncrement(24);
        scr.getViewport().addComponentListener(new ComponentAdapter() {
            /** {@inheritDoc} */
            @Override
            public void componentResized(ComponentEvent event) {
                capNhatBoCuc(scr.getViewport().getWidth() < 900);
            }
        });
        add(scr, BorderLayout.CENTER);
        capNhatBoCuc(false);
    }

    /** {@inheritDoc} */
    @Override
    public void doLayout() {
        int le = Math.max(0, (getWidth() - 1588) / 2);
        if (getInsets().left != le) {
            setBorder(BorderFactory.createEmptyBorder(0, le, 0, le));
        }
        super.doLayout();
    }

    /** @return breadcrumb, tiêu đề và nút cuộn đến tác vụ thường dùng */
    private JPanel taoPhanDau() {
        JPanel pnl = new JPanel(new BorderLayout(0, 12));
        pnl.setOpaque(false);
        pnl.add(QuanLyUi.taoNhan("Quản lý / Hệ thống / Tổng quan điều hành", 11.5f, false,
                UiTheme.APP_MUTED), BorderLayout.NORTH);
        JPanel pnlTieuDe = new JPanel(new BorderLayout(0, 5));
        pnlTieuDe.setOpaque(false);
        pnlTieuDe.add(QuanLyUi.taoNhan("Tổng quan điều hành", 24, true,
                UiTheme.APP_TEXT), BorderLayout.NORTH);
        String thoiDiem = tongQuan.getThoiDiem().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        pnlTieuDe.add(QuanLyUi.taoNhan("Theo dõi tình hình garage · Dữ liệu đến " + thoiDiem,
                12, false, UiTheme.APP_MUTED), BorderLayout.SOUTH);
        JPanel pnlHang = new JPanel(new BorderLayout(12, 0));
        pnlHang.setOpaque(false);
        pnlHang.add(pnlTieuDe, BorderLayout.CENTER);
        JButton btnTacVu = UiTheme.createAppButton("Tác vụ thường dùng", false);
        btnTacVu.setPreferredSize(new Dimension(162, 36));
        btnTacVu.addActionListener(event -> pnlDuongTat.scrollRectToVisible(
                new Rectangle(0, 0, pnlDuongTat.getWidth(), pnlDuongTat.getHeight())));
        JPanel pnlNut = new JPanel(new BorderLayout());
        pnlNut.setOpaque(false);
        pnlNut.add(btnTacVu, BorderLayout.NORTH);
        pnlHang.add(pnlNut, BorderLayout.EAST);
        pnl.add(pnlHang, BorderLayout.CENTER);
        return pnl;
    }

    /** @return bốn chỉ số lấy số lượng từ hồ sơ, không đặt số riêng trên nhãn */
    private JPanel taoChiSo() {
        JPanel pnl = new JPanel(new GridLayout(1, 4, 12, 12));
        pnl.setOpaque(false);
        pnl.add(taoChiSo("Lệnh đang xử lý", tongQuan.getCacMuc(Nhom.LENH_DANG_XU_LY).size(),
                tongQuan.getLenhDenHanTrongNgay().size() + " lệnh đến hạn ngày "
                        + tongQuan.getDenNgay().format(DateTimeFormatter.ofPattern("dd/MM")), UiTheme.APP_MUTED));
        long canLienHe = tongQuan.getCacMuc(Nhom.BAO_GIA_CHO_KHACH).stream()
                .filter(MucTongQuanQuanLy::isCanChuY).count();
        pnl.add(taoChiSo("Báo giá chờ khách", tongQuan.getCacMuc(Nhom.BAO_GIA_CHO_KHACH).size(),
                canLienHe + " trường hợp cần liên hệ", CAN_CHU_Y));
        pnl.add(taoChiSo("Chờ bàn giao", tongQuan.getCacMuc(Nhom.XE_CHO_BAN_GIAO).size(),
                "Đã thanh toán, tiếp nhận giao", SAN_SANG));
        pnl.add(taoChiSo("Phụ tùng cảnh báo", tongQuan.getCacMuc(Nhom.PHU_TUNG_CANH_BAO).size(),
                "Theo tồn và ngưỡng được ghi nhận", UiTheme.ERROR));
        return pnl;
    }

    /**
     * @param ten tên chỉ số
     * @param soLuong số hồ sơ được đếm
     * @param moTa ngữ cảnh của số lượng
     * @param mau màu nhấn mô tả
     * @return thẻ chỉ số cao theo font thực tế
     */
    private JPanel taoChiSo(String ten, int soLuong, String moTa, Color mau) {
        JPanel pnl = QuanLyUi.taoThe(new BorderLayout(0, 5));
        pnl.setBorder(BorderFactory.createCompoundBorder(pnl.getBorder(),
                BorderFactory.createEmptyBorder(15, 16, 15, 16)));
        pnl.add(QuanLyUi.taoNhan(ten, 12, false, UiTheme.APP_MUTED), BorderLayout.NORTH);
        pnl.add(QuanLyUi.taoNhan(String.format("%02d", soLuong), 26, true, UiTheme.APP_TEXT));
        pnl.add(QuanLyUi.taoNhan(moTa, 11, false, mau), BorderLayout.SOUTH);
        return pnl;
    }

    /** @return cột công việc cần điều phối và lệnh đến hạn trong ngày */
    private JPanel taoCotTrai() {
        JPanel pnl = taoCot();
        pnl.add(taoDieuPhoi());
        pnl.add(Box.createVerticalStrut(16));
        JPanel pnlHan = QuanLyUi.taoThe(new BorderLayout());
        pnlHan.add(QuanLyUi.taoDauThe("Lệnh đến hạn trong ngày",
                "Ngày " + tongQuan.getDenNgay().format(NGAY)), BorderLayout.NORTH);
        JPanel pnlDanhSach = taoDanhSach();
        List<MucTongQuanQuanLy> cacLenh = tongQuan.getLenhDenHanTrongNgay();
        for (MucTongQuanQuanLy muc : cacLenh) {
            String moTa = muc.getMoTa() + " · hạn "
                    + muc.getHanXuLy().format(DateTimeFormatter.ofPattern("HH:mm"));
            pnlDanhSach.add(taoDong(muc.getMa(), moTa, muc.getTrangThai(),
                    muc.isCanChuY() ? CAN_CHU_Y : UiTheme.APP_PRIMARY));
        }
        if (cacLenh.isEmpty()) {
            pnlDanhSach.add(QuanLyUi.taoNhan("Không có lệnh đến hạn trong ngày này.", 12, false,
                    UiTheme.APP_MUTED));
        }
        pnlHan.add(pnlDanhSach);
        pnl.add(pnlHan);
        return pnl;
    }

    /** @return nhắc việc được tính từ các hồ sơ đang cần chú ý */
    private JPanel taoDieuPhoi() {
        JPanel pnl = QuanLyUi.taoThe(new BorderLayout());
        pnl.add(QuanLyUi.taoDauThe("Công việc cần điều phối", "Ưu tiên theo ảnh hưởng đến tiến độ"),
                BorderLayout.NORTH);
        JPanel pnlDanhSach = taoDanhSach();
        List<MucTongQuanQuanLy> baoGia = tongQuan.getCacMuc(Nhom.BAO_GIA_CHO_KHACH).stream()
                .filter(MucTongQuanQuanLy::isCanChuY).toList();
        String maBaoGia = String.join(", ", baoGia.stream().map(MucTongQuanQuanLy::getMa).toList());
        pnlDanhSach.add(taoDong(baoGia.size() + " báo giá cần liên hệ",
                baoGia.isEmpty() ? "Không có báo giá cần nhắc liên hệ." : maBaoGia, "Chờ khách", CAN_CHU_Y));
        List<MucTongQuanQuanLy> thieuPhuTung = tongQuan.getCacMuc(Nhom.LENH_DANG_XU_LY).stream()
                .filter(MucTongQuanQuanLy::isCanChuY).toList();
        String maLenh = String.join(", ", thieuPhuTung.stream().map(MucTongQuanQuanLy::getMa).toList());
        pnlDanhSach.add(taoDong(thieuPhuTung.size() + " lệnh đang chờ phụ tùng",
                thieuPhuTung.isEmpty() ? "Không có lệnh đang chờ phụ tùng." : maLenh,
                "Chờ phụ tùng", UiTheme.ERROR));
        int choGiao = tongQuan.getCacMuc(Nhom.XE_CHO_BAN_GIAO).size();
        pnlDanhSach.add(taoDong(choGiao + " xe sẵn sàng bàn giao", "Thu ngân đã xác nhận thanh toán",
                "Tiếp nhận giao", SAN_SANG));
        pnl.add(pnlDanhSach);
        return pnl;
    }

    /**
     * @param khiChonNgay mở đúng ngày khi chọn cột
     * @return cột doanh thu và đường tắt
     */
    private JPanel taoCotPhai(Consumer<LocalDate> khiChonNgay) {
        JPanel pnl = taoCot();
        JPanel pnlDoanhThu = QuanLyUi.taoThe(new BorderLayout());
        pnlDoanhThu.add(QuanLyUi.taoDauThe("Doanh thu đã thanh toán",
                tongQuan.getTuNgay().format(NGAY) + " – " + tongQuan.getDenNgay().format(NGAY)), BorderLayout.NORTH);
        JPanel pnlDuLieu = new JPanel(new BorderLayout(0, 8));
        pnlDuLieu.setOpaque(false);
        pnlDuLieu.setBorder(BorderFactory.createEmptyBorder(16, 17, 16, 17));
        JPanel pnlTong = new JPanel(new BorderLayout(0, 4));
        pnlTong.setOpaque(false);
        pnlTong.add(QuanLyUi.taoNhan(QuanLyUi.dinhDangTien(tongQuan.tinhDoanhThuTrongKy()),
                26, true, UiTheme.APP_TEXT), BorderLayout.NORTH);
        pnlTong.add(QuanLyUi.taoNhan("Tổng đã thu trong kỳ theo ngày thanh toán", 11, false,
                UiTheme.APP_MUTED), BorderLayout.SOUTH);
        pnlDuLieu.add(pnlTong, BorderLayout.NORTH);
        pnlDuLieu.add(new BieuDoDoanhThuPanel(tongQuan, khiChonNgay), BorderLayout.CENTER);
        pnlDuLieu.add(QuanLyUi.taoNhan("Di chuột xem số tiền · Nhấn cột để đối chiếu hóa đơn",
                11, false, UiTheme.APP_MUTED), BorderLayout.SOUTH);
        pnlDoanhThu.add(pnlDuLieu);
        pnl.add(pnlDoanhThu);
        pnl.add(Box.createVerticalStrut(16));
        pnl.add(pnlDuongTat);
        return pnl;
    }

    /** @return các điểm mở theo Figma; khóa rõ khi màn đích chưa được triển khai */
    private JPanel taoDuongTat() {
        JPanel pnl = QuanLyUi.taoThe(new BorderLayout());
        pnl.add(QuanLyUi.taoDauThe("Đường tắt", "Các chức năng Quản lý"), BorderLayout.NORTH);
        JPanel pnlNut = new JPanel(new GridLayout(2, 2, 8, 8));
        pnlNut.setOpaque(false);
        pnlNut.setBorder(BorderFactory.createEmptyBorder(16, 17, 16, 17));
        for (String ten : List.of("Nhân viên", "Tài khoản & phân quyền", "Dịch vụ và bảng giá", "Phụ tùng")) {
            JButton btn = UiTheme.createAppButton(ten, false);
            btn.setFont(UiTheme.APP_FONT.deriveFont(12.0f));
            btn.setEnabled(false);
            btn.setToolTipText(ten + ": màn hình chưa được triển khai.");
            pnlNut.add(btn);
        }
        pnl.add(pnlNut);
        return pnl;
    }

    /** @return cột dọc không có nền riêng */
    private JPanel taoCot() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setOpaque(false);
        return pnl;
    }

    /** @return vùng danh sách có lề, mỗi dòng tự tính chiều cao */
    private JPanel taoDanhSach() {
        JPanel pnl = new JPanel(new GridLayout(0, 1, 0, 0));
        pnl.setOpaque(false);
        pnl.setBorder(BorderFactory.createEmptyBorder(4, 17, 4, 17));
        return pnl;
    }

    /**
     * @param tieuDe nội dung chính
     * @param moTa thông tin đối chiếu
     * @param trangThai trạng thái dạng chữ, không chỉ dùng màu
     * @param mau màu nhấn
     * @return một dòng nhắc việc
     */
    private JPanel taoDong(String tieuDe, String moTa, String trangThai, Color mau) {
        JPanel pnl = new JPanel(new BorderLayout(11, 0));
        pnl.setOpaque(false);
        pnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE9EDF2)),
                BorderFactory.createEmptyBorder(12, 0, 12, 0)));
        JPanel pnlChu = new JPanel(new BorderLayout(0, 3));
        pnlChu.setOpaque(false);
        pnlChu.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 3, 0, 0, mau),
                BorderFactory.createEmptyBorder(0, 10, 0, 0)));
        pnlChu.add(QuanLyUi.taoNhan(tieuDe, 12.5f, true, UiTheme.APP_TEXT), BorderLayout.NORTH);
        pnlChu.add(QuanLyUi.taoNhan(moTa, 11.5f, false, UiTheme.APP_MUTED), BorderLayout.SOUTH);
        pnl.add(pnlChu, BorderLayout.CENTER);
        JLabel lblTrangThai = QuanLyUi.taoNhan(trangThai, 10.5f, true, mau);
        lblTrangThai.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
        JPanel pnlNhan = UiTheme.roundedPanel(new BorderLayout(),
                new Color(mau.getRed(), mau.getGreen(), mau.getBlue(), 18), 4);
        pnlNhan.add(lblTrangThai);
        JPanel pnlTrangThai = new JPanel(new GridBagLayout());
        pnlTrangThai.setOpaque(false);
        pnlTrangThai.add(pnlNhan);
        pnl.add(pnlTrangThai, BorderLayout.EAST);
        return pnl;
    }

    /**
     * Bố cục desktop hai cột theo tỷ lệ Figma; cửa sổ hẹp chuyển thành một cột và cuộn dọc.
     * @param hep true nếu vùng nội dung hẹp hơn 900 px
     */
    private void capNhatBoCuc(boolean hep) {
        if (pnlCacCot.getComponentCount() > 0 && boCucHep == hep) {
            return;
        }
        boCucHep = hep;
        pnlChiSo.setLayout(new GridLayout(hep ? 2 : 1, hep ? 2 : 4, 12, 12));
        pnlCacCot.removeAll();
        GridBagConstraints viTri = new GridBagConstraints();
        viTri.fill = GridBagConstraints.HORIZONTAL;
        viTri.anchor = GridBagConstraints.NORTH;
        viTri.weightx = hep ? 1 : 0.59;
        viTri.weighty = 1;
        viTri.insets = new Insets(0, 0, hep ? 16 : 0, hep ? 0 : 16);
        pnlCacCot.add(pnlTrai, viTri);
        viTri.gridx = hep ? 0 : 1;
        viTri.gridy = hep ? 1 : 0;
        viTri.weightx = hep ? 1 : 0.41;
        viTri.insets = new Insets(0, 0, 0, 0);
        pnlCacCot.add(pnlPhai, viTri);
        pnlCacCot.revalidate();
        pnlCacCot.repaint();
    }
}
