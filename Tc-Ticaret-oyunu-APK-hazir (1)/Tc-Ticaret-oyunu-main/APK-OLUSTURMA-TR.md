# TC Ticaret Oyunu – APK oluşturma

Bu projeye GitHub Actions ile otomatik Android APK derleme iş akışı eklendi.

## Kullanım
1. Bu klasördeki tüm proje dosyalarını GitHub deposuna yükleyin (mevcut deponuzdaki dosyalarla güncelleyin).
2. GitHub deposunda **Actions** sekmesini açın.
3. **Android APK** iş akışını seçin.
4. **Run workflow** düğmesine basın. (Ana dala gönderim yapıldığında da otomatik çalışır.)
5. İşlem tamamlanınca çalışmanın altındaki **Artifacts** bölümünden **Tc-Ticaret-APK** dosyasını indirin.
6. İnen ZIP dosyasının içindeki `app-debug.apk`, telefona kurulacak tek APK dosyasıdır.

## Notlar
- Bu, test/kurulum amaçlı imzalanmış debug APK üretir; Play Store yayını için ayrı bir release imzalama anahtarı gerekir.
- Firebase/Google giriş gibi çevrimiçi özellikler, projeye ait geçerli Firebase yapılandırması ve servis ayarları gerektirebilir.
