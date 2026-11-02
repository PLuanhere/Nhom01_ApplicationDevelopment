package dao.baocao;

import dao.chung.DuLieuGarageDAO;
import dao.nhansu.NhanVienDAO;
import dao.nhansu.TaiKhoanDAO;
import entity.baocao.BaoCao;
import entity.baogia.BaoGia;
import entity.baogia.TrangThaiBaoGia;
import entity.hoadon.HinhThucThanhToan;
import entity.hoadon.HoaDon;
import entity.kiemtra.BaoCaoKiemTra;
import entity.nhansu.NhanVien;
import entity.nhansu.TaiKhoan;
import entity.nhansu.VaiTro;
import entity.phutung.PhuTung;
import entity.suachua.PhieuSuaChua;
import entity.suachua.TrangThaiSuaChua;
import entity.tiepnhan.PhieuTiepNhan;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Tổng hợp nguồn chung bằng vòng lặp; chỉ Quản lý được đọc và lưu báo cáo. */
public final class BaoCaoDAO {
    private final DuLieuGarageDAO nguon;
    private final List<BaoCao> daLuu = new ArrayList<>();
    private final List<BaoCao> banGoc = new ArrayList<>();
    private int soTiepTheo = 1;
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private static final DateTimeFormatter GIO = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");

    /** Dùng đúng các DAO nghiệp vụ của lần chạy ứng dụng. */
    public BaoCaoDAO(DuLieuGarageDAO nguon) {
        if (nguon == null) {
            throw new IllegalArgumentException("Thiếu nguồn báo cáo.");
        }
        this.nguon = nguon;
    }

    /** Kiểm tra vai trò và phiên ngay tại thao tác báo cáo. */
    public void kiemTraQuyen(TaiKhoan tk) {
        if (tk == null || tk.getVaiTro() != VaiTro.QUAN_LY) {
            throw new SecurityException("Chỉ Quản lý được xem báo cáo.");
        }
        tk.kiemTraPhien();
    }

    /** Kiểm tra hai ngày có đủ và theo đúng thứ tự. */
    private void kiemTraNgay(LocalDate tu, LocalDate den) {
        if (tu == null || den == null || tu.isAfter(den)) {
            throw new IllegalArgumentException("Nhập đủ ngày; Từ ngày không được sau Đến ngày.");
        }
    }

    /** Xét một thời điểm thuộc kỳ, gồm cả ngày đầu và ngày cuối. */
    private boolean trongKy(LocalDateTime luc, LocalDate tu, LocalDate den) {
        return luc != null && !luc.toLocalDate().isBefore(tu) && !luc.toLocalDate().isAfter(den);
    }

    /** Lập đúng loại báo cáo; tồn kho luôn là ảnh chụp hiện tại. */
    public BaoCao lap(TaiKhoan tk, String loai, LocalDate tu, LocalDate den) {
        kiemTraQuyen(tk);
        kiemTraNgay(tu, den);
        if (BaoCao.DOANH_THU.equals(loai)) {
            return lapDoanhThu(tk, tu, den);
        }
        if (BaoCao.TIEP_NHAN.equals(loai)) {
            return lapSuaChua(tk, tu, den);
        }
        if (BaoCao.HIEU_SUAT.equals(loai)) {
            return lapHieuSuat(tk, tu, den);
        }
        if (BaoCao.TON_KHO.equals(loai)) {
            return lapTonKho(tk);
        }
        throw new IllegalArgumentException("Loại báo cáo không hợp lệ.");
    }

    /** Lọc hóa đơn đã thu theo ngày thanh toán, xếp mới nhất trước. */
    public List<HoaDon> layHoaDon(TaiKhoan tk, LocalDate tu, LocalDate den) {
        kiemTraQuyen(tk);
        kiemTraNgay(tu, den);
        List<HoaDon> ketQua = new ArrayList<>();
        for (HoaDon hd : nguon.getHoaDonDAO().layHoaDonBaoCao()) {
            if (trongKy(hd.getThoiDiemThanhToan(), tu, den)) {
                ketQua.add(hd);
            }
        }
        for (int i = 0; i < ketQua.size() - 1; i++) {
            for (int j = i + 1; j < ketQua.size(); j++) {
                if (ketQua.get(i).getThoiDiemThanhToan().isBefore(ketQua.get(j).getThoiDiemThanhToan())) {
                    HoaDon cu = ketQua.get(i);
                    ketQua.set(i, ketQua.get(j));
                    ketQua.set(j, cu);
                }
            }
        }
        return ketQua;
    }

    /** Cộng tiền nguyên đồng và chặn tràn số trước khi trả báo cáo. */
    private long congTien(long tong, long tien) {
        if (tien < 0 || Long.MAX_VALUE - tong < tien) {
            throw new IllegalStateException("Tổng doanh thu vượt giới hạn cho phép.");
        }
        return tong + tien;
    }

    /** Tra cứu hóa đơn chỉ đọc cho Quản lý, không mở thao tác thu tiền. */
    public HoaDon timHoaDon(TaiKhoan tk, String ma) {
        kiemTraQuyen(tk);
        for (HoaDon hd : nguon.getHoaDonDAO().layHoaDonBaoCao()) {
            if (hd.getMaHoaDon().equals(ma)) {
                return hd;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy hóa đơn.");
    }

    /** Tính doanh thu từ hóa đơn, không cộng tiền khách đưa hoặc tiền thừa. */
    private BaoCao lapDoanhThu(TaiKhoan tk, LocalDate tu, LocalDate den) {
        long tong = 0, tienMat = 0, chuyenKhoan = 0;
        List<String[]> dong = new ArrayList<>();
        for (HoaDon hd : layHoaDon(tk, tu, den)) {
            tong = congTien(tong, hd.getTongTien());
            if (hd.getHinhThucThanhToan() == HinhThucThanhToan.TIEN_MAT) {
                tienMat = congTien(tienMat, hd.getTongTien());
            } else {
                chuyenKhoan = congTien(chuyenKhoan, hd.getTongTien());
            }
            dong.add(new String[] { hd.getMaHoaDon(), hd.getThoiDiemThanhToan().format(GIO),
                    hd.getHinhThucThanhToan().getTenHienThi(), String.valueOf(hd.getTongTien()) });
        }
        return tao(tk, BaoCao.DOANH_THU, tu, den,
                new String[] { "Doanh thu đã thu", "Số hóa đơn", "Tiền mặt", "Chuyển khoản" },
                new long[] { tong, dong.size(), tienMat, chuyenKhoan },
                new String[] { "Mã hóa đơn", "Ngày thanh toán", "Hình thức", "Số tiền (đ)" }, dong);
    }

    /** Lấy lệnh phát hành trong kỳ; tiến độ là trạng thái hiện tại lúc lập báo cáo. */
    private List<PhieuSuaChua> layLenhTrongKy(LocalDate tu, LocalDate den) {
        List<PhieuSuaChua> ketQua = new ArrayList<>();
        for (PhieuSuaChua lenh : nguon.getSuaChuaDAO().layLenhBaoCao()) {
            if (trongKy(lenh.getThoiDiemPhatHanh(), tu, den)) {
                ketQua.add(lenh);
            }
        }
        return ketQua;
    }

    /** Lấy phiên bản báo giá hiện hành, không đếm lặp các lần điều chỉnh. */
    public List<BaoGia> layBaoGiaChoKhach(TaiKhoan tk) {
        kiemTraQuyen(tk);
        List<BaoGia> danhSach = nguon.getBaoGiaDAO().layBaoGiaBaoCao();
        List<BaoGia> ketQua = new ArrayList<>();
        for (BaoGia bg : danhSach) {
            if (bg.getTrangThai() != TrangThaiBaoGia.CHO_XAC_NHAN) {
                continue;
            }
            boolean moiNhat = true;
            for (BaoGia khac : danhSach) {
                if (bg.getMaBaoGiaGoc().equals(khac.getMaBaoGiaGoc()) && khac.getPhienBan() > bg.getPhienBan()) {
                    moiNhat = false;
                    break;
                }
            }
            if (moiNhat) {
                ketQua.add(bg);
            }
        }
        return ketQua;
    }

    /** Xác định lệnh đã hoàn thành, đã thu tiền và chưa được Tiếp nhận bàn giao. */
    private boolean choBanGiao(PhieuSuaChua lenh) {
        return lenh.getTrangThai() == TrangThaiSuaChua.HOAN_THANH
                && nguon.getHoaDonDAO().docCanCuBanGiao(lenh.getMaPhieu()) != null
                && nguon.getBanGiaoDAO().timGiaoNoiBo(lenh.getMaPhieu()) == null;
    }

    /** Tách lượt tiếp nhận, xe riêng biệt và lệnh sửa chữa thành các số đếm khác nhau. */
    private BaoCao lapSuaChua(TaiKhoan tk, LocalDate tu, LocalDate den) {
        int tiepNhan = 0, choKiemTra = 0, choKhach = 0, dangSua = 0, hoanThanh = 0, choGiao = 0;
        int choSua = 0, choPhuTung = 0;
        List<String> cacXe = new ArrayList<>();
        for (PhieuTiepNhan p : nguon.getTiepNhanDAO().layPhieuBaoCao()) {
            if (!trongKy(p.getThoiGianNhan(), tu, den)) {
                continue;
            }
            tiepNhan++;
            if (!cacXe.contains(p.getMaXe())) {
                cacXe.add(p.getMaXe());
            }
            boolean daKiemTra = false;
            for (BaoCaoKiemTra bc : nguon.getKiemTraDAO().layHoanTatNoiBo()) {
                if (bc.getPhieu().getMaPhieu().equals(p.getMaPhieu())) {
                    daKiemTra = true;
                    break;
                }
            }
            if (!daKiemTra) {
                choKiemTra++;
            }
        }
        for (BaoGia bg : layBaoGiaChoKhach(tk)) {
            if (trongKy(bg.getNgayGui(), tu, den)) {
                choKhach++;
            }
        }
        for (PhieuSuaChua lenh : layLenhTrongKy(tu, den)) {
            if (lenh.getTrangThai() == TrangThaiSuaChua.DANG_SUA) {
                dangSua++;
            }
            if (lenh.getTrangThai() == TrangThaiSuaChua.CHO_SUA_CHUA) {
                choSua++;
            }
            if (lenh.getTrangThai() == TrangThaiSuaChua.CHO_PHU_TUNG) {
                choPhuTung++;
            }
            if (lenh.getTrangThai() == TrangThaiSuaChua.HOAN_THANH) {
                hoanThanh++;
            }
            if (choBanGiao(lenh)) {
                choGiao++;
            }
        }
        List<String[]> dong = new ArrayList<>();
        dong.add(new String[] { "Chờ kiểm tra", "" + choKiemTra, "Phiếu tiếp nhận",
                "Tiếp nhận trong kỳ, chưa kiểm tra xong" });
        dong.add(new String[] { "Chờ khách xác nhận", "" + choKhach, "Báo giá",
                "Bản hiện hành gửi trong kỳ, chưa phản hồi" });
        dong.add(new String[] { "Chờ sửa chữa", "" + choSua, "Lệnh sửa chữa", "Phát hành trong kỳ, chưa bắt đầu" });
        dong.add(new String[] { "Đang sửa", "" + dangSua, "Lệnh sửa chữa", "Phát hành trong kỳ, đang thực hiện" });
        dong.add(new String[] { "Chờ phụ tùng", "" + choPhuTung, "Lệnh sửa chữa",
                "Phát hành trong kỳ, đang chờ phụ tùng" });
        dong.add(new String[] { "Hoàn thành", "" + hoanThanh, "Lệnh sửa chữa", "Phát hành trong kỳ, đã hoàn thành" });
        dong.add(new String[] { "Chờ bàn giao", "" + choGiao, "Lệnh sửa chữa",
                "Trong nhóm lệnh hoàn thành, đã thu đủ và chưa giao" });
        return tao(tk, BaoCao.TIEP_NHAN, tu, den,
                new String[] { "Lượt tiếp nhận", "Xe riêng biệt", "Đang sửa", "Đã hoàn thành" },
                new long[] { tiepNhan, cacXe.size(), dangSua, hoanThanh },
                new String[] { "Trạng thái", "Số hồ sơ", "Đối tượng đếm", "Ý nghĩa" }, dong);
    }

    /** Chụp toàn bộ danh mục và tồn hiện tại, gồm cả phụ tùng đã ngừng hoạt động. */
    private BaoCao lapTonKho(TaiKhoan tk) {
        List<String[]> dong = new ArrayList<>();
        int canhBao = 0;
        for (PhuTung pt : nguon.getKhoDAO().layDanhSach(tk)) {
            boolean thieu = pt.getSoLuongTon() <= pt.getMucToiThieu();
            if (thieu) {
                canhBao++;
            }
            dong.add(new String[] { pt.getMaPhuTung(), pt.getTenPhuTung(), pt.getDonViTinh(),
                    String.valueOf(pt.getSoLuongTon()), String.valueOf(pt.getMucToiThieu()),
                    thieu ? "Cảnh báo" : "Đủ tồn", pt.isHoatDong() ? "Đang hoạt động" : "Ngừng hoạt động" });
        }
        LocalDate homNay = LocalDate.now();
        return tao(tk, BaoCao.TON_KHO, homNay, homNay,
                new String[] { "Mã phụ tùng đang quản lý", "Dưới hoặc bằng ngưỡng", "Đang đủ tồn" },
                new long[] { dong.size(), canhBao, dong.size() - canhBao }, new String[] { "Mã", "Tên phụ tùng",
                        "Đơn vị", "Tồn hiện tại", "Tồn tối thiểu", "Kết quả", "Trạng thái" },
                dong);
    }

    /** Đếm mỗi lệnh cho kỹ thuật chính và hỗ trợ, mỗi người tối đa một lượt/lệnh. */
    private BaoCao lapHieuSuat(TaiKhoan tk, LocalDate tu, LocalDate den) {
        List<PhieuSuaChua> cacLenh = layLenhTrongKy(tu, den);
        List<String> taiKhoan = new ArrayList<>();
        for (TaiKhoan t : new TaiKhoanDAO().layDanhSach(tk)) {
            if (t.getVaiTro() == VaiTro.NHAN_VIEN_KY_THUAT) {
                taiKhoan.add(t.getTenDangNhap());
            }
        }
        for (PhieuSuaChua lenh : cacLenh) {
            if (!taiKhoan.contains(lenh.getKyThuatChinh())) {
                taiKhoan.add(lenh.getKyThuatChinh());
            }
            String hoTro = lenh.getKyThuatPhoiHop();
            if (!hoTro.isBlank() && !taiKhoan.contains(hoTro)) {
                taiKhoan.add(hoTro);
            }
        }
        List<String[]> dong = new ArrayList<>();
        int soHoanThanh = 0;
        for (PhieuSuaChua lenh : cacLenh) {
            if (lenh.getTrangThai() == TrangThaiSuaChua.HOAN_THANH) {
                soHoanThanh++;
            }
        }
        for (String ten : taiKhoan) {
            int giao = 0, xong = 0;
            for (PhieuSuaChua lenh : cacLenh) {
                if (ten.equals(lenh.getKyThuatChinh()) || ten.equals(lenh.getKyThuatPhoiHop())) {
                    giao++;
                    if (lenh.getTrangThai() == TrangThaiSuaChua.HOAN_THANH) {
                        xong++;
                    }
                }
            }
            String tyLe = giao == 0 ? "—" : String.valueOf(Math.round(xong * 10000.0 / giao) / 100.0) + "%";
            dong.add(new String[] { ten, tenNhanVien(tk, ten), "" + giao, "" + xong, "" + (giao - xong), tyLe });
        }
        return tao(tk, BaoCao.HIEU_SUAT, tu, den,
                new String[] { "Kỹ thuật viên", "Lệnh phát hành trong kỳ", "Lệnh đã hoàn thành" },
                new long[] { dong.size(), cacLenh.size(), soHoanThanh }, new String[] { "Tài khoản", "Nhân viên",
                        "Lệnh được giao", "Đã hoàn thành", "Chưa hoàn thành", "Tỷ lệ" },
                dong);
    }

    /** Tìm họ tên qua mã nhân viên, không ghép bằng tên hiển thị. */
    private String tenNhanVien(TaiKhoan tk, String tenTaiKhoan) {
        for (TaiKhoan t : new TaiKhoanDAO().layDanhSach(tk)) {
            if (t.getTenDangNhap().equals(tenTaiKhoan)) {
                for (NhanVien nv : new NhanVienDAO().layDanhSach(tk)) {
                    if (nv.getMaNhanVien().equals(t.getMaNhanVien())) {
                        return nv.getHoTen();
                    }
                }
            }
        }
        return tenTaiKhoan;
    }

    /** Tạo kết quả chưa lưu với thời điểm chụp số liệu. */
    private BaoCao tao(TaiKhoan tk, String loai, LocalDate tu, LocalDate den, String[] ten, long[] giaTri, String[] cot,
            List<String[]> dong) {
        return new BaoCao("", loai, tu, den, LocalDateTime.now(), tk.getTenDangNhap(), ten, giaTri, cot, dong);
    }

    /** Lưu đúng bản chụp đã xem; bấm lặp cùng kết quả không tạo báo cáo khác. */
    public BaoCao luu(TaiKhoan tk, BaoCao ketQua) {
        kiemTraQuyen(tk);
        if (ketQua == null || !ketQua.getNguoiLap().equals(tk.getTenDangNhap())) {
            throw new IllegalArgumentException("Hãy lập báo cáo trước khi lưu.");
        }
        for (int i = 0; i < daLuu.size(); i++) {
            if (banGoc.get(i) == ketQua || daLuu.get(i) == ketQua) {
                return daLuu.get(i);
            }
        }
        String ma = "BC-" + LocalDate.now().getYear() + "-" + String.format("%04d", soTiepTheo);
        BaoCao baoCao = new BaoCao(ma, ketQua.getLoai(), ketQua.getTuNgay(), ketQua.getDenNgay(), ketQua.getNgayLap(),
                ketQua.getNguoiLap(), ketQua.getTenChiSo(), ketQua.getGiaTri(), ketQua.getTenCot(),
                ketQua.getCacDong());
        daLuu.add(baoCao);
        banGoc.add(ketQua);
        soTiepTheo++;
        return baoCao;
    }

    /** Tìm và lọc báo cáo đã lưu, mới nhất trước; không tính lại bản cũ. */
    public List<BaoCao> timDaLuu(TaiKhoan tk, String chu, String loai) {
        kiemTraQuyen(tk);
        String khoa = chu == null ? "" : chu.trim().toLowerCase();
        List<BaoCao> ketQua = new ArrayList<>();
        for (int i = daLuu.size() - 1; i >= 0; i--) {
            BaoCao bc = daLuu.get(i);
            String noiDung = bc.getMaBaoCao() + " " + bc.getLoai() + " " + bc.getNguoiLap();
            if (noiDung.toLowerCase().contains(khoa) && (loai == null || loai.equals(bc.getLoai()))) {
                ketQua.add(bc);
            }
        }
        return ketQua;
    }

    /** Lấy bảy ngày doanh thu để vẽ biểu đồ; mỗi cột dùng cùng nguồn với báo cáo. */
    public long[] doanhThuBayNgay(TaiKhoan tk, LocalDate den) {
        kiemTraQuyen(tk);
        if (den == null) {
            throw new IllegalArgumentException("Thiếu ngày biểu đồ.");
        }
        long[] ketQua = new long[7];
        for (HoaDon hd : layHoaDon(tk, den.minusDays(6), den)) {
            for (int i = 0; i < 7; i++) {
                if (hd.getThoiDiemThanhToan().toLocalDate().equals(den.minusDays(6 - i))) {
                    ketQua[i] = congTien(ketQua[i], hd.getTongTien());
                }
            }
        }
        return ketQua;
    }

    /** Đọc lệnh còn trong xưởng hoặc xe đã thu tiền còn chờ giao. */
    public List<PhieuSuaChua> layLenhTrangChu(TaiKhoan tk, boolean choGiao) {
        kiemTraQuyen(tk);
        List<PhieuSuaChua> ketQua = new ArrayList<>();
        for (PhieuSuaChua lenh : nguon.getSuaChuaDAO().layLenhBaoCao()) {
            if (choGiao ? choBanGiao(lenh) : lenh.getTrangThai() != TrangThaiSuaChua.HOAN_THANH) {
                ketQua.add(lenh);
            }
        }
        for (int i = 0; i < ketQua.size() - 1; i++) {
            for (int j = i + 1; j < ketQua.size(); j++) {
                if (ketQua.get(i).getHanHoanThanh().isAfter(ketQua.get(j).getHanHoanThanh())) {
                    PhieuSuaChua cu = ketQua.get(i);
                    ketQua.set(i, ketQua.get(j));
                    ketQua.set(j, cu);
                }
            }
        }
        return ketQua;
    }

    /** Lấy phụ tùng dưới hoặc bằng ngưỡng từ chính nguồn tồn kho. */
    public List<PhuTung> layCanhBaoTon(TaiKhoan tk) {
        kiemTraQuyen(tk);
        List<PhuTung> ketQua = new ArrayList<>();
        for (PhuTung pt : nguon.getKhoDAO().layDanhSach(tk)) {
            if (pt.getSoLuongTon() <= pt.getMucToiThieu()) {
                ketQua.add(pt);
            }
        }
        return ketQua;
    }

    /** Định dạng khoảng kỳ cho nhãn trên giao diện và bản xuất. */
    public static String khoangNgay(BaoCao bc) {
        return bc.getTuNgay().format(NGAY) + " – " + bc.getDenNgay().format(NGAY);
    }
}
