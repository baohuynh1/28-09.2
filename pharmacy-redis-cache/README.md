# Pharmacy – Redis Cache cho chi tiết thuốc

- `@Cacheable(value="medicines", key="#id")` – lấy chi tiết thuốc
- `@CacheEvict(value="medicines", key="#medicine.id")` – cập nhật thuốc / giá → xoá cache ngay
- TTL cache `medicines` = **60 phút** (`app.cache.medicines-ttl=60m`)
- Key trong Redis: `medicines::<id>`, value dạng JSON

## Chạy
```bash
docker compose up -d          # PostgreSQL + Redis
mvn spring-boot:run           # lần đầu tự sinh 5.000 thuốc mẫu
redis-cli FLUSHALL            # làm sạch cache trước khi thực hành
```

## Thực hành 4 bước

**Bước 1 – Lần đầu (Cache MISS, chậm):**
```bash
curl -i http://localhost:8080/api/medicines/1
```
Log: `Cache MISS ...` + `Hibernate: select ... from medicines ... where m1_0.id=?`

**Bước 2 – Gọi lại (Cache HIT, cực nhanh):**
```bash
curl -i http://localhost:8080/api/medicines/1
redis-cli KEYS "*"                 # -> "medicines::1"
redis-cli TTL  "medicines::1"      # -> khoảng 3600 (giây)
```
Log: `Cache entry for key '1' found in cache(s) [medicines]`, **không có SQL**, `X-Response-Time-ms` < 10.

**Bước 3 – Sửa giá thuốc ID=1 (xoá cache):**
```bash
curl -X PATCH http://localhost:8080/api/medicines/1/price \
     -H "Content-Type: application/json" -d '{"price": 99000}'
# hoặc: curl -X PUT http://localhost:8080/api/medicines/1 -H "Content-Type: application/json" -d '{"price": 99000}'

redis-cli KEYS "*"                 # -> (empty array): key medicines::1 đã biến mất
```
Log: `Invalidating cache key [1] for operation ... CacheEvictOperation` và `Đã cập nhật thuốc id = 1 (giá mới: 99000) -> XOÁ cache medicines::1`

**Bước 4 – Gọi lại (Cache MISS, nạp giá mới):**
```bash
curl http://localhost:8080/api/medicines/1     # "price": 99000
redis-cli KEYS "*"                             # -> "medicines::1" xuất hiện lại
```
Log: SQL `select ... from medicines` **xuất hiện lại** → hệ thống nhận ra cache cũ đã bị xoá và nạp dữ liệu mới nhất từ DB.

## Đổi sang MySQL
Trong `pom.xml` thay driver `postgresql` bằng `mysql-connector-j`, đổi `spring.datasource.url`.
