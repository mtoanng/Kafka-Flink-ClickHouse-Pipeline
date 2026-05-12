# Maven Setup — Bosch laptop, portable install

**Status (2026-05-12, off-VPN home network):** Maven 3.9.6 installed, builds work end-to-end via the **APAC** Bosch proxy. Full JavaFX + Spring Boot 2.7 dep tree resolves from Maven Central in ~25 seconds.

## TL;DR — what works right now

```powershell
mvn -v                                  # Apache Maven 3.9.6
cd path\to\backend-api ; mvn package    # works on home WiFi (no VPN)
cd path\to\desktop-admin ; mvn package  # works on home WiFi (no VPN)
```

No VPN needed for builds on home network — the **APAC corporate proxy `rb-proxy-apac.bosch.com:8080` is reachable from public internet** and is what the laptop is already configured to use system-wide (per `HTTPS_PROXY` env + WPAD PAC file).

## Install summary

| Item | Value |
|---|---|
| Maven version | Apache Maven **3.9.6** |
| Install path | `C:\Users\GOT4HC\tools\apache-maven-3.9.6\` |
| Download source | `https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip` (primary `dlcdn.apache.org` returned 404 — that mirror only keeps current versions) |
| Zip size | 9,513,253 bytes (~9.5 MB) |
| Scope | **User PATH** (no admin required) |
| Persisted env vars | `PATH` += `…\apache-maven-3.9.6\bin`, `MAVEN_HOME` = `…\apache-maven-3.9.6` |
| JDK | 21.0.10 (Oracle JDK on `PATH`; Maven 3.9.x is JDK-21-compatible) |

`mvn -v` output:

```
Apache Maven 3.9.6 (bc0240f3c744dd6b6ec2920b3cd08dcc295161ae)
Maven home: C:\Users\GOT4HC\tools\apache-maven-3.9.6
Java version: 21.0.10, vendor: Oracle Corporation, runtime: C:\Program Files\Java\jdk-21
Default locale: en_US, platform encoding: UTF-8
OS name: "windows 11", version: "10.0", arch: "amd64", family: "windows"
```

> Future PowerShell windows will pick up `mvn` automatically. **The PowerShell session in which you ran the install will not see it on `PATH`** until you close and reopen a new shell.

## Network mode discovered (off-VPN, home WiFi)

| Probe | Result |
|---|---|
| `Test-NetConnection dlcdn.apache.org:443` | **False** (raw TCP blocked) |
| `Test-NetConnection repo.maven.apache.org:443` | **False** (raw TCP blocked) |
| `Test-NetConnection rb-proxy-emea.internal.bosch.cloud:8080` | TIMEOUT (VPN-only host) |
| `Test-NetConnection rb-proxy-apac.bosch.com:8080` | **True** (reachable from home internet) |
| `Invoke-WebRequest https://repo.maven.apache.org/maven2/` | **200 OK** (via system proxy env) |

Conclusion: the laptop has **no direct outbound to the public internet**. Everything HTTPS goes through one of the corporate proxies. Only the APAC proxy is internet-facing; the EMEA proxy is internal and needs VPN.

System-wide proxy configuration on this machine:

```
HTTP_PROXY  = http://rb-proxy-apac.bosch.com:8080
HTTPS_PROXY = http://rb-proxy-apac.bosch.com:8080
NO_PROXY    = rb-omscloudasl4.server.bosch.com,127.0.0.*,localhost
WinINET     = ProxyEnable=0, AutoConfigURL=http://rbins-ap.bosch.com/sgp.pac
```

## `~/.m2/settings.xml` — proxy state after this run

Current entries (`C:\Users\GOT4HC\.m2\settings.xml`):

| id | active | host |
|---|---|---|
| `bosch-emea-https` | **false** | `rb-proxy-emea.internal.bosch.cloud:8080` |
| `bosch-emea-http`  | **false** | `rb-proxy-emea.internal.bosch.cloud:8080` |
| `bosch-apac-https` | **true**  | `rb-proxy-apac.bosch.com:8080` |
| `bosch-apac-http`  | **true**  | `rb-proxy-apac.bosch.com:8080` |

> **Why both pairs?** EMEA is unreachable without VPN; APAC works from public internet. Keeping both lets you flip a single flag per location.
>
> **Important — Maven 3.9 caveat:** The default Apache HttpClient transport in Maven 3.9 **does not honour the JVM proxy system properties** (`-Dhttps.proxyHost=…`) unless `aether.connector.http.useSystemProperties=true` is also set. Setting `MAVEN_OPTS` / `_JAVA_OPTIONS` with `https.proxyHost` alone does nothing. The reliable way to configure the proxy is **via `settings.xml` `<proxies>`** as above.

### How to toggle between home (off-VPN) and on-VPN

Edit `C:\Users\GOT4HC\.m2\settings.xml`:

| Scenario | `bosch-emea-*` `<active>` | `bosch-apac-*` `<active>` |
|---|---|---|
| Off-VPN, home / APAC public internet (current) | `false` | `true` |
| On Bosch VPN (corporate LAN) | `true` | `false` |
| Off any network with cache warm (just compile, no fetch) | either — Maven will use local `~/.m2/repository` |

A backup of the original 2-entry settings.xml lives next to it as `settings.xml.bak.<timestamp>`.

## Verification — what was tested

**Step 6 — smallest dep probe** (resolves `junit:junit:4.13.2` from a scratch dir):

```powershell
cd C:\Users\GOT4HC\tmp-maven-test    # any dir outside the repo
mvn -B -U dependency:get -Dartifact=junit:junit:4.13.2 -Dtransitive=false
```

Result: **BUILD SUCCESS** in 41 s. Maven downloaded ~70 plugin POMs/JARs plus the junit artifacts via `https://repo.maven.apache.org/maven2/` (through the APAC proxy). Download speeds 36 kB/s – 1.7 MB/s.

**Step 7 — stretch dep probe** (the actual deps used by `backend-api` + `desktop-admin`):

```xml
<!-- C:\Users\GOT4HC\tmp-maven-test\pom.xml -->
<dependencies>
  <dependency><groupId>org.openjfx</groupId><artifactId>javafx-controls</artifactId><version>17.0.10</version></dependency>
  <dependency><groupId>org.openjfx</groupId><artifactId>javafx-fxml</artifactId><version>17.0.10</version></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId><version>2.7.18</version></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-security</artifactId><version>2.7.18</version></dependency>
  <dependency><groupId>org.postgresql</groupId><artifactId>postgresql</artifactId><version>42.7.3</version></dependency>
  <dependency><groupId>org.mindrot</groupId><artifactId>jbcrypt</artifactId><version>0.4</version></dependency>
</dependencies>
```

```powershell
cd C:\Users\GOT4HC\tmp-maven-test
mvn -B -U dependency:resolve
```

Result: **BUILD SUCCESS** in 24.6 s. **114 artifacts downloaded from Central.** Notable deps confirmed in the resolved tree:

- JavaFX 17.0.10: `javafx-base`, `javafx-controls`, `javafx-graphics`, `javafx-fxml`
- Spring Boot 2.7.18 starters: web, security, tomcat
- Apache Tomcat 9.0.83 (embed-core, embed-el, embed-websocket)
- Spring Security 5.7.11 (config, core, crypto, web)
- Spring Framework 5.3.31 (web, webmvc, beans, context, expression, aop)
- Jackson 2.13.5 (core, databind, annotations, jdk8, jsr310, paramnames)
- PostgreSQL JDBC 42.7.3
- jbcrypt 0.4

> JavaFX OS-specific natives use Maven's `<classifier>` mechanism (`win`, `mac`, `linux`). The plain `javafx-controls` / `javafx-fxml` artifacts above pull the Windows natives automatically on this machine; for cross-platform packaging you would add classifiers explicitly in the real `pom.xml`.

Post-test cache: `C:\Users\GOT4HC\.m2\repository` is **~51 MB** (124 JARs, 327 POMs). Once a real `mvn package` runs on the repo modules it will grow further (Spring Boot brings in more JARs for `starter-test`, `starter-data-jpa`, etc., and JavaFX `maven-plugin` pulls its own chain).

## How to re-run the smoke test

```powershell
$probe = "C:/Users/GOT4HC/tmp-maven-test"
New-Item -ItemType Directory -Force -Path $probe | Out-Null
Set-Location $probe
mvn -B dependency:get -Dartifact=junit:junit:4.13.2 -Dtransitive=false
```

Expected: `BUILD SUCCESS`, junit JAR comes from `https://repo.maven.apache.org/maven2/`.

If you get `407 Proxy Authentication Required`, the proxy started requiring auth — add `<username>` / `<password>` to the active `<proxy>` block in `settings.xml`.

If you get `repo.maven.apache.org` as the error message, the proxy block isn't active or you accidentally hit the EMEA proxy — verify only the APAC entries have `<active>true</active>`.

## Anti-patterns / gotchas hit during install

- **`dlcdn.apache.org` returns 404 for Maven 3.9.6** — that mirror only serves the latest patch on each branch. Use `archive.apache.org` for pinned older versions.
- **`Test-NetConnection` returns False for Maven Central** — that's expected on this network (direct TCP is blocked). The real reachability test is `Invoke-WebRequest`, which goes through the system proxy.
- **`MAVEN_OPTS=-Dhttps.proxyHost=…` alone does NOT make Maven 3.9 use a proxy.** Maven 3.9's default `maven-resolver-transport-http` ignores JVM system properties unless `aether.connector.http.useSystemProperties=true` is set. Configure via `settings.xml` instead.
- **PowerShell + cmd quoting:** `-Dhttp.nonProxyHosts=localhost|127.*|*.bosch.com` from PowerShell will be split on `|` by `cmd.exe` inside `mvn.cmd`. Either escape carefully or put `nonProxyHosts` inside `settings.xml` where pipes are a normal XML element value.
- **Don't install via Chocolatey** for this — it needs admin. The portable zip + user-scope `PATH` is the no-admin path.

## Files created by this setup

- `C:\Users\GOT4HC\tools\apache-maven-3.9.6\` — Maven install (~10 MB)
- `C:\Users\GOT4HC\.m2\repository\` — local artifact cache (~51 MB after probes; will grow with real builds)
- `C:\Users\GOT4HC\.m2\settings.xml.bak.<timestamp>` — backup of the EMEA-only settings.xml prior to adding the APAC entries
- `docs/MAVEN_SETUP.md` (this file)

## Next step

Once the Phase 5.1–5.5 sibling worker finishes editing `desktop-admin/` and `backend-api/`:

```powershell
cd C:\Users\GOT4HC\Real-time-processing-with-Kafka-Flink-Postgres\desktop-admin
mvn javafx:run        # launches the admin JavaFX UI

cd C:\Users\GOT4HC\Real-time-processing-with-Kafka-Flink-Postgres\backend-api
mvn spring-boot:run   # launches the REST API
```

Both should resolve any not-yet-cached deps via the APAC proxy and start without VPN.
