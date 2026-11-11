package dao;

import dao.HoaDonDAO;
import dao.PhieuSuaChuaDAO;
import connectDB.ConnectDB;
import entity.BanGiaoXe;
import entity.BanGiaoXe.CanCuBanGiao;
import entity.BanGiaoXe.TrangThaiBanGiao;
import entity.BaoGia;
import entity.Xe;
import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;
import entity.PhieuSuaChua.ChiTietSuaChua;
import entity.PhieuSuaChua;
import entity.PhieuSuaChua.TrangThaiSuaChua;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** Đọc căn cứ và ghi nhận bàn giao xe cho vai trò tiếp nhận. */
public final class BanGiaoXeDAO {
    private final PhieuSuaChuaDAO suaChua;


    /**
     * Khởi tạo dữ liệu và thành phần cần dùng của lớp.
     */
    public BanGiaoXeDAO(PhieuSuaChuaDAO suaChua, HoaDonDAO nguonCanCu) {
        if (suaChua == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.suaChua = suaChua;
        if (nguonCanCu == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
    }

    /**
     * Đọc lenh từ dữ liệu hiện tại.
     */
    public PhieuSuaChua docLenh(TaiKhoan tk, String ma) {
        quyen(tk);
        PhieuSuaChua ketQuaTraCuu1 = suaChua.timTheoMa(tk, ma);
        if (ketQuaTraCuu1 == null) {
            throw new IllegalArgumentException("Lệnh sửa chữa không tồn tại.");
        }
        return ketQuaTraCuu1;
    }

    /**
     * Đọc bao gia từ dữ liệu hiện tại.
     */
    public BaoGia docBaoGia(TaiKhoan tk, String ma) {
        docLenh(tk, ma);
        return suaChua.layBaoGia(tk, ma);
    }

    /**
     * Đọc can cu từ dữ liệu hiện tại.
     */
    public CanCuBanGiao docCanCu(TaiKhoan tk, String ma) {
        docLenh(tk, ma);
        return docCanCuMoRong(ma);
    }

    /** Lấy giờ garage cùng độ chính xác với thời điểm lưu trên phiếu bàn giao. */
    public LocalDateTime docGioGarage(TaiKhoan tk) {
        quyen(tk);
        String sql = "SELECT dbo.fn_ThoiDiemGarage()";
        try (PreparedStatement ps = ketNoi().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getTimestamp(1).toLocalDateTime();
            }
            throw new IllegalStateException("Không đọc được giờ garage.");
        } catch (SQLException ex) {
            throw loi("Không đọc được giờ garage.", ex);
        }
    }

    /**
     * Thực hiện tim theo lenh cho thành phần hiện tại.
     */
    public BanGiaoXe timTheoLenh(TaiKhoan tk, String ma) {
        quyen(tk);
        return timGiaoNoiBo(ma);
    }

    /**
     * Thực hiện ly do chua du cho thành phần hiện tại.
     */
    public String lyDoChuaDu(TaiKhoan tk, String ma) {
        PhieuSuaChua lenh = docLenh(tk, ma);
        return lyDo(lenh, docBaoGia(tk, ma), docCanCu(tk, ma));
    }

    /**
     * Thực hiện ly do cho thành phần hiện tại.
     */
    private String lyDo(PhieuSuaChua lenh, BaoGia bg, CanCuBanGiao canCu) {
        if (lenh.getTrangThai() != TrangThaiSuaChua.HOAN_THANH || lenh.getThoiDiemHoanTat() == null) {
            return "Xe chưa hoàn thành sửa chữa.";
        }
        for (ChiTietSuaChua hm : lenh.getDanhSachHangMuc()) {
            if (hm.getTrangThai() != TrangThaiSuaChua.HOAN_THANH || hm.getKetQua().isBlank()) {
                return "Còn hạng mục chưa hoàn thành.";
            }
        }
        if (!bg.isDaDongY()) {
            return "Thiếu báo giá được khách đồng ý của lệnh.";
        }
        if (bg.getBaoCao() == null) {
            return "Thiếu liên kết phiếu tiếp nhận của lệnh.";
        }
        if (canCu == null) {
            return lyDoCanCu(lenh.getMaPhieu());
        }
        if (!canCu.getMaLenh().equals(lenh.getMaPhieu())) {
            return "Căn cứ thanh toán/phê duyệt không thuộc lệnh đang giao.";
        }
        if (canCu.getThoiDiem().isAfter(LocalDateTime.now())) {
            return "Thời điểm xác nhận căn cứ không hợp lệ.";
        }
        if (!canCu.duDieuKien()) {
            return "Khách chưa thanh toán đủ và chưa có phê duyệt ngoại lệ.";
        }
        return "";
    }

    /**
     * Thực hiện trang thai cho thành phần hiện tại.
     */
    public TrangThaiBanGiao trangThai(TaiKhoan tk, String ma) {
        quyen(tk);
        if ((timGiaoNoiBo(ma) != null)) {
            return TrangThaiBanGiao.DA_GIAO;
        }
        return lyDoChuaDu(tk, ma).isEmpty() ? TrangThaiBanGiao.CHO_GIAO : TrangThaiBanGiao.CHO_CAN_CU;
    }

    /**
     * Chỉ ghi một lần cho lệnh; lặp đúng cùng thao tác trả chứng từ cũ và giữ thời điểm cũ.
     */
    public BanGiaoXe xacNhan(TaiKhoan tk, String token, String maLenh, CanCuBanGiao daDoc, String nguoiNhan,
                             LocalDateTime lucGiao, String tinhTrang, String taiSan, String ghiChu, String hinhThuc,
                             boolean khachXacNhan) {
        quyen(tk);
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Thiếu mã thao tác bàn giao.");
        }
        String daLuu = timLenhTheoLuot(token);
        if (daLuu != null) {
            BanGiaoXe cu = timGiaoNoiBo(daLuu);
            if (!cungThaoTac(cu, tk, maLenh, daDoc, nguoiNhan, lucGiao, tinhTrang, taiSan, ghiChu, hinhThuc,
                    khachXacNhan)) {
                throw new IllegalArgumentException("Mã thao tác đã lưu một nội dung bàn giao khác.");
            }
            return cu;
        }
        if ((timGiaoNoiBo(maLenh) != null)) {
            throw new IllegalStateException("Lệnh đã bàn giao; mở lịch sử để xem, không ghi lần nữa.");
        }
        PhieuSuaChua lenh = docLenh(tk, maLenh);
        BaoGia bg = docBaoGia(tk, maLenh);
        CanCuBanGiao hienTai = docCanCu(tk, maLenh);
        String lyDo = lyDo(lenh, bg, hienTai);
        if (!lyDo.isEmpty()) {
            throw new IllegalStateException(lyDo);
        }
        if (!hienTai.cungNoiDung(daDoc)) {
            throw new IllegalStateException("Thanh toán/phê duyệt đã thay đổi. Mở lại lệnh để đối chiếu.");
        }
        // Dựng thử entity trước để áp dụng đầy đủ kiểm tra UI/domain; SQL mới là nơi khóa và ghi quyết định cuối.
        new BanGiaoXe("GX-TAM", lenh, bg, hienTai, tk, nguoiNhan, lucGiao, tinhTrang, taiSan, ghiChu,
                hinhThuc, khachXacNhan, LocalDateTime.now());
        luuBanGiaoBangThuTuc(tk, token, maLenh, hienTai, nguoiNhan, lucGiao, tinhTrang, taiSan, ghiChu,
                hinhThuc);
        BanGiaoXe daGhi = timGiaoNoiBo(maLenh);
        if (daGhi == null) {
            throw new IllegalStateException("Không đọc lại được chứng từ bàn giao vừa lưu.");
        }
        return daGhi;
    }

    /**
     * Với xe chưa giao, bộ lọc ngày dùng ngày hoàn tất sửa chữa; xe đã giao dùng ngày giao thực tế.
     */
    public List<PhieuSuaChua> timKiem(TaiKhoan tk, String tuKhoa, TrangThaiBanGiao trangThai, LocalDate tu,
                                      LocalDate den) {
        quyen(tk);
        if (tu != null && den != null && tu.isAfter(den)) {
            throw new IllegalArgumentException("Từ ngày không được sau đến ngày.");
        }
        if (tuKhoa == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        String khoa = tuKhoa.trim().toLowerCase();
        String bien = Xe.khoaBienSo(khoa);
        List<PhieuSuaChua> ketQua = new ArrayList<>();
        for (PhieuSuaChua lenh : suaChua.timKiem(tk, "", "Hoàn thành")) {
            BanGiaoXe giao = timGiaoNoiBo(lenh.getMaPhieu());
            LocalDate ngay = giao == null
                    ? lenh.getThoiDiemHoanTat() == null ? null : lenh.getThoiDiemHoanTat().toLocalDate()
                    : giao.getThoiDiemGiao().toLocalDate();
            if (ngay == null || (tu != null && ngay.isBefore(tu)) || (den != null && ngay.isAfter(den))) {
                continue;
            }
            BaoGia bg = docBaoGia(tk, lenh.getMaPhieu());
            String chu = lenh.getMaPhieu() + " " + lenh.getBienSo() + " "
                    + (bg.getBaoCao() == null ? "" : bg.getBaoCao().getPhieu().getTenKhach());
            if (giao != null) {
                chu += " " + giao.getMaBanGiao() + " " + giao.getNguoiNhan() + " " + giao.getNguoiGiao();
            }
            if (trangThai != null && trangThai(tk, lenh.getMaPhieu()) != trangThai) {
                continue;
            }
            if (chu.toLowerCase().contains(khoa)
                    || (!bien.isEmpty() && Xe.chuanHoaBienSo(lenh.getBienSo()).contains(bien))) {
                ketQua.add(lenh);
            }
        }
        return ketQua;
    }

    /**
     * Thực hiện lich su theo xe cho thành phần hiện tại.
     */
    public List<BanGiaoXe> lichSuTheoXe(TaiKhoan tk, String maXe) {
        quyen(tk);
        List<BanGiaoXe> ds = new ArrayList<>();
        String sql = "SELECT MaLenh FROM dbo.BanGiaoXe bg JOIN dbo.PhieuSuaChua sc ON sc.MaPhieu=bg.MaLenh "
                + "JOIN dbo.BaoGia b ON b.MaBaoGia=sc.MaBaoGia JOIN dbo.NhomBaoGia n ON n.MaBaoGiaGoc=b.MaBaoGiaGoc "
                + "JOIN dbo.BaoCaoKiemTra bc ON bc.MaBaoCao=n.MaBaoCao JOIN dbo.PhieuTiepNhan tn ON tn.MaPhieu=bc.MaPhieuTiepNhan WHERE tn.MaXe=?";
        try (PreparedStatement ps = ketNoi().prepareStatement(sql)) {
            ps.setString(1, maXe);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BanGiaoXe giao = timGiaoNoiBo(rs.getString(1));
                    if (giao != null) ds.add(giao);
                }
            }
        } catch (SQLException ex) {
            throw loi("Không đọc được lịch sử bàn giao.", ex);
        }
        return ds;
    }

    /**
     * Thực hiện cung thao tac cho thành phần hiện tại.
     */
    private boolean cungThaoTac(BanGiaoXe g, TaiKhoan tk, String ma, CanCuBanGiao canCu, String nhan, LocalDateTime luc,
                                String tinhTrang, String taiSan, String ghiChu, String hinhThuc, boolean xacNhan) {
        return g != null && xacNhan && g.getMaLenh().equals(ma) && g.getNguoiGiao().equals(tk.getTenDangNhap())
                && g.getCanCu().cungNoiDung(canCu) && g.getNguoiNhan().equals(chu(nhan))
                && g.getThoiDiemGiao().equals(luc) && g.getTinhTrang().equals(chu(tinhTrang))
                && g.getTaiSanDaTra().equals(chu(taiSan)) && g.getGhiChu().equals(chu(ghiChu))
                && g.getHinhThucXacNhan().equals(chu(hinhThuc));
    }

    /**
     * Thực hiện chu cho thành phần hiện tại.
     */
    private String chu(String s) {
        return s == null ? "" : s.trim();
    }

    /**
     * Thực hiện quyen cho thành phần hiện tại.
     */
    private void quyen(TaiKhoan tk) {
        if (tk == null || tk.getVaiTro() != VaiTro.NHAN_VIEN_TIEP_NHAN) {
            throw new SecurityException("Chức năng bàn giao dành cho tiếp nhận.");
        }
    }

    /**
     * Tìm bản bàn giao đã lưu của một lệnh.
     */
    public BanGiaoXe timGiaoNoiBo(String maLenh) {
        if (maLenh == null || maLenh.isBlank()) return null;
        String sql = "SELECT * FROM dbo.BanGiaoXe WHERE MaLenh=?";
        try (PreparedStatement ps = ketNoi().prepareStatement(sql)) {
            ps.setString(1, maLenh);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? docBanGiao(rs) : null;
            }
        } catch (SQLException ex) {
            throw loi("Không đọc được bàn giao xe.", ex);
        }
    }

    /**
     * Tìm chứng từ đã lưu theo mã thao tác để tránh ghi lặp.
     */
    private String timLenhTheoLuot(String maThaoTac) {
        String sql = "SELECT MaLenh FROM dbo.BanGiaoXe WHERE MaThaoTac=?";
        try (PreparedStatement ps = ketNoi().prepareStatement(sql)) {
            ps.setString(1, maThaoTac);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        } catch (SQLException ex) {
            throw loi("Không kiểm tra được mã thao tác.", ex);
        }
    }

    /**
     * Ghi bàn giao cùng mã thao tác để lần bấm lặp chỉ đọc lại chứng từ cũ.
     */
    private void luuBanGiaoBangThuTuc(TaiKhoan tk, String token, String maLenh, CanCuBanGiao canCu,
            String nguoiNhan, LocalDateTime lucGiao, String tinhTrang, String taiSan, String ghiChu,
            String hinhThuc) {
        String sql = "EXEC dbo.usp_BanGiaoXe @MaLenh=?,@NguoiGiao=?,@NguoiNhan=?,@ThoiDiemGiao=?,"
                + "@TinhTrang=?,@TaiSanDaTra=?,@HinhThucXacNhan=?,@GhiChu=?,@MaThaoTac=?,"
                + "@MaPheDuyetNgoaiLe=?";
        try (PreparedStatement ps = ketNoi().prepareStatement(sql)) {
            ps.setString(1, maLenh);
            ps.setString(2, tk.getMaNhanVien());
            ps.setString(3, chu(nguoiNhan));
            ps.setTimestamp(4, Timestamp.valueOf(lucGiao));
            ps.setString(5, chu(tinhTrang));
            ps.setString(6, chu(taiSan));
            ps.setString(7, chu(hinhThuc));
            ps.setString(8, chu(ghiChu));
            ps.setString(9, token);
            if (canCu.isNgoaiLe()) {
                ps.setLong(10, maPheDuyet(canCu));
            } else {
                ps.setNull(10, java.sql.Types.BIGINT);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || rs.getString("MaBanGiao") == null) {
                    throw new IllegalStateException("SQL không trả về mã bàn giao.");
                }
            }
        } catch (SQLException ex) {
            throw loi("Không lưu được bàn giao xe.", ex);
        }
    }

    /**
     * Khôi phục bàn giao từ dữ liệu SQL và các chứng từ liên quan hiện có.
     */
    private BanGiaoXe docBanGiao(ResultSet rs) throws SQLException {
        String maLenh = rs.getString("MaLenh");
        TaiKhoan nguoiGiao = docTaiKhoan(rs.getString("NguoiGiao"));
        PhieuSuaChua lenh = suaChua.timTheoMa(nguoiGiao, maLenh);
        BaoGia baoGia = suaChua.layBaoGia(nguoiGiao, maLenh);
        CanCuBanGiao canCu = rs.getObject("MaPheDuyetNgoaiLe") == null
                ? docCanCuHoaDon(rs.getString("MaHoaDon"))
                : docCanCuPheDuyet(rs.getLong("MaPheDuyetNgoaiLe"));
        return new BanGiaoXe(rs.getString("MaBanGiao"), lenh, baoGia, canCu,
                nguoiGiao, rs.getString("NguoiNhan"),
                rs.getTimestamp("ThoiDiemGiao").toLocalDateTime(), rs.getString("TinhTrang"), rs.getString("TaiSanDaTra"),
                rs.getString("GhiChu"), rs.getString("HinhThucXacNhan"), true,
                rs.getTimestamp("ThoiDiemGhiNhan").toLocalDateTime());
    }

    /** Đọc căn cứ mới nhất từ view mở rộng, gồm hóa đơn đủ tiền hoặc phê duyệt ngoại lệ còn hạn. */
    private CanCuBanGiao docCanCuMoRong(String maLenh) {
        if (maLenh == null || maLenh.isBlank()) return null;
        String sql = "SELECT MaLenh,MaChungTu,PhaiThu,DaThu,NguoiXacNhan,ThoiDiem,NgoaiLe,LyDo "
                + "FROM dbo.vw_CanCuBanGiaoMoRong WHERE MaLenh=? ORDER BY NgoaiLe";
        try (PreparedStatement ps = ketNoi().prepareStatement(sql)) {
            ps.setString(1, maLenh.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? docCanCu(rs) : null;
            }
        } catch (SQLException ex) {
            throw loi("Không đọc được căn cứ bàn giao.", ex);
        }
    }

    /** Giải thích ngắn gọn khi lệnh chưa có căn cứ bàn giao hợp lệ. */
    private String lyDoCanCu(String maLenh) {
        String sql = "SELECT TOP 1 TrangThai,ThoiHan FROM dbo.PheDuyetBanGiaoNgoaiLe "
                + "WHERE MaLenh=? ORDER BY MaPheDuyet DESC";
        try (PreparedStatement ps = ketNoi().prepareStatement(sql)) {
            ps.setString(1, maLenh);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return "Chưa có xác nhận thanh toán/phê duyệt từ nguồn phụ trách.";
                String trangThai = rs.getString("TrangThai");
                Timestamp thoiHan = rs.getTimestamp("ThoiHan");
                if ("CHO_DUYET".equals(trangThai)) return "Phê duyệt ngoại lệ đang chờ quản lý duyệt.";
                if ("TU_CHOI".equals(trangThai)) return "Phê duyệt ngoại lệ đã bị từ chối.";
                if ("HET_HAN".equals(trangThai)
                        || (thoiHan != null && thoiHan.toLocalDateTime().isBefore(LocalDateTime.now()))) {
                    return "Phê duyệt ngoại lệ đã hết hạn.";
                }
            }
        } catch (SQLException ex) {
            throw loi("Không kiểm tra được phê duyệt ngoại lệ.", ex);
        }
        return "Chưa có xác nhận thanh toán/phê duyệt từ nguồn phụ trách.";
    }

    private CanCuBanGiao docCanCuHoaDon(String maHoaDon) {
        String sql = "SELECT hd.MaPhieuSuaChua AS MaLenh,hd.MaHoaDon AS MaChungTu,hd.TongTien AS PhaiThu,"
                + "hd.TongTien AS DaThu,hd.NguoiThucHien AS NguoiXacNhan,hd.ThoiDiemThanhToan AS ThoiDiem,"
                + "CONVERT(bit,0) AS NgoaiLe,CONVERT(nvarchar(500),'') AS LyDo FROM dbo.HoaDon hd WHERE hd.MaHoaDon=?";
        return docCanCuTheoKhoa(sql, maHoaDon, "Hóa đơn bàn giao không còn tồn tại.");
    }

    private CanCuBanGiao docCanCuPheDuyet(long maPheDuyet) {
        String sql = "SELECT MaLenh,CONCAT('NGOAI_LE-',MaPheDuyet) AS MaChungTu,SoTienConNo AS PhaiThu,"
                + "CONVERT(bigint,0) AS DaThu,NguoiPheDuyet AS NguoiXacNhan,ThoiDiemPheDuyet AS ThoiDiem,"
                + "CONVERT(bit,1) AS NgoaiLe,LyDo FROM dbo.PheDuyetBanGiaoNgoaiLe WHERE MaPheDuyet=?";
        return docCanCuTheoKhoa(sql, maPheDuyet, "Phê duyệt ngoại lệ không còn tồn tại.");
    }

    private CanCuBanGiao docCanCuTheoKhoa(String sql, Object khoa, String thongBaoKhongCo) {
        try (PreparedStatement ps = ketNoi().prepareStatement(sql)) {
            if (khoa instanceof Long) ps.setLong(1, (Long) khoa); else ps.setString(1, (String) khoa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return docCanCu(rs);
            }
        } catch (SQLException ex) {
            throw loi("Không đọc được căn cứ bàn giao.", ex);
        }
        throw new IllegalStateException(thongBaoKhongCo);
    }

    private CanCuBanGiao docCanCu(ResultSet rs) throws SQLException {
        boolean ngoaiLe = rs.getBoolean("NgoaiLe");
        Timestamp thoiDiem = rs.getTimestamp("ThoiDiem");
        return new CanCuBanGiao(rs.getString("MaLenh"), rs.getString("MaChungTu"), rs.getLong("PhaiThu"),
                rs.getLong("DaThu"), docTaiKhoan(rs.getString("NguoiXacNhan")),
                thoiDiem == null ? LocalDateTime.now() : thoiDiem.toLocalDateTime(), ngoaiLe, rs.getString("LyDo"));
    }

    private TaiKhoan docTaiKhoan(String maNhanVien) {
        String sql = "SELECT MaNhanVien,VaiTro,HoatDong,NgayTao,PhienBan FROM dbo.TaiKhoan WHERE MaNhanVien=?";
        try (PreparedStatement ps = ketNoi().prepareStatement(sql)) {
            ps.setString(1, maNhanVien);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalStateException("Tài khoản xác nhận không còn tồn tại.");
                return new TaiKhoan(rs.getString("MaNhanVien"), VaiTro.valueOf(rs.getString("VaiTro")),
                        rs.getBoolean("HoatDong"), rs.getTimestamp("NgayTao").toLocalDateTime(), rs.getInt("PhienBan"));
            }
        } catch (SQLException ex) { throw loi("Không đọc được tài khoản xác nhận.", ex); }
    }

    private long maPheDuyet(CanCuBanGiao canCu) {
        String tienTo = "NGOAI_LE-";
        if (!canCu.getMaChungTu().startsWith(tienTo))
            throw new IllegalStateException("Mã phê duyệt ngoại lệ không hợp lệ.");
        try { return Long.parseLong(canCu.getMaChungTu().substring(tienTo.length())); }
        catch (NumberFormatException ex) { throw new IllegalStateException("Mã phê duyệt ngoại lệ không hợp lệ.", ex); }
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
