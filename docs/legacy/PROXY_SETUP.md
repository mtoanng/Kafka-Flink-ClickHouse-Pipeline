# Maven Proxy Setup — Bosch Corporate Network

> **Status (2026-05-12):** Configuration staged but **NOT verified end-to-end** because two prerequisite blockers were detected on this machine. See [Verification status](#verification-status) below.

This document describes how to make Maven dependency downloads work for `backend-api` (Spring Boot 2.7.18) and `desktop-admin` (JavaFX 17.0.10) from the Bosch corporate network.

---

## Environment

| Item            | Value                                                           |
| --------------- | --------------------------------------------------------------- |
| Repo            | `C:/Users/GOT4HC/Real-time-processing-with-Kafka-Flink-Postgres` |
| OS              | Windows 11, PowerShell                                          |
| JDK             | `C:\Program Files\Java\jdk-21\bin` (on PATH)                    |
| Maven           | **NOT installed** at time of writing                            |
| Maven user dir  | `C:/Users/GOT4HC/.m2/` (created by this script)                 |
| Settings file   | `C:/Users/GOT4HC/.m2/settings.xml` (created by this script)     |
| Corporate proxy | `rb-proxy-emea.internal.bosch.cloud:8080` (Bosch EMEA)          |

---

## Verification status

The plan called for: probe → write `settings.xml` → smoke-test download → resolve all module deps. The first two probes both failed:

### Blocker 1 — Maven is not installed

```
PS> mvn -v
mvn : The term 'mvn' is not recognized as a name of a cmdlet, function, script file, or executable program.
```

Searched the usual install roots and env vars — none found:

- `C:\Program Files\Apache` — missing
- `C:\Program Files\Apache Software Foundation` — missing
- `C:\apache-maven`, `C:\maven`, `C:\tools\maven` — missing
- `C:\ProgramData\chocolatey\lib\maven` — missing
- `$env:M2_HOME`, `$env:MAVEN_HOME` — both empty
- No Maven Wrapper (`mvnw` / `mvnw.cmd` / `.mvn/`) committed in repo root, `backend-api/`, or `desktop-admin/`

### Blocker 2 — Proxy is unreachable from this machine right now

DNS resolves, but TCP to port 8080 times out:

```
PS> Test-NetConnection rb-proxy-emea.internal.bosch.cloud -Port 8080
WARNING: TCP connect to (139.15.111.168 : 8080) failed
WARNING: Ping to 139.15.111.168 failed with status: TimedOut
```

Confirmed with a short-timeout TcpClient probe:

```
DNS=139.15.111.168
PROXY_TCP=TIMEOUT (5s)
```

The hostname resolves to a Bosch internal IP, but the network path is blocked. **Most likely cause: not connected to the corporate VPN.** A second possibility is a local firewall rule, but VPN is by far the more common cause of this exact symptom.

Because the proxy is unreachable, no anonymous probe to Maven Central, no `mvn dependency:get`, and no `mvn dependency:resolve` could be executed. Steps 4 and 5 of the plan are deferred until Maven is installed **and** the VPN is connected.

---

## What was staged (ready for first run)

### `C:/Users/GOT4HC/.m2/settings.xml` — created fresh

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <proxies>
    <proxy>
      <id>bosch-emea-https</id>
      <active>true</active>
      <protocol>https</protocol>
      <host>rb-proxy-emea.internal.bosch.cloud</host>
      <port>8080</port>
      <nonProxyHosts>localhost|127.*|*.bosch.com|*.bosch.cloud|*.bosch.de</nonProxyHosts>
    </proxy>
    <proxy>
      <id>bosch-emea-http</id>
      <active>true</active>
      <protocol>http</protocol>
      <host>rb-proxy-emea.internal.bosch.cloud</host>
      <port>8080</port>
      <nonProxyHosts>localhost|127.*|*.bosch.com|*.bosch.cloud|*.bosch.de</nonProxyHosts>
    </proxy>
  </proxies>
</settings>
```

Both `http` and `https` protocols are configured because Maven Central, Spring repos, and OpenJFX use HTTPS, but a few mirrors and metadata fetches still use HTTP. The `<nonProxyHosts>` list bypasses the proxy for localhost and Bosch-internal hosts so a future Bosch artifact mirror can be reached directly. No credentials are stored — the assumption is that the proxy accepts integrated Windows auth (Negotiate/SSPI) or anonymous access.

> If you need to edit this file, **back it up first**:
> `Copy-Item "$HOME/.m2/settings.xml" "$HOME/.m2/settings.xml.bak.$(Get-Date -Format yyyyMMdd)"`

---

## How to finish the setup (next steps for the user)

### 1. Install Maven

Pick one — **option A** is simplest if you have Chocolatey:

**A) Chocolatey** (run elevated PowerShell):

```powershell
choco install maven -y
```

**B) Manual ZIP** (works without admin):

1. Download `apache-maven-3.9.x-bin.zip` from <https://maven.apache.org/download.cgi> on a non-restricted network or via a colleague.
2. Extract to e.g. `C:\Users\GOT4HC\tools\apache-maven-3.9.6`.
3. Add to user PATH:
   ```powershell
   [Environment]::SetEnvironmentVariable("MAVEN_HOME","C:\Users\GOT4HC\tools\apache-maven-3.9.6","User")
   [Environment]::SetEnvironmentVariable("Path","$env:Path;C:\Users\GOT4HC\tools\apache-maven-3.9.6\bin","User")
   ```
4. Open a **new** PowerShell window and verify: `mvn -v`.

**C) Maven Wrapper (mvnw)** — fastest if you can get it from a colleague on a working machine:

```powershell
cd C:/Users/GOT4HC/Real-time-processing-with-Kafka-Flink-Postgres
mvn wrapper:wrapper -Dmaven=3.9.6   # one-time, on a working machine
git add mvnw mvnw.cmd .mvn && git commit -m "chore: add Maven wrapper"
```

After that, every checkout can use `./mvnw` without a global Maven install.

### 2. Connect to the corporate VPN

Re-run the reachability check; expected output is `TcpTestSucceeded : True`:

```powershell
Test-NetConnection rb-proxy-emea.internal.bosch.cloud -Port 8080
```

### 3. Smoke-test the proxy + Maven Central path

```powershell
mvn dependency:get `
  -DremoteRepositories=https://repo.maven.apache.org/maven2 `
  -Dartifact=junit:junit:4.13.2 `
  -Dtransitive=false
```

Acceptable outcomes:

| Output                                | Meaning                                                                               |
| ------------------------------------- | ------------------------------------------------------------------------------------- |
| `BUILD SUCCESS` + `Downloaded from central:` | Proxy works without explicit creds. **You're done.** Continue to step 4.       |
| `BUILD SUCCESS` + `already in local repository` | Same as above (already cached). Continue.                                    |
| `407 Proxy Authentication Required`   | NTLM/Basic auth required. See [Fallback if 407 persists](#fallback-if-407-persists).  |
| Connection refused / 503 / timeout    | Network/proxy issue. Verify VPN, then retry.                                          |

### 4. Pre-warm dependencies for both modules

```powershell
cd C:/Users/GOT4HC/Real-time-processing-with-Kafka-Flink-Postgres/desktop-admin
mvn dependency:resolve -DskipTests=true

cd C:/Users/GOT4HC/Real-time-processing-with-Kafka-Flink-Postgres/backend-api
mvn dependency:resolve -DskipTests=true
```

Expect 5–15 minutes total over the proxy. Once both finish with `BUILD SUCCESS`, normal `mvn package` / `mvn javafx:run` will work.

---

## Fallback if 407 persists

The configuration above relies on the proxy accepting either anonymous traffic or integrated Windows auth via `UseDefaultCredentials`. If `mvn dependency:get` returns `407 Proxy Authentication Required`, try in this order:

### Fallback A — `MAVEN_OPTS` with NTLM hints (no creds in plaintext)

```powershell
$env:MAVEN_OPTS = "-Dhttp.proxyHost=rb-proxy-emea.internal.bosch.cloud " +
                  "-Dhttp.proxyPort=8080 " +
                  "-Dhttps.proxyHost=rb-proxy-emea.internal.bosch.cloud " +
                  "-Dhttps.proxyPort=8080 " +
                  "-Dhttp.auth.ntlm.domain= " +
                  "-Djdk.http.auth.tunneling.disabledSchemes= " +
                  "-Djdk.http.auth.proxying.disabledSchemes="
mvn dependency:get -Dartifact=junit:junit:4.13.2 -Dtransitive=false
```

The `-Djdk.http.auth.*.disabledSchemes=` (empty) lines re-enable Basic-over-tunnel which some JDK versions disable by default — needed for some NTLM-then-Basic proxies.

### Fallback B — Explicit credentials in `settings.xml` (last resort)

Only if the proxy refuses integrated auth. **Do not commit this file.**

```xml
<proxy>
  <id>bosch-emea-https</id>
  <active>true</active>
  <protocol>https</protocol>
  <host>rb-proxy-emea.internal.bosch.cloud</host>
  <port>8080</port>
  <username>YOUR_AD_USER</username>
  <password>YOUR_AD_PASSWORD</password>
  <nonProxyHosts>localhost|127.*|*.bosch.com|*.bosch.cloud|*.bosch.de</nonProxyHosts>
</proxy>
```

For NTLM you can also try `<username>DOMAIN\YOUR_AD_USER</username>` (e.g. `DE01\GOT4HC`).

Maven supports password encryption via `mvn --encrypt-master-password` and `mvn --encrypt-password` — see <https://maven.apache.org/guides/mini/guide-encryption.html>. Use it before storing anything sensitive.

### Fallback C — `cntlm` local proxy wrapper

If the corporate proxy speaks NTLMv2 only and the JDK negotiation fails, install [`cntlm`](https://cntlm.sourceforge.net/) locally. It runs as `127.0.0.1:3128`, handles NTLM upstream once, and Maven points at it instead. This avoids storing the AD password in `settings.xml` (cntlm uses a hashed `PassNTLMv2`). _Mentioned for awareness — not installed by this script._

### Fallback D — Whitelist request

Ask Bosch IT to whitelist `repo.maven.apache.org`, `repo1.maven.org`, `repo.spring.io`, and `maven.openjfx.io` for direct egress on this laptop. Then drop the `<proxies>` block entirely.

---

## Reproducibility notes

- **Network requirement:** must be on the Bosch corporate VPN whenever Maven needs to fetch new artifacts. After the first successful resolve, `~/.m2/repository/` caches everything and offline (`mvn -o`) builds work.
- **Settings location:** `~/.m2/settings.xml` is **outside** the repo and is intentionally not version-controlled. Each developer maintains their own. Do not commit it.
- **Credentials:** none are stored in the staged config. If Fallback B or C is needed, treat that file as a secret.
- **JDK:** project targets Java 17 (Spring Boot 2.7.18 + JavaFX 17). JDK 21 is forward-compatible for builds; if a plugin complains, install JDK 17 alongside and point `JAVA_HOME` at it before running Maven.

---

## Verification log (what was actually run)

| Step                                              | Result                                              |
| ------------------------------------------------- | --------------------------------------------------- |
| `mvn -v`                                          | **FAIL** — Maven not installed                      |
| `Test-Path ~/.m2/settings.xml`                    | False (and `~/.m2` did not exist)                   |
| `Test-NetConnection rb-proxy-emea... -Port 8080`  | **FAIL** — TCP timeout, ping timeout                |
| `TcpClient` probe with 5 s timeout                | TIMEOUT; DNS resolved to `139.15.111.168`           |
| Anonymous `Invoke-WebRequest` via proxy           | Skipped (proxy unreachable)                         |
| Created `~/.m2/`                                  | OK                                                  |
| Wrote `~/.m2/settings.xml`                        | OK (no prior file to back up)                       |
| `mvn dependency:get junit:junit:4.13.2`           | Skipped — Maven and proxy both unavailable          |
| `mvn dependency:resolve` (desktop-admin)          | Skipped — same reason                               |
| `mvn dependency:resolve` (backend-api)            | Skipped — same reason                               |
