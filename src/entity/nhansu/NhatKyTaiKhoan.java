package entity.nhansu;

import java.time.LocalDateTime;

/** Lưu thời điểm, người thực hiện và nội dung thay đổi tài khoản. */
public final class NhatKyTaiKhoan {
    private final LocalDateTime thoiDiem;
    private final String nguoiThucHien;
    private final String maTaiKhoan;
    private final String hanhDong;
    private final String lyDo;

    /** Tạo thông tin nhật ký sau một thao tác tài khoản. */
    public NhatKyTaiKhoan(LocalDateTime thoiDiem, String nguoiThucHien, String maTaiKhoan, String hanhDong,
            String lyDo) {
        this.thoiDiem = thoiDiem;
        this.nguoiThucHien = nguoiThucHien;
        this.maTaiKhoan = maTaiKhoan;
        this.hanhDong = hanhDong;
        this.lyDo = lyDo;
    }

    /** Trả về thoiDiem. */
    public LocalDateTime getThoiDiem() {
        return thoiDiem;
    }

    /** Trả về nguoiThucHien. */
    public String getNguoiThucHien() {
        return nguoiThucHien;
    }

    /** Trả về maTaiKhoan. */
    public String getMaTaiKhoan() {
        return maTaiKhoan;
    }

    /** Trả về hanhDong. */
    public String getHanhDong() {
        return hanhDong;
    }

    /** Trả về lyDo. */
    public String getLyDo() {
        return lyDo;
    }
}
