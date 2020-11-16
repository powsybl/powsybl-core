@echo off
REM Copyright (c) 2018, RTE (http://www.rte-france.com)
REM This Source Code Form is subject to the terms of the Mozilla Public
REM License, v. 2.0. If a copy of the MPL was not distributed with this
REM file, You can obtain one at http://mozilla.org/MPL/2.0/.

setlocal EnableDelayedExpansion

if "%JAVA_HOME%" == "" (
  where /Q java.exe
  if %errorlevel% == 0 (
    set JAVA_BIN=java.exe
  ) else (
    echo "Unable to find java" >&2
    exit /B -1
  )
) else (
  set JAVA_BIN=%JAVA_HOME%\bin\java.exe
)

set installBinDir=%~dp0
set installDir=%installBinDir%..

for /f "usebackq delims=" %%x in ("%installDir%\etc\itools.conf") do (
  set var=%%x
  if not "!var:~0,1!" == "#" ( set "!var!" )
)

set args=
set parallel=false
:continue
if "%~1"=="" ( goto done ) else (
  if "%~1" == "--config-name" (
    set powsybl_config_name=%2
    shift
  ) else (
    set args=%args% %1 %2
    shift
  )
  shift
  goto continue
)
:done

set options=
if not defined powsybl_config_dirs ( set powsybl_config_dirs=%HOMEDRIVE%%HOMEPATH%\.itools;%installDir%\etc)
set options=%options% -Dpowsybl.config.dirs="%powsybl_config_dirs%"
if not "%powsybl_config_name%" == "" ( set options=%options% -Dpowsybl.config.name=%powsybl_config_name%)

set options=%options% -Dlogback.configurationFile="
set logFile=%installDir%\etc\logback-itools.xml
for %%G in ("%powsybl_config_dirs:;=";"%") do (
    if exist "%%~G\logback-itools.xml" (
        set logFile=%%~G\logback-itools.xml
        goto doneLogback
    )
)
:doneLogback
set options=%options%%logFile%"

if "%java_xmx%"=="" ( set java_xmx=8G )

"%JAVA_BIN%" -Xmx%java_xmx% -cp "%installDir%\share\java\*" %options% com.powsybl.tools.Main %args%

endlocal
