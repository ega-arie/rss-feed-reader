package com.ega.rssfeedreader.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.ega.rssfeedreader.R;
import com.ega.rssfeedreader.adapter.ContentAdapter;
import com.ega.rssfeedreader.helper.RssParser;
import com.ega.rssfeedreader.model.RssItem;
import com.ega.rssfeedreader.model.RssSource;
import com.ega.rssfeedreader.service.HttpService;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MenuItem activeMenu;

    private ArrayList<RssSource> listRssSource;
    private HashMap<Integer, RssSource> rssSources;
//    private ArrayList<RssItem> listRssItem;

    private ContentAdapter contentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                activeMenu = menuItem;

                updateContent();

                drawerLayout.closeDrawers();

                return true;
            }
        });

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View view, float v) {

            }

            @Override
            public void onDrawerOpened(@NonNull View view) {

            }

            @Override
            public void onDrawerClosed(@NonNull View view) {

            }

            @Override
            public void onDrawerStateChanged(int i) {

            }
        });

        progressBar = findViewById(R.id.progressBar);

        recyclerView = findViewById(R.id.content_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);

        swipeRefreshLayout = (SwipeRefreshLayout) this.findViewById(R.id.container);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.canChildScrollUp();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateContent();

                swipeRefreshLayout.setRefreshing(true);
            }
        });

        setupMenu();

        if(! isNetworkAvailable()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.no_internet_connection)
                    .setTitle(R.string.alert_title)
                    .setCancelable(false)
                    .setPositiveButton(R.string.label_close,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    finish();
                                }
                            });

            AlertDialog alert = builder.create();
            alert.show();
        }
        else{
            updateContent();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return (networkInfo != null && networkInfo.isConnected());
    }

    private void setupMenu(){
        setRssSources();

        Menu mainMenu = navigationView.getMenu();

        RssSource rssSource = null;

        for (Map.Entry item:rssSources.entrySet()){
            rssSource = (RssSource) item.getValue();

            mainMenu.add(0, (int)item.getKey(), Menu.NONE, rssSource.getName());
        }

        //set first data as active menu
        Map.Entry<Integer, RssSource> firstResource = rssSources.entrySet().iterator().next();

        activeMenu = navigationView.getMenu().getItem(0);

//        updateContent();
    }

    private void updateContent() {
        if(activeMenu != null){
            activeMenu.setChecked(true);
            activeMenu.setCheckable(true);
        }

        RssSource rssSource = rssSources.get(activeMenu.getItemId());

        if(rssSource == null) return;

        setTitle(rssSource.getName());
        progressBar.setVisibility(View.VISIBLE);

        if(contentAdapter != null){
            contentAdapter.clearData();
            contentAdapter.notifyDataSetChanged();
        }

        HttpService service = new HttpService();
        service.execute(rssSource.getUrl());

        service.onFinish(new HttpService.OnTaskCompleted() {
            @Override
            public void onSuccess(String response) {
                RssParser parser = new RssParser(response);

                try {
                    ArrayList<RssItem> listRssItem = parser.getList();
                    contentAdapter = new ContentAdapter(MainActivity.this, R.layout.content_row, listRssItem);

                    recyclerView.setAdapter(contentAdapter);

                    progressBar.setVisibility(View.GONE);

                    swipeRefreshLayout.setRefreshing(false);
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setRssSources(){
        rssSources = new HashMap<Integer, RssSource>();

        rssSources.put(10, new RssSource("detik.com", "http://rss.detik.com/index.php/detikcom"));
        rssSources.put(20, new RssSource("okezone.com", "https://sindikasi.okezone.com/index.php/rss/0/RSS2.0"));
    }


}
