package com.jwtest.wishes.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.GlobalVariable;
import com.custom.toast.LoadToast;
import com.fragement.part.Grade;
import com.fragement.part.OneKey;
import com.fragement.part.Test;
import com.login.method.LoginPage;
import com.sharepreference.SharePreferenceUtils;
import com.sqlite.utils.SQLiteHelper;

import java.util.ArrayList;
import java.util.List;

import static com.jwtest.wishes.myapplication.R.id.username;
import static com.jwtest.wishes.myapplication.R.id.usernumber;

public class LoginActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private View view;
    private TextView tvName, tvNumber;
    private List<Fragment> mList = null;

    //声明自定义Toast
    LoadToast lt = null;

    private SQLiteDatabase sqlitedb = null;

    //检查并显示登录信息
    private SharedPreferences sp = null;
    private boolean isLoginFlag = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //实例化自定义Toast对象
        //先获取屏幕高度
        Display display = getWindowManager().getDefaultDisplay();
        int height = display.getHeight();
        GlobalVariable.height = height;
        //实例化Toast
        //lt = new LoadToast(this).setProgressColor(Color.RED).setTranslationY(height);

        //实例化SharePreferences
        sp = getApplicationContext().getSharedPreferences("student", Context.MODE_PRIVATE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //创建数据库
        SQLiteHelper database = SQLiteHelper.getInstance(this);
        sqlitedb = database.getReadableDatabase();// 创建数据库

        /**
         * create Fragment View
         */

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mList = new ArrayList<>();

        OneKey mOnePage = new OneKey();
        Grade mGradePage = new Grade();
        Test mTestPage = new Test();
        //Sport mSportPage = new Sport();//还没写完

        mList.add(mGradePage);
        mList.add(mTestPage);
        mList.add(mOnePage);
        //mList.add(mSportPage);


        //构建Fragment
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), mList);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.content);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(4);//设置装载的Fragment数量

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        /**
         * Create Navigation View
         */
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);


        //动态生成navigationLatout的head
        view = navigationView.getHeaderView(0);
        tvName = (TextView) view.findViewById(username);
        tvNumber = (TextView) view.findViewById(usernumber);

        tvName.setText("(●'◡'●)");
        tvNumber.setText("如果你看到这行小字说明你还没有登录噢");

        //检测SharedPreferences
        sp = getApplicationContext().getSharedPreferences("student", Context.MODE_PRIVATE);

        //判断是否登录过
        if (SharePreferenceUtils.isLogin(getApplicationContext())) {
            //更新UI
            refreshUI();
        }
        navigationView.setNavigationItemSelectedListener(this);

        IntentFilter filter = new IntentFilter(LoginPage.action);
        registerReceiver(receiver, filter);
    }

    private void refreshUI() {
        String username = null;
        String usernumber = null;

        //设置未登录状态下默认显示的文字
        usernumber = sp.getString("number", "如果你看到这行小字说明你还没有登录噢");
        username = sp.getString("name", "(●'◡'●)");
        if (username != null && usernumber != null) {
            tvName.setText(username);
            tvNumber.setText(usernumber);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent = new Intent();
        if (id == R.id.nav_login) {
            if (SharePreferenceUtils.isLogin(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), "你已经登陆过啦！", Toast.LENGTH_SHORT).show();
                //lt.setText("你已经登陆过啦！").show().during(5000, 0);
            } else {
                //跳转页面
                intent.setClass(LoginActivity.this, LoginPage.class);
                startActivity(intent);
            }


        } else if (id == R.id.nav_logout) {
            SharedPreferences.Editor edit = sp.edit();
            edit.clear();//清空
            edit.commit();//提交
            refreshUI();//刷新UI
            Toast.makeText(this, "已注销，请重新登录", Toast.LENGTH_LONG).show();
            //lt.setText("已注销，请重新登录").show().during(5000, 1);
        } else if (id == R.id.nav_about) {
            //Toast.makeText(this, "关于方法", Toast.LENGTH_SHORT).show();
            Intent intent1 = new Intent();
            intent1.setClass(LoginActivity.this, About.class);
            startActivity(intent1);
        } else if (id == R.id.nav_help) {
            //Toast.makeText(this, "帮助", Toast.LENGTH_SHORT).show();
            Intent intent2 = new Intent();
            intent2.setClass(LoginActivity.this, Help.class);
            startActivity(intent2);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //利用广播刷新UI
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            tvName.setText(intent.getExtras().getString("name"));
            tvNumber.setText(intent.getExtras().getString("number"));
        }
    };


    //Fragment Class
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText("Fragment " + rootView.getId());
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        List<Fragment> mList = null;
        boolean misLoginFlag = false;

        public SectionsPagerAdapter(FragmentManager fm, List<Fragment> mFragmentList) {
            super(fm);
            mList = mFragmentList;
            misLoginFlag = isLoginFlag;
        }

        @Override
        public Fragment getItem(int position) {
            // 如果已登录，则显示功能菜单，否则显示准备菜单
            //            if (!misLoginFlag) {
            //                return PlaceholderFragment.newInstance(position);
            //            }

            return mList.get(position);
        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return mList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "成绩查询";
                case 1:
                    return "考试安排";
                case 2:
                    return "一键评教";
                case 3:
                    return "体测查询";
            }
            return null;
        }
    }

}
