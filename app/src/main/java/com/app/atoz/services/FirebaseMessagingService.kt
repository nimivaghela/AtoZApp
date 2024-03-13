package com.app.atoz.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.models.UserHolder
import com.app.atoz.ui.main.MainActivity
import com.app.atoz.ui.myorders.orderdetails.OrderDetailsActivity
import com.app.atoz.ui.myorders.orderdetails.OrderDetailsActivity.Companion.KEY_ORDER_DATA
import com.app.atoz.ui.provider.existservice.editservice.EditPersonalServiceActivity
import com.app.atoz.ui.provider.existservice.editservice.EditPersonalServiceActivity.Companion.KEY_SERVICE_ID
import com.app.atoz.ui.provider.jobdetails.JobDetailsActivity
import com.app.atoz.ui.provider.jobdetails.JobDetailsActivity.Companion.KEY_JOB_ID
import com.app.atoz.ui.provider.jobdetails.JobDetailsActivity.Companion.KEY_REQUEST_END_TIMER
import com.app.atoz.ui.splash.SplashActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject


const val NOTIFICATION_TYPE = "notification_type"

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }

    enum class NotificationType {
        ORDER_PLACED, ORDER_CONFIRMED, ORDER_COMPLETED, ORDER_CANCELLED, PRICE_CHANGE_APPROVED
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if message contains a data payload.
        if (!remoteMessage.data.isNullOrEmpty() && userHolder.deviceToken.isNullOrEmpty()) {
            sendNotification(remoteMessage.data, provideNotificationManager())
        }
    }

    private lateinit var image: Bitmap

    @Inject
    lateinit var userHolder: UserHolder

    override fun onCreate() {
        super.onCreate()
        ((applicationContext as AppApplication)).mComponent.inject(this)
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Timber.tag(TAG).d("Refreshed token: %s", token)

        if (!userHolder.deviceToken.isNullOrEmpty()) {
            sendRegistrationToServer(token)
        }
    }

    /**
     * Persist token to third-party servers.
     *
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */

    private fun sendRegistrationToServer(token: String?) {
        token?.let {
            UpdateFCMTokenService.startService(applicationContext, it)
        }

    }


    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private fun sendNotification(messageBody: MutableMap<String, String>, notificationManager: NotificationManager) {
        var channelId = getString(R.string.app_name)
        val notificationType = messageBody["notification_type"]

        try {
            if (messageBody.containsKey("data")) {

                JSONObject(messageBody["data"]).optString("image", null)?.let {
                    if (it.isNotEmpty()) {
                        getBitmapFromURL(it)?.let { bitmap ->
                            image = bitmap
                        }
                    }
                }
            }
        } catch (e: IOException) {
            Timber.e(e.localizedMessage)
        }

        val soundUri = Uri.parse("android.resource://" + application.packageName + "/" + R.raw.notification_bell)
        Timber.d("Sound file URI is $soundUri")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId =
                createNotificationChannel(notificationType.toString(), notificationManager, soundUri)
        }

        val navigationIntent = buildIntent(messageBody)

        val backIntent = Intent(this, MainActivity::class.java)
        backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)

        val defaultIntent = Intent(this, SplashActivity::class.java)

        /*val pendingIntent = PendingIntent.getActivities(
            applicationContext, 0,
            navigationIntent?.let {
                arrayOf(backIntent, navigationIntent)
            } ?: arrayOf(defaultIntent)
            , PendingIntent.FLAG_UPDATE_CURRENT
        )*/

        // Create the TaskStackBuilder
        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            // Add the intent, which inflates the back stack
            navigationIntent?.let {
                for (i in 1..2) {
                    // arrayOf(backIntent, navigationIntent)
                    when (i) {
                        1 -> addNextIntentWithParentStack(backIntent)
                        2 -> addNextIntentWithParentStack(navigationIntent)
                    }
                }

            } ?: addNextIntentWithParentStack(defaultIntent)

            // Get the PendingIntent containing the entire back stack
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        image = (getDrawable(R.drawable.ic_notification_main) as BitmapDrawable).bitmap
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(messageBody["title"])
            .setContentText(messageBody["body"])
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(messageBody["body"])
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
            .setLargeIcon(image)
            .setSound(soundUri)
            .setContentIntent(resultPendingIntent)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }


    private fun buildIntent(messageBody: MutableMap<String, String>): Intent? {
        var intent: Intent? = null

        when (val notificationType = messageBody["notification_type"]) {
            NotificationType.ORDER_PLACED.name, NotificationType.ORDER_CONFIRMED.name, NotificationType.ORDER_COMPLETED.name, NotificationType.ORDER_CANCELLED.name -> {

                try {
                    intent = when (JSONObject(messageBody["data"]).optString("user_type", null)) {
                        "agent" -> Intent(this, JobDetailsActivity::class.java).apply {
                            putExtra(NOTIFICATION_TYPE, notificationType)
                            putExtra(KEY_JOB_ID, JSONObject(messageBody["data"]).getString("order_id"))
                            putExtra(KEY_REQUEST_END_TIMER, JSONObject(messageBody["data"]).getString("created_at"))
                        }
                        "user" -> Intent(this, OrderDetailsActivity::class.java).apply {
                            putExtra(NOTIFICATION_TYPE, notificationType)
                            putExtra(KEY_ORDER_DATA, JSONObject(messageBody["data"]).getString("order_id"))
                        }
                        else -> Intent(this, SplashActivity::class.java).apply {
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            NotificationType.PRICE_CHANGE_APPROVED.name -> {

                try {
                    intent =
                        Intent(this, EditPersonalServiceActivity::class.java).apply {
                            putExtra(NOTIFICATION_TYPE, notificationType)
                            putExtra(KEY_SERVICE_ID, JSONObject(messageBody["data"]).getString("service_id"))
                        }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            else -> intent = null
        }
        return intent
    }

    private fun provideNotificationManager(): NotificationManager {


        return getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun getBitmapFromURL(src: String): Bitmap? {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(src)
            connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } finally {
            connection?.disconnect()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(
        notificationType: String,
        notificationManager: NotificationManager,
        soundUri: Uri
    ): String {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        when (notificationType) {
            NotificationType.ORDER_PLACED.name, NotificationType.ORDER_CONFIRMED.name,
            NotificationType.ORDER_COMPLETED.name, NotificationType.ORDER_CANCELLED.name -> {
                // Create the NotificationChannel
                val importance = NotificationManager.IMPORTANCE_DEFAULT

                val mChannel = NotificationChannel(getString(R.string.order), getString(R.string.order), importance)
                mChannel.description =
                    getString(R.string.notification_channel_description_order)
                mChannel.enableVibration(true)
                mChannel.setSound(soundUri, attributes)
                mChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                notificationManager.createNotificationChannelGroup(
                    NotificationChannelGroup(
                        getString(R.string.order),
                        getString(R.string.order)
                    )
                )
                notificationManager.createNotificationChannel(mChannel)
                return getString(R.string.order)
            }
            NotificationType.PRICE_CHANGE_APPROVED.name -> {
                // Create the NotificationChannel
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val mChannel =
                    NotificationChannel(getString(R.string.price_change), getString(R.string.price_change), importance)
                mChannel.description = getString(R.string.notification_channel_description_price_change)
                mChannel.enableVibration(true)
                mChannel.setSound(soundUri, attributes)
                mChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

                notificationManager.createNotificationChannelGroup(
                    NotificationChannelGroup(
                        getString(R.string.price_change),
                        getString(R.string.price_change)
                    )
                )
                notificationManager.createNotificationChannel(mChannel)
                return getString(R.string.price_change)
            }
            else -> {
                // Create the NotificationChannel
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val mChannel =
                    NotificationChannel(getString(R.string.promotion), getString(R.string.promotion), importance)
                mChannel.description = getString(R.string.notification_channel_description_promotion)
                mChannel.enableVibration(true)
                mChannel.setSound(soundUri, attributes)
                mChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                notificationManager.createNotificationChannelGroup(
                    NotificationChannelGroup(
                        getString(R.string.promotion),
                        getString(R.string.promotion)
                    )
                )
                notificationManager.createNotificationChannel(mChannel)
                return getString(R.string.promotion)
            }
        }
    }
}