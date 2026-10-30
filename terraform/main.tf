# ========================================================
# Main Infrastructure as Code (IaC) - E-LearnHub Server
# Provisions: VPC, Subnet, Internet Gateway, Security Group,
# and an EC2 Instance pre-configured with Docker, Jenkins & kubectl
# ========================================================

# 1. Dedicated VPC for isolation
resource "aws_vpc" "elearnhub_vpc" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name = "elearnhub-vpc"
  }
}

# 2. Internet Gateway for internet access
resource "aws_internet_gateway" "elearnhub_igw" {
  vpc_id = aws_vpc.elearnhub_vpc.id

  tags = {
    Name = "elearnhub-igw"
  }
}

# 3. Public Subnet
resource "aws_subnet" "public_subnet" {
  vpc_id                  = aws_vpc.elearnhub_vpc.id
  cidr_block              = var.subnet_cidr
  map_public_ip_on_launch = true
  availability_zone       = "${var.aws_region}a"

  tags = {
    Name = "elearnhub-public-subnet"
  }
}

# 4. Route Table directing outbound traffic to IGW
resource "aws_route_table" "public_rt" {
  vpc_id = aws_vpc.elearnhub_vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.elearnhub_igw.id
  }

  tags = {
    Name = "elearnhub-public-rt"
  }
}

# Route table association
resource "aws_route_table_association" "public_assoc" {
  subnet_id      = aws_subnet.public_subnet.id
  route_table_id = aws_route_table.public_rt.id
}

# 5. Security Group for CI/CD and Application traffic
resource "aws_security_group" "elearnhub_sg" {
  name        = "elearnhub-security-group"
  description = "Security group for Jenkins CI/CD, Kubernetes, and E-LearnHub services"
  vpc_id      = aws_vpc.elearnhub_vpc.id

  # SSH Access
  ingress {
    description = "SSH Remote Access"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.allowed_ssh_ip]
  }

  # Jenkins Web UI
  ingress {
    description = "Jenkins CI/CD Dashboard"
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # HTTP Web Traffic
  ingress {
    description = "Standard HTTP"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Kubernetes Frontend NodePort
  ingress {
    description = "Kubernetes Frontend NodePort"
    from_port   = 30081
    to_port     = 30081
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Kubernetes Backend NodePort
  ingress {
    description = "Kubernetes Backend NodePort"
    from_port   = 30082
    to_port     = 30082
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Outbound rule (allow all traffic)
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "elearnhub-sg"
  }
}

# 6. Fetch latest Ubuntu 22.04 LTS official AMI
data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"] # Canonical

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# 7. EC2 Instance with automated DevOps bootstrap script
resource "aws_instance" "devops_server" {
  ami                         = data.aws_ami.ubuntu.id
  instance_type               = var.instance_type
  subnet_id                   = aws_subnet.public_subnet.id
  vpc_security_group_ids      = [aws_security_group.elearnhub_sg.id]
  key_name                    = var.key_name
  associate_public_ip_address = true

  root_block_device {
    volume_size           = 30
    volume_type           = "gp3"
    delete_on_termination = true
    tags = {
      Name = "elearnhub-devops-disk"
    }
  }

  # Cloud-init script: automatically installs Docker, Jenkins, Git, and kubectl
  user_data = <<-EOF
              #!/bin/bash
              set -e
              # System updates
              apt-get update -y
              apt-get upgrade -y
              apt-get install -y apt-transport-https ca-certificates curl software-properties-common git openjdk-17-jdk

              # Install Docker
              curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
              echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
              apt-get update -y
              apt-get install -y docker-ce docker-ce-cli containerd.io
              systemctl enable docker
              systemctl start docker
              usermod -aG docker ubuntu

              # Install Jenkins
              curl -fsSL https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key | tee /usr/share/keyrings/jenkins-keyring.asc > /dev/null
              echo deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc] https://pkg.jenkins.io/debian-stable binary/ | tee /etc/apt/sources.list.d/jenkins.list > /dev/null
              apt-get update -y
              apt-get install -y jenkins
              usermod -aG docker jenkins
              systemctl enable jenkins
              systemctl start jenkins

              # Install kubectl
              curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
              install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

              echo "DevOps Server initialization completed successfully!" > /var/log/user_data.log
              EOF

  tags = {
    Name = "elearnhub-devops-jenkins-server"
    Role = "CI-CD-Controller"
  }
}
