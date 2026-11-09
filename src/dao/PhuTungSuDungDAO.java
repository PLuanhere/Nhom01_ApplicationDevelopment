package dao;

import connectDB.ConnectDB;
import entity.PhuTung;
import entity.PhieuSuaChua;
import entity.PhieuSuaChua.*;
import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// Đọc và ghi phụ tùng thực dùng bằng JDBC theo từng hạng mục được duyệt.
public final class PhuTungSuDungDAO {
    private final PhuTungDAO kho;

    // Khởi tạo DAO dùng nguồn kho SQL.
    public PhuTungSuDungDAO() {
        this(new PhuTungDAO(), false);
    }

    // Giữ constructor hiện có và không tạo chứng từ mẫu trong bộ nhớ.
    public PhuTungSuDungDAO(boolean coHoSoMau) {
        this(new PhuTungDAO(), coHoSoMau);
    }

    // Nhận nguồn kho dùng chung giữa các vai trò.
    public PhuTungSuDungDAO(PhuTungDAO kho, boolean coHoSoMau) {
        if (kho == null) {
            throw new IllegalArgumentException("Thiếu nguồn kho.");
        }
        this.kho = kho;
    }

    // Giữ hợp đồng nối nguồn; lệnh được tra cứu trực tiếp từ SQL.
    public void noiNguonLenh(PhieuSuaChuaDAO nguon) {
        if (nguon == null) {
            throw new IllegalArgumentException("Thiếu nguồn lệnh.");
        }
    }

    // Trả danh mục phụ tùng hiện tại trong SQL.
    public List<PhuTung> docDanhMuc() {
        return kho.danhMucNoiBo();
    }

    // Trả các phụ tùng đang được áp dụng.
    public List<PhuTung> docDanhMucDangHoatDong() {
        return kho.danhMucDangHoatDong();
    }

    // Trả DAO kho để các vai trò dùng chung nguồn dữ liệu.
    public PhuTungDAO getKhoDAO() {
        return kho;
    }

    // Đọc thông tin phụ tùng theo mã.
    public PhuTung docPhuTung(String ma) {
        return kho.timNoiBo(ma);
    }

    // Đọc tồn kho hiện tại từ SQL.
    public double docTon(String ma) {
        return kho.tonNoiBo(ma);
    }

    // Lưu chứng từ thực dùng, trừ tồn và ghi lịch sử trong cùng một giao dịch.
    public PhuTungSuDung ghiTheoHangMuc(TaiKhoan tk, String maLenh, int viTri, String maPT, double so, String luot) {
        kiemTraQuyen(tk, VaiTro.NHAN_VIEN_KY_THUAT);
        if (luot == null || luot.isBlank() || luot.length() > 100) {
            throw new IllegalArgumentException("Mã thao tác phải có từ 1 đến 100 ký tự.");
        }
        Connection c = ketNoi();
        try {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement("SELECT * FROM dbo.PhuTungSuDung "
                    + "WITH (UPDLOCK,HOLDLOCK) WHERE MaThaoTac=?")) {
                ps.setString(1, luot);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        if (!rs.getString("MaLenhSuaChua").equals(maLenh) || rs.getInt("ViTriHangMuc") != viTri
                                || !rs.getString("MaPhuTung").equals(maPT) || rs.getDouble("SoLuong") != so
                                || !rs.getString("NguoiThucHien").equals(tk.getMaNhanVien())
                                || !"DA_GHI_NHAN".equals(rs.getString("TrangThai"))) {
                            throw new IllegalStateException("Mã thao tác đã được sử dụng cho nội dung khác.");
                        }
                        PhieuSuaChua lenhCu = new PhieuSuaChuaDAO().timTheoMa(tk, maLenh);
                        if (lenhCu == null) {
                            throw new SecurityException("Lệnh không còn thuộc phân công.");
                        }
                        PhuTungSuDung daGhi = docHoSo(c, rs);
                        c.commit();
                        return daGhi;
                    }
                }
            }
            PhieuSuaChua p = new PhieuSuaChuaDAO().khoaLenh(c, tk, maLenh);
            if (viTri < 0 || viTri >= p.getDanhSachHangMuc().size()) {
                throw new IllegalArgumentException("Chưa chọn hạng mục hợp lệ.");
            }
            ChiTietSuaChua ct = p.getDanhSachHangMuc().get(viTri);
            if (!p.getKyThuatChinh().equals(tk.getMaNhanVien())
                    && !ct.getTaiKhoanThucHien().equals(tk.getMaNhanVien())) {
                throw new SecurityException("Hạng mục không thuộc phân công của bạn.");
            }
            if (ct.isPhatSinh()) PhatSinhDAO.kiemTraHangMuc(c, maLenh, viTri);
            entity.BaoGia.ChiTietBaoGia dongDuyet = PhatSinhDAO.docDongDuocDuyet(c, maLenh, viTri);
            if (dongDuyet == null || !dongDuyet.getMaPhuTung().equals(maPT)) {
                throw new IllegalArgumentException("Phụ tùng không thuộc hạng mục đã được khách duyệt.");
            }
            BigDecimal duyet = BigDecimal.valueOf(dongDuyet.getSoLuong());
            String sql;
            PhuTung pt = kho.docTheoMa(c, maPT, true);
            if (!pt.isHoatDong()) {
                throw new IllegalStateException("Phụ tùng đã ngừng áp dụng.");
            }
            pt.kiemTraSoLuong(so);
            BigDecimal daDung;
            sql = "SELECT COALESCE(SUM(x.ConLai),0) AS Tong FROM (SELECT s.SoLuong-"
                    + HoanPhuTungDAO.sqlDaHoan("s") + " AS ConLai FROM dbo.PhuTungSuDung s "
                    + "WHERE MaLenhSuaChua=? AND ViTriHangMuc=? AND MaPhuTung=? AND TrangThai='DA_GHI_NHAN') x";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, maLenh);
                ps.setInt(2, viTri);
                ps.setString(3, maPT);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    daDung = rs.getBigDecimal("Tong");
                }
            }
            if (daDung.add(BigDecimal.valueOf(so)).compareTo(duyet) > 0) {
                throw new IllegalArgumentException("Lượng thực dùng vượt phần còn được duyệt của hạng mục.");
            }
            String ma = taoMa(c);
            sql = "INSERT dbo.PhuTungSuDung (MaGhiNhan,MaLenhSuaChua,ViTriHangMuc,MaPhuTung,SoLuong,"
                    + "NguoiThucHien,GhiChu,TrangThai,MaThaoTac) VALUES (?,?,?,?,?,?,'','DA_GHI_NHAN',?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, ma);
                ps.setString(2, maLenh);
                ps.setInt(3, viTri);
                ps.setString(4, maPT);
                ps.setBigDecimal(5, BigDecimal.valueOf(so));
                ps.setString(6, tk.getMaNhanVien());
                ps.setString(7, luot);
                ps.executeUpdate();
            }
            kho.ghiBienDong(c, new String[] {maPT}, new double[] {so}, false, ma, tk,
                    "Thực dùng cho " + maLenh + ", hạng mục " + (viTri + 1),
                    LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
            PhuTungSuDung moi = new PhuTungSuDung(ma, maLenh, kho.docTheoMa(c, maPT, false), so,
                    tk.getMaNhanVien(), "", TrangThaiSuDung.DA_GHI_NHAN, maLenh + "#" + viTri);
            c.commit();
            return moi;
        } catch (SQLException | RuntimeException ex) {
            hoanTac(c);
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new IllegalStateException("Không ghi được phụ tùng thực dùng.", ex);
        } finally {
            batTuDongLuu(c);
        }
    }

    // Giữ cách gọi cũ nhưng phải xác định duy nhất vị trí hạng mục để ghi SQL.
    public PhuTungSuDung ghiTheoHangMuc(TaiKhoan tk, String maLenh, String hangMuc,
            String maPT, double so, double gioiHan, String luot) {
        kiemTraQuyen(tk, VaiTro.NHAN_VIEN_KY_THUAT);
        PhieuSuaChua p = new PhieuSuaChuaDAO().timTheoMa(tk, maLenh);
        if (p == null) {
            throw new SecurityException("Lệnh không thuộc phân công.");
        }
        int viTri = -1;
        for (int i = 0; i < p.getDanhSachHangMuc().size(); i++) {
            if (p.getDanhSachHangMuc().get(i).getTenHangMuc().equals(hangMuc)) {
                if (viTri >= 0) {
                    throw new IllegalArgumentException("Tên hạng mục bị trùng; cần chọn theo vị trí.");
                }
                viTri = i;
            }
        }
        return ghiTheoHangMuc(tk, maLenh, viTri, maPT, so, luot);
    }

    // Đọc chứng từ theo từ khóa, trạng thái và lệnh được phân công.
    public List<PhuTungSuDung> docDanhSach(TaiKhoan tk, String tuKhoa, TrangThaiSuDung tt) {
        kiemTraQuyen(tk, VaiTro.NHAN_VIEN_KY_THUAT);
        String sql = "SELECT s.* FROM dbo.PhuTungSuDung s JOIN dbo.PhieuSuaChua p "
                + "ON p.MaPhieu=s.MaLenhSuaChua WHERE p.KyThuatChinh=? OR p.KyThuatPhoiHop=? ORDER BY s.MaGhiNhan";
        String khoa = tuKhoa == null ? "" : tuKhoa.trim().toLowerCase(java.util.Locale.ROOT);
        List<PhuTungSuDung> ds = new ArrayList<>();
        Connection c = ketNoi();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, tk.getMaNhanVien());
            ps.setString(2, tk.getMaNhanVien());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal conLai = HoanPhuTungDAO.docConLai(c, rs.getString("MaGhiNhan"));
                    if (conLai.signum() == 0) {
                        continue;
                    }
                    PhuTungSuDung s = docHoSo(c, rs, conLai.doubleValue());
                    String chu = s.getMaGhiNhan() + " " + s.getMaLenhSuaChua() + " " + s.getPhuTung();
                    if (chu.toLowerCase(java.util.Locale.ROOT).contains(khoa) && (tt == null || tt == s.getTrangThai())) {
                        ds.add(s);
                    }
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không đọc được chứng từ thực dùng.", ex);
        }
        return ds;
    }

    // Cho tiếp nhận xem thực dùng của lệnh mà không có quyền xuất kho.
    public List<PhuTungSuDung> layThucDungChoTiepNhan(TaiKhoan tk, String maLenh) {
        kiemTraQuyen(tk, VaiTro.NHAN_VIEN_TIEP_NHAN);
        if (new PhieuSuaChuaDAO().timTheoMa(tk, maLenh) == null) {
            throw new IllegalArgumentException("Lệnh không tồn tại.");
        }
        List<PhuTungSuDung> ds = new ArrayList<>();
        Connection c = ketNoi();
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM dbo.PhuTungSuDung "
                + "WHERE MaLenhSuaChua=? AND TrangThai='DA_GHI_NHAN' ORDER BY MaGhiNhan")) {
            ps.setString(1, maLenh);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal conLai = HoanPhuTungDAO.docConLai(c, rs.getString("MaGhiNhan"));
                    if (conLai.signum() > 0) {
                        ds.add(docHoSo(c, rs, conLai.doubleValue()));
                    }
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không đọc được lịch sử thực dùng.", ex);
        }
        return ds;
    }

    // Đọc các mã lệnh đang thuộc phạm vi phân công của kỹ thuật viên.
    public List<String> docLenhDaDuyet(TaiKhoan tk) {
        kiemTraQuyen(tk, VaiTro.NHAN_VIEN_KY_THUAT);
        List<String> ds = new ArrayList<>();
        for (PhieuSuaChua p : new PhieuSuaChuaDAO().timKiem(tk, "", "Tất cả")) {
            ds.add(p.getMaPhieu());
        }
        return ds;
    }

    // Tổng hợp lượng phụ tùng được duyệt theo mã phụ tùng của một lệnh.
    public List<LuongPhuTung> docPhamViDuyet(TaiKhoan tk, String maLenh) {
        kiemTraQuyen(tk, VaiTro.NHAN_VIEN_KY_THUAT);
        if (new PhieuSuaChuaDAO().timTheoMa(tk, maLenh) == null) {
            throw new SecurityException("Lệnh không thuộc phân công.");
        }
        List<LuongPhuTung> ds = new ArrayList<>();
        String sql = "SELECT MaPhuTung,SUM(SoLuong) AS Tong FROM ("
                + "SELECT ct.MaPhuTung,ct.SoLuong FROM dbo.ChiTietBaoGia ct JOIN dbo.PhieuSuaChua p "
                + "ON p.MaBaoGia=ct.MaBaoGia WHERE p.MaPhieu=? AND ct.MaPhuTung IS NOT NULL UNION ALL "
                + "SELECT ct.MaPhuTung,ct.SoLuong FROM dbo.ChiTietDeXuatPhatSinh ct JOIN dbo.DeXuatPhatSinh d "
                + "ON d.MaPhatSinh=ct.MaPhatSinh WHERE d.MaPhieu=? AND d.TrangThai='DA_DONG_Y' "
                + "AND ct.MaPhuTung IS NOT NULL) x GROUP BY MaPhuTung";
        try (PreparedStatement ps = ketNoi().prepareStatement(sql)) {
            ps.setString(1, maLenh);
            ps.setString(2, maLenh);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(new LuongPhuTung(rs.getString("MaPhuTung"), rs.getDouble("Tong")));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không đọc được lượng phụ tùng được duyệt.", ex);
        }
        return ds;
    }

    // Tính phần còn được dùng bằng lượng duyệt trừ các chứng từ đã xác nhận.
    public double docLuongConLai(TaiKhoan tk, String maLenh, String maPT) {
        double so = layLuong(docPhamViDuyet(tk, maLenh), maPT);
        String sql = "SELECT COALESCE(SUM(x.ConLai),0) AS Tong FROM (SELECT s.SoLuong-"
                    + HoanPhuTungDAO.sqlDaHoan("s") + " AS ConLai FROM dbo.PhuTungSuDung s "
                + "WHERE MaLenhSuaChua=? AND MaPhuTung=? AND TrangThai='DA_GHI_NHAN') x";
        try (PreparedStatement ps = ketNoi().prepareStatement(sql)) {
            ps.setString(1, maLenh);
            ps.setString(2, maPT);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return Math.round((so - rs.getDouble("Tong")) * 1000) / 1000.0;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không tính được lượng còn được dùng.", ex);
        }
    }

    // Giữ giới hạn của luồng chung: ghi thực dùng trực tiếp theo vị trí hạng mục.
    public PhuTungSuDung luuNhap(TaiKhoan tk, PhuTungSuDung cu, String maLenh, String maPT, double so, String gc) {
        kiemTraQuyen(tk, VaiTro.NHAN_VIEN_KY_THUAT);
        throw new IllegalStateException("Ghi thực dùng theo từng hạng mục trong không gian kỹ thuật.");
    }

    // Xóa chứng từ nháp của đúng người tạo, không thay đổi tồn kho.
    public void xoaNhap(TaiKhoan tk, PhuTungSuDung cu) {
        kiemTraQuyen(tk, VaiTro.NHAN_VIEN_KY_THUAT);
        if (cu == null) {
            throw new IllegalArgumentException("Chưa chọn chứng từ.");
        }
        Connection c = ketNoi();
        try {
            c.setAutoCommit(false);
            new PhieuSuaChuaDAO().khoaLenh(c, tk, cu.getMaLenhSuaChua());
            try (PreparedStatement ps = c.prepareStatement("DELETE dbo.PhuTungSuDung "
                    + "WHERE MaGhiNhan=? AND MaLenhSuaChua=? AND NguoiThucHien=? AND TrangThai='NHAP'")) {
                ps.setString(1, cu.getMaGhiNhan());
                ps.setString(2, cu.getMaLenhSuaChua());
                ps.setString(3, tk.getMaNhanVien());
                if (ps.executeUpdate() != 1) {
                    throw new IllegalStateException("Chứng từ không còn là bản nháp của bạn.");
                }
            }
            c.commit();
        } catch (SQLException | RuntimeException ex) {
            hoanTac(c);
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new IllegalStateException("Không xóa được bản nháp.", ex);
        } finally {
            batTuDongLuu(c);
        }
    }

    // Giữ đường gọi cũ và yêu cầu dùng chức năng xác nhận theo từng hạng mục.
    public PhuTungSuDung xacNhanThucDung(TaiKhoan tk, PhuTungSuDung cu, List<LuongPhuTung> ds, String gc) {
        kiemTraQuyen(tk, VaiTro.NHAN_VIEN_KY_THUAT);
        throw new IllegalStateException("Xác nhận thực dùng theo từng hạng mục trong không gian kỹ thuật.");
    }

    // Chuyển đúng hạng mục của bản nháp sang trạng thái chờ phụ tùng.
    public void chuyenChoPhuTung(TaiKhoan tk, PhuTungSuDung cu) {
        kiemTraQuyen(tk, VaiTro.NHAN_VIEN_KY_THUAT);
        if (cu == null) {
            throw new IllegalArgumentException("Chưa chọn chứng từ.");
        }
        String ma;
        int viTri;
        try (PreparedStatement ps = ketNoi().prepareStatement("SELECT MaLenhSuaChua,ViTriHangMuc "
                + "FROM dbo.PhuTungSuDung WHERE MaGhiNhan=? AND NguoiThucHien=? AND TrangThai='NHAP'")) {
            ps.setString(1, cu.getMaGhiNhan());
            ps.setString(2, tk.getMaNhanVien());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Bản nháp không còn tồn tại.");
                }
                ma = rs.getString("MaLenhSuaChua");
                viTri = rs.getInt("ViTriHangMuc");
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không đọc được bản nháp.", ex);
        }
        new PhieuSuaChuaDAO().capNhatTienDo(tk, ma, viTri, TrangThaiSuaChua.CHO_PHU_TUNG, "Chờ cấp phụ tùng");
    }

    // Khôi phục chứng từ và vị trí hạng mục từ ResultSet.
    private PhuTungSuDung docHoSo(Connection c, ResultSet rs) throws SQLException {
        return docHoSo(c, rs, rs.getDouble("SoLuong"));
    }

    // Tạo dữ liệu thực dùng sau khi trừ các chứng từ hoàn đã ghi nhận.
    private PhuTungSuDung docHoSo(Connection c, ResultSet rs, double soLuong) throws SQLException {
        String maLenh = rs.getString("MaLenhSuaChua");
        return new PhuTungSuDung(rs.getString("MaGhiNhan"), maLenh,
                kho.docTheoMa(c, rs.getString("MaPhuTung"), false), soLuong,
                rs.getString("NguoiThucHien"), rs.getString("GhiChu"),
                TrangThaiSuDung.valueOf(rs.getString("TrangThai")), maLenh + "#" + rs.getInt("ViTriHangMuc"));
    }

    // Sinh mã ghi nhận bằng sequence SQL và tránh trùng dữ liệu mẫu.
    private String taoMa(Connection c) throws SQLException {
        while (true) {
            long so;
            try (PreparedStatement ps = c.prepareStatement("SELECT NEXT VALUE FOR dbo.SoMaThaoTac");
                    ResultSet rs = ps.executeQuery()) {
                rs.next();
                so = rs.getLong(1);
            }
            String ma = String.format(java.util.Locale.ROOT, "SD-%04d-%04d", LocalDateTime.now().getYear(), so);
            try (PreparedStatement ps = c.prepareStatement("SELECT MaGhiNhan FROM dbo.PhuTungSuDung WHERE MaGhiNhan=?")) {
                ps.setString(1, ma);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return ma;
                    }
                }
            }
        }
    }

    // Tìm lượng theo mã phụ tùng trong danh sách tổng hợp.
    public static double layLuong(List<LuongPhuTung> ds, String ma) {
        for (LuongPhuTung d : ds) {
            if (d.getMaPhuTung().equals(ma)) {
                return d.getSoLuong();
            }
        }
        return 0;
    }

    // Gộp lượng các dòng cùng mã và làm tròn về ba chữ số thập phân.
    public static void themLuong(List<LuongPhuTung> ds, String ma, double so) {
        for (int i = 0; i < ds.size(); i++) {
            if (ds.get(i).getMaPhuTung().equals(ma)) {
                ds.set(i, new LuongPhuTung(ma, Math.round((ds.get(i).getSoLuong() + so) * 1000) / 1000.0));
                return;
            }
        }
        ds.add(new LuongPhuTung(ma, so));
    }

    // Lấy kết nối đã cấu hình trong ConnectDB.
    private Connection ketNoi() {
        try {
            return ConnectDB.getInstance().getConnection();
        } catch (SQLException ex) {
            throw new IllegalStateException("Không kết nối được SQL Server.", ex);
        }
    }

    // Hoàn tác các thay đổi trong giao dịch đang xử lý.
    private void hoanTac(Connection c) {
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

    // Bật lại tự động lưu sau khi giao dịch kết thúc.
    private void batTuDongLuu(Connection c) {
        try {
            if (!c.isClosed()) {
                c.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không khôi phục được kết nối.", ex);
        }
    }

    // Kiểm tra vai trò và phiên tài khoản trước khi thao tác dữ liệu.
    private void kiemTraQuyen(TaiKhoan tk, VaiTro... vaiTro) {
        if (tk == null || !new TaiKhoanDAO().phienConHopLe(tk)) {
            throw new SecurityException("Cần phiên đăng nhập còn hiệu lực.");
        }
        tk.kiemTraPhien();
        for (VaiTro role : vaiTro) {
            if (tk.getVaiTro() == role) {
                return;
            }
        }
        throw new SecurityException("Bạn không có quyền thực hiện chức năng này.");
    }


    public static final class LuongPhuTung {
        private final String maPhuTung;
        private final double soLuong;

        // Tạo một dòng số lượng; quy tắc đơn vị được DAO kiểm tra với danh mục.
        public LuongPhuTung(String maPhuTung, double soLuong) {
            if (maPhuTung == null || maPhuTung.trim().isEmpty() || !(soLuong >= 0 && soLuong < Long.MAX_VALUE / 1000.0)
                    || soLuong != Math.round(soLuong * 1000) / 1000.0) {
                throw new IllegalArgumentException("Mã phụ tùng hoặc số lượng không hợp lệ.");
            }
            this.maPhuTung = maPhuTung.trim();
            this.soLuong = soLuong;
        }

        // Trả về mã phụ tùng của dòng.
        public String getMaPhuTung() {
            return maPhuTung;
        }

        // Trả về số lượng, tối đa ba số lẻ.
        public double getSoLuong() {
            return soLuong;
        }
    }

    public static final class PhuTungSuDung {
        private final String maGhiNhan;
        private final String maLenhSuaChua;
        private final PhuTung phuTung;
        private final double soLuong;
        private final String nguoiThucHien;
        private final String ghiChu;
        private final TrangThaiSuDung trangThai;
        private final String hangMuc;

        // Khởi tạo dữ liệu và thành phần cần dùng của lớp.
        public PhuTungSuDung(String maGhiNhan, String maLenhSuaChua, PhuTung phuTung, double soLuong, String nguoiThucHien,
                String ghiChu, TrangThaiSuDung trangThai) {
            this(maGhiNhan, maLenhSuaChua, phuTung, soLuong, nguoiThucHien, ghiChu, trangThai, "");
        }

        // Khởi tạo dữ liệu và thành phần cần dùng của lớp.
        public PhuTungSuDung(String maGhiNhan, String maLenhSuaChua, PhuTung phuTung, double soLuong, String nguoiThucHien,
                String ghiChu, TrangThaiSuDung trangThai, String hangMuc) {
            if (maGhiNhan == null) {
                throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
            }
            this.maGhiNhan = maGhiNhan.trim();
            if (maLenhSuaChua == null) {
                throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
            }
            this.maLenhSuaChua = maLenhSuaChua.trim();
            if (phuTung == null) {
                throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
            }
            this.phuTung = phuTung;
            phuTung.kiemTraSoLuong(soLuong);
            this.soLuong = soLuong;
            if (nguoiThucHien == null) {
                throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
            }
            this.nguoiThucHien = nguoiThucHien;
            if (ghiChu == null) {
                throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
            }
            this.ghiChu = ghiChu;
            if (trangThai == null) {
                throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
            }
            this.trangThai = trangThai;
            if (hangMuc == null) {
                throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
            }
            this.hangMuc = hangMuc;
            if (this.maGhiNhan.isEmpty() || this.maLenhSuaChua.isEmpty()) {
                throw new IllegalArgumentException("Thiếu mã ghi nhận hoặc mã lệnh.");
            }
        }

        // Trả về ma ghi nhan.
        public String getMaGhiNhan() {
            return maGhiNhan;
        }

        // Trả về ma lenh sua chua.
        public String getMaLenhSuaChua() {
            return maLenhSuaChua;
        }

        // Trả về phu tung.
        public PhuTung getPhuTung() {
            return phuTung;
        }

        // Trả về so luong.
        public double getSoLuong() {
            return soLuong;
        }

        // Trả về nguoi thuc hien.
        public String getNguoiThucHien() {
            return nguoiThucHien;
        }

        // Trả về ghi chu.
        public String getGhiChu() {
            return ghiChu;
        }

        // Trả về trang thai.
        public TrangThaiSuDung getTrangThai() {
            return trangThai;
        }

        // Trả về hang muc.
        public String getHangMuc() {
            return hangMuc;
        }
    }

    public enum TrangThaiSuDung {
        NHAP("Nháp"), DA_GHI_NHAN("Đã ghi nhận");

        private final String tenHienThi;

        // Khởi tạo dữ liệu và thành phần cần dùng của lớp.
        TrangThaiSuDung(String tenHienThi) {
            this.tenHienThi = tenHienThi;
        }

        // Trả về ten hien thi.
        public String getTenHienThi() {
            return tenHienThi;
        }
    }
}
