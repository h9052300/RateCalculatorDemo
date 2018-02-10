package com.aaronlife.ratecalculator;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aaronlife.ratecalculator.adapter.ResultAdapter;
import com.aaronlife.ratecalculator.util.BotRateParser;
import com.aaronlife.ratecalculator.util.PersistentData;
import com.aaronlife.ratecalculator.util.RateData;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private ListView listResult;
    private TextView txtMsg, txtTotal, txtHistory, txtCurrency;
    private ResultAdapter resultAdapter;

    public static final int UPDATE_EXCHANGE_RATE_FAILED = -1;
    public static final int REFRESH_EXCHANGE_RATE_UI = 0;
    public static final int UPDATE_EXCHANGE_RATE_SUCCESS = 1;

    private RateData rateNow = new RateData();
    private RateData rateTWD = null;

    private Handler handler = new MyHandler(this);

    private String baseCurrency = RateData.TWD;
    private double total = 0;
    private String history = "";
    private String updateTime = "";

    private PersistentData persistentData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        // 顯示版本名稱
        TextView txtVersion = (TextView) findViewById(R.id.version);
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        txtVersion.setText(pInfo.versionName);

        getSupportActionBar().hide();

        initUI();

        persistentData = new PersistentData(this);

        updateTime = persistentData.loadUpdateTime();
        txtMsg.setText(getString(R.string.last_update) + updateTime);

        baseCurrency = persistentData.loadBaseCurrency();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 要先載入舊的值，以免匯率更新失敗無法使用
        rateTWD = persistentData.loadExchangeRateDataTWD();
        updateCurrency();
        handler.sendEmptyMessage(REFRESH_EXCHANGE_RATE_UI);

        Log.d("aarontest", "==>" + rateTWD.jpy);

        if (updateTime.length() < 10 || persistentData.isNeedUpdate()) {
            new ExchangeRateUpdateThread().start();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh_rate) {
            new ExchangeRateUpdateThread().start();
        } else if (id == R.id.action_rate_info) {
            AlertDialog.Builder myDlg = new AlertDialog.Builder(this);

            myDlg.setTitle(getString(R.string.rate_info) + "(" +
                    getString(R.string.current) +
                    txtCurrency.getText() + ")");

            myDlg.setMessage(
                    String.format("%s:%10.5f\n", getString(R.string.usd), rateNow.usd) +
                            String.format("%s:%10.5f\n", getString(R.string.eur), rateNow.eur) +
                            String.format("%s:%10.5f\n", getString(R.string.jpy), rateNow.jpy) +
                            String.format("%s:%10.5f\n", getString(R.string.krw), rateNow.krw) +
                            String.format("%s:%10.5f\n", getString(R.string.hkd), rateNow.hkd) +
                            String.format("%s:%10.5f\n", getString(R.string.twd), rateNow.twd) +
                            String.format("%s:%10.5f\n", getString(R.string.idr), rateNow.idr) +
                            String.format("%s:%10.5f\n", getString(R.string.cny), rateNow.cny) +
                            String.format("%s:%10.5f\n", getString(R.string.gbp), rateNow.gbp) +
                            String.format("%s:%10.5f\n", getString(R.string.aud), rateNow.aud) +
                            String.format("%s:%10.5f\n", getString(R.string.cad), rateNow.cad) +
                            String.format("%s:%10.5f\n", getString(R.string.sgd), rateNow.sgd) +
                            String.format("%s:%10.5f\n", getString(R.string.chf), rateNow.chf) +
                            String.format("%s:%10.5f\n", getString(R.string.zar), rateNow.zar) +
                            String.format("%s:%10.5f\n", getString(R.string.sek), rateNow.sek) +
                            String.format("%s:%10.5f\n", getString(R.string.nzd), rateNow.nzd) +
                            String.format("%s:%10.5f\n", getString(R.string.thb), rateNow.thb) +
                            String.format("%s:%10.5f\n", getString(R.string.php), rateNow.php) +
                            String.format("%s:%10.5f\n", getString(R.string.vnd), rateNow.vnd) +
                            String.format("%s:%10.5f", getString(R.string.myr), rateNow.myr));

            AlertDialog dlg = myDlg.show();

            TextView textView = (TextView) dlg.findViewById(android.R.id.message);
            textView.setTypeface(Typeface.MONOSPACE);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
        }

        return super.onOptionsItemSelected(item);
    }

    private void initUI() {
        txtMsg = (TextView) findViewById(R.id.txt_update_time);
        listResult = (ListView) findViewById(R.id.list_result);

        txtTotal = (TextView) findViewById(R.id.txt_total);
        txtHistory = (TextView) findViewById(R.id.txt_history);

        txtCurrency = (TextView) findViewById(R.id.txt_currency);

        resultAdapter = new ResultAdapter(this, rateNow, total);
        listResult.setAdapter(resultAdapter);
    }

    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity act = mActivity.get();

            if (act != null) {
                switch (msg.what) {
                    case MainActivity.REFRESH_EXCHANGE_RATE_UI:
                        switch (act.baseCurrency) {
                            case RateData.USD:
                                act.txtCurrency.setText(act.getString(R.string.usd_short));
                                break;

                            case RateData.EUR:
                                act.txtCurrency.setText(act.getString(R.string.eur_short));
                                break;

                            case RateData.JPY:
                                act.txtCurrency.setText(act.getString(R.string.jpy_short));
                                break;

                            case RateData.KRW:
                                act.txtCurrency.setText(act.getString(R.string.krw_short));
                                break;

                            case RateData.HKD:
                                act.txtCurrency.setText(act.getString(R.string.hkd_short));
                                break;

                            case RateData.TWD:
                                act.txtCurrency.setText(act.getString(R.string.twd_short));
                                break;

                            case RateData.IDR:
                                act.txtCurrency.setText(act.getString(R.string.idr_short));
                                break;

                            case RateData.CNY:
                                act.txtCurrency.setText(act.getString(R.string.cny_short));
                                break;

                            case RateData.GBP:
                                act.txtCurrency.setText(act.getString(R.string.gbp_short));
                                break;

                            case RateData.AUD:
                                act.txtCurrency.setText(act.getString(R.string.aud_short));
                                break;

                            case RateData.CAD:
                                act.txtCurrency.setText(act.getString(R.string.cad_short));
                                break;

                            case RateData.SGD:
                                act.txtCurrency.setText(act.getString(R.string.sgd_short));
                                break;

                            case RateData.CHF:
                                act.txtCurrency.setText(act.getString(R.string.chf_short));
                                break;

                            case RateData.ZAR:
                                act.txtCurrency.setText(act.getString(R.string.zar_short));
                                break;

                            case RateData.SEK:
                                act.txtCurrency.setText(act.getString(R.string.sek_short));
                                break;

                            case RateData.NZD:
                                act.txtCurrency.setText(act.getString(R.string.nzd_short));
                                break;

                            case RateData.THB:
                                act.txtCurrency.setText(act.getString(R.string.thb_short));
                                break;

                            case RateData.PHP:
                                act.txtCurrency.setText(act.getString(R.string.php_short));
                                break;

                            case RateData.VND:
                                act.txtCurrency.setText(act.getString(R.string.vnd_short));
                                break;

                            case RateData.MYR:
                                act.txtCurrency.setText(act.getString(R.string.myr_short));
                                break;
                        }

                        if (act.updateTime.length() < 10) break;

                        act.txtMsg.setText(act.getString(R.string.last_update)
                                + act.updateTime);

                        act.resultAdapter.update(act.rateNow, act.total);
                        act.resultAdapter.notifyDataSetChanged();

                        break;

                    case UPDATE_EXCHANGE_RATE_SUCCESS:
                        Toast.makeText(act,
                                act.getString(R.string.update_ok),
                                Toast.LENGTH_SHORT).show();
                        break;

                    case UPDATE_EXCHANGE_RATE_FAILED:
                        Toast.makeText(act,
                                act.getString(R.string.update_failed),
                                Toast.LENGTH_SHORT).show();
                        break;
                }


                if (act.total == (int) act.total)
                    act.txtTotal.setText(String.format("%.0f", act.total));
                else {
                    BigDecimal c = new BigDecimal(String.valueOf(act.total));

                    if (c.toPlainString().length() > 13)
                        act.txtTotal.setText(c.toPlainString().substring(0, 13));
                    else
                        act.txtTotal.setText(c.toPlainString());
                }

                act.txtHistory.setText(act.history);
            }
        }
    }

    protected class ExchangeRateUpdateThread extends Thread {
        @Override
        public void run() {
            RateData rateTmp = new BotRateParser().parse();

            if (rateTmp != null) {
                rateTWD = rateTmp;

                persistentData.saveExchangeRateDataTWD(rateTWD);

                SimpleDateFormat sdf =
                        new SimpleDateFormat("yyyy/MM/dd HH:mm");
                updateTime = sdf.format(Calendar.getInstance().getTime());

                persistentData.saveUpdateTime(updateTime);

                updateCurrency();

                handler.sendEmptyMessage(REFRESH_EXCHANGE_RATE_UI);
                handler.sendEmptyMessage(UPDATE_EXCHANGE_RATE_SUCCESS);
            } else {
                handler.sendEmptyMessage(UPDATE_EXCHANGE_RATE_FAILED);
            }

            super.run();
        }
    }

    protected void updateCurrency() {
        float baseCurrencyRate = 0;

        switch (baseCurrency) {
            case RateData.USD:
                baseCurrencyRate = rateTWD.usd;
                break;
            case RateData.EUR:
                baseCurrencyRate = rateTWD.eur;
                break;
            case RateData.JPY:
                baseCurrencyRate = rateTWD.jpy;
                break;
            case RateData.KRW:
                baseCurrencyRate = rateTWD.krw;
                break;
            case RateData.HKD:
                baseCurrencyRate = rateTWD.hkd;
                break;
            case RateData.TWD:
                baseCurrencyRate = rateTWD.twd;
                break;
            case RateData.IDR:
                baseCurrencyRate = rateTWD.idr;
                break;
            case RateData.CNY:
                baseCurrencyRate = rateTWD.cny;
                break;

            case RateData.GBP:
                baseCurrencyRate = rateTWD.gbp;
                break;
            case RateData.AUD:
                baseCurrencyRate = rateTWD.aud;
                break;
            case RateData.CAD:
                baseCurrencyRate = rateTWD.cad;
                break;
            case RateData.SGD:
                baseCurrencyRate = rateTWD.sgd;
                break;
            case RateData.CHF:
                baseCurrencyRate = rateTWD.chf;
                break;
            case RateData.ZAR:
                baseCurrencyRate = rateTWD.zar;
                break;
            case RateData.SEK:
                baseCurrencyRate = rateTWD.sek;
                break;
            case RateData.NZD:
                baseCurrencyRate = rateTWD.nzd;
                break;
            case RateData.THB:
                baseCurrencyRate = rateTWD.thb;
                break;
            case RateData.PHP:
                baseCurrencyRate = rateTWD.php;
                break;
            case RateData.VND:
                baseCurrencyRate = rateTWD.vnd;
                break;
            case RateData.MYR:
                baseCurrencyRate = rateTWD.myr;
                break;
        }

        rateNow.usd = rateTWD.usd / baseCurrencyRate;
        rateNow.eur = rateTWD.eur / baseCurrencyRate;
        rateNow.jpy = rateTWD.jpy / baseCurrencyRate;
        rateNow.krw = rateTWD.krw / baseCurrencyRate;
        rateNow.hkd = rateTWD.hkd / baseCurrencyRate;
        rateNow.twd = rateTWD.twd / baseCurrencyRate;
        rateNow.idr = rateTWD.idr / baseCurrencyRate;
        rateNow.cny = rateTWD.cny / baseCurrencyRate;

        rateNow.gbp = rateTWD.gbp / baseCurrencyRate;
        rateNow.aud = rateTWD.aud / baseCurrencyRate;
        rateNow.cad = rateTWD.cad / baseCurrencyRate;
        rateNow.sgd = rateTWD.sgd / baseCurrencyRate;
        rateNow.chf = rateTWD.chf / baseCurrencyRate;
        rateNow.zar = rateTWD.zar / baseCurrencyRate;
        rateNow.sek = rateTWD.sek / baseCurrencyRate;
        rateNow.nzd = rateTWD.nzd / baseCurrencyRate;
        rateNow.thb = rateTWD.thb / baseCurrencyRate;
        rateNow.php = rateTWD.php / baseCurrencyRate;
        rateNow.vnd = rateTWD.vnd / baseCurrencyRate;
        rateNow.myr = rateTWD.myr / baseCurrencyRate;
    }

    public void changeCurrency(View v) {
        baseCurrency = (String) v.getTag();

        persistentData.saveBaseCurrency(baseCurrency);

        updateCurrency();
        handler.sendEmptyMessage(REFRESH_EXCHANGE_RATE_UI);
    }

    public void calculator(View v) {
        if (updateTime.length() < 10) return;
        if (total >= 999999999) {
            Toast.makeText(this, "已超出計算範圍", Toast.LENGTH_SHORT).show();
            return;
        }

        if ((history.endsWith("+") || history.endsWith("-")) &&
                (v.getTag().equals("+") || v.getTag().equals("-"))) {
            history = history.substring(0, history.length() - 1);
        }

        if (history.endsWith(".") && v.getTag().equals(".")) {
            history = history.substring(0, history.length() - 1);
        }

        if (history.endsWith(".") && (v.getTag().equals("+") || v.getTag().equals("-"))) {
            return;
        }

        if ((v.getTag().equals("+") || v.getTag().equals("-") ||
                v.getTag().equals(".")) && history.length() == 0) {
            return;
        }

        if ((history.endsWith("+") || history.endsWith("-") || history.length() == 0) &&
                v.getTag().equals(".")) {
            return;
        }

        if ((history.endsWith("0") && history.length() == 1) &&
                (v.getTag().equals("+") || v.getTag().equals("-"))) {
            return;
        }

        if ((history.endsWith("0") && history.length() == 1) && !v.getTag().equals(".")) {
            history = history.substring(0, history.length() - 1);
        }

        if ((history.endsWith("+0") || history.endsWith("-0")) && v.getTag().equals("0")) {
            history = history.substring(0, history.length() - 1);
        }

        history += v.getTag();
        sum();
        handler.sendEmptyMessage(REFRESH_EXCHANGE_RATE_UI);
    }

    // 分析history並算出結果
    private void sum() {
        String[] numbers = history.split("\\+|\\-");

        total = Double.parseDouble(numbers[0]);

        int index = numbers[0].length();

        for (int i = 1; i < numbers.length; i++) {
            BigDecimal b1 = new BigDecimal(String.valueOf(total));
            BigDecimal b2 = new BigDecimal(numbers[i]);

            // 取得運算符號並計算
            if (history.charAt(index) == '+')
                total = Double.parseDouble(b1.add(b2).toString());
            else
                total = Double.parseDouble(b1.subtract(b2).toString());

            index += numbers[i].length();
            index++;
        }

        if (total > 999999999) {
            Toast.makeText(this, "已超出計算範圍", Toast.LENGTH_SHORT).show();
            total = 999999999;
        }
    }

    public void modify(View v) {
        if (history.length() > 0)
            history = history.substring(0, history.length() - 1);

        // 這行不能與上面合併，因為history長度為上一個步驟清除一個字元後的結果
        if (history.length() > 0)
            sum();
        else
            total = 0;

        handler.sendEmptyMessage(REFRESH_EXCHANGE_RATE_UI);
    }

    public void clear(View v) {
        total = 0;
        history = "";
        handler.sendEmptyMessage(REFRESH_EXCHANGE_RATE_UI);
    }

    public void rateInfo(View v) {
        AlertDialog.Builder myDlg = new AlertDialog.Builder(this);

        myDlg.setTitle(getString(R.string.rate_info) + "(" +
                getString(R.string.current) +
                txtCurrency.getText() + ")");

        myDlg.setMessage(
                String.format("%s:%10.5f\n", getString(R.string.usd), rateNow.usd) +
                        String.format("%s:%10.5f\n", getString(R.string.eur), rateNow.eur) +
                        String.format("%s:%10.5f\n", getString(R.string.jpy), rateNow.jpy) +
                        String.format("%s:%10.5f\n", getString(R.string.krw), rateNow.krw) +
                        String.format("%s:%10.5f\n", getString(R.string.hkd), rateNow.hkd) +
                        String.format("%s:%10.5f\n", getString(R.string.twd), rateNow.twd) +
                        String.format("%s:%10.5f\n", getString(R.string.idr), rateNow.idr) +
                        String.format("%s:%10.5f\n", getString(R.string.cny), rateNow.cny) +
                        String.format("%s:%10.5f\n", getString(R.string.gbp), rateNow.gbp) +
                        String.format("%s:%10.5f\n", getString(R.string.aud), rateNow.aud) +
                        String.format("%s:%10.5f\n", getString(R.string.cad), rateNow.cad) +
                        String.format("%s:%10.5f\n", getString(R.string.sgd), rateNow.sgd) +
                        String.format("%s:%10.5f\n", getString(R.string.chf), rateNow.chf) +
                        String.format("%s:%10.5f\n", getString(R.string.zar), rateNow.zar) +
                        String.format("%s:%10.5f\n", getString(R.string.sek), rateNow.sek) +
                        String.format("%s:%10.5f\n", getString(R.string.nzd), rateNow.nzd) +
                        String.format("%s:%10.5f\n", getString(R.string.thb), rateNow.thb) +
                        String.format("%s:%10.5f\n", getString(R.string.php), rateNow.php) +
                        String.format("%s:%10.5f\n", getString(R.string.vnd), rateNow.vnd) +
                        String.format("%s:%10.5f", getString(R.string.myr), rateNow.myr));

        AlertDialog dlg = myDlg.show();

        TextView textView = (TextView) dlg.findViewById(android.R.id.message);
        textView.setTypeface(Typeface.MONOSPACE);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
    }

    public void onRefreshRate(View v) {
        new ExchangeRateUpdateThread().start();
    }
}