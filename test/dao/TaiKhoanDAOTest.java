package dao;

import java.util.Arrays;

import entity.TaiKhoan;
import entity.VaiTro;

/** Kiểm thử bốn tài khoản có sẵn, độc lập với Swing và database, chỉ dùng JDK 17. */
public final class TaiKhoanDAOTest {

    /** Lớp chỉ cung cấp điểm chạy kiểm thử. */
    private TaiKhoanDAOTest() {
    }

    /**
     * Kiểm tra bốn tài khoản, thông tin sai và hợp đồng đầu vào.
     * @param args không sử dụng
     */
    public static void main(String[] args) {
        TaiKhoanDAO dao = new TaiKhoanDAO();
        checkAccount(dao, "quanly", VaiTro.QUAN_LY);
        checkAccount(dao, "tiepnhan", VaiTro.NHAN_VIEN_TIEP_NHAN);
        checkAccount(dao, "kythuat", VaiTro.NHAN_VIEN_KY_THUAT);
        checkAccount(dao, "thungan", VaiTro.THU_NGAN);

        // Thông tin không đúng không được tạo ra danh tính có quyền mặc định.
        check(dao.dangNhap("khongtontai", "123".toCharArray()).isEmpty(), "Tài khoản lạ bị chấp nhận.");
        check(dao.dangNhap("quanly", "sai".toCharArray()).isEmpty(), "Mật khẩu sai bị chấp nhận.");
        check(dao.dangNhap("quanly", new char[0]).isEmpty(), "Mật khẩu rỗng bị chấp nhận.");
        check(dao.dangNhap("  ", "123".toCharArray()).isEmpty(), "Tên rỗng bị chấp nhận.");
        check(dao.dangNhap("Quanly", "123".toCharArray()).isEmpty(), "Tên đăng nhập phải phân biệt hoa thường.");
        check(dao.dangNhap("quanly", " 123 ".toCharArray()).isEmpty(), "Không được trim mật khẩu.");
        check(dao.dangNhap("  quanly  ", "123".toCharArray()).orElseThrow().getTenDangNhap().equals("quanly"),
                "Tên đăng nhập phải bỏ khoảng trắng hai đầu.");

        // Đầu vào lập trình sai phải báo lỗi rõ, không che thành đăng nhập thất bại.
        expectError(NullPointerException.class, () -> dao.dangNhap(null, "123".toCharArray()));
        expectError(NullPointerException.class, () -> dao.dangNhap("quanly", null));
        expectError(IllegalArgumentException.class, () -> new TaiKhoan(" ", VaiTro.QUAN_LY));
        expectError(NullPointerException.class, () -> new TaiKhoan("quanly", null));
        System.out.println("PASS: 4 tài khoản đúng vai trò; từ chối thông tin sai; kiểm tra hợp đồng đầu vào.");
    }

    /**
     * Xác nhận mật khẩu và vai trò được cấp từ nguồn dữ liệu.
     * @param dao đối tượng truy cập tài khoản
     * @param tenDangNhap tài khoản cần kiểm tra
     * @param vaiTro vai trò mong đợi
     */
    private static void checkAccount(TaiKhoanDAO dao, String tenDangNhap, VaiTro vaiTro) {
        char[] matKhau = "123".toCharArray();
        try {
            TaiKhoan taiKhoan = dao.dangNhap(tenDangNhap, matKhau).orElseThrow();
            check(taiKhoan.getTenDangNhap().equals(tenDangNhap), "Trả nhầm tài khoản.");
            check(taiKhoan.getVaiTro() == vaiTro, "Trả nhầm vai trò của " + tenDangNhap);
            check(Arrays.equals(matKhau, "123".toCharArray()), "DAO không được sửa mảng của bên gọi.");
        } finally {
            Arrays.fill(matKhau, '\0');
        }
    }

    /**
     * Dừng kiểm thử khi kết quả không đúng mong đợi, không phụ thuộc cờ -ea của JVM.
     * @param dieuKien kết quả cần đúng
     * @param thongBao mô tả lỗi
     * @throws AssertionError nếu điều kiện sai
     */
    private static void check(boolean dieuKien, String thongBao) {
        if (!dieuKien) {
            throw new AssertionError(thongBao);
        }
    }

    /**
     * Xác nhận đầu vào sai phát sinh đúng loại ngoại lệ.
     * @param loaiLoi loại ngoại lệ mong đợi
     * @param thaoTac lời gọi cần kiểm tra
     * @throws AssertionError nếu không có lỗi hoặc phát sinh lỗi khác
     */
    private static void expectError(Class<? extends RuntimeException> loaiLoi, Runnable thaoTac) {
        try {
            thaoTac.run();
        } catch (RuntimeException error) {
            check(loaiLoi.isInstance(error), "Sai loại lỗi: " + error.getClass().getSimpleName());
            return;
        }
        throw new AssertionError("Thiếu lỗi: " + loaiLoi.getSimpleName());
    }
}
