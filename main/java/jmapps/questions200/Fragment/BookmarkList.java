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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

import jmapps.questions200.Adapter.ListViewAdapter;
import jmapps.questions200.DBSetup.DBAssetHelper;
import jmapps.questions200.R;

public class BookmarkList extends DialogFragment implements
        AdapterView.OnItemClickListener {

    private SQLiteDatabase database;
    private ListViewAdapter listViewAdapter;
    private CurrentPosition getCurrentPosition;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View bookmarkList = inflater.inflate(R.layout.fragment_bookmark_list, container, false);

        setRetainInstance(true);
        Objects.requireNonNull(getDialog().getWindow()).requestFeature(Window.FEATURE_NO_TITLE);

        ListView lvListBookmark = bookmarkList.findViewById(R.id.lv_list_bookmark);
        TextView tvIfBookmarkIsEmpty = bookmarkList.findViewById(R.id.tv_if_bookmark_is_empty);

        DBAssetHelper dbAssetHelper = new DBAssetHelper(getActivity());
        database = dbAssetHelper.getReadableDatabase();

        try {
            @SuppressLint("Recycle")
            Cursor cursor = database.query("TABLE_QUESTION",
                    null,
                    "Favorite = 1",
                    null, null, null, null, null);

            listViewAdapter = new ListViewAdapter(
                    getActivity(),
                    cursor,
                    0);

            lvListBookmark.setAdapter(listViewAdapter);

        } catch (Exception e) {
            Toast.makeText(getActivity(), "База данных недоступна", Toast.LENGTH_SHORT).show();
        }

        if (listViewAdapter.isEmpty()) {
            tvIfBookmarkIsEmpty.setVisibility(View.VISIBLE);
            lvListBookmark.setVisibility(View.GONE);
        } else {
            tvIfBookmarkIsEmpty.setVisibility(View.GONE);
            lvListBookmark.setVisibility(View.VISIBLE);
        }

        lvListBookmark.setOnItemClickListener(this);

        return bookmarkList;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            getCurrentPosition = (CurrentPosition) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " implements method");
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