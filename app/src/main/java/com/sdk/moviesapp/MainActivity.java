package com.sdk.moviesapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import br.com.mauker.materialsearchview.MaterialSearchView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.sdk.moviesapp.adapter.MoviesAdapter;
import com.sdk.moviesapp.api.Client;
import com.sdk.moviesapp.api.Service;
import com.sdk.moviesapp.data.FavoriteDbHelper;
import com.sdk.moviesapp.model.Movie;
import com.sdk.moviesapp.model.MoviesResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private RecyclerView recyclerView;
    private MoviesAdapter adapter;
    private List<Movie> movieList;
    br.com.mauker.materialsearchview.MaterialSearchView searchView;
    ProgressDialog pd;
    private SwipeRefreshLayout swipeContainer;
    public static final String LOG_TAG = MoviesAdapter.class.getName();
    private SharedPreferences sph;
    public static String sphFile = "com.sdk.moviesapp.sd";
    int cacheSize = 10*1024*1024;
    Toolbar toolbar;

//    private static  final int PAGE_START = 1;
//    private boolean isLoading = false;
//    private boolean isLastPage = false;
//    private int TOTAL_PAGES = 5;
//    private int currentPage = PAGE_START;

    private static String LIST_STATE = "list_state";
    private Parcelable savedRecyclerLayoutState;
    private static final String BUNDLE_RECYCLER_LAYOUT = "recycler_layout";
    private ArrayList<Movie> moviesInstance = new ArrayList<>();

    private Service movieService;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("MoviesApp");
        sph = getSharedPreferences(sphFile, MODE_PRIVATE);

        if (savedInstanceState != null) {
            moviesInstance = savedInstanceState.getParcelableArrayList(LIST_STATE);
            savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
            displayData();
        } else {
            initViews();
        }

        searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(MainActivity.this, "Submit clicked", Toast.LENGTH_SHORT).show();
                if (!query.isEmpty())
                    loadSearch(query);
                else
                    Toast.makeText(MainActivity.this, "Empty query..", Toast.LENGTH_SHORT).show();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Toast.makeText(MainActivity.this, "typing...", Toast.LENGTH_SHORT).show();
                if (!newText.isEmpty()) {
                    loadSearch(newText);
                }
                else {
                    //loadSearch("sd");
                }
                return true;
            }
        });

        swipeContainer = findViewById(R.id.main_content);
        swipeContainer.setColorSchemeColors(android.R.color.holo_orange_dark);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initViews();
                Toast.makeText(MainActivity.this, "List refreshed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayData () {
        recyclerView = findViewById(R.id.recyclerview);
        adapter = new MoviesAdapter(this, moviesInstance);

        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        }

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        restoreLayoutManagerPosition();
        adapter.notifyDataSetChanged();
    }

    private void restoreLayoutManagerPosition() {
        if (savedRecyclerLayoutState != null) {
            recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
        }
    }

    private void initViews() {
        pd = new ProgressDialog(this);
        pd.setMessage("Fetching Movies... ");
        pd.setCancelable(false);
        pd.show();

        recyclerView = findViewById(R.id.recyclerview);
        movieList = new ArrayList<>();
        adapter = new MoviesAdapter(this, movieList);
        
        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        }
        
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        
        checkSortOrder();
    }

    private boolean checkNetworkAvailability() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo()!=null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    @Override
    public void onBackPressed() {
        if (searchView.isOpen())
            searchView.closeSearch();
        else
            super.onBackPressed();
    }

    private void loadJSON() {
        try {
            if (BuildConfig.THE_MOVIE_DB_API_TOKEN.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please obtain API Key ", Toast.LENGTH_SHORT).show();
                pd.dismiss();
                return;
            }

            Client Client = new Client();
            Service apiService = Client.getClient().create(Service.class);
            Call<MoviesResponse> call = apiService.getPopularMovies(BuildConfig.THE_MOVIE_DB_API_TOKEN, 5);
            call.enqueue(new Callback<MoviesResponse>() {
                @Override
                public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                    List<Movie> movies = response.body().getResults();
                    moviesInstance.addAll(movies);
                    recyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), movies));
                    recyclerView.smoothScrollToPosition(0);
                    if (swipeContainer.isRefreshing()) {
                        swipeContainer.setRefreshing(false);
                    }
                    pd.dismiss();                }

                @Override
                public void onFailure(Call<MoviesResponse> call, Throwable t) {
                    Log.d("Error", t.getMessage());
                    Toast.makeText(MainActivity.this, "Error fetching Data" + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        catch (Exception e) {
            Log.d("Error", e.getMessage());
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadJSON1() {
        try {
            if (BuildConfig.THE_MOVIE_DB_API_TOKEN.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please obtain API Key ", Toast.LENGTH_SHORT).show();
                pd.dismiss();
                return;
            }

            Client Client = new Client();
            Service apiService = Client.getClient().create(Service.class);
            Call<MoviesResponse> call = apiService.getTopRatedMovies(BuildConfig.THE_MOVIE_DB_API_TOKEN, 5);
            call.enqueue(new Callback<MoviesResponse>() {
                @Override
                public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                    List<Movie> movies = response.body().getResults();
                    moviesInstance.addAll(movies);
                    recyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), movies));
                    recyclerView.smoothScrollToPosition(0);
                    if (swipeContainer.isRefreshing()) {
                        swipeContainer.setRefreshing(false);
                    }
                    pd.dismiss();                }

                @Override
                public void onFailure(Call<MoviesResponse> call, Throwable t) {
                    Log.d("Error", t.getMessage());
                    Toast.makeText(MainActivity.this, "Error fetching Data" + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        catch (Exception e) {
            Log.d("Error", e.getMessage());
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }


    public Activity getActivity() {
        Context context = this;
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onPrepareOptionsMenu(menu);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//
//        return super.onCreateOptionsMenu(menu);
//    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        SharedPreferences.Editor sphEditor = sph.edit();
        switch (item.getItemId()) {
            case R.id.popularity:
                sphEditor.putString("sortOrder", "Most Popular");
                //Toast.makeText(this, "not clicked", Toast.LENGTH_SHORT).show();
                sphEditor.apply();
                checkSortOrder();
                return true;
            case R.id.rating:
                sphEditor.putString("sortOrder", "Top Rated");
                sphEditor.apply();
                checkSortOrder();
                return true;
            case R.id.favorite:
                sphEditor.putString("sortOrder", "Favorite");
                sphEditor.apply();
                getFavorites();
                return true;
            case R.id.notes:
                Intent intent = new Intent(getApplicationContext(), NotesActivity.class);
                startActivity(intent);
                return true;
            case R.id.search:
                searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        Toast.makeText(MainActivity.this, "Submit clicked", Toast.LENGTH_SHORT).show();
                        if (!query.isEmpty())
                            loadSearch(query);
                        else
                            Toast.makeText(MainActivity.this, "Empty query..", Toast.LENGTH_SHORT).show();
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        //Toast.makeText(MainActivity.this, "typing...", Toast.LENGTH_SHORT).show();
                        if (!newText.isEmpty()) {
                            loadSearch(newText);
                        }
                        else {
                            //loadSearch("sd");
                        }
                        return true;
                    }
                });
                Toast.makeText(getApplicationContext(), "Search Clicked", Toast.LENGTH_SHORT).show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(LIST_STATE, moviesInstance);
        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, recyclerView.getLayoutManager().onSaveInstanceState());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        moviesInstance =  savedInstanceState.getParcelableArrayList(LIST_STATE);
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void loadSearch(String query) {
        try {
            if (BuildConfig.THE_MOVIE_DB_API_TOKEN.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please obtain API Key ", Toast.LENGTH_SHORT).show();
                pd.dismiss();
                return;
            }
            Toast.makeText(this, "Inside loadSearch", Toast.LENGTH_SHORT).show();

            Client Client = new Client();
            Service apiService = Client.getClient().create(Service.class);
            Call<MoviesResponse> call = apiService.getMovies(BuildConfig.THE_MOVIE_DB_API_TOKEN, query);
            call.enqueue(new Callback<MoviesResponse>() {
                @Override
                public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                    List<Movie> movies = response.body().getResults();
                    recyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), movies));
                    recyclerView.smoothScrollToPosition(0);
                    if (swipeContainer.isRefreshing()) {
                        swipeContainer.setRefreshing(false);
                    }
                    pd.dismiss();                }

                @Override
                public void onFailure(Call<MoviesResponse> call, Throwable t) {
                    Log.d("Error", t.getMessage());
                    Toast.makeText(MainActivity.this, "Error fetching Data" + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        catch (Exception e) {
            Log.d("Error", e.getMessage());
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void getFavorites() {
        List<Movie> favoriteList = new FavoriteDbHelper(getApplicationContext()).fetch();
        if (!favoriteList.isEmpty()) {
            recyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), favoriteList));
            recyclerView.smoothScrollToPosition(0);
        } else {
            Toast.makeText(getApplicationContext(), "No movies in Favorite List", Toast.LENGTH_SHORT).show();
            loadJSON();
        }
        pd.dismiss();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Log.d(LOG_TAG, "Preferences Updated");
        checkSortOrder();
    }

    private void checkSortOrder() {
        Toast.makeText(this, "Inside checkSortOrder", Toast.LENGTH_SHORT).show();
        String sortOrder = sph.getString("sortOrder", "Most Popular");
        if (sortOrder.equals(this.getString(R.string.pref_most_popular))) {
            Log.d(LOG_TAG, "Sorting by popularity");
            loadJSON();
            //loadSearch("once");
        }else if(sortOrder.equals("Favorite")) {
            Log.d(LOG_TAG, "Getting Favorites");
            getFavorites();
        } else{
            Log.d(LOG_TAG, "Sorting by rating");
            loadJSON1();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        displayData();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //getIntent().putExtra("state", onSaveInstanceState());
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
