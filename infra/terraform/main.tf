# Resource definitions are split by concern:
# - network.tf: VPC, subnet, route, internet gateway, security group, Elastic IP
# - ec2.tf: temporary EC2 host
# - confluent.tf: optional Confluent Cloud Basic cluster and contracts

module "astra" {
  count  = var.enable_astra_resources ? 1 : 0
  source = "./modules/astra"

  astra_database_name  = var.astra_database_name
  astra_keyspace       = var.astra_keyspace
  astra_cloud_provider = var.astra_cloud_provider
  astra_region         = var.astra_region
}
