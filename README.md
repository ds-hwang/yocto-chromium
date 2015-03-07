# Introduction

Let's build Chromium on yocto. Both X11 and GBM are built in this doc.
Acknowledge: @tmpsantos teach me everything. This doc also is copied from https://github.com/otcshare/meta-crosswalk-embedded

I have experience to build chromium on chrome os and tizen. Yocto is way better. Yocto doesn't use chroot, so you can keep the source in another partition of storage. 

# Contents

  - [Design](#design) - the architecture behind
  - [Howto](#howto) - set up the system environment, build and run

# Design

* similar to crosswalk
![Alt text](https://raw.github.com/tiagovignatti/misc/master/yoctocrosswalkembedded-arch.png "Embedded Crosswalk Project architecture overview")

# Howto

**Important caveat: this doc only covers x86 or x64.
(Intel) and modern hardware containing GPU due the Chromium Ozone-GBM
architecture. That said, we don't have plans to extend it to any other category of
devices. For testing, development and deployment we recommend the [MinnowBoard MAX](http://www.minnowboard.org/meet-minnowboard-max/)**.

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
  $ git clone https://github.com/ds-hwang/meta-browser.git
  ```

* If you want stable branch of poky, checkout `git checkout dizzy`. I usually work on master.
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
  /media/yocto/meta-browser \
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
PREFERRED_VERSION_chromium = "local"
# your local chromium tree
CHROMIUM_LOCAL_PATH = "/home/dshwang/chromium/src"
CHROMIUM_OUT_DIR = "out_yocto"
# if you want to build shared library
PACKAGECONFIG_append_pn-chromium = " component-build"
  ```

Now close the file and let's cook the package: 
  ```
  $ bitbake chromium
  ```  

If you want to compile only, you can do it. It's conve
  ```
  $ bitbake -c compile chromium
  ```

You can build whole yocto image including chromium also.
  ```
  $ bitbake core-image-sato
  ```

It will take several hours to download much of the dependencies, build and
etc. Relax now. If everything goes fine, you will have the following directory
with the images built in inside:
  ```
  $ ls tmp/deploy/images/genericx86-64/*.hddimg
  $ tmp/deploy/images/genericx86-64/core-image-sato-genericx86-64-20141009113028.hddimg
  $ tmp/deploy/images/genericx86-64/core-image-sato-genericx86-64.hddimg
  ```

Make sure you have now inserted a USB flash drive, **checking the correct file
descriptor** that Linux will be using with the `sudo fdisk -l` command. For
example in our system it is ```/dev/sdd```, so the following is what we used to
flash it:
  ```
  $ cd tmp/deploy/images/genericx86-64/
  $ sudo dd if=core-image-sato-genericx86-64.hddimg of=/dev/sdd
  $ sync 
  $ sudo eject /dev/sdd
  ```

You are able now to boot the flash drive in your hardware and play around with
Chromium browser.

# Tips
## ICECC
add following lines in local.conf
```
PARALLEL_MAKE = "-j 40"
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

 * A1: purge fucking hardening-wrapper (which wastes my 2 days) `sudo apt-get purge hardening-wrapper hardening-includes`
 * A2: purge clang and all gcc and then reinstall only minimal gcc

## my conf
```
diff --git a/bblayers.conf b/bblayers.conf
index 4513d30..53e0bce 100644
--- a/bblayers.conf
+++ b/bblayers.conf
@@ -9,6 +9,7 @@ BBLAYERS ?= " \
   /d/workspace/yocto/poky/meta \
   /d/workspace/yocto/poky/meta-yocto \
   /d/workspace/yocto/poky/meta-yocto-bsp \
+  /d/workspace/yocto/meta-browser \
   "
 BBLAYERS_NON_REMOVABLE ?= " \
   /d/workspace/yocto/poky/meta \
diff --git a/local.conf b/local.conf
index a1d99f9..334152f 100644
--- a/local.conf
+++ b/local.conf
@@ -29,12 +29,12 @@
 #
 #MACHINE ?= "beaglebone"
 #MACHINE ?= "genericx86"
-#MACHINE ?= "genericx86-64"
+MACHINE ?= "genericx86-64"
 #MACHINE ?= "mpc8315e-rdb"
 #MACHINE ?= "edgerouter"
 #
 # This sets the default machine to be qemux86 if no other machine is selected:
-MACHINE ??= "qemux86"
+#MACHINE ??= "qemux86"
 
 #
 # Where to place downloads
@@ -230,3 +230,13 @@ ASSUME_PROVIDED += "libsdl-native"
 # track the version of this file when it was generated. This can safely be ignored if
 # this doesn't mean anything to you.
 CONF_VERSION = "1"
+
+PARALLEL_MAKE = "-j 40"
+ICECC_PATH = "/home/dshwang/thirdparty/icecream/install/bin/icecc"
+INHERIT += "icecc"
+
+LICENSE_FLAGS_WHITELIST = "commercial"
+PREFERRED_VERSION_chromium = "local"
+CHROMIUM_LOCAL_PATH = "/home/dshwang/chromium/src"
+CHROMIUM_OUT_DIR = "out_yocto"
+PACKAGECONFIG_append_pn-chromium = " component-build"
```

# Reference
* [META Crosswalk project](https://github.com/otcshare/meta-crosswalk-embedded)
* [Yocto Quick start](http://www.yoctoproject.org/docs/latest/yocto-project-qs/yocto-project-qs.html)
* [Yocto dev manual](http://www.yoctoproject.org/docs/1.6/dev-manual/dev-manual.html)
