# ARINDOM OS – Smooth Edition

A separate experimental Debian-based live operating system build for the Lenovo V15 IGL (64-bit, 4 GB RAM).

This project lives on the `arindom-os-build` branch so the AI ARINDOM NEON X application on `main` is not modified.

## Build status

The GitHub Actions workflow builds a live, bootable amd64 ISO and uploads it as a workflow artifact. Hardware support (especially Wi-Fi and Bluetooth) must be tested on the actual laptop.

## Safety

- Test from a USB in live mode first.
- Do not format the internal drive or install over Windows during initial testing.
- This is an experimental first build, not a production-certified operating system.

## Build environment

The ISO workflow runs inside a native Debian Trixie container (not Ubuntu's live-build package), explicitly selects Debian mode and the `live-boot` initramfs, and builds the amd64 kernel flavour. This avoids pulling Ubuntu-only live-system packages into the Debian image.
