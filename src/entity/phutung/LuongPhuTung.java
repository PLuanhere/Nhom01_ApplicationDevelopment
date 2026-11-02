package entity.phutung;

/** Mã phụ tùng và lượng được duyệt hoặc thực dùng trong một danh sách. */
public final class LuongPhuTung {
    private final String maPhuTung;
    private final double soLuong;

    /** Tạo một dòng số lượng; quy tắc đơn vị được DAO kiểm tra với danh mục. */
    public LuongPhuTung(String maPhuTung, double soLuong) {
        if (maPhuTung == null || maPhuTung.trim().isEmpty() || !(soLuong >= 0 && soLuong < Long.MAX_VALUE / 1000.0)
                || soLuong != Math.round(soLuong * 1000) / 1000.0) {
            throw new IllegalArgumentException("Mã phụ tùng hoặc số lượng không hợp lệ.");
        }
        this.maPhuTung = maPhuTung.trim();
        this.soLuong = soLuong;
    }

    /** Trả về mã phụ tùng của dòng. */
    public String getMaPhuTung() {
        return maPhuTung;
    }

    /** Trả về số lượng, tối đa ba số lẻ. */
    public double getSoLuong() {
        return soLuong;
    }
}
