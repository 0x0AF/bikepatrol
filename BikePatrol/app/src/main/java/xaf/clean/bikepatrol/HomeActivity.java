package xaf.clean.bikepatrol;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.Iconics;
import com.mikepenz.iconics.IconicsDrawable;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Iconics.init(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageDrawable(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_add).sizeDp(24).color(Color.WHITE));
//        fab.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
//        fab.setRippleColor(getResources().getColor(R.color.colorPrimaryDark));
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, ActionActivity.class);
            startActivity(intent);
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_reports);

//        navigationView.getMenu().getItem(R.id.nav_reports).setIcon(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_report).sizeDp(24).color(Color.BLACK));
//        navigationView.getMenu().getItem(R.id.nav_share).setIcon(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_share).sizeDp(24).color(Color.BLACK));
//        navigationView.getMenu().getItem(R.id.nav_about).setIcon(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_help).sizeDp(24).color(Color.BLACK));

        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_reports) {
            //ignore
        } else if (id == R.id.nav_share) {
            shareApp();
        } else if (id == R.id.nav_about) {
            showAboutSection();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void shareApp() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey! Check out Bike Patrol, it's a really useful app for cyclists and commuters: \n http://twitter.com/get_bike_patrol");
        sendIntent.setType("text/plain");
        this.startActivity(sendIntent);
    }

    public void showAboutSection() {
        new LibsBuilder()
                .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                .withAboutIconShown(true)
                .withAboutVersionShown(true)
                .withAboutDescription("") // TODO: add some description
                .start(this);
    }
}
