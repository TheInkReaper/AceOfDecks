package com.theinkreaper.aceofdecks.di

import androidx.room.Room
import com.theinkreaper.aceofdecks.data.AppDatabase
import com.theinkreaper.aceofdecks.data.AppRepository
import com.theinkreaper.aceofdecks.data.DataExchangeManager
import com.theinkreaper.aceofdecks.domain.SrsAlgorithm
import com.theinkreaper.aceofdecks.ui.deck.DeckDetailViewModel
import com.theinkreaper.aceofdecks.ui.editor.CardEditorViewModel
import com.theinkreaper.aceofdecks.ui.home.HomeViewModel
import com.theinkreaper.aceofdecks.ui.quiz.QuizViewModel
import com.theinkreaper.aceofdecks.ui.study.StudyViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "aceofdecks.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    single { get<AppDatabase>().appDao() }

    factory { SrsAlgorithm() }

    single { AppRepository(get(), get()) }

    viewModel { HomeViewModel(get(), get(), androidContext()) }
    viewModel { DeckDetailViewModel(get()) }
    viewModel { CardEditorViewModel(get()) }
    viewModel { StudyViewModel(get()) }
    viewModel { QuizViewModel(get()) }
    single { DataExchangeManager(androidContext(), get()) }
}