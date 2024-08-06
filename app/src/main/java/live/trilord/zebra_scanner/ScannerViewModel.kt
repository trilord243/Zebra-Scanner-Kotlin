package live.trilord.zebra_scanner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.symbol.emdk.barcode.ScanDataCollection
import com.symbol.emdk.barcode.ScannerResults


//ViewModel para manejar  la data
class ScannerViewModel : ViewModel() {
    private val _scanData = MutableLiveData<String>()
    val scanData: LiveData<String> = _scanData

    fun updateScanData(scanDataCollection: ScanDataCollection?) {
        scanDataCollection?.let {
            if (it.result == ScannerResults.SUCCESS) {
                for (data in it.scanData) {
                    _scanData.postValue(data.data)
                }
            }
        }
    }
}