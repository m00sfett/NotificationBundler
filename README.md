# Notification Bundler (Android, Kotlin, Compose)

**Summary (EN):** The app collects incoming notifications, stores them locally (Room), and delivers them bundled at user‑defined times (WorkManager). Critical messages can be forwarded immediately. All user‑visible text is German (strings.xml); code and comments are in English.

## Build
- Android Studio Hedgehog / Iguana or newer
- Min SDK 26, Target SDK 35
- Kotlin 2.0, AGP 8.4.x

Open → start `app` as Run Configuration (grant permissions).

## Main Components
- `service/NotificationCollectorService` – captures notifications (NotificationListenerService).
- `data/` – Room (`NotificationEntity`, `FilterRuleEntity`) + repositories.
- `settings/SettingsStore` – DataStore (schedules, retention).
- `work/DeliveryWorker`, `work/Scheduling` – bundling & scheduling (WorkManager).
- `notifications/Notifier` – channel setup + summary/critical paths.
- `receivers/*` – BOOT_COMPLETED, TIMEZONE_CHANGED, actions.
- `ui/*` – minimal Compose screens.

## TODO / Next Steps
- Full UI for schedule/filter editor.
- Wire action intents in `Notifier` (Deliver/Snooze/Skip).
- Expand unit tests (filter engine, retention).
- Refine Hilt integration for worker/service.

---

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
