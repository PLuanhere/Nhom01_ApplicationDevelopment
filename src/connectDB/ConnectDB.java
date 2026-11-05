package connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Mở và đóng kết nối dùng chung tới SQL Server. */
public final class ConnectDB {
    private static final String URL =
            "jdbc:sqlserver://localhost:1433;databaseName=QuanLiGarage;encrypt=false;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASSWORD = "123456";

    private static final ConnectDB INSTANCE = new ConnectDB();
    private Connection connection;

    private ConnectDB() {
    }

    /** Trả về đối tượng kết nối dùng chung của ứng dụng. */
    public static ConnectDB getInstance() {
        return INSTANCE;
    }

    /** Mở kết nối nếu kết nối hiện tại chưa tồn tại hoặc đã đóng. */
    public void connect() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
    }

    /** Trả về kết nối đang mở để DAO thực hiện câu lệnh SQL. */
    public Connection getConnection() throws SQLException {
        connect();
        return connection;
    }

    /** Đóng kết nối hiện tại khi ứng dụng kết thúc. */
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
