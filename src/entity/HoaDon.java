package entity;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


/** Hóa đơn thanh toán được lập sau khi phiếu sửa chữa hoàn thành. */
public final class HoaDon {

    private final String maHoaDon;
    private final String maPhieuSuaChua;
    private final String tenKhachHang;
    private final long tongTien;
    private final String bienSo;
    private final String maGiaoDich;
    private final HinhThucThanhToan hinhThucThanhToan;
    private final LocalDateTime thoiDiemThanhToan;
    private final String nguoiThucHien;

    /** Tạo hóa đơn đã thanh toán. */
    public HoaDon(String maHoaDon, String maPhieuSuaChua, String tenKhachHang, long tongTien,
            HinhThucThanhToan hinhThucThanhToan, LocalDateTime thoiDiemThanhToan, String nguoiThucHien) {
        this(maHoaDon, maPhieuSuaChua, tenKhachHang, "", tongTien, hinhThucThanhToan, thoiDiemThanhToan, nguoiThucHien,
                "");
    }

    /** Giữ biển số và tham chiếu chuyển khoản trên chính hóa đơn đã thu. */
    public HoaDon(String maHoaDon, String maPhieuSuaChua, String tenKhachHang, String bienSo, long tongTien,
            HinhThucThanhToan hinhThucThanhToan, LocalDateTime thoiDiemThanhToan, String nguoiThucHien,
            String maGiaoDich) {
        if (bienSo == null || maGiaoDich == null) {
            throw new IllegalArgumentException("Thiếu thông tin hóa đơn.");
        }
        this.bienSo = bienSo.trim();
        this.maGiaoDich = maGiaoDich.trim();
        this.maHoaDon = requireNonBlank(maHoaDon, "Mã hóa đơn");
        this.maPhieuSuaChua = requireNonBlank(maPhieuSuaChua, "Mã phiếu sửa chữa");
        this.tenKhachHang = requireNonBlank(tenKhachHang, "Tên khách hàng");
        this.nguoiThucHien = requireNonBlank(nguoiThucHien, "Người thực hiện");
        this.tongTien = tongTien;
        if (hinhThucThanhToan == null) {
            throw new NullPointerException("Hình thức thanh toán không được null.");
        }
        this.hinhThucThanhToan = hinhThucThanhToan;
        if (thoiDiemThanhToan == null) {
            throw new NullPointerException("Thời điểm thanh toán không được null.");
        }
        this.thoiDiemThanhToan = thoiDiemThanhToan;

        if ((tongTien < 0L)) {
            throw new IllegalArgumentException("Tổng tiền không được âm.");
        }
    }

    /** Trả biển số tại thời điểm lập hóa đơn. */
    public String getBienSo() {
        return bienSo;
    }

    /** Trả mã tham chiếu chuyển khoản, rỗng với thanh toán tiền mặt. */
    public String getMaGiaoDich() {
        return maGiaoDich;
    }

    /** Trả về ma hoa don. */
    public String getMaHoaDon() {
        return maHoaDon;
    }

    /** Trả về ma phieu sua chua. */
    public String getMaPhieuSuaChua() {
        return maPhieuSuaChua;
    }

    /** Trả về ten khach hang. */
    public String getTenKhachHang() {
        return tenKhachHang;
    }

    /** Trả về tong tien. */
    public long getTongTien() {
        return tongTien;
    }

    /** Trả về hinh thuc thanh toan. */
    public HinhThucThanhToan getHinhThucThanhToan() {
        return hinhThucThanhToan;
    }

    /** Trả về thoi diem thanh toan. */
    public LocalDateTime getThoiDiemThanhToan() {
        return thoiDiemThanhToan;
    }

    /** Trả về nguoi thuc hien. */
    public String getNguoiThucHien() {
        return nguoiThucHien;
    }

    /** Kiểm tra dữ liệu người dùng nhập trước khi DAO ghi thanh toán. */
    public static String kiemTraThanhToan(long phaiThu, long tienNhan, HinhThucThanhToan hinhThuc,
            String maGiaoDich) {
        if (phaiThu < 0 || tienNhan < phaiThu) {
            throw new IllegalArgumentException("Số tiền nhận chưa đủ.");
        }
        if (hinhThuc == null) {
            throw new IllegalArgumentException("Cần chọn hình thức thanh toán.");
        }
        String thamChieu = maGiaoDich == null ? "" : maGiaoDich.trim();
        if (hinhThuc == HinhThucThanhToan.CHUYEN_KHOAN && thamChieu.isEmpty()) {
            throw new IllegalArgumentException("Nhập mã giao dịch chuyển khoản.");
        }
        return thamChieu;
    }

    /** Tạo mã hóa đơn từ số thứ tự đã được DAO khóa và đọc trong database. */
    public static String taoMa(int nam, int soThuTu) {
        if (nam < 1 || soThuTu < 1 || soThuTu > 9999) {
            throw new IllegalArgumentException("Số thứ tự hóa đơn không hợp lệ.");
        }
        return String.format("HD-%04d-%04d", nam, soThuTu);
    }

    /** Cộng tiền nguyên đồng và phát hiện dữ liệu âm hoặc tràn số. */
    public static long congTien(long hienTai, long them) {
        if (hienTai < 0 || them < 0 || Long.MAX_VALUE - hienTai < them) {
            throw new IllegalArgumentException("Tổng tiền vượt giới hạn.");
        }
        return hienTai + them;
    }

    /** Tính tổng các dòng hóa đơn/báo giá bằng quy tắc của entity. */
    public static long tinhTongChiTiet(List<ChiTietHoaDon> chiTiet) {
        return ChiTietHoaDon.tinhTong(chiTiet);
    }

    /** Kiểm tra snapshot chi tiết khớp tổng tiền trên hóa đơn. */
    public static void kiemTraTongChiTiet(long tongHoaDon, List<ChiTietHoaDon> chiTiet) {
        if (tongHoaDon != tinhTongChiTiet(chiTiet)) {
            throw new IllegalStateException("Chi tiết hóa đơn không khớp tổng phải thu.");
        }
    }

    /** Lọc hóa đơn theo kỳ và hình thức; DAO chỉ cung cấp danh sách đã truy vấn. */
    public static List<HoaDon> locTheoKy(List<HoaDon> danhSach, LocalDate tuNgay, LocalDate denNgay,
            HinhThucThanhToan hinhThuc) {
        if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) {
            throw new IllegalArgumentException("Từ ngày không được sau đến ngày.");
        }
        List<HoaDon> ketQua = new ArrayList<>();
        if (danhSach == null) {
            return ketQua;
        }
        for (HoaDon hoaDon : danhSach) {
            LocalDate ngay = hoaDon.getThoiDiemThanhToan().toLocalDate();
            if ((tuNgay == null || !ngay.isBefore(tuNgay)) && (denNgay == null || !ngay.isAfter(denNgay))
                    && (hinhThuc == null || hoaDon.getHinhThucThanhToan() == hinhThuc)) {
                ketQua.add(hoaDon);
            }
        }
        return ketQua;
    }

    /** Tính tổng các hóa đơn đã được entity lọc. */
    public static long tinhTong(List<HoaDon> danhSach) {
        long tong = 0;
        if (danhSach != null) {
            for (HoaDon hoaDon : danhSach) {
                tong = congTien(tong, hoaDon.getTongTien());
            }
        }
        return tong;
    }

    /** Thực hiện require non blank cho thành phần hiện tại. */
    private static String requireNonBlank(String value, String tenTruong) {
        if (value == null) {
            throw new NullPointerException(tenTruong + " không được null.");
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(tenTruong + " không được rỗng.");
        }
        return trimmed;
    }

    public enum HinhThucThanhToan {
    
        /** Khách trả bằng tiền mặt tại quầy. */
        TIEN_MAT("Tiền mặt"),
    
        /** Khách chuyển khoản ngân hàng, thu ngân đối chiếu trước khi xác nhận. */
        CHUYEN_KHOAN("Chuyển khoản");
    
        private final String tenHienThi;
    
        /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
        HinhThucThanhToan(String tenHienThi) {
            this.tenHienThi = tenHienThi;
        }
    
        /** Nhãn tiếng Việt hiển thị trên giao diện và bộ lọc tra cứu. */
        public String getTenHienThi() {
            return tenHienThi;
        }
    
        /** Tìm enum từ nhãn hiển thị; dùng khi ánh xạ lựa chọn JComboBox. */
        public static HinhThucThanhToan tuTenHienThi(String tenHienThi) {
            for (HinhThucThanhToan h : values()) {
                if (h.tenHienThi.equals(tenHienThi)) {
                    return h;
                }
            }
            return null;
        }
    }
}
