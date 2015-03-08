# Recipe files compiles chromium using local chromium source pointed by CHROMIUM_EXTRA_GYP_DEFINES
# Must set values for these variables:
#    * CHROMIUM_LOCAL_PATH : "/path/to/my/chromium/checkout/src"
# Optionally, set values for these variables:
#    * CHROMIUM_OUT_DIR : which directory do you want to build. default is "out"
#    * PACKAGECONFIG_append_pn : choose more packages ex> PACKAGECONFIG_append_pn-chromium = " component-build ozone-gbm"
#    * CHROMIUM_EXTRA_GYP_DEFINES : additional GYP_DEFINES ex> CHROMIUM_EXTRA_GYP_DEFINES = "-Dwerror="


DESCRIPTION = "Chromium browser"
LICENSE = "BSD"
DEPENDS = "xz-native pciutils pulseaudio cairo nss zlib-native libav cups ninja-native gconf libexif pango libdrm gtk+ "
SRC_URI = "\
        file://include.gypi \
        ${@bb.utils.contains('PACKAGECONFIG', 'component-build', 'file://component-build.gypi', '', d)} \
        file://google-chrome \
        file://google-chrome.desktop \
"

GYP_DEFINES = "clang=0 host_clang=0 "

CHROMIUM_EXTRA_GYP_DEFINES += " \
	-I ${WORKDIR}/include.gypi \
	${@bb.utils.contains('PACKAGECONFIG', 'component-build', '-I ${WORKDIR}/component-build.gypi', '', d)} \
"

# yocto doesn't include
## use_gnome_keyring : gnome keyring library
## use_kerberos : libkrb5
## disable_fatal_linker_warnings : hidden symbol 'sqlite3_XXX'
GYP_DEFINES += " use_gnome_keyring=0  use_kerberos=0 disable_fatal_linker_warnings=1 "

GOLD_DEFINES = "${@base_contains('DISTRO_FEATURES', 'ld-is-gold', '', ' linux_use_gold_binary=0 linux_use_gold_flags=0', d)}"

# if icecc is used, don't use gold.
GYP_DEFINES += " \
    ${@bb.utils.contains('INHERIT', 'icecc', ' linux_use_bundled_binutils=0 linux_use_debug_fission=0', '${GOLD_DEFINES}', d)} \
"

# Set this variable with the path of your chromium checkout after
# running gclient sync.
CHROMIUM_LOCAL_PATH ?= "/path/to/my/chromium/checkout/src"

CHROMIUM_OUT_DIR ?= "out"

# PACKAGECONFIG explanations:
#
# * ozone-gbm : Chromium uses GBM instead of X11. To run it, bitbake core-image-minimal, not core-image-sato
#
# * use-egl : Without this packageconfig, the Chromium build will use GLX for creating an OpenGL context in X11,
#             and regular OpenGL for painting operations. Neither are desirable on embedded platforms. With this
#             packageconfig, EGL and OpenGL ES 2.x are used instead. On by default.
#
# * component-build : Enables component build mode. By default, all of Chromium (with the exception of FFmpeg)
#                     is linked into one big binary. The linker step requires at least 8 GB RAM. Component mode
#                     was created to facilitate development and testing, since with it, there is not one big
#                     binary; instead, each component is linked to a separate shared object.
#                     Use component mode for development, testing, and in case the build machine is not a 64-bit
#                     one, or has less than 8 GB RAM. Off by default.

# include.gypi exists only for armv6 and armv7a and there isn't something like COMPATIBLE_ARCH afaik
COMPATIBLE_MACHINE = "(-)"
COMPATIBLE_MACHINE_i586 = "(.*)"
COMPATIBLE_MACHINE_x86-64 = "(.*)"
COMPATIBLE_MACHINE_armv6 = "(.*)"
COMPATIBLE_MACHINE_armv7a = "(.*)"

inherit gettext

ARMFPABI_armv7a = "${@bb.utils.contains('TUNE_FEATURES', 'callconvention-hard', 'arm_float_abi=hard', 'arm_float_abi=softfp', d)}"

GYP_DEFINES += " ${ARMFPABI}"

PACKAGECONFIG[use-egl] = ",, virtual/egl virtual/libgles2 "
PACKAGECONFIG[ozone-gbm] = ",, virtual/egl udev , libegl-mesa liberation-fonts libgbm libglapi libgles1-mesa libgles2-mesa libudev mesa-megadriver "

CHROMIUM_EXTRA_ARGS ?= " \
	${@bb.utils.contains('PACKAGECONFIG', 'use-egl', '--use-gl=egl', '', d)} \
	${@bb.utils.contains('PACKAGECONFIG', 'ozone-gbm', '--ozone-platform=gbm', '', d)} \
"

# OZONE GBM flag.
CHROMIUM_ENABLE_GBM = "${@bb.utils.contains('PACKAGECONFIG', 'ozone-gbm', '1', '0', d)}"

python() {
    if d.getVar('CHROMIUM_ENABLE_GBM', True) == '1':
        d.appendVar('DEPENDS_remove', "libxss")
        # -Duse_brlapi=0 -Dremoting=0 for lack of some libraries.
        d.appendVar('GYP_DEFINES', " use_ozone=1 chromeos=1 ozone_platform_gbm=1 use_brlapi=0 remoting=0")
}

do_configure() {
	cd ${S}
	export GYP_DEFINES="${GYP_DEFINES}"
	export GYP_GENERATOR_FLAGS="output_dir=${CHROMIUM_OUT_DIR}"
	# replace LD with CXX, to workaround a possible gyp issue?
	export LD="${CXX}"
	export CC="${CC}"
	export CXX="${CXX}"
	export CC_host="${BUILD_CC}"
	export CXX_host="${BUILD_CXX}"
	build/gyp_chromium --depth=. ${CHROMIUM_EXTRA_GYP_DEFINES}
}

do_compile() {
	# build with ninja
	ninja -C ${S}/${CHROMIUM_OUT_DIR}/Release ${PARALLEL_MAKE} chrome content_shell app_shell chrome_sandbox
}

do_install() {
	install -d ${D}${bindir}
	install -m 0755 ${WORKDIR}/google-chrome ${D}${bindir}/

	# Add extra command line arguments to google-chrome script by modifying
	# the dummy "CHROME_EXTRA_ARGS" line
	sed -i "s/^CHROME_EXTRA_ARGS=\"\"/CHROME_EXTRA_ARGS=\"${CHROMIUM_EXTRA_ARGS}\"/" ${D}${bindir}/google-chrome

	install -d ${D}${datadir}/applications
	install -m 0644 ${WORKDIR}/google-chrome.desktop ${D}${datadir}/applications/

	install -d ${D}${bindir}/chrome/
	install -m 0755 ${S}/${CHROMIUM_OUT_DIR}/Release/chrome ${D}${bindir}/chrome/chrome
	install -m 0755 ${S}/${CHROMIUM_OUT_DIR}/Release/app_shell ${D}${bindir}/chrome/app_shell
	install -m 0755 ${S}/${CHROMIUM_OUT_DIR}/Release/content_shell ${D}${bindir}/chrome/content_shell
	install -m 0644 ${S}/${CHROMIUM_OUT_DIR}/Release/resources.pak ${D}${bindir}/chrome/
	install -m 0644 ${S}/${CHROMIUM_OUT_DIR}/Release/icudtl.dat ${D}${bindir}/chrome/
	install -m 0644 ${S}/${CHROMIUM_OUT_DIR}/Release/content_resources.pak ${D}${bindir}/chrome/
	install -m 0644 ${S}/${CHROMIUM_OUT_DIR}/Release/keyboard_resources.pak ${D}${bindir}/chrome/
	install -m 0644 ${S}/${CHROMIUM_OUT_DIR}/Release/chrome_100_percent.pak ${D}${bindir}/chrome/
	install -m 0644 ${S}/${CHROMIUM_OUT_DIR}/Release/product_logo_48.png ${D}${bindir}/chrome/
	install -m 0755 ${S}/${CHROMIUM_OUT_DIR}/Release/libffmpegsumo.so ${D}${bindir}/chrome/
	install -m 0755 ${S}/${CHROMIUM_OUT_DIR}/Release/*.bin ${D}${bindir}/chrome/

	# Always adding this libdir (not just with component builds), because the
	# LD_LIBRARY_PATH line in the google-chromium script refers to it
	install -d ${D}${libdir}/chrome/
	if [ -n "${@bb.utils.contains('PACKAGECONFIG', 'component-build', 'component-build', '', d)}" ]; then
		install -m 0755 ${S}/${CHROMIUM_OUT_DIR}/Release/lib/*.so ${D}${libdir}/chrome/
	fi

	install -d ${D}${sbindir}
	install -m 4755 ${S}/${CHROMIUM_OUT_DIR}/Release/chrome_sandbox ${D}${sbindir}/chrome-devel-sandbox

	install -d ${D}${bindir}/chrome/locales/
	install -m 0644 ${S}/${CHROMIUM_OUT_DIR}/Release/locales/en-US.pak ${D}${bindir}/chrome/locales
}

FILES_${PN} = "${bindir}/chrome/ ${bindir}/google-chrome ${datadir}/applications ${sbindir}/ ${libdir}/chrome/"
FILES_${PN}-dbg += "${bindir}/chrome/.debug/ ${libdir}/chrome/.debug/"

PACKAGE_DEBUG_SPLIT_STYLE = "debug-without-src"

LIC_FILES_CHKSUM = "file://LICENSE;md5=537e0b52077bf0a616d0a0c8a79bc9d5"

S = "${CHROMIUM_LOCAL_PATH}"
