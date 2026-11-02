package entity;

import java.time.LocalDateTime;

/** Lưu người thực hiện, thời điểm và nội dung thay đổi tài khoản. */
public record NhatKyTaiKhoan(LocalDateTime thoiDiem, String nguoiThucHien,
        String maTaiKhoan, String hanhDong, String lyDo) {
}
