package dao;

import connectDB.ConnectDB;
import entity.BaoGia.ChiTietBaoGia;
import entity.PhuTung;
import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Lưu và đọc phiếu nhập kho bằng transaction do SQL Server quản lý. */
public final class PhieuNhapKhoDAO {
    private final PhuTungDAO kho;

    PhieuNhapKhoDAO(PhuTungDAO kho) {
        if (kho == null) {
            throw new IllegalArgumentException("Thiếu nguồn kho.");
        }
        this.kho = kho;
    }

    /** Trả mã dự kiến; mã chính thức được procedure cấp khi lưu. */
    public String maTiepTheo(TaiKhoan taiKhoan) {
        kiemTraQuyen(taiKhoan);
        String sql = "SELECT CONCAT('NK-', YEAR(dbo.fn_ThoiDiemGarage()), '-', "
                + "ISNULL(CONVERT(BIGINT, current_value) + CONVERT(BIGINT, increment), "
                + "CONVERT(BIGINT, start_value))) FROM sys.sequences "
                + "WHERE object_id = OBJECT_ID('dbo.SoMaChungTu')";
        try (PreparedStatement lenh = ketNoi().prepareStatement(sql);
                ResultSet ketQua = lenh.executeQuery()) {
            if (ketQua.next()) {
                return ketQua.getString(1);
            }
            return "NK-TỰ-SINH";
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể đọc mã phiếu nhập dự kiến.", ex);
        }
    }

    /** Lưu phiếu với mã thao tác mới. */
    public PhieuNhapKho ghiNhan(TaiKhoan taiKhoan, String maDuKien, String nguon,
            String chungTu, List<ChiTietNhapKho> chiTiet) {
        String maThaoTac = "NK-" + UUID.randomUUID();
        return ghiNhan(taiKhoan, maDuKien, nguon, chungTu, chiTiet, maThaoTac);
    }

    /** Lưu phiếu idempotent; procedure đồng thời ghi chi tiết, tồn và nhật ký. */
    public PhieuNhapKho ghiNhan(TaiKhoan taiKhoan, String maDuKien, String nguon,
            String chungTu, List<ChiTietNhapKho> chiTiet, String maThaoTac) {
        kiemTraQuyen(taiKhoan);
        if (maDuKien == null || maDuKien.isBlank()) {
            throw new IllegalArgumentException("Thiếu mã phiếu nhập dự kiến.");
        }
        if (nguon == null || nguon.isBlank()) {
            throw new IllegalArgumentException("Nhập nguồn hàng / diễn giải.");
        }
        if (chiTiet == null || chiTiet.isEmpty()) {
            throw new IllegalArgumentException("Phiếu nhập cần ít nhất một mặt hàng.");
        }
        if (maThaoTac == null || maThaoTac.isBlank()) {
            throw new IllegalArgumentException("Thiếu mã thao tác nhập kho.");
        }

        String json = taoJsonChiTiet(chiTiet);
        String maPhieu;
        String sql = "{call dbo.usp_NhapKho(?, ?, ?, ?, ?)}";
        try (CallableStatement lenh = ketNoi().prepareCall(sql)) {
            lenh.setString(1, taiKhoan.getMaNhanVien());
            lenh.setString(2, nguon.trim());
            lenh.setString(3, chungTu == null ? "" : chungTu.trim());
            lenh.setString(4, json);
            lenh.setString(5, maThaoTac.trim());
            try (ResultSet ketQua = lenh.executeQuery()) {
                if (!ketQua.next()) {
                    throw new IllegalStateException("SQL không trả mã phiếu nhập.");
                }
                maPhieu = ketQua.getString("MaPhieu");
            }
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể ghi nhận nhập kho.", ex);
        }
        return timTheoMa(taiKhoan, maPhieu);
    }

    /** Đọc toàn bộ phiếu nhập đã lưu, mới nhất trước. */
    public List<PhieuNhapKho> layDanhSach(TaiKhoan taiKhoan) {
        kiemTraQuyen(taiKhoan);
        String sql = "SELECT nk.MaPhieu, nk.NgayNhap, nk.NguonNhap, nk.ChungTuNguon, nk.NguoiNhap, "
                + "ct.ViTri, ct.SoLuong, ct.DonGia, pt.* "
                + "FROM dbo.PhieuNhapKho nk "
                + "JOIN dbo.ChiTietNhapKho ct ON ct.MaPhieu = nk.MaPhieu "
                + "JOIN dbo.PhuTung pt ON pt.MaPhuTung = ct.MaPhuTung "
                + "ORDER BY nk.NgayNhap DESC, nk.MaPhieu DESC, ct.ViTri";
        try (PreparedStatement lenh = ketNoi().prepareStatement(sql);
                ResultSet ketQua = lenh.executeQuery()) {
            return docDanhSach(ketQua);
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể đọc danh sách phiếu nhập.", ex);
        }
    }

    /** Tìm một phiếu nhập theo mã. */
    public PhieuNhapKho timTheoMa(TaiKhoan taiKhoan, String maPhieu) {
        kiemTraQuyen(taiKhoan);
        if (maPhieu == null || maPhieu.isBlank()) {
            throw new IllegalArgumentException("Thiếu mã phiếu nhập.");
        }
        String sql = "SELECT nk.MaPhieu, nk.NgayNhap, nk.NguonNhap, nk.ChungTuNguon, nk.NguoiNhap, "
                + "ct.ViTri, ct.SoLuong, ct.DonGia, pt.* "
                + "FROM dbo.PhieuNhapKho nk "
                + "JOIN dbo.ChiTietNhapKho ct ON ct.MaPhieu = nk.MaPhieu "
                + "JOIN dbo.PhuTung pt ON pt.MaPhuTung = ct.MaPhuTung "
                + "WHERE nk.MaPhieu = ? ORDER BY ct.ViTri";
        try (PreparedStatement lenh = ketNoi().prepareStatement(sql)) {
            lenh.setString(1, maPhieu.trim());
            try (ResultSet ketQua = lenh.executeQuery()) {
                List<PhieuNhapKho> danhSach = docDanhSach(ketQua);
                if (!danhSach.isEmpty()) {
                    return danhSach.get(0);
                }
            }
            throw new IllegalArgumentException("Không tìm thấy lần nhập kho.");
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể đọc phiếu nhập.", ex);
        }
    }

    private List<PhieuNhapKho> docDanhSach(ResultSet ketQua) throws SQLException {
        List<PhieuNhapKho> danhSach = new ArrayList<>();
        String maDangDoc = null;
        LocalDateTime ngayNhap = null;
        String nguonNhap = "";
        String chungTu = "";
        String nguoiNhap = "";
        List<ChiTietNhapKho> cacDong = new ArrayList<>();

        while (ketQua.next()) {
            String maPhieu = ketQua.getString("MaPhieu");
            if (maDangDoc != null && !maDangDoc.equals(maPhieu)) {
                danhSach.add(new PhieuNhapKho(
                        maDangDoc,
                        ngayNhap,
                        nguonNhap,
                        chungTu,
                        nguoiNhap,
                        cacDong));
                cacDong = new ArrayList<>();
            }
            maDangDoc = maPhieu;
            ngayNhap = ketQua.getTimestamp("NgayNhap").toLocalDateTime();
            nguonNhap = ketQua.getString("NguonNhap");
            chungTu = ketQua.getString("ChungTuNguon");
            nguoiNhap = ketQua.getString("NguoiNhap");
            PhuTung phuTung = docPhuTung(ketQua);
            cacDong.add(new ChiTietNhapKho(
                    phuTung,
                    ketQua.getDouble("SoLuong"),
                    ketQua.getLong("DonGia")));
        }
        if (maDangDoc != null) {
            danhSach.add(new PhieuNhapKho(
                    maDangDoc,
                    ngayNhap,
                    nguonNhap,
                    chungTu,
                    nguoiNhap,
                    cacDong));
        }
        return danhSach;
    }

    private PhuTung docPhuTung(ResultSet ketQua) throws SQLException {
        PhuTung phuTung = new PhuTung(
                ketQua.getString("MaPhuTung"),
                ketQua.getString("TenPhuTung"),
                ketQua.getString("LoaiPhuTung"),
                ketQua.getString("DonViTinh"),
                ketQua.getLong("GiaNhap"),
                ketQua.getLong("GiaBan"),
                ketQua.getDouble("MucToiThieu"),
                ketQua.getBoolean("HoatDong"));
        phuTung.setSoLuongTon(ketQua.getDouble("SoLuongTon"));
        return phuTung;
    }

    private String taoJsonChiTiet(List<ChiTietNhapKho> chiTiet) {
        StringBuilder json = new StringBuilder("[");
        List<PhuTung> danhMuc = kho.danhMucNoiBo();
        for (int i = 0; i < chiTiet.size(); i++) {
            ChiTietNhapKho dong = chiTiet.get(i);
            if (dong == null) {
                throw new IllegalArgumentException("Dòng nhập không được rỗng.");
            }
            PhuTung hienTai = timPhuTung(
                    danhMuc,
                    dong.getPhuTung().getMaPhuTung());
            if (!kho.cungThongTin(hienTai, dong.getPhuTung())) {
                throw new IllegalArgumentException(
                        "Danh mục đã thay đổi. Mở sửa dòng hàng rồi kiểm tra lại.");
            }
            if (!hienTai.isHoatDong()) {
                throw new IllegalArgumentException(
                        "Phụ tùng đã ngừng hoạt động: " + hienTai.getMaPhuTung());
            }
            if (i > 0) {
                json.append(',');
            }
            json.append("{\"MaPhuTung\":\"");
            json.append(thoatJson(hienTai.getMaPhuTung()));
            json.append("\",\"SoLuong\":");
            json.append(BigDecimal.valueOf(dong.getSoLuong()).stripTrailingZeros().toPlainString());
            json.append(",\"DonGia\":");
            json.append(dong.getDonGia());
            json.append('}');
        }
        json.append(']');
        return json.toString();
    }

    private PhuTung timPhuTung(List<PhuTung> danhMuc, String maPhuTung) {
        for (PhuTung phuTung : danhMuc) {
            if (phuTung.getMaPhuTung().equals(maPhuTung)) {
                return phuTung;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy phụ tùng: " + maPhuTung);
    }

    private String thoatJson(String chu) {
        return chu.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void kiemTraQuyen(TaiKhoan taiKhoan) {
        if (taiKhoan == null || !new TaiKhoanDAO().phienConHopLe(taiKhoan)) {
            throw new SecurityException("Cần phiên đăng nhập còn hiệu lực.");
        }
        taiKhoan.kiemTraPhien();
        if (taiKhoan.getVaiTro() != VaiTro.QUAN_LY) {
            throw new SecurityException("Chỉ Quản lý được ghi nhận nhập kho.");
        }
    }

    private Connection ketNoi() {
        try {
            return ConnectDB.getInstance().getConnection();
        } catch (SQLException ex) {
            throw loiDuLieu("Không thể kết nối database QuanLiGarage.", ex);
        }
    }

    private static IllegalStateException loiDuLieu(String thongBao, SQLException ex) {
        return new IllegalStateException(thongBao + " " + ex.getMessage(), ex);
    }

    /** Một dòng phụ tùng trên phiếu nhập. */
    public static final class ChiTietNhapKho {
        private final PhuTung phuTung;
        private final double soLuong;
        private final long donGia;

        public ChiTietNhapKho(PhuTung phuTung, double soLuong, long donGia) {
            if (phuTung == null) {
                throw new IllegalArgumentException("Cần chọn phụ tùng.");
            }
            this.phuTung = new PhuTung(phuTung);
            phuTung.kiemTraSoLuong(soLuong);
            this.soLuong = soLuong;
            if (donGia < 0) {
                throw new IllegalArgumentException("Đơn giá nhập không được âm.");
            }
            this.donGia = donGia;
            getThanhTien();
        }

        public PhuTung getPhuTung() {
            return new PhuTung(phuTung);
        }

        public double getSoLuong() {
            return soLuong;
        }

        public long getDonGia() {
            return donGia;
        }

        public long getThanhTien() {
            return ChiTietBaoGia.tinhThanhTien(soLuong, donGia);
        }
    }

    /** Header và các dòng của một phiếu nhập đã ghi nhận. */
    public static final class PhieuNhapKho {
        private final String maPhieu;
        private final LocalDateTime ngayNhap;
        private final String nguonNhap;
        private final String chungTuNguon;
        private final String nguoiNhap;
        private final List<ChiTietNhapKho> chiTiet;

        public PhieuNhapKho(String maPhieu, LocalDateTime ngayNhap, String nguonNhap,
                String chungTuNguon, String nguoiNhap, List<ChiTietNhapKho> chiTiet) {
            this.maPhieu = batBuoc(maPhieu, "Mã phiếu");
            if (ngayNhap == null) {
                throw new IllegalArgumentException("Thiếu ngày nhập.");
            }
            this.ngayNhap = ngayNhap;
            this.nguonNhap = batBuoc(nguonNhap, "Nguồn nhập");
            this.chungTuNguon = chungTuNguon == null ? "" : chungTuNguon.trim();
            this.nguoiNhap = batBuoc(nguoiNhap, "Người nhập");
            if (chiTiet == null || chiTiet.isEmpty()) {
                throw new IllegalArgumentException("Phiếu nhập cần ít nhất một mặt hàng.");
            }
            this.chiTiet = new ArrayList<>(chiTiet);
            getTongTien();
        }

        private static String batBuoc(String chu, String ten) {
            if (chu == null || chu.isBlank()) {
                throw new IllegalArgumentException(ten + " không được rỗng.");
            }
            return chu.trim();
        }

        public String getMaPhieu() {
            return maPhieu;
        }

        public LocalDateTime getNgayNhap() {
            return ngayNhap;
        }

        public String getNguonNhap() {
            return nguonNhap;
        }

        public String getChungTuNguon() {
            return chungTuNguon;
        }

        public String getNguoiNhap() {
            return nguoiNhap;
        }

        public List<ChiTietNhapKho> getChiTiet() {
            return new ArrayList<>(chiTiet);
        }

        public long getTongTien() {
            long tong = 0;
            for (ChiTietNhapKho dong : chiTiet) {
                if (Long.MAX_VALUE - tong < dong.getThanhTien()) {
                    throw new IllegalArgumentException("Tổng tiền vượt giới hạn cho phép.");
                }
                tong = tong + dong.getThanhTien();
            }
            return tong;
        }
    }
}
