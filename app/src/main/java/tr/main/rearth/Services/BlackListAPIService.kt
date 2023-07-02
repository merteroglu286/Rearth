package tr.main.rearth.Services

import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class BlackListAPIService {

    //https://raw.githubusercontent.com/ooguz/turkce-kufur-karaliste/master/karaliste.json
    //BASE_URL -> https://raw.githubusercontent.com/
    //EXT -> ooguz/turkce-kufur-karaliste/master/karaliste.json

    private val BASE_URL = "https://raw.githubusercontent.com/"
    private val api = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(BlackListAPI::class.java)

    fun getData() : Single<List<String>>{
        return api.getBlackList()
    }
}