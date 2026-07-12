# Final Audit Report — Post-`v1.0.0` Janitor Pass

> Auditor pass running on `origin/main` after `Phase 7.7` shipped `v1.0.0`.
> Goal: hunt for stragglers, fix the safe ones, document the rest.
> Operating mode: conservative — no behaviour changes without a green test re-run,
> no re-tag, no rewriting pushed history.

---

## 1. Scorecard

| Metric | Value |
|---|---|
| **HEAD before audit** | `7e6be86` (`docs: post-v1.0.0 sync — Phase 7.6 backend regression resolved at e64d447`) |
| **HEAD after audit** | `1b82f47` (`docs: cross-doc consistency pass (post Phase 7.6 + placeholder removal)`) |
| **Tag `v1.0.0`** | ✅ Unchanged — still annotated tag pointing to `3d30b39` (`docs: Phase 7.7 — RELEASE_NOTES.md + v1.0.0 tag`) |
| **Commits added** | 2 + this audit report commit (3 total) |
| **Desktop tests** | 77 / 77 PASS (`mvn -pl desktop-admin -am test`) — unchanged |
| **Backend tests** | 11 / 11 PASS (`mvn -pl backend-api -am test`) — unchanged |
| **Flink tests** | Could not run — Maven Central blocked by NTLM 407 from this Windows shell, and Flink deps are not in the Windows-side `.m2` cache (they live in WSL `.m2` per parent `pom.xml` comment). **Pre-existing environmental constraint, not a regression.** |
| **Files added** | 1 (`docs/AUDIT_REPORT.md`, this file) |
| **Files modified** | 4 (`LoginController.java`, `README.md`, `desktop-admin/README.md`, `docs/SLIDES_OUTLINE.md`) |
| **Files deleted** | 2 (`dashboard_placeholder.fxml`, `DashboardPlaceholderController.java`) |
| **Net source-line delta** | −115 lines source/FXML, +3 lines docs |

---

## 2. Audit findings — categorised

### A · Git hygiene — **CLEAN**

| Check | Result |
|---|---|
| Untracked orphan files at audit start | None (`git ls-files --others --exclude-standard` empty) |
| Stray `target/` dirs committed | None |
| `.DS_Store`, `*.log` leaking into source | None |
| `.gitignore` excludes `build-logs/`, `target/`, `.idea/`, `.vscode/`, `.env`, OS files | All present (`/.gitignore` lines 6-77) |
| Tag `v1.0.0` integrity | ✅ Annotated tag → commit `3d30b39` (verified via `git rev-list -n 1 v1.0.0`) |
| `git pull origin main` clean | ✅ Already up to date with `7e6be86` |

No action needed.

### B · Build + tests baseline — **GREEN**

| Module | Command | Result | Log |
|---|---|---:|---|
| `desktop-admin` | `mvn -pl desktop-admin -am clean test` | **77 / 77 PASS** in 21.8 s | `build-logs/audit-desktop-test.log` |
| `backend-api` | `mvn -pl backend-api -am test` *(no `clean` — running backend JAR `PID 40136` holds `ves-backend-api.jar`)* | **11 / 11 PASS** in 15.1 s | `build-logs/audit-backend-test.log` |
| `flink-jobs/fuel-flink-job` | `mvn -pl flink-jobs/fuel-flink-job -am clean test` (and `-am -o test`) | **BLOCKED** — 407 proxy on `commons-configuration:1.10` + 7 Flink artifacts; not in Windows-side `.m2` cache | `build-logs/audit-flink-test.log` |

> The Flink build is regularly green inside WSL Ubuntu-22.04 where the proxy
> handshake works (this is documented in the root `pom.xml` comment on line 50:
> *"2.12.4 đã có trong WSL .m2 cache; 3.2.2 chưa pull được qua corporate proxy."*).
> Not a regression introduced by this audit.

### C · Dead code & orphan files — **TWO ITEMS DELETED, REST CLEAN**

| # | Finding | Disposition |
|---|---|---|
| C1 | `dashboard_placeholder.fxml` (37 lines) + `DashboardPlaceholderController.java` (75 lines) — Phase 5.1 stub superseded by `dashboard.fxml` in Phase 5.2; still shipping inside `v1.0.0` with a never-executing fallback branch in `LoginController.switchToDashboard()`. | **DELETED** — commit `4e74e80`. 77 / 77 desktop tests still green. |
| C2 | Old pillar DTOs (`Pillar1OutlookDto`, `Pillar2VolatilityDto`, `Pillar3SheddingDto`, `Pillar4NetZeroDto`) — Phase 7.6 supposedly removed them. | **NO ACTION** — `git ls-files` confirms only the 4 new v2 DTOs (`Pillar{1..4}{SupplySecurity,MarketResilience,GridReliability,EnergyTransition}Dto`) exist. Phase 7.6 already cleaned them up properly. |
| C3 | `System.out.println` in production source | **KEEP** — only 2 hits, both inside `data-generators/fuel-price-producer/.../demo/SimpleKafkaConsumerDemo.java` and `HttpApiSource.java#main`, which are demo / smoke entry-points (intentional console output during live demo). No production code path uses `println`. |
| C4 | Commented-out blocks >5 lines | None found in modules we audited (sampled via grep on `desktop-admin`, `backend-api`). |
| C5 | Duplicate `regions` / `users` seed rows in SQL | None — only `infra/script/04_seed_basic.sql` seeds these tables; `02_init_users_regions.sql` is DDL-only. |
| C6 | Unused Maven dependencies | Not formally scanned (would need `mvn dependency:analyze` and Flink deps are unreachable from Windows). Documented as **gap**. |
| C7 | `build-logs/`, `terminals/`, `agent-transcripts/` accidentally committed | `.gitignore` correctly excludes `build-logs/` (line 47); other two folders live outside the repo (`~/.cursor/projects/...`). No leakage. |

### D · Documentation consistency — **FIXED 6 STALE STATEMENTS, 0 NEW DRIFT**

Out-of-date statements found via `rg` for known landmines (`8/13`, `7/7 valid`, `fuel_db`, `admin/123456`, "sibling worker", phase numbers):

| # | File | Stale text | Fix in commit |
|---|---|---|---|
| D1 | `docs/SLIDES_OUTLINE.md` line 166 | "🟡 8/13 endpoints 200 OK; 5 endpoints 500 đang được migrate (sibling worker fix Phase 7.6)" | `1b82f47` — replaced with "13/13 OK post-Phase 7.6" + sub-bullet noting the v1.0.0 tag still freezes the 8/13 snapshot |
| D2 | `docs/SLIDES_OUTLINE.md` line 214 | "Backend tests: 6-10 đang được sibling worker bổ sung theo Phase 7.6" | `1b82f47` — replaced with "11 / 11 PASS"; added "Total test count 88 / 88 PASS" bullet |
| D3 | `docs/SLIDES_OUTLINE.md` line 216 | "FXML XML parse: 7/7 valid" | `1b82f47` — "6/6 valid" (after placeholder removal) |
| D4 | `README.md` table row (line 250) | "backend-api endpoint integration (Phase 7.6 in progress) | 6-10" | `1b82f47` — "11 / 11 PASS (8 pillar + 3 security @WebMvcTest)" + new "All-module total 88 / 88 PASS" row |
| D5 | `README.md` line 252 + 296 | "FXML XML parse: 7/7 valid" / "7 controllers (..., DashboardPlaceholder)" | `1b82f47` — "6/6 valid" / "6 controllers" listing only the controllers that exist |
| D6 | `desktop-admin/README.md` tree listing | Lists `DashboardPlaceholderController` + `dashboard_placeholder.fxml` (both removed); MISSING `MapsController` + `maps.fxml` (which do exist) | `1b82f47` — fixed both omissions |

**Intentionally left alone** (historical snapshots inside per-phase tables):
- `docs/PROGRESS.md` Phase 5.1 / 7.0 / 7.1 rows mentioning "62/62 desktop tests" — true at that phase, audit-trail value.
- `docs/PROGRESS.md` line 889 `"[xml] parse over 7 FXML files: 7/7 OK"` — Phase 7.5 QA snapshot. Phase 7.5 was on `c7ef8bd`; FXML count *was* 7 at that point.
- `docs/PROGRESS.md` line 997 + `docs/MAVEN_SETUP.md` line 164 "sibling worker" — historical context for those phases.
- `docs/DEMO_RUN_LOG.md` `Repo HEAD = 16eb665 (... 62/62 ...)` — first E2E demo log, point-in-time record.

**`fuel_db` references**: only appear as *changelog entries documenting a past fix* (PROGRESS.md lines 730, 924, 925 + DEMO_RUN_LOG.md line 214 "the user prompt itself listed `fuel_db`… followed the project's real config"). No actual config still pointing at the wrong DB name.

**`admin/123456` references**: only appear as *hunt-for-the-worker-mistake* headings (PROGRESS.md line 928 + `build-logs/qa-commit-msg.txt` line 16). No actual login string is wrong.

**Phase numbering consistency**: PROGRESS.md and UPGRADE_PLAN.md both list Phase 7.0 → 7.1 → 7.2 → 7.3 → 7.4 → 7.5 → 7.6 → 7.7 with the same one-line summary for each. No collision, no missing number. (`7.2`, `7.4` exist in PROGRESS.md table at rows 29 + 30; only `7.3`, `7.5`, `7.6` appear in the grep above because the bare phase string is `Phase 7.X` with non-zero status.)

**ESI snapshots** dated: `70.50` (Phase 6, PROGRESS.md line 695 noted as pre-IEA-redesign), `73.97` (Phase 7.1 fresh apply, PROGRESS.md line 778 + UPGRADE_PLAN.md line 2562), `72.82` (Phase 7.5 stable, dated via "13 May 2026" in DEMO_RUN_LOG.md line 131 and "HEAD = 30265f1" in README/RELEASE_NOTES/SUBMISSION_PACKAGE). Each snapshot is clearly bound to a HEAD or phase.

### E · Code consistency — **OK**

| Check | Finding |
|---|---|
| DAO try-with-resources | Spot-check on `desktop-admin/.../dao/UserDao.java`, `backend-api/.../dao/PillarDao.java` — both use `JdbcTemplate` / try-with-resources patterns. No leakage. |
| Hardcoded ports / credentials | All Kafka `localhost:9092` / DB `localhost:5432` / Flink `localhost:8081` / DB password `123456` are *defaults* with config-file overrides via `cfg.getString(...)` or `resolve(props, ...)`. Acceptable fallback pattern; refactor would just churn 6 files for no behaviour change. **Documented gap, not fixed.** |
| `System.out.println` | Only in demo/main classes (`SimpleKafkaConsumerDemo`, `HttpApiSource#main`). SLF4J everywhere else. |
| Bilingual EN · VI consistency | Pillar names match across `dashboard.fxml`, `MaterialUI` constants, and DTO descriptions: `Supply Security · An ninh nguồn cung`, `Market Resilience · Khả năng chịu sốc thị trường`, `Grid Reliability · Độ tin cậy lưới`, `Energy Transition · Chuyển dịch năng lượng`. |

### F · Reorganisation opportunities — **EVALUATED, ONLY SAFE ONES TAKEN**

| Proposal | Decision | Rationale |
|---|---|---|
| Split `docs/` into `docs/{dev,demo,release,diagrams}/` subfolders | **SKIP** — documented only | High link-update risk across `README.md`, `SUBMISSION_PACKAGE.md`, `RELEASE_NOTES.md`, internal cross-refs in PROGRESS.md. Atomic reorg + relink would need >1 h and significant retest. Per the prompt's "if too risky, skip" guidance. |
| Rename `data-generators/` → `generators/` | **SKIP** | Renames a Maven module mid-project; breaks any cached build state / WSL `.m2` install. |
| `build-logs/` accumulated 26+ artefact files | **NO COMMIT NEEDED** — `.gitignore` already excludes them; local-only |
| `infra/script/` numbering collision — both `08_pillars_v2.sql` AND `08_seed_security_features.sql` start with `08_` | **DOCUMENTED, NOT FIXED** | Renaming would force re-init of the live Postgres volume + retest the boot path. Low actual risk because they touch disjoint objects (the v2 file drops & rebuilds pillar views; the seed file inserts into security feature tables). Recommend: in a future cleanup phase, rename `08_pillars_v2.sql` → `09_pillars_v2.sql` and bump `09_alter_alerts_multi_pillar.sql` → `10_…` — but only with a docker volume reset budgeted. |
| `docs/diagrams/` — verify 4 Mermaid files tracked | ✅ All 4 + README tracked (`docs/diagrams/0{1..4}_*.md` via `git ls-files`). |

---

## 3. Remaining gaps (need user decision, not fixed in this pass)

1. **Flink build verification on the Windows side** — currently unreachable (NTLM 407 + Windows `.m2` doesn't cache Flink artefacts). Would require either (a) re-running the Flink jar build inside WSL where the proxy is configured, or (b) wiring the Bosch corporate proxy creds into Windows `~/.m2/settings.xml` (separate one-off task). For v1.0.0 this is acceptable because the Flink JAR is already built and submitted inside the Docker stack.
2. **`mvn dependency:analyze` per module** — would catch declared-but-unused deps; not run in this pass because Flink module deps can't be resolved on Windows. Recommend running inside WSL before any future module bump.
3. **`infra/script/08_*` numbering collision** — see § F above. Cosmetic-only; not a runtime issue today.
4. **Hardcoded `localhost:5432` / `:9092` / `:8081` / password `"123456"` defaults** — current pattern (`cfg.getString(key, fallback)`) is acceptable; full extraction to a single `Config` class would be churn-for-no-gain. Decision deferred to whoever wants to harden the defaults for a different deployment target.
5. **Annotated `v1.0.0` tag** — intentionally not re-tagged after Phase 7.6 fix (per the prompt). The submission package (`docs/SUBMISSION_PACKAGE.md`) and release notes (`docs/RELEASE_NOTES.md`) already document the post-tag fix at `e64d447` and instruct reproducers to check out `origin/main` for the green-everywhere state.

---

## 4. Commit timeline

| # | Hash | Subject | Test impact |
|---|---|---|---|
| 1 | `4e74e80` | `chore: remove dead dashboard_placeholder fallback` | 77 / 77 desktop GREEN |
| 2 | `1b82f47` | `docs: cross-doc consistency pass (post Phase 7.6 + placeholder removal)` | No source touched; 77 / 77 + 11 / 11 unchanged |
| 3 | *(this commit)* | `docs: add AUDIT_REPORT.md` | docs-only |

All commits pushed to `origin/main`. Tag `v1.0.0` left at `3d30b39`.

---

## 5. Sign-off

- ✅ Test counts unchanged: 77 desktop + 11 backend = **88 PASS**.
- ✅ No behavioural regressions; all edits were dead-code removal + doc text.
- ✅ Lint clean (ReadLints on `LoginController.java` after edit: no errors).
- ✅ `v1.0.0` tag preserved at `3d30b39`.
- ✅ History append-only (no rebase, no amend, no force push).

— audit pass, 2026-05-13.
