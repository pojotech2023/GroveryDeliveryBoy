package wrteam.ecart.rider.helper

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.text.Html
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import wrteam.ecart.rider.R
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class MyNotificationManager(private val mCtx: Context) {
    private val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    fun showBigNotification(title: String?, message: String?, url: String?, intent: Intent?) {

        val resultPendingIntent = PendingIntent.getActivity(
            mCtx,
            ID_BIG_NOTIFICATION,
            intent,
            PendingIntent.FLAG_IMMUTABLE // setting the mutability flag
        )

        val bigPictureStyle = NotificationCompat.BigPictureStyle()
        bigPictureStyle.setBigContentTitle(Html.fromHtml(title,0).toString())
        bigPictureStyle.setSummaryText(Html.fromHtml(message,0).toString())
        bigPictureStyle.bigPicture(getBitmapFromURL(url))
        val mBuilder = NotificationCompat.Builder(
            mCtx, "notification"
        )
        val notification: Notification = mBuilder.setSmallIcon(R.mipmap.ic_launcher_round).setTicker(title)
            .setAutoCancel(true)
            .setShowWhen(true)
            .setWhen(Calendar.getInstance().timeInMillis)
            .setContentIntent(resultPendingIntent)
            .setContentTitle(Html.fromHtml(title,0).toString())
            .setContentText(Html.fromHtml(message,0).toString())
            .setStyle(bigPictureStyle)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setLargeIcon(BitmapFactory.decodeResource(mCtx.resources, R.mipmap.ic_launcher_round))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_SOUND)
            .setSound(defaultSoundUri)
            .build()

//        textToSpeech.speak(title, TextToSpeech.QUEUE_FLUSH, null, null);
        notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL
        val notificationManager =
            mCtx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createChannel(notificationManager)
        notificationManager.notify(ID_BIG_NOTIFICATION, notification)
    }

    fun showSmallNotification(title: String?, message: String?, intent: Intent?) {
        val resultPendingIntent = PendingIntent.getActivity(
            mCtx,
            ID_SMALL_NOTIFICATION,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        //NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mCtx);
        val mBuilder = NotificationCompat.Builder(
            mCtx, "notification"
        )
        val notification: Notification = mBuilder.setSmallIcon(R.mipmap.ic_launcher_round).setTicker(title)
            .setAutoCancel(true)
            .setShowWhen(true)
            .setWhen(Calendar.getInstance().timeInMillis)
            .setContentIntent(resultPendingIntent)
            .setContentTitle(Html.fromHtml(title,0).toString())
            .setContentText(Html.fromHtml(message,0).toString())
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setLargeIcon(BitmapFactory.decodeResource(mCtx.resources, R.mipmap.ic_launcher_round))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.BigTextStyle().bigText(Html.fromHtml(message,0).toString()))
            .setDefaults(Notification.DEFAULT_SOUND)
            .setSound(defaultSoundUri)
            .build()

//        textToSpeech.speak(title, TextToSpeech.QUEUE_FLUSH, null, null);
        notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL
        val notificationManager =
            mCtx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createChannel(notificationManager)
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    //The method will return Bitmap from an image URL
    private fun getBitmapFromURL(strURL: String?): Bitmap? {
        return try {
            val url = URL(strURL)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createChannel(notificationManager: NotificationManager) {
        val name = "notification"
        val description = "Notifications for download status"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel("notification", name, importance)
        mChannel.description = description
        mChannel.enableLights(true)
        mChannel.lightColor = Color.BLUE
        mChannel.setShowBadge(true)
        mChannel.setSound(
            defaultSoundUri, AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        )
        notificationManager.createNotificationChannel(mChannel)
    }

    companion object {
        const val ID_BIG_NOTIFICATION = 234
        const val ID_SMALL_NOTIFICATION = 235
    }
}