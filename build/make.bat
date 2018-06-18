@echo off

SET version=2.0.0

REM see https://stackoverflow.com/questions/19131029/how-to-get-date-in-bat-file
FOR /f "tokens=2 delims==" %%a IN ('wmic OS Get localdatetime /value') DO SET "dt=%%a"
set "YYYY=%dt:~0,4%"
set "MM=%dt:~4,2%"
set "DD=%dt:~6,2%"

SET vdate="%version%_%YYYY%_%MM%_%DD%"

if exist builds\dist rmdir builds\dist /s /q
mkdir builds\dist

IF EXIST builds\win32.win32.x86_64 (
	REM Build Windows 64-bit version
	robocopy jre\win64 builds\win32.win32.x86_64\Sophena\jre /e
	cd builds\win32.win32.x86_64
	..\..\tools\7za a ..\dist\sophena_%vdate%_win64.zip Sophena
	cd ..\..
)