# ArdoTv Hub (televizor + telefon)

## 1. Firebase'ni sozlash (hisob/profil ishlashi uchun, bir marta)
1. https://console.firebase.google.com → **Add project** → nom bering (masalan `ArdoTvHub`).
2. **Build → Authentication → Get started → Sign-in method → Email/Password → Enable → Save**.
3. **Project settings (⚙️) → General → Web API Key** ni nusxa oling.
4. `app/src/main/java/uz/ardo/tvhub/AuthApi.kt` faylida
   `const val API_KEY = "PASTE_FIREBASE_WEB_API_KEY"` o'rniga kalitni qo'ying.

Telefonda ochilgan hisob televizorda ham ishlaydi (bitta Firebase bazasi).
Televizorda faqat **Kirish**, telefonda **Kirish + Ro'yxatdan o'tish**.

## 2. GitHub'da APK yig'ish
Papka tuzilishi aynan shunday bo'lsin (ayniqsa `.github` va `.gitignore`):
```
.github/workflows/build.yml
.gitignore
build.gradle.kts
settings.gradle.kts
gradle.properties
app/build.gradle.kts
app/src/main/AndroidManifest.xml
app/src/main/assets/apps.json
app/src/main/java/uz/ardo/tvhub/*.kt
app/src/main/res/values/{strings,themes,colors}.xml
app/src/main/res/drawable/{bg_gradient.xml,banner.png}
app/src/main/res/mipmap-xxxhdpi/ic_launcher.png
```
1. GitHub'da yangi repozitoriy oching → **uploading an existing file** emas, quyidagini qiling:
   kompyuterda papkani oching, barcha fayllarni (yashirin `.github` bilan) sudrab tashlang.
   Telefondan yuklash noqulay — kompyuter yoki `git` ishlating.
2. **Actions** bo'limi → "APK yig'ish" → yashil ✔ bo'lguncha kuting (3–5 daqiqa).
3. Ish sahifasi pastidagi **Artifacts → ArdoTvHub-apk** ni yuklang, zip ichida `app-debug.apk`.
4. APK ni telefon va televizorga o'rnating (Noma'lum manbalarga ruxsat bering).

## 3. Ilovalar ro'yxati
`app/src/main/assets/apps.json`: `name`, `packages` (bir nechta variant), `color`, `url`, `action`.
- Paket bor, o'rnatilmagan → Play Market ochiladi.
- Paket yo'q, `url` bor → havola ochiladi.
- Paket ham, `url` ham yo'q → Play Market'da nom bo'yicha qidiriladi.
- `action`: `profile`, `help`, `wifi`, `settings`.

"Yordam" kartochkasi https://ardosher.uz/ArdoTvHub ni ochadi (brauzer bo'lmasa, ilova ichida).
