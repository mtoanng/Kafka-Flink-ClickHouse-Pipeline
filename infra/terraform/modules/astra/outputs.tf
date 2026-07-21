output "database_id" {
  value = astra_database.current_state.id
}

output "keyspace" {
  value = astra_database.current_state.keyspace
}
