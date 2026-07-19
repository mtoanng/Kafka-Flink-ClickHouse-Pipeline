output "raw_archive_bucket" {
  description = "Bucket receiving replay JSONL archives."
  value       = aws_s3_bucket.raw_archive.bucket
}

output "raw_archive_prefix" {
  description = "Object prefix used by the archive script."
  value       = var.object_prefix
}
