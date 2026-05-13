$projectRoot = Resolve-Path (Join-Path $PSScriptRoot '..')
$frontendRoot = Join-Path $projectRoot 'data-agent-frontend'
Set-Location $frontendRoot

npm.cmd install
npm.cmd run dev
