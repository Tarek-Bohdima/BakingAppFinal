/*
 * MIT License
 * Copyright (c) 2021.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * This project was submitted by Tarek Bohdima as part of the Android Developer
 * Nanodegree At Udacity.
 * As part of Udacity Honor code, your submissions must be your own work, hence
 * submitting this project as yours will cause you to break the Udacity Honor Code
 * and the suspension of your account.
 * I, the author of the project, allow you to check the code as a reference, but if you
 * submit it, it's your own responsibility if you get expelled.
 */

package com.example.bakingapp.ui.list;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bakingapp.IdlingResource.EspressoIdlingResource;
import com.example.bakingapp.R;
import com.example.bakingapp.databinding.ActivityRecipesListBinding;
import com.example.bakingapp.model.Recipes;
import com.example.bakingapp.repository.Preferences;
import com.example.bakingapp.ui.detail.RecipeDetailActivity;
import com.example.bakingapp.ui.detail.fragments.RecipeDetailFragment;
import com.example.bakingapp.ui.list.adapters.RecipesAdapter;
import com.example.bakingapp.ui.list.viewmodels.RecipeViewModel;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
 * An activity representing a list of Items. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link RecipeDetailActivity} representing
 * item details. On tablets.
 */
public class RecipesActivity extends AppCompatActivity implements RecipesAdapter.OnRecipeClickListener {

    @Inject
    Preferences preferences;
    private final List<Recipes> recipesList = new ArrayList<>();
    RecipesAdapter recipesAdapter;
    private ActivityRecipesListBinding activityItemListBinding;

    public static final String ANONYMOUS = "anonymous";
    private static final int RC_SIGN_IN = 1;

    private String username;
    private DatabaseReference recipesDatabaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityItemListBinding = DataBindingUtil.setContentView(this, R.layout.activity_recipes_list);
        activityItemListBinding.setLifecycleOwner(this);
        setSupportActionBar(activityItemListBinding.toolbar);
        activityItemListBinding.toolbar.setTitle(getTitle());
        RecipeViewModel recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);

        this.username = ANONYMOUS;

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        recipesDatabaseReference = firebaseDatabase.getReference("recipes");

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // user signed in
                    onSignedInInitialize(user.getDisplayName());
                } else {
                    // user signed out
                    onSignedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                                            new AuthUI.IdpConfig.EmailBuilder().build()))
                                    .setLogo(R.drawable.recipe_image_placeholder)
                                    .setTheme(R.style.Theme_BakingApp)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };

        setupRecyclerView();
        recipeViewModel.getRecipesList().observe(this,
                recipesData -> {
//                    recipesAdapter.setData(recipesData);
                    EspressoIdlingResource.decrement();
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        recipesDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                List<Recipes> recipesList1 = new ArrayList<Recipes>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    recipesList1.add(child.getValue(Recipes.class));
                }
                recipesAdapter.setData(recipesList1);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void onSignedInInitialize(String userName) {
        this.username = userName;
    }

    private void onSignedOutCleanup() {
        this.username = ANONYMOUS;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                // sign in was cancelled by the user, finish the activity
                Toast.makeText(this, "Sign in Cancelled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.sign_out_menu) {
            // sign out
            AuthUI.getInstance().signOut(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView() {
        EspressoIdlingResource.increment();
        recipesAdapter = new RecipesAdapter(this, recipesList);
        RecyclerView recyclerView = activityItemListBinding.includedLayout.itemList;
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(recipesAdapter);
    }

    @Override
    public void onRecipeClick(Recipes recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailFragment.CURRENT_RECIPE, recipe);
        startActivity(intent);
    }
}