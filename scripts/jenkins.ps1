param (
    [Parameter(Position = 0, Mandatory = $false)]
    [ValidateSet("start", "stop", "status", "restart", "logs", "build-status")]
    [string]$Action = "status"
)

$VPS_HOST = "42.96.2.76"
$VPS_USER = "root"
$JENKINS_URL = "http://${VPS_HOST}:8081"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   PENTAVA VPS - QUAN LY JENKINS ON-DEMAND ($VPS_HOST)" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

switch ($Action.ToLower()) {
    "start" {
        Write-Host ">> Dang khoi dong container Jenkins tren VPS..." -ForegroundColor Yellow
        ssh -o StrictHostKeyChecking=no "${VPS_USER}@${VPS_HOST}" "docker start jenkins"
        Start-Sleep -Seconds 2
        Write-Host "[OK] Jenkins da duoc khoi dong!" -ForegroundColor Green
        Write-Host "Dashboard: $JENKINS_URL" -ForegroundColor Cyan
        Write-Host "Luu y: Khi deploy xong, chay '.\scripts\jenkins.ps1 stop' de tat Jenkins giai phong CPU/RAM." -ForegroundColor Gray
    }
    "stop" {
        Write-Host ">> Dang tat container Jenkins tren VPS..." -ForegroundColor Yellow
        ssh -o StrictHostKeyChecking=no "${VPS_USER}@${VPS_HOST}" "docker stop jenkins"
        Write-Host "[OK] Jenkins da tat an toan! RAM va CPU da duoc giai phong cho Backend." -ForegroundColor Green
    }
    "restart" {
        Write-Host ">> Dang khoi dong lai Jenkins tren VPS..." -ForegroundColor Yellow
        ssh -o StrictHostKeyChecking=no "${VPS_USER}@${VPS_HOST}" "docker restart jenkins"
        Write-Host "[OK] Jenkins da khoi dong lai thanh cong!" -ForegroundColor Green
        Write-Host "Dashboard: $JENKINS_URL" -ForegroundColor Cyan
    }
    "logs" {
        Write-Host ">> Xem 50 dong log gan nhat cua Jenkins:" -ForegroundColor Yellow
        ssh -o StrictHostKeyChecking=no "${VPS_USER}@${VPS_HOST}" "docker logs --tail 50 -f jenkins"
    }
    "build-status" {
        Write-Host ">> Kiem tra ban build moi nhat cua pipeline pentava-backend..." -ForegroundColor Yellow
        ssh -o StrictHostKeyChecking=no "${VPS_USER}@${VPS_HOST}" '
            LAST_BUILD=$(docker exec jenkins cat /var/jenkins_home/jobs/pentava-backend/builds/permalinks 2>/dev/null | grep lastCompletedBuild | awk "{print \$2}")
            if [ -z "$LAST_BUILD" ]; then
                echo "Chua tim thay ban build nao."
            else
                echo "=== BAN BUILD MOI NHAT: #$LAST_BUILD ==="
                docker exec jenkins tail -n 12 "/var/jenkins_home/jobs/pentava-backend/builds/$LAST_BUILD/log" 2>/dev/null || true
            fi
            echo ""
            echo "=== TRANG THAI CUA CONTAINER BACKEND TREN VPS ==="
            docker ps --format "table {{.Names}}\t{{.Status}}" | grep pentava
        '
    }
    "status" {
        Write-Host ">> Kiem tra trang thai Jenkins va tai nguyen VPS..." -ForegroundColor Yellow
        ssh -o StrictHostKeyChecking=no "${VPS_USER}@${VPS_HOST}" '
            echo "--- CONTAINER JENKINS ---"
            docker ps -a --filter name=jenkins --format "Ten: {{.Names}} | Trang thai: {{.Status}} | Cong: {{.Ports}}"
            echo ""
            echo "--- TAI NGUYEN VPS ---"
            uptime
            free -m
        '
        Write-Host ""
        Write-Host "Lenh su dung:" -ForegroundColor Cyan
        Write-Host "  .\scripts\jenkins.ps1 start   -> Bat Jenkins" -ForegroundColor Green
        Write-Host "  .\scripts\jenkins.ps1 stop    -> Tat Jenkins" -ForegroundColor Red
        Write-Host "  .\scripts\jenkins.ps1 status  -> Xem trang thai" -ForegroundColor Gray
    }
}
