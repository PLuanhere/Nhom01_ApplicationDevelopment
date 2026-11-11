package dao;

import connectDB.ConnectDB;
import entity.KhachHang;
import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;
import entity.Xe;

import java.sql.*;
import java.util.*;

/**
 * Đọc và ghi hồ sơ xe bằng SQL Server.
 */
public final class XeDAO {
    private final KhachHangDAO khachHangDAO;

    public XeDAO(KhachHangDAO khachHangDAO) {
        if (khachHangDAO == null) throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        this.khachHangDAO = khachHangDAO;
    }

    public static XeDAO taoDuLieuMau(KhachHangDAO kh, TaiKhoan tk) {
        return new XeDAO(kh);
    }

    public List<Xe> timKiem(TaiKhoan tk, String tuKhoa) {
        kiemTraQuyen(tk);
        if (tuKhoa == null) throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        String khoa = tuKhoa.trim().toLowerCase(Locale.ROOT), bien = Xe.khoaBienSo(khoa);
        List<Xe> ds = new ArrayList<>();
        String sql = "SELECT x.* , k.HoTen FROM dbo.Xe x JOIN dbo.KhachHang k ON k.MaKH=x.MaKH ORDER BY x.MaXe";
        try (PreparedStatement ps = ketNoi().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Xe x = docXe(rs);
                String ten = rs.getString("HoTen");
                if (khoa.isEmpty() || (!bien.isEmpty() && Xe.khoaBienSo(x.getBienSo()).contains(bien)) || ten.toLowerCase(Locale.ROOT).contains(khoa) || x.getMaKH().toLowerCase(Locale.ROOT).contains(khoa))
                    ds.add(x);
            }
        } catch (SQLException ex) {
            throw loi("Không đọc được hồ sơ xe.", ex);
        }
        return ds;
    }

    public Xe timTheoMa(TaiKhoan tk, String ma) {
        kiemTraQuyen(tk);
        try (PreparedStatement ps = ketNoi().prepareStatement("SELECT * FROM dbo.Xe WHERE MaXe=?")) {
            ps.setString(1, ma);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return docXe(rs);
            }
        } catch (SQLException ex) {
            throw loi("Không đọc được hồ sơ xe.", ex);
        }
        throw new IllegalArgumentException("Hồ sơ xe không tồn tại.");
    }

    public int demTheoKhach(TaiKhoan tk, String maKH) {
        kiemTraQuyen(tk);
        khachHangDAO.timTheoMa(tk, maKH);
        try (PreparedStatement ps = ketNoi().prepareStatement("SELECT COUNT(*) FROM dbo.Xe WHERE MaKH=?")) {
            ps.setString(1, maKH);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            throw loi("Không đếm được xe của khách.", ex);
        }
    }

    public Xe luu(TaiKhoan tk, String maXe, String bienSo, String maKH, String hang, String dong, int nam, String mau, String soKhung, String soMay) {
        kiemTraQuyen(tk);
        khachHangDAO.timTheoMa(tk, maKH);
        Connection c = ketNoi();
        try {
            boolean tuDong = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                if (maXe != null) {
                    Xe cu = timTheoMa(tk, maXe);
                    if (!cu.getMaKH().equals(maKH))
                        throw new IllegalArgumentException("Không đổi chủ xe trong thao tác sửa hồ sơ thông thường.");
                }
                String ma = maXe == null ? taoMaMoi(c) : maXe;
                Xe moi = new Xe(ma, bienSo, maKH, hang, dong, nam, mau, soKhung, soMay);
                kiemTraBienSo(c, moi);
                String sql = maXe == null ? "INSERT dbo.Xe(MaXe,BienSo,MaKH,HangXe,Model,NamSX,MauXe,SoKhung,SoMay) VALUES(?,?,?,?,?,?,?,?,?)" : "UPDATE dbo.Xe SET BienSo=?,HangXe=?,Model=?,NamSX=?,MauXe=?,SoKhung=?,SoMay=? WHERE MaXe=?";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    if (maXe == null) {
                        datXe(ps, 1, moi);
                    } else {
                        ps.setString(1, moi.getBienSo());
                        ps.setString(2, moi.getHangXe());
                        ps.setString(3, moi.getModel());
                        ps.setInt(4, moi.getNamSX());
                        ps.setString(5, moi.getMauXe());
                        ps.setString(6, moi.getSoKhung());
                        ps.setString(7, moi.getSoMay());
                        ps.setString(8, moi.getMaXe());
                    }
                    if (ps.executeUpdate() != 1) throw new IllegalArgumentException("Hồ sơ xe không tồn tại.");
                }
                c.commit();
                return moi;
            } catch (RuntimeException | SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(tuDong);
            }
        } catch (SQLException ex) {
            throw loi("Không lưu được hồ sơ xe.", ex);
        }
    }

    private String taoMaMoi(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT COALESCE(MAX(TRY_CONVERT(int,REPLACE(REPLACE(MaXe,'XE-',''),'XE',''))),0)+1 FROM dbo.Xe WITH (UPDLOCK,HOLDLOCK) WHERE MaXe LIKE 'XE%'"); ResultSet rs = ps.executeQuery()) {
            rs.next();
            return String.format(Locale.ROOT, "XE%03d", rs.getInt(1));
        }
    }

    private void kiemTraBienSo(Connection c, Xe moi) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT MaXe,BienSo FROM dbo.Xe WITH (UPDLOCK,HOLDLOCK) WHERE MaXe<>?")) {
            ps.setString(1, moi.getMaXe());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) if (Xe.khoaBienSo(rs.getString("BienSo")).equals(Xe.khoaBienSo(moi.getBienSo())))
                    throw new IllegalArgumentException("Biển số đã tồn tại ở " + rs.getString("MaXe") + ". Hãy chọn hồ sơ hiện có.");
            }
        }
    }

    private void datXe(PreparedStatement ps, int i, Xe x) throws SQLException {
        ps.setString(i, x.getMaXe());
        ps.setString(i + 1, x.getBienSo());
        ps.setString(i + 2, x.getMaKH());
        ps.setString(i + 3, x.getHangXe());
        ps.setString(i + 4, x.getModel());
        ps.setInt(i + 5, x.getNamSX());
        ps.setString(i + 6, x.getMauXe());
        ps.setString(i + 7, x.getSoKhung());
        ps.setString(i + 8, x.getSoMay());
    }

    private Xe docXe(ResultSet rs) throws SQLException {
        return new Xe(rs.getString("MaXe"), rs.getString("BienSo"), rs.getString("MaKH"), rs.getString("HangXe"), rs.getString("Model"), rs.getInt("NamSX"), rs.getString("MauXe"), rs.getString("SoKhung"), rs.getString("SoMay"));
    }

    private Connection ketNoi() {
        try {
            return ConnectDB.getInstance().getConnection();
        } catch (SQLException ex) {
            throw loi("Không kết nối được SQL Server.", ex);
        }
    }

    private IllegalStateException loi(String m, SQLException e) {
        return new IllegalStateException(m, e);
    }

    private void kiemTraQuyen(TaiKhoan tk) {
        if (tk == null) throw new NullPointerException("Cần phiên đăng nhập.");
        tk.kiemTraPhien();
        if (tk.getVaiTro() != VaiTro.NHAN_VIEN_TIEP_NHAN)
            throw new SecurityException("Chỉ nhân viên tiếp nhận được quản lý hồ sơ xe.");
    }
}
