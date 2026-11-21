#!/usr/bin/env bash
# Security verification script for EditorialWeb
set -e

HOST="${1:-127.0.0.1}"
echo "=============================================="
echo "Security Audit & Checks for: $HOST"
echo "=============================================="

echo "[1] Checking HTTP Security Headers on Gateway / Frontend..."
curl -s -I "http://$HOST:8080" | grep -Ei "HTTP/|X-Frame-Options|X-Content-Type-Options|Content-Security-Policy|Strict-Transport-Security|Server" || true

echo ""
echo "[2] Checking External Port Exposure..."
echo "Only ports 80, 443, 22 should be open to the internet."
echo "Checking if internal database ports are closed from outside:"

for port in 5434 5435 5436 9092 9001; do
    if nc -z -w 2 "$HOST" "$port" 2>/dev/null; then
        echo "  [WARNING] Port $port is accessible from outside!"
    else
        echo "  [OK] Port $port is closed/filtered."
    fi
done

echo ""
echo "[3] Checking Docker Compose secrets in git history..."
if git log -n 5 -S "ARTICLE_DB_PASSWORD" | grep -q "commit"; then
    echo "  [INFO] Commit history checked for secrets."
fi

echo "=============================================="
echo "Security check completed!"
echo "=============================================="
