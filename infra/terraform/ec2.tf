resource "aws_instance" "demo_host" {
  ami                         = var.ami_id
  instance_type               = var.instance_type
  subnet_id                   = aws_subnet.public.id
  vpc_security_group_ids      = [aws_security_group.demo.id]
  associate_public_ip_address = true
  key_name                    = var.key_name
  user_data = templatefile("${path.module}/cloud_user_data.sh", {
    flink_version          = var.flink_version
    flink_download_url     = var.flink_download_url
    flink_sha256           = var.flink_sha256
    application_jar_url    = var.application_jar_url
    application_jar_sha256 = var.application_jar_sha256
    runtime_bundle_url     = var.runtime_bundle_url
    runtime_profile        = var.runtime_profile
    auto_start_runtime     = var.auto_start_runtime
  })

  root_block_device {
    encrypted   = true
    volume_type = "gp3"
    volume_size = var.root_volume_size_gb
  }

  tags = {
    Name      = "taobao-cloud-e2e-host"
    Project   = "taobao-realtime"
    ManagedBy = "terraform"
    Purpose   = "temporary-cloud-e2e"
  }
}
