package entity;

import java.time.LocalDateTime;
import java.util.Objects;

// Đề xuất phát sinh và dấu vết phản hồi khách; không tự duyệt khi kỹ thuật gửi đề xuất.
public final class PhatSinhSuaChua {
    // Thứ tự: hư hỏng, nguyên nhân, đề xuất, thời gian thêm và ghi chú.
    private final String[] noiDung;

    private final TrangThaiPhatSinh trangThai;
    private final String nguoiXacNhan;
    private final LocalDateTime thoiDiemXacNhan;

    public PhatSinhSuaChua(String[] noiDung, TrangThaiPhatSinh trangThai,
            String nguoiXacNhan, LocalDateTime thoiDiemXacNhan) {
        if (noiDung == null || noiDung.length != 5) {
            throw new IllegalArgumentException("Đề xuất phát sinh phải có đủ năm trường thông tin.");
        }
        for (String giaTri : noiDung) {
            Objects.requireNonNull(giaTri);
        }
        if (noiDung[0].isBlank() || noiDung[1].isBlank() || noiDung[2].isBlank()) {
            throw new IllegalArgumentException("Cần nhập hư hỏng, nguyên nhân và đề xuất.");
        }
        this.trangThai = Objects.requireNonNull(trangThai);
        this.nguoiXacNhan = Objects.requireNonNull(nguoiXacNhan);
        if (trangThai != TrangThaiPhatSinh.CHO_DUYET
                && (nguoiXacNhan.isBlank() || thoiDiemXacNhan == null)) {
            throw new IllegalArgumentException("Phản hồi khách cần có người và thời điểm xác nhận.");
        }
        this.noiDung = noiDung.clone();
        this.thoiDiemXacNhan = thoiDiemXacNhan;
    }

    public String[] getNoiDung() { return noiDung.clone(); }
    public TrangThaiPhatSinh getTrangThai() { return trangThai; }
    public String getNguoiXacNhan() { return nguoiXacNhan; }
    public LocalDateTime getThoiDiemXacNhan() { return thoiDiemXacNhan; }
}