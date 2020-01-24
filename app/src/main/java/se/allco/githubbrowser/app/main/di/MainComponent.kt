package se.allco.githubbrowser.app.main.di

import dagger.Subcomponent
import javax.inject.Scope

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class MainScope

@MainScope
@Subcomponent(modules = [MainModule::class])
interface MainComponent
