# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure(2) do |config|
  config.vm.define "ubuntu1404" do |guest|
    guest.vm.box = "minimal/trusty64"
    guest.vm.provision :shell, inline: "apt-get update; apt-get install -y openjdk-7-jre-headless;"
  end

  config.vm.define "debian8", autostart: false do |guest|
    guest.vm.box = "minimal/jessie64"
    guest.vm.provision :shell, inline: "apt-get update; apt-get install -y openjdk-7-jre-headless;"
  end

  config.vm.define "centos7", autostart: false do |guest|
    guest.vm.box = "minimal/centos7"
    guest.vm.provision :shell, inline: "yum install -y java-1.7.0-openjdk;"
  end

  #config.vm.define "centos6", autostart: false do |guest|
  #  guest.vm.box = "minimal/centos6"
  #end

  config.vm.define "freebsd102", autostart: false do |guest|
    guest.vm.box = "bento/freebsd-10.2"
    guest.vm.synced_folder ".", "/vagrant", type: "rsync", rsync__exclude: ".git/"
    guest.vm.provision :shell, inline: "pkg install -y openjdk; echo 'fdesc /dev/fd fdescfs rw 0 0' >> /etc/fstab; echo 'proc /proc procfs rw 0	0' >> /etc/fstab; mount /dev/fd; mount /proc"
  end

  config.vm.define "openbsd58", autostart: false do |guest|
    guest.vm.box = "boxcutter/openbsd58"
    guest.vm.provider "virtualbox" do |vb|
      vb.customize ["modifyvm", :id, "--natdnshostresolver1", "on"]
    end
    guest.ssh.sudo_command = "doas su -"
    guest.vm.synced_folder ".", "/vagrant", type: "rsync", rsync__rsync_path: "doas rsync", rsync__exclude: ".git/"
    guest.vm.provision :shell, inline: "PKG_PATH=\"http://ftp.openbsd.org/pub/OpenBSD/`uname -r`/packages/`arch -s`/\" pkg_add -I wget--"
    guest.vm.provision :shell, inline: "cd /tmp; wget \"http://ftp.openbsd.org/pub/OpenBSD/`uname -r`/`arch -s`/xbase58.tgz\"; cd /; tar xzvphf /tmp/xbase58.tgz"
    guest.vm.provision :shell, inline: "PKG_PATH=\"http://ftp.openbsd.org/pub/OpenBSD/`uname -r`/packages/`arch -s`/\" pkg_add -I jdk-1.7.0.80p0v0"
  end
end
