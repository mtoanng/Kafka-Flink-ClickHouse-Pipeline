resource "aws_s3_bucket" "raw_archive" {
  bucket        = var.bucket_name
  force_destroy = var.force_destroy

  tags = {
    Project   = "taobao-realtime"
    ManagedBy = "terraform"
    Purpose   = "bounded-raw-archive"
  }
}

resource "aws_s3_bucket_versioning" "raw_archive" {
  bucket = aws_s3_bucket.raw_archive.id

  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "raw_archive" {
  bucket = aws_s3_bucket.raw_archive.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_lifecycle_configuration" "raw_archive" {
  bucket = aws_s3_bucket.raw_archive.id

  rule {
    id     = "expire-bounded-raw-archive"
    status = "Enabled"

    filter {
      prefix = var.object_prefix
    }

    expiration {
      days = var.retention_days
    }
  }
}
