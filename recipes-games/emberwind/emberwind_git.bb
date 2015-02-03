DESCRIPTION = "Emberwind HTML5 is a HTML5 port of the C++ implementation of Emberwind."
HOMEPAGE = "https://github.com/operasoftware/Emberwind"
LICENSE = "BSD-3-Clause"

RDEPENDS_${PN} = "crosswalk"

SRC_URI += "git://github.com/operasoftware/Emberwind.git;rev=78a2811ee9db28bd566427253c3e4fe8a9ac78b3 \
    file://manifest.json \
    file://emberwind.desktop \
    file://emberwind"

S = "${WORKDIR}/git"

LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=5706b15318cb4073ca7b04aa1e74620d"

do_install() {
    install -d ${D}${libdir}/emberwind/
    install -m 0644 ${S}/icon_114x114.png ${D}${libdir}/emberwind
    install -m 0644 ${S}/index.html ${D}${libdir}/emberwind
    install -m 0644 ${WORKDIR}/manifest.json ${D}${libdir}/emberwind
    cp -Pr ${S}/resources ${D}${libdir}/emberwind/
    cp -Pr ${S}/src ${D}${libdir}/emberwind/

    install -d ${D}${bindir}/
    install -m 0755 ${WORKDIR}/emberwind ${D}${bindir}/emberwind

    install -d ${D}${datadir}/applications/
    install -m 0644 ${WORKDIR}/emberwind.desktop ${D}${datadir}/applications/emberwind.desktop
}

FILES_${PN} = "${bindir}/emberwind ${datadir}/applications/* ${libdir}/emberwind/*"
