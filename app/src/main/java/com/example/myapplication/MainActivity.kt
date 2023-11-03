package com.example.myapplication

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.repo.PointRepo
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets
import java.text.DecimalFormat


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private var nfcAdapter: NfcAdapter? = null
    private var nfcPendingIntent: PendingIntent? = null
    private var intentFiltersArray: Array<IntentFilter?> = arrayOfNulls(1)
    private var techListsArray: Array<Array<String>?> = arrayOfNulls(1)
    
    private val TAG = javaClass.simpleName


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)


        nfcAdapter = NfcAdapter.getDefaultAdapter(this@MainActivity)

        setupNfcFilter()

        lifecycleScope.launch(Dispatchers.IO) {
            val pointList = selectAllPoint()
            val sumPoint = pointList.sumOf { it.earn.toLong() }

            Log.d(TAG, "sum point >> $sumPoint")
            setupUI(sumPoint)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }


    override fun onResume() {
        super.onResume()

        /*
        nfcAdapter?.enableForegroundDispatch(
            this@MainActivity,
            nfcPendingIntent,
            intentFiltersArray,
            techListsArray
        )*/

        nfcAdapter?.enableForegroundDispatch(
            this,
            nfcPendingIntent,
            null,
            null
        )

        Log.d(TAG, "call enableForegroundDispatch!")
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this@MainActivity)
        Log.d(TAG, "call disableForegroundDispatch")

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "call onNewIntent >> action >>  ${intent?.action}")
        processIntent(intent)
    }

    private fun setupNfcFilter() {

        if (nfcAdapter == null) {
            // NFC를 지원하지 않는 경우에 대한 처리
            Log.d(TAG, "call nfcAdapter == null")
        } else {
            Log.d(TAG, "call nfcAdapter != null")

            val intent =
                Intent(this@MainActivity, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            nfcPendingIntent =
                PendingIntent.getActivity(this@MainActivity, 9999, intent, FLAG_MUTABLE)

            val filter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
                try {
                    addDataType("*/*")
                } catch (e: IntentFilter.MalformedMimeTypeException) {
                    throw RuntimeException("fail", e)
                }
            }

            intentFiltersArray = arrayOf(filter)
            techListsArray = arrayOf(arrayOf(Ndef::class.java.name))
        }
    }

    private fun processIntent(intent: Intent?) {

        if (NfcAdapter.ACTION_NDEF_DISCOVERED != intent?.action) {
            return
        }

        val ndef = Ndef.get(intent.getParcelableExtra(NfcAdapter.EXTRA_TAG) as Tag?)
        if (ndef == null) {
            Log.d(TAG, "ndef is null")
            return
        }

        ndef.connect()

        if (ndef.ndefMessage == null) {
            Log.d(TAG, "nfc message is empty")
            return
        }

        val payloadBytes = ndef.ndefMessage.records[0].payload
        if (payloadBytes != null) {
            val payload = String(payloadBytes, StandardCharsets.UTF_8)
            Log.d(TAG, "Payload: $payload")
            Toast.makeText(this, "payload >>. $payload", Toast.LENGTH_LONG).show()

            val point = Gson().fromJson(payload, Point::class.java)
            insertPoint(point)

        } else {
            Log.d(TAG, "Payload is empty")
        }
    }


    private fun setupUI(earnPoint: Long) {

        lifecycleScope.launch(Dispatchers.Main) {
            val hostFragment: NavHostFragment? =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment?

            val firstFragment = hostFragment?.childFragmentManager?.fragments?.get(0) as FirstFragment
            val decimalFormat = DecimalFormat("#,###")
            firstFragment.setupFragmentUI(decimalFormat.format(earnPoint))
        }
    }


    /**
     * db 에 포인트 저장
     */
    fun insertPoint(point: Point) {
        lifecycleScope.launch(Dispatchers.IO) {
            Log.d(TAG, "insertPoint >> $point")

            val dao = PointRepo.getInstance(application).pointDao
            dao.insertPoint(point)


            val sumEarn = totalPoint()
            Log.d(TAG, "sumEarn >> $sumEarn")
            setupUI(sumEarn)

        }
    }

    fun totalPoint(): Long {
        return selectAllPoint().sumOf { it.earn.toLong() }
    }


    private fun selectAllPoint(): List<Point> {

        Log.d(TAG, "selectAllPoint!")

        val dao = PointRepo.getInstance(application).pointDao
        val pointList = dao.getAll()

        pointList.forEach {
            Log.d(TAG, "selectAllPoint >> point >> $it!")
        }

        return pointList
    }
}