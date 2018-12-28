package jmapps.questions200.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import jmapps.questions200.R;

public class ListViewAdapter extends CursorAdapter {

    private final LayoutInflater cursorInflater;

    public ListViewAdapter(Context ctx, Cursor cursor, int flags) {
        super(ctx, cursor, flags);
        cursorInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return cursorInflater.inflate(R.layout.item_list_chapter, viewGroup, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        TextView chapterNumber = view.findViewById(R.id.tv_chapter_number);
        TextView chapterTitle = view.findViewById(R.id.tv_chapter_title);

        String strChapterNumber = getCursor().getString(getCursor().getColumnIndex("QuestionNumber"));
        String strChapterTitle = getCursor().getString(getCursor().getColumnIndex("QuestionText"));

        chapterNumber.setText(strChapterNumber);
        chapterTitle.setText(Html.fromHtml(strChapterTitle));
    }
}