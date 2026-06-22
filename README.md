<div align="center">

# 🌅 Liman

### Zihnine ve bağlarına bir liman

*Bireysel psikolojik iyi oluş ile sosyal ilişkileri dengeli biçimde harmanlayan bir **Zihinsel Sağlık & Sosyal Bağ Günlüğü***

<br/>

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![minSdk](https://img.shields.io/badge/minSdk-26-1E9E6A?style=for-the-badge)

<br/>

```
☀️ Bugün        🔒 Ben        💚 Bağlar        📈 İçgörü
```

</div>

---

## 🌿 Liman nedir?

**Liman**, iki dünyayı net biçimde ayıran ama aralarında akıcı geçiş sunan bir günlük uygulamasıdır:

> 🔒 **Tamamen mahrem, kilitli iç dünya** — ham, savunmasız duygular için güvenli bir sığınak.
>
> 💚 **Sıcak, yapıcı sosyal ilişki günlüğü** — sevdiklerinle bağını canlı tutan nazik bir hatırlatıcı.

Tasarım dili **Material 3 (Material You)**, sıcak **krem yüzeyler**, turuncu *"zindelik"* teması ve sosyal alan için **yeşil aksan** üzerine kuruludur. Akşamları ana ekran, saate göre **yıldızlı bir gökyüzüne** bürünür. 🌌

---

## ✨ Öne çıkanlar

| | |
|---|---|
| 🎨 **Material You** | Dinamik renk, açık/koyu tema, sistem temasını izler |
| 🌅 **Saate duyarlı ambiyans** | Akşam & gece elemanların ardında nazikçe parıldayan yıldızlar + koyu mavi gökyüzü |
| 🗣️ **Cinsiyete duyarlı ton** | Çok nazik dil bazılarına fazla gelebiliyor; "Erkek" seçildiğinde daha sade, yere basan bir üslup |
| 🆘 **Sakinleş (SOS)** | Bunaldığında 5-4-3-2-1 topraklanma, nefes kısayolu ve hızlı destek |
| 🔔 **Bildirimli hatırlatıcılar** | WorkManager ile günlük nazik hatırlatmalar; izin başlangıçta sorulur |
| 🔐 **Akıllı gizlilik** | Kilit yalnızca iç dünyaya (Ben) girerken devreye girer — günlük kullanım sürtünmesiz |
| 👆 **Biyometrik + PIN** | Parmak izi / yüz tanıma ve 4 haneli PIN; ayarlanabilir otomatik kilit |
| 🔏 **Cihazda şifreleme** | Günlük içerikleri Android Keystore (AES/GCM) ile cihaz içinde şifrelenir |
| 🪶 **Nazik mikro etkileşimler** | Dokunuşta yumuşak ölçek, akıcı geçişler, kademeli içerik animasyonları |
| ♿ **Erişilebilirlik** | ≥ 48dp dokunma hedefleri, yeterli kontrast, dinamik yazı boyutu |

---

## 🆕 Sürüm 1.5.0 yenilikleri

- 🎨 **Tüm emojiler ikonlara dönüştü** — ruh hali ve duygusal hava artık tutarlı Material ikonları.
- 🔒 **Kilit ekranı sıfırdan yeniden tasarlandı** — minimal; parmak izi/yüz tuşu doğrudan tuş takımında.
- 🖼️ **Profil fotoğrafı** — Ayarlar'dan seç, Bugün avatarında görün.
- 🔎 **Arama** · 📤 **Veri dışa aktarma** · 📅 **Haftalık rapor** · 🏷️ **Günlük etiketleri** · 💞 **İletişim ritmi**
- 🧼 İlk açılışta örnek/test verisi yok; sıfırdan senin verinle başlar.

## 🧭 Bilgi mimarisi

Alt navigasyonda **4 sekme** ve her ekranda davranışı değişen **uyarlanabilir bir `+` (FAB)**:

| Sekme | İçerik | FAB davranışı |
|:--:|---|---|
| ☀️ **Bugün** | Karşılama, hızlı ruh hali, iç dünya & bağ özetleri | Hızlı-ekle menüsü |
| 🔒 **Ben** | Mahrem iç dünya: günlük + araçlar *(kilit rozetli)* | Kalem / günlüğe yaz |
| 💚 **Bağlar** | İlişki günlüğü *(kilitsiz)* | Kişi ekle *(yeşil)* |
| 📈 **İçgörü** | İstatistik & eğilimler | Paylaş |

> FAB'in ikonu, rengi ve köşe yarıçapı bulunduğun ekrana göre yumuşakça değişir.

---

## 🧰 Ben sekmesi araçları

<div align="center">

| 🌬️ Nefes & Topraklanma | 🧠 Düşünce Kaydı | 🙏 Şükran Defteri | ⏳ Zaman Kapsülü | 📅 Ruh Hali Takvimi |
|:--:|:--:|:--:|:--:|:--:|
| Ritimli evre animasyonu (4-7-8, kutu, sakinleştirici) | 5 adımlı BDT yeniden çerçeveleme | Yönlendirmeli 3 kart — **seri yok, baskı yok** | Yaz → mühürle, gelecekte aç | Yüz ifadeli aylık görünüm + ayın özeti |

</div>

---

## 🔐 Kilit & gizlilik stratejisi

- Uygulama **serbest açılır**; **Bağlar** alanı kilitsizdir.
- Kilit yalnızca **iç dünyaya** (Ben / günlük) girerken devreye girer — böylece ham duygular korunur, günlük kullanım akıcı kalır.
- İstersen Ayarlar'dan kilidi **"uygulama açılışında"** olacak şekilde değiştirebilirsin.
- **Biyometrik + PIN**, ayarlanabilir otomatik kilit (hemen / 1 / 5 / 15 dk).
- Tüm hassas veri cihazda; günlük içerikleri **uçtan uca (cihaz içi) şifreli**.

---

## 🏗️ Mimari & teknoloji

**Kotlin · Jetpack Compose · Material 3 · MVVM (tek yönlü veri akışı) · StateFlow · DataStore · Navigation Compose · Biometric · Android Keystore**

```
app/src/main/java/com/liman/app/
├── data/
│   ├── crypto/        # CryptoManager — Keystore AES/GCM (günlük şifreleme)
│   ├── local/         # SettingsStore — DataStore (ayar kalıcılığı)
│   ├── model/         # Alan modelleri (Mood, Journal, Bonds, Tools, Profile)
│   └── repository/    # LimanRepository — tek kaynaklı StateFlow deposu
└── ui/
    ├── theme/         # Renkler, tipografi, şekiller, tema (gece paleti dâhil)
    ├── components/    # Paylaşılan bileşenler + StarrySky (yıldızlı gökyüzü)
    ├── navigation/    # Rotalar & sekmeler
    ├── onboarding/ lock/ today/ me/ mood/ bonds/ insight/ settings/ tools/
    ├── LimanViewModel.kt   # Tek orkestratör ViewModel
    ├── LimanApp.kt         # Kök gezinme grafiği
    └── MainScaffold.kt     # Alt navigasyon + uyarlanabilir FAB + ambiyans
```

---

## 🚀 Çalıştırma

```bash
# Depoyu klonla
git clone https://github.com/erenkang0/liman.git
cd liman

# Debug APK derle
./gradlew assembleDebug

# Bir cihaza/emülatöre kur
./gradlew installDebug
```

> Android Studio (Koala+) ile açıp doğrudan **Run** demek de yeterlidir.

---

## 📦 Sürüm (release)

- Her push'ta **Android CI** debug APK'yı derleyip artefakt olarak yükler.
- `v*` etiketli bir push (ör. `v1.0.0`) **Release** akışını tetikler: release APK derlenir ve **GitHub Release** olarak yayınlanır.

```bash
git tag v1.0.0
git push origin v1.0.0   # → GitHub Release + APK
```

---

## 🗺️ Yol haritası

- [x] Gerçek bildirim planlaması (WorkManager) + başlangıçta izin
- [x] Cinsiyete duyarlı yazı tonu
- [x] Sakinleş (SOS / topraklanma) modu
- [x] Arama, dışa aktarma, haftalık rapor, günlük etiketleri, iletişim ritmi
- [x] Zengin metin günlük + sesli not + afiş fotoğraf
- [ ] Room + SQLCipher ile kalıcı şifreli depolama
- [ ] Takvim entegrasyonu

---

<div align="center">

*Bir gün hiçbir şey yazmazsan, seni yargılamayız.* 💛

</div>
