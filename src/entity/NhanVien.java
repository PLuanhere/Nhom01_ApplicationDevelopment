package entity;

import java.time.LocalDate;
import java.util.Objects;

/** Hồ sơ bất biến; kiểm tra dữ liệu nội tại, không chứa UI hoặc thông tin đăng nhập. */
public final class NhanVien {

    /** Thông tin định danh và liên hệ. */
    private final String maNhanVien;
    private final String hoTen;
    private final LocalDate ngaySinh;
    private final String soDienThoai;
    private final String gioiTinh;
    private final String diaChi;
    /** Thông tin công việc, không quyết định quyền của tài khoản. */
    private final int soNamKinhNghiem;
    private final BoPhanNhanVien boPhan;
    private final TrangThaiNhanVien trangThai;

    /**
     * Tạo hồ sơ sau khi kiểm tra các ràng buộc của REPORT.
     * @param maNhanVien mã duy nhất do DAO cấp
     * @param hoTen tên bắt buộc
     * @param ngaySinh ngày sinh, nhân viên phải đủ 18 tuổi
     * @param soDienThoai chuỗi 10–11 chữ số
     * @param soNamKinhNghiem số năm nguyên không âm
     * @param gioiTinh Nam hoặc Nữ
     * @param diaChi địa chỉ, có thể trống
     * @param boPhan bộ phận làm việc
     * @param trangThai tình trạng làm việc
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ
     * @throws NullPointerException nếu thiếu mã, bộ phận hoặc trạng thái
     */
    public NhanVien(String maNhanVien, String hoTen, LocalDate ngaySinh, String soDienThoai,
            int soNamKinhNghiem, String gioiTinh, String diaChi,
            BoPhanNhanVien boPhan, TrangThaiNhanVien trangThai) {
        this.maNhanVien = Objects.requireNonNull(maNhanVien).trim();
        if (this.maNhanVien.isEmpty()) {
            throw new IllegalArgumentException("Mã nhân viên không được rỗng.");
        }
        if (!hoTenHopLe(hoTen)) {
            throw new IllegalArgumentException("Vui lòng nhập họ và tên.");
        }
        if (!ngaySinhHopLe(ngaySinh)) {
            throw new IllegalArgumentException("Ngày sinh phải hợp lệ và nhân viên phải đủ 18 tuổi.");
        }
        if (!dienThoaiHopLe(soDienThoai)) {
            throw new IllegalArgumentException("Số điện thoại phải gồm 10–11 chữ số.");
        }
        if (!kinhNghiemHopLe(soNamKinhNghiem)) {
            throw new IllegalArgumentException("Số năm kinh nghiệm không được âm.");
        }
        if (!"Nam".equals(gioiTinh) && !"Nữ".equals(gioiTinh)) {
            throw new IllegalArgumentException("Giới tính phải là Nam hoặc Nữ.");
        }
        this.hoTen = hoTen.trim();
        this.ngaySinh = ngaySinh;
        this.soDienThoai = soDienThoai.trim();
        this.soNamKinhNghiem = soNamKinhNghiem;
        this.gioiTinh = gioiTinh;
        this.diaChi = diaChi == null ? "" : diaChi.trim();
        this.boPhan = Objects.requireNonNull(boPhan, "Cần chọn bộ phận.");
        this.trangThai = Objects.requireNonNull(trangThai, "Cần chọn trạng thái làm việc.");
    }

    /**
     * @param hoTen tên nhập vào
     * @return true nếu tên không trống
     */
    public static boolean hoTenHopLe(String hoTen) {
        return hoTen != null && !hoTen.isBlank();
    }

    /**
     * @param ngaySinh ngày sinh
     * @return true nếu có ngày sinh và đủ 18 tuổi
     */
    public static boolean ngaySinhHopLe(LocalDate ngaySinh) {
        return ngaySinh != null && !ngaySinh.isAfter(LocalDate.now().minusYears(18));
    }

    /**
     * @param soDienThoai số liên hệ
     * @return true nếu gồm 10–11 chữ số
     */
    public static boolean dienThoaiHopLe(String soDienThoai) {
        return soDienThoai != null && soDienThoai.trim().matches("[0-9]{10,11}");
    }

    /**
     * @param soNam số năm kinh nghiệm
     * @return true nếu số năm không âm
     */
    public static boolean kinhNghiemHopLe(int soNam) {
        return soNam >= 0;
    }

    /** @return mã nhân viên */
    public String getMaNhanVien() {
        return maNhanVien;
    }

    /** @return họ và tên */
    public String getHoTen() {
        return hoTen;
    }

    /** @return ngày sinh */
    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    /** @return số điện thoại gồm các chữ số */
    public String getSoDienThoai() {
        return soDienThoai;
    }

    /** @return giới tính Nam hoặc Nữ */
    public String getGioiTinh() {
        return gioiTinh;
    }

    /** @return địa chỉ, có thể rỗng */
    public String getDiaChi() {
        return diaChi;
    }

    /** @return số năm kinh nghiệm không âm */
    public int getSoNamKinhNghiem() {
        return soNamKinhNghiem;
    }

    /** @return bộ phận làm việc */
    public BoPhanNhanVien getBoPhan() {
        return boPhan;
    }

    /** @return tình trạng làm việc */
    public TrangThaiNhanVien getTrangThai() {
        return trangThai;
    }
}
