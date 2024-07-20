package io.github.sugarmgp.eos.util;

import java.util.Random;

public enum EnumFriendMembers {
    Mo_Ink,
    zijing_233,
    chihuoQwQ,
    jujixiguan(true),
    SEALchanPS(true),
    Tank1014,
    Wubaozi123,
    xtexChooser,
    CalciumSilicate(true),
    Ping_yuGTS,
    Ultrahiperism,
    MoonBigD,
    bilicapr(true),
    kid_sui;

    private final boolean slim;

    EnumFriendMembers() {
        this.slim = false;
    }

    EnumFriendMembers(boolean slimIn) {
        this.slim = slimIn;
    }

    public static EnumFriendMembers getByKey(int key) {
        return EnumFriendMembers.values()[key];
    }

    public static int randomGetKey(Random rand) {
        return rand.nextInt(EnumFriendMembers.values().length);
    }

    public int getKey() {
        return this.ordinal();
    }

    public String getId() {
        return this.name().toLowerCase();
    }

    public boolean getSlim() {
        return this.slim;
    }
}
