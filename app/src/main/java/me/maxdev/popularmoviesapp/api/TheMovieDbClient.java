package me.maxdev.popularmoviesapp.api;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public final class TheMovieDbClient {

    private static final String BASE_URL = "http://api.themoviedb.org/3/";
    private static final String CACHE_DIR = "HttpResponseCache";
    private static final long CACHE_SIZE = 10 * 1024 * 1024;    // 10 MB
    private static final int CONNECT_TIMEOUT = 15;
    private static final int WRITE_TIMEOUT = 60;
    private static final int TIMEOUT = 60;

    private static volatile TheMovieDbService instance;

    private TheMovieDbClient() {
    }

    public static TheMovieDbService getInstance(Context context) {
        synchronized (TheMovieDbService.class) {
            if (instance == null) {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(getClient(context))
                        //.callbackExecutor(new BackgroundThreadExecutor())
                        .build();
                instance = retrofit.create(TheMovieDbService.class);
            }
        }
        return instance;
    }

    private static OkHttpClient getClient(Context context) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(new LoggingInterceptor())
                .addInterceptor(new AuthorizationInterceptor());

        final File baseDir = context.getCacheDir();
        if (baseDir != null) {
            final File cacheDir = new File(baseDir, CACHE_DIR);
            builder.cache(new Cache(cacheDir, CACHE_SIZE));
        }

        return builder.build();
    }

    private static class BackgroundThreadExecutor implements Executor {

        private static Handler handler = new Handler();

        @Override
        public void execute(@NonNull Runnable command) {
            handler.post(command);
        }
    }
}
