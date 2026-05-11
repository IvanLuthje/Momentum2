package com.example.momentum2.ui.settings

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.momentum2.R
import java.util.Locale

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val themeSwitch = findPreference<SwitchPreferenceCompat>("theme_dark")
        val languageList = findPreference<ListPreference>("language")

        themeSwitch?.setOnPreferenceChangeListener { _, newValue ->
            val enabled = newValue as Boolean
            AppCompatDelegate.setDefaultNightMode(
                if (enabled) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
            true
        }

        languageList?.setOnPreferenceChangeListener { _, newLang ->
            cambiarIdioma(newLang.toString())
            activity?.recreate()
            true
        }
    }

    private fun cambiarIdioma(lang: String) {
        val locale = Locale(lang)
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)

        requireActivity().baseContext.resources.updateConfiguration(
            config,
            requireActivity().baseContext.resources.displayMetrics
        )

        val prefs = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("language", lang).apply()
    }
}
