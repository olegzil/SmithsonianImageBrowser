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
        logging.level = HttpLoggingInterceptor.Level.HEADERS
        httpClient.addInterceptor(logging)
        baseUrl = headerInitializer.base
        httpClient.addInterceptor { interceptor ->
            val original = interceptor.request()
            headerInitializer.headerValues?.forEach { headerData ->
                original.newBuilder().addHeader(headerData.key, headerData.value)
            }
            interceptor.proceed(original.newBuilder().build())
        }
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