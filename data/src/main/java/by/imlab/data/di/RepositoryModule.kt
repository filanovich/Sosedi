package by.imlab.data.di

import by.imlab.data.repository.*
import by.imlab.data.update.FtpUpdate
import org.koin.dsl.module

val repositoryModule = module {
    single { LoginRepository(get(), get()) }
    single { OrderRepository(get(), get(), get(), get(), get(), get(), get(), get()) }
    single { SkuRepository(get()) }
    single { CargoSpaceRepository(get()) }
    single { BarcodeRepository(get()) }
}
