package tr.main.rearth.Services

import io.reactivex.Single
import retrofit2.http.GET

interface BlackListAPI {

    //https://raw.githubusercontent.com/ooguz/turkce-kufur-karaliste/master/karaliste.json
    //BASE_URL -> https://raw.githubusercontent.com/
    //EXT -> ooguz/turkce-kufur-karaliste/master/karaliste.json
    @GET("ooguz/turkce-kufur-karaliste/master/karaliste.json")
    fun getBlackList():Single<List<String>>

}