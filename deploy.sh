#!/bin/bash
set -eu
trap 'echo "[ERROR] 배포 중 에러 발생 - 라인 번호: $LINENO"; exit 1' ERR

echo "Blue-Green 배포 시작"

CURRENT_PORT=$(grep -oP '127.0.0.1:\K[0-9]+' /etc/nginx/sites-available/dodorose)

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

echo "새 컨테이너(${AFTER_COLOR}) 실행"
sudo docker-compose -p $AFTER_COLOR \
  -f /home/ubuntu/CrystalRose-back/$COMPOSE_FILE \
  --env-file /home/ubuntu/.env up -d

echo "Health 체크 중"
for i in {1..10}
do
  echo "시도 $i: curl http://127.0.0.1:${AFTER_PORT}/actuator/health"
  HEALTH=$(curl -s http://127.0.0.1:${AFTER_PORT}/actuator/health || true)

  if [[ "$HEALTH" == *'"status":"UP"'* ]]; then
    echo "서버 정상 상태 확인"
    break
  fi
  sleep 5
done

if [[ "$HEALTH" != *'"status":"UP"'* ]]; then
  echo "헬스체크 실패 - 롤백 수행"

  echo "실패한 컨테이너 ${AFTER_COLOR} 정리"
  sudo docker-compose -p $AFTER_COLOR \
    -f /home/ubuntu/CrystalRose-back/$COMPOSE_FILE \
    --env-file /home/ubuntu/.env down

  echo "이전 컨테이너 ${BEFORE_COLOR} 재실행"
  ROLLBACK_COMPOSE="docker-compose.${BEFORE_COLOR}.yml"
  sudo docker-compose -p $BEFORE_COLOR \
    -f /home/ubuntu/CrystalRose-back/$ROLLBACK_COMPOSE \
    --env-file /home/ubuntu/.env up -d

  echo "Nginx 포트 복구 (${AFTER_PORT} → ${BEFORE_PORT})"
  sudo sed -i "s/${AFTER_PORT}/${BEFORE_PORT}/" /etc/nginx/sites-available/dodorose

  echo "Nginx 설정 테스트"
  sudo nginx -t || { echo "Nginx 설정 에러 - 롤백 중 중단"; exit 1; }

  echo "Nginx 재시작"
  sudo nginx -s reload

  echo "[롤백 완료] ${BEFORE_COLOR} 복원"
  exit 1
fi

echo "Nginx 포트 스위칭: ${BEFORE_PORT} → ${AFTER_PORT}"
sudo sed -i "s/${BEFORE_PORT}/${AFTER_PORT}/" /etc/nginx/sites-available/dodorose
sudo nginx -s reload

echo "이전 컨테이너(${BEFORE_COLOR}) 종료"
sudo docker-compose -p $BEFORE_COLOR \
  -f /home/ubuntu/CrystalRose-back/docker-compose.${BEFORE_COLOR}.yml \
  --env-file /home/ubuntu/.env down

echo "배포 완료: ${AFTER_COLOR} 컨테이너 실행 중 (port: ${AFTER_PORT})"
