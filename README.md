# Ticket Management System

Rol bazlı yetkilendirme ve iş kuralları içeren Ticket Yönetim Sistemi (Spring Boot + Spring Security + Spring Data JPA + JWT).

## Teknolojiler

- Java 17
- Spring Boot 3.3
- Spring Security (JWT tabanlı authentication)
- Spring Data JPA
- H2 (in-memory veritabanı — kurulum gerektirmez)
- JUnit 5 + Mockito

## Çalıştırma

```bash
mvn spring-boot:run
```

## Guvenli Calisma Notlari

- Veritabani varsayilan olarak dosya tabanli H2 ile calisir ve `data/` klasorunde kalici tutulur.
- H2 console varsayilan olarak kapali gelir. Local inceleme icin `H2_CONSOLE_ENABLED=true` ile acilabilir.
- JWT secret production ortaminda ortam degiskeniyle verilmelidir: `JWT_SECRET`.
- Veritabani baglantisi `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` ile degistirilebilir.

## Teslim Dokumanlari

Ek dokumanlar `docs/` klasorundedir:

- `docs/PROJECT_DOCUMENTATION.md`
- `docs/PRESENTATION.md`
- `docs/DAILY_REPORT_SAMPLE.md`

Uygulama `http://localhost:8080` üzerinde çalışır.

Testler:

```bash
mvn test
```

## Varsayılan Admin

Uygulama açılırken otomatik oluşturulur:

- Kullanıcı adı: `admin`
- Şifre: `admin123`

Register endpoint'i her zaman `USER` rolünde kullanıcı oluşturur.

## Endpoint'ler

### Auth (herkese açık)

| Metot | Endpoint | Açıklama |
|---|---|---|
| POST | `/api/auth/register` | Kayıt (USER rolü) — 201, otomatik giriş yapmaz |
| POST | `/api/auth/login` | Giriş, JWT token döner |

Diğer tüm endpoint'ler `Authorization: Bearer <token>` header'ı ister.

### Ticket

| Metot | Endpoint | Yetki | Açıklama |
|---|---|---|---|
| POST | `/api/tickets` | USER/ADMIN | Ticket oluşturma (status otomatik OPEN) — 201 |
| GET | `/api/tickets?status=&priority=&assignedToId=&page=0&size=10` | USER/ADMIN | Listeleme (pagination + filtre). ADMIN hepsini, USER kendi oluşturduğu/atandığı ticket'ları görür |
| GET | `/api/tickets/{id}` | USER/ADMIN | Tek ticket |
| PUT | `/api/tickets/{id}` | Sadece oluşturan | Güncelleme (createdBy değiştirilemez) |
| PATCH | `/api/tickets/{id}/status` | Sadece atanan kullanıcı | Status değişikliği (OPEN → IN_PROGRESS → DONE) |
| DELETE | `/api/tickets/{id}` | Sadece ADMIN | Silme — 204 |

### Comment

| Metot | Endpoint | Açıklama |
|---|---|---|
| POST | `/api/tickets/{ticketId}/comments` | Yorum ekleme (boş olamaz) — 201 |
| GET | `/api/tickets/{ticketId}/comments` | Ticket'ın yorumları |

### Dashboard

| Metot | Endpoint | Açıklama |
|---|---|---|
| GET | `/api/dashboard` | Toplam ticket sayısı, status bazlı sayılar, son 5 ticket |

### Admin

| Metot | Endpoint | Açıklama |
|---|---|---|
| GET | `/api/users` | Kullanıcı listesi (ADMIN) |
| GET | `/api/audit-logs` | Audit log kayıtları (ADMIN, bonus) |

## İş Kuralları (service katmanında)

1. Ticket oluşturulduğunda status otomatik `OPEN` olur.
2. Sadece ticket'ı oluşturan kullanıcı ticket'ı güncelleyebilir → aksi halde **403**.
3. Sadece ADMIN ticket silebilir → aksi halde **403**.
4. Status geçişleri yalnızca `OPEN → IN_PROGRESS → DONE`; `DONE` geri `OPEN` yapılamaz → aksi halde **400**.
5. Sadece assigned kullanıcı status değiştirebilir → aksi halde **403**.
6. `createdBy` alanı değiştirilemez (DTO'da yok + entity'de `updatable = false`).

## Hata Yönetimi

Global exception handler (`@RestControllerAdvice`) ile:

- 400 — validation hatası / geçersiz status geçişi
- 401 — hatalı giriş bilgileri
- 403 — yetkisiz işlem
- 404 — kaynak bulunamadı

## Bonus: Audit Log

Ticket oluşturma, güncelleme, silme, status değişikliği ve login işlemleri `audit_logs` tablosuna kaydedilir.

## Proje Yapısı

```
src/main/java/com/ticketmanagement/
├── config/          SecurityConfig, DataInitializer
├── controller/      Auth, Ticket, Comment, User, Dashboard, AuditLog
├── dto/
│   ├── request/     Register, Login, TicketCreate, TicketUpdate, StatusUpdate, Comment
│   └── response/    Auth, Register, Ticket, Comment, User, Dashboard, Page
├── exception/       GlobalExceptionHandler + özel exception'lar
├── model/
│   ├── entity/      User, Ticket, Comment, AuditLog
│   └── enums/       Role, TicketStatus, TicketPriority, AuditAction
├── repository/      User, Ticket, Comment, AuditLog
├── security/        JwtUtil, JwtAuthenticationFilter, CustomUserDetailsService
└── service/
    └── impl/        İş kurallarının uygulandığı katman

src/test/java/com/ticketmanagement/
└── service/         TicketServiceImplTest (zorunlu unit test senaryoları)
```
