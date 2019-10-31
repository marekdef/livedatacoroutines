package pl.mobilization.livedataarchitecture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import timber.log.Timber
import java.lang.Exception
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*
import kotlin.coroutines.CoroutineContext

class MainActivityViewModel : ViewModel() {
    private val service: HelloService

    init {

        val trustManager = object:  X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                Timber.d("checkClientTrusted(%s, %s)", chain?.map { it.serialNumber }?.joinToString(), authType)
            }

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                Timber.d("checkServerTrusted(%s, %s)", chain?.map { it.serialNumber }?.joinToString(), authType)
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()

        }

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, arrayOf(trustManager), SecureRandom())

        val sslSocketFactory = sslContext.socketFactory

        val hostnameVerifier = HostnameVerifier { hostname, session ->
            Timber.d("verify(%s,%s)", hostname, session.id)
            true }

        val client =
        OkHttpClient().newBuilder()
            .also {
                if (BuildConfig.DEBUG) {
                    it.addInterceptor(HttpLoggingInterceptor().also {
                        it.level = HttpLoggingInterceptor.Level.BODY
                    })
                    .sslSocketFactory(sslSocketFactory, trustManager)
                    .hostnameVerifier(hostnameVerifier)
                }
            }

            .build()

        val retrofit = Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
//            .baseUrl("https://192.168.45.199:8443/")
            .baseUrl("https://192.168.100.3:8443/")
            .client(client)
            .build()

        service = retrofit.create(HelloService::class.java)
    }

    fun connect() = liveData  {
        Timber.d("emitting loading %s", Thread.currentThread().name)
        emit(Resource.loading())
        viewModelScope.launch {
            try {
                Timber.d("connecting service %s", Thread.currentThread().name)
                val hello = service.hello("marekdef")
                Timber.d("emitting success %s", Thread.currentThread().name)
                emit(Resource.success("hello"))
            } catch (e: Exception) {
                Timber.w(e)
                emit(Resource.error(e.message ?: "Unknown error"))
            }
        }
    }

}