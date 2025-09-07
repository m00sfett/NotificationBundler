package de.moosfett.notificationbundler.data.db

import androidx.sqlite.db.SupportSQLiteDatabase
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class MigrationTest {
    @Test
    fun addsIndices() {
        val db = mock(SupportSQLiteDatabase::class.java)
        AppDatabase.MIGRATION_1_2.migrate(db)
        verify(db).execSQL("CREATE INDEX IF NOT EXISTS index_notifications_postTime ON notifications(postTime)")
        verify(db).execSQL("CREATE INDEX IF NOT EXISTS index_notifications_packageName ON notifications(packageName)")
    }
}
