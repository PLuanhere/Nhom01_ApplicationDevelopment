# Hệ thống quản lý dịch vụ sửa chữa Garage Ô tô Đức Dũng

Ứng dụng desktop nội bộ giúp Garage Ô tô Đức Dũng quản lý hồ sơ khách hàng và xe, tiếp nhận, kiểm tra, báo giá, sửa chữa, phụ tùng, thanh toán và bàn giao xe. Dự án do **Nhóm 01** thực hiện trong môn **Phát triển ứng dụng**, Khoa Công nghệ thông tin, Trường Đại học Công nghiệp TP.HCM, học kỳ 1 năm học 2026–2027.

**Trạng thái hiện tại:** đã có khung Swing, đăng nhập/đăng xuất bằng bốn tài khoản tạo sẵn, menu Hệ thống và tổng quan Quản lý theo Page 3. Biểu đồ doanh thu hỗ trợ hover và mở chi tiết từng ngày. Tổng quan đọc dữ liệu tạo sẵn trong DAO, chưa kết nối database. Các chức năng trong bảng phạm vi dưới đây không đồng nghĩa đã hoàn thành.

## 1. Thành viên thực hiện

| Họ và tên | MSSV |
| --- | --- |
| Phan Hoàng Luân | 24675951 |
| Nguyễn Trung Hậu | 24678601 |
| Chung Ngô Minh Hoàng | 24654011 |
| Nguyễn Minh Vũ | 24635361 |

## 2. Phạm vi và quy trình nghiệp vụ

Hệ thống phục vụ bốn vai trò nhân viên. Khách hàng là đối tượng được quản lý, không đăng nhập ứng dụng.

| Vai trò | Chức năng thuộc phạm vi xây dựng |
| --- | --- |
| Quản lý | Quản lý nhân viên, tài khoản và phân quyền; dịch vụ và bảng giá; phụ tùng, nhập/xuất/tồn kho; xem báo cáo doanh thu, tiếp nhận, sửa chữa và hiệu suất kỹ thuật viên; phê duyệt ngoại lệ theo nghiệp vụ. |
| Nhân viên tiếp nhận | Tra cứu hồ sơ khách hàng, xe và lịch sử sửa chữa; lập phiếu tiếp nhận; lập, điều chỉnh báo giá và ghi nhận phản hồi khách hàng; xác nhận và tra cứu bàn giao xe. |
| Nhân viên kỹ thuật | Xem công việc được phân công; ghi nhận kết quả kiểm tra, phụ tùng thực dùng, tiến độ sửa chữa và báo cáo hoàn thành. |
| Thu ngân | Tra cứu phiếu sửa chữa đã hoàn thành; đối chiếu chi phí, lập hóa đơn, xác nhận thanh toán bằng tiền mặt hoặc chuyển khoản/QR; tra cứu và in lại hóa đơn. |

Luồng xử lý chính:

**Tiếp nhận xe → Kiểm tra và chẩn đoán → Báo giá và xác nhận của khách hàng → Sửa chữa → Thanh toán → Bàn giao xe.**

Các nguyên tắc nghiệp vụ cần giữ khi triển khai:

- Mỗi lần tiếp nhận có phiếu riêng. Tra cứu khách hàng và xe trước khi tạo hồ sơ để tránh trùng dữ liệu.
- Chỉ sửa chữa các hạng mục khách hàng đã đồng ý. Phát sinh phải được cập nhật và xác nhận trước khi thực hiện.
- Chỉ trừ kho khi xác nhận phụ tùng đã sử dụng thực tế; không trừ lúc lập báo giá, không xuất vượt tồn hoặc trừ lặp.
- Chỉ lập hóa đơn cho phiếu sửa chữa đã hoàn thành và chưa có hóa đơn. Xác nhận thu đủ tiền mới lưu hóa đơn đã thanh toán và cập nhật trạng thái phiếu trong cùng transaction.
- Với chuyển khoản/QR, thu ngân phải đối chiếu tiền thực nhận trước khi xác nhận thanh toán.
- Nhân viên tiếp nhận xác nhận bàn giao khi xe đã hoàn thành và thanh toán đủ, hoặc có phê duyệt của quản lý được lưu lại.
- Doanh thu được tính từ hóa đơn đã thanh toán theo thời điểm thanh toán. Thay đổi giá danh mục không làm thay đổi chứng từ cũ.

Đặt lịch trực tuyến, nhắc bảo dưỡng tự động và phân hệ bảo hành riêng là hướng mở rộng, chỉ triển khai khi nhóm thống nhất đưa vào phạm vi.

## 3. Những phần đã triển khai

Phần Luân đã triển khai gồm **layout chung, đăng nhập và tổng quan Quản lý**, cùng các màn thành viên khác đã tích hợp.

| Thành phần | Hiện trạng |
| --- | --- |
| Khởi chạy | `app.App` áp dụng theme và mở `LoginFrame` trên Event Dispatch Thread (EDT). Đăng nhập thành công mới mở `MainFrame`. |
| Header | Thanh điều khiển cửa sổ 32 px; header Page 3 cao 66 px nằm bên phải sidebar, nền trắng, menu và danh tính hiện tại. Hệ thống có Đăng xuất/Thoát. Tìm nhanh/thông báo là vùng giữ chỗ, chưa có chức năng. |
| Sidebar | Rộng 216 px, nền tối, logo/icon gốc từ Figma; vai trò ở trên và danh tính ở dưới. Quản lý có 7 mục theo Page 3. Các panel điều hướng đã tích hợp vẫn giữ listener riêng. |
| Nội dung | Nền #F4F6F9, lề ngang 26 px. Quản lý mở tổng quan điều hành; giữ các màn thành viên khác đã tích hợp. |
| Tổng quan Quản lý | Bốn chỉ số, nhắc việc, lệnh đến hạn trong ngày và doanh thu bảy ngày. Hover cột xem tiền/số hóa đơn; nhấn cột mở dialog chỉ đọc, có tổng tiền mặt/chuyển khoản và hóa đơn đúng ngày. |
| Thanh trạng thái | Footer hiển thị tên garage và thông báo từ API setStatus; danh tính hiển thị ở header/sidebar. |
| Đăng nhập | Cửa sổ ban đầu 1280×800, tối thiểu 960×600; thẻ 488×573 ở giữa ảnh garage Page 3, có cuộn khi cửa sổ thấp. Giữ kiểm tra nhập, hiện/ẩn mật khẩu, Enter và SwingWorker. |
| Tài khoản và vai trò | Bốn tài khoản có sẵn trong `TaiKhoanDAO`, mỗi tài khoản một vai trò cố định. Danh tính bất biến, không chứa mật khẩu, được truyền cho khung chính sau xác thực. |
| Đăng xuất | Xóa danh tính trong khung cũ, đóng khung và mở form đăng nhập mới với các trường trống, mật khẩu được che lại. |

Ứng dụng **không cần SQL Server hoặc thư viện ngoài JDK**. Sidebar Quản lý mở Tổng quan điều hành; các mục còn lại thông báo chưa triển khai, bốn đường tắt tương ứng đang khóa. “Quên mật khẩu?” hiển thị hướng dẫn liên hệ Quản lý theo Page 3. Bốn tài khoản và nguồn xác thực hiện tại được giữ nguyên.

`TrangChuQuanLyDAO` cấp dữ liệu chỉ đọc cho vai trò Quản lý, chốt **27/10/2026 lúc 15:30**. Kỳ **21–27/10/2026** có **9 hóa đơn**, tổng **5.940.000đ** (tiền mặt 3.370.000đ, chuyển khoản 2.570.000đ). Chỉ số, biểu đồ và dialog dùng cùng nguồn; doanh thu tính theo thời điểm thanh toán. Chờ bàn giao thuộc Tiếp nhận sau khi Thu ngân xác nhận tiền. Bộ dữ liệu này chưa đồng bộ với các DAO nghiệp vụ khác.

### Tài khoản có sẵn

| Tên đăng nhập | Mật khẩu | Vai trò |
| --- | --- | --- |
| `quanly` | `123` | Quản lý |
| `tiepnhan` | `123` | Nhân viên tiếp nhận |
| `kythuat` | `123` | Nhân viên kỹ thuật |
| `thungan` | `123` | Thu ngân |

Đây là dữ liệu đăng nhập hiện tại của ứng dụng, được tạo sẵn trong code. Cả bốn tài khoản dùng mật khẩu `123` theo yêu cầu đã chốt. DAO đối chiếu bằng PBKDF2 có salt từ JDK theo QUY_DINH; không đưa mật khẩu vào entity hoặc log. Tên đăng nhập bỏ khoảng trắng hai đầu và phân biệt hoa/thường; mật khẩu giữ nguyên khoảng trắng.

Vai trò lấy từ tài khoản xác thực, không có lựa chọn tự đổi vai trò. DAO tổng quan kiểm tra quyền Quản lý cho cả đọc tổng và chi tiết ngày; các nghiệp vụ khác kiểm tra tại DAO tương ứng.

## 4. Công nghệ và môi trường

| Thành phần | Yêu cầu |
| --- | --- |
| Ngôn ngữ | Java, **JDK 17**, không bật tính năng preview. |
| IDE chuẩn | Eclipse IDE có hỗ trợ Java 17. |
| Giao diện | Java Swing/AWT. |
| Encoding | UTF-8 cho mã nguồn và tài liệu. |
| Cơ sở dữ liệu khi tích hợp | SQL Server 2022. |
| Kết nối khi tích hợp | Microsoft JDBC Driver for SQL Server tương thích JDK 17; nhóm chưa chốt phiên bản. |
| Quản trị database | SQL Server Management Studio (SSMS); dùng để quản trị và chạy SQL, ứng dụng kết nối trực tiếp đến SQL Server qua JDBC. |
| Quản lý mã nguồn | Git. |

Dự án hiện dùng cấu hình Java của Eclipse, chưa có Maven hoặc Gradle. Source ứng dụng là `src/`, output `bin/`; source kiểm thử riêng là `test/`, output `bin-test/`.

## 5. Cấu trúc dự án

```text
src/
├── module-info.java
├── app/
│   └── App.java
├── ui/
│   ├── MainFrame.java
│   ├── LoginFrame.java
│   └── component/
│       ├── SidebarPanel.java
│       ├── QuanLyNavigationPanel.java
│       ├── AppMenuBar.java
│       ├── UiTheme.java
│       ├── WindowTitlePanel.java
│       ├── WindowMouseHandler.java
│       └── font/               # Noto Sans Regular/Bold và giấy phép OFL.txt
├── dao/
│   └── TaiKhoanDAO.java
├── entity/
│   ├── TaiKhoan.java
│   └── VaiTro.java
└── connectDB/                  # Chưa có lớp

test/
└── dao/
    ├── TaiKhoanDAOTest.java      # Chạy bằng main(), không cần JUnit
    └── TrangChuQuanLyDAOTest.java
```

`MainFrame` chỉ quản lý bố cục bằng `BorderLayout`. `SidebarPanel` quản lý vùng điều hướng; `UiTheme` tập trung màu, font và khoảng cách. Các thành phần giữ trạng thái riêng bằng thuộc tính private, không dùng dữ liệu giao diện static dùng chung.

`QuanLyNavigationPanel` chứa các mục của Quản lý. `ButtonGroup` giữ duy nhất một mục được chọn; panel báo tên mục cho `MainFrame` để mở lại tổng quan hoặc thông báo chức năng chưa triển khai.

Tổng quan dùng `TrangChuQuanLyDAO` → `TongQuanQuanLy` (chứa `MucTongQuanQuanLy`, `HoaDonDoanhThu`) → `TrangChuQuanLyPanel`. `BieuDoDoanhThuPanel` vẽ các cột và báo ngày được chọn cho `MainFrame` để đọc DAO, mở `ChiTietDoanhThuDialog`. Tiền dùng `BigDecimal`; entity không phụ thuộc Swing, UI không tự tạo chứng từ. Nút “Tác vụ thường dùng” cuộn đến đường tắt. Trang chuyển thành một cột khi hẹp và cuộn dọc khi thiếu chiều cao.

`AppMenuBar` hiển thị các nhóm menu. Menu Hệ thống nhận hai thao tác từ `MainFrame`: Đăng xuất gọi hàm `dangXuat()` để kết thúc phiên; Thoát phát sự kiện đóng cửa sổ như nút ×. Panel menu không tự quản lý phiên hoặc tạo form đăng nhập.

Phần đăng nhập có bốn lớp, mỗi lớp một trách nhiệm:

| Lớp | Trách nhiệm |
| --- | --- |
| `TaiKhoan` | Giữ tên đăng nhập và vai trò, không cho thay đổi sau khi tạo. |
| `VaiTro` | Khai báo bốn vai trò và tên hiển thị tiếng Việt. |
| `TaiKhoanDAO` | Khai báo danh sách bốn tài khoản; duyệt danh sách và kiểm tra mật khẩu. |
| `LoginFrame` | Tạo biểu mẫu, nhận thao tác và hiển thị kết quả xác thực. |

Đọc `LoginFrame` theo luồng `dangNhap()` → `xacThucTaiKhoan()` → `moKhungChinh()`. `dangNhap()` kiểm tra trường trống; `xacThucTaiKhoan()` gọi DAO ở nền rồi nhận kết quả trên EDT; `moKhungChinh()` mở layout với tài khoản đúng. Đăng xuất mở lại `new LoginFrame()` với dữ liệu nhập trống. Không có lớp service hoặc interface trung gian.

Khi tích hợp chức năng, dùng `winChinh.getTaiKhoanDangNhap()` để lấy danh tính hiện tại; phương thức báo lỗi nếu khung đã đăng xuất. Khi nối database, cập nhật nguồn dữ liệu tại `TaiKhoanDAO`.

`WindowTitlePanel` vẽ thanh tiêu đề trắng theo Figma, thay thanh tiêu đề có màu nhấn của Windows. `WindowMouseHandler` hỗ trợ kéo tiêu đề, nhấp đôi để phóng to/khôi phục và kéo các cạnh/góc để đổi kích thước, giữ tối thiểu 960×600. Các nút cửa sổ hoạt động độc lập với chức năng nghiệp vụ.

Layout và đăng nhập Page 3 dùng Segoe UI của Windows theo Design System. Theme Noto Sans cũ được giữ cho các màn nghiệp vụ đã tích hợp; Font mặc định cho màn hình chức năng vẫn là 14 pt theo QUY_DINH. Nguồn font: [kho Noto chính thức](https://github.com/notofonts/noto-fonts/tree/main/hinted/ttf/NotoSans); giữ giấy phép `src/ui/component/font/OFL.txt` khi phân phối. Eclipse tự sao chép các tài nguyên font sang `bin/`, không cần cài font trên Windows hoặc thêm thư viện JAR.

Khi phát triển tiếp, đặt màn hình tại `ui.screen`, hộp thoại tại `ui.dialog`, thành phần dùng chung tại `ui.component`. Giữ luồng **UI → DAO → SQL Server**; `entity` chứa đối tượng dữ liệu, `connectDB` cung cấp kết nối. Không đặt SQL trong UI hoặc tạo package theo tên thành viên. Chưa tạo lớp DAO/entity khi chưa có nhu cầu.

### Gắn màn hình của thành viên

Các API sau gọi trên EDT, sau khi tạo `MainFrame`:

```java
// pnlChucNang là JPanel của thành viên, tự quản lý nội dung và JScrollPane nếu cần.
winChinh.setScreen(pnlChucNang);

// pnlDieuHuong là JPanel chứa các mục điều hướng do nhóm bổ sung sau.
winChinh.setSidebarContent(pnlDieuHuong);
winChinh.setStatus("Sẵn sàng");
```

`setScreen()` và `setSidebarContent()` thay panel cũ, cập nhật layout và vẽ lại. Không cần sửa bố cục khung chính để gắn một màn hình. Panel điều hướng nên đặt `setOpaque(false)` để giữ màu sidebar. Gọi `setSidebarContent()` sẽ thay toàn bộ danh sách mục hiện tại. Nếu một phân hệ có nhiều màn hình, thành viên có thể dùng `CardLayout` bên trong panel của mình; layout hiện tại chưa có trang giả hoặc điều hướng nghiệp vụ.

## 6. Cài đặt và chạy

### Chạy bằng Eclipse

1. Cài **JDK 17**. Trong Eclipse, vào **Window → Preferences → Java → Installed JREs**, thêm JDK và cấu hình execution environment **JavaSE-17**.
2. Chọn **File → Import → General → Existing Projects into Workspace**, trỏ đến thư mục dự án có file `.project`.
3. Kiểm tra **Java Build Path**: JRE System Library là **JavaSE-17**, source ứng dụng `src/` → `bin/`, source kiểm thử `test/` → `bin-test/`. Trong **Properties → Resource**, chọn encoding **UTF-8**.
4. Refresh dự án bằng **F5**. Nếu còn lỗi cấu hình cũ, chọn **Project → Clean** để build lại.
5. Mở `src/app/App.java`, chọn **Run As → Java Application**. Main class là **`app.App`**.

Khi chạy, ứng dụng mở form đăng nhập. Dùng một tài khoản trong bảng trên để vào layout chung. Giữ `module-info.java` với hai khai báo `requires java.desktop;` và `requires java.sql;`.

### Chạy bằng PowerShell

Mở PowerShell tại thư mục gốc dự án. Kiểm tra cả `java` và `javac` đều trỏ tới **JDK 17**; chỉ cài JRE sẽ không có trình biên dịch `javac`.

```powershell
java -version
javac -version
```

Biên dịch mã nguồn và chạy module:

```powershell
$sourceFiles = @(Get-ChildItem -LiteralPath src -Recurse -Filter '*.java' | ForEach-Object FullName)
New-Item -ItemType Directory -Path bin -Force | Out-Null
javac --release 17 -encoding UTF-8 -d bin $sourceFiles
if ($LASTEXITCODE -ne 0) { throw 'Biên dịch thất bại. Kiểm tra lỗi trước khi chạy.' }
New-Item -ItemType Directory -Path bin/ui/component/font -Force | Out-Null
Copy-Item -Path src/ui/component/font/* -Destination bin/ui/component/font -Force
New-Item -ItemType Directory -Path bin/ui/component/image -Force | Out-Null
Copy-Item -Path src/ui/component/image/* -Destination bin/ui/component/image -Force
java --module-path bin -m Nhom01_ApplicationDevelopment/app.App
```

Lệnh trên áp dụng cho khung UI hiện tại, chưa có thư viện ngoài. Khi bổ sung JDBC driver, phải cập nhật Modulepath và hướng dẫn chạy tương ứng trong [hướng dẫn thư viện](lib/README.md).

### Kiểm tra nhanh sau khi chạy

- Form đăng nhập bám Figma, hai ô nhập ban đầu trống. Thử thiếu thông tin, sai mật khẩu, hiện/ẩn mật khẩu và nhấn Enter.
- Lần lượt đăng nhập bốn tài khoản có sẵn; tiêu đề, sidebar và thanh trạng thái hiển thị đúng tài khoản/vai trò.
- Nhấn “Đăng xuất”: khung cũ đóng, form đăng nhập mới có hai ô trống và mật khẩu được che.
- Khung chung có sidebar tối 216 px, header trắng và danh tính ở header/sidebar. Quản lý mở tổng quan; menu Hệ thống dùng được, các nhóm còn lại chỉ hiển thị nhãn.
- Mở Hệ thống bằng chuột hoặc Alt+H. Chọn Đăng xuất bằng chuột hoặc phím D khi menu đang mở: phiên cũ kết thúc, form đăng nhập mới trống. Đăng xuất nằm trong menu Hệ thống.
- Nhấn Esc hoặc bấm ra ngoài để đóng menu. Mở lại rồi chọn Thoát (hoặc phím T khi menu mở): ứng dụng đóng như nút ×, không mở lại form đăng nhập.
- Đăng nhập `quanly`: sidebar có 7 mục Page 3, mặc định Tổng quan điều hành. Bốn chỉ số lần lượt 08/04/02/02; có 3 lệnh đến hạn ngày 27/10. Chọn mục khác báo chưa triển khai; chọn Tổng quan để trở lại trang.
- Hover từng cột: tooltip hiện đủ ngày, tiền và số hóa đơn. Bấm cột (hoặc Tab rồi Enter/Space): dialog có đúng hóa đơn ngày đó, tổng khớp cột. Ngày 27/10 có 2 hóa đơn, tổng 1.450.000đ bằng chuyển khoản. Esc hoặc Đóng để trở về.
- Bốn vai trò đều đăng xuất được từ Hệ thống. Các màn/điều hướng kỹ thuật và sửa chữa đã tích hợp của thành viên khác được giữ nguyên.
- Tổng quan chỉ đọc bộ dữ liệu có sẵn; không ghi hóa đơn, sửa chữa, bàn giao hoặc kho.
- Kiểm tra 1920×1200, 1366×768 và tối thiểu 960×600; sidebar giữ 216 px. Khi hẹp, ẩn vùng giữ chỗ tìm kiếm/thông báo để menu và danh tính không chồng nhau.
- Kéo thanh tiêu đề để di chuyển, kéo cạnh/góc để đổi kích thước; kiểm tra nút thu nhỏ, phóng to/khôi phục và đóng ở góc trên phải.

### Chạy kiểm thử dữ liệu

Sau khi biên dịch ứng dụng, chạy các lệnh sau tại thư mục gốc bằng JDK 17. Kiểm thử không mở giao diện hoặc kết nối database, báo lỗi bằng `AssertionError` nếu kết quả sai.

```powershell
$testFiles = @(Get-ChildItem -LiteralPath test -Recurse -Filter '*.java' | ForEach-Object FullName)
New-Item -ItemType Directory -Path bin-test -Force | Out-Null
javac --release 17 -encoding UTF-8 -Xlint:all -cp bin -d bin-test $testFiles
if ($LASTEXITCODE -ne 0) { throw 'Biên dịch kiểm thử thất bại.' }
java '-Dfile.encoding=UTF-8' -cp 'bin;bin-test' dao.TaiKhoanDAOTest
java '-Dfile.encoding=UTF-8' -cp 'bin;bin-test' dao.TrangChuQuanLyDAOTest
```

Kiểm thử tổng quan kiểm tra quyền theo vai trò, số lượng hồ sơ, tổng bảy ngày khớp từng hóa đơn, tiền mặt/chuyển khoản, dữ liệu rỗng, biên nửa đêm, hóa đơn ngoài kỳ, chứng từ trùng và thanh toán sau thời điểm chốt. Chương trình kiểm tra Swing và ảnh đối chiếu được lưu cục bộ trong `output/manager-overview/`.

Kết quả tổng quan Quản lý ngày 28/10/2026:

- **PASS:** biên dịch toàn bộ Java 17 với `-Xlint:all`; kiểm thử DAO tổng quan, đăng nhập và dữ liệu kỹ thuật hiện có.
- **PASS:** kiểm tra Swing hover, tooltip và bấm cả 7 cột mở dialog đúng ngày; số dòng/tổng khớp DAO, bảng chỉ đọc; đóng bằng Esc/nút mặc định, ngày rỗng trả 0đ.
- **PASS:** render và xem ảnh 1920×1234, 1366×768, 960×600; kiểm tra cuộn/đường tắt trên cửa sổ hẹp, bảng dialog có cuộn ngang khi thiếu chỗ.
- **PASS:** kiểm tra Swing bốn luồng đăng nhập/đăng xuất, thông tin sai/thiếu, hiện mật khẩu và đóng khi đang xác thực.
- **NOT_RUN:** thao tác thủ công trong Eclipse và đồng bộ dữ liệu nghiệp vụ/database.

Kết quả cập nhật layout/đăng nhập theo Page 3 ngày 28/10/2026 (trước phần tổng quan):

- **PASS:** Java 17 biên dịch toàn bộ source; kiểm thử DAO giữ nguyên bốn tài khoản/mật khẩu.
- **PASS:** kiểm tra Swing đủ bốn luồng đăng nhập → khung chung → Hệ thống/Đăng xuất; xóa phiên và mở lại form trống.
- **PASS:** trường thiếu/sai, giữ dữ liệu khi lỗi, hiện/ẩn mật khẩu, nút mặc định Enter, trạng thái chờ và đóng lúc đang xác thực.
- **PASS:** render đăng nhập và layout Quản lý ở 1920, 1366 và 960 px; kiểm tra menu không đè danh tính, 7 mục chọn độc quyền.
- **NOT_RUN:** thao tác chuột/bàn phím thủ công trong Eclipse và nghiệp vụ/database. Tìm nhanh/thông báo vẫn chưa triển khai.

`AppHeaderPanel` ghép menu với danh tính; `UserInfoPanel` tái sử dụng ở hai vị trí; `LoginBackgroundPanel` chỉ trình bày ảnh nền và hỗ trợ cuộn. Ảnh/icon gốc và các layer Figma được ghi tại `src/ui/component/image/README.md`; Eclipse sao chép tài nguyên này sang `bin/`.

Các kết quả bên dưới là lịch sử kiểm tra giao diện trước Page 3; kích thước và số mục cũ không còn mô tả bản hiện tại.

Kết quả kiểm tra menu Hệ thống ngày 26/10/2026:

- **PASS:** biên dịch Java 17 với `-Xlint:all`; kiểm tra sidebar HT01 vẫn đạt.
- **PASS:** chương trình kiểm tra Swing tạm xác nhận Đăng xuất/Thoát ở cả bốn vai trò, đăng xuất từ menu/sidebar chỉ kết thúc phiên một lần và xóa danh tính cũ.
- **PASS:** Thoát phát sự kiện đóng cửa sổ, không gọi thao tác mở lại đăng nhập; chính sách cửa sổ vẫn là `EXIT_ON_CLOSE`.
- **PASS:** phím mở menu Alt+H được Swing đăng ký; mục Đăng xuất/Thoát có mnemonic D/T. Render menu ở ba kích thước; đã xem ảnh 1280×800.
- **NOT_RUN:** thao tác chuột/bàn phím thủ công trong Eclipse, gồm mở/đóng popup và Esc; chưa đối chiếu trực tiếp được frame SYS_MENU trên Figma. Menu hiện chỉ nối hai thao tác đã có, chưa xác nhận đầy đủ các mục của thiết kế.

Kết quả kiểm tra sidebar Quản lý (HT01) ngày 26/10/2026:

- **PASS:** biên dịch bằng JDK 17 với `--release 17 -encoding UTF-8 -Xlint:all`.
- **PASS:** chương trình kiểm tra Swing tạm xác nhận đủ 8 mục đúng thứ tự, mặc định Trang chủ, chọn độc quyền, màu/chữ và thông báo chưa triển khai; không sinh màn hình giả.
- **PASS:** render ở 1280×800, 1366×768 và 960×600; sidebar rộng 180 px, hàng cao 32 px, nhãn không bị cắt. Đã xem ảnh render 1280×800.
- **PASS:** ba vai trò khác không có các mục Quản lý; thay panel điều hướng và đăng xuất/xóa phiên vẫn hoạt động.
- **NOT_RUN:** thao tác bàn phím/chuột thủ công trong Eclipse; chưa đối chiếu lại trực tiếp Figma vì liên kết không truy cập được qua công cụ.

Kết quả kiểm tra đăng nhập sau khi rút gọn code ngày 20/10/2026:

- **PASS:** JDK 17 biên dịch ứng dụng và kiểm thử với `-Xlint:all`.
- **PASS:** bộ kiểm thử trong `test/` xác nhận bốn tài khoản/mật khẩu `123` đúng vai trò; từ chối tài khoản lạ, mật khẩu sai/rỗng và đầu vào không hợp lệ.
- **PASS:** chương trình kiểm tra Swing tạm chạy đủ bốn luồng đăng nhập → khung chung → đăng xuất; kiểm tra danh tính, vai trò và xóa phiên cũ.
- **PASS:** kiểm tra thông báo thiếu/sai thông tin, giữ dữ liệu khi lỗi, hiện/ẩn mật khẩu, nút mặc định Enter, khóa form khi xử lý và đóng form trong lúc xác thực.
- **PASS:** render form 600×404 và đối chiếu Figma; layout chung giữ kích thước header, sidebar, footer và vùng nội dung trống ở ba kích thước bên dưới.
- **PASS:** ảnh form trước/sau khi rút gọn code giống hệt nhau (SHA-256 trùng khớp).
- **NOT_RUN:** thao tác thủ công trong Eclipse; kiểm tra quyền nghiệp vụ và database chưa thuộc phạm vi triển khai.

Kết quả kiểm tra layout ngày 20/10/2026:

- **PASS:** biên dịch toàn bộ source bằng JDK 17.0.18 với `--release 17 -encoding UTF-8 -Xlint:all`.
- **PASS:** rà soát package, đóng gói, EDT, tên biến Swing; kiểm tra JavaDoc, import tường minh, thụt lề 4 space và dòng không quá 120 ký tự.
- **PASS:** render Swing ở 1280×800, 1366×768 và 960×600; sidebar rộng 180 px, vùng nội dung trống co giãn, menu chưa có hành động.
- **PASS:** font Noto Sans nạp từ classpath; header cao 58 px (32 + 26), thanh trạng thái 22 px, đối chiếu ảnh render với Figma.
- **PASS:** gắn và thay panel nội dung/điều hướng, cập nhật trạng thái trên EDT bằng chương trình kiểm tra tạm.
- **PASS:** chương trình kiểm tra tạm gọi handler kéo tiêu đề, kiểm tra con trỏ và đổi kích thước tại bốn cạnh/bốn góc, giới hạn 960×600, các nút phóng to/khôi phục/thu nhỏ/đóng. Đây là kiểm tra chương trình, chưa thay thế thao tác chuột thủ công trên máy người dùng.
- **PASS:** ảnh render 1280×800 trước/sau lần rút gọn code giống hệt nhau (SHA-256 trùng khớp).
- **NOT_RUN:** thao tác thủ công trong Eclipse; nghiệp vụ và database nằm ngoài phạm vi.

Ảnh render và chương trình kiểm tra tạm nằm trong `output/`, được Git bỏ qua và không phải thành phần ứng dụng.

## 7. Chuẩn bị tích hợp cơ sở dữ liệu

Phần này dành cho giai đoạn phát triển tiếp; khung UI hiện tại chưa đọc cấu hình hoặc mở kết nối database.

1. **Chốt schema và dữ liệu mẫu** theo REPORT V3. Lưu các script SQL trong Git, ghi rõ thứ tự chạy bằng SSMS; có thể đặt ngay thư mục gốc khi số file còn ít.
2. **Chốt JDBC driver** tương thích JDK 17. Ghi phiên bản, tên JAR, nguồn tải và cách thêm vào Modulepath trong `lib/README.md`; kiểm tra tên module thực tế trước khi thêm `requires`.
3. **Chuẩn bị cấu hình cục bộ.** Bản làm việc hiện có `config/database.properties.example`; sao chép thành `config/database.properties` và điền cấu hình riêng:

   ```powershell
   Copy-Item -LiteralPath config/database.properties.example -Destination config/database.properties
   ```

   File mẫu gồm `db.url`, `db.username`, `db.password` và `db.queryTimeoutSeconds`. Host, cổng và tên database chỉ là ví dụ; thay bằng thông tin SQL Server thực tế. Không dùng tài khoản `sa` cho ứng dụng hoặc đưa mật khẩu vào mã nguồn.
4. **Triển khai `connectDB`, `entity` và `dao`**, sau đó tích hợp vào UI. Dùng `PreparedStatement`, đóng tài nguyên JDBC đúng cách và thực hiện các thao tác thanh toán/kho liên quan trong cùng transaction.
5. **Kiểm thử bằng database phát triển riêng** trước khi tích hợp: lỗi kết nối, dữ liệu sai/trùng, rollback, xuất vượt tồn và thanh toán lặp.

Hiện toàn bộ `config/` đang bị `.gitignore` loại khỏi Git, nên bản clone có thể không có file mẫu. Khi tích hợp database, nhóm cần bổ sung file mẫu không chứa bí mật vào bản bàn giao và cập nhật cấu hình Git để thành viên khác thiết lập lại được.

## 8. Quy ước phát triển và kiểm thử

Khi hoàn thiện, hệ thống cần đáp ứng các yêu cầu chất lượng trong REPORT: giao diện tiếng Việt dễ dùng; tra cứu và cập nhật phản hồi nhanh; hoạt động ổn định khi nhiều nhân viên cùng sử dụng; dữ liệu nhất quán, không thiếu hoặc trùng khi lưu lỗi. Dữ liệu cần được sao lưu định kỳ, kiểm tra khả năng phục hồi và lưu lịch sử để tra cứu. Mã nguồn và cấu trúc dữ liệu phải thuận tiện cho bảo trì, bổ sung chức năng.

- Giữ cấu trúc package đã thống nhất; không tạo lớp hoặc thư mục nghiệp vụ chỉ để làm đầy cấu trúc.
- Tên lớp nghiệp vụ dùng tiếng Việt không dấu theo REPORT, ví dụ `KhachHang`, `PhieuSuaChua`, `HoaDonDAO`. Giao diện và tài liệu dùng tiếng Việt có dấu.
- Tiền dùng `BigDecimal`; ngày và thời điểm dùng `LocalDate`, `LocalDateTime`. Không dùng `float`/`double` để tính tiền.
- Khởi tạo và cập nhật Swing trên EDT. Truy vấn SQL và tác vụ lâu chạy nền, thường bằng `SwingWorker`, để tránh treo giao diện.
- Kiểm tra quyền tại DAO, băm mật khẩu có salt và ghi lịch sử thao tác quan trọng khi triển khai xác thực và nghiệp vụ.
- Kiểm thử cả luồng hợp lệ, dữ liệu sai, thiếu quyền, lỗi lưu và thao tác đồng thời. Ghi rõ kết quả `PASS`, `FAIL`, `NOT_RUN`; giao diện mẫu chưa được xem là chức năng nghiệp vụ hoàn thành.
- Dùng nhánh riêng cho từng công việc, commit theo mẫu `<type>: <nội dung>` và có ít nhất một thành viên khác review trước khi merge. PR nêu yêu cầu liên quan, cách kiểm tra và giới hạn còn lại.
- Không commit `bin/`, file `.class`, log, backup database, thông tin đăng nhập hoặc dữ liệu khách hàng thật. Giữ cấu hình Eclipse dùng chung với đường dẫn tương đối.

## 9. Tài liệu đối chiếu

- **`Nhom01_REPORT_ApplicationDevelopment_V3.docx`**: căn cứ về phạm vi, tác nhân, Use Case và thiết kế nghiệp vụ; được cung cấp riêng, hiện chưa nằm trong repository.
- **[QUY_DINH_DU_AN.md](skills/QUY_DINH_DU_AN.md)**, phiên bản 4.0 ngày 20/10/2026: quy định về package, dữ liệu, Swing, phân quyền, Git và kiểm thử. File đang nằm trong `skills/` của bản làm việc cục bộ; thư mục này bị Git bỏ qua nên cần được chia sẻ riêng nếu giữ cấu hình hiện tại.
- **[lib/README.md](lib/README.md)**: hướng dẫn quản lý và bổ sung thư viện.
- **[Figma layout chung](https://www.figma.com/design/XEDrqEs0dSe51rsn2PIQ6s/?node-id=2095-5572)**: đối chiếu màu, thanh menu, sidebar và thanh trạng thái; nội dung dashboard không thuộc đầu việc layout.
- **[Figma đăng nhập](https://www.figma.com/design/XEDrqEs0dSe51rsn2PIQ6s/?node-id=2080-1766)**: form đăng nhập trong phần Luân.

Quyền bàn giao trong README áp dụng theo bảng Use Case của REPORT và mục 11.1 của QUY_DINH: **thu ngân xác nhận tiền, nhân viên tiếp nhận xác nhận bàn giao**. Các mô tả chưa thống nhất trong báo cáo cần được nhóm chốt và cập nhật khi triển khai.

Khi tham chiếu đặc tả chi tiết trong REPORT, dùng ánh xạ: `UC_01 → UC001` (hóa đơn), `UC_02 → UC019` (kiểm tra xe), `UC_03 → UC010` (tiếp nhận), `UC_04 → UC036` (doanh thu).
