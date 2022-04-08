package by.imlab.sosedi.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.preference.PreferenceFragmentCompat
import by.imlab.sosedi.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.search_printer_fragment.*

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onDestroy() {
        Preferences().readPreferences(requireActivity().applicationContext);
        super.onDestroy()
    }
}