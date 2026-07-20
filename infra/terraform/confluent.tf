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
