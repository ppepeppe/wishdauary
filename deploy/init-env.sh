#!/bin/sh
set -eu

cd "$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"

if [ -e .env ]; then
    printf '%s\n' '.env already exists; leaving it unchanged.' >&2
    exit 1
fi

command -v openssl >/dev/null 2>&1 || {
    printf '%s\n' 'openssl is required.' >&2
    exit 1
}

umask 077
db_password=$(openssl rand -hex 32)
token_secret=$(openssl rand -hex 48)
# noclobber also prevents replacing a file created after the check above.
set -C
sed \
    -e "s/^WISHDIARY_DB_PASSWORD=.*/WISHDIARY_DB_PASSWORD=$db_password/" \
    -e "s/^WISHDIARY_AUTH_TOKEN_SECRET=.*/WISHDIARY_AUTH_TOKEN_SECRET=$token_secret/" \
    .env.example > .env
printf '%s\n' 'Created .env (owner read/write only). Set the public origin before starting.'
