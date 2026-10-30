package entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Lưu báo cáo kiểm tra bất biến và đề xuất tương ứng ba khu vực kiểm tra.
public final class BaoCaoKiemTra {
    private final String maBaoCao;
    private final PhieuTiepNhan phieu;
    private final LocalDate ngayKiemTra;
    private final String boPhan;
    // Thứ tự: kết quả, hư hỏng, đề xuất, phanh, dầu, điện, mã lỗi.
    private final String[] noiDung;
    private final String[] deXuatTheoVung;
    private final TrangThaiKiemTra trangThai;
    private final LocalDateTime thoiDiemHoanTat;

    public BaoCaoKiemTra(String maBaoCao, PhieuTiepNhan phieu, LocalDate ngayKiemTra,
            String boPhan, String[] noiDung, TrangThaiKiemTra trangThai, LocalDateTime thoiDiemHoanTat) {
        this(maBaoCao, phieu, ngayKiemTra, boPhan, noiDung, trangThai, thoiDiemHoanTat,
                new String[] {"", "", ""});
    }

    public BaoCaoKiemTra(String maBaoCao, PhieuTiepNhan phieu, LocalDate ngayKiemTra,
            String boPhan, String[] noiDung, TrangThaiKiemTra trangThai, LocalDateTime thoiDiemHoanTat,
            String[] deXuatTheoVung) {
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
        if (deXuatTheoVung == null || deXuatTheoVung.length != 3) {
            throw new IllegalArgumentException("Cần ba đề xuất theo khu vực kiểm tra.");
        }
        for (String deXuat : deXuatTheoVung) {
            if (deXuat == null) {
                throw new IllegalArgumentException("Đề xuất theo khu vực không được null.");
            }
        }
        this.maBaoCao = maBaoCao;
        this.phieu = phieu;
        this.ngayKiemTra = ngayKiemTra;
        this.boPhan = boPhan;
        this.noiDung = noiDung.clone();
        this.deXuatTheoVung = deXuatTheoVung.clone();
        this.trangThai = trangThai;
        this.thoiDiemHoanTat = thoiDiemHoanTat;
    }

    public String getMaBaoCao() {
        return maBaoCao;
    }

    public PhieuTiepNhan getPhieu() {
        return phieu;
    }

    public LocalDate getNgayKiemTra() {
        return ngayKiemTra;
    }

    public String getBoPhan() {
        return boPhan;
    }

    public String[] getNoiDung() {
        return noiDung.clone();
    }

    public String[] getDeXuatTheoVung() {
        return deXuatTheoVung.clone();
    }

    public TrangThaiKiemTra getTrangThai() {
        return trangThai;
    }

    public LocalDateTime getThoiDiemHoanTat() {
        return thoiDiemHoanTat;
    }
}