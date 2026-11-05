package entity;

/** Lưu thông tin dịch vụ, đơn giá và trạng thái áp dụng. */
public final class DichVu {

    private final String maDichVu;
    private final String tenDichVu;
    private final String nhomDichVu;
    private final String donViTinh;
    private final long donGia;
    private final String moTa;
    private final String thoiGianDuKien;
    private final String dieuKienApDung;
    private final boolean dangApDung;

    /** Tạo dịch vụ và kiểm tra mã, tên, đơn vị tính, đơn giá. */
    public DichVu(String maDichVu, String tenDichVu, String nhomDichVu, String donViTinh, long donGia, String moTa,
            String thoiGianDuKien, String dieuKienApDung, boolean dangApDung) {
        this.maDichVu = batBuoc(maDichVu, "Mã dịch vụ");
        this.tenDichVu = batBuoc(tenDichVu, "Tên dịch vụ");
        this.nhomDichVu = nhomDichVu == null ? "" : nhomDichVu.trim();
        this.donViTinh = batBuoc(donViTinh, "Đơn vị tính");
        this.donGia = donGia;
        if (donGia < 0) {
            throw new IllegalArgumentException("Đơn giá không được âm.");
        }
        this.moTa = moTa == null ? "" : moTa.trim();
        this.thoiGianDuKien = thoiGianDuKien == null ? "" : thoiGianDuKien.trim();
        this.dieuKienApDung = dieuKienApDung == null ? "" : dieuKienApDung.trim();
        this.dangApDung = dangApDung;
    }

    /** Kiểm tra trường bắt buộc không rỗng và trả về nội dung đã bỏ khoảng trắng. */
    private static String batBuoc(String chu, String ten) {
        if (chu == null || chu.isBlank()) {
            throw new IllegalArgumentException(ten + " không được rỗng.");
        }
        return chu.trim();
    }

    /** Trả về mã dịch vụ. */
    public String getMaDichVu() {
        return maDichVu;
    }

    /** Trả về tên dịch vụ. */
    public String getTenDichVu() {
        return tenDichVu;
    }

    /** Trả về nhóm dịch vụ. */
    public String getNhomDichVu() {
        return nhomDichVu;
    }

    /** Trả về đơn vị tính. */
    public String getDonViTinh() {
        return donViTinh;
    }

    /** Trả về đơn giá đã lưu. */
    public long getDonGia() {
        return donGia;
    }

    /** Trả về mô tả dịch vụ. */
    public String getMoTa() {
        return moTa;
    }

    /** Trả về thời gian thực hiện dự kiến. */
    public String getThoiGianDuKien() {
        return thoiGianDuKien;
    }

    /** Trả về điều kiện áp dụng dịch vụ. */
    public String getDieuKienApDung() {
        return dieuKienApDung;
    }

    /** Cho biết dịch vụ có đang được áp dụng hay không. */
    public boolean isDangApDung() {
        return dangApDung;
    }

    /** Hiển thị mã và tên dịch vụ trong ô chọn danh mục. */
    @Override
    public String toString() {
        return maDichVu + " · " + tenDichVu;
    }
}
