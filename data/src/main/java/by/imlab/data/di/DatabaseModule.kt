package by.imlab.data.di

import by.imlab.data.database.AppDatabase
import org.koin.dsl.module

val databaseModule = module {
    single { AppDatabase.getInstance(get()) }
    single { get<AppDatabase>().orderDao }
    single { get<AppDatabase>().skuDao }
    single { get<AppDatabase>().cargoSpaceDao }
    single { get<AppDatabase>().barcodeDao }
}