# Notification Bundler
v0.3.1

**Summary:** The app collects incoming notifications, stores them locally (Room), and delivers them bundled at user‑defined times (WorkManager). Critical messages can be forwarded immediately; bundled summaries offer actions to deliver now, snooze 15 minutes, or skip.

## How It Works
Notification Bundler listens for notifications on the device and persists each entry in a local Room database. A WorkManager job schedules summary deliveries based on user‑defined rules. When a bundle is ready, the app posts a summary notification containing all collected items. From that summary, users can deliver messages immediately, snooze the bundle for fifteen minutes, or dismiss it entirely.

## Current Features
- Intercepts notifications via `NotificationListenerService`.
- Persists notifications and filter rules using Room.
- Supports critical message rules that bypass bundling.
- Bundles notifications and schedules delivery with WorkManager.
- Offers actions to deliver now, snooze for 15 minutes, or skip.
- Stores schedules and retention policies in DataStore.
- Provides a minimal Jetpack Compose UI for configuration.

## Upcoming Features
- Richer Compose UI for browsing history and managing rules.
- Granular scheduling options and per‑app bundling controls.
- Backup and restore of rules and settings.
- Additional filter types and grouping strategies.

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
