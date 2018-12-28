package jmapps.questions200.DBSetup;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class DBAssetHelper extends SQLiteAssetHelper {

    private static final String DBName = "QuestionsDB";
    private static final int DBVersion = 4;

    public DBAssetHelper(Context context) {
        super(context, DBName, null, DBVersion);

        setForcedUpgrade(DBVersion);
    }
}