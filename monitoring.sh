#!/bin/bash
set -eu

PROMETHEUS_SD_DIR="/home/ubuntu/prometheus/file_sd"
PROMETHEUS_SD_FILE="${PROMETHEUS_SD_DIR}/back_targets.yml"
MONITORING_COMPOSE="/home/ubuntu/docker-compose.monitoring.yml"

# Prometheus 타겟 업데이트
update_prometheus_target() {
    local target="$1"
    local environment="$2"
    
    echo "[INFO] Prometheus 타겟 업데이트: $target ($environment)"
    
    if ! sudo mkdir -p "$PROMETHEUS_SD_DIR"; then
        echo "[ERROR] 디렉토리 생성 실패: $PROMETHEUS_SD_DIR"
        return 1
    fi
    
    if [[ -f "$PROMETHEUS_SD_FILE" ]]; then
        if ! sudo cp "$PROMETHEUS_SD_FILE" "${PROMETHEUS_SD_FILE}.backup"; then
            echo "[WARN] 백업 파일 생성 실패"
        fi
    fi
    
    if ! sudo tee "$PROMETHEUS_SD_FILE" > /dev/null <<EOF
- labels:
    job: "spring-boot-dodorose"
    environment: "${environment}"
  targets: ["${target}"]
EOF
    then
        echo "[ERROR] 타겟 파일 생성 실패"
        return 1
    fi
    
    sudo chmod 644 "$PROMETHEUS_SD_FILE"
    return 0
}

# Prometheus 재로드
reload_prometheus() {
    local target="$1"
    
    echo "[INFO] Prometheus 재로드"
    
    if ! docker ps | grep -q prometheus; then
        echo "[INFO] Prometheus 시작"
        if ! sudo docker-compose -f "$MONITORING_COMPOSE" up -d prometheus; then
            echo "[ERROR] Prometheus 시작 실패"
            return 1
        fi
        sleep 10
    fi
    
    if curl -sf -X POST http://localhost:9090/-/reload; then
        echo "[SUCCESS] Prometheus 재로드 완료"
        sleep 3
        return 0
    else
        echo "[INFO] Prometheus 재시작"
        if sudo docker restart prometheus; then
            sleep 10
            if curl -sf http://localhost:9090/-/healthy > /dev/null; then
                echo "[SUCCESS] Prometheus 재시작 완료"
                return 0
            else
                echo "[ERROR] Prometheus 재시작 후 응답 없음"
                return 1
            fi
        else
            echo "[ERROR] Prometheus 재시작 실패"
            return 1
        fi
    fi
}

# 모니터링 상태 확인
check_status() {
    echo ""
    echo "=== 모니터링 상태 ==="
    
    if docker ps | grep -q prometheus && curl -sf http://localhost:9090/-/healthy > /dev/null 2>&1; then
        echo "  - Prometheus: 정상"
    else
        echo "  - Prometheus: 오류"
    fi
    
    if docker ps | grep -q grafana && curl -sf http://localhost:3100/api/health > /dev/null 2>&1; then
        echo "  - Grafana: 정상"
    else
        echo "  - Grafana: 오류"
    fi
    echo ""
}

# 메인 업데이트 함수
update_monitoring() {
    local target="$1"
    local environment="$2"
    
    if ! update_prometheus_target "$target" "$environment"; then
        echo "[ERROR] 타겟 업데이트 실패"
        return 1
    fi
    
    if ! reload_prometheus "$target"; then
        echo "[ERROR] Prometheus 재로드 실패"
        return 1
    fi
    
    check_status
    echo "[SUCCESS] 모니터링 업데이트 완료"
    return 0
}

# 메인 실행 로직
case "${1:-}" in
    "update")
        if [[ $# -ne 3 ]]; then
            echo "사용법: $0 update <target> <environment>"
            echo "예시: $0 update app-blue:4000 blue"
            exit 1
        fi
        update_monitoring "$2" "$3"
        ;;
    "check")
        check_status
        ;;
    "reload")
        if [[ $# -ne 2 ]]; then
            echo "사용법: $0 reload <target>"
            exit 1
        fi
        reload_prometheus "$2"
        ;;
    *)
        echo "사용법: $0 {update|check|reload}"
        echo ""
        echo "  update <target> <env>  - 타겟 업데이트"
        echo "  check                  - 상태 확인"
        echo "  reload <target>        - 재로드"
        exit 1
        ;;
esac
