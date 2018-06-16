echo "GerarEPublicar 201602041253"
echo "== Changing Directory =="
cd C:\Users\Administrator\Google Drive\EXECUCAO_ITAU_CULTURAL\mf-ri
echo "== Compiling and executing =="
mvn -e -Pwinbuild clean compile exec:java >> c:\GerarSite.%date:~-4%%date:~4,2%%date:~7,2%.%time::=%.log 2>&1
echo "== Publishing =="
aws s3 sync C:\Users\Administrator\SiteMF s3://laura.marcosfaerman.jor.br --exclude="*.pdf" >> c:\Publicar.%date:~-4%%date:~4,2%%date:~7,2%.%time::=%.log 2>&1
echo "== Done =="




