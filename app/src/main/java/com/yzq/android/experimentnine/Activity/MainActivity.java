package com.yzq.android.experimentnine.Activity;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yzq.android.experimentnine.Adapter.ForecastAdapter;
import com.yzq.android.experimentnine.Adapter.IndexAdapter;
import com.yzq.android.experimentnine.Compoment.Forecast;
import com.yzq.android.experimentnine.Compoment.Index;
import com.yzq.android.experimentnine.Exception.MyException;
import com.yzq.android.experimentnine.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private TextView cityname, cityupdatetime, temperate, humidity, wind, aqi, pm, suggest, quality, pollutants;
    private ListView indexList;
    private RecyclerView forecastList;

    private static final int UPDATE_CONTENT = 0;
    private static final int NO_NETWORK_FOUND = 1;
    private static final String[] weather_info_tag = new String[] {"wendu", "shidu", "fengxiang", "fengli"};
    private static final String[] extra_info_tag = new String[] {"aqi", "pm25", "suggest", "quality", "MajorPollutants"};
    private static final String[] weather_forward_tag = new String[] {"date", "high", "low", "type", "type"};
    private static final String[] extra_index_tag = new String[] {"name", "value", "detail"};

    private String cityName, updateTime;
    private String[] weatherInfo = new String[4]; // 温度，湿度，风向，风力
    private String[] extraInfo = new String[5]; // 空气质量指数， pm2.5， 建议， 污染程度， 主要污染物
    private ArrayList<Forecast> weatherForward; // 未来5天气温
    private ArrayList<Index> extraIndex; // 各种指数

    private XmlPullParserFactory factory;
    private XmlPullParser parser;
    private int eventType;

    private Handler handler = new Handler() {
        public void handleMessage(Message message) {
            switch (message.what) {
                case UPDATE_CONTENT:
                    String res = (String)message.obj;
                    refreshLayout(res);
                    break;
                case NO_NETWORK_FOUND:
                    Toast.makeText(MainActivity.this, "当前没有可用网络!", Toast.LENGTH_SHORT).show();
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityname = (TextView)findViewById(R.id.city_name);
        cityupdatetime = (TextView)findViewById(R.id.city_update_time);
        temperate = (TextView)findViewById(R.id.temperature);
        humidity = (TextView)findViewById(R.id.humidity);
        wind = (TextView)findViewById(R.id.wind);
        aqi = (TextView)findViewById(R.id.aqi);
        pm = (TextView)findViewById(R.id.pm);
        suggest = (TextView)findViewById(R.id.suggest);
        quality = (TextView)findViewById(R.id.air_quality);
        pollutants = (TextView)findViewById(R.id.pollutants);
        indexList = (ListView)findViewById(R.id.index_list);
        forecastList = (RecyclerView)findViewById(R.id.forecast_list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_bar, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        android.support.v7.widget.SearchView searchView = (android.support.v7.widget.SearchView) searchMenuItem.getActionView();
        searchView.setQueryHint("请输入城市名");
        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                WeatherSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    private void WeatherSearch(final String cityName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "http://wthrcdn.etouch.cn/WeatherApi?city="+cityName;
                HttpURLConnection http = null;
                try {
                    URL text = new URL(url);
                    http = (HttpURLConnection)text.openConnection();
                    if (http.getResponseCode() == 200) {
                        InputStream in = http.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        StringBuilder response = new StringBuilder();
                        String mes;
                        while ((mes = reader.readLine()) != null) {
                            response.append(mes);
                        }
                        Message message = new Message();
                        message.what = UPDATE_CONTENT;
                        message.obj = response.toString();
                        handler.sendMessage(message);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    Message message = new Message();
                    message.what = NO_NETWORK_FOUND;
                    handler.sendMessage(message);
                    e.printStackTrace();
                } finally {
                    if (http != null) {
                        http.disconnect();
                    }
                }
            }
        }).start();
    }

    private void refreshLayout(String res) {
        try {
            factory = XmlPullParserFactory.newInstance();
            parser = factory.newPullParser();
            parser.setInput(new StringReader(res));
            eventType = parser.getEventType();
            setCityInfo();
            setWeatherInfo();
            setExtraInfo();
            setWeatherForward();
            serExtraIndex();
            refreshUI();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            Toast.makeText(MainActivity.this, "当前城市不存在，请重新输入", Toast.LENGTH_SHORT).show();
        }
    }

    private void setCityInfo() throws IOException, XmlPullParserException, MyException {
        boolean complete = false;
        while (!complete) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if ("error".equals(parser.getName())) {
                        throw new MyException(parser.nextText());
                    }
                    else if ("city".equals(parser.getName())) {
                        cityName = parser.nextText();
                    } else if ("updatetime".equals(parser.getName())) {
                        updateTime = "更新时间："+parser.nextText();
                        complete = true;
                    }
                    break;
                default:
                    break;
            }
            eventType = parser.next();
        }
    }

    private void setWeatherInfo() throws IOException, XmlPullParserException {
        int[] order = new int[]{0, 3, 1, 2};
        int i = 0;
        while (i < order.length) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if (weather_info_tag[order[i]].equals(parser.getName())) {
                        weatherInfo[order[i]] = parser.nextText();
                        i++;
                    }
                    break;
                default:
                    break;
            }
            eventType = parser.next();
        }
    }

    private void setExtraInfo() throws IOException, XmlPullParserException {
        int[] order = new int[]{0, 1, 2, 3, 4};
        int i = 0;
        while (i < order.length) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if (extra_info_tag[order[i]].equals(parser.getName())) {
                        extraInfo[order[i]] = parser.nextText();
                        i++;
                    }
                    break;
                default:
                    break;
            }
            eventType = parser.next();
        }
    }

    private void setWeatherForward() throws IOException, XmlPullParserException {
        weatherForward = new ArrayList<Forecast>();
        int[] order = new int[]{0, 1, 2, 3};
        int i = 0;
        int total = 5;
        String[] w = new String[5]; // date, high, low, type, night_type;
        while (i < total) {
            int j  = 0;
            while (j < order.length) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (weather_forward_tag[order[j]].equals(parser.getName())) {
                            w[order[j]] = parser.nextText();
                            j++;
                        }
                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }
            weatherForward.add(new Forecast(w[0], w[1], w[2], w[3]));
            i++;
        }
    }

    private void serExtraIndex() throws IOException, XmlPullParserException {
        extraIndex = new ArrayList<Index>();
        int[] order = new int[]{0, 1, 2};
        int i = 0;
        int total = 11;
        String[] w = new String[3]; // name, value, detail
        while (i < total) {
            int j  = 0;
            while (j < order.length) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (extra_index_tag[order[j]].equals(parser.getName())) {
                            w[order[j]] = parser.nextText();
                            j++;
                        }
                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }
            extraIndex.add(new Index(w[0], w[1], w[2]));
            i++;
        }
    }

    private void refreshUI() {
        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.activity_main);
        linearLayout.setVisibility(View.VISIBLE);
        cityname.setText(cityName);
        cityupdatetime.setText(updateTime);
        String temp = weatherInfo[0] + "℃";
        temperate.setText(temp);
        temp = "湿度："+ weatherInfo[1];
        humidity.setText(temp);
        temp = weatherInfo[2] + " " + weatherInfo[3];
        wind.setText(temp);
        temp = "空气质量指数："+ extraInfo[0];
        aqi.setText(temp);
        temp = "pm2.5："+ extraInfo[1];
        pm.setText(temp);
        suggest.setText(extraInfo[2]);
        temp = "空气质量："+ extraInfo[3];
        quality.setText(temp);
        temp = "主要污染物："+ (extraInfo[4].isEmpty()? "无" : extraInfo[4]);
        pollutants.setText(temp);
        IndexAdapter indexAdapter = new IndexAdapter(MainActivity.this, extraIndex);

        indexList.setAdapter(indexAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);

        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        forecastList.setLayoutManager(layoutManager);
        ForecastAdapter forecastAdapter = new ForecastAdapter(MainActivity.this, weatherForward);
        forecastList.setAdapter(forecastAdapter);
    }
}
