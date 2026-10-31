package dao;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import entity.BoPhanNhanVien;
import entity.NhanVien;
import entity.KyThuatVienPhanCong;
import entity.TaiKhoan;
import entity.TrangThaiNhanVien;
import entity.VaiTro;

/** Quản lý hồ sơ trong bộ nhớ của ứng dụng; dữ liệu trở về ban đầu khi tắt ứng dụng. */
public final class NhanVienDAO {

    /** Dữ liệu dùng chung trong lần chạy ứng dụng; UI thao tác trên EDT. */
    private static final List<NhanVien> DANH_SACH = taoDuLieuBanDau();

    /** Nguồn phân công đọc lại tình trạng hồ sơ; tài khoản nối bằng mã cố định, không theo tên. */
    List<KyThuatVienPhanCong> layLienKetKyThuat(TaiKhoanDAO taiKhoanDAO) {
        List<KyThuatVienPhanCong> ketQua = new ArrayList<>();
        for (TaiKhoan tk : taiKhoanDAO.layTaiKhoanNoiBo()) {
            for (NhanVien nv : DANH_SACH) {
                if (nv.getMaNhanVien().equals(tk.getMaNhanVien())) {
                    KyThuatVienPhanCong lienKet = new KyThuatVienPhanCong(nv, tk);
                    if (lienKet.duocPhanCong()) ketQua.add(lienKet);
                }
            }
        }
        return ketQua;
    }

    /**
     * @param taiKhoan danh tính đã xác thực
     * @return bản chụp danh sách bất biến theo thứ tự tạo
     * @throws SecurityException nếu không phải Quản lý
     */
    public List<NhanVien> layDanhSach(TaiKhoan taiKhoan) {
        kiemTraQuyen(taiKhoan);
        return List.copyOf(DANH_SACH);
    }

    /**
     * Tìm không phân biệt hoa/thường hoặc dấu; kết hợp điều kiện tìm và trạng thái.
     * @param taiKhoan tài khoản Quản lý
     * @param tuKhoa mã, tên hoặc điện thoại, có thể rỗng
     * @param trangThai null để lấy tất cả trạng thái
     * @return danh sách kết quả riêng, không làm thay đổi nguồn
     */
    public List<NhanVien> timKiem(TaiKhoan taiKhoan, String tuKhoa, TrangThaiNhanVien trangThai) {
        String chu = boDau(Objects.requireNonNull(tuKhoa).trim());
        String so = chu.replaceAll("[\\s().-]", "");
        List<NhanVien> ketQua = new ArrayList<>();
        for (NhanVien nv : layDanhSach(taiKhoan)) {
            boolean dungTrangThai = trangThai == null || nv.getTrangThai() == trangThai;
            boolean dungTuKhoa = boDau(nv.getMaNhanVien()).contains(chu) || boDau(nv.getHoTen()).contains(chu)
                    || (!so.isEmpty() && nv.getSoDienThoai().contains(so));
            if (dungTrangThai && dungTuKhoa) {
                ketQua.add(nv);
            }
        }
        return ketQua;
    }

    /**
     * @param taiKhoan tài khoản Quản lý
     * @param maNhanVien mã cần xem
     * @return hồ sơ hiện tại
     * @throws IllegalArgumentException nếu không tìm thấy
     */
    public NhanVien timTheoMa(TaiKhoan taiKhoan, String maNhanVien) {
        kiemTraQuyen(taiKhoan);
        for (NhanVien nv : DANH_SACH) {
            if (nv.getMaNhanVien().equals(maNhanVien)) {
                return nv;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy nhân viên.");
    }

    /**
     * @param taiKhoan tài khoản Quản lý
     * @return mã dự kiến, chưa chiếm mã hoặc lưu hồ sơ
     */
    public String maTiepTheo(TaiKhoan taiKhoan) {
        kiemTraQuyen(taiKhoan);
        int soLonNhat = 0;
        for (NhanVien nv : DANH_SACH) {
            int so = Integer.parseInt(nv.getMaNhanVien().substring(2));
            soLonNhat = Math.max(soLonNhat, so);
        }
        return String.format(Locale.ROOT, "NV%03d", soLonNhat + 1);
    }

    /**
     * Thêm hồ sơ với mã đã sinh trên form; kiểm tra trùng trước khi lưu.
     * @param taiKhoan tài khoản Quản lý
     * @param nhanVien hồ sơ hợp lệ từ form
     * @return hồ sơ vừa lưu
     * @throws IllegalArgumentException nếu mã không hợp lệ hoặc đã tồn tại
     */
    public NhanVien them(TaiKhoan taiKhoan, NhanVien nhanVien) {
        kiemTraQuyen(taiKhoan);
        Objects.requireNonNull(nhanVien);
        if (!nhanVien.getMaNhanVien().equals(maTiepTheo(taiKhoan))) {
            throw new IllegalArgumentException("Mã nhân viên đã thay đổi. Hãy mở lại form thêm.");
        }
        DANH_SACH.add(nhanVien);
        return nhanVien;
    }

    /**
     * Tìm theo mã và thay thông tin của đúng nhân viên.
     * @param taiKhoan tài khoản Quản lý
     * @param nhanVien thông tin cần lưu, mã lấy từ ô chỉ đọc trên form
     * @throws IllegalArgumentException nếu không tìm thấy nhân viên
     */
    public void capNhat(TaiKhoan taiKhoan, NhanVien nhanVien) {
        kiemTraQuyen(taiKhoan);
        Objects.requireNonNull(nhanVien);
        for (int i = 0; i < DANH_SACH.size(); i++) {
            if (DANH_SACH.get(i).getMaNhanVien().equals(nhanVien.getMaNhanVien())) {
                DANH_SACH.set(i, nhanVien);
                return;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy nhân viên cần cập nhật.");
    }

    /**
     * @param taiKhoan danh tính cần quyền Quản lý
     * @throws SecurityException nếu sai vai trò
     */
    private void kiemTraQuyen(TaiKhoan taiKhoan) {
        Objects.requireNonNull(taiKhoan, "Cần tài khoản đã đăng nhập.");
        if (taiKhoan.getVaiTro() != VaiTro.QUAN_LY) {
            throw new SecurityException("Chỉ Quản lý được quản lý hồ sơ nhân viên.");
        }
    }

    /**
     * @param chu nội dung cần so sánh
     * @return chữ thường không dấu
     */
    private static String boDau(String chu) {
        return Normalizer.normalize(chu.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replace('đ', 'd');
    }

    /** @return bốn hồ sơ tạo sẵn theo danh sách Figma, có đủ dữ liệu để xem/sửa */
    private static List<NhanVien> taoDuLieuBanDau() {
        return new ArrayList<>(List.of(
                new NhanVien("NV001", "Trần Minh Hùng", LocalDate.of(1985, 3, 15), "0909345211", 12,
                        "Nam", "Gò Vấp, TP. Hồ Chí Minh", BoPhanNhanVien.QUAN_LY, TrangThaiNhanVien.DANG_LAM),
                new NhanVien("NV003", "Ngô Minh Hoàng", LocalDate.of(1994, 7, 20), "0901234567", 5,
                        "Nam", "Bình Thạnh, TP. Hồ Chí Minh", BoPhanNhanVien.TIEP_NHAN, TrangThaiNhanVien.DANG_LAM),
                new NhanVien("NV005", "Lê Minh Tâm", LocalDate.of(1996, 11, 8), "0912888456", 4,
                        "Nam", "Tân Bình, TP. Hồ Chí Minh", BoPhanNhanVien.KY_THUAT, TrangThaiNhanVien.DANG_LAM),
                new NhanVien("NV007", "Nguyễn Minh Vũ", LocalDate.of(1998, 1, 10), "0908678111", 3,
                        "Nam", "Phú Nhuận, TP. Hồ Chí Minh", BoPhanNhanVien.THU_NGAN, TrangThaiNhanVien.DANG_LAM)));
    }
}
