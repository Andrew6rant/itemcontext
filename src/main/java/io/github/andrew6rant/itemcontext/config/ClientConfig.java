package io.github.andrew6rant.itemcontext.config;

public class ClientConfig extends MidnightConfig {
    @Comment public static Comment invert_description;
    @Entry public static boolean invert = false;
    @Entry public static int leftOffset = 0;
    @Entry public static int rightOffset = 0;
    @Entry public static boolean enableMainHand = true;
    @Entry public static boolean enableOffHand = true;
    @Entry public static boolean enableArmor = true;
    @Comment public static Comment fullCount_description;
    @Entry public static boolean fullCount = true;
    @Entry public static int MAX_TIME = 60;
    @Entry public static int ANIM_TIME = 5;
}
