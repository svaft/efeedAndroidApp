package com.example.myapplication

import android.Manifest
import android.Manifest.permission.BLUETOOTH_SCAN
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.PowerManager
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.forEach
import app.akexorcist.bluetotohspp.library.BluetoothSPP
import app.akexorcist.bluetotohspp.library.BluetoothSPP.AutoConnectionListener
import app.akexorcist.bluetotohspp.library.BluetoothState
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import java.util.Locale
import java.util.Timer
import kotlin.concurrent.schedule


//import java.util.*
//@Suppress("UNUSED_PARAMETER")
class MainActivity : AppCompatActivity() //, AdapterView.OnItemSelectedListener
//    , RotaryKnob.RotaryKnobListener
{
    private val viewModel: MyViewModel by viewModels()

    private var heartbitMessageCnt: Int = 0
    private var controllerConnectionState: Int =
        0 // 0 - bt not connected, 1 - bt connected, 2 - bt connected and get valid data from efeed
    private var alreadyGreenState: Boolean = false
    private var btConnectingBlink: Boolean = false

    lateinit var mfUpdIf: UpdateableFragment

    private var selectedTabNum: Int = 0
    private var outerCutSourceDiameterMod: Boolean = false
    private var waitDocFeedEnd: Boolean = false
    var cmdRecorded = false
    var cmdStarted = false
//    enum class Direction { Left, Right, Forward, Backward }
    var pauseProgramFlag: Boolean = false
//    var feedDirection: Direction = Direction.Left

    private var controllerX: Short = 0
    private var controllerZ: Short = 0

    private var mXlimbInitial: Boolean = true
    private var mXlimb = 0
    private var mZlimbInitial: Boolean = true
    private var mZlimb = 0

    lateinit var font: Typeface

    private var frLp = false
    private var flLp = false
    private var ffLp = false
    private var ftaLp = false
    private var fbLp = false

    private var fbChamferUp: Boolean = false
    private var fbChamfer = false
    private var fbChamferDbg = 0

//    private var mChatService: BluetoothChatService? = null

    private lateinit var bt: BluetoothSPP
    lateinit var adapter: ViewPagerAdapter


    fun putLog(value: String) {
        val fragment: confFragment = adapter.getItem(4) as confFragment

        fragment.putLog(value)
        return
//        log.add("\r\n $value")
//        if (mutex.tryAcquire()) {
//            try {
//                runOnUiThread {
//                    fragment.tvLog.text = log.toString()
////                    tvZLabel.text = "Z: %.${2}f".format(mZ)
////                    tvXLabel.text = "X: %.${2}f".format(mX)
//
//                    mutex.release()
//                }
//            } catch (ex: Exception) {
//                Log.i("---", "Exception in thread")
//                mutex.release()
//            }
//        }
    }

    override fun onPause() {
        super.onPause()
        // If the screen is off then the device has been locked
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        val isScreenOn: Boolean = powerManager.isInteractive

        if (!isScreenOn) {
            bt.stopAutoConnect()
            // The screen has been locked
            // do stuff...
        } else {
            bt.autoConnect("EFEED")
        }
    }


    // Register the permissions callback, which handles the user's response to the
// system permissions dialog. Save the return value, an instance of
// ActivityResultLauncher. You can use either a val, as shown in this snippet,
// or a lateinit var in your onAttach() or onCreate() method.
    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
            } else {

            }
        }


    var tcnt = 0

    lateinit var b: ActivityMainBinding

    @OptIn(ExperimentalStdlibApi::class)
    @SuppressLint("ClickableViewAccessibility", "WrongViewCast", "MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            b = ActivityMainBinding.inflate(layoutInflater)
        } catch (ex: Exception) {
            println(ex.message)
        }

        setContentView(b.root)

        supportActionBar!!.hide()

        adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(odFragment(0), "OD turn")
        adapter.addFragment(odFragment(1), "ID turn")
        adapter.addFragment(threadFragment(), "thread")
        adapter.addFragment(JogFragment(), "jog")
        adapter.addFragment(confFragment(), "...")
        b.viewPager.adapter = adapter
        b.viewPager.addOnPageChangeListener(TabLayoutOnPageChangeListener(b.tabLayout))

        mfUpdIf = adapter.getItem(0) as UpdateableFragment

        //bind the viewPager with the TabLayout.
        b.tabLayout.setupWithViewPager(b.viewPager)

        val tabs = b.tabLayout.getChildAt(0) as LinearLayout
        for (i in 0 until tabs.childCount) {
            val currentTab1 = tabs.getChildAt(i)
            if (i == 0) {
                currentTab1.setBackgroundColor(Color.rgb(0x6A, 0x6A, 0x6A))
            } else
                currentTab1.setBackgroundColor(Color.WHITE)

//            tabs.getChildAt(i).setOnLongClickListener {
//                for (i2 in 0 until tabs.childCount) {
//                    val currentTab = tabs.getChildAt(i2)
//                    if (i2 == selectedTabNum) {
//                        currentTab.setBackgroundColor(Color.rgb(0x6A, 0x6A, 0x6A))
//                        activeTabNum = selectedTabNum
//                        try {
//                            b.btMain.text = mfUpdIf.fabText()
//                            mfUpdIf.update()
//                        } catch (exception: Exception) {
//                            exception.message?.let { putLog(it) }
//                        }
//                    } else {
//
//                        currentTab.setBackgroundColor(Color.WHITE)
//                    }
//                }
//                true
//            }
        }

        b.tabLayout.setOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                b.viewPager.setCurrentItem(tab.position)
                tab.view.setBackgroundColor(Color.rgb(0x6A, 0x6A, 0x6A))
//                activeTabNum = tab.position
                selectedTabNum = tab.position
                mfUpdIf = adapter.getItem(tab.position) as UpdateableFragment
                mfUpdIf.update()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                tab.view.setBackgroundColor(Color.WHITE)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })

        when {
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED -> {
            }
            else -> {
                // You can directly ask for the permission.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(
                        arrayOf(Manifest.permission.BLUETOOTH_CONNECT, BLUETOOTH_SCAN),
                        PERMISSION_GRANTED
                    )
                }
            }
        }

        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
        if (!bluetoothAdapter!!.isEnabled) {
            bluetoothAdapter.enable()
            while (!bluetoothAdapter.isEnabled) {
                tcnt++
                Thread.sleep(100)
                if (tcnt > 40) { // finish app if bluetooth cant enable more than 4sec
                    finish()
                    return
                }
            }
        }
        /**
         * The Handler that gets information back from the BluetoothChatService
         */
        val mHandler: Handler = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    Constants.MESSAGE_STATE_CHANGE -> when (msg.arg1) {
                        BluetoothChatService.STATE_CONNECTED -> {
//                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName))
//                            mConversationArrayAdapter.clear()
                        }

                        BluetoothChatService.STATE_CONNECTING -> {}//setStatus(R.string.title_connecting)
                        BluetoothChatService.STATE_LISTEN -> {}
                        BluetoothChatService.STATE_NONE -> {} //  setStatus(R.string.title_not_connected ) }
                    }

                    Constants.MESSAGE_WRITE -> {
                        // construct a string from the buffer
                        val writeMessage = msg.obj.toString() // String(msg.obj)
                        //mConversationArrayAdapter.add("Me:  $writeMessage")
                    }

                    Constants.MESSAGE_READ -> {
                        // construct a string from the valid bytes in the buffer
                        //val readMessage = String(msg.obj, 0, msg.arg1)
                        //mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage)
                    }

                    Constants.MESSAGE_DEVICE_NAME -> {
                        // save the connected device's name
                        //mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME)
                    }

                    Constants.MESSAGE_TOAST -> {
                    }
                }
            }
        }

        try {
            font = ResourcesCompat.getFont(this, R.font.f2)!!
            //val tf = Typeface.createFromAsset(assets, "font/f2.ttf");

            b.textViewXr.typeface = font
            b.textViewZ.typeface = font
            b.tvZLabel.typeface = font
            b.tvXLabel.typeface = font
            b.tvXLabelGlobal.typeface = font
            b.tvZLabelGlobal.typeface = font
        } catch (ex: Exception) {
            println(ex.message)
        }

//        // Create the observer which updates the UI.
//        val nameObserver = Observer<String> { newName ->
//            b.tvFeedValue.text = newName
//        }
        viewModel.currentFeed.observe(this) { newName ->
            b.tvFeedValue.text = newName
        }

        viewModel.internalCut.observe(this) { value ->
            if(value == 0){
                b.tvFeedText.text = "OD:"
            } else{
                b.tvFeedText.text = "ID:"
            }
            updateMainIcon()
        }



        b.btChamfer.apply {
            setOnLongClickListener {
                fbChamfer = true
                fbChamferUp = false
                putLog("easy chamfer")
                easyChamfer()
                true
            }
            setOnTouchListener { _: View?, event: MotionEvent ->
                if (event.action == MotionEvent.ACTION_UP && fbChamfer) {
                    putLog("chamfer longpress end")
                    fbChamferUp = true
                }
                false
            }
        }
        b.btPause.setOnLongClickListener {
            putLog("go home")
            sendCommand("!1") // yankee go home! todo unclear what target coordinates of home pos, remove button on set it to zero XZ by limb?
        }
        b.btStop.setOnLongClickListener {
            sendCommand("!R")
            putLog("stop longpress")
            true
        }
        b.btMain.apply {
            updateMainIcon()
            setBackgroundColor(Color.GREEN)

            setOnLongClickListener {
                mfUpdIf.programMainLP()
                true
            }
            setOnClickListener {
                mfUpdIf.programMainClick()
            }
        }

        b.tvXLabelGlobal.setOnEditorActionListener(
            OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event == null || !event.isShiftPressed) {
                        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
//                        outerCutTargetDiameterMod = true
                        onOuterCutSourceDiameterClick(v)
                        v.text =
                            String.format(locale = Locale.ROOT, "%.2f", v.text.toString().toFloat())
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        return@OnEditorActionListener true // consume.
                    }
                }
                false // pass on to other listeners.
            }
        )

        b.buttonTest.setOnClickListener{
            sendCommand("G94")
            sendCommand("G01 Z-10 F100")
            sendCommand("G01 X-1 F100")
            sendCommand("G01 X1 Z10 F100")

        }
        b.tvXLabel.setOnLongClickListener {
            mXlimb = 0
            b.tvXLabel.text = "0.00"
            putLog("zero X")
            true
        }
        b.tvZLabel.setOnLongClickListener {
            mZlimb = 0
            b.tvZLabel.text = "0.00"
            putLog("zero Z")
            true
        }
        b.btFastTakeAway.apply {
            setOnLongClickListener {
                putLog("G00 Z100 X30")
                sendCommand("G00 Z100 X30")
                ftaLp = true
                true
            }
            setOnTouchListener { _: View?, event: MotionEvent ->
                if (event.action == MotionEvent.ACTION_UP && ftaLp) {
                    sendCommand("!S")
                    putLog("!S")
                    ftaLp = false
                }
                false
            }
        }

        b.btFF.apply {
            setOnLongClickListener {
                putLog("FF longpress")
                sendCommand("G00 X-200")
                ffLp = true
                true
            }
            setOnClickListener{
                viewModel.feedDirection.value = Direction.Forward
                updateMainIcon()
            }

            setOnTouchListener { _: View?, event: MotionEvent ->
                if (event.action == MotionEvent.ACTION_UP && ffLp) {
                    sendCommand("!S")
                    putLog("FF longpress end")
                    ffLp = false
                }
                false
            }
        }
        b.btFB.apply {
            setOnLongClickListener {
                putLog("FB longpress")
                sendCommand("G00 X200")
                fbLp = true
                true
            }
            setOnClickListener{
                viewModel.feedDirection.value = Direction.Backward
                updateMainIcon()
            }

            setOnTouchListener { _: View?, event: MotionEvent ->
                if (event.action == MotionEvent.ACTION_UP && fbLp) {
                    sendCommand("!S")
                    putLog("FB longpress end")
                    fbLp = false
                }
                false
            }
        }
        b.btFR.apply {
            setOnLongClickListener {
                putLog("FR longpress")
                sendCommand("G00 Z200")
                frLp = true
                true
            }
            setOnClickListener{
                viewModel.feedDirection.value = Direction.Right
                updateMainIcon()
            }

            setOnTouchListener { _: View?, event: MotionEvent ->
                if (event.action == MotionEvent.ACTION_UP && frLp) {
                    sendCommand("!S")
                    putLog("FR longpress end")
                    frLp = false
                }
                false
            }
        }
        b.btFL.apply {
            setOnLongClickListener {
                sendCommand("G00 Z-200")
                putLog("FL longpress")
                flLp = true
                true
            }

            setOnClickListener{
                viewModel.feedDirection.value = Direction.Left
                updateMainIcon()
            }

            setOnTouchListener { _: View?, event: MotionEvent ->
                if (event.action == MotionEvent.ACTION_UP && flLp) {
                    sendCommand("!S")
                    putLog("FL longpress end")
                    flLp = false
                }
                false
            }
        }


        bt = BluetoothSPP(applicationContext)
        if (!bt.isBluetoothAvailable) {
            putLog("BT is not available!")// any command for bluetooth is not available
        }
        putLog("try to start BT")
        Log.i("OnCreate", "connectBT() is called")
        if (bt.isBluetoothAvailable) {
            bt.setupService()
            bt.startService(BluetoothState.DEVICE_OTHER)

            bt.setBluetoothStateListener(object : BluetoothSPP.BluetoothStateListener {
                override fun onServiceStateChanged(state: Int) {
                    when (state) {
                        BluetoothState.STATE_CONNECTED -> {
                            controllerConnectionState = 1
                            updateUIbyConnectionState()
                        }

                        BluetoothState.STATE_CONNECTING -> {
                            controllerConnectionState = 0
                            b.btStateView.setBackgroundColor(Color.RED)
                            b.btStateView.setImageResource(R.drawable.baseline_bluetooth_searching_24)
                            putLog("BT STATE_CONNECTING")
                        }

                        BluetoothState.STATE_LISTEN -> {
                            putLog("BT STATE_LISTEN")
                            controllerConnectionState = 0
                            b.btStateView.setBackgroundColor(Color.RED)
                            b.btStateView.setImageResource(R.drawable.baseline_bluetooth_searching_24)
                        }

                        BluetoothState.STATE_NONE -> {
                            controllerConnectionState = 0
                            b.btStateView.setBackgroundColor(Color.RED)
                            b.btStateView.setImageResource(R.drawable.baseline_bluetooth_searching_24)
                            putLog("BT STATE_NONE")
                        }
                    }
                }
            })

            bt.setAutoConnectionListener(object : AutoConnectionListener {
                override fun onNewConnection(name: String, address: String) {
                    putLog("BT: connecting to $name")
                }

                override fun onAutoConnectionStarted() {
                    putLog("AutoConnectionStarted")
                    b.constraintLayout.forEach {
                    }
                }
            })
            bt.autoConnect("EFEED")

            bt.setOnDataReceivedListener { data, message ->
                if (message.startsWith("!!!") && ( message[3] in 'A' .. 'F' || message[3] in '0' .. '9'  ) ) {
                    heartbitMessageCnt++
                    if (controllerConnectionState != 2) {
                        alreadyGreenState = false
                        controllerConnectionState = 2
                        updateUIbyConnectionState()
                    }
                    try {
                        extractXZ(data)
                    } catch (ex: Exception) {
                        putLog("response: exception $message")
                        Log.i("---", "Exception in thread")
                    }

                } else if (message.startsWith("!!!")) {
                    processMessage(data, message)
                }
                if (!message.startsWith("!!!!") && !message.startsWith("!!!"))
                    putLog("response: message $message")
            }
            putLog(tcnt.toString())

        }

        Timer().schedule(0, 1000) {
            if (heartbitMessageCnt < 5) {
                if (controllerConnectionState == 2) { // poor connection link, change to yellow
                    runOnUiThread { b.btStateView.setBackgroundColor(Color.YELLOW) }
                    alreadyGreenState = false
                } else if (controllerConnectionState == 0) {
                    if (btConnectingBlink) {
                        btConnectingBlink = false
                        runOnUiThread { b.btStateView.setImageResource(R.drawable.baseline_bluetooth_searching_24) }
                    } else {
                        btConnectingBlink = true
                        runOnUiThread { b.btStateView.setImageResource(R.drawable.baseline_bluetooth_24) }
                    }
                }
            } else {
                if (!alreadyGreenState) {
                    alreadyGreenState = true
                    runOnUiThread { b.btStateView.setBackgroundColor(Color.GREEN) }
                }
            }
            heartbitMessageCnt = 0
        }
    }

    private fun updateUIbyConnectionState() {
        when (controllerConnectionState) {
            0 -> {
                b.btStateView.setBackgroundColor(Color.RED)
                b.btStateView.setImageResource(R.drawable.baseline_bluetooth_searching_24)
            }

            1 -> {
                b.btStateView.setBackgroundColor(Color.YELLOW)
                b.btStateView.setImageResource(R.drawable.baseline_bluetooth_connected_24)
                putLog("BT STATE_CONNECTED")
                b.constraintLayout.forEach {
                    it.isEnabled = true
                }
            }

            2 -> {
                b.btStateView.setBackgroundColor(Color.GREEN)
                b.btStateView.setImageResource(R.drawable.baseline_bluetooth_connected_24)
            }
        }

    }

    fun disableUnselectedTabs(){
        for (i in 0..b.tabLayout.tabCount-2)
            if(i!=selectedTabNum)  b.tabLayout.getTabAt(i)?.view?.isEnabled = false
    }

    fun enableTabs(){
        for (i in 0..b.tabLayout.tabCount-2) b.tabLayout.getTabAt(i)?.view?.isEnabled = true
    }
    @OptIn(ExperimentalStdlibApi::class)
    private fun extractXZ(data: ByteArray, eom: Boolean = false) {
        if(!eom){
            val stateFlags: Int = String(data, 3, 1, Charsets.UTF_8).hexToByte().toInt()
            val bit0 = stateFlags.shr(0).and(1)
            val bit1 = stateFlags.shr(1).and(1)
            val bit2 = stateFlags.shr(2).and(1)
//        val bit3 = stateFlags.shr(3).and(1)

            if(viewModel.mG90G91.value != bit0){// uint8_t G90G91 0 - absolute distance mode, 1 - incremental distance mode
                viewModel.mG90G91.value = bit0
            }
            if(viewModel.mG94G95.value != bit1){ //0 - unit per min, 1 - unit per rev
                viewModel.mG94G95.value = bit1
            }
            if(viewModel.internalCut.value != bit2){ // uint8_t ODID 0 - outside turning, 1 - inside turning
                viewModel.internalCut.value = bit2
            }
        } else{
  //          mfUpdIf.eom()
        }

        val z: Short = String(data, 4, 4, Charsets.UTF_8).hexToShort()
        val x: Short = String(data, 8, 4, Charsets.UTF_8).hexToShort()
        if (z != controllerZ || x != controllerX)
            putLog("x: ${x.toString()} z: ${z.toString()}")
        if (z != controllerZ) {
            val delta = controllerZ - z
            if (mZlimbInitial)
                mZlimbInitial = false
            else
                mZlimb += delta
            controllerZ = z
//            mZ = z/160.0f
//            val getZ = String.format("%.2f", mZ).replace(',','.') //diameter mode, dive by 50
            b.tvZLabel.text = String.format(locale = Locale.ROOT, "%.2f", mZlimb / 160.0f)
            b.tvZLabelGlobal.setText( String.format(locale = Locale.ROOT, "%.2f", controllerZ / 160.0f))
        }

        if (x != controllerX) {
            val delta = controllerX - x
            if (mXlimbInitial)
                mXlimbInitial = false
            else
                mXlimb += delta
            controllerX = x
//            mX = x/50.0f
//            val getX = String.format("%.2f", mX).replace(',','.') //diameter mode, dive by 50
            b.tvXLabel.text = String.format(locale = Locale.ROOT, "%.2f", mXlimb / 100.0f)
            b.tvXLabelGlobal.setText( String.format(locale = Locale.ROOT, "%.2f", controllerX / 50.0f)) //.replace(',','.')
//            findViewById<TextView>(R.id.tvOuterCutSourceDiameter).text = getX
        }
    }


    fun updateMainIcon() {
        when(viewModel.feedDirection.value){
            Direction.Left -> {
                if (viewModel.internalCut.value == 0)
                    (b.btMain as MaterialButton).setIconResource(R.drawable.left_outer)
                else
                    (b.btMain as MaterialButton).setIconResource(R.drawable.left_inner)
            }
            Direction.Right -> {
                if (viewModel.internalCut.value == 0)
                    (b.btMain as MaterialButton).setIconResource(R.drawable.right_outer)
                else
                    (b.btMain as MaterialButton).setIconResource(R.drawable.right_inner)
            }
            Direction.Forward -> {
                (b.btMain as MaterialButton).setIconResource(R.drawable.front_and_return)
            }
            Direction.Backward -> {
                (b.btMain as MaterialButton).setIconResource(R.drawable.back_and_return)
            }
            null -> TODO()
        }
    }

    private fun processMessage(dataBA: ByteArray, message: String) {
        putLog("response: message $message")
        //      Integer.decode( message.substring(4,11))
        try {
            val data = message.substring(4, 12)
            when (message[3]) {
                'r' -> {
                    putLog("reset event, fw build: ${Integer.parseUnsignedInt(data, 16)}")
                    mfUpdIf.processEvent("reset")
                    cmdRecorded = false
                    cmdStarted = false
                    b.btMain.setBackgroundColor(Color.GREEN)
                    b.btPause.setBackgroundColor(Color.GREEN)
                    outerCutSourceDiameterMod = false
                    mXlimb = 0
                    mZlimb = 0
                    mZlimbInitial = true
                    mXlimbInitial = true

                }
                'S' -> putLog("on stop: ${Integer.parseUnsignedInt(data, 16) / 160.0f}")// stop
                'e' -> {
                    cmdStarted = false
                    mfUpdIf.processEvent("eom")
                    extractXZ(dataBA, true)
                    processProgram()
                    if (fbChamfer)
                        easyChamfer()
                    if (viewModel.threadMode.value == true)
                        b.btMain.setText(R.string.thread)
                    else
                        b.btMain.setText(R.string.feed)
                }

                'f' -> {
                    val feed = Integer.parseUnsignedInt(data, 16)
                    putLog("feed 1616: ${feed.toString()}")
                    val feedF = 1474560.0f / feed
                    putLog("feed: ${feedF.toString()}")
                    b.tvFeedValue.text = String.format(locale = Locale.ROOT, "%.1f", feedF)
                }

//                'F' -> {
//                    val i = java.lang.Long.parseLong(data, 16);
//                    val f = java.lang.Float.intBitsToFloat(i.toInt());
//                }
            }

        } catch (ex: Exception) {
            putLog("response: exception $message")
            Log.i("---", "Exception in thread")
        }

    }
    private fun easyChamfer() {
        val iCh = 4
        when (fbChamferDbg) {
            0 -> sendCommand("G01 Z$iCh X-${iCh * 2} F${b.tvFeedValue.text}")
            1 -> sendCommand("G00 X${iCh * 2}")
            2 -> {
                if (fbChamferUp) { //check if we want to stop chamfer mode
                    fbChamfer = false
                    fbChamferUp = false
                    sendCommand("G00 Z-$iCh")
                } else {
                    sendCommand("G00 Z-$iCh.4")
                }
            }
        }
        if (++fbChamferDbg > 2)
            fbChamferDbg = 0
    }
    private fun processProgram() {
        mfUpdIf.processProgram()
        if (!pauseProgramFlag && cmdRecorded) {
            if (waitDocFeedEnd) {
                putLog("DOC complete, repeat main cut")
                sendCommand("!3")
                waitDocFeedEnd = false
            } else {
                putLog("move DOC:")
                if (viewModel.internalCut.value == 1) {
                    sendCommand("G00 X -${viewModel.getDOC()}")
                } else
                    sendCommand("G00 X ${viewModel.getDOC()}")
                waitDocFeedEnd = true
            }
        } else {
            if (cmdRecorded)
                putLog("program on pause")
        }
    }
    override fun onDestroy() {
//        countdownTimer.cancelTimer()
        bt.disconnect()
        bt.stopAutoConnect()
        bt.stopService()
        Log.e("MY_APP_TAG", "onDestroy() is called")
        super.onDestroy()
    }
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        Log.e("MY_APP_TAG", "onWindowFocusCHanged() is called")
    }
    override fun onStart() {
        super.onStart()
        Log.v("STATE", "onStart() is called")
        if (!bt.isBluetoothEnabled) {
            putLog("BT not enabled!")
        } else {
            putLog("BT enabled, ok")
        }
    }

    fun buildCmd(): String {
        val out = mfUpdIf.buildCmd()
        putLog("cmd:$out")
        return out
    }
    fun onEditTextClickSelectAll(v: View) {
        (v as EditText).selectAll()
    }
    fun onJogShow(v: View) {
        val newFragment: JogFragment = JogFragment.newInstance()
//        newFragment.show(supportFragmentManager, "dialog")
    }
    private fun onOuterCutSourceDiameterClick(v: View) {
// calibrate X axis by measure real position of lathe tool(diameter of part) and send it to lathe controller
//        outerCutSourceDiameterMod = true
        var fDia = (v as EditText).text.toString().toFloat()
        var iRadius = (fDia * 50f).toInt()
        controllerX = iRadius.toShort()
        mXlimb = 0
        putLog("radius: ${String.format("%08X", iRadius)}")
        sendCommand("!=X${String.format("%08X", iRadius)}")
//        (v as EditText).selectAll()
    }
    fun onPauseClick(v: View) {
        pauseProgramFlag = !pauseProgramFlag
        if (pauseProgramFlag) {
            b.btPause.setBackgroundColor(Color.RED)
//            findViewById<Button>(R.id.btPause).setBackgroundColor(Color.RED)
        } else {
            b.btPause.setBackgroundColor(Color.GREEN)
//            findViewById<Button>(R.id.btPause).setBackgroundColor(Color.GREEN)
            processProgram()
        }
    }
    fun onStop(v: View) {
        sendCommand("!S")
    }

    private var miniLogStr = ""
    fun sendCommand(cmd: String?): Boolean {
        miniLogStr = "$cmd<-$miniLogStr".take(30)
        b.tvLogMini.text = miniLogStr
        putLog("send: $cmd")
        bt.send("$cmd\n", false)
        return true
    }
    fun atoui64(str: ByteArray): Int {
        var res = 0 // Initialize result
        for (i in 0..5) {
            val decode = if (str[i] - '0'.code.toByte() > 9) {
                str[i] - 'A'.code.toByte() + 10
            } else {
                str[i] - '0'.code.toByte()
            }
            res = res * 64 + decode;
        }
        return res
    }
}
