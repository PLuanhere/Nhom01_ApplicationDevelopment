package dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import entity.BaoCaoKiemTra;
import entity.PhieuTiepNhan;
import entity.TaiKhoan;
import entity.TrangThaiKiemTra;
import entity.VaiTro;

// Quản lý dữ liệu kiểm tra mẫu trong bộ nhớ của phiên sử dụng.
public final class BaoCaoKiemTraDAO {
    private final List<PhieuTiepNhan> danhSachPhieu = new ArrayList<PhieuTiepNhan>();
    private final List<BaoCaoKiemTra> danhSachBaoCao = new ArrayList<BaoCaoKiemTra>();
    private int soTiepTheo = 113;

    public BaoCaoKiemTraDAO() {
        PhieuTiepNhan phieuCu = new PhieuTiepNhan("TN-2026-0108", "51H-246.80",
                "Bảo dưỡng định kỳ, kiểm tra tiếng kêu phanh trước.",
                "Tiếng kêu nhẹ khi phanh; thân xe không va quẹt mới.", "kythuat", true);
        danhSachPhieu.add(phieuCu);
        danhSachPhieu.add(new PhieuTiepNhan("TN-2026-0111", "51K-001.23", "Kiểm tra điều hòa",
                "Điều hòa làm lạnh yếu.", "kythuat", true));
        danhSachPhieu.add(new PhieuTiepNhan("TN-2026-0113", "51H-135.79", "Bảo dưỡng, kiểm tra phanh",
                "Tiếng kêu nhẹ khi phanh.", "kythuat", true));
        danhSachPhieu.add(new PhieuTiepNhan("TN-2026-0112", "51F-112.34", "Kiểm tra xe",
                "Chưa xác nhận nội dung tiếp nhận.", "kythuat", false));
        danhSachBaoCao.add(new BaoCaoKiemTra("KT-2026-0112", danhSachPhieu.get(1),
                LocalDate.of(2026, 10, 5), "Điều hòa",
                new String[] {"Làm lạnh yếu", "Lọc gió bẩn", "Vệ sinh lọc gió và đo áp suất", "", "", "", ""},
                TrangThaiKiemTra.NHAP, null));
        danhSachBaoCao.add(new BaoCaoKiemTra("KT-2026-0108", phieuCu, LocalDate.of(2026, 10, 5),
                "Tổng hợp", new String[] {"Đã hoàn tất kiểm tra", "Bụi bám ở phanh trước",
                    "Bảo dưỡng định kỳ, thay dầu/lọc dầu, vệ sinh phanh.", "Bụi bám, má phanh còn đạt",
                    "Đến kỳ thay", "Bình thường", "Không ghi nhận mã lỗi."},
                TrangThaiKiemTra.HOAN_TAT, LocalDateTime.of(2026, 10, 5, 10, 5),
                new String[] {"Vệ sinh và kiểm tra phanh", "Thay dầu và lọc dầu", "Không có hạng mục sửa"}));
    }

    // Đọc các phiếu tiếp nhận thuộc phạm vi phân công của tài khoản.
    public List<PhieuTiepNhan> layPhieu(TaiKhoan taiKhoan) {
        kiemTraQuyen(taiKhoan);
        List<PhieuTiepNhan> ketQua = new ArrayList<PhieuTiepNhan>();
        for (PhieuTiepNhan phieu : danhSachPhieu) {
            if (phieu.getTaiKhoanKyThuat().equals(taiKhoan.getTenDangNhap())) {
                ketQua.add(phieu);
            }
        }
        return ketQua;
    }

    // Lọc báo cáo theo phân công, mã báo cáo, mã phiếu, biển số và trạng thái.
    public List<BaoCaoKiemTra> timKiem(TaiKhoan taiKhoan, String tuKhoa, String trangThai) {
        kiemTraQuyen(taiKhoan);
        String tuKhoaChuanHoa = tuKhoa.trim().toLowerCase(Locale.ROOT);
        List<BaoCaoKiemTra> ketQua = new ArrayList<BaoCaoKiemTra>();
        for (BaoCaoKiemTra baoCao : danhSachBaoCao) {
            PhieuTiepNhan phieu = baoCao.getPhieu();
            String noiDung = baoCao.getMaBaoCao() + " " + phieu.getMaPhieu() + " " + phieu.getBienSo();
            if (phieu.getTaiKhoanKyThuat().equals(taiKhoan.getTenDangNhap())
                    && noiDung.toLowerCase(Locale.ROOT).contains(tuKhoaChuanHoa)
                    && ("Tất cả".equals(trangThai)
                    || baoCao.getTrangThai().getTenHienThi().equals(trangThai))) {
                ketQua.add(baoCao);
            }
        }
        return ketQua;
    }

    // Tìm báo cáo của phiếu tiếp nhận để ngăn tạo trùng báo cáo.
    public Optional<BaoCaoKiemTra> timTheoPhieu(TaiKhoan taiKhoan, PhieuTiepNhan phieu) {
        kiemTraPhieu(taiKhoan, phieu);
        for (BaoCaoKiemTra baoCao : danhSachBaoCao) {
            if (baoCao.getPhieu().getMaPhieu().equals(phieu.getMaPhieu())) {
                return Optional.of(baoCao);
            }
        }
        return Optional.empty();
    }

    // Kiểm tra dữ liệu và lưu báo cáo nháp hoặc báo cáo hoàn tất.
    public BaoCaoKiemTra luu(TaiKhoan taiKhoan, BaoCaoKiemTra banNhap, boolean hoanTat) {
        kiemTraPhieu(taiKhoan, banNhap.getPhieu());
        String[] noiDung = banNhap.getNoiDung();
        if (noiDung[0].trim().isEmpty() || noiDung[2].trim().isEmpty()
                || (hoanTat && "Tổng hợp".equals(banNhap.getBoPhan())
                && (noiDung[3].trim().isEmpty() || noiDung[4].trim().isEmpty()))) {
            throw new IllegalArgumentException("Cần nhập đủ kết quả và đề xuất bắt buộc.");
        }
        BaoCaoKiemTra baoCaoCu = timTheoPhieu(taiKhoan, banNhap.getPhieu()).orElse(null);
        if (baoCaoCu != null && !baoCaoCu.getMaBaoCao().equals(banNhap.getMaBaoCao())) {
            throw new IllegalStateException("Phiếu đã có báo cáo. Hãy mở báo cáo hiện có để sửa.");
        }
        if (baoCaoCu != null && baoCaoCu.getTrangThai() != TrangThaiKiemTra.NHAP) {
            throw new IllegalStateException("Báo cáo hoàn tất được giữ lại, không sửa hoặc hoàn tất lần nữa.");
        }
        if (baoCaoCu == null && !banNhap.getMaBaoCao().isEmpty()) {
            throw new IllegalStateException("Báo cáo không còn tồn tại. Hãy tải lại danh sách.");
        }
        String maBaoCao = baoCaoCu == null
                ? String.format("KT-2026-%04d", soTiepTheo) : baoCaoCu.getMaBaoCao();
        BaoCaoKiemTra baoCaoMoi = new BaoCaoKiemTra(maBaoCao, banNhap.getPhieu(), banNhap.getNgayKiemTra(),
                banNhap.getBoPhan(), noiDung, hoanTat ? TrangThaiKiemTra.HOAN_TAT : TrangThaiKiemTra.NHAP,
                hoanTat ? LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")) : null,
                banNhap.getDeXuatTheoVung());
        if (baoCaoCu == null) {
            danhSachBaoCao.add(baoCaoMoi);
            soTiepTheo++;
        } else {
            danhSachBaoCao.set(danhSachBaoCao.indexOf(baoCaoCu), baoCaoMoi);
        }
        return baoCaoMoi;
    }

    // Xóa báo cáo nháp thuộc phân công và chặn báo cáo đã hoàn tất hoặc đã thay đổi.
    public void xoa(TaiKhoan taiKhoan, BaoCaoKiemTra baoCao) {
        kiemTraPhieu(taiKhoan, baoCao.getPhieu());
        if (baoCao.getTrangThai() != TrangThaiKiemTra.NHAP) {
            throw new IllegalStateException("Không thể xóa " + baoCao.getMaBaoCao() + " vì đã có dữ liệu liên quan.");
        }
        if (!danhSachBaoCao.remove(baoCao)) {
            throw new IllegalStateException("Báo cáo đã thay đổi. Hãy tải lại danh sách.");
        }
    }

    // Kiểm tra phiếu tồn tại, đã xác nhận và được phân công cho đúng tài khoản.
    private void kiemTraPhieu(TaiKhoan taiKhoan, PhieuTiepNhan phieu) {
        kiemTraQuyen(taiKhoan);
        if (!danhSachPhieu.contains(phieu) || !phieu.isDaXacNhan()
                || !phieu.getTaiKhoanKyThuat().equals(taiKhoan.getTenDangNhap())) {
            throw new IllegalArgumentException("Phiếu tiếp nhận chưa hợp lệ hoặc không thuộc phân công.");
        }
    }

    // Chặn truy cập khi tài khoản không có vai trò kỹ thuật.
    private void kiemTraQuyen(TaiKhoan taiKhoan) {
        if (taiKhoan == null || taiKhoan.getVaiTro() != VaiTro.NHAN_VIEN_KY_THUAT) {
            throw new SecurityException("Chức năng này dành cho nhân viên kỹ thuật.");
        }
    }
}