package entity.khachhang;

import java.time.Year;

/** Hồ sơ xe bất biến; quan hệ chủ xe và tính duy nhất được kiểm tra ở DAO. */
public final class Xe {
    private final String maXe, bienSo, maKH, hangXe, model, mauXe, soKhung, soMay;
    private final int namSX;

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public Xe(String maXe, String bienSo, String maKH, String hangXe, String model, int namSX, String mauXe,
            String soKhung, String soMay) {
        this.maXe = batBuoc(maXe, "Mã xe");
        this.bienSo = batBuoc(bienSo, "Biển số").toUpperCase();
        chuanHoaBienSo(this.bienSo);
        this.maKH = batBuoc(maKH, "Chủ xe");
        this.hangXe = batBuoc(hangXe, "Hãng xe");
        this.model = batBuoc(model, "Dòng xe");
        if (namSX <= 0 || namSX > Year.now().getValue()) {
            throw new IllegalArgumentException("Năm sản xuất phải lớn hơn 0 và không vượt năm hiện tại.");
        }
        this.namSX = namSX;
        this.mauXe = tuyChon(mauXe);
        this.soKhung = tuyChon(soKhung);
        this.soMay = tuyChon(soMay);
    }

    /** Khóa so sánh chung cho tìm kiếm và chống trùng, không làm mất dạng hiển thị. */
    public static String chuanHoaBienSo(String bienSo) {
        String khoa = khoaBienSo(batBuoc(bienSo, "Biển số"));
        if (khoa.isEmpty()) {
            throw new IllegalArgumentException("Biển số không được rỗng.");
        }
        for (int i = 0; i < khoa.length(); i++) {
            char c = khoa.charAt(i);
            if (!((c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9'))) {
                throw new IllegalArgumentException("Biển số chỉ gồm chữ, số, khoảng trắng, dấu chấm hoặc gạch nối.");
            }
        }
        return khoa;
    }

    /** Trả về ma xe. */
    public String getMaXe() {
        return maXe;
    }

    /** Trả về bien so. */
    public String getBienSo() {
        return bienSo;
    }

    /** Trả về ma kh. */
    public String getMaKH() {
        return maKH;
    }

    /** Trả về hang xe. */
    public String getHangXe() {
        return hangXe;
    }

    /** Trả về model. */
    public String getModel() {
        return model;
    }

    /** Trả về nam sx. */
    public int getNamSX() {
        return namSX;
    }

    /** Trả về mau xe. */
    public String getMauXe() {
        return mauXe;
    }

    /** Trả về so khung. */
    public String getSoKhung() {
        return soKhung;
    }

    /** Trả về so may. */
    public String getSoMay() {
        return soMay;
    }

    /** Thực hiện bat buoc cho thành phần hiện tại. */
    private static String batBuoc(String chu, String ten) {
        if (chu == null) {
            throw new NullPointerException(ten + " không được null.");
        }
        String giaTri = chu.trim();
        if (giaTri.isEmpty()) {
            throw new IllegalArgumentException(ten + " không được rỗng.");
        }
        return giaTri;
    }

    /** Thực hiện tuy chon cho thành phần hiện tại. */
    private static String tuyChon(String chu) {
        return chu == null ? "" : chu.trim();
    }

    /** Bỏ khoảng trắng và dấu phân cách để tìm theo toàn bộ hoặc một phần biển số. */
    public static String khoaBienSo(String chu) {
        String khoa = chu == null ? "" : chu.toUpperCase();
        String ketQua = "";
        for (int i = 0; i < khoa.length(); i++) {
            char c = khoa.charAt(i);
            if (c != '.' && c != '-' && !Character.isWhitespace(c)) {
                ketQua += c;
            }
        }
        return ketQua;
    }
}
