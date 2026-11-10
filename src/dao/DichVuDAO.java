package dao;

import connectDB.ConnectDB;
import entity.DichVu;
import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Quản lý danh mục dịch vụ và bảng giá trực tiếp trong SQL Server. */
public final class DichVuDAO {
    private BaoGiaDAO nguonBaoGia;

    /** Đọc toàn bộ danh mục dịch vụ. */
    public List<DichVu> layDanhSach(TaiKhoan taiKhoan) {
        kiemTraQuyen(taiKhoan);
        return docDanhSach(false);
    }

    /** Tìm và lọc dịch vụ theo từ khóa và trạng thái áp dụng. */
    public List<DichVu> timKiem(TaiKhoan taiKhoan, String tuKhoa, int dangApDung) {
        String chu = boDau(tuKhoa == null ? "" : tuKhoa.trim());
        List<DichVu> ketQua = new ArrayList<>();
        for (DichVu dichVu : layDanhSach(taiKhoan)) {
            boolean dungChu = boDau(dichVu.getMaDichVu()).contains(chu)
                    || boDau(dichVu.getTenDichVu()).contains(chu)
                    || boDau(dichVu.getNhomDichVu()).contains(chu);
            boolean dungTrangThai = dangApDung == -1
                    || dichVu.isDangApDung() == (dangApDung == 1);
            if (dungChu && dungTrangThai) {
                ketQua.add(dichVu);
            }
        }
        return ketQua;
    }

    /** Tìm dịch vụ theo mã. */
    public DichVu timTheoMa(TaiKhoan taiKhoan, String maDichVu) {
        kiemTraQuyen(taiKhoan);
        return timNoiBo(maDichVu);
    }

    /** Sinh mã dịch vụ dự kiến để hiển thị trên form. */
    public String maTiepTheo(TaiKhoan taiKhoan) {
        kiemTraQuyen(taiKhoan);
        return maTiepTheo(ketNoi(), false);
    }

    /** Thêm dịch vụ mới vào database. */
    public void them(TaiKhoan taiKhoan, DichVu dichVu) {
        kiemTraQuyen(taiKhoan);
        if (dichVu == null) {
            throw new IllegalArgumentException("Thiếu dịch vụ.");
        }
        Connection ketNoi = ketNoi();
        try {
            ketNoi.setAutoCommit(false);
            String maMoi = maTiepTheo(ketNoi, true);
            if (!maMoi.equals(dichVu.getMaDichVu())) {
                throw new IllegalArgumentException("Mã dịch vụ đã thay đổi. Hãy mở lại form thêm dịch vụ.");
            }
            String sql = "INSERT dbo.DichVu "
                    + "(MaDichVu, TenDichVu, NhomDichVu, DonViTinh, DonGia, MoTa, "
                    + "ThoiGianDuKien, DieuKienApDung, DangApDung) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
                ganDichVu(lenh, dichVu, true);
                lenh.executeUpdate();
            }
            ketNoi.commit();
        } catch (SQLException ex) {
            hoanTac(ketNoi);
            throw loiDuLieu("Không thể thêm dịch vụ.", ex);
        } catch (RuntimeException ex) {
            hoanTac(ketNoi);
            throw ex;
        } finally {
            batLaiTuDongLuu(ketNoi);
        }
    }

    /** Cập nhật dịch vụ nếu bản đang lưu chưa bị người khác thay đổi. */
    public void capNhat(TaiKhoan taiKhoan, DichVu banDau, DichVu dichVu) {
        kiemTraQuyen(taiKhoan);
        if (banDau == null || dichVu == null) {
            throw new IllegalArgumentException("Thiếu thông tin dịch vụ.");
        }
        if (!banDau.getMaDichVu().equals(dichVu.getMaDichVu())) {
            throw new IllegalArgumentException("Không được thay đổi mã dịch vụ.");
        }
        Connection ketNoi = ketNoi();
        try {
            ketNoi.setAutoCommit(false);
            DichVu hienTai = timNoiBo(ketNoi, banDau.getMaDichVu(), true);
            if (!cungThongTin(hienTai, banDau)) {
                throw new IllegalArgumentException(
                        "Dịch vụ đã được cập nhật ở cửa sổ khác. Hãy mở lại chi tiết.");
            }
            String sql = "UPDATE dbo.DichVu SET TenDichVu = ?, NhomDichVu = ?, "
                    + "DonViTinh = ?, DonGia = ?, MoTa = ?, ThoiGianDuKien = ?, "
                    + "DieuKienApDung = ?, DangApDung = ? WHERE MaDichVu = ?";
            try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
                ganDichVu(lenh, dichVu, false);
                lenh.executeUpdate();
            }
            ketNoi.commit();
        } catch (SQLException ex) {
            hoanTac(ketNoi);
            throw loiDuLieu("Không thể cập nhật dịch vụ.", ex);
        } catch (RuntimeException ex) {
            hoanTac(ketNoi);
            throw ex;
        } finally {
            batLaiTuDongLuu(ketNoi);
        }
    }

    /** Xóa dịch vụ chưa từng được dùng; dịch vụ đã dùng phải chuyển sang tạm dừng. */
    public void xoa(TaiKhoan taiKhoan, DichVu dichVu) {
        kiemTraQuyen(taiKhoan);
        if (dichVu == null) {
            throw new IllegalArgumentException("Thiếu dịch vụ.");
        }
        Connection ketNoi = ketNoi();
        try {
            ketNoi.setAutoCommit(false);
            DichVu hienTai = timNoiBo(ketNoi, dichVu.getMaDichVu(), true);
            if (!cungThongTin(hienTai, dichVu)) {
                throw new IllegalArgumentException(
                        "Dịch vụ đã thay đổi. Hãy mở lại chi tiết trước khi xóa.");
            }
            String kiemTra = "SELECT COUNT(*) FROM dbo.ChiTietBaoGia WHERE MaDichVu = ?";
            try (PreparedStatement lenh = ketNoi.prepareStatement(kiemTra)) {
                lenh.setString(1, dichVu.getMaDichVu());
                try (ResultSet ketQua = lenh.executeQuery()) {
                    ketQua.next();
                    if (ketQua.getInt(1) > 0) {
                        throw new IllegalArgumentException(
                                "Dịch vụ đã có trong báo giá, không được xóa. Hãy tạm dừng dịch vụ.");
                    }
                }
            }
            try (PreparedStatement lenh = ketNoi.prepareStatement(
                    "DELETE dbo.DichVu WHERE MaDichVu = ?")) {
                lenh.setString(1, dichVu.getMaDichVu());
                lenh.executeUpdate();
            }
            ketNoi.commit();
        } catch (SQLException ex) {
            hoanTac(ketNoi);
            if (ex.getErrorCode() == 547) {
                throw new IllegalArgumentException(
                        "Dịch vụ đã phát sinh dữ liệu, không được xóa. Hãy tạm dừng dịch vụ.");
            }
            throw loiDuLieu("Không thể xóa dịch vụ.", ex);
        } catch (RuntimeException ex) {
            hoanTac(ketNoi);
            throw ex;
        } finally {
            batLaiTuDongLuu(ketNoi);
        }
    }

    /** Tìm, lọc rồi sắp xếp danh sách dịch vụ. */
    public List<DichVu> timKiem(TaiKhoan taiKhoan, String tuKhoa,
            int dangApDung, int sapXep) {
        List<DichVu> ketQua = timKiem(taiKhoan, tuKhoa, dangApDung);
        for (int i = 0; i < ketQua.size() - 1; i++) {
            for (int j = i + 1; j < ketQua.size(); j++) {
                DichVu a = ketQua.get(i);
                DichVu b = ketQua.get(j);
                int soSanh;
                if (sapXep == 0) {
                    soSanh = a.getMaDichVu().compareToIgnoreCase(b.getMaDichVu());
                } else if (sapXep == 3 || sapXep == 4) {
                    soSanh = Long.compare(a.getDonGia(), b.getDonGia());
                } else {
                    soSanh = boDau(a.getTenDichVu()).compareTo(boDau(b.getTenDichVu()));
                }
                if (sapXep == 2 || sapXep == 4) {
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

    /** Giữ phương thức nối nguồn cũ để không làm thay đổi các màn hình hiện tại. */
    public void noiNguonBaoGia(BaoGiaDAO nguon) {
        if (nguon == null || nguonBaoGia != null) {
            throw new IllegalArgumentException("Nguồn báo giá không hợp lệ hoặc đã nối.");
        }
        nguonBaoGia = nguon;
    }

    /** Đọc các dịch vụ đang áp dụng để lập báo giá. */
    public List<DichVu> layChoBaoGia(TaiKhoan taiKhoan) {
        if (taiKhoan == null || !new TaiKhoanDAO().phienConHopLe(taiKhoan)) {
            throw new SecurityException("Cần phiên đăng nhập còn hiệu lực.");
        }
        taiKhoan.kiemTraPhien();
        if (taiKhoan.getVaiTro() != VaiTro.NHAN_VIEN_TIEP_NHAN
                && taiKhoan.getVaiTro() != VaiTro.QUAN_LY) {
            throw new SecurityException("Chỉ Tiếp nhận hoặc Quản lý được đọc danh mục lập báo giá.");
        }
        return docDanhSach(true);
    }

    private List<DichVu> docDanhSach(boolean chiDangApDung) {
        String sql = "SELECT * FROM dbo.DichVu";
        if (chiDangApDung) {
            sql = sql + " WHERE DangApDung = 1";
        }
        sql = sql + " ORDER BY MaDichVu";
        List<DichVu> danhSach = new ArrayList<>();
        try (PreparedStatement lenh = ketNoi().prepareStatement(sql);
                ResultSet ketQua = lenh.executeQuery()) {
            while (ketQua.next()) {
                danhSach.add(docDichVu(ketQua));
            }
            return danhSach;
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể đọc danh mục dịch vụ.", ex);
        }
    }

    private DichVu timNoiBo(String maDichVu) {
        try {
            return timNoiBo(ketNoi(), maDichVu, false);
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể đọc dịch vụ.", ex);
        }
    }

    private DichVu timNoiBo(Connection ketNoi, String maDichVu, boolean coKhoa)
            throws SQLException {
        if (maDichVu == null || maDichVu.isBlank()) {
            throw new IllegalArgumentException("Thiếu mã dịch vụ.");
        }
        String goiY = coKhoa ? " WITH (UPDLOCK, HOLDLOCK)" : "";
        String sql = "SELECT * FROM dbo.DichVu" + goiY + " WHERE MaDichVu = ?";
        try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            lenh.setString(1, maDichVu.trim());
            try (ResultSet ketQua = lenh.executeQuery()) {
                if (ketQua.next()) {
                    return docDichVu(ketQua);
                }
            }
            throw new IllegalArgumentException("Không tìm thấy dịch vụ.");
        }
    }

    private String maTiepTheo(Connection ketNoi, boolean coKhoa) {
        String goiY = coKhoa ? " WITH (UPDLOCK, HOLDLOCK)" : "";
        String sql = "SELECT ISNULL(MAX(TRY_CONVERT(INT, SUBSTRING(MaDichVu, 3, 28))), 0) + 1 "
                + "FROM dbo.DichVu" + goiY + " WHERE MaDichVu LIKE 'DV%'";
        try (PreparedStatement lenh = ketNoi.prepareStatement(sql);
                ResultSet ketQua = lenh.executeQuery()) {
            ketQua.next();
            return String.format("DV%03d", ketQua.getInt(1));
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể cấp mã dịch vụ.", ex);
        }
    }

    private static void ganDichVu(PreparedStatement lenh, DichVu dichVu, boolean coMa)
            throws SQLException {
        int viTri = 1;
        if (coMa) {
            lenh.setString(viTri++, dichVu.getMaDichVu());
        }
        lenh.setString(viTri++, dichVu.getTenDichVu());
        lenh.setString(viTri++, dichVu.getNhomDichVu());
        lenh.setString(viTri++, dichVu.getDonViTinh());
        lenh.setLong(viTri++, dichVu.getDonGia());
        lenh.setString(viTri++, dichVu.getMoTa());
        lenh.setString(viTri++, dichVu.getThoiGianDuKien());
        lenh.setString(viTri++, dichVu.getDieuKienApDung());
        lenh.setBoolean(viTri++, dichVu.isDangApDung());
        if (!coMa) {
            lenh.setString(viTri, dichVu.getMaDichVu());
        }
    }

    private static DichVu docDichVu(ResultSet ketQua) throws SQLException {
        return new DichVu(
                ketQua.getString("MaDichVu"),
                ketQua.getString("TenDichVu"),
                ketQua.getString("NhomDichVu"),
                ketQua.getString("DonViTinh"),
                ketQua.getLong("DonGia"),
                ketQua.getString("MoTa"),
                ketQua.getString("ThoiGianDuKien"),
                ketQua.getString("DieuKienApDung"),
                ketQua.getBoolean("DangApDung"));
    }

    private boolean cungThongTin(DichVu a, DichVu b) {
        return a.getMaDichVu().equals(b.getMaDichVu())
                && a.getTenDichVu().equals(b.getTenDichVu())
                && a.getNhomDichVu().equals(b.getNhomDichVu())
                && a.getDonViTinh().equals(b.getDonViTinh())
                && a.getDonGia() == b.getDonGia()
                && a.getMoTa().equals(b.getMoTa())
                && a.getThoiGianDuKien().equals(b.getThoiGianDuKien())
                && a.getDieuKienApDung().equals(b.getDieuKienApDung())
                && a.isDangApDung() == b.isDangApDung();
    }

    private void kiemTraQuyen(TaiKhoan taiKhoan) {
        if (taiKhoan == null || !new TaiKhoanDAO().phienConHopLe(taiKhoan)) {
            throw new SecurityException("Cần phiên đăng nhập còn hiệu lực.");
        }
        taiKhoan.kiemTraPhien();
        if (taiKhoan.getVaiTro() != VaiTro.QUAN_LY) {
            throw new SecurityException("Chỉ Quản lý được quản lý dịch vụ và bảng giá.");
        }
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
}
