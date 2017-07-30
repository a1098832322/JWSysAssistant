package com.login.method;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.GlobalVariable;
import com.jwtest.wishes.myapplication.R;
import com.sharepreference.SharePreferenceUtils;

import net.utils.OKHttpUtils;
import net.utils.VerificationCode;

import analysis.utils.DecodeHTML;

/**
 * Created by 10988 on 2017/4/24.
 */

public class LoginPage extends AppCompatActivity {
    //Broadcast接收器
    public static final String action = "Refresh UI";

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mNumberView;
    private EditText mPasswordView;
    private EditText mCode;
    private View mProgressView;
    private View mLoginFormView;
    private ImageView mImgBack;
    private ImageView mImgCode;//存放验证码
    private TextView mTitle;


    /**
     * hanlder用于接收验证码图片
     */
    //验证码状态
    public static final int getCodeSuccess = 1;
    public static final int getCodeError = 0;

    public final Handler imghandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case getCodeSuccess:
                    mImgCode.setImageBitmap((Bitmap) msg.obj);
                    break;
                case getCodeError:
                    Log.e("TAG", "hanlder获取验证码失败！");
                    break;
            }
        }
    };


    //判断是否登录成功
    private DecodeHTML decode = new DecodeHTML();

    //登录状态
    public Handler loginHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Toast.makeText(LoginPage.this, "登陆失败，请核对用户名密码以及验证码后再重新登录", Toast.LENGTH_LONG).show();
                    break;
                case 1:
                    Toast.makeText(LoginPage.this, "登陆成功", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.login_page);
        //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.title_layout);
        //设置Title bar
        mImgBack = (ImageView) findViewById(R.id.title_btn_back);
        //返回事件
        mImgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //标题
        mTitle = (TextView) findViewById(R.id.title_text);
        mTitle.setText("登录");

        // Set up the login form.
        mNumberView = (EditText) findViewById(R.id.number);
        mPasswordView = (EditText) findViewById(R.id.password);

        //验证码点击事件
        mCode = (EditText) findViewById(R.id.edt_code);

        mImgCode = (ImageView) findViewById(R.id.img_code);
        //验证码加载时自动刷新
        VerificationCode.getCode(imghandler);

        mImgCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击可刷新验证码
                VerificationCode.getCode(imghandler);
            }
        });

        Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mNumberView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String number = mNumberView.getText().toString();
        String password = mPasswordView.getText().toString();
        String code = mCode.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!isPasswordValid(password)) {
            Animation anim = AnimationUtils.loadAnimation(LoginPage.this, R.anim.myanim);
            mPasswordView.setAnimation(anim);
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }
        if (!isCodeValid(code)) {
            mCode.setError(getString(R.string.error_invalid_code));
            focusView = mCode;
            cancel = true;
        }

        // Check for a valid school number.
        if (!isNumberValid(number)) {
            mNumberView.setError(getString(R.string.error_field_required));
            focusView = mNumberView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(number, password, code);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isCodeValid(String code) {
        //TODO: Replace this with your own logic
        return code.length() == 4 ? true : false;
    }

    private boolean isNumberValid(String number) {
        //TODO: Replace this with your own logic
        return number.length() == 12 ? true : false;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4 ? true : false;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        private final String mNumber;
        private final String mPassword;
        private final String mCodeNumber;

        public UserLoginTask(String number, String password, String code) {
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
                if (decode.isLoginSuccess(result) == null) {
                    Message message = Message.obtain();
                    message.what = 0;
                    loginHandler.sendMessage(message);
                    return false;
                }
                //存入账号密码数据到SharePreference中
                String name = decode.isLoginSuccess(result);
                SharePreferenceUtils sp = new SharePreferenceUtils();
                sp.SaveData(getApplicationContext(), mNumber, mPassword, name);

                //返回登陆成功的消息给handler
                Message message = Message.obtain();
                message.what = 1;
                loginHandler.sendMessage(message);
            } catch (Exception e) {
                Log.e("TAG", "doInBackground: ", e);
                Message message = Message.obtain();
                message.what = 0;
                loginHandler.sendMessage(message);
                return false;
            }


            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                SharedPreferences sp = getApplicationContext().getSharedPreferences("student", Context.MODE_PRIVATE);
                Intent intent = new Intent(action);
                intent.putExtra("name", sp.getString("name", "null"));
                intent.putExtra("number", sp.getString("number", "null"));
                sendBroadcast(intent);
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_login_error));
                mPasswordView.requestFocus();
                //并刷新验证码
                VerificationCode.getCode(imghandler);
                Message message = Message.obtain();
                message.what = 0;
                loginHandler.sendMessage(message);
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }


}
