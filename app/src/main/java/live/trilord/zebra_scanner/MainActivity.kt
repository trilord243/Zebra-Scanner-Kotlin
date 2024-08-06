package live.trilord.zebra_scanner

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.symbol.emdk.EMDKManager
import com.symbol.emdk.EMDKManager.EMDKListener
import com.symbol.emdk.EMDKManager.FEATURE_TYPE
import com.symbol.emdk.barcode.BarcodeManager
import com.symbol.emdk.barcode.ScanDataCollection
import com.symbol.emdk.barcode.Scanner
import com.symbol.emdk.barcode.Scanner.DataListener
import com.symbol.emdk.barcode.Scanner.StatusListener
import com.symbol.emdk.barcode.ScannerException
import com.symbol.emdk.barcode.ScannerResults
import com.symbol.emdk.barcode.StatusData
import live.trilord.zebra_scanner.ui.theme.ZebrascannerTheme

class MainActivity : ComponentActivity(), EMDKListener, StatusListener, DataListener {
    private var emdkManager: EMDKManager? = null
    private var barcodeManager: BarcodeManager? = null
    private var scanner: Scanner? = null
    private val scannerViewModel: ScannerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val results = EMDKManager.getEMDKManager(applicationContext, this)
        enableEdgeToEdge()
        setContent {
            ZebrascannerTheme {
                MainScreen(scannerViewModel)
            }
        }
    }

    override fun onOpened(emdkManager: EMDKManager?) {
        this.emdkManager = emdkManager
        initBarcodeManager()
        initScanner()
    }

    override fun onClosed() {
        emdkManager = null
        deInitScanner()
    }

    override fun onStatus(statusData: StatusData?) {
        // Check if the scanner status is IDLE and restart scanning
        statusData?.let {
            if (it.state == StatusData.ScannerStates.IDLE) {
                try {
                    scanner?.read()
                } catch (e: ScannerException) {
                    println(e.message)
                }
            }
        }
    }

    override fun onData(scanDataCollection: ScanDataCollection?) {
        scannerViewModel.updateScanData(scanDataCollection)
    }

    private fun initBarcodeManager() {
        emdkManager?.let {
            barcodeManager = it.getInstance(FEATURE_TYPE.BARCODE) as BarcodeManager
            if (barcodeManager == null) {
                Toast.makeText(this, "Barcode scanning is not supported.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun initScanner() {
        if (scanner == null && barcodeManager != null) {
            scanner = barcodeManager!!.getDevice(BarcodeManager.DeviceIdentifier.DEFAULT)
            scanner?.let {
                it.addDataListener(this)
                it.addStatusListener(this)
                it.triggerType = Scanner.TriggerType.HARD
                try {
                    it.enable()
                    it.read()
                } catch (e: ScannerException) {
                    println(e.message)
                }
            } ?: println("Failed to initialize the scanner device.")
        }
    }

    private fun deInitScanner() {
        scanner?.let {
            try {
                it.release()
            } catch (e: Exception) {
                println(e.message)
            }
            scanner = null
        }
    }
}

@Composable
fun MainScreen(scannerViewModel: ScannerViewModel = viewModel()) {
    val scanData = scannerViewModel.scanData.observeAsState("")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {


        Text(text = "Scanned Data: ${scanData.value}", modifier = Modifier.padding(top = 16.dp))
    }
}