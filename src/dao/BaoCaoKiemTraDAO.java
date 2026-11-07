package dao;

import connectDB.ConnectDB;
import entity.PhieuTiepNhan;
import entity.PhieuTiepNhan.BaoCaoKiemTra;
import entity.PhieuTiepNhan.TrangThaiKiemTra;
import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

// Lưu và tra cứu báo cáo kiểm tra bằng JDBC.
public final class BaoCaoKiemTraDAO {
    // Khởi tạo DAO; dữ liệu được đọc từ SQL Server.
    public BaoCaoKiemTraDAO() {
    }

    // Giữ constructor mà nguồn dữ liệu chung đang gọi.
    public BaoCaoKiemTraDAO(PhieuTiepNhanDAO nguon) {
        if (nguon == null) {
            throw new IllegalArgumentException("Thiếu nguồn tiếp nhận.");
        }
    }

    // Đọc các phiếu đã xác nhận được giao cho kỹ thuật viên hiện tại.
    public List<PhieuTiepNhan> layPhieu(TaiKhoan tk) {
        kiemTraQuyen(tk, VaiTro.NHAN_VIEN_KY_THUAT);
        String sql = "SELECT * FROM dbo.PhieuTiepNhan WHERE TaiKhoanKyThuat=? "
                + "AND DaXacNhan=1 ORDER BY ThoiGianNhan,MaPhieu";
        List<PhieuTiepNhan> ds = new ArrayList<>();
        try {
            Connection c = ConnectDB.getInstance().getConnection();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, tk.getMaNhanVien());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ds.add(docPhieu(rs));
                    }
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không đọc được phiếu kiểm tra.", ex);
        }
        return ds;
    }

    // Lọc báo cáo theo phân công, mã, biển số và trạng thái.
    public List<BaoCaoKiemTra> timKiem(TaiKhoan tk, String tuKhoa, String trangThai) {
        kiemTraQuyen(tk, VaiTro.NHAN_VIEN_KY_THUAT);
        String sql = "SELECT b.* FROM dbo.BaoCaoKiemTra b JOIN dbo.PhieuTiepNhan p "
                + "ON p.MaPhieu=b.MaPhieuTiepNhan WHERE p.TaiKhoanKyThuat=? "
                + "ORDER BY b.NgayKiemTra DESC,b.MaBaoCao";
        List<BaoCaoKiemTra> ds = new ArrayList<>();
        String khoa = tuKhoa == null ? "" : tuKhoa.trim().toLowerCase(java.util.Locale.ROOT);
        try {
            Connection c = ConnectDB.getInstance().getConnection();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, tk.getMaNhanVien());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        BaoCaoKiemTra b = docBaoCao(c, rs);
                        String chu = b.getMaBaoCao() + " " + b.getPhieu().getMaPhieu() + " " + b.getPhieu().getBienSo();
                        if (chu.toLowerCase(java.util.Locale.ROOT).contains(khoa)
                                && (trangThai == null || "Tất cả".equals(trangThai)
                                || b.getTrangThai().getTenHienThi().equals(trangThai))) {
                            ds.add(b);
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không tìm được báo cáo kiểm tra.", ex);
        }
        return ds;
    }

    // Tìm báo cáo hiện có của phiếu thuộc phân công kỹ thuật.
    public BaoCaoKiemTra timTheoPhieu(TaiKhoan tk, PhieuTiepNhan p) {
        kiemTraQuyen(tk, VaiTro.NHAN_VIEN_KY_THUAT);
        if (p == null) {
            throw new IllegalArgumentException("Chưa chọn phiếu.");
        }
        try {
            Connection c = ConnectDB.getInstance().getConnection();
            kiemTraPhieu(c, tk, p.getMaPhieu(), false);
            return timBaoCao(c, p.getMaPhieu());
        } catch (SQLException ex) {
            throw new IllegalStateException("Không đọc được báo cáo của phiếu.", ex);
        }
    }

    // Cho tiếp nhận đọc các báo cáo để theo dõi tiến độ kiểm tra.
    public List<BaoCaoKiemTra> layBaoCaoChoTiepNhan(TaiKhoan tk) {
        kiemTraQuyen(tk, VaiTro.NHAN_VIEN_TIEP_NHAN);
        return docDanhSach(false);
    }

    // Cho tiếp nhận đọc kết quả hoàn tất để lập báo giá.
    public List<BaoCaoKiemTra> layBaoCaoHoanTatChoBaoGia(TaiKhoan tk) {
        kiemTraQuyen(tk, VaiTro.NHAN_VIEN_TIEP_NHAN);
        return layHoanTatNoiBo();
    }

    // Cung cấp các báo cáo hoàn tất cho DAO liên kết.
    public List<BaoCaoKiemTra> layHoanTatNoiBo() {
        return docDanhSach(true);
    }

    // Thêm hoặc cập nhật báo cáo nháp, hoàn tác khi ghi SQL bị lỗi.
    public BaoCaoKiemTra luu(TaiKhoan tk, BaoCaoKiemTra ban, boolean hoanTat) {
        kiemTraQuyen(tk, VaiTro.NHAN_VIEN_KY_THUAT);
        if (ban == null) {
            throw new IllegalArgumentException("Thiếu báo cáo.");
        }
        Connection c = null;
        try {
            c = ConnectDB.getInstance().getConnection();
            c.setAutoCommit(false);
            PhieuTiepNhan p = kiemTraPhieu(c, tk, ban.getPhieu().getMaPhieu(), true);
            LocalDate homNay = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            if (ban.getNgayKiemTra().isBefore(p.getThoiGianNhan().toLocalDate())
                    || ban.getNgayKiemTra().isAfter(homNay)) {
                throw new IllegalArgumentException("Ngày kiểm tra phải từ ngày tiếp nhận đến hôm nay.");
            }
            String[] nd = ban.getNoiDung(), dx = ban.getDeXuatTheoVung();
            if (ban.getBoPhan().isBlank() || nd[0].isBlank() || nd[2].isBlank()) {
                throw new IllegalArgumentException("Cần bộ phận, kết quả và đề xuất kiểm tra.");
            }
            if (hoanTat) {
                for (int i = 0; i < 3; i++) {
                    if (nd[i + 3].isBlank() || dx[i].isBlank()) {
                        throw new IllegalArgumentException("Cần đủ kết quả và đề xuất của ba khu vực.");
                    }
                }
            }
            BaoCaoKiemTra cu = timBaoCao(c, p.getMaPhieu());
            if (cu != null && (!cu.getMaBaoCao().equals(ban.getMaBaoCao())
                    || cu.getTrangThai() != TrangThaiKiemTra.NHAP)) {
                throw new IllegalStateException("Phiếu đã có báo cáo hoặc báo cáo đã hoàn tất.");
            }
            if (cu == null && !ban.getMaBaoCao().isEmpty()) {
                throw new IllegalStateException("Báo cáo đã bị xóa. Hãy tải lại.");
            }
            String ma = cu == null ? taoMa(c) : cu.getMaBaoCao();
            TrangThaiKiemTra tt = hoanTat ? TrangThaiKiemTra.HOAN_TAT : TrangThaiKiemTra.NHAP;
            LocalDateTime luc = hoanTat ? LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")) : null;
            String sql;
            if (cu == null) {
                sql = "INSERT dbo.BaoCaoKiemTra (NgayKiemTra,BoPhan,NoiDung0,NoiDung1,NoiDung2,"
                        + "NoiDung3,NoiDung4,NoiDung5,NoiDung6,DeXuat0,DeXuat1,DeXuat2,TrangThai,"
                        + "ThoiDiemHoanTat,MaBaoCao,MaPhieuTiepNhan) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            } else {
                sql = "UPDATE dbo.BaoCaoKiemTra SET NgayKiemTra=?,BoPhan=?,NoiDung0=?,NoiDung1=?,"
                        + "NoiDung2=?,NoiDung3=?,NoiDung4=?,NoiDung5=?,NoiDung6=?,DeXuat0=?,DeXuat1=?,"
                        + "DeXuat2=?,TrangThai=?,ThoiDiemHoanTat=? WHERE MaBaoCao=? AND TrangThai='NHAP'";
            }
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setDate(1, java.sql.Date.valueOf(ban.getNgayKiemTra()));
                ps.setString(2, ban.getBoPhan().trim());
                for (int i = 0; i < nd.length; i++) {
                    ps.setString(i + 3, nd[i].trim());
                }
                for (int i = 0; i < dx.length; i++) {
                    ps.setString(i + 10, dx[i].trim());
                }
                ps.setString(13, tt.name());
                ps.setTimestamp(14, luc == null ? null : Timestamp.valueOf(luc));
                ps.setString(15, ma);
                if (cu == null) {
                    ps.setString(16, p.getMaPhieu());
                }
                if (ps.executeUpdate() != 1) {
                    throw new IllegalStateException("Báo cáo đã thay đổi. Hãy tải lại.");
                }
            }
            c.commit();
            return new BaoCaoKiemTra(ma, p, ban.getNgayKiemTra(), ban.getBoPhan().trim(), nd, tt, luc, dx);
        } catch (SQLException | RuntimeException ex) {
            hoanTac(c);
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new IllegalStateException("Không lưu được báo cáo kiểm tra.", ex);
        } finally {
            batTuDongLuu(c);
        }
    }

    // Chỉ xóa báo cáo nháp thuộc phiếu đang được giao cho tài khoản hiện tại.
    public void xoa(TaiKhoan tk, BaoCaoKiemTra b) {
        kiemTraQuyen(tk, VaiTro.NHAN_VIEN_KY_THUAT);
        if (b == null) {
            throw new IllegalArgumentException("Chưa chọn báo cáo.");
        }
        Connection c = null;
        try {
            c = ConnectDB.getInstance().getConnection();
            c.setAutoCommit(false);
            kiemTraPhieu(c, tk, b.getPhieu().getMaPhieu(), true);
            try (PreparedStatement ps = c.prepareStatement("DELETE dbo.BaoCaoKiemTra "
                    + "WHERE MaBaoCao=? AND MaPhieuTiepNhan=? AND TrangThai='NHAP'")) {
                ps.setString(1, b.getMaBaoCao());
                ps.setString(2, b.getPhieu().getMaPhieu());
                if (ps.executeUpdate() != 1) {
                    throw new IllegalStateException("Báo cáo đã hoàn tất hoặc không còn tồn tại.");
                }
            }
            c.commit();
        } catch (SQLException | RuntimeException ex) {
            hoanTac(c);
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new IllegalStateException("Không xóa được báo cáo.", ex);
        } finally {
            batTuDongLuu(c);
        }
    }

    // Đọc danh sách báo cáo, tùy chọn chỉ lấy báo cáo hoàn tất.
    private List<BaoCaoKiemTra> docDanhSach(boolean hoanTat) {
        String sql = "SELECT * FROM dbo.BaoCaoKiemTra "
                + (hoanTat ? "WHERE TrangThai='HOAN_TAT' " : "") + "ORDER BY MaBaoCao";
        List<BaoCaoKiemTra> ds = new ArrayList<>();
        try {
            Connection c = ConnectDB.getInstance().getConnection();
            try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(docBaoCao(c, rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không đọc được danh sách báo cáo.", ex);
        }
        return ds;
    }

    // Kiểm tra phiếu đã xác nhận và đúng phân công trong SQL.
    private PhieuTiepNhan kiemTraPhieu(Connection c, TaiKhoan tk, String ma, boolean khoa) throws SQLException {
        String sql = "SELECT * FROM dbo.PhieuTiepNhan " + (khoa ? "WITH (UPDLOCK,HOLDLOCK) " : "")
                + "WHERE MaPhieu=? AND TaiKhoanKyThuat=? AND DaXacNhan=1";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ma);
            ps.setString(2, tk.getMaNhanVien());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return docPhieu(rs);
                }
            }
        }
        throw new SecurityException("Phiếu chưa hợp lệ hoặc không thuộc phân công.");
    }

    // Tìm báo cáo theo mã phiếu trên cùng kết nối JDBC.
    private BaoCaoKiemTra timBaoCao(Connection c, String maPhieu) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM dbo.BaoCaoKiemTra WHERE MaPhieuTiepNhan=?")) {
            ps.setString(1, maPhieu);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? docBaoCao(c, rs) : null;
            }
        }
    }

    // Khôi phục báo cáo kiểm tra từ ResultSet, dùng chung khi đọc báo giá.
    static BaoCaoKiemTra docBaoCao(Connection c, ResultSet rs) throws SQLException {
        String ma = rs.getString("MaBaoCao");
        String[] nd = new String[7], dx = new String[3];
        for (int i = 0; i < nd.length; i++) {
            nd[i] = rs.getString("NoiDung" + i);
        }
        for (int i = 0; i < dx.length; i++) {
            dx[i] = rs.getString("DeXuat" + i);
        }
        Timestamp t = rs.getTimestamp("ThoiDiemHoanTat");
        PhieuTiepNhan p;
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM dbo.PhieuTiepNhan WHERE MaPhieu=?")) {
            ps.setString(1, rs.getString("MaPhieuTiepNhan"));
            try (ResultSet phieu = ps.executeQuery()) {
                if (!phieu.next()) {
                    throw new IllegalStateException("Báo cáo thiếu phiếu tiếp nhận.");
                }
                p = docPhieu(phieu);
            }
        }
        return new BaoCaoKiemTra(ma, p, rs.getDate("NgayKiemTra").toLocalDate(), rs.getString("BoPhan"),
                nd, TrangThaiKiemTra.valueOf(rs.getString("TrangThai")), t == null ? null : t.toLocalDateTime(), dx);
    }

    // Khôi phục snapshot phiếu tiếp nhận từ ResultSet.
    static PhieuTiepNhan docPhieu(ResultSet rs) throws SQLException {
        String kyThuat = rs.getString("TaiKhoanKyThuat");
        return new PhieuTiepNhan(rs.getString("MaPhieu"), rs.getString("BienSo"), rs.getString("YeuCau"),
                rs.getString("TinhTrang"), kyThuat == null ? "" : kyThuat, rs.getBoolean("DaXacNhan"),
                rs.getString("MaXe"), rs.getString("MaKH"), rs.getString("TenKhach"), rs.getString("SoDienThoai"),
                rs.getString("TenXe"), rs.getString("TaiSan"), rs.getString("NguoiTiepNhan"), rs.getLong("SoKm"),
                rs.getTimestamp("ThoiGianNhan").toLocalDateTime(), rs.getTimestamp("ThoiGianXacNhan").toLocalDateTime());
    }

    // Sinh mã SQL bằng sequence và bỏ qua mã đã có trong dữ liệu mẫu.
    private String taoMa(Connection c) throws SQLException {
        while (true) {
            long so;
            try (PreparedStatement ps = c.prepareStatement("SELECT NEXT VALUE FOR dbo.SoMaThaoTac");
                    ResultSet rs = ps.executeQuery()) {
                rs.next();
                so = rs.getLong(1);
            }
            String ma = String.format(java.util.Locale.ROOT, "KT-%04d-%04d", LocalDate.now().getYear(), so);
            try (PreparedStatement ps = c.prepareStatement("SELECT MaBaoCao FROM dbo.BaoCaoKiemTra WHERE MaBaoCao=?")) {
                ps.setString(1, ma);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return ma;
                    }
                }
            }
        }
    }

    // Kiểm tra vai trò và phiên tài khoản bằng DAO đăng nhập hiện có.
    private void kiemTraQuyen(TaiKhoan tk, VaiTro vaiTro) {
        if (tk == null || tk.getVaiTro() != vaiTro || !new TaiKhoanDAO().phienConHopLe(tk)) {
            throw new SecurityException("Tài khoản không có quyền hoặc phiên đã hết hiệu lực.");
        }
        tk.kiemTraPhien();
    }

    // Hoàn tác giao dịch khi dữ liệu không hợp lệ hoặc SQL gặp lỗi.
    private void hoanTac(Connection c) {
        if (c != null) {
            try {
                c.rollback();
            } catch (SQLException ex) {
                try {
                    c.close();
                } catch (SQLException loi) {
                    ex.addSuppressed(loi);
                }
                throw new IllegalStateException("Không hoàn tác được giao dịch.", ex);
            }
        }
    }

    // Bật lại tự động lưu sau khi giao dịch đã kết thúc.
    private void batTuDongLuu(Connection c) {
        if (c != null) {
            try {
                if (!c.isClosed()) {
                    c.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                throw new IllegalStateException("Không khôi phục được kết nối.", ex);
            }
        }
    }
}
