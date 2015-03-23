package com.tugbaustundag.mesajlasmauygulamasi;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class Veritabani extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    //Veritabanı ismi
    private static final String DATABASE_NAME = "Message";

    public Veritabani(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //kullanici tablosunu olusturma SQL sorgusunu olusturduk
        String CREATE_USER_TABLE = "CREATE TABLE kullanici ( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "sifre TEXT)";

        //kullanici tablosunu olusturduk
        db.execSQL(CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Eskiden olusturulan, kullanici adında tablo varsa, kaldırıp yeniden olusturduk
        db.execSQL("DROP TABLE IF EXISTS kullanici");
        this.onCreate(db);
    }

    /**
     * getRecordNumber methodu telefondaki kayit sayisini dondurur
     * @return
     */
    public int getRecordNumber() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns={"_id"};
        Cursor cursor=db.query("kullanici",columns,null,null,null,null,null);

        int count = 0;
        if (cursor != null) {

            count=cursor.getCount();
            Log.w("count",String.valueOf(count));
        }
        cursor.close();
        db.close();
        return count;
    }

    //Android cihaza , kullanıcı sifresini kayıt eden metod
    public void insertUser(String sifre){

        SQLiteDatabase db = this.getWritableDatabase();
        String table_name="kullanici";

        SQLiteStatement sqLiteStatement = db.compileStatement("" +
                "INSERT INTO " +
                table_name +
                " (sifre) " +
                " VALUES (?) ");
        sqLiteStatement.bindString(1, sifre);

        sqLiteStatement.executeInsert();
        db.close();
    }
    //Android cihazdaki , kullanıcı sifresini ceken metod
    public String getPassword() {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns={"sifre"};
        Cursor cursor=db.query("kullanici",columns,null,null,null,null,null);

        String  sifre="";
        if(cursor!=null && cursor.getCount()>0){
            cursor.moveToFirst();
            sifre = cursor.getString(cursor.getColumnIndex("sifre"));
        }

        cursor.close();
        db.close();
        return sifre;
    }

}