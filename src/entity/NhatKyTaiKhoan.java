package entity;

import java.time.LocalDateTime;

/** Nhật ký quản trị tài khoản; chỉ lưu hành động, không lưu mật khẩu hoặc hash. */
public record NhatKyTaiKhoan(LocalDateTime thoiDiem, String nguoiThucHien,
        String maTaiKhoan, String hanhDong, String lyDo) {
}
