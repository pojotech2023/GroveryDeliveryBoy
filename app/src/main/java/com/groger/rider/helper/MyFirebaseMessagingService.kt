package com.groger.rider.helper

import android.content.Intent
import android.speech.tts.TextToSpeech
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONException
import org.json.JSONObject
import com.groger.rider.activity.OrderDetailActivity
import com.groger.rider.model.WalletHistory
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private var textToSpeech: TextToSpeech? = null
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            try {
                val json = JSONObject(remoteMessage.data.toString())
                sendPushNotification(json)
            } catch (e: Exception) {
                Log.e(TAG, "Exception: " + e.message)
            }
        }
    }

    private fun sendPushNotification(json: JSONObject) {
        try {
            val data = json.getJSONObject(Constant.DATA)
            val title = data.getString("title")
            val message = data.getString("message")
            val imageUrl = data.getString("image")
            val type = data.getString("type")
            val id = data.getString("id")
            val intent: Intent
            if (type == "delivery_boys") {
                intent = Intent(applicationContext, OrderDetailActivity::class.java)
                intent.putExtra("order_id", id)
            } else {
                intent = Intent(applicationContext, WalletHistory::class.java)
            }
            textToSpeech = TextToSpeech(applicationContext) { status: Int ->
                if (status != TextToSpeech.ERROR) {
                    // To Choose language of speech
                    textToSpeech!!.language = Locale.ENGLISH
                    textToSpeech!!.speak(title, TextToSpeech.QUEUE_FLUSH, null, null)
                }
            }
            val mNotificationManager = MyNotificationManager(applicationContext)
            //Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            if (imageUrl == "null" || imageUrl == "") {
                mNotificationManager.showSmallNotification(title, message, intent)
            } else {
                mNotificationManager.showBigNotification(title, message, imageUrl, intent)
            }
        } catch (e: JSONException) {
            Log.e(TAG, "Json Exception: " + e.message)
        } catch (e: Exception) {
            Log.e(TAG, "Exception: " + e.message)
        }
    }

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        Session(applicationContext).setData(Constant.FCM_ID,s)
        //MainActivity.UpdateToken(s);
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}