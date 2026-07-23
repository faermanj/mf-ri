#!/usr/bin/env bash
# After-deploy steps for main.cform.yaml: publish the generated site
# (target/mf_out) to the content bucket and invalidate CloudFront.
#
# Run from the mf-ri directory, after the site is built (mvn exec:java):
#   bash cform/main.after-deploy.sh
#
# Override the stack name with STACK=<name> if it differs from the default.
# The bucket is private (served via CloudFront OAC), so no public-read ACL
# is set - the old previa.sh style --acl public-read would be rejected by
# the bucket's public access block.

set -euo pipefail

STACK="${STACK:-marcosfaerman}"
REGION=us-east-1

BUCKET=$(aws cloudformation describe-stacks --stack-name "$STACK" --region "$REGION" --query "Stacks[0].Outputs[?OutputKey=='BucketName'].OutputValue" --output text)
DISTRIBUTION=$(aws cloudformation describe-stacks --stack-name "$STACK" --region "$REGION" --query "Stacks[0].Outputs[?OutputKey=='DistributionId'].OutputValue" --output text)

aws s3 sync ./target/mf_out/ "s3://$BUCKET/" --delete --exclude "*:Zone.Identifier"
aws cloudfront create-invalidation --distribution-id "$DISTRIBUTION" --paths "/*"
