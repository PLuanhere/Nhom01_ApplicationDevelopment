package entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Báo cáo kiểm tra chỉ đọc; cập nhật bằng cách tạo bản mới thông qua DAO.
public final class BaoCaoKiemTra {
    private final String maBaoCao;
    private final PhieuTiepNhan phieu;
    private final LocalDate ngayKiemTra;
    private final String boPhan;
    // Thứ tự: kết quả, hư hỏng, đề xuất, phanh, dầu, điện, mã lỗi.
    private final String[] noiDung;

    // Trạng thái và thời điểm xác nhận hoàn tất; nháp chưa có thời điểm hoàn tất
    private final TrangThaiKiemTra trangThai;
    private final LocalDateTime thoiDiemHoanTat;

    /**
     * @param maBaoCao mã báo cáo, rỗng khi chưa lưu
     * @param phieu phiếu được giao kiểm tra
     * @param ngayKiemTra ngày kiểm tra
     * @param boPhan bộ phận hoặc Tổng hợp
     * @param noiDung lần lượt: kết quả, hư hỏng, đề xuất, phanh, dầu, điện, mã lỗi
     * @param trangThai trạng thái nghiệp vụ
     * @param thoiDiemHoanTat thời điểm hoàn tất, null khi còn nháp
     */
    public BaoCaoKiemTra(String maBaoCao, PhieuTiepNhan phieu, LocalDate ngayKiemTra,
            String boPhan, String[] noiDung, TrangThaiKiemTra trangThai, LocalDateTime thoiDiemHoanTat) {
        if (maBaoCao == null || phieu == null || ngayKiemTra == null || boPhan == null
                || noiDung == null || noiDung.length != 7 || trangThai == null) {
            throw new IllegalArgumentException("Thông tin báo cáo không hợp lệ.");
        }
        for (String giaTri : noiDung) {
            if (giaTri == null) {
                throw new IllegalArgumentException("Nội dung kiểm tra không được null.");
            }
        }
        if ((trangThai == TrangThaiKiemTra.HOAN_TAT) != (thoiDiemHoanTat != null)) {
            throw new IllegalArgumentException("Thời điểm hoàn tất không khớp trạng thái.");
        }
        this.maBaoCao = maBaoCao;
        this.phieu = phieu;
        this.ngayKiemTra = ngayKiemTra;
        this.boPhan = boPhan;
        this.noiDung = noiDung.clone();
        this.trangThai = trangThai;
        this.thoiDiemHoanTat = thoiDiemHoanTat;
    }

    public String getMaBaoCao() { return maBaoCao; }
    public PhieuTiepNhan getPhieu() { return phieu; }
    public LocalDate getNgayKiemTra() { return ngayKiemTra; }
    public String getBoPhan() { return boPhan; }
    public String[] getNoiDung() { return noiDung.clone(); }
    public TrangThaiKiemTra getTrangThai() { return trangThai; }
    public LocalDateTime getThoiDiemHoanTat() { return thoiDiemHoanTat; }
}