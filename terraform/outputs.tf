# ========================================================
# Output Values for Provisioned Resources
# ========================================================

output "instance_id" {
  description = "The ID of the provisioned DevOps EC2 instance"
  value       = aws_instance.devops_server.id
}

output "instance_public_ip" {
  description = "Public IP address of the DevOps Jenkins server"
  value       = aws_instance.devops_server.public_ip
}

output "ssh_connection_command" {
  description = "Command to connect to the server via SSH"
  value       = "ssh -i ${var.key_name}.pem ubuntu@${aws_instance.devops_server.public_ip}"
}

output "jenkins_url" {
  description = "Direct URL to access the Jenkins Web Dashboard"
  value       = "http://${aws_instance.devops_server.public_ip}:8080"
}

output "elearnhub_frontend_k8s_url" {
  description = "Access URL for E-LearnHub Frontend application via NodePort"
  value       = "http://${aws_instance.devops_server.public_ip}:30081"
}

output "elearnhub_backend_k8s_url" {
  description = "Access URL for E-LearnHub Backend health endpoint via NodePort"
  value       = "http://${aws_instance.devops_server.public_ip}:30082/actuator/health"
}
