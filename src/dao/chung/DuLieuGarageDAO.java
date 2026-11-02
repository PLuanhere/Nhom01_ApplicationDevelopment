package dao.chung;

import dao.bangiao.BanGiaoXeDAO;
import dao.baocao.BaoCaoDAO;
import dao.baocao.TrangChuTiepNhanDAO;
import dao.baogia.BaoGiaDAO;
import dao.dichvu.DichVuDAO;
import dao.hoadon.HoaDonDAO;
import dao.khachhang.KhachHangDAO;
import dao.khachhang.XeDAO;
import dao.kiemtra.BaoCaoKiemTraDAO;
import dao.nhansu.NhanVienDAO;
import dao.phutung.PhuTungDAO;
import dao.phutung.PhuTungSuDungDAO;
import dao.suachua.PhieuSuaChuaDAO;
import dao.tiepnhan.PhieuTiepNhanDAO;

/** Tạo một nguồn dữ liệu dùng chung trong suốt lần chạy ứng dụng. */
public final class DuLieuGarageDAO {
    private static long soThaoTac = 0;
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final XeDAO xeDAO = new XeDAO(khachHangDAO);
    private final PhieuTiepNhanDAO tiepNhanDAO = new PhieuTiepNhanDAO(khachHangDAO, xeDAO, nhanVienDAO);
    private final BaoCaoKiemTraDAO kiemTraDAO = new BaoCaoKiemTraDAO(tiepNhanDAO);
    private final PhuTungSuDungDAO phuTungDAO = new PhuTungSuDungDAO(false);
    private final DichVuDAO dichVuDAO = new DichVuDAO();
    private final BaoGiaDAO baoGiaDAO = new BaoGiaDAO(tiepNhanDAO, kiemTraDAO, phuTungDAO, dichVuDAO);
    private final PhieuSuaChuaDAO suaChuaDAO = new PhieuSuaChuaDAO(baoGiaDAO, nhanVienDAO);
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO(suaChuaDAO);
    private final BanGiaoXeDAO banGiaoDAO = new BanGiaoXeDAO(suaChuaDAO, hoaDonDAO);
    private final TrangChuTiepNhanDAO trangChuDAO = new TrangChuTiepNhanDAO(tiepNhanDAO, kiemTraDAO, baoGiaDAO,
            suaChuaDAO, phuTungDAO, banGiaoDAO);

    private final BaoCaoDAO baoCaoThongKeDAO = new BaoCaoDAO(this);

    /** Nối nguồn lệnh với kho để các vai trò cùng theo dõi lượng thực dùng. */
    public DuLieuGarageDAO() {
        phuTungDAO.noiNguonLenh(suaChuaDAO);
        suaChuaDAO.noiNguonThucDung(phuTungDAO);
        dichVuDAO.noiNguonBaoGia(baoGiaDAO);
    }

    /** Trả về nguồn KhachHangDAO dùng chung. */
    public KhachHangDAO getKhachHangDAO() {
        return khachHangDAO;
    }

    /** Trả về nguồn XeDAO dùng chung. */
    public XeDAO getXeDAO() {
        return xeDAO;
    }

    /** Trả về nguồn TiepNhanDAO dùng chung. */
    public PhieuTiepNhanDAO getTiepNhanDAO() {
        return tiepNhanDAO;
    }

    /** Trả về nguồn KiemTraDAO dùng chung. */
    public BaoCaoKiemTraDAO getKiemTraDAO() {
        return kiemTraDAO;
    }

    /** Trả về nguồn BaoGiaDAO dùng chung. */
    public BaoGiaDAO getBaoGiaDAO() {
        return baoGiaDAO;
    }

    /** Trả về nguồn SuaChuaDAO dùng chung. */
    public PhieuSuaChuaDAO getSuaChuaDAO() {
        return suaChuaDAO;
    }

    /** Trả về nguồn PhuTungDAO dùng chung. */
    public PhuTungSuDungDAO getPhuTungDAO() {
        return phuTungDAO;
    }

    /** Trả về nguồn HoaDonDAO dùng chung. */
    public HoaDonDAO getHoaDonDAO() {
        return hoaDonDAO;
    }

    /** Trả về nguồn BanGiaoDAO dùng chung. */
    public BanGiaoXeDAO getBanGiaoDAO() {
        return banGiaoDAO;
    }

    /** Trả về nguồn TrangChuDAO dùng chung. */
    public TrangChuTiepNhanDAO getTrangChuDAO() {
        return trangChuDAO;
    }

    /** Trả về danh mục dịch vụ và liên kết báo giá dùng chung. */
    public DichVuDAO getDichVuDAO() {
        return dichVuDAO;
    }

    /** Trả về nguồn danh mục và tồn kho của toàn ứng dụng. */
    public PhuTungDAO getKhoDAO() {
        return phuTungDAO.getKhoDAO();
    }

    /** Trả nguồn báo cáo và bản lưu dùng chung qua các lần đăng nhập. */
    public BaoCaoDAO getBaoCaoDAO() {
        return baoCaoThongKeDAO;
    }

    /** Sinh mã thao tác bằng bộ đếm trong lần chạy; giữ mã khi bấm xác nhận lặp. */
    public static String taoMaThaoTac() {
        if (soThaoTac == Long.MAX_VALUE) {
            throw new IllegalStateException("Đã vượt số thao tác cho phép trong lần chạy.");
        }
        soThaoTac++;
        return "THAO-TAC-" + soThaoTac;
    }
}
