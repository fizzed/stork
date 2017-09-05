# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure(2) do |config|

  config.vm.define "ubuntu14" do |guest|
    guest.vm.box = "minimal/trusty64"
    guest.vm.box_version = "14.04.3"
    guest.vm.provision :shell, inline: "echo 'install depends'; apt-get update; apt-get install -y openjdk-7-jre-headless unzip curl"
    # unit tests required passing along env vars in ssh commands
    guest.vm.provision :shell, inline: "echo 'forcing sshd to accept env'; echo 'AcceptEnv *' >> /etc/ssh/sshd_config"
    guest.vm.provision :shell, inline: "echo 'restart sshd'; service ssh restart"
  end

  config.vm.define "ubuntu16" do |guest|
    guest.vm.box = "bento/ubuntu-16.04"
    guest.vm.box_version = "201708.22.0"
    guest.vm.provision :shell, inline: "echo 'install depends'; apt update; apt install -y openjdk-8-jre-headless unzip curl"
    # unit tests required passing along env vars in ssh commands
    guest.vm.provision :shell, inline: "echo 'forcing sshd to accept env'; echo 'AcceptEnv *' >> /etc/ssh/sshd_config"
    guest.vm.provision :shell, inline: "echo 'restart sshd'; service ssh restart"
  end

  config.vm.define "windows10" do |guest|
    guest.vm.box = "giorgioinf/win10N-shell"
    guest.vm.box_version = "0.0.1"
    # box crawls to a halt if less than 2G of RAM
    guest.vm.provider "virtualbox" do |vb|
      vb.customize ["modifyvm", :id, "--memory", "2048"]
    end
    # go into box and then run "choco install -y jdk8"
    #guest.vm.provision :shell, powershell_elevated_interactive: true, privileged: true, inline: "choco install -y jdk8"
    #guest.vm.provision :shell, path: "https://raw.githubusercontent.com/xpando/Dash/master/PowerShell/Scripts/Install-JDK8.ps1"
  end

  config.vm.define "debian8", autostart: false do |guest|
    guest.vm.box = "minimal/jessie64"
    guest.vm.box_version = "8.0"
    guest.vm.provision :shell, inline: "apt-get update; apt-get install -y openjdk-7-jre-headless unzip curl"
    # unit tests required passing along env vars in ssh commands
    guest.vm.provision :shell, inline: "echo 'forcing sshd to accept env'; echo 'AcceptEnv *' >> /etc/ssh/sshd_config"
    guest.vm.provision :shell, inline: "/etc/init.d/ssh restart"
  end

  config.vm.define "centos7", autostart: false do |guest|
    guest.vm.box = "minimal/centos7"
    guest.vm.box_version = "7.0"
    guest.vm.provision :shell, inline: "yum install -y java-1.7.0-openjdk-headless unzip curl"
    # unit tests required passing along env vars in ssh commands
    guest.vm.provision :shell, inline: "echo 'forcing sshd to accept env'; echo 'AcceptEnv *' >> /etc/ssh/sshd_config"
    guest.vm.provision :shell, inline: "systemctl restart sshd.service"
  end

  config.vm.define "freebsd10", autostart: false do |guest|
    guest.vm.box = "bento/freebsd-10.3"
    guest.vm.box_version = "201708.22.0"
    guest.vm.synced_folder ".", "/vagrant", type: "rsync", rsync__exclude: ".git/"
    guest.vm.provision :shell, inline: "pkg install -y openjdk; echo 'fdesc /dev/fd fdescfs rw 0 0' >> /etc/fstab; echo 'proc /proc procfs rw 0	0' >> /etc/fstab; mount /dev/fd; mount /proc"
    # unit tests required passing along env vars in ssh commands
    guest.vm.provision :shell, inline: "echo 'AcceptEnv *' >> /etc/ssh/sshd_config"
    guest.vm.provision :shell, inline: "service sshd restart"
  end

  config.vm.define "openbsd60", autostart: false do |guest|
    guest.vm.box = "ryanmaclean/openbsd-6.0"
    guest.vm.provider "virtualbox" do |vb|
      vb.customize ["modifyvm", :id, "--natdnshostresolver1", "on"]
    end
    guest.ssh.sudo_command = "doas su -"
    guest.vm.synced_folder ".", "/vagrant", type: "rsync", rsync__rsync_path: "doas rsync", rsync__exclude: ".git/"
    guest.vm.provision :shell, inline: "PKG_PATH=\"http://ftp.openbsd.org/pub/OpenBSD/`uname -r`/packages/`arch -s`/\" pkg_add -I wget--"
    guest.vm.provision :shell, inline: "cd /tmp; wget \"http://ftp.openbsd.org/pub/OpenBSD/`uname -r`/`arch -s`/xbase58.tgz\"; cd /; tar xzvphf /tmp/xbase58.tgz"
    guest.vm.provision :shell, inline: "PKG_PATH=\"http://ftp.openbsd.org/pub/OpenBSD/`uname -r`/packages/`arch -s`/\" pkg_add -I jdk-1.7.0.80p0v0"
  end

  config.vm.define "osx1010", autostart: false do |guest|
    guest.vm.box = "jhcook/osx-yosemite-10.10"
    guest.vm.synced_folder ".", "/vagrant", type: "rsync", rsync__exclude: ".git/"
  end

end