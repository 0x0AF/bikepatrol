package xaf.clean.bikepatrol.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;
import com.google.android.gms.vision.text.TextRecognizer;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.aboutlibraries.util.Colors;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.Iconics;
import com.mikepenz.iconics.IconicsDrawable;

import java.io.File;

import io.realm.Realm;
import xaf.clean.bikepatrol.R;
import xaf.clean.bikepatrol.model.Report;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.VIBRATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String[] PERMISSIONS = new String[]{
            ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION,
            READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE,
            ACCESS_NETWORK_STATE, INTERNET,
            VIBRATE, CAMERA, RECORD_AUDIO
    };
    public static final int REQUEST_CHECK_SETTINGS = 0xAF;

    private DrawerLayout mDrawerLayout;
    private Realm realm;
    private ReportsAdapter reportsAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Iconics.init(this);
        new TextRecognizer.Builder(this).build();

        Answers.getInstance().logCustom(new CustomEvent("App opened"));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageDrawable(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_add).sizeDp(24).color(Color.WHITE));
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, ActionActivity.class);
            startActivity(intent);
        });

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_reports);

        navigationView.getHeaderView(0).findViewById(R.id.view_reports).setOnClickListener(v -> {
            Answers.getInstance().logCustom(new CustomEvent("Community viewed"));

            mDrawerLayout.closeDrawer(GravityCompat.START, false);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://twitter.com/get_bike_patrol"));
            startActivity(intent);
        });

        IconicsDrawable reportsDrawable = new IconicsDrawable(this, GoogleMaterial.Icon.gmd_report)
                .sizeDp(24)
                .colorRes(R.color.colorIcon);

        IconicsDrawable shareDrawable = new IconicsDrawable(this, GoogleMaterial.Icon.gmd_share)
                .sizeDp(24)
                .colorRes(R.color.colorIcon);

        IconicsDrawable aboutDrawable = new IconicsDrawable(this, GoogleMaterial.Icon.gmd_help)
                .sizeDp(24)
                .colorRes(R.color.colorIcon);

        navigationView.getMenu().getItem(0).setIcon(reportsDrawable);
        navigationView.getMenu().getItem(1).setIcon(shareDrawable);
        navigationView.getMenu().getItem(2).setIcon(aboutDrawable);

        navigationView.setNavigationItemSelectedListener(this);

        realm = Realm.getDefaultInstance();

        initRecyclerView();

        tryRequestPermissions();
    }

    private void initRecyclerView() {
        reportsAdapter = new ReportsAdapter(this, realm.where(Report.class).findAll());
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(reportsAdapter);
        recyclerView.setHasFixedSize(true);

        TouchHelperCallback touchHelperCallback = new TouchHelperCallback();
        ItemTouchHelper touchHelper = new ItemTouchHelper(touchHelperCallback);
        touchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recyclerView.setAdapter(null);
        realm.close();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START, true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                tryRequestPermissions();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Snackbar.make(findViewById(R.id.camera), "Permissions are required", Snackbar.LENGTH_INDEFINITE)
                        .setAction(
                                "Grant",
                                (view) -> ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CHECK_SETTINGS))
                        .show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void tryRequestPermissions() {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CHECK_SETTINGS);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_reports) {
            //ignore
            mDrawerLayout.closeDrawer(GravityCompat.START, true);
        } else if (id == R.id.nav_share) {
            mDrawerLayout.closeDrawer(GravityCompat.START, false);
            shareApp();
        } else if (id == R.id.nav_about) {
            mDrawerLayout.closeDrawer(GravityCompat.START, false);
            showAboutSection();
        }

        return true;
    }

    public void shareApp() {
        Answers.getInstance().logCustom(new CustomEvent("App shared"));

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey! Check out Bike Patrol, it's a really useful app for cyclists and commuters: \n http://twitter.com/get_bike_patrol");
        sendIntent.setType("text/plain");
        this.startActivity(sendIntent);
    }

    public void showAboutSection() {
        Answers.getInstance().logCustom(new CustomEvent("About opened"));

        new LibsBuilder()
                .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                .withActivityColor(new Colors(getResources().getColor(R.color.colorPrimaryDark), getResources().getColor(R.color.colorPrimaryDark)))
                .withAboutIconShown(true)
                .withAboutVersionShown(true)
                .withAboutAppName(getResources().getString(R.string.app_name))
                .withAboutDescription("Safety for cyclists by cyclists <br/> <a href=\"https://t.co/o1P0RQybRO\">Participate in out study</a>  | <a href=\"https://twitter.com/get_bike_patrol\">Follow our community on Twitter</a>")
                .start(this);
    }

    private class TouchHelperCallback extends ItemTouchHelper.SimpleCallback {

        TouchHelperCallback() {
            super(0, ItemTouchHelper.RIGHT);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
            realm.executeTransaction(realm1 -> {
                new File(reportsAdapter.getItem(viewHolder.getAdapterPosition()).getMediaAbsolutePath()).delete();
                reportsAdapter.getItem(viewHolder.getAdapterPosition()).deleteFromRealm();
            });
            Snackbar.make(viewHolder.itemView, R.string.rep_del, Snackbar.LENGTH_LONG).show();
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }
    }
}
