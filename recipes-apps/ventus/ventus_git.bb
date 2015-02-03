DESCRIPTION = "A window manager experiment written in Javascript, HTML5 and CSS3."
HOMEPAGE = "http://www.rlamana.es/ventus"
LICENSE = "MIT"

RDEPENDS_${PN} = "\
    crosswalk \
    crosswalk-example \
    emberwind \
    hexgl \
    jquerymobile-demos \
    webgl-motion-detector \
    webrtc \
    "

SRC_URI += "\
    git://github.com/rlamana/Ventus.git;rev=27ddc5e296d6f95737534abe2727e95a1a741b33 \
    file://crosswalk_wm.patch;patch=1 \
    file://init \
    file://ventus \
    file://ventus.service \
    file://manifest.json \
    "

S = "${WORKDIR}/git"

LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=cb22e397c3ec96cd37c14022da86a678"

inherit update-rc.d systemd

INITSCRIPT_NAME = "ventus-service"
INITSCRIPT_PARAMS = "start 06 5 2 3 . stop 22 0 1 6 ."

SYSTEMD_SERVICE_${PN} = "ventus.service"

do_compile() {
    # No-op. Do not run make!
    exit 0
}

do_install() {
    install -d ${D}${libdir}/ventus/
    cp -Pr ${S}/* ${D}${libdir}/ventus/
    install -m 0755 ${WORKDIR}/manifest.json ${D}${libdir}/ventus/examples/simple/

    install -d ${D}${bindir}/
    install -m 0755 ${WORKDIR}/ventus ${D}${bindir}/ventus

    if ${@bb.utils.contains('DISTRO_FEATURES','sysvinit','true','false',d)}; then
        install -d ${D}${sysconfdir}/init.d
        install -m 0755 ${WORKDIR}/init ${D}${sysconfdir}/init.d/ventus-service
    else
        install -d ${D}${systemd_unitdir}/system
        install -m 0644 ${WORKDIR}/ventus.service ${D}${systemd_unitdir}/system
    fi
}
