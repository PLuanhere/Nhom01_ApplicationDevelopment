package entity;

import java.math.BigDecimal;
import java.util.Objects;

// Thông tin phụ tùng bất biến; tồn kho do DAO quản lý riêng, không sửa qua entity
public final class PhuTung {
    private final String maPhuTung;
    private final String tenPhuTung;
    private final String donViTinh;
    private final BigDecimal mucToiThieu;

    /**
     * Tạo thông tin danh mục phụ tùng.
     * @param maPhuTung mã không rỗng
     * @param tenPhuTung tên không rỗng
     * @param donViTinh đơn vị lít hoặc cái
     * @param mucToiThieu ngưỡng cảnh báo không âm
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ
     */
    public PhuTung(String maPhuTung, String tenPhuTung, String donViTinh, BigDecimal mucToiThieu) {
        this.maPhuTung = Objects.requireNonNull(maPhuTung).trim();
        this.tenPhuTung = Objects.requireNonNull(tenPhuTung).trim();
        this.donViTinh = Objects.requireNonNull(donViTinh).trim();
        this.mucToiThieu = Objects.requireNonNull(mucToiThieu);
        if (this.maPhuTung.isEmpty() || this.tenPhuTung.isEmpty()
                || !("lít".equals(donViTinh) || "cái".equals(donViTinh)) || mucToiThieu.signum() < 0) {
            throw new IllegalArgumentException("Thông tin phụ tùng không hợp lệ.");
        }
    }

    public String getMaPhuTung() {
        return maPhuTung;
    }

    public String getTenPhuTung() {
        return tenPhuTung;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public BigDecimal getMucToiThieu() {
        return mucToiThieu;
    }

    /**
     * Kiểm tra lượng theo đơn vị; phụ tùng tính bằng cái phải là số nguyên.
     * @param soLuong lượng cần kiểm tra
     * @throws IllegalArgumentException nếu lượng không dương hoặc sai đơn vị
     */
    // Kiểm tra số lượng dương và tính nguyên đối với phụ tùng đếm theo cái.
    public void kiemTraSoLuong(BigDecimal soLuong) {
        if (soLuong == null || soLuong.signum() <= 0
                || ("cái".equals(donViTinh) && soLuong.stripTrailingZeros().scale() > 0)) {
            throw new IllegalArgumentException(tenPhuTung + ": nhập số dương"
                    + ("cái".equals(donViTinh) ? " nguyên." : "."));
        }
    }

    /** {@inheritDoc} */
    // Trả nhãn mã và tên dùng trong danh sách chọn phụ tùng.
    @Override
    public String toString() {
        return maPhuTung + " · " + tenPhuTung;
    }
}