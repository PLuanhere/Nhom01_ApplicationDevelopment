package dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import entity.BienDongKho;
import entity.PhuTung;
import entity.TaiKhoan;
import entity.VaiTro;

/** Quản lý danh mục, tồn kho và lịch sử biến động dùng chung giữa các vai trò. */
public final class PhuTungDAO {
    private final List<PhuTung> danhMuc = new ArrayList<>();
    private final List<BienDongKho> nhatKy = new ArrayList<>();
    private final PhieuNhapKhoDAO nhapKhoDAO = new PhieuNhapKhoDAO(this);

    /** Khởi tạo danh mục phụ tùng và tồn đầu kỳ dùng chung. */
    public PhuTungDAO() {
        themBanDau("PT001", "Dầu 5W-30", "lít", 48, 12);
        themBanDau("PT005", "Lọc dầu động cơ", "cái", 6, 12);
        themBanDau("PT004", "Rotuyn lái", "cái", 5, 2);
    }

    /** Lấy nguồn phiếu nhập gắn với cùng danh mục và tồn kho. */
    public PhieuNhapKhoDAO getNhapKhoDAO() { return nhapKhoDAO; }

    /** Đọc danh sách phụ tùng sau khi kiểm tra quyền truy cập. */
    public List<PhuTung> layDanhSach(TaiKhoan tk) {
        kiemTraQuyen(tk);
        return danhMucNoiBo();
    }

    /** Tìm và lọc phụ tùng theo các điều kiện được truyền vào. */
    public List<PhuTung> timKiem(TaiKhoan tk, String tuKhoa, String tinhTrang, int hoatDong) {
        kiemTraQuyen(tk);
        String chu = boDau(tuKhoa == null ? "" : tuKhoa.trim());
        List<PhuTung> ketQua = new ArrayList<>();
        for (PhuTung pt : danhMuc) {
            String noiDung = pt.getMaPhuTung() + " " + pt.getTenPhuTung() + " " + pt.getLoaiPhuTung();
            if (boDau(noiDung).contains(chu) && (hoatDong == -1 || pt.isHoatDong() == (hoatDong == 1))
                && (tinhTrang == null || tinhTrang.equals(tinhTrangTon(tk, pt)))) { ketQua.add(new PhuTung(pt)); }
        }
        return ketQua;
    }

    /** Tìm phụ tùng theo mã; báo lỗi nếu không tồn tại. */
    public PhuTung timTheoMa(TaiKhoan tk, String ma) {
        kiemTraQuyen(tk);
        return new PhuTung(timNoiBo(ma));
    }

    /** Đọc tồn hiện tại của phụ tùng sau khi kiểm tra quyền Quản lý. */
    public double layTon(TaiKhoan tk, String ma) {
        kiemTraQuyen(tk);
        return tonNoiBo(ma);
    }

    /** Xác định phụ tùng hết hàng, sắp hết hay đủ tồn theo mức tối thiểu. */
    public String tinhTrangTon(TaiKhoan tk, PhuTung pt) {
        kiemTraQuyen(tk);
        double ton = tonNoiBo(pt.getMaPhuTung());
        if (ton == 0) { return "Hết hàng"; }
        return ton < pt.getMucToiThieu() ? "Sắp hết" : "Đủ tồn";
    }

    /** Kiểm tra quyền và dữ liệu trước khi thêm phụ tùng vào nguồn. */
    public void them(TaiKhoan tk, PhuTung pt) {
        kiemTraQuyen(tk);
        if (pt == null) { throw new IllegalArgumentException("Thiếu phụ tùng."); }
        for (PhuTung daCo : danhMuc) {
            if (daCo.getMaPhuTung().equalsIgnoreCase(pt.getMaPhuTung())) {
                throw new IllegalArgumentException("Mã phụ tùng đã tồn tại.");
            }
        }
        PhuTung moi = new PhuTung(pt);
        moi.setSoLuongTon(0);
        danhMuc.add(moi);
    }

    /** Kiểm tra dữ liệu rồi cập nhật phụ tùng và giữ nguyên số tồn. */
    public void capNhat(TaiKhoan tk, PhuTung banDau, PhuTung moi) {
        kiemTraQuyen(tk);
        if (banDau == null || moi == null) { throw new IllegalArgumentException("Thiếu phụ tùng."); }
        if (!banDau.getMaPhuTung().equals(moi.getMaPhuTung())) {
            throw new IllegalArgumentException("Không được đổi mã phụ tùng.");
        }
        PhuTung hienTai = timNoiBo(banDau.getMaPhuTung());
        if (!cungThongTin(hienTai, banDau)) {
            throw new IllegalArgumentException("Danh mục đã thay đổi. Hãy mở lại chi tiết.");
        }
        if (!banDau.getDonViTinh().equals(moi.getDonViTinh())) {
            throw new IllegalArgumentException("Đơn vị tính cố định sau khi tạo phụ tùng.");
        }
        PhuTung capNhat = new PhuTung(moi);
        capNhat.setSoLuongTon(hienTai.getSoLuongTon());
        for (int i = 0; i < danhMuc.size(); i++) {
            if (danhMuc.get(i).getMaPhuTung().equals(moi.getMaPhuTung())) {
                danhMuc.set(i, capNhat);
                return;
            }
        }
    }

    /** Đọc lịch sử thay đổi theo quyền truy cập và các điều kiện lọc. */
    public List<BienDongKho> layNhatKy(TaiKhoan tk, String tuKhoa, String loai,
        String maPhuTung, LocalDate tuNgay, LocalDate denNgay) {
        kiemTraQuyen(tk);
        if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) {
            throw new IllegalArgumentException("Từ ngày không được sau Đến ngày.");
        }
        String chu = boDau(tuKhoa == null ? "" : tuKhoa.trim());
        List<BienDongKho> ketQua = new ArrayList<>();
        for (int i = nhatKy.size() - 1; i >= 0; i--) {
            BienDongKho dong = nhatKy.get(i);
            LocalDate ngay = dong.getThoiDiem().toLocalDate();
            String noiDung = dong.getPhuTung().getMaPhuTung() + " " + dong.getPhuTung().getTenPhuTung()
                + " " + dong.getChungTu() + " " + dong.getLoai() + " " + dong.getLyDo();
            if ((loai == null || loai.equals(dong.getLoai()))
                && (maPhuTung == null || maPhuTung.equals(dong.getPhuTung().getMaPhuTung()))
                && (tuNgay == null || !ngay.isBefore(tuNgay)) && (denNgay == null || !ngay.isAfter(denNgay))
                && boDau(noiDung).contains(chu)) { ketQua.add(dong); }
        }
        return ketQua;
    }

    /** Kiểm tra số kiểm kê và dữ liệu đã đọc, cập nhật tồn rồi ghi nhật ký điều chỉnh. */
    public void dieuChinh(TaiKhoan tk, PhuTung pt, double tonDaDoc, double kiemKe,
        String lyDo, String dienGiai) {
        kiemTraQuyen(tk);
        if (pt == null) { throw new IllegalArgumentException("Thiếu phụ tùng."); }
        PhuTung hienTai = timNoiBo(pt.getMaPhuTung());
        if (!cungThongTin(hienTai, pt) || tonNoiBo(pt.getMaPhuTung()) != tonDaDoc) {
            throw new IllegalArgumentException("Danh mục hoặc tồn kho đã thay đổi. Hãy mở lại đối soát.");
        }
        if (!(kiemKe >= 0 && kiemKe < Long.MAX_VALUE / 1000.0)) {
            throw new IllegalArgumentException("Số lượng kiểm kê phải không âm.");
        }
        if (kiemKe > 0) { pt.kiemTraSoLuong(kiemKe); }
        if (lyDo == null || lyDo.isBlank()) { throw new IllegalArgumentException("Nhập lý do điều chỉnh."); }
        if (kiemKe == tonDaDoc) {
            throw new IllegalArgumentException("Không có chênh lệch, không cần điều chỉnh tồn.");
        }
        BienDongKho dong = new BienDongKho(pt, LocalDateTime.now(), "Điều chỉnh", tonDaDoc, kiemKe,
            dienGiai == null ? "" : dienGiai.trim(), tk.getTenDangNhap(), lyDo.trim());
        hienTai.setSoLuongTon(kiemKe);
        nhatKy.add(dong);
    }

    /** Tìm phụ tùng trong nguồn nội bộ theo mã; báo lỗi nếu không có. */
    PhuTung timNoiBo(String ma) {
        for (PhuTung pt : danhMuc) {
            if (pt.getMaPhuTung().equals(ma)) { return pt; }
        }
        throw new IllegalArgumentException("Không tìm thấy phụ tùng.");
    }

    /** Đọc số lượng tồn từ phụ tùng trong nguồn nội bộ. */
    double tonNoiBo(String ma) { timNoiBo(ma); return timNoiBo(ma).getSoLuongTon(); }

    /** Sao chép danh mục phụ tùng để các module khác đọc chung dữ liệu kho. */
    List<PhuTung> danhMucNoiBo() {
        List<PhuTung> ketQua = new ArrayList<>();
        for (PhuTung pt : danhMuc) { ketQua.add(new PhuTung(pt)); }
        return ketQua;
    }

    /** Lấy bản sao các phụ tùng đang hoạt động để chọn cho nghiệp vụ mới. */
    List<PhuTung> danhMucDangHoatDong() {
        List<PhuTung> ketQua = new ArrayList<>();
        for (PhuTung pt : danhMuc) {
            if (pt.isHoatDong()) { ketQua.add(new PhuTung(pt)); }
        }
        return ketQua;
    }

    /** Kiểm tra đủ các dòng trước khi cập nhật tồn và ghi lịch sử nhập hoặc xuất. */
    void ghiBienDong(String[] ma, double[] soLuong, boolean laNhap,
        String chungTu, TaiKhoan tk, String lyDo, LocalDateTime thoiDiem) {
        if (ma.length != soLuong.length) { throw new IllegalArgumentException("Thiếu số lượng dòng kho."); }
        List<BienDongKho> cacDong = new ArrayList<>();
        for (int i = 0; i < ma.length; i++) {
            for (int j = 0; j < i; j++) {
                if (ma[i].equals(ma[j])) { throw new IllegalArgumentException("Cần gộp số lượng cùng mã trước khi ghi."); }
            }
            PhuTung pt = timNoiBo(ma[i]);
            pt.kiemTraSoLuong(soLuong[i]);
            double cu = pt.getSoLuongTon();
            double moi;
            if (laNhap) { moi = cu + soLuong[i]; }
            else { moi = cu - soLuong[i]; }
            if (!(moi > -Long.MAX_VALUE / 1000.0 && moi < Long.MAX_VALUE / 1000.0)) {
                throw new IllegalArgumentException("Lượng xuất vượt tồn hoặc lượng nhập vượt giới hạn: " + ma[i]);
            }
            moi = Math.round(moi * 1000) / 1000.0;
            if (moi < 0) { throw new IllegalArgumentException("Lượng xuất vượt tồn: " + ma[i]); }
            cacDong.add(new BienDongKho(pt, thoiDiem, laNhap ? "Nhập" : "Xuất", cu, moi,
                chungTu, tk.getTenDangNhap(), lyDo));
        }

        for (BienDongKho dong : cacDong) {
            timNoiBo(dong.getPhuTung().getMaPhuTung()).setSoLuongTon(dong.getTonSau());
            nhatKy.add(dong);
        }
    }

    /** Thêm phụ tùng và tồn đầu kỳ vào dữ liệu tạo sẵn. */
    private void themBanDau(String ma, String ten, String donVi, double ton, double toiThieu) {
        PhuTung pt = new PhuTung(ma, ten, donVi, toiThieu);
        pt.setSoLuongTon(ton);
        danhMuc.add(pt);
    }

    /** Kiểm tra tài khoản, phiên đăng nhập và quyền Quản lý trước thao tác DAO. */
    private void kiemTraQuyen(TaiKhoan tk) {
        if (tk == null) { throw new SecurityException("Cần đăng nhập."); }
        tk.kiemTraPhien();
        if (tk.getVaiTro() != VaiTro.QUAN_LY) { throw new SecurityException("Chức năng dành cho Quản lý."); }
    }

    /** Đổi chữ tiếng Việt thành chữ thường không dấu để tìm và sắp xếp. */
    private static String boDau(String chu) {
        String thuong = chu.toLowerCase();
        String[] cacNhom = {"àáảãạăằắẳẵặâầấẩẫậ", "èéẻẽẹêềếểễệ", "ìíỉĩị",
            "òóỏõọôồốổỗộơờớởỡợ", "ùúủũụưừứửữự", "ỳýỷỹỵ", "đ"};
        String thayThe = "aeiouyd";
        String ketQua = "";
        for (int i = 0; i < thuong.length(); i++) {
            char c = thuong.charAt(i);
            if (c >= '\u0300' && c <= '\u036F') { continue; }
            for (int j = 0; j < cacNhom.length; j++) {
                if (cacNhom[j].indexOf(c) >= 0) { c = thayThe.charAt(j); break; }
            }
            ketQua = ketQua + c;
        }
        return ketQua;
    }

    /** Tìm và lọc phụ tùng theo các điều kiện rồi sắp xếp danh sách kết quả. */
    public List<PhuTung> timKiem(TaiKhoan tk, String tuKhoa, String tinhTrang, int hoatDong, int sapXep) {
        List<PhuTung> ketQua = timKiem(tk, tuKhoa, tinhTrang, hoatDong);
        for (int i = 0; i < ketQua.size() - 1; i++) {
            for (int j = i + 1; j < ketQua.size(); j++) {
                PhuTung a = ketQua.get(i);
                PhuTung b = ketQua.get(j);
                int soSanh;
                if (sapXep == 0) { soSanh = a.getMaPhuTung().compareToIgnoreCase(b.getMaPhuTung()); }
                else { soSanh = boDau(a.getTenPhuTung()).compareTo(boDau(b.getTenPhuTung())); }
                if (sapXep == 2) { soSanh = -soSanh; }
                if (sapXep == 3 || sapXep == 4) {
                    double tonA = tonNoiBo(a.getMaPhuTung());
                    double tonB = tonNoiBo(b.getMaPhuTung());
                    soSanh = 0;
                    if (tonA > tonB) { soSanh = 1; }
                    if (tonA < tonB) { soSanh = -1; }
                    if (sapXep == 4) { soSanh = -soSanh; }
                }
                if (soSanh > 0) { ketQua.set(i, b); ketQua.set(j, a); }
            }
        }
        return ketQua;
    }

    /** So sánh dữ liệu danh mục để tránh ghi đè bản ghi đã được thay đổi. */
    boolean cungThongTin(PhuTung a, PhuTung b) {
        return a.getMaPhuTung().equals(b.getMaPhuTung()) && a.getTenPhuTung().equals(b.getTenPhuTung())
            && a.getLoaiPhuTung().equals(b.getLoaiPhuTung()) && a.getDonViTinh().equals(b.getDonViTinh())
            && a.getGiaNhap() == b.getGiaNhap() && a.getGiaBan() == b.getGiaBan()
            && a.getMucToiThieu() == b.getMucToiThieu() && a.isHoatDong() == b.isHoatDong();
    }
}
