package de.moosfett.notificationbundler.service

import android.app.NotificationManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import dagger.hilt.android.AndroidEntryPoint
import de.moosfett.notificationbundler.data.entity.FilterRuleEntity
import de.moosfett.notificationbundler.data.entity.NotificationEntity
import de.moosfett.notificationbundler.data.repo.FiltersRepository
import de.moosfett.notificationbundler.data.repo.NotificationsRepository
import de.moosfett.notificationbundler.notifications.Notifier
import de.moosfett.notificationbundler.settings.SettingsStore
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONObject

@AndroidEntryPoint
class NotificationCollectorService : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Inject lateinit var notificationsRepo: NotificationsRepository
    @Inject lateinit var filtersRepo: FiltersRepository
    @Inject lateinit var settings: SettingsStore
    private lateinit var notificationManager: NotificationManager
    private val rulesCache = MutableStateFlow<List<FilterRuleEntity>>(emptyList())

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NotificationManager::class.java)
        scope.launch {
            filtersRepo.observeAll().collect { rulesCache.value = it }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Prevent feedback loop
        if (sbn.packageName == packageName) return

        scope.launch {
            val n = sbn.notification
            val isOngoing = sbn.isOngoing
            val importance = n.channelId?.let { id ->
                notificationManager.getNotificationChannel(id)?.importance
            } ?: NotificationManager.IMPORTANCE_DEFAULT

            val includeOngoing = settings.includeOngoing()
            val includeLowImportance = settings.includeLowImportance()

            if (!includeOngoing && isOngoing) return@launch
            if (!includeLowImportance &&
                (importance == NotificationManager.IMPORTANCE_LOW ||
                 importance == NotificationManager.IMPORTANCE_MIN)) return@launch

            val extras = n.extras
            val title = extras.getCharSequence("android.title")?.toString()
            val text = extras.getCharSequence("android.text")?.toString()
            val channelId = n.channelId
            val category = n.category

            val extrasJson = JSONObject().apply {
                try {
                    put("tickerText", n.tickerText?.toString())
                    put("subText", extras.getCharSequence("android.subText")?.toString())
                } catch (_: Exception) {}
            }.toString()

            val entity = NotificationEntity(
                key = sbn.key,
                packageName = sbn.packageName,
                channelId = channelId,
                category = category,
                title = title,
                text = text,
                postTime = sbn.postTime,
                groupKey = sbn.groupKey,
                isOngoing = isOngoing,
                importance = importance,
                extrasJson = extrasJson
            )

            val rules = rulesCache.value
            val match = matchRule(rules, entity)

            when {
                match?.isExcluded == true -> {
                    // drop (do not insert)
                }
                match?.isCritical == true -> {
                    // mirror immediately
                    val sourceLabel = try {
                        val pm = packageManager
                        val info = pm.getApplicationInfo(sbn.packageName, 0)
                        pm.getApplicationLabel(info).toString()
                    } catch (_: Exception) {
                        sbn.packageName
                    }
                    Notifier.notifyCritical(applicationContext, title, text, sourceLabel)
                    notificationsRepo.insert(entity.copy(critical = true, delivered = true))
                }
                else -> {
                    notificationsRepo.insert(entity)
                }
            }
        }
    }

    private fun matchRule(rules: List<FilterRuleEntity>, e: NotificationEntity): FilterRuleEntity? {
        return rules.firstOrNull { r ->
            val pkgOk = r.packageName?.let { it == e.packageName } ?: true
            val channelOk = r.channelId?.let { it == e.channelId } ?: true
            val keywordOk = r.keyword?.let { kw ->
                (e.title?.contains(kw, ignoreCase = true) == true) ||
                (e.text?.contains(kw, ignoreCase = true) == true)
            } ?: true
            pkgOk && channelOk && keywordOk
        }
    }
}
