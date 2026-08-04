package com.example.slidesave;

import okhttp3.*;
import java.io.IOException;

public class TelegramSender {
    private static final String BOT_TOKEN = "Enter your BOT TOKEN here";

    public static void sendMessage(String chatId,String message){
        OkHttpClient client = new OkHttpClient();

        String url = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage";

        RequestBody body = new FormBody.Builder()
                        .add("chat_id", chatId)
                        .add("text", message)
                        .build();

        Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();

        client.newCall(request).enqueue(new Callback(){
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }
                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                System.out.println(response.body().string());
                            }
                        });
    }
}