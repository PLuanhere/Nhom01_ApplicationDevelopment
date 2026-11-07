package dao;

import connectDB.ConnectDB;
import entity.PhuTung;
import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Quản lý danh mục, tồn kho và nhật ký bằng JDBC.
public final class PhuTungDAO {
    private final PhieuNhapKhoDAO nhapKhoDAO = new PhieuNhapKhoDAO(this);

    // Khởi tạo DAO, không thêm danh mục mẫu vào bộ nhớ.
    public PhuTungDAO() {
    }

    // Trả nguồn nhập kho mà giao diện quản lý đang sử dụng.
    public PhieuNhapKhoDAO getNhapKhoDAO() {
        return nhapKhoDAO;
    }

    // Đọc danh mục theo quyền quản lý.
    public List<PhuTung> layDanhSach(TaiKhoan tk) {
        kiemTraQuyen(tk, VaiTro.QUAN_LY);
        return danhMucNoiBo();
    }

    // Lọc phụ tùng theo từ khóa, mức tồn và trạng thái hoạt động.
    public List<PhuTung> timKiem(TaiKhoan tk, String tuKhoa, String tinhTrang, int hoatDong) {
        kiemTraQuyen(tk, VaiTro.QUAN_LY);
        List<PhuTung> ds = new ArrayList<>();
        String khoa = boDau(tuKhoa == null ? "" : tuKhoa.trim());
        for (PhuTung pt : danhMucNoiBo()) {
            String chu = pt.getMaPhuTung() + " " + pt.getTenPhuTung() + " " + pt.getLoaiPhuTung();
            if (boDau(chu).contains(khoa) && (hoatDong == -1 || pt.isHoatDong() == (hoatDong == 1))
                    && (tinhTrang == null || tinhTrang.equals(tinhTrangTon(tk, pt)))) {
                ds.add(pt);
            }
        }
        return ds;
    }

    // Sắp xếp kết quả lọc theo mã, tên hoặc số lượng tồn.
    public List<PhuTung> timKiem(TaiKhoan tk, String tuKhoa, String tinhTrang, int hoatDong, int sapXep) {
        List<PhuTung> ds = timKiem(tk, tuKhoa, tinhTrang, hoatDong);
        for (int i = 0; i < ds.size() - 1; i++) {
            for (int j = i + 1; j < ds.size(); j++) {
                PhuTung a = ds.get(i), b = ds.get(j);
                int ss;
                if (sapXep == 0) {
                    ss = a.getMaPhuTung().compareToIgnoreCase(b.getMaPhuTung());
                } else if (sapXep == 3 || sapXep == 4) {
                    ss = Double.compare(a.getSoLuongTon(), b.getSoLuongTon());
                } else {
                    ss = boDau(a.getTenPhuTung()).compareTo(boDau(b.getTenPhuTung()));
                }
                if (sapXep == 2 || sapXep == 4) {
                    ss = -ss;
                }
                if (ss > 0) {
                    ds.set(i, b);
                    ds.set(j, a);
                }
            }
        }
        return ds;
    }

    // Đọc một phụ tùng sau khi kiểm tra quyền quản lý.
    public PhuTung timTheoMa(TaiKhoan tk, String ma) {
        kiemTraQuyen(tk, VaiTro.QUAN_LY);
        return timNoiBo(ma);
    }

    // Đọc số tồn hiện tại theo quyền quản lý.
    public double layTon(TaiKhoan tk, String ma) {
        kiemTraQuyen(tk, VaiTro.QUAN_LY);
        return tonNoiBo(ma);
    }

    // Phân loại tồn kho theo mức tối thiểu của phụ tùng.
    public String tinhTrangTon(TaiKhoan tk, PhuTung pt) {
        kiemTraQuyen(tk, VaiTro.QUAN_LY);
        double so = pt.getSoLuongTon();
        if (so == 0) {
            return "Hết hàng";
        }
        return so <= pt.getMucToiThieu() ? "Sắp hết" : "Đủ tồn";
    }

    // Thêm danh mục mới với tồn đầu bằng không.
    public void them(TaiKhoan tk, PhuTung pt) {
        kiemTraQuyen(tk, VaiTro.QUAN_LY);
        if (pt == null) {
            throw new IllegalArgumentException("Thiếu phụ tùng.");
        }
        String sql = "INSERT dbo.PhuTung (TenPhuTung,LoaiPhuTung,DonViTinh,GiaNhap,GiaBan,MucToiThieu,"
                + "HoatDong,MaPhuTung,SoLuongTon) VALUES (?,?,?,?,?,?,?,?,0)";
        try (PreparedStatement ps = ketNoi().prepareStatement(sql)) {
            ganDanhMuc(ps, pt);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thêm được danh mục; kiểm tra mã trùng.", ex);
        }
    }

    // Cập nhật thông tin danh mục và giữ nguyên tồn kho, mã, đơn vị tính.
    public void capNhat(TaiKhoan tk, PhuTung cu, PhuTung moi) {
        kiemTraQuyen(tk, VaiTro.QUAN_LY);
        if (cu == null || moi == null || !cu.getMaPhuTung().equals(moi.getMaPhuTung())
                || !cu.getDonViTinh().equals(moi.getDonViTinh())) {
            throw new IllegalArgumentException("Không đổi mã hoặc đơn vị tính của phụ tùng.");
        }
        Connection c = ketNoi();
        try {
            c.setAutoCommit(false);
            PhuTung hienTai = docTheoMa(c, cu.getMaPhuTung(), true);
            if (!cungThongTin(hienTai, cu)) {
                throw new IllegalStateException("Danh mục đã thay đổi. Hãy tải lại.");
            }
            String sql = "UPDATE dbo.PhuTung SET TenPhuTung=?,LoaiPhuTung=?,DonViTinh=?,GiaNhap=?,"
                    + "GiaBan=?,MucToiThieu=?,HoatDong=? WHERE MaPhuTung=?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ganDanhMuc(ps, moi);
                ps.executeUpdate();
            }
            c.commit();
        } catch (SQLException | RuntimeException ex) {
            hoanTac(c);
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new IllegalStateException("Không cập nhật được danh mục.", ex);
        } finally {
            batTuDongLuu(c);
        }
    }

    // Đọc lịch sử biến động từ SQL và lọc theo thời gian, loại, mã phụ tùng.
    public List<BienDongKho> layNhatKy(TaiKhoan tk, String tuKhoa, String loai, String maPT,
            LocalDate tuNgay, LocalDate denNgay) {
        kiemTraQuyen(tk, VaiTro.QUAN_LY);
        if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) {
            throw new IllegalArgumentException("Từ ngày không được sau đến ngày.");
        }
        List<BienDongKho> ds = new ArrayList<>();
        String khoa = boDau(tuKhoa == null ? "" : tuKhoa.trim());
        Connection c = ketNoi();
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM dbo.BienDongKho ORDER BY MaBienDong DESC");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LocalDateTime luc = rs.getTimestamp("ThoiDiem").toLocalDateTime();
                String ma = rs.getString("MaPhuTung"), loaiDong = rs.getString("Loai");
                PhuTung pt = docTheoMa(c, ma, false);
                String chu = ma + " " + pt.getTenPhuTung() + " " + rs.getString("ChungTu") + " "
                        + loaiDong + " " + rs.getString("LyDo");
                if ((loai == null || loai.equals(loaiDong)) && (maPT == null || maPT.equals(ma))
                        && (tuNgay == null || !luc.toLocalDate().isBefore(tuNgay))
                        && (denNgay == null || !luc.toLocalDate().isAfter(denNgay)) && boDau(chu).contains(khoa)) {
                    ds.add(new BienDongKho(pt, luc, loaiDong, rs.getDouble("TonTruoc"), rs.getDouble("TonSau"),
                            rs.getString("ChungTu"), rs.getString("NguoiThucHien"), rs.getString("LyDo")));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không đọc được lịch sử kho.", ex);
        }
        return ds;
    }

    // Điều chỉnh số tồn sau kiểm kê và lưu nhật ký trong một giao dịch.
    public void dieuChinh(TaiKhoan tk, PhuTung pt, double tonDaDoc, double kiemKe, String lyDo, String dienGiai) {
        kiemTraQuyen(tk, VaiTro.QUAN_LY);
        if (pt == null || !Double.isFinite(kiemKe) || kiemKe < 0 || lyDo == null || lyDo.isBlank()) {
            throw new IllegalArgumentException("Cần phụ tùng, số kiểm kê không âm và lý do.");
        }
        if (kiemKe > 0) {
            pt.kiemTraSoLuong(kiemKe);
        }
        Connection c = ketNoi();
        try {
            c.setAutoCommit(false);
            PhuTung hienTai = docTheoMa(c, pt.getMaPhuTung(), true);
            if (!cungThongTin(hienTai, pt) || hienTai.getSoLuongTon() != tonDaDoc) {
                throw new IllegalStateException("Danh mục hoặc số tồn đã thay đổi.");
            }
            if (tonDaDoc == kiemKe) {
                throw new IllegalArgumentException("Không có chênh lệch kiểm kê.");
            }
            capNhatTon(c, pt.getMaPhuTung(), kiemKe);
            ghiNhatKy(c, pt.getMaPhuTung(), LocalDateTime.now(), "Điều chỉnh", tonDaDoc, kiemKe,
                    dienGiai == null ? "" : dienGiai.trim(), tk.getMaNhanVien(), lyDo.trim());
            c.commit();
        } catch (SQLException | RuntimeException ex) {
            hoanTac(c);
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new IllegalStateException("Không điều chỉnh được tồn kho.", ex);
        } finally {
            batTuDongLuu(c);
        }
    }

    // Đọc danh mục để các DAO khác dùng chung dữ liệu SQL.
    List<PhuTung> danhMucNoiBo() {
        List<PhuTung> ds = new ArrayList<>();
        try (PreparedStatement ps = ketNoi().prepareStatement("SELECT * FROM dbo.PhuTung ORDER BY MaPhuTung");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(docPhuTung(rs));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không đọc được danh mục phụ tùng.", ex);
        }
        return ds;
    }

    // Đọc danh mục đang hoạt động cho các nghiệp vụ tạo mới.
    List<PhuTung> danhMucDangHoatDong() {
        List<PhuTung> ds = new ArrayList<>();
        for (PhuTung pt : danhMucNoiBo()) {
            if (pt.isHoatDong()) {
                ds.add(pt);
            }
        }
        return ds;
    }

    // Đọc một phụ tùng từ SQL để các DAO liên kết sử dụng.
    PhuTung timNoiBo(String ma) {
        try {
            return docTheoMa(ketNoi(), ma, false);
        } catch (SQLException ex) {
            throw new IllegalStateException("Không đọc được phụ tùng.", ex);
        }
    }

    // Đọc tồn SQL thay vì số tồn trong danh sách mẫu.
    double tonNoiBo(String ma) {
        return timNoiBo(ma).getSoLuongTon();
    }

    // Nhập hoặc xuất các dòng kho trong một giao dịch do phương thức này quản lý.
    void ghiBienDong(String[] ma, double[] so, boolean nhap, String chungTu, TaiKhoan tk,
            String lyDo, LocalDateTime luc) {
        kiemTraQuyen(tk, VaiTro.QUAN_LY, VaiTro.NHAN_VIEN_KY_THUAT);
        Connection c = ketNoi();
        try {
            c.setAutoCommit(false);
            ghiBienDong(c, ma, so, nhap, chungTu, tk, lyDo, luc);
            c.commit();
        } catch (SQLException | RuntimeException ex) {
            hoanTac(c);
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new IllegalStateException("Không cập nhật được kho.", ex);
        } finally {
            batTuDongLuu(c);
        }
    }

    // Ghi kho trên kết nối giao dịch của DAO gọi, không tự commit giữa chừng.
    void ghiBienDong(Connection c, String[] ma, double[] so, boolean nhap, String chungTu,
            TaiKhoan tk, String lyDo, LocalDateTime luc) throws SQLException {
        if (ma == null || so == null || ma.length == 0 || ma.length != so.length
                || (nhap && tk.getVaiTro() != VaiTro.QUAN_LY)) {
            throw new IllegalArgumentException("Dòng kho hoặc quyền nhập kho không hợp lệ.");
        }
        for (int i = 0; i < ma.length; i++) {
            for (int j = 0; j < i; j++) {
                if (ma[i].equals(ma[j])) {
                    throw new IllegalArgumentException("Cần gộp các dòng cùng mã phụ tùng.");
                }
            }
            PhuTung pt = docTheoMa(c, ma[i], true);
            pt.kiemTraSoLuong(so[i]);
            double tonCu = pt.getSoLuongTon();
            double tonMoi = Math.round((tonCu + (nhap ? so[i] : -so[i])) * 1000) / 1000.0;
            pt.setSoLuongTon(tonMoi);
            capNhatTon(c, ma[i], tonMoi);
            ghiNhatKy(c, ma[i], luc, nhap ? "Nhập" : "Xuất", tonCu, tonMoi, chungTu,
                    tk.getMaNhanVien(), lyDo == null ? "" : lyDo);
        }
    }

    // Đọc phụ tùng và tùy chọn khóa dòng trước khi thay đổi tồn.
    PhuTung docTheoMa(Connection c, String ma, boolean khoa) throws SQLException {
        String sql = "SELECT * FROM dbo.PhuTung " + (khoa ? "WITH (UPDLOCK,HOLDLOCK) " : "") + "WHERE MaPhuTung=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ma);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return docPhuTung(rs);
                }
            }
        }
        throw new IllegalArgumentException("Không tìm thấy phụ tùng: " + ma);
    }

    // Khôi phục đối tượng phụ tùng cùng số tồn từ ResultSet.
    private PhuTung docPhuTung(ResultSet rs) throws SQLException {
        PhuTung pt = new PhuTung(rs.getString("MaPhuTung"), rs.getString("TenPhuTung"), rs.getString("LoaiPhuTung"),
                rs.getString("DonViTinh"), rs.getLong("GiaNhap"), rs.getLong("GiaBan"),
                rs.getDouble("MucToiThieu"), rs.getBoolean("HoatDong"));
        pt.setSoLuongTon(rs.getDouble("SoLuongTon"));
        return pt;
    }

    // Gán các tham số chung của câu lệnh thêm hoặc sửa danh mục.
    private void ganDanhMuc(PreparedStatement ps, PhuTung pt) throws SQLException {
        ps.setString(1, pt.getTenPhuTung());
        ps.setString(2, pt.getLoaiPhuTung());
        ps.setString(3, pt.getDonViTinh());
        ps.setLong(4, pt.getGiaNhap());
        ps.setLong(5, pt.getGiaBan());
        ps.setDouble(6, pt.getMucToiThieu());
        ps.setBoolean(7, pt.isHoatDong());
        ps.setString(8, pt.getMaPhuTung());
    }

    // Cập nhật số tồn trên kết nối giao dịch đang mở.
    private void capNhatTon(Connection c, String ma, double ton) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE dbo.PhuTung SET SoLuongTon=? WHERE MaPhuTung=?")) {
            ps.setBigDecimal(1, java.math.BigDecimal.valueOf(ton));
            ps.setString(2, ma);
            ps.executeUpdate();
        }
    }

    // Lưu dấu vết thay đổi số tồn cùng chứng từ và người thực hiện.
    private void ghiNhatKy(Connection c, String ma, LocalDateTime luc, String loai, double truoc, double sau,
            String chungTu, String nguoi, String lyDo) throws SQLException {
        String sql = "INSERT dbo.BienDongKho (MaPhuTung,ThoiDiem,Loai,TonTruoc,TonSau,ChungTu,NguoiThucHien,LyDo) "
                + "VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ma);
            ps.setTimestamp(2, Timestamp.valueOf(luc));
            ps.setString(3, loai);
            ps.setBigDecimal(4, java.math.BigDecimal.valueOf(truoc));
            ps.setBigDecimal(5, java.math.BigDecimal.valueOf(sau));
            ps.setString(6, chungTu);
            ps.setString(7, nguoi);
            ps.setString(8, lyDo);
            ps.executeUpdate();
        }
    }

    // Đổi chữ tiếng Việt thành chữ thường không dấu để tìm và sắp xếp.
    private static String boDau(String chu) {
        String thuong = chu.toLowerCase();
        String[] cacNhom = { "àáảãạăằắẳẵặâầấẩẫậ", "èéẻẽẹêềếểễệ", "ìíỉĩị", "òóỏõọôồốổỗộơờớởỡợ", "ùúủũụưừứửữự", "ỳýỷỹỵ",
                "đ" };
        String thayThe = "aeiouyd";
        String ketQua = "";
        for (int i = 0; i < thuong.length(); i++) {
            char c = thuong.charAt(i);
            if (c >= '\u0300' && c <= '\u036F') {
                continue;
            }
            for (int j = 0; j < cacNhom.length; j++) {
                if (cacNhom[j].indexOf(c) >= 0) {
                    c = thayThe.charAt(j);
                    break;
                }
            }
            ketQua = ketQua + c;
        }
        return ketQua;
    }

    // So sánh dữ liệu danh mục để tránh ghi đè bản ghi đã được thay đổi.
    boolean cungThongTin(PhuTung a, PhuTung b) {
        return a.getMaPhuTung().equals(b.getMaPhuTung()) && a.getTenPhuTung().equals(b.getTenPhuTung())
                && a.getLoaiPhuTung().equals(b.getLoaiPhuTung()) && a.getDonViTinh().equals(b.getDonViTinh())
                && a.getGiaNhap() == b.getGiaNhap() && a.getGiaBan() == b.getGiaBan()
                && a.getMucToiThieu() == b.getMucToiThieu() && a.isHoatDong() == b.isHoatDong();
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


    public static final class BienDongKho {
        private final PhuTung phuTung;
        private final LocalDateTime thoiDiem;
        private final String loai;
        private final double tonTruoc;
        private final double tonSau;
        private final String chungTu;
        private final String nguoiThucHien;
        private final String lyDo;

        // Tạo bản ghi biến động và kiểm tra tồn trước, tồn sau.
        public BienDongKho(PhuTung phuTung, LocalDateTime thoiDiem, String loai, double tonTruoc, double tonSau,
                String chungTu, String nguoiThucHien, String lyDo) {
            if (phuTung == null) {
                throw new IllegalArgumentException("Cần chọn phụ tùng.");
            }
            if (thoiDiem == null) {
                throw new IllegalArgumentException("Thiếu thoiDiem.");
            }
            if (loai == null) {
                throw new IllegalArgumentException("Thiếu loai.");
            }
            if (chungTu == null) {
                throw new IllegalArgumentException("Thiếu chungTu.");
            }
            if (nguoiThucHien == null) {
                throw new IllegalArgumentException("Thiếu nguoiThucHien.");
            }
            if (lyDo == null) {
                throw new IllegalArgumentException("Thiếu lyDo.");
            }
            this.phuTung = new PhuTung(phuTung);
            this.thoiDiem = thoiDiem;
            this.loai = loai;
            this.tonTruoc = tonTruoc;
            this.tonSau = tonSau;
            this.chungTu = chungTu;
            this.nguoiThucHien = nguoiThucHien;
            this.lyDo = lyDo;
            if (!(tonTruoc >= 0 && tonTruoc <= Double.MAX_VALUE) || !(tonSau >= 0 && tonSau <= Double.MAX_VALUE)
                    || tonTruoc == tonSau) {
                throw new IllegalArgumentException("Biến động phải có chênh lệch và không được làm âm tồn.");
            }
        }

        // Trả về thông tin phụ tùng được lưu trên dòng dữ liệu.
        public PhuTung getPhuTung() {
            return phuTung;
        }

        // Trả về thời điểm ghi nhận biến động.
        public LocalDateTime getThoiDiem() {
            return thoiDiem;
        }

        // Trả về loại biến động kho.
        public String getLoai() {
            return loai;
        }

        // Trả về số lượng tồn trước khi thay đổi.
        public double getTonTruoc() {
            return tonTruoc;
        }

        // Trả về số lượng tồn sau khi thay đổi.
        public double getTonSau() {
            return tonSau;
        }

        // Tính chênh lệch tồn sau và tồn trước, giữ tối đa 3 chữ số lẻ.
        public double getChenhLech() {
            return Math.round((tonSau - tonTruoc) * 1000) / 1000.0;
        }

        // Trả về mã chứng từ liên quan.
        public String getChungTu() {
            return chungTu;
        }

        // Trả về tài khoản thực hiện thao tác.
        public String getNguoiThucHien() {
            return nguoiThucHien;
        }

        // Trả về lý do thay đổi.
        public String getLyDo() {
            return lyDo;
        }
    }
}
