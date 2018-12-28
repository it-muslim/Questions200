package jmapps.questions200.Fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Objects;

import jmapps.questions200.DBSetup.DBAssetHelper;
import jmapps.questions200.R;
import jmapps.questions200.TypeFace.TypeFaces;

public class PlaceholderFragment extends Fragment implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private int sectionNumber;

    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    private SQLiteDatabase sqLiteDatabase;

    private TextView tvQuestionText;
    private TextView tvAnswerText;

    private String strQuestionNumber;
    private String strQuestionText;
    private String strAnswerText;

    private String strFootnoteId;
    private String strFootnoteContent;

    private PagesProgress pagesProgress;

    public static PlaceholderFragment newInstance(int sectionNumber) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        setRetainInstance(true);

        if (getArguments() != null) {
            sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
        }

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mEditor = mPreferences.edit();

        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .registerOnSharedPreferenceChangeListener(this);

        DBAssetHelper dbAssetHelper = new DBAssetHelper(getActivity());
        sqLiteDatabase = dbAssetHelper.getReadableDatabase();

        TextView tvQuestionNumber = rootView.findViewById(R.id.tv_question_number);
        tvQuestionText = rootView.findViewById(R.id.tv_question_text);
        tvAnswerText = rootView.findViewById(R.id.tv_answer_text);

        ToggleButton tbAddRemoveBookmark = rootView.findViewById(R.id.tb_add_remove_bookmark);
        Button btnCopyContent = rootView.findViewById(R.id.btn_copy_content);

        boolean bookmarkState = mPreferences.getBoolean(
                "key_bookmark_chapter " + sectionNumber, false);
        tbAddRemoveBookmark.setChecked(bookmarkState);

        try {
            @SuppressLint("Recycle")
            Cursor cursor = sqLiteDatabase.query("TABLE_QUESTION",
                    new String[]{"QuestionNumber", "QuestionText", "AnswerText"},
                    "_id = ?",
                    new String[]{String.valueOf(sectionNumber)},
                    null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {

                strQuestionNumber = cursor.getString(0);
                strQuestionText = cursor.getString(1);
                strAnswerText = cursor.getString(2);

                tvQuestionText.setMovementMethod(LinkMovementMethod.getInstance());
                tvAnswerText.setMovementMethod(LinkMovementMethod.getInstance());

                tvQuestionNumber.setText(Html.fromHtml(strQuestionNumber));

                tvQuestionText.setText(stringBuilder(strQuestionText), TextView.BufferType.SPANNABLE);
                tvAnswerText.setText(stringBuilder(strAnswerText), TextView.BufferType.SPANNABLE);
            }

        } catch (Exception e) {
            Toast.makeText(getActivity(), "База данных недоступна", Toast.LENGTH_SHORT).show();
        }

        setTextIndentAndSize();
        setTextFont();
        tbAddRemoveBookmark.setOnCheckedChangeListener(this);
        btnCopyContent.setOnClickListener(this);
        pagesProgress.setPageProgress(sectionNumber);

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            pagesProgress = (PagesProgress) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " implement method");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_copy_content:
                copyContent();
                break;
        }
    }

    public interface PagesProgress {
        void setPageProgress(int sectionNumber);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        ContentValues bookmark = new ContentValues();
        bookmark.put("Favorite", isChecked);

        if (isChecked) {
            Toast.makeText(getActivity(), "Добавлено в избранное", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "Удалено из избранного", Toast.LENGTH_SHORT).show();
        }

        mEditor.putBoolean("key_bookmark_chapter " + sectionNumber, isChecked).apply();

        try {
            sqLiteDatabase.update("TABLE_QUESTION",
                    bookmark,
                    "_id = ?",
                    new String[]{String.valueOf(sectionNumber)});
        } catch (Exception e) {

            Toast.makeText(getActivity(), "База данных недоступна", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        setTextIndentAndSize();
        setTextFont();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        sqLiteDatabase.close();
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private void setTextIndentAndSize() {
        int valueIndent = mPreferences.getInt("key_indent_size", 16);
        int valueSize = mPreferences.getInt("key_text_size", 18);

        tvAnswerText.setPadding(valueIndent, 16, valueIndent, 16);
        tvQuestionText.setTextSize(valueSize);
        tvAnswerText.setTextSize(valueSize);
    }

    private void setTextFont() {
        boolean fontOneState = mPreferences.getBoolean("key_font_one", true);
        boolean fontTwoState = mPreferences.getBoolean("key_font_two", false);
        boolean fontThreeState = mPreferences.getBoolean("key_font_three", false);

        if (fontOneState) {

            tvQuestionText.setTypeface(TypeFaces.get(getContext(), "fonts/veranda.ttf"));
            tvAnswerText.setTypeface(TypeFaces.get(getContext(), "fonts/veranda.ttf"));

        } else if (fontTwoState) {

            tvQuestionText.setTypeface(TypeFaces.get(getContext(), "fonts/arial.ttf"));
            tvAnswerText.setTypeface(TypeFaces.get(getContext(), "fonts/arial.ttf"));

        } else if (fontThreeState) {

            tvQuestionText.setTypeface(TypeFaces.get(getContext(), "fonts/times.ttf"));
            tvAnswerText.setTypeface(TypeFaces.get(getContext(), "fonts/times.ttf"));
        }
    }

    private SpannableStringBuilder stringBuilder(String str) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(Html.fromHtml(str));

        str = ssb.toString();

        int indexOne = str.indexOf("[");
        final int[] indexTwo = {0};

        while (indexOne != -1) {
            indexTwo[0] = str.indexOf("]", indexOne) + 1;

            String clickString = str.substring(indexOne, indexTwo[0]);
            clickString = clickString.substring(1, clickString.length() - 1);
            final String finalClickString = clickString;

            ssb.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {

                    try {
                        @SuppressLint("Recycle")
                        Cursor cursor = sqLiteDatabase.query("TABLE_FOOTNOTE",
                                new String[]{"_id", "FootnoteText"},
                                "_id = ?",
                                new String[]{finalClickString},
                                null, null, null);

                        if ((cursor != null) && cursor.moveToFirst()) {

                            strFootnoteId = cursor.getString(0);
                            strFootnoteContent = cursor.getString(1);
                        }

                        if (cursor != null && !cursor.isClosed()) {
                            cursor.close();
                        }

                    } catch (Exception e) {

                        Toast.makeText(getContext(), "База данных недоступна", Toast.LENGTH_SHORT).show();
                    }

                    DialogFootnote(strFootnoteId, strFootnoteContent);

                }
            }, indexOne, indexTwo[0], 0);
            indexOne = str.indexOf("[", indexTwo[0]);
        }
        return ssb;
    }

    @SuppressLint("SetTextI18n")
    private void DialogFootnote(String strFootnoteId, String strFootnoteContent) {
        @SuppressLint("InflateParams")
        View footnoteView = getLayoutInflater().inflate(R.layout.dialog_footnote, null);
        AlertDialog.Builder footnoteDialog = new AlertDialog.Builder(getActivity());
        footnoteDialog.setView(footnoteView);
        TextView footnoteIdNumber = footnoteView.findViewById(R.id.tv_footnote_id_number);
        TextView footnoteContent = footnoteView.findViewById(R.id.tv_footnote_content);
        footnoteIdNumber.setText("Сноска [" + strFootnoteId + "]");
        footnoteContent.setText(strFootnoteContent);
        footnoteDialog.create().show();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void copyContent() {
        Cursor c = sqLiteDatabase.rawQuery("SELECT * FROM TABLE_FOOTNOTE WHERE ContentNumber = " +
                String.valueOf(sectionNumber) + " ORDER BY _id", null);
        final StringBuilder stringBuilder = new StringBuilder();
        if (c != null && c.moveToFirst()) {
            for (int i = 0; i < c.getCount(); i++) {
                stringBuilder.append("[")
                        .append(String.valueOf(c.getInt(c.getColumnIndex("_id"))))
                        .append("] - ")
                        .append(c.getString(c.getColumnIndex("FootnoteText")))
                        .append("<p/>");
                c.moveToNext();
            }
            c.close();
        }

        String stringFootnotes = "";
        if (!stringBuilder.toString().isEmpty()) {
            stringFootnotes = "<p/>" + stringBuilder.toString();
        }

        ClipboardManager clipboardManager = (ClipboardManager)
                Objects.requireNonNull(getContext()).getSystemService(Context.CLIPBOARD_SERVICE);

        ClipData copyData = ClipData.newPlainText("",
                Html.fromHtml(strQuestionNumber + "<p/>" + strQuestionText + "<p/>" +
                        "ОТВЕТ" + "<p/>" + strAnswerText + stringFootnotes + "<p/>" +
                        "_____________________" + "<p/>" +
                        "https://play.google.com/store/apps/details?id=jmapps.questions200"));
        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(copyData);
            Toast.makeText(getActivity(), "Скопировано в буфер", Toast.LENGTH_SHORT).show();
        }
    }
}