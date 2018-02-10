package com.aaronlife.ratecalculator.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

//取得台銀匯率網頁與解析出匯率資料的類別
public class BotRateParser {
    public static final String BOT_ADDRESS_NEW =
            "http://rate.bot.com.tw/xrt?Lang=zh-TW";

    public RateData parse() {
        String csvAddress = getRateCsvLink();

        //若無法取得資料則傳回null
        if (csvAddress == null) return null;

        return vaildateRateData(getRateCsvData(csvAddress));
    }

    // 如果有某個幣別匯率為0，則判定該次更新無效
    private RateData vaildateRateData(RateData rateData) {
        if (rateData.usd <= 0.0F) return null;
        if (rateData.eur <= 0.0F) return null;
        if (rateData.jpy <= 0.0F) return null;
        if (rateData.krw <= 0.0F) return null;
        if (rateData.hkd <= 0.0F) return null;
        if (rateData.twd <= 0.0F) return null;
        if (rateData.idr <= 0.0F) return null;
        if (rateData.cny <= 0.0F) return null;
        if (rateData.gbp <= 0.0F) return null;
        if (rateData.aud <= 0.0F) return null;
        if (rateData.cad <= 0.0F) return null;
        if (rateData.sgd <= 0.0F) return null;
        if (rateData.chf <= 0.0F) return null;
        if (rateData.zar <= 0.0F) return null;
        if (rateData.sek <= 0.0F) return null;
        if (rateData.nzd <= 0.0F) return null;
        if (rateData.thb <= 0.0F) return null;
        if (rateData.php <= 0.0F) return null;
        if (rateData.vnd <= 0.0F) return null;
        if (rateData.myr <= 0.0F) return null;

        return rateData;
    }

    private String getRateCsvLink() {
        try {
            URL url = new URL(BOT_ADDRESS_NEW);

            HttpURLConnection httpURLConnection =
                    (HttpURLConnection) url.openConnection();

            httpURLConnection.setConnectTimeout(2000);
            httpURLConnection.setReadTimeout(5000);
            httpURLConnection.connect();

            InputStream is = httpURLConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "utf8");
            BufferedReader reader = new BufferedReader(isr);

            String line = "";

            while ((line = reader.readLine()) != null) {
                if (line.indexOf("下載 Excel 檔") != -1) {
                    line = line.substring(line.indexOf("href=\"") + 6,
                            line.lastIndexOf("\">"));
                    line = line.replace("&amp;", "&");

                    return "http://rate.bot.com.tw" + line;
                }
            }
        } catch (IOException e) {
        }

        return null;
    }

    private RateData getRateCsvData(String csvAddress) {
        RateData rd = new RateData(); //建立RateData物件，用來存放匯率
        rd.twd = 1; //台幣

        try {
            URL url = new URL(csvAddress);

            HttpURLConnection httpURLConnection =
                    (HttpURLConnection) url.openConnection();

            httpURLConnection.setConnectTimeout(2000);
            httpURLConnection.setReadTimeout(5000);
            httpURLConnection.setRequestProperty("Accept-Language",
                    "en-US,en;q=0.8,zh-TW;q=0.6,zh;q=0.4");
            httpURLConnection.connect();

            // 網頁伺服器回應HTTP錯誤碼200
            if (httpURLConnection.getResponseCode() != 200) return null;

            InputStream is = httpURLConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "utf8");
            BufferedReader reader = new BufferedReader(isr);

            String line = "";

            while ((line = reader.readLine()) != null) {
                String[] rateData = line.split(",");

                float val = 0;

                try {
                    // 現金賣出匯率
                    val = Float.parseFloat(rateData[12].trim());

                    // 如果無「現金賣出」匯率，則使用「即期賣出」匯率
                    if (val == 0)
                        val = Float.parseFloat(rateData[13].trim());
                } catch (RuntimeException e) {
                    continue;
                }

                switch ((rateData[0].trim())) {
                    case RateData.USD:
                        rd.usd = val;
                        break;
                    case RateData.EUR:
                        rd.eur = val;
                        break;
                    case RateData.JPY:
                        rd.jpy = val;
                        break;
                    case RateData.KRW:
                        rd.krw = val;
                        break;
                    case RateData.HKD:
                        rd.hkd = val;
                        break;
                    case RateData.IDR:
                        rd.idr = val;
                        break;
                    case RateData.CNY:
                        rd.cny = val;
                        break;

                    case RateData.GBP:
                        rd.gbp = val;
                        break;
                    case RateData.AUD:
                        rd.aud = val;
                        break;
                    case RateData.CAD:
                        rd.cad = val;
                        break;
                    case RateData.SGD:
                        rd.sgd = val;
                        break;
                    case RateData.CHF:
                        rd.chf = val;
                        break;
                    case RateData.ZAR:
                        rd.zar = val;
                        break;
                    case RateData.SEK:
                        rd.sek = val;
                        break;
                    case RateData.NZD:
                        rd.nzd = val;
                        break;
                    case RateData.THB:
                        rd.thb = val;
                        break;
                    case RateData.PHP:
                        rd.php = val;
                        break;
                    case RateData.VND:
                        rd.vnd = val;
                        break;
                    case RateData.MYR:
                        rd.myr = val;
                        break;
                }
            }
        } catch (IOException e) {
            return null;
        }

        return rd;
    }
}