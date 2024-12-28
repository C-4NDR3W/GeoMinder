package com.example.geominder.settings

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.preference.DropDownPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.geominder.R

class NotificationSettingFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.notification_preferences, rootKey)

        val notificationDelayPreference = findPreference<ListPreference>("notificationDelay")
        val notificationTriggerRangePreference = findPreference<ListPreference>("notificationTriggerRange")
        val vibrationModePreference = findPreference<ListPreference>("vibrationMode")
        val vibrationLengthPreference = findPreference<ListPreference>("vibrationLength")
        val notificationStrengthPreference = findPreference<ListPreference>("notificationStrength")

        notificationDelayPreference?.apply {
            summary = entry
            setOnPreferenceChangeListener { _, newValue ->
                val index = findIndexOfValue(newValue.toString())
                summary = if (index >= 0) entries[index] else newValue.toString()
                true
            }
        }

        notificationTriggerRangePreference?.apply {
            summary = entry
            setOnPreferenceChangeListener { _, newValue ->
                val index = findIndexOfValue(newValue.toString())
                summary = if (index >= 0) entries[index] else newValue.toString()
                true
            }
        }
        notificationStrengthPreference?.apply {
            summary = entry
            setOnPreferenceChangeListener { _, newValue ->
                val index = findIndexOfValue(newValue.toString())
                summary = if (index >= 0) entries[index] else newValue.toString()
                true
            }
        }

        vibrationModePreference?.apply {
            summary = entry // Set current value as summary
            setOnPreferenceChangeListener { _, newValue ->
                val index = findIndexOfValue(newValue.toString())
                summary = if (index >= 0) entries[index] else newValue.toString()
                true
            }
        }
        vibrationLengthPreference?.apply {
            summary = entry // Set current value as summary
            setOnPreferenceChangeListener { _, newValue ->
                val index = findIndexOfValue(newValue.toString())
                summary = if (index >= 0) entries[index] else newValue.toString()
                true
            }
        }


        setupListeners()
    }


    private fun setupListeners() {
        val enableVibrationPref = findPreference<SwitchPreferenceCompat>("enableVibration")
        enableVibrationPref?.setOnPreferenceChangeListener { _, newValue ->
            val isEnabled = newValue as Boolean
            handleVibrationChange(isEnabled)
            true
        }
        val vibrationModePref = findPreference<DropDownPreference>("vibrationMode")
        val vibrationLengthPref = findPreference<DropDownPreference>("vibrationLength")
        vibrationModePref?.setOnPreferenceChangeListener { _, newValue ->
            val selectedMode = newValue as String
            val selectedLength = vibrationLengthPref?.value ?: "long"
            updateVibrationPattern(selectedMode, selectedLength)
            true
        }

        vibrationLengthPref?.setOnPreferenceChangeListener { _, newValue ->
            val selectedLength = newValue as String
            val selectedMode = vibrationModePref?.value ?: "default"
            updateVibrationPattern(selectedMode, selectedLength)
            true
        }
    }

    private fun handleVibrationChange(isEnabled: Boolean) {
        val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (isEnabled && vibrator.hasVibrator()) {
            val testMode = "default"
            val testLength = 1000L
            vibrateTest(vibrator, testMode, testLength)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.cancel()
            }
        }
    }

    private fun updateVibrationPattern(selectedMode: String, selectedLength: String) {
        val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (!vibrator.hasVibrator()) return

        val vibrationLength = when (selectedLength) {
            "short" -> 500L
            "medium" -> 1000L
            "long" -> 2000L
            else -> 2000L
        }
        vibrateTest(vibrator, selectedMode, vibrationLength)
    }

    private fun vibrateTest(vibrator: Vibrator, mode: String, length: Long) {
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val vibrationEffect = when (mode) {
                "tick" -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                "heavy_click" -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                "click" -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                else -> VibrationEffect.createOneShot(length, VibrationEffect.DEFAULT_AMPLITUDE)
            }
            vibrator.vibrate(vibrationEffect)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect =
                VibrationEffect.createOneShot(length, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(vibrationEffect)
        } else {
            vibrator.vibrate(length)
        }
    }
}