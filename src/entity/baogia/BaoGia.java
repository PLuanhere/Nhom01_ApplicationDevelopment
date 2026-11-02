package entity.baogia;

import entity.kiemtra.BaoCaoKiemTra;
import entity.kiemtra.TrangThaiKiemTra;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

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
        if (chiTiet == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.chiTiet = new ArrayList<>(chiTiet);
        maBaoGiaGoc = this.maBaoGia;
        phienBan = 1;
        baoCao = null;
        ngayHen = null;
        ngayLap = null;
        ngayGui = null;
        thoiDiemPhanHoi = null;
        thoiDiemGhiNhan = null;
        nguoiLap = "";
        nguoiGui = "";
        nguoiGhiNhan = "";
        kenhPhanHoi = "";
        ghiChuPhanHoi = "";
        trangThai = daDongY ? TrangThaiBaoGia.DA_DONG_Y : TrangThaiBaoGia.CHO_XAC_NHAN;
    }

    /** Tạo bản nháp đầy đủ. */
    public BaoGia(String ma, String maGoc, int phienBan, BaoCaoKiemTra baoCao, List<ChiTietBaoGia> chiTiet,
            LocalDateTime ngayHen, String nguoiLap, LocalDateTime ngayLap) {
        maBaoGia = batBuoc(ma, "Mã báo giá");
        maBaoGiaGoc = batBuoc(maGoc, "Mã nhóm báo giá");
        if (phienBan < 1) {
            throw new IllegalArgumentException("Phiên bản phải lớn hơn 0.");
        }
        this.phienBan = phienBan;
        if (baoCao == null) {
            throw new NullPointerException("Cần báo cáo kiểm tra.");
        }
        this.baoCao = baoCao;
        if (baoCao.getTrangThai() != TrangThaiKiemTra.HOAN_TAT || !baoCao.getPhieu().isDaXacNhan()) {
            throw new IllegalArgumentException("Cần kết quả kiểm tra hoàn tất của phiếu đã xác nhận.");
        }
        if (ngayHen == null) {
            throw new IllegalArgumentException("Cần ngày hẹn.");
        }
        this.ngayHen = ngayHen;
        if (ngayHen.isBefore(baoCao.getThoiDiemHoanTat())) {
            throw new IllegalArgumentException("Ngày hẹn không được trước khi hoàn tất kiểm tra.");
        }
        this.nguoiLap = batBuoc(nguoiLap, "Người lập");
        if (ngayLap == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.ngayLap = ngayLap;
        bienSo = baoCao.getPhieu().getBienSo();
        if (chiTiet == null || chiTiet.isEmpty()) {
            throw new IllegalArgumentException("Báo giá cần ít nhất một hạng mục.");
        }
        this.chiTiet = new ArrayList<>();
        for (ChiTietBaoGia ct : chiTiet) {
            if (ct == null) {
                throw new IllegalArgumentException("Hạng mục không được null.");
            }
            batBuoc(ct.getTenHangMuc(), "Tên hạng mục");
            batBuoc(ct.getLoai(), "Loại hạng mục");
            batBuoc(ct.getDonVi(), "Đơn vị");
            if ((ct.getSoLuong() <= 0) || (ct.getDonGia() < 0)) {
                throw new IllegalArgumentException("Số lượng phải dương, đơn giá không âm.");
            }
            this.chiTiet.add(ct);
        }
        trangThai = TrangThaiBaoGia.NHAP;
        ngayGui = null;
        thoiDiemPhanHoi = null;
        thoiDiemGhiNhan = null;
        nguoiGui = "";
        nguoiGhiNhan = "";
        kenhPhanHoi = "";
        ghiChuPhanHoi = "";
    }

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    private BaoGia(BaoGia cu, TrangThaiBaoGia trangThai, String nguoiGui, LocalDateTime ngayGui, String nguoiGhiNhan,
            LocalDateTime phanHoi, LocalDateTime ghiNhan, String kenh, String ghiChu) {
        maBaoGia = cu.maBaoGia;
        maBaoGiaGoc = cu.maBaoGiaGoc;
        bienSo = cu.bienSo;
        phienBan = cu.phienBan;
        baoCao = cu.baoCao;
        chiTiet = new ArrayList<>(cu.chiTiet);
        ngayHen = cu.ngayHen;
        nguoiLap = cu.nguoiLap;
        ngayLap = cu.ngayLap;
        this.trangThai = trangThai;
        this.nguoiGui = nguoiGui;
        this.ngayGui = ngayGui;
        this.nguoiGhiNhan = nguoiGhiNhan;
        thoiDiemPhanHoi = phanHoi;
        thoiDiemGhiNhan = ghiNhan;
        kenhPhanHoi = kenh;
        ghiChuPhanHoi = ghiChu;
    }

    /** Trả đối tượng mới, không sửa bản nháp mà UI đã đọc. */
    public BaoGia guiKhach(String nguoiGui, LocalDateTime lucGui) {
        if (trangThai != TrangThaiBaoGia.NHAP) {
            throw new IllegalStateException("Chỉ gửi được báo giá nháp.");
        }
        if (lucGui == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        return new BaoGia(this, TrangThaiBaoGia.CHO_XAC_NHAN, batBuoc(nguoiGui, "Người gửi"), lucGui, "", null, null,
                "", "");
    }

    /** Thực hiện ghi nhan phan hoi cho thành phần hiện tại. */
    public BaoGia ghiNhanPhanHoi(TrangThaiBaoGia quyetDinh, String nguoiNhan, LocalDateTime lucKhachPhanHoi,
            LocalDateTime lucGhiNhan, String kenh, String ghiChu) {
        if (trangThai != TrangThaiBaoGia.CHO_XAC_NHAN) {
            throw new IllegalStateException("Báo giá không còn chờ phản hồi.");
        }
        if (quyetDinh != TrangThaiBaoGia.DA_DONG_Y && quyetDinh != TrangThaiBaoGia.YEU_CAU_DIEU_CHINH
                && quyetDinh != TrangThaiBaoGia.TU_CHOI) {
            throw new IllegalArgumentException("Cần chọn đồng ý, điều chỉnh hoặc từ chối.");
        }
        if (lucKhachPhanHoi == null || lucGhiNhan == null) {
            throw new IllegalArgumentException("Cần thời điểm phản hồi và ghi nhận.");
        }
        if (lucKhachPhanHoi.isBefore(ngayGui.truncatedTo(ChronoUnit.MINUTES))) {
            throw new IllegalArgumentException("Phản hồi không được trước thời điểm gửi báo giá.");
        }
        if (lucKhachPhanHoi.isAfter(lucGhiNhan)) {
            throw new IllegalArgumentException("Phản hồi không được ở tương lai.");
        }
        return new BaoGia(this, quyetDinh, nguoiGui, ngayGui, batBuoc(nguoiNhan, "Người ghi nhận"), lucKhachPhanHoi,
                lucGhiNhan, batBuoc(kenh, "Kênh phản hồi"), ghiChu == null ? "" : ghiChu.trim());
    }

    /** Trả về ma bao gia. */
    public String getMaBaoGia() {
        return maBaoGia;
    }

    /** Trả về bien so. */
    public String getBienSo() {
        return bienSo;
    }

    /** Trả về chi tiet. */
    public List<ChiTietBaoGia> getChiTiet() {
        return new ArrayList<>(chiTiet);
    }

    /** Cho biết da dong y. */
    public boolean isDaDongY() {
        return trangThai == TrangThaiBaoGia.DA_DONG_Y;
    }

    /** Trả về ma bao gia goc. */
    public String getMaBaoGiaGoc() {
        return maBaoGiaGoc;
    }

    /** Trả về phien ban. */
    public int getPhienBan() {
        return phienBan;
    }

    /** Trả về bao cao. */
    public BaoCaoKiemTra getBaoCao() {
        return baoCao;
    }

    /** Trả về ngay hen. */
    public LocalDateTime getNgayHen() {
        return ngayHen;
    }

    /** Trả về ngay lap. */
    public LocalDateTime getNgayLap() {
        return ngayLap;
    }

    /** Trả về ngay gui. */
    public LocalDateTime getNgayGui() {
        return ngayGui;
    }

    /** Trả về thoi diem phan hoi. */
    public LocalDateTime getThoiDiemPhanHoi() {
        return thoiDiemPhanHoi;
    }

    /** Trả về thoi diem ghi nhan. */
    public LocalDateTime getThoiDiemGhiNhan() {
        return thoiDiemGhiNhan;
    }

    /** Trả về nguoi lap. */
    public String getNguoiLap() {
        return nguoiLap;
    }

    /** Trả về nguoi gui. */
    public String getNguoiGui() {
        return nguoiGui;
    }

    /** Trả về nguoi ghi nhan. */
    public String getNguoiGhiNhan() {
        return nguoiGhiNhan;
    }

    /** Trả về kenh phan hoi. */
    public String getKenhPhanHoi() {
        return kenhPhanHoi;
    }

    /** Trả về ghi chu phan hoi. */
    public String getGhiChuPhanHoi() {
        return ghiChuPhanHoi;
    }

    /** Trả về trang thai. */
    public TrangThaiBaoGia getTrangThai() {
        return trangThai;
    }

    /** Trả về tong tien. */
    public long getTongTien() {
        long tong = 0;
        for (ChiTietBaoGia ct : chiTiet) {
            long tien = ct.getThanhTien();
            if (Long.MAX_VALUE - tong < tien) {
                throw new IllegalArgumentException("Tổng báo giá vượt giới hạn.");
            }
            tong += tien;
        }
        return tong;
    }

    /** Thực hiện to string cho thành phần hiện tại. */
    @Override
    public String toString() {
        return maBaoGia + (isDaDongY() ? " · Khách đồng ý" : " · " + trangThai.getTenHienThi());
    }

    /** Thực hiện bat buoc cho thành phần hiện tại. */
    private static String batBuoc(String chu, String ten) {
        if (chu == null || chu.isBlank()) {
            throw new IllegalArgumentException(ten + " không được rỗng.");
        }
        return chu.trim();
    }
}
