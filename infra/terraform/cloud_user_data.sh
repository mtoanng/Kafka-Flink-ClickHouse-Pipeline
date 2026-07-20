#!/usr/bin/env bash
set -euo pipefail

# Amazon Linux 2023 bootstrap. No application credentials or endpoints belong here.
dnf install -y docker git python3
systemctl enable --now docker
usermod -aG docker ec2-user
