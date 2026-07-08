# Ticket Management System - Sunum Taslagi

## 1. Proje Ozeti

Rol bazli yetkilendirme, JWT authentication, ticket is kurallari, yorum sistemi ve dashboard iceren Spring Boot tabanli Ticket Management System gelistirildi.

## 2. Teknik Stack

- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- JWT
- H2 file database
- HTML/CSS/JavaScript frontend

## 3. Roller

ADMIN:
- Tum ticket'lari gorur.
- Tum ticket'lari siler.
- Kullanicilari listeler.

USER:
- Ticket olusturur.
- Kendi ticket'ini gunceller.
- Kendisine atanan ticket'in status bilgisini degistirir.
- Yetkili oldugu ticket'lara yorum ekler.

## 4. Is Kurallari

- Yeni ticket status'u otomatik `OPEN` olur.
- Sadece olusturan kullanici ticket gunceller.
- Sadece ADMIN ticket siler.
- Status sadece `OPEN -> IN_PROGRESS -> DONE` akar.
- Sadece assigned kullanici status degistirir.
- `createdBy` degistirilemez.

## 5. Mimari

Controller, Service, Repository ayrimi uygulanmistir. Is kurallari service katmanindadir. Entity'ler dogrudan disari acilmaz; request/response DTO'lari kullanilir.

## 6. Guvenlik

- JWT tabanli stateless authentication
- BCrypt password hashing
- Role-based authorization
- Global exception handling
- Comment ve dashboard tarafinda kullanici bazli veri erisim kontrolu

## 7. Bonus Ozellikler

- Audit log sistemi
- Responsive web UI
- Detayli admin kullanici listesi

## 8. Testler

Service katmaninda is kurallarini dogrulayan unit testler yazildi ve `mvn test` ile basarili calisiyor.

## 9. Demo Akisi

1. Admin login: `admin / admin123`
2. Yeni USER kaydi
3. USER ile ticket olusturma
4. USER ile ticket guncelleme ve yorum ekleme
5. Assigned kullanici ile status gecisi
6. ADMIN ile ticket silme ve kullanici listesini goruntuleme
7. Dashboard ve audit log ekranlarini inceleme

