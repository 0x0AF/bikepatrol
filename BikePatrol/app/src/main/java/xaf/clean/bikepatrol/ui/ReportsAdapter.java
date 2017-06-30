package xaf.clean.bikepatrol.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import xaf.clean.bikepatrol.BuildConfig;
import xaf.clean.bikepatrol.R;
import xaf.clean.bikepatrol.model.Report;

class ReportsAdapter extends RealmRecyclerViewAdapter<Report, ReportsAdapter.ReportViewHolder> {

    private final Context context;

    ReportsAdapter(Context context, OrderedRealmCollection<Report> data) {
        super(data, true);
        this.context = context;
        setHasStableIds(false);
    }

    @Override
    public ReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_report, parent, false);
        return new ReportViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ReportViewHolder holder, int position) {
        if (getData() == null)
            return;

        Report report = getData().get(position);
        if (report == null)
            return;

        final String mediaPath = report.getMediaAbsolutePath();

        if (report.getType().equals(Report.TYPE_PICTURE)) {
            holder.reportThumbnail.setOnClickListener(v -> {
                Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", new File(mediaPath));
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(uri, "image/*");
                context.startActivity(intent);
            });
            new AsyncTask<Void, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Void... params) {
                    return ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(mediaPath), 720, 720, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    holder.reportThumbnail.setImageBitmap(bitmap);
                }
            }.execute();
        } else {
            holder.reportThumbnail.setOnClickListener(v -> {
                Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", new File(mediaPath));
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "video/*");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
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
                    holder.reportThumbnail.setImageBitmap(bitmap);
                }
            }.execute();
        }

        final String timestamp = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT).format(new Date(report.getTimestamp()));

        holder.reportTimestamp.setText(timestamp);
        holder.buttonSeeLocation.setOnClickListener(v -> {
            String geo = String.format(Locale.ENGLISH, "geo:%f,%f", report.getLatitude(), report.getLongitude());
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geo));
            context.startActivity(intent);
        });
        holder.buttonSync.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReportActivity.class);
            intent.putExtra("report_timestamp", report.getTimestamp());
            context.startActivity(intent);
        });
    }

    class ReportViewHolder extends RecyclerView.ViewHolder {
        ImageView reportThumbnail;
        TextView reportTimestamp;
        Button buttonSync;
        Button buttonSeeLocation;

        ReportViewHolder(View itemView) {
            super(itemView);
            reportThumbnail = (ImageView) itemView.findViewById(R.id.report_thumbnail);
            reportTimestamp = (TextView) itemView.findViewById(R.id.report_timestamp);
            buttonSync = (Button) itemView.findViewById(R.id.button_sync);
            buttonSeeLocation = (Button) itemView.findViewById(R.id.button_see_location);
        }
    }

}
