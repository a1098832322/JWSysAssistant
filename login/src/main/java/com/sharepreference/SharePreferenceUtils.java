package com.sharepreference;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by 10988 on 2017/4/24.
 */

public class SharePreferenceUtils {
    public void SaveData(Context context, String number, String passwd, String name) {
        //指定操作的文件名称
        SharedPreferences share = context.getSharedPreferences("student", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = share.edit(); //编辑文件
        edit.putString("number", number);         //根据键值对添加数据
        edit.putString("passwd", passwd);
        edit.putString("name", name);
        edit.commit();  //保存数据信息
    }

    public static boolean isLogin(Context context) {
        SharedPreferences share = context.getSharedPreferences("student", Context.MODE_PRIVATE);

        String name = share.getString("name", null);
        if (name != null) {
            return true;
        }
        return false;
    }

    public List<HashMap<String, String>> LoadData(Context context) {
        //指定操作的文件名称
        SharedPreferences share = context.getSharedPreferences("student", Context.MODE_PRIVATE);

        HashMap<String, String> map = new HashMap<>();
        map.put("number", share.getString("number", ""));
        map.put("passwd", share.getString("passwd", ""));
        map.put("name", share.getString("name", ""));
        List<HashMap<String, String>> student = new ArrayList<>();
        student.add(map);

        return student;

    }
}
