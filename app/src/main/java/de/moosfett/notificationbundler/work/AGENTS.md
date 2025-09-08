# AGENTS

Goal: Deterministic scheduling based on a list of HH:mm (local time). After each run, compute the next occurrence and enqueue one-off work (REPLACE policy).

Tasks:
- Wire action buttons: deliver now, snooze 15m, skip.
- Respect `includeOngoing` and `includeLowImportance` toggles.
- Add idempotency (run guard) to DeliveryWorker.
