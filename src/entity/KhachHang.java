package entity;

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

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public KhachHang(KhachHang khac) {
        this(khac.maKH, khac.hoTen, khac.soDienThoai, khac.diaChi, khac.email, khac.ghiChu);
    }

    /** Trả về ma kh. */
    public String getMaKH() {
        return maKH;
    }

    /** Trả về ho ten. */
    public String getHoTen() {
        return hoTen;
    }

    /** Trả về so dien thoai. */
    public String getSoDienThoai() {
        return soDienThoai;
    }

    /** Trả về dia chi. */
    public String getDiaChi() {
        return diaChi;
    }

    /** Trả về email. */
    public String getEmail() {
        return email;
    }

    /** Trả về ghi chu. */
    public String getGhiChu() {
        return ghiChu;
    }

    /** Kiểm tra và cập nhật ho ten. */
    public void setHoTen(String giaTri) {
        hoTen = batBuoc(giaTri, "Họ tên");
    }

    /** Kiểm tra và cập nhật dia chi. */
    public void setDiaChi(String giaTri) {
        diaChi = tuyChon(giaTri);
    }

    /** Kiểm tra và cập nhật ghi chu. */
    public void setGhiChu(String giaTri) {
        ghiChu = tuyChon(giaTri);
    }

    /** Kiểm tra và cập nhật so dien thoai. */
    public void setSoDienThoai(String giaTri) {
        String so = batBuoc(giaTri, "Số điện thoại");
        boolean hopLe = so.length() == 10 && so.charAt(0) == '0';
        for (int i = 0; i < so.length(); i++) {
            if (so.charAt(i) < '0' || so.charAt(i) > '9') {
                hopLe = false;
            }
        }
        if (!hopLe) {
            throw new IllegalArgumentException("Số điện thoại phải gồm đúng 10 chữ số và bắt đầu bằng 0.");
        }
        soDienThoai = so;
    }

    /** Kiểm tra và cập nhật email. */
    public void setEmail(String giaTri) {
        String mail = tuyChon(giaTri);
        if (!mail.isEmpty()) {
            int aCong = mail.indexOf('@');
            int dauCham = mail.lastIndexOf('.');
            boolean hopLe = aCong > 0 && aCong == mail.lastIndexOf('@') && dauCham > aCong + 1
                    && dauCham < mail.length() - 1;
            for (int i = 0; i < mail.length(); i++) {
                if (Character.isWhitespace(mail.charAt(i))) {
                    hopLe = false;
                }
            }
            if (!hopLe) {
                throw new IllegalArgumentException("Email không đúng định dạng.");
            }
        }
        email = mail;
    }

    /** Thực hiện bat buoc cho thành phần hiện tại. */
    private static String batBuoc(String giaTri, String ten) {
        if (giaTri == null) {
            throw new NullPointerException(ten + " không được null.");
        }
        String chu = giaTri.trim();
        if (chu.isEmpty()) {
            throw new IllegalArgumentException(ten + " không được rỗng.");
        }
        return chu;
    }

    /** Thực hiện tuy chon cho thành phần hiện tại. */
    private static String tuyChon(String giaTri) {
        return giaTri == null ? "" : giaTri.trim();
    }
}
