package de.moosfett.notificationbundler

import de.moosfett.notificationbundler.data.entity.FilterRuleEntity
import de.moosfett.notificationbundler.data.entity.NotificationEntity
import de.moosfett.notificationbundler.service.NotificationCollectorService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleMatchTest {
    @Test
    fun simpleKeywordMatch() {
        val rule = FilterRuleEntity(keyword = "ALARM", isCritical = true)
        val n = NotificationEntity(
            key = null, packageName = "pkg", channelId = null, category = null,
            title = "ALARM ausgelöst", text = "Motion", postTime = 0,
            groupKey = null, isOngoing = false, importance = null, extrasJson = null
        )
        val ok = (rule.keyword ?: "") in (n.title ?: "")
        assertTrue(ok)
    }

    @Test
    fun firstMatchingRuleWins() {
        val rules = listOf(
            FilterRuleEntity(id = 1, keyword = "ALARM", isExcluded = true),
            FilterRuleEntity(id = 2, keyword = "ALARM", isCritical = true)
        )
        val n = NotificationEntity(
            key = null, packageName = "pkg", channelId = null, category = null,
            title = "ALARM ausgelöst", text = "Motion", postTime = 0,
            groupKey = null, isOngoing = false, importance = null, extrasJson = null
        )
        val service = NotificationCollectorService()
        val method = NotificationCollectorService::class.java.getDeclaredMethod(
            "matchRule", List::class.java, NotificationEntity::class.java
        )
        method.isAccessible = true
        val match = method.invoke(service, rules, n) as FilterRuleEntity?
        assertEquals(1L, match?.id)
        assertTrue(match?.isExcluded == true)
    }
}
