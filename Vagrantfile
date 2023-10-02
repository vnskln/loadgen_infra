Vagrant.configure("2") do |config| 
  config.vm.boot_timeout = 600
  config.vm.define "master" do |master|
    master.vm.box = "ubuntu/focal64"
	master.vm.hostname = "master"
	master.vm.synced_folder "master/", "/home/vagrant/eng/"
	master.vm.network "private_network", ip: "192.168.50.100"
	master.vm.network "forwarded_port", guest: 8000, host: 8000
	master.vm.network "forwarded_port", guest: 8004, host: 8004
	master.vm.network "forwarded_port", guest: 8005, host: 8005
	master.vm.provider "virtualbox" do |vb|
	  vb.gui = true
	  vb.name = "master"
	  vb.cpus = 4
	  vb.memory = "4096"
      vb.customize ["modifyvm", :id, "--uart1", "0x3f8", "4"]
	  vb.customize ["modifyvm", :id, "--uartmode1", "file", File::NULL]
	  vb.customize ["modifyvm", :id, "--audio", "none"]
	  vb.customize ["modifyvm", :id, "--vram", "12"]
          vb.customize ["modifyvm", :id, "--paravirtprovider", "minimal", "--cpus", "4"]
	end  
	# Docker
    master.vm.provision :docker    
	# Docker Compose
    master.vm.provision :docker_compose	
	# Install jq, start Jenkins, Prometheus
    master.vm.provision "shell", inline: <<-SHELL
	  sudo apt-get update
	  sudo apt-get -y install jq
	  cd /home/vagrant/eng
	  docker build -t myjenkins .
	  docker-compose -f master_compose.yaml up -d
	  sleep 240
	  touch cookie
	  touch crumb
	  curl -s --cookie-jar cookie -X GET http://localhost:8000/crumbIssuer/api/json
	  curl -s --cookie-jar cookie -X GET http://localhost:8000/crumbIssuer/api/json | jq -r '.crumb' > crumb
	  curl -v -X POST -H 'Jenkins-Crumb:'$(cat crumb)'' --cookie cookie http://localhost:8000/restart
    SHELL
  end
  config.vm.define "node" do |node|
    node.vm.box = "ubuntu/focal64"
    node.vm.hostname = "node"
	node.vm.synced_folder "node/", "/home/vagrant/eng/"
	node.vm.network "private_network", ip: "192.168.50.200"
	node.vm.network "forwarded_port", guest: 8001, host: 8001
	node.vm.network "forwarded_port", guest: 8002, host: 8002
	node.vm.network "forwarded_port", guest: 8003, host: 8003
	node.vm.provider "virtualbox" do |vb|
	  vb.gui = true
	  vb.name = "node"
	  vb.cpus = 4
	  vb.memory = "4096"
	  vb.customize ["modifyvm", :id, "--uart1", "0x3f8", "4"]
	  vb.customize ["modifyvm", :id, "--uartmode1", "file", File::NULL]
	  vb.customize ["modifyvm", :id, "--audio", "none"]
	  vb.customize ["modifyvm", :id, "--vram", "24"]
          vb.customize ["modifyvm", :id, "--paravirtprovider", "minimal", "--cpus", "4"]
	end	
	# Docker
    node.vm.provision :docker
	node.vm.provision "shell", inline: <<-SHELL
	  sudo apt-get update
	  sudo apt-get -y install sysstat
	  sudo chmod a+rx /home/vagrant/eng/monitor.sh
	  sudo sed -i 's|ExecStart=/usr/bin/dockerd -H fd:// --containerd=/run/containerd/containerd.sock|ExecStart=/usr/bin/dockerd -H tcp://0.0.0.0:2375 -H unix:///var/run/docker.sock|' /lib/systemd/system/docker.service
	  sudo systemctl daemon-reload
	  sudo service docker restart
    SHELL
  end
end
