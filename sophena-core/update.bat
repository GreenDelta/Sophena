@echo off

rem updates the core library in the RCP application
set current_path=%cd%

echo install core-lib
call mvn install -DskipTests=true -q

echo copy libs
cd ../sophena
call mvn package -q

cd %current_path%
echo all done
