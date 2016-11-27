package com.yzq.android.experimentnine.Activity;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;
import android.widget.TextView;

import com.yzq.android.experimentnine.Compoment.Forecast;
import com.yzq.android.experimentnine.Compoment.Index;
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
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private Map<String, String> cityData = new HashMap<String, String>();
    private SearchView searchView;
    private TextView cityId, cityInfo;

    private static final int UPDATE_CONTENT = 0;
    private static final String[] weather_info_tag = new String[] {"wendu", "shidu", "fengxiang", "fengli"};
    private static final String[] extra_info_tag = new String[] {"aqi", "pm25", "suggest", "quality", "MajorPollutants"};
    private static final String[] weather_forward_tag = new String[] {"date", "high", "low", "type", "type"};
    private static final String[] extra_index_tag = new String[] {"name", "value", "detail"};

    private String cityName, updateTime;
    private String[] weatherInfo = new String[4]; // 温度，湿度，风向，风力
    private String[] extraInfo = new String[5]; // 空气质量指数， pm2.5， 建议， 污染程度， 主要污染物
    private ArrayList<Forecast> weatherForward = new ArrayList<Forecast>(); // 未来5天气温
    private ArrayList<Index> extraIndex = new ArrayList<Index>(); // 各种指数

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
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //decodeCityId();
        searchView = (SearchView)findViewById(R.id.search_view);
        cityInfo = (TextView)findViewById(R.id.city_info);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String cityQueryID = cityData.get(query);
                WeatherSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void WeatherSearch(final String cityName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "http://wthrcdn.etouch.cn/WeatherApi?city="+cityName;
                try {
                    URL text = new URL(url);
                    HttpURLConnection http = (HttpURLConnection)text.openConnection();
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
                    e.printStackTrace();
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
            refresh();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setCityInfo() throws IOException, XmlPullParserException {
        boolean complete = false;
        while (!complete) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if ("city".equals(parser.getName())) {
                        cityName = parser.nextText();
                    } else if ("updatetime".equals(parser.getName())) {
                        updateTime = parser.nextText();
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

    private void refresh() {
        Log.i("cityname", cityName);
        Log.i("updateTime", updateTime);
        Log.i("shidu", weatherInfo[1]);
        Log.i("jianyi", extraInfo[2]);
        Log.i("di2tian", weatherForward.get(2).getType());
        Log.i("yuehui", extraIndex.get(9).getName()+" "+extraIndex.get(9).getDetail());
    }
}
