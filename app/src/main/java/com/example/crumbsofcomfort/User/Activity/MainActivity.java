package com.example.crumbsofcomfort.User.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;

import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.example.crumbsofcomfort.Delivery.Activity.SignUpDelivery;
import com.example.crumbsofcomfort.databinding.DrawerHeaderBinding;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;


import com.example.crumbsofcomfort.R;
import com.example.crumbsofcomfort.User.Adapter.BestSellerAdapter;
import com.example.crumbsofcomfort.User.Adapter.CategoryAdapter;
import com.example.crumbsofcomfort.User.Adapter.SearchAdapter;
import com.example.crumbsofcomfort.User.Adapter.ShopAdapter;
import com.example.crumbsofcomfort.User.Adapter.SliderAdapter;
import com.example.crumbsofcomfort.User.Helper.ManagementFav;
import com.example.crumbsofcomfort.User.Helper.ManagementShop;
import com.example.crumbsofcomfort.User.Helper.ManagmentCart;
import com.example.crumbsofcomfort.User.Model.ItemsModel;
import com.example.crumbsofcomfort.User.Model.SearchModel;
import com.example.crumbsofcomfort.User.Model.ShopModel;
import com.example.crumbsofcomfort.User.Model.SliderModel;
import com.example.crumbsofcomfort.User.ViewModel.MainViewModel;
import com.example.crumbsofcomfort.Vendor.Activity.VendorSignUp;
import com.example.crumbsofcomfort.databinding.ActivityMainBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements BestSellerAdapter.AuthCheckListener {

    private ActivityMainBinding binding;
    private MainViewModel viewModel =new MainViewModel();
    private List<ItemsModel> allItems = new ArrayList<>();
    private List<ShopModel> allShops = new ArrayList<>();
    private List<SearchModel> searchResults = new ArrayList<>();
    private SearchAdapter searchAdapter;
    private ActivityResultLauncher<Intent> detailLauncher;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding=ActivityMainBinding.inflate(getLayoutInflater());
        Window window = getWindow();
        ViewCompat.setOnApplyWindowInsetsListener(getWindow().getDecorView(), (v, insets) -> {
            return insets;
        });
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNav, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.bottomMargin = systemBars.bottom + 16;
            v.setLayoutParams(params);
            return insets;
        });

        SharedPreferences prefs = getSharedPreferences("introPrefs", MODE_PRIVATE);
        boolean isIntroShown = prefs.getBoolean("introShown", false);

        if (!isIntroShown) {
            startActivity(new Intent(this, IntroActivity.class));
            finish();
            return;
        }

        drawerLayout = binding.drawerLayout;
        navigationView = binding.navigationView;
        binding.imgMenu.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(MainActivity.this, OrderHistoryActivity.class));
            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
        View headerView = binding.navigationView.getHeaderView(0);
        DrawerHeaderBinding headerBinding = DrawerHeaderBinding.bind(headerView);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            headerBinding.navHeaderName.setText("Welcome, " + name);

            if (user.getPhotoUrl() != null) {
                headerBinding.tvInitials.setVisibility(View.GONE);
                Glide.with(this).load(user.getPhotoUrl()).into(headerBinding.profileImage);
            } else {
                if (name != null && !name.isEmpty()) {
                    String[] parts = name.trim().split(" ");
                    String initials = parts.length >= 2
                            ? ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase()
                            : ("" + name.charAt(0)).toUpperCase();

                    headerBinding.tvInitials.setText(initials);
                    headerBinding.tvInitials.setVisibility(View.VISIBLE);
                }
            }
        }
        detailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    initBestSeller();
                }
        );
        initBanners();
        initCategory();
        initBestSeller();
        initShop();

        searchAdapter = new SearchAdapter(searchResults);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(searchAdapter);

        binding.searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchResults.clear();
                String query = s.toString().toLowerCase();

                if (query.isEmpty()) {
                    binding.recyclerView.setVisibility(View.GONE);
                } else {
                    if (allItems != null) {
                        for (ItemsModel item : allItems) {
                            if (item != null && item.getTitle() != null && item.getTitle().toLowerCase().contains(query)) {
                                searchResults.add(new SearchModel(item));
                            }
                        }
                    }

                    if (allShops != null) {
                        for (ShopModel shop : allShops) {
                            if (shop != null && shop.getSellerName() != null &&
                                    shop.getSellerName().toLowerCase().contains(query)) {
                                searchResults.add(new SearchModel(shop));
                            }
                        }
                    }

                    binding.recyclerView.setVisibility(View.VISIBLE);
                    searchAdapter.notifyDataSetChanged();
                }
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        loadAllDataForSearch();

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        View decor=window.getDecorView();
        decor.setSystemUiVisibility(0);

        if (user != null) {
            String name = user.getDisplayName();
            binding.textWelcomeName.setText(name);
        }
        bottomNavigation();
        if (currentUser != null) {
            binding.imgMenu.setVisibility(View.VISIBLE);
            binding.textChooseRole.setVisibility(View.GONE);

        } else {
            binding.imgMenu.setVisibility(View.GONE);
            binding.textChooseRole.setVisibility(View.VISIBLE);
        }



        binding.textChooseRole.setOnClickListener(v -> {
            View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_choose_role, null);
            BottomSheetDialog dialog = new BottomSheetDialog(this);
            dialog.setContentView(view);
            dialog.show();

            view.findViewById(R.id.optionUser).setOnClickListener(opt -> {
                dialog.dismiss();
                startActivity(new Intent(this, SignupActivity.class));
            });

            view.findViewById(R.id.optionVendor).setOnClickListener(opt -> {
                dialog.dismiss();
                startActivity(new Intent(this, VendorSignUp.class));
            });

            view.findViewById(R.id.optionDelivery).setOnClickListener(opt -> {
                dialog.dismiss();
                startActivity(new Intent(this, SignUpDelivery.class));
            });
        });

    }
    private void bottomNavigation(){
        binding.linearCartLayout.setOnClickListener(view -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                String uid = user.getUid();
                ManagmentCart managmentCart = new ManagmentCart(MainActivity.this, uid);
                if (!managmentCart.getListCart().isEmpty()) {
                    startActivity(new Intent(MainActivity.this, CartActivity.class));
                } else {
                    showNotLoggedInSheet("Empty Cart", "Your Cart is Empty", false);
                }
            } else {
                showNotLoggedInSheet("Not logged In","Please login to view cart");
            }
        });

        binding.linearFavoriteLayout.setOnClickListener(view -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                String uid = user.getUid();
                ManagementFav managementFav = new ManagementFav(MainActivity.this, uid);
                if (!managementFav.getFavList().isEmpty()) {
                    startActivity(new Intent(MainActivity.this, FavouriteActivity.class));
                } else {
                    showNotLoggedInSheet("Empty Favourites", "Your Favourites List is Empty", false);
                }
            } else {
                showNotLoggedInSheet("Not Logged In","Please login to view favorites");
            }
        });

        binding.linearOrderLayout.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                startActivity(new Intent(MainActivity.this, MyOrderActivity.class));
            } else {
                showNotLoggedInSheet("Not Logged In","Please login to view orders");
            }
        });

        binding.linearShopLayout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ShopListActivity.class);
            startActivity(intent);
        });
    }

    private void initCategory() {
        viewModel.getCategory().observe(this,items->{
            binding.recyclerviewCategory.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
            binding.recyclerviewCategory.setAdapter(new CategoryAdapter(items));
        });
        viewModel.loadCategory();
    }

    private void initBestSeller() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user != null ? user.getUid() : null;

        viewModel.getBestSeller().observe(this, items -> {
            binding.recyclerviewBestSeller.setLayoutManager(new GridLayoutManager(this, 2));
            binding.recyclerviewBestSeller.setAdapter(new BestSellerAdapter(items, detailLauncher, uid,this));
        });

        viewModel.loadBestSeller();
    }


    private void initShop() {
        viewModel.getShop().observe(this, items -> {
            binding.recyclerviewShops.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            binding.recyclerviewShops.setAdapter(new ShopAdapter(items));

            ManagementShop managementShop = new ManagementShop(MainActivity.this);
            managementShop.insertShopList(new ArrayList<>(items));
        });
        viewModel.loadShop();
    }



    private void initBanners() {
        binding.progressCircular.setVisibility(View.VISIBLE);
        viewModel.getSlider().observe(this,banners->{
            setupBanners(banners);
            binding.progressCircular.setVisibility(View.GONE);
        });
        viewModel.loadSlider();
    }

    private void setupBanners(List<SliderModel> images) {
        SliderAdapter.SliderClickListener bannerClickListener = (position, model) -> {
            if (position == 1) {
                Intent intent = new Intent(MainActivity.this, CustomizationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Clicked banner at position " + position, Toast.LENGTH_SHORT).show();
            }
        };

        binding.viewPager2.setAdapter(new SliderAdapter(images, binding.viewPager2, bannerClickListener));
        binding.viewPager2.setClipToPadding(false);
        binding.viewPager2.setClipChildren(false);
        binding.viewPager2.setOffscreenPageLimit(3);
        binding.viewPager2.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(40));
        binding.viewPager2.setPageTransformer(transformer);

        if (images.size() > 1) {
            binding.dotsIndicator.setVisibility(View.VISIBLE);
            binding.dotsIndicator.attachTo(binding.viewPager2);
        }
    }
    private void loadAllDataForSearch() {
        ManagementShop managementShop = new ManagementShop(this);
        allShops = managementShop.getShopList();

        allItems.clear();
        if (allShops != null) {
            for (ShopModel shop : allShops) {
                if (shop.getItems() != null) {
                    for (ItemsModel item : shop.getItems()) {
                        item.setSellerName(shop.getSellerName());
                        item.setSellerPic(shop.getSellerPic());
                        item.setSellerTell(shop.getSellerTell());
                        allItems.add(item);
                    }
                } else {
                    Log.w("MainActivity", "Shop " + shop.getSellerName() + " has null item list");
                }
            }
        } else {
            Log.e("MainActivity", "Shop list is null from TinyDB");
        }
    }

    @Override
    public void showNotLoggedInSheet(String title, String msg) {
        // Default: redirect to login
        showNotLoggedInSheet(title, msg, true);
    }
    public void showNotLoggedInSheet(String title, String msg, boolean shouldGoToLogin) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_validation, null);
        dialog.setContentView(view);

        TextView titleText = view.findViewById(R.id.textTitle);
        TextView msgText = view.findViewById(R.id.textMessage);
        AppCompatButton loginButton = view.findViewById(R.id.btnOk);

        titleText.setText(title);
        msgText.setText(msg);

        loginButton.setOnClickListener(v -> {
            dialog.dismiss();
            if (shouldGoToLogin) {
                startActivity(new Intent(this, LoginActivity.class));
            }
        });

        dialog.show();
    }



}

