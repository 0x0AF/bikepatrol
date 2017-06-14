package xaf.clean.bikepatrol;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;

import io.fabric.sdk.android.Fabric;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Answers(), new Crashlytics());
        setContentView(R.layout.activity_home);

        final TwitterApi api = new TwitterApi();
        api.instantiate();

        // TODO: interface
        //    new AsyncTask<Void, Void, Void>() {
        //
        //        @Override
        //        protected Void doInBackground(Void... params) {
        //            try {
        //                api.postText("test");
        //            } catch (TwitterException e) {
        //                Log.e(HomeActivity.class.getName(), "Update failed", e);
        //            }
        //            return null;
        //        }
        //    }.execute();
    }
}
