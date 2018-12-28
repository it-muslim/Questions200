package jmapps.questions200.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Objects;

import jmapps.questions200.Adapter.ListViewAdapter;
import jmapps.questions200.DBSetup.DBAssetHelper;
import jmapps.questions200.R;

public class ChapterList extends DialogFragment implements
        AdapterView.OnItemClickListener, TextWatcher {

    private SQLiteDatabase database;
    private CurrentPosition getCurrentPosition;
    private ListViewAdapter listViewAdapter;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View chapterList = inflater.inflate(R.layout.fragment_chapter_list, container, false);

        setRetainInstance(true);
        Objects.requireNonNull(getDialog().getWindow()).requestFeature(Window.FEATURE_NO_TITLE);

        ListView lvListChapter = chapterList.findViewById(R.id.lv_list_chapter);
        EditText etSearchByChapter = chapterList.findViewById(R.id.et_search_by_chapter);

        DBAssetHelper dbAssetHelper = new DBAssetHelper(getActivity());
        database = dbAssetHelper.getReadableDatabase();

        try {
            @SuppressLint("Recycle")
            Cursor cursor = database.rawQuery("SELECT * FROM " + "TABLE_QUESTION", null);

            listViewAdapter = new ListViewAdapter(
                    getActivity(),
                    cursor,
                    0);

            if (!etSearchByChapter.getText().toString().isEmpty()) {
                listViewAdapter.getFilter().filter(etSearchByChapter.toString());
            }

            etSearchByChapter.addTextChangedListener(this);
            lvListChapter.setAdapter(listViewAdapter);

        } catch (Exception e) {
            Toast.makeText(getActivity(), "База данных недоступна", Toast.LENGTH_SHORT).show();
        }

        listViewAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {

                if (constraint == null || constraint.length() == 0) {

                    return database.rawQuery("SELECT * FROM " + "TABLE_QUESTION", null);

                } else {

                    String lastState = String.valueOf(constraint.toString());

                    return database.rawQuery("SELECT * FROM " + "TABLE_QUESTION " + "WHERE " +
                                    "QuestionNumber " + "LIKE ?" + " OR " + "QuestionText " + "LIKE ?",
                            new String[]{"%" + lastState + "%", "%" + lastState + "%"});

                }
            }
        });

        lvListChapter.setOnItemClickListener(this);

        return chapterList;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        listViewAdapter.getFilter().filter(s.toString());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            getCurrentPosition = (CurrentPosition) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " implements this interface");
        }
    }

    public interface CurrentPosition {
        void setCurrentPosition(long id);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        database.close();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        getCurrentPosition.setCurrentPosition(id - 1);
        getDialog().cancel();
    }
}