package utils;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Tiện ích băm và xác thực mật khẩu theo chuẩn PBKDF2-HMAC-SHA256.
 * Đảm bảo an toàn thông tin theo chuẩn OWASP với muối ngẫu nhiên và so sánh constant-time.
 */
public final class PasswordHashUtil {

    public static final String THUAT_TOAN = "PBKDF2-HMAC-SHA256";
    public static final int SO_VONG_MAC_DINH = 600000;
    public static final int DO_DAI_HASH_BIT = 256;
    public static final int DO_DAI_MUOI_BYTE = 16;

    private static final SecureRandom NGAU_NHIEN = new SecureRandom();

    private PasswordHashUtil() {
    }

    /**
     * Băm mật khẩu mới với muối ngẫu nhiên 16 bytes và 600.000 vòng lặp PBKDF2.
     *
     * @param matKhau Mật khẩu dạng mảng char[]
     * @return Đối tượng chứa mảng byte muối và mảng byte giá trị băm
     */
    public static ThongTinBam bamMatKhau(char[] matKhau) {
        return bamMatKhau(matKhau, SO_VONG_MAC_DINH);
    }

    /**
     * Băm mật khẩu với số vòng lặp tùy chỉnh.
     *
     * @param matKhau Mật khẩu dạng mảng char[]
     * @param soVong  Số vòng lặp băm
     * @return Đối tượng chứa mảng byte muối và mảng byte giá trị băm
     */
    public static ThongTinBam bamMatKhau(char[] matKhau, int soVong) {
        if (matKhau == null || matKhau.length == 0) {
            throw new IllegalArgumentException("Mật khẩu không được để trống.");
        }
        byte[] muoi = sinhMuoiNgauNhien();
        byte[] hash = taoHash(matKhau, muoi, soVong);
        return new ThongTinBam(muoi, hash);
    }

    /**
     * Sinh muối ngẫu nhiên với độ dài chuẩn 16 bytes.
     */
    public static byte[] sinhMuoiNgauNhien() {
        byte[] muoi = new byte[DO_DAI_MUOI_BYTE];
        NGAU_NHIEN.nextBytes(muoi);
        return muoi;
    }

    /**
     * Tạo giá trị băm PBKDF2WithHmacSHA256 từ mật khẩu và muối.
     */
    public static byte[] taoHash(char[] matKhau, byte[] muoi, int soVong) {
        if (matKhau == null || muoi == null) {
            throw new IllegalArgumentException("Mật khẩu và muối không được null.");
        }
        PBEKeySpec dacTa = new PBEKeySpec(matKhau, muoi, soVong, DO_DAI_HASH_BIT);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(dacTa).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Hệ thống không hỗ trợ thuật toán PBKDF2-HMAC-SHA256.", ex);
        } finally {
            dacTa.clearPassword();
        }
    }

    /**
     * Xác thực mật khẩu nhập vào so với thông tin băm lưu trữ trong CSDL.
     * Sử dụng so sánh constant-time để chống tấn công Timing Attack.
     *
     * @param matKhau   Mật khẩu người dùng nhập vào
     * @param muoi      Muối đã lưu trong CSDL
     * @param hashDaLuu Giá trị băm đã lưu trong CSDL
     * @param soVong    Số vòng lặp đã lưu
     * @param thuatToan Tên thuật toán đã lưu
     * @return true nếu mật khẩu chính xác, ngược lại false
     */
    public static boolean xacThucMatKhau(char[] matKhau, byte[] muoi, byte[] hashDaLuu, int soVong, String thuatToan) {
        if (matKhau == null || muoi == null || hashDaLuu == null) {
            return false;
        }
        if (!THUAT_TOAN.equals(thuatToan) || soVong < SO_VONG_MAC_DINH || hashDaLuu.length != 32) {
            throw new SecurityException("Thông tin bảo mật tài khoản không hợp lệ; cần Quản lý đặt lại mật khẩu.");
        }
        byte[] hashNhap = taoHash(matKhau, muoi, soVong);
        try {
            return MessageDigest.isEqual(hashDaLuu, hashNhap);
        } finally {
            Arrays.fill(hashNhap, (byte) 0);
        }
    }

    /**
     * Xác thực dự phòng cho các tài khoản legacy cũ lưu plaintext (nếu có).
     */
    public static boolean xacThucPlainTextLegacy(char[] matKhau, String matKhauCu) {
        if (matKhau == null || matKhauCu == null) {
            return false;
        }
        byte[] nhap = new String(matKhau).getBytes(StandardCharsets.UTF_8);
        byte[] cu = matKhauCu.getBytes(StandardCharsets.UTF_8);
        try {
            return MessageDigest.isEqual(cu, nhap);
        } finally {
            Arrays.fill(nhap, (byte) 0);
            Arrays.fill(cu, (byte) 0);
        }
    }

    /**
     * Kiểm tra độ mạnh của mật khẩu mới: tối thiểu 8 ký tự, có chữ hoa, chữ số và ký tự đặc biệt.
     */
    public static void kiemTraDoManhMatKhau(char[] matKhau) {
        if (matKhau == null || matKhau.length == 0) {
            throw new IllegalArgumentException("Mật khẩu không được để trống.");
        }
        boolean chuHoa = false;
        boolean chuSo = false;
        boolean dacBiet = false;

        for (char kyTu : matKhau) {
            if (Character.isWhitespace(kyTu)) {
                throw new IllegalArgumentException("Mật khẩu không được chứa khoảng trắng.");
            }
            if (Character.isUpperCase(kyTu)) {
                chuHoa = true;
            } else if (Character.isDigit(kyTu)) {
                chuSo = true;
            } else if (!Character.isLetterOrDigit(kyTu)) {
                dacBiet = true;
            }
        }

        if (matKhau.length < 8 || !chuHoa || !chuSo || !dacBiet) {
            throw new IllegalArgumentException(
                    "Mật khẩu phải từ 8 ký tự trở lên, gồm ít nhất một chữ hoa, một chữ số và một ký tự đặc biệt.");
        }
    }

    /**
     * Cấu trúc dữ liệu chứa muối và giá trị băm của mật khẩu.
     */
    public static final class ThongTinBam {
        private final byte[] muoi;
        private final byte[] hash;

        public ThongTinBam(byte[] muoi, byte[] hash) {
            this.muoi = muoi;
            this.hash = hash;
        }

        public byte[] getMuoi() {
            return muoi;
        }

        public byte[] getHash() {
            return hash;
        }
    }
}
