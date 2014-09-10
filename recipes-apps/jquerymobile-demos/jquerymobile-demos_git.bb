DESCRIPTION = "jQuery Mobile is a HTML5-based user interface system."
HOMEPAGE = "http://jquerymobile.com/"
LICENSE = "MIT | GPL-2.0"

RDEPENDS_${PN} = "crosswalk"

SRC_URI += "git://github.com/jquery/demos.jquerymobile.com.git;rev=10c8fde5e8b63b75ee22a902bd89d579461e1179 \
    file://jquerymobile-demos.desktop \
    file://jquerymobile-demos.png \
    file://jquerymobile-demos"

S = "${WORKDIR}/git"

LIC_FILES_CHKSUM = "file://${S}/1.1.0-rc.1/LICENSE-INFO.txt;md5=c2a81c0efd4574c2c4c4e6a6f1a1006a"

do_install() {
    install -d ${D}${libdir}/jquerymobile-demos/
    install -m 0644 ${WORKDIR}/jquerymobile-demos.png ${D}${libdir}/jquerymobile-demos/jquerymobile-demos.png
    cp -Pr ${S}/1.4.3/* ${D}${libdir}/jquerymobile-demos/

    install -d ${D}${bindir}/
    install -m 0755 ${WORKDIR}/jquerymobile-demos ${D}${bindir}/jquerymobile-demos

    install -d ${D}${datadir}/applications/
    install -m 0644 ${WORKDIR}/jquerymobile-demos.desktop ${D}${datadir}/applications/jquerymobile-demos.desktop
}

FILES_${PN} = "${bindir}/jquerymobile-demos ${datadir}/applications/* ${libdir}/jquerymobile-demos/*"
