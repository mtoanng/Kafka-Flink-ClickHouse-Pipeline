resource "aws_instance" "demo_host" {
  ami                         = var.ami_id
  instance_type               = var.instance_type
  subnet_id                   = aws_subnet.public.id
  vpc_security_group_ids      = [aws_security_group.demo.id]
  associate_public_ip_address = true
  key_name                    = var.key_name
  user_data                   = file("${path.module}/cloud_user_data.sh")

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
