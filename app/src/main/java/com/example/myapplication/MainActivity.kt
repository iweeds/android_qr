package com.example.myapplication

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private var nfcAdapter: NfcAdapter? = null
    private var nfcPendingIntent: PendingIntent? = null
    private val intentFiltersArray: Array<IntentFilter?> = arrayOfNulls(1)
    private val techListsArray: Array<Array<String>?> = arrayOfNulls(1)


    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(this@MainActivity, nfcPendingIntent, intentFiltersArray, techListsArray)

    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this@MainActivity)
    }

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

        readNfc()
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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("MainActivity", "call onNewIntent")

        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent?.action) {
            val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            Log.d("MainActivity", "call onNewIntent >> ${rawMessages}")


            if (rawMessages != null) {
                val messages = arrayOfNulls<NdefMessage>(rawMessages.size)
                for (i in rawMessages.indices) {
                    messages[i] = rawMessages[i] as NdefMessage
                }
                if (messages.isNotEmpty()) {
                    val payload = String(messages[0]?.records?.get(0)?.payload ?: byteArrayOf())
                    // payload를 처리하거나 표시하는 작업을 수행

                    Log.d("MainActivity", "payload")
                }
            }
        }
    }

    private fun readNfc() {

        nfcAdapter = NfcAdapter.getDefaultAdapter(applicationContext)
        if (nfcAdapter == null) {
            // NFC를 지원하지 않는 경우에 대한 처리
        } else {
            nfcPendingIntent = PendingIntent.getActivity(
                this, 0, Intent(this, javaClass)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), FLAG_MUTABLE
            )
            val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
            try {
                ndef.addDataType("text/plain")

                intentFiltersArray[0] = ndef
                techListsArray[0] = arrayOf("android.nfc.tech.Ndef")

            } catch (e: IntentFilter.MalformedMimeTypeException) {
                e.printStackTrace()
            }
        }
    }
}