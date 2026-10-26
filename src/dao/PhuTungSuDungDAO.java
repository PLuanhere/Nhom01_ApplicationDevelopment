package dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import entity.PhuTung;
import entity.PhuTungSuDung;
import entity.TaiKhoan;
import entity.TrangThaiSuDung;
import entity.VaiTro;

// DAO mẫu trong bộ nhớ của nhánh phụ tùng; chưa thay thế transaction SQL Server khi tích hợp.
public final class PhuTungSuDungDAO {
    private final Map<String, PhuTung> danhMuc = new LinkedHashMap<String, PhuTung>();
    private final Map<String, BigDecimal> tonKho = new LinkedHashMap<String, BigDecimal>();
    private final Map<String, PhuTungSuDung> hoSo = new LinkedHashMap<String, PhuTungSuDung>();

    private final Map<String, Map<String, BigDecimal>> phamViDuyet =
            new LinkedHashMap<String, Map<String, BigDecimal>>();
    private final Map<String, String> phanCong = new LinkedHashMap<String, String>();
    private final List<String> lenhChoPhuTung = new ArrayList<String>();
    private int soTiepTheo = 113;

    public PhuTungSuDungDAO() {
        themDanhMuc("PT001", "Dầu 5W-30", "lít", "48", "12");
        themDanhMuc("PT005", "Lọc dầu động cơ", "cái", "6", "12");
        themDanhMuc("PT004", "Rotuyn lái", "cái", "5", "2");
        String[] maLenh = {"SC-2026-0111", "SC-2026-0113", "SC-2026-0114", "SC-2026-0108"};
        for (String ma : maLenh) {
            Map<String, BigDecimal> chiTiet = new LinkedHashMap<String, BigDecimal>();
            chiTiet.put("PT001", new BigDecimal("4"));
            chiTiet.put("PT005", BigDecimal.ONE);
            if ("SC-2026-0114".equals(ma)) {
                chiTiet.put("PT004", BigDecimal.ONE);
            }
            phamViDuyet.put(ma, chiTiet);
            phanCong.put(ma, "kythuat");
        }
        hoSo.put("SD-2026-0112", taoBanGhi("SD-2026-0112", "SC-2026-0111", "PT001",
                new BigDecimal("2"), "Chưa xác nhận xuất kho", TrangThaiSuDung.NHAP));
        hoSo.put("SD-2026-0108", taoBanGhi("SD-2026-0108", "SC-2026-0108", "PT001",
                new BigDecimal("4"), "Đã ghi nhận thực dùng", TrangThaiSuDung.DA_GHI_NHAN));
    }

    /**
     * Đọc danh sách theo tiêu chí; kết quả không cho phép sửa kho nội bộ.
     * @param taiKhoan tài khoản kỹ thuật đã đăng nhập
     * @param tuKhoa mã ghi nhận, mã lệnh hoặc tên phụ tùng
     * @param trangThai trạng thái cần lọc, null để lấy tất cả
     * @return các hồ sơ phù hợp
     * @throws SecurityException nếu không có quyền
     */
    // Lọc hồ sơ mẫu theo từ khóa và trạng thái của người được phân công.
    public synchronized List<PhuTungSuDung> docDanhSach(TaiKhoan taiKhoan, String tuKhoa,
            TrangThaiSuDung trangThai) {
        kiemTraTaiKhoan(taiKhoan);
        List<PhuTungSuDung> ketQua = new ArrayList<PhuTungSuDung>();
        String khoa = Objects.requireNonNull(tuKhoa).trim().toLowerCase(Locale.ROOT);
        for (PhuTungSuDung banGhi : hoSo.values()) {
            String noiDung = banGhi.getMaGhiNhan() + " " + banGhi.getMaLenhSuaChua() + " " + banGhi.getPhuTung();
            if (taiKhoan.getTenDangNhap().equals(phanCong.get(banGhi.getMaLenhSuaChua()))
                    && noiDung.toLowerCase(Locale.ROOT).contains(khoa)
                    && (trangThai == null || trangThai == banGhi.getTrangThai())) {
                ketQua.add(banGhi);
            }
        }
        return ketQua;
    }

    /**
     * Đọc các lệnh đã duyệt thuộc tài khoản hiện tại.
     * @param taiKhoan tài khoản kỹ thuật
     * @return bản sao danh sách mã lệnh
     * @throws SecurityException nếu sai vai trò
     */
    // Lấy các lệnh mẫu được phân công và có phạm vi phụ tùng khách đã duyệt.
    public synchronized List<String> docLenhDaDuyet(TaiKhoan taiKhoan) {
        kiemTraTaiKhoan(taiKhoan);
        List<String> ketQua = new ArrayList<String>();
        for (String maLenh : phamViDuyet.keySet()) {
            if (taiKhoan.getTenDangNhap().equals(phanCong.get(maLenh))) {
                ketQua.add(maLenh);
            }
        }
        return ketQua;
    }

    /**
     * Lấy phạm vi khách duyệt của một lệnh.
     * @param taiKhoan tài khoản kỹ thuật được phân công
     * @param maLenh mã lệnh
     * @return bản sao mã phụ tùng và lượng được duyệt
     * @throws IllegalArgumentException nếu lệnh không tồn tại
     * @throws SecurityException nếu không được phân công
     */
    // Trả bản sao phạm vi duyệt để màn hình không sửa được dữ liệu nghiệp vụ.
    public synchronized Map<String, BigDecimal> docPhamViDuyet(TaiKhoan taiKhoan, String maLenh) {
        kiemTraLenh(taiKhoan, maLenh);
        return new LinkedHashMap<String, BigDecimal>(phamViDuyet.get(maLenh));
    }

    /**
     * Đọc danh mục của một mã phụ tùng.
     * @param maPhuTung mã cần đọc
     * @return phụ tùng bất biến
     * @throws IllegalArgumentException nếu mã không tồn tại
     */
    // Tìm phụ tùng trong danh mục mẫu và báo rõ khi mã không tồn tại.
    public synchronized PhuTung docPhuTung(String maPhuTung) {
        PhuTung phuTung = danhMuc.get(maPhuTung);
        if (phuTung == null) {
            throw new IllegalArgumentException("Phụ tùng không tồn tại.");
        }
        return phuTung;
    }

    /**
     * Đọc tồn hiện tại theo một nguồn dữ liệu duy nhất.
     * @param maPhuTung mã phụ tùng
     * @return lượng tồn hiện tại
     * @throws IllegalArgumentException nếu mã không tồn tại
     */
    // Lấy tồn hiện tại để biểu mẫu và màn xác nhận cùng dùng một số liệu.
    public synchronized BigDecimal docTon(String maPhuTung) {
        docPhuTung(maPhuTung);
        return tonKho.get(maPhuTung);
    }

    /**
     * Tính lượng còn được phép dùng sau các lần đã ghi nhận.
     * @param taiKhoan tài khoản được phân công
     * @param maLenh mã lệnh
     * @param maPhuTung mã trong phạm vi duyệt
     * @return lượng còn được dùng, không âm
     * @throws IllegalArgumentException nếu phụ tùng chưa được duyệt
     */
    // Trừ lượng đã ghi nhận khỏi phạm vi khách duyệt để ngăn dùng vượt qua nhiều lần.
    public synchronized BigDecimal docLuongConLai(TaiKhoan taiKhoan, String maLenh, String maPhuTung) {
        kiemTraLenh(taiKhoan, maLenh);
        BigDecimal conLai = phamViDuyet.get(maLenh).get(maPhuTung);
        if (conLai == null) {
            throw new IllegalArgumentException("Phụ tùng chưa được khách duyệt.");
        }
        for (PhuTungSuDung banGhi : hoSo.values()) {
            if (maLenh.equals(banGhi.getMaLenhSuaChua()) && maPhuTung.equals(banGhi.getPhuTung().getMaPhuTung())
                    && banGhi.getTrangThai() == TrangThaiSuDung.DA_GHI_NHAN) {
                conLai = conLai.subtract(banGhi.getSoLuong());
            }
        }
        return conLai;
    }

    /**
     * Thêm hoặc sửa nháp mà không thay đổi tồn kho.
     * @param taiKhoan tài khoản được phân công
     * @param banCu bản đã đọc, null khi thêm mới
     * @param maLenh mã lệnh đã duyệt
     * @param maPhuTung mã phụ tùng đã duyệt
     * @param soLuong lượng dương đúng đơn vị
     * @param ghiChu nội dung ghi chú
     * @return bản nháp đã lưu
     * @throws IllegalArgumentException nếu dữ liệu sai hoặc trùng nháp cùng phụ tùng
     * @throws IllegalStateException nếu bản cũ đã đổi hoặc đã ghi nhận
     */
    // Lưu nháp sau khi kiểm tra phân công, phạm vi duyệt và phiên bản đang sửa.
    public synchronized PhuTungSuDung luuNhap(TaiKhoan taiKhoan, PhuTungSuDung banCu, String maLenh,
            String maPhuTung, BigDecimal soLuong, String ghiChu) {
        kiemTraLenh(taiKhoan, maLenh);
        if (banCu != null) {
            kiemTraNhap(taiKhoan, banCu);
        }
        PhuTung phuTung = docPhuTung(maPhuTung);
        phuTung.kiemTraSoLuong(soLuong);
        kiemTraPhamVi(taiKhoan, maLenh, maPhuTung, soLuong);
        for (PhuTungSuDung banGhi : hoSo.values()) {
            if (banGhi != banCu && banGhi.getTrangThai() == TrangThaiSuDung.NHAP
                    && maLenh.equals(banGhi.getMaLenhSuaChua())
                    && maPhuTung.equals(banGhi.getPhuTung().getMaPhuTung())) {
                throw new IllegalArgumentException("Lệnh đã có nháp cho phụ tùng này. Hãy sửa nháp hiện có.");
            }
        }
        String ma = banCu == null ? taoMaMoi() : banCu.getMaGhiNhan();
        PhuTungSuDung ketQua = taoBanGhi(ma, maLenh, maPhuTung, soLuong, ghiChu, TrangThaiSuDung.NHAP);
        hoSo.put(ma, ketQua);
        return ketQua;
    }

    /**
     * Xóa một nháp chưa xuất kho.
     * @param taiKhoan tài khoản được phân công
     * @param banCu bản nháp đã đọc
     * @throws IllegalStateException nếu bản đã đổi hoặc đã ghi nhận
     */
    // Xóa đúng bản nháp đang hiển thị mà không hoàn kho cho hồ sơ đã ghi nhận.
    public synchronized void xoaNhap(TaiKhoan taiKhoan, PhuTungSuDung banCu) {
        kiemTraNhap(taiKhoan, banCu);
        hoSo.remove(banCu.getMaGhiNhan());
    }

    /**
     * Xác nhận cả nhóm phụ tùng trong một lần, chống xuất lặp theo bản nháp gốc.
     * @param taiKhoan tài khoản được phân công
     * @param banCu bản nháp dùng làm mốc xác nhận
     * @param luongDung mã phụ tùng và lượng thực dùng; lượng 0 được bỏ qua
     * @param ghiChu ghi chú chung
     * @return bản đã ghi nhận tương ứng bản nháp gốc
     * @throws IllegalArgumentException nếu sai lượng, vượt tồn hoặc vượt phạm vi duyệt
     * @throws IllegalStateException nếu bản đã được xác nhận hoặc thay đổi
     */
    // Kiểm tra toàn bộ nhóm trước khi ghi để lỗi một dòng không trừ kho các dòng khác.
    public synchronized PhuTungSuDung xacNhanThucDung(TaiKhoan taiKhoan, PhuTungSuDung banCu,
            Map<String, BigDecimal> luongDung, String ghiChu) {
        kiemTraNhap(taiKhoan, banCu);
        String maLenh = banCu.getMaLenhSuaChua();
        Map<String, PhuTungSuDung> hoSoMoi = new LinkedHashMap<String, PhuTungSuDung>(hoSo);
        Map<String, BigDecimal> tonMoi = new LinkedHashMap<String, BigDecimal>(tonKho);
        BigDecimal luongGoc = luongDung.get(banCu.getPhuTung().getMaPhuTung());
        if (luongGoc == null || luongGoc.signum() <= 0) {
            throw new IllegalArgumentException("Phụ tùng của bản nháp đang xác nhận phải có lượng dùng lớn hơn 0.");
        }
        // Kiểm tra kho trước phạm vi để hướng người dùng tới đúng trạng thái vượt tồn của thiết kế.
        for (Map.Entry<String, BigDecimal> dong : luongDung.entrySet()) {
            PhuTung phuTung = docPhuTung(dong.getKey());
            BigDecimal soLuong = Objects.requireNonNull(dong.getValue(), "Thiếu số lượng.");
            if (soLuong.signum() == 0) {
                continue;
            }
            phuTung.kiemTraSoLuong(soLuong);
            if (soLuong.compareTo(tonKho.get(dong.getKey())) > 0) {
                throw new IllegalArgumentException(phuTung.getMaPhuTung() + " còn "
                        + tonKho.get(dong.getKey()).toPlainString() + " " + phuTung.getDonViTinh()
                        + " nhưng nhập " + soLuong.toPlainString() + " " + phuTung.getDonViTinh() + ".");
            }
            kiemTraPhamVi(taiKhoan, maLenh, dong.getKey(), soLuong);
        }
        for (Map.Entry<String, BigDecimal> dong : luongDung.entrySet()) {
            if (dong.getValue().signum() == 0) {
                continue;
            }
            PhuTungSuDung nhap = null;
            for (PhuTungSuDung banGhi : hoSo.values()) {
                if (banGhi.getTrangThai() == TrangThaiSuDung.NHAP && maLenh.equals(banGhi.getMaLenhSuaChua())
                        && dong.getKey().equals(banGhi.getPhuTung().getMaPhuTung())) {
                    nhap = banGhi;
                }
            }
            String ma = nhap == null ? taoMaMoi() : nhap.getMaGhiNhan();
            hoSoMoi.put(ma, taoBanGhi(ma, maLenh, dong.getKey(), dong.getValue(), ghiChu,
                    TrangThaiSuDung.DA_GHI_NHAN));
            tonMoi.put(dong.getKey(), tonKho.get(dong.getKey()).subtract(dong.getValue()));
        }
        hoSo.clear();
        hoSo.putAll(hoSoMoi);
        tonKho.clear();
        tonKho.putAll(tonMoi);
        lenhChoPhuTung.remove(maLenh);
        return hoSo.get(banCu.getMaGhiNhan());
    }

    /**
     * Đánh dấu lệnh mẫu chờ phụ tùng, giữ nguyên nháp và kho.
     * @param taiKhoan tài khoản được phân công
     * @param banCu nháp chưa ghi nhận
     * @throws IllegalStateException nếu nháp không còn hợp lệ
     */
    // Lưu trạng thái chờ phụ tùng trong phiên thử mà không ghi nhận xuất kho.
    public synchronized void chuyenChoPhuTung(TaiKhoan taiKhoan, PhuTungSuDung banCu) {
        kiemTraNhap(taiKhoan, banCu);
        if (!lenhChoPhuTung.contains(banCu.getMaLenhSuaChua())) {
            lenhChoPhuTung.add(banCu.getMaLenhSuaChua());
        }
    }

    // Thêm danh mục và lượng tồn đầu phiên theo dữ liệu mẫu.
    private void themDanhMuc(String ma, String ten, String donVi, String ton, String toiThieu) {
        danhMuc.put(ma, new PhuTung(ma, ten, donVi, new BigDecimal(toiThieu)));
        tonKho.put(ma, new BigDecimal(ton));
    }

    // Tạo bản ghi với tên nhân viên của tài khoản kỹ thuật mẫu.
    private PhuTungSuDung taoBanGhi(String ma, String maLenh, String maPhuTung, BigDecimal soLuong,
            String ghiChu, TrangThaiSuDung trangThai) {
        return new PhuTungSuDung(ma, maLenh, docPhuTung(maPhuTung), soLuong,
                "Chung Ngô Minh Hoàng", ghiChu, trangThai);
    }

    // Sinh mã không trùng trong một phiên DAO mẫu.
    private String taoMaMoi() {
        String ma;
        do {
            ma = String.format(Locale.ROOT, "SD-2026-%04d", soTiepTheo++);
        } while (hoSo.containsKey(ma));
        return ma;
    }

    // Kiểm tra vai trò ở DAO trước mọi thao tác trên hồ sơ.
    private void kiemTraTaiKhoan(TaiKhoan taiKhoan) {
        if (taiKhoan == null || taiKhoan.getVaiTro() != VaiTro.NHAN_VIEN_KY_THUAT) {
            throw new SecurityException("Chức năng dành cho nhân viên kỹ thuật đã đăng nhập.");
        }
    }

    // Kiểm tra lệnh tồn tại và đúng người được phân công.
    private void kiemTraLenh(TaiKhoan taiKhoan, String maLenh) {
        kiemTraTaiKhoan(taiKhoan);
        if (!phamViDuyet.containsKey(maLenh)) {
            throw new IllegalArgumentException("Lệnh không tồn tại hoặc chưa được khách duyệt.");
        }
        if (!taiKhoan.getTenDangNhap().equals(phanCong.get(maLenh))) {
            throw new SecurityException("Lệnh không được phân công cho tài khoản hiện tại.");
        }
    }

    // Ngăn thao tác lên hồ sơ đã ghi nhận hoặc bản đã thay đổi từ lần đọc trước.
    private void kiemTraNhap(TaiKhoan taiKhoan, PhuTungSuDung banCu) {
        Objects.requireNonNull(banCu, "Chưa chọn hồ sơ.");
        kiemTraLenh(taiKhoan, banCu.getMaLenhSuaChua());
        if (hoSo.get(banCu.getMaGhiNhan()) != banCu || banCu.getTrangThai() != TrangThaiSuDung.NHAP) {
            throw new IllegalStateException("Hồ sơ đã ghi nhận hoặc đã thay đổi. Hãy tải lại danh sách.");
        }
    }

    // Kiểm tra lượng dùng tích lũy không vượt lượng khách đã duyệt.
    private void kiemTraPhamVi(TaiKhoan taiKhoan, String maLenh, String maPhuTung, BigDecimal soLuong) {
        if (soLuong.compareTo(docLuongConLai(taiKhoan, maLenh, maPhuTung)) > 0) {
            throw new IllegalArgumentException("Số lượng " + maPhuTung + " vượt lượng còn được khách duyệt.");
        }
    }
}