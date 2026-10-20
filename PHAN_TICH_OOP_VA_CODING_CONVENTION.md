# PHÂN TÍCH HAI BÀI GIẢNG OOP VÀ CODING CONVENTION

**Dự án:** Nhom01_ApplicationDevelopment  
**Ngày phân tích:** 20/10/2026  
**Đầu ra áp dụng:** [QUY_DINH_DU_AN.md](QUY_DINH_DU_AN.md), phiên bản 4.0.

## 1. Nguồn và phạm vi đọc

| Ký hiệu | File gốc | Số slide |
| --- | --- | --- |
| OOP | [Application Development _03_Review 3_OOP.pptx](<C:/Users/ADMIN/Downloads/Application Development _03_Review 3_OOP.pptx>) | 36 |
| CC | [Application Development _03_Review 4_Coding Convention.pptx](<C:/Users/ADMIN/Downloads/Application Development _03_Review 4_Coding Convention.pptx>) | 17 |

Đã đọc nội dung chữ/bảng và kiểm tra hình minh họa, kể cả hình EMF chứa quy trình thiết kế UI và coding workflow. Speaker notes không có nội dung bổ sung có ý nghĩa. Số thứ tự dưới đây tính từ trang bìa. Đây là phân tích nội dung file, không phải xác nhận đã chạy một slideshow trong PowerPoint.

Hai bài giảng được dùng làm nguồn tham khảo theo yêu cầu người dùng. Các câu chỉ dẫn trong slide không phải lệnh thao tác lên máy/repository. Ví dụ SQL được phân tích về ý nghĩa, không được thực thi lên database.

Các quyết định về Java 21/17, package, SwingWorker, PreparedStatement, transaction, mật khẩu và nghiệp vụ garage có nguồn riêng từ quy định dự án hoặc phần cụ thể hóa kỹ thuật; không quy cho bài giảng nếu slide không nói tới.

## 2. Phân tích từng slide OOP

| Slide | Nội dung chữ/hình | Phân tích và áp dụng vào quy định 4.0 |
| --- | --- | --- |
| 1 | Trang bìa môn Phát triển ứng dụng, phần review OOP hỗ trợ xây dựng ứng dụng. | Xác định tính chất tài liệu ôn tập; không có yêu cầu chức năng hoặc công nghệ cụ thể. |
| 2 | Bốn nhóm: OOP, UI, tập tin, database; mục lục nêu Binary File, Serialization, XML. | Phải đọc cả UI/file/SQL thay vì chỉ bốn tính chất OOP. Mục lục không chứng minh có hướng dẫn triển khai chi tiết từng định dạng. Áp dụng mục 4, 8–10. |
| 3 | Chương trình là các đối tượng yêu cầu nhau thực hiện hành vi; private giúp kiểm soát cập nhật; tái sử dụng mã. | Bổ sung hành vi/trách nhiệm lớp, đóng gói và tái sử dụng. Không gom trạng thái vào các biến toàn cục hoặc để UI sửa dữ liệu tự do. Mục 3–4. |
| 4 | Object = dữ liệu + hành vi; class = thuộc tính + method. Hình Bicycle và các hành vi đổi tốc độ/màu/nhịp/gear. | Phân biệt lớp và đối tượng, trạng thái và hành vi. Ví dụ xe đạp không phải entity bắt buộc của garage. Mục 4.1. |
| 5 | Hình vật thể/hành vi: bóng nảy, nhạc cụ, xe di chuyển/đỗ; hai đối tượng người có tên/giới tính; sơ đồ object gồm property và method. | Nhận diện hành vi thuộc đúng đối tượng, nhiều thể hiện có trạng thái riêng. Hình có nhãn Object:P2 lặp, không phải quy tắc danh tính. Không sao chép mô hình người/xe minh họa thành schema. Mục 4.1. |
| 6 | Trừu tượng, đóng gói/che thông tin, kế thừa, đa hình; lợi ích tái sử dụng, phù hợp bài toán lớn/phức tạp. | Bổ sung bốn tính chất và điều kiện áp dụng. Câu thay đổi dữ liệu không cần đổi mã khác là lợi ích khi hợp đồng được giữ, không bảo đảm cho mọi thay đổi API. Mục 4. |
| 7 | Từ bài toán chọn danh từ làm properties, động từ làm operations, gom dữ liệu/hành vi vào class rồi sử dụng object. | Thêm bước phân tích lớp trước code; xem danh từ/động từ là gợi ý, phải kiểm trách nhiệm và quan hệ. Không sao chép giả mã trong hình thành Java nguyên trạng. Mục 4.1, 12. |
| 8 | Nhắc lại trừu tượng hóa, kế thừa, đóng gói, đa hình. | Chuyển từ tên khái niệm thành tiêu chí review: private, hợp đồng, quan hệ là-một/có-nhiều, override hợp lý. Các chi tiết Java là cụ thể hóa của nhóm. Mục 4. |
| 9 | UI dễ học, quen thuộc, nhất quán, ít gây bất ngờ. | Dùng thuật ngữ garage, nhãn rõ; không âm thầm thay đổi dữ liệu khi tra cứu; cùng thao tác cùng cách kích hoạt. Mục 10.1. |
| 10 | Khôi phục sau lỗi, hướng dẫn theo ngữ cảnh, phục vụ người dùng đa dạng. | Giữ dữ liệu nhập khi lỗi, thông báo cách sửa, hỗ trợ vai trò/bàn phím/font dễ đọc. Mục 10. |
| 11 | Năm kiểu tương tác: trực tiếp, menu, form, dòng lệnh, ngôn ngữ tự nhiên. | Phân loại phương án, không yêu cầu ứng dụng có cả năm. Swing chọn form/menu/bảng và thao tác phù hợp. Mục 10.1. |
| 12 | Sáu loại màn hình: chính, nhập dữ liệu, nhập liệu xử lý, kết quả, thông báo, tra cứu; bảng nêu mục đích và nội dung. | Phân biệt nơi chọn việc, nơi cấp dữ liệu để xử lý và nơi trình bày kết quả; không buộc mỗi loại tương ứng một JFrame riêng. Mục 10.1–10.2. |
| 13 | UI trực quan, thân thiện, dùng ngôn ngữ người dùng; dễ chuyển màn hình, một công việc/màn hình, tránh ghép hai việc. | Mỗi màn hình một mục tiêu chính; bảng và form của cùng việc có thể chung. Hạn chế cuộn/đi nhiều bước, không diễn giải thành cấm JScrollPane. Mục 10.1. |
| 14 | Hiệu quả: ít thao tác, nhanh, mặc định, phím tắt/icon; hạn chế sai bằng lựa chọn/nhắc; có undo. | Giá trị mặc định an toàn và lựa chọn hợp lệ; undo với chỉnh sửa chưa lưu. Tiền/kho đã commit cần quy trình điều chỉnh có quyền. Mục 10.1–10.2. |
| 15 | Nhất quán vị trí, ngôn ngữ, hình dáng, màu, kích hoạt; màu hài hòa/bố cục hợp lý. | Chuẩn UI chung, nhãn và vị trí nút thống nhất. Font/kích thước cụ thể là lựa chọn dự án, không lấy số từ slide. Mục 10. |
| 16 | Phím nóng Alt+?, phím tắt Ctrl+?, nhóm menu liên quan, biểu tượng trực quan. | Áp dụng mnemonic/accelerator/key binding, tránh trùng/ghi đè nhập liệu; icon có nhãn/tooltip. Mục 10.1. |
| 17 | Menu hướng chức năng; hình Hệ thống, Danh mục, Cập nhật, Xử lý, Tìm kiếm, Báo biểu của bài toán trường học. | Đây là một cách nhóm chức năng. Không tạo môn học/giáo viên/học phí trong garage. Mục 10.1. |
| 18 | Menu hướng đối tượng; hình Sinh viên, Giáo viên, Học phần, Phòng, Trường. | Một cách nhóm theo đối tượng và tác vụ liên quan. Không phải cấu trúc menu bắt buộc. Mục 10.1. |
| 19 | Menu hướng nghiệp vụ theo giai đoạn tổ chức/kế hoạch/tiếp nhận/hoạt động/tổng kết; hình minh họa trường học. | Garage ưu tiên quy trình nghiệp vụ và vai trò để điều hướng nhất quán. Không triển khai đồng thời ba cây menu trùng nội dung. Mục 10.1. |
| 20 | Chữ và sơ đồ: phân tích người dùng, mẫu giấy, đánh giá với người dùng, điều chỉnh mẫu, đánh giá lại, cài đặt. | Thêm quy trình mẫu thử có vòng phản hồi trước khi triển khai; sau triển khai vẫn kiểm UI thật. Sơ đồ hình đã được đọc riêng. Mục 10.1. |
| 21 | File văn bản, nhị phân, mẫu tin có cấu trúc, định dạng chuyên dụng, XML. | Chọn theo nhu cầu; UTF-8 cho văn bản, nhị phân không đồng nghĩa mã hóa. Nhận định về mua khóa cho mọi định dạng chuyên dụng cần giới hạn lại. Mục 9. |
| 22 | Nhu cầu lưu kết quả phiên, nhập lượng lớn, xuất lượng lớn. | Garage giữ nghiệp vụ chính ở SQL Server; file phục vụ cấu hình/nhập/xuất/báo cáo có yêu cầu cụ thể. Mục 9.1. |
| 23 | Các cấp độ đọc/ghi: byte thô, ký tự có encoding, kiểu cơ bản nhị phân, đối tượng và đồ thị tham chiếu ở binary/XML. | Chốt định dạng/thứ tự đọc ghi/encoding; serialization/XML không bắt buộc. Tài nguyên, kiểm tra nhập và parser an toàn là bổ sung dự án. Mục 9.2. |
| 24 | Giới thiệu DDL, DML, SQL, DCL. | Giữ phân biệt mục đích nhưng diễn đạt SQL là ngôn ngữ bao gồm nhiều nhóm lệnh. SELECT là truy vấn, DCL liên quan cấp/thu quyền. Mục 8.1. |
| 25 | CREATE/ALTER/DROP; thêm/sửa/xóa thuộc tính và ràng buộc. | Schema phải thay bằng script được quản lý; thay đổi có kiểm phụ thuộc và ảnh hưởng dữ liệu. Mục 8. |
| 26 | Mẫu CREATE TABLE, ALTER ADD và ALTER COLUMN. | Tên/kiểu/khóa theo schema được chốt; không sao chép cú pháp placeholder thành bảng thật. Mục 8. |
| 27 | DROP COLUMN; ADD CONSTRAINT với UNIQUE, PK, FK, CHECK; tùy chọn CASCADE. | Duy trì toàn vẹn tại DB; cascade chỉ theo nghiệp vụ, không mặc định cho chứng từ. Mục 8. |
| 28 | Xóa constraint/table, lưu ý FK tham chiếu. | Kiểm phụ thuộc trước xóa/sửa khóa; không thực thi ví dụ phá cấu trúc trên DB chung. Mục 8. |
| 29 | INSERT có/không liệt kê cột; UPDATE/DELETE có WHERE tùy chọn trong cú pháp. | Quy tắc dự án chặt hơn: INSERT liệt kê cột, sửa/xóa xác định bản ghi và kiểm số dòng. Giá trị từ PreparedStatement. Mục 7–8. |
| 30 | SELECT tổng quát, DISTINCT, WHERE/GROUP/HAVING/ORDER, JOIN, alias. | Không SELECT * ở truy vấn trả dữ liệu; alias rõ, lọc nhóm đúng, join tránh nhân dòng và không dùng DISTINCT che lỗi. Mục 8.1. |
| 31 | So sánh, logic, số học, BETWEEN, NULL, LIKE %/_, IN/NOT IN, EXISTS/NOT EXISTS, SOME/ALL. | Thêm tiêu chí chọn toán tử; xét NULL, hai đầu BETWEEN, wildcard, tập rỗng và điều kiện ghép. Không yêu cầu dùng đủ toán tử. Mục 8.1. |
| 32 | IN/EXISTS tìm hóa đơn mua cả BB01 và BB02; truy vấn con tương quan. | Hiểu “có cả hai” trên nhiều dòng chi tiết; không dùng điều kiện một dòng có hai mã khác nhau. Ví dụ không định nghĩa schema garage. Mục 8.1. |
| 33 | NOT IN/NOT EXISTS tìm có BB01, không BB02; có lỗi NOT EXITST và nháy cong. | Dùng cú pháp đúng, xét NULL của NOT IN và tương quan EXISTS. Không sao chép lỗi gõ vào mã. Mục 8.1. |
| 34 | Hai NOT EXISTS tìm hóa đơn đã mua tất cả sản phẩm xuất xứ Việt Nam. | Mẫu truy vấn “với mọi”; cần hiểu tập yêu cầu rỗng có thể làm điều kiện đúng với mọi hóa đơn. Không biến xuất xứ này thành yêu cầu garage. Mục 8.1. |
| 35 | COUNT, MIN, MAX, AVG, SUM. | Xét NULL, nhóm rỗng, kiểu số và join trùng; COUNT(*) khác COUNT(cot). Doanh thu phải theo điều kiện nghiệp vụ riêng. Mục 8.1, 11. |
| 36 | Hình dấu hỏi kết thúc. | Trang kết thúc/thảo luận, không có quy tắc kỹ thuật mới. |

## 3. Phân tích từng slide Coding Convention

| Slide | Nội dung chữ/hình | Phân tích và áp dụng vào quy định 4.0 |
| --- | --- | --- |
| 1 | Bìa phần quy tắc/cách viết code. | Xác định mục đích chuẩn hóa, không thêm yêu cầu chức năng. |
| 2 | Bảy nhóm: tổ chức file/thư mục, viết hoa, class/function, biến, comment, coding plan, review. | Tài liệu dự án cần bao quát cả kế hoạch/review, không chỉ đặt tên. Mục 3, 5, 12. |
| 3 | Thư mục mô tả mục đích, tránh gom file không liên quan, chữ thường, không space, có thể dùng _. | Áp dụng package theo trách nhiệm; giữ `connectDB` là ngoại lệ tương thích, không tự rename. Mục 3. |
| 4 | File đúng nơi, tên mô tả nội dung, không space/tiếng Việt có dấu, có thể dùng _. | File Java theo tên public class PascalCase; tài liệu/script có thể gạch dưới. Không đổi tên nguồn PPTX chỉ để ép convention. Mục 3. |
| 5 | Java/C# phân biệt hoa/thường; tên bắt đầu A–Z/a–z/$/…; tránh keyword. | Quy định nhóm chặt hơn cho tên tự đặt: chữ ASCII ở đầu, tránh $, không keyword/số đầu. Không coi danh sách slide là toàn bộ ngữ pháp định danh Java. Mục 3. |
| 6 | PascalCase, camelCase, UPPERCASE; lời khuyên độ dài từ ba/hai ký tự. | Giải thích dạng chữ, áp dụng theo loại thành phần Java; không viết hoa biến vì ngắn. Hằng/enum là UPPER_SNAKE_CASE theo nhóm. Mục 5.1. |
| 7 | Bảng ghi rõ ví dụ C#: interface tiền tố I, method/property/namespace PascalCase, private field/param camelCase. | Không áp bảng nguyên trạng cho Java; method Java camelCase, interface không buộc I. Mục 5.1. |
| 8 | Class danh từ, tên có mục đích, bắt đầu hoa; method động từ; phân biệt Java/C#; comment class/function/vào/ra. | Chuẩn hóa câu Java hơi mâu thuẫn bằng camelCase: ký tự đầu thường, từ sau viết hoa. Tên method/class không tùy tiện chèn _. JavaDoc mô tả hợp đồng. Mục 5. |
| 9 | Biến rõ nghĩa, chữ thường, không space, có thể _, có/không kiểu; comment mục đích. | Java dùng camelCase, có kiểu tĩnh; var là suy luận kiểu biến cục bộ, không biến Java thành ngôn ngữ động. Comment biến/nhóm biến có ý nghĩa. Mục 5. |
| 10 | Comment class/function/biến/điều kiện; // và /* */. | Bổ sung nghĩa vụ mô tả lớp, method, field, biến/nhóm biến và điều kiện nghiệp vụ; không chỉ comment khi tên chưa rõ. Dùng JavaDoc cho hợp đồng, tránh comment lặp câu lệnh. Mục 5.3. |
| 11 | Ví dụ `/// <summary>` và class Person. | Đây là kiểu documentation C#; chuyển sang `/** ... */`, @param/@return/@throws của Java, không sao chép nguyên văn. Mục 5.3. |
| 12 | Bảng tiền tố control C# như btn/txt/cbo/rdo và nhiều control đặc thù. | Chỉ là ví dụ nền tảng khác; không đưa DataSet/CrystalReport/DateTimePicker thành dependency Swing. Với Java chọn bảng slide 13. Mục 5.1. |
| 13 | Bảng Java Swing/AWT, gồm btn/chk/cmb/tbl/pnl/mni…; một số tên API viết sai và dòng tab bị ghép JTable. | Dùng cmb cho JComboBox, rad cho JRadioButton, tbl cho JTable, tab cho JTabbedPane; sửa tên API đúng Java. Prefix cho biến, không thay tên class. Mục 5.1. |
| 14 | Sơ đồ hình Coding Workflow, không có body text. | Đã đọc hình: Start → Plan → Code Library Modules → Code Functional Modules → hai nhánh Integrate Components/Create System Description → Code Functional Modules → Finish. Ô cuối vẫn ghi Code Functional Modules, không tự gán thành Test. Nhóm bổ sung kiểm thử/review/PR. Mục 12.1. |
| 15 | Ba cách review: chéo, nhóm, technical leader. | Chọn hình thức theo độ lớn/rủi ro; tối thiểu review độc lập và leader cuối theo quy trình nhóm. Không bắt ba cuộc họp mỗi PR. Mục 12.4. |
| 16 | Lợi ích: gọn/rõ/hiệu năng, phát hiện lỗi sớm, kỹ năng, đồng bộ, góp ý UI/design. | Reviewer kiểm cả hành vi/thiết kế/dữ liệu/UI/bằng chứng; góp ý cụ thể, không chỉ formatter. Mục 12.4–13. |
| 17 | Hình dấu hỏi kết thúc. | Trang thảo luận, không có quy tắc mới. |

## 4. Những điểm phải diễn giải thay vì chép nguyên slide

| Điểm trong nguồn | Cách xử lý |
| --- | --- |
| Có cả C# và Java | Chọn quy tắc Java cho mã dự án; tên/summary/control C# chỉ để đối chiếu. |
| Casing theo độ dài, biến viết thường | Chọn casing theo loại: class PascalCase, method/biến camelCase, hằng/enum UPPER_SNAKE_CASE. |
| Có/không khai báo kiểu | Java luôn có kiểu; var chỉ suy luận một số biến cục bộ. |
| Comment mọi thành phần | Chuyển thành JavaDoc/hợp đồng và comment ý nghĩa, cho phép comment nhóm biến; không giảm nghĩa vụ thành “chỉ khi tên khó hiểu”. |
| Thư mục chữ thường | Giữ ngoại lệ `connectDB`, không đổi cấu trúc dự án không có kế hoạch. |
| Tiền tố Swing có lỗi gõ | Sửa tên API; cmb/rad theo bảng Java; phân biệt tab/tbl. |
| Một việc/màn hình, tránh màn hình trôi | Giữ mục tiêu công việc, hạn chế chia bước/scroll dài; vẫn dùng vùng cuộn cần thiết. |
| Undo | Phục hồi tương tác và hủy sửa trước lưu; tài chính/kho đã commit cần nghiệp vụ điều chỉnh. |
| Định dạng chuyên dụng cần mua khóa | Không khái quát cho mọi file; tùy định dạng và công cụ, nhị phân cũng không tự là mã hóa. |
| SQL được liệt kê cạnh DDL/DML/DCL | Làm rõ SQL bao gồm các nhóm câu lệnh; SELECT là truy vấn. |
| UPDATE/DELETE không WHERE, INSERT không cột, SELECT * | Là ví dụ cú pháp tổng quát; quy tắc ứng dụng dùng điều kiện rõ/cột rõ/tham số và kiểm kết quả. |
| NOT EXITST, nháy cong | Nêu lỗi và cách sửa, không thực thi ví dụ chưa sửa. |
| Tên ô cuối Coding Workflow lặp | Ghi đúng hình gốc; kiểm thử/review là bổ sung của nhóm. |
| JDK, transaction, bảo mật Java/XML | Không được PPTX chỉ định đầy đủ; ghi là quyết định/cụ thể hóa dự án. |

## 5. Thay đổi từ quy định 3.3 sang 4.0

| Nội dung | Trước | Sau |
| --- | --- | --- |
| Nguồn | Tập trung REPORT và dự án. | Nêu hai bài giảng, số slide, cách áp dụng, tách quy tắc DA và giới hạn kiểm tra REPORT V3. |
| OOP | Chủ yếu private và chia package/entity. | Có mục riêng về lớp/đối tượng, bốn tính chất, là-một/có-nhiều, hợp đồng, quan hệ và tái sử dụng có lý do. |
| Comment | JavaDoc khi tên chưa đủ rõ. | JavaDoc lớp/method và hợp đồng; comment field, biến/nhóm biến, điều kiện nghiệp vụ; phân biệt C#. |
| Control | Ví dụ cboTrangThai. | cmbTrangThai theo bảng Java; bảng tiền tố đầy đủ và chính sách không đổi mã cũ hàng loạt. |
| UI | Tập trung layout, EDT, trạng thái. | Thêm usability, sáu loại màn hình, kiểu tương tác, ba cách nhóm menu, phím tắt và vòng mẫu thử. |
| File | Chưa có mục riêng. | Có tổng quan byte/ký tự/kiểu cơ bản/đối tượng, phạm vi file, quy tắc nhập/xuất và giới hạn serialization/XML. |
| SQL | Tập trung an toàn và quản lý schema. | Thêm DDL/DML/DCL, join/lọc/nhóm, toán tử/NULL, subquery và tổng hợp theo slide. |
| Plan/review | Có phối hợp/PR. | Đối chiếu đúng sơ đồ workflow, ba cách review và mục tiêu review. |
| Hiện trạng | Một số câu coi UI/theme/test đã tồn tại. | Nêu cấu trúc là chuẩn dự kiến; bỏ dẫn chiếu test chưa có; không xác nhận App/MainFrame/UiTheme hiện đã triển khai. |
| Môi trường | JDK 21 cố định. | Lần rà soát cuối thấy cấu hình workspace đã đổi sang 17; quy định dùng Java 17 và nêu phần JDK/runtime/README cần kiểm tra. Không tự sửa file Eclipse. |

## 6. Giữ nguyên và điểm cần đồng bộ sau này

- Giữ cấu trúc `app`, `ui`, `dao`, `entity`, `connectDB`, các package con UI; không tự thêm service hoặc đổi namespace theo hướng dẫn AI cũ.
- Giữ nguyên tắc Swing EDT/SwingWorker, PreparedStatement, tài nguyên JDBC, BigDecimal, transaction và chống xử lý lặp. Đây là quy định dự án, không gán sai nguồn cho slide.
- Giữ nội dung nghiệp vụ từ 3.3, gồm thu ngân xác nhận tiền/tiếp nhận bàn giao. Chưa đọc lại toàn văn REPORT V3 nên không tuyên bố đã xác minh lại các điều kiện/mã use case.
| Cấu hình kiểm tra cuối | Giá trị |
| --- | --- |
| `.classpath` | JRE System Library: JavaSE-17 |
| `.settings/org.eclipse.jdt.core.prefs` | source/compliance/targetPlatform: 17; preview disabled |

Các file trên thay đổi trong workspace trong lúc phân tích; công việc này chỉ đọc và cập nhật nội dung quy định theo hiện trạng mới. Chưa xác nhận cài đặt JDK 17 hoặc build/chạy thành công. README còn ghi JDK 21 cần được đồng bộ khi cập nhật hướng dẫn chạy.
- `skills/AGENTS.md` và skill hỗ trợ cũ có mô tả service/package khác quy định chính. Bản 4.0 nêu rõ nguồn chính; lần này chưa sửa bộ hướng dẫn AI.
- `.gitignore` bỏ `/skills`; hai tài liệu được sửa/tạo cục bộ, chưa tự đưa vào Git. README còn mô tả một số UI chưa có; cần cập nhật khi bàn giao mã thực tế.
- Không đổi code, schema, cấu hình Eclipse, commit/push hoặc thực thi SQL trong công việc phân tích/viết quy định này.
