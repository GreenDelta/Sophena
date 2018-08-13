@echo off

cd sophena-core
call mvn install -DskipTests=true
cd ..\sophena
call mvn package
cd ..

