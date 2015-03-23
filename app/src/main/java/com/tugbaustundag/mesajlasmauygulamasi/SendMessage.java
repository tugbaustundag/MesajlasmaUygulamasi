package com.tugbaustundag.mesajlasmauygulamasi;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class SendMessage extends ActionBarActivity {
    private  EditText mesajInput;
    private PendingIntent pendingIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        //NotificationReceiver sınıfımızı,10 saniyede bir tekrar ederek çalısmasını saglayan AlarmManager sınıfını kullanıyoruz
        Intent dialogIntent = new Intent(getBaseContext(), NotificationReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, dialogIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        //AlarmManager sınıfını kullanmak icin ALARM_SERVICE servisini tanımlıyorurz...
        AlarmManager alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        // 1000 * 10 =10 saniye anlamına gelir
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 10, pendingIntent);
        //----------------

        //Form elemanlarını tanımladık
        mesajInput=(EditText)findViewById(R.id.mesaj);
        Button btn_kayit=(Button)findViewById(R.id.btn_kayit);
        Button btn_list=(Button)findViewById(R.id.btn_list);

        btn_kayit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                MyMethod myMethod=new MyMethod();
                //Form dan gelen degerleri aldık..
                String mesaj=mesajInput.getText().toString();

                //Xss ataklarını önlemek icin,form dan gelen degerleri stripXSS metodunu kullanarak süzgecledik
                mesaj=myMethod.stripXSS(mesaj);


                if(mesaj.equals("") || mesaj.equals(null))
                {
                    Toast.makeText(getApplicationContext(), "Mesaj alanını boş bırakmayın!", Toast.LENGTH_SHORT).show();
                }
                else{
                    //MainActivity den gelen sifre verimizi Intent ile aldık
                    String sifre="";
                    Bundle ext=getIntent().getExtras();
                    if(ext!=null){
                        sifre=ext.getString("sifre");
                    }
                    //Kullanıcının gönderdiği mesajı,kendisine bildirim olarak gelmesini engelemede kullanacagımız kontrol icin,
                    // Android cihazın seri numarası alıyouruz..
                    String deviceId= setDeviceUUID(getApplicationContext());

                    //Kontrollerimizi geçen dataları, sunucuya gonderen methodu cagırdık..
                    sendServerData_with_WebService(sifre, mesaj,deviceId);

                    Toast.makeText(getApplicationContext(),"Kayıt yapıldı",Toast.LENGTH_LONG).show();

                }
            }
        });

        //Mesajlari listemele buttonuna tıkladıgında ,mesaj listeleme sayfasına gidebilmesi Intent kullandık...
        btn_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(SendMessage.this,MessageList.class);
                startActivity(i);
            }
        });

    }

    /**
     * Form dan gelen degerleri ,JSONObject objesine ekleyip ,sendServerDataWithWebService methoduna gonderdik
     * @param sifre
     * @param mesaj
     * @param deviceId
     */
    private void sendServerData_with_WebService(String sifre,String mesaj,String deviceId){

        //StrictMode kullanarak,ağ erişiminin güvenli bir şekilde yapılmasını sağlıyoruz...
        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String wcfUrl= "http://test.com/insertMessage.php";

        JSONObject obj=new JSONObject();
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date =new Date();

            //RESTful Web servisiyle göndermek isteğimiz içerikleri,Json objesine   put methoduyla ekliyoruz..
            obj.put("date",dateFormat.format(date));
            obj.put("sifre",sifre);
            obj.put("mesaj",mesaj);

            obj.put("deviceId",deviceId);
            HttpClientMy HttpClientMy=new HttpClientMy();

            //RESTful Web servisini çağırıp, içerikleri sunucuya gönderen  sağlayan method çağırdık,
            HttpClientMy.sendServerDataWithWebService(wcfUrl, obj);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Android cihazın, tekil cihaz id (seri numarası) değerini aldık
     * @param context
     * @return String
     */
    public  String setDeviceUUID(Context context)
    {
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);


        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        return deviceUuid.toString();
    }


}
