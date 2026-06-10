package com.example.crumbsofcomfort.User.Helper;

import android.content.Context;
import android.widget.Toast;

import com.example.crumbsofcomfort.User.Model.ItemsModel;
import com.example.crumbsofcomfort.R;

import java.util.ArrayList;

public class ManagementFav {
    private TinyDB tinyDB;
    private Context context;
    private String uid;

    public ManagementFav(Context context, String uid) {
        this.context = context;
        this.uid = uid;
        this.tinyDB = new TinyDB(context);
    }

    public void insertFav(ItemsModel item) {
        ArrayList<ItemsModel> favList = getFavList();
        boolean alreadyExists = false;

        for (ItemsModel favItem : favList) {
            if (favItem.getTitle().equals(item.getTitle())) {
                alreadyExists = true;
                break;
            }
        }

        if (!alreadyExists) {
            favList.add(item);
            tinyDB.putListObject("FavList_" + uid, favList);
            Toast.makeText(context, "Added to Favorites", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Already in Favorites", Toast.LENGTH_SHORT).show();
        }
    }

    public void removeFavByTitle(String title) {
        ArrayList<ItemsModel> favList = getFavList();
        for (int i = 0; i < favList.size(); i++) {
            if (favList.get(i).getTitle().equals(title)) {
                favList.remove(i);
                break;
            }
        }
        tinyDB.putListObject("FavList_" + uid, favList);
        Toast.makeText(context, "Removed from Favorites", Toast.LENGTH_SHORT).show();
    }

    public boolean isFavorite(String title) {
        for (ItemsModel item : getFavList()) {
            if (item.getTitle().equals(title)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<ItemsModel> getFavList() {
        return tinyDB.getListObject("FavList_" + uid);
    }

    public int heartIcon(String title) {
        return isFavorite(title) ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline;
    }
}
