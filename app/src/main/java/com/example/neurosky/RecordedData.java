package com.example.neurosky;

import android.util.Log;

public class RecordedData {
    private String time = null;
    private String delta = null;
    private String theta = null;
    private String lowAlpha = null;
    private String highAlpha = null;
    private String lowBeta = null;
    private String highBeta = null;
    private String lowGamma = null;
    private String highGamma = null;
    private String dominates = null;
    private String state = "null";

    public void setTime(int time) {
        int hour = time / 3600;
        int minute = (time - (3600 * hour)) / 60;
        int second = time - (3600 * hour) - (60 * minute);

        if (hour < 10) this.time = "0" + hour + ":";
        else this.time = hour + ":";

        if (minute < 10) this.time += "0" + minute + ":";
        else this.time += minute + ":";

        if (second < 10) this.time += "0" + second;
        else this.time += "" + second;
    }

    public String getTime() {
        return time;
    }

    public void setDelta(int delta) {
        this.delta = Integer.toString(delta);
    }

    public String getDelta() {
        return delta;
    }

    public void setTheta(int theta) {
        this.theta = Integer.toString(theta);
    }

    public String getTheta() {
        return theta;
    }

    public void setLowAlpha(int lowAlpha) {
        this.lowAlpha = Integer.toString(lowAlpha);
    }

    public String getLowAlpha() {
        return lowAlpha;
    }

    public void setHighAlpha(int highAlpha) {
        this.highAlpha = Integer.toString(highAlpha);
    }

    public String getHighAlpha() {
        return highAlpha;
    }

    public void setLowBeta(int lowBeta) {
        this.lowBeta = Integer.toString(lowBeta);
    }

    public String getLowBeta() {
        return lowBeta;
    }

    public void setHighBeta(int highBeta) {
        this.highBeta = Integer.toString(highBeta);
    }

    public String getHighBeta() {
        return highBeta;
    }

    public void setLowGamma(int lowGamma) {
        this.lowGamma = Integer.toString(lowGamma);
    }

    public String getLowGamma() {
        return lowGamma;
    }

    public void setHighGamma(int highGamma) {
        this.highGamma = Integer.toString(highGamma);
    }

    public String getHighGamma() {
        return highGamma;
    }

    public String getDominates() {
        return dominates;
    }

    public void setDominates() {
        dominates();
    }

    public void setState(String strDom) {
        State(strDom);
    }

    public String getState() {
        return state;
    }

    private void dominates() {
        int max = 0;
        int deltaInt = 0;
        int thetaInt = 0;
        int AlphaInt = 0;
        int BetaInt = 0;
        int GammaInt = 0;

        try {
             deltaInt = Integer.parseInt(delta);
             thetaInt = Integer.parseInt(theta);
             AlphaInt = Integer.parseInt(lowAlpha) + Integer.parseInt(highAlpha);
             BetaInt = Integer.parseInt(lowBeta) + Integer.parseInt(highBeta);
             GammaInt = Integer.parseInt(lowGamma) + Integer.parseInt(highGamma);
        } catch (NumberFormatException e) {
            Log.e("RecordedData", e.getMessage());
        }

        if(deltaInt > max) max = deltaInt;
        if(thetaInt > max) max = thetaInt;
        if(AlphaInt > max) max = AlphaInt;
        if(BetaInt > max) max = BetaInt;
        if(GammaInt > max) max = GammaInt;

        if (max == deltaInt) dominates = "Delta";
        else if (max == thetaInt) dominates = "Theta";
        else if (max == AlphaInt) dominates = "Alpha";
        else if (max == BetaInt) dominates = "Beta";
        else if (max == GammaInt) dominates = "Gamma";
    }

    private void State(String strDom) {
        dominates();

        if (dominates == "Beta" || dominates == "Alpha" || dominates == "Gamma") {
            state = "Бодрствование";
        } else if ((dominates == "Theta" || dominates == "Delta") &&
                (((strDom != null) && (strDom == "Theta" || strDom == "Delta"))))
            state = "Сон";
        else
            state = "Бодрствование";

    }
}

