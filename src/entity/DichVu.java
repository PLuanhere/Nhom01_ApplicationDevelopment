package entity;

import java.math.BigDecimal;
import java.util.Objects;

/** Danh mục dịch vụ và đơn giá hiện hành; không thay đổi giá trên chứng từ đã lập. */
public final class DichVu {

    /** Thông tin theo REPORT; nhóm, thời gian dự kiến và điều kiện theo Figma Page 3. */
    private final String maDichVu;
    private final String tenDichVu;
    private final String nhomDichVu;
    private final String donViTinh;
    private final BigDecimal donGia;
    private final String moTa;
    private final String thoiGianDuKien;
    private final String dieuKienApDung;
    private final boolean dangApDung;

    /**
     * @param maDichVu mã không rỗng; tính duy nhất do DAO kiểm tra khi lưu
     * @param tenDichVu tên bắt buộc
     * @param nhomDichVu nhóm hiển thị, có thể trống
     * @param donViTinh đơn vị tính bắt buộc
     * @param donGia đơn giá không âm, dùng BigDecimal cho tiền
     * @param moTa mô tả tùy chọn
     * @param thoiGianDuKien thời gian/quy trình dự kiến, có thể trống
     * @param dieuKienApDung điều kiện áp dụng, có thể trống
     * @param dangApDung false nếu dịch vụ tạm dừng
     */
    public DichVu(String maDichVu, String tenDichVu, String nhomDichVu, String donViTinh,
            BigDecimal donGia, String moTa, String thoiGianDuKien, String dieuKienApDung, boolean dangApDung) {
        this.maDichVu = batBuoc(maDichVu, "Mã dịch vụ");
        this.tenDichVu = batBuoc(tenDichVu, "Tên dịch vụ");
        this.nhomDichVu = nhomDichVu == null ? "" : nhomDichVu.trim();
        this.donViTinh = batBuoc(donViTinh, "Đơn vị tính");
        this.donGia = Objects.requireNonNull(donGia, "Đơn giá không được null.");
        if (donGia.signum() < 0) { throw new IllegalArgumentException("Đơn giá không được âm."); }
        this.moTa = moTa == null ? "" : moTa.trim();
        this.thoiGianDuKien = thoiGianDuKien == null ? "" : thoiGianDuKien.trim();
        this.dieuKienApDung = dieuKienApDung == null ? "" : dieuKienApDung.trim();
        this.dangApDung = dangApDung;
    }

    /** @return nội dung bắt buộc đã bỏ khoảng trắng hai đầu */
    private static String batBuoc(String chu, String ten) {
        if (chu == null || chu.isBlank()) { throw new IllegalArgumentException(ten + " không được rỗng."); }
        return chu.trim();
    }

    /** @return mã danh mục */
    public String getMaDichVu() { return maDichVu; }

    /** @return tên dịch vụ */
    public String getTenDichVu() { return tenDichVu; }

    /** @return nhóm dịch vụ */
    public String getNhomDichVu() { return nhomDichVu; }

    /** @return đơn vị tính tiền công/dịch vụ */
    public String getDonViTinh() { return donViTinh; }

    /** @return đơn giá hiện hành */
    public BigDecimal getDonGia() { return donGia; }

    /** @return mô tả, có thể rỗng */
    public String getMoTa() { return moTa; }

    /** @return thời gian hoặc quy trình dự kiến */
    public String getThoiGianDuKien() { return thoiGianDuKien; }

    /** @return điều kiện áp dụng dịch vụ */
    public String getDieuKienApDung() { return dieuKienApDung; }

    /** @return dịch vụ có đang được áp dụng hay không */
    public boolean isDangApDung() { return dangApDung; }
}
