package dao;

import connectDB.ConnectDB;
import entity.PhieuSuaChua;
import entity.PhuTung;
import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

// Hoàn phụ tùng đã xuất bằng chứng từ bù trừ, không sửa hoặc xóa lần xuất gốc.
public final class HoanPhuTungDAO {
    // Tính tổng đã hoàn của một lần ghi nhận, chỉ tính chứng từ đã xác nhận.
    static String sqlDaHoan(String bang) {
        return "COALESCE((SELECT SUM(d.SoLuong) FROM dbo.ChiTietDieuChinhKho d "
                + "JOIN dbo.PhieuDieuChinhKho h ON h.MaPhieuDieuChinh=d.MaPhieuDieuChinh "
                + "JOIN dbo.BienDongKho b ON b.MaBienDong=d.MaBienDongGoc "
                + "WHERE h.TrangThai='DA_GHI_NHAN' AND d.Chieu='NHAP' "
                + "AND d.MaPhuTung=" + bang + ".MaPhuTung AND b.MaPhuTung=" + bang + ".MaPhuTung "
                + "AND b.ChungTu=" + bang + ".MaGhiNhan AND b.Loai=N'Xuất'),0)";
    }

    // Đọc lịch sử xuất và lượng còn có thể hoàn của lệnh được phân công.
    public List<String[]> docLanXuat(TaiKhoan tk, String maLenh) {
        kiemTra(tk);
        if (new PhieuSuaChuaDAO().timTheoMa(tk, maLenh) == null) {
            throw new SecurityException("Lệnh không thuộc phân công.");
        }
        List<String[]> ds = new ArrayList<>();
        String sql = "SELECT s.*,p.TenPhuTung,p.DonViTinh," + sqlDaHoan("s") + " AS DaHoan "
                + "FROM dbo.PhuTungSuDung s JOIN dbo.PhuTung p ON p.MaPhuTung=s.MaPhuTung "
                + "WHERE s.MaLenhSuaChua=? AND s.TrangThai='DA_GHI_NHAN' ORDER BY s.MaGhiNhan";
        try (PreparedStatement ps = ketNoi().prepareStatement(sql)) {
            ps.setString(1, maLenh);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal daHoan = rs.getBigDecimal("DaHoan");
                    BigDecimal daXuat = rs.getBigDecimal("SoLuong");
                    ds.add(new String[] {rs.getString("MaGhiNhan"),
                        String.valueOf(rs.getInt("ViTriHangMuc") + 1), rs.getString("MaPhuTung"),
                        rs.getString("TenPhuTung"), rs.getString("DonViTinh"), daXuat.toPlainString(),
                        daHoan.toPlainString(), daXuat.subtract(daHoan).toPlainString()});
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không đọc được các lần xuất phụ tùng.", ex);
        }
        return ds;
    }

    // Đọc chứng từ hoàn có liên kết tới lần xuất của lệnh hiện tại.
    public List<String[]> docLichSu(TaiKhoan tk, String maLenh) {
        kiemTra(tk);
        if (new PhieuSuaChuaDAO().timTheoMa(tk, maLenh) == null) {
            throw new SecurityException("Lệnh không thuộc phân công.");
        }
        List<String[]> ds = new ArrayList<>();
        String sql = "SELECT h.MaPhieuDieuChinh,h.ThoiDiem,h.NguoiThucHien,h.LyDo,"
                + "s.MaGhiNhan,d.MaPhuTung,d.SoLuong FROM dbo.PhieuDieuChinhKho h "
                + "JOIN dbo.ChiTietDieuChinhKho d ON d.MaPhieuDieuChinh=h.MaPhieuDieuChinh "
                + "JOIN dbo.BienDongKho b ON b.MaBienDong=d.MaBienDongGoc "
                + "JOIN dbo.PhuTungSuDung s ON s.MaGhiNhan=b.ChungTu AND s.MaPhuTung=d.MaPhuTung "
                + "WHERE s.MaLenhSuaChua=? AND h.TrangThai='DA_GHI_NHAN' AND d.Chieu='NHAP' "
                + "ORDER BY h.ThoiDiem,h.MaPhieuDieuChinh";
        try (PreparedStatement ps = ketNoi().prepareStatement(sql)) {
            ps.setString(1, maLenh);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(new String[] {rs.getString(1), rs.getTimestamp(2).toLocalDateTime().toString(),
                        rs.getString(3), rs.getString(5), rs.getString(6), rs.getBigDecimal(7).toPlainString(),
                        rs.getString(4)});
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không đọc được lịch sử hoàn phụ tùng.", ex);
        }
        return ds;
    }

    // Hoàn lượng thực tế đã trả kho, khóa lệnh và tồn rồi lưu tất cả trong một giao dịch.
    public void hoan(TaiKhoan tk, String maLenh, String maGhiNhan, double soLuong,
            String lyDo, String maPhieu) {
        kiemTra(tk);
        if (lyDo == null || lyDo.isBlank() || lyDo.trim().length() > 2000) {
            throw new IllegalArgumentException("Lý do phải có từ 1 đến 2000 ký tự.");
        }
        if (maPhieu == null || !maPhieu.matches("HK-[a-f0-9]{32}")) {
            throw new IllegalArgumentException("Mã thao tác hoàn kho không hợp lệ.");
        }
        Connection c = ketNoi();
        try {
            c.setAutoCommit(false);
            // Khóa lệnh trước để không chạy đồng thời với xuất dùng hoặc hoàn tất.
            PhieuSuaChua p = new PhieuSuaChuaDAO().khoaLenh(c, tk, maLenh);
            String maPT;
            int viTri;
            BigDecimal daXuat;
            String sql = "SELECT * FROM dbo.PhuTungSuDung WITH (UPDLOCK,HOLDLOCK) "
                    + "WHERE MaGhiNhan=? AND MaLenhSuaChua=? AND TrangThai='DA_GHI_NHAN'";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, maGhiNhan);
                ps.setString(2, maLenh);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("Không tìm thấy lần xuất đã ghi nhận của lệnh.");
                    }
                    maPT = rs.getString("MaPhuTung");
                    viTri = rs.getInt("ViTriHangMuc");
                    daXuat = rs.getBigDecimal("SoLuong");
                }
            }
            if (!p.getKyThuatChinh().equals(tk.getMaNhanVien())
                    && !p.getDanhSachHangMuc().get(viTri).getTaiKhoanThucHien().equals(tk.getMaNhanVien())) {
                throw new SecurityException("Hạng mục không thuộc phân công của bạn.");
            }
            PhuTung pt = new PhuTungDAO().docTheoMa(c, maPT, true);
            pt.kiemTraSoLuong(soLuong);
            BigDecimal so = BigDecimal.valueOf(soLuong);
            long maBienDong = docBienDongGoc(c, maGhiNhan, maPT, daXuat);
            if (daGhi(c, maPhieu, tk, maBienDong, so, lyDo.trim())) {
                c.commit();
                return;
            }
            BigDecimal conLai = docConLai(c, maGhiNhan);
            if (so.compareTo(conLai) > 0) {
                throw new IllegalArgumentException("Lượng hoàn vượt lượng còn lại của lần xuất: " + conLai);
            }
            LocalDateTime luc = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            try (PreparedStatement ps = c.prepareStatement("INSERT dbo.PhieuDieuChinhKho "
                    + "(MaPhieuDieuChinh,ThoiDiem,LyDo,NguoiThucHien,TrangThai) VALUES (?,?,?,?,'DA_GHI_NHAN')")) {
                ps.setString(1, maPhieu);
                ps.setTimestamp(2, Timestamp.valueOf(luc));
                ps.setString(3, lyDo.trim());
                ps.setString(4, tk.getMaNhanVien());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("INSERT dbo.ChiTietDieuChinhKho "
                    + "(MaPhieuDieuChinh,ViTri,MaPhuTung,SoLuong,Chieu,MaBienDongGoc) VALUES (?,0,?,?,'NHAP',?)")) {
                ps.setString(1, maPhieu);
                ps.setString(2, maPT);
                ps.setBigDecimal(3, so);
                ps.setLong(4, maBienDong);
                ps.executeUpdate();
            }
            BigDecimal truoc = BigDecimal.valueOf(pt.getSoLuongTon());
            BigDecimal sau = truoc.add(so);
            // Kiểm tra giới hạn tồn của entity trước khi ghi SQL.
            pt.setSoLuongTon(sau.doubleValue());
            try (PreparedStatement ps = c.prepareStatement("UPDATE dbo.PhuTung SET SoLuongTon=? WHERE MaPhuTung=?")) {
                ps.setBigDecimal(1, sau);
                ps.setString(2, maPT);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("INSERT dbo.BienDongKho "
                    + "(MaPhuTung,ThoiDiem,Loai,TonTruoc,TonSau,ChungTu,NguoiThucHien,LyDo) "
                    + "VALUES (?,?,N'Hoàn kho',?,?,?,?,?)")) {
                ps.setString(1, maPT);
                ps.setTimestamp(2, Timestamp.valueOf(luc));
                ps.setBigDecimal(3, truoc);
                ps.setBigDecimal(4, sau);
                ps.setString(5, maPhieu);
                ps.setString(6, tk.getMaNhanVien());
                ps.setString(7, lyDo.trim());
                ps.executeUpdate();
            }
            c.commit();
        } catch (SQLException | RuntimeException ex) {
            try {
                c.rollback();
            } catch (SQLException loi) {
                ex.addSuppressed(loi);
                try {
                    c.close();
                } catch (SQLException loiDong) {
                    ex.addSuppressed(loiDong);
                }
            }
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new IllegalStateException("Không hoàn được phụ tùng; giao dịch đã được hủy.", ex);
        } finally {
            try {
                if (!c.isClosed()) {
                    c.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                throw new IllegalStateException("Không khôi phục được kết nối.", ex);
            }
        }
    }

    // Đối chiếu duy nhất biến động xuất gốc và số lượng thực xuất trước khi hoàn.
    private long docBienDongGoc(Connection c, String ma, String maPT, BigDecimal so) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT MaBienDong,TonTruoc,TonSau FROM dbo.BienDongKho "
                + "WHERE ChungTu=? AND MaPhuTung=? AND Loai=N'Xuất'")) {
            ps.setString(1, ma);
            ps.setString(2, maPT);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Thiếu biến động xuất gốc; cần quản lý kho đối chiếu.");
                }
                long id = rs.getLong(1);
                if (rs.getBigDecimal(2).subtract(rs.getBigDecimal(3)).compareTo(so) != 0 || rs.next()) {
                    throw new IllegalStateException("Biến động xuất gốc không khớp hoặc bị trùng.");
                }
                return id;
            }
        }
    }

    // Từ chối tái sử dụng mã thao tác cho nội dung khác; gửi lại đúng nội dung không cộng kho lần hai.
    private boolean daGhi(Connection c, String ma, TaiKhoan tk, long goc, BigDecimal so, String lyDo)
            throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT h.*,d.MaBienDongGoc,d.SoLuong,d.Chieu "
                + "FROM dbo.PhieuDieuChinhKho h LEFT JOIN dbo.ChiTietDieuChinhKho d "
                + "ON d.MaPhieuDieuChinh=h.MaPhieuDieuChinh WHERE h.MaPhieuDieuChinh=?")) {
            ps.setString(1, ma);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                if (!tk.getMaNhanVien().equals(rs.getString("NguoiThucHien"))
                        || !lyDo.equals(rs.getString("LyDo")) || !"DA_GHI_NHAN".equals(rs.getString("TrangThai"))
                        || rs.getLong("MaBienDongGoc") != goc || !"NHAP".equals(rs.getString("Chieu"))
                        || rs.getBigDecimal("SoLuong") == null || rs.getBigDecimal("SoLuong").compareTo(so) != 0
                        || rs.next()) {
                    throw new IllegalStateException("Mã thao tác đã dùng cho nội dung khác.");
                }
                return true;
            }
        }
    }

    // Tính lượng thực dùng còn lại của chứng từ trên cùng kết nối giao dịch.
    static BigDecimal docConLai(Connection c, String ma) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT s.SoLuong-" + sqlDaHoan("s")
                + " AS ConLai FROM dbo.PhuTungSuDung s WHERE s.MaGhiNhan=?")) {
            ps.setString(1, ma);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Không tìm thấy lần xuất.");
                }
                BigDecimal con = rs.getBigDecimal(1);
                if (con.signum() < 0) {
                    throw new IllegalStateException("Lịch sử hoàn vượt lượng xuất; cần đối chiếu kho.");
                }
                return con;
            }
        }
    }

    // Chỉ kỹ thuật viên có phiên hợp lệ được hoàn phụ tùng thuộc phân công.
    private void kiemTra(TaiKhoan tk) {
        if (tk == null || !new TaiKhoanDAO().phienConHopLe(tk)) {
            throw new SecurityException("Cần đăng nhập lại.");
        }
        tk.kiemTraPhien();
        if (tk.getVaiTro() != VaiTro.NHAN_VIEN_KY_THUAT) {
            throw new SecurityException("Chỉ kỹ thuật viên được dùng chức năng này.");
        }
    }

    // Dùng kết nối SQL đã được nhóm cấu hình.
    private Connection ketNoi() {
        try {
            return ConnectDB.getInstance().getConnection();
        } catch (SQLException ex) {
            throw new IllegalStateException("Không kết nối được SQL Server.", ex);
        }
    }
}
