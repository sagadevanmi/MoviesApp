package com.sdk.moviesapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.sdk.moviesapp.adapter.TrailerAdapter;
import com.sdk.moviesapp.api.Client;
import com.sdk.moviesapp.api.Service;
import com.sdk.moviesapp.data.FavoriteDbHelper;
import com.sdk.moviesapp.data.NotesDbHelper;
import com.sdk.moviesapp.model.Movie;
import com.sdk.moviesapp.model.Notes;
import com.sdk.moviesapp.model.Trailer;
import com.sdk.moviesapp.model.TrailerResponse;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//import static com.sdk.moviesapp.MainActivity.sphFile;

public class DetailActivity extends AppCompatActivity {

    TextView movieName, synopsis, userRating, releaseDate;
    ImageView imageView;
    private RecyclerView recyclerView;
    private TrailerAdapter adapter;
    private List<Trailer> trailerList;
    String sphFile = "com.sdk.moviesapp.sd2", name;
    private FavoriteDbHelper favoriteDbHelper;
    private NotesDbHelper notesDbHelper;
    private Movie favorite;
    private Notes note;
    private final AppCompatActivity activity = DetailActivity.this;
    private FloatingActionButton fab;
    EditText etNotes;
    Button btnAddNote;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initCollapsingToolbar();

        imageView = (ImageView) findViewById(R.id.thumbnail_image_header);
        movieName = (TextView) findViewById(R.id.title);
        synopsis = (TextView) findViewById(R.id.plotsynopsis);
        userRating = (TextView) findViewById(R.id.userrating);
        releaseDate = (TextView) findViewById(R.id.releasedate);
        fab = findViewById(R.id.fab);

        Intent intent = getIntent();
        if (intent.hasExtra("original_title")) {
            String thumbnail = getIntent().getStringExtra("poster_path");
            name = getIntent().getStringExtra("original_title");
            String plot = getIntent().getStringExtra("overview");
            String rating = getIntent().getStringExtra("vote_average");
            String date = getIntent().getStringExtra("release_date");

            Glide.with(this)
                    .load(thumbnail)
                    .into(imageView);

            movieName.setText(name);
            synopsis.setText(plot);
            userRating.setText(rating);
            releaseDate.setText(date);
        } else {
            Toast.makeText(this, "API has no Data", Toast.LENGTH_SHORT).show();
        }

        MaterialFavoriteButton mfb = findViewById(R.id.favorite);

        final SharedPreferences sph = getSharedPreferences(sphFile, MODE_PRIVATE);
        mfb.setOnFavoriteChangeListener(
                new MaterialFavoriteButton.OnFavoriteChangeListener() {
                    @Override
                    public void onFavoriteChanged(MaterialFavoriteButton buttonView, boolean favorite) {
                        if (favorite) {
                            SharedPreferences.Editor sphEditor = sph.edit();
                            sphEditor.putBoolean("favoriteAdded", true);
                            sphEditor.apply();
                            boolean flag = saveFavorite();
                            if (flag)
                                Snackbar.make(buttonView, "Added to Favorites", Snackbar.LENGTH_SHORT).show();
                            else
                                removeFavorite();
                        } else {
                            SharedPreferences.Editor sphEditor = sph.edit();
                            sphEditor.putBoolean("favoriteRemoved", true);
                            sphEditor.apply();
                            removeFavorite();
                            Snackbar.make(buttonView, "Removed from Favorites", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        initViews();
        
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(DetailActivity.this, "Clicked fab", Toast.LENGTH_SHORT).show();
               String thumbnail = getIntent().getStringExtra("poster_path");
               Intent intent = new Intent(DetailActivity.this, MyNoteActivity.class);
               intent.putExtra("title", movieName.getText().toString());
               intent.putExtra("poster_path", thumbnail);
               startActivity(intent);
            }
        });

    }

    private void removeFavorite() {
        int movie_id = getIntent().getIntExtra("id", 110);
        favoriteDbHelper = new FavoriteDbHelper(activity);
        favoriteDbHelper.deleteFavorite(movie_id);
    }

    private boolean saveFavorite() {
        favoriteDbHelper = new FavoriteDbHelper(activity);
        int movie_id = getIntent().getIntExtra("id", 110);
        String rating = getIntent().getStringExtra("vote_average");
        String poster = getIntent().getStringExtra("poster_path");

        favorite = new Movie();
        favorite.setOverview(synopsis.getText().toString());
        favorite.setPosterPath(poster);
        favorite.setVoteaverage(Double.parseDouble(rating));
        favorite.setId(movie_id);
        favorite.setOriginalTitle(movieName.getText().toString());

        boolean flag = favoriteDbHelper.addFavorite(favorite, getApplicationContext());
        return flag;
    }

    private void initCollapsingToolbar() {

        final CollapsingToolbarLayout collapsingToolbarLayout =
                findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle("");
        AppBarLayout appBarLayout =  findViewById(R.id.appbar);
        appBarLayout.setExpanded(true);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();

                }
                if (scrollRange == 0) {
                    collapsingToolbarLayout.setTitle(movieName.getText().toString());
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbarLayout.setTitle(movieName.getText().toString());
                    isShow = false;
                }
            }
        });

    }

    private void initViews() {
        trailerList = new ArrayList<>();
        adapter = new TrailerAdapter(this, trailerList);

        recyclerView = findViewById(R.id.recycler_view1);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        loadJSON();
    }

    private void loadJSON() {

        int movie_id = getIntent().getIntExtra("id", 354912);
        try {
            if (BuildConfig.THE_MOVIE_DB_API_TOKEN.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please Obtain API Key", Toast.LENGTH_SHORT).show();
                return;
            }

            Client Client = new Client();
            Service apiService = Client.getClient().create(Service.class);
            Call<TrailerResponse> call = apiService.getMovieTrailer(movie_id, BuildConfig.THE_MOVIE_DB_API_TOKEN);

            call.enqueue(new Callback<TrailerResponse>() {
                @Override
                public void onResponse(Call<TrailerResponse> call, Response<TrailerResponse> response) {
                    if (response != null) {
                        if (response.body() != null) {
                            List<Trailer> list = response.body().getResults();
                            recyclerView.setAdapter(new TrailerAdapter(getApplicationContext(), list));
                            recyclerView.smoothScrollToPosition(0);
                        } else {
                            Toast.makeText(DetailActivity.this, "Response was empty", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(DetailActivity.this, "Response was empty", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<TrailerResponse> call, Throwable t) {
                    Log.d("Error", t.getMessage());
                    Toast.makeText(DetailActivity.this, "Error fetching trailer data", Toast.LENGTH_SHORT).show();
                }
            });
        }
        catch (Exception e) {
            Log.d("Error", e.getMessage());
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
