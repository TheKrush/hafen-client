xcopy ".\build\hafen.jar" ".\updater\build\hafen.jar" /R /Y

cd ".\updater\build\"

run-debug.bat
