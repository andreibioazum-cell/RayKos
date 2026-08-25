<div align="center">
  <p>
    <img src="https://github.com/andreibioazum-cell/RayKos/raw/master/.github/images/app.svg" alt="RayKos logo" width="200" />
  </p>
  <h1>RayKos</h1>
  <p>
    <a href="README.md">Русский</a> |
    English |
    <a href="README-tr.md">Türkçe</a>
  </p>
  <p>
    <a href="https://github.com/andreibioazum-cell/RayKos/releases/latest"><img src="https://img.shields.io/github/v/release/andreibioazum-cell/RayKos" alt="Latest Release" /></a>
    <a href="https://github.com/andreibioazum-cell/RayKos/releases"><img src="https://img.shields.io/github/downloads/andreibioazum-cell/RayKos/total" alt="Downloads" /></a>
    <a href="https://github.com/andreibioazum-cell/RayKos/blob/master/LICENSE"><img src="https://img.shields.io/github/license/andreibioazum-cell/RayKos" alt="License" /></a>
    <a href="https://github.com/andreibioazum-cell/RayKos"><img src="https://img.shields.io/github/languages/code-size/andreibioazum-cell/RayKos" alt="GitHub code size in bytes"/></a>
  </p>
</div>

An Android application that runs ByeDPI locally and redirects all traffic through it.

RayKos has no settings, no editors, and no strategy selection: the best bypass parameters are built into the app, so you just press the button.

This application is not a VPN. It uses Android's VPN mode to redirect traffic, but it does not send any data to a remote server. It does not encrypt your traffic or hide your IP address.

---

### Features

* The best bypass parameters are built in — nothing to configure
* Automatically starts the service when the device boots
* Improved compatibility with Android TV/BOX devices
* SOCKS proxy at 127.0.0.1:1080 is always available (e.g. for SmartTube)

### Usage

* Press the big button on the main screen.
* It is recommended to connect to the VPN once in order to accept the permission request.
* After that, the application will automatically start the service when the device boots.
* For SmartTube, the connection works right away, or configure the SOCKS proxy 127.0.0.1:1080.

### Building

1. Clone the repository with its submodules:

```bash
git clone --recurse-submodules
```

2. Run the build script from the repository root:

```bash
./gradlew assembleRelease
```

3. The APK will be located in:
   `app/build/outputs/apk/release/`

> P.S.: hev_socks5_tunnel will not build on Windows, you will need to use WSL

### Dependencies

- [ByeDPI](https://github.com/hufrea/byedpi)
- [hev-socks5-tunnel](https://github.com/heiher/hev-socks5-tunnel)

### Acknowledgements

- [hufrea](https://github.com/hufrea) — for [ByeDPI](https://github.com/hufrea/byedpi)
- [dovecoteescapee](https://github.com/dovecoteescapee) — for the original [ByeDPIAndroid](https://github.com/dovecoteescapee/ByeDPIAndroid)
