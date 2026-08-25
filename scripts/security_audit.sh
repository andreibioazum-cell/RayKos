#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

fail() {
  printf 'security audit: %s\n' "$*" >&2
  exit 1
}

printf '%s\n' '==> Verifying pinned native dependencies'
[[ "$(git config -f .gitmodules --get submodule.byedpi.url)" == \
  "https://github.com/hufrea/byedpi.git" ]] || fail 'unexpected ByeDPI submodule URL'
[[ "$(git config -f .gitmodules --get submodule.hev-socks5-tunnel.url)" == \
  "https://github.com/heiher/hev-socks5-tunnel.git" ]] || fail 'unexpected tun2socks submodule URL'

for path in app/src/main/cpp/byedpi app/src/main/jni/hev-socks5-tunnel; do
  expected="$(git ls-tree HEAD "$path" | awk '{print $3}')"
  [[ -n "$expected" ]] || fail "missing gitlink for $path"
  [[ -e "$path/.git" ]] || fail "$path is not initialized (run git submodule update --init --recursive)"
  actual="$(git -C "$path" rev-parse HEAD)"
  [[ "$actual" == "$expected" ]] || fail "$path is at $actual, expected $expected"
done

printf '%s\n' '==> Rejecting unexpected prebuilt executable payloads'
while IFS= read -r path; do
  case "$path" in
    gradle/wrapper/gradle-wrapper.jar) ;;
    *) fail "unexpected tracked binary payload: $path" ;;
  esac
done < <(git ls-files | grep -Ei '\.(apk|aab|apks|dex|class|jar|so|exe|dll|dylib)$' || true)

expected_wrapper_sha='7d3a4ac4de1c32b59bc6a4eb8ecb8e612ccd0cf1ae1e99f66902da64df296172'
actual_wrapper_sha="$(sha256sum gradle/wrapper/gradle-wrapper.jar | awk '{print $1}')"
[[ "$actual_wrapper_sha" == "$expected_wrapper_sha" ]] || fail 'Gradle wrapper JAR checksum mismatch'
grep -q '^distributionSha256Sum=bd71102213493060956ec229d946beee57158dbd89d0e62b91bca0fa2c5f3531$' \
  gradle/wrapper/gradle-wrapper.properties || fail 'Gradle distribution checksum is missing or changed'

printf '%s\n' '==> Checking for dynamic-code and silent-install APIs'
if git grep -nE \
  '(DexClassLoader|PathClassLoader|dalvik\.system|REQUEST_INSTALL_PACKAGES|setJavaScriptEnabled[[:space:]]*\([[:space:]]*true)' \
  -- app ':!app/src/main/cpp/byedpi' ':!app/src/main/jni/hev-socks5-tunnel'; then
  fail 'dynamic-code loading or package-install capability found'
fi

printf '%s\n' '==> Checking for accidentally committed credentials'
if git grep -nEI \
  '(BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY|discord(app)?\.com/api/webhooks/|gh[pousr]_[A-Za-z0-9_]{30,}|AIza[0-9A-Za-z_-]{30,})' \
  -- . ':!scripts/security_audit.sh'; then
  fail 'possible credential found in tracked source'
fi

printf '%s\n' '==> Checking Android component hardening'
python3 - <<'PY'
import xml.etree.ElementTree as ET

android = "{http://schemas.android.com/apk/res/android}"
root = ET.parse("app/src/main/AndroidManifest.xml").getroot()
application = root.find("application")
assert application is not None
assert application.get(android + "allowBackup") == "false", "application backups must stay disabled"

activities = {
    item.get(android + "name"): item
    for item in application.findall("activity")
}
assert activities[".activities.MainActivity"].get(android + "exported") == "true"
assert activities[".activities.ToggleActivity"].get(android + "exported") == "true"

exported_allowed = {".activities.MainActivity", ".activities.ToggleActivity"}
for name, item in activities.items():
    if name in exported_allowed:
        continue
    assert item.get(android + "exported") == "false", f"unexpected exported activity: {name}"

source = open(
    "app/src/main/java/io/github/romanvht/byedpi/activities/ToggleActivity.kt",
    encoding="utf-8",
).read()
assert "isTrustedShortcut()" in source, "exported shortcut activity must authenticate calls"
PY

printf '%s\n' '==> Checking patch whitespace'
git diff --check

if command -v clamscan >/dev/null 2>&1; then
  printf '%s\n' '==> Running optional ClamAV scan'
  clamscan --infected --recursive \
    --exclude-dir='^\.git$' \
    --exclude-dir='(^|/)build$' \
    app/src/main
else
  printf '%s\n' '==> ClamAV is not installed; static security checks completed'
fi

printf '%s\n' 'Security audit passed.'
