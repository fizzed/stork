# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure(2) do |config|
  config.vm.define "ubuntu1404" do |guest|
    guest.vm.box = "minimal/trusty64"
    guest.vm.provision :shell, inline: "apt-get update; apt-get install -y openjdk-7-jre-headless unzip curl;"
    # unit tests required passing along env vars in ssh commands
    guest.vm.provision :shell, inline: "sed -i 's/\(^AcceptEnv.*$\)/AcceptEnv \*/' /etc/ssh/sshd_config"
    guest.vm.provision :shell, inline: "/etc/init.d/ssh restart"
  end

  config.vm.define "debian8", autostart: false do |guest|
    guest.vm.box = "minimal/jessie64"
    guest.vm.provision :shell, inline: "apt-get update; apt-get install -y openjdk-7-jre-headless unzip curl;"
    # unit tests required passing along env vars in ssh commands
    guest.vm.provision :shell, inline: "sed -i 's/\(^AcceptEnv.*$\)/AcceptEnv \*/' /etc/ssh/sshd_config"
    guest.vm.provision :shell, inline: "/etc/init.d/ssh restart"
  end

  config.vm.define "centos7", autostart: false do |guest|
    guest.vm.box = "minimal/centos7"
    guest.vm.provision :shell, inline: "yum install -y java-1.7.0-openjdk-headless unzip curl;"
    # unit tests required passing along env vars in ssh commands
    guest.vm.provision :shell, inline: "sed -i 's/\(^AcceptEnv.*$\)/AcceptEnv \*/' /etc/ssh/sshd_config"
    guest.vm.provision :shell, inline: "systemctl restart sshd.service"
  end

  config.vm.define "freebsd102", autostart: false do |guest|
    guest.vm.box = "bento/freebsd-10.2"
    guest.vm.synced_folder ".", "/vagrant", type: "rsync", rsync__exclude: ".git/"
    guest.vm.provision :shell, inline: "pkg install -y openjdk; echo 'fdesc /dev/fd fdescfs rw 0 0' >> /etc/fstab; echo 'proc /proc procfs rw 0	0' >> /etc/fstab; mount /dev/fd; mount /proc"
    # unit tests required passing along env vars in ssh commands
    guest.vm.provision :shell, inline: "echo 'AcceptEnv *' >> /etc/ssh/sshd_config"
    guest.vm.provision :shell, inline: "service sshd restart"
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

  config.vm.define "osx1010", autostart: false do |guest|
    guest.vm.box = "jhcook/osx-yosemite-10.10"
    guest.vm.synced_folder ".", "/vagrant", type: "rsync", rsync__exclude: ".git/"
  end

  # hmmm... local vars in script do not work, rsync folders wrong user
  #config.vm.define "omni151014", autostart: false do |guest|
  #  guest.vm.box = "http://omnios.omniti.com/media/omnios-r151014.box"
  #  guest.vm.synced_folder ".", "/vagrant", type: "rsync", rsync__exclude: ".git/"
  #end

  # rsync folders wrong user!
  #config.vm.define "netbsd7" do |guest|
  #  guest.vm.box = "kja/netbsd-7-amd64"
  #  guest.vm.synced_folder ".", "/vagrant", type: "rsync", rsync__exclude: ".git/"
  #  guest.vm.provision :shell, inline: "PKG_PATH=\"ftp://ftp.netbsd.org/pub/pkgsrc/packages/NetBSD/x86_64/7.0/All\" pkg_add -v openjdk7"
  #end

  #config.vm.define "centos6" do |guest|
  #  guest.vm.box = "minimal/centos6"
  #end

  #config.vm.define "solaris10" do |guest|
  #  guest.vm.box = "tnarik/solaris10-minimal"
  #end

  config.vm.define "openbsd58" do |guest|
    guest.vm.box = "boxcutter/openbsd58"
  end

  #config.vm.define "windows2012" do |guest|
  #  guest.vm.box = "mwrock/Windows2012R2"
  #end

end
