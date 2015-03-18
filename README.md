# Yocto Chromium
Chromium recipes for yocto

# Introduction

Let's build Chromium on yocto. You can build both X11 and Ozone GBM. This recipes is for chromium developers who want to build chromium on yocto. You may checkout chromium source in your computer already. You will build Chromium using your local source. Yocto build system is smart enough to not copy or change your local source.

Acknowledge: @tmpsantos teach me everything.

I have experience to build chromium on chrome os and tizen. Yocto is way better. Yocto doesn't use chroot, so you can keep the source in another partition of storage. 

# Contents

  - [Design](#design) - the architecture behind
  - [Howto](#howto) - set up the system environment, build and run

# Design

* similar to [Crosswalk](https://github.com/crosswalk-project/crosswalk)
![Alt text](https://raw.github.com/tiagovignatti/misc/master/yoctocrosswalkembedded-arch.png "Embedded Crosswalk Project architecture overview")

# Howto

**Important caveat: this project only covers x86 or x64. I didn't test ARM
but there is no reason to not be possible to build for ARM.
For Chromium Ozone-GBM, I test it on Intel Haswell and newer generation GPU. That said, we don't have plans to extend it to any other category of
devices. For testing, development and deployment we recommend the [MinnowBoard MAX](http://www.minnowboard.org/meet-minnowboard-max/) or Intel Haswell without any external GPU**.

This guide will help you to build a bootable image with the Chromium. Most of
the toolchain needed to build comes from Yocto Poky and it's expected to use
just a few of your system's dependencies. We use Ubuntu 14.04 LTS (Trusty
Tahr) in particular, but there's no reason to not use any other different
system. Besides, make sure you have **at least** 45 GB of disk space to store
all the sources and a few types of images to be built.

Firstly, you will need anyway a couple of your distribution tools and
development packages:

  ```
  $ sudo apt-get install gawk wget git-core diffstat unzip texinfo \
gcc-multilib build-essential chrpath libsdl1.2-dev xterm gyp
  ```

then download the needed stuff:

  ```
  $ mkdir yocto
  $ cd yocto/

  $ git clone http://git.yoctoproject.org/git/poky
  $ git clone https://github.com/otcshare/yocto-chromium.git
  ```

* If you want stable branch of poky, checkout `git checkout dizzy`. I usually work on master. I currently use `2f8e5a8be1b2baf1aa9cf275f74a906b7597a45b` commit.
* jump to its build environment:

  ```
  $ cd poky/
  $ source oe-init-build-env

  ```

You had no conf/local.conf file so a configuration file has therefore been
created for you with some default values, but we need still to add the
meta-browser layers in conf/bblayers.conf - mind to change
the lines below with the **full path of the directory you are cloning the
repos** (in our case it was /media/yocto/) :

  ```
BBLAYERS ?= " \
  /media/yocto/poky/meta \
  /media/yocto/poky/meta-yocto \
  /media/yocto/poky/meta-yocto-bsp \
  /media/yocto/yocto-chromium/meta-chromium \
  "
  ```

then, set in conf/local.conf the genericx86-64 machine (you could try a
different architecture but we haven't yet):

  ```
MACHINE ?= "genericx86-64"
  ```

then, set chromium configuration:

  ```
# to use libav
LICENSE_FLAGS_WHITELIST = "commercial"
# your local chromium tree
CHROMIUM_LOCAL_PATH = "/home/dshwang/chromium/src"
# you might use "out" directory for linux chromium already.
CHROMIUM_OUT_DIR = "out_yocto"
# if you want to build shared library
PACKAGECONFIG_append_pn-chromium = " component-build"
IMAGE_INSTALL_append = " chromium"
  ```

Now close the file and let's build whole yocto image including chromium also. `core-image-sato` is the referece image including window manager using X11.
  ```
  $ bitbake core-image-sato
  ```

You can cook only the package: 

  ```
  $ bitbake chromium
  ```  

If you want to compile only, you can do it.
  ```
  $ bitbake -c compile chromium
  ```


It will take several hours to download much of the dependencies, build and
etc. Relax now. If everything goes fine, you will have the following directory
with the images built in inside:
  ```
  $ ls tmp/deploy/images/genericx86-64/*.hddimg
  $ tmp/deploy/images/genericx86-64/core-image-sato-genericx86-64-20150307113028.hddimg
  $ tmp/deploy/images/genericx86-64/core-image-sato-genericx86-64.hddimg
  ```

Make sure you have now inserted a USB flash drive, **checking the correct file
descriptor** that Linux will be using with the `sudo fdisk -l` command. For
example in our system it is ```/dev/sdc```, so the following is what we used to
flash it:
  ```
  $ cd tmp/deploy/images/genericx86-64/
  $ sudo dd if=core-image-sato-genericx86-64.hddimg of=/dev/sdc
  $ sync 
  $ sudo eject /dev/sdc
  ```

You are able now to boot the flash drive in your hardware and play around with
Chromium browser.

## Ozone GBM
* change `local.conf` as follows to build Chromium Ozone GBM
 * Add "ozone-gbm" to "PACKAGECONFIG"
 * GBM requires the latest version of kernel
```
PACKAGECONFIG_append_pn-chromium = " component-build ozone-gbm"
PREFERRED_VERSION_linux-yocto = "3.17%"
```

When you build whole yocto image, use `core-image-minimal` which doesn't include X11 and window manager.
  ```
  $ bitbake core-image-minimal
  ```


# Tips
## ICECC
add following lines in local.conf
```
PARALLEL_MAKE = "-j 80"
ICECC_PATH = "/home/dshwang/thirdparty/icecream/install/bin/icecc"
INHERIT += "icecc"
```

Before `bitbake` you must exclude icecc toolchain wrapper path(e.g. `/usr/lib/icecc/bin`) from $PATH
* Reference
 * [icecc.bbclass](http://git.yoctoproject.org/cgit.cgi/poky/plain/meta/classes/icecc.bbclass)
 * [Using IceCC in OpenEmbedded](http://www.openembedded.org/wiki/Using_IceCC)


### Icecc trouble shooting
* icecc you builds by yourself gets along with yocto. don't worry.

* exception 23
 * this means that your machine makes wrong toolchain.
```
ICECC[24079] 13:52:14: compiler did not start - compiled on 10.237.72.78
ICECC[24079] 13:52:14: got exception 23 (10.237.72.78) 
```

 * A1: purge annoying hardening-wrapper (which wastes my 2 days) `sudo apt-get purge hardening-wrapper hardening-includes`
 * A2: purge clang and all gcc and then reinstall only minimal gcc

## Add more tools in minimal images
* you might need "bash", "ssh", "sshfs" for more convinient embedded development.
 * checkout [meta-openembedded](git://git.openembedded.org/meta-openembedded) and then add following lines to `bblayers.conf`
```
BBLAYERS ?= " \
   ...
  /d/workspace/yocto/meta-openembedded/meta-oe \
  /d/workspace/yocto/meta-openembedded/meta-filesystems \
  "
```

 * add packages you want ot `local.conf`
```
-EXTRA_IMAGE_FEATURES = "debug-tweaks"
+EXTRA_IMAGE_FEATURES = "debug-tweaks ssh-server-dropbear"

-IMAGE_INSTALL_append = " chromium"
+IMAGE_INSTALL_append = " chromium sshfs-fuse bash"

```

## How to use sshfs
* install sshfs on your machine, then add yourself to the fuse group:
 * Refer [Ubuntu SSHFS](https://help.ubuntu.com/community/SSHFS)
```
> sudo apt-get install sshfs
> sudo gpasswd -a $USER fuse
```

* ssh to device
```
> ssh root@$<YOCTOURL>
$
```

* (optional) you can use bash. Do you remember we added bash package on the image :)
```
$ bash
```

* ssh from device to your machine because it loads some kernel module, which means sshfs has a bug not-loading it.
```
$ ssh <ID>@<HOST>
> [crtl + d]
```

* sshfs mounts host chromium directory on device
```
$ mkdir remote
$ sshfs -o idmap=user <ID>@<HOST>:<chromium path> /home/root/remote/
```

* Enjoy hack

* unmount if needed
```
fusermount -u /home/root/remote
```


## my conf
* Refer to my [local.conf](reference_conf/local.conf) and [bblayers.conf](reference_conf/bblayers.conf)

# Reference
* [META Crosswalk project](https://github.com/otcshare/meta-crosswalk-embedded)
* [Yocto Quick start](http://www.yoctoproject.org/docs/latest/yocto-project-qs/yocto-project-qs.html)
* [Yocto dev manual](http://www.yoctoproject.org/docs/1.6/dev-manual/dev-manual.html)
* [meta-crosswalk-embedded project](https://github.com/otcshare/meta-crosswalk-embedded)