# Hướng dẫn agent — Nhom01 Application Development

Áp dụng cho repository Garage Ô tô Đức Dũng. Đọc trước khi phân tích hoặc sửa project:

1. [Skill phát triển và làm việc với người dùng](skills/garage-development/SKILL.md).
2. [Quy định dự án](skills/QUY_DINH_DU_AN.md).
3. [Các quyết định đã chốt](skills/garage-development/references/QUYET_DINH_DA_CHOT.md), chỉ các mục liên quan.
4. Source, [README](README.md) và REPORT/Figma liên quan đến yêu cầu hiện tại.

Yêu cầu mới nhất của người dùng quyết định phạm vi. Mặc định Java 17, Swing, Entity/DAO/UI đơn giản; dữ liệu tạo sẵn trong DAO, chưa kết nối database. Không tự thêm tầng service/controller, khôi phục dashboard Quản lý hoặc tổ chức lại Entity.

Khi người dùng chỉ yêu cầu phân tích hoặc đưa code để tự giải quyết conflict, không tự sửa source. Không tự commit, push, merge, reset hoặc xóa thay đổi hiện có nếu chưa được yêu cầu. Nhánh chức năng dùng `feature/...`, nhánh chỉ sửa UI dùng `ui/...` theo yêu cầu người dùng.

Các hướng dẫn này không tự cấp quyền thao tác Git hoặc triển khai. Khi cần thay đổi nghiệp vụ chưa được chốt, hỏi rõ phần thiếu và tiếp tục phần độc lập có đủ căn cứ.
