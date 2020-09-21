package com.clarabridge.core.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RetryIntervalDto implements Serializable {

    @SerializedName("regular")
    private int regular = 60;
    @SerializedName("aggressive")
    private int aggressive = 15;

    public int getRegular() {
        return regular;
    }

    public void setRegular(int regular) {
        this.regular = regular;
    }

    public int getAggressive() {
        return aggressive;
    }

    public void setAggressive(int aggressive) {
        this.aggressive = aggressive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RetryIntervalDto that = (RetryIntervalDto) o;

        if (regular != that.regular) {
            return false;
        }
        return aggressive == that.aggressive;
    }

    @Override
    public int hashCode() {
        int result = regular;
        result = 31 * result + aggressive;
        return result;
    }
}
