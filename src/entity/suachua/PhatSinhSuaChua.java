package entity.suachua;

import java.time.LocalDateTime;

// Đề xuất phát sinh và dấu vết phản hồi khách; không tự duyệt khi kỹ thuật gửi đề xuất.
public final class PhatSinhSuaChua {
    // Thứ tự: hư hỏng, nguyên nhân, đề xuất, thời gian thêm và ghi chú.
    private final String[] noiDung;

    private final TrangThaiPhatSinh trangThai;
    private final String nguoiXacNhan;
    private final LocalDateTime thoiDiemXacNhan;

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public PhatSinhSuaChua(String[] noiDung, TrangThaiPhatSinh trangThai, String nguoiXacNhan,
            LocalDateTime thoiDiemXacNhan) {
        if (noiDung == null || noiDung.length != 5) {
            throw new IllegalArgumentException("Đề xuất phát sinh phải có đủ năm trường thông tin.");
        }
        for (String giaTri : noiDung) {
            if (giaTri == null) {
                throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
            }

        }
        if (noiDung[0].isBlank() || noiDung[1].isBlank() || noiDung[2].isBlank()) {
            throw new IllegalArgumentException("Cần nhập hư hỏng, nguyên nhân và đề xuất.");
        }
        if (trangThai == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.trangThai = trangThai;
        if (nguoiXacNhan == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.nguoiXacNhan = nguoiXacNhan;
        if (trangThai != TrangThaiPhatSinh.CHO_DUYET && (nguoiXacNhan.isBlank() || thoiDiemXacNhan == null)) {
            throw new IllegalArgumentException("Phản hồi khách cần có người và thời điểm xác nhận.");
        }
        this.noiDung = noiDung.clone();
        this.thoiDiemXacNhan = thoiDiemXacNhan;
    }

    /** Trả về noi dung. */
    public String[] getNoiDung() {
        return noiDung.clone();
    }

    /** Trả về trang thai. */
    public TrangThaiPhatSinh getTrangThai() {
        return trangThai;
    }

    /** Trả về nguoi xac nhan. */
    public String getNguoiXacNhan() {
        return nguoiXacNhan;
    }

    /** Trả về thoi diem xac nhan. */
    public LocalDateTime getThoiDiemXacNhan() {
        return thoiDiemXacNhan;
    }
}