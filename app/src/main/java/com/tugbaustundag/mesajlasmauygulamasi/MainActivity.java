/*
 * Copyright (C) 2015 Tuğba Üstündağ <tugba.ust2008@gmail.com><http://tugbaustundag.com>
*/
package com.tugbaustundag.mesajlasmauygulamasi;
import android.app.Activity;
import android.content.Intent;
import android.os.StrictMode;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

public class MainActivity extends Activity {
    private  Veritabani db;
    private  EditText sifreInput;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Android cihaz veritabanındaki,kayıt sayısını, getRecordNumber metodu kullanarak alındı.
        db=new Veritabani(MainActivity.this);
        int count=db.getRecordNumber();

        //Kullanıcı programı her actıgında,  mesajla ilgili işlemleri yapmak istedginde,sifre girme ekranı gelmemesi icin kontrol yaptık.
        //Eğer kullanıcı Android cihaz veritabanında kayıdı yoksa kullanıcı giriş ekran gelcek,kayıt varsada direk Mesaj gönderme ekranı gelmesi icin kontrol yaptık

        //Android cihaz da kayıt yok ise, sifre girilmesi saglanır
        if (count == 0) {

            //Form elemanlarını tanımladık

            sifreInput=(EditText)findViewById(R.id.sifre);
            Button btn_kayit=(Button)findViewById(R.id.btn_kayit);

            btn_kayit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    MyMethod myMethod=new MyMethod();
                    //Form dan gelen degerleri aldık..
                    String sifre=sifreInput.getText().toString();

                    //Xss ataklarını önlemek icin,form dan gelen degerleri stripXSS metodunu kullanarak süzgecledik
                    sifre=myMethod.stripXSS(sifre);

                    if(sifre.equals("")|| sifre.equals(null))
                    {
                        Toast.makeText(getApplicationContext(), "Şifre  alanı boş bırakmayın!", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        //Sifre kontrolu icin metod cagrıldı ve true/false degeri degiskene atıldı
                        boolean sifreDurum=PasswordControl_in_Server(sifre);

                        //Sifre dogruysa,Android cihazın veritabanına sifre kayıdı yaptım ve SendMessage sınıfına yonlendrdim
                        if(sifreDurum){
                            //Android cihazın veritabanına sifre kayıtı yapılır
                            db.insertUser(sifre);

                            Intent i=new Intent(MainActivity.this,SendMessage.class);
                            Bundle b=new Bundle();
                            b.putString("sifre",sifre);
                            i.putExtras(b);
                            startActivity(i);
                        }else{
                            Toast.makeText(getApplicationContext(),"Yalnış şifre girdiniz", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

        }
        else{
            //Kullanıcı grubundan bir kişi sifresi veritabanından silinerek cıkarıldıgında , o kişinin programı actgında mesaj girmesini engelemek icin kontrol yapıyoruz
            //Yani engelenen kişinin android cihazında sifre kayıdı dahi olsa ,sunucu veritabanında kullanıcı cıkarıldıgından , programı actgında sifre girme ekranı getirmesini sagladık.
            String sifre=db.getPassword();
            boolean sifreDurum=PasswordControl_in_Server(sifre);
            if(sifreDurum){
                //Android cihaz da kayıt varsa bu alana girer ve SendMessage sınıfına yonlendirme yapılır
                Intent i=new Intent(MainActivity.this,SendMessage.class);
                Bundle b=new Bundle();
                b.putString("sifre",sifre);
                i.putExtras(b);
                startActivity(i);
            }else{
                Toast.makeText(getApplicationContext(),"Yönetim tarafından, kullanıcı kaydınız kaldırıldıgından yeniden kayıt olmalısınız", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Kullanıcının girdigi sifrenin doğru olup olmadıgna dair kontrolu,sifreyi sunucuya gonderip, sunucu veritabanı kontrollerinden sonra sifre durumunu(true/false)
     * RESTful Web servisi aracılıgıyla sunucudan cektik
     * @param sifre
     * @return boolean
     */

    private boolean PasswordControl_in_Server(String sifre){

        //StrictMode kullanarak,ağ erişiminin güvenli bir şekilde yapılmasını sağlıyoruz...
        StrictMode.ThreadPolicy policy = new
        StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String wcfUrl= "http://test.com/passwordControl.php";
        String jsonString="";
        JSONObject obj=new JSONObject();
        boolean sifreDurum=false;
        try {
            //RESTful Web servisiyle göndermek isteğimiz içerikleri,Json objesine   put methoduyla ekliyoruz..

            obj.put("sifre",sifre);
            HttpClientMy HttpClientMy=new HttpClientMy();

            //Sifre kontrolu icin,RESTful Web servisini çağırıp, sifreyi sunucuya gönderen ve  sunucudan  sifrenin dogru olup olmadıgına dair
            //(true/false) degerini çekmeyi sağlayn methodu çağırdık,
            jsonString=HttpClientMy.callWebService(wcfUrl, obj);

            //Json string ini parse ediyoruz
            JSONObject reader=new JSONObject(jsonString);
            JSONObject jsonObject=reader.getJSONObject("Android");
            sifreDurum=jsonObject.getBoolean("sifreDurum");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return sifreDurum;
    }

}
