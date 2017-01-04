package com.luzharif.smarteye.model;

/**
 * Created by LuZharif on 23/04/2016.
 */
public class Shots {
    private int _id;
    private String _nameshot;
    private String _namefruit;
    private int _fruitquality;
    private String _imageFruit;

    public Shots() {
    }

    public Shots(int id, String nameshot, String namefruit, int fruitquality, String imageFruit) {
        this._id = id;
        this._nameshot = nameshot;
        this._namefruit = namefruit;
        this._fruitquality = fruitquality;
        this._imageFruit = imageFruit;
    }

    public Shots(String nameshot, String namefruit, int fruitquality, String imageFruit) {
        this._nameshot = nameshot;
        this._namefruit = namefruit;
        this._fruitquality = fruitquality;
        this._imageFruit = imageFruit;
    }

    // getting ID
    public int getID(){
        return this._id;
    }

    // setting id
    public void setID(int id){
        this._id = id;
    }

    public String getNameShot() {
        return this._nameshot;
    }

    public String getNameFruit() {
        return this._namefruit;
    }

    public int getFruitQuality() {
        return this._fruitquality;
    }

    public String getImageFruit() {
        return this._imageFruit;
    }

    public void setNameShot(String nameShot) {
        this._nameshot = nameShot;
    }

    public void setNameFruit(String nameFruit){
        this._namefruit = nameFruit;
    }

    public void setFruitQuality(int fruitQuality){
        this._fruitquality = fruitQuality;
    }

    public void setImageFruit(String imageFruit){
        this._imageFruit = imageFruit;
    }
}
