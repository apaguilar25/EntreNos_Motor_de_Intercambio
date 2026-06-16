$ErrorActionPreference = "Stop"
$backendDir = "C:\Users\PC\IdeaProjects\EntreNos_Motor_de_Intercambio\backend"
$dataDir = "C:\Users\PC\IdeaProjects\EntreNos_Motor_de_Intercambio\data"
$collectionPath = "C:\Users\PC\IdeaProjects\EntreNos_Motor_de_Intercambio\postman_completo.json"

Write-Host "=== Starting Backend ==="

# Clean data
Remove-Item -Path "$dataDir\*.json" -Force -ErrorAction SilentlyContinue

# Start backend
Set-Location $backendDir
$logFile = "$dataDir\..\backend-run.log"
$p = Start-Process -FilePath "cmd.exe" -ArgumentList "/c .\mvnw.cmd spring-boot:run" -NoNewWindow -PassThru

Write-Host "Backend started with PID: $($p.Id)"

# Wait for readiness
$timeout = [DateTime]::Now.AddSeconds(120)
$ready = $false
while ([DateTime]::Now -lt $timeout) {
    Start-Sleep -Seconds 3
    try {
        $r = Invoke-WebRequest -Uri "http://localhost:8080/api/publicaciones" -TimeoutSec 3 -UseBasicParsing -ErrorAction Stop
        if ($r.StatusCode -eq 200 -or $r.StatusCode -eq 403 -or $r.StatusCode -eq 401) {
            $ready = $true
            break
        }
    } catch {}
}

if (-not $ready) {
    Write-Host "Backend failed to start. Stopping."
    Stop-Process -Id $p.Id -Force -ErrorAction SilentlyContinue
    exit 1
}

Write-Host "Backend is READY"

# Run Postman collection
Write-Host "=== Running Postman Collection ==="
newman run "$collectionPath" --reporters cli --timeout-request 15000 --timeout-script 30000

Write-Host "=== Tests Complete ==="

# Stop backend
Stop-Process -Id $p.Id -Force -ErrorAction SilentlyContinue
Stop-Process -Name "java" -Force -ErrorAction SilentlyContinue

Write-Host "Backend stopped"
