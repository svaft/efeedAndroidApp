package com.example.myapplication

import android.Manifest
import android.Manifest.permission.BLUETOOTH_SCAN
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Message
import android.os.PowerManager
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.viewpager.widget.ViewPager
import app.akexorcist.bluetotohspp.library.BluetoothSPP
import app.akexorcist.bluetotohspp.library.BluetoothSPP.AutoConnectionListener
import app.akexorcist.bluetotohspp.library.BluetoothState
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import kotlinx.coroutines.sync.Semaphore
import org.apache.commons.collections4.queue.CircularFifoQueue
import java.util.Locale
import java.util.Timer
import kotlin.concurrent.schedule


//import java.util.*
//@Suppress("UNUSED_PARAMETER")
class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener
//    , RotaryKnob.RotaryKnobListener
{
//    private val viewModel: MyViewModel by viewModels()

    //    private var jogLP: Boolean = false
    private var heartbitMessageCnt: Int = 0
    private var controllerConnectionState: Int =
        0 // 0 - bt not connected, 1 - bt connected, 2 - bt connected and get valid data from efeed
    private var alreadyGreenState: Boolean = false
    private var btConnectingBlink: Boolean = false

    lateinit var mfUpdIf: UpdateableFragment
    private var activeTabNum: Int = 0
    private var selectedTabNum: Int = 0
    private lateinit var pager: ViewPager // creating object of ViewPager
    private lateinit var tab: TabLayout  // creating object of TabLayout
    private lateinit var bar: Toolbar    // creating object of ToolBar

    private var ma = this

    private var outerCutSourceDiameterMod: Boolean = false
    private var outerCutTargetDiameterMod: Boolean = false

    //    private var pauseProgramFlag: Boolean = false
    var internalCut: Boolean = false
    private var vaitDocFeedEnd: Boolean = false

    private var cmdRecorded = false
    private var cmdStarted = false
    private var feedUnitG95 = true

    val JOG_X = 0
    val JOG_Z = 1

    enum class Direction { Left, Right, Forward, Backward }

    //    var feedDirectionV: Direction = Direction.Left
//    var outD: Float = 0f
    var pauseProgramFlag: Boolean = false
    var jogLP: Boolean = false
    var threadMode: Boolean = false
    var feedDirection: Direction = Direction.Left


//    private var feedDirection = Direction.Left

    private var log = CircularFifoQueue<String>(20)

    private var controllerX: Short = 0
    private var controllerZ: Short = 0

//    private var mX = 0.0f
//    private var mZ = 0.0f

    private var mXlimbInitial: Boolean = true
    private var mXlimb = 0
    private var mZlimbInitial: Boolean = true
    private var mZlimb = 0
    private var mXglobal = 0
    private var mZglobal = 0


    private lateinit var viewsMap: Map<Int, View>
    private var currentView = 2

    //    private var mainLp = false
    private var frLp = false
    private var flLp = false
    private var ffLp = false
    private var ftaLp = false
    private var fbLp = false

    private var fbChamferUp: Boolean = false
    private var fbChamfer = false
    private var fbChamferDbg = 0


    //    private var threadMode = false
    private var mMetricUnit = true
    private val mutex: Semaphore = Semaphore(1)
//    private var mChatService: BluetoothChatService? = null


    private lateinit var countdownTimer: CountDownTimer

    //   lateinit var  jog: RotaryKnob// by lazy { findViewById<RotaryKnobView3>(R.id.jog) }
    //    var timer = Timer()
    private lateinit var bt: BluetoothSPP
    lateinit var fab: MaterialButton
    private lateinit var FR: ImageButton
    private lateinit var FL: ImageButton
    private lateinit var FF: ImageButton
    private lateinit var FB: ImageButton
    private lateinit var FTA: ImageButton

    private lateinit var btStop: MaterialButton

    //    private lateinit var tvRead: TextView
    private lateinit var tvZLabel: TextView
    private lateinit var tvXLabel: TextView

    private lateinit var tvXLabelGlobal: TextView
    private lateinit var tvZLabelGlobal: TextView

    private lateinit var btStateView: ImageView

    private lateinit var tvLogMini: TextView


    //    private lateinit var tvScrewPitch: TextView
    lateinit var tvFeedValue: TextView
//    private lateinit var tvFeedLength: TextView


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

        val view = b.root
//        setContentView(R.layout.activity_main)
        setContentView(view)

        supportActionBar!!.hide()
        ma = this
        /*
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    when(it.feedDirectionV) {
                        com.example.myapplication.Direction.Left -> fab.setIconResource(R.drawable.ic_baseline_arrow_forward_24)
                        else -> {fab.setIconResource(R.drawable.ic_baseline_arrow_back_24)}
                    }
                    if(it.pauseProgramFlag)
                        findViewById<Button>(R.id.btPause).setBackgroundColor(Color.RED)
                    else
                        findViewById<Button>(R.id.btPause).setBackgroundColor(Color.GREEN)
                }
            }
        }
*/

        pager = findViewById(R.id.view_pager)
        tab = findViewById(R.id.tabLayout)
//        bar = findViewById(R.id.toolbar)
        adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(odFragment(0), "OD turn")
        adapter.addFragment(odFragment(1), "ID turn")
        adapter.addFragment(threadFragment(), "thread")
        adapter.addFragment(odFragment(2), "Face")
        adapter.addFragment(confFragment(), "...")
        pager.adapter = adapter

        //bind the viewPager with the TabLayout.
        tab.setupWithViewPager(pager)
        val tabs = tab.getChildAt(0) as LinearLayout

        for (i in 0 until tabs.childCount) {
            val currentTab1 = tabs.getChildAt(i)
            if (i == 0) {
                currentTab1.setBackgroundColor(Color.rgb(0x6A, 0x6A, 0x6A))
            } else
                currentTab1.setBackgroundColor(Color.WHITE)
            tabs.getChildAt(i).setOnLongClickListener {
                for (i2 in 0 until tabs.childCount) {
                    val currentTab = tabs.getChildAt(i2)
                    if (i2 == selectedTabNum) {
                        currentTab.setBackgroundColor(Color.rgb(0x6A, 0x6A, 0x6A))
                        activeTabNum = selectedTabNum

                        try {
                            fab.text = mfUpdIf.fabText()
                            mfUpdIf.update()
                        } catch (exception: Exception) {
                            exception.message?.let { putLog(it) }
                        }


                    } else
                        currentTab.setBackgroundColor(Color.WHITE)
                }
                true
            }
        }


//        countdownTimer = object : CountDownTimer(30000, 1000) { // 30 seconds, 1-second intervals
//            override fun onTick(millisUntilFinished: Long) {
//                val secondsRemaining = millisUntilFinished / 1000
//                println("Seconds remaining: $secondsRemaining")
//            }
//
//            override fun onFinish() {
//                println("Timer finished!")
//            }
//        }

        mfUpdIf = adapter.getItem(0) as UpdateableFragment
        pager.addOnPageChangeListener(TabLayoutOnPageChangeListener(tab))
        tab.setOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
//                activeTabNum = tab.position
                selectedTabNum = tab.position
                mfUpdIf = adapter.getItem(tab.position) as UpdateableFragment
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
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

//            ActivityCompat.shouldShowRequestPermissionRationale(
//                this, Manifest.permission.BLUETOOTH_CONNECT
//            ) -> {
//            }

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

        btStateView = findViewById(R.id.btStateView)

//        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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

//        mChatService = BluetoothChatService(this, mHandler)


// todo jog
//        findViewById<ConstraintLayout>(R.id.constraintLayoutJog).isVisible = false
//        val jogX = findViewById<RotaryKnob>(R.id.knob)
//        jogX.isVisible = false
//        jogX.listener = this
//        jogX.setOnTouchListener { _: View?, event: MotionEvent ->
//            if (event.action == MotionEvent.ACTION_UP && jogLP) {
//                jogLP = false
//                sendCommand("!S")
//            }
//            false
//        }
//        val jogZ = findViewById<RotaryKnob>(R.id.knobZ)
//        jogZ.isVisible = false
//        jogZ.listener = this
//        jogZ.setOnTouchListener { _: View?, event: MotionEvent ->
//            if (event.action == MotionEvent.ACTION_UP && jogLP) {
//                jogLP = false
//                sendCommand("!S")
//            }
//            false
//        }


        threadMode = false //(findViewById<View>(R.id.radioButton2) as RadioButton).isChecked

//        tvRead = findViewById(R.id.tvReadLog)
//        tvRead.movementMethod = ScrollingMovementMethod()

        FF = findViewById(R.id.btFF)
        FB = findViewById(R.id.btFB)
        FR = findViewById(R.id.btFastRight)
        FL = findViewById(R.id.btFastLeft)
        FTA = findViewById(R.id.btFastTakeAway)
        fab = findViewById(R.id.btMain)
        btStop = findViewById(R.id.button)

        tvLogMini = findViewById(R.id.tvLogMini)
        tvZLabel = findViewById(R.id.tvZLabel)
        tvXLabel = findViewById(R.id.tvXLabel)
        tvXLabelGlobal = findViewById<TextView>(R.id.tvXLabelGlobal)
        tvZLabelGlobal = findViewById<TextView>(R.id.tvZLabelGlobal)
        try {
            val font = ResourcesCompat.getFont(this, R.font.f2)
            //val tf = Typeface.createFromAsset(assets, "font/f2.ttf");

            findViewById<TextView>(R.id.textViewXr).typeface = font
            findViewById<TextView>(R.id.textViewZ).typeface = font
            tvZLabel.typeface = font
            tvXLabel.typeface = font
            tvXLabelGlobal.typeface = font
            tvZLabelGlobal.typeface = font
        } catch (ex: Exception) {
            println(ex.message)
        }


//        tvScrewPitch = findViewById(R.id.tvScrewPitch)
        tvFeedValue = findViewById(R.id.tvFeedValue)
//        tvFeedLength = findViewById(R.id.editTextFeedLength)
//
//        viewsMap = mapOf(
//            0 to findViewById<View>(R.id.viewOuterOneWay),
//            1 to findViewById<View>(R.id.viewOuterOneCycle),
//            2 to findViewById<View>(R.id.viewOuterProgram),
//            3 to findViewById<View>(R.id.viewInnerOneWay),
//            4 to findViewById<View>(R.id.viewInnerOneCycle),
//            5 to findViewById<View>(R.id.viewInnerProgram)
//        )
//
//        val spinner: Spinner = findViewById(R.id.spinner2)

        val btnChamfer = findViewById<ImageButton>(R.id.btChamfer)
        val btPause = findViewById<Button>(R.id.btPause)


// Create an ArrayAdapter using the string array and a default spinner layout

//        ArrayAdapter.createFromResource(
//            this,
//            R.array.loop_array,
//            android.R.layout.simple_spinner_item
//        ).also { adapter ->
//            // Specify the layout to use when the list of choices appears
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//            // Apply the adapter to the spinner
//            spinner.adapter = adapter
//        }
//        spinner.onItemSelectedListener = this
//
//        spinner.setSelection(currentView)
//
//        viewsMap.forEach { it.value.isVisible = false }
//        viewsMap[currentView]!!.isVisible = true


        btnChamfer.setOnLongClickListener {
            fbChamfer = true
            fbChamferUp = false
            putLog("easy chamfer")
            easyChamfer()
            true
//            sendCommand("!1") // yankee go home
        }

        btnChamfer.setOnTouchListener { _: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_UP && fbChamfer) {
                putLog("chamfer longpress end")
                fbChamferUp = true
            }
            false
        }


        btPause.setOnLongClickListener {
            putLog("go home")
            sendCommand("!1") // yankee go home! todo unclear what target coordinates of home pos, remove button on set it to zero XZ by limb?
        }


        btStop.setOnLongClickListener {
            sendCommand("!R")
            putLog("stop longpress")
            true
        }

        fab.setIconResource(R.drawable.ic_baseline_arrow_back_24)

        fab.setBackgroundColor(Color.GREEN)
        fab.setOnLongClickListener {
            putLog("reset record")
            cmdRecorded = false
            sendCommand("!S")
            fab.setBackgroundColor(Color.GREEN)
            btPause.setBackgroundColor(Color.GREEN)
            pauseProgramFlag = false
//            mainLp = true
            true
        }
        fab.setOnClickListener {
            if (cmdRecorded) {
                if (cmdStarted) {
                    putLog("stop current move")
                    sendCommand("!S") // stop current move
                } else {
                    cmdStarted = true
                    putLog("repeat or zero")
                    sendCommand("!3") //repeat last command or quick move to initial position
                    fab.setText(R.string.stop)
                }
            } else { // command not recorded yet
                if (cmdStarted) { // command started but not recorded for future replay
                    cmdStarted = false
                    cmdRecorded = true
                    fab.setBackgroundColor(Color.RED)
                    putLog("stop&save last move")
                    sendCommand("!2")
                    if (currentView == 2 || currentView == 5) { // we in program mode so set on pause to make sure that first cut was correct
                        pauseProgramFlag = true
                        findViewById<Button>(R.id.btPause).setBackgroundColor(Color.RED)
                    }
                    if (threadMode)
                        fab.setText(R.string.thread)
                    else
                        fab.setText(R.string.feed)
                } else { // build command with active TAB config and send it to lathe
                    val cmd = buildCmd()
                    sendCommand(cmd)
                    cmdStarted = true
                    putLog(cmd)
                    fab.setText(R.string.sns)
                }
            }
        }


        (findViewById<View>(R.id.tvXLabelGlobal) as TextView).setOnEditorActionListener(
            OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event == null || !event.isShiftPressed) {
                        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
//                        outerCutTargetDiameterMod = true
                        onOuterCutSourceDiameterClick(v)

                        val getX =
                            String.format(locale = Locale.ROOT, "%.2f", v.text.toString().toFloat())
                        tvXLabelGlobal.text = getX
//                        findViewById<TextView>(R.id.tvOuterCutSourceDiameter).text = getX
                        // the user is done typing.
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        return@OnEditorActionListener true // consume.
                    }
                }
                false // pass on to other listeners.
            }
        )


//
//
//        (findViewById<View>(R.id.tvXLabelGlobal) as EditText).setOnEditorActionListener(
//            OnEditorActionListener { v, actionId, event ->
//                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
//                    if (event == null || !event.isShiftPressed) {
//                        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
//                        outerCutTargetDiameterMod = true
//                        onOuterCutSourceDiameterClick(v)
//
//                        val getX = String.format("%.2f", v.text.toString().toFloat()).replace(',','.')
//                        tvXLabelGlobal.text = getX
//                        findViewById<TextView>(R.id.tvXLabelGlobal).text = getX
//                        // the user is done typing.
//                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
//                        return@OnEditorActionListener true // consume.
//                    }
//                }
//                false // pass on to other listeners.
//            }
//        )
//        findViewById<View>(R.id.tvOuterCutTargetDiameter).setOnFocusChangeListener{_, b -> outerCutTargetDiameterMod = true }
//        findViewById<View>(R.id.tvOuterCutSourceDiameter).setOnFocusChangeListener{v, b -> onOuterCutSourceDiameterClick(v) }

//        findViewById<View>(R.id.tvFeedUnit).setOnLongClickListener {
//            changeFeedUnit()
//            true
//        }

        tvXLabel.setOnLongClickListener {
            //        mX = 0.0f
            mXlimb = 0
            tvXLabel.text = "0.00"
            putLog("zero X")
            true
        }
        tvZLabel.setOnLongClickListener {
            //        mZ = 0.0f
            mZlimb = 0
            tvZLabel.text = "0.00"
            putLog("zero Z")
            true
        }
        FTA.setOnLongClickListener {
            putLog("G00 Z100 X30")
            sendCommand("G00 Z100 X30")
            ftaLp = true
            true
        }

        FTA.setOnTouchListener { _: View?, event: MotionEvent ->
//            if (event.action == MotionEvent.ACTION_DOWN) {
//                false
//            } else
            if (event.action == MotionEvent.ACTION_UP && ftaLp) {
                sendCommand("!S")
                putLog("!S")
                ftaLp = false
            }
            false
        }
        FF.setOnLongClickListener {
            putLog("FF longpress")
            sendCommand("G00 X-200")
            ffLp = true
            true
        }
        FF.setOnTouchListener { _: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_UP && ffLp) {
                sendCommand("!S")
                putLog("FF longpress end")
                ffLp = false
            }
            false
        }
        FB.setOnLongClickListener {
            putLog("FB longpress")
            sendCommand("G00 X200")
            fbLp = true
            true
        }
        FB.setOnTouchListener { _: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_UP && fbLp) {
                sendCommand("!S")
                putLog("FB longpress end")
                fbLp = false
            }
            false
        }
        FR.setOnLongClickListener {
            putLog("FR longpress")
            sendCommand("G00 Z200")
            frLp = true
            true
        }
        FR.setOnTouchListener { _: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_UP && frLp) {
                sendCommand("!S")
                putLog("FR longpress end")
                frLp = false
            }
            false
        }
        FL.setOnLongClickListener {
            sendCommand("G00 Z-200")
            putLog("FL longpress")
            flLp = true
            true
        }
        FL.setOnTouchListener { _: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_UP && flLp) {
                sendCommand("!S")
                putLog("FL longpress end")
                flLp = false
            }
            false
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
                            btStateView.setBackgroundColor(Color.RED)
                            btStateView.setImageResource(R.drawable.baseline_bluetooth_searching_24)
                            putLog("BT STATE_CONNECTING")
                        }

                        BluetoothState.STATE_LISTEN -> {
                            putLog("BT STATE_LISTEN")
                            controllerConnectionState = 0
                            btStateView.setBackgroundColor(Color.RED)
                            btStateView.setImageResource(R.drawable.baseline_bluetooth_searching_24)
                        }

                        BluetoothState.STATE_NONE -> {
                            controllerConnectionState = 0
                            btStateView.setBackgroundColor(Color.RED)
                            btStateView.setImageResource(R.drawable.baseline_bluetooth_searching_24)
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
                    findViewById<ConstraintLayout>(R.id.constraintLayout).forEach {
//                        it.isEnabled = false
                    }
                }
            })
            bt.autoConnect("EFEED")

            bt.setOnDataReceivedListener { data, message ->
//                if (message == "!w") {
//                    mX += 0.01f
//                } else if (message == "!s") {
//                    mX -= 0.01f
//                } else if (message == "!a") {
//                    mZ += 0.05f
//                } else if (message == "!d") {
//                    mZ -= 0.05f
//                } else
                if (message.startsWith("!!!!")) {
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
            putLog("${tcnt.toString()}")

        }

        val timer = Timer()
        timer.schedule(0, 1000) {
            if (heartbitMessageCnt < 5) {
                if (controllerConnectionState == 2) { // poor connection link, change to yellow
                    runOnUiThread{ btStateView.setBackgroundColor(Color.YELLOW) }
//                    btStateView.setBackgroundColor(Color.YELLOW)
                    alreadyGreenState = false
                } else if (controllerConnectionState == 0) {
                    if (btConnectingBlink) {
                        btConnectingBlink = false
                        runOnUiThread{ btStateView.setImageResource(R.drawable.baseline_bluetooth_searching_24) }
  //                      btStateView.setImageResource(R.drawable.baseline_bluetooth_searching_24)
                    } else {
                        btConnectingBlink = true
                        runOnUiThread{ btStateView.setImageResource(R.drawable.baseline_bluetooth_24) }
    //                    btStateView.setImageResource(R.drawable.baseline_bluetooth_24)
                    }
                }
            } else {
                if (!alreadyGreenState) {
                    alreadyGreenState = true
                    runOnUiThread{ btStateView.setBackgroundColor(Color.GREEN) }
                    //              btStateView.setBackgroundColor(Color.GREEN)
                }
            }
            heartbitMessageCnt = 0
//            println("Timer ticked!")
        }

    }

    private fun updateUIbyConnectionState() {
        when (controllerConnectionState) {
            0 -> {
                btStateView.setBackgroundColor(Color.RED)
                btStateView.setImageResource(R.drawable.baseline_bluetooth_searching_24)
//                putLog("BT STATE_CONNECTED")
//                findViewById<ConstraintLayout>(R.id.constraintLayout).forEach {
//                    it.isEnabled = true
            }

            1 -> {
                btStateView.setBackgroundColor(Color.YELLOW)
                btStateView.setImageResource(R.drawable.baseline_bluetooth_connected_24)
                putLog("BT STATE_CONNECTED")
                findViewById<ConstraintLayout>(R.id.constraintLayout).forEach {
                    it.isEnabled = true
                }
            }

            2 -> {
                btStateView.setBackgroundColor(Color.GREEN)
                btStateView.setImageResource(R.drawable.baseline_bluetooth_connected_24)
            }
        }

    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun extractXZ(data: ByteArray) {
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
            tvZLabel.text = String.format(locale = Locale.ROOT,"%.2f", mZlimb / 160.0f)
            tvZLabelGlobal.text = String.format(locale = Locale.ROOT,"%.2f", controllerZ / 160.0f)
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
            tvXLabel.text = String.format(locale = Locale.ROOT,"%.2f", mXlimb / 100.0f)
            tvXLabelGlobal.text =
                String.format(locale = Locale.ROOT, "%.2f", controllerX / 50.0f) //.replace(',','.')
//            findViewById<TextView>(R.id.tvOuterCutSourceDiameter).text = getX
        }
    }

    private fun processMessage(dataBA: ByteArray, message: String) {
        putLog("response: message $message")
        //      Integer.decode( message.substring(4,11))
        try {
            val data = message.substring(4, 12)
            when (message[3]) {
                'R' -> {
                    putLog("reset event, fw build: ${Integer.parseUnsignedInt(data, 16)}")
                    cmdRecorded = false
                    cmdStarted = false
                    fab.setBackgroundColor(Color.GREEN)
                    outerCutTargetDiameterMod = false
                    outerCutSourceDiameterMod = false
                    mXlimb = 0
                    mZlimb = 0
                    mZlimbInitial = true
                    mXlimbInitial = true

                }
//                'Z' -> {
//                    putLog("global Z ${Integer.parseUnsignedInt(data,16)/160.0f}")
//                }
//                'X' -> {
//                    val getX = String.format("%.2f", (Integer.parseUnsignedInt(data,16)/50.0f)).replace(',','.') //diameter mode, dive by 50
//                    tvXLabelGlobal.text = getX
//                    findViewById<TextView>(R.id.tvOuterCutSourceDiameter).text = getX
//                    putLog("global X ${tvXLabelGlobal.text}")
//                }
                'S' -> putLog("on stop: ${Integer.parseUnsignedInt(data, 16) / 160.0f}")// stop
                'E' -> {
                    cmdStarted = false
                    extractXZ(dataBA)
                    processProgram()
                    if (fbChamfer)
                        easyChamfer()
                    if (threadMode)
                        fab.setText(R.string.thread)
                    else
                        fab.setText(R.string.feed)
                }

                'f' -> {
                    val feed = Integer.parseUnsignedInt(data, 16)
                    putLog("feed 1616: ${feed.toString()}")
                    val feedF = 1474560.0f / feed
                    putLog("feed: ${feedF.toString()}")
                    tvFeedValue.text = String.format(locale = Locale.ROOT,"%.1f", feedF)
                }

                'F' -> {
                    val i = java.lang.Long.parseLong(data, 16);
                    val f = java.lang.Float.intBitsToFloat(i.toInt());
                }
            }

        } catch (ex: Exception) {
            putLog("response: exception $message")
            Log.i("---", "Exception in thread")
        }

    }

//    var currentJog : Int = 0
//    fun onClickZ(v: View){
//        currentJog = JOG_Z
//        findViewById<MaterialButton>(R.id.btJogX).setBackgroundColor(Color.GRAY)
//        findViewById<MaterialButton>(R.id.btJogZ).setBackgroundColor(Color.RED)
//        findViewById<View>(R.id.knob).isVisible = false
//        findViewById<View>(R.id.knobZ).isVisible = true
//        findViewById<ConstraintLayout>(R.id.constraintLayoutJog).isVisible = true
//    }
//    fun onClickX(v: View){
//        currentJog = JOG_X
//        findViewById<MaterialButton>(R.id.btJogX).setBackgroundColor(Color.RED)
//        findViewById<MaterialButton>(R.id.btJogZ).setBackgroundColor(Color.GRAY)
//
//        findViewById<View>(R.id.knob).isVisible = true
//        findViewById<View>(R.id.knobZ).isVisible = false
//        findViewById<ConstraintLayout>(R.id.constraintLayoutJog).isVisible = true
//    }
//    fun onJogHide(v: View){
//        findViewById<ConstraintLayout>(R.id.constraintLayoutJog).isVisible = false
//    }
//
//    override fun onJogLongPress(e: MotionEvent) {
//        jogLP = true
//        if(currentJog == JOG_X) {
//            if(e.y < 300){
//                sendCommand("G00 X-200")
//            } else if(e.y > 400){
//                sendCommand("G00 X200")
//            }
//        } else if (currentJog ==JOG_Z){
//            if(e.x < 300){
//                sendCommand("G00 Z-200")
//            } else if(e.x > 400){
//                sendCommand("G00 Z200")
//            }
//        }
////        findViewById<ConstraintLayout>(R.id.constraintLayoutJog).isVisible = false
//    }
//
//    override fun onJogRotate(value: Int, delta: Int) {
//        if(currentJog == JOG_X) {
//            if(delta>0)
//                sendCommand("!w")
//            else
//                sendCommand("!s")
//        }
//        else {
//            if(delta>0)
//                sendCommand("!d")
//            else
//                sendCommand("!a")
//        }
//    }


    private fun easyChamfer() {
        var iCh = 4
        when (fbChamferDbg) {
            0 -> sendCommand("G01 Z$iCh X-${iCh * 2} F${tvFeedValue.text}")
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
        if (currentView == 2 || currentView == 5) {
//            if(outerCutSourceDiameterMod && outerCutTargetDiameterMod && !pauseProgramFlag && cmdRecorded) {
            if (!pauseProgramFlag && cmdRecorded) {
                if (vaitDocFeedEnd == true) {
                    putLog("DOC complete, repeat main cut")
                    sendCommand("!3")
                    vaitDocFeedEnd = false
                } else {
                    putLog("move DOC:")
                    if (currentView == 2) {
                        sendCommand("G00 X -${findViewById<TextView>(R.id.etDOC).text}")
                    } else
                        sendCommand("G00 X ${findViewById<TextView>(R.id.etDOC).text}")
                    vaitDocFeedEnd = true
                }
            } else {
                if (cmdRecorded)
                    putLog("program on pause")
//                else
//                    putLog("no active records to play")

            }
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

    @Suppress("RemoveRedundantQualifierName")
    fun onClickFastLeft(view: android.view.View) {
        feedDirection = Direction.Left
        fab.setIconResource(R.drawable.ic_baseline_arrow_back_24)
    }

    @Suppress("RemoveRedundantQualifierName")
    fun onClickFastRight(view: android.view.View) {
        feedDirection = Direction.Right
        fab.setIconResource(R.drawable.ic_baseline_arrow_forward_24)
    }

    @Suppress("RemoveRedundantQualifierName")
    fun onClickFF(view: android.view.View) {
        feedDirection = Direction.Forward
        fab.setIconResource(R.drawable.ic_baseline_north_24)
    }

    @Suppress("RemoveRedundantQualifierName")
    fun onClickFB(view: android.view.View) {
        feedDirection = Direction.Backward
        fab.setIconResource(R.drawable.ic_baseline_south_24)
    }

    fun onRadioButtonThreadClicked(v: View) {
        //    putLog("connection fail");
        val checked = (v as RadioButton).isChecked
        if (checked) {
            threadMode = true
            fab.setText(R.string.thread)
        }
    }

    fun onRadioButtonFeedClicked(v: View) {
        //putLog("connection fail");
        val checked = (v as RadioButton).isChecked
        if (checked) {
            threadMode = false
            fab.setText(R.string.feed)
        }
    }

//    fun onLoopClicked(v: View) {
//        val checked = (v as Switch).isChecked
//        if (checked) {
//            loopMode = true
//            ping = false
//            putLog("loop mode on")
//            pingpong()
//        } else {
//            if (loopMode) {
//                loopMode = false
//                putLog("loop mode off")
//            }
//        }
//    }

//    private fun pingpong() {
//        if (!loopMode) return
//
////        String minus = ( feed_direction == direction.right || feed_direction == direction.backward )  ? "" : "-";
//        ping = if (!ping) {
//            val cmd = buildCmd()
//            //            String cmd = "G01 Z" + minus + tvFeedLength.getText() +" F" +  tvFeedValue.getText();
//            putLog("ping:$cmd")
//            sendCommand(cmd)
//            true
//        } else {
//            val cmd = "G01 Z0 F" + tvFeedValue.text.toString()
//            putLog("pong:$cmd")
//            sendCommand(cmd)
//            false
//        }
//    }

    fun buildCmd(): String {
//        val minus =
//            if (feedDirection == Direction.Right || feedDirection == Direction.Backward) "" else "-"
//        var gCmd = "G01"
//        var gSub: String
//        val gLength: String
//        var gTaper = ""
//        val axis = if (feedDirection == Direction.Right || feedDirection == Direction.Left) " Z" else " X"
//        val tvLen: TextView
//
//        val tvTaperValue = findViewById<TextView>(R.id.tvTaperValue).text.toString().toFloat()

        val out = mfUpdIf.buildCmd()
//        if (threadMode) {
//            gCmd = "G33"
//            gSub = "K" + tvScrewPitch.text
//            tvLen = findViewById(R.id.editTextThreadLength)
//            gLength = tvLen.text.toString()
//            val tvMultiThread = findViewById<TextView>(R.id.editTextMultiThreadCount)
//            val cnt = tvMultiThread.text.toString().toInt()
//            if (cnt > 1) {
//                gSub += " " + "M" + tvMultiThread.text.toString()
//            }
//
//        } else {
//            gSub = "F" + tvFeedValue.text
//            gLength = tvFeedLength.text.toString()
//            if(tvTaperValue > 0){
//                var targetTaperFloat = tvTaperValue*tvFeedLength.text.toString().toFloat()///2 // controller use diameter mode for X axis so no need to divide by 2 here
//                if(axis == " Z"){
//                    if(internalCut){ // for internal cut inverse positive value of taper
//                        targetTaperFloat = -targetTaperFloat
//                    }
//                    var taperStr =  String.format("%.3f", targetTaperFloat).replace(",", ".") // "%.$3f".format(targetTaperFloat.toString())
//                    gTaper = " X${taperStr}"
//                    putLog("taper:$taperStr")
//                } else {
//                    TODO("taper on X axis?")
//                }
//            }
//        }
        //        val out = "$gCmd$axis$minus$gLength$gTaper $gSub"
        putLog("cmd:$out")
        return out
    }

    fun onEditTextClickSelectAll(v: View) {
        (v as EditText).selectAll()
    }

    fun onOuterCutSourceDiameterClick(v: View) {
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

    fun onOuterCutTargetDiameter(v: View) {
        outerCutTargetDiameterMod = true
        (v as EditText).selectAll()
    }

    fun onPauseClick(v: View) {

//        pauseProgramFlag = !pauseProgramFlag
        pauseProgramFlag = !pauseProgramFlag
        if (pauseProgramFlag) {
            findViewById<Button>(R.id.btPause).setBackgroundColor(Color.RED)
        } else {
            findViewById<Button>(R.id.btPause).setBackgroundColor(Color.GREEN)
            processProgram()
        }
    }

    fun onClickUnit(v: View) {
        if (mMetricUnit) {
            mMetricUnit = false
            (v as TextView).text = getString(R.string.inch)
        } else {
            mMetricUnit = true
            (v as TextView).text = getString(R.string.metric)
        }
    }

    fun onLongCLickX(v: View) {
        //mX = 0.0f
    }

    fun onStop(v: View) {
        sendCommand("!S")
    }

//    private fun changeFeedUnit() {
//        if (feedUnitG95) {
//            feedUnitG95 = false
//            (findViewById<View>(R.id.tvFeedUnit) as TextView).setText(R.string.feed_unit_metric_G94)
//            putLog("set mm\\min: G94")
//            sendCommand("G94")
//        } else {
//            feedUnitG95 = true
//            (findViewById<View>(R.id.tvFeedUnit) as TextView).setText(R.string.feed_unit_metric_G95)
//            //            tvFeedValue.setText("0.1");
//            putLog("set mm\\rev: G95")
//            sendCommand("G95")
//        }
//    }

    private var feedMmG95 =
        arrayOf("0.01", "0.02", "0.04", "0.06", "0.08", "0.10", "0.15", "0.20", "0.25", "0.30")
    private var feedMmG94 =
        arrayOf("100", "200", "250", "500", "750", "1000", "1250", "1500", "1750", "2000")
    var pitchMm = arrayOf("0.5", "0.75", "1.0", "1.25", "1.5", "1.75", "2.0", "2.5", "3.0", "3.5")
    fun getFeedValue(progress: Int): String {
        return if (feedUnitG95) feedMmG95[progress] else feedMmG94[progress]
    }

    var miniLogStr = ""
    fun sendCommand(cmd: String?): Boolean {
        miniLogStr = "$cmd<-$miniLogStr".take(30)
        tvLogMini.text = miniLogStr
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

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        currentView = p2
        viewsMap.forEach { it.value.isVisible = false }
        viewsMap[currentView]!!.isVisible = true
        if (currentView in 0..2) {
            internalCut = false
            sendCommand("!O")
        } else {
            internalCut = true
            sendCommand("!I")
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }


}

class CustomScrollView : HorizontalScrollView {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
    }

    override fun computeScrollDeltaToGetChildRectOnScreen(rect: Rect): Int {
        return 0
    }
}