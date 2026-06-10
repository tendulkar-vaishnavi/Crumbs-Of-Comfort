package com.example.crumbsofcomfort.User.ViewModel;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.crumbsofcomfort.User.Model.CategoryModel;
import com.example.crumbsofcomfort.User.Model.ItemsModel;
import com.example.crumbsofcomfort.User.Model.ShopModel;
import com.example.crumbsofcomfort.User.Model.SliderModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends ViewModel {

    private FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();

    private MutableLiveData<List<SliderModel>> _slider = new MutableLiveData<>();
    private MutableLiveData<List<CategoryModel>> _category = new MutableLiveData<>();
    private MutableLiveData<List<ItemsModel>> _bestSeller = new MutableLiveData<>();
    private MutableLiveData<List<ShopModel>> _shop = new MutableLiveData<>();


    public LiveData<List<CategoryModel>> getCategory(){
        return _category;
    }
    public LiveData<List<ItemsModel>> getBestSeller(){
        return _bestSeller;
    }
    public LiveData<List<SliderModel>> getSlider(){
        return _slider;
    }

    public LiveData<List<ShopModel>> getShop() {
        return _shop;
    }

    public void loadSlider(){
        DatabaseReference ref = firebaseDatabase.getReference("Banner");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<SliderModel> lists = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()){
                    SliderModel list= childSnapshot.getValue(SliderModel.class);
                    if(list!=null){
                        lists.add(list);
                        _slider.setValue(lists);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void loadCategory(){
        DatabaseReference reference = firebaseDatabase.getReference("Category");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<CategoryModel> lists= new ArrayList<>();
                for(DataSnapshot childSnapshot:snapshot.getChildren()){
                    CategoryModel list=childSnapshot.getValue(CategoryModel.class);
                    if(list!=null){
                        lists.add(list);
                    }
                }
                _category.setValue(lists);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void loadBestSeller() {
        DatabaseReference reference = firebaseDatabase.getReference("Items");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ItemsModel> lists = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    ItemsModel item = childSnapshot.getValue(ItemsModel.class);
                    if (item != null) {
                        String sellerName = childSnapshot.child("sellerName").getValue(String.class);
                        String sellerPic = childSnapshot.child("sellerPic").getValue(String.class);
                        Long sellerTellLong = childSnapshot.child("sellerTell").getValue(Long.class);
                        String shopId = childSnapshot.child("shopId").getValue(String.class);

                        if (sellerName != null) item.setSellerName(sellerName);
                        if (sellerPic != null) item.setSellerPic(sellerPic);
                        if (sellerTellLong != null) item.setSellerTell(sellerTellLong.intValue());
                        if (shopId != null) item.setShopId(shopId);

                        lists.add(item);
                    }
                }

                // ✅ Ensure correct type
                _bestSeller.setValue(new ArrayList<>(lists));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FIREBASE_ERROR", "Failed to load best sellers: " + error.getMessage());
            }
        });
    }


    public void loadShop() {
        DatabaseReference reference = firebaseDatabase.getReference("Shops");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ShopModel> lists = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    if (childSnapshot.exists()) {
                        ShopModel shop = childSnapshot.getValue(ShopModel.class);
                        if (shop != null) {
                            String shopId = childSnapshot.getKey();
                            shop.setShopId(shopId);

                            List<ItemsModel> itemList = new ArrayList<>();
                            DataSnapshot itemsSnapshot = childSnapshot.child("items");
                            for (DataSnapshot itemSnap : itemsSnapshot.getChildren()) {
                                ItemsModel item = itemSnap.getValue(ItemsModel.class);
                                if (item != null) {
                                    item.setShopId(shopId);
                                    itemList.add(item);
                                }
                            }

                            shop.setItems((ArrayList<ItemsModel>) itemList);
                            lists.add(shop);
                        }
                    }
                }
                _shop.setValue(lists);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
