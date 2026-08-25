<div align="center">
  <p>
    <img src="https://github.com/andreibioazum-cell/RayKos/raw/master/.github/images/app.svg" alt="Логотип RayKos" width="200" />
  </p>
  <h1>RayKos</h1>
  <p>
    Русский |
    <a href="README-en.md">English</a> |
    <a href="README-tr.md">Türkçe</a>
  </p>
  <p>
    <a href="https://github.com/andreibioazum-cell/RayKos/releases/latest"><img src="https://img.shields.io/github/v/release/andreibioazum-cell/RayKos" alt="Latest Release" /></a>
    <a href="https://github.com/andreibioazum-cell/RayKos/releases"><img src="https://img.shields.io/github/downloads/andreibioazum-cell/RayKos/total" alt="Downloads" /></a>
    <a href="https://github.com/andreibioazum-cell/RayKos/blob/master/LICENSE"><img src="https://img.shields.io/github/license/andreibioazum-cell/RayKos" alt="License" /></a>
    <a href="https://github.com/andreibioazum-cell/RayKos"><img src="https://img.shields.io/github/languages/code-size/andreibioazum-cell/RayKos" alt="GitHub code size in bytes"/></a>
  </p>
</div>

Приложение для Android, которое локально запускает ByeDPI и перенаправляет весь трафик через него.

В RayKos нет настроек, редакторов и подбора стратегий: лучшие параметры обхода встроены в приложение, поэтому нужно просто нажать кнопку.

Приложение не является VPN. Оно использует VPN-режим на Android для перенаправления трафика, но не передает ничего на удаленный сервер. Оно не шифрует трафик и не скрывает ваш IP-адрес.

---

### Возможности
* Лучшие параметры обхода встроены — настраивать ничего не нужно
* Автозапуск сервиса при старте устройства
* Улучшена совместимость с Android TV/BOX
* SOCKS-прокси на 127.0.0.1:1080 всегда доступно (например, для SmartTube)

### Использование
* Нажмите большую кнопку на главном экране.
* Рекомендуется подключиться один раз к VPN, чтобы принять запрос.
* После этого, при загрузке устройства, приложение автоматически запустит сервис.
* Для SmartTube подключение работает сразу, либо задайте SOCKS-прокси 127.0.0.1:1080.

### Сборка
1. Клонируйте репозиторий с сабмодулями:
```bash
git clone --recurse-submodules
```
2. Запустите скрипт сборки из корня репозитория:
```bash
./gradlew assembleRelease
```
3. APK будет в `app/build/outputs/apk/release/`

> P.S.: hev_socks5_tunnel не соберется под Windows, вам нужно будет использовать WSL

### Зависимости
- [ByeDPI](https://github.com/hufrea/byedpi)
- [hev-socks5-tunnel](https://github.com/heiher/hev-socks5-tunnel)

### Благодарность
- [hufrea](https://github.com/hufrea) - за [ByeDPI](https://github.com/hufrea/byedpi)
- [dovecoteescapee](https://github.com/dovecoteescapee) - за изначальную реализацию [ByeDPIAndroid](https://github.com/dovecoteescapee/ByeDPIAndroid)
