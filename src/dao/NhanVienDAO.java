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
import java.util.ArrayList;
import java.util.List;

/** Đọc và cập nhật hồ sơ nhân viên trực tiếp trong SQL Server. */
public final class NhanVienDAO {

    /** Lấy nhân viên kỹ thuật có tài khoản đang hoạt động để phân công. */
    public List<KyThuatVienPhanCong> layLienKetKyThuat(TaiKhoanDAO taiKhoanDAO) {
        if (taiKhoanDAO == null) {
            throw new IllegalArgumentException("Thiếu nguồn tài khoản.");
        }
        String sql = "SELECT nv.*, tk.VaiTro, tk.HoatDong, tk.NgayTao, tk.PhienBan "
                + "FROM dbo.NhanVien nv JOIN dbo.TaiKhoan tk ON tk.MaNhanVien = nv.MaNhanVien "
                + "WHERE nv.BoPhan = 'KY_THUAT' AND nv.TrangThai = 'DANG_LAM' "
                + "AND tk.VaiTro = 'NHAN_VIEN_KY_THUAT' AND tk.HoatDong = 1 "
                + "ORDER BY nv.HoTen, nv.MaNhanVien";
        List<KyThuatVienPhanCong> danhSach = new ArrayList<>();
        try (PreparedStatement lenh = ketNoi().prepareStatement(sql);
                ResultSet ketQua = lenh.executeQuery()) {
            while (ketQua.next()) {
                NhanVien nhanVien = docNhanVien(ketQua);
                Timestamp ngayTao = ketQua.getTimestamp("NgayTao");
                TaiKhoan taiKhoan = new TaiKhoan(
                        ketQua.getString("MaNhanVien"),
                        VaiTro.valueOf(ketQua.getString("VaiTro")),
                        ketQua.getBoolean("HoatDong"),
                        ngayTao.toLocalDateTime(),
                        ketQua.getInt("PhienBan"));
                danhSach.add(new KyThuatVienPhanCong(nhanVien, taiKhoan));
            }
            return danhSach;
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể đọc danh sách kỹ thuật viên.", ex);
        }
    }

    /** Đọc toàn bộ hồ sơ nhân viên từ database. */
    public List<NhanVien> layDanhSach(TaiKhoan taiKhoan) {
        kiemTraQuyen(taiKhoan);
        List<NhanVien> danhSach = new ArrayList<>();
        String sql = "SELECT * FROM dbo.NhanVien ORDER BY MaNhanVien";
        try (PreparedStatement lenh = ketNoi().prepareStatement(sql);
                ResultSet ketQua = lenh.executeQuery()) {
            while (ketQua.next()) {
                danhSach.add(docNhanVien(ketQua));
            }
            return danhSach;
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể đọc danh sách nhân viên.", ex);
        }
    }

    /** Tìm và lọc nhân viên theo từ khóa và trạng thái. */
    public List<NhanVien> timKiem(TaiKhoan taiKhoan, String tuKhoa, TrangThaiNhanVien trangThai) {
        String chu = boDau(tuKhoa == null ? "" : tuKhoa.trim());
        String so = chiLayChuSo(chu);
        List<NhanVien> ketQua = new ArrayList<>();
        for (NhanVien nhanVien : layDanhSach(taiKhoan)) {
            boolean dungTrangThai = trangThai == null || nhanVien.getTrangThai() == trangThai;
            boolean dungTuKhoa = boDau(nhanVien.getMaNhanVien()).contains(chu)
                    || boDau(nhanVien.getHoTen()).contains(chu)
                    || (!so.isEmpty() && nhanVien.getSoDienThoai().contains(so));
            if (dungTrangThai && dungTuKhoa) {
                ketQua.add(nhanVien);
            }
        }
        return ketQua;
    }

    /** Tìm nhân viên theo mã. */
    public NhanVien timTheoMa(TaiKhoan taiKhoan, String maNhanVien) {
        kiemTraQuyen(taiKhoan);
        return timNoiBo(maNhanVien);
    }

    /** Sinh mã nhân viên dự kiến để hiển thị trên form. */
    public String maTiepTheo(TaiKhoan taiKhoan) {
        kiemTraQuyen(taiKhoan);
        return maTiepTheo(ketNoi(), false);
    }

    /** Thêm hồ sơ nhân viên vào database. */
    public NhanVien them(TaiKhoan taiKhoan, NhanVien nhanVien) {
        kiemTraQuyen(taiKhoan);
        if (nhanVien == null) {
            throw new IllegalArgumentException("Thiếu nhân viên.");
        }
        Connection ketNoi = ketNoi();
        try {
            ketNoi.setAutoCommit(false);
            String maMoi = maTiepTheo(ketNoi, true);
            if (!maMoi.equals(nhanVien.getMaNhanVien())) {
                throw new IllegalArgumentException("Mã nhân viên đã thay đổi. Hãy mở lại form thêm.");
            }
            String sql = "INSERT dbo.NhanVien "
                    + "(MaNhanVien, HoTen, NgaySinh, SoDienThoai, GioiTinh, DiaChi, "
                    + "SoNamKinhNghiem, BoPhan, TrangThai) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
                ganNhanVien(lenh, nhanVien, true);
                lenh.executeUpdate();
            }
            ketNoi.commit();
            return nhanVien;
        } catch (SQLException ex) {
            hoanTac(ketNoi);
            throw loiDuLieu("Không thể thêm nhân viên.", ex);
        } catch (RuntimeException ex) {
            hoanTac(ketNoi);
            throw ex;
        } finally {
            batLaiTuDongLuu(ketNoi);
        }
    }

    /** Cập nhật hồ sơ và khóa tài khoản nếu nhân viên đã nghỉ việc. */
    public void capNhat(TaiKhoan taiKhoan, NhanVien nhanVien) {
        kiemTraQuyen(taiKhoan);
        if (nhanVien == null) {
            throw new IllegalArgumentException("Thiếu nhân viên.");
        }
        if (taiKhoan.getMaNhanVien().equals(nhanVien.getMaNhanVien())
                && nhanVien.getTrangThai() == TrangThaiNhanVien.DA_NGHI) {
            throw new IllegalArgumentException("Không được tự chuyển tài khoản đang đăng nhập sang đã nghỉ.");
        }
        Connection ketNoi = ketNoi();
        try {
            ketNoi.setAutoCommit(false);
            kiemTraBoPhanKhopTaiKhoan(ketNoi, nhanVien);
            String sql = "UPDATE dbo.NhanVien SET HoTen = ?, NgaySinh = ?, SoDienThoai = ?, "
                    + "GioiTinh = ?, DiaChi = ?, SoNamKinhNghiem = ?, BoPhan = ?, TrangThai = ? "
                    + "WHERE MaNhanVien = ?";
            try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
                ganNhanVien(lenh, nhanVien, false);
                if (lenh.executeUpdate() == 0) {
                    throw new IllegalArgumentException("Không tìm thấy nhân viên cần cập nhật.");
                }
            }
            if (nhanVien.getTrangThai() == TrangThaiNhanVien.DA_NGHI) {
                String khoaTaiKhoan = "UPDATE dbo.TaiKhoan SET HoatDong = 0, PhienBan = PhienBan + 1 "
                        + "WHERE MaNhanVien = ? AND HoatDong = 1";
                try (PreparedStatement lenh = ketNoi.prepareStatement(khoaTaiKhoan)) {
                    lenh.setString(1, nhanVien.getMaNhanVien());
                    lenh.executeUpdate();
                }
            }
            ketNoi.commit();
        } catch (SQLException ex) {
            hoanTac(ketNoi);
            throw loiDuLieu("Không thể cập nhật nhân viên.", ex);
        } catch (RuntimeException ex) {
            hoanTac(ketNoi);
            throw ex;
        } finally {
            batLaiTuDongLuu(ketNoi);
        }
    }

    /** Không để bộ phận nhân viên lệch với vai trò tài khoản đang được cấp. */
    private void kiemTraBoPhanKhopTaiKhoan(Connection ketNoi, NhanVien nhanVien)
            throws SQLException {
        String sql = "SELECT VaiTro FROM dbo.TaiKhoan WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE MaNhanVien = ?";
        try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            lenh.setString(1, nhanVien.getMaNhanVien());
            try (ResultSet ketQua = lenh.executeQuery()) {
                if (!ketQua.next()) {
                    return;
                }
                VaiTro vaiTroHienTai = VaiTro.valueOf(ketQua.getString("VaiTro"));
                VaiTro vaiTroTheoBoPhan = vaiTroTheoBoPhan(nhanVien.getBoPhan());
                if (vaiTroHienTai != vaiTroTheoBoPhan) {
                    throw new IllegalArgumentException(
                            "Bộ phận không khớp vai trò tài khoản. "
                                    + "Hãy cập nhật vai trò tài khoản trước.");
                }
            }
        }
    }

    private VaiTro vaiTroTheoBoPhan(BoPhanNhanVien boPhan) {
        if (boPhan == BoPhanNhanVien.QUAN_LY) {
            return VaiTro.QUAN_LY;
        }
        if (boPhan == BoPhanNhanVien.TIEP_NHAN) {
            return VaiTro.NHAN_VIEN_TIEP_NHAN;
        }
        if (boPhan == BoPhanNhanVien.KY_THUAT) {
            return VaiTro.NHAN_VIEN_KY_THUAT;
        }
        return VaiTro.THU_NGAN;
    }

    /** Tìm, lọc rồi sắp xếp danh sách nhân viên. */
    public List<NhanVien> timKiem(TaiKhoan taiKhoan, String tuKhoa,
            TrangThaiNhanVien trangThai, int sapXep) {
        List<NhanVien> ketQua = timKiem(taiKhoan, tuKhoa, trangThai);
        for (int i = 0; i < ketQua.size() - 1; i++) {
            for (int j = i + 1; j < ketQua.size(); j++) {
                NhanVien a = ketQua.get(i);
                NhanVien b = ketQua.get(j);
                int soSanh = sapXep == 0
                        ? a.getMaNhanVien().compareToIgnoreCase(b.getMaNhanVien())
                        : boDau(a.getHoTen()).compareTo(boDau(b.getHoTen()));
                if (sapXep == 2) {
                    soSanh = -soSanh;
                }
                if (soSanh > 0) {
                    ketQua.set(i, b);
                    ketQua.set(j, a);
                }
            }
        }
        return ketQua;
    }

    private NhanVien timNoiBo(String maNhanVien) {
        if (maNhanVien == null || maNhanVien.isBlank()) {
            throw new IllegalArgumentException("Thiếu mã nhân viên.");
        }
        String sql = "SELECT * FROM dbo.NhanVien WHERE MaNhanVien = ?";
        try (PreparedStatement lenh = ketNoi().prepareStatement(sql)) {
            lenh.setString(1, maNhanVien.trim());
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

    private String maTiepTheo(Connection ketNoi, boolean coKhoa) {
        String goiYKhoa = coKhoa ? " WITH (UPDLOCK, HOLDLOCK)" : "";
        String sql = "SELECT ISNULL(MAX(TRY_CONVERT(INT, SUBSTRING(MaNhanVien, 3, 18))), 0) + 1 "
                + "FROM dbo.NhanVien" + goiYKhoa + " WHERE MaNhanVien LIKE 'NV%'";
        try (PreparedStatement lenh = ketNoi.prepareStatement(sql);
                ResultSet ketQua = lenh.executeQuery()) {
            ketQua.next();
            return String.format("NV%03d", ketQua.getInt(1));
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể cấp mã nhân viên.", ex);
        }
    }

    private static void ganNhanVien(PreparedStatement lenh, NhanVien nhanVien, boolean coMa)
            throws SQLException {
        int viTri = 1;
        if (coMa) {
            lenh.setString(viTri++, nhanVien.getMaNhanVien());
        }
        lenh.setString(viTri++, nhanVien.getHoTen());
        lenh.setDate(viTri++, Date.valueOf(nhanVien.getNgaySinh()));
        lenh.setString(viTri++, nhanVien.getSoDienThoai());
        lenh.setString(viTri++, nhanVien.getGioiTinh());
        lenh.setString(viTri++, nhanVien.getDiaChi());
        lenh.setInt(viTri++, nhanVien.getSoNamKinhNghiem());
        lenh.setString(viTri++, nhanVien.getBoPhan().name());
        lenh.setString(viTri++, nhanVien.getTrangThai().name());
        if (!coMa) {
            lenh.setString(viTri, nhanVien.getMaNhanVien());
        }
    }

    private static NhanVien docNhanVien(ResultSet ketQua) throws SQLException {
        return new NhanVien(
                ketQua.getString("MaNhanVien"),
                ketQua.getString("HoTen"),
                ketQua.getDate("NgaySinh").toLocalDate(),
                ketQua.getString("SoDienThoai"),
                ketQua.getInt("SoNamKinhNghiem"),
                ketQua.getString("GioiTinh"),
                ketQua.getString("DiaChi"),
                BoPhanNhanVien.valueOf(ketQua.getString("BoPhan")),
                TrangThaiNhanVien.valueOf(ketQua.getString("TrangThai")));
    }

    private void kiemTraQuyen(TaiKhoan taiKhoan) {
        if (taiKhoan == null || !new TaiKhoanDAO().phienConHopLe(taiKhoan)) {
            throw new SecurityException("Cần phiên đăng nhập còn hiệu lực.");
        }
        taiKhoan.kiemTraPhien();
        if (taiKhoan.getVaiTro() != VaiTro.QUAN_LY) {
            throw new SecurityException("Chỉ Quản lý được quản lý hồ sơ nhân viên.");
        }
    }

    private static String chiLayChuSo(String chu) {
        String ketQua = "";
        for (int i = 0; i < chu.length(); i++) {
            char kyTu = chu.charAt(i);
            if (kyTu >= '0' && kyTu <= '9') {
                ketQua = ketQua + kyTu;
            }
        }
        return ketQua;
    }

    private static String boDau(String chu) {
        String thuong = chu.toLowerCase();
        String[] cacNhom = { "àáảãạăằắẳẵặâầấẩẫậ", "èéẻẽẹêềếểễệ", "ìíỉĩị",
                "òóỏõọôồốổỗộơờớởỡợ", "ùúủũụưừứửữự", "ỳýỷỹỵ", "đ" };
        String thayThe = "aeiouyd";
        String ketQua = "";
        for (int i = 0; i < thuong.length(); i++) {
            char kyTu = thuong.charAt(i);
            for (int j = 0; j < cacNhom.length; j++) {
                if (cacNhom[j].indexOf(kyTu) >= 0) {
                    kyTu = thayThe.charAt(j);
                    break;
                }
            }
            ketQua = ketQua + kyTu;
        }
        return ketQua;
    }

    private static Connection ketNoi() {
        try {
            return ConnectDB.getInstance().getConnection();
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể kết nối database QuanLiGarage.", ex);
        }
    }

    private static void hoanTac(Connection ketNoi) {
        try {
            ketNoi.rollback();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static void batLaiTuDongLuu(Connection ketNoi) {
        try {
            ketNoi.setAutoCommit(true);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static IllegalStateException loiDuLieu(String thongBao, SQLException ex) {
        return new IllegalStateException(thongBao + " " + ex.getMessage(), ex);
    }

    /** Ghép hồ sơ kỹ thuật viên với tài khoản dùng để phân công. */
    public static final class KyThuatVienPhanCong {
        private final NhanVien nhanVien;
        private final TaiKhoan taiKhoan;

        public KyThuatVienPhanCong(NhanVien nhanVien, TaiKhoan taiKhoan) {
            if (nhanVien == null || taiKhoan == null) {
                throw new IllegalArgumentException("Thiếu dữ liệu kỹ thuật viên.");
            }
            this.nhanVien = nhanVien;
            this.taiKhoan = taiKhoan;
        }

        public NhanVien getNhanVien() {
            return nhanVien;
        }

        public TaiKhoan getTaiKhoan() {
            return taiKhoan;
        }

        public boolean duocPhanCong() {
            return nhanVien.getBoPhan() == BoPhanNhanVien.KY_THUAT
                    && nhanVien.getTrangThai() == TrangThaiNhanVien.DANG_LAM
                    && taiKhoan.getVaiTro() == VaiTro.NHAN_VIEN_KY_THUAT
                    && taiKhoan.isHoatDong();
        }

        @Override
        public String toString() {
            return nhanVien.getHoTen() + " · " + taiKhoan.getTenDangNhap();
        }
    }
}
