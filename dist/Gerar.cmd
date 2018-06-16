cd C:\Users\Administrator\Google Drive\EXECUCAO_ITAU_CULTURAL\mf-ri
mvn -e -Pwinbuild clean compile exec:java >> c:\GerarSite.%date:~-4%%date:~4,2%%date:~7,2%.%time::=%.log 2>&1
