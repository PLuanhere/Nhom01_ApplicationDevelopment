# Garage Ô tô Đức Dũng — Nhóm 01

Ứng dụng Java 17, Swing, tổ chức theo **Entity → DAO → UI**. Dữ liệu được tạo sẵn và lưu trong bộ nhớ DAO, chưa kết nối database.

| Thành viên | MSSV |
| --- | --- |
| Phan Hoàng Luân | 24675951 |
| Nguyễn Trung Hậu | 24678601 |
| Chung Ngô Minh Hoàng | 24654011 |
| Nguyễn Minh Vũ | 24635361 |

Thiết kế tham khảo: [Figma Page 3](https://www.figma.com/design/XEDrqEs0dSe51rsn2PIQ6s/?node-id=2458-6712). Nghiệp vụ theo REPORT và các quyết định đã chốt trong `skills/garage-development/references/QUYET_DINH_DA_CHOT.md`.

## Chạy ứng dụng

Trong Eclipse, chọn **JDK 17**, import project rồi chạy `src/app/App.java` bằng **Run As → Java Application**. Giao diện khởi tạo trên EDT của Swing.

| Tài khoản | Mật khẩu | Vai trò | Nhân viên |
| --- | --- | --- | --- |
| `quanly` | `123` | Quản lý | NV001 |
| `tiepnhan` | `123` | Tiếp nhận | NV003 |
| `kythuat` | `123` | Kỹ thuật | NV005 |
| `thungan` | `123` | Thu ngân | NV007 |

Bốn mật khẩu ban đầu là ngoại lệ để thử nghiệm. Tài khoản mới/đặt lại mật khẩu phải có ít nhất 8 ký tự, chữ hoa, chữ số và ký tự đặc biệt. Theo lựa chọn của nhóm, bản cục bộ lưu mật khẩu dạng chuỗi riêng trong DAO; không hiển thị mật khẩu trong bảng hoặc nhật ký.

Nếu chạy bằng PowerShell, cần đưa JDK 17 vào PATH trước; Java mặc định trên máy có thể vẫn là Java 8:

```powershell
$sources = @(Get-ChildItem src -Recurse -Filter *.java | ForEach-Object { $_.FullName })
javac --release 17 -encoding UTF-8 -Xlint:all -d output/classes $sources
java -cp 'output/classes;src' app.App
```

`src` trong classpath cung cấp ảnh và font. Không cần SQL Server hoặc thư viện ngoài JDK để chạy bản hiện tại.

Sau khi đổi branch hoặc cập nhật package từ bên ngoài Eclipse, chọn đúng project và nhấn **F5 (Refresh)**,
rồi **Project → Clean…** cho riêng project này nếu dấu lỗi cũ vẫn còn. Đóng các tab đang trỏ tới đường dẫn
Entity/DAO cũ và mở lại từ package mới. Không lưu nội dung tab cũ vào đường dẫn đã chuyển.

## Chức năng hiện có

| Vai trò | Nội dung |
| --- | --- |
| Quản lý | Trang chủ điều hành; Nhân viên; Tài khoản/phân quyền; Dịch vụ/bảng giá; Phụ tùng/kho; báo cáo doanh thu, tiếp nhận/sửa chữa, tồn kho và hiệu suất kỹ thuật viên. |
| Tiếp nhận | Hồ sơ khách/xe; tiếp nhận và phân công kiểm tra; báo giá, phản hồi khách; phát hành lệnh và bàn giao xe. |
| Kỹ thuật | Công việc được giao; báo cáo kiểm tra; tiến độ; phụ tùng thực dùng; phát sinh và xác nhận hoàn tất. |
| Thu ngân | Phiếu hoàn thành chờ thu; đối chiếu chi phí; xác nhận tiền mặt/chuyển khoản; tra cứu, xem chi tiết và in lại hóa đơn đã thanh toán. |

Trang chủ và Báo cáo Quản lý đã được triển khai theo yêu cầu ngày 02/11/2026. Đã có đối chiếu chi phí, thu tiền mặt/chuyển khoản thủ công, xem và in lại hóa đơn. Tài khoản ngân hàng/QR thật chưa được cấu hình; chưa tích hợp ngân hàng. Các màn xem trước cũ nằm trong package vai trò tương ứng không đồng nghĩa đã được nối vào luồng chính.

## Cấu trúc code

```text
src/
  app/                     Điểm chạy App
  entity/<nhom>/           Thực thể và trạng thái theo nhóm nghiệp vụ
  dao/<nhom>/              DAO của nhóm nghiệp vụ tương ứng
  ui/
    LoginFrame.java        Đăng nhập
    MainFrame.java         Khung chung và điều hướng
    component/             Thành phần dùng lại; UiChung, UiTheme
    screen/
      quanly/
      tiepnhan/
      kythuat/
      thungan/
    dialog/
      quanly/
      tiepnhan/
      kythuat/
  module-info.java
```

Entity và DAO chia các package cùng tên theo thực thể, dùng chung cho mọi vai trò:

| Nhóm package | Nội dung |
| --- | --- |
| `nhansu` | Nhân viên, tài khoản, vai trò, nhật ký tài khoản và liên kết phân công. |
| `khachhang` | Khách hàng và xe thuộc khách hàng. |
| `tiepnhan` | Phiếu tiếp nhận và trạng thái tiếp nhận. |
| `kiemtra` | Báo cáo kiểm tra xe và trạng thái kiểm tra. |
| `dichvu` | Danh mục dịch vụ và bảng giá. |
| `baogia` | Báo giá, các dòng chi tiết và trạng thái phản hồi. |
| `suachua` | Lệnh sửa chữa, hạng mục, phát sinh và xử lý công việc kỹ thuật. |
| `phutung` | Phụ tùng, tồn kho, phiếu nhập/chi tiết, biến động và thực dùng. |
| `hoadon` | Hóa đơn, phiếu chờ thu và hình thức thanh toán. |
| `bangiao` | Bàn giao xe, căn cứ và trạng thái bàn giao. |
| `baocao` | Báo cáo, bản lưu và tổng hợp dữ liệu trang chủ. |
| `dao.chung` | `DuLieuGarageDAO` kết nối các nguồn dữ liệu của toàn ứng dụng. |

Ví dụ: `entity.khachhang.KhachHang` và `dao.khachhang.KhachHangDAO`. UI vẫn chia theo vai trò;
Entity/DAO không chia theo Quản lý/Tiếp nhận/Kỹ thuật/Thu ngân. Không thêm tầng nghiệp vụ mới.

- Entity giữ thuộc tính private, constructor, getter/setter cần dùng và kiểm tra giá trị nội tại.
- DAO dùng ArrayList/mảng và vòng lặp để đọc, thêm, cập nhật, xóa có điều kiện, tìm, lọc, sắp xếp và kiểm tra quyền.
- UI đọc đầu vào, gọi DAO và hiển thị. Callback UI dùng ActionListener, không dùng java.util.function. Không có tầng service/controller hoặc khung CRUD tổng quát.
- `UiChung` giữ định dạng bảng, form, số và vùng cuộn dùng chung. Bảng danh sách chỉ đọc, khóa kéo cột; các bảng phân công có ô nhập được phép theo nghiệp vụ.
- `.editorconfig` và `.settings/org.eclipse.jdt.core.prefs` thống nhất UTF-8, 4 space, mục tiêu 120 ký tự/dòng. Comment constructor/hàm ngắn gọn, UI có chú thích nhóm thành phần.

## Luồng dữ liệu dùng chung

Đợt rà soát lỗi, package và luồng tiếp nhận → thanh toán nằm trong [REVIEW_LUONG_NGHIEP_VU.md](docs/REVIEW_LUONG_NGHIEP_VU.md). Phần duyệt phát sinh chưa hoàn thiện và được giữ lại để làm sau theo yêu cầu.

Một `DuLieuGarageDAO` được tạo khi mở ứng dụng và truyền qua các lần đăng xuất/đăng nhập. Tồn kho, khách/xe, phiếu, báo giá và hóa đơn cùng thuộc nguồn này; không tạo bộ hóa đơn hoặc tồn riêng cho mỗi vai trò.

1. Tiếp nhận tạo khách/xe, xác nhận phiếu và giao kiểm tra cho tài khoản Kỹ thuật.
2. Kỹ thuật hoàn tất báo cáo kiểm tra; Tiếp nhận lập báo giá, ghi nhận quyết định khách và phát hành lệnh.
3. Kỹ thuật cập nhật công việc, ghi nhận phụ tùng thực dùng và hoàn tất lệnh.
4. `HoaDonDAO` đọc lệnh đã hoàn thành, lấy tổng từ báo giá liên kết. Thu ngân chọn đúng phiếu, đối chiếu tiền rồi xác nhận; không thu thiếu hoặc lập lại hóa đơn.
5. `BanGiaoXeDAO` đọc căn cứ từ chính hóa đơn đã thanh toán. **Tiếp nhận** xác nhận bàn giao; Thu ngân không có quyền giao xe.

Tiền thừa không cộng vào doanh thu. Hóa đơn mới hiển thị trước. Đăng xuất làm phiên cũ hết hiệu lực; dữ liệu đã lưu vẫn còn khi đăng nhập lại trong cùng ứng dụng.

Khi khởi động luồng chung, danh mục/nhân viên/tài khoản đã có dữ liệu; khách/xe và chứng từ mới bắt đầu rỗng. Một số constructor DAO mặc định vẫn giữ dữ liệu tạo sẵn để xem thử từng màn độc lập. Đóng ứng dụng sẽ mất dữ liệu thay đổi; chưa lưu file/database.

## Dịch vụ, giá và tồn kho

- Báo giá mới bắt buộc chọn dịch vụ từ danh mục đang áp dụng; dòng lưu mã dịch vụ. Phụ tùng lưu mã riêng, không suy ra liên kết từ tên.
- Tên, đơn vị, giá trong chứng từ là bản chụp. Thay danh mục không tự đổi báo giá/hóa đơn cũ. Mở sửa dòng giữ giá đã nhập; đổi lựa chọn danh mục mới điền thông tin hiện tại.
- Chỉ xóa dịch vụ chưa có trong nguồn báo giá dùng chung. Dịch vụ đã dùng có thể tạm dừng. Màn Quản lý sử dụng cùng nguồn kiểm tra liên kết với Tiếp nhận.
- Không xóa vật lý nhân viên/tài khoản/phụ tùng/chứng từ đã ghi nhận. Dùng trạng thái nghỉ, khóa, ngừng áp dụng theo quyết định nghiệp vụ.
- Nhập kho và lịch sử nằm trong Phụ tùng. Một lần nhập có nhiều mặt hàng; nguồn hàng/diễn giải bắt buộc, mã chứng từ tùy chọn. Chỉ cộng tồn khi xác nhận thành công.
- Mã và đơn vị phụ tùng cố định sau tạo. Giá nhập trên chứng từ không tự sửa giá danh mục. Ngừng hoạt động chặn chọn cho báo giá/nhập mới, vẫn cho hoàn tất lệnh đã được duyệt.
- Chỉ trừ kho khi xác nhận lượng thực dùng; kiểm tra toàn bộ nhóm trước khi ghi, không âm kho, không ghi lặp. Đối soát phải có lý do và lịch sử.

## Java cơ bản và kiểu số

- Tiền `long`, nguyên đồng. Lượng `double`: lít tối đa 3 số lẻ; cái/bộ phải nguyên.
- Thành tiền từng dòng làm tròn đến đồng gần nhất, từ 0,5 lên. Tổng cộng các dòng đã làm tròn. Nhân tiền bằng số nguyên để giữ đúng giá lớn và kiểm tra tràn số.
- Nhập kiểu Việt Nam: số lít `1,25`, tiền `150.000`. Mã xử lý nhập liệu nằm ở UI, thực thể nhận số đã chuyển kiểu.
- Source ứng dụng không dùng BigDecimal, Optional, Stream, lambda/method reference, Map, java.util.function, synchronized/volatile hoặc worker. Các phương thức Swing/định dạng ngày và số thông thường vẫn được dùng.
- Mã thao tác dùng bộ đếm `long` trong `DuLieuGarageDAO`, không UUID; giữ mã trong cùng lần xác nhận để DAO chống lưu lặp.
- Không viết lại cơ chế EDT của Swing. `invokeLater(new Runnable() {...})` chỉ để khởi tạo/cập nhật giao diện, không chạy nghiệp vụ nền.

## Kiểm tra

Biên dịch source bằng Java 17 với `-Xlint:all`. Các kiểm tra DAO nằm trong `test/dao`; mỗi lớp có `main`, không cần JUnit:

```powershell
$tests = @(Get-ChildItem test/dao -Recurse -Filter '*.java' | ForEach-Object { $_.FullName })
javac --release 17 -encoding UTF-8 -cp output/classes -d output/test-classes $tests
foreach ($testFile in (Get-ChildItem test/dao -Recurse -Filter '*Test.java')) {
    $packageLine = Select-String -LiteralPath $testFile.FullName -Pattern '^package ([\w.]+);'
    $testClass = $packageLine.Matches[0].Groups[1].Value + '.' + $testFile.BaseName
    java -cp 'output/classes;output/test-classes' $testClass
    if ($LASTEXITCODE -ne 0) { throw ('Thất bại: ' + $testFile.Name) }
}
```

Các bộ kiểm tra gồm Nhân viên, Tài khoản/quyền, Dịch vụ, Phụ tùng, kiểu số, Hóa đơn, Báo cáo và toàn luồng đến Bàn giao. Lần đổi package: các bộ kiểm tra DAO PASS; JDK 17 `-Xlint:all` và Eclipse compiler đều 0 lỗi. Eclipse compiler còn các cảnh báo cũ về biến/hàm UI chưa sử dụng, không tắt cảnh báo để che chúng. Chương trình kiểm tra Swing và ảnh mới nằm trong `output/package-refactor`; kiểm tra điều hướng bốn vai trò ở 1920×1234, 1366×768 và 960×600 đạt 199 kiểm tra.

`skills`, `output` và phần lớn file mới trong `test` hiện bị `.gitignore` bỏ qua. Kiểm tra danh sách file trước khi bàn giao; cấu hình ignore được giữ nguyên. Chưa kiểm thử kết nối SQL Server hoặc sử dụng nhiều người dùng đồng thời.

## Trang chủ và Báo cáo Quản lý

Các frame Page 3: `manager-overview`, `manager-reports`, `manager-report-workshop`, `manager-report-stock`, `manager-report-saved`.

- Đăng nhập Quản lý mở Tổng quan điều hành. Các ô chỉ số mở chi tiết; biểu đồ doanh thu 7 ngày có hover và bấm ngày để đối chiếu hóa đơn. Đường tắt mở các mục Quản lý hiện có.
- `BaoCaoDAO` đọc cùng `DuLieuGarageDAO` với các vai trò. Không tạo doanh thu hoặc số xe riêng để lấp chỗ trống; chưa có chứng từ thì báo cáo bằng 0. Đọc lại số liệu bằng Làm mới hoặc mở lại mục.
- Doanh thu dùng **ngày thanh toán**, gồm cả ngày đầu/cuối kỳ, tách tiền mặt/chuyển khoản. Tiền thừa không tính doanh thu. Quản lý chỉ đọc hóa đơn, không được gọi thao tác thu tiền.
- Tiếp nhận: lượt và xe riêng biệt theo ngày nhận; lệnh theo ngày phát hành; báo giá chờ theo ngày gửi và chỉ tính phiên bản hiện hành. Trạng thái là **hiện tại lúc lập** của nhóm chứng từ thuộc kỳ, không tái dựng trạng thái quá khứ. Các nhóm trong bảng có thể giao nhau, không cộng tất cả thành tổng số xe.
- Hiệu suất: lệnh phát hành trong kỳ; mỗi lệnh tính cho cả kỹ thuật chính và hỗ trợ, mỗi người một lượt. Tỷ lệ hoàn thành = lệnh đã hoàn thành / lệnh được giao; chưa giao lệnh thì hiển thị “—”. Tổng lệnh không nhân đôi theo số nhân viên.
- Tồn kho là **tồn hiện tại**, gồm toàn bộ mã, cả mã ngừng hoạt động; không suy tồn lịch sử từ bộ lọc ngày. Cảnh báo khi tồn **≤ mức tối thiểu**, thống nhất với danh mục Phụ tùng.
- Lưu báo cáo giữ bản chụp chỉ số, dòng kết quả, người và thời điểm lập. Tìm/lọc và mở bản đã lưu không tự tính lại. Bấm lưu lặp cùng kết quả không tạo nhiều bản; lưu lại sau lần thống kê mới tạo một bản mới. Bản lưu được giữ qua đăng xuất, mất khi tắt ứng dụng vì chưa có database/lưu file.
- Xuất **CSV UTF-8 có BOM**, dấu chấm phẩy, mở bằng Excel; có thông tin kỳ/người lập/thời điểm, chỉ số và toàn bộ dòng kết quả. Lưu/CSV dùng toàn bộ báo cáo, tìm trong bảng chỉ đổi các dòng đang nhìn thấy. Có chọn nơi lưu, hủy và xác nhận ghi đè. Không cần thư viện Excel; chưa xuất XLSX/PDF.
- `entity.baocao.BaoCao` giữ bản chụp; `dao.baocao.BaoCaoDAO` tính và lưu; `ui.screen.quanly.TrangChuQuanLyPanel`, `BaoCaoPanel`, `BaoCaoDaLuuPanel` hiển thị. `BaoCaoUi` và `BieuDoDoanhThuPanel` dùng Swing cơ bản. Không thêm service/controller.

## Nhánh phát triển

`feature/manager-dashboard-reports`: Trang chủ và Báo cáo Quản lý, phần đơn giản hóa Java và chia Entity/DAO theo nhóm thực thể. Các thay đổi từ nhánh `refactor/fix-errors-group-domain-packages` được giữ nguyên khi chuyển về nhánh bàn giao này.

## Tích hợp Thu ngân từ main

Đã đối chiếu thay đổi Thu ngân trên main đến `e4f1117` (đối chiếu chi phí, chuyển khoản, chi tiết/in hóa đơn). Giữ package theo nhóm thực thể và tiền long; không khôi phục nguồn ThuNganDAO độc lập.

- Chọn đúng phiếu chờ → đối chiếu chi phí từ báo giá liên kết → chọn tiền mặt hoặc chuyển khoản → xác nhận thành công → mở đúng mã hóa đơn vừa tạo.
- Chuyển khoản cần số tiền thực nhận hợp lệ và mã tham chiếu; biển số, mã giao dịch được giữ trên hóa đơn. Phiếu đã thu rời danh sách đối chiếu/chờ thu, không thu lặp.
- Xem/in lại đọc được chi tiết sau thanh toán và sau bàn giao; không dựa vào danh sách phiếu còn chờ thu. Quản lý và Bàn giao vẫn đọc cùng nguồn hóa đơn.
- Bản in chứa mã hóa đơn, khách/xe, người/thời điểm thu, hình thức/tham chiếu, từng dòng và tổng tiền. Có chia trang; chỉ gửi đến máy in khi người dùng chấp thuận hộp thoại in. Kiểm tra bản in bằng ảnh và Printable, chưa in thử lên máy in vật lý.
