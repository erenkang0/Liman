<div align="center">

# 🌅 Liman

### Profesyonel psikolog defteri + zihnine bir liman

*Psikologlar için **klinik danışan defteri** (SOAP seans notları, randevu, tedavi planı) ile kişisel iyi oluş günlüğünü iç içe getiren uygulama.*

<br/>

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![minSdk](https://img.shields.io/badge/minSdk-26-1E9E6A?style=for-the-badge)

<br/>

```
📓 Defter   ⇄   🌅 Liman
```

</div>

---

## 🌿 İki dünya, tek uygulama

Üstteki **[ Defter | Liman ]** düğmesiyle iki ayrı dünya arasında akıcı geçiş yaparsın:

> 📓 **Psikolog Defteri** *(varsayılan, kilitli)* — profesyonel psikologlar için klinik bir
> defter. Her **danışan** için ayrı bir dosya: başvuru nedeni, **risk düzeyi**, **durum**,
> ilk görüşme, **randevular**, **tedavi hedefleri** ve **SOAP** biçiminde seans notları.
> Tema: **kırmızı / siyah / sepya**, modern bir klinik deri defter.
>
> 🌅 **Liman** — kişisel iyi oluş günlüğü: ruh hali, günlük, araçlar ve içgörü. Sıcak turuncu
> *"zindelik"* teması; akşamları ana ekran saate göre **yıldızlı bir gökyüzüne** bürünür. 🌌

Her danışan dosyası: serbest **etiketler/temalar** (örn. *anksiyete · yas*), **risk düzeyi**
(Yok → Yüksek), **durum** (Aktif / Beklemede / Tamamlandı) ve **kişiye özel renk** ile düzenlenir.

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

## 🆕 Sürüm 2.0.0 yenilikleri

- 🔗 **@bağlantı motoru** — notlarda `@` ile danışan, defter kaydı veya günlüğe canlı bağ kur; tıklayınca o kayda git.
- 🩺 **Profesyonel psikolog araçları** — danışan dosyaları, **SOAP** seans notları (S/O/A/P + süre),
  **randevu** planlama, **tedavi hedefleri** (yapılacaklar) ve **risk/durum** takibi.
- 📊 **Klinik pano** — aktif danışan, bu haftaki randevular, risk takibi, uzun süredir görülmeyenler.
- ⇄ **İki dünya, üstte geçiş** — Defter (kırmızı/siyah/sepya, kilitli) ⇄ Liman (zindelik).
- 💾 **Kalıcı veri** — danışanlar/defterler, ruh hali ve günlük artık cihazda **şifreli** saklanıyor;
  uygulamayı kapatıp açınca **silinmiyor**. *(Daha önce yalnızca bellekteydi.)*
- 🔐 **Defter kilidi** — tüm klinik defter biyometri/PIN arkasında.
- 🎞️ **Daha bol animasyon** — dünyaya özel canlı arka plan (Liman: dalga + yıldız; Defter: kıvılcım +
  parıltı) ve yeni animasyonlu vektör. *"Duygusal hava" kaldırıldı.*

<details><summary>Sürüm 1.5.0 / 1.6.0</summary>

- Tüm emojiler ikonlara dönüştü, kilit ekranı sıfırdan tasarlandı, profil fotoğrafı, arama,
  veri dışa aktarma, haftalık rapor, günlük etiketleri, iletişim ritmi.

</details>

## 🧭 Bilgi mimarisi

İki dünya, üstte **[ Defter | Liman ]** geçişi ve her ekranda davranışı değişen **uyarlanabilir `+` (FAB)**:

**📓 Defter dünyası** *(varsayılan, kilitli — profesyonel/klinik)*

| Sekme | İçerik | FAB |
|:--:|---|---|
| 👥 **Danışanlar** | Danışan dosyaları (risk, durum, sonraki randevu) | Danışan ekle |
| 📖 **Pano** | Bu haftaki randevular, risk takibi, görülmeyenler, seans özeti | — |

> Danışan dosyasında: **SOAP** seans notları, **tedavi hedefleri**, **randevu**, risk/durum, belgeler.

**🌅 Liman dünyası**

| Sekme | İçerik | FAB |
|:--:|---|---|
| ☀️ **Bugün** | Karşılama, hızlı ruh hali, kısayollar | Hızlı-ekle |
| 🔒 **Ben** | Mahrem iç dünya: günlük + araçlar *(kilit rozetli)* | Günlüğe yaz |
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

- **Psikolog Defteri** birincil dünyadır ve **kilitlidir** — uygulamayı açınca önce defter kilidi gelir.
- **Liman** (zindelik) dünyası serbest gezilir; kilit yalnızca **iç dünyaya** (Ben / günlük) girerken devreye girer.
- **Biyometrik + PIN**, ayarlanabilir otomatik kilit (hemen / 1 / 5 / 15 dk).
- Tüm hassas veri yalnızca cihazda; defter ve günlük içerikleri **cihaz içinde şifrelenir**
  (Android Keystore AES/GCM + şifreli yerel depolama).

---

## 🏗️ Mimari & teknoloji

**Kotlin · Jetpack Compose · Material 3 · MVVM (tek yönlü veri akışı) · StateFlow · DataStore · Navigation Compose · Biometric · Android Keystore**

```
app/src/main/java/com/liman/app/
├── data/
│   ├── crypto/        # CryptoManager — Keystore AES/GCM şifreleme
│   ├── local/         # SettingsStore + LimanStore (şifreli kalıcı veri) + Serializers
│   ├── model/         # Mood, Journal, Bonds (Contact/NotebookEntry/Risk/Goal), Profile
│   ├── export/        # Düz metin dışa aktarım
│   └── repository/    # LimanRepository — tek kaynaklı StateFlow + kalıcılık
└── ui/
    ├── theme/         # Renkler, tipografi, LimanTheme + DefterTheme (kırmızı/siyah/sepya)
    ├── components/    # Ortak bileşenler + StarrySky + WorldBackground + AnimatedVectors
    ├── navigation/    # World (Defter/Liman), sekmeler, rotalar
    ├── bonds/         # BondsScreen, ContactProfile (klinik), AddContact, NoteDetail, DefterSummary
    ├── onboarding/ lock/ today/ me/ mood/ insight/ search/ report/ calm/ settings/ tools/
    ├── LimanViewModel.kt   # Tek orkestratör ViewModel
    ├── LimanApp.kt         # Kök gezinme grafiği
    └── MainScaffold.kt     # İki dünyalı kabuk + üst geçiş + uyarlanabilir FAB
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
- [x] Profesyonel psikolog araçları (SOAP seans notu, randevu, tedavi hedefi, risk/durum, pano)
- [x] Cihazda kalıcı + şifreli veri (kişiler/danışanlar, ruh hali, günlük)
- [ ] Room + SQLCipher ile dosya tabanlı şifreli depolama
- [ ] Randevu için takvim/bildirim entegrasyonu

---

<div align="center">

*Bir gün hiçbir şey yazmazsan, seni yargılamayız.* 💛

</div>
