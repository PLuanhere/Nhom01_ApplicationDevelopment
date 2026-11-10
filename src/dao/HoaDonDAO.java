package dao;

import connectDB.ConnectDB;
import entity.BanGiaoXe.CanCuBanGiao;
import entity.BaoGia.ChiTietBaoGia;
import entity.HoaDon;
import entity.HoaDon.HinhThucThanhToan;
import entity.ChiTietHoaDon;
import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Đọc và ghi hóa đơn Thu ngân trong SQL Server theo schema QuanLiGarage_v2. */
public final class HoaDonDAO {

    /** Tạo DAO dùng nguồn SQL Server. */
    public HoaDonDAO() {
    }

    /** Giữ tương thích với nơi khởi tạo cũ; nguồn sửa chữa được đọc trực tiếp từ SQL. */
    public HoaDonDAO(PhieuSuaChuaDAO suaChuaDAO) {
        if (suaChuaDAO == null) {
            throw new IllegalArgumentException("Thiếu nguồn sửa chữa.");
        }
    }

    /** Đọc view các lệnh hoàn thành chưa có hóa đơn. */
    public List<PhieuChoThu> layPhieuChoThu(TaiKhoan taiKhoan) {
        kiemTraThuNgan(taiKhoan);
        String sql = "SELECT p.MaLenh, p.TenKhachHang, p.BienSo, p.SoTienPhaiThu, sc.ThoiDiemHoanTat "
                + "FROM dbo.vw_PhieuChoThu p JOIN dbo.PhieuSuaChua sc ON sc.MaPhieu = p.MaLenh "
                + "ORDER BY sc.ThoiDiemHoanTat, p.MaLenh";
        List<PhieuChoThu> ketQua = new ArrayList<>();
        try (PreparedStatement lenh = ketNoi().prepareStatement(sql); ResultSet dong = lenh.executeQuery()) {
            while (dong.next()) {
                ketQua.add(docPhieuChoThu(dong));
            }
            return ketQua;
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể đọc phiếu chờ thu.", ex);
        }
    }

    /** Tìm phiếu còn chờ thu theo mã. */
    public PhieuChoThu timPhieuTheoMa(TaiKhoan taiKhoan, String maPhieu) {
        kiemTraThuNgan(taiKhoan);
        String ma = batBuoc(maPhieu, "Mã phiếu");
        String sql = "SELECT p.MaLenh, p.TenKhachHang, p.BienSo, p.SoTienPhaiThu, sc.ThoiDiemHoanTat "
                + "FROM dbo.vw_PhieuChoThu p JOIN dbo.PhieuSuaChua sc ON sc.MaPhieu = p.MaLenh "
                + "WHERE p.MaLenh = ?";
        try (PreparedStatement lenh = ketNoi().prepareStatement(sql)) {
            lenh.setString(1, ma);
            try (ResultSet dong = lenh.executeQuery()) {
                return dong.next() ? docPhieuChoThu(dong) : null;
            }
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể tìm phiếu chờ thu.", ex);
        }
    }

    /** Đọc hóa đơn đã thanh toán, mới nhất trước. */
    public List<HoaDon> layHoaDonDaThu(TaiKhoan taiKhoan) {
        kiemTraThuNgan(taiKhoan);
        String sql = "SELECT MaHoaDon, MaPhieuSuaChua, TenKhachHang, BienSo, TongTien, "
                + "HinhThucThanhToan, ThoiDiemThanhToan, NguoiThucHien, MaGiaoDich "
                + "FROM dbo.HoaDon ORDER BY ThoiDiemThanhToan DESC, MaHoaDon DESC";
        return docDanhSachHoaDon(sql, null);
    }

    /** Tìm hóa đơn bằng mã, trả null khi không tồn tại. */
    public HoaDon timHoaDonTheoMa(TaiKhoan taiKhoan, String maHoaDon) {
        kiemTraThuNgan(taiKhoan);
        String ma = batBuoc(maHoaDon, "Mã hóa đơn");
        String sql = "SELECT MaHoaDon, MaPhieuSuaChua, TenKhachHang, BienSo, TongTien, "
                + "HinhThucThanhToan, ThoiDiemThanhToan, NguoiThucHien, MaGiaoDich "
                + "FROM dbo.HoaDon WHERE MaHoaDon = ?";
        List<HoaDon> ketQua = docDanhSachHoaDon(sql, ma);
        return ketQua.isEmpty() ? null : ketQua.get(0);
    }

    /** Đọc thông tin đơn vị đã được quản trị xác nhận để in chứng từ; không dùng dữ liệu giả. */
    public String[] docThongTinGarage(TaiKhoan taiKhoan) {
        kiemTraThuNgan(taiKhoan);
        String sql = "SELECT TenGarage,DiaChi,SoDienThoai,MaSoThue FROM dbo.ThongTinGarage "
                + "WHERE MaCauHinh=1 AND DangSuDung=1";
        try (PreparedStatement lenh = ketNoi().prepareStatement(sql); ResultSet rs = lenh.executeQuery()) {
            if (!rs.next()) return null;
            String ten = rs.getString(1), diaChi = rs.getString(2), dienThoai = rs.getString(3), mst = rs.getString(4);
            if (ten == null || ten.isBlank() || diaChi == null || diaChi.isBlank()
                    || dienThoai == null || dienThoai.isBlank()) return null;
            return new String[] {ten.trim(), diaChi.trim(), dienThoai.trim(), mst == null ? "" : mst.trim()};
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể đọc thông tin garage để in hóa đơn.", ex);
        }
    }

    /** Tìm hóa đơn theo mã, khách, biển số, lệnh và hình thức thanh toán. */
    public List<HoaDon> timKiem(TaiKhoan taiKhoan, String tuKhoa, HinhThucThanhToan hinhThuc) {
        kiemTraThuNgan(taiKhoan);
        String khoa = tuKhoa == null ? "" : tuKhoa.trim();
        String sql = "SELECT MaHoaDon, MaPhieuSuaChua, TenKhachHang, BienSo, TongTien, "
                + "HinhThucThanhToan, ThoiDiemThanhToan, NguoiThucHien, MaGiaoDich FROM dbo.HoaDon "
                + "WHERE (MaHoaDon LIKE ? OR MaPhieuSuaChua LIKE ? OR TenKhachHang LIKE ? OR BienSo LIKE ?) "
                + "AND (? IS NULL OR HinhThucThanhToan = ?) "
                + "ORDER BY ThoiDiemThanhToan DESC, MaHoaDon DESC";
        try (PreparedStatement lenh = ketNoi().prepareStatement(sql)) {
            String mau = "%" + khoa + "%";
            for (int i = 1; i <= 4; i++) {
                lenh.setString(i, mau);
            }
            String maHinhThuc = hinhThuc == null ? null : hinhThuc.name();
            lenh.setString(5, maHinhThuc);
            lenh.setString(6, maHinhThuc);
            return docHoaDon(lenh);
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể tìm hóa đơn.", ex);
        }
    }

    /** Lập hóa đơn tiền mặt sau khi đối chiếu đủ tiền. */
    public String xacNhanThanhToanTienMat(TaiKhoan taiKhoan, String maPhieu, long soTienNhan) {
        return xacNhanThanhToan(taiKhoan, maPhieu, soTienNhan, HinhThucThanhToan.TIEN_MAT);
    }

    /** Lập hóa đơn theo hình thức đã chọn. */
    public String xacNhanThanhToan(TaiKhoan taiKhoan, String maPhieu, long soTienNhan,
            HinhThucThanhToan hinhThuc) {
        return ghiThanhToan(taiKhoan, maPhieu, soTienNhan, hinhThuc, "");
    }

    /** Chuyển khoản bắt buộc có mã tham chiếu. */
    public String xacNhanThanhToanChuyenKhoan(TaiKhoan taiKhoan, String maPhieu, long soTienNhan,
            String maGiaoDich) {
        return ghiThanhToan(taiKhoan, maPhieu, soTienNhan, HinhThucThanhToan.CHUYEN_KHOAN,
                maGiaoDich);
    }

    /** Chặn thao tác nếu phiên hết hiệu lực hoặc người dùng không phải Thu ngân. */
    private static void kiemTraThuNgan(TaiKhoan taiKhoan) {
        if (taiKhoan == null) {
            throw new SecurityException("Cần phiên đăng nhập Thu ngân.");
        }
        taiKhoan.kiemTraQuyenThuNgan();
    }

    /**
     * Ghi nhận khoản thực thu, xác minh và chốt hóa đơn qua các procedure nghiệp vụ.
     * Tiền khách đưa có thể lớn hơn phải thu khi trả tiền mặt, nhưng sổ thu chỉ ghi đúng
     * số phải thu; phần chênh lệch là tiền thối lại chứ không phải doanh thu.
     */
    private String ghiThanhToan(TaiKhoan taiKhoan, String maPhieu, long soTienNhan,
            HinhThucThanhToan hinhThuc, String maGiaoDich) {
        kiemTraThuNgan(taiKhoan);
        String maLenh = batBuoc(maPhieu, "Mã phiếu");
        String nguoiThu = taiKhoan.getMaNhanVien();
        String daCo = docMaHoaDonTheoLenh(maLenh);
        if (daCo != null) {
            return daCo;
        }
        PhieuChoThu phieu = timPhieuTheoMa(taiKhoan, maLenh);
        if (phieu == null) {
            throw new IllegalStateException("Phiếu không tồn tại, chưa hoàn thành hoặc đã thanh toán.");
        }
        String thamChieu = HoaDon.kiemTraThanhToan(phieu.getTongTien(), soTienNhan, hinhThuc, maGiaoDich);
        xacNhanDoiChieu(taiKhoan, maLenh);
        String maThaoTacThu = "APP-THU-" + maLenh + "-" + hinhThuc.name();
        String maThaoTacHoaDon = "APP-HD-" + maLenh;
        try {
            long maNoiBo = ghiNhanKhoanThu(maLenh, nguoiThu, hinhThuc, phieu.getTongTien(), thamChieu,
                    maThaoTacThu);
            xacMinhKhoanThu(maNoiBo, nguoiThu,
                    hinhThuc == HinhThucThanhToan.TIEN_MAT
                            ? "Đã kiểm đếm tiền mặt tại quầy"
                            : "Đã đối chiếu mã chuyển khoản " + thamChieu);
            return lapHoaDonDaThu(maLenh, nguoiThu, maThaoTacHoaDon);
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể ghi nhận thanh toán.", ex);
        }
    }

    /** Chụp chi phí thực tế đã kiểm tra trước khi nhận tiền. */
    public void xacNhanDoiChieu(TaiKhoan taiKhoan, String maPhieu) {
        kiemTraThuNgan(taiKhoan);
        String ma = batBuoc(maPhieu, "Mã phiếu");
        String daCo = "SELECT 1 FROM dbo.DoiChieuChiPhi WHERE MaPhieu=? AND TrangThai='DA_XAC_NHAN'";
        try (PreparedStatement kiemTra = ketNoi().prepareStatement(daCo)) {
            kiemTra.setString(1, ma);
            try (ResultSet rs = kiemTra.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể kiểm tra đối chiếu chi phí.", ex);
        }
        try (PreparedStatement lenh = ketNoi().prepareStatement(
                "EXEC dbo.usp_XacNhanDoiChieu ?,?,?,?,?")) {
            lenh.setString(1, ma);
            lenh.setString(2, taiKhoan.getMaNhanVien());
            lenh.setLong(3, 0L);
            lenh.setLong(4, 0L);
            lenh.setString(5, "");
            lenh.execute();
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể xác nhận đối chiếu chi phí.", ex);
        }
    }

    private long ghiNhanKhoanThu(String maLenh, String nguoiThu, HinhThucThanhToan hinhThuc,
            long soTien, String thamChieu, String maThaoTac) throws SQLException {
        try (PreparedStatement lenh = ketNoi().prepareStatement(
                "EXEC dbo.usp_GhiNhanThanhToan ?,?,?,?,?,?")) {
            lenh.setString(1, maLenh);
            lenh.setString(2, nguoiThu);
            lenh.setString(3, hinhThuc.name());
            lenh.setLong(4, soTien);
            lenh.setString(5, thamChieu);
            lenh.setString(6, maThaoTac);
            try (ResultSet rs = lenh.executeQuery()) {
                if (!rs.next()) throw new SQLException("Procedure không trả mã giao dịch.");
                return rs.getLong("MaGiaoDichNoiBo");
            }
        }
    }

    private void xacMinhKhoanThu(long maNoiBo, String nguoiThu, String chungCu) throws SQLException {
        String trangThai;
        try (PreparedStatement lenh = ketNoi().prepareStatement(
                "SELECT TrangThai FROM dbo.GiaoDichThanhToan WHERE MaGiaoDichNoiBo=?")) {
            lenh.setLong(1, maNoiBo);
            try (ResultSet rs = lenh.executeQuery()) {
                if (!rs.next()) throw new SQLException("Không tìm thấy giao dịch vừa ghi nhận.");
                trangThai = rs.getString(1);
            }
        }
        if ("DA_XAC_MINH".equals(trangThai)) return;
        if (!"CHO_XAC_MINH".equals(trangThai))
            throw new IllegalStateException("Khoản thu đã bị từ chối hoặc ở trạng thái không hợp lệ.");
        try (PreparedStatement lenh = ketNoi().prepareStatement(
                "EXEC dbo.usp_XacMinhThanhToan ?,?,?,?")) {
            lenh.setLong(1, maNoiBo);
            lenh.setString(2, nguoiThu);
            lenh.setString(3, chungCu);
            lenh.setBoolean(4, true);
            lenh.execute();
        }
    }

    private String lapHoaDonDaThu(String maLenh, String nguoiThu, String maThaoTac) throws SQLException {
        try (PreparedStatement lenh = ketNoi().prepareStatement(
                "EXEC dbo.usp_LapHoaDonDaThu ?,?,?")) {
            lenh.setString(1, maLenh);
            lenh.setString(2, nguoiThu);
            lenh.setString(3, maThaoTac);
            try (ResultSet rs = lenh.executeQuery()) {
                if (!rs.next()) throw new SQLException("Procedure không trả mã hóa đơn.");
                return rs.getString("MaHoaDon");
            }
        }
    }

    private String docMaHoaDonTheoLenh(String maLenh) {
        try (PreparedStatement lenh = ketNoi().prepareStatement(
                "SELECT MaHoaDon FROM dbo.HoaDon WHERE MaPhieuSuaChua=?")) {
            lenh.setString(1, maLenh);
            try (ResultSet rs = lenh.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể kiểm tra hóa đơn hiện có.", ex);
        }
    }

    /** Đếm phiếu chờ thu. */
    public int demPhieuChoThu(TaiKhoan taiKhoan) {
        return layPhieuChoThu(taiKhoan).size();
    }

    /** Đếm hóa đơn đã thanh toán. */
    public int demHoaDonDaThu(TaiKhoan taiKhoan) {
        return layHoaDonDaThu(taiKhoan).size();
    }

    /** Tính tổng đã thu trong khoảng ngày và theo hình thức tùy chọn. */
    public long tinhTongDaThu(TaiKhoan taiKhoan, LocalDate tuNgay, LocalDate denNgay,
            HinhThucThanhToan hinhThuc) {
        kiemTraThuNgan(taiKhoan);
        HoaDon.locTheoKy(null, tuNgay, denNgay, hinhThuc);
        String sql = "SELECT COALESCE(SUM(TongTien), 0) FROM dbo.HoaDon "
                + "WHERE (? IS NULL OR CONVERT(date, ThoiDiemThanhToan) >= ?) "
                + "AND (? IS NULL OR CONVERT(date, ThoiDiemThanhToan) <= ?) "
                + "AND (? IS NULL OR HinhThucThanhToan = ?)";
        try (PreparedStatement lenh = ketNoi().prepareStatement(sql)) {
            datNgay(lenh, 1, 2, tuNgay);
            datNgay(lenh, 3, 4, denNgay);
            String maHinhThuc = hinhThuc == null ? null : hinhThuc.name();
            lenh.setString(5, maHinhThuc);
            lenh.setString(6, maHinhThuc);
            try (ResultSet dong = lenh.executeQuery()) {
                dong.next();
                return dong.getLong(1);
            }
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể tính tổng đã thu.", ex);
        }
    }

    /** Tính tổng theo nhãn hình thức thanh toán. */
    public long tinhTongTheoHinhThuc(TaiKhoan taiKhoan, String hinhThuc) {
        return tinhTongDaThu(taiKhoan, null, null, HinhThucThanhToan.tuTenHienThi(hinhThuc));
    }

    /** Cung cấp căn cứ thanh toán cho luồng bàn giao xe. */
    public CanCuBanGiao docCanCuBanGiao(String maLenh) {
        if (maLenh == null || maLenh.isBlank()) {
            return null;
        }
        String sql = "SELECT MaLenh, MaChungTu, PhaiThu, DaThu, NguoiXacNhan, ThoiDiem, NgoaiLe, LyDo "
                + "FROM dbo.vw_CanCuBanGiao WHERE MaLenh = ?";
        try (PreparedStatement lenh = ketNoi().prepareStatement(sql)) {
            lenh.setString(1, maLenh.trim());
            try (ResultSet dong = lenh.executeQuery()) {
                if (!dong.next()) {
                    return null;
                }
                TaiKhoan nguoiThu = new TaiKhoan(dong.getString("NguoiXacNhan"), VaiTro.THU_NGAN);
                return new CanCuBanGiao(dong.getString("MaLenh"), dong.getString("MaChungTu"),
                        dong.getLong("PhaiThu"), dong.getLong("DaThu"), nguoiThu,
                        dong.getTimestamp("ThoiDiem").toLocalDateTime(), dong.getBoolean("NgoaiLe"),
                        dong.getString("LyDo"));
            }
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể đọc căn cứ bàn giao.", ex);
        }
    }

    /** Đọc công việc hoàn tất, phát sinh được duyệt và phụ tùng thực dùng sau hoàn kho. */
    public List<ChiTietBaoGia> layChiTietDoiChieu(TaiKhoan taiKhoan, String maPhieu) {
        if (timPhieuTheoMa(taiKhoan, maPhieu) == null) {
            throw new IllegalArgumentException("Phiếu không còn chờ thu.");
        }
        String sql = "SELECT MaDichVu,MaPhuTung,TenHangMuc,Loai,DonVi,SoLuongTinhTien AS SoLuong,"
                + "DonGia,ThanhTien FROM dbo.vw_ChiPhiThucTe WHERE MaPhieu=? AND SoLuongTinhTien>0 "
                + "ORDER BY ViTriSuaChua,COALESCE(MaPhatSinh,0),TenHangMuc";
        return docChiTiet(sql, maPhieu);
    }

    /** Đọc snapshot chi tiết của hóa đơn để xem/in lại. */
    public List<ChiTietHoaDon> layChiTietHoaDon(TaiKhoan taiKhoan, String maHoaDon) {
        if (timHoaDonTheoMa(taiKhoan, maHoaDon) == null) {
            throw new IllegalArgumentException("Không tìm thấy hóa đơn.");
        }
        String sql = "SELECT MaHoaDon, ViTri, TenHangMuc, Loai, DonVi, SoLuong, DonGia, ThanhTien "
                + "FROM dbo.ChiTietHoaDon WHERE MaHoaDon = ? ORDER BY ViTri";
        return docChiTietHoaDon(sql, maHoaDon);
    }

    /** Tách tổng tiền dịch vụ và phụ tùng của phiếu đang chờ. */
    public long[] docChiPhi(TaiKhoan taiKhoan, String maPhieu) {
        kiemTraThuNgan(taiKhoan);
        String sql = "SELECT COALESCE(SUM(CASE WHEN Loai=N'Phụ tùng' THEN 0 ELSE ThanhTien END),0),"
                + "COALESCE(SUM(CASE WHEN Loai=N'Phụ tùng' THEN ThanhTien ELSE 0 END),0) "
                + "FROM dbo.vw_ChiPhiThucTe WHERE MaPhieu=?";
        try (PreparedStatement lenh = ketNoi().prepareStatement(sql)) {
            lenh.setString(1, batBuoc(maPhieu, "Mã phiếu"));
            try (ResultSet dong = lenh.executeQuery()) {
                dong.next();
                return new long[] { dong.getLong(1), dong.getLong(2) };
            }
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể đọc tổng chi phí.", ex);
        }
    }

    /** Đọc hóa đơn cho báo cáo Quản lý; không yêu cầu vai trò Thu ngân. */
    public List<HoaDon> layHoaDonBaoCao() {
        String sql = "SELECT MaHoaDon, MaPhieuSuaChua, TenKhachHang, BienSo, TongTien, "
                + "HinhThucThanhToan, ThoiDiemThanhToan, NguoiThucHien, MaGiaoDich "
                + "FROM dbo.HoaDon ORDER BY ThoiDiemThanhToan DESC, MaHoaDon DESC";
        return docDanhSachHoaDon(sql, null);
    }

    private PhieuChoThu docPhieuChoThuKhoa(Connection ketNoi, String maPhieu) throws SQLException {
        String sql = "SELECT sc.MaPhieu AS MaLenh, tn.TenKhach AS TenKhachHang, sc.BienSo, "
                + "SUM(ct.ThanhTien) AS SoTienPhaiThu, sc.ThoiDiemHoanTat "
                + "FROM dbo.PhieuSuaChua sc WITH (UPDLOCK, HOLDLOCK) "
                + "JOIN dbo.BaoGia bg ON bg.MaBaoGia = sc.MaBaoGia "
                + "JOIN dbo.NhomBaoGia nbg ON nbg.MaBaoGiaGoc = bg.MaBaoGiaGoc "
                + "JOIN dbo.BaoCaoKiemTra kt ON kt.MaBaoCao = nbg.MaBaoCao "
                + "JOIN dbo.PhieuTiepNhan tn ON tn.MaPhieu = kt.MaPhieuTiepNhan "
                + "JOIN dbo.ChiTietBaoGia ct ON ct.MaBaoGia = bg.MaBaoGia "
                + "WHERE sc.MaPhieu = ? AND sc.TrangThai = 'HOAN_THANH' AND sc.DaThanhToan = 0 "
                + "AND NOT EXISTS (SELECT 1 FROM dbo.HoaDon hd WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE hd.MaPhieuSuaChua = sc.MaPhieu) "
                + "GROUP BY sc.MaPhieu, tn.TenKhach, sc.BienSo, sc.ThoiDiemHoanTat";
        try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            lenh.setString(1, maPhieu);
            try (ResultSet dong = lenh.executeQuery()) {
                return dong.next() ? docPhieuChoThu(dong) : null;
            }
        }
    }

    private static PhieuChoThu docPhieuChoThu(ResultSet dong) throws SQLException {
        Timestamp hoanTat = dong.getTimestamp("ThoiDiemHoanTat");
        if (hoanTat == null) {
            throw new SQLException("Phiếu chờ thu thiếu thời điểm hoàn tất.");
        }
        return new PhieuChoThu(dong.getString("MaLenh"), dong.getString("BienSo"),
                dong.getString("TenKhachHang"), dong.getLong("SoTienPhaiThu"), hoanTat.toLocalDateTime());
    }

    private List<HoaDon> docDanhSachHoaDon(String sql, String thamSo) {
        List<HoaDon> ketQua = new ArrayList<>();
        try (PreparedStatement lenh = ketNoi().prepareStatement(sql)) {
            if (thamSo != null) {
                lenh.setString(1, thamSo);
            }
            try (ResultSet dong = lenh.executeQuery()) {
                while (dong.next()) {
                    ketQua.add(docHoaDon(dong));
                }
            }
            return ketQua;
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể đọc hóa đơn.", ex);
        }
    }

    private List<HoaDon> docHoaDon(PreparedStatement lenh) throws SQLException {
        List<HoaDon> ketQua = new ArrayList<>();
        try (ResultSet dong = lenh.executeQuery()) {
            while (dong.next()) {
                ketQua.add(docHoaDon(dong));
            }
        }
        return ketQua;
    }

    private static HoaDon docHoaDon(ResultSet dong) throws SQLException {
        return new HoaDon(dong.getString("MaHoaDon"), dong.getString("MaPhieuSuaChua"),
                dong.getString("TenKhachHang"), dong.getString("BienSo"), dong.getLong("TongTien"),
                HinhThucThanhToan.valueOf(dong.getString("HinhThucThanhToan")),
                dong.getTimestamp("ThoiDiemThanhToan").toLocalDateTime(), dong.getString("NguoiThucHien"),
                dong.getString("MaGiaoDich"));
    }

    private List<ChiTietBaoGia> docChiTiet(String sql, String ma) {
        List<ChiTietBaoGia> ketQua = new ArrayList<>();
        try (PreparedStatement lenh = ketNoi().prepareStatement(sql)) {
            lenh.setString(1, ma);
            try (ResultSet dong = lenh.executeQuery()) {
                while (dong.next()) {
                    String maDichVu = dong.getString("MaDichVu");
                    String maPhuTung = dong.getString("MaPhuTung");
                    ChiTietBaoGia chiTiet = new ChiTietBaoGia(maDichVu == null ? "" : maDichVu,
                            dong.getString("TenHangMuc"), dong.getString("Loai"), dong.getString("DonVi"),
                            dong.getBigDecimal("SoLuong").doubleValue(), dong.getLong("DonGia"),
                            maPhuTung == null ? "" : maPhuTung);
                    if (chiTiet.getThanhTien() != dong.getLong("ThanhTien")) {
                        throw new SQLException("Thành tiền snapshot không khớp ở " + chiTiet.getTenHangMuc());
                    }
                    ketQua.add(chiTiet);
                }
            }
            return ketQua;
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể đọc chi tiết chứng từ.", ex);
        }
    }

    private List<ChiTietHoaDon> docChiTietHoaDon(String sql, String maHoaDon) {
        try (PreparedStatement lenh = ketNoi().prepareStatement(sql)) {
            lenh.setString(1, maHoaDon);
            return docChiTietHoaDon(lenh);
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể đọc chi tiết hóa đơn.", ex);
        }
    }

    private static List<ChiTietHoaDon> docChiTietHoaDon(Connection ketNoi, String maHoaDon) throws SQLException {
        String sql = "SELECT MaHoaDon, ViTri, TenHangMuc, Loai, DonVi, SoLuong, DonGia, ThanhTien "
                + "FROM dbo.ChiTietHoaDon WHERE MaHoaDon = ? ORDER BY ViTri";
        try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            lenh.setString(1, maHoaDon);
            return docChiTietHoaDon(lenh);
        }
    }

    private static List<ChiTietHoaDon> docChiTietHoaDon(PreparedStatement lenh) throws SQLException {
        List<ChiTietHoaDon> ketQua = new ArrayList<>();
        try (ResultSet dong = lenh.executeQuery()) {
            while (dong.next()) {
                ChiTietHoaDon chiTiet = new ChiTietHoaDon(dong.getString("MaHoaDon"), dong.getInt("ViTri"),
                        dong.getString("TenHangMuc"), dong.getString("Loai"), dong.getString("DonVi"),
                        dong.getBigDecimal("SoLuong").doubleValue(), dong.getLong("DonGia"));
                if (chiTiet.getThanhTien() != dong.getLong("ThanhTien")) {
                    throw new SQLException("Thành tiền chi tiết hóa đơn không khớp tại vị trí "
                            + chiTiet.getViTri());
                }
                ketQua.add(chiTiet);
            }
        }
        return ketQua;
    }

    private static void datNgay(PreparedStatement lenh, int chiSoNull, int chiSoGiaTri, LocalDate ngay)
            throws SQLException {
        if (ngay == null) {
            lenh.setNull(chiSoNull, Types.DATE);
            lenh.setNull(chiSoGiaTri, Types.DATE);
        } else {
            java.sql.Date giaTri = java.sql.Date.valueOf(ngay);
            lenh.setDate(chiSoNull, giaTri);
            lenh.setDate(chiSoGiaTri, giaTri);
        }
    }

    private static String batBuoc(String giaTri, String ten) {
        if (giaTri == null || giaTri.trim().isEmpty()) {
            throw new IllegalArgumentException(ten + " không được để trống.");
        }
        return giaTri.trim();
    }

    private static Connection ketNoi() {
        try {
            return ConnectDB.getInstance().getConnection();
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể kết nối database QuanLiGarage.", ex);
        }
    }

    private static void hoanTac(Connection ketNoi) {
        try {
            ketNoi.rollback();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static void batLaiTuDongLuu(Connection ketNoi) {
        try {
            ketNoi.setAutoCommit(true);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static IllegalStateException loiDuLieu(String thongBao, SQLException ex) {
        return new IllegalStateException(thongBao + " " + ex.getMessage(), ex);
    }


    /** Dữ liệu chỉ đọc của một lệnh đã hoàn thành đang chờ thu. */
    public static final class PhieuChoThu {
        private final String maPhieu;
        private final String bienSo;
        private final String tenKhach;
        private final long tongTien;
        private final LocalDateTime thoiDiemHoanTat;

        public PhieuChoThu(String maPhieu, String bienSo, String tenKhach, long tongTien,
                LocalDateTime thoiDiemHoanTat) {
            if (maPhieu == null || maPhieu.trim().isEmpty() || bienSo == null || tenKhach == null
                    || tongTien < 0 || thoiDiemHoanTat == null) {
                throw new IllegalArgumentException("Thông tin phiếu chờ thu không hợp lệ.");
            }
            this.maPhieu = maPhieu.trim();
            this.bienSo = bienSo;
            this.tenKhach = tenKhach;
            this.tongTien = tongTien;
            this.thoiDiemHoanTat = thoiDiemHoanTat;
        }

        public String getMaPhieu() {
            return maPhieu;
        }

        public String getBienSo() {
            return bienSo;
        }

        public String getTenKhach() {
            return tenKhach;
        }

        public long getTongTien() {
            return tongTien;
        }

        public LocalDateTime getThoiDiemHoanTat() {
            return thoiDiemHoanTat;
        }
    }
}
