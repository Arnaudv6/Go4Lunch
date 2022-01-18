package com.freemyip.arnaudv6.go4lunch.data.settings

import android.app.Application
import android.content.SharedPreferences
import androidx.work.WorkManager
import com.freemyip.arnaudv6.go4lunch.R
import com.freemyip.arnaudv6.go4lunch.data.AllDispatchers
import com.freemyip.arnaudv6.go4lunch.utils.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class SettingsRepositoryWithLateinitTest {

    @get:Rule
    val testRule = TestCoroutineRule()

    @MockK
    private lateinit var application: Application

    @MockK
    private lateinit var settingsDao: SettingsDao

    @MockK
    private lateinit var allDispatchers: AllDispatchers

    @MockK
    private lateinit var workManager: WorkManager

    @MockK
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { application.getString(R.string.preferences_theme_key_dark) } returns "PREFERENCES_THEME_KEY_DARK"
        every { application.getString(R.string.preferences_theme_key_light) } returns "PREFERENCES_THEME_KEY_LIGHT"
        every { application.getString(R.string.preferences_theme_key_system) } returns "PREFERENCES_THEME_KEY_SYSTEM"

        settingsRepository = SettingsRepository(
            application = application,
            settingsDao = settingsDao,
            allDispatchers = allDispatchers,
            workManager = workManager,
            sharedPreferences = sharedPreferences
        )
    }

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