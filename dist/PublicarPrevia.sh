#/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
aws s3 sync --delete --region=sa-east-1 "$HOME/SiteMF/" s3://marcosprevia.marcosfaerman.jor.br
read