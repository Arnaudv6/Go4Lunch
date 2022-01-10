package com.cleanup.go4lunch.data.settings

import android.app.Application
import android.content.SharedPreferences
import androidx.work.WorkManager
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.AllDispatchers
import com.cleanup.go4lunch.utils.TestCoroutineRule
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class SettingsRepositoryTest {

    @get:Rule
    val testRule = TestCoroutineRule()

    private val application: Application = mockk {
        every { getString(R.string.preferences_theme_key_dark) } returns "PREFERENCES_THEME_KEY_DARK"
        every { getString(R.string.preferences_theme_key_light) } returns "PREFERENCES_THEME_KEY_LIGHT"
        every { getString(R.string.preferences_theme_key_system) } returns "PREFERENCES_THEME_KEY_SYSTEM"
    }

    private val settingsDao: SettingsDao = mockk()

    private val allDispatchers: AllDispatchers = mockk()

    private val workManager: WorkManager = mockk()

    private val sharedPreferences: SharedPreferences = mockk()

    private val settingsRepository = SettingsRepository(
        application = application,
        settingsDao = settingsDao,
        allDispatchers = allDispatchers,
        workManager = workManager,
        sharedPreferences = sharedPreferences
    )

    @Test
    fun `setNotification with false param should cancel workManager`() {
        // Given
        every { workManager.cancelUniqueWork(any()) } returns mockk()

        // When
        settingsRepository.setNotification(false)

        // Then
        verify(exactly = 1) {
            workManager.cancelUniqueWork("NOTIFICATION WORKER")
        }
        confirmVerified(workManager)
    }
}