package info.justaway

import android.app.ActionBar
import android.app.Activity
import android.os.Bundle
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.view.MenuItem

import info.justaway.util.ThemeUtil

class NotificationSettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil.setTheme(this)
        fragmentManager.beginTransaction().replace(android.R.id.content, SettingsFragment()).commit()

        actionBar?.run {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

    class SettingsFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            val preferenceManager = preferenceManager ?: return
            preferenceManager.sharedPreferencesName = "settings"

            addPreferencesFromResource(R.xml.pref_notification)
        }
    }
}
