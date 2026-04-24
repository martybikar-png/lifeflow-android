# RELEASE SECURITY TOOLKIT

## Main commands

### 1) Create/refresh release security archive
powershell -ExecutionPolicy Bypass -File "C:\Users\marty\AndroidStudioProjects\LifeFlow\tools\Invoke-ReleaseSecurityToolkit.ps1" -Mode archive

### 2) Show latest archive status
powershell -ExecutionPolicy Bypass -File "C:\Users\marty\AndroidStudioProjects\LifeFlow\tools\Invoke-ReleaseSecurityToolkit.ps1" -Mode status

### 3) Verify latest archive integrity
powershell -ExecutionPolicy Bypass -File "C:\Users\marty\AndroidStudioProjects\LifeFlow\tools\Invoke-ReleaseSecurityToolkit.ps1" -Mode verify-latest

### 4) Resolve obfuscated crash using latest archive
powershell -ExecutionPolicy Bypass -File "C:\Users\marty\AndroidStudioProjects\LifeFlow\tools\Invoke-ReleaseSecurityToolkit.ps1" -Mode resolve -StackTraceFile "C:\REAL\PATH\crash.txt"

## Expected archive structure
artifacts/
  release-r8/
    LATEST_BUNDLE.txt
    YYYY-MM-DD_HH-mm-ss/
      mapping.txt
      usage.txt
      seeds.txt
      configuration.txt
      manifest.json
    YYYY-MM-DD_HH-mm-ss.zip
  release-crashes/
    *.txt
    *.retraced.txt

## Notes
- mapping.txt is the key retrace file
- usage.txt helps inspect what R8 removed
- seeds.txt helps inspect what was explicitly kept
- configuration.txt preserves the effective configuration snapshot
