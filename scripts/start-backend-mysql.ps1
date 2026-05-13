$projectRoot = Resolve-Path (Join-Path $PSScriptRoot '..')
Set-Location $projectRoot

mvn -gs .mvn\settings.xml -pl data-agent-management spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=mysql"
