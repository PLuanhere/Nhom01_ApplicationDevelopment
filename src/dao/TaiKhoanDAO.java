package dao;

import connectDB.ConnectDB;
import entity.NhanVien;
import entity.NhanVien.BoPhanNhanVien;
import entity.NhanVien.TrangThaiNhanVien;
import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Đọc và cập nhật tài khoản nhân viên trong SQL Server. */
public final class TaiKhoanDAO {
    private static final String THUAT_TOAN = "PBKDF2-HMAC-SHA256";
    private static final int SO_VONG = 600_000;
    private static final int DO_DAI_HASH_BIT = 256;
    private static final SecureRandom NGAU_NHIEN = new SecureRandom();

    /** Đăng nhập bằng mã nhân viên và mật khẩu được cấp. */
    public TaiKhoan dangNhap(String maNhanVien, char[] matKhau) {
        if (maNhanVien == null || maNhanVien.trim().isEmpty() || matKhau == null || matKhau.length == 0) {
            throw new IllegalArgumentException("Cần nhập mã nhân viên và mật khẩu.");
        }
        String sql = "SELECT tk.MaNhanVien,tk.MatKhau,tk.VaiTro,tk.HoatDong,tk.NgayTao,tk.PhienBan,"
                + "tk.BuocDoiMatKhau,"
                + "bm.ThuatToan,bm.Muoi,bm.GiaTriBam,bm.SoVong FROM dbo.TaiKhoan tk "
                + "LEFT JOIN dbo.TaiKhoanThongTinBaoMat bm ON bm.MaNhanVien=tk.MaNhanVien "
                + "WHERE tk.MaNhanVien=?";
        try (PreparedStatement lenh = ketNoi().prepareStatement(sql)) {
            lenh.setString(1, maNhanVien.trim());
            try (ResultSet ketQua = lenh.executeQuery()) {
                if (!ketQua.next() || !xacThucMatKhau(ketQua, matKhau)) {
                    return null;
                }
                TaiKhoan taiKhoan = docTaiKhoan(ketQua);
                if (!taiKhoan.isHoatDong()) {
                    throw new SecurityException("Tài khoản đã bị khóa. Vui lòng liên hệ Quản lý.");
                }
                return taiKhoan;
            }
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể kiểm tra tài khoản.", ex);
        }
    }

    /** Kết thúc phiên đăng nhập hiện tại. */
    public void dangXuat(TaiKhoan taiKhoan) {
        if (taiKhoan != null) {
            taiKhoan.ketThucPhien();
        }
    }

    /** Đọc tài khoản để ghép với hồ sơ nhân viên trong các nghiệp vụ khác. */
    List<TaiKhoan> layTaiKhoanNoiBo() {
        String sql = "SELECT MaNhanVien,VaiTro,HoatDong,NgayTao,PhienBan,BuocDoiMatKhau "
                + "FROM dbo.TaiKhoan ORDER BY MaNhanVien";
        List<TaiKhoan> danhSach = new ArrayList<>();
        try (PreparedStatement lenh = ketNoi().prepareStatement(sql); ResultSet ketQua = lenh.executeQuery()) {
            while (ketQua.next()) {
                danhSach.add(docTaiKhoan(ketQua));
            }
            return danhSach;
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể đọc danh sách tài khoản.", ex);
        }
    }

    /** Đọc toàn bộ tài khoản sau khi kiểm tra quyền Quản lý. */
    public List<TaiKhoan> layDanhSach(TaiKhoan nguoiDung) {
        kiemTraQuyen(nguoiDung);
        return layTaiKhoanNoiBo();
    }

    /** Tìm, lọc và sắp xếp tài khoản cùng hồ sơ nhân viên. */
    public List<TaiKhoan> timKiem(TaiKhoan nguoiDung, String tuKhoa, int hoatDong, VaiTro vaiTro, int sapXep) {
        kiemTraQuyen(nguoiDung);
        String sapXepSql = "tk.MaNhanVien ASC";
        if (sapXep == 1) {
            sapXepSql = "nv.HoTen ASC";
        } else if (sapXep == 2) {
            sapXepSql = "nv.HoTen DESC";
        }
        String sql = "SELECT tk.MaNhanVien,tk.VaiTro,tk.HoatDong,tk.NgayTao,tk.PhienBan,"
                + "tk.BuocDoiMatKhau "
                + "FROM dbo.TaiKhoan tk JOIN dbo.NhanVien nv ON nv.MaNhanVien = tk.MaNhanVien "
                + "WHERE (tk.MaNhanVien LIKE ? OR nv.HoTen LIKE ?) "
                + "AND (? = -1 OR tk.HoatDong = ?) "
                + "AND (? IS NULL OR tk.VaiTro = ?) ORDER BY " + sapXepSql;
        String tim = "%" + (tuKhoa == null ? "" : tuKhoa.trim()) + "%";
        List<TaiKhoan> danhSach = new ArrayList<>();
        try (PreparedStatement lenh = ketNoi().prepareStatement(sql)) {
            lenh.setString(1, tim);
            lenh.setString(2, tim);
            lenh.setInt(3, hoatDong);
            lenh.setBoolean(4, hoatDong == 1);
            if (vaiTro == null) {
                lenh.setNull(5, java.sql.Types.VARCHAR);
                lenh.setNull(6, java.sql.Types.VARCHAR);
            } else {
                lenh.setString(5, vaiTro.name());
                lenh.setString(6, vaiTro.name());
            }
            try (ResultSet ketQua = lenh.executeQuery()) {
                while (ketQua.next()) {
                    danhSach.add(docTaiKhoan(ketQua));
                }
            }
            return danhSach;
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể tìm tài khoản.", ex);
        }
    }

    /** Tìm tài khoản theo mã nhân viên. */
    public TaiKhoan timTheoMa(TaiKhoan nguoiDung, String maNhanVien) {
        kiemTraQuyen(nguoiDung);
        TaiKhoan taiKhoan = timTaiKhoan(maNhanVien);
        if (taiKhoan == null) {
            throw new IllegalArgumentException("Không tìm thấy tài khoản.");
        }
        return taiKhoan;
    }

    /** Tìm hồ sơ nhân viên phục vụ màn quản lý tài khoản. */
    public NhanVien timNhanVienTheoMa(TaiKhoan nguoiDung, String maNhanVien) {
        kiemTraQuyen(nguoiDung);
        String sql = "SELECT * FROM dbo.NhanVien WHERE MaNhanVien = ?";
        try (PreparedStatement lenh = ketNoi().prepareStatement(sql)) {
            lenh.setString(1, maNhanVien);
            try (ResultSet ketQua = lenh.executeQuery()) {
                if (ketQua.next()) {
                    return docNhanVien(ketQua);
                }
            }
            throw new IllegalArgumentException("Không tìm thấy nhân viên.");
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể đọc nhân viên.", ex);
        }
    }

    /** Lấy nhân viên đang làm việc và chưa được cấp tài khoản. */
    public List<NhanVien> nhanVienChuaCoTaiKhoan(TaiKhoan nguoiDung) {
        kiemTraQuyen(nguoiDung);
        String sql = "SELECT nv.* FROM dbo.NhanVien nv LEFT JOIN dbo.TaiKhoan tk "
                + "ON tk.MaNhanVien = nv.MaNhanVien WHERE tk.MaNhanVien IS NULL "
                + "AND nv.TrangThai = 'DANG_LAM' ORDER BY nv.MaNhanVien";
        List<NhanVien> danhSach = new ArrayList<>();
        try (PreparedStatement lenh = ketNoi().prepareStatement(sql); ResultSet ketQua = lenh.executeQuery()) {
            while (ketQua.next()) {
                danhSach.add(docNhanVien(ketQua));
            }
            return danhSach;
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể đọc nhân viên chưa có tài khoản.", ex);
        }
    }

    /** Cấp tài khoản mới; mã tài khoản chính là mã nhân viên. */
    public TaiKhoan them(TaiKhoan nguoiDung, TaiKhoan thongTin, char[] matKhau) {
        kiemTraQuyen(nguoiDung);
        if (thongTin == null) {
            throw new IllegalArgumentException("Thiếu thông tin tài khoản.");
        }
        TaiKhoan.kiemTraMatKhauMoi(matKhau);
        Connection ketNoi = ketNoi();
        try {
            ketNoi.setAutoCommit(false);
            ThongTinBam baoMat = bamMatKhau(matKhau);
            String sql = "INSERT dbo.TaiKhoan "
                    + "(MaNhanVien,MatKhau,VaiTro,HoatDong,NgayTao,BuocDoiMatKhau) "
                    + "VALUES (?,?,?,?,SYSDATETIME(),1)";
            try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
                String ma = thongTin.getMaNhanVien();
                lenh.setString(1, ma);
                lenh.setString(2, "!PBKDF2!" + UUID.randomUUID());
                lenh.setString(3, thongTin.getVaiTro().name());
                lenh.setBoolean(4, thongTin.isHoatDong());
                lenh.executeUpdate();
            }
            try (PreparedStatement lenh = ketNoi.prepareStatement(
                    "INSERT dbo.TaiKhoanThongTinBaoMat(MaNhanVien,ThuatToan,Muoi,GiaTriBam,SoVong) "
                            + "VALUES(?,?,?,?,?)")) {
                lenh.setString(1, thongTin.getMaNhanVien());
                lenh.setString(2, THUAT_TOAN);
                lenh.setBytes(3, baoMat.muoi);
                lenh.setBytes(4, baoMat.hash);
                lenh.setInt(5, SO_VONG);
                lenh.executeUpdate();
            }
            TaiKhoan moi = timTaiKhoan(thongTin.getMaNhanVien(), ketNoi);
            ghiNhatKy(ketNoi, nguoiDung, moi, "Cấp tài khoản · " + moi.getVaiTro().getTenHienThi(),
                    "Cấp cho nhân viên");
            ketNoi.commit();
            return moi;
        } catch (SQLException ex) {
            hoanTac(ketNoi);
            if (ex.getErrorCode() == 2601 || ex.getErrorCode() == 2627) {
                throw new IllegalArgumentException("Nhân viên này đã có tài khoản.");
            }
            throw loiDuLieu("Không thể cấp tài khoản.", ex);
        } finally {
            batLaiTuDongLuu(ketNoi);
        }
    }

    /** Cập nhật vai trò hoặc trạng thái tài khoản. */
    public void capNhat(TaiKhoan nguoiDung, TaiKhoan banDau, VaiTro vaiTro, boolean hoatDong, String lyDo) {
        kiemTraQuyen(nguoiDung);
        if (banDau == null || vaiTro == null) {
            throw new IllegalArgumentException("Thiếu thông tin tài khoản.");
        }
        String lyDoHopLe = kiemTraLyDo(lyDo);
        TaiKhoan hienTai = timTaiKhoan(banDau.getMaNhanVien());
        if (hienTai == null) {
            throw new IllegalArgumentException("Không tìm thấy tài khoản.");
        }
        if (hienTai.getPhienBan() != banDau.getPhienBan()
                || hienTai.getVaiTro() != banDau.getVaiTro()
                || hienTai.isHoatDong() != banDau.isHoatDong()) {
            throw new IllegalArgumentException("Tài khoản đã thay đổi. Hãy mở lại danh sách.");
        }
        if (hienTai.getVaiTro() == vaiTro && hienTai.isHoatDong() == hoatDong) {
            throw new IllegalArgumentException("Vai trò và trạng thái chưa thay đổi.");
        }
        if (nguoiDung.getMaNhanVien().equals(hienTai.getMaNhanVien())
                && (!hoatDong || vaiTro != VaiTro.QUAN_LY)) {
            throw new IllegalArgumentException("Không được tự khóa hoặc tự hạ quyền tài khoản đang đăng nhập.");
        }
        if (hienTai.isHoatDong() && hienTai.getVaiTro() == VaiTro.QUAN_LY
                && (!hoatDong || vaiTro != VaiTro.QUAN_LY) && demQuanLyHoatDong() <= 1) {
            throw new IllegalArgumentException("Phải giữ ít nhất một tài khoản Quản lý hoạt động.");
        }
        Connection ketNoi = ketNoi();
        try {
            ketNoi.setAutoCommit(false);
            try (PreparedStatement lenh = ketNoi.prepareStatement(
                    "UPDATE dbo.TaiKhoan SET VaiTro = ?, HoatDong = ?, PhienBan = PhienBan + 1 "
                            + "WHERE MaNhanVien = ?")) {
                lenh.setString(1, vaiTro.name());
                lenh.setBoolean(2, hoatDong);
                lenh.setString(3, hienTai.getMaNhanVien());
                lenh.executeUpdate();
            }
            TaiKhoan moi = timTaiKhoan(hienTai.getMaNhanVien(), ketNoi);
            String hanhDong = hienTai.getVaiTro().getTenHienThi() + " → " + vaiTro.getTenHienThi() + "; "
                    + tenTrangThai(hienTai) + " → " + tenTrangThai(moi);
            ghiNhatKy(ketNoi, nguoiDung, moi, hanhDong, lyDoHopLe);
            ketNoi.commit();
        } catch (SQLException ex) {
            hoanTac(ketNoi);
            throw loiDuLieu("Không thể cập nhật tài khoản.", ex);
        } finally {
            batLaiTuDongLuu(ketNoi);
        }
    }

    /** Đặt mật khẩu mới và thu hồi các phiên cũ của tài khoản. */
    public void datLaiMatKhau(TaiKhoan nguoiDung, TaiKhoan banDau, char[] matKhau, String lyDo) {
        kiemTraQuyen(nguoiDung);
        if (banDau == null) {
            throw new IllegalArgumentException("Thiếu tài khoản cần đặt lại mật khẩu.");
        }
        TaiKhoan.kiemTraMatKhauMoi(matKhau);
        String lyDoHopLe = kiemTraLyDo(lyDo);
        if (timTaiKhoan(banDau.getMaNhanVien()) == null) {
            throw new IllegalArgumentException("Không tìm thấy tài khoản.");
        }
        Connection ketNoi = ketNoi();
        try {
            ketNoi.setAutoCommit(false);
            ThongTinBam baoMat = bamMatKhau(matKhau);
            try (PreparedStatement lenh = ketNoi.prepareStatement(
                    "EXEC dbo.usp_DatMatKhauBam ?,?,?,?,?")) {
                lenh.setString(1, banDau.getMaNhanVien());
                lenh.setString(2, nguoiDung.getMaNhanVien());
                lenh.setBytes(3, baoMat.muoi);
                lenh.setBytes(4, baoMat.hash);
                lenh.setInt(5, SO_VONG);
                lenh.execute();
            }
            ghiNhatKy(ketNoi, nguoiDung, banDau, "Ghi nhận lý do đặt lại mật khẩu", lyDoHopLe);
            ketNoi.commit();
        } catch (SQLException ex) {
            hoanTac(ketNoi);
            throw loiDuLieu("Không thể đặt lại mật khẩu.", ex);
        } finally {
            batLaiTuDongLuu(ketNoi);
        }
    }

    /** Đổi mật khẩu bắt buộc ngay sau đăng nhập và trả về phiên tài khoản mới. */
    public TaiKhoan doiMatKhauLanDau(TaiKhoan taiKhoan, char[] matKhauMoi) {
        if (taiKhoan == null || !taiKhoan.isBuocDoiMatKhau()) {
            throw new IllegalArgumentException("Tài khoản không ở trạng thái bắt buộc đổi mật khẩu.");
        }

        TaiKhoan.kiemTraMatKhauMoi(matKhauMoi);
        Connection ketNoi = ketNoi();

        try {
            ketNoi.setAutoCommit(false);
            ThongTinBam baoMat = bamMatKhau(matKhauMoi);

            try (PreparedStatement lenh = ketNoi.prepareStatement(
                    "UPDATE dbo.TaiKhoan SET MatKhau=?,BuocDoiMatKhau=0,PhienBan=PhienBan+1 "
                            + "WHERE MaNhanVien=? AND BuocDoiMatKhau=1")) {
                lenh.setString(1, "!PBKDF2!" + UUID.randomUUID());
                lenh.setString(2, taiKhoan.getMaNhanVien());

                if (lenh.executeUpdate() != 1) {
                    throw new IllegalStateException(
                            "Trạng thái tài khoản đã thay đổi. Vui lòng đăng nhập lại.");
                }
            }

            try (PreparedStatement lenh = ketNoi.prepareStatement(
                    "UPDATE dbo.TaiKhoanThongTinBaoMat SET ThuatToan=?,Muoi=?,GiaTriBam=?,"
                            + "SoVong=?,NgayCapNhat=SYSDATETIME() WHERE MaNhanVien=?")) {
                lenh.setString(1, THUAT_TOAN);
                lenh.setBytes(2, baoMat.muoi);
                lenh.setBytes(3, baoMat.hash);
                lenh.setInt(4, SO_VONG);
                lenh.setString(5, taiKhoan.getMaNhanVien());

                if (lenh.executeUpdate() != 1) {
                    throw new IllegalStateException("Tài khoản thiếu thông tin bảo mật.");
                }
            }

            try (PreparedStatement lenh = ketNoi.prepareStatement(
                    "INSERT dbo.NhatKyTaiKhoan(ThoiDiem,NguoiThucHien,MaTaiKhoan,HanhDong,LyDo) "
                            + "VALUES(SYSDATETIME(),?,?,?,?)")) {
                lenh.setString(1, taiKhoan.getMaNhanVien());
                lenh.setString(2, taiKhoan.getMaNhanVien());
                lenh.setString(3, "Đổi mật khẩu lần đầu");
                lenh.setString(4, "Người dùng đổi mật khẩu tạm trước khi vào hệ thống");
                lenh.executeUpdate();
            }

            TaiKhoan moi = timTaiKhoan(taiKhoan.getMaNhanVien(), ketNoi);
            ketNoi.commit();
            taiKhoan.ketThucPhien();
            return moi;
        } catch (SQLException ex) {
            hoanTac(ketNoi);
            throw loiDuLieu("Không thể đổi mật khẩu lần đầu.", ex);
        } catch (RuntimeException ex) {
            hoanTac(ketNoi);
            throw ex;
        } finally {
            batLaiTuDongLuu(ketNoi);
        }
    }

    /** Trả về tên trạng thái tài khoản. */
    public static String tenTrangThai(TaiKhoan taiKhoan) {
        return taiKhoan.isHoatDong() ? "Đang hoạt động" : "Đã khóa";
    }

    /** Kiểm tra tài khoản đăng nhập còn hoạt động và chưa bị đổi vai trò. */
    public boolean phienConHopLe(TaiKhoan taiKhoan) {
        if (taiKhoan == null || !taiKhoan.isPhienHopLe()) {
            return false;
        }
        TaiKhoan hienTai = timTaiKhoan(taiKhoan.getMaNhanVien());
        boolean hopLe = hienTai != null && hienTai.isHoatDong()
                && hienTai.getVaiTro() == taiKhoan.getVaiTro()
                && hienTai.getPhienBan() == taiKhoan.getPhienBan();
        if (!hopLe) {
            taiKhoan.ketThucPhien();
        }
        return hopLe;
    }

    /** Kiểm tra phiên hiện tại vẫn là tài khoản Quản lý hợp lệ trong database. */
    private void kiemTraQuyen(TaiKhoan nguoiDung) {
        if (nguoiDung == null) {
            throw new SecurityException("Cần đăng nhập.");
        }
        nguoiDung.kiemTraPhien();
        if (nguoiDung.getVaiTro() != VaiTro.QUAN_LY || !phienConHopLe(nguoiDung)) {
            nguoiDung.ketThucPhien();
            throw new SecurityException("Phiên Quản lý đã hết hiệu lực. Vui lòng đăng nhập lại.");
        }
    }

    /** Tìm một tài khoản trực tiếp trong database. */
    private TaiKhoan timTaiKhoan(String maNhanVien) {
        try {
            return timTaiKhoan(maNhanVien, ketNoi());
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể đọc tài khoản.", ex);
        }
    }

    /** Tìm một tài khoản bằng kết nối đang tham gia giao dịch. */
    private TaiKhoan timTaiKhoan(String maNhanVien, Connection ketNoi) throws SQLException {
        String sql = "SELECT MaNhanVien,VaiTro,HoatDong,NgayTao,PhienBan,BuocDoiMatKhau "
                + "FROM dbo.TaiKhoan WHERE MaNhanVien = ?";
        try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            lenh.setString(1, maNhanVien);
            try (ResultSet ketQua = lenh.executeQuery()) {
                return ketQua.next() ? docTaiKhoan(ketQua) : null;
            }
        }
    }

    /** Đếm số tài khoản Quản lý đang hoạt động. */
    private int demQuanLyHoatDong() {
        String sql = "SELECT COUNT(*) FROM dbo.TaiKhoan WHERE VaiTro = 'QUAN_LY' AND HoatDong = 1";
        try (PreparedStatement lenh = ketNoi().prepareStatement(sql); ResultSet ketQua = lenh.executeQuery()) {
            ketQua.next();
            return ketQua.getInt(1);
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể kiểm tra tài khoản Quản lý.", ex);
        }
    }

    /** Ghi một thay đổi tài khoản vào nhật ký. */
    private void ghiNhatKy(Connection ketNoi, TaiKhoan nguoiDung, TaiKhoan dich, String hanhDong, String lyDo)
            throws SQLException {
        String sql = "INSERT dbo.NhatKyTaiKhoan "
                + "(ThoiDiem, NguoiThucHien, MaTaiKhoan, HanhDong, LyDo) "
                + "VALUES (SYSDATETIME(), ?, ?, ?, ?)";
        try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            lenh.setString(1, nguoiDung.getMaNhanVien());
            lenh.setString(2, dich.getMaNhanVien());
            lenh.setString(3, hanhDong);
            lenh.setString(4, lyDo);
            lenh.executeUpdate();
        }
    }

    /** Chuyển một dòng tài khoản thành Entity. */
    private static TaiKhoan docTaiKhoan(ResultSet ketQua) throws SQLException {
        String maNhanVien = ketQua.getString("MaNhanVien");
        Timestamp ngayTao = ketQua.getTimestamp("NgayTao");
        return new TaiKhoan(maNhanVien, VaiTro.valueOf(ketQua.getString("VaiTro")),
                ketQua.getBoolean("HoatDong"), ngayTao.toLocalDateTime(), ketQua.getInt("PhienBan"),
                ketQua.getBoolean("BuocDoiMatKhau"));
    }

    /** Chuyển một dòng nhân viên thành Entity. */
    private static NhanVien docNhanVien(ResultSet ketQua) throws SQLException {
        Date ngaySinh = ketQua.getDate("NgaySinh");
        return new NhanVien(ketQua.getString("MaNhanVien"), ketQua.getString("HoTen"), ngaySinh.toLocalDate(),
                ketQua.getString("SoDienThoai"), ketQua.getInt("SoNamKinhNghiem"),
                ketQua.getString("GioiTinh"), ketQua.getString("DiaChi"),
                BoPhanNhanVien.valueOf(ketQua.getString("BoPhan")),
                TrangThaiNhanVien.valueOf(ketQua.getString("TrangThai")));
    }

    private static boolean xacThucMatKhau(ResultSet ketQua, char[] matKhau) throws SQLException {
        byte[] hashDaLuu = ketQua.getBytes("GiaTriBam");
        byte[] muoi = ketQua.getBytes("Muoi");
        if (hashDaLuu != null && muoi != null) {
            String thuatToan = ketQua.getString("ThuatToan");
            int soVong = ketQua.getInt("SoVong");
            if (!THUAT_TOAN.equals(thuatToan) || soVong < SO_VONG || hashDaLuu.length != 32) {
                throw new SecurityException("Thông tin bảo mật tài khoản không hợp lệ; cần Quản lý đặt lại mật khẩu.");
            }
            byte[] hashNhap = taoHash(matKhau, muoi, soVong);
            try {
                return MessageDigest.isEqual(hashDaLuu, hashNhap);
            } finally {
                Arrays.fill(hashNhap, (byte) 0);
            }
        }
        byte[] nhap = new String(matKhau).getBytes(StandardCharsets.UTF_8);
        byte[] cu = ketQua.getString("MatKhau").getBytes(StandardCharsets.UTF_8);
        try {
            return MessageDigest.isEqual(cu, nhap);
        } finally {
            Arrays.fill(nhap, (byte) 0);
            Arrays.fill(cu, (byte) 0);
        }
    }

    private static ThongTinBam bamMatKhau(char[] matKhau) {
        byte[] muoi = new byte[16];
        NGAU_NHIEN.nextBytes(muoi);
        return new ThongTinBam(muoi, taoHash(matKhau, muoi, SO_VONG));
    }

    private static byte[] taoHash(char[] matKhau, byte[] muoi, int soVong) {
        PBEKeySpec dacTa = new PBEKeySpec(matKhau, muoi, soVong, DO_DAI_HASH_BIT);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(dacTa).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Máy chạy không hỗ trợ PBKDF2-HMAC-SHA256.", ex);
        } finally {
            dacTa.clearPassword();
        }
    }

    private static final class ThongTinBam {
        private final byte[] muoi;
        private final byte[] hash;

        private ThongTinBam(byte[] muoi, byte[] hash) {
            this.muoi = muoi;
            this.hash = hash;
        }
    }

    /** Kiểm tra lý do thay đổi. */
    private static String kiemTraLyDo(String lyDo) {
        if (lyDo == null || lyDo.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do thay đổi.");
        }
        return lyDo.trim();
    }

    /** Lấy kết nối SQL Server đang mở. */
    private static Connection ketNoi() {
        try {
            return ConnectDB.getInstance().getConnection();
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể kết nối database QuanLiGarage.", ex);
        }
    }

    /** Hoàn tác giao dịch khi lưu thất bại. */
    private static void hoanTac(Connection ketNoi) {
        try {
            ketNoi.rollback();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /** Trả kết nối về chế độ tự động lưu sau giao dịch. */
    private static void batLaiTuDongLuu(Connection ketNoi) {
        try {
            ketNoi.setAutoCommit(true);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /** Tạo lỗi dễ hiểu cho tầng giao diện. */
    private static IllegalStateException loiDuLieu(String thongBao, SQLException ex) {
        return new IllegalStateException(thongBao + " " + ex.getMessage(), ex);
    }

}
