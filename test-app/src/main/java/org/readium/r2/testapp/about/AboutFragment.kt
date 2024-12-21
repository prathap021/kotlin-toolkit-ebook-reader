/*
 * Copyright 2021 Readium Foundation. All rights reserved.
 * Use of this source code is governed by the BSD-style license
 * available in the top-level LICENSE file of the project.
 */

package org.readium.r2.testapp.about

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import java.util.Locale
import org.readium.r2.testapp.LanguageViewModel
import org.readium.r2.testapp.MainActivity
import org.readium.r2.testapp.R
import org.readium.r2.testapp.databinding.FragmentAboutBinding
import org.readium.r2.testapp.databinding.FragmentBookshelfBinding
import org.readium.r2.testapp.utils.viewLifecycle

class AboutFragment : Fragment() {

    private var binding: FragmentAboutBinding by viewLifecycle()
    private val languageViewModel: LanguageViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root

//        val root = inflater.inflate(R.layout.fragment_about, container, false)

//        val switchLanguage: Switch = root.findViewById(R.id.switch_toggle)
//        val currentLanguage = LanguageManager.getCurrentLanguage(requireContext())
//
//        // Set the switch state based on the current language
//        switchLanguage.isChecked = currentLanguage == "ta"
//
//        // Handle switch toggle
//        switchLanguage.setOnCheckedChangeListener { _, isChecked ->
//            val newLanguage = if (isChecked) "ta" else "en"
//            languageViewModel.setLanguage(newLanguage)
//        }


//        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val languagePreference = LanguagePreference(requireContext())

        binding.switchToggle.isChecked =
            if (languagePreference.getLanguage() == "en") false else true
        binding.selectedLanguage.text = "தமிழ்"
        binding.switchToggle.setOnCheckedChangeListener { _, isChecked ->
            val newLanguage = if (isChecked) {
                "ta"
            } else {
                "en"
            }

            languagePreference.setLanguage(newLanguage)
            setLocale(requireContext(), newLanguage)
            refreshTheScreen()
        }

    }


    private fun refreshTheScreen() {
        val i = Intent(requireContext(), MainActivity::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            requireActivity().overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, 0, 0)
            requireActivity().startActivity(i)
            requireActivity().overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, 0, 0)
        } else {
            requireActivity().overridePendingTransition(0, 0)
            requireActivity().startActivity(i)
            requireActivity().overridePendingTransition(0, 0)

        }
    }

    fun setLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

}
