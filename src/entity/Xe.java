package entity;

import java.time.Year;
import java.util.Locale;
import java.util.Objects;

/** Hồ sơ xe bất biến; quan hệ chủ xe và tính duy nhất được kiểm tra ở DAO. */
public final class Xe {
    private final String maXe, bienSo, maKH, hangXe, model, mauXe, soKhung, soMay;
    private final int namSX;

    public Xe(String maXe, String bienSo, String maKH, String hangXe, String model,
            int namSX, String mauXe, String soKhung, String soMay) {
        this.maXe = batBuoc(maXe, "Mã xe");
        this.bienSo = batBuoc(bienSo, "Biển số").toUpperCase(Locale.ROOT);
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
        String khoa = batBuoc(bienSo, "Biển số").toUpperCase(Locale.ROOT).replaceAll("[\\s.\\-]", "");
        if (!khoa.matches("[A-Z0-9]+")) {
            throw new IllegalArgumentException("Biển số chỉ gồm chữ, số, khoảng trắng, dấu chấm hoặc gạch nối.");
        }
        return khoa;
    }

    public String getMaXe() { return maXe; }
    public String getBienSo() { return bienSo; }
    public String getMaKH() { return maKH; }
    public String getHangXe() { return hangXe; }
    public String getModel() { return model; }
    public int getNamSX() { return namSX; }
    public String getMauXe() { return mauXe; }
    public String getSoKhung() { return soKhung; }
    public String getSoMay() { return soMay; }

    private static String batBuoc(String chu, String ten) {
        String giaTri = Objects.requireNonNull(chu, ten + " không được null.").strip();
        if (giaTri.isEmpty()) throw new IllegalArgumentException(ten + " không được rỗng.");
        return giaTri;
    }
    private static String tuyChon(String chu) { return chu == null ? "" : chu.strip(); }
}
