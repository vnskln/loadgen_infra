# Load Generator test lab

What is it?
- two Infrastructure as Code virtual machines created with Vagrant and Virtual Box
- both VMs has docker installed and all applications run as containers
- Jenkins is used to build and run Load Generator application
- cAdvisor scrapes data from containers
- Prometheus Node Exporter screapes data from virtual machine
- Prometheus gathers metrics from cAdvisor and PNE 
- dashboards are created in Grafana- 

How to start?
- install Vagrant, Docker Compose plugin for Vagrant and Virtual Box
- clone repo and in location of Vagrantfile use command "vagrant up"
- tested with Windows 10
