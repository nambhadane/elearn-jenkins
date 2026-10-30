# ========================================================
# Terraform Provider Configuration for E-LearnHub IaC
# ========================================================
terraform {
  required_version = ">= 1.0.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "E-LearnHub"
      Environment = "DevOps-Assignment"
      ManagedBy   = "Terraform"
      Owner       = "Namrata-Bhadane"
    }
  }
}
