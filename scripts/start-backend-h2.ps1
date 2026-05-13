$projectRoot = Resolve-Path (Join-Path $PSScriptRoot '..')
Set-Location $projectRoot

mvn -gs .mvn\settings.xml -pl data-agent-management spring-boot:run
