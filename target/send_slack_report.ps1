param(
    [string]$WebhookUrl = $env:SLACK_WEBHOOK_URL,
    [string]$PayloadFile = ""
)

# Load WebhookUrl from local config if not provided
if ([string]::IsNullOrWhiteSpace($WebhookUrl)) {
    $configFile = Join-Path $PSScriptRoot "slack_config.json"
    if (Test-Path $configFile) {
        try {
            $config = Get-Content $configFile -Raw | ConvertFrom-Json
            $WebhookUrl = $config.WebhookUrl
        } catch {}
    }
}

if ([string]::IsNullOrWhiteSpace($WebhookUrl)) {
    Write-Warning "No Slack Webhook URL provided. Set `$env:SLACK_WEBHOOK_URL or pass -WebhookUrl '<url>'."
    exit 1
}

if ([string]::IsNullOrWhiteSpace($PayloadFile)) {
    $errorFile = Join-Path $PSScriptRoot "slack_error_payload.json"
    $successFile = Join-Path $PSScriptRoot "slack_payload.json"
    if (Test-Path $errorFile) {
        $PayloadFile = $errorFile
    } elseif (Test-Path $successFile) {
        $PayloadFile = $successFile
    } else {
        $PayloadFile = Join-Path $PSScriptRoot "slack_error_payload.json"
    }
}

if (!(Test-Path $PayloadFile)) {
    Write-Error "Slack payload file not found at: $PayloadFile"
    exit 1
}

Write-Host "Reading Slack payload from: $PayloadFile"
$jsonBody = Get-Content -Path $PayloadFile -Raw -Encoding UTF8

Write-Host "Sending report to Slack webhook..."
try {
    $response = Invoke-RestMethod -Uri $WebhookUrl -Method Post -Body $jsonBody -ContentType "application/json; charset=utf-8"
    Write-Host "Slack Response: $response"
    Write-Host "Report successfully sent to Slack!"
} catch {
    Write-Error "Failed to send message to Slack: $_"
}
