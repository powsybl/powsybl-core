@echo off
REM Copyright (c) 2018, RTE (http://www.rte-france.com)
REM This Source Code Form is subject to the terms of the Mozilla Public
REM License, v. 2.0. If a copy of the MPL was not distributed with this
REM file, You can obtain one at http://mozilla.org/MPL/2.0/.

if "%JAVA_HOME%" == "" (
  where /Q java.exe
  if %errorlevel% == 0 (
    set JAVA_BIN=java.exe
  ) else (
    echo "Unable to find java" >&2
    exit -1
  )
) else (
  set JAVA_BIN=%JAVA_HOME%\bin\java.exe
)

setlocal EnableDelayedExpansion

set installBinDir=%~dp0
set installDir=%installBinDir%..

for /f "delims=" %%x in (%installDir%\etc\itools.conf) do (
  set var=%%x
  if not "!var:~0,1!" == "#" ( set "!var!" )
)

set args=
set parallel=false
:continue
if "%1"=="" ( goto done ) else (
  if "%1" == "--config-name" (
    set itools_config_name=%2
    shift
  ) else (
    if "%1" == "--parallel" (
      set parallel=true
      shift
      echo WARNING : '--parallel' option not currently supported on Windows
    ) else (
        set args=%args% %1 %2
        shift
      )
  )
  shift
  goto continue
)
:done

set options=
if not "%itools_cache_dir%" == "" ( set options=-Ditools.cache.dir="%itools_cache_dir%" )
if "%itools_config_dir%" == "" ( set itools_config_dir=%HOMEDRIVE%%HOMEPATH%\.itools)
set options=%options% -Ditools.config.dir="%itools_config_dir%"
if not "%itools_config_name%" == "" ( set options=%options% -Ditools.config.name=%itools_config_name%)

set options=%options% -Dlogback.configurationFile="
if exist %itools_config_dir%\logback-itools.xml (
  set options=%options%%itools_config_dir%
) else (
  set options=%options%%installDir%\etc
)
set options=%options%\logback-itools.xml"

if "%java_xmx%"=="" ( set java_xmx=8G )

"%JAVA_BIN%" -Xmx%java_xmx% -cp %installDir%\share\java\* %options% com.powsybl.tools.Main %args%

endlocal
