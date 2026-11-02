package entity;

/** Lưu một mặt hàng nhập, số lượng, đơn giá và thành tiền. */
public final class ChiTietNhapKho {
    private final PhuTung phuTung;
    private final double soLuong;
    private final long donGia;

    /** Tạo dòng nhập, kiểm tra số lượng, đơn giá và giới hạn thành tiền. */
    public ChiTietNhapKho(PhuTung phuTung, double soLuong, long donGia) {
        if (phuTung == null) { throw new IllegalArgumentException("Cần chọn phụ tùng."); }
        this.phuTung = new PhuTung(phuTung);
        phuTung.kiemTraSoLuong(soLuong);
        this.soLuong = soLuong;
        this.donGia = donGia;
        if (donGia < 0) { throw new IllegalArgumentException("Đơn giá nhập không được âm."); }
        getThanhTien();
    }

    /** Trả về thông tin phụ tùng được lưu trên dòng dữ liệu. */
    public PhuTung getPhuTung() { return phuTung; }

    /** Trả về số lượng nhập. */
    public double getSoLuong() { return soLuong; }

    /** Trả về đơn giá đã lưu. */
    public long getDonGia() { return donGia; }

    /** Tính tiền dòng nhập và làm tròn đến đồng gần nhất, đồng thời kiểm tra tràn số. */
    public long getThanhTien() {

        long luongNghin = Math.round(soLuong * 1000);
        long luongNguyen = luongNghin / 1000;
        long luongLe = luongNghin % 1000;
        if (luongNguyen > 0 && donGia > Long.MAX_VALUE / luongNguyen) {
            throw new IllegalArgumentException("Thành tiền vượt giới hạn cho phép.");
        }
        long tienNguyen = luongNguyen * donGia;
        long tienLe = (donGia / 1000) * luongLe + ((donGia % 1000) * luongLe + 500) / 1000;
        if (Long.MAX_VALUE - tienNguyen < tienLe) {
            throw new IllegalArgumentException("Thành tiền vượt giới hạn cho phép.");
        }
        return tienNguyen + tienLe;
    }
}
