#!/usr/bin/env bash
# Smoke testing script for EditorialWeb API
set -e

BASE_URL="${1:-http://localhost:8080}"
echo "=========================================="
echo "Running Smoke Tests against: $BASE_URL"
echo "=========================================="

# 1. Check Gateway & JWKS availability
echo -n "[1/5] Checking Auth Service JWKS endpoint... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/.well-known/jwks.json" || true)
if [ "$HTTP_CODE" = "200" ]; then
    echo "PASS (HTTP 200)"
else
    echo "FAIL (HTTP $HTTP_CODE)"
fi

# 2. Check Public Articles Endpoint
echo -n "[2/5] Checking GET /api/articles... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/articles" || true)
if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "401" ]; then
    echo "PASS (HTTP $HTTP_CODE)"
else
    echo "FAIL (HTTP $HTTP_CODE)"
fi

# 3. Check Registration validation (should reject empty payload)
echo -n "[3/5] Checking POST /api/auth/register validation... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" -d '{}' || true)
if [ "$HTTP_CODE" = "400" ]; then
    echo "PASS (HTTP 400 Validation Error as expected)"
else
    echo "FAIL (HTTP $HTTP_CODE)"
fi

# 4. Check Frontend availability (if served on port 80 / root)
FRONTEND_URL="${BASE_URL/8080/80}"
echo -n "[4/5] Checking Frontend availability on $FRONTEND_URL... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$FRONTEND_URL/" || true)
if [ "$HTTP_CODE" = "200" ]; then
    echo "PASS (HTTP 200)"
else
    echo "INFO (HTTP $HTTP_CODE - check port 80)"
fi

# 5. Check 404 handler
echo -n "[5/5] Checking Non-existent endpoint... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/non-existent-route-123" || true)
if [ "$HTTP_CODE" = "404" ]; then
    echo "PASS (HTTP 404 Not Found)"
else
    echo "INFO (HTTP $HTTP_CODE)"
fi

echo "=========================================="
echo "Smoke tests completed!"
echo "=========================================="
