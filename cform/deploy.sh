#!/bin/bash
set -euo pipefail

# CloudFront requires its ACM certificate in us-east-1, so the whole
# stack is deployed there. Stack name must be lowercase: it is used as
# the S3 bucket name prefix.
REGION=us-east-1
STACK_NAME="${STACK_NAME:-mf-site}"
CERT_STACK_NAME="${CERT_STACK_NAME:-mf-site-certificate}"

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
CONTENT_DIR="$DIR/../target/mf_out"

# The certificate lives in its own stack (deploy-certificate.sh) so the
# site stack can be torn down or recreated without re-validating it.
CERT_ARN="$(aws cloudformation describe-stacks \
  --region "$REGION" \
  --stack-name "$CERT_STACK_NAME" \
  --query "Stacks[0].Outputs[?OutputKey=='CertificateArn'].OutputValue" \
  --output text 2>/dev/null || true)"

if [ -z "$CERT_ARN" ] || [ "$CERT_ARN" = "None" ]; then
  echo "Certificate stack '$CERT_STACK_NAME' not found in $REGION." >&2
  echo "Deploy it first: $DIR/deploy-certificate.sh" >&2
  exit 1
fi

echo "Deploying stack '$STACK_NAME' in $REGION (certificate: $CERT_ARN)..."

aws cloudformation deploy \
  --region "$REGION" \
  --stack-name "$STACK_NAME" \
  --template-file "$DIR/main.cform.yaml" \
  --parameter-overrides "CertificateArn=$CERT_ARN" \
  --tags "termination-protected=true" \
  --no-fail-on-empty-changeset

aws cloudformation update-termination-protection \
  --region "$REGION" \
  --stack-name "$STACK_NAME" \
  --enable-termination-protection > /dev/null

stack_output() {
  aws cloudformation describe-stacks \
    --region "$REGION" \
    --stack-name "$STACK_NAME" \
    --query "Stacks[0].Outputs[?OutputKey=='$1'].OutputValue" \
    --output text
}

BUCKET="$(stack_output BucketName)"
DISTRIBUTION_ID="$(stack_output DistributionId)"
DISTRIBUTION_DOMAIN="$(stack_output DistributionDomainName)"

if [ ! -d "$CONTENT_DIR" ]; then
  echo "Content directory $CONTENT_DIR not found." >&2
  echo "Generate the site first: (cd \"$DIR/..\" && mvn exec:java)" >&2
  exit 1
fi

# The bucket is private (CloudFront reads it via Origin Access Control),
# so no --acl public-read here, unlike the old previa.sh.
echo "Copying site content to s3://$BUCKET..."
aws s3 sync "$CONTENT_DIR/" "s3://$BUCKET/" --delete

echo "Invalidating CloudFront cache..."
aws cloudfront create-invalidation \
  --distribution-id "$DISTRIBUTION_ID" \
  --paths "/*" \
  --query "Invalidation.Id" \
  --output text

echo "Done. Point DNS for the four domains at: $DISTRIBUTION_DOMAIN"
