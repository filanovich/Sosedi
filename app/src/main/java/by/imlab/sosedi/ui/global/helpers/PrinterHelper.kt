package by.imlab.sosedi.ui.global.helpers

import android.bluetooth.BluetoothAdapter
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import by.imlab.core.extensions.default
import by.imlab.core.extensions.set
import com.zebra.sdk.comm.BluetoothConnection
import com.zebra.sdk.comm.Connection
import com.zebra.sdk.comm.ConnectionException
import com.zebra.sdk.device.ZebraIllegalArgumentException
import com.zebra.sdk.graphics.internal.ZebraImageAndroid
import com.zebra.sdk.printer.SGD
import com.zebra.sdk.printer.ZebraPrinter
import com.zebra.sdk.printer.ZebraPrinterFactory
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class PrinterHelper {

    private val state = MutableLiveData<PrintState>().default(PrintState.Suspense)

    private var printerConnection: Connection? = null
    private var printer: ZebraPrinter? = null

    private var macAddress: String = ""

    fun connect(mac: String) = flow {
        macAddress = mac

        try {
            emit(openBluetoothConnection(macAddress))
        } catch (e: Exception) {
            emit(false)
            disconnect()
        }
    }

    private fun disconnect() {
        printer = null
        (printerConnection as BluetoothConnection).close()
    }

    fun printBitmap(bitmap: Bitmap): Flow<PrintState> {
        // Open connection and start printing
        initAndPrint(bitmap)

        return state.asFlow()
    }

    private fun initAndPrint(bitmap: Bitmap) {
        if (checkPrinterConnected()) {
            openBluetoothConnection(macAddress)
        }

        try {
            // Initialize printer
            initializePrinter()

            // Check printer status
            val isPrinterReady = checkPrintAvailable()
            if (isPrinterReady) {
                printLabel(bitmap)
                printer?.connection?.close()
                Log.e(TAG, "Printing is success")
                state.set(PrintState.Success)
            } else {
                Log.e(TAG, "Printer status is not ready")
            }
        } catch (e: ConnectionException) {
            Log.e(TAG, "Connection exception: " + e.message)
            state.set(PrintState.ConnectionError)
        } catch (e: ZebraPrinterLanguageUnknownException) {
            Log.e(TAG, "ZebraPrinterLanguageUnknown exception: " + e.message)
            state.set(PrintState.InternalError)
        } catch (e: ZebraIllegalArgumentException) {
            Log.e(TAG, "ZebraIllegalArgument exception: " + e.message)
            state.set(PrintState.InternalError)
        } catch (e: Exception) {
            Log.e(TAG, "Exception: " + e.message)
            state.set(PrintState.NotAvailableError)
        }
    }

    private fun printLabel(bitmap: Bitmap) {
        val zebraPrinterLinkOs = ZebraPrinterFactory.createLinkOsPrinter(printer)
        val zebraImageAndroid = ZebraImageAndroid(bitmap)
        val labelHeight = Integer.valueOf(zebraImageAndroid.height)
        val labelSleep = Integer.valueOf(labelHeight / 400) * 1000 * speed // 2 - is speed
        Log.d(TAG, "labelHeight: $labelHeight")
        Log.d(TAG, "labelSleep: $labelSleep")

        // Set the length of the label first to prevent too small or too large a print
        if (zebraPrinterLinkOs != null) {
            setLabelLength(zebraImageAndroid)
        }
        if (zebraPrinterLinkOs != null) {
            Log.d(TAG, "calling printer.printImage")
            printer?.printImage(zebraImageAndroid, 0, 0, 0, 0, false)
        } else {
            Log.d(TAG, "Storing label on printer...")
            printer?.storeImage("wgkimage.pcx", zebraImageAndroid, -1, -1)
            printImageTheOldWay(zebraImageAndroid)
            SGD.SET("device.languages", "line_print", printerConnection)
        }
    }

    private fun printImageTheOldWay(zebraImageAndroid: ZebraImageAndroid) {
        Log.d(TAG, "Printing image...")
        var cpcl = "! 0 200 200 "
        cpcl += zebraImageAndroid.height
        cpcl += " 1\r\n"
        // print diff
        cpcl += "PW 750\r\nTONE 0\r\nSPEED 6\r\nSETFF 203 5\r\nON - FEED FEED\r\nAUTO - PACE\r\nJOURNAL\r\n"
        //cpcl += "TONE 0\r\nJOURNAL\r\n";
        cpcl += "PCX 150 0 !<wgkimage.pcx\r\n"
        cpcl += "FORM\r\n"
        cpcl += "PRINT\r\n"
        printerConnection?.write(cpcl.toByteArray())
    }

    private fun setLabelLength(zebraImageAndroid: ZebraImageAndroid) {
        val zebraPrinterLinkOs =
            ZebraPrinterFactory.createLinkOsPrinter(printer)
        if (zebraPrinterLinkOs != null) {
            val currentLabelLength =
                zebraPrinterLinkOs.getSettingValue("zpl.label_length")
            Log.d(TAG, "mitja $currentLabelLength")
            if (currentLabelLength != zebraImageAndroid.height.toString()) {
                // printer_diff
                Log.d(TAG, "mitja me " + zebraImageAndroid.height)
                zebraPrinterLinkOs.setSetting(
                    "zpl.label_length",
                    zebraImageAndroid.height.toString() + ""
                )
            }
        }
    }

    private fun checkPrinterConnected(): Boolean {
        return printerConnection?.isConnected ?: false
    }

    private fun openBluetoothConnection(macAddress: String): Boolean {
        return try {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (bluetoothAdapter.isEnabled) {
                try {
                    return checkPrintAvailable()
                } catch (e: Exception) {
                    printerConnection = BluetoothConnection(macAddress)
                    initializePrinter()
                    true
                }
            } else {
                Log.d(TAG, "Bluetooth is disabled...")
                state.set(PrintState.BluetoothError)
                false
            }
        } catch (e: Exception) {
            state.set(PrintState.BluetoothError)
            false
        }
    }

    private fun initializePrinter() {
        Log.d(TAG, "Opening connection...")
        printerConnection?.open()
        Log.d(TAG, "connection successfully opened...")

        printer = ZebraPrinterFactory.getInstance(printerConnection)
        val printerLanguage =
            SGD.GET("device.languages", printerConnection)
        if (!printerLanguage.contains("zpl")) {
            SGD.SET("device.languages", "hybrid_xml_zpl", printerConnection)
            Log.d(TAG, "printer language set...")
        }
    }

    private fun checkPrintAvailable(): Boolean {
        Log.d(TAG, "Start checking status")
        val printerStatus = printer!!.currentStatus
        when {
            printerStatus.isReadyToPrint -> {
                Log.d(TAG, "Ready To Print")
                return true
            }
            printerStatus.isHeadOpen -> {
                Log.d(TAG, "Cannot Print because the printer head is open.")
                state.set(PrintState.HeadOpenError)
                return false
            }
            printerStatus.isPaperOut -> {
                Log.d(TAG, "Cannot Print because the paper is out.")
                state.set(PrintState.PaperOutError)
                return false
            }
            else -> {
                Log.d(TAG, "Cannot Print.")
                state.set(PrintState.InternalError)
                return false
            }
        }
    }

    companion object {
        private var speed = 2
        private val TAG = PrinterHelper::class.simpleName
    }

}