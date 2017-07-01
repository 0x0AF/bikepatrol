package xaf.clean.bikepatrol.ui;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.Iconics;
import com.mikepenz.iconics.IconicsDrawable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;
import twitter4j.TwitterException;
import xaf.clean.bikepatrol.BuildConfig;
import xaf.clean.bikepatrol.R;
import xaf.clean.bikepatrol.model.Report;
import xaf.clean.bikepatrol.util.TwitterApi;

public class ReportActivity extends AppCompatActivity {

    private static final String PATTERN_GERMAN_LP = "[0-9]{1,4}";
    private Report report;
    private Realm realm;
    private TextView reportTimestamp;
    private ImageView reportThumbnail;
    private Switch reportKeepAudio;
    private Switch reportKeepLocation;
    private Switch reportMarkSensitive;
    private EditText reportDescription;
    private TextRecognizer textRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Answers.getInstance().logCustom(new CustomEvent("Report activity opened"));

        Iconics.init(this);
        textRecognizer = new TextRecognizer.Builder(this).build();

        getSupportActionBar().setTitle("Sync report");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        long timestamp = intent.getLongExtra("report_timestamp", -1);

        if (timestamp == -1) {
            finish();
        }

        realm = Realm.getDefaultInstance();

        report = realm.where(Report.class).equalTo("timestamp", timestamp).findFirst();

        if (report == null) {
            finish();
        }

        bindViews();
    }

    private void bindViews() {
        reportThumbnail = (ImageView) findViewById(R.id.report_thumbnail);
        reportTimestamp = (TextView) findViewById(R.id.report_timestamp);
        reportDescription = (EditText) findViewById(R.id.report_description);
        reportKeepAudio = (Switch) findViewById(R.id.report_keep_audio);
        reportKeepLocation = (Switch) findViewById(R.id.report_keep_location);
        reportMarkSensitive = (Switch) findViewById(R.id.report_mark_sensitive);

        final String mediaPath = report.getMediaAbsolutePath();

        if (report.getType().equals(Report.TYPE_PICTURE)) {
            reportThumbnail.setOnClickListener(v -> {
                Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", new File(mediaPath));
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(uri, "image/*");
                startActivity(intent);
            });
            new AsyncTask<Void, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Void... params) {
                    return ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(mediaPath), 720, 720, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    reportThumbnail.setImageBitmap(bitmap);
                }
            }.execute();

            AsyncTask.execute(() -> {
                if (!textRecognizer.isOperational()) {
                    Log.w(ReportActivity.class.getName(), "Detector dependencies are not yet available.");

                    IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
                    boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

                    if (hasLowStorage) {
                        Toast.makeText(this, "Low Storage", Toast.LENGTH_LONG).show();
                        Log.w(ReportActivity.class.getName(), "Low Storage");
                    }
                }

                Frame imageFrame = new Frame.Builder()
                        .setBitmap(BitmapFactory.decodeFile(mediaPath))
                        .build();

                SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);

                for (int i = 0; i < textBlocks.size(); i++) {
                    TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));

                    Log.i(ReportActivity.class.getName(), textBlock.getValue());

                    PatternMatcher patternMatcher = new PatternMatcher(PATTERN_GERMAN_LP, 3);
                    if (patternMatcher.match(textBlock.getValue())) {
                        Log.i(ReportActivity.class.getName(), "MATCH");
                        runOnUiThread(() -> reportDescription.getText().append(textBlock.getValue()));
                    }
                }
            });
        } else {
            reportThumbnail.setOnClickListener(v -> {
                Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", new File(mediaPath));
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "video/*");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            });
            new AsyncTask<Void, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Void... params) {
                    return ThumbnailUtils.extractThumbnail(
                            ThumbnailUtils.createVideoThumbnail(mediaPath, MediaStore.Images.Thumbnails.MINI_KIND),
                            720, 720, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    reportThumbnail.setImageBitmap(bitmap);
                }
            }.execute();
        }

        final String timestamp = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT).format(new Date(report.getTimestamp()));

        reportTimestamp.setText(timestamp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.complete_edit, menu);
        menu.findItem(R.id.action_complete_edit).setIcon(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_check).sizeDp(24).color(Color.WHITE));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_complete_edit:
                syncReport(report);
                return true;
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    private void syncReport(Report report) {
        Answers.getInstance().logCustom(new CustomEvent("Report sync requested"));

        boolean keepLocation = reportKeepLocation.isChecked();
        boolean isSensitive = reportMarkSensitive.isChecked();

        final String timestamp = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT).format(new Date(report.getTimestamp()));
        String message;
        if (keepLocation) {
            message = String.format(Locale.ENGLISH, "%s | %s | https://google.com/maps/?q=%f,%f", reportDescription.getText().toString(), timestamp, report.getLatitude(), report.getLongitude());
        } else {
            message = String.format(Locale.ENGLISH, "%s | %s", reportDescription.getText().toString(), timestamp);
        }
        File out = new File(report.getMediaAbsolutePath());
        double lat = report.getLatitude();
        double lon = report.getLongitude();

        if (report.getType().equals(Report.TYPE_PICTURE)) {
            new AsyncTask<Object, Object, Boolean>() {
                @Override
                protected Boolean doInBackground(Object... params) {
                    try {
                        if (keepLocation) {
                            new TwitterApi().instantiate().postImage(message, lat, lon, out, isSensitive);
                        } else {
                            new TwitterApi().instantiate().postImageNoLocation(message, out, isSensitive);
                        }
                        return true;
                    } catch (IOException | TwitterException e) {
                        Log.e(ActionActivity.class.getName(), "Update failed", e);
                        return false;
                    }
                }

                @Override
                protected void onPostExecute(Boolean isSuccessful) {
                    if (isSuccessful) {
                        Realm realm = Realm.getDefaultInstance();
                        realm.executeTransaction(realm1 -> report.deleteFromRealm());
                        realm.close();
                        Toast.makeText(ReportActivity.this, R.string.rep_sync, Toast.LENGTH_LONG).show();
                        finish();
//                        Snackbar.make(findViewById(R.id.toolbar), R.string.rep_sync, Snackbar.LENGTH_LONG)
//                                .setAction(R.string.vis_com, v1 -> {
//                                    Intent intent = new Intent(Intent.ACTION_VIEW);
//                                    intent.setData(Uri.parse("https://twitter.com/get_bike_patrol"));
//                                    ReportActivity.this.startActivity(intent);
//                                }).show();
                    }
                }
            }.execute();
        } else {
            new AsyncTask<Object, Object, Boolean>() {

                @Override
                protected Boolean doInBackground(Object... params) {
                    try {
                        if (keepLocation) {
                            new TwitterApi().instantiate().postVideo(message, lat, lon, out, isSensitive);
                        } else {
                            new TwitterApi().instantiate().postVideoNoLocation(message, out, isSensitive);
                        }
                        return true;
                    } catch (IOException | TwitterException e) {
                        Log.e(ActionActivity.class.getName(), "Update failed", e);
                        return false;
                    }
                }

                @Override
                protected void onPostExecute(Boolean isSuccessful) {
                    if (isSuccessful) {
                        Realm realm = Realm.getDefaultInstance();
                        realm.executeTransaction(realm1 -> report.deleteFromRealm());
                        realm.close();
                        Toast.makeText(ReportActivity.this, R.string.rep_sync, Toast.LENGTH_LONG).show();
                        finish();

//                        .setAction(R.string.vis_com, v1 -> {
//                            Intent intent = new Intent(Intent.ACTION_VIEW);
//                            intent.setData(Uri.parse("https://twitter.com/get_bike_patrol"));
//                            ReportActivity.this.startActivity(intent);
//                        })
                    }
                }
            }.execute();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

}
