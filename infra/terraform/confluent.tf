resource "confluent_environment" "cloud" {
  count        = var.enable_confluent_resources ? 1 : 0
  display_name = var.confluent_environment_name
}

resource "confluent_kafka_cluster" "basic" {
  count        = var.enable_confluent_resources ? 1 : 0
  display_name = var.confluent_cluster_name
  availability = "SINGLE_ZONE"
  cloud        = "AWS"
  region       = var.confluent_cloud_region

  basic {}

  environment {
    id = confluent_environment.cloud[0].id
  }
}

resource "confluent_service_account" "replay_flink" {
  count        = var.enable_confluent_resources ? 1 : 0
  display_name = "taobao-replay-flink"
  description  = "Bounded E2E producer and Flink consumer service account"
}

resource "confluent_api_key" "kafka" {
  count        = var.enable_confluent_resources ? 1 : 0
  display_name = "taobao-replay-flink-kafka"

  owner {
    id          = confluent_service_account.replay_flink[0].id
    api_version = "sa/v1"
    kind        = "ServiceAccount"
  }

  managed_resource {
    id          = confluent_kafka_cluster.basic[0].id
    api_version = "cmk/v2"
    kind        = "Kafka"

    environment {
      id = confluent_environment.cloud[0].id
    }
  }
}

resource "confluent_kafka_topic" "events" {
  count            = var.enable_confluent_resources ? 1 : 0
  topic_name       = "user-behavior-events"
  partitions_count = 3
  rest_endpoint    = confluent_kafka_cluster.basic[0].rest_endpoint

  kafka_cluster {
    id = confluent_kafka_cluster.basic[0].id
  }

  credentials {
    key    = confluent_api_key.kafka[0].id
    secret = confluent_api_key.kafka[0].secret
  }
}

resource "confluent_kafka_topic" "rules" {
  count            = var.enable_confluent_resources ? 1 : 0
  topic_name       = "behavior-rules"
  partitions_count = 1
  config = {
    "cleanup.policy" = "compact"
  }
  rest_endpoint = confluent_kafka_cluster.basic[0].rest_endpoint

  kafka_cluster {
    id = confluent_kafka_cluster.basic[0].id
  }

  credentials {
    key    = confluent_api_key.kafka[0].id
    secret = confluent_api_key.kafka[0].secret
  }
}

locals {
  connect_topics = {
    configs = {
      name   = "taobao-connect-configs"
      policy = "compact"
    }
    offsets = {
      name   = "taobao-connect-offsets"
      policy = "compact"
    }
    status = {
      name   = "taobao-connect-status"
      policy = "compact"
    }
  }
}

resource "confluent_kafka_topic" "connect_internal" {
  for_each         = var.enable_confluent_resources ? local.connect_topics : {}
  topic_name       = each.value.name
  partitions_count = each.key == "configs" ? 1 : 3
  config = {
    "cleanup.policy" = each.value.policy
  }
  rest_endpoint = confluent_kafka_cluster.basic[0].rest_endpoint

  kafka_cluster {
    id = confluent_kafka_cluster.basic[0].id
  }

  credentials {
    key    = confluent_api_key.kafka[0].id
    secret = confluent_api_key.kafka[0].secret
  }
}

resource "confluent_kafka_acl" "service_account_cluster_describe" {
  count = var.enable_confluent_resources ? 1 : 0
  kafka_cluster {
    id = confluent_kafka_cluster.basic[0].id
  }
  resource_type = "CLUSTER"
  resource_name = "kafka-cluster"
  pattern_type  = "LITERAL"
  principal     = "User:${confluent_service_account.replay_flink[0].id}"
  host          = "*"
  operation     = "DESCRIBE"
  permission    = "ALLOW"
  rest_endpoint = confluent_kafka_cluster.basic[0].rest_endpoint
  credentials {
    key    = confluent_api_key.kafka[0].id
    secret = confluent_api_key.kafka[0].secret
  }
}

# The bounded runtime uses one injected service account for replay, Flink and
# Connect. These ACLs intentionally spell out the operations used by each
# component instead of relying on a cluster-wide wildcard.
resource "confluent_kafka_acl" "events_write" {
  count = var.enable_confluent_resources ? 1 : 0
  kafka_cluster { id = confluent_kafka_cluster.basic[0].id }
  resource_type = "TOPIC"
  resource_name = confluent_kafka_topic.events[0].topic_name
  pattern_type  = "LITERAL"
  principal     = "User:${confluent_service_account.replay_flink[0].id}"
  host          = "*"
  operation     = "WRITE"
  permission    = "ALLOW"
  rest_endpoint = confluent_kafka_cluster.basic[0].rest_endpoint
  credentials {
    key    = confluent_api_key.kafka[0].id
    secret = confluent_api_key.kafka[0].secret
  }
}

resource "confluent_kafka_acl" "events_read" {
  count = var.enable_confluent_resources ? 1 : 0
  kafka_cluster { id = confluent_kafka_cluster.basic[0].id }
  resource_type = "TOPIC"
  resource_name = confluent_kafka_topic.events[0].topic_name
  pattern_type  = "LITERAL"
  principal     = "User:${confluent_service_account.replay_flink[0].id}"
  host          = "*"
  operation     = "READ"
  permission    = "ALLOW"
  rest_endpoint = confluent_kafka_cluster.basic[0].rest_endpoint
  credentials {
    key    = confluent_api_key.kafka[0].id
    secret = confluent_api_key.kafka[0].secret
  }
}

resource "confluent_kafka_acl" "events_describe" {
  count = var.enable_confluent_resources ? 1 : 0
  kafka_cluster { id = confluent_kafka_cluster.basic[0].id }
  resource_type = "TOPIC"
  resource_name = confluent_kafka_topic.events[0].topic_name
  pattern_type  = "LITERAL"
  principal     = "User:${confluent_service_account.replay_flink[0].id}"
  host          = "*"
  operation     = "DESCRIBE"
  permission    = "ALLOW"
  rest_endpoint = confluent_kafka_cluster.basic[0].rest_endpoint
  credentials {
    key    = confluent_api_key.kafka[0].id
    secret = confluent_api_key.kafka[0].secret
  }
}

resource "confluent_kafka_acl" "events_group_read" {
  count = var.enable_confluent_resources ? 1 : 0
  kafka_cluster { id = confluent_kafka_cluster.basic[0].id }
  resource_type = "GROUP"
  resource_name = "taobao-flink"
  pattern_type  = "LITERAL"
  principal     = "User:${confluent_service_account.replay_flink[0].id}"
  host          = "*"
  operation     = "READ"
  permission    = "ALLOW"
  rest_endpoint = confluent_kafka_cluster.basic[0].rest_endpoint
  credentials {
    key    = confluent_api_key.kafka[0].id
    secret = confluent_api_key.kafka[0].secret
  }
}

resource "confluent_kafka_acl" "rules_read" {
  count = var.enable_confluent_resources ? 1 : 0
  kafka_cluster { id = confluent_kafka_cluster.basic[0].id }
  resource_type = "TOPIC"
  resource_name = confluent_kafka_topic.rules[0].topic_name
  pattern_type  = "LITERAL"
  principal     = "User:${confluent_service_account.replay_flink[0].id}"
  host          = "*"
  operation     = "READ"
  permission    = "ALLOW"
  rest_endpoint = confluent_kafka_cluster.basic[0].rest_endpoint
  credentials {
    key    = confluent_api_key.kafka[0].id
    secret = confluent_api_key.kafka[0].secret
  }
}

resource "confluent_kafka_acl" "rules_write" {
  count = var.enable_confluent_resources ? 1 : 0
  kafka_cluster { id = confluent_kafka_cluster.basic[0].id }
  resource_type = "TOPIC"
  resource_name = confluent_kafka_topic.rules[0].topic_name
  pattern_type  = "LITERAL"
  principal     = "User:${confluent_service_account.replay_flink[0].id}"
  host          = "*"
  operation     = "WRITE"
  permission    = "ALLOW"
  rest_endpoint = confluent_kafka_cluster.basic[0].rest_endpoint
  credentials {
    key    = confluent_api_key.kafka[0].id
    secret = confluent_api_key.kafka[0].secret
  }
}

resource "confluent_kafka_acl" "rules_describe" {
  count = var.enable_confluent_resources ? 1 : 0
  kafka_cluster { id = confluent_kafka_cluster.basic[0].id }
  resource_type = "TOPIC"
  resource_name = confluent_kafka_topic.rules[0].topic_name
  pattern_type  = "LITERAL"
  principal     = "User:${confluent_service_account.replay_flink[0].id}"
  host          = "*"
  operation     = "DESCRIBE"
  permission    = "ALLOW"
  rest_endpoint = confluent_kafka_cluster.basic[0].rest_endpoint
  credentials {
    key    = confluent_api_key.kafka[0].id
    secret = confluent_api_key.kafka[0].secret
  }
}

resource "confluent_kafka_acl" "rules_group_read" {
  count = var.enable_confluent_resources ? 1 : 0
  kafka_cluster { id = confluent_kafka_cluster.basic[0].id }
  resource_type = "GROUP"
  resource_name = "taobao-rule-broadcast"
  pattern_type  = "LITERAL"
  principal     = "User:${confluent_service_account.replay_flink[0].id}"
  host          = "*"
  operation     = "READ"
  permission    = "ALLOW"
  rest_endpoint = confluent_kafka_cluster.basic[0].rest_endpoint
  credentials {
    key    = confluent_api_key.kafka[0].id
    secret = confluent_api_key.kafka[0].secret
  }
}

resource "confluent_kafka_acl" "connect_internal_read" {
  for_each = var.enable_confluent_resources ? local.connect_topics : {}
  kafka_cluster { id = confluent_kafka_cluster.basic[0].id }
  resource_type = "TOPIC"
  resource_name = each.value.name
  pattern_type  = "LITERAL"
  principal     = "User:${confluent_service_account.replay_flink[0].id}"
  host          = "*"
  operation     = "READ"
  permission    = "ALLOW"
  rest_endpoint = confluent_kafka_cluster.basic[0].rest_endpoint
  credentials {
    key    = confluent_api_key.kafka[0].id
    secret = confluent_api_key.kafka[0].secret
  }
}

resource "confluent_kafka_acl" "connect_internal_write" {
  for_each = var.enable_confluent_resources ? local.connect_topics : {}
  kafka_cluster { id = confluent_kafka_cluster.basic[0].id }
  resource_type = "TOPIC"
  resource_name = each.value.name
  pattern_type  = "LITERAL"
  principal     = "User:${confluent_service_account.replay_flink[0].id}"
  host          = "*"
  operation     = "WRITE"
  permission    = "ALLOW"
  rest_endpoint = confluent_kafka_cluster.basic[0].rest_endpoint
  credentials {
    key    = confluent_api_key.kafka[0].id
    secret = confluent_api_key.kafka[0].secret
  }
}

resource "confluent_kafka_acl" "connect_internal_describe" {
  for_each = var.enable_confluent_resources ? local.connect_topics : {}
  kafka_cluster { id = confluent_kafka_cluster.basic[0].id }
  resource_type = "TOPIC"
  resource_name = each.value.name
  pattern_type  = "LITERAL"
  principal     = "User:${confluent_service_account.replay_flink[0].id}"
  host          = "*"
  operation     = "DESCRIBE"
  permission    = "ALLOW"
  rest_endpoint = confluent_kafka_cluster.basic[0].rest_endpoint
  credentials {
    key    = confluent_api_key.kafka[0].id
    secret = confluent_api_key.kafka[0].secret
  }
}

resource "confluent_kafka_acl" "connect_group_read" {
  count = var.enable_confluent_resources ? 1 : 0
  kafka_cluster { id = confluent_kafka_cluster.basic[0].id }
  resource_type = "GROUP"
  resource_name = "taobao-debezium"
  pattern_type  = "LITERAL"
  principal     = "User:${confluent_service_account.replay_flink[0].id}"
  host          = "*"
  operation     = "READ"
  permission    = "ALLOW"
  rest_endpoint = confluent_kafka_cluster.basic[0].rest_endpoint
  credentials {
    key    = confluent_api_key.kafka[0].id
    secret = confluent_api_key.kafka[0].secret
  }
}

data "confluent_schema_registry_cluster" "runtime" {
  count = var.enable_confluent_resources && var.confluent_schema_registry_cluster_id != null ? 1 : 0
  id    = var.confluent_schema_registry_cluster_id

  environment {
    id = confluent_environment.cloud[0].id
  }
}

resource "confluent_role_binding" "schema_subject_read" {
  count       = length(data.confluent_schema_registry_cluster.runtime) == 1 ? 1 : 0
  principal   = "User:${confluent_service_account.replay_flink[0].id}"
  role_name   = "DeveloperRead"
  crn_pattern = "${data.confluent_schema_registry_cluster.runtime[0].resource_name}/subject=*"
}

resource "confluent_schema" "events" {
  count         = var.enable_confluent_resources ? 1 : 0
  subject_name  = "user-behavior-events-value"
  format        = "AVRO"
  schema        = file("${path.module}/../../schemas/user-behavior-event.avsc")
  rest_endpoint = var.confluent_schema_registry_rest_endpoint

  schema_registry_cluster {
    id = var.confluent_schema_registry_cluster_id
  }

  credentials {
    key    = var.confluent_schema_registry_api_key
    secret = var.confluent_schema_registry_api_secret
  }
}

resource "confluent_schema" "rules" {
  count         = var.enable_confluent_resources ? 1 : 0
  subject_name  = "behavior-rules-value"
  format        = "AVRO"
  schema        = file("${path.module}/../../schemas/behavior-rule.avsc")
  rest_endpoint = var.confluent_schema_registry_rest_endpoint

  schema_registry_cluster {
    id = var.confluent_schema_registry_cluster_id
  }

  credentials {
    key    = var.confluent_schema_registry_api_key
    secret = var.confluent_schema_registry_api_secret
  }
}
