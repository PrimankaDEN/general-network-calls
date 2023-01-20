package com.municorn.example

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.municorn.example.calls.api.DispatchersImpl
import com.municorn.example.calls.api.entity.errorOrNull
import com.municorn.example.calls.api.entity.payloadOrNull
import com.municorn.example.data.api.StatusApi
import com.municorn.example.data.database.StatusDatabase
import com.municorn.example.data.repo.ServerStatusApiCall
import com.municorn.example.data.repo.ServerStatusLocalStorage
import com.municorn.example.data.repo.ServerStatusMapper
import com.municorn.example.data.repo.StatusRepo
import com.municorn.example.data.retrofit.GeneralResultCallAdapterFactory
import com.squareup.moshi.Moshi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : AppCompatActivity() {
    private val gateway by lazy {
        val client = OkHttpClient.Builder().build()
        val moshi = Moshi.Builder().build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://esi.evetech.net/latest/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(GeneralResultCallAdapterFactory(moshi))
            .build()
        val room = Room.databaseBuilder(this, StatusDatabase::class.java, "database")
            .build()
        StatusRepo(
            DispatchersImpl(),
            ServerStatusApiCall(retrofit.create(StatusApi::class.java)),
            ServerStatusLocalStorage(room.statusDao()),
            ServerStatusMapper()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MainScope().launch {
            val result = gateway.getStatus("tranquility", force = true)
            Toast.makeText(
                this@MainActivity,
                "${result.payloadOrNull?.players ?: 0}",
                Toast.LENGTH_SHORT
            ).show()
            Log.d("Result", result.toString())
            result.errorOrNull?.printStackTrace()
        }
    }
}
