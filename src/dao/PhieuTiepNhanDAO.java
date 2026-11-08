package dao;

import connectDB.ConnectDB;
import dao.NhanVienDAO.KyThuatVienPhanCong;
import entity.*;
import entity.NhanVien.BoPhanNhanVien;
import entity.NhanVien.TrangThaiNhanVien;
import entity.PhieuTiepNhan.TrangThaiTiepNhan;
import entity.TaiKhoan.VaiTro;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Lưu phiếu tiếp nhận và phân công kiểm tra bằng SQL Server.
 */
public final class PhieuTiepNhanDAO {
    private final KhachHangDAO khachHangDAO;
    private final XeDAO xeDAO;

    public PhieuTiepNhanDAO(KhachHangDAO khachHangDAO, XeDAO xeDAO) {
        this(khachHangDAO, xeDAO, new NhanVienDAO());
    }

    public PhieuTiepNhanDAO(KhachHangDAO khachHangDAO, XeDAO xeDAO, NhanVienDAO nguonKyThuat) {
        if (khachHangDAO == null || xeDAO == null || nguonKyThuat == null)
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        this.khachHangDAO = khachHangDAO;
        this.xeDAO = xeDAO;
    }

    public PhieuTiepNhan luu(TaiKhoan tk, String maThaoTac, Xe xeDoiChieu, KhachHang khachDoiChieu, long km,
                             LocalDateTime ngayNhan, String tinhTrang, String taiSan, String yeuCau, boolean khachXacNhan) {
        kiemTraQuyen(tk);
        if (maThaoTac == null || maThaoTac.isBlank())
            throw new IllegalArgumentException("Thiếu mã thao tác tiếp nhận.");
        if (!khachXacNhan) throw new IllegalArgumentException("Cần khách xác nhận nội dung trước khi lưu.");
        if (xeDoiChieu == null || khachDoiChieu == null || ngayNhan == null)
            throw new IllegalArgumentException("Thiếu xe, khách hoặc thời gian nhận.");
        Connection c = ketNoi();
        try {
            boolean tuDong = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                PhieuTiepNhan cu = timTheoThaoTac(c, maThaoTac);
                Xe xe = xeDAO.timTheoMa(tk, xeDoiChieu.getMaXe());
                KhachHang kh = khachHangDAO.timTheoMa(tk, khachDoiChieu.getMaKH());
                if (!khopHoSo(xe, kh, xeDoiChieu, khachDoiChieu))
                    throw new IllegalArgumentException("Hồ sơ đã thay đổi hoặc không khớp. Hãy đối chiếu lại với khách.");
                PhieuTiepNhan moi = new PhieuTiepNhan(cu == null ? taoMaMoi(c, ngayNhan) : cu.getMaPhieu(), xe, kh, km, ngayNhan, tinhTrang, taiSan, yeuCau, tk.getTenDangNhap(), LocalDateTime.now());
                if (cu != null) {
                    if (!cungNoiDung(cu, moi))
                        throw new IllegalArgumentException("Mã thao tác đã được lưu với nội dung khác.");
                    c.commit();
                    return cu;
                }
                String sql = "INSERT dbo.PhieuTiepNhan(MaPhieu,MaThaoTac,MaXe,MaKH,BienSo,TenKhach,SoDienThoai,TenXe,SoKm,ThoiGianNhan,TinhTrang,TaiSan,YeuCau,NguoiTiepNhan,DaXacNhan,ThoiGianXacNhan) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    datPhieu(ps, moi, maThaoTac);
                    ps.executeUpdate();
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
            throw loi("Không lưu được phiếu tiếp nhận.", ex);
        }
    }

    public List<PhieuTiepNhan> timKiem(TaiKhoan tk, String tuKhoa, TrangThaiTiepNhan trangThai) {
        kiemTraQuyen(tk);
        if (tuKhoa == null) throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        String khoa = tuKhoa.trim().toLowerCase(Locale.ROOT), bien = Xe.khoaBienSo(khoa);
        List<PhieuTiepNhan> ds = new ArrayList<>();
        try (PreparedStatement ps = ketNoi().prepareStatement("SELECT * FROM dbo.PhieuTiepNhan ORDER BY ThoiGianNhan DESC"); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PhieuTiepNhan p = docPhieu(rs);
                String chu = (p.getMaPhieu() + " " + p.getBienSo() + " " + p.getTenKhach() + " " + p.getSoDienThoai()).toLowerCase(Locale.ROOT);
                if ((trangThai == null || p.getTrangThai() == trangThai) && (chu.contains(khoa) || (!bien.isEmpty() && Xe.khoaBienSo(p.getBienSo()).contains(bien))))
                    ds.add(p);
            }
        } catch (SQLException ex) {
            throw loi("Không đọc được danh sách phiếu.", ex);
        }
        return ds;
    }

    public PhieuTiepNhan timTheoMa(TaiKhoan tk, String ma) {
        kiemTraQuyen(tk);
        try (PreparedStatement ps = ketNoi().prepareStatement("SELECT * FROM dbo.PhieuTiepNhan WHERE MaPhieu=?")) {
            ps.setString(1, ma);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return docPhieu(rs);
            }
        } catch (SQLException ex) {
            throw loi("Không đọc được phiếu tiếp nhận.", ex);
        }
        throw new IllegalArgumentException("Phiếu tiếp nhận không tồn tại.");
    }

    public List<PhieuTiepNhan> lichSuTheoXe(TaiKhoan tk, String maXe) {
        kiemTraQuyen(tk);
        xeDAO.timTheoMa(tk, maXe);
        return docDanhSach("SELECT * FROM dbo.PhieuTiepNhan WHERE MaXe=? ORDER BY ThoiGianNhan DESC", maXe);
    }

    public List<KyThuatVienPhanCong> layKyThuatPhanCong(TaiKhoan tk) {
        kiemTraQuyen(tk);
        List<KyThuatVienPhanCong> ds = new ArrayList<>();
        String sql = "SELECT n.*,t.NgayTao,t.PhienBan FROM dbo.NhanVien n JOIN dbo.TaiKhoan t ON t.MaNhanVien=n.MaNhanVien WHERE n.BoPhan='KY_THUAT' AND n.TrangThai='DANG_LAM' AND t.HoatDong=1 AND t.VaiTro='NHAN_VIEN_KY_THUAT' ORDER BY n.MaNhanVien";
        try (PreparedStatement ps = ketNoi().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                NhanVien nv = new NhanVien(rs.getString("MaNhanVien"), rs.getString("HoTen"), rs.getDate("NgaySinh").toLocalDate(), rs.getString("SoDienThoai"), rs.getInt("SoNamKinhNghiem"), rs.getString("GioiTinh"), rs.getString("DiaChi"), BoPhanNhanVien.KY_THUAT, TrangThaiNhanVien.DANG_LAM);
                TaiKhoan kt = new TaiKhoan(rs.getString("MaNhanVien"), VaiTro.NHAN_VIEN_KY_THUAT, true, rs.getTimestamp("NgayTao").toLocalDateTime(), rs.getInt("PhienBan"));
                ds.add(new KyThuatVienPhanCong(nv, kt));
            }
        } catch (SQLException ex) {
            throw loi("Không đọc được nhân sự kỹ thuật.", ex);
        }
        return ds;
    }

    public void giaoKiemTra(TaiKhoan tk, PhieuTiepNhan daDoc, String taiKhoanKyThuat) {
        kiemTraQuyen(tk);
        if (daDoc == null) throw new IllegalArgumentException("Cần phiếu tiếp nhận đã lưu.");
        boolean hopLe = false;
        for (KyThuatVienPhanCong kt : layKyThuatPhanCong(tk))
            if (kt.getTaiKhoan().getTenDangNhap().equals(taiKhoanKyThuat)) hopLe = true;
        if (!hopLe)
            throw new IllegalArgumentException("Kỹ thuật viên không còn hoạt động hoặc không có liên kết tài khoản hợp lệ.");
        String sql = "UPDATE dbo.PhieuTiepNhan SET TaiKhoanKyThuat=?,NguoiPhanCong=?,ThoiDiemPhanCong=? WHERE MaPhieu=? AND TaiKhoanKyThuat IS NULL";
        try (PreparedStatement ps = ketNoi().prepareStatement(sql)) {
            ps.setString(1, taiKhoanKyThuat);
            ps.setString(2, tk.getMaNhanVien());
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(4, daDoc.getMaPhieu());
            if (ps.executeUpdate() == 0) {
                PhieuTiepNhan hienTai = timTheoMa(tk, daDoc.getMaPhieu());
                if (!taiKhoanKyThuat.equals(hienTai.getTaiKhoanKyThuat()))
                    throw new IllegalStateException("Phiếu đã giao kiểm tra. Không tự đổi người kiểm tra.");
            }
        } catch (SQLException ex) {
            throw loi("Không phân công được kiểm tra.", ex);
        }
    }

    public String docPhanCongKiemTra(TaiKhoan tk, String ma) {
        timTheoMa(tk, ma);
        String sql = "SELECT TaiKhoanKyThuat,NguoiPhanCong,ThoiDiemPhanCong FROM dbo.PhieuTiepNhan WHERE MaPhieu=?";
        try (PreparedStatement ps = ketNoi().prepareStatement(sql)) {
            ps.setString(1, ma);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                String kt = rs.getString(1);
                if (kt == null) return "Chưa phân công";
                Timestamp luc = rs.getTimestamp(3);
                return kt + " · " + rs.getString(2) + " · " + (luc == null ? "" : luc.toLocalDateTime());
            }
        } catch (SQLException ex) {
            throw loi("Không đọc được phân công.", ex);
        }
    }

    public List<PhieuTiepNhan> layPhieuKiemTra(TaiKhoan tk) {
        if (tk == null || tk.getVaiTro() != VaiTro.NHAN_VIEN_KY_THUAT)
            throw new SecurityException("Chỉ kỹ thuật được đọc phiếu kiểm tra đã giao.");
        tk.kiemTraPhien();
        return docDanhSach("SELECT * FROM dbo.PhieuTiepNhan WHERE TaiKhoanKyThuat=? ORDER BY ThoiGianNhan", tk.getMaNhanVien());
    }

    public List<PhieuTiepNhan> layPhieuBaoCao() {
        return docDanhSach("SELECT * FROM dbo.PhieuTiepNhan ORDER BY ThoiGianNhan", null);
    }

    private List<PhieuTiepNhan> docDanhSach(String sql, String thamSo) {
        List<PhieuTiepNhan> ds = new ArrayList<>();
        try (PreparedStatement ps = ketNoi().prepareStatement(sql)) {
            if (thamSo != null) ps.setString(1, thamSo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(docPhieu(rs));
            }
        } catch (SQLException ex) {
            throw loi("Không đọc được danh sách phiếu.", ex);
        }
        return ds;
    }

    private PhieuTiepNhan timTheoThaoTac(Connection c, String ma) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM dbo.PhieuTiepNhan WITH (UPDLOCK,HOLDLOCK) WHERE MaThaoTac=?")) {
            ps.setString(1, ma);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? docPhieu(rs) : null;
            }
        }
    }

    private String taoMaMoi(Connection c, LocalDateTime luc) throws SQLException {
        String prefix = "TN-" + luc.getYear() + "-";
        try (PreparedStatement ps = c.prepareStatement("SELECT COALESCE(MAX(TRY_CONVERT(int,RIGHT(MaPhieu,4))),0)+1 FROM dbo.PhieuTiepNhan WITH (UPDLOCK,HOLDLOCK) WHERE MaPhieu LIKE ?")) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return prefix + String.format(Locale.ROOT, "%04d", rs.getInt(1));
            }
        }
    }

    private void datPhieu(PreparedStatement ps, PhieuTiepNhan p, String luot) throws SQLException {
        ps.setString(1, p.getMaPhieu());
        ps.setString(2, luot);
        ps.setString(3, p.getMaXe());
        ps.setString(4, p.getMaKH());
        ps.setString(5, p.getBienSo());
        ps.setString(6, p.getTenKhach());
        ps.setString(7, p.getSoDienThoai());
        ps.setString(8, p.getTenXe());
        ps.setLong(9, p.getSoKm());
        ps.setTimestamp(10, Timestamp.valueOf(p.getThoiGianNhan()));
        ps.setString(11, p.getTinhTrang());
        ps.setString(12, p.getTaiSan());
        ps.setString(13, p.getYeuCau());
        ps.setString(14, p.getNguoiTiepNhan());
        ps.setBoolean(15, true);
        ps.setTimestamp(16, Timestamp.valueOf(p.getThoiGianXacNhan()));
    }

    private PhieuTiepNhan docPhieu(ResultSet rs) throws SQLException {
        Timestamp nhan = rs.getTimestamp("ThoiGianNhan"), xacNhan = rs.getTimestamp("ThoiGianXacNhan");
        return new PhieuTiepNhan(rs.getString("MaPhieu"), rs.getString("BienSo"), rs.getString("YeuCau"), rs.getString("TinhTrang"), rs.getString("TaiKhoanKyThuat"), rs.getBoolean("DaXacNhan"), rs.getString("MaXe"), rs.getString("MaKH"), rs.getString("TenKhach"), rs.getString("SoDienThoai"), rs.getString("TenXe"), rs.getString("TaiSan"), rs.getString("NguoiTiepNhan"), rs.getLong("SoKm"), nhan == null ? null : nhan.toLocalDateTime(), xacNhan == null ? null : xacNhan.toLocalDateTime());
    }

    private boolean khopHoSo(Xe xe, KhachHang kh, Xe doiChieu, KhachHang khDoiChieu) {
        return xe.getMaKH().equals(kh.getMaKH()) && xe.getBienSo().equals(doiChieu.getBienSo()) && xe.getHangXe().equals(doiChieu.getHangXe()) && xe.getModel().equals(doiChieu.getModel()) && kh.getHoTen().equals(khDoiChieu.getHoTen()) && kh.getSoDienThoai().equals(khDoiChieu.getSoDienThoai());
    }

    private boolean cungNoiDung(PhieuTiepNhan a, PhieuTiepNhan b) {
        return a.getMaXe().equals(b.getMaXe()) && a.getMaKH().equals(b.getMaKH()) && a.getSoKm() == b.getSoKm() && a.getThoiGianNhan().equals(b.getThoiGianNhan()) && a.getTinhTrang().equals(b.getTinhTrang()) && a.getTaiSan().equals(b.getTaiSan()) && a.getYeuCau().equals(b.getYeuCau()) && a.getNguoiTiepNhan().equals(b.getNguoiTiepNhan());
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
            throw new SecurityException("Chỉ nhân viên tiếp nhận được truy cập kho phiếu.");
    }
}
