package dao;

import entity.*;
import java.time.*;
import java.util.*;
import java.util.function.Function;

/** Kho bàn giao trong bộ nhớ; nguồn thu tiền/phê duyệt chỉ đọc, luôn kiểm tra lại khi xác nhận. */
public final class BanGiaoXeDAO {
    private final PhieuSuaChuaDAO suaChua;
    private final Function<String, CanCuBanGiao> nguonCanCu;
    private final Map<String, BanGiaoXe> theoLenh = new LinkedHashMap<>();
    private final Map<String, String> theoThaoTac = new LinkedHashMap<>();
    private int soThuTu = 1;

    public BanGiaoXeDAO(PhieuSuaChuaDAO suaChua, Function<String, CanCuBanGiao> nguonCanCu) {
        this.suaChua = Objects.requireNonNull(suaChua); this.nguonCanCu = Objects.requireNonNull(nguonCanCu);
    }

    public synchronized PhieuSuaChua docLenh(TaiKhoan tk, String ma) {
        quyen(tk); return suaChua.timTheoMa(tk, ma).orElseThrow(() -> new IllegalArgumentException("Lệnh sửa chữa không tồn tại."));
    }
    public synchronized BaoGia docBaoGia(TaiKhoan tk, String ma) { docLenh(tk, ma); return suaChua.layBaoGia(tk, ma); }
    public synchronized CanCuBanGiao docCanCu(TaiKhoan tk, String ma) { docLenh(tk, ma); return nguonCanCu.apply(ma); }
    public synchronized Optional<BanGiaoXe> timTheoLenh(TaiKhoan tk, String ma) { quyen(tk); return Optional.ofNullable(theoLenh.get(ma)); }

    public synchronized String lyDoChuaDu(TaiKhoan tk, String ma) {
        PhieuSuaChua lenh = docLenh(tk, ma);
        return lyDo(lenh, docBaoGia(tk, ma), docCanCu(tk, ma));
    }

    private String lyDo(PhieuSuaChua lenh, BaoGia bg, CanCuBanGiao canCu) {
        if (lenh.getTrangThai() != TrangThaiSuaChua.HOAN_THANH || lenh.getThoiDiemHoanTat() == null) return "Xe chưa hoàn thành sửa chữa.";
        for (HangMucSuaChua hm : lenh.getDanhSachHangMuc()) if (hm.getTrangThai() != TrangThaiSuaChua.HOAN_THANH || hm.getKetQua().isBlank()) return "Còn hạng mục chưa hoàn thành.";
        if (!bg.isDaDongY()) return "Thiếu báo giá được khách đồng ý của lệnh.";
        if (bg.getBaoCao() == null) return "Thiếu liên kết phiếu tiếp nhận của lệnh.";
        if (canCu == null) return "Chưa có xác nhận thanh toán/phê duyệt từ nguồn phụ trách.";
        if (!canCu.getMaLenh().equals(lenh.getMaPhieu())) return "Căn cứ thanh toán/phê duyệt không thuộc lệnh đang giao.";
        if (canCu.getThoiDiem().isAfter(LocalDateTime.now())) return "Thời điểm xác nhận căn cứ không hợp lệ.";
        if (!canCu.duDieuKien()) return "Khách chưa thanh toán đủ và chưa có phê duyệt ngoại lệ.";
        return "";
    }

    public synchronized TrangThaiBanGiao trangThai(TaiKhoan tk, String ma) {
        quyen(tk); if (theoLenh.containsKey(ma)) return TrangThaiBanGiao.DA_GIAO;
        return lyDoChuaDu(tk, ma).isEmpty() ? TrangThaiBanGiao.CHO_GIAO : TrangThaiBanGiao.CHO_CAN_CU;
    }

    /** Chỉ ghi một lần cho lệnh; lặp đúng cùng thao tác trả chứng từ cũ và giữ thời điểm cũ. */
    public synchronized BanGiaoXe xacNhan(TaiKhoan tk, String token, String maLenh, CanCuBanGiao daDoc,
            String nguoiNhan, LocalDateTime lucGiao, String tinhTrang, String taiSan, String ghiChu,
            String hinhThuc, boolean khachXacNhan) {
        quyen(tk);
        if (token == null || token.isBlank()) throw new IllegalArgumentException("Thiếu mã thao tác bàn giao.");
        String daLuu = theoThaoTac.get(token);
        if (daLuu != null) {
            BanGiaoXe cu = theoLenh.get(daLuu);
            if (!cungThaoTac(cu, tk, maLenh, daDoc, nguoiNhan, lucGiao, tinhTrang, taiSan, ghiChu, hinhThuc, khachXacNhan)) throw new IllegalArgumentException("Mã thao tác đã lưu một nội dung bàn giao khác.");
            return cu;
        }
        if (theoLenh.containsKey(maLenh)) throw new IllegalStateException("Lệnh đã bàn giao; mở lịch sử để xem, không ghi lần nữa.");
        PhieuSuaChua lenh = docLenh(tk, maLenh); BaoGia bg = docBaoGia(tk, maLenh);
        CanCuBanGiao hienTai = docCanCu(tk, maLenh);
        String lyDo = lyDo(lenh, bg, hienTai);
        if (!lyDo.isEmpty()) throw new IllegalStateException(lyDo);
        if (!hienTai.cungNoiDung(daDoc)) throw new IllegalStateException("Thanh toán/phê duyệt đã thay đổi. Mở lại lệnh để đối chiếu.");
        LocalDateTime lucLuu = LocalDateTime.now();
        String ma = String.format(Locale.ROOT, "GX-%04d-%04d", lucGiao == null ? lucLuu.getYear() : lucGiao.getYear(), soThuTu);
        BanGiaoXe moi = new BanGiaoXe(ma, lenh, bg, hienTai, tk, nguoiNhan, lucGiao, tinhTrang, taiSan, ghiChu, hinhThuc, khachXacNhan, lucLuu);
        theoLenh.put(maLenh, moi); theoThaoTac.put(token, maLenh); soThuTu++; return moi;
    }

    /** Với xe chưa giao, bộ lọc ngày dùng ngày hoàn tất sửa chữa; xe đã giao dùng ngày giao thực tế. */
    public synchronized List<PhieuSuaChua> timKiem(TaiKhoan tk, String tuKhoa, TrangThaiBanGiao trangThai, LocalDate tu, LocalDate den) {
        quyen(tk);
        if (tu != null && den != null && tu.isAfter(den)) throw new IllegalArgumentException("Từ ngày không được sau đến ngày.");
        String khoa = Objects.requireNonNull(tuKhoa).strip().toLowerCase(Locale.ROOT);
        String bien = khoa.toUpperCase(Locale.ROOT).replaceAll("[\\s.\\-]", "");
        List<PhieuSuaChua> ketQua = new ArrayList<>();
        for (PhieuSuaChua lenh : suaChua.timKiem(tk, "", "Hoàn thành")) {
            BanGiaoXe giao = theoLenh.get(lenh.getMaPhieu());
            LocalDate ngay = giao == null ? lenh.getThoiDiemHoanTat() == null ? null : lenh.getThoiDiemHoanTat().toLocalDate() : giao.getThoiDiemGiao().toLocalDate();
            if (ngay == null || (tu != null && ngay.isBefore(tu)) || (den != null && ngay.isAfter(den))) continue;
            BaoGia bg = docBaoGia(tk, lenh.getMaPhieu());
            String chu = lenh.getMaPhieu() + " " + lenh.getBienSo() + " " + (bg.getBaoCao() == null ? "" : bg.getBaoCao().getPhieu().getTenKhach());
            if (giao != null) chu += " " + giao.getMaBanGiao() + " " + giao.getNguoiNhan() + " " + giao.getNguoiGiao();
            if (trangThai != null && trangThai(tk, lenh.getMaPhieu()) != trangThai) continue;
            if (chu.toLowerCase(Locale.ROOT).contains(khoa) || (!bien.isEmpty() && Xe.chuanHoaBienSo(lenh.getBienSo()).contains(bien))) ketQua.add(lenh);
        }
        return ketQua;
    }

    public synchronized List<BanGiaoXe> lichSuTheoXe(TaiKhoan tk, String maXe) {
        quyen(tk); List<BanGiaoXe> ds = new ArrayList<>();
        for (BanGiaoXe g : theoLenh.values()) if (g.getMaXe().equals(maXe)) ds.add(g);
        return ds;
    }

    private boolean cungThaoTac(BanGiaoXe g, TaiKhoan tk, String ma, CanCuBanGiao canCu, String nhan, LocalDateTime luc,
            String tinhTrang, String taiSan, String ghiChu, String hinhThuc, boolean xacNhan) {
        return g != null && xacNhan && g.getMaLenh().equals(ma) && g.getNguoiGiao().equals(tk.getTenDangNhap())
                && g.getCanCu().cungNoiDung(canCu) && g.getNguoiNhan().equals(chu(nhan)) && g.getThoiDiemGiao().equals(luc)
                && g.getTinhTrang().equals(chu(tinhTrang)) && g.getTaiSanDaTra().equals(chu(taiSan))
                && g.getGhiChu().equals(chu(ghiChu)) && g.getHinhThucXacNhan().equals(chu(hinhThuc));
    }
    private String chu(String s) { return s == null ? "" : s.strip(); }
    private void quyen(TaiKhoan tk) { if (tk == null || tk.getVaiTro() != VaiTro.NHAN_VIEN_TIEP_NHAN) throw new SecurityException("Chức năng bàn giao dành cho tiếp nhận."); }
}
