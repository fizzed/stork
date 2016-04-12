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
  end

  config.vm.define "freebsd102", autostart: false do |guest|
    guest.vm.box = "bento/freebsd-10.2"
    guest.vm.synced_folder ".", "/vagrant", type: "rsync", rsync__exclude: ".git/"
    guest.vm.provision :shell, inline: "pkg install -y openjdk; echo 'fdesc /dev/fd fdescfs rw 0 0' >> /etc/fstab; echo 'proc /proc procfs rw 0	0' >> /etc/fstab; mount /dev/fd; mount /proc"
  end

  #config.vm.define "openbsd57", autostart: false do |guest|
  #  guest.vm.box = "tmatilai/openbsd-5.7"
  #  guest.vm.synced_folder ".", "/vagrant", type: "rsync", rsync__exclude: ".git/"
  #  #guest.vm.provision :shell, inline: "pkg install -y openjdk; echo 'fdesc /dev/fd fdescfs rw 0 0' >> /etc/fstab; echo 'proc /proc procfs rw 0	0' >> /etc/fstab; mount /dev/fd; mount /proc"
  #end

  #config.vm.define "openbsd58", autostart: false do |guest|
  #  guest.vm.box = "twingly/openbsd-5.8-amd64"
  #  guest.ssh.sudo_command = "doas"
    # PKG_PATH="http://ftp.openbsd.org/pub/OpenBSD/`uname -r`/packages/`arch -s`/" pkg_add -I rsync--
    #guest.vm.synced_folder ".", "/vagrant", type: "rsync", rsync__exclude: ".git/"
    #guest.vm.provision :shell, inline: "pkg install -y openjdk; echo 'fdesc /dev/fd fdescfs rw 0 0' >> /etc/fstab; echo 'proc /proc procfs rw 0	0' >> /etc/fstab; mount /dev/fd; mount /proc"
  #end

  #config.vm.define "centos6", autostart: false do |guest|
  #  guest.vm.box = "minimal/centos6"
  #end

end
