package entity;

import java.time.LocalDate;

/** Lưu hồ sơ, bộ phận và trạng thái làm việc của nhân viên. */
public final class NhanVien {

    private final String maNhanVien;
    private final String hoTen;
    private final LocalDate ngaySinh;
    private final String soDienThoai;
    private final String gioiTinh;
    private final String diaChi;

    private final int soNamKinhNghiem;
    private final BoPhanNhanVien boPhan;
    private final TrangThaiNhanVien trangThai;

    /** Tạo hồ sơ nhân viên và kiểm tra các thông tin bắt buộc. */
    public NhanVien(String maNhanVien, String hoTen, LocalDate ngaySinh, String soDienThoai,
        int soNamKinhNghiem, String gioiTinh, String diaChi,
        BoPhanNhanVien boPhan, TrangThaiNhanVien trangThai) {
        if (maNhanVien == null) { throw new IllegalArgumentException("Thiếu maNhanVien."); }
        this.maNhanVien = maNhanVien.trim();
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
        if (boPhan == null) { throw new IllegalArgumentException("Thiếu boPhan."); }
        this.boPhan = boPhan;
        if (trangThai == null) { throw new IllegalArgumentException("Thiếu trangThai."); }
        this.trangThai = trangThai;
    }

    /** Kiểm tra họ tên có nội dung sau khi bỏ khoảng trắng. */
    public static boolean hoTenHopLe(String hoTen) {
        return hoTen != null && !hoTen.isBlank();
    }

    /** Kiểm tra ngày sinh hợp lệ và nhân viên đã đủ 18 tuổi. */
    public static boolean ngaySinhHopLe(LocalDate ngaySinh) {
        return ngaySinh != null && !ngaySinh.isAfter(LocalDate.now().minusYears(18));
    }

    /** Kiểm tra số điện thoại có từ 10 đến 11 chữ số. */
    public static boolean dienThoaiHopLe(String soDienThoai) {
        if (soDienThoai == null) { return false; }
        String so = soDienThoai.trim();
        if (so.length() < 10 || so.length() > 11) { return false; }
        for (int i = 0; i < so.length(); i++) {
            if (so.charAt(i) < '0' || so.charAt(i) > '9') { return false; }
        }
        return true;
    }

    /** Kiểm tra số năm kinh nghiệm không âm. */
    public static boolean kinhNghiemHopLe(int soNam) {
        return soNam >= 0;
    }

    /** Trả về mã nhân viên. */
    public String getMaNhanVien() {
        return maNhanVien;
    }

    /** Trả về họ tên nhân viên. */
    public String getHoTen() {
        return hoTen;
    }

    /** Trả về ngày sinh. */
    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    /** Trả về số điện thoại. */
    public String getSoDienThoai() {
        return soDienThoai;
    }

    /** Trả về giới tính. */
    public String getGioiTinh() {
        return gioiTinh;
    }

    /** Trả về địa chỉ. */
    public String getDiaChi() {
        return diaChi;
    }

    /** Trả về số năm kinh nghiệm. */
    public int getSoNamKinhNghiem() {
        return soNamKinhNghiem;
    }

    /** Trả về bộ phận làm việc. */
    public BoPhanNhanVien getBoPhan() {
        return boPhan;
    }

    /** Trả về trạng thái làm việc. */
    public TrangThaiNhanVien getTrangThai() {
        return trangThai;
    }
}
