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

The ISO workflow runs Debian Trixie on the GitHub-hosted Ubuntu VM using a privileged Docker container. This is required because live-build must mount `/dev/pts` and `/proc` for its chroot; a standard GitHub Actions job container blocks those mount operations. The workflow explicitly selects Debian mode, the `live-boot` initramfs, and the amd64 kernel flavour.


Build workflow improvements: phase timestamps, bounded package installation and ISO generation, verbose live-build output, and a concurrency guard to prevent duplicate builds.
