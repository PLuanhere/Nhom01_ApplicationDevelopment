package entity;

import entity.BaoGia.ChiTietBaoGia;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Giữ một đề xuất và phụ lục giá để khách xác nhận, không ghi đè báo giá gốc.
public final class DeXuatPhatSinh {
    private final long maPhatSinh;
    private final String maPhieu;
    private final int soThuTu;
    private final String huHong;
    private final String nguyenNhan;
    private final String deXuat;
    private final String trangThai;
    private final Integer thoiGianDuKienPhut;
    private final String nguoiBao;
    private final LocalDateTime thoiDiemBao;
    private final List<ChiTietBaoGia> chiTiet;
    private final String tenKhach;
    private final String kenh;
    private final String ghiChu;
    private final String nguoiGhiNhan;
    private final LocalDateTime thoiDiemPhanHoi;

    public DeXuatPhatSinh(long ma, String phieu, int thuTu, String huHong, String nguyenNhan, String deXuat,
            String trangThai, Integer phut, String nguoiBao, LocalDateTime lucBao, List<ChiTietBaoGia> chiTiet,
            String tenKhach, String kenh, String ghiChu, String nguoiGhiNhan, LocalDateTime lucPhanHoi) {
        this.maPhatSinh = ma;
        this.maPhieu = phieu;
        this.soThuTu = thuTu;
        this.huHong = huHong;
        this.nguyenNhan = nguyenNhan;
        this.deXuat = deXuat;
        this.trangThai = trangThai;
        this.thoiGianDuKienPhut = phut;
        this.nguoiBao = nguoiBao;
        this.thoiDiemBao = lucBao;
        this.chiTiet = Collections.unmodifiableList(new ArrayList<>(chiTiet));
        this.tenKhach = tenKhach;
        this.kenh = kenh;
        this.ghiChu = ghiChu;
        this.nguoiGhiNhan = nguoiGhiNhan;
        this.thoiDiemPhanHoi = lucPhanHoi;
    }

    public long getMaPhatSinh() {
        return maPhatSinh;
    }
    public String getMaPhieu() {
        return maPhieu;
    }
    public int getSoThuTu() {
        return soThuTu;
    }
    public String getHuHong() {
        return huHong;
    }
    public String getNguyenNhan() {
        return nguyenNhan;
    }
    public String getDeXuat() {
        return deXuat;
    }
    public String getTrangThai() {
        return trangThai;
    }
    public Integer getThoiGianDuKienPhut() {
        return thoiGianDuKienPhut;
    }
    public String getNguoiBao() {
        return nguoiBao;
    }
    public LocalDateTime getThoiDiemBao() {
        return thoiDiemBao;
    }
    public List<ChiTietBaoGia> getChiTiet() {
        return chiTiet;
    }
    public String getTenKhach() {
        return tenKhach;
    }
    public String getKenh() {
        return kenh;
    }
    public String getGhiChu() {
        return ghiChu;
    }
    public String getNguoiGhiNhan() {
        return nguoiGhiNhan;
    }
    public LocalDateTime getThoiDiemPhanHoi() {
        return thoiDiemPhanHoi;
    }

    // Hiển thị đề xuất trong danh sách chọn của tiếp nhận và kỹ thuật.
    @Override
    public String toString() {
        return "#" + soThuTu + " · " + huHong + " · " + getTenTrangThai();
    }

    // Tính tổng phụ lục và chặn tràn số khi cộng các dòng.
    public long getTongTien() {
        long tong = 0;
        for (ChiTietBaoGia dong : chiTiet) {
            tong = Math.addExact(tong, dong.getThanhTien());
        }
        return tong;
    }

    // Dịch trạng thái SQL thành nội dung dễ hiểu trên giao diện.
    public String getTenTrangThai() {
        if ("DA_DONG_Y".equals(trangThai)) {
            return "Khách đã đồng ý";
        }
        if ("TU_CHOI".equals(trangThai)) {
            return "Khách từ chối";
        }
        if ("DA_HUY".equals(trangThai)) {
            return "Đã hủy";
        }
        return chiTiet.isEmpty() ? "Chờ lập giá bổ sung" : "Chờ khách phản hồi";
    }

    // So sánh đúng nội dung giá mà nhân viên đã mở trước khi ghi quyết định khách.
    public boolean cungBaoGia(DeXuatPhatSinh khac) {
        if (khac == null || maPhatSinh != khac.maPhatSinh || !maPhieu.equals(khac.maPhieu) || !huHong.equals(khac.huHong)
                || !nguyenNhan.equals(khac.nguyenNhan) || !deXuat.equals(khac.deXuat)
                || !java.util.Objects.equals(thoiGianDuKienPhut, khac.thoiGianDuKienPhut)
                || chiTiet.size() != khac.chiTiet.size()) return false;
        for (int i = 0; i < chiTiet.size(); i++) {
            ChiTietBaoGia a = chiTiet.get(i), b = khac.chiTiet.get(i);
            if (!a.getMaDichVu().equals(b.getMaDichVu()) || !a.getMaPhuTung().equals(b.getMaPhuTung())
                    || !a.getTenHangMuc().equals(b.getTenHangMuc()) || !a.getLoai().equals(b.getLoai())
                    || !a.getDonVi().equals(b.getDonVi()) || a.getSoLuong() != b.getSoLuong()
                    || a.getDonGia() != b.getDonGia()) return false;
        }
        return true;
    }
}
