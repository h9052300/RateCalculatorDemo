package com.aaronlife.ratecalculator.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.aaronlife.ratecalculator.R;
import com.aaronlife.ratecalculator.util.RateData;

public class ResultAdapter extends BaseAdapter
{
    private LayoutInflater layoutInflater;

    private int[] rates = { R.string.usd,
                            R.string.eur,
                            R.string.jpy,
                            R.string.krw,
                            R.string.hkd,
                            R.string.twd,
                            R.string.idr,
                            R.string.cny,

                            R.string.gbp,
                            R.string.aud,
                            R.string.cad,
                            R.string.sgd,
                            R.string.chf,
                            R.string.zar,
                            R.string.sek,
                            R.string.nzd,
                            R.string.thb,
                            R.string.php,
                            R.string.vnd,
                            R.string.myr};

    private double total;
    private RateData rateNow;

    public ResultAdapter(Context context, RateData rateNow, double total)
    {
        layoutInflater = LayoutInflater.from(context);
        update(rateNow, total);
    }

    @Override
    public int getCount()
    {
        return rates.length;
    }

    @Override
    public Integer getItem(int position)
    {
        return rates[position];
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        convertView = layoutInflater.inflate(R.layout.listview_exchange,
                                             parent, false);

        final Button btnName = (Button)convertView.findViewById(R.id.btn_name);
        btnName.setText(getItem(position));
        btnName.setLongClickable(true);

        final TextView txtResult = (TextView)convertView.findViewById(R.id.txt_result);

        if(position % 2 == 1)
        {
            btnName.setBackgroundColor(Color.rgb(0xe0, 0xe0, 0xe0));
            txtResult.setBackgroundColor(Color.rgb(0xe0, 0xe0, 0xe0));
        }

        switch(getItem(position))
        {
        case R.string.usd:
            Log.d("aarontest", "now: " + rateNow.usd);
            txtResult.setText(String.format("%.2f", 1 / rateNow.usd * total));
            btnName.setTag(RateData.USD);
            break;
        case R.string.eur:
            txtResult.setText(String.format("%.2f", 1 / rateNow.eur * total));
            btnName.setTag(RateData.EUR);
            break;
        case R.string.jpy:
            txtResult.setText(String.format("%.2f", 1 / rateNow.jpy * total));
            btnName.setTag(RateData.JPY);
            break;
        case R.string.krw:
            txtResult.setText(String.format("%.2f", 1 / rateNow.krw * total));
            btnName.setTag(RateData.KRW);
            break;

        case R.string.hkd:
            txtResult.setText(String.format("%.2f", 1 / rateNow.hkd * total));
            btnName.setTag(RateData.HKD);
            break;
        case R.string.twd:
            txtResult.setText(String.format("%.2f", 1 / rateNow.twd * total));
            btnName.setTag(RateData.TWD);
            break;
        case R.string.idr:
            txtResult.setText(String.format("%.2f", 1 / rateNow.idr * total));
            btnName.setTag(RateData.IDR);
            break;
        case R.string.cny:
            txtResult.setText(String.format("%.2f", 1 / rateNow.cny * total));
            btnName.setTag(RateData.CNY);
            break;

        case R.string.gbp:
            txtResult.setText(String.format("%.2f", 1 / rateNow.gbp * total));
            btnName.setTag(RateData.GBP);
            break;
        case R.string.aud:
            txtResult.setText(String.format("%.2f", 1 / rateNow.aud * total));
            btnName.setTag(RateData.AUD);
            break;
        case R.string.cad:
            txtResult.setText(String.format("%.2f", 1 / rateNow.cad * total));
            btnName.setTag(RateData.CAD);
            break;
        case R.string.sgd:
            txtResult.setText(String.format("%.2f", 1 / rateNow.sgd * total));
            btnName.setTag(RateData.SGD);
            break;
        case R.string.chf:
            txtResult.setText(String.format("%.2f", 1 / rateNow.chf * total));
            btnName.setTag(RateData.CHF);
            break;
        case R.string.zar:
            txtResult.setText(String.format("%.2f", 1 / rateNow.zar * total));
            btnName.setTag(RateData.ZAR);
            break;
        case R.string.sek:
            txtResult.setText(String.format("%.2f", 1 / rateNow.sek * total));
            btnName.setTag(RateData.SEK);
            break;
        case R.string.nzd:
            txtResult.setText(String.format("%.2f", 1 / rateNow.nzd * total));
            btnName.setTag(RateData.NZD);
            break;
        case R.string.thb:
            txtResult.setText(String.format("%.2f", 1 / rateNow.thb * total));
            btnName.setTag(RateData.THB);
            break;
        case R.string.php:
            txtResult.setText(String.format("%.2f", 1 / rateNow.php * total));
            btnName.setTag(RateData.PHP);
            break;
        case R.string.vnd:
            txtResult.setText(String.format("%.2f", 1 / rateNow.vnd * total));
            btnName.setTag(RateData.VND);
            break;
        case R.string.myr:
            txtResult.setText(String.format("%.2f", 1 / rateNow.myr * total));
            btnName.setTag(RateData.MYR);
            break;
        }

        return convertView;
    }

    public void update(RateData rateNow, double total)
    {
        this.rateNow = rateNow;
        this.total = total;
    }
}
