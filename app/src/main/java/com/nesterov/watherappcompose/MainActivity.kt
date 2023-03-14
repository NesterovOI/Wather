package com.nesterov.watherappcompose

import android.app.DownloadManager.Request
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.nesterov.watherappcompose.data.WeatherModel
import com.nesterov.watherappcompose.screens.DialogSearch
import com.nesterov.watherappcompose.screens.MainCard
import com.nesterov.watherappcompose.screens.TabLayout
import com.nesterov.watherappcompose.ui.theme.WatherAppComposeTheme
import org.json.JSONObject
import java.lang.reflect.Method

const val API_KEY = "a1a3247ff43a46529b294539232502"
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WatherAppComposeTheme {
                val dayList = remember {
                    mutableStateOf(listOf<WeatherModel>())
                }
                val dialogState = remember {
                    mutableStateOf(false)
                }
                val currentDay = remember {
                    mutableStateOf(
                        WeatherModel(
                        "",
                        "",
                        "0.0",
                        "",
                        "",
                        "0.0",
                        "0.0",
                        ""
                    ))
                }
                if (dialogState.value){
                    DialogSearch(dialogState, onSubmit = {
                        getData(it, this, dayList, currentDay)
                    })
                }
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    getData("London", this, dayList, currentDay)
                    Image(
                        painter = painterResource(id = R.drawable.weather_bg),
                        contentDescription = "image1",
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.7f),
                        contentScale = ContentScale.FillBounds
                    )

                    Column {
                        MainCard(currentDay, onClickSync = {
                            getData("London", this@MainActivity, dayList, currentDay)
                        }, onClickSearch = {
                            dialogState.value = true
                        })
                        TabLayout(dayList, currentDay)
                    }

                }
            }
        }
    }
}

private fun getData(city: String, context: Context,
                    deyList: MutableState<List<WeatherModel>>, currentDay: MutableState<WeatherModel>){
    val url = "https://api.weatherapi.com/v1/forecast.json" +
            "?key=$API_KEY&" +
            "&q=$city" +
            "&days=3"+
            "&aqi=no"+
            "&alerts=no"

    val queue = Volley.newRequestQueue(context)
    val stringRequest = StringRequest(com.android.volley.Request.Method.GET, url,
        {
                response ->
            val list = getWeatherByDays(response)
            currentDay.value = list[0]
            deyList.value = list
        },
        {
                error -> Log.d("MyLog", "Error $error")
        })
    queue.add(stringRequest)
}

private fun getWeatherByDays(response: String): List<WeatherModel>{
    if (response.isEmpty()) return listOf()
    val list = ArrayList<WeatherModel>()
    val mainObject = JSONObject(response)
    val city = mainObject.getJSONObject("location").getString("name")
    val days = mainObject.getJSONObject("forecast").getJSONArray("forecastday")

    for (i in 0 until days.length()){
        val item = days[i] as JSONObject
        list.add(
            WeatherModel(
                city.toString(),
                item.getString("date"),
                "",
                item.getJSONObject("day").getJSONObject("condition").getString("text"),
                item.getJSONObject("day").getJSONObject("condition").getString("icon"),
                item.getJSONObject("day").getString("maxtemp_c"),
                item.getJSONObject("day").getString("mintemp_c"),
                item.getJSONArray("hour").toString()
            )
        )
    }
    list[0] = list[0].copy(
        time = mainObject.getJSONObject("current").getString("last_updated"),
        currentTemp = mainObject.getJSONObject("current").getString("temp_c")
    )
    return list
}