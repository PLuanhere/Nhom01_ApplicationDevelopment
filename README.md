# Hệ thống quản lý dịch vụ sửa chữa Garage Ô tô Đức Dũng

Ứng dụng desktop nội bộ giúp Garage Ô tô Đức Dũng quản lý hồ sơ khách hàng và xe, tiếp nhận, kiểm tra, báo giá, sửa chữa, phụ tùng, thanh toán và bàn giao xe. Dự án do **Nhóm 01** thực hiện trong môn **Phát triển ứng dụng**, Khoa Công nghệ thông tin, Trường Đại học Công nghiệp TP.HCM, học kỳ 1 năm học 2026–2027.

**Trạng thái hiện tại:** đã có khung giao diện Swing, đăng nhập/đăng xuất bằng bốn tài khoản tạo sẵn trong code, sidebar Quản lý (HT01) và menu Hệ thống với Đăng xuất/Thoát. Sidebar cho chọn mục, chưa mở màn hình nghiệp vụ. Chưa kết nối database. Các chức năng nghiệp vụ dưới đây là phạm vi cần xây dựng theo báo cáo, chưa phải các chức năng đã hoàn thành.

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

Phạm vi bàn giao hiện tại gồm **layout chung và đăng nhập**, làm nền để các thành viên gắn màn hình của mình.

| Thành phần | Hiện trạng |
| --- | --- |
| Khởi chạy | `app.App` áp dụng theme và mở `LoginFrame` trên Event Dispatch Thread (EDT). Đăng nhập thành công mới mở `MainFrame`. |
| Header | Thanh tiêu đề trắng 32 px, icon vuông cam, nút thu nhỏ/phóng to/đóng; hàng menu cao 26 px, mỗi nhóm chiếm 108 px. Hệ thống có Đăng xuất/Thoát; các nhóm còn lại chỉ hiển thị nhãn. |
| Sidebar | Rộng 180 px, nền xanh xám, nhãn ĐỨC DŨNG, vai trò và nút đăng xuất. Quản lý có 8 mục chọn; các vai trò khác chưa có mục điều hướng. |
| Nội dung | Vùng trống co giãn theo cửa sổ, lề 16 px; chưa có dashboard, trang chức năng hoặc dữ liệu mẫu. |
| Thanh trạng thái | Hiển thị “Sẵn sàng”, tên đăng nhập và vai trò; có API cập nhật thông báo khi tích hợp. |
| Đăng nhập | Form 600×404 theo Figma; kiểm tra trường trống, hiện/ẩn mật khẩu, nhấn Enter để đăng nhập, báo lỗi khi thông tin sai. DAO kiểm tra ở luồng nền bằng `SwingWorker`. |
| Tài khoản và vai trò | Bốn tài khoản có sẵn trong `TaiKhoanDAO`, mỗi tài khoản một vai trò cố định. Danh tính bất biến, không chứa mật khẩu, được truyền cho khung chính sau xác thực. |
| Đăng xuất | Xóa danh tính trong khung cũ, đóng khung và mở form đăng nhập mới với các trường trống, mật khẩu được che lại. |

Ứng dụng **không cần SQL Server hoặc thư viện ngoài JDK**. Vùng nội dung vẫn trống; các mục sidebar Quản lý chưa mở màn hình nghiệp vụ. Nút “Quên mật khẩu?” trong Figma đang bị vô hiệu hóa vì chưa triển khai chức năng này.

### Tài khoản có sẵn

| Tên đăng nhập | Mật khẩu | Vai trò |
| --- | --- | --- |
| `quanly` | `123` | Quản lý |
| `tiepnhan` | `123` | Nhân viên tiếp nhận |
| `kythuat` | `123` | Nhân viên kỹ thuật |
| `thungan` | `123` | Thu ngân |

Đây là dữ liệu đăng nhập hiện tại của ứng dụng, được tạo sẵn trong code. Cả bốn tài khoản dùng mật khẩu `123` theo yêu cầu đã chốt. DAO đối chiếu bằng PBKDF2 có salt từ JDK theo QUY_DINH; không đưa mật khẩu vào entity hoặc log. Tên đăng nhập bỏ khoảng trắng hai đầu và phân biệt hoa/thường; mật khẩu giữ nguyên khoảng trắng.

Vai trò được lấy từ tài khoản xác thực, không có lựa chọn tự đổi vai trò. Hiện mới cấp và hiển thị vai trò; kiểm tra quyền của từng thao tác nghiệp vụ sẽ bổ sung tại DAO khi triển khai chức năng tương ứng.

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
    └── TaiKhoanDAOTest.java      # Chạy bằng main(), không cần JUnit
```

`MainFrame` chỉ quản lý bố cục bằng `BorderLayout`. `SidebarPanel` quản lý vùng điều hướng; `UiTheme` tập trung màu, font và khoảng cách. Các thành phần giữ trạng thái riêng bằng thuộc tính private, không dùng dữ liệu giao diện static dùng chung.

`QuanLyNavigationPanel` chứa các mục của Quản lý. `ButtonGroup` giữ duy nhất một mục được chọn; panel báo tên mục cho `MainFrame` để cập nhật thanh trạng thái. Chưa có màn hình nghiệp vụ hoặc truy cập DAO trong panel này.

`AppMenuBar` hiển thị các nhóm menu. Menu Hệ thống nhận hai thao tác từ `MainFrame`: Đăng xuất gọi chung hàm `dangXuat()` với sidebar; Thoát phát sự kiện đóng cửa sổ như nút ×. Panel menu không tự quản lý phiên hoặc tạo form đăng nhập.

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

Khung dùng Noto Sans đóng gói cùng source: menu/tiêu đề 13 pt, thương hiệu 14 pt đậm, trạng thái 11 pt. Font mặc định cho màn hình chức năng vẫn là 14 pt theo QUY_DINH. Nguồn font: [kho Noto chính thức](https://github.com/notofonts/noto-fonts/tree/main/hinted/ttf/NotoSans); giữ giấy phép `src/ui/component/font/OFL.txt` khi phân phối. Eclipse tự sao chép các tài nguyên font sang `bin/`, không cần cài font trên Windows hoặc thêm thư viện JAR.

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
java --module-path bin -m Nhom01_ApplicationDevelopment/app.App
```

Lệnh trên áp dụng cho khung UI hiện tại, chưa có thư viện ngoài. Khi bổ sung JDBC driver, phải cập nhật Modulepath và hướng dẫn chạy tương ứng trong [hướng dẫn thư viện](lib/README.md).

### Kiểm tra nhanh sau khi chạy

- Form đăng nhập bám Figma, hai ô nhập ban đầu trống. Thử thiếu thông tin, sai mật khẩu, hiện/ẩn mật khẩu và nhấn Enter.
- Lần lượt đăng nhập bốn tài khoản có sẵn; tiêu đề, sidebar và thanh trạng thái hiển thị đúng tài khoản/vai trò.
- Nhấn “Đăng xuất”: khung cũ đóng, form đăng nhập mới có hai ô trống và mật khẩu được che.
- Khung chung có thanh menu, sidebar màu xanh xám với nhãn ĐỨC DŨNG và vùng nội dung trống. Menu Hệ thống dùng được; các nhóm còn lại chỉ hiển thị nhãn.
- Mở Hệ thống bằng chuột hoặc Alt+H. Chọn Đăng xuất bằng chuột hoặc phím D khi menu đang mở: phiên cũ kết thúc, form đăng nhập mới trống. Thử cả từ menu và nút sidebar.
- Nhấn Esc hoặc bấm ra ngoài để đóng menu. Mở lại rồi chọn Thoát (hoặc phím T khi menu mở): ứng dụng đóng như nút ×, không mở lại form đăng nhập.
- Đăng nhập `quanly`: sidebar có 8 mục theo Figma, mặc định chọn Trang chủ. Chọn bằng chuột hoặc Tab rồi Space: chỉ một mục được tô nền xanh nhạt và in đậm; thanh trạng thái báo mục đó chưa triển khai. Không mở màn hình nghiệp vụ.
- Ba vai trò còn lại chưa có các mục điều hướng. Cả bốn vai trò vẫn có nút đăng xuất.
- Chưa có dashboard hoặc dữ liệu nghiệp vụ mẫu.
- Thử thay đổi kích thước ở 1366×768 và mức tối thiểu 960×600; sidebar giữ chiều rộng 180 px, vùng nội dung co giãn, thanh trạng thái nằm dưới cùng.
- Kéo thanh tiêu đề để di chuyển, kéo cạnh/góc để đổi kích thước; kiểm tra nút thu nhỏ, phóng to/khôi phục và đóng ở góc trên phải.

### Chạy kiểm thử đăng nhập

Sau khi biên dịch ứng dụng, chạy các lệnh sau tại thư mục gốc bằng JDK 17. Kiểm thử không mở giao diện hoặc kết nối database, báo lỗi bằng `AssertionError` nếu kết quả sai.

```powershell
$testFiles = @(Get-ChildItem -LiteralPath test -Recurse -Filter '*.java' | ForEach-Object FullName)
New-Item -ItemType Directory -Path bin-test -Force | Out-Null
javac --release 17 -encoding UTF-8 -Xlint:all -cp bin -d bin-test $testFiles
if ($LASTEXITCODE -ne 0) { throw 'Biên dịch kiểm thử thất bại.' }
java '-Dfile.encoding=UTF-8' -cp 'bin;bin-test' dao.TaiKhoanDAOTest
```

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
