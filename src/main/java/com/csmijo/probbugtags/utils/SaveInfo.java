package com.csmijo.probbugtags.utils;

import com.csmijo.probbugtags.baseData.AppInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

/**
 * Created by chengqianqian-xy on 2016/6/20.
 */
public class SaveInfo extends Thread {
    private final String TAG = "SaveInfo";
    private JSONObject jsonObject;
    private String filePath;

    public SaveInfo(JSONObject jsonObject, String filePath) {
        this.filePath = filePath;
        this.jsonObject = jsonObject;
    }

    @Override
    public void run() {
        super.run();
        saveData(this.jsonObject, this.filePath);
    }

    public void saveData(JSONObject saveObject, String filePath) {
        JSONObject existObject = null;

        File file = new File(filePath);
        try {

            if (file.exists()) {
                Logger.i(TAG, "file exist " + file.getAbsolutePath());
            } else {
                file.createNewFile();
            }

            //if file.length is more than 1M, delete the file and create a new file
            if (file.length() > 1024 * 1024) {
                file.delete();
                file = new File(filePath);
                file.createNewFile();
            }

            FileInputStream fin = new FileInputStream(filePath);
            StringBuffer sb = new StringBuffer();

            BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
            String line = new String();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            fin.close();

            if (sb.length() > 0) {
                //修改已有文件
                existObject = new JSONObject(sb.toString());
                Iterator<String> iterator = saveObject.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    JSONArray newDataArray = saveObject.getJSONArray(key);

                    if (existObject.has(key)) {
                        JSONArray existDataArray = existObject.getJSONArray(key);
                        existDataArray.put(newDataArray.get(0));
                    } else {
                        existObject.put(key, saveObject.getJSONArray(key));
                    }
                }

                FileOutputStream fileOutputStream = new FileOutputStream(filePath, false);
                fileOutputStream.write(existObject.toString().getBytes());
                fileOutputStream.flush();
                fileOutputStream.close();
                Logger.i(TAG, "save exist info finished, filePath = " + filePath);
            } else {
                //存新文件
                Iterator<String> iterator = saveObject.keys();
                JSONObject jsonObject = new JSONObject();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    JSONArray array = saveObject.getJSONArray(key);
                    jsonObject.put(key, array);
                }
                jsonObject.put("appkey", AppInfo.getAppKey());

                FileOutputStream fileOutputStream = new FileOutputStream(filePath, false);
                fileOutputStream.write(jsonObject.toString().getBytes());
                fileOutputStream.flush();
                fileOutputStream.close();
                Logger.i(TAG, "save info finished, filePath = " + filePath);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
