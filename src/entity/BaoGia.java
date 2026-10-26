package entity;

import java.util.ArrayList;
import java.util.List;

// Bản đọc báo giá mẫu của nhánh sửa chữa
public final class BaoGia {
    private final String maBaoGia;
    private final String bienSo;
    private final boolean daDongY;
    private final List<ChiTietBaoGia> chiTiet;

    public BaoGia(String maBaoGia, String bienSo, boolean daDongY, List<ChiTietBaoGia> chiTiet) {
        if (maBaoGia == null || bienSo == null || maBaoGia.isBlank() || bienSo.isBlank()) {
            throw new IllegalArgumentException("Thông tin báo giá không hợp lệ.");
        }
        this.maBaoGia = maBaoGia;
        this.bienSo = bienSo;
        this.daDongY = daDongY;
        this.chiTiet = new ArrayList<ChiTietBaoGia>(chiTiet);
    }

    public String getMaBaoGia() { return maBaoGia; }
    public String getBienSo() { return bienSo; }
    public List<ChiTietBaoGia> getChiTiet() { return new ArrayList<ChiTietBaoGia>(chiTiet); }
    public boolean isDaDongY() { return daDongY; }

    /** {@inheritDoc} */
    // Hiển thị mã báo giá và trạng thái khách xác nhận trong danh sách chọn.
    @Override
    public String toString() {
        return maBaoGia + (daDongY ? " · Khách đồng ý" : " · Chưa được duyệt");
    }
}