package dao;

import connectDB.ConnectDB;
import entity.BaoGia;
import entity.BaoGia.ChiTietBaoGia;
import entity.BaoGia.TrangThaiBaoGia;
import entity.DichVu;
import entity.PhieuTiepNhan.BaoCaoKiemTra;
import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;
import entity.Xe;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Đọc và ghi báo giá, phiên bản và chi tiết báo giá bằng SQL Server.
 */
public final class BaoGiaDAO {
    private DichVuDAO dichVuDAO = new DichVuDAO();
    private final PhieuTiepNhanDAO phieuDAO;
    private final BaoCaoKiemTraDAO nguonBaoCao;
    private PhuTungSuDungDAO danhMucPhuTung;

    public BaoGiaDAO(PhieuTiepNhanDAO phieuDAO, BaoCaoKiemTraDAO nguonBaoCao) {
        if (phieuDAO == null || nguonBaoCao == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.phieuDAO = phieuDAO;
        this.nguonBaoCao = nguonBaoCao;
    }

    public BaoGiaDAO(PhieuTiepNhanDAO phieuDAO, BaoCaoKiemTraDAO nguonBaoCao,
                     PhuTungSuDungDAO danhMuc) {
        this(phieuDAO, nguonBaoCao);
        if (danhMuc == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        danhMucPhuTung = danhMuc;
    }

    public BaoGiaDAO(PhieuTiepNhanDAO phieuDAO, BaoCaoKiemTraDAO baoCaoDAO,
                     PhuTungSuDungDAO phuTungDAO, DichVuDAO dichVuDAO) {
        this(phieuDAO, baoCaoDAO, phuTungDAO);
        if (dichVuDAO == null) {
            throw new IllegalArgumentException("Thiếu nguồn dịch vụ.");
        }
        this.dichVuDAO = dichVuDAO;
    }

    /**
     * Chỉ đọc báo cáo đã hoàn tất, chưa có nhóm báo giá.
     */
    public List<BaoCaoKiemTra> layCanCu(TaiKhoan tk) {
        quyen(tk);
        List<BaoCaoKiemTra> ketQua = new ArrayList<>();
        for (BaoCaoKiemTra baoCao : nguonBaoCao.layHoanTatNoiBo()) {
            if (chuaCoNhom(baoCao.getMaBaoCao())) {
                ketQua.add(baoCao);
            }
        }
        return ketQua;
    }

    public BaoGia taoNhap(TaiKhoan tk, String token, String maBaoCao,
                          List<ChiTietBaoGia> chiTiet, LocalDateTime hen) {
        quyen(tk);
        kiemTraToken(token);
        kiemTraDanhMuc(tk, chiTiet);
        BaoCaoKiemTra baoCao = canCu(tk, maBaoCao);
        Connection c = ketNoi();
        try {
            boolean tuDong = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                BaoGia daLuu = timTheoToken(c, token);
                if (daLuu != null) {
                    kiemTraLap(daLuu, baoCao, chiTiet, hen, tk, null);
                    c.commit();
                    return daLuu;
                }
                if (!chuaCoNhom(c, baoCao.getMaBaoCao())) {
                    throw new IllegalStateException("Báo cáo đã có báo giá. Hãy mở báo giá hiện có hoặc tạo bản điều chỉnh.");
                }
                String ma = taoMaMoi(c, LocalDateTime.now());
                BaoGia moi = new BaoGia(ma, ma, 1, baoCao, chiTiet, hen, tk.getMaNhanVien(), LocalDateTime.now());
                themNhom(c, moi);
                themBaoGia(c, moi, token);
                c.commit();
                return moi;
            } catch (RuntimeException | SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(tuDong);
            }
        } catch (SQLException ex) {
            throw loi("Không lập được báo giá.", ex);
        }
    }

    public BaoGia suaNhap(TaiKhoan tk, BaoGia daDoc, List<ChiTietBaoGia> chiTiet,
                          LocalDateTime hen) {
        quyen(tk);
        if (daDoc == null) {
            throw new IllegalArgumentException("Cần bản báo giá đã đọc để kiểm tra thay đổi đồng thời.");
        }
        kiemTraDanhMuc(tk, chiTiet);
        Connection c = ketNoi();
        try {
            boolean tuDong = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                byte[] banKhoa = khoaBanNhap(c, daDoc.getMaBaoGia());
                BaoGia cu = docBaoGia(c, daDoc.getMaBaoGia());
                if (cu == null || cu.getTrangThai() != TrangThaiBaoGia.NHAP) {
                    throw new IllegalStateException("Bản đã gửi/duyệt chỉ đọc; cần tạo phiên bản mới.");
                }
                if (!cungNoiDung(cu, daDoc)) {
                    throw new IllegalStateException("Báo giá đã được người khác thay đổi; tải lại trước khi sửa.");
                }
                BaoGia moi = new BaoGia(cu.getMaBaoGia(), cu.getMaBaoGiaGoc(), cu.getPhienBan(),
                        cu.getBaoCao(), chiTiet, hen, cu.getNguoiLap(), cu.getNgayLap());
                capNhatNoiDung(c, moi, tk.getMaNhanVien(), banKhoa);
                c.commit();
                return moi;
            } catch (RuntimeException | SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(tuDong);
            }
        } catch (SQLException ex) {
            throw loi("Không cập nhật được báo giá.", ex);
        }
    }

    public BaoGia taoDieuChinh(TaiKhoan tk, String token, BaoGia coSo,
                               List<ChiTietBaoGia> chiTiet, LocalDateTime hen) {
        quyen(tk);
        kiemTraToken(token);
        kiemTraDanhMuc(tk, chiTiet);
        if (coSo == null) {
            throw new NullPointerException("Cần bản cơ sở.");
        }
        Connection c = ketNoi();
        try {
            boolean tuDong = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                BaoGia cu = docBaoGia(c, coSo.getMaBaoGia());
                BaoGia daLuu = timTheoToken(c, token);
                if (daLuu != null) {
                    kiemTraLap(daLuu, cu.getBaoCao(), chiTiet, hen, tk, cu);
                    c.commit();
                    return daLuu;
                }
                kiemTraMoiNhat(c, cu);
                if (cu.getTrangThai() == TrangThaiBaoGia.NHAP) {
                    throw new IllegalStateException("Bản hiện tại còn nháp; sửa bản nháp trước.");
                }
                int phienBan = cu.getPhienBan() + 1;
                String ma = cu.getMaBaoGiaGoc() + "-V" + phienBan;
                BaoGia moi = new BaoGia(ma, cu.getMaBaoGiaGoc(), phienBan, cu.getBaoCao(), chiTiet,
                        hen, tk.getMaNhanVien(), LocalDateTime.now());
                themBaoGia(c, moi, token);
                c.commit();
                return moi;
            } catch (RuntimeException | SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(tuDong);
            }
        } catch (SQLException ex) {
            throw loi("Không tạo được phiên bản điều chỉnh.", ex);
        }
    }

    public BaoGia guiKhach(TaiKhoan tk, BaoGia daDoc) {
        quyen(tk);
        BaoGia cu = timTheoMa(tk, daDoc == null ? null : daDoc.getMaBaoGia());
        kiemTraMoiNhat(ketNoi(), cu);
        BaoGia moi = cu.guiKhach(tk.getMaNhanVien(), LocalDateTime.now());
        capNhatTrangThai(moi);
        return moi;
    }

    public BaoGia ghiNhanPhanHoi(TaiKhoan tk, BaoGia daDoc, TrangThaiBaoGia quyetDinh,
                                 LocalDateTime lucPhanHoi, String kenh, String ghiChu) {
        quyen(tk);
        BaoGia cu = timTheoMa(tk, daDoc == null ? null : daDoc.getMaBaoGia());
        kiemTraMoiNhat(ketNoi(), cu);
        BaoGia moi = cu.ghiNhanPhanHoi(quyetDinh, tk.getMaNhanVien(), lucPhanHoi,
                LocalDateTime.now(), kenh, ghiChu);
        capNhatTrangThai(moi);
        return moi;
    }

    public BaoGia timTheoMa(TaiKhoan tk, String ma) {
        quyen(tk);
        BaoGia baoGia = timNoiBo(ma);
        if (baoGia == null) {
            throw new IllegalArgumentException("Báo giá không tồn tại.");
        }
        return baoGia;
    }

    public List<BaoGia> timKiem(TaiKhoan tk, String tuKhoa, TrangThaiBaoGia trangThai) {
        quyen(tk);
        if (tuKhoa == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        String khoa = tuKhoa.trim().toLowerCase(Locale.ROOT);
        String bien = Xe.khoaBienSo(khoa);
        List<BaoGia> ketQua = new ArrayList<>();
        String sql = "SELECT * FROM dbo.BaoGia ORDER BY NgayLap DESC, MaBaoGia DESC";
        try (PreparedStatement ps = ketNoi().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                BaoGia baoGia = docBaoGia(ketNoi(), rs);
                String chu = (baoGia.getMaBaoGia() + " " + baoGia.getBienSo() + " "
                        + baoGia.getBaoCao().getPhieu().getTenKhach()).toLowerCase(Locale.ROOT);
                if ((trangThai == null || baoGia.getTrangThai() == trangThai)
                        && (chu.contains(khoa) || (!bien.isEmpty()
                        && Xe.khoaBienSo(baoGia.getBienSo()).contains(bien)))) {
                    ketQua.add(baoGia);
                }
            }
        } catch (SQLException ex) {
            throw loi("Không đọc được danh sách báo giá.", ex);
        }
        return ketQua;
    }

    public List<BaoGia> layDaDuyet(TaiKhoan tk) {
        quyen(tk);
        String sql = "SELECT b.* FROM dbo.BaoGia b WHERE b.TrangThai='DA_DONG_Y' AND NOT EXISTS "
                + "(SELECT 1 FROM dbo.BaoGia m WHERE m.MaBaoGiaGoc=b.MaBaoGiaGoc AND m.PhienBan>b.PhienBan)";
        return docDanhSach(sql);
    }

    public List<DichVu> layDichVu(TaiKhoan tk) {
        quyen(tk);
        return dichVuDAO.layChoBaoGia(tk);
    }

    public boolean daSuDungDichVu(String ma) {
        String sql = "SELECT 1 FROM dbo.ChiTietBaoGia WHERE MaDichVu=?";
        try (PreparedStatement ps = ketNoi().prepareStatement(sql)) {
            ps.setString(1, ma);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw loi("Không kiểm tra được dịch vụ.", ex);
        }
    }

    public List<BaoGia> layBaoGiaBaoCao() {
        return docDanhSach("SELECT * FROM dbo.BaoGia ORDER BY NgayLap DESC");
    }

    public BaoGia timNoiBo(String ma) {
        if (ma == null || ma.isBlank()) return null;
        try {
            return docBaoGia(ketNoi(), ma);
        } catch (SQLException ex) {
            throw loi("Không đọc được báo giá.", ex);
        }
    }

    private void kiemTraDanhMuc(TaiKhoan tk, List<ChiTietBaoGia> chiTiet) {
        if (chiTiet == null || chiTiet.isEmpty()) throw new IllegalArgumentException("Cần nhập hạng mục báo giá.");
        for (ChiTietBaoGia dong : chiTiet) {
            if (dong == null || (!"Dịch vụ".equals(dong.getLoai()) && !"Phụ tùng".equals(dong.getLoai()))) {
                throw new IllegalArgumentException("Hạng mục báo giá không hợp lệ.");
            }
            if ("Dịch vụ".equals(dong.getLoai()) && dong.getMaDichVu().isEmpty()) {
                throw new IllegalArgumentException("Cần chọn dịch vụ cho hạng mục.");
            }
            if ("Phụ tùng".equals(dong.getLoai()) && dong.getMaPhuTung().isEmpty()) {
                throw new IllegalArgumentException("Cần chọn phụ tùng cho hạng mục.");
            }
        }
    }

    private BaoCaoKiemTra canCu(TaiKhoan tk, String ma) {
        for (BaoCaoKiemTra baoCao : nguonBaoCao.layHoanTatNoiBo()) {
            if (baoCao.getMaBaoCao().equals(ma)) return baoCao;
        }
        throw new IllegalArgumentException("Báo cáo chưa hoàn tất hoặc chưa liên kết phiếu tiếp nhận.");
    }

    private void themNhom(Connection c, BaoGia baoGia) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("INSERT dbo.NhomBaoGia(MaBaoGiaGoc,MaBaoCao) VALUES(?,?)")) {
            ps.setString(1, baoGia.getMaBaoGiaGoc());
            ps.setString(2, baoGia.getBaoCao().getMaBaoCao());
            ps.executeUpdate();
        }
    }

    private void themBaoGia(Connection c, BaoGia baoGia, String token) throws SQLException {
        String sql = "INSERT dbo.BaoGia(MaBaoGia,MaBaoGiaGoc,PhienBan,BienSo,NgayHen,NgayLap,NguoiLap,TrangThai,MaThaoTac) VALUES(?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, baoGia.getMaBaoGia());
            ps.setString(2, baoGia.getMaBaoGiaGoc());
            ps.setInt(3, baoGia.getPhienBan());
            ps.setString(4, baoGia.getBienSo());
            ps.setTimestamp(5, Timestamp.valueOf(baoGia.getNgayHen()));
            ps.setTimestamp(6, Timestamp.valueOf(baoGia.getNgayLap()));
            ps.setString(7, baoGia.getNguoiLap());
            ps.setString(8, baoGia.getTrangThai().name());
            ps.setString(9, token);
            ps.executeUpdate();
        }
        themChiTiet(c, baoGia);
    }

    private byte[] khoaBanNhap(Connection c, String maBaoGia) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT PhienBanKhoa FROM dbo.BaoGia WITH (UPDLOCK,HOLDLOCK) WHERE MaBaoGia=?")) {
            ps.setString(1, maBaoGia);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Báo giá không tồn tại.");
                return rs.getBytes(1);
            }
        }
    }

    private void capNhatNoiDung(Connection c, BaoGia baoGia, String nguoiSua, byte[] banKhoa)
            throws SQLException {
        String sql = "EXEC dbo.usp_SuaBaoGiaNhap @MaBaoGia=?,@NguoiSua=?,@BanDaDoc=?,@NgayHen=?,@ChiTiet=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, baoGia.getMaBaoGia());
            ps.setString(2, nguoiSua);
            ps.setBytes(3, banKhoa);
            ps.setTimestamp(4, Timestamp.valueOf(baoGia.getNgayHen()));
            ps.setString(5, jsonChiTiet(baoGia.getChiTiet()));
            ps.execute();
        }
    }

    private String jsonChiTiet(List<ChiTietBaoGia> chiTiet) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < chiTiet.size(); i++) {
            ChiTietBaoGia d = chiTiet.get(i);
            if (i > 0) json.append(',');
            json.append('{')
                    .append("\"MaDichVu\":").append(jsonChu(d.getMaDichVu())).append(',')
                    .append("\"MaPhuTung\":").append(jsonChu(d.getMaPhuTung())).append(',')
                    .append("\"TenHangMuc\":").append(jsonChu(d.getTenHangMuc())).append(',')
                    .append("\"Loai\":").append(jsonChu(d.getLoai())).append(',')
                    .append("\"DonVi\":").append(jsonChu(d.getDonVi())).append(',')
                    .append("\"SoLuong\":").append(Double.toString(d.getSoLuong())).append(',')
                    .append("\"DonGia\":").append(d.getDonGia()).append('}');
        }
        return json.append(']').toString();
    }

    private String jsonChu(String giaTri) {
        if (giaTri == null || giaTri.isEmpty()) return "null";
        return "\"" + giaTri.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n").replace("\t", "\\t") + "\"";
    }

    private boolean cungNoiDung(BaoGia hienTai, BaoGia daDoc) {
        if (!hienTai.getMaBaoGia().equals(daDoc.getMaBaoGia())
                || hienTai.getTrangThai() != daDoc.getTrangThai()
                || !hienTai.getBaoCao().getMaBaoCao().equals(daDoc.getBaoCao().getMaBaoCao())
                || !hienTai.getNgayHen().equals(daDoc.getNgayHen())
                || hienTai.getChiTiet().size() != daDoc.getChiTiet().size()) return false;
        for (int i = 0; i < hienTai.getChiTiet().size(); i++) {
            ChiTietBaoGia a = hienTai.getChiTiet().get(i), b = daDoc.getChiTiet().get(i);
            if (!a.getMaDichVu().equals(b.getMaDichVu()) || !a.getMaPhuTung().equals(b.getMaPhuTung())
                    || !a.getTenHangMuc().equals(b.getTenHangMuc()) || !a.getLoai().equals(b.getLoai())
                    || !a.getDonVi().equals(b.getDonVi()) || Double.compare(a.getSoLuong(), b.getSoLuong()) != 0
                    || a.getDonGia() != b.getDonGia()) return false;
        }
        return true;
    }

    private void themChiTiet(Connection c, BaoGia baoGia) throws SQLException {
        String sql = "INSERT dbo.ChiTietBaoGia(MaBaoGia,ViTri,MaDichVu,MaPhuTung,TenHangMuc,Loai,DonVi,SoLuong,DonGia) VALUES(?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            int viTri = 0;
            for (ChiTietBaoGia dong : baoGia.getChiTiet()) {
                ps.setString(1, baoGia.getMaBaoGia());
                ps.setInt(2, viTri++);
                ps.setString(3, dong.getMaDichVu().isEmpty() ? null : dong.getMaDichVu());
                ps.setString(4, dong.getMaPhuTung().isEmpty() ? null : dong.getMaPhuTung());
                ps.setString(5, dong.getTenHangMuc());
                ps.setString(6, dong.getLoai());
                ps.setString(7, dong.getDonVi());
                ps.setDouble(8, dong.getSoLuong());
                ps.setLong(9, dong.getDonGia());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void capNhatTrangThai(BaoGia baoGia) {
        String sql = "UPDATE dbo.BaoGia SET TrangThai=?,NgayGui=?,NguoiGui=?,ThoiDiemPhanHoi=?,ThoiDiemGhiNhan=?,NguoiGhiNhan=?,KenhPhanHoi=?,GhiChuPhanHoi=? WHERE MaBaoGia=?";
        try (PreparedStatement ps = ketNoi().prepareStatement(sql)) {
            ps.setString(1, baoGia.getTrangThai().name());
            datThoiGian(ps, 2, baoGia.getNgayGui());
            ps.setString(3, rongThanhNull(baoGia.getNguoiGui()));
            datThoiGian(ps, 4, baoGia.getThoiDiemPhanHoi());
            datThoiGian(ps, 5, baoGia.getThoiDiemGhiNhan());
            ps.setString(6, rongThanhNull(baoGia.getNguoiGhiNhan()));
            ps.setString(7, baoGia.getKenhPhanHoi());
            ps.setString(8, baoGia.getGhiChuPhanHoi());
            ps.setString(9, baoGia.getMaBaoGia());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw loi("Không cập nhật được trạng thái báo giá.", ex);
        }
    }

    private BaoGia docBaoGia(Connection c, String ma) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM dbo.BaoGia WHERE MaBaoGia=?")) {
            ps.setString(1, ma);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? docBaoGia(c, rs) : null;
            }
        }
    }

    private BaoGia docBaoGia(Connection c, ResultSet rs) throws SQLException {
        BaoCaoKiemTra baoCao;
        try (PreparedStatement ps = c.prepareStatement("SELECT k.* FROM dbo.NhomBaoGia n JOIN dbo.BaoCaoKiemTra k ON k.MaBaoCao=n.MaBaoCao WHERE n.MaBaoGiaGoc=?")) {
            ps.setString(1, rs.getString("MaBaoGiaGoc"));
            try (ResultSet ketQua = ps.executeQuery()) {
                if (!ketQua.next()) throw new IllegalStateException("Báo giá thiếu báo cáo kiểm tra.");
                baoCao = BaoCaoKiemTraDAO.docBaoCao(c, ketQua);
            }
        }
        List<ChiTietBaoGia> chiTiet = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM dbo.ChiTietBaoGia WHERE MaBaoGia=? ORDER BY ViTri")) {
            ps.setString(1, rs.getString("MaBaoGia"));
            try (ResultSet ketQua = ps.executeQuery()) {
                while (ketQua.next())
                    chiTiet.add(new ChiTietBaoGia(ketQua.getString("MaDichVu") == null ? "" : ketQua.getString("MaDichVu"), ketQua.getString("TenHangMuc"), ketQua.getString("Loai"), ketQua.getString("DonVi"), ketQua.getDouble("SoLuong"), ketQua.getLong("DonGia"), ketQua.getString("MaPhuTung") == null ? "" : ketQua.getString("MaPhuTung")));
            }
        }
        BaoGia baoGia = new BaoGia(rs.getString("MaBaoGia"), rs.getString("MaBaoGiaGoc"), rs.getInt("PhienBan"), baoCao, chiTiet, rs.getTimestamp("NgayHen").toLocalDateTime(), rs.getString("NguoiLap"), rs.getTimestamp("NgayLap").toLocalDateTime());
        TrangThaiBaoGia trangThai = TrangThaiBaoGia.valueOf(rs.getString("TrangThai"));
        if (trangThai != TrangThaiBaoGia.NHAP) {
            baoGia = baoGia.guiKhach(rs.getString("NguoiGui"), rs.getTimestamp("NgayGui").toLocalDateTime());
            if (trangThai != TrangThaiBaoGia.CHO_XAC_NHAN)
                baoGia = baoGia.ghiNhanPhanHoi(trangThai, rs.getString("NguoiGhiNhan"), rs.getTimestamp("ThoiDiemPhanHoi").toLocalDateTime(), rs.getTimestamp("ThoiDiemGhiNhan").toLocalDateTime(), rs.getString("KenhPhanHoi"), rs.getString("GhiChuPhanHoi"));
        }
        return baoGia;
    }

    private List<BaoGia> docDanhSach(String sql) {
        List<BaoGia> ds = new ArrayList<>();
        Connection c = ketNoi();
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ds.add(docBaoGia(c, rs));
        } catch (SQLException ex) {
            throw loi("Không đọc được danh sách báo giá.", ex);
        }
        return ds;
    }

    private BaoGia timTheoToken(Connection c, String token) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT MaBaoGia FROM dbo.BaoGia WITH (UPDLOCK,HOLDLOCK) WHERE MaThaoTac=?")) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? docBaoGia(c, rs.getString(1)) : null;
            }
        }
    }

    private boolean chuaCoNhom(String maBaoCao) {
        try {
            return chuaCoNhom(ketNoi(), maBaoCao);
        } catch (SQLException ex) {
            throw loi("Không kiểm tra được báo giá.", ex);
        }
    }

    private boolean chuaCoNhom(Connection c, String maBaoCao) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM dbo.NhomBaoGia WITH (UPDLOCK,HOLDLOCK) WHERE MaBaoCao=?")) {
            ps.setString(1, maBaoCao);
            try (ResultSet rs = ps.executeQuery()) {
                return !rs.next();
            }
        }
    }

    private void kiemTraMoiNhat(Connection c, BaoGia baoGia) {
        try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM dbo.BaoGia WHERE MaBaoGiaGoc=? AND PhienBan>?")) {
            ps.setString(1, baoGia.getMaBaoGiaGoc());
            ps.setInt(2, baoGia.getPhienBan());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    throw new IllegalStateException("Đã có phiên bản mới hơn. Hãy mở đúng phiên bản hiện hành.");
            }
        } catch (SQLException ex) {
            throw loi("Không kiểm tra được phiên bản báo giá.", ex);
        }
    }

    private String taoMaMoi(Connection c, LocalDateTime luc) throws SQLException {
        String prefix = "BG-" + luc.getYear() + "-";
        try (PreparedStatement ps = c.prepareStatement("SELECT COALESCE(MAX(TRY_CONVERT(int,RIGHT(MaBaoGiaGoc,4))),0)+1 FROM dbo.NhomBaoGia WITH (UPDLOCK,HOLDLOCK) WHERE MaBaoGiaGoc LIKE ?")) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return prefix + String.format(Locale.ROOT, "%04d", rs.getInt(1));
            }
        }
    }

    private void kiemTraLap(BaoGia cu, BaoCaoKiemTra bc, List<ChiTietBaoGia> ct, LocalDateTime hen, TaiKhoan tk, BaoGia coSo) {
        if ((coSo == null && cu.getPhienBan() != 1) || (coSo != null && (!cu.getMaBaoGiaGoc().equals(coSo.getMaBaoGiaGoc()) || cu.getPhienBan() != coSo.getPhienBan() + 1)))
            throw new IllegalArgumentException("Mã thao tác thuộc bản báo giá khác.");
        BaoGia noiDung = new BaoGia(cu.getMaBaoGia(), cu.getMaBaoGiaGoc(), cu.getPhienBan(), bc, ct, hen, tk.getMaNhanVien(), cu.getNgayLap());
        if (!cungNoiDung(cu, noiDung)) throw new IllegalArgumentException("Mã thao tác đã lưu nội dung khác.");
    }

    private void kiemTraToken(String token) {
        if (token == null || token.isBlank()) throw new IllegalArgumentException("Thiếu mã thao tác.");
    }

    private void quyen(TaiKhoan tk) {
        if (tk == null || tk.getVaiTro() != VaiTro.NHAN_VIEN_TIEP_NHAN)
            throw new SecurityException("Chỉ tiếp nhận được quản lý báo giá.");
        tk.kiemTraPhien();
    }

    private void datThoiGian(PreparedStatement ps, int viTri, LocalDateTime luc) throws SQLException {
        if (luc == null) ps.setNull(viTri, java.sql.Types.TIMESTAMP);
        else ps.setTimestamp(viTri, Timestamp.valueOf(luc));
    }

    private String rongThanhNull(String chu) {
        return chu == null || chu.isBlank() ? null : chu;
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
}
