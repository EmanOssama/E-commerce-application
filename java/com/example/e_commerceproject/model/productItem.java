package com.example.e_commerceproject.model;

public class productItem {

   private String name;
   private String price;
   private int image;
   private int quantity;

    public productItem(String name, String price, int image, int quantity) {
        this.name = name;
        this.price = price;
        this.image = image;
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public int getImage() {
        return image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
