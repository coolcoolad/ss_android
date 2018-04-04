package com.github.shadowsocks

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.shadowsocks.model.User
import com.github.shadowsocks.utils.HttpUtil
import com.github.shadowsocks.utils.SharedPrefUtils
import com.github.shadowsocks.utils.UsernameConflictException
import com.github.shadowsocks.utils.Util
import okhttp3.Credentials
import org.json.JSONObject
import java.util.*
import kotlin.math.sign

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
    }

    fun onSignUpBtn(view: View) {
        val usernameET = findViewById<EditText>(R.id.username_sign_up)
        val passwordET = findViewById<EditText>(R.id.password_sign_up)
        val contactsET = findViewById<EditText>(R.id.contacts)
        val username = usernameET.text.toString()
        val password = passwordET.text.toString()
        val contacts = contactsET.text.toString()
        val thread = Thread(Runnable { signUp(username, password, contacts) })
        thread.start()
    }

    private fun signUp(username: String, password: String, contacts: String) {
        val bundle = Bundle()
        try {
            val json = JSONObject()
            json.put("access_token", getString(R.string.master_key))
            json.put("username", username)
            json.put("password", password)
            json.put("other_contacts", contacts)
            json.put("service_termination_time", Date())

            val url = getString(R.string.ss_server_url)+"/users"
            val resJson = HttpUtil.httpJsonPost(url, json.toString(), HashMap())

            System.out.println(resJson)
            val userJObject = resJson.getJSONObject("user")
            val mapper = ObjectMapper()
            val user = mapper.readValue(userJObject.toString(), User::class.java)
            bundle.putString("token", resJson.getString("token"))
            bundle.putString("username", user.username)
        } catch (ex: UsernameConflictException) {
            val text = "注册失败\n用户名已存在"
            bundle.putString("errMsg", text)
        } catch (ex: Exception) {
            val text = "注册失败\n"+ex.message
            bundle.putString("errMsg", text)
        }
        val msg = Message()
        msg.data = bundle
        signUpHandler.sendMessage(msg)
    }

    private val signUpHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val data = msg.data;
            val errMsg = data.getString("errMsg")
            if (errMsg != null) {
                Log.e("",errMsg)
                Toast.makeText(applicationContext, errMsg, Toast.LENGTH_LONG).show()
                return
            }
            SharedPrefUtils.putValToSP(applicationContext, SharedPrefUtils.token, data.getString("token")) //保存当前用户名
            SharedPrefUtils.putValToSP(applicationContext, SharedPrefUtils.username, data.getString("username")) //保存当前用户id
            Toast.makeText(applicationContext, "注册成功，请微信联系coolcoolad，申请配额", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
