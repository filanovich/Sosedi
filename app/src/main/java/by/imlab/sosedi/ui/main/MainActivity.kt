package by.imlab.sosedi.ui.main

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import by.imlab.sosedi.BuildConfig
import by.imlab.sosedi.R
import by.imlab.sosedi.ui.global.extentions.setupWithNavController
import by.imlab.sosedi.ui.global.extentions.toast
import by.imlab.sosedi.ui.global.helpers.ScannerHelper
import by.imlab.sosedi.ui.orderdetails.OrderDetailsError
import by.imlab.sosedi.ui.settings.Preferences
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.search_printer_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity() {

    //class MediaReceiver : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
  //          var activity = this
    //        //MediaButtonReceiver.handleIntent(mediaSession, intent)
      //  }
    //}

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val viewModel: MainViewModel by viewModel()

    private var appBackgroundThread: Thread? = null

    private var needLogout = false

    private var notificationBuilder: NotificationCompat.Builder? = null

    private var lastEventTime = 0L
    private var cursorBarcode = ""
    private var startCursorSymbol = false

    //@ExperimentalTime
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainActivity = this
        tb = toolbar

        // Drawer navigation
        navController = findNavController(R.id.fagNavHost)
        navigationView.setupWithNavController(navController = navController) {
            viewModel.updateLastClickedItem(it.itemId)
            when (it.itemId) {
                R.id.logout -> {
                    showLogoutDialog()
                }
            }
            false
        }

        // Drawable showing handler
        drawableShowingHandler()

        // Setup toolbar
        appBarConfiguration = AppBarConfiguration(
            topLevelDestinationIds = setOf(
                R.id.ordersListFragment,
                R.id.underwayFragment,
                R.id.openPackageFragment,
                R.id.orderDetailsFragment,
                R.id.collectedOrdersFragment,
                R.id.transferredOrdersFragment,
                R.id.cancelledOrdersFragment,
            ),
            drawerLayout = drawerLayout
        )
        toolbar.setupWithNavController(
            navController = navController,
            configuration = appBarConfiguration
        )

        // Toolbar item click listener
        toolbar.setOnMenuItemClickListener {
            viewModel.updateLastClickedItem(it.itemId)
            return@setOnMenuItemClickListener true
        }

        // Toolbar items showing handler
        toolbarItemShowingHandler()

        // Barcode receiver
        val filter = IntentFilter(ScannerHelper.SCANNER_RESULT);
        registerReceiver(viewModel.scanReceiver, filter)

        // Notification receiver
        //val notificationFilter = IntentFilter("test");
        //registerReceiver(myBroadcastReceiver, notificationFilter)

        Preferences().readPreferences(this);

        createNotification()

        viewModel.setOnNotificationListener {
            if (it)
                onNotifyNewOrders()
        }

        viewModel.fetchNotification()
    }

//    var myBroadcastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            Toast.makeText(context, "hello!", Toast.LENGTH_SHORT).show()
//        }
//    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        //val fragment = supportFragmentManager.findFragmentById(R.id.container);
        //if (fragment != null && fragment is LoginFragment) {
        //    if (fragment.isVisible) {
        //        super.onBackPressed()
        //    }
        //}
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(viewModel.scanReceiver)
        //unregisterReceiver(myBroadcastReceiver)
        System.exit(0);
    }

    private fun drawableShowingHandler() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.searchPrinterFragment -> {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    toolbar.isVisible = false
                }
                R.id.scanPrinterFragment -> {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    toolbar.isVisible = true
                }
                R.id.loginFragment -> {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    toolbar.isVisible = false
                }
                else -> {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    toolbar.isVisible = true
                }
            }
        }
    }

    private fun toolbarItemShowingHandler() {
        val refreshItem = toolbar.menu.findItem(R.id.refreshButton)
        val enterItem = toolbar.menu.findItem(R.id.enterButton)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            enterItem.isVisible = false
            refreshItem.isVisible = false
            when (destination.id) {
                R.id.ordersListFragment -> {
                    refreshItem.isVisible = true
                }
                R.id.searchPrinterFragment -> {
                    enterItem.isVisible = true
                }
            }
        }
    }

    // TODO: Change this
    private fun showLogoutDialog() {
        //dialog(R.string.you_have_pending_orders, R.string.complete, {
            findNavController(R.id.fagNavHost).navigate(R.id.loginFragment)
        //}, R.string.cancel, {})
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_DOWN) {

            val keyCode = event?.keyCode;

            if (keyCode == KeyEvent.KEYCODE_UNKNOWN)
                return true;

            val eventTime = event?.eventTime;

            if (this.lastEventTime == 0L)
                this.lastEventTime = eventTime!!

            if (eventTime != null) {
                if (eventTime - lastEventTime > 100) {
                    this.startCursorSymbol = false
                    this.cursorBarcode = ""
                }
            }

            if (eventTime != null) {
                this.lastEventTime = eventTime
            }

            when {
                keyCode == KeyEvent.KEYCODE_LEFT_BRACKET -> {
                    this.startCursorSymbol = true
                    return true
                }
                keyCode == KeyEvent.KEYCODE_RIGHT_BRACKET -> {
                    if (!this.cursorBarcode.isEmpty()) {
                        toast(this.cursorBarcode, Toast.LENGTH_SHORT)
                        viewModel.updateLastBarcode(this.cursorBarcode)
                        this.startCursorSymbol = false
                        this.cursorBarcode = ""
                        return true
                    }
                }
                else -> {
                    if (this.startCursorSymbol) {
                        //val letterChar = Character.toString(event.displayLabel)
                        val unicode = event.unicodeChar;
                        val unicodeChar = Character.toChars(unicode)[0]
                        if (unicodeChar != '\u0000')
                            this.cursorBarcode += unicodeChar
                        return true
                    }
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
//        toast(newConfig.toString(), Toast.LENGTH_SHORT)
        super.onConfigurationChanged(newConfig)
    }

    private fun wakeDevice(context: Context) {
        //Создаём Power manager
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        //Создаём WakeLock
        val myWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE, "MyApp:NotificationWakelockTag")
        //Указываем длительность работы (В данном случае 5 секунд)
        myWakeLock.acquire(5 * 1000L)
        //Запускаем WakeLock
        myWakeLock.release()
    }

    fun onNotifyNewOrders() {

        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, notificationBuilder!!.build()) // посылаем уведомление
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        var item = toolbar.findViewById<ActionMenuItemView>(R.id.refreshButton)
        item?.callOnClick()
    }

    private fun createNotification() {

        createNotificationChannel()

        val bm = BitmapFactory.decodeResource(resources, R.drawable.box);

        val contentIntent = Intent(this, MainActivity::class.java)
        contentIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        contentIntent.action = "android.intent.action.MAIN"
        contentIntent.addCategory("android.intent.category.LAUNCHER")
        val contentPendingIntent = PendingIntent.getActivity(this, 1, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        //val actionIntent = Intent(this, NotificationReceiver::class.java)
        //actionIntent.action = "test"
        //val actionPendingIntent = PendingIntent.getBroadcast(this, 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.box_32)
                   .setContentTitle("Новый заказ!")
                    .setContentText("Обновите список заказов")
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setDefaults(NotificationCompat.DEFAULT_VIBRATE or NotificationCompat.DEFAULT_SOUND)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setLargeIcon(bm)
                    .setContentIntent(contentPendingIntent)
                    //.addAction(0, "Обновить список заказов", actionPendingIntent);
        }
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.description = CHANNEL_DESCRIPRION
            //notificationChannel.setSound(soundUri, audioAttributes)
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    companion object {
        var tb: MaterialToolbar? = null
        var mainActivity: MainActivity? = null
        const val NOTIFICATION_ID = 101
        const val CHANNEL_ID = "channelID"
        const val CHANNEL_NAME = "Sosedi Picker"
        const val CHANNEL_DESCRIPRION = "sosedi channel"
    }
}