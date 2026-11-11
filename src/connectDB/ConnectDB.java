package connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Mở một kết nối SQL riêng cho từng luồng làm việc. */
public final class ConnectDB {
    private static final String TEN_BIEN_URL = "GARAGE_DB_URL";
    private static final String TEN_BIEN_USER = "GARAGE_DB_USER";
    private static final String TEN_BIEN_PASSWORD = "GARAGE_DB_PASSWORD";

    private static final String URL_MAC_DINH =
            "jdbc:sqlserver://localhost:1433;databaseName=QuanLiGarage;"
                    + "encrypt=true;trustServerCertificate=true;";
    private static final String USER_MAC_DINH = "sa";
    private static final String PASSWORD_MAC_DINH = "123456";

    private static final ConnectDB INSTANCE = new ConnectDB();

    private final ThreadLocal<Connection> ketNoiTheoLuong = new ThreadLocal<>();
    private final List<Connection> cacKetNoiDaMo = new ArrayList<>();

    private ConnectDB() {
    }

    /** Trả về đối tượng kết nối dùng chung của ứng dụng. */
    public static ConnectDB getInstance() {
        return INSTANCE;
    }

    /** Mở kết nối cho luồng hiện tại nếu chưa có. */
    public void connect() throws SQLException {
        Connection ketNoi = ketNoiTheoLuong.get();

        if (ketNoi != null && !ketNoi.isClosed()) {
            return;
        }

        String url = docBienMoiTruong(TEN_BIEN_URL, URL_MAC_DINH);
        String user = docBienMoiTruong(TEN_BIEN_USER, USER_MAC_DINH);
        String password = docBienMoiTruong(TEN_BIEN_PASSWORD, PASSWORD_MAC_DINH);

        ketNoi = DriverManager.getConnection(url, user, password);
        ketNoiTheoLuong.set(ketNoi);

        synchronized (cacKetNoiDaMo) {
            cacKetNoiDaMo.add(ketNoi);
        }
    }

    /** Trả về kết nối của đúng luồng đang thực hiện nghiệp vụ. */
    public Connection getConnection() throws SQLException {
        connect();
        return ketNoiTheoLuong.get();
    }

    /** Dọn kết nối của tác vụ nền đã xong, giữ kết nối các luồng khác. */
    public void dongKetNoiHienTai() {
        Connection ketNoi = ketNoiTheoLuong.get();
        ketNoiTheoLuong.remove();
        if (ketNoi == null) {
            return;
        }
        synchronized (cacKetNoiDaMo) {
            cacKetNoiDaMo.remove(ketNoi);
        }
        try {
            ketNoi.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /** Đóng tất cả kết nối khi ứng dụng kết thúc. */
    public void disconnect() {
        synchronized (cacKetNoiDaMo) {
            for (Connection ketNoi : cacKetNoiDaMo) {
                try {
                    if (ketNoi != null && !ketNoi.isClosed()) {
                        ketNoi.close();
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

            cacKetNoiDaMo.clear();
        }

        ketNoiTheoLuong.remove();
    }

    /** Đọc cấu hình đơn giản từ biến môi trường, không lưu mật khẩu trong source. */
    private static String docBienMoiTruong(String ten, String giaTriMacDinh) {
        String giaTri = System.getenv(ten);

        if (giaTri == null || giaTri.trim().isEmpty()) {
            return giaTriMacDinh;
        }

        return giaTri.trim();
    }
}
