DESCRIPTION = "Extremely Fast and Simple WebGL Motion Detector."
HOMEPAGE = "http://www.sw-engineering-candies.com/blog-1/extremely-fast-and-simple-webgl-motion-detector-to-rotate-3d-graphic"
LICENSE = "Proprietary"

RDEPENDS_${PN} = "crosswalk"

SRC_URI += "git://github.com/MarkusSprunck/webgl-motion-detector.git;rev=c71881d60d93b0676378c4a49d8d213598d75f17 \
    file://webgl-motion-detector.desktop \
    file://webgl-motion-detector.png \
    file://webgl-motion-detector"

S = "${WORKDIR}/git"

LIC_FILES_CHKSUM = "file://${S}/README.md;md5=82393b214d2fd20e8943b5c3a83e4639"

do_install() {
    install -d ${D}${libdir}/webgl-motion-detector/
    install -m 0644 ${WORKDIR}/webgl-motion-detector.png ${D}${libdir}/webgl-motion-detector/webgl-motion-detector.png
    cp -Pr ${S}/webgl-motion-detector/* ${D}${libdir}/webgl-motion-detector/

    install -d ${D}${bindir}/
    install -m 0755 ${WORKDIR}/webgl-motion-detector ${D}${bindir}/webgl-motion-detector

    install -d ${D}${datadir}/applications/
    install -m 0644 ${WORKDIR}/webgl-motion-detector.desktop ${D}${datadir}/applications/webgl-motion-detector.desktop
}

FILES_${PN} = "${bindir}/webgl-motion-detector ${datadir}/applications/* ${libdir}/webgl-motion-detector/*"
