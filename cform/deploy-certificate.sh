#!/bin/bash
set -euo pipefail

# CloudFront requires its ACM certificate in us-east-1.
REGION=us-east-1
CERT_STACK_NAME="${CERT_STACK_NAME:-mf-site-certificate}"

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo "Deploying certificate stack '$CERT_STACK_NAME' in $REGION..."
echo "NOTE: on first deploy the stack pauses on certificate validation."
echo "Create the CNAME records shown in the ACM console in the DNS zone"
echo "of both domains so the deploy can proceed."

aws cloudformation deploy \
  --region "$REGION" \
  --stack-name "$CERT_STACK_NAME" \
  --template-file "$DIR/certificate.cform.yaml" \
  --tags "termination-protected=true" \
  --no-fail-on-empty-changeset

aws cloudformation update-termination-protection \
  --region "$REGION" \
  --stack-name "$CERT_STACK_NAME" \
  --enable-termination-protection > /dev/null

CERT_ARN="$(aws cloudformation describe-stacks \
  --region "$REGION" \
  --stack-name "$CERT_STACK_NAME" \
  --query "Stacks[0].Outputs[?OutputKey=='CertificateArn'].OutputValue" \
  --output text)"

echo "Done. Certificate ARN: $CERT_ARN"
echo "Now deploy the site stack: $DIR/deploy.sh"
