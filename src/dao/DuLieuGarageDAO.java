package dao;

import entity.KyThuatVienPhanCong;
import java.util.List;

/** Nguồn bộ nhớ của một lần chạy app, dùng chung giữa các vai trò nhưng không giữ phiên đăng nhập. */
public final class DuLieuGarageDAO {
    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final XeDAO xeDAO = new XeDAO(khachHangDAO);
    private final PhieuTiepNhanDAO tiepNhanDAO = new PhieuTiepNhanDAO(khachHangDAO, xeDAO, this::layLienKetKyThuat);
    private final BaoCaoKiemTraDAO kiemTraDAO = new BaoCaoKiemTraDAO(tiepNhanDAO);
    private final PhuTungSuDungDAO phuTungDAO = new PhuTungSuDungDAO(false);
    private final BaoGiaDAO baoGiaDAO = new BaoGiaDAO(tiepNhanDAO, this::baoCaoHoanTat, phuTungDAO);
    private final PhieuSuaChuaDAO suaChuaDAO = new PhieuSuaChuaDAO(baoGiaDAO, this::layLienKetKyThuat);
    private final TrangChuTiepNhanDAO trangChuDAO = new TrangChuTiepNhanDAO(tiepNhanDAO, kiemTraDAO, baoGiaDAO, suaChuaDAO, phuTungDAO);

    public DuLieuGarageDAO() { phuTungDAO.noiNguonLenh(suaChuaDAO); }

    /** Chỉ đọc nội bộ giữa các DAO; quyền của UI được kiểm tra tại từng thao tác nghiệp vụ. */
    private List<KyThuatVienPhanCong> layLienKetKyThuat() { return nhanVienDAO.layLienKetKyThuat(taiKhoanDAO); }
    private List<entity.BaoCaoKiemTra> baoCaoHoanTat() { return kiemTraDAO.layHoanTatNoiBo(); }
    public KhachHangDAO getKhachHangDAO() { return khachHangDAO; }
    public XeDAO getXeDAO() { return xeDAO; }
    public PhieuTiepNhanDAO getTiepNhanDAO() { return tiepNhanDAO; }
    public BaoCaoKiemTraDAO getKiemTraDAO() { return kiemTraDAO; }
    public BaoGiaDAO getBaoGiaDAO() { return baoGiaDAO; }
    public PhieuSuaChuaDAO getSuaChuaDAO() { return suaChuaDAO; }
    public PhuTungSuDungDAO getPhuTungDAO() { return phuTungDAO; }
    public TrangChuTiepNhanDAO getTrangChuDAO() { return trangChuDAO; }
}
