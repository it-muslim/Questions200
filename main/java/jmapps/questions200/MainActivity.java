package jmapps.questions200;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.Arrays;

import jmapps.questions200.Adapter.ListAppsAdapter;
import jmapps.questions200.Adapter.SectionsPagerAdapter;
import jmapps.questions200.Fragment.BookmarkList;
import jmapps.questions200.Fragment.ChapterList;
import jmapps.questions200.Fragment.PlaceholderFragment;

import static jmapps.questions200.Model.ListAppModel.listAppModel;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        BookmarkList.CurrentPosition, ChapterList.CurrentPosition,
        PlaceholderFragment.PagesProgress, RadioGroup.OnCheckedChangeListener {

    private static final String keyNightMode = "key_night_mode";
    private static final String keyLastViewPagerPosition = "key_last_view_pager_position";

    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    private ViewPager mViewPager;
    private MenuItem itemNightMode;

    private RadioButton rbFontOne;
    private RadioButton rbFontTwo;
    private RadioButton rbFontThree;

    private ProgressBar pbScrollPagesProgress;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (MainApplication.getInstance().isNightModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_main);

        AppBarLayout appbarMain = findViewById(R.id.appbar_main);

        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mPreferences.edit();

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        pbScrollPagesProgress = findViewById(R.id.pb_scroll_pages_progress);

        mViewPager = findViewById(R.id.container_main);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        final FloatingActionButton fabChapters = findViewById(R.id.fab_main_chapters);
        fabChapters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChapterList chapterList = new ChapterList();
                chapterList.show(getSupportFragmentManager(), "chapter_list");
            }
        });

        appbarMain.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int scroll) {
                if (scroll != 0) {
                    fabChapters.hide();
                } else {
                    fabChapters.show();
                }
            }
        });

        boolean firstLaunch = mPreferences.getBoolean("key_for_first_launch", true);

        if (firstLaunch) {
            mEditor.putBoolean("key_for_first_launch", false).apply();
            supportDialog();
        }

        loadLastPosition();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        itemNightMode = menu.findItem(R.id.action_night_mode);
        boolean nightModeState = mPreferences.getBoolean(keyNightMode, false);
        itemNightMode.setChecked(nightModeState);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bookmark_list:
                BookmarkList bookmarkList = new BookmarkList();
                bookmarkList.show(getSupportFragmentManager(), "bookmark_list");
                break;
            case R.id.action_night_mode:
                if (!itemNightMode.isChecked()) {
                    recreateActivity(true);
                } else {
                    recreateActivity(false);
                }
                mEditor.putBoolean(keyNightMode, itemNightMode.isChecked()).apply();
                break;
            case R.id.action_settings:
                bottomSheetSettings();
                break;
            case R.id.action_support:
                supportDialog();
                break;
            case R.id.action_list_apps:
                bottomSheetListApps();
                break;
            case R.id.action_about_us:
                aboutUsDialog();
                break;
            case R.id.action_share:
                shareAppLink();
                break;
            case R.id.action_exit:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveLastPosition();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveLastPosition();
    }

    @Override
    public void onClick(View v) {

        int valueIndent = mPreferences.getInt("key_indent_size", 16);
        int valueSize = mPreferences.getInt("key_text_size", 18);

        switch (v.getId()) {
            case R.id.btn_indent_minus:
                if (valueIndent > 12) {
                    valueIndent--;
                }
                break;
            case R.id.btn_indent_plus:
                if (valueIndent <= 50) {
                    valueIndent++;
                }
                break;
            case R.id.btn_size_minus:
                if (valueSize > 12) {
                    valueSize--;
                }
                break;
            case R.id.btn_size_plus:
                if (valueSize <= 50) {
                    valueSize++;
                }
                break;
        }

        mEditor.putInt("key_indent_size", valueIndent);
        mEditor.putInt("key_text_size", valueSize);
        mEditor.apply();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        mEditor.putBoolean("key_font_one", rbFontOne.isChecked());
        mEditor.putBoolean("key_font_two", rbFontTwo.isChecked());
        mEditor.putBoolean("key_font_three", rbFontThree.isChecked());
        mEditor.apply();
    }

    @Override
    public void setCurrentPosition(long id) {
        mViewPager.setCurrentItem((int) id);
    }

    @Override
    public void setPageProgress(int sectionPosition) {
        pbScrollPagesProgress.setProgress(sectionPosition);
    }

    private void saveLastPosition() {
        mEditor.putInt(keyLastViewPagerPosition, mViewPager.getCurrentItem()).apply();
    }

    private void loadLastPosition() {
        mViewPager.setCurrentItem(mPreferences.getInt(keyLastViewPagerPosition, 0));
    }

    private void recreateActivity(boolean nightModeState) {
        saveLastPosition();
        itemNightMode.setChecked(nightModeState);
        MainApplication.getInstance().setIsNightModeEnabled(nightModeState);
        Intent recreate = new Intent(MainActivity.this, MainActivity.class);
        startActivity(recreate);
        finish();
        overridePendingTransition(0, 0);
    }

    private void supportDialog() {
        final AlertDialog.Builder support = new AlertDialog.Builder(this);

        TextView supportText = new TextView(this);
        supportText.setText(R.string.action_support_text);
        supportText.setPadding(25, 25, 25, 25);
        supportText.setTextSize(18);
        supportText.setAutoLinkMask(Linkify.ALL);
        supportText.setTextIsSelectable(true);

        support.setView(supportText)
                .setIcon(R.drawable.ic_money_accent)
                .setTitle("Поддержать проект")
                .setCancelable(false);

        support.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        support.create().show();
    }

    @SuppressLint("InflateParams")
    private void bottomSheetSettings() {
        BottomSheetDialog dialogSettings = new BottomSheetDialog(this, R.style.BottomSheetDialog);
        View settingRoot = getLayoutInflater().inflate(R.layout.bottom_sheet_settings, null);
        dialogSettings.setContentView(settingRoot);

        Button btnIndentMinus = settingRoot.findViewById(R.id.btn_indent_minus);
        Button btnIndentPlus = settingRoot.findViewById(R.id.btn_indent_plus);
        Button btnSizeMinus = settingRoot.findViewById(R.id.btn_size_minus);
        Button btnSizePlus = settingRoot.findViewById(R.id.btn_size_plus);

        RadioGroup rgFontGroup = settingRoot.findViewById(R.id.rg_font_group);
        rbFontOne = settingRoot.findViewById(R.id.rb_font_one);
        rbFontTwo = settingRoot.findViewById(R.id.rb_font_two);
        rbFontThree = settingRoot.findViewById(R.id.rb_font_three);

        boolean fontOneState = mPreferences.getBoolean("key_font_one", true);
        boolean fontTwoState = mPreferences.getBoolean("key_font_two", false);
        boolean fontThreeState = mPreferences.getBoolean("key_font_three", false);

        rbFontOne.setChecked(fontOneState);
        rbFontTwo.setChecked(fontTwoState);
        rbFontThree.setChecked(fontThreeState);

        btnIndentMinus.setOnClickListener(this);
        btnIndentPlus.setOnClickListener(this);
        btnSizeMinus.setOnClickListener(this);
        btnSizePlus.setOnClickListener(this);

        rgFontGroup.setOnCheckedChangeListener(this);
        dialogSettings.show();
    }

    @SuppressLint("InflateParams")
    private void bottomSheetListApps() {
        BottomSheetDialog dialogListApp = new BottomSheetDialog(this, R.style.BottomSheetDialog);
        View listAppsRoot = getLayoutInflater().inflate(R.layout.bottom_sheet_list_apps, null);
        dialogListApp.setContentView(listAppsRoot);

        RecyclerView rvListApps = listAppsRoot.findViewById(R.id.rv_list_apps);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvListApps.setLayoutManager(linearLayoutManager);
        rvListApps.setHasFixedSize(true);

        ListAppsAdapter listAppsAdapter = new ListAppsAdapter(Arrays.asList(listAppModel));
        rvListApps.setAdapter(listAppsAdapter);

        dialogListApp.show();
    }

    private void aboutUsDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        @SuppressLint("InflateParams")
        View dialogAboutUs = inflater.inflate(R.layout.dialog_about_us, null);

        AlertDialog.Builder instructionDialog = new AlertDialog.Builder(this);

        instructionDialog.setView(dialogAboutUs);
        TextView tvAboutUsContent = dialogAboutUs.findViewById(R.id.tv_about_us_content);
        tvAboutUsContent.setMovementMethod(LinkMovementMethod.getInstance());

        instructionDialog.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        instructionDialog.create().show();
    }

    private void shareAppLink() {
        String strAppLink = "https://play.google.com/store/apps/details?id=jmapps.questions200";

        Intent shareLink = new Intent(Intent.ACTION_SEND);
        shareLink.setType("text/plain");
        shareLink.putExtra(Intent.EXTRA_TEXT,
                "Советую приложение:\n" +
                        "200 вопросов по вероучениюю Ислама\n" + strAppLink);
        startActivity(shareLink);
    }
}