package com.vpapps.item;

import java.io.Serializable;

public class ItemCat implements Serializable{
	
	private String id, name, image;

	public ItemCat(String id, String name, String image) {
		this.id = id;
		this.name = name;
		this.image = image;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getImage() {
		return image;
	}
}
