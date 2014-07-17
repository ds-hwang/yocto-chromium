DESCRIPTION = "WebRTC is an open source project that enables web browsers with Real-Time Communications (RTC) capabilities via simple Javascript APIs."
HOMEPAGE = "http://www.webrtc.org/demo"
LICENSE = "BSD-3-Clause"

RDEPENDS_${PN} = "crosswalk"

SRC_URI += "file://webrtc.desktop \
    file://webrtc \
    file://icon.png \
    file://LICENSE"

LIC_FILES_CHKSUM = "file://${WORKDIR}/LICENSE;md5=ad296492125bc71530d06234d9bfebe0"

do_install() {
    install -d ${D}${libdir}/webrtc/
    install -m 0644 ${WORKDIR}/icon.png ${D}${libdir}/webrtc/icon.png

    install -d ${D}${bindir}/
    install -m 0755 ${WORKDIR}/webrtc ${D}${bindir}/webrtc

    install -d ${D}${datadir}/applications/
    install -m 0644 ${WORKDIR}/webrtc.desktop ${D}${datadir}/applications/webrtc.desktop
}

FILES_${PN} = "${bindir}/webrtc ${datadir}/applications/* ${libdir}/webrtc/*"
