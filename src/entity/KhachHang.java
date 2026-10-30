package entity;

import java.util.Objects;

/** Hồ sơ khách hàng; không chứa danh sách xe hay chứng từ sao chép từ phân hệ khác. */
public final class KhachHang {
    private final String maKH;
    private String hoTen;
    private String soDienThoai;
    private String diaChi;
    private String email;
    private String ghiChu;

    /** Tạo hồ sơ đã hợp lệ; mã duy nhất được kiểm tra tại DAO. */
    public KhachHang(String maKH, String hoTen, String soDienThoai, String diaChi, String email, String ghiChu) {
        this.maKH = batBuoc(maKH, "Mã khách hàng");
        setHoTen(hoTen);
        setSoDienThoai(soDienThoai);
        setDiaChi(diaChi);
        setEmail(email);
        setGhiChu(ghiChu);
    }

    public KhachHang(KhachHang khac) {
        this(khac.maKH, khac.hoTen, khac.soDienThoai, khac.diaChi, khac.email, khac.ghiChu);
    }

    public String getMaKH() { return maKH; }
    public String getHoTen() { return hoTen; }
    public String getSoDienThoai() { return soDienThoai; }
    public String getDiaChi() { return diaChi; }
    public String getEmail() { return email; }
    public String getGhiChu() { return ghiChu; }
    public void setHoTen(String giaTri) { hoTen = batBuoc(giaTri, "Họ tên"); }
    public void setDiaChi(String giaTri) { diaChi = tuyChon(giaTri); }
    public void setGhiChu(String giaTri) { ghiChu = tuyChon(giaTri); }

    public void setSoDienThoai(String giaTri) {
        String so = batBuoc(giaTri, "Số điện thoại");
        if (!so.matches("0[0-9]{9}")) {
            throw new IllegalArgumentException("Số điện thoại phải gồm đúng 10 chữ số và bắt đầu bằng 0.");
        }
        soDienThoai = so;
    }

    public void setEmail(String giaTri) {
        String mail = tuyChon(giaTri);
        if (!mail.isEmpty() && !mail.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new IllegalArgumentException("Email không đúng định dạng.");
        }
        email = mail;
    }

    private static String batBuoc(String giaTri, String ten) {
        String chu = Objects.requireNonNull(giaTri, ten + " không được null.").strip();
        if (chu.isEmpty()) throw new IllegalArgumentException(ten + " không được rỗng.");
        return chu;
    }

    private static String tuyChon(String giaTri) { return giaTri == null ? "" : giaTri.strip(); }
}
