package entity;

import java.time.LocalDateTime;

/** Lưu tồn trước, tồn sau và thông tin của một lần nhập, xuất hoặc điều chỉnh. */
public final class BienDongKho {
    private final PhuTung phuTung;
    private final LocalDateTime thoiDiem;
    private final String loai;
    private final double tonTruoc;
    private final double tonSau;
    private final String chungTu;
    private final String nguoiThucHien;
    private final String lyDo;

    /** Tạo bản ghi biến động và kiểm tra tồn trước, tồn sau. */
    public BienDongKho(PhuTung phuTung, LocalDateTime thoiDiem, String loai,
        double tonTruoc, double tonSau, String chungTu, String nguoiThucHien, String lyDo) {
        if (phuTung == null) { throw new IllegalArgumentException("Cần chọn phụ tùng."); }
        if (thoiDiem == null) { throw new IllegalArgumentException("Thiếu thoiDiem."); }
        if (loai == null) { throw new IllegalArgumentException("Thiếu loai."); }
        if (chungTu == null) { throw new IllegalArgumentException("Thiếu chungTu."); }
        if (nguoiThucHien == null) { throw new IllegalArgumentException("Thiếu nguoiThucHien."); }
        if (lyDo == null) { throw new IllegalArgumentException("Thiếu lyDo."); }
        this.phuTung = new PhuTung(phuTung);
        this.thoiDiem = thoiDiem;
        this.loai = loai;
        this.tonTruoc = tonTruoc;
        this.tonSau = tonSau;
        this.chungTu = chungTu;
        this.nguoiThucHien = nguoiThucHien;
        this.lyDo = lyDo;
        if (!(tonTruoc >= 0 && tonTruoc <= Double.MAX_VALUE)
            || !(tonSau >= 0 && tonSau <= Double.MAX_VALUE) || tonTruoc == tonSau) {
            throw new IllegalArgumentException("Biến động phải có chênh lệch và không được làm âm tồn.");
        }
    }

    /** Trả về thông tin phụ tùng được lưu trên dòng dữ liệu. */
    public PhuTung getPhuTung() { return phuTung; }

    /** Trả về thời điểm ghi nhận biến động. */
    public LocalDateTime getThoiDiem() { return thoiDiem; }

    /** Trả về loại biến động kho. */
    public String getLoai() { return loai; }

    /** Trả về số lượng tồn trước khi thay đổi. */
    public double getTonTruoc() { return tonTruoc; }

    /** Trả về số lượng tồn sau khi thay đổi. */
    public double getTonSau() { return tonSau; }

    /** Tính chênh lệch tồn sau và tồn trước, giữ tối đa 3 chữ số lẻ. */
    public double getChenhLech() { return Math.round((tonSau - tonTruoc) * 1000) / 1000.0; }

    /** Trả về mã chứng từ liên quan. */
    public String getChungTu() { return chungTu; }

    /** Trả về tài khoản thực hiện thao tác. */
    public String getNguoiThucHien() { return nguoiThucHien; }

    /** Trả về lý do thay đổi. */
    public String getLyDo() { return lyDo; }
}
