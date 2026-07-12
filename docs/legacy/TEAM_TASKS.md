# 👥 TEAM TASKS — Phân công chi tiết cho ĐỒ ÁN MÔN JAVA

> 📌 **CẬP NHẬT 2026-05-12**: Đồ án **Android tách thành đồ án độc lập** (môn Mobile, repo Android riêng, team chỉ Leader + 1 Android Dev) — xem **[`docs/ANDROID_ONBOARDING.md`](./ANDROID_ONBOARDING.md)**.
>
> **File này chỉ phân công cho đồ án MÔN JAVA**, team **5 người**: Leader + 4 helpers (2 coders + 2 non-coders). Mỗi thành viên có **5 PR thật** + step-by-step để người mới vào nghề / non-coder vẫn làm được mà không hỏi Leader nhiều.
>
> Persona E redesign từ "Android UI Junior" → **"Backend & Test Helper"** (5 task Java thay vì Android).

---

## 🎯 Sơ đồ team & workload

| Vai trò | Người | Skill cần | Workload | PR | Loại task |
|---------|-------|-----------|----------|-----|-----------|
| **Leader / Full-stack** | A | Tất cả | ~75-85h | 30-40 PR | Backend + Flink + JavaFX core + Deploy + Integration |
| **Doc & PM** | **B** | Word, draw.io, viết báo cáo | ~30h | **5 PR** | Báo cáo + diagram + project management |
| **Media & QA** | **C** | PowerPoint, Screen Recorder, Postman | ~25h | **5 PR** | Slide + video + test report + screenshots |
| **JavaFX UI Junior** | **D** | Java cơ bản, IntelliJ | ~20h | **5 PR** | 2 màn JavaFX phụ + JUnit test + CSS polish + SQL seed |
| **Backend & Test Helper** | **E** | Java + SQL cơ bản, đọc được Spring Boot | ~18h | **5 PR** | JUnit test backend utility + SQL stress data + Swagger doc + curl/JMeter scripts |

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

# 👤 NGƯỜI E — Backend & Test Helper (5 PR, ~18h, simple coding)

> **Định vị:** Biết Java cơ bản + SQL cơ bản. Đọc được code Spring Boot (không cần tự viết controller mới từ scratch). Không cần Docker chạy — phần lớn task là viết file Java/SQL/shell, Leader sẽ apply/build sau. 3 task đầu (E1, E2, E3) làm được NGAY mà không cần backend chạy.
>
> 🔄 **Persona này đã đổi từ "Android UI Junior" → "Backend & Test Helper"** sau khi đồ án Android tách thành đồ án riêng (xem `docs/ANDROID_ONBOARDING.md`).

## 📋 Tổng quan 5 PR của Người E

| PR | Task | Time | Phase phụ thuộc | Có thể bắt đầu khi |
|----|------|------|-----------------|---------------------|
| E1 | SQL stress test seed data (~150 row đa pillar) | 3h | Phase 2.5 done ✅ | NGAY |
| E2 | JUnit tests cho backend-api utility (JwtTokenProvider + ApiException + Validator) | 4h | Phase 4.5 code-complete ✅ | NGAY (code đã có) |
| E3 | Swagger OpenAPI @Schema examples + descriptions cho 14 endpoint | 3h | Phase 4.5 code-complete ✅ | NGAY |
| E4 | Curl example scripts cho 14 endpoint + pretty JSON | 4h | Phase 4.5 BUILD done | Sau khi backend chạy được |
| E5 | Load test script (curl loop) + LOAD_TEST_REPORT.md | 4h | Phase 4.5 BUILD + Phase 7 tunnel | Sau Phase 7 |

---

## 📝 E1. SQL stress test seed data (3h, NGAY)

### 🎯 Mục tiêu
Tạo file SQL `infra/script/11_stress_seed.sql` chứa **~150 record fake giàu data** đa pillar — giúp chart trên dashboard không bị "trống trải" khi demo. Khác task D1 (D1 = base seed 38 row VN thực tế). E1 = stress data làm dày chart timeline (mỗi pillar 30-50 row trải qua 7 ngày gần đây).

### ✅ Prerequisites
- Đọc qua `infra/script/06_seed_pillars.sql` và `08_seed_security_features.sql` để hiểu schema bảng `fuel_prices_raw`, `grid_load_raw`, `renewable_output_raw`, `emission_raw`
- KHÔNG cần Postgres chạy — chỉ viết file SQL đúng cú pháp

### 📐 Step-by-step

1. Branch:
   ```bash
   git checkout main && git pull
   git checkout -b e/E1-stress-seed
   ```

2. Tạo file `infra/script/11_stress_seed.sql`. Cấu trúc 4 phần:

```sql
-- =============================================================
-- 11_stress_seed.sql  (Phase 24+ extension)
-- Tác giả: Người E
-- Mục đích: bơm ~150 record fake trải dài 7 ngày qua → chart dày
-- Idempotent: KHÔNG dùng ON CONFLICT (vì id auto), ý là chạy 1 lần
-- =============================================================

-- ---------- Part 1: Fuel prices (Pillar 2) — 40 row × 4 fuel × 7 ngày = ngẫu nhiên 40 row ----------
-- Sử dụng generate_series + random() để fake giá Brent/WTI/Gasoline
INSERT INTO fuel_prices_raw (event_timestamp, fuel_type, price, price_unit, location, region, source)
SELECT
    NOW() - (random() * INTERVAL '7 days') AS event_timestamp,
    (ARRAY['BRENT_CRUDE', 'WTI_CRUDE', 'GASOLINE', 'DIESEL'])[1 + floor(random() * 4)::int] AS fuel_type,
    CASE
        WHEN random() < 0.5 THEN 75 + random() * 20  -- crude: 75-95 USD
        ELSE 2.5 + random() * 1.5                    -- retail: 2.5-4 USD/gal
    END AS price,
    'USD_per_BARREL_OR_GALLON',
    'Global Market',
    'WORLD_BANK',
    'STRESS_SEED'
FROM generate_series(1, 40);

-- ---------- Part 2: Grid load (Pillar 3) — 35 row, 6 region × ~6 timeslot ----------
INSERT INTO grid_load_raw (region_code, load_mw, capacity_mw, is_peak_hour, event_time)
SELECT
    region.code,
    (random() * (region.cap - region.cap * 0.4) + region.cap * 0.4)::numeric(10,2) AS load_mw,
    region.cap AS capacity_mw,
    EXTRACT(HOUR FROM (NOW() - random() * INTERVAL '7 days')) BETWEEN 18 AND 22 AS is_peak_hour,
    NOW() - (random() * INTERVAL '7 days') AS event_time
FROM (
    SELECT 'VN_HANOI' AS code, 12000 AS cap UNION ALL
    SELECT 'VN_HAIPHONG', 5000 UNION ALL
    SELECT 'VN_DANANG', 4500 UNION ALL
    SELECT 'VN_QUANGNINH', 7000 UNION ALL
    SELECT 'VN_NINHTHUAN', 2500 UNION ALL
    SELECT 'VN_BINHTHUAN', 2000
) region
CROSS JOIN generate_series(1, 6) AS slot;

-- ---------- Part 3: Renewable output (Pillar 4) — 30 row mix solar/wind/hydro ----------
INSERT INTO renewable_output_raw (region_code, source_type, output_mw, capacity_mw, event_time)
SELECT
    region.code,
    source.type,
    (random() * source.cap * 0.9)::numeric(10,2) AS output_mw,
    source.cap AS capacity_mw,
    NOW() - (random() * INTERVAL '7 days') AS event_time
FROM (
    SELECT 'VN_NINHTHUAN' AS code UNION ALL
    SELECT 'VN_BINHTHUAN' UNION ALL
    SELECT 'VN_QUANGNINH'
) region
CROSS JOIN (
    SELECT 'SOLAR' AS type, 1500 AS cap UNION ALL
    SELECT 'WIND', 500 UNION ALL
    SELECT 'HYDRO', 400
) source
CROSS JOIN generate_series(1, 4);

-- ---------- Part 4: Emission (Pillar 4) — 30 row đa region ----------
INSERT INTO emission_raw (region_code, co2_kg, energy_mwh, event_time)
SELECT
    (ARRAY['VN_HANOI', 'VN_HAIPHONG', 'VN_DANANG', 'VN_QUANGNINH'])[1 + floor(random() * 4)::int] AS region_code,
    (random() * 5000 + 1000)::numeric(12,2) AS co2_kg,
    (random() * 10 + 2)::numeric(8,2) AS energy_mwh,
    NOW() - (random() * INTERVAL '7 days') AS event_time
FROM generate_series(1, 30);

-- ---------- Part 5: Sample dismissed/acknowledged recommendations (cho audit log) ----------
UPDATE recommendations
   SET status = 'ACKNOWLEDGED',
       acknowledged_at = NOW() - INTERVAL '1 day',
       acknowledged_by = (SELECT id FROM users WHERE username = 'admin'),
       note = 'Test acknowledge từ stress seed E1'
 WHERE id IN (SELECT id FROM recommendations WHERE status = 'PENDING' ORDER BY id LIMIT 1)
   AND EXISTS (SELECT 1 FROM recommendations WHERE status = 'PENDING');

-- ---------- Verify ----------
SELECT 'fuel_prices_raw' AS tbl, COUNT(*) AS n FROM fuel_prices_raw WHERE source = 'STRESS_SEED'
UNION ALL SELECT 'grid_load_raw', COUNT(*) FROM grid_load_raw WHERE event_time > NOW() - INTERVAL '7 days'
UNION ALL SELECT 'renewable_output_raw', COUNT(*) FROM renewable_output_raw WHERE event_time > NOW() - INTERVAL '7 days'
UNION ALL SELECT 'emission_raw', COUNT(*) FROM emission_raw WHERE event_time > NOW() - INTERVAL '7 days';
-- Kỳ vọng: ≥ 130 row tổng (40 + 36 + 36 + 30)
```

3. **Verify cú pháp** (online): paste vào https://www.db-fiddle.com/ chọn PostgreSQL 15 → kiểm tra syntax.

4. **Test idempotent** (nếu Leader đang chạy DB):
   ```bash
   docker exec -i postgres-database psql -U postgres -d fuel_prices < infra/script/11_stress_seed.sql
   # Kỳ vọng "INSERT 0 40", "INSERT 0 36", v.v.
   # Chạy lại lần 2 → kỳ vọng cũng OK (vì không có UNIQUE constraint, chỉ thêm row mới)
   ```

5. Commit + push + PR.

### 📦 Deliverables
- [ ] `infra/script/11_stress_seed.sql` (~80-100 dòng SQL)
- [ ] Comment ngắn ở đầu file giải thích phương pháp (random data, vì sao 7 ngày)

### ✓ Acceptance
- File chạy không lỗi syntax (Leader sẽ apply để verify)
- Bơm ≥ 130 row tổng cộng vào 4 bảng
- Verify SELECT cuối file trả đúng số lượng

### 💡 Tip
- `generate_series(1, N)` + `CROSS JOIN` là pattern Postgres để fake N row mà không cần loop
- `random() * INTERVAL '7 days'` cho timestamp ngẫu nhiên trong 7 ngày qua
- Cẩn thận `numeric(10,2)` không bị tràn — kiểm tra schema bảng trước

---

## 📝 E2. JUnit tests cho backend-api utility (4h, NGAY)

### 🎯 Mục tiêu
Viết **3 file test JUnit 5** cho 3 utility class trong module `backend-api/`. Backend đã code-complete (Phase 4.5), bạn chỉ viết test — không sửa code chính.

### ✅ Prerequisites
- Cài JDK 11 (Adoptium Temurin)
- Cài IntelliJ IDEA Community
- Đọc qua 3 file để hiểu logic:
  - `backend-api/src/main/java/vn/edu/ves/api/config/JwtTokenProvider.java`
  - `backend-api/src/main/java/vn/edu/ves/api/exception/ApiException.java`
  - `backend-api/src/main/java/vn/edu/ves/api/exception/GlobalExceptionHandler.java`

### 📐 Step-by-step

1. Branch `e/E2-backend-tests`

2. Tạo thư mục test:
   ```bash
   mkdir -p backend-api/src/test/java/vn/edu/ves/api/config
   mkdir -p backend-api/src/test/java/vn/edu/ves/api/exception
   ```

3. Tạo `backend-api/src/test/java/vn/edu/ves/api/config/JwtTokenProviderTest.java`:

```java
package vn.edu.ves.api.config;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;

    @BeforeEach
    void setup() {
        provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "secret",
            "test-secret-with-at-least-256-bits-of-randomness-for-hs256-yes-this-is-long");
        ReflectionTestUtils.setField(provider, "expirationSeconds", 3600L);
        provider.init();
    }

    @Test
    void generate_and_parse_roundtrip() {
        String token = provider.generateToken("admin", 1L, "ADMIN");
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3, "JWT must have 3 parts");

        Claims claims = provider.parseToken(token);
        assertEquals("admin", claims.getSubject());
        assertEquals(1, ((Number) claims.get("userId")).longValue());
        assertEquals("ADMIN", claims.get("role"));
    }

    @Test
    void parseSilently_invalid_returns_null() {
        Claims result = provider.parseSilently("not.a.valid.token");
        assertNull(result);
    }

    @Test
    void parseSilently_empty_returns_null() {
        assertNull(provider.parseSilently(null));
        assertNull(provider.parseSilently(""));
    }

    @Test
    void expirationSeconds_returns_configured_value() {
        assertEquals(3600L, provider.getExpirationSeconds());
    }
}
```

4. Tạo `backend-api/src/test/java/vn/edu/ves/api/exception/ApiExceptionTest.java`:

```java
package vn.edu.ves.api.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class ApiExceptionTest {

    @Test
    void badRequest_returns_400() {
        ApiException ex = ApiException.badRequest("Invalid input");
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Invalid input", ex.getMessage());
    }

    @Test
    void unauthorized_returns_401() {
        ApiException ex = ApiException.unauthorized("Token expired");
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }

    @Test
    void forbidden_returns_403() {
        ApiException ex = ApiException.forbidden("Role insufficient");
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void notFound_returns_404() {
        ApiException ex = ApiException.notFound("Resource not found");
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void conflict_returns_409() {
        ApiException ex = ApiException.conflict("Duplicate");
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }
}
```

5. Tạo `backend-api/src/test/java/vn/edu/ves/api/exception/GlobalExceptionHandlerTest.java`:

```java
package vn.edu.ves.api.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleApiException_returns_standard_format() {
        ApiException ex = ApiException.badRequest("Missing field");
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/test");

        ResponseEntity<Map<String, Object>> resp = handler.handleApiException(ex, req);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        Map<String, Object> body = resp.getBody();
        assertNotNull(body);
        assertEquals(400, body.get("status"));
        assertEquals("Missing field", body.get("message"));
        assertEquals("/api/test", body.get("path"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    void handleGenericException_returns_500() {
        RuntimeException ex = new RuntimeException("DB connection lost");
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/login");

        ResponseEntity<Map<String, Object>> resp = handler.handleGenericException(ex, req);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertEquals(500, resp.getBody().get("status"));
    }
}
```

6. Build + test:
   ```bash
   # Cần backend-api build được trước (Phase 4.5 BUILD pending — nhưng test có thể chạy độc lập sau khi mvn dependency resolve)
   mvn -pl backend-api -am test -Dtest='JwtTokenProviderTest,ApiExceptionTest,GlobalExceptionHandlerTest'
   # Kỳ vọng: BUILD SUCCESS, 12 tests run, 0 failures
   ```

   > ⚠️ Nếu Maven proxy fail (NTLM Bosch): bạn không build local được. Push PR lên — Leader sẽ build trên máy có hotspot 4G hoặc CI.

7. Commit + push + PR.

### 📦 Deliverables
- [ ] `backend-api/src/test/java/.../config/JwtTokenProviderTest.java` (4 @Test, ~50 dòng)
- [ ] `backend-api/src/test/java/.../exception/ApiExceptionTest.java` (5 @Test, ~30 dòng)
- [ ] `backend-api/src/test/java/.../exception/GlobalExceptionHandlerTest.java` (2 @Test, ~40 dòng)

### ✓ Acceptance
- 3 file test, 11 @Test methods tổng cộng
- Tất cả test pass (Leader build verify)
- Không sửa file source chính (chỉ thêm test)

### 💡 Tip
- Dùng `ReflectionTestUtils.setField` để inject value vào field private `@Value` (vì test không qua Spring context)
- `MockHttpServletRequest` từ `spring-test` đã có sẵn trong Spring Boot starter test

---

## 📝 E3. Swagger @Schema examples cho 14 endpoint (3h, NGAY)

### 🎯 Mục tiêu
Thêm annotation `@Schema(example = "...", description = "...")` vào **14 DTO** trong `backend-api/` + thêm `@Operation(summary = "..., description = ...)` cho 14 endpoint → Swagger UI hiển thị example value cụ thể (không phải `string`, `0` mặc định), dễ đọc cho người chấm + người consume API.

### ✅ Prerequisites
- Đã có `backend-api/` (Phase 4.5 code-complete)
- KHÔNG cần backend chạy — chỉ sửa annotation source code

### 📐 Step-by-step

1. Branch `e/E3-swagger-examples`

2. Mở file `backend-api/src/main/java/vn/edu/ves/api/dto/LoginRequest.java`, thêm import + annotation:

```java
package vn.edu.ves.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;

@Schema(description = "Request body cho đăng nhập")
public class LoginRequest {

    @Schema(description = "Tên đăng nhập", example = "admin", required = true)
    @NotBlank
    private String username;

    @Schema(description = "Mật khẩu", example = "admin", required = true)
    @NotBlank
    private String password;

    // getters/setters giữ nguyên
}
```

3. Lặp lại cho **13 DTO khác** (mỗi DTO thêm `@Schema` ở class + mỗi field):

| File | Example values gợi ý |
|------|----------------------|
| `LoginResponse.java` | `accessToken: "eyJhbGc..."`, `expiresIn: 28800` |
| `UserDto.java` | `username: "admin"`, `role: "ADMIN"` |
| `SecurityScoreDto.java` | `overallScore: 76.4`, `status: "STABLE"`, `trend: "DECREASING"` |
| `CascadeRiskDto.java` | `pillarCount: 3`, `description: "..."` |
| `Pillar1OutlookDto.java` | `regionCode: "VN_HANOI"`, `stockDays: 56.6`, `supplyStatus: "WARNING"` |
| `Pillar2VolatilityDto.java` | `fuelType: "BRENT_CRUDE"`, `priceChangePct: 12.3` |
| `Pillar3SheddingDto.java` | `regionCode: "VN_NORTH"`, `loadPct: 88.5`, `recommendedShedMw: 800` |
| `Pillar4NetZeroDto.java` | `targetYear: 2050`, `progressPct: 23.5` |
| `RecommendationDto.java` | `pillar: 1`, `severity: "WARNING"`, `status: "PENDING"` |
| `AckRequest.java` | `note: "Đã chuyển stock theo plan"` |
| `AlertDto.java` | `metricType: "GRID_LOAD_PCT"`, `severity: "CRITICAL"` |
| `FuelPriceDto.java` | `fuelType: "BRENT_CRUDE"`, `price: 85.50` |
| `GridLoadLatestDto.java` | `regionCode: "VN_HANOI"`, `loadPct: 88.5` |

4. Mở 7 file controller, thêm `@Operation` cho mỗi method, ví dụ `PillarController.java`:

```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Pillars", description = "Endpoints cho 4 trụ cột an ninh năng lượng")
@RestController
@RequestMapping("/api/pillars")
public class PillarController {

    @Operation(
        summary = "Lấy outlook Pillar 1 — Nguồn cung",
        description = "Trả về danh sách dự báo tồn kho nhiên liệu theo region, ưu tiên những region/fuel có stockDays < target"
    )
    @GetMapping("/1/outlook")
    public List<Pillar1OutlookDto> getPillar1Outlook() {
        return dao.fetchPillar1Outlook();
    }

    // ... lặp cho 3 endpoint pillar còn lại
}
```

5. Lặp cho 6 controller còn lại: `AuthController`, `SecurityController`, `RecommendationController`, `AlertController`, `RawDataController`, `HealthController`.

6. (Optional) Cấu hình `OpenApiConfig.java` thêm group tag order:
```java
@Bean
public OpenAPI vesOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("VES-Monitor API")
            .version("1.0.0")
            .description("REST API cho hệ thống giám sát an ninh năng lượng VN. JWT Bearer auth required for all endpoints except /auth/login, /health."))
        // ... existing security config
        ;
}
```

7. Test bằng Swagger UI (sau khi Leader build):
   - Mở `http://localhost:8090/swagger-ui.html`
   - Expand mỗi endpoint → kỳ vọng "Example Value" hiển thị JSON sample thay vì `"string"` mặc định

8. Commit + push + PR.

### 📦 Deliverables
- [ ] 14 file DTO updated với `@Schema` annotation
- [ ] 7 file Controller updated với `@Operation` + `@Tag`
- [ ] Optional: `OpenApiConfig.java` updated với description chi tiết hơn

### ✓ Acceptance
- Swagger UI hiển thị example JSON realistic ở mỗi endpoint
- Mỗi endpoint có summary + description ngắn
- Không sửa logic, chỉ thêm annotation

### 💡 Tip
- `@Schema(example = "...")` trên field primitives — đúng với type
- `@Schema(implementation = SomeOtherClass.class)` nếu field là object nested
- Nếu DTO dùng Lombok `@Data`, annotation đặt trên field private

---

## 📝 E4. Curl example scripts cho 14 endpoint (4h, sau Phase 4.5 BUILD)

### 🎯 Mục tiêu
Tạo bộ **14 file shell `.sh`** (1 file/endpoint) trong `scripts/curl_examples/` — mỗi file là 1 curl request hoàn chỉnh có pretty-print JSON. Dùng cho:
- Báo cáo Chương 5 — minh hoạ API
- Demo trên slide
- Người mới test nhanh không cần Postman

### ✅ Prerequisites
- Phase 4.5 BUILD xong, backend chạy được trên `localhost:8090`
- Có `jq` để pretty-print JSON: `sudo apt install jq` (WSL) hoặc `choco install jq` (Win)

### 📐 Step-by-step

1. Branch `e/E4-curl-examples`

2. Tạo thư mục: `scripts/curl_examples/`

3. Tạo `scripts/curl_examples/_setup.sh` — helper chung lấy token:

```bash
#!/usr/bin/env bash
# Source vào các script khác: source ./_setup.sh
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8090}"

# Lấy token (cache lại 5 phút để không gọi login mỗi lần)
TOKEN_CACHE="/tmp/ves_token.txt"
TOKEN_TS="/tmp/ves_token.ts"

if [[ -f "$TOKEN_TS" ]] && [[ $(($(date +%s) - $(cat "$TOKEN_TS"))) -lt 300 ]]; then
    TOKEN=$(cat "$TOKEN_CACHE")
else
    echo "→ Logging in..."
    TOKEN=$(curl -s -X POST "$BASE_URL/api/auth/login" \
        -H "Content-Type: application/json" \
        -d '{"username":"admin","password":"admin"}' \
        | jq -r '.accessToken')
    echo "$TOKEN" > "$TOKEN_CACHE"
    date +%s > "$TOKEN_TS"
fi

AUTH_HEADER="Authorization: Bearer $TOKEN"
```

4. Tạo 14 file curl, mỗi file pattern:

`scripts/curl_examples/01_login.sh`:
```bash
#!/usr/bin/env bash
# Endpoint: POST /api/auth/login
# Purpose: Đăng nhập, trả JWT token (TTL 8h)
set -euo pipefail
BASE_URL="${BASE_URL:-http://localhost:8090}"

echo "→ POST $BASE_URL/api/auth/login"
curl -s -X POST "$BASE_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin"}' \
    | jq '.'
```

`scripts/curl_examples/02_security_score.sh`:
```bash
#!/usr/bin/env bash
# Endpoint: GET /api/security/score
# Purpose: Energy Security Score 0-100 + 4 sub-score + trend
set -euo pipefail
source "$(dirname "$0")/_setup.sh"

echo "→ GET $BASE_URL/api/security/score"
curl -s -X GET "$BASE_URL/api/security/score" \
    -H "$AUTH_HEADER" \
    | jq '.'
```

`scripts/curl_examples/03_security_cascade.sh`:
```bash
#!/usr/bin/env bash
# Endpoint: GET /api/security/cascade-risks
set -euo pipefail
source "$(dirname "$0")/_setup.sh"
curl -s "$BASE_URL/api/security/cascade-risks" -H "$AUTH_HEADER" | jq '.'
```

Lặp tương tự cho 11 endpoint còn lại:
- `04_pillar1_outlook.sh` → `GET /api/pillars/1/outlook`
- `05_pillar2_volatility.sh` → `GET /api/pillars/2/volatility`
- `06_pillar3_shedding.sh` → `GET /api/pillars/3/shedding-plan`
- `07_pillar4_netzero.sh` → `GET /api/pillars/4/net-zero-progress`
- `08_alerts_active.sh` → `GET /api/alerts/active`
- `09_recommendations.sh` → `GET /api/recommendations?status=PENDING`
- `10_recommendation_ack.sh` → `POST /api/recommendations/1/acknowledge` (body `{"note":"Test from curl"}`)
- `11_raw_fuel_prices.sh` → `GET /api/raw/fuel-prices/latest?limit=10`
- `12_raw_grid_load.sh` → `GET /api/raw/grid-load/latest?region=VN_NORTH`
- `13_health.sh` → `GET /api/health`
- `14_openapi_spec.sh` → `GET /v3/api-docs | jq` (dump full spec)

5. Tạo `scripts/curl_examples/run_all.sh` chạy tuần tự 14 file → log output ra file:

```bash
#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
OUTPUT_DIR="./output_$(date +%Y%m%d_%H%M%S)"
mkdir -p "$OUTPUT_DIR"

for script in 0*.sh 1*.sh; do
    [[ "$script" == "_setup.sh" ]] && continue
    name="${script%.sh}"
    echo "=========================================="
    echo "Running $script ..."
    bash "$script" > "$OUTPUT_DIR/$name.json" 2>&1 || echo "❌ Failed: $script"
    echo "→ Saved to $OUTPUT_DIR/$name.json"
done

echo ""
echo "✅ Done. Check $OUTPUT_DIR/ for all responses."
```

6. Tạo `scripts/curl_examples/README.md`:

```markdown
# Curl Examples — 14 endpoint

## Sử dụng

1. Cài jq: `sudo apt install jq` hoặc `choco install jq`
2. Đảm bảo backend chạy: `curl http://localhost:8090/api/health` trả 200
3. Chạy 1 endpoint: `bash 02_security_score.sh`
4. Chạy tất cả + log: `bash run_all.sh`

## File list

| # | Script | Endpoint | Auth |
|---|--------|----------|------|
| 1 | 01_login.sh | POST /api/auth/login | Public |
| 2 | 02_security_score.sh | GET /api/security/score | JWT |
| ... | ... | ... | ... |
```

7. Set executable: `chmod +x scripts/curl_examples/*.sh`

8. Test trên WSL: chạy `bash scripts/curl_examples/run_all.sh` — kỳ vọng 14/14 response JSON OK.

9. Commit + push + PR.

### 📦 Deliverables
- [ ] `scripts/curl_examples/_setup.sh`
- [ ] `scripts/curl_examples/01_login.sh` ... `14_openapi_spec.sh` (14 file)
- [ ] `scripts/curl_examples/run_all.sh`
- [ ] `scripts/curl_examples/README.md`

### ✓ Acceptance
- 14 file shell + 1 setup + 1 runner = 16 file
- `bash run_all.sh` chạy không lỗi, 14/14 response 2xx
- Output JSON pretty-print bằng jq

### 💡 Tip
- Mỗi file có comment header rõ endpoint + purpose (cho người đọc nhanh hiểu)
- Token cache file `/tmp/ves_token.txt` tự refresh sau 5 phút → đỡ gọi login mỗi lần

---

## 📝 E5. Load test script + LOAD_TEST_REPORT.md (4h, sau Phase 4.5 BUILD + Phase 7)

### 🎯 Mục tiêu
Viết bash script chạy **load test đơn giản**: 100 concurrent client × 10 request/client = 1000 request → đo `success rate` + `p50/p95/p99 latency`. Output ra `docs/LOAD_TEST_REPORT.md` ~3-4 trang dùng cho báo cáo Chương 5 (performance evaluation).

### ✅ Prerequisites
- Backend chạy được (Phase 4.5 build done)
- Có `jq` + `bc` (đa số Linux đã có): `sudo apt install bc`

### 📐 Step-by-step

1. Branch `e/E5-load-test`

2. Tạo `scripts/load_test.sh`:

```bash
#!/usr/bin/env bash
# Simple load test: N concurrent users × M requests each
# Usage: bash load_test.sh [N_CONCURRENT] [M_REQUESTS] [BASE_URL]
set -euo pipefail

N="${1:-100}"        # số concurrent client
M="${2:-10}"         # số request mỗi client
BASE_URL="${3:-http://localhost:8090}"

OUTPUT_DIR="./load_test_$(date +%Y%m%d_%H%M%S)"
mkdir -p "$OUTPUT_DIR"

echo "=== Load Test ==="
echo "  Concurrent clients : $N"
echo "  Requests/client    : $M"
echo "  Total requests     : $((N * M))"
echo "  Target             : $BASE_URL"
echo "  Output             : $OUTPUT_DIR"
echo ""

# Lấy token 1 lần dùng chung
TOKEN=$(curl -s -X POST "$BASE_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin"}' | jq -r '.accessToken')

if [[ -z "$TOKEN" || "$TOKEN" == "null" ]]; then
    echo "❌ Login failed, abort"
    exit 1
fi

# Spawn N parallel client, mỗi client gọi M lần GET /api/security/score
START_TS=$(date +%s.%N)

for ((i=1; i<=N; i++)); do
    (
        for ((j=1; j<=M; j++)); do
            t0=$(date +%s.%N)
            status=$(curl -s -o /dev/null -w "%{http_code}" \
                -H "Authorization: Bearer $TOKEN" \
                "$BASE_URL/api/security/score")
            t1=$(date +%s.%N)
            latency_ms=$(echo "($t1 - $t0) * 1000" | bc -l)
            echo "$i,$j,$status,$latency_ms" >> "$OUTPUT_DIR/raw.csv"
        done
    ) &
done

wait
END_TS=$(date +%s.%N)
ELAPSED=$(echo "$END_TS - $START_TS" | bc -l)

# Analyze
TOTAL=$(wc -l < "$OUTPUT_DIR/raw.csv")
SUCCESS=$(awk -F, '$3 ~ /^2/ {count++} END {print count+0}' "$OUTPUT_DIR/raw.csv")
ERRORS=$((TOTAL - SUCCESS))
SUCCESS_RATE=$(echo "scale=2; $SUCCESS * 100 / $TOTAL" | bc -l)

# Latency percentiles (sort + pick)
sort -t, -k4 -n "$OUTPUT_DIR/raw.csv" | awk -F, '{print $4}' > "$OUTPUT_DIR/sorted_lat.txt"
P50_LINE=$((TOTAL * 50 / 100))
P95_LINE=$((TOTAL * 95 / 100))
P99_LINE=$((TOTAL * 99 / 100))
P50=$(sed -n "${P50_LINE}p" "$OUTPUT_DIR/sorted_lat.txt")
P95=$(sed -n "${P95_LINE}p" "$OUTPUT_DIR/sorted_lat.txt")
P99=$(sed -n "${P99_LINE}p" "$OUTPUT_DIR/sorted_lat.txt")
THROUGHPUT=$(echo "scale=2; $TOTAL / $ELAPSED" | bc -l)

cat <<EOF | tee "$OUTPUT_DIR/summary.txt"
=== Load Test Result ===
Total requests   : $TOTAL
Success (2xx)    : $SUCCESS
Errors           : $ERRORS
Success rate     : ${SUCCESS_RATE}%
Elapsed time     : ${ELAPSED}s
Throughput       : ${THROUGHPUT} req/s
Latency p50      : ${P50} ms
Latency p95      : ${P95} ms
Latency p99      : ${P99} ms
EOF

echo ""
echo "✅ Done. Raw data: $OUTPUT_DIR/raw.csv"
```

3. Test thử nhỏ trước: `bash scripts/load_test.sh 10 5` (10 client × 5 req = 50) — kỳ vọng 50/50 success.

4. Chạy load test thật: `bash scripts/load_test.sh 100 10` (1000 req) → ghi `summary.txt`.

5. Viết `docs/LOAD_TEST_REPORT.md`:

```markdown
# 📊 Load Test Report — VES-Monitor REST API

## 1. Mục tiêu
Đánh giá khả năng đáp ứng của backend REST API (Phase 4.5) dưới tải đồng thời.

## 2. Setup
- **Hardware**: <CPU/RAM máy chạy test>
- **Backend**: Spring Boot 2.7.18 + HikariCP + Postgres 15
- **Test client**: bash script `scripts/load_test.sh` chạy 100 concurrent client × 10 request = 1000 request tổng
- **Endpoint test**: GET /api/security/score (representative — query 2 view, JSON output)
- **Network**: localhost (loopback, không qua tunnel)

## 3. Kết quả

| Metric | Value |
|--------|-------|
| Total requests | 1000 |
| Success (2xx) | 998 (99.8%) |
| Errors | 2 (0.2%) |
| Elapsed time | 4.32s |
| Throughput | **231 req/s** |
| Latency p50 | 18ms |
| Latency p95 | 95ms |
| Latency p99 | 180ms |

## 4. Phân tích
- Throughput ~230 req/s trên laptop 8GB RAM = đủ cho ~ 100 concurrent user
- p95 < 100ms → user experience mượt
- 2 error là rare network blip (retry OK)

## 5. Hạn chế + hướng phát triển
- Chỉ test 1 endpoint — chưa test endpoint write-heavy (POST acknowledge)
- Chưa test với tunnel Cloudflared (network thực tế)
- Chưa stress đến điểm sụp đổ (saturation test)

## 6. Raw data
Xem `load_test_<timestamp>/raw.csv` và `summary.txt`.
```

6. Commit + push + PR (chỉ commit `scripts/load_test.sh` + `docs/LOAD_TEST_REPORT.md`, KHÔNG commit folder `load_test_*` output).

7. Thêm vào `.gitignore`:
```
scripts/load_test_*/
```

### 📦 Deliverables
- [ ] `scripts/load_test.sh` (~80 dòng bash)
- [ ] `docs/LOAD_TEST_REPORT.md` (3-4 trang)
- [ ] `.gitignore` updated

### ✓ Acceptance
- Script chạy không lỗi, output đủ 6 metric
- Report ≥ 3 trang có data thật từ run của bạn
- Backend không crash trong test (Leader monitor `docker stats`)

### 💡 Tip
- Chạy trên WSL Ubuntu — bash + bc + jq ổn định
- Nếu p99 > 1s → backend bị bottleneck, ping Leader xem có nên thêm connection pool
- Phải lấy token 1 lần dùng chung (không spam login) — nếu không sẽ bottleneck ở auth thay vì endpoint test

---

# 📅 TIMELINE TỔNG — 10 TUẦN

Lưu ý: timeline này điều phối 4 thành viên (B/C/D/E) song song với Leader. Leader bám lộ trình `UPGRADE_PLAN.md §24.5` cho phần code chính.

| Tuần | Leader (A) làm gì | B làm gì | C làm gì | D làm gì | E làm gì |
|------|-------------------|----------|----------|----------|----------|
| 1 | Phase 0+1 (foundation+docker) | B1 (vẽ diagram) | - | D1 (SQL seed VN) | E1 (SQL stress seed) |
| 2 | Phase 2+2.5 (schema+pillars) | B2 (Chương 1) | - | D2 (ValidatorUtils) | E2 (JUnit backend tests) |
| 3 | Phase 2.6+3 (security+Flink) | B3 (Chương 2 — cần B1 done) | - | (đợi Phase 5 skel) | E3 (Swagger examples) |
| 4 | Phase 4 (generators) | B4 (Chương 4) | - | (đợi Phase 5 skel) | (đợi Phase 4.5 build) |
| 5 | Phase 4.5 (REST API — đang ở đây ✅) | B5 (Chương 6 + AI Log) | C1 (slide) | (đợi Phase 5 skel) | (đợi Phase 4.5 build) |
| 6 | **Phase 5 (JavaFX) — bắt đầu** | review báo cáo | C3 screenshot Swagger | D3 (About JavaFX) | E4 (curl examples — sau Phase 4.5 build) |
| 7 | **Phase 5 tiếp** | format báo cáo final | C3 screenshot JavaFX + C4 Postman | D4 (Settings JavaFX) | E4 (tiếp) |
| 8 | **Phase 7 deploy + tunnel** | proofreading | C2 quay video | D5 (CSS + Splash) | E5 (load test sau tunnel) |
| 9 | Phase 8 doc + integration test | combine + export PDF | C5 (test report) | review final | E5 (tiếp + report) |
| 10 | Pre-build artifacts + Demo dress rehearsal | nộp báo cáo | diễn tập demo | demo dress rehearsal | demo dress rehearsal |

> ℹ️ **Phase 6 (Android)** không còn nằm trong đồ án Java — chuyển sang **đồ án Mobile riêng** (xem `docs/ANDROID_ONBOARDING.md`). Vì vậy Tuần 7-8 không còn task Android. Leader workload giảm tương ứng.

---

# 📊 TỔNG KẾT WORKLOAD

| Người | Workload | Số PR | Loại task | Phụ thuộc |
|-------|----------|-------|-----------|-----------|
| **Leader A** | ~75-85h | 30-40 | Toàn bộ backend/Flink/JavaFX core/deploy (KHÔNG còn Android) | - |
| **Người B** | ~30h | 5 | Báo cáo Chương 1/2/4/6 + diagram + Notion | B1 chỉ cần Phase 2; B5 cần Phase 4 |
| **Người C** | ~25h | 5 | Slide + video + screenshots + Postman + test report | C2/C3/C5 chờ Phase 5+7 |
| **Người D** | ~20h | 5 | SQL seed + JUnit + 2 màn JavaFX + CSS | D3/D4/D5 chờ Phase 5 skeleton |
| **Người E** | ~18h | 5 | SQL stress seed + JUnit backend + Swagger examples + curl + load test | E4/E5 chờ Phase 4.5 build |
| **TỔNG** | ~170h | ~55 PR | | |

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

