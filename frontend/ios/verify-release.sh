#!/bin/sh
set -eu
[ "${CONFIGURATION:-Debug}" = "Release" ] || exit 0
metadata="${SRCROOT}/App/public/mobile-build.json"
if [ ! -f "$metadata" ]; then
  echo 'error: Run npm run mobile:sync -- ios --api-url https://YOUR_BACKEND before archiving.'
  exit 1
fi
development=$(/usr/bin/plutil -extract development raw -o - "$metadata")
api_url=$(/usr/bin/plutil -extract apiBaseUrl raw -o - "$metadata")
if [ "$development" != 'false' ]; then
  echo 'error: A local test bundle cannot be archived. Run mobile:sync without --development.'
  exit 1
fi
case "$api_url" in
  https://*) ;;
  *) echo 'error: Release requires an HTTPS backend.'; exit 1 ;;
esac
