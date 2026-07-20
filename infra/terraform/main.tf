# Resource definitions are split by concern:
# - network.tf: VPC, subnet, route, internet gateway, security group, Elastic IP
# - ec2.tf: temporary EC2 host
# - confluent.tf: optional Confluent Cloud Basic cluster and contracts
