package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppsManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("لانشر بسيط", appName)
    }

    @Test
    fun `installed apps query returns fallback or registered apps`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val apps = AppsManager.getInstalledApps(context)
        assertNotNull(apps)
        assertTrue(apps.isNotEmpty())
    }

    @Test
    fun `device stats returns non-null values`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val info = AppsManager.getDeviceInfo(context)
        assertNotNull(info)
        assertTrue(info.totalStorageGb > 0)
    }
}
