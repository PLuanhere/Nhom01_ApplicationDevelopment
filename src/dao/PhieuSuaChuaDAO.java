package dao;

import connectDB.ConnectDB;
import entity.*;
import entity.BaoGia.*;
import entity.NhanVien.*;
import entity.PhieuSuaChua.*;
import entity.TaiKhoan.VaiTro;
import dao.NhanVienDAO.KyThuatVienPhanCong;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

// Đọc và ghi lệnh sửa chữa bằng JDBC trong DAO hiện có.
public final class PhieuSuaChuaDAO {
    // Khởi tạo DAO sử dụng SQL Server.
    public PhieuSuaChuaDAO() {
    }

    // Giữ constructor của nguồn dữ liệu chung.
    public PhieuSuaChuaDAO(BaoGiaDAO bg, NhanVienDAO nv) {
        if (bg == null || nv == null) {
            throw new IllegalArgumentException("Thiếu nguồn báo giá hoặc nhân viên.");
        }
    }

    // Giữ chỗ nối nguồn; phụ tùng thực dùng được đối chiếu trực tiếp trong SQL.
    public void noiNguonThucDung(PhuTungSuDungDAO nguon) {
        if (nguon == null) {
            throw new IllegalArgumentException("Thiếu nguồn phụ tùng.");
        }
    }

    // Đọc các lệnh đúng phân công rồi lọc từ khóa và trạng thái.
    public List<PhieuSuaChua> timKiem(TaiKhoan tk, String tuKhoa, String trangThai) {
        kiemTraQuyen(tk, VaiTro.NHAN_VIEN_TIEP_NHAN, VaiTro.NHAN_VIEN_KY_THUAT);
        String sql = "SELECT * FROM dbo.PhieuSuaChua";
        boolean kyThuat = tk.getVaiTro() == VaiTro.NHAN_VIEN_KY_THUAT;
        if (kyThuat) {
            sql += " WHERE KyThuatChinh=? OR KyThuatPhoiHop=?";
        }
        sql += " ORDER BY MaPhieu";
        List<PhieuSuaChua> ds = new ArrayList<>();
        String khoa = tuKhoa == null ? "" : tuKhoa.trim().toLowerCase(Locale.ROOT);
        Connection c = ketNoi();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            if (kyThuat) {
                ps.setString(1, tk.getMaNhanVien());
                ps.setString(2, tk.getMaNhanVien());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PhieuSuaChua p = docPhieu(c, rs);
                    String chu = p.getMaPhieu() + " " + p.getMaBaoGia() + " " + p.getBienSo();
                    String bien = Xe.khoaBienSo(khoa);
                    boolean khop = chu.toLowerCase(Locale.ROOT).contains(khoa)
                            || (!bien.isEmpty() && Xe.khoaBienSo(p.getBienSo()).contains(bien));
                    if (khop && (trangThai == null || "Tất cả".equals(trangThai)
                            || p.getTrangThai().getTenHienThi().equals(trangThai))) {
                        ds.add(p);
                    }
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không đọc được danh sách lệnh.", ex);
        }
        return ds;
    }

    // Tìm một lệnh và từ chối trả dữ liệu ngoài phân công kỹ thuật.
    public PhieuSuaChua timTheoMa(TaiKhoan tk, String ma) {
        kiemTraQuyen(tk, VaiTro.NHAN_VIEN_TIEP_NHAN, VaiTro.NHAN_VIEN_KY_THUAT);
        try {
            PhieuSuaChua p = docTheoMa(ketNoi(), ma, false);
            if (p != null && tk.getVaiTro() == VaiTro.NHAN_VIEN_KY_THUAT && !duocGiao(tk, p)) {
                return null;
            }
            return p;
        } catch (SQLException ex) {
            throw new IllegalStateException("Không đọc được lệnh sửa chữa.", ex);
        }
    }

    // Đọc báo giá đúng với lệnh mà tài khoản được phép xem.
    public BaoGia layBaoGia(TaiKhoan tk, String ma) {
        PhieuSuaChua p = timTheoMa(tk, ma);
        if (p == null) {
            throw new SecurityException("Lệnh không tồn tại hoặc không thuộc phân công.");
        }
        return docBaoGiaNoiBo(p.getMaBaoGia());
    }

    // Đọc báo giá khách đồng ý ở phiên bản mới nhất của mỗi nhóm.
    public List<BaoGia> layBaoGiaDaDuyet(TaiKhoan tk) {
        kiemTraQuyen(tk, VaiTro.NHAN_VIEN_TIEP_NHAN);
        String sql = "SELECT b.MaBaoGia FROM dbo.BaoGia b WHERE b.TrangThai='DA_DONG_Y' "
                + "AND NOT EXISTS (SELECT 1 FROM dbo.BaoGia x WHERE x.MaBaoGiaGoc=b.MaBaoGiaGoc "
                + "AND x.PhienBan>b.PhienBan) ORDER BY b.MaBaoGia";
        List<BaoGia> ds = new ArrayList<>();
        Connection c = ketNoi();
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(docBaoGia(c, rs.getString("MaBaoGia")));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không đọc được báo giá đã duyệt.", ex);
        }
        return ds;
    }

    // Loại các nhóm báo giá đã có lệnh để ngăn phát hành trùng.
    public List<BaoGia> layBaoGiaCoThePhatHanh(TaiKhoan tk) {
        List<BaoGia> ds = new ArrayList<>();
        Connection c = ketNoi();
        String sql = "SELECT MaPhieu FROM dbo.PhieuSuaChua WHERE MaBaoGiaGoc=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (BaoGia bg : layBaoGiaDaDuyet(tk)) {
                ps.setString(1, bg.getMaBaoGiaGoc());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        ds.add(bg);
                    }
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không đọc được nguồn phát hành lệnh.", ex);
        }
        return ds;
    }

    // Đọc nhân viên kỹ thuật đang làm và có tài khoản hoạt động từ SQL.
    public List<KyThuatVienPhanCong> layKyThuatPhanCong(TaiKhoan tk) {
        kiemTraQuyen(tk, VaiTro.NHAN_VIEN_TIEP_NHAN);
        String sql = "SELECT n.*,t.NgayTao,t.PhienBan FROM dbo.NhanVien n JOIN dbo.TaiKhoan t "
                + "ON t.MaNhanVien=n.MaNhanVien WHERE n.BoPhan='KY_THUAT' AND n.TrangThai='DANG_LAM' "
                + "AND t.HoatDong=1 AND t.VaiTro='NHAN_VIEN_KY_THUAT' ORDER BY n.MaNhanVien";
        List<KyThuatVienPhanCong> ds = new ArrayList<>();
        try (PreparedStatement ps = ketNoi().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                NhanVien nv = new NhanVien(rs.getString("MaNhanVien"), rs.getString("HoTen"),
                        rs.getDate("NgaySinh").toLocalDate(), rs.getString("SoDienThoai"),
                        rs.getInt("SoNamKinhNghiem"), rs.getString("GioiTinh"), rs.getString("DiaChi"),
                        BoPhanNhanVien.KY_THUAT, TrangThaiNhanVien.DANG_LAM);
                TaiKhoan kt = new TaiKhoan(rs.getString("MaNhanVien"), VaiTro.NHAN_VIEN_KY_THUAT,
                        true, rs.getTimestamp("NgayTao").toLocalDateTime(), rs.getInt("PhienBan"));
                ds.add(new KyThuatVienPhanCong(nv, kt));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không đọc được nhân sự kỹ thuật.", ex);
        }
        return ds;
    }

    // Phát hành một lệnh cùng chi tiết và phân công trong cùng giao dịch.
    public PhieuSuaChua phatHanh(TaiKhoan tk, String luot, BaoGia ban, String chinh, String hoTro,
            List<String> phuTrach, LocalDateTime han, String ghiChu) {
        kiemTraQuyen(tk, VaiTro.NHAN_VIEN_TIEP_NHAN);
        if (ban == null || luot == null || luot.isBlank() || luot.length() > 100) {
            throw new IllegalArgumentException("Thiếu báo giá hoặc mã thao tác không hợp lệ.");
        }
        String phu = hoTro == null ? "" : hoTro.trim();
        String gc = ghiChu == null ? "" : ghiChu.trim();
        Connection c = ketNoi();
        try {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement("SELECT * FROM dbo.PhieuSuaChua "
                    + "WITH (UPDLOCK,HOLDLOCK) WHERE MaThaoTac=?")) {
                ps.setString(1, luot);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        PhieuSuaChua cu = docPhieu(c, rs);
                        kiemTraPhatHanhLap(cu, tk, ban, chinh, phu, phuTrach, han, gc);
                        c.commit();
                        return cu;
                    }
                }
            }
            try (PreparedStatement ps = c.prepareStatement("SELECT MaBaoGia FROM dbo.BaoGia "
                    + "WITH (UPDLOCK,HOLDLOCK) WHERE MaBaoGia=?")) {
                ps.setString(1, ban.getMaBaoGia());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalStateException("Báo giá chưa được lưu trong SQL.");
                    }
                }
            }
            BaoGia bg = docBaoGia(c, ban.getMaBaoGia());
            if (!bg.isDaDongY() || !cungBaoGia(ban, bg)) {
                throw new IllegalStateException("Báo giá đã thay đổi hoặc chưa được khách đồng ý.");
            }
            try (PreparedStatement ps = c.prepareStatement("SELECT MaBaoGia FROM dbo.BaoGia "
                    + "WHERE MaBaoGiaGoc=? AND PhienBan>?")) {
                ps.setString(1, bg.getMaBaoGiaGoc());
                ps.setInt(2, bg.getPhienBan());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        throw new IllegalStateException("Báo giá đã có phiên bản mới hơn.");
                    }
                }
            }
            try (PreparedStatement ps = c.prepareStatement("SELECT MaPhieu FROM dbo.PhieuSuaChua "
                    + "WITH (UPDLOCK,HOLDLOCK) WHERE MaBaoGiaGoc=?")) {
                ps.setString(1, bg.getMaBaoGiaGoc());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        throw new IllegalStateException("Nhóm báo giá đã có lệnh sửa chữa.");
                    }
                }
            }
            LocalDateTime luc = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            if (han == null || han.isBefore(luc) || han.isBefore(bg.getThoiDiemPhanHoi())) {
                throw new IllegalArgumentException("Hạn phải sau thời điểm phát hành và khách đồng ý.");
            }
            Set<String> nhanSu = new HashSet<>();
            for (KyThuatVienPhanCong kt : layKyThuatPhanCong(tk)) {
                nhanSu.add(kt.getTaiKhoan().getMaNhanVien());
            }
            if (!nhanSu.contains(chinh) || chinh.equals(phu) || (!phu.isEmpty() && !nhanSu.contains(phu))) {
                throw new IllegalArgumentException("Kỹ thuật viên chính hoặc hỗ trợ không hợp lệ.");
            }
            if (phuTrach == null || phuTrach.size() != bg.getChiTiet().size()) {
                throw new IllegalArgumentException("Cần phân công đủ từng hạng mục.");
            }
            for (String nguoi : phuTrach) {
                if (nguoi == null || nguoi.isBlank() || (!nguoi.equals(chinh) && !nguoi.equals(phu))) {
                    throw new IllegalArgumentException("Mỗi hạng mục phải giao cho người chính hoặc hỗ trợ.");
                }
            }
            String ma = taoMa(c, "SC");
            String sql = "INSERT dbo.PhieuSuaChua (MaPhieu,MaBaoGia,MaBaoGiaGoc,BienSo,KyThuatChinh,"
                    + "KyThuatPhoiHop,HanHoanThanh,GhiChu,TrangThai,NguoiPhatHanh,ThoiDiemPhatHanh,MaThaoTac) "
                    + "VALUES (?,?,?,?,?,?,?,?,'CHO_SUA_CHUA',?,?,?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, ma);
                ps.setString(2, bg.getMaBaoGia());
                ps.setString(3, bg.getMaBaoGiaGoc());
                ps.setString(4, bg.getBienSo());
                ps.setString(5, chinh);
                ps.setString(6, phu.isEmpty() ? null : phu);
                ps.setTimestamp(7, Timestamp.valueOf(han));
                ps.setString(8, gc);
                ps.setString(9, tk.getMaNhanVien());
                ps.setTimestamp(10, Timestamp.valueOf(luc));
                ps.setString(11, luot);
                ps.executeUpdate();
            }
            sql = "INSERT dbo.ChiTietSuaChua (MaPhieu,ViTri,TenHangMuc,TaiKhoanThucHien,KetQua,TrangThai,PhatSinh) "
                    + "VALUES (?,?,?,?,'','CHO_SUA_CHUA',0)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                for (int i = 0; i < phuTrach.size(); i++) {
                    ps.setString(1, ma);
                    ps.setInt(2, i);
                    ps.setString(3, bg.getChiTiet().get(i).getTenHangMuc());
                    ps.setString(4, phuTrach.get(i));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            sql = "INSERT dbo.PhanCong (MaPC,MaPhieuSC,MaNV,NgayPhanCong,GhiChu) VALUES (?,?,?,?,?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                for (String nv : new String[] {chinh, phu}) {
                    if (!nv.isEmpty()) {
                        ps.setString(1, taoMa(c, "PC"));
                        ps.setString(2, ma);
                        ps.setString(3, nv);
                        ps.setTimestamp(4, Timestamp.valueOf(luc));
                        ps.setString(5, gc);
                        ps.executeUpdate();
                    }
                }
            }
            PhieuSuaChua moi = docTheoMa(c, ma, false);
            c.commit();
            return moi;
        } catch (SQLException | RuntimeException ex) {
            hoanTac(c);
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new IllegalStateException("Không phát hành được lệnh sửa chữa.", ex);
        } finally {
            batTuDongLuu(c);
        }
    }

    // Giữ hợp đồng cũ và yêu cầu dùng màn hình phân công từng hạng mục như luồng chung hiện có.
    public PhieuSuaChua luu(TaiKhoan tk, String ma, String bg, String chinh, String phu,
            LocalDateTime han, String gc) {
        kiemTraQuyen(tk, VaiTro.NHAN_VIEN_TIEP_NHAN);
        throw new IllegalStateException("Sử dụng màn hình phát hành lệnh và phân công từng hạng mục.");
    }

    // Giữ lịch sử lệnh đã phát hành, không xóa trực tiếp chứng từ.
    public void xoa(TaiKhoan tk, String ma) {
        kiemTraQuyen(tk, VaiTro.NHAN_VIEN_TIEP_NHAN);
        throw new IllegalStateException("Không xóa trực tiếp lệnh đã phát hành.");
    }

    // Cập nhật tiến độ đúng hạng mục được giao và tính trạng thái lệnh.
    public void capNhatTienDo(TaiKhoan tk, String ma, int viTri, TrangThaiSuaChua tt, String ketQua) {
        kiemTraQuyen(tk, VaiTro.NHAN_VIEN_KY_THUAT);
        Connection c = ketNoi();
        try {
            c.setAutoCommit(false);
            PhieuSuaChua p = khoaLenh(c, tk, ma);
            if (tt == null || ketQua == null || ketQua.isBlank()
                    || viTri < 0 || viTri >= p.getDanhSachHangMuc().size()) {
                throw new IllegalArgumentException("Cần hạng mục, trạng thái và kết quả hợp lệ.");
            }
            ChiTietSuaChua ct = p.getDanhSachHangMuc().get(viTri);
            if (!tk.getMaNhanVien().equals(p.getKyThuatChinh())
                    && !tk.getMaNhanVien().equals(ct.getTaiKhoanThucHien())) {
                throw new SecurityException("Bạn không được cập nhật công việc này.");
            }
            if (ct.isPhatSinh() && (p.getPhatSinh() == null
                    || p.getPhatSinh().getTrangThai() != TrangThaiPhatSinh.DA_DUYET)) {
                throw new IllegalStateException("Phát sinh chưa được duyệt.");
            }
            try (PreparedStatement ps = c.prepareStatement("UPDATE dbo.ChiTietSuaChua "
                    + "SET TrangThai=?,KetQua=? WHERE MaPhieu=? AND ViTri=?")) {
                ps.setString(1, tt.name());
                ps.setString(2, ketQua.trim());
                ps.setString(3, ma);
                ps.setInt(4, viTri);
                ps.executeUpdate();
            }
            String sql = "UPDATE dbo.PhieuSuaChua SET TrangThai=CASE WHEN EXISTS "
                    + "(SELECT 1 FROM dbo.ChiTietSuaChua WHERE MaPhieu=? AND TrangThai='CHO_PHU_TUNG') "
                    + "THEN 'CHO_PHU_TUNG' ELSE 'DANG_SUA' END WHERE MaPhieu=?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, ma);
                ps.setString(2, ma);
                ps.executeUpdate();
            }
            c.commit();
        } catch (SQLException | RuntimeException ex) {
            hoanTac(c);
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new IllegalStateException("Không cập nhật được tiến độ.", ex);
        } finally {
            batTuDongLuu(c);
        }
    }

    // Lưu đề xuất phát sinh để chờ phản hồi của khách, không tự phê duyệt.
    public void guiPhatSinh(TaiKhoan tk, String ma, String[] nd) {
        kiemTraQuyen(tk, VaiTro.NHAN_VIEN_KY_THUAT);
        PhatSinhSuaChua deXuat = new PhatSinhSuaChua(nd, TrangThaiPhatSinh.CHO_DUYET, "", null);
        Connection c = ketNoi();
        try {
            c.setAutoCommit(false);
            PhieuSuaChua p = khoaLenh(c, tk, ma);
            if (p.getPhatSinh() != null) {
                throw new IllegalStateException("Lệnh đã có đề xuất phát sinh.");
            }
            String sql = "INSERT dbo.PhatSinhSuaChua (MaPhieu,HuHong,NguyenNhan,DeXuat,ThoiGianThem,GhiChu,TrangThai) "
                    + "VALUES (?,?,?,?,?,?,'CHO_DUYET')";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, ma);
                for (int i = 0; i < 5; i++) {
                    ps.setString(i + 2, deXuat.getNoiDung()[i]);
                }
                ps.executeUpdate();
            }
            c.commit();
        } catch (SQLException | RuntimeException ex) {
            hoanTac(c);
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new IllegalStateException("Không gửi được phát sinh.", ex);
        } finally {
            batTuDongLuu(c);
        }
    }

    // Hoàn tất khi mọi hạng mục đã xong, đủ thực dùng và không còn phát sinh chờ duyệt.
    public void hoanTat(TaiKhoan tk, String ma, String[] kq, boolean[] dat) {
        kiemTraQuyen(tk, VaiTro.NHAN_VIEN_KY_THUAT);
        Connection c = ketNoi();
        try {
            c.setAutoCommit(false);
            PhieuSuaChua p = khoaLenh(c, tk, ma);
            if (!p.getKyThuatChinh().equals(tk.getMaNhanVien())) {
                throw new SecurityException("Chỉ kỹ thuật viên chính được hoàn tất lệnh.");
            }
            if (kq == null || kq.length != 3 || kq[0] == null || kq[0].isBlank()
                    || kq[1] == null || kq[2] == null || dat == null || dat.length != 4) {
                throw new IllegalArgumentException("Cần kết luận và đủ bốn điều kiện kiểm tra cuối.");
            }
            for (boolean b : dat) {
                if (!b) {
                    throw new IllegalArgumentException("Chưa đối chiếu đủ điều kiện kiểm tra cuối.");
                }
            }
            if (p.getPhatSinh() != null && p.getPhatSinh().getTrangThai() == TrangThaiPhatSinh.CHO_DUYET) {
                throw new IllegalStateException("Còn phát sinh chờ khách duyệt.");
            }
            for (ChiTietSuaChua ct : p.getDanhSachHangMuc()) {
                if (ct.getTrangThai() != TrangThaiSuaChua.HOAN_THANH || ct.getKetQua().isBlank()) {
                    throw new IllegalStateException("Còn công việc chưa hoàn thành.");
                }
            }
            String sql = "SELECT c.ViTri FROM dbo.ChiTietBaoGia c WHERE c.MaBaoGia=? AND c.MaPhuTung IS NOT NULL "
                    + "AND NOT EXISTS (SELECT 1 FROM dbo.PhuTungSuDung s WHERE s.MaLenhSuaChua=? "
                    + "AND s.ViTriHangMuc=c.ViTri AND s.MaPhuTung=c.MaPhuTung AND s.TrangThai='DA_GHI_NHAN')";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, p.getMaBaoGia());
                ps.setString(2, ma);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        throw new IllegalStateException("Chưa ghi nhận phụ tùng ở hạng mục " + (rs.getInt(1) + 1));
                    }
                }
            }
            sql = "UPDATE dbo.PhieuSuaChua SET TrangThai='HOAN_THANH',ThoiDiemHoanTat=?,KetQua0=?,KetQua1=?,"
                    + "KetQua2=? WHERE MaPhieu=?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))));
                for (int i = 0; i < 3; i++) {
                    ps.setString(i + 2, kq[i].trim());
                }
                ps.setString(5, ma);
                ps.executeUpdate();
            }
            c.commit();
        } catch (SQLException | RuntimeException ex) {
            hoanTac(c);
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new IllegalStateException("Không hoàn tất được lệnh.", ex);
        } finally {
            batTuDongLuu(c);
        }
    }

    // Khóa lệnh trên kết nối giao dịch để bảo vệ tiến độ và việc xuất phụ tùng.
    PhieuSuaChua khoaLenh(Connection c, TaiKhoan tk, String ma) throws SQLException {
        PhieuSuaChua p = docTheoMa(c, ma, true);
        if (p == null || !duocGiao(tk, p)) {
            throw new SecurityException("Lệnh không tồn tại hoặc không thuộc phân công.");
        }
        if (p.getTrangThai() == TrangThaiSuaChua.HOAN_THANH) {
            throw new IllegalStateException("Lệnh đã hoàn thành.");
        }
        return p;
    }

    // Đọc lệnh bằng mã trên kết nối được cấp, tùy chọn khóa để cập nhật.
    private PhieuSuaChua docTheoMa(Connection c, String ma, boolean khoa) throws SQLException {
        String sql = "SELECT * FROM dbo.PhieuSuaChua " + (khoa ? "WITH (UPDLOCK,HOLDLOCK) " : "") + "WHERE MaPhieu=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ma);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? docPhieu(c, rs) : null;
            }
        }
    }

    // Khôi phục đủ chi tiết, phát sinh và các thời điểm của lệnh từ SQL.
    private PhieuSuaChua docPhieu(Connection c, ResultSet rs) throws SQLException {
        String ma = rs.getString("MaPhieu");
        List<ChiTietSuaChua> ct = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM dbo.ChiTietSuaChua WHERE MaPhieu=? ORDER BY ViTri")) {
            ps.setString(1, ma);
            try (ResultSet dong = ps.executeQuery()) {
                while (dong.next()) {
                    if (dong.getInt("ViTri") != ct.size()) {
                        throw new IllegalStateException("Thứ tự hạng mục không liên tục.");
                    }
                    ct.add(new ChiTietSuaChua(dong.getString("TenHangMuc"), dong.getString("TaiKhoanThucHien"),
                            dong.getString("KetQua"), TrangThaiSuaChua.valueOf(dong.getString("TrangThai")),
                            dong.getBoolean("PhatSinh")));
                }
            }
        }
        PhatSinhSuaChua phatSinh = null;
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM dbo.PhatSinhSuaChua WHERE MaPhieu=?")) {
            ps.setString(1, ma);
            try (ResultSet dong = ps.executeQuery()) {
                if (dong.next()) {
                    String nguoi = dong.getString("NguoiXacNhan");
                    phatSinh = new PhatSinhSuaChua(new String[] {dong.getString("HuHong"), dong.getString("NguyenNhan"),
                            dong.getString("DeXuat"), dong.getString("ThoiGianThem"), dong.getString("GhiChu")},
                            TrangThaiPhatSinh.valueOf(dong.getString("TrangThai")), nguoi == null ? "" : nguoi,
                            docThoiGian(dong, "ThoiDiemXacNhan"));
                }
            }
        }
        String phu = rs.getString("KyThuatPhoiHop");
        return new PhieuSuaChua(ma, rs.getString("MaBaoGia"), rs.getString("BienSo"), rs.getString("KyThuatChinh"),
                phu == null ? "" : phu, docThoiGian(rs, "HanHoanThanh"), rs.getString("GhiChu"),
                TrangThaiSuaChua.valueOf(rs.getString("TrangThai")), ct, phatSinh, rs.getString("NguoiPhatHanh"),
                docThoiGian(rs, "ThoiDiemPhatHanh"), new String[] {rs.getString("KetQua0"),rs.getString("KetQua1"),
                        rs.getString("KetQua2")}, docThoiGian(rs, "ThoiDiemHoanTat"));
    }

    // Đọc báo giá liên kết cho thu ngân và các DAO sử dụng nội bộ.
    public BaoGia docBaoGiaNoiBo(String ma) {
        try {
            return docBaoGia(ketNoi(), ma);
        } catch (SQLException ex) {
            throw new IllegalStateException("Không đọc được báo giá của lệnh.", ex);
        }
    }

    // Khôi phục báo giá, các dòng được duyệt và phản hồi khách bằng JDBC.
    private BaoGia docBaoGia(Connection c, String ma) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM dbo.BaoGia WHERE MaBaoGia=?")) {
            ps.setString(1, ma);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                List<ChiTietBaoGia> ct = new ArrayList<>();
                try (PreparedStatement lenh = c.prepareStatement("SELECT * FROM dbo.ChiTietBaoGia WHERE MaBaoGia=? ORDER BY ViTri")) {
                    lenh.setString(1, ma);
                    try (ResultSet dong = lenh.executeQuery()) {
                        while (dong.next()) {
                            if (dong.getInt("ViTri") != ct.size()) {
                                throw new IllegalStateException("Thứ tự dòng báo giá không liên tục.");
                            }
                            String dv = dong.getString("MaDichVu"), pt = dong.getString("MaPhuTung");
                            ct.add(new ChiTietBaoGia(dv == null ? "" : dv, dong.getString("TenHangMuc"),
                                    dong.getString("Loai"), dong.getString("DonVi"), dong.getDouble("SoLuong"),
                                    dong.getLong("DonGia"), pt == null ? "" : pt));
                        }
                    }
                }
                entity.PhieuTiepNhan.BaoCaoKiemTra kt;
                String sql = "SELECT k.* FROM dbo.BaoCaoKiemTra k JOIN dbo.NhomBaoGia n "
                        + "ON n.MaBaoCao=k.MaBaoCao WHERE n.MaBaoGiaGoc=?";
                try (PreparedStatement lenh = c.prepareStatement(sql)) {
                    lenh.setString(1, rs.getString("MaBaoGiaGoc"));
                    try (ResultSet dong = lenh.executeQuery()) {
                        if (!dong.next()) {
                            throw new IllegalStateException("Báo giá thiếu báo cáo kiểm tra.");
                        }
                        kt = BaoCaoKiemTraDAO.docBaoCao(c, dong);
                    }
                }
                BaoGia bg = new BaoGia(ma, rs.getString("MaBaoGiaGoc"), rs.getInt("PhienBan"), kt, ct,
                        docThoiGian(rs, "NgayHen"), rs.getString("NguoiLap"), docThoiGian(rs, "NgayLap"));
                String tt = rs.getString("TrangThai");
                if (!"NHAP".equals(tt)) {
                    bg = bg.guiKhach(rs.getString("NguoiGui"), docThoiGian(rs, "NgayGui"));
                    if (!"CHO_XAC_NHAN".equals(tt)) {
                        bg = bg.ghiNhanPhanHoi(TrangThaiBaoGia.valueOf(tt), rs.getString("NguoiGhiNhan"),
                                docThoiGian(rs,"ThoiDiemPhanHoi"), docThoiGian(rs,"ThoiDiemGhiNhan"),
                                rs.getString("KenhPhanHoi"), rs.getString("GhiChuPhanHoi"));
                    }
                }
                return bg;
            }
        }
    }

    // Đọc các lệnh hoàn thành để thu ngân lập hóa đơn.
    public List<PhieuSuaChua> layLenhHoanThanhNoiBo() {
        return docDanhSachNoiBo(true);
    }

    // Đọc tất cả lệnh cho chức năng báo cáo của nhóm.
    public List<PhieuSuaChua> layLenhBaoCao() {
        return docDanhSachNoiBo(false);
    }

    // Đọc các lệnh SQL phục vụ những DAO liên kết nội bộ.
    private List<PhieuSuaChua> docDanhSachNoiBo(boolean hoanTat) {
        List<PhieuSuaChua> ds = new ArrayList<>();
        Connection c = ketNoi();
        String sql = "SELECT * FROM dbo.PhieuSuaChua " + (hoanTat ? "WHERE TrangThai='HOAN_THANH' " : "") + "ORDER BY MaPhieu";
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(docPhieu(c, rs));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không đọc được nguồn sửa chữa.", ex);
        }
        return ds;
    }

    // Kiểm tra dịch vụ đã được dùng trong báo giá trước khi quản lý xóa danh mục.
    public static boolean daSuDungDichVu(TaiKhoan tk, String ma) {
        if (tk == null || tk.getVaiTro() != VaiTro.QUAN_LY || !new TaiKhoanDAO().phienConHopLe(tk)) {
            throw new SecurityException("Chỉ quản lý được kiểm tra dịch vụ.");
        }
        try {
            Connection c = ConnectDB.getInstance().getConnection();
            try (PreparedStatement ps = c.prepareStatement("SELECT TOP 1 MaBaoGia FROM dbo.ChiTietBaoGia WHERE MaDichVu=?")) {
                ps.setString(1, ma);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không kiểm tra được dịch vụ.", ex);
        }
    }

    // Xác định tài khoản được giao chính hoặc phối hợp trong lệnh.
    private boolean duocGiao(TaiKhoan tk, PhieuSuaChua p) {
        return tk.getMaNhanVien().equals(p.getKyThuatChinh()) || tk.getMaNhanVien().equals(p.getKyThuatPhoiHop());
    }

    // Kiểm tra nội dung báo giá chưa thay đổi kể từ lúc màn hình đọc dữ liệu.
    private boolean cungBaoGia(BaoGia a, BaoGia b) {
        if (!a.getMaBaoGia().equals(b.getMaBaoGia()) || a.getPhienBan() != b.getPhienBan()
                || a.getTrangThai() != b.getTrangThai() || a.getChiTiet().size() != b.getChiTiet().size()) {
            return false;
        }
        for (int i = 0; i < a.getChiTiet().size(); i++) {
            ChiTietBaoGia x = a.getChiTiet().get(i), y = b.getChiTiet().get(i);
            if (!x.getTenHangMuc().equals(y.getTenHangMuc()) || !x.getMaDichVu().equals(y.getMaDichVu())
                    || !x.getMaPhuTung().equals(y.getMaPhuTung()) || !x.getLoai().equals(y.getLoai())
                    || !x.getDonVi().equals(y.getDonVi()) || x.getSoLuong() != y.getSoLuong() || x.getDonGia() != y.getDonGia()) {
                return false;
            }
        }
        return true;
    }

    // Cho phép bấm phát hành lại cùng nội dung mà không tạo thêm lệnh.
    private void kiemTraPhatHanhLap(PhieuSuaChua cu, TaiKhoan tk, BaoGia bg, String chinh,
            String phu, List<String> phuTrach, LocalDateTime han, String gc) {
        if (!cu.getMaBaoGia().equals(bg.getMaBaoGia()) || !cu.getKyThuatChinh().equals(chinh)
                || !cu.getKyThuatPhoiHop().equals(phu) || !cu.getHanHoanThanh().equals(han)
                || !cu.getGhiChu().equals(gc) || !cu.getNguoiPhatHanh().equals(tk.getMaNhanVien())
                || phuTrach == null || phuTrach.size() != cu.getDanhSachHangMuc().size()) {
            throw new IllegalStateException("Mã thao tác đã sử dụng cho nội dung khác.");
        }
        for (int i = 0; i < phuTrach.size(); i++) {
            if (!cu.getDanhSachHangMuc().get(i).getTaiKhoanThucHien().equals(phuTrach.get(i))) {
                throw new IllegalStateException("Mã thao tác đã sử dụng cho phân công khác.");
            }
        }
    }

    // Sinh mã SQL duy nhất cho lệnh hoặc bản ghi phân công.
    private String taoMa(Connection c, String tienTo) throws SQLException {
        String bang = "SC".equals(tienTo) ? "PhieuSuaChua" : "PhanCong";
        String cot = "SC".equals(tienTo) ? "MaPhieu" : "MaPC";
        while (true) {
            long so;
            try (PreparedStatement ps = c.prepareStatement("SELECT NEXT VALUE FOR dbo.SoMaThaoTac");
                    ResultSet rs = ps.executeQuery()) {
                rs.next();
                so = rs.getLong(1);
            }
            String ma = String.format(Locale.ROOT, "%s-%04d-%04d", tienTo, LocalDateTime.now().getYear(), so);
            try (PreparedStatement ps = c.prepareStatement("SELECT " + cot + " FROM dbo." + bang + " WHERE " + cot + "=?")) {
                ps.setString(1, ma);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return ma;
                    }
                }
            }
        }
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

    // Đọc DATETIME2 và giữ giá trị null nếu SQL chưa có thời điểm.
    private LocalDateTime docThoiGian(ResultSet rs, String cot) throws SQLException {
        Timestamp t = rs.getTimestamp(cot);
        return t == null ? null : t.toLocalDateTime();
    }

}
