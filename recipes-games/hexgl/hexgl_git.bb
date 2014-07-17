DESCRIPTION = "HexGL is a futuristic, fast-paced racing game."
HOMEPAGE = "http://hexgl.bkcore.com/"
LICENSE = "CC-BY-NC-3.0"

RDEPENDS_${PN} = "crosswalk"

SRC_URI += "git://github.com/BKcore/HexGL.git;rev=182a636fa01797cea0a92aa58199931e116506b1 \
    file://legalcode \
    file://hexgl.desktop \
    file://hexgl"

LIC_FILES_CHKSUM = "file://${WORKDIR}/legalcode;md5=96071a19a493825e8c7a2e6e9c5be24f"

S = "${WORKDIR}/git"

do_install() {
    install -d ${D}${libdir}/hexgl/
    install -m 0644 ${S}/favicon.png ${D}${libdir}/hexgl
    install -m 0644 ${S}/icon_256.png ${D}${libdir}/hexgl
    install -m 0644 ${S}/index.html ${D}${libdir}/hexgl
    install -m 0644 ${S}/launch.coffee ${D}${libdir}/hexgl
    install -m 0644 ${S}/launch.js ${D}${libdir}/hexgl
    cp -Pr ${S}/bkcore ${D}${libdir}/hexgl/
    cp -Pr ${S}/bkcore.coffee ${D}${libdir}/hexgl/
    cp -Pr ${S}/css ${D}${libdir}/hexgl/
    cp -Pr ${S}/geometries ${D}${libdir}/hexgl/
    cp -Pr ${S}/replays ${D}${libdir}/hexgl/
    cp -Pr ${S}/textures ${D}${libdir}/hexgl/
    cp -Pr ${S}/textures.full ${D}${libdir}/hexgl/

    install -d ${D}${libdir}/hexgl/libs/
    install -m 0644 ${S}/libs/*.js ${D}${libdir}/hexgl/libs
    cp -Pr ${S}/libs/postprocessing ${D}${libdir}/hexgl/libs/

    install -d ${D}${bindir}/
    install -m 0755 ${WORKDIR}/hexgl ${D}${bindir}/hexgl

    install -d ${D}${datadir}/applications/
    install -m 0644 ${WORKDIR}/hexgl.desktop ${D}${datadir}/applications/hexgl.desktop
}

FILES_${PN} = "${bindir}/hexgl ${datadir}/applications/* ${libdir}/hexgl/*"
