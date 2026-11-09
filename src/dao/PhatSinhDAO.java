package dao;

import connectDB.ConnectDB;
import entity.BaoGia.ChiTietBaoGia;
import entity.DeXuatPhatSinh;
import entity.PhieuSuaChua;
import entity.PhieuSuaChua.PhatSinhSuaChua;
import entity.PhieuSuaChua.TrangThaiPhatSinh;
import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

// Đọc và ghi các bảng phát sinh mới; mọi thao tác thay đổi đều khóa cùng lệnh sửa chữa.
public final class PhatSinhDAO {
    // Đọc thời điểm theo cùng múi giờ nghiệp vụ của nhóm.
    private static LocalDateTime bayGio() {
        return LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
    }

    // Kiểm tra phiên và vai trò trước khi truy cập dữ liệu phát sinh.
    private static void quyen(TaiKhoan tk, VaiTro vaiTro) {
        if (tk == null || tk.getVaiTro() != vaiTro) {
            throw new SecurityException("Không có quyền thao tác phát sinh.");
        }
        tk.kiemTraPhien();
    }

    // Lấy kết nối chung đã được cấu hình trong project.
    private static Connection ketNoi() {
        try {
            return ConnectDB.getInstance().getConnection();
        } catch (SQLException ex) {
            throw new IllegalStateException("Không kết nối được SQL Server.", ex);
        }
    }

    // Chuẩn hóa nội dung bắt buộc theo giới hạn cột SQL.
    private static String batBuoc(String s, int dai, String ten) {
        if (s == null || s.isBlank() || s.trim().length() > dai)
            throw new IllegalArgumentException(ten + " bắt buộc và tối đa " + dai + " ký tự.");
        return s.trim();
    }

    // Xác nhận người dùng được đọc lệnh; kỹ thuật chỉ đọc trong phạm vi phân công.
    public List<DeXuatPhatSinh> docDanhSach(TaiKhoan tk, String maLenh) {
        if (tk == null || (tk.getVaiTro() != VaiTro.NHAN_VIEN_TIEP_NHAN
                && tk.getVaiTro() != VaiTro.NHAN_VIEN_KY_THUAT)) throw new SecurityException("Không có quyền xem phát sinh.");
        tk.kiemTraPhien();
        if (new PhieuSuaChuaDAO().timTheoMa(tk, maLenh) == null)
            throw new SecurityException("Lệnh không tồn tại hoặc không thuộc phân công.");
        try {
            return docDanhSach(ketNoi(), maLenh);
        } catch (SQLException ex) {
            throw new IllegalStateException("Không đọc được phát sinh.", ex);
        }
    }

    // Đọc đủ nội dung, chi tiết giá và phản hồi đã lưu trên cùng kết nối JDBC.
    static List<DeXuatPhatSinh> docDanhSach(Connection c, String maLenh) throws SQLException {
        List<DeXuatPhatSinh> ds = new ArrayList<>();
        String sql = "SELECT d.*,p.TenNguoiXacNhanKhach,p.KenhPhanHoi,p.GhiChu AS GhiChuPhanHoi,"
                + "p.NguoiGhiNhan,p.ThoiDiemKhachPhanHoi FROM dbo.DeXuatPhatSinh d OUTER APPLY "
                + "(SELECT TOP 1 * FROM dbo.PhanHoiPhatSinh WHERE MaPhatSinh=d.MaPhatSinh "
                + "ORDER BY MaPhanHoi DESC) p WHERE d.MaPhieu=? ORDER BY d.SoThuTu";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maLenh);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long ma = rs.getLong("MaPhatSinh");
                    List<ChiTietBaoGia> chiTiet = new ArrayList<>();
                    try (PreparedStatement ct = c.prepareStatement(
                            "SELECT * FROM dbo.ChiTietDeXuatPhatSinh WHERE MaPhatSinh=? ORDER BY ViTri")) {
                        ct.setLong(1, ma);
                        try (ResultSet dong = ct.executeQuery()) {
                            while (dong.next()) chiTiet.add(docDong(dong));
                        }
                    }
                    Timestamp phanHoi = rs.getTimestamp("ThoiDiemKhachPhanHoi");
                    int phut = rs.getInt("ThoiGianDuKienPhut");
                    Integer soPhut = rs.wasNull() ? null : phut;
                    ds.add(new DeXuatPhatSinh(ma, maLenh, rs.getInt("SoThuTu"), rs.getString("HuHong"),
                            rs.getString("NguyenNhan"), rs.getString("DeXuat"), rs.getString("TrangThai"), soPhut,
                            rs.getString("NguoiBao"), rs.getTimestamp("ThoiDiemBao").toLocalDateTime(), chiTiet,
                            rs.getString("TenNguoiXacNhanKhach"), rs.getString("KenhPhanHoi"),
                            rs.getString("GhiChuPhanHoi"), rs.getString("NguoiGhiNhan"),
                            phanHoi == null ? null : phanHoi.toLocalDateTime()));
                }
            }
        }
        return ds;
    }

    // Đọc một dòng dịch vụ hoặc phụ tùng trong phụ lục giá.
    private static ChiTietBaoGia docDong(ResultSet rs) throws SQLException {
        String dv = rs.getString("MaDichVu"), pt = rs.getString("MaPhuTung");
        return new ChiTietBaoGia(dv == null ? "" : dv, rs.getString("TenHangMuc"), rs.getString("Loai"),
                rs.getString("DonVi"), rs.getDouble("SoLuong"), rs.getLong("DonGia"), pt == null ? "" : pt);
    }

    // Gửi một phát sinh mới; gửi lại cùng nội dung đang chờ không tạo thêm bản ghi.
    public void gui(TaiKhoan tk, String maLenh, String huHong, String nguyenNhan, String deXuat) {
        quyen(tk, VaiTro.NHAN_VIEN_KY_THUAT);
        huHong = batBuoc(huHong, 2000, "Hư hỏng");
        nguyenNhan = batBuoc(nguyenNhan, 2000, "Mô tả/nguyên nhân");
        deXuat = batBuoc(deXuat, 2000, "Đề xuất xử lý");
        Connection c = ketNoi();
        try {
            c.setAutoCommit(false);
            new PhieuSuaChuaDAO().khoaLenh(c, tk, maLenh);
            List<DeXuatPhatSinh> ds = docDanhSach(c, maLenh);
            int thuTu = 1;
            for (DeXuatPhatSinh d : ds) {
                thuTu = Math.max(thuTu, d.getSoThuTu() + 1);
                if ("CHO_KHACH".equals(d.getTrangThai()) && d.getHuHong().equals(huHong)
                        && d.getNguyenNhan().equals(nguyenNhan) && d.getDeXuat().equals(deXuat)) {
                    c.commit();
                    return;
                }
            }
            String sql = "INSERT dbo.DeXuatPhatSinh(MaPhieu,SoThuTu,HuHong,NguyenNhan,DeXuat,"
                    + "TrangThai,NguoiBao,ThoiDiemBao) VALUES(?,?,?,?,?,'CHO_KHACH',?,?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, maLenh);
                ps.setInt(2, thuTu);
                ps.setString(3, huHong);
                ps.setString(4, nguyenNhan);
                ps.setString(5, deXuat);
                ps.setString(6, tk.getMaNhanVien());
                ps.setTimestamp(7, Timestamp.valueOf(bayGio()));
                ps.executeUpdate();
            }
            c.commit();
        } catch (SQLException | RuntimeException ex) {
            hoanTac(c);
            throw loi(ex);
        } finally {
            tuDongLuu(c);
        }
    }

    // Khóa lệnh trước rồi khóa đề xuất; mọi thao tác dùng cùng thứ tự để tránh tranh chấp.
    private DeXuatPhatSinh khoaDeXuat(Connection c, DeXuatPhatSinh daDoc) throws SQLException {
        if (daDoc == null) {
            throw new IllegalArgumentException("Chưa chọn phát sinh.");
        }
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT TrangThai FROM dbo.PhieuSuaChua WITH(UPDLOCK,HOLDLOCK) WHERE MaPhieu=?")) {
            ps.setString(1, daDoc.getMaPhieu());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Lệnh không còn tồn tại.");
                }
                if ("HOAN_THANH".equals(rs.getString(1))) {
                    throw new IllegalStateException("Lệnh đã hoàn thành.");
                }
            }
        }
        for (DeXuatPhatSinh d : docDanhSach(c, daDoc.getMaPhieu())) {
            if (d.getMaPhatSinh() == daDoc.getMaPhatSinh()) {
                return d;
            }
        }
        throw new IllegalStateException("Phát sinh không còn tồn tại.");
    }

    // Lưu phụ lục giá khi còn chờ khách; bản đã có phản hồi không được sửa.
    public void luuGia(TaiKhoan tk, DeXuatPhatSinh daDoc, List<ChiTietBaoGia> chiTiet, int soPhut) {
        quyen(tk, VaiTro.NHAN_VIEN_TIEP_NHAN);
        if (chiTiet == null || chiTiet.isEmpty() || soPhut < 0)
            throw new IllegalArgumentException("Cần hạng mục giá bổ sung và số phút không âm.");
        Connection c = ketNoi();
        try {
            c.setAutoCommit(false);
            DeXuatPhatSinh hienTai = khoaDeXuat(c, daDoc);
            if (!"CHO_KHACH".equals(hienTai.getTrangThai()) || !hienTai.cungBaoGia(daDoc))
                throw new IllegalStateException("Nội dung đã đổi hoặc đã có phản hồi; hãy tải lại.");
            long tong = 0;
            for (ChiTietBaoGia dong : chiTiet) {
                if (dong == null) {
                    throw new IllegalArgumentException("Hạng mục không hợp lệ.");
                }
                kiemTraDanhMuc(c, dong);
                tong = Math.addExact(tong, dong.getThanhTien());
            }
            try (PreparedStatement ps = c.prepareStatement("DELETE dbo.ChiTietDeXuatPhatSinh WHERE MaPhatSinh=?")) {
                ps.setLong(1, daDoc.getMaPhatSinh());
                ps.executeUpdate();
            }
            String sql = "INSERT dbo.ChiTietDeXuatPhatSinh(MaPhatSinh,ViTri,MaDichVu,MaPhuTung,TenHangMuc,"
                    + "Loai,DonVi,SoLuong,DonGia) VALUES(?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                for (int i = 0; i < chiTiet.size(); i++) {
                    ChiTietBaoGia dong = chiTiet.get(i);
                    ps.setLong(1, daDoc.getMaPhatSinh());
                    ps.setInt(2, i);
                    if (dong.getMaDichVu().isEmpty()) ps.setNull(3, Types.VARCHAR);
                    else ps.setString(3, dong.getMaDichVu());
                    if (dong.getMaPhuTung().isEmpty()) ps.setNull(4, Types.VARCHAR);
                    else ps.setString(4, dong.getMaPhuTung());
                    ps.setString(5, dong.getTenHangMuc());
                    ps.setString(6, dong.getLoai());
                    ps.setString(7, dong.getDonVi());
                    ps.setBigDecimal(8, BigDecimal.valueOf(dong.getSoLuong()));
                    ps.setLong(9, dong.getDonGia());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE dbo.DeXuatPhatSinh SET ThoiGianDuKienPhut=? WHERE MaPhatSinh=?")) {
                ps.setInt(1, soPhut);
                ps.setLong(2, daDoc.getMaPhatSinh());
                ps.executeUpdate();
            }
            c.commit();
        } catch (SQLException | RuntimeException ex) {
            hoanTac(c);
            throw loi(ex);
        } finally {
            tuDongLuu(c);
        }
    }

    // Đối chiếu mã, tên và đơn vị với danh mục SQL, không tin giá trị giả từ giao diện.
    private void kiemTraDanhMuc(Connection c, ChiTietBaoGia dong) throws SQLException {
        boolean phuTung = "Phụ tùng".equals(dong.getLoai());
        if ((!phuTung && !"Dịch vụ".equals(dong.getLoai())) || dong.getTenHangMuc().length() > 200
                || (phuTung && (!dong.getMaDichVu().isEmpty() || dong.getMaPhuTung().isEmpty()))
                || (!phuTung && (!dong.getMaPhuTung().isEmpty() || dong.getMaDichVu().isEmpty())))
            throw new IllegalArgumentException("Mã danh mục và loại hạng mục không hợp lệ.");
        String sql = phuTung ? "SELECT TenPhuTung,DonViTinh,HoatDong FROM dbo.PhuTung WHERE MaPhuTung=?"
                : "SELECT TenDichVu,DonViTinh,DangApDung FROM dbo.DichVu WHERE MaDichVu=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, phuTung ? dong.getMaPhuTung() : dong.getMaDichVu());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !rs.getBoolean(3) || !rs.getString(1).equals(dong.getTenHangMuc())
                        || !rs.getString(2).equals(dong.getDonVi()))
                    throw new IllegalStateException("Danh mục đã thay đổi hoặc ngừng áp dụng; hãy chọn lại.");
            }
        }
    }

    // Ghi quyết định khách đúng phụ lục đã mở; đồng ý mới thêm công việc và liên kết thực hiện.
    public void phanHoi(TaiKhoan tk, DeXuatPhatSinh daDoc, boolean dongY, String tenKhach, String kenh,
            String ghiChu, LocalDateTime lucKhach) {
        quyen(tk, VaiTro.NHAN_VIEN_TIEP_NHAN);
        tenKhach = batBuoc(tenKhach, 150, "Tên người xác nhận phía khách");
        kenh = batBuoc(kenh, 100, "Kênh phản hồi");
        ghiChu = batBuoc(ghiChu, 2000, "Nội dung phản hồi");
        if (lucKhach == null || lucKhach.isAfter(bayGio()))
            throw new IllegalArgumentException("Thời điểm phản hồi không được ở tương lai.");
        Connection c = ketNoi();
        try {
            c.setAutoCommit(false);
            DeXuatPhatSinh hienTai = khoaDeXuat(c, daDoc);
            String quyetDinh = dongY ? "DA_DONG_Y" : "TU_CHOI";
            if (!hienTai.cungBaoGia(daDoc)) {
                throw new IllegalStateException("Phụ lục giá đã đổi; tải lại trước khi ghi phản hồi.");
            }
            if (!"CHO_KHACH".equals(hienTai.getTrangThai())) {
                if (quyetDinh.equals(hienTai.getTrangThai()) && tenKhach.equals(hienTai.getTenKhach())
                        && kenh.equals(hienTai.getKenh()) && ghiChu.equals(hienTai.getGhiChu())
                        && lucKhach.equals(hienTai.getThoiDiemPhanHoi())
                        && tk.getMaNhanVien().equals(hienTai.getNguoiGhiNhan())) {
                    c.commit();
                    return;
                }
                throw new IllegalStateException("Đã ghi quyết định khách; không ghi đè hoặc tạo công việc lần nữa.");
            }
            if (lucKhach.isBefore(hienTai.getThoiDiemBao()))
                throw new IllegalArgumentException("Phản hồi không được trước khi báo phát sinh.");
            if (dongY && hienTai.getChiTiet().isEmpty())
                throw new IllegalStateException("Cần lưu phụ lục giá trước khi khách đồng ý.");
            String sql = "INSERT dbo.PhanHoiPhatSinh(MaPhatSinh,QuyetDinh,TenNguoiXacNhanKhach,KenhPhanHoi,"
                    + "GhiChu,NguoiGhiNhan,ThoiDiemKhachPhanHoi,ThoiDiemGhiNhan) VALUES(?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setLong(1, daDoc.getMaPhatSinh());
                ps.setString(2, quyetDinh);
                ps.setString(3, tenKhach);
                ps.setString(4, kenh);
                ps.setString(5, ghiChu);
                ps.setString(6, tk.getMaNhanVien());
                ps.setTimestamp(7, Timestamp.valueOf(lucKhach));
                ps.setTimestamp(8, Timestamp.valueOf(bayGio()));
                ps.executeUpdate();
            }
            if (dongY) themCongViec(c, hienTai);
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE dbo.DeXuatPhatSinh SET TrangThai=? WHERE MaPhatSinh=? AND TrangThai='CHO_KHACH'")) {
                ps.setString(1, quyetDinh);
                ps.setLong(2, hienTai.getMaPhatSinh());
                if (ps.executeUpdate() != 1) {
                    throw new IllegalStateException("Phản hồi đã thay đổi.");
                }
            }
            c.commit();
        } catch (SQLException | RuntimeException ex) {
            hoanTac(c);
            throw loi(ex);
        } finally {
            tuDongLuu(c);
        }
    }

    // Thêm các dòng được khách đồng ý vào cuối lệnh, giao cho kỹ thuật viên chính của lệnh.
    private void themCongViec(Connection c, DeXuatPhatSinh d) throws SQLException {
        int viTri;
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COALESCE(MAX(ViTri),-1)+1 FROM dbo.ChiTietSuaChua WHERE MaPhieu=?")) {
            ps.setString(1, d.getMaPhieu());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                viTri = rs.getInt(1);
            }
        }
        for (int i = 0; i < d.getChiTiet().size(); i++) {
            ChiTietBaoGia dong = d.getChiTiet().get(i);
            String sql = "INSERT dbo.ChiTietSuaChua(MaPhieu,ViTri,TenHangMuc,TaiKhoanThucHien,KetQua,TrangThai,PhatSinh) "
                    + "SELECT MaPhieu,?,?,KyThuatChinh,'','CHO_SUA_CHUA',1 FROM dbo.PhieuSuaChua WHERE MaPhieu=?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, viTri + i);
                ps.setString(2, dong.getTenHangMuc());
                ps.setString(3, d.getMaPhieu());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("INSERT dbo.ChiTietSuaChuaPhatSinh"
                    + "(MaPhieu,ViTriSuaChua,MaPhatSinh,ViTriDeXuat) VALUES(?,?,?,?)")) {
                ps.setString(1, d.getMaPhieu());
                ps.setInt(2, viTri + i);
                ps.setLong(3, d.getMaPhatSinh());
                ps.setInt(4, i);
                ps.executeUpdate();
            }
        }
        try (PreparedStatement ps = c.prepareStatement("UPDATE dbo.PhieuSuaChua SET TrangThai='DANG_SUA',"
                + "HanHoanThanh=DATEADD(MINUTE,?,HanHoanThanh) WHERE MaPhieu=?")) {
            ps.setInt(1, d.getThoiGianDuKienPhut() == null ? 0 : d.getThoiGianDuKienPhut());
            ps.setString(2, d.getMaPhieu());
            ps.executeUpdate();
        }
    }

    // Kiểm tra đúng phát sinh của hạng mục, không lấy trạng thái chung của cả lệnh.
    static void kiemTraHangMuc(Connection c, String maLenh, int viTri) throws SQLException {
        String sql = "SELECT d.TrangThai FROM dbo.ChiTietSuaChuaPhatSinh l JOIN dbo.DeXuatPhatSinh d "
                + "ON d.MaPhatSinh=l.MaPhatSinh WHERE l.MaPhieu=? AND l.ViTriSuaChua=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maLenh);
            ps.setInt(2, viTri);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !"DA_DONG_Y".equals(rs.getString(1)))
                    throw new IllegalStateException("Hạng mục phát sinh chưa được khách đồng ý.");
            }
        }
    }

    // Đọc đúng dòng giá đã duyệt của công việc gốc hoặc công việc phát sinh.
    static ChiTietBaoGia docDongDuocDuyet(Connection c, String maLenh, int viTri) throws SQLException {
        String cot = "ct.MaDichVu,ct.MaPhuTung,ct.TenHangMuc,ct.Loai,ct.DonVi,ct.SoLuong,ct.DonGia";
        String sql = "SELECT " + cot + " FROM dbo.ChiTietBaoGia ct JOIN dbo.PhieuSuaChua p ON p.MaBaoGia=ct.MaBaoGia "
                + "JOIN dbo.BaoGia bg ON bg.MaBaoGia=ct.MaBaoGia WHERE p.MaPhieu=? AND ct.ViTri=? "
                + "AND bg.TrangThai='DA_DONG_Y' UNION ALL SELECT " + cot
                + " FROM dbo.ChiTietSuaChuaPhatSinh l JOIN dbo.DeXuatPhatSinh d ON d.MaPhatSinh=l.MaPhatSinh "
                + "JOIN dbo.ChiTietDeXuatPhatSinh ct ON ct.MaPhatSinh=l.MaPhatSinh AND ct.ViTri=l.ViTriDeXuat "
                + "WHERE l.MaPhieu=? AND l.ViTriSuaChua=? AND d.TrangThai='DA_DONG_Y'";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maLenh);
            ps.setInt(2, viTri);
            ps.setString(3, maLenh);
            ps.setInt(4, viTri);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? docDong(rs) : null;
            }
        }
    }

    // Cho các DAO ngoài đọc căn cứ phụ tùng mà vẫn kiểm tra quyền lệnh.
    public ChiTietBaoGia docDongDuocDuyet(TaiKhoan tk, String maLenh, int viTri) {
        quyen(tk, VaiTro.NHAN_VIEN_KY_THUAT);
        if (new PhieuSuaChuaDAO().timTheoMa(tk, maLenh) == null) {
            throw new SecurityException("Không có quyền đọc lệnh.");
        }
        try {
            return docDongDuocDuyet(ketNoi(), maLenh, viTri);
        } catch (SQLException ex) {
            throw new IllegalStateException("Không đọc được căn cứ hạng mục.", ex);
        }
    }

    // Kiểm tra còn đề xuất chưa xử lý để chặn hoàn tất cả lệnh.
    static boolean coChoKhach(Connection c, String maLenh) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT 1 FROM dbo.DeXuatPhatSinh WHERE MaPhieu=? AND TrangThai='CHO_KHACH'")) {
            ps.setString(1, maLenh);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Tạo trạng thái tổng hợp tương thích với các màn hình cũ trong project.
    static PhatSinhSuaChua docTongHop(Connection c, String maLenh) throws SQLException {
        List<DeXuatPhatSinh> ds = docDanhSach(c, maLenh);
        if (ds.isEmpty()) {
            return null;
        }
        DeXuatPhatSinh chon = ds.get(ds.size() - 1);
        for (DeXuatPhatSinh d : ds) {
            if ("CHO_KHACH".equals(d.getTrangThai())) {
                chon = d;
                break;
            }
        }
        TrangThaiPhatSinh tt = "CHO_KHACH".equals(chon.getTrangThai()) ? TrangThaiPhatSinh.CHO_DUYET
                : "DA_DONG_Y".equals(chon.getTrangThai()) ? TrangThaiPhatSinh.DA_DUYET : TrangThaiPhatSinh.TU_CHOI;
        String nguoi = chon.getNguoiGhiNhan() == null ? chon.getNguoiBao() : chon.getNguoiGhiNhan();
        LocalDateTime luc = chon.getThoiDiemPhanHoi() == null ? chon.getThoiDiemBao() : chon.getThoiDiemPhanHoi();
        return new PhatSinhSuaChua(new String[] {chon.getHuHong(),chon.getNguyenNhan(),chon.getDeXuat(),"",""},
                tt, nguoi, tt == TrangThaiPhatSinh.CHO_DUYET ? null : luc);
    }

    // Đọc danh mục dịch vụ SQL để lập giá bổ sung cùng dữ liệu với khóa ngoại.
    public List<entity.DichVu> docDichVu(TaiKhoan tk) {
        quyen(tk, VaiTro.NHAN_VIEN_TIEP_NHAN);
        List<entity.DichVu> ds = new ArrayList<>();
        try (PreparedStatement ps = ketNoi().prepareStatement("SELECT * FROM dbo.DichVu WHERE DangApDung=1");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ds.add(new entity.DichVu(rs.getString("MaDichVu"), rs.getString("TenDichVu"),
                    rs.getString("NhomDichVu"), rs.getString("DonViTinh"), rs.getLong("DonGia"),
                    rs.getString("MoTa"), rs.getString("ThoiGianDuKien"), rs.getString("DieuKienApDung"), true));
        } catch (SQLException ex) {
            throw new IllegalStateException("Không đọc được dịch vụ bổ sung.", ex);
        }
        return ds;
    }

    // Hoàn tác toàn bộ khi bất kỳ bước lưu nào thất bại.
    private static void hoanTac(Connection c) {
        try {
            c.rollback();
        } catch (SQLException ex) {
            throw new IllegalStateException("Không rollback được phát sinh.", ex);
        }
    }

    // Khôi phục chế độ tự động lưu cho kết nối dùng chung.
    private static void tuDongLuu(Connection c) {
        try {
            c.setAutoCommit(true);
        } catch (SQLException ex) {
            throw new IllegalStateException("Không khôi phục được kết nối.", ex);
        }
    }

    // Giữ thông báo nghiệp vụ; bọc lỗi JDBC để giao diện có thể báo cho người dùng.
    private static RuntimeException loi(Exception ex) {
        return ex instanceof RuntimeException ? (RuntimeException) ex : new IllegalStateException("Không lưu được phát sinh.", ex);
    }
}
