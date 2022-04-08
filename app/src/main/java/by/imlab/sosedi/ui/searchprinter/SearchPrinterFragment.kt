package by.imlab.sosedi.ui.searchprinter

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import by.imlab.core.extensions.set
import by.imlab.core.utils.tickerFlow
import by.imlab.data.model.Printer
import by.imlab.sosedi.R
import by.imlab.sosedi.ui.global.extentions.dialog
import by.imlab.sosedi.ui.global.extentions.setDivider
import by.imlab.sosedi.ui.global.extentions.startRotateAnimation
import by.imlab.sosedi.ui.global.extentions.toast
import com.fondesa.kpermissions.allGranted
import com.fondesa.kpermissions.coroutines.sendSuspend
import com.fondesa.kpermissions.extension.permissionsBuilder
import kotlinx.android.synthetic.main.search_printer_fragment.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class SearchPrinterFragment : Fragment(R.layout.search_printer_fragment) {

    private val viewModel: SearchPrinterViewModel by viewModel()

    private val adapter: SearchPrinterAdapter by lazy { SearchPrinterAdapter(viewModel) }
    private val bluetoothAdapter: BluetoothAdapter by lazy { BluetoothAdapter.getDefaultAdapter() }

    private var printerFound: Boolean = false

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Set divider for printers list
        printers.setDivider(R.drawable.recycler_view_divider)

        // Setup SearchPrinterAdapter
        printers.adapter = adapter

        // Check search printer state
        viewModel.state.observe(viewLifecycleOwner) { state -> render(state = state) }

        // Check bluetooth state
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            } else {
                // Check connected devices
                val devices = bluetoothAdapter.bondedDevices
                viewModel.checkDevicesAndConnect(devices = devices)
            }
        }

        // Start rotation
        syncIcon.startRotateAnimation()

        // Listen exit button
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                //dialog(R.string.exit_and_stop_work, R.string.complete, {
                //    findNavController().navigate(R.id.loginFragment)
                    viewModel.resetState()
                //}, R.string.cancel, {})
            }

        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        // Listen refresh button
        refreshButton.setOnClickListener {
            refreshPrintersList()
        }

        // Listen exit button
        exitButton.setOnClickListener {
            dialog(R.string.exit_and_stop_work, R.string.complete, {
                findNavController().navigate(R.id.loginFragment)
                viewModel.resetState()
            }, R.string.cancel, {})
        }

        // Listen enter scan button
        enterButton.setOnClickListener {
            dialog(R.string.enter_without_printer_connect, R.string.enter, {
                val action = SearchPrinterFragmentDirections
                    .actionSearchPrinterFragmentToOrdersListFragment()
                findNavController().navigate(action)
                viewModel.resetState()
            }, R.string.cancel, {})
        }

        // Listen barcode scan button
        barcodeScanButton.setOnClickListener {
            val action = SearchPrinterFragmentDirections
                .actionSearchPrinterFragmentToScanPrinterFragment()
            findNavController().navigate(action)
            viewModel.resetState()
        }

        // Register for broadcasts when a device is discovered.
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        requireActivity().registerReceiver(viewModel.bluetoothReceiver, filter)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.checkActivityResult(requestCode, resultCode)
    }

    override fun onDestroy() {
        requireActivity().unregisterReceiver(viewModel.bluetoothReceiver)
        bluetoothAdapter.cancelDiscovery()
        super.onDestroy()
    }

    private fun render(state: SearchPrinterState) {
        when (state) {
            is SearchPrinterState.BluetoothEnabled -> renderBluetoothEnabled()
            is SearchPrinterState.BluetoothDisabled -> renderBluetoothDisabled()
            is SearchPrinterState.PermissionsGranted -> renderPermissionsGranted()
            is SearchPrinterState.PermissionsDenied -> renderPermissionsDenied()
            is SearchPrinterState.PrinterFetchSuccess -> renderPrinterFetchSuccess(state.printer)
            is SearchPrinterState.PrinterConnect -> renderPrinterConnect()
            is SearchPrinterState.PrinterConnectError -> renderPrinterConnectError()
        }
    }

    private fun renderBluetoothEnabled() {
        // Build the request with the permissions
        viewLifecycleOwner.lifecycleScope.launch {
            val result = permissionsBuilder(Manifest.permission.ACCESS_COARSE_LOCATION)
                .build().sendSuspend()
            // Handle the result.
            if (result.allGranted()) {
                viewModel.state.set(SearchPrinterState.PermissionsGranted)
            } else {
                viewModel.state.set(SearchPrinterState.PermissionsDenied)
            }
        }
    }

    private fun renderBluetoothDisabled() {
        dialog(R.string.bluetooth_disabled_error, R.string.go_to) {
            val intentOpenBluetoothSettings = Intent()
            intentOpenBluetoothSettings.action = Settings.ACTION_BLUETOOTH_SETTINGS
            startActivity(intentOpenBluetoothSettings)
            viewModel.resetState()
        }
    }

    private fun renderPermissionsGranted() {
        // Start listen bluetooth devices
        bluetoothAdapter.startDiscovery()

        // Show printers list after two seconds
        viewLifecycleOwner.lifecycleScope.launch {
            tickerFlow(3000).collect {
                if (actionBar != null) {
                    actionBar.isVisible = true
                    if (!printerListBlock.isVisible) {
                        printerListBlock.isVisible = true
                        searchBlock.isVisible = false
                    }
                }
            }
        }

        // Waiting for printer and show dialog
        viewLifecycleOwner.lifecycleScope.launch {
            tickerFlow(15000).collect {
                if (!printerFound) {
                    dialog(R.string.printer_not_found, R.string.go_to) {
                        val intentOpenBluetoothSettings = Intent()
                        intentOpenBluetoothSettings.action = Settings.ACTION_BLUETOOTH_SETTINGS
                        startActivity(intentOpenBluetoothSettings)
                        viewModel.resetState()
                    }
                }
            }
        }
    }

    private fun renderPermissionsDenied() {
        dialog(R.string.permissions_error, R.string.go_to) {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + requireContext().packageName)
            )
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            viewModel.resetState()
        }
    }

    private fun renderPrinterFetchSuccess(printer: Printer) {
        printerFound = true
        adapter.addPrinter(printer)

        // Stop rotate animation of the refresh button
        refreshButton.clearAnimation()
    }

    private fun renderPrinterConnect() {
        printerFound = true
        toast(R.string.printer_connected)
        val action = SearchPrinterFragmentDirections
            .actionSearchPrinterFragmentToOrdersListFragment()
        findNavController().navigate(action)
        viewModel.resetState()
    }

    private fun renderPrinterConnectError() {
        toast(R.string.printer_is_not_responding)
    }

    private fun refreshPrintersList() {
        val rotate = RotateAnimation(
            0.0f,
            360.0f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        rotate.duration = 2.toLong() * 500
        rotate.repeatCount = Animation.INFINITE
        refreshButton.startAnimation(rotate)

        adapter.clearList()
        bluetoothAdapter.cancelDiscovery()
        bluetoothAdapter.startDiscovery()
    }

    companion object {
        // Bluetooth turn-on request code
        const val REQUEST_ENABLE_BT = 42

        // Bluetooth turn-on request was allowed
        const val ENABLE_BT_ALLOW = -1

        // Bluetooth turn-on request was denied
        const val ENABLE_BT_DENY = 0
    }

}
