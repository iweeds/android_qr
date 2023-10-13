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
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private var nfcAdapter: NfcAdapter? = null
    private var nfcPendingIntent: PendingIntent? = null
    private var intentFiltersArray: Array<IntentFilter?> = arrayOfNulls(1)
    private var techListsArray: Array<Array<String>?> = arrayOfNulls(1)



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this@MainActivity)

        setupNfcFilter()
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

        Log.d(javaClass.simpleName, "call enableForegroundDispatch!")
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this@MainActivity)
        Log.d(javaClass.simpleName, "call disableForegroundDispatch")

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(javaClass.simpleName, "call onNewIntent >> action >>  ${intent?.action}")
        processIntent(intent)
    }

    private fun setupNfcFilter() {

        if (nfcAdapter == null) {
            // NFC를 지원하지 않는 경우에 대한 처리
            Log.d(javaClass.simpleName, "call nfcAdapter == null")
        } else {
            Log.d(javaClass.simpleName, "call nfcAdapter != null")

            val intent = Intent(this@MainActivity, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            nfcPendingIntent = PendingIntent.getActivity(this@MainActivity, 9999, intent, FLAG_MUTABLE)

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

        if (ndef != null) {
            ndef.connect()
            val payloadBytes = ndef.ndefMessage.records[0].payload
            if (payloadBytes != null) {
                val payload = String(payloadBytes, StandardCharsets.UTF_8)
                Log.d(javaClass.simpleName, "Payload: $payload")
                Toast.makeText(this, "payload >>. $payload", Toast.LENGTH_LONG).show()
            } else {
                Log.d(javaClass.simpleName, "Payload is empty")
            }
        }
    }
}