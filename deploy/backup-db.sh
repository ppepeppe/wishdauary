#!/bin/sh
set -eu

cd "$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
umask 077
mkdir -p backups
backup_path="backups/wishdauary-$(date -u +%Y%m%dT%H%M%SZ)-$$.dump"
partial_path="$backup_path.partial"
trap 'rm -f "$partial_path"' EXIT HUP INT TERM

docker compose exec -T postgres pg_dump \
    -U wishdauary -d wishdauary --format=custom --no-owner --no-acl > "$partial_path"
test -s "$partial_path"
mv "$partial_path" "$backup_path"
printf 'Saved %s\n' "$backup_path"
