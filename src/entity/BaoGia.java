package entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Một phiên bản bất biến: nội dung được gửi khách không thay đổi theo bản điều chỉnh. */
public final class BaoGia {
    private final String maBaoGia, bienSo, maBaoGiaGoc;
    private final List<ChiTietBaoGia> chiTiet;
    private final int phienBan;
    private final BaoCaoKiemTra baoCao;
    private final LocalDateTime ngayHen, ngayLap, ngayGui, thoiDiemPhanHoi, thoiDiemGhiNhan;
    private final String nguoiLap, nguoiGui, nguoiGhiNhan, kenhPhanHoi, ghiChuPhanHoi;
    private final TrangThaiBaoGia trangThai;

    /** Giữ constructor mà phân hệ sửa chữa/phụ tùng đang dùng cho fixture cũ. */
    public BaoGia(String maBaoGia, String bienSo, boolean daDongY, List<ChiTietBaoGia> chiTiet) {
        this.maBaoGia = batBuoc(maBaoGia, "Mã báo giá");
        this.bienSo = batBuoc(bienSo, "Biển số");
        this.chiTiet = new ArrayList<>(Objects.requireNonNull(chiTiet));
        maBaoGiaGoc = this.maBaoGia; phienBan = 1; baoCao = null;
        ngayHen = null; ngayLap = null; ngayGui = null; thoiDiemPhanHoi = null; thoiDiemGhiNhan = null;
        nguoiLap = ""; nguoiGui = ""; nguoiGhiNhan = ""; kenhPhanHoi = ""; ghiChuPhanHoi = "";
        trangThai = daDongY ? TrangThaiBaoGia.DA_DONG_Y : TrangThaiBaoGia.CHO_XAC_NHAN;
    }

    /** Tạo bản nháp đầy đủ. DAO kiểm tra nguồn báo cáo và số phiên bản trước khi ghi. */
    public BaoGia(String ma, String maGoc, int phienBan, BaoCaoKiemTra baoCao,
            List<ChiTietBaoGia> chiTiet, LocalDateTime ngayHen, String nguoiLap, LocalDateTime ngayLap) {
        maBaoGia = batBuoc(ma, "Mã báo giá"); maBaoGiaGoc = batBuoc(maGoc, "Mã nhóm báo giá");
        if (phienBan < 1) throw new IllegalArgumentException("Phiên bản phải lớn hơn 0.");
        this.phienBan = phienBan; this.baoCao = Objects.requireNonNull(baoCao, "Cần báo cáo kiểm tra.");
        if (baoCao.getTrangThai() != TrangThaiKiemTra.HOAN_TAT || !baoCao.getPhieu().isDaXacNhan()) {
            throw new IllegalArgumentException("Cần kết quả kiểm tra hoàn tất của phiếu đã xác nhận.");
        }
        if (ngayHen == null) throw new IllegalArgumentException("Cần ngày hẹn.");
        this.ngayHen = ngayHen;
        if (ngayHen.isBefore(baoCao.getThoiDiemHoanTat())) throw new IllegalArgumentException("Ngày hẹn không được trước khi hoàn tất kiểm tra.");
        this.nguoiLap = batBuoc(nguoiLap, "Người lập"); this.ngayLap = Objects.requireNonNull(ngayLap);
        bienSo = baoCao.getPhieu().getBienSo();
        if (chiTiet == null || chiTiet.isEmpty()) throw new IllegalArgumentException("Báo giá cần ít nhất một hạng mục.");
        this.chiTiet = new ArrayList<>();
        for (ChiTietBaoGia ct : chiTiet) {
            if (ct == null) throw new IllegalArgumentException("Hạng mục không được null.");
            batBuoc(ct.getTenHangMuc(), "Tên hạng mục"); batBuoc(ct.getLoai(), "Loại hạng mục");
            batBuoc(ct.getDonVi(), "Đơn vị");
            if (ct.getSoLuong().signum() <= 0 || ct.getDonGia().signum() < 0) throw new IllegalArgumentException("Số lượng phải dương, đơn giá không âm.");
            this.chiTiet.add(ct);
        }
        trangThai = TrangThaiBaoGia.NHAP; ngayGui = null; thoiDiemPhanHoi = null; thoiDiemGhiNhan = null;
        nguoiGui = ""; nguoiGhiNhan = ""; kenhPhanHoi = ""; ghiChuPhanHoi = "";
    }

    private BaoGia(BaoGia cu, TrangThaiBaoGia trangThai, String nguoiGui, LocalDateTime ngayGui,
            String nguoiGhiNhan, LocalDateTime phanHoi, LocalDateTime ghiNhan, String kenh, String ghiChu) {
        maBaoGia = cu.maBaoGia; maBaoGiaGoc = cu.maBaoGiaGoc; bienSo = cu.bienSo; phienBan = cu.phienBan;
        baoCao = cu.baoCao; chiTiet = new ArrayList<>(cu.chiTiet); ngayHen = cu.ngayHen;
        nguoiLap = cu.nguoiLap; ngayLap = cu.ngayLap; this.trangThai = trangThai;
        this.nguoiGui = nguoiGui; this.ngayGui = ngayGui; this.nguoiGhiNhan = nguoiGhiNhan;
        thoiDiemPhanHoi = phanHoi; thoiDiemGhiNhan = ghiNhan; kenhPhanHoi = kenh; ghiChuPhanHoi = ghiChu;
    }

    /** Trả đối tượng mới, không sửa bản nháp mà UI đã đọc. Chỉ DAO ghi vào kho. */
    public BaoGia guiKhach(String nguoiGui, LocalDateTime lucGui) {
        if (trangThai != TrangThaiBaoGia.NHAP) throw new IllegalStateException("Chỉ gửi được báo giá nháp.");
        return new BaoGia(this, TrangThaiBaoGia.CHO_XAC_NHAN, batBuoc(nguoiGui, "Người gửi"), Objects.requireNonNull(lucGui), "", null, null, "", "");
    }

    public BaoGia ghiNhanPhanHoi(TrangThaiBaoGia quyetDinh, String nguoiNhan, LocalDateTime lucKhachPhanHoi,
            LocalDateTime lucGhiNhan, String kenh, String ghiChu) {
        if (trangThai != TrangThaiBaoGia.CHO_XAC_NHAN) throw new IllegalStateException("Báo giá không còn chờ phản hồi.");
        if (quyetDinh != TrangThaiBaoGia.DA_DONG_Y && quyetDinh != TrangThaiBaoGia.YEU_CAU_DIEU_CHINH && quyetDinh != TrangThaiBaoGia.TU_CHOI) {
            throw new IllegalArgumentException("Cần chọn đồng ý, điều chỉnh hoặc từ chối.");
        }
        if (lucKhachPhanHoi == null || lucGhiNhan == null) throw new IllegalArgumentException("Cần thời điểm phản hồi và ghi nhận.");
        if (lucKhachPhanHoi.isBefore(ngayGui.truncatedTo(java.time.temporal.ChronoUnit.MINUTES))) {
            throw new IllegalArgumentException("Phản hồi không được trước thời điểm gửi báo giá.");
        }
        if (lucKhachPhanHoi.isAfter(lucGhiNhan)) throw new IllegalArgumentException("Phản hồi không được ở tương lai.");
        return new BaoGia(this, quyetDinh, nguoiGui, ngayGui, batBuoc(nguoiNhan, "Người ghi nhận"),
                lucKhachPhanHoi, Objects.requireNonNull(lucGhiNhan), batBuoc(kenh, "Kênh phản hồi"), ghiChu == null ? "" : ghiChu.strip());
    }

    public String getMaBaoGia() { return maBaoGia; }
    public String getBienSo() { return bienSo; }
    public List<ChiTietBaoGia> getChiTiet() { return new ArrayList<>(chiTiet); }
    public boolean isDaDongY() { return trangThai == TrangThaiBaoGia.DA_DONG_Y; }
    public String getMaBaoGiaGoc() { return maBaoGiaGoc; }
    public int getPhienBan() { return phienBan; }
    public BaoCaoKiemTra getBaoCao() { return baoCao; }
    public LocalDateTime getNgayHen() { return ngayHen; }
    public LocalDateTime getNgayLap() { return ngayLap; }
    public LocalDateTime getNgayGui() { return ngayGui; }
    public LocalDateTime getThoiDiemPhanHoi() { return thoiDiemPhanHoi; }
    public LocalDateTime getThoiDiemGhiNhan() { return thoiDiemGhiNhan; }
    public String getNguoiLap() { return nguoiLap; }
    public String getNguoiGui() { return nguoiGui; }
    public String getNguoiGhiNhan() { return nguoiGhiNhan; }
    public String getKenhPhanHoi() { return kenhPhanHoi; }
    public String getGhiChuPhanHoi() { return ghiChuPhanHoi; }
    public TrangThaiBaoGia getTrangThai() { return trangThai; }
    public BigDecimal getTongTien() { BigDecimal tong = BigDecimal.ZERO; for (ChiTietBaoGia ct : chiTiet) tong = tong.add(ct.getThanhTien()); return tong; }
    @Override public String toString() { return maBaoGia + (isDaDongY() ? " · Khách đồng ý" : " · " + trangThai.getTenHienThi()); }
    private static String batBuoc(String chu, String ten) { if (chu == null || chu.isBlank()) throw new IllegalArgumentException(ten + " không được rỗng."); return chu.strip(); }
}
