package com.edgar.taskflow.security.location;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import org.springframework.stereotype.Service;

@Service
public class IpLocationService {

    public String getLocation(String ip) {

        try {

            URL url = new URL("http://ip-api.com/json/" + ip);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(url.openStream())
            );

            String response = reader.readLine();

            if (response.contains("\"city\":\"")) {

                String city = response.split("\"city\":\"")[1].split("\"")[0];
                String country = response.split("\"country\":\"")[1].split("\"")[0];

                return city + ", " + country;
            }

        } catch (Exception ignored) {}

        return "Unknown location";
    }
}
