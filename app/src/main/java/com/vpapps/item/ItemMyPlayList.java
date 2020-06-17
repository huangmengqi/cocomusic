package com.vpapps.item;

import java.io.Serializable;
import java.util.ArrayList;

public class ItemMyPlayList implements Serializable{

	private String id, name;
	private ArrayList<String> arrayListUrl;

	public ItemMyPlayList(String id, String name, ArrayList<String> arrayListUrl) {
		this.id = id;
		this.name = name;
		this.arrayListUrl = arrayListUrl;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public ArrayList<String> getArrayListUrl() {
		return arrayListUrl;
	}
}
