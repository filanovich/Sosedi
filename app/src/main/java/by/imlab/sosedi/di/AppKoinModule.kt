package by.imlab.sosedi.di

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import by.imlab.core.KoinModule
import by.imlab.sosedi.ui.cancelledorders.CancelledOrdersViewModel
import by.imlab.sosedi.ui.cargospase.CargoSpaceViewModel
import by.imlab.sosedi.ui.collectedorders.CollectedOrdersViewModel
import by.imlab.sosedi.ui.collectedspec.CollectedSpecViewModel
import by.imlab.sosedi.ui.collectedtransfer.CollectedTransferViewModel
import by.imlab.sosedi.ui.global.helpers.NavigationHelper
import by.imlab.sosedi.ui.global.helpers.PrinterHelper
import by.imlab.sosedi.ui.global.helpers.ScannerHelper
import by.imlab.sosedi.ui.labelprint.LabelPrintViewModel
import by.imlab.sosedi.ui.login.LoginViewModel
import by.imlab.sosedi.ui.main.MainActivity
import by.imlab.sosedi.ui.main.MainViewModel
import by.imlab.sosedi.ui.main.NotificationReceiver
import by.imlab.sosedi.ui.openpackage.OpenPackageViewModel
import by.imlab.sosedi.ui.openpocket.OpenPocketViewModel
import by.imlab.sosedi.ui.orderdetails.OrderDetailsViewModel
import by.imlab.sosedi.ui.orderslist.OrdersListViewModel
import by.imlab.sosedi.ui.scanprinter.ScanPrinterViewModel
import by.imlab.sosedi.ui.searchprinter.SearchPrinterViewModel
import by.imlab.sosedi.ui.skudetails.SkuDetailsViewModel
import by.imlab.sosedi.ui.specification.SpecificationViewModel
import by.imlab.sosedi.ui.transferredorders.TransferredOrdersViewModel
import by.imlab.sosedi.ui.transferredspec.TransferredSpecViewModel
import by.imlab.sosedi.ui.underway.UnderwayViewModel
import by.imlab.sosedi.ui.usecase.PrintCargoUseCase
import by.imlab.sosedi.ui.usecase.PrintSpecUseCase
import com.google.auto.service.AutoService
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val helperModule = module {
    single { PrinterHelper() }
    single { ScannerHelper() }
    single { NavigationHelper() }
}

val useCaseModule = module {
    factory { PrintCargoUseCase(get(), get(), get()) }
    factory { PrintSpecUseCase(get(), get()) }
}

val viewModelModule = module {
    viewModel { MainViewModel(get(), get(), get(), get()) }
    viewModel { SkuDetailsViewModel(get(), get(), get(), get()) }
    viewModel { SearchPrinterViewModel(get()) }
    viewModel { ScanPrinterViewModel(get(), get()) }
    viewModel { OrdersListViewModel(get(), get(), get(), get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { UnderwayViewModel(get()) }
    viewModel { OpenPackageViewModel(get(), get(), get()) }
    viewModel { OrderDetailsViewModel(get(), get(), get(), get()) }
    viewModel { SpecificationViewModel(get(), get(), get(), get()) }
    viewModel { CollectedOrdersViewModel(get(), get()) }
    viewModel { CollectedTransferViewModel(get(), get()) }
    viewModel { LabelPrintViewModel(get(), get()) }
    viewModel { TransferredOrdersViewModel(get(), get()) }
    viewModel { CargoSpaceViewModel(get(), get()) }
    viewModel { TransferredSpecViewModel(get()) }
    viewModel { CollectedSpecViewModel(get(), get()) }
    viewModel { OpenPocketViewModel(get(), get(), get()) }
    viewModel { CancelledOrdersViewModel(get(), get()) }
}

val bluetoothModule = module {
    single { BluetoothAdapter.getDefaultAdapter() }
}

@AutoService(KoinModule::class)
class AppKoinModule : KoinModule {
    override val modulesList = listOf(
        viewModelModule,
        bluetoothModule,
        useCaseModule,
        helperModule
    )
}