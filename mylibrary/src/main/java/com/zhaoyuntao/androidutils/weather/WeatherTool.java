package com.zhaoyuntao.androidutils.weather;

import com.zhaoyuntao.androidutils.tools.S;
import com.zhaoyuntao.androidutils.tools.ZHttp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeatherTool {

    public static void getWeather2(String city, final CallBack callBack) {
        String url = "http://wthrcdn.etouch.cn/weather_mini?city=" + city;
        S.s("正在查询天气:" + url);
        final String finalCity = city;
        ZHttp.getInstance().request(url, new ZHttp.CallBack() {
            @Override
            public void onFailure(IOException e) {
                S.e("请求失败:" + e.getMessage());
                if (callBack != null) {
                    callBack.whenFailed("请求失败:" + e.getMessage());
                }
            }

            @Override
            public void onResponse(byte[] response) {
                String msg = "";
                try {
                    String result = new String(response, "utf-8");
                    S.s("请求结果:" + result);

                    JSONObject jsonObject = new JSONObject(result);
                    int code = jsonObject.getInt("status");
                    if (code == 1000) {
                        S.s("请求成功");

                        List<Weather> list = new ArrayList<>();
                        try {
                            JSONObject dataJson = jsonObject.getJSONObject("data");
                            String sugggestion = dataJson.getString("ganmao");
                            int wendu = getNumber(dataJson.getString("wendu"));
                            JSONArray forecast = dataJson.getJSONArray("forecast");

                            for (int i = 0; i < forecast.length(); i++) {
                                Weather weather = new Weather();
                                CityInfo cityInfo = new CityInfo();
                                cityInfo.cityName = finalCity;
                                weather.cityInfo = cityInfo;
                                weather.tem_now=wendu;
                                weather.sugggestion = sugggestion;
                                JSONObject weatherJson = forecast.getJSONObject(i);
                                try {
                                    String date = weatherJson.getString("date");//日期
                                    if (S.isNotEmpty(date)) {
                                        if (date.contains("日")) {
                                            String[] arr = date.trim().split("日");
                                            if (arr != null && arr.length == 2) {
                                                try {
                                                    weather.date_day = Integer.parseInt(arr[0]);
                                                } catch (NumberFormatException e) {
                                                    S.e(e.getMessage());
                                                }
                                                weather.date_xingqi = arr[1];
                                            }
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    weather.direct = weatherJson.getString("fengxiang");//风向
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    weather.power = weatherJson.getString("fengli");//风力
                                    weather.power = weather.power.replace("<![CDATA[", "");
                                    weather.power = weather.power.replace("]]>", "");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    weather.temperature_high = getNumber(weatherJson.getString("high"));//气温
                                    weather.temperature_low = getNumber(weatherJson.getString("low"));//气温
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    weather.humidity = weatherJson.getString("humidity");//湿度
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    weather.weather = weatherJson.getString("type");//天气情况
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                list.add(weather);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            S.e(e);
                        }
                        if (callBack != null) {
                            callBack.whenGotWeather(list);
                        }
                    } else {
                        try {
                            msg = jsonObject.getString("reason");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            S.e(e);
                        }
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    S.e(e);
                    msg = e.getMessage();
                } catch (JSONException e) {
                    e.printStackTrace();
                    msg = e.getMessage();
                }
                if (callBack != null) {
                    callBack.whenFailed(msg);
                }
            }
        });
    }

    public static int getNumber(String number) {
        Pattern compile = Pattern.compile("\\d+");
        Matcher matcher = compile.matcher(number);
        matcher.find();
        String string = matcher.group();//提取匹配到的结果
        int num = Integer.parseInt(string);
        return num;
    }

    public static void getWeather(final CallBack callBack) {
        getCity(new CallBack_city() {
            @Override
            public void whenGotCity(CityInfo cityInfo) {
                getWeather2(cityInfo.cityName, callBack);
            }

            @Override
            public void whenFailed(String msg) {
                if (callBack != null) {
                    callBack.whenFailed(msg);
                }
            }
        });
    }

    public static void getWeather(CityInfo cityInfo, final CallBack callBack) {
        getWeather2(cityInfo.cityName, callBack);
    }

    public static void getCity(final CallBack_city callBack) {
        //var returnCitySN = {"cip": "123.113.217.158", "cid": "110105", "cname": "北京市朝阳区"};
        String url = "https://pv.sohu.com/cityjson";
        S.s("正在查询天气:" + url);
        ZHttp.getInstance().request(url, new ZHttp.CallBack() {
            @Override
            public void onFailure(IOException e) {
                S.e("getCity失败:" + e.getMessage());
                if (callBack != null) {
                    callBack.whenFailed("getCity失败:" + e.getMessage());
                }
            }

            @Override
            public void onResponse(byte[] response) {
                try {
                    String result = new String(response, "GBK");
                    S.s("getCity成功:" + result);
                    if (S.isEmpty(result)) {
                        S.e("getCity Error:result is null");
                        if (callBack != null) {
                            callBack.whenFailed("getCity Error:result is null");
                        }
                        return;
                    }
                    int index = result.indexOf('{');
                    if (index == -1) {
                        S.e("getCity Error:result dose not contains result JSON");
                        if (callBack != null) {
                            callBack.whenFailed("getCity Error:result dose not contains result JSON");
                        }
                        return;
                    }
                    result = result.substring(index);
                    S.s("截取到json:" + result);
                    CityInfo cityInfo = new CityInfo();
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        String cityString = jsonObject.getString("cname");
                        int start = cityString.indexOf("省");
                        if (start != -1) {
                            String province = cityString.substring(0, start);
                            S.s("获取到省信息:" + province);
                            cityInfo.province = province;
                            int length = cityString.length();
                            if (length > (start + 1)) {
                                cityString = cityString.substring(start + 1, length);
                            }
                        }
                        start = cityString.indexOf("市");
                        if (start != -1) {
                            String city = cityString.substring(0, start);
                            S.s("获取到市:" + city);
                            cityInfo.cityName = city;
                            int length = cityString.length();
                            if (length > (start + 1)) {
                                cityString = cityString.substring(start + 1, length);
                            }
                        }
                        start = cityString.indexOf("区");

                        if (start != -1) {
                            String area = cityString.substring(0, start);
                            S.s("获取到区:" + area);
                            cityInfo.area = area;
                        }

                        if (callBack != null) {
                            callBack.whenGotCity(cityInfo);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    S.e(e);
                }
                if (callBack != null) {
                    callBack.whenFailed("获取城市信息失败");
                }
            }
        });
    }

    public interface CallBack {
        void whenGotWeather(List<Weather> weathers);

        void whenFailed(String msg);
    }

    public interface CallBack_city {
        void whenGotCity(CityInfo cityInfo);

        void whenFailed(String msg);
    }
}
