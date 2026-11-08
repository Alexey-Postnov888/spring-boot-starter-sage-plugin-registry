# Spring Boot Starter для Sage Plugin Registry

Spring Boot Starter для интеграции Kotlin- и Java-приложений с Sage Plugin Registry.

Starter автоматически регистрирует экземпляры плагинов, отправляет heartbeat и снимает регистрацию при завершении работы приложения.

## Возможности

- Автоматическая регистрация плагина при запуске.
- Формирование манифеста из аннотаций и YAML-конфигурации.
- Периодическая отправка heartbeat.
- Автоматическая отправка запроса на снятие регистрации при остановке приложения.

## Требования

- Java 21+
- Spring Boot 4+

## Установка

Добавьте зависимость:

build.gradle.kts:

```kotlin
dependencies {
    implementation("ru.alexeypostnov:spring-boot-starter-sage-plugin-registry:0.1.0")
}
```

## Быстрый старт

### 1. Опишите плагин

Для описания плагина можно использовать аннотации `@PluginDefinition` и `@PluginCapability`:

```kotlin
@Service
@PluginDefinition(
    id = "weather-plugin",
    name = "Weather Plugin",
    version = "1.0.0",
    description = "Provides weather information"
)
class WeatherPlugin {

    @PluginCapability(
        id = "weather.current",
        description = "Get current weather"
    )
    fun getCurrentWeather() {
        // Command implementation
    }
}
```

В примере обе аннотации находятся в одном классе, но их можно размещать отдельно в разных Spring-компонентах.

Аннотации необязательны: метаданные плагина и список его capabilities можно полностью определить в `application.yaml` или комбинировать с аннотациями.

### 2. Настройте подключение к Registry

```yaml
sage:
  plugin-registry:
    url: http://localhost:8080
    manifest:
      spec:
        endpoint: /api/v1/commands
```

`url` - адрес вашего инстанса Sage Plugin Registry.

`manifest.spec.endpoint` - endpoint плагина, на который Command Service будет отправлять команды.

### 3. Запустите приложение

При стандартных настройках Starter автоматически:
1. Сформирует манифест плагина.
2. Зарегистрирует экземпляр в Registry.
3. Начнёт отправлять heartbeat.

Дополнительная реализация регистрации и управления жизненным циклом не требуется.

## Конфигурация

Все параметры находятся под префиксом `sage.plugin-registry`.

| Параметр                           | По умолчанию | Описание                                                                                 |
|------------------------------------|--------------|------------------------------------------------------------------------------------------|
| `enabled`                          | `true`       | Включает или отключает Starter                                                           |
| `url`                              | -            | URL Plugin Registry. Обязателен при `registration.enabled = true`                        |
| `connection-timeout`               | `3s`         | Таймаут подключения                                                                      |
| `read-timeout`                     | `5s`         | Таймаут ожидания ответа                                                                  |
| `registration.enabled`             | `true`       | Включает регистрацию                                                                     |
| `registration.fail-fast`           | `false`      | `false` - запуск продолжается, если регистрация не удалась; `true` - запуск прерывается. |
| `registration.retry.max-attempts`  | `3`          | Максимальное количество попыток регистрации                                              |
| `registration.retry.initial-delay` | `1s`         | Начальная задержка перед повторной попыткой                                              |
| `registration.retry.multiplier`    | `2.0`        | Множитель задержки между попытками                                                       |
| `heartbeat.enabled`                | `true`       | Включает отправку heartbeat                                                              |
| `heartbeat.interval`               | `15s`        | Интервал отправки heartbeat                                                              |

### Манифест плагина

Параметры манифеста находятся под префиксом `sage.plugin-registry.manifest` и позволяют настроить метаданные плагина, его endpoint и список возможностей.

Если аннотации не используются, манифест можно полностью описать в YAML:

```yaml
sage:
  plugin-registry:
    manifest:
      manifest-version: v1
      type: Plugin
      info:
        id: weather-plugin
        name: Weather Plugin
        version: 1.0.0
        description: Provides weather information
      spec:
        endpoint: /api/v1/commands
        capabilities:
          - id: weather.current
            description: Get current weather
```

Аннотации и YAML можно использовать совместно:

- Значения `@PluginDefinition` имеют приоритет над соответствующими значениями YAML.
- Capabilities из `@PluginCapability` и YAML объединяются.
- Повторяющиеся ID capabilities приводят к ошибке валидации.

### Жизненный цикл регистрации

- **Запуск:** регистрация экземпляра в Registry.
- **Работа:** периодическая отправка heartbeat.
- **Завершение:** отправка запроса на снятие регистрации.

Если регистрация завершается неудачей, приложение продолжает работу без зарегистрированного экземпляра. Чтобы сделать регистрацию обязательным условием запуска, установите `registration.fail-fast: true`.

## Расширение

Starter поддерживает замену стандартных компонентов пользовательскими Spring-бинами. Например:

- `PluginManifestProvider` - позволяет настроить собственную логику формирования манифеста.
- `RegistryClient` - позволяет реализовать собственное взаимодействие с Registry.

Чтобы заменить стандартную реализацию, достаточно объявить бин соответствующего типа. Starter автоматически использует пользовательский бин вместо стандартного.