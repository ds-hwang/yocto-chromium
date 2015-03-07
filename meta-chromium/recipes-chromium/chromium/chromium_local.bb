include chromium.inc

LIC_FILES_CHKSUM = "file://LICENSE;md5=537e0b52077bf0a616d0a0c8a79bc9d5"
SRC_URI = "\
        file://include.gypi \
        file://oe-defaults.gypi \
        ${@bb.utils.contains('PACKAGECONFIG', 'component-build', 'file://component-build.gypi', '', d)} \
        file://google-chrome \
        file://google-chrome.desktop \
        "

EXTRA_OEGYP += "\
        -Dlinux_use_bundled_binutils=0 \
        -Dlinux_use_debug_fission=0 \
        "

# Set this variable with the path of your chromium checkout after
# running gclient sync.
CHROMIUM_LOCAL_PATH ?= "/path/to/my/chromium/checkout/src"

S = "${CHROMIUM_LOCAL_PATH}"
