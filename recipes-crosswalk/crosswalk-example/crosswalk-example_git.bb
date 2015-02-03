DESCRIPTION = "Crosswalk Example app."
LICENSE = "BSD"

DEPENDS = "ninja-native"
RDEPENDS_${PN} = "crosswalk"

SRC_URI += "git://github.com/tmpsantos/crosswalk-example.git;rev=da99f63fa2a7163e4b92cc4a507b9cbd1d72c02f \
    file://crosswalk-example.desktop \
    file://crosswalk.png"

S = "${WORKDIR}/git"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=c3d4637b0c8ceffb4111debb006efe58"

do_configure() {
    cd ${S}
    ./configure
}

do_compile() {
    ninja ${PARALLEL_MAKE} -C ${S}/out/Default
}

do_install() {
    install -d ${D}${libdir}/xwalk-example/
    install -m 0644 ${WORKDIR}/crosswalk.png ${D}${libdir}/xwalk-example/crosswalk.png
    install -m 0644 ${S}/src/dygraph-combined.js ${D}${libdir}/xwalk-example/dygraph-combined.js
    install -m 0644 ${S}/src/index.html ${D}${libdir}/xwalk-example/index.html
    install -m 0644 ${S}/src/manifest.json ${D}${libdir}/xwalk-example/manifest.json
    install -m 0644 ${S}/out/Default/libcpu.so ${D}${libdir}/xwalk-example/libcpu.so

    install -d ${D}${datadir}/applications/
    install -m 0644 ${WORKDIR}/crosswalk-example.desktop ${D}${datadir}/applications/crosswalk-example.desktop

    install -d ${D}${bindir}/
    install -m 0755 ${S}/debian/scripts/xwalk-example ${D}${bindir}/xwalk-example
}

FILES_${PN} = "${bindir}/xwalk-example ${datadir}/applications/* ${libdir}/xwalk-example/*"
FILES_${PN}-dbg = "${libdir}/xwalk-example/.debug/"

PACKAGE_DEBUG_SPLIT_STYLE = "debug-without-src"
