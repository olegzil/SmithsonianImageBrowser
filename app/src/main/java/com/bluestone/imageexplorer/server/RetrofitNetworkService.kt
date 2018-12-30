package com.bluestone.imageexplorer.server

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class RetrofitNetworkService(headerInitializer: NetworkServiceInitializer) {
    private var retrofit: Retrofit? = null
    private var api: NetworkApiInterface? = null
    private var httpClient = OkHttpClient.Builder()
    private val logging = HttpLoggingInterceptor()
    private var baseUrl: String

    init {
        //Minimum log output for http calls
        logging.level = HttpLoggingInterceptor.Level.NONE
        httpClient.addInterceptor(logging)
        baseUrl = headerInitializer.base
        httpClient
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                headerInitializer.headerValues?.forEach { headerData ->
                    request.addHeader(headerData.key, headerData.value)
                }
                chain.proceed(request.build())
            }.build()
    }

    private fun getRetrofit(): Retrofit? {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(httpClient.build())
                .build()
        }
        return retrofit
    }

    fun getApi(): NetworkApiInterface? {
        if (api == null) {
            api = getRetrofit()?.create<NetworkApiInterface>(NetworkApiInterface::class.java)
        }
        return api
    }
}