variable "aws_region" {
  description = "AWS region for the bounded archive demo."
  type        = string
  default     = "ap-southeast-1"
}

variable "bucket_name" {
  description = "Globally unique S3 bucket name; required to avoid accidental defaults."
  type        = string
  nullable    = false
}

variable "object_prefix" {
  description = "Prefix reserved for replay JSONL archives."
  type        = string
  default     = "taobao/raw/"
}

variable "retention_days" {
  description = "Bounded demo retention for raw archive objects."
  type        = number
  default     = 30

  validation {
    condition     = var.retention_days >= 1
    error_message = "retention_days must be at least one day."
  }
}

variable "force_destroy" {
  description = "Allow terraform destroy to remove non-empty demo buckets."
  type        = bool
  default     = false
}
