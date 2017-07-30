package com.fragement.part;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.GlobalVariable;
import com.jwtest.wishes.myapplication.R;
import com.sharepreference.SharePreferenceUtils;
import com.sqlite.utils.SQLiteHelper;

import net.utils.OKHttpUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import analysis.utils.DecodeHTML;
import listview.adapter.MyCustomsBaseAdapter;
import listview.adapter.SwingRightInAnimationAdapter;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

/**
 * Created by 10988 on 2017/4/26.
 */

public class OneKey extends Fragment {
    private Spinner sp1, sp2;
    private ListView listView = null;
    private TextView mTime = null;
    private TextView mRefresh = null;
    private Button sender = null;
    private ImageView arrow;
    private View mGradeBodyTitleView;
    private View mRealTitleView;
    private View mGradeBodyBtn;

    private View mProgressView;
    private View mGradeBodyView;
    private List<HashMap<String, String>> data = new ArrayList<>();

    //是否点击显示菜单按钮
    private boolean isclicked = false;

    //网络
    private getData mGetDataTask = null;
    //重新登录
    private ReLoginTask mReloginTask = null;

    //构造POST数据
    private String post_str_year = "2016";
    private String post_str_term = "0";

    //Hanlder
    public Handler refreshUIHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Toast.makeText(getActivity().getApplicationContext(), "请求失败，请重试或重新登陆！", Toast.LENGTH_SHORT).show();
                    ReLoginDialog dialog = new ReLoginDialog(getActivity(), new ReLoginDialog.DialogCallBackListener() {
                        @Override
                        public void callBack(String msg) {
                            //回调时登录
                            SharedPreferences sp = getActivity().getApplicationContext().getSharedPreferences("student", Context.MODE_PRIVATE);
                            String number = sp.getString("number", "");
                            String passwd = sp.getString("passwd", "");
                            String code = msg;
                            showProgress(true);
                            mReloginTask = new ReLoginTask(number, passwd, code);
                            mReloginTask.execute((Void) null);

                        }
                    });
                    dialog.setCanceledOnTouchOutside(false);//触摸dialog外部范围不可取消
                    dialog.show();
                    break;
                case 1:
                    //ListView绑定数据源
                    //先清空上次数据
                    data = new ArrayList<>();
                    for (String s : (List<String>) msg.obj) {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("text", s);
                        data.add(map);
                    }

                    //绑定数据到控件
                    MyCustomsBaseAdapter adapter = new MyCustomsBaseAdapter(getActivity(), data, R.layout.result_list);

                    //设置动画
                    SwingRightInAnimationAdapter swingRightInAnimationAdapter = new SwingRightInAnimationAdapter(adapter);
                    swingRightInAnimationAdapter.setListView(listView);
                    listView.setAdapter(swingRightInAnimationAdapter);


                    //显示信息
                    Toast.makeText(getActivity(), "恭喜，一键给老师好评成功！", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    String sub = "上次刷新时间：";
                    SimpleDateFormat sdf = new SimpleDateFormat("YYYY年MM月dd日HH:mm:ss");
                    String time = new String(sdf.format(new Date()));
                    sub += time;
                    mTime.setText(sub);


                    //將刷新时间存入数据库
                    SQLiteHelper helper = SQLiteHelper.getInstance(getActivity().getApplicationContext());
                    helper.insert("REFRESHTIMETABLE", "REFRESHTIME", "3", time);
                    break;
            }
        }
    };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_onekey, container, false);
        init(v);

        //从数据库中取出相应值
        SQLiteHelper helper = SQLiteHelper.getInstance(getActivity().getApplicationContext());
        String refreshTime = helper.getRefreshTime("REFRESHTIMETABLE", "1");

        mTime.setText("上次刷新时间：" + refreshTime);

        mRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SharePreferenceUtils.isLogin(getActivity().getApplicationContext())) {
                    reLoginFunc();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "你还没有登录，请先登录！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //隐藏菜单功能按钮点击事件
        mGradeBodyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isclicked = !isclicked;//取反改变状态
                showHideMenu(isclicked);
            }
        });

        sender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == (R.id.grade_btn_showgrade)) {
                    if (!SharePreferenceUtils.isLogin(getActivity().getApplicationContext())) {
                        Toast.makeText(getActivity().getApplicationContext(), "你还没有登录，请先登录！", Toast.LENGTH_SHORT).show();
                        listView.removeAllViewsInLayout();
                        return;
                    }
                    doPost();
                }
            }
        });

        sp1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int value = ((ValueMap) sp1.getSelectedItem()).getValue();
                post_str_year = String.valueOf(value);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sp1.setPrompt("2016");

        sp2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int value = ((ValueMap) sp2.getSelectedItem()).getValue();
                post_str_term = String.valueOf(value);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sp2.setPrompt("第一学期");

        return v;
    }

    private void showHideMenu(boolean isclicked) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);//设置时间
        //默认情况下未点击button，则点击时显示，再点击则取消
        mRealTitleView.setVisibility(isclicked ? View.VISIBLE : View.GONE);
        if (isclicked) {
            //mGradeBodyView.animate().setDuration(shortAnimTime);
            Animation titleAnim = AnimationUtils.loadAnimation(this.getActivity(), R.anim.titlemenu_in_anim);

            //设置图标旋转
            Animation arrowRotateAnim = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            arrowRotateAnim.setFillAfter(true);
            arrowRotateAnim.setDuration(500);
            arrowRotateAnim.setRepeatCount(0);
            arrowRotateAnim.setInterpolator(new LinearInterpolator());
            arrow.startAnimation(arrowRotateAnim);


            mGradeBodyTitleView.setAnimation(titleAnim);
        } else {
            Animation anim = AnimationUtils.loadAnimation(this.getActivity(), R.anim.titlemenu_out_anim);
            Animation titleHideAnim = AnimationUtils.loadAnimation(this.getActivity(), R.anim.title_hide_anim);

            //设置图标旋转
            Animation arrowRotateAnim = new RotateAnimation(180, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            arrowRotateAnim.setFillAfter(true);
            arrowRotateAnim.setDuration(300);
            arrowRotateAnim.setRepeatCount(0);
            arrowRotateAnim.setInterpolator(new LinearInterpolator());
            arrow.startAnimation(arrowRotateAnim);

            mRealTitleView.setAnimation(titleHideAnim);
            mGradeBodyTitleView.setAnimation(anim);

        }
    }

    private void init(View v) {
        mProgressView = v.findViewById(R.id.grade_progress);
        mGradeBodyView = v.findViewById(R.id.grade_body);
        mGradeBodyTitleView = v.findViewById(R.id.grade_body_title);
        mRealTitleView = v.findViewById(R.id.grade_body_title_realtitle);
        mGradeBodyBtn = v.findViewById(R.id.grade_body_btn);
        arrow = (ImageView) v.findViewById(R.id.grade_body_showhide_btn_img);

        listView = (ListView) v.findViewById(R.id.one_list);
        mTime = (TextView) v.findViewById(R.id.grade_tv_time);
        mRefresh = (TextView) v.findViewById(R.id.grade_tv_refresh);

        sender = (Button) v.findViewById(R.id.grade_btn_showgrade);

        sp1 = (Spinner) v.findViewById(R.id.grade_sp_xuenian);
        sp2 = (Spinner) v.findViewById(R.id.grade_sp_xueqi);


        //给Spinner绑定数据
        Resources res = getResources();
        String[] year = res.getStringArray(R.array.xuenian);
        String[] term = res.getStringArray(R.array.xueqi);

        ArrayAdapter<ValueMap> yearAdapter = new ArrayAdapter<ValueMap>(v.getContext(), android.R.layout.simple_dropdown_item_1line,
                bindYearAdapter(year));
        sp1.setAdapter(yearAdapter);

        ArrayAdapter<ValueMap> termAdapter = new ArrayAdapter<ValueMap>(v.getContext(), android.R.layout.simple_dropdown_item_1line,
                bindOtherAdapter(term));
        sp2.setAdapter(termAdapter);
    }

    private List<ValueMap> bindYearAdapter(String[] keys) {
        int i = 2016;
        List<ValueMap> list = new ArrayList<>();
        for (String k : keys) {
            ValueMap map = new ValueMap(k, i);
            i--;
            list.add(map);
        }

        return list;
    }

    private List<ValueMap> bindOtherAdapter(String[] keys) {
        int i = 0;
        List<ValueMap> list = new ArrayList<>();
        for (String k : keys) {
            ValueMap map = new ValueMap(k, i);
            i++;
            list.add(map);
        }

        return list;
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mGradeBodyView.setVisibility(show ? View.GONE : View.VISIBLE);
            mGradeBodyView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mGradeBodyView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mGradeBodyView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /*网络请求*/
    private void reLoginFunc() {
        //强制输入验证码刷新成绩
        Message msg = Message.obtain();
        msg.what = 0;
        refreshUIHandler.sendMessage(msg);
    }

    private void doPost() {
        if (mGetDataTask != null) {
            return;
        }

        boolean cancle = false;
        SharedPreferences sp = getActivity().getApplicationContext().getSharedPreferences("student", Context.MODE_PRIVATE);
        String name = sp.getString("name", null);
        if (name == null) {
            cancle = true;
        }


        if (cancle) {
            showProgress(false);
        } else {
            showProgress(true);
            mGetDataTask = new getData(post_str_year, post_str_term);
            mGetDataTask.execute((Void) null);
        }
    }

    //异步网络请求
    public class getData extends AsyncTask<Void, Void, Boolean> {
        private final String xn;
        private final String xq;
        private final String moshi = "0";
        private final String type = "4";

        getData(String _xn, String _xq) {
            this.xn = _xn;
            this.xq = _xq;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (getScoData(GlobalVariable.OneKeyUrl, xq, xn, moshi, type)) {
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mGetDataTask = null;
            showProgress(false);

            Message msg = Message.obtain();
            msg.what = 2;
            refreshUIHandler.sendMessage(msg);

        }

        @Override
        protected void onCancelled() {
            mGetDataTask = null;
            showProgress(false);
        }


        private boolean getScoData(String url, String xq, String xn, String sj, String type) {
            OkHttpClient client = OKHttpUtils.getInstanceClient();
            try {
                RequestBody formBody = new FormBody.Builder().add("xn", xn)
                        .add("xq", xq).add("moshi", moshi).add("type", type).build();
                Log.e("xn&xq", xn + "\t" + xq);
                Request request = new Request.Builder().addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8").post
                        (formBody).url(url)
                        .build();
                //执行POST请求
                Response response = client.newCall(request).execute();

                //获取返回数据
                String result = response.body().string();
                DecodeHTML decoder = new DecodeHTML();
                List<String> name = decoder.getClassName(result);
                if (name.size() != 0) {
                    //如果解析结果不为空
                    Message msg = Message.obtain();
                    msg.what = 1;
                    msg.obj = name;
                    refreshUIHandler.sendMessage(msg);
                    return true;
                }

            } catch (Exception e) {
                Log.e(TAG, "getScoData: 网络请求获取一键评教异常！", e);
            }
            Message msg = Message.obtain();
            msg.what = 0;
            refreshUIHandler.sendMessage(msg);
            return false;
        }
    }

    //重新登录的网络请求类
    public class ReLoginTask extends AsyncTask<Void, Void, Boolean> {
        private final String mNumber;
        private final String mPassword;
        private final String mCodeNumber;

        public ReLoginTask(String number, String password, String code) {
            mNumber = number;
            mPassword = password;
            mCodeNumber = code;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                //再附带Cookies登陆
                OKHttpUtils post = OKHttpUtils.getInstanceUtils();
                String result = post.okHttp_postFromParameters(GlobalVariable.LoginUrl, mNumber, mPassword, mCodeNumber);
                DecodeHTML decode = new DecodeHTML();
                if (decode.isLoginSuccess(result) == null) {
                    return false;
                }
            } catch (Exception e) {
                Log.e("TAG", "doInBackground: ", e);
                return false;
            }


            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mReloginTask = null;
            showProgress(false);

            if (success) {

            } else {

            }
            doPost();
        }

        @Override
        protected void onCancelled() {
            mReloginTask = null;
            showProgress(false);
        }
    }
}
