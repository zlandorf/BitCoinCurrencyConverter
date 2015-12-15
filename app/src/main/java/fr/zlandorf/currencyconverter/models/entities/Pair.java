package fr.zlandorf.currencyconverter.models.entities;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Pair implements Parcelable {
    private static final String SEPARATOR = "/";
    private Currency from;
    private Currency to;

    public Pair(Currency from, Currency to) {
        this.from = from;
        this.to = to;
    }

    protected Pair(Parcel in) {
        String[] data = new String[2];
        in.readStringArray(data);
        this.from = Currency.valueOf(data[0]);
        this.to = Currency.valueOf(data[1]);
    }

    public Currency getFrom() {
        return from;
    }

    public Currency getTo() {
        return to;
    }

    @Override
    public String toString() {
        return String.format("%s%s%s", from, SEPARATOR, to);
    }

    @Override
    public int hashCode() {
        return (from.getValue() + to.getValue()).hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Pair) {
            Pair other = (Pair) o;
            return from.equals(other.from) && to.equals(other.to);
        }
        return false;
    }

    public Pair invert() {
        return new Pair(to, from);
    }

    public static Pair valueOf(String pairAsString) {
        try {
            String[] split = pairAsString.split(SEPARATOR);
            if (split.length == 2) {
                return new Pair(
                    Currency.valueOf(split[0]),
                    Currency.valueOf(split[1])
                );
            }

        } catch (Exception e) {
            Log.e("Pair", "Failed to get Pair for the string value : " + pairAsString + " (" + e.getMessage() + ")");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{
            from.getValue(),
            to.getValue()
        });
    }

    public static final Creator<Pair> CREATOR = new Creator<Pair>() {
        @Override
        public Pair createFromParcel(Parcel in) {
            return new Pair(in);
        }

        @Override
        public Pair[] newArray(int size) {
            return new Pair[size];
        }
    };
}
