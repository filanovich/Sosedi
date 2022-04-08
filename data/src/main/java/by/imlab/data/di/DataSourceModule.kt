package by.imlab.data.di

import by.imlab.data.datasource.LoginDataSource
import by.imlab.data.datasource.OrderDataSource
import org.koin.dsl.module

val dataSourceModule = module {
    single { LoginDataSource() }
    single { OrderDataSource() }
}