package cn.duanyufei.kiwivm.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cn.duanyufei.kiwivm.R;
import cn.duanyufei.kiwivm.app.AppDefine;
import cn.duanyufei.kiwivm.app.KWApplication;

/**
 * Created by DUAN Yufei on 2017/3/15.
 */
public class UpdateTask extends AsyncTask<String, Void, String> {
    public static final String TAG = UpdateTask.class.getName();

    private Context context;

    public UpdateTask(Context context) {
        this.context = context;
        Toast.makeText(context, context.getString(R.string.update_checking), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected String doInBackground(String... uri) {
        String result = null;
        OkHttpClient ohc = new OkHttpClient();
        final Request request = new Request.Builder().url(uri[0]).build();
        Call call = ohc.newCall(request);
        try {
            Response response = call.execute();
            if (response.isSuccessful()) {
                Log.i(TAG, "Request Success!");
                result = response.body().string();
            } else {
                Log.e(TAG, "Request Failed!");
            }
        } catch (IOException e) {
            Log.e(TAG, "No Response!");
            e.printStackTrace();
        }

        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result == null || result.length() == 0) {
            ToastUtil.show(context, R.string.update_error, ToastUtil.SHORT);
            Log.e(TAG, "No Result from Http Request!");
            return;
        }
        try {
            JSONObject verJSON = new JSONObject(result);
            String latestVersion = verJSON.getString("versionShort");
            int latestVersionCode = verJSON.getInt("build");

            int versionCode = KWApplication.getInstance().getVersionCode();

            Log.i(TAG, "Latest Version: " + latestVersionCode);
            Log.i(TAG, "This Version: " + versionCode);

            if (versionCode >= latestVersionCode) {
                ToastUtil.show(context, context.getString(R.string.update_already_updated), ToastUtil.SHORT);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(context.getString(R.string.update_dialog_title) + latestVersion);
                builder.setMessage(context.getString(R.string.update_dialog_message));
                builder.setPositiveButton(context.getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(AppDefine.DOWNLOAD_URL));
                        browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(browserIntent);
                    }
                });
                builder.setNegativeButton(context.getString(R.string.button_later), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO: Note to remind later
                    }
                });
                builder.create().show();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Checking Update Error!");
            e.printStackTrace();
        }
    }

    public void update() {
        this.execute(AppDefine.UPDATE_URL + AppDefine.FIR_TOKEN_URL);
    }
}
