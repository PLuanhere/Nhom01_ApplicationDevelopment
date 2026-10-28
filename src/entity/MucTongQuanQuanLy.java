package entity;

import java.time.LocalDateTime;
import java.util.Objects;

/** Thông tin tóm tắt của hồ sơ đang xử lý, phục vụ trang tổng quan Quản lý. */
public final class MucTongQuanQuanLy {

    /** Nhóm hồ sơ để tính bốn chỉ số, không phải quyền truy cập hoặc trạng thái chứng từ. */
    public enum Nhom {
        LENH_DANG_XU_LY, BAO_GIA_CHO_KHACH, XE_CHO_BAN_GIAO, PHU_TUNG_CANH_BAO
    }

    /** Nhóm, mã, thông tin đối chiếu và trạng thái hiện tại. */
    private final Nhom nhom;
    private final String ma;
    private final String moTa;
    private final String trangThai;
    /** Hạn xử lý của lệnh; null đối với hồ sơ không có hạn. */
    private final LocalDateTime hanXuLy;
    /** Cờ nhắc việc đã được xác định trong nguồn dữ liệu. */
    private final boolean canChuY;

    /**
     * Tạo một mục chỉ đọc; không thay thế entity nghiệp vụ của các module khác.
     * @param nhom nhóm hồ sơ
     * @param ma mã dùng đối chiếu
     * @param moTa khách/xe hoặc nội dung liên quan
     * @param trangThai nhãn trạng thái thực tế
     * @param hanXuLy hạn xử lý, có thể null
     * @param canChuY có cần nhắc việc hay không
     */
    public MucTongQuanQuanLy(Nhom nhom, String ma, String moTa, String trangThai,
            LocalDateTime hanXuLy, boolean canChuY) {
        this.nhom = Objects.requireNonNull(nhom);
        this.ma = Objects.requireNonNull(ma).trim();
        this.moTa = Objects.requireNonNull(moTa).trim();
        this.trangThai = Objects.requireNonNull(trangThai).trim();
        this.hanXuLy = hanXuLy;
        this.canChuY = canChuY;
        if (this.ma.isEmpty() || this.moTa.isEmpty() || this.trangThai.isEmpty()) {
            throw new IllegalArgumentException("Thông tin tổng quan không được rỗng.");
        }
    }

    /** @return nhóm hồ sơ */
    public Nhom getNhom() {
        return nhom;
    }

    /** @return mã hồ sơ */
    public String getMa() {
        return ma;
    }

    /** @return nội dung đối chiếu */
    public String getMoTa() {
        return moTa;
    }

    /** @return trạng thái hiện tại */
    public String getTrangThai() {
        return trangThai;
    }

    /** @return hạn xử lý hoặc null khi không áp dụng */
    public LocalDateTime getHanXuLy() {
        return hanXuLy;
    }

    /** @return hồ sơ đang cần chú ý */
    public boolean isCanChuY() {
        return canChuY;
    }
}
