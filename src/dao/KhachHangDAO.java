package dao;

import connectDB.ConnectDB;
import entity.KhachHang;
import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Đọc và ghi hồ sơ khách hàng bằng SQL Server.
 */
public final class KhachHangDAO {
    public static KhachHangDAO taoDuLieuMau() {
        return new KhachHangDAO();
    }

    public List<KhachHang> timKiem(TaiKhoan taiKhoan, String tuKhoa) {
        kiemTraQuyen(taiKhoan);
        if (tuKhoa == null) throw new NullPointerException("Cần từ khóa.");
        String khoa = tuKhoa.trim().toLowerCase(Locale.ROOT);
        List<KhachHang> ketQua = new ArrayList<>();
        String sql = "SELECT MaKH,HoTen,SoDienThoai,DiaChi,Email,GhiChu FROM dbo.KhachHang ORDER BY MaKH";
        try (PreparedStatement ps = ketNoi().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                KhachHang kh = docKhach(rs);
                if (kh.getMaKH().toLowerCase(Locale.ROOT).contains(khoa) || kh.getHoTen().toLowerCase(Locale.ROOT).contains(khoa) || kh.getSoDienThoai().contains(khoa))
                    ketQua.add(kh);
            }
        } catch (SQLException ex) {
            throw loi("Không đọc được hồ sơ khách hàng.", ex);
        }
        return ketQua;
    }

    public KhachHang timTheoMa(TaiKhoan taiKhoan, String ma) {
        kiemTraQuyen(taiKhoan);
        String sql = "SELECT MaKH,HoTen,SoDienThoai,DiaChi,Email,GhiChu FROM dbo.KhachHang WHERE MaKH=?";
        try (PreparedStatement ps = ketNoi().prepareStatement(sql)) {
            ps.setString(1, ma);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return docKhach(rs);
            }
        } catch (SQLException ex) {
            throw loi("Không đọc được hồ sơ khách hàng.", ex);
        }
        throw new IllegalArgumentException("Hồ sơ khách hàng không tồn tại.");
    }

    public List<KhachHang> timTrungSoDienThoai(TaiKhoan taiKhoan, String so, String maLoaiTru) {
        kiemTraQuyen(taiKhoan);
        List<KhachHang> ketQua = new ArrayList<>();
        String sql = "SELECT MaKH,HoTen,SoDienThoai,DiaChi,Email,GhiChu FROM dbo.KhachHang WHERE SoDienThoai=?";
        try (PreparedStatement ps = ketNoi().prepareStatement(sql)) {
            ps.setString(1, so == null ? "" : so.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    KhachHang kh = docKhach(rs);
                    if (!kh.getMaKH().equals(maLoaiTru)) ketQua.add(kh);
                }
            }
        } catch (SQLException ex) {
            throw loi("Không kiểm tra được số điện thoại.", ex);
        }
        return ketQua;
    }

    public KhachHang luu(TaiKhoan taiKhoan, String ma, String ten, String so, String diaChi, String email, String ghiChu) {
        kiemTraQuyen(taiKhoan);
        Connection c = ketNoi();
        try {
            boolean tuDong = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                String maLuu = ma == null ? taoMaMoi(c) : ma;
                KhachHang kh = new KhachHang(maLuu, ten, so, diaChi, email, ghiChu);
                String sql = ma == null ? "INSERT dbo.KhachHang(MaKH,HoTen,SoDienThoai,DiaChi,Email,GhiChu) VALUES(?,?,?,?,?,?)" : "UPDATE dbo.KhachHang SET HoTen=?,SoDienThoai=?,DiaChi=?,Email=?,GhiChu=? WHERE MaKH=?";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    if (ma == null) {
                        ps.setString(1, kh.getMaKH());
                        datThongTin(ps, 2, kh);
                    } else {
                        datThongTin(ps, 1, kh);
                        ps.setString(6, kh.getMaKH());
                    }
                    if (ps.executeUpdate() != 1) throw new IllegalArgumentException("Hồ sơ khách hàng không tồn tại.");
                }
                c.commit();
                return kh;
            } catch (RuntimeException | SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(tuDong);
            }
        } catch (SQLException ex) {
            throw loi("Không lưu được hồ sơ khách hàng.", ex);
        }
    }

    private String taoMaMoi(Connection c) throws SQLException {
        String sql = "SELECT COALESCE(MAX(TRY_CONVERT(int,SUBSTRING(MaKH,3,20))),0)+1 AS SoThuTu FROM dbo.KhachHang WITH (UPDLOCK,HOLDLOCK) WHERE MaKH LIKE 'KH%'";
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            rs.next();
            return String.format(Locale.ROOT, "KH%04d", rs.getInt("SoThuTu"));
        }
    }

    private void datThongTin(PreparedStatement ps, int i, KhachHang kh) throws SQLException {
        ps.setString(i, kh.getHoTen());
        ps.setString(i + 1, kh.getSoDienThoai());
        ps.setString(i + 2, kh.getDiaChi());
        ps.setString(i + 3, kh.getEmail());
        ps.setString(i + 4, kh.getGhiChu());
    }

    private KhachHang docKhach(ResultSet rs) throws SQLException {
        return new KhachHang(rs.getString("MaKH"), rs.getString("HoTen"), rs.getString("SoDienThoai"), rs.getString("DiaChi"), rs.getString("Email"), rs.getString("GhiChu"));
    }

    private Connection ketNoi() {
        try {
            return ConnectDB.getInstance().getConnection();
        } catch (SQLException ex) {
            throw loi("Không kết nối được SQL Server.", ex);
        }
    }

    private IllegalStateException loi(String thongBao, SQLException ex) {
        return new IllegalStateException(thongBao, ex);
    }

    private void kiemTraQuyen(TaiKhoan tk) {
        if (tk == null) throw new NullPointerException("Cần phiên đăng nhập.");
        tk.kiemTraPhien();
        if (tk.getVaiTro() != VaiTro.NHAN_VIEN_TIEP_NHAN)
            throw new SecurityException("Chỉ nhân viên tiếp nhận được quản lý hồ sơ khách hàng.");
    }
}
