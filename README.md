# Hệ thống quản lý dịch vụ sửa chữa Garage Ô tô Đức Dũng

Ứng dụng desktop nội bộ giúp Garage Ô tô Đức Dũng quản lý hồ sơ khách hàng và xe, tiếp nhận, kiểm tra, báo giá, sửa chữa, phụ tùng, thanh toán và bàn giao xe. Dự án do **Nhóm 01** thực hiện trong môn **Phát triển ứng dụng**, Khoa Công nghệ thông tin, Trường Đại học Công nghiệp TP.HCM, học kỳ 1 năm học 2026–2027.

**Trạng thái hiện tại:** dự án mới triển khai khung giao diện Swing. Các chức năng nghiệp vụ dưới đây là phạm vi cần xây dựng theo báo cáo, chưa phải các chức năng đã hoàn thành.

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

| Thành phần | Hiện trạng |
| --- | --- |
| Khởi chạy | Có `app.App`, mở giao diện trên Event Dispatch Thread (EDT). |
| Khung giao diện | Có `MainFrame`, menu, sidebar, thanh trạng thái và điều hướng bằng `CardLayout`. |
| Các trang nội dung | Có sáu trang giữ chỗ: Trang chủ, Nhân viên, Tài khoản và phân quyền, Dịch vụ và bảng giá, Phụ tùng, Lịch sử thao tác. Chưa có biểu mẫu hay dữ liệu nghiệp vụ. |
| Tài khoản | Hiển thị nhãn demo `demo.nhom01`, vai trò `Quản lý`; chưa có đăng nhập hoặc kiểm tra quyền. |
| Dữ liệu | `dao`, `entity`, `connectDB` mới có thư mục giữ chỗ; chưa có lớp xử lý, JDBC driver hoặc script tạo database. |

Các mục chưa triển khai như nhập kho, thống kê, đổi vai trò và đăng xuất đang bị vô hiệu hóa. Trang Lịch sử thao tác cũng chỉ là trang giữ chỗ, chưa ghi nhận hoạt động người dùng.

**Có thể mở khung giao diện mà không cần SQL Server hoặc thư viện ngoài JDK.**

## 4. Công nghệ và môi trường

| Thành phần | Yêu cầu |
| --- | --- |
| Ngôn ngữ | Java, **JDK 21**, không bật tính năng preview. |
| IDE chuẩn | Eclipse IDE có hỗ trợ Java 21. |
| Giao diện | Java Swing/AWT. |
| Encoding | UTF-8 cho mã nguồn và tài liệu. |
| Cơ sở dữ liệu khi tích hợp | SQL Server 2022. |
| Kết nối khi tích hợp | Microsoft JDBC Driver for SQL Server tương thích JDK 21; nhóm chưa chốt phiên bản. |
| Quản trị database | SQL Server Management Studio (SSMS); dùng để quản trị và chạy SQL, ứng dụng kết nối trực tiếp đến SQL Server qua JDBC. |
| Quản lý mã nguồn | Git. |

Dự án hiện dùng cấu hình Java của Eclipse, chưa có Maven hoặc Gradle. Chỉ `src/` được khai báo là source folder; kết quả biên dịch nằm trong `bin/`.

## 5. Cấu trúc dự án

```text
Nhom01_ApplicationDevelopment/
├── src/
│   ├── module-info.java
│   ├── app/
│   │   └── App.java
│   ├── ui/
│   │   ├── MainFrame.java
│   │   ├── screen/
│   │   │   ├── ShellPage.java
│   │   │   └── PlaceholderPanel.java
│   │   ├── dialog/             # Chưa có lớp
│   │   └── component/
│   │       ├── SidebarPanel.java
│   │       └── UiTheme.java
│   ├── dao/                    # Chưa có lớp
│   ├── entity/                 # Chưa có lớp
│   └── connectDB/              # Chưa có lớp
├── lib/
│   └── README.md               # Hướng dẫn bổ sung thư viện
├── .settings/                  # Cấu hình Java 21 và UTF-8
├── .classpath
├── .project
├── .gitignore
└── README.md
```

Trách nhiệm các package khi phát triển tiếp:

| Package | Trách nhiệm |
| --- | --- |
| `app` | Điểm chạy và khởi tạo ứng dụng; giữ `main()` ngắn. |
| `ui`, `ui.screen`, `ui.dialog`, `ui.component` | Khung chính, màn hình, hộp thoại và thành phần dùng chung; kiểm tra nhập liệu và hiển thị kết quả, không chứa SQL. |
| `dao` | Truy vấn, ánh xạ dữ liệu, kiểm tra quyền và điều kiện nghiệp vụ; điều phối transaction, không phụ thuộc Swing. |
| `entity` | Đối tượng dữ liệu và enum nghiệp vụ; không truy vấn database hoặc chứa thành phần giao diện. |
| `connectDB` | Đọc cấu hình và cung cấp kết nối cho DAO; không chứa nghiệp vụ. |

Luồng xử lý khi tích hợp: **UI → DAO → SQL Server → DAO → UI**. DAO lấy kết nối từ `connectDB`, nhận và trả dữ liệu qua `entity`. Dự án hiện không có tầng service; DAO chịu trách nhiệm cho các thao tác nghiệp vụ cần transaction. Giữ đúng tên `connectDB`; `dao` và `entity` chưa chia package con.

## 6. Cài đặt và chạy

### Chạy bằng Eclipse

1. Cài **JDK 21**. Trong Eclipse, vào **Window → Preferences → Java → Installed JREs**, thêm JDK và cấu hình execution environment **JavaSE-21**.
2. Chọn **File → Import → General → Existing Projects into Workspace**, trỏ đến thư mục dự án có file `.project`.
3. Kiểm tra **Java Build Path**: JRE System Library là **JavaSE-21**, source là `src/`, output là `bin/`. Trong **Properties → Resource**, chọn encoding **UTF-8**.
4. Refresh dự án bằng **F5**. Nếu còn lỗi cấu hình cũ, chọn **Project → Clean** để build lại.
5. Mở `src/app/App.java`, chọn **Run As → Java Application**. Main class là **`app.App`**.

Khi chạy, ứng dụng mở thẳng khung Quản lý với tài khoản demo, chưa yêu cầu đăng nhập. Giữ `module-info.java` với hai khai báo `requires java.desktop;` và `requires java.sql;`.

### Chạy bằng PowerShell

Mở PowerShell tại thư mục gốc dự án. Kiểm tra cả `java` và `javac` đều trỏ tới **JDK 21**; chỉ cài JRE sẽ không có trình biên dịch `javac`.

```powershell
java -version
javac -version
```

Biên dịch mã nguồn và chạy module:

```powershell
$sourceFiles = @(Get-ChildItem -LiteralPath src -Recurse -Filter '*.java' | ForEach-Object FullName)
New-Item -ItemType Directory -Path bin -Force | Out-Null
javac --release 21 -encoding UTF-8 -d bin $sourceFiles
if ($LASTEXITCODE -ne 0) { throw 'Biên dịch thất bại. Kiểm tra lỗi trước khi chạy.' }
java --module-path bin -m Nhom01_ApplicationDevelopment/app.App
```

Lệnh trên áp dụng cho khung UI hiện tại, chưa có thư viện ngoài. Khi bổ sung JDBC driver, phải cập nhật Modulepath và hướng dẫn chạy tương ứng trong [hướng dẫn thư viện](lib/README.md).

### Kiểm tra nhanh sau khi chạy

- Cửa sổ có menu, sidebar, vùng nội dung và thanh trạng thái hiển thị `demo.nhom01` cùng vai trò `Quản lý`.
- Chọn sáu mục điều hướng trên sidebar hoặc menu để kiểm tra tiêu đề trang và mục đang được chọn. Mỗi trang hiện hiển thị “Nội dung đang được chuẩn bị.”
- Các chức năng chưa triển khai vẫn bị vô hiệu hóa.
- Thử thay đổi kích thước ở màn hình 1366×768 và mức tối thiểu 960×600; kiểm tra tiếng Việt, vùng cuộn và thông tin tài khoản.
- Chọn **Hệ thống → Thoát** để đóng cửa sổ.

Dự án hiện chưa có thư mục `test/` hoặc bộ kiểm thử tự động. Các bước trên kiểm tra khung giao diện; nghiệp vụ và kết nối dữ liệu cần được kiểm thử khi triển khai.

## 7. Chuẩn bị tích hợp cơ sở dữ liệu

Phần này dành cho giai đoạn phát triển tiếp; khung UI hiện tại chưa đọc cấu hình hoặc mở kết nối database.

1. **Chốt schema và dữ liệu mẫu** theo REPORT V3. Lưu các script SQL trong Git, ghi rõ thứ tự chạy bằng SSMS; có thể đặt ngay thư mục gốc khi số file còn ít.
2. **Chốt JDBC driver** tương thích JDK 21. Ghi phiên bản, tên JAR, nguồn tải và cách thêm vào Modulepath trong `lib/README.md`; kiểm tra tên module thực tế trước khi thêm `requires`.
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
- **[QUY_DINH_DU_AN.md](skills/QUY_DINH_DU_AN.md)**, phiên bản 3.3 ngày 19/10/2026: quy định về package, dữ liệu, Swing, phân quyền, Git và kiểm thử. File đang nằm trong `skills/` của bản làm việc cục bộ; thư mục này bị Git bỏ qua nên cần được chia sẻ riêng nếu giữ cấu hình hiện tại.
- **[lib/README.md](lib/README.md)**: hướng dẫn quản lý và bổ sung thư viện.

Quyền bàn giao trong README áp dụng theo bảng Use Case của REPORT và mục 9.1 của QUY_DINH: **thu ngân xác nhận tiền, nhân viên tiếp nhận xác nhận bàn giao**. Các mô tả chưa thống nhất trong báo cáo cần được nhóm chốt và cập nhật khi triển khai.

Khi tham chiếu đặc tả chi tiết trong REPORT, dùng ánh xạ: `UC_01 → UC001` (hóa đơn), `UC_02 → UC019` (kiểm tra xe), `UC_03 → UC010` (tiếp nhận), `UC_04 → UC036` (doanh thu).
