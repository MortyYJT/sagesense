#!/usr/bin/env bash
set -euo pipefail

repo_root="$(git rev-parse --show-toplevel)"
output_dir="${1:-$repo_root/dist}"
commit="$(git -C "$repo_root" rev-parse HEAD)"
short_commit="$(git -C "$repo_root" rev-parse --short=7 HEAD)"
source_zip="$output_dir/sagesense-source-$short_commit.zip"
apk_source="$repo_root/android/app/build/outputs/apk/debug/app-debug.apk"
apk_output="$output_dir/SageSense-RC-$short_commit-debug.apk"
checksums="$output_dir/SHA256SUMS.txt"

mkdir -p "$output_dir"

if ! git -C "$repo_root" diff --quiet || ! git -C "$repo_root" diff --cached --quiet; then
    echo "Note: tracked working-tree changes exist; the source ZIP uses committed HEAD $short_commit."
fi

git -C "$repo_root" archive \
    --format=zip \
    --prefix=sagesense/ \
    --output="$source_zip" \
    "$commit"

unzip -t "$source_zip" >/dev/null

forbidden_pattern='(^|/)(\.env$|\.env\.(?!example$)[^/]+$|local\.properties$|[^/]+\.(jks|keystore|apk)$|\.git/|\.idea/|\.android-sdk/|tmp/|Sixth[ _-]?Sense[^/]*\.(pdf|fig)$)'
if unzip -Z1 "$source_zip" | rg --pcre2 -i "$forbidden_pattern"; then
    echo "Refusing bundle: forbidden private, secret, binary, or original-design material found." >&2
    exit 1
fi

if [[ ! -f "$apk_source" ]]; then
    echo "Missing RC APK. Run: cd android && ./gradlew assembleDebug" >&2
    exit 1
fi

install -m 0644 "$apk_source" "$apk_output"

(
    cd "$output_dir"
    shasum -a 256 "$(basename "$source_zip")" "$(basename "$apk_output")" > "$(basename "$checksums")"
)

echo "Source ZIP: $source_zip"
echo "RC APK:     $apk_output"
echo "Checksums:  $checksums"
echo "Commit:     $commit"
