# 🛍️ Sellify

**Sellify** — bu Telegram orqali ishlovchi **onlayn bozor** bot va backend xizmati.  
Foydalanuvchilar mahsulotlarni ko‘rishlari, savatga qo‘shishlari va buyurtma qilishlari mumkin.  
Adminlar esa mahsulotlarni qo‘shish, o‘chirish va boshqarish imkoniyatiga ega.

---

## ✨ Imkoniyatlar

- 📦 **Mahsulot katalogi** (kategoriya, rasm, narx, tavsif)
- 🔎 **Qidiruv va filtrlash**
- 🛒 **Savat va buyurtma tizimi**
- 👨‍💼 **Admin boshqaruvi** (bot orqali CRUD)
- 🖼️ **Bir nechta rasm qo‘llab-quvvatlash**
- 🌐 **REST API + Swagger UI** (integratsiya uchun)

---

## 🧰 Texnologiyalar

- **Java 17+**, **Spring Boot 3**
- **PostgreSQL** (ma’lumotlar bazasi)
- **Hibernate / JPA**
- **Telegram Bots Java**
- **Swagger / OpenAPI**

---

## 🚀 O‘rnatish

### 1. Muhit sozlamalari

`application.yml` yoki `.env` faylida quyidagilarni belgilang:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/sellify
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: update
    open-in-view: false

telegram:
  bot:
    token: 123456:ABCDEF...   # @BotFather dan olingan token
    username: sellify_bot

files:
  storage-root: ./storage
