# Prime Worker

<!-- Repository badges -->

![Static Badge](https://img.shields.io/badge/opp2211-prime--worker-orange)
![GitHub top language](https://img.shields.io/github/languages/top/opp2211/prime-worker)
![GitHub](https://img.shields.io/github/license/opp2211/prime-worker)
![GitHub Repo stars](https://img.shields.io/github/stars/opp2211/prime-worker)
![GitHub issues](https://img.shields.io/github/issues/opp2211/prime-worker)

## Описание

Проект задеплоен на VPS и используется в рабочем процессе.

Приложение решает конкретную бизнес‑задачу: автоматически получает данные о курсе с Bybit P2P и записывает 
их в Google Таблицы, автоматизируя одну из регулярных рабочих задач.

Приложение ориентировано на стабильность, расширяемость и дальнейшее развитие.

## Возможности

* Получение актуального курса с Bybit P2P через официальный API
* Запись полученных значений в Google Sheets

## CI/CD

Настроен CI/CD pipeline с использованием **GitHub Actions**:

* при коммите в ветку `master` выполняется сборка и тестирование проекта
* приложение автоматически пересобирается и перезапускается на сервере с новой версией

## Технологический стек

* Java 21
* Spring Boot
* Spring WebFlux
* Bybit API
* Google Sheets API
* Maven
* Docker / Docker Compose
* GitHub Actions

## Планы по развитию

* Добавить хранение истории курсов и расчет среднего значения
* Добавить поддержку дополнительных источников (Binance, OKX)
* Добавить логирование и мониторинг 


---
Если вас заинтересовал проект или вы хотите обсудить детали реализации — буду рад обратной связи.