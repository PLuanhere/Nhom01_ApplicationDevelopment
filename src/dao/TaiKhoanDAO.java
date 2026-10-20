package dao;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import entity.TaiKhoan;
import entity.VaiTro;

/* Quản lý và xác thực bốn tài khoản được tạo sẵn trong code; chưa kết nối database */
public final class TaiKhoanDAO {

    private static final int SO_VONG_BAM = 100_000;
    private static final byte[] SALT = Base64.getDecoder().decode("WxicU03syFGfMza7C+tuKA==");
    private static final byte[] MAT_KHAU_DA_BAM = Base64.getDecoder().decode(
            "HDg+XvXmvAPJjeZgQLmrPozADDpa/2XOT/PsBNcN+vo=");
    /* Dữ liệu tài khoản hiện tại; mỗi tài khoản có một vai trò và không cho sửa từ UI */
    private static final List<TaiKhoan> DANH_SACH_TAI_KHOAN = List.of(
            new TaiKhoan("quanly", VaiTro.QUAN_LY),
            new TaiKhoan("tiepnhan", VaiTro.NHAN_VIEN_TIEP_NHAN),
            new TaiKhoan("kythuat", VaiTro.NHAN_VIEN_KY_THUAT),
            new TaiKhoan("thungan", VaiTro.THU_NGAN));

    /* Tạo đối tượng truy cập danh sách tài khoản có sẵn */
    public TaiKhoanDAO() {
    }

    /*Kiểm tra tên đăng nhập và mật khẩu, chỉ trả danh tính khi xác thực thành công*/
    public Optional<TaiKhoan> dangNhap(String tenDangNhap, char[] matKhau) {
        Objects.requireNonNull(tenDangNhap, "Tên đăng nhập không được null.");
        Objects.requireNonNull(matKhau, "Mật khẩu không được null.");
        if (matKhau.length == 0) {
            return Optional.empty();
        }
        // Chỉ trả tài khoản trong danh sách khi cả tên và mật khẩu đều đúng.
        for (TaiKhoan taiKhoan : DANH_SACH_TAI_KHOAN) {
            if (taiKhoan.getTenDangNhap().equals(tenDangNhap.trim()) && kiemTraMatKhau(matKhau)) {
                return Optional.of(taiKhoan);
            }
        }
        return Optional.empty();
    }

    /**
     * Băm mật khẩu vừa nhập rồi so sánh với kết quả đã lưu, không tự viết thuật toán băm.
     * @param matKhau mảng mật khẩu do UI cung cấp
     * @return true nếu mật khẩu đúng
     * @throws IllegalStateException nếu không băm được mật khẩu
     */
    private boolean kiemTraMatKhau(char[] matKhau) {
        // Xóa bản sao mật khẩu và kết quả trung gian sau khi đối chiếu.
        PBEKeySpec thongSoBam = new PBEKeySpec(matKhau, SALT, SO_VONG_BAM, 256);
        try {
            byte[] ketQuaBam = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(thongSoBam).getEncoded();
            boolean matKhauDung = MessageDigest.isEqual(MAT_KHAU_DA_BAM, ketQuaBam);
            Arrays.fill(ketQuaBam, (byte) 0);
            return matKhauDung;
        } catch (GeneralSecurityException error) {
            throw new IllegalStateException("Không thể kiểm tra mật khẩu.", error);
        } finally {
            thongSoBam.clearPassword();
        }
    }
}
