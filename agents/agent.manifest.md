# Agent Brief — Manifest & Permissions

Goal: Keep the manifest minimal but correct. Ensure NotificationListenerService is declared with proper permission; ask for POST_NOTIFICATIONS at runtime on 33+; reschedule on BOOT_COMPLETED and TIMEZONE_CHANGED.

Tasks:
- Add `QUERY_ALL_PACKAGES` only if strictly needed.
- Add explicit exported flags as required by targetSdk 35.
