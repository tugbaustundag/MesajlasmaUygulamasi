package com.tugbaustundag.mesajlasmauygulamasi;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.StrictMode;
import android.provider.Settings;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NotificationReceiver extends BroadcastReceiver {
    int MID=0;

    @Override
    public void onReceive(Context context, Intent intent) {
            //StrictMode kullanarak,ağ erişiminin güvenli bir şekilde yapılmasını sağlıyoruz...
            StrictMode.ThreadPolicy policy = new
            StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            // RESTful Web servisini kullanarak,  sunucudan  data çekmeyi sağlayan metoduzu cagırdık
            HttpClientMy HttpClientMy = new HttpClientMy();
            String wcfUrl = "http://test.com/getMessage.php";
            String jsonString=HttpClientMy.getServerDataWithWebService(wcfUrl);


            //Json string ini parse edicek methodu yazdık..
            ParseResponseJSONData(jsonString,context);
    }

    /**
     *  RESTful Web servis'den gelen json string i parse edip,elde ettigi
     * verileri push notification ile  kullanıcıya gosteren method
     * @param jsonString
     */
    public void ParseResponseJSONData(String jsonString,Context context) {

        JSONObject jsonResponse;

        try {
            //Json objesi olusturuyoruz..
            jsonResponse = new JSONObject(jsonString);
            //Olusturdugumuz obje üzerinden  json string deki dataları parse ediyoruz..
            JSONArray jArray=jsonResponse.getJSONArray("Android");

            for(int i=0;i<jArray.length();i++) {
                JSONObject json_data=jArray.getJSONObject(i);
                int mesaj_id = json_data.getInt("mesaj_id");
                String mesaj = json_data.getString("mesaj");
                String deviceId = json_data.getString("deviceId");//Tekil cihaz id (seri numarası)

                //Mesajı kayıt eden kişinin cihazı haricindeki, diğer cihazlara bildirim göster kontrolu yaptık
                //Bu kontrolu , sunucudan gelen deviceId degeri ile uygulamanın çalıştığı cihazın deviceId ini karsılastırarak yaptım
                SendMessage  sendMessage=new SendMessage();
                if(!sendMessage.setDeviceUUID(context).equals(deviceId)) {
                    //Sunucudan cektigimiz degerleri  PushNotification a ekleyen methodu cagırdık..
                    sendPushNotification(mesaj, context);

                    //Gönderilmiş mesajın ,tekrar tekrar gönderilmemesi icin ,sunucu veritabanındaki
                    // mesaj tablosundaki , flag sutununun degeri 1 diye update eden methodu cagırdım

                    updateFlagRow(mesaj_id);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sunucudan gelen degerleri PushNotification ile kullanıcıya, uyarı bildirimi seklinde gormesini saglayan method
     * @param mesajData
     * @param context
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void sendPushNotification(String mesajData,Context context){

        // Push Notification pencerisinin yanında gorunecek icon resmini belirledik...
        int icon=R.drawable.ic_launcher;
        //Gösterilcek mesajı hazırladık..
        String mesaj="Yeni mesajınız var: "+mesajData;

        //Push Notification pencerisinde gostermek istedigimiz baslıgı hazırladık.Ben uygulama ismini verdim..
        String ApplicationTitle=context.getString(R.string.app_name);
        //NOTIFICATION_SERVICE  cagırıp, notification un icon , baslık  gibi ozeliklerini vererek tanımladık
        NotificationManager notificationManager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        long[] pattern = {1000, 1000, 1000, 1000, 1000 };//titreme süresi
        Notification.Builder notification = new Notification.Builder(context)
                .setContentTitle(ApplicationTitle) //Push Notification pencerisinde gozukcek baslık belirlendi
                .setContentText(mesaj)//Uyarı mesajı gonderildi
                .setSmallIcon(icon)//Push Notification pencerisinin yanında gorunecek icon resmini belirledik...
                .setWhen(System.currentTimeMillis())
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI) ////Notification geldiginde telefonun default sesinin calısmasını saglar

                //Dilerseniz, notification.sound kullanarak istediginiz bir muzik dosyasını,notification geldiginde calmasını saglayabilirsiniz..
                //.setSound(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.beyoncesingleladies))

                .setVibrate(pattern)//Notification geldiginde telefonun titremisini saglar

                .setAutoCancel(true);//Notification a bastıgınızda ,uyarı penceresinin kapanmasını saglar

        Intent mesajListIntent=new Intent(context,MessageList.class);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, mesajListIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(contentIntent);

        //ve notification ozelliklerimizi ,notificationManager a gondererek, notification bildirimini sagladık
        notificationManager.notify(MID,notification.build());
        MID++;

    }

    /**
     * Gönderilmiş mesajın ,tekrar tekrar gönderilmemesini saglamak amacıyla kullanılacak mesaj_id datasını
     * sunucuya gonderen method
     * @param mesaj_id
     */
    public void updateFlagRow(int mesaj_id){

        //StrictMode kullanarak,ağ erişiminin güvenli bir şekilde yapılmasını sağlıyoruz...
        StrictMode.ThreadPolicy policy = new
        StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String wcfUrl= "http://test.com/updateFlagRow.php";

        JSONObject obj=new JSONObject();
        try {
            //RESTful Web servisiyle göndermek isteğimiz içerikleri,Json objesine   put methoduyla ekliyoruz..
            obj.put("mesaj_id",mesaj_id);

            HttpClientMy HttpClientMy=new HttpClientMy();

            //RESTful Web servisini çağırıp, içerikleri sunucuya gönderen  sağlayan method çağırdık,
            HttpClientMy.sendServerDataWithWebService(wcfUrl, obj);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
