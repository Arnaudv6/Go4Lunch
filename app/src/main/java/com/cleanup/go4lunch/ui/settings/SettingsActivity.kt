package com.cleanup.go4lunch.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.cleanup.go4lunch.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
    }

    @AndroidEntryPoint
    class SettingsFragment : PreferenceFragmentCompat() {

        private val viewModel: SettingsViewModel by viewModels()

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            this.findPreference<ListPreference>(getString(R.string.preferences_theme_key))
                ?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    viewModel.themeSet(newValue)
                    true
                }

            this.findPreference<Preference>(getString(R.string.preferences_clear_cache_key))
                ?.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    viewModel.clearCache()
                    true
                }

            this.findPreference<Preference>(getString(R.string.preferences_login_key))
                ?.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    true
                }

            this.findPreference<Preference>(getString(R.string.preferences_gps_key))
                ?.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    true
                }
        }
    }
}

