package dao;

import connectDB.ConnectDB;
import dao.DuLieuGarageDAO;
import dao.NhanVienDAO;
import dao.TaiKhoanDAO;
import entity.BaoCao;
import entity.BaoGia;
import entity.BaoGia.TrangThaiBaoGia;
import entity.HoaDon.HinhThucThanhToan;
import entity.HoaDon;
import entity.PhieuTiepNhan.BaoCaoKiemTra;
import entity.NhanVien;
import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;
import entity.PhuTung;
import entity.PhieuSuaChua;
import entity.PhieuSuaChua.TrangThaiSuaChua;
import entity.PhieuTiepNhan;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Tổng hợp nguồn chung bằng vòng lặp; chỉ Quản lý được đọc và lưu báo cáo. */
public final class BaoCaoDAO {
    private final DuLieuGarageDAO nguon;
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
        if (tk == null || !new TaiKhoanDAO().phienConHopLe(tk)) {
            throw new SecurityException("Cần phiên đăng nhập còn hiệu lực.");
        }
        if (tk.getVaiTro() != VaiTro.QUAN_LY) {
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

    /** Tính doanh thu từ số tiền ròng đã xác minh, có tính điều chỉnh và hoàn tiền. */
    private BaoCao lapDoanhThu(TaiKhoan tk, LocalDate tu, LocalDate den) {
        long tong = 0;
        long tienMat = 0;
        long chuyenKhoan = 0;
        List<String[]> dong = new ArrayList<>();
        String sql = "SELECT d.MaHoaDon, d.ThoiDiemThanhToan, h.HinhThucThanhToan, "
                + "d.TienThucNhanConLai FROM dbo.vw_DoanhThuDaThu d "
                + "JOIN dbo.HoaDon h ON h.MaHoaDon = d.MaHoaDon "
                + "WHERE d.ThoiDiemThanhToan >= ? AND d.ThoiDiemThanhToan < ? "
                + "ORDER BY d.ThoiDiemThanhToan DESC, d.MaHoaDon DESC";
        try (PreparedStatement lenh = ketNoi().prepareStatement(sql)) {
            lenh.setDate(1, Date.valueOf(tu));
            lenh.setDate(2, Date.valueOf(den.plusDays(1)));
            try (ResultSet ketQua = lenh.executeQuery()) {
                while (ketQua.next()) {
                    long soTien = ketQua.getLong("TienThucNhanConLai");
                    tong = congTien(tong, soTien);
                    HinhThucThanhToan hinhThuc = HinhThucThanhToan.valueOf(
                            ketQua.getString("HinhThucThanhToan"));
                    if (hinhThuc == HinhThucThanhToan.TIEN_MAT) {
                        tienMat = congTien(tienMat, soTien);
                    } else {
                        chuyenKhoan = congTien(chuyenKhoan, soTien);
                    }
                    dong.add(new String[] {
                            ketQua.getString("MaHoaDon"),
                            ketQua.getTimestamp("ThoiDiemThanhToan").toLocalDateTime().format(GIO),
                            hinhThuc.getTenHienThi(),
                            String.valueOf(soTien)
                    });
                }
            }
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể lập báo cáo doanh thu.", ex);
        }
        return tao(tk, BaoCao.DOANH_THU, tu, den,
                new String[] { "Thu ròng đã xác minh", "Số hóa đơn", "Tiền mặt", "Chuyển khoản" },
                new long[] { tong, dong.size(), tienMat, chuyenKhoan },
                new String[] { "Mã hóa đơn", "Ngày thanh toán", "Hình thức", "Thu ròng (đ)" }, dong);
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

    /** Lưu nguyên bản báo cáo đã xem bằng procedure có transaction và idempotency. */
    public BaoCao luu(TaiKhoan tk, BaoCao ketQua) {
        kiemTraQuyen(tk);
        if (ketQua == null || !ketQua.getNguoiLap().equals(tk.getTenDangNhap())) {
            throw new IllegalArgumentException("Hãy lập báo cáo trước khi lưu.");
        }
        String maThaoTac = taoMaThaoTacBaoCao(ketQua);
        String sql = "{call dbo.usp_LuuBaoCao(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        String maBaoCao;
        try (CallableStatement lenh = ketNoi().prepareCall(sql)) {
            lenh.setString(1, tk.getMaNhanVien());
            lenh.setString(2, ketQua.getLoai());
            lenh.setDate(3, Date.valueOf(ketQua.getTuNgay()));
            lenh.setDate(4, Date.valueOf(ketQua.getDenNgay()));
            lenh.setString(5, "Bản chụp báo cáo đã lưu từ ứng dụng");
            lenh.setString(6, jsonChiSo(ketQua));
            lenh.setString(7, jsonMang(ketQua.getTenCot()));
            lenh.setString(8, jsonDong(ketQua.getCacDong()));
            lenh.setString(9, maThaoTac);
            try (ResultSet ketQuaSql = lenh.executeQuery()) {
                if (!ketQuaSql.next()) {
                    throw new IllegalStateException("SQL không trả mã báo cáo.");
                }
                maBaoCao = ketQuaSql.getString("MaBaoCao");
            }
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể lưu báo cáo.", ex);
        }
        return timBaoCaoTheoMa(tk, maBaoCao);
    }

    /** Tìm các snapshot đã lưu trong database, mới nhất trước. */
    public List<BaoCao> timDaLuu(TaiKhoan tk, String chu, String loai) {
        kiemTraQuyen(tk);
        return docBaoCaoDaLuu(chu, loai, null);
    }

    /** Lấy bảy ngày doanh thu để vẽ biểu đồ; mỗi cột dùng cùng nguồn với báo cáo. */
    public long[] doanhThuBayNgay(TaiKhoan tk, LocalDate den) {
        kiemTraQuyen(tk);
        if (den == null) {
            throw new IllegalArgumentException("Thiếu ngày biểu đồ.");
        }
        long[] ketQua = new long[7];
        LocalDate tu = den.minusDays(6);
        String sql = "SELECT CAST(ThoiDiemThanhToan AS DATE) AS Ngay, "
                + "SUM(CONVERT(DECIMAL(38, 0), TienThucNhanConLai)) AS Tong "
                + "FROM dbo.vw_DoanhThuDaThu "
                + "WHERE ThoiDiemThanhToan >= ? AND ThoiDiemThanhToan < ? "
                + "GROUP BY CAST(ThoiDiemThanhToan AS DATE)";
        try (PreparedStatement lenh = ketNoi().prepareStatement(sql)) {
            lenh.setDate(1, Date.valueOf(tu));
            lenh.setDate(2, Date.valueOf(den.plusDays(1)));
            try (ResultSet duLieu = lenh.executeQuery()) {
                while (duLieu.next()) {
                    LocalDate ngay = duLieu.getDate("Ngay").toLocalDate();
                    int viTri = (int) java.time.temporal.ChronoUnit.DAYS.between(tu, ngay);
                    if (viTri >= 0 && viTri < ketQua.length) {
                        ketQua[viTri] = duLieu.getLong("Tong");
                    }
                }
            }
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể đọc doanh thu bảy ngày.", ex);
        }
        return ketQua;
    }

    /** Đọc một snapshot theo mã, không tính lại số liệu hiện tại. */
    private BaoCao timBaoCaoTheoMa(TaiKhoan tk, String maBaoCao) {
        kiemTraQuyen(tk);
        List<BaoCao> danhSach = docBaoCaoDaLuu("", null, maBaoCao);
        if (danhSach.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy báo cáo đã lưu.");
        }
        return danhSach.get(0);
    }

    /** Đọc header, chỉ số, cột và ô bằng bốn truy vấn cố định, không N+1. */
    private List<BaoCao> docBaoCaoDaLuu(String chu, String loai, String maChinhXac) {
        String khoa = "%" + (chu == null ? "" : chu.trim()) + "%";
        String dieuKien = " WHERE (b.MaBaoCao LIKE ? OR b.LoaiBaoCao LIKE ? OR b.NguoiLap LIKE ?) "
                + "AND (? IS NULL OR b.LoaiBaoCao = ?) "
                + "AND (? IS NULL OR b.MaBaoCao = ?) ";
        List<BaoCaoDangDoc> cacBaoCao = new ArrayList<>();
        String sqlHeader = "SELECT b.MaBaoCao, b.LoaiBaoCao, b.TuNgay, b.DenNgay, "
                + "b.NgayLap, b.NguoiLap FROM dbo.BaoCao b "
                + dieuKien + "ORDER BY b.NgayLap DESC, b.MaBaoCao DESC";
        try (PreparedStatement lenh = ketNoi().prepareStatement(sqlHeader)) {
            ganDieuKienBaoCao(lenh, khoa, loai, maChinhXac);
            try (ResultSet ketQua = lenh.executeQuery()) {
                while (ketQua.next()) {
                    cacBaoCao.add(new BaoCaoDangDoc(
                            ketQua.getString("MaBaoCao"),
                            ketQua.getString("LoaiBaoCao"),
                            ketQua.getDate("TuNgay").toLocalDate(),
                            ketQua.getDate("DenNgay").toLocalDate(),
                            ketQua.getTimestamp("NgayLap").toLocalDateTime(),
                            ketQua.getString("NguoiLap")));
                }
            }
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể đọc báo cáo đã lưu.", ex);
        }
        if (cacBaoCao.isEmpty()) {
            return new ArrayList<>();
        }

        String sqlChiSo = "SELECT c.MaBaoCao, c.TenChiSo, c.GiaTri "
                + "FROM dbo.ChiSoBaoCao c JOIN dbo.BaoCao b ON b.MaBaoCao = c.MaBaoCao "
                + dieuKien + "ORDER BY c.MaBaoCao, c.ViTri";
        docChiSo(cacBaoCao, sqlChiSo, khoa, loai, maChinhXac);

        String sqlCot = "SELECT c.MaBaoCao, c.TenCot "
                + "FROM dbo.CotBaoCao c JOIN dbo.BaoCao b ON b.MaBaoCao = c.MaBaoCao "
                + dieuKien + "ORDER BY c.MaBaoCao, c.ViTri";
        docCot(cacBaoCao, sqlCot, khoa, loai, maChinhXac);

        String sqlO = "SELECT o.MaBaoCao, o.Dong, o.Cot, o.GiaTri "
                + "FROM dbo.OBaoCao o JOIN dbo.BaoCao b ON b.MaBaoCao = o.MaBaoCao "
                + dieuKien + "ORDER BY o.MaBaoCao, o.Dong, o.Cot";
        docO(cacBaoCao, sqlO, khoa, loai, maChinhXac);

        List<BaoCao> ketQua = new ArrayList<>();
        for (BaoCaoDangDoc baoCao : cacBaoCao) {
            ketQua.add(baoCao.taoBaoCao());
        }
        return ketQua;
    }

    private void docChiSo(List<BaoCaoDangDoc> cacBaoCao, String sql,
            String khoa, String loai, String maChinhXac) {
        try (PreparedStatement lenh = ketNoi().prepareStatement(sql)) {
            ganDieuKienBaoCao(lenh, khoa, loai, maChinhXac);
            try (ResultSet ketQua = lenh.executeQuery()) {
                while (ketQua.next()) {
                    BaoCaoDangDoc baoCao = timDangDoc(cacBaoCao, ketQua.getString("MaBaoCao"));
                    baoCao.tenChiSo.add(ketQua.getString("TenChiSo"));
                    baoCao.giaTri.add(ketQua.getLong("GiaTri"));
                }
            }
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể đọc chỉ số báo cáo.", ex);
        }
    }

    private void docCot(List<BaoCaoDangDoc> cacBaoCao, String sql,
            String khoa, String loai, String maChinhXac) {
        try (PreparedStatement lenh = ketNoi().prepareStatement(sql)) {
            ganDieuKienBaoCao(lenh, khoa, loai, maChinhXac);
            try (ResultSet ketQua = lenh.executeQuery()) {
                while (ketQua.next()) {
                    BaoCaoDangDoc baoCao = timDangDoc(cacBaoCao, ketQua.getString("MaBaoCao"));
                    baoCao.tenCot.add(ketQua.getString("TenCot"));
                }
            }
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể đọc cột báo cáo.", ex);
        }
    }

    private void docO(List<BaoCaoDangDoc> cacBaoCao, String sql,
            String khoa, String loai, String maChinhXac) {
        try (PreparedStatement lenh = ketNoi().prepareStatement(sql)) {
            ganDieuKienBaoCao(lenh, khoa, loai, maChinhXac);
            try (ResultSet ketQua = lenh.executeQuery()) {
                while (ketQua.next()) {
                    BaoCaoDangDoc baoCao = timDangDoc(cacBaoCao, ketQua.getString("MaBaoCao"));
                    int dong = ketQua.getInt("Dong");
                    int cot = ketQua.getInt("Cot");
                    while (baoCao.cacDong.size() <= dong) {
                        baoCao.cacDong.add(new String[baoCao.tenCot.size()]);
                    }
                    baoCao.cacDong.get(dong)[cot] = ketQua.getString("GiaTri");
                }
            }
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể đọc nội dung báo cáo.", ex);
        }
    }

    private void ganDieuKienBaoCao(PreparedStatement lenh, String khoa,
            String loai, String maChinhXac) throws SQLException {
        lenh.setString(1, khoa);
        lenh.setString(2, khoa);
        lenh.setString(3, khoa);
        if (loai == null) {
            lenh.setNull(4, java.sql.Types.NVARCHAR);
            lenh.setNull(5, java.sql.Types.NVARCHAR);
        } else {
            lenh.setString(4, loai);
            lenh.setString(5, loai);
        }
        if (maChinhXac == null) {
            lenh.setNull(6, java.sql.Types.VARCHAR);
            lenh.setNull(7, java.sql.Types.VARCHAR);
        } else {
            lenh.setString(6, maChinhXac);
            lenh.setString(7, maChinhXac);
        }
    }

    private BaoCaoDangDoc timDangDoc(List<BaoCaoDangDoc> danhSach, String maBaoCao) {
        for (BaoCaoDangDoc baoCao : danhSach) {
            if (baoCao.maBaoCao.equals(maBaoCao)) {
                return baoCao;
            }
        }
        throw new IllegalStateException("Dữ liệu chi tiết không có header báo cáo.");
    }

    private String taoMaThaoTacBaoCao(BaoCao baoCao) {
        String thoiDiem = baoCao.getNgayLap().toString()
                .replace(":", "")
                .replace(".", "");
        return "BC-" + baoCao.getNguoiLap() + "-" + thoiDiem;
    }

    private String jsonChiSo(BaoCao baoCao) {
        String[] ten = baoCao.getTenChiSo();
        long[] giaTri = baoCao.getGiaTri();
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < ten.length; i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append("{\"TenChiSo\":\"");
            json.append(thoatJson(ten[i]));
            json.append("\",\"GiaTri\":");
            json.append(giaTri[i]);
            json.append('}');
        }
        json.append(']');
        return json.toString();
    }

    private String jsonMang(String[] cacChu) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < cacChu.length; i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append('\"');
            json.append(thoatJson(cacChu[i]));
            json.append('\"');
        }
        json.append(']');
        return json.toString();
    }

    private String jsonDong(List<String[]> cacDong) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < cacDong.size(); i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append(jsonMang(cacDong.get(i)));
        }
        json.append(']');
        return json.toString();
    }

    private String thoatJson(String chu) {
        return chu.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    private Connection ketNoi() {
        try {
            return ConnectDB.getInstance().getConnection();
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể kết nối database QuanLiGarage.", ex);
        }
    }

    private static IllegalStateException loiDuLieu(String thongBao, SQLException ex) {
        return new IllegalStateException(thongBao + " " + ex.getMessage(), ex);
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

    /** Bộ chứa đơn giản để ghép bốn result set thành một entity báo cáo. */
    private static final class BaoCaoDangDoc {
        private final String maBaoCao;
        private final String loai;
        private final LocalDate tuNgay;
        private final LocalDate denNgay;
        private final LocalDateTime ngayLap;
        private final String nguoiLap;
        private final List<String> tenChiSo = new ArrayList<>();
        private final List<Long> giaTri = new ArrayList<>();
        private final List<String> tenCot = new ArrayList<>();
        private final List<String[]> cacDong = new ArrayList<>();

        private BaoCaoDangDoc(String maBaoCao, String loai, LocalDate tuNgay,
                LocalDate denNgay, LocalDateTime ngayLap, String nguoiLap) {
            this.maBaoCao = maBaoCao;
            this.loai = loai;
            this.tuNgay = tuNgay;
            this.denNgay = denNgay;
            this.ngayLap = ngayLap;
            this.nguoiLap = nguoiLap;
        }

        private BaoCao taoBaoCao() {
            String[] cacTenChiSo = tenChiSo.toArray(new String[0]);
            long[] cacGiaTri = new long[giaTri.size()];
            for (int i = 0; i < giaTri.size(); i++) {
                cacGiaTri[i] = giaTri.get(i);
            }
            String[] cacTenCot = tenCot.toArray(new String[0]);
            for (String[] dong : cacDong) {
                for (int i = 0; i < dong.length; i++) {
                    if (dong[i] == null) {
                        dong[i] = "";
                    }
                }
            }
            return new BaoCao(
                    maBaoCao,
                    loai,
                    tuNgay,
                    denNgay,
                    ngayLap,
                    nguoiLap,
                    cacTenChiSo,
                    cacGiaTri,
                    cacTenCot,
                    cacDong);
        }
    }

    /** Định dạng khoảng kỳ cho nhãn trên giao diện và bản xuất. */
    public static String khoangNgay(BaoCao bc) {
        return bc.getTuNgay().format(NGAY) + " – " + bc.getDenNgay().format(NGAY);
    }
}
