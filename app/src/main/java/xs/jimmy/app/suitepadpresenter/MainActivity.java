package xs.jimmy.app.suitepadpresenter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;

public class MainActivity extends AppCompatActivity {

    private WebView mPresenterWebview;


    private boolean isBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPresenterWebview = findViewById(R.id.presenter);

        mPresenterWebview.post(new Runnable() {
            @Override
            public void run() {
                WebSettings webSettings = mPresenterWebview.getSettings();
                webSettings.setJavaScriptEnabled(true);
                webSettings.setAllowUniversalAccessFromFileURLs(true);
                WebViewClient client =  new WebViewClient() {
                    @Override
                    public WebResourceResponse shouldInterceptRequest(final WebView view, WebResourceRequest request) {
                        Retrofit retrofit = new Retrofit.Builder()
                                .addConverterFactory(ScalarsConverterFactory.create())
                                .addConverterFactory(GsonConverterFactory.create())
                                .baseUrl("http://localhost:8080")
                                .build();

                        getData service = retrofit.create(getData.class);

                        Call<String> repos = service.listRepos();
                        String data = "";
                        try {
                            data = repos.execute().body();
                            return new WebResourceResponse("text/plain", "UTF-8", new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                };
                mPresenterWebview.setWebViewClient(client);
                mPresenterWebview.loadUrl("file:///android_asset/sample.html");
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        //Bind to the remote service
        Intent intentProxy = new Intent();
        intentProxy.setClassName("xs.jimmy.app.suitepadproxy", "xs.jimmy.app.suitepadproxy.ProxyService");

        this.bindService(intentProxy, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        //Unbind if it is bound to the service
        if(this.isBound)
        {
            this.unbindService(null);
            this.isBound = false;
        }
    }


    public interface getData {
        @GET("/")
        Call<String> listRepos();
    }
}
