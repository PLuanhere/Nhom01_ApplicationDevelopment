package ui.screen.quanly;

import dao.baocao.BaoCaoDAO;
import entity.baocao.BaoCao;
import entity.baogia.BaoGia;
import entity.nhansu.TaiKhoan;
import entity.phutung.PhuTung;
import entity.suachua.PhieuSuaChua;
import entity.suachua.TrangThaiSuaChua;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import ui.component.BaoCaoUi;
import ui.component.BieuDoDoanhThuPanel;
import ui.component.UiChung;
import ui.component.UiTheme;

/** Tổng quan điều hành chỉ đọc; số đếm và chi tiết cùng lấy từ DAO báo cáo. */
public final class TrangChuQuanLyPanel extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;
    private final TaiKhoan taiKhoan;
    private final BaoCaoDAO dao;
    private final ActionListener dieuHuong;
    private final DateTimeFormatter gio = DateTimeFormatter.ofPattern("dd/MM HH:mm");
    private List<PhieuSuaChua> dangSua;
    private List<PhieuSuaChua> choGiao;
    private List<BaoGia> choKhach;
    private List<PhuTung> canhBao;

    /** Nhận quyền hiện tại, nguồn chung và thao tác mở các mục Quản lý. */
    public TrangChuQuanLyPanel(TaiKhoan tk, BaoCaoDAO dao, ActionListener dieuHuong) {
        super(new BorderLayout());
        this.taiKhoan = tk;
        this.dao = dao;
        this.dieuHuong = dieuHuong;
        setOpaque(false);
        taiDuLieu();
    }

    /** Đọc lại số liệu khi mở Trang chủ hoặc bấm Làm mới. */
    public void taiDuLieu() {
        dao.kiemTraQuyen(taiKhoan);
        dangSua = dao.layLenhTrangChu(taiKhoan, false);
        choGiao = dao.layLenhTrangChu(taiKhoan, true);
        choKhach = dao.layBaoGiaChoKhach(taiKhoan);
        canhBao = dao.layCanhBaoTon(taiKhoan);
        removeAll();
        JPanel pnl = BaoCaoUi.taoCot();
        BaoCaoUi.them(pnl,
                UiChung.taoDauTrang("Quản lý / Hệ thống / Tổng quan điều hành", "Tổng quan điều hành",
                        "Theo dõi tình hình garage và các vướng mắc ảnh hưởng tiến độ.", nut("Làm mới", "lamMoi"),
                        nut("Báo cáo và thống kê", "baoCao")));

        // Bốn chỉ số mở được danh sách chi tiết tương ứng.
        JPanel chiSo = BaoCaoUi.taoLuoiChiSo();
        chiSo.setOpaque(false);
        List<String> xeDangSua = new ArrayList<>();
        List<String> xeHenHomNay = new ArrayList<>();
        for (PhieuSuaChua p : dangSua) {
            if (!xeDangSua.contains(p.getBienSo())) {
                xeDangSua.add(p.getBienSo());
            }
            if (p.getHanHoanThanh().toLocalDate().equals(LocalDate.now()) && !xeHenHomNay.contains(p.getBienSo())) {
                xeHenHomNay.add(p.getBienSo());
            }
        }
        chiSo.add(
                theChiSo("Xe đang sửa", xeDangSua.size(), xeHenHomNay.size() + " xe dự kiến xong hôm nay", "dangSua"));
        chiSo.add(theChiSo("Báo giá chờ khách", choKhach.size(), "Báo giá hiện hành đang chờ phản hồi", "choKhach"));
        chiSo.add(theChiSo("Chờ bàn giao", choGiao.size(), "Đã thanh toán, Tiếp nhận giao", "choGiao"));
        chiSo.add(theChiSo("Phụ tùng cảnh báo", canhBao.size(), "Tồn dưới hoặc bằng mức tối thiểu", "canhBao"));
        BaoCaoUi.them(pnl, chiSo);

        // Hai cột điều phối và doanh thu theo bố cục Figma.
        JPanel trai = BaoCaoUi.taoCot();
        BaoCaoUi.them(trai, BaoCaoUi.taoThe("Công việc cần điều phối", taoDieuPhoi()));
        BaoCaoUi.them(trai, BaoCaoUi.taoThe("Các lệnh gần đến hạn", taoGanHan()));
        JPanel phai = BaoCaoUi.taoCot();
        BaoCaoUi.them(phai, taoDoanhThu());
        JPanel duongTat = new JPanel(new GridLayout(2, 2, 8, 8));
        duongTat.setOpaque(false);
        for (String ten : new String[] { "Nhân viên", "Tài khoản & phân quyền", "Dịch vụ và bảng giá", "Phụ tùng" }) {
            duongTat.add(nut(ten, ten));
        }
        BaoCaoUi.them(phai, BaoCaoUi.taoThe("Đường tắt", duongTat));
        JPanel haiCot = new JPanel(new GridLayout(1, 2, 14, 0));
        haiCot.setOpaque(false);
        haiCot.add(trai);
        haiCot.add(phai);
        BaoCaoUi.them(pnl, haiCot);
        add(UiChung.taoVungCuon(pnl));
        revalidate();
        repaint();
    }

    /** Tạo nút có mã thao tác rõ ràng. */
    private JButton nut(String ten, String lenh) {
        JButton btn = UiChung.taoNut(ten, false);
        btn.setActionCommand(lenh);
        btn.addActionListener(this);
        return btn;
    }

    /** Tạo ô chỉ số có nhãn, giá trị và mô tả; bấm để xem toàn bộ chi tiết. */
    private JButton theChiSo(String ten, int so, String moTa, String lenh) {
        JButton btn = nut("", lenh);
        btn.setLayout(new BorderLayout(0, 7));
        btn.setPreferredSize(new Dimension(220, 112));
        btn.setBorder(
                BorderFactory.createCompoundBorder(btn.getBorder(), BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        btn.add(UiChung.taoNhan(ten, 12, false, UiTheme.APP_MUTED), BorderLayout.NORTH);
        btn.add(UiChung.taoNhan("" + so, 26, true, UiTheme.APP_TEXT));
        btn.add(UiChung.taoNhan(moTa, 11, false, UiTheme.APP_MUTED), BorderLayout.SOUTH);
        btn.setToolTipText(moTa + " · Bấm xem chi tiết");
        return btn;
    }

    /** Liệt kê báo giá chờ phản hồi, lệnh chờ phụ tùng và xe chờ bàn giao. */
    private JPanel taoDieuPhoi() {
        JPanel pnl = BaoCaoUi.taoCot();
        if (!choKhach.isEmpty()) {
            dongThongTin(pnl, choKhach.size() + " báo giá chờ khách phản hồi", "Xem các báo giá hiện hành", "choKhach");
        }
        int thieu = 0;
        for (PhieuSuaChua p : dangSua) {
            if (p.getTrangThai() == TrangThaiSuaChua.CHO_PHU_TUNG) {
                thieu++;
            }
        }
        if (thieu > 0) {
            dongThongTin(pnl, thieu + " lệnh đang chờ phụ tùng", "Kiểm tra kế hoạch cung ứng", "dangSua");
        }
        if (!choGiao.isEmpty()) {
            dongThongTin(pnl, choGiao.size() + " xe sẵn sàng bàn giao", "Thu ngân đã xác nhận thanh toán", "choGiao");
        }
        if (pnl.getComponentCount() == 0) {
            pnl.add(BaoCaoUi.moTa("Chưa có công việc cần điều phối."));
        }
        return pnl;
    }

    /** Hiển thị các lệnh chưa hoàn thành theo hạn gần nhất, gồm cả lệnh quá hạn. */
    private JPanel taoGanHan() {
        JPanel pnl = BaoCaoUi.taoCot();
        for (int i = 0; i < dangSua.size() && i < 3; i++) {
            PhieuSuaChua lenh = dangSua.get(i);
            String han = lenh.getHanHoanThanh().format(gio);
            if (lenh.getHanHoanThanh().isBefore(LocalDateTime.now())) {
                han = "Quá hạn · " + han;
            }
            dongThongTin(pnl, lenh.getMaPhieu() + " · " + lenh.getBienSo(), han, "dangSua");
        }
        if (dangSua.isEmpty()) {
            pnl.add(BaoCaoUi.moTa("Chưa có lệnh đang thực hiện."));
        } else {
            pnl.add(nut("Xem tất cả " + dangSua.size() + " lệnh", "dangSua"));
        }
        return pnl;
    }

    /** Tạo một dòng thông tin có thể bấm để tra cứu. */
    private void dongThongTin(JPanel pnl, String ten, String moTa, String lenh) {
        JButton btn = nut("<html>" + ten + "<br><small>" + moTa + "</small></html>", lenh);
        btn.setHorizontalAlignment(JButton.LEFT);
        btn.setPreferredSize(new Dimension(270, 60));
        JPanel dong = new JPanel(new BorderLayout());
        dong.setOpaque(false);
        dong.add(btn);
        BaoCaoUi.them(pnl, dong);
    }

    /** Vẽ doanh thu bảy ngày kết thúc hôm nay; mỗi cột mở đúng báo cáo ngày. */
    private JPanel taoDoanhThu() {
        LocalDate den = LocalDate.now();
        BaoCao bc = dao.lap(taiKhoan, BaoCao.DOANH_THU, den.minusDays(6), den);
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setOpaque(false);
        JPanel dau = BaoCaoUi.taoCot();
        dau.add(UiChung.taoNhan(UiChung.so(bc.getGiaTri()[0]) + " đ", 25, true, UiTheme.APP_TEXT));
        dau.add(UiChung.taoNhan(BaoCaoDAO.khoangNgay(bc), 11, false, UiTheme.APP_MUTED));
        pnl.add(dau, BorderLayout.NORTH);
        pnl.add(new BieuDoDoanhThuPanel(den, dao.doanhThuBayNgay(taiKhoan, den), new ActionListener() {
            /** Mở danh sách hóa đơn của đúng cột ngày được bấm. */
            @Override
            public void actionPerformed(ActionEvent event) {
                LocalDate ngay = LocalDate.parse(event.getActionCommand());
                BaoCaoUi.hienBaoCao(TrangChuQuanLyPanel.this, dao.lap(taiKhoan, BaoCao.DOANH_THU, ngay, ngay), taiKhoan,
                        dao);
            }
        }));
        return BaoCaoUi.taoThe("Doanh thu đã thanh toán", pnl);
    }

    /** Mở bảng chi tiết của chỉ số đang chọn, không cấp quyền sửa chứng từ. */
    private void xemChiSo(String lenh) {
        dao.kiemTraQuyen(taiKhoan);
        List<String[]> dong = new ArrayList<>();
        String[] cot;
        if (lenh.equals("choKhach")) {
            cot = new String[] { "Mã báo giá", "Biển số", "Gửi khách lúc", "Số tiền (đ)" };
            for (BaoGia bg : dao.layBaoGiaChoKhach(taiKhoan)) {
                dong.add(new String[] { bg.getMaBaoGia(), bg.getBienSo(), bg.getNgayGui().format(gio),
                        "" + bg.getTongTien() });
            }
        } else if (lenh.equals("canhBao")) {
            cot = new String[] { "Mã", "Tên phụ tùng", "Tồn hiện tại", "Tồn tối thiểu" };
            for (PhuTung pt : dao.layCanhBaoTon(taiKhoan)) {
                dong.add(new String[] { pt.getMaPhuTung(), pt.getTenPhuTung(), "" + pt.getSoLuongTon(),
                        "" + pt.getMucToiThieu() });
            }
        } else {
            cot = new String[] { "Mã lệnh", "Biển số", "Hạn hoàn thành", "Trạng thái" };
            for (PhieuSuaChua p : dao.layLenhTrangChu(taiKhoan, lenh.equals("choGiao"))) {
                dong.add(new String[] { p.getMaPhieu(), p.getBienSo(), p.getHanHoanThanh().format(gio),
                        lenh.equals("choGiao") ? "Đã thu tiền, chưa bàn giao" : p.getTrangThai().getTenHienThi() });
            }
        }
        JPanel pnl = BaoCaoUi.taoCot();
        BaoCaoUi.them(pnl, BaoCaoUi.taoThe("Chi tiết · " + dong.size() + " hồ sơ",
                BaoCaoUi.cuonBang(BaoCaoUi.taoBang(cot, dong))));
        BaoCaoUi.hienChiTiet(this, "Chi tiết tổng quan", pnl);
    }

    /** Làm mới, xem chi tiết hoặc chuyển tới chức năng Quản lý đã có. */
    @Override
    public void actionPerformed(ActionEvent event) {
        String lenh = event.getActionCommand();
        if (lenh.equals("lamMoi")) {
            taiDuLieu();
        } else if (lenh.equals("dangSua") || lenh.equals("choGiao") || lenh.equals("choKhach")
                || lenh.equals("canhBao")) {
            xemChiSo(lenh);
        } else {
            dieuHuong.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
                    lenh.equals("baoCao") ? "Báo cáo và thống kê" : lenh));
        }
    }
}
