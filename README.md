# Ev Analiz - Gayrimenkul Yatırım Analiz Uygulaması

Android uygulaması - Ekrandaki ev fiyatı ve kira bilgilerini okuyarak yatırım mantıklı mı değerlendirir.

## Özellikler

- 🏠 Ekrandan otomatik fiyat/kira algılama (Accessibility)
- 💰 PMT formülü ile kredi hesaplama
- 📊 NPV, IRR, Payback period analizi
- 🎯 4 farklı senaryo (İyimser → Stres)
- ✅ MANTIKLI / ❌ MANTIKSIZ kararı

## Mimari

```
DOMAIN A (Android)          DOMAIN B (Fintech)
────────────────────        ──────────────────────
Floating UI                 Calculation Engine
Accessibility Service  ────▶ Verdict Engine
Integration Bridge          Scenario Analysis
```

## Kurulum

1. Android Studio'da projeyi aç
2. `local.properties` dosyasında SDK yolunu ayarla
3. Sync & Build
4. Cihaza/emülatöre yükle

## Kullanım

1. Uygulamayı başlat
2. İzinleri ver (Overlay + Accessibility)
3. Herhangi bir emlak ilanına git
4. Floating butona tıkla
5. Sonucu gör

## Lisans

MIT
