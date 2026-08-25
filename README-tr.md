<div align="center">
  <p>
    <img src="https://github.com/andreibioazum-cell/RayKos/raw/master/.github/images/app.svg" alt="RayKos logosu" width="200" />
  </p>
  <h1>RayKos</h1>
  <p>
    <a href="README.md">Русский</a> |
    <a href="README-en.md">English</a> |
    Türkçe
  </p>
  <p>
    <a href="https://github.com/andreibioazum-cell/RayKos/releases/latest"><img src="https://img.shields.io/github/v/release/andreibioazum-cell/RayKos" alt="Latest Release" /></a>
    <a href="https://github.com/andreibioazum-cell/RayKos/releases"><img src="https://img.shields.io/github/downloads/andreibioazum-cell/RayKos/total" alt="Downloads" /></a>
    <a href="https://github.com/andreibioazum-cell/RayKos/blob/master/LICENSE"><img src="https://img.shields.io/github/license/andreibioazum-cell/RayKos" alt="License" /></a>
    <a href="https://github.com/andreibioazum-cell/RayKos"><img src="https://img.shields.io/github/languages/code-size/andreibioazum-cell/RayKos" alt="GitHub code size in bytes"/></a>
  </p>
</div>

ByeDPI'yi yerel olarak çalıştıran ve tüm trafiği üzerinden yönlendiren bir Android uygulaması.

RayKos'ta ayar, düzenleyici veya strateji seçimi yok: en iyi atlatma parametreleri uygulamanın içine gömülü, bu yüzden sadece düğmeye basmanız yeterli.

Bu uygulama bir VPN değildir. Trafiği yönlendirmek için Android'in VPN modunu kullanır, ancak hiçbir veriyi uzak bir sunucuya göndermez. Trafiği şifrelemez ve IP adresinizi gizlemez.

---

### Özellikler

* En iyi atlatma parametreleri gömülü — yapılandırılacak hiçbir şey yok
* Cihaz açıldığında hizmeti otomatik başlatma
* Android TV/BOX ile geliştirilmiş uyumluluk
* 127.0.0.1:1080 adresindeki SOCKS proxy her zaman kullanılabilir (ör. SmartTube için)

### Kullanım

* Ana ekrandaki büyük düğmeye basın.
* İzin isteğini kabul etmek için VPN bağlantısını en az bir kez başlatmanız önerilir.
* Bundan sonra uygulama, cihaz açıldığında hizmeti otomatik olarak başlatır.
* SmartTube için bağlantı hemen çalışır; dilerse SOCKS proxy 127.0.0.1:1080 ayarlayabilirsiniz.

### Derleme

1. Depoyu alt modüllerle birlikte klonlayın:

```bash
git clone --recurse-submodules
```

2. Derleme betiğini deponun kök dizininden çalıştırın:

```bash
./gradlew assembleRelease
```

3. APK dosyası şu dizinde bulunacaktır:
   `app/build/outputs/apk/release/`

> P.S.: hev_socks5_tunnel Windows'ta derlenmez, WSL kullanmanız gerekir

### Bağımlılıklar

- [ByeDPI](https://github.com/hufrea/byedpi)
- [hev-socks5-tunnel](https://github.com/heiher/hev-socks5-tunnel)

### Teşekkürler

- [hufrea](https://github.com/hufrea) — [ByeDPI](https://github.com/hufrea/byedpi) için
- [dovecoteescapee](https://github.com/dovecoteescapee) — orijinal [ByeDPIAndroid](https://github.com/dovecoteescapee/ByeDPIAndroid) uygulaması için
