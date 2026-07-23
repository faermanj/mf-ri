# Gerador do site MF

Este é o gerador de site estático usado para construir http://marcosfaerman.jor.br | http://marcosfaerman.com.br


# Conteúdo do site

O conteúdo do site (PDFs, imagens e textos) **não fica neste repositório**:
ele é grande demais para o git. Antes de gerar o site, copie a pasta
`ConteudoSite` do Google Drive para a raiz deste repositório:

```
mf-ri/
├── ConteudoSite/   <- copiada do Google Drive (ignorada pelo git)
├── src/
├── cform/
└── pom.xml
```

A pasta está no `.gitignore`, então ela nunca será enviada ao GitHub.


# Gerando o site

Requisitos: Java (JDK 8+) e Maven.

Na raiz do repositório, execute:

```bash
mvn compile exec:java
```

Isso lê o conteúdo de `ConteudoSite/` e grava o site gerado em
`target/mf_out/`. Para visualizar localmente, abra
`target/mf_out/index.html` ou sirva o diretório, por exemplo:

```bash
python3 -m http.server -d target/mf_out
```

# Publicando (deploy)

O site é hospedado na AWS: um bucket S3 privado atrás de uma
distribuição CloudFront, definidos como templates CloudFormation em
`cform/`. É necessário ter o AWS CLI configurado com credenciais da
conta de destino.

1. **Apenas na primeira vez** — faça o deploy da stack do certificado
   ACM (ela fica em uma stack separada para que a stack do site possa
   ser recriada sem revalidar o certificado):

   ```bash
   cform/deploy-certificate.sh
   ```

   O deploy pausa na validação do certificado: crie os registros CNAME
   mostrados no console do ACM na zona DNS dos dois domínios
   (registro.br) para que ele possa continuar.

2. **A cada publicação** — gere o site (veja acima) e depois:

   ```bash
   cform/deploy.sh
   ```

   Isso atualiza a stack CloudFormation `mf-site`, sincroniza
   `target/mf_out/` com o bucket S3 e invalida o cache do CloudFront.
   No primeiro deploy, aponte o DNS dos domínios para o domínio da
   distribuição CloudFront exibido ao final.

Tudo é implantado em `us-east-1` (o CloudFront exige que o certificado
esteja lá). As duas stacks são protegidas contra remoção acidental.
