# Ticket Management System - Proje Dokumantasyonu

## Proje Amaci

Bu proje, rol bazli yetkilendirme ve service katmaninda uygulanan is kurallari iceren bir Mini Ticket Management System uygulamasidir. Sistem sadece CRUD islemlerinden olusmaz; ticket sahipligi, status gecisleri, assigned kullanici yetkisi, comment erisimi ve admin yetkileri gibi kurallari uygular.

## Kullanilan Teknolojiler

- Java 17
- Spring Boot 3.3
- Spring Security
- Spring Data JPA
- JWT
- H2 file database
- JUnit 5 + Mockito
- Saf HTML/CSS/JavaScript frontend

## Katmanli Mimari

- `controller`: REST endpoint'leri ve HTTP status cevaplari.
- `service`: Is kurallari ve yetki kontrolleri.
- `repository`: Spring Data JPA ile veritabani erisimi.
- `dto`: Request/response modelleri ve validation kurallari.
- `model/entity`: Veritabani tablolarinin JPA karsiliklari.
- `security`: JWT uretimi, token dogrulama ve Spring Security ayarlari.
- `exception`: Global exception handling.

## Roller ve Yetkiler

### ADMIN

- Tum ticket'lari goruntuleyebilir.
- Tum ticket'lari silebilir.
- Kullanicilari listeleyebilir.
- Audit log kayitlarini goruntuleyebilir.
- Tum dashboard verisini gorebilir.

### USER

- Ticket olusturabilir.
- Sadece kendi olusturdugu ticket'i guncelleyebilir.
- Sadece kendisine atanan ticket'in status bilgisini degistirebilir.
- Sadece kendi olusturdugu veya kendisine atanan ticket'larda yorum goruntuleyebilir/ekleyebilir.
- Dashboard'da sadece kendi olusturdugu veya kendisine atanan ticket verilerini gorur.

## Ticket Is Kurallari

1. Ticket olusturuldugunda status otomatik `OPEN` olur.
2. Sadece ticket'i olusturan kullanici ticket'i guncelleyebilir.
3. Sadece `ADMIN` ticket silebilir.
4. Status gecisleri sadece `OPEN -> IN_PROGRESS -> DONE` seklindedir.
5. Sadece assigned kullanici status degistirebilir.
6. `createdBy` alani degistirilemez.

Bu kurallar controller katmaninda degil, `TicketServiceImpl` icinde uygulanir.

## Comment Sistemi

- Yorum bos olamaz.
- Yorumlar ilgili ticket altinda listelenir.
- Comment erisimi ticket goruntuleme kuraliyla aynidir:
  - ADMIN tum ticket yorumlarina erisebilir.
  - USER sadece kendi olusturdugu veya kendisine atanan ticket yorumlarina erisebilir.

## Dashboard

Dashboard asagidaki bilgileri gosterir:

- Toplam ticket sayisi
- Status bazli ticket sayilari
- Son 5 olusturulan ticket

ADMIN tum sistem verilerini gorur. USER sadece kendi gorebildigi ticket'larin dashboard bilgisini gorur.

## Veritabani

Veritabani olarak H2 kullanilmistir. Uygulama varsayilan olarak dosya tabanli H2 ile calisir:

```properties
spring.datasource.url=jdbc:h2:file:./data/ticketdb
```

Bu sayede veriler uygulama kapaninca kaybolmaz. Local veritabani dosyalari `data/` klasorunde olusur ve `.gitignore` ile GitHub'a yuklenmez.

Istenirse `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` ortam degiskenleriyle MySQL/PostgreSQL gibi baska bir veritabanina gecilebilir.

## Guvenlik

- Register yeni USER kullanici olusturur, otomatik oturum acmaz.
- Login JWT tabanlidir ve basarili giriste token uretir.
- Sifreler BCrypt ile hashlenir.
- JWT secret ortam degiskeniyle verilebilir: `JWT_SECRET`.
- H2 console varsayilan olarak kapali gelir. Gerekirse local ortamda `H2_CONSOLE_ENABLED=true` ile acilabilir.
- ADMIN endpoint'leri Spring Security ile korunur.

## Testler

Unit testler service katmanindaki is kurallarini dogrular:

- Ticket olusturma
- Status gecis kurali
- Yetkisiz guncelleme
- Silme yetki kontrolu
- Comment erisim kontrolu
- Role bazli dashboard filtreleme

Calistirma:

```bash
mvn test
```
