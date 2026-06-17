package com.BookShop_Backend.configurations;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.seed")
public class SeedDataProperties {
    private boolean enabled = false;
    private boolean clearExisting = false;
    private int adminCount = 5;
    private int userCount = 1200;
    private int authorCount = 120;
    private int categoryCount = 30;
    private int publisherCount = 50;
    private int bookCount = 3500;
    private int orderCount = 2500;
    private int refreshTokenCount = 1000;
    private int maxCartItemsPerCart = 5;
    private int maxOrderItemsPerOrder = 4;
    private String adminPassword = "Admin@123";
    private String userPassword = "User@123";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isClearExisting() {
        return clearExisting;
    }

    public void setClearExisting(boolean clearExisting) {
        this.clearExisting = clearExisting;
    }

    public int getAdminCount() {
        return adminCount;
    }

    public void setAdminCount(int adminCount) {
        this.adminCount = adminCount;
    }

    public int getUserCount() {
        return userCount;
    }

    public void setUserCount(int userCount) {
        this.userCount = userCount;
    }

    public int getAuthorCount() {
        return authorCount;
    }

    public void setAuthorCount(int authorCount) {
        this.authorCount = authorCount;
    }

    public int getCategoryCount() {
        return categoryCount;
    }

    public void setCategoryCount(int categoryCount) {
        this.categoryCount = categoryCount;
    }

    public int getPublisherCount() {
        return publisherCount;
    }

    public void setPublisherCount(int publisherCount) {
        this.publisherCount = publisherCount;
    }

    public int getBookCount() {
        return bookCount;
    }

    public void setBookCount(int bookCount) {
        this.bookCount = bookCount;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public int getRefreshTokenCount() {
        return refreshTokenCount;
    }

    public void setRefreshTokenCount(int refreshTokenCount) {
        this.refreshTokenCount = refreshTokenCount;
    }

    public int getMaxCartItemsPerCart() {
        return maxCartItemsPerCart;
    }

    public void setMaxCartItemsPerCart(int maxCartItemsPerCart) {
        this.maxCartItemsPerCart = maxCartItemsPerCart;
    }

    public int getMaxOrderItemsPerOrder() {
        return maxOrderItemsPerOrder;
    }

    public void setMaxOrderItemsPerOrder(int maxOrderItemsPerOrder) {
        this.maxOrderItemsPerOrder = maxOrderItemsPerOrder;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }
}
