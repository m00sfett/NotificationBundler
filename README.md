# Notification Bundler (Android, Kotlin, Compose)

**Kurzfassung (DE):** App sammelt eingehende Benachrichtigungen, speichert sie lokal (Room) und liefert sie zu benutzerdefinierten Zeiten gebündelt aus (WorkManager). Kritische Nachrichten können sofort durchgereicht werden. Alle sichtbaren Texte sind Deutsch (strings.xml); Code & Kommentare sind Englisch.

## Build
- Android Studio Hedgehog / Iguana oder neuer
- Min SDK 26, Target SDK 35
- Kotlin 2.0, AGP 8.4.x

Öffnen → `app` als Run Configuration starten (Berechtigungen erteilen).

## Hauptkomponenten
- `service/NotificationCollectorService` – erfasst Benachrichtigungen (NotificationListenerService).
- `data/` – Room (`NotificationEntity`, `FilterRuleEntity`) + Repositories.
- `settings/SettingsStore` – DataStore (Zeiten, Retention).
- `work/DeliveryWorker`, `work/Scheduling` – Bündelung & Planung (WorkManager).
- `notifications/Notifier` – Kanal-Setup + Summary/Kritisch.
- `receivers/*` – BOOT_COMPLETED, TIMEZONE_CHANGED, Aktionen.
- `ui/*` – Compose-Screens (minimal).

## TODO / Next Steps
- Volle UI für Schedule/Filter-Editor.
- Action-Intents in `Notifier` (Deliver/Snooze/Skip) verdrahten.
- Unit-Tests ausbauen (Filter Engine, Retention).
- Hilt-Integration für Worker/Service verfeinern.
