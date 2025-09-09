# Notification Bundler
v0.3.1

**Summary:** The app collects incoming notifications, stores them locally (Room), and delivers them bundled at user‑defined times (WorkManager). Critical messages can be forwarded immediately; bundled summaries offer actions to deliver now, snooze 15 minutes, or skip. All user‑visible text is German (strings.xml); code and comments are in English.

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
- `receivers/*` – BOOT_COMPLETED, TIMEZONE_CHANGED, actions (Deliver/Snooze/Skip).
- `ui/*` – minimal Compose screens.
