package com.cleanup.go4lunch.ui.settings

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.cleanup.go4lunch.R
import com.google.android.material.snackbar.Snackbar
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // https://stackoverflow.com/questions/28438030
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @AndroidEntryPoint
    class SettingsFragment : PreferenceFragmentCompat() {

        private val viewModel: SettingsViewModel by viewModels()

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

            setPreferencesFromResource(R.xml.root_preferences, null)

            this.findPreference<ListPreference>(getString(R.string.preferences_theme_key))
                ?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    viewModel.themeSet(newValue)
                    true
                }

            this.findPreference<ListPreference>(getString(R.string.preferences_locale_key))
                ?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    viewModel.themeLocale(newValue)
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
                    // startActivity(Intent())
                    true
                }

            // no binding for notifications SwitchPreference, as Android automagically
            // saves the value in defaultSharedPreferences, and settings repo has a callBack.
        }


        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            // todo enable notifications by default : observe from MainActivity? if not, 2 events => ViewAction
            viewModel.notificationsEnabledLiveData.observe(viewLifecycleOwner) {
                viewModel.enableNotifications(it)
            }

            viewModel.snackBarSingleLiveEvent.observe(viewLifecycleOwner) {
                Snackbar.make(view, it, Snackbar.LENGTH_SHORT).setAction("Dismiss") {}.show()
            }
        }
    }
}

