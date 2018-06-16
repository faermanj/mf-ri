#/bin/sh

BUCKET=s3://previa.marcosfaerman.jor.br
mvn exec:java
aws s3 sync ./target/mf_out/ $BUCKET --acl public-read --delete