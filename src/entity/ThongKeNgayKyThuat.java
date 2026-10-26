package entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Lưu số công việc hoàn tất của ba nhóm trong một ngày
public final class ThongKeNgayKyThuat {
    private final LocalDate ngay;
    private final List<Integer> soLuongTheoNhom;

    /**
     * @param ngay ngày hoàn tất
     * @param soLuongTheoNhom số lượng của ba nhóm công việc
     * @throws IllegalArgumentException nếu ngày hoặc số lượng không hợp lệ
     */
    public ThongKeNgayKyThuat(LocalDate ngay, List<Integer> soLuongTheoNhom) {
        if (ngay == null || soLuongTheoNhom == null || soLuongTheoNhom.size() != 3) {
            throw new IllegalArgumentException("Cần ngày và số lượng của ba nhóm công việc.");
        }
        for (Integer soLuong : soLuongTheoNhom) {
            if (soLuong == null || soLuong < 0) {
                throw new IllegalArgumentException("Số lượng công việc không được âm hoặc null.");
            }
        }
        this.ngay = ngay;
        this.soLuongTheoNhom = new ArrayList<Integer>(soLuongTheoNhom);
    }

    public LocalDate getNgay() {
        return ngay;
    }

    public List<Integer> getSoLuongTheoNhom() {
        return new ArrayList<Integer>(soLuongTheoNhom);
    }

    /** @return tổng số công việc hoàn tất trong ngày */
    // Cộng số lượng của ba nhóm để tính tổng công việc hoàn tất trong ngày.
    public int tinhTongCongViec() {
        int tong = 0;
        for (Integer soLuong : soLuongTheoNhom) {
            tong += soLuong;
        }
        return tong;
    }
}