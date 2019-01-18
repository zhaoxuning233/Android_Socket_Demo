package com.qilinkeji.libsocket.travel;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author zxn
 */
public class TravelLocationUtil {

    private static TravelLocationUtil mInstance = null;
    private static Context mcontext = null;
    private static File cacheDir;
    public static final String DRIVER_TRACTION = "driverTraction";//文件目录名

    private TravelLocationUtil(Context context) {
        mcontext = context;
    }

    public static TravelLocationUtil getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new TravelLocationUtil(context);
        } else {
            mcontext = context;
        }
        MakeFile();
        return mInstance;
    }

    private static void MakeFile() {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), DRIVER_TRACTION);
        } else {
            cacheDir = mcontext.getCacheDir();
        }
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        cacheDir = new File(Environment.getExternalStorageDirectory(), DRIVER_TRACTION);
    }

    public void writefile(String filename, String content) {
        File saveFile = new File(cacheDir, filename + ".txt");
        if (!saveFile.exists()) {
            try {
                saveFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(saveFile, true)));
            out.write(content);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static HashMap<String, String> getKMSP(Context context, String key, String[] strings) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        SharedPreferences sharedPreferences = getSharedPreferencesOther(context, key);
        for (String keyName : strings) {
            hashMap.put(keyName, sharedPreferences.getString(keyName, "-1"));
        }
        return hashMap;
    }

    private static SharedPreferences getSharedPreferencesOther(Context context, String key) {
        return context.getSharedPreferences(key, Context.MODE_PRIVATE);
    }

    /**
     * 保留一位小数
     * 四舍五入
     */
    public static String getbignum(String lastnum, int scale) {
        try {
            BigDecimal result2 = new BigDecimal(lastnum);
            return result2.setScale(scale, BigDecimal.ROUND_HALF_UP).toPlainString() + "";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }


    public static void setKMSP(Context context, String key, HashMap<String, String> map) {
        SharedPreferences sharedPreferences = getSharedPreferencesOther(context, key);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Iterator iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            String mapKey = iterator.next().toString();
            String mapValue = map.get(mapKey);
            editor.putString(mapKey, mapValue);
        }
        editor.commit();
    }
}
