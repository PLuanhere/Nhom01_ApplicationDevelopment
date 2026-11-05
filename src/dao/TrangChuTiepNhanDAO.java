package dao;

import dao.BanGiaoXeDAO;
import dao.BaoGiaDAO;
import dao.HoaDonDAO;
import dao.BaoCaoKiemTraDAO;
import dao.PhuTungSuDungDAO;
import dao.PhieuSuaChuaDAO;
import dao.PhieuTiepNhanDAO;
import entity.BanGiaoXe.TrangThaiBanGiao;
import entity.BaoGia;
import entity.BaoGia.TrangThaiBaoGia;
import entity.PhieuTiepNhan.BaoCaoKiemTra;
import entity.PhieuTiepNhan.TrangThaiKiemTra;
import entity.TaiKhoan;
import dao.PhuTungSuDungDAO.PhuTungSuDung;
import entity.PhieuSuaChua;
import entity.PhieuSuaChua.TrangThaiSuaChua;
import entity.PhieuTiepNhan;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Tổng hợp từ chứng từ trong bộ nhớ; không tự gán trạng thái hoàn thành hay đã thanh toán. */
public final class TrangChuTiepNhanDAO {
    private final PhieuTiepNhanDAO tiepNhan;
    private final BaoCaoKiemTraDAO kiemTra;
    private final BaoGiaDAO baoGia;
    private final PhieuSuaChuaDAO suaChua;
    private PhuTungSuDungDAO phuTung;
    private BanGiaoXeDAO banGiao;

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public TrangChuTiepNhanDAO(PhieuTiepNhanDAO tiepNhan, BaoCaoKiemTraDAO kiemTra, BaoGiaDAO baoGia,
            PhieuSuaChuaDAO suaChua) {
        this.tiepNhan = tiepNhan;
        this.kiemTra = kiemTra;
        this.baoGia = baoGia;
        this.suaChua = suaChua;
        banGiao = new BanGiaoXeDAO(suaChua, new HoaDonDAO(suaChua));
    }

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public TrangChuTiepNhanDAO(PhieuTiepNhanDAO tiepNhan, BaoCaoKiemTraDAO kiemTra, BaoGiaDAO baoGia,
            PhieuSuaChuaDAO suaChua, PhuTungSuDungDAO phuTung, BanGiaoXeDAO banGiao) {
        this(tiepNhan, kiemTra, baoGia, suaChua, phuTung);
        if (banGiao == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.banGiao = banGiao;
    }

    /** Chỉ lấy lệnh chưa giao có đủ căn cứ hiện tại; lỗi nguồn phải báo ra, không coi là không có xe. */
    public List<PhieuSuaChua> layChoBanGiao(TaiKhoan tk) {
        List<PhieuSuaChua> ds = banGiao.timKiem(tk, "", TrangThaiBanGiao.CHO_GIAO, null, null);

        for (int i = 0; i < ds.size() - 1; i++) {
            for (int j = i + 1; j < ds.size(); j++) {
                if (ds.get(i).getThoiDiemHoanTat().isAfter(ds.get(j).getThoiDiemHoanTat())) {
                    PhieuSuaChua cu = ds.get(i);
                    ds.set(i, ds.get(j));
                    ds.set(j, cu);
                }
            }
        }
        return ds;
    }

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public TrangChuTiepNhanDAO(PhieuTiepNhanDAO tiepNhan, BaoCaoKiemTraDAO kiemTra, BaoGiaDAO baoGia,
            PhieuSuaChuaDAO suaChua, PhuTungSuDungDAO phuTung) {
        this(tiepNhan, kiemTra, baoGia, suaChua);
        this.phuTung = phuTung;
    }

    /** Đọc lenh theo xe từ dữ liệu hiện tại. */
    public List<PhieuSuaChua> layLenhTheoXe(TaiKhoan tk, String maXe) {
        List<PhieuTiepNhan> cacPhieu = tiepNhan.lichSuTheoXe(tk, maXe);
        List<PhieuSuaChua> ketQua = new ArrayList<>();
        for (PhieuSuaChua lenh : suaChua.timKiem(tk, "", "Tất cả")) {
            BaoGia bg = suaChua.layBaoGia(tk, lenh.getMaPhieu());
            if (cacPhieu.contains(bg.getBaoCao().getPhieu())) {
                ketQua.add(lenh);
            }
        }
        return ketQua;
    }

    /** Đọc thuc dung từ dữ liệu hiện tại. */
    public List<PhuTungSuDung> layThucDung(TaiKhoan tk, String maLenh) {
        PhieuSuaChua ketQuaTraCuu1 = suaChua.timTheoMa(tk, maLenh);
        if (ketQuaTraCuu1 == null) {
            throw new IllegalArgumentException("Lệnh không tồn tại.");
        }

        return phuTung == null ? new ArrayList<>() : phuTung.layThucDungChoTiepNhan(tk, maLenh);
    }

    /** Thực hiện dem tiep nhan cho thành phần hiện tại. */
    public int demTiepNhan(TaiKhoan tk, LocalDate ngay) {
        int dem = 0;
        for (PhieuTiepNhan p : tiepNhan.timKiem(tk, "", null)) {
            if (p.getThoiGianNhan().toLocalDate().equals(ngay)) {
                dem++;
            }
        }
        return dem;
    }

    /** Thực hiện dem chua kiem tra xong cho thành phần hiện tại. */
    public int demChuaKiemTraXong(TaiKhoan tk) {
        List<String> daXong = new ArrayList<>();
        for (BaoCaoKiemTra bc : kiemTra.layBaoCaoHoanTatChoBaoGia(tk)) {
            daXong.add(bc.getPhieu().getMaPhieu());
        }
        int dem = 0;
        for (PhieuTiepNhan p : tiepNhan.timKiem(tk, "", null)) {
            if (!daXong.contains(p.getMaPhieu())) {
                dem++;
            }
        }
        return dem;
    }

    /** Thực hiện dem cho khach cho thành phần hiện tại. */
    public int demChoKhach(TaiKhoan tk) {
        List<BaoGia> danhSach = baoGia.timKiem(tk, "", null);
        int dem = 0;
        for (BaoGia bg : danhSach) {
            if (bg.getTrangThai() != TrangThaiBaoGia.CHO_XAC_NHAN) {
                continue;
            }
            boolean moiNhat = true;
            for (BaoGia khac : danhSach) {
                if (khac.getMaBaoGiaGoc().equals(bg.getMaBaoGiaGoc()) && khac.getPhienBan() > bg.getPhienBan()) {
                    moiNhat = false;
                    break;
                }
            }
            if (moiNhat) {
                dem++;
            }
        }
        return dem;
    }

    /** Đọc gan day từ dữ liệu hiện tại. */
    public List<PhieuTiepNhan> layGanDay(TaiKhoan tk) {
        List<PhieuTiepNhan> danhSach = tiepNhan.timKiem(tk, "", null);
        for (int i = 0; i < danhSach.size() - 1; i++) {
            for (int j = i + 1; j < danhSach.size(); j++) {
                if (danhSach.get(i).getThoiGianNhan().isBefore(danhSach.get(j).getThoiGianNhan())) {
                    PhieuTiepNhan cu = danhSach.get(i);
                    danhSach.set(i, danhSach.get(j));
                    danhSach.set(j, cu);
                }
            }
        }
        List<PhieuTiepNhan> ketQua = new ArrayList<>();
        for (int i = 0; i < danhSach.size() && i < 3; i++) {
            ketQua.add(danhSach.get(i));
        }
        return ketQua;
    }

    /** Đọc hoan thanh từ dữ liệu hiện tại. */
    public List<PhieuSuaChua> layHoanThanh(TaiKhoan tk) {
        return suaChua.timKiem(tk, "", "Hoàn thành");
    }

    /** Thực hiện trang thai phieu cho thành phần hiện tại. */
    public String trangThaiPhieu(TaiKhoan tk, String maPhieu) {
        tiepNhan.timTheoMa(tk, maPhieu);
        BaoGia moiNhat = null;
        for (BaoGia bg : baoGia.timKiem(tk, "", null)) {
            if (bg.getBaoCao().getPhieu().getMaPhieu().equals(maPhieu)) {
                for (PhieuSuaChua lenh : suaChua.timKiem(tk, "", "Tất cả")) {
                    if (lenh.getMaBaoGia().equals(bg.getMaBaoGia())) {
                        if (lenh.getTrangThai() == TrangThaiSuaChua.HOAN_THANH) {
                            return banGiao.trangThai(tk, lenh.getMaPhieu()).getTenHienThi();
                        }
                        return lenh.getTrangThai().getTenHienThi();
                    }
                }
                if (moiNhat == null || bg.getPhienBan() > moiNhat.getPhienBan()) {
                    moiNhat = bg;
                }
            }
        }
        if (moiNhat != null) {
            return moiNhat.getTrangThai().getTenHienThi();
        }
        for (BaoCaoKiemTra bc : kiemTra.layBaoCaoChoTiepNhan(tk)) {
            if (bc.getPhieu().getMaPhieu().equals(maPhieu)) {
                return bc.getTrangThai() == TrangThaiKiemTra.HOAN_TAT ? "Đã kiểm tra" : "Đang kiểm tra";
            }
        }
        return "Chờ kiểm tra";
    }
}
