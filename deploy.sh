#!/bin/bash
set -eu
trap 'echo "[ERROR] 배포 중 에러 발생 - 라인 번호: $LINENO"; exit 1' ERR

echo "[INFO] Blue-Green 배포 시작"

# 현재 Nginx가 사용하는 포트 추출
CURRENT_PORT=$(grep -oP '127.0.0.1:\K[0-9]+' /etc/nginx/conf.d/service-url.inc || echo "")

# DockerHub 관련 정보 추출
DOCKERHUB_USERNAME=$(grep -m 1 '^DOCKERHUB_USERNAME=' /home/ubuntu/.env | cut -d '=' -f2)
DOCKERHUB_REPO=$(grep -m 1 '^DOCKERHUB_REPO=' /home/ubuntu/.env | cut -d '=' -f2)

if [[ -z "$DOCKERHUB_USERNAME" || -z "$DOCKERHUB_REPO" ]]; then
  echo "[ERROR] DOCKERHUB_USERNAME 또는 DOCKERHUB_REPO 값이 비어 있습니다."
  echo "[DEBUG] /home/ubuntu/.env 내용:"
  cat /home/ubuntu/.env
  exit 1
fi

if [[ -z "$CURRENT_PORT" ]]; then
  echo "[ERROR] 현재 포트를 읽지 못했습니다. service-url.inc 포맷을 확인해주세요."
  sudo cat /etc/nginx/conf.d/service-url.inc
  exit 1
fi

echo "[DEBUG] 현재 Nginx 포트: $CURRENT_PORT"
sudo cat /etc/nginx/conf.d/service-url.inc

if [[ "$CURRENT_PORT" == "4001" ]]; then
  AFTER_COLOR="blue"
  BEFORE_COLOR="green"
  AFTER_PORT=4000
  BEFORE_PORT=4001
  COMPOSE_FILE="docker-compose.blue.yml"
  CONTAINER_NAME="app-blue"
else
  AFTER_COLOR="green"
  BEFORE_COLOR="blue"
  AFTER_PORT=4001
  BEFORE_PORT=4000
  COMPOSE_FILE="docker-compose.green.yml"
  CONTAINER_NAME="app-green"
fi

echo "[INFO] 최신 Docker 이미지 pull"
if ! sudo docker pull $DOCKERHUB_USERNAME/$DOCKERHUB_REPO; then
  echo "[ERROR] Docker 이미지 pull 실패 - 존재하지 않거나 로그인 오류"
  exit 1
fi

echo "[INFO] 새 컨테이너(${AFTER_COLOR}) 실행"
sudo docker-compose -p $AFTER_COLOR \
  -f /home/ubuntu/CrystalRose-back/$COMPOSE_FILE \
  --env-file /home/ubuntu/.env up -d

echo "[INFO] Health 체크 중"
for i in {1..20}; do
  echo "[시도 $i] curl http://127.0.0.1:${AFTER_PORT}/actuator/health"

  RESPONSE=$(curl -s -w "%{http_code}" -o health.json.tmp http://127.0.0.1:${AFTER_PORT}/actuator/health || true)
  STATUS_CODE="${RESPONSE:(-3)}"

  if [[ -f health.json.tmp ]]; then
    mv health.json.tmp health.json
    BODY=$(cat health.json)
  else
    BODY=""
  fi

  echo "[DEBUG] 응답 코드: $STATUS_CODE"
  echo "[DEBUG] 응답 바디: $BODY"

  if [[ "$STATUS_CODE" == "200" && "$BODY" == *'"status":"UP"'* ]]; then
    echo "[SUCCESS] 서버 정상 상태 확인됨"
    rm -f health.json health.json.tmp
    break
  fi

  sleep 5
done

if [[ "$BODY" != *'"status":"UP"'* ]]; then
  echo "[ERROR] 20회 시도에도 헬스체크 실패. 배포 중단"
  rm -f health.json health.json.tmp
  exit 1
fi

if [[ "$BODY" != *'"status":"UP"'* ]]; then
  echo "[ERROR] 헬스체크 실패 - 롤백 수행"

  echo "[INFO] 실패한 컨테이너 ${AFTER_COLOR} 정리"
  sudo docker-compose -p $AFTER_COLOR \
    -f /home/ubuntu/CrystalRose-back/$COMPOSE_FILE \
    --env-file /home/ubuntu/.env down

  echo "[INFO] 이전 컨테이너 ${BEFORE_COLOR} 재실행"
  ROLLBACK_COMPOSE="docker-compose.${BEFORE_COLOR}.yml"

  echo "[INFO] Nginx 포트 복구 (${AFTER_PORT} → ${BEFORE_PORT})"
  sudo bash -c "echo 'set \$service_url http://127.0.0.1:${BEFORE_PORT};' > /etc/nginx/conf.d/service-url.inc"

  echo "[DEBUG] 롤백된 service-url.inc 내용:"
  sudo cat /etc/nginx/conf.d/service-url.inc

  echo "[INFO] Nginx 설정 테스트"
  sudo nginx -t || { echo "[ERROR] Nginx 설정 오류 - 롤백 중단"; exit 1; }

  echo "[INFO] Nginx 재시작"
  sudo systemctl reload nginx

  echo "[ROLLBACK 완료] ${BEFORE_COLOR} 컨테이너로 복원"
  exit 1
fi

echo "[INFO] Nginx 포트 스위칭: ${BEFORE_PORT} → ${AFTER_PORT}"
sudo bash -c "echo 'set \$service_url http://127.0.0.1:${AFTER_PORT};' > /etc/nginx/conf.d/service-url.inc"

echo "[DEBUG] 변경된 service-url.inc 내용:"
sudo cat /etc/nginx/conf.d/service-url.inc

echo "[INFO] Nginx 설정 테스트"
sudo nginx -t || { echo "[ERROR] Nginx 설정 오류 - 배포 중단"; exit 1; }

echo "[INFO] Nginx Reload"
sudo systemctl reload nginx
sleep 2

echo "[INFO] 이전 컨테이너(${BEFORE_COLOR}) 종료"
sudo docker-compose -p $BEFORE_COLOR \
  -f /home/ubuntu/CrystalRose-back/docker-compose.${BEFORE_COLOR}.yml \
  --env-file /home/ubuntu/.env down

echo "[INFO] 최종 확인: Nginx 설정과 컨테이너 상태"
NGINX_PORT=$(grep -oP '127.0.0.1:\K[0-9]+' /etc/nginx/conf.d/service-url.inc)
ACTIVE_CONTAINER=$(sudo docker ps --format "table {{.Names}}\t{{.Ports}}" | grep app- | head -1)

echo "[DEBUG] Nginx 포트: $NGINX_PORT"
echo "[DEBUG] 활성 컨테이너: $ACTIVE_CONTAINER"

curl -f http://127.0.0.1:${NGINX_PORT}/actuator/health || {
  echo "[ERROR] 최종 헬스체크 실패 - 서비스 불일치!"
  exit 1
}

echo "[INFO] 모니터링 스택 확인"
if ! docker ps | grep -q prometheus; then
  echo "[INFO] Prometheus 시작"
  sudo docker-compose -f /home/ubuntu/CrystalRose-back/infra/docker-compose.monitoring.yml up -d prometheus
fi

if ! docker ps | grep -q grafana; then
  echo "[INFO] Grafana 시작"
  sudo docker-compose -f /home/ubuntu/CrystalRose-back/infra/docker-compose.monitoring.yml up -d grafana
fi

echo "[SUCCESS] 배포 완료: ${AFTER_COLOR} 컨테이너 실행 중 (port: ${AFTER_PORT})"
echo "[INFO] 모니터링 URL:"
echo "  - Prometheus: https://api.dodorose.com/monitor/prometheus/"
echo "  - Grafana: https://api.dodorose.com/monitor/grafana/"
echo "  - Health Check: https://api.dodorose.com/actuator/health"
echo "  - Metrics: https://api.dodorose.com/actuator/prometheus"
