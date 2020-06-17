package com.vpapps.item;

import android.graphics.Bitmap;

import java.io.Serializable;

public class ItemSong implements Serializable{

	private String id, catId, catName, artist, url, imageBig, imageSmall, title, duration, description, totalRate, averageRating="0", views, downloads, userRating="", tempName;
	private Bitmap image;
	private Boolean isSelected = false;

	public ItemSong(String id, String catId, String catName, String artist, String url, String imageBig, String imageSmall, String title, String Duration, String Description, String totalRate, String averageRating, String views, String downloads) {
		this.id = id;
		this.catId = catId;
		this.catName = catName;
		this.artist = artist;
		this.url = url;
		this.imageBig = imageBig;
		this.imageSmall = imageSmall;
		this.title = title;
		this.duration = Duration;
		this.description = Description;
		this.totalRate = totalRate;
		this.averageRating = averageRating;
		this.views = views;
		this.downloads = downloads;
	}

	public ItemSong(String id, String artist, String url, Bitmap image, String title, String Duration, String Description) {
		this.id = id;
		this.artist = artist;
		this.url = url;
		this.image = image;
		this.title = title;
		this.duration = Duration;
		this.description = Description;
	}


	public String getId() {
		return id;
	}
	
	public String getCatId() {
		return catId;
	}

	public String getArtist() {
		return artist;
	}
	
	public String getCatName() {
		return catName;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getImageBig() {
		return imageBig;
	}

	public String getImageSmall() {
		return imageSmall;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getDuration() {
		return duration;
	}
	
	public String getDescription() {
		return description;
	}

	public Bitmap getBitmap() {
		return image;
	}

	public String getTotalRate() {
		return totalRate;
	}

	public String getAverageRating() {
		return averageRating;
	}

	public String getViews() {
		return views;
	}

	public String getDownloads() {
		return downloads;
	}

	public String getUserRating() {
		return userRating;
	}

	public void setUserRating(String userRating) {
		this.userRating = userRating;
	}

	public void setAverageRating(String averageRating) {
		this.averageRating = averageRating;
	}

	public void setTotalRate(String totalRate) {
		this.totalRate = totalRate;
	}

	public Boolean getSelected() {
		return isSelected;
	}

	public void setSelected(Boolean selected) {
		isSelected = selected;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setImageBig(String imageBig) {
		this.imageBig = imageBig;
	}

	public void setImageSmall(String imageSmall) {
		this.imageSmall = imageSmall;
	}

	public void setTempName(String tempName) {
		this.tempName = tempName;
	}

	public String getTempName() {
		return tempName;
	}
}