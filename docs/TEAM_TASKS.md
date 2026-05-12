# 👥 TEAM TASKS — Phân công chi tiết step-by-step cho 4 thành viên

> File này thay thế `UPGRADE_PLAN.md §22.3` (vốn chỉ 2 thành viên). Hiện tại team **5 người**: Leader + 2 coders đơn giản + 2 non-coders. Mỗi thành viên có **5 PR thật** + step-by-step để **người mới vào nghề / non-coder** vẫn làm được mà không hỏi Leader nhiều.

---

## 🎯 Sơ đồ team & workload

| Vai trò | Người | Skill cần | Workload | PR | Loại task |
|---------|-------|-----------|----------|-----|-----------|
| **Leader / Full-stack** | A | Tất cả | ~75-85h | 30-40 PR | Backend + Flink + Core UI + Deploy + Integration |
| **Doc & PM** | **B** | Word, draw.io, viết báo cáo | ~30h | **5 PR** | Báo cáo + diagram + project management |
| **Media & QA** | **C** | PowerPoint, Screen Recorder, Postman | ~25h | **5 PR** | Slide + video + test report + screenshots |
| **JavaFX UI Junior** | **D** | Java cơ bản, IntelliJ | ~20h | **5 PR** | 2 màn JavaFX phụ + JUnit test + CSS polish + SQL seed |
| **Android UI Junior** | **E** | Java/Kotlin cơ bản, Android Studio | ~20h | **5 PR** | 2 activity Android phụ + drawable + strings i18n + Postman |

**Tổng PR 4 thành viên: 20 PR**. Đủ chứng minh đóng góp trong báo cáo Chương 6 + GitHub history.

---

## 🛠️ Tools cần cài đặt — tổng hợp cho cả team

| Tool | Bắt buộc cho ai | Link tải | Ghi chú |
|------|------------------|----------|---------|
| **Git** | Tất cả | https://git-scm.com/downloads | Cấu hình `git config --global user.name`, `user.email` |
| **GitHub Desktop** | B, C (nếu sợ command line) | https://desktop.github.com/ | GUI thay cho terminal git |
| **VS Code** | Tất cả | https://code.visualstudio.com/ | Đọc code + edit markdown |
| **IntelliJ IDEA Community** | D | https://www.jetbrains.com/idea/download/ | Free, đủ cho JavaFX |
| **Android Studio** | E | https://developer.android.com/studio | Bắt buộc cho Android dev |
| **draw.io desktop** | B | https://www.drawio.com/ | Hoặc dùng online tại app.diagrams.net |
| **Microsoft Word + PowerPoint** | B, C | (trường có Office 365) | Hoặc LibreOffice / Google Docs |
| **OBS Studio** | C | https://obsproject.com/ | Quay video demo |
| **Postman** | C, E | https://www.postman.com/downloads/ | Test API |
| **DBeaver Community** | D, E | https://dbeaver.io/download/ | GUI cho Postgres khi cần peek data (optional) |

---

## 🌿 Git workflow chung — TẤT CẢ thành viên phải làm theo

> **Không bao giờ commit thẳng vào `main`.** Mọi thay đổi đi qua **branch riêng + Pull Request + Leader review + merge**.

### Lần đầu: clone repo

```bash
git clone https://github.com/<your-org>/Real-time-processing-with-Kafka-Flink-Postgres.git
cd Real-time-processing-with-Kafka-Flink-Postgres
git checkout main
git pull
```

### Mỗi task = 1 branch + 1 PR

```bash
# 1. Sync main mới nhất (LUÔN làm trước khi tạo branch mới)
git checkout main
git pull origin main

# 2. Tạo branch theo convention: <persona>/<id>-<short-desc>
#    Ví dụ: b/B1-draw-erd, c/C2-record-demo-video, d/D3-javafx-about-screen, e/E1-android-icons
git checkout -b b/B1-draw-erd

# 3. Làm việc, commit thường xuyên (commit nhỏ tốt hơn 1 commit khổng lồ)
git add docs/erd.png docs/usecase.png
git commit -m "B1: add ERD + Use-case diagrams"

# 4. Push lên GitHub
git push -u origin b/B1-draw-erd

# 5. Mở browser → vào https://github.com/.../pulls → "Compare & pull request"
#    Title PR: theo format: "B1: ERD + Use-case + Architecture diagrams"
#    Body PR: paste lại checklist từ task description trong file này
#    Reviewer: tag @leader-username
#    → Tạo PR

# 6. Sau khi Leader approve & merge: xoá branch
git checkout main
git pull origin main
git branch -d b/B1-draw-erd
```

### PR title convention

`<TaskID>: <short description>` — ví dụ:
- `B2: Báo cáo Chương 1 — Tổng quan đề tài`
- `D3: JavaFX Settings screen với toggle dark mode`
- `E5: Postman Collection import từ Swagger`

### PR description template (paste vào ô Description khi tạo PR)

```markdown
## Task
<task ID + title>

## Checklist
- [x] <step 1>
- [x] <step 2>
- [ ] <step chưa xong>

## Deliverables
- `docs/erd.png` (1.2 MB)
- `docs/usecase.png` (800 KB)

## Cách verify
<viết 2-3 dòng Leader có thể click vào đâu / chạy lệnh gì để kiểm tra>

## Screenshot (nếu có)
<paste ảnh>
```

### Khi PR bị conflict

```bash
git checkout main
git pull origin main
git checkout <your-branch>
git merge main
# Mở các file có dấu <<<<<<<<, sửa thủ công
git add <file>
git commit -m "Resolve conflict with main"
git push
```

Nếu không biết sửa conflict → **không tự sửa**, comment vào PR @leader để Leader giúp.

---

## 🆘 Khi bí — quy tắc "2-giờ"

1. **Tự thử 30 phút** — Google / Stack Overflow / ChatGPT.
2. **Hỏi Cursor AI / Copilot** trong IDE — đặt câu hỏi cụ thể, paste error message.
3. **Sau 2 giờ vẫn không xong** → comment vào GitHub Issue của task `@leader help: <mô tả ngắn>`. Leader sẽ trả lời trong vòng 24h.
4. **TUYỆT ĐỐI KHÔNG**:
   - Commit code sai vào `main`
   - Force push (`git push -f`)
   - Xoá file của người khác
   - Sửa file ngoài scope task của mình

---

# 👤 NGƯỜI B — Doc & Project Manager (5 PR, ~30h, non-coding)

> **Định vị:** Không cần biết code. Chỉ cần Word, draw.io, viết tốt tiếng Việt. Đầu ra là **báo cáo PDF + diagram + project management artifacts** — phần lớn điểm số môn Java đến từ những thứ này (Chương 1, 2, 4, 6 + diagram + AI Usage Log).

## 📋 Tổng quan 5 PR của Người B

| PR | Task | Time | Phase phụ thuộc | Có thể bắt đầu khi |
|----|------|------|-----------------|---------------------|
| B1 | Vẽ ERD + Use-case + Activity diagram | 6h | Phase 2 done ✅ | NGAY |
| B2 | Báo cáo Chương 1 — Tổng quan đề tài | 5h | - | NGAY |
| B3 | Báo cáo Chương 2 — Phân tích thiết kế HTTT | 7h | B1 done | Sau B1 |
| B4 | Báo cáo Chương 4 — Quản lý dự án + Charter + SOW | 6h | - | NGAY |
| B5 | Báo cáo Chương 6 + AI Usage Log + Notion page | 6h | Phase 4 done ✅ | NGAY |

---

## 📝 B1. Vẽ ERD + Use-case + Activity diagram (6h, NGAY làm được)

### 🎯 Mục tiêu
Vẽ 3 diagram để dùng cho Chương 2 báo cáo + slide PowerPoint:
- **ERD** (Entity-Relationship Diagram) cho database
- **Use-case diagram** cho 3 actor (Admin, Manager, Viewer)
- **Activity diagram** cho luồng nghiệp vụ chính (Đăng nhập → Xem dashboard → Acknowledge recommendation)

### ✅ Prerequisites
- Đã cài draw.io desktop (hoặc dùng online tại https://app.diagrams.net)
- Đã clone repo
- Mở các file `infra/script/01_init_fuel_schema.sql`, `02_init_users_regions.sql`, `03_init_alerts.sql`, `05_init_pillars.sql`, `07_init_security_features.sql` để hiểu schema

### 📐 Step-by-step

#### Phần 1: Vẽ ERD (2h)

1. Mở draw.io → **File → New → Blank Diagram** → đặt tên `erd.drawio`
2. Bên trái sidebar có template "Entity Relation", kéo các **Entity** boxes ra. Mỗi entity là 1 bảng (tên cột + kiểu — đọc trong file SQL tương ứng):
   - `users` (id, username, password_hash, full_name, email, role, enabled)
   - `regions` (code [PK], name, country, latitude, longitude)
   - `alert_rules` (id, rule_name, metric_type, fuel_type, region_code, threshold, operator, severity, enabled)
   - `alerts` (id, rule_id [FK], metric_type, fuel_type, region, triggered_price, threshold, severity, message, event_timestamp, alert_timestamp)
   - `fuel_prices_raw` (id, event_timestamp, fuel_type, price, price_unit, location, region, source)
   - `fuel_inventory_raw` (id, region_code [FK], fuel_type, stock_volume_kl, daily_consumption_kl, stock_days [generated], target_days, reported_at)
   - `grid_load_raw` (id, region_code [FK], load_mw, capacity_mw, load_pct [generated], is_peak_hour, event_time)
   - `renewable_output_raw` (id, region_code [FK], source_type, output_mw, capacity_mw, event_time)
   - `emission_raw` (id, region_code [FK], co2_kg, energy_mwh, intensity_kg_per_mwh [generated], event_time)
   - `recommendations` (id, pillar, action_type, severity, title, message, suggested_data, status, suggested_at, acknowledged_at, acknowledged_by [FK to users.id], note, expires_at)
   - `daily_briefings` (id, briefing_date, security_score, key_risks, pillar1_summary, ...)

3. **Vẽ relationship (đường nối có ký hiệu cardinality)**:
   - `alerts.rule_id` → `alert_rules.id` (Many-to-One)
   - `alert_rules.region_code` → `regions.code` (Many-to-One, nullable)
   - `fuel_inventory_raw.region_code` → `regions.code` (Many-to-One)
   - `grid_load_raw.region_code`, `renewable_output_raw.region_code`, `emission_raw.region_code` → `regions.code`
   - `recommendations.acknowledged_by` → `users.id` (nullable)

4. **Đánh dấu PK với chìa khoá vàng** 🔑, **FK với chìa khoá xanh** 🗝️ (right-click cell → Edit Tooltip → mở "Field Type" trong template).
5. Nhóm các entity theo 4 Pillar bằng màu nền (Pillar 1 vàng, Pillar 2 xanh dương, Pillar 3 cam, Pillar 4 xanh lá). Right-click entity → Edit Style → đổi `fillColor=#FFE599` (vàng).
6. **File → Export As → PNG** → chọn "Zoom 200%" → đặt tên `docs/erd.png` (sẽ ≥ 1920px chiều rộng).

#### Phần 2: Vẽ Use-case diagram (1.5h)

1. New diagram → đặt tên `usecase.drawio`
2. Vẽ 3 actor (hình stickman, kéo từ sidebar UML) ở bên trái:
   - **End-user (Viewer)**: chỉ xem
   - **Quản lý (Manager)**: xem + acknowledge recommendation
   - **Admin**: tất cả + CRUD users/regions/alert_rules
3. Bên phải vẽ "System boundary" (hình chữ nhật bo) bao quanh các use-case (hình ellipse):
   - **Common (cả 3 actor)**: Đăng nhập, Xem Dashboard 4 Pillar, Xem Security Score, Xem Cảnh báo
   - **Manager + Admin**: Acknowledge recommendation, Dismiss recommendation, Xem cascade risks
   - **Admin only**: CRUD User, CRUD Region, CRUD Alert Rule, Xem audit log
4. Nối actor → use-case bằng đường thẳng (associations).
5. Dùng `<<include>>` (đường đứt + arrow mở) cho relationship: ví dụ "Xem Dashboard" `<<include>>` "Đăng nhập".
6. Export PNG → `docs/usecase.png`.

#### Phần 3: Vẽ Activity diagram (1.5h)

1. New diagram → đặt tên `activity-acknowledge-recommendation.drawio`
2. Vẽ luồng: **User mở app → Đăng nhập → Server xác thực → Lưu JWT** → ... → **User click "Acknowledge" → Server UPDATE status='ACKNOWLEDGED'** → trả về.
3. Dùng các shape (sidebar UML): Start (hình tròn đen), Activity (hình chữ nhật bo góc), Decision (hình thoi), End (hình tròn đôi), Fork/Join (thanh ngang đen).
4. Chia 3 swim-lane (cột dọc, kéo "Swimlane" từ sidebar): **User** | **Backend API** | **PostgreSQL**.
5. Export PNG → `docs/activity-acknowledge.png`.

#### Phần 4: (Bonus 1h) Vẽ Architecture diagram

Mở `README.md` mục "Kiến trúc" → có sẵn ASCII diagram. Vẽ lại bằng draw.io đẹp hơn với:
- 3 Producer (icon Java logo từ sidebar Cloud → Java)
- Kafka cluster (icon)
- Flink job (icon)
- PostgreSQL (icon)
- Spring Boot API (icon)
- JavaFX + Android (icon)
- Cloudflared tunnel (icon)

Export → `docs/architecture.png`.

### 📦 Deliverables (paste vào PR description)

- [ ] `docs/erd.drawio` (source file để sau này sửa được)
- [ ] `docs/erd.png` (≥ 1920px)
- [ ] `docs/usecase.drawio`
- [ ] `docs/usecase.png`
- [ ] `docs/activity-acknowledge.drawio`
- [ ] `docs/activity-acknowledge.png`
- [ ] `docs/architecture.drawio` (bonus)
- [ ] `docs/architecture.png` (bonus)

### ✓ Acceptance criteria (Leader sẽ check)

- ERD đầy đủ ≥ 11 bảng, mỗi bảng có PK + ≥ 1 FK rõ ràng
- Use-case có 3 actor + ≥ 10 use-case + ≥ 2 include/extend
- Activity có ≥ 3 swim-lane + decision + start/end
- Tất cả PNG kích thước ≥ 1920×1080 (HD), chữ đọc được khi zoom 100%

### 💡 Tip
- Dùng template ERD có sẵn trong draw.io (kéo từ sidebar) thay vì vẽ từ đầu
- Lưu file .drawio cùng với .png để sau này sửa được
- Màu sắc đồng bộ với 4 pillar trong README để slide nhìn nhất quán

---

## 📝 B2. Báo cáo Chương 1 — Tổng quan đề tài (5h, NGAY)

### 🎯 Mục tiêu
Viết ~5 trang Word về **tổng quan đề tài an ninh năng lượng VN** — phần mở đầu báo cáo cuối kỳ.

### ✅ Prerequisites
- Đọc trước `README.md` mục "4 Pillars An ninh năng lượng"
- Đọc `JAVA_FINAL_PROJECT_REQUIREMENT.md` mục "4. Cấu trúc quyển báo cáo"
- Tải template Word của trường UIT (hỏi giảng viên link, hoặc dùng template chung)

### 📐 Step-by-step

1. Tạo file `docs/report/chuong-1-tong-quan.docx` (font Times New Roman 13pt, line spacing 1.5)

2. **Cấu trúc 5 trang**:
   - **1.1 Đặt vấn đề** (~1 trang): An ninh năng lượng là gì? VN nhập 70% xăng dầu, lưới điện quá tải mùa khô, cam kết Net Zero 2050 — nguồn: Bộ Công Thương, EVN annual report, IEA Vietnam Energy Profile 2023.
   - **1.2 Lý do chọn đề tài** (~0.5 trang): Cần hệ thống monitoring real-time đa pillar; hiện chưa có open-source nào cover trọn 4 pillar cho VN.
   - **1.3 Phát biểu bài toán** (~1 trang): Cho biết input (4 nguồn dữ liệu Kafka), output (alerts + recommendations + Security Score), constraint (real-time < 5s, free-tier deploy).
   - **1.4 Mục tiêu đề tài** (~0.5 trang): Liệt kê 5 mục tiêu measurable:
     1. Pipeline Kafka→Flink→Postgres xử lý ≥ 500 record/phút
     2. 14 endpoint REST cover 4 pillar
     3. Desktop JavaFX 5 màn (môn Java) + Android 4 màn (môn Mobile)
     4. Energy Security Score 0-100 update real-time
     5. Clone-and-run trong 5 phút
   - **1.5 Phạm vi đề tài** (~0.5 trang): IN/OUT scope. IN = 4 pillar VN, real-time. OUT = forecast ML/LSTM, mobile push notification (stretch).
   - **1.6 Phương pháp luận** (~0.5 trang): Lean startup + Agile 2-week sprint + AI-assisted development (Cursor + Claude).
   - **1.7 Bố cục báo cáo** (~0.5 trang): Tóm tắt 6 chương.

3. **Cách viết** (quan trọng):
   - Mỗi paragraph 3-5 câu, không lê thê
   - Mỗi số liệu có trích dẫn `[1]`, `[2]` ... → list tham khảo cuối Chương 6
   - Hình chèn vào: ít nhất 1 hình minh hoạ ở mục 1.3 (kéo từ `docs/architecture.png` của B1, nếu chưa có thì để placeholder và cập nhật sau)

### 📦 Deliverables
- [ ] `docs/report/chuong-1-tong-quan.docx`
- [ ] `docs/report/references-chuong-1.md` (list 5-7 references APA format)

### ✓ Acceptance
- 5±1 trang (không tính bìa)
- 1 hình minh hoạ + caption "Hình 1.X: ..."
- 5-7 reference có ghi rõ tác giả/năm/URL
- Không lỗi chính tả (dùng Word spell-check)

### 💡 Tip
- Dùng ChatGPT/Cursor để viết draft → tự edit lại cho có giọng văn riêng (KHÔNG copy-paste 100% AI — vi phạm chính sách §5 Java requirement)
- Lưu `docs/report/ai-usage-log-chuong-1.md` ghi rõ prompt + output AI + cách chỉnh sửa (dùng cho Phụ lục báo cáo)

---

## 📝 B3. Báo cáo Chương 2 — Phân tích thiết kế HTTT (7h, sau B1)

### 🎯 Mục tiêu
Viết ~8-10 trang về phân tích bài toán + thiết kế CSDL + Use-case + Activity, dùng diagram đã vẽ ở B1.

### ✅ Prerequisites
- B1 done (đã có ERD + Use-case + Activity PNG)
- Đã đọc `docs/PROGRESS.md` để hiểu các phase đã build

### 📐 Step-by-step

1. Tạo `docs/report/chuong-2-phan-tich-thiet-ke.docx`

2. **Cấu trúc 8-10 trang**:
   - **2.1 Phân tích yêu cầu** (~1 trang): 3 actor + 12 use-case chính (chèn `docs/usecase.png` từ B1).
   - **2.2 Đặc tả use-case chi tiết** (~2 trang): Mô tả 3 use-case quan trọng nhất theo template (Tên, Actor, Pre-condition, Main flow, Alternative flow, Post-condition):
     - UC01: Đăng nhập
     - UC02: Acknowledge Recommendation
     - UC03: Xem Dashboard Pillar 3 + Load Shedding Plan
   - **2.3 Activity diagram** (~1 trang): Chèn `docs/activity-acknowledge.png`, giải thích từng activity.
   - **2.4 Thiết kế CSDL** (~3 trang):
     - Chèn `docs/erd.png` (B1)
     - Bảng "Mô tả entity" với 4 cột: Bảng | Mô tả | Số cột | Ghi chú quan trọng
     - Liệt kê 19 view: tên + mục đích (1 dòng/view), nhóm theo pillar
   - **2.5 Ràng buộc dữ liệu** (~1 trang):
     - Foreign keys (liệt kê 8-10 cái chính)
     - Generated columns: `fuel_inventory_raw.stock_days`, `grid_load_raw.load_pct`, `emission_raw.intensity_kg_per_mwh`
     - CHECK constraints: `recommendations.status IN (PENDING, ACKNOWLEDGED, DISMISSED, EXPIRED)`
   - **2.6 Sequence diagram cho 1 luồng end-to-end** (~1 trang): Generator → Kafka → Flink → Postgres → Spring Boot API → JavaFX. Vẽ bằng draw.io thêm `docs/sequence-end2end.png` rồi chèn vào.

3. **Format**: heading 2.1, 2.2... → dùng style "Heading 2" trong Word để tự tạo Table of Contents sau.

### 📦 Deliverables
- [ ] `docs/report/chuong-2-phan-tich-thiet-ke.docx`
- [ ] `docs/sequence-end2end.png` + `.drawio`

### ✓ Acceptance
- 8-10 trang, 4+ hình
- Đặc tả ≥ 3 use-case chi tiết theo template
- Bảng mô tả entity ≥ 11 dòng (≥ 11 bảng)

---

## 📝 B4. Báo cáo Chương 4 — Quản lý dự án (6h, NGAY)

### 🎯 Mục tiêu
Viết ~6-8 trang về cách quản lý dự án: Charter + SOW + tiến độ + phân công + công cụ.

### ✅ Prerequisites
- Đọc `UPGRADE_PLAN.md §22` (phân công) + `docs/TEAM_TASKS.md` (file này)
- Có quyền truy cập GitHub repo để screenshot Insights

### 📐 Step-by-step

1. Tạo `docs/report/chuong-4-quan-ly-du-an.docx`

2. **Cấu trúc**:
   - **4.1 Bản điều lệ dự án (Project Charter)** (~1 trang): bảng 1 trang gồm:
     - Tên dự án: VES-Monitor — Vietnam Energy Security Real-time Monitor
     - Sponsor: Khoa HTTT — Trường ĐH CNTT
     - Project Manager: A (Leader)
     - Mục tiêu (3 bullets), Phạm vi IN/OUT, Deliverables, Constraint (10 tuần, free-tier), Stakeholder (giảng viên + nhóm), Sign-off date
   - **4.2 Statement of Work (SOW)** (~1 trang):
     - Scope: 11 phase (liệt kê từ §24.5 UPGRADE_PLAN)
     - Timeline: Tuần 1 → Tuần 10
     - Acceptance criteria: §12 hướng dẫn ĐA + smoke test mỗi phase
     - Out of scope: ESI Flink, FCM push, Room cache
   - **4.3 Tổ chức nhóm** (~1 trang): Bảng 5 thành viên + vai trò + email + GitHub username + workload %. Lấy từ `docs/TEAM_TASKS.md` section đầu.
   - **4.4 Phân công nhiệm vụ chi tiết** (~2 trang): Bảng cho mỗi thành viên (B/C/D/E) + 5 PR + deadline + actual completion date.
   - **4.5 Tiến độ thực tế** (~1 trang):
     - Screenshot GitHub Insights → Contributors (tab Contributors): xuất biểu đồ commit theo người
     - Bảng tiến độ phase: Phase | Plan date | Actual date | Status (lấy từ `docs/PROGRESS.md`)
   - **4.6 Công cụ quản lý** (~1 trang):
     - Git + GitHub (PR workflow, branch protection main)
     - GitHub Projects board (kanban Todo/In-progress/Done)
     - Daily standup 15' qua Zalo/Discord
     - AI tools: Cursor AI, ChatGPT, Claude (chi tiết §5 hướng dẫn ĐA)
   - **4.7 Risk management** (~1 trang): bảng 5-7 risk + likelihood + impact + mitigation. Lấy từ `UPGRADE_PLAN.md §15` (Risk).

### 📦 Deliverables
- [ ] `docs/report/chuong-4-quan-ly-du-an.docx`
- [ ] `docs/report/screenshots/github-insights-contributors.png`
- [ ] `docs/report/screenshots/github-projects-board.png`

### ✓ Acceptance
- 6-8 trang, có 2 screenshot GitHub
- Bảng risk ≥ 5 hàng
- Charter có sign-off date (ghi ngày bắt đầu thực tế của nhóm)

---

## 📝 B5. Báo cáo Chương 6 + AI Usage Log + Notion page (6h, NGAY)

### 🎯 Mục tiêu
Viết ~5-6 trang Chương 6 (kết quả + đánh giá) + tổng hợp AI Usage Log + setup Notion giới thiệu nhóm.

### ✅ Prerequisites
- Đã có ít nhất Phase 4 done (Leader confirm)
- Đã có Notion account (free)
- Truy cập 5 thành viên thu thập "AI prompts đã dùng"

### 📐 Step-by-step

#### Phần 1: Chương 6 (3h)

1. Tạo `docs/report/chuong-6-ket-qua-danh-gia.docx`
2. **Cấu trúc**:
   - **6.1 Kết quả đạt được** (~2 trang): Liệt kê deliverable theo phase (copy từ `docs/PROGRESS.md`):
     - Phase 0-4: ✅ Done (smoke test pass)
     - Phase 4.5: 🟡 Code-complete
     - Phase 5/6/7/8: status hiện tại
     - Kèm screenshot UI + curl response.
   - **6.2 Đánh giá thành viên** (~1.5 trang): Bảng 5 thành viên × 5 tiêu chí (Đúng deadline / Chất lượng PR / Tham gia review / Đóng góp họp / Tự học). Mỗi tiêu chí ★ 1-5. **Quan trọng**: liệt kê PR + LOC từ GitHub Insights — con số khách quan tự nói.
   - **6.3 Ưu điểm sản phẩm** (~0.5 trang): 5-7 bullets (real-time, multi-pillar, low memory, JWT, Swagger, etc.)
   - **6.4 Nhược điểm + giới hạn** (~0.5 trang): Không có ML forecast, chưa support multi-tenant, demo data mock không real EVN feed.
   - **6.5 Hướng phát triển** (~0.5 trang): 5 ideas: integrate EVN OAuth, mobile push, LSTM forecast, multi-language, dashboard share link.
   - **6.6 Tài liệu tham khảo** (~1 trang): list 10-15 reference (paper, blog, doc) theo APA format.

#### Phần 2: AI Usage Log (2h)

1. Tạo `docs/report/phu-luc-ai-usage-log.docx`
2. Hỏi từng thành viên: "Bạn đã dùng AI tool nào? Cho prompt + output mẫu."
3. **Format mỗi entry**:
   - Loại AI: ChatGPT-4 / Cursor / Claude / Copilot
   - Mục đích: code generation / debug / writing / diagram
   - Prompt example (paste)
   - Output (paste 1-2 đoạn)
   - Cách kiểm chứng: testing / Leader review / Stack Overflow cross-check
4. Mục tiêu: ≥ 20 entries, đa dạng 5 thành viên.
5. Theo §5 yêu cầu Java requirement: "phải mô tả rõ loại AI, cách dùng, nội dung AI cung cấp và cách nhóm kiểm tra lại".

#### Phần 3: Notion page (1h)

1. Vào https://notion.so → đăng ký free account
2. Tạo workspace tên "VES-Monitor Team"
3. Tạo page "Project Introduction" với các section:
   - **Mục tiêu**: copy từ Chương 1
   - **Thành viên**: 5 card (ảnh + tên + GitHub + email + role)
   - **Tiến độ tuần**: timeline (lấy từ §24.5 UPGRADE_PLAN)
   - **Backlog**: link tới GitHub Projects board
   - **Mockup giao diện**: chèn screenshot từ C3
   - **Video giới thiệu**: chèn link YouTube (sau khi C2 quay)
4. Share Notion public link (Settings → Share → Web link)
5. Paste link vào file `docs/notion-link.md`:
   ```
   # Notion Workspace
   https://notion.so/<your-workspace>/Project-Introduction-...
   ```

### 📦 Deliverables
- [ ] `docs/report/chuong-6-ket-qua-danh-gia.docx`
- [ ] `docs/report/phu-luc-ai-usage-log.docx` (≥ 20 entries)
- [ ] `docs/notion-link.md` (Notion public URL)

### ✓ Acceptance
- Chương 6 đủ 6 mục, ≥ 5 trang
- AI Usage Log đủ 20 entries, có ít nhất 3 entry/thành viên
- Notion accessible bằng incognito browser (test không cần login)

---

# 👤 NGƯỜI C — Media & QA (5 PR, ~25h, non-coding)

> **Định vị:** Không code. Thành thạo PowerPoint, OBS/screen recorder, Postman (click GUI), Markdown. Đầu ra là **slide thuyết trình + video demo + bộ ảnh + test report** — tất cả là tài sản trực quan giúp giảng viên/giám khảo cho điểm cao.

## 📋 Tổng quan 5 PR của Người C

| PR | Task | Time | Phase phụ thuộc | Có thể bắt đầu khi |
|----|------|------|-----------------|---------------------|
| C1 | Slide PowerPoint 20-25 slide | 6h | B1 done (cần diagram) | Sau B1 |
| C2 | Quay video demo 5-7 phút | 5h | Phase 5 + 6 + 7 done | Sau Phase 7 |
| C3 | Bộ screenshot 6 JavaFX + 5 Android + Swagger | 4h | Phase 5 + 6 + 4.5 build | Cuốn chiếu theo phase |
| C4 | Postman Collection từ Swagger | 3h | Phase 4.5 build done | Sau Phase 4.5 build |
| C5 | Manual test report 3 platform + Troubleshooting | 7h | C3 + C4 | Sau C3, C4 |

---

## 📝 C1. Slide PowerPoint 20-25 slide (6h)

### 🎯 Mục tiêu
Slide thuyết trình theo cấu trúc §4 Java requirement: intro / quản lý / công cụ / thiết kế / kiến trúc / demo / Q&A.

### ✅ Prerequisites
- B1 done (có diagram để chèn)
- README + Chương 1 (B2) draft xong

### 📐 Step-by-step

1. Mở PowerPoint → tạo file `docs/slide.pptx`. Dùng template "Madison" hoặc "Wisp" cho clean look.

2. **Cấu trúc 22 slide chuẩn**:

| # | Slide | Nội dung |
|---|-------|----------|
| 1 | Bìa | Tên đề tài + logo trường + 5 thành viên + GVHD + môn |
| 2 | Mục lục | 8 mục lớn |
| 3 | Giới thiệu nhóm | Bảng 5 ảnh thành viên + role + skill |
| 4 | Đặt vấn đề | An ninh năng lượng VN 2026 (3 fact + 1 ảnh) |
| 5 | Mục tiêu | 5 bullets measurable (từ B2 1.4) |
| 6 | Phạm vi | IN/OUT scope (2 cột) |
| 7 | Quản lý dự án | Charter mini + timeline 10 tuần (từ B4) |
| 8 | Công cụ & công nghệ | Logo grid: Kafka, Flink, Postgres, Spring Boot, JavaFX, Android, Docker, Cloudflare |
| 9 | Use-case diagram | Chèn `docs/usecase.png` (B1) |
| 10 | ERD | Chèn `docs/erd.png` (B1), full slide |
| 11 | Activity diagram | Chèn `docs/activity-acknowledge.png` (B1) |
| 12 | Kiến trúc tổng | Chèn `docs/architecture.png` (B1) — slide chính nhất |
| 13 | 4 Pillars an ninh NL | Table 4 row (lấy từ README phần "4 Pillars") |
| 14 | Pipeline real-time | Flow: Generator → Kafka → Flink (9 vertex) → Postgres → API → UI |
| 15 | Energy Security Score | Diagram tính ESI 4 pillar × 25% + screenshot `v_security_score` |
| 16 | Demo: Dashboard JavaFX | Screenshot 4-tab pillar (từ C3) |
| 17 | Demo: Cảnh báo + Recommendation | Screenshot recommendation panel + ACK button (từ C3) |
| 18 | Demo: Android | Screenshot 4 màn (từ C3) |
| 19 | Demo video | Embed link YouTube (từ C2) hoặc QR code |
| 20 | Kết quả đạt được | 5 bullets ✓ (từ B5 mục 6.1) |
| 21 | Hạn chế + Hướng phát triển | 2 cột |
| 22 | Q&A | "Cảm ơn" + 5 thành viên email + GitHub link |

3. **Style guide**:
   - Font: Calibri / Inter, body 18pt, heading 28pt
   - Màu chính: xanh dương #1F77B4 (tone tech)
   - Logo trường UIT ở góc dưới mỗi slide
   - Mỗi slide ≤ 5 bullet, ≤ 30 từ — đừng nhồi chữ

4. Export bản backup PDF: **File → Export → PDF** → `docs/slide.pdf` (dự phòng máy demo không có PowerPoint).

### 📦 Deliverables
- [ ] `docs/slide.pptx`
- [ ] `docs/slide.pdf` (backup)

### ✓ Acceptance
- 20-25 slide, không quá 30 từ/slide
- Có ≥ 5 hình từ B1 (ERD, Use-case, Activity, Architecture, Sequence)
- Slide 13 (4 Pillars) đầy đủ 4 hàng
- File ≤ 50 MB (compress hình nếu cần: File → Compress Pictures)

### 💡 Tip
- Dùng SmartArt cho slide 14 (Pipeline) — đẹp hơn vẽ tay
- Mỗi animation nên là "Appear" hoặc "Fade", tránh "Bounce" — gây ngứa mắt giám khảo

---

## 📝 C2. Quay video demo 5-7 phút (5h, sau Phase 7)

### 🎯 Mục tiêu
1 video MP4 5-7 phút demo end-to-end + voice over tiếng Việt. Dự phòng nếu live demo bị crash trong Q&A.

### ✅ Prerequisites
- Phase 5 + 6 + 7 done (UI desktop + Android + tunnel chạy được)
- Cài OBS Studio: https://obsproject.com/
- Microphone (built-in laptop là đủ) hoặc clip on mic
- Headphone để monitor

### 📐 Step-by-step

#### Bước 1: Viết kịch bản (1h)

1. Tạo `docs/demo-script.md`:

```markdown
## Phút 0:00 - 0:30 — Intro
"Xin chào, em là <C tên>, đại diện nhóm <X>. Hôm nay nhóm em demo VES-Monitor — nền tảng giám sát an ninh năng lượng VN real-time. Stack chính: Kafka + Flink + Postgres + JavaFX + Android."

## Phút 0:30 - 1:00 — Khởi động hệ thống (đã bật sẵn từ trước, chỉ cần chỉ vào terminal)
"Em đã chạy sẵn lệnh bash scripts/run.sh — toàn bộ stack docker UP trong 60 giây. Hiện tại 3 generator đang gửi 4 stream Kafka, Flink 9 vertex đều RUNNING."

## Phút 1:00 - 2:00 — Backend API + Swagger
- Mở Swagger UI http://localhost:8090/swagger-ui.html
- Login với admin/admin → lấy JWT
- Click thử /api/security/score → giải thích Light ESI 76.4 STABLE
- Click /api/pillars/3/shedding-plan → giải thích load shedding priority

## Phút 2:00 - 3:30 — JavaFX Desktop
- Mở JavaFX app, login
- Vào tab Pillar 1: chỉ vào "URGENT: còn 25 ngày, đề xuất chuyển từ VN_CENTRAL..." → đây là actionable
- Vào tab Pillar 3: chỉ load_pct chart + button "Acknowledge" recommendation
- Click ACK → thông báo thành công + status chuyển PENDING → ACKNOWLEDGED

## Phút 3:30 - 5:00 — Android app
- Bật điện thoại thật, kết nối WiFi cùng máy + URL cloudflared tunnel
- Mở app, login
- Vào bottom-nav Pillar 2 (giá nhiên liệu) → chart realtime
- Vào tab Alerts → list alerts từ 3 pillar
- Tap 1 alert → chi tiết

## Phút 5:00 - 6:00 — Bonus: stress test trigger cascade
- Quay sang terminal: chạy bash scripts/phase4_stress_alerts.sh
- Quay lại JavaFX: thấy Security Score rớt từ 76 → 63 trong real-time, badge "CASCADE RISK" hiện sidebar
- Đây là điểm nhấn — multi-pillar tự phát hiện compound risk

## Phút 6:00 - 6:30 — Kiến trúc tóm tắt
- Quay sang slide kiến trúc (slide 12)
- "Mọi thứ vừa demo được serve bởi 1 Flink job với 9 vertex song song và 19 SQL views trong Postgres."

## Phút 6:30 - 7:00 — Cảm ơn
- "Cảm ơn thầy/cô đã xem. Em sẵn sàng nhận câu hỏi."
- Hiện QR code GitHub repo + Notion page.
```

#### Bước 2: Test setup OBS (30 phút)

1. Mở OBS → Source mới → Display Capture (chọn màn hình chính)
2. Source thứ 2 → Audio Input Capture (chọn mic)
3. Settings → Output → Video Bitrate 4000, Audio Bitrate 192. Format MP4, FPS 30.
4. Test record 30 giây → playback xem có rõ tiếng + hình không.

#### Bước 3: Quay (2h, kể cả retake)

1. Chuẩn bị môi trường: tắt notification (Do Not Disturb), đóng app không cần thiết, clear desktop.
2. Mở sẵn các app/tab cần demo (theo kịch bản). Pre-set zoom 125% để chữ to dễ nhìn.
3. **Quay 3 lần** rồi chọn bản tốt nhất. Mỗi lần ~7 phút.
4. Voice: nói chậm, rõ, không "ờ ờ um um". Có thể đọc kịch bản (nhưng tránh nghe như đang đọc).

#### Bước 4: Edit nhanh (1.5h)

1. Cài DaVinci Resolve (free) hoặc dùng Clipchamp (sẵn trong Windows 10/11)
2. Import 3 bản → chọn bản tốt nhất
3. Cắt phần thừa đầu/cuối
4. Thêm tiêu đề mở đầu "VES-Monitor Demo — Nhóm X" 3 giây
5. Thêm subtitle tự động (Clipchamp có Auto Captions)
6. Export 1080p MP4 → `docs/demo-video.mp4`

#### Bước 5: Upload + share (30 phút)

1. Upload lên YouTube (Unlisted) hoặc Google Drive (link Anyone with link)
2. Lưu link vào `docs/demo-video-link.md`:
   ```
   # Demo Video Link
   YouTube: https://youtu.be/...
   Backup Drive: https://drive.google.com/...
   ```

### 📦 Deliverables
- [ ] `docs/demo-script.md`
- [ ] `docs/demo-video.mp4` (≤ 100 MB, hoặc upload Drive nếu lớn hơn)
- [ ] `docs/demo-video-link.md` (YouTube + Drive backup link)

### ✓ Acceptance
- Video 5-7 phút, 1080p, có audio rõ ràng
- Có subtitle (Auto Caption OK)
- Cover đủ 4 pillar
- Demo được luồng acknowledge recommendation (luồng nghiệp vụ chính)

### 💡 Tip
- Quay vào buổi tối yên tĩnh, đóng cửa, tắt quạt
- Đọc qua kịch bản 3 lần TRƯỚC khi quay — sẽ nói trôi chảy hơn
- Backup video lên cả YouTube + Drive — phòng 1 trong 2 không truy cập được lúc demo

---

## 📝 C3. Bộ screenshot 6 JavaFX + 5 Android + Swagger (4h)

### 🎯 Mục tiêu
~16 ảnh PNG đẹp dùng cho slide + báo cáo Chương 3 (UI/UX).

### ✅ Prerequisites
- Phase 4.5 build done → Swagger up
- Phase 5 done → JavaFX up
- Phase 6 done → Android trên emulator hoặc điện thoại thật

### 📐 Step-by-step

#### Phần 1: Screenshot JavaFX (1.5h)

Cần ≥ 6 màn:
1. **Login screen** — form trống
2. **Login screen** — error "Sai password" (chụp validation)
3. **Dashboard Pillar 1** — table với recommendation_text
4. **Dashboard Pillar 2** — line chart giá nhiên liệu
5. **Dashboard Pillar 3** — bar chart load + panel shedding plan
6. **Dashboard Pillar 4** — pie chart renewable + Net Zero gauge
7. **Recommendations sidebar** — list 3 recommendation PENDING với button ACK
8. **About screen** (D2 sẽ làm) — thông tin nhóm

Cách chụp:
- Windows: `Win + Shift + S` → chụp region, paste vào Paint → save PNG
- Hoặc dùng **ShareX** (free, có annotate)
- Đặt tên: `docs/screenshots/javafx-01-login.png`, `javafx-02-login-error.png`, `javafx-03-pillar1.png`, ...

#### Phần 2: Screenshot Android (1.5h)

Cần ≥ 5 màn:
1. **Login**
2. **Home** — 4 KPI cards
3. **Chart** — fuel price line chart
4. **Alert list** — list 5 alerts đa pillar
5. **About** (E2 sẽ làm)

Cách chụp:
- **Android Studio Emulator**: nút Camera trong toolbar bên phải
- **Điện thoại thật**: nút Power + Volume Down 1 giây
- Đặt tên: `docs/screenshots/android-01-login.png`, ...

#### Phần 3: Screenshot Swagger + Postman (1h)

1. **Swagger UI** với 14 endpoint visible → 2 ảnh:
   - `docs/screenshots/swagger-overview.png` (cả list endpoint)
   - `docs/screenshots/swagger-pillar3-response.png` (expand 1 endpoint show response)
2. **Postman** chạy login → token + 1 endpoint response → 1 ảnh:
   - `docs/screenshots/postman-login-response.png`

#### Phần 4: Crop + annotate (30 phút)

Mở mỗi ảnh trong Paint hoặc ShareX:
- Crop bỏ taskbar Windows / status bar Android (chỉ giữ window app)
- Thêm số thứ tự + chú thích nhỏ ở góc nếu cần (ví dụ ① "Login form")

### 📦 Deliverables
- [ ] `docs/screenshots/javafx-XX-*.png` × ≥ 6
- [ ] `docs/screenshots/android-XX-*.png` × ≥ 5
- [ ] `docs/screenshots/swagger-*.png` × ≥ 2
- [ ] `docs/screenshots/postman-*.png` × 1
- [ ] `docs/screenshots/README.md` — index liệt kê tất cả ảnh + mô tả 1 dòng/ảnh

### ✓ Acceptance
- ≥ 14 ảnh PNG
- Mỗi ảnh ≥ 1280×720
- Không chứa info nhạy cảm (token, password thật → mask đi)

---

## 📝 C4. Postman Collection từ Swagger (3h)

### 🎯 Mục tiêu
1 file JSON `*.postman_collection.json` chứa 14 request đã config sẵn (auth, headers, body) — để Leader/giảng viên click chạy thử nhanh không cần curl.

### ✅ Prerequisites
- Phase 4.5 build done, app chạy port 8090
- Cài Postman: https://www.postman.com/downloads/
- Đăng ký account Postman free

### 📐 Step-by-step

1. Mở Postman → top-right "Import" → chọn "Link" → paste `http://localhost:8090/v3/api-docs`
2. Postman tự tạo collection từ OpenAPI spec, đặt tên "VES-Monitor API"
3. **Tạo Environment** mới tên "Local Dev" với 2 variable:
   - `baseUrl` = `http://localhost:8090`
   - `token` = (để trống, sẽ tự fill sau khi login)
4. Mở request **POST /api/auth/login**:
   - Body → raw JSON: `{"username":"admin","password":"admin"}`
   - Tab "Tests" paste script auto-extract token:
     ```javascript
     const json = pm.response.json();
     pm.environment.set("token", json.accessToken);
     console.log("Token saved:", json.accessToken.substring(0, 20) + "...");
     ```
5. Mở mỗi request authenticated → tab "Authorization" → Type "Bearer Token" → Token = `{{token}}`. Postman auto-add header.
6. Test từng request → confirm 14/14 trả 2xx.
7. **Export collection**:
   - Right-click collection "VES-Monitor API" → Export → Collection v2.1 (JSON)
   - Save vào `tests/postman/ves-monitor-api.postman_collection.json`
8. **Export environment**:
   - Settings → Environments → "Local Dev" → 3 dots → Export
   - Save vào `tests/postman/local-dev.postman_environment.json`
9. Viết `tests/postman/README.md`:

```markdown
# VES-Monitor Postman Collection

## Cách dùng
1. Mở Postman → Import → chọn 2 file:
   - `ves-monitor-api.postman_collection.json`
   - `local-dev.postman_environment.json`
2. Top-right chọn environment "Local Dev"
3. Chạy "POST /api/auth/login" trước → token tự lưu vào env variable
4. Chạy 13 request còn lại — tất cả tự dùng token

## Có thể chạy "Run Collection" (Runner)
- Click "Run" trên collection → Postman chạy toàn bộ 14 request tuần tự
- Verify: 14/14 status 2xx
```

### 📦 Deliverables
- [ ] `tests/postman/ves-monitor-api.postman_collection.json`
- [ ] `tests/postman/local-dev.postman_environment.json`
- [ ] `tests/postman/README.md`

### ✓ Acceptance
- 14 request đã được Postman tự import
- POST /login có script Tests auto-save token
- 13 request authenticated dùng `{{token}}`
- "Run Collection" pass 14/14

---

## 📝 C5. Manual test report 3 platform + Troubleshooting (7h)

### 🎯 Mục tiêu
1 file Markdown ~20 trang ghi lại quá trình test thủ công 3 platform (API + JavaFX + Android), kèm bug log + 10 lỗi thường gặp (Troubleshooting).

### ✅ Prerequisites
- C3 done (có screenshot)
- C4 done (có Postman collection)
- 3 platform chạy được

### 📐 Step-by-step

1. Tạo `docs/TEST_REPORT.md`

2. **Cấu trúc**:

   #### 5.1 Test API (1.5h)
   - Mở Postman, chạy tuần tự 14 request
   - Mỗi request ghi:
     | # | Endpoint | Expected | Actual | Status | Screenshot |
     |---|----------|----------|--------|--------|------------|
     | 1 | POST /login admin/admin | 200, token | 200, token | ✅ | postman-01.png |
     | 2 | POST /login admin/wrong | 401 | 401 | ✅ | postman-02.png |
     | ... | | | | | |
   - Test edge cases: token expired (đợi 8h hoặc dùng token cũ), invalid JSON body, missing field

   #### 5.2 Test JavaFX (2h)
   - Mở app, theo checklist:
     - [ ] Login đúng admin/admin → vào Dashboard
     - [ ] Login sai password → hiện error
     - [ ] Switch 4 tab Pillar → mỗi tab vẽ chart/table
     - [ ] Click "Acknowledge" recommendation → confirm dialog → ACK xong, list refresh
     - [ ] Logout → trở về Login screen
   - Test responsive: resize cửa sổ 800×600 → 1920×1080 → có bị vỡ layout không
   - Test long data: chạy stress test, mở table có 100+ row → scroll OK không

   #### 5.3 Test Android (2h)
   - Tương tự checklist:
     - [ ] Login admin/admin
     - [ ] Bottom nav 4 pillar mỗi tab render
     - [ ] Pull-to-refresh trên Alert list
     - [ ] Mất mạng → app có crash không?
     - [ ] Background app 5 phút → quay lại token còn valid không?
   - Test trên 2 device: emulator + điện thoại thật (note Android version)

   #### 5.4 Bug log (1h)
   - Bảng:
     | # | Bug | Severity | Platform | Reproduce steps | Status | PR fix |
     |---|-----|----------|----------|-----------------|--------|--------|
     | 1 | Recommendation ACK button đôi khi không refresh list | Low | JavaFX | 1. Mở app 2. ACK rec id=1 3. List không update | Open | - |

3. **Phần 5.5 Troubleshooting** (0.5h): 10 lỗi thường gặp + giải pháp. Tạo file riêng `docs/TROUBLESHOOTING.md`:

```markdown
# 🐛 TROUBLESHOOTING — 10 lỗi thường gặp

## 1. Docker stack không UP
**Triệu chứng**: `docker compose ps` thấy service "exited"
**Giải pháp**: `bash scripts/stop.sh --volumes && bash scripts/run.sh`

## 2. Kafka topic không tồn tại
**Triệu chứng**: Flink log `UnknownTopicOrPartitionException`
**Giải pháp**: `bash scripts/create_kafka_topics.sh`

## 3. Spring Boot build fail 407 Proxy Auth
**Triệu chứng**: `mvn package` thất bại
**Giải pháp**: Hotspot 4G hoặc cntlm local bridge. Xem `backend-api/README.md`.

## 4. JavaFX không chạy với Java 17+
**Triệu chứng**: `Error: JavaFX runtime components are missing`
**Giải pháp**: `mvn javafx:run` (plugin tự setup module path)

## 5. Android emulator không kết nối API localhost
**Triệu chứng**: `ConnectException`
**Giải pháp**: Đổi BASE_URL từ `localhost` → `10.0.2.2` (emulator alias)

## 6. Postgres "password authentication failed"
**Triệu chứng**: Login app báo lỗi DB
**Giải pháp**: Check `.env` POSTGRES_PASSWORD đồng nhất với `application.yml`

## 7. Metabase không thấy bảng
**Triệu chứng**: Empty database trong Metabase
**Giải pháp**: Trong Metabase, Host = `postgresql` (Docker service name), KHÔNG phải `localhost`

## 8. Port 8090 bị chiếm
**Triệu chứng**: `Port already in use`
**Giải pháp**: `SERVER_PORT=8091 java -jar backend-api/target/ves-backend-api.jar`

## 9. JWT token hết hạn
**Triệu chứng**: 401 sau 8h
**Giải pháp**: Login lại

## 10. Cloudflared tunnel 502 từ điện thoại
**Triệu chứng**: App điện thoại báo Bad Gateway
**Giải pháp**: Restart `bash scripts/tunnel.sh`, URL sẽ đổi → cập nhật vào app config
```

### 📦 Deliverables
- [ ] `docs/TEST_REPORT.md` (≥ 15 trang)
- [ ] `docs/TROUBLESHOOTING.md` (10 lỗi)
- [ ] Bug log ≥ 5 bug (có thể minor)

### ✓ Acceptance
- 3 platform mỗi cái có checklist ≥ 5 item
- Có ≥ 5 bug ghi trong log (kể cả minor)
- Troubleshooting ≥ 10 entries

---

# 👤 NGƯỜI D — JavaFX UI Junior (5 PR, ~20h, simple coding)

> **Định vị:** Biết Java cơ bản (class, method, if/else, ArrayList). Không cần hiểu Spring, Flink, Docker. Chỉ cài IntelliJ IDEA + chạy `mvn` trong module `desktop-admin/` của mình. Tasks là **copy template + sửa text/màu/string** — Leader cung cấp file mẫu, bạn nhân bản và đổi thông tin riêng.

> ⚠️ **D KHÔNG cần chạy Docker, Postgres, Kafka, Flink, Spring Boot.** Chỉ cần build + chạy được module `desktop-admin/` standalone. Cho 2 task đầu (D1, D2), thậm chí không cần JavaFX skeleton từ Leader.

## 📋 Tổng quan 5 PR của Người D

| PR | Task | Time | Phase phụ thuộc | Có thể bắt đầu khi |
|----|------|------|-----------------|---------------------|
| D1 | Mở rộng SQL seed VN (30+ INSERT) | 3h | Phase 2.5 done ✅ | NGAY |
| D2 | Util class: `ValidatorUtils.java` + 5 JUnit test | 3h | - | NGAY (không cần phase nào, code Java thuần) |
| D3 | JavaFX About screen (copy template từ Leader) | 4h | Phase 5 skeleton từ Leader | Sau Leader đẩy skeleton Phase 5 |
| D4 | JavaFX Settings screen + theme toggle | 5h | D3 done | Sau D3 |
| D5 | CSS polish theo 4 pillar color + Splash screen | 5h | Phase 5 done | Sau Phase 5 |

---

## 📝 D1. Mở rộng SQL seed VN (3h, NGAY)

### 🎯 Mục tiêu
Tạo file SQL `infra/script/10_seed_vn_extended.sql` thêm 30+ INSERT thực tế VN (cơ sở năng lượng, tỉnh thành, alert rule mở rộng) để demo trông giàu dữ liệu hơn.

### ✅ Prerequisites
- Đọc qua `infra/script/02_init_users_regions.sql` (xem cấu trúc bảng `regions`)
- Đọc `infra/script/04_seed_basic.sql` + `06_seed_pillars.sql` (xem cách Leader đã seed mẫu)
- Đọc `infra/script/08_seed_security_features.sql` (xem cách seed alert rule)
- KHÔNG cần Postgres chạy — chỉ cần viết file SQL đúng cú pháp

### 📐 Step-by-step

1. Tạo branch:
   ```bash
   git checkout main && git pull
   git checkout -b d/D1-seed-vn-extended
   ```

2. Tạo file `infra/script/10_seed_vn_extended.sql` với cấu trúc:

```sql
-- =============================================================
-- 10_seed_vn_extended.sql  (Phase 24.6+ extension)
-- Tác giả: Người D
-- Mục đích: thêm 30+ data thực tế VN cho demo
-- Idempotent: ON CONFLICT DO NOTHING
-- =============================================================

-- ---------- 6 vùng mở rộng (đã có VN_NORTH, VN_CENTRAL, VN_SOUTH trong 02_init) ----------
INSERT INTO regions (code, name, country, latitude, longitude) VALUES
  ('VN_HANOI',     'Hà Nội',          'Vietnam', 21.0285, 105.8542),
  ('VN_HAIPHONG',  'Hải Phòng',       'Vietnam', 20.8449, 106.6881),
  ('VN_DANANG',    'Đà Nẵng',         'Vietnam', 16.0544, 108.2022),
  ('VN_QUANGNINH', 'Quảng Ninh',      'Vietnam', 21.0064, 107.2925),
  ('VN_NINHTHUAN', 'Ninh Thuận',      'Vietnam', 11.6739, 108.8629),
  ('VN_BINHTHUAN', 'Bình Thuận',      'Vietnam', 11.0904, 108.0721)
ON CONFLICT (code) DO NOTHING;

-- ---------- Inventory thực tế VN (Pillar 1) ----------
-- 8 hàng bao gồm các kho lớn: Nhà Bè, Đình Vũ, Vân Phong, Cát Lái
-- Mỗi region 1-2 loại nhiên liệu
INSERT INTO fuel_inventory_raw (region_code, fuel_type, stock_volume_kl, daily_consumption_kl, target_days, reported_at) VALUES
  ('VN_HANOI',     'GASOLINE',  85000,  1500, 90, NOW()),
  ('VN_HANOI',     'DIESEL',    120000, 2200, 90, NOW()),
  ('VN_HAIPHONG',  'GASOLINE',  45000,  900,  90, NOW()),  -- Đình Vũ
  ('VN_HAIPHONG',  'DIESEL',    78000,  1400, 90, NOW()),
  ('VN_DANANG',    'GASOLINE',  35000,  650,  90, NOW()),
  ('VN_QUANGNINH', 'DIESEL',    55000,  1100, 90, NOW()),  -- Cảng Cái Lân
  ('VN_NINHTHUAN', 'GASOLINE',  18000,  280,  60, NOW()),  -- nhỏ, dùng cho local
  ('VN_BINHTHUAN', 'DIESEL',    25000,  450,  60, NOW())
ON CONFLICT DO NOTHING;

-- ---------- Grid load mẫu (Pillar 3) ----------
-- 6 region × 1 record mỗi region = 6 row
-- Để demo có data baseline khi không chạy generator
INSERT INTO grid_load_raw (region_code, load_mw, capacity_mw, is_peak_hour, event_time) VALUES
  ('VN_HANOI',     8500, 12000, false, NOW()),
  ('VN_HAIPHONG',  3200, 5000,  false, NOW()),
  ('VN_DANANG',    2800, 4500,  false, NOW()),
  ('VN_QUANGNINH', 4500, 7000,  false, NOW()),
  ('VN_NINHTHUAN', 1200, 2500,  false, NOW()),
  ('VN_BINHTHUAN', 950,  2000,  false, NOW())
ON CONFLICT DO NOTHING;

-- ---------- Renewable output (Pillar 4) — 3 source type × 3 region ----------
INSERT INTO renewable_output_raw (region_code, source_type, output_mw, capacity_mw, event_time) VALUES
  -- Ninh Thuận = thủ phủ điện mặt trời VN
  ('VN_NINHTHUAN', 'SOLAR', 850,  1500, NOW()),
  ('VN_NINHTHUAN', 'WIND',  120,  300,  NOW()),
  -- Bình Thuận = điện gió
  ('VN_BINHTHUAN', 'SOLAR', 320,  600,  NOW()),
  ('VN_BINHTHUAN', 'WIND',  280,  500,  NOW()),
  -- Quảng Ninh = thuỷ điện ít
  ('VN_QUANGNINH', 'HYDRO', 150,  400,  NOW()),
  -- Hà Nội (nội thành ít renewable)
  ('VN_HANOI',     'SOLAR', 50,   200,  NOW())
ON CONFLICT DO NOTHING;

-- ---------- Alert rules mở rộng (10 rule) ----------
INSERT INTO alert_rules (rule_name, metric_type, fuel_type, region_code, threshold, operator, severity, enabled) VALUES
  ('Inventory HANOI Gasoline < 30 ngày',  'INVENTORY_DAYS',    'GASOLINE', 'VN_HANOI',     30,  '<', 'WARNING',  true),
  ('Inventory HANOI Gasoline < 15 ngày',  'INVENTORY_DAYS',    'GASOLINE', 'VN_HANOI',     15,  '<', 'CRITICAL', true),
  ('Inventory HAIPHONG Diesel < 30 ngày', 'INVENTORY_DAYS',    'DIESEL',   'VN_HAIPHONG',  30,  '<', 'WARNING',  true),
  ('Grid load HANOI > 80%',                'GRID_LOAD_PCT',     NULL,       'VN_HANOI',     80,  '>', 'WARNING',  true),
  ('Grid load HANOI > 92%',                'GRID_LOAD_PCT',     NULL,       'VN_HANOI',     92,  '>', 'CRITICAL', true),
  ('Grid load QUANGNINH > 85%',           'GRID_LOAD_PCT',     NULL,       'VN_QUANGNINH', 85,  '>', 'WARNING',  true),
  ('Emission DANANG > 650 kg/MWh',         'EMISSION_INTENSITY', NULL,       'VN_DANANG',    650, '>', 'WARNING',  true),
  ('Brent > 90 USD/barrel',                'FUEL_PRICE',         'BRENT_CRUDE', NULL,        90,  '>', 'WARNING',  true),
  ('WTI < 60 USD/barrel (price crash)',    'FUEL_PRICE',         'WTI_CRUDE',   NULL,        60,  '<', 'WARNING',  true),
  ('Gasoline > 3.5 USD/gallon',            'FUEL_PRICE',         'GASOLINE',    NULL,        3.5, '>', 'WARNING',  true)
ON CONFLICT DO NOTHING;

-- ---------- Sample recommendations PENDING ----------
INSERT INTO recommendations (pillar, action_type, severity, title, message, suggested_data, expires_at) VALUES
  (1, 'TRANSFER_STOCK', 'WARNING',  'Chuyển 5000 KL Gasoline NINHTHUAN → HANOI',
       'Hà Nội còn 56.6 ngày tồn kho, dưới target 90. Đề xuất chuyển 5000 KL từ kho Cát Lái.',
       '{"from":"VN_NINHTHUAN","to":"VN_HANOI","volume_kl":5000,"fuel":"GASOLINE"}',
       NOW() + INTERVAL '7 days'),
  (3, 'PEAK_SHAVING_PREP', 'INFO', 'Chuẩn bị peak shaving HANOI 18-22h',
       'Dự báo load HANOI hôm nay > 90% trong khung 18-22h. Sẵn sàng cut 800MW khu công nghiệp Thăng Long.',
       '{"region":"VN_HANOI","window":"18:00-22:00","shed_mw":800}',
       NOW() + INTERVAL '1 day')
ON CONFLICT DO NOTHING;
```

3. **Verify cú pháp** (không cần Postgres chạy, chỉ check syntax):
   ```bash
   # Cài psql client nếu chưa có: sudo apt install postgresql-client (WSL)
   # Hoặc dùng VSCode extension "SQLTools" tự check syntax
   # Hoặc paste online vào https://www.db-fiddle.com/ chọn PostgreSQL 15
   ```

4. **Test idempotent** (optional, nếu Leader đang chạy Postgres):
   ```bash
   docker exec -i postgres-database psql -U postgres -d fuel_prices < infra/script/10_seed_vn_extended.sql
   # Chạy lại lần 2: kỳ vọng "INSERT 0 0" cho mọi statement (do ON CONFLICT)
   ```

5. Commit + push + PR:
   ```bash
   git add infra/script/10_seed_vn_extended.sql
   git commit -m "D1: thêm seed VN mở rộng — 6 region + 8 inventory + 6 grid + 6 renewable + 10 alert_rules + 2 recommendation"
   git push -u origin d/D1-seed-vn-extended
   # Mở PR trên GitHub
   ```

### 📦 Deliverables
- [ ] `infra/script/10_seed_vn_extended.sql` (~120 dòng)

### ✓ Acceptance
- File chạy không lỗi syntax (Leader sẽ apply vào DB để verify)
- Tất cả INSERT có `ON CONFLICT DO NOTHING` (idempotent)
- ≥ 30 row mới tổng cộng (6 region + 8 inventory + 6 grid + 6 renewable + 10 alert + 2 rec = 38)
- Data thực tế VN có ý nghĩa (không random)

### 💡 Tip
- Nếu không biết toạ độ/dung tích kho — search Google "kho xăng dầu Nhà Bè dung tích" hoặc tham khảo báo cáo EVN
- Dùng AI: "Cho tôi danh sách 6 kho xăng dầu lớn nhất VN với toạ độ và dung tích" — copy data, ghi vào AI Usage Log

---

## 📝 D2. Util class ValidatorUtils + 5 JUnit test (3h, NGAY)

### 🎯 Mục tiêu
Tạo class Java thuần `ValidatorUtils` với 5 method tiện ích + 5 JUnit test → góp phần đạt yêu cầu "Unit Test bằng JUnit" trong §3 Java requirement.

### ✅ Prerequisites
- Cài IntelliJ IDEA Community
- Cài JDK 11 (Adoptium Temurin)
- KHÔNG cần biết JavaFX/Spring — chỉ là Java thuần

### 📐 Step-by-step

1. Branch:
   ```bash
   git checkout main && git pull
   git checkout -b d/D2-validator-utils
   ```

2. Tạo cấu trúc folder mới `desktop-admin/` (Phase 5 chưa có nên D tạo skeleton):
   ```bash
   mkdir -p desktop-admin/src/main/java/vn/edu/ves/desktop/util
   mkdir -p desktop-admin/src/test/java/vn/edu/ves/desktop/util
   ```

3. Tạo file `desktop-admin/pom.xml` (skeleton tối thiểu, Leader sẽ extend sau ở Phase 5):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>vn.edu.ves</groupId>
        <artifactId>ves-monitor-parent</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>
    <artifactId>desktop-admin</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    <name>VES-Monitor JavaFX Desktop Admin (Phase 5)</name>

    <dependencies>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>11</source>
                    <target>11</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

4. Tạo `desktop-admin/src/main/java/vn/edu/ves/desktop/util/ValidatorUtils.java`:

```java
package vn.edu.ves.desktop.util;

/**
 * Tiện ích validate dữ liệu nhập từ form JavaFX.
 *
 * Không depend vào JavaFX framework — pure Java để dễ JUnit test
 * và Leader có thể tái sử dụng ở module khác (backend-api, Android).
 */
public final class ValidatorUtils {

    private ValidatorUtils() {}

    /** Email hợp lệ theo regex đơn giản (đủ cho form đăng ký user). */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /** Username 4-20 ký tự, chỉ chữ/số/_, không bắt đầu bằng số. */
    public static boolean isValidUsername(String username) {
        if (username == null) return false;
        return username.matches("^[a-zA-Z_][a-zA-Z0-9_]{3,19}$");
    }

    /** Password ≥ 6 ký tự, có ít nhất 1 chữ + 1 số. */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 6) return false;
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit  = password.chars().anyMatch(Character::isDigit);
        return hasLetter && hasDigit;
    }

    /** Region code: 2-20 ký tự uppercase + underscore. */
    public static boolean isValidRegionCode(String code) {
        if (code == null) return false;
        return code.matches("^[A-Z][A-Z0-9_]{1,19}$");
    }

    /** Threshold số dương (cho alert_rules.threshold). */
    public static boolean isPositiveNumber(String input) {
        if (input == null || input.isBlank()) return false;
        try {
            double v = Double.parseDouble(input.trim());
            return v > 0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
```

5. Tạo `desktop-admin/src/test/java/vn/edu/ves/desktop/util/ValidatorUtilsTest.java`:

```java
package vn.edu.ves.desktop.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidatorUtilsTest {

    @Test
    void email_valid() {
        assertTrue(ValidatorUtils.isValidEmail("admin@uit.edu.vn"));
        assertTrue(ValidatorUtils.isValidEmail("user.name+tag@gmail.com"));
    }

    @Test
    void email_invalid() {
        assertFalse(ValidatorUtils.isValidEmail(null));
        assertFalse(ValidatorUtils.isValidEmail(""));
        assertFalse(ValidatorUtils.isValidEmail("not-email"));
        assertFalse(ValidatorUtils.isValidEmail("missing@dot"));
    }

    @Test
    void username_valid() {
        assertTrue(ValidatorUtils.isValidUsername("admin"));
        assertTrue(ValidatorUtils.isValidUsername("user_123"));
        assertFalse(ValidatorUtils.isValidUsername("a"));            // quá ngắn
        assertFalse(ValidatorUtils.isValidUsername("123admin"));     // bắt đầu số
        assertFalse(ValidatorUtils.isValidUsername("user name"));    // có space
    }

    @Test
    void password_strong() {
        assertTrue(ValidatorUtils.isStrongPassword("abc123"));
        assertTrue(ValidatorUtils.isStrongPassword("MySecure2026"));
        assertFalse(ValidatorUtils.isStrongPassword("abc"));         // ngắn
        assertFalse(ValidatorUtils.isStrongPassword("abcdef"));      // không có số
        assertFalse(ValidatorUtils.isStrongPassword("123456"));      // không có chữ
    }

    @Test
    void region_and_number_validation() {
        assertTrue(ValidatorUtils.isValidRegionCode("VN_NORTH"));
        assertTrue(ValidatorUtils.isValidRegionCode("VN_HANOI"));
        assertFalse(ValidatorUtils.isValidRegionCode("vn_north"));   // lowercase

        assertTrue(ValidatorUtils.isPositiveNumber("3.14"));
        assertTrue(ValidatorUtils.isPositiveNumber("100"));
        assertFalse(ValidatorUtils.isPositiveNumber("-5"));
        assertFalse(ValidatorUtils.isPositiveNumber("abc"));
        assertFalse(ValidatorUtils.isPositiveNumber(""));
    }
}
```

6. Update `pom.xml` (root) — Leader sẽ làm bước này nếu chưa add module, hoặc bạn tự add:
   - Tìm dòng `<modules>` và thêm dòng `<module>desktop-admin</module>` nếu chưa có.

7. Build + test:
   ```bash
   mvn -pl desktop-admin -am clean test
   # Kỳ vọng: BUILD SUCCESS, 5 tests run, 0 failures
   ```

8. Commit + push + PR.

### 📦 Deliverables
- [ ] `desktop-admin/pom.xml` (skeleton)
- [ ] `desktop-admin/src/main/java/vn/edu/ves/desktop/util/ValidatorUtils.java` (~50 dòng)
- [ ] `desktop-admin/src/test/java/vn/edu/ves/desktop/util/ValidatorUtilsTest.java` (~55 dòng, 5 @Test methods)

### ✓ Acceptance
- `mvn -pl desktop-admin -am test` → BUILD SUCCESS, 5/5 tests pass
- File `ValidatorUtils.java` có 5 method public + 1 private constructor
- Không có dependency JavaFX (chỉ Java core)

### 💡 Tip
- IntelliJ: right-click file test → Run → tự cài JUnit nếu thiếu
- Nếu `mvn` không có trên PATH: cài Maven https://maven.apache.org/download.cgi và thêm vào PATH
- Coverage cao là điểm cộng → có thể thêm test cho edge case (null, empty, unicode)

---

## 📝 D3. JavaFX About screen (4h, sau Phase 5 skeleton)

### 🎯 Mục tiêu
1 màn JavaFX "Giới thiệu nhóm" — Leader đã có template `LoginController.java` + `login.fxml`, bạn copy/đổi text/đổi nội dung.

### ✅ Prerequisites
- Leader đã đẩy Phase 5 skeleton (có `desktop-admin/src/main/java/.../controller/LoginController.java` + `view/login.fxml`)
- Đã cài Scene Builder (https://gluonhq.com/products/scene-builder/) để edit FXML kéo thả

### 📐 Step-by-step

1. Branch:
   ```bash
   git checkout main && git pull
   git checkout -b d/D3-javafx-about
   ```

2. Copy template:
   ```bash
   cp desktop-admin/src/main/java/vn/edu/ves/desktop/controller/LoginController.java \
      desktop-admin/src/main/java/vn/edu/ves/desktop/controller/AboutController.java

   cp desktop-admin/src/main/resources/view/login.fxml \
      desktop-admin/src/main/resources/view/about.fxml
   ```

3. Edit `AboutController.java`:
   - Đổi tên class: `LoginController` → `AboutController`
   - Xoá các @FXML field về login (username, password, btnLogin)
   - Thêm method khởi tạo hiển thị info:

```java
package vn.edu.ves.desktop.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import java.net.URL;
import java.util.ResourceBundle;

public class AboutController implements Initializable {

    @FXML private Label lblProjectName;
    @FXML private Label lblVersion;
    @FXML private Label lblMembers;
    @FXML private Label lblTech;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lblProjectName.setText("VES-Monitor — Vietnam Energy Security Real-time Monitor");
        lblVersion.setText("Version 1.0.0 (Build " + java.time.LocalDate.now() + ")");
        lblMembers.setText(
            "Thành viên nhóm:\n" +
            "  • A — Leader / Full-stack\n" +
            "  • B — Doc & Project Manager\n" +
            "  • C — Media & QA\n" +
            "  • D — JavaFX UI Junior  ← Bạn\n" +
            "  • E — Android UI Junior\n\n" +
            "GVHD: <tên thầy/cô>\n" +
            "Lớp: IS402.P21"
        );
        lblTech.setText(
            "Công nghệ: Java 11, JavaFX 21, Spring Boot 2.7, Apache Kafka 7.5, Apache Flink 1.17, " +
            "PostgreSQL 15, Docker, JWT.\n\n" +
            "GitHub: https://github.com/<your-org>/Real-time-processing-with-Kafka-Flink-Postgres"
        );
    }
}
```

4. Edit `about.fxml` (mở bằng Scene Builder):
   - Xoá form login cũ
   - Kéo `VBox` (vertical box) làm container
   - Thêm 4 `Label` với `fx:id="lblProjectName"`, `lblVersion`, `lblMembers`, `lblTech`
   - Set `Controller` ở Document → Controller pane → `vn.edu.ves.desktop.controller.AboutController`
   - Thêm `Button` "Close" với onAction = `#onClose`
   - Save

5. Thêm method `onClose` vào `AboutController`:
   ```java
   @FXML
   private void onClose() {
       lblProjectName.getScene().getWindow().hide();
   }
   ```

6. Thêm menu "About" vào Dashboard chính (Leader sẽ chỉ chỗ thêm `MenuItem`):
   ```java
   // Trong DashboardController.java (file Leader đã có)
   @FXML
   private void onAboutClick() {
       FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/about.fxml"));
       Stage stage = new Stage();
       stage.setScene(new Scene(loader.load()));
       stage.setTitle("Giới thiệu — VES-Monitor");
       stage.show();
   }
   ```

7. Chạy test:
   ```bash
   cd desktop-admin
   mvn javafx:run
   # Login admin/admin → menu File → About → kỳ vọng cửa sổ About hiện ra
   ```

8. Chụp screenshot About screen → save vào branch tại `docs/screenshots/javafx-08-about.png`

9. Commit + push + PR.

### 📦 Deliverables
- [ ] `desktop-admin/src/main/java/vn/edu/ves/desktop/controller/AboutController.java`
- [ ] `desktop-admin/src/main/resources/view/about.fxml`
- [ ] Cập nhật `DashboardController.java` thêm menu item (chỉ thêm 1 method, không sửa code Leader)
- [ ] `docs/screenshots/javafx-08-about.png`

### ✓ Acceptance
- Build + run → menu About → cửa sổ hiện ra với 4 Label đầy đủ
- Click Close → cửa sổ đóng, không crash
- Không sửa file controller nào khác của Leader (chỉ thêm 1 method `onAboutClick` ở Dashboard)

---

## 📝 D4. JavaFX Settings screen + theme toggle (5h, sau D3)

### 🎯 Mục tiêu
1 màn Settings cho phép user đổi theme (Light/Dark) + lưu preference vào file local.

### 📐 Step-by-step (rút gọn — pattern tương tự D3)

1. Branch `d/D4-javafx-settings`
2. Copy template như D3 → tạo `SettingsController.java` + `settings.fxml`
3. UI gồm:
   - `ToggleGroup` 2 RadioButton "Light" / "Dark"
   - `Slider` font size (12-20)
   - `Button` "Save" + "Cancel"
4. Logic:
   - Đọc/ghi file `~/.ves-monitor/settings.properties` (Java `Properties` API)
   - Khi Save → apply theme bằng cách load CSS khác nhau:
     ```java
     scene.getStylesheets().clear();
     scene.getStylesheets().add(getClass().getResource("/style/" + theme + ".css").toExternalForm());
     ```
5. Cần 2 CSS file: `style/light.css` + `style/dark.css` (Leader cung cấp `material.css` làm base, bạn fork ra 2 phiên bản)
6. Thêm menu "Settings" tương tự About
7. Test: switch theme → toàn bộ JavaFX UI đổi màu ngay
8. Screenshot `docs/screenshots/javafx-09-settings-light.png` + `javafx-10-settings-dark.png`

### 📦 Deliverables
- [ ] `SettingsController.java` + `settings.fxml`
- [ ] `style/light.css` + `style/dark.css` (fork từ material.css)
- [ ] 2 screenshot light/dark
- [ ] `~/.ves-monitor/settings.properties` được tạo khi Save

### ✓ Acceptance
- Click Save → toàn bộ UI đổi theme ngay (không cần restart app)
- Đóng app, mở lại → theme preference được giữ
- File properties có 2 key: `theme=light|dark`, `fontSize=14`

---

## 📝 D5. CSS polish 4 pillar color + Splash screen (5h, sau Phase 5 done)

### 🎯 Mục tiêu
1. Đổi màu primary của UI theo 4 pillar (cards tab 1 = vàng, tab 2 = xanh dương, tab 3 = cam, tab 4 = xanh lá) để dashboard nhìn rõ pillar nào đang xem
2. Tạo splash screen 2 giây khi mở app

### 📐 Step-by-step (rút gọn)

#### Phần A — CSS polish (2h)

1. Mở `desktop-admin/src/main/resources/style/material.css` (Leader đã có)
2. Thêm rules cho 4 pillar tab:

```css
/* Pillar 1 - Vàng (nguồn cung) */
.tab-pillar-1 { -fx-background-color: #FFE599; }
.tab-pillar-1 .label { -fx-text-fill: #7F6000; }

/* Pillar 2 - Xanh dương (biến động giá) */
.tab-pillar-2 { -fx-background-color: #CFE2F3; }
.tab-pillar-2 .label { -fx-text-fill: #0B5394; }

/* Pillar 3 - Cam (phụ tải) */
.tab-pillar-3 { -fx-background-color: #FCE5CD; }
.tab-pillar-3 .label { -fx-text-fill: #B45F06; }

/* Pillar 4 - Xanh lá (chuyển đổi & MT) */
.tab-pillar-4 { -fx-background-color: #D9EAD3; }
.tab-pillar-4 .label { -fx-text-fill: #38761D; }

/* Security Score gauge color theo status */
.score-secure   { -fx-text-fill: #38761D; -fx-font-weight: bold; }
.score-stable   { -fx-text-fill: #0B5394; -fx-font-weight: bold; }
.score-at-risk  { -fx-text-fill: #B45F06; -fx-font-weight: bold; }
.score-critical { -fx-text-fill: #CC0000; -fx-font-weight: bold; }
```

3. Trong Dashboard FXML (Leader đã tạo), thêm `styleClass="tab-pillar-1"` ... cho 4 tab tương ứng

#### Phần B — Splash screen (3h)

1. Tạo `splash.fxml` với logo + tên app + ProgressBar
2. Tạo `SplashController.java`:
   ```java
   public void initialize() {
       Task<Void> task = new Task<>() {
           @Override
           protected Void call() throws InterruptedException {
               Thread.sleep(2000); // simulate loading
               return null;
           }
       };
       progressBar.progressProperty().bind(task.progressProperty());
       task.setOnSucceeded(e -> {
           // Mở Login screen, đóng splash
           Stage primaryStage = (Stage) progressBar.getScene().getWindow();
           primaryStage.close();
           openLoginScreen();
       });
       new Thread(task).start();
   }
   ```
3. Update `MainApp.java` (Leader có sẵn) → show splash trước login
4. Thêm 1 logo PNG `style/images/logo.png` (D tự design hoặc tải free icon từ flaticon.com — nhớ ghi credit)

### 📦 Deliverables
- [ ] `material.css` updated với 4 pillar color
- [ ] `splash.fxml` + `SplashController.java`
- [ ] `style/images/logo.png`
- [ ] Screenshot dashboard với 4 tab màu

### ✓ Acceptance
- Switch tab → tab active có màu pillar tương ứng
- Mở app → splash 2 giây → tự chuyển sang Login

---

# 👤 NGƯỜI E — Android UI Junior (5 PR, ~20h, simple coding)

> **Định vị:** Biết Java/Kotlin cơ bản. Cài Android Studio + chạy emulator hoặc cắm điện thoại thật. Tasks **không cần Spring Boot / Postgres chạy** — chỉ build APK debug, test giao diện và icon. 2 task đầu (E1, E2) làm được NGAY mà không cần backend.

## 📋 Tổng quan 5 PR của Người E

| PR | Task | Time | Phase phụ thuộc | Có thể bắt đầu khi |
|----|------|------|-----------------|---------------------|
| E1 | Drawable + Icon resources (logo + 4 pillar icons + splash) | 3h | - | NGAY |
| E2 | strings.xml + colors.xml i18n (VN + EN) | 2h | - | NGAY |
| E3 | Android About activity (copy template) | 4h | Phase 6 skeleton | Sau Leader đẩy Phase 6 |
| E4 | Android Settings activity + dark mode toggle | 5h | E3 done | Sau E3 |
| E5 | Postman Collection runner + screenshot bộ ảnh Android | 6h | Phase 4.5 build + Phase 6 done | Sau Phase 6 |

---

## 📝 E1. Drawable + Icon resources (3h, NGAY)

### 🎯 Mục tiêu
Tạo bộ icon + logo Android (PNG/SVG/Vector Drawable) cho 4 pillar + app launcher + splash → đẹp hơn icon default Android Studio.

### ✅ Prerequisites
- Cài Android Studio (https://developer.android.com/studio) — version Hedgehog (2023.1) trở lên
- KHÔNG cần phone, không cần emulator (chỉ làm resource)

### 📐 Step-by-step

1. Tạo skeleton Android folder (Phase 6 chưa có):
   ```bash
   git checkout -b e/E1-android-resources
   mkdir -p android-app/app/src/main/res/drawable
   mkdir -p android-app/app/src/main/res/mipmap-hdpi
   mkdir -p android-app/app/src/main/res/mipmap-xhdpi
   mkdir -p android-app/app/src/main/res/mipmap-xxhdpi
   mkdir -p android-app/app/src/main/res/mipmap-xxxhdpi
   ```

2. **Tải / design 6 icon SVG** (dùng https://www.flaticon.com hoặc https://heroicons.com — chọn free icons, ghi credit):
   - `ic_logo.svg` — logo VES-Monitor (ý tưởng: lightning bolt + shield)
   - `ic_pillar_1.svg` — Nguồn cung (drum/barrel)
   - `ic_pillar_2.svg` — Biến động giá (chart line up/down)
   - `ic_pillar_3.svg` — Phụ tải (lightning bolt)
   - `ic_pillar_4.svg` — Renewable (solar panel hoặc leaf)
   - `ic_alert.svg` — chuông cảnh báo
   - `ic_recommendation.svg` — bóng đèn ý tưởng

3. **Convert SVG → Vector Drawable XML** (Android format):
   - Android Studio → New → Vector Asset → Local file (SVG) → chọn file → Next → Finish
   - Tự động lưu vào `app/src/main/res/drawable/ic_*.xml`

4. **Tạo launcher icon** (ic_launcher.png 4 size):
   - Vào https://easyappicon.com hoặc Android Studio → File → New → Image Asset → Launcher Icons (Adaptive)
   - Foreground: `ic_logo.svg`
   - Background: solid color `#1F77B4` (xanh tone tech)
   - Generate → tự lưu vào 4 folder `mipmap-*`

5. **Tạo splash drawable** `drawable/splash_background.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:drawable="@color/colorPrimary"/>
    <item>
        <bitmap android:gravity="center" android:src="@drawable/ic_logo"/>
    </item>
</layer-list>
```

6. **Color resources** `app/src/main/res/values/colors.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Brand -->
    <color name="colorPrimary">#1F77B4</color>
    <color name="colorPrimaryDark">#155A8A</color>
    <color name="colorAccent">#FF6F00</color>
    <color name="white">#FFFFFF</color>
    <color name="black">#000000</color>

    <!-- 4 Pillar colors -->
    <color name="pillar1_bg">#FFE599</color>
    <color name="pillar1_text">#7F6000</color>
    <color name="pillar2_bg">#CFE2F3</color>
    <color name="pillar2_text">#0B5394</color>
    <color name="pillar3_bg">#FCE5CD</color>
    <color name="pillar3_text">#B45F06</color>
    <color name="pillar4_bg">#D9EAD3</color>
    <color name="pillar4_text">#38761D</color>

    <!-- Status -->
    <color name="status_secure">#38761D</color>
    <color name="status_stable">#0B5394</color>
    <color name="status_at_risk">#B45F06</color>
    <color name="status_critical">#CC0000</color>
</resources>
```

7. Tạo `android-app/CREDITS.md` ghi credit cho icon đã dùng:
```markdown
# Icon Credits
- `ic_pillar_*.svg` — Flaticon, designed by <author>, free for personal use
- `ic_logo.svg` — designed in-house by Người E
```

8. Commit + push + PR.

### 📦 Deliverables
- [ ] `android-app/app/src/main/res/drawable/ic_*.xml` × 7
- [ ] `android-app/app/src/main/res/mipmap-*/ic_launcher.png` × 4 sizes
- [ ] `android-app/app/src/main/res/values/colors.xml`
- [ ] `android-app/app/src/main/res/drawable/splash_background.xml`
- [ ] `android-app/CREDITS.md`

### ✓ Acceptance
- 7 icon vector hiển thị OK trong Android Studio preview
- 4 launcher icon size (48dp/72dp/96dp/144dp/192dp)
- Tất cả icon có credit (không dính bản quyền)

### 💡 Tip
- Dùng Material Symbols (free, đầy đủ): https://fonts.google.com/icons
- Vector Drawable nhẹ hơn PNG nhiều → ưu tiên dùng SVG

---

## 📝 E2. strings.xml + colors.xml i18n (2h, NGAY)

### 🎯 Mục tiêu
Hỗ trợ 2 ngôn ngữ Tiếng Việt (default) + English, theo Android i18n pattern.

### 📐 Step-by-step

1. Branch `e/E2-android-i18n`
2. Tạo `app/src/main/res/values/strings.xml` (Vietnamese default):

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- App-wide -->
    <string name="app_name">VES-Monitor</string>
    <string name="app_tagline">Giám sát An ninh Năng lượng Việt Nam</string>

    <!-- Login -->
    <string name="login_title">Đăng nhập</string>
    <string name="login_username_hint">Tên đăng nhập</string>
    <string name="login_password_hint">Mật khẩu</string>
    <string name="login_button">Đăng nhập</string>
    <string name="login_error_empty">Vui lòng nhập đầy đủ thông tin</string>
    <string name="login_error_invalid">Sai tên đăng nhập hoặc mật khẩu</string>

    <!-- Bottom nav -->
    <string name="nav_pillar1">Nguồn cung</string>
    <string name="nav_pillar2">Giá NL</string>
    <string name="nav_pillar3">Phụ tải</string>
    <string name="nav_pillar4">Renewable</string>
    <string name="nav_alerts">Cảnh báo</string>

    <!-- Home -->
    <string name="home_security_score">Điểm An ninh Năng lượng</string>
    <string name="home_active_alerts">Cảnh báo đang hoạt động</string>
    <string name="home_active_recs">Khuyến nghị chờ xử lý</string>

    <!-- Alerts -->
    <string name="alerts_empty">Không có cảnh báo nào đang hoạt động</string>
    <string name="alert_severity_critical">NGHIÊM TRỌNG</string>
    <string name="alert_severity_warning">CẢNH BÁO</string>
    <string name="alert_severity_info">THÔNG TIN</string>

    <!-- Recommendations -->
    <string name="rec_acknowledge">Xác nhận</string>
    <string name="rec_dismiss">Bỏ qua</string>
    <string name="rec_ack_success">Đã xác nhận khuyến nghị</string>

    <!-- About -->
    <string name="about_title">Giới thiệu</string>
    <string name="about_version">Phiên bản</string>
    <string name="about_team">Đội ngũ phát triển</string>

    <!-- Settings -->
    <string name="settings_title">Cài đặt</string>
    <string name="settings_dark_mode">Chế độ tối</string>
    <string name="settings_language">Ngôn ngữ</string>
    <string name="settings_server_url">Địa chỉ server</string>
</resources>
```

3. Tạo `app/src/main/res/values-en/strings.xml` (English fallback):

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">VES-Monitor</string>
    <string name="app_tagline">Vietnam Energy Security Monitor</string>

    <string name="login_title">Sign In</string>
    <string name="login_username_hint">Username</string>
    <string name="login_password_hint">Password</string>
    <string name="login_button">Sign In</string>
    <string name="login_error_empty">Please fill all fields</string>
    <string name="login_error_invalid">Invalid username or password</string>

    <string name="nav_pillar1">Supply</string>
    <string name="nav_pillar2">Prices</string>
    <string name="nav_pillar3">Grid Load</string>
    <string name="nav_pillar4">Renewable</string>
    <string name="nav_alerts">Alerts</string>

    <string name="home_security_score">Energy Security Score</string>
    <string name="home_active_alerts">Active Alerts</string>
    <string name="home_active_recs">Pending Recommendations</string>

    <string name="alerts_empty">No active alerts</string>
    <string name="alert_severity_critical">CRITICAL</string>
    <string name="alert_severity_warning">WARNING</string>
    <string name="alert_severity_info">INFO</string>

    <string name="rec_acknowledge">Acknowledge</string>
    <string name="rec_dismiss">Dismiss</string>
    <string name="rec_ack_success">Recommendation acknowledged</string>

    <string name="about_title">About</string>
    <string name="about_version">Version</string>
    <string name="about_team">Development Team</string>

    <string name="settings_title">Settings</string>
    <string name="settings_dark_mode">Dark mode</string>
    <string name="settings_language">Language</string>
    <string name="settings_server_url">Server URL</string>
</resources>
```

4. `app/src/main/res/values/themes.xml` (dùng Material 3):

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.VESMonitor" parent="Theme.Material3.DayNight">
        <item name="colorPrimary">@color/colorPrimary</item>
        <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
        <item name="colorAccent">@color/colorAccent</item>
    </style>

    <style name="Theme.VESMonitor.Splash" parent="Theme.VESMonitor">
        <item name="android:windowBackground">@drawable/splash_background</item>
        <item name="android:windowNoTitle">true</item>
        <item name="android:windowFullscreen">true</item>
    </style>
</resources>
```

5. Commit + push + PR.

### 📦 Deliverables
- [ ] `app/src/main/res/values/strings.xml` (30+ string keys)
- [ ] `app/src/main/res/values-en/strings.xml` (cùng 30+ key)
- [ ] `app/src/main/res/values/themes.xml`

### ✓ Acceptance
- Tất cả 30+ key có cả VN + EN
- Không có key trùng giữa 2 file
- Build skeleton APK → không lỗi resource

---

## 📝 E3. Android About activity (4h, sau Phase 6 skeleton)

### 🎯 Mục tiêu
Tương tự D3 nhưng cho Android: copy template `MainActivity` của Leader → tạo `AboutActivity`.

### 📐 Step-by-step (rút gọn)

1. Branch `e/E3-android-about`
2. Copy `LoginActivity.java` + `activity_login.xml` → `AboutActivity.java` + `activity_about.xml`
3. UI gồm:
   - `ImageView` logo (`@drawable/ic_logo`)
   - `TextView` lblProjectName
   - `TextView` lblVersion
   - `RecyclerView` (hoặc đơn giản `LinearLayout`) list 5 thành viên
   - `Button` "Đóng"
4. Logic: tương tự D3 (hard-code thông tin)
5. Đăng ký activity trong `AndroidManifest.xml`:
   ```xml
   <activity android:name=".AboutActivity"
             android:label="@string/about_title"
             android:parentActivityName=".MainActivity"/>
   ```
6. Thêm menu vào toolbar (Leader chỉ chỗ):
   ```kotlin
   override fun onOptionsItemSelected(item: MenuItem): Boolean {
       if (item.itemId == R.id.action_about) {
           startActivity(Intent(this, AboutActivity::class.java))
           return true
       }
       return super.onOptionsItemSelected(item)
   }
   ```
7. Test trên emulator → menu About → activity hiện ra
8. Screenshot → `docs/screenshots/android-06-about.png`

### 📦 Deliverables
- [ ] `AboutActivity.java/kt`
- [ ] `activity_about.xml`
- [ ] Update `AndroidManifest.xml` + menu XML
- [ ] `docs/screenshots/android-06-about.png`

### ✓ Acceptance
- Tap menu About từ MainActivity → AboutActivity mở
- Hiển thị đầy đủ 5 thành viên + version + GitHub link
- Back button hoạt động đúng (về MainActivity)

---

## 📝 E4. Android Settings activity + dark mode toggle (5h, sau E3)

### 🎯 Mục tiêu
Settings cho phép user toggle dark mode + thay đổi server URL (cho dev/prod env switch).

### 📐 Step-by-step (rút gọn)

1. Branch `e/E4-android-settings`
2. Tạo `SettingsActivity.java` + `activity_settings.xml`
3. UI:
   - `SwitchMaterial` "Dark mode"
   - `EditText` "Server URL" (default `http://10.0.2.2:8090`)
   - `Spinner` "Language" (VN / EN)
   - `Button` "Save"
4. Lưu vào `SharedPreferences`:
   ```kotlin
   getSharedPreferences("ves_settings", MODE_PRIVATE).edit().apply {
       putBoolean("dark_mode", switchDark.isChecked)
       putString("server_url", edtUrl.text.toString())
       putString("language", spinnerLang.selectedItem.toString())
       apply()
   }
   ```
5. Apply dark mode:
   ```kotlin
   AppCompatDelegate.setDefaultNightMode(
       if (isDark) AppCompatDelegate.MODE_NIGHT_YES
       else AppCompatDelegate.MODE_NIGHT_NO
   )
   ```
6. Apply language (cần recreate activity):
   ```kotlin
   val config = resources.configuration
   config.setLocale(Locale(if (lang == "VN") "vi" else "en"))
   createConfigurationContext(config)
   recreate()
   ```
7. Test: toggle dark → toàn app đổi màu; switch VN→EN → strings đổi
8. Screenshot 2 ảnh: light + dark

### 📦 Deliverables
- [ ] `SettingsActivity.java/kt`
- [ ] `activity_settings.xml`
- [ ] Cập nhật `AndroidManifest.xml`
- [ ] 2 screenshot

### ✓ Acceptance
- Toggle dark → toàn app đổi màu, restart app vẫn giữ
- Server URL save → next login dùng URL mới (API connect đúng host)
- Switch language → string tự đổi sang ngôn ngữ tương ứng

---

## 📝 E5. Postman runner + bộ screenshot Android (6h, sau Phase 6)

### 🎯 Mục tiêu
1. Mở rộng Postman collection của C4: thêm "Pre-request script" để auto-login + "Tests script" để assert response → có thể "Run Collection" 14/14 pass
2. Bộ screenshot 5 màn Android dùng cho slide + báo cáo Chương 3

### 📐 Step-by-step (rút gọn)

#### Phần A — Postman runner (3h)

1. Branch `e/E5-postman-runner`
2. Import file `tests/postman/ves-monitor-api.postman_collection.json` của C4
3. Mở từng request → tab "Tests" → thêm assertion:

```javascript
pm.test("Status code is 200 or 201", () => {
    pm.expect(pm.response.code).to.be.oneOf([200, 201]);
});

pm.test("Response has JSON body", () => {
    pm.response.to.be.json;
});

// Endpoint-specific (ví dụ /api/security/score)
pm.test("Score is between 0 and 100", () => {
    const json = pm.response.json();
    pm.expect(json.overallScore).to.be.within(0, 100);
});
```

4. Click "Run Collection" (Runner) → chạy tuần tự 14 request → kỳ vọng 14/14 pass
5. Export collection mới → overwrite `tests/postman/ves-monitor-api.postman_collection.json`
6. Update `tests/postman/README.md` thêm section "Run Collection":

```markdown
## Run Collection (CI-style automated test)
1. Top of Postman → click "Runner"
2. Drag "VES-Monitor API" collection vào panel phải
3. Click "Run VES-Monitor API"
4. Kỳ vọng: 14/14 pass (mỗi request có ít nhất 1 assertion)
5. Có thể schedule chạy hàng giờ qua Postman Monitor (free 1000 run/tháng)
```

#### Phần B — Screenshot 5 màn Android (3h)

1. Chạy Phase 6 app trên emulator Android
2. Capture từng màn (theo C3 style):
   - `android-01-login.png` — Login screen
   - `android-02-home.png` — Bottom-nav, 4 KPI card
   - `android-03-chart.png` — Chart pillar 2 line
   - `android-04-alerts.png` — RecyclerView list 5 alerts
   - `android-05-recommendation.png` — Recommendation detail với ACK button
3. Save vào `docs/screenshots/android-*.png`

### 📦 Deliverables
- [ ] `tests/postman/ves-monitor-api.postman_collection.json` (updated với 14+ assertion)
- [ ] `tests/postman/README.md` (updated)
- [ ] `docs/screenshots/android-XX-*.png` × 5

### ✓ Acceptance
- Postman Runner: 14/14 request pass, 28+ assertion pass (2/request)
- 5 screenshot Android đầy đủ, ≥ 720p

---

# 📅 TIMELINE TỔNG — 10 TUẦN

Lưu ý: timeline này điều phối 4 thành viên (B/C/D/E) song song với Leader. Leader bám lộ trình `UPGRADE_PLAN.md §24.5` cho phần code chính.

| Tuần | Leader (A) làm gì | B làm gì | C làm gì | D làm gì | E làm gì |
|------|-------------------|----------|----------|----------|----------|
| 1 | Phase 0+1 (foundation+docker) | B1 (vẽ diagram) | - | D1 (SQL seed VN) | E1 (Android icons) |
| 2 | Phase 2+2.5 (schema+pillars) | B2 (Chương 1) | - | D2 (ValidatorUtils) | E2 (i18n strings) |
| 3 | Phase 2.6+3 (security+Flink) | B3 (Chương 2 — cần B1 done) | - | (đợi Phase 5 skel) | (đợi Phase 6 skel) |
| 4 | Phase 4 (generators) | B4 (Chương 4) | - | (đợi Phase 5 skel) | (đợi Phase 6 skel) |
| 5 | Phase 4.5 (REST API — đang ở đây ✅) | B5 (Chương 6 + AI Log) | C1 (slide) | (đợi Phase 5 skel) | (đợi Phase 6 skel) |
| 6 | **Phase 5 (JavaFX) — bắt đầu** | review báo cáo | C3 screenshot Swagger | D3 (About JavaFX) | (đợi Phase 6) |
| 7 | **Phase 5 tiếp + Phase 6 bắt đầu** | format báo cáo final | C3 screenshot JavaFX | D4 (Settings JavaFX) | E3 (About Android) |
| 8 | **Phase 6 tiếp + Phase 7 deploy** | proofreading | C2 quay video, C3 screenshot Android | D5 (CSS + Splash) | E4 (Settings Android) |
| 9 | Phase 8 doc + integration test | combine + export PDF | C4 + C5 (test report) | review final | E5 (Postman + screenshots) |
| 10 | Pre-build artifacts + Demo dress rehearsal | nộp báo cáo | diễn tập demo | demo dress rehearsal | demo dress rehearsal |

---

# 📊 TỔNG KẾT WORKLOAD

| Người | Workload | Số PR | Loại task | Phụ thuộc |
|-------|----------|-------|-----------|-----------|
| **Leader A** | ~80h | 30-40 | Toàn bộ backend/Flink/UI core/deploy | - |
| **Người B** | ~30h | 5 | Báo cáo Chương 1/2/4/6 + diagram + Notion | B1 chỉ cần Phase 2; B5 cần Phase 4 |
| **Người C** | ~25h | 5 | Slide + video + screenshots + Postman + test report | C2/C3/C5 chờ Phase 5+6+7 |
| **Người D** | ~20h | 5 | SQL seed + JUnit + 2 màn JavaFX + CSS | D3/D4/D5 chờ Phase 5 skeleton |
| **Người E** | ~20h | 5 | Icon + i18n + 2 màn Android + Postman runner | E3/E4 chờ Phase 6 skeleton |
| **TỔNG** | ~175h | ~55 PR | | |

---

# ❓ FAQ — Hỏi đáp thường gặp

### Q1. Tôi không biết Java có làm task D/E được không?
- D1, D2: chỉ cần biết SQL `INSERT` + Java cơ bản (class/method). Có thể học trong 1 ngày qua tutorial.
- D3, D4, D5: copy template Leader cung cấp, chỉ đổi text/màu. Nếu vẫn không tự tin, đổi sang task B hoặc C.

### Q2. Phụ thuộc Phase nào? Khi nào tôi mới start được?
- Mỗi task có bảng "Phase phụ thuộc" rõ. Nếu task của bạn chờ → làm task khác trước.
- Cứ 2 tuần Leader sẽ thông báo "Phase X done, các bạn D/E start được task Y".

### Q3. Tôi commit code sai, hỏng repo, làm sao?
- Đừng panic. **Báo Leader ngay** qua chat group + comment vào PR `@leader help`.
- Leader sẽ `git revert` hoặc `git reset --hard <last-good-commit>`.
- Tuyệt đối **không** `git push -f` để cố "fix".

### Q4. Tôi không kịp deadline, làm sao?
- Báo Leader trước deadline T-2 ngày.
- Leader sẽ:
  - Cắt scope task (làm 50% còn lại sau)
  - Hoặc Leader làm thay (ghi nhận trong commit)
  - Hoặc rời task sang tuần sau (nếu không trên đường găng)

### Q5. Tôi có thể đổi task với người khác không?
- Có thể, nhưng phải báo Leader trước. Phải đảm bảo 2 người đều OK + không vỡ timeline.

### Q6. Tôi nên dùng AI tool nào?
- **Cursor AI** (https://cursor.com): IDE có AI, free 14 ngày trial. Tốt nhất cho coder (D/E).
- **ChatGPT** free: tốt cho viết báo cáo (B), viết kịch bản (C).
- **Claude.ai** free: tốt cho phân tích/edit text dài.
- **GitHub Copilot**: free cho sinh viên (https://education.github.com).
- **Luôn ghi lại prompt vào AI Usage Log** (B5 task) — yêu cầu §5 Java requirement.

### Q7. PR của tôi bị Leader request changes, có sao không?
- Bình thường — code review tốt phải có feedback. Sửa theo comment, push commit mới, comment "@leader done" → Leader review lại.

### Q8. Tôi muốn làm thêm task ngoài 5 PR được giao?
- Tuyệt vời! Báo Leader → có thể nhận task "stretch goal" trong `UPGRADE_PLAN.md §24.10` (LLM Insight, PDF export, Metabase polish).
- Hoặc giúp test các Phase Leader đang làm dở.

---

# 📞 LIÊN HỆ KHẨN

| Vấn đề | Liên hệ | Cách |
|--------|---------|------|
| Code bị bug, không tự fix được | Leader A | GitHub Issue + tag `@leader help` |
| Không hiểu task | Persona owner cũ | Đọc lại file này + Google + AI |
| Repo bị conflict / hỏng | Leader A | Group chat (Zalo/Discord) — gấp |
| Deadline gần, sợ không kịp | Leader A | Báo T-2 ngày qua chat |
| Tool/IDE không cài được | Persona còn lại | Search Stack Overflow + AI |

---

> **Mỗi thành viên đọc kỹ section của mình + bookmark file này.** Khi xong task, mark ✅ vào checklist trong PR description để Leader biết review.
>
> File này là **single source of truth** cho phân công team. Khi có thay đổi (scope cắt thêm, đổi người), Leader sẽ update và thông báo trong group chat.

