package io.khashayar.test

import com.wrapper.spotify.SpotifyApi
import com.wrapper.spotify.SpotifyHttpManager
import io.khashayar.data.ProductsRedisRepository
import io.khashayar.domain.product.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.net.URI
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletionException

class ExtractionTests {
    private val ext = SpotifyBloc(ProductInteractor(ProductsRedisRepository(

    )))

    @Test
    fun run() {
        runBlocking {
            ext.initiate()
            ext.dispatch(Start())

            for (state in ext.states()) {
                print("\n\n$state\n\n")

                if(state is GotURL) {
                    print("\n\n${state.url}\n\n" )
                }
            }
        }
    }



}