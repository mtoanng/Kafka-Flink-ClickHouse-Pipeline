#!/usr/bin/env bash
# Explicitly destroy the Phase 5 Terraform demo; never runs implicitly.
set -euo pipefail

cd "$(dirname "$0")/.."

if [ "${CONFIRM_TEARDOWN:-}" != "YES" ]; then
  echo "Refusing teardown. Set CONFIRM_TEARDOWN=YES and provide Terraform variables."
  exit 2
fi
if [ -z "${TF_VAR_ami_id:-}" ] && [ "$#" -eq 0 ]; then
  echo "Set TF_VAR_ami_id or pass Terraform destroy arguments."
  exit 2
fi

terraform -chdir=infra/terraform destroy "$@"
