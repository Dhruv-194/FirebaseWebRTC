package com.dhruv194.firebasewebrtc.di

import android.content.Context
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides //for providing application context to wherever needed
    fun provideContext(@ApplicationContext context: Context) : Context = context.applicationContext

    @Provides //returns a new Gson object
    fun provideGson():Gson = Gson()

    @Provides //returns the FirebaseDatabase instance to query db
    fun provideFirebaseDBInstance() : FirebaseDatabase = FirebaseDatabase.getInstance()

    @Provides //we will need the above firebaseDatabase to get a reference to our database
    fun providesFirebaseDBRef(db : FirebaseDatabase) : DatabaseReference = db.reference
}