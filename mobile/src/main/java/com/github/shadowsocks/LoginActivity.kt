package com.github.shadowsocks

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.shadowsocks.model.User
import com.github.shadowsocks.utils.HttpUtil.httpJsonGet
import com.github.shadowsocks.utils.HttpUtil.httpJsonPost
import org.json.JSONObject
import com.github.shadowsocks.utils.SharedPrefUtils
import okhttp3.Credentials
import com.github.shadowsocks.utils.Util
import android.util.Log

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    override fun onResume() {
        super.onResume()
        val thread = Thread(Runnable { checkLogin() })
        thread.start()
    }

    private fun checkLogin() {
        val bundle = Bundle()
        try {
            val params = HashMap<String, String>()
            val token = SharedPrefUtils.getValFromSP(applicationContext, "token")
            params.put("access_token", token)

            val url = getString(R.string.ss_server_url)+"/users/me"
            val resJson = httpJsonGet(url, params, HashMap())

            System.out.println(resJson)
            val mapper = ObjectMapper()
            val user = mapper.readValue(resJson.toString(), User::class.java)
            bundle.putBoolean("isOverdue", Util.isOverdue(user.serviceTerminationTime))
        } catch (ex: Exception) {
            bundle.putString("errMsg", ex.message)
        }
        val msg = Message()
        msg.data = bundle
        checkLoginHandler.sendMessage(msg)
    }

    private val checkLoginHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val data = msg.data;
            val errMsg = data.getString("errMsg")
            if (errMsg != null) {
                Log.e("",errMsg)
                return
            }

            if (data.getBoolean("isOverdue")) {
                val builder = AlertDialog.Builder(applicationContext)
                builder.setTitle("提示")
                builder.setMessage("配额已用完，请微信联系coolcoolad，申请配额")
                builder.setCancelable(false)
                val dialog = builder.create()
                dialog.show()
                System.exit(0)
            }
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun onLoginBtn(view: View) {
        val usernameET = findViewById<EditText>(R.id.username_login)
        val passwordET = findViewById<EditText>(R.id.password_login)
        val username = usernameET.text.toString()
        val password = passwordET.text.toString()
        val thread = Thread(Runnable { userAuth(username, password) })
        thread.start()
    }

    private fun userAuth(username: String, password: String) {
        val bundle = Bundle()
        try {
            val json = JSONObject()
            json.put("access_token", getString(R.string.master_key))

            val credentials = Credentials.basic(username, password)
            val map = HashMap<String, String>()
            map.put("Authorization", credentials)

            val url = getString(R.string.ss_server_url)+"/auth"
            val resJson = httpJsonPost(url, json.toString(), map)

            System.out.println(resJson)
            val userJObject = resJson.getJSONObject("user")
            val mapper = ObjectMapper()
            val user = mapper.readValue(userJObject.toString(), User::class.java)
            bundle.putBoolean("isOverdue", Util.isOverdue(user.serviceTerminationTime))
            bundle.putString("token", resJson.getString("token"))
            bundle.putString("username", user.username)
        } catch (ex: Exception) {
            val text = "登录失败, 密码错误或者用户名不存在\n"+ex.message
            bundle.putString("errMsg", text)
        }
        val msg = Message()
        msg.data = bundle
        userAuthHandler.sendMessage(msg)
    }

    private val userAuthHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val data = msg.data;
            val errMsg = data.getString("errMsg")
            if (errMsg != null) {
                Log.e("",errMsg)
                Toast.makeText(applicationContext, errMsg, Toast.LENGTH_LONG).show()
                return
            }
            if (data.getBoolean("isOverdue")) {
                Toast.makeText(applicationContext, "请微信联系coolcoolad，申请配额", Toast.LENGTH_LONG).show()
                return
            }
            SharedPrefUtils.putValToSP(applicationContext, SharedPrefUtils.token, data.getString("token")) //保存当前用户名
            SharedPrefUtils.putValToSP(applicationContext, SharedPrefUtils.username, data.getString("username")) //保存当前用户id
            Toast.makeText(applicationContext, "登录成功", Toast.LENGTH_LONG).show()
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun onSignUpBtn(view: View) {
        val intent = Intent(this, SignUpActivity::class.java)
        this.startActivity(intent)
    }
}
