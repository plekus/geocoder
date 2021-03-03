package ru.geocoder.di

import org.koin.dsl.module
import org.koin.experimental.builder.create
import ru.geocoder.ui.MainPresenter

val uiModule = module {
    factory<MainPresenter> { create<MainPresenter>() }
}