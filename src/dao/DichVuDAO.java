package dao;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import entity.DichVu;
import entity.TaiKhoan;
import entity.VaiTro;

/** Danh mục dùng chung trong bộ nhớ; dữ liệu trở về ban đầu khi tắt ứng dụng. */
public final class DichVuDAO {

    /** Nguồn dùng chung trên EDT; bản ghi bất biến, được thay thế khi sửa. */
    private static final List<DichVu> DANH_SACH = new ArrayList<>(List.of(
            new DichVu("DV001", "Bảo dưỡng định kỳ", "Bảo dưỡng", "Công", new BigDecimal("300000"), "",
                    "Theo quy trình garage", "Theo loại xe và phạm vi công việc", true),
            new DichVu("DV002", "Vệ sinh phanh trước", "Phanh", "Công", new BigDecimal("100000"), "", "", "", true),
            new DichVu("DV003", "Kiểm tra điều hòa", "Điện lạnh", "Công", new BigDecimal("200000"), "", "", "", true)));
    /** Chỉ tăng sau khi thêm thành công, không tái sử dụng mã đã xóa. */
    private static int soTiepTheo = 4;

    /** @return bản chụp danh mục, không cho UI sửa trực tiếp nguồn */
    public List<DichVu> layDanhSach(TaiKhoan taiKhoan) {
        kiemTraQuyen(taiKhoan);
        return List.copyOf(DANH_SACH);
    }

    /**
     * Tìm theo mã, tên hoặc nhóm, không phân biệt dấu và hoa/thường.
     * @param taiKhoan Quản lý có phiên hợp lệ
     * @param tuKhoa nội dung tìm, có thể rỗng
     * @param dangApDung null để lấy mọi trạng thái
     * @return kết quả thỏa đồng thời từ khóa và trạng thái
     */
    public List<DichVu> timKiem(TaiKhoan taiKhoan, String tuKhoa, Boolean dangApDung) {
        String chu = boDau(Objects.requireNonNull(tuKhoa).trim());
        List<DichVu> ketQua = new ArrayList<>();
        for (DichVu dv : layDanhSach(taiKhoan)) {
            boolean dungChu = boDau(dv.getMaDichVu()).contains(chu) || boDau(dv.getTenDichVu()).contains(chu)
                    || boDau(dv.getNhomDichVu()).contains(chu);
            if (dungChu && (dangApDung == null || dv.isDangApDung() == dangApDung)) { ketQua.add(dv); }
        }
        return List.copyOf(ketQua);
    }

    /** @return dịch vụ hiện tại; báo lỗi nếu mã không tồn tại */
    public DichVu timTheoMa(TaiKhoan taiKhoan, String maDichVu) {
        kiemTraQuyen(taiKhoan);
        return DANH_SACH.stream().filter(dv -> dv.getMaDichVu().equals(maDichVu)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dịch vụ."));
    }

    /** @return mã dự kiến cho form thêm, chưa lưu hoặc chiếm mã */
    public String maTiepTheo(TaiKhoan taiKhoan) {
        kiemTraQuyen(taiKhoan);
        return String.format(Locale.ROOT, "DV%03d", soTiepTheo);
    }

    /** Thêm dịch vụ hợp lệ; mã tự sinh không được tái sử dụng sau khi xóa. */
    public void them(TaiKhoan taiKhoan, DichVu dichVu) {
        kiemTraQuyen(taiKhoan);
        Objects.requireNonNull(dichVu);
        if (!dichVu.getMaDichVu().equals(maTiepTheo(taiKhoan))) {
            throw new IllegalArgumentException("Mã dịch vụ đã thay đổi. Hãy mở lại form thêm dịch vụ.");
        }
        DANH_SACH.add(dichVu);
        soTiepTheo++;
    }

    /**
     * Thay bản ghi danh mục, gồm giá, điều kiện và trạng thái; không sửa chứng từ.
     * @param taiKhoan Quản lý có phiên hợp lệ
     * @param banDau bản ghi khi mở form, tránh ghi đè thay đổi từ cửa sổ khác
     * @param dichVu thông tin mới, giữ nguyên mã
     */
    public void capNhat(TaiKhoan taiKhoan, DichVu banDau, DichVu dichVu) {
        kiemTraQuyen(taiKhoan);
        Objects.requireNonNull(banDau);
        Objects.requireNonNull(dichVu);
        if (!banDau.getMaDichVu().equals(dichVu.getMaDichVu())) {
            throw new IllegalArgumentException("Không được thay đổi mã dịch vụ.");
        }
        DichVu hienTai = timTheoMa(taiKhoan, banDau.getMaDichVu());
        if (hienTai != banDau) {
            throw new IllegalArgumentException("Dịch vụ đã được cập nhật ở cửa sổ khác. Hãy mở lại chi tiết.");
        }
        DANH_SACH.set(DANH_SACH.indexOf(hienTai), dichVu);
    }

    /** Xóa sau khi kiểm tra lại báo giá liên kết; dịch vụ đã dùng chỉ được tạm dừng. */
    public void xoa(TaiKhoan taiKhoan, DichVu dichVu) {
        kiemTraQuyen(taiKhoan);
        Objects.requireNonNull(dichVu);
        DichVu hienTai = timTheoMa(taiKhoan, dichVu.getMaDichVu());
        if (hienTai != dichVu) {
            throw new IllegalArgumentException("Dịch vụ đã thay đổi. Hãy mở lại chi tiết trước khi xóa.");
        }
        if (PhieuSuaChuaDAO.daSuDungDichVu(taiKhoan, dichVu.getMaDichVu())) {
            throw new IllegalArgumentException("Dịch vụ đã có trong báo giá, không được xóa. Hãy tạm dừng dịch vụ.");
        }
        DANH_SACH.remove(hienTai);
    }

    /** Quyền được kiểm tra lại ở mỗi lần đọc/lưu, không chỉ lúc mở UI. */
    private void kiemTraQuyen(TaiKhoan taiKhoan) {
        Objects.requireNonNull(taiKhoan, "Cần tài khoản đã đăng nhập.").kiemTraPhien();
        if (taiKhoan.getVaiTro() != VaiTro.QUAN_LY) {
            throw new SecurityException("Chỉ Quản lý được quản lý dịch vụ và bảng giá.");
        }
    }

    /** @return chữ thường không dấu để tìm tên/nhóm tiếng Việt */
    private static String boDau(String chu) {
        return Normalizer.normalize(chu.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replace('đ', 'd');
    }
}
