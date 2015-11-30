package org.excelsi.nausicaa.ca;


import java.util.Random;


public class HueTransform implements Transform {
    private final Random _rand;


    public HueTransform(Random rand) {
        _rand = rand;
    }

    public String name() { return "Hue"; }

    public CA transform(CA c) {
        if(_rand.nextInt(100)<=20) {
            return c.palette(Palette.random(c.getPalette().getColorCount(), _rand));
        }
        else {
            return c.palette(c.getPalette().replace(_rand.nextInt(c.getPalette().getColorCount()),
                Colors.randomColor(_rand)));
        }
    }
}
