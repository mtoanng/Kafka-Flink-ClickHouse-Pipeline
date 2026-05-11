# AI Usage Log

> Theo yêu cầu §5 trong [`JAVA_FINAL_PROJECT_REQUIREMENT.md`](../JAVA_FINAL_PROJECT_REQUIREMENT.md), file này ghi lại các prompt AI đã dùng + cách verify output.

## Format

```
### [Date] - [Phase/Module] - [Task ngắn]
**Prompt:** (nội dung prompt gửi AI)
**Tool:** Cursor / Claude / Copilot / ChatGPT
**Output:** (tóm tắt 1-2 dòng AI trả về cái gì)
**Verify:** (cách mình kiểm chứng output đúng — chạy test, đọc lại, so sánh doc, v.v.)
**Edit/Adjust:** (mình đã chỉnh sửa gì sau khi nhận output)
```

---

## Entries

### 2026-05-11 — Phase 0 — Repo restructure & planning
**Prompt:** "Đọc và phân tích toàn bộ code của project để hiểu" + "Soạn kế hoạch comprehensive chi tiết để nâng cấp đề tài..."
**Tool:** Cursor (Claude Opus 4.7)
**Output:** AI tạo `UPGRADE_PLAN.md` ~2700 dòng với 24 section, bao gồm Section 24 — Minimalist Execution Plan với 8 phase atomic + smoke test + rollback strategy.
**Verify:** Đọc qua từng section, đối chiếu với code có sẵn (Project/KafkaConsumer + KafkaProducer), confirm tên bảng (`fuel_prices_raw`, `fuel_price_window_agg`, `fuel_price_alerts`) khớp với SQL init script.
**Edit/Adjust:** Re-design Section 22 từ 5 thành viên → 2 thành viên + Leader theo yêu cầu thực tế. Thêm Section 24 phương án Minimalist sau khi user request "đảm bảo đồ án không quá phức tạp".

### 2026-05-11 — Phase 0 — Git restructure
**Prompt:** "act as senior software engineer and senior data engineer, start implementing. after each phase, carefully do test..."
**Tool:** Cursor agent
**Output:** AI thực hiện `git mv` các folder từ `Project/` sang cấu trúc mới (`data-generators/`, `flink-jobs/`, `infra/`, `docs/`), tạo parent `pom.xml` multi-module, `.env.example`, `.gitignore`, `scripts/run.sh|ps1`.
**Verify:** `git status --short` cho thấy 100% file là rename (R) chứ không phải delete+add → history Git được giữ. Tag `v0.0-start-minimalist` đặt làm điểm rollback.
**Edit/Adjust:** Chưa, đang trong tiến trình Phase 0.

---

*(Tiếp tục thêm entry mỗi phase / mỗi prompt quan trọng)*
