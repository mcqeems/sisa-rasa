package com.mobile.sisarasa.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mobile.sisarasa.SisaRasaApp
import com.mobile.sisarasa.di.AppContainer

@Composable
inline fun <reified VM : ViewModel> appViewModel(crossinline create: (AppContainer) -> VM): VM {
    val app = LocalContext.current.applicationContext as SisaRasaApp
    return viewModel(factory = viewModelFactory {
        initializer { create(app.container) }
    })
}