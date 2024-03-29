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

package com.tarekbohdima.bakingapp;

import android.app.Application;

import com.tarekbohdima.bakingapp.di.BakingComponent;
import com.tarekbohdima.bakingapp.di.ContextModule;
import com.tarekbohdima.bakingapp.di.DaggerBakingComponent;
import com.tarekbohdima.bakingapp.di.PreferencesModule;
import com.tarekbohdima.bakingapp.di.RetrofitModule;
import com.google.firebase.database.FirebaseDatabase;

import timber.log.Timber;

public class BaseApplication  extends Application {

    private BakingComponent bakingComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        bakingComponent = DaggerBakingComponent.builder()
                .contextModule(new ContextModule(this))
                .retrofitModule(new RetrofitModule())
                .preferencesModule(new PreferencesModule())
                .build();
    }

    public BakingComponent getBakingComponent() {
        return bakingComponent;
    }
}
