# Agent Brief — Data Layer (Room + DataStore)

Goal: Expand entities and DAOs; add indices on `postTime`, `packageName`. Provide query helpers for "pending" windows and retention. Use KSP (no KAPT).

Tasks:
- Add migrations (v2+).
- Introduce DeliveryLog entity (timestamp, counts).
- Optimize queries for large volumes.
