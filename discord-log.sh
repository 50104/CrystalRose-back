#!/bin/bash
set -eu

# 로그 디렉토리 경로 (컨테이너 내부 기준)
LOG_DIR="/app/logs"

# Discord 웹훅 URL
WEBHOOK_ERROR="https://discord.com/api/webhooks/1398294276437442742/tro8eCPjHC-wQpObpDBWgM9K6YKqR2VvdhbP45-vOoxjLhLtGnh9H3TQlr5-2PduaKNK"
WEBHOOK_ACCESS="https://discord.com/api/webhooks/1398295288766140558/mPRVdAX33mDUBerqku7oO0F2LaRBVkih01Gzsx1iYr-gvB1pqgEvqR4ZqxL6sPKt7jSK"
WEBHOOK_SQL="https://discord.com/api/webhooks/1398295425932595302/guDHMwAOY6Q4INRGTrCiYPi3SpQqANFoEEetJa4IQ-DWZOIFMwb51iILN0KiuWiNHqGq"
WEBHOOK_WS="https://discord.com/api/webhooks/1398295467317788813/Rm04ZSwoRtQT2vR9WTYEEihRICxrmQdW6xdojIbp0mLxw9ewD43nnSPFYLQ3fpT6JeW3"

# 로그 중복 전송 방지용 해시 캐시
declare -A SENT_CACHE

send_discord_message() {
  local webhook_url="$1"
  local message="$2"

  # 동일 메시지 반복 방지
  local hash
  hash=$(echo "$message" | md5sum | cut -d' ' -f1)
  if [[ -n "${SENT_CACHE[$hash]:-}" ]]; then
    return
  fi
  SENT_CACHE[$hash]=1

  curl -s -H "Content-Type: application/json" -X POST \
    -d "{\"content\": \"\`\`\`\n${message}\n\`\`\`\"}" "$webhook_url" > /dev/null || echo "[WARN] 전송 실패: $message"
}

monitor_log() {
  local file="$1"
  local webhook="$2"

  mkdir -p "$(dirname "$file")"
  touch "$file"

  tail -n 0 -F "$file" | while read -r line; do
    [[ -n "$line" ]] && send_discord_message "$webhook" "$line"
  done &
}

# 로그 파일별 모니터링 시작
monitor_log "$LOG_DIR/error.log" "$WEBHOOK_ERROR"
monitor_log "$LOG_DIR/access.log" "$WEBHOOK_ACCESS"
monitor_log "$LOG_DIR/sql.log" "$WEBHOOK_SQL"
monitor_log "$LOG_DIR/websocket.log" "$WEBHOOK_WS"

# 무한 대기
wait
