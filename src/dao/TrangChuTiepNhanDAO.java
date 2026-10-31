package dao;

import entity.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Tổng hợp từ chứng từ trong bộ nhớ; không tự gán trạng thái hoàn thành hay đã thanh toán. */
public final class TrangChuTiepNhanDAO {
    private final PhieuTiepNhanDAO tiepNhan;
    private final BaoCaoKiemTraDAO kiemTra;
    private final BaoGiaDAO baoGia;
    private final PhieuSuaChuaDAO suaChua;
    private PhuTungSuDungDAO phuTung;

    public TrangChuTiepNhanDAO(PhieuTiepNhanDAO tiepNhan, BaoCaoKiemTraDAO kiemTra, BaoGiaDAO baoGia, PhieuSuaChuaDAO suaChua) {
        this.tiepNhan = tiepNhan; this.kiemTra = kiemTra; this.baoGia = baoGia; this.suaChua = suaChua;
    }

    public TrangChuTiepNhanDAO(PhieuTiepNhanDAO tiepNhan, BaoCaoKiemTraDAO kiemTra, BaoGiaDAO baoGia,
            PhieuSuaChuaDAO suaChua, PhuTungSuDungDAO phuTung) {
        this(tiepNhan, kiemTra, baoGia, suaChua); this.phuTung = phuTung;
    }

    public List<PhieuSuaChua> layLenhTheoXe(TaiKhoan tk, String maXe) {
        List<PhieuTiepNhan> cacPhieu = tiepNhan.lichSuTheoXe(tk, maXe);
        List<PhieuSuaChua> ketQua = new ArrayList<>();
        for (PhieuSuaChua lenh : suaChua.timKiem(tk, "", "Tất cả")) {
            BaoGia bg = suaChua.layBaoGia(tk, lenh.getMaPhieu());
            if (cacPhieu.contains(bg.getBaoCao().getPhieu())) ketQua.add(lenh);
        }
        return ketQua;
    }

    public List<PhuTungSuDung> layThucDung(TaiKhoan tk, String maLenh) {
        suaChua.timTheoMa(tk, maLenh).orElseThrow(() -> new IllegalArgumentException("Lệnh không tồn tại."));
        return phuTung == null ? List.of() : phuTung.layThucDungChoTiepNhan(tk, maLenh);
    }

    public int demTiepNhan(TaiKhoan tk, LocalDate ngay) {
        int dem = 0;
        for (PhieuTiepNhan p : tiepNhan.timKiem(tk, "", null)) if (p.getThoiGianNhan().toLocalDate().equals(ngay)) dem++;
        return dem;
    }

    public int demChuaKiemTraXong(TaiKhoan tk) {
        List<String> daXong = new ArrayList<>();
        for (BaoCaoKiemTra bc : kiemTra.layBaoCaoHoanTatChoBaoGia(tk)) daXong.add(bc.getPhieu().getMaPhieu());
        int dem = 0;
        for (PhieuTiepNhan p : tiepNhan.timKiem(tk, "", null)) if (!daXong.contains(p.getMaPhieu())) dem++;
        return dem;
    }

    public int demChoKhach(TaiKhoan tk) {
        Map<String, BaoGia> moiNhat = new LinkedHashMap<>();
        for (BaoGia bg : baoGia.timKiem(tk, "", null)) {
            BaoGia cu = moiNhat.get(bg.getMaBaoGiaGoc());
            if (cu == null || bg.getPhienBan() > cu.getPhienBan()) moiNhat.put(bg.getMaBaoGiaGoc(), bg);
        }
        int dem = 0;
        for (BaoGia bg : moiNhat.values()) if (bg.getTrangThai() == TrangThaiBaoGia.CHO_XAC_NHAN) dem++;
        return dem;
    }

    public List<PhieuTiepNhan> layGanDay(TaiKhoan tk) {
        List<PhieuTiepNhan> ds = tiepNhan.timKiem(tk, "", null);
        ds.sort((a, b) -> b.getThoiGianNhan().compareTo(a.getThoiGianNhan()));
        return new ArrayList<>(ds.subList(0, Math.min(3, ds.size())));
    }

    public List<PhieuSuaChua> layHoanThanh(TaiKhoan tk) { return suaChua.timKiem(tk, "", "Hoàn thành"); }

    public String trangThaiPhieu(TaiKhoan tk, String maPhieu) {
        tiepNhan.timTheoMa(tk, maPhieu);
        BaoGia moiNhat = null;
        for (BaoGia bg : baoGia.timKiem(tk, "", null)) {
            if (bg.getBaoCao().getPhieu().getMaPhieu().equals(maPhieu)) {
                for (PhieuSuaChua lenh : suaChua.timKiem(tk, "", "Tất cả")) {
                    if (lenh.getMaBaoGia().equals(bg.getMaBaoGia())) return lenh.getTrangThai().getTenHienThi();
                }
                if (moiNhat == null || bg.getPhienBan() > moiNhat.getPhienBan()) moiNhat = bg;
            }
        }
        if (moiNhat != null) return moiNhat.getTrangThai().getTenHienThi();
        for (BaoCaoKiemTra bc : kiemTra.layBaoCaoChoTiepNhan(tk)) if (bc.getPhieu().getMaPhieu().equals(maPhieu)) {
            return bc.getTrangThai() == TrangThaiKiemTra.HOAN_TAT ? "Đã kiểm tra" : "Đang kiểm tra";
        }
        return "Chờ kiểm tra";
    }
}
