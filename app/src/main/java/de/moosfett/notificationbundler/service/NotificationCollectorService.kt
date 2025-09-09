package de.moosfett.notificationbundler.service

import android.app.NotificationManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import dagger.hilt.android.AndroidEntryPoint
import de.moosfett.notificationbundler.data.entity.FilterRuleEntity
import de.moosfett.notificationbundler.data.entity.FilterEvaluationEntity
import de.moosfett.notificationbundler.data.entity.PatternType
import de.moosfett.notificationbundler.data.entity.NotificationEntity
import de.moosfett.notificationbundler.data.repo.FiltersRepository
import de.moosfett.notificationbundler.data.repo.NotificationsRepository
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
    private val includeOngoingSetting = MutableStateFlow(true)
    private val includeLowImportanceSetting = MutableStateFlow(true)
    private val handlingActive = MutableStateFlow(true)
    private val logActive = MutableStateFlow(false)
    private val learningActive = MutableStateFlow(false)

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NotificationManager::class.java)
        scope.launch {
            filtersRepo.observeAll().collect { rulesCache.value = it }
        }
        scope.launch { settings.includeOngoingFlow.collect { includeOngoingSetting.value = it } }
        scope.launch { settings.includeLowImportanceFlow.collect { includeLowImportanceSetting.value = it } }
        scope.launch { settings.handlingActiveFlow.collect { handlingActive.value = it } }
        scope.launch { settings.logActiveFlow.collect { logActive.value = it } }
        scope.launch { settings.learningActiveFlow.collect { learningActive.value = it } }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Prevent feedback loop
        if (sbn.packageName == packageName) return

        scope.launch {
            if (!handlingActive.value) return@launch

            val n = sbn.notification
            val isOngoing = sbn.isOngoing
            val importance = n.channelId?.let { id ->
                notificationManager.getNotificationChannel(id)?.importance
            } ?: NotificationManager.IMPORTANCE_DEFAULT

            val includeOngoing = includeOngoingSetting.value
            val includeLowImportance = includeLowImportanceSetting.value

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

            val decision = when {
                match?.isCritical == true -> "whitelist"
                match?.isExcluded == true -> "blacklist"
                else -> "unlisted"
            }

            val shouldLog = logActive.value || (learningActive.value && decision == "unlisted")
            if (shouldLog) {
                filtersRepo.logEvaluation(
                    FilterEvaluationEntity(
                        notificationKey = entity.key,
                        packageName = entity.packageName,
                        postTime = entity.postTime,
                        ruleId = match?.id,
                        decision = decision
                    )
                )
            }

            when (decision) {
                "blacklist" -> {
                    cancelNotification(sbn.key)
                    notificationsRepo.insert(entity)
                }
                "whitelist" -> {
                    // pass through
                }
                else -> {
                    // unlisted - pass through
                }
            }
        }
    }

    private fun matchRule(rules: List<FilterRuleEntity>, e: NotificationEntity): FilterRuleEntity? {
        val specific = rules.filter { !it.isDefault }
        val match = specific.firstOrNull { r ->
            val pkgOk = r.packageName?.let { it == e.packageName } ?: true
            val channelOk = r.channelId?.let { it == e.channelId } ?: true
            val keywordOk = r.keyword?.let { kw ->
                keywordMatches(kw, r.patternType, e)
            } ?: true
            pkgOk && channelOk && keywordOk
        }
        return match ?: rules.firstOrNull { it.isDefault && it.packageName == e.packageName }
    }

    private fun keywordMatches(kw: String, type: PatternType, e: NotificationEntity): Boolean {
        val fields = listOf(e.title, e.text)
        return when (type) {
            PatternType.EXACT -> fields.any { it?.contains(kw, ignoreCase = true) == true }
            PatternType.LIKE -> {
                val regex = Regex(kw.replace("%", ".*"), RegexOption.IGNORE_CASE)
                fields.any { it?.let { txt -> regex.containsMatchIn(txt) } == true }
            }
            PatternType.REGEX -> {
                runCatching { Regex(kw, RegexOption.IGNORE_CASE) }.getOrNull()?.let { rx ->
                    fields.any { it?.let { txt -> rx.containsMatchIn(txt) } == true }
                } ?: false
            }
        }
    }
}
