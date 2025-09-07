package de.moosfett.notificationbundler

import de.moosfett.notificationbundler.data.entity.FilterRuleEntity
import de.moosfett.notificationbundler.data.entity.NotificationEntity
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
}
