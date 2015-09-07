xcopy ".\dist\hafen.jar" ".\updater\dist\hafen.jar" /R /Y

cd ".\updater\dist\"

@echo off

java -Xms512m -Xmx1024m -jar hafen-minion.jar TESTING

cd ".\..\..\"
