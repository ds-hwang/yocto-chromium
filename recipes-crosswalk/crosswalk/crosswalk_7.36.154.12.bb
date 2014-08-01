DESCRIPTION = "Crosswalk is a web runtime for ambitious HTML5 applications."
HOMEPAGE = "https://crosswalk-project.org/"
LICENSE = "BSD"

DEPENDS = "ninja-native pkgconfig-native gtk+ glib-2.0 pulseaudio libxss libdrm nss elfutils libxslt icu fontconfig harfbuzz"

SRC_URI += "https://download.01.org/crosswalk/releases/crosswalk/source/crosswalk-${PV}.tar.xz;name=tarball \
    file://use_window_manager_native_decorations.patch;patch=1 \
    file://include.gypi \
    file://defaults.gypi"

SRC_URI[tarball.md5sum] = "b20ebbbb6f87799ab861f0f1065cebbb"
SRC_URI[tarball.sha256sum] = "d9d925302c7091f1febd7dda93f6fd1aa041e2760fa90ec76f34c5eefa134f18"

COMPATIBLE_MACHINE = "(-)"
COMPATIBLE_MACHINE_i586 = "(.*)"
COMPATIBLE_MACHINE_x86-64 = "(.*)"
COMPATIBLE_MACHINE_armv6 = "(.*)"
COMPATIBLE_MACHINE_armv7a = "(.*)"

inherit gettext

S = "${WORKDIR}/crosswalk-${PV}"
LIC_FILES_CHKSUM = "file://${S}/xwalk/LICENSE;md5=c3d4637b0c8ceffb4111debb006efe58"

do_configure() {
    cd ${S}

    # Attempt to link on 32-bits systems.
    export LDFLAGS="${LDFLAGS} -Wl,--no-keep-memory"

    # Force the host compiler. When building for 64-bits target in a 64-bits host,
    # the tools built by Chromium (mostly generators) were not working because apparently
    # the target compiler is being used and the output is not always valid on the host.
    export CC_host="gcc"
    export CXX_host="g++"

    xwalk/gyp_xwalk --depth=. -I${WORKDIR}/defaults.gypi -I${WORKDIR}/include.gypi
}

do_compile() {
    ninja ${PARALLEL_MAKE} -C ${S}/out/Release xwalk
}

do_install() {
    install -d ${D}${libdir}/xwalk/
    install -m 0755 ${S}/out/Release/xwalk ${D}${libdir}/xwalk/xwalk
    install -m 0644 ${S}/out/Release/icudtl.dat ${D}${libdir}/xwalk/icudtl.dat
    install -m 0644 ${S}/out/Release/libffmpegsumo.so ${D}${libdir}/xwalk/libffmpegsumo.so
    install -m 0644 ${S}/out/Release/libosmesa.so ${D}${libdir}/xwalk/libosmesa.so
    install -m 0644 ${S}/out/Release/xwalk.pak ${D}${libdir}/xwalk/xwalk.pak

    install -d ${D}${bindir}/
    ln -sf ${libdir}/xwalk/xwalk ${D}${bindir}/xwalk
}

FILES_${PN} = "${bindir}/xwalk ${libdir}/xwalk/*"
FILES_${PN}-dbg = "${bindir}/.debug/ ${libdir}/xwalk/.debug/"

PACKAGE_DEBUG_SPLIT_STYLE = "debug-without-src"
