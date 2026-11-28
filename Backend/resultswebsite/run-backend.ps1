$ErrorActionPreference = 'Stop'

function ConvertFrom-SecureInput {
    param([Security.SecureString]$Value)

    $pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($Value)
    try {
        return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)
    }
    finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
    }
}

function Test-AdminPassword {
    param([string]$Value)

    return $Value.Length -ge 12 `
        -and $Value.Length -le 72 `
        -and [Text.Encoding]::UTF8.GetByteCount($Value) -le 72 `
        -and $Value -cmatch '[A-Z]' `
        -and $Value -cmatch '[a-z]' `
        -and $Value -cmatch '\d' `
        -and $Value -cmatch '[^A-Za-z0-9\s]'
}

function Read-AdminPassword {
    param(
        [string]$OwnerName,
        [string[]]$PreviouslyChosen
    )

    while ($true) {
        $secureValue = Read-Host "Enter a private password for $OwnerName" -AsSecureString
        $secureConfirmation = Read-Host "Confirm the password for $OwnerName" -AsSecureString
        $password = ConvertFrom-SecureInput $secureValue
        $confirmation = ConvertFrom-SecureInput $secureConfirmation

        if ($password -cne $confirmation) {
            Write-Host 'The passwords did not match. Try again.' -ForegroundColor Yellow
            continue
        }
        if (-not (Test-AdminPassword $password)) {
            Write-Host 'Use 12 to 72 characters with uppercase, lowercase, a number, and a symbol (maximum 72 UTF-8 bytes).' -ForegroundColor Yellow
            continue
        }
        if ($PreviouslyChosen -ccontains $password) {
            Write-Host 'Each owner must use a different password.' -ForegroundColor Yellow
            continue
        }

        return $password
    }
}

if (-not $env:JAVA_HOME) {
    throw 'Set JAVA_HOME to your JDK 21 folder before running this script.'
}
if (-not (Test-Path (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
    throw "JAVA_HOME does not point to a JDK: $env:JAVA_HOME"
}

Write-Host 'Choose three unique owner passwords. They are entered privately and are not saved in this file.'
Write-Host 'These values create missing owner accounts only; they do not reset existing accounts.'

$owners = @(
    @{ Name = 'Nitin'; Variable = 'OWNER_NITIN_PASSWORD' },
    @{ Name = 'Dayanand'; Variable = 'OWNER_DAYANAND_PASSWORD' },
    @{ Name = 'Ashish'; Variable = 'OWNER_ASHISH_PASSWORD' }
)
$previousValues = @{}
foreach ($owner in $owners) {
    $previousValues[$owner.Variable] = [Environment]::GetEnvironmentVariable($owner.Variable, 'Process')
}
$previousValues['DB_URL'] = [Environment]::GetEnvironmentVariable('DB_URL', 'Process')
$previousValues['DB_USERNAME'] = [Environment]::GetEnvironmentVariable('DB_USERNAME', 'Process')
$previousValues['DB_PASSWORD'] = [Environment]::GetEnvironmentVariable('DB_PASSWORD', 'Process')

try {
    $chosenPasswords = @()
    foreach ($owner in $owners) {
        $password = Read-AdminPassword -OwnerName $owner.Name -PreviouslyChosen $chosenPasswords
        [Environment]::SetEnvironmentVariable($owner.Variable, $password, 'Process')
        $chosenPasswords += $password
    }

    if (-not $env:DB_URL) {
        $env:DB_URL = 'jdbc:postgresql://localhost:5432/dpboss'
    }
    if (-not $env:DB_USERNAME) {
        $env:DB_USERNAME = 'postgres'
    }
    if (-not $env:DB_PASSWORD) {
        $secureDatabasePassword = Read-Host 'Enter the PostgreSQL password (leave blank only if the database has no password)' -AsSecureString
        $env:DB_PASSWORD = ConvertFrom-SecureInput $secureDatabasePassword
    }

    Push-Location $PSScriptRoot
    try {
        & (Join-Path $PSScriptRoot 'mvnw.cmd') spring-boot:run
        if ($LASTEXITCODE -ne 0) {
            throw "Spring Boot stopped with exit code $LASTEXITCODE."
        }
    }
    finally {
        Pop-Location
    }
}
finally {
    foreach ($variableName in $previousValues.Keys) {
        [Environment]::SetEnvironmentVariable($variableName, $previousValues[$variableName], 'Process')
    }
}
