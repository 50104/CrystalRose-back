#!/bin/bash
set -eu
trap 'echo "[ERROR] 배포 중 에러 발생 - 라인 번호: $LINENO"; exit 1' ERR

echo "[INFO] Blue-Green 배포 시작"

# 현재 Nginx가 사용하는 포트 추출
CURRENT_PORT=$(grep -oP '127.0.0.1:\K[0-9]+' /etc/nginx/conf.d/service-url.inc || echo "")

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

echo "[INFO] 새 컨테이너(${AFTER_COLOR}) 실행"
sudo docker-compose -p $AFTER_COLOR \
  -f /home/ubuntu/CrystalRose-back/$COMPOSE_FILE \
  --env-file /home/ubuntu/.env up -d

echo "[INFO] Health 체크 중"
for i in {1..10}
do
  echo "시도 $i: curl http://127.0.0.1:${AFTER_PORT}/actuator/health"
  HEALTH=$(curl -s http://127.0.0.1:${AFTER_PORT}/actuator/health || true)

  if [[ "$HEALTH" == *'"status":"UP"'* ]]; then
    echo "[SUCCESS] 서버 정상 상태 확인됨"
    break
  fi
  sleep 5
done

if [[ "$HEALTH" != *'"status":"UP"'* ]]; then
  echo "[ERROR] 헬스체크 실패 - 롤백 수행"

  echo "[INFO] 실패한 컨테이너 ${AFTER_COLOR} 정리"
  sudo docker-compose -p $AFTER_COLOR \
    -f /home/ubuntu/CrystalRose-back/$COMPOSE_FILE \
    --env-file /home/ubuntu/.env down

  echo "[INFO] 이전 컨테이너 ${BEFORE_COLOR} 재실행"
  ROLLBACK_COMPOSE="docker-compose.${BEFORE_COLOR}.yml"
  sudo docker-compose -p $BEFORE_COLOR \
    -f /home/ubuntu/CrystalRose-back/$ROLLBACK_COMPOSE \
    --env-file /home/ubuntu/.env up -d

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

echo "[SUCCESS] 배포 완료: ${AFTER_COLOR} 컨테이너 실행 중 (port: ${AFTER_PORT})"
