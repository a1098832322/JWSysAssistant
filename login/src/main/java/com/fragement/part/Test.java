package com.fragement.part;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.GlobalVariable;
import com.image.utils.SimpleSampleActivity;
import com.jwtest.wishes.myapplication.R;
import com.sharepreference.SharePreferenceUtils;
import com.sqlite.utils.SQLiteHelper;

import net.utils.OKHttpUtils;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import analysis.utils.DecodeHTML;

import static android.content.ContentValues.TAG;


/**
 * Created by 10988 on 2017/4/26.
 */

public class Test extends Fragment {
    private Spinner sp1, sp2;
    private Button sender;
    private ImageView img;
    private ImageView arrow;
    private View mProgressView;
    private View mGradeBodyView;
    private View mGradeBodyTitleView;
    private View mRealTitleView;
    private View mGradeBodyBtn;

    private TextView mTime, mRefresh;

    private getData mGetDataTask = null;
    //重新登录
    private ReLoginTask mReloginTask = null;

    //是否点击显示菜单按钮
    private boolean isclicked = false;

    //构造POST数据
    private String post_str_year = "2016";
    private String post_str_term = "0";


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
                    img.setImageBitmap((Bitmap) msg.obj);
                    break;
                case 2:
                    String sub = "上次刷新时间：";
                    SimpleDateFormat sdf = new SimpleDateFormat("YYYY年MM月dd日HH:mm:ss");
                    String time = new String(sdf.format(new Date()));
                    sub += time;
                    mTime.setText(sub);


                    //將刷新时间存入数据库
                    SQLiteHelper helper = SQLiteHelper.getInstance(getActivity().getApplicationContext());
                    helper.insert("REFRESHTIMETABLE", "REFRESHTIME", "2", time);
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_test, container, false);
        init(v);//初始化UI界面

        //从数据库中取出相应值
        SQLiteHelper helper = SQLiteHelper.getInstance(getActivity().getApplicationContext());
        String refreshTime = helper.getRefreshTime("REFRESHTIMETABLE", "2");

        //设置ImageView点击事件
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Bitmap bitmap = ((BitmapDrawable) img.getDrawable()).getBitmap();

                    Intent intent = new Intent(getActivity(), SimpleSampleActivity.class);

                    //不能直接传递大于40k的图片，所以要把bitmap存储为byte数组，然后再通过Intent传递。
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] bitmapByte = baos.toByteArray();
                    //构建文件名称
                    String bitName = sp1.getSelectedItem() + " " + sp2.getSelectedItem();

                    intent.putExtra("bitmapName", bitName);
                    intent.putExtra("bitmap", bitmapByte);
                    startActivity(intent);
                } catch (Exception e) {
                    return;
                }
            }
        });

        mTime.setText("上次刷新时间：" + refreshTime);

        //隐藏菜单功能按钮点击事件
        mGradeBodyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isclicked = !isclicked;//取反改变状态
                showHideMenu(isclicked);
            }
        });

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

        sender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == (R.id.grade_btn_showgrade)) {
                    if (!SharePreferenceUtils.isLogin(getActivity().getApplicationContext())) {
                        Toast.makeText(getActivity().getApplicationContext(), "你还没有登录，请先登录！", Toast.LENGTH_SHORT).show();
                        img.setImageBitmap(null);
                        return;
                    }
                    doGet();
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

    private void reLoginFunc() {
        //强制输入验证码刷新成绩
        Message msg = Message.obtain();
        msg.what = 0;
        refreshUIHandler.sendMessage(msg);
    }

    private void doGet() {
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

    private void init(View v) {
        mProgressView = v.findViewById(R.id.grade_progress);
        mGradeBodyView = v.findViewById(R.id.grade_body);
        mGradeBodyTitleView = v.findViewById(R.id.grade_body_title);
        mRealTitleView = v.findViewById(R.id.grade_body_title_realtitle);
        mGradeBodyBtn = v.findViewById(R.id.grade_body_btn);

        mTime = (TextView) v.findViewById(R.id.grade_tv_time);
        mRefresh = (TextView) v.findViewById(R.id.grade_tv_refresh);

        sp1 = (Spinner) v.findViewById(R.id.grade_sp_xuenian);
        sp2 = (Spinner) v.findViewById(R.id.grade_sp_xueqi);

        sender = (Button) v.findViewById(R.id.grade_btn_showgrade);
        img = (ImageView) v.findViewById(R.id.grade_img_gradeimg);
        arrow = (ImageView) v.findViewById(R.id.grade_body_showhide_btn_img);

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


    //异步网络请求
    public class getData extends AsyncTask<Void, Void, Boolean> {
        private final String xn;
        private final String xq;

        getData(String _xn, String _xq) {
            this.xn = _xn;
            this.xq = _xq;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (getImgData(GlobalVariable.ExamPicUrl, xq, xn)) {
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


        private boolean getImgData(String url, String xq, String xn) {
            OKHttpUtils utils = OKHttpUtils.getInstanceUtils();
            try {
                String imgUrl = url + "xn=" + xn + "&xq=" + xq;
                Log.e("imgURL:", imgUrl);
                utils.okHttp_asynchronousGet(imgUrl, refreshUIHandler);
                return true;
            } catch (Exception e) {
                Log.e(TAG, "getImgData: 网络请求获取成绩图片异常！", e);
            }
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
            doGet();
        }

        @Override
        protected void onCancelled() {
            mReloginTask = null;
            showProgress(false);
        }
    }


}


