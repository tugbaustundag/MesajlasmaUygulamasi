package com.tugbaustundag.mesajlasmauygulamasi;

import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MessageList extends ActionBarActivity {
    private ArrayList mesajList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);
        //Kullancagımız controller tanımladık...
        ListView list=(ListView)findViewById(R.id.msj_list);
        Button btn_msjGonder=(Button)findViewById(R.id.btn_msjGonder);

        //StrictMode kullanarak,ağ erişiminin güvenli bir şekilde yapılmasını sağlıyoruz...
        StrictMode.ThreadPolicy policy = new
        StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // RESTful Web servisini kullanarak,  sunucudan  data çekmeyi sağlayan metoduzu cagırdık
        HttpClientMy HttpClientMy = new HttpClientMy();
        String wcfUrl = "http://test.com/getAllMessage.php";
        String jsonString=HttpClientMy.getServerDataWithWebService(wcfUrl);

        mesajList=new ArrayList();
        //Json string ini parse ediyoruz ve listview 'e mesajları atadık..
        JSONObject jsonResponse;
        try {
            //Json objesi olusturuyoruz..
            jsonResponse = new JSONObject(jsonString);
            //Olusturdugumuz obje üzerinden  json string deki dataları kullanıyoruz..
            JSONArray jArray=jsonResponse.getJSONArray("Android");

            for(int i=0;i<jArray.length();i++) {
                JSONObject json_data=jArray.getJSONObject(i);
                String mesaj = json_data.getString("mesaj");
                // ArrayList e sunucudan gelen mesajları ekledik.
                mesajList.add(mesaj);

            }
            //ListView' e ArrayList deki mesajları aktardık ...
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, mesajList);
            list.setAdapter(adapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Mesaj gönderme sayfasına geri donmeyi saglayan butonun click event ini yazdık...
        btn_msjGonder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(MessageList.this,MainActivity.class);
                startActivity(i);
            }
        });

    }

}
