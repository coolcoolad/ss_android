package com.github.shadowsocks.utils

/**
 * Created by yajie on 3/23/2018.
 */

import android.os.Handler
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.Charset

object HttpUtil {
    fun httpJsonGet(url: String, params: Map<String, String>, headers: Map<String, String>): JSONObject {
        var url_ = ""
        val paraStr = StringBuilder()
        for (kv in params)
        {
            if (kv.value.isNotEmpty())
                paraStr.append(kv.key + "=" + kv.value + "&")
        }
        if (paraStr.isNotEmpty())
        {
            url_ = url + "?" + paraStr
        }

        val url = URL(url_)
        val http = url.openConnection() as HttpURLConnection
        http.connectTimeout = 5000//超时时间

        for ((key, value) in headers) {
            http.setRequestProperty(key, value)
        }


        //主角开始登场，不注意就是几个小时的调试，输入流
        val `in` = InputStreamReader(http.inputStream)

        val buffer = BufferedReader(`in`)
        var inputLine: String? = null
        //循环逐行读取输入流中的内容

        val result = StringBuilder()

        while (buffer.readLine().apply { inputLine = this } != null) {
            result.append(inputLine!! + "\n")
        }

        `in`.close()
        http.disconnect()
        return JSONObject(result.toString())
    }

    fun httpJsonPost(strUrlPath: String, data: String, headers: Map<String, String>): JSONObject {
        val data = data.toByteArray(Charset.forName("utf-8"))
        val url = URL(strUrlPath)

        val http = url.openConnection() as HttpURLConnection
        http.connectTimeout = 5000
        http.doInput = true
        http.doOutput = true
        http.requestMethod = "POST"
        http.useCaches = false//使用post方式不能用缓存

        for ((key, value) in headers) {
            http.setRequestProperty(key, value)
        }

        //设置请求体的类型是文本类型
        http.setRequestProperty("Content-Type", "application/json;charset=UTF-8")
        http.connect()
        //获得输出流，向服务器写入数据
        val out = http.outputStream
        out.write(data)
        out.flush()
        out.close()

        val response = http.responseCode
        if (response >= HttpURLConnection.HTTP_OK && response < HttpURLConnection.HTTP_BAD_REQUEST) {
            val inputStream = http.inputStream
            val res = dealResponseResult(inputStream)
            return JSONObject(res)
        }
        if (response == HttpURLConnection.HTTP_CONFLICT)
            throw UsernameConflictException()
        throw Exception("error code: "+response)
    }

    private fun dealResponseResult(inputStream: InputStream): String {
        var resultData: String? = null      //存储处理结果
        val byteArrayOutputStream = ByteArrayOutputStream()
        val data = ByteArray(1024)
        var len = 0
        try {
            while (inputStream.read(data).apply { len = this } != -1) {
                byteArrayOutputStream.write(data, 0, len)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        resultData = String(byteArrayOutputStream.toByteArray(), Charset.forName("utf-8"))
        return resultData
    }
}