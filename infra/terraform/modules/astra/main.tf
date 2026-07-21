resource "astra_database" "current_state" {
  name                = var.astra_database_name
  keyspace            = var.astra_keyspace
  cloud_provider      = var.astra_cloud_provider
  regions             = [var.astra_region]
  deletion_protection = false

  timeouts {
    create = "30m"
    delete = "30m"
  }
}
