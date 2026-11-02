package entity.baocao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Lưu số công việc hoàn tất của ba nhóm trong một ngày
public final class ThongKeNgayKyThuat {
    private final LocalDate ngay;
    private final List<Integer> soLuongTheoNhom;

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
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

    /** Trả về ngay. */
    public LocalDate getNgay() {
        return ngay;
    }

    /** Trả về so luong theo nhom. */
    public List<Integer> getSoLuongTheoNhom() {
        return new ArrayList<Integer>(soLuongTheoNhom);
    }

    // Cộng số lượng của ba nhóm để tính tổng công việc hoàn tất trong ngày.
    /** Thực hiện tinh tong cong viec cho thành phần hiện tại. */
    public int tinhTongCongViec() {
        int tong = 0;
        for (Integer soLuong : soLuongTheoNhom) {
            tong += soLuong;
        }
        return tong;
    }
}