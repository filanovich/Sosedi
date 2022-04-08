package by.imlab.data.di

import by.imlab.core.KoinModule
import by.imlab.data.api.Common
import by.imlab.data.database.AppDatabase
import by.imlab.data.repository.*
import by.imlab.data.update.FtpUpdate
import com.google.auto.service.AutoService
import org.koin.dsl.module

@AutoService(KoinModule::class)
class DataKoinModule : KoinModule {
    override val modulesList = listOf(
        databaseModule,
        dataSourceModule,
        repositoryModule,
        module {
            single { FtpUpdate() }
            single { Common() }
        }
    )
}