# ========================================================
# Input Variables for E-LearnHub Infrastructure
# ========================================================

variable "aws_region" {
  description = "AWS deployment region"
  type        = string
  default     = "ap-south-1" # Mumbai region (closest for India)
}

variable "instance_type" {
  description = "EC2 instance sizing suitable for Docker, Jenkins, and Kubernetes"
  type        = string
  default     = "t3.medium" # 2 vCPU, 4GB RAM recommended for Jenkins + K8s
}

variable "key_name" {
  description = "Name of existing AWS EC2 Key Pair for SSH access"
  type        = string
  default     = "elearnhub-key"
}

variable "vpc_cidr" {
  description = "CIDR block for the dedicated E-LearnHub VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "subnet_cidr" {
  description = "CIDR block for the public subnet"
  type        = string
  default     = "10.0.1.0/24"
}

variable "allowed_ssh_ip" {
  description = "IP CIDR allowed to connect via SSH (set to your IP for production)"
  type        = string
  default     = "0.0.0.0/0"
}
