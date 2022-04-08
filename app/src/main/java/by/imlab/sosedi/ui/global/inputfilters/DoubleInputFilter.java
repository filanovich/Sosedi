package by.imlab.sosedi.ui.global.inputfilters;

import android.text.InputFilter;
import android.text.Spanned;

public class DoubleInputFilter implements InputFilter {

    private double mMin;
    private double mMax;
    private int mDecimals;

    public DoubleInputFilter(double min, double max, int decimals) {
        this.mMin = min;
        this.mMax = max;
        this.mDecimals = decimals;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

        int dotPos = -1;
        int len = dest.length();
        for (int i = 0; i < len; i++) {
            char c = dest.charAt(i);
            if (c == '.' || c == ',') {
                dotPos = i;
                break;
            }
        }
        if (dotPos >= 0) {
            // protects against many dots
            if (source.equals(".") || source.equals(",")) {
                return "";
            }
            // if the text is entered before the dot
            if (dend <= dotPos) {
                return null;
            }
            if (len - dotPos > mDecimals) {
                return "";
            }
        }

        try {
            String s = dest.toString() + source.toString();
            double input = Double.parseDouble(s);
            if (!isInRange(mMin, mMax, input))
                return "";
        } catch (NumberFormatException nfe) { }

        return null;
    }

    private boolean isInRange(double min, double max, double val) {
        return max > min ? val >= min && val <= max : val >= max && val <= min;
    }
}