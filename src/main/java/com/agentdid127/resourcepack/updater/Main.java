package com.agentdid127.resourcepack.updater;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Main {
    private boolean gui = true;

    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String GET_URL_CONSOLE = "https://api.github.com/repos/agentdid127/ResourcePackConverter/releases";
    private static final String GET_URL_GUI = "https://api.github.com/repos/agentdid127/RPC-GUI/releases";

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        new Main(args);
    }

    public Main(String[] args) throws IOException, InterruptedException, URISyntaxException {

        String args2[];
        if (args.length > 0) {
        if (args[0].equalsIgnoreCase("nogui")) gui = false;
         args2 = new String[args.length-1];
        for (int i=1; i < args.length; i++) {
            args2[i-1] = args[i];
        }
        }
        else {
            args2 = new String[0];
        }
        if (gui) {
            update(args, GET_URL_GUI);
        } else {
            update(args2, GET_URL_CONSOLE);
        }
        }
        private void update(String[] args, String url) throws IOException, InterruptedException, URISyntaxException {
            GsonBuilder gsonBuilder = new GsonBuilder().disableHtmlEscaping();
            Gson gson = gsonBuilder.create();
            URI uri = new URI(url);
            JsonObject latest = gson.fromJson(getInfo(uri), JsonArray.class).get(0).getAsJsonObject();
            System.out.println("Retrieving Jar version: " + latest.get("tag_name").getAsString());


            String fileName = latest.getAsJsonArray("assets").get(0).getAsJsonObject().get("name").getAsString();
            InputStream in = new URI(latest.getAsJsonArray("assets").get(0).getAsJsonObject().get("browser_download_url").getAsString()).toURL().openStream();
            System.out.println("Downloading Jar");
            Files.copy(in, Paths.get("./" + fileName), StandardCopyOption.REPLACE_EXISTING);

            String[] args2 = new String[args.length + 3];
            args2[0] = "java";
            args2[1] = "-jar";
            args2[2] = fileName;
            for (int i = 0; i < args.length; i++) {
                args2[i + 3] = args[i];
            }

            System.out.println("Running Jar");
            ProcessBuilder pb = new ProcessBuilder(args2);
            Process p = pb.start();
            BufferedReader in2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s = "";
            while ((s = in2.readLine()) != null) {
                System.out.println(s);
            }
            int status = p.waitFor();
            System.out.println("Finished with Status: " + status);
            p.destroy();
            System.out.println("Deleting: " + fileName);
            Files.delete(Paths.get("./" + fileName));
            return;
        }



    private String getInfo(URI uri) throws IOException, URISyntaxException {


        HttpGet request = new HttpGet(uri);
        request.addHeader(HttpHeaders.USER_AGENT, USER_AGENT);

        try (CloseableHttpResponse response = httpClient.execute(request)) {

            System.out.println(response.getStatusLine().toString());

            HttpEntity entity = response.getEntity();
            Header headers = entity.getContentType();

            if (entity != null) {
                String result = EntityUtils.toString(entity);
                return result;
            }
            return null;
        }
    }
}
