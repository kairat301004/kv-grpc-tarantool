# KV gRPC Service for Tarantool

gRPC сервис для key-value хранилища на базе Tarantool 3.2.x.

## Реализованные методы

| Метод | Описание |
|-------|----------|
| `put(key, value)` | Сохраняет значение для ключа (перезаписывает существующее) |
| `get(key)` | Возвращает значение по ключу |
| `delete(key)` | Удаляет ключ |
| `range(from, to)` | Возвращает stream пар ключ-значение в диапазоне |
| `count()` | Возвращает количество записей |

## Технологии

- Java 11
- gRPC 1.62.2
- Tarantool 3.2.x
- tarantool-client 1.5.0

## Схема БД

```lua
{
    {name = 'key', type = 'string'},
    {name = 'value', type = 'varbinary', is_nullable = true}
}
```
## Запуск
### 1. Запустить Tarantool
```bash
docker-compose up -d
```
### 2. Собрать проект
```bash
mvn clean package
```
### 3. Запустить сервер
```bash
java -jar target/kv-grpc-tarantool-1.0.0.jar
```
## Тестирование (grpcurl)
```bash
# Put
grpcurl -plaintext -d '{"key":"user1","value":"dGVzdA=="}' localhost:9090 KvService/put

# Get
grpcurl -plaintext -d '{"key":"user1"}' localhost:9090 KvService/get

# Count
grpcurl -plaintext -d '{}' localhost:9090 KvService/count

# Range
grpcurl -plaintext -d '{"from":"a","to":"z"}' localhost:9090 KvService/range

# Delete
grpcurl -plaintext -d '{"key":"user1"}' localhost:9090 KvService/delete
```