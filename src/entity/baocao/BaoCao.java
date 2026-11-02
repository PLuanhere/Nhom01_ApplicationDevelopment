package entity.baocao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Bản chụp kết quả thống kê; dữ liệu đã lập không đổi theo chứng từ về sau. */
public final class BaoCao {
    public static final String DOANH_THU = "Doanh thu";
    public static final String TIEP_NHAN = "Tiếp nhận và sửa chữa";
    public static final String TON_KHO = "Tồn kho";
    public static final String HIEU_SUAT = "Hiệu suất kỹ thuật viên";

    private final String maBaoCao;
    private final String loai;
    private final LocalDate tuNgay;
    private final LocalDate denNgay;
    private final LocalDateTime ngayLap;
    private final String nguoiLap;
    private final String[] tenChiSo;
    private final long[] giaTri;
    private final String[] tenCot;
    private final List<String[]> cacDong;

    /** Giữ khoảng ngày, chỉ số và bản sao từng dòng kết quả. */
    public BaoCao(String ma, String loai, LocalDate tu, LocalDate den, LocalDateTime ngayLap, String nguoi,
            String[] tenChiSo, long[] giaTri, String[] tenCot, List<String[]> cacDong) {
        if (ma == null || loai == null || tu == null || den == null || tu.isAfter(den) || ngayLap == null
                || nguoi == null || nguoi.isBlank() || tenChiSo == null || giaTri == null || tenCot == null
                || cacDong == null || tenChiSo.length != giaTri.length) {
            throw new IllegalArgumentException("Thông tin báo cáo chưa hợp lệ.");
        }
        if (!loai.equals(DOANH_THU) && !loai.equals(TIEP_NHAN) && !loai.equals(TON_KHO) && !loai.equals(HIEU_SUAT)) {
            throw new IllegalArgumentException("Loại báo cáo không hợp lệ.");
        }
        for (long so : giaTri) {
            if (so < 0) {
                throw new IllegalArgumentException("Chỉ số báo cáo không được âm.");
            }
        }
        for (String[] dong : cacDong) {
            if (dong == null || dong.length != tenCot.length) {
                throw new IllegalArgumentException("Thiếu cột báo cáo.");
            }
            for (String chu : dong) {
                if (chu == null) {
                    throw new IllegalArgumentException("Ô báo cáo không được null.");
                }
            }
        }
        this.maBaoCao = ma;
        this.loai = loai;
        this.tuNgay = tu;
        this.denNgay = den;
        this.ngayLap = ngayLap;
        this.nguoiLap = nguoi;
        this.tenChiSo = tenChiSo.clone();
        this.giaTri = giaTri.clone();
        this.tenCot = tenCot.clone();
        this.cacDong = saoChep(cacDong);
    }

    /** Sao chép cả danh sách và từng mảng để bảo vệ bản chụp. */
    private static List<String[]> saoChep(List<String[]> nguon) {
        List<String[]> ketQua = new ArrayList<>();
        for (String[] dong : nguon) {
            ketQua.add(dong.clone());
        }
        return ketQua;
    }

    /** Trả mã báo cáo; rỗng khi kết quả chưa được lưu. */
    public String getMaBaoCao() {
        return maBaoCao;
    }

    /** Trả loại thống kê. */
    public String getLoai() {
        return loai;
    }

    /** Trả ngày đầu kỳ. */
    public LocalDate getTuNgay() {
        return tuNgay;
    }

    /** Trả ngày cuối kỳ. */
    public LocalDate getDenNgay() {
        return denNgay;
    }

    /** Trả thời điểm chụp số liệu. */
    public LocalDateTime getNgayLap() {
        return ngayLap;
    }

    /** Trả tài khoản lập kết quả. */
    public String getNguoiLap() {
        return nguoiLap;
    }

    /** Trả bản sao tên các chỉ số tổng hợp. */
    public String[] getTenChiSo() {
        return tenChiSo.clone();
    }

    /** Trả bản sao giá trị; tiền nguyên đồng, các chỉ số khác là số đếm. */
    public long[] getGiaTri() {
        return giaTri.clone();
    }

    /** Trả bản sao tiêu đề bảng. */
    public String[] getTenCot() {
        return tenCot.clone();
    }

    /** Trả bản sao các dòng báo cáo. */
    public List<String[]> getCacDong() {
        return saoChep(cacDong);
    }
}
