#!/bin/bash

# =====================================================
# 多租户电商平台 - 服务启动脚本
# 使用方法:
#   bash start-services.sh          启动全部服务
#   bash start-services.sh core     只启动核心服务(IAM/商品/订单/网关)
#   bash start-services.sh stop     停止所有服务
# =====================================================

PROJECT_DIR=$(cd "$(dirname "$0")"; pwd)
LOG_DIR="${PROJECT_DIR}/logs"
PID_DIR="${PROJECT_DIR}/.pids"

mkdir -p "${LOG_DIR}"
mkdir -p "${PID_DIR}"

# 服务定义
ALL_SERVICES=(
    "ecommerce-iam:8001"
    "ecommerce-product:8002"
    "ecommerce-order:8003"
    "ecommerce-recommendation:8004"
    "ecommerce-approval:8005"
    "ecommerce-analytics:8006"
    "ecommerce-notification:8007"
    "ecommerce-payment:8008"
    "ecommerce-audit:8009"
    "ecommerce-gateway:8080"
)

CORE_SERVICES=(
    "ecommerce-iam:8001"
    "ecommerce-product:8002"
    "ecommerce-order:8003"
    "ecommerce-gateway:8080"
)

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

check_maven() {
    if ! command -v mvn &> /dev/null; then
        echo -e "${RED}[错误] Maven未安装，请先安装Maven 3.8+${NC}"
        echo "  macOS: brew install maven"
        echo "  或者从 https://maven.apache.org/download.cgi 下载"
        exit 1
    fi
}

check_port() {
    port=$1
    if lsof -Pi :${port} -sTCP:LISTEN -t >/dev/null 2>&1; then
        return 0  # 端口被占用
    else
        return 1  # 端口可用
    fi
}

wait_for_port() {
    port=$1
    service_name=$2
    timeout=120
    count=0
    echo -n "等待 ${service_name} (${port}) 启动..."
    while [ $count -lt $timeout ]; do
        if check_port $port; then
            echo -e " ${GREEN}OK${NC}"
            return 0
        fi
        sleep 2
        count=$((count + 2))
        echo -n "."
    done
    echo -e " ${RED}超时!${NC}"
    return 1
}

start_service() {
    service_def=$1
    service_name=$(echo $service_def | cut -d':' -f1)
    service_port=$(echo $service_def | cut -d':' -f2)
    service_dir="${PROJECT_DIR}/${service_name}"
    log_file="${LOG_DIR}/${service_name}.log"
    pid_file="${PID_DIR}/${service_name}.pid"

    echo ""
    echo -e "${YELLOW}========================================${NC}"
    echo -e "${YELLOW}启动服务: ${service_name} (端口:${service_port})${NC}"
    echo -e "${YELLOW}========================================${NC}"

    if check_port $service_port; then
        echo -e "${YELLOW}[!] 端口 ${service_port} 已被占用，跳过 ${service_name}${NC}"
        PID=$(lsof -Pi :${service_port} -sTCP:LISTEN -t | head -1)
        echo "  PID: $PID"
        echo $PID > "$pid_file"
        return 0
    fi

    if [ ! -d "${service_dir}" ]; then
        echo -e "${RED}[错误] 服务目录不存在: ${service_dir}${NC}"
        return 1
    fi

    cd "${service_dir}"

    # 后台启动，不等待测试
    nohup mvn spring-boot:run \
        -Dspring-boot.run.jvmArguments="-Xms256m -Xmx512m" \
        > "$log_file" 2>&1 &
    echo $! > "$pid_file"

    sleep 5
    wait_for_port $service_port $service_name

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}[✓] ${service_name} 启动成功!${NC}"
        echo "  日志文件: ${log_file}"
        echo "  健康检查: curl http://localhost:${service_port}/$( [ "$service_name" != "ecommerce-gateway" ] && echo "${service_name#ecommerce-}" | sed 's/-//g' )/health || curl http://localhost:${service_port}/health"
    else
        echo -e "${RED}[✗] ${service_name} 启动失败!${NC}"
        echo "  查看日志: tail -100f ${log_file}"
    fi

    cd "${PROJECT_DIR}"
}

stop_services() {
    echo ""
    echo -e "${YELLOW}停止所有服务...${NC}"
    for service_def in "${ALL_SERVICES[@]}"; do
        service_name=$(echo $service_def | cut -d':' -f1)
        service_port=$(echo $service_def | cut -d':' -f2)
        pid_file="${PID_DIR}/${service_name}.pid"

        if [ -f "$pid_file" ]; then
            PID=$(cat "$pid_file")
            if kill -0 $PID 2>/dev/null; then
                kill $PID 2>/dev/null
                echo -e "${GREEN}  停止 ${service_name} (PID:${PID})${NC}"
                sleep 1
                if kill -0 $PID 2>/dev/null; then
                    kill -9 $PID 2>/dev/null
                fi
            fi
            rm -f "$pid_file"
        else
            # 按端口查找
            PID=$(lsof -Pi :${service_port} -sTCP:LISTEN -t 2>/dev/null | head -1)
            if [ -n "$PID" ]; then
                kill $PID 2>/dev/null
                echo -e "${GREEN}  停止 ${service_name} (PID:${PID})${NC}"
                sleep 1
                if kill -0 $PID 2>/dev/null; then
                    kill -9 $PID 2>/dev/null
                fi
            fi
        fi
    done
    echo -e "${GREEN}所有服务已停止${NC}"
}

print_usage() {
    echo "用法: bash start-services.sh [命令]"
    echo ""
    echo "命令:"
    echo "  all     启动全部10个服务 (默认)"
    echo "  core    只启动核心服务 (IAM/商品/订单/网关) - 推荐首次使用"
    echo "  stop    停止所有服务"
    echo ""
    echo "示例:"
    echo "  bash start-services.sh core    # 启动4个核心服务"
    echo "  bash start-services.sh         # 启动全部服务"
    echo "  bash start-services.sh stop    # 停止所有服务"
    echo ""
    echo "前置条件:"
    echo "  1. 启动MySQL并导入 sql/init.sql"
    echo "  2. 启动Redis (默认端口6379)"
    echo "  3. 安装JDK17+ 和 Maven3.8+"
    echo ""
    echo "启动后验证:"
    echo "  bash test-quick-start.sh"
}

# =====================================================
# 主逻辑
# =====================================================

MODE="${1:-all}"

case $MODE in
    -h|--help|help)
        print_usage
        exit 0
        ;;
    stop)
        stop_services
        exit 0
        ;;
esac

check_maven

echo ""
echo "================================================"
echo "  多租户电商平台 - 服务启动脚本"
echo "  模式: ${MODE}"
echo "  项目目录: ${PROJECT_DIR}"
echo "================================================"

echo ""
echo -e "${YELLOW}[前置检查]${NC}"
echo "  → 请确保已启动 MySQL 和 Redis"
echo "  → 数据库初始化: mysql -u root -p < sql/init.sql"
echo ""
read -p "按 Enter 继续，或 Ctrl+C 取消..."

# 选择要启动的服务
if [ "$MODE" = "core" ]; then
    SERVICES=("${CORE_SERVICES[@]}")
else
    SERVICES=("${ALL_SERVICES[@]}")
fi

echo ""
echo -e "${GREEN}将启动以下 ${#SERVICES[@]} 个服务:${NC}"
for svc in "${SERVICES[@]}"; do
    echo "  - $svc"
done
echo ""

# 启动服务
for svc in "${SERVICES[@]}"; do
    start_service $svc
done

echo ""
echo "================================================"
echo -e "${GREEN}  服务启动流程完成!${NC}"
echo "================================================"
echo ""
echo "下一步操作:"
echo "  1. 验证服务健康: curl http://localhost:8080/health (网关)"
echo "  2. 运行完整测试: bash test-quick-start.sh"
echo "  3. 查看各个服务日志: tail -f logs/*.log"
echo ""
echo "停止服务: bash start-services.sh stop"
echo ""
