package com.newsapi.sample;

import android.app.job.JobService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.internal.NavigationMenu;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.transition.TransitionManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.Cache;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    String category[] = {"business", "entertainment", "general", "health", "science", "sports", "technology"};


    private static RecyclerView.Adapter adapter;
    private LinearLayoutManager mLayoutManager;
   // private RecyclerView.LayoutManager layoutManager;
    private static RecyclerView recyclerView;
    private static ArrayList<DataModel> data;
    Urls urls = new Urls();
    //String headlines;
    String headlines = "https://newsapi.org/v2/top-headlines?country=in&category=business&apiKey=be234d8f91b14144884b6e4514609b9c";
    // String entertainment = "https://newsapi.org/v2/top-headlines?country=in&category=entertainment&apiKey=be234d8f91b14144884b6e4514609b9c";
    DataModel dataModel;
    ImageView imageView;

    private EndlessRecyclerOnScrollListener mScrollListener = null;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    NavigationView navigationView;
    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

/*
        if (AppCompatDelegate.getDefaultNightMode()
                ==AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.FeedActivityThemeDark);
        }
*/


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("News");
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        imageView = (ImageView) findViewById(R.id.img1);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);
        mScrollListener = new EndlessRecyclerOnScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                // do something...

                // after loading is done, please call the following method to re-enable onLoadMore
                // usually it should be called in onCompleted() method
                mScrollListener.setLoading(false);

                loadapi();

                Log.e("@@Endless", "loadData");
            }
        };
        recyclerView.addOnScrollListener(mScrollListener);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // do something
                Log.e("@@set", "loadData");


                loadapi();
                // after refresh is done, remember to call the following code
                if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
                }

            }
        });

        CheckIntertConnection();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


    }

    private void CheckIntertConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        // showSnack(isConnected);


        if (isConnected == true) {
           loadapi();

        } else {


            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Check your Internet Connection")
                    .setCancelable(false)
                    .setPositiveButton("Connect to WIFI", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        }
                    })
                    .setNegativeButton("Quit App", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            MainActivity.this.finish();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }


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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    int id;

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        id=item.getItemId();
        // Handle navigation view item clicks here.

      //  NavigationView mNavigationView=null;
        //mNavigationView.getMenu().findItem(R.id.nav_view).setActionView(new Switch(this));

        SwitchCompat drawerSwitch = (SwitchCompat) navigationView.getMenu().findItem(R.id.switch_bt).getActionView();

        drawerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // do stuff

                    Log.e("aa", "che"+isChecked);
                    //setTheme(R.style.FeedActivityThemeDark);

                } else {
                    // do other stuff
                    Log.e("aa", "unche"+isChecked);
                   // setTheme(R.style.FeedActivityThemeLight);


                }
            }
        });


if (id == R.id.headlines) {
    toolbar.setTitle("TOP NEWS");

    headlines = "https://newsapi.org/v2/top-headlines?" + urls.country + "in" + urls.category + category[1] + urls.api_key;

            loadapi();
        } else if (id == R.id.technology) {
    toolbar.setTitle("TECHNOLOGY");

          headlines = "https://newsapi.org/v2/top-headlines?" + urls.country + "in" + urls.category + category[2] + urls.api_key;
            Log.e("@aa", headlines);


            loadapi();

        } else if (id == R.id.business) {
    toolbar.setTitle("BUSINESS");


    //   headlines = "https://newsapi.org/v2/top-headlines?country=in&category=business&apiKey=be234d8f91b14144884b6e4514609b9c";
            headlines = "https://newsapi.org/v2/top-headlines?" + urls.country + "in" + urls.category + category[0] + urls.api_key;
            Log.e("@aa", headlines);

            loadapi();

        } else if (id == R.id.sports) {
    toolbar.setTitle("SPORTS");
//            headlines = "https://newsapi.org/v2/top-headlines?country=in&category=sports&apiKey=be234d8f91b14144884b6e4514609b9c";
            headlines = "https://newsapi.org/v2/top-headlines?" + urls.country + "in" + urls.category + category[5] + urls.api_key;
            Log.e("@aa", headlines);

            loadapi();


        } else if (id == R.id.entertainment) {
//            headlines = "https://newsapi.org/v2/top-headlines?country=in&category=entertainment&apiKey=be234d8f91b14144884b6e4514609b9c";


            headlines = "https://newsapi.org/v2/top-headlines?" + urls.country + "in" + urls.category + category[1] + urls.api_key;
            Log.e("@aa", headlines);


            loadapi();

        } else if (id == R.id.science) {
//            headlines = "https://newsapi.org/v2/top-headlines?country=in&category=science&apiKey=be234d8f91b14144884b6e4514609b9c";


            headlines = "https://newsapi.org/v2/top-headlines?" + urls.country + "in" + urls.category + category[4] + urls.api_key;
            Log.e("@aa", headlines);

            loadapi();


        } else if (id == R.id.health) {
//            headlines = "https://newsapi.org/v2/top-headlines?country=in&category=health&apiKey=be234d8f91b14144884b6e4514609b9c";

    toolbar.setTitle("News");
            headlines = "https://newsapi.org/v2/top-headlines?" + urls.country + "in" + urls.category + category[3] + urls.api_key;
            Log.e("@aa", headlines);
            loadapi();

        }



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void loadapi() {
        VolleyUtils.GET_METHOD(MainActivity.this, headlines, new VolleyResponseListener() {
            @Override
            public void onError(String message) {
                System.out.println("Error" + message);


            }
            @Override
            public void onResponse(String response) {
                getResponse(response);


               // final String jsonString = new String(response.toString(), HttpHeaderParser.parseCharset(response.headers));

                Log.e("@hh", headlines);
            }
        });







    }




    private void getResponse(String response) {

        Cache cache = MyApplication.getInstance().getRequestQueue().getCache();
        Cache.Entry entry = cache.get(response);
        if(entry != null){

            try {
                String data = new String(entry.data ,"UTF-8");
                // Get JSON from the data

                Log.e("@@", data);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            //Make network call
        }

        data = new ArrayList<DataModel>();

        try {
            JSONObject jsonObject = new JSONObject(response);

            JSONArray jsonArray = jsonObject.getJSONArray("articles");

            for (int i = 0; i < jsonArray.length(); i++) {

                dataModel = new DataModel();
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                String title = jsonObject1.getString("title");
                String description = jsonObject1.getString("description");
                String publishedAt = jsonObject1.getString("publishedAt");
                String imgUrl = jsonObject1.getString("urlToImage");
                String newsUrl = jsonObject1.getString("url");
                dataModel.setDate(publishedAt);
                dataModel.setTitle(title);
                dataModel.setDescription(description);
                dataModel.setUrlToImage(imgUrl);
                dataModel.setUrl_description(newsUrl);

                data.add(dataModel);

                recyclerView.setAdapter(adapter);
                adapter = new CustomAdapter(data);


            }

            //String publishedAt = jsonObject.getString("publishedAt");

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

        private ArrayList<DataModel> dataSet;
        // Context mContext;
        String imgUrl;


        private int currentPosition = 0;
        private int mExpandedPosition = -1;

        private RecyclerView recyclerView = null;

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView heading;
            TextView date;
            TextView discription;
            ImageView ivBasicImage;
            ToggleButton tg_button;
            private TextToSpeech tts;
            Context context;
            Button bt_reamore;


            public MyViewHolder(View itemView) {
                super(itemView);
                this.heading = (TextView) itemView.findViewById(R.id.heading);
                this.date = (TextView) itemView.findViewById(R.id.date);
                this.discription = (TextView) itemView.findViewById(R.id.description);
                this.ivBasicImage = (ImageView) itemView.findViewById(R.id.img1);
                this.tg_button = (ToggleButton) itemView.findViewById(R.id.tgplay);
                this.bt_reamore = (Button) itemView.findViewById(R.id.bt_readmore);

                this.context = itemView.getContext();


            }


        }

        public CustomAdapter(ArrayList<DataModel> data) {
            this.dataSet = data;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.custom_views, parent, false);


            MyViewHolder myViewHolder = new MyViewHolder(view);


            return myViewHolder;


        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

            TextView title = holder.heading;
            TextView date = holder.date;
            TextView discription = holder.discription;
            ImageView ivBasicImage = holder.ivBasicImage;
            ToggleButton toggleButton = holder.tg_button;
            Button Button = holder.bt_reamore;

            Uri uri = Uri.parse(dataSet.get(listPosition).getUrlToImage());
            Context context = holder.ivBasicImage.getContext();
//            Context context1 = holder.discription.getContext();

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;

            Picasso.with(context).load(uri)
                    .placeholder((R.drawable.image_not_found))
                    .error((R.drawable.image_not_found))
                    .resize(width,300)
                    .into(holder.ivBasicImage)

            ;


            final boolean isExpanded = listPosition == mExpandedPosition;

            holder.discription.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            holder.bt_reamore.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

            holder.itemView.setActivated(isExpanded);


            holder.itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {


                    mExpandedPosition = isExpanded ? -1 : listPosition;

//                TransitionManager.beginDelayedTransition(recyclerView);
                    notifyDataSetChanged();

                    return false;

                }
            });
/*
            holder.heading.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //currentPosition = listPosition;


                }
            });

*/
/*
            holder.tg_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                }
            });

*/

            holder.bt_reamore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String news_url=dataSet.get(listPosition).getUrl_description();
                    Intent intent = new Intent(getApplicationContext(), News_Activity.class);
                    intent.putExtra("news", news_url);
                    startActivity(intent);


                }            });



            imgUrl = (dataSet.get(listPosition).getUrlToImage());
            title.setText(dataSet.get(listPosition).getTitle());
            date.setText(dataSet.get(listPosition).getDate());
            discription.setText(dataSet.get(listPosition).getDescription());

        }


        @Override
        public int getItemCount() {
            return dataSet.size();
        }


        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {

            this.recyclerView = recyclerView;
            super.onAttachedToRecyclerView(recyclerView);
        }

    }
}




