resource "aws_vpc" "demo" {
  cidr_block           = var.vpc_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Project   = "taobao-realtime"
    ManagedBy = "terraform"
    Purpose   = "temporary-cloud-e2e"
  }
}

resource "aws_subnet" "public" {
  vpc_id                  = aws_vpc.demo.id
  cidr_block              = var.public_subnet_cidr
  availability_zone       = var.availability_zone
  map_public_ip_on_launch = true

  tags = {
    Name    = "taobao-cloud-e2e-public"
    Project = "taobao-realtime"
  }
}

resource "aws_internet_gateway" "demo" {
  vpc_id = aws_vpc.demo.id

  tags = {
    Name    = "taobao-cloud-e2e-igw"
    Project = "taobao-realtime"
  }
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.demo.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.demo.id
  }

  tags = {
    Name    = "taobao-cloud-e2e-public-routes"
    Project = "taobao-realtime"
  }
}

resource "aws_route_table_association" "public" {
  subnet_id      = aws_subnet.public.id
  route_table_id = aws_route_table.public.id
}

resource "aws_security_group" "demo" {
  name        = "taobao-cloud-e2e"
  description = "SSH-only administration; demo services use outbound connections."
  vpc_id      = aws_vpc.demo.id

  ingress {
    description = "Restricted SSH administration"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.ssh_cidr]
  }

  egress {
    description = "Outbound access for package installs and external managed services"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name    = "taobao-cloud-e2e"
    Project = "taobao-realtime"
  }
}

resource "aws_eip" "demo" {
  domain = "vpc"

  tags = {
    Name    = "taobao-cloud-e2e-eip"
    Project = "taobao-realtime"
  }
}

resource "aws_eip_association" "demo" {
  instance_id   = aws_instance.demo_host.id
  allocation_id = aws_eip.demo.id
}
