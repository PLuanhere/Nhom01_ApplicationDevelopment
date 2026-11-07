package dao;

import connectDB.ConnectDB;
import entity.PhieuSuaChua;
import entity.PhieuSuaChua.TrangThaiSuaChua;
import entity.PhieuSuaChua.TrangThaiPhatSinh;
import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

// Đọc số liệu tổng quan và công việc hoàn tất của kỹ thuật từ SQL.
public final class TrangChuKyThuatDAO {
    // Giữ tên hàm đang được giao diện gọi nhưng trả dữ liệu SQL thay cho mẫu.
    public TongQuanKyThuat docDuLieuMau(TaiKhoan tk) {
        if (tk == null || tk.getVaiTro() != VaiTro.NHAN_VIEN_KY_THUAT
                || !new TaiKhoanDAO().phienConHopLe(tk)) {
            throw new SecurityException("Cần phiên đăng nhập kỹ thuật còn hiệu lực.");
        }
        tk.kiemTraPhien();
        LocalDate ngay = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        int dangSua = 0, choPhuTung = 0, choDuyet = 0;
        for (PhieuSuaChua p : new PhieuSuaChuaDAO().timKiem(tk, "", "Tất cả")) {
            if (p.getTrangThai() != TrangThaiSuaChua.HOAN_THANH) {
                if (p.getTrangThai() == TrangThaiSuaChua.CHO_PHU_TUNG) {
                    choPhuTung++;
                } else {
                    dangSua++;
                }
                if (p.getPhatSinh() != null && p.getPhatSinh().getTrangThai() == TrangThaiPhatSinh.CHO_DUYET) {
                    choDuyet++;
                }
            }
        }
        List<ThongKeNgayKyThuat> thongKe = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            thongKe.add(docThongKeNgay(tk, ngay.minusDays(i)));
        }
        String ten;
        try {
            Connection c = ConnectDB.getInstance().getConnection();
            try (PreparedStatement ps = c.prepareStatement("SELECT HoTen FROM dbo.NhanVien WHERE MaNhanVien=?")) {
                ps.setString(1, tk.getMaNhanVien());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalStateException("Không tìm thấy hồ sơ kỹ thuật viên.");
                    }
                    ten = rs.getString("HoTen");
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không đọc được tên kỹ thuật viên.", ex);
        }
        return new TongQuanKyThuat(ten, ngay, dangSua, choPhuTung, choDuyet, thongKe);
    }

    // Đếm các hạng mục dịch vụ đã hoàn tất trong ngày theo ba nhóm hiển thị hiện có.
    private ThongKeNgayKyThuat docThongKeNgay(TaiKhoan tk, LocalDate ngay) {
        int baoDuong = 0, coKhi = 0, dien = 0;
        String sql = "SELECT dv.NhomDichVu FROM dbo.PhieuSuaChua p "
                + "JOIN dbo.ChiTietSuaChua ct ON ct.MaPhieu=p.MaPhieu "
                + "JOIN dbo.ChiTietBaoGia bg ON bg.MaBaoGia=p.MaBaoGia AND bg.ViTri=ct.ViTri "
                + "JOIN dbo.DichVu dv ON dv.MaDichVu=bg.MaDichVu "
                + "WHERE p.TrangThai='HOAN_THANH' AND ct.TrangThai='HOAN_THANH' "
                + "AND ct.TaiKhoanThucHien=? AND p.ThoiDiemHoanTat>=? AND p.ThoiDiemHoanTat<?";
        try {
            Connection c = ConnectDB.getInstance().getConnection();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, tk.getMaNhanVien());
                ps.setTimestamp(2, Timestamp.valueOf(ngay.atStartOfDay()));
                ps.setTimestamp(3, Timestamp.valueOf(ngay.plusDays(1).atStartOfDay()));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String nhom = rs.getString("NhomDichVu").toLowerCase(java.util.Locale.ROOT);
                        if (nhom.contains("bảo dưỡng")) {
                            baoDuong++;
                        } else if (nhom.contains("điện") || nhom.contains("chẩn đoán")) {
                            dien++;
                        } else {
                            coKhi++;
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không đọc được thống kê theo ngày.", ex);
        }
        List<Integer> soLuong = new ArrayList<>();
        soLuong.add(baoDuong);
        soLuong.add(coKhi);
        soLuong.add(dien);
        return new ThongKeNgayKyThuat(ngay, soLuong);
    }

    public static final class ThongKeNgayKyThuat {
        private final LocalDate ngay;
        private final List<Integer> soLuongTheoNhom;

        // Khởi tạo dữ liệu và thành phần cần dùng của lớp.
        public ThongKeNgayKyThuat(LocalDate ngay, List<Integer> soLuongTheoNhom) {
            if (ngay == null || soLuongTheoNhom == null || soLuongTheoNhom.size() != 3) {
                throw new IllegalArgumentException("Cần ngày và số lượng của ba nhóm công việc.");
            }
            for (Integer soLuong : soLuongTheoNhom) {
                if (soLuong == null || soLuong < 0) {
                    throw new IllegalArgumentException("Số lượng công việc không được âm hoặc null.");
                }
            }
            this.ngay = ngay;
            this.soLuongTheoNhom = new ArrayList<Integer>(soLuongTheoNhom);
        }

        // Trả về ngay.
        public LocalDate getNgay() {
            return ngay;
        }

        // Trả về so luong theo nhom.
        public List<Integer> getSoLuongTheoNhom() {
            return new ArrayList<Integer>(soLuongTheoNhom);
        }

        // Cộng số lượng của ba nhóm để tính tổng công việc hoàn tất trong ngày.
        // Thực hiện tinh tong cong viec cho thành phần hiện tại.
        public int tinhTongCongViec() {
            int tong = 0;
            for (Integer soLuong : soLuongTheoNhom) {
                tong += soLuong;
            }
            return tong;
        }
    }

    public static final class TongQuanKyThuat {
        private final String tenKyThuatVien;
        private final LocalDate ngayBaoCao;
        private final int dangSua;
        private final int choPhuTung;
        private final int choKhachDuyet;
        private final List<ThongKeNgayKyThuat> thongKeNgay;

        // Khởi tạo dữ liệu và thành phần cần dùng của lớp.
        public TongQuanKyThuat(String tenKyThuatVien, LocalDate ngayBaoCao, int dangSua, int choPhuTung, int choKhachDuyet,
                List<ThongKeNgayKyThuat> thongKeNgay) {
            if (tenKyThuatVien == null || tenKyThuatVien.trim().isEmpty() || ngayBaoCao == null || thongKeNgay == null
                    || dangSua < 0 || choPhuTung < 0 || choKhachDuyet < 0) {
                throw new IllegalArgumentException("Thông tin tổng quan không hợp lệ.");
            }
            for (ThongKeNgayKyThuat thongKe : thongKeNgay) {
                if (thongKe == null) {
                    throw new IllegalArgumentException("Thống kê theo ngày không được null.");
                }
            }
            this.tenKyThuatVien = tenKyThuatVien.trim();
            this.ngayBaoCao = ngayBaoCao;
            this.dangSua = dangSua;
            this.choPhuTung = choPhuTung;
            this.choKhachDuyet = choKhachDuyet;
            this.thongKeNgay = new ArrayList<ThongKeNgayKyThuat>(thongKeNgay);
        }

        // Trả về ten ky thuat vien.
        public String getTenKyThuatVien() {
            return tenKyThuatVien;
        }

        // Trả về ngay bao cao.
        public LocalDate getNgayBaoCao() {
            return ngayBaoCao;
        }

        // Trả về dang sua.
        public int getDangSua() {
            return dangSua;
        }

        // Trả về cho phu tung.
        public int getChoPhuTung() {
            return choPhuTung;
        }

        // Trả về cho khach duyet.
        public int getChoKhachDuyet() {
            return choKhachDuyet;
        }

        // Trả về thong ke ngay.
        public List<ThongKeNgayKyThuat> getThongKeNgay() {
            return new ArrayList<ThongKeNgayKyThuat>(thongKeNgay);
        }

        // Cộng số công việc hoàn tất của các thống kê có ngày trùng với ngày báo cáo.
        // Thực hiện tinh hoan tat trong ngay cho thành phần hiện tại.
        public int tinhHoanTatTrongNgay() {
            int tong = 0;
            for (ThongKeNgayKyThuat thongKe : thongKeNgay) {
                if (thongKe.getNgay().equals(ngayBaoCao)) {
                    tong += thongKe.tinhTongCongViec();
                }
            }
            return tong;
        }
    }
}
